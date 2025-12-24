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

import net.ymate.platform.core.persistence.Params;
import net.ymate.platform.core.persistence.base.IEntity;
import net.ymate.platform.persistence.jdbc.IDatabase;
import net.ymate.platform.persistence.jdbc.JDBC;
import net.ymate.platform.persistence.jdbc.query.LambdaUtils.SFunction;
import org.apache.commons.lang3.StringUtils;

/**
 * 连接查询语句对象
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/12 下午6:04
 */
public class Join extends Query<Join> {

    /**
     * 连接方式枚举
     */
    public enum Type {

        /**
         * INNER
         */
        INNER("INNER JOIN"),

        /**
         * CROSS
         *
         * @since 2.1.4
         */
        CROSS("CROSS JOIN"),

        /**
         * LEFT
         */
        LEFT("LEFT JOIN"),

        /**
         * RIGHT
         */
        RIGHT("RIGHT JOIN");

        private final String name;

        Type(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private final String from;

    private String alias;

    private final Cond on;

    public static Join inner(String from) {
        return inner((String) null, from, true);
    }

    public static Join inner(String from, boolean safePrefix) {
        return inner((String) null, from, safePrefix);
    }

    public static Join inner(String prefix, String from) {
        return inner(prefix, from, true);
    }

    public static Join inner(String prefix, String from, boolean safePrefix) {
        IDatabase owner = JDBC.get();
        return new Join(owner, owner.getConfig().getDefaultDataSourceName(), Type.INNER.getName(), prefix, from, safePrefix);
    }

    /**
     * 创建内连接查询对象（基于实体类）
     *
     * @param entityClass 实体类
     * @param alias       别名
     * @return Join实例
     * @since 2.1.4
     */
    public static Join inner(Class<? extends IEntity<?>> entityClass, String alias) {
        return inner((Query<?>) null, entityClass, alias);
    }

    /**
     * 创建内连接查询对象（基于实体类）
     *
     * @param query       查询对象
     * @param entityClass 实体类
     * @param alias       别名
     * @return Join实例
     * @since 2.1.4
     */
    public static Join inner(Query<?> query, Class<? extends IEntity<?>> entityClass, String alias) {
        return inner(query, null, entityClass, alias);
    }

    /**
     * 创建内连接查询对象（基于实体类）
     *
     * @param query       查询对象
     * @param prefix      前缀
     * @param entityClass 实体类
     * @param alias       别名
     * @return Join实例
     * @since 2.1.4
     */
    public static Join inner(Query<?> query, String prefix, Class<? extends IEntity<?>> entityClass, String alias) {
        IDatabase owner = query != null ? query.owner() : JDBC.get();
        String dataSourceName = query != null ? query.dataSourceName() : owner.getConfig().getDefaultDataSourceName();
        return new Join(owner, dataSourceName, Type.INNER.getName(), prefix, LambdaUtils.getEntityName(entityClass), true).alias(alias);
    }

    /**
     * 创建交叉连接查询对象（基于实体类）
     *
     * @param entityClass 实体类
     * @param alias       别名
     * @return Join实例
     * @since 2.1.4
     */
    public static Join cross(Class<? extends IEntity<?>> entityClass, String alias) {
        return cross(null, entityClass, alias);
    }

    /**
     * 创建交叉连接查询对象（基于实体类）
     *
     * @param query       查询对象
     * @param entityClass 实体类
     * @param alias       别名
     * @return Join实例
     * @since 2.1.4
     */
    public static Join cross(Query<?> query, Class<? extends IEntity<?>> entityClass, String alias) {
        return cross(query, null, entityClass, alias);
    }

    /**
     * 创建交叉连接查询对象（基于实体类）
     *
     * @param query       查询对象
     * @param prefix      前缀
     * @param entityClass 实体类
     * @param alias       别名
     * @return Join实例
     * @since 2.1.4
     */
    public static Join cross(Query<?> query, String prefix, Class<? extends IEntity<?>> entityClass, String alias) {
        IDatabase owner = query != null ? query.owner() : JDBC.get();
        String dataSourceName = query != null ? query.dataSourceName() : owner.getConfig().getDefaultDataSourceName();
        return new Join(owner, dataSourceName, Type.CROSS.getName(), prefix, LambdaUtils.getEntityName(entityClass), true).alias(alias);
    }

    /**
     * 创建左连接查询对象（基于实体类）
     *
     * @param entityClass 实体类
     * @param alias       别名
     * @return Join实例
     * @since 2.1.4
     */
    public static Join left(Class<? extends IEntity<?>> entityClass, String alias) {
        return left(null, entityClass, alias);
    }

    /**
     * 创建左连接查询对象（基于实体类）
     *
     * @param query       查询对象
     * @param entityClass 实体类
     * @param alias       别名
     * @return Join实例
     * @since 2.1.4
     */
    public static Join left(Query<?> query, Class<? extends IEntity<?>> entityClass, String alias) {
        return left(query, null, entityClass, alias);
    }

    /**
     * 创建左连接查询对象（基于实体类）
     *
     * @param query       查询对象
     * @param prefix      前缀
     * @param entityClass 实体类
     * @param alias       别名
     * @return Join实例
     * @since 2.1.4
     */
    public static Join left(Query<?> query, String prefix, Class<? extends IEntity<?>> entityClass, String alias) {
        IDatabase owner = query != null ? query.owner() : JDBC.get();
        String dataSourceName = query != null ? query.dataSourceName() : owner.getConfig().getDefaultDataSourceName();
        return new Join(owner, dataSourceName, Type.LEFT.getName(), prefix, LambdaUtils.getEntityName(entityClass), true).alias(alias);
    }

    /**
     * 创建右连接查询对象（基于实体类）
     *
     * @param entityClass 实体类
     * @param alias       别名
     * @return Join实例
     * @since 2.1.4
     */
    public static Join right(Class<? extends IEntity<?>> entityClass, String alias) {
        return right(null, entityClass, alias);
    }

    /**
     * 创建右连接查询对象（基于实体类）
     *
     * @param query       查询对象
     * @param entityClass 实体类
     * @param alias       别名
     * @return Join实例
     * @since 2.1.4
     */
    public static Join right(Query<?> query, Class<? extends IEntity<?>> entityClass, String alias) {
        return right(query, null, entityClass, alias);
    }

    /**
     * 创建右连接查询对象（基于实体类）
     *
     * @param query       查询对象
     * @param prefix      前缀
     * @param entityClass 实体类
     * @param alias       别名
     * @return Join实例
     * @since 2.1.4
     */
    public static Join right(Query<?> query, String prefix, Class<? extends IEntity<?>> entityClass, String alias) {
        IDatabase owner = query != null ? query.owner() : JDBC.get();
        String dataSourceName = query != null ? query.dataSourceName() : owner.getConfig().getDefaultDataSourceName();
        return new Join(owner, dataSourceName, Type.RIGHT.getName(), prefix, LambdaUtils.getEntityName(entityClass), true).alias(alias);
    }

    /**
     * @since 2.1.4
     */
    public static Join cross(String from) {
        return cross((String) null, from, true);
    }

    /**
     * @since 2.1.4
     */
    public static Join cross(String from, boolean safePrefix) {
        return cross((String) null, from, safePrefix);
    }

    /**
     * @since 2.1.4
     */
    public static Join cross(String prefix, String from) {
        return cross(prefix, from, true);
    }

    /**
     * @since 2.1.4
     */
    public static Join cross(String prefix, String from, boolean safePrefix) {
        IDatabase owner = JDBC.get();
        return new Join(owner, owner.getConfig().getDefaultDataSourceName(), Type.CROSS.getName(), prefix, from, safePrefix);
    }

    public static Join left(String from) {
        return left((String) null, from, true);
    }

    public static Join left(String from, boolean safePrefix) {
        return left((String) null, from, safePrefix);
    }

    public static Join left(String prefix, String from) {
        return left(prefix, from, true);
    }

    public static Join left(String prefix, String from, boolean safePrefix) {
        IDatabase owner = JDBC.get();
        return new Join(owner, owner.getConfig().getDefaultDataSourceName(), Type.LEFT.getName(), prefix, from, safePrefix);
    }

    public static Join right(String from) {
        return right((String) null, from, true);
    }

    public static Join right(String from, boolean safePrefix) {
        return right((String) null, from, safePrefix);
    }

    public static Join right(String prefix, String from) {
        return right(prefix, from, true);
    }

    public static Join right(String prefix, String from, boolean safePrefix) {
        IDatabase owner = JDBC.get();
        return new Join(owner, owner.getConfig().getDefaultDataSourceName(), Type.RIGHT.getName(), prefix, from, safePrefix);
    }

    //

    public static Join inner(Select select) {
        Join target = inner(select.owner(), select.dataSourceName(), null, select.toString(), false);
        target.params().add(select.params());
        return target;
    }

    public static Join inner(Query<?> query, String from) {
        return inner(query.owner(), query.dataSourceName(), from, true);
    }

    public static Join inner(IDatabase owner, String dataSourceName, String from) {
        return inner(owner, dataSourceName, from, true);
    }

    public static Join inner(Query<?> query, String from, boolean safePrefix) {
        return inner(query.owner(), query.dataSourceName(), null, from, safePrefix);
    }

    public static Join inner(IDatabase owner, String dataSourceName, String from, boolean safePrefix) {
        return inner(owner, dataSourceName, null, from, safePrefix);
    }

    public static Join inner(Query<?> query, String prefix, String from) {
        return inner(query.owner(), query.dataSourceName(), prefix, from, true);
    }

    public static Join inner(IDatabase owner, String dataSourceName, String prefix, String from) {
        return inner(owner, dataSourceName, prefix, from, true);
    }

    public static Join inner(Query<?> query, String prefix, String from, boolean safePrefix) {
        return inner(query.owner(), query.dataSourceName(), prefix, from, safePrefix);
    }

    public static Join inner(IDatabase owner, String dataSourceName, String prefix, String from, boolean safePrefix) {
        return new Join(owner, dataSourceName, Type.INNER.getName(), prefix, from, safePrefix);
    }

    /**
     * @since 2.1.4
     */
    public static Join cross(Select select) {
        Join target = cross(select.owner(), select.dataSourceName(), null, select.toString(), false);
        target.params().add(select.params());
        return target;
    }

    /**
     * @since 2.1.4
     */
    public static Join cross(Query<?> query, String from) {
        return cross(query.owner(), query.dataSourceName(), from, true);
    }

    /**
     * @since 2.1.4
     */
    public static Join cross(IDatabase owner, String dataSourceName, String from) {
        return cross(owner, dataSourceName, from, true);
    }

    /**
     * @since 2.1.4
     */
    public static Join cross(Query<?> query, String from, boolean safePrefix) {
        return cross(query.owner(), query.dataSourceName(), null, from, safePrefix);
    }

    /**
     * @since 2.1.4
     */
    public static Join cross(IDatabase owner, String dataSourceName, String from, boolean safePrefix) {
        return cross(owner, dataSourceName, null, from, safePrefix);
    }

    /**
     * @since 2.1.4
     */
    public static Join cross(Query<?> query, String prefix, String from) {
        return cross(query.owner(), query.dataSourceName(), prefix, from, true);
    }

    /**
     * @since 2.1.4
     */
    public static Join cross(IDatabase owner, String dataSourceName, String prefix, String from) {
        return cross(owner, dataSourceName, prefix, from, true);
    }

    /**
     * @since 2.1.4
     */
    public static Join cross(Query<?> query, String prefix, String from, boolean safePrefix) {
        return cross(query.owner(), query.dataSourceName(), prefix, from, safePrefix);
    }

    /**
     * @since 2.1.4
     */
    public static Join cross(IDatabase owner, String dataSourceName, String prefix, String from, boolean safePrefix) {
        return new Join(owner, dataSourceName, Type.CROSS.getName(), prefix, from, safePrefix);
    }

    //

    public static Join left(Select select) {
        Join target = left(select.owner(), select.dataSourceName(), null, select.toString(), false);
        target.params().add(select.params());
        return target;
    }

    public static Join left(Query<?> query, String from) {
        return left(query.owner(), query.dataSourceName(), from, true);
    }

    public static Join left(IDatabase owner, String dataSourceName, String from) {
        return left(owner, dataSourceName, from, true);
    }

    public static Join left(Query<?> query, String from, boolean safePrefix) {
        return left(query.owner(), query.dataSourceName(), null, from, safePrefix);
    }

    public static Join left(IDatabase owner, String dataSourceName, String from, boolean safePrefix) {
        return left(owner, dataSourceName, null, from, safePrefix);
    }

    public static Join left(Query<?> query, String prefix, String from) {
        return left(query.owner(), query.dataSourceName(), prefix, from, true);
    }

    public static Join left(IDatabase owner, String dataSourceName, String prefix, String from) {
        return left(owner, dataSourceName, prefix, from, true);
    }

    public static Join left(Query<?> query, String prefix, String from, boolean safePrefix) {
        return left(query.owner(), query.dataSourceName(), prefix, from, safePrefix);
    }

    public static Join left(IDatabase owner, String dataSourceName, String prefix, String from, boolean safePrefix) {
        return new Join(owner, dataSourceName, Type.LEFT.getName(), prefix, from, safePrefix);
    }

    //

    public static Join right(Select select) {
        Join target = right(select.owner(), select.dataSourceName(), null, select.toString(), false);
        target.params().add(select.params());
        return target;
    }

    public static Join right(Query<?> query, String from) {
        return right(query.owner(), query.dataSourceName(), from);
    }

    public static Join right(IDatabase owner, String dataSourceName, String from) {
        return right(owner, dataSourceName, from, true);
    }

    public static Join right(Query<?> query, String from, boolean safePrefix) {
        return right(query.owner(), query.dataSourceName(), from, safePrefix);
    }

    public static Join right(IDatabase owner, String dataSourceName, String from, boolean safePrefix) {
        return right(owner, dataSourceName, null, from, safePrefix);
    }

    public static Join right(Query<?> query, String prefix, String from) {
        return right(query.owner(), query.dataSourceName(), prefix, from, true);
    }

    public static Join right(IDatabase owner, String dataSourceName, String prefix, String from) {
        return right(owner, dataSourceName, prefix, from, true);
    }

    public static Join right(Query<?> query, String prefix, String from, boolean safePrefix) {
        return right(query.owner(), query.dataSourceName(), prefix, from, safePrefix);
    }

    public static Join right(IDatabase owner, String dataSourceName, String prefix, String from, boolean safePrefix) {
        return new Join(owner, dataSourceName, Type.RIGHT.getName(), prefix, from, safePrefix);
    }

    public Join(IDatabase owner, String dataSourceName, String type, String prefix, String from, boolean safePrefix) {
        super(owner, dataSourceName);
        this.from = String.format("%s %s", type, buildSafeTableName(prefix, from, safePrefix));
        on = Cond.create(owner, dataSourceName);
    }

    public Join alias(String alias) {
        this.alias = alias;
        return this;
    }

    public Join on(Cond cond) {
        on.cond(cond);
        return this;
    }

    // ---------- Lambda Support for ON ----------

    /**
     * 通过Lambda表达式创建连接条件（两个字段相等）
     *
     * @param columnOne 第一个字段的方法引用
     * @param columnTwo 第二个字段的方法引用
     * @param <T>       第一个实体类型
     * @param <U>       第二个实体类型
     * @param <R>       返回值类型
     * @return 当前Join实例
     * @since 2.1.4
     */
    public <T, U, R> Join onEq(SFunction<T, R> columnOne, SFunction<U, R> columnTwo) {
        String fieldOne = getColumnName(columnOne);
        String fieldTwo = getColumnName(columnTwo);
        return on(Cond.create(this).eqField(fieldOne, fieldTwo));
    }

    /**
     * 通过Lambda表达式创建连接条件（两个字段相等，带前缀）
     *
     * @param prefixOne 第一个字段的前缀
     * @param columnOne 第一个字段的方法引用
     * @param prefixTwo 第二个字段的前缀
     * @param columnTwo 第二个字段的方法引用
     * @param <T>       第一个实体类型
     * @param <U>       第二个实体类型
     * @param <R>       返回值类型
     * @return 当前Join实例
     * @since 2.1.4
     */
    public <T, U, R> Join onEq(String prefixOne, SFunction<T, R> columnOne, String prefixTwo, SFunction<U, R> columnTwo) {
        return on(Cond.create(this).eq(prefixOne, columnOne, prefixTwo, columnTwo));
    }

    /**
     * 通过Lambda表达式创建连接条件（两个字段相等，带字段包装）
     *
     * @param columnOne 第一个字段的方法引用
     * @param columnTwo 第二个字段的方法引用
     * @param <T>       第一个实体类型
     * @param <U>       第二个实体类型
     * @param <R>       返回值类型
     * @return 当前Join实例
     * @since 2.1.4
     */
    public <T, U, R> Join onEqWrap(SFunction<T, R> columnOne, SFunction<U, R> columnTwo) {
        String fieldOne = getColumnName(columnOne);
        String fieldTwo = getColumnName(columnTwo);
        return on(Cond.create(this).eqFieldWrap(fieldOne, fieldTwo));
    }

    /**
     * 通过Lambda表达式创建连接条件（两个字段相等，带前缀和字段包装）
     *
     * @param prefixOne 第一个字段的前缀
     * @param columnOne 第一个字段的方法引用
     * @param prefixTwo 第二个字段的前缀
     * @param columnTwo 第二个字段的方法引用
     * @param <T>       第一个实体类型
     * @param <U>       第二个实体类型
     * @param <R>       返回值类型
     * @return 当前Join实例
     * @since 2.1.4
     */
    public <T, U, R> Join onEqWrap(String prefixOne, SFunction<T, R> columnOne, String prefixTwo, SFunction<U, R> columnTwo) {
        return on(Cond.create(this).eqWrap(prefixOne, columnOne, prefixTwo, columnTwo));
    }

    /**
     * 通过Lambda表达式创建连接条件（两个字段不相等）
     *
     * @param columnOne 第一个字段的方法引用
     * @param columnTwo 第二个字段的方法引用
     * @param <T>       第一个实体类型
     * @param <U>       第二个实体类型
     * @param <R>       返回值类型
     * @return 当前Join实例
     * @since 2.1.4
     */
    public <T, U, R> Join onNotEq(SFunction<T, R> columnOne, SFunction<U, R> columnTwo) {
        String fieldOne = getColumnName(columnOne);
        String fieldTwo = getColumnName(columnTwo);
        return on(Cond.create(this).notEqField(fieldOne, fieldTwo));
    }

    /**
     * 通过Lambda表达式创建连接条件（两个字段不相等，带前缀）
     *
     * @param prefixOne 第一个字段的前缀
     * @param columnOne 第一个字段的方法引用
     * @param prefixTwo 第二个字段的前缀
     * @param columnTwo 第二个字段的方法引用
     * @param <T>       第一个实体类型
     * @param <U>       第二个实体类型
     * @param <R>       返回值类型
     * @return 当前Join实例
     * @since 2.1.4
     */
    public <T, U, R> Join onNotEq(String prefixOne, SFunction<T, R> columnOne, String prefixTwo, SFunction<U, R> columnTwo) {
        return on(Cond.create(this).notEq(prefixOne, columnOne, prefixTwo, columnTwo));
    }

    /**
     * 通过Lambda表达式创建连接条件（两个字段不相等，带字段包装）
     *
     * @param columnOne 第一个字段的方法引用
     * @param columnTwo 第二个字段的方法引用
     * @param <T>       第一个实体类型
     * @param <U>       第二个实体类型
     * @param <R>       返回值类型
     * @return 当前Join实例
     * @since 2.1.4
     */
    public <T, U, R> Join onNotEqWrap(SFunction<T, R> columnOne, SFunction<U, R> columnTwo) {
        String fieldOne = getColumnName(columnOne);
        String fieldTwo = getColumnName(columnTwo);
        return on(Cond.create(this).notEqFieldWrap(fieldOne, fieldTwo));
    }

    /**
     * 通过Lambda表达式创建连接条件（两个字段不相等，带前缀和字段包装）
     *
     * @param prefixOne 第一个字段的前缀
     * @param columnOne 第一个字段的方法引用
     * @param prefixTwo 第二个字段的前缀
     * @param columnTwo 第二个字段的方法引用
     * @param <T>       第一个实体类型
     * @param <U>       第二个实体类型
     * @param <R>       返回值类型
     * @return 当前Join实例
     * @since 2.1.4
     */
    public <T, U, R> Join onNotEqWrap(String prefixOne, SFunction<T, R> columnOne, String prefixTwo, SFunction<U, R> columnTwo) {
        return on(Cond.create(this).notEqWrap(prefixOne, columnOne, prefixTwo, columnTwo));
    }

    /**
     * 通过Lambda表达式创建连接条件（第一个字段大于第二个字段）
     *
     * @param columnOne 第一个字段的方法引用
     * @param columnTwo 第二个字段的方法引用
     * @param <T>       第一个实体类型
     * @param <U>       第二个实体类型
     * @param <R>       返回值类型
     * @return 当前Join实例
     * @since 2.1.4
     */
    public <T, U, R> Join onGt(SFunction<T, R> columnOne, SFunction<U, R> columnTwo) {
        String fieldOne = getColumnName(columnOne);
        String fieldTwo = getColumnName(columnTwo);
        return on(Cond.create(this).gtField(fieldOne, fieldTwo));
    }

    /**
     * 通过Lambda表达式创建连接条件（第一个字段大于第二个字段，带前缀）
     *
     * @param prefixOne 第一个字段的前缀
     * @param columnOne 第一个字段的方法引用
     * @param prefixTwo 第二个字段的前缀
     * @param columnTwo 第二个字段的方法引用
     * @param <T>       第一个实体类型
     * @param <U>       第二个实体类型
     * @param <R>       返回值类型
     * @return 当前Join实例
     * @since 2.1.4
     */
    public <T, U, R> Join onGt(String prefixOne, SFunction<T, R> columnOne, String prefixTwo, SFunction<U, R> columnTwo) {
        return on(Cond.create(this).gt(prefixOne, columnOne, prefixTwo, columnTwo));
    }

    /**
     * 通过Lambda表达式创建连接条件（第一个字段大于第二个字段，带字段包装）
     *
     * @param columnOne 第一个字段的方法引用
     * @param columnTwo 第二个字段的方法引用
     * @param <T>       第一个实体类型
     * @param <U>       第二个实体类型
     * @param <R>       返回值类型
     * @return 当前Join实例
     * @since 2.1.4
     */
    public <T, U, R> Join onGtWrap(SFunction<T, R> columnOne, SFunction<U, R> columnTwo) {
        String fieldOne = getColumnName(columnOne);
        String fieldTwo = getColumnName(columnTwo);
        return on(Cond.create(this).gtFieldWrap(fieldOne, fieldTwo));
    }

    /**
     * 通过Lambda表达式创建连接条件（第一个字段大于第二个字段，带前缀和字段包装）
     *
     * @param prefixOne 第一个字段的前缀
     * @param columnOne 第一个字段的方法引用
     * @param prefixTwo 第二个字段的前缀
     * @param columnTwo 第二个字段的方法引用
     * @param <T>       第一个实体类型
     * @param <U>       第二个实体类型
     * @param <R>       返回值类型
     * @return 当前Join实例
     * @since 2.1.4
     */
    public <T, U, R> Join onGtWrap(String prefixOne, SFunction<T, R> columnOne, String prefixTwo, SFunction<U, R> columnTwo) {
        return on(Cond.create(this).gtWrap(prefixOne, columnOne, prefixTwo, columnTwo));
    }

    /**
     * 通过Lambda表达式创建连接条件（第一个字段大于等于第二个字段）
     *
     * @param columnOne 第一个字段的方法引用
     * @param columnTwo 第二个字段的方法引用
     * @param <T>       第一个实体类型
     * @param <U>       第二个实体类型
     * @param <R>       返回值类型
     * @return 当前Join实例
     * @since 2.1.4
     */
    public <T, U, R> Join onGtEq(SFunction<T, R> columnOne, SFunction<U, R> columnTwo) {
        String fieldOne = getColumnName(columnOne);
        String fieldTwo = getColumnName(columnTwo);
        return on(Cond.create(this).gtEqField(fieldOne, fieldTwo));
    }

    /**
     * 通过Lambda表达式创建连接条件（第一个字段大于等于第二个字段，带前缀）
     *
     * @param prefixOne 第一个字段的前缀
     * @param columnOne 第一个字段的方法引用
     * @param prefixTwo 第二个字段的前缀
     * @param columnTwo 第二个字段的方法引用
     * @param <T>       第一个实体类型
     * @param <U>       第二个实体类型
     * @param <R>       返回值类型
     * @return 当前Join实例
     * @since 2.1.4
     */
    public <T, U, R> Join onGtEq(String prefixOne, SFunction<T, R> columnOne, String prefixTwo, SFunction<U, R> columnTwo) {
        return on(Cond.create(this).gtEq(prefixOne, columnOne, prefixTwo, columnTwo));
    }

    /**
     * 通过Lambda表达式创建连接条件（第一个字段大于等于第二个字段，带字段包装）
     *
     * @param columnOne 第一个字段的方法引用
     * @param columnTwo 第二个字段的方法引用
     * @param <T>       第一个实体类型
     * @param <U>       第二个实体类型
     * @param <R>       返回值类型
     * @return 当前Join实例
     * @since 2.1.4
     */
    public <T, U, R> Join onGtEqWrap(SFunction<T, R> columnOne, SFunction<U, R> columnTwo) {
        String fieldOne = getColumnName(columnOne);
        String fieldTwo = getColumnName(columnTwo);
        return on(Cond.create(this).gtEqFieldWrap(fieldOne, fieldTwo));
    }

    /**
     * 通过Lambda表达式创建连接条件（第一个字段大于等于第二个字段，带前缀和字段包装）
     *
     * @param prefixOne 第一个字段的前缀
     * @param columnOne 第一个字段的方法引用
     * @param prefixTwo 第二个字段的前缀
     * @param columnTwo 第二个字段的方法引用
     * @param <T>       第一个实体类型
     * @param <U>       第二个实体类型
     * @param <R>       返回值类型
     * @return 当前Join实例
     * @since 2.1.4
     */
    public <T, U, R> Join onGtEqWrap(String prefixOne, SFunction<T, R> columnOne, String prefixTwo, SFunction<U, R> columnTwo) {
        return on(Cond.create(this).gtEqWrap(prefixOne, columnOne, prefixTwo, columnTwo));
    }

    /**
     * 通过Lambda表达式创建连接条件（第一个字段小于第二个字段）
     *
     * @param columnOne 第一个字段的方法引用
     * @param columnTwo 第二个字段的方法引用
     * @param <T>       第一个实体类型
     * @param <U>       第二个实体类型
     * @param <R>       返回值类型
     * @return 当前Join实例
     * @since 2.1.4
     */
    public <T, U, R> Join onLt(SFunction<T, R> columnOne, SFunction<U, R> columnTwo) {
        String fieldOne = getColumnName(columnOne);
        String fieldTwo = getColumnName(columnTwo);
        return on(Cond.create(this).ltField(fieldOne, fieldTwo));
    }

    /**
     * 通过Lambda表达式创建连接条件（第一个字段小于第二个字段，带前缀）
     *
     * @param prefixOne 第一个字段的前缀
     * @param columnOne 第一个字段的方法引用
     * @param prefixTwo 第二个字段的前缀
     * @param columnTwo 第二个字段的方法引用
     * @param <T>       第一个实体类型
     * @param <U>       第二个实体类型
     * @param <R>       返回值类型
     * @return 当前Join实例
     * @since 2.1.4
     */
    public <T, U, R> Join onLt(String prefixOne, SFunction<T, R> columnOne, String prefixTwo, SFunction<U, R> columnTwo) {
        return on(Cond.create(this).lt(prefixOne, columnOne, prefixTwo, columnTwo));
    }

    /**
     * 通过Lambda表达式创建连接条件（第一个字段小于第二个字段，带字段包装）
     *
     * @param columnOne 第一个字段的方法引用
     * @param columnTwo 第二个字段的方法引用
     * @param <T>       第一个实体类型
     * @param <U>       第二个实体类型
     * @param <R>       返回值类型
     * @return 当前Join实例
     * @since 2.1.4
     */
    public <T, U, R> Join onLtWrap(SFunction<T, R> columnOne, SFunction<U, R> columnTwo) {
        String fieldOne = getColumnName(columnOne);
        String fieldTwo = getColumnName(columnTwo);
        return on(Cond.create(this).ltFieldWrap(fieldOne, fieldTwo));
    }

    /**
     * 通过Lambda表达式创建连接条件（第一个字段小于第二个字段，带前缀和字段包装）
     *
     * @param prefixOne 第一个字段的前缀
     * @param columnOne 第一个字段的方法引用
     * @param prefixTwo 第二个字段的前缀
     * @param columnTwo 第二个字段的方法引用
     * @param <T>       第一个实体类型
     * @param <U>       第二个实体类型
     * @param <R>       返回值类型
     * @return 当前Join实例
     * @since 2.1.4
     */
    public <T, U, R> Join onLtWrap(String prefixOne, SFunction<T, R> columnOne, String prefixTwo, SFunction<U, R> columnTwo) {
        return on(Cond.create(this).ltWrap(prefixOne, columnOne, prefixTwo, columnTwo));
    }

    /**
     * 通过Lambda表达式创建连接条件（第一个字段小于等于第二个字段）
     *
     * @param columnOne 第一个字段的方法引用
     * @param columnTwo 第二个字段的方法引用
     * @param <T>       第一个实体类型
     * @param <U>       第二个实体类型
     * @param <R>       返回值类型
     * @return 当前Join实例
     * @since 2.1.4
     */
    public <T, U, R> Join onLtEq(SFunction<T, R> columnOne, SFunction<U, R> columnTwo) {
        String fieldOne = getColumnName(columnOne);
        String fieldTwo = getColumnName(columnTwo);
        return on(Cond.create(this).ltEqField(fieldOne, fieldTwo));
    }

    /**
     * 通过Lambda表达式创建连接条件（第一个字段小于等于第二个字段，带前缀）
     *
     * @param prefixOne 第一个字段的前缀
     * @param columnOne 第一个字段的方法引用
     * @param prefixTwo 第二个字段的前缀
     * @param columnTwo 第二个字段的方法引用
     * @param <T>       第一个实体类型
     * @param <U>       第二个实体类型
     * @param <R>       返回值类型
     * @return 当前Join实例
     * @since 2.1.4
     */
    public <T, U, R> Join onLtEq(String prefixOne, SFunction<T, R> columnOne, String prefixTwo, SFunction<U, R> columnTwo) {
        return on(Cond.create(this).ltEq(prefixOne, columnOne, prefixTwo, columnTwo));
    }

    /**
     * 通过Lambda表达式创建连接条件（第一个字段小于等于第二个字段，带字段包装）
     *
     * @param columnOne 第一个字段的方法引用
     * @param columnTwo 第二个字段的方法引用
     * @param <T>       第一个实体类型
     * @param <U>       第二个实体类型
     * @param <R>       返回值类型
     * @return 当前Join实例
     * @since 2.1.4
     */
    public <T, U, R> Join onLtEqWrap(SFunction<T, R> columnOne, SFunction<U, R> columnTwo) {
        String fieldOne = getColumnName(columnOne);
        String fieldTwo = getColumnName(columnTwo);
        return on(Cond.create(this).ltEqFieldWrap(fieldOne, fieldTwo));
    }

    /**
     * 通过Lambda表达式创建连接条件（第一个字段小于等于第二个字段，带前缀和字段包装）
     *
     * @param prefixOne 第一个字段的前缀
     * @param columnOne 第一个字段的方法引用
     * @param prefixTwo 第二个字段的前缀
     * @param columnTwo 第二个字段的方法引用
     * @param <T>       第一个实体类型
     * @param <U>       第二个实体类型
     * @param <R>       返回值类型
     * @return 当前Join实例
     * @since 2.1.4
     */
    public <T, U, R> Join onLtEqWrap(String prefixOne, SFunction<T, R> columnOne, String prefixTwo, SFunction<U, R> columnTwo) {
        return on(Cond.create(this).ltEqWrap(prefixOne, columnOne, prefixTwo, columnTwo));
    }

    /**
     * 通过条件构建器创建连接条件
     *
     * @param appender 条件构建器
     * @return 当前Join实例
     * @since 2.1.4
     */
    public Join on(IConditionAppender appender) {
        Cond cond = Cond.create(this);
        appender.append(cond);
        return on(cond);
    }

    public Params params() {
        return on.params();
    }

    @Override
    public String toString() {
        alias = StringUtils.trimToNull(alias);
        if (alias == null) {
            alias = StringUtils.EMPTY;
        } else {
            alias = StringUtils.SPACE.concat(alias);
        }
        if (on.isEmpty()) {
            return String.format("%s%s", from, alias);
        }
        return String.format("%s%s ON %s", from, alias, on);
    }
}
