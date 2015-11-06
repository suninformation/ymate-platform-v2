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
package net.ymate.platform.cache;

import net.ymate.platform.cache.impl.DefaultModuleCfg;
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

    public static final Version VERSION = new Version(2, 0, 0, Caches.class.getPackage().getImplementationVersion(), Version.VersionType.Alphal);

    private final Log _LOG = LogFactory.getLog(Caches.class);

    private static ICaches __instance;

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

    public String getName() {
        return ICaches.MODULE_NAME;
    }

    public void init(YMP owner) throws Exception {
        if (!__inited) {
            //
            _LOG.info("Initializing ymate-platform-cache-" + VERSION);
            //
            __owner = owner;
            __moduleCfg = new DefaultModuleCfg(owner);
            __owner.getEvents().registerEvent(CacheEvent.class);
            __cacheProvider = __moduleCfg.getCacheProvider();
            __cacheProvider.init(this);
            //
            __inited = true;
        }
    }

    public boolean isInited() {
        return __inited;
    }

    public YMP getOwner() {
        return __owner;
    }

    public ICacheModuleCfg getModuleCfg() {
        return __moduleCfg;
    }

    public Object get(String cacheName, Object key) throws CacheException {
        ICache _cache = __cacheProvider.getCache(cacheName);
        if (_cache != null) {
            return _cache.get(key);
        }
        return null;
    }

    public Map<Object, Object> getAll(String cacheName) throws CacheException {
        Map<Object, Object> _returnValue = new HashMap<Object, Object>();
        for (Object key : this.keys(cacheName)) {
            _returnValue.put(key, this.get(cacheName, key));
        }
        return _returnValue;
    }

    public void put(String cacheName, Object key, Object value) throws CacheException {
        ICache _cache = __cacheProvider.getCache(cacheName);
        if (_cache == null) {
            _cache = __cacheProvider.createCache(cacheName);
        }
        _cache.put(key, value);
    }

    public void update(String cacheName, Object key, Object value) throws CacheException {
        this.put(cacheName, key, value);
    }

    public List<?> keys(String cacheName) throws CacheException {
        ICache _cache = __cacheProvider.getCache(cacheName);
        if (_cache != null) {
            return _cache.keys();
        }
        return Collections.emptyList();
    }

    public void remove(String cacheName, Object key) throws CacheException {
        ICache _cache = __cacheProvider.getCache(cacheName);
        if (_cache != null) {
            _cache.remove(key);
        }
    }

    public void removeAll(String cacheName, List keys) throws CacheException {
        ICache _cache = __cacheProvider.getCache(cacheName);
        if (_cache != null) {
            _cache.removeAll(keys);
        }
    }

    public void clear(String cacheName) throws CacheException {
        ICache _cache = __cacheProvider.getCache(cacheName);
        if (_cache != null) {
            _cache.clear();
        }
    }

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
}
