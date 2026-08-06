---
name: ymp-test
description: YMP框架单元测试工具包，集成JUnit5(@ExtendWith YMPJUnit5Extension)与JUnit4(@RunWith YMPJUnit4ClassRunner)，支持@Inject依赖注入、组合套件测试、MockWebRequestHelper模拟WebMVC控制器请求、MockHttpServletRequest/Response
version: 2.1.4-dev
author: YMP Team
category: framework
tags:
  - java
  - framework
  - test
  - junit5
  - junit4
  - mock
  - webmvc
  - integration-test
trigger: 当用户需要写YMP单元测试、@ExtendWith(YMPJUnit5Extension)/@RunWith(YMPJUnit4ClassRunner)、@Inject注入Bean到测试类、MockWebRequestHelper模拟控制器请求、套件@YMPJUnit5Suite组合测试时触发
tools:
  - junit5
  - junit4
  - mock-web-request
  - test-suite
  - dependency-injection-test
examples:
  - JUnit5: @ExtendWith + @Inject WebMVC/JDBC + @Test断言
  - MockWebRequestHelper.create(webmvc).post().parameter().doFilter()模拟登录请求
  - JUnit4 @RunWith(YMPJUnit4ClassRunner.class)兼容旧项目
  - @YMPJUnit5Suite组合多个测试类一次初始化
  - 存储器Repository接口方法调用集成测试
---

# Test 单元测试技能包

> AI读取指引：本模块边界=JUnit集成+Mock请求；所有类路径前缀`net.ymate.platform.test`(扩展)与`net.ymate.platform.mock`(Mock工具)；Mock控制器请求需配合webmvc模块(跳转webmvc/SKILL.md)；持久化测试跳转persistence-jdbc/mongodb/redis子模块。

---

## 0. 快速索引

- Maven artifactId：`ymate-platform-test`（需另外依赖单元测试框架junit-jupiter-api:5.x或junit:4.12）
- 静态入口类：`net.ymate.platform.test.YMPTestUtils`(initializeYMP/InitConfig)；Mock入口`net.ymate.platform.mock.MockWebRequestHelper`
- 必备注解：JUnit5=`@ExtendWith(YMPJUnit5Extension.class)`；JUnit4=`@RunWith(YMPJUnit4ClassRunner.class)`；配合`@EnableAutoScan`+`@EnableBeanProxy`(需AOP时)
- 5行最简JUnit5测试（5行内）：
```java
@ExtendWith(YMPJUnit5Extension.class)
@EnableAutoScan
class MyTest {
    @Inject private MyService svc;
    @Test void t(){ Assertions.assertNotNull(svc); }
}
```

## 1. 模块摘要

YMP单元测试工具包封装JUnit5与JUnit4的容器生命周期：自动初始化IApplication、注册测试类到BeanFactory支持@Inject注入、测试后自动销毁避免内存泄漏；提供MockWebRequestHelper走完整WebMVC过滤器链模拟HTTP请求（含参数/响应/Json解析）；支持Suite套件一次初始化，多个测试类共享应用容器。

- **JUnit5集成**：`@ExtendWith(YMPJUnit5Extension.class)` 支持TestInstanceFactory创建实例+ParameterResolver注入IApplication参数+AfterAll清理
- **JUnit4集成**：`@RunWith(YMPJUnit4ClassRunner.class)` 兼容老项目，Runner内部调用YMPTestUtils统一初始化逻辑
- **套件组合测试**：`@YMPJUnit5Suite({A.class,B.class})` / `@RunWith(YMPJUnit4Suite.class)+@SuiteClasses` 多测试类共享同一个YMP应用
- **Mock WebMVC请求**：`MockWebRequestHelper.create(webmvc).get/post(path).parameter(k,v).doFilter()` 走DispatcherServlet真实流程，返回MockHttpServletResponse断言status+body
- **YMPTestUtils底层API**：`initializeYMP(testClass)` / `InitConfig` 设置主类名/测试类列表/SystemProperties，可手动初始化

## 2. 核心注解/类速查表（全限定名）

