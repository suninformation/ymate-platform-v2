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
package net.ymate.platform.cache.support;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;
import net.ymate.platform.cache.CacheException;
import net.ymate.platform.cache.ICache;
import net.ymate.platform.cache.ICacheEventListener;
import net.ymate.platform.cache.ICaches;
import net.ymate.platform.core.util.RuntimeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author 刘镇 (suninformation@163.com) on 15/12/7 上午12:16
 * @version 1.0
 */
public class EhCacheWraper implements ICache {

    private Ehcache __ehcache;

    public EhCacheWraper(ICaches owner, Ehcache ehcache, final ICacheEventListener listener) {
        __ehcache = ehcache;
        if (listener != null) {
            __ehcache.getCacheEventNotificationService().registerListener(new CacheEventListener() {

                private ICacheEventListener __listener = listener;

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

    @SuppressWarnings("unchecked")
    public List keys() throws CacheException {
        return new ArrayList(__ehcache.getKeys());
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
            __ehcache.getCacheManager().removeCache(__ehcache.getName());
        } catch (Exception e) {
            throw new CacheException(RuntimeUtils.unwrapThrow(e));
        }
    }
}
