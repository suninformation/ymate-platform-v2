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

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;
import net.ymate.platform.cache.*;
import net.ymate.platform.core.util.RuntimeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author 刘镇 (suninformation@163.com) on 15/12/7 上午12:16
 * @version 1.0
 */
public class EhCacheWrapper implements ICache, ICacheLocker {

    private final ICaches __owner;

    private final Ehcache __ehcache;

    public EhCacheWrapper(ICaches owner, Ehcache ehcache, final ICacheEventListener listener) {
        __owner = owner;
        __ehcache = ehcache;
        if (listener != null) {
            __ehcache.getCacheEventNotificationService().registerListener(new CacheEventListener() {

                @Override
                public void notifyElementRemoved(Ehcache ehcache, Element element) throws net.sf.ehcache.CacheException {
                    listener.notifyElementRemoved(ehcache.getName(), element.getObjectKey());
                }

                @Override
                public void notifyElementPut(Ehcache ehcache, Element element) throws net.sf.ehcache.CacheException {
                    listener.notifyElementPut(ehcache.getName(), element.getObjectKey(), element.getObjectValue());
                }

                @Override
                public void notifyElementUpdated(Ehcache ehcache, Element element) throws net.sf.ehcache.CacheException {
                    listener.notifyElementUpdated(ehcache.getName(), element.getObjectKey(), element.getObjectValue());
                }

                @Override
                public void notifyElementExpired(Ehcache ehcache, Element element) {
                    listener.notifyElementExpired(ehcache.getName(), element.getObjectKey());
                }

                @Override
                public void notifyElementEvicted(Ehcache ehcache, Element element) {
                    listener.notifyElementEvicted(ehcache.getName(), element.getObjectKey());
                }

                @Override
                public void notifyRemoveAll(Ehcache ehcache) {
                    listener.notifyRemoveAll(ehcache.getName());
                }

                @Override
                public void dispose() {
                }

                @Override
                public Object clone() throws CloneNotSupportedException {
                    throw new CloneNotSupportedException();
                }
            });
        }
    }

    @Override
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

    @Override
    public void put(Object key, Object value) throws CacheException {
        try {
            Element _element = new Element(key, value);
            int _timeout = 0;
            if (value instanceof CacheElement) {
                _timeout = ((CacheElement) value).getTimeout();
            }
            if (_timeout <= 0) {
                _timeout = __owner.getModuleCfg().getDefaultCacheTimeout();
            }
            if (_timeout > 0) {
                _element.setTimeToLive(_timeout);
            }
            __ehcache.put(_element);
        } catch (Exception e) {
            throw new CacheException(RuntimeUtils.unwrapThrow(e));
        }
    }

    @Override
    public void update(Object key, Object value) throws CacheException {
        put(key, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List keys() throws CacheException {
        return new ArrayList(__ehcache.getKeys());
    }

    @Override
    public void remove(Object key) throws CacheException {
        try {
            __ehcache.remove(key);
        } catch (IllegalStateException e) {
            throw new CacheException(RuntimeUtils.unwrapThrow(e));
        }
    }

    @Override
    public void removeAll(Collection<?> keys) throws CacheException {
        __ehcache.removeAll(keys);
    }

    @Override
    public void clear() throws CacheException {
        __ehcache.removeAll();
    }

    @Override
    public void destroy() throws CacheException {
        // 不要移除缓存, 否则后果将抛出异常: java.lang.IllegalStateException: The CacheManager has been shut down. It can no longer be used.
    }

    @Override
    public ICacheLocker acquireCacheLocker() {
        return this;
    }

    @Override
    public void readLock(Object key) {
        __ehcache.acquireReadLockOnKey(key);
    }

    @Override
    public void writeLock(Object key) {
        __ehcache.acquireWriteLockOnKey(key);
    }

    @Override
    public boolean tryReadLock(Object key, long timeout) throws CacheException {
        try {
            return __ehcache.tryReadLockOnKey(key, timeout);
        } catch (InterruptedException e) {
            throw new CacheException(e.getMessage(), RuntimeUtils.unwrapThrow(e));
        }
    }

    @Override
    public boolean tryWriteLock(Object key, long timeout) throws CacheException {
        try {
            return __ehcache.tryWriteLockOnKey(key, timeout);
        } catch (InterruptedException e) {
            throw new CacheException(e.getMessage(), RuntimeUtils.unwrapThrow(e));
        }
    }

    @Override
    public void releaseReadLock(Object key) {
        __ehcache.releaseReadLockOnKey(key);
    }

    @Override
    public void releaseWriteLock(Object key) {
        __ehcache.releaseWriteLockOnKey(key);
    }
}
