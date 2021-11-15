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
 * 分组对象
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/12 下午4:10
 */
public final class GroupBy extends Query<GroupBy> {

    private final StringBuilder groupByBuilder;

    private final Params params;

    private Cond having;

    private boolean rollup;

    public static GroupBy create() {
        return create(JDBC.get());
    }

    public static GroupBy create(Cond having) {
        return create(having.owner(), having.dataSourceName(), having);
    }

    public static GroupBy create(String prefix, String field) {
        return create().field(prefix, field);
    }

    public static GroupBy create(String prefix, String field, boolean wrapIdentifier) {
        return create().field(prefix, field, wrapIdentifier);
    }

    public static GroupBy create(String field) {
        return create().field(field);
    }

    public static GroupBy create(String field, boolean wrapIdentifier) {
        return create().field(field, wrapIdentifier);
    }

    public static GroupBy create(Fields fields) {
        return create().field(fields);
    }

    public static GroupBy create(Fields fields, boolean wrapIdentifier) {
        return create().field(fields, wrapIdentifier);
    }

    public static GroupBy create(String prefix, Fields fields, boolean wrapIdentifier) {
        return create().field(prefix, fields, wrapIdentifier);
    }

    //

    public static GroupBy create(IDatabase owner) {
        return new GroupBy(owner, owner.getConfig().getDefaultDataSourceName());
    }

    public static GroupBy create(IDatabase owner, String dataSourceName) {
        return new GroupBy(owner, dataSourceName);
    }

    public static GroupBy create(IDatabase owner, String dataSourceName, Cond having) {
        return new GroupBy(owner, dataSourceName).having(having);
    }

    public static GroupBy create(IDatabase owner, String dataSourceName, String prefix, String field) {
        return new GroupBy(owner, dataSourceName).field(prefix, field);
    }

    public static GroupBy create(IDatabase owner, String dataSourceName, String prefix, String field, boolean wrapIdentifier) {
        return new GroupBy(owner, dataSourceName).field(prefix, field, wrapIdentifier);
    }

    public static GroupBy create(IDatabase owner, String dataSourceName, String prefix, String field, boolean desc, boolean wrapIdentifier) {
        return new GroupBy(owner, dataSourceName).field(prefix, field, desc, wrapIdentifier);
    }

    public static GroupBy create(IDatabase owner, String dataSourceName, String field) {
        return new GroupBy(owner, dataSourceName).field(field);
    }

    public static GroupBy create(IDatabase owner, String dataSourceName, String field, boolean wrapIdentifier) {
        return new GroupBy(owner, dataSourceName).field(field, wrapIdentifier);
    }


    public static GroupBy create(IDatabase owner, String dataSourceName, String field, boolean desc, boolean wrapIdentifier) {
        return new GroupBy(owner, dataSourceName).field(field, desc, wrapIdentifier);
    }

    public static GroupBy create(IDatabase owner, String dataSourceName, String prefix, Fields fields) {
        return new GroupBy(owner, dataSourceName).field(prefix, fields);
    }

    public static GroupBy create(IDatabase owner, String dataSourceName, String prefix, Fields fields, boolean wrapIdentifier) {
        return new GroupBy(owner, dataSourceName).field(prefix, fields, wrapIdentifier);
    }

    public static GroupBy create(IDatabase owner, String dataSourceName, String prefix, Fields fields, boolean desc, boolean wrapIdentifier) {
        return new GroupBy(owner, dataSourceName).field(prefix, fields, desc, wrapIdentifier);
    }

    public static GroupBy create(IDatabase owner, String dataSourceName, Fields fields) {
        return new GroupBy(owner, dataSourceName).field(fields);
    }

    public static GroupBy create(IDatabase owner, String dataSourceName, Fields fields, boolean wrapIdentifier) {
        return new GroupBy(owner, dataSourceName).field(fields, wrapIdentifier);
    }

    public static GroupBy create(IDatabase owner, String dataSourceName, Fields fields, boolean desc, boolean wrapIdentifier) {
        return new GroupBy(owner, dataSourceName).field(fields, desc, wrapIdentifier);
    }

    public static GroupBy create(Query<?> query) {
        return new GroupBy(query.owner(), query.dataSourceName());
    }

    public GroupBy(IDatabase owner, String dataSourceName) {
        super(owner, dataSourceName);
        groupByBuilder = new StringBuilder();
        params = Params.create();
    }

    public GroupBy groupBy(GroupBy groupBy) {
        if (groupBy != null) {
            String newGroupBy = StringUtils.substringAfter(groupBy.toSQL(), "GROUP BY ");
            if (StringUtils.isNotBlank(newGroupBy)) {
                if (groupByBuilder.length() > 0) {
                    groupByBuilder.append(LINE_END_FLAG);
                }
                groupByBuilder.append(newGroupBy);
                params.add(groupBy.params());
            }
        }
        return this;
    }

