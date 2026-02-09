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
package net.ymate.platform.persistence.jdbc.query;

import net.ymate.platform.core.annotation.EnableAutoScan;
import net.ymate.platform.core.annotation.EnableBeanProxy;
import net.ymate.platform.core.annotation.EnableDevMode;
import net.ymate.platform.core.persistence.Page;
import net.ymate.platform.core.persistence.Params;
import net.ymate.platform.core.persistence.annotation.Entity;
import net.ymate.platform.core.persistence.annotation.Id;
import net.ymate.platform.core.persistence.annotation.PK;
import net.ymate.platform.core.persistence.annotation.Property;
import net.ymate.platform.core.persistence.base.IEntity;
import net.ymate.platform.core.persistence.base.IEntityPK;
import net.ymate.platform.test.YMPJUnit4ClassRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

/**
 * Lambda Query 功能测试类 - 完整覆盖所有Lambda表达式查询功能
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

    @PK
    public static class MultiUserId implements IEntityPK {

        @Property
        private String uid;

        @Property
        private String orderId;

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public String getOrderId() {
            return orderId;
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }
    }

    @Entity
    public static class MultiUser implements IEntity<MultiUserId> {

        @Id
        private MultiUserId id;

        @Property
        private String title;

        @Override
        public MultiUserId getId() {
            return id;
        }

        @Override
        public void setId(MultiUserId id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }

    // ========================== 测试 LambdaUtils 工具类 ==========================

    @Test
    public void testLambdaUtils() {
        // 测试字段名解析
        String fieldName = LambdaUtils.getFieldName(User::getId);
        Assert.assertEquals("id", fieldName);

        fieldName = LambdaUtils.getFieldName(User::getUsername);
        Assert.assertEquals("username", fieldName);

        // 测试数据库字段名解析
        String columnName = LambdaUtils.getColumnName(User::getId);
        Assert.assertEquals("id", columnName);

        // 测试带前缀的字段名
        String fullFieldName = LambdaUtils.getFullFieldName("u", User::getId);
        Assert.assertEquals("u.id", fullFieldName);
    }

    // ========================== 测试基础查询 (SELECT) ==========================

    @Test
    public void testSelectBasic() {
        // 测试基本查询
        Select select = Select.create()
                .field(User::getId)
                .field(User::getUsername)
                .from(User.class);

        String expectedSql = "SELECT  `id`,`username` FROM `user`";
        Assert.assertEquals(expectedSql, select.toString());
        Assert.assertTrue(select.params().params().isEmpty());

        // 测试带条件的查询
        select = Select.create()
                .field(User::getId)
                .field(User::getUsername)
                .from(User.class)
                .where(Cond.create().eq(User::getId, "1"));

        expectedSql = "SELECT  `id`,`username` FROM `user`  WHERE  id = ?";
        Assert.assertEquals(expectedSql, select.toString());
        Assert.assertEquals(1, select.params().params().size());
        Assert.assertEquals("1", select.params().params().get(0));
    }

    @Test
    public void testSelectFieldAlias() {
        // 测试字段别名
        Select select = Select.create()
                .field(User::getUsername, "user_name")
                .field(User::getEmail, "email_address")
                .from(User.class);

        String expectedSql = "SELECT  `username` AS user_name,`email` AS email_address FROM `user`";
        Assert.assertEquals(expectedSql, select.toString());
    }

    @Test
    public void testSelectAllFields() {
        // 测试选择所有字段
        Select select = Select.create()
                .field(User::getId)
                .field(User::getName)
                .field(User::getUsername)
                .field(User::getAge)
                .field(User::getEmail)
                .field(User::getCreateTime)
                .field(User::getStatus)
                .field(User::getDeptId)
                .from(User.class);

        String expectedSql = "SELECT  `id`,`name`,`username`,`age`,`email`,`create_time`,`status`,`dept_id` FROM `user`";
        Assert.assertEquals(expectedSql, select.toString());
    }

    // ========================== 测试插入操作 (INSERT) ==========================

    @Test
    public void testInsertBasic() {
        // 测试简单插入
        Insert insert = Insert.create(User.class)
                .field(User::getUsername, "test_user")
                .field(User::getEmail, "test@example.com")
                .field(User::getAge, 25)
                .field(User::getStatus, 1);

        String expectedSql = "INSERT INTO `user` (`username`,`email`,`age`,`status`) VALUES (?,?,?,?)";
        Assert.assertEquals(expectedSql, insert.toString());
        Assert.assertEquals(4, insert.params().params().size());
    }

    @Test
    public void testInsertSelect() {
        // 测试带选择的插入
        Select select = Select.create()
                .field(User::getUsername)
                .field(User::getEmail)
                .from(User.class)
                .where(Cond.create().eq(User::getId, "1"));

        Insert insert = Insert.create(User.class)
                .field(User::getUsername)
                .field(User::getEmail)
                .select(select);

        String expectedSql = "INSERT INTO `user` (`username`,`email`) SELECT  `username`,`email` FROM `user`  WHERE  id = ?";
        Assert.assertEquals(expectedSql, insert.toString());
        Assert.assertEquals(1, insert.params().params().size());
    }

    // ========================== 测试更新操作 (UPDATE) ==========================

    @Test
    public void testUpdateBasic() {
        // 测试简单更新
        Update update = Update.create(User.class)
                .field(User::getStatus, 2)
                .where(Cond.create().eq(User::getId, "1"));

        String expectedSql = "UPDATE `user`  SET `status` = ? WHERE  id = ?";
        Assert.assertEquals(expectedSql, update.toString());
        Assert.assertEquals(2, update.params().params().size());
        Assert.assertEquals(2, update.params().params().get(0));
        Assert.assertEquals("1", update.params().params().get(1));
    }

    @Test
    public void testUpdateMultipleFields() {
        // 测试更新多个字段
        Update update = Update.create(User.class)
                .field(User::getUsername, "new_user")
                .field(User::getEmail, "new@example.com")
                .field(User::getAge, 30)
                .where(Cond.create().eq(User::getId, "1"));

        String expectedSql = "UPDATE `user`  SET `username` = ?, `email` = ?, `age` = ? WHERE  id = ?";
        Assert.assertEquals(expectedSql, update.toString());
        Assert.assertEquals(4, update.params().params().size());
    }

    // ========================== 测试删除操作 (DELETE) ==========================

    @Test
    public void testDeleteBasic() {
        // 测试简单删除
        Delete delete = Delete.create(User.class)
                .where(Cond.create().eq(User::getId, "1"));

        String expectedSql = "DELETE  FROM `user`  WHERE  id = ?";
        Assert.assertEquals(expectedSql, delete.toString());
        Assert.assertEquals(1, delete.params().params().size());
    }

    @Test
    public void testDeleteWithCondition() {
        // 测试带条件的删除
        Delete delete = Delete.create(User.class)
                .where(Cond.create()
                        .eq(User::getStatus, 0)
                        .and().lt(User::getAge, 18));

        String expectedSql = "DELETE  FROM `user`  WHERE  status = ?  AND  age < ?";
        Assert.assertEquals(expectedSql, delete.toString());
        Assert.assertEquals(2, delete.params().params().size());
    }

    // ========================== 测试复杂条件查询 ==========================

    @Test
    public void testConditionEq() {
        // 测试等于条件
        Cond cond = Cond.create().eq(User::getId, "1");
        Assert.assertEquals(" id = ? ", cond.toString());

        // 测试不等于条件
        cond = Cond.create().notEq(User::getId, "1");
        Assert.assertEquals(" id != ? ", cond.toString());
    }

    @Test
    public void testConditionComparison() {
        // 测试比较操作符
        Cond cond = Cond.create().gt(User::getAge, 18);
        Assert.assertEquals(" age > ? ", cond.toString());

        cond = Cond.create().gtEq(User::getAge, 18);
        Assert.assertEquals(" age >= ? ", cond.toString());

        cond = Cond.create().lt(User::getAge, 60);
        Assert.assertEquals(" age < ? ", cond.toString());

        cond = Cond.create().ltEq(User::getAge, 60);
        Assert.assertEquals(" age <= ? ", cond.toString());
    }

    @Test
    public void testConditionLike() {
        // 测试模糊查询
        Cond cond = Cond.create().like(User::getUsername, "%test%");
        Assert.assertEquals(" username LIKE ? ", cond.toString());

        cond = Cond.create().like(User::getUsername, Like.startsWith("test"));
        Assert.assertEquals(" username LIKE ? ", cond.toString());
        Assert.assertEquals("test%", cond.params().params().get(0));

        cond = Cond.create().like(User::getUsername, Like.endsWith("test"));
        Assert.assertEquals(" username LIKE ? ", cond.toString());
        Assert.assertEquals("%test", cond.params().params().get(0));
    }

    @Test
    public void testConditionIsNull() {
        // 测试NULL条件
        Cond cond = Cond.create().isNull(User::getCreateTime);
        Assert.assertEquals(" create_time IS NULL ", cond.toString());

        cond = Cond.create().isNotNull(User::getCreateTime);
        Assert.assertEquals(" create_time IS NOT NULL ", cond.toString());
    }

    @Test
    public void testConditionIn() {
        // 测试IN条件
        Cond cond = Cond.create().in(User::getAge, Params.create(18, 19, 20));
        Assert.assertEquals(" age IN (?,?,?) ", cond.toString());
        Assert.assertEquals(3, cond.params().params().size());
    }

    @Test
    public void testConditionBetween() {
        // 测试BETWEEN条件
        Cond cond = Cond.create().between(User::getAge, 18, 30);
        Assert.assertEquals(" age BETWEEN ? AND ? ", cond.toString());
        Assert.assertEquals(2, cond.params().params().size());
    }

    @Test
    public void testConditionCombination() {
        // 测试条件组合
        Cond cond = Cond.create()
                .eq(User::getStatus, 1)
                .and().gt(User::getAge, 18)
                .and().lt(User::getAge, 60);

        String expected = " status = ?  AND  age > ?  AND  age < ? ";
        Assert.assertEquals(expected, cond.toString());
        Assert.assertEquals(3, cond.params().params().size());
    }

    @Test
    public void testConditionBracket() {
        // 测试括号条件
        Cond cond = Cond.create()
                .bracketBegin()
                .eq(User::getAge, 18)
                .or().eq(User::getAge, 19)
                .or().eq(User::getAge, 20)
                .bracketEnd()
                .and().eq(User::getStatus, 1);

        String expected = " (  age = ?  OR  age = ?  OR  age = ?  )  AND  status = ? ";
        Assert.assertEquals(expected, cond.toString());
        Assert.assertEquals(4, cond.params().params().size());
    }

    // ========================== 测试多表关联查询 (JOIN) ==========================

    @Test
    public void testJoinInner() {
        // 测试内连接
        Select select = Select.create()
                .field(User::getUsername, "user_name")
                .field(Department::getDeptName, "dept_name")
                .from(User.class, "u")
                .innerJoin(Department.class, "d", User::getDeptId, Department::getId);

        String expectedSql = "SELECT  `username` AS user_name,`dept_name` AS dept_name FROM `user` u INNER JOIN `department` d ON  `dept_id` = `id`";
        Assert.assertEquals(expectedSql, select.toString());
    }

    @Test
    public void testJoinLeft() {
        // 测试左连接
        Select select = Select.create()
                .field(User::getUsername, "user_name")
                .field(Department::getDeptName, "dept_name")
                .from(User.class, "u")
                .leftJoin(Department.class, "d", User::getDeptId, Department::getId);

        String expectedSql = "SELECT  `username` AS user_name,`dept_name` AS dept_name FROM `user` u LEFT JOIN `department` d ON  `dept_id` = `id`";
        Assert.assertEquals(expectedSql, select.toString());
    }

    @Test
    public void testJoinRight() {
        // 测试右连接
        Select select = Select.create()
                .field(User::getUsername, "user_name")
                .field(Department::getDeptName, "dept_name")
                .from(User.class, "u")
                .rightJoin(Department.class, "d", User::getDeptId, Department::getId);

        String expectedSql = "SELECT  `username` AS user_name,`dept_name` AS dept_name FROM `user` u RIGHT JOIN `department` d ON  `dept_id` = `id`";
        Assert.assertEquals(expectedSql, select.toString());
    }

    @Test
    public void testJoinWithCondition() {
        // 测试带条件的连接
        Select select = Select.create()
                .field(User::getUsername, "user_name")
                .field(Order::getAmount, "order_amount")
                .from(User.class, "u")
                .innerJoin(Order.class, "o", Cond.create()
                        .eq("u", User::getId, "o", Order::getUserId)
                        .and().gt("o", Order::getAmount, 100.0));

        String expectedSql = "SELECT  `username` AS user_name,`amount` AS order_amount FROM `user` u INNER JOIN `order` o ON  u.id = o.user_id  AND  o.amount > ?";
        Assert.assertEquals(expectedSql, select.toString());
        Assert.assertEquals(1, select.params().params().size());
    }

    @Test
    public void testJoinMultiTable() {
        // 测试多表连接
        Select select = Select.create()
                .field(User::getUsername, "user_name")
                .field(Department::getDeptName, "dept_name")
                .field(Order::getAmount, "order_amount")
                .from(User.class, "u")
                .innerJoin(Department.class, "d", User::getDeptId, Department::getId)
                .innerJoin(Order.class, "o", User::getId, Order::getUserId);

        String expectedSql = "SELECT  `username` AS user_name,`dept_name` AS dept_name,`amount` AS order_amount FROM `user` u INNER JOIN `department` d ON  `dept_id` = `id`  INNER JOIN `order` o ON  `id` = `user_id`";
        Assert.assertEquals(expectedSql, select.toString());
    }

    // ========================== 测试排序和分组 ==========================

    @Test
    public void testOrderBy() {
        // 测试升序排序
        Select select = Select.create()
                .field(User::getId)
                .field(User::getUsername)
                .field(User::getAge)
                .from(User.class)
                .orderByAsc(User::getAge);

        String expectedSql = "SELECT  `id`,`username`,`age` FROM `user`   ORDER BY `age`";
        Assert.assertEquals(expectedSql, select.toString());

        // 测试降序排序
        select = Select.create()
                .field(User::getId)
                .field(User::getUsername)
                .field(User::getAge)
                .from(User.class)
                .orderByDesc(User::getAge);

        expectedSql = "SELECT  `id`,`username`,`age` FROM `user`   ORDER BY `age` DESC";
        Assert.assertEquals(expectedSql, select.toString());

        // 测试多字段排序
        select = Select.create()
                .field(User::getId)
                .field(User::getUsername)
                .field(User::getDeptId)
                .field(User::getAge)
                .from(User.class)
                .orderByAsc(User::getDeptId)
                .orderByDesc(User::getAge);

        expectedSql = "SELECT  `id`,`username`,`dept_id`,`age` FROM `user`   ORDER BY `dept_id`,`age` DESC";
        Assert.assertEquals(expectedSql, select.toString());
    }

    @Test
    public void testGroupBy() {
        // 测试分组查询
        Select select = Select.create()
                .field(User::getDeptId)
                .field(Func.aggregate.COUNT(User::getId), "user_count")
                .field(Func.aggregate.AVG(User::getAge), "avg_age")
                .from(User.class)
                .groupBy(User::getDeptId);

        String expectedSql = "SELECT  `dept_id`,COUNT(id) AS user_count,AVG(age) AS avg_age FROM `user`  GROUP BY `dept_id`";
        Assert.assertEquals(expectedSql, select.toString());

        // 测试带HAVING的分组查询
        select = Select.create()
                .field(User::getDeptId)
                .field(Func.aggregate.COUNT(User::getId), "user_count")
                .from(User.class)
                .groupBy(User::getDeptId)
                .having(Cond.create().gt(Func.aggregate.COUNT(User::getId), 5));

        expectedSql = "SELECT  `dept_id`,COUNT(id) AS user_count FROM `user`  GROUP BY `dept_id` HAVING  COUNT(id) > ?";
        Assert.assertEquals(expectedSql, select.toString());
        Assert.assertEquals(1, select.params().params().size());
    }

    // ========================== 测试分页 ==========================

    @Test
    public void testPagination() {
        // 测试分页查询
        Select select = Select.create()
                .field(User::getId)
                .field(User::getUsername)
                .from(User.class)
                .orderByAsc(User::getId)
                .page(Page.create(1).pageSize(10));

        String expectedSql = "SELECT  `id`,`username` FROM `user`   ORDER BY `id` LIMIT 0, 10";
        Assert.assertEquals(expectedSql, select.toString());

        // 测试带条件的分页查询
        select = Select.create()
                .field(User::getId)
                .field(User::getUsername)
                .from(User.class)
                .where(Cond.create().eq(User::getStatus, 1))
                .orderByAsc(User::getId)
                .page(Page.create(2).pageSize(10));

        expectedSql = "SELECT  `id`,`username` FROM `user`  WHERE  status = ? ORDER BY `id` LIMIT 10, 10";
        Assert.assertEquals(expectedSql, select.toString());
        Assert.assertEquals(1, select.params().params().size());
    }

    // ========================== 测试聚合函数 ==========================

    @Test
    public void testAggregateFunctions() {
        // 测试COUNT函数
        Select select = Select.create()
                .field(Func.aggregate.COUNT(User::getId), "total_users")
                .from(User.class);

        String expectedSql = "SELECT  COUNT(id) AS total_users FROM `user`";
        Assert.assertEquals(expectedSql, select.toString());

        // 测试MAX函数
        select = Select.create()
                .field(Func.aggregate.MAX(User::getAge), "max_age")
                .from(User.class);

        expectedSql = "SELECT  MAX(age) AS max_age FROM `user`";
        Assert.assertEquals(expectedSql, select.toString());

        // 测试MIN函数
        select = Select.create()
                .field(Func.aggregate.MIN(User::getAge), "min_age")
                .from(User.class);

        expectedSql = "SELECT  MIN(age) AS min_age FROM `user`";
        Assert.assertEquals(expectedSql, select.toString());

        // 测试AVG函数
        select = Select.create()
                .field(Func.aggregate.AVG(User::getAge), "avg_age")
                .from(User.class);

        expectedSql = "SELECT  AVG(age) AS avg_age FROM `user`";
        Assert.assertEquals(expectedSql, select.toString());

        // 测试SUM函数
        select = Select.create()
                .field(Func.aggregate.SUM(User::getAge), "sum_age")
                .from(User.class);

        expectedSql = "SELECT  SUM(age) AS sum_age FROM `user`";
        Assert.assertEquals(expectedSql, select.toString());
    }

    @Test
    public void testMathFunctions() {
        // 测试数学函数
        Select select = Select.create()
                .field(User::getId)
                .field(Func.math.ABS(User::getAge), "abs_age")
                .field(Func.math.ROUND(User::getAge), "round_age")
                .from(User.class);

        String expectedSql = "SELECT  `id`,ABS(age) AS abs_age,ROUND(age) AS round_age FROM `user`";
        Assert.assertEquals(expectedSql, select.toString());
    }

    @Test
    public void testStringFunctions() {
        // 测试字符串函数
        Select select = Select.create()
                .field(User::getId)
                .field(Func.strings.UPPER(User::getUsername), "upper_username")
                .field(Func.strings.LOWER(User::getEmail), "lower_email")
                .field(Func.strings.LENGTH(User::getUsername), "username_length")
                .from(User.class);

        String expectedSql = "SELECT  `id`,UPPER(username) AS upper_username,LOWER(email) AS lower_email,LENGTH(username) AS username_length FROM `user`";
        Assert.assertEquals(expectedSql, select.toString());
    }

    // ========================== 测试子查询 ==========================

    @Test
    public void testSubQuery() {
        // 测试子查询 - IN子查询
        Select subSelect = Select.create()
                .field(User::getId)
                .from(User.class)
                .where(Cond.create().gt(User::getAge, 30));

        Select select = Select.create()
                .field(Order::getId)
                .field(Order::getAmount)
                .from(Order.class)
                .where(Cond.create().in(Order::getUserId, subSelect));

        String expectedSql = "SELECT  `id`,`amount` FROM `order`  WHERE  user_id IN (SELECT  `id` FROM `user`  WHERE  age > ?)";
        Assert.assertEquals(expectedSql, select.toString());
        Assert.assertEquals(1, select.params().params().size());

        // 测试子查询 - EXISTS子查询
        select = Select.create()
                .field(User::getId)
                .field(User::getUsername)
                .from(User.class)
                .where(Cond.create().exists(subSelect));

        expectedSql = "SELECT  `id`,`username` FROM `user`  WHERE  EXISTS (SELECT  `id` FROM `user`  WHERE  age > ?)";
        Assert.assertEquals(expectedSql, select.toString());
        Assert.assertEquals(1, select.params().params().size());
    }

    // ========================== 测试单主键操作 ==========================

    @Test
    public void testSinglePkSelect() {
        // 测试根据主键查询
        Select select = Select.create()
                .field(User::getId)
                .field(User::getUsername)
                .from(User.class)
                .where(Cond.create().eq(User::getId, "1"));

        String expectedSql = "SELECT  `id`,`username` FROM `user`  WHERE  id = ?";
        Assert.assertEquals(expectedSql, select.toString());
    }

    @Test
    public void testSinglePkUpdate() {
        // 测试根据主键更新
        Update update = Update.create(User.class)
                .field(User::getUsername, "updated_user")
                .where(Cond.create().eq(User::getId, "1"));

        String expectedSql = "UPDATE `user`  SET `username` = ? WHERE  id = ?";
        Assert.assertEquals(expectedSql, update.toString());
    }

    @Test
    public void testSinglePkDelete() {
        // 测试根据主键删除
        Delete delete = Delete.create(User.class)
                .where(Cond.create().eq(User::getId, "1"));

        String expectedSql = "DELETE  FROM `user`  WHERE  id = ?";
        Assert.assertEquals(expectedSql, delete.toString());
    }

    // ========================== 测试复合主键操作 ==========================

    @Test
    public void testCompositePkSelect() {
        // 测试复合主键查询
        Select select = Select.create()
                .field(MultiUser::getTitle)
                .from(MultiUser.class)
                .where(Cond.create().eq(MultiUserId::getUid, "user001"));

        String expectedSql = "SELECT  `title` FROM `multi_user`  WHERE  uid = ?";
        Assert.assertEquals(expectedSql, select.toString());

        // 测试复合主键完整查询
        select = Select.create()
                .field(MultiUser::getTitle)
                .from(MultiUser.class)
                .where(Cond.create()
                        .eq(MultiUserId::getUid, "user001")
                        .and().eq(MultiUserId::getOrderId, "order001"));

        expectedSql = "SELECT  `title` FROM `multi_user`  WHERE  uid = ?  AND  order_id = ?";
        Assert.assertEquals(expectedSql, select.toString());
        Assert.assertEquals(2, select.params().params().size());
    }

    @Test
    public void testCompositePkUpdate() {
        // 测试复合主键更新
        Update update = Update.create(MultiUser.class)
                .field(MultiUser::getTitle, "updated_title")
                .where(Cond.create()
                        .eq(MultiUserId::getUid, "user001")
                        .and().eq(MultiUserId::getOrderId, "order001"));

        String expectedSql = "UPDATE `multi_user`  SET `title` = ? WHERE  uid = ?  AND  order_id = ?";
        Assert.assertEquals(expectedSql, update.toString());
        Assert.assertEquals(3, update.params().params().size());
    }

    @Test
    public void testCompositePkDelete() {
        // 测试复合主键删除
        Delete delete = Delete.create(MultiUser.class)
                .where(Cond.create()
                        .eq(MultiUserId::getUid, "user001")
                        .and().eq(MultiUserId::getOrderId, "order001"));

        String expectedSql = "DELETE  FROM `multi_user`  WHERE  uid = ?  AND  order_id = ?";
        Assert.assertEquals(expectedSql, delete.toString());
        Assert.assertEquals(2, delete.params().params().size());
    }

    // ========================== 测试更新和删除操作中的连接 ==========================

    @Test
    public void testUpdateWithJoin() {
        // 测试更新操作中的连接
        Update update = Update.create()
                .table(User.class, "u")
                .field("u", User::getStatus, 2)
                .innerJoin(Department.class, "d", User::getDeptId, Department::getId)
                .where(Cond.create().eq("d", Department::getDeptName, "IT"));

        String expectedSql = "UPDATE `user` u INNER JOIN `department` d ON  `dept_id` = `id`  SET u.`status` = ? WHERE  d.dept_name = ?";
        Assert.assertEquals(expectedSql, update.toString());
        Assert.assertEquals(2, update.params().params().size());
    }

    @Test
    public void testDeleteWithJoin() {
        // 测试删除操作中的连接
        Delete delete = Delete.create()
                .from(User.class, "u")
                .innerJoin(Department.class, "d", User::getDeptId, Department::getId)
                .where(Cond.create().eq("d", Department::getDeptName, "IT"));

        String expectedSql = "DELETE  FROM `user` u INNER JOIN `department` d ON  `dept_id` = `id`  WHERE  d.dept_name = ?";
        Assert.assertEquals(expectedSql, delete.toString());
        Assert.assertEquals(1, delete.params().params().size());
    }

    // ========================== 测试字段包装控制 ==========================

    @Test
    public void testFieldWrapControl() {
        // 测试不带包装的字段
        Select select = Select.create()
                .field(User::getId, false)
                .field(User::getUsername, false)
                .from(User.class);

        String expectedSql = "SELECT  id,username FROM `user`";
        Assert.assertEquals(expectedSql, select.toString());

        // 测试混合包装的字段
        select = Select.create()
                .field(User::getId, true)
                .field(User::getUsername, false)
                .from(User.class);

        expectedSql = "SELECT  `id`,username FROM `user`";
        Assert.assertEquals(expectedSql, select.toString());
    }
}