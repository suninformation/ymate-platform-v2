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
 * DELETE语句对象
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/12 下午6:03
 */
@SuppressWarnings("rawtypes")
public class Delete extends Query<Delete> {

    private final List<String> froms = new ArrayList<>();

    private final Fields fields = Fields.create();

    private final List<Join> joins = new ArrayList<>();

    private Where where;

    /**
     * @since 2.1.4
     */
    private With with;

    public static Delete create() {
        IDatabase owner = JDBC.get();
        return new Delete(owner, owner.getConfig().getDefaultDataSourceName());
    }

    public static Delete create(Class<? extends IEntity> entityClass) {
        return create(null, entityClass, null);
    }

    public static Delete create(String prefix, Class<? extends IEntity> entityClass) {
        return create(prefix, entityClass, null);
    }

    public static Delete create(Class<? extends IEntity> entityClass, String alias) {
        return create(null, entityClass, alias);
    }

    public static Delete create(String prefix, Class<? extends IEntity> entityClass, String alias) {
        IDatabase owner = JDBC.get();
        return new Delete(owner, owner.getConfig().getDefaultDataSourceName()).from(prefix, entityClass, alias);
    }

    public static Delete create(String prefix, String tableName, String alias) {
        IDatabase owner = JDBC.get();
        return new Delete(owner, owner.getConfig().getDefaultDataSourceName(), prefix, tableName, alias, true);
    }

    public static Delete create(String tableName, String alias) {
        return create((String) null, tableName, alias);
    }

    public static Delete create(String tableName, String alias, boolean safePrefix) {
        IDatabase owner = JDBC.get();
        return new Delete(owner, owner.getConfig().getDefaultDataSourceName(), null, tableName, alias, safePrefix);
    }

    public static Delete create(String tableName) {
        return create((String) null, tableName, null);
    }

    public static Delete create(String tableName, boolean safePrefix) {
        return create(tableName, null, safePrefix);
    }

    public static Delete create(Select select) {
        Delete target = new Delete(select.owner(), select.dataSourceName(), null, select.toString(), null, false);
        target.where().param(select.params());
        return target;
    }

    public static Delete create(IDatabase owner) {
        return new Delete(owner, owner.getConfig().getDefaultDataSourceName());
    }

    public static Delete create(IDatabase owner, String dataSourceName) {
        return new Delete(owner, dataSourceName);
    }

    public static Delete create(IDatabase owner, String dataSourceName, Class<? extends IEntity> entityClass) {
        return new Delete(owner, dataSourceName).from(entityClass);
    }

    public static Delete create(IDatabase owner, String dataSourceName, String prefix, Class<? extends IEntity> entityClass) {
        return new Delete(owner, dataSourceName).from(prefix, entityClass, null);
    }

    public static Delete create(IDatabase owner, String dataSourceName, Class<? extends IEntity> entityClass, String alias) {
        return new Delete(owner, dataSourceName).from(null, entityClass, alias);
    }

    public static Delete create(IDatabase owner, String dataSourceName, String prefix, Class<? extends IEntity> entityClass, String alias) {
        return new Delete(owner, dataSourceName).from(prefix, entityClass, alias);
    }

    public static Delete create(IDatabase owner, String dataSourceName, String prefix, String tableName, String alias) {
        return new Delete(owner, dataSourceName, prefix, tableName, alias, true);
    }

    public static Delete create(IDatabase owner, String dataSourceName, String tableName, String alias) {
        return new Delete(owner, dataSourceName, null, tableName, alias, true);
    }

    public static Delete create(IDatabase owner, String dataSourceName, String tableName, String alias, boolean safePrefix) {
        return new Delete(owner, dataSourceName, null, tableName, alias, safePrefix);
    }

    public static Delete create(IDatabase owner, String dataSourceName, String tableName) {
        return new Delete(owner, dataSourceName, null, tableName, null, true);
    }

