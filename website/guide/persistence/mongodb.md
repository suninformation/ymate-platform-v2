---
sidebar_position: 2
slug: mongodb
---

# MongoDB

针对 MongoDB 的数据存取操作的特点，以 JDBC 持久化模块的设计思想进行简单封装，采用会话机制，简化事务处理逻辑，支持多数据源配置和实体操作，基于操作器（IOperator）对象化拼装查询条件，并集成 MapReduce、GridFS、聚合及函数表达式等。



## Maven包依赖

```xml
<dependency>
    <groupId>net.ymate.platform</groupId>
    <artifactId>ymate-platform-persistence-mongodb</artifactId>
    <version>2.1.2</version>
</dependency>
```



## 模块配置

### 配置文件参数说明

```properties
# 默认数据源名称，默认值为default
ymp.configs.persistence.mongodb.ds_default_name=

# 数据源列表，多个数据源名称间用'|'分隔，默认为default
ymp.configs.persistence.mongodb.ds_name_list=

# 数据源访问用户名称，必要参数
ymp.configs.persistence.mongodb.ds.default.username=

# 数据源访问密码，可选参数
ymp.configs.persistence.mongodb.ds.default.password=

# 数据源访问密码是否已加密，默认为false
ymp.configs.persistence.mongodb.ds.default.password_encrypted=

# 数据源密码处理器，可选参数，用于对已加密码数据源访问密码进行解密，默认为空
ymp.configs.persistence.mongodb.ds.default.password_class=

# 集合前缀名称，可选参数，默认为空
ymp.configs.persistence.mongodb.ds.default.collection_prefix=

# 服务器主机连接字符串，可选参数，若提供此参数则下面的servers等参数就不在需要提供
ymp.configs.persistence.mongodb.ds.default.connection_url=

# 数据库名称，必填参数
ymp.configs.persistence.mongodb.ds.default.database_name=

# 服务器主机集合，格式：<IP地址[:端口]>，多个主机之间用'|'分隔，默认为空
ymp.configs.persistence.mongodb.ds.default.servers=

# 自定义MongoDB客户端参数配置处理器
ymp.configs.persistence.mongodb.ds.default.options_handler_class=
```



### 配置注解参数说明

:::tip **特别说明：** 

当 MongoDB 持久化模块初始化时，若在配置文件中存在数据源相关配置，则基于注解的数据源配置将全部失效。

:::



#### @MongoConf

| 配置项        | 描述           |
| ------------- | -------------- |
| dsDefaultName | 默认数据源名称 |
| value         | 数据源配置     |



#### @MongoDataSource

| 配置项              | 描述                     |
| ------------------- | ------------------------ |
| name                | 数据源名称               |
| username            | 数据库访问用户名称       |
| password            | 数据库访问密码           |
| passwordEncrypted   | 数据库访问密码是否已加密 |
| passwordClass       | 数据库密码处理器         |
| collectionPrefix    | 集合前缀名称             |
| databaseName        | 数据库名称               |
| connectionUrl       | 服务器主机连接字符串     |
| servers             | 服务器主机集合           |
| optionsHandlerClass | 数据源自定义配置处理器   |



## 数据源（DataSource）

### 多数据源连接

MongoDB 持久化模块默认支持多数据源配置，下面通过简单的配置来展示如何连接多个服务：

```properties
# 定义两个数据源分别用于连接本地和另一台IP地址两个MongoDB服务
ymp.configs.persistence.mongodb.ds_default_name=default
ymp.configs.persistence.mongodb.ds_name_list=default|othermongodb

# 默认数据源连接本地默认端口MongoDB服务
ymp.configs.persistence.mongodb.ds.default.username=clientuser
ymp.configs.persistence.mongodb.ds.default.password==12345678
ymp.configs.persistence.mongodb.ds.default.database_name=demo
ymp.configs.persistence.mongodb.ds.default.servers=localhost

# 名称为othermongodb数据源连接指定IP地址和端口的MongoDB服务
ymp.configs.persistence.mongodb.ds.othermongodb.username=clientuser
ymp.configs.persistence.mongodb.ds.othermongodb.password==12345678
ymp.configs.persistence.mongodb.ds.othermongodb.database_name=demo
ymp.configs.persistence.mongodb.ds.othermongodb.servers=10.211.55.5
```

通过注解方式配置多数据源，如下所示：

```java
@MongoConf(dsDefaultName = "default", value = {
        @MongoDataSource(
                name = "default",
                username = "clientuser",
                password = "12345678",
                databaseName = "demo",
                servers = "localhost"),
        @MongoDataSource(
                name = "othermongodb",
                username = "clientuser",
                password = "12345678",
                databaseName = "demo",
                servers = {"10.211.55.5"})})
```



### MongoDB 连接持有者（IMongoConnectionHolder）

用于记录真正的数据库连接对象（MongoDatabase）原始的状态及与数据源对应关系，在 MongoDB 持久化模块中获取到的所有连接对象均由数据库连接持有者对象包装，基于数据库连接持有者接口可以进行如下操作：



**示例：**

