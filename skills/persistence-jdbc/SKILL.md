---
name: ymp-persistence-jdbc
description: YMP框架JDBC持久化模块，针对关系型数据库数据存取的轻量级解决方案，支持ORM、多数据源、事务等
version: 2.1.4-dev
author: YMP Team
category: persistence
tags:
  - java
  - jdbc
  - orm
  - persistence
  - database
trigger: 当用户需要使用关系型数据库、实现JDBC持久化、ORM操作、事务管理、多数据源、存储器Repository、Where/Lambda查询构建等场景时触发
examples:
  - 配置HikariCP/Druid数据源
  - @Entity实体类save/load/update/delete CRUD操作
  - @Repository存储器接口+@Sql自定义SQL
  - @Transaction事务方法（含嵌套事务）
  - Where/Cond/Lambda条件查询构建器分页查询
---

# JDBC 持久化技能包

> AI读取指引：本技能仅处理关系型数据库JDBC持久化。涉及Redis/MongoDB请跳转到对应SKILL；事务依赖AOP代理，需配合@EnableBeanProxy。

---

## 0. 快速索引

- Maven artifactId：`ymate-platform-persistence-jdbc`
- 静态入口类全限定名：`net.ymate.platform.persistence.jdbc.JDBC`
- 必备启动注解：`@EnableAutoScan`（扫描实体/Repository）、`@EnableBeanProxy`（事务AOP代理生效）
- 5行最简调用代码片段：

```java
UserEntity user = UserEntity.builder()
    .id(UUIDUtils.UUID()).username("ymp").build();
user.save();
UserEntity loaded = UserEntity.builder().id(user.getId()).build().load();
loaded.bind().nickname("测试").build().update();
```

## 1. 模块摘要

JDBC持久化模块是针对关系型数据库（RDBMS）的轻量级存取方案，基于JDBC API封装，提供ORM、多数据源、事务嵌套、存储器Repository、Lambda查询构建等能力。

