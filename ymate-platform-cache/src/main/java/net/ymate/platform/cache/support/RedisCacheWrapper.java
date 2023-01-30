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

import net.ymate.platform.cache.*;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.persistence.redis.IRedis;
import net.ymate.platform.persistence.redis.IRedisCommandHolder;
import net.ymate.platform.persistence.redis.IRedisCommander;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.JedisPubSub;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author 刘镇 (suninformation@163.com) on 15/12/7 上午12:16
 */
public class RedisCacheWrapper implements ICache {

    private static final String SEPARATOR = ":";

    private static final int KEY_LENGTH = 2;

    private final String cacheName;

    private IRedis redis;

    private final ICaches owner;

    private final ICacheEventListener cacheEventListener;

    private final IRedisCacheLocker redisCacheLocker;

    public RedisCacheWrapper(ICaches owner, IRedis redis, String cacheName, final ICacheEventListener cacheEventListener) {
        this.owner = owner;
        this.redis = redis;
        this.cacheName = cacheName;
        this.cacheEventListener = cacheEventListener;
        // 通过 SPI 方式尝试加载基于 Redis 的锁实现
        redisCacheLocker = ClassUtils.loadClass(IRedisCacheLocker.class);
        if (redisCacheLocker != null && !redisCacheLocker.isInitialized()) {
            try {
                redisCacheLocker.initialize(owner, redis, cacheName);
            } catch (CacheException e) {
                throw e;
            } catch (Exception e) {
                throw new CacheException(e);
            }
        }
        //
        if (cacheEventListener != null && owner.getConfig().isEnabledSubscribeExpired()) {
            redis.subscribe(new JedisPubSub() {
                @Override
                public void onMessage(String channel, String message) {
                    if (StringUtils.isNotBlank(message)) {
                        String[] keyStr = StringUtils.split(message, SEPARATOR);
                        if (ArrayUtils.isNotEmpty(keyStr) && keyStr.length == KEY_LENGTH && StringUtils.equals(cacheName, keyStr[0])) {
                            if (owner.getConfig().isStorageWithSet()) {
                                remove(keyStr[1]);
                            }
                            cacheEventListener.notifyElementExpired(cacheName, keyStr[1]);
                        }
                    }
                }
            }, String.format("__keyevent@%d__:expired", redis.getConfig().getDefaultDataSourceConfig().getMasterServerMeta().getDatabase()));
        }
    }

    private String serializeKey(Object key) {
        if (key instanceof String || key instanceof StringBuilder || key instanceof StringBuffer || key instanceof Number) {
            return key.toString();
        }
        return DigestUtils.sha1Hex((StringUtils.EMPTY + key).getBytes());
    }

    private String serializeValue(Object value) throws Exception {
        return Base64.encodeBase64String(owner.getConfig().getSerializer().serialize(value));
    }

    private Object deserializeValue(String value) throws Exception {
        if (value == null) {
            return null;
        }
        byte[] bytes = Base64.decodeBase64(value);
        return owner.getConfig().getSerializer().deserialize(bytes, Object.class);
    }