| 类/注解 | 全限定名 | 核心作用 |
|---|---|---|
| YMPJUnit5Extension | `net.ymate.platform.test.YMPJUnit5Extension` | JUnit5扩展，@ExtendWith(Class)引用，实现TestInstanceFactory/ParameterResolver/AfterAllCallback |
| @YMPJUnit5Suite | `net.ymate.platform.test.YMPJUnit5Suite` | JUnit5套件注解，value=Class[]为测试类列表；整个Suite初始化一次YMP应用 |
| YMPJUnit5SuiteExtension | `net.ymate.platform.test.YMPJUnit5SuiteExtension` | 套件内部扩展（配合@YMPJUnit5Suite使用，一般不显式引用） |
| YMPJUnit4ClassRunner | `net.ymate.platform.test.YMPJUnit4ClassRunner` | JUnit4 Runner，@RunWith(YMPJUnit4ClassRunner.class)引用 |
| YMPJUnit4Suite | `net.ymate.platform.test.YMPJUnit4Suite` | JUnit4套件Runner：@RunWith(YMPJUnit4Suite.class)+@SuiteClasses({A.class,B.class}) |
| YMPTestUtils | `net.ymate.platform.test.YMPTestUtils` | 底层初始化工具：initializeYMP(InitConfig) / initializeYMP(testClass) / initializeYMP(suiteClass, testClasses) |
| YMPTestUtils.InitConfig | `net.ymate.platform.test.YMPTestUtils$InitConfig` | 初始化配置Builder：setMainClassName/setTestClasses/setSystemProperties |
| MockWebRequestHelper | `net.ymate.platform.mock.MockWebRequestHelper` | Mock请求助手：create(WebMVC)→RequestBuilder→链式.parameter/header/doFilter |
| MockWebRequestHelper.RequestBuilder | `net.ymate.platform.mock.MockWebRequestHelper$RequestBuilder` | 链式Builder：get/post/put/delete(path) / parameter(k,v) / header(k,v) / doFilter()→MockHttpServletResponse |
| MockHttpServletRequest | `net.ymate.platform.mock.web.MockHttpServletRequest` | Servlet API Mock请求实现（HttpServletRequest接口），可手动构造 |
| MockHttpServletResponse | `net.ymate.platform.mock.web.MockHttpServletResponse` | Servlet API Mock响应实现：getStatus()/getContentAsString()/getHeader()等 |
| @Inject（复用core） | `net.ymate.platform.core.beans.annotation.Inject` | 测试类字段注入Bean（WebMVC/JDBC/自定义Service等） |
| @EnableAutoScan / @EnableBeanProxy / @EnableDevMode | `net.ymate.platform.core.annotation.*` | 启动类/测试类上启用扫描/AOP/开发模式（与正式启动类一致） |
| JsonWrapper | `net.ymate.platform.commons.json.JsonWrapper` | JSON工具：JsonWrapper.fromJson(response.getContentAsString())解析响应body |

## 3. 核心API速查（≤8条最常用）

- `@ExtendWith(YMPJUnit5Extension.class)` （JUnit5类上注解）：自动初始化YMP+支持@Inject
- `@RunWith(YMPJUnit4ClassRunner.class)` （JUnit4类上注解）：同上，兼容JUnit4
- `@Inject WebMVC webmvc / @Inject JDBC database`：测试字段注入框架模块或自定义Bean
- `MockWebRequestHelper.create(webmvc).post("/login").parameter("uname","admin").parameter("passwd","xxx").doFilter()` → `MockHttpServletResponse`：模拟HTTP请求
- `response.getStatus()` → `int`：断言HTTP状态码（HttpServletResponse.SC_OK=200）
- `response.getContentAsString()` → `String`：获取响应体字符串，JsonWrapper.fromJson()解析
- `YMPTestUtils.initializeYMP(YMPTestUtils.InitConfig cfg)` → `IApplication`：手动初始化应用（非常规场景）
- `@YMPJUnit5Suite({LoginTest.class, OrderTest.class})` + @EnableAutoScan：JUnit5套件组合测试一次启动

## 4. 标准代码模板

### 模板1：JUnit5 @ExtendWith(YMPJUnit5Extension) + @Inject注入 + @Test基本断言

