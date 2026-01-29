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
4. **避免 SELECT ***：只选择需要的字段，减少数据传输和内存占用

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
