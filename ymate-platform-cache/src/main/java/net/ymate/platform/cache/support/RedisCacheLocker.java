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
import net.ymate.platform.cache.ICaches;
import net.ymate.platform.persistence.redis.IRedis;
import net.ymate.platform.persistence.redis.support.JedisLocker;

import java.io.Closeable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.ymate.platform.cache.support.RedisCacheWrapper.REDIS_DATA_SOURCE_NAME;

/**
 * 基于 {@link JedisLocker} 封装的 Redis 分布式读写缓存锁实现。
 * <p>
 * 设计要点：
 * <ul>
 *     <li>线程级 token + per-key 重入计数 + Lua 原子操作 + 可配置看门狗自动续期，保证读写互斥的并发正确性；</li>
 *     <li>以缓存域（cacheName）作为 key 前缀隔离不同命名空间；不同 key 之间无实例级串行瓶颈；</li>
 *     <li>使用 {@link AtomicBoolean} + volatile 字段完成 DCL 安全发布与生命周期管理，支持多线程并发初始化；</li>
 *     <li>实现 {@link Closeable}，关闭时停止接受新锁请求并联动关闭底层 locker，保证资源释放幂等；</li>
 *     <li>对外按 {@code ICacheLocker} 合约暴露接口，底层异常统一翻译为 {@link CacheException}，空 key 直接抛 {@link IllegalArgumentException}。</li>
 * </ul>
 *
 * @author 刘镇 (suninformation@163.com) on 2025/1/11 00:25
 * @since 2.1.4
 */
public class RedisCacheLocker implements IRedisCacheLocker, Closeable {

    /**
     * 所属缓存模块（保留引用，方便未来扩展读取配置/事件）。volatile 保证 DCL 并发 initialize 时跨线程可见发布。
     */
    private volatile ICaches owner;

    /**
     * 缓存域名称。volatile 确保 DCL 并发初始化下：其他线程看到 initialized=true 时能同时读到正确 cacheName。
     */
    private volatile String cacheName;

    /**
     * 底层 JedisLocker。volatile 确保 DCL 并发初始化安全发布。
     */
    private volatile JedisLocker jedisLocker;

    /**
     * 初始化状态：AtomicBoolean 替代 boolean + DCL，安全发布内部依赖。
     */
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    /**
     * 关闭状态：关闭后拒绝新锁请求。
     */
    private final AtomicBoolean closed = new AtomicBoolean(false);

    @Override
    public void initialize(ICaches owner, IRedis redis, String cacheName) throws Exception {
        if (initialized.compareAndSet(false, true)) {
            this.owner = owner;
            this.cacheName = Objects.requireNonNull(cacheName, "cacheName");
            this.jedisLocker = new JedisLocker(Objects.requireNonNull(redis, "redis"), REDIS_DATA_SOURCE_NAME);
        }
    }

    @Override
    public boolean isInitialized() {
        return initialized.get();
    }

    /**
     * @return 当前缓存锁绑定到的所属缓存域（cacheName），便于诊断。
     */
    public String getCacheName() {
        return cacheName;
    }

    /**
     * 将用户传参的业务 key 加上缓存域前缀，组成完整锁 key。
     * 这里不使用 String.format，直接字符串拼接，避免 format 解析开销，也避免 null 走到 format 的 "null" 字面量。
     */
    private String doBuildKey(Object key) {
        return cacheName.concat(":").concat(key.toString());
    }

    private void ensureOpen() {
        if (closed.get()) {
            throw new CacheException("RedisCacheLocker for cache '" + cacheName + "' has already been closed.");
        }
        if (!initialized.get()) {
            throw new CacheException("RedisCacheLocker for cache '" + cacheName + "' is not initialized yet.");
        }
    }

    private static void requireNonNullKey(Object key) {
        if (key == null) {
            throw new IllegalArgumentException("Cache lock key must not be null.");
        }
    }

    // ---------------------------------------------------------------- ICacheLocker

    @Override
    public void readLock(Object key) {
        requireNonNullKey(key);
        ensureOpen();
        String lockKey = doBuildKey(key);
        try {
            jedisLocker.readLock(lockKey);
        } catch (IllegalStateException e) {
            // 超时未取到锁：ISE message 里已经携带 lockKey + 时长
            throw new CacheException(e.getMessage(), e);
        } catch (Exception e) {
            throw new CacheException("Failed to acquire read lock for '" + lockKey + "': " + e.getMessage(), e);
        }
    }

    @Override
    public void writeLock(Object key) {
        requireNonNullKey(key);
        ensureOpen();
        String lockKey = doBuildKey(key);
        try {
            jedisLocker.writeLock(lockKey);
        } catch (IllegalStateException e) {
            throw new CacheException(e.getMessage(), e);
        } catch (Exception e) {
            throw new CacheException("Failed to acquire write lock for '" + lockKey + "': " + e.getMessage(), e);
        }
    }

    @Override
    public boolean tryReadLock(Object key, long timeout) throws CacheException {
        requireNonNullKey(key);
        ensureOpen();
        try {
            return jedisLocker.tryReadLock(doBuildKey(key), timeout);
        } catch (CacheException e) {
            throw e;
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }

    @Override
    public boolean tryWriteLock(Object key, long timeout) throws CacheException {
        requireNonNullKey(key);
        ensureOpen();
        try {
            return jedisLocker.tryWriteLock(doBuildKey(key), timeout);
        } catch (CacheException e) {
            throw e;
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }

    @Override
    public void releaseReadLock(Object key) {
        requireNonNullKey(key);
        ensureOpen();
        try {
            jedisLocker.releaseReadLock(doBuildKey(key));
        } catch (CacheException e) {
            throw e;
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }

    @Override
    public void releaseWriteLock(Object key) {
        requireNonNullKey(key);
        ensureOpen();
        try {
            jedisLocker.releaseWriteLock(doBuildKey(key));
        } catch (CacheException e) {
            throw e;
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }

    // ---------------------------------------------------------------- Closeable

    /**
     * 关闭此缓存锁：标记 closed 为 true，并联动关闭底层 {@link JedisLocker}（停止接受新的锁请求、清理 key monitor）。
     * 多次调用安全。<b>不会去 Redis 上删除还被持有的锁 key</b>——它们要么在 finally 里被持有者线程释放，
     * 要么靠看门狗发现 key 失效后 self-cancel，最坏情况等 leaseMillis 到期。
     */
    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            if (jedisLocker != null) {
                try {
                    jedisLocker.close();
                } catch (Exception ignored) {
                    // ignore: close 只做兜底，不抛异常干扰上层 wrapper.close
                }
            }
        }
    }
}
