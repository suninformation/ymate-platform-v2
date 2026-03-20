---
name: ymp-persistence-jdbc
description: YMP框架JDBC持久化模块，针对关系型数据库数据存取的轻量级解决方案，支持ORM、多数据源、事务等
version: 2.1.4
author: YMP Team
category: persistence
tags:
  - java
  - jdbc
  - orm
  - persistence
  - database
trigger: 当用户需要使用关系型数据库、实现JDBC持久化、ORM操作、事务管理等场景时触发
tools:
  - jdbc
  - orm
  - database
examples:
  - 配置数据源
  - 实体类CRUD操作
  - 使用存储器
  - 事务管理
  - 批量操作
---

# JDBC 持久化组件技能指南

## 概述

JDBC 持久化模块是 YMP 框架中针对关系型数据库（RDBMS）数据存取的一套轻量级解决方案，主要关注数据存取的效率、易用性、稳定性和透明度。该模块基于 JDBC 框架 API 进行封装，提供了丰富的功能特性，简化了数据库操作的复杂性，使开发者能够更加专注于业务逻辑的实现。

### 核心功能特性

- 基于 JDBC 框架 API 进行轻量封装，结构简单、便于开发、调试和维护
- 优化批量数据更新、标准化结果集、预编译 SQL 语句处理
- 支持单实体 ORM 操作，无需编写 SQL 语句
- 提供脚手架工具，快速生成数据实体类，支持链式调用
- 支持通过存储器注解自定义 SQL 语句或从配置文件中动态加载 SQL 并自动执行
- 支持结果集与值对象的自动装配，支持自定义装配规则
- 支持多数据源，默认支持 C3P0、DBCP、Druid、HikariCP、JNDI 连接池配置，支持数据源扩展
- 支持多种数据库（如：Oracle、MySQL、SQLServer、SQLite、H2、PostgreSQL 等）
- 支持面向对象的数据库查询封装，有助于减少或降低程序编译期错误
- 支持数据库事务嵌套
- 支持数据库视图和存储过程
- 支持 Lambda 表达式进行类型安全的查询构建

## 架构设计

### 核心组件

JDBC 持久化模块采用分层架构设计，主要包含以下核心组件：

1. **模块管理**：`JDBC` 类作为模块的入口点和管理器，负责初始化和管理整个 JDBC 模块
2. **配置管理**：通过 `IDatabaseConfig` 和 `IDatabaseDataSourceConfig` 接口处理模块和数据源配置
3. **连接管理**：通过 `IDatabaseConnectionHolder` 和 `IDatabaseDataSourceAdapter` 接口管理数据库连接
4. **会话管理**：通过 `IDatabaseSession` 接口提供数据库操作会话，封装 SQL 执行逻辑
5. **实体管理**：通过实体类和注解支持对象关系映射
6. **事务管理**：通过 `Transactions` 类和 `@Transaction` 注解实现事务控制
7. **查询构建**：提供强大的查询构建器，用于构建复杂的 SQL 语句
8. **存储器支持**：提供存储器模式实现，用于数据访问层抽象
9. **结果集处理**：通过 `IResultSetHandler` 接口实现结果集的自定义处理

### 数据流向

JDBC 模块中的典型数据流向如下：

1. **初始化**：JDBC 模块使用配置参数进行初始化
2. **连接获取**：当请求数据库操作时，从数据源适配器获取连接
3. **会话创建**：创建数据库会话来处理操作
4. **SQL 执行**：会话执行 SQL 语句并处理结果
5. **连接释放**：操作完成后，连接被释放回连接池

## Maven 依赖

在项目中使用 JDBC 持久化模块，需要添加以下 Maven 依赖：

```xml
<dependency>
    <groupId>net.ymate.platform</groupId>
    <artifactId>ymate-platform-persistence-jdbc</artifactId>
    <version>2.1.4-dev</version>
</dependency>
```

## 模块配置

### 配置文件参数

JDBC 持久化模块的配置主要通过 `ymp.properties` 文件进行，以下是常用配置项：

```properties
#-------------------------------------
# JDBC 持久化模块初始化参数
#-------------------------------------

# 默认数据源名称, 默认值: default
ymp.configs.persistence.jdbc.ds_default_name=default

# 数据源列表, 多个数据源名称间用'|'分隔, 默认值: default
ymp.configs.persistence.jdbc.ds_name_list=default

# 是否自动连接, 即模块初始化时完成连接动作, 默认值: false
ymp.configs.persistence.jdbc.ds.default.auto_connection=true

# 是否显示执行的SQL语句, 默认值: false
ymp.configs.persistence.jdbc.ds.default.show_sql=true

# 是否开启堆栈跟踪, 默认值: false
ymp.configs.persistence.jdbc.ds.default.stack_traces=true

# 堆栈跟踪层级深度, 默认值: 0(即全部)
ymp.configs.persistence.jdbc.ds.default.stack_trace_depth=

# 堆栈跟踪包名前缀过滤, 默认值: 空
ymp.configs.persistence.jdbc.ds.default.stack_trace_packages=

# 自定义引用标识符, 根据数据库类型进行设置, 默认值: 空
ymp.configs.persistence.jdbc.ds.default.identifier_quote=

# 数据库表前缀名称, 多个前缀名称间用'|'分隔, 默认值: 空
ymp.configs.persistence.jdbc.ds.default.table_prefix=

# 数据源适配器, 可选值为已知适配器名称或自定义适配置类名称, 默认值: default
ymp.configs.persistence.jdbc.ds.default.adapter_class=dbcp

# 数据源适配器配置文件，可选参数，若未设置或设置的文件路径无效将被忽略，默认值为空
ymp.configs.persistence.jdbc.ds.default.config_file=

# 数据库类型, 可选参数, 默认值将通过连接字符串分析获得
ymp.configs.persistence.jdbc.ds.default.type=mysql

# 数据库方言, 可选参数, 自定义方言将覆盖默认配置
ymp.configs.persistence.jdbc.ds.default.dialect_class=

# 数据库连接驱动, 可选参数, 框架默认将根据数据库类型进行自动匹配
ymp.configs.persistence.jdbc.ds.default.driver_class=

# 数据库连接字符串, 必填参数
ymp.configs.persistence.jdbc.ds.default.connection_url=jdbc:mysql://localhost:3306/db_name?useUnicode=true&useSSL=false&characterEncoding=UTF-8

# 数据库访问用户名称, 必填参数
ymp.configs.persistence.jdbc.ds.default.username=root

# 数据库访问密码, 可选参数
ymp.configs.persistence.jdbc.ds.default.password=123456

# 数据库访问密码是否已加密, 默认值: false
ymp.configs.persistence.jdbc.ds.default.password_encrypted=false

# 数据库密码处理器, 可选参数, 用于对已加密码数据库访问密码进行解密, 默认值: 空
ymp.configs.persistence.jdbc.ds.default.password_class=
```

### 注解配置

除了配置文件外，JDBC 持久化模块还支持通过注解进行配置：

#### @DatabaseConf

| 配置项        | 描述           |
| ------------- | -------------- |
| dsDefaultName | 默认数据源名称 |
| value         | 数据源配置     |

#### @DatabaseDataSource

| 配置项             | 描述                     |
| ------------------ | ------------------------ |
| name               | 数据源名称               |
| connectionUrl      | 数据库连接字符串         |
| username           | 数据库访问用户名称       |
| password           | 数据库访问密码           |
| passwordEncrypted  | 数据库访问密码是否已加密 |
| passwordClass      | 数据库密码处理器         |
| type               | 数据库类型               |
| dialectClass       | 数据库方言               |
| adapterClass       | 数据源适配器             |
| configFile         | 数据源适配器配置文件     |
| driverClass        | 数据库默认驱动类名称     |
| autoConnection     | 是否自动连接             |
| showSql            | 是否显示执行的 SQL 语句    |
| stackTraces        | 是否开启堆栈跟踪         |
| stackTraceDepth    | 堆栈跟踪层级深度         |
| stackTracePackages | 堆栈跟踪过滤包名前缀集合 |
| tablePrefix        | 数据库表前缀名称         |
| identifierQuote    | 数据库引用标识符         |

**使用示例：**

```java
@DatabaseConf(dsDefaultName = "default", value = {
        @DatabaseDataSource(name = "default",
                            connectionUrl = "jdbc:mysql://localhost:3306/mydb",
                            username = "root",
                            password = "123456",
                            adapterClass = DBCPDataSourceAdapter.class,
                            autoConnection = true,
                            showSql = true)
})
public class DatabaseConfig {
}
```

## 数据源管理

### 多数据源支持

JDBC 持久化模块默认支持多数据源配置，可以在同一个应用中连接到不同的数据库：

```properties
# 定义两个数据源分别用于连接MySQL和Oracle数据库，同时指定默认数据源为default(即MySQL数据库)
ymp.configs.persistence.jdbc.ds_default_name=default
ymp.configs.persistence.jdbc.ds_name_list=default|oracledb

# 连接到MySQL数据库的数据源配置
ymp.configs.persistence.jdbc.ds.default.connection_url=jdbc:mysql://localhost:3306/mydb
ymp.configs.persistence.jdbc.ds.default.username=root
ymp.configs.persistence.jdbc.ds.default.password=123456

# 连接到Oracle数据库的数据源配置
ymp.configs.persistence.jdbc.ds.oracledb.connection_url=jdbc:oracle:thin:@localhost:1521:ORCL
ymp.configs.persistence.jdbc.ds.oracledb.username=ORCL
ymp.configs.persistence.jdbc.ds.oracledb.password=123456
```

### 连接池配置

JDBC 模块支持多种连接池实现，提供了灵活的选择：

| 名称     | 类型                      | 描述                                                         |
| -------- | ------------------------- | ------------------------------------------------------------ |
| default  | DefaultDataSourceAdapter  | 默认数据源适配器，通过 DriverManager 直接连接数据库，建议仅用于测试。 |
| c3p0     | C3P0DataSourceAdapter     | 基于 C3P0 连接池的数据源适配器。                             |
| dbcp     | DBCPDataSourceAdapter     | 基于 DBCP 连接池的数据源适配器。                             |
| druid    | DruidDataSourceAdapter    | 基于阿里巴巴开源的 Druid 连接池的数据源适配器。              |
| hikaricp | HikariCPDataSourceAdapter | 基于 HikariCP 连接池的数据源适配器。                         |
| jndi     | JNDIDataSourceAdapter     | 基于 JNDI 的数据源适配器。                                   |

连接池配置文件（如：`dbcp.properties`、`c3p0.properties` 等）应放置在工程的 `classpath` 根路径下。若配置文件不存在，JDBC 持久化模块在初始化时将自动创建。

### 数据库连接持有者

`IDatabaseConnectionHolder` 接口用于管理数据库连接，提供对各种数据库相关对象的访问：

