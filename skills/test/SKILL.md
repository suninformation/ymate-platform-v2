---
name: ymp-test
description: YMP框架测试模块，集成了JUnit 5和JUnit 4的测试开发支持，提供模拟工具和测试生命周期管理
version: 2.1.4
author: YMP Team
category: testing
tags:
  - java
  - testing
  - junit
  - unit-test
trigger: 当用户需要编写单元测试、集成测试、模拟HTTP请求等场景时触发
tools:
  - junit
  - testing-framework
  - mock
examples:
  - 使用JUnit 5测试
  - 使用JUnit 4测试
  - 模拟控制器请求
  - 测试存储器接口
  - 组合单元测试
---

# 测试模块（Test）

## 1. 模块概述

测试模块是 YMP 框架中的单元测试工具包，集成了 JUnit 5 和 JUnit 4 的测试开发支持，分别提供了对应的单测、套件扩展类及专属注解与使用方式，封装了核心工具类 YMPTestUtils 统一管理应用初始化逻辑，同时给出了两类 JUnit 版本下模拟控制器请求、存储器接口调用和组合单元测试的具体使用示例，整体支持依赖注入、测试生命周期管理、Bean 工厂注册等功能。

## 2. 核心功能

### 2.1 JUnit 5 支持

- **YMPJUnit5Extension**：实现了 TestInstanceFactory、ParameterResolver 和 AfterAllCallback 接口，支持通过 YMP 容器创建测试实例，支持在测试方法中注入 IApplication 参数，支持测试完成后清理缓存和销毁应用，完善的应用生命周期管理。
- **YMPJUnit5Suite**：基于 JUnit 5 的 @Suite 注解，支持组合多个测试类，使用独立的 YMPJUnit5SuiteExtension 扩展，确保 YMP 应用仅初始化一次，整个测试套件执行完毕后自动销毁应用。

### 2.2 JUnit 4 支持

- **YMPJUnit4ClassRunner**：实现了 JUnit 4 的 Runner 接口，为单个测试类提供 YMP 集成，使用 YMPTestUtils 统一管理应用初始化，测试执行完毕后自动销毁应用。
- **YMPJUnit4Suite**：基于 JUnit 4 的 Suite 运行器，支持组合多个测试类，使用独立的 YMPJUnit4RunnerBuilder 类管理测试运行，整个测试套件执行完毕后自动销毁应用。

### 2.3 核心工具类

- **YMPTestUtils**：统一管理 YMP 应用初始化逻辑，减少代码重复，提供 InitConfig 配置类，支持自定义应用初始化参数。

### 2.4 模拟工具

- **MockWebRequestHelper**：用于模拟控制器方法请求，支持 HTTP 方法、参数设置、请求头设置等。
- **MockHttpServletRequest/Response**：模拟 HTTP 请求和响应对象，支持各种 HTTP 相关操作。

## 3. 技术架构

测试模块采用分层架构设计，主要包含以下核心组件：

1. **JUnit 集成层**：包含 JUnit 5 和 JUnit 4 的扩展类和运行器，负责与测试框架的集成。
2. **核心工具层**：包含 YMPTestUtils 等核心工具类，负责应用初始化和管理。
3. **模拟层**：包含 MockWebRequestHelper 和各种 Mock 类，负责模拟 HTTP 请求和响应。
4. **集成层**：与 YMP 框架的其他模块无缝集成，支持依赖注入等特性。

## 4. 核心 API

### 4.1 JUnit 5 相关

- **YMPJUnit5Extension**：JUnit 5 扩展类，用于集成 YMP 框架。
- **YMPJUnit5Suite**：JUnit 5 测试套件注解，用于组合多个测试类。
- **YMPJUnit5SuiteExtension**：JUnit 5 测试套件扩展类，确保 YMP 应用仅初始化一次。

### 4.2 JUnit 4 相关

- **YMPJUnit4ClassRunner**：JUnit 4 运行器，用于集成 YMP 框架。
- **YMPJUnit4Suite**：JUnit 4 测试套件运行器，用于组合多个测试类。
- **YMPJUnit4RunnerBuilder**：JUnit 4 运行器构建器，用于管理测试运行。

### 4.3 核心工具

- **YMPTestUtils**：测试工具类，负责 YMP 应用的初始化和管理。
- **YMPTestUtils.InitConfig**：初始化配置类，用于自定义应用初始化参数。

### 4.4 模拟工具

- **MockWebRequestHelper**：用于模拟控制器方法请求的工具类。
- **MockHttpServletRequest**：模拟 HTTP 请求对象。
- **MockHttpServletResponse**：模拟 HTTP 响应对象。