- 单实体ORM操作（save/load/update/delete/saveOrUpdate/saveIfNotExist），无需手写SQL
- 支持C3P0/DBCP/Druid/HikariCP/JNDI多种连接池，支持多数据源切换
- 支持@Transaction注解事务和Transactions.execute()手动事务，支持事务嵌套
- @Repository存储器接口支持@Sql注解或从resources/*.yml/sql动态加载SQL
- 提供Where/Cond/FieldCondition/Select/Insert/Update/Delete面向对象查询构建

## 2. 核心注解速查表（全限定名）

| 注解 | 全限定名 | 作用 | 核心参数 |
|---|---|---|---|
| @Entity | `net.ymate.platform.core.persistence.annotation.Entity` | 声明类为数据实体（映射数据库表） | value：表名，默认类名 |
| @Id | `net.ymate.platform.core.persistence.annotation.Id` | 声明成员为主键 | 无参数（配合@Property使用） |
| @Property | `net.ymate.platform.core.persistence.annotation.Property` | 声明成员为实体属性（映射表字段） | name/nullable/length/autoincrement/useKeyGenerator |
| @Readonly | `net.ymate.platform.core.persistence.annotation.Readonly` | 声明只读属性，更新时忽略 | 无参数 |
| @Comment | `net.ymate.platform.core.persistence.annotation.Comment` | 实体/字段注释 | value：注释内容 |
| @Default | `net.ymate.platform.core.persistence.annotation.Default` | 字段默认值 | value：默认值字符串 |
| @PK | `net.ymate.platform.core.persistence.annotation.PK` | 声明类为复合主键对象 | 无参数 |
| @Transaction | `net.ymate.platform.core.persistence.annotation.Transaction` | 类/方法开启数据库事务 | value：隔离级别（Type.TRANSACTION枚举） |
| @Repository | `net.ymate.platform.persistence.jdbc.repo.annotation.Repository` | 声明存储器接口/方法 | dsName/value/item/configFile/update/page/resultClass |
| @DatabaseConf | `net.ymate.platform.persistence.jdbc.annotation.DatabaseConf` | JDBC模块配置注解 | dsDefaultName/value(@DatabaseDataSource数组) |
| @DatabaseDataSource | `net.ymate.platform.persistence.jdbc.annotation.DatabaseDataSource` | 单个数据源配置 | name/connectionUrl/username/password/adapterClass/showSql/autoConnection |

## 3. 核心API速查（入口类常用方法≤8条）

| API | 说明 |
|---|---|
| `JDBC.get()` | 获取JDBC模块静态入口实例 |
| `JDBC.get().openSession(IDatabaseSessionExecutor)` | 用默认数据源开启会话并执行回调（自动关闭） |
| `JDBC.get().openSession(String dsName, executor)` | 用指定数据源开启会话 |
| `JDBC.get().openSession()` | try-with-resources方式直接打开会话（需手动关闭） |
| `session.save(entity)` / `session.load(Class, id)` / `session.update(entity)` / `session.delete(Class, id)` | 会话级实体CRUD |
| `session.find(EntitySQL.create(Class), Where, Page)` | 会话级条件查询+分页 |
| `entity.save()/.load()/.update()/.delete()/.find()/.findFirst()/.count()/.saveOrUpdate()/.saveIfNotExist()` | 继承BaseEntity的实体直接CRUD |
| `Transactions.execute(ITrade)` / `Transactions.execute(Type.TRANSACTION, ITrade)` / `Transactions.execute(AbstractTrade<T>)` | 手动事务管理（有无返回值两种） |

## 4. 标准代码模板（最少可运行）

### 模板1：ymp-conf.properties中HikariCP数据源配置

```properties
#-------------------------------------
# JDBC持久化模块初始化参数
#-------------------------------------
ymp.configs.persistence.jdbc.ds_default_name=default
ymp.configs.persistence.jdbc.ds_name_list=default

ymp.configs.persistence.jdbc.ds.default.adapter_class=hikaricp
ymp.configs.persistence.jdbc.ds.default.auto_connection=true
ymp.configs.persistence.jdbc.ds.default.show_sql=true
ymp.configs.persistence.jdbc.ds.default.stack_traces=false
ymp.configs.persistence.jdbc.ds.default.connection_url=jdbc:mysql://localhost:3306/ymp_demo?useUnicode=true&useSSL=false&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
ymp.configs.persistence.jdbc.ds.default.username=root
ymp.configs.persistence.jdbc.ds.default.password=123456
ymp.configs.persistence.jdbc.ds.default.password_encrypted=false
ymp.configs.persistence.jdbc.ds.default.table_prefix=ym_
```

### 模板2：标准@Entity实体类（含@Id/@Property/builder链式+save/load/update/delete调用）

```java
/*
 * Copyright 2007-2026 the original author or authors.
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
package net.ymate.demo.persistence.entity;

import net.ymate.platform.core.persistence.annotation.Comment;
import net.ymate.platform.core.persistence.annotation.Default;
import net.ymate.platform.core.persistence.annotation.Entity;
import net.ymate.platform.core.persistence.annotation.Id;
import net.ymate.platform.core.persistence.annotation.Property;
import net.ymate.platform.core.persistence.annotation.Readonly;
import net.ymate.platform.persistence.jdbc.support.BaseEntity;

/**
 * 用户信息实体
 *
 * @author YMP Team
 * @since 2.1.4-dev
 */
@Entity(UserEntity.TABLE_NAME)
@Comment("用户信息")
public class UserEntity extends BaseEntity<UserEntity, String> {

    public static final String TABLE_NAME = "user";

    @Id
    @Property(name = FIELDS.ID, nullable = false, length = 32)
    @Comment("用户唯一标识")
    private String id;

    @Property(name = FIELDS.USERNAME, length = 32)
    @Comment("用户名称")
    private String username;

