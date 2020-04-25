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

    private final Fields groupByNames;

    private Cond having;

    public static GroupBy create() {
        return create(JDBC.get());
    }

    public static GroupBy create(Cond having) {
        return create().having(having);
    }

    public static GroupBy create(String prefix, String field, String alias) {
        return create().field(Fields.create().add(prefix, field, alias));
    }

    public static GroupBy create(String prefix, String field) {
        return create().field(Fields.create().add(prefix, field));
    }

    public static GroupBy create(String field) {
        return create().field(Fields.create(field));
    }

    public static GroupBy create(Fields fields) {
        return create().field(fields);
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

    public static GroupBy create(IDatabase owner, String dataSourceName, String prefix, String field, String alias) {
        return new GroupBy(owner, dataSourceName).field(Fields.create().add(prefix, field, alias));
    }

    public static GroupBy create(IDatabase owner, String dataSourceName, String prefix, String field) {
        return new GroupBy(owner, dataSourceName).field(Fields.create().add(prefix, field));
    }

    public static GroupBy create(IDatabase owner, String dataSourceName, String field) {
        return new GroupBy(owner, dataSourceName).field(Fields.create(field));
    }

    public static GroupBy create(IDatabase owner, String dataSourceName, Fields fields) {
        return new GroupBy(owner, dataSourceName).field(fields);
    }

    public static GroupBy create(Query<?> query) {
        return new GroupBy(query.owner(), query.dataSourceName());
    }

    public GroupBy(IDatabase owner, String dataSourceName) {
        super(owner, dataSourceName);
        groupByNames = Fields.create();
    }

    public GroupBy field(Fields fields) {
        groupByNames.add(checkFieldExcluded(fields));
        return this;
    }

    public Fields fields() {
        return groupByNames;
    }

    public Cond having() {
        return having;
    }

    public GroupBy having(Cond cond) {
        having = cond;
        return this;
    }

    @Override
    public String toString() {
        ExpressionUtils expression = ExpressionUtils.bind(getExpressionStr("${groupBy} ${having}"));
        if (queryHandler() != null) {
            queryHandler().beforeBuild(expression, this);
        }
        List<String> variables = expression.getVariables();
        if (!groupByNames.isEmpty() && variables.contains("groupBy")) {
            expression.set("groupBy", String.format("GROUP BY %s", StringUtils.join(wrapIdentifierFields(groupByNames.toArray()).fields(), LINE_END_FLAG)));
        }
        if (having != null && variables.contains("having")) {
            expression.set("having", String.format("HAVING %s", having.toString()));
        }
        if (queryHandler() != null) {
            queryHandler().afterBuild(expression, this);
        }
        return StringUtils.trimToEmpty(expression.clean().getResult());
    }
}