## 5. 依赖关系

测试模块依赖以下 YMP 框架模块：

- **core**：核心模块，提供应用容器和依赖注入功能。
- **webmvc**（可选）：WebMVC 模块，用于模拟控制器请求。
- **persistence-jdbc**（可选）：JDBC 持久化模块，用于测试存储器接口。

## 6. 使用示例

### 6.1 JUnit 5 使用示例

#### 示例一：模拟控制器方法请求

```java
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

#### 示例二：存储器接口方法调用

```java
@ExtendWith(YMPJUnit5Extension.class)
@EnableAutoScan
@EnableBeanProxy
@EnableDevMode
public class SystemConfigRepositoryTest {

    @Inject
    private JDBC database;

    @Inject
    private ISystemConfigRepository repository;

    @Test
    public void testQuerySystemConfigs() throws Exception {
        SystemConfigBean systemConfigBean = SystemConfigBean.builder()
            .siteId("ymate.net")
            .build();
        IResultSet<SystemConfigVO> systemConfigs = repository.querySystemConfigs(database, systemConfigBean, Page.create());
        Assertions.assertNotNull(systemConfigs);
    }
}
```

#### 示例三：组合单元测试

```java
@YMPJUnit5Suite({
    LoginControllerTest.class,
    SystemConfigRepositoryTest.class
})
@EnableAutoScan
@EnableBeanProxy
@EnableDevMode
public class ControllersTest {
}
```

### 6.2 JUnit 4 使用示例

#### 示例一：模拟控制器方法请求

```java
@RunWith(YMPJUnit4ClassRunner.class)
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
        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        JsonWrapper jsonWrapper = JsonWrapper.fromJson(response.getContentAsString());
        Assert.assertNotNull(jsonWrapper);
    }
}
```

#### 示例二：存储器接口方法调用

```java
@RunWith(YMPJUnit4ClassRunner.class)
@EnableAutoScan
@EnableBeanProxy
@EnableDevMode
public class SystemConfigRepositoryTest {

    @Inject
    private JDBC database;

    @Inject
    private ISystemConfigRepository repository;

    @Test
    public void testQuerySystemConfigs() throws Exception {
        SystemConfigBean systemConfigBean = SystemConfigBean.builder()
            .siteId("ymate.net")
            .build();
        IResultSet<SystemConfigVO> systemConfigs = repository.querySystemConfigs(database, systemConfigBean, Page.create());
        Assert.assertNotNull(systemConfigs);
    }
}
```

#### 示例三：组合单元测试

```java
@RunWith(YMPJUnit4Suite.class)
@SuiteClasses({
    LoginControllerTest.class,
    SystemConfigRepositoryTest.class
})
@EnableAutoScan
@EnableBeanProxy
@EnableDevMode
public class ControllersTest {
}
```

## 7. 配置说明

### 7.1 Maven 依赖配置

```xml
<dependency>
    <groupId>net.ymate.platform</groupId>
    <artifactId>ymate-platform-test</artifactId>
    <version>2.1.4-dev</version>
</dependency>
```

### 7.2 应用初始化配置

测试模块支持通过 YMPTestUtils.InitConfig 类自定义应用初始化参数，包括：

- **mainClassName**：主类名称，用于指定应用的主类。
- **testClasses**：测试类列表，用于指定需要注册到 Bean 工厂的测试类。
- **systemProperties**：系统属性，用于设置应用的系统属性。

## 8. 技术特点

- **无缝集成**：与 JUnit 5 和 JUnit 4 无缝集成，支持两种测试框架的所有特性。
- **统一管理**：通过 YMPTestUtils 统一管理应用初始化逻辑，减少代码重复。
- **模拟功能**：提供丰富的模拟工具，支持模拟 HTTP 请求和响应。
- **依赖注入**：支持依赖注入，便于测试对象的创建和管理。
- **灵活配置**：支持多种配置方式，适应不同的测试场景。
- **生命周期管理**：完善的应用生命周期管理，测试执行完毕后自动销毁应用，避免资源泄漏。

## 9. 最佳实践

1. **选择合适的测试框架**：根据项目需求选择 JUnit 5 或 JUnit 4。
2. **使用套件测试**：对于多个相关的测试类，使用套件测试可以减少应用初始化次数，提高测试效率。
3. **模拟 HTTP 请求**：对于控制器测试，使用 MockWebRequestHelper 模拟 HTTP 请求，避免启动真实的 Web 服务器。
4. **依赖注入**：使用 @Inject 注解注入测试所需的依赖对象，提高测试的可维护性。
5. **合理配置**：根据测试需要，合理配置应用初始化参数，确保测试环境的一致性。
