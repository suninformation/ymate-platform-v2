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
package net.ymate.platform.persistence.jdbc.dialect.impl;

import net.ymate.platform.core.annotation.EnableAutoScan;
import net.ymate.platform.core.annotation.EnableBeanProxy;
import net.ymate.platform.core.annotation.EnableDevMode;
import net.ymate.platform.core.persistence.Fields;
import net.ymate.platform.persistence.jdbc.dialect.DialectTestBase;
import net.ymate.platform.persistence.jdbc.dialect.IDialect;
import net.ymate.platform.test.YMPJUnit4ClassRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * MySQLDialect 单元测试
 *
 * @author 刘镇 (suninformation@163.com) on 2026/2/24 上午10:00
 * @since 2.1.4
 */
@RunWith(YMPJUnit4ClassRunner.class)
@EnableAutoScan
@EnableBeanProxy
@EnableDevMode
public class MySQLDialectTest extends DialectTestBase {

    @Override
    protected IDialect createDialect() {
        return new MySQLDialect();
    }

    @Test
    public void testGetName() {
        IDialect dialect = createDialect();
        // Type.DATABASE.MYSQL 的值是 "MYSQL"（大写）
        org.junit.Assert.assertEquals("MYSQL", dialect.getName());
    }

    @Test
    public void testBuildPagedQuerySql() {
        IDialect dialect = createDialect();
        String originSql = "SELECT * FROM test_user";

        // 测试分页查询 - MySQL 使用 LIMIT offset, count 格式
        String pagedSql = dialect.buildPagedQuerySql(originSql, 2, 10);
        printSql("testBuildPagedQuerySql (page=2, pageSize=10)", pagedSql);
        assertSqlEquals(pagedSql, "SELECT * FROM test_user LIMIT 10, 10");

        // 测试第一页
        pagedSql = dialect.buildPagedQuerySql(originSql, 1, 20);
        printSql("testBuildPagedQuerySql (page=1, pageSize=20)", pagedSql);
        assertSqlEquals(pagedSql, "SELECT * FROM test_user LIMIT 0, 20");
    }

    @Test
    public void testBuildInsertSql() {
        IDialect dialect = createDialect();
        String sql = dialect.buildInsertSql(TestUser.class, null, null, null);
        printSql("testBuildInsertSql", sql);

        // 验证 INSERT 语句结构
        assertSqlContains(sql, "INSERT INTO");
        assertSqlContains(sql, "`test_user`");
        assertSqlContains(sql, "`id`");
        assertSqlContains(sql, "`name`");
        assertSqlContains(sql, "`age`");
        assertSqlContains(sql, "`email`");
        assertSqlContains(sql, "`create_time`");
        assertSqlContains(sql, "VALUES");
    }

    @Test
    public void testBuildUpsertSql() {
        IDialect dialect = createDialect();

        // 测试单主键实体的 upsert
        String sql = dialect.buildUpsertSql(TestUser.class, null, null, null);
        printSql("testBuildUpsertSql (单主键)", sql);

        // 验证基本结构
        assertSqlContains(sql, "INSERT INTO");
        assertSqlContains(sql, "`test_user`");
        assertSqlContains(sql, "ON DUPLICATE KEY UPDATE");

        // 验证更新部分包含非主键字段
        assertSqlContains(sql, "`name` = VALUES(`name`)");
        assertSqlContains(sql, "`age` = VALUES(`age`)");
        assertSqlContains(sql, "`email` = VALUES(`email`)");
        assertSqlContains(sql, "`create_time` = VALUES(`create_time`)");

        // 验证不包含主键在 UPDATE 部分
        org.junit.Assert.assertFalse("PK should not be in UPDATE clause",
                sql.contains("`id` = VALUES(`id`)"));
    }

