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
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.configuration.IConfigReader;
import net.ymate.platform.core.persistence.AbstractDataSourceConfig;
import net.ymate.platform.core.persistence.base.Type;
import net.ymate.platform.persistence.jdbc.*;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-07-26 12:08
 * @since 2.1.0
 */
public final class DefaultDatabaseDataSourceConfig extends AbstractDataSourceConfig<IDatabase> implements IDatabaseDataSourceConfig {

    private String tablePrefix;

    private String identifierQuote;

    private Class<? extends IDatabaseDataSourceAdapter> adapterClass;

    private File configFile;

    private Type.DATABASE type;

    private String dialectClass;

    private String driverClass;

    private String connectionUrl;

    private boolean showSql;

    private boolean stackTraces;

    private int stackTraceDepth;

    private String stackTracePackage;

    public static IDatabaseDataSourceConfig create(String dataSourceName, IConfigReader configReader) throws ClassNotFoundException {
        return new DefaultDatabaseDataSourceConfig(dataSourceName, configReader);
    }

    public static Builder builder(String dataSourceName) {
        return new Builder(dataSourceName);
    }

    private DefaultDatabaseDataSourceConfig(String dataSourceName) {
        super(dataSourceName);
    }

    @SuppressWarnings("unchecked")
    private DefaultDatabaseDataSourceConfig(String dataSourceName, IConfigReader configReader) throws ClassNotFoundException {
        super(dataSourceName, configReader);
        // 验证必填参数
        if (StringUtils.isNotBlank(getUsername())) {
            this.connectionUrl = configReader.getString(IDatabaseConfig.CONNECTION_URL);
            if (StringUtils.isNotBlank(this.connectionUrl)) {
                // 基础参数
                this.showSql = configReader.getBoolean(IDatabaseConfig.SHOW_SQL);
                this.stackTraces = configReader.getBoolean(IDatabaseConfig.STACK_TRACES);
                this.stackTraceDepth = configReader.getInt(IDatabaseConfig.STACK_TRACE_DEPTH);
                this.stackTracePackage = configReader.getString(IDatabaseConfig.STACK_TRACE_PACKAGE);
                this.tablePrefix = configReader.getString(IDatabaseConfig.TABLE_PREFIX);
                this.identifierQuote = configReader.getString(IDatabaseConfig.IDENTIFIER_QUOTE);
                // 数据源适配器
                String adapterClassName = configReader.getString(IDatabaseConfig.ADAPTER_CLASS, IDatabaseConfig.DEFAULT_STR);
                adapterClassName = StringUtils.defaultIfBlank(JDBC.DS_ADAPTERS.get(adapterClassName), adapterClassName);
                this.adapterClass = (Class<? extends IDatabaseDataSourceAdapter>) ClassUtils.loadClass(adapterClassName, this.getClass());
                // 连接和数据库类型
                try {
                    this.type = Type.DATABASE.valueOf(StringUtils.trimToEmpty(configReader.getString(IDatabaseConfig.TYPE)).toUpperCase());
                } catch (IllegalArgumentException ignored) {
                }
                //
                String filePath = RuntimeUtils.replaceEnvVariable(configReader.getString(IDatabaseConfig.CONFIG_FILE));
                if (StringUtils.isNotBlank(filePath)) {
                    this.configFile = new File(filePath);
                }
                this.dialectClass = configReader.getString(IDatabaseConfig.DIALECT_CLASS);
                this.driverClass = configReader.getString(IDatabaseConfig.DRIVER_CLASS);
            }
        }
    }

    private void parseDatabaseType() {
        // 通过连接字符串分析数据库类型
        String[] connStrArr = StringUtils.split(this.connectionUrl, ":");
        if (connStrArr != null && connStrArr.length > 0) {
            if ("microsoft".equals(connStrArr[1])) {
                this.type = Type.DATABASE.SQLSERVER;
            } else {
                this.type = Type.DATABASE.valueOf(connStrArr[1].toUpperCase());
            }
        }
    }

    @Override
    protected void doInitialize(IDatabase iDatabase) throws Exception {
        if (StringUtils.isBlank(connectionUrl)) {
            throw new NullArgumentException("connectionUrl");
        }
        if (StringUtils.isNotBlank(getUsername())) {
            throw new NullArgumentException("username");
        }
        connectionUrl = RuntimeUtils.replaceEnvVariable(connectionUrl);
        if (type == null) {
            parseDatabaseType();
        }
        driverClass = StringUtils.defaultIfBlank(driverClass, JDBC.DB_DRIVERS.get(this.type));
        if (configFile == null || !configFile.isAbsolute() || !configFile.canRead() || !configFile.exists() || configFile.isDirectory()) {
            configFile = null;
        }
    }

