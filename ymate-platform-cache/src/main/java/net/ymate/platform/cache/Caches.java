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
package net.ymate.platform.cache;

import net.ymate.platform.cache.impl.DefaultCacheConfig;
import net.ymate.platform.cache.support.CacheableProxy;
import net.ymate.platform.commons.LazyHolder;
import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.beans.proxy.IProxyFactory;
import net.ymate.platform.core.module.AbstractModule;
import net.ymate.platform.core.module.IModuleConfigurer;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 缓存管理器
 *
 * @author 刘镇 (suninformation@163.com) on 14-10-16
 */
public final class Caches extends AbstractModule<ICacheConfig> implements ICaches {

    private static final LazyHolder<ICaches> instance = LazyHolder.of(() -> YMP.get().getModuleManager().getModule(Caches.class));

    public static ICaches get() {
        return instance.get();
    }

    public Caches() {
    }

    public Caches(ICacheConfig config) {
        doSetConfig(config);
    }

    @Override
    public String getName() {
        return ICaches.MODULE_NAME;
    }

    @Override
    protected String doGetModuleVersion() {
        return "ymate-platform-cache";
    }

    @Override
    protected ICacheConfig doCreateModuleConfig(Class<?> mainClass, IModuleConfigurer moduleConfigurer) {
        return DefaultCacheConfig.create(mainClass, moduleConfigurer);
    }

    @Override
    protected ICacheConfig doCreateDefaultConfig() {
        return DefaultCacheConfig.defaultConfig();
    }

    @Override
    protected void onInit(IApplication owner) throws Exception {
        owner.getEvents().registerEvent(CacheEvent.class);
        //
        IProxyFactory proxyFactory = owner.getBeanFactory().getProxyFactory();
        if (proxyFactory != null) {
            proxyFactory.registerProxy(new CacheableProxy());
        }
    }

    @Override
    protected void onClose() throws Exception {
    }

    @Override
    public boolean isMultilevel() {
        return ICache.MULTILEVEL.equalsIgnoreCase(getConfig().getCacheProvider().getName());
    }

    @Override
    public Object get(String cacheName, Object key) {
        ICache cache = getConfig().getCacheProvider().getCache(cacheName);
        if (cache != null) {
            return cache.get(key);
        }
        return null;
    }

    @Override
    public Object get(Object key) {
        return get(getConfig().getDefaultCacheName(), key);
    }

    @Override
    public Map<Object, Object> getAll(String cacheName) {
        Map<Object, Object> returnValue = new HashMap<>(16);
        this.keys(cacheName).forEach((key) -> returnValue.put(key, this.get(cacheName, key)));
        return returnValue;
    }

    @Override
    public Map<Object, Object> getAll() {
        return getAll(getConfig().getDefaultCacheName());
    }

    private void doPut(String cacheName, Object key, Object value, int timeout, boolean update) {
        ICache cache = getConfig().getCacheProvider().getCache(cacheName);
        if (cache == null) {
            cache = getConfig().getCacheProvider().createCache(cacheName, getConfig().getCacheEventListener());
        }
        if (update) {
            cache.update(key, value, timeout);
        } else {
            cache.put(key, value, timeout);
        }
    }

    @Override
    public void put(String cacheName, Object key, Object value) {
        doPut(cacheName, key, value, 0, false);
    }

    @Override
    public void put(String cacheName, Object key, Object value, int timeout) throws CacheException {
        doPut(cacheName, key, value, timeout, false);
    }

    @Override
    public void put(Object key, Object value) {
        put(getConfig().getDefaultCacheName(), key, value, 0);
    }

    @Override
    public void put(Object key, Object value, int timeout) throws CacheException {
        put(getConfig().getDefaultCacheName(), key, value, timeout);
    }

    @Override
    public void update(String cacheName, Object key, Object value) {
        doPut(cacheName, key, value, 0, true);
    }

    @Override
    public void update(String cacheName, Object key, Object value, int timeout) throws CacheException {
        doPut(cacheName, key, value, timeout, true);
    }

    @Override
    public void update(Object key, Object value) {
        update(getConfig().getDefaultCacheName(), key, value);
    }

    @Override
    public void update(Object key, Object value, int timeout) throws CacheException {
        update(getConfig().getDefaultCacheName(), key, value, timeout);
    }

    @Override
    public List<?> keys(String cacheName) {
        ICache cache = getConfig().getCacheProvider().getCache(cacheName);
        if (cache != null) {
            return cache.keys();
        }
        return Collections.emptyList();
    }

    @Override
    public List<?> keys() {
        return keys(getConfig().getDefaultCacheName());
    }

    @Override
    public void remove(String cacheName, Object key) {
        ICache cache = getConfig().getCacheProvider().getCache(cacheName);
        if (cache != null) {
            cache.remove(key);
        }
    }

    @Override
    public void remove(Object key) {
        remove(getConfig().getDefaultCacheName(), key);
    }

    @Override
    public void removeAll(String cacheName, List<?> keys) {
        ICache cache = getConfig().getCacheProvider().getCache(cacheName);
        if (cache != null) {
            cache.removeAll(keys);
        }
    }

    @Override
    public void removeAll(List<?> keys) {
        removeAll(getConfig().getDefaultCacheName(), keys);
    }

    @Override
    public void clear(String cacheName) {
        ICache cache = getConfig().getCacheProvider().getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }

    @Override
    public void clear() {
        clear(getConfig().getDefaultCacheName());
    }
}