```java
/*
 * Copyright 2007-present the original author or authors.
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
package com.example.test;

import net.ymate.platform.core.annotation.EnableAutoScan;
import net.ymate.platform.core.annotation.EnableBeanProxy;
import net.ymate.platform.core.annotation.EnableDevMode;
import net.ymate.platform.core.beans.annotation.Inject;
import net.ymate.platform.test.YMPJUnit5Extension;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * JUnit5基础测试示例：@ExtendWith启用扩展，@Inject注入Service，JUnit5生命周期注解+断言
 *
 * @author YMP Team
 * @since 2.1.4-dev
 */
@ExtendWith(YMPJUnit5Extension.class)
@EnableAutoScan
@EnableBeanProxy
@EnableDevMode
class JUnit5BasicTest {

    /**
     * 注入自定义业务Service（需@Bean注册或被扫描到）
     */
    @Inject
    private DemoUserService userService;

    /**
     * 也可直接注入框架模块（如JDBC/WebMVC等，需对应artifactId依赖）
     * @Inject private net.ymate.platform.persistence.jdbc.JDBC database;
     */

    @BeforeAll
    static void setUpClass() {
        // 所有测试执行前一次初始化（静态方法，JUnit5标准）
        System.out.println("@BeforeAll - 测试套件前置");
    }

    @AfterAll
    static void tearDownClass() {
        // 所有测试执行后一次清理（YMPJUnit5Extension内部已自动销毁应用容器）
        System.out.println("@AfterAll - 测试套件后置");
    }

    @BeforeEach
    void setUp() {
        // 每个@Test前执行
        Assertions.assertNotNull(userService, "注入失败，检查@EnableAutoScan包范围是否覆盖DemoUserService");
    }

    @AfterEach
    void tearDown() {
        // 每个@Test后执行
    }

    /**
     * 用例1：注入对象非空 + 业务方法返回预期
     * @since 2.1.4-dev
     */
    @Test
    @DisplayName("用户服务-创建用户正常流程")
    void testCreateUser() {
        String userId = userService.createUser("Alice", "alice@example.com");
        Assertions.assertNotNull(userId, "返回userId不应空");
        Assertions.assertTrue(userId.startsWith("U_"), "userId前缀应为U_");
    }

    /**
     * 用例2：IApplication作为测试方法参数（ParameterResolver自动注入）
     *
     * @param application YMP应用容器实例（由YMPJUnit5Extension.ParameterResolver自动注入）
     * @since 2.1.4-dev
     */
    @Test
    @DisplayName("IApplication方法参数注入验证")
    void testApplicationInjected(net.ymate.platform.core.IApplication application) {
        Assertions.assertNotNull(application);
        Assertions.assertTrue(application.isInitialized(), "应用容器必须已初始化");
        // 也可通过application.getBeanFactory().getBean()获取
        DemoUserService svc2 = application.getBeanFactory().getBean(DemoUserService.class);
        Assertions.assertSame(userService, svc2, "同一Bean在单例下应为同一引用");
    }

    /**
     * 示例业务Service（实际项目中单独放在src/main/java下，此处仅为模板演示）
     */
    @net.ymate.platform.core.beans.annotation.Bean
    public static class DemoUserService {
        public String createUser(String name, String email) {
            return "U_" + System.currentTimeMillis() + "_" + name;
        }
    }
}
```

### 模板2：MockWebRequestHelper模拟登录请求 + SC_OK断言 + JsonWrapper解析响应

