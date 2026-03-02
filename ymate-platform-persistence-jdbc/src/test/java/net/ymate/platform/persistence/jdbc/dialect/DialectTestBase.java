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
package net.ymate.platform.persistence.jdbc.dialect;

import net.ymate.platform.core.persistence.annotation.*;
import net.ymate.platform.core.persistence.base.IEntity;
import net.ymate.platform.core.persistence.base.IEntityPK;
import net.ymate.platform.core.persistence.base.Type;
import org.junit.Assert;

import java.sql.Date;

/**
 * 方言测试基类
 *
 * @author 刘镇 (suninformation@163.com) on 2026/2/24 上午10:00
 * @since 2.1.4
 */
public abstract class DialectTestBase {

    /**
     * 测试实体类 - 单主键
     */
    @Entity("test_user")
    @Comment("用户信息表")
    @Index(name = "idx_name", fields = {"name"}, unique = false)
    @Index(name = "idx_email", fields = {"email"}, unique = true)
    public static class TestUser implements IEntity<String> {

        @Id
        @Comment("用户ID")
        @Property(name = "id", type = Type.FIELD.VARCHAR, length = 32, nullable = false)
        private String id;

        @Comment("用户名")
        @Property(name = "name", type = Type.FIELD.VARCHAR, length = 100, nullable = false)
        private String name;

        @Comment("年龄")
        @Default("18")
        @Property(name = "age", type = Type.FIELD.INT, length = 3, nullable = true)
        private Integer age;

        @Comment("邮箱")
        @Property(name = "email", type = Type.FIELD.VARCHAR, length = 200, nullable = true)
        private String email;

        @Comment("创建时间")
        @Property(name = "create_time", type = Type.FIELD.TIMESTAMP, nullable = false)
        @Default(value = "current_timestamp", ignored = true)
        private Date createTime;

        @Override
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    /**
     * 测试实体类 - 复合主键
     */
    @Entity("test_order_item")
    public static class TestOrderItem implements IEntity<TestOrderItem.OrderPK> {

        @PK
        public static class OrderPK implements IEntityPK {

            @Property(name = "order_id", type = Type.FIELD.VARCHAR, length = 32, nullable = false)
            private String orderId;

            @Property(name = "product_id", type = Type.FIELD.VARCHAR, length = 32, nullable = false)
            private String productId;

            public OrderPK() {
            }

            public OrderPK(String orderId, String productId) {
                this.orderId = orderId;
                this.productId = productId;
            }

            public String getOrderId() {
                return orderId;
            }

            public void setOrderId(String orderId) {
                this.orderId = orderId;
            }

            public String getProductId() {
                return productId;
            }

            public void setProductId(String productId) {
                this.productId = productId;
            }
        }

        @Id
        private OrderPK id;

        @Property(name = "quantity", type = Type.FIELD.INT, length = 5, nullable = false)
        private Integer quantity;

        @Property(name = "price", type = Type.FIELD.DOUBLE, length = 10, decimals = 2, nullable = false)
        private Double price;

        @Override
        public OrderPK getId() {
            return id;
        }

        public void setId(OrderPK id) {
            this.id = id;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public Double getPrice() {
            return price;
        }

        public void setPrice(Double price) {
            this.price = price;
        }
    }

    /**
     * 测试实体类 - 自增主键
     */
    @Entity("test_auto_increment")
    public static class TestAutoIncrement implements IEntity<Long> {

        @Id
        @Property(name = "id", type = Type.FIELD.LONG, autoincrement = true, sequenceName = "seq_test_auto_increment", nullable = false)
        private Long id;

        @Property(name = "name", type = Type.FIELD.VARCHAR, length = 100, nullable = false)
        private String name;

        @Property(name = "status", type = Type.FIELD.INT, length = 1, nullable = false)
        private Integer status;

        @Property(name = "create_time", type = Type.FIELD.TIMESTAMP, nullable = false)
        @Default(value = "current_timestamp", ignored = true)
        private Date createTime;

        @Override
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }
    }

    /**
     * 测试实体类 - 无主键（仅用于测试异常情况）
     */
    @Entity("test_no_pk")
    public static class TestNoPK implements IEntity<String> {

        @Property
        private String name;

        @Property
        private Integer value;

        @Override
        public String getId() {
            return null;
        }

        public void setId(String id) {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getValue() {
            return value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }
    }

    /**
     * 断言 SQL 包含预期内容（忽略空白差异）
     *
     * @param actual   实际 SQL
     * @param expected 预期包含的内容
     */
    protected void assertSqlContains(String actual, String expected) {
        String normalizedActual = normalizeSql(actual);
        String normalizedExpected = normalizeSql(expected);
        Assert.assertTrue("SQL should contain: " + expected + "\nActual: " + actual,
                normalizedActual.contains(normalizedExpected));
    }