    @Property(name = FIELDS.NICKNAME, length = 32)
    @Comment("昵称")
    private String nickname;

    @Property(name = FIELDS.EMAIL, length = 100)
    @Comment("电子邮件")
    private String email;

    @Property(name = FIELDS.STATUS, unsigned = true, length = 2)
    @Default("0")
    @Comment("状态")
    private Integer status;

    @Property(name = FIELDS.CREATE_TIME, nullable = false, length = 13)
    @Readonly
    @Comment("注册时间")
    private Long createTime;

    public UserEntity() {
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Builder bind() {
        return new Builder(this);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final UserEntity target;

        public Builder() {
            target = new UserEntity();
        }

        public Builder(UserEntity target) {
            this.target = target;
        }

        public UserEntity build() {
            return target;
        }

        public Builder id(String id) {
            target.setId(id);
            return this;
        }

        public Builder username(String username) {
            target.setUsername(username);
            return this;
        }

        public Builder nickname(String nickname) {
            target.setNickname(nickname);
            return this;
        }

        public Builder email(String email) {
            target.setEmail(email);
            return this;
        }

        public Builder status(Integer status) {
            target.setStatus(status);
            return this;
        }

        public Builder createTime(Long createTime) {
            target.setCreateTime(createTime);
            return this;
        }
    }

    public interface FIELDS {
        String ID = "id";
        String USERNAME = "username";
        String NICKNAME = "nickname";
        String EMAIL = "email";
        String STATUS = "status";
        String CREATE_TIME = "create_time";
    }

    // ---------- 业务使用示例（实际代码中写在Service/Dao层） ----------
    // UserEntity user = UserEntity.builder()
    //     .id(UUIDUtils.UUID()).username("ymp").createTime(System.currentTimeMillis()).build();
    // user.save();                               // 插入
    // UserEntity loaded = UserEntity.builder().id(user.getId()).build().load();
    // loaded.bind().nickname("新昵称").build().update();  // 更新
    // loaded.delete();                           // 删除
}
```

### 模板3：@Repository存储器接口（带@Sql/从resources/*.yml/sql加载）

```java
/*
 * Copyright 2007-2026 the original author or authors.
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
package net.ymate.demo.persistence.repository;

import net.ymate.platform.core.persistence.IResultSet;
import net.ymate.platform.core.persistence.Page;
import net.ymate.platform.persistence.jdbc.repo.annotation.Repository;
import net.ymate.platform.persistence.jdbc.repo.annotation.Sql;

/**
 * 用户存储器接口
 *
 * @author YMP Team
 * @since 2.1.4-dev
 */
@Repository(dsName = "default", configFile = "repositories/user_repository.yml")
public interface IUserRepository {

    /**
     * 内嵌@Sql注解方式：根据用户名查询
     *
     * @param username 用户名
     * @return 用户实体
     * @since 2.1.4-dev
     */
    @Repository(value = "SELECT * FROM ym_user WHERE username = ?", resultClass = net.ymate.demo.persistence.entity.UserEntity.class)
    Object findByUsername(String username);

    /**
     * 从yml/sql配置文件加载item=updateStatusById的SQL
     *
     * @param status 状态
     * @param id     用户ID
     * @return 影响行数
     * @since 2.1.4-dev
     */
    @Repository(item = "updateStatusById", update = true)
    int updateStatusById(Integer status, String id);

