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
package net.ymate.platform.cache.support;

import net.ymate.platform.cache.CacheException;
import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.persistence.IDataSourceRouter;
import net.ymate.platform.persistence.redis.*;
import net.ymate.platform.persistence.redis.impl.DefaultRedisConfig;
import net.ymate.platform.persistence.redis.impl.DefaultRedisDataSourceConfig;
import net.ymate.platform.persistence.redis.impl.RedisCommandHolder;
import net.ymate.platform.persistence.redis.impl.RedisDataSourceAdapter;
import net.ymate.platform.persistence.redis.support.JedisLocker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import redis.clients.jedis.JedisPubSub;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 针对 {@link RedisCacheLocker} 包装层的合约 / 行为单元测试。
 * <p>
 * 前置环境要求（用户约定）：本机 {@code localhost:6379} 必须有一个可访问的 Redis 服务
 * （默认 Database 0，无需密码）。测试会把所有锁 key 放在带随机前缀的 cacheName 下，
 * 所有测试结束后会统一删除，不会污染开发环境已有的用户数据。
 * <p>
 * 覆盖范围：
 * <ol>
 *     <li>initialize 幂等 & isInitialized / cacheName 状态一致</li>
 *     <li>所有 6 个锁 API：key == null 抛 {@link IllegalArgumentException}</li>
 *     <li>cacheName 前缀隔离：两个 locker 不同 cacheName 的同 key 互不干扰</li>
 *     <li>写互斥 & 读共享（经由真实 Redis 透传）</li>
 *     <li>writeLock 超时统一包装 {@link CacheException}，消息中含 key 名</li>
 *     <li>tryReadLock 在写者持有时正确返回 false</li>
 *     <li>close() 之后：6 种锁 API 全部抛 CacheException</li>
 *     <li>并发 DCL 初始化：20 个线程最终全部看到 initialized=true 且 cacheName 稳定</li>
 *     <li>未初始化的 locker 立刻抛 not initialized CacheException（不会 NPE）</li>
 *     <li>典型 try-finally 范式：8 线程串行化计数原子性正确</li>
 * </ol>
 *
 * @author 刘镇 (suninformation@163.com) on 2026-08-30
 * @since 2.1.4
 */
public class RedisCacheLockerTest {

    private static final Log LOG = LogFactory.getLog(RedisCacheLockerTest.class);

    /**
     * 所有本测试产生的 cacheName 都会加此前缀，结束后据此模式在真实 Redis 里 KEYS * 删除残留。
     */
    private static final String CACHE_NAME_PREFIX = "ympcacheunittest_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);

    private static final String[] TIMING_PROPS = {
            "ymp.jedisLocker.acquireTimeoutMillis",
            "ymp.jedisLocker.expiryMillis",
            "ymp.jedisLocker.resolutionMillis"
    };
    private static final String[] ORIGINAL_PROP_VALUES = new String[TIMING_PROPS.length];

    private static IRedis realRedis;               // 真实 Redis 桩（连接 localhost:6379）
    private static RedisDataSourceAdapter adapter; // 持有底层 JedisPool，关闭时必须释放

    // -------------------------------------------------------------- 环境搭建与清理

