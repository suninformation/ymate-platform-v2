---
sidebar_position: 12
slug: test
---

# 单元测试工具包（Test）

YMP 框架的单元测试工具包，集成了 JUnit 5 和 JUnit 4 的测试开发支持，分别提供了对应的单测、套件扩展类及专属注解与使用方式，封装了核心工具类 YMPTestUtils 统一管理应用初始化逻辑，同时给出了两类 JUnit 版本下模拟控制器请求、存储器接口调用和组合单元测试的具体使用示例，整体支持依赖注入、测试生命周期管理、Bean 工厂注册等功能。


## Maven包依赖

```xml
<dependency>
    <groupId>net.ymate.platform</groupId>
    <artifactId>ymate-platform-test</artifactId>
    <version>2.1.4-dev</version>
</dependency>
```


## 功能说明

### JUnit 5 支持

#### YMPJUnit5Extension

**功能特点：**
- 实现了 `TestInstanceFactory` 接口，支持通过 YMP 容器创建测试实例
- 实现了 `ParameterResolver` 接口，支持在测试方法中注入 `IApplication` 参数
- 实现了 `AfterAllCallback` 接口，支持测试完成后清理缓存
- 使用 `WeakReference` 管理应用缓存，避免内存泄漏
- 自动注册测试类到 YMP 的 Bean 工厂，支持依赖注入

**使用方法：**
- 通过 `@ExtendWith(YMPJUnit5Extension.class)` 注解启用
- 支持标准的 JUnit 5 生命周期注解（`@BeforeAll`、`@AfterAll`、`@BeforeEach`、`@AfterEach`）
- 支持 `@Inject` 注解进行依赖注入

**测试参数解析：**
```java
@ExtendWith(YMPJUnit5Extension.class)
@EnableAutoScan
public class ExampleTest {

    @Test
    public void testWithApplication(IApplication application) {
        // 直接使用注入的 IApplication 实例
        Assertions.assertNotNull(application);
    }
}
```

#### YMPJUnit5Suite

**功能特点：**
- 基于 JUnit 5 的 `@Suite` 注解，支持组合多个测试类
- 使用独立的 `YMPJUnit5SuiteExtension` 扩展，确保 YMP 应用仅初始化一次
- 支持批量注册多个测试类到 YMP 的 Bean 工厂
- 提供线程安全的初始化机制

**使用方法：**
- 通过 `@YMPJUnit5Suite({TestClass1.class, TestClass2.class})` 注解指定测试类
- 支持标准的 YMP 配置注解（`@EnableAutoScan`、`@EnableBeanProxy`、`@EnableDevMode` 等）

### JUnit 4 支持

#### YMPJUnit4ClassRunner

**功能特点：**
- 实现了 JUnit 4 的 `Runner` 接口，为单个测试类提供 YMP 集成
- 使用 `YMPTestUtils` 统一管理应用初始化，减少代码重复
- 自动初始化 YMP 应用并注册测试类到 Bean 工厂
- 支持依赖注入和测试生命周期管理

**使用方法：**
- 通过 `@RunWith(YMPJUnit4ClassRunner.class)` 注解启用
- 支持标准的 JUnit 4 生命周期注解（`@BeforeClass`、`@AfterClass`、`@Before`、`@After`）

#### YMPJUnit4Suite

**功能特点：**
- 基于 JUnit 4 的 `Suite` 运行器，支持组合多个测试类
- 使用独立的 `YMPJUnit4RunnerBuilder` 类管理测试运行，提高代码可读性
- 确保 YMP 应用在测试套件执行前正确初始化
- 支持批量注册测试类到 YMP 的 Bean 工厂

**使用方法：**
- 通过 `@RunWith(YMPJUnit4Suite.class)` 和 `@SuiteClasses({TestClass1.class, TestClass2.class})` 注解指定测试类

### 核心工具类

#### YMPTestUtils

**功能特点：**
- 统一管理 YMP 应用初始化逻辑，减少代码重复
- 提供 `InitConfig` 配置类，支持自定义应用初始化参数
- 支持为单个测试类或测试套件初始化 YMP 应用
- 优化异常处理机制，提供更详细的错误信息

**使用方法：**
```java
// 为单个测试类初始化应用
IApplication application = YMPTestUtils.initializeYMP(testClass);

// 为测试套件初始化应用
IApplication application = YMPTestUtils.initializeYMP(suiteClass, testClasses);

// 自定义初始化配置
IApplication application = YMPTestUtils.initializeYMP(new YMPTestUtils.InitConfig()
        .setMainClassName("com.example.TestMain")
        .setTestClasses(Arrays.asList(TestClass1.class, TestClass2.class))
        .setSystemProperties(Collections.singletonMap("ymate.test.env", "dev")));
```


## 示例代码

### JUnit 5 使用示例

#### 示例一：模拟控制器方法请求

```java
@ExtendWith(YMPJUnit5Extension.class)
@EnableAutoScan
@EnableBeanProxy
@EnableDevMode
public class LoginControllerTest {

    @Inject
    private WebMVC webmvc;

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
    }

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
        System.out.println(jsonWrapper.getAsJsonObject().toString(true, true));
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

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
    }

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

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
    }
}
```


### JUnit 4 使用示例

#### 示例一：模拟控制器方法请求

```java
@RunWith(YMPJUnit4ClassRunner.class)
@EnableAutoScan
@EnableBeanProxy
@EnableDevMode
public class LoginControllerTest {

    @Inject
    private WebMVC webmvc;

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

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
        System.out.println(jsonWrapper.getAsJsonObject().toString(true, true));
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

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

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

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }
}
```
