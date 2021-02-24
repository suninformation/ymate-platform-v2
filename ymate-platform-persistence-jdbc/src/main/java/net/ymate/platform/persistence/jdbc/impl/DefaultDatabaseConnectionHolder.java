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

import net.ymate.platform.persistence.jdbc.IDatabase;
import net.ymate.platform.persistence.jdbc.IDatabaseConnectionHolder;
import net.ymate.platform.persistence.jdbc.IDatabaseDataSourceAdapter;
import net.ymate.platform.persistence.jdbc.IDatabaseDataSourceConfig;
import net.ymate.platform.persistence.jdbc.dialect.IDialect;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 默认数据库Connection对象持有者接口实现
 *
 * @author 刘镇 (suninformation@163.com) on 2012-12-29 下午4:44:08
 */
public class DefaultDatabaseConnectionHolder implements IDatabaseConnectionHolder {

    private final IDatabaseDataSourceAdapter dataSourceAdapter;

    private Connection conn;

    public DefaultDatabaseConnectionHolder(IDatabaseDataSourceAdapter dsAdapter) throws Exception {
        dataSourceAdapter = dsAdapter;
        if (dataSourceAdapter.initializeIfNeed()) {
            conn = dsAdapter.getConnection();
        }
    }

    @Override
    public IDatabase getOwner() {
        return dataSourceAdapter.getOwner();
    }

    @Override
    public IDatabaseDataSourceConfig getDataSourceConfig() {
        return dataSourceAdapter.getDataSourceConfig();
    }

    @Override
    public Connection getConnection() {
        return conn;
    }

    @Override
    public void close() {
        try {
            if (this.conn != null && !this.conn.isClosed()) {
                this.conn.close();
            }
        } catch (SQLException ignored) {
        }
    }

    @Override
    public IDialect getDialect() {
        return dataSourceAdapter.getDialect();
    }

    @Override
    public IDatabaseDataSourceAdapter getDataSourceAdapter() {
        return dataSourceAdapter;
    }
}