    /**
     * 分页查询所有用户（page=true自动处理分页参数）
     *
     * @param page 分页对象
     * @return 分页结果集
     * @since 2.1.4-dev
     */
    @Repository(item = "findAllUsers", page = true, resultClass = net.ymate.demo.persistence.entity.UserEntity.class)
    IResultSet<?> findAllUsers(Page page);
}
```

对应 `resources/repositories/user_repository.yml`（可选）：
```yaml
updateStatusById: "UPDATE ym_user SET status = ? WHERE id = ?"
findAllUsers: "SELECT * FROM ym_user ORDER BY create_time DESC"
```

### 模板4：@Transaction事务方法（嵌套事务）

```java
/*
 * Copyright 2007-2026 the original author or authors.
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
package net.ymate.demo.service;

import net.ymate.platform.core.beans.annotation.Bean;
import net.ymate.platform.core.persistence.ITrade;
import net.ymate.platform.core.persistence.annotation.Transaction;
import net.ymate.platform.core.persistence.base.Type;
import net.ymate.platform.demo.persistence.entity.UserEntity;
import net.ymate.platform.persistence.jdbc.transaction.Transactions;

/**
 * 用户事务服务示例
 *
 * @author YMP Team
 * @since 2.1.4-dev
 */
@Bean
public class UserTransactionService {

    /**
     * 注解方式：转账（外部事务，隔离级别读已提交）
     * 嵌套说明：内部调用nestedUpdate()会合并到同一个事务，任何异常整体回滚
     *
     * @param fromId 转出用户ID
     * @param toId   转入用户ID
     * @param amount 金额
     * @since 2.1.4-dev
     */
    @Transaction(Type.TRANSACTION.READ_COMMITTED)
    public void transfer(String fromId, String toId, int amount) throws Exception {
        UserEntity from = UserEntity.builder().id(fromId).build().load();
        from.bind().status(from.getStatus() - amount).build().update();
        nestedUpdate(toId, amount);
    }

    /**
     * 嵌套事务（实际与外层共享连接，以最外层级别为准）
     *
     * @since 2.1.4-dev
     */
    @Transaction
    public void nestedUpdate(String toId, int amount) throws Exception {
        UserEntity to = UserEntity.builder().id(toId).build().load();
        to.bind().status(to.getStatus() + amount).build().update();
    }