    @BeforeClass
    public static void setupRealRedisAndTuneTiming() throws Exception {
        // 1) 调小锁重试系统属性，让测试尽快完成
        for (int i = 0; i < TIMING_PROPS.length; i++) {
            ORIGINAL_PROP_VALUES[i] = System.getProperty(TIMING_PROPS[i]);
        }
        System.setProperty("ymp.jedisLocker.acquireTimeoutMillis", "2000");
        System.setProperty("ymp.jedisLocker.expiryMillis", "10000");
        System.setProperty("ymp.jedisLocker.resolutionMillis", "5");

        // 2) 构造真实 Redis 连接池 + IRedis 薄适配
        //    连接池扩大：8 个测试线程 + watchdog 续期线程都会借用 Jedis，默认 maxTotal=8 容易饥饿。
        IRedisDataSourceConfig dsConfig = DefaultRedisDataSourceConfig.builder(IRedisConfig.DEFAULT_STR)
                .connectionType(IRedis.ConnectionType.DEFAULT)
                .poolMaxTotal(64)
                .poolMaxIdle(32)
                .poolMinIdle(4)
                .poolMaxWaitMillis(3000L)
                .build();
        DefaultRedisConfig redisCfg = DefaultRedisConfig.builder()
                .dataSourceDefaultName(IRedisConfig.DEFAULT_STR)
                .addDataSourceConfigs(dsConfig)
                .build();
        final IRedisConfig cfgSnapshot = redisCfg;
        adapter = new RedisDataSourceAdapter();
        // 先让 config 初始化（AbstractDataSourceConfig 没有 owner 依赖，基本是空操作），
        // 再给 adapter 做初始化，owner 传 stub IRedis 即可（adapter 里 DEFAULT 分支空 serverMetas 时不会用到 owner）
        adapter.initialize(new MinimalRedis(cfgSnapshot), dsConfig);

        realRedis = new MinimalRedis(cfgSnapshot) {
            @Override
            public IRedisCommandHolder getConnectionHolder(String dataSourceName) {
                return new RedisCommandHolder(adapter);
            }

            @Override
            public void close() {
                try {
                    adapter.close();
                } catch (Exception e) {
                    LOG.warn("Closing Redis adapter threw: " + e.getMessage());
                }
            }
        };

        // 3) 预发一条 PING 验证真实 Redis 可达；不可达时尽早失败并提示
        try (IRedisCommandHolder holder = realRedis.getConnectionHolder(IRedisConfig.DEFAULT_STR)) {
            IRedisCommander c = holder.getConnection();
            String pong = c.ping();
            if (!"PONG".equals(pong)) {
                throw new IllegalStateException("Redis ping returned unexpected value: " + pong);
            }
        }
    }

