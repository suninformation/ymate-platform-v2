---
name: ymp-core
description: YMP框架核心模块，提供应用容器、依赖注入、AOP拦截、事件机制、国际化资源管理等核心功能
version: 2.1.4
author: YMP Team
category: framework
tags:
  - java
  - framework
  - core
  - di
  - aop
  - event
  - i18n
trigger: 当用户需要使用YMP框架核心功能、依赖注入、AOP、事件机制等时触发
tools:
  - dependency-injection
  - aop
  - event-management
  - i18n
examples:
  - 创建YMP应用
  - 实现依赖注入
  - 配置AOP拦截器
  - 实现事件监听
  - 使用国际化资源
---

# YMP框架核心（Core）模块技能文档

## 1. 核心功能

YMP框架核心模块是整个框架的基础，提供了应用容器、依赖注入、AOP拦截、事件机制等核心功能，主要包括：

- **应用容器（IApplication）**：负责框架初始化、模块生命周期管理、事件广播与监听等核心功能
- **自动扫描（AutoScan）**：自动扫描并注册被@Bean注解标记的类，支持自定义扫描规则
- **依赖注入（DI）**：通过@Inject和@By注解实现对象之间的依赖注入，支持自定义注入逻辑
- **拦截器（AOP）**：基于代理技术实现的方法拦截，支持前置、后置和环绕拦截
- **事件服务（Events）**：通过事件的注册、订阅和广播实现模块间的解耦，支持同步和异步模式
- **国际化资源管理（I18N）**：提供多语言支持，支持资源文件的加载和切换
- **SPI加载机制**：支持通过SPI机制加载服务实现，提供默认实现和自定义实现的优先级管理

## 2. 技术架构

核心模块采用分层设计，主要包括以下组件：

- **应用容器层**：由IApplication接口及其实现类组成，负责整体协调和管理
- **对象管理层**：由IBeanFactory接口及其实现类组成，负责对象的创建、管理和依赖注入
- **拦截器层**：由IInterceptor接口及其实现类组成，负责方法拦截和增强
- **事件层**：由Events类和IEventListener接口组成，负责事件的注册、触发和监听
- **国际化层**：由I18N接口及其实现类组成，负责国际化资源的管理
- **配置层**：负责框架和模块的配置管理，支持配置文件和注解两种方式

## 3. 核心API

### 3.1 应用容器（IApplication）

应用容器是框架的核心，提供了以下主要方法：

| 方法名 | 描述 | 参数 | 返回值 |
|-------|------|------|-------|
| initialize() | 初始化应用容器 | 无 | void |
| isInitialized() | 检查应用容器是否已初始化 | 无 | boolean |
| getBeanFactory() | 获取对象工厂 | 无 | IBeanFactory |
| getEvents() | 获取事件管理器 | 无 | Events |
| getI18n() | 获取国际化资源管理器 | 无 | I18N |
| getModuleManager() | 获取模块管理器 | 无 | ModuleManager |
| getRunEnv() | 获取当前运行环境 | 无 | Environment |
| getParam(String name) | 获取全局参数值 | name: 参数名称 | String |
| registerInterceptor(Class<? extends IInterceptor> interceptClass) | 注册拦截器 | interceptClass: 拦截器类 | void |

### 3.2 对象工厂（IBeanFactory）

对象工厂负责对象的创建、管理和依赖注入，提供了以下主要方法：

| 方法名 | 描述 | 参数 | 返回值 |
|-------|------|------|-------|
| getBean(Class<T> clazz) | 获取指定类型的对象实例 | clazz: 目标类型 | T |
| getBeans() | 获取当前工厂管理的所有类对象映射 | 无 | Map<Class<?>, BeanMeta> |
| registerBean(Class<?> clazz) | 注册一个类到工厂 | clazz: 预注册类型 | void |
| registerInjector(Class<? extends Annotation> annClass, IBeanInjector injector) | 注册自定义依赖注入注解的逻辑处理器 | annClass: 目标注解类型<br>injector: 目标依赖注入注解逻辑处理器 | void |
| getProxyFactory() | 获取代理工厂 | 无 | IProxyFactory |

### 3.3 事件管理器（Events）

