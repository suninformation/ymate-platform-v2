/*
 * Copyright 2007-2107 the original author or authors.
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

import org.apache.commons.lang.StringUtils;

/**
 * Where条件及参数对象
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/7 下午1:19
 * @version 1.0
 */
public class Where {

    /**
     * Where条件SQL语句
     */
    private String __cond;

    /**
     * SQL参数集合
     */
    private Params __params;

    private GroupBy __groupBy;

    private OrderBy __orderBy;

    public static Where create() {
        return new Where();
    }

    public static Where create(String whereCond) {
        return new Where(whereCond);
    }

    public static Where create(Cond cond) {
        return new Where(cond);
    }

    private Where() {
        this.__params = Params.create();
        this.__orderBy = OrderBy.create();
    }

    private Where(String whereCond) {
        this();
        this.__cond = whereCond;
    }

    private Where(Cond cond) {
        this.__orderBy = OrderBy.create();
        this.__cond = cond.toString();
        this.__params = cond.getParams();
    }

    public GroupBy getGroupBy() {
        return __groupBy;
    }

    public OrderBy getOrderBy() {
        return __orderBy;
    }

    public Params getParams() {
        Params _p = Params.create().add(this.__params);
        if (__groupBy != null) {
            _p.add(__groupBy.getHaving().getParams());
        }
        return _p;
    }

    public String getWhereSQL() {
        if (StringUtils.isNotBlank(this.__cond)) {
            return "WHERE ".concat(this.__cond);
        }
        return "";
    }

    public Where addParam(Object param) {
        this.__params.add(param);
        return this;
    }

    public Where groupBy(String field) {
        groupBy(Fields.create().add(field));
        return this;
    }

    public Where groupBy(Fields fields) {
        __groupBy = GroupBy.create(fields);
        return this;
    }

    public Where groupBy(GroupBy groupBy) {
        __groupBy = groupBy;
        return this;
    }

    public Where having(Cond cond) {
        __groupBy.having(cond);
        return this;
    }

    public Where orderAsc(String field) {
        this.__orderBy.asc(field);
        return this;
    }

    public Where orderDesc(String field) {
        this.__orderBy.desc(field);
        return this;
    }

    @Override
    public String toString() {
        return getWhereSQL().concat(" ").concat(__groupBy.getGroupBySQL()).concat(" ").concat(__orderBy.getOrderBySQL());
    }
}
