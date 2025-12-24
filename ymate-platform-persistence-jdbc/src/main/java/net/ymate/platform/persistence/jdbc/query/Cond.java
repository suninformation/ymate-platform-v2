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

import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.persistence.Fields;
import net.ymate.platform.core.persistence.IFunction;
import net.ymate.platform.core.persistence.Params;
import net.ymate.platform.persistence.jdbc.IDatabase;
import net.ymate.platform.persistence.jdbc.JDBC;
import net.ymate.platform.persistence.jdbc.query.LambdaUtils.SFunction;
import net.ymate.platform.persistence.jdbc.query.annotation.QField;
import net.ymate.platform.validation.validate.DateTimeValue;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 条件对象
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/9 下午8:12
 */
public class Cond extends Query<Cond> {

    private static final Map<Class<?>, List<QFieldWrapper>> BEAN_FIELD_CACHE = new ConcurrentHashMap<>();

    /**
     * @since 2.1.3
     */
    public static Cond create(IDatabase owner, String dataSourceName, Object bean) {
        Cond cond = create(owner, dataSourceName).eqOne();
        if (bean != null) {
            List<QFieldWrapper> cachedFields = BEAN_FIELD_CACHE.computeIfAbsent(bean.getClass(), key ->
                    ClassUtils.wrapper(key).getFields().stream()
                            .filter(field -> field.isAnnotationPresent(QField.class))
                            .map(QFieldWrapper::new)
                            .collect(Collectors.toList()));
            //
            if (!cachedFields.isEmpty()) {
                cachedFields.forEach(fieldWrapper -> {
                    try {
                        Object value = fieldWrapper.field.get(bean);
                        cond.exprNotEmpty(value, c -> {
                            QField qField = fieldWrapper.qField;
                            if (value instanceof DateTimeValue) {
                                c.rangeWrap(qField.prefix(), qField.value(), (DateTimeValue) value, LogicalOpt.AND);
                            } else if (qField.opt().equals(OPT.LIKE)) {
                                c.and().likeWrap(qField.prefix(), qField.value()).param(Like.contains(value.toString()));
                            } else if (qField.opt().equals(OPT.RLIKE)) {
                                c.and().rlikeWrap(qField.prefix(), qField.value()).param(value.toString());
                            } else if (qField.opt().equals(OPT.REGEXP)) {
                                c.and().regexpWrap(qField.prefix(), qField.value()).param(value.toString());
                            } else if (value instanceof Collection || value.getClass().isArray()) {
                                c.and().inWrap(Fields.field(qField.prefix(), qField.value()), Params.create(value));
                            } else {
                                c.and().optWrap(Fields.field(qField.prefix(), qField.value()), qField.opt()).param(value);
                            }
                        });
                    } catch (IllegalAccessException e) {
                        throw RuntimeUtils.wrapRuntimeThrow(e, "Failed to access field '%s' in bean of type '%s'", fieldWrapper.field.getName(), bean.getClass().getName());
                    }
                });
            }
        }
        return cond;
    }

    /**
     * 条件操作符枚举
     */
    public enum OPT {

        /**
         * 等于
         */
        EQ("="),

        /**
         * 不等于
         */
        NOT_EQ("!="),

        /**
         * 小于
         */
        LT("<"),

        /**
         * 大于
         */
        GT(">"),

        /**
         * 小于等于
         */
        LT_EQ("<="),

        /**
         * 大于等于
         */
        GT_EQ(">="),

        /**
         * 模糊
         */
        LIKE("LIKE"),

        /**
         * @since 2.1.4
         */
        RLIKE("RLIKE"),

        /**
         * @since 2.1.4
         */
        REGEXP("REGEXP");

        private final String opt;

        OPT(String opt) {
            this.opt = opt;
        }

        @Override
        public String toString() {
            return opt;
        }
    }

    /**
     * 逻辑运算符枚举
     */
    public enum LogicalOpt {

        /**
         * 与
         */
        AND,

        /**
         * 或
         */
        OR,

        /**
         * 非
         */
        NOT
    }

    private final StringBuilder condition = new StringBuilder();

    private boolean brackets;

    /**
     * SQL参数集合
     */
    private final Params params = Params.create();

    public static Cond create() {
        return create(JDBC.get());
    }

    public static Cond create(IDatabase owner) {
        return new Cond(owner, owner.getConfig().getDefaultDataSourceName());
    }

    public static Cond create(IDatabase owner, String dataSourceName) {
        return new Cond(owner, dataSourceName);
    }

    public static Cond create(Query<?> query) {
        return new Cond(query.owner(), query.dataSourceName());
    }

    public Cond(IDatabase owner, String dataSourceName) {
        super(owner, dataSourceName);
    }

    public Params params() {
        return this.params;
    }

    public Cond param(Object param) {
        this.params.add(param);
        return this;
    }

    public Cond param(Params params) {
        this.params.add(params);
        return this;
    }

    public Cond cond(String cond) {
        if (StringUtils.isNotBlank(cond)) {
            condition.append(StringUtils.SPACE).append(cond).append(StringUtils.SPACE);
        }
        return this;
    }

    public Cond cond(IFunction func) {
        return cond(func.build()).param(func.params());
    }

    public Cond cond(Cond cond) {
        condition.append(cond.toString());
        params.add(cond.params());
        return this;
    }

    public Cond cond(FieldCondition cond) {
        return cond(cond.build());
    }

    public Cond cond(LogicalOpt opt, Object... condArr) {
        if (opt != null && ArrayUtils.isNotEmpty(condArr)) {
            for (Object cond : condArr) {
                if (cond instanceof IFunction) {
                    String funcStr = ((IFunction) cond).build();
                    if (StringUtils.isNotBlank(funcStr)) {
                        opt(opt).cond(funcStr);
                    }
                } else if (cond instanceof Cond) {
                    if (!((Cond) cond).isEmpty()) {
                        opt(opt).cond((Cond) cond);
                    }
                } else if (cond != null) {
                    String condStr = cond.toString();
                    if (StringUtils.isNotBlank(condStr)) {
                        opt(opt).cond(condStr);
                    }
                }
            }
        }
        return this;
    }

    public Cond opt(String fieldOne, OPT opt, String fieldTwo) {
        return cond(String.format("%s %s %s", fieldOne, opt, fieldTwo));
    }

    public Cond optWrap(String fieldOne, OPT opt, String fieldTwo) {
        return opt(wrapIdentifierField(fieldOne), opt, wrapIdentifierField(fieldTwo));
    }

    public Cond opt(String field, OPT opt) {
        return cond(String.format("%s %s ?", field, opt));
    }

    public Cond optWrap(String field, OPT opt) {
        return opt(wrapIdentifierField(field), opt);
    }

    public Cond opt(IFunction funcOne, OPT opt, IFunction funcTwo) {
        return opt(funcOne.build(), opt, funcTwo.build())
                .param(funcOne.params())
                .param(funcTwo.params());
    }

    public Cond opt(IFunction func, OPT opt) {
        return opt(func.build(), opt).param(func.params());
    }

    public Cond opt(LogicalOpt opt) {
        return cond(opt.name());
    }

    public Cond optIfNeed(LogicalOpt opt) {
        if (!isEmpty()) {
            return cond(opt.name());
        }
        return this;
    }

    /**
     * @return 用于生成Where条件辅助表达式1=1
     */
    public Cond eqOne() {
        return cond("1 = 1");
    }

    public Cond eqField(String fieldOne, String fieldTwo) {
        return opt(fieldOne, OPT.EQ, fieldTwo);
    }

    public Cond eqFieldWrap(String fieldOne, String fieldTwo) {
        return optWrap(fieldOne, OPT.EQ, fieldTwo);
    }

    public Cond eq(String prefix, String field) {
        return opt(Fields.field(prefix, field), OPT.EQ);
    }

    public Cond eq(String field) {
        return opt(field, OPT.EQ);
    }

    public Cond eqWrap(String prefix, String field) {
        return optWrap(Fields.field(prefix, field), OPT.EQ);
    }

    public Cond eqWrap(String field) {
        return optWrap(field, OPT.EQ);
    }

    public Cond eq(IFunction funcOne, IFunction funcTwo) {
        return opt(funcOne, OPT.EQ, funcTwo);
    }

    public Cond eq(IFunction func) {
        return opt(func, OPT.EQ);
    }

    // ---------- Lambda Support for EQ ----------

