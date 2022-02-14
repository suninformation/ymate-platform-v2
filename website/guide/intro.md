---
sidebar_position: 1
slug: /
---

# 概述（Introduction）

![LOGO](/img/logo_small.png)

[![Maven Central status](https://img.shields.io/maven-central/v/net.ymate.platform/ymate-platform-core.svg)](https://search.maven.org/#search%7Cga%7C1%7Cnet.ymate.platform)
[![LICENSE](https://img.shields.io/github/license/suninformation/ymate-platform-v2.svg)](https://gitee.com/suninformation/ymate-platform-v2/blob/master/LICENSE.txt)


> A lightweight modular simple and powerful Java application development framework.

> 一个非常简单、易用的一套轻量级 Java 应用开发框架，设计原则主要侧重于简化工作任务、规范开发流程、提高开发效率，让开发工作像搭积木一样轻松是我们一直不懈努力的目标！



## 技术特点（Features）

- 采用组件化、模块方式打包，可按需装配，灵活可扩展；
- 采用微内核实现 AutoScan、AOP、IoC、Events 等，涵盖 SSH 和 SSM 框架中绝大部分核心功能；
- 统一配置体系结构，感受不一样的文件资源配置及管理模式；
- 整合多种日志系统（Log4j、JCL、Slf4j 等）、日志文件可分离存储；
- 轻量级持久化层封装，针对 RDBMS（MySQL、SQL Server、Oracle、PostgreSQL）和 NoSQL（MongoDB、Redis）提供支持；
- 完善的插件机制，助力于更细颗粒度的业务拆分；
- 独特的独立服务（Serv）开发体验；
- 功能强大的验证框架，完全基于 Java 注解，易于使用和扩展；
- 灵活的缓存服务，支持  EhCache、Redis 和多级缓存（MultiLevel）技术；
- 配置简单的 MVC 架构，强大且易于维护和扩展，支持 RESTful 风格，支持 JSP、HTML、Binary、Freemarker、Velocity 等多种视图技术；



## 模块及功能（Modules）

YMP 框架主要是由核心（Core）和若干模块（Modules）组成，整体结构简约、清晰，如图所示：

![Structure Diagram](/img/structure_diagram.png)

### 核心（Core）

核心也称之为应用容器（IApplication），主要负责框架初始化、事件（Events）广播与监听、模块的定义及其生命周期管理、国际化资源管理（I18N）和类对象管理等，其核心功能是对包和类的自动扫描（AutoScan）、对象的生命周期管理、以及反转控制（IoC）、依赖注入（DI）和方法拦截（AOP）等。

[点击阅读 >>](core)



### 配置体系（Configuration）

通过简单的目录结构实现项目开发、维护过程中，对配置文件等各种资源的统一管理，为模块化开发和部署提供灵活的、简单有效的解决方案：

- 从开发角度规范了模块化开发流程、统一资源文件的生命周期管理；
- 从可维护角度将全部资源集成在整个体系中，具备有效的资源重用和灵活的系统集成构建、部署和数据备份与迁移等优势；
- 简单的配置文件检索、加载及管理模式；
- 模块间资源共享，模块（modules）可以共用所属项目（projects）的配置、类和jar包等资源文件；
- 默认支持对 XML、Properties 和 JSON 配置文件的解析，可以通过 IConfigurationProvider 接口自定义文件格式，支持缓存，避免重复加载；
- 配置对象支持 @Configuration 注解方式声明，无需编码即可自动加载并填充配置内容到类对象；
- 修改配置文件无需重启服务，支持自动重新加载；
- 集成模块的构建（编译）与分发、服务的启动与停止，以及清晰的资源文件分类结构可快速定位；

[点击阅读 >>](configuration)



### 日志（Log）

基于开源日志框架 Log4j 2 实现，提供日志记录器对象的统一管理，并整合了 JCL、Slf4j 等优秀的日志系统，可以在任意位置调用任意日志记录器输出日志，实现了系统与业务日志的分离，同时与 YMP 配置体系模块配合使用，效果更佳。

[点击阅读 >>](log)



### 持久化（Persistence）

#### JDBC

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

[点击阅读 >>](persistence/jdbc)



#### MongoDB

针对 MongoDB 的数据存取操作的特点，以 JDBC 持久化模块的设计思想进行简单封装，采用会话机制，简化事务处理逻辑，支持多数据源配置和实体操作，基于操作器（IOperator）对象化拼装查询条件，并集成MapReduce、GridFS、聚合及函数表达式等。

[点击阅读 >>](persistence/mongodb)



#### Redis

基于 Jedis 驱动，以 JDBC 持久化模块的设计思想进行简单封装，采用会话机制，统一 API 调用，简化订阅（subscribe）和发布（publish）操作，支持多数据源及连接池配置，支持 jedis、shard、sentinel 和 cluster 等数据源连接方式。

[点击阅读 >>](persistence/redis)



### 插件（Plugin）

插件模块采用独立的类加载器（ClassLoader）来管理私有包、类、资源文件等，设计目标是在接口开发模式下，将需求进行更细颗粒度拆分，从而达到一个理想化可重用代码的封装形态。

每个插件都是封闭的世界，插件与外界之间沟通的唯一方法是通过业务接口调用，管理这些插件的容器被称之为插件工厂（IPluginFactory），其负责插件的分析、加载和初始化，以及插件的生命周期管理，插件模块支持创建多个插件工厂实例，工厂对象之间完全独立，无任何依赖关系。

[点击阅读 >>](plugin)



### 服务（Serv）

服务（Serv）是一套基于 NIO 实现的通讯服务框架，提供 TCP、UDP 协议的客户端与服务端封装，灵活的消息监听与消息内容编/解码，简约的配置使二次开发更加便捷。同时针对客户端提供默认的断线重连、链路维护（心跳）等服务支持，您只需了解业务即可轻松完成开发工作。

[点击阅读 >>](serv)



### 验证（Validation）

验证模块是服务端参数有效性验证工具，采用注解声明方式配置验证规则，更简单、更直观、更友好，支持方法参数和类成员属性验证，支持验证结果国际化 I18N 资源绑定，支持自定义验证器，支持多种验证模式。

[点击阅读 >>](validation)



### 缓存（Cache）

缓存模块是以 EhCache 作为默认 JVM 进程内缓存服务，通过整合外部 Redis 服务实现多级缓存（MultiLevel）的轻量级缓存框架，并与 YMP 框架深度集成（支持针对类方法的缓存，可以根据方法参数值进行缓存），灵活的配置、易于使用和扩展。

[点击阅读 >>](cache)



### WebMVC

WebMVC 模块在 YMP 框架中是除了 JDBC 持久化模块以外的另一个非常重要的模块，集成了 YMP 框架的诸多特性，在功能结构的设计和使用方法上依然保持一贯的简单风格，同时也继承了主流 MVC 框架的基因，对于了解和熟悉 SSH 或 SSM 等框架技术的开发人员来说，上手极其容易，毫无学习成本。

其主要功能特性如下：

- 标准 MVC 实现，结构清晰，完全基于注解方式配置简单；
- 支持约定模式，无需编写控制器代码，直接匹配并执行视图‘
- 支持多种视图技术（Binary、Forward、Freemarker、HTML、HttpStatus、JSON、JSP、Redirect、Text、Velocity等）；
- 支持 RESTful 模式及 URL 风格；
- 支持请求参数与控制器方法参数的自动绑定；
- 支持参数有效性验证；
- 支持控制器方法的拦截；
- 支持注解配置控制器请求路由映射；
- 支持自动扫描控制器类并注册；
- 支持事件和异常的自定义处理；
- 支持I18N资源国际化；
- 支持控制器方法和视图缓存；
- 支持插件扩展；

[点击阅读 >>](webmvc)



### 通用工具包（Commons）

常用的工具类库封装，是在开发 YMP 框架过程中积累下来的一些非常实用的辅助工具，其中主要涉及 HttpClient 请求包装器、JSON 包装器、文件及资源管理、数据导入与导出、视频图片处理、二维码、序列化、类、日期时间、数学、经纬度、字符串加解密、运行时环境、网络、线程操作等。

[点击阅读 >>](commons)



import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

:::tip One More Thing

YMP 不仅提供便捷的 Web 及其它 Java 项目的快速开发体验，也将不断提供更多丰富的项目实践经验。

感兴趣的小伙伴儿们可以加入官方 QQ 群：[480374360](https://qm.qq.com/cgi-bin/qm/qr?k=3KSXbRoridGeFxTVA8HZzyhwU_btZQJ2)，一起交流学习，帮助 YMP 成长！

如果喜欢 YMP，希望得到你的支持和鼓励！

<Tabs
defaultValue="donationCode"
values={[
{label: '请喝一杯咖啡', value: 'donationCode'},
{label: '微信支付', value: 'wepay'},
{label: '支付宝', value: 'alipay'},
]}>
<TabItem value="donationCode"><img src="/img/donation_code.png" alt="donationCode"/></TabItem>
<TabItem value="wepay"><img src="/img/wepay.png" alt="WePay"/></TabItem>
<TabItem value="alipay"><img src="/img/alipay.jpeg" alt="AliPay"/></TabItem>
</Tabs>

了解更多有关 YMP 框架的内容，请访问官网：[https://ymate.net](https://ymate.net)

:::