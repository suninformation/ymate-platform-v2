---
name: ymp
description: YMP框架技能包，提供完整的Java框架开发技能支持，包括缓存、通用工具、配置、核心、AOP、事件、日志、MongoDB、JDBC持久化、插件、Redis、网络服务、验证和WebMVC等模块
version: 2.1.4
author: YMP Team
category: framework
tags:
  - java
  - framework
  - mvc
  - persistence
  - cache
  - redis
  - mongodb
  - validation
  - aop
  - event
  - plugin
  - web
  - nio
trigger: 当用户需要使用YMP框架进行Java应用开发、配置、持久化操作、缓存管理、Web开发等场景时触发
tools:
  - code-generation
  - configuration
  - database
  - cache
  - web-server
examples:
  - 创建YMP框架应用
  - 配置数据源
  - 实现持久化操作
  - 配置缓存
  - 开发Web控制器
  - 实现事件监听
  - 配置AOP拦截器
---

# YMP框架技能包文档

## 1. 概述

YMP框架是一个轻量级、模块化、简单而强大的Java框架，提供了丰富的功能模块，包括缓存、通用工具、配置、核心、AOP、事件、日志、MongoDB、JDBC持久化、插件、Redis、网络服务、验证和WebMVC等。本文档旨在系统地汇总和详细说明各个模块的技能信息，作为Skills技能包的官方文档，供开发人员查阅和使用。

## 2. 模块索引

