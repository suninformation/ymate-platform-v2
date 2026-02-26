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

import net.ymate.platform.commons.util.ExpressionUtils;
import net.ymate.platform.core.persistence.Fields;
import net.ymate.platform.core.persistence.LambdaUtils.SFunction;
import net.ymate.platform.core.persistence.Params;
import net.ymate.platform.core.persistence.base.EntityMeta;
import net.ymate.platform.core.persistence.base.IEntity;
import net.ymate.platform.persistence.jdbc.IDatabase;
import net.ymate.platform.persistence.jdbc.JDBC;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Update语句对象
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/12 下午6:02
 */
@SuppressWarnings("rawtypes")
public class Update extends Query<Update> {

    private final List<String> tables = new ArrayList<>();

    private final Fields fields = Fields.create();

    private final List<Join> joins = new ArrayList<>();

    private Where where;

    /**
     * @since 2.1.4
     */
    private With with;

    public static Update create() {
        IDatabase owner = JDBC.get();
        return new Update(owner, owner.getConfig().getDefaultDataSourceName());
    }

    public static Update create(String prefix, Class<? extends IEntity> entityClass, String alias) {
        IDatabase owner = JDBC.get();
        return new Update(owner, owner.getConfig().getDefaultDataSourceName()).table(prefix, entityClass, alias);
    }

    public static Update create(String prefix, Class<? extends IEntity> entityClass) {
        return create(prefix, entityClass, null);
    }

    public static Update create(Class<? extends IEntity> entityClass) {
        return create(null, entityClass, null);
    }

    public static Update create(String prefix, String tableName, String alias) {
        return create(prefix, tableName, alias, true);
    }

    public static Update create(String prefix, String tableName, String alias, boolean safePrefix) {
        IDatabase owner = JDBC.get();
        return new Update(owner, owner.getConfig().getDefaultDataSourceName(), prefix, tableName, alias, safePrefix);
    }

    public static Update create(String tableName, String alias) {
        return create((String) null, tableName, alias, true);
    }

    public static Update create(String tableName, String alias, boolean safePrefix) {
        return create((String) null, tableName, alias, safePrefix);
    }

    public static Update create(String tableName) {
        return create((String) null, tableName, null, true);
    }

    public static Update create(String tableName, boolean safePrefix) {
        return create((String) null, tableName, null, safePrefix);
    }

    public static Update create(IDatabase owner) {
        return new Update(owner, owner.getConfig().getDefaultDataSourceName());
    }

    public static Update create(IDatabase owner, String dataSourceName) {
        return new Update(owner, dataSourceName);
    }

    public static Update create(IDatabase owner, String dataSourceName, String prefix, Class<? extends IEntity> entityClass, String alias) {
        return new Update(owner, dataSourceName).table(prefix, entityClass, alias);
    }

    public static Update create(IDatabase owner, String dataSourceName, String prefix, Class<? extends IEntity> entityClass) {
        return new Update(owner, dataSourceName).table(prefix, entityClass, null);
    }

    public static Update create(IDatabase owner, String dataSourceName, Class<? extends IEntity> entityClass) {
        return new Update(owner, dataSourceName).table(entityClass, null);
    }

    public static Update create(IDatabase owner, String dataSourceName, String prefix, String tableName, String alias) {
        return new Update(owner, dataSourceName, prefix, tableName, alias, true);
    }

    public static Update create(IDatabase owner, String dataSourceName, String prefix, String tableName, String alias, boolean safePrefix) {
        return new Update(owner, dataSourceName, prefix, tableName, alias, safePrefix);
    }

    public static Update create(IDatabase owner, String dataSourceName, String tableName, String alias) {
        return new Update(owner, dataSourceName, null, tableName, alias, true);
    }

    public static Update create(IDatabase owner, String dataSourceName, String tableName, String alias, boolean safePrefix) {
        return new Update(owner, dataSourceName, null, tableName, alias, safePrefix);
    }

    public static Update create(IDatabase owner, String dataSourceName, String tableName) {
        return new Update(owner, dataSourceName, null, tableName, null, true);
    }

    public static Update create(IDatabase owner, String dataSourceName, String tableName, boolean safePrefix) {
        return new Update(owner, dataSourceName, null, tableName, null, safePrefix);
    }

    public static Update create(Query<?> query) {
        return new Update(query.owner(), query.dataSourceName());
    }

    public static Update create(Query<?> query, String prefix, String tableName, String alias, boolean safePrefix) {
        return new Update(query.owner(), query.dataSourceName(), prefix, tableName, alias, safePrefix);
    }

    public Update(IDatabase owner, String dataSourceName) {
        super(owner, dataSourceName);
    }

    public Update(IDatabase owner, String dataSourceName, String prefix, String tableName, String alias, boolean safePrefix) {
        super(owner, dataSourceName);
        //
        table(prefix, tableName, alias, safePrefix);
    }

    public Update table(Class<? extends IEntity> entityClass) {
        return table(entityClass, true);
    }

    public Update table(Class<? extends IEntity> entityClass, boolean safePrefix) {
        return table(null, buildSafeTableName(null, EntityMeta.createAndGet(entityClass), safePrefix), null, false);
    }