    /**
     * 通过Lambda表达式创建相等条件
     *
     * @param column 方法引用
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond eq(SFunction<T, R> column) {
        return opt(getColumnName(column), OPT.EQ);
    }

    /**
     * 通过Lambda表达式创建相等条件（带前缀）
     *
     * @param prefix 前缀
     * @param column 方法引用
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond eq(String prefix, SFunction<T, R> column) {
        return opt(Fields.field(prefix, getColumnName(column)), OPT.EQ);
    }

    /**
     * 通过Lambda表达式创建相等条件（带标识符包装）
     *
     * @param column 方法引用
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond eqWrap(SFunction<T, R> column) {
        return optWrap(getColumnName(column), OPT.EQ);
    }

    /**
     * 通过Lambda表达式创建相等条件（带前缀和标识符包装）
     *
     * @param prefix 前缀
     * @param column 方法引用
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond eqWrap(String prefix, SFunction<T, R> column) {
        return optWrap(Fields.field(prefix, getColumnName(column)), OPT.EQ);
    }

    /**
     * 通过Lambda表达式创建两个字段的相等条件
     *
     * @param columnOne 第一个字段的方法引用
     * @param columnTwo 第二个字段的方法引用
     * @param <T>       第一个实体类型
     * @param <F>       第二个实体类型
     * @param <R>       返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, F, R> Cond eq(SFunction<T, R> columnOne, SFunction<F, R> columnTwo) {
        return opt(getColumnName(columnOne), OPT.EQ, getColumnName(columnTwo));
    }

    /**
     * 通过Lambda表达式创建两个字段的相等条件（带前缀）
     *
     * @param prefixOne 第一个字段的前缀
     * @param columnOne 第一个字段的方法引用
     * @param prefixTwo 第二个字段的前缀
     * @param columnTwo 第二个字段的方法引用
     * @param <T>       第一个实体类型
     * @param <F>       第二个实体类型
     * @param <R>       返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, F, R> Cond eq(String prefixOne, SFunction<T, R> columnOne, String prefixTwo, SFunction<F, R> columnTwo) {
        return opt(Fields.field(prefixOne, getColumnName(columnOne)), OPT.EQ, Fields.field(prefixTwo, getColumnName(columnTwo)));
    }

    /**
     * 通过Lambda表达式创建两个字段的相等条件（带前缀，包装标识符）
     *
     * @param prefixOne 第一个字段的前缀
     * @param columnOne 第一个字段的方法引用
     * @param prefixTwo 第二个字段的前缀
     * @param columnTwo 第二个字段的方法引用
     * @param <T>       第一个实体类型
     * @param <F>       第二个实体类型
     * @param <R>       返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, F, R> Cond eqWrap(String prefixOne, SFunction<T, R> columnOne, String prefixTwo, SFunction<F, R> columnTwo) {
        return optWrap(Fields.field(prefixOne, getColumnName(columnOne)), OPT.EQ, Fields.field(prefixTwo, getColumnName(columnTwo)));
    }

    /**
     * 通过Lambda表达式创建相等条件（直接传值）
     *
     * @param column 方法引用
     * @param value  参数值
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond eq(SFunction<T, R> column, Object value) {
        return eq(column).param(value);
    }

    /**
     * 通过Lambda表达式创建相等条件（直接传值，带前缀）
     *
     * @param prefix 前缀
     * @param column 方法引用
     * @param value  参数值
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond eq(String prefix, SFunction<T, R> column, Object value) {
        return eq(prefix, column).param(value);
    }

    /**
     * 通过Lambda表达式创建相等条件（直接传值，带标识符包装）
     *
     * @param column 方法引用
     * @param value  参数值
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond eqWrap(SFunction<T, R> column, Object value) {
        return eqWrap(column).param(value);
    }

    /**
     * 通过Lambda表达式创建相等条件（直接传值，带前缀和标识符包装）
     *
     * @param prefix 前缀
     * @param column 方法引用
     * @param value  参数值
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond eqWrap(String prefix, SFunction<T, R> column, Object value) {
        return eqWrap(prefix, column).param(value);
    }

    // ------

    public Cond notEqField(String fieldOne, String fieldTwo) {
        return opt(fieldOne, OPT.NOT_EQ, fieldTwo);
    }

    public Cond notEqFieldWrap(String fieldOne, String fieldTwo) {
        return optWrap(fieldOne, OPT.NOT_EQ, fieldTwo);
    }

    public Cond notEq(String prefix, String field) {
        return opt(Fields.field(prefix, field), OPT.NOT_EQ);
    }

    public Cond notEq(String field) {
        return opt(field, OPT.NOT_EQ);
    }

    public Cond notEqWrap(String prefix, String field) {
        return optWrap(Fields.field(prefix, field), OPT.NOT_EQ);
    }

    public Cond notEqWrap(String field) {
        return optWrap(field, OPT.NOT_EQ);
    }

    public Cond notEq(IFunction funcOne, IFunction funcTwo) {
        return opt(funcOne, OPT.NOT_EQ, funcTwo);
    }

    public Cond notEq(IFunction func) {
        return opt(func, OPT.NOT_EQ);
    }

    // ---------- Lambda Support for NOT_EQ ----------

    /**
     * 通过Lambda表达式创建不等条件
     *
     * @param column 方法引用
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond notEq(SFunction<T, R> column) {
        return opt(getColumnName(column), OPT.NOT_EQ);
    }

    /**
     * 通过Lambda表达式创建不等条件（带前缀）
     *
     * @param prefix 前缀
     * @param column 方法引用
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond notEq(String prefix, SFunction<T, R> column) {
        return opt(Fields.field(prefix, getColumnName(column)), OPT.NOT_EQ);
    }

    /**
     * 通过Lambda表达式创建不等条件（带标识符包装）
     *
     * @param column 方法引用
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond notEqWrap(SFunction<T, R> column) {
        return optWrap(getColumnName(column), OPT.NOT_EQ);
    }

    /**
     * 通过Lambda表达式创建不等条件（带前缀和标识符包装）
     *
     * @param prefix 前缀
     * @param column 方法引用
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond notEqWrap(String prefix, SFunction<T, R> column) {
        return optWrap(Fields.field(prefix, getColumnName(column)), OPT.NOT_EQ);
    }

    /**
     * 通过Lambda表达式创建两个字段的不等条件
     *
     * @param columnOne 第一个字段的方法引用
     * @param columnTwo 第二个字段的方法引用
     * @param <T>       第一个实体类型
     * @param <F>       第二个实体类型
     * @param <R>       返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, F, R> Cond notEq(SFunction<T, R> columnOne, SFunction<F, R> columnTwo) {
        return opt(getColumnName(columnOne), OPT.NOT_EQ, getColumnName(columnTwo));
    }

    /**
     * 通过Lambda表达式创建两个字段的不等条件（带前缀）
     *
     * @param prefixOne 第一个字段的前缀
     * @param columnOne 第一个字段的方法引用
     * @param prefixTwo 第二个字段的前缀
     * @param columnTwo 第二个字段的方法引用
     * @param <T>       第一个实体类型
     * @param <F>       第二个实体类型
     * @param <R>       返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, F, R> Cond notEq(String prefixOne, SFunction<T, R> columnOne, String prefixTwo, SFunction<F, R> columnTwo) {
        return opt(Fields.field(prefixOne, getColumnName(columnOne)), OPT.NOT_EQ, Fields.field(prefixTwo, getColumnName(columnTwo)));
    }

    /**
     * 通过Lambda表达式创建两个字段的不等条件（带前缀，包装标识符）
     *
     * @param prefixOne 第一个字段的前缀
     * @param columnOne 第一个字段的方法引用
     * @param prefixTwo 第二个字段的前缀
     * @param columnTwo 第二个字段的方法引用
     * @param <T>       第一个实体类型
     * @param <F>       第二个实体类型
     * @param <R>       返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, F, R> Cond notEqWrap(String prefixOne, SFunction<T, R> columnOne, String prefixTwo, SFunction<F, R> columnTwo) {
        return optWrap(Fields.field(prefixOne, getColumnName(columnOne)), OPT.NOT_EQ, Fields.field(prefixTwo, getColumnName(columnTwo)));
    }

    /**
     * 通过Lambda表达式创建不等条件（直接传值）
     *
     * @param column 方法引用
     * @param value  参数值
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond notEq(SFunction<T, R> column, Object value) {
        return notEq(column).param(value);
    }

    /**
     * 通过Lambda表达式创建不等条件（直接传值，带前缀）
     *
     * @param prefix 前缀
     * @param column 方法引用
     * @param value  参数值
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond notEq(String prefix, SFunction<T, R> column, Object value) {
        return notEq(prefix, column).param(value);
    }

    /**
     * 通过Lambda表达式创建不等条件（直接传值，带标识符包装）
     *
     * @param column 方法引用
     * @param value  参数值
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond notEqWrap(SFunction<T, R> column, Object value) {
        return notEqWrap(column).param(value);
    }

    /**
     * 通过Lambda表达式创建不等条件（直接传值，带前缀和标识符包装）
     *
     * @param prefix 前缀
     * @param column 方法引用
     * @param value  参数值
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond notEqWrap(String prefix, SFunction<T, R> column, Object value) {
        return notEqWrap(prefix, column).param(value);
    }

    // ------

    public Cond gtEqField(String fieldOne, String fieldTwo) {
        return opt(fieldOne, OPT.GT_EQ, fieldTwo);
    }

    public Cond gtEqFieldWrap(String fieldOne, String fieldTwo) {
        return optWrap(fieldOne, OPT.GT_EQ, fieldTwo);
    }

    public Cond gtEq(String field) {
        return opt(field, OPT.GT_EQ);
    }

    public Cond gtEq(String prefix, String field) {
        return opt(Fields.field(prefix, field), OPT.GT_EQ);
    }

    public Cond gtEqWrap(String field) {
        return optWrap(field, OPT.GT_EQ);
    }

    public Cond gtEqWrap(String prefix, String field) {
        return optWrap(Fields.field(prefix, field), OPT.GT_EQ);
    }

    public Cond gtEq(IFunction funcOne, IFunction funcTwo) {
        return opt(funcOne, OPT.GT_EQ, funcTwo);
    }

    public Cond gtEq(IFunction func) {
        return opt(func, OPT.GT_EQ);
    }

    // ------

    public Cond gtField(String fieldOne, String fieldTwo) {
        return opt(fieldOne, OPT.GT, fieldTwo);
    }

    public Cond gtFieldWrap(String fieldOne, String fieldTwo) {
        return optWrap(fieldOne, OPT.GT, fieldTwo);
    }

    public Cond gt(String field) {
        return opt(field, OPT.GT);
    }

    public Cond gt(String prefix, String field) {
        return opt(Fields.field(prefix, field), OPT.GT);
    }

    public Cond gtWrap(String field) {
        return optWrap(field, OPT.GT);
    }

    public Cond gtWrap(String prefix, String field) {
        return optWrap(Fields.field(prefix, field), OPT.GT);
    }

    public Cond gt(IFunction funcOne, IFunction funcTwo) {
        return opt(funcOne, OPT.GT, funcTwo);
    }

    public Cond gt(IFunction func) {
        return opt(func, OPT.GT);
    }

    /**
     * 通过函数创建大于条件（函数值大于指定值）
     *
     * @param func  函数
     * @param value 值
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public Cond gt(IFunction func, Object value) {
        return gt(func).param(value);
    }

    // ---------- Lambda Support for GT and GT_EQ ----------

    /**
     * 通过Lambda表达式创建大于等于条件
     *
     * @param column 方法引用
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond gtEq(SFunction<T, R> column) {
        return opt(getColumnName(column), OPT.GT_EQ);
    }

    /**
     * 通过Lambda表达式创建大于等于条件（带前缀）
     *
     * @param prefix 前缀
     * @param column 方法引用
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond gtEq(String prefix, SFunction<T, R> column) {
        return opt(Fields.field(prefix, getColumnName(column)), OPT.GT_EQ);
    }

    /**
     * 通过Lambda表达式创建大于等于条件（带标识符包装）
     *
     * @param column 方法引用
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond gtEqWrap(SFunction<T, R> column) {
        return optWrap(getColumnName(column), OPT.GT_EQ);
    }

    /**
     * 通过Lambda表达式创建大于等于条件（带前缀和标识符包装）
     *
     * @param prefix 前缀
     * @param column 方法引用
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond gtEqWrap(String prefix, SFunction<T, R> column) {
        return optWrap(Fields.field(prefix, getColumnName(column)), OPT.GT_EQ);
    }

    /**
     * 通过Lambda表达式创建大于条件
     *
     * @param column 方法引用
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond gt(SFunction<T, R> column) {
        return opt(getColumnName(column), OPT.GT);
    }

    /**
     * 通过Lambda表达式创建大于条件（带前缀）
     *
     * @param prefix 前缀
     * @param column 方法引用
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond gt(String prefix, SFunction<T, R> column) {
        return opt(Fields.field(prefix, getColumnName(column)), OPT.GT);
    }

    /**
     * 通过Lambda表达式创建大于条件（带标识符包装）
     *
     * @param column 方法引用
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond gtWrap(SFunction<T, R> column) {
        return optWrap(getColumnName(column), OPT.GT);
    }

    /**
     * 通过Lambda表达式创建大于条件（带前缀和标识符包装）
     *
     * @param prefix 前缀
     * @param column 方法引用
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond gtWrap(String prefix, SFunction<T, R> column) {
        return optWrap(Fields.field(prefix, getColumnName(column)), OPT.GT);
    }

    /**
     * 通过Lambda表达式创建两个字段的大于条件
     *
     * @param columnOne 第一个字段的方法引用
     * @param columnTwo 第二个字段的方法引用
     * @param <T>       第一个实体类型
     * @param <F>       第二个实体类型
     * @param <R>       返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, F, R> Cond gt(SFunction<T, R> columnOne, SFunction<F, R> columnTwo) {
        return opt(getColumnName(columnOne), OPT.GT, getColumnName(columnTwo));
    }

    /**
     * 通过Lambda表达式创建两个字段的大于条件（带前缀）
     *
     * @param prefixOne 第一个字段的前缀
     * @param columnOne 第一个字段的方法引用
     * @param prefixTwo 第二个字段的前缀
     * @param columnTwo 第二个字段的方法引用
     * @param <T>       第一个实体类型
     * @param <F>       第二个实体类型
     * @param <R>       返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, F, R> Cond gt(String prefixOne, SFunction<T, R> columnOne, String prefixTwo, SFunction<F, R> columnTwo) {
        return opt(Fields.field(prefixOne, getColumnName(columnOne)), OPT.GT, Fields.field(prefixTwo, getColumnName(columnTwo)));
    }

    /**
     * 通过Lambda表达式创建两个字段的大于条件（带前缀，包装标识符）
     *
     * @param prefixOne 第一个字段的前缀
     * @param columnOne 第一个字段的方法引用
     * @param prefixTwo 第二个字段的前缀
     * @param columnTwo 第二个字段的方法引用
     * @param <T>       第一个实体类型
     * @param <F>       第二个实体类型
     * @param <R>       返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, F, R> Cond gtWrap(String prefixOne, SFunction<T, R> columnOne, String prefixTwo, SFunction<F, R> columnTwo) {
        return optWrap(Fields.field(prefixOne, getColumnName(columnOne)), OPT.GT, Fields.field(prefixTwo, getColumnName(columnTwo)));
    }

    /**
     * 通过Lambda表达式创建两个字段的大于等于条件
     *
     * @param columnOne 第一个字段的方法引用
     * @param columnTwo 第二个字段的方法引用
     * @param <T>       第一个实体类型
     * @param <F>       第二个实体类型
     * @param <R>       返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, F, R> Cond gtEq(SFunction<T, R> columnOne, SFunction<F, R> columnTwo) {
        return opt(getColumnName(columnOne), OPT.GT_EQ, getColumnName(columnTwo));
    }

    /**
     * 通过Lambda表达式创建两个字段的大于等于条件（带前缀）
     *
     * @param prefixOne 第一个字段的前缀
     * @param columnOne 第一个字段的方法引用
     * @param prefixTwo 第二个字段的前缀
     * @param columnTwo 第二个字段的方法引用
     * @param <T>       第一个实体类型
     * @param <F>       第二个实体类型
     * @param <R>       返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, F, R> Cond gtEq(String prefixOne, SFunction<T, R> columnOne, String prefixTwo, SFunction<F, R> columnTwo) {
        return opt(Fields.field(prefixOne, getColumnName(columnOne)), OPT.GT_EQ, Fields.field(prefixTwo, getColumnName(columnTwo)));
    }

    /**
     * 通过Lambda表达式创建两个字段的大于等于条件（带前缀，包装标识符）
     *
     * @param prefixOne 第一个字段的前缀
     * @param columnOne 第一个字段的方法引用
     * @param prefixTwo 第二个字段的前缀
     * @param columnTwo 第二个字段的方法引用
     * @param <T>       第一个实体类型
     * @param <F>       第二个实体类型
     * @param <R>       返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, F, R> Cond gtEqWrap(String prefixOne, SFunction<T, R> columnOne, String prefixTwo, SFunction<F, R> columnTwo) {
        return optWrap(Fields.field(prefixOne, getColumnName(columnOne)), OPT.GT_EQ, Fields.field(prefixTwo, getColumnName(columnTwo)));
    }

    /**
     * 通过Lambda表达式创建大于等于条件（直接传值）
     *
     * @param column 方法引用
     * @param value  参数值
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond gtEq(SFunction<T, R> column, Object value) {
        return gtEq(column).param(value);
    }

    /**
     * 通过Lambda表达式创建大于等于条件（直接传值，带前缀）
     *
     * @param prefix 前缀
     * @param column 方法引用
     * @param value  参数值
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond gtEq(String prefix, SFunction<T, R> column, Object value) {
        return gtEq(prefix, column).param(value);
    }

    /**
     * 通过Lambda表达式创建大于等于条件（直接传值，带标识符包装）
     *
     * @param column 方法引用
     * @param value  参数值
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond gtEqWrap(SFunction<T, R> column, Object value) {
        return gtEqWrap(column).param(value);
    }

    /**
     * 通过Lambda表达式创建大于等于条件（直接传值，带前缀和标识符包装）
     *
     * @param prefix 前缀
     * @param column 方法引用
     * @param value  参数值
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond gtEqWrap(String prefix, SFunction<T, R> column, Object value) {
        return gtEqWrap(prefix, column).param(value);
    }

    /**
     * 通过Lambda表达式创建大于条件（直接传值）
     *
     * @param column 方法引用
     * @param value  参数值
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond gt(SFunction<T, R> column, Object value) {
        return gt(column).param(value);
    }

    /**
     * 通过Lambda表达式创建大于条件（直接传值，带前缀）
     *
     * @param prefix 前缀
     * @param column 方法引用
     * @param value  参数值
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond gt(String prefix, SFunction<T, R> column, Object value) {
        return gt(prefix, column).param(value);
    }

    /**
     * 通过Lambda表达式创建大于条件（直接传值，带标识符包装）
     *
     * @param column 方法引用
     * @param value  参数值
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond gtWrap(SFunction<T, R> column, Object value) {
        return gtWrap(column).param(value);
    }

    /**
     * 通过Lambda表达式创建大于条件（直接传值，带前缀和标识符包装）
     *
     * @param prefix 前缀
     * @param column 方法引用
     * @param value  参数值
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond gtWrap(String prefix, SFunction<T, R> column, Object value) {
        return gtWrap(prefix, column).param(value);
    }

    // ------

    public Cond ltEqField(String fieldOne, String fieldTwo) {
        return opt(fieldOne, OPT.LT_EQ, fieldTwo);
    }

    public Cond ltEqFieldWrap(String fieldOne, String fieldTwo) {
        return optWrap(fieldOne, OPT.LT_EQ, fieldTwo);
    }

    public Cond ltEq(String field) {
        return opt(field, OPT.LT_EQ);
    }

    public Cond ltEq(String prefix, String field) {
        return opt(Fields.field(prefix, field), OPT.LT_EQ);
    }

    public Cond ltEqWrap(String field) {
        return optWrap(field, OPT.LT_EQ);
    }

    public Cond ltEqWrap(String prefix, String field) {
        return optWrap(Fields.field(prefix, field), OPT.LT_EQ);
    }

    public Cond ltEq(IFunction funcOne, IFunction funcTwo) {
        return opt(funcOne, OPT.LT_EQ, funcTwo);
    }

    public Cond ltEq(IFunction func) {
        return opt(func, OPT.LT_EQ);
    }

    // ------

    public Cond ltField(String fieldOne, String fieldTwo) {
        return opt(fieldOne, OPT.LT, fieldTwo);
    }

    public Cond ltFieldWrap(String fieldOne, String fieldTwo) {
        return optWrap(fieldOne, OPT.LT, fieldTwo);
    }

    public Cond lt(String field) {
        return opt(field, OPT.LT);
    }

    public Cond lt(String prefix, String field) {
        return opt(Fields.field(prefix, field), OPT.LT);
    }

    public Cond ltWrap(String field) {
        return optWrap(field, OPT.LT);
    }

    public Cond ltWrap(String prefix, String field) {
        return optWrap(Fields.field(prefix, field), OPT.LT);
    }

    public Cond lt(IFunction funcOne, IFunction funcTwo) {
        return opt(funcOne, OPT.LT, funcTwo);
    }

    public Cond lt(IFunction func) {
        return opt(func, OPT.LT);
    }

    // ---------- Lambda Support for LT and LT_EQ ----------

    /**
     * 通过Lambda表达式创建小于等于条件
     *
     * @param column 方法引用
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond ltEq(SFunction<T, R> column) {
        return opt(getColumnName(column), OPT.LT_EQ);
    }

    /**
     * 通过Lambda表达式创建小于等于条件（带前缀）
     *
     * @param prefix 前缀
     * @param column 方法引用
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond ltEq(String prefix, SFunction<T, R> column) {
        return opt(Fields.field(prefix, getColumnName(column)), OPT.LT_EQ);
    }

    /**
     * 通过Lambda表达式创建小于等于条件（带标识符包装）
     *
     * @param column 方法引用
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond ltEqWrap(SFunction<T, R> column) {
        return optWrap(getColumnName(column), OPT.LT_EQ);
    }

    /**
     * 通过Lambda表达式创建小于等于条件（带前缀和标识符包装）
     *
     * @param prefix 前缀
     * @param column 方法引用
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond ltEqWrap(String prefix, SFunction<T, R> column) {
        return optWrap(Fields.field(prefix, getColumnName(column)), OPT.LT_EQ);
    }

    /**
     * 通过Lambda表达式创建小于条件
     *
     * @param column 方法引用
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond lt(SFunction<T, R> column) {
        return opt(getColumnName(column), OPT.LT);
    }

    /**
     * 通过Lambda表达式创建小于条件（带前缀）
     *
     * @param prefix 前缀
     * @param column 方法引用
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond lt(String prefix, SFunction<T, R> column) {
        return opt(Fields.field(prefix, getColumnName(column)), OPT.LT);
    }

    /**
     * 通过Lambda表达式创建小于条件（带标识符包装）
     *
     * @param column 方法引用
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond ltWrap(SFunction<T, R> column) {
        return optWrap(getColumnName(column), OPT.LT);
    }

    /**
     * 通过Lambda表达式创建小于条件（带前缀和标识符包装）
     *
     * @param prefix 前缀
     * @param column 方法引用
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond ltWrap(String prefix, SFunction<T, R> column) {
        return optWrap(Fields.field(prefix, getColumnName(column)), OPT.LT);
    }

    /**
     * 通过Lambda表达式创建两个字段的小于条件
     *
     * @param columnOne 第一个字段的方法引用
     * @param columnTwo 第二个字段的方法引用
     * @param <T>       第一个实体类型
     * @param <F>       第二个实体类型
     * @param <R>       返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, F, R> Cond lt(SFunction<T, R> columnOne, SFunction<F, R> columnTwo) {
        return opt(getColumnName(columnOne), OPT.LT, getColumnName(columnTwo));
    }

    /**
     * 通过Lambda表达式创建两个字段的小于条件（带前缀）
     *
     * @param prefixOne 第一个字段的前缀
     * @param columnOne 第一个字段的方法引用
     * @param prefixTwo 第二个字段的前缀
     * @param columnTwo 第二个字段的方法引用
     * @param <T>       第一个实体类型
     * @param <F>       第二个实体类型
     * @param <R>       返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, F, R> Cond lt(String prefixOne, SFunction<T, R> columnOne, String prefixTwo, SFunction<F, R> columnTwo) {
        return opt(Fields.field(prefixOne, getColumnName(columnOne)), OPT.LT, Fields.field(prefixTwo, getColumnName(columnTwo)));
    }

    /**
     * 通过Lambda表达式创建两个字段的小于条件（带前缀，包装标识符）
     *
     * @param prefixOne 第一个字段的前缀
     * @param columnOne 第一个字段的方法引用
     * @param prefixTwo 第二个字段的前缀
     * @param columnTwo 第二个字段的方法引用
     * @param <T>       第一个实体类型
     * @param <F>       第二个实体类型
     * @param <R>       返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, F, R> Cond ltWrap(String prefixOne, SFunction<T, R> columnOne, String prefixTwo, SFunction<F, R> columnTwo) {
        return optWrap(Fields.field(prefixOne, getColumnName(columnOne)), OPT.LT, Fields.field(prefixTwo, getColumnName(columnTwo)));
    }

    /**
     * 通过Lambda表达式创建两个字段的小于等于条件
     *
     * @param columnOne 第一个字段的方法引用
     * @param columnTwo 第二个字段的方法引用
     * @param <T>       第一个实体类型
     * @param <F>       第二个实体类型
     * @param <R>       返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, F, R> Cond ltEq(SFunction<T, R> columnOne, SFunction<F, R> columnTwo) {
        return opt(getColumnName(columnOne), OPT.LT_EQ, getColumnName(columnTwo));
    }

    /**
     * 通过Lambda表达式创建两个字段的小于等于条件（带前缀）
     *
     * @param prefixOne 第一个字段的前缀
     * @param columnOne 第一个字段的方法引用
     * @param prefixTwo 第二个字段的前缀
     * @param columnTwo 第二个字段的方法引用
     * @param <T>       第一个实体类型
     * @param <F>       第二个实体类型
     * @param <R>       返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, F, R> Cond ltEq(String prefixOne, SFunction<T, R> columnOne, String prefixTwo, SFunction<F, R> columnTwo) {
        return opt(Fields.field(prefixOne, getColumnName(columnOne)), OPT.LT_EQ, Fields.field(prefixTwo, getColumnName(columnTwo)));
    }

    /**
     * 通过Lambda表达式创建两个字段的小于等于条件（带前缀，包装标识符）
     *
     * @param prefixOne 第一个字段的前缀
     * @param columnOne 第一个字段的方法引用
     * @param prefixTwo 第二个字段的前缀
     * @param columnTwo 第二个字段的方法引用
     * @param <T>       第一个实体类型
     * @param <F>       第二个实体类型
     * @param <R>       返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, F, R> Cond ltEqWrap(String prefixOne, SFunction<T, R> columnOne, String prefixTwo, SFunction<F, R> columnTwo) {
        return optWrap(Fields.field(prefixOne, getColumnName(columnOne)), OPT.LT_EQ, Fields.field(prefixTwo, getColumnName(columnTwo)));
    }

    /**
     * 通过Lambda表达式创建小于等于条件（直接传值）
     *
     * @param column 方法引用
     * @param value  参数值
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond ltEq(SFunction<T, R> column, Object value) {
        return ltEq(column).param(value);
    }

    /**
     * 通过Lambda表达式创建小于等于条件（直接传值，带前缀）
     *
     * @param prefix 前缀
     * @param column 方法引用
     * @param value  参数值
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond ltEq(String prefix, SFunction<T, R> column, Object value) {
        return ltEq(prefix, column).param(value);
    }

    /**
     * 通过Lambda表达式创建小于等于条件（直接传值，带标识符包装）
     *
     * @param column 方法引用
     * @param value  参数值
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond ltEqWrap(SFunction<T, R> column, Object value) {
        return ltEqWrap(column).param(value);
    }

    /**
     * 通过Lambda表达式创建小于等于条件（直接传值，带前缀和标识符包装）
     *
     * @param prefix 前缀
     * @param column 方法引用
     * @param value  参数值
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond ltEqWrap(String prefix, SFunction<T, R> column, Object value) {
        return ltEqWrap(prefix, column).param(value);
    }

    /**
     * 通过Lambda表达式创建小于条件（直接传值）
     *
     * @param column 方法引用
     * @param value  参数值
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond lt(SFunction<T, R> column, Object value) {
        return lt(column).param(value);
    }

    /**
     * 通过Lambda表达式创建小于条件（直接传值，带前缀）
     *
     * @param prefix 前缀
     * @param column 方法引用
     * @param value  参数值
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond lt(String prefix, SFunction<T, R> column, Object value) {
        return lt(prefix, column).param(value);
    }

    /**
     * 通过Lambda表达式创建小于条件（直接传值，带标识符包装）
     *
     * @param column 方法引用
     * @param value  参数值
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond ltWrap(SFunction<T, R> column, Object value) {
        return ltWrap(column).param(value);
    }

    /**
     * 通过Lambda表达式创建小于条件（直接传值，带前缀和标识符包装）
     *
     * @param prefix 前缀
     * @param column 方法引用
     * @param value  参数值
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond ltWrap(String prefix, SFunction<T, R> column, Object value) {
        return ltWrap(prefix, column).param(value);
    }

    // ------

    public Cond like(String field) {
        return opt(field, OPT.LIKE);
    }

    public Cond like(String prefix, String field) {
        return opt(Fields.field(prefix, field), OPT.LIKE);
    }

    public Cond likeWrap(String field) {
        return optWrap(field, OPT.LIKE);
    }

    public Cond likeWrap(String prefix, String field) {
        return optWrap(Fields.field(prefix, field), OPT.LIKE);
    }

    public Cond like(IFunction func) {
        return opt(func, OPT.LIKE);
    }

    /**
     * 配合like使用，指定不同的转义符
     *
     * @param escapeChar 转义字符
     * @return 返回当前条件对象
     * @since 2.1.3
     */
    public Cond escape(char escapeChar) {
        return cond("ESCAPE ?").param(String.valueOf(escapeChar));
    }