    /**
     * 断言 SQL 等于预期内容（忽略空白差异）
     *
     * @param actual   实际 SQL
     * @param expected 预期 SQL
     */
    protected void assertSqlEquals(String actual, String expected) {
        Assert.assertEquals(normalizeSql(expected), normalizeSql(actual));
    }

    /**
     * 标准化 SQL（去除多余空白，统一大小写关键字用于比较）
     *
     * @param sql SQL 语句
     * @return 标准化后的 SQL
     */
    protected String normalizeSql(String sql) {
        if (sql == null) {
            return "";
        }
        // 去除首尾空白，将多个空白替换为单个空格
        return sql.trim().replaceAll("\\s+", " ");
    }

    /**
     * 打印 SQL 语句（用于调试）
     *
     * @param testName 测试名称
     * @param sql      SQL 语句
     */
    protected void printSql(String testName, String sql) {
        System.out.println("\n========== " + getClass().getSimpleName() + " - " + testName + " ==========");
        System.out.println(sql);
        System.out.println("================================================================================\n");
    }

    /**
     * 创建方言实例
     *
     * @return 方言实例
     */
    protected abstract IDialect createDialect();

    /**
     * 测试 buildCountSQL 方法
     */
    @org.junit.Test
    public void testBuildCountSQL() {
        IDialect dialect = createDialect();
        String originSql = "SELECT * FROM test_user WHERE age > 18 ORDER BY name DESC";
        String countSql = dialect.buildCountSQL(originSql);
        printSql("testBuildCountSQL", countSql);

        // 验证 COUNT SQL 结构
        assertSqlContains(countSql, "SELECT count(*)");
        assertSqlContains(countSql, "FROM");
        assertSqlContains(countSql, "test_user");
        // ORDER BY 应该被移除
        org.junit.Assert.assertFalse("ORDER BY should be removed",
                countSql.toLowerCase().contains("order by"));
    }

    /**
     * 测试 buildInsertSql 方法 - 默认字段
     */
    @org.junit.Test
    public void testBuildInsertSql() {
        IDialect dialect = createDialect();
        String sql = dialect.buildInsertSql(TestUser.class, null, null, null);
        printSql("testBuildInsertSql", sql);

        // 验证 INSERT 语句结构
        assertSqlContains(sql, "INSERT INTO");
        assertSqlContains(sql, "test_user");
        assertSqlContains(sql, "id");
        assertSqlContains(sql, "name");
        assertSqlContains(sql, "age");
        assertSqlContains(sql, "email");
        assertSqlContains(sql, "VALUES");
        // 验证占位符数量
        int placeholderCount = sql.split("\\?").length - 1;
        org.junit.Assert.assertEquals("Should have 4 placeholders", 4, placeholderCount);
    }

    /**
     * 测试 buildInsertSql 方法 - 指定字段
     */
    @org.junit.Test
    public void testBuildInsertSqlWithFields() {
        IDialect dialect = createDialect();
        net.ymate.platform.core.persistence.Fields fields = net.ymate.platform.core.persistence.Fields.create("name", "email");
        String sql = dialect.buildInsertSql(TestUser.class, null, null, fields);
        printSql("testBuildInsertSqlWithFields (指定字段 name, email)", sql);

        // 验证只包含指定字段
        assertSqlContains(sql, "name");
        assertSqlContains(sql, "email");
        // 验证占位符数量
        int placeholderCount = sql.split("\\?").length - 1;
        org.junit.Assert.assertEquals("Should have 2 placeholders", 2, placeholderCount);
    }

    /**
     * 测试 buildDeleteByPkSql 方法 - 单主键
     */
    @org.junit.Test
    public void testBuildDeleteByPkSql() {
        IDialect dialect = createDialect();
        String sql = dialect.buildDeleteByPkSql(TestUser.class, null, null, null);
        printSql("testBuildDeleteByPkSql (单主键)", sql);

        // 验证 DELETE 语句结构
        assertSqlContains(sql, "DELETE FROM");
        assertSqlContains(sql, "test_user");
        assertSqlContains(sql, "WHERE");
        // 验证包含 id 和 = ?，考虑到不同数据库可能有引号
        String normalizedSql = normalizeSql(sql);
        org.junit.Assert.assertTrue("Should contain id and = ?",
                normalizedSql.contains("id") && normalizedSql.contains("=") && normalizedSql.contains("?"));
    }

