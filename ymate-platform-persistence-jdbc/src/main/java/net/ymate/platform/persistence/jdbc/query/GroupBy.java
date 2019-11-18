/*
 * Copyright 2007-2017 the original author or authors.
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

import net.ymate.platform.persistence.Fields;
import org.apache.commons.lang.StringUtils;

/**
 * 分组对象
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/12 下午4:10
 * @version 1.0
 */
public final class GroupBy extends Query<GroupBy> {

    private final Fields __groupByNames;

    private Cond __having;

    public static GroupBy create() {
        return new GroupBy();
    }

    public static GroupBy create(Cond having) {
        return new GroupBy().having(having);
    }

    public static GroupBy create(String prefix, String field, String alias) {
        return new GroupBy(Fields.create().add(prefix, field, alias));
    }

    public static GroupBy create(String prefix, String field) {
        return new GroupBy(Fields.create().add(prefix, field));
    }

    public static GroupBy create(String field) {
        return new GroupBy(Fields.create().add(field));
    }

    public static GroupBy create(Fields fields) {
        return new GroupBy(fields);
    }

    private GroupBy() {
        __groupByNames = Fields.create();
    }

    private GroupBy(Fields fields) {
        __groupByNames = Fields.create().add(__checkFieldExcluded(fields));
    }

    public GroupBy field(Fields fields) {
        __groupByNames.add(__checkFieldExcluded(fields));
        return this;
    }

    public Fields fields() {
        return __groupByNames;
    }

    public Cond having() {
        return __having;
    }

    public GroupBy having(Cond cond) {
        __having = cond;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder groupByBuilder = new StringBuilder();
        if (!__groupByNames.fields().isEmpty()) {
            groupByBuilder.append(" GROUP BY ").append(StringUtils.join(__wrapIdentifierFields(__groupByNames.toArray()).fields(), ", "));
        }
        if (__having != null) {
            groupByBuilder.append(" HAVING ").append(__having);
        }
        return groupByBuilder.toString();
    }
}