    // ------

    /**
     * @since 2.1.4
     */
    public Cond rlike(String field) {
        return opt(field, OPT.RLIKE);
    }

    /**
     * @since 2.1.4
     */
    public Cond rlike(String prefix, String field) {
        return opt(Fields.field(prefix, field), OPT.RLIKE);
    }

    /**
     * @since 2.1.4
     */
    public Cond rlikeWrap(String field) {
        return optWrap(field, OPT.RLIKE);
    }

    /**
     * @since 2.1.4
     */
    public Cond rlikeWrap(String prefix, String field) {
        return optWrap(Fields.field(prefix, field), OPT.RLIKE);
    }

    /**
     * @since 2.1.4
     */
    public Cond rlike(IFunction func) {
        return opt(func, OPT.RLIKE);
    }

    // ------

    /**
     * @since 2.1.4
     */
    public Cond regexp(String field) {
        return opt(field, OPT.REGEXP);
    }

    /**
     * @since 2.1.4
     */
    public Cond regexp(String prefix, String field) {
        return opt(Fields.field(prefix, field), OPT.REGEXP);
    }

    /**
     * @since 2.1.4
     */
    public Cond regexpWrap(String field) {
        return optWrap(field, OPT.REGEXP);
    }

    /**
     * @since 2.1.4
     */
    public Cond regexpWrap(String prefix, String field) {
        return optWrap(Fields.field(prefix, field), OPT.REGEXP);
    }

