/*
 * Copyright 2007-2025 the original author or authors.
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

import static net.ymate.platform.cache.support.RedisCacheWrapper.REDIS_DATA_SOURCE_NAME;

/**
 * 参考自：<a href="https://github.com/kaidul/jedis-lock/blob/master/src/main/java/com/github/jedis/lock/JedisLock.java">github.com/kaidul/jedis-lock</a>
 *
 * @author 刘镇 (suninformation@163.com) on 2025/1/11 00:25
 * @since 2.1.4
 */
public class RedisCacheLocker implements IRedisCacheLocker {

    private String cacheName;

    private JedisLocker jedisLocker;

    private boolean initialized;

    @Override
    public void initialize(ICaches owner, IRedis redis, String cacheName) throws Exception {
        if (!initialized) {
            this.cacheName = cacheName;
            this.jedisLocker = new JedisLocker(redis, REDIS_DATA_SOURCE_NAME);
            //
            this.initialized = true;
        }
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    private String doBuildKey(Object key) {
        return String.format("%s:%s", cacheName, key.toString());
    }

    @Override
    public synchronized void readLock(Object key) {
        try {
            jedisLocker.readLock(doBuildKey(key));
        } catch (Exception e) {
            throw new CacheException(String.format("Failed to create read lock for '%s' within %d milliseconds.", key, jedisLocker.getAcquireTimeoutMillis()));
        }
    }

    @Override
    public synchronized void writeLock(Object key) {
        try {
            jedisLocker.writeLock(doBuildKey(key));
        } catch (Exception e) {
            throw new CacheException(String.format("Failed to create write lock for '%s' within %d milliseconds.", key, jedisLocker.getAcquireTimeoutMillis()));
        }
    }

    @Override
    public synchronized boolean tryReadLock(Object key, long timeout) throws CacheException {
        try {
            return jedisLocker.tryReadLock(doBuildKey(key), timeout);
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }

    @Override
    public synchronized boolean tryWriteLock(Object key, long timeout) throws CacheException {
        try {
            return jedisLocker.tryWriteLock(doBuildKey(key), timeout);
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }

    @Override
    public synchronized void releaseReadLock(Object key) {
        try {
            jedisLocker.releaseReadLock(doBuildKey(key));
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }

    @Override
    public synchronized void releaseWriteLock(Object key) {
        try {
            jedisLocker.releaseWriteLock(doBuildKey(key));
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }
}
