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

import net.ymate.platform.commons.IPasswordProcessor;
import net.ymate.platform.core.configuration.IConfigReader;
import net.ymate.platform.core.configuration.impl.MapSafeConfigReader;
import net.ymate.platform.core.persistence.AbstractDataSourceConfig;
import net.ymate.platform.persistence.redis.IRedis;
import net.ymate.platform.persistence.redis.IRedisConfig;
import net.ymate.platform.persistence.redis.IRedisDataSourceConfig;
import net.ymate.platform.persistence.redis.RedisServerMeta;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Protocol;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-07-31 18:58
 * @since 2.1.0
 */
public final class DefaultRedisDataSourceConfig extends AbstractDataSourceConfig<IRedis> implements IRedisDataSourceConfig {

    private IRedis.ConnectionType connectionType;

    private String masterServerName;

    private final Map<String, RedisServerMeta> serverMetas = new LinkedHashMap<>();

    private final GenericObjectPoolConfig<?> objectPoolConfig = new GenericObjectPoolConfig<>();

    public static DefaultRedisDataSourceConfig create(String dataSourceName, IConfigReader configReader) throws ClassNotFoundException {
        return new DefaultRedisDataSourceConfig(dataSourceName, configReader);
    }

    public static Builder builder(String dataSourceName) {
        return new Builder(dataSourceName);
    }

    private DefaultRedisDataSourceConfig(String dataSourceName) {
        super(dataSourceName);
    }