    public Update table(Class<? extends IEntity> entityClass, String alias) {
        return table(entityClass, alias, true);
    }

    public Update table(Class<? extends IEntity> entityClass, String alias, boolean safePrefix) {
        return table(null, buildSafeTableName(null, EntityMeta.createAndGet(entityClass), safePrefix), alias, false);
    }

    public Update table(String prefix, Class<? extends IEntity> entityClass, String alias) {
        return table(prefix, entityClass, alias, true);
    }

    public Update table(String prefix, Class<? extends IEntity> entityClass, String alias, boolean safePrefix) {
        return table(null, buildSafeTableName(prefix, EntityMeta.createAndGet(entityClass), safePrefix), alias, false);
    }

    public Update table(String tableName, String alias) {
        return table(null, tableName, alias, true);
    }

    public Update table(String tableName) {
        return table(null, tableName, null, true);
    }

    public Update table(String prefix, String from, String alias) {
        return table(prefix, from, alias, true);
    }

    public Update table(String prefix, String from, String alias, boolean safePrefix) {
        from = buildSafeTableName(prefix, from, safePrefix);
        if (StringUtils.isNotBlank(alias)) {
            from = String.format("%s %s", from, alias);
        }
        this.tables.add(from);
        return this;
    }

    /**
     * @since 2.1.4
     */
    private String doParseField(String field, boolean wrapIdentifier) {
        if (field != null && field.contains("=")) {
            String[] fieldParts = StringUtils.split(field, '=');
            if (fieldParts.length == 2) {
                return (wrapIdentifier ? wrapIdentifierField(fieldParts[0]) : fieldParts[0]) + " = " + fieldParts[1];
            }
        }
        return (wrapIdentifier ? wrapIdentifierField(field) : field) + " = ?";
    }

    public Fields fields() {
        return this.fields;
    }

    public Update field(String field) {
        return field(field, true);
    }

    public Update field(String field, boolean wrapIdentifier) {
        this.fields.add(doParseField(field, wrapIdentifier));
        return this;
    }

    public Update field(String prefix, String field) {
        return field(prefix, field, true);
    }

    public Update field(String prefix, String field, boolean wrapIdentifier) {
        this.fields.add(prefix, doParseField(field, wrapIdentifier));
        return this;
    }

    /**
     * @since 2.1.3
     * @deprecated 从2.1.4版本开始废弃，在UPDATE语句的SET子句中字段不需要别名
     */
    @Deprecated
    public Update field(String prefix, String field, String alias) {
        return field(prefix, field, alias, true);
    }

    /**
     * @since 2.1.3
     * @deprecated 从2.1.4版本开始废弃，在UPDATE语句的SET子句中字段不需要别名
     */
    @Deprecated
    public Update field(String prefix, String field, String alias, boolean wrapIdentifier) {
        this.fields.add(prefix, doParseField(field, wrapIdentifier), alias);
        return this;
    }

    // ---------- Lambda Support for FIELD ----------

    /**
     * 通过Lambda表达式设置更新字段
     *
     * @param column 方法引用
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Update实例
     * @since 2.1.4
     */
    public <T, R> Update field(SFunction<T, R> column) {
        return field(getColumnName(column), true);
    }

    /**
     * 通过Lambda表达式设置更新字段（带标识符包装控制）
     *
     * @param column         方法引用
     * @param wrapIdentifier 是否包装标识符
     * @param <T>            实体类型
     * @param <R>            返回值类型
     * @return 当前Update实例
     * @since 2.1.4
     */
    public <T, R> Update field(SFunction<T, R> column, boolean wrapIdentifier) {
        return field(getColumnName(column), wrapIdentifier);
    }

    /**
     * 通过Lambda表达式设置更新字段（带前缀）
     *
     * @param prefix 前缀
     * @param column 方法引用
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Update实例
     * @since 2.1.4
     */
    public <T, R> Update field(String prefix, SFunction<T, R> column) {
        return field(prefix, getColumnName(column), true);
    }

    /**
     * 通过Lambda表达式设置更新字段（带前缀和标识符包装控制）
     *
     * @param prefix         前缀
     * @param column         方法引用
     * @param wrapIdentifier 是否包装标识符
     * @param <T>            实体类型
     * @param <R>            返回值类型
     * @return 当前Update实例
     * @since 2.1.4
     */
    public <T, R> Update field(String prefix, SFunction<T, R> column, boolean wrapIdentifier) {
        return field(prefix, getColumnName(column), wrapIdentifier);
    }

    /**
     * 通过Lambda表达式设置更新字段并添加值
     *
     * @param column 方法引用
     * @param value  参数值
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Update实例
     * @since 2.1.4
     */
    public <T, R> Update field(SFunction<T, R> column, Object value) {
        return field(column).param(value);
    }

    /**
     * 通过Lambda表达式设置更新字段并添加值（带前缀）
     *
     * @param prefix 前缀
     * @param column 方法引用
     * @param value  参数值
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Update实例
     * @since 2.1.4
     */
    public <T, R> Update field(String prefix, SFunction<T, R> column, Object value) {
        return field(prefix, column).param(value);
    }

