---
sidebar_position: 1
slug: jdbc
---

# JDBC

JDBC 持久化模块针对关系型数据库（RDBMS）数据存取的一套简单解决方案，主要关注数据存取的效率、易用性、稳定和透明，其具备以下功能特征：

- 基于 JDBC 框架 API 进行轻量封装，结构简单、便于开发、调试和维护；
- 优化批量数据更新、标准化结果集、预编译 SQL 语句处理；
- 支持单实体 ORM 操作，无需编写 SQL 语句；
- 提供脚手架工具，快速生成数据实体类，支持链式调用；
- 支持通过存储器注解自定义 SQL 语句或从配置文件中动态加载 SQL 并自动执行；
- 支持结果集与值对象的自动装配，支持自定义装配规则；
- 支持多数据源，默认支持 C3P0、DBCP、Druid、HikariCP、JNDI 连接池配置，支持数据源扩展；
- 支持多种数据库（如：Oracle、MySQL、SQLServer、SQLite、H2、PostgreSQL 等）；
- 支持面向对象的数据库查询封装，有助于减少或降低程序编译期错误；
- 支持数据库事务嵌套；
- 支持数据库视图和存储过程；



## Maven包依赖

```xml
<dependency>
    <groupId>net.ymate.platform</groupId>
    <artifactId>ymate-platform-persistence-jdbc</artifactId>
    <version>2.1.0</version>
</dependency>
```



## 模块配置

### 配置文件参数说明

```properties
#-------------------------------------
# JDBC持久化模块初始化参数
#-------------------------------------

# 默认数据源名称, 默认值: default
ymp.configs.persistence.jdbc.ds_default_name=

# 数据源列表, 多个数据源名称间用'|'分隔, 默认值: default
ymp.configs.persistence.jdbc.ds_name_list=

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

# 数据源适配器, 可选值为已知适配器名称或自定义适配置类名称, 默认值: default, 目前支持已知适配器[default|dbcp|c3p0|druid|hikaricp|jndi|...]
ymp.configs.persistence.jdbc.ds.default.adapter_class=dbcp

# 数据源适配器配置文件，可选参数，若未设置或设置的文件路径无效将被忽略，默认值为空
ymp.configs.persistence.jdbc.ds.default.config_file=

# 数据库类型, 可选参数, 默认值将通过连接字符串分析获得, 目前支持[mysql|oracle|sqlserver|db2|sqlite|postgresql|hsqldb|h2]
ymp.configs.persistence.jdbc.ds.default.type=

# 数据库方言, 可选参数, 自定义方言将覆盖默认配置
ymp.configs.persistence.jdbc.ds.default.dialect_class=

# 数据库连接驱动, 可选参数, 框架默认将根据数据库类型进行自动匹配
ymp.configs.persistence.jdbc.ds.default.driver_class=

# 数据库连接字符串, 必填参数
ymp.configs.persistence.jdbc.ds.default.connection_url=jdbc:mysql://localhost:3306/db_name?useUnicode=true&useSSL=false&characterEncoding=UTF-8

# 数据库访问用户名称, 必填参数
ymp.configs.persistence.jdbc.ds.default.username=root

# 数据库访问密码, 可选参数, 经过默认密码处理器加密后的admin字符串为wRI2rASW58E
ymp.configs.persistence.jdbc.ds.default.password=wRI2rASW58E

# 数据库访问密码是否已加密, 默认值: false
ymp.configs.persistence.jdbc.ds.default.password_encrypted=true

# 数据库密码处理器, 可选参数, 用于对已加密码数据库访问密码进行解密, 默认值: 空
ymp.configs.persistence.jdbc.ds.default.password_class=
```



### 配置注解参数说明

:::tip **特别说明：** 

当 JDBC 持久化模块初始化时，若在配置文件中存在数据源相关配置，则基于注解的数据源配置将全部失效。

:::



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
| showSql            | 是否显示执行的 SQL 语句    |
| stackTraces        | 是否开启堆栈跟踪         |
| stackTraceDepth    | 堆栈跟踪层级深度         |
| stackTracePackages | 堆栈跟踪过滤包名前缀集合 |
| tablePrefix        | 数据库表前缀名称         |
| identifierQuote    | 数据库引用标识符         |



## 数据源（DataSource）

### 多数据源连接

JDBC 持久化模块默认支持多数据源配置，下面通过简单的配置来展示如何连接多个数据库：

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

从上述配置中可以看出，配置不同的数据源时只需要定义数据源名称列表，再根据列表逐一配置即可。

通过注解方式配置多数据源，如下所示：

```java
@DatabaseConf(dsDefaultName = "default", value = {
        @DatabaseDataSource(name = "default", 
                            connectionUrl = "jdbc:mysql://localhost:3306/mydb",
                            username = "root", 
                            password = "123456"),
        @DatabaseDataSource(name = "oracledb", 
                            connectionUrl = "jdbc:oracle:thin:@localhost:1521:ORCL",
                            username = "ORCL", 
                            password = "123456")
})
```



### 连接池配置

JDBC 持久化模块提供的数据源类型如下：

| 名称     | 类型                      | 描述                                                         |
| -------- | ------------------------- | ------------------------------------------------------------ |
| default  | DefaultDataSourceAdapter  | 默认数据源适配器，通过 DriverManager 直接连接数据库，建议仅用于测试。 |
| c3p0     | C3P0DataSourceAdapter     | 基于 C3P0 连接池的数据源适配器。                             |
| dbcp     | DBCPDataSourceAdapter     | 基于 DBCP 连接池的数据源适配器。                             |
| druid    | DruidDataSourceAdapter    | 基于阿里巴巴开源的 Druid 连接池的数据源适配器。              |
| hikaricp | HikariCPDataSourceAdapter | 基于 HikariCP 连接池的数据源适配器。                         |
| jndi     | JNDIDataSourceAdapter     | 基于 JNDI 的数据源适配器。                                   |

只需根据实际情况调整对应数据源名称的配置，如：

```properties
ymp.configs.persistence.jdbc.ds.default.adapter_class=dbcp
```

通过注解配置方式，如下所示：

```java
@DatabaseConf(dsDefaultName = "default",
        value = {
                @DatabaseDataSource(name = "default",
                        connectionUrl = "jdbc:mysql://localhost:3306/mydb",
                        username = "root",
                        password = "123456",
                        adapterClass = DBCPDataSourceAdapter.class)
        })
```

针对于 dbcp、druid、hikaricp 和 c3p0 连接池的配置文件及内容，请将对应的配置文件（如：`dbcp.properties`、`c3p0.properties`等）放置在工程的 `classpath` 根路径下，若上述配置文件不存在，JDBC 持久化模块在初始化时将自动创建。

另外，dbcp、druid、hikaricp 和 c3p0 连接池支持根据数据源名称进行单独配置（如：`dbcp_oracledb.properties`，此文件将优先于 `dbcp.properties` 被加载），其中 druid 连接池可以兼容 dbcp 连接池的配置文件。

当然，也可以通过 `IDatabaseDataSourceAdapter` 接口自行实现，框架针对该接口提供了 `AbstractDatabaseDataSourceAdapter` 抽象类，直接继承即可。



### 数据库连接持有者（IDatabaseConnectionHolder）

用于记录真正的数据库连接对象（Connection）原始的状态及与数据源对应关系，在 JDBC 持久化模块中获取到的所有连接对象均由数据库连接持有者对象包装，基于数据库连接持有者接口可以进行如下操作：

```java
public class Main {

    public static void main(String[] args) throws Exception {
        try (IApplication application = YMP.run(args)) {
            if (application.isInitialized()) {
                // 获取当前容器内JDBC模块实例
                IDatabase database = application.getModuleManager().getModule(JDBC.class);
                // 获取默认数据源的连接持有者实例，等同于：database.getConnectionHolder("default");
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
            }
        }
    }
}
```



## 数据实体（Entity）

数据实体是以对象的形式与数据库表之间的一种映射关系，实体中的属性与表中字段一一对应。

数据实体类包含以下几个部份：

- 基本属性：注解配置属性与字段之间的关系及属性的 Getter 和 Setter 方法。
- FIELDS：字段名常量。
- Builder：基本属性构建器类，支持以链式调用方式为实体属性赋值。
- FieldConditionBuilder：属性条件构建器类，为具体实体属性构建字段查询条件。



### 实体类与表对应关系示例

假定数据库中的 `ym_user` 数据表结构如下：

```sql
CREATE TABLE `ym_user` (
  `id` varchar(32) NOT NULL COMMENT '用户唯一标识',
  `username` varchar(32) DEFAULT NULL COMMENT '用户名称',
  `nickname` varchar(32) DEFAULT NULL COMMENT '昵称',
  `gender` varchar(1) DEFAULT NULL COMMENT '性别',
  `age` int(2) unsigned DEFAULT '0' COMMENT '年龄',
  `avatar_url` varchar(255) DEFAULT NULL COMMENT '头像URL地址',
  `password` varchar(32) DEFAULT NULL COMMENT '登录密码',
  `email` varchar(100) DEFAULT NULL COMMENT '电子邮件',
  `mobile` varchar(20) DEFAULT NULL COMMENT '手机号码',
  `type` smallint(2) unsigned DEFAULT '0' COMMENT '类型',
  `status` smallint(2) unsigned DEFAULT '0' COMMENT '状态',
  `create_time` bigint(13) NOT NULL COMMENT '注册时间',
  `last_modify_time` bigint(13) DEFAULT '0' COMMENT '最后修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息';
```

通过实体生成工具自动构建的实体类如下：

