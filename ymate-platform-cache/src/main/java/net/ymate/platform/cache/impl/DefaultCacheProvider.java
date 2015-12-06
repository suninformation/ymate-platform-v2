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

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;
import net.ymate.platform.cache.*;
import net.ymate.platform.core.util.RuntimeUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 刘镇 (suninformation@163.com) on 14/10/17
 * @version 1.0
 */
public class DefaultCacheProvider implements ICacheProvider {

    private CacheManager __cacheManager;

    private Map<String, ICache> __caches;

    private static final Object __LOCKER = new Object();

    protected ICaches __owner;

    public String getName() {
        return "default";
    }

    private String __safedCacheName(String name) {
        if ("default".equalsIgnoreCase(name)) {
            name = CacheManager.DEFAULT_NAME;
        }
        return name;
    }

    public void init(ICaches owner) throws CacheException {
        __owner = owner;
        __cacheManager = CacheManager.create();
        __caches = new ConcurrentHashMap<String, ICache>();
    }

    public ICache createCache(String name, final ICacheExpiredListener listener) throws CacheException {
        name = __safedCacheName(name);
        //
        ICache _cache = __caches.get(name);
        if (_cache == null) {
            synchronized (__LOCKER) {
                Ehcache __cache = __cacheManager.getEhcache(name);
                //
                if (__cache == null) {
                    __cacheManager.addCache(name);
                    __cache = __cacheManager.getCache(name);
                    if (listener != null) {
                        __cache.getCacheEventNotificationService().registerListener(new CacheEventListener() {

                            private ICacheExpiredListener __listener = listener;

                            public void notifyElementRemoved(Ehcache ehcache, Element element) throws net.sf.ehcache.CacheException {
                            }

                            public void notifyElementPut(Ehcache ehcache, Element element) throws net.sf.ehcache.CacheException {
                            }

                            public void notifyElementUpdated(Ehcache ehcache, Element element) throws net.sf.ehcache.CacheException {
                            }

                            public void notifyElementExpired(Ehcache ehcache, Element element) {
                                if (__listener != null) {
                                    __listener.notifyElementExpired(ehcache.getName(), element.getObjectKey());
                                }
                            }

                            public void notifyElementEvicted(Ehcache ehcache, Element element) {
                            }

                            public void notifyRemoveAll(Ehcache ehcache) {
                            }

                            public void dispose() {
                            }

                            public Object clone() throws CloneNotSupportedException {
                                throw new CloneNotSupportedException();
                            }
                        });
                    }
                }
                final Ehcache __ehcache = __cache;
                //
                _cache = new ICache() {

                    public Object get(Object key) throws CacheException {
                        if (key != null) {
                            try {
                                Element _element = __ehcache.get(key);
                                if (_element != null) {
                                    return _element.getObjectValue();
                                }
                            } catch (net.sf.ehcache.CacheException e) {
                                throw new CacheException(RuntimeUtils.unwrapThrow(e));
                            }
                        }
                        return null;
                    }

                    public void put(Object key, Object value) throws CacheException {
                        try {
                            __ehcache.put(new Element(key, value));
                        } catch (Exception e) {
                            throw new CacheException(RuntimeUtils.unwrapThrow(e));
                        }
                    }

                    public void update(Object key, Object value) throws CacheException {
                        put(key, value);
                    }

                    public List keys() throws CacheException {
                        return __ehcache.getKeys();
                    }

                    public void remove(Object key) throws CacheException {
                        try {
                            __ehcache.remove(key);
                        } catch (Exception e) {
                            throw new CacheException(RuntimeUtils.unwrapThrow(e));
                        }
                    }

                    public void removeAll(Collection<?> keys) throws CacheException {
                        __ehcache.removeAll(keys);
                    }

                    public void clear() throws CacheException {
                        __ehcache.removeAll();
                    }

                    public void destroy() throws CacheException {
                        try {
                            __cacheManager.removeCache(__ehcache.getName());
                            __caches.remove(__ehcache.getName());
                        } catch (Exception e) {
                            throw new CacheException(RuntimeUtils.unwrapThrow(e));
                        }
                    }
                };
                __caches.put(name, _cache);
            }
        }
        return _cache;
    }

    public ICache getCache(String name) {
        return __caches.get(__safedCacheName(name));
    }

    public void destroy() throws CacheException {
        for (ICache _cache : __caches.values()) {
            _cache.destroy();
        }
        __caches.clear();
        __caches = null;
        //
        __cacheManager.shutdown();
        __cacheManager = null;
    }
}
