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

import net.ymate.platform.commons.IPasswordProcessor;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.configuration.IConfigReader;
import net.ymate.platform.core.module.IModuleConfigurer;
import net.ymate.platform.core.persistence.AbstractPersistenceConfig;
import net.ymate.platform.core.persistence.base.Type;
import net.ymate.platform.persistence.jdbc.IDatabase;
import net.ymate.platform.persistence.jdbc.IDatabaseConfig;
import net.ymate.platform.persistence.jdbc.IDatabaseDataSourceAdapter;
import net.ymate.platform.persistence.jdbc.IDatabaseDataSourceConfig;
import net.ymate.platform.persistence.jdbc.annotation.DatabaseConf;
import net.ymate.platform.persistence.jdbc.annotation.DatabaseDataSource;
import net.ymate.platform.persistence.jdbc.dialect.IDialect;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 默认数据库JDBC持久化模块配置类
 *
 * @author 刘镇 (suninformation@163.com) on 2012-12-29 下午2:19:18
 */
public final class DefaultDatabaseConfig extends AbstractPersistenceConfig<IDatabase, IDatabaseDataSourceConfig> implements IDatabaseConfig {

    public static DefaultDatabaseConfig defaultConfig() {
        return builder().build();
    }

    public static DefaultDatabaseConfig create(IModuleConfigurer moduleConfigurer) throws Exception {
        return new DefaultDatabaseConfig(null, moduleConfigurer);
    }

    public static DefaultDatabaseConfig create(Class<?> mainClass, IModuleConfigurer moduleConfigurer) throws Exception {
        return new DefaultDatabaseConfig(mainClass, moduleConfigurer);
    }

    public static Builder builder() {
        return new Builder();
    }

    private DefaultDatabaseConfig() {
        super();
    }

    private DefaultDatabaseConfig(Class<?> mainClass, IModuleConfigurer moduleConfigurer) throws Exception {
        super(mainClass, moduleConfigurer);
    }

    @Override
    protected void afterDataSourceConfigs(Class<?> mainClass, Map<String, IDatabaseDataSourceConfig> dataSourceConfigs) {
        if (mainClass != null && dataSourceConfigs.isEmpty()) {
            DatabaseConf databaseConf = mainClass.getAnnotation(DatabaseConf.class);
            setDataSourceDefaultName(StringUtils.defaultIfBlank(databaseConf != null ? databaseConf.dsDefaultName() : null, DEFAULT_STR));
            Map<String, DatabaseDataSource> dataSourceMap = new HashMap<>(16);
            if (databaseConf != null) {
                for (DatabaseDataSource dataSource : databaseConf.value()) {
                    if (StringUtils.isNotBlank(dataSource.name())) {
                        dataSourceMap.put(dataSource.name(), dataSource);
                    }
                }
            } else {
                DatabaseDataSource dataSource = mainClass.getAnnotation(DatabaseDataSource.class);
                if (dataSource != null && StringUtils.isNotBlank(dataSource.name())) {
                    dataSourceMap.put(dataSource.name(), dataSource);
                }
            }
            if (!dataSourceMap.isEmpty()) {
                for (DatabaseDataSource dataSource : dataSourceMap.values()) {
                    DefaultDatabaseDataSourceConfig.Builder builder = DefaultDatabaseDataSourceConfig.builder(dataSource.name())
                            .connectionUrl(dataSource.connectionUrl())
                            .username(dataSource.username())
                            .password(dataSource.password())
                            .passwordEncrypted(dataSource.passwordEncrypted())
                            .passwordClass(dataSource.passwordClass().equals(IPasswordProcessor.class) ? null : dataSource.passwordClass())
                            .type(StringUtils.defaultIfBlank(dataSource.type(), Type.DATABASE.UNKNOWN).toUpperCase())
                            .dialectClass(dataSource.dialectClass().equals(IDialect.class) ? null : dataSource.dialectClass().getName())
                            .adapterClass(dataSource.adapterClass().equals(IDatabaseDataSourceAdapter.class) ? DefaultDataSourceAdapter.class : dataSource.adapterClass())
                            .driverClass(dataSource.driverClass())
                            .showSql(dataSource.showSql())
                            .stackTraces(dataSource.stackTraces())
                            .stackTraceDepth(dataSource.stackTraceDepth())
                            .stackTracePackages(StringUtils.join(dataSource.stackTracePackages(), '|'))
                            .tablePrefix(dataSource.tablePrefix())
                            .identifierQuote(dataSource.identifierQuote());
                    String filePath = RuntimeUtils.replaceEnvVariable(dataSource.configFile());
                    if (StringUtils.isNotBlank(filePath)) {
                        builder.configFile(new File(filePath));
                    }
                    dataSourceConfigs.put(dataSource.name(), builder.build());
                }
            }
        }
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

        public DefaultDatabaseConfig build() {
            return config;
        }
    }
}
