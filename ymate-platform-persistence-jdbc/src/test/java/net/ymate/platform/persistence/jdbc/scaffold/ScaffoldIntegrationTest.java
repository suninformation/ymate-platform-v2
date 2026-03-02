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
package net.ymate.platform.persistence.jdbc.scaffold;

import net.ymate.platform.core.annotation.EnableAutoScan;
import net.ymate.platform.core.annotation.EnableBeanProxy;
import net.ymate.platform.core.annotation.EnableDevMode;
import net.ymate.platform.core.beans.annotation.Inject;
import net.ymate.platform.core.persistence.base.EntityMeta;
import net.ymate.platform.persistence.jdbc.IDatabaseConnectionHolder;
import net.ymate.platform.persistence.jdbc.JDBC;
import net.ymate.platform.persistence.jdbc.query.BatchSQL;
import net.ymate.platform.persistence.jdbc.query.SQL;
import net.ymate.platform.persistence.jdbc.query.Table;
import net.ymate.platform.test.YMPJUnit4ClassRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

/**
 * Scaffold 集成测试
 *
 * @author 刘镇 (suninformation@163.com) on 2026/2/28 下午3:00
 * @since 2.1.4
 */
@RunWith(YMPJUnit4ClassRunner.class)
@EnableAutoScan
@EnableBeanProxy
@EnableDevMode
public class ScaffoldIntegrationTest {

    private static final String testTableName = "test_scaffold_table";

    @Inject
    private JDBC database;

    @Before
    public void beforeTest() throws Exception {
        // 创建测试表
        createTestTable();
    }

    private void createTestTable() throws Exception {
        System.out.println("=== 创建测试表 ===");
        EntityMeta entityMeta = EntityMeta.createAndGet(TestScaffoldTable.class);
        Table table = new Table(database.getDefaultConnectionHolder().getDialect(), entityMeta);
        String dropTableSQL = table.toDropSQL();
        String createTableSQL = table.toCreateSQL();
        System.out.println("生成的删除表 SQL: " + dropTableSQL);
        System.out.println("生成的创建表 SQL: " + createTableSQL);
        List<String> sqls = new ArrayList<>();
        sqls.add(dropTableSQL);
        sqls.add(createTableSQL);
        int effectCount = BatchSQL.execSQL(database, sqls);
        System.out.println("批量执行 SQL 影响行数: " + effectCount);
        System.out.println("创建测试表成功: " + testTableName);
        System.out.println("建表SQL: " + createTableSQL);
    }

    @After
    public void afterTest() throws Exception {
        // 删除测试表
        System.out.println("=== 删除测试表 ===");
        EntityMeta entityMeta = EntityMeta.createAndGet(TestScaffoldTable.class);
        Table table = new Table(database.getDefaultConnectionHolder().getDialect(), entityMeta);
        String dropTableSQL = table.toDropSQL();
        System.out.println("生成的删除表 SQL: " + dropTableSQL);
        int effectCount = SQL.create(database, dropTableSQL).execute();
        System.out.println("执行 SQL 影响行数: " + effectCount);
        System.out.println("删除测试表: " + testTableName);
    }

