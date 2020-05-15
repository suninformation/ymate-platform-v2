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
package net.ymate.platform.persistence.jdbc.query;

import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.persistence.*;
import net.ymate.platform.core.persistence.base.EntityMeta;
import net.ymate.platform.persistence.jdbc.IDatabase;
import net.ymate.platform.persistence.jdbc.IDatabaseConnectionHolder;
import net.ymate.platform.persistence.jdbc.JDBC;
import net.ymate.platform.persistence.jdbc.base.impl.BeanResultSetHandler;
import net.ymate.platform.persistence.jdbc.dialect.IDialect;
import net.ymate.platform.persistence.jdbc.query.annotation.*;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @param <T> 当前实现类类型
 * @author 刘镇 (suninformation@163.com) on 2017/12/14 下午11:43
 */
public class Query<T> extends QueryHandleAdapter<T> {

    private static final Log LOG = LogFactory.getLog(Query.class);

    public static final String LINE_END_FLAG = ",";

    private final IDatabase owner;

    private String dataSourceName;

    private IDialect dialect;

    private String defaultTablePrefix;

    private IShardingRule shardingRule;

    private IShardingable shardingable;

    public static <T> Executor<T> build(Class<T> queryClass) {
        IDatabase owner = JDBC.get();
        return build(owner, owner.getConfig().getDefaultDataSourceName(), queryClass);
    }

    public static <T> Executor<T> build(IDatabase owner, String dataSourceName, Class<T> queryClass) {
        return new Executor<>(owner, dataSourceName, queryClass);
    }

    public Query(IDatabase owner, String dataSourceName) {
        this.owner = owner;
        this.dataSourceName = dataSourceName;
    }

    public IDatabase owner() {
        return owner;
    }

    public String dataSourceName() {
        return StringUtils.isNotBlank(dataSourceName) ? dataSourceName : owner.getConfig().getDefaultDataSourceName();
    }