```java
@Entity(UserEntity.TABLE_NAME)
@Comment("用户信息")
public class UserEntity extends BaseEntity<UserEntity, String> {

    private static final long serialVersionUID = 1L;

    @Id
    @Property(name = FIELDS.ID, nullable = false, length = 32)
    @Comment("用户唯一标识")
    @PropertyState(propertyName = FIELDS.ID)
    private String id;

    @Property(name = FIELDS.USERNAME, length = 32)
    @Comment("用户名称")
    @PropertyState(propertyName = FIELDS.USERNAME)
    private String username;

    @Property(name = FIELDS.NICKNAME, length = 32)
    @Comment("昵称")
    @PropertyState(propertyName = FIELDS.NICKNAME)
    private String nickname;

    @Property(name = FIELDS.GENDER, length = 1)
    @Comment("性别")
    @PropertyState(propertyName = FIELDS.GENDER)
    private String gender;

    @Property(name = FIELDS.AGE, length = 2)
    @Comment("年龄")
    @PropertyState(propertyName = FIELDS.AGE)
    private Integer age;

    @Property(name = FIELDS.AVATAR_URL, length = 255)
    @Comment("头像URL地址")
    @PropertyState(propertyName = FIELDS.AVATAR_URL)
    private String avatarUrl;

    @Property(name = FIELDS.PASSWORD, length = 32)
    @Comment("登录密码")
    @PropertyState(propertyName = FIELDS.PASSWORD)
    private String password;

    @Property(name = FIELDS.EMAIL, length = 100)
    @Comment("电子邮件")
    @PropertyState(propertyName = FIELDS.EMAIL)
    private String email;

    @Property(name = FIELDS.MOBILE, length = 20)
    @Comment("手机号码")
    @PropertyState(propertyName = FIELDS.MOBILE)
    private String mobile;

    @Property(name = FIELDS.TYPE, unsigned = true, length = 2)
    @Default("0")
    @Comment("类型")
    @PropertyState(propertyName = FIELDS.TYPE)
    private Integer type;

    @Property(name = FIELDS.STATUS, unsigned = true, length = 2)
    @Default("0")
    @Comment("状态")
    @PropertyState(propertyName = FIELDS.STATUS)
    private Integer status;

    @Property(name = FIELDS.CREATE_TIME, nullable = false, length = 13)
    @Comment("注册时间")
    @PropertyState(propertyName = FIELDS.CREATE_TIME)
    @Readonly
    private Long createTime;

    @Property(name = FIELDS.LAST_MODIFY_TIME, length = 13)
    @Default("0")
    @Comment("最后修改时间")
    @PropertyState(propertyName = FIELDS.LAST_MODIFY_TIME)
    private Long lastModifyTime;

    public UserEntity() {
    }

    public UserEntity(IDatabase dbOwner) {
        super(dbOwner);
    }

    public UserEntity(String id, Long createTime) {
        this.id = id;
        this.createTime = createTime;
    }

    public UserEntity(IDatabase dbOwner, String id, Long createTime) {
        super(dbOwner);
        this.id = id;
        this.createTime = createTime;
    }

    public UserEntity(String id, String username, String nickname, String gender, Integer age, String avatarUrl, String password, String email, String mobile, Integer type, Integer status, Long createTime, Long lastModifyTime) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
        this.gender = gender;
        this.age = age;
        this.avatarUrl = avatarUrl;
        this.password = password;
        this.email = email;
        this.mobile = mobile;
        this.type = type;
        this.status = status;
        this.createTime = createTime;
        this.lastModifyTime = lastModifyTime;
    }

    public UserEntity(IDatabase dbOwner, String id, String username, String nickname, String gender, Integer age, String avatarUrl, String password, String email, String mobile, Integer type, Integer status, Long createTime, Long lastModifyTime) {
        super(dbOwner);
        this.id = id;
        this.username = username;
        this.nickname = nickname;
        this.gender = gender;
        this.age = age;
        this.avatarUrl = avatarUrl;
        this.password = password;
        this.email = email;
        this.mobile = mobile;
        this.type = type;
        this.status = status;
        this.createTime = createTime;
        this.lastModifyTime = lastModifyTime;
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

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
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

    public Long getLastModifyTime() {
        return lastModifyTime;
    }

    public void setLastModifyTime(Long lastModifyTime) {
        this.lastModifyTime = lastModifyTime;
    }

    public Builder bind() {
        return new Builder(this);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(IDatabase dbOwner) {
        return new Builder(dbOwner);
    }

    public static class Builder {

        private final UserEntity targetEntity;

        public Builder() {
            targetEntity = new UserEntity();
        }

        public Builder(IDatabase dbOwner) {
            targetEntity = new UserEntity(dbOwner);
        }

        public Builder(UserEntity targetEntity) {
            this.targetEntity = targetEntity;
        }

        public UserEntity build() {
            return targetEntity;
        }

        public IDatabaseConnectionHolder connectionHolder() {
            return targetEntity.getConnectionHolder();
        }

        public Builder connectionHolder(IDatabaseConnectionHolder connectionHolder) {
            targetEntity.setConnectionHolder(connectionHolder);
            return this;
        }

        public IDatabase dbOwner() {
            return targetEntity.getDbOwner();
        }

        public Builder dbOwner(IDatabase dbOwner) {
            targetEntity.setDbOwner(dbOwner);
            return this;
        }

        public String dataSourceName() {
            return targetEntity.getDataSourceName();
        }

        public Builder dataSourceName(String dataSourceName) {
            targetEntity.setDataSourceName(dataSourceName);
            return this;
        }

        public IShardingable shardingable() {
            return targetEntity.getShardingable();
        }

        public Builder shardingable(IShardingable shardingable) {
            targetEntity.setShardingable(shardingable);
            return this;
        }

        public String id() {
            return targetEntity.getId();
        }

        public Builder id(String id) {
            targetEntity.setId(id);
            return this;
        }

        public String username() {
            return targetEntity.getUsername();
        }

        public Builder username(String username) {
            targetEntity.setUsername(username);
            return this;
        }

        public String nickname() {
            return targetEntity.getNickname();
        }

        public Builder nickname(String nickname) {
            targetEntity.setNickname(nickname);
            return this;
        }

        public String gender() {
            return targetEntity.getGender();
        }

        public Builder gender(String gender) {
            targetEntity.setGender(gender);
            return this;
        }

        public Integer age() {
            return targetEntity.getAge();
        }

        public Builder age(Integer age) {
            targetEntity.setAge(age);
            return this;
        }

        public String avatarUrl() {
            return targetEntity.getAvatarUrl();
        }

        public Builder avatarUrl(String avatarUrl) {
            targetEntity.setAvatarUrl(avatarUrl);
            return this;
        }

        public String password() {
            return targetEntity.getPassword();
        }

        public Builder password(String password) {
            targetEntity.setPassword(password);
            return this;
        }

        public String email() {
            return targetEntity.getEmail();
        }

        public Builder email(String email) {
            targetEntity.setEmail(email);
            return this;
        }

        public String mobile() {
            return targetEntity.getMobile();
        }

        public Builder mobile(String mobile) {
            targetEntity.setMobile(mobile);
            return this;
        }

        public Integer type() {
            return targetEntity.getType();
        }

        public Builder type(Integer type) {
            targetEntity.setType(type);
            return this;
        }

        public Integer status() {
            return targetEntity.getStatus();
        }

        public Builder status(Integer status) {
            targetEntity.setStatus(status);
            return this;
        }

        public Long createTime() {
            return targetEntity.getCreateTime();
        }

        public Builder createTime(Long createTime) {
            targetEntity.setCreateTime(createTime);
            return this;
        }

        public Long lastModifyTime() {
            return targetEntity.getLastModifyTime();
        }

        public Builder lastModifyTime(Long lastModifyTime) {
            targetEntity.setLastModifyTime(lastModifyTime);
            return this;
        }
    }

    public interface FIELDS {
        String ID = "id";
        String USERNAME = "username";
        String NICKNAME = "nickname";
        String GENDER = "gender";
        String AGE = "age";
        String AVATAR_URL = "avatar_url";
        String PASSWORD = "password";
        String EMAIL = "email";
        String MOBILE = "mobile";
        String TYPE = "type";
        String STATUS = "status";
        String CREATE_TIME = "create_time";
        String LAST_MODIFY_TIME = "last_modify_time";
    }

    public static final String TABLE_NAME = "user";

    public static FieldConditionBuilder conditionBuilder() {
        return new FieldConditionBuilder();
    }

    public static FieldConditionBuilder conditionBuilder(String prefix) {
        return new FieldConditionBuilder(prefix);
    }

    public static FieldConditionBuilder conditionBuilder(Query<?> query) {
        return conditionBuilder(query, null);
    }

    public static FieldConditionBuilder conditionBuilder(Query<?> query, String prefix) {
        return new FieldConditionBuilder(query.owner(), query.dataSourceName(), prefix);
    }

    public static FieldConditionBuilder conditionBuilder(UserEntity entity) {
        return conditionBuilder(entity, null);
    }

    public static FieldConditionBuilder conditionBuilder(UserEntity entity, String prefix) {
        return new FieldConditionBuilder(entity.doGetSafeOwner(), entity.getDataSourceName(), prefix);
    }

    public static FieldConditionBuilder conditionBuilder(IDatabase owner, String prefix) {
        return new FieldConditionBuilder(owner, prefix);
    }

    public static FieldConditionBuilder conditionBuilder(IDatabase owner, String dataSourceName, String prefix) {
        return new FieldConditionBuilder(owner, dataSourceName, prefix);
    }

    public static class FieldConditionBuilder extends AbstractFieldConditionBuilder {

        public FieldConditionBuilder() {
            super(null, null, null);
        }

        public FieldConditionBuilder(String prefix) {
            super(null, null, prefix);
        }

        public FieldConditionBuilder(Query<?> query) {
            super(query.owner(), null, null);
        }

        public FieldConditionBuilder(Query<?> query, String prefix) {
            super(query.owner(), query.dataSourceName(), prefix);
        }

        public FieldConditionBuilder(IDatabase owner) {
            super(owner, null, null);
        }

        public FieldConditionBuilder(IDatabase owner, String prefix) {
            super(owner, null, prefix);
        }

        public FieldConditionBuilder(IDatabase owner, String dataSourceName, String prefix) {
            super(owner, dataSourceName, prefix);
        }

        public FieldCondition id() {
            return createFieldCondition(UserEntity.FIELDS.ID);
        }

        public FieldCondition username() {
            return createFieldCondition(UserEntity.FIELDS.USERNAME);
        }

        public FieldCondition nickname() {
            return createFieldCondition(UserEntity.FIELDS.NICKNAME);
        }

        public FieldCondition gender() {
            return createFieldCondition(UserEntity.FIELDS.GENDER);
        }

        public FieldCondition age() {
            return createFieldCondition(UserEntity.FIELDS.AGE);
        }

        public FieldCondition avatarUrl() {
            return createFieldCondition(UserEntity.FIELDS.AVATAR_URL);
        }

        public FieldCondition password() {
            return createFieldCondition(UserEntity.FIELDS.PASSWORD);
        }

        public FieldCondition email() {
            return createFieldCondition(UserEntity.FIELDS.EMAIL);
        }

        public FieldCondition mobile() {
            return createFieldCondition(UserEntity.FIELDS.MOBILE);
        }

        public FieldCondition type() {
            return createFieldCondition(UserEntity.FIELDS.TYPE);
        }

        public FieldCondition status() {
            return createFieldCondition(UserEntity.FIELDS.STATUS);
        }

        public FieldCondition createTime() {
            return createFieldCondition(UserEntity.FIELDS.CREATE_TIME);
        }

        public FieldCondition lastModifyTime() {
            return createFieldCondition(UserEntity.FIELDS.LAST_MODIFY_TIME);
        }
    }
}
```



### 数据实体注解

#### @Entity

声明一个类为数据实体对象。

| 配置项 | 描述                                         |
| ------ | -------------------------------------------- |
| value  | 实体名称（数据库表名称），默认采用当前类名称 |



#### @Id

声明一个类成员为主键，与 `@Property` 注解配合使用，无参数。



#### @Property

声明一个类成员为数据实体属性。

| 配置项          | 描述                                                         |
| --------------- | ------------------------------------------------------------ |
| name            | 实现属性名称，默认采用当前成员名称                           |
| autoincrement   | 是否为自动增长，默认为 `false`                               |
| sequenceName    | 序列名称，适用于类似 Oracle 等数据库，配合 `autoincrement` 参数一同使用 |
| useKeyGenerator | 指定键值生成器名称，默认为空表示不启用（仅当非自动增长且主键值为空时调用）。<br />目前框架提供了 `IKeyGenerator.UUID` 键值生成器，其采用 UUID 策略。<br />可通过实现 `IKeyGenerator` 接口自行实现并通过 `SPI` 方式向框架注册。 |
| nullable        | 允许为空，默认为 `true`                                      |
| unsigned        | 是否为无符号，默认为 `false`                                 |
| length          | 数据长度，默认为 `0` 表示不限制                              |
| decimals        | 小数位数，默认为 `0` 表示无小数                              |
| type            | 数据类型，默认为 `Type.FIELD.UNKNOWN`                        |

**示例：** 使用自定义主键生成器 `custom` 为实体类的 `id` 属性自动赋值。

**步骤1：** 编写自定义主键生成器，为其命名为 `custom`。

```java
package net.ymate.platform.examples.persistence.jdbc;

import net.ymate.platform.core.persistence.IKeyGenerator;
import net.ymate.platform.core.persistence.IPersistence;
import net.ymate.platform.core.persistence.base.IEntity;
import net.ymate.platform.core.persistence.base.PropertyMeta;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

@KeyGenerator(value = "custom")
public class CustomKeyGenerator implements IKeyGenerator {
    @Override
    public Object generate(IPersistence<?, ?, ?> owner, PropertyMeta propertyMeta, IEntity<?> entity) {
        // 判断当前主键属性类型，仅对字符串类型生成
        if (propertyMeta.getField().getType().equals(String.class)) {
            if (entity instanceof UserEntity) {
                String username = ((UserEntity) entity).getUsername();
                String mobile = ((UserEntity) entity).getMobile();
                if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(mobile)) {
                    // 将用户密和密码值联合进行MD5运算
                    return DigestUtils.md5Hex(username + mobile);
                }
                throw new IllegalArgumentException("username and mobile can not be empty.");
            }
        }
        return null;
    }
}
```



**步骤2：** 注册自定义主键生成器

方式一：通过在 `META-INF/service/`或`META-INF/service/internal/` 目录下对应的 `SPI` 配置文件中增加由 **步骤1** 创建的自定义主键生成器类名。配置文件名称应为 `net.ymate.platform.core.persistence.IKeyGenerator`，若不存在请手动创建并追加如下内容：

```properties
net.ymate.platform.examples.persistence.jdbc.CustomKeyGenerator
```

方式二：手动向主键生成器管理器进行注册。

```java
IKeyGenerator.Manager.registerKeyGenerator("custom", CustomKeyGenerator.class);
```



**步骤3：** 调整数据实体类的 `id` 属性 `@Property` 注解配置：`useKeyGenerator = "custom"`

```java
@Entity(UserEntity.TABLE_NAME)
@Comment("用户信息")
public class UserEntity extends BaseEntity<UserEntity, String> {

    private static final long serialVersionUID = 1L;

    @Id
    @Property(name = FIELDS.ID, nullable = false, length = 32, useKeyGenerator = "custom")
    @Comment("用户唯一标识")
    @PropertyState(propertyName = FIELDS.ID)
    private String id;
    
    ......
}
```



**步骤4：** 测试

下面的代码中包含了如何通过捕获 JDBC 模块初始化事件向管理器注册自定义主键生成器，如果与 `SPI` 方式同时使用会造成重复注册动作，尽管管理器会忽略重复注册的行为，但仍然建议避免重复操作，两种注册方式达到的目的是一样的，但更推荐使用 `SPI` 方式。

```java
@EnableAutoScan
@EnableBeanProxy
public class Starter implements IApplicationInitializer {

    static {
        System.setProperty(IApplication.SYSTEM_MAIN_CLASS, Starter.class.getName());
    }

    private static final Log LOG = LogFactory.getLog(Starter.class);
    
    @Override
    public void afterEventInit(IApplication application, Events events) {
        events.registerListener(ModuleEvent.class, new IEventListener<ModuleEvent>() {
            @Override
            public boolean handle(ModuleEvent context) {
                if (Objects.equals(JDBC.MODULE_NAME, context.getSource().getName()) 
                    && context.getEventName() == ModuleEvent.EVENT.MODULE_INITIALIZED) {
                    try {
                        IKeyGenerator.Manager.registerKeyGenerator("custom", CustomKeyGenerator.class);
                    } catch (Exception ignored) {
                    }
                }
                return false;
            }
        });
    }

    public static void main(String[] args) throws Exception {
        try (IApplication application = YMP.run(args, new Starter())) {
            UserEntity userEntity = UserEntity.builder()
                .username("abc")
                .mobile("13088888888")
                .createTime(System.currentTimeMillis()).build();
            LOG.info("Custom UserEntity Id: " + userEntity.save().getId());
        }
    }
}
```



#### @PK

声明一个类为某数据实体的复合主键对象，无参数。

示例：

```java
@PK
public class UserExtPK {

    @Property
    private String uid;

    @Property(name = "wx_id")
    private String wxId;

    // 省略Get/Set方法...
}

@Entity("user_ext")
public class UserExt {

    @Id
    private UserExtPK id;

    @Property(name = "open_id", nullable = false, length = 32)
    private String openId;

    // 省略Get/Set方法...
}
```



#### @Readonly

声明一个成员为只读属性，数据实体更新时其值将被忽略，与 `@Property` 注解配合使用，无参数。

示例：

```java
@Entity("demo")
public class Demo {

    @Id
    @Property
    private String id;

    @Property(name = "create_time")
    @Readonly
    private Date createTime;

    // 省略Get/Set方法...
}
```