- [缓存模块](#缓存模块)
- [通用工具包模块](#通用工具包模块)
- [配置模块](#配置模块)
- [核心模块](#核心模块)
- [Core-AOP模块](#core-aop模块)
- [事件模块](#事件模块)
- [日志模块](#日志模块)
- [MongoDB模块](#mongodb模块)
- [JDBC持久化模块](#jdbc持久化模块)
- [插件模块](#插件模块)
- [Redis模块](#redis模块)
- [Serv模块](#serv模块)
- [验证模块](#验证模块)
- [WebMVC模块](#webmvc模块)
- [测试模块](#测试模块)

## 3. 模块详情

### 缓存模块

#### 引导说明

**工具读取指引**：请读取 `cache/SKILL.md` 文件内容，获取缓存模块的详细技能信息。

#### 模块概述

缓存模块是以 EhCache 作为默认 JVM 进程内缓存服务，通过整合外部 Redis 服务实现多级缓存（MultiLevel）的轻量级缓存框架，并与 YMP 框架深度集成（支持针对类方法的缓存，可以根据方法参数值进行缓存），提供灵活的配置、易于使用和扩展的特性。

#### 核心功能

- **多级缓存支持**：基于内存的本地缓存（EhCache）和基于Redis的分布式缓存，融合两者的缓存服务
- **方法缓存**：通过 `@Cacheable` 注解标记类方法，自动缓存方法执行结果
- **缓存事件监听**：提供缓存事件监听接口，可监听缓存元素的添加、更新、过期、删除等事件
- **灵活的配置**：支持多种缓存提供者配置、自定义缓存键生成器、自定义序列化服务等

#### 技术架构

1. **API层**：提供 `Caches` 静态工具类，简化缓存操作
2. **核心层**：包含缓存管理、缓存提供者、缓存键生成器等核心组件
3. **实现层**：包含不同缓存提供者的具体实现
4. **集成层**：与YMP框架集成，支持方法缓存注解

#### 核心API

- **ICaches接口**：缓存服务核心接口，提供缓存操作方法
- **ICache接口**：单个缓存实例接口，提供缓存元素操作方法
- **@Cacheable注解**：标记需要缓存的方法

#### 使用示例

```java
@EnableAutoScan
public class Starter {

    static {
        System.setProperty(IApplication.SYSTEM_MAIN_CLASS, Starter.class.getName());
    }

    public static void main(String[] args) throws Exception {
        try (IApplication application = YMP.run(args)) {
            ICaches caches = Caches.get();
            // 1. 将键名为key1的值写入默认缓存
            caches.put("key1", "value1");
            // 2. 从默认缓存中读取键名为key1的值
            System.out.println(caches.get("key1"));
            // 3. 将键名为key2的值写入指定名称的缓存
            caches.put("default", "key2", "value2");
            // 4. 从指定名称的缓存中读取键名为key2的值
            System.out.println(caches.get("default", "key2"));
        }
    }
}
```

### 通用工具包模块

#### 引导说明

**工具读取指引**：请读取 `commons/SKILL.md` 文件内容，获取通用工具包模块的详细技能信息。

#### 模块概述

Commons 通用工具包是 YMP 框架中一个功能丰富的工具类库集合，是在开发 YMP 框架过程中积累下来的一些非常实用的辅助工具。该模块提供了广泛的工具类，涵盖了日常开发中常见的各种操作，如 HTTP 客户端、JSON 处理、文件操作、序列化、字符串处理、日期时间处理等。

#### 核心功能

- **基础类型（Lang）**：提供了针对任意对象之间的类型转换、组合和无限层级树型结构的支持
- **HttpClient**：基于 Apache HttpComponents 组件封装的 HttpClient 请求与处理工具
- **JSON 包装器**：为不同的第三方 JSON 解析器提供统一的 API 接口
- **Markdown**：对 Markdown 语法中常用到的格式进行对象封装
- **序列化（Serialize）**：基于 `ISerializer` 接口实现对象序列化与反序列化操作
- **Utils**：提供包含类与反射、字符串加密与解密、地理位置与编码、日期时间、正则表达式、文件、网络、参数、资源、运行时、线程操作等常用工具类封装

#### 核心API

- **BlurObject**：用于任意类型对象之间的转换
- **PairObject**：用于将任意两种类型的对象以 `<K, V>` 的形式组合在一起
- **TreeObject**：使用级联方式存储各种数据类型，不限层级深度，支持 JSON 转换
- **CloseableHttpClientHelper**：支持自定义安全连接方式，支持 GET、POST 请求方法，简化文件上传与下载的处理逻辑等
- **JsonWrapper**：为不同的第三方 JSON 解析器提供统一的 API 接口
- **SerializerManager**：对象序列化管理器，支持通过 `SPI` 机制和自动扫描 `@Serializer` 注解方式加载并注册序列化器

#### 使用示例

```java
// 基本数据类型转换
Object targetObj = "123.4";
BlurObject blurObject = BlurObject.bind(targetObj);
blurObject.toIntValue();
blurObject.toDoubleValue();

// JSON处理
IJsonObjectWrapper jsonObj = JsonWrapper.createJsonObject(true);
jsonObj.put("name", "suninformation");
jsonObj.put("age", 20);
System.out.println(jsonObj.toString(true, true));

// HttpClient使用
try (CloseableHttpClientHelper httpClientHelper = CloseableHttpClientHelper.create();
     IHttpResponse response = httpClientHelper.get(url, headers, charset)) {
    System.out.println("StatusCode: " + response.getStatusCode());
    System.out.println("Content: " + response.getContent());
}
```

### 配置模块

#### 引导说明

**工具读取指引**：请读取 `config/SKILL.md` 文件内容，获取配置模块的详细技能信息。

#### 模块概述

配置体系模块，是通过简单的目录结构实现在项目开发以及维护过程中，对配置文件等各种资源的统一管理，为模块化开发和部署提供灵活的、简单有效的解决方案。

#### 核心功能

- **统一资源管理**：规范化的目录结构，便于资源的组织和管理
- **多格式配置支持**：默认支持对 XML、Properties 和 JSON 配置文件的解析
- **注解驱动配置**：配置对象支持 @Configuration 注解方式声明，无需编码即可自动加载并填充配置内容到类对象
- **动态配置**：修改配置文件无需重启服务，支持自动重新加载
- **灵活的配置路径**：支持全局（configHome）> 项目（projects）> 模块（modules）的多级配置路径

#### 技术架构

1. **API层**：提供 `Cfgs` 静态工具类，简化配置操作
2. **核心层**：包含配置对象、配置提供者、配置文件解析器等核心组件
3. **实现层**：包含不同格式配置文件的解析器实现
4. **集成层**：与YMP框架集成，支持注解驱动配置

#### 核心API

- **IConfiguration接口**：配置对象接口，提供配置值的获取方法
- **IConfigurationProvider接口**：配置文件提供者接口，负责配置文件的解析和加载
- **@Configuration注解**：配置对象注解，用于声明配置类
- **@ConfigValue注解**：配置值注解，用于直接注入配置值到类成员变量

#### 使用示例

```java
@Configuration(value = "cfgs/configuration.xml", reload = true)
public class DemoConfig extends DefaultConfiguration {
}

// 使用配置
@Bean
@Configs(DemoConfig.class)
public class Demo {

    @ConfigValue("company_name")
    private String companyName;

    @ConfigValue("product_spec")
    private Map<String, String> productSpec;

    public String getCompanyName() {
        return companyName;
    }
}
```

### 核心模块

#### 引导说明

**工具读取指引**：请读取 `core/SKILL.md` 文件内容，获取核心模块的详细技能信息。

#### 模块概述

YMP框架核心模块是整个框架的基础，提供了应用容器、依赖注入、AOP拦截、事件机制等核心功能。

#### 核心功能

- **应用容器（IApplication）**：负责框架初始化、模块生命周期管理、事件广播与监听等核心功能
- **自动扫描（AutoScan）**：自动扫描并注册被@Bean注解标记的类，支持自定义扫描规则
- **依赖注入（DI）**：通过@Inject和@By注解实现对象之间的依赖注入，支持自定义注入逻辑
- **拦截器（AOP）**：基于代理技术实现的方法拦截，支持前置、后置和环绕拦截
- **事件服务（Events）**：通过事件的注册、订阅和广播实现模块间的解耦，支持同步和异步模式
- **国际化资源管理（I18N）**：提供多语言支持，支持资源文件的加载和切换
- **SPI加载机制**：支持通过SPI机制加载服务实现，提供默认实现和自定义实现的优先级管理

#### 技术架构

- **应用容器层**：由IApplication接口及其实现类组成，负责整体协调和管理
- **对象管理层**：由IBeanFactory接口及其实现类组成，负责对象的创建、管理和依赖注入
- **拦截器层**：由IInterceptor接口及其实现类组成，负责方法拦截和增强
- **事件层**：由Events类和IEventListener接口组成，负责事件的注册、触发和监听
- **国际化层**：由I18N接口及其实现类组成，负责国际化资源的管理
- **配置层**：负责框架和模块的配置管理，支持配置文件和注解两种方式

#### 核心API

- **IApplication接口**：应用容器核心接口，提供框架初始化、模块管理等功能
- **IBeanFactory接口**：对象工厂接口，负责对象的创建和依赖注入
- **Events类**：事件管理器，负责事件的注册、触发和监听
- **IInterceptor接口**：拦截器接口，用于方法的拦截和增强
- **I18N接口**：国际化资源管理器，负责多语言资源的管理

#### 使用示例

```java
@EnableAutoScan
@EnableBeanProxy
@EnableDevMode
public class Starter {
    static {
        System.setProperty(IApplication.SYSTEM_MAIN_CLASS, Starter.class.getName());
    }
    public static void main(String[] args) throws Exception {
        try (IApplication application = YMP.run(args)) {
            if (application.isInitialized()) {
                System.out.println("YMP框架初始化成功！");
                // 获取Bean
                UserService userService = application.getBeanFactory().getBean(UserService.class);
                // 调用方法
                String result = userService.getUserName("123");
                System.out.println(result);
            }
        }
    }
}
```

### Core-AOP模块

#### 引导说明

**工具读取指引**：请读取 `core-aop/SKILL.md` 文件内容，获取Core-AOP模块的详细技能信息。

#### 模块概述

Core-AOP 模块是 YMP 框架中的核心模块之一，提供了强大的面向切面编程（AOP）功能。该模块基于代理技术实现了方法拦截机制，允许开发者在方法执行前后插入自定义逻辑，而无需修改原有代码结构。

#### 核心功能

- **基于注解的拦截器配置**：通过丰富的注解来配置拦截器
- **支持前置、后置和环绕拦截**：可以满足各种拦截需求
- **支持拦截器参数传递**：通过@ContextParam注解传递参数
- **支持多层拦截器嵌套**：可以配置多个拦截器
- **支持拦截器全局规则设置**：通过配置文件或注解设置全局规则
- **支持自定义拦截器注解**：可以创建自定义的拦截器注解

#### 核心API

- **IInterceptor接口**：拦截器接口，是所有拦截器的基础接口
- **AbstractInterceptor抽象类**：拦截器抽象类，提供了更方便的拦截器编写方式
- **InterceptContext接口**：拦截上下文对象，包含了拦截相关的所有信息
- **@Interceptor注解**：声明一个类为拦截器
- **@Before注解**：配置前置拦截器
- **@After注解**：配置后置拦截器
- **@Around注解**：配置环绕拦截器

#### 使用示例

```java
@Interceptor
public class LogInterceptor extends AbstractInterceptor {

    private static final Log LOG = LogFactory.getLog(LogInterceptor.class);

    @Override
    protected Object before(InterceptContext context) throws InterceptException {
        long startTime = System.currentTimeMillis();
        context.getContextParams().put("startTime", startTime);
        LOG.info("Method " + context.getTargetMethod().getName() + " started");
        return null;
    }

    @Override
    protected Object after(InterceptContext context) throws InterceptException {
        long startTime = (long) context.getContextParams().get("startTime");
        long endTime = System.currentTimeMillis();
        LOG.info("Method " + context.getTargetMethod().getName() + " completed in " + (endTime - startTime) + "ms");
        return null;
    }
}

@Bean
public class UserService {
    @Before(LogInterceptor.class)
    public String getUserName(String userId) {
        return "User: " + userId;
    }
}
```

### 事件模块

#### 引导说明

**工具读取指引**：请读取 `event/SKILL.md` 文件内容，获取事件模块的详细技能信息。

#### 模块概述

Event 模块是 YMP 框架中的一个核心模块，提供了事件驱动的编程模型。该模块通过事件的注册、订阅和广播机制，实现了模块之间的解耦，降低了代码的侵入性，提高了系统的可维护性和可扩展性。

#### 核心功能

- **支持事件的注册、订阅和广播**：实现模块间的解耦
- **支持同步（NORMAL）和异步（ASYNC）两种事件处理模式**：可以根据业务需求选择合适的模式
- **基于多线程的事件处理机制**：提高系统性能
- **支持事件监听器的优先级设置**：可以控制事件处理的顺序
- **支持事件的过滤和拦截**：可以更精细地控制事件处理
- **与框架其他模块无缝集成**：支持依赖注入等特性

#### 核心API

- **IEventListener接口**：事件监听器接口，是所有事件监听器的基础接口
- **IEvent接口**：事件接口，表示一个具体的事件
- **Events类**：事件管理器类，负责事件的注册、订阅和广播
- **@EventListener注解**：声明一个类为事件监听器
- **@EventRegister注解**：声明一个类为事件注册器，用于注册事件监听器

#### 使用示例

```java
public class UserEvent extends AbstractEventContext<Object, UserEvent.EVENT> implements IEvent {
    public enum EVENT {
        USER_CREATED,
        USER_UPDATED,
        USER_DELETED
    }
    public UserEvent(Object owner, EVENT eventName) {
        super(owner, UserEvent.class, eventName);
    }
}

@EventListener(mode = Events.Mode.ASYNC, value = UserEvent.class, priority = 10)
public class UserEventListener implements IEventListener<UserEvent> {

    private static final Log LOG = LogFactory.getLog(UserEventListener.class);

    @Override
    public boolean handle(UserEvent context) throws Exception {
        UserEvent.EVENT eventName = (UserEvent.EVENT) context.getEventName();
        switch (eventName) {
            case USER_CREATED:
                LOG.info("User created event triggered");
                break;
            case USER_UPDATED:
                LOG.info("User updated event triggered");
                break;
            case USER_DELETED:
                LOG.info("User deleted event triggered");
                break;
        }
        return false;
    }
}

// 触发事件
try (IApplication application = YMP.run(args)) {
    Events events = application.getEvents();
    UserEvent event = new UserEvent(application, UserEvent.EVENT.USER_CREATED);
    events.fireEvent(event);
}
```

### 日志模块

#### 引导说明

**工具读取指引**：请读取 `log/SKILL.md` 文件内容，获取日志模块的详细技能信息。

#### 模块概述

Log 日志模块是 YMP 框架中基于开源日志框架 Log4j 2 实现的日志管理模块，提供日志记录器对象的统一管理，并整合了 JCL、Slf4j 等优秀的日志系统，可以在任意位置调用任意日志记录器输出日志，实现了系统与业务日志的分离，同时与 YMP 配置体系模块配合使用，效果更佳。

#### 核心功能

- **统一日志管理**：提供日志记录器对象的统一管理
- **多日志系统整合**：整合了 JCL、Slf4j 等优秀的日志系统
- **灵活的配置**：支持通过配置文件和注解进行配置
- **系统与业务日志分离**：可以根据需要将不同类型的日志输出到不同的文件
- **日志格式定制**：支持自定义日志格式模板

#### 核心API

- **ILogger接口**：日志记录器接口，提供了各种级别的日志记录方法
- **Logs工具类**：日志模块的工具类，提供了获取日志记录器的静态方法
- **LogFactory类**：日志工厂类，用于创建日志记录器

#### 使用示例

```java
// 基于 JCL 接口调用
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LogDemo {

    private static final Log LOG = LogFactory.getLog(LogDemo.class);

    public static void main(String[] args) throws Exception {
        if (LOG.isInfoEnabled()) {
            LOG.info("Everything depends on ability!  -- YMP :)");
        }
    }
}

// 基于 Slf4j 接口调用
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogDemo {

    private static final Logger LOG = LoggerFactory.getLogger(LogDemo.class);

    public static void main(String[] args) throws Exception {
        if (LOG.isInfoEnabled()) {
            LOG.info("Everything depends on ability!  -- {} :)", "YMP");
        }
    }
}

// 任意日志输出
Logs.get().getLogger().debug("Everything depends on ability!  -- YMP :)");
```

### MongoDB模块

#### 引导说明

**工具读取指引**：请读取 `mongodb/SKILL.md` 文件内容，获取MongoDB模块的详细技能信息。

#### 模块概述

MongoDB 持久化模块是 YMP 框架中针对 MongoDB 数据存取操作的专业封装，以 JDBC 持久化模块的设计思想为基础，采用会话机制，简化事务处理逻辑，支持多数据源配置和实体操作，基于操作器（IOperator）对象化拼装查询条件，并集成 MapReduce、GridFS、聚合及函数表达式等高级特性。

#### 核心功能

- **多数据源支持**：支持配置多个 MongoDB 数据源，灵活切换
- **会话管理**：提供会话机制，统一管理连接资源
- **事务支持**：支持 MongoDB 事务操作，确保数据一致性
- **GridFS 文件存储**：支持大文件存储和管理
- **丰富的查询表达式**：提供多种查询表达式，支持复杂查询条件构建
- **实体映射**：支持对象与 MongoDB 文档之间的映射
- **操作器 API**：提供流畅的操作器 API，简化查询条件构建
- **聚合操作**：支持 MongoDB 聚合操作
- **表达式系统**：内置丰富的表达式类型，支持各种查询场景

#### 技术架构

MongoDB 模块采用分层架构设计，主要包含以下核心组件：

1. **模块核心**：`MongoDB` 类，实现 `IMongo` 接口，负责模块初始化和管理
2. **会话层**：`IMongoSession` 和 `IGridFsSession` 接口，提供操作方法
3. **连接层**：`IMongoConnectionHolder` 接口，管理数据库连接
4. **数据源层**：`IMongoDataSourceAdapter` 接口，适配不同数据源
5. **表达式层**：各种表达式实现，如 `Query`、`Operator` 等
6. **事务层**：`ITransaction` 接口，管理事务操作

#### 核心API

- **MongoDB类**：模块核心类，提供获取会话、管理数据源等方法
- **IMongoSession接口**：MongoDB 会话接口，提供 CRUD 操作方法
- **IGridFsSession接口**：GridFS 会话接口，提供文件操作方法
- **Query类**：查询对象，用于构建查询条件
- **Operator类**：操作符对象，用于构建查询操作符
- **Aggregation类**：聚合操作对象，用于构建聚合查询

#### 使用示例

```java
// 插入数据
UserEntity user = new UserEntity();
user.setUsername("admin");
user.setPassword("123456");
user = MongoDB.get().openSession(session -> session.insert(user));

// 查询数据
UserEntity foundUser = MongoDB.get().openSession(session ->
    session.find(UserEntity.class, user.getId())
);

// 更新数据
foundUser.setEmail("updated@example.com");
MongoDB.get().openSession(session -> session.update(foundUser));

// 删除数据
MongoDB.get().openSession(session ->
    session.delete(UserEntity.class, user.getId())
);
```

### JDBC持久化模块

#### 引导说明

**工具读取指引**：请读取 `persistence-jdbc/SKILL.md` 文件内容，获取JDBC持久化模块的详细技能信息。

#### 模块概述

JDBC 持久化模块是 YMP 框架中针对关系型数据库（RDBMS）数据存取的一套轻量级解决方案，主要关注数据存取的效率、易用性、稳定性和透明度。该模块基于 JDBC 框架 API 进行封装，提供了丰富的功能特性，简化了数据库操作的复杂性，使开发者能够更加专注于业务逻辑的实现。

#### 核心功能特性

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

#### 使用示例

```java
// 插入数据
UserEntity user = UserEntity.builder()
    .id(UUIDUtils.UUID())
    .username("suninformation")
    .password(DigestUtils.md5Hex("123456"))
    .build();
user.save();

// 查询数据
UserEntity foundUser = UserEntity.builder()
    .id(user.getId())
    .build().load();

// 更新数据
foundUser.bind().nickname("有理想的鱼").build().update();

// 删除数据
foundUser.delete();
```

### 插件模块

#### 引导说明

**工具读取指引**：请读取 `plugin/SKILL.md` 文件内容，获取插件模块的详细技能信息。

#### 模块概述

Plugin 模块是 YMP 框架中的一个重要模块，提供了插件化开发的能力。该模块采用独立的类加载器（ClassLoader）来管理私有包、类、资源文件等，设计目标是在接口开发模式下，将需求进行更细颗粒度拆分，从而达到一个理想化可重用代码的封装形态。

#### 主要功能特点

- 独立的类加载器管理插件的私有包、类和资源文件
- 支持插件的自动扫描和注册
- 支持插件的生命周期管理（初始化、启动、停止、销毁）
- 支持插件的依赖注入
- 支持插件的事件监听
- 支持多个插件工厂实例，工厂对象之间完全独立
- 支持从 JAR 包或目录加载插件

#### 使用示例

```java
// 创建插件
@Plugin(id = "echo_plugin", name = "DemoPlugin", version = "1.0.0")
public class EchoPlugin extends AbstractPlugin implements IEchoService {
    @Override
    protected void doInitialize(IPluginContext context) throws Exception {
        System.out.println("initialized.");
    }

    @Override
    public void sayHi() {
        System.out.println("Hi, from Plugin.");
    }
}

// 使用插件
@EnableAutoScan
public class Starter {
    public static void main(String[] args) throws Exception {
        try (IApplication application = YMP.run(args)) {
            IEchoService echoService = Plugins.get().getPlugin(IEchoService.class);
            echoService.sayHi();
        }
    }
}
```

### Redis模块

#### 引导说明

**工具读取指引**：请读取 `redis/SKILL.md` 文件内容，获取Redis模块的详细技能信息。

#### 模块概述

Redis 持久化模块是 YMP 框架中基于 Jedis 驱动的 Redis 客户端封装，以 JDBC 持久化模块的设计思想进行简单封装，采用会话机制，统一 API 调用，简化订阅（subscribe）和发布（publish）操作，支持多数据源及连接池配置，支持 jedis、shard、sentinel 和 cluster 等数据源连接方式。

#### 核心功能

- **统一 API 调用**：封装不同 Redis 连接模式的操作接口，提供一致的 API 调用方式
- **多数据源支持**：支持配置多个 Redis 数据源，适用于复杂的应用场景
- **连接池管理**：内置连接池配置，优化连接资源的使用
- **会话机制**：采用会话机制管理连接资源，确保资源正确释放
- **发布/订阅支持**：简化 Redis 的发布/订阅操作

#### 使用示例

```java
// 使用默认数据源开启会话
Redis.get().openSession(new IRedisSessionExecutor<Object>() {
    @Override
    public Object execute(IRedisSession session) throws Exception {
        return session.getConnectionHolder().getConnection().set("key", "value");
    }
});

// 使用指定的数据源开启会话
String value = Redis.get().openSession("otherredis", new IRedisSessionExecutor<String>() {
    @Override
    public String execute(IRedisSession session) throws Exception {
        return session.getConnectionHolder().getConnection().get("key");
    }
});
```

### Serv模块

#### 引导说明

**工具读取指引**：请读取 `serv/SKILL.md` 文件内容，获取Serv模块的详细技能信息。

#### 模块概述

Serv 模块是 YMP 框架中的一个基于 NIO 实现的通讯服务框架，提供 TCP、UDP 协议的客户端与服务端封装，灵活的消息监听与消息内容编/解码，简约的配置使二次开发更加便捷。同时针对客户端提供默认的断线重连、链路维护（心跳）等服务支持，开发者只需了解业务即可轻松完成开发工作。

#### 主要功能特点

- 基于 NIO 实现的高性能通讯框架
- 支持 TCP 和 UDP 协议
- 提供多种编/解码器（ByteArrayCodec、NioStringCodec、TextLineCodec）
- 内置链路维护（心跳）服务
- 内置断线重连服务
- 支持会话管理和流量统计
- 支持事件监听机制
- 简约的配置方式

#### 使用示例

```java
// TCP服务端
IServerCfg serverCfg = DefaultServerCfg.builder()
        .serverName("tcpServer")
        .serverHost("0.0.0.0")
        .port(8281)
        .build();
NioServer nioServer = Servs.createServer(serverCfg, new TextLineCodec(), new NioServerListener() {
    @Override
    public void onMessageReceived(Object message, INioSession session) throws IOException {
        session.send("Hi, guys! I received a message: " + message);
    }
});
nioServer.start();

// TCP客户端
IClientCfg clientCfg = DefaultClientCfg.builder()
        .clientName("tcpClient")
        .remoteHost("0.0.0.0")
        .port(8281)
        .build();
NioClient nioClient = Servs.createClient(clientCfg, new TextLineCodec(), new DefaultReconnectServiceImpl(), new DefaultHeartbeatServiceImpl(), new NioClientListener());
nioClient.connect();
```

### 验证模块

#### 引导说明

**工具读取指引**：请读取 `validation/SKILL.md` 文件内容，获取验证模块的详细技能信息。

#### 模块概述

验证模块是 YMP 框架中的服务端参数有效性验证工具，采用注解声明方式配置验证规则，更简单、更直观、更友好。该模块支持方法参数和类成员属性验证，支持验证结果国际化 I18N 资源绑定，支持自定义验证器，支持多种验证模式。

#### 核心功能

- **注解式验证**：通过简单的注解声明，快速配置验证规则
- **多种验证器**：内置丰富的验证器，覆盖常见验证场景
- **国际化支持**：验证错误信息支持 I18N 国际化资源绑定
- **自定义验证器**：支持开发者自定义验证器，扩展验证能力
- **多层级验证**：支持嵌套对象验证，实现复杂数据结构的验证
- **验证模式**：支持短路式验证（NORMAL）和全量验证（FULL）两种模式

#### 使用示例

```java
@Validation(mode = Validation.MODE.FULL)
public class UserBase {

    @VRequired(msg = "{0}不能为空")
    @VLength(min = 3, max = 16, msg = "{0}长度必须在3到16之间")
    @VField(label = "用户名称")
    private String username;

    @VRequired
    @VLength(eq = 32)
    @VMsg("{0}无效")
    @VField(name = "密码")
    private String password;

    // Getter和Setter方法
}

// 执行验证
Map<String, Object> paramValues = new HashMap<>();
paramValues.put("username", "lz");
paramValues.put("password", "1233");

Map<String, ValidateResult> resultMap = Validations.get()
    .validate(UserBase.class, paramValues);
resultMap.forEach((key, value) -> System.out.println(value));
```

### WebMVC模块

#### 引导说明

**工具读取指引**：请读取 `webmvc/SKILL.md` 文件内容，获取WebMVC模块的详细技能信息。

#### 模块概述

WebMVC 模块是 YMP 框架中除了 JDBC 持久化模块以外的另一个非常重要的模块，集成了 YMP 框架的诸多特性，在功能结构的设计和使用方法上依然保持一贯的简单风格，同时也继承了主流 MVC 框架的基因，对于了解和熟悉 SSH 或 SSM 等框架技术的开发人员来说，上手极其容易，毫无学习成本。

#### 核心功能

- **标准 MVC 实现**：结构清晰，完全基于注解方式配置简单
- **约定模式支持**：无需编写控制器代码，直接匹配并执行视图渲染
- **多种视图技术**：支持 Binary、Forward、Freemarker、HTML、HttpStatus、JSON、JSP、Redirect、Text、Velocity 等
- **RESTful 支持**：支持 RESTful 模式及 URL 风格
- **自动参数绑定**：支持请求参数与控制器方法参数的自动绑定
- **参数有效性验证**：集成验证框架，支持参数验证
- **控制器方法拦截**：支持控制器方法的拦截
- **注解配置路由**：支持注解配置控制器请求路由映射
- **自动扫描注册**：支持自动扫描控制器类并注册
- **自定义事件和异常处理**：支持事件和异常的自定义处理
- **I18N 国际化**：支持 I18N 资源国际化
- **缓存支持**：支持控制器方法和视图缓存
- **插件扩展**：支持插件扩展
- **URL 扩展名匹配**：支持通过 @RequestMapping 注解的 suffix 属性匹配带扩展名的请求路径
- **扩展名值注入**：支持通过 @RequestSuffix 注解将请求路径的扩展名值注入到控制器方法参数

#### 使用示例

```java
@Controller
@RequestMapping("/hello")
public class HelloController {

    @RequestMapping(value = "/", method = {Type.HttpMethod.GET, Type.HttpMethod.POST})
    public IView hello() throws Exception {
        return View.textView("Everything depends on ability!  -- YMP :)");
    }
}

@Controller
@RequestMapping("/demo")
public class DemoController {

    @RequestMapping("/param")
    public IView testParam(@RequestParam String name,
                           @RequestParam(defaultValue = "18") Integer age) {
        return View.textView(String.format("Hi, %s, Age: %d", name, age));
    }

    @RequestMapping("/json")
    public Object jsonView() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "YMP");
        data.put("version", "2.1.4");
        return data; // 自动转换为 JSON
    }
}
```

### 测试模块

#### 引导说明

**工具读取指引**：请读取 `test/SKILL.md` 文件内容，获取测试模块的详细技能信息。

#### 模块概述

测试模块是 YMP 框架中的单元测试工具包，集成了 JUnit 5 和 JUnit 4 的测试开发支持，分别提供了对应的单测、套件扩展类及专属注解与使用方式，封装了核心工具类 YMPTestUtils 统一管理应用初始化逻辑，同时给出了两类 JUnit 版本下模拟控制器请求、存储器接口调用和组合单元测试的具体使用示例，整体支持依赖注入、测试生命周期管理、Bean 工厂注册等功能。

#### 核心功能

- **JUnit 5 支持**：YMPJUnit5Extension、YMPJUnit5Suite
- **JUnit 4 支持**：YMPJUnit4ClassRunner、YMPJUnit4Suite
- **核心工具类**：YMPTestUtils
- **模拟工具**：MockWebRequestHelper、MockHttpServletRequest/Response

#### 使用示例

```java
// JUnit 5 示例
@ExtendWith(YMPJUnit5Extension.class)
@EnableAutoScan
@EnableBeanProxy
@EnableDevMode
public class LoginControllerTest {

    @Inject
    private WebMVC webmvc;

    @Test
    public void testLogin() throws Exception {
        MockHttpServletResponse response = MockWebRequestHelper.create(webmvc)
            .post("/login")
            .parameter("uname", "admin")
            .parameter("passwd", DigestUtils.md5Hex("admin"))
            .parameter("format", "json")
            .doFilter();
        Assertions.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        JsonWrapper jsonWrapper = JsonWrapper.fromJson(response.getContentAsString());
        Assertions.assertNotNull(jsonWrapper);
    }
}
```

## 4. 快速开始

### 4.1 Maven 依赖

```xml
<dependency>
    <groupId>net.ymate.platform</groupId>
    <artifactId>ymate-platform-core</artifactId>
    <version>2.1.4-dev</version>
</dependency>
```

### 4.2 创建应用

```java
@EnableAutoScan
@EnableBeanProxy
@EnableDevMode
public class Starter {
    static {
        System.setProperty(IApplication.SYSTEM_MAIN_CLASS, Starter.class.getName());
    }
    public static void main(String[] args) throws Exception {
        try (IApplication application = YMP.run(args)) {
            if (application.isInitialized()) {
                System.out.println("YMP框架初始化成功！");
            }
        }
    }
}
```

## 5. 最佳实践

1. **模块化开发**：根据业务需求选择合适的模块，避免引入不必要的依赖
2. **注解优先**：优先使用注解配置，减少 XML 配置文件的使用
3. **依赖注入**：充分利用框架的依赖注入功能，降低模块间耦合
4. **异常处理**：合理使用拦截器和事件机制处理异常
5. **性能优化**：合理使用缓存、连接池等机制提高应用性能
6. **测试驱动**：使用测试模块编写单元测试，确保代码质量

## 6. 总结

YMP框架是一个轻量级、模块化、简单而强大的Java框架，提供了丰富的功能模块，帮助开发者快速构建高质量的应用程序。通过本文档的介绍，相信开发者能够快速掌握YMP框架的使用方法，并在实际项目中灵活应用各个模块的功能。