    /**
     * @since 2.1.3
     * @deprecated 从2.1.4版本开始废弃，在UPDATE语句的SET子句中字段不需要别名
     */
    @Deprecated
    public Update fieldAlias(String field, String alias) {
        return fieldAlias(field, alias, true);
    }

    /**
     * @since 2.1.3
     * @deprecated 从2.1.4版本开始废弃，在UPDATE语句的SET子句中字段不需要别名
     */
    @Deprecated
    public Update fieldAlias(String field, String alias, boolean wrapIdentifier) {
        this.fields.add(doParseField(field, wrapIdentifier));
        return this;
    }

    public Update field(Fields fields) {
        return field(fields, true);
    }

    public Update field(Fields fields, boolean wrapIdentifier) {
        Fields newFields = checkFieldExcluded(fields);
        newFields.fields()
                .stream()
                .map(field -> doParseField(field, wrapIdentifier))
                .forEach(this.fields::add);
        return this;
    }

    public Update field(String prefix, Fields fields) {
        return field(prefix, fields, true);
    }

    public Update field(String prefix, Fields fields, boolean wrapIdentifier) {
        checkFieldExcluded(fields)
                .fields()
                .forEach((field) -> this.fields.add(prefix, doParseField(field, wrapIdentifier)));
        return this;
    }

    public Update join(Join join) {
        joins.add(join);
        where().param(join.params());
        return this;
    }

    public Update innerJoin(Select select, Cond on) {
        return join(Join.inner(select).on(on));
    }

    /**
     * @since 2.1.4
     */
    public Update innerJoin(Select select, IConditionAppender on) {
        Cond cond = Cond.create(this);
        on.append(cond);
        return join(Join.inner(select).on(cond));
    }

    /**
     * @since 2.1.4
     */
    public Update innerJoin(Select select) {
        return join(Join.inner(select));
    }

    /**
     * @since 2.1.4
     */
    public Update crossJoin(Select select, Cond on) {
        return join(Join.cross(select).on(on));
    }

    /**
     * @since 2.1.4
     */
    public Update crossJoin(Select select, IConditionAppender on) {
        Cond cond = Cond.create(this);
        on.append(cond);
        return join(Join.cross(select).on(cond));
    }

    /**
     * @since 2.1.4
     */
    public Update crossJoin(Select select) {
        return join(Join.cross(select));
    }

    public Update leftJoin(Select select, Cond on) {
        return join(Join.left(select).on(on));
    }

    /**
     * @since 2.1.4
     */
    public Update leftJoin(Select select, IConditionAppender on) {
        Cond cond = Cond.create(this);
        on.append(cond);
        return join(Join.left(select).on(cond));
    }

    public Update rightJoin(Select select, Cond on) {
        return join(Join.right(select).on(on));
    }

    /**
     * @since 2.1.4
     */
    public Update rightJoin(Select select, IConditionAppender on) {
        Cond cond = Cond.create(this);
        on.append(cond);
        return join(Join.right(select).on(cond));
    }

    //

    public Update innerJoin(String from, Cond on) {
        return join(Join.inner(owner(), dataSourceName(), from).on(on));
    }

    /**
     * @since 2.1.4
     */
    public Update innerJoin(String from, IConditionAppender on) {
        Cond cond = Cond.create(this);
        on.append(cond);
        return join(Join.inner(owner(), dataSourceName(), from).on(cond));
    }

    /**
     * @since 2.1.4
     */
    public Update innerJoin(String from) {
        return join(Join.inner(owner(), dataSourceName(), from));
    }

    /**
     * @since 2.1.4
     */
    public Update crossJoin(String from, Cond on) {
        return join(Join.cross(owner(), dataSourceName(), from).on(on));
    }

    /**
     * @since 2.1.4
     */
    public Update crossJoin(String from, IConditionAppender on) {
        Cond cond = Cond.create(this);
        on.append(cond);
        return join(Join.cross(owner(), dataSourceName(), from).on(cond));
    }

    /**
     * @since 2.1.4
     */
    public Update crossJoin(String from) {
        return join(Join.cross(owner(), dataSourceName(), from));
    }

    public Update leftJoin(String from, Cond on) {
        return join(Join.left(owner(), dataSourceName(), from).on(on));
    }

    /**
     * @since 2.1.4
     */
    public Update leftJoin(String from, IConditionAppender on) {
        Cond cond = Cond.create(this);
        on.append(cond);
        return join(Join.left(owner(), dataSourceName(), from).on(cond));
    }

    public Update rightJoin(String from, Cond on) {
        return join(Join.right(owner(), dataSourceName(), from).on(on));
    }

    /**
     * @since 2.1.4
     */
    public Update rightJoin(String from, IConditionAppender on) {
        Cond cond = Cond.create(this);
        on.append(cond);
        return join(Join.right(owner(), dataSourceName(), from).on(cond));
    }

    //

    public Update innerJoin(String from, String alias, Cond on) {
        return join(Join.inner(owner(), dataSourceName(), from).alias(alias).on(on));
    }

    /**
     * @since 2.1.4
     */
    public Update innerJoin(String from, String alias, IConditionAppender on) {
        Cond cond = Cond.create(this);
        on.append(cond);
        return join(Join.inner(owner(), dataSourceName(), from).alias(alias).on(cond));
    }