#### @Indexes

声明一组数据实体的索引。

| 配置项 | 描述                   |
| ------ | ---------------------- |
| value  | 索引注解 `@Index` 集合 |



#### @Index

声明一个数据实体的索引。

| 配置项 | 描述                        |
| ------ | --------------------------- |
| name   | 索引名称                    |
| unique | 是否唯一索引，默认为 `true` |
| fields | 索引字段名称集合            |

示例：

```java
@Indexes({
        @Index(name="unique_uname", unique = true, fields = {UserEntity.FIELDS.USERNAME})
})
```



#### @Comment

实体或成员属性的注释内容。



#### @Default

为一个成员属性指定默认值。

| 配置项  | 描述                                                         |
| ------- | ------------------------------------------------------------ |
| value   | 默认值                                                       |
| ignored | 是否忽略（即该默认值仅用于生成 DDL 语句，主要是为了避免函数名称导致 SQL 执行错误），默认为 `false` |



### 自动生成实体类

YMP 框架自 `v1.x` 开始就支持通过数据库表结构自动生成实体类代码，所以 `v2.x` 版本不但重构了实体代码生成器，而且更简单好用！

**步骤1：**配置数据实体代码生成器所需参数：

```properties
#-------------------------------------
# JDBC数据实体代码生成器配置参数
#-------------------------------------

# 是否生成新的BaseEntity类, 默认值: false(即表示使用框架提供的BaseEntity类)
ymp.params.jdbc.use_base_entity=

# 是否使用类名后缀, 不使用和使用的区别如: User->UserModel, 默认值: false
ymp.params.jdbc.use_class_suffix=true

# 实体类名后缀, 默认值: model
ymp.params.jdbc.class_suffix=entity

# 是否采用链式调用模式, 默认值: false
ymp.params.jdbc.use_chain_mode=true

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

实际上你可以什么都不用配置（请参看以上配置项说明，根据实际情况进行调整），但使用过程中需要注意以下几点：

- 在多数据源模式下，需要指定具体数据源名称，否则代码生成器使用的是默认数据源；
- 如果使用的 JDBC 驱动是 `mysql-connector-java-6.x` 及以上版本时，则必须配置 `db_name` 和 `db_username` 参数；
- 实体及属性命名过滤器参数 `named_filter_class` 指定的类需要实现 `IEntityNamedFilter` 接口；



**步骤2：**添加插件配置，数据实体生成器是以 Maven 插件的形式提供的，需要在工程的 `pom.xml` 文件添加如下内容：

```xml
<plugin>
    <groupId>net.ymate.maven.plugins</groupId>
    <artifactId>ymate-maven-plugin</artifactId>
    <version>1.1-SNAPSHOT</version>
</plugin>
```

插件中默认已经包含 `mysql-connector-java-5.1.48` 驱动，若需要其它版本或其它类型数据库驱动时，需要在插件中配置相关依赖，如：

```xml
<plugin>
    <groupId>net.ymate.maven.plugins</groupId>
    <artifactId>ymate-maven-plugin</artifactId>
    <version>1.1-SNAPSHOT</version>
    <dependencies>
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>8.0.20</version>
        </dependency>
    </dependencies>
</plugin>
```



**步骤3：**在工程根路径下执行插件命令：

```shell
mvn ymate:entity -Doverwrite=true
```

插件命令参数说明：

| 参数       | 描述                                                         |
| ---------- | ------------------------------------------------------------ |
| dev        | 是否使用开发模式，默认为 `false`                             |
| overwrite  | 是否覆盖已存在的文件，默认为 `false`                         |
| cfgFile    | 加载指定的框架初始化配置文件，默认为空                       |
| dataSource | 指定数据源名称，默认为 `default`                             |
| view       | 是否为视图，默认为 `false`                                   |
| showOnly   | 是否仅在控制台输出结构信息（不生成任何文件），默认为 `false` |
| format     | 控制台输出格式，配合 `showOnly` 使用，可选值：`table` `markdown` `csv`，默认为 `table` |
| beanOnly   | 是否仅生成 JavaBean（非实体类），默认为 `false`              |
| apidocs    | 是否使用 `@ApiProperty` 文档注解，配合 `beanOnly` 使用，默认为 `false` |



通过插件生成的代码默认放置在 `src/main/java` 路径，当数据库表发生变化时，重新执行插件命令就可以快速更新数据实体对象，是不是很更方便呢，大家可以动手尝试一下！:p



### 数据实体操作

本小节所指的数据实体类必须是通过实体生成工具自动生成的并继承框架提供的 `BaseEntity` 抽象类。

#### 插入（Insert）

```java
UserEntity user = UserEntity.builder()
    .id(UUIDUtils.UUID())
    .username("suninformation")
    .nickname("有理想的鱼")
    .password(DigestUtils.md5Hex("123456"))
    .email("suninformation@163.com")
    .build();
// 执行数据插入
user.save();
// 或者在插入时也可以指定/排除某些字段
user.save(Fields.create(UserEntity.FIELDS.NICKNAME, UserEntity.FIELDS.EMAIL).excluded(true));
// 或者插入前判断记录是否已存在，若已存在则执行记录更新操作
user.saveOrUpdate();
// 或者插入前判断记录是否已存在，若已存在则执行记录更新操作时仅更新指定的字段
user.saveOrUpdate(Fields.create(UserEntity.FIELDS.NICKNAME, UserEntity.FIELDS.EMAIL));
```

#### 更新（Update）

```java
UserEntity user = UserEntity.builder()
	.id("bc19f5645aa9438089c5e9954e5f1ac5")
	.password(DigestUtils.md5Hex("654321"))
	.gender("F")
	.build();
// 执行记录更新
user.update();
// 或者仅更新指定的字段/排除某些字段
user.update(Fields.create(UserEntity.FIELDS.PASSWORD));
```

#### 查询（Find）

##### 方式一：根据记录ID加载

```java
UserEntity user = UserEntity.builder()
	.id("bc19f5645aa9438089c5e9954e5f1ac5")
    .build();
// 根据记录ID加载全部字段
user = user.load();
// 或者根据记录ID加载指定的字段
user = user.load(Fields.create(UserEntity.FIELDS.USERNAME, UserEntity.FIELDS.NICKNAME));
```

##### 方式二：根据实体属性设置条件

```java
UserEntity user = UserEntity.builder()
    .username("suninformation")
    .email("suninformation@163.com")
    .build();
// 非空属性之间将使用and条件连接，查询所有符合条件的记录并返回所有字段
IResultSet<UserEntity> users = user.find();
// 或者返回指定的字段
users = user.find(Fields.create(UserEntity.FIELDS.ID, UserEntity.FIELDS.PASSWORD));
// 非空属性之间将使用or条件连接，查询所有符合条件的记录并返回
users = user.matchAny().find();
```

##### 方式三：自定义属性条件并分页查询

```java
// 构建字段条件：邮件后缀为"@163.com"的记录
FieldCondition cond = UserEntity.conditionBuilder().email().like(Like.create("@163.com").endsWith());
// 构建Where对象，并设置按创建日期降序排列
Where where = Where.create(cond.build()).orderByDesc(UserEntity.FIELDS.CREATE_TIME);
// 执行分页查询每1页且每页10行记录，返回全部字段
IResultSet<UserEntity> users = new UserEntity().find(where, Page.create(1).pageSize(10));
```

##### 方式四：返回符合条件的第一条记录

```java
UserEntity user = UserEntity.builder()
    .username("suninformation")
    .password(DigestUtils.md5Hex("654321"))
    .build();
// 返回与用户名称和密码匹配的第一条记录
user = user.findFirst();
// 或者返回与用户名称和密码匹配的第一条记录的ID和NICKNAME字段
user = user.findFirst(Fields.create(UserEntity.FIELDS.ID, UserEntity.FIELDS.NICKNAME));
```

##### 方式五：统计符合条件的记录数

```java
UserEntity user = UserEntity.builder()
    .username("suninformation")
    .password(DigestUtils.md5Hex("654321"))
    .build();
// 返回与用户名称和密码匹配的记录数
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

基于数据实体类可以帮助你完成的事情还有很多，上面的示例仅是一部份比较典型的应用，请大家在实际应用中结合源码和API文档去尝试，也随时欢迎与您一起沟通、交流经验和建议。




## 事务（Transaction）

JDBC 持久化模块对数据库事务的处理是基于 YMP 框架的 AOP 特性实现的，任何被应用容器管理的对象都可以通过 `@Transaction` 注解开启事务。

:::tip **特别说明：**

- `@Transaction` 注解仅作用于公有非静态、非抽象且不属于 Object 基类方法上才能开启事务，不支持接口方法。
- 当类方法上声明的 `@Transaction` 注解的事务级别为 `Type.TRANSACTION.NONE` 时，将判断当前类是否存在 `@Transaction` 注解声明并尝试获取其事务级别设置。
- 在同一个线程内的事务将被合并为一个事务，称之为嵌套事务，支持事务的无限层级嵌套，如果每一层嵌套，指定的事务级别有所不同，不同的数据库，可能引发不可预知的错误。 所以嵌套的事务将以最顶层的事务级别为标准，也就是说，如果最顶层的事务级别为 `TRANSACTION_READ_COMMITTED`， 那么下面所包含的所有事务，无论你指定什么样的事务级别都将视为无效。

:::



### @Transaction

声明一个类方法开启数据库事务。

| 配置项 | 描述                                                         |
| ------ | ------------------------------------------------------------ |
| value  | 事务类型（参考 JDBC 事务类型），默认为 `Type.TRANSACTION.READ_COMMITTED` |



**示例：**

```java
public interface IUserService {

    UserEntity findUser(String username, String pwd) throws Exception;

    boolean login(String username, String pwd) throws Exception;
}

@Bean
public class UserServiceImpl implements IUserService {

    @Override
    public UserEntity findUser(String username, String pwd) throws Exception {
        return JDBC.get().openSession(new IDatabaseSessionExecutor<UserEntity>() {
            public UserEntity execute(IDatabaseSession session) throws Exception {
                Cond cond = Cond.create()
                        .eq(UserEntity.FIELDS.USERNAME).param(username)
                        .and().eq(UserEntity.FIELDS.PASSWORD).param(pwd);
                return session.findFirst(EntitySQL.create(UserEntity.class), Where.create(cond));
            }
        });
    }

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
}

@EnableAutoScan
@EnableBeanProxy
public class Starter {

    static {
        System.setProperty(IApplication.SYSTEM_MAIN_CLASS, Starter.class.getName());
    }

    private static final Log LOG = LogFactory.getLog(Starter.class);

    public static void main(String[] args) throws Exception {
        try (IApplication application = YMP.run(args)) {
            IUserService userService = application.getBeanFactory().getBean(IUserService.class);
            if (userService.login("suninformation", DigestUtils.md5Hex("123456"))) {
                LOG.info("Login succeeded.");
            }
        }
    }
}
```



### 事务管理器（ITransaction）

用于管理和执行基于 JDBC 事务相关操作（如：事务的开启、关闭、提交和回滚等）的接口类，该接口采用 `SPI` 方式加载以方便业务扩展（如：分步式事务实现等），一般情况下，JDBC 模块所提供的默认实现，基本可以满足常规的业务场景。



### 手动开启事务

手动开启事务操作需要借助 `Transactions` 类完成，此类提供了两种事务执行方式，分别针对无返回值和有返回值的情况。

**示例：**无返回值事务，支持批量操作。

```java
Transactions.execute(new ITrade() {
    @Override
    public void deal() throws Throwable {
        // 具体业务逻辑
    }
});

// 支持批量业务逻辑处理
Transactions.execute(new ITrade() {
    @Override
    public void deal() throws Throwable {
        // 具体业务逻辑1
    }
}, new ITrade() {
    @Override
    public void deal() throws Throwable {
        // 具体业务逻辑2
    }
});

// 可以指定事务级别，默认为Type.TRANSACTION.READ_COMMITTED
Transactions.execute(Type.TRANSACTION.REPEATABLE_READ, new ITrade() {
    @Override
    public void deal() throws Throwable {
        // 具体业务逻辑
    }
});
```



**示例：**有返回值事务，不支持批量操作。

```java
UserEntity userEntity = Transactions.execute(new AbstractTrade<UserEntity>() {
    @Override
    public UserEntity dealing() throws Throwable {
        // 具体业务逻辑
        return null;
    }
});

// 可以指定事务级别，默认为Type.TRANSACTION.READ_COMMITTED
UserEntity userEntity = Transactions.execute(Type.TRANSACTION.REPEATABLE_READ, new AbstractTrade<UserEntity>() {
    @Override
    public UserEntity dealing() throws Throwable {
        // 具体业务逻辑
        return null;
    }
});
```



### 事务回滚（Rollback）

不论是手动开启事务还是通过 `@Transaction` 注解自动开启事务，只要在具体业务逻辑处理过程中抛出任何异常都将终止事务并回滚。



## 会话（Session）

会话是对应用中具体业务操作触发的一系列与数据库之间的交互过程的封装，通过建立一个临时通道，负责与数据库之间连接资源的创建及回收，同时提供更为高级的抽象指令接口调用，基于会话的优点：

- 开发人员不需要担心连接资源是否正确释放。
- 严格的编码规范更利于维护和理解。
- 更好的业务封装性。



### 如何开启会话

**示例：** 使用默认数据源开启会话

```java
UserEntity userEntity = JDBC.get().openSession(new IDatabaseSessionExecutor<UserEntity>() {
    public UserEntity execute(IDatabaseSession session) throws Exception {
        // TODO 此处填写业务逻辑代码...
        return session.findFirst(EntitySQL.create(UserEntity.class));
    }
});
```