```java
// 获取当前容器内JDBC模块实例
IDatabase database = application.getModuleManager().getModule(JDBC.class);
// 或通过静态方法获取
IDatabase database = JDBC.get();

// 获取默认数据源的连接持有者实例
IDatabaseConnectionHolder connectionHolder = database.getDefaultConnectionHolder();

// 获取指定名称的数据源连接持有者实例
connectionHolder = database.getConnectionHolder("oracledb");

// 获取连接对象
Connection connection = connectionHolder.getConnection();

// 获取数据源配置对象
IDatabaseDataSourceConfig dataSourceConfig = connectionHolder.getDataSourceConfig();

// 获取当前数据源适配器对象
IDatabaseDataSourceAdapter dataSourceAdapter = connectionHolder.getDataSourceAdapter();

// 获取当前数据库方言
IDialect dialect = connectionHolder.getDialect();

// 获取当前连接持有者所属JDBC模块实例
IDatabase owner = connectionHolder.getOwner();
```

## 数据实体

### 实体类结构

数据实体是以对象的形式与数据库表之间的一种映射关系，实体中的属性与表中字段一一对应。一个典型的实体类包含以下几个部分：

- **基本属性**：注解配置属性与字段之间的关系及属性的 Getter 和 Setter 方法
- **FIELDS**：字段名常量
- **Builder**：基本属性构建器类，支持以链式调用方式为实体属性赋值
- **FieldConditionBuilder**：属性条件构建器类，为具体实体属性构建字段查询条件

### 实体注解

JDBC 模块提供了一系列注解用于定义实体类和其属性：

#### @Entity

声明一个类为数据实体对象，指定实体名称（数据库表名称）。

#### @Id

声明一个类成员为主键，与 `@Property` 注解配合使用。

#### @Property

声明一个类成员为数据实体属性，可配置以下参数：

| 配置项          | 描述                                                         |
| --------------- | ------------------------------------------------------------ |
| name            | 实现属性名称，默认采用当前成员名称                           |
| autoincrement   | 是否为自动增长，默认为 `false`                               |
| sequenceName    | 序列名称，适用于类似 Oracle 等数据库，配合 `autoincrement` 参数一同使用 |
| useKeyGenerator | 指定键值生成器名称，默认为空表示不启用                       |
| nullable        | 允许为空，默认为 `true`                                      |
| unsigned        | 是否为无符号，默认为 `false`                                 |
| length          | 数据长度，默认为 `0` 表示不限制                              |
| decimals        | 小数位数，默认为 `0` 表示无小数                              |
| type            | 数据类型，默认为 `Type.FIELD.UNKNOWN`                        |

#### @PK

声明一个类为某数据实体的复合主键对象。

#### @Readonly

声明一个成员为只读属性，数据实体更新时其值将被忽略。

#### @Indexes 和 @Index

声明数据实体的索引。

#### @Comment

实体或成员属性的注释内容。

#### @Default

为一个成员属性指定默认值。

### 自动生成实体类

YMP 框架提供了 Maven 插件，可以通过数据库表结构自动生成实体类代码：

**步骤 1：** 配置数据实体代码生成器所需参数

```properties
#-------------------------------------
# JDBC 数据实体代码生成器配置参数
#-------------------------------------

# 是否生成新的BaseEntity类, 默认值: false
ymp.params.jdbc.use_base_entity=

# 是否使用类名后缀, 默认值: false
ymp.params.jdbc.use_class_suffix=true

# 实体类名后缀, 默认值: model
ymp.params.jdbc.class_suffix=entity

# 是否采用链式调用模式, 默认值: false
ymp.params.jdbc.use_chain_mode=true

# 为兼容历史数据库保持原表和字段名称的大小写，默认值: false
ymp.params.jdbc.keep_case=

# 自定义表或字段名称过滤器, 默认值: 空
ymp.params.jdbc.named_filter_class=

# 是否添加类成员属性值状态变化注解, 默认值: false
ymp.params.jdbc.use_state_support=true

# 数据库名称, 默认值: 空
ymp.params.jdbc.db_name=mydb

# 数据库用户名称, 默认值: 空
ymp.params.jdbc.db_username=root

# 数据库表名称前缀, 多个用'|'分隔, 默认值: 空
ymp.params.jdbc.table_prefix=ym_

# 否剔除生成的实体映射表名前缀, 默认值: false
ymp.params.jdbc.remove_table_prefix=true

# 预生成实体的数据表名称列表, 多个用'|'分隔, 默认值: 空(即全部生成)
ymp.params.jdbc.table_list=

# 排除的数据表名称列表, 在此列表内的数据表将不被生成实体, 多个用'|'分隔, 默认值: 空
ymp.params.jdbc.table_exclude_list=

# 需要添加@Readonly注解声明的字段名称列表, 多个用'|'分隔, 默认值: 空
ymp.params.jdbc.readonly_field_list=create_time

# 生成的代码文件输出路径, 默认值: ${root}/src/main/java
ymp.params.jdbc.output_path=

# 生成的代码所属包名称, 默认值: packages
ymp.params.jdbc.package_name=
```

**步骤 2：** 添加插件配置

```xml
<plugin>
    <groupId>net.ymate.maven.plugins</groupId>
    <artifactId>ymate-maven-plugin</artifactId>
    <version>1.0.2</version>
    <dependencies>
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>8.0.32</version>
        </dependency>
        <!-- 其他数据库驱动 -->
    </dependencies>
</plugin>
```

**步骤 3：** 执行插件命令

```shell
mvn ymate:entity -Doverwrite=true
```

### 实体操作

#### 插入（Insert）

```java
// 创建一个新的用户实体
UserEntity user = UserEntity.builder()
    .id(UUIDUtils.UUID())
    .username("suninformation")
    .nickname("有理想的鱼")
    .password(DigestUtils.md5Hex("123456"))
    .email("suninformation@163.com")
    .createTime(System.currentTimeMillis())
    .lastModifyTime(System.currentTimeMillis())
    .build();

// 执行数据插入
user.save();

// 插入时指定/排除某些字段
user.save(Fields.create(UserEntity.FIELDS.NICKNAME, UserEntity.FIELDS.EMAIL).excluded(true));

// 插入前判断记录是否已存在，若已存在则执行记录更新操作
user.saveOrUpdate();

// 插入前判断记录是否已存在，若已存在则执行记录更新操作时仅更新指定的字段
user.saveOrUpdate(Fields.create(UserEntity.FIELDS.NICKNAME, UserEntity.FIELDS.EMAIL));
```

#### 更新（Update）

```java
// 常规更新
UserEntity user = UserEntity.builder()
    .id("bc19f5645aa9438089c5e9954e5f1ac5")
    .password(DigestUtils.md5Hex("654321"))
    .gender("F")
    .build();

// 执行记录更新
user.update();

// 仅更新指定的字段/排除某些字段
user.update(Fields.create(UserEntity.FIELDS.PASSWORD));

// 仅更新值发生变化的字段
UserEntity user = UserEntity.builder()
    .id("bc19f5645aa9438089c5e9954e5f1ac5").build().load();

EntityStateWrapper<UserEntity> stateWrapper = user.stateWrapper();
stateWrapper.getEntity().bind()
    .password(DigestUtils.md5Hex("654321"))
    .gender("M");

stateWrapper.update();
```

#### 查询（Find）

```java
// 根据记录ID加载
UserEntity user = UserEntity.builder()
    .id("bc19f5645aa9438089c5e9954e5f1ac5")
    .build().load();

// 根据记录ID加载指定的字段
user = user.load(Fields.create(UserEntity.FIELDS.USERNAME, UserEntity.FIELDS.NICKNAME));

// 根据实体属性设置条件
UserEntity user = UserEntity.builder()
    .username("suninformation")
    .email("suninformation@163.com")
    .build();

IResultSet<UserEntity> users = user.find();

// 自定义条件并分页查询
FieldCondition cond = UserEntity.conditionBuilder().email().like(Like.create("@163.com").endsWith());
Where where = Where.create(cond.build()).orderByDesc(UserEntity.FIELDS.CREATE_TIME);
IResultSet<UserEntity> users = new UserEntity().find(where, Page.create(1).pageSize(10));

// 返回符合条件的第一条记录
UserEntity user = UserEntity.builder()
    .username("suninformation")
    .build().findFirst();

// 统计符合条件的记录数
long count = user.count();
```

#### 删除（Delete）

```java
// 根据实体主键删除记录
UserEntity user = UserEntity.builder()
    .id("bc19f5645aa9438089c5e9954e5f1ac5")
    .build().delete();

// 根据实体属性进行有条件删除
UserEntity user = UserEntity.builder()
    .username("suninformation")
    .password(DigestUtils.md5Hex("654321"))
    .build().delete();
```

## 事务处理

### @Transaction 注解

JDBC 持久化模块对数据库事务的处理是基于 YMP 框架的 AOP 特性实现的，任何被应用容器管理的对象都可以通过 `@Transaction` 注解开启事务：

```java
@Bean
public class UserServiceImpl implements IUserService {

    @Override
    @Transaction
    public boolean login(String username, String pwd) throws Exception {
        UserEntity user = findUser(username, pwd);
        if (user != null) {
            long now = System.currentTimeMillis();
            user.bind().lastModifyTime(now).build()
                    .update(Fields.create(UserEntity.FIELDS.LAST_MODIFY_TIME));
            return true;
        }
        return false;
    }

    @Override
    @Transaction(level = Type.TRANSACTION.REPEATABLE_READ)
    public void transferFunds(String fromUserId, String toUserId, double amount) throws Exception {
        // 业务逻辑
    }
}
```

### 手动事务管理

对于更复杂的事务场景，可以使用 `Transactions` 类手动管理事务：

```java
// 无返回值事务
Transactions.execute(new ITrade() {
    @Override
    public void deal() throws Throwable {
        // 业务逻辑
    }
});

// 有返回值事务
UserEntity userEntity = Transactions.execute(new AbstractTrade<UserEntity>() {
    private UserEntity user;

    @Override
    public void deal() throws Throwable {
        // 业务逻辑
        user = UserEntity.builder()
            .id(UUIDUtils.UUID())
            .username("newuser")
            .build();
        user.save();
    }

    @Override
    public UserEntity getResult() {
        return user;
    }
});

// 指定事务隔离级别
Transactions.execute(Type.TRANSACTION.SERIALIZABLE, new ITrade() {
    @Override
    public void deal() throws Throwable {
        // 业务逻辑
    }
});
```

### 事务嵌套

JDBC 模块支持事务嵌套，当事务嵌套时：

1. 最外层事务决定事务隔离级别
2. 内部事务与外部事务共享同一个连接
3. 提交操作延迟到最外层事务提交时执行
4. 任何嵌套事务中的回滚都会导致整个事务回滚

## 会话管理

会话是对应用中具体业务操作触发的一系列与数据库之间的交互过程的封装：

