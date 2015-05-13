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
 * 条件对象
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/9 下午8:12
 * @version 1.0
 */
public class Cond {

    private StringBuilder __condSB;

    /**
     * SQL参数集合
     */
    private Params __params;

    public static Cond create() {
        return new Cond();
    }

    private Cond() {
        __condSB = new StringBuilder();
        __params = Params.create();
    }

    public Params getParams() {
        return this.__params;
    }

    public Cond addParam(Object param) {
        this.__params.add(param);
        return this;
    }

    public Cond addParam(Params params) {
        this.__params.add(params);
        return this;
    }

    public Cond opt(String field, String opt) {
        __condSB.append(field).append(" ").append(opt).append(" ?");
        return this;
    }

    public Cond eq(String field) {
        return opt(field, "=");
    }

    public Cond notEq(String field) {
        return opt(field, "!=");
    }

    public Cond gtEq(String field) {
        return opt(field, ">=");
    }

    public Cond gt(String field) {
        return opt(field, ">");
    }

    public Cond ltEq(String field) {
        return opt(field, "<=");
    }

    public Cond lt(String field) {
        return opt(field, "<");
    }

    public Cond like(String field) {
        return opt(field, "LIKE");
    }

    public Cond and() {
        __doAppendCond("AND");
        return this;
    }

    public Cond or() {
        __doAppendCond("OR");
        return this;
    }

    public Cond not() {
        __doAppendCond("NOT");
        return this;
    }

    public Cond in(String field, SQL subSql) {
        __condSB.append(field).append(" IN (").append(subSql.getSQL()).append(")");
        __params.add(subSql.getParams());
        return this;
    }

    public Cond in(String field, Params params) {
        __condSB.append(field).append(" IN (").append(StringUtils.repeat("?", ", ", params.getParams().size())).append(")");
        __params.add(params);
        return this;
    }

    private void __doAppendCond(String cond) {
        if (__condSB.length() > 0) {
            __condSB.append(" ").append(cond).append(" ");
        }
    }

    @Override
    public String toString() {
        return __condSB.toString();
    }
}
