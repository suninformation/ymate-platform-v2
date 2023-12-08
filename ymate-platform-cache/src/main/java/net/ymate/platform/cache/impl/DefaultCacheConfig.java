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
import net.ymate.platform.cache.annotation.CacheConf;
import net.ymate.platform.commons.serialize.ISerializer;
import net.ymate.platform.commons.serialize.SerializerManager;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.configuration.IConfigReader;
import net.ymate.platform.core.module.IModuleConfigurer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;

/**
 * 缓存模块配置类
 *
 * @author 刘镇 (suninformation@163.com) on 14/12/25 下午5:58
 */
public final class DefaultCacheConfig implements ICacheConfig {

    private static final Log LOG = LogFactory.getLog(DefaultCacheConfig.class);

    private ICacheManager cacheManager;

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

    public static DefaultCacheConfig defaultConfig() {
        return builder().build();
    }

    public static DefaultCacheConfig create(IModuleConfigurer moduleConfigurer) {
        return new DefaultCacheConfig(null, moduleConfigurer);
    }

    public static DefaultCacheConfig create(Class<?> mainClass, IModuleConfigurer moduleConfigurer) {
        return new DefaultCacheConfig(mainClass, moduleConfigurer);
    }

    public static Builder builder() {
        return new Builder();
    }

    private DefaultCacheConfig() {
    }

    private DefaultCacheConfig(Class<?> mainClass, IModuleConfigurer moduleConfigurer) {
        IConfigReader configReader = moduleConfigurer.getConfigReader();
        //
        CacheConf confAnn = mainClass == null ? null : mainClass.getAnnotation(CacheConf.class);
        //
        String providerClassStr = configReader.getString(PROVIDER_CLASS, confAnn != null && !confAnn.providerClass().equals(ICacheProvider.class) ? confAnn.providerClass().getName() : DEFAULT_STR);
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
        cacheEventListener = configReader.getClassImpl(EVENT_LISTENER_CLASS, confAnn == null || confAnn.eventListenerClass().equals(ICacheEventListener.class) ? null : confAnn.eventListenerClass().getName(), ICacheEventListener.class);
        cacheScopeProcessor = configReader.getClassImpl(SCOPE_PROCESSOR_CLASS, confAnn == null || confAnn.scopeProcessorClass().equals(ICacheScopeProcessor.class) ? null : confAnn.scopeProcessorClass().getName(), ICacheScopeProcessor.class);
        serializer = SerializerManager.getSerializer(configReader.getString(SERIALIZER_CLASS, confAnn == null || confAnn.serializerClass().equals(ISerializer.class) ? null : confAnn.serializerClass().getName()));
        keyGenerator = configReader.getClassImpl(KEY_GENERATOR_CLASS, confAnn == null || confAnn.keyGeneratorClass().equals(ICacheKeyGenerator.class) ? null : confAnn.keyGeneratorClass().getName(), ICacheKeyGenerator.class);
        defaultCacheName = configReader.getString(DEFAULT_CACHE_NAME, confAnn != null ? confAnn.defaultCacheName() : null);
        defaultCacheTimeout = configReader.getInt(DEFAULT_CACHE_TIMEOUT, confAnn != null ? confAnn.defaultCacheTimeout() : 0);
        //
        configFile = new File(RuntimeUtils.replaceEnvVariable(configReader.getString(CONFIG_FILE, StringUtils.defaultIfBlank(confAnn != null ? confAnn.configFile() : null, DEFAULT_CONFIG_FILE))));
        storageWithSet = configReader.getBoolean(ICacheConfig.STORAGE_WITH_SET, confAnn != null && confAnn.storageWithSet());
        enabledSubscribeExpired = configReader.getBoolean(ICacheConfig.ENABLED_SUBSCRIBE_EXPIRED, confAnn != null && confAnn.subscribeExpired());
        multilevelSlavesAutoSync = configReader.getBoolean(ICacheConfig.MULTILEVEL_SLAVE_AUTO_SYNC, confAnn != null && confAnn.multilevelSlavesAutoSync());
    }

    @Override
    public void initialize(ICaches owner) throws Exception {
        if (!initialized) {
            if (cacheEventListener == null) {
                cacheEventListener = ClassUtils.loadClass(ICacheEventListener.class, DefaultCacheEventListener.class);
            }
            cacheEventListener.initialize(owner);
            //
            if (cacheScopeProcessor == null) {
                cacheScopeProcessor = ClassUtils.loadClass(ICacheScopeProcessor.class);
            }
            if (cacheScopeProcessor != null && LOG.isInfoEnabled()) {
                LOG.info(String.format("Using CacheScopeProcessor class [%s].", cacheScopeProcessor.getClass().getName()));
            }
            //
            if (serializer == null) {
                serializer = SerializerManager.getDefaultSerializer();
            }
            if (keyGenerator == null) {
                keyGenerator = ClassUtils.loadClass(ICacheKeyGenerator.class, DefaultCacheKeyGenerator.class);
            }
            keyGenerator.initialize(owner, serializer);
            //
            defaultCacheName = StringUtils.defaultIfBlank(defaultCacheName, DEFAULT_STR);
            if (defaultCacheTimeout < 0) {
                defaultCacheTimeout = 0;
            }
            if (configFile == null || !configFile.isAbsolute() || configFile.isDirectory()) {
                configFile = null;
            }
            //
            cacheManager = ClassUtils.getExtensionLoader(ICacheManager.class).getExtension();
            if (cacheManager == null) {
                cacheManager = new DefaultCacheManager();
            }
            if (LOG.isInfoEnabled()) {
                LOG.info(String.format("Using CacheManager class [%s].", cacheManager.getClass().getName()));
            }
            cacheManager.initialize(owner);
            //
            if (cacheProvider == null) {
                cacheProvider = ClassUtils.loadClass(ICacheProvider.class, DefaultCacheProvider.class);
            }
            cacheProvider.initialize(owner);
            //
            initialized = true;
        }
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public ICacheManager getCacheManager() {
        return cacheManager;
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

    @Override
    public void close() throws Exception {
        if (initialized) {
            initialized = false;
            //
            cacheEventListener.close();
            cacheProvider.close();
            cacheManager.close();
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

        public DefaultCacheConfig build() {
            return config;
        }
    }
}
