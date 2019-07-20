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
import net.ymate.platform.core.persistence.Params;
import net.ymate.platform.persistence.jdbc.IDatabase;
import org.apache.commons.lang3.StringUtils;

/**
 * Where条件及参数对象
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/7 下午1:19
 */
public final class Where {

    private final IDatabase owner;

    /**
     * SQL条件对象
     */
    private Cond cond;

    private GroupBy groupBy;

    private OrderBy orderBy;

    public static Where create(IDatabase owner) {
        return new Where(owner);
    }

    public static Where create(IDatabase owner, String whereCond) {
        return new Where(owner, whereCond);
    }

    public static Where create(Cond cond) {
        return new Where(cond);
    }

    private Where(IDatabase owner) {
        this.owner = owner;
        orderBy = OrderBy.create(owner);
        cond = Cond.create(owner);
    }

    private Where(IDatabase owner, String whereCond) {
        this(owner);
        cond.cond(whereCond);
    }

    private Where(Cond cond) {
        owner = cond.owner();
        orderBy = OrderBy.create(cond.owner());
        this.cond = cond;
    }

    public Where where(Where where) {
        cond.cond(where.cond());
        orderBy.orderBy(where.orderBy());
        //
        if (where.groupBy() != null) {
            if (groupBy != null) {
                groupBy.fields().add(where.groupBy().fields());
                groupBy.having().cond(where.groupBy().having());
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

    /**
     * @return 此方法仅返回只读参数集合, 若要维护参数请调用where().param(...)相关方法
     */
    public Params getParams() {
        Params params = Params.create().add(cond.params());
        if (groupBy != null && groupBy.having() != null) {
            params.add(groupBy.having().params());
        }
        return params;
    }

    public String toSQL() {
        StringBuilder whereBuilder = new StringBuilder();
        if (cond != null && StringUtils.isNotBlank(cond.toString())) {
            whereBuilder.append("WHERE ").append(cond.toString());
        }
        if (groupBy != null) {
            whereBuilder.append(StringUtils.SPACE).append(groupBy);
        }
        return whereBuilder.toString();
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
            groupBy = GroupBy.create(owner, fields);
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
        groupBy.having(cond);
        return this;
    }

    public Where orderAsc(String field) {
        orderBy.asc(field);
        return this;
    }

    public Where orderAsc(String prefix, String field) {
        orderBy.asc(prefix, field);
        return this;
    }

    public Where orderDesc(String field) {
        orderBy.desc(field);
        return this;
    }

    public Where orderDesc(String prefix, String field) {
        orderBy.desc(prefix, field);
        return this;
    }

    @Override
    public String toString() {
        return String.format("%s %s", toSQL(), orderBy);
    }
}
