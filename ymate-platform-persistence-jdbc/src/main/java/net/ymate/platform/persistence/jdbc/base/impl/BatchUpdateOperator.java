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

import net.ymate.platform.core.persistence.base.Type;
import net.ymate.platform.persistence.jdbc.IDatabaseConnectionHolder;
import net.ymate.platform.persistence.jdbc.base.*;
import org.apache.commons.lang3.StringUtils;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 数据库批量更新操作器实现
 *
 * @author 刘镇 (suninformation@163.com) on 2011-9-23 下午01:15:43
 */
public class BatchUpdateOperator extends AbstractOperator implements IBatchUpdateOperator {

    private int[] effectCounts;

    private int effectCountsTotal;

    private List<String> batchSQL;

    private List<SQLBatchParameter> batchParameters;

    public BatchUpdateOperator(IDatabaseConnectionHolder connectionHolder) {
        this(null, connectionHolder, null);
    }

    public BatchUpdateOperator(IDatabaseConnectionHolder connectionHolder, IAccessorConfig accessorConfig) {
        this(null, connectionHolder, accessorConfig);
    }

    public BatchUpdateOperator(String sql, IDatabaseConnectionHolder connectionHolder) {
        this(sql, connectionHolder, null);
    }

    public BatchUpdateOperator(String sql, IDatabaseConnectionHolder connectionHolder, IAccessorConfig accessorConfig) {
        super(sql, connectionHolder, accessorConfig);
        this.batchSQL = new ArrayList<>();
        this.batchParameters = new ArrayList<>();
    }

    @Override
    protected String serializeParameters() {
        List<Object> params = new ArrayList<>(batchParameters);
        params.addAll(batchSQL);
        return params.toString();
    }

    @Override
    protected int doExecute() throws Exception {
        Statement statement = null;
        AccessorEventContext eventContext = null;
        boolean hasEx = false;
        try {
            IAccessor accessor = new BaseAccessor(this.getAccessorConfig());
            if (StringUtils.isNotBlank(this.getSQL())) {
                statement = accessor.getPreparedStatement(this.getConnectionHolder().getConnection(), this.getSQL());
                //
                for (SQLBatchParameter batchParam : this.batchParameters) {
                    for (int i = 0; i < batchParam.getParameters().size(); i++) {
                        SQLParameter param = batchParam.getParameters().get(i);
                        if (param.getValue() == null) {
                            ((PreparedStatement) statement).setNull(i + 1, 0);
                        } else {
                            ((PreparedStatement) statement).setObject(i + 1, param.getValue());
                        }
                    }
                    ((PreparedStatement) statement).addBatch();
                }
            } else {
                statement = accessor.getStatement(this.getConnectionHolder().getConnection());
            }
            //
            for (String item : this.batchSQL) {
                statement.addBatch(item);
            }
            //
            if (this.getAccessorConfig() != null) {
                eventContext = new AccessorEventContext(statement, Type.OPT.BATCH_UPDATE);
                this.getAccessorConfig().beforeStatementExecution(eventContext);
            }
            effectCounts = statement.executeBatch();
            // 累计受影响的总记录数
            for (int c : effectCounts) {
                effectCountsTotal += c;
            }
            return effectCountsTotal;
        } catch (Exception ex) {
            hasEx = true;
            throw ex;
        } finally {
            if (!hasEx && this.getAccessorConfig() != null && eventContext != null) {
                this.getAccessorConfig().afterStatementExecution(eventContext);
            }
            if (statement != null) {
                statement.clearBatch();
                statement.close();
            }
        }
    }

    @Override
    public int[] getEffectCounts() {
        return effectCounts;
    }

    @Override
    public int getEffectCountsTotal() {
        return effectCountsTotal;
    }

    @Override
    public IBatchUpdateOperator addBatchSQL(String sql) {
        this.batchSQL.add(sql);
        return this;
    }

    @Override
    public IBatchUpdateOperator addBatchParameter(SQLBatchParameter parameter) {
        if (StringUtils.isBlank(this.getSQL())) {
            // 构造未设置SQL时将不支持添加批量参数
            throw new UnsupportedOperationException();
        }
        if (parameter != null) {
            this.batchParameters.add(parameter);
        }
        return this;
    }

    @Override
    public List<SQLBatchParameter> getBatchParameters() {
        return Collections.unmodifiableList(batchParameters);
    }

    @Override
    public IOperator addParameter(Object parameter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IOperator addParameter(SQLParameter parameter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<SQLParameter> getParameters() {
        throw new UnsupportedOperationException();
    }
}
