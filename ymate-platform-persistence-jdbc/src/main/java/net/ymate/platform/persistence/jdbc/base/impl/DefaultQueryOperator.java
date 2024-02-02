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

import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.persistence.base.Type;
import net.ymate.platform.persistence.jdbc.IDatabaseConnectionHolder;
import net.ymate.platform.persistence.jdbc.base.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * 数据库查询操作器接口实现
 *
 * @param <T> 元素类型
 * @author 刘镇 (suninformation@163.com) on 2011-9-23 上午09:32:37
 */
public class DefaultQueryOperator<T> extends AbstractOperator implements IQueryOperator<T> {

    private static final Log LOG = LogFactory.getLog(DefaultQueryOperator.class);

    private final IResultSetHandler<T> resultSetHandler;

    private List<T> resultSet;

    private final int maxRow;

    public DefaultQueryOperator(String sql, IDatabaseConnectionHolder connectionHolder, IResultSetHandler<T> resultSetHandler) {
        this(sql, connectionHolder, null, resultSetHandler, 0);
    }

    public DefaultQueryOperator(String sql, IDatabaseConnectionHolder connectionHolder, IResultSetHandler<T> resultSetHandler, int maxRow) {
        this(sql, connectionHolder, null, resultSetHandler, maxRow);
    }

    public DefaultQueryOperator(String sql, IDatabaseConnectionHolder connectionHolder, IAccessorConfig accessorConfig, IResultSetHandler<T> resultSetHandler) {
        this(sql, connectionHolder, accessorConfig, resultSetHandler, 0);
    }

    public DefaultQueryOperator(String sql, IDatabaseConnectionHolder connectionHolder, IAccessorConfig accessorConfig, IResultSetHandler<T> resultSetHandler, int maxRow) {
        super(sql, connectionHolder, accessorConfig);
        this.resultSetHandler = resultSetHandler;
        this.maxRow = maxRow;
    }

    @Override
    protected int doExecute() throws Exception {
        PreparedStatement statement = null;
        ResultSet result = null;
        AccessorEventContext eventContext = null;
        try {
            IAccessor accessor = new BaseAccessor(this.getAccessorConfig());
            statement = accessor.getPreparedStatement(this.getConnectionHolder().getConnection(), this.getSQL());
            if (this.maxRow > 0) {
                statement.setMaxRows(this.maxRow);
            }
            doSetParameters(statement);
            if (this.getAccessorConfig() != null) {
                eventContext = new AccessorEventContext(statement, Type.OPT.QUERY);
                this.getAccessorConfig().beforeStatementExecution(eventContext);
            }
            result = statement.executeQuery();
            this.resultSet = this.getResultSetHandler().handle(result);
            return this.resultSet.size();
        } catch (Exception ex) {
            hasEx = true;
            throw ex;
        } finally {
            doAfterStatementExecutionIfNeed(eventContext);
            if (result != null) {
                try {
                    result.close();
                } catch (SQLException e) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(e.getMessage(), RuntimeUtils.unwrapThrow(e));
                    }
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(e.getMessage(), RuntimeUtils.unwrapThrow(e));
                    }
                }
            }
        }
    }

    @Override
    public IResultSetHandler<T> getResultSetHandler() {
        return resultSetHandler;
    }

    @Override
    public List<T> getResultSet() {
        return Collections.unmodifiableList(resultSet);
    }

    @Override
    public int getMaxRow() {
        return maxRow;
    }
}