    private DefaultRedisDataSourceConfig(String dataSourceName, IConfigReader configReader) throws ClassNotFoundException {
        super(dataSourceName, configReader);
        //
        connectionType = IRedis.ConnectionType.valueOf(configReader.getString(IRedisConfig.CONNECTION_TYPE, IRedisConfig.DEFAULT_STR).toUpperCase());
        masterServerName = configReader.getString(IRedisConfig.MASTER_SERVER_NAME, IRedisConfig.DEFAULT_STR);
        //
        String[] serverNames = StringUtils.split(configReader.getString(IRedisConfig.SERVER_NAME_LIST, IRedisConfig.DEFAULT_STR), "|");
        for (String serverName : serverNames) {
            IConfigReader serverConfigReader = MapSafeConfigReader.bind(configReader.getMap("server." + serverName + "."));
            if (!serverConfigReader.toMap().isEmpty()) {
                serverMetas.put(serverName, RedisServerMeta.builder(serverName)
                        .host(serverConfigReader.getString(IRedisConfig.HOST, Protocol.DEFAULT_HOST))
                        .port(serverConfigReader.getInt(IRedisConfig.PORT, Protocol.DEFAULT_PORT))
                        .timeout(serverConfigReader.getInt(IRedisConfig.TIMEOUT, Protocol.DEFAULT_TIMEOUT))
                        .socketTimeout(serverConfigReader.getInt(IRedisConfig.SOCKET_TIMEOUT, Protocol.DEFAULT_TIMEOUT))
                        .maxAttempts(serverConfigReader.getInt(IRedisConfig.MAX_ATTEMPTS, 3))
                        .weight(serverConfigReader.getInt(IRedisConfig.WEIGHT, 1))
                        .database(serverConfigReader.getInt(IRedisConfig.DATABASE, Protocol.DEFAULT_DATABASE))
                        .clientName(serverConfigReader.getString(IRedisConfig.CLIENT_NAME))
                        .password(serverConfigReader.getString(IRedisConfig.PASSWORD)).build());
            }
        }
        //
        IConfigReader poolConfigReader = MapSafeConfigReader.bind(configReader.getMap("pool."));
        if (!poolConfigReader.toMap().isEmpty()) {
            objectPoolConfig.setMinIdle(poolConfigReader.getInt(IRedisConfig.MIN_IDLE, GenericObjectPoolConfig.DEFAULT_MIN_IDLE));
            objectPoolConfig.setMaxIdle(poolConfigReader.getInt(IRedisConfig.MAX_IDLE, GenericObjectPoolConfig.DEFAULT_MAX_IDLE));
            objectPoolConfig.setMaxTotal(poolConfigReader.getInt(IRedisConfig.MAX_TOTAL, GenericObjectPoolConfig.DEFAULT_MAX_TOTAL));
            objectPoolConfig.setBlockWhenExhausted(poolConfigReader.getBoolean(IRedisConfig.BLOCK_WHEN_EXHAUSTED, GenericObjectPoolConfig.DEFAULT_BLOCK_WHEN_EXHAUSTED));
            objectPoolConfig.setFairness(poolConfigReader.getBoolean(IRedisConfig.FAIRNESS, GenericObjectPoolConfig.DEFAULT_FAIRNESS));
            objectPoolConfig.setJmxEnabled(poolConfigReader.getBoolean(IRedisConfig.JMX_ENABLE, GenericObjectPoolConfig.DEFAULT_JMX_ENABLE));
            objectPoolConfig.setJmxNameBase(poolConfigReader.getString(IRedisConfig.JMX_NAME_BASE, GenericObjectPoolConfig.DEFAULT_JMX_NAME_BASE));
            objectPoolConfig.setJmxNamePrefix(poolConfigReader.getString(IRedisConfig.JMX_NAME_PREFIX, GenericObjectPoolConfig.DEFAULT_JMX_NAME_PREFIX));
            objectPoolConfig.setEvictionPolicyClassName(poolConfigReader.getString(IRedisConfig.EVICTION_POLICY_CLASS_NAME, GenericObjectPoolConfig.DEFAULT_EVICTION_POLICY_CLASS_NAME));
            objectPoolConfig.setLifo(poolConfigReader.getBoolean(IRedisConfig.LIFO, GenericObjectPoolConfig.DEFAULT_LIFO));
            //
            long maxWaitMillis = poolConfigReader.getLong(IRedisConfig.MAX_WAIT_MILLIS);
            objectPoolConfig.setMaxWait(maxWaitMillis > 0 ? Duration.ofMillis(maxWaitMillis) : GenericObjectPoolConfig.DEFAULT_MAX_WAIT);
            //
            long minEvictableIdleTime = poolConfigReader.getLong(IRedisConfig.MIN_EVICTABLE_IDLE_TIME_MILLIS);
            objectPoolConfig.setMinEvictableIdleTime(minEvictableIdleTime > 0 ? Duration.ofMillis(minEvictableIdleTime) : GenericObjectPoolConfig.DEFAULT_MIN_EVICTABLE_IDLE_DURATION);
            //
            long softMinEvictableIdleTimeMillis = poolConfigReader.getLong(IRedisConfig.SOFT_MIN_EVICTABLE_IDLE_TIME_MILLIS);
            objectPoolConfig.setSoftMinEvictableIdleTime(softMinEvictableIdleTimeMillis > 0 ? Duration.ofMillis(softMinEvictableIdleTimeMillis) : GenericObjectPoolConfig.DEFAULT_SOFT_MIN_EVICTABLE_IDLE_DURATION);
            //
            objectPoolConfig.setTestOnBorrow(poolConfigReader.getBoolean(IRedisConfig.TEST_ON_BORROW, GenericObjectPoolConfig.DEFAULT_TEST_ON_BORROW));
            objectPoolConfig.setTestOnReturn(poolConfigReader.getBoolean(IRedisConfig.TEST_ON_RETURN, GenericObjectPoolConfig.DEFAULT_TEST_ON_RETURN));
            objectPoolConfig.setTestOnCreate(poolConfigReader.getBoolean(IRedisConfig.TEST_ON_CREATE, GenericObjectPoolConfig.DEFAULT_TEST_ON_CREATE));
            objectPoolConfig.setTestWhileIdle(poolConfigReader.getBoolean(IRedisConfig.TEST_WHILE_IDLE, GenericObjectPoolConfig.DEFAULT_TEST_WHILE_IDLE));
            objectPoolConfig.setNumTestsPerEvictionRun(poolConfigReader.getInt(IRedisConfig.NUM_TESTS_PER_EVICTION_RUN, GenericObjectPoolConfig.DEFAULT_NUM_TESTS_PER_EVICTION_RUN));
            //
            long timeBetweenEvictionRunsMillis = poolConfigReader.getLong(IRedisConfig.TIME_BETWEEN_EVICTION_RUNS_MILLIS);
            objectPoolConfig.setTimeBetweenEvictionRuns(timeBetweenEvictionRunsMillis > 0 ? Duration.ofMillis(timeBetweenEvictionRunsMillis) : GenericObjectPoolConfig.DEFAULT_TIME_BETWEEN_EVICTION_RUNS);
        }
    }

    @Override
    protected void doInitialize(IRedis iRedis) throws Exception {
    }

    @Override
    public IRedis.ConnectionType getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(IRedis.ConnectionType connectionType) {
        if (!isInitialized()) {
            this.connectionType = connectionType;
        }
    }

    @Override
    public String getMasterServerName() {
        return masterServerName;
    }

    public void setMasterServerName(String masterServerName) {
        if (!isInitialized()) {
            this.masterServerName = masterServerName;
        }
    }

    @Override
    public RedisServerMeta getMasterServerMeta() {
        return serverMetas.get(masterServerName);
    }

    @Override
    public Map<String, RedisServerMeta> getServerMetas() {
        return Collections.unmodifiableMap(serverMetas);
    }

    public void addServerMeta(RedisServerMeta serverMeta) {
        if (!isInitialized()) {
            serverMetas.put(serverMeta.getName(), serverMeta);
        }
    }

    @Override
    public GenericObjectPoolConfig<?> getObjectPoolConfig() {
        return objectPoolConfig;
    }

    public static final class Builder {

        private final DefaultRedisDataSourceConfig config;

        private Builder(String dataSourceName) {
            config = new DefaultRedisDataSourceConfig(dataSourceName);
        }

        public Builder autoConnection(boolean autoConnection) {
            config.setAutoConnection(autoConnection);
            return this;
        }

