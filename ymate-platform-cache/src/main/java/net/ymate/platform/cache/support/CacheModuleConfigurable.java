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
package net.ymate.platform.cache.support;

import net.ymate.platform.cache.*;
import net.ymate.platform.core.support.IModuleConfigurable;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2018-12-14 10:48
 * @version 1.0
 * @since 2.0.6
 */
public class CacheModuleConfigurable implements IModuleConfigurable {

    public static CacheModuleConfigurable create() {
        return new CacheModuleConfigurable();
    }

    private Map<String, String> __configs = new HashMap<String, String>();

    public CacheModuleConfigurable providerClass(String providerClass) {
        __configs.put(ICacheModuleCfg.PROVIDER_CLASS, providerClass);
        return this;
    }

    public CacheModuleConfigurable providerClass(Class<? extends ICacheProvider> providerClass) {
        __configs.put(ICacheModuleCfg.PROVIDER_CLASS, providerClass.getName());
        return this;
    }

    public CacheModuleConfigurable eventListenerClass(Class<? extends ICacheEventListener> eventListenerClass) {
        __configs.put(ICacheModuleCfg.EVENT_LISTENER_CLASS, eventListenerClass.getName());
        return this;
    }

    public CacheModuleConfigurable scopeProcessorClass(Class<? extends ICacheScopeProcessor> scopeProcessorClass) {
        __configs.put(ICacheModuleCfg.SCOPE_PROCESSOR_CLASS, scopeProcessorClass.getName());
        return this;
    }

    public CacheModuleConfigurable serializerClass(String serializerClass) {
        __configs.put(ICacheModuleCfg.SERIALIZER_CLASS, StringUtils.trimToEmpty(serializerClass));
        return this;
    }

    public CacheModuleConfigurable keyGeneratorClass(Class<? extends IKeyGenerator> keyGeneratorClass) {
        __configs.put(ICacheModuleCfg.KEY_GENERATOR_CLASS, keyGeneratorClass.getName());
        return this;
    }

    public CacheModuleConfigurable defaultCacheName(String defaultCacheName) {
        __configs.put(ICacheModuleCfg.DEFAULT_CACHE_NAME, StringUtils.trimToEmpty(defaultCacheName));
        return this;
    }

    public CacheModuleConfigurable defaultCacheTimeout(int defaultCacheTimeout) {
        __configs.put(ICacheModuleCfg.DEFAULT_CACHE_TIMEOUT, String.valueOf(defaultCacheTimeout));
        return this;
    }

    @Override
    public String getModuleName() {
        return ICaches.MODULE_NAME;
    }

    @Override
    public Map<String, String> toMap() {
        return __configs;
    }
}