    /**
     * @since 2.1.4
     */
    public Update crossJoin(String from, String alias, Cond on) {
        return join(Join.cross(owner(), dataSourceName(), from).alias(alias).on(on));
    }

    /**
     * @since 2.1.4
     */
    public Update crossJoin(String from, String alias, IConditionAppender on) {
        Cond cond = Cond.create(this);
        on.append(cond);
        return join(Join.cross(owner(), dataSourceName(), from).alias(alias).on(cond));
    }

    public Update leftJoin(String from, String alias, Cond on) {
        return join(Join.left(owner(), dataSourceName(), from).alias(alias).on(on));
    }

    /**
     * @since 2.1.4
     */
    public Update leftJoin(String from, String alias, IConditionAppender on) {
        Cond cond = Cond.create(this);
        on.append(cond);
        return join(Join.left(owner(), dataSourceName(), from).alias(alias).on(cond));
    }

    public Update rightJoin(String from, String alias, Cond on) {
        return join(Join.right(owner(), dataSourceName(), from).alias(alias).on(on));
    }

    /**
     * @since 2.1.4
     */
    public Update rightJoin(String from, String alias, IConditionAppender on) {
        Cond cond = Cond.create(this);
        on.append(cond);
        return join(Join.right(owner(), dataSourceName(), from).alias(alias).on(cond));
    }

    //

    public Update innerJoin(String prefix, String from, String alias, Cond on) {
        return join(Join.inner(owner(), dataSourceName(), prefix, from).alias(alias).on(on));
    }

    /**
     * @since 2.1.4
     */
    public Update innerJoin(String prefix, String from, String alias, IConditionAppender on) {
        Cond cond = Cond.create(this);
        on.append(cond);
        return join(Join.inner(owner(), dataSourceName(), prefix, from).alias(alias).on(cond));
    }

    /**
     * @since 2.1.4
     */
    public Update innerJoin(String prefix, String from, String alias) {
        return join(Join.inner(owner(), dataSourceName(), prefix, from).alias(alias));
    }

    /**
     * @since 2.1.4
     */
    public Update crossJoin(String prefix, String from, String alias, Cond on) {
        return join(Join.cross(owner(), dataSourceName(), prefix, from).alias(alias).on(on));
    }

    /**
     * @since 2.1.4
     */
    public Update crossJoin(String prefix, String from, String alias, IConditionAppender on) {
        Cond cond = Cond.create(this);
        on.append(cond);
        return join(Join.cross(owner(), dataSourceName(), prefix, from).alias(alias).on(cond));
    }

    /**
     * @since 2.1.4
     */
    public Update crossJoin(String prefix, String from, String alias) {
        return join(Join.cross(owner(), dataSourceName(), prefix, from).alias(alias));
    }

    public Update leftJoin(String prefix, String from, String alias, Cond on) {
        return join(Join.left(owner(), dataSourceName(), prefix, from).alias(alias).on(on));
    }

    /**
     * @since 2.1.4
     */
    public Update leftJoin(String prefix, String from, String alias, IConditionAppender on) {
        Cond cond = Cond.create(this);
        on.append(cond);
        return join(Join.left(owner(), dataSourceName(), prefix, from).alias(alias).on(cond));
    }

    public Update rightJoin(String prefix, String from, String alias, Cond on) {
        return join(Join.right(owner(), dataSourceName(), prefix, from).alias(alias).on(on));
    }

    /**
     * @since 2.1.4
     */
    public Update rightJoin(String prefix, String from, String alias, IConditionAppender on) {
        Cond cond = Cond.create(this);
        on.append(cond);
        return join(Join.right(owner(), dataSourceName(), prefix, from).alias(alias).on(cond));
    }

    //

    public Update innerJoin(String prefix, String from, String alias, Cond on, boolean safePrefix) {
        return join(Join.inner(owner(), dataSourceName(), prefix, from, safePrefix).alias(alias).on(on));
    }

    /**
     * @since 2.1.4
     */
    public Update innerJoin(String prefix, String from, String alias, IConditionAppender on, boolean safePrefix) {
        Cond cond = Cond.create(this);
        on.append(cond);
        return join(Join.inner(owner(), dataSourceName(), prefix, from, safePrefix).alias(alias).on(cond));
    }

    /**
     * @since 2.1.4
     */
    public Update innerJoin(String prefix, String from, String alias, boolean safePrefix) {
        return join(Join.inner(owner(), dataSourceName(), prefix, from, safePrefix).alias(alias));
    }

    /**
     * @since 2.1.4
     */
    public Update crossJoin(String prefix, String from, String alias, Cond on, boolean safePrefix) {
        return join(Join.cross(owner(), dataSourceName(), prefix, from, safePrefix).alias(alias).on(on));
    }

    /**
     * @since 2.1.4
     */
    public Update crossJoin(String prefix, String from, String alias, IConditionAppender on, boolean safePrefix) {
        Cond cond = Cond.create(this);
        on.append(cond);
        return join(Join.cross(owner(), dataSourceName(), prefix, from, safePrefix).alias(alias).on(cond));
    }

