---
name: ymp-mongodb
description: YMP框架MongoDB模块，针对MongoDB数据存取操作的专业封装，支持多数据源、会话管理、事务、GridFS等
version: 2.1.4
author: YMP Team
category: persistence
tags:
  - java
  - mongodb
  - nosql
  - persistence
  - gridfs
trigger: 当用户需要使用MongoDB数据库、实现NoSQL持久化、使用GridFS文件存储等场景时触发
tools:
  - mongodb
  - nosql-database
  - gridfs
examples:
  - 配置MongoDB数据源
  - 插入文档数据
  - 查询文档数据
  - 更新文档数据
  - 删除文档数据
  - 使用GridFS存储文件
---

# YMP MongoDB 模块技能文档

## 模块概述

MongoDB 持久化模块是 YMP 框架中针对 MongoDB 数据存取操作的专业封装，以 JDBC 持久化模块的设计思想为基础，采用会话机制，简化事务处理逻辑，支持多数据源配置和实体操作，基于操作器（IOperator）对象化拼装查询条件，并集成 MapReduce、GridFS、聚合及函数表达式等高级特性。

## 核心功能

- **多数据源支持**：支持配置多个 MongoDB 数据源，灵活切换
- **会话管理**：提供会话机制，统一管理连接资源
- **事务支持**：支持 MongoDB 事务操作，确保数据一致性
- **GridFS 文件存储**：支持大文件存储和管理
- **丰富的查询表达式**：提供多种查询表达式，支持复杂查询条件构建
- **实体映射**：支持对象与 MongoDB 文档之间的映射
- **操作器 API**：提供流畅的操作器 API，简化查询条件构建
- **聚合操作**：支持 MongoDB 聚合操作
- **表达式系统**：内置丰富的表达式类型，支持各种查询场景

## 架构设计

### 核心架构

MongoDB 模块采用分层架构设计，主要包含以下核心组件：

1. **模块核心**：`MongoDB` 类，实现 `IMongo` 接口，负责模块初始化和管理
2. **会话层**：`IMongoSession` 和 `IGridFsSession` 接口，提供操作方法
3. **连接层**：`IMongoConnectionHolder` 接口，管理数据库连接
4. **数据源层**：`IMongoDataSourceAdapter` 接口，适配不同数据源
5. **表达式层**：各种表达式实现，如 `Query`、`Operator` 等
6. **事务层**：`ITransaction` 接口，管理事务操作

### 组件关系

```
┌─────────────────────────────────────────────────────────┐
│                     MongoDB (模块核心)                 │
├─────────────────────────────────────────────────────────┤
│ ┌─────────────┐  ┌─────────────┐  ┌─────────────┐    │
│ │ IMongoSession │  │ IGridFsSession │  │ ITransaction │    │
│ └─────────────┘  └─────────────┘  └─────────────┘    │
├─────────────────────────────────────────────────────────┤
│ ┌───────────────────────────────────────────────────┐  │
│ │                IMongoConnectionHolder             │  │
│ └───────────────────────────────────────────────────┘  │
├─────────────────────────────────────────────────────────┤
│ ┌───────────────────────────────────────────────────┐  │
│ │             IMongoDataSourceAdapter              │  │
│ └───────────────────────────────────────────────────┘  │
├─────────────────────────────────────────────────────────┤
│ ┌───────────────────────┐  ┌───────────────────────┐  │
│ │        表达式系统       │  │        实体映射        │  │
│ └───────────────────────┘  └───────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

## 核心 API

### 模块核心 API

#### MongoDB 类

**获取模块实例**

```java
IMongo mongo = MongoDB.get();
```

**开启会话**

```java
// 开启默认数据源会话
IMongoSession session = mongo.openSession();

// 开启指定数据源会话
IMongoSession session = mongo.openSession("dataSourceName");

// 使用会话执行器
T result = mongo.openSession(new IMongoSessionExecutor<T>() {
    @Override
    public T execute(IMongoSession session) throws Exception {
        // 执行操作
        return result;
    }
});
```

**开启 GridFS 会话**

```java
// 开启默认桶的 GridFS 会话
IGridFsSession gridFsSession = mongo.openGridFsSession();

