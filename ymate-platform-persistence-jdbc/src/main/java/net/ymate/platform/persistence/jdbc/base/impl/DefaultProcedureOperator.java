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

import net.ymate.platform.commons.util.ExpressionUtils;
import net.ymate.platform.core.persistence.base.Type;
import net.ymate.platform.persistence.jdbc.IDatabaseConnectionHolder;
import net.ymate.platform.persistence.jdbc.IDatabaseDataSourceConfig;
import net.ymate.platform.persistence.jdbc.base.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 数据库存储过程操作器接口实现
 *
 * @param <T> 元素类型
 * @author 刘镇 (suninformation@163.com) on 16/12/8 上午1:04
 */
public class DefaultProcedureOperator<T> extends AbstractOperator implements IProcedureOperator<T> {

    private static final Log LOG = LogFactory.getLog(DefaultProcedureOperator.class);

    /**
     * 存储过程OUT参数类型集合
     */
    private final List<Integer> outParams = new ArrayList<>();

    private IOutResultProcessor resultProcessor;

    private IResultSetHandler<T> resultSetHandler;

    private final List<List<T>> resultSets = new ArrayList<>();

    public DefaultProcedureOperator(String sql, IDatabaseConnectionHolder connectionHolder) {
        super(sql, connectionHolder);
    }

    public DefaultProcedureOperator(String sql, IDatabaseConnectionHolder connectionHolder, IAccessorConfig accessorConfig) {
        super(sql, connectionHolder, accessorConfig);
    }

    @Override
    public void execute() throws Exception {
        if (!this.executed) {
            StopWatch time = new StopWatch();
            time.start();
            try {
                doExecute();
                // 执行过程未发生异常将标记已执行，避免重复执行
                this.executed = true;
            } finally {
                time.stop();
                this.expenseTime = time.getTime();
                //
                if (LOG.isInfoEnabled()) {
                    IDatabaseDataSourceConfig dataSourceConfig = this.getConnectionHolder().getDataSourceConfig();
                    if (dataSourceConfig.isShowSql()) {
                        String logStr = ExpressionUtils.bind("[${sql}]${param}[${count}][${time}]")
                                .set("sql", StringUtils.defaultIfBlank(this.sql, "@NULL"))
                                .set("param", serializeParameters())
                                .set("count", "N/A")
                                .set("time", this.expenseTime + "ms").getResult();
                        if (dataSourceConfig.isStackTraces()) {
                            StringBuilder stackBuilder = new StringBuilder(logStr);
                            doAppendStackTraces(dataSourceConfig, stackBuilder);
                            LOG.info(stackBuilder.toString());
                        } else {
                            LOG.info(logStr);
                        }
                    }
                }
            }
        }
    }

    @Override
    public IProcedureOperator<T> execute(IResultSetHandler<T> resultSetHandler) throws Exception {
        this.resultSetHandler = resultSetHandler;
        this.execute();
        return this;
    }

    @Override
    public IProcedureOperator<T> execute(IOutResultProcessor resultProcessor) throws Exception {
        this.resultProcessor = resultProcessor;
        this.execute();
        return this;
    }

    @Override
    protected int doExecute() throws Exception {
        CallableStatement statement = null;
        AccessorEventContext eventContext = null;
        boolean hasEx = false;
        try {
            IAccessor accessor = new BaseAccessor(this.getAccessorConfig());
            statement = accessor.getCallableStatement(this.getConnectionHolder().getConnection(), doBuildCallSql());
            doSetParameters(statement);
            doRegisterOutParams(statement);
            if (this.getAccessorConfig() != null) {
                eventContext = new AccessorEventContext(statement, Type.OPT.PROCEDURE);
                this.getAccessorConfig().beforeStatementExecution(eventContext);
            }
            boolean flag = statement.execute();
            if (flag) {
                do {
                    ResultSet resultSet = statement.getResultSet();
                    if (resultSet != null) {
                        resultSets.add(resultSetHandler.handle(resultSet));
                        resultSet.close();
                    }
                } while (statement.getMoreResults());
            } else {
                int idx = this.getParameters().size() + 1;
                for (Integer paramType : outParams) {
                    resultProcessor.process(idx, paramType, statement.getObject((idx)));
                    idx++;
                }
            }
            return -1;
        } catch (Exception ex) {
            hasEx = true;
            throw ex;
        } finally {
            if (!hasEx && this.getAccessorConfig() != null && eventContext != null) {
                this.getAccessorConfig().afterStatementExecution(eventContext);
            }
            if (statement != null) {
                statement.close();
            }
        }
    }

    /**
     * 构建存储过程CALL语句(根据不同的数据库, 可由子类重新实现)
     *
     * @return 返回CALL语句
     */
    protected String doBuildCallSql() {
        List<String> params = new ArrayList<>();
        for (int i = 0; i < this.getParameters().size() + this.outParams.size(); i++) {
            params.add("?");
        }
        this.sql = String.format("{CALL %s%s}", this.getSQL(), params.isEmpty() ? "()" : String.format("(%s)", StringUtils.join(params, ',')));
        return this.sql;
    }

    /**
     * 注册存储过程输出的参数(从最后一个输入参数后开始, 根据不同的数据库，可由子类重新实现)
     *
     * @param statement CallableStatement
     * @throws SQLException 可能产生的任何异常
     */
    protected void doRegisterOutParams(CallableStatement statement) throws SQLException {
        int idx = this.getParameters().size() + 1;
        for (Integer type : outParams) {
            statement.registerOutParameter(idx++, type);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public IProcedureOperator<T> addParameter(SQLParameter parameter) {
        return (IProcedureOperator<T>) super.addParameter(parameter);
    }

    @Override
    @SuppressWarnings("unchecked")
    public IProcedureOperator<T> addParameter(Object parameter) {
        return (IProcedureOperator<T>) super.addParameter(parameter);
    }

    @Override
    public IProcedureOperator<T> addOutParameter(Integer sqlParamType) {
        this.outParams.add(sqlParamType);
        return this;
    }

    @Override
    public IProcedureOperator<T> setOutResultProcessor(IOutResultProcessor outResultProcessor) {
        resultProcessor = outResultProcessor;
        return this;
    }

    @Override
    public IProcedureOperator<T> setResultSetHandler(IResultSetHandler<T> resultSetHandler) {
        this.resultSetHandler = resultSetHandler;
        return this;
    }

    @Override
    public List<List<T>> getResultSets() {
        return Collections.unmodifiableList(resultSets);
    }
}