**示例：** 使用指定的数据源开启会话

```java
IResultSet<UserEntity> users = JDBC.get().openSession("oracledb", new IDatabaseSessionExecutor<IResultSet<UserEntity>>() {
    public IResultSet<UserEntity> execute(IDatabaseSession session) throws Exception {
        // TODO 此处填写业务逻辑代码...
        return session.find(EntitySQL.create(UserEntity.class));
    }
});
```



### 基于会话的数据库操作

以下示例代码仍然是围绕前面用到的用户数据实体（UserEntity）类来展示如何使用会话对象完成数据表的CRUD操作。

实际上，在 YMP 框架的 `v2.1.x` 版本中已重构了数据实体方法，针对单表的绝大部份操作完全可以通过自动生成的数据实体类完成。尽管如此，了解会话的运行机制和使用方法是非常有必要的，因为它是 JDBC 模块的基础、是框架与数据库之间的桥梁，一些更为复杂的操作仍需通过它来完成。



#### 插入（Insert）

```java
UserEntity userEntity = JDBC.get().openSession(new IDatabaseSessionExecutor<UserEntity>() {
    public UserEntity execute(IDatabaseSession session) throws Exception {
        UserEntity user = UserEntity.builder()
            .id(UUIDUtils.UUID())
            .username("suninformation")
            .nickname("有理想的鱼")
            .password(DigestUtils.md5Hex("123456"))
            .email("suninformation@163.com")
            .build();
        // 执行数据插入
        user = session.insert(user);
        // 或者在插入时也可以指定/排除某些字段
        user = session.insert(user, Fields.create(UserEntity.FIELDS.NICKNAME, 
                                                  UserEntity.FIELDS.EMAIL).excluded(true));
        return user;
    }
});
```



#### 更新（Update）

```java
UserEntity userEntity = JDBC.get().openSession(new IDatabaseSessionExecutor<UserEntity>() {
    public UserEntity execute(IDatabaseSession session) throws Exception {
        UserEntity user = UserEntity.builder()
            .id("bc19f5645aa9438089c5e9954e5f1ac5")
            .password(DigestUtils.md5Hex("654321"))
            .gender("F")
            .build();
        // 执行记录更新
        user = session.update(user);
        // 或者仅更新指定的字段/排除某些字段
        user = session.update(user, Fields.create(UserEntity.FIELDS.PASSWORD));
        return user;
    }
});
```



#### 查询（Find）

##### 方式一：根据记录ID加载

```java
UserEntity userEntity = JDBC.get().openSession(new IDatabaseSessionExecutor<UserEntity>() {
    public UserEntity execute(IDatabaseSession session) throws Exception {
        EntitySQL entitySQL = EntitySQL.create(UserEntity.class);
        // 或者加载指定的字段
        entitySQL.field(UserEntity.FIELDS.USERNAME).field(UserEntity.FIELDS.NICKNAME);
        return session.find(entitySQL, "bc19f5645aa9438089c5e9954e5f1ac5");
    }
});
```



##### 方式二：通过数据实体设置条件

```java
IResultSet<UserEntity> users = JDBC.get().openSession(new IDatabaseSessionExecutor<IResultSet<UserEntity>>() {
    public IResultSet<UserEntity> execute(IDatabaseSession session) throws Exception {
        // 非空属性之间将使用and条件连接，查询所有符合条件的记录并返回所有字段
        UserEntity user = new UserEntity();
        user.setUsername("suninformation");
        user.setPassword(DigestUtils.md5Hex("654321"));
        // 返回指定的字段
        return session.find(user, Fields.create(UserEntity.FIELDS.ID, UserEntity.FIELDS.EMAIL));
    }
});
```



##### 方式三：自定义属性条件并分页查询

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



##### 方式四：返回符合条件的第一条记录

```java
UserEntity user = JDBC.get().openSession(new IDatabaseSessionExecutor<UserEntity>() {
    public UserEntity execute(IDatabaseSession session) throws Exception {
        // 返回与用户名称包含"info"的的第一条记录
        Cond cond = Cond.create().like(UserEntity.FIELDS.USERNAME).param(Like.create("info").contains()));
        return session.findFirst(EntitySQL.create(UserEntity.class)
                                 .field(Fields.create(UserEntity.FIELDS.ID, UserEntity.FIELDS.NICKNAME)),
                                 Where.create(cond).orderByDesc(UserEntity.FIELDS.CREATE_TIME));
    }
});
```



##### 方式五：统计符合条件的记录数

```java
Long count = JDBC.get().openSession(new IDatabaseSessionExecutor<Long>() {
    public Long execute(IDatabaseSession session) throws Exception {
        // 返回与用户名称和密码匹配的记录数
        return session.count(UserEntity.class, Where.create(Cond.create()
                .eq(UserEntity.FIELDS.USERNAME).param("suninformation")
                .and().eq(UserEntity.FIELDS.PASSWORD).param(DigestUtils.md5Hex("654321"))));
    }
});
```



##### 方式六：执行自定义SQL查询

```java
IResultSet<Object[]> resultSet = JDBC.get().openSession(new IDatabaseSessionExecutor<IResultSet<Object[]>>() {
    public IResultSet<Object[]> execute(IDatabaseSession session) throws Exception {
        // 查询邮件后缀为`@163.com`的全部记录
        return session.find(SQL.create("SELECT * FROM user WHERE email LIKE ?")
                            .param(Like.create("@163.com").endsWith()), IResultSetHandler.ARRAY.create());
    }
});
```



#### 删除（Delete）

##### 方式一：根据记录ID删除

```java
Integer effectCount = JDBC.get().openSession(new IDatabaseSessionExecutor<Integer>() {
    public Integer execute(IDatabaseSession session) throws Exception {
        // 根据实体主键删除记录，返回影响记录数
        return session.delete(UserEntity.class, "bc19f5645aa9438089c5e9954e5f1ac5");
    }
});
```



##### 方式二：根据条件删除记录

```java
UserEntity user = JDBC.get().openSession(new IDatabaseSessionExecutor<UserEntity>() {
    public UserEntity execute(IDatabaseSession session) throws Exception {
        // 非空属性之间将使用and条件连接
        UserEntity user = UserEntity.builder()
            .username("suninformation")
            .password(DigestUtils.md5Hex("654321"))
            .build();
        return session.delete(user);
    }
});
```



#### 执行更新类操作（ExecuteForUpdate）

该方法用于执行会话接口中并未提供对应的方法封装且执行操作会对数据库产生变化的 SQL 语句，执行该方法后将返回受影响记录行数。

**示例：** 删除邮件后缀为 `@163.com` 的记录。

```java
Integer effectCount = JDBC.get().openSession(new IDatabaseSessionExecutor<Integer>() {
    public Integer execute(IDatabaseSession session) throws Exception {
        return session.executeForUpdate(Delete.create(UserEntity.class)
                                      .where(Cond.create().like(UserEntity.FIELDS.EMAIL)
                                             .param(Like.create("@163.com").endsWith())).toSQL());
    }
});
```

**注**：以上操作均支持批量操作，具体使用请阅读 API 接口文档和相关源码。



## 结果集（ResultSet）

JDBC 持久化模块将数据查询的结果集合统一使用 `IResultSet` 接口进行封装并集成分页参数，下面通过一段代码来了解它：

```java
IResultSet<UserEntity> results = JDBC.get().openSession(new IDatabaseSessionExecutor<IResultSet<UserEntity>>() {
    public IResultSet<UserEntity> execute(IDatabaseSession session) throws Exception {
        return session.find(EntitySQL.create(UserEntity.class), Page.create());
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




## 对象查询（Query）

本节主要介绍 JDBC 持久化模块从 `v2.x` 版本开始新增的特性，主要用于辅助开发人员像写 Java 代码一样编写 SQL 语句，在一定程度上替代传统字符串拼接的模式，与数据实体的字段常量一起配合使用，这样做的好处是降低字符串拼接过程中出错的机率，让一些问题能够在编译期间被及时发现。该特性在 `v2.1.x` 版本中进行了重构和完善，使用起来也更简单、便捷。

### Fields：字段名称集合

用于辅助拼接数据表字段名称等，支持自定义前缀和别名。

**示例代码：**

```java
// 创建Fields对象
Fields fields = Fields.create(UserEntity.FIELDS.USERNAME, "password");
// 添加带前缀和别名
fields.add("u", UserEntity.FIELDS.EMAIL, "e");
// 添加带前缀
fields = Fields.create().add("u", UserEntity.FIELDS.ID).add(fields);
// 标记集合中的字段为排除的
fields.excluded(true);
// 判断是否存在排除标记
fields.isExcluded();
// 输出
System.out.println(fields.fields());
```

**执行结果：**

```shell
[u.id, username, password, u.email e]
```



### Params：参数集合

主要存储用于替换 SQL 语句中 `?` 号占位符对应的参数值对象。

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



### Pages：分页参数

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



### Cond：条件参数

用于生成 SQL 条件语句并存储条件参数。



#### 示例一：构造方式

```java
// 使用全局JDBC模块的默认数据源配置构建;
Cond.create();
// 使用指定的JDBC模块实例和数据源配置构建;
Cond.create(JDBC.get(), "default");
// 通过任何继承Query（如：Cond、Where、Select、Insert、Delete等本章节所提及的大部份对象都是其子类）类的实例对象构建;
// 通过已存在的Query对象构建的主要目的是避免重复获取其所依赖的相同容器、数据源等配置;
Cond cond = Cond.create(JDBC.get(), "oracledb");
cond.bracket(Cond.create(cond).eq("age").param(18));
```

:::tip **特别说明：**

以 Cond 条件参数为例，对象查询所涉及的大部份对象（如：OrderBy、GroupBy、Where、Select、Insert、Update、Delete、Join、SQL、BatchSQL 和 EntitySQL 等）均与之构造方式不尽相同，后面内容将不再赘述。

:::



#### 示例二：参数的传递

在 Cond 对象中除两个字段间比较之外的条件都将构建一个基于 `?` 占位的SQL表达式，通过 `param` 方法传入与其对应的参数值，条件对象将按参数传入顺序存储：

```java
Cond cond = Cond.create();
cond.bracket(cond.like("username").param(Like.create("ymp").contains()).and().gtEq("age").param(20))
        .or().bracket(cond.eq("gender").param("F").and().lt("age").param(18));
System.out.println("SQL: " + cond.toString());
System.out.println("参数: " + cond.params().params());
```

执行结果：

```shell
SQL: ( username LIKE ? AND age >= ? )  OR  ( gender = ? AND age < ? ) 
参数: [%ymp%, 20, F, 18]
```



#### 示例三：比较运算符的使用

| 运算符 | 代码                          | 输出SQL语句 |
| ------ | ----------------------------- | ----------- |
| `=`    | `cond.eq("age").param(18)`    | age = 18    |
| `!=`   | `cond.notEq("age").param(18)` | age != 18   |
| `>`    | `cond.gt("age").param(18)`    | age > 18    |
| `<`    | `cond.lt("age").param(18)`    | age < 18    |
| `>=`   | `cond.gtEq("age").param(18)`  | age >= 18   |
| `<=`   | `cond.ltEq("age").param(18)`  | age <= 18   |

:::tip **特别说明：** 

以上操作均支持两字段之间比较，如：

```java
// 输出SQL：username = nickname
cond.eqField("username", "nickname");
// 输出SQL：username != nickname
cond.notEqField("username", "nickname")
```

:::



#### 示例四：逻辑运算符的使用

| 运算符 | 代码                                                         | 输出SQL语句 |
| ------ | ------------------------------------------------------------ | ----------- |
| `AND`  | `cond.and()`<br />`cond.andIfNeed()`<br />`cond.andIfNeed(cond...)`<br />`cond.and(cond...)` | AND ...     |
| `OR`   | `cond.or()`<br />`cond.orIfNeed()`<br />`cond.orIfNeed(cond...)`<br />`cond.or(cond...)` | OR ...      |
| `NOT`  | `cond.not()`<br />`cond.not(cond...)`                        | NOT ...     |



#### 示例四：其它运算符的使用

| 运算符    | 代码                                                         | 输出SQL语句                                                  |
| --------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| `IN`      | `cond.in("uid", Params.create(...))`                         | uid IN (...)                                                 |
| `EXISTS`  | `cond.exists(...)`<br />`cond.not().exists(...)`             | EXISTS (...)<br />NOT EXISTS (...)                           |
| `RANGE`   | `cond.range("age", 18, 20, LogicalOpt.AND)`<br />`cond.range("age", 18, null, LogicalOpt.OR)`<br />`cond.range("age", null, 20, null)` | AND age BETWEEN (18 AND 20)<br />OR age >= 18<br />age <= 20 |
| `BETWEEN` | `cond.between("age", 18, 20)`                                | age BETWEEN 18 AND 20                                        |
| `()`      | `cond.bracket(cond...)`<br />`cond.bracketBegin()...bracketEnd()` | (...)                                                        |
| `1=1`     | `cond.eqOne()`                                               | 1=1                                                          |
| `LIKE`    | `cond.like("username").param(Like.create("ymp").contains())` | username LIKE '%ymp%'                                        |
| `OPT`     | `cond.opt("username", OPT.EQ)`<br />`cond.opt("username", OPT.EQ, "nickname")` | username = ?<br />username = nickname                        |

:::tip **特别说明：** 

Cond 类提供的诸多方法（如：`eq`）中，方法名称以 `Wrap` 为后缀（如：`eqWrap`）的作用是为字段名称添加与当前数据库匹配的引用标识符。

:::



#### 示例五：表达式条件判断

```java
public Cond exprBuild(Cond cond, String username) {
    // 方式一：
    return cond.expr(StringUtils.isNotBlank(username), new IConditionAppender() {
        @Override
        public void append(Cond cond) {
            cond.andIfNeed().eq("username").param(username);
        }
    });
    // 方式二：
    return cond.expr(StringUtils.isNotBlank(username), new IConditionBuilder() {
        @Override
        public Cond build() {
            return Cond.create(cond.andIfNeed()).eq("username").param(username);
        }
    });
}
```



#### 示例六：对象非空条件判断

```java
public Cond notEmptyBuild(Cond cond, String username) {
    // 方式一：
    return cond.exprNotEmpty(username, new IConditionAppender() {
        @Override
        public void append(Cond cond) {
            cond.andIfNeed().eq("username").param(username);
        }
    });
    // 方式二：
    return cond.exprNotEmpty(username, new IConditionBuilder() {
        @Override
        public Cond build() {
            return Cond.create(cond.andIfNeed()).eq("username").param(username);
        }
    });
}
```



### FieldCondition：字段条件参数

用于为指定字段构建 Cond 条件参数对象。

假设我们要在用户表中查询使用了QQ邮箱的用户且年龄在18岁以下的女性用户，通常情况下的写法如下：

```java
Cond.create().like(UserEntity.FIELDS.EMAIL).param(Like.create("@qq.com").endsWith())
    .and().eq(UserEntity.FIELDS.GENDER).param("F")
    .and().ltEq(UserEntity.FIELDS.AGE).param(18);