// 开启指定桶的 GridFS 会话
IGridFsSession gridFsSession = mongo.openGridFsSession("bucketName");

// 使用 GridFS 会话执行器
T result = mongo.openGridFsSession("bucketName", new IGridFsSessionExecutor<T>() {
    @Override
    public T execute(IGridFsSession session) throws Exception {
        // 执行文件操作
        return result;
    }
});
```

**开启事务**

```java
// 开启无返回值事务
mongo.openTransaction(new ITrade() {
    @Override
    public void deal() throws Throwable {
        // 事务操作
    }
});

// 开启有返回值事务
T result = mongo.openTransaction(new AbstractTrade<T>() {
    @Override
    public T dealing() throws Throwable {
        // 事务操作
        return result;
    }
});
```

### 会话 API

#### IMongoSession 接口

**插入操作**

```java
// 插入单个实体
UserEntity user = new UserEntity();
user.setUsername("admin");
user.setPassword("123456");
user = session.insert(user);

// 批量插入
List<UserEntity> users = new ArrayList<>();
// 添加用户...
users = session.insert(users);
```

**查询操作**

```java
// 查询所有记录
IResultSet<UserEntity> users = session.find(UserEntity.class);

// 条件查询
Query query = Query.create()
    .cond("username", Operator.create().eq("admin"))
    .cond("age", Operator.create().gte(18));
IResultSet<UserEntity> users = session.find(UserEntity.class, query);

// 分页查询
Page page = Page.create(1).pageSize(10);
IResultSet<UserEntity> users = session.find(UserEntity.class, query, page);

// 排序查询
OrderBy orderBy = OrderBy.create().asc("username").desc("createdTime");
IResultSet<UserEntity> users = session.find(UserEntity.class, query, orderBy, page);

// 根据ID查询
UserEntity user = session.find(UserEntity.class, "5f9e1a7b9c9d4e5f6a7b8c9d");
```

**更新操作**

```java
// 更新单个实体
UserEntity user = session.find(UserEntity.class, "5f9e1a7b9c9d4e5f6a7b8c9d");
user.setUsername("updatedAdmin");
session.update(user);

// 批量更新
List<UserEntity> users = new ArrayList<>();
// 添加要更新的用户...
session.update(users);

// 指定字段更新
Fields fields = Fields.create("username", "password");
session.update(user, fields);
```

**删除操作**

```java
// 删除单个实体
UserEntity user = new UserEntity();
user.setId("5f9e1a7b9c9d4e5f6a7b8c9d");
session.delete(user);

// 根据ID删除
session.delete(UserEntity.class, "5f9e1a7b9c9d4e5f6a7b8c9d");

// 批量删除
List<String> ids = Arrays.asList("id1", "id2");
session.delete(UserEntity.class, ids);

// 条件删除
Query query = Query.create().cond("age", Operator.create().lt(18));
session.delete(UserEntity.class, query);
```

**统计操作**

```java
// 统计所有记录
long count = session.count(UserEntity.class);

// 条件统计
Query query = Query.create().cond("age", Operator.create().gte(18));
long count = session.count(UserEntity.class, query);

// 检查记录是否存在
boolean exists = session.exists(UserEntity.class, "5f9e1a7b9c9d4e5f6a7b8c9d");

// 条件检查
boolean exists = session.exists(UserEntity.class, query);
```

#### IGridFsSession 接口

**文件上传**

```java
// 从文件上传
String fileId = session.upload(new File("localFile.txt"), new GridFSUploadOptions());

// 从输入流上传
InputStream inputStream = new FileInputStream("localFile.txt");
String fileId = session.upload("fileName.txt", inputStream, new GridFSUploadOptions());

// 自定义文件ID上传
String fileId = session.upload("customId", new File("localFile.txt"), new GridFSUploadOptions());
```

**文件下载**

```java
// 下载到输出流
OutputStream outputStream = new FileOutputStream("downloadedFile.txt");
session.download("fileId", outputStream);

// 下载到文件
File destFile = new File("downloadedFile.txt");
session.download("fileId", destFile);
```

**文件查询**

```java
// 查询所有文件
IResultSet<GridFSFile> files = session.find();

