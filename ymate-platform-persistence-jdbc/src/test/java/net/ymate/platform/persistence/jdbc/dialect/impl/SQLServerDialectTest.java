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
 * SQLServerDialect 单元测试
 *
 * @author 刘镇 (suninformation@163.com) on 2026/2/24 上午10:00
 * @since 2.1.4
 */
@RunWith(YMPJUnit4ClassRunner.class)
@EnableAutoScan
@EnableBeanProxy
@EnableDevMode
public class SQLServerDialectTest extends DialectTestBase {

    @Override
    protected IDialect createDialect() {
        return new SQLServerDialect();
    }

    @Test
    public void testGetName() {
        IDialect dialect = createDialect();
        // Type.DATABASE.SQLSERVER 的值是 "SQLSERVER"（大写）
        org.junit.Assert.assertEquals("SQLSERVER", dialect.getName());
    }

    @Test
    public void testBuildPagedQuerySql() {
        IDialect dialect = createDialect();
        String originSql = "SELECT * FROM test_user";

        // 测试分页查询
        String pagedSql = dialect.buildPagedQuerySql(originSql, 2, 10);
        printSql("testBuildPagedQuerySql (page=2, pageSize=10)", pagedSql);
        assertSqlContains(pagedSql, "SELECT * FROM");
        assertSqlContains(pagedSql, "ROW_NUMBER()");
        assertSqlContains(pagedSql, "__rn__ > 10");
        // SQLServer 使用 TOP 限制上限
        assertSqlContains(pagedSql, "TOP 20");

        // 测试带 DISTINCT 的查询
        pagedSql = dialect.buildPagedQuerySql("SELECT DISTINCT * FROM test_user", 1, 10);
        printSql("testBuildPagedQuerySql (DISTINCT, page=1, pageSize=10)", pagedSql);
        assertSqlContains(pagedSql, "DISTINCT");
    }

    @Test
    public void testBuildInsertSql() {
        IDialect dialect = createDialect();
        String sql = dialect.buildInsertSql(TestUser.class, null, null, null);
        printSql("testBuildInsertSql", sql);

        // 验证 INSERT 语句结构
        assertSqlContains(sql, "INSERT INTO");
        assertSqlContains(sql, "[test_user]");
        assertSqlContains(sql, "[id]");
        assertSqlContains(sql, "[name]");
        assertSqlContains(sql, "[age]");
        assertSqlContains(sql, "[email]");
        assertSqlContains(sql, "VALUES");
    }

    @Test
    public void testBuildUpsertSql() {
        IDialect dialect = createDialect();

        // 测试单主键实体的 upsert
        String sql = dialect.buildUpsertSql(TestUser.class, null, null, null);
        printSql("testBuildUpsertSql (单主键)", sql);

        // 验证基本结构
        assertSqlContains(sql, "MERGE INTO");
        assertSqlContains(sql, "[test_user]");
        assertSqlContains(sql, "AS target");
        assertSqlContains(sql, "USING");
        assertSqlContains(sql, "AS source");
        assertSqlContains(sql, "ON");
        assertSqlContains(sql, "WHEN MATCHED THEN UPDATE SET");
        assertSqlContains(sql, "WHEN NOT MATCHED THEN INSERT");

        // 验证 ON 条件
        assertSqlContains(sql, "target.[id] = source.[id]");

        // 验证 UPDATE SET 部分包含非主键字段
        assertSqlContains(sql, "[name] = source.[name]");
        assertSqlContains(sql, "[age] = source.[age]");
        assertSqlContains(sql, "[email] = source.[email]");
        assertSqlContains(sql, "[create_time] = source.[create_time]");

        // 验证 INSERT 部分
        assertSqlContains(sql, "INSERT ([id], [name], [age], [email], [create_time])");
        assertSqlContains(sql, "VALUES (source.[id], source.[name], source.[age], source.[email], source.[create_time])");
    }

    @Test
    public void testBuildUpsertSqlWithFields() {
        IDialect dialect = createDialect();

        // 测试指定字段 - 注意：当指定字段时，只使用指定字段
        Fields fields = Fields.create("name", "age");
        String sql = dialect.buildUpsertSql(TestUser.class, null, null, fields);
        printSql("testBuildUpsertSqlWithFields (指定字段 name, age)", sql);

        // 验证只包含指定字段
        assertSqlContains(sql, "[name]");
        assertSqlContains(sql, "[age]");
        // email 不在指定字段中
        org.junit.Assert.assertFalse("email should not be in SQL",
                sql.contains("[email]"));
    }