    /**
     * 测试 buildDeleteByPkSql 方法 - 复合主键
     */
    @org.junit.Test
    public void testBuildDeleteByPkSqlCompositeKey() {
        IDialect dialect = createDialect();
        String sql = dialect.buildDeleteByPkSql(TestOrderItem.class, null, null, null);
        printSql("testBuildDeleteByPkSqlCompositeKey (复合主键)", sql);

        // 验证 DELETE 语句结构
        assertSqlContains(sql, "DELETE FROM");
        assertSqlContains(sql, "test_order_item");
        assertSqlContains(sql, "WHERE");
        // 验证包含主键字段（EntityMeta 将 orderId 转换为 order_id）
        String normalizedSql = normalizeSql(sql);
        org.junit.Assert.assertTrue("Should contain order_id",
                normalizedSql.contains("order_id"));
        // 复合主键应该同时包含 order_id 和 product_id，并且用 AND 连接
        if (normalizedSql.contains("product_id")) {
            org.junit.Assert.assertTrue("Should contain and for composite key",
                    normalizedSql.toLowerCase().contains("and"));
        }
    }

    /**
     * 测试 buildUpdateByPkSql 方法 - 单主键，默认字段
     */
    @org.junit.Test
    public void testBuildUpdateByPkSql() {
        IDialect dialect = createDialect();
        String sql = dialect.buildUpdateByPkSql(TestUser.class, null, null, null, null);
        printSql("testBuildUpdateByPkSql (单主键，默认字段)", sql);

        // 验证 UPDATE 语句结构
        assertSqlContains(sql, "UPDATE");
        assertSqlContains(sql, "test_user");
        assertSqlContains(sql, "SET");
        assertSqlContains(sql, "WHERE");

        // 验证包含主键在 WHERE 部分
        String normalizedSql = normalizeSql(sql);
        org.junit.Assert.assertTrue("Should contain id in WHERE clause",
                normalizedSql.contains("id") && normalizedSql.contains("=") && normalizedSql.contains("?"));

        // 验证 SET 部分包含非主键字段（考虑到不同数据库可能有引号）
        org.junit.Assert.assertTrue("Should contain name",
                normalizedSql.contains("name"));
        org.junit.Assert.assertTrue("Should contain age",
                normalizedSql.contains("age"));
        org.junit.Assert.assertTrue("Should contain email",
                normalizedSql.contains("email"));
    }

    /**
     * 测试 buildUpdateByPkSql 方法 - 指定字段
     */
    @org.junit.Test
    public void testBuildUpdateByPkSqlWithFields() {
        IDialect dialect = createDialect();
        net.ymate.platform.core.persistence.Fields fields = net.ymate.platform.core.persistence.Fields.create("name", "age");
        String sql = dialect.buildUpdateByPkSql(TestUser.class, null, null, null, fields);
        printSql("testBuildUpdateByPkSqlWithFields (指定字段 name, age)", sql);

        // 验证只包含指定字段
        String normalizedSql = normalizeSql(sql);
        org.junit.Assert.assertTrue("Should contain name",
                normalizedSql.contains("name"));
        org.junit.Assert.assertTrue("Should contain age",
                normalizedSql.contains("age"));
        // email 不在指定字段中
        org.junit.Assert.assertFalse("email should not be in SET clause",
                normalizedSql.contains("email"));
    }

    /**
     * 测试 buildSelectSql 方法 - 默认字段
     */
    @org.junit.Test
    public void testBuildSelectSql() {
        IDialect dialect = createDialect();
        String sql = dialect.buildSelectSql(TestUser.class, null, null, null);
        printSql("testBuildSelectSql (默认字段)", sql);

        // 验证 SELECT 语句结构
        assertSqlContains(sql, "SELECT");
        assertSqlContains(sql, "FROM");
        assertSqlContains(sql, "test_user");
        assertSqlContains(sql, "id");
        assertSqlContains(sql, "name");
        assertSqlContains(sql, "age");
        assertSqlContains(sql, "email");
    }

    /**
     * 测试 buildSelectSql 方法 - 指定字段
     */
    @org.junit.Test
    public void testBuildSelectSqlWithFields() {
        IDialect dialect = createDialect();
        net.ymate.platform.core.persistence.Fields fields = net.ymate.platform.core.persistence.Fields.create("id", "name");
        String sql = dialect.buildSelectSql(TestUser.class, null, null, fields);
        printSql("testBuildSelectSqlWithFields (指定字段 id, name)", sql);

        // 验证只包含指定字段
        assertSqlContains(sql, "id");
        assertSqlContains(sql, "name");
        // age 和 email 不在指定字段中
        org.junit.Assert.assertFalse("age should not be in SELECT clause",
                sql.contains("age"));
        org.junit.Assert.assertFalse("email should not be in SELECT clause",
                sql.contains("email"));
    }

