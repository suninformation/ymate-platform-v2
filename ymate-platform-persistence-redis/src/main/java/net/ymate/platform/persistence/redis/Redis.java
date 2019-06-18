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

import net.ymate.platform.core.Version;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.module.IModule;
import net.ymate.platform.core.module.annotation.Module;
import net.ymate.platform.core.util.RuntimeUtils;
import net.ymate.platform.core.util.ThreadUtils;
import net.ymate.platform.persistence.IDataSourceRouter;
import net.ymate.platform.persistence.redis.impl.RedisCommandsHolder;
import net.ymate.platform.persistence.redis.impl.RedisDataSourceAdapter;
import net.ymate.platform.persistence.redis.impl.DefaultRedisModuleCfg;
import net.ymate.platform.persistence.redis.impl.RedisSession;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import redis.clients.jedis.JedisPubSub;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/30 上午3:14
 * @version 1.0
 */
@Module
public class Redis implements IModule, IRedis {

    public static final Version VERSION = new Version(2, 0, 7, Redis.class.getPackage().getImplementationVersion(), Version.VersionType.Release);

    private static final Log _LOG = LogFactory.getLog(Redis.class);

    private static volatile IRedis __instance;

    private YMP __owner;

    private IRedisModuleCfg __moduleCfg;

    private Map<String, IRedisDataSourceAdapter> __dataSourceCaches;

    private Map<String, JedisPubSub> __pubSubs = new ConcurrentHashMap<String, JedisPubSub>();

    private ExecutorService __subscribePool;

    private boolean __inited;

    /**
     * @return 返回默认Redis模块管理器实例对象
     */
    public static IRedis get() {
        if (__instance == null) {
            synchronized (VERSION) {
                if (__instance == null) {
                    __instance = YMP.get().getModule(Redis.class);
                }
            }
        }
        return __instance;
    }

    /**
     * @param owner YMP框架管理器实例
     * @return 返回指定YMP框架管理器容器内的Redis模块管理器实例
     */
    public static IRedis get(YMP owner) {
        return owner.getModule(Redis.class);
    }

    @Override
    public String getName() {
        return IRedis.MODULE_NAME;
    }

    @Override
    public void init(YMP owner) throws Exception {
        if (!__inited) {
            //
            _LOG.info("Initializing ymate-platform-persistence-redis-" + VERSION);
            //
            __owner = owner;
            __moduleCfg = new DefaultRedisModuleCfg(owner);
            //
            __dataSourceCaches = new HashMap<String, IRedisDataSourceAdapter>();
            for (RedisDataSourceCfgMeta _meta : __moduleCfg.getDataSourceCfgs().values()) {
                IRedisDataSourceAdapter _adapter = new RedisDataSourceAdapter();
                _adapter.initialize(_meta);
                // 将数据源适配器添加到缓存
                __dataSourceCaches.put(_meta.getName(), _adapter);
            }
            //
            __subscribePool = ThreadUtils.newCachedThreadPool(ThreadUtils.createFactory("redis-subscribe-pool"));
            //
            __inited = true;
        }
    }

    @Override
    public boolean isInited() {
        return __inited;
    }

    @Override
    public YMP getOwner() {
        return __owner;
    }

    @Override
    public void destroy() throws Exception {
        if (__inited) {
            __inited = false;
            //
            for (Map.Entry<String, JedisPubSub> _entry : __pubSubs.entrySet()) {
                _entry.getValue().unsubscribe();
            }
            __subscribePool.shutdown();
            __subscribePool = null;
            //
            for (IRedisDataSourceAdapter _adapter : __dataSourceCaches.values()) {
                _adapter.destroy();
            }
            __dataSourceCaches = null;
            __moduleCfg = null;
            __owner = null;
        }
    }

    @Override
    public IRedisModuleCfg getModuleCfg() {
        return __moduleCfg;
    }

    @Override
    public IRedisCommandsHolder getDefaultCommandsHolder() {
        return new RedisCommandsHolder(__dataSourceCaches.get(__moduleCfg.getDataSourceDefaultName()));
    }

    @Override
    public IRedisCommandsHolder getCommandsHolder(String dsName) {
        return new RedisCommandsHolder(__dataSourceCaches.get(dsName));
    }

    @Override
    public <T> T openSession(IRedisSessionExecutor<T> executor) throws Exception {
        IRedisSession _session = new RedisSession(this, getDefaultCommandsHolder());
        try {
            return executor.execute(_session);
        } finally {
            _session.close();
        }
    }

    @Override
    public <T> T openSession(String dsName, IRedisSessionExecutor<T> executor) throws Exception {
        IRedisSession _session = new RedisSession(this, getCommandsHolder(dsName));
        try {
            return executor.execute(_session);
        } finally {
            _session.close();
        }
    }

    @Override
    public <T> T openSession(IRedisCommandsHolder commandsHolder, IRedisSessionExecutor<T> executor) throws Exception {
        IRedisSession _session = new RedisSession(this, commandsHolder);
        try {
            return executor.execute(_session);
        } finally {
            _session.close();
        }
    }

    @Override
    public <T> T openSession(IDataSourceRouter dataSourceRouter, IRedisSessionExecutor<T> executor) throws Exception {
        return openSession(dataSourceRouter.getDataSourceName(), executor);
    }

    @Override
    public IRedisSession openSession() {
        return new RedisSession(this, getDefaultCommandsHolder());
    }

    @Override
    public IRedisSession openSession(String dsName) {
        return new RedisSession(this, getCommandsHolder(dsName));
    }

    @Override
    public IRedisSession openSession(IRedisCommandsHolder commandsHolder) {
        return new RedisSession(this, commandsHolder);
    }

    @Override
    public IRedisSession openSession(IDataSourceRouter dataSourceRouter) {
        return new RedisSession(this, getCommandsHolder(dataSourceRouter.getDataSourceName()));
    }

    @Override
    public void subscribe(JedisPubSub jedisPubSub, String... channels) {
        subscribe(__moduleCfg.getDataSourceDefaultName(), jedisPubSub, channels);
    }

    @Override
    public void subscribe(final String dsName, final JedisPubSub jedisPubSub, final String... channels) {
        String _key = dsName + "@" + jedisPubSub.getClass().getName() + ":" + StringUtils.join(channels, '|');
        if (!__pubSubs.containsKey(_key)) {
            __pubSubs.put(_key, jedisPubSub);
            __subscribePool.execute(new Runnable() {
                @Override
                public void run() {
                    while (__inited) {
                        try {
                            openSession(dsName, new IRedisSessionExecutor<Object>() {
                                @Override
                                public Void execute(IRedisSession session) throws Exception {
                                    session.getCommandHolder().getJedis().subscribe(jedisPubSub, channels);
                                    return null;
                                }
                            });
                        } catch (Exception e) {
                            _LOG.error("Redis connection [" + dsName + "] has been interrupted and is constantly trying to reconnect....", RuntimeUtils.unwrapThrow(e));
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e1) {
                                break;
                            }
                        }
                    }
                }
            });
        }
    }
}
