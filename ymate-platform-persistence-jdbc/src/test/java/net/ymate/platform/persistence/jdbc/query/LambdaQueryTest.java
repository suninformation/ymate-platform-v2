/*
 * Copyright 2007-2025 the original author or authors.
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

import net.ymate.platform.core.annotation.EnableAutoScan;
import net.ymate.platform.core.annotation.EnableBeanProxy;
import net.ymate.platform.core.annotation.EnableDevMode;
import net.ymate.platform.core.persistence.annotation.Entity;
import net.ymate.platform.core.persistence.annotation.Id;
import net.ymate.platform.core.persistence.annotation.Property;
import net.ymate.platform.core.persistence.base.IEntity;
import net.ymate.platform.test.YMPJUnit4ClassRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

/**
 * Lambda Query 功能测试类
 *
 * @author 刘镇 (suninformation@163.com) on 2025/12/22 下午2:00
 * @since 2.1.4
 */
@RunWith(YMPJUnit4ClassRunner.class)
@EnableAutoScan
@EnableBeanProxy
@EnableDevMode
public class LambdaQueryTest {

    // 测试实体类定义
    @Entity
    public static class User implements IEntity<String> {

        @Id
        @Property
        private String id;

        @Property
        private String name;

        @Property
        private String username;

        @Property
        private Integer age;

        @Property
        private String email;

        @Property
        private Date createTime;

        @Property
        private Integer status;

        @Property
        private String deptId;

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

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
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

        public Date getCreateTime() {
            return createTime;
        }

        public void setCreateTime(Date createTime) {
            this.createTime = createTime;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }

        public String getDeptId() {
            return deptId;
        }

        public void setDeptId(String deptId) {
            this.deptId = deptId;
        }
    }

    @Entity
    public static class Department implements IEntity<String> {

        @Id
        @Property
        private String id;

        @Property
        private String deptName;

        @Property
        private String parentId;

        @Property
        private Integer sortOrder;

        @Override
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getDeptName() {
            return deptName;
        }

        public void setDeptName(String deptName) {
            this.deptName = deptName;
        }

        public String getParentId() {
            return parentId;
        }

        public void setParentId(String parentId) {
            this.parentId = parentId;
        }

        public Integer getSortOrder() {
            return sortOrder;
        }

        public void setSortOrder(Integer sortOrder) {
            this.sortOrder = sortOrder;
        }
    }

    @Entity
    public static class Order implements IEntity<String> {

        @Id
        @Property
        private String id;

        @Property
        private String userId;

        @Property
        private Double amount;

        @Property
        private Date orderTime;

        @Override
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public Double getAmount() {
            return amount;
        }

        public void setAmount(Double amount) {
            this.amount = amount;
        }

        public Date getOrderTime() {
            return orderTime;
        }

        public void setOrderTime(Date orderTime) {
            this.orderTime = orderTime;
        }
    }

    @Test
    public void testBasicJoin() {
        // 基本内连接 - 使用Lambda表达式
        Select select = Select.create()
                .field(User::getUsername, "uname")
                .field(Department::getDeptName, "dept")
                .from(User.class, "u")
                .innerJoin(Department.class, "d", User::getDeptId, Department::getId);
        System.out.println("Basic Inner Join: " + select);
        System.out.println("Params: " + select.params().params());
        System.out.println();

        // 左连接 - 使用Lambda表达式
        select = Select.create()
                .field(User::getUsername, "uname")
                .field(Department::getDeptName, "dept")
                .from(User.class, "u")
                .leftJoin(Department.class, "d", User::getDeptId, Department::getId);
        System.out.println("Basic Left Join: " + select);
        System.out.println("Params: " + select.params().params());
        System.out.println();
    }

    @Test
    public void testJoinWithAliases() {
        // 带别名的内连接 - 使用Lambda表达式
        Select select = Select.create()
                .field(User::getUsername, "user_name")
                .field(Department::getDeptName, "dept_name")
                .from(User.class, "u")
                .innerJoin(Department.class, "d", "u", User::getId, "d", Department::getParentId);
        System.out.println("Inner Join with Aliases: " + select);
        System.out.println("Params: " + select.params().params());
        System.out.println();
    }

