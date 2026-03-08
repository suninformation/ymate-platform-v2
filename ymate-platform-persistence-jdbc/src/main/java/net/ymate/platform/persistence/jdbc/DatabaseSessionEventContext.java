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
package net.ymate.platform.persistence.jdbc;

import net.ymate.platform.core.persistence.Params;
import net.ymate.platform.core.persistence.SessionEventContext;
import net.ymate.platform.core.persistence.base.Type;
import net.ymate.platform.persistence.jdbc.query.BatchSQL;
import net.ymate.platform.persistence.jdbc.query.SQL;

/**
 * 数据库会话事件上下文
 *
 * @author 刘镇 (suninformation@163.com) on 2026/3/8 15:23
 * @since 2.1.4
 */
public class DatabaseSessionEventContext extends SessionEventContext {

    private String sql;

    private Params params;

    private BatchSQL batchSQL;

    public DatabaseSessionEventContext(IDatabaseSession source, Type.OPT operationType, String sql, Params params) {
        super(source, operationType);
        this.sql = sql;
        this.params = params;
    }

    public DatabaseSessionEventContext(IDatabaseSession source, Type.OPT operationType, BatchSQL batchSQL) {
        super(source, operationType);
        this.batchSQL = batchSQL;
    }

    @Override
    public IDatabaseSession getSource() {
        return (IDatabaseSession) super.getSource();
    }

    @Override
    public DatabaseSessionEventContext putAttribute(String key, Object value) {
        super.putAttribute(key, value);
        return this;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public Params getParams() {
        return params;
    }

    public void setParams(Params params) {
        this.params = params;
    }

    public BatchSQL getBatchSQL() {
        return batchSQL;
    }

    public void setBatchSQL(BatchSQL batchSQL) {
        this.batchSQL = batchSQL;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Type.OPT operationType;

        private String sql;

        private Params params;

        private BatchSQL batchSQL;

        public Builder operationType(Type.OPT operationType) {
            this.operationType = operationType;
            return this;
        }

        public Builder sql(SQL sql) {
            this.sql = sql.toString();
            this.params = sql.params();
            return this;
        }

        public Builder sql(BatchSQL batchSQL) {
            this.batchSQL = batchSQL;
            return this;
        }

        public Builder sql(String sql) {
            this.sql = sql;
            return this;
        }

        public Builder params(Params params) {
            this.params = params;
            return this;
        }

        public DatabaseSessionEventContext build(IDatabaseSession source) {
            DatabaseSessionEventContext sessionEventContext;
            if (batchSQL != null) {
                sessionEventContext = new DatabaseSessionEventContext(source, this.operationType, batchSQL);
            } else {
                sessionEventContext = new DatabaseSessionEventContext(source, operationType, sql, params);
            }
            return sessionEventContext;
        }
    }
}
