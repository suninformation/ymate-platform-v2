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

import net.ymate.platform.cache.*;
import net.ymate.platform.core.IConfig;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.serialize.ISerializer;
import net.ymate.platform.core.support.IConfigReader;
import net.ymate.platform.core.support.impl.MapSafeConfigReader;
import net.ymate.platform.core.util.ClassUtils;
import org.apache.commons.lang.StringUtils;

/**
 * 缓存模块配置类
 *
 * @author 刘镇 (suninformation@163.com) on 14/12/25 下午5:58
 * @version 1.0
 */
public class DefaultCacheModuleCfg implements ICacheModuleCfg {

    private ICacheProvider __cacheProvider;

    private ICacheEventListener __cacheEventListener;

    private final ICacheScopeProcessor __cacheScopeProcessor;

    private IKeyGenerator<?> __keyGenerator;

    private ISerializer __serializer;

    private final String __defaultCacheName;

    private int __defaultCacheTimeout;

    public DefaultCacheModuleCfg(YMP owner) throws Exception {
        // 尝试加载配置体系模块，若存在则将决定配置文件加载的路径
        if (!owner.isModuleExcluded(IConfig.MODULE_NAME_CONFIGURATION) && !owner.isModuleExcluded(IConfig.MODULE_CLASS_NAME_CONFIGURATION)) {
            owner.getModule(IConfig.MODULE_CLASS_NAME_CONFIGURATION);
        }
        //
        IConfigReader _moduleCfg = MapSafeConfigReader.bind(owner.getConfig().getModuleConfigs(ICaches.MODULE_NAME));
        //
        String _providerClassStr = _moduleCfg.getString(PROVIDER_CLASS, IConfig.DEFAULT_STR);
        __cacheProvider = ClassUtils.impl(StringUtils.defaultIfBlank(Caches.PROVIDERS.get(_providerClassStr), _providerClassStr), ICacheProvider.class, this.getClass());
        if (__cacheProvider == null) {
            __cacheProvider = new DefaultCacheProvider();
        }
        //
        __cacheEventListener = _moduleCfg.getClassImpl(EVENT_LISTENER_CLASS, ICacheEventListener.class);
        if (__cacheEventListener == null) {
            __cacheEventListener = new DefaultCacheEventListener();
        }
        //
        __cacheScopeProcessor = _moduleCfg.getClassImpl(SCOPE_PROCESSOR_CLASS, ICacheScopeProcessor.class);
        //
        __serializer = ISerializer.SerializerManager.getSerializer(_moduleCfg.getString(SERIALIZER_CLASS, IConfig.DEFAULT_STR));
        if (__serializer == null) {
            __serializer = ISerializer.SerializerManager.getDefaultSerializer();
        }
        //
        __keyGenerator = _moduleCfg.getClassImpl(KEY_GENERATOR_CLASS, IKeyGenerator.class);
        if (__keyGenerator == null) {
            __keyGenerator = new DefaultKeyGenerator();
        }
        __keyGenerator.init(__serializer);
        //
        __defaultCacheName = _moduleCfg.getString(DEFAULT_CACHE_NAME, IConfig.DEFAULT_STR);

        __defaultCacheTimeout = _moduleCfg.getInt(DEFAULT_CACHE_TIMEOUT, IConfig.DEFAULT_INT);
        if (__defaultCacheTimeout <= 0) {
            __defaultCacheTimeout = 300;
        }
    }

    @Override
    public ICacheProvider getCacheProvider() {
        return __cacheProvider;
    }

    @Override
    public ICacheEventListener getCacheEventListener() {
        return __cacheEventListener;
    }

    @Override
    public ICacheScopeProcessor getCacheScopeProcessor() {
        return __cacheScopeProcessor;
    }

    @Override
    public IKeyGenerator<?> getKeyGenerator() {
        return __keyGenerator;
    }

    @Override
    public ISerializer getSerializer() {
        return __serializer;
    }

    @Override
    public String getDefaultCacheName() {
        return __defaultCacheName;
    }

    @Override
    public int getDefaultCacheTimeout() {
        return __defaultCacheTimeout;
    }
}