```java
public class Main {

    public static void main(String[] args) throws Exception {
        try (IApplication application = YMP.run(args)) {
            if (application.isInitialized()) {
                // 获取当前容器内MongoDB模块实例
                IMongo mongo = application.getModuleManager().getModule(MongoDB.class);
                // 获取默认数据源的连接持有者实例，等同于 mongo.getConnectionHolder("default");
                IMongoConnectionHolder connectionHolder = mongo.getDefaultConnectionHolder();
                // 获取指定名称数据源连接持有者实例
                connectionHolder = mongo.getConnectionHolder("othermongodb");
                // 获取数据库连接对象
                MongoDatabase connection = connectionHolder.getConnection();
                // 获取数据源配置对象
                IMongoDataSourceConfig dataSourceConfig = connectionHolder.getDataSourceConfig();
                // 获取当前数据源适配器对象
                IMongoDataSourceAdapter dataSourceAdapter = connectionHolder.getDataSourceAdapter();
                // 获取当前连接持有者所属MongoDB模块实例
                IMongo owner = connectionHolder.getOwner();
            }
        }
    }
}
```



## 数据实体（Entity）

数据实体是以对象的形式与数据库表之间的一种映射关系，实体中的属性与表中字段一一对应，与 JDBC 持久化模块中的实体在使用上做了相应的简化。

在 MongoDB 表中，记录的主键名称固定为 `_id`（在编写代码时可以使用 `IMongo.Opt.ID`  常量）， 一般采用自动生成且无复合主键的情况，因此，在无特殊需求的情况下，编写实体类只需要继承 `BaseEntity` 类（注意：与 JDBC 持久化模块中的实体基类名称同名），否则需要保证实体类实现 `IEntity` 接口 ，该类仅做了简单的主键映射，若需要自定义主键请为 `id` 属性赋值，否则将获取自动生成的值，其代码如下：

```java
public class BaseEntity implements IEntity<String> {

    @Id
    @Property(name = IMongo.Opt.ID)
    private String id;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BaseEntity that = (BaseEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
    }
}
```



### 数据实体注解

在 MongoDB 持久化模块中，可用于数据实体的注解仅支持以下几种，且 `@Property` 注解中的一些配置项在 MongoDB 中无效。



#### @Entity

声明一个类为数据实体对象。

| 配置项 | 描述                                         |
| ------ | -------------------------------------------- |
| value  | 实体名称（数据库表名称），默认采用当前类名称 |



#### @Property

声明一个类成员为数据实体属性。

| 配置项           | 描述                                                         |
| ---------------- | ------------------------------------------------------------ |
| name             | 实现属性名称，默认采用当前成员名称                           |
| autoincrement    | 是否为自动增长，默认为 `false`                               |
| ~~sequenceName~~ | ~~在 MongoDB 中，该配置项无效~~                              |
| useKeyGenerator  | 指定键值生成器名称，默认为空表示不启用（仅当非自动增长且主键值为空时调用）。<br />目前框架提供了 `IKeyGenerator.UUID` 键值生成器，其采用 UUID 策略。<br />可通过实现 `IKeyGenerator` 接口自行实现并通过 `SPI` 方式向框架注册。 |
| nullable         | 允许为空，默认为 `true`                                      |
| ~~unsigned~~     | ~~在 MongoDB 中，该配置项无效~~                              |
| ~~length~~       | ~~在 MongoDB 中，该配置项无效~~                              |
| ~~decimals~~     | ~~在 MongoDB 中，该配置项无效~~                              |
| ~~type~~         | ~~在 MongoDB 中，该配置项无效~~                              |



#### @Readonly

声明一个成员为只读属性，数据实体更新时其值将被忽略，与 `@Property` 注解配合使用，无参数。



#### @Comment

实体或成员属性的注释内容。



#### @Default

为一个成员属性指定默认值。

| 配置项  | 描述                     |
| ------- | ------------------------ |
| value   | 默认值                   |
| ignored | 是否忽略，默认为 `false` |



### 实体类与表对应关系示例

假定数据库 `demo` 集合中的  `user`  表存在 `nick_name`、`age`、 `gender` 和 `create_time` 属性，通过实体类表示如下：

```java
@Entity(value = "user")
public class UserEntity extends BaseEntity {

    @Property(name = FIELDS.NICKNAME, nullable = false)
    private String nickname;

    @Property
    private Integer age;

    @Property
    @Default("F")
    private String gender;

    @Property(name = FIELDS.CREATE_TIME, nullable = false)
    @Readonly
    private Date createTime;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public interface FIELDS {
        String NICKNAME = "nickname";
        String GENDER = "gender";
        String AGE = "age";
        String CREATE_TIME = "create_time";
    }
}
```



## 事务（Transaction）

MongoDB 持久化模块与 JDBC 持久化模块在事务的处理方式上有异曲同工之处。

:::tip **需要特别注意：** 

MongoDB 在进行事务操作时必须开启副本集（同时只能在 `PRIMARY` 服务上才能进行正常的写入操作），否则可能会产生类似如下所示的异常：