    @Test
    public void testDifferentComparisonOperators() {
        // 使用不同比较运算符的连接
        Select select = Select.create()
                .field(User::getUsername)
                .field(Order::getAmount)
                .from(User.class, "u")
                .innerJoin(Order.class, "o", "u", User::getId, "o", Order::getUserId);
        System.out.println("Inner Join with EQ: " + select);
        System.out.println("Params: " + select.params().params());
        System.out.println();
    }

    @Test
    public void testMultiTableJoin() {
        // 多表连接 - 用户、部门、订单
        Select select = Select.create()
                .field(User::getUsername)
                .field(Department::getDeptName)
                .field(Order::getAmount)
                .from(User.class, "u")
                .leftJoin(Department.class, "d", "a", User::getDeptId, "u", Department::getId)
                .innerJoin(Order.class, "o", "u", User::getId, "o", Order::getUserId);
        System.out.println("Multi-table Join: " + select);
        System.out.println("Params: " + select.params().params());
        System.out.println();
    }

    @Test
    public void testComplexJoinCondition() {
        // 复杂连接条件 - 使用条件构建器
        Select select = Select.create()
                .field(User::getUsername)
                .field(Order::getAmount)
                .from(User.class, "u")
                .innerJoin(Order.class, "o", Cond.create()
                        .eq("u", User::getId, "o", Order::getUserId)
                        .and().gt("o", Order::getAmount, 100.0)
                        .and().eq("u", User::getStatus, 1));
        System.out.println("Complex Join Condition: " + select);
        System.out.println("Params: " + select.params().params());
        System.out.println();
    }

    @Test
    public void testJoinWithCondLambda() {
        // 使用Cond的Lambda方法构建连接条件
        Select select = Select.create()
                .field(User::getUsername, "uname")
                .field(Department::getDeptName, "dname")
                .from(User.class, "u")
                .innerJoin(Department.class, "d", "u", User::getId, "d", Department::getParentId);
        System.out.println("Join with Cond Lambda: " + select);
        System.out.println("Params: " + select.params().params());
        System.out.println();
    }

    @Test
    public void testJoinWithDifferentOperators() {
        // 测试不同连接操作符
        Select select = Select.create()
                .field(User::getUsername)
                .field(Department::getDeptName)
                .from(User.class, "u")
                .leftJoin(Department.class, "d", "u", User::getDeptId, "d", Department::getId)
                .rightJoin(Order.class, "o", "u", User::getId, "o", Order::getUserId);
        System.out.println("Join with Different Operators: " + select);
        System.out.println("Params: " + select.params().params());
        System.out.println();
    }

    @Test
    public void testDeleteWithJoin() {
        // 删除操作中的连接
        Delete delete = Delete.create()
                .from(User.class, "u")
                .innerJoin(Department.class, "d", User::getDeptId, Department::getId)
                .where(User::getDeptId, "123");
        System.out.println("Delete with Join: " + delete);
        System.out.println("Params: " + delete.params().params());
        System.out.println();
    }

    @Test
    public void testUpdateWithJoin() {
        // 更新操作中的连接
        Update update = Update.create()
                .table(User.class, "u")
                .field("u", User::getStatus, 2)
                .innerJoin(Department.class, "d", User::getDeptId, Department::getId)
                .where(Cond.create().eq("d", Department::getDeptName, "IT"));
        System.out.println("Update with Join: " + update);
        System.out.println("Params: " + update.params().params());
        System.out.println();
    }

    @Test
    public void testCondTwoFieldComparison() {
        // 测试Cond类的两字段比较
        Cond cond1 = Cond.create()
                .eq(User::getId, Department::getParentId);
        System.out.println("Cond Two Field EQ: " + cond1);
        System.out.println("Params: " + cond1.params().params());
        System.out.println();

        Cond cond2 = Cond.create()
                .gt(User::getId, Department::getParentId);
        System.out.println("Cond Two Field GT: " + cond2);
        System.out.println("Params: " + cond2.params().params());
        System.out.println();

        Cond cond3 = Cond.create()
                .eq("u", User::getId, "d", Department::getParentId);
        System.out.println("Cond Two Field EQ with Prefix: " + cond3);
        System.out.println("Params: " + cond3.params().params());
        System.out.println();

        Cond cond4 = Cond.create()
                .lt("u", User::getStatus, "d", Department::getSortOrder);
        System.out.println("Cond Two Field LT with Prefix: " + cond4);
        System.out.println("Params: " + cond4.params().params());
        System.out.println();
    }