    /**
     * @since 2.1.4
     */
    public Update crossJoin(String prefix, String from, String alias, boolean safePrefix) {
        return join(Join.cross(owner(), dataSourceName(), prefix, from, safePrefix).alias(alias));
    }

    public Update leftJoin(String prefix, String from, String alias, Cond on, boolean safePrefix) {
        return join(Join.left(owner(), dataSourceName(), prefix, from, safePrefix).alias(alias).on(on));
    }

    /**
     * @since 2.1.4
     */
    public Update leftJoin(String prefix, String from, String alias, IConditionAppender on, boolean safePrefix) {
        Cond cond = Cond.create(this);
        on.append(cond);
        return join(Join.left(owner(), dataSourceName(), prefix, from, safePrefix).alias(alias).on(cond));
    }

    public Update rightJoin(String prefix, String from, String alias, Cond on, boolean safePrefix) {
        return join(Join.right(owner(), dataSourceName(), prefix, from, safePrefix).alias(alias).on(on));
    }

    /**
     * @since 2.1.4
     */
    public Update rightJoin(String prefix, String from, String alias, IConditionAppender on, boolean safePrefix) {
        Cond cond = Cond.create(this);
        on.append(cond);
        return join(Join.right(owner(), dataSourceName(), prefix, from, safePrefix).alias(alias).on(cond));
    }

    // ---------- Lambda Support for JOIN ----------

    /**
     * 内连接（基于实体类，使用Lambda表达式指定连接条件）
     *
     * @param entityClass 实体类
     * @param alias       别名
     * @param columnOne   第一个连接字段的方法引用
     * @param columnTwo   第二个连接字段的方法引用
     * @param <T>         第一个实体类型
     * @param <U>         第二个实体类型
     * @param <R>         返回值类型
     * @return 当前Update实例
     * @since 2.1.4
     */
    public <T, U, R> Update innerJoin(Class<? extends IEntity<?>> entityClass, String alias, SFunction<T, R> columnOne, SFunction<U, R> columnTwo) {
        return innerJoin(entityClass, alias, columnOne, columnTwo, true);
    }

    /**
     * 内连接（基于实体类，使用Lambda表达式指定连接条件）
     *
     * @param entityClass    实体类
     * @param alias          别名
     * @param columnOne      第一个连接字段的方法引用
     * @param columnTwo      第二个连接字段的方法引用
     * @param wrapIdentifier 是否包装标识符
     * @param <T>            第一个实体类型
     * @param <U>            第二个实体类型
     * @param <R>            返回值类型
     * @return 当前Update实例
     * @since 2.1.4
     */
    public <T, U, R> Update innerJoin(Class<? extends IEntity<?>> entityClass, String alias, SFunction<T, R> columnOne, SFunction<U, R> columnTwo, boolean wrapIdentifier) {
        Join join = Join.inner(this, entityClass, alias);
        if (wrapIdentifier) {
            join.onEqWrap(columnOne, columnTwo);
        } else {
            join.onEq(columnOne, columnTwo);
        }
        return join(join);
    }

    /**
     * 左连接（基于实体类，使用Lambda表达式指定连接条件）
     *
     * @param entityClass 实体类
     * @param alias       别名
     * @param columnOne   第一个连接字段的方法引用
     * @param columnTwo   第二个连接字段的方法引用
     * @param <T>         第一个类型
     * @param <U>         第二个类型
     * @param <R>         返回值类型
     * @return 当前Update实例
     * @since 2.1.4
     */
    public <T, U, R> Update leftJoin(Class<? extends IEntity<?>> entityClass, String alias, SFunction<T, R> columnOne, SFunction<U, R> columnTwo) {
        return leftJoin(entityClass, alias, columnOne, columnTwo, true);
    }

    /**
     * 左连接（基于实体类，使用Lambda表达式指定连接条件）
     *
     * @param entityClass    实体类
     * @param alias          别名
     * @param columnOne      第一个连接字段的方法引用
     * @param columnTwo      第二个连接字段的方法引用
     * @param wrapIdentifier 是否包装标识符
     * @param <T>            第一个类型
     * @param <U>            第二个类型
     * @param <R>            返回值类型
     * @return 当前Update实例
     * @since 2.1.4
     */
    public <T, U, R> Update leftJoin(Class<? extends IEntity<?>> entityClass, String alias, SFunction<T, R> columnOne, SFunction<U, R> columnTwo, boolean wrapIdentifier) {
        Join join = Join.left(this, entityClass, alias);
        if (wrapIdentifier) {
            join.onEqWrap(columnOne, columnTwo);
        } else {
            join.onEq(columnOne, columnTwo);
        }
        return join(join);
    }

    /**
     * 右连接（基于实体类，使用Lambda表达式指定连接条件）
     *
     * @param entityClass 实体类
     * @param alias       别名
     * @param columnOne   第一个连接字段的方法引用
     * @param columnTwo   第二个连接字段的方法引用
     * @param <T>         第一个类型
     * @param <U>         第二个类型
     * @param <R>         返回值类型
     * @return 当前Update实例
     * @since 2.1.4
     */
    public <T, U, R> Update rightJoin(Class<? extends IEntity<?>> entityClass, String alias, SFunction<T, R> columnOne, SFunction<U, R> columnTwo) {
        return rightJoin(entityClass, alias, columnOne, columnTwo, true);
    }

