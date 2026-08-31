/*
 * Copyright 2007-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.ymate.platform.persistence.redis.support;

import net.ymate.platform.commons.impl.DefaultThreadFactory;
import net.ymate.platform.commons.util.DateTimeUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.commons.util.UUIDUtils;
import net.ymate.platform.persistence.redis.IRedis;
import net.ymate.platform.persistence.redis.IRedisCommander;
import net.ymate.platform.persistence.redis.IRedisConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Closeable;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 基于 Redis 的可靠分布式读写锁（重入 + 看门狗自动续期 + 多读者共享/写者互斥）。
 * <p>
 * 设计要点：
 * <ol>
 *     <li><b>线程级持有者</b>：每个线程拥有独立 token（ThreadLocal），只有加锁的线程才能释放自己的锁，从根本上杜绝误删他人锁。</li>
 *     <li><b>per-key 重入计数</b>：本地 ThreadLocal 维护每个 (key, 线程) 的重入次数，同线程同 key 多次 lock 只在首次走 SET NX，其余走 PEXPIRE 续约。</li>
 *     <li><b>读者共享</b>：读锁使用 Redis INCR/DECR 计数，多个读者并发持有；写锁互斥必须满足"无其他写者 AND 读计数=0"两个条件，Lua 原子保障。</li>
 *     <li><b>看门狗续期</b>：当调用方没有显式指定租约时间（使用默认 TTL）时，首次加锁成功后注册固定延迟调度任务，每隔 TTL/3 自动 PEXPIRE，
 *         只要线程存活就不会过期；线程 crash 则续期任务随 ThreadLocal 丢失，最多一个租约后 Redis 自动释放。</li>
 *     <li><b>抢锁失败必有反馈</b>：{@link #readLock(Object)} / {@link #writeLock(Object)} 不再静默返回，
 *         在 acquireTimeoutMillis 内拿不到锁即抛出 {@link IllegalStateException}（由包装层转为 CacheException）。</li>
 *     <li><b>细粒度 key monitor</b>：本地 monitor 不再是实例级 synchronized，而是按 lockKey 维度映射独立的 ReentrantLock，
 *         不同 key 的线程完全并行，只有争抢"同一个 key"的本地线程才会排队，大大提升吞吐量。</li>
 * </ol>
 *
 * @author 刘镇 (suninformation@163.com) on 2025/1/11 00:25
 * @since 2.1.4
 */
public class JedisLocker implements Closeable {

    private static final Log LOG = LogFactory.getLog(JedisLocker.class);

    // ---------------------------------------------------------------- Lua Scripts

    /**
     * 原子获取写锁：检查读计数是否为 0 → SET NX PX 写锁 key。
     * KEYS[1]=writeLockKey, KEYS[2]=readCountKey
     * ARGV[1]=threadToken, ARGV[2]=leaseMs
     * 返回 1=成功，0=失败
     */
    private static final String ACQUIRE_WRITE_LUA =
            "local rc = redis.call('get', KEYS[2]) " +
                    "if rc ~= false and tonumber(rc) > 0 then " +
                    "   return 0 " +
                    "end " +
                    "local ok = redis.call('set', KEYS[1], ARGV[1], 'NX', 'PX', ARGV[2]) " +
                    "if ok then return 1 else return 0 end";

    /**
     * 原子获取读锁：检查写锁是否被其他 token 持有 → INCR 读计数并刷新 TTL。
     * 注意：自己持有的写锁不阻塞自己的读锁（支持锁降级语义）。
     * KEYS[1]=readCountKey, KEYS[2]=writeLockKey
     * ARGV[1]=threadToken, ARGV[2]=leaseMs
     * 返回 1=成功，0=失败
     */
    private static final String ACQUIRE_READ_LUA =
            "local writer = redis.call('get', KEYS[2]) " +
                    "if writer ~= false and writer ~= ARGV[1] then " +
                    "   return 0 " +
                    "end " +
                    "redis.call('incr', KEYS[1]) " +
                    "redis.call('pexpire', KEYS[1], ARGV[2]) " +
                    "return 1";

    /**
     * 原子释放写锁：仅当 token 匹配时才 DEL。返回 1=真删除，0=不匹配或已不存在。
     * KEYS[1]=writeLockKey
     * ARGV[1]=threadToken
     */
    private static final String RELEASE_WRITE_LUA =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "   redis.call('del', KEYS[1]) " +
                    "   return 1 " +
                    "else " +
                    "   return 0 " +
                    "end";