// 根据文件名查询
IResultSet<GridFSFile> files = session.find("fileName.txt");

// 条件查询
Query query = Query.create()
    .cond("length", Operator.create().lte(1024 * 1024)); // 小于1MB
IResultSet<GridFSFile> files = session.find(query);

// 分页查询
Page page = Page.create(1).pageSize(10);
IResultSet<GridFSFile> files = session.find(query, page);

// 根据ID查询
GridFSFile file = session.find("fileId");
```

**文件操作**

```java
// 检查文件是否存在
boolean exists = session.exists("fileId");

// 重命名文件
session.rename("fileId", "newFileName.txt");

// 删除文件
session.remove("fileId");

// 批量删除
List<String> fileIds = Arrays.asList("id1", "id2");
session.remove(fileIds);
```

### 表达式 API

#### Query 类

```java
// 创建查询对象
Query query = Query.create();

// 添加条件
query.cond("name", Operator.create().eq("YMP"))
     .cond("age", Operator.create().between(18, 30))
     .cond("tags", Operator.create().in(Arrays.asList("java", "mongodb")));

// 逻辑操作
Query query1 = Query.create().cond("age", Operator.create().gte(18));
Query query2 = Query.create().cond("status", Operator.create().eq("active"));
Query combinedQuery = Query.create().and(query1, query2);
```

#### Operator 类

```java
// 等于
Operator eq = Operator.create().eq("value");

// 不等于
Operator ne = Operator.create().ne("value");

// 大于
Operator gt = Operator.create().gt(10);

// 大于等于
Operator gte = Operator.create().gte(10);

// 小于
Operator lt = Operator.create().lt(100);

// 小于等于
Operator lte = Operator.create().lte(100);

// 范围
Operator between = Operator.create().between(10, 100);

// 包含
Operator in = Operator.create().in(Arrays.asList(1, 2, 3));

// 不包含
Operator nin = Operator.create().nin(Arrays.asList(4, 5, 6));

// 存在
Operator exists = Operator.create().exists(true);

// 类型
Operator type = Operator.create().type("string");

// 正则
Operator regex = Operator.create().regex("^A.*");
```

#### 聚合操作

```java
// 聚合查询
Aggregation aggregation = Aggregation.create()
    .match(Query.create().cond("status", Operator.create().eq("active")))
    .group("department",
           Aggregation.field("count").sum(1),
           Aggregation.field("avgAge").avg("age"))
    .sort(OrderBy.create().desc("count"));

// 执行聚合
List<Document> results = session.aggregate("users", aggregation);
```

## 配置与使用

### 模块配置

#### 注解配置

```java
@MongoConf(dsDefaultName = "default", value = {
    @MongoDataSource(
        name = "default",
        username = "admin",
        password = "123456",
        databaseName = "test",
        servers = "localhost:27017",
        autoConnection = true
    ),
    @MongoDataSource(
        name = "secondary",
        username = "admin",
        password = "123456",
        databaseName = "test",
        servers = "192.168.1.100:27017",
        autoConnection = true
    )
})
public class AppConfig {
    // 配置类
}
```

#### 配置文件参数

```properties
#-------------------------------------
# MongoDB持久化模块初始化参数
#-------------------------------------

# 默认数据源名称，默认值为default
ymp.configs.persistence.mongodb.ds_default_name=default

# 数据源列表，多个数据源名称间用'|'分隔，默认为default
ymp.configs.persistence.mongodb.ds_name_list=default|secondary

# 默认数据源配置
ymp.configs.persistence.mongodb.ds.default.username=admin
ymp.configs.persistence.mongodb.ds.default.password=123456
ymp.configs.persistence.mongodb.ds.default.database_name=test
ymp.configs.persistence.mongodb.ds.default.servers=localhost:27017
ymp.configs.persistence.mongodb.ds.default.auto_connection=true

