/*
 * Copyright 2007-2018 the original author or authors.
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

import net.sf.ehcache.CacheManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/11/8 11:43 PM
 * @version 1.0
 */
public abstract class AbstractCacheProvider implements ICacheProvider {

    private static final Object __LOCKER = new Object();

    private ICaches __owner;

    private Map<String, ICache> __caches;

    @Override
    public void init(ICaches owner) {
        __owner = owner;
        __caches = new ConcurrentHashMap<String, ICache>();
    }

    private String __saferCacheName(String name) {
        if (ICache.DEFAULT.equalsIgnoreCase(name)) {
            name = CacheManager.DEFAULT_NAME;
        }
        return name;
    }

    private ICache __fromCache(String name) {
        return __caches.get(name);
    }

    private void __putCache(String name, ICache cache) {
        __caches.put(name, cache);
    }

    public ICaches getOwner() {
        return __owner;
    }

    @Override
    public ICache createCache(String name, final ICacheEventListener listener) {
        name = __saferCacheName(name);
        //
        ICache _cache = __fromCache(name);
        if (_cache == null) {
            synchronized (__LOCKER) {
                _cache = __createCache(name, listener);
                if (_cache != null) {
                    __putCache(name, _cache);
                }
            }
        }
        return _cache;
    }

    protected abstract ICache __createCache(String saferName, ICacheEventListener listener);

    @Override
    public ICache getCache(String name) {
        return getCache(name, true);
    }

    @Override
    public ICache getCache(String name, boolean create) {
        return getCache(name, create, getOwner().getModuleCfg().getCacheEventListener());
    }

    @Override
    public ICache getCache(String name, boolean create, ICacheEventListener listener) {
        ICache _cache = __fromCache(__saferCacheName(name));
        if (_cache == null && create) {
            _cache = createCache(name, listener);
        }
        return _cache;
    }

    @Override
    public void destroy() throws CacheException {
        for (ICache _cache : __caches.values()) {
            _cache.destroy();
        }
        __caches.clear();
        __caches = null;
    }
}