    /**
     * 右连接（基于实体类，使用Lambda表达式指定连接条件）
     *
     * @param entityClass    实体类
     * @param alias          别名
     * @param columnOne      第一个连接字段的方法引用
     * @param columnTwo      第二个连接字段的方法引用
     * @param wrapIdentifier 是否包装标识符
     * @param <T>            第一个类型
     * @param <U>            第二个类型
     * @param <R>            返回值类型
     * @return 当前Update实例
     * @since 2.1.4
     */
    public <T, U, R> Update rightJoin(Class<? extends IEntity<?>> entityClass, String alias, SFunction<T, R> columnOne, SFunction<U, R> columnTwo, boolean wrapIdentifier) {
        Join join = Join.right(this, entityClass, alias);
        if (wrapIdentifier) {
            join.onEqWrap(columnOne, columnTwo);
        } else {
            join.onEq(columnOne, columnTwo);
        }
        return join(join);
    }

    /**
     * 交叉连接（基于实体类，使用Lambda表达式指定连接条件）
     *
     * @param entityClass 实体类
     * @param alias       别名
     * @param columnOne   第一个连接字段的方法引用
     * @param columnTwo   第二个连接字段的方法引用
     * @param <T>         第一个类型
     * @param <U>         第二个类型
     * @param <R>         返回值类型
     * @return 当前Update实例
     * @since 2.1.4
     */
    public <T, U, R> Update crossJoin(Class<? extends IEntity<?>> entityClass, String alias, SFunction<T, R> columnOne, SFunction<U, R> columnTwo) {
        return crossJoin(entityClass, alias, columnOne, columnTwo, true);
    }

    /**
     * 交叉连接（基于实体类，使用Lambda表达式指定连接条件）
     *
     * @param entityClass    实体类
     * @param alias          别名
     * @param columnOne      第一个连接字段的方法引用
     * @param columnTwo      第二个连接字段的方法引用
     * @param wrapIdentifier 是否包装标识符
     * @param <T>            第一个类型
     * @param <U>            第二个类型
     * @param <R>            返回值类型
     * @return 当前Update实例
     * @since 2.1.4
     */
    public <T, U, R> Update crossJoin(Class<? extends IEntity<?>> entityClass, String alias, SFunction<T, R> columnOne, SFunction<U, R> columnTwo, boolean wrapIdentifier) {
        Join join = Join.cross(this, entityClass, alias);
        if (wrapIdentifier) {
            join.onEqWrap(columnOne, columnTwo);
        } else {
            join.onEq(columnOne, columnTwo);
        }
        return join(join);
    }

    /**
     * 内连接（基于实体类，使用带前缀的Lambda表达式指定连接条件）
     *
     * @param entityClass 实体类
     * @param alias       别名
     * @param prefixOne   第一个表前缀
     * @param columnOne   第一个连接字段的方法引用
     * @param prefixTwo   第二个表前缀
     * @param columnTwo   第二个连接字段的方法引用
     * @param <T>         第一个实体类型
     * @param <U>         第二个实体类型
     * @param <R>         返回值类型
     * @return 当前Update实例
     * @since 2.1.4
     */
    public <T, U, R> Update innerJoin(Class<? extends IEntity<?>> entityClass, String alias, String prefixOne, SFunction<T, R> columnOne, String prefixTwo, SFunction<U, R> columnTwo) {
        return innerJoin(entityClass, alias, prefixOne, columnOne, prefixTwo, columnTwo, true);
    }

    /**
     * 内连接（基于实体类，使用带前缀的Lambda表达式指定连接条件）
     *
     * @param entityClass    实体类
     * @param alias          别名
     * @param prefixOne      第一个表前缀
     * @param columnOne      第一个连接字段的方法引用
     * @param prefixTwo      第二个表前缀
     * @param columnTwo      第二个连接字段的方法引用
     * @param wrapIdentifier 是否包装标识符
     * @param <T>            第一个实体类型
     * @param <U>            第二个实体类型
     * @param <R>            返回值类型
     * @return 当前Update实例
     * @since 2.1.4
     */
    public <T, U, R> Update innerJoin(Class<? extends IEntity<?>> entityClass, String alias, String prefixOne, SFunction<T, R> columnOne, String prefixTwo, SFunction<U, R> columnTwo, boolean wrapIdentifier) {
        Join join = Join.inner(this, entityClass, alias);
        if (wrapIdentifier) {
            join.onEqWrap(prefixOne, columnOne, prefixTwo, columnTwo);
        } else {
            join.onEq(prefixOne, columnOne, prefixTwo, columnTwo);
        }
        return join(join);
    }

    /**
     * 左连接（基于实体类，使用带前缀的Lambda表达式指定连接条件）
     *
     * @param entityClass 实体类
     * @param alias       别名
     * @param prefixOne   第一个表前缀
     * @param columnOne   第一个连接字段的方法引用
     * @param prefixTwo   第二个表前缀
     * @param columnTwo   第二个连接字段的方法引用
     * @param <T>         第一个实体类型
     * @param <U>         第二个实体类型
     * @param <R>         返回值类型
     * @return 当前Update实例
     * @since 2.1.4
     */
    public <T, U, R> Update leftJoin(Class<? extends IEntity<?>> entityClass, String alias, String prefixOne, SFunction<T, R> columnOne, String prefixTwo, SFunction<U, R> columnTwo) {
        return leftJoin(entityClass, alias, prefixOne, columnOne, prefixTwo, columnTwo, true);
    }