# 第二个数据源配置
ymp.configs.persistence.mongodb.ds.secondary.username=admin
ymp.configs.persistence.mongodb.ds.secondary.password=123456
ymp.configs.persistence.mongodb.ds.secondary.database_name=test
ymp.configs.persistence.mongodb.ds.secondary.servers=192.168.1.100:27017
ymp.configs.persistence.mongodb.ds.secondary.auto_connection=true
```

### 数据源配置

#### @MongoDataSource 注解参数

| 配置项 | 描述 |
|-------|------|
| name | 数据源名称 |
| username | 数据库访问用户名称 |
| password | 数据库访问密码 |
| passwordEncrypted | 数据库访问密码是否已加密 |
| passwordClass | 数据库密码处理器 |
| collectionPrefix | 集合前缀名称 |
| connectionUrl | 服务器主机连接字符串 |
| autoConnection | 是否自动连接 |
| databaseName | 数据库名称 |
| authenticationDatabaseName | 包含用户身份验证数据的数据库名称 |
| servers | 服务器主机集合 |
| optionsHandlerClass | 数据源自定义配置处理器 |

## 使用示例

### 基本 CRUD 操作

```java
// 插入数据
UserEntity user = new UserEntity();
user.setUsername("admin");
user.setPassword("123456");
user.setEmail("admin@example.com");
user.setAge(25);
user.setCreatedTime(new Date());

UserEntity savedUser = MongoDB.get().openSession(session -> session.insert(user));

// 查询数据
UserEntity foundUser = MongoDB.get().openSession(session ->
    session.find(UserEntity.class, savedUser.getId())
);

// 更新数据
foundUser.setEmail("updated@example.com");
MongoDB.get().openSession(session -> session.update(foundUser));

// 删除数据
MongoDB.get().openSession(session ->
    session.delete(UserEntity.class, savedUser.getId())
);
```

### GridFS 文件操作

```java
// 上传文件
String fileId = MongoDB.get().openGridFsSession("files", session -> {
    File localFile = new File("document.pdf");
    return session.upload(localFile, new GridFSUploadOptions());
});

// 下载文件
MongoDB.get().openGridFsSession("files", session -> {
    File destFile = new File("downloaded.pdf");
    session.download(fileId, destFile);
    return null;
});

// 查询文件
GridFSFile file = MongoDB.get().openGridFsSession("files", session ->
    session.find(fileId)
);

// 删除文件
MongoDB.get().openGridFsSession("files", session -> {
    session.remove(fileId);
    return null;
});
```

### 事务处理

```java
// 无返回值事务
MongoDB.get().openTransaction(() -> {
    // 事务操作1
    UserEntity user = new UserEntity();
    user.setUsername("transactionUser");
    user.setPassword("123456");
    MongoDB.get().openSession(session -> session.insert(user));

    // 事务操作2
    OrderEntity order = new OrderEntity();
    order.setUserId(user.getId());
    order.setAmount(100.0);
    MongoDB.get().openSession(session -> session.insert(order));
});

// 有返回值事务
UserEntity result = MongoDB.get().openTransaction(new AbstractTrade<UserEntity>() {
    @Override
    public UserEntity dealing() throws Throwable {
        UserEntity user = new UserEntity();
        user.setUsername("transactionUser");
        user.setPassword("123456");
        return MongoDB.get().openSession(session -> session.insert(user));
    }
});
```

### 复杂查询

```java
// 构建复杂查询条件
Query query = Query.create()
    // 基本条件
    .cond("status", Operator.create().eq("active"))
    .cond("age", Operator.create().between(18, 35))
    // 数组包含
    .cond("skills", Operator.create().in(Arrays.asList("java", "mongodb")))
    // 文本搜索
    .cond("description", Operator.create().text("developer"))
    // 正则匹配
    .cond("email", Operator.create().regex("^.*@example\.com$"))
    // 存在字段
    .cond("profile", Operator.create().exists(true));

// 排序
OrderBy orderBy = OrderBy.create()
    .desc("createdTime")
    .asc("username");

// 分页
Page page = Page.create(1).pageSize(20);

// 执行查询
IResultSet<UserEntity> users = MongoDB.get().openSession(session ->
    session.find(UserEntity.class, query, orderBy, page)
);