事件管理器负责事件的注册、触发和监听，提供了以下主要方法：

| 方法名 | 描述 | 参数 | 返回值 |
|-------|------|------|-------|
| registerListener(Class<? extends IEvent> eventClass, IEventListener<?> listener) | 注册事件监听器 | eventClass: 事件类型<br>listener: 事件监听器 | void |
| registerListener(Events.MODE mode, Class<? extends IEvent> eventClass, IEventListener<?> listener) | 注册事件监听器，指定模式 | mode: 事件处理模式<br>eventClass: 事件类型<br>listener: 事件监听器 | void |
| fireEvent(IEvent event) | 触发事件 | event: 事件对象 | void |
| registerEvent(Class<? extends IEvent> eventClass) | 注册自定义事件 | eventClass: 事件类型 | void |

### 3.4 拦截器（IInterceptor）

拦截器用于方法的拦截和增强，提供了以下主要方法：

| 方法名 | 描述 | 参数 | 返回值 |
|-------|------|------|-------|
| intercept(InterceptContext context) | 执行拦截逻辑 | context: 拦截上下文 | Object |

### 3.5 国际化资源管理器（I18N）

国际化资源管理器负责多语言资源的管理，提供了以下主要方法：

| 方法名 | 描述 | 参数 | 返回值 |
|-------|------|------|-------|
| get(String key) | 获取指定键的国际化资源 | key: 资源键 | String |
| get(String key, Locale locale) | 获取指定键和语言的国际化资源 | key: 资源键<br>locale: 语言 | String |
| get(String key, Object... params) | 获取指定键并格式化的国际化资源 | key: 资源键<br>params: 格式化参数 | String |

## 4. 配置与部署

### 4.1 配置文件

YMP框架默认使用`ymp-conf.properties`配置文件，支持根据环境加载不同的配置文件：

- `-Dymp.env=test`：测试环境，优先加载`ymp-conf_TEST.properties`
- `-Dymp.env=dev`：开发环境，优先加载`ymp-conf_DEV.properties`
- `-Dymp.env=product`：生产环境，优先加载`ymp-conf_PRODUCT.properties`

主要配置项包括：

| 配置项 | 描述 | 默认值 |
|-------|------|-------|
| ymp.dev_mode | 是否为开发模式 | false |
| ymp.packages | 框架自动扫描的包名称集合 | net.ymate.platform |
| ymp.excluded_packages | 排除包名称集合 | 无 |
| ymp.default_locale | 国际化资源默认语言设置 | 系统环境语言 |
| ymp.configs.event.default_mode | 默认事件触发模式 | ASYNC |
| ymp.configs.event.thread_pool_size | 初始化线程池大小 | Runtime.getRuntime().availableProcessors() |

### 4.2 注解配置

从2.1.0版本开始，YMP框架支持通过注解进行配置，主要注解包括：

| 注解 | 描述 | 主要参数 |
|------|------|---------|
| @EnableAutoScan | 开启自动扫描 | value: 扫描包路径<br>excluded: 排除包路径 |
| @EnableBeanProxy | 开启代理 | factoryClass: 代理工厂类型 |
| @EnableDevMode | 开启开发模式 | 无 |
| @EventsConf | 事件配置 | mode: 事件处理模式<br>threadPoolSize: 线程池大小 |
| @I18nConf | 国际化配置 | defaultLocale: 默认语言 |
| @Params | 自定义参数 | value: 参数数组 |

### 4.3 部署方式

核心模块作为框架的基础，无需单独部署，而是作为其他模块的依赖存在。在Maven项目中，通过以下依赖引入：

```xml
<dependency>
    <groupId>net.ymate.platform</groupId>
    <artifactId>ymate-platform-core</artifactId>
    <version>2.1.4-dev</version>
</dependency>
```

## 5. 使用指南

### 5.1 框架初始化

#### 基于配置文件初始化

```java
public class Starter {
    public static void main(String[] args) throws Exception {
        try (IApplication application = YMP.run(args)) {
            if (application.isInitialized()) {
                System.out.println("YMP框架初始化成功！");
            }
        }
    }
}
```

#### 基于注解初始化