    /**
     * 手动事务方式：Transactions.execute()
     *
     * @since 2.1.4-dev
     */
    public void manualTransactionDemo() throws Exception {
        Transactions.execute(Type.TRANSACTION.REPEATABLE_READ, new ITrade() {
            @Override
            public void deal() throws Throwable {
                UserEntity u = UserEntity.builder()
                    .id("manual-tx-id")
                    .username("manual")
                    .createTime(System.currentTimeMillis())
                    .build();
                u.save();
            }
        });
    }
}
```

### 模板5：Where/Lambda查询构建器查询

> **Lambda vs 字符串字段名**：优先使用 Lambda 方式（`User::getId`），编译期检查字段名正确性，支持 IDE 重构自动同步；复杂动态 SQL 场景再退回到字符串方式。

```java
// import net.ymate.platform.core.persistence.Fields;
// import net.ymate.platform.core.persistence.Page;
// import net.ymate.platform.core.persistence.Params;
// import net.ymate.platform.persistence.jdbc.query.*;
// import static net.ymate.platform.persistence.jdbc.query.Func.*;

// ==================== 方式1：Lambda 表达式查询（推荐）====================

// ---- 1.1 Lambda 基础查询 + 条件 ----
Select lambdaSelect = Select.create()
    .field(UserEntity::getId)
    .field(UserEntity::getUsername, "user_name")  // 字段别名
    .field(UserEntity::getEmail)
    .from(UserEntity.class)
    .where(Cond.create()
        .eq(UserEntity::getStatus, 1)              // Lambda 等值条件
        .and().like(UserEntity::getEmail, Like.endsWith("@163.com"))  // Lambda LIKE
        .and().between(UserEntity::getCreateTime, startTime, endTime)) // BETWEEN
    .orderByDesc(UserEntity::getCreateTime)       // Lambda 排序
    .page(Page.create(1).pageSize(10));           // 分页
IResultSet<UserEntity> lambdaPage = lambdaSelect.find(new EntityResultSetHandler<>(UserEntity.class));

// ---- 1.2 Lambda 多表 JOIN 查询 ----
Select joinSelect = Select.create()
    .field("u", UserEntity::getUsername, "user_name")
    .field("d", DepartmentEntity::getDeptName, "dept_name")
    .field("o", OrderEntity::getAmount, "order_amount")
    .from(UserEntity.class, "u")
    .innerJoin(DepartmentEntity.class, "d", UserEntity::getDeptId, DepartmentEntity::getId)
    .leftJoin(OrderEntity.class, "o", Cond.create()
        .eq("u", UserEntity::getId, "o", OrderEntity::getUserId)
        .and().gt("o", OrderEntity::getAmount, 100.0))
    .where(Cond.create().eq("d", DepartmentEntity::getDeptName, "技术部"))
    .orderByAsc("d", DepartmentEntity::getSortOrder)
    .orderByDesc("o", OrderEntity::getOrderTime);
IResultSet<UserEntity> joinResult = joinSelect.find(new EntityResultSetHandler<>(UserEntity.class));

// ---- 1.3 Lambda 分组聚合 + HAVING ----
Select groupSelect = Select.create()
    .field(UserEntity::getDeptId)
    .field(aggregate.COUNT(UserEntity::getId), "user_count")   // COUNT 聚合
    .field(aggregate.AVG(UserEntity::getAge), "avg_age")       // AVG 聚合
    .field(aggregate.SUM(UserEntity::getStatus), "total_status") // SUM 聚合
    .from(UserEntity.class)
    .groupBy(UserEntity::getDeptId)
    .having(Cond.create().gt(aggregate.COUNT(UserEntity::getId), 5)); // HAVING 条件

// ---- 1.4 Lambda Insert/Update/Delete ----
// Lambda 插入
Insert lambdaInsert = Insert.create(UserEntity.class)
    .field(UserEntity::getUsername, "lambda_user")
    .field(UserEntity::getEmail, "lambda@example.com")
    .field(UserEntity::getStatus, 1);

// Lambda 更新（支持 JOIN）
Update lambdaUpdate = Update.create(UserEntity.class)
    .field(UserEntity::getStatus, 2)
    .field(UserEntity::getNickname, "新昵称")
    .where(Cond.create().eq(UserEntity::getId, "user-001"));

// Lambda 删除（带复合条件）
Delete lambdaDelete = Delete.create(UserEntity.class)
    .where(Cond.create()
        .bracketBegin()
            .eq(UserEntity::getStatus, 0)
            .or().isNull(UserEntity::getEmail)
        .bracketEnd()
        .and().lt(UserEntity::getCreateTime, expireTime));

// ---- 1.5 Lambda Fields 构建 ----
Fields lambdaFields = Fields.of(
    UserEntity::getId,
    UserEntity::getUsername,
    UserEntity::getEmail
);
// 带表前缀
Fields lambdaFieldsWithPrefix = Fields.of("u",
    UserEntity::getId,
    UserEntity::getUsername
);

// ==================== 方式2：函数操作 Func（聚合/数学/字符串/日期）====================

// ---- 2.1 聚合函数（aggregate）：COUNT/MAX/MIN/AVG/SUM ----
Select aggSelect = Select.create()
    .field(aggregate.COUNT(UserEntity::getId), "total")           // COUNT(id)
    .field(aggregate.MAX(UserEntity::getAge), "max_age")          // MAX(age)
    .field(aggregate.MIN(UserEntity::getAge), "min_age")          // MIN(age)
    .field(aggregate.AVG(UserEntity::getAge), "avg_age")          // AVG(age)
    .field(aggregate.SUM(UserEntity::getStatus), "status_sum")    // SUM(status)
    .field(aggregate.COUNT_DISTINCT(UserEntity::getDeptId), "dept_count") // COUNT(DISTINCT dept_id)
    .from(UserEntity.class)
    .where(Cond.create().eq(UserEntity::getStatus, 1));

// ---- 2.2 数学函数（math）：ABS/ROUND/CEIL/FLOOR/MOD/POW ----
Select mathSelect = Select.create()
    .field(UserEntity::getId)
    .field(math.ABS(UserEntity::getAge), "abs_age")               // ABS(age)
    .field(math.ROUND(UserEntity::getAge, 0), "round_age")        // ROUND(age, 0)
    .field(math.CEIL(UserEntity::getAge), "ceil_age")             // CEIL(age)
    .field(math.FLOOR(UserEntity::getAge), "floor_age")           // FLOOR(age)
    .field(math.MOD(UserEntity::getStatus, 2), "status_mod")      // status % 2
    .from(UserEntity.class);

// ---- 2.3 字符串函数（strings）：UPPER/LOWER/LENGTH/CONCAT/SUBSTRING/TRIM ----
Select strSelect = Select.create()
    .field(UserEntity::getId)
    .field(strings.UPPER(UserEntity::getUsername), "upper_name")  // UPPER(username)
    .field(strings.LOWER(UserEntity::getEmail), "lower_email")    // LOWER(email)
    .field(strings.LENGTH(UserEntity::getUsername), "name_len")   // LENGTH(username)
    .field(strings.CONCAT(UserEntity::getUsername, strings.VAL(" <"), UserEntity::getEmail, strings.VAL(">")), "contact_info")
    .field(strings.SUBSTRING(UserEntity::getEmail, 1, 10), "email_prefix")
    .from(UserEntity.class);

// ---- 2.4 日期函数（dateTime）：NOW/YEAR/MONTH/DAY/DATE_FORMAT/DATEDIFF ----
Select dateSelect = Select.create()
    .field(UserEntity::getId)
    .field(dateTime.NOW(), "server_time")                         // NOW()
    .field(dateTime.YEAR(UserEntity::getCreateTime), "year")      // YEAR(create_time)
    .field(dateTime.MONTH(UserEntity::getCreateTime), "month")    // MONTH(create_time)
    .field(dateTime.DATE_FORMAT(UserEntity::getCreateTime, "%Y-%m-%d"), "fmt_date")
    .from(UserEntity.class);

// ---- 2.5 条件中使用函数：Func 作为 Cond 的左值/右值 ----
Cond funcCond = Cond.create()
    // WHERE LENGTH(username) > 5
    .gt(strings.LENGTH(UserEntity::getUsername), 5)
    // AND YEAR(create_time) = 2026
    .and().eq(dateTime.YEAR(UserEntity::getCreateTime), 2026)
    // AND UPPER(email) LIKE '%@EXAMPLE.COM'
    .and().like(strings.UPPER(UserEntity::getEmail), "%@EXAMPLE.COM");

// ---- 2.6 自定义函数 Func.create() ----
IFunction customFunc = Func.create("CUSTOM_HASH")
    .field(UserEntity::getPassword)
    .param("salt123");
Select customSelect = Select.create()
    .field(UserEntity::getId)
    .field(customFunc, "pwd_hash")
    .from(UserEntity.class);

// ==================== 方式3：传统字符串字段（兼容/动态SQL场景）====================

// ---- 3.1 FieldCondition + Where + Page（原方式1） ----
FieldCondition cond = UserEntity.conditionBuilder()
    .email().like(Like.create("@163.com").endsWith())
    .and()
    .status().eq(1);
Where where = Where.create(cond.build()).orderByDesc(UserEntity.FIELDS.CREATE_TIME);
IResultSet<UserEntity> page = new UserEntity().find(where, Page.create(1).pageSize(10));

// ---- 3.2 用 Cond 手动构建字符串条件 ----
Cond c2 = Cond.create()
    .gtEq(UserEntity.FIELDS.CREATE_TIME).param(startTime)
    .and().lt(UserEntity.FIELDS.CREATE_TIME).param(endTime);
IResultSet<UserEntity> list = UserEntity.builder().status(1).build()
    .find(Where.create(c2), Fields.create(UserEntity.FIELDS.ID, UserEntity.FIELDS.USERNAME));

// ---- 3.3 用 Select 构建复杂 Join 查询 ----
Select select = Select.create(UserEntity.class, "u")
    .field("u", UserEntity.FIELDS.ID)
    .field("u", UserEntity.FIELDS.USERNAME)
    .join(Join.inner("user_ext", "ue")
        .on(Cond.create().eqField(Fields.field("u", UserEntity.FIELDS.ID), Fields.field("ue", "uid"))))
    .where(Cond.create().eq("u", UserEntity.FIELDS.STATUS).param(1))
    .orderByDesc("u", UserEntity.FIELDS.CREATE_TIME)
    .page(Page.create(1).pageSize(20));
IResultSet<UserEntity> result = select.find(new EntityResultSetHandler<>(UserEntity.class));
```

> **核心函数分类速查表**（Func.xxx）：
> | 分类 | 常用方法 | 典型场景 |
> |---|---|---|
> | `aggregate` 聚合 | COUNT/MAX/MIN/AVG/SUM/COUNT_DISTINCT | 统计报表、分组计数 |
> | `math` 数学 | ABS/ROUND/CEIL/FLOOR/MOD/POW/SQRT | 数值计算、取整、四舍五入 |
> | `strings` 字符串 | UPPER/LOWER/LENGTH/CONCAT/SUBSTRING/TRIM/REPLACE | 大小写转换、拼接、截取 |
> | `dateTime` 日期 | NOW/YEAR/MONTH/DAY/DATE_FORMAT/DATEDIFF/DATE_ADD | 日期格式化、时间差计算 |
> | `controlFlow` 控制流 | IF/CASE_WHEN/COALESCE/NULLIF | 条件分支、空值默认值 |
> | `operators` 操作符 | ADD/SUBTRACT/MULTIPLY/DIVIDE | 字段间加减乘除运算 |
> | `comparison` 比较 | GREATEST/LEAST/ISNULL/IFNULL | 多值最大/最小、空值判断 |
> | `window` 窗口 | ROW_NUMBER/RANK/DENSE_RANK + Over(PARTITION BY) | 分组排名、TopN |
> | `Func.create(name)` | 自定义函数 | 数据库特定函数扩展 |

## 5. 配置速查

### 5.1 ymp-conf.properties最常改项≤15条

| 配置key | 默认值 | 说明 |
|---|---|---|
| `ymp.configs.persistence.jdbc.ds_default_name` | default | 默认数据源名称 |
| `ymp.configs.persistence.jdbc.ds_name_list` | default | 数据源名称列表，多数据源用'&#124;'分隔 |
| `ymp.configs.persistence.jdbc.ds.default.adapter_class` | default | 数据源适配器：default/dbcp/c3p0/druid/hikaricp/jndi或类全限定名 |
| `ymp.configs.persistence.jdbc.ds.default.connection_url` | - | 数据库连接URL（必填），如jdbc:mysql://localhost:3306/db |
| `ymp.configs.persistence.jdbc.ds.default.username` | - | 数据库用户名（必填） |
| `ymp.configs.persistence.jdbc.ds.default.password` | - | 数据库密码 |
| `ymp.configs.persistence.jdbc.ds.default.password_encrypted` | false | 密码是否已加密（需配合password_class处理器） |
| `ymp.configs.persistence.jdbc.ds.default.show_sql` | false | 是否打印执行的SQL语句 |
| `ymp.configs.persistence.jdbc.ds.default.auto_connection` | false | 模块初始化时是否自动建立连接 |
| `ymp.configs.persistence.jdbc.ds.default.table_prefix` | - | 表名前缀（多个用'&#124;'分隔），生成实体/拼SQL时自动处理 |
| `ymp.configs.persistence.jdbc.ds.default.type` | - | 数据库类型，可选mysql/oracle/sqlserver/sqlite/postgresql/h2/hsqldb/db2 |
| `ymp.configs.persistence.jdbc.ds.default.stack_traces` | false | 执行SQL时是否记录调用堆栈 |
| `ymp.configs.persistence.jdbc.ds.default.identifier_quote` | - | 自定义引用标识符（数据库关键字转义用） |
| `ymp.configs.persistence.jdbc.ds.default.config_file` | - | 连接池配置文件路径（如dbcp.properties） |
| `ymp.configs.persistence.jdbc.ds.default.dialect_class` | - | 自定义数据库方言实现类全限定名 |

### 5.2 启动注解核心参数

- `@EnableAutoScan`：无必选参数，必填。用于扫描@Entity实体类、@Repository接口、@Bean服务类。
- `@EnableBeanProxy`：无必选参数，事务必填。开启CGLIB代理，使@Transaction、@Cacheable等AOP注解生效。
- `@DatabaseConf`（可选，替代properties）：
  - `dsDefaultName`：默认数据源名
  - `value`：`@DatabaseDataSource[]`数组
- `@DatabaseDataSource`（可选）：
  - `name/connectionUrl/username/password/adapterClass/showSql/autoConnection/tablePrefix`

## 6. 常见坑点（3-6条）：现象 | 原因 | 解决

| 现象 | 原因 | 解决 |
|---|---|---|
| @Transaction标注的方法抛异常后数据未回滚 | 1. 类未加@Bean，未交给容器管理；2. 启动类未加@EnableBeanProxy（AOP代理不生效）；3. 方法非public/静态；4. 同类内部方法调用（自调用绕过代理） | 1. 类上@Bean；2. 启动类加@EnableBeanProxy；3. 事务方法必须public非静态；4. 自调用请通过application.getBeanFactory().getBean()获取代理对象调用，或拆分成两个Bean |
| 实体save()/update()执行成功但数据库无变化 | 1. 实体setId后没有设置@Id标注；2. 主键值为空且未配置autoincrement/useKeyGenerator；3. update时用stateWrapper只更新变化字段但实际值未变（影响行数0） | 1. 主键字段必须@Id+@Property组合；2. 非自增主键请手动id()或配置useKeyGenerator；3. 需要全量更新用update()不传Fields参数 |
| 使用@Repository接口调用时报nullPointerException/找不到SQL | 1. 接口没有被@EnableAutoScan扫描到（包路径不对）；2. item指定的key在yml/sql文件中不存在；3. configFile路径错误；4. 没有通过@Bean注入而是自己new的接口对象 | 1. 启动类包根路径确保覆盖Repository；2. 检查resources下yml/sql配置item名一致；3. configFile用相对classpath路径如repositories/xxx.yml；4. Repository通过@Inject或@Bean获取注入使用 |
| 查询find()分页返回Page.total=0但列表有数据 | 使用了自定义Select/原生SQL进行page分页但没有单独执行count语句，框架无法获知总数 | 若需要total字段，请先调用对应count()方法手动赋值，或使用继承BaseEntity的find(Where,Page)方式框架自动count |
| saveOrUpdate()在MySQL下报DuplicateKeyException | 表唯一索引不是主键，框架按主键判断存在与否，冲突唯一索引时insert失败 | 针对非主键唯一键冲突场景请改用saveIfNotExist()后再update()，或使用会话层SQL自己写INSERT ... ON DUPLICATE KEY UPDATE |
| 多数据源切换：Repository.save()仍然走default数据源 | 实体类没指定dataSourceName，或@Repository的dsName没配置，或调用entity时通过dbOwner()显式传入 | 1. 实体操作前builder().dbOwner(JDBC.get()).dataSourceName("oracledb")；2. Repository接口/方法上@Repository(dsName="oracledb")；3. openSession("oracledb", executor)会话指定数据源 |
