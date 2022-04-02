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
import net.ymate.platform.core.persistence.IFunction;
import net.ymate.platform.core.persistence.Params;
import net.ymate.platform.persistence.jdbc.IDatabase;
import net.ymate.platform.persistence.jdbc.JDBC;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Where条件及参数对象
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/7 下午1:19
 */
public final class Where extends QueryHandleAdapter<Where> {

    private final IDatabase owner;

    private final String dataSourceName;

    /**
     * SQL条件对象
     */
    private final Cond cond;

    private GroupBy groupBy;

    private final OrderBy orderBy;

    private final Slot slot = new Slot();

    public static Where create() {
        return create(JDBC.get());
    }

    public static Where create(String whereCond) {
        return new Where(JDBC.get(), whereCond);
    }

    public static Where create(IDatabase owner) {
        return new Where(owner, owner.getConfig().getDefaultDataSourceName());
    }

    public static Where create(IDatabase owner, String whereCond) {
        return new Where(owner, whereCond);
    }

    public static Where create(Cond cond) {
        return new Where(cond);
    }

    public static Where create(Query<?> query) {
        return new Where(query.owner(), query.dataSourceName());
    }

    public Where(IDatabase owner, String dataSourceName) {
        this.owner = owner;
        this.dataSourceName = dataSourceName;
        groupBy = GroupBy.create(owner, dataSourceName);
        orderBy = OrderBy.create(owner, dataSourceName);
        cond = Cond.create(owner, dataSourceName);
    }

    public Where(IDatabase owner, String dataSourceName, String whereCond) {
        this(owner, dataSourceName);
        cond.cond(whereCond);
    }

    public Where(Cond cond) {
        owner = cond.owner();
        dataSourceName = cond.dataSourceName();
        groupBy = GroupBy.create(owner, dataSourceName);
        orderBy = OrderBy.create(cond);
        this.cond = cond;
    }

    public Where where(Where where) {
        cond.cond(where.cond());
        groupBy.groupBy(where.groupBy());
        orderBy.orderBy(where.orderBy());
        return this;
    }

    public Cond cond() {
        return cond;
    }

    public GroupBy groupBy() {
        return groupBy;
    }

    public OrderBy orderBy() {
        return orderBy;
    }

    public Slot getSlot() {
        return slot;
    }

    /**
     * @return 此方法仅返回只读参数集合, 若要维护参数请调用where().param(...)相关方法
     */
    public Params params() {
        Params params = Params.create()
                .add(cond.params())
                .add(groupBy.params());
        if (groupBy.having() != null) {
            params.add(groupBy.having().params());
        }
        return params;
    }

    public Where param(Object param) {
        cond.param(param);
        return this;
    }

    public Where param(Params params) {
        cond.param(params);
        return this;
    }

    // ------

    public Where groupByRollup() {
        this.groupBy.rollup();
        return this;
    }

    public Where groupBy(GroupBy groupBy) {
        this.groupBy.groupBy(groupBy);
        return this;
    }

    public Where groupBy(Fields fields) {
        groupBy.field(null, fields, false, true);
        return this;
    }

    public Where groupBy(Fields fields, boolean wrapIdentifier) {
        groupBy.field(null, fields, false, wrapIdentifier);
        return this;
    }

    public Where groupBy(Fields fields, boolean desc, boolean wrapIdentifier) {
        groupBy.field(null, fields, desc, wrapIdentifier);
        return this;
    }

    public Where groupBy(String prefix, Fields fields) {
        groupBy.field(prefix, fields, false, true);
        return this;
    }

    public Where groupBy(String prefix, Fields fields, boolean wrapIdentifier) {
        groupBy.field(prefix, fields, false, wrapIdentifier);
        return this;
    }

    public Where groupBy(String prefix, Fields fields, boolean desc, boolean wrapIdentifier) {
        groupBy.field(prefix, fields, desc, wrapIdentifier);
        return this;
    }

    public Where groupBy(String field) {
        groupBy.field(null, field, false, true);
        return this;
    }

    public Where groupBy(String field, boolean wrapIdentifier) {
        groupBy.field(null, field, false, wrapIdentifier);
        return this;
    }

    public Where groupBy(String field, boolean desc, boolean wrapIdentifier) {
        groupBy.field(null, field, desc, wrapIdentifier);
        return this;
    }

    public Where groupBy(String prefix, String field) {
        groupBy.field(prefix, field, false, true);
        return this;
    }

    public Where groupBy(String prefix, String field, boolean wrapIdentifier) {
        groupBy.field(prefix, field, false, wrapIdentifier);
        return this;
    }

    public Where groupBy(String prefix, String field, boolean desc, boolean wrapIdentifier) {
        groupBy.field(prefix, field, desc, wrapIdentifier);
        return this;
    }

    public Where groupBy(IFunction func) {
        groupBy.field(func, false);
        return this;
    }

    public Where groupBy(IFunction func, boolean desc) {
        groupBy.field(func, desc);
        return this;
    }

    // --- GroupBy DESC

