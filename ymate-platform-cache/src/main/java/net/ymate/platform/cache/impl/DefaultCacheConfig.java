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

import net.ymate.platform.cache.*;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.configuration.IConfigReader;
import net.ymate.platform.core.module.IModuleConfigurer;
import net.ymate.platform.core.serialize.ISerializer;
import net.ymate.platform.core.serialize.SerializerManager;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

/**
 * 缓存模块配置类
 *
 * @author 刘镇 (suninformation@163.com) on 14/12/25 下午5:58
 */
public final class DefaultCacheConfig implements ICacheConfig {

    private ICacheProvider cacheProvider;

    private ICacheEventListener cacheEventListener;

    private ICacheScopeProcessor cacheScopeProcessor;

    private ICacheKeyGenerator<?> keyGenerator;

    private ISerializer serializer;

    private String defaultCacheName;

    private int defaultCacheTimeout;

    private File configFile;

    private boolean storageWithSet;

    private boolean enabledSubscribeExpired;

    private boolean multilevelSlavesAutoSync;

    private boolean initialized;

    public static ICacheConfig defaultConfig() {
        return builder().build();
    }

    public static ICacheConfig create(IModuleConfigurer moduleConfigurer) {
        return new DefaultCacheConfig(moduleConfigurer);
    }

    public static Builder builder() {
        return new Builder();
    }

    private DefaultCacheConfig() {
    }

    private DefaultCacheConfig(IModuleConfigurer moduleConfigurer) {
        IConfigReader configReader = moduleConfigurer.getConfigReader();
        //
        String providerClassStr = configReader.getString(PROVIDER_CLASS, DEFAULT_STR);
        switch (StringUtils.lowerCase(providerClassStr)) {
            case DEFAULT_STR:
                cacheProvider = new DefaultCacheProvider();
                break;
            case "redis":
                cacheProvider = new RedisCacheProvider();
                break;
            case "multilevel":
                cacheProvider = new MultilevelCacheProvider();
                break;
            default:
                cacheProvider = ClassUtils.impl(providerClassStr, ICacheProvider.class, this.getClass());
        }
        //
        cacheEventListener = configReader.getClassImpl(EVENT_LISTENER_CLASS, ICacheEventListener.class);
        cacheScopeProcessor = configReader.getClassImpl(SCOPE_PROCESSOR_CLASS, ICacheScopeProcessor.class);
        serializer = SerializerManager.getSerializer(configReader.getString(SERIALIZER_CLASS));
        keyGenerator = configReader.getClassImpl(KEY_GENERATOR_CLASS, ICacheKeyGenerator.class);
        defaultCacheName = configReader.getString(DEFAULT_CACHE_NAME);
        defaultCacheTimeout = configReader.getInt(DEFAULT_CACHE_TIMEOUT);
        //
        configFile = new File(RuntimeUtils.replaceEnvVariable(configReader.getString(CONFIG_FILE, DEFAULT_CONFIG_FILE)));
        storageWithSet = configReader.getBoolean(ICacheConfig.STORAGE_WITH_SET);
        enabledSubscribeExpired = configReader.getBoolean(ICacheConfig.ENABLED_SUBSCRIBE_EXPIRED);
        multilevelSlavesAutoSync = configReader.getBoolean(ICacheConfig.MULTILEVEL_SLAVE_AUTO_SYNC);
    }

