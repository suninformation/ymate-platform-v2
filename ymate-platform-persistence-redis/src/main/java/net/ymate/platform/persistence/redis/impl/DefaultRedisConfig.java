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

import net.ymate.platform.core.configuration.IConfigReader;
import net.ymate.platform.core.module.IModuleConfigurer;
import net.ymate.platform.core.persistence.AbstractPersistenceConfig;
import net.ymate.platform.persistence.redis.IRedis;
import net.ymate.platform.persistence.redis.IRedisConfig;
import net.ymate.platform.persistence.redis.IRedisDataSourceConfig;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-07-31 18:54
 * @since 2.1.0
 */
public final class DefaultRedisConfig extends AbstractPersistenceConfig<IRedis, IRedisDataSourceConfig> implements IRedisConfig {

    public static IRedisConfig defaultConfig() {
        return builder().build();
    }

    public static IRedisConfig create(IModuleConfigurer moduleConfigurer) throws Exception {
        return new DefaultRedisConfig(moduleConfigurer);
    }

    public static Builder builder() {
        return new Builder();
    }

    private DefaultRedisConfig() {
        super();
    }

    private DefaultRedisConfig(IModuleConfigurer moduleConfigurer) throws Exception {
        super(moduleConfigurer);
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
            if (dataSourceConfigs != null && dataSourceConfigs.length > 0) {
                for (IRedisDataSourceConfig dataSourceConfig : dataSourceConfigs) {
                    config.addDataSourceConfig(dataSourceConfig);
                }
            }
            return this;
        }

        public IRedisConfig build() {
            return config;
        }
    }
}
