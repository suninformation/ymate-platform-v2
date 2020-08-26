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

import net.ymate.platform.core.persistence.AbstractDataSourceConfigurable;
import net.ymate.platform.persistence.redis.IRedis;
import net.ymate.platform.persistence.redis.IRedisConfig;
import net.ymate.platform.persistence.redis.IRedisDataSourceConfigurable;
import net.ymate.platform.persistence.redis.RedisServerConfigurable;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-08-15 04:20
 * @since 2.1.0
 */
public final class DefaultRedisDataSourceConfigurable extends AbstractDataSourceConfigurable implements IRedisDataSourceConfigurable {

    public static Builder builder(String dataSourceName) {
        return new Builder(dataSourceName);
    }

    private final Map<String, RedisServerConfigurable> servers = new HashMap<>();

    private DefaultRedisDataSourceConfigurable(String dataSourceName) {
        super(dataSourceName);
    }

    public void addServer(RedisServerConfigurable serverConfigurable) {
        servers.put(serverConfigurable.getName(), serverConfigurable);
    }

    @Override
    public Map<String, String> toMap() {
        if (!servers.isEmpty()) {
            addConfig(IRedisConfig.SERVER_NAME_LIST, StringUtils.join(servers.keySet(), "|"));
            servers.values().stream().map(RedisServerConfigurable::toMap).forEach(this::addConfig);
        }
        return super.toMap();
    }

    public static final class Builder {

        private final DefaultRedisDataSourceConfigurable configurable;

        private Builder(String dataSourceName) {
            configurable = new DefaultRedisDataSourceConfigurable(dataSourceName);
        }

        public Builder connectionType(IRedis.ConnectionType connectionType) {
            configurable.addConfig(IRedisConfig.CONNECTION_TYPE, connectionType.name());
            return this;
        }

        public Builder masterServerName(String masterServerName) {
            configurable.addConfig(IRedisConfig.MASTER_SERVER_NAME, masterServerName);
            return this;
        }

        public Builder poolMinIdle(int minIdle) {
            configurable.addConfig(IRedisConfig.MIN_IDLE, String.valueOf(minIdle));
            return this;
        }

        public Builder poolMaxIdle(int maxIdle) {
            configurable.addConfig(IRedisConfig.MAX_IDLE, String.valueOf(maxIdle));
            return this;
        }

        public Builder poolMaxTotal(int maxTotal) {
            configurable.addConfig(IRedisConfig.MAX_IDLE, String.valueOf(maxTotal));
            return this;
        }

        public Builder poolBlockWhenExhausted(boolean blockWhenExhausted) {
            configurable.addConfig(IRedisConfig.BLOCK_WHEN_EXHAUSTED, String.valueOf(blockWhenExhausted));
            return this;
        }

        public Builder poolFairness(String fairness) {
            configurable.addConfig(IRedisConfig.FAIRNESS, String.valueOf(fairness));
            return this;
        }

        public Builder poolJmxEnabled(boolean jmxEnabled) {
            configurable.addConfig(IRedisConfig.JMX_ENABLE, String.valueOf(jmxEnabled));
            return this;
        }

        public Builder poolJmxNameBase(String jmxNameBase) {
            configurable.addConfig(IRedisConfig.JMX_NAME_BASE, jmxNameBase);
            return this;
        }

        public Builder poolJmxNamePrefix(String jmxNamePrefix) {
            configurable.addConfig(IRedisConfig.JMX_NAME_PREFIX, jmxNamePrefix);
            return this;
        }

        public Builder poolEvictionPolicyClassName(String evictionPolicyClassName) {
            configurable.addConfig(IRedisConfig.EVICTION_POLICY_CLASS_NAME, evictionPolicyClassName);
            return this;
        }

        public Builder poolLifo(boolean lifo) {
            configurable.addConfig(IRedisConfig.LIFO, String.valueOf(lifo));
            return this;
        }

        public Builder poolMaxWaitMillis(long maxWaitMillis) {
            configurable.addConfig(IRedisConfig.MAX_WAIT_MILLIS, String.valueOf(maxWaitMillis));
            return this;
        }

        public Builder poolMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
            configurable.addConfig(IRedisConfig.MIN_EVICTABLE_IDLE_TIME_MILLIS, String.valueOf(minEvictableIdleTimeMillis));
            return this;
        }

        public Builder poolSoftMinEvictableIdleTimeMillis(long softMinEvictableIdleTimeMillis) {
            configurable.addConfig(IRedisConfig.SOFT_MIN_EVICTABLE_IDLE_TIME_MILLIS, String.valueOf(softMinEvictableIdleTimeMillis));
            return this;
        }

        public Builder poolTestOnBorrow(boolean testOnBorrow) {
            configurable.addConfig(IRedisConfig.TEST_ON_BORROW, String.valueOf(testOnBorrow));
            return this;
        }

        public Builder poolTestOnReturn(boolean testOnReturn) {
            configurable.addConfig(IRedisConfig.TEST_ON_RETURN, String.valueOf(testOnReturn));
            return this;
        }

        public Builder poolTestOnCreate(boolean testOnCreate) {
            configurable.addConfig(IRedisConfig.TEST_ON_CREATE, String.valueOf(testOnCreate));
            return this;
        }

        public Builder poolTestWhileIdle(boolean testWhileIdle) {
            configurable.addConfig(IRedisConfig.TEST_WHILE_IDLE, String.valueOf(testWhileIdle));
            return this;
        }

        public Builder poolNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
            configurable.addConfig(IRedisConfig.NUM_TESTS_PER_EVICTION_RUN, String.valueOf(numTestsPerEvictionRun));
            return this;
        }

        public Builder poolTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
            configurable.addConfig(IRedisConfig.TIME_BETWEEN_EVICTION_RUNS_MILLIS, String.valueOf(timeBetweenEvictionRunsMillis));
            return this;
        }

        public Builder addServers(RedisServerConfigurable... serverConfigurables) {
            if (ArrayUtils.isNotEmpty(serverConfigurables)) {
                Arrays.stream(serverConfigurables).forEach(configurable::addServer);
            }
            return this;
        }

        public DefaultRedisDataSourceConfigurable build() {
            return configurable;
        }
    }
}