    /**
     * @since 2.1.4
     */
    public Cond regexp(IFunction func) {
        return opt(func, OPT.REGEXP);
    }

    // ---------- Lambda Support for LIKE, RLIKE, REGEXP ----------

    /**
     * 通过Lambda表达式创建模糊查询条件
     *
     * @param column 方法引用
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond like(SFunction<T, R> column) {
        return opt(getColumnName(column), OPT.LIKE);
    }

    /**
     * 通过Lambda表达式创建模糊查询条件（带前缀）
     *
     * @param prefix 前缀
     * @param column 方法引用
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond like(String prefix, SFunction<T, R> column) {
        return opt(Fields.field(prefix, getColumnName(column)), OPT.LIKE);
    }

    /**
     * 通过Lambda表达式创建模糊查询条件（带标识符包装）
     *
     * @param column 方法引用
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond likeWrap(SFunction<T, R> column) {
        return optWrap(getColumnName(column), OPT.LIKE);
    }

    /**
     * 通过Lambda表达式创建模糊查询条件（带前缀和标识符包装）
     *
     * @param prefix 前缀
     * @param column 方法引用
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond likeWrap(String prefix, SFunction<T, R> column) {
        return optWrap(Fields.field(prefix, getColumnName(column)), OPT.LIKE);
    }

    /**
     * 通过Lambda表达式创建正则表达式查询条件（RLIKE）
     *
     * @param column 方法引用
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond rlike(SFunction<T, R> column) {
        return opt(getColumnName(column), OPT.RLIKE);
    }

    /**
     * 通过Lambda表达式创建正则表达式查询条件（RLIKE，带前缀）
     *
     * @param prefix 前缀
     * @param column 方法引用
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond rlike(String prefix, SFunction<T, R> column) {
        return opt(Fields.field(prefix, getColumnName(column)), OPT.RLIKE);
    }

    /**
     * 通过Lambda表达式创建正则表达式查询条件（RLIKE，带标识符包装）
     *
     * @param column 方法引用
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond rlikeWrap(SFunction<T, R> column) {
        return optWrap(getColumnName(column), OPT.RLIKE);
    }

    /**
     * 通过Lambda表达式创建正则表达式查询条件（RLIKE，带前缀和标识符包装）
     *
     * @param prefix 前缀
     * @param column 方法引用
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond rlikeWrap(String prefix, SFunction<T, R> column) {
        return optWrap(Fields.field(prefix, getColumnName(column)), OPT.RLIKE);
    }

    /**
     * 通过Lambda表达式创建正则表达式查询条件（REGEXP）
     *
     * @param column 方法引用
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond regexp(SFunction<T, R> column) {
        return opt(getColumnName(column), OPT.REGEXP);
    }

    /**
     * 通过Lambda表达式创建正则表达式查询条件（REGEXP，带前缀）
     *
     * @param prefix 前缀
     * @param column 方法引用
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond regexp(String prefix, SFunction<T, R> column) {
        return opt(Fields.field(prefix, getColumnName(column)), OPT.REGEXP);
    }

    /**
     * 通过Lambda表达式创建正则表达式查询条件（REGEXP，带标识符包装）
     *
     * @param column 方法引用
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond regexpWrap(SFunction<T, R> column) {
        return optWrap(getColumnName(column), OPT.REGEXP);
    }

    /**
     * 通过Lambda表达式创建正则表达式查询条件（REGEXP，带前缀和标识符包装）
     *
     * @param prefix 前缀
     * @param column 方法引用
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond regexpWrap(String prefix, SFunction<T, R> column) {
        return optWrap(Fields.field(prefix, getColumnName(column)), OPT.REGEXP);
    }

    /**
     * 通过Lambda表达式创建模糊查询条件（直接传值）
     *
     * @param column 方法引用
     * @param value  参数值
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond like(SFunction<T, R> column, Object value) {
        return like(column).param(value);
    }

    /**
     * 通过Lambda表达式创建模糊查询条件（直接传值，带前缀）
     *
     * @param prefix 前缀
     * @param column 方法引用
     * @param value  参数值
     * @param <T>    类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond like(String prefix, SFunction<T, R> column, Object value) {
        return like(prefix, column).param(value);
    }

    /**
     * 通过Lambda表达式创建模糊查询条件（直接传值，带标识符包装）
     *
     * @param column 方法引用
     * @param value  参数值
     * @param <T>    类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond likeWrap(SFunction<T, R> column, Object value) {
        return likeWrap(column).param(value);
    }

    /**
     * 通过Lambda表达式创建模糊查询条件（直接传值，带前缀和标识符包装）
     *
     * @param prefix 前缀
     * @param column 方法引用
     * @param value  参数值
     * @param <T>    类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond likeWrap(String prefix, SFunction<T, R> column, Object value) {
        return likeWrap(prefix, column).param(value);
    }

    /**
     * 通过Lambda表达式创建正则表达式查询条件（RLIKE，直接传值）
     *
     * @param column 方法引用
     * @param value  参数值
     * @param <T>    类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond rlike(SFunction<T, R> column, Object value) {
        return rlike(column).param(value);
    }

    /**
     * 通过Lambda表达式创建正则表达式查询条件（RLIKE，直接传值，带前缀）
     *
     * @param prefix 前缀
     * @param column 方法引用
     * @param value  参数值
     * @param <T>    类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond rlike(String prefix, SFunction<T, R> column, Object value) {
        return rlike(prefix, column).param(value);
    }

    /**
     * 通过Lambda表达式创建正则表达式查询条件（RLIKE，直接传值，带标识符包装）
     *
     * @param column 方法引用
     * @param value  参数值
     * @param <T>    类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond rlikeWrap(SFunction<T, R> column, Object value) {
        return rlikeWrap(column).param(value);
    }

    /**
     * 通过Lambda表达式创建正则表达式查询条件（RLIKE，直接传值，带前缀和标识符包装）
     *
     * @param prefix 前缀
     * @param column 方法引用
     * @param value  参数值
     * @param <T>    类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond rlikeWrap(String prefix, SFunction<T, R> column, Object value) {
        return rlikeWrap(prefix, column).param(value);
    }

    /**
     * 通过Lambda表达式创建正则表达式查询条件（REGEXP，直接传值）
     *
     * @param column 方法引用
     * @param value  参数值
     * @param <T>    类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond regexp(SFunction<T, R> column, Object value) {
        return regexp(column).param(value);
    }

    /**
     * 通过Lambda表达式创建正则表达式查询条件（REGEXP，直接传值，带前缀）
     *
     * @param prefix 前缀
     * @param column 方法引用
     * @param value  参数值
     * @param <T>    类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond regexp(String prefix, SFunction<T, R> column, Object value) {
        return regexp(prefix, column).param(value);
    }

    /**
     * 通过Lambda表达式创建正则表达式查询条件（REGEXP，直接传值，带标识符包装）
     *
     * @param column 方法引用
     * @param value  参数值
     * @param <T>    类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond regexpWrap(SFunction<T, R> column, Object value) {
        return regexpWrap(column).param(value);
    }

    /**
     * 通过Lambda表达式创建正则表达式查询条件（REGEXP，直接传值，带前缀和标识符包装）
     *
     * @param prefix 前缀
     * @param column 方法引用
     * @param value  参数值
     * @param <T>    类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond regexpWrap(String prefix, SFunction<T, R> column, Object value) {
        return regexpWrap(prefix, column).param(value);
    }

    // ------

    public Cond between(String field, Object valueOne, Object valueTwo) {
        return between(null, field, valueOne, valueTwo);
    }

    public Cond between(String prefix, String field, Object valueOne, Object valueTwo) {
        params.add(valueOne).add(valueTwo);
        return cond(String.format("%s BETWEEN ? AND ?", Fields.field(prefix, field)));
    }

    public Cond betweenWrap(String field, Object valueOne, Object valueTwo) {
        return betweenWrap(null, field, valueOne, valueTwo);
    }

    public Cond betweenWrap(String prefix, String field, Object valueOne, Object valueTwo) {
        return between(prefix, wrapIdentifierField(field), valueOne, valueTwo);
    }

    public Cond between(IFunction func, Object valueOne, Object valueTwo) {
        return between(func.build(), valueOne, valueTwo).param(func.params());
    }

    // ------

    /**
     * @since 2.1.4
     */
    public Cond range(String field, Object valueOne, Object valueTwo) {
        return range(null, field, valueOne, valueTwo, null);
    }

