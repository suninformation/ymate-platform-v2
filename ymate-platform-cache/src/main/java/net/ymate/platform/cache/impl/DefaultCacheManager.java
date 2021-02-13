/*
 * Copyright 2007-2021 the original author or authors.
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

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.ymate.platform.cache.*;
import net.ymate.platform.cache.support.EhCacheWrapper;
import net.ymate.platform.commons.util.FileUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

/**
 * @author 刘镇 (suninformation@163.com) on 2021/02/13 14:00
 * @version 2.1.0
 */
public class DefaultCacheManager implements ICacheManager {

    private static final Log LOG = LogFactory.getLog(DefaultCacheManager.class);

    private ICaches owner;

    private CacheManager cacheManager;

    private boolean initialized;

    public DefaultCacheManager() {
    }

    @Override
    public void initialize(ICaches owner) throws Exception {
        if (!initialized) {
            this.owner = owner;
            //
            File configFile = owner.getConfig().getConfigFile();
            if (configFile == null) {
                configFile = new File(RuntimeUtils.replaceEnvVariable(ICacheConfig.DEFAULT_CONFIG_FILE));
                try (InputStream inputStream = AbstractCacheProvider.class.getClassLoader().getResourceAsStream("META-INF/default-ehcache.xml")) {
                    if (!FileUtils.createFileIfNotExists(configFile, inputStream) && LOG.isWarnEnabled()) {
                        LOG.warn(String.format("Failed to create default ehcache config file: %s", configFile.getPath()));
                    }
                } catch (IOException e) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(String.format("An exception occurred while trying to generate the default ehcache config file: %s", configFile.getPath()), RuntimeUtils.unwrapThrow(e));
                    }
                }
            }
            if (configFile.exists()) {
                try {
                    cacheManager = CacheManager.create(configFile.toURI().toURL());
                } catch (MalformedURLException e) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
                    }
                }
            }
            if (cacheManager == null) {
                cacheManager = CacheManager.create();
            }
            initialized = true;
        }
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public ICache createCache(String cacheName, ICacheEventListener listener) {
        Ehcache ehcache = cacheManager.getEhcache(cacheName);
        if (ehcache == null) {
            cacheManager.addCache(cacheName);
            ehcache = cacheManager.getCache(cacheName);
        }
        return new EhCacheWrapper(owner, ehcache, listener);
    }

    @Override
    public String cacheNameSafety(String name) {
        if (ICache.DEFAULT.equalsIgnoreCase(name)) {
            return CacheManager.DEFAULT_NAME;
        }
        return name;
    }

    @Override
    public void close() throws IOException {
        if (initialized) {
            initialized = false;
            cacheManager.shutdown();
        }
    }
}