```

将上述代码通过字段条件参数进行改写：

```java
Cond cond = Cond.create();
cond.cond(FieldCondition.create(cond, UserEntity.FIELDS.EMAIL).like(Like.create("@qq.com").endsWith()))
    .and(FieldCondition.create(cond, UserEntity.FIELDS.GENDER).eqValue("F"))
    .cond(FieldCondition.create(cond, UserEntity.FIELDS.AGE).ltEqValue(18));
```

改写后的代码看上去并不理想，因为每个字段都需要通过 `FieldCondition.create` 方法创建一次，这反而变得更麻烦，别担心！我们可以通过自动生成的实体类提供的字段条件构建器（FieldConditionBuilder）再次改写：

```java
UserEntity.FieldConditionBuilder fieldCondBuilder = UserEntity.conditionBuilder();
Cond cond = fieldCondBuilder.email().like(Like.create("@qq.com").endsWith()).build()
    .and(fieldCondBuilder.gender().eqValue("F"))
    .and(fieldCondBuilder.age().ltEqValue(18));
```

它们最终生成并执行的 SQL 语句和参数是一样的，如下所示：

```sql
email LIKE '%@qq.com' AND gender = 'F' and age <= 18
```

上述示例展示了同一种查询条件语句的三种不同构建方法，请开发人员根据实际情况选择适合的构建方式。



### Like：模糊参数

用于模糊查询所需参数值的通配符填充，并支持将其中的特殊字符（如：`%`、`_`、`/` 等）进行转义，这样做的主要目的是为了避免传统的字符串拼接过程容易产生的错误。

| 代码                              | 描述               | 输出SQL语句 |
| --------------------------------- | ------------------ | ----------- |
| `Like.create("ymp").contains()`   | 包含某字符串       | %ymp%       |
| `Like.create("ymp").startsWith()` | 以某字符串做为前缀 | ymp%        |
| `Like.create("ymp").endsWith()`   | 以某字符串做为后缀 | %ymp        |



### OrderBy：排序对象

用于生成 SQL 语句中的 ORDER BY 子句。

**示例代码：**

```java
OrderBy orderBy = OrderBy.create().asc("age").desc("u", "create_time");
//
System.out.println(orderBy.toString());
```

**执行结果：**

```shell
ORDER BY age, u.create_time DESC
```



### GroupBy：分组对象

用于生成 SQL 语句中的 GROUP BY 子句。

**示例代码：**

```java
GroupBy groupBy = GroupBy.create(Fields.create().add("u", "gender").add("age"))
    .having(Cond.create().lt("age").param(18));
//
System.out.println("SQL: " + groupBy.toString());
System.out.println("参数: " + groupBy.having().params().params());
```

**执行结果：**

```shell
SQL: GROUP BY u.gender, age HAVING age < ?
参数: [18]
```



### Where：条件对象

用于生成 SQL 语句中的 WHERE 子句，同时集成了 OrderBy 和 GroupBy 参数对象。

**示例代码：**

```java
// 方式一：通过Cond条件参数直接构建Where对象
Where where = Cond.create().like("username").param("%ymp%").and().gtEq("age").param(20).buildWhere()
    .groupBy("u", "gender")
    .groupBy("age")
    .orderByAsc("age")
    .orderByDesc("u", "create_time");

// 方式二：分解各个部份
Cond cond = Cond.create()
        .like("username").param("%ymp%")
        .and().gtEq("age").param(20);
OrderBy orderBy = OrderBy.create().asc("age").desc("u", "creaate_time");
GroupBy groupBy = GroupBy.create(Fields.create().add("u", "gender").add("age"));
//
Where where = Where.create(cond).orderBy(orderBy).groupBy(groupBy);
//
System.out.println("SQL: " + where.toString());
System.out.println("参数: " + where.params().params());
```

**执行结果：**（为方便阅读，此处美化了 SQL 的输出格式）

```shell
SQL: WHERE
		 username LIKE ?
	 AND age >= ?
	 GROUP BY
		 u.gender,
		 age
	 ORDER BY
		 age,
		 u.create_time DESC
参数: [%ymp%, 20]
```



### Select：查询语句对象

用于构建 SELECT 数据库查询语句。



#### 示例一：通过用户数据实体查询

采用分页查询用户表中年龄大于等于20岁的数据，返回每条记录的主键、昵称和年龄字段并按年龄倒序排列。

```java
Select select = Select.create(UserEntity.class, "u")
    .field("u", UserEntity.FIELDS.ID)
    .field("u", UserEntity.FIELDS.NICKNAME)
    .field("u", UserEntity.FIELDS.AGE)
    .orderByDesc("u", UserEntity.FIELDS.AGE)
    .page(Page.create())
    .where(Cond.create().gtEq("u", UserEntity.FIELDS.AGE).param(20))
    .distinct();
System.out.println("SQL: " + select.toString());
System.out.println("参数: " + select.params().params());
```

执行结果：（为方便阅读，此处美化了 SQL 的输出格式）

```shell
SQL: SELECT DISTINCT u.id, u.nickname, u.age
     FROM user u
     WHERE u.age >= ?
     ORDER BY u.age DESC
     LIMIT 0, 20
参数: [%ymp%, 20]
```



#### 示例二：子查询

```java
Select subSelect = Select.create().from("user", "u").where(Cond.create().gtEq("u", "age").param(20));
Select select = Select.create(subSelect.alias("u"))
    .field(Func.Aggregate.MAX(Fields.field("u", "age")))
    .groupBy("u", "age");
System.out.println("SQL: " + select.toString());
System.out.println("参数: " + select.params().params());
```

执行结果：

```shell
SQL: SELECT MAX(u.age) FROM (SELECT * FROM user u WHERE u.age >= ?) u GROUP BY u.age LIMIT 0, 20
参数: [20]
```



#### 示例三：执行查询

本例代码用另一种方式生成与 **示例一** 相同的 SQL 并执行。

```java
IResultSet<UserEntity> users = Select.create("user", "u")
    .field("u", Fields.create("id", "nickname", "age"))
    .orderByDesc("u", "age")
    .page(1)
    .where(Cond.create().gtEq("u", "age").param(20))
    .distinct()
    .find(new EntityResultSetHandler<>());
```



### Insert：插入语句对象

用于构建 INSERT 数据插入语句。



#### 示例一：单记录插入

```java
// 方式一：
Insert insert = Insert.create(UserEntity.class)
    .field(UserEntity.FIELDS.ID).param(UUIDUtils.UUID())
    .field(UserEntity.FIELDS.USERNAME).param("suninformation")
    .field(UserEntity.FIELDS.NICKNAME).param("有理想的鱼")
    .field(UserEntity.FIELDS.PASSWORD).param("123456")
    .field(UserEntity.FIELDS.EMAIL).param("suninformation@163.com");
// 方式二：
Insert insert = Insert.create(UserEntity.class)
    .field(Fields.create(UserEntity.FIELDS.ID, 
                         UserEntity.FIELDS.USERNAME, 
                         UserEntity.FIELDS.NICKNAME, 
                         UserEntity.FIELDS.PASSWORD, 
                         UserEntity.FIELDS.EMAIL))
    .param(Params.create(UUIDUtils.UUID(), 
                         "suninformation", 
                         "有理想的鱼", 
                         "123456", 
                         "suninformation@163.com"))
SQL sql = insert.toSQL();
System.out.println("SQL: " + sql.toString());
System.out.println("参数: " + sql.params().params());
```

执行结果：

```shell
SQL: INSERT INTO user (id, username, nickname, password, email) VALUES (?,?,?,?,?)
参数: [28b89f37bd7d467e93f518080240ede4, suninformation, 有理想的鱼, 123456, suninformation@163.com]
```



#### 示例二：批量插入

```java
Insert insert = Insert.create(UserEntity.class)
    .field(UserEntity.FIELDS.ID)
    .field(UserEntity.FIELDS.USERNAME)
    .field(UserEntity.FIELDS.NICKNAME)
    .field(UserEntity.FIELDS.PASSWORD)
    .field(UserEntity.FIELDS.EMAIL)
    .addGroupParam(Params.create("1", "用户A", "昵称A", "密码A", "邮件A"))
    .addGroupParam(Params.create("2", "用户B", "昵称B", "密码B", "邮件B"));
SQL sql = insert.toSQL();
System.out.println("SQL: " + sql.toString());
System.out.println("参数: " + sql.params().params());
```

执行结果：

```shell
SQL: INSERT INTO user (id, username, nickname, password, email) VALUES (?,?,?,?,?), (?,?,?,?,?)
参数: [1, 用户A, 昵称A, 密码A, 邮件A, 2, 用户B, 昵称B, 密码B, 邮件B]
```



#### 示例三：通过SELECT结果集插入

```java
Insert insert = Insert.create("new_table")
    .select(Select.create().from("user").where(Cond.create().gtEq("age").param(20)));
SQL sql = insert.toSQL();
System.out.println("SQL: " + sql.toString());
System.out.println("参数: " + sql.params().params());
```

执行结果：

```shell
SQL: INSERT INTO new_table SELECT * FROM user WHERE age >= ?
参数: [20]
```



#### 示例四：执行插入

```java
int effectCount = Insert.create(UserEntity.class)
    .field(UserEntity.FIELDS.ID).param(UUIDUtils.UUID())
    .field(UserEntity.FIELDS.USERNAME).param("suninformation")
    .field(UserEntity.FIELDS.NICKNAME).param("有理想的鱼")
    .field(UserEntity.FIELDS.PASSWORD).param("123456")
    .field(UserEntity.FIELDS.EMAIL).param("suninformation@163.com")
    .execute();
System.out.println("本次操作影响记录数：" + effectCount);
```



### Update：更新语句对象

用于构建 UPDATE 数据更新语句。



#### 示例一：常规更新

```java
Update update = Update.create().table(UserEntity.class).field(UserEntity.FIELDS.AGE).param(18)
    .where(Cond.create().lt(UserEntity.FIELDS.AGE).param(18));
SQL sql = update.toSQL();
System.out.println("SQL: " + sql.toString());
System.out.println("参数: " + sql.params().params());
```

执行结果：

```shell
SQL: UPDATE user SET age = ? WHERE  age < ?
参数: [18, 18]
```



#### 示例二：跨表更新

```java
Update update = Update.create().table(UserEntity.class, "u").field("u", UserEntity.FIELDS.AGE).param(18)
    .table("user_ext", "ue").field("ue", "country").param("CN")
    .leftJoin("user_ext", "ue", Cond.create().eqField("ue.id", Fields.field("u", UserEntity.FIELDS.ID)))
    .where(Cond.create().lt("u", UserEntity.FIELDS.AGE).param(18));
SQL sql = update.toSQL();
System.out.println("SQL: " + sql.toString());
System.out.println("参数: " + sql.params().params());
```

执行结果：（为方便阅读，此处美化了 SQL 的输出格式）

```shell
SQL: UPDATE user u, user_ext ue
     LEFT JOIN user_ext ue ON ue.id = u.id
     SET u.age = ?, ue.country = ? WHERE  age < ?
参数: [18, CN, 18]
```



#### 示例三：执行更新

```java
int effectCount = Update.create().table(UserEntity.class).field(UserEntity.FIELDS.AGE).param(18)
    .where(Cond.create().lt("u", UserEntity.FIELDS.AGE).param(18)).execute();
System.out.println("本次操作影响记录数：" + effectCount);
```



### Delete：删除语句对象

用于构建 DELETE 数据删除语句。



#### 示例一：单表删除

```java
Delete delete = Delete.create()
    .from(UserEntity.class)
    .where(Cond.create().lt(UserEntity.FIELDS.AGE).param(18));
SQL sql = delete.toSQL();
System.out.println("SQL: " + sql.toString());
System.out.println("参数: " + sql.params().params());
```

执行结果：

```shell
SQL: DELETE FROM user WHERE age < ?
参数: [18]
```



#### 示例二：多表数据删除

```java
Delete delete = Delete.create()
    .table("u")
    .table("ue")
    .from(UserEntity.class, "u")
    .leftJoin("user_ext", "ue", Cond.create()
              .eqField("ue.id", Fields.field("u", UserEntity.FIELDS.ID)))
    .where(Cond.create().lt("u", UserEntity.FIELDS.AGE).param(18));