    /**
     * 原子释放读锁：DECR 读计数，若计数<=0 则删除计数 key。
     * KEYS[1]=readCountKey
     */
    private static final String RELEASE_READ_LUA =
            "local rc = redis.call('decr', KEYS[1]) " +
                    "if rc <= 0 then " +
                    "   redis.call('del', KEYS[1]) " +
                    "end " +
                    "return 1";

    /**
     * 原子续期。
     * KEYS[1]=lockKey
     * ARGV[1]=valueCheck (写锁=threadToken，读锁="" 占位)
     * ARGV[2]=mode 'W'|'R'
     * ARGV[3]=leaseMs
     * 返回 1=成功，0=失败（key 不存在 / token 不匹配）
     */
    private static final String RENEW_LUA =
            "if ARGV[2] == 'W' then " +
                    "   if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "       return redis.call('pexpire', KEYS[1], ARGV[3]) " +
                    "   else " +
                    "       return 0 " +
                    "   end " +
                    "else " +
                    "   if redis.call('exists', KEYS[1]) == 1 then " +
                    "       return redis.call('pexpire', KEYS[1], ARGV[3]) " +
                    "   else " +
                    "       return 0 " +
                    "   end " +
                    "end";

    // ---------------------------------------------------------------- Watchdog

    /**
     * 续期周期 = lease / WATCHDOG_DIVISOR，行业默认 1/3。
     */
    private static final int WATCHDOG_DIVISOR = 3;

    /**
     * 看门狗启用开关的系统属性名：{@value}（默认 true）。
     * <p>
     * 设为 {@code false} 可全局禁用所有锁实例的自动续期——
     * 适用于用户希望严格依赖 TTL 自动过期、不希望后台周期任务占用线程池资源的场景。
     */
    public static final String PROP_WATCHDOG_ENABLED = "ymp.jedisLocker.watchdogEnabled";

    /**
     * 看门狗最小可用租约阈值（毫秒）：{@value}。
     * <p>
     * 当 {@code expiryInMillis} 低于此值时，说明用户有意使用短 TTL 快过期模式
     * （典型：锁临界区 ≤ TTL、依赖 Redis 被动过期兜底即可），
     * 此时调度看门狗反而会带来不必要的 ScheduledFuture 开销，自动跳过。
     */
    public static final long WATCHDOG_MIN_LEASE_MILLIS = 1000L;

    /**
     * 全局看门狗调度器（所有 JedisLocker 实例共用，避免资源浪费）。守护线程，JVM 退出时不阻塞。
     */
    private static final ScheduledExecutorService WATCHDOG_SCHEDULER;