    public Cond range(String field, Object valueOne, Object valueTwo, LogicalOpt opt) {
        return range(null, field, valueOne, valueTwo, opt);
    }

    /**
     * @since 2.1.4
     */
    public Cond range(String prefix, String field, Object valueOne, Object valueTwo) {
        return range(prefix, field, valueOne, valueTwo, null);
    }

    public Cond range(String prefix, String field, Object valueOne, Object valueTwo, LogicalOpt opt) {
        if (valueOne != null && valueTwo != null) {
            if (opt != null) {
                opt(opt);
            }
            between(prefix, field, valueOne, valueTwo);
        } else if (valueOne != null) {
            if (opt != null) {
                opt(opt);
            }
            gtEq(prefix, field).param(valueOne);
        } else if (valueTwo != null) {
            if (opt != null) {
                opt(opt);
            }
            ltEq(prefix, field).param(valueTwo);
        }
        return this;
    }

    /**
     * @since 2.1.4
     */
    public Cond rangeWrap(String field, Object valueOne, Object valueTwo) {
        return rangeWrap(null, field, valueOne, valueTwo, null);
    }

    public Cond rangeWrap(String field, Object valueOne, Object valueTwo, LogicalOpt opt) {
        return rangeWrap(null, field, valueOne, valueTwo, opt);
    }

