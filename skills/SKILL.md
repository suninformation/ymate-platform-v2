---
name: ymp
description: YMP框架技能包总入口。根据场景路由到子模块技能包：core(核心容器/DI/AOP/Event)、persistence-jdbc(关系型ORM)、webmvc(WebMVC控制器)、validation(参数验证)、cache(缓存)、redis(Redis)、mongodb(MongoDB)、commons(工具类)、configuration(配置)、plugin(插件)、serv(NIO通讯)、log(日志)、test(单元测试)
version: 2.1.4-dev
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
trigger: 当用户需要使用YMP框架进行Java应用开发、配置、持久化操作、缓存管理、Web开发等任何场景时触发。AI应根据具体子场景自动读取对应的子模块SKILL.md。
tools:
  - code-generation
  - configuration
  - database
  - cache
  - web-server
examples:
  - 创建YMP框架标准启动类
  - 基于@Bean+@Inject实现DI
  - 使用persistence-jdbc实现实体CRUD
  - 用WebMVC编写控制器和RESTful接口
  - 使用validation模块做参数验证
---

# YMP框架技能包（总入口）

> **AI读取指引**：本文件是总索引，**不包含各模块细节**。请根据用户的具体子任务**立即跳转到对应的子模块SKILL.md**。只有在用户仅问整体概念、全局规范或标准项目骨架时才停留在此文件。

---

## 0. 全局编码与文件规范（AI必须优先遵守，优先级高于各子模块）

> 本章节内容来源于用户规则，所有通过AI生成的YMP框架代码必须符合以下规范。

### 0.1 项目版本
- 当前Maven工程 `${project.version}` = **2.1.4-dev**
- 所有新创建的Java类/方法注释中 `@since` 必须写 **2.1.4-dev**（编辑已存在类/方法时**不修改**原有 `@since`）

### 0.2 文件格式要求
- 换行符：**LF**
- 编码：**UTF-8 无BOM**

### 0.3 系统环境（AI必须根据用户实际OS动态选择规则）

> **AI执行规则**：先判断用户当前操作系统（从 `<env>` 标签或用户环境信息读取），再按以下对应规则选择 shell 语法和路径分隔符；若无法判断则**优先询问用户**，不要主观假设。

| 操作系统 | Shell 语法 | 路径分隔符 | 文件系统说明 |
|---|---|---|---|
| **Windows** | PowerShell 7+ 语法（优先）；兼容 cmd | `\` 反斜杠，脚本中建议用 `/` 兼容 | 支持盘符 `C:\` `D:\`；注意路径中包含空格需加引号 |
| **macOS** | Bash 或 Zsh（默认 zsh） | `/` 正斜杠 | 根目录 `/`；用户目录 `/Users/用户名`；注意 SIP 系统完整性保护路径 |
| **Linux** | Bash（默认）；兼容其他 POSIX Shell | `/` 正斜杠 | 根目录 `/`；用户目录 `/home/用户名`；注意权限 chmod/chown |

- JDK版本：若无法从Maven判断，默认 **JDK 8** 兼容语法

### 0.4 Java文件必须包含的内容
所有新建 `.java` 文件开头必须附带 **Apache License 2.0** 协议头，格式如下：

```java
/*
 * Copyright 2007-2024 the original author or authors.
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
package xxx.xxx.xxx;
```

### 0.5 Java类注释（新建类必填）
```java
/**
 * 类功能一句话描述。
 *
 * @author 开发者姓名（如无则填 YMP Team）
 * @since 2.1.4-dev
 */
public class XxxClass {
}
```

### 0.6 Java方法注释（新建方法必填）
```java
/**
 * 方法功能一句话描述。
 *
 * @param param1 参数1说明
 * @param param2 参数2说明
 * @return 返回值说明
 * @throws ExceptionType 抛出异常说明（如有）
 * @since 2.1.4-dev
 */