    @Override
    public Object get(Object key) throws CacheException {
        try (IRedisCommandHolder holder = redis.getDefaultConnectionHolder()) {
            IRedisCommander commander = holder.getConnection();
            String cacheKey = serializeKey(key);
            Object cacheValue;
            if (owner.getConfig().isStorageWithSet()) {
                cacheValue = deserializeValue(commander.hget(cacheName, cacheKey));
                if (owner.getConfig().isEnabledSubscribeExpired() && cacheValue != null && Boolean.TRUE.equals(!commander.exists(cacheName.concat(SEPARATOR).concat(cacheKey)))) {
                    remove(key);
                }
            } else {
                cacheValue = deserializeValue(commander.get(cacheName.concat(SEPARATOR).concat(cacheKey)));
            }
            return cacheValue;
        } catch (CacheException e) {
            throw e;
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }

    private void put(Object key, Object value, int timeout, boolean update) throws CacheException {
        try (IRedisCommandHolder holder = redis.getDefaultConnectionHolder()) {
            IRedisCommander commander = holder.getConnection();
            String cacheKey = serializeKey(key);
            String cacheValue = serializeValue(value);
            if (value instanceof CacheElement) {
                timeout = ((CacheElement) value).getTimeout();
            }
            if (timeout <= 0) {
                timeout = owner.getConfig().getDefaultCacheTimeout();
            }
            //
            if (owner.getConfig().isStorageWithSet()) {
                commander.hset(cacheName, cacheKey, cacheValue);
                //
                if (owner.getConfig().isEnabledSubscribeExpired() && timeout > 0) {
                    commander.setex(cacheName.concat(SEPARATOR).concat(cacheKey), (long) timeout, StringUtils.EMPTY);
                }
            } else if (timeout > 0) {
                commander.setex(cacheName.concat(SEPARATOR).concat(cacheKey), (long) timeout, cacheValue);
            } else {
                commander.set(cacheName.concat(SEPARATOR).concat(cacheKey), cacheValue);
            }
            //
            if (cacheEventListener != null) {
                if (update) {
                    cacheEventListener.notifyElementUpdated(cacheName, cacheKey, cacheValue);
                } else {
                    cacheEventListener.notifyElementPut(cacheName, cacheKey, cacheValue);
                }
            }
        } catch (CacheException e) {
            throw e;
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }

    @Override
    public void put(Object key, Object value) throws CacheException {
        put(key, value, 0, false);
    }

    @Override
    public void put(Object key, Object value, int timeout) throws CacheException {
        put(key, value, timeout, false);
    }

    @Override
    public void update(Object key, Object value) throws CacheException {
        put(key, value, 0, true);
    }

    @Override
    public void update(Object key, Object value, int timeout) throws CacheException {
        put(key, value, timeout, true);
    }

    @Override
    public List<String> keys() throws CacheException {
        List<String> returnValue = new ArrayList<>();
        try (IRedisCommandHolder holder = redis.getDefaultConnectionHolder()) {
            IRedisCommander commander = holder.getConnection();
            if (owner.getConfig().isStorageWithSet()) {
                Set<String> keys = commander.hkeys(cacheName);
                if (keys != null && !keys.isEmpty()) {
                    returnValue.addAll(keys);
                }
            } else {
                String keyPrefix = cacheName.concat(SEPARATOR);
                commander.keys(keyPrefix.concat("*"))
                        .forEach(key -> returnValue.add(StringUtils.substringAfterLast(key, keyPrefix)));
            }
        } catch (CacheException e) {
            throw e;
        } catch (Exception e) {
            throw new CacheException(e);
        }
        return returnValue;
    }

    @Override
    public void remove(Object key) throws CacheException {
        try (IRedisCommandHolder holder = redis.getDefaultConnectionHolder()) {
            IRedisCommander commander = holder.getConnection();
            String cacheKey = serializeKey(key);
            if (owner.getConfig().isStorageWithSet()) {
                commander.hdel(cacheName, cacheKey);
                //
                if (owner.getConfig().isEnabledSubscribeExpired()) {
                    commander.del(cacheName.concat(SEPARATOR).concat(cacheKey));
                }
            } else {
                commander.del(cacheName.concat(SEPARATOR).concat(cacheKey));
            }
            //
            if (cacheEventListener != null) {
                cacheEventListener.notifyElementRemoved(cacheName, cacheKey);
            }
        } catch (CacheException e) {
            throw e;
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }

    @Override
    public void removeAll(Collection<?> keys) throws CacheException {
        try (IRedisCommandHolder holder = redis.getDefaultConnectionHolder()) {
            IRedisCommander commander = holder.getConnection();
            List<String> serializeKeys = new ArrayList<>(keys.size());
            keys.forEach(key -> serializeKeys.add(serializeKey(key)));
            if (owner.getConfig().isStorageWithSet()) {
                commander.hdel(cacheName, serializeKeys.toArray(new String[0]));
                if (owner.getConfig().isEnabledSubscribeExpired()) {
                    serializeKeys.forEach(key -> {
                        commander.del(cacheName.concat(SEPARATOR).concat(key));
                        if (cacheEventListener != null) {
                            cacheEventListener.notifyElementRemoved(cacheName, key);
                        }
                    });
                }
            } else {
                serializeKeys.forEach(key -> {
                    commander.del(cacheName.concat(SEPARATOR).concat(key));
                    if (cacheEventListener != null) {
                        cacheEventListener.notifyElementRemoved(cacheName, key);
                    }
                });
            }
        } catch (CacheException e) {
            throw e;
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }

    @Override
    public void clear() throws CacheException {
        try (IRedisCommandHolder holder = redis.getDefaultConnectionHolder()) {
            IRedisCommander commander = holder.getConnection();
            if (owner.getConfig().isStorageWithSet()) {
                if (owner.getConfig().isEnabledSubscribeExpired()) {
                    commander.hkeys(cacheName)
                            .forEach(key -> commander.del(cacheName.concat(SEPARATOR).concat(key)));
                }
                commander.del(cacheName);
            } else {
                Set<String> keys = commander.keys(cacheName.concat(SEPARATOR).concat("*"));
                keys.forEach(commander::del);
            }
            if (cacheEventListener != null) {
                cacheEventListener.notifyRemoveAll(cacheName);
            }
        } catch (CacheException e) {
            throw e;
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }

    @Override
    public void close() throws Exception {
        if (redisCacheLocker != null && redisCacheLocker.isInitialized()) {
            redisCacheLocker.close();
        }
        redis = null;
    }

    @Override
    public ICacheLocker acquireCacheLocker() {
        return redisCacheLocker;
    }
}