    @Test
    public void testScaffoldIntegration() throws Exception {
        System.out.println("\n=== 开始脚手架集成测试 ===");

        // 获取数据库连接持有者
        IDatabaseConnectionHolder connectionHolder = database.getDefaultConnectionHolder();

        // 1. 创建 Scaffold
        Scaffold scaffold = Scaffold.builder().build();
        System.out.println("1. 创建 Scaffold 成功");

        // 2. 获取表列表
        List<TableInfo> tables = scaffold.getTables(database, false);
        System.out.println("2. 获取表列表: " + tables.stream().map(TableInfo::getName).collect(java.util.stream.Collectors.toList()));

        // 3. 读取表信息
        TableInfo tableInfo = TableInfo.create(connectionHolder, scaffold, testTableName, false);
        Assert.assertNotNull(tableInfo);
        System.out.println("3. 读取表信息: " + tableInfo.getName());
        System.out.println("   表备注: " + tableInfo.getComment());
        System.out.println("   主键字段: " + tableInfo.getPrimaryKeys());

        // 4. 输出字段信息
        System.out.println("4. 字段信息:");
        tableInfo.getColumns().forEach((name, column) -> {
            System.out.println("   - " + name + ": " + column.getColumnType() +
                    " (autoIncrement: " + column.isAutoIncrement() + ", primaryKey: " + column.isPrimaryKey() + ", nullable: " + column.isNullable() + ", signed: " + column.isSigned() + ")");
            System.out.println("     默认值: " + column.getDefaultValue() + ", 备注: " + column.getRemarks());
        });

        // 5. 使用 Scaffold 构建实体信息
        EntityInfo entityInfo = scaffold.buildEntityInfo(tableInfo);
        System.out.println("5. 构建实体信息成功: " + entityInfo.getName());

        // 6. 基于实体信息构建 Table 对象
        Table table = new Table(connectionHolder.getDialect(), entityInfo);
        System.out.println("6. 构建 Table 对象成功");

        // 7. 生成 CREATE TABLE SQL
        String createSql = table.toCreateSQL();
        System.out.println("7. 生成 CREATE TABLE SQL:");
        System.out.println(createSql);

        // 8. 验证 SQL 包含关键字段和数据类型
        System.out.println("8. 验证 SQL 包含关键字段和数据类型:");

        // 验证表名
        Assert.assertTrue("SQL 应包含表名: " + testTableName, createSql.contains(testTableName));

        // 验证主键定义
        Assert.assertTrue("SQL 应包含主键定义", createSql.contains("PRIMARY KEY"));

        // 验证各个字段的定义（注意：字段名保持数据库原始命名）
        Assert.assertTrue("id 字段应使用 BIGINT 数据类型", createSql.contains("`id` BIGINT"));
        Assert.assertTrue("name 字段应使用 VARCHAR 数据类型", createSql.contains("`name` VARCHAR"));
        Assert.assertTrue("age 字段应使用 INTEGER 数据类型", createSql.contains("`age` INTEGER"));
        Assert.assertTrue("salary 字段应使用 NUMERIC 数据类型", createSql.contains("`salary` NUMERIC"));
        Assert.assertTrue("active 字段应使用 TINYINT 数据类型", createSql.contains("`active` TINYINT"));
        Assert.assertTrue("create_time 字段应使用 TIMESTAMP 数据类型", createSql.contains("`create_time` TIMESTAMP"));
        Assert.assertTrue("update_time 字段应使用 TIMESTAMP 数据类型", createSql.contains("`update_time` TIMESTAMP"));
        Assert.assertTrue("remark 字段应使用 VARCHAR 数据类型", createSql.contains("`remark` VARCHAR"));
        Assert.assertTrue("data 字段应使用 BINARY 数据类型", createSql.contains("`data` BINARY"));

        // 打印验证结果
        System.out.println("   包含表名: " + createSql.contains(testTableName));
        System.out.println("   包含主键: " + createSql.contains("PRIMARY KEY"));
        System.out.println("   id 字段: " + createSql.contains("`id` BIGINT"));
        System.out.println("   name 字段: " + createSql.contains("`name` VARCHAR"));
        System.out.println("   age 字段: " + createSql.contains("`age` INTEGER"));
        System.out.println("   salary 字段: " + createSql.contains("`salary` NUMERIC"));
        System.out.println("   active 字段: " + createSql.contains("`active` TINYINT"));
        System.out.println("   create_time 字段: " + createSql.contains("`create_time` TIMESTAMP"));
        System.out.println("   update_time 字段: " + createSql.contains("`update_time` TIMESTAMP"));
        System.out.println("   remark 字段: " + createSql.contains("`remark` VARCHAR"));
        System.out.println("   data 字段: " + createSql.contains("`data` BINARY"));

        // ==================== CRUD 操作示例 ====================
        System.out.println("\n=== 开始 CRUD 操作示例 ===");

        // 9. Create - 插入记录
        System.out.println("\n9. Create - 插入记录:");
        TestScaffoldTable entity = new TestScaffoldTable();
        entity.setName("测试用户");
        entity.setAge(25);
        entity.setSalary(new java.math.BigDecimal("10000.50"));
        entity.setActive(true);
        entity.setCreateTime(new java.util.Date());
        entity.setRemark("这是一个测试记录");

        TestScaffoldTable savedEntity = entity.save();
        Assert.assertNotNull("插入操作应返回实体", savedEntity);
        Assert.assertNotNull("插入后应有主键", savedEntity.getId());
        System.out.println("   插入成功，ID: " + savedEntity.getId() + ", 名称: " + savedEntity.getName());

        // 10. Read - 查询记录
        System.out.println("\n10. Read - 查询记录:");
        TestScaffoldTable queryEntity = new TestScaffoldTable();
        queryEntity.setId(savedEntity.getId());
        TestScaffoldTable loadedEntity = queryEntity.load();
        Assert.assertNotNull("查询应返回实体", loadedEntity);
        Assert.assertEquals("名称应一致", "测试用户", loadedEntity.getName());
        Assert.assertEquals("年龄应一致", Integer.valueOf(25), loadedEntity.getAge());
        System.out.println("   查询成功，ID: " + loadedEntity.getId() + ", 名称: " + loadedEntity.getName() + ", 年龄: " + loadedEntity.getAge());

        // 11. Update - 更新记录
        System.out.println("\n11. Update - 更新记录:");
        loadedEntity.setAge(30);
        loadedEntity.setSalary(new java.math.BigDecimal("15000.00"));
        loadedEntity.setUpdateTime(new java.util.Date());
        TestScaffoldTable updatedEntity = loadedEntity.update();
        Assert.assertNotNull("更新操作应返回实体", updatedEntity);
        Assert.assertEquals("年龄应已更新", Integer.valueOf(30), updatedEntity.getAge());
        System.out.println("   更新成功，ID: " + updatedEntity.getId() + ", 新年龄: " + updatedEntity.getAge() + ", 新薪资: " + updatedEntity.getSalary());

        // 12. 再次查询验证更新
        System.out.println("\n12. 再次查询验证更新:");
        TestScaffoldTable verifyEntity = new TestScaffoldTable();
        verifyEntity.setId(savedEntity.getId());
        TestScaffoldTable verifiedEntity = verifyEntity.load();
        Assert.assertEquals("年龄应已持久化", Integer.valueOf(30), verifiedEntity.getAge());
        System.out.println("   验证成功，年龄: " + verifiedEntity.getAge());

        // 13. 测试 saveOrUpdate 方法
        System.out.println("\n13. 测试 saveOrUpdate 方法:");
        TestScaffoldTable upsertEntity = new TestScaffoldTable();
        upsertEntity.setId(savedEntity.getId());
        upsertEntity.setName("更新后的用户");
        upsertEntity.setAge(35);
        upsertEntity.setSalary(new java.math.BigDecimal("20000.00"));
        upsertEntity.setActive(false);
        upsertEntity.setCreateTime(verifiedEntity.getCreateTime());
        TestScaffoldTable upsertResult = upsertEntity.saveOrUpdate();
        Assert.assertNotNull("saveOrUpdate 应返回实体", upsertResult);
        Assert.assertEquals("名称应已更新", "更新后的用户", upsertResult.getName());
        System.out.println("   saveOrUpdate 成功，名称: " + upsertResult.getName() + ", 年龄: " + upsertResult.getAge());

        // 14. 测试 saveIfNotExist 方法
        System.out.println("\n14. 测试 saveIfNotExist 方法:");
        TestScaffoldTable newEntity = new TestScaffoldTable();
        newEntity.setName("新用户");
        newEntity.setAge(28);
        newEntity.setSalary(new java.math.BigDecimal("8000.00"));
        newEntity.setActive(true);
        newEntity.setCreateTime(new java.util.Date());
        boolean insertResult = newEntity.saveIfNotExist();
        Assert.assertTrue("新记录插入应成功", insertResult);
        System.out.println("   saveIfNotExist 新记录成功，ID: " + newEntity.getId());

        // 注意：由于 TestScaffoldTable 使用自增主键，saveIfNotExist 不会在 INSERT 中包含 ID 字段
        // 因此无法通过指定已存在的 ID 来测试重复插入的场景
        // 对于自增主键实体，saveIfNotExist 主要用于插入新记录，避免唯一索引冲突
        System.out.println("   注意：自增主键实体的 saveIfNotExist 主要用于避免唯一索引冲突");

        // 15. 测试 exist 方法
        System.out.println("\n15. 测试 exist 方法:");
        TestScaffoldTable existCheck = new TestScaffoldTable();
        existCheck.setId(savedEntity.getId());
        boolean exists = existCheck.exist();
        Assert.assertTrue("记录应存在", exists);
        System.out.println("   记录存在检查: " + exists);

        // 16. 测试 find 方法
        System.out.println("\n16. 测试 find 方法:");
        TestScaffoldTable findEntity = new TestScaffoldTable();
        findEntity.setActive(true);
        net.ymate.platform.core.persistence.IResultSet<TestScaffoldTable> resultSet = findEntity.find();
        Assert.assertNotNull("查询结果不应为空", resultSet);
        System.out.println("   查询到 " + resultSet.getRecordCount() + " 条活跃记录");

        // 17. Delete - 删除记录
        System.out.println("\n17. Delete - 删除记录:");
        TestScaffoldTable deleteEntity = new TestScaffoldTable();
        deleteEntity.setId(savedEntity.getId());
        int deleteCount = deleteEntity.delete();
        Assert.assertEquals("应删除一条记录", 1, deleteCount);
        System.out.println("   删除成功，影响行数: " + deleteCount);

        // 删除第二条记录
        TestScaffoldTable deleteEntity2 = new TestScaffoldTable();
        deleteEntity2.setId(newEntity.getId());
        int deleteCount2 = deleteEntity2.delete();
        Assert.assertEquals("应删除一条记录", 1, deleteCount2);
        System.out.println("   删除第二条记录成功，影响行数: " + deleteCount2);

        // 18. 验证删除
        System.out.println("\n18. 验证删除:");
        TestScaffoldTable deletedCheck = new TestScaffoldTable();
        deletedCheck.setId(savedEntity.getId());
        boolean existsAfterDelete = deletedCheck.exist();
        Assert.assertFalse("删除后记录不应存在", existsAfterDelete);
        System.out.println("   删除后记录存在检查: " + existsAfterDelete);

        System.out.println("\n=== CRUD 操作示例完成 ===");

        System.out.println("\n=== 脚手架集成测试完成 ===");
    }


}
