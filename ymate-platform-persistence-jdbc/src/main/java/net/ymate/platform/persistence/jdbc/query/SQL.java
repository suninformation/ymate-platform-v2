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
import net.ymate.platform.persistence.jdbc.IDatabaseConnectionHolder;
import net.ymate.platform.persistence.jdbc.JDBC;
import net.ymate.platform.persistence.jdbc.base.IResultSetHandler;

import java.util.List;

/**
 * SQL语句及参数对象
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/7 上午8:25
 */
public final class SQL {

    private final IDatabase owner;

    private final String sql;

    private final Params params;

    public static SQL create(String sql) {
        return new SQL(JDBC.get(), sql);
    }

    public static SQL create(IDatabase owner, String sql) {
        return new SQL(owner, sql);
    }

    public static SQL create(Select select) {
        return new SQL(select.owner(), select.toString()).param(select.getParams());
    }

    public static SQL create(Insert insert) {
        return new SQL(insert.owner(), insert.toString()).param(insert.params());
    }

    public static SQL create(Update update) {
        return new SQL(update.owner(), update.toString()).param(update.getParams());
    }

    public static SQL create(Delete delete) {
        return new SQL(delete.owner(), delete.toString()).param(delete.getParams());
    }

    private SQL(IDatabase owner, String sql) {
        this.owner = owner;
        this.params = Params.create();
        this.sql = sql;
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

    public int execute() throws Exception {
        return owner.openSession(session -> session.executeForUpdate(this));
    }

    public int execute(String dataSourceName) throws Exception {
        return owner.openSession(dataSourceName, session -> session.executeForUpdate(this));
    }

    public int execute(IDatabaseConnectionHolder connectionHolder) throws Exception {
        return owner.openSession(connectionHolder, session -> session.executeForUpdate(this));
    }

    public <T> T findFirst(IResultSetHandler<T> handler) throws Exception {
        return owner.openSession(session -> session.findFirst(this, handler));
    }

    public <T> T findFirst(String dataSourceName, IResultSetHandler<T> handler) throws Exception {
        return owner.openSession(dataSourceName, session -> session.findFirst(this, handler));
    }

    public <T> T findFirst(IDatabaseConnectionHolder connectionHolder, IResultSetHandler<T> handler) throws Exception {
        return owner.openSession(connectionHolder, session -> session.findFirst(this, handler));
    }

    public <T> List<T> find(IResultSetHandler<T> handler) throws Exception {
        return owner.openSession(session -> session.find(this, handler).getResultData());
    }

    public <T> List<T> find(String dataSourceName, IResultSetHandler<T> handler) throws Exception {
        return owner.openSession(dataSourceName, session -> session.find(this, handler).getResultData());
    }

    public <T> List<T> find(IDatabaseConnectionHolder connectionHolder, IResultSetHandler<T> handler) throws Exception {
        return owner.openSession(connectionHolder, session -> session.find(this, handler).getResultData());
    }
}