// 处理结果
if (users.isResultsAvailable()) {
    List<UserEntity> userList = users.getResultData();
    long totalCount = users.getRecordCount();
    int totalPages = users.getPageCount();
    // 处理用户列表...
}
```

### 聚合查询

```java
// 构建聚合管道
Aggregation aggregation = Aggregation.create()
    // 匹配条件
    .match(Query.create().cond("status", Operator.create().eq("active")))
    // 分组
    .group("department",
           Aggregation.field("employeeCount").sum(1),
           Aggregation.field("avgSalary").avg("salary"),
           Aggregation.field("maxAge").max("age"),
           Aggregation.field("minAge").min("age"))
    // 排序
    .sort(OrderBy.create().desc("employeeCount"))
    // 限制结果
    .limit(10);

// 执行聚合
List<Document> results = MongoDB.get().openSession(session ->
    session.aggregate("users", aggregation)
);

// 处理聚合结果
for (Document doc : results) {
    String department = doc.getString("_id");
    int count = doc.getInteger("employeeCount");
    double avgSalary = doc.getDouble("avgSalary");
    // 处理结果...
}
```

## 高级特性

### 表达式系统

MongoDB 模块提供了丰富的表达式系统，支持各种查询场景：

#### 比较表达式

```java
// 等于
ComparisonExp.eq("value")

// 不等于
ComparisonExp.ne("value")

// 大于
ComparisonExp.gt(10)

// 大于等于
ComparisonExp.gte(10)

// 小于
ComparisonExp.lt(100)

// 小于等于
ComparisonExp.lte(100)

// 在范围内
ComparisonExp.between(10, 100)

// 在集合中
ComparisonExp.in(Arrays.asList(1, 2, 3))

// 不在集合中
ComparisonExp.nin(Arrays.asList(4, 5, 6))
```

#### 逻辑表达式

```java
// 与
LogicalExp.and(query1, query2)

// 或
LogicalExp.or(query1, query2)

// 非
LogicalExp.not(operator)

//  nor
LogicalExp.nor(query1, query2)
```

#### 数组表达式

```java
// 数组包含所有元素
ArrayExp.all(Arrays.asList("java", "mongodb"))

// 数组元素匹配
ArrayExp.elemMatch(Operator.create().eq("value"))

// 数组大小
ArrayExp.size(5)
```

#### 元素表达式

```java
// 字段存在
ElementExp.exists(true)

// 字段类型
ElementExp.type("string")
```

#### 评估表达式

```java
// 正则匹配
EvaluationExp.regex("^A.*")

// 文本搜索
EvaluationExp.text("developer", "english")

// 模式匹配
EvaluationExp.mod(5, 0) // 能被5整除

// JavaScript表达式
EvaluationExp.where("this.age > 18")
```

#### 更新表达式

```java
// 设置字段
UpdateExp.set("field", "value")

// 递增
UpdateExp.inc("count", 1)

// 递减
UpdateExp.inc("count", -1)

// 相乘
UpdateExp.mul("price", 1.1)

// 重命名
UpdateExp.rename("oldField", "newField")

// 删除字段
UpdateExp.unset("field")

// 添加到数组
UpdateExp.push("tags", "newTag")

// 从数组删除
UpdateExp.pull("tags", "oldTag")

// 添加到集合
UpdateExp.addToSet("uniqueTags", "tag")
```

### 实体映射

#### 基本实体

```java
public class UserEntity extends BaseEntity {

    @Property(name = "username", nullable = false)
    private String username;

    @Property(name = "password", nullable = false)
    private String password;

    @Property(name = "email")
    private String email;

    @Property(name = "age")
    private Integer age;

    @Property(name = "created_time")
    @Readonly
    private Date createdTime;

    @Property(name = "status")
    @Default("active")
    private String status;

    // Getter and Setter methods...
}
```

#### 复杂实体

```java
public class ProductEntity extends BaseEntity {

    @Property(name = "name", nullable = false)
    private String name;

    @Property(name = "price")
    private Double price;

    @Property(name = "description")
    private String description;

    @Property(name = "tags")
    private List<String> tags;

    @Property(name = "attributes")
    private Map<String, Object> attributes;

    @Property(name = "variants")
    private List<ProductVariant> variants;

    @Property(name = "created_by")
    private String createdBy;

    @Property(name = "created_time")
    private Date createdTime;

    // Getter and Setter methods...

