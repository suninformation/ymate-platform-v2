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
package net.ymate.platform.persistence.jdbc.impl;

import net.ymate.platform.core.configuration.IConfigReader;
import net.ymate.platform.core.module.IModuleConfigurer;
import net.ymate.platform.core.persistence.AbstractPersistenceConfig;
import net.ymate.platform.persistence.jdbc.IDatabase;
import net.ymate.platform.persistence.jdbc.IDatabaseConfig;
import net.ymate.platform.persistence.jdbc.IDatabaseDataSourceConfig;

/**
 * 默认数据库JDBC持久化模块配置类
 *
 * @author 刘镇 (suninformation@163.com) on 2012-12-29 下午2:19:18
 */
public final class DefaultDatabaseConfig extends AbstractPersistenceConfig<IDatabase, IDatabaseDataSourceConfig> implements IDatabaseConfig {

    public static IDatabaseConfig defaultConfig() {
        return builder().build();
    }

    public static IDatabaseConfig create(IModuleConfigurer moduleConfigurer) throws Exception {
        return new DefaultDatabaseConfig(moduleConfigurer);
    }

    public static Builder builder() {
        return new Builder();
    }

    private DefaultDatabaseConfig() {
        super();
    }

    private DefaultDatabaseConfig(IModuleConfigurer moduleConfigurer) throws Exception {
        super(moduleConfigurer);
    }

    @Override
    protected IDatabaseDataSourceConfig buildDataSourceConfig(String dataSourceName, IConfigReader configReader) throws Exception {
        return DefaultDatabaseDataSourceConfig.create(dataSourceName, configReader);
    }

    public static final class Builder {

        private final DefaultDatabaseConfig config = new DefaultDatabaseConfig();

        private Builder() {
        }

        public Builder dataSourceDefaultName(String dataSourceDefaultName) {
            config.setDataSourceDefaultName(dataSourceDefaultName);
            return this;
        }

        public Builder addDataSourceConfigs(IDatabaseDataSourceConfig... dataSourceConfigs) {
            if (dataSourceConfigs != null && dataSourceConfigs.length > 0) {
                for (IDatabaseDataSourceConfig dataSourceConfig : dataSourceConfigs) {
                    config.addDataSourceConfig(dataSourceConfig);
                }
            }
            return this;
        }

        public IDatabaseConfig build() {
            return config;
        }
    }
}
