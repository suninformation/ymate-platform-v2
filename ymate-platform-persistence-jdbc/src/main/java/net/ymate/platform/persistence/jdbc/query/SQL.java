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

/**
 * SQL语句及参数对象
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/7 上午8:25
 */
public final class SQL {

    private final String sql;

    private final Params params;

    public static SQL create(String sql) {
        return new SQL(sql);
    }

    public static SQL create(Select select) {
        return new SQL(select.toString()).param(select.getParams());
    }

    public static SQL create(Insert insert) {
        return new SQL(insert.toString()).param(insert.params());
    }

    public static SQL create(Update update) {
        return new SQL(update.toString()).param(update.getParams());
    }

    public static SQL create(Delete delete) {
        return new SQL(delete.toString()).param(delete.getParams());
    }

    private SQL(String sql) {
        this.params = Params.create();
        this.sql = sql;
    }

    public String getSQL() {
        return this.sql;
    }

    public SQL param(Object param) {
        this.params.add(param);
        return this;
    }

    public SQL param(Params params) {
        this.params.add(params);
        return this;
    }

    public Params params() {
        return this.params;
    }

    @Override
    public String toString() {
        return this.sql;
    }
}