SQL sql = delete.toSQL();
System.out.println("SQL: " + sql.toString());
System.out.println("参数: " + sql.params().params());
```

执行结果：

```shell
SQL: DELETE u, ue FROM user u LEFT JOIN user_ext ue ON ue.id = u.id WHERE u.age < ?
参数: [18]
```



#### 示例三：执行删除

```java
int effectCount = Delete.create()
    .from(UserEntity.class)
    .where(Cond.create().lt(UserEntity.FIELDS.AGE).param(18)).execute();
System.out.println("本次操作影响记录数：" + effectCount);
```



### Join：连接对象

用于生成 SQL 语句中的 JOIN 子句，支持 LEFT、RIGHT 和 INNER 连接方式。

**示例代码：**

```java
Join join = Join.inner("user_ext").alias("ue").on(Cond.create().eqField("ue.uid", "u.id"));
Select select = Select.create("user", "u").join(join)
    .where(Cond.create()
           .gtEq("u", "age").param(18)
           .and().isNotNull("ue", "type"));
SQL sql = select.toSQL();
System.out.println("SQL: " + sql.toString());
System.out.println("参数: " + sql.params().params());
```

**执行结果：**

```shell
SQL: SELECT * FROM user u INNER JOIN user_ext ue ON ue.uid = u.id WHERE u.age >= ? AND ue.type IS NOT NULL
参数: [18]
```



### Union：联合对象

用于配合 SELECT 查询语句对象对 UNION 或 UNION ALL 子句的支持，其目的是将多个 SELECT 语句关联在一起，仅做为参数使用。

**示例代码：**

```java
Select select = Select.create("user").where(Where.create(Cond.create().eq("dept").param("IT")))
    .union(Union.create(Select.create("user").where(Where.create(Cond.create().lt("age").param(18)))).all());
SQL sql = select.toSQL();
System.out.println("SQL: " + sql.toString());
System.out.println("参数: " + sql.params().params());
```

**执行结果：**

```shell
SQL: SELECT * FROM user WHERE dept = ? UNION ALL SELECT * FROM user WHERE age < ?
参数: [IT, 18]
```



### SQL：自定义SQL语句

它是对象查询的基础组件，为各种 SQL 语句提供执行能力。

**示例代码：**

```java
SQL sql = SQL.create("select * from user where age > ? and username like ?").param(18).param("%ymp%");
// 执行查询：按指定的结果集类型返回
IResultSet<Object[]> resultSet = sql.find(IResultSetHandler.ARRAY.create());
// 计算记录数量：返回符合条件的记录数
long count = sql.count();
// 执行更新类操作：返回被影响记录数量
int effectCount = sql.execute(); 
```



### BatchSQL：批量SQL语句对象

与 SQL 对象一样属于对象查询的基础组件，主要用于批量更新类 SQL 语句的执行和参数对象的封装。

**示例代码：**

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



### Func：函数

在编写 SQL 语句时，经常会用到由数据库提供（或根据业务自定义）的一系列函数来处理数据查询等操作，为了能够像编写 Java 代码一样在对象查询的 SQL 语句中使用函数，JDBC模块提供了一种简单的函数封装方法，同时也封装了一些比较常用的函数（目前这些函数封装主要针对 MySQL 数据库实现，其它类型数据库的支持也将在未来版本中逐步完善），其中主要包括：常规运算、数学计算、字符操作、聚合分组、日期时间和流程控制等相关函数。

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
| LOG      | `Func.math.LOG("x")`<br />`Func.math.LOG("b", x")`          | X 的自然对数<br />X 对于任意基数 B 的对数                    |
| LOG10    | `Func.math.LOG10("x")`                                      |                                                              |
| LOG2     | `Func.math.LOG2("x")`                                       |                                                              |
| MOD      | `Func.math.MOD("n", "m")`                                   | N 被 M 除后的余数                                            |
| PI       | `Func.math.PI()`                                            | PI 的值（默认的显示小数位数是7位）                           |
| POW      | `Func.math.POW("y", x")`                                    | X 的 Y 乘方的结果值                                          |
| POWER    | `Func.math.POWER("y", x")`                                  |                                                              |
| RADIANS  | `Func.math.RADIANS("x")`                                    | 由度转化为弧度的参数 X                                       |
| RAND     | `Func.math.RAND()`<br />`Func.math.RAND("n")`               | 随机浮点值 v ，范围在 0 到1 之间 (即, 其范围为 0 ≤ v ≤ 1.0)<br />指定一个整数参数 N ，它被用作种子值，用来产生重复序列 |
| ROUND    | `Func.math.ROUND("x")`<br />`Func.math.ROUND("x", d)`       | X 值接近于最近似的整数<br />X 值保留到小数点后 D 位并四舍五入<br />（若要直接保留 X 值小数点左边的 D 位，可将 D 设为负值） |
| SIGN     | `Func.math.SIGN("n")`                                       | X 值的符号（负数、零或正）对应 -1，0或1                      |
| SIN      | `Func.math.SIN("x")`                                        | X 的正弦                                                     |
| SQRT     | `Func.math.SQRT("x")`                                       | 非负数 X 的二次方根                                          |
| TAN      | `Func.math.TAN("x")`                                        | X 的正切                                                     |
| TRUNCATE | `Func.math.TRUNCATE("x")`<br />`Func.math.TRUNCATE("x", d)` | 舍去至小数点后 D 位的数字 X<br />若 D 的值为 0，则结果不带有小数点或不带有小数部分<br />可以将 D 设为负数，若要截去 X 小数点左起第 D 位开始后面所有低位的值 |



#### 字符操作类函数：Func.Strings

| 函数             | 代码                                                         | 描述                                     |
| ---------------- | ------------------------------------------------------------ | ---------------------------------------- |
| ASCII            | `Func.strings.ASCII(str)`                                    | 字符串 str 的最左字符的数值              |
| BIN              | `Func.strings.BIN(n)`                                        | N 的二进制值的字符串表示                 |
| BIN_LENGTH       | `Func.strings.BIN_LENGTH(str)`                               | 二进制的字符串 str 长度                  |
| CHAR             | `Func.strings.CHAR(...n)`                                    | 将 n 个整数代码所对应的字符组成的字符串  |
| CHAR_LENGTH      | `Func.strings.CHAR_LENGTH(str)`                              | 字符串 str 的长度，长度的单位为字符      |
| CHARACTER_LENGTH | `Func.strings.CHARACTER_LENGTH(str)`                         | 与 CHAR_LENGTH 同                        |
| CONCAT           | `Func.strings.CONCAT(str1, str2...n)`                        | 连接多个 str 参数产生的字符串            |
| CONCAT_WS        | `Func.strings.CONCAT_WS(sp, str1, str2...n)`                 | 与 CONCAT 同，第一个参数用于指定分隔符   |
| ELT              | `Func.strings.ELT(n, str1...n)`                              |                                          |
| FIELD            | `Func.strings.FIELD(str, ...n)`                              |                                          |
| FIND_IN_SET      | `Func.strings.FIND_IN_SET(x, strlist)`                       |                                          |
| FORMAT           | `Func.strings.FORMAT(x, d)`                                  |                                          |
| HEX              | `Func.strings.HEX(str)`                                      |                                          |
| FROM_BASE64      | `Func.strings.FROM_BASE64(str)`                              |                                          |
| TO_BASE64        | `Func.strings.TO_BASE64(str)`                                |                                          |
| INSERT           | `Func.strings.INSERT(str, pos, len, newstr)`                 |                                          |
| INSTR            | `Func.strings.INSTR(str, substr)`                            | 字符串 str 中子字符串的第一个出现位置    |
| LEFT             | `Func.strings.LEFT(str, len)`                                | 字符串 str 开始的 len 最左字符           |
| LENGTH           | `Func.strings.LENGTH(str)`                                   | 字符串 str 的长度，单位为字节            |
| LOAD_FILE        | `Func.strings.LOAD_FILE(str)`                                | 读取文件并将这一文件按照字符串的格式返回 |
| LOCATE           | `Func.strings.LOCATE(substr, str)`<br />`Func.strings.LOCATE(substr, str, pos)` |                                          |
| LOWER            | `Func.strings.LOWER(str)`                                    |                                          |
| LPAD             | `Func.strings.LPAD(str, len, padstr)`                        |                                          |
| LTRIM            | `Func.strings.LTRIM(str)`                                    |                                          |
| OCT              | `Func.strings.OCT(n)`                                        | N 的八进制值的字符串表示                 |
| ORD              | `Func.strings.ORD(str)`                                      |                                          |
| QUOTE            | `Func.strings.QUOTE(str)`                                    |                                          |
| REPEAT           | `Func.strings.REPEAT(str, count)`                            |                                          |
| REPLACE          | `Func.strings.REPLACE(str, fromStr, toStr)`                  |                                          |
| REVERSE          | `Func.strings.REVERSE(str)`                                  |                                          |
| RIGHT            | `Func.strings.RIGHT(str)`                                    |                                          |
| RPAD             | `Func.strings.RPAD(str, len, padstr)`                        |                                          |
| RTRIM            | `Func.strings.RTRIM(str)`                                    |                                          |
| SOUNDEX          | `Func.strings.SOUNDEX(str)`                                  |                                          |
| SPACE            | `Func.strings.SPACE(n)`                                      |                                          |
| STRCMP           | `Func.strings.STRCMP(expr1, expr2)`                          |                                          |
| SUBSTRING        | `Func.strings.SUBSTRING(str, pos)`<br />`Func.strings.SUBSTRING(str, pos, len)` |                                          |
| SUBSTRING_INDEX  | `Func.strings.SUBSTRING_INDEX(str, delim, count)`            |                                          |
| TRIM             | `Func.strings.TRIM(str)`                                     |                                          |
| TRIM_BOTH        | `Func.strings.TRIM_BOTH(remstr, str)`                        |                                          |
| TRIM_LEADIN      | `Func.strings.TRIM_LEADIN(remstr, str)`                      |                                          |
| TRIM_TRAILING    | `Func.strings.TRIM_TRAILING(remstr, str)`                    |                                          |
| UNHEX            | `Func.strings.UNHEX(str)`                                    |                                          |
| UPPER            | `Func.strings.UPPER(str)`                                    |                                          |



#### 聚合分组类函数：Func.Aggregate

| 函数         | 代码                                                         | 描述          |
| ------------ | ------------------------------------------------------------ | ------------- |
| AVG          | `Func.aggregate.AVG(expr)`<br />`Func.aggregate.AVG(distinct, expr)` | expr 的平均值 |
| BIT_AND      | `Func.aggregate.BIT_AND(expr)`                               |               |
| BIT_OR       | `Func.aggregate.BIT_OR(expr)`                                |               |
| BIT_XOR      | `Func.aggregate.BIT_XOR(expr)`                               |               |
| COUNT        | `Func.aggregate.COUNT(expr)`<br />`Func.aggregate.COUNT(distinct, expr)` |               |
| GROUP_CONCAT | `Func.aggregate.GROUP_CONCAT(...expr)`<br />`Func.aggregate.GROUP_CONCAT(distinct, ...expr)` |               |
| WM_CONCAT    | `Func.aggregate.WM_CONCAT(expr)`                             |               |
| MAX          | `Func.aggregate.MAX(expr)`<br />`Func.aggregate.MAX(distinct, expr)` |               |
| MIN          | `Func.aggregate.MIN(expr)`<br />`Func.aggregate.MIN(distinct, expr)` |               |
| SUM          | `Func.aggregate.SUM(expr)`<br />`Func.aggregate.SUM(distinct, expr)` |               |



#### 日期时间类函数：Func.DateTime

| 函数           | 代码                          | 描述 |
| -------------- | ----------------------------- | ---- |
| ADDDATE        | `Func.dateTime.ADDDATE`       |      |
| ADDTIME        | `Func.dateTime.ADDTIME`       |      |
| CONVERT_TZ     | `Func.dateTime.CONVERT_TZ`    |      |
| CURDATE        | `Func.dateTime.CURDATE`       |      |
| CURTIME        | `Func.dateTime.CURTIME`       |      |
| DATE           | `Func.dateTime.DATE`          |      |
| DATE_FORMAT    | `Func.dateTime.DATE_FORMAT`   |      |
| DATEDIFF       | `Func.dateTime.DATEDIFF`      |      |
| DAYNAME        | `Func.dateTime.DAYNAME`       |      |
| DAYOFMONTH     | `Func.dateTime.DAYOFMONTH`    |      |
| DAYOFWEEK      | `Func.dateTime.DAYOFWEEK`     |      |
| DAYOFYEAR      | `Func.dateTime.DAYOFYEAR`     |      |
| FROM_UNIXTIME  | `Func.dateTime.FROM_UNIXTIME` |      |
| UNIX_TIMESTAMP | `Func.dateTime.UNIX_TIMESTAM` |      |
| GET_FORMAT     | `Func.dateTime.GET_FORMAT`    |      |
| HOUR           | `Func.dateTime.HOUR`          |      |
| LAST_DAY       | `Func.dateTime.LAST_DAY`      |      |
| MAKEDATE       | `Func.dateTime.MAKEDATE`      |      |
| MAKETIME       | `Func.dateTime.MAKETIME`      |      |
| MICROSECOND    | `Func.dateTime.MICROSECOND`   |      |
| MINUTE         | `Func.dateTime.MINUTE`        |      |
| MONTH          | `Func.dateTime.MONTH`         |      |
| MONTHNAME      | `Func.dateTime.MONTHNAME`     |      |
| NOW            | `Func.dateTime.NOW`           |      |
| PERIOD_ADD     | `Func.dateTime.PERIOD_ADD`    |      |
| PERIOD_DIFF    | `Func.dateTime.PERIOD_DIFF`   |      |
| QUARTER        | `Func.dateTime.QUARTER`       |      |
| SEC_TO_TIME    | `Func.dateTime.SEC_TO_TIME`   |      |
| SECOND         | `Func.dateTime.SECOND`        |      |
| STR_TO_DATE    | `Func.dateTime.STR_TO_DATE`   |      |
| SYSDATE        | `Func.dateTime.SYSDATE`       |      |
| TIME           | `Func.dateTime.TIME`          |      |
| TIME_FORMAT    | `Func.dateTime.TIME_FORMAT`   |      |
| TIME_TO_SEC    | `Func.dateTime.TIME_TO_SEC`   |      |
| TIMEDIFF       | `Func.dateTime.TIMEDIFF`      |      |
| TIMESTAMP      | `Func.dateTime.TIMESTAMP`     |      |
| TIMESTAMPDIFF  | `Func.dateTime.TIMESTAMPDIFF` |      |
| TO_DAYS        | `Func.dateTime.TO_DAYS`       |      |
| UTC_DATE       | `Func.dateTime.UTC_DATE`      |      |
| UTC_TIME       | `Func.dateTime.UTC_TIME`      |      |
| UTC_TIMESTAMP  | `Func.dateTime.UTC_TIMESTAMP` |      |
| WEEK           | `Func.dateTime.WEEK`          |      |
| WEEKDAY        | `Func.dateTime.WEEKDAY`       |      |
| WEEKOFYEAR     | `Func.dateTime.WEEKOFYEAR`    |      |
| YEAR           | `Func.dateTime.YEAR`          |      |
| YEARWEEK       | `Func.dateTime.YEARWEEK`      |      |



#### 控制流函数：Func.ControlFlow

| 函数   | 代码                                            | 描述 |
| ------ | ----------------------------------------------- | ---- |
| CASE   | `Func.controlFlow.CASE(expr, whenFn[], elseFn)` |      |
| WHEN   | `Func.controlFlow.WHEN(expr)`                   |      |
| ELSE   | `Func.controlFlow.ELSE(expr)`                   |      |
| IF     | `Func.controlFlow.IF(expr1, expr2, expr3)`      |      |
| IFNULL | `Func.controlFlow.IFNULL(expr1, expr2)`         |      |
| NULLIF | `Func.controlFlow.NULLIF(expr1, expr2)`         |      |



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



示例二：创建表达式风格的函数封装，如：`CASE value WHEN exp1 THEN result1 ELSE result2 END` 

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
          .join(leftJoin(UserExtEntity.TABLE_NAME).alias("ue")
                .on(cond().eqField(field("u", UserEntity.FIELDS.ID), field("ue", UserExtEntity.FIELDS.UID))))
          .field("u", fields(UserEntity.FIELDS.ID, UserEntity.FIELDS.USERNAME))
          .field(field("ue", UserExtEntity.FIELDS.MONEY));
    // 返回最终结果
    build(select.find(IResultSetHandler.ARRAY.create()));
}}.build();
```



## 存储器（Repository）

为了能够更方便的维护和执行 SQL 语句，JDBC模块提供了存储器的支持，可以通过 `@Repository` 注解自定义 SQL 语句或从配置文件中加载 SQL 语句并自动执行。

### @Repository

| 配置项      | 描述                                                         |
| ----------- | ------------------------------------------------------------ |
| dsName      | 数据源名称，默认为空                                         |
| item        | 从资源文件中加载 `item` 指定的配置项，默认为空               |
| configFile  | 资源文件路径名称，默认为空                                   |
| value       | 自定义 SQL 配置，默认为空                                    |
| update      | 是否为更新操作，默认为 `false`                               |
| page        | 是否分页查询，默认为 `false`                                 |
| useFilter   | 是否调用方法过滤，默认为 `false`                             |
| dbType      | 指定当前存储器适用的数据库类型，默认为全部，否则将根据数据库类型进行存储器加载 |
| resultClass | 指定结果集类型，若设置则使用 `BeanResultSetHandler` ，默认使用 `ArrayResultSetHandler` 处理结果集 |

:::tip **说明：**

- 存储器类通过声明 `@Repository` 注解被框架自动扫描并加载；
- 与其它被容器管理的 `@Bean` 一样支持拦截器、事务、缓存等注解；
- 当 `useFilter=true` 时，存储器类方法的参数至少有一个参数（方法有多个参数时，采用最后一个参数）用于接收SQL执行结果；
- 当 `useFilter=false` 时，存储器方法体将不会被执行；
- 当 `page=true` 时，若 `useFilter=false` 则存储器类方法的最后一个参数必须是 `Page` 类型，否则 `Page` 参数必须位于结果集参数之前；
- 查询类型 SQL 的执行结果数据类型默认为 `IResultSet<Object[]>`，而更新类型 SQL（即`update=true`时）的执行结果必须为 `int` 类型；
- 用于接收 SQL 执行结果的方法参数支持变长类型，如：`IResultSet<Object[]> results`和`IResultSet<Object[]>... results`的效果是一样的；
- 读取配置文件中的 SQL 配置时，配置项名称必须全部采用小写字符，如：`demo_query`；
- 框架将优先加载以当前数据源连接的数据库类型名称作为后缀的配置项，如：`demo_query_mysql`、`demo_query_oracle`，若找不到则加载默认名称，即：`demo_query`；

:::



### 示例一：执行自定义SQL语句

```java
@Repository
public class DemoRepository implements IRepository {