    @Test
    public void testJoinOnMethods() {
        // 测试Join类的各种on方法
        Join join1 = Join.inner(User.class, "u")
                .on(User::getId, Department::getParentId);
        System.out.println("Join on(SFunction, SFunction): " + join1);
        System.out.println("Params: " + join1.params().params());
        System.out.println();

        Join join2 = Join.inner(User.class, "u")
                .on("u", User::getId, "d", Department::getParentId);
        System.out.println("Join on(String, SFunction, String, SFunction): " + join2);
        System.out.println("Params: " + join2.params().params());
        System.out.println();

        Join join3 = Join.inner(User.class, "u")
                .on(cond -> cond.eq("u", User::getId, "d", Department::getParentId));
        System.out.println("Join on(Cond): " + join3);
        System.out.println("Params: " + join3.params().params());
        System.out.println();
    }

    @Test
    public void testJoinWithDifferentComparison() {
        // 测试Join类的各种比较运算符方法
        Join join1 = Join.inner(User.class, "u")
                .onGt(User::getId, Department::getParentId);
        System.out.println("Join onGt: " + join1);
        System.out.println("Params: " + join1.params().params());
        System.out.println();

        Join join2 = Join.inner(User.class, "u")
                .onLtEq(User::getStatus, Department::getSortOrder);
        System.out.println("Join onLtEq: " + join2);
        System.out.println("Params: " + join2.params().params());
        System.out.println();

        Join join3 = Join.inner(User.class, "u")
                .onNotEq(User::getDeptId, Department::getId);
        System.out.println("Join onNotEq: " + join3);
        System.out.println("Params: " + join3.params().params());
        System.out.println();

        Join join4 = Join.inner(User.class, "u")
                .onGtEq("u", User::getStatus, "d", Department::getSortOrder);
        System.out.println("Join onGtEq with Prefix: " + join4);
        System.out.println("Params: " + join4.params().params());
        System.out.println();
    }

    // ---

    /**
     * 测试基本条件查询
     */
    @Test
    public void testBasicCondition() {
        System.out.println("\n1. 测试基本条件查询:");

        // 简单等于条件
        Select select1 = Select.create()
                .field(User::getId)
                .field(User::getUsername)
                .field(User::getEmail)
                .from(User.class)
                .where(Cond.create().eq(User::getStatus, "active"));
        System.out.println("简单等于条件: " + select1.toString());

        // 多个条件组合
        Select select2 = Select.create()
                .field(User::getId)
                .field(User::getUsername)
                .field(User::getAge)
                .from(User.class)
                .where(Cond.create()
                        .eq(User::getStatus, "active")
                        .and()
                        .gt(User::getAge, 18)
                        .and()
                        .lt(User::getAge, 60));
        System.out.println("多个条件组合: " + select2.toString());

        // 模糊查询
        Select select3 = Select.create()
                .field(User::getId)
                .field(User::getUsername)
                .field(User::getEmail)
                .from(User.class)
                .where(Cond.create()
                        .like(User::getUsername, "%admin%")
                        .or()
                        .like(User::getEmail, "%admin%"));
        System.out.println("模糊查询: " + select3.toString());
    }

    /**
     * 测试字段选择
     */
    @Test
    public void testFieldSelection() {
        System.out.println("\n2. 测试字段选择:");

        // 选择特定字段
        Select select1 = Select.create()
                .field(User::getId)
                .field(User::getUsername)
                .from(User.class);
        System.out.println("选择特定字段: " + select1.toString());

        // 选择所有字段
        Select select2 = Select.create()
                .field(User::getId)
                .field(User::getUsername)
                .field(User::getEmail)
                .field(User::getAge)
                .field(User::getDeptId)
                .field(User::getStatus, false)
                .from(User.class);
        System.out.println("选择所有字段: " + select2.toString());

        // 使用函数选择字段
        Select select3 = Select.create()
                .field(User::getId)
                .field(Func.strings.UPPER(User::getUsername), "upper_username")
                .from(User.class);
        System.out.println("使用函数选择字段: " + select3.toString());
    }