    static {
        ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(
                Math.max(1, Math.min(2, Runtime.getRuntime().availableProcessors())),
                DefaultThreadFactory.create("redis-lock-watchdog")
                        .daemon(true)
                        .uncaughtExceptionHandler((t, e) -> LOG.error("Watchdog thread " + t.getName() + " failed", RuntimeUtils.unwrapThrow(e))));
        scheduler.setRemoveOnCancelPolicy(true);
        scheduler.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        WATCHDOG_SCHEDULER = Executors.unconfigurableScheduledExecutorService(scheduler);
        //
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            WATCHDOG_SCHEDULER.shutdownNow();
            try {
                if (!WATCHDOG_SCHEDULER.awaitTermination(2, TimeUnit.SECONDS) && LOG.isWarnEnabled()) {
                    LOG.warn("JedisLocker watchdog scheduler did not terminate gracefully within 2s.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "redis-lock-watchdog-shutdown"));
    }

    // ---------------------------------------------------------------- ThreadLocal State

    /**
     * 每个线程独立的锁持有者状态。线程退出或 ThreadLocal 清理时不会泄漏——
     * 只要 Watchdog 续期发现 key 不再存在就会 self-cancel。
     */
    private static final class ThreadLockState {
        /**
         * 本线程唯一身份标识，绝不复用。
         */
        final String token = UUIDUtils.UUID();
        /**
         * 写锁重入计数：key -> count
         */
        final Map<String, Integer> writeHoldings = new HashMap<>(4);
        /**
         * 读锁重入计数：key -> count
         */
        final Map<String, Integer> readHoldings = new HashMap<>(4);
        /**
         * 看门狗续期任务：key -> future，便于释放时 cancel
         */
        final Map<String, ScheduledFuture<?>> watchdogFutures = new HashMap<>(4);
    }

    private static final ThreadLocal<ThreadLockState> THREAD_STATE = ThreadLocal.withInitial(ThreadLockState::new);

    // ---------------------------------------------------------------- per-key monitor

    /**
     * 按 lockKey 维度做本地互斥，避免不同 key 互相阻塞。
     */
    private final ConcurrentHashMap<String, ReentrantLock> keyMonitors = new ConcurrentHashMap<>();

    private ReentrantLock monitorFor(String lockKey) {
        return keyMonitors.computeIfAbsent(lockKey, k -> new ReentrantLock(true));
    }

    // ---------------------------------------------------------------- Instance fields

    /**
     * 向后兼容：实例级标识（不再作为锁 token 使用，仅提供 getter 兼容旧代码）。
     */
    private final String instanceId = UUIDUtils.UUID();

    private final IRedis owner;

    private final String dataSourceName;

    private final long acquireTimeoutMillis;

    private final long expiryInMillis;

    private final int resolutionMillis;

    /**
     * 是否启用看门狗自动续期（实例级别）。
     * 最终生效条件 = {@code watchdogEnabled} 且 {@code expiryInMillis >= WATCHDOG_MIN_LEASE_MILLIS}。
     */
    private final boolean watchdogEnabled;

    /**
     * 标记本实例是否已关闭，close() 之后拒绝新的锁请求。
     */
    private volatile boolean closed;

    // ---------------------------------------------------------------- Constructors

    public JedisLocker(IRedis owner) {
        this(owner, null, 0, 0, 0, null);
    }

    public JedisLocker(IRedis owner, String dataSourceName) {
        this(owner, dataSourceName, 0, 0, 0, null);
    }

    public JedisLocker(IRedis owner, String dataSourceName, long acquireTimeoutMillis, long expiryInMillis, int resolutionMillis) {
        this(owner, dataSourceName, acquireTimeoutMillis, expiryInMillis, resolutionMillis, null);
    }

    /**
     * 全参数构造器（所有对外构造最终都委托到此）。
     *
     * @param owner                所属 Redis 管理器，非空
     * @param dataSourceName       数据源名，空则使用 {@link IRedisConfig#DEFAULT_STR}
     * @param acquireTimeoutMillis 获取锁超时（毫秒），<=0 读取系统属性或默认 10s
     * @param expiryInMillis       Redis 锁 key TTL（毫秒），<=0 读取系统属性或默认 60s
     * @param resolutionMillis     抢锁重试间隔（毫秒），<=0 读取系统属性或默认 100ms
     * @param watchdogEnabled      看门狗显式开关；{@code null} 表示读取系统属性
     *                             {@value #PROP_WATCHDOG_ENABLED}（默认 true）
     */
    public JedisLocker(IRedis owner, String dataSourceName, long acquireTimeoutMillis, long expiryInMillis, int resolutionMillis, Boolean watchdogEnabled) {
        this.owner = Objects.requireNonNull(owner, "owner");
        this.dataSourceName = StringUtils.defaultIfBlank(dataSourceName, IRedisConfig.DEFAULT_STR);
        this.acquireTimeoutMillis = acquireTimeoutMillis > 0 ? acquireTimeoutMillis : Long.getLong("ymp.jedisLocker.acquireTimeoutMillis", 10L * DateTimeUtils.SECOND);
        this.expiryInMillis = expiryInMillis > 0 ? expiryInMillis : Long.getLong("ymp.jedisLocker.expiryMillis", 60L * DateTimeUtils.SECOND);
        this.resolutionMillis = resolutionMillis > 0 ? resolutionMillis : Integer.getInteger("ymp.jedisLocker.resolutionMillis", 100);
        if (watchdogEnabled != null) {
            this.watchdogEnabled = watchdogEnabled;
        } else {
            this.watchdogEnabled = !"false".equalsIgnoreCase(System.getProperty(PROP_WATCHDOG_ENABLED));
        }
    }

    // ---------------------------------------------------------------- Key builders

    private String writeLockKeyOf(String lockKey) {
        return lockKey + "_write_lock";
    }

    private String readCountKeyOf(String lockKey) {
        return lockKey + "_read_lock:cnt";
    }

    /**
     * 将用户传入的任意 key 对象归一化为字符串。非空直接 toString，空抛 IAE。
     */
    private static String toSafeLockKey(Object key) {
        if (key == null) {
            throw new IllegalArgumentException("Lock key must not be null.");
        }
        return key.toString();
    }

    // ---------------------------------------------------------------- Watchdog helpers

    /**
     * 注册看门狗续期。
     *
     * @param lockKey     用户层面的归一化 lockKey（非 Redis 存储 key）
     * @param redisKey    真正要 PEXPIRE 的 Redis key（写锁或读计数）
     * @param threadToken 当前线程 token（写锁校验用，读锁传空字符串占位）
     * @param mode        'W' 或 'R'
     * @param leaseMillis 租约时长
     */
    private void startWatchdog(String lockKey, String redisKey, String threadToken, String mode, long leaseMillis) {
        long interval = Math.max(1L, leaseMillis / WATCHDOG_DIVISOR);
        ThreadLockState state = THREAD_STATE.get();
        ScheduledFuture<?> existing = state.watchdogFutures.get(lockKey + mode);
        if (existing != null && !existing.isDone()) {
            // 重入：续期任务已在跑，无需重复注册
            return;
        }
        ScheduledFuture<?> future = WATCHDOG_SCHEDULER.scheduleWithFixedDelay(() -> {
            try {
                Object ret;
                try (IRedisCommander commander = owner.getConnectionHolder(dataSourceName).getConnection()) {
                    ret = commander.eval(RENEW_LUA,
                            Collections.singletonList(redisKey),
                            Arrays.asList(threadToken, mode, String.valueOf(leaseMillis)));
                }
                if (!Long.valueOf(1).equals(ret)) {
                    // 续期失败（key 不存在或 token 不匹配）：自我注销
                    cancelWatchdog(lockKey, mode);
                }
            } catch (Exception e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("Renew lock '%s' mode=%s transiently failed, will retry next cycle", redisKey, mode), RuntimeUtils.unwrapThrow(e));
                }
            }
        }, interval, interval, TimeUnit.MILLISECONDS);
        state.watchdogFutures.put(lockKey + mode, future);
    }

    private void cancelWatchdog(String lockKey, String mode) {
        ThreadLockState state = THREAD_STATE.get();
        ScheduledFuture<?> f = state.watchdogFutures.remove(lockKey + mode);
        if (f != null) {
            f.cancel(false);
        }
        if (state.watchdogFutures.isEmpty()
                && state.writeHoldings.isEmpty()
                && state.readHoldings.isEmpty()) {
            THREAD_STATE.remove();
        }
    }

    // ---------------------------------------------------------------- Core Locking

    private long resolveAcquireTimeout(long timeout) {
        return timeout > 0 ? timeout : acquireTimeoutMillis;
    }

    private boolean doAcquireWrite(String lockKey, long timeoutMillis) throws Exception {
        final long lease = expiryInMillis;
        // 看门狗生效条件：1) 实例/系统开关显式开；2) 租约足够长，续期才有意义（否则退化为纯 TTL 自过期）
        final boolean useWatchdog = watchdogEnabled && lease >= WATCHDOG_MIN_LEASE_MILLIS;
        ThreadLockState state = THREAD_STATE.get();
        ReentrantLock monitor = monitorFor(lockKey);

        // 1) 重入：本线程已持有写锁 → 续约 + bump 计数（重入路径不需要重试轮询，保持 monitor 即可）
        monitor.lock();
        try {
            Integer held = state.writeHoldings.get(lockKey);
            if (held != null && held > 0) {
                boolean renewed = doRenewWriteLease(lockKey, state.token, lease);
                if (renewed) {
                    state.writeHoldings.put(lockKey, held + 1);
                    return true;
                }
                // 续约失败：远端锁被覆盖/过期，fallback 到循环抢锁（退出 monitor 再轮询）
            }
        } finally {
            monitor.unlock();
        }

        // 2) 首次或重入失败 → 循环尝试 SET NX。
        //    注意：轮询 + Thread.sleep 必须放在 monitor 之外，否则一个线程的重试间隔
        //    会阻塞所有相同 key 的其他等待者（饥饿/超时雪崩）。monitor 仅用于一次原子尝试 + 本地状态更新。
        long deadline = System.currentTimeMillis() + timeoutMillis;
        long remain = timeoutMillis;
        while (remain >= 0) {
            ensureNotClosed();
            boolean acquired = false;
            monitor.lock();
            try {
                // double-check: 可能在 sleep 期间，本线程通过别的路径已经拿到了重入锁
                Integer heldNow = state.writeHoldings.get(lockKey);
                if (heldNow != null && heldNow > 0) {
                    boolean renewed = doRenewWriteLease(lockKey, state.token, lease);
                    if (renewed) {
                        state.writeHoldings.put(lockKey, heldNow + 1);
                        return true;
                    }
                }
                boolean ok = doTrySetWriteLockLua(lockKey, state.token, lease);
                if (ok) {
                    state.writeHoldings.put(lockKey, 1);
                    if (useWatchdog) {
                        startWatchdog(lockKey, writeLockKeyOf(lockKey), state.token, "W", lease);
                    }
                    acquired = true;
                }
            } finally {
                monitor.unlock();
            }
            if (acquired) {
                return true;
            }
            if (remain < resolutionMillis) {
                return false;
            }
            Thread.sleep(resolutionMillis);
            remain = deadline - System.currentTimeMillis();
        }
        return false;
    }

    private boolean doAcquireRead(String lockKey, long timeoutMillis) throws Exception {
        final long lease = expiryInMillis;
        // 与写锁同规则：避免调度无意义的超短租约续期任务
        final boolean useWatchdog = watchdogEnabled && lease >= WATCHDOG_MIN_LEASE_MILLIS;
        ThreadLockState state = THREAD_STATE.get();
        ReentrantLock monitor = monitorFor(lockKey);

        // 1) 重入：本线程已持有读锁 → 续约 + bump 计数
        monitor.lock();
        try {
            Integer held = state.readHoldings.get(lockKey);
            if (held != null && held > 0) {
                boolean renewed = doRenewReadLease(lockKey, lease);
                if (renewed) {
                    state.readHoldings.put(lockKey, held + 1);
                    return true;
                }
            }
            // 写锁持有中 → 锁降级必然成功（Lua 判断 token 相同放行），无需轮询
            Integer writeHeld = state.writeHoldings.get(lockKey);
            if (writeHeld != null && writeHeld > 0) {
                boolean ok = doTrySetReadLockLua(lockKey, state.token, lease);
                if (ok) {
                    state.readHoldings.put(lockKey, 1);
                    if (useWatchdog) {
                        startWatchdog(lockKey, readCountKeyOf(lockKey), "", "R", lease);
                    }
                    return true;
                }
            }
        } finally {
            monitor.unlock();
        }

        // 2) 正常抢读锁：monitor 外轮询，避免 sleep 时阻塞其他线程
        long deadline = System.currentTimeMillis() + timeoutMillis;
        long remain = timeoutMillis;
        while (remain >= 0) {
            ensureNotClosed();
            boolean acquired = false;
            monitor.lock();
            try {
                Integer heldNow = state.readHoldings.get(lockKey);
                if (heldNow != null && heldNow > 0) {
                    boolean renewed = doRenewReadLease(lockKey, lease);
                    if (renewed) {
                        state.readHoldings.put(lockKey, heldNow + 1);
                        return true;
                    }
                }
                Integer writeNow = state.writeHoldings.get(lockKey);
                if (writeNow != null && writeNow > 0) {
                    // 锁降级路径：本线程持写，Lua 必放行
                    boolean ok = doTrySetReadLockLua(lockKey, state.token, lease);
                    if (ok) {
                        state.readHoldings.put(lockKey, 1);
                        if (useWatchdog) {
                            startWatchdog(lockKey, readCountKeyOf(lockKey), "", "R", lease);
                        }
                        acquired = true;
                    }
                } else {
                    boolean ok = doTrySetReadLockLua(lockKey, state.token, lease);
                    if (ok) {
                        state.readHoldings.put(lockKey, 1);
                        if (useWatchdog) {
                            startWatchdog(lockKey, readCountKeyOf(lockKey), "", "R", lease);
                        }
                        acquired = true;
                    }
                }
            } finally {
                monitor.unlock();
            }
            if (acquired) {
                return true;
            }
            if (remain < resolutionMillis) {
                return false;
            }
            Thread.sleep(resolutionMillis);
            remain = deadline - System.currentTimeMillis();
        }
        return false;
    }

    private boolean doTrySetWriteLockLua(String lockKey, String token, long lease) throws Exception {
        try (IRedisCommander commander = owner.getConnectionHolder(dataSourceName).getConnection()) {
            Object ret = commander.eval(ACQUIRE_WRITE_LUA,
                    Arrays.asList(writeLockKeyOf(lockKey), readCountKeyOf(lockKey)),
                    Arrays.asList(token, String.valueOf(lease)));
            return Long.valueOf(1).equals(ret);
        }
    }

    private boolean doTrySetReadLockLua(String lockKey, String token, long lease) throws Exception {
        try (IRedisCommander commander = owner.getConnectionHolder(dataSourceName).getConnection()) {
            Object ret = commander.eval(ACQUIRE_READ_LUA,
                    Arrays.asList(readCountKeyOf(lockKey), writeLockKeyOf(lockKey)),
                    Arrays.asList(token, String.valueOf(lease)));
            return Long.valueOf(1).equals(ret);
        }
    }

    private boolean doRenewWriteLease(String lockKey, String token, long lease) throws Exception {
        try (IRedisCommander commander = owner.getConnectionHolder(dataSourceName).getConnection()) {
            Object ret = commander.eval(RENEW_LUA,
                    Collections.singletonList(writeLockKeyOf(lockKey)),
                    Arrays.asList(token, "W", String.valueOf(lease)));
            return Long.valueOf(1).equals(ret);
        }
    }

    private boolean doRenewReadLease(String lockKey, long lease) throws Exception {
        try (IRedisCommander commander = owner.getConnectionHolder(dataSourceName).getConnection()) {
            Object ret = commander.eval(RENEW_LUA,
                    Collections.singletonList(readCountKeyOf(lockKey)),
                    Arrays.asList("", "R", String.valueOf(lease)));
            return Long.valueOf(1).equals(ret);
        }
    }

    private boolean doReleaseWrite(String lockKey) throws Exception {
        ThreadLockState state = THREAD_STATE.get();
        ReentrantLock monitor = monitorFor(lockKey);
        monitor.lock();
        try {
            Integer held = state.writeHoldings.get(lockKey);
            if (held == null || held <= 0) {
                return false; // 本线程未持有该写锁
            }
            held -= 1;
            if (held > 0) {
                state.writeHoldings.put(lockKey, held);
                return true; // 仍处于重入层，不释放 Redis，只减计数
            }
            // 重入归零：真正释放 Redis 并取消续期
            state.writeHoldings.remove(lockKey);
            cancelWatchdog(lockKey, "W");
            try (IRedisCommander commander = owner.getConnectionHolder(dataSourceName).getConnection()) {
                commander.eval(RELEASE_WRITE_LUA,
                        Collections.singletonList(writeLockKeyOf(lockKey)),
                        Collections.singletonList(state.token));
            }
            return true;
        } finally {
            monitor.unlock();
        }
    }

    private boolean doReleaseRead(String lockKey) throws Exception {
        ThreadLockState state = THREAD_STATE.get();
        ReentrantLock monitor = monitorFor(lockKey);
        monitor.lock();
        try {
            Integer held = state.readHoldings.get(lockKey);
            if (held == null || held <= 0) {
                return false;
            }
            held -= 1;
            if (held > 0) {
                state.readHoldings.put(lockKey, held);
                return true;
            }
            state.readHoldings.remove(lockKey);
            cancelWatchdog(lockKey, "R");
            try (IRedisCommander commander = owner.getConnectionHolder(dataSourceName).getConnection()) {
                commander.eval(RELEASE_READ_LUA,
                        Collections.singletonList(readCountKeyOf(lockKey)),
                        Collections.emptyList());
            }
            return true;
        } finally {
            monitor.unlock();
        }
    }

    private void ensureNotClosed() {
        if (closed) {
            throw new IllegalStateException("JedisLocker has already been closed.");
        }
    }

    // ---------------------------------------------------------------- Public API (对外契约不变，但失败时不再静默)

    /**
     * 阻塞获取读锁。超时仍未获取到则抛出 {@link IllegalStateException}。
     *
     * @param key 锁目标，非空
     * @throws IllegalStateException    超时未获锁
     * @throws IllegalArgumentException key 为 null
     * @throws Exception                Redis 层 IO 异常
     */
    public void readLock(Object key) throws Exception {
        String lockKey = toSafeLockKey(key);
        long timeout = resolveAcquireTimeout(0);
        if (!doAcquireRead(lockKey, timeout)) {
            throw new IllegalStateException(String.format(
                    "Failed to acquire read lock for '%s' within %d ms.", lockKey, timeout));
        }
    }

    /**
     * 阻塞获取写锁。超时仍未获取到则抛出 {@link IllegalStateException}。
     *
     * @param key 锁目标，非空
     * @throws IllegalStateException    超时未获锁
     * @throws IllegalArgumentException key 为 null
     * @throws Exception                Redis 层 IO 异常
     */
    public void writeLock(Object key) throws Exception {
        String lockKey = toSafeLockKey(key);
        long timeout = resolveAcquireTimeout(0);
        if (!doAcquireWrite(lockKey, timeout)) {
            throw new IllegalStateException(String.format(
                    "Failed to acquire write lock for '%s' within %d ms.", lockKey, timeout));
        }
    }

    /**
     * 尝试获取读锁，最多等待 timeout 毫秒。返回 false 表示超时未取到。
     *
     * @param key     锁目标，非空
     * @param timeout 毫秒；<=0 时使用配置的 acquireTimeoutMillis
     */
    public boolean tryReadLock(Object key, long timeout) throws Exception {
        return doAcquireRead(toSafeLockKey(key), resolveAcquireTimeout(timeout));
    }

    /**
     * 尝试获取写锁，最多等待 timeout 毫秒。返回 false 表示超时未取到。
     *
     * @param key     锁目标，非空
     * @param timeout 毫秒；<=0 时使用配置的 acquireTimeoutMillis
     */
    public boolean tryWriteLock(Object key, long timeout) throws Exception {
        return doAcquireWrite(toSafeLockKey(key), resolveAcquireTimeout(timeout));
    }

    /**
     * 释放读锁。若本线程未持有该锁，记录 debug 日志，不抛异常（幂等释放）。
     */
    public void releaseReadLock(Object key) throws Exception {
        String lockKey = toSafeLockKey(key);
        if (!doReleaseRead(lockKey) && LOG.isDebugEnabled()) {
            LOG.debug(String.format("Thread '%s' attempted to release read lock '%s' but did not hold it.",
                    Thread.currentThread().getName(), lockKey));
        }
    }

    /**
     * 释放写锁。若本线程未持有该锁，记录 debug 日志，不抛异常（幂等释放）。
     */
    public void releaseWriteLock(Object key) throws Exception {
        String lockKey = toSafeLockKey(key);
        if (!doReleaseWrite(lockKey) && LOG.isDebugEnabled()) {
            LOG.debug(String.format("Thread '%s' attempted to release write lock '%s' but did not hold it.",
                    Thread.currentThread().getName(), lockKey));
        }
    }

    // ---------------------------------------------------------------- Getters (向后兼容)

    public IRedis getOwner() {
        return owner;
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

    /**
     * @deprecated 锁持有者 token 已迁移为 ThreadLocal 级别，此方法返回的是实例级一次性 ID，
     * 仅保留作为 getter 兼容旧代码，不可再用于 Redis SET 值。
     */
    @Deprecated
    public String getUuid() {
        return instanceId;
    }

    public long getAcquireTimeoutMillis() {
        return acquireTimeoutMillis;
    }

    public long getExpiryInMillis() {
        return expiryInMillis;
    }

    public int getResolutionMillis() {
        return resolutionMillis;
    }

    /**
     * 当前线程是否持有任意一个读锁？
     */
    public boolean isReadLocked() {
        return !THREAD_STATE.get().readHoldings.isEmpty();
    }

    /**
     * 当前线程是否持有任意一个写锁？
     */
    public boolean isWriteLocked() {
        return !THREAD_STATE.get().writeHoldings.isEmpty();
    }

    @Override
    public void close() {
        this.closed = true;
        // 不主动去 Redis 上删除 key（持有者可能还在临界区）。
        // 看门狗续期任务发现本实例 closed 或 key 不存在会自行退出；
        // 用户线程退出后，ThreadLocal 不清理也无碍，最多 leaseMillis 后锁自动过期。
        keyMonitors.clear();
    }
}