```shell
com.mongodb.MongoQueryException: Query failed with error code 20 and error message 'Transaction numbers are only allowed on a replica set member or mongos' on server xxx.xxx.xxx.xxx:27017
```

另外，请避免在已开启的事务中执行与文件存储（GridFS）相关操作（其不支持事务操作），可能产生如下异常：

```shell
Command failed with error 263 (OperationNotSupportedInTransaction): 'Cannot run 'listIndexes' in a multi-document transaction.' on server xxx.xxx.xxx.xxx:27017......
```

:::



### 开启事务

在 MongoDB 持久化模块中的事务仅支持手动方式开启，目前提供了两种事务执行方式，分别针对无返回值和有返回值的情况。

**示例：** 无返回值事务

```java
// 开启默认数据源事务
MongoDB.get().openTransaction(new ITrade() {
    @Override
    public void deal() throws Throwable {
        // 具体业务逻辑
    }
});
// 开启指定数据源事务
MongoDB.get().openTransaction("othermongodb", new ITrade() {
    @Override
    public void deal() throws Throwable {
        // 具体业务逻辑
    }
});
```



**示例：** 有返回值事务

```java
// 开启默认数据源事务
UserEntity userEntity = MongoDB.get().openTransaction(new AbstractTrade<UserEntity>() {
    @Override
    public UserEntity dealing() throws Throwable {
        // 具体业务逻辑
        return null;
    }
});
// 开启指定数据源事务
UserEntity userEntity = MongoDB.get().openTransaction("othermongodb", new AbstractTrade<UserEntity>() {
    @Override
    public UserEntity dealing() throws Throwable {
        // 具体业务逻辑
        return null;
    }
});
```



**示例：** 传递自定义事务配置

```java
// 自定义事务配置
ClientSessionOptions sessionOptions = ClientSessionOptions.builder()
    .causallyConsistent(true)
    .defaultTransactionOptions(TransactionOptions.builder()
                               .writeConcern(WriteConcern.MAJORITY)
                               .readConcern(ReadConcern.AVAILABLE)
                               .readPreference(ReadPreference.primary())
                               .build()).build();
// 无返回值事务调用
MongoDB.get().openTransaction(new ITrade() {
    @Override
    public void deal() throws Throwable {
        // 具体业务逻辑
    }
}, sessionOptions);

// 有返回值事务调用
UserEntity userEntity = MongoDB.get().openTransaction(new AbstractTrade<UserEntity>() {
    @Override
    public UserEntity dealing() throws Throwable {
        // 具体业务逻辑
        return null;
    }
}, sessionOptions);
```



### 事务回滚（Rollback）

与 JDBC 持久化模块一样，在开启事务的业务逻辑处理过程中，只要抛出任何异常都将终止事务并回滚。



## 会话（Session）

会话是对应用中具体业务操作触发的一系列与 MongoDB 之间的交互过程的封装，通过建立一个临时通道，负责与 MongoDB 之间连接资源的创建及回收，同时提供更为高级的抽象指令接口调用，基于会话的优点：

- 开发人员不需要担心连接资源是否正确释放。
- 严格的编码规范更利于维护和理解。
- 更好的业务封装性。



### 如何开启会话

**示例：** 使用默认数据源开启会话

```java
IResultSet<UserEntity> users = MongoDB.get().openSession(new IMongoSessionExecutor<IResultSet<UserEntity>>() {
    @Override
    public IResultSet<UserEntity> execute(IMongoSession session) throws Exception {
        return session.find(UserEntity.class, Query.create()
                            .cond(UserEntity.FIELDS.NICKNAME, Operator.create().eq("suninformation")));
    }
});
```



**示例：** 使用指定的数据源开启会话

```java
IResultSet<UserEntity> users = MongoDB.get().openSession("othermongodb", new IMongoSessionExecutor<IResultSet<UserEntity>>() {
    @Override
    public IResultSet<UserEntity> execute(IMongoSession session) throws Exception {
        return session.find(UserEntity.class, new QueryBuilder() {{
            and(query(UserEntity.FIELDS.NICKNAME, operator().eq("suninformation")),
                query(UserEntity.FIELDS.GENDER, operator().eq("M")));
        }});
    }
});
```



**示例：** 手动开启与关闭会话

```java
// 一定要确保连接使用完毕后关闭会话以释放连接
try (IMongoSession session = MongoDB.get().openSession()) {
    IMongoConnectionHolder holder = session.getConnectionHolder();
    MongoDatabase mongoDatabase = holder.getConnection();
    //
    MongoCollection<Document> collection = mongoDatabase.getCollection("user");
    // 或 collection = session.getCollection(UserEntity.class);
    // ......
}
```



### 查询（Find）

**示例：** 查询指定实体类型的全部记录

```java
MongoDB.get().openSession(new IMongoSessionExecutor<IResultSet<UserEntity>>() {
    @Override
    public IResultSet<UserEntity> execute(IMongoSession session) throws Exception {
        return session.find(UserEntity.class);
    }
});
```



**示例：** 查询符合条件的全部记录