```java
/*
 * Copyright 2007-present the original author or authors.
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
package com.example.test;

import net.ymate.platform.commons.json.JsonWrapper;
import net.ymate.platform.core.annotation.EnableAutoScan;
import net.ymate.platform.core.annotation.EnableBeanProxy;
import net.ymate.platform.core.annotation.EnableDevMode;
import net.ymate.platform.core.beans.annotation.Inject;
import net.ymate.platform.mock.MockWebRequestHelper;
import net.ymate.platform.mock.web.MockHttpServletResponse;
import net.ymate.platform.test.YMPJUnit5Extension;
import net.ymate.platform.webmvc.WebMVC;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.servlet.http.HttpServletResponse;

/**
 * Mock WebMVC控制器请求示例：走完整过滤器链模拟登录POST请求，断言状态码+解析JSON返回体
 *
 * @author YMP Team
 * @since 2.1.4-dev
 */
@ExtendWith(YMPJUnit5Extension.class)
@EnableAutoScan
@EnableBeanProxy
@EnableDevMode
class LoginControllerMockTest {

    /**
     * 注入WebMVC模块（MockWebRequestHelper.create必须传入此实例）
     * 需确保pom依赖了ymate-platform-webmvc
     */
    @Inject
    private WebMVC webmvc;

    @BeforeEach
    void setUp() {
        Assumptions.assumeTrue(webmvc != null, "WebMVC未初始化，跳过WebMock相关测试");
    }

    /**
     * 模拟登录控制器POST请求：参数+密码MD5+format=json，断言200+解析响应JSON
     *
     * @throws Exception Mock请求执行异常
     * @since 2.1.4-dev
     */
    @Test
    @DisplayName("登录接口-管理员正确账号密码返回成功")
    void testLoginSuccess() throws Exception {
        // 1. 构造Mock请求并执行doFilter（走真实WebMVC DispatcherServlet+拦截器链）
        MockHttpServletResponse response = MockWebRequestHelper.create(webmvc)
                .post("/login")
                .parameter("uname", "admin")
                .parameter("passwd", DigestUtils.md5Hex("admin"))
                .parameter("format", "json")
                // 可选：.header("Authorization","Bearer xxx")
                // 可选：.contentType("application/x-www-form-urlencoded")
                .doFilter();

        // 2. 断言HTTP状态码=200
        Assertions.assertEquals(HttpServletResponse.SC_OK, response.getStatus(),
                "登录接口HTTP状态应为200，响应body=" + response.getContentAsString());

        // 3. 解析JSON响应（假设接口返回{ret:0, msg:"ok", data:{token:"xxx"}}）
        String body = response.getContentAsString();
        Assertions.assertNotNull(body, "响应体不应空");
        JsonWrapper jsonWrapper = JsonWrapper.fromJson(body);
        Assertions.assertNotNull(jsonWrapper, "响应JSON解析失败：" + body);
        // 打印格式化JSON便于调试
        System.out.println("登录接口响应JSON:\n" + jsonWrapper.getAsJsonObject().toString(true, true));

        // 4. 业务断言（根据实际接口字段调整）
        int ret = jsonWrapper.getAsJsonObject().getInt("ret", -1);
        Assertions.assertEquals(0, ret, "业务ret=0表示成功");
        String token = jsonWrapper.getAsJsonObject().getAsJsonObject("data").getString("token");
        Assertions.assertNotNull(token, "登录成功应返回token");
    }

    /**
     * 模拟登录失败：错误密码应返回ret非0
     *
     * @throws Exception 异常
     * @since 2.1.4-dev
     */
    @Test
    @DisplayName("登录接口-错误密码返回业务失败")
    void testLoginFail() throws Exception {
        MockHttpServletResponse response = MockWebRequestHelper.create(webmvc)
                .post("/login")
                .parameter("uname", "admin")
                .parameter("passwd", DigestUtils.md5Hex("wrong_password"))
                .parameter("format", "json")
                .doFilter();
        Assertions.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        JsonWrapper jsonWrapper = JsonWrapper.fromJson(response.getContentAsString());
        int ret = jsonWrapper.getAsJsonObject().getInt("ret", -1);
        Assertions.assertNotEquals(0, ret, "错误密码应返回非0业务码");
    }
}

/*
需要的pom依赖片段（除ymate-platform-test外）：
<dependencies>
    <!-- JUnit 5 (任选其一JUnit版本) -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter-api</artifactId>
        <version>5.9.2</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter-engine</artifactId>
        <version>5.9.2</version>
        <scope>test</scope>
    </dependency>
    <!-- JUnit 4 (兼容老项目，二选一)
    <dependency>
        <groupId>junit</groupId>
        <artifactId>junit</artifactId>
        <version>4.13.2</version>
        <scope>test</scope>
    </dependency> -->
    <!-- Commons Codec (DigestUtils.md5Hex示例依赖，若已存在可略) -->
    <dependency>
        <groupId>commons-codec</groupId>
        <artifactId>commons-codec</artifactId>
        <version>1.15</version>
    </dependency>
</dependencies>
*/
```

## 5. 配置速查

### 5.1 配置文件最常改项（≤12条 key|默认值|说明）

| 配置/依赖项 | 默认值/推荐 | 说明 |
|---|---|---|
| junit-jupiter-api version | 5.9.x | JUnit5 API依赖（scope=test） |
| junit-jupiter-engine version | 5.9.x | JUnit5运行引擎（Surefire/Maven Failsafe需匹配） |
| junit:junit version | 4.13.2 | JUnit4兼容包（新项目推荐JUnit5） |
| @EnableDevMode在测试类 | 必加 | 开开发模式便于定位，生产勿加 |
| @EnableAutoScan value | 测试类所在包 | 需覆盖src/main下被测试Bean的包，否则@Inject失败 |
| @EnableBeanProxy | 按需 | 测试AOP拦截/事务时必加 |
| ymp-conf.properties放置 | src/test/resources | 测试环境独立配置：测试DB/缓存/登录白名单等 |
| ymate-platform-webmvc artifactId | 依赖 | MockWebRequestHelper需要WebMVC模块 |
| ymate-platform-persistence-jdbc artifactId | 依赖 | 集成测试JDBC/JPA Repository时需要 |
| ymate-platform-commons (JsonWrapper) | 传递依赖 | 一般由webmvc/jdbc传递引入 |

