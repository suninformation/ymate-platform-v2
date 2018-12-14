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
package net.ymate.platform.persistence.redis.impl;

import net.ymate.platform.core.IConfig;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.support.IConfigReader;
import net.ymate.platform.core.support.IPasswordProcessor;
import net.ymate.platform.core.support.impl.MapSafeConfigReader;
import net.ymate.platform.persistence.redis.IRedis;
import net.ymate.platform.persistence.redis.IRedisModuleCfg;
import net.ymate.platform.persistence.redis.RedisDataSourceCfgMeta;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.util.*;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/30 上午3:23
 * @version 1.0
 */
public class DefaultRedisModuleCfg implements IRedisModuleCfg {

    private YMP __owner;

    private String dataSourceDefaultName;

    private Map<String, RedisDataSourceCfgMeta> dataSourceCfgMetas;

    public DefaultRedisModuleCfg(YMP owner) throws Exception {
        __owner = owner;
        //
        IConfigReader _moduleCfg = MapSafeConfigReader.bind(owner.getConfig().getModuleConfigs(IRedis.MODULE_NAME));
        //
        this.dataSourceDefaultName = _moduleCfg.getString(DS_DEFAULT_NAME, IConfig.DEFAULT_STR);
        //
        this.dataSourceCfgMetas = new HashMap<String, RedisDataSourceCfgMeta>();
        String _dsNameStr = _moduleCfg.getString(DS_NAME_LIST, IConfig.DEFAULT_STR);
        if (StringUtils.contains(_dsNameStr, this.dataSourceDefaultName)) {
            String[] _dsNameList = StringUtils.split(_dsNameStr, "|");
            for (String _dsName : _dsNameList) {
                RedisDataSourceCfgMeta _meta = __doParserDataSourceCfgMeta(_dsName, _moduleCfg.getMap("ds." + _dsName + "."));
                this.dataSourceCfgMetas.put(_dsName, _meta);
            }
        } else {
            throw new IllegalArgumentException("The default datasource name does not match");
        }
    }

