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
package net.ymate.platform.persistence.mongodb.impl;

import net.ymate.platform.core.configuration.IConfigReader;
import net.ymate.platform.core.module.IModuleConfigurer;
import net.ymate.platform.core.persistence.AbstractPersistenceConfig;
import net.ymate.platform.persistence.mongodb.IMongo;
import net.ymate.platform.persistence.mongodb.IMongoConfig;
import net.ymate.platform.persistence.mongodb.IMongoDataSourceConfig;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/22 上午12:42
 */
public final class DefaultMongoConfig extends AbstractPersistenceConfig<IMongo, IMongoDataSourceConfig> implements IMongoConfig {

    public static IMongoConfig defaultConfig() {
        return builder().build();
    }

    public static IMongoConfig create(IModuleConfigurer moduleConfigurer) throws Exception {
        return new DefaultMongoConfig(moduleConfigurer);
    }

    public static Builder builder() {
        return new Builder();
    }

    private DefaultMongoConfig() {
        super();
    }

    private DefaultMongoConfig(IModuleConfigurer moduleConfigurer) throws Exception {
        super(moduleConfigurer);
    }

    @Override
    protected IMongoDataSourceConfig buildDataSourceConfig(String dataSourceName, IConfigReader configReader) throws Exception {
        return DefaultMongoDataSourceConfig.create(dataSourceName, configReader);
    }

    public static final class Builder {

        private final DefaultMongoConfig config = new DefaultMongoConfig();

        private Builder() {
        }

        public Builder dataSourceDefaultName(String dataSourceDefaultName) {
            config.setDataSourceDefaultName(dataSourceDefaultName);
            return this;
        }

        public Builder addDataSourceConfigs(IMongoDataSourceConfig... dataSourceConfigs) {
            if (dataSourceConfigs != null && dataSourceConfigs.length > 0) {
                for (IMongoDataSourceConfig dataSourceConfig : dataSourceConfigs) {
                    config.addDataSourceConfig(dataSourceConfig);
                }
            }
            return this;
        }

        public IMongoConfig build() {
            return config;
        }
    }
}
