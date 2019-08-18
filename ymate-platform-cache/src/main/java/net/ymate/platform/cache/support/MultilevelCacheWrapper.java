/*
 * Copyright 2007-2019 the original author or authors.
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

import net.sf.ehcache.Ehcache;
import net.ymate.platform.cache.*;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.persistence.redis.IRedis;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.List;

/**
 * @author 刘镇 (suninformation@163.com) on 15/12/7 上午2:23
 */
public class MultilevelCacheWrapper implements ICache, ICacheLocker {

    private static final Log LOG = LogFactory.getLog(MultilevelCacheWrapper.class);

    private final ICache masterCache;

    private final ICache slaveCache;

    private final boolean slaveCacheAutoSync;

    public MultilevelCacheWrapper(ICaches owner, String cacheName, Ehcache ehcache, IRedis redis, ICacheEventListener listener) {
        slaveCacheAutoSync = owner.getConfig().isMultilevelSlavesAutoSync();
        //
        masterCache = new EhCacheWrapper(owner, ehcache, listener);
        //
        ICacheEventListener slaveCacheListener = null;
        if (slaveCacheAutoSync) {
            slaveCacheListener = new ICacheEventListener() {

                @Override
                public ICaches getOwner() {
                    return owner;
                }

                @Override
                public void notifyElementRemoved(String cacheName, Object key) {
                }

                @Override
                public void notifyElementPut(String cacheName, Object key, Object value) {
                }

                @Override
                public void notifyElementUpdated(String cacheName, Object key, Object value) {
                }

                @Override
                public void notifyElementExpired(String cacheName, Object key) {
                    try {
                        // 从缓存元素过期时将与主缓存同步
                        masterCache.remove(key);
                    } catch (CacheException e) {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn(String.format("An exception occurred while synchronously removing the expired element [%s]", key), RuntimeUtils.unwrapThrow(e));
                        }
                    }
                }

                @Override
                public void notifyElementEvicted(String cacheName, Object key) {
                }

                @Override
                public void notifyRemoveAll(String cacheName) {
                }

                @Override
                public void close() {
                }

                @Override
                public void initialize(ICaches owner) {
                }

                @Override
                public boolean isInitialized() {
                    return true;
                }
            };
        }
        slaveCache = new RedisCacheWrapper(owner, redis, cacheName, slaveCacheListener);
    }

    @Override
    public Object get(Object key) throws CacheException {
        MultilevelKey multilevelKey = MultilevelKey.bind(key);
        if (multilevelKey.isMaster()) {
            Object value = masterCache.get(multilevelKey.getKey());
            if (slaveCacheAutoSync) {
                if (value == null) {
                    value = slaveCache.get(multilevelKey.getKey());
                    if (value != null) {
                        masterCache.put(multilevelKey.getKey(), value);
                    }
                }
            }
            return value;
        }
        return slaveCache.get(multilevelKey.getKey());
    }

    @Override
    public void put(Object key, Object value) throws CacheException {
        MultilevelKey multilevelKey = MultilevelKey.bind(key);
        if (multilevelKey.isMaster()) {
            masterCache.put(multilevelKey.getKey(), value);
            if (slaveCacheAutoSync) {
                slaveCache.put(multilevelKey.getKey(), value);
            }
        } else {
            slaveCache.put(multilevelKey.getKey(), value);
        }
    }

    @Override
    public void update(Object key, Object value) throws CacheException {
        MultilevelKey multilevelKey = MultilevelKey.bind(key);
        if (multilevelKey.isMaster()) {
            masterCache.update(multilevelKey.getKey(), value);
            if (slaveCacheAutoSync) {
                slaveCache.update(multilevelKey.getKey(), value);
            }
        } else {
            slaveCache.update(multilevelKey.getKey(), value);
        }
    }

    @Override
    public List<?> keys() throws CacheException {
        return keys(true);
    }

    public List<?> keys(boolean master) throws CacheException {
        if (master) {
            return masterCache.keys();
        }
        return slaveCache.keys();
    }

    @Override
    public void remove(Object key) throws CacheException {
        MultilevelKey multilevelKey = MultilevelKey.bind(key);
        if (multilevelKey.isMaster()) {
            masterCache.remove(multilevelKey.getKey());
            if (slaveCacheAutoSync) {
                slaveCache.remove(multilevelKey.getKey());
            }
        } else {
            slaveCache.remove(multilevelKey.getKey());
        }
    }

    @Override
    public void removeAll(Collection<?> keys) throws CacheException {
        removeAll(true, keys);
    }

    public void removeAll(boolean master, Collection<?> keys) throws CacheException {
        if (master) {
            masterCache.removeAll(keys);
            if (slaveCacheAutoSync) {
                slaveCache.removeAll(keys);
            }
        } else {
            slaveCache.removeAll(keys);
        }
    }

    @Override
    public void clear() throws CacheException {
        masterCache.clear();
    }

    public void clear(boolean master) throws CacheException {
        if (master) {
            masterCache.clear();
            if (slaveCacheAutoSync) {
                slaveCache.clear();
            }
        } else {
            slaveCache.clear();
        }
    }

    @Override
    public void close() throws Exception {
        slaveCache.close();
        masterCache.close();
    }

    @Override
    public ICacheLocker acquireCacheLocker() {
        return this;
    }

    @Override
    public void readLock(Object key) {
        MultilevelKey multilevelKey = MultilevelKey.bind(key);
        masterCache.acquireCacheLocker().readLock(multilevelKey.getKey());
    }

    @Override
    public void writeLock(Object key) {
        MultilevelKey multilevelKey = MultilevelKey.bind(key);
        masterCache.acquireCacheLocker().writeLock(multilevelKey.getKey());
    }

    @Override
    public boolean tryReadLock(Object key, long timeout) throws CacheException {
        MultilevelKey multilevelKey = MultilevelKey.bind(key);
        return masterCache.acquireCacheLocker().tryReadLock(multilevelKey.getKey(), timeout);
    }

    @Override
    public boolean tryWriteLock(Object key, long timeout) throws CacheException {
        MultilevelKey multilevelKey = MultilevelKey.bind(key);
        return masterCache.acquireCacheLocker().tryWriteLock(multilevelKey.getKey(), timeout);
    }

    @Override
    public void releaseReadLock(Object key) {
        MultilevelKey multilevelKey = MultilevelKey.bind(key);
        masterCache.acquireCacheLocker().releaseReadLock(multilevelKey.getKey());
    }

    @Override
    public void releaseWriteLock(Object key) {
        MultilevelKey multilevelKey = MultilevelKey.bind(key);
        masterCache.acquireCacheLocker().releaseWriteLock(multilevelKey.getKey());
    }
}