public ReturnType methodName(ParamType param1, ParamType param2) throws ExceptionType {
}
```
> 编辑已存在方法时，**不修改**原有 `@since`；仅在新增方法时写当前版本。

### 0.7 编译命令

> **跨平台说明**：优先使用已安装的全局 `mvn` 命令（跨平台通用）；若项目根目录存在 Maven Wrapper 脚本，请按操作系统选择：
> - Windows：`.\mvnw.cmd clean install`（PowerShell 中 `./mvnw.cmd` 也可用）
> - macOS/Linux：`./mvnw clean install`（首次执行前请 `chmod +x mvnw` 赋执行权限）

- 完整编译：`mvn clean install`
- 跳过测试加速：`mvn clean install -DskipTests`
- 单模块编译（避免全量构建）：`mvn clean install -pl <模块artifactId> -am`（如 `mvn clean install -pl ymate-platform-persistence-jdbc -am`）

---

## 1. 模块技能包快速索引

| 序号 | 模块名 | 子SKILL路径 | 典型触发场景（AI据此路由） |
|---|---|---|---|
| 1 | 核心 Core | [core/SKILL.md](core/SKILL.md) | 创建启动类、@Bean/@Inject依赖注入、YMP.run()、@EnableAutoScan、IApplication容器、Bean生命周期 |
| 2 | AOP切面 | [core-aop/SKILL.md](core-aop/SKILL.md) | @Before/@After/@Around方法拦截、IInterceptor自定义拦截器、@EnableBeanProxy |
| 3 | 配置体系 | [configuration/SKILL.md](configuration/SKILL.md) | @Configuration配置类、@ConfigValue注入、Cfgs工具类、XML/Properties/JSON配置文件、动态reload |
| 4 | 通用工具 Commons | [commons/SKILL.md](commons/SKILL.md) | BlurObject类型转换、JsonWrapper、HttpClient请求、UUIDUtils、DateTimeHelper、CodecUtils加密、ClassUtils反射、Retry重试、Serializer序列化 |
| 5 | JDBC持久化 | [persistence-jdbc/SKILL.md](persistence-jdbc/SKILL.md) | 实体Entity CRUD、@Repository存储器、@Transaction事务、多数据源、查询构建器、结果集自动装配 |
| 6 | MongoDB | [mongodb/SKILL.md](mongodb/SKILL.md) | MongoDB实体、GridFS、聚合Aggregation、Query/Operator条件构建、会话管理 |
| 7 | Redis | [redis/SKILL.md](redis/SKILL.md) | Jedis会话、发布订阅、sentinel/cluster/shard多数据源 |
| 8 | 缓存 Cache | [cache/SKILL.md](cache/SKILL.md) | @Cacheable方法缓存、EhCache本地+Redis多级缓存、ICaches/ Caches工具类、缓存Key生成器 |
| 9 | 参数验证 Validation | [validation/SKILL.md](validation/SKILL.md) | @VRequired/@VEmail/@VLength等内置验证器、分组验证groups、条件验证@VCondition、自定义IValidator、嵌套@VModel |
| 10 | WebMVC | [webmvc/SKILL.md](webmvc/SKILL.md) | @Controller控制器、@RequestMapping路由、@RequestParam/@PathVariable参数绑定、@VUploadFile/@VHostName/@VToken Web专用验证器、View工厂、跨域@CrossDomain、@ResponseCache、DispatchFilter |
| 11 | 插件 Plugin | [plugin/SKILL.md](plugin/SKILL.md) | @Plugin插件、独立ClassLoader、生命周期、Plugins.get()获取插件 |
| 12 | NIO通讯 Serv | [serv/SKILL.md](serv/SKILL.md) | TCP/UDP NIO服务端/客户端、心跳断线重连、编解码Codec、消息监听 |
| 13 | 事件 Event | [event/SKILL.md](event/SKILL.md) | IEvent事件、@EventListener监听器、同步/异步模式、Events.fireEvent() |
| 14 | 日志 Log | [log/SKILL.md](log/SKILL.md) | LogFactory.getLog()、Logs工具类、整合JCL/Slf4j、配置Log4j2 |
| 15 | 单元测试 Test | [test/SKILL.md](test/SKILL.md) | JUnit4/JUnit5扩展、@ExtendWith(YMPJUnit5Extension)、MockWebRequestHelper模拟请求 |

---

## 2. 标准项目骨架（最简可运行）

### 2.1 Maven pom.xml 核心依赖
```xml
<properties>
    <project.version>2.1.4-dev</project.version>
    <maven.compiler.source>1.8</maven.compiler.source>
    <maven.compiler.target>1.8</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>

