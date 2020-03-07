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
import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.beans.proxy.IProxyFactory;
import net.ymate.platform.core.module.IModule;
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
public final class Caches implements IModule, ICaches {

    private static volatile ICaches instance;

    private IApplication owner;

    private ICacheConfig config;

    private boolean initialized;

    public static ICaches get() {
        ICaches inst = instance;
        if (inst == null) {
            synchronized (Caches.class) {
                inst = instance;
                if (inst == null) {
                    instance = inst = YMP.get().getModuleManager().getModule(Caches.class);
                }
            }
        }
        return inst;
    }

    public Caches() {
    }

    public Caches(ICacheConfig config) {
        this.config = config;
    }

    @Override
    public String getName() {
        return ICaches.MODULE_NAME;
    }

    @Override
    public void initialize(IApplication owner) throws Exception {
        if (!initialized) {
            //
            YMP.showModuleVersion("ymate-platform-cache", this);
            //
            this.owner = owner;
            this.owner.getEvents().registerEvent(CacheEvent.class);
            //
            if (config == null) {
                IModuleConfigurer moduleConfigurer = owner.getConfigureFactory().getConfigurer().getModuleConfigurer(MODULE_NAME);
                config = moduleConfigurer == null ? DefaultCacheConfig.defaultConfig() : DefaultCacheConfig.create(moduleConfigurer);
            }
            if (!config.isInitialized()) {
                config.initialize(this);
            }
            //
            IProxyFactory proxyFactory = owner.getBeanFactory().getProxyFactory();
            if (proxyFactory != null) {
                proxyFactory.registerProxy(new CacheableProxy());
            }
            //
            initialized = true;
        }
    }

    @Override
    public boolean isInitialized() {
        return false;
    }

    @Override
    public void close() throws Exception {
        if (initialized) {
            initialized = false;
            //
            config.getCacheProvider().close();
            config = null;
            owner = null;
        }
    }

    @Override
    public IApplication getOwner() {
        return owner;
    }

    @Override
    public ICacheConfig getConfig() {
        return config;
    }

    @Override
    public Object get(String cacheName, Object key) {
        ICache cache = config.getCacheProvider().getCache(cacheName);
        if (cache != null) {
            return cache.get(key);
        }
        return null;
    }

    @Override
    public Object get(Object key) {
        return get(config.getDefaultCacheName(), key);
    }

    @Override
    public Map<Object, Object> getAll(String cacheName) {
        Map<Object, Object> returnValue = new HashMap<>(16);
        this.keys(cacheName).forEach((key) -> returnValue.put(key, this.get(cacheName, key)));
        return returnValue;
    }

    @Override
    public Map<Object, Object> getAll() {
        return getAll(config.getDefaultCacheName());
    }

    private void doPut(String cacheName, Object key, Object value) {
        ICache cache = config.getCacheProvider().getCache(cacheName);
        if (cache == null) {
            cache = config.getCacheProvider().createCache(cacheName, config.getCacheEventListener());
        }
        cache.put(key, value);
    }

    @Override
    public void put(String cacheName, Object key, Object value) {
        doPut(cacheName, key, value);
    }

    @Override
    public void put(Object key, Object value) {
        put(config.getDefaultCacheName(), key, value);
    }

    @Override
    public void update(String cacheName, Object key, Object value) {
        doPut(cacheName, key, value);
    }

    @Override
    public void update(Object key, Object value) {
        update(config.getDefaultCacheName(), key, value);
    }

    @Override
    public List<?> keys(String cacheName) {
        ICache cache = config.getCacheProvider().getCache(cacheName);
        if (cache != null) {
            return cache.keys();
        }
        return Collections.emptyList();
    }

    @Override
    public List<?> keys() {
        return keys(config.getDefaultCacheName());
    }

    @Override
    public void remove(String cacheName, Object key) {
        ICache cache = config.getCacheProvider().getCache(cacheName);
        if (cache != null) {
            cache.remove(key);
        }
    }

    @Override
    public void remove(Object key) {
        remove(config.getDefaultCacheName(), key);
    }

    @Override
    public void removeAll(String cacheName, List keys) {
        ICache cache = config.getCacheProvider().getCache(cacheName);
        if (cache != null) {
            cache.removeAll(keys);
        }
    }

    @Override
    public void removeAll(List keys) {
        removeAll(config.getDefaultCacheName(), keys);
    }

    @Override
    public void clear(String cacheName) {
        ICache cache = config.getCacheProvider().getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }

    @Override
    public void clear() {
        clear(config.getDefaultCacheName());
    }
}
