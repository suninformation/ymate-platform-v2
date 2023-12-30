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
package net.ymate.platform.persistence.redis;

import net.ymate.platform.commons.ReentrantLockHelper;
import net.ymate.platform.commons.impl.DefaultThreadFactory;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.commons.util.ThreadUtils;
import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.IApplicationConfigureFactory;
import net.ymate.platform.core.IApplicationConfigurer;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.module.IModule;
import net.ymate.platform.core.module.IModuleConfigurer;
import net.ymate.platform.core.module.impl.DefaultModuleConfigurer;
import net.ymate.platform.core.persistence.IDataSourceRouter;
import net.ymate.platform.persistence.redis.impl.DefaultRedisConfig;
import net.ymate.platform.persistence.redis.impl.RedisCommandHolder;
import net.ymate.platform.persistence.redis.impl.RedisDataSourceAdapter;
import net.ymate.platform.persistence.redis.impl.RedisSession;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.ShardedJedis;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/30 上午3:14
 */
public final class Redis implements IModule, IRedis {

    private static final Log LOG = LogFactory.getLog(Redis.class);

    private static volatile IRedis instance;

    private static final ReentrantLockHelper LOCKER = new ReentrantLockHelper();

    private IApplication owner;

    private IRedisConfig config;

    private final Map<String, IRedisDataSourceAdapter> dataSourceCaches = new ConcurrentHashMap<>();

    private final Map<String, JedisPubSub> pubSubMap = new ConcurrentHashMap<>();

    private ExecutorService subscribePool;

    private boolean initialized;

    public static IRedis get() {
        IRedis inst = instance;
        if (inst == null) {
            synchronized (Redis.class) {
                inst = instance;
                if (inst == null) {
                    instance = inst = YMP.get().getModuleManager().getModule(Redis.class);
                }
            }
        }
        return inst;
    }

    public Redis() {
    }

    public Redis(IRedisConfig config) {
        this.config = config;
    }

    @Override
    public String getName() {
        return IRedis.MODULE_NAME;
    }

