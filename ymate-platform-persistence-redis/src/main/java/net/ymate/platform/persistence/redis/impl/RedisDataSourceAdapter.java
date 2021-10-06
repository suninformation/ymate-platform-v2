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
package net.ymate.platform.persistence.redis.impl;

import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.persistence.AbstractDataSourceAdapter;
import net.ymate.platform.persistence.redis.*;
import net.ymate.platform.persistence.redis.support.JedisCommandsWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.*;
import redis.clients.jedis.util.Pool;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 刘镇 (suninformation@163.com) on 15/12/2 上午2:31
 */
public class RedisDataSourceAdapter extends AbstractDataSourceAdapter<IRedis, IRedisDataSourceConfig, IRedisCommander> implements IRedisDataSourceAdapter {

    private static final Log LOG = LogFactory.getLog(RedisDataSourceAdapter.class);

    private Pool<?> pool;

    private JedisCluster jedisCluster;

    private boolean isCluster;

    private boolean isSentinel;

    private boolean isSharded;

    @Override
    @SuppressWarnings("unchecked")
    public void doInitialize(IRedis owner, IRedisDataSourceConfig dataSourceConfig) throws Exception {
        switch (dataSourceConfig.getConnectionType()) {
            case SHARD:
                if (!dataSourceConfig.getServerMetas().isEmpty()) {
                    List<JedisShardInfo> shardInfos = dataSourceConfig.getServerMetas().values().stream()
                            .map(serverMeta -> {
                                JedisShardInfo shardInfo = new JedisShardInfo(serverMeta.getHost(),
                                        serverMeta.getName(),
                                        serverMeta.getPort(),
                                        serverMeta.getTimeout(),
                                        serverMeta.getWeight());
                                try {
                                    shardInfo.setPassword(decryptPasswordIfNeed(serverMeta.getPassword()));
                                } catch (Exception e) {
                                    if (LOG.isWarnEnabled()) {
                                        LOG.warn(String.format("%s initialization failed...", serverMeta), RuntimeUtils.unwrapThrow(e));
                                    }
                                }
                                return shardInfo;
                            })
                            .collect(Collectors.toList());
                    pool = new ShardedJedisPool((GenericObjectPoolConfig<ShardedJedis>) dataSourceConfig.getObjectPoolConfig(), shardInfos);
                    isSharded = true;
                }
                break;
            case SENTINEL:
                if (!dataSourceConfig.getServerMetas().isEmpty()) {
                    Set<String> sentinels = dataSourceConfig.getServerMetas().values().stream()
                            .map(serverMeta -> serverMeta.getHost() + ":" + serverMeta.getPort()).collect(Collectors.toSet());
                    RedisServerMeta masterServerMeta = dataSourceConfig.getMasterServerMeta();
                    pool = new JedisSentinelPool(masterServerMeta.getName(), sentinels,
                            (GenericObjectPoolConfig<Jedis>) dataSourceConfig.getObjectPoolConfig(),
                            masterServerMeta.getTimeout(),
                            decryptPasswordIfNeed(masterServerMeta.getPassword()),
                            masterServerMeta.getDatabase(),
                            masterServerMeta.getClientName());
                    isSentinel = true;
                }
                break;
            case CLUSTER:
                if (!dataSourceConfig.getServerMetas().isEmpty()) {
                    Set<HostAndPort> hostAndPorts = dataSourceConfig.getServerMetas().values().stream()
                            .map(serverMeta -> new HostAndPort(serverMeta.getHost(), serverMeta.getPort()))
                            .collect(Collectors.toSet());
                    RedisServerMeta masterServerMeta = dataSourceConfig.getMasterServerMeta();
                    jedisCluster = new JedisCluster(hostAndPorts,
                            masterServerMeta.getTimeout(),
                            masterServerMeta.getSocketTimeout(),
                            masterServerMeta.getMaxAttempts(),
                            decryptPasswordIfNeed(masterServerMeta.getPassword()),
                            (GenericObjectPoolConfig<Jedis>) dataSourceConfig.getObjectPoolConfig());
                    isCluster = true;
                }
                break;
            case DEFAULT:
            default:
                if (dataSourceConfig.getServerMetas().isEmpty()) {
                    pool = new JedisPool((GenericObjectPoolConfig<Jedis>) dataSourceConfig.getObjectPoolConfig(), "localhost");
                } else {
                    RedisServerMeta defaultServerMeta = dataSourceConfig.getServerMetas().get(owner.getConfig().getDefaultDataSourceName());
                    pool = new JedisPool((GenericObjectPoolConfig<Jedis>) dataSourceConfig.getObjectPoolConfig(),
                            defaultServerMeta.getHost(),
                            defaultServerMeta.getPort(),
                            defaultServerMeta.getTimeout(),
                            decryptPasswordIfNeed(defaultServerMeta.getPassword()),
                            defaultServerMeta.getDatabase(),
                            defaultServerMeta.getClientName());
                }
        }
    }

    @Override
    public boolean initializeIfNeed() throws Exception {
        return isInitialized();
    }

    @Override
    public IRedisCommander getConnection() throws Exception {
        if (isCluster) {
            return JedisCommandsWrapper.bind(jedisCluster);
        } else if (isSharded) {
            return JedisCommandsWrapper.bind((ShardedJedis) pool.getResource());
        }
        return JedisCommandsWrapper.bind((Jedis) pool.getResource(), isSentinel);
    }

    @Override
    public void doClose() throws Exception {
        if (jedisCluster != null) {
            jedisCluster.close();
        }
        if (pool != null) {
            pool.destroy();
        }
    }
}
