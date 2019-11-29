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
import net.ymate.platform.persistence.jdbc.IDatabaseConnectionHolder;
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

    public static GroupBy create() throws Exception {
        return new GroupBy(JDBC.get().getDefaultConnectionHolder());
    }

    public static GroupBy create(Cond having) throws Exception {
        return new GroupBy(JDBC.get().getDefaultConnectionHolder()).having(having);
    }

    public static GroupBy create(String prefix, String field, String alias) throws Exception {
        return new GroupBy(JDBC.get().getDefaultConnectionHolder()).field(Fields.create().add(prefix, field, alias));
    }

    public static GroupBy create(String prefix, String field) throws Exception {
        return new GroupBy(JDBC.get().getDefaultConnectionHolder()).field(Fields.create().add(prefix, field));
    }

    public static GroupBy create(String field) throws Exception {
        return new GroupBy(JDBC.get().getDefaultConnectionHolder()).field(Fields.create(field));
    }

    public static GroupBy create(Fields fields) throws Exception {
        return new GroupBy(JDBC.get().getDefaultConnectionHolder()).field(fields);
    }

    //

    public static GroupBy create(IDatabase owner) throws Exception {
        return new GroupBy(owner.getDefaultConnectionHolder());
    }

    public static GroupBy create(IDatabase owner, Cond having) throws Exception {
        return new GroupBy(owner.getDefaultConnectionHolder()).having(having);
    }

    public static GroupBy create(IDatabase owner, String prefix, String field, String alias) throws Exception {
        return new GroupBy(owner.getDefaultConnectionHolder()).field(Fields.create().add(prefix, field, alias));
    }

    public static GroupBy create(IDatabase owner, String prefix, String field) throws Exception {
        return new GroupBy(owner.getDefaultConnectionHolder()).field(Fields.create().add(prefix, field));
    }

    public static GroupBy create(IDatabase owner, String field) throws Exception {
        return new GroupBy(owner.getDefaultConnectionHolder()).field(Fields.create(field));
    }

    public static GroupBy create(IDatabase owner, Fields fields) throws Exception {
        return new GroupBy(owner.getDefaultConnectionHolder()).field(fields);
    }

    //

    public static GroupBy create(IDatabaseConnectionHolder connectionHolder) {
        return new GroupBy(connectionHolder);
    }

    public static GroupBy create(IDatabaseConnectionHolder connectionHolder, Cond having) {
        return new GroupBy(connectionHolder).having(having);
    }

    public static GroupBy create(IDatabaseConnectionHolder connectionHolder, String prefix, String field, String alias) {
        return new GroupBy(connectionHolder).field(Fields.create().add(prefix, field, alias));
    }

    public static GroupBy create(IDatabaseConnectionHolder connectionHolder, String prefix, String field) {
        return new GroupBy(connectionHolder).field(Fields.create().add(prefix, field));
    }

    public static GroupBy create(IDatabaseConnectionHolder connectionHolder, String field) {
        return new GroupBy(connectionHolder).field(Fields.create(field));
    }

    public static GroupBy create(IDatabaseConnectionHolder connectionHolder, Fields fields) {
        return new GroupBy(connectionHolder).field(fields);
    }

    private GroupBy(IDatabaseConnectionHolder connectionHolder) {
        super(connectionHolder);
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
            expression.set("having", having.toString());
        }
        if (queryHandler() != null) {
            queryHandler().afterBuild(expression, this);
        }
        return StringUtils.trimToEmpty(expression.clean().getResult());
    }
}