<dependencies>
    <!-- 核心（必须） -->
    <dependency>
        <groupId>net.ymate.platform</groupId>
        <artifactId>ymate-platform-core</artifactId>
        <version>${project.version}</version>
    </dependency>

    <!-- 按需要追加其它模块：
         ymate-platform-persistence-jdbc / ymate-platform-webmvc /
         ymate-platform-validation  / ymate-platform-cache / ...
    -->
</dependencies>
```

### 2.2 最简启动类 Starter.java
```java
/*
 * Copyright 2007-2024 the original author or authors.
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
package com.example;

import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.annotation.EnableAutoScan;
import net.ymate.platform.core.annotation.EnableBeanProxy;
import net.ymate.platform.core.annotation.EnableDevMode;

/**
 * YMP应用标准启动类。
 *
 * @author YMP Team
 * @since 2.1.4-dev
 */
@EnableAutoScan                  // 开启类路径自动扫描（注册@Bean）
@EnableBeanProxy                 // 开启AOP代理（拦截器、@Cacheable等需要）
@EnableDevMode                   // 开发模式（生产请移除）
public class Starter {

    static {
        // 必须：设置启动主类，供框架定位根包
        System.setProperty(IApplication.SYSTEM_MAIN_CLASS, Starter.class.getName());
    }

    /**
     * 应用入口。
     *
     * @param args 启动参数
     * @throws Exception 启动异常
     * @since 2.1.4-dev
     */
    public static void main(String[] args) throws Exception {
        try (IApplication application = YMP.run(args)) {
            if (application.isInitialized()) {
                // 启动后业务逻辑写在此处
                // 例如：获取Bean：application.getBeanFactory().getBean(MyService.class)
            }
        }
    }
}
```

### 2.3 最简 DI + AOP 业务服务
```java
/*
 * Copyright 2007-2024 the original author or authors.
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
package com.example.service;

import net.ymate.platform.core.beans.annotation.Bean;
import net.ymate.platform.core.beans.annotation.Inject;

/**
 * 用户服务示例。
 *
 * @author YMP Team
 * @since 2.1.4-dev
 */
@Bean
public class UserService {

    @Inject
    private OrderService orderService;