    public static class ProductVariant {
        private String sku;
        private String color;
        private String size;
        private Double price;
        private Integer stock;
        // Getter and Setter methods...
    }
}
```

## 配置项

### 模块配置参数

| 配置项 | 描述 | 默认值 |
|-------|------|-------|
| ds_default_name | 默认数据源名称 | default |
| ds_name_list | 数据源列表，多个数据源名称间用'|'分隔 | default |

### 数据源配置参数

| 配置项 | 描述 | 默认值 |
|-------|------|-------|
| username | 数据源访问用户名称 | - |
| password | 数据源访问密码 | - |
| password_encrypted | 数据源访问密码是否已加密 | false |
| password_class | 数据源密码处理器 | - |
| collection_prefix | 集合前缀名称 | - |
| connection_url | 服务器主机连接字符串 | - |
| auto_connection | 是否自动连接 | false |
| database_name | 数据库名称 | - |
| authentication_database_name | 包含用户身份验证数据的数据库名称 | admin |
| servers | 服务器主机集合，格式：<IP地址[:端口]>，多个主机之间用'|'分隔 | - |
| options_handler_class | 自定义MongoDB客户端参数配置处理器 | - |

## 最佳实践

1. **会话管理**：使用 try-with-resources 或会话执行器模式，确保会话正确关闭

2. **连接池配置**：根据应用需求合理配置连接池参数，避免连接泄漏

3. **索引优化**：为频繁查询的字段创建索引，提高查询性能

4. **批量操作**：对于大量数据操作，使用批量插入和更新，减少网络往返

5. **分页查询**：使用分页查询，避免一次性加载过多数据

6. **事务使用**：仅在需要保证数据一致性的场景下使用事务，因为事务会影响性能

7. **GridFS 使用**：对于大于 16MB 的文件，使用 GridFS 存储

8. **查询优化**：使用合适的查询条件，避免全表扫描

9. **错误处理**：妥善处理 MongoDB 异常，提供友好的错误信息

10. **监控与日志**：监控 MongoDB 性能指标，记录关键操作日志

## 常见问题与解决方案

### 1. 连接失败

**问题**：无法连接到 MongoDB 服务器

**解决方案**：
- 检查网络连接
- 确认 MongoDB 服务是否运行
- 验证连接字符串和认证信息
- 检查防火墙设置

### 2. 性能问题

**问题**：查询速度慢

**解决方案**：
- 创建合适的索引
- 优化查询条件
- 使用投影减少返回字段
- 考虑使用聚合管道
- 检查 MongoDB 服务器资源使用情况

### 3. 事务失败

**问题**：事务操作失败

**解决方案**：
- 确认 MongoDB 版本支持事务（4.0+）
- 确认使用了副本集
- 检查事务超时设置
- 避免在事务中执行长时间操作

### 4. 内存溢出

**问题**：处理大量数据时内存溢出

**解决方案**：
- 使用游标分批处理数据
- 限制查询结果数量
- 增加 JVM 内存配置
- 优化数据处理逻辑

### 5. 索引丢失

**问题**：索引不生效

**解决方案**：
- 检查索引是否存在
- 验证查询条件是否使用了索引字段
- 避免在索引字段上使用函数
- 检查复合索引顺序

### 6. 数据一致性

**问题**：数据不一致

**解决方案**：
- 使用事务保证操作原子性
- 实现乐观锁机制
- 检查应用逻辑中的并发处理
- 考虑使用 MongoDB 事务

## 总结

MongoDB 模块是 YMP 框架中一个功能强大、设计灵活的 MongoDB 持久化解决方案，它提供了丰富的特性和友好的 API，使开发者能够轻松操作 MongoDB 数据库。

通过会话管理、事务支持、GridFS 文件存储、丰富的查询表达式等特性，MongoDB 模块满足了各种 MongoDB 操作场景的需求。同时，其流畅的 API 设计和强大的表达式系统，大大简化了复杂查询条件的构建。

MongoDB 模块不仅支持基本的 CRUD 操作，还支持高级特性如聚合查询、文本搜索、地理空间查询等，为开发者提供了全面的 MongoDB 操作能力。

通过本文档的介绍，相信开发者能够快速掌握 MongoDB 模块的使用方法，并在实际项目中灵活应用，构建高性能、可靠的 MongoDB 应用。
