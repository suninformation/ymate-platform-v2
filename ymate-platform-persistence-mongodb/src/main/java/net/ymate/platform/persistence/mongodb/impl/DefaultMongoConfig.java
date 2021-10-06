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

import com.mongodb.ServerAddress;
import net.ymate.platform.commons.IPasswordProcessor;
import net.ymate.platform.core.configuration.IConfigReader;
import net.ymate.platform.core.module.IModuleConfigurer;
import net.ymate.platform.core.persistence.AbstractPersistenceConfig;
import net.ymate.platform.persistence.mongodb.IMongo;
import net.ymate.platform.persistence.mongodb.IMongoClientOptionsHandler;
import net.ymate.platform.persistence.mongodb.IMongoConfig;
import net.ymate.platform.persistence.mongodb.IMongoDataSourceConfig;
import net.ymate.platform.persistence.mongodb.annotation.MongoConf;
import net.ymate.platform.persistence.mongodb.annotation.MongoDataSource;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/22 上午12:42
 */
public final class DefaultMongoConfig extends AbstractPersistenceConfig<IMongo, IMongoDataSourceConfig> implements IMongoConfig {

    public static DefaultMongoConfig defaultConfig() {
        return builder().build();
    }

    public static DefaultMongoConfig create(IModuleConfigurer moduleConfigurer) throws Exception {
        return new DefaultMongoConfig(null, moduleConfigurer);
    }

    public static DefaultMongoConfig create(Class<?> mainClass, IModuleConfigurer moduleConfigurer) throws Exception {
        return new DefaultMongoConfig(mainClass, moduleConfigurer);
    }

    public static Builder builder() {
        return new Builder();
    }

    private DefaultMongoConfig() {
        super();
    }

    private DefaultMongoConfig(Class<?> mainClass, IModuleConfigurer moduleConfigurer) throws Exception {
        super(mainClass, moduleConfigurer);
    }

    @Override
    protected void afterDataSourceConfigs(Class<?> mainClass, Map<String, IMongoDataSourceConfig> dataSourceConfigs) {
        if (mainClass != null && dataSourceConfigs.isEmpty()) {
            MongoConf mongoConf = mainClass.getAnnotation(MongoConf.class);
            setDataSourceDefaultName(StringUtils.defaultIfBlank(mongoConf != null ? mongoConf.dsDefaultName() : null, DEFAULT_STR));
            Map<String, MongoDataSource> dataSourceMap = new HashMap<>(16);
            if (mongoConf != null) {
                for (MongoDataSource dataSource : mongoConf.value()) {
                    if (StringUtils.isNotBlank(dataSource.name())) {
                        dataSourceMap.put(dataSource.name(), dataSource);
                    }
                }
            } else {
                MongoDataSource dataSource = mainClass.getAnnotation(MongoDataSource.class);
                if (dataSource != null && StringUtils.isNotBlank(dataSource.name())) {
                    dataSourceMap.put(dataSource.name(), dataSource);
                }
            }
            if (!dataSourceMap.isEmpty()) {
                for (MongoDataSource dataSource : dataSourceMap.values()) {
                    DefaultMongoDataSourceConfig.Builder builder = DefaultMongoDataSourceConfig.builder(dataSource.name())
                            .username(StringUtils.trimToNull(dataSource.username()))
                            .password(StringUtils.trimToNull(dataSource.password()))
                            .passwordEncrypted(dataSource.passwordEncrypted())
                            .passwordClass(dataSource.passwordClass().equals(IPasswordProcessor.class) ? null : dataSource.passwordClass())
                            .collectionPrefix(StringUtils.trimToNull(dataSource.collectionPrefix()))
                            .databaseName(StringUtils.trimToNull(dataSource.databaseName()))
                            .clientOptionsHandlerClass(dataSource.optionsHandlerClass().equals(IMongoClientOptionsHandler.class) ? null : dataSource.optionsHandlerClass());
                    if (StringUtils.isNotBlank(dataSource.connectionUrl())) {
                        builder.connectionUrl(dataSource.connectionUrl());
                    } else if (dataSource.servers().length > 0) {
                        Arrays.stream(dataSource.servers()).map(serverStr -> StringUtils.split(serverStr, ":"))
                                .forEachOrdered(server -> builder.addServerAddresses(server.length > 1 ? new ServerAddress(server[0], Integer.parseInt(server[1])) : new ServerAddress(server[0])));
                    }
                    dataSourceConfigs.put(dataSource.name(), builder.build());
                }
            }
        }
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

        public DefaultMongoConfig build() {
            return config;
        }
    }
}