```java
// 使用默认数据源开启会话
UserEntity userEntity = JDBC.get().openSession(new IDatabaseSessionExecutor<UserEntity>() {
    public UserEntity execute(IDatabaseSession session) throws Exception {
        // 业务逻辑
        return session.findFirst(EntitySQL.create(UserEntity.class));
    }
});

// 使用指定的数据源开启会话
IResultSet<UserEntity> users = JDBC.get().openSession("oracledb", new IDatabaseSessionExecutor<IResultSet<UserEntity>>() {
    public IResultSet<UserEntity> execute(IDatabaseSession session) throws Exception {
        // 业务逻辑
        return session.find(EntitySQL.create(UserEntity.class));
    }
});

// 使用 try-with-resources
try (IDatabaseSession session = JDBC.get().openSession()) {
    // 业务逻辑
}
```

## 数据库会话事件监听器

通过实现 `IDatabaseSessionEventListener` 接口，可以监听数据库会话中的 CRUD 操作事件。此外，还可以在事件触发时对 SQL 语句和参数进行修改等操作，从而实现 SQL 拦截等功能。该监听器需要在数据库会话中手动设置。

### 事件类型

| 事件方法 | 说明 |
|---|---|
| `onQueryBefore` | 查询操作执行前触发 |
| `onQueryAfter` | 查询操作执行后触发 |
| `onInsertBefore` | 插入操作执行前触发 |
| `onInsertAfter` | 插入操作执行后触发 |
| `onInsertIfNotExistBefore` | 插入（记录不存在时）操作执行前触发 |
| `onInsertIfNotExistAfter` | 插入（记录不存在时）操作执行后触发 |
| `onUpdateBefore` | 更新操作执行前触发 |
| `onUpdateAfter` | 更新操作执行后触发 |
| `onUpsertBefore` | 更新插入操作执行前触发 |
| `onUpsertAfter` | 更新插入操作执行后触发 |
| `onRemoveBefore` | 删除操作执行前触发 |
| `onRemoveAfter` | 删除操作执行后触发 |

### 事件上下文

`DatabaseSessionEventContext` 事件上下文提供以下方法：

| 方法 | 说明 |
|---|---|
| `getSource()` | 获取数据库会话对象（`IDatabaseSession`） |
| `getOperationType()` | 获取操作类型（`Type.OPT` 枚举） |
| `getSql()` | 获取执行的 SQL 语句字符串 |
| `setSql(String sql)` | 设置 SQL 语句字符串（可在 before 事件中修改） |
| `getParams()` | 获取 SQL 参数（`Params` 对象） |
| `setParams(Params params)` | 设置 SQL 参数（可在 before 事件中修改） |
| `getBatchSQL()` | 获取批量 SQL 对象（`BatchSQL`） |
| `setBatchSQL(BatchSQL batchSQL)` | 设置批量 SQL 对象（可在 before 事件中修改） |
| `putAttribute(String key, Object value)` | 存储自定义属性 |
| `getAttribute(String key)` | 获取自定义属性 |
| `getAttributes()` | 获取所有自定义属性 |

**特别说明**：在 `after` 事件中，可以通过 `getAttribute(IOperator.class.getName())` 获取当前操作器接口对象，从而获取执行结果，例如：
- `IQueryOperator`：查询操作器，可获取查询结果
- `IUpdateOperator`：更新操作器，可获取影响行数
- `IDeleteOperator`：删除操作器，可获取影响行数

### 使用示例

```java
import net.ymate.platform.persistence.jdbc.IDatabaseSessionEventListener;
import net.ymate.platform.persistence.jdbc.DatabaseSessionEventContext;
import net.ymate.platform.persistence.jdbc.IOperator;
import net.ymate.platform.persistence.jdbc.IQueryOperator;
import net.ymate.platform.persistence.jdbc.IUpdateOperator;
import net.ymate.platform.persistence.jdbc.IDeleteOperator;

public class DatabaseSessionEventListenerImpl implements IDatabaseSessionEventListener {

    @Override
    public void onQueryBefore(DatabaseSessionEventContext eventContext) {
        System.out.println("执行查询前 - SQL: " + eventContext.getSql());
    }

    @Override
    public void onQueryAfter(DatabaseSessionEventContext eventContext) {
        System.out.println("执行查询后 - SQL: " + eventContext.getSql());
        // 获取查询操作器，获取执行结果
        IOperator operator = (IOperator) eventContext.getAttribute(IOperator.class.getName());
        if (operator instanceof IQueryOperator) {
            IQueryOperator queryOperator = (IQueryOperator) operator;
            // 可以通过 queryOperator 获取查询结果
            System.out.println("查询操作器类型: " + queryOperator.getClass().getName());
        }
    }

    @Override
    public void onInsertBefore(DatabaseSessionEventContext eventContext) {
        System.out.println("执行插入前 - SQL: " + eventContext.getSql());
    }

    @Override
    public void onInsertAfter(DatabaseSessionEventContext eventContext) {
        System.out.println("执行插入后 - SQL: " + eventContext.getSql());
        // 获取更新操作器，获取影响行数
        IOperator operator = (IOperator) eventContext.getAttribute(IOperator.class.getName());
        if (operator instanceof IUpdateOperator) {
            IUpdateOperator updateOperator = (IUpdateOperator) operator;
            System.out.println("插入影响行数: " + updateOperator.getEffectCounts());
        }
    }

    @Override
    public void onUpdateBefore(DatabaseSessionEventContext eventContext) {
        System.out.println("执行更新前 - SQL: " + eventContext.getSql());
    }

    @Override
    public void onUpdateAfter(DatabaseSessionEventContext eventContext) {
        System.out.println("执行更新后 - SQL: " + eventContext.getSql());
        // 获取更新操作器，获取影响行数
        IOperator operator = (IOperator) eventContext.getAttribute(IOperator.class.getName());
        if (operator instanceof IUpdateOperator) {
            IUpdateOperator updateOperator = (IUpdateOperator) operator;
            System.out.println("更新影响行数: " + updateOperator.getEffectCounts());
        }
    }

    @Override
    public void onRemoveBefore(DatabaseSessionEventContext eventContext) {
        System.out.println("执行删除前 - SQL: " + eventContext.getSql());
    }

    @Override
    public void onRemoveAfter(DatabaseSessionEventContext eventContext) {
        System.out.println("执行删除后 - SQL: " + eventContext.getSql());
        // 获取删除操作器，获取影响行数
        IOperator operator = (IOperator) eventContext.getAttribute(IOperator.class.getName());
        if (operator instanceof IDeleteOperator) {
            IDeleteOperator deleteOperator = (IDeleteOperator) operator;
            System.out.println("删除影响行数: " + deleteOperator.getEffectCounts());
        }
    }

    @Override
    public void onInsertIfNotExistBefore(DatabaseSessionEventContext eventContext) {
        System.out.println("执行插入(如果记录不存在)前 - SQL: " + eventContext.getSql());
    }

    @Override
    public void onInsertIfNotExistAfter(DatabaseSessionEventContext eventContext) {
        System.out.println("执行插入(如果记录不存在)后 - SQL: " + eventContext.getSql());
        // 获取更新操作器，获取影响行数
        IOperator operator = (IOperator) eventContext.getAttribute(IOperator.class.getName());
        if (operator instanceof IUpdateOperator) {
            IUpdateOperator updateOperator = (IUpdateOperator) operator;
            System.out.println("插入影响行数: " + updateOperator.getEffectCounts());
        }
    }

    @Override
    public void onUpsertBefore(DatabaseSessionEventContext eventContext) {
        System.out.println("执行更新插入前 - SQL: " + eventContext.getSql());
    }

    @Override
    public void onUpsertAfter(DatabaseSessionEventContext eventContext) {
        System.out.println("执行更新插入后 - SQL: " + eventContext.getSql());
        // 获取更新操作器，获取影响行数
        IOperator operator = (IOperator) eventContext.getAttribute(IOperator.class.getName());
        if (operator instanceof IUpdateOperator) {
            IUpdateOperator updateOperator = (IUpdateOperator) operator;
            System.out.println("更新插入影响行数: " + updateOperator.getEffectCounts());
        }
    }
}
```

**重要说明**：如果在任何 `before` 事件方法中抛出异常，将中止当前数据库操作，不会执行实际的数据库操作，也不会调用对应的 `after` 事件方法。这可以用于实现数据校验、权限控制等功能。例如：

```java
@Override
public void onInsertBefore(DatabaseSessionEventContext eventContext) throws Exception {
    // 检查参数中是否包含敏感数据
    if (eventContext.getParams() != null) {
        for (Object param : eventContext.getParams().params()) {
            if ("sensitive_data".equals(param)) {
                throw new RuntimeException("检测到敏感数据，操作被中止");
            }
        }
    }
}
```

在会话中设置监听器：

```java
JDBC.get().openSession(session -> {
    // 设置事件监听器
    session.setSessionEventListener(new DatabaseSessionEventListenerImpl());

    // 执行数据库操作，事件监听器将被触发
    UserEntity user = new UserEntity();
    user.setId("user_001");
    user.setUsername("test");
    session.insert(user);

    // 查询
    UserEntity loadedUser = session.findFirst(EntitySQL.create(UserEntity.class));

    // 更新
    loadedUser.setNickname("测试用户");
    session.update(loadedUser);

    // 删除
    session.delete(UserEntity.class, user.getId());

    return null;
});
```

## 查询构建

### Fields：字段名称集合

用于辅助拼接数据表字段名称等，支持自定义前缀和别名：

```java
// 创建 Fields 对象
Fields fields = Fields.create(UserEntity.FIELDS.USERNAME, "password");

// 添加带前缀和别名
fields.add("u", UserEntity.FIELDS.EMAIL, "e");

// 添加带前缀
fields = Fields.create().add("u", UserEntity.FIELDS.ID).add(fields);

// 标记集合中的字段为排除的
fields.excluded(true);
```

### Cond：条件参数

用于生成 SQL 条件语句并存储条件参数：

```java
// 基本条件
Cond cond = Cond.create()
    .eq(UserEntity.FIELDS.USERNAME).param("admin")
    .and().eq(UserEntity.FIELDS.PASSWORD).param("123456");

// 范围条件
cond = Cond.create()
    .gt(UserEntity.FIELDS.AGE).param(18)
    .and().lt(UserEntity.FIELDS.AGE).param(30);

// 模糊查询
cond = Cond.create()
    .like(UserEntity.FIELDS.USERNAME).param(Like.create("test").contains());

// 字段比较
cond = Cond.create()
    .eqField(UserEntity.FIELDS.ID, UserExtEntity.FIELDS.UID);
```

### Select：查询语句对象

用于构建 SELECT 数据库查询语句：

