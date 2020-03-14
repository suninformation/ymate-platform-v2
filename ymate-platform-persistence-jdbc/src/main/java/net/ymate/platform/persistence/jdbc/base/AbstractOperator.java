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
package net.ymate.platform.persistence.jdbc.base;

import net.ymate.platform.commons.util.ExpressionUtils;
import net.ymate.platform.core.persistence.base.Type;
import net.ymate.platform.persistence.jdbc.IDatabaseConnectionHolder;
import net.ymate.platform.persistence.jdbc.IDatabaseDataSourceConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 数据库操作器接口抽象实现
 *
 * @author 刘镇 (suninformation@163.com) on 2011-9-22 下午10:19:53
 */
public abstract class AbstractOperator implements IOperator {

    private static final Log LOG = LogFactory.getLog(AbstractOperator.class);

    protected String sql;

    private final IDatabaseConnectionHolder connectionHolder;

    private IAccessorConfig accessorConfig;

    private final List<SQLParameter> parameters;

    protected long expenseTime;

    /**
     * 是否已执行
     */
    protected boolean executed;

    public AbstractOperator(String sql, IDatabaseConnectionHolder connectionHolder) {
        this(sql, connectionHolder, null);
    }

    public AbstractOperator(String sql, IDatabaseConnectionHolder connectionHolder, IAccessorConfig accessorConfig) {
        this.sql = sql;
        this.connectionHolder = connectionHolder;
        this.accessorConfig = accessorConfig;
        this.parameters = new ArrayList<>();
    }

    @Override
    public void execute() throws Exception {
        if (!this.executed) {
            StopWatch time = new StopWatch();
            time.start();
            int effectCounts = 0;
            try {
                effectCounts = doExecute();
                // 执行过程未发生异常将标记已执行，避免重复执行
                this.executed = true;
            } finally {
                time.stop();
                this.expenseTime = time.getTime();
                //
                if (LOG.isInfoEnabled()) {
                    IDatabaseDataSourceConfig dataSourceConfig = this.connectionHolder.getDataSourceConfig();
                    if (dataSourceConfig.isShowSql()) {
                        String logStr = ExpressionUtils.bind("[${sql}]${param}[${count}][${time}]")
                                .set("sql", StringUtils.defaultIfBlank(this.sql, "@NULL"))
                                .set("param", serializeParameters())
                                .set("count", effectCounts + "")
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

    protected void doAppendStackTraces(IDatabaseDataSourceConfig dataSourceConfig, StringBuilder stackBuilder) {
        String[] tracePackages = StringUtils.split(dataSourceConfig.getStackTracePackages(), "|");
        StackTraceElement[] stacks = new Throwable().getStackTrace();
        if (stacks != null && stacks.length > 0) {
            int depth = dataSourceConfig.getStackTraceDepth() <= 0 ? stacks.length : Math.min(dataSourceConfig.getStackTraceDepth(), stacks.length);
            if (depth > 0) {
                for (int idx = 0; idx < depth; idx++) {
                    if (tracePackages != null && tracePackages.length > 0) {
                        if (StringUtils.containsAny(stacks[idx].getClassName(), "$$EnhancerByCGLIB$$", "_$$_") || !StringUtils.startsWithAny(stacks[idx].getClassName(), tracePackages)) {
                            continue;
                        }
                    }
                    stackBuilder.append("\n\t--> ").append(stacks[idx]);
                }
            }
        }
    }

    protected String serializeParameters() {
        return this.parameters.toString();
    }

    /**
     * 执行具体的操作过程
     *
     * @return 返回影响行数
     * @throws Exception 执行过程中产生的异常
     */
    protected abstract int doExecute() throws Exception;

    protected void doSetParameters(PreparedStatement statement) throws SQLException {
        int idx = 1;
        for (SQLParameter parameter : this.getParameters()) {
            if (parameter.getValue() == null) {
                statement.setNull(idx, 0);
            } else if (parameter.getType() != null && !Type.FIELD.UNKNOWN.equals(parameter.getType())) {
                statement.setObject(idx, parameter.getValue(), parameter.getType().getType());
            } else {
                statement.setObject(idx, parameter.getValue());
            }
            idx++;
        }
    }

    @Override
    public boolean isExecuted() {
        return executed;
    }

    @Override
    public String getSQL() {
        return sql;
    }

    @Override
    public IAccessorConfig getAccessorConfig() {
        return accessorConfig;
    }

    @Override
    public void setAccessorConfig(IAccessorConfig accessorConfig) {
        this.accessorConfig = accessorConfig;
    }

    @Override
    public IDatabaseConnectionHolder getConnectionHolder() {
        return connectionHolder;
    }

    @Override
    public long getExpenseTime() {
        return expenseTime;
    }

    @Override
    public List<SQLParameter> getParameters() {
        return Collections.unmodifiableList(this.parameters);
    }

    @Override
    public IOperator addParameter(SQLParameter parameter) {
        if (parameter != null) {
            this.parameters.add(parameter);
        }
        return this;
    }

    @Override
    public IOperator addParameter(Object parameter) {
        SQLParameter.addParameter(this.parameters, parameter);
        return this;
    }
}