    /**
     * 根据用户ID查询用户名称。
     *
     * @param userId 用户ID
     * @return 用户名称
     * @since 2.1.4-dev
     */
    public String getUserName(String userId) {
        return "User:" + userId + " -> Orders:" + orderService.countByUser(userId);
    }
}
```

---

## 3. 常见全局坑点（必读）

| # | 坑点 | 原因 | 解决 |
|---|---|---|---|
| 1 | `@Inject` 注入字段为 null | 类没有被 `@Bean` 注册，或未开启 `@EnableAutoScan`，或字段是 `new` 出来而非从容器获取 | 保证类上有 `@Bean`，通过 `application.getBeanFactory().getBean(X.class)` 获取实例 |
| 2 | AOP拦截器 / `@Cacheable` / `@Transaction` 完全不生效 | 未添加 `@EnableBeanProxy`，或方法是 `private`/`static`/`final`，或对象不是从容器获取的 | 类上补 `@EnableBeanProxy`（启动类）+ 方法必须是 `public` 非静态 |
| 3 | 打包后找不到 `ymp-conf.properties` / 找不到配置类 | `-Dymp.mainClass` 未设置，或配置类不在扫描路径 | 启动类 static 块写 `System.setProperty(IApplication.SYSTEM_MAIN_CLASS, Starter.class.getName())`；或 JVM 参数 `-Dymp.mainClass=xxx.Starter` |
| 4 | 注解配置 vs 配置文件优先级冲突 | 两者共存时 **配置文件非空值优先于注解** | 希望注解生效请在配置文件中留空对应项 |
| 5 | WebMVC 文件上传拿不到 `IUploadFileWrapper` | 方法/类缺失 `@FileUpload` 注解 | 控制器方法或类加 `@FileUpload` |
| 6 | `@VRequired` 等验证器不生效 | Web环境：验证注解要放在 `@RequestParam` 同一参数上；独立调用：必须调用 `Validations.get().validate(...)` | 检查注解放置位置和实际触发方式 |

---

## 4. 核心注解快速包路径速查（AI生成import备用）

> 全限定名表。AI在写代码时遇到这些注解，直接从此表取全限定名拼 import。

| 注解/类 | 全限定名 | 作用 |
|---|---|---|
| `@Bean` | `net.ymate.platform.core.beans.annotation.Bean` | 注册到IOC容器 |
| `@Inject` | `net.ymate.platform.core.beans.annotation.Inject` | 字段/构造器依赖注入 |
| `@By` | `net.ymate.platform.core.beans.annotation.By` | 指定注入实现（配合@Inject多实现场景） |
| `@EnableAutoScan` | `net.ymate.platform.core.annotation.EnableAutoScan` | 启动类开启自动扫描 |
| `@EnableBeanProxy` | `net.ymate.platform.core.annotation.EnableBeanProxy` | 启动类开启CGLIB代理（AOP/事务/缓存必须） |
| `@EnableDevMode` | `net.ymate.platform.core.annotation.EnableDevMode` | 开发模式（热加载/更多日志） |
| `@Before` | `net.ymate.platform.core.aop.annotation.Before` | 前置拦截器 |
| `@After` | `net.ymate.platform.core.aop.annotation.After` | 后置拦截器 |
| `@Around` | `net.ymate.platform.core.aop.annotation.Around` | 环绕拦截器 |
| `@Interceptor` | `net.ymate.platform.core.aop.annotation.Interceptor` | 标记类为拦截器 |
| `@EventListener` | `net.ymate.platform.core.event.annotation.EventListener` | 标记类为事件监听器 |
| `@EventRegister` | `net.ymate.platform.core.event.annotation.EventRegister` | 事件注册器 |
| `@Configuration` | `net.ymate.platform.configuration.annotation.Configuration` | 配置类声明 |
| `@ConfigValue` | `net.ymate.platform.configuration.annotation.ConfigValue` | 配置值注入 |
| `@Controller` | `net.ymate.platform.webmvc.annotation.Controller` | WebMVC控制器 |
| `@RequestMapping` | `net.ymate.platform.webmvc.annotation.RequestMapping` | 路由映射 |
| `@RequestParam` | `net.ymate.platform.webmvc.annotation.RequestParam` | 请求参数绑定 |
| `@PathVariable` | `net.ymate.platform.webmvc.annotation.PathVariable` | 路径变量绑定 |
| `@ModelBind` | `net.ymate.platform.webmvc.annotation.ModelBind` | 对象级参数绑定 |
| `@CrossDomain` | `net.ymate.platform.webmvc.annotation.CrossDomain` | 跨域支持 |
| `@FileUpload` | `net.ymate.platform.webmvc.annotation.FileUpload` | 文件上传开关 |
| `@ResponseCache` | `net.ymate.platform.webmvc.annotation.ResponseCache` | Web响应缓存 |
| `@Cacheable` | `net.ymate.platform.cache.annotation.Cacheable` | 方法级缓存 |
| `@CacheConf` | `net.ymate.platform.cache.annotation.CacheConf` | 缓存模块配置 |
| `@Transaction` | `net.ymate.platform.persistence.jdbc.transaction.annotation.Transaction` | JDBC事务 |
| `@Repository` | `net.ymate.platform.persistence.jdbc.repo.annotation.Repository` | JDBC存储器接口 |
| `@Repository` | `net.ymate.platform.persistence.mongodb.repo.annotation.Repository` | MongoDB存储器接口（包不同） |
| `@Validation` | `net.ymate.platform.validation.annotation.Validation` | 开启验证模式（类/方法） |
| `@VRequired` | `net.ymate.platform.validation.annotation.VRequired` | 必填 |
| `@VEmail` | `net.ymate.platform.validation.annotation.VEmail` | 邮箱 |
| `@VLength` | `net.ymate.platform.validation.annotation.VLength` | 字符串长度 |
| `@VCompare` | `net.ymate.platform.validation.annotation.VCompare` | 参数比较 |
| `@VField` | `net.ymate.platform.validation.annotation.VField` | 验证字段命名 |
| `@VModel` | `net.ymate.platform.validation.annotation.VModel` | 嵌套对象验证 |
| `@VMsg` | `net.ymate.platform.validation.annotation.VMsg` | 自定义验证消息 |
| `@ValidateGroups` | `net.ymate.platform.validation.annotation.ValidateGroups` | 声明分组（类/方法） |
| `@VCondition` | `net.ymate.platform.validation.annotation.VCondition` | 条件验证 |
| `@VUploadFile` | `net.ymate.platform.webmvc.validation.annotation.VUploadFile` | 上传文件验证 |
| `@VHostName` | `net.ymate.platform.webmvc.validation.annotation.VHostName` | 主机名校验 |
| `@VToken` | `net.ymate.platform.webmvc.validation.annotation.VToken` | CSRF令牌校验 |
| `@Plugin` | `net.ymate.platform.plugin.annotation.Plugin` | 插件类声明 |
| `@Serializer` | `net.ymate.platform.commons.serialize.annotation.Serializer` | 自定义序列化器 |
| `IApplication` | `net.ymate.platform.core.IApplication` | 应用容器接口 |
| `YMP` | `net.ymate.platform.core.YMP` | 启动入口（YMP.run(args)） |
| `Caches` | `net.ymate.platform.cache.Caches` | 缓存工具入口 |
| `Validations` | `net.ymate.platform.validation.Validations` | 验证工具入口 |
| `View` | `net.ymate.platform.webmvc.view.View` | Web视图工厂 |
| `JDBC` | `net.ymate.platform.persistence.jdbc.JDBC` | JDBC模块入口 |
| `MongoDB` | `net.ymate.platform.persistence.mongodb.MongoDB` | MongoDB模块入口 |
| `Redis` | `net.ymate.platform.persistence.redis.Redis` | Redis模块入口 |
| `Plugins` | `net.ymate.platform.plugin.Plugins` | 插件工具入口 |
| `Servs` | `net.ymate.platform.serv.Servs` | NIO服务/客户端工厂 |
| `WebMVC` | `net.ymate.platform.webmvc.WebMVC` | WebMVC模块实例 |

---

## 5. 模块依赖图（最简组合）

```
[ymate-platform-commons]   ← 所有其它模块都依赖（工具类基础库）
       ↑
[ymate-platform-core]      ← 核心容器（IOC/AOP/Event/I18N），除commons外所有模块都依赖
       ↑
       ├→ [ymate-platform-configuration] 配置体系
       ├→ [ymate-platform-cache]          缓存（含EhCache/Redis多级）
       ├→ [ymate-platform-validation]     参数验证
       ├→ [ymate-platform-plugin]         插件
       ├→ [ymate-platform-event]          事件（通常已被core包含）
       ├→ [ymate-platform-log]            日志（通常已被core包含）
       ├→ [ymate-platform-serv]           NIO通讯
       ├→ [ymate-platform-persistence-jdbc]    关系型数据库ORM
       ├→ [ymate-platform-persistence-mongodb] MongoDB持久化
       ├→ [ymate-platform-persistence-redis]   Redis持久化
       └→ [ymate-platform-webmvc]         WebMVC（间接依赖validation/persistence-jdbc等）
```

> 当用户只需要某功能但未加依赖时，AI应主动提示需要添加的 artifactId。