```java
// 基本查询
Select select = Select.create(UserEntity.class, "u")
    .field("u", UserEntity.FIELDS.ID)
    .field("u", UserEntity.FIELDS.USERNAME)
    .where(Cond.create().gtEq("u", UserEntity.FIELDS.AGE).param(18))
    .orderByDesc("u", UserEntity.FIELDS.CREATE_TIME)
    .page(Page.create());

// 执行查询
IResultSet<UserEntity> users = select.find(new EntityResultSetHandler<>(UserEntity.class));

// 子查询
Select subSelect = Select.create().from("user", "u").where(Cond.create().gtEq("u", "age").param(20));
Select select = Select.create(subSelect.alias("u"))
    .field(Func.aggregate.MAX(Fields.field("u", "age")))
    .groupBy("u", "age");
```

### Insert：插入语句对象

用于构建 INSERT 数据插入语句：

```java
// 单记录插入
Insert insert = Insert.create(UserEntity.class)
    .field(UserEntity.FIELDS.ID).param(UUIDUtils.UUID())
    .field(UserEntity.FIELDS.USERNAME).param("suninformation")
    .field(UserEntity.FIELDS.NICKNAME).param("有理想的鱼")
    .field(UserEntity.FIELDS.PASSWORD).param("123456")
    .field(UserEntity.FIELDS.EMAIL).param("suninformation@163.com");

// 批量插入
Insert insert = Insert.create(UserEntity.class)
    .field(UserEntity.FIELDS.ID)
    .field(UserEntity.FIELDS.USERNAME)
    .field(UserEntity.FIELDS.NICKNAME)
    .addGroupParam(Params.create("1", "用户A", "昵称A"))
    .addGroupParam(Params.create("2", "用户B", "昵称B"));

// 执行插入
int effectCount = insert.execute();
```

### Update：更新语句对象

用于构建 UPDATE 数据更新语句：

```java
// 常规更新
Update update = Update.create().table(UserEntity.class)
    .field(UserEntity.FIELDS.AGE).param(18)
    .where(Cond.create().lt(UserEntity.FIELDS.AGE).param(18));

// 执行更新
int effectCount = update.execute();
```

### Delete：删除语句对象

用于构建 DELETE 数据删除语句：

```java
// 单表删除
Delete delete = Delete.create()
    .from(UserEntity.class)
    .where(Cond.create().lt(UserEntity.FIELDS.AGE).param(18));

// 执行删除
int effectCount = delete.execute();
```

### Join：连接对象

用于生成 SQL 语句中的 JOIN 子句：

```java
// 内连接
Join join = Join.inner("user_ext").alias("ue")
    .on(Cond.create().eqField(Fields.field("u", UserEntity.FIELDS.ID), Fields.field("ue", UserExtEntity.FIELDS.UID)));

Select select = Select.create("user", "u")
    .join(join)
    .where(Cond.create().gtEq("u", "age").param(18));
```

### Func：函数

在编写 SQL 语句时，经常会用到由数据库提供（或根据业务自定义）的一系列函数来处理数据查询等操作，为了能够像编写 Java 代码一样在对象查询的 SQL 语句中使用函数，JDBC模块提供了一种简单的函数封装方法，同时也封装了一些比较常用的函数（目前这些函数封装主要针对 MySQL 数据库实现，其它类型数据库的支持也将在未来版本中逐步完善），其中主要包括：常规运算、数学计算、字符操作、聚合分组、日期时间和流程控制等相关函数。

#### 常规运算函数：Func.Operators

| 函数     | 代码                                                         | 描述 |
| -------- | ------------------------------------------------------------ | ---- |
| brackets | `Func.operators.brackets("x")`                               | 括号 |
| quotes   | `Func.operators.quotes("x")`                                 | 引号 |
| addition | `Func.operators.addition(n)`<br />`Func.operators.addition("x")`<br />`Func.operators.addition("x", n)`<br />`Func.operators.addition("x", 'y')` | 加法 |
| subtract | `Func.operators.subtract(n)`<br />`Func.operators.subtract("x")`<br />`Func.operators.subtract("x", n)`<br />`Func.operators.subtract(n, 'x')`<br />`Func.operators.subtract("x", 'y')` | 减法 |
| multiply | `Func.operators.multiply(n)`<br />`Func.operators.multiply("x")`<br />`Func.operators.multiply("x", n)`<br />`Func.operators.multiply("x", 'y')` | 乘法 |
| divide   | `Func.operators.divide(n)`<br />`Func.operators.divide("x")`<br />`Func.operators.divide("x", n)`<br />`Func.operators.divide(n, 'x')`<br />`Func.operators.divide("x", 'y')` | 除法 |

#### 数学计算类函数：Func.Math

| 函数     | 代码                                                        | 描述                                                         |
| -------- | ----------------------------------------------------------- | ------------------------------------------------------------ |
| ABS      | `Func.math.ABS("x")`                                        | X 的绝对值                                                   |
| ACOS     | `Func.math.ACOS("x")`                                       | X 的反余弦                                                   |
| ASIN     | `Func.math.ASIN("x")`                                       | X 的反正弦                                                   |
| ATAN     | `Func.math.ATAN("x")`                                       | X 的反正切                                                   |
| CEILING  | `Func.math.CEILING("x")`                                    | 不小于 X 的最小整数值                                        |
| CONV     | `Func.math.CONV("x", 10, 2)`                                | 进制转换                                                     |
| COS      | `Func.math.COS("x")`                                        | X 的余弦                                                     |
| COT      | `Func.math.COT("x")`                                        | X 的余切                                                     |
| CRC32    | `Func.math.CRC32("x")`                                      | 计算循环冗余码校验值并返回一个32比特无符号值                 |
| DEGREES  | `Func.math.DEGREES("x")`                                    | X 由弧度被转化为度                                           |
| EXP      | `Func.math.EXP("x")`                                        | e 的 X 乘方后的值（自然对数的底）                            |
| FLOOR    | `Func.math.FLOOR("x")`                                      | 不大于 X 的最大整数值                                        |
| LN       | `Func.math.LN("x")`                                         | X 的自然对数，即 X 相对于基数 e 的对数                       |
| LOG      | `Func.math.LOG("x")`<br />`Func.math.LOG("b", "x")`        | X 的自然对数<br />X 对于任意基数 B 的对数                    |
| LOG10    | `Func.math.LOG10("x")`                                      | X 的常用对数（以 10 为底）                                   |
| LOG2     | `Func.math.LOG2("x")`                                       | X 的以 2 为底的对数                                          |
| MOD      | `Func.math.MOD("n", "m")`                                   | N 被 M 除后的余数                                            |
| PI       | `Func.math.PI()`                                            | PI 的值（默认的显示小数位数是7位）                           |
| POW      | `Func.math.POW("y", "x")`                                   | X 的 Y 乘方的结果值                                          |
| POWER    | `Func.math.POWER("y", "x")`                                 | 同 POW 函数                                                  |
| RADIANS  | `Func.math.RADIANS("x")`                                    | 由度转化为弧度的参数 X                                       |
| RAND     | `Func.math.RAND()`<br />`Func.math.RAND("n")`               | 随机浮点值 v ，范围在 0 到1 之间 (即, 其范围为 0 ≤ v ≤ 1.0)<br />指定一个整数参数 N ，它被用作种子值，用来产生重复序列 |
| ROUND    | `Func.math.ROUND("x")`<br />`Func.math.ROUND("x", d)`       | X 值接近于最近似的整数<br />X 值保留到小数点后 D 位并四舍五入<br />（若要直接保留 X 值小数点左边的 D 位，可将 D 设为负值） |
| SIGN     | `Func.math.SIGN("n")`                                       | X 值的符号（负数、零或正）对应 -1，0或1                      |
| SIN      | `Func.math.SIN("x")`                                        | X 的正弦                                                     |
| SQRT     | `Func.math.SQRT("x")`                                       | 非负数 X 的二次方根                                          |
| TAN      | `Func.math.TAN("x")`                                        | X 的正切                                                     |
| TRUNCATE | `Func.math.TRUNCATE("x")`<br />`Func.math.TRUNCATE("x", d)` | 舍去至小数点后 D 位的数字 X<br />若 D 的值为 0，则结果不带有小数点或不带有小数部分<br />可以将 D 设为负数，若要截去 X 小数点左起第 D 位开始后面所有低位的值 |

#### 字符操作类函数：Func.Strings