    public static Delete create(IDatabase owner, String dataSourceName, String tableName, boolean safePrefix) {
        return new Delete(owner, dataSourceName, null, tableName, null, safePrefix);
    }

    public static Delete create(Query<?> query) {
        return new Delete(query.owner(), query.dataSourceName());
    }

    public static Delete create(Query<?> query, String prefix, String from, String alias, boolean safePrefix) {
        return new Delete(query.owner(), query.dataSourceName(), prefix, from, alias, safePrefix);
    }

    public Delete(IDatabase owner, String dataSourceName) {
        super(owner, dataSourceName);
    }

    public Delete(IDatabase owner, String dataSourceName, String prefix, String from, String alias, boolean safePrefix) {
        super(owner, dataSourceName);
        //
        from(prefix, from, alias, safePrefix);
    }

    public Delete from(Class<? extends IEntity> entityClass) {
        return from(entityClass, true);
    }

    public Delete from(Class<? extends IEntity> entityClass, boolean safePrefix) {
        return from(null, buildSafeTableName(null, EntityMeta.createAndGet(entityClass), safePrefix), null);
    }

    public Delete from(Class<? extends IEntity> entityClass, String alias) {
        return from(entityClass, alias, true);
    }

    public Delete from(Class<? extends IEntity> entityClass, String alias, boolean safePrefix) {
        return from(null, buildSafeTableName(null, EntityMeta.createAndGet(entityClass), safePrefix), alias);
    }

    public Delete from(String prefix, Class<? extends IEntity> entityClass, String alias) {
        return from(prefix, entityClass, alias, true);
    }

    public Delete from(String prefix, Class<? extends IEntity> entityClass, String alias, boolean safePrefix) {
        return from(null, buildSafeTableName(prefix, EntityMeta.createAndGet(entityClass), safePrefix), alias);
    }

    public Delete from(Select select) {
        Delete target = from(null, select.toString(), null);
        target.where().param(select.params());
        return target;
    }

    public Delete from(String tableName, String alias) {
        return from(null, buildSafeTableName(null, tableName, true), alias);
    }

    public Delete from(String tableName) {
        return from(null, buildSafeTableName(null, tableName, true), null);
    }

    public Delete from(String prefix, String from, String alias) {
        return from(prefix, from, alias, false);
    }

    public Delete from(String prefix, String from, String alias, boolean safePrefix) {
        from = buildSafeTableName(prefix, from, safePrefix);
        if (StringUtils.isNotBlank(alias)) {
            from = from.concat(StringUtils.SPACE).concat(alias);
        }
        this.froms.add(from);
        return this;
    }

    public Delete table(String tableName) {
        this.fields.add(tableName);
        return this;
    }

    public Delete table(String prefix, String tableName) {
        this.fields.add(prefix, tableName);
        return this;
    }

    public Delete table(Class<? extends IEntity> entityClass) {
        return table(null, entityClass, true);
    }

    public Delete table(String prefix, Class<? extends IEntity> entityClass) {
        return table(prefix, entityClass, true);
    }

    public Delete table(String prefix, Class<? extends IEntity> entityClass, boolean safePrefix) {
        this.fields.add(buildSafeTableName(prefix, EntityMeta.createAndGet(entityClass), safePrefix));
        return this;
    }

    public Delete join(Join join) {
        joins.add(join);
        where().param(join.params());
        return this;
    }

    public Delete innerJoin(Select select, Cond on) {
        return join(Join.inner(select).on(on));
    }

    /**
     * @since 2.1.4
     */
    public Delete innerJoin(Select select, IConditionAppender on) {
        Cond cond = Cond.create(this);
        on.append(cond);
        return join(Join.inner(select).on(cond));
    }

    /**
     * @since 2.1.4
     */
    public Delete innerJoin(Select select) {
        return join(Join.inner(select));
    }

    /**
     * @since 2.1.4
     */
    public Delete crossJoin(Select select, Cond on) {
        return join(Join.cross(select).on(on));
    }