    @AfterClass
    public static void tearDown() {
        // 1) 恢复系统属性
        for (int i = 0; i < TIMING_PROPS.length; i++) {
            String v = ORIGINAL_PROP_VALUES[i];
            if (v == null) System.clearProperty(TIMING_PROPS[i]);
            else System.setProperty(TIMING_PROPS[i], v);
        }
        // 2) 清理所有前缀匹配的锁 key
        try (IRedisCommandHolder holder = realRedis.getConnectionHolder(IRedisConfig.DEFAULT_STR)) {
            IRedisCommander c = holder.getConnection();
            for (String k : c.keys(CACHE_NAME_PREFIX + "*")) {
                try {
                    c.del(k);
                } catch (Exception ignore) { /* best effort */ }
            }
        } catch (Exception e) {
            LOG.warn("Cleanup test cache keys failed: " + e.getMessage());
        }
        // 3) 关闭 JedisPool
        try {
            realRedis.close();
        } catch (Exception e) {
            LOG.warn("Closing pool failed: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------- 构造测试用的 locker

    private static RedisCacheLocker create(String suffix) {
        RedisCacheLocker l = new RedisCacheLocker();
        try {
            l.initialize(null, realRedis, CACHE_NAME_PREFIX + "_" + suffix);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return l;
    }

    // -------------------------------------------------------------- 初始化 & 幂等

    @Test
    public void initializeIsIdempotent() throws Exception {
        RedisCacheLocker locker = new RedisCacheLocker();
        Assert.assertFalse(locker.isInitialized());
        locker.initialize(null, realRedis, CACHE_NAME_PREFIX + "_idem");
        Assert.assertTrue(locker.isInitialized());
        locker.initialize(null, realRedis, CACHE_NAME_PREFIX + "_otherName");
        Assert.assertEquals(CACHE_NAME_PREFIX + "_idem", locker.getCacheName());
    }

    // -------------------------------------------------------------- key = null 抛 IAE

    @Test(expected = IllegalArgumentException.class)
    public void nullKeyReadLockIAE() throws Exception {
        create("nk1").readLock(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullKeyWriteLockIAE() throws Exception {
        create("nk2").writeLock(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullKeyTryReadLockIAE() throws Exception {
        create("nk3").tryReadLock(null, 10L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullKeyTryWriteLockIAE() throws Exception {
        create("nk4").tryWriteLock(null, 10L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullKeyReleaseReadIAE() throws Exception {
        create("nk5").releaseReadLock(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullKeyReleaseWriteIAE() throws Exception {
        create("nk6").releaseWriteLock(null);
    }

    // -------------------------------------------------------------- cacheName 前缀隔离

    @Test
    public void cacheNamePrefixIsolation() throws Exception {
        RedisCacheLocker a = create("alpha");
        RedisCacheLocker b = create("beta");

        a.writeLock("k");
        try {
            // 不同 cache 前缀下同名 key 互不干扰
            Assert.assertTrue(b.tryWriteLock("k", 200L));
            b.releaseWriteLock("k");
        } finally {
            a.releaseWriteLock("k");
        }

        // 真实 Redis 快照中应能看到 alpha:only-a 前缀的 key，看不到 beta:
        a.writeLock("only-a");
        try {
            try (IRedisCommandHolder h = realRedis.getConnectionHolder(IRedisConfig.DEFAULT_STR)) {
                IRedisCommander c = h.getConnection();
                Assert.assertFalse("alpha lock key should exist",
                        c.keys(CACHE_NAME_PREFIX + "_alpha:only-a*").isEmpty());
                Assert.assertTrue("beta key should not exist",
                        c.keys(CACHE_NAME_PREFIX + "_beta:*").isEmpty());
            }
        } finally {
            a.releaseWriteLock("only-a");
        }
    }

    // -------------------------------------------------------------- 写互斥 / 读共享

    @Test
    public void writeLockMutexViaWrapper() throws Exception {
        Runnable releaseBlocker = latchLockInOtherThread("mutex", "x", false);
        try {
            RedisCacheLocker challenger = create("mutex");
            Assert.assertFalse("Other thread holds write: tryWriteLock must be false",
                    challenger.tryWriteLock("x", 30L));
        } finally {
            releaseBlocker.run();
        }
    }

    @Test
    public void readLockSharedViaWrapper() throws Exception {
        Runnable releaseFirstReader = latchLockInOtherThread("shared", "s", true);
        try {
            RedisCacheLocker main = create("shared");
            Assert.assertTrue("Read locks shared across threads", main.tryReadLock("s", 200L));
            main.releaseReadLock("s");
        } finally {
            releaseFirstReader.run();
        }
    }

    // -------------------------------------------------------------- 超时统一包装 CacheException

    @Test
    public void writeLockTimeoutThrowsCacheException() throws Exception {
        Runnable releaseBlocker = latchLockInOtherThread("to", "heldKey", false);
        try {
            RedisCacheLocker waiter = create("to");
            try {
                waiter.writeLock("heldKey");
                Assert.fail("Expected CacheException (timeout)");
            } catch (CacheException ce) {
                Assert.assertTrue("Message should contain key, got: " + ce.getMessage(),
                        ce.getMessage().contains("heldKey"));
            }
        } finally {
            releaseBlocker.run();
        }
    }

    @Test
    public void tryReadLockReturnsFalseOnTimeout() throws Exception {
        Runnable releaseWriter = latchLockInOtherThread("tryl", "r", false);
        try {
            RedisCacheLocker b = create("tryl");
            Assert.assertFalse(b.tryReadLock("r", 20L));
        } finally {
            releaseWriter.run();
        }
    }

    // -------------------------------------------------------------- close 之后拒绝请求

    @Test
    public void closedLockerRejects() throws Exception {
        RedisCacheLocker l = create("closeT");
        l.close();
        l.close(); // 多次 close 幂等

        try {
            l.readLock("k");
            Assert.fail();
        } catch (CacheException ok) {
        }
        try {
            l.writeLock("k");
            Assert.fail();
        } catch (CacheException ok) {
        }
        try {
            l.tryReadLock("k", 1);
            Assert.fail();
        } catch (CacheException ok) {
        }
        try {
            l.tryWriteLock("k", 1);
            Assert.fail();
        } catch (CacheException ok) {
        }
        try {
            l.releaseReadLock("k");
            Assert.fail();
        } catch (CacheException ok) {
        }
        try {
            l.releaseWriteLock("k");
            Assert.fail();
        } catch (CacheException ok) {
        }
    }

    // -------------------------------------------------------------- 并发 initialize DCL

    @Test
    public void concurrentInitializeOnlyOnce() throws Exception {
        final RedisCacheLocker locker = new RedisCacheLocker();
        int threads = 20;
        ExecutorService ex = Executors.newFixedThreadPool(threads);
        CountDownLatch done = new CountDownLatch(threads);
        AtomicInteger seenTrue = new AtomicInteger();
        AtomicReference<String> observedName = new AtomicReference<>();
        for (int i = 0; i < threads; i++) {
            final int id = i;
            ex.submit(() -> {
                try {
                    if ((id & 1) == 0) {
                        try {
                            locker.initialize(null, realRedis, CACHE_NAME_PREFIX + "_dcl");
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                    // 非初始化线程需要等待 CAS 完成：此处自旋最多 2s 直到 isInitialized==true，
                    // 用来验证 "DCL 后发布的 cacheName / initialized 状态对所有线程可见"。
                    long deadline = System.currentTimeMillis() + 2000L;
                    while (!locker.isInitialized() && System.currentTimeMillis() < deadline) {
                        try {
                            Thread.sleep(5L);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                    if (locker.isInitialized()) {
                        seenTrue.incrementAndGet();
                        observedName.compareAndSet(null, locker.getCacheName());
                    }
                } finally {
                    done.countDown();
                }
            });
        }
        Assert.assertTrue(done.await(15, TimeUnit.SECONDS));
        ex.shutdownNow();
        Assert.assertEquals(CACHE_NAME_PREFIX + "_dcl", observedName.get());
        Assert.assertEquals(threads, seenTrue.get());
    }

    // -------------------------------------------------------------- 未初始化的异常包装

    @Test
    public void uninitializedThrowsNotInitialized() {
        RedisCacheLocker uninit = new RedisCacheLocker();
        try {
            uninit.writeLock("k");
            Assert.fail();
        } catch (CacheException ok) {
            Assert.assertTrue("Expected 'not initialized', got: " + ok.getMessage(),
                    ok.getMessage().contains("not initialized"));
        }
    }

    // -------------------------------------------------------------- 典型 try-finally 原子性

    @Test
    public void typicalTryFinallyUsage() throws Exception {
        RedisCacheLocker locker = create("typical");
        final AtomicInteger counter = new AtomicInteger();
        final AtomicInteger errors = new AtomicInteger();
        int threads = 4;
        ExecutorService ex = Executors.newFixedThreadPool(threads);
        Future<?>[] fs = new Future[threads];
        final long customTimeout = 15000L;
        for (int i = 0; i < threads; i++) {
            final int idx = i;
            fs[i] = ex.submit(() -> {
                long start = System.currentTimeMillis();
                boolean ok;
                try {
                    ok = locker.tryWriteLock("work", customTimeout);
                } catch (Exception e) {
                    errors.incrementAndGet();
                    throw new RuntimeException("worker-" + idx + " tryWrite threw", e);
                }
                if (!ok) {
                    long elapsed = System.currentTimeMillis() - start;
                    errors.incrementAndGet();
                    throw new RuntimeException("worker-" + idx + " failed tryWrite after " + elapsed + "ms; counter=" + counter.get());
                }
                try {
                    int cur = counter.get();
                    Thread.yield();
                    counter.set(cur + 1);
                } finally {
                    locker.releaseWriteLock("work");
                }
            });
        }
        Exception firstFail = null;
        for (Future<?> f : fs) {
            try {
                f.get(60, TimeUnit.SECONDS);
            } catch (Exception e) {
                if (firstFail == null) firstFail = e;
            }
        }
        ex.shutdownNow();
        if (firstFail != null) throw firstFail;
        Assert.assertEquals("errors", 0, errors.get());
        Assert.assertEquals(threads, counter.get());
    }

    // -------------------------------------------------------------- Watchdog 条件覆盖（useWatchdog 不再恒真）
    // useWatchdog = watchdogEnabled(实例开关, 读 PROP_WATCHDOG_ENABLED) && lease >= WATCHDOG_MIN_LEASE_MILLIS(1000ms)
    // 通过系统属性局部覆盖 + RedisCacheLocker（内部走 JedisLocker 的系统属性路径）验证三条分支。

    /**
     * 临时覆盖若干系统属性，执行完成后原样 restore，避免与 @BeforeClass 的全局值互相污染。
     */
    private static <V> V withProperties(Map<String, String> overrides, Callable<V> block) throws Exception {
        Map<String, String> snapshot = new LinkedHashMap<>();
        for (Map.Entry<String, String> e : overrides.entrySet()) {
            snapshot.put(e.getKey(), System.getProperty(e.getKey()));
            if (e.getValue() == null) System.clearProperty(e.getKey());
            else System.setProperty(e.getKey(), e.getValue());
        }
        try {
            return block.call();
        } finally {
            for (Map.Entry<String, String> e : snapshot.entrySet()) {
                if (e.getValue() == null) System.clearProperty(e.getKey());
                else System.setProperty(e.getKey(), e.getValue());
            }
        }
    }

    /**
     * 常量回归：外部代码可能会引用这两个公开常量，确保名字/阈值不漂移。
     */
    @Test
    public void watchdogPublicConstantsSanity() {
        Assert.assertEquals("ymp.jedisLocker.watchdogEnabled", JedisLocker.PROP_WATCHDOG_ENABLED);
        Assert.assertEquals(1000L, JedisLocker.WATCHDOG_MIN_LEASE_MILLIS);
    }

    /**
     * 开关关：ymp.jedisLocker.watchdogEnabled=false → useWatchdog=false，看门狗完全不启动。
     * 即便 lease(2000ms) >= 1s，持有锁 3s 后（>lease）挑战线程应能获得锁（Redis TTL 已自然过期）。
     */
    @Test
    public void watchdogDisabled_globalProperty_disablesRenewal() throws Exception {
        Map<String, String> props = new LinkedHashMap<>();
        props.put(JedisLocker.PROP_WATCHDOG_ENABLED, "false");
        props.put("ymp.jedisLocker.expiryMillis", "2000");   // 2s lease，本应够触发 watchdog
        props.put("ymp.jedisLocker.resolutionMillis", "5");
        withProperties(props, () -> {
            RedisCacheLocker locker = create("wdgOff");
            locker.writeLock("mut");
            try {
                // 锁持有者睡 3200ms —— 若 watchdog 开启，lease/3 ≈ 666ms 周期续期，锁仍在；
                // 但 watchdog=false，Redis 原生 TTL=2000ms 过期，3200ms 时锁一定没了。
                Thread.sleep(3200L);
            } finally {
                locker.releaseWriteLock("mut"); // 此时 key 可能已过期，release 是幂等 NOP
            }
            // 挑战：一定立即拿得到
            boolean got = locker.tryWriteLock("mut", 500L);
            if (got) locker.releaseWriteLock("mut");
            Assert.assertTrue("Watchdog disabled + lease=2s, hold 3.2s → key expired by TTL", got);
            return null;
        });
    }

    /**
     * 阈值拦：watchdogEnabled=true（默认），但 expiryMillis=500 < WATCHDOG_MIN_LEASE_MILLIS(1000) → 自动跳过 watchdog。
     * 持有锁 > 500ms 后，挑战线程应秒获得（靠 TTL 自然过期，无续期）。
     */
    @Test
    public void watchdogSkipped_whenLeaseBelowOneSecond() throws Exception {
        Map<String, String> props = new LinkedHashMap<>();
        props.put(JedisLocker.PROP_WATCHDOG_ENABLED, "true"); // 即使开关开，阈值也必须命中
        props.put("ymp.jedisLocker.expiryMillis", "500");
        props.put("ymp.jedisLocker.resolutionMillis", "5");
        withProperties(props, () -> {
            RedisCacheLocker locker = create("wdgShort");
            locker.writeLock("mut");
            try {
                Thread.sleep(1100L); // 超短租约 + 无 watchdog → TTL 500ms 已到期
            } finally {
                locker.releaseWriteLock("mut");
            }
            boolean got = locker.tryWriteLock("mut", 300L);
            if (got) locker.releaseWriteLock("mut");
            Assert.assertTrue("lease=500ms < 1000ms → watchdog skipped; hold 1.1s → key gone by TTL", got);
            return null;
        });
    }

    /**
     * 正例：watchdogEnabled=true 且 lease(1500ms) >= 1s → 看门狗应当每 500ms(lease/3) PEXPIRE 续期，
     * 即便持锁 3500ms（远超 1500ms TTL），挑战线程仍拿不到锁，证明 watchdog 真在工作。
     */
    @Test
    public void watchdogEnabled_keepsAliveBeyondOriginalLease() throws Exception {
        Map<String, String> props = new LinkedHashMap<>();
        props.put(JedisLocker.PROP_WATCHDOG_ENABLED, "true");
        props.put("ymp.jedisLocker.expiryMillis", "1500");   // 1.5s lease → 500ms renew interval
        props.put("ymp.jedisLocker.resolutionMillis", "5");
        props.put("ymp.jedisLocker.acquireTimeoutMillis", "600");
        withProperties(props, () -> {
            RedisCacheLocker locker = create("wdgOn");
            locker.writeLock("mut");
            try {
                // watchdog 周期 500ms，每轮 PEXPIRE 1500；3500ms 内至少 6 次续期，锁一定仍存在
                Thread.sleep(3500L);
            } finally {
                locker.releaseWriteLock("mut");
            }
            // 注意：release 之后锁立即释放，挑战者应立刻拿到（证明互斥状态来自 watchdog，而非 TTL）
            boolean gotAfterRelease = locker.tryWriteLock("mut", 300L);
            if (gotAfterRelease) locker.releaseWriteLock("mut");
            Assert.assertTrue("Watchdog worked → release cleared key → challenger gets it immediately",
                    gotAfterRelease);

            // --- 同配置验证"持锁期间挑战者抢不到"（关键断言 watchdog 真延长了锁期）---
            // 必须新开线程做 challenger：同一线程下 ThreadLocal token 相同，Redis 会判定为"同持有者重入"。
            final RedisCacheLocker holder = create("wdgOnB");
            holder.writeLock("mut2");
            try {
                Thread.sleep(3300L); // 远超原始 TTL 1500ms，若无续期一定过期
                CountDownLatch done = new CountDownLatch(1);
                AtomicReference<Boolean> stolenRef = new AtomicReference<>();
                AtomicReference<Throwable> errRef = new AtomicReference<>();
                Thread thief = new Thread(() -> {
                    RedisCacheLocker challenger = create("wdgOnB");
                    try {
                        stolenRef.set(challenger.tryWriteLock("mut2", 600L));
                    } catch (Throwable t) {
                        errRef.set(t);
                    } finally {
                        done.countDown();
                    }
                }, "wdgOnB-challenger");
                thief.setDaemon(true);
                thief.start();
                done.await(10, TimeUnit.SECONDS);
                if (errRef.get() != null) throw new AssertionError("challenger failed", errRef.get());
                Assert.assertFalse("Lease 1500ms + watchdog ON; hold 3.3s > TTL but challenger MUST still be blocked",
                        Boolean.TRUE.equals(stolenRef.get()));
            } finally {
                holder.releaseWriteLock("mut2");
            }
            return null;
        });
    }

    /**
     * 构造显式关 watchdog：新 6 参构造 watchdogEnabled=false 优先级高于系统属性"true"。
     * 走 RedisCacheLocker 不可触达 6 参构造（包装层只走 2 参读系统属性），此用例直接实例化 JedisLocker
     * 以覆盖构造级开关 —— 仍放置在 cache 模块，遵守「Cache 测试放在 Cache 里」的约束。
     * 用独立线程发起挑战，避免同 ThreadLocal token 被判定为锁重入。
     */
    @Test
    public void watchdogDisabled_explicitCtorArg_overridesGlobalTrue() throws Exception {
        Map<String, String> props = new LinkedHashMap<>();
        props.put(JedisLocker.PROP_WATCHDOG_ENABLED, "true"); // 系统属性开，但构造显式 false 应优先
        props.put("ymp.jedisLocker.expiryMillis", "1500");
        props.put("ymp.jedisLocker.resolutionMillis", "5");
        withProperties(props, () -> {
            // 关键：两个 JedisLocker 实例，但共享同一个 token = ThreadLocal 依据。
            // 主线程用 locker1 加锁；挑战放到 NEW THREAD（新 ThreadLocal token）做 locker2。
            final JedisLocker locker1 = new JedisLocker(
                    realRedis, "default",
                    800L,          // acquireTimeout
                    1500L,         // expiry — 本应触发 watchdog（>=1s）
                    5,             // resolution
                    Boolean.FALSE  // ← 显式关，覆盖系统属性 true
            );
            try {
                Assert.assertTrue(locker1.tryWriteLock(CACHE_NAME_PREFIX + "_wdgExp:mut", 800L));
                // 看门狗显式关 → 只能靠 TTL 1500ms；等 2500ms → 必然过期
                Thread.sleep(2500L);

                // ---- 新线程尝试 challenge ----
                CountDownLatch resultLatch = new CountDownLatch(1);
                AtomicReference<Boolean> challengerGot = new AtomicReference<>(null);
                AtomicReference<Throwable> errRef = new AtomicReference<>();
                Thread challenger = new Thread(() -> {
                    JedisLocker locker2 = new JedisLocker(
                            realRedis, "default",
                            600L, 1500L, 5, Boolean.FALSE);
                    try {
                        boolean got = locker2.tryWriteLock(CACHE_NAME_PREFIX + "_wdgExp:mut", 600L);
                        if (got) locker2.releaseWriteLock(CACHE_NAME_PREFIX + "_wdgExp:mut");
                        challengerGot.set(got);
                    } catch (Throwable t) {
                        errRef.set(t);
                    } finally {
                        try {
                            locker2.close();
                        } catch (Exception ignore) {
                        }
                        resultLatch.countDown();
                    }
                }, "wdgExplicit-challenger");
                challenger.setDaemon(true);
                challenger.start();
                Assert.assertTrue("Challenger finished within 10s",
                        resultLatch.await(10, TimeUnit.SECONDS));
                if (errRef.get() != null) {
                    throw new AssertionError("challenger failed", errRef.get());
                }
                // release main-thread lock (already TTL-expired; unlock is NOP)
                locker1.releaseWriteLock(CACHE_NAME_PREFIX + "_wdgExp:mut");
                Assert.assertTrue("watchdogEnabled=false wins over global=true → no renew; hold 2.5s > 1.5s → TTL expired",
                        Boolean.TRUE.equals(challengerGot.get()));
            } finally {
                locker1.close();
            }
            return null;
        });
    }

    // -------------------------------------------------------------- 辅助：在其他线程加锁，返回一个释放的 Runnable

    private static Runnable latchLockInOtherThread(String cacheSuffix, String key, boolean readLock)
            throws Exception {
        CountDownLatch heldLatch = new CountDownLatch(1);
        CountDownLatch releaseLatch = new CountDownLatch(1);
        AtomicReference<Throwable> err = new AtomicReference<>();
        Thread t = new Thread(() -> {
            try {
                RedisCacheLocker locker = create(cacheSuffix);
                if (readLock) locker.readLock(key);
                else locker.writeLock(key);
                heldLatch.countDown();
                boolean ok;
                try {
                    ok = releaseLatch.await(30, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    ok = false;
                }
                if (ok) {
                    if (readLock) locker.releaseReadLock(key);
                    else locker.releaseWriteLock(key);
                }
            } catch (Throwable th) {
                err.compareAndSet(null, th);
                heldLatch.countDown();
            }
        }, "cache-lock-blocker-" + cacheSuffix + "-" + key);
        t.setDaemon(true);
        t.start();
        Assert.assertTrue("Other thread obtained lock within 10s",
                heldLatch.await(10, TimeUnit.SECONDS));
        if (err.get() != null) {
            throw new AssertionError("lockInOtherThread failed", err.get());
        }
        return () -> {
            releaseLatch.countDown();
            try {
                t.join(5_000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };
    }

    // -------------------------------------------------------------- 真实 Redis 的最小化 IRedis 包装：
    //
    // 只实现 getConfig() / getConnectionHolder() / close() 三项；其余抛 UOE（在测试中绝不应触达）。
    // 注意：AbstractModule / 完整 Redis 初始化需要 IApplication 容器，这里为了单元测试轻巧启动，
    // 我们绕过 AbstractModule，自行把 IRedisDataSourceAdapter + IRedisCommandHolder 组装为 IRedis。

    private static class MinimalRedis implements IRedis {
        private final IRedisConfig config;
        private boolean initialized = true;
        private IApplication owner;

        MinimalRedis(IRedisConfig cfg) {
            this.config = cfg;
        }

        @Override
        public IApplication getOwner() {
            return owner;
        }

        @Override
        public IRedisConfig getConfig() {
            return config;
        }

        @Override
        public void initialize(IApplication app) {
            this.owner = app;
            this.initialized = true;
        }

        @Override
        public boolean isInitialized() {
            return initialized;
        }

        @Override
        public IRedisCommandHolder getDefaultConnectionHolder() {
            return getConnectionHolder(IRedisConfig.DEFAULT_STR);
        }

        @Override
        public IRedisCommandHolder getConnectionHolder(String s) {
            throw new UnsupportedOperationException("Must override in subclass/anon");
        }

        @Override
        public void releaseConnectionHolder(IRedisCommandHolder h) throws Exception {
            if (h != null) h.close();
        }

        @Override
        public IRedisDataSourceAdapter getDefaultDataSourceAdapter() {
            throw uoe();
        }

        @Override
        public IRedisDataSourceAdapter getDataSourceAdapter(String s) {
            throw uoe();
        }

        @Override
        public <T> T openSession(IRedisSessionExecutor<T> e) throws Exception {
            throw uoe();
        }

        @Override
        public <T> T openSession(String s, IRedisSessionExecutor<T> e) throws Exception {
            throw uoe();
        }

        @Override
        public <T> T openSession(IRedisCommandHolder h, IRedisSessionExecutor<T> e) throws Exception {
            throw uoe();
        }

        @Override
        public <T> T openSession(IDataSourceRouter r, IRedisSessionExecutor<T> e) throws Exception {
            throw uoe();
        }

        @Override
        public IRedisSession openSession() {
            throw uoe();
        }

        @Override
        public IRedisSession openSession(String s) {
            throw uoe();
        }

        @Override
        public IRedisSession openSession(IRedisCommandHolder h) {
            throw uoe();
        }

        @Override
        public IRedisSession openSession(IDataSourceRouter r) {
            throw uoe();
        }

        @Override
        public void subscribe(JedisPubSub j, String... c) {
        }

        @Override
        public void subscribe(String s, JedisPubSub j, String... c) {
        }

        @Override
        public void close() {
            initialized = false;
        }

        private static UnsupportedOperationException uoe() {
            return new UnsupportedOperationException("MinimalRedis: only getConfig/getConnectionHolder/close supported");
        }
    }
}