    /**
     * @since 2.1.4
     */
    public Cond rangeWrap(String prefix, String field, Object valueOne, Object valueTwo) {
        return rangeWrap(prefix, field, valueOne, valueTwo, null);
    }

    public Cond rangeWrap(String prefix, String field, Object valueOne, Object valueTwo, LogicalOpt opt) {
        return range(prefix, wrapIdentifierField(field), valueOne, valueTwo, opt);
    }

    /**
     * @since 2.1.4
     */
    public Cond range(IFunction func, Object valueOne, Object valueTwo) {
        return range(func.build(), valueOne, valueTwo, null).param(func.params());
    }

    public Cond range(IFunction func, Object valueOne, Object valueTwo, LogicalOpt opt) {
        return range(func.build(), valueOne, valueTwo, opt).param(func.params());
    }

    /**
     * @since 2.1.4
     */
    public Cond range(String field, DateTimeValue dateTimeValue) {
        return range(null, field, dateTimeValue.getStartDateTimeMillisOrNull(), dateTimeValue.getEndDateTimeMillisOrNull(), null);
    }

    /**
     * @since 2.1.3
     */
    public Cond range(String field, DateTimeValue dateTimeValue, LogicalOpt opt) {
        return range(null, field, dateTimeValue.getStartDateTimeMillisOrNull(), dateTimeValue.getEndDateTimeMillisOrNull(), opt);
    }

    /**
     * @since 2.1.4
     */
    public Cond range(String prefix, String field, DateTimeValue dateTimeValue) {
        return range(prefix, field, dateTimeValue.getStartDateTimeMillisOrNull(), dateTimeValue.getEndDateTimeMillisOrNull(), null);
    }

    /**
     * @since 2.1.3
     */
    public Cond range(String prefix, String field, DateTimeValue dateTimeValue, LogicalOpt opt) {
        return range(prefix, field, dateTimeValue.getStartDateTimeMillisOrNull(), dateTimeValue.getEndDateTimeMillisOrNull(), opt);
    }

    /**
     * @since 2.1.4
     */
    public Cond rangeWrap(String field, DateTimeValue dateTimeValue) {
        return rangeWrap(null, field, dateTimeValue.getStartDateTimeMillisOrNull(), dateTimeValue.getEndDateTimeMillisOrNull(), null);
    }

