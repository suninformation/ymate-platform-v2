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

import net.ymate.platform.persistence.jdbc.IDatabase;
import net.ymate.platform.persistence.jdbc.JDBC;
import org.apache.commons.lang3.StringUtils;

/**
 * 排序对象
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/7 上午10:55
 */
public final class OrderBy extends Query<OrderBy> {

    private final StringBuilder orderByBuilder;

    public static OrderBy create() {
        return create(JDBC.get());
    }

    public static OrderBy create(IDatabase owner) {
        return new OrderBy(owner, owner.getConfig().getDefaultDataSourceName());
    }

    public static OrderBy create(IDatabase owner, String dataSourceName) {
        return new OrderBy(owner, dataSourceName);
    }

    private OrderBy(IDatabase owner, String dataSourceName) {
        super(owner, dataSourceName);
        orderByBuilder = new StringBuilder();
    }

    public OrderBy orderBy(OrderBy orderBy) {
        String newOrderBy = StringUtils.substringAfter(orderBy.toSQL(), "ORDER BY ");
        if (StringUtils.isNotBlank(newOrderBy)) {
            if (orderByBuilder.length() > 0) {
                orderByBuilder.append(LINE_END_FLAG);
            }
            orderByBuilder.append(newOrderBy);
        }
        return this;
    }

    public OrderBy asc(String field) {
        return asc(null, field);
    }

    public OrderBy asc(String prefix, String field) {
        if (orderByBuilder.length() > 0) {
            orderByBuilder.append(LINE_END_FLAG);
        }
        if (StringUtils.isNotBlank(prefix)) {
            orderByBuilder.append(prefix).append(".");
        }
        orderByBuilder.append(wrapIdentifierField(field));
        return this;
    }

    public OrderBy desc(String field) {
        return desc(null, field);
    }

    public OrderBy desc(String prefix, String field) {
        if (orderByBuilder.length() > 0) {
            orderByBuilder.append(LINE_END_FLAG);
        }
        if (StringUtils.isNotBlank(prefix)) {
            orderByBuilder.append(prefix).append(".");
        }
        orderByBuilder.append(wrapIdentifierField(field)).append(" DESC");
        return this;
    }

    public String toSQL() {
        StringBuilder stringBuilder = new StringBuilder();
        if (orderByBuilder.length() > 0) {
            stringBuilder.append("ORDER BY ").append(orderByBuilder);
        }
        return stringBuilder.toString();
    }

    @Override
    public String toString() {
        return toSQL();
    }
}
