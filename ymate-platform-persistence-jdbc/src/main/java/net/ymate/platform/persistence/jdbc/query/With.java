/*
 * Copyright 2007-present the original author or authors.
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
import net.ymate.platform.core.persistence.Params;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * WITH 公用表表达式（Common Table Expressions - CTE）
 *
 * @author 刘镇 (suninformation@163.com) on 2025/9/29 17:18
 * @since 2.1.4
 */
public class With {

    private final String name;

    private final Fields columnNames = Fields.create();

    private final SQL subquery;

    private boolean recursive;

    private final Params params = Params.create();

    private final List<With> withs = new ArrayList<>();

    public static With create(String name, Select subquery) {
        return new With(name, subquery);
    }

    public static With create(String name, Fields columnNames, Select subquery) {
        return new With(name, columnNames, subquery);
    }

    public static With create(String name, SQL subquery) {
        return new With(name, subquery);
    }

    public static With create(String name, Fields columnNames, SQL subquery) {
        return new With(name, columnNames, subquery);
    }

    public With(String name, Select subquery) {
        this(name, null, subquery.toSQL());
    }

    public With(String name, Fields columnNames, Select subquery) {
        this(name, null, subquery.toSQL());
    }

    public With(String name, SQL subquery) {
        this(name, null, subquery);
    }

    public With(String name, Fields columnNames, SQL subquery) {
        this.name = name;
        if (columnNames != null && !columnNames.isEmpty()) {
            this.columnNames.add(columnNames);
        }
        this.subquery = subquery;
        this.params.add(subquery.params());
    }

    public String name() {
        return name;
    }

    public With recursive(boolean recursive) {
        this.recursive = recursive;
        return this;
    }

    public Params params() {
        return params;
    }

    public With with(String name, Select subquery) {
        withs.add(With.create(name, columnNames, subquery));
        params.add(subquery.params());
        return this;
    }

    public With with(String name, Fields columnNames, Select subquery) {
        withs.add(With.create(name, columnNames, subquery));
        params.add(subquery.params());
        return this;
    }

    public With with(String name, SQL subquery) {
        withs.add(With.create(name, columnNames, subquery));
        params.add(subquery.params());
        return this;
    }

    public With with(String name, Fields columnNames, SQL subquery) {
        withs.add(With.create(name, columnNames, subquery));
        params.add(subquery.params());
        return this;
    }

    public String toSQL() {
        return String.format("WITH %s%s", recursive ? "RECURSIVE " : StringUtils.EMPTY, this);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(name);
        if (!columnNames.isEmpty()) {
            builder.append("(").append(StringUtils.join(columnNames.toArray(), ", ")).append(")");
        }
        builder.append(" AS (").append(subquery).append(")");
        for (With other : withs) {
            builder.append(", ").append(other);
        }
        return builder.toString();
    }

}