    @Test
    public void testBuildUpsertSqlWithFields() {
        IDialect dialect = createDialect();

        // 测试指定字段 - 注意：当指定字段时，只使用指定字段，不会自动添加主键
        Fields fields = Fields.create("name", "age");
        String sql = dialect.buildUpsertSql(TestUser.class, null, null, fields);
        printSql("testBuildUpsertSqlWithFields (指定字段 name, age)", sql);

        // 验证只包含指定字段
        assertSqlContains(sql, "`name`");
        assertSqlContains(sql, "`age`");
        // email 和 create_time 不在指定字段中，不应该出现在 SQL 中
        org.junit.Assert.assertFalse("email should not be in SQL",
                sql.contains("`email`"));
        org.junit.Assert.assertFalse("create_time should not be in SQL",
                sql.contains("`create_time`"));
    }

    @Test
    public void testBuildUpsertSqlCompositeKey() {
        IDialect dialect = createDialect();

        // 测试复合主键实体的 upsert
        String sql = dialect.buildUpsertSql(TestOrderItem.class, null, null, null);
        printSql("testBuildUpsertSqlCompositeKey (复合主键)", sql);

        // 验证基本结构
        assertSqlContains(sql, "INSERT INTO");
        assertSqlContains(sql, "`test_order_item`");
        assertSqlContains(sql, "ON DUPLICATE KEY UPDATE");

        // 验证包含所有主键（注意：字段名会被转换为下划线命名）
        assertSqlContains(sql, "`order_id`");
        assertSqlContains(sql, "`product_id`");

        // 验证更新部分包含非主键字段
        assertSqlContains(sql, "`quantity` = VALUES(`quantity`)");
        assertSqlContains(sql, "`price` = VALUES(`price`)");
    }

    @Test
    public void testBuildInsertIfNotExistSql() {
        IDialect dialect = createDialect();

        // 测试单主键实体的 insert if not exist
        String sql = dialect.buildInsertIfNotExistSql(TestUser.class, null, null, null);
        printSql("testBuildInsertIfNotExistSql (单主键)", sql);

        // 验证使用 INSERT IGNORE
        assertSqlContains(sql, "INSERT IGNORE INTO");
        assertSqlContains(sql, "`test_user`");
        assertSqlContains(sql, "`id`");
        assertSqlContains(sql, "`name`");
        assertSqlContains(sql, "`age`");
        assertSqlContains(sql, "`email`");
    }

    @Test
    public void testBuildInsertIfNotExistSqlWithFields() {
        IDialect dialect = createDialect();

        // 测试指定字段 - 注意：当指定字段时，只使用指定字段
        Fields fields = Fields.create("name", "email");
        String sql = dialect.buildInsertIfNotExistSql(TestUser.class, null, null, fields);
        printSql("testBuildInsertIfNotExistSqlWithFields (指定字段 name, email)", sql);

        // 验证只包含指定字段
        assertSqlContains(sql, "`name`");
        assertSqlContains(sql, "`email`");
        // age 不在指定字段中
        org.junit.Assert.assertFalse("age should not be in SQL",
                sql.contains("`age`"));
    }

    @Test
    public void testBuildInsertIfNotExistSqlCompositeKey() {
        IDialect dialect = createDialect();

        // 测试复合主键实体的 insert if not exist
        String sql = dialect.buildInsertIfNotExistSql(TestOrderItem.class, null, null, null);
        printSql("testBuildInsertIfNotExistSqlCompositeKey (复合主键)", sql);

        // 验证使用 INSERT IGNORE
        assertSqlContains(sql, "INSERT IGNORE INTO");
        assertSqlContains(sql, "`test_order_item`");

        // 验证包含所有主键（注意：字段名会被转换为下划线命名）
        assertSqlContains(sql, "`order_id`");
        assertSqlContains(sql, "`product_id`");

        // 验证包含非主键字段
        assertSqlContains(sql, "`quantity`");
        assertSqlContains(sql, "`price`");
    }

    @Test
    public void testWrapIdentifierQuote() {
        IDialect dialect = createDialect();

        // 测试标识符引用
        org.junit.Assert.assertEquals("`test`", dialect.wrapIdentifierQuote("test"));
        org.junit.Assert.assertEquals("`user_name`", dialect.wrapIdentifierQuote("user_name"));
    }
}