    private RedisDataSourceCfgMeta __doParserDataSourceCfgMeta(String dsName, Map<String, String> dataSourceCfgs) throws Exception {
        IConfigReader _dataSourceCfg = MapSafeConfigReader.bind(dataSourceCfgs);
        //
        IRedis.ConnectionType _connectionType;
        try {
            _connectionType = IRedis.ConnectionType.valueOf(_dataSourceCfg.getString(CONNECTION_TYPE, IConfig.DEFAULT_STR).toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new UnsupportedOperationException("Redis connection type unsupported.");
        }
        String _masterServerName = _dataSourceCfg.getString(MASTER_SERVER_NAME, IConfig.DEFAULT_STR);
        List<ServerMeta> _servers = new ArrayList<ServerMeta>();
        String[] _serverNames = StringUtils.split(_dataSourceCfg.getString(SERVER_NAME_LIST, IConfig.DEFAULT_STR), "|");
        if (_serverNames != null) {
            for (String _serverName : _serverNames) {
                IConfigReader _serverCfg = MapSafeConfigReader.bind(_dataSourceCfg.getMap("server." + _serverName + "."));
                if (!_serverCfg.toMap().isEmpty()) {
                    ServerMeta _servMeta = new ServerMeta();
                    _servMeta.setName(_serverName);
                    _servMeta.setHost(_serverCfg.getString(HOST, "localhost"));
                    _servMeta.setPort(_serverCfg.getInt(PORT, 6379));
                    _servMeta.setTimeout(_serverCfg.getInt(TIMEOUT, 2000));
                    _servMeta.setSocketTimeout(_serverCfg.getInt(SOCKET_TIMEOUT, 2000));
                    _servMeta.setMaxAttempts(_serverCfg.getInt(MAX_ATTEMPTS, 3));
                    _servMeta.setWeight(_serverCfg.getInt(WEIGHT, 1));
                    _servMeta.setDatabase(_serverCfg.getInt(DATABASE, 0));
                    _servMeta.setClientName(_serverCfg.getString(CLIENT_NAME));
                    _servMeta.setPassword(_serverCfg.getString(PASSWORD));
                    //
                    boolean _isPwdEncrypted = _dataSourceCfg.getBoolean(PASSWORD_ENCRYPTED);
                    //
                    if (_isPwdEncrypted && StringUtils.isNotBlank(_servMeta.getPassword())) {
                        IPasswordProcessor _proc = _serverCfg.getClassImpl(PASSWORD_CLASS, IPasswordProcessor.class);
                        if (_proc == null) {
                            _proc = __owner.getConfig().getDefaultPasswordClass().newInstance();
                        }
                        if (_proc != null) {
                            _servMeta.setPassword(_proc.decrypt(_servMeta.getPassword()));
                        }
                    }
                    //
                    _servers.add(_servMeta);
                }
            }
        }
        //
        GenericObjectPoolConfig _poolConfig = new GenericObjectPoolConfig();
        IConfigReader _poolCfg = MapSafeConfigReader.bind(_dataSourceCfg.getMap("pool."));
        if (!_poolCfg.toMap().isEmpty()) {
            _poolConfig.setMinIdle(_poolCfg.getInt(MIN_IDLE, GenericObjectPoolConfig.DEFAULT_MIN_IDLE));
            _poolConfig.setMaxIdle(_poolCfg.getInt(MAX_IDLE, GenericObjectPoolConfig.DEFAULT_MAX_IDLE));
            _poolConfig.setMaxTotal(_poolCfg.getInt(MAX_TOTAL, GenericObjectPoolConfig.DEFAULT_MAX_TOTAL));
            _poolConfig.setBlockWhenExhausted(_poolCfg.getBoolean(BLOCK_WHEN_EXHAUSTED, GenericObjectPoolConfig.DEFAULT_BLOCK_WHEN_EXHAUSTED));
            _poolConfig.setFairness(_poolCfg.getBoolean(FAIRNESS, GenericObjectPoolConfig.DEFAULT_FAIRNESS));
            _poolConfig.setJmxEnabled(_poolCfg.getBoolean(JMX_ENABLE, GenericObjectPoolConfig.DEFAULT_JMX_ENABLE));
            _poolConfig.setJmxNameBase(_poolCfg.getString(JMX_NAME_BASE, GenericObjectPoolConfig.DEFAULT_JMX_NAME_BASE));
            _poolConfig.setJmxNamePrefix(_poolCfg.getString(JMX_NAME_PREFIX, GenericObjectPoolConfig.DEFAULT_JMX_NAME_PREFIX));
            _poolConfig.setEvictionPolicyClassName(_poolCfg.getString(EVICTION_POLICY_CLASS_NAME, GenericObjectPoolConfig.DEFAULT_EVICTION_POLICY_CLASS_NAME));
            _poolConfig.setLifo(_poolCfg.getBoolean(LIFO, GenericObjectPoolConfig.DEFAULT_LIFO));
            _poolConfig.setMaxWaitMillis(_poolCfg.getLong(MAX_WAIT_MILLIS, GenericObjectPoolConfig.DEFAULT_MAX_WAIT_MILLIS));
            _poolConfig.setMinEvictableIdleTimeMillis(_poolCfg.getLong(MIN_EVICTABLE_IDLE_TIME_MILLIS, GenericObjectPoolConfig.DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS));
            _poolConfig.setSoftMinEvictableIdleTimeMillis(_poolCfg.getLong(SOFT_MIN_EVICTABLE_IDLE_TIME_MILLIS, GenericObjectPoolConfig.DEFAULT_SOFT_MIN_EVICTABLE_IDLE_TIME_MILLIS));
            _poolConfig.setTestOnBorrow(_poolCfg.getBoolean(TEST_ON_BORROW, GenericObjectPoolConfig.DEFAULT_TEST_ON_BORROW));
            _poolConfig.setTestOnReturn(_poolCfg.getBoolean(TEST_ON_RETURN, GenericObjectPoolConfig.DEFAULT_TEST_ON_RETURN));
            _poolConfig.setTestOnCreate(_poolCfg.getBoolean(TEST_ON_CREATE, GenericObjectPoolConfig.DEFAULT_TEST_ON_CREATE));
            _poolConfig.setTestWhileIdle(_poolCfg.getBoolean(TEST_WHILE_IDLE, GenericObjectPoolConfig.DEFAULT_TEST_WHILE_IDLE));
            _poolConfig.setNumTestsPerEvictionRun(_poolCfg.getInt(NUM_TESTS_PER_EVICTION_RUN, GenericObjectPoolConfig.DEFAULT_NUM_TESTS_PER_EVICTION_RUN));
            _poolConfig.setTimeBetweenEvictionRunsMillis(_poolCfg.getLong(TIME_BETWEEN_EVICTION_RUNS_MILLIS, GenericObjectPoolConfig.DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS));
        }
        return new RedisDataSourceCfgMeta(dsName, _connectionType, _masterServerName, _servers, _poolConfig);
    }

    @Override
    public String getDataSourceDefaultName() {
        return dataSourceDefaultName;
    }

    @Override
    public Map<String, RedisDataSourceCfgMeta> getDataSourceCfgs() {
        return Collections.unmodifiableMap(dataSourceCfgMetas);
    }

    @Override
    public RedisDataSourceCfgMeta getDefaultDataSourceCfg() {
        return dataSourceCfgMetas.get(dataSourceDefaultName);
    }

    @Override
    public RedisDataSourceCfgMeta getDataSourceCfg(String name) {
        return dataSourceCfgMetas.get(name);
    }
}