```java
@EnableAutoScan
@EnableBeanProxy
@EnableDevMode
@EventsConf(mode = Events.MODE.NORMAL, threadPoolSize = 200)
@I18nConf(defaultLocale = "zh_CN")
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

### 5.2 自动扫描与依赖注入

#### 定义Bean

```java
@Bean
public class UserService implements IUserService {
    @Override
    public String getUserName(String userId) {
        return "User: " + userId;
    }
}
```

#### 依赖注入

```java
@Bean
public class UserController {
    @Inject
    private IUserService userService;
    public String getUserInfo(String userId) {
        return userService.getUserName(userId);
    }
}
```

### 5.3 拦截器使用

#### 定义拦截器

```java
@Interceptor
public class LogInterceptor extends AbstractInterceptor {
    @Override
    protected Object before(InterceptContext context) throws InterceptException {
        System.out.println("方法执行前：" + context.getTargetMethod().getName());
        return null;
    }
    @Override
    protected Object after(InterceptContext context) throws InterceptException {
        System.out.println("方法执行后：" + context.getTargetMethod().getName());
        return null;
    }
}
```

#### 使用拦截器

```java
@Bean
public class UserService {
    @Before(LogInterceptor.class)
    public String getUserName(String userId) {
        return "User: " + userId;
    }
}
```

### 5.4 事件使用

#### 定义事件

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
```

#### 订阅事件

```java
@EventListener(value = UserEvent.class)
public class UserEventListener implements IEventListener<UserEvent> {
    @Override
    public boolean handle(UserEvent context) {
        switch (context.getEventName()) {
            case USER_CREATED:
                System.out.println("用户创建事件触发");
                break;
            case USER_UPDATED:
                System.out.println("用户更新事件触发");
                break;
            case USER_DELETED:
                System.out.println("用户删除事件触发");
                break;
        }
        return false;
    }
}
```

#### 触发事件

```java
try (IApplication application = YMP.run(args)) {
    Events events = application.getEvents();
    UserEvent event = new UserEvent(application, UserEvent.EVENT.USER_CREATED);
    events.fireEvent(event);
}
```

### 5.5 国际化使用

#### 资源文件

在resources目录下创建i18n目录，并添加资源文件：

- messages.properties (默认语言)
- messages_zh_CN.properties (中文)
- messages_en_US.properties (英文)

#### 使用国际化资源

```java
try (IApplication application = YMP.run(args)) {
    I18N i18n = application.getI18n();
    String message = i18n.get("user.not.found", "zh_CN");
    System.out.println(message);
}
```

## 6. 最佳实践

### 6.1 应用容器使用

- **推荐使用try-with-resources语法**：确保应用容器正确关闭，释放资源
- **合理配置扫描路径**：避免扫描过多无关包，提高启动速度
- **使用开发模式**：在开发环境中开启开发模式，获得更详细的日志信息

### 6.2 依赖注入

- **优先使用接口注入**：通过接口类型注入，而非具体实现类
- **避免循环依赖**：设计时注意避免循环依赖，否则会导致初始化失败
- **合理使用@By注解**：当多个实现类时，使用@By注解指定具体实现

### 6.3 拦截器使用

- **合理设计拦截器**：拦截器逻辑应简洁，避免耗时操作
- **使用环绕拦截器**：对于需要同时处理前置和后置逻辑的场景，使用@Around注解
- **注意拦截顺序**：多个拦截器的执行顺序是按照声明顺序

### 6.4 事件使用

- **选择合适的事件模式**：对于耗时操作，推荐使用异步模式
- **合理设计事件结构**：事件对象应包含必要的上下文信息
- **避免事件风暴**：避免在事件处理中触发新的事件，导致事件风暴

### 6.5 性能优化

- **减少反射使用**：反射操作较慢，应避免频繁使用
- **合理使用缓存**：对于频繁访问的数据，使用缓存减少计算
- **优化线程池配置**：根据实际需求调整事件线程池大小

## 7. 常见问题与解决方案

### 7.1 初始化失败

**问题**：框架初始化失败，抛出异常

**原因**：可能是配置错误、依赖冲突或循环依赖

