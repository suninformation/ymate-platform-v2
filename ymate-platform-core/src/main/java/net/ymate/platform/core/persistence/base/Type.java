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
package net.ymate.platform.core.persistence.base;

import java.sql.Connection;
import java.sql.Types;

/**
 * 数据常量/枚举类型
 *
 * @author 刘镇 (suninformation@163.com) on 15/3/29 下午5:23
 */
public final class Type {

    /**
     * 字段类型
     */
    public enum FIELD {

        /**
         * 字符串
         */
        VARCHAR(Types.VARCHAR, "VARCHAR"),

        /**
         * 非ASCII字符串
         */
        NCHAR(Types.NCHAR, "NCHAR"),

        /**
         * 字符
         */
        CHAR(Types.CHAR, "CHAR"),

        /**
         * 备注
         */
        TEXT(Types.LONGVARCHAR, "TEXT"),

        /**
         * 布尔
         */
        BOOLEAN(Types.BOOLEAN, "BOOLEAN"),

        /**
         * 二进制
         */
        BINARY(Types.BINARY, "BINARY"),

        /**
         * 时间戳
         */
        TIMESTAMP(Types.TIMESTAMP, "TIMESTAMP"),

        /**
         * 日期
         */
        DATE(Types.DATE, "DATE"),

        /**
         * 时间
         */
        TIME(Types.TIME, "TIME"),

        /**
         * 整型
         */
        INT(Types.INTEGER, "INTEGER"),

        /**
         * 短整型
         */
        SMALLINT(Types.SMALLINT, "SMALLINT"),

        /**
         * 长整型
         */
        LONG(Types.BIGINT, "BIGINT"),

        /**
         * 浮点型
         */
        FLOAT(Types.FLOAT, "FLOAT"),

        /**
         * 双精度型
         */
        DOUBLE(Types.DOUBLE, "DOUBLE"),

        /**
         * 数值型
         */
        NUMBER(Types.NUMERIC, "NUMERIC"),

        /**
         * 二进制大对象
         */
        BLOB(Types.BLOB, "BLOB"),

        /**
         * 字符大对象
         */
        CLOB(Types.CLOB, "CLOB"),

        /**
         * 布尔型
         */
        BIT(Types.BIT, "BIT"),

        /**
         * 单字节整型
         */
        TINYINT(Types.TINYINT, "TINYINT"),

        /**
         * 未知类型
         */
        UNKNOWN(Types.OTHER, "VARCHAR");

        private int type;

        private String name;

        FIELD(int type, String name) {
            this.type = type;
            this.name = name;
        }

        public int getType() {
            return this.type;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * 操作类型
     */
    public enum OPT {

        /**
         * 查询
         */
        QUERY,

        /**
         * 更新
         */
        UPDATE,

        /**
         * 批量更新
         */
        BATCH_UPDATE,

        /**
         * 存储过程
         */
        PROCEDURE
    }

    /**
     * 数据库类型
     */
    public interface DATABASE {

        String MYSQL = "MYSQL";

        String ORACLE = "ORACLE";

        String SQLSERVER = "SQLSERVER";

        String DB2 = "DB2";

        String SQLITE = "SQLITE";

        String POSTGRESQL = "POSTGRESQL";

        String HSQLDB = "HSQLDB";

        String H2 = "H2";

        String UNKNOWN = "UNKNOWN";
    }

    /**
     * 数据源适配器类型
     */
    public interface DS_ADAPTER {

        String DEFAULT = "default";

        String JNDI = "jndi";

        String C3P0 = "c3p0";

        String DBCP = "dbcp";

        String DRUID = "druid";

        String HIKARICP = "hikaricp";
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

        private int level;

        /**
         * 构造器
         *
         * @param level 事务级别
         */
        TRANSACTION(int level) {
            this.level = level;
        }

        /**
         * @return the level
         */
        public int getLevel() {
            return level;
        }

        /**
         * @param level the level to set
         */
        public void setLevel(int level) {
            this.level = level;
        }
    }
}
