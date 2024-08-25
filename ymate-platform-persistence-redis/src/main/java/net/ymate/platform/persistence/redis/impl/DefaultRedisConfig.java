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
import net.ymate.platform.core.module.IModuleConfigurer;
import net.ymate.platform.core.persistence.AbstractPersistenceConfig;
import net.ymate.platform.persistence.redis.IRedis;
import net.ymate.platform.persistence.redis.IRedisConfig;
import net.ymate.platform.persistence.redis.IRedisDataSourceConfig;
import net.ymate.platform.persistence.redis.RedisServerMeta;
import net.ymate.platform.persistence.redis.annotation.RedisConf;
import net.ymate.platform.persistence.redis.annotation.RedisDataSource;
import net.ymate.platform.persistence.redis.annotation.RedisServer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Protocol;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-07-31 18:54
 * @since 2.1.0
 */
public final class DefaultRedisConfig extends AbstractPersistenceConfig<IRedis, IRedisDataSourceConfig> implements IRedisConfig {

    public static DefaultRedisConfig defaultConfig() {
        return builder().build();
    }

    public static DefaultRedisConfig create(IModuleConfigurer moduleConfigurer) throws Exception {
        return new DefaultRedisConfig(null, moduleConfigurer);
    }

    public static DefaultRedisConfig create(Class<?> mainClass, IModuleConfigurer moduleConfigurer) throws Exception {
        return new DefaultRedisConfig(mainClass, moduleConfigurer);
    }

    public static Builder builder() {
        return new Builder();
    }

    private DefaultRedisConfig() {
        super();
    }

    private DefaultRedisConfig(Class<?> mainClass, IModuleConfigurer moduleConfigurer) throws Exception {
        super(mainClass, moduleConfigurer);
    }

    @Override
    protected void afterDataSourceConfigs(Class<?> mainClass, Map<String, IRedisDataSourceConfig> dataSourceConfigs) {
        if (mainClass != null && dataSourceConfigs.isEmpty()) {
            RedisConf redisConf = mainClass.getAnnotation(RedisConf.class);
            setDataSourceDefaultName(StringUtils.defaultIfBlank(redisConf != null ? redisConf.dsDefaultName() : null, DEFAULT_STR));
            Map<String, RedisDataSource> dataSourceMap = new HashMap<>(16);
            if (redisConf != null) {
                for (RedisDataSource dataSource : redisConf.value()) {
                    if (StringUtils.isNotBlank(dataSource.name())) {
                        dataSourceMap.put(dataSource.name(), dataSource);
                    }
                }
            } else {
                RedisDataSource dataSource = mainClass.getAnnotation(RedisDataSource.class);
                if (dataSource != null && StringUtils.isNotBlank(dataSource.name())) {
                    dataSourceMap.put(dataSource.name(), dataSource);
                }
            }
            if (!dataSourceMap.isEmpty()) {
                for (RedisDataSource dataSource : dataSourceMap.values()) {
                    DefaultRedisDataSourceConfig.Builder builder = DefaultRedisDataSourceConfig.builder(dataSource.name())
                            .autoConnection(dataSource.autoConnection())
                            .connectionType(dataSource.connectionType())
                            .masterServerName(StringUtils.defaultIfBlank(dataSource.masterServerName(), IRedisConfig.DEFAULT_STR))
                            .passwordEncrypted(dataSource.passwordEncrypted())
                            .passwordClass(dataSource.passwordClass().equals(IPasswordProcessor.class) ? null : dataSource.passwordClass())
                            .poolMinIdle(dataSource.poolMinIdle())
                            .poolMaxIdle(dataSource.poolMaxIdle())
                            .poolMaxTotal(dataSource.poolMaxTotal())
                            .poolBlockWhenExhausted(dataSource.poolBlockWhenExhausted())
                            .poolFairness(dataSource.poolFairness())
                            .poolJmxEnabled(dataSource.poolJmxEnabled())
                            .poolJmxNameBase(StringUtils.trimToNull(dataSource.poolJmxNameBase()))
                            .poolJmxNamePrefix(StringUtils.trimToNull(dataSource.poolJmxNamePrefix()))
                            .poolEvictionPolicyClassName(StringUtils.defaultIfBlank(dataSource.poolEvictionPolicyClassName(), GenericObjectPoolConfig.DEFAULT_EVICTION_POLICY_CLASS_NAME))
                            .poolLifo(dataSource.poolLifo())
                            .poolMaxWaitMillis(dataSource.poolMaxWaitMillis())
                            .poolMinEvictableIdleTimeMillis(dataSource.poolMinEvictableIdleTimeMillis())
                            .poolSoftMinEvictableIdleTimeMillis(dataSource.poolSoftMinEvictableIdleTimeMillis())
                            .poolTestOnBorrow(dataSource.poolTestOnBorrow())
                            .poolTestOnReturn(dataSource.poolTestOnReturn())
                            .poolTestOnCreate(dataSource.poolTestOnCreate())
                            .poolTestWhileIdle(dataSource.poolTestWhileIdle())
                            .poolNumTestsPerEvictionRun(dataSource.poolNumTestsPerEvictionRun())
                            .poolTimeBetweenEvictionRunsMillis(dataSource.poolTimeBetweenEvictionRunsMillis());
                    for (RedisServer server : dataSource.servers()) {
                        builder.addServerMetas(RedisServerMeta.builder(server.name())
                                .host(StringUtils.defaultIfBlank(server.host(), Protocol.DEFAULT_HOST))
                                .port(server.port() > 0 ? server.port() : Protocol.DEFAULT_PORT)
                                .timeout(server.timeout() > 0 ? server.timeout() : Protocol.DEFAULT_TIMEOUT)
                                .socketTimeout(server.socketTimeout() > 0 ? server.socketTimeout() : Protocol.DEFAULT_TIMEOUT)
                                .maxAttempts(server.maxAttempts() > 0 ? server.maxAttempts() : 3)
                                .weight(server.weight() > 0 ? server.weight() : 1)
                                .database(server.database() >= 0 ? server.database() : Protocol.DEFAULT_DATABASE)
                                .clientName(StringUtils.trimToNull(server.clientName()))
                                .password(StringUtils.trimToNull(server.password())).build());
                    }
                    dataSourceConfigs.put(dataSource.name(), builder.build());
                }
            }
        }
    }

    @Override
    protected IRedisDataSourceConfig buildDataSourceConfig(String dataSourceName, IConfigReader configReader) throws Exception {
        return DefaultRedisDataSourceConfig.create(dataSourceName, configReader);
    }

    public static final class Builder {

        private final DefaultRedisConfig config = new DefaultRedisConfig();

        private Builder() {
        }

        public Builder dataSourceDefaultName(String dataSourceDefaultName) {
            config.setDataSourceDefaultName(dataSourceDefaultName);
            return this;
        }

        public Builder addDataSourceConfigs(IRedisDataSourceConfig... dataSourceConfigs) {
            if (dataSourceConfigs != null) {
                for (IRedisDataSourceConfig dataSourceConfig : dataSourceConfigs) {
                    config.addDataSourceConfig(dataSourceConfig);
                }
            }
            return this;
        }

        public DefaultRedisConfig build() {
            return config;
        }
    }
}
