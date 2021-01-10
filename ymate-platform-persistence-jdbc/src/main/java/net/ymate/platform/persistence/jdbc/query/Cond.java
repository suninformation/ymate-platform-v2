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

import net.ymate.platform.core.persistence.Fields;
import net.ymate.platform.core.persistence.IFunction;
import net.ymate.platform.core.persistence.Params;
import net.ymate.platform.persistence.jdbc.IDatabase;
import net.ymate.platform.persistence.jdbc.JDBC;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 条件对象
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/9 下午8:12
 */
public final class Cond extends Query<Cond> {

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
        LIKE("LIKE");

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
        return cond(func.build());
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
        return opt(funcOne.build(), opt, funcTwo.build());
    }

    public Cond opt(IFunction func, OPT opt) {
        return opt(func.build(), opt);
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

    public Cond eq(String fieldOne, String fieldTwo) {
        return opt(fieldOne, OPT.EQ, fieldTwo);
    }

    public Cond eq(String field) {
        return opt(field, OPT.EQ);
    }

    public Cond eqWrap(String fieldOne, String fieldTwo) {
        return optWrap(fieldOne, OPT.EQ, fieldTwo);
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

    // ------

    public Cond notEq(String fieldOne, String fieldTwo) {
        return opt(fieldOne, OPT.NOT_EQ, fieldTwo);
    }

    public Cond notEq(String field) {
        return opt(field, OPT.NOT_EQ);
    }

    public Cond notEqWrap(String fieldOne, String fieldTwo) {
        return optWrap(fieldOne, OPT.NOT_EQ, fieldTwo);
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

    // ------

    public Cond gtEq(String fieldOne, String fieldTwo) {
        return opt(fieldOne, OPT.GT_EQ, fieldTwo);
    }

    public Cond gtEq(String field) {
        return opt(field, OPT.GT_EQ);
    }

    public Cond gtEqWrap(String fieldOne, String fieldTwo) {
        return optWrap(fieldOne, OPT.GT_EQ, fieldTwo);
    }

    public Cond gtEqWrap(String field) {
        return optWrap(field, OPT.GT_EQ);
    }

    public Cond gtEq(IFunction funcOne, IFunction funcTwo) {
        return opt(funcOne, OPT.GT_EQ, funcTwo);
    }

    public Cond gtEq(IFunction func) {
        return opt(func, OPT.GT_EQ);
    }

    // ------

    public Cond gt(String fieldOne, String fieldTwo) {
        return opt(fieldOne, OPT.GT, fieldTwo);
    }

    public Cond gt(String field) {
        return opt(field, OPT.GT);
    }

    public Cond gtWrap(String fieldOne, String fieldTwo) {
        return optWrap(fieldOne, OPT.GT, fieldTwo);
    }

    public Cond gtWrap(String field) {
        return optWrap(field, OPT.GT);
    }

    public Cond gt(IFunction funcOne, IFunction funcTwo) {
        return opt(funcOne, OPT.GT, funcTwo);
    }

    public Cond gt(IFunction func) {
        return opt(func, OPT.GT);
    }

    // ------

    public Cond ltEq(String fieldOne, String fieldTwo) {
        return opt(fieldOne, OPT.LT_EQ, fieldTwo);
    }

    public Cond ltEq(String field) {
        return opt(field, OPT.LT_EQ);
    }

    public Cond ltEqWrap(String fieldOne, String fieldTwo) {
        return optWrap(fieldOne, OPT.LT_EQ, fieldTwo);
    }

    public Cond ltEqWrap(String field) {
        return optWrap(field, OPT.LT_EQ);
    }

    public Cond ltEq(IFunction funcOne, IFunction funcTwo) {
        return opt(funcOne, OPT.LT_EQ, funcTwo);
    }

    public Cond ltEq(IFunction func) {
        return opt(func, OPT.LT_EQ);
    }

    // ------

    public Cond lt(String fieldOne, String fieldTwo) {
        return opt(fieldOne, OPT.LT, fieldTwo);
    }

    public Cond lt(String field) {
        return opt(field, OPT.LT);
    }

    public Cond ltWrap(String fieldOne, String fieldTwo) {
        return optWrap(fieldOne, OPT.LT, fieldTwo);
    }

    public Cond ltWrap(String field) {
        return optWrap(field, OPT.LT);
    }

    public Cond lt(IFunction funcOne, IFunction funcTwo) {
        return opt(funcOne, OPT.LT, funcTwo);
    }

    public Cond lt(IFunction func) {
        return opt(func, OPT.LT);
    }

    // ------

    public Cond like(String field) {
        return opt(field, OPT.LIKE);
    }

    public Cond likeWrap(String field) {
        return optWrap(field, OPT.LIKE);
    }

    public Cond like(IFunction func) {
        return opt(func, OPT.LIKE);
    }

    // ------

    public Cond between(String field, Object valueOne, Object valueTwo) {
        params.add(valueOne).add(valueTwo);
        return cond(String.format("%s BETWEEN ? AND ?", field));
    }

    public Cond betweenWrap(String field, Object valueOne, Object valueTwo) {
        return between(wrapIdentifierField(field), valueOne, valueTwo);
    }

    public Cond between(IFunction func, Object valueOne, Object valueTwo) {
        return between(func.build(), valueOne, valueTwo);
    }

    // ------

    public Cond range(String field, Number valueOne, Number valueTwo, LogicalOpt opt) {
        if (valueOne != null && valueTwo != null) {
            if (opt != null) {
                opt(opt);
            }
            between(field, valueOne, valueTwo);
        } else if (valueOne != null) {
            if (opt != null) {
                opt(opt);
            }
            gtEq(field).param(valueOne);
        } else if (valueTwo != null) {
            if (opt != null) {
                opt(opt);
            }
            ltEq(field).param(valueTwo);
        }
        return this;
    }

    public Cond rangeWrap(String field, Number valueOne, Number valueTwo, LogicalOpt opt) {
        return range(wrapIdentifierField(field), valueOne, valueTwo, opt);
    }

    public Cond range(IFunction func, Number valueOne, Number valueTwo, LogicalOpt opt) {
        return range(func.build(), valueOne, valueTwo, opt);
    }

    // ------

    public Cond isNull(String prefix, String field) {
        return isNull(Fields.field(prefix, field));
    }

    public Cond isNull(String field) {
        return cond(String.format("%s IS NULL", field));
    }

    public Cond isNullWrap(String field) {
        return isNull(null, wrapIdentifierField(field));
    }

    public Cond isNull(IFunction func) {
        return isNull(func.build());
    }

    // ------

    public Cond isNotNull(String prefix, String field) {
        return isNotNull(Fields.field(prefix, field));
    }

    public Cond isNotNull(String field) {
        return cond(String.format("%s IS NOT NULL", field));
    }

    public Cond isNotNullWrap(String field) {
        return isNotNull(null, wrapIdentifierField(field));
    }

    public Cond isNotNull(IFunction func) {
        return isNotNull(func.build());
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
        return cond(String.format("EXISTS (%s)", subSql.toString()));
    }

    public Cond exists(Select subSql) {
        params.add(subSql.params());
        return cond(String.format("EXISTS (%s)", subSql.toString()));
    }

    // ------

    public Cond in(String prefix, String field, SQL subSql) {
        return in(Fields.field(prefix, field), subSql);
    }

    public Cond in(String field, SQL subSql) {
        params.add(subSql.params());
        return cond(String.format("%s IN (%s)", field, subSql.toString()));
    }

    public Cond inWrap(String field, SQL subSql) {
        return in(wrapIdentifierField(field), subSql);
    }

    public Cond in(String prefix, String field, Select subSql) {
        return in(Fields.field(prefix, field), subSql);
    }

    public Cond in(String field, Select subSql) {
        params.add(subSql.params());
        return cond(String.format("%s IN (%s)", field, subSql.toString()));
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

    public Cond inWrap(String field, Params params) {
        return in(wrapIdentifierField(field), params);
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
     * @param target  目标对象
     * @param builder 条件构建器
     * @return 当目标对象非空则采纳cond条件
     */
    public Cond exprNotEmpty(Object target, IConditionBuilder builder) {
        if (target != null && builder != null) {
            boolean flag = true;
            if (target.getClass().isArray()) {
                flag = ((Object[]) target).length > 0;
            } else if (target instanceof String) {
                flag = StringUtils.isNotBlank((String) target);
            }
            //
            if (flag) {
                this.cond(builder.build());
            }
        }
        return this;
    }

    /**
     * @param target   目标对象
     * @param appender 条件追加器
     * @return 当目标对象非空则采纳cond条件
     */
    public Cond exprNotEmpty(Object target, IConditionAppender appender) {
        if (target != null && appender != null) {
            boolean flag = true;
            if (target.getClass().isArray()) {
                flag = ((Object[]) target).length > 0;
            } else if (target instanceof String) {
                flag = StringUtils.isNotBlank((String) target);
            }
            //
            if (flag) {
                appender.append(this);
            }
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

    @Override
    public String toString() {
        if (brackets) {
            return String.format(" (%s) ", condition.toString());
        }
        return condition.toString();
    }
}