    /**
     * 测试排序
     */
    @Test
    public void testOrderBy() {
        System.out.println("\n3. 测试排序:");

        // 升序排序
        Select select1 = Select.create()
                .field(User::getId)
                .field(User::getUsername)
                .field(User::getAge)
                .from(User.class)
                .where(Cond.create().eq(User::getStatus, "active"))
                .orderByAsc(User::getAge);
        System.out.println("升序排序: " + select1.toString());

        // 降序排序
        Select select2 = Select.create()
                .field(User::getId)
                .field(User::getUsername)
                .field(User::getAge)
                .from(User.class)
                .where(Cond.create().eq(User::getStatus, "active"))
                .orderByDesc(User::getAge);
        System.out.println("降序排序: " + select2.toString());

        // 多字段排序
        Select select3 = Select.create()
                .field(User::getId)
                .field(User::getUsername)
                .field(User::getDeptId)
                .field(User::getAge)
                .from(User.class)
                .where(Cond.create().eq(User::getStatus, "active"))
                .orderByAsc(User::getDeptId)
                .orderByDesc(User::getAge);
        System.out.println("多字段排序: " + select3.toString());
    }

    /**
     * 测试分组和聚合
     */
    @Test
    public void testGroupByAndAggregation() {
        System.out.println("\n4. 测试分组和聚合:");

        // 分组查询
        Select select1 = Select.create()
                .field(User::getDeptId)
                .field(Func.aggregate.COUNT(User::getId), "user_count")
                .field(Func.aggregate.AVG(User::getAge), "avg_age")
                .from(User.class)
                .where(Cond.create().eq(User::getStatus, "active"))
                .groupBy(User::getDeptId)
                .having(Cond.create().gt(Func.aggregate.COUNT(User::getId)));
        System.out.println("分组查询: " + select1.toString());
    }

    /**
     * 测试连接查询
     */
    @Test
    public void testJoinQueries() {
        System.out.println("\n5. 测试连接查询:");

        // 内连接
        Select select1 = Select.create()
                .field(User::getId)
                .field(User::getUsername)
                .field(Department::getDeptName, "department_name")
                .from(User.class, "u")
                .innerJoin(Department.class, "d", User::getDeptId, Department::getId)
                .where(Cond.create().eq(User::getStatus, "active"));
        System.out.println("内连接: " + select1.toString());

        // 左连接
        Select select2 = Select.create()
                .field(User::getId)
                .field(User::getUsername)
                .field(Department::getDeptName, "department_name")
                .from(User.class, "u")
                .leftJoin(Department.class, "d", User::getDeptId, Department::getId)
                .where(Cond.create().eq(User::getStatus, "active"));
        System.out.println("左连接: " + select2.toString());

        // 带别名的连接条件
        Select select3 = Select.create()
                .field(User::getId, "user_id")
                .field(User::getUsername)
                .field("d", Department::getDeptName, "department_name")
                .from(User.class, "u")
                .innerJoin(Department.class, "d", Cond.create().eq("u", User::getDeptId, "d", Department::getId))
                .where(Cond.create().eq(User::getStatus, "active"));
        System.out.println("带别名的连接条件: " + select3.toString());
    }

    /**
     * 测试函数调用
     */
    @Test
    public void testFunctionCalls() {
        System.out.println("\n6. 测试函数调用:");

        // 字符串函数
        Select select1 = Select.create()
                .field(User::getId)
                .field(Func.strings.UPPER(User::getUsername), "upper_username")
                .field(Func.strings.LOWER(User::getEmail), "lower_email")
                .field(Func.strings.LENGTH(User::getUsername), "username_length")
                .from(User.class);
        System.out.println("字符串函数: " + select1.toString());

        // 数学函数
        Select select2 = Select.create()
                .field(User::getId)
                .field(User::getAge)
                .field(Func.math.ABS(User::getAge), "abs_age")
                .field(Func.math.ROUND(User::getAge), "round_age")
                .from(User.class);
        System.out.println("数学函数: " + select2.toString());

        // 聚合函数
        Select select3 = Select.create()
                .field(Func.aggregate.COUNT(User::getId), "total_users")
                .field(Func.aggregate.MAX(User::getAge), "max_age")
                .field(Func.aggregate.MIN(User::getAge), "min_age")
                .field(Func.aggregate.AVG(User::getAge), "avg_age")
                .field(Func.aggregate.SUM(User::getAge), "sum_age")
                .from(User.class)
                .where(Cond.create().eq(User::getStatus, "active"));
        System.out.println("聚合函数: " + select3.toString());
    }