| 函数             | 代码                                                         | 描述                                                         |
| ---------------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| ASCII            | `Func.strings.ASCII(str)`                                    | 字符串 str 的最左字符的数值                                  |
| BIN              | `Func.strings.BIN(n)`                                        | N 的二进制值的字符串表示                                     |
| BIN_LENGTH       | `Func.strings.BIN_LENGTH(str)`                               | 二进制的字符串 str 长度                                      |
| CHAR             | `Func.strings.CHAR(...n)`                                    | 将 n 个整数代码所对应的字符组成的字符串                      |
| CHAR_LENGTH      | `Func.strings.CHAR_LENGTH(str)`                              | 字符串 str 的长度，长度的单位为字符                          |
| CHARACTER_LENGTH | `Func.strings.CHARACTER_LENGTH(str)`                         | 与 CHAR_LENGTH 同                                            |
| CONCAT           | `Func.strings.CONCAT(str1, str2...n)`                        | 连接多个 str 参数产生的字符串                                |
| CONCAT_WS        | `Func.strings.CONCAT_WS(sp, str1, str2...n)`                 | 与 CONCAT 同，第一个参数用于指定分隔符                       |
| ELT              | `Func.strings.ELT(n, str1...n)`                              | 返回第 N 个字符串                                            |
| FIELD            | `Func.strings.FIELD(str, ...n)`                              | 返回 str 在列表中的位置索引                                  |
| FIND_IN_SET      | `Func.strings.FIND_IN_SET(x, strlist)`                       | 返回字符串 x 在字符串列表 strlist 中的位置                   |
| FORMAT           | `Func.strings.FORMAT(x, d)`                                  | 将数字 x 格式化为 '#,###,###.##' 格式，保留 d 位小数         |
| HEX              | `Func.strings.HEX(str)`                                      | 将字符串 str 转换为十六进制                                  |
| FROM_BASE64      | `Func.strings.FROM_BASE64(str)`                              | 将 Base64 编码的字符串解码                                   |
| TO_BASE64        | `Func.strings.TO_BASE64(str)`                                | 将字符串 str 编码为 Base64                                   |
| INSERT           | `Func.strings.INSERT(str, pos, len, newstr)`                 | 在字符串 str 的 pos 位置插入 newstr，替换 len 个字符         |
| INSTR            | `Func.strings.INSTR(str, substr)`                            | 字符串 str 中子字符串的第一个出现位置                        |
| LEFT             | `Func.strings.LEFT(str, len)`                                | 字符串 str 开始的 len 最左字符                               |
| LENGTH           | `Func.strings.LENGTH(str)`                                   | 字符串 str 的长度，单位为字节                                |
| LOAD_FILE        | `Func.strings.LOAD_FILE(str)`                                | 读取文件并将这一文件按照字符串的格式返回                     |
| LOCATE           | `Func.strings.LOCATE(substr, str)`<br />`Func.strings.LOCATE(substr, str, pos)` | 返回子字符串 substr 在字符串 str 中第一次出现的位置          |
| LOWER            | `Func.strings.LOWER(str)`                                    | 返回字符串 str，以及根据最新字符集映射转化为小写字母的字符   |
| LPAD             | `Func.strings.LPAD(str, len, padstr)`                        | 返回字符串 str，其左边被字符串 padstr 填补至 len 字符长度    |
| LTRIM            | `Func.strings.LTRIM(str)`                                    | 返回字符串 str，起始空格字符被删去                           |
| OCT              | `Func.strings.OCT(n)`                                        | N 的八进制值的字符串表示                                     |
| ORD              | `Func.strings.ORD(str)`                                      | 若字符串 str 的最左字符是一个多字节字符，则返回该字符的代码  |
| QUOTE            | `Func.strings.QUOTE(str)`                                    | 引证一个字符串，由此产生一个在 SQL 语句中可用作完全转义数据值的结果 |
| REPEAT           | `Func.strings.REPEAT(str, count)`                            | 返回一个由重复的字符串 str 组成的字符串，重复次数为 count    |
| REPLACE          | `Func.strings.REPLACE(str, fromStr, toStr)`                  | 替换字符串 str 中所有的 fromStr 为 toStr                  |
| REVERSE          | `Func.strings.REVERSE(str)`                                  | 返回字符串 str，顺序和字符顺序相反                         |
| RIGHT            | `Func.strings.RIGHT(str, len)`                               | 从字符串 str 开始，返回最右 len 字符                       |
| RPAD             | `Func.strings.RPAD(str, len, padstr)`                        | 返回字符串 str，其右边被字符串 padstr 填补至 len 字符长度  |
| RTRIM            | `Func.strings.RTRIM(str)`                                    | 返回字符串 str，结尾空格字符被删去                         |
| SOUNDEX          | `Func.strings.SOUNDEX(str)`                                  | 从 str 返回一个 soundex 字符串                             |
| SPACE            | `Func.strings.SPACE(n)`                                      | 返回一个由 N 间隔符号组成的字符串                          |
| STRCMP           | `Func.strings.STRCMP(expr1, expr2)`                          | 若所有的字符串均相同，则返回 0，若第一个参数小于第二个，则返回 -1，其它情况返回 1 |
| SUBSTRING        | `Func.strings.SUBSTRING(str, pos)`<br />`Func.strings.SUBSTRING(str, pos, len)` | 从字符串 str 返回一个子字符串，起始于位置 pos，可选长度 len |
| SUBSTRING_INDEX  | `Func.strings.SUBSTRING_INDEX(str, delim, count)`            | 在定界符 delim 以及 count 出现前，从字符串 str 返回子字符串 |
| TRIM             | `Func.strings.TRIM(str)`                                     | 返回字符串 str，其中所有前缀和/或后缀都已被删除            |
| TRIM_BOTH        | `Func.strings.TRIM_BOTH(remstr, str)`                        | 返回字符串 str，其中所有 remstr 前缀和后缀都已被删除       |
| TRIM_LEADIN      | `Func.strings.TRIM_LEADIN(remstr, str)`                      | 返回字符串 str，其中所有 remstr 前缀都已被删除             |
| TRIM_TRAILING    | `Func.strings.TRIM_TRAILING(remstr, str)`                    | 返回字符串 str，其中所有 remstr 后缀都已被删除             |
| UNHEX            | `Func.strings.UNHEX(str)`                                    | 执行从 HEX(str) 的反向操作，将十六进制数字转化为字符       |
| UPPER            | `Func.strings.UPPER(str)`                                    | 返回字符串 str，以及根据最新字符集映射转化为大写字母的字符 |
| REGEXP_INSTR     | `Func.strings.REGEXP_INSTR(str, pattern)`<br />`Func.strings.REGEXP_INSTR(str, pattern, pos, occurrence, return_end_opt, match_type)` | 返回字符串 str 中匹配正则表达式 pattern 的子串的起始位置   |
| REGEXP_LIKE      | `Func.strings.REGEXP_LIKE(str, pattern)`<br />`Func.strings.REGEXP_LIKE(str, pattern, match_type)` | 检查字符串 str 是否匹配正则表达式 pattern                  |
| REGEXP_REPLACE   | `Func.strings.REGEXP_REPLACE(str, pattern, replacement)`<br />`Func.strings.REGEXP_REPLACE(str, pattern, replacement, pos, occurrence, match_type)` | 替换字符串 str 中匹配正则表达式 pattern 的子串             |
| REGEXP_SUBSTR    | `Func.strings.REGEXP_SUBSTR(str, pattern)`<br />`Func.strings.REGEXP_SUBSTR(str, pattern, pos, occurrence, match_type)` | 返回字符串 str 中匹配正则表达式 pattern 的子串             |

#### 聚合分组类函数：Func.Aggregate

| 函数         | 代码                                                         | 描述                                                   |
| ------------ | ------------------------------------------------------------ | ------------------------------------------------------ |
| AVG          | `Func.aggregate.AVG(expr)`<br />`Func.aggregate.AVG(distinct, expr)`<br />`Func.aggregate.AVG(expr, over)` | 返回 expr 的平均值，DISTINCT 选项可用于返回不同值的平均值，支持窗口函数 |
| BIT_AND      | `Func.aggregate.BIT_AND(expr)`<br />`Func.aggregate.BIT_AND(expr, over)` | 返回 expr 中所有比特的按位与，计算执行的精确度为 64 比特 |
| BIT_OR       | `Func.aggregate.BIT_OR(expr)`<br />`Func.aggregate.BIT_OR(expr, over)` | 返回 expr 中所有比特的按位或，计算执行的精确度为 64 比特 |
| BIT_XOR      | `Func.aggregate.BIT_XOR(expr)`<br />`Func.aggregate.BIT_XOR(expr, over)` | 返回 expr 中所有比特的按位异或，计算执行的精确度为 64 比特 |
| COUNT        | `Func.aggregate.COUNT(expr)`<br />`Func.aggregate.COUNT(distinct, expr)`<br />`Func.aggregate.COUNT(expr, over)` | 返回 SELECT 语句检索到的行中非 NULL 值的数目，支持窗口函数 |
| GROUP_CONCAT | `Func.aggregate.GROUP_CONCAT(...expr)`<br />`Func.aggregate.GROUP_CONCAT(distinct, ...expr)`<br />`Func.aggregate.GROUP_CONCAT(distinct, orderBy, separator, ...expr)` | 返回一个字符串结果，该结果由分组中的值连接而成，可指定排序和分隔符 |
| WM_CONCAT    | `Func.aggregate.WM_CONCAT(expr)`                             | Oracle 数据库特有的聚合函数，用于连接字符串             |
| MAX          | `Func.aggregate.MAX(expr)`<br />`Func.aggregate.MAX(distinct, expr)`<br />`Func.aggregate.MAX(expr, over)` | 返回 expr 的最大值，支持窗口函数                        |
| MIN          | `Func.aggregate.MIN(expr)`<br />`Func.aggregate.MIN(distinct, expr)`<br />`Func.aggregate.MIN(expr, over)` | 返回 expr 的最小值，支持窗口函数                        |
| SUM          | `Func.aggregate.SUM(expr)`<br />`Func.aggregate.SUM(distinct, expr)`<br />`Func.aggregate.SUM(expr, over)` | 返回 expr 的总数，若返回集合中无任何行则返回 NULL，支持窗口函数 |

#### 日期时间类函数：Func.DateTime

