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
package net.ymate.platform.persistence.base;

import net.ymate.platform.persistence.jdbc.dialect.IDialect;
import net.ymate.platform.persistence.jdbc.dialect.impl.*;
import net.ymate.platform.persistence.jdbc.impl.DefaultDataSourceAdapter;
import net.ymate.platform.persistence.jdbc.impl.JNDIDataSourceAdapter;

import java.sql.Connection;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据常量/枚举类型
 *
 * @author 刘镇 (suninformation@163.com) on 15/3/29 下午5:23
 * @version 1.0
 */
public class Type {

    /**
     * 字段类型
     */
    public enum FIELD {
        VARCHAR(Types.VARCHAR),
        CHAR(Types.CHAR),
        TEXT(Types.LONGNVARCHAR),
        BOOLEAN(Types.SMALLINT),
        BINARY(Types.BINARY),
        TIMESTAMP(Types.TIMESTAMP),
        DATE(Types.DATE),
        TIME(Types.TIME),
        INT(Types.INTEGER),
        LONG(Types.BIGINT),
        FLOAT(Types.FLOAT),
        NUMBER(Types.NUMERIC),
        UNKNOW(Types.OTHER);

        private int type;

        FIELD(int type) {
            this.type = type;
        }

        public int getType() {
            return this.type;
        }

        public void setType(int type) {
            this.type = type;
        }
    }

    /**
     * 数据库类型
     */
    public enum DATABASE {
        MYSQL, ORACLE, SQLSERVER, DB2, SQLLITE, POSTGRESQL, HSQLDB, H2, UNKNOW
    }

    /**
     * 数据库操作类型
     */
    public enum DB_OPERATION_TYPE {
        QUERY, UPDATE, BATCH_UPDATE
    }

    /**
     * 数据库事务类型
     */
    public enum TRANSACTION {
        /**
         * 不（使用）支持事务
         */
        NONE(Connection.TRANSACTION_NONE),

        /**
         * 在一个事务中进行查询时，允许读取提交前的数据，数据提交后，当前查询就可以读取到数据，update数据时候并不锁住表
         */
        READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED),

        /**
         * 俗称“脏读”（dirty read），在没有提交数据时能够读到已经更新的数据
         */
        READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),
        /**
         * 在一个事务中进行查询时，不允许读取其他事务update的数据，允许读取到其他事务提交的新增数据
         */
        REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ),

        /**
         * 在一个事务中进行查询时，不允许任何对这个查询表的数据修改
         */
        SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE);

        private int _level;

        /**
         * 构造器
         *
         * @param level 事务级别
         */
        private TRANSACTION(int level) {
            this._level = level;
        }

        /**
         * @return the level
         */
        public int getLevel() {
            return _level;
        }

        /**
         * @param level the level to set
         */
        public void setLevel(int level) {
            this._level = level;
        }
    }

    /**
     * 框架提供的已知数据源适配器名称映射
     */
    public static Map<String, String> DS_ADAPTERS;

    /**
     * 框架提供的已知数据库连接驱动
     */
    public static Map<DATABASE, String> DB_DRIVERS;

    /**
     * 框架提供的已知数据库方言
     */
    public static Map<DATABASE, Class<? extends IDialect>> DB_DIALECTS;

    static {
        //
        DS_ADAPTERS = new HashMap<String, String>();
        DS_ADAPTERS.put("default", DefaultDataSourceAdapter.class.getName());
        DS_ADAPTERS.put("jndi", JNDIDataSourceAdapter.class.getName());
        //
        DB_DRIVERS = new HashMap<DATABASE, String>();
        DB_DRIVERS.put(DATABASE.MYSQL, "com.mysql.jdbc.Driver");
        DB_DRIVERS.put(DATABASE.ORACLE, "oracle.jdbc.OracleDriver");
        DB_DRIVERS.put(DATABASE.SQLSERVER, "com.microsoft.sqlserver.jdbc.SQLServerDriver");
        DB_DRIVERS.put(DATABASE.DB2, "com.ibm.db2.jcc.DB2Driver");
        DB_DRIVERS.put(DATABASE.SQLLITE, "org.sqlite.JDBC");
        DB_DRIVERS.put(DATABASE.POSTGRESQL, "org.postgresql.Driver");
        DB_DRIVERS.put(DATABASE.HSQLDB, "org.hsqldb.jdbcDriver");
        DB_DRIVERS.put(DATABASE.H2, "org.h2.Driver");
        //
        DB_DIALECTS = new HashMap<DATABASE, Class<? extends IDialect>>();
        DB_DIALECTS.put(DATABASE.MYSQL, MySQLDialect.class);
        DB_DIALECTS.put(DATABASE.ORACLE, OracleDialect.class);
        DB_DIALECTS.put(DATABASE.SQLSERVER, SQLServerDialect.class);
        DB_DIALECTS.put(DATABASE.DB2, DB2Dialect.class);
        DB_DIALECTS.put(DATABASE.SQLLITE, SQLiteDialect.class);
        DB_DIALECTS.put(DATABASE.POSTGRESQL, PostgreSQLDialect.class);
        DB_DIALECTS.put(DATABASE.HSQLDB, HSQLDBDialect.class);
        DB_DIALECTS.put(DATABASE.H2, H2Dialect.class);
    }
}
