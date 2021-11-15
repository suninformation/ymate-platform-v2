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
 * 排序对象
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/7 上午10:55
 */
public final class OrderBy extends Query<OrderBy> {

    private final StringBuilder orderByBuilder;

    // TODO 若OrderBy中存在参数值，当分页查询count记录数量时可能产生参数下标越界异常!!! 需要单独获取OrderBy中参数完成赋值。
    private final Params params;

    public static OrderBy create() {
        return create(JDBC.get());
    }

    public static OrderBy create(IDatabase owner) {
        return new OrderBy(owner, owner.getConfig().getDefaultDataSourceName());
    }

    public static OrderBy create(IDatabase owner, String dataSourceName) {
        return new OrderBy(owner, dataSourceName);
    }

    public static OrderBy create(Query<?> query) {
        return new OrderBy(query.owner(), query.dataSourceName());
    }

    public OrderBy(IDatabase owner, String dataSourceName) {
        super(owner, dataSourceName);
        orderByBuilder = new StringBuilder();
        params = Params.create();
    }

    public OrderBy orderBy(OrderBy orderBy) {
        if (orderBy != null) {
            String newOrderBy = StringUtils.substringAfter(orderBy.toSQL(), "ORDER BY ");
            if (StringUtils.isNotBlank(newOrderBy)) {
                if (orderByBuilder.length() > 0) {
                    orderByBuilder.append(LINE_END_FLAG);
                }
                orderByBuilder.append(newOrderBy);
                params.add(orderBy.params);
            }
        }
        return this;
    }

    public OrderBy asc(String prefix, Fields fields, boolean wrapIdentifier) {
        if (fields != null && !fields.isEmpty()) {
            fields.fields().forEach(field -> asc(prefix, field, wrapIdentifier));
        }
        return this;
    }

    public OrderBy asc(String prefix, Fields fields) {
        return asc(prefix, fields, true);
    }

    public OrderBy asc(Fields fields) {
        return asc(null, fields);
    }

    public OrderBy asc(Fields fields, boolean wrapIdentifier) {
        return asc(null, fields, wrapIdentifier);
    }

    public OrderBy asc(String field) {
        return asc(null, field, true);
    }

    public OrderBy asc(String field, boolean wrapIdentifier) {
        return asc(null, field, wrapIdentifier);
    }

    public OrderBy asc(String prefix, String field) {
        return asc(prefix, field, true);
    }

    public OrderBy asc(String prefix, String field, boolean wrapIdentifier) {
        if (orderByBuilder.length() > 0) {
            orderByBuilder.append(LINE_END_FLAG);
        }
        if (StringUtils.isNotBlank(prefix)) {
            orderByBuilder.append(prefix).append(".");
        }
        orderByBuilder.append(wrapIdentifier ? wrapIdentifierField(field) : field);
        return this;
    }

    public OrderBy asc(IFunction func) {
        return asc(null, func.build(), false).param(func.params());
    }

    // ------

    public OrderBy desc(String prefix, Fields fields, boolean wrapIdentifier) {
        if (fields != null && !fields.isEmpty()) {
            fields.fields().forEach(field -> desc(prefix, field, wrapIdentifier));
        }
        return this;
    }

    public OrderBy desc(String prefix, Fields fields) {
        return desc(prefix, fields, true);
    }

    public OrderBy desc(Fields fields) {
        return desc(null, fields, true);
    }

    public OrderBy desc(Fields fields, boolean wrapIdentifier) {
        return desc(null, fields, wrapIdentifier);
    }

    public OrderBy desc(String field) {
        return desc(null, field, true);
    }

    public OrderBy desc(String field, boolean wrapIdentifier) {
        return desc(null, field, wrapIdentifier);
    }

    public OrderBy desc(String prefix, String field) {
        return desc(prefix, field, true);
    }

    public OrderBy desc(String prefix, String field, boolean wrapIdentifier) {
        if (orderByBuilder.length() > 0) {
            orderByBuilder.append(LINE_END_FLAG);
        }
        if (StringUtils.isNotBlank(prefix)) {
            orderByBuilder.append(prefix).append(".");
        }
        orderByBuilder.append(wrapIdentifier ? wrapIdentifierField(field) : field).append(" DESC");
        return this;
    }

    public OrderBy desc(IFunction func) {
        return desc(null, func.build(), false).param(func.params());
    }

    // ------

    public Params params() {
        return params;
    }

    public OrderBy param(Object param) {
        params.add(param);
        return this;
    }

    public OrderBy param(Params params) {
        this.params.add(params);
        return this;
    }

    public boolean isEmpty() {
        return orderByBuilder.length() == 0;
    }

    // ------

    public String toSQL() {
        ExpressionUtils expression = ExpressionUtils.bind(getExpressionStr("${orderBy}"));
        if (queryHandler() != null) {
            queryHandler().beforeBuild(expression, this);
        }
        List<String> variables = expression.getVariables();
        if (orderByBuilder.length() > 0 && variables.contains("orderBy")) {
            expression.set("orderBy", String.format("ORDER BY %s", orderByBuilder));
        }
        if (queryHandler() != null) {
            queryHandler().afterBuild(expression, this);
        }
        return StringUtils.trimToEmpty(expression.clean().getResult());
    }

    @Override
    public String toString() {
        return toSQL();
    }
}