    /**
     * @since 2.1.3
     */
    public Cond rangeWrap(String field, DateTimeValue dateTimeValue, LogicalOpt opt) {
        return rangeWrap(null, field, dateTimeValue.getStartDateTimeMillisOrNull(), dateTimeValue.getEndDateTimeMillisOrNull(), opt);
    }

    /**
     * @since 2.1.4
     */
    public Cond rangeWrap(String prefix, String field, DateTimeValue dateTimeValue) {
        return range(prefix, wrapIdentifierField(field), dateTimeValue.getStartDateTimeMillisOrNull(), dateTimeValue.getEndDateTimeMillisOrNull(), null);
    }

    /**
     * @since 2.1.3
     */
    public Cond rangeWrap(String prefix, String field, DateTimeValue dateTimeValue, LogicalOpt opt) {
        return range(prefix, wrapIdentifierField(field), dateTimeValue.getStartDateTimeMillisOrNull(), dateTimeValue.getEndDateTimeMillisOrNull(), opt);
    }

    /**
     * @since 2.1.4
     */
    public Cond range(IFunction func, DateTimeValue dateTimeValue) {
        return range(func.build(), dateTimeValue.getStartDateTimeMillisOrNull(), dateTimeValue.getEndDateTimeMillisOrNull(), null).param(func.params());
    }

    /**
     * @since 2.1.3
     */
    public Cond range(IFunction func, DateTimeValue dateTimeValue, LogicalOpt opt) {
        return range(func.build(), dateTimeValue.getStartDateTimeMillisOrNull(), dateTimeValue.getEndDateTimeMillisOrNull(), opt).param(func.params());
    }

    // ------

    public Cond isNull(String prefix, String field) {
        return isNull(Fields.field(prefix, field));
    }

    public Cond isNull(String field) {
        return cond(String.format("%s IS NULL", field));
    }

    public Cond isNullWrap(String prefix, String field) {
        return isNull(prefix, wrapIdentifierField(field));
    }

    public Cond isNullWrap(String field) {
        return isNull(null, wrapIdentifierField(field));
    }

    public Cond isNull(IFunction func) {
        return isNull(func.build()).param(func.params());
    }

    // ------

    public Cond isNotNull(String prefix, String field) {
        return isNotNull(Fields.field(prefix, field));
    }

    public Cond isNotNull(String field) {
        return cond(String.format("%s IS NOT NULL", field));
    }

    public Cond isNotNullWrap(String prefix, String field) {
        return isNotNull(prefix, wrapIdentifierField(field));
    }

    public Cond isNotNullWrap(String field) {
        return isNotNull(null, wrapIdentifierField(field));
    }

    public Cond isNotNull(IFunction func) {
        return isNotNull(func.build()).param(func.params());
    }

    // ---------- Lambda Support for NULL, BETWEEN, IN ----------

    /**
     * 通过Lambda表达式创建IS NULL条件
     *
     * @param column 方法引用
     * @param <T>    类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond isNull(SFunction<T, R> column) {
        return isNull(getColumnName(column));
    }

    /**
     * 通过Lambda表达式创建IS NULL条件（带前缀）
     *
     * @param prefix 前缀
     * @param column 方法引用
     * @param <T>    类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond isNull(String prefix, SFunction<T, R> column) {
        return isNull(Fields.field(prefix, getColumnName(column)));
    }

    /**
     * 通过Lambda表达式创建IS NULL条件（带标识符包装）
     *
     * @param column 方法引用
     * @param <T>    类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond isNullWrap(SFunction<T, R> column) {
        return isNullWrap(null, column);
    }

    /**
     * 通过Lambda表达式创建IS NULL条件（带前缀和标识符包装）
     *
     * @param prefix 前缀
     * @param column 方法引用
     * @param <T>    类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond isNullWrap(String prefix, SFunction<T, R> column) {
        return isNull(prefix, wrapIdentifierField(getColumnName(column)));
    }

    /**
     * 通过Lambda表达式创建IS NOT NULL条件
     *
     * @param column 方法引用
     * @param <T>    类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond isNotNull(SFunction<T, R> column) {
        return isNotNull(getColumnName(column));
    }

    /**
     * 通过Lambda表达式创建IS NOT NULL条件（带前缀）
     *
     * @param prefix 前缀
     * @param column 方法引用
     * @param <T>    类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond isNotNull(String prefix, SFunction<T, R> column) {
        return isNotNull(Fields.field(prefix, getColumnName(column)));
    }

    /**
     * 通过Lambda表达式创建IS NOT NULL条件（带标识符包装）
     *
     * @param column 方法引用
     * @param <T>    类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond isNotNullWrap(SFunction<T, R> column) {
        return isNotNullWrap(null, column);
    }

    /**
     * 通过Lambda表达式创建IS NOT NULL条件（带前缀和标识符包装）
     *
     * @param prefix 前缀
     * @param column 方法引用
     * @param <T>    类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond isNotNullWrap(String prefix, SFunction<T, R> column) {
        return isNotNull(prefix, wrapIdentifierField(getColumnName(column)));
    }

    /**
     * 通过Lambda表达式创建BETWEEN条件
     *
     * @param column   方法引用
     * @param valueOne 起始值
     * @param valueTwo 结束值
     * @param <T>      类型
     * @param <R>      返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond between(SFunction<T, R> column, Object valueOne, Object valueTwo) {
        return between(null, column, valueOne, valueTwo);
    }

    /**
     * 通过Lambda表达式创建BETWEEN条件（带前缀）
     *
     * @param prefix   前缀
     * @param column   方法引用
     * @param valueOne 起始值
     * @param valueTwo 结束值
     * @param <T>      类型
     * @param <R>      返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond between(String prefix, SFunction<T, R> column, Object valueOne, Object valueTwo) {
        String columnName = getColumnName(column);
        return between(prefix, columnName, valueOne, valueTwo);
    }

    /**
     * 通过Lambda表达式创建BETWEEN条件（带标识符包装）
     *
     * @param column   方法引用
     * @param valueOne 起始值
     * @param valueTwo 结束值
     * @param <T>      类型
     * @param <R>      返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond betweenWrap(SFunction<T, R> column, Object valueOne, Object valueTwo) {
        return betweenWrap(null, column, valueOne, valueTwo);
    }

    /**
     * 通过Lambda表达式创建BETWEEN条件（带前缀和标识符包装）
     *
     * @param prefix   前缀
     * @param column   方法引用
     * @param valueOne 起始值
     * @param valueTwo 结束值
     * @param <T>      实体类型
     * @param <R>      返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond betweenWrap(String prefix, SFunction<T, R> column, Object valueOne, Object valueTwo) {
        String columnName = getColumnName(column);
        return between(prefix, wrapIdentifierField(columnName), valueOne, valueTwo);
    }

    /**
     * 通过Lambda表达式创建IN条件（子查询）
     *
     * @param column 方法引用
     * @param subSql 子查询SQL
     * @param <T>    类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond in(SFunction<T, R> column, SQL subSql) {
        return in(null, column, subSql);
    }

    /**
     * 通过Lambda表达式创建IN条件（子查询，带前缀）
     *
     * @param prefix 前缀
     * @param column 方法引用
     * @param subSql 子查询SQL
     * @param <T>    类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond in(String prefix, SFunction<T, R> column, SQL subSql) {
        String columnName = getColumnName(column);
        return in(Fields.field(prefix, columnName), subSql);
    }

    /**
     * 通过Lambda表达式创建IN条件（子查询，带标识符包装）
     *
     * @param column 方法引用
     * @param subSql 子查询SQL
     * @param <T>    类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond inWrap(SFunction<T, R> column, SQL subSql) {
        return inWrap(null, column, subSql);
    }

    /**
     * 通过Lambda表达式创建IN条件（子查询，带前缀和标识符包装）
     *
     * @param prefix 前缀
     * @param column 方法引用
     * @param subSql 子查询SQL
     * @param <T>    类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond inWrap(String prefix, SFunction<T, R> column, SQL subSql) {
        String columnName = getColumnName(column);
        return in(prefix, wrapIdentifierField(columnName), subSql);
    }

    /**
     * 通过Lambda表达式创建IN条件（子查询）
     *
     * @param column 方法引用
     * @param subSql 子查询Select对象
     * @param <T>    类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond in(SFunction<T, R> column, Select subSql) {
        return in(null, column, subSql);
    }

    /**
     * 通过Lambda表达式创建IN条件（子查询，带前缀）
     *
     * @param prefix 前缀
     * @param column 方法引用
     * @param subSql 子查询Select对象
     * @param <T>    类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond in(String prefix, SFunction<T, R> column, Select subSql) {
        String columnName = getColumnName(column);
        return in(Fields.field(prefix, columnName), subSql);
    }

    /**
     * 通过Lambda表达式创建IN条件（子查询，带标识符包装）
     *
     * @param column 方法引用
     * @param subSql 子查询Select对象
     * @param <T>    类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond inWrap(SFunction<T, R> column, Select subSql) {
        return inWrap(null, column, subSql);
    }

    /**
     * 通过Lambda表达式创建IN条件（子查询，带前缀和标识符包装）
     *
     * @param prefix 前缀
     * @param column 方法引用
     * @param subSql 子查询Select对象
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond inWrap(String prefix, SFunction<T, R> column, Select subSql) {
        String columnName = getColumnName(column);
        return in(prefix, wrapIdentifierField(columnName), subSql);
    }

    /**
     * 通过Lambda表达式创建IN条件（参数列表）
     *
     * @param column 方法引用
     * @param params 参数列表
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond in(SFunction<T, R> column, Params params) {
        return in(null, column, params);
    }

    /**
     * 通过Lambda表达式创建IN条件（参数列表，带前缀）
     *
     * @param prefix 前缀
     * @param column 方法引用
     * @param params 参数列表
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond in(String prefix, SFunction<T, R> column, Params params) {
        String columnName = getColumnName(column);
        return in(prefix, columnName, params);
    }

    /**
     * 通过Lambda表达式创建IN条件（参数列表，带标识符包装）
     *
     * @param column 方法引用
     * @param params 参数列表
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond inWrap(SFunction<T, R> column, Params params) {
        return inWrap(null, column, params);
    }

    /**
     * 通过Lambda表达式创建IN条件（参数列表，带前缀和标识符包装）
     *
     * @param prefix 前缀
     * @param column 方法引用
     * @param params 参数列表
     * @param <T>    实体类型
     * @param <R>    返回值类型
     * @return 当前Cond实例
     * @since 2.1.4
     */
    public <T, R> Cond inWrap(String prefix, SFunction<T, R> column, Params params) {
        String columnName = getColumnName(column);
        return in(prefix, wrapIdentifierField(columnName), params);
    }

