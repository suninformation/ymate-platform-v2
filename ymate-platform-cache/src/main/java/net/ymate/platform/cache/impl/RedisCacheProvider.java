/*
 * Copyright 2007-2016 the original author or authors.
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
package net.ymate.platform.cache.impl;

import net.ymate.platform.cache.*;
import net.ymate.platform.core.util.RuntimeUtils;
import net.ymate.platform.persistence.redis.IRedis;
import net.ymate.platform.persistence.redis.IRedisCommandsHolder;
import net.ymate.platform.persistence.redis.Redis;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 刘镇 (suninformation@163.com) on 15/12/6 上午4:20
 * @version 1.0
 */
public class RedisCacheProvider implements ICacheProvider {

    private Map<String, ICache> __caches;

    private IRedis __redis;

    private static final Object __LOCKER = new Object();

    protected ICaches __owner;

    public String getName() {
        return "redis";
    }

    public void init(ICaches owner) throws CacheException {
        __owner = owner;
        __caches = new ConcurrentHashMap<String, ICache>();
        __redis = Redis.get(__owner.getOwner());
    }

    protected String __doSerializeKey(Object key) throws Exception {
        if (key instanceof String || key instanceof StringBuilder || key instanceof StringBuffer || key instanceof Number) {
            return key.toString();
        }
        return DigestUtils.md5Hex(("" + key).getBytes());
    }

    protected String __doSerializeValue(Object value) throws Exception {
        return Base64.encodeBase64String(__owner.getModuleCfg().getSerializer().serialize(value));
    }

    protected Object __doUnserializeValue(String value) throws Exception {
        if (value == null) {
            return null;
        }
        byte[] _bytes = Base64.decodeBase64(value);
        return __owner.getModuleCfg().getSerializer().deserialize(_bytes);
    }

    public ICache createCache(final String name, final ICacheExpiredListener listener) throws CacheException {
        ICache _cache = __caches.get(name);
        if (_cache == null) {
            synchronized (__LOCKER) {
                _cache = new ICache() {

                    private String __cacheName = name;

                    public Object get(Object key) throws CacheException {
                        IRedisCommandsHolder _holder = null;
                        try {
                            _holder = __redis.getDefaultDataSourceAdapter().getCommandsHolder();
                            return __doUnserializeValue(_holder.getCommands().hget(__cacheName, __doSerializeKey(key)));
                        } catch (Exception e) {
                            throw new CacheException(RuntimeUtils.unwrapThrow(e));
                        } finally {
                            if (_holder != null) {
                                _holder.release();
                            }
                        }
                    }

                    public void put(Object key, Object value) throws CacheException {
                        IRedisCommandsHolder _holder = null;
                        try {
                            _holder = __redis.getDefaultDataSourceAdapter().getCommandsHolder();
                            _holder.getCommands().hset(__cacheName, __doSerializeKey(key), __doSerializeValue(value));
                        } catch (Exception e) {
                            throw new CacheException(RuntimeUtils.unwrapThrow(e));
                        } finally {
                            if (_holder != null) {
                                _holder.release();
                            }
                        }
                    }

                    public void update(Object key, Object value) throws CacheException {
                        put(key, value);
                    }

                    public List<String> keys() throws CacheException {
                        IRedisCommandsHolder _holder = null;
                        try {
                            _holder = __redis.getDefaultDataSourceAdapter().getCommandsHolder();
                            return new ArrayList<String>(_holder.getCommands().hkeys(__cacheName));
                        } catch (Exception e) {
                            throw new CacheException(RuntimeUtils.unwrapThrow(e));
                        } finally {
                            if (_holder != null) {
                                _holder.release();
                            }
                        }
                    }

                    public void remove(Object key) throws CacheException {
                        IRedisCommandsHolder _holder = null;
                        try {
                            _holder = __redis.getDefaultDataSourceAdapter().getCommandsHolder();
                            _holder.getCommands().hdel(__cacheName, __doSerializeKey(key));
                        } catch (Exception e) {
                            throw new CacheException(RuntimeUtils.unwrapThrow(e));
                        } finally {
                            if (_holder != null) {
                                _holder.release();
                            }
                        }
                    }

                    public void removeAll(Collection<?> keys) throws CacheException {
                        IRedisCommandsHolder _holder = null;
                        try {
                            _holder = __redis.getDefaultDataSourceAdapter().getCommandsHolder();
                            List<String> _keys = new ArrayList<String>(keys.size());
                            for (Object _key : keys) {
                                _keys.add(__doSerializeKey(_key));
                            }
                            _holder.getCommands().hdel(__cacheName, _keys.toArray(new String[_keys.size()]));
                        } catch (Exception e) {
                            throw new CacheException(RuntimeUtils.unwrapThrow(e));
                        } finally {
                            if (_holder != null) {
                                _holder.release();
                            }
                        }
                    }

                    public void clear() throws CacheException {
                        IRedisCommandsHolder _holder = null;
                        try {
                            _holder = __redis.getDefaultDataSourceAdapter().getCommandsHolder();
                            _holder.getCommands().del(__cacheName);
                        } catch (Exception e) {
                            throw new CacheException(RuntimeUtils.unwrapThrow(e));
                        } finally {
                            if (_holder != null) {
                                _holder.release();
                            }
                        }
                    }

                    public void destroy() throws CacheException {
                        __redis = null;
                    }
                };
                _cache.clear();
                if (listener != null) {
                    IRedisCommandsHolder _holder = null;
                    try {
                        _holder = __redis.getDefaultDataSourceAdapter().getCommandsHolder();
                        Jedis _jedis = _holder.getJedis();
                        if (_jedis != null) {
                            _jedis.subscribe(new JedisPubSub() {
                                public void onMessage(String channel, String message) {
                                    if (StringUtils.isNotBlank(message)) {
                                        listener.notifyElementExpired(name, message);
                                    }
                                }
                            }, "__keyevent@" + _holder.getDataSourceCfgMeta().getMasterServerMeta().getDatabase() + "__:expired");
                        }
                    } catch (Exception e) {
                        throw new CacheException(RuntimeUtils.unwrapThrow(e));
                    } finally {
                        if (_holder != null) {
                            _holder.release();
                        }
                    }
                }
                __caches.put(name, _cache);
            }
        }
        return _cache;
    }

    public ICache getCache(String name) {
        return __caches.get(name);
    }

    public void destroy() throws CacheException {
        for (ICache _cache : __caches.values()) {
            _cache.destroy();
        }
        __caches.clear();
        __caches = null;
    }
}