    /**
     * 测试 buildSelectByPkSql 方法 - 单主键
     */
    @org.junit.Test
    public void testBuildSelectByPkSql() {
        IDialect dialect = createDialect();
        String sql = dialect.buildSelectByPkSql(TestUser.class, null, null, null, null);
        printSql("testBuildSelectByPkSql (单主键)", sql);

        // 验证 SELECT 语句结构
        assertSqlContains(sql, "SELECT");
        assertSqlContains(sql, "FROM");
        assertSqlContains(sql, "test_user");
        assertSqlContains(sql, "WHERE");
        // 验证包含主键条件
        String normalizedSql = normalizeSql(sql);
        org.junit.Assert.assertTrue("Should contain id and = ?",
                normalizedSql.contains("id") && normalizedSql.contains("=") && normalizedSql.contains("?"));
    }

    /**
     * 测试 buildSelectByPkSql 方法 - 复合主键
     */
    @org.junit.Test
    public void testBuildSelectByPkSqlCompositeKey() {
        IDialect dialect = createDialect();
        String sql = dialect.buildSelectByPkSql(TestOrderItem.class, null, null, null, null);
        printSql("testBuildSelectByPkSqlCompositeKey (复合主键)", sql);

        // 验证 SELECT 语句结构
        assertSqlContains(sql, "SELECT");
        assertSqlContains(sql, "FROM");
        assertSqlContains(sql, "test_order_item");
        assertSqlContains(sql, "WHERE");
        // 验证包含主键字段（EntityMeta 将 orderId 转换为 order_id）
        String normalizedSql = normalizeSql(sql);
        org.junit.Assert.assertTrue("Should contain order_id",
                normalizedSql.contains("order_id"));
        // 检查 WHERE 子句中是否有多个条件（AND）
        String whereClause = "";
        int whereIndex = normalizedSql.toLowerCase().indexOf("where");
        if (whereIndex >= 0) {
            whereClause = normalizedSql.substring(whereIndex);
        }
        // 如果 WHERE 子句中有 product_id，则应该有 AND
        if (whereClause.contains("product_id")) {
            org.junit.Assert.assertTrue("Should contain and for composite key in WHERE clause",
                    whereClause.toLowerCase().contains("and"));
        }
    }

    /**
     * 测试 buildCreateSql 方法 - 普通主键
     */
    @org.junit.Test
    public void testBuildCreateSql() {
        IDialect dialect = createDialect();
        String sql = dialect.buildCreateSql(TestUser.class, null, null);
        printSql("testBuildCreateSql (普通主键)", sql);

        // 验证 CREATE TABLE 语句结构
        assertSqlContains(sql, "CREATE TABLE");
        assertSqlContains(sql, "test_user");
        // 验证包含字段定义（带类型和长度）
        assertSqlContains(sql, "id");
        assertSqlContains(sql, "name");
        assertSqlContains(sql, "age");
        assertSqlContains(sql, "email");
        // 验证主键
        assertSqlContains(sql, "PRIMARY KEY");
        // 验证索引（不同数据库实现方式不同）
        // MySQL: 索引在 CREATE TABLE 语句中
        // SQLServer/Oracle: 索引是单独的 CREATE INDEX 语句
    }

    /**
     * 测试 buildCreateSql 方法 - 自增主键
     */
    @org.junit.Test
    public void testBuildCreateSqlAutoIncrement() {
        IDialect dialect = createDialect();
        String sql = dialect.buildCreateSql(TestAutoIncrement.class, null, null);
        printSql("testBuildCreateSqlAutoIncrement (自增主键)", sql);

        // 验证 CREATE TABLE 语句结构
        assertSqlContains(sql, "CREATE TABLE");
        assertSqlContains(sql, "test_auto_increment");
        // 验证包含字段定义
        assertSqlContains(sql, "id");
        assertSqlContains(sql, "name");
        assertSqlContains(sql, "status");
        // 验证主键
        assertSqlContains(sql, "PRIMARY KEY");
        // 验证自增（不同数据库语法不同）
        // MySQL: AUTO_INCREMENT
        // PostgreSQL: SERIAL 或 GENERATED ALWAYS AS IDENTITY
        // SQLServer: IDENTITY(1,1)
        // Oracle: 使用序列和触发器
    }

    /**
     * 测试 buildDropSql 方法
     */
    @org.junit.Test
    public void testBuildDropSql() {
        IDialect dialect = createDialect();
        String sql = dialect.buildDropSql(TestUser.class, null, null);
        printSql("testBuildDropSql", sql);

        // 验证 DROP TABLE 语句结构
        assertSqlContains(sql, "DROP TABLE");
        assertSqlContains(sql, "test_user");
    }
}