    /**
     * 测试更新操作
     */
    @Test
    public void testUpdateOperation() {
        System.out.println("\n7. 测试更新操作:");

        // 简单更新
        Update update1 = Update.create(User.class)
                .field(User::getStatus, "inactive")
                .where(Cond.create().eq(User::getId, 1L));
        System.out.println("简单更新: " + update1.toString());

        // 多个字段更新
        Update update2 = Update.create(User.class)
                .field(User::getUsername, "new_username")
                .field(User::getEmail, "new_email@example.com")
                .where(Cond.create().eq(User::getId, 2L));
        System.out.println("多个字段更新: " + update2.toString());

        // 带条件的更新
        Update update3 = Update.create(User.class)
                .field(User::getStatus, "inactive")
                .where(Cond.create()
                        .eq(User::getStatus, "active")
                        .and()
                        .lt(User::getAge, 18));
        System.out.println("带条件的更新: " + update3.toString());
    }

    /**
     * 测试删除操作
     */
    @Test
    public void testDeleteOperation() {
        System.out.println("\n8. 测试删除操作:");

        // 简单删除
        Delete delete1 = Delete.create(User.class)
                .where(Cond.create().eq(User::getId, 1L));
        System.out.println("简单删除: " + delete1.toString());

        // 带条件的删除
        Delete delete2 = Delete.create(User.class)
                .where(Cond.create()
                        .eq(User::getStatus, "inactive")
                        .and()
                        .lt(User::getAge, 18));
        System.out.println("带条件的删除: " + delete2.toString());

        // 带连接的删除
        Delete delete3 = Delete.create(User.class, "u")
                .innerJoin(Department.class, "d", User::getDeptId, Department::getId)
                .where(Cond.create().eq("d", Department::getDeptName, "Test Department"));
        System.out.println("带连接的删除: " + delete3.toString());
    }

    /**
     * 测试插入操作
     */
    @Test
    public void testInsertOperation() {
        System.out.println("\n9. 测试插入操作:");

        // 简单插入
        Insert insert1 = Insert.create(User.class)
                .field(User::getUsername, "test_user")
                .field(User::getEmail, "test@example.com")
                .field(User::getAge, 25)
                .field(User::getDeptId, 1L)
                .field(User::getStatus, "active");
        System.out.println("简单插入: " + insert1.toString());

        // 带选择的插入
        Select select = Select.create()
                .field(User::getUsername)
                .field(User::getEmail)
                .field(User::getAge)
                .field(User::getDeptId)
                .field(User::getStatus)
                .from(User.class)
                .where(Cond.create().eq(User::getId, 1L));
        Insert insert2 = Insert.create(User.class)
                .field(User::getUsername)
                .field(User::getEmail)
                .field(User::getAge)
                .field(User::getDeptId)
                .field(User::getStatus)
                .select(select);
        System.out.println("带选择的插入: " + insert2.toString());
    }

    // ---

    /**
     * 测试1: 简单条件查询
     */
    @Test
    public void testSimpleSelect() {
        System.out.println("\n1. 简单条件查询:");

        // 基本查询
        Select select = Select.create()
                .field(User::getId)
                .field(User::getName)
                .field(User::getAge)
                .from(User.class)
                .where(Cond.create()
                        .eq(User::getAge, 18)
                        .and().like(User::getName, Like.contains("test"))
                );

        System.out.println("SQL: " + select.toString());
        System.out.println("参数: " + select.params());
        System.out.println("预期结果: 查询年龄等于18且名称包含'test'的记录");
    }