    @Override
    public void initialize(ICaches owner) throws Exception {
        if (!initialized) {
            if (cacheProvider == null) {
                cacheProvider = new DefaultCacheProvider();
            }
            cacheProvider.initialize(owner);
            //
            if (cacheEventListener == null) {
                cacheEventListener = new DefaultCacheEventListener();
            }
            cacheEventListener.initialize(owner);
            //
            if (serializer == null) {
                serializer = SerializerManager.getDefaultSerializer();
            }
            if (keyGenerator == null) {
                keyGenerator = new DefaultCacheKeyGenerator();
            }
            keyGenerator.initialize(owner, serializer);
            //
            defaultCacheName = StringUtils.defaultIfBlank(defaultCacheName, DEFAULT_STR);
            if (defaultCacheTimeout < 0) {
                defaultCacheTimeout = 0;
            }
            if (configFile == null || !configFile.isAbsolute() || !configFile.canRead() || !configFile.exists() || configFile.isDirectory()) {
                configFile = null;
            }
            //
            initialized = true;
        }
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public ICacheProvider getCacheProvider() {
        return cacheProvider;
    }

    public void setCacheProvider(ICacheProvider cacheProvider) {
        if (!initialized) {
            this.cacheProvider = cacheProvider;
        }
    }

    @Override
    public ICacheEventListener getCacheEventListener() {
        return cacheEventListener;
    }

    public void setCacheEventListener(ICacheEventListener cacheEventListener) {
        if (!initialized) {
            this.cacheEventListener = cacheEventListener;
        }
    }

    @Override
    public ICacheScopeProcessor getCacheScopeProcessor() {
        return cacheScopeProcessor;
    }

    public void setCacheScopeProcessor(ICacheScopeProcessor cacheScopeProcessor) {
        if (!initialized) {
            this.cacheScopeProcessor = cacheScopeProcessor;
        }
    }

    @Override
    public ICacheKeyGenerator<?> getKeyGenerator() {
        return keyGenerator;
    }

    public void setKeyGenerator(ICacheKeyGenerator<?> keyGenerator) {
        if (!initialized) {
            this.keyGenerator = keyGenerator;
        }
    }

    @Override
    public ISerializer getSerializer() {
        return serializer;
    }

    public void setSerializer(ISerializer serializer) {
        if (!initialized) {
            this.serializer = serializer;
        }
    }

    @Override
    public String getDefaultCacheName() {
        return defaultCacheName;
    }

    public void setDefaultCacheName(String defaultCacheName) {
        if (!initialized) {
            this.defaultCacheName = defaultCacheName;
        }
    }

    @Override
    public int getDefaultCacheTimeout() {
        return defaultCacheTimeout;
    }

    @Override
    public File getConfigFile() {
        return configFile;
    }

    public void setConfigFile(File configFile) {
        this.configFile = configFile;
    }

    public void setDefaultCacheTimeout(int defaultCacheTimeout) {
        if (!initialized) {
            this.defaultCacheTimeout = defaultCacheTimeout;
        }
    }

    @Override
    public boolean isStorageWithSet() {
        return storageWithSet;
    }

    public void setStorageWithSet(boolean storageWithSet) {
        if (!initialized) {
            this.storageWithSet = storageWithSet;
        }
    }

    @Override
    public boolean isEnabledSubscribeExpired() {
        return enabledSubscribeExpired;
    }

    public void setEnabledSubscribeExpired(boolean enabledSubscribeExpired) {
        if (!initialized) {
            this.enabledSubscribeExpired = enabledSubscribeExpired;
        }
    }

    @Override
    public boolean isMultilevelSlavesAutoSync() {
        return multilevelSlavesAutoSync;
    }

    public void setMultilevelSlavesAutoSync(boolean multilevelSlavesAutoSync) {
        if (!initialized) {
            this.multilevelSlavesAutoSync = multilevelSlavesAutoSync;
        }
    }

    public static final class Builder {

        private final DefaultCacheConfig config = new DefaultCacheConfig();

        private Builder() {
        }

        public Builder cacheProvider(ICacheProvider cacheProvider) {
            config.setCacheProvider(cacheProvider);
            return this;
        }

        public Builder cacheEventListener(ICacheEventListener cacheEventListener) {
            config.setCacheEventListener(cacheEventListener);
            return this;
        }

        public Builder cacheScopeProcessor(ICacheScopeProcessor cacheScopeProcessor) {
            config.setCacheScopeProcessor(cacheScopeProcessor);
            return this;
        }

        public Builder keyGenerator(ICacheKeyGenerator<?> keyGenerator) {
            config.setKeyGenerator(keyGenerator);
            return this;
        }

        public Builder serializer(ISerializer serializer) {
            config.setSerializer(serializer);
            return this;
        }

        public Builder defaultCacheName(String defaultCacheName) {
            config.setDefaultCacheName(defaultCacheName);
            return this;
        }

        public Builder defaultCacheTimeout(int defaultCacheTimeout) {
            config.setDefaultCacheTimeout(defaultCacheTimeout);
            return this;
        }

        public Builder configFile(File configFile) {
            config.setConfigFile(configFile);
            return this;
        }

        public Builder storageWithSet(boolean storageWithSet) {
            config.setStorageWithSet(storageWithSet);
            return this;
        }

        public Builder enabledSubscribeExpired(boolean enabledSubscribeExpired) {
            config.setEnabledSubscribeExpired(enabledSubscribeExpired);
            return this;
        }

        public Builder multilevelSlavesAutoSync(boolean multilevelSlavesAutoSync) {
            config.setMultilevelSlavesAutoSync(multilevelSlavesAutoSync);
            return this;
        }

        public ICacheConfig build() {
            return config;
        }
    }
}
