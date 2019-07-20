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
package net.ymate.platform.cache.impl;

import net.ymate.platform.cache.CacheEvent;
import net.ymate.platform.cache.ICacheEventListener;
import net.ymate.platform.cache.ICaches;

/**
 * @author 刘镇 (suninformation@163.com) on 2017/11/20 上午2:12
 */
public class DefaultCacheEventListener implements ICacheEventListener {

    private ICaches owner;

    private boolean initialized;

    @Override
    public void initialize(ICaches owner) {
        if (!initialized) {
            this.owner = owner;
            this.initialized = true;
        }
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void close() throws Exception {
        if (initialized) {
            initialized = false;
            owner = null;
        }
    }

    @Override
    public ICaches getOwner() {
        return owner;
    }

    @Override
    public void notifyElementRemoved(String cacheName, Object key) {
        owner.getOwner().getEvents().fireEvent(new CacheEvent(owner, CacheEvent.EVENT.ELEMENT_REMOVED)
                .addParamExtend(CACHE_NAME, cacheName)
                .addParamExtend(CACHE_KEY, key));
    }

    @Override
    public void notifyElementPut(String cacheName, Object key, Object value) {
        owner.getOwner().getEvents().fireEvent(new CacheEvent(owner, CacheEvent.EVENT.ELEMENT_PUT)
                .addParamExtend(CACHE_NAME, cacheName)
                .addParamExtend(CACHE_KEY, key)
                .addParamExtend(CACHE_VALUE, value));
    }

    @Override
    public void notifyElementUpdated(String cacheName, Object key, Object value) {
        owner.getOwner().getEvents().fireEvent(new CacheEvent(owner, CacheEvent.EVENT.ELEMENT_UPDATED)
                .addParamExtend(CACHE_NAME, cacheName)
                .addParamExtend(CACHE_KEY, key)
                .addParamExtend(CACHE_VALUE, value));
    }

    @Override
    public void notifyElementExpired(String cacheName, Object key) {
        owner.getOwner().getEvents().fireEvent(new CacheEvent(owner, CacheEvent.EVENT.ELEMENT_EXPIRED)
                .addParamExtend(CACHE_NAME, cacheName)
                .addParamExtend(CACHE_KEY, key));
    }

    @Override
    public void notifyElementEvicted(String cacheName, Object key) {
        owner.getOwner().getEvents().fireEvent(new CacheEvent(owner, CacheEvent.EVENT.ELEMENT_EVICTED)
                .addParamExtend(CACHE_NAME, cacheName)
                .addParamExtend(CACHE_KEY, key));
    }

    @Override
    public void notifyRemoveAll(String cacheName) {
        owner.getOwner().getEvents().fireEvent(new CacheEvent(owner, CacheEvent.EVENT.ELEMENT_REMOVED_ALL)
                .addParamExtend(CACHE_NAME, cacheName));
    }
}