    /**
     * 左连接（基于实体类，使用带前缀的Lambda表达式指定连接条件）
     *
     * @param entityClass    实体类
     * @param alias          别名
     * @param prefixOne      第一个表前缀
     * @param columnOne      第一个连接字段的方法引用
     * @param prefixTwo      第二个表前缀
     * @param columnTwo      第二个连接字段的方法引用
     * @param wrapIdentifier 是否包装标识符
     * @param <T>            第一个实体类型
     * @param <U>            第二个实体类型
     * @param <R>            返回值类型
     * @return 当前Update实例
     * @since 2.1.4
     */
    public <T, U, R> Update leftJoin(Class<? extends IEntity<?>> entityClass, String alias, String prefixOne, SFunction<T, R> columnOne, String prefixTwo, SFunction<U, R> columnTwo, boolean wrapIdentifier) {
        Join join = Join.left(this, entityClass, alias);
        if (wrapIdentifier) {
            join.onEqWrap(prefixOne, columnOne, prefixTwo, columnTwo);
        } else {
            join.onEq(prefixOne, columnOne, prefixTwo, columnTwo);
        }
        return join(join);
    }

    /**
     * 右连接（基于实体类，使用带前缀的Lambda表达式指定连接条件）
     *
     * @param entityClass 实体类
     * @param alias       别名
     * @param prefixOne   第一个表前缀
     * @param columnOne   第一个连接字段的方法引用
     * @param prefixTwo   第二个表前缀
     * @param columnTwo   第二个连接字段的方法引用
     * @param <T>         第一个实体类型
     * @param <U>         第二个实体类型
     * @param <R>         返回值类型
     * @return 当前Update实例
     * @since 2.1.4
     */
    public <T, U, R> Update rightJoin(Class<? extends IEntity<?>> entityClass, String alias, String prefixOne, SFunction<T, R> columnOne, String prefixTwo, SFunction<U, R> columnTwo) {
        return rightJoin(entityClass, alias, prefixOne, columnOne, prefixTwo, columnTwo, true);
    }

    /**
     * 右连接（基于实体类，使用带前缀的Lambda表达式指定连接条件）
     *
     * @param entityClass    实体类
     * @param alias          别名
     * @param prefixOne      第一个表前缀
     * @param columnOne      第一个连接字段的方法引用
     * @param prefixTwo      第二个表前缀
     * @param columnTwo      第二个连接字段的方法引用
     * @param wrapIdentifier 是否包装标识符
     * @param <T>            第一个实体类型
     * @param <U>            第二个实体类型
     * @param <R>            返回值类型
     * @return 当前Update实例
     * @since 2.1.4
     */
    public <T, U, R> Update rightJoin(Class<? extends IEntity<?>> entityClass, String alias, String prefixOne, SFunction<T, R> columnOne, String prefixTwo, SFunction<U, R> columnTwo, boolean wrapIdentifier) {
        Join join = Join.right(this, entityClass, alias);
        if (wrapIdentifier) {
            join.onEqWrap(prefixOne, columnOne, prefixTwo, columnTwo);
        } else {
            join.onEq(prefixOne, columnOne, prefixTwo, columnTwo);
        }
        return join(join);
    }

    /**
     * 交叉连接（基于实体类，使用带前缀的Lambda表达式指定连接条件）
     *
     * @param entityClass 实体类
     * @param alias       别名
     * @param prefixOne   第一个表前缀
     * @param columnOne   第一个连接字段的方法引用
     * @param prefixTwo   第二个表前缀
     * @param columnTwo   第二个连接字段的方法引用
     * @param <T>         第一个实体类型
     * @param <U>         第二个实体类型
     * @param <R>         返回值类型
     * @return 当前Update实例
     * @since 2.1.4
     */
    public <T, U, R> Update crossJoin(Class<? extends IEntity<?>> entityClass, String alias, String prefixOne, SFunction<T, R> columnOne, String prefixTwo, SFunction<U, R> columnTwo) {
        return crossJoin(entityClass, alias, prefixOne, columnOne, prefixTwo, columnTwo, true);
    }

    /**
     * 交叉连接（基于实体类，使用带前缀的Lambda表达式指定连接条件）
     *
     * @param entityClass    实体类
     * @param alias          别名
     * @param prefixOne      第一个表前缀
     * @param columnOne      第一个连接字段的方法引用
     * @param prefixTwo      第二个表前缀
     * @param columnTwo      第二个连接字段的方法引用
     * @param wrapIdentifier 是否包装标识符
     * @param <T>            第一个实体类型
     * @param <U>            第二个实体类型
     * @param <R>            返回值类型
     * @return 当前Update实例
     * @since 2.1.4
     */
    public <T, U, R> Update crossJoin(Class<? extends IEntity<?>> entityClass, String alias, String prefixOne, SFunction<T, R> columnOne, String prefixTwo, SFunction<U, R> columnTwo, boolean wrapIdentifier) {
        Join join = Join.cross(this, entityClass, alias);
        if (wrapIdentifier) {
            join.onEqWrap(prefixOne, columnOne, prefixTwo, columnTwo);
        } else {
            join.onEq(prefixOne, columnOne, prefixTwo, columnTwo);
        }
        return join(join);
    }