    @Override
    public void initialize(IApplication owner) throws Exception {
        if (!initialized) {
            //
            YMP.showModuleVersion("ymate-platform-persistence-redis", this);
            //
            this.owner = owner;
            //
            if (config == null) {
                IApplicationConfigureFactory configureFactory = owner.getConfigureFactory();
                if (configureFactory != null) {
                    IApplicationConfigurer configurer = configureFactory.getConfigurer();
                    IModuleConfigurer moduleConfigurer = configurer == null ? null : configurer.getModuleConfigurer(MODULE_NAME);
                    if (moduleConfigurer != null) {
                        config = DefaultRedisConfig.create(configureFactory.getMainClass(), moduleConfigurer);
                    } else {
                        config = DefaultRedisConfig.create(configureFactory.getMainClass(), DefaultModuleConfigurer.createEmpty(MODULE_NAME));
                    }
                }
                if (config == null) {
                    config = DefaultRedisConfig.defaultConfig();
                }
            }
            //
            if (!config.isInitialized()) {
                config.initialize(this);
            }
            //
            subscribePool = ThreadUtils.newCachedThreadPool(DefaultThreadFactory.create("redis-subscribe-pool"));
            initialized = true;
        }
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public IApplication getOwner() {
        return owner;
    }

    @Override
    public IRedisConfig getConfig() {
        return config;
    }

    private IRedisDataSourceAdapter doSafeGetDataSourceAdapter(String dataSourceName) {
        IRedisDataSourceAdapter dataSourceAdapter = dataSourceCaches.get(dataSourceName);
        if (dataSourceAdapter == null) {
            ReentrantLock lock = null;
            try {
                lock = LOCKER.getLocker(dataSourceName);
                lock.lock();
                IRedisDataSourceConfig dataSourceConfig = config.getDataSourceConfig(dataSourceName);
                if (dataSourceConfig != null) {
                    if (!dataSourceConfig.isInitialized()) {
                        dataSourceConfig.initialize(this);
                    }
                    // 实例化数据源适配器并放入缓存
                    dataSourceAdapter = dataSourceCaches.computeIfAbsent(dataSourceName, s -> new RedisDataSourceAdapter());
                    if (!dataSourceAdapter.isInitialized()) {
                        dataSourceAdapter.initialize(this, dataSourceConfig);
                    }
                }
            } catch (Exception e) {
                throw RuntimeUtils.wrapRuntimeThrow(e);
            } finally {
                ReentrantLockHelper.unlock(lock);
            }
        }
        if (dataSourceAdapter == null) {
            throw new IllegalStateException(String.format("Datasource '%s' not found.", dataSourceName));
        }
        return dataSourceAdapter;
    }

    @Override
    public IRedisCommandHolder getDefaultConnectionHolder() {
        return getConnectionHolder(config.getDefaultDataSourceName());
    }

    @Override
    public IRedisCommandHolder getConnectionHolder(String dataSourceName) {
        return new RedisCommandHolder(doSafeGetDataSourceAdapter(dataSourceName));
    }

    @Override
    public void releaseConnectionHolder(IRedisCommandHolder connectionHolder) throws Exception {
        connectionHolder.close();
    }

    @Override
    public IRedisDataSourceAdapter getDefaultDataSourceAdapter() {
        return getDataSourceAdapter(config.getDefaultDataSourceName());
    }

    @Override
    public IRedisDataSourceAdapter getDataSourceAdapter(String dataSourceName) {
        return doSafeGetDataSourceAdapter(dataSourceName);
    }

    @Override
    public <T> T openSession(IRedisSessionExecutor<T> executor) throws Exception {
        try (IRedisSession session = new RedisSession(this, getDefaultConnectionHolder())) {
            return executor.execute(session);
        }
    }

    @Override
    public <T> T openSession(String dsName, IRedisSessionExecutor<T> executor) throws Exception {
        try (IRedisSession session = new RedisSession(this, getConnectionHolder(dsName))) {
            return executor.execute(session);
        }
    }

    @Override
    public <T> T openSession(IRedisCommandHolder commandsHolder, IRedisSessionExecutor<T> executor) throws Exception {
        try (IRedisSession session = new RedisSession(this, commandsHolder)) {
            return executor.execute(session);
        }
    }

    @Override
    public <T> T openSession(IDataSourceRouter dataSourceRouter, IRedisSessionExecutor<T> executor) throws Exception {
        return openSession(dataSourceRouter.getDataSourceName(), executor);
    }

    @Override
    public IRedisSession openSession() {
        return new RedisSession(this, getDefaultConnectionHolder());
    }

    @Override
    public IRedisSession openSession(String dsName) {
        return new RedisSession(this, getConnectionHolder(dsName));
    }

    @Override
    public IRedisSession openSession(IRedisCommandHolder commandsHolder) {
        return new RedisSession(this, commandsHolder);
    }

    @Override
    public IRedisSession openSession(IDataSourceRouter dataSourceRouter) {
        return new RedisSession(this, getConnectionHolder(dataSourceRouter.getDataSourceName()));
    }

    @Override
    public void subscribe(JedisPubSub jedisPubSub, String... channels) {
        subscribe(config.getDefaultDataSourceName(), jedisPubSub, channels);
    }

    @Override
    public void subscribe(final String dsName, final JedisPubSub jedisPubSub, final String... channels) {
        String key = dsName + "@" + jedisPubSub.getClass().getName() + ":" + StringUtils.join(channels, '|');
        if (!pubSubMap.containsKey(key)) {
            pubSubMap.put(key, jedisPubSub);
            subscribePool.execute(() -> {
                while (initialized) {
                    try {
                        boolean succeeded = openSession(dsName, session -> {
                            IRedisCommander redisCommander = session.getConnectionHolder().getConnection();
                            if (redisCommander.isSharded()) {
                                // 分片模式下不支持订阅，此处是将订阅命令发送至第一个分片服务
                                ShardedJedis shardedJedis = (ShardedJedis) redisCommander.getOriginJedis();
                                shardedJedis.getAllShards().stream().findFirst().ifPresent(jedis -> jedis.subscribe(jedisPubSub, channels));
                            } else {
                                redisCommander.subscribe(jedisPubSub, channels);
                            }
                            return true;
                        });
                        if (succeeded) {
                            break;
                        }
                    } catch (Exception e) {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn(String.format("Redis connection [%s] has been interrupted and is constantly trying to reconnect....", dsName), RuntimeUtils.unwrapThrow(e));
                        }
                    }
                }
            });
        }
    }

    @Override
    public void close() {
        if (initialized) {
            initialized = false;
            //
            pubSubMap.values().stream().filter(JedisPubSub::isSubscribed).forEach(JedisPubSub::unsubscribe);
            subscribePool.shutdown();
            subscribePool = null;
            //
            dataSourceCaches.values().forEach((dataSourceAdapter) -> {
                try {
                    dataSourceAdapter.close();
                } catch (Exception e) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
                    }
                }
            });
            config = null;
            owner = null;
        }
    }
}