    public GroupBy field(String prefix, Fields fields) {
        return field(prefix, fields, false, true);
    }

    public GroupBy field(String prefix, Fields fields, boolean wrapIdentifier) {
        return field(prefix, fields, false, wrapIdentifier);
    }

    public GroupBy field(String prefix, Fields fields, boolean desc, boolean wrapIdentifier) {
        checkFieldExcluded(fields).fields().forEach((field) -> field(prefix, field, desc, wrapIdentifier));
        return this;
    }

    public GroupBy field(Fields fields) {
        return field(null, fields, false, true);
    }

    public GroupBy field(Fields fields, boolean wrapIdentifier) {
        return field(null, fields, false, wrapIdentifier);
    }

    public GroupBy field(Fields fields, boolean desc, boolean wrapIdentifier) {
        return field(null, fields, desc, wrapIdentifier);
    }

    // ------

    public GroupBy field(String field) {
        return field(null, field, false, true);
    }

    public GroupBy field(String field, boolean desc) {
        return field(null, field, desc, true);
    }

    public GroupBy field(String field, boolean desc, boolean wrapIdentifier) {
        return field(null, field, desc, wrapIdentifier);
    }

    public GroupBy field(IFunction func) {
        return field(null, func.build(), false, false).param(func.params());
    }

    public GroupBy field(IFunction func, boolean desc) {
        return field(null, func.build(), desc, false).param(func.params());
    }

    public GroupBy field(String prefix, String field) {
        return field(prefix, field, false, true);
    }

    public GroupBy field(String prefix, String field, boolean desc) {
        return field(prefix, field, desc, true);
    }

    public GroupBy field(String prefix, String field, boolean desc, boolean wrapIdentifier) {
        if (groupByBuilder.length() > 0) {
            groupByBuilder.append(LINE_END_FLAG);
        }
        if (StringUtils.isNotBlank(prefix)) {
            groupByBuilder.append(prefix).append(".");
        }
        groupByBuilder.append(wrapIdentifier ? wrapIdentifierField(field) : field);
        if (desc) {
            groupByBuilder.append(" DESC");
        }
        return this;
    }

    // --- DESC

    public GroupBy desc(String prefix, Fields fields) {
        return field(prefix, fields, true, true);
    }

    public GroupBy desc(String prefix, Fields fields, boolean wrapIdentifier) {
        return field(prefix, fields, true, wrapIdentifier);
    }

    public GroupBy desc(Fields fields) {
        return field(null, fields, true, true);
    }

    public GroupBy desc(Fields fields, boolean wrapIdentifier) {
        return field(null, fields, true, wrapIdentifier);
    }

    // ------

    public GroupBy desc(String field) {
        return field(null, field, true, true);
    }

    public GroupBy desc(String field, boolean wrapIdentifier) {
        return field(null, field, true, wrapIdentifier);
    }

    public GroupBy desc(IFunction func) {
        return field(func, true);
    }

    public GroupBy desc(String prefix, String field) {
        return field(prefix, field, true, true);
    }

    public GroupBy desc(String prefix, String field, boolean wrapIdentifier) {
        return field(prefix, field, true, wrapIdentifier);
    }

    public Cond having() {
        return having;
    }

    public GroupBy having(Cond cond) {
        having = cond;
        return this;
    }

    public GroupBy rollup() {
        rollup = true;
        return this;
    }

    public Params params() {
        return params;
    }

    public GroupBy param(Object param) {
        params.add(param);
        return this;
    }

    public GroupBy param(Params params) {
        this.params.add(params);
        return this;
    }

    public boolean isEmpty() {
        return groupByBuilder.length() == 0 && (having == null || having.isEmpty());
    }

    public String toSQL() {
        ExpressionUtils expression = ExpressionUtils.bind(getExpressionStr("${groupBy} ${having}"));
        if (queryHandler() != null) {
            queryHandler().beforeBuild(expression, this);
        }
        List<String> variables = expression.getVariables();
        if (groupByBuilder.length() > 0 && variables.contains("groupBy")) {
            StringBuilder stringBuilder = new StringBuilder("GROUP BY ").append(groupByBuilder);
            if (rollup) {
                stringBuilder.append(" WITH ROLLUP");
            }
            expression.set("groupBy", stringBuilder.toString());
        }
        if (having != null && !having().isEmpty() && variables.contains("having")) {
            expression.set("having", String.format("HAVING %s", having.toString()));
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