    /**
     * @since 2.1.4
     */
    public Delete crossJoin(Select select, IConditionAppender on) {
        Cond cond = Cond.create(this);
        on.append(cond);
        return join(Join.cross(select).on(cond));
    }

    /**
     * @since 2.1.4
     */
    public Delete crossJoin(Select select) {
        return join(Join.cross(select));
    }

    public Delete leftJoin(Select select, Cond on) {
        return join(Join.left(select).on(on));
    }

    /**
     * @since 2.1.4
     */
    public Delete leftJoin(Select select, IConditionAppender on) {
        Cond cond = Cond.create(this);
        on.append(cond);
        return join(Join.left(select).on(cond));
    }

    public Delete rightJoin(Select select, Cond on) {
        return join(Join.right(select).on(on));
    }

    /**
     * @since 2.1.4
     */
    public Delete rightJoin(Select select, IConditionAppender on) {
        Cond cond = Cond.create(this);
        on.append(cond);
        return join(Join.right(select).on(cond));
    }

    //

    public Delete innerJoin(String from, Cond on) {
        return join(Join.inner(owner(), dataSourceName(), from).on(on));
    }

    /**
     * @since 2.1.4
     */
    public Delete innerJoin(String from, IConditionAppender on) {
        Cond cond = Cond.create(this);
        on.append(cond);
        return join(Join.inner(owner(), dataSourceName(), from).on(cond));
    }

    /**
     * @since 2.1.4
     */
    public Delete innerJoin(String from) {
        return join(Join.inner(owner(), dataSourceName(), from));
    }

    /**
     * @since 2.1.4
     */
    public Delete crossJoin(String from, Cond on) {
        return join(Join.cross(owner(), dataSourceName(), from).on(on));
    }

    /**
     * @since 2.1.4
     */
    public Delete crossJoin(String from, IConditionAppender on) {
        Cond cond = Cond.create(this);
        on.append(cond);
        return join(Join.cross(owner(), dataSourceName(), from).on(cond));
    }

    /**
     * @since 2.1.4
     */
    public Delete crossJoin(String from) {
        return join(Join.cross(owner(), dataSourceName(), from));
    }

    public Delete leftJoin(String from, Cond on) {
        return join(Join.left(owner(), dataSourceName(), from).on(on));
    }

    /**
     * @since 2.1.4
     */
    public Delete leftJoin(String from, IConditionAppender on) {
        Cond cond = Cond.create(this);
        on.append(cond);
        return join(Join.left(owner(), dataSourceName(), from).on(cond));
    }

    public Delete rightJoin(String from, Cond on) {
        return join(Join.right(owner(), dataSourceName(), from).on(on));
    }

    /**
     * @since 2.1.4
     */
    public Delete rightJoin(String from, IConditionAppender on) {
        Cond cond = Cond.create(this);
        on.append(cond);
        return join(Join.right(owner(), dataSourceName(), from).on(cond));
    }

    //

    public Delete innerJoin(String from, String alias, Cond on) {
        return join(Join.inner(owner(), dataSourceName(), from).alias(alias).on(on));
    }

    /**
     * @since 2.1.4
     */
    public Delete innerJoin(String from, String alias, IConditionAppender on) {
        Cond cond = Cond.create(this);
        on.append(cond);
        return join(Join.inner(owner(), dataSourceName(), from).alias(alias).on(cond));
    }

    /**
     * @since 2.1.4
     */
    public Delete innerJoin(String from, String alias) {
        return join(Join.inner(owner(), dataSourceName(), from).alias(alias));
    }

    /**
     * @since 2.1.4
     */
    public Delete crossJoin(String from, String alias, Cond on) {
        return join(Join.cross(owner(), dataSourceName(), from).alias(alias).on(on));
    }

    /**
     * @since 2.1.4
     */
    public Delete crossJoin(String from, String alias, IConditionAppender on) {
        Cond cond = Cond.create(this);
        on.append(cond);
        return join(Join.cross(owner(), dataSourceName(), from).alias(alias).on(cond));
    }

