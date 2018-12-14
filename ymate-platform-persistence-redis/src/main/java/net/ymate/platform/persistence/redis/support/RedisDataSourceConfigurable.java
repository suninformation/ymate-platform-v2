/*
 * Copyright 2007-2018 the original author or authors.
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
package net.ymate.platform.persistence.redis.support;

import net.ymate.platform.persistence.redis.IRedis;
import net.ymate.platform.persistence.redis.IRedisModuleCfg;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2018-12-14 16:14
 * @version 1.0
 * @since 2.0.6
 */
public class RedisDataSourceConfigurable {

    public static RedisDataSourceConfigurable create(String name) {
        return new RedisDataSourceConfigurable(name);
    }

    private Map<String, String> __configs = new HashMap<String, String>();

    private Map<String, RedisServerConfigurable> __servers = new HashMap<String, RedisServerConfigurable>();

    private String name;

    public RedisDataSourceConfigurable(String name) {
        if (StringUtils.isBlank(name)) {
            throw new NullArgumentException("name");
        }
        this.name = name;
    }

    private void __putItem(String key, String value) {
        if (StringUtils.isBlank(key) || StringUtils.isBlank(value)) {
            return;
        }
        __configs.put("ds." + name + "." + key, value);
    }

    public RedisDataSourceConfigurable connectionType(IRedis.ConnectionType connectionType) {
        __putItem(IRedisModuleCfg.CONNECTION_TYPE, connectionType.name());
        return this;
    }

    public RedisDataSourceConfigurable masterServerName(String masterServerName) {
        __putItem(IRedisModuleCfg.MASTER_SERVER_NAME, masterServerName);
        return this;
    }

    public RedisDataSourceConfigurable poolMinIdle(int minIdle) {
        __putItem(IRedisModuleCfg.MIN_IDLE, String.valueOf(minIdle));
        return this;
    }

    public RedisDataSourceConfigurable poolMaxIdle(int maxIdle) {
        __putItem(IRedisModuleCfg.MAX_IDLE, String.valueOf(maxIdle));
        return this;
    }

    public RedisDataSourceConfigurable poolMaxTotal(int maxTotal) {
        __putItem(IRedisModuleCfg.MAX_IDLE, String.valueOf(maxTotal));
        return this;
    }

    public RedisDataSourceConfigurable poolBlockWhenExhausted(boolean blockWhenExhausted) {
        __putItem(IRedisModuleCfg.BLOCK_WHEN_EXHAUSTED, String.valueOf(blockWhenExhausted));
        return this;
    }

    public RedisDataSourceConfigurable poolFairness(String fairness) {
        __putItem(IRedisModuleCfg.FAIRNESS, String.valueOf(fairness));
        return this;
    }

    public RedisDataSourceConfigurable poolJmxEnabled(boolean jmxEnabled) {
        __putItem(IRedisModuleCfg.JMX_ENABLE, String.valueOf(jmxEnabled));
        return this;
    }

    public RedisDataSourceConfigurable poolJmxNameBase(String jmxNameBase) {
        __putItem(IRedisModuleCfg.JMX_NAME_BASE, jmxNameBase);
        return this;
    }

    public RedisDataSourceConfigurable poolJmxNamePrefix(String jmxNamePrefix) {
        __putItem(IRedisModuleCfg.JMX_NAME_PREFIX, jmxNamePrefix);
        return this;
    }

    public RedisDataSourceConfigurable poolEvictionPolicyClassName(String evictionPolicyClassName) {
        __putItem(IRedisModuleCfg.EVICTION_POLICY_CLASS_NAME, evictionPolicyClassName);
        return this;
    }

    public RedisDataSourceConfigurable poolLifo(boolean lifo) {
        __putItem(IRedisModuleCfg.LIFO, String.valueOf(lifo));
        return this;
    }

    public RedisDataSourceConfigurable poolMaxWaitMillis(long maxWaitMillis) {
        __putItem(IRedisModuleCfg.MAX_WAIT_MILLIS, String.valueOf(maxWaitMillis));
        return this;
    }

    public RedisDataSourceConfigurable poolMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
        __putItem(IRedisModuleCfg.MIN_EVICTABLE_IDLE_TIME_MILLIS, String.valueOf(minEvictableIdleTimeMillis));
        return this;
    }

    public RedisDataSourceConfigurable poolSoftMinEvictableIdleTimeMillis(long softMinEvictableIdleTimeMillis) {
        __putItem(IRedisModuleCfg.SOFT_MIN_EVICTABLE_IDLE_TIME_MILLIS, String.valueOf(softMinEvictableIdleTimeMillis));
        return this;
    }

    public RedisDataSourceConfigurable poolTestOnBorrow(boolean testOnBorrow) {
        __putItem(IRedisModuleCfg.TEST_ON_BORROW, String.valueOf(testOnBorrow));
        return this;
    }

    public RedisDataSourceConfigurable poolTestOnReturn(boolean testOnReturn) {
        __putItem(IRedisModuleCfg.TEST_ON_RETURN, String.valueOf(testOnReturn));
        return this;
    }

    public RedisDataSourceConfigurable poolTestOnCreate(boolean testOnCreate) {
        __putItem(IRedisModuleCfg.TEST_ON_CREATE, String.valueOf(testOnCreate));
        return this;
    }

    public RedisDataSourceConfigurable poolTestWhileIdle(boolean testWhileIdle) {
        __putItem(IRedisModuleCfg.TEST_WHILE_IDLE, String.valueOf(testWhileIdle));
        return this;
    }

    public RedisDataSourceConfigurable poolNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
        __putItem(IRedisModuleCfg.NUM_TESTS_PER_EVICTION_RUN, String.valueOf(numTestsPerEvictionRun));
        return this;
    }

    public RedisDataSourceConfigurable poolTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
        __putItem(IRedisModuleCfg.TIME_BETWEEN_EVICTION_RUNS_MILLIS, String.valueOf(timeBetweenEvictionRunsMillis));
        return this;
    }

    public RedisDataSourceConfigurable addServer(RedisServerConfigurable serverConfigurable) {
        __servers.put(serverConfigurable.getName(), serverConfigurable);
        return this;
    }

    public RedisDataSourceConfigurable addServers(Collection<RedisServerConfigurable> serverConfigurables) {
        for (RedisServerConfigurable _server : serverConfigurables) {
            __servers.put(_server.getName(), _server);
        }
        return this;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> toMap() {
        Map<String, String> _configs = new HashMap<String, String>(__configs);
        if (!__servers.isEmpty()) {
            _configs.put(IRedisModuleCfg.SERVER_NAME_LIST, StringUtils.join(__servers.keySet(), "|"));
            for (RedisServerConfigurable _server : __servers.values()) {
                Map<String, String> _serverCfg = _server.toMap();
                for (Map.Entry<String, String> _cfg : _serverCfg.entrySet()) {
                    _configs.put("ds." + name + "." + _cfg.getKey(), _cfg.getValue());
                }
            }
        }
        return _configs;
    }
}
