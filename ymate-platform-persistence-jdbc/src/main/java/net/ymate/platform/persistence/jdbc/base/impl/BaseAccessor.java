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
package net.ymate.platform.persistence.jdbc.base.impl;

import net.ymate.platform.persistence.jdbc.base.IAccessor;
import net.ymate.platform.persistence.jdbc.base.IAccessorConfig;

import java.sql.*;

/**
 * 访问器接口基础实现
 *
 * @author 刘镇 (suninformation@163.com) on 2011-9-2 下午03:17:32
 */
public class BaseAccessor implements IAccessor {

    private IAccessorConfig accessorConfig;

    public BaseAccessor() {
    }

    public BaseAccessor(IAccessorConfig accessorConfig) {
        this.accessorConfig = accessorConfig;
    }

    protected void doSetupStatement(Statement statement) throws SQLException {
        if (statement != null && accessorConfig != null) {
            if (accessorConfig.getFetchDirection() > 0) {
                statement.setFetchDirection(accessorConfig.getFetchDirection());
            }
            if (accessorConfig.getFetchSize() > 0) {
                statement.setFetchSize(accessorConfig.getFetchSize());
            }
            if (accessorConfig.getMaxRows() > 0) {
                statement.setMaxRows(accessorConfig.getMaxRows());
            }
            if (accessorConfig.getQueryTimeout() > 0) {
                statement.setQueryTimeout(accessorConfig.getQueryTimeout());
            }
            if (accessorConfig.getMaxFieldSize() > 0) {
                statement.setMaxFieldSize(accessorConfig.getMaxFieldSize());
            }
        }
    }

    @Override
    public Statement getStatement(Connection conn) throws Exception {
        Statement statement = null;
        if (accessorConfig != null) {
            statement = accessorConfig.getStatement(conn);
        }
        if (statement == null) {
            statement = conn.createStatement();
        }
        doSetupStatement(statement);
        return statement;
    }

    @Override
    public PreparedStatement getPreparedStatement(Connection conn, String sql) throws Exception {
        PreparedStatement statement = null;
        if (this.accessorConfig != null) {
            statement = this.accessorConfig.getPreparedStatement(conn, sql);
        }
        if (statement == null) {
            statement = conn.prepareStatement(sql);
        }
        doSetupStatement(statement);
        return statement;
    }

    @Override
    public CallableStatement getCallableStatement(Connection conn, String sql) throws Exception {
        CallableStatement statement = null;
        if (accessorConfig != null) {
            statement = accessorConfig.getCallableStatement(conn, sql);
        }
        if (statement == null) {
            statement = conn.prepareCall(sql);
        }
        doSetupStatement(statement);
        return statement;
    }

    @Override
    public IAccessorConfig getAccessorConfig() {
        return accessorConfig;
    }

    public void setAccessorConfig(IAccessorConfig accessorConfig) {
        this.accessorConfig = accessorConfig;
    }
}