    /**
     * @since 2.1.4
     */
    public Delete crossJoin(String from, String alias) {
        return join(Join.cross(owner(), dataSourceName(), from).alias(alias));
    }

    public Delete leftJoin(String from, String alias, Cond on) {
        return join(Join.left(owner(), dataSourceName(), from).alias(alias).on(on));
    }

    /**
     * @since 2.1.4
     */
    public Delete leftJoin(String from, String alias, IConditionAppender on) {
        Cond cond = Cond.create(this);
        on.append(cond);
        return join(Join.left(owner(), dataSourceName(), from).alias(alias).on(cond));
    }

    public Delete rightJoin(String from, String alias, Cond on) {
        return join(Join.right(owner(), dataSourceName(), from).alias(alias).on(on));
    }

    /**
     * @since 2.1.4
     */
    public Delete rightJoin(String from, String alias, IConditionAppender on) {
        Cond cond = Cond.create(this);
        on.append(cond);
        return join(Join.right(owner(), dataSourceName(), from).alias(alias).on(cond));
    }

    //

    public Delete innerJoin(String prefix, String from, String alias, Cond on) {
        return join(Join.inner(owner(), dataSourceName(), prefix, from).alias(alias).on(on));
    }

    /**
     * @since 2.1.4
     */
    public Delete innerJoin(String prefix, String from, String alias, IConditionAppender on) {
        Cond cond = Cond.create(this);
        on.append(cond);
        return join(Join.inner(owner(), dataSourceName(), prefix, from).alias(alias).on(cond));
    }

    /**
     * @since 2.1.4
     */
    public Delete innerJoin(String prefix, String from, String alias) {
        return join(Join.inner(owner(), dataSourceName(), prefix, from).alias(alias));
    }

    /**
     * @since 2.1.4
     */
    public Delete crossJoin(String prefix, String from, String alias, Cond on) {
        return join(Join.cross(owner(), dataSourceName(), prefix, from).alias(alias).on(on));
    }

    /**
     * @since 2.1.4
     */
    public Delete crossJoin(String prefix, String from, String alias, IConditionAppender on) {
        Cond cond = Cond.create(this);
        on.append(cond);
        return join(Join.cross(owner(), dataSourceName(), prefix, from).alias(alias).on(cond));
    }

    /**
     * @since 2.1.4
     */
    public Delete crossJoin(String prefix, String from, String alias) {
        return join(Join.cross(owner(), dataSourceName(), prefix, from).alias(alias));
    }

    public Delete leftJoin(String prefix, String from, String alias, Cond on) {
        return join(Join.left(owner(), dataSourceName(), prefix, from).alias(alias).on(on));
    }

    /**
     * @since 2.1.4
     */
    public Delete leftJoin(String prefix, String from, String alias, IConditionAppender on) {
        Cond cond = Cond.create(this);
        on.append(cond);
        return join(Join.left(owner(), dataSourceName(), prefix, from).alias(alias).on(cond));
    }

    public Delete rightJoin(String prefix, String from, String alias, Cond on) {
        return join(Join.right(owner(), dataSourceName(), prefix, from).alias(alias).on(on));
    }

    /**
     * @since 2.1.4
     */
    public Delete rightJoin(String prefix, String from, String alias, IConditionAppender on) {
        Cond cond = Cond.create(this);
        on.append(cond);
        return join(Join.right(owner(), dataSourceName(), prefix, from).alias(alias).on(cond));
    }

    //

    public Delete innerJoin(String prefix, String from, String alias, Cond on, boolean safePrefix) {
        return join(Join.inner(owner(), dataSourceName(), prefix, from, safePrefix).alias(alias).on(on));
    }

    /**
     * @since 2.1.4
     */
    public Delete innerJoin(String prefix, String from, String alias, IConditionAppender on, boolean safePrefix) {
        Cond cond = Cond.create(this);
        on.append(cond);
        return join(Join.inner(owner(), dataSourceName(), prefix, from, safePrefix).alias(alias).on(cond));
    }