    @Override
    public String getTablePrefix() {
        return tablePrefix;
    }

    public void setTablePrefix(String tablePrefix) {
        if (!isInitialized()) {
            this.tablePrefix = tablePrefix;
        }
    }

    @Override
    public String getIdentifierQuote() {
        return identifierQuote;
    }

    public void setIdentifierQuote(String identifierQuote) {
        if (!isInitialized()) {
            this.identifierQuote = identifierQuote;
        }
    }

    @Override
    public Class<? extends IDatabaseDataSourceAdapter> getAdapterClass() {
        return adapterClass;
    }

    public void setAdapterClass(Class<? extends IDatabaseDataSourceAdapter> adapterClass) {
        if (!isInitialized()) {
            this.adapterClass = adapterClass;
        }
    }

    @Override
    public File getConfigFile() {
        return configFile;
    }

    public void setConfigFile(File configFile) {
        this.configFile = configFile;
    }

    @Override
    public Type.DATABASE getType() {
        return type;
    }

    public void setType(Type.DATABASE type) {
        if (!isInitialized()) {
            this.type = type;
        }
    }

    @Override
    public String getDialectClass() {
        return dialectClass;
    }

    public void setDialectClass(String dialectClass) {
        if (!isInitialized()) {
            this.dialectClass = dialectClass;
        }
    }

    @Override
    public String getDriverClass() {
        return driverClass;
    }

    public void setDriverClass(String driverClass) {
        if (!isInitialized()) {
            this.driverClass = driverClass;
        }
    }

    @Override
    public String getConnectionUrl() {
        return connectionUrl;
    }

    public void setConnectionUrl(String connectionUrl) {
        if (!isInitialized()) {
            this.connectionUrl = connectionUrl;
        }
    }

    @Override
    public boolean isShowSql() {
        return showSql;
    }

    public void setShowSql(boolean showSql) {
        if (!isInitialized()) {
            this.showSql = showSql;
        }
    }

    @Override
    public boolean isStackTraces() {
        return stackTraces;
    }

    public void setStackTraces(boolean stackTraces) {
        if (!isInitialized()) {
            this.stackTraces = stackTraces;
        }
    }

    @Override
    public int getStackTraceDepth() {
        return stackTraceDepth;
    }

    public void setStackTraceDepth(int stackTraceDepth) {
        if (!isInitialized()) {
            this.stackTraceDepth = stackTraceDepth;
        }
    }

    @Override
    public String getStackTracePackage() {
        return stackTracePackage;
    }

    public void setStackTracePackage(String stackTracePackage) {
        if (!isInitialized()) {
            this.stackTracePackage = stackTracePackage;
        }
    }

    public static final class Builder {

        private final DefaultDatabaseDataSourceConfig config;

        private Builder(String dataSourceName) {
            config = new DefaultDatabaseDataSourceConfig(dataSourceName);
        }

        public Builder tablePrefix(String tablePrefix) {
            config.setTablePrefix(tablePrefix);
            return this;
        }

        public Builder identifierQuote(String identifierQuote) {
            config.setIdentifierQuote(identifierQuote);
            return this;
        }

        public Builder adapterClass(Class<? extends IDatabaseDataSourceAdapter> adapterClass) {
            config.setAdapterClass(adapterClass);
            return this;
        }

        public Builder configFile(File configFile) {
            config.setConfigFile(configFile);
            return this;
        }

        public Builder type(Type.DATABASE type) {
            config.setType(type);
            return this;
        }

        public Builder dialectClass(String dialectClass) {
            config.setDialectClass(dialectClass);
            return this;
        }

        public Builder driverClass(String driverClass) {
            config.setDriverClass(driverClass);
            return this;
        }

        public Builder connectionUrl(String connectionUrl) {
            config.setConnectionUrl(connectionUrl);
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

        public Builder showSql(boolean showSql) {
            config.setShowSql(showSql);
            return this;
        }

        public Builder stackTraces(boolean stackTraces) {
            config.setStackTraces(stackTraces);
            return this;
        }

        public Builder stackTraceDepth(int stackTraceDepth) {
            config.setStackTraceDepth(stackTraceDepth);
            return this;
        }

        public Builder stackTracePackage(String stackTracePackage) {
            config.setStackTracePackage(stackTracePackage);
            return this;
        }

        public Builder passwordClass(Class<? extends IPasswordProcessor> passwordClass) {
            config.setPasswordClass(passwordClass);
            return this;
        }

        public IDatabaseDataSourceConfig build() {
            return config;
        }
    }
}
