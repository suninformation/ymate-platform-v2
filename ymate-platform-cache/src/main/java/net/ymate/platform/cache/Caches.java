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
package net.ymate.platform.cache;

import net.ymate.platform.cache.impl.DefaultCacheProvider;
import net.ymate.platform.cache.impl.DefaultCacheModuleCfg;
import net.ymate.platform.cache.impl.MultilevelCacheProvider;
import net.ymate.platform.cache.impl.RedisCacheProvider;
import net.ymate.platform.core.Version;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.module.IModule;
import net.ymate.platform.core.module.annotation.Module;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 缓存管理器
 *
 * @author 刘镇 (suninformation@163.com) on 14-10-16
 * @version 1.0
 */
@Module
public class Caches implements IModule, ICaches {

    public static final Version VERSION = new Version(2, 0, 11, Caches.class.getPackage().getImplementationVersion(), Version.VersionType.Release);

    private static final Log _LOG = LogFactory.getLog(Caches.class);

    private static volatile ICaches __instance;

    private YMP __owner;

    private ICacheModuleCfg __moduleCfg;

    private ICacheProvider __cacheProvider;

    private boolean __inited;

    /**
     * @return 返回默认缓存模块管理器实例对象
     */
    public static ICaches get() {
        if (__instance == null) {
            synchronized (VERSION) {
                if (__instance == null) {
                    __instance = YMP.get().getModule(Caches.class);
                }
            }
        }
        return __instance;
    }

    /**
     * @param owner YMP框架管理器实例
     * @return 返回指定YMP框架管理器容器内的缓存模块实例
     */
    public static ICaches get(YMP owner) {
        return owner.getModule(Caches.class);
    }

    @Override
    public String getName() {
        return ICaches.MODULE_NAME;
    }

    @Override
    public void init(YMP owner) throws Exception {
        if (!__inited) {
            //
            _LOG.info("Initializing ymate-platform-cache-" + VERSION);
            //
            __owner = owner;
            __owner.getEvents().registerEvent(CacheEvent.class);
            __moduleCfg = new DefaultCacheModuleCfg(owner);
            __moduleCfg.getCacheEventListener().init(this);
            __cacheProvider = __moduleCfg.getCacheProvider();
            __cacheProvider.init(this);
            //
            __inited = true;
        }
    }

    @Override
    public boolean isInited() {
        return __inited;
    }

    @Override
    public YMP getOwner() {
        return __owner;
    }

    @Override
    public ICacheModuleCfg getModuleCfg() {
        return __moduleCfg;
    }

    @Override
    public ICacheProvider getCacheProvider() {
        return __cacheProvider;
    }

    @Override
    public Object get(String cacheName, Object key) {
        ICache _cache = __cacheProvider.getCache(cacheName);
        if (_cache != null) {
            return _cache.get(key);
        }
        return null;
    }

    @Override
    public Object get(Object key) {
        return get(__moduleCfg.getDefaultCacheName(), key);
    }

    @Override
    public Map<Object, Object> getAll(String cacheName) {
        Map<Object, Object> _returnValue = new HashMap<Object, Object>();
        for (Object key : this.keys(cacheName)) {
            _returnValue.put(key, this.get(cacheName, key));
        }
        return _returnValue;
    }

    @Override
    public Map<Object, Object> getAll() {
        return getAll(__moduleCfg.getDefaultCacheName());
    }

    private void __doPut(String cacheName, Object key, Object value) {
        ICache _cache = __cacheProvider.getCache(cacheName);
        if (_cache == null) {
            _cache = __cacheProvider.createCache(cacheName, __moduleCfg.getCacheEventListener());
        }
        _cache.put(key, value);
    }

    @Override
    public void put(String cacheName, Object key, Object value) {
        __doPut(cacheName, key, value);
    }

    @Override
    public void put(Object key, Object value) {
        put(__moduleCfg.getDefaultCacheName(), key, value);
    }

    @Override
    public void update(String cacheName, Object key, Object value) {
        __doPut(cacheName, key, value);
    }

    @Override
    public void update(Object key, Object value) {
        update(__moduleCfg.getDefaultCacheName(), key, value);
    }

    @Override
    public List<?> keys(String cacheName) {
        ICache _cache = __cacheProvider.getCache(cacheName);
        if (_cache != null) {
            return _cache.keys();
        }
        return Collections.emptyList();
    }

    @Override
    public List keys() {
        return keys(__moduleCfg.getDefaultCacheName());
    }

    @Override
    public void remove(String cacheName, Object key) {
        ICache _cache = __cacheProvider.getCache(cacheName);
        if (_cache != null) {
            _cache.remove(key);
        }
    }

    @Override
    public void remove(Object key) {
        remove(__moduleCfg.getDefaultCacheName(), key);
    }

    @Override
    public void removeAll(String cacheName, List keys) {
        ICache _cache = __cacheProvider.getCache(cacheName);
        if (_cache != null) {
            _cache.removeAll(keys);
        }
    }

    @Override
    public void removeAll(List keys) {
        removeAll(__moduleCfg.getDefaultCacheName(), keys);
    }

    @Override
    public void clear(String cacheName) {
        ICache _cache = __cacheProvider.getCache(cacheName);
        if (_cache != null) {
            _cache.clear();
        }
    }

    @Override
    public void clear() {
        clear(__moduleCfg.getDefaultCacheName());
    }

    @Override
    public void destroy() throws Exception {
        if (__inited) {
            __inited = false;
            //
            __cacheProvider.destroy();
            __cacheProvider = null;
            __moduleCfg = null;
            __owner = null;
        }
    }

    public static final Map<String, String> PROVIDERS;

    static {
        PROVIDERS = new HashMap<String, String>();
        PROVIDERS.put(ICache.DEFAULT, DefaultCacheProvider.class.getName());
        PROVIDERS.put(ICache.REDIS, RedisCacheProvider.class.getName());
        PROVIDERS.put(ICache.MULTILEVEL, MultilevelCacheProvider.class.getName());
    }
}