| 函数           | 代码                                                         | 描述                                                         |
| -------------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| ADDDATE        | `Func.dateTime.ADDDATE(expr, days)`                          | 将 days 天数添加至 expr                                      |
| ADDTIME        | `Func.dateTime.ADDTIME(expr, expr2)`                         | 将 expr2 添加至 expr 然后返回结果                            |
| CONVERT_TZ     | `Func.dateTime.CONVERT_TZ(dt, fromTz, toTz)`                 | 将时间日期值 dt 从 fromTz 给出的时区转到 toTz 给出的时区     |
| CURDATE        | `Func.dateTime.CURDATE()`                                    | 将当前日期按照 'YYYY-MM-DD' 或 YYYYMMDD 格式的值返回         |
| CURTIME        | `Func.dateTime.CURTIME()`                                    | 将当前时间以 'HH:MM:SS' 或 HHMMSS 的格式返回                 |
| DATE           | `Func.dateTime.DATE(expr)`                                   | 提取日期或时间日期表达式 expr 中的日期部分                   |
| DATE_FORMAT    | `Func.dateTime.DATE_FORMAT(date, format)`                    | 根据 format 字符串安排 date 值的格式                         |
| DATEDIFF       | `Func.dateTime.DATEDIFF(expr, expr2)`                        | 返回起始时间 expr 和结束时间 expr2 之间的天数                |
| DAYNAME        | `Func.dateTime.DAYNAME(date)`                                | 返回 date 对应的工作日名称                                   |
| DAYOFMONTH     | `Func.dateTime.DAYOFMONTH(date)`                             | 返回 date 对应的该月日期，范围是从 1 到 31                   |
| DAYOFWEEK      | `Func.dateTime.DAYOFWEEK(date)`                              | 返回 date (1 = 周日, 2 = 周一, ..., 7 = 周六)对应的工作日索引 |
| DAYOFYEAR      | `Func.dateTime.DAYOFYEAR(date)`                              | 返回 date 对应的一年中的天数，范围是从 1 到 366              |
| FROM_UNIXTIME  | `Func.dateTime.FROM_UNIXTIME(timestamp)`<br />`Func.dateTime.FROM_UNIXTIME(timestamp, format)` | 将 Unix 时间戳转换为日期格式                                 |
| UNIX_TIMESTAMP | `Func.dateTime.UNIX_TIMESTAMP()`<br />`Func.dateTime.UNIX_TIMESTAMP(date)` | 返回 Unix 时间戳，或返回 date 参数以秒数的形式表示           |
| GET_FORMAT     | `Func.dateTime.GET_FORMAT(date, type)`                       | 返回一个格式字符串                                           |
| HOUR           | `Func.dateTime.HOUR(time)`                                    | 返回 time 对应的小时数，范围是从 0 到 23                     |
| LAST_DAY       | `Func.dateTime.LAST_DAY(date)`                               | 获取一个日期或日期时间值，返回该月最后一天对应的值           |
| MAKEDATE       | `Func.dateTime.MAKEDATE(year, dayOfYear)`                    | 给出年份值和一年中的天数值，返回一个日期                     |
| MAKETIME       | `Func.dateTime.MAKETIME(hour, minute, second)`               | 返回由 hour、minute 和 second 参数计算得出的时间值           |
| MICROSECOND    | `Func.dateTime.MICROSECOND(expr)`                            | 从时间或日期时间表达式 expr 返回微秒值，范围从 0 到 999999   |
| MINUTE         | `Func.dateTime.MINUTE(time)`                                 | 返回 time 对应的分钟数，范围是从 0 到 59                     |
| MONTH          | `Func.dateTime.MONTH(date)`                                  | 返回 date 对应的月份，范围是从 1 到 12                       |
| MONTHNAME      | `Func.dateTime.MONTHNAME(date)`                              | 返回 date 对应的月份名称                                     |
| NOW            | `Func.dateTime.NOW()`                                        | 返回当前日期和时间值，格式为 'YYYY-MM-DD HH:MM:SS'           |
| PERIOD_ADD     | `Func.dateTime.PERIOD_ADD(p, n)`                             | 为年-月组合日期 p 添加 n 个月                                |
| PERIOD_DIFF    | `Func.dateTime.PERIOD_DIFF(p1, p2)`                          | 返回 p1 和 p2 之间的月数                                     |
| QUARTER        | `Func.dateTime.QUARTER(date)`                                | 返回 date 对应的一年中的季度值，范围是从 1 到 4              |
| SEC_TO_TIME    | `Func.dateTime.SEC_TO_TIME(seconds)`                         | 返回被转化为小时、分钟和秒数的 seconds 参数值                |
| SECOND         | `Func.dateTime.SECOND(time)`                                 | 返回 time 对应的秒数，范围是从 0 到 59                       |
| STR_TO_DATE    | `Func.dateTime.STR_TO_DATE(str, format)`                     | DATE_FORMAT() 函数的倒转，将字符串转换为日期时间值           |
| SYSDATE        | `Func.dateTime.SYSDATE()`                                    | 返回当前日期和时间值                                         |
| TIME           | `Func.dateTime.TIME(expr)`                                   | 提取一个时间或日期时间表达式的时间部分                       |
| TIME_FORMAT    | `Func.dateTime.TIME_FORMAT(time, format)`                    | 其使用和 DATE_FORMAT() 函数相同，但仅处理时间格式            |
| TIME_TO_SEC    | `Func.dateTime.TIME_TO_SEC(time)`                            | 返回已转化为秒的 time 参数                                   |
| TIMEDIFF       | `Func.dateTime.TIMEDIFF(expr, expr2)`                        | 返回起始时间 expr 和结束时间 expr2 之间的时间差              |
| TIMESTAMP      | `Func.dateTime.TIMESTAMP(expr)`<br />`Func.dateTime.TIMESTAMP(expr, expr2)` | 将日期或日期时间表达式 expr 作为日期时间值返回               |
| TIMESTAMPDIFF  | `Func.dateTime.TIMESTAMPDIFF(unit, datetimeExpr1, datetimeExpr2)` | 返回日期或日期时间表达式之间的整数差                         |
| TO_DAYS        | `Func.dateTime.TO_DAYS(date)`                                | 给定一个日期 date，返回一个天数（从年份 0 开始的天数）       |
| UTC_DATE       | `Func.dateTime.UTC_DATE()`                                   | 返回当前 UTC 日期值，格式为 'YYYY-MM-DD' 或 YYYYMMDD         |
| UTC_TIME       | `Func.dateTime.UTC_TIME()`                                   | 返回当前 UTC 时间值，格式为 'HH:MM:SS' 或 HHMMSS             |
| UTC_TIMESTAMP  | `Func.dateTime.UTC_TIMESTAMP()`                              | 返回当前 UTC 日期及时间值                                    |
| WEEK           | `Func.dateTime.WEEK(date)`<br />`Func.dateTime.WEEK(date, mode)` | 返回 date 对应的星期数                                       |
| WEEKDAY        | `Func.dateTime.WEEKDAY(date)`                                | 返回 date (0 = 周一, 1 = 周二, ... 6 = 周日)对应的工作日索引 |
| WEEKOFYEAR     | `Func.dateTime.WEEKOFYEAR(date)`                             | 将该日期的阳历周以数字形式返回，范围是从 1 到 53             |
| YEAR           | `Func.dateTime.YEAR(date)`                                   | 返回 date 对应的年份，范围是从 1000 到 9999                  |
| YEARWEEK       | `Func.dateTime.YEARWEEK(date)`<br />`Func.dateTime.YEARWEEK(date, mode)` | 返回一个日期对应的年或周                                     |

#### 控制流函数：Func.ControlFlow

| 函数   | 代码                                                         | 描述                                                         |
| ------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| CASE   | `Func.controlFlow.CASE(whenFn[])`<br />`Func.controlFlow.CASE(value, whenFn[])`<br />`Func.controlFlow.CASE(value, whenFn[], elseFn)` | CASE 表达式，根据条件返回不同的值                           |
| WHEN   | `Func.controlFlow.WHEN(expr)`<br />`Func.controlFlow.WHEN(expr, result)` | CASE 表达式中的 WHEN 子句                                    |
| ELSE   | `Func.controlFlow.ELSE()`<br />`Func.controlFlow.ELSE(result)` | CASE 表达式中的 ELSE 子句                                    |
| IF     | `Func.controlFlow.IF(expr1, expr2, expr3)`                   | 如果 expr1 为 TRUE，则返回 expr2，否则返回 expr3             |
| IFNULL | `Func.controlFlow.IFNULL()`<br />`Func.controlFlow.IFNULL(expr1, expr2)` | 如果 expr1 不为 NULL，则返回 expr1，否则返回 expr2           |
| NULLIF | `Func.controlFlow.NULLIF()`<br />`Func.controlFlow.NULLIF(expr1, expr2)` | 如果 expr1 = expr2，则返回 NULL，否则返回 expr1              |

#### 比较函数：Func.Comparison

> @since 2.1.4

| 函数        | 代码                                                  | 描述                                                   |
| ----------- | ----------------------------------------------------- | ------------------------------------------------------ |
| BETWEEN     | `Func.comparison.BETWEEN(min, max)`                   | 检查值是否在指定范围内                                 |
| COALESCE    | `Func.comparison.COALESCE(value, ...values)`          | 返回参数列表中的第一个非 NULL 值                       |
| EXISTS      | `Func.comparison.EXISTS(query)`                       | 检查子查询是否返回任何行                               |
| NOT_EXISTS  | `Func.comparison.NOT_EXISTS(query)`                   | 检查子查询是否不返回任何行                             |
| GREATEST    | `Func.comparison.GREATEST(value, ...values)`          | 返回参数列表中的最大值                                 |
| IN          | `Func.comparison.IN(value, ...values)`                | 检查值是否在指定列表中                                 |
| NOT_IN      | `Func.comparison.NOT_IN(value, ...values)`            | 检查值是否不在指定列表中                               |
| IS          | `Func.comparison.IS(value)`                           | 检查值是否为指定值（主要用于布尔值判断）               |
| IS_NOT      | `Func.comparison.IS_NOT(value)`                       | 检查值是否不为指定值                                   |
| IS_NULL     | `Func.comparison.IS_NULL()`                           | 检查值是否为 NULL                                      |
| IS_NOT_NULL | `Func.comparison.IS_NOT_NULL()`                       | 检查值是否不为 NULL                                    |
| ISNULL      | `Func.comparison.ISNULL(value)`                       | 检查值是否为 NULL（类似于 IS_NULL）                    |
| LEAST       | `Func.comparison.LEAST(value, ...values)`             | 返回参数列表中的最小值                                 |

#### 窗口函数：Func.Window

> @since 2.1.4

窗口函数用于在结果集的分区上执行计算，常用于排名、累计统计等场景。使用窗口函数时需要配合 `WindowOver` 对象来定义分区和排序规则。

**排名窗口函数：**

| 函数         | 代码                                       | 描述                                                   |
| ------------ | ------------------------------------------ | ------------------------------------------------------ |
| ROW_NUMBER   | `Func.window.ROW_NUMBER()`<br />`Func.window.ROW_NUMBER(over)` | 返回当前行在其分区中的行号，从 1 开始                  |
| RANK         | `Func.window.RANK()`<br />`Func.window.RANK(over)` | 返回当前行在其分区中的排名，相同值的行获得相同排名，排名会有跳跃 |
| DENSE_RANK   | `Func.window.DENSE_RANK()`<br />`Func.window.DENSE_RANK(over)` | 返回当前行在其分区中的排名，相同值的行获得相同排名，排名不会有跳跃 |
| PERCENT_RANK | `Func.window.PERCENT_RANK()`<br />`Func.window.PERCENT_RANK(over)` | 返回当前行在其分区中的相对排名（0 到 1 之间）          |
| CUME_DIST    | `Func.window.CUME_DIST()`<br />`Func.window.CUME_DIST(over)` | 返回当前行在其分区中的累积分布值（0 到 1 之间）        |
| NTILE        | `Func.window.NTILE(num_buckets)`<br />`Func.window.NTILE(num_buckets, over)` | 将分区中的行分成指定数量的桶，并返回当前行所在的桶号   |

**偏移窗口函数：**

| 函数        | 代码                                                         | 描述                                                   |
| ----------- | ------------------------------------------------------------ | ------------------------------------------------------ |
| LAG         | `Func.window.LAG(expr)`<br />`Func.window.LAG(expr, offset)`<br />`Func.window.LAG(expr, offset, default_value, over)` | 返回分区中当前行之前第 offset 行的值                   |
| LEAD        | `Func.window.LEAD(expr)`<br />`Func.window.LEAD(expr, offset)`<br />`Func.window.LEAD(expr, offset, default_value, over)` | 返回分区中当前行之后第 offset 行的值                   |
| FIRST_VALUE | `Func.window.FIRST_VALUE(expr)`<br />`Func.window.FIRST_VALUE(expr, over)` | 返回分区中第一行的值                                   |
| LAST_VALUE  | `Func.window.LAST_VALUE(expr)`<br />`Func.window.LAST_VALUE(expr, over)` | 返回分区中最后一行的值                                 |
| NTH_VALUE   | `Func.window.NTH_VALUE(expr, n)`<br />`Func.window.NTH_VALUE(expr, n, over)` | 返回分区中第 n 行的值                                  |

**窗口函数使用示例：**

```java
// 创建 WindowOver 对象
WindowOver over = WindowOver.create()
    .partitionBy(UserEntity::getDept)    // 按部门分区
    .orderByDesc(UserEntity::getSalary); // 按薪资降序排序

// 使用 ROW_NUMBER 进行排名
Select select = Select.create(UserEntity.class)
    .field(UserEntity::getId)
    .field(UserEntity::getUsername)
    .field(Func.window.ROW_NUMBER(over), "rank");

// 使用 LAG 获取上一行数据
Select select = Select.create(SalesEntity.class)
    .field(SalesEntity::getMonth)
    .field(SalesEntity::getAmount)
    .field(Func.window.LAG(SalesEntity::getAmount, 1, "0", 
        WindowOver.create().orderByAsc(SalesEntity::getMonth)), "prev_amount");
```

#### 如何自定义函数封装？

示例一：创建单个参数风格的函数封装，如：`ABS(X)`