```java
MongoDB.get().openSession(new IMongoSessionExecutor<IResultSet<UserEntity>>() {
    @Override
    public IResultSet<UserEntity> execute(IMongoSession session) throws Exception {
        return session.find(UserEntity.class, Query.create()
                            .cond(UserEntity.FIELDS.GENDER, Operator.create().eq("M")));
    }
});
```



**示例：** 查询指定唯一标识的记录

```java
MongoDB.get().openSession(new IMongoSessionExecutor<UserEntity>() {
    @Override
    public UserEntity execute(IMongoSession session) throws Exception {
        return session.find(UserEntity.class, "616a875998fa6c768fbd8708");
    }
});
```



**示例：** 分页查询符合条件的记录并按时间升序排序

```java
MongoDB.get().openSession(new IMongoSessionExecutor<IResultSet<UserEntity>>() {
    @Override
    public IResultSet<UserEntity> execute(IMongoSession session) throws Exception {
        return session.find(UserEntity.class, Query.create()
                            .cond(UserEntity.FIELDS.GENDER, Operator.create().eq("M")), 
                            OrderBy.create().asc(UserEntity.FIELDS.CREATE_TIME), Page.create().pageSize(10));
    }
});
```



### 统计（Count）

**示例：** 统计指定实体类型的总记录数

```java
MongoDB.get().openSession(new IMongoSessionExecutor<Long>() {
    @Override
    public Long execute(IMongoSession session) throws Exception {
        return session.count(UserEntity.class);
    }
});
```



**示例：** 统计符合指定查询条件的记录数

```java
MongoDB.get().openSession(new IMongoSessionExecutor<Long>() {
    @Override
    public Long execute(IMongoSession session) throws Exception {
        return session.exists(UserEntity.class, new QueryBuilder() {{
            and(query(UserEntity.FIELDS.NICKNAME, operator().eq("suninformation")),
                query(UserEntity.FIELDS.GENDER, operator().eq("M")));
        }});
    }
});
```



### 去重（Distinct）

```java
// TODO
```



### 记录是否存在（Exists）

**示例：** 判断指定唯一标识的记录是否存在

```java
MongoDB.get().openSession(new IMongoSessionExecutor<Boolean>() {
    @Override
    public Boolean execute(IMongoSession session) throws Exception {
        return session.exists(UserEntity.class, "616a875998fa6c768fbd8707");
    }
});
```



**示例：** 判断符合指定查询条件的记录是否存在

```java
MongoDB.get().openSession(new IMongoSessionExecutor<Boolean>() {
    @Override
    public Boolean execute(IMongoSession session) throws Exception {
        return session.exists(UserEntity.class, new QueryBuilder() {{
            and(query(UserEntity.FIELDS.NICKNAME, operator().eq("suninformation")),
                query(UserEntity.FIELDS.GENDER, operator().eq("M")));
        }});
    }
});
```





### 插入（Insert）

**示例：** 单实体插入

```java
MongoDB.get().openSession(new IMongoSessionExecutor<UserEntity>() {
    @Override
    public UserEntity execute(IMongoSession session) throws Exception {
        UserEntity user = new UserEntity();
        // 默认为自动生成主键值，若自定义需手动设置
        // user.setId("616447c05dc4f4358e621233");
        user.setNickname("suninformation");
        user.setAge(19);
        user.setCreateTime(new Date());
        //
        return session.insert(user);
    }
});
```



**示例：** 批量实体插入

```java
MongoDB.get().openSession(new IMongoSessionExecutor<List<UserEntity>>() {
    @Override
    public List<UserEntity> execute(IMongoSession session) throws Exception {
        UserEntity user = new UserEntity();
        user.setNickname("suninformation");
        user.setAge(19);
        user.setCreateTime(new Date());
        //
        UserEntity otherUser = new UserEntity();
        otherUser.setNickname("otherUser");
        otherUser.setAge(20);
        otherUser.setCreateTime(new Date());
        //
        return session.insert(Arrays.asList(user, otherUser));
    }
});
```



### 更新（Update）

**示例：** 单实体更新

```java
MongoDB.get().openSession(new IMongoSessionExecutor<UserEntity>() {
    @Override
    public UserEntity execute(IMongoSession session) throws Exception {
        UserEntity user = new UserEntity();
        user.setGender("M");
        // 可以通过 Fields 参数指定预更新的字段集合
        return session.update(user, Fields.create(UserEntity.FIELDS.GENDER));
    }
});
```



**示例：** 批量实体更新

```java
MongoDB.get().openSession(new IMongoSessionExecutor<List<UserEntity>>() {
    @Override
    public List<UserEntity> execute(IMongoSession session) throws Exception {
        UserEntity user = new UserEntity();
        user.setId("616a875998fa6c768fbd8707");
        user.setGender("M");
        //
        UserEntity otherUser = new UserEntity();
        otherUser.setId("616a875998fa6c768fbd8708");
        otherUser.setGender("F");
        //
        return session.update(Arrays.asList(user, otherUser), Fields.create(UserEntity.FIELDS.GENDER));
    }
});
```



### 删除（Delete）

**示例：** 单记录删除