    /**
     * 测试4: 分组与聚合函数
     */
    @Test
    public void testGroupBy() {
        System.out.println("\n4. 分组与聚合函数:");

        // 分组与聚合函数
        Select select = Select.create()
                .field(User::getName)
                .field(Func.aggregate.COUNT(User::getId), "total")
                .field(Func.aggregate.AVG(User::getAge), "avg_age")
                .field(Func.aggregate.MAX(User::getAge), "max_age")
                .field(Func.aggregate.MIN(User::getAge), "min_age")
                .from(User.class)
                .groupBy(User::getName)
                .having(Cond.create()
                        .gt(Func.aggregate.COUNT(User::getId), 1)
                )
                .orderByDesc(Func.aggregate.COUNT(User::getId));

        System.out.println("SQL: " + select.toString());
        System.out.println("参数: " + select.params());
        System.out.println("预期结果: 按名称分组，统计每组记录数、平均年龄、最大年龄、最小年龄，只显示记录数大于1的组");
    }

    /**
     * 测试5: 连接查询
     */
    @Test
    public void testJoinQuery() {
        System.out.println("\n5. 连接查询:");

        // 内连接
        Select select = Select.create()
                .field(User::getId, "entity_id")
                .field(User::getName, "entity_name")
                .field(Order::getUserId, "relation_name")
                .field(Order::getAmount, "relation_value")
                .from(User.class, "e")
                .innerJoin(Order.class, "r",
                        "e", User::getId,
                        "r", Order::getUserId)
                .where(Cond.create()
                        .eq(User::getAge, 18)
                );

        System.out.println("SQL: " + select.toString());
        System.out.println("参数: " + select.params());
        System.out.println("预期结果: 内连接查询，关联两个表，查询年龄等于18的记录");
    }

    /**
     * 测试6: 更新操作
     */
    @Test
    public void testUpdate() {
        System.out.println("\n6. 更新操作:");

        // 更新操作
        Update update = Update.create()
                .table(User.class)
                .field(User::getName, "updated_name")
                .field(User::getAge, 20)
                .where(Cond.create()
                        .eq(User::getId, "test_id")
                        .and().lt(User::getAge, 18)
                );

        System.out.println("SQL: " + update.toString());
        System.out.println("参数: " + update.params());
        System.out.println("预期结果: 更新ID为'test_id'且年龄小于18的记录，设置名称为'updated_name'，年龄为20");
    }

    /**
     * 测试7: 删除操作
     */
    @Test
    public void testDelete() {
        System.out.println("\n7. 删除操作:");

        // 删除操作
        Delete delete = Delete.create()
                .from(User.class)
                .where(Cond.create()
                        .eq(User::getId, "test_id")
                );

        System.out.println("SQL: " + delete.toString());
        System.out.println("参数: " + delete.params());
        System.out.println("预期结果: 删除ID为'test_id'的记录");
    }

    /**
     * 测试8: 复杂条件查询
     */
    @Test
    public void testComplexConditions() {
        System.out.println("\n8. 复杂条件查询:");

        // 复杂条件查询
        Select select = Select.create()
                .field(User::getId)
                .field(User::getName)
                .field(User::getAge)
                .from(User.class)
                .where(Cond.create()
                        .bracketBegin()
                        .eq(User::getAge, 18)
                        .or().eq(User::getAge, 19)
                        .or().eq(User::getAge, 20)
                        .bracketEnd()
                        .and().like(User::getName, "%test%")
                        .and().isNull(User::getCreateTime)
                );

        System.out.println("SQL: " + select.toString());
        System.out.println("参数: " + select.params());
        System.out.println("预期结果: 查询年龄为18、19或20，名称包含'test'且创建时间为空的记录");
    }

    /**
     * 测试9: 函数使用
     */
    @Test
    public void testFunctions() {
        System.out.println("\n9. 函数使用:");

        // 函数使用
        Select select = Select.create()
                .field(User::getId)
                .field(Func.math.ROUND(User::getAge), "rounded_age")
                .field(Func.strings.LOWER(User::getName), "lower_name")
                .field(Func.aggregate.COUNT(User::getId), "total")
                .from(User.class)
                .groupBy(User::getId)
                .orderByDesc(Func.math.ROUND(User::getAge));

        System.out.println("SQL: " + select.toString());
        System.out.println("参数: " + select.params());
        System.out.println("预期结果: 查询记录，使用ROUND和LOWER函数处理字段，按四舍五入后的年龄降序排序");
    }
}