    /**
     * @since 2.1.4
     */
    public Delete innerJoin(String prefix, String from, String alias, boolean safePrefix) {
        return join(Join.inner(owner(), dataSourceName(), prefix, from, safePrefix).alias(alias));
    }

    /**
     * @since 2.1.4
     */
    public Delete crossJoin(String prefix, String from, String alias, Cond on, boolean safePrefix) {
        return join(Join.cross(owner(), dataSourceName(), prefix, from, safePrefix).alias(alias).on(on));
    }

    /**
     * @since 2.1.4
     */
    public Delete crossJoin(String prefix, String from, String alias, IConditionAppender on, boolean safePrefix) {
        Cond cond = Cond.create(this);
        on.append(cond);
        return join(Join.cross(owner(), dataSourceName(), prefix, from, safePrefix).alias(alias).on(cond));
    }

    /**
     * @since 2.1.4
     */
    public Delete crossJoin(String prefix, String from, String alias, boolean safePrefix) {
        return join(Join.cross(owner(), dataSourceName(), prefix, from, safePrefix).alias(alias));
    }

    public Delete leftJoin(String prefix, String from, String alias, Cond on, boolean safePrefix) {
        return join(Join.left(owner(), dataSourceName(), prefix, from, safePrefix).alias(alias).on(on));
    }

    /**
     * @since 2.1.4
     */
    public Delete leftJoin(String prefix, String from, String alias, IConditionAppender on, boolean safePrefix) {
        Cond cond = Cond.create(this);
        on.append(cond);
        return join(Join.left(owner(), dataSourceName(), prefix, from, safePrefix).alias(alias).on(cond));
    }

    public Delete rightJoin(String prefix, String from, String alias, Cond on, boolean safePrefix) {
        return join(Join.right(owner(), dataSourceName(), prefix, from, safePrefix).alias(alias).on(on));
    }

