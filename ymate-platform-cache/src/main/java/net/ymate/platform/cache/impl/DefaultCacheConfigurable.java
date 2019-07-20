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

import net.ymate.platform.cache.ICacheConfig;
import net.ymate.platform.cache.ICaches;
import net.ymate.platform.core.module.IModuleConfigurer;
import net.ymate.platform.core.module.impl.DefaultModuleConfigurable;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-08-12 18:11
 * @since 2.1.0
 */
public final class DefaultCacheConfigurable extends DefaultModuleConfigurable {

    public static Builder builder() {
        return new Builder();
    }

    private DefaultCacheConfigurable() {
        super(ICaches.MODULE_NAME);
    }

    public static final class Builder {

        private final DefaultCacheConfigurable configurable = new DefaultCacheConfigurable();

        private Builder() {
        }

        public Builder cacheProvider(String cacheProvider) {
            configurable.addConfig(ICacheConfig.PROVIDER_CLASS, cacheProvider);
            return this;
        }

        public Builder cacheEventListener(String cacheEventListener) {
            configurable.addConfig(ICacheConfig.EVENT_LISTENER_CLASS, cacheEventListener);
            return this;
        }

        public Builder cacheScopeProcessor(String cacheScopeProcessor) {
            configurable.addConfig(ICacheConfig.SCOPE_PROCESSOR_CLASS, cacheScopeProcessor);
            return this;
        }

        public Builder keyGenerator(String keyGenerator) {
            configurable.addConfig(ICacheConfig.KEY_GENERATOR_CLASS, keyGenerator);
            return this;
        }

        public Builder serializer(String serializer) {
            configurable.addConfig(ICacheConfig.SERIALIZER_CLASS, serializer);
            return this;
        }

        public Builder defaultCacheName(String defaultCacheName) {
            configurable.addConfig(ICacheConfig.DEFAULT_CACHE_NAME, defaultCacheName);
            return this;
        }

        public Builder defaultCacheTimeout(int defaultCacheTimeout) {
            configurable.addConfig(ICacheConfig.DEFAULT_CACHE_TIMEOUT, String.valueOf(defaultCacheTimeout));
            return this;
        }

        public Builder storageWithSet(boolean storageWithSet) {
            configurable.addConfig(ICacheConfig.PARAMS_CACHE_STORAGE_WITH_SET, String.valueOf(storageWithSet));
            return this;
        }

        public Builder enabledSubscribeExpired(boolean enabledSubscribeExpired) {
            configurable.addConfig(ICacheConfig.PARAMS_CACHE_ENABLED_SUBSCRIBE_EXPIRED, String.valueOf(enabledSubscribeExpired));
            return this;
        }

        public Builder multilevelSlavesAutoSync(boolean multilevelSlavesAutoSync) {
            configurable.addConfig(ICacheConfig.PARAMS_CACHE_MULTILEVEL_SLAVE_AUTO_SYNC, String.valueOf(multilevelSlavesAutoSync));
            return this;
        }

        public IModuleConfigurer build() {
            return configurable.toModuleConfigurer();
        }
    }
}