```java
// 方式一：
MongoDB.get().openSession(new IMongoSessionExecutor<UserEntity>() {
    @Override
    public UserEntity execute(IMongoSession session) throws Exception {
        UserEntity user = new UserEntity();
        user.setId("616a875998fa6c768fbd8707");
        //
        return session.delete(user);
    }
});

// 方式二：
MongoDB.get().openSession(new IMongoSessionExecutor<UserEntity>() {
    @Override
    public UserEntity execute(IMongoSession session) throws Exception {
        return session.delete(UserEntity.class, "616a875998fa6c768fbd8707");
    }
});
```



**示例：** 批量删除

```java
// 方式一：
MongoDB.get().openSession(new IMongoSessionExecutor<Long>() {
    @Override
    public Long execute(IMongoSession session) throws Exception {
        UserEntity user = new UserEntity();
        user.setId("616a875998fa6c768fbd8707");
        //
        UserEntity otherUser = new UserEntity();
        otherUser.setId("616a875998fa6c768fbd8708");
        //
        return session.delete(Arrays.asList(user, otherUser));
    }
});

// 方式二：
MongoDB.get().openSession(new IMongoSessionExecutor<Long>() {
    @Override
    public Long execute(IMongoSession session) throws Exception {
        return session.delete(UserEntity.class, Arrays.asList("616a875998fa6c768fbd8707", "616a875998fa6c768fbd8708"));
    }
});
```



### 并行计算（MapReduce）

```java
// TODO
```



### 聚合（Aggregate）

```java
// TODO
```



## 文件存储（GridFS）

文件存储是 MongoDB 持久化模块中的另一种会话模式，称之为文件存储会话（IGridFsSession），不同之处在于文件存储的会话接口方法主要提供的是对文件或流的操作，如：文件上传、下载、查询、匹配、重命名、删除等。



### 如何开启文件存储会话

:::tip **注意：** 

文件存储会话开启时，若不指定桶名称 `bucketName` 参数则默认值为 `fs`。若在 MongoDB 中未提前创建桶的情况下执行上传文件操作时会有异常信息输出，同时会自动创建对应名称的桶并完成文件上传。

以下示例代码中指定的桶名称为：`bucket001`

:::



**示例：** 使用默认数据源开启文件存储会话

```java
IResultSet<GridFSFile> files = MongoDB.get().openGridFsSession("bucket001", new IGridFsSessionExecutor<IResultSet<GridFSFile>>() {
    @Override
    public IResultSet<GridFSFile> execute(IGridFsSession session) throws Exception {
        return session.find();
    }
});
```



**示例：** 使用指定的数据源开启文件存储会话

```java
IResultSet<GridFSFile> files = MongoDB.get().openGridFsSession("othermongodb", "bucket001", new IGridFsSessionExecutor<IResultSet<GridFSFile>>() {
    @Override
    public IResultSet<GridFSFile> execute(IGridFsSession session) throws Exception {
        return session.find();
    }
});
```



**示例：** 手动开启与关闭文件存储会话

```java
// 一定要确保连接使用完毕后关闭会话以释放连接
try (IGridFsSession session = MongoDB.get().openGridFsSession("bucket001")) {
    String bucketName = session.getBucketName();
    GridFSBucket fsBucket = session.getGridFsBucket();
    // ......
}
```



### 查询文件（Find）

**示例：** 查询指定唯一标识的文件

```java
public GridFSFile findById(final String id) throws Exception {
    return MongoDB.get().openGridFsSession("bucket001", new IGridFsSessionExecutor<GridFSFile>() {
        @Override
        public GridFSFile execute(IGridFsSession session) throws Exception {
            return session.find(id);
        }
    });
}
```



**示例：** 分页查询指定名称的文件并按上传时间升序排序

```java
public IResultSet<GridFSFile> findByName(final String fileName, final int page) throws Exception {
    return MongoDB.get().openGridFsSession("bucket001", new IGridFsSessionExecutor<IResultSet<GridFSFile>>() {
        @Override
        public IResultSet<GridFSFile> execute(IGridFsSession session) throws Exception {
            return session.find(fileName, OrderBy.create().asc(IMongo.GridFs.UPLOAD_DATE), Page.create(page).pageSize(10));
        }
    });
}
```



**示例：** 自定义条件查询

```java
public IResultSet<GridFSFile> findByCond(final int size, final Date uploadDate) throws Exception {
    return MongoDB.get().openGridFsSession("bucket001", new IGridFsSessionExecutor<IResultSet<GridFSFile>>() {
        @Override
        public IResultSet<GridFSFile> execute(IGridFsSession session) throws Exception {
            // 以下两种书写方式的执行结果是一样的！
            // 方式一：
            return session.find(Query.create()
                         .and(Query.create(IMongo.GridFs.LENGTH, Operator.create().gte(size)),
                              Query.create(IMongo.GridFs.UPLOAD_DATE, Operator.create().lte(uploadDate))));
            // 方式二：
            return session.find(new QueryBuilder() {{
                and(query(IMongo.GridFs.LENGTH, operator().gte(size)),
                    query(IMongo.GridFs.UPLOAD_DATE, operator().lte(uploadDate)));
            }});
        }
    });
}
```



