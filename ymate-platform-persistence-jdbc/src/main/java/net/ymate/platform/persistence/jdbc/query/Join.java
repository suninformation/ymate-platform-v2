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

import net.ymate.platform.core.persistence.Params;
import net.ymate.platform.persistence.jdbc.IDatabase;
import net.ymate.platform.persistence.jdbc.JDBC;
import org.apache.commons.lang3.StringUtils;

/**
 * 连接查询语句对象
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/12 下午6:04
 */
public final class Join extends Query<Join> {

    private final String from;

    private String alias;

    private final Cond on;

    public static Join inner(String from) {
        return inner(JDBC.get(), JDBC.get().getConfig().getDefaultDataSourceName(), from, true);
    }

    public static Join inner(String from, boolean safePrefix) {
        return inner(JDBC.get(), JDBC.get().getConfig().getDefaultDataSourceName(), null, from, safePrefix);
    }

    public static Join inner(String prefix, String from) {
        return inner(JDBC.get(), JDBC.get().getConfig().getDefaultDataSourceName(), prefix, from, true);
    }

    public static Join inner(String prefix, String from, boolean safePrefix) {
        return new Join(JDBC.get(), JDBC.get().getConfig().getDefaultDataSourceName(), "INNER JOIN", prefix, from, safePrefix);
    }

    public static Join left(String from) {
        return left(JDBC.get(), JDBC.get().getConfig().getDefaultDataSourceName(), from, true);
    }

    public static Join left(String from, boolean safePrefix) {
        return left(JDBC.get(), JDBC.get().getConfig().getDefaultDataSourceName(), null, from, safePrefix);
    }

    public static Join left(String prefix, String from) {
        return left(JDBC.get(), JDBC.get().getConfig().getDefaultDataSourceName(), prefix, from, true);
    }

    public static Join left(String prefix, String from, boolean safePrefix) {
        return new Join(JDBC.get(), JDBC.get().getConfig().getDefaultDataSourceName(), "LEFT JOIN", prefix, from, safePrefix);
    }

    public static Join right(String from) {
        return right(JDBC.get(), JDBC.get().getConfig().getDefaultDataSourceName(), from, true);
    }

    public static Join right(String from, boolean safePrefix) {
        return right(JDBC.get(), JDBC.get().getConfig().getDefaultDataSourceName(), null, from, safePrefix);
    }

    public static Join right(String prefix, String from) {
        return right(JDBC.get(), JDBC.get().getConfig().getDefaultDataSourceName(), prefix, from, true);
    }

    public static Join right(String prefix, String from, boolean safePrefix) {
        return new Join(JDBC.get(), JDBC.get().getConfig().getDefaultDataSourceName(), "RIGHT JOIN", prefix, from, safePrefix);
    }

    //

    public static Join inner(Select select) {
        Join target = inner(select.owner(), select.dataSourceName(), null, select.toString(), false);
        target.params().add(select.params());
        return target;
    }

    public static Join inner(IDatabase owner, String dataSourceName, String from) {
        return inner(owner, dataSourceName, from, true);
    }

    public static Join inner(IDatabase owner, String dataSourceName, String from, boolean safePrefix) {
        return inner(owner, dataSourceName, null, from, safePrefix);
    }

    public static Join inner(IDatabase owner, String dataSourceName, String prefix, String from) {
        return inner(owner, dataSourceName, prefix, from, true);
    }

    public static Join inner(IDatabase owner, String dataSourceName, String prefix, String from, boolean safePrefix) {
        return new Join(owner, dataSourceName, "INNER JOIN", prefix, from, safePrefix);
    }

    //

    public static Join left(Select select) {
        Join target = left(select.owner(), select.dataSourceName(), null, select.toString(), false);
        target.params().add(select.params());
        return target;
    }

    public static Join left(IDatabase owner, String dataSourceName, String from) {
        return left(owner, dataSourceName, from, true);
    }

    public static Join left(IDatabase owner, String dataSourceName, String from, boolean safePrefix) {
        return left(owner, dataSourceName, null, from, safePrefix);
    }

    public static Join left(IDatabase owner, String dataSourceName, String prefix, String from) {
        return left(owner, dataSourceName, prefix, from, true);
    }

    public static Join left(IDatabase owner, String dataSourceName, String prefix, String from, boolean safePrefix) {
        return new Join(owner, dataSourceName, "LEFT JOIN", prefix, from, safePrefix);
    }

    //

    public static Join right(Select select) {
        Join target = right(select.owner(), select.dataSourceName(), null, select.toString(), false);
        target.params().add(select.params());
        return target;
    }

    public static Join right(IDatabase owner, String dataSourceName, String from) {
        return right(owner, dataSourceName, from, true);
    }

    public static Join right(IDatabase owner, String dataSourceName, String from, boolean safePrefix) {
        return right(owner, dataSourceName, null, from, safePrefix);
    }

    public static Join right(IDatabase owner, String dataSourceName, String prefix, String from) {
        return right(owner, dataSourceName, prefix, from, true);
    }

    public static Join right(IDatabase owner, String dataSourceName, String prefix, String from, boolean safePrefix) {
        return new Join(owner, dataSourceName, "RIGHT JOIN", prefix, from, safePrefix);
    }

    private Join(IDatabase owner, String dataSourceName, String type, String prefix, String from, boolean safePrefix) {
        super(owner, dataSourceName);
        if (safePrefix) {
            from = buildSafeTableName(prefix, from, true);
        }
        this.from = String.format("%s %s", type, from);
        on = Cond.create(owner, dataSourceName);
    }

    public Join alias(String alias) {
        this.alias = alias;
        return this;
    }

    public Join on(Cond cond) {
        on.cond(cond);
        return this;
    }

    public Params params() {
        return on.params();
    }

    @Override
    public String toString() {
        alias = StringUtils.trimToNull(alias);
        if (alias == null) {
            alias = StringUtils.EMPTY;
        } else {
            alias = StringUtils.SPACE.concat(alias);
        }
        return String.format("%s%s ON %s", from, alias, on);
    }
}