    @Test
    public void testBuildUpsertSqlCompositeKey() {
        IDialect dialect = createDialect();

        // 测试复合主键实体的 upsert
        String sql = dialect.buildUpsertSql(TestOrderItem.class, null, null, null);
        printSql("testBuildUpsertSqlCompositeKey (复合主键)", sql);

        // 验证基本结构
        assertSqlContains(sql, "MERGE INTO");
        assertSqlContains(sql, "[test_order_item]");

        // 验证 ON 条件包含所有主键（注意：字段名会被转换为下划线命名）
        assertSqlContains(sql, "target.[order_id] = source.[order_id]");

        // 验证 UPDATE SET 部分包含非主键字段
        assertSqlContains(sql, "[quantity] = source.[quantity]");
        assertSqlContains(sql, "[price] = source.[price]");
    }

    @Test
    public void testBuildInsertIfNotExistSql() {
        IDialect dialect = createDialect();

        // 测试单主键实体的 insert if not exist
        String sql = dialect.buildInsertIfNotExistSql(TestUser.class, null, null, null);
        printSql("testBuildInsertIfNotExistSql (单主键)", sql);

        // 验证使用 MERGE INTO ... WHEN NOT MATCHED
        assertSqlContains(sql, "MERGE INTO");
        assertSqlContains(sql, "[test_user]");
        assertSqlContains(sql, "AS target");
        assertSqlContains(sql, "USING");
        assertSqlContains(sql, "AS source");
        assertSqlContains(sql, "ON");
        assertSqlContains(sql, "WHEN NOT MATCHED THEN INSERT");

        // 验证不包含 WHEN MATCHED THEN UPDATE
        org.junit.Assert.assertFalse("Should not have WHEN MATCHED THEN UPDATE",
                sql.contains("WHEN MATCHED THEN UPDATE"));

        // 验证 ON 条件
        assertSqlContains(sql, "target.[id] = source.[id]");

        // 验证 INSERT 部分
        assertSqlContains(sql, "INSERT ([id], [name], [age], [email], [create_time])");
        assertSqlContains(sql, "VALUES (source.[id], source.[name], source.[age], source.[email], source.[create_time])");
    }

    @Test
    public void testBuildInsertIfNotExistSqlWithFields() {
        IDialect dialect = createDialect();

        // 测试指定字段 - 注意：当指定字段时，只使用指定字段
        Fields fields = Fields.create("name", "email");
        String sql = dialect.buildInsertIfNotExistSql(TestUser.class, null, null, fields);
        printSql("testBuildInsertIfNotExistSqlWithFields (指定字段 name, email)", sql);

        // 验证只包含指定字段
        assertSqlContains(sql, "[name]");
        assertSqlContains(sql, "[email]");
        // age 不在指定字段中
        org.junit.Assert.assertFalse("age should not be in SQL",
                sql.contains("[age]"));
    }

    @Test
    public void testBuildInsertIfNotExistSqlCompositeKey() {
        IDialect dialect = createDialect();

        // 测试复合主键实体的 insert if not exist
        String sql = dialect.buildInsertIfNotExistSql(TestOrderItem.class, null, null, null);
        printSql("testBuildInsertIfNotExistSqlCompositeKey (复合主键)", sql);

        // 验证使用 MERGE INTO
        assertSqlContains(sql, "MERGE INTO");
        assertSqlContains(sql, "[test_order_item]");

        // 验证 ON 条件包含所有主键（注意：字段名会被转换为下划线命名）
        assertSqlContains(sql, "target.[order_id] = source.[order_id]");

        // 验证不包含 WHEN MATCHED THEN UPDATE
        org.junit.Assert.assertFalse("Should not have WHEN MATCHED THEN UPDATE",
                sql.contains("WHEN MATCHED THEN UPDATE"));

        // 验证包含非主键字段
        assertSqlContains(sql, "[quantity]");
        assertSqlContains(sql, "[price]");
    }

    @Test
    public void testWrapIdentifierQuote() {
        IDialect dialect = createDialect();

        // 测试标识符引用（SQLServer 使用方括号）
        org.junit.Assert.assertEquals("[test]", dialect.wrapIdentifierQuote("test"));
        org.junit.Assert.assertEquals("[user_name]", dialect.wrapIdentifierQuote("user_name"));
    }
}