    /**
     * @since 2.1.4
     */
    public Delete rightJoin(String prefix, String from, String alias, IConditionAppender on, boolean safePrefix) {
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
     * @return 当前Delete实例
     * @since 2.1.4
     */
    public <T, U, R> Delete innerJoin(Class<? extends IEntity<?>> entityClass, String alias, SFunction<T, R> columnOne, SFunction<U, R> columnTwo) {
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
     * @return 当前Delete实例
     * @since 2.1.4
     */
    public <T, U, R> Delete innerJoin(Class<? extends IEntity<?>> entityClass, String alias, SFunction<T, R> columnOne, SFunction<U, R> columnTwo, boolean wrapIdentifier) {
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
     * @param <T>         第一个实体类型
     * @param <U>         第二个实体类型
     * @param <R>         返回值类型
     * @return 当前Delete实例
     * @since 2.1.4
     */
    public <T, U, R> Delete leftJoin(Class<? extends IEntity<?>> entityClass, String alias, SFunction<T, R> columnOne, SFunction<U, R> columnTwo) {
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
     * @param <T>            第一个实体类型
     * @param <U>            第二个实体类型
     * @param <R>            返回值类型
     * @return 当前Delete实例
     * @since 2.1.4
     */
    public <T, U, R> Delete leftJoin(Class<? extends IEntity<?>> entityClass, String alias, SFunction<T, R> columnOne, SFunction<U, R> columnTwo, boolean wrapIdentifier) {
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
     * @param <T>         第一个实体类型
     * @param <U>         第二个实体类型
     * @param <R>         返回值类型
     * @return 当前Delete实例
     * @since 2.1.4
     */
    public <T, U, R> Delete rightJoin(Class<? extends IEntity<?>> entityClass, String alias, SFunction<T, R> columnOne, SFunction<U, R> columnTwo) {
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
     * @param <T>            第一个实体类型
     * @param <U>            第二个实体类型
     * @param <R>            返回值类型
     * @return 当前Delete实例
     * @since 2.1.4
     */
    public <T, U, R> Delete rightJoin(Class<? extends IEntity<?>> entityClass, String alias, SFunction<T, R> columnOne, SFunction<U, R> columnTwo, boolean wrapIdentifier) {
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
     * @param <T>         第一个实体类型
     * @param <U>         第二个实体类型
     * @param <R>         返回值类型
     * @return 当前Delete实例
     * @since 2.1.4
     */
    public <T, U, R> Delete crossJoin(Class<? extends IEntity<?>> entityClass, String alias, SFunction<T, R> columnOne, SFunction<U, R> columnTwo) {
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
     * @param <T>            第一个实体类型
     * @param <U>            第二个实体类型
     * @param <R>            返回值类型
     * @return 当前Delete实例
     * @since 2.1.4
     */
    public <T, U, R> Delete crossJoin(Class<? extends IEntity<?>> entityClass, String alias, SFunction<T, R> columnOne, SFunction<U, R> columnTwo, boolean wrapIdentifier) {
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
     * @return 当前Delete实例
     * @since 2.1.4
     */
    public <T, U, R> Delete innerJoin(Class<? extends IEntity<?>> entityClass, String alias, String prefixOne, SFunction<T, R> columnOne, String prefixTwo, SFunction<U, R> columnTwo) {
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
     * @return 当前Delete实例
     * @since 2.1.4
     */
    public <T, U, R> Delete innerJoin(Class<? extends IEntity<?>> entityClass, String alias, String prefixOne, SFunction<T, R> columnOne, String prefixTwo, SFunction<U, R> columnTwo, boolean wrapIdentifier) {
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
     * @return 当前Delete实例
     * @since 2.1.4
     */
    public <T, U, R> Delete leftJoin(Class<? extends IEntity<?>> entityClass, String alias, String prefixOne, SFunction<T, R> columnOne, String prefixTwo, SFunction<U, R> columnTwo) {
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
     * @return 当前Delete实例
     * @since 2.1.4
     */
    public <T, U, R> Delete leftJoin(Class<? extends IEntity<?>> entityClass, String alias, String prefixOne, SFunction<T, R> columnOne, String prefixTwo, SFunction<U, R> columnTwo, boolean wrapIdentifier) {
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
     * @return 当前Delete实例
     * @since 2.1.4
     */
    public <T, U, R> Delete rightJoin(Class<? extends IEntity<?>> entityClass, String alias, String prefixOne, SFunction<T, R> columnOne, String prefixTwo, SFunction<U, R> columnTwo) {
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
     * @return 当前Delete实例
     * @since 2.1.4
     */
    public <T, U, R> Delete rightJoin(Class<? extends IEntity<?>> entityClass, String alias, String prefixOne, SFunction<T, R> columnOne, String prefixTwo, SFunction<U, R> columnTwo, boolean wrapIdentifier) {
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
     * @return 当前Delete实例
     * @since 2.1.4
     */
    public <T, U, R> Delete crossJoin(Class<? extends IEntity<?>> entityClass, String alias, String prefixOne, SFunction<T, R> columnOne, String prefixTwo, SFunction<U, R> columnTwo) {
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
     * @return 当前Delete实例
     * @since 2.1.4
     */
    public <T, U, R> Delete crossJoin(Class<? extends IEntity<?>> entityClass, String alias, String prefixOne, SFunction<T, R> columnOne, String prefixTwo, SFunction<U, R> columnTwo, boolean wrapIdentifier) {
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
     * @return 当前Delete实例
     * @since 2.1.4
     */
    public Delete innerJoin(Class<? extends IEntity<?>> entityClass, String alias, Cond on) {
        return join(Join.inner(this, entityClass, alias).on(on));
    }

    /**
     * @since 2.1.4
     */
    public Delete innerJoin(Class<? extends IEntity<?>> entityClass, String alias, IConditionAppender on) {
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
     * @return 当前Delete实例
     * @since 2.1.4
     */
    public Delete leftJoin(Class<? extends IEntity<?>> entityClass, String alias, Cond on) {
        return join(Join.left(this, entityClass, alias).on(on));
    }

    /**
     * @since 2.1.4
     */
    public Delete leftJoin(Class<? extends IEntity<?>> entityClass, String alias, IConditionAppender on) {
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
     * @return 当前Delete实例
     * @since 2.1.4
     */
    public Delete rightJoin(Class<? extends IEntity<?>> entityClass, String alias, Cond on) {
        return join(Join.right(this, entityClass, alias).on(on));
    }

    /**
     * @since 2.1.4
     */
    public Delete rightJoin(Class<? extends IEntity<?>> entityClass, String alias, IConditionAppender on) {
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
     * @return 当前Delete实例
     * @since 2.1.4
     */
    public Delete crossJoin(Class<? extends IEntity<?>> entityClass, String alias, Cond on) {
        return join(Join.cross(this, entityClass, alias).on(on));
    }

    /**
     * @since 2.1.4
     */
    public Delete crossJoin(Class<? extends IEntity<?>> entityClass, String alias, IConditionAppender on) {
        Cond cond = Cond.create(this);
        on.append(cond);
        return join(Join.cross(this, entityClass, alias).on(cond));
    }

    public Delete where(Where where) {
        where().where(where);
        return this;
    }

    public Params params() {
        return where().params();
    }

    public Delete param(Object param) {
        where().param(param);
        return this;
    }

    public Delete param(Params params) {
        where().param(params);
        return this;
    }

    public Where where() {
        if (this.where == null) {
            this.where = Where.create(this);
        }
        return where;
    }

    public Delete where(Cond cond) {
        where().cond().cond(cond);
        return this;
    }

    // ---------- Lambda Support for WHERE ----------

    /**
     * 通过Lambda表达式创建删除条件（相等条件）
     *
     * @param column 方法引用
     * @param value  参数值
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Delete实例
     * @since 2.1.4
     */
    public <T, R> Delete where(SFunction<T, R> column, Object value) {
        return where(Cond.create(this).eqWrap(column).param(value));
    }

    /**
     * 通过Lambda表达式创建删除条件（相等条件）
     *
     * @param column         方法引用
     * @param value          参数值
     * @param wrapIdentifier 是否包装标识符
     * @param <T>            实体类型
     * @param <R>            返回值类型
     * @return 当前Delete实例
     * @since 2.1.4
     */
    public <T, R> Delete where(SFunction<T, R> column, Object value, boolean wrapIdentifier) {
        if (wrapIdentifier) {
            return where(Cond.create(this).eqWrap(column).param(value));
        }
        return where(Cond.create(this).eq(column).param(value));
    }

    /**
     * 通过条件构建器创建删除条件
     *
     * @param appender 条件构建器
     * @return 当前Delete实例
     * @since 2.1.4
     */
    public Delete where(IConditionAppender appender) {
        Cond cond = Cond.create(this);
        appender.append(cond);
        return where(cond);
    }

    /**
     * @since 2.1.4
     */
    public Delete with(With with) {
        this.with = with;
        where().param(with.params());
        return this;
    }

    @Override
    public String toString() {
        ExpressionUtils expression = ExpressionUtils.bind(getExpressionStr("DELETE ${fields} FROM ${froms} ${joins} ${where}"));
        if (queryHandler() != null) {
            queryHandler().beforeBuild(expression, this);
        }
        List<String> variables = expression.getVariables();
        //
        if (!fields.fields().isEmpty() && variables.contains("fields")) {
            expression.set("fields", StringUtils.join(fields.fields(), LINE_END_FLAG));
        }
        expression.set("froms", StringUtils.join(froms, LINE_END_FLAG));
        if (where != null && variables.contains("where")) {
            expression.set("where", where.toString());
        }
        if (variables.contains("joins")) {
            expression.set("joins", StringUtils.join(joins, StringUtils.SPACE));
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
