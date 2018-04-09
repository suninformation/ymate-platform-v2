/*
 * Copyright 2007-2017 the original author or authors.
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
import net.ymate.platform.core.util.RuntimeUtils;
import net.ymate.platform.persistence.redis.IRedis;
import net.ymate.platform.persistence.redis.IRedisCommandsHolder;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import redis.clients.jedis.JedisPubSub;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author 刘镇 (suninformation@163.com) on 15/12/7 上午12:16
 * @version 1.0
 */
public class RedisCacheWrapper extends JedisPubSub implements ICache {

    private String __cacheName;

    private IRedis __redis;

    private ICaches __owner;

    private ICacheEventListener __listener;

    public RedisCacheWrapper(ICaches owner, IRedis redis, String cacheName, final ICacheEventListener listener) {
        __owner = owner;
        __redis = redis;
        __cacheName = cacheName;
        __listener = listener;
        //
        if (__listener != null) {
            __redis.subscribe(this, "__keyevent@" + __redis.getModuleCfg().getDefaultDataSourceCfg().getMasterServerMeta().getDatabase() + "__:expired");
        }
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
        return __owner.getModuleCfg().getSerializer().deserialize(_bytes, Object.class);
    }

    @Override
    public Object get(Object key) throws CacheException {
        IRedisCommandsHolder _holder = null;
        try {
            _holder = __redis.getDefaultCommandsHolder();
            return __doUnserializeValue(_holder.getCommands().hget(__cacheName, __doSerializeKey(key)));
        } catch (Exception e) {
            throw new CacheException(RuntimeUtils.unwrapThrow(e));
        } finally {
            if (_holder != null) {
                _holder.release();
            }
        }
    }

    private void __doPut(Object key, Object value, boolean update) throws CacheException {
        IRedisCommandsHolder _holder = null;
        try {
            String _cacheKey = __doSerializeKey(key);
            String _cacheValue = __doSerializeValue(value);
            //
            _holder = __redis.getDefaultCommandsHolder();
            _holder.getCommands().hset(__cacheName, _cacheKey, _cacheValue);
            //
            if (__listener != null) {
                if (update) {
                    __listener.notifyElementUpdated(__cacheName, _cacheKey, _cacheValue);
                } else {
                    __listener.notifyElementPut(__cacheName, _cacheKey, _cacheValue);
                }
            }
        } catch (Exception e) {
            throw new CacheException(RuntimeUtils.unwrapThrow(e));
        } finally {
            if (_holder != null) {
                _holder.release();
            }
        }
    }

    @Override
    public void put(Object key, Object value) throws CacheException {
        __doPut(key, value, false);
    }

    @Override
    public void update(Object key, Object value) throws CacheException {
        __doPut(key, value, true);
    }

    @Override
    public List<String> keys() throws CacheException {
        IRedisCommandsHolder _holder = null;
        try {
            _holder = __redis.getDefaultCommandsHolder();
            return new ArrayList<String>(_holder.getCommands().hkeys(__cacheName));
        } catch (Exception e) {
            throw new CacheException(RuntimeUtils.unwrapThrow(e));
        } finally {
            if (_holder != null) {
                _holder.release();
            }
        }
    }

    @Override
    public void remove(Object key) throws CacheException {
        IRedisCommandsHolder _holder = null;
        try {
            String _cacheKey = __doSerializeKey(key);
            //
            _holder = __redis.getDefaultCommandsHolder();
            _holder.getCommands().hdel(__cacheName, _cacheKey);
            //
            if (__listener != null) {
                __listener.notifyElementRemoved(__cacheName, _cacheKey);
            }
        } catch (Exception e) {
            throw new CacheException(RuntimeUtils.unwrapThrow(e));
        } finally {
            if (_holder != null) {
                _holder.release();
            }
        }
    }

    @Override
    public void removeAll(Collection<?> keys) throws CacheException {
        IRedisCommandsHolder _holder = null;
        try {
            _holder = __redis.getDefaultCommandsHolder();
            List<String> _keys = new ArrayList<String>(keys.size());
            for (Object _key : keys) {
                _keys.add(__doSerializeKey(_key));
            }
            _holder.getCommands().hdel(__cacheName, _keys.toArray(new String[_keys.size()]));
            //
            if (__listener != null) {
                for (String _k : _keys) {
                    __listener.notifyElementRemoved(__cacheName, _k);
                }
            }
        } catch (Exception e) {
            throw new CacheException(RuntimeUtils.unwrapThrow(e));
        } finally {
            if (_holder != null) {
                _holder.release();
            }
        }
    }

    @Override
    public void clear() throws CacheException {
        IRedisCommandsHolder _holder = null;
        try {
            _holder = __redis.getDefaultCommandsHolder();
            _holder.getCommands().del(__cacheName);
            //
            if (__listener != null) {
                __listener.notifyRemoveAll(__cacheName);
            }
        } catch (Exception e) {
            throw new CacheException(RuntimeUtils.unwrapThrow(e));
        } finally {
            if (_holder != null) {
                _holder.release();
            }
        }
    }

    @Override
    public void destroy() throws CacheException {
        __redis = null;
    }

    @Override
    public ICacheLocker acquireCacheLocker() {
        return null;
    }

    @Override
    public void onMessage(String channel, String message) {
        if (StringUtils.isNotBlank(message)) {
            __listener.notifyElementExpired(__cacheName, message);
        }
    }
}
