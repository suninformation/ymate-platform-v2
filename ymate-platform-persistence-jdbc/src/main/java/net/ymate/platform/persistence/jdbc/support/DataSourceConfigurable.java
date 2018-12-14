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
package net.ymate.platform.persistence.jdbc.support;

import net.ymate.platform.core.support.IPasswordProcessor;
import net.ymate.platform.persistence.jdbc.IDataSourceAdapter;
import net.ymate.platform.persistence.jdbc.IDatabaseModuleCfg;
import net.ymate.platform.persistence.jdbc.JDBC;
import net.ymate.platform.persistence.jdbc.dialect.IDialect;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2018-12-14 13:08
 * @version 1.0
 * @since 2.0.6
 */
public class DataSourceConfigurable {

    public static DataSourceConfigurable create(String name) {
        return new DataSourceConfigurable(name);
    }

    private Map<String, String> __configs = new HashMap<String, String>();

    private String name;

    public DataSourceConfigurable(String name) {
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

    public DataSourceConfigurable connectionUrl(String connectionUrl) {
        __putItem(IDatabaseModuleCfg.CONNECTION_URL, connectionUrl);
        return this;
    }

    public DataSourceConfigurable username(String username) {
        __putItem(IDatabaseModuleCfg.USERNAME, username);
        return this;
    }

    public DataSourceConfigurable password(String password) {
        __putItem(IDatabaseModuleCfg.PASSWORD, password);
        return this;
    }

    public DataSourceConfigurable passwordEncrypted(boolean passwordEncrypted) {
        __putItem(IDatabaseModuleCfg.PASSWORD, String.valueOf(passwordEncrypted));
        return this;
    }

    public DataSourceConfigurable passwordClass(Class<? extends IPasswordProcessor> passwordClass) {
        __putItem(IDatabaseModuleCfg.PASSWORD_CLASS, passwordClass.getName());
        return this;
    }

    public DataSourceConfigurable showSql(boolean showSql) {
        __putItem(IDatabaseModuleCfg.SHOW_SQL, String.valueOf(showSql));
        return this;
    }

    public DataSourceConfigurable stackTraces(boolean stackTraces) {
        __putItem(IDatabaseModuleCfg.STACK_TRACES, String.valueOf(stackTraces));
        return this;
    }

    public DataSourceConfigurable stackTraceDepth(int stackTraceDepth) {
        __putItem(IDatabaseModuleCfg.STACK_TRACE_DEPTH, String.valueOf(stackTraceDepth));
        return this;
    }

    public DataSourceConfigurable stackTracePackage(String stackTracePackage) {
        __putItem(IDatabaseModuleCfg.STACK_TRACE_PACKAGE, stackTracePackage);
        return this;
    }

    public DataSourceConfigurable tablePrefix(String tablePrefix) {
        __putItem(IDatabaseModuleCfg.TABLE_PREFIX, tablePrefix);
        return this;
    }

    public DataSourceConfigurable identifierQuote(String identifierQuote) {
        __putItem(IDatabaseModuleCfg.IDENTIFIER_QUOTE, identifierQuote);
        return this;
    }

    public DataSourceConfigurable adapterClass(String adapterClass) {
        __putItem(IDatabaseModuleCfg.ADAPTER_CLASS, adapterClass);
        return this;
    }

    public DataSourceConfigurable adapterClass(Class<? extends IDataSourceAdapter> adapterClass) {
        __putItem(IDatabaseModuleCfg.ADAPTER_CLASS, adapterClass.getName());
        return this;
    }

    public DataSourceConfigurable type(JDBC.DATABASE type) {
        __putItem(IDatabaseModuleCfg.TYPE, type.name());
        return this;
    }

    public DataSourceConfigurable dialectClass(Class<? extends IDialect> dialectClass) {
        __putItem(IDatabaseModuleCfg.DIALECT_CLASS, dialectClass.getName());
        return this;
    }

    public DataSourceConfigurable driverClass(String driverClass) {
        __putItem(IDatabaseModuleCfg.DRIVER_CLASS, driverClass);
        return this;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> toMap() {
        return __configs;
    }
}
