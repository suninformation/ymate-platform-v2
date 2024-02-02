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
import java.sql.SQLException;

/**
 * 数据库更新操作器接口实现
 *
 * @author 刘镇 (suninformation@163.com) on 2011-9-23 上午10:38:24
 */
public class DefaultUpdateOperator extends AbstractOperator implements IUpdateOperator {

    private static final Log LOG = LogFactory.getLog(DefaultUpdateOperator.class);

    private int effectCounts;

    public DefaultUpdateOperator(String sql, IDatabaseConnectionHolder connectionHolder) {
        super(sql, connectionHolder);
    }

    public DefaultUpdateOperator(String sql, IDatabaseConnectionHolder connectionHolder, IAccessorConfig accessorConfig) {
        super(sql, connectionHolder, accessorConfig);
    }

    @Override
    protected int doExecute() throws Exception {
        PreparedStatement statement = null;
        AccessorEventContext eventContext = null;
        try {
            IAccessor accessor = new BaseAccessor(this.getAccessorConfig());
            statement = accessor.getPreparedStatement(this.getConnectionHolder().getConnection(), this.getSQL());
            doSetParameters(statement);
            if (this.getAccessorConfig() != null) {
                eventContext = new AccessorEventContext(statement, Type.OPT.UPDATE);
                this.getAccessorConfig().beforeStatementExecution(eventContext);
            }
            effectCounts = statement.executeUpdate();
            return effectCounts;
        } catch (Exception ex) {
            hasEx = true;
            throw ex;
        } finally {
            doAfterStatementExecutionIfNeed(eventContext);
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
    public int getEffectCounts() {
        return effectCounts;
    }
}