### 5.2 注解配置核心参数

| 注解/方法参数 | 类型 | 说明 |
|---|---|---|
| @ExtendWith.value | Class<? extends Extension> | 固定填YMPJUnit5Extension.class |
| @RunWith.value | Class<? extends Runner> | JUnit4固定填YMPJUnit4ClassRunner.class |
| @YMPJUnit5Suite.value | Class<?>[] | 套件包含的测试类数组，顺序执行 |
| @SuiteClasses.value | Class<?>[] | JUnit4套件包含的测试类数组（org.junit.runners.Suite.SuiteClasses） |
| MockWebRequestHelper.create(WebMVC webmvc) | WebMVC实例 | 必须通过@Inject注入的WebMVC模块实例（非手动new） |
| RequestBuilder.post/get/put/delete(path) | String path | 控制器@RequestMapping路径，包含contextPath部分 |
| RequestBuilder.parameter(name, value) | String, String | 表单参数（多次调用叠加）；文件上传需MockMultipartFile（高级） |
| RequestBuilder.header(name, value) | String, String | HTTP请求头（如Authorization/Content-Type） |
| RequestBuilder.doFilter() | MockHttpServletResponse | 执行请求并返回响应（阻塞式，同步等待MVC处理完） |
| YMPTestUtils.InitConfig.setMainClassName(String) | String | 设置IApplication.SYSTEM_MAIN_CLASS（默认读取测试类所在包） |
| YMPTestUtils.InitConfig.setSystemProperties(Map) | Map<String,String> | 启动前注入System属性（替代-D参数） |

## 6. 常见坑点排查

| 现象 | 可能原因 | 排查/修复 |
|---|---|---|
| @Inject字段为null/NPE | 测试类未加@ExtendWith/@RunWith；测试类所在包未被@EnableAutoScan扫描；目标Bean缺少@Bean注解 | 确认类头注解存在；@EnableAutoScan(value={"com.example"})扩大包范围；被测试类加@Bean或被同包其它@Bean依赖触发扫描 |
| MockWebRequestHelper.doFilter()无响应/空指针/报WebMVC未初始化 | 未引入ymate-platform-webmvc；webmvc字段为null（inject失败）；yml-conf.properties缺WebMVC配置 | pom加webmvc依赖；Assertions.assertNotNull(webmvc)；ymp.configs.webmvc.enabled=true（默认true） |
| JUnit4 vs JUnit5注解混用冲突导致@Test不执行 | 包导入错误：导入org.junit.Test（JUnit4）却用@ExtendWith（JUnit5） | 统一：JUnit5→org.junit.jupiter.api.Test；JUnit4→org.junit.Test+@RunWith。不要在同一项目跨版本，避免Surefire双引擎 |
| Mock请求报404 SC_NOT_FOUND | post("/login")路径与@RequestMapping不一致；contextPath前缀是否多余；控制器未@Controller且未被扫描 | 先单独启动正式服务访问接口路径确认；@EnableAutoScan必须覆盖控制器所在包；必要时用webmvc.getOwner().getBeanFactory().getBean(LoginController.class)确认控制器已注册 |
| JsonWrapper.fromJson抛解析异常/响应body为HTML | format参数未传json；全局异常堆栈；Mock响应为404/500错误页 | .parameter("format","json")；response.getStatus()优先断言；body以text/html开头说明走了错误页→检查response.getContentAsString()中的异常栈定位 |
| 重复初始化应用/测试间互相干扰（Suite失败但单独通过） | 未用Suite却手动初始化多次；static资源未清理；多个@ExtendWith同时启用 | 组合测试用@YMPJUnit5Suite或@RunWith(YMPJUnit4Suite.class)统一共享；避免@BeforeAll写静态全局状态；扩展内置WeakReference已尽力防泄漏 |
| 静态资源找不到/cfgs/*.properties | src/test/resources未放测试配置；classpath优先级 | 测试配置放src/test/resources优先于main；或YMPTestUtils.InitConfig.setSystemProperties设置ymp.configFile指向绝对路径 |
