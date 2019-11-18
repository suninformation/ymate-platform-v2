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
import net.ymate.platform.persistence.jdbc.IDatabase;
import net.ymate.platform.persistence.jdbc.JDBC;
import org.apache.commons.lang3.StringUtils;

/**
 * 分组对象
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/12 下午4:10
 */
public final class GroupBy extends Query<GroupBy> {

    private final Fields groupByNames;

    private Cond having;

    public static GroupBy create() {
        return new GroupBy(JDBC.get());
    }

    public static GroupBy create(IDatabase owner) {
        return new GroupBy(owner);
    }

    public static GroupBy create(Cond having) {
        return new GroupBy(JDBC.get()).having(having);
    }

    public static GroupBy create(IDatabase owner, Cond having) {
        return new GroupBy(owner).having(having);
    }

    public static GroupBy create(String prefix, String field, String alias) {
        return new GroupBy(JDBC.get()).field(Fields.create().add(prefix, field, alias));
    }

    public static GroupBy create(String prefix, String field) {
        return new GroupBy(JDBC.get()).field(Fields.create().add(prefix, field));
    }

    public static GroupBy create(String field) {
        return new GroupBy(JDBC.get()).field(Fields.create(field));
    }

    public static GroupBy create(Fields fields) {
        return new GroupBy(JDBC.get()).field(fields);
    }

    public static GroupBy create(IDatabase owner, String prefix, String field, String alias) {
        return new GroupBy(owner).field(Fields.create().add(prefix, field, alias));
    }

    public static GroupBy create(IDatabase owner, String prefix, String field) {
        return new GroupBy(owner).field(Fields.create().add(prefix, field));
    }

    public static GroupBy create(IDatabase owner, String field) {
        return new GroupBy(owner).field(Fields.create(field));
    }

    public static GroupBy create(IDatabase owner, Fields fields) {
        return new GroupBy(owner).field(fields);
    }

    private GroupBy(IDatabase owner) {
        super(owner);
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
        StringBuilder groupByBuilder = new StringBuilder();
        if (!groupByNames.isEmpty()) {
            groupByBuilder.append(" GROUP BY ").append(StringUtils.join(wrapIdentifierFields(groupByNames.toArray()).fields(), ", "));
        }
        if (having != null) {
            groupByBuilder.append(" HAVING ").append(having);
        }
        return groupByBuilder.toString();
    }
}