        public Builder connectionType(IRedis.ConnectionType connectionType) {
            config.setConnectionType(connectionType);
            return this;
        }

        public Builder masterServerName(String masterServerName) {
            config.setMasterServerName(masterServerName);
            return this;
        }

        public Builder addServerMetas(RedisServerMeta... serverMetas) {
            if (serverMetas != null && serverMetas.length > 0) {
                Arrays.stream(serverMetas).forEachOrdered(config::addServerMeta);
            }
            return this;
        }

        public Builder username(String username) {
            config.setUsername(username);
            return this;
        }

        public Builder password(String password) {
            config.setPassword(password);
            return this;
        }

        public Builder passwordEncrypted(boolean passwordEncrypted) {
            config.setPasswordEncrypted(passwordEncrypted);
            return this;
        }

        public Builder passwordClass(Class<? extends IPasswordProcessor> passwordClass) {
            config.setPasswordClass(passwordClass);
            return this;
        }

        public Builder poolMinIdle(int minIdle) {
            config.getObjectPoolConfig().setMinIdle(minIdle);
            return this;
        }

        public Builder poolMaxIdle(int maxIdle) {
            config.getObjectPoolConfig().setMaxIdle(maxIdle);
            return this;
        }

        public Builder poolMaxTotal(int maxTotal) {
            config.getObjectPoolConfig().setMaxTotal(maxTotal);
            return this;
        }

        public Builder poolBlockWhenExhausted(boolean blockWhenExhausted) {
            config.getObjectPoolConfig().setBlockWhenExhausted(blockWhenExhausted);
            return this;
        }

        public Builder poolFairness(boolean fairness) {
            config.getObjectPoolConfig().setFairness(fairness);
            return this;
        }

        public Builder poolJmxEnabled(boolean jmxEnabled) {
            config.getObjectPoolConfig().setJmxEnabled(jmxEnabled);
            return this;
        }

        public Builder poolJmxNameBase(String jmxNameBase) {
            config.getObjectPoolConfig().setJmxNameBase(jmxNameBase);
            return this;
        }

        public Builder poolJmxNamePrefix(String jmxNamePrefix) {
            config.getObjectPoolConfig().setJmxNamePrefix(jmxNamePrefix);
            return this;
        }

        public Builder poolEvictionPolicyClassName(String evictionPolicyClassName) {
            config.getObjectPoolConfig().setEvictionPolicyClassName(evictionPolicyClassName);
            return this;
        }

        public Builder poolLifo(boolean lifo) {
            config.getObjectPoolConfig().setLifo(lifo);
            return this;
        }

        public Builder poolMaxWaitMillis(long maxWaitMillis) {
            config.getObjectPoolConfig().setMaxWait(maxWaitMillis > 0 ? Duration.ofMillis(maxWaitMillis) : GenericObjectPoolConfig.DEFAULT_MAX_WAIT);
            return this;
        }

        public Builder poolMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
            config.getObjectPoolConfig().setMinEvictableIdleTime(minEvictableIdleTimeMillis > 0 ? Duration.ofMillis(minEvictableIdleTimeMillis) : GenericObjectPoolConfig.DEFAULT_MIN_EVICTABLE_IDLE_DURATION);
            return this;
        }

        public Builder poolSoftMinEvictableIdleTimeMillis(long softMinEvictableIdleTimeMillis) {
            config.getObjectPoolConfig().setSoftMinEvictableIdleTime(softMinEvictableIdleTimeMillis > 0 ? Duration.ofMillis(softMinEvictableIdleTimeMillis) : GenericObjectPoolConfig.DEFAULT_SOFT_MIN_EVICTABLE_IDLE_DURATION);
            return this;
        }

        public Builder poolTestOnBorrow(boolean testOnBorrow) {
            config.getObjectPoolConfig().setTestOnBorrow(testOnBorrow);
            return this;
        }

        public Builder poolTestOnReturn(boolean testOnReturn) {
            config.getObjectPoolConfig().setTestOnReturn(testOnReturn);
            return this;
        }

        public Builder poolTestOnCreate(boolean testOnCreate) {
            config.getObjectPoolConfig().setTestOnCreate(testOnCreate);
            return this;
        }

        public Builder poolTestWhileIdle(boolean testWhileIdle) {
            config.getObjectPoolConfig().setTestWhileIdle(testWhileIdle);
            return this;
        }

        public Builder poolNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
            config.getObjectPoolConfig().setNumTestsPerEvictionRun(numTestsPerEvictionRun);
            return this;
        }

        public Builder poolTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
            config.getObjectPoolConfig().setTimeBetweenEvictionRuns(timeBetweenEvictionRunsMillis > 0 ? Duration.ofMillis(timeBetweenEvictionRunsMillis) : GenericObjectPoolConfig.DEFAULT_TIME_BETWEEN_EVICTION_RUNS);
            return this;
        }

        public DefaultRedisDataSourceConfig build() {
            return config;
        }
    }
}
