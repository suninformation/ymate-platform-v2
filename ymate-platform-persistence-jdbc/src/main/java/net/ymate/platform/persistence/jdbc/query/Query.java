/*
 * Copyright 2007-2025 the original author or authors.
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

import net.ymate.platform.commons.lang.BlurObject;
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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
        return build(JDBC.get(), null, queryClass);
    }

    /**
     * @since 2.1.3
     */
    public static <T> Executor<T> build(IDatabase owner, Class<T> queryClass) {
        return new Executor<>(owner, null, queryClass);
    }

    public static <T> Executor<T> build(IDatabase owner, String dataSourceName, Class<T> queryClass) {
        return new Executor<>(owner, dataSourceName, queryClass);
    }

    /**
     * @since 2.1.3
     */
    public static Select build(String alias, boolean unionAll, Class<?>... queryClasses) throws Exception {
        return build(JDBC.get(), null, alias, unionAll, queryClasses);
    }

    /**
     * @since 2.1.3
     */
    public static Select build(IDatabase owner, String alias, boolean unionAll, Class<?>... queryClasses) throws Exception {
        return build(owner, null, alias, unionAll, queryClasses);
    }

    /**
     * @since 2.1.3
     */
    public static Select build(IDatabase owner, String dataSourceName, String alias, boolean unionAll, Class<?>... queryClasses) throws Exception {
        if (StringUtils.isBlank(alias)) {
            throw new NullArgumentException("alias");
        }
        Select select = null;
        if (ArrayUtils.isNotEmpty(queryClasses)) {
            for (Class<?> clazz : queryClasses) {
                Select subselect = build(owner, dataSourceName, clazz).ignoreOrderBy().buildSelect();
                if (select == null) {
                    select = subselect;
                } else if (unionAll) {
                    select.unionAll(subselect);
                } else {
                    select.union(subselect);
                }
            }
        }
        if (select == null) {
            throw new NullArgumentException("queryClasses");
        }
        return Select.create(select.alias(alias));
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
            try {
                dialect = owner.getDataSourceAdapter(dataSourceName()).getDialect();
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
                    if (alias != null) {
                        if (alias.length == 2) {
                            return String.format("%s.%s AS %s", splits[0], dialect.wrapIdentifierQuote(alias[0]), dialect.wrapIdentifierQuote(alias[1]));
                        } else if (alias.length == 3 && StringUtils.equalsIgnoreCase(alias[1], "as")) {
                            return String.format("%s AS %s", dialect.wrapIdentifierQuote(alias[0]), dialect.wrapIdentifierQuote(alias[2]));
                        }
                    }
                    return String.format("%s.%s", splits[0], dialect.wrapIdentifierQuote(splits[1]));
                } else if (splits.length == 1) {
                    String[] alias = StringUtils.split(splits[0]);
                    if (alias != null) {
                        if (alias.length == 2) {
                            return String.format("%s AS %s", dialect.wrapIdentifierQuote(alias[0]), dialect.wrapIdentifierQuote(alias[1]));
                        } else if (alias.length == 3 && StringUtils.equalsIgnoreCase(alias[1], "as")) {
                            return String.format("%s AS %s", dialect.wrapIdentifierQuote(alias[0]), dialect.wrapIdentifierQuote(alias[2]));
                        }
                    }
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

        private static final Map<Class<?>, ParsedQueryMetadata> METADATA_CACHE = new ConcurrentHashMap<>();

        private final Class<T> queryClass;

        private final Set<String> excludedFields = new HashSet<>();

        private final Map<String, Object> variables = new HashMap<>();

        private Where where;

        private boolean replaceWhere;

        private boolean ignoreOrderBy;

        /**
         * @since 2.1.3
         */
        private boolean distinct;

        public Executor(IDatabase owner, String dataSourceName, Class<T> queryClass) {
            super(owner, dataSourceName);
            if (queryClass == null) {
                throw new NullArgumentException("queryClass");
            }
            this.queryClass = queryClass;
            this.distinct = !ClassUtils.getAnnotation(queryClass, QDistinct.class, true, true).isEmpty();
        }

        public Executor<T> addExcludeField(String field) {
            if (StringUtils.isNotBlank(field)) {
                excludedFields.add(field);
            }
            return this;
        }

        public Executor<T> addExcludeField(Fields fields) {
            if (fields != null && !fields.isEmpty()) {
                excludedFields.addAll(fields.fields());
            }
            return this;
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

        /**
         * @see #appendWhere(Where)
         * @deprecated since 2.1.4, use {@link #appendWhere(Where)} instead.
         */
        @Deprecated
        public Executor<T> where(Where where) {
            return appendWhere(where);
        }

        /**
         * @see #appendWhere(Where)
         * @see #replaceWhere(Where)
         * @deprecated since 2.1.4, use {@link #appendWhere(Where)} or {@link #replaceWhere(Where)} instead.
         */
        @Deprecated
        public Executor<T> where(Where where, boolean replace) {
            this.where = where;
            this.replaceWhere = replace;
            return this;
        }

        /**
         * @since 2.1.4
         */
        public Executor<T> appendWhere(Where where) {
            this.where = where;
            this.replaceWhere = false;
            return this;
        }

        /**
         * @since 2.1.4
         */
        public Executor<T> replaceWhere(Where where) {
            this.where = where;
            this.replaceWhere = true;
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

        private Object doProcessCondWithValue(QCond qCond, Cond cond) {
            String withFieldValue = qCond.with().value();
            char firstChar = withFieldValue.charAt(0);
            if (firstChar == '#') {
                // 以#开头则替换变量值
                String varName = StringUtils.substring(withFieldValue, 1);
                if (variables.containsKey(varName)) {
                    cond.param(variables.get(varName));
                    return "?";
                } else if (qCond.ignorable()) {
                    return null; // Indicates skipped
                } else {
                    throw new IllegalArgumentException(String.format("Variable '%s' is not set.", varName));
                }
            } else if (firstChar == '$') {
                // 以$开头的字符串表达式可以通过分隔符指定其数据类型并根据表达式尝试转换数据类型或跳过
                String fieldValue = StringUtils.substring(withFieldValue, 1);
                if (StringUtils.contains(fieldValue, ":")) {
                    String[] fieldValueArr = StringUtils.split(fieldValue, ":");
                    if (fieldValueArr != null && fieldValueArr.length == 2) {
                        String type = fieldValueArr[0].toLowerCase();
                        String value = fieldValueArr[1];
                        switch (type) {
                            case "int":
                                cond.param(BlurObject.bind(value).toIntValue());
                                break;
                            case "long":
                                cond.param(BlurObject.bind(value).toLongValue());
                                break;
                            case "float":
                                cond.param(BlurObject.bind(value).toFloatValue());
                                break;
                            case "double":
                                cond.param(BlurObject.bind(value).toDoubleValue());
                                break;
                            case "string":
                                cond.param(value);
                                break;
                            default:
                                throw new UnsupportedOperationException(String.format("Unsupported data type prefix '%s:'.", fieldValueArr[0]));
                        }
                        return "?";
                    } else if (qCond.ignorable()) {
                        return null; // Indicates skipped
                    } else {
                        cond.param(fieldValue);
                    }
                } else if (StringUtils.isBlank(fieldValue) && qCond.ignorable()) {
                    return null; // Indicates skipped
                } else {
                    cond.param(fieldValue);
                }
                return "?";
            }
            return Fields.field(qCond.with().prefix(), qCond.with().value());
        }

        private Cond doParseCond(QCond[] qConds) {
            if (ArrayUtils.isEmpty(qConds)) {
                return null;
            }
            Cond cond = Cond.create(this);
            int idx = 0;
            for (QCond qCond : qConds) {
                if (StringUtils.isBlank(qCond.field().value()) || StringUtils.isBlank(qCond.with().value())) {
                    continue;
                }
                Object withFieldValue = doProcessCondWithValue(qCond, cond);
                if (withFieldValue != null) {
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
                    String fieldOne = Fields.field(qCond.field().prefix(), qCond.field().value());
                    if (qCond.field().wrapIdentifier()) {
                        fieldOne = wrapIdentifierField(fieldOne);
                    }
                    String withFieldValueStr = (String) withFieldValue;
                    if (!StringUtils.equals(withFieldValueStr, "?") && qCond.with().wrapIdentifier()) {
                        withFieldValueStr = wrapIdentifierField(withFieldValueStr);
                    }
                    cond.opt(fieldOne, qCond.opt(), withFieldValueStr);
                    idx++;
                }
            }
            return cond.isEmpty() ? null : cond;
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

        private void doParseOrderBy(Where where, List<QOrderBy> qOrderBys) {
            if (!ignoreOrderBy && where != null) {
                OrderBy orderBy = where.orderBy();
                if (orderBy == null || orderBy.isEmpty()) {
                    if (!qOrderBys.isEmpty()) {
                        for (QOrderBy qOrderBy : qOrderBys) {
                            for (QOrderField qOrderField : qOrderBy.value()) {
                                if (QOrderField.Type.DESC.equals(qOrderField.type())) {
                                    where.orderByDesc(qOrderField.prefix(), qOrderField.value(), qOrderField.wrapIdentifier());
                                } else {
                                    where.orderByAsc(qOrderField.prefix(), qOrderField.value(), qOrderField.wrapIdentifier());
                                }
                            }
                            if (qOrderBy.replace()) {
                                break;
                            }
                        }
                    }
                }
            }
        }

        private void doParseGroupBy(Where where, List<QField> groupedFields, List<QGroupBy> qGroupBys) {
            if (where != null) {
                GroupBy groupBy = where.groupBy();
                if (groupBy == null || groupBy.isEmpty()) {
                    if (!qGroupBys.isEmpty()) {
                        for (QGroupBy qGroupBy : qGroupBys) {
                            if (qGroupBy != null) {
                                if (ArrayUtils.isEmpty(qGroupBy.value())) {
                                    for (QField qField : groupedFields) {
                                        where.groupBy(Fields.field(qField.prefix(), qField.value()), qField.wrapIdentifier());
                                    }
                                } else {
                                    for (QField qField : qGroupBy.value()) {
                                        where.groupBy(Fields.field(qField.prefix(), qField.value()), qField.wrapIdentifier());
                                    }
                                    Cond havingCond = doParseCond(qGroupBy.having());
                                    if (havingCond != null && !havingCond.isEmpty()) {
                                        where.having(havingCond);
                                        if (qGroupBy.rollup()) {
                                            where.groupByRollup();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        private void doParseWhere(Select select, List<QField> groupedFields, List<QWhere> qWheres, List<QOrderBy> qOrderBys, List<QGroupBy> qGroupBys) {
            Where selectWhere = select.where();
            if (!qWheres.isEmpty()) {
                for (QWhere qWhere : qWheres) {
                    if (qWhere != null) {
                        Cond newCond = doParseCond(qWhere.value());
                        if (newCond != null && !newCond.isEmpty()) {
                            selectWhere.cond().cond(newCond);
                        }
                        if (qWhere.replace()) {
                            break;
                        }
                    }
                }
            }
            doParseOrderBy(selectWhere, qOrderBys);
            doParseGroupBy(selectWhere, groupedFields, qGroupBys);
            if (where != null) {
                selectWhere.where(where);
            }
        }

        public Select buildSelect() {
            ParsedQueryMetadata metadata = METADATA_CACHE.computeIfAbsent(queryClass, ParsedQueryMetadata::new);

            Select select = Select.create(this);
            // Parse From
            metadata.froms.forEach(qFrom -> doParseFrom(select, qFrom));

            // Parse Field
            List<QField> groupedFields = new ArrayList<>();
            metadata.fields.stream()
                    .filter(wrapper -> !excludedFields.contains(wrapper.field.getName()))
                    .filter(wrapper -> excludedFields.isEmpty() || !excludedFields.contains(Fields.field(wrapper.qField.prefix(), StringUtils.defaultIfBlank(wrapper.qField.alias(), wrapper.qField.value()))))
                    .forEachOrdered(wrapper -> {
                        QField qField = wrapper.qField;
                        select.field(qField.prefix(), qField.value(), qField.alias(), qField.wrapIdentifier());
                        if (qField.grouped()) {
                            groupedFields.add(qField);
                        }
                    });

            // Parse Join
            metadata.joins.forEach(qJoin -> doParseJoin(select, qJoin));

            // Parse Where
            if (where != null && replaceWhere) {
                doParseOrderBy(where, metadata.orderBys);
                doParseGroupBy(where, groupedFields, metadata.groupBys);
                select.where(where);
            } else {
                doParseWhere(select, groupedFields, metadata.wheres, metadata.orderBys, metadata.groupBys);
            }
            if (distinct || metadata.distinct) {
                return select.distinct();
            }
            return select;
        }

        public Executor<T> distinct() {
            distinct = true;
            return this;
        }

        public Executor<T> ignoreOrderBy() {
            ignoreOrderBy = true;
            return this;
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

        /**
         * @since 2.1.3
         */
        public <E> E findFirst(Class<E> beanClass) throws Exception {
            return buildSelect().findFirst(new BeanResultSetHandler<>(beanClass));
        }

        /**
         * @since 2.1.3
         */
        public <E> IResultSet<E> find(Class<E> beanClass) throws Exception {
            return buildSelect().find(new BeanResultSetHandler<>(beanClass));
        }

        /**
         * @since 2.1.3
         */
        public <E> IResultSet<E> find(Class<E> beanClass, Page page) throws Exception {
            return buildSelect().find(new BeanResultSetHandler<>(beanClass), page);
        }
    }

    /**
     * @since 2.1.4
     */
    private static class ParsedQueryMetadata {

        final List<QFrom> froms = new ArrayList<>();
        final List<QFieldWrapper> fields = new ArrayList<>();
        final List<QJoin> joins = new ArrayList<>();
        final List<QWhere> wheres = new ArrayList<>();
        final List<QOrderBy> orderBys = new ArrayList<>();
        final List<QGroupBy> groupBys = new ArrayList<>();
        final boolean distinct;

        ParsedQueryMetadata(Class<?> queryClass) {
            distinct = !ClassUtils.getAnnotation(queryClass, QDistinct.class, true, true).isEmpty();
            //
            List<QFroms> qFromsList = ClassUtils.getAnnotation(queryClass, QFroms.class, true, false);
            if (!qFromsList.isEmpty()) {
                for (QFroms qFroms : qFromsList) {
                    froms.addAll(Arrays.asList(qFroms.value()));
                }
            }
            froms.addAll(ClassUtils.getAnnotation(queryClass, QFrom.class, true, false));
            //
            ClassUtils.getFields(queryClass, true)
                    .stream()
                    .filter(field -> ClassUtils.isNormalField(field) && field.isAnnotationPresent(QField.class))
                    .map(QFieldWrapper::new)
                    .forEachOrdered(fields::add);
            //
            List<QJoins> qJoinsList = ClassUtils.getAnnotation(queryClass, QJoins.class, true, false);
            if (!qJoinsList.isEmpty()) {
                for (QJoins qJoins : qJoinsList) {
                    joins.addAll(Arrays.asList(qJoins.value()));
                }
            }
            joins.addAll(ClassUtils.getAnnotation(queryClass, QJoin.class, true, false));
            //
            wheres.addAll(ClassUtils.getAnnotation(queryClass, QWhere.class, true, false));
            orderBys.addAll(ClassUtils.getAnnotation(queryClass, QOrderBy.class, true, false));
            groupBys.addAll(ClassUtils.getAnnotation(queryClass, QGroupBy.class, true, false));
        }
    }
}