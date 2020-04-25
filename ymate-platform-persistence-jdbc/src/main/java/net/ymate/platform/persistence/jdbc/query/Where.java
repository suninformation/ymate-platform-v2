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
        orderBy = OrderBy.create(cond);
        this.cond = cond;
    }

    public Where where(Where where) {
        cond.cond(where.cond());
        orderBy.orderBy(where.orderBy());
        //
        if (where.groupBy() != null) {
            if (groupBy != null) {
                groupBy.fields().add(where.groupBy().fields());
                Cond havingCond = where.groupBy().having();
                if (havingCond != null) {
                    if (groupBy.having() != null) {
                        groupBy.having().cond(havingCond);
                    } else {
                        groupBy.having(havingCond);
                    }
                }
            } else {
                groupBy = where.groupBy();
            }
        }
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
        Params params = Params.create().add(cond.params());
        if (groupBy != null && groupBy.having() != null) {
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

    public Where groupBy(String field) {
        return groupBy(Fields.create().add(field));
    }

    public Where groupBy(Fields fields) {
        if (groupBy != null) {
            groupBy.fields().add(fields);
        } else {
            groupBy = GroupBy.create(owner, dataSourceName, fields);
        }
        return this;
    }

    public Where groupBy(String prefix, String field) {
        return groupBy(Fields.create().add(prefix, field));
    }

    public Where groupBy(GroupBy groupBy) {
        this.groupBy = groupBy;
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

    public Where orderByAsc(String field) {
        orderBy.asc(field);
        return this;
    }

    public Where orderByAsc(String prefix, String field) {
        orderBy.asc(prefix, field);
        return this;
    }

    public Where orderByDesc(String field) {
        orderBy.desc(field);
        return this;
    }

    public Where orderByDesc(String prefix, String field) {
        orderBy.desc(prefix, field);
        return this;
    }

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
        if (groupBy != null && variables.contains("groupBy")) {
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