**解决方案**：
- 检查配置文件是否正确
- 检查依赖是否冲突，使用mvn dependency:tree查看依赖树
- 检查是否存在循环依赖

### 7.2 依赖注入失败

**问题**：@Inject注解标记的字段为null

**原因**：可能是类未被@Bean注解标记，或未被自动扫描到

**解决方案**：
- 确保类被@Bean注解标记
- 确保类所在包被包含在扫描路径中
- 检查是否开启了自动扫描

### 7.3 拦截器不生效

**问题**：拦截器逻辑未执行

**原因**：可能是未开启代理，或方法不满足拦截条件

**解决方案**：
- 确保使用@EnableBeanProxy开启了代理
- 确保方法是public的，非public方法不会被拦截
- 确保方法不是Object类的方法，Object类方法不会被拦截

### 7.4 事件监听不生效

**问题**：事件监听器未接收到事件

**原因**：可能是事件未注册，或监听器未注册

**解决方案**：
- 确保事件已通过registerEvent方法注册
- 确保监听器已通过@EventListener注解或registerListener方法注册
- 检查事件触发代码是否正确

### 7.5 国际化资源未加载

**问题**：国际化资源未加载，返回键名而非值

**原因**：可能是资源文件路径错误，或资源文件未打包到jar中

**解决方案**：
- 确保资源文件放在正确的路径下（resources/i18n/）
- 确保资源文件已正确打包到jar中
- 检查资源文件编码是否正确（推荐UTF-8）

## 8. 代码示例

### 8.1 完整应用示例

```java
@EnableAutoScan("com.example")
@EnableBeanProxy
@EnableDevMode
@EventsConf(mode = Events.MODE.ASYNC, threadPoolSize = 10)
@I18nConf(defaultLocale = "zh_CN")
public class Application {
    static {
        System.setProperty(IApplication.SYSTEM_MAIN_CLASS, Application.class.getName());
    }
    public static void main(String[] args) throws Exception {
        try (IApplication application = YMP.run(args)) {
            if (application.isInitialized()) {
                // 获取Bean
                UserService userService = application.getBeanFactory().getBean(UserService.class);
                // 调用方法
                String result = userService.getUserName("123");
                System.out.println(result);
                // 触发事件
                Events events = application.getEvents();
                UserEvent event = new UserEvent(application, UserEvent.EVENT.USER_CREATED);
                events.fireEvent(event);
                // 使用国际化
                I18N i18n = application.getI18n();
                String message = i18n.get("welcome.message");
                System.out.println(message);
            }
        }
    }
}
```

### 8.2 自定义模块示例

```java
// 模块接口
@Ignored
public interface IDemoModule extends IInitialization<IApplication>, IDestroyable {
    String MODULE_NAME = "module.demo";
    IApplication getOwner();
    IDemoConfig getConfig();
    String sayHello(String name);
}

// 模块实现
public class DemoModule implements IDemoModule {
    private IApplication owner;
    private IDemoConfig config;
    @Override
    public void initialize(IApplication owner) throws Exception {
        this.owner = owner;
        this.config = new DefaultDemoConfig(owner);
        System.out.println("DemoModule initialized");
    }
    @Override
    public String sayHello(String name) {
        return "Hello, " + name + "!";
    }
    @Override
    public IApplication getOwner() {
        return owner;
    }
    @Override
    public IDemoConfig getConfig() {
        return config;
    }
    @Override
    public void destroy() throws Exception {
        System.out.println("DemoModule destroyed");
    }
}
```

## 9. 总结

YMP框架核心模块提供了一套完整的企业级应用开发基础设施，包括应用容器、依赖注入、AOP拦截、事件机制和国际化支持等核心功能。通过简洁的API设计和灵活的配置方式，使得开发者可以专注于业务逻辑的实现，而无需关心底层基础设施的构建。

核心模块的设计理念是"约定优于配置"，通过合理的默认值和注解配置，减少了繁琐的XML配置，提高了开发效率。同时，通过SPI机制和模块化设计，使得框架具有良好的扩展性和可维护性。

在实际应用中，开发者应根据具体需求合理使用核心模块提供的功能，遵循最佳实践，以获得最佳的性能和可维护性。
