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
import net.ymate.platform.core.persistence.AbstractDataSourceConfigurable;
import net.ymate.platform.core.persistence.IDataSourceAdapter;
import net.ymate.platform.core.persistence.IDataSourceConfigurable;
import net.ymate.platform.core.persistence.base.Type;
import net.ymate.platform.persistence.jdbc.IDatabaseConfig;
import net.ymate.platform.persistence.jdbc.IDatabaseDataSourceConfigurable;
import net.ymate.platform.persistence.jdbc.dialect.IDialect;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-08-15 03:40
 * @since 2.1.0
 */
public final class DefaultDatabaseDataSourceConfigurable extends AbstractDataSourceConfigurable implements IDatabaseDataSourceConfigurable {

    public static Builder builder(String dataSourceName) {
        return new Builder(dataSourceName);
    }

    private DefaultDatabaseDataSourceConfigurable(String dataSourceName) {
        super(dataSourceName);
    }

    public static final class Builder {

        private final DefaultDatabaseDataSourceConfigurable configurable;

        private Builder(String dataSourceName) {
            configurable = new DefaultDatabaseDataSourceConfigurable(dataSourceName);
        }

        public Builder connectionUrl(String connectionUrl) {
            configurable.addConfig(IDatabaseConfig.CONNECTION_URL, connectionUrl);
            return this;
        }

        public Builder username(String username) {
            configurable.addConfig(IDatabaseConfig.USERNAME, username);
            return this;
        }

        public Builder password(String password) {
            configurable.addConfig(IDatabaseConfig.PASSWORD, password);
            return this;
        }

        public Builder passwordEncrypted(boolean passwordEncrypted) {
            configurable.addConfig(IDatabaseConfig.PASSWORD_ENCRYPTED, String.valueOf(passwordEncrypted));
            return this;
        }

        public Builder passwordClass(Class<? extends IPasswordProcessor> passwordClass) {
            configurable.addConfig(IDatabaseConfig.PASSWORD_CLASS, passwordClass.getName());
            return this;
        }

        public Builder showSql(boolean showSql) {
            configurable.addConfig(IDatabaseConfig.SHOW_SQL, String.valueOf(showSql));
            return this;
        }

        public Builder stackTraces(boolean stackTraces) {
            configurable.addConfig(IDatabaseConfig.STACK_TRACES, String.valueOf(stackTraces));
            return this;
        }

        public Builder stackTraceDepth(int stackTraceDepth) {
            configurable.addConfig(IDatabaseConfig.STACK_TRACE_DEPTH, String.valueOf(stackTraceDepth));
            return this;
        }

        public Builder stackTracePackage(String stackTracePackage) {
            configurable.addConfig(IDatabaseConfig.STACK_TRACE_PACKAGE, stackTracePackage);
            return this;
        }

        public Builder tablePrefix(String tablePrefix) {
            configurable.addConfig(IDatabaseConfig.TABLE_PREFIX, tablePrefix);
            return this;
        }

        public Builder identifierQuote(String identifierQuote) {
            configurable.addConfig(IDatabaseConfig.IDENTIFIER_QUOTE, identifierQuote);
            return this;
        }

        public Builder adapterClass(String adapterClass) {
            configurable.addConfig(IDatabaseConfig.ADAPTER_CLASS, adapterClass);
            return this;
        }

        public Builder adapterClass(Class<? extends IDataSourceAdapter> adapterClass) {
            configurable.addConfig(IDatabaseConfig.ADAPTER_CLASS, adapterClass.getName());
            return this;
        }

        public Builder configFile(String configFile) {
            configurable.addConfig(IDatabaseConfig.CONFIG_FILE, configFile);
            return this;
        }

        public Builder type(Type.DATABASE type) {
            configurable.addConfig(IDatabaseConfig.TYPE, type.name());
            return this;
        }

        public Builder dialectClass(Class<? extends IDialect> dialectClass) {
            configurable.addConfig(IDatabaseConfig.DIALECT_CLASS, dialectClass.getName());
            return this;
        }

        public Builder driverClass(String driverClass) {
            configurable.addConfig(IDatabaseConfig.DRIVER_CLASS, driverClass);
            return this;
        }

        public IDataSourceConfigurable build() {
            return configurable;
        }
    }
}