### 匹配文件（Match）

**示例：** 对指定的文件进行 `MD5` 签名并判断该文件签名值是否已存在，若存在则返回文件信息，否则返回 `null`

```java
public GridFSFile matchFile(final File originFile) throws Exception {
    return MongoDB.get().openGridFsSession("bucket001", new IGridFsSessionExecutor<GridFSFile>() {
        @Override
        public GridFSFile execute(IGridFsSession session) throws Exception {
            try (InputStream inputStream = new FileInputStream(originFile)) {
                String fileHash = DigestUtils.md5Hex(inputStream);
                return session.match(fileHash);
            }
        }
    });
}
```



### 文件是否存在（Exists）

**示例：** 判断指定唯一标识的文件是否存在

```java
public boolean isExists(final String id) throws Exception {
    return MongoDB.get().openGridFsSession("bucket001", new IGridFsSessionExecutor<Boolean>() {
        @Override
        public Boolean execute(IGridFsSession session) throws Exception {
            return session.exists(id);
        }
    });
}
```



### 上传文件（Upload）

**示例：** 通过文件上传

```java
public String uploadFromFile(final File originFile) throws Exception {
    return MongoDB.get().openGridFsSession("bucket001", new IGridFsSessionExecutor<String>() {
        @Override
        public String execute(IGridFsSession session) throws Exception {
            return session.upload(originFile, new GridFSUploadOptions());
        }
    });
}
```



**示例：** 通过输入数据流上传

```java
public String uploadFromStream(final String fileName, final InputStream originInputStream) throws Exception {
    return MongoDB.get().openGridFsSession("bucket001", new IGridFsSessionExecutor<String>() {
        @Override
        public String execute(IGridFsSession session) throws Exception {
            return session.upload(fileName, originInputStream, new GridFSUploadOptions());
        }
    });
}
```



**示例：** 通过以上两种方式上传文件时都可以自定义文件唯一标识

```java
public String uploadFromFile(final String id, final File originFile) throws Exception {
    return MongoDB.get().openGridFsSession("bucket001", new IGridFsSessionExecutor<String>() {
        @Override
        public String execute(IGridFsSession session) throws Exception {
            return session.upload(id, originFile, new GridFSUploadOptions());
        }
    });
}
```



### 下载文件（Download）

**示例：** 下载指定唯一标识的文件到目标输出流

```java
public void downloadToStream(String id, OutputStream distOutputStream) throws Exception {
    try (IGridFsSession session = MongoDB.get().openGridFsSession("bucket001")) {
        session.download(id, distOutputStream);
    }
}
```



**示例：** 下载指定唯一标识的文件到目标文件

```java
public void downloadToFile(String id, File distFile) throws Exception {
    try (IGridFsSession session = MongoDB.get().openGridFsSession("bucket001")) {
        session.download(id, distFile);
    }
}
```



### 文件重命名（Rename）

**示例：** 为指定唯一标识的文件重新命名

```java
public void rename(String id, String newFileName) throws Exception {
    try (IGridFsSession session = MongoDB.get().openGridFsSession("bucket001")) {
        session.rename(id, newFileName);
    }
}
```



### 删除文件（Remove）

**示例：** 删除指定唯一标识的文件

```java
public void remove(String id) throws Exception {
    try (IGridFsSession session = MongoDB.get().openGridFsSession("bucket001")) {
        session.remove(id);
    }
}
```



**示例：** 批量删除文件

```java
public void removeAll(Collection<String> ids) throws Exception {
    try (IGridFsSession session = MongoDB.get().openGridFsSession("bucket001")) {
        session.remove(ids);
    }
}
```



## 结果集（ResultSet）

MongoDB 持久化模块与 JDBC 持久化模块一样，统一使用 `IResultSet` 接口将数据查询的结果集合进行封装并集成分页参数，下面通过一段代码来了解它：

```java
IResultSet<UserEntity> users = MongoDB.get().openSession(new IMongoSessionExecutor<IResultSet<UserEntity>>() {
    @Override
    public IResultSet<UserEntity> execute(IMongoSession session) throws Exception {
        return session.find(UserEntity.class, Query.create(), Page.create());
    }
});
// 返回当前是否分页查询
boolean isPaginated = results.isPaginated();
// 当前结果集是否可用，即是否为空或元素数量为0
boolean isAvailable = results.isResultsAvailable();
// 返回当前页号，若未分页则返回0
int pNumber = results.getPageNumber();
// 返回每页记录数，若未分页则返回0
int pSize = results.getPageSize();
// 返回总页数，若未分页则返回0
int pCount = results.getPageCount();
// 返回总记录数，若未分页则返回0
long rCount = results.getRecordCount();
// 返回结果集数据
List<UserEntity> users = results.getResultData();
```




## 操作器（IOperator）

本节主要介绍 MongoDB 持久化模块从 `v2.x` 版本开始新增的特性，主要用于辅助编写基于 Bson 格式的组合查询条件表达式。



### 查询对象（Query）