    @SuppressWarnings("unchecked")
    public T dataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
        return (T) this;
    }

    /**
     * @return 返回当前数据库方言，若未设置则返回默认数据源配置的方言
     */
    public IDialect dialect() {
        if (dialect == null) {
            try (IDatabaseConnectionHolder connectionHolder = owner.getConnectionHolder(dataSourceName())) {
                dialect = connectionHolder.getDialect();
            } catch (Exception e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
                }
            }
        }
        return dialect;
    }

    @SuppressWarnings("unchecked")
    public T dialect(IDialect dialect) {
        this.dialect = dialect;
        return (T) this;
    }

    public String defaultTablePrefix() {
        if (defaultTablePrefix == null) {
            defaultTablePrefix = StringUtils.defaultIfBlank(owner.getConfig().getDataSourceConfig(dataSourceName()).getTablePrefix(), StringUtils.EMPTY);
        }
        return StringUtils.trimToEmpty(defaultTablePrefix);
    }

    public IShardingRule shardingRule() {
        return shardingRule;
    }

    @SuppressWarnings("unchecked")
    public T shardingRule(IShardingRule shardingRule) {
        this.shardingRule = shardingRule;
        return (T) this;
    }

    public IShardingable shardingable() {
        return shardingable;
    }

    @SuppressWarnings("unchecked")
    public T shardingable(IShardingable shardingable) {
        this.shardingable = shardingable;
        return (T) this;
    }

    // ----------

    protected Fields checkFieldExcluded(Fields fields) {
        if (fields.isExcluded()) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Query fields do not support exclusion and have been cleaned up.");
            }
            return Fields.create();
        }
        return fields;
    }

    protected String buildSafeTableName(String prefix, String tableName, boolean safePrefix) {
        if (safePrefix) {
            if (StringUtils.isBlank(prefix)) {
                prefix = defaultTablePrefix();
            }
            return dialect().buildTableName(prefix, tableName, shardingRule(), shardingable());
        }
        if (StringUtils.isNotBlank(prefix) && StringUtils.startsWith(tableName, prefix)) {
            prefix = StringUtils.EMPTY;
        }
        return StringUtils.trimToEmpty(prefix).concat(tableName);
    }

    protected String buildSafeTableName(String prefix, EntityMeta entityMeta, boolean safePrefix) {
        if (safePrefix) {
            if (StringUtils.isBlank(prefix)) {
                prefix = defaultTablePrefix();
            }
            return dialect().buildTableName(prefix, entityMeta, shardingable());
        }
        if (StringUtils.isNotBlank(prefix) && StringUtils.startsWith(entityMeta.getEntityName(), prefix)) {
            prefix = StringUtils.EMPTY;
        }
        return StringUtils.trimToEmpty(prefix).concat(entityMeta.getEntityName());
    }

    protected Fields wrapIdentifierFields(String... fields) {
        if (!this.dialect().hasIdentifierQuote()) {
            return Fields.create(fields);
        }
        Fields returnValue = Fields.create();
        if (fields != null) {
            for (String field : fields) {
                returnValue.add(wrapIdentifierField(field));
            }
        }
        return returnValue;
    }

    protected String wrapIdentifierField(String field) {
        return wrapIdentifierField(dialect(), field);
    }

    public static String wrapIdentifierField(IDialect dialect, String field) {
        if (dialect.hasIdentifierQuote()) {
            String[] splits = StringUtils.split(field, ".");
            if (splits != null && splits.length > 0) {
                if (splits.length == 2) {
                    String[] alias = StringUtils.split(splits[1]);
                    if (alias != null && alias.length == 2) {
                        return String.format("%s.%s %s", splits[0], dialect.wrapIdentifierQuote(alias[0]), alias[1]);
                    }
                    return String.format("%s.%s", splits[0], dialect.wrapIdentifierQuote(splits[1]));
                } else if (splits.length == 1) {
                    return dialect.wrapIdentifierQuote(splits[0]);
                }
            }
        }
        return field;
    }

    public static String wrapIdentifierField(IDatabaseConnectionHolder connectionHolder, String field) {
        return wrapIdentifierField(connectionHolder.getDialect(), field);
    }

    /**
     * 查询执行器，用于解析和执行基于注解配置查询的类
     *
     * @param <T> 结果对象类型
     */
    public static class Executor<T> extends Query<Executor<T>> {

        private final Class<T> queryClass;

        private final Map<String, Object> variables = new HashMap<>();

        private Where where;

        private boolean replaceWhere;

        public Executor(IDatabase owner, String dataSourceName, Class<T> queryClass) {
            super(owner, dataSourceName);
            if (queryClass == null) {
                throw new NullArgumentException("queryClass");
            }
            this.queryClass = queryClass;
        }

        public Executor<T> addVariable(String name, Object value) {
            if (StringUtils.isNotBlank(name)) {
                variables.put(name, value);
            }
            return this;
        }

        public Executor<T> addVariables(Map<String, Object> variables) {
            if (variables != null && !variables.isEmpty()) {
                variables.forEach(this::addVariable);
            }
            return this;
        }

        public Executor<T> where(Where where) {
            return where(where, false);
        }

        public Executor<T> where(Where where, boolean replace) {
            this.where = where;
            this.replaceWhere = replace;
            return this;
        }

        private void doParseFrom(Select select, QFrom qFrom) {
            if (qFrom != null) {
                if (QFrom.Type.SQL == qFrom.type()) {
                    String sql = qFrom.value();
                    if (StringUtils.isNotBlank(qFrom.alias())) {
                        sql = String.format("(%s)", sql);
                    }
                    select.from(null, sql, qFrom.alias());
                } else {
                    select.from(qFrom.prefix(), qFrom.value(), qFrom.alias(), true);
                }
            }
        }

        private Cond doParseCond(QCond[] qConds) {
            if (ArrayUtils.isNotEmpty(qConds)) {
                Cond cond = Cond.create(this);
                int idx = 0;
                for (QCond qCond : qConds) {
                    String withFieldValue = qCond.with().value();
                    if (StringUtils.isNotBlank(qCond.field().value()) && StringUtils.isNotBlank(withFieldValue)) {
                        boolean skipped = false;
                        char firstChar = withFieldValue.charAt(0);
                        if (firstChar == '#') {
                            // 以#开头则替换变量值
                            String varName = StringUtils.substring(withFieldValue, 1);
                            if (variables.containsKey(varName)) {
                                withFieldValue = "?";
                                cond.param(variables.get(varName));
                            } else if (qCond.ignorable()) {
                                skipped = true;
                            } else {
                                throw new IllegalArgumentException(String.format("Variable '%s' is not set.", varName));
                            }
                        } else if (firstChar == '$') {
                            // 以$开头则字符串原样传入
                            withFieldValue = StringUtils.substring(withFieldValue, 1);
                        } else {
                            withFieldValue = Fields.field(qCond.with().prefix(), qCond.with().value());
                        }
                        if (!skipped) {
                            if (idx > 0) {
                                switch (qCond.logicalOpt()) {
                                    case NOT:
                                        cond.not();
                                        break;
                                    case OR:
                                        cond.or();
                                        break;
                                    default:
                                        cond.and();
                                }
                            }
                            cond.opt(Fields.field(qCond.field().prefix(), qCond.field().value()), qCond.opt(), withFieldValue);
                            idx++;
                        }
                    }
                }
                return cond;
            }
            return null;
        }

        private void doParseJoin(Select select, QJoin qJoin) {
            if (qJoin != null) {
                Join join;
                if (QFrom.Type.SQL == qJoin.from().type()) {
                    String sql = qJoin.from().value();
                    if (StringUtils.isNotBlank(qJoin.from().alias())) {
                        sql = String.format("(%s)", sql);
                    }
                    join = new Join(owner(), dataSourceName(), qJoin.type().getName(), null, sql, false);
                } else {
                    join = new Join(owner(), dataSourceName(), qJoin.type().getName(), qJoin.from().prefix(), qJoin.from().value(), true);
                }
                join.alias(qJoin.from().alias());
                Cond cond = doParseCond(qJoin.on());
                if (cond != null) {
                    select.join(join.on(cond));
                }
            }
        }

        private void doParseWhere(Select select) {
            Where selectWhere = select.where();
            QWhere qWhere = queryClass.getAnnotation(QWhere.class);
            if (qWhere != null) {
                Cond newCond = doParseCond(qWhere.value());
                if (newCond != null && !newCond.isEmpty()) {
                    selectWhere.cond().cond(newCond);
                }
            }
            QOrderBy qOrderBy = queryClass.getAnnotation(QOrderBy.class);
            if (qOrderBy != null) {
                for (QOrderField qOrderField : qOrderBy.value()) {
                    if (QOrderField.Type.DESC.equals(qOrderField.type())) {
                        selectWhere.orderByDesc(qOrderField.prefix(), qOrderField.value());
                    } else {
                        selectWhere.orderByAsc(qOrderField.prefix(), qOrderField.value());
                    }
                }
            }
            QGroupBy qGroupBy = queryClass.getAnnotation(QGroupBy.class);
            if (qGroupBy != null) {
                for (QField qField : qGroupBy.value()) {
                    selectWhere.groupBy(Fields.field(qField.prefix(), qField.value()));
                }
                Cond havingCond = doParseCond(qGroupBy.having());
                if (havingCond != null && !havingCond.isEmpty()) {
                    selectWhere.having(havingCond);
                }
            }
            if (where != null) {
                selectWhere.where(where);
            }
        }

        public Select buildSelect() {
            Select select = Select.create(this);
            // Parse From
            QFroms qFroms = queryClass.getAnnotation(QFroms.class);
            if (qFroms != null) {
                Arrays.stream(qFroms.value()).forEachOrdered(qFrom -> doParseFrom(select, qFrom));
            }
            doParseFrom(select, queryClass.getAnnotation(QFrom.class));
            // Parse Field
            ClassUtils.getFields(queryClass, true)
                    .stream()
                    .filter(ClassUtils::isNormalField)
                    .forEachOrdered((field) -> {
                        QField qField = field.getAnnotation(QField.class);
                        if (qField != null) {
                            select.field(qField.prefix(), qField.value(), qField.alias());
                        }
                    });
            // Parse Join
            QJoins qJoins = queryClass.getAnnotation(QJoins.class);
            if (qJoins != null) {
                Arrays.stream(qJoins.value()).forEachOrdered(qJoin -> doParseJoin(select, qJoin));
            }
            doParseJoin(select, queryClass.getAnnotation(QJoin.class));
            // Parse Where
            if (where != null && replaceWhere) {
                select.where(where);
            } else {
                doParseWhere(select);
            }
            //
            return select;
        }

        public T findFirst() throws Exception {
            return buildSelect().findFirst(new BeanResultSetHandler<>(queryClass));
        }

        public IResultSet<T> find() throws Exception {
            return buildSelect().find(new BeanResultSetHandler<>(queryClass));
        }

        public IResultSet<T> find(Page page) throws Exception {
            return buildSelect().find(new BeanResultSetHandler<>(queryClass), page);
        }

        public long count() throws Exception {
            return buildSelect().count();
        }
    }
}