    @Repository(value = "select * from user where type = ${type}", page = true)
    public IResultSet<Object[]> execQuery(Integer type, Page page, IResultSet<Object[]> results) throws Exception {
        // 此处代码将不会执行，因为注解配置中：useFilter=false
        return results;
    }
}
```



### 示例二：执行配置文件中的SQL语句

新增配置文件 `demo.repo.xml`，内容如下：

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



在存储器中加载配置文件有两种方式，如下：

**方式一：** 通过 `@Repository` 注解的 `configFile` 属性指定具体配置文件路径名称（方法上的 `@Repository` 注解优先于类上的）。

```java
@Repository(configFile = "cfg/demo.repo.xml")
public class DemoRepository implements IRepository {

    @Repository(item = "custom_query", useFilter = true)
    public List<UserEntity> execQuery(Integer type, IResultSet<Object[]>... results) throws Exception {
        // 以下代码仅为了演示如何对结果集的再处理过程
        final List<UserEntity> returnValues = new ArrayList<>();
        if (results != null && results.length > 0) {
            ResultSetHelper.bind(results[0]).forEach((wrapper, row) -> {
                returnValues.add(wrapper.toEntity(new UserEntity()));
                return true;
            });
        }
        return returnValues;
    }
}
```



**方式二：** 通过 `IRepository` 存储器接口提供的 `getConfig` 方法指定具体配置文件对象（其优先于注解方式）。

新增配置类用于加载 `demo.repo.xml` 配置文件：

```java
@Configuration(value = "cfgs/demo.repo.xml")
public class DemoRepoConfig extends DefaultConfiguration {
}
```

重写存储器接口方法：

```java
@Repository
public class DemoRepository implements IRepository {

    @Inject
    private DemoRepoConfig demoRepoConfig;

    @Override
    public IConfiguration getConfig() {
        return demoRepoConfig;
    }

    @Repository(item = "custom_query", useFilter = true, resultClass = UserEntity.class)
    public List<UserEntity> execQuery(Integer type, IResultSet<UserEntity>... results) throws Exception {
        // 以下代码仅为了演示如何对结果集的再处理过程
        final List<UserEntity> returnValues = new ArrayList<>();
        if (results != null && results.length > 0) {
            ResultSetHelper.bind(results[0]).forEach((wrapper, row) -> {
                returnValues.add(wrapper.toEntity(new UserEntity()));
                return true;
            });
        }
        return returnValues;
    }
}
```



### 示例三：执行动态SQL语句及数据过滤

目前， JDBC 持久化模块的存储器默认仅支持基于 `JavaScript` 脚本语言实现的动态 SQL 语句拼装和数据过滤。

可以通过 `IRepositoryScriptProcessor` 脚本处理器接口实现自定义脚本语言并通过 `SPI` 机制向模块注册，接口结构及方法说明如下：

```java
@RepositoryScriptProcessor(value = "lua")
public class LuaRepositoryScriptProcessor implements IRepositoryScriptProcessor {

    /**
     * 初始化脚本处理器
     *
     * @param scriptStatement 脚本代码段
     * @throws Exception 可能产生的任何异常
     */
    @Override
    public void init(String scriptStatement) throws Exception {
    }

    /**
     * 执行处理器
     *
     * @param name   方法名称
     * @param params 参数集合
     * @return 返回最终预执行SQL语句
     * @throws Exception 可能产生的任何异常
     */
    @Override
    public String process(String name, Object... params) throws Exception {
        // 此处调用脚本中的具体方法实现SQL的动态拼装
        return null;
    }

    /**
     * 判断是否支持结果数据过滤
     *
     * @return 返回true表示支持结果数据过滤
     */
    @Override
    public boolean isFilterable() {
        // 请根据不同的脚本语言特性判断是否支持，比如freemarker模板语言就不支持动态脚本的执行
        return filterable;
    }

    /**
     * 执行结果数据过滤
     *
     * @param results 待过滤结果对象
     * @return 返回过滤后的结果对象
     */
    @Override
    public Object doFilter(Object results) {
        // 此处调用脚本中的具体方法实现数据过滤
        return null;
    }
}
```



以 `JavaScript` 脚本语言为例，修改配置文件 `demo.repo.xml` 内容如下：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<properties>
    <category name="default">
        <!-- 动态SQL也支持通过JavaScript脚本处理, 如下所示: -->
        <property name="custom_query" language="javascript">
            <value><![CDATA[
                // 方法名称要与name属性名称一致
                function custom_query(type) {
                    var sqlStr = "select * from user";
                    if (type) {
                        sqlStr += " where type = ${type}";
                    }
                    // 方式一：直接返回拼装后的SQL字符串
                    // return sqlStr;
                    // 方式二：添加回调方法用于过滤查询结果集
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
                            // 注意：返回的结果类型需与Java接口方法参数类型匹配
                            return result;
                        }
                    }
                }
            ]]></value>
        </property>
    </category>
</properties>
```

调整存储器类，此处请注意 `execQuery` 方法的最后一个参数的类型是 `List`，其与 `JavaScript` 脚本中指定的 `filter` 过滤器返回的类型须一致，否则将发生类型转换异常，代码如下：

```java
@Repository(configFile = "cfg/demo.repo.xml")
public class DemoRepository implements IRepository {

    @Repository(item = "custom_query", useFilter = true, resultClass = UserEntity.class)
    public List<UserEntity> execQuery(Integer type, List<UserEntity>... results) throws Exception {
        // 以下代码仅为了演示如何对结果集的再处理过程
        final List<UserEntity> returnValues = new ArrayList<>();
        if (results != null && results.length > 0) {
            results[0].stream()
                .filter(user -> BlurObject.bind(user.getAge()).toIntValue() > 18)
                .forEach(returnValues::add);
        }
        return returnValues;
    }
}
```



### 示例四：按数据库类型加载存储器实例

本例中 `IDemoRepository` 业务存储器接口类分别有 `MySQL` 和 `Oracle` 两种实现类，通过 `@Repository` 注解的 `dsName` 和 `dbType` 属性可以让框架在初始化时的自动扫描程序根据当前存储器实现所指定的数据源连接的数据库类型来判断是否加载，若数据源名称未设置则使用默认数据源，若数据库类型未设置则默认为支持全部数据库类型。

```java
// 业务存储器接口类
public interface IDemoRepository extends IRepository {
    ......
}

// 基于MySQL数据库的业务存储器接口实现类
@Repository(dbType = Type.DATABASE.MYSQL)
public class DemoMySQLRepository implements IDemoRepository {
    ......
}

// 基于Oracle数据库的业务存储器接口实现类
@Repository(dsName = "oracledb", dbType = Type.DATABASE.ORACLE)
public class DemoOracleRepository implements IDemoRepository {
    ......
}
```



## 高级特性

### 多表查询及自定义结果集数据处理

JDBC 持久化模块提供的 ORM 主要是针对单实体操作，实际业务中往往会涉及到多表关联查询以及返回多个表字段，在单实体 ORM 中是无法将 JDBC 结果集记录自动转换为实体对象的，这时就需要对结果集数据自定义处理来满足业务需求。
	
若想实现结果集数据的自定义处理，需要了解以下相关接口和类：

+ IResultSetHandler 接口：结果集数据处理接口，用于完成将 JDBC 结果集原始数据的每一行记录进行转换为目标对象，JDBC 持久化模块默认提供了该接口的三种实现：
  + EntityResultSetHandler：采用实体类存储结果集数据的接口实现，仅用于处理单实体的数据转换；
  + BeanResultSetHandler：将数据直接映射到类成员属性的结果集处理接口实现；
  + MapResultSetHandler：采用 Map 存储结果集数据的接口实现；
  + ArrayResultSetHandler：采用 Object[] 数组存储结果集数据的接口实现；

+ ResultSetHelper 类：数据结果集辅助处理工具，用于帮助开发人员便捷的读取和遍历结果集中数据内容，仅支持由 ArrayResultSetHandler 和 MapResultSetHandler 产生的结果集数据类型；



下面通过简单的多表关联查询来介绍 IResultSetHandler 接口和 ResultSetHelper 类如何配合使用。

**示例一：** 使用 ArrayResultSetHandler 或 MapResultSetHandler 处理结果集数据。