```java
IFunction ABS(String x) {
    AbstractFunction func = Func.create("ABS");
    func.param(x);
    return func;
}
```

示例二：创建多个参数风格的函数封装，如：`FORMAT(X, D)`

```java
IFunction FORMAT(String x, Number d) {
    AbstractFunction func = Func.create("FORMAT");
    func.param(x).separator().param(d);
    return func;
}
```

示例三：创建表达式风格的函数封装，如：`CASE value WHEN exp1 THEN result1 ELSE result2 END`

```java
IFunction CASE(String value, IFunction[] whenFn, String elseFn) {
    return new AbstractFunction() {
        @Override
        public void onBuild() {
            field("CASE ");
            if (StringUtils.isNotBlank(value)) {
                field(value).space();
            }
            Arrays.stream(whenFn).forEach(func -> field(func).space());
            if (StringUtils.isNotBlank(elseFn)) {
                field(elseFn).space();
            }
            field("END");
        }
    };
}

IFunction WHEN(String expr, String result) {
    return new AbstractFunction() {
        @Override
        public void onBuild() {
            field("WHEN ").field(expr).space().field("THEN ").field(result).space();
        }
    };
}

IFunction ELSE(String result) {
    return new AbstractFunction() {
        @Override
        public void onBuild() {
            field("ELSE ").field(result).space();
        }
    };
}
```

### BatchSQL：批量SQL语句对象

与 SQL 对象一样属于对象查询的基础组件，主要用于批量更新类 SQL 语句的执行和参数对象的封装。

**示例代码一：** 基本使用方法

```java
// 构建SQL插入语句：INSERT INTO user (id, username, nickname, password, email) VALUES (?, ?, ?, ?, ?)
Insert insert = Insert.create(UserEntity.class)
    .field(Fields.create(UserEntity.FIELDS.ID,
                         UserEntity.FIELDS.USERNAME,
                         UserEntity.FIELDS.NICKNAME,
                         UserEntity.FIELDS.PASSWORD,
                         UserEntity.FIELDS.EMAIL));
// 构建批处理SQL对象（此处通过Insert对象构建，也可以直接书写SQL语句）
BatchSQL batchSQL = BatchSQL.create(insert)
    // 添加批参数
    .addParameter(Params.create("1", "用户A", "昵称A", "密码A", "邮件A"))
    .addParameter(Params.create("2", "用户B", "昵称B", "密码B", "邮件B"))
    // 可以添加额外的SQL语句（注意：非预编译，即不支持使用问号'?'占位和参数值传递）
    .addSQL("DELETE FROM user WHERE age > 30")
    .addSQL("DELETE FROM user WHERE age < 18");
// 执行批处理并返回每条SQK受影响记录数的数组
int[] effectCounts = batchSQL.execute();
// 可以通过此方法计算实际受影响的记录总数
int effectCount = BatchUpdateOperator.parseEffectCounts(effectCounts);
```

**示例代码二：** 读取并执行SQL脚本文件

```java
Transactions.execute(() -> {
    IDatabase database = JDBC.get();
    int effectCount = database.openSession(session -> {
        String dialectName = session.getConnectionHolder().getDialect().getName();
        String filename = String.format("db-init_%s.sql", dialectName);
        List<String> scripts = BatchSQL.loadSQL(filename);
        if (scripts.isEmpty()) {
            scripts = BatchSQL.loadSQL("db-init.sql");
        }
        return BatchSQL.execSQL(database, scripts);
    });
});
```

### EntitySQL：实体参数封装对象

主要用于使用会话（Session）执行数据实体查询时的条件及参数的封装。

**示例代码：**

```java
IResultSet<UserEntity> users = JDBC.get().openSession(new IDatabaseSessionExecutor<IResultSet<UserEntity>>() {
    public IResultSet<UserEntity> execute(IDatabaseSession session) throws Exception {
        return session.find(EntitySQL.create(UserEntity.class)
                            .field(Fields.create(UserEntity.FIELDS.ID, UserEntity.FIELDS.PASSWORD)),
                            Where.create(Cond.create()
                                         .eq(UserEntity.FIELDS.USERNAME).param("suninformation").and()
                                         .eq(UserEntity.FIELDS.PASSWORD).param(DigestUtils.md5Hex("654321")))
                            .orderByDesc(UserEntity.FIELDS.CREATE_TIME),
                            Page.create().pageSize(10));
    }
});
```

### 对象查询的另一种写法

由于对象查询中使用的各种类构造方法大部份都需要传递 `IDatabase` 和数据源名称等对象，在编写比较复杂的逻辑时，代码会很冗余，因此 JDBC 持久化模块特别提供了 `QueryBuilder` 类使能够参数重用和简化查询对象类的创建过程，减少代码冗余。

下面的示例是通过简单的复合查询来对比两种方式的不同之处：

```java
IDatabase owner = JDBC.get();
String dsName = "oracledb";

// 普通写法
IResultSet<Object[]> resultSet = Select.create(owner, dsName, UserEntity.class, "u")
    .join(Join.left(owner, dsName, UserExtEntity.TABLE_NAME).alias("ue")
          .on(Cond.create(owner, dsName)
              .eqField(Fields.field("u", UserEntity.FIELDS.ID), Fields.field("ue", UserExtEntity.FIELDS.UID))))
    .field(Fields.create()
           .add("u", UserEntity.FIELDS.ID)
           .add("u", UserEntity.FIELDS.USERNAME)
           .add("ue", UserExtEntity.FIELDS.MONEY))
    .find(IResultSetHandler.ARRAY.create());

// 另一种写法
IResultSet<Object[]> resultSet = new QueryBuilder<IResultSet<Object[]>>(owner, dsName) {{
    Select select = select(UserEntity.class, "u")
          .join(left(UserExtEntity.TABLE_NAME).alias("ue")
                .on(cond().eqField(field("u", UserEntity.FIELDS.ID), field("ue", UserExtEntity.FIELDS.UID))))
          .field("u", fields(UserEntity.FIELDS.ID, UserEntity.FIELDS.USERNAME))
          .field(field("ue", UserExtEntity.FIELDS.MONEY));
    // 返回最终结果
    build(select.find(IResultSetHandler.ARRAY.create()));
}}.build();
```

## Lambda 表达式支持

从 v2.1.4 版本开始，JDBC 持久化模块引入了对 Lambda 表达式的支持，允许开发者使用方法引用的方式编写类型安全的查询语句：

### LambdaUtils 工具类

`LambdaUtils` 类提供了一系列工具方法，用于解析 Lambda 表达式和方法引用：

```java
// 从方法引用中解析出字段名
String fieldName = LambdaUtils.getFieldName(UserEntity::getId);

// 从方法引用中解析出数据库字段名
String columnName = LambdaUtils.getColumnName(UserEntity::getCreateTime);

// 获取带前缀的完整字段名
String fullFieldName = LambdaUtils.getFullFieldName("u", UserEntity::getUsername);

// 从实体方法引用中获取实体名称
String entityName = LambdaUtils.getEntityName(UserEntity::getId);
```

### 使用 Lambda 表达式构建查询

#### 字段选择

```java
// 基本字段选择
Select select = Select.create(UserEntity.class)
    .field(UserEntity::getId)
    .field(UserEntity::getUsername);

// 带别名的字段选择
select.field(UserEntity::getId, "user_id");

// 带前缀的字段选择
select.field("u", UserEntity::getEmail);
```

#### 条件查询

```java
// 等于条件
Cond cond = Cond.create()
    .eq(UserEntity::getUsername, "admin")
    .and().eq(UserEntity::getPassword, "123456");

// 范围条件
cond = Cond.create()
    .gt(UserEntity::getAge, 18)
    .and().lt(UserEntity::getAge, 30);

// 字段比较
cond = Cond.create()
    .eq(UserEntity::getId, UserExtEntity::getUid);

// 带前缀的字段比较
cond = Cond.create()
    .eq("u", UserEntity::getId, "ue", UserExtEntity::getUid);
```

#### 连接查询

```java
// 内连接
Select select = Select.create(UserEntity.class, "u")
    .innerJoin(UserExtEntity.class, "ue", UserEntity::getId, UserExtEntity::getUid);

// 左连接
select = Select.create(UserEntity.class, "u")
    .leftJoin(UserExtEntity.class, "ue", UserEntity::getId, UserExtEntity::getUid);
```

#### 排序和分组

```java
// 排序
Select select = Select.create(UserEntity.class)
    .orderByAsc(UserEntity::getCreateTime)
    .orderByDesc(UserEntity::getId);

// 分组
select = Select.create(UserEntity.class)
    .groupBy(UserEntity::getDept)
    .having(Cond.create().gt(Func.aggregate.AVG(UserEntity::getSalary), 5000));
```

#### 完整示例

```java
// 创建查询
Select select = Select.create(UserEntity.class, "u")
    // 选择字段
    .field("u", UserEntity::getId)
    .field("u", UserEntity::getUsername)
    .field("u", UserEntity::getAge)
    .field("ue", UserExtEntity::getMoney, "salary")
    // 左连接
    .leftJoin(UserExtEntity.class, "ue", "u", UserEntity::getId, "ue", UserExtEntity::getUid)
    // 查询条件
    .where(Cond.create()
           .gt("u", UserEntity::getAge, 18)
           .and().eq("ue", UserExtEntity::getType, 1)
           .and().likeWrap("u", UserEntity::getUsername).param("%test%"))
    // 排序
    .orderByAsc("u", UserEntity::getCreateTime)
    .orderByDesc("u", UserEntity::getId);

// 执行查询
SQL sql = select.toSQL();
System.out.println("SQL: " + sql.toString());
System.out.println("参数: " + sql.params().params());
```

### 与 QueryBuilder 结合使用

```java
IDatabase owner = JDBC.get();
String dsName = "oracledb";

IResultSet<Object[]> resultSet = new QueryBuilder<IResultSet<Object[]>>(owner, dsName) {{
    Select select = select(UserEntity.class, "u")
          .field("u", UserEntity::getId)
          .field("u", UserEntity::getUsername)
          .field("ue", UserExtEntity::getMoney)
          .leftJoin(UserExtEntity.class, "ue", "u", UserEntity::getId, "ue", UserExtEntity::getUid)
          .where(cond()
                .gt("u", UserEntity::getAge, 18)
                .and().eq("ue", UserExtEntity::getType, 1))
          .orderByAsc("u", UserEntity::getCreateTime);

    build(select.find(IResultSetHandler.ARRAY.create()));
}}.build();
```

## 存储器

为了能够更方便地维护和执行 SQL 语句，JDBC 模块提供了存储器的支持：

### @Repository 注解

