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
package net.ymate.platform.persistence.jdbc.support;

import net.ymate.platform.core.annotation.EnableAutoScan;
import net.ymate.platform.core.annotation.EnableBeanProxy;
import net.ymate.platform.core.annotation.EnableDevMode;
import net.ymate.platform.core.beans.annotation.Inject;
import net.ymate.platform.core.persistence.annotation.Entity;
import net.ymate.platform.core.persistence.annotation.Id;
import net.ymate.platform.core.persistence.annotation.Property;
import net.ymate.platform.core.persistence.base.EntityMeta;
import net.ymate.platform.persistence.jdbc.IDatabaseSession;
import net.ymate.platform.persistence.jdbc.JDBC;
import net.ymate.platform.persistence.jdbc.impl.DefaultDatabaseSession;
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
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 原子操作测试
 *
 * @author 刘镇 (suninformation@163.com) on 2026/2/28 下午4:00
 * @since 2.1.4
 */
@RunWith(YMPJUnit4ClassRunner.class)
@EnableAutoScan
@EnableBeanProxy
@EnableDevMode
public class AtomicOperationsTest {

    private static final String TEST_TABLE_NAME = "test_atomic_operations";

    @Inject
    private JDBC jdbc;

    @Before
    public void beforeTest() throws Exception {
        EntityMeta testEntityMeta = EntityMeta.createAndGet(TestEntity.class);
        // 创建测试表
        Table table = new Table(jdbc.getDefaultConnectionHolder().getDialect(), testEntityMeta);
        // 生成删除表和创建表的 SQL 语句
        String dropTableSQL = table.toDropSQL();
        String createTableSQL = table.toCreateSQL();
        System.out.println("生成的删除表 SQL: " + dropTableSQL);
        System.out.println("生成的创建表 SQL: " + createTableSQL);
        // 通过 BatchSQL 批量执行 SQL 语句
        int effectCount = BatchSQL.execSQL(jdbc, Arrays.asList(dropTableSQL, createTableSQL));
        System.out.println("批量执行 SQL 影响行数: " + effectCount);
    }

    @After
    public void afterTest() throws Exception {
        // 删除测试表
        Table table = new Table(jdbc.getDefaultConnectionHolder().getDialect(), EntityMeta.createAndGet(TestEntity.class));
        String dropTableSQL = table.toDropSQL();
        int effectCount = SQL.create(jdbc, dropTableSQL).execute();
        System.out.println("执行 SQL 影响行数: " + effectCount);
    }

    @Test
    public void testBaseEntityAtomicOperations() throws Exception {
        System.out.println("=== 测试 BaseEntity 原子操作 ===");

        // 测试 saveIfNotExist
        TestEntity entity1 = new TestEntity();
        entity1.setId(1L);
        entity1.setName("Test 1");
        entity1.setValue(100);
        entity1.setCreateTime(new Date());

        boolean saved1 = entity1.saveIfNotExist();
        System.out.println("saveIfNotExist 第一次执行: " + saved1);
        Assert.assertTrue("第一次 saveIfNotExist 应返回 true", saved1);

        // 再次执行，应该返回 false
        boolean saved2 = entity1.saveIfNotExist();
        System.out.println("saveIfNotExist 第二次执行: " + saved2);
        Assert.assertFalse("第二次 saveIfNotExist 应返回 false", saved2);

        // 测试 saveOrUpdate
        TestEntity entity2 = new TestEntity();
        entity2.setId(2L);
        entity2.setName("Test 2");
        entity2.setValue(200);
        entity2.setCreateTime(new Date());

        TestEntity result1 = (TestEntity) entity2.saveOrUpdate();
        System.out.println("saveOrUpdate 插入操作: " + result1);
        Assert.assertNotNull("saveOrUpdate 插入操作应返回实体", result1);

        // 更新操作
        entity2.setValue(250);
        TestEntity result2 = (TestEntity) entity2.saveOrUpdate();
        System.out.println("saveOrUpdate 更新操作: " + result2);
        Assert.assertNotNull("saveOrUpdate 更新操作应返回实体", result2);
        Assert.assertEquals("saveOrUpdate 应更新值为 250", 250, result2.getValue().intValue());
    }

    @Test
    public void testEntityWrapperAtomicOperations() throws Exception {
        System.out.println("\n=== 测试 EntityWrapper 原子操作 ===");

        // 测试 saveIfNotExist
        TestEntity entity1 = new TestEntity();
        entity1.setId(3L);
        entity1.setName("Test 3");
        entity1.setValue(300);
        entity1.setCreateTime(new Date());

        EntityWrapper<TestEntity> wrapper1 = EntityWrapper.bind(entity1);
        boolean saved1 = wrapper1.saveIfNotExist(null);
        System.out.println("saveIfNotExist 第一次执行: " + saved1);
        Assert.assertTrue("第一次 saveIfNotExist 应返回 true", saved1);

        // 再次执行，应该返回 false
        boolean saved2 = wrapper1.saveIfNotExist(null);
        System.out.println("saveIfNotExist 第二次执行: " + saved2);
        Assert.assertFalse("第二次 saveIfNotExist 应返回 false", saved2);

        // 测试 saveOrUpdate
        TestEntity entity2 = new TestEntity();
        entity2.setId(4L);
        entity2.setName("Test 4");
        entity2.setValue(400);
        entity2.setCreateTime(new Date());

        EntityWrapper<TestEntity> wrapper2 = EntityWrapper.bind(entity2);
        TestEntity result1 = (TestEntity) wrapper2.saveOrUpdate(null);
        System.out.println("saveOrUpdate 插入操作: " + result1);
        Assert.assertNotNull("saveOrUpdate 插入操作应返回实体", result1);

        // 更新操作
        entity2.setValue(450);
        TestEntity result2 = (TestEntity) wrapper2.saveOrUpdate(null);
        System.out.println("saveOrUpdate 更新操作: " + result2);
        Assert.assertNotNull("saveOrUpdate 更新操作应返回实体", result2);
        Assert.assertEquals("saveOrUpdate 应更新值为 450", 450, result2.getValue().intValue());
    }

