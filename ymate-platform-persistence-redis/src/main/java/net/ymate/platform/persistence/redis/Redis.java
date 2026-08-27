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

import net.ymate.platform.commons.LazyHolder;
import net.ymate.platform.commons.ReentrantLockHelper;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.module.AbstractModule;
import net.ymate.platform.core.module.IModuleConfigurer;
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

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/30 上午3:14
 */
public final class Redis extends AbstractModule<IRedisConfig> implements IRedis {

    private static final Log LOG = LogFactory.getLog(Redis.class);

    private static final long RECONNECT_INTERVAL = 3000L;

    private static final LazyHolder<IRedis> instance = LazyHolder.of(() -> YMP.get().getModuleManager().getModule(Redis.class));

    private static final ReentrantLockHelper LOCKER = new ReentrantLockHelper();

    public static IRedis get() {
        return instance.get();
    }

    private final Map<String, IRedisDataSourceAdapter> dataSourceCaches = new ConcurrentHashMap<>();

    private final Map<String, JedisPubSub> pubSubMap = new ConcurrentHashMap<>();

    private final Set<Thread> subscribeThreads = ConcurrentHashMap.newKeySet();

    private volatile boolean closed;

    public Redis() {
    }

    public Redis(IRedisConfig config) {
        doSetConfig(config);
    }

    @Override
    public String getName() {
        return IRedis.MODULE_NAME;
    }

    @Override
    protected String doGetModuleVersion() {
        return "ymate-platform-persistence-redis";
    }

    @Override
    protected IRedisConfig doCreateModuleConfig(Class<?> mainClass, IModuleConfigurer moduleConfigurer) throws Exception {
        return DefaultRedisConfig.create(mainClass, moduleConfigurer);
    }

    @Override
    protected IRedisConfig doCreateDefaultConfig() {
        return DefaultRedisConfig.defaultConfig();
    }

    @Override
    protected void onInit(IApplication owner) throws Exception {
        // 处理设置为自动连接的数据源
        getConfig().getDataSourceConfigs()
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().isAutoConnection())
                .map(Map.Entry::getKey)
                .forEach(this::doSafeGetDataSourceAdapter);
    }

    @Override
    protected void onClose() throws Exception {
        closed = true;
        //
        pubSubMap.values().stream().filter(JedisPubSub::isSubscribed).forEach(JedisPubSub::unsubscribe);
        pubSubMap.clear();
        // 中断所有订阅连接
        for (Thread t : subscribeThreads) {
            t.interrupt();
        }
        subscribeThreads.clear();
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
        dataSourceCaches.clear();
    }

    private IRedisDataSourceAdapter doSafeGetDataSourceAdapter(String dataSourceName) {
        IRedisDataSourceAdapter dataSourceAdapter = dataSourceCaches.get(dataSourceName);
        if (dataSourceAdapter == null) {
            ReentrantLock lock = null;
            try {
                lock = LOCKER.getLocker(dataSourceName);
                lock.lock();
                IRedisDataSourceConfig dataSourceConfig = getConfig().getDataSourceConfig(dataSourceName);
                if (dataSourceConfig != null) {
                    if (!dataSourceConfig.isInitialized()) {
                        dataSourceConfig.initialize(this);
                    }
                    // 实例化数据源适配器并放入缓存
                    dataSourceAdapter = dataSourceCaches.get(dataSourceName);
                    if (dataSourceAdapter == null) {
                        dataSourceAdapter = new RedisDataSourceAdapter();
                    }
                    if (!dataSourceAdapter.isInitialized()) {
                        dataSourceAdapter.initialize(this, dataSourceConfig);
                    }
                    dataSourceCaches.put(dataSourceName, dataSourceAdapter);
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
        return getConnectionHolder(getConfig().getDefaultDataSourceName());
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
        return getDataSourceAdapter(getConfig().getDefaultDataSourceName());
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
        subscribe(getConfig().getDefaultDataSourceName(), jedisPubSub, channels);
    }

    @Override
    public void subscribe(final String dsName, final JedisPubSub jedisPubSub, final String... channels) {
        String key = dsName + "@" + jedisPubSub.getClass().getName() + ":" + StringUtils.join(channels, '|');
        if (!pubSubMap.containsKey(key)) {
            pubSubMap.put(key, jedisPubSub);
            IRedisDataSourceAdapter dataSourceAdapter = doSafeGetDataSourceAdapter(dsName);
            Thread subscribeThread = new Thread(() -> {
                while (!closed && !Thread.currentThread().isInterrupted()) {
                    try (IRedisCommander commander = dataSourceAdapter.getConnection()) {
                        if (commander.isSharded()) {
                            // 分片模式下不支持订阅，此处是将订阅命令发送至第一个分片服务
                            ShardedJedis shardedJedis = (ShardedJedis) commander.getOriginJedis();
                            shardedJedis.getAllShards().stream().findFirst().ifPresent(jedis -> jedis.subscribe(jedisPubSub, channels));
                        } else {
                            commander.subscribe(jedisPubSub, channels);
                        }
                    } catch (Exception e) {
                        if (closed) {
                            break;
                        }
                        if (LOG.isWarnEnabled()) {
                            LOG.warn(String.format("Redis connection [%s] has been interrupted and is constantly trying to reconnect....", dsName), RuntimeUtils.unwrapThrow(e));
                        }
                        try {
                            Thread.sleep(RECONNECT_INTERVAL);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }, "redis-subscriber-" + dsName + "-" + Arrays.toString(channels));
            subscribeThread.setDaemon(true);
            subscribeThreads.add(subscribeThread);
            subscribeThread.start();
        }
    }
}