用于组合表达式构建完整查询条件。



构建查询条件可以通过以下两种方式编写，其运行结果是一致的，在编写复杂查询时 **方式二** 会更为简洁一些。



**方式一：** 基于 Query 类构建

```java
Query.create().and(
    Query.create().cond(UserEntity.FIELDS.NICKNAME, Operator.create().eq("suninformation")),
    Query.create().cond(UserEntity.FIELDS.GENDER, Operator.create().eq("M"))
);
```

**方式二：** 基于 QueryBuilder 类构建

```java
new QueryBuilder() {{
    and(query(UserEntity.FIELDS.NICKNAME, operator().eq("suninformation")),
        query(UserEntity.FIELDS.GENDER, operator().eq("M")));
}};
```



### 表达式对象（Exp）

用于查询语句中的表达式封装，根据 MongoDB 中所提供的表达式，按其类型划分为以下几种常用表达式对象：



#### ArrayExp：数组查询运算符

| 示例                              | 描述                                                         |
| --------------------------------- | ------------------------------------------------------------ |
| `ArrayExp.all(params)`            | 匹配包含查询中指定的所有元素的数组。                         |
| `ArrayExp.elemMatch(operator...)` | 如果 array 字段中的元素符合所有指定 $elemMatch 条件，则选择文档。 |
| `ArrayExp.size(size)`             | 如果数组字段为指定大小，则选择文档。                         |



#### ComparisonExp：比较查询运算符

| 示例                            | 描述                       |
| ------------------------------- | -------------------------- |
| `ComparisonExp.cmp(exp1, exp2)` | 两个值的比较。             |
| `ComparisonExp.eq(params)`      | 匹配等于指定值的值。       |
| `ComparisonExp.gt(params)`      | 匹配大于指定值的值。       |
| `ComparisonExp.gte(params)`     | 匹配大于或等于指定值的值。 |
| `ComparisonExp.in(params)`      | 匹配数组中指定的任何值。   |
| `ComparisonExp.lt(params)`      | 匹配小于指定值的值。       |
| `ComparisonExp.lte(params)`     | 匹配小于或等于指定值的值。 |
| `ComparisonExp.ne(params)`      | 匹配所有不等于指定值的值。 |
| `ComparisonExp.nin(params)`     | 不匹配数组中指定的任何值。 |



#### ElementExp：元素查询运算符

| 示例                        | 描述                             |
| --------------------------- | -------------------------------- |
| `ElementExp.exists(exists)` | 匹配具有指定字段的文档。         |
| `ElementExp.type(type)`     | 如果字段是指定类型，则选择文档。 |



#### EvaluationExp：评估查询运算符

| 示例                                    | 描述                                           |
| --------------------------------------- | ---------------------------------------------- |
| `EvaluationExp.mod(divisor, remainder)` | 对字段的值执行模运算并选择具有指定结果的文档。 |
| `EvaluationExp.regex(regex)`            | 选择值与指定的正则表达式匹配的文档。           |
| `EvaluationExp.text(search, language)`  | 执行文本搜索。                                 |
| `EvaluationExp.where(jsFunction)`       | 匹配满足 JavaScript 表达式的文档。             |



#### LogicalExp：逻辑查询运算符

| 示例                         | 描述                                                         |
| ---------------------------- | ------------------------------------------------------------ |
| `LogicalExp.and(queries...)` | 使用逻辑 AND 连接查询子句，返回与这两个子句条件匹配的所有文档。 |
| `LogicalExp.not(operator)`   | 反转查询表达式的效果，并返回与查询表达式不匹配的文档。       |
| `LogicalExp.nor(queries...)` | 用逻辑 NOR 连接查询子句，返回所有不能匹配这两个子句的文档。  |
| `LogicalExp.or(queries...)`  | 用逻辑 OR 连接查询子句，返回与任一子句条件匹配的所有文档。   |



#### ProjectionExp

| 示例                                   | 描述 |
| -------------------------------------- | ---- |
| `ProjectionExp.elemMatch(operator...)` |      |
| `ProjectionExp.meta(meta)`             |      |
| `ProjectionExp.slice(sikp, limit)`     |      |



#### UpdateExp：更新运算符

| 示例                                  | 描述                                                         |
| ------------------------------------- | ------------------------------------------------------------ |
| `UpdateExp.inc(field, amount)`        | 将字段的值增加指定的数量。                                   |
| `UpdateExp.mul(field, number)`        | 将字段的值乘以指定的数量。                                   |
| `UpdateExp.rename(field, newName)`    | 重命名字段。                                                 |
| `UpdateExp.setOnInsert(field, value)` | 如果更新导致插入文档，则设置字段的值。对修改现有文档的更新操作没有影响。 |
| `UpdateExp.set(field, value)`         | 设置文档中字段的值。                                         |
| `UpdateExp.unset(field...)`           | 从文档中删除指定的字段。                                     |
| `UpdateExp.min(value)`                | 仅当指定值小于现有字段值时才更新该字段。                     |
| `UpdateExp.max(value)`                | 仅当指定值大于现有字段值时才更新该字段。                     |
| `UpdateExp.addToSet(field, value)`    | 仅当元素不存在于集合中时才将它们添加到数组中。               |
| `UpdateExp.each(value)`               | 修改$push和$addToSet运算符以附加多个项以进行数组更新。       |
| `UpdateExp.sort(asc)`                 | 修改$push运算符以对存储在数组中的文档重新排序。              |
| `UpdateExp.position(position)`        | 修改$push运算符以指定要添加元素的数组中的位置。              |
| `UpdateExp.isolated()`                |                                                              |
| `UpdateExp.push(field, value)`        | 将项目添加到数组。                                           |
| `UpdateExp.pushAll(field, value...)`  | 从数组中删除所有匹配的值。                                   |
| `UpdateExp.pull(field, query)`        | 删除与指定查询匹配的所有数组元素。                           |
| `UpdateExp.pop(field, first)`         | 删除数组的第一项或最后一项。                                 |