```java
// 通过查询对象创建如下 SQL 语句:
//
// SELECT u.id id, u.username, ue.money
// 			FROM user u LEFT JOIN user_ext ue ON u.id = ue.uid
//
IResultSet<Object[]> resultSet = Select.create(UserEntity.class, "u")
    .join(Join.left(UserExtEntity.TABLE_NAME).alias("ue")
          .on(Cond.create()
              .eqField(Fields.field("u", UserEntity.FIELDS.ID), Fields.field("ue", UserExtEntity.FIELDS.UID))))
    .field(Fields.create()
           .add("u", UserEntity.FIELDS.ID)
           .add("u", UserEntity.FIELDS.USERNAME)
           .add("ue", UserExtEntity.FIELDS.MONEY))
    // 执行查询并指定采用Object[]数组存储结果集数据，若采用Map存储请使用：IResultSetHandler.MAP.create()
    .find(IResultSetHandler.ARRAY.create());

// 采用默认步长(step=1)逐行遍历
ResultSetHelper.bind(resultSet).forEach(new ResultSetHelper.ItemHandler() {
    public boolean handle(ResultSetHelper.ItemWrapper wrapper, int row) throws Exception {
        System.out.println("当前记录行数: " + row);
        // 通过返回的结果集字段名取值
        String id = wrapper.getAsString("id");
        String username = wrapper.getAsString("username");
        // 也可以通过索引下标取值
        Double money = wrapper.getAsDouble(2);
        // 也可以直接将当前行数据赋值给实体对象或自定义JavaBean对象
        wrapper.toEntity(new UserEntity());
        // 当赋值给自定义的JavaBean对象时需要注意返回的字段名称与对象成员属性名称要一一对应并且要符合命名规范
        // 例如：对象成员名称为"userName"，将与名称为"user_name"的字段对应
        wrapper.toObject(new UserEntity());
        // 返回值将决定遍历是否继续执行
        return true;
    }
});

// 采用指定的步长进行数据遍历，此处step=2
ResultSetHelper.bind(resultSet).forEach(2, new ResultSetHelper.ItemHandler() {
    public boolean handle(ResultSetHelper.ItemWrapper wrapper, int row) throws Exception {
        // 代码略......
        return true;
    }
});
```



**示例二：** 使用自定义 IResultSetHandler 处理结果集数据。

```java
// 自定义JavaBean对象，用于封装多表关联的结果集的记录
public class CustomUser {

    private String id;

    private String username;

    private Double money;

    // 忽略Getter和Setter方法
}

// 修改示例一的代码，将结果集中的每一条记录转换成自定义的CustomUser对象
Select select = Select.create(UserEntity.class, "u")
    .join(Join.left(UserExtEntity.TABLE_NAME).alias("ue")
          .on(Cond.create()
              .eqField(Fields.field("u", UserEntity.FIELDS.ID), Fields.field("ue", UserExtEntity.FIELDS.UID))))
    .field("u", Fields.create(UserEntity.FIELDS.ID, UserEntity.FIELDS.USERNAME))
    .field("ue", UserExtEntity.FIELDS.MONEY);
// 通过实现IResultSetHandler接口实现结果集的自定义处理
select.find(new IResultSetHandler<CustomUser>() {
    @Override
    public List<CustomUser> handle(ResultSet resultSet) throws Exception {
        List<CustomUser> customUsers = new ArrayList<>();
        while (resultSet.next()) {
            CustomUser customUser = new CustomUser();
            customUser.setId(resultSet.getString("id"));
            customUser.setUsername(resultSet.getString("username"));
            customUser.setMoney(resultSet.getDouble("money"));
            //
            customUsers.add(customUser);
        }
        return customUsers;
    }
});
```



### 基于注解配置多表连接查询

在我们的日常项目开发过程中，多表关联查询是使用非常频繁的一种查询手段，在前面的 `多表查询及自定义结果集数据处理` 章节中已经介绍过如何通过代码编写多表关联查询及结果集的处理方法，但编写比较复杂的关联查询时仍然很麻烦，因此，JDBC 持久化模块特别提供了一系列注解类用来定义多表关联关系及字段与类成员之间的关系绑定，根据注解配置将自动生成 SQL 语句并执行，查询结果集也将被自动映射到类成员变量。

下面是多表关联查询时所涉及的一系注解及说明：

#### @QFroms

定义多个查询目标，值为 `@QFrom` 集合。

#### @QFrom

定义一个查询目标。

| 配置项 | 描述                                     |
| ------ | ---------------------------------------- |
| prefix | 前缀，默认为空                           |
| alias  | 别名，默认为空                           |
| type   | 值类型，默认为：`QFrom.Type.TABLE`（表） |
| value  | 表名或SQL语句字符串                      |



#### @QField

定义一个字段或用于类成员与字段之间的关系绑定。

| 配置项 | 描述           |
| ------ | -------------- |
| prefix | 前缀，默认为空 |
| alias  | 别名，默认为空 |
| value  | 名称           |



#### @QCond

定义一个条件。

| 配置项     | 描述                                                         |
| ---------- | ------------------------------------------------------------ |
| logicalOpt | 与已存在条件之间的逻辑关系，默认为：`Cond.LogicalOpt.AND`（与） |
| opt        | 条件字段之间的运算操作方式，默认为：`Cond.OPT.EQ`（等于）    |
| field      | 条件字段1                                                    |
| with       | 条件字段2<br />（支持以 `#` 开头的字符形式参数变量或以 `$` 开头的字符串原样传入） |
| ignorable  | 是否可被忽略<br />（仅以 `#` 开头的字符形式参数时有效，若为 `true` 则忽略当前条件并不会抛出异常） |



#### @QGroupBy

定义分组规则。

| 配置项 | 描述                                       |
| ------ | ------------------------------------------ |
| value  | 参于分组的字段集合                         |
| having | 条件过滤                                   |
| rollup | 是否对分组结果进行数据统计，默认为 `false` |



#### @QOrderBy

定义排序规则，值为 `@QOrderField` 集合。

#### @QOrderField

定义一个参与排序的字段。

| 配置项 | 描述                                               |
| ------ | -------------------------------------------------- |
| prefix | 前缀，默认为空                                     |
| value  | 字段名称                                           |
| type   | 排序类型，默认值为：`QOrderField.Type.ASC`（正序） |



#### @QWhere

定义查询所需条件，值为 `QCond` 集合。



#### @QJoins

定义多个关联关系配置，值为 `@QJoin` 集合。

#### @QJoin

定义一个关联关系配置。

| 配置项 | 描述                                         |
| ------ | -------------------------------------------- |
| from   | 指定关联表或子查询                           |
| on     | 设置关联条件集合                             |
| type   | 关联方式，默认为：`Join.Type.LEFT`（左连接） |



#### 示例：注解的使用与执行查询

定义一个基于注解的查询类：

```java
@QFrom(value = DeviceEntity.TABLE_NAME, alias = "d")
@QJoin(from = @QFrom(value = DeviceTypeEntity.TABLE_NAME, alias = "dt"),
        on = @QCond(field = @QField(prefix = "d", value = DeviceEntity.FIELDS.DEVICE_TYPE_ID), with = @QField(prefix = "dt", value = DeviceTypeEntity.FIELDS.ID)))
@QWhere({
        @QCond(field = @QField(prefix = "d", value = DeviceEntity.FIELDS.SUBARRAY_ID), with = @QField("#subarray_id")),
        @QCond(field = @QField(prefix = "d", value = DeviceEntity.FIELDS.PARENT_ID), with = @QField(value = "#parent_id"), ignorable = true),
        @QCond(field = @QField(prefix = "dt", value = DeviceTypeEntity.FIELDS.TYPE), with = @QField("#type"))
})
@QOrderBy(@QOrderField(prefix = "d", value = DeviceEntity.FIELDS.ORDER_NO))
public class SubarrayStatusVO implements Serializable {

    @QField(prefix = "d", value = DeviceEntity.FIELDS.ID)
    private String id;

    @QField(prefix = "d", value = DeviceEntity.FIELDS.DEVICE_TYPE_ID)
    private String deviceTypeId;

    @QField(prefix = "d", value = DeviceEntity.FIELDS.SUBARRAY_ID)
    private String subarrayId;

    @QField(prefix = "d", value = DeviceEntity.FIELDS.NAME)
    private String name;

    @QField(prefix = "d", value = DeviceEntity.FIELDS.METRIC_NAMES)
    private String metricNames;

    // 忽略Getter和Setter方法
}
```



执行查询类：

```java
IResultSet<SubarrayStatusVO> statusQuery(String subarrayId, String parentId, int type) throws Exception {
    Query.Executor<SubarrayStatusVO> executor = Query.build(SubarrayStatusVO.class)
        .addVariable("subarray_id", subarrayId)
        .addVariable("type", type == 1 ? "DEVICE_A" : "DEVICE_B");
    if (StringUtils.isNotBlank(parentId)) {
        executor.addVariable("parent_id", parentId);
    }
    return executor.find();
}
```



### 存储过程调用与结果集数据处理

针对于存储过程，JDBC 持久化模块提供了 `IProcedureOperator` 操作器接口及其默认接口实现类 `DefaultProcedureOperator` 来帮助你完成，存储过程有以下几种调用方式，举例说明：



**示例一：** 有输入参数无输出参数

```java
try (IDatabaseConnectionHolder connectionHolder = JDBC.get().getDefaultConnectionHolder()) {
    // 执行名称为`procedure_name`的存储过程，并向该存储过程转入两个字符串参数
    IProcedureOperator<Object[]> procedureOperator = new DefaultProcedureOperator<Object[]>("procedure_name", connectionHolder)
        .addParameter("param1")
        .addParameter("param2")
        .execute(IResultSetHandler.ARRAY.create());
    // 遍历结果集集合
    for (List<Object[]> resultSet : procedureOperator.getResultSets()) {
        ResultSetHelper.bind(resultSet).forEach(new ResultSetHelper.ItemHandler() {
            public boolean handle(ResultSetHelper.ItemWrapper wrapper, int row) throws Exception {
                System.out.println(wrapper.toObject(new UserEntity()));
                return true;
            }
        });
    }
}
```



**示例二：** 有输入输出参数

```java
try (IDatabaseConnectionHolder connectionHolder = JDBC.get().getDefaultConnectionHolder()) {
    // 通过addOutParameter方法按存储过程输出参数顺序指定JDBC参数类型
    new DefaultProcedureOperator<Void>("procedure_name", connectionHolder)
        .addParameter("param1")
        .addParameter("param2")
        .addOutParameter(Types.VARCHAR)
        .execute(new IProcedureOperator.IOutResultProcessor() {
            public void process(int idx, int paramType, Object result) throws Exception {
                System.out.println(result);
            }
        });
}
```



### 数据库锁操作

数据库是一个多用户使用的共享资源，当多个用户并发地存取数据时，在数据库中就会产生多个事务同时存取同一数据的情况，若对并发操作不加以控制就可能会造成数据的错误读取和存储，破坏数据库的数据一致性，所以说，加锁是实现数据库并发控制的一个非常重要的技术。
> 数据库加锁的流程是：当事务在对某个数据对象进行操作前，先向系统发出请求对其加锁，加锁后的事务就对该数据对象有了一定的控制，在该事务释放锁之前，其他的事务不能对此数据对象进行更新操作；

因此，JDBC 持久化模块在数据库查询操作中集成了针对数据库记录锁的控制能力，称之为 `IDBLocker`，以参数的方式使用起来同样的简单！

首先了解一下 `IDBLocker` 提供的锁的类型：

+ MySQL：

	> IDBLocker.DEFAULT：行级锁，只有符合条件的数据被加锁，其它进程等待资源解锁后再进行操作；

+ Oracle：

	> IDBLocker.DEFAULT：行级锁，只有符合条件的数据被加锁，其它进程等待资源解锁后再进行操作；
	>
	> IDBLocker.NOWAIT：行级锁，不进行资源等待，只要发现结果集中有些数据被加锁，立刻返回 “ORA-00054错误”；

+ SQL Server：

	> IDBLocker.SQLServer.NOLOCK：不加锁，在读取或修改数据时不加任何锁；
	>
	> IDBLocker.SQLServer.HOLDLOCK：保持锁，将此共享锁保持至整个事务结束，而不会在途中释放；
	>
	> IDBLocker.SQLServer.UPDLOCK：修改锁，能够保证多个进程能同时读取数据但只有该进程能修改数据；
	>
	> IDBLocker.SQLServer.TABLOCK：表锁，整个表设置共享锁直至该命令结束，保证其他进程只能读取而不能修改数据；
	>
	> IDBLocker.SQLServer.PAGLOCK：页锁；
	>
	> IDBLocker.SQLServer.TABLOCKX：排它表锁，将在整个表设置排它锁，能够防止其他进程读取或修改表中的数据；
	
+ 其它数据库：

	> 可以通过IDBLocker接口自行实现；



下面通过示例代码展示如何使用锁：

**示例一：** 通过 EntitySQL 对象传递锁参数

```java
session.find(EntitySQL.create(User.class)
        .field(Fields.create(User.FIELDS.ID, User.FIELDS.USER_NAME).excluded(true))
        .forUpdate(IDBLocker.DEFAULT));
```



**示例二：** 通过 Select 查询对象传递锁参数

```java
Select select = Select.create(User.class, "u")
        .field("u", "username").field("ue", "money")
        .where(Where.create(
                Cond.create().eq(User.FIELDS.ID).param("bc19f5645aa9438089c5e9954e5f1ac5")))
        .forUpdate(IDBLocker.DEFAULT);
select.find(IResultSetHandler.ARRAY.create());
```



**示例三：** 基于数据实体对象传递锁参数

```java
UserEntity user = new UserEntity();
user.setId("bc19f5645aa9438089c5e9954e5f1ac5");
user.load(IDBLocker.DEFAULT);
//
UserEntity user = new UserEntity();
user.setUsername("suninformation");
user.setPwd(DigestUtils.md5Hex("123456"));
IResultSet<User> users = _user.find(IDBLocker.DEFAULT);
```

:::tip **注意**：

请谨慎使用数据库锁机制，尽量避免产生锁表，以免发生死锁情况！

:::