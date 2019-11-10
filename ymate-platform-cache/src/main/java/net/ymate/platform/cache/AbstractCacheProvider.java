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

import net.sf.ehcache.CacheManager;
import net.ymate.platform.commons.ReentrantLockHelper;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.FileUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.persistence.redis.Redis;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/11/8 11:43 PM
 */
public abstract class AbstractCacheProvider implements ICacheProvider {

    private static final Log LOG = LogFactory.getLog(AbstractCacheProvider.class);

    protected static final IRedisCreator REDIS_CREATOR;

    static {
        IRedisCreator redisCreator = null;
        try {
            redisCreator = ClassUtils.getExtensionLoader(IRedisCreator.class).getExtension();
            if (redisCreator == null) {
                redisCreator = Redis::get;
            }
        } catch (NoClassDefFoundError ignored) {
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
            }
        }
        REDIS_CREATOR = redisCreator;
    }

    private ICaches owner;

    private boolean initialized;

    private final Map<String, ICache> caches = new ConcurrentHashMap<>();

    private String cacheNameSafety(String name) {
        if (ICache.DEFAULT.equalsIgnoreCase(name)) {
            return CacheManager.DEFAULT_NAME;
        }
        return name;
    }

    /**
     * 初始化
     *
     * @throws Exception 可能产生的任何异常
     */
    protected abstract void onInitialize() throws Exception;

    /**
     * 销毁
     *
     * @throws Exception 可能产生的任何异常
     */
    protected abstract void onDestroy() throws Exception;

    protected CacheManager doCreateCacheManager() {
        CacheManager cacheManager = null;
        //
        File configFile = owner.getConfig().getConfigFile();
        if (configFile == null) {
            configFile = new File(RuntimeUtils.replaceEnvVariable(ICacheConfig.DEFAULT_CONFIG_FILE));
            try (InputStream inputStream = AbstractCacheProvider.class.getClassLoader().getResourceAsStream("META-INF/default-ehcache.xml")) {
                if (!FileUtils.createFileIfNotExists(configFile, inputStream) && LOG.isInfoEnabled()) {
                    LOG.info(String.format("Failed to create default ehcache file: %s", configFile.getPath()));
                }
            } catch (IOException e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(String.format("An exception occurred while trying to generate the default ehcache file: %s", configFile.getPath()), RuntimeUtils.unwrapThrow(e));
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
        //
        if (cacheManager == null) {
            cacheManager = CacheManager.create();
        }
        return cacheManager;
    }

    @Override
    public void initialize(ICaches owner) throws Exception {
        if (!initialized) {
            this.owner = owner;
            //
            onInitialize();
            //
            initialized = true;
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
            //
            onDestroy();
            //
            Iterator<Map.Entry<String, ICache>> cacheIt = caches.entrySet().iterator();
            while (cacheIt.hasNext()) {
                ICache cache = cacheIt.next().getValue();
                cache.close();
                cacheIt.remove();
            }
        }
    }

    @Override
    public ICaches getOwner() {
        return owner;
    }

    @Override
    public ICache createCache(String name, final ICacheEventListener listener) {
        try {
            final String cacheName = cacheNameSafety(name);
            return ReentrantLockHelper.putIfAbsentAsync(caches, cacheName, () -> onCreateCache(cacheName, listener));
        } catch (Exception e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
            }
        }
        return null;
    }

    /**
     * 创建缓存对象，若已存在则直接返回
     *
     * @param cacheName 缓存名称
     * @param listener  缓存元素过期监听器接口实现
     * @return 返回缓存对象
     */
    protected abstract ICache onCreateCache(String cacheName, ICacheEventListener listener);

    @Override
    public ICache getCache(String name) {
        return getCache(name, true);
    }

    @Override
    public ICache getCache(String name, boolean create) {
        return getCache(name, create, getOwner().getConfig().getCacheEventListener());
    }

    @Override
    public ICache getCache(String name, boolean create, ICacheEventListener listener) {
        try {
            final String cacheName = cacheNameSafety(name);
            return ReentrantLockHelper.putIfAbsentAsync(caches, cacheName, () -> create ? onCreateCache(cacheName, listener) : null);
        } catch (Exception e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
            }
        }
        return null;
    }
}