    public Where groupByDesc(Fields fields) {
        groupBy.field(null, fields, true, true);
        return this;
    }

    public Where groupByDesc(Fields fields, boolean wrapIdentifier) {
        groupBy.field(null, fields, true, wrapIdentifier);
        return this;
    }

    public Where groupByDesc(String prefix, Fields fields) {
        groupBy.field(prefix, fields, true, true);
        return this;
    }

    public Where groupByDesc(String prefix, Fields fields, boolean wrapIdentifier) {
        groupBy.field(prefix, fields, true, wrapIdentifier);
        return this;
    }

    public Where groupByDesc(String field) {
        groupBy.field(null, field, true, true);
        return this;
    }

    public Where groupByDesc(String field, boolean wrapIdentifier) {
        groupBy.field(null, field, true, wrapIdentifier);
        return this;
    }

    public Where groupByDesc(String prefix, String field) {
        groupBy.field(prefix, field, true, true);
        return this;
    }

    public Where groupByDesc(String prefix, String field, boolean wrapIdentifier) {
        groupBy.field(prefix, field, true, wrapIdentifier);
        return this;
    }

    public Where groupByDesc(IFunction func) {
        groupBy.field(func, true);
        return this;
    }

    public Where having(Cond cond) {
        if (groupBy != null) {
            groupBy.having(cond);
        } else {
            groupBy = GroupBy.create(owner, dataSourceName, cond);
        }
        return this;
    }

    // ------

    public Where orderBy(OrderBy orderBy) {
        this.orderBy.orderBy(orderBy);
        return this;
    }

    public Where orderByAsc(Fields fields) {
        orderBy.asc(fields, true);
        return this;
    }

    public Where orderByAsc(Fields fields, boolean wrapIdentifier) {
        orderBy.asc(fields, wrapIdentifier);
        return this;
    }

    public Where orderByAsc(String prefix, Fields fields) {
        orderBy.asc(prefix, fields, true);
        return this;
    }

    public Where orderByAsc(String prefix, Fields fields, boolean wrapIdentifier) {
        orderBy.asc(prefix, fields, wrapIdentifier);
        return this;
    }

    public Where orderByAsc(String field) {
        orderBy.asc(field, true);
        return this;
    }

    public Where orderByAsc(String field, boolean wrapIdentifier) {
        orderBy.asc(field, wrapIdentifier);
        return this;
    }

    public Where orderByAsc(String prefix, String field) {
        orderBy.asc(prefix, field, true);
        return this;
    }

    public Where orderByAsc(String prefix, String field, boolean wrapIdentifier) {
        orderBy.asc(prefix, field, wrapIdentifier);
        return this;
    }

    public Where orderByAsc(IFunction func) {
        orderBy.asc(func);
        return this;
    }

    // ------

    public Where orderByDesc(Fields fields) {
        orderBy.desc(fields, true);
        return this;
    }

    public Where orderByDesc(Fields fields, boolean wrapIdentifier) {
        orderBy.desc(fields, wrapIdentifier);
        return this;
    }

    public Where orderByDesc(String prefix, Fields fields) {
        orderBy.desc(prefix, fields, true);
        return this;
    }

    public Where orderByDesc(String prefix, Fields fields, boolean wrapIdentifier) {
        orderBy.desc(prefix, fields, wrapIdentifier);
        return this;
    }

    public Where orderByDesc(String field) {
        orderBy.desc(field, true);
        return this;
    }

    public Where orderByDesc(String field, boolean wrapIdentifier) {
        orderBy.desc(field, wrapIdentifier);
        return this;
    }

    public Where orderByDesc(String prefix, String field) {
        orderBy.desc(prefix, field, true);
        return this;
    }

    public Where orderByDesc(String prefix, String field, boolean wrapIdentifier) {
        orderBy.desc(prefix, field, wrapIdentifier);
        return this;
    }

    public Where orderByDesc(IFunction func) {
        orderBy.desc(func);
        return this;
    }

    // ------

    public String toSQL() {
        ExpressionUtils expression = ExpressionUtils.bind(getExpressionStr("${whereCond} ${slot} ${groupBy}"));
        if (queryHandler() != null) {
            queryHandler().beforeBuild(expression, this);
        }
        List<String> variables = expression.getVariables();
        if (cond != null && variables.contains("whereCond")) {
            String condStr = cond.toString();
            if (StringUtils.isNotBlank(condStr)) {
                expression.set("whereCond", String.format("WHERE %s", condStr));
            }
        }
        if (slot.hasSlotContent() && variables.contains("slot")) {
            expression.set("slot", slot.buildSlot());
        }
        if (!groupBy.isEmpty() && variables.contains("groupBy")) {
            expression.set("groupBy", groupBy.toString());
        }
        if (queryHandler() != null) {
            queryHandler().afterBuild(expression, this);
        }
        return StringUtils.trimToEmpty(expression.clean().getResult());
    }

    @Override
    public String toString() {
        return String.format("%s %s", toSQL(), orderBy);
    }
}
