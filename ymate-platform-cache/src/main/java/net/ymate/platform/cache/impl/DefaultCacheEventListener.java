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
package net.ymate.platform.cache.impl;

import net.ymate.platform.cache.CacheEvent;
import net.ymate.platform.cache.ICacheEventListener;
import net.ymate.platform.cache.ICaches;

/**
 * @author 刘镇 (suninformation@163.com) on 2017/11/20 上午2:12
 * @version 1.0
 */
public class DefaultCacheEventListener implements ICacheEventListener {

    private ICaches __owner;

    @Override
    public void init(ICaches owner) throws Exception {
        __owner = owner;
    }

    @Override
    public void destroy() throws Exception {
        __owner = null;
    }

    @Override
    public void notifyElementRemoved(String cacheName, Object key) {
        __owner.getOwner().getEvents().fireEvent(new CacheEvent(__owner, CacheEvent.EVENT.ELEMENT_REMOVED)
                .addParamExtend("cacheName", cacheName)
                .addParamExtend("key", key));
    }

    @Override
    public void notifyElementPut(String cacheName, Object key, Object value) {
        __owner.getOwner().getEvents().fireEvent(new CacheEvent(__owner, CacheEvent.EVENT.ELEMENT_PUT)
                .addParamExtend("cacheName", cacheName)
                .addParamExtend("key", key)
                .addParamExtend("value", value));
    }

    @Override
    public void notifyElementUpdated(String cacheName, Object key, Object value) {
        __owner.getOwner().getEvents().fireEvent(new CacheEvent(__owner, CacheEvent.EVENT.ELEMENT_UPDATED)
                .addParamExtend("cacheName", cacheName)
                .addParamExtend("key", key)
                .addParamExtend("value", value));
    }

    @Override
    public void notifyElementExpired(String cacheName, Object key) {
        __owner.getOwner().getEvents().fireEvent(new CacheEvent(__owner, CacheEvent.EVENT.ELEMENT_EXPIRED)
                .addParamExtend("cacheName", cacheName)
                .addParamExtend("key", key));
    }

    @Override
    public void notifyElementEvicted(String cacheName, Object key) {
        __owner.getOwner().getEvents().fireEvent(new CacheEvent(__owner, CacheEvent.EVENT.ELEMENT_EVICTED)
                .addParamExtend("cacheName", cacheName)
                .addParamExtend("key", key));
    }

    @Override
    public void notifyRemoveAll(String cacheName) {
        __owner.getOwner().getEvents().fireEvent(new CacheEvent(__owner, CacheEvent.EVENT.ELEMENT_REMOVED_ALL)
                .addParamExtend("cacheName", cacheName));
    }
}