    /**
     * 内连接（基于实体类）
     *
     * @param entityClass 实体类
     * @param alias       别名
     * @param on          连接条件
     * @return 当前Update实例
     * @since 2.1.4
     */
    public Update innerJoin(Class<? extends IEntity<?>> entityClass, String alias, Cond on) {
        return join(Join.inner(this, entityClass, alias).on(on));
    }

    /**
     * @since 2.1.4
     */
    public Update innerJoin(Class<? extends IEntity<?>> entityClass, String alias, IConditionAppender on) {
        Cond cond = Cond.create(this);
        on.append(cond);
        return join(Join.inner(this, entityClass, alias).on(cond));
    }

    /**
     * 左连接（基于实体类）
     *
     * @param entityClass 实体类
     * @param alias       别名
     * @param on          连接条件
     * @return 当前Update实例
     * @since 2.1.4
     */
    public Update leftJoin(Class<? extends IEntity<?>> entityClass, String alias, Cond on) {
        return join(Join.left(this, entityClass, alias).on(on));
    }

    /**
     * @since 2.1.4
     */
    public Update leftJoin(Class<? extends IEntity<?>> entityClass, String alias, IConditionAppender on) {
        Cond cond = Cond.create(this);
        on.append(cond);
        return join(Join.left(this, entityClass, alias).on(cond));
    }

    /**
     * 右连接（基于实体类）
     *
     * @param entityClass 实体类
     * @param alias       别名
     * @param on          连接条件
     * @return 当前Update实例
     * @since 2.1.4
     */
    public Update rightJoin(Class<? extends IEntity<?>> entityClass, String alias, Cond on) {
        return join(Join.right(this, entityClass, alias).on(on));
    }

    /**
     * @since 2.1.4
     */
    public Update rightJoin(Class<? extends IEntity<?>> entityClass, String alias, IConditionAppender on) {
        Cond cond = Cond.create(this);
        on.append(cond);
        return join(Join.right(this, entityClass, alias).on(cond));
    }

    /**
     * 交叉连接（基于实体类）
     *
     * @param entityClass 实体类
     * @param alias       别名
     * @param on          连接条件
     * @return 当前Update实例
     * @since 2.1.4
     */
    public Update crossJoin(Class<? extends IEntity<?>> entityClass, String alias, Cond on) {
        return join(Join.cross(this, entityClass, alias).on(on));
    }

    /**
     * @since 2.1.4
     */
    public Update crossJoin(Class<? extends IEntity<?>> entityClass, String alias, IConditionAppender on) {
        Cond cond = Cond.create(this);
        on.append(cond);
        return join(Join.cross(this, entityClass, alias).on(cond));
    }

    public Update where(Where where) {
        where().where(where);
        return this;
    }

    public Params params() {
        return where().params();
    }

    public Update param(Object param) {
        where().param(param);
        return this;
    }

    public Update param(Params params) {
        where().param(params);
        return this;
    }

    public Where where() {
        if (this.where == null) {
            this.where = Where.create(owner());
        }
        return where;
    }

    public Update where(Cond cond) {
        where().cond().cond(cond);
        return this;
    }

    /**
     * 通过条件构建器创建更新条件
     *
     * @param appender 条件构建器
     * @return 当前Update实例
     * @since 2.1.4
     */
    public Update where(IConditionAppender appender) {
        Cond cond = Cond.create(this);
        appender.append(cond);
        return where(cond);
    }

    /**
     * @since 2.1.4
     */
    public Update with(With with) {
        this.with = with;
        where().param(with.params());
        return this;
    }

    @Override
    public String toString() {
        ExpressionUtils expression = ExpressionUtils.bind(getExpressionStr("UPDATE ${tableNames} ${joins} SET ${fields} ${where}"));
        if (queryHandler() != null) {
            queryHandler().beforeBuild(expression, this);
        }
        List<String> variables = expression.getVariables();
        //
        expression.set("tableNames", StringUtils.join(tables, LINE_END_FLAG));
        if (!fields.isEmpty()) {
            expression.set("fields", StringUtils.join(fields.fields(), ", "));
        }
        if (variables.contains("joins")) {
            expression.set("joins", StringUtils.join(joins, StringUtils.SPACE));
        }
        if (where != null && variables.contains("where")) {
            expression.set("where", where.toString());
        }
        if (queryHandler() != null) {
            queryHandler().afterBuild(expression, this);
        }
        String resultStr = StringUtils.trimToEmpty(expression.clean().getResult());
        if (with != null) {
            resultStr = String.format("%s %s", with.toSQL(), resultStr);
        }
        return resultStr;
    }

    public SQL toSQL() {
        return SQL.create(this);
    }

    public int execute() throws Exception {
        return toSQL().execute(dataSourceName());
    }
}