    @Test
    public void testDatabaseSessionAtomicOperations() throws Exception {
        System.out.println("\n=== 测试 IDatabaseSession 原子操作 ===");

        try (IDatabaseSession session = new DefaultDatabaseSession(jdbc, jdbc.getDefaultConnectionHolder())) {
            // 测试单个实体的 upsert 操作
            TestEntity entity1 = new TestEntity();
            entity1.setId(5L);
            entity1.setName("Test 5");
            entity1.setValue(500);
            entity1.setCreateTime(new Date());

            TestEntity upsertResult1 = session.upsert(entity1);
            System.out.println("单个实体 upsert 操作: " + upsertResult1);
            Assert.assertNotNull("单个实体 upsert 操作应返回实体", upsertResult1);

            // 测试批量 upsert 操作
            List<TestEntity> batchUpsertEntities = new ArrayList<>();
            TestEntity entity2 = new TestEntity();
            entity2.setId(6L);
            entity2.setName("Test 6");
            entity2.setValue(600);
            entity2.setCreateTime(new Date());
            batchUpsertEntities.add(entity2);

            TestEntity entity3 = new TestEntity();
            entity3.setId(7L);
            entity3.setName("Test 7");
            entity3.setValue(700);
            entity3.setCreateTime(new Date());
            batchUpsertEntities.add(entity3);

            List<TestEntity> batchUpsertResults = session.upsert(batchUpsertEntities);
            System.out.println("批量 upsert 操作结果数量: " + batchUpsertResults.size());
            Assert.assertEquals("批量 upsert 操作应返回 2 个实体", 2, batchUpsertResults.size());

            // 测试单个实体的 insertIfNotExist 操作
            TestEntity entity4 = new TestEntity();
            entity4.setId(8L);
            entity4.setName("Test 8");
            entity4.setValue(800);
            entity4.setCreateTime(new Date());

            TestEntity insertIfNotExistResult1 = session.insertIfNotExist(entity4);
            System.out.println("单个实体 insertIfNotExist 操作: " + insertIfNotExistResult1);
            Assert.assertNotNull("单个实体 insertIfNotExist 操作应返回实体", insertIfNotExistResult1);

            // 再次执行 insertIfNotExist，应该返回 null
            TestEntity insertIfNotExistResult2 = session.insertIfNotExist(entity4);
            System.out.println("单个实体 insertIfNotExist 操作（重复）: " + insertIfNotExistResult2);
            Assert.assertNull("重复的 insertIfNotExist 操作应返回 null", insertIfNotExistResult2);

            // 测试批量 insertIfNotExist 操作
            List<TestEntity> batchInsertIfNotExistEntities = new ArrayList<>();
            TestEntity entity5 = new TestEntity();
            entity5.setId(9L);
            entity5.setName("Test 9");
            entity5.setValue(900);
            entity5.setCreateTime(new Date());
            batchInsertIfNotExistEntities.add(entity5);

            TestEntity entity6 = new TestEntity();
            entity6.setId(10L);
            entity6.setName("Test 10");
            entity6.setValue(1000);
            entity6.setCreateTime(new Date());
            batchInsertIfNotExistEntities.add(entity6);

            List<TestEntity> batchInsertIfNotExistResults = session.insertIfNotExist(batchInsertIfNotExistEntities);
            System.out.println("批量 insertIfNotExist 操作结果数量: " + batchInsertIfNotExistResults.size());
            Assert.assertEquals("批量 insertIfNotExist 操作应返回 2 个实体", 2, batchInsertIfNotExistResults.size());
        }
    }

    @Entity(TEST_TABLE_NAME)
    public static class TestEntity extends BaseEntity<TestEntity, Long> {

        @Id
        @Property(name = "id")
        private Long id;

        @Property(name = "name")
        private String name;

        @Property(name = "value")
        private Integer value;

        @Property(name = "create_time")
        private Date createTime;

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

        public Integer getValue() {
            return value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }

        public Date getCreateTime() {
            return createTime;
        }

        public void setCreateTime(Date createTime) {
            this.createTime = createTime;
        }

        @Override
        public String toString() {
            return "TestEntity{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", value=" + value +
                    ", createTime=" + createTime +
                    '}';
        }
    }
}