| 配置项      | 描述                                                         |
| ----------- | ------------------------------------------------------------ |
| dsName      | 数据源名称，默认为空                                         |
| item        | 从资源文件中加载 `item` 指定的配置项，默认为空               |
| configFile  | 资源文件路径名称，默认为空                                   |
| value       | 自定义 SQL 配置，默认为空                                    |
| update      | 是否为更新操作，默认为 `false`                               |
| page        | 是否分页查询，默认为 `false`                                 |
| useFilter   | 是否调用方法过滤，默认为 `false`                             |
| dbType      | 指定当前存储器适用的数据库类型，默认为全部                   |
| resultClass | 指定结果集类型                                               |

### 示例：执行自定义 SQL 语句

```java
@Repository
public class DemoRepository implements IRepository {

    @Repository(value = "select * from user where type = ${type}", page = true)
    public IResultSet<Object[]> execQuery(Integer type, Page page, IResultSet<Object[]> results) throws Exception {
        // 处理结果集
        return results;
    }
}
```

### 示例：从配置文件加载 SQL

**配置文件 `demo.repo.xml`：**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<properties>
    <category name="default">
        <property name="custom_query">
            <value><![CDATA[select * from user where type = ${type}]]></value>
        </property>
    </category>
</properties>
```

**存储器实现：**

```java
@Repository(configFile = "demo.repo.xml")
public class DemoRepository implements IRepository {

    @Repository(item = "custom_query", useFilter = true, resultClass = UserEntity.class)
    public List<UserEntity> execQuery(Integer type, List<UserEntity>... results) throws Exception {
        // 处理结果集
        final List<UserEntity> returnValues = new ArrayList<>();
        if (results != null && results.length > 0) {
            results[0].stream()
                .filter(user -> user.getAge() > 18)
                .forEach(returnValues::add);
        }
        return returnValues;
    }
}
```

### 动态 SQL 和数据过滤

存储器支持基于 JavaScript 和 Groovy 脚本语言实现的动态 SQL 语句拼装和数据过滤：

```xml
<property name="custom_query" language="javascript">
    <value><![CDATA[
        function custom_query(type) {
            var sqlStr = "select * from user";
            if (type) {
                sqlStr += " where type = ${type}";
            }
            return {
                "sql": function() { return sqlStr },
                "filter": function(results) {
                    var List = Java.type("java.util.ArrayList");
                    var result = new List();
                    if (results && results.isResultsAvailable) {
                        for (i = 0; i < results.resultData.length; i++) {
                            if (i % 2 == 0) {
                                result.add(results.resultData.get(i))
                            }
                        }
                    }
                    return result;
                }
            }
        }
    ]]></value>
</property>
```

## 高级特性

### 多表查询及自定义结果集处理

对于多表关联查询，可以使用自定义 `IResultSetHandler` 处理结果集：

```java
// 自定义结果集处理器
class CustomResultSetHandler implements IResultSetHandler<CustomUser> {
    @Override
    public List<CustomUser> handle(ResultSet resultSet) throws Exception {
        List<CustomUser> customUsers = new ArrayList<>();
        while (resultSet.next()) {
            CustomUser customUser = new CustomUser();
            customUser.setId(resultSet.getString("id"));
            customUser.setUsername(resultSet.getString("username"));
            customUser.setMoney(resultSet.getDouble("money"));
            customUsers.add(customUser);
        }
        return customUsers;
    }
}

// 执行查询
Select select = Select.create(UserEntity.class, "u")
    .join(Join.left(UserExtEntity.TABLE_NAME).alias("ue")
          .on(Cond.create()
              .eqField(Fields.field("u", UserEntity.FIELDS.ID), Fields.field("ue", UserExtEntity.FIELDS.UID))))
    .field("u", Fields.create(UserEntity.FIELDS.ID, UserEntity.FIELDS.USERNAME))
    .field("ue", UserExtEntity.FIELDS.MONEY);

List<CustomUser> customUsers = select.find(new CustomResultSetHandler());
```

### 基于注解的多表查询

JDBC 模块提供了一系列注解，用于定义多表关联查询：

```java
@QFrom(value = DeviceEntity.TABLE_NAME, alias = "d")
@QJoin(from = @QFrom(value = DeviceTypeEntity.TABLE_NAME, alias = "dt"),
        on = @QCond(field = @QField(prefix = "d", value = DeviceEntity.FIELDS.DEVICE_TYPE_ID),
                   with = @QField(prefix = "dt", value = DeviceTypeEntity.FIELDS.ID)))
@QWhere({
        @QCond(field = @QField(prefix = "d", value = DeviceEntity.FIELDS.SUBARRAY_ID),
               with = @QField("#subarray_id")),
        @QCond(field = @QField(prefix = "d", value = DeviceEntity.FIELDS.PARENT_ID),
               with = @QField(value = "#parent_id"), ignorable = true)
})
@QOrderBy(@QOrderField(prefix = "d", value = DeviceEntity.FIELDS.ORDER_NO))
public class SubarrayStatusVO implements Serializable {

    @QField(prefix = "d", value = DeviceEntity.FIELDS.ID)
    private String id;

    @QField(prefix = "d", value = DeviceEntity.FIELDS.DEVICE_TYPE_ID)
    private String deviceTypeId;

    @QField(prefix = "d", value = DeviceEntity.FIELDS.NAME)
    private String name;

    // 忽略 Getter 和 Setter 方法
}

// 执行查询
Query.Executor<SubarrayStatusVO> executor = Query.build(SubarrayStatusVO.class)
    .addVariable("subarray_id", subarrayId);

if (StringUtils.isNotBlank(parentId)) {
    executor.addVariable("parent_id", parentId);
}

IResultSet<SubarrayStatusVO> resultSet = executor.find();
```

### 存储过程调用

JDBC 模块提供了 `IProcedureOperator` 接口，用于调用存储过程：

```java
// 有输入参数无输出参数
try (IDatabaseConnectionHolder connectionHolder = JDBC.get().getDefaultConnectionHolder()) {
    IProcedureOperator<Object[]> procedureOperator = new DefaultProcedureOperator<Object[]>("procedure_name", connectionHolder)
        .addParameter("param1")
        .addParameter("param2")
        .execute(IResultSetHandler.ARRAY.create());

    // 遍历结果集集合
    for (List<Object[]> resultSet : procedureOperator.getResultSets()) {
        ResultSetHelper.bind(resultSet).forEach((wrapper, row) -> {
            System.out.println(wrapper.toObject(new UserEntity()));
            return true;
        });
    }
}

// 有输入输出参数
try (IDatabaseConnectionHolder connectionHolder = JDBC.get().getDefaultConnectionHolder()) {
    new DefaultProcedureOperator<Void>("procedure_name", connectionHolder)
        .addParameter("param1")
        .addParameter("param2")
        .addOutParameter(Types.VARCHAR)
        .execute((idx, paramType, result) -> {
            System.out.println(result);
        });
}
```

### 数据库锁操作

JDBC 模块集成了针对数据库记录锁的控制能力：

```java
// 通过 EntitySQL 对象传递锁参数
session.find(EntitySQL.create(User.class)
        .field(Fields.create(User.FIELDS.ID, User.FIELDS.USER_NAME).excluded(true))
        .forUpdate(IDBLocker.DEFAULT));

// 通过 Select 查询对象传递锁参数
Select select = Select.create(User.class, "u")
        .field("u", "username").field("ue", "money")
        .where(Where.create(Cond.create().eq(User.FIELDS.ID).param("bc19f5645aa9438089c5e9954e5f1ac5")))
        .forUpdate(IDBLocker.DEFAULT);

select.find(IResultSetHandler.ARRAY.create());
```

## 最佳实践

### 数据源配置

1. **生产环境建议**：在生产环境中，建议使用性能较好的连接池，如 HikariCP 或 Druid
2. **连接池配置**：根据应用的并发量和数据库服务器的性能，合理配置连接池的大小
3. **多数据源**：对于需要访问多个数据库的应用，使用多数据源配置

### 实体操作

1. **使用生成工具**：使用 Maven 插件自动生成实体类，减少手动编码错误
2. **链式调用**：使用实体类的 Builder 模式进行链式调用，提高代码可读性
3. **批量操作**：对于大量数据的插入或更新，使用批量操作提高性能
4. **部分更新**：只更新必要的字段，减少网络传输和数据库负载

### 查询优化

1. **使用 Lambda 表达式**：优先使用 Lambda 表达式构建查询，提高代码的类型安全性和可维护性
2. **合理使用索引**：在查询条件中使用索引字段，提高查询性能
3. **分页查询**：对于大量数据的查询，使用分页查询减少内存占用
4. **避免 SELECT *：** 只选择需要的字段，减少数据传输和内存占用

### 事务管理

1. **合理使用事务**：只在需要保证数据一致性的操作中使用事务
2. **事务范围**：事务范围应尽可能小，减少锁的持有时间
3. **异常处理**：在事务中正确处理异常，确保事务能够回滚

### 存储器使用

1. **复杂查询**：对于复杂的 SQL 查询，使用存储器管理 SQL 语句
2. **动态 SQL**：使用脚本语言实现动态 SQL，提高灵活性
3. **结果集处理**：使用存储器的过滤功能处理结果集，减少代码量

## 常见问题与解决方案

### 连接池相关

**问题**：连接池耗尽
**解决方案**：
- 增加连接池大小
- 检查代码中是否存在连接泄漏
- 使用 try-with-resources 确保连接正确关闭

**问题**：连接超时
**解决方案**：
- 增加连接超时时间
- 检查网络连接和数据库服务器状态
- 优化慢查询

### 性能相关

**问题**：查询性能差
**解决方案**：
- 为查询条件添加索引
- 优化 SQL 语句
- 使用分页查询
- 避免在循环中执行数据库操作

**问题**：批量操作性能差
**解决方案**：
- 使用 JDBC 的批量操作 API
- 合理设置批量操作的批次大小
- 考虑使用事务批量提交

### 事务相关

**问题**：事务回滚失败
**解决方案**：
- 确保在事务中正确处理异常
- 检查事务隔离级别设置
- 避免在事务中执行长时间操作

**问题**：死锁
**解决方案**：
- 避免长事务
- 统一操作顺序
- 合理使用索引
- 考虑使用乐观锁

### 其他问题

**问题**：实体类与数据库表结构不一致
**解决方案**：
- 使用 Maven 插件重新生成实体类
- 手动更新实体类以匹配数据库表结构

**问题**：SQL 语法错误
**解决方案**：
- 检查 SQL 语句语法
- 确保参数类型正确
- 使用数据库方言处理不同数据库的语法差异

## 总结

JDBC 持久化模块是 YMP 框架中一个功能强大、设计灵活的数据库访问解决方案。它提供了丰富的功能特性，包括轻量级 ORM、多数据源支持、事务管理、查询构建、Lambda 表达式支持等，使开发者能够更加高效地进行数据库操作。

通过本技能指南，您应该已经了解了 JDBC 持久化模块的核心功能、使用方法和最佳实践。在实际应用中，您可以根据具体需求选择合适的功能特性，结合最佳实践，构建高效、可靠的数据库访问层。