#### Aggregation：聚合管道阶段

| 示例                                | 描述                                                         |
| ----------------------------------- | ------------------------------------------------------------ |
| `Aggregation.project(fields)`       | 重塑流中的每个文档，例如通过添加新字段或删除现有字段。对于每个输入文档，输出一个文档。 |
| `Aggregation.match(query)`          | 过滤文档流以仅允许匹配的文档未经修改地传递到下一个管道阶段。 $match 使用标准的 MongoDB 查询。对于每个输入文档，输出一个文档(匹配)或零文档(不匹配)。 |
| `Aggregation.redact(expression)`    | 通过基于文档本身中存储的信息限制每个文档的内容来重塑流中的每个文档。包含$project和$match的功能。可用于实现字段级编辑。对于每个输入文档，输出一个或零个文档。 |
| `Aggregation.limit(n)`              | 将未修改的前 n 个文档传递给管道，其中 n 是指定的限制。对于每个输入文档，输出一个文档(对于前 n 个文档)或零文档(在前 n 个文档之后)。 |
| `Aggregation.skip(n)`               | 跳过前 n 个文档，其中 n 是指定的跳过编号，并将未修改的其余文档传递给管道。对于每个输入文档，输出零文档(对于前 n 个文档)或一个文档(如果在前 n 个文档之后)。 |
| `Aggregation.unwind(field)`         | 从输入文档解构 array 字段以输出每个元素的文档。每个输出文档都使用元素 value 替换 array。对于每个输入文档，输出 n 个文档，其中 n 是 array 元素的数量，对于空 array 可以为零。 |
| `Aggregation.group(id, queries...)` | 按指定的标识符表达式对文档进行分组，并将累加器表达式(如果指定)应用于每个 group。消耗所有输入文档，并为每个不同的 group 输出一个文档。输出文档仅包含标识符字段，如果指定，则包含累积字段。 |
| `Aggregation.sort(orderBy)`         | 按指定的排序 key 重新排序文档流。只有顺序改变;文档保持不变。对于每个输入文档，输出一个文档。 |
| `Aggregation.out(targetCollection)` | 将聚合管道的结果文档写入集合。要使用 $out 阶段，它必须是管道中的最后一个阶段。 |



#### OrderBy：排序对象

**示例代码：**

```java
OrderBy orderBy = OrderBy.create()
    .asc(IMongo.GridFs.UPLOAD_DATE).desc(IMongo.GridFs.FILE_NAME);
System.out.println(orderBy.toBson().toString());
```

**执行结果：**

```json
{"uploadDate": -1, "filename": 1}
```



### 辅助类

在 MongoDB 持久化模块中会用到以下几个辅助工具类，它们都属于 YMP 框架持久化包中提供的通用类型。与 JDBC 持久化模块中的用法有些许简化，具体使用方法请仔细阅读其文字描述和示例代码。



#### Fields：字段名称集合

用于辅助传递多个数据表字段名称。

**示例代码：**

```java
// 创建Fields对象
Fields fields = Fields.create(UserEntity.FIELDS.NICKNAME, "gender");
// 标记集合中的字段为排除的
fields.excluded(true);
// 判断是否存在排除标记
fields.isExcluded();
// 输出
System.out.println(fields.fields());
```

**执行结果：**

```shell
[nickname, gender]
```



#### Params：参数集合

用于辅助传递多个参数值对象。

**示例代码：**

```java
// 创建Params对象，任何类型参数
Params params = Params.create("p1", 2, false, 0.1).add("param");
// 
params = Params.create().add("paramN").add(params);
// 输出
System.out.println(params.params());
```

**执行结果：**

```shell
[paramN, p1, 2, false, 0.1, param]
```



#### Pages：分页参数

**示例代码：**

```java
// 默认查询第1页，每页20条记录
Page.create();
// 查询第2页, 每页10条记录
Page.create(2).pageSize(10);
// 查询第1页, 每页10条记录, 但不统计总记录数
Page.create(1).pageSize(10).count(false);
// 根据参数值尝试创建分页对象，若page或pageSize参数为空或小于等于0则返回null
Page.createIfNeed(1, Page.DEFAULT_PAGE_SIZE);
```