    // ------

    public Cond and() {
        return opt(LogicalOpt.AND);
    }

    public Cond and(Cond cond) {
        return and().cond(cond);
    }

    public Cond and(FieldCondition cond) {
        return and().cond(cond);
    }

    public Cond andIfNeed() {
        return optIfNeed(LogicalOpt.AND);
    }

    public Cond andIfNeed(Cond cond) {
        return andIfNeed().cond(cond);
    }

    public Cond andIfNeed(FieldCondition cond) {
        return andIfNeed().cond(cond);
    }

    public Cond or() {
        return opt(LogicalOpt.OR);
    }

    public Cond or(Cond cond) {
        return or().cond(cond);
    }

    public Cond or(FieldCondition cond) {
        return or().cond(cond);
    }

    public Cond orIfNeed() {
        return optIfNeed(LogicalOpt.OR);
    }

    public Cond orIfNeed(Cond cond) {
        return orIfNeed().cond(cond);
    }

    public Cond orIfNeed(FieldCondition cond) {
        return orIfNeed().cond(cond);
    }

    public Cond not() {
        return opt(LogicalOpt.NOT);
    }

    public Cond not(Cond cond) {
        return not().cond(cond);
    }

    public Cond not(FieldCondition cond) {
        return not().cond(cond);
    }

    public Cond notIfNeed() {
        return optIfNeed(LogicalOpt.NOT);
    }

    public Cond notIfNeed(Cond cond) {
        return notIfNeed().cond(cond);
    }

    public Cond notIfNeed(FieldCondition cond) {
        return notIfNeed().cond(cond);
    }

    public Cond bracketBegin() {
        return cond("(");
    }

    public Cond bracketEnd() {
        return cond(")");
    }

    public Cond bracket(Cond cond) {
        return bracketBegin().cond(cond).bracketEnd();
    }

    public Cond bracket(FieldCondition cond) {
        return bracketBegin().cond(cond).bracketEnd();
    }

    public Cond brackets() {
        brackets = true;
        return this;
    }

    public Cond exists(SQL subSql) {
        params.add(subSql.params());
        return cond(String.format("EXISTS (%s)", subSql));
    }

    public Cond exists(Select subSql) {
        params.add(subSql.params());
        return cond(String.format("EXISTS (%s)", subSql));
    }

    // ------

    public Cond in(String prefix, String field, SQL subSql) {
        return in(Fields.field(prefix, field), subSql);
    }

    public Cond in(String field, SQL subSql) {
        params.add(subSql.params());
        return cond(String.format("%s IN (%s)", field, subSql));
    }

    public Cond inWrap(String prefix, String field, SQL subSql) {
        return in(prefix, wrapIdentifierField(field), subSql);
    }

    public Cond inWrap(String field, SQL subSql) {
        return in(null, wrapIdentifierField(field), subSql);
    }

    public Cond in(String prefix, String field, Select subSql) {
        return in(Fields.field(prefix, field), subSql);
    }

    public Cond in(String field, Select subSql) {
        params.add(subSql.params());
        return cond(String.format("%s IN (%s)", field, subSql));
    }

    public Cond inWrap(String prefix, String field, Select subSql) {
        return in(prefix, wrapIdentifierField(field), subSql);
    }

    public Cond inWrap(String field, Select subSql) {
        return in(null, wrapIdentifierField(field), subSql);
    }

    public Cond in(String prefix, String field, Params params) {
        return in(Fields.field(prefix, field), params);
    }

    public Cond in(String field, Params params) {
        this.params.add(params);
        return cond(String.format("%s IN (%s)", field, StringUtils.repeat("?", LINE_END_FLAG, params.params().size())));
    }

    public Cond inWrap(String prefix, String field, Params params) {
        return in(prefix, wrapIdentifierField(field), params);
    }

    public Cond inWrap(String field, Params params) {
        return in(null, wrapIdentifierField(field), params);
    }

    /**
     * @param expression 逻辑表达式
     * @param builder    条件构建器
     * @return 根据逻辑表达式运算结果决定是否采纳cond条件
     */
    public Cond expr(boolean expression, IConditionBuilder builder) {
        if (expression && builder != null) {
            this.cond(builder.build());
        }
        return this;
    }

    /**
     * @param expression 逻辑表达式
     * @param appender   条件追加器
     * @return 根据逻辑表达式运算结果决定是否采纳cond条件
     */
    public Cond expr(boolean expression, IConditionAppender appender) {
        if (expression && appender != null) {
            appender.append(this);
        }
        return this;
    }

    public Cond expr(boolean expression, String cond) {
        if (expression && cond != null) {
            this.cond(cond);
        }
        return this;
    }

    /**
     * @since 2.1.3
     */
    public Cond expr(DateTimeValue expression, IConditionBuilder builder) {
        if (expression != null && (!expression.isNullStartDate() || !expression.isNullEndDate()) && builder != null) {
            this.cond(builder.build());
        }
        return this;
    }

    /**
     * @since 2.1.3
     */
    public Cond expr(DateTimeValue expression, IConditionAppender appender) {
        if (expression != null && (!expression.isNullStartDate() || !expression.isNullEndDate()) && appender != null) {
            appender.append(this);
        }
        return this;
    }

    /**
     * @since 2.1.3
     */
    public Cond expr(DateTimeValue expression, String cond) {
        if (expression != null && (!expression.isNullStartDate() || !expression.isNullEndDate()) && cond != null) {
            this.cond(cond);
        }
        return this;
    }

    private boolean doCheckNotEmpty(Object target) {
        if (target != null) {
            boolean flag = true;
            if (target.getClass().isArray()) {
                flag = ((Object[]) target).length > 0;
            } else if (Collection.class.isAssignableFrom(target.getClass())) {
                flag = !((Collection<?>) target).isEmpty();
            } else if (target instanceof String) {
                flag = StringUtils.isNotBlank((String) target);
            }
            return flag;
        }
        return false;
    }

    /**
     * @param target  目标对象
     * @param builder 条件构建器
     * @return 当目标对象非空则采纳cond条件
     */
    public Cond exprNotEmpty(Object target, IConditionBuilder builder) {
        if (builder != null && doCheckNotEmpty(target)) {
            this.cond(builder.build());
        }
        return this;
    }

    /**
     * @param target   目标对象
     * @param appender 条件追加器
     * @return 当目标对象非空则采纳cond条件
     */
    public Cond exprNotEmpty(Object target, IConditionAppender appender) {
        if (appender != null && doCheckNotEmpty(target)) {
            appender.append(this);
        }
        return this;
    }

    public boolean isEmpty() {
        return StringUtils.isBlank(condition);
    }

    /**
     * 通过当前条件对象创建Where对象实例
     *
     * @return 返回Where对象实例
     * @since 2.1.0
     */
    public Where buildWhere() {
        return Where.create(this);
    }

    public String build() {
        if (brackets) {
            return String.format(" (%s) ", condition);
        }
        return condition.toString();
    }

    @Override
    public String toString() {
        return build();
    }
}
