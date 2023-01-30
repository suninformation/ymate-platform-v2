---
sidebar_position: 2
slug: core
---

# 核心（Core）

YMP 框架主要是由核心（Core）和若干模块（Module）组成，核心也称之为应用容器（IApplication），主要负责框架初始化、事件（Events）广播与监听、模块的定义及其生命周期管理、国际化资源管理（I18N）和类对象管理等，其核心功能是对包和类的自动扫描（AutoScan）、对象的生命周期管理、以及反转控制（IoC）、依赖注入（DI）和方法拦截（AOP）等。



## Maven包依赖

```xml
<dependency>
    <groupId>net.ymate.platform</groupId>
    <artifactId>ymate-platform-core</artifactId>
    <version>2.1.2</version>
</dependency>
```



## 框架初始化

YMP 框架的初始化方式有两种，一种是配置文件方式，另一种则是基于注解。我们更推荐两者配合使用，当两者共存时，配置文件方式更优先于注解方式（即，当注解和配置文件中对相同配置项赋值时，则优先使用配置文件中的非空值）。



### 基于配置文件初始化

YMP 框架默认使用名称为 `ymp-conf.properties` 的配置文件 ，默认该文件放置在 `classpath` 的根路径下，也可以通过 `JVM` 启动参数 `-Dymp.configFile` 指定具体配置文件路径。

具体加载流程如下：

- 首先，检查JVM启动参数 `-Dymp.env` 判断当前系统远行环境（默认值：`dev`）：
  + `-Dymp.env=test`：测试环境，将优先加载 `ymp-conf_TEST.properties`
  + `-Dymp.env=dev`：开发环境，将优先加载 `ymp-conf_DEV.properties`
  + `-Dymp.env=product`：生产环境，将优先加载 `ymp-conf_PRODUCT.properties`
- 若以上配置文件未找到，则尝试加载与当前操作系统类型相匹配的配置文件：
    + Unix/Linux环境下，优先加载 `ymp-conf_UNIX.properties`
    + Windows环境下，优先加载 `ymp-conf_WIN.properties`
- 若以上配置文件未找到，则尝试加载 `ymp-conf.properties` 默认配置文件。



**示例代码：**


```java
public class Starter {

    private static final Log LOG = LogFactory.getLog(Starter.class);

    public static void main(String[] args) throws Exception {
        try (IApplication application = YMP.run(args)) {
            if (application.isInitialized()) {
                LOG.info("Everything depends on ability!  -- YMP :)");
            }
        }
    }
}
```

上述代码中通过 `YMP.run()` 方法构建了一个 `IApplication` 接口实例对象，而这个 `IApplication` 对象就是框架应用容器。



### 基于注解初始化

YMP 框架从 `2.1.0` 版本开始支持通过注解针对各个模块进行初始化配置，项目中允许包括多个基于注解的配置类，并支持通过 `JVM` 启动参数 `-Dymp.mainClass`  任意切换使其生效，方便根据自身业务特点灵活调配。



**示例代码：**


```java
@EnableAutoScan
@EnableBeanProxy
@EnableDevMode
@DefaultPasswordProcessClass
@EventsConf(mode = Events.MODE.NORMAL, threadPoolSize = 200)
@I18nConf(defaultLocale = "zh_CN")
@LogConf(allowConsoleOutput = true, configFile = "${user.dir}/cfgs/log4j.xml")
@ConfigurationConf(configHome = "${user.dir}/configs", checkTimeInterval = 300000)
public class Starter {

    static {
        System.setProperty(IApplication.SYSTEM_MAIN_CLASS, Starter.class.getName());
    }

    private static final Log LOG = LogFactory.getLog(Starter.class);

    public static void main(String[] args) throws Exception {
        try (IApplication application = YMP.run(args)) {
            if (application.isInitialized()) {
                LOG.info("Everything depends on ability!  -- YMP :)");
            }
        }
    }
}
```



### 应用容器扩展初始化处理器接口（IApplicationInitializer）

为了让开发者可以对应用容器有更多的可定制、扩展能力，框架提供了应用容器扩展初始化处理器（`IApplicationInitializer`） 接口，其通过`SPI`方式在框架启动时自动扫描类和包路径下 `META-INF/services/`或 `META-INF/services/internal/` 目录中所有名称为 `net.ymate.platform.core.IApplicationInitializer` 的文件并加载文件中指定的所有接口实现类。

该接口可以帮助你完成如下操作：

| 方法名称                | 描述                                       |
| ----------------------- | ------------------------------------------ |
| afterEventInit          | 当事件管理器初始化完毕后将调用此方法       |
| beforeBeanLoad          | 当对象加载器开始执行加载动作前将调用此方法 |
| beforeModuleManagerInit | 当模块管理器执行初始化动作前将调用此方法   |
| beforeBeanFactoryInit   | 当对象工厂执行初始化动作前将调用此方法     |



### 自定义Banner

以 Maven 工程为例，在 `src/main/resources` 路径下新建名称为 `banner.txt` 的文本文件，该文件编译后将被放置在 `classes` 目录下，框架在启动时会优先加载该文件内容，若文件不存在或文件内容为空时，将默认输出如下内容：

```shell
__  __ __  ___ ___
\ \/ //  |/  // _ \
 \  // /|_/ // ___/
 /_//_/  /_//_/  www.ymate.net
```



## 基本配置

YMP框架从 `2.1.0` 版本开始遵循零配置原则，在不提供配置文件的情况下，尽可能使用默认配置以保证各个模块能够正确初始化。



### 配置文件参数说明

```properties
#-------------------------------------
# 框架基本配置参数
#-------------------------------------

# 是否为开发模式, 默认值: false
ymp.dev_mode=

# 框架自动扫描的包名称集合, 多个包名之间用'|'分隔, 默认已包含net.ymate.platform包, 其子包也将被扫描
ymp.packages=

# 排除包名称集合, 多个包名之间用'|'分隔, 被包含在包路径下的类文件在扫描过程中将被忽略
ymp.excluded_packages=

# 排除包文件名称集合, 多个文件名称之间用'|'分隔, 被包含的JAR或ZIP文件在扫描过程中将被忽略
ymp.excluded_files=

# 排除模块类名集合, 多个模块类名之间用'|'分隔, 被包含的模块在加载过程中将被忽略
ymp.excluded_modules=

# 包含模块名集合, 多个模块类名之间用'|'分隔，若设置该参数则框架初始化时仅加载被包含的模块
ymp.included_modules=

# 国际化资源默认语言设置, 默认采用系统环境语言
ymp.default_locale=zh_CN

# 国际化资源事件监听处理器, 默认值: 空
ymp.i18n_event_handler_class=

# 默认密码处理器, 默认值: 空
ymp.default_password_process_class=

# 自定义扩展参数, xxx表示自定义参数名称, vvv表示参数值
ymp.params.xxx=vvv

#-------------------------------------
# 框架事件初始化参数
#-------------------------------------

# 默认事件触发模式, 取值范围: NORMAL-同步执行, ASYNC-异步执行, 默认值: ASYNC
ymp.configs.event.default_mode=

# 事件管理提供者接口实现, 默认值: net.ymate.platform.core.event.impl.DefaultEventProvider
ymp.configs.event.provider_class=

# 初始化线程池大小, 默认值: Runtime.getRuntime().availableProcessors()
ymp.configs.event.thread_pool_size=

# 最大线程池大小, 默认值: 200
ymp.configs.event.thread_max_pool_size=

# 线程队列大小, 默认值: 1024
ymp.configs.event.thread_queue_size=
```



### 配置注解参数说明

以下所列注解仅用于 YMP 框架基本配置，关于其它模块所提供的注解和配置项说明，请参见各模块文档。



#### @EnableAutoScan 

用于开启包和类的自动扫描特性，默认情况下框架在启动时不会进行自动扫描。

| 配置项        | 描述             |
| --------------- | ------------------------------------------------------------ |
| value           | 自动扫描的包名称集合，默认已包含主程序类所在包。      |
| excluded        | 排除包名称集合, 被包含在包路径下的类文件在扫描过程中将被忽略。 |
| excludedFiles   | 排除包文件名称集合, 被包含的JAR或ZIP文件在扫描过程中将被忽略。 |
| excludedModules | 排除模块类名集合, 被包含的模块在加载过程中将被忽略。          |
| includedModules | 包含模块类名集合，若设置该参数则框架初始化时仅加载被包含的模块 |
| factoryClass | 对象加载器工厂类型，默认值：`DefaultBeanLoadFactory` |

**示例：**

```java
@EnableAutoScan(
        value = {"net.ymate.module", "com.myproject.demo"},
        excluded = "net.ymate.module.captcha",
        factoryClass = DemoBeanLoadFactory.class)
```



#### @EnableBeanProxy

用于开启代理并配置代理工厂类型，框架提供了三种代理工厂接口实现类：

- `DefaultProxyFactory.class`：基于 `CGLIB` 实现的默认代理工厂。
- `JavassistProxyFactory`：基于  `Javassist`  实现的代理工厂。
- `NoOpProxyFactory`：空操作代理工厂，使用它表示需要禁用框架的 `AOP` 特性。

| 配置项       | 描述                                        |
| ------------ | ------------------------------------------- |
| factoryClass | 代理工厂类型，默认值：`DefaultProxyFactory` |

**示例：**

```java
@EnableBeanProxy(factoryClass = JavassistProxyFactory.class)
```



#### @EnableDevMode

开启开发模式，无参数，与 `ymp.dev_mode=true` 作用相同，优先级低于 `JVM` 启动参数 `-Dymp.env`。



#### @EventConf

框架事件初始化参数。

| 配置项            | 描述                                                         |
| ----------------- | ------------------------------------------------------------ |
| mode              | 取值范围: `NORMAL`-同步执行, `ASYNC`-异步执行, 默认值: `ASYNC` |
| providerClass     | 事件管理提供者接口实现，默认值：`DefaultEventProvider`       |
| threadPoolSize    | 初始化线程池大小，默认值：`Runtime.getRuntime().availableProcessors()` |
| threadMaxPoolSize | 最大线程池大小，默认值：`200`                                |
| threadQueueSize   | 线程队列大小，默认值：`1024`                                 |

**示例：**

```java
@EventsConf(mode = Events.MODE.NORMAL,
        threadPoolSize = 10,
        threadMaxPoolSize = 200,
        threadQueueSize = 1000)
```



#### @I18nConf

国际化初始化参数。

| 配置项            | 描述                                           |
| ----------------- | ---------------------------------------------- |
| defaultLocale     | 国际化资源默认语言设置, 默认采用系统环境语言。 |
| eventHandlerClass | 国际化资源事件监听处理器。                     |

**示例：**

```java
@I18nConf(defaultLocale = "zh_CN", eventHandlerClass = I18nWebEventHandler.class)
```



#### @DefaultPaswordProcessClass

设置框架默认密码处理器，与 `ymp.default_password_process_class`作用相同。

| 配置项 | 描述                                                   |
| ------ | ------------------------------------------------------ |
| value  | 默认密码处理器类型，默认值：`DefaultPasswordProcessor` |

**示例：**

```java
@DefaultPasswordProcessClass(DemoPasswordProcessor.class)
```



#### @Params

#### @Param

自定义扩展参数，与 `ymp.params.xxx=vvv`作用相同。

| 配置项 | 描述     |
| ------ | -------- |
| name   | 参数名称 |
| value  | 值       |

**示例：**

```java
@Params({@Param(name = "xxx", value = "vvv")})
```



#### @ParamValue

自定义扩展参数值注入。

| 配置项 | 描述   |
| ------ |------|
| value  | 自定义扩展参数名称, 若未提供则使用成员变量或方法参数名称 |
| defaultValue  | 自定义扩展参数默认值 |
| replaceEnvVariable  | 是否替换字符串中的环境变量 |

**示例：**

```java
@ParamValue(value = "xxx", defaultValue = "${root}/a", replaceEnvVariable = true)
```



## 关于SPI加载机制

YMP 框架从 `2.1.0` 版本开始大量采用`SPI`的加载机制，允许开发者针对框架中诸多功能特性进行自定义，如新版框架中的模块就已经放弃了自动扫描，转而采用了 `SPI` 机制进行加载，更多的细节请注意各文档中的相关描述。

YMP 框架提供了两种 `SPI` 配置文件存放路径：

- `META-INF/services/internal/` ：内部配置路径用于存放默认配置，是否使用默认配置由功能特性的设计者决定。
- `META-INF/services/` ：自定义配置路径，该路径将优先于内部配置路径被加载。

在以上配置文件路径中存放的配置文件为普通文本文件，无扩展名且名称一般为接口类文件全称。



### 示例一：加载指定业务接口实例

**步骤1：** 定义 `IDemoService` 服务接口及两个实现类。

```java
package net.ymate.demo.service;

/**
 * 示例服务接口类
 */
public interface IDemoService {

    /**
     * 执行业务逻辑
     *
     * @return 返回执行结果
     */
    String doService();
}


package net.ymate.demo.service.impl;

/**
 * 示例服务接口实现类：DemoOneService
 */
public class DemoOneServiceImpl implements IDemoService{
    @Override
    public String doService() {
        return "来自 DemoOneService 的接口实现。";
    }
}

package net.ymate.demo.service.impl;

/**
 * 示例服务接口实现类：DemoTwoService
 */
public class DemoTwoServiceImpl implements IDemoService{
    @Override
    public String doService() {
        return "来自 DemoTwoService 的接口实现。";
    }
}
```

**步骤2：** 在内部配置路径 `META-INF/services/internal/` 中添加 `SPI` 配置文件，内容如下：

```shell
# more META-INF/services/internal/net.ymate.demo.service.IDemoService
net.ymate.demo.service.impl.DemoOneServiceImpl
```

**步骤3：** 加载并执行业务逻辑。

```java
public class Demo {

    public static void main(String[] args) throws Exception {
        IDemoService demoService = ClassUtils.getExtensionLoader(IDemoService.class).getExtension();
        if (demoService != null) {
            // 此处执行输出结果为：来自 DemoOneService 的接口实现。
            System.out.println(demoService.doService());
        }
    }
}
```

**步骤4：** 在自定义配置路径 `META-INF/services/` 中添加 `SPI` 配置文件，内容如下：

```shell
# more META-INF/services/net.ymate.demo.service.IDemoService
net.ymate.demo.service.impl.DemoTwoServiceImpl
```

**步骤5：** 再次加载并执行业务逻辑。

```java
public class Demo {

    public static void main(String[] args) throws Exception {
        IDemoService demoService = ClassUtils.getExtensionLoader(IDemoService.class).getExtension();
        if (demoService != null) {
            // 此处执行输出结果为：来自 DemoTwoService 的接口实现。
            System.out.println(demoService.doService());
        }
    }
}
```

通过本例可以清楚的知道，当通过 `ClassUtils.getExtensionLoader` 方法加载指定接口类的 `SPI` 配置时，其首先尝试加载自定义配置路径下的配置文件，若配置文件存在则加载并返回，否则尝试从内部配置路径中加载。



### 示例二：加载指定业务接口多实例

根据 **示例一** 的配置，通过以下示例展示如何获取业务接口所配置的全部实现类及实现类实例对象：

```java
public class Demo {

    public static void main(String[] args) throws Exception {
        // 获取指定业务接口配置的所有实现类类型
        List<Class<IDemoService>> demoServiceClasses = ClassUtils.getExtensionLoader(IDemoService.class, true).getExtensionClasses();
        if (demoServiceClasses != null) {
            demoServiceClasses.forEach(demoServiceClass -> System.out.println(demoServiceClass.getName()));
        }
        // 获取指定业务接口配置的所有实现类实例对象
        List<IDemoService> demoServiceImpls = ClassUtils.getExtensionLoader(IDemoService.class, true).getExtensions();
        if (demoServiceImpls != null) {
            demoServiceImpls.forEach(demoServiceImpl -> System.out.println(demoServiceImpl.doService()));
        }
    }
}
```

本例中通过 `ClassUtils.getExtensionLoader` 方法的第二个参数 `alwaysInternal` 是用来指定本次操作是否强制加载内部配置路径，所示需要开发人员自行根据实际业务情况合理使用。



## 自动扫描（AutoScan）

YMP 框架初始化时默认并未开启自动扫描，可以通过 `@EnableAutoScan` 注解或者通过在 `pom.xml` 中引入以下依赖包来开启：

```xml
<dependency>
    <groupId>net.ymate.platform</groupId>
    <artifactId>ymate-platform-starter</artifactId>
    <version>2.1.0</version>
</dependency>
```

自动扫描程序将根据 `@EnableAutoScan` 注解或配置文件中 `ymp.packages` 的参数配置，遍历指定包路径下（含子包）所有被 `@Bean` 注解声明的类文件，分析其所实现的接口类型并向应用容器注册，使用时仅需告知应用容器你需要的实例类型或接口类型即可得到对应的实例对象。

若不希望某个类被自动扫描，只需在该类上声明 `@Ignored` 注解，自动扫描程序将会忽略它的存在。

:::tip **注意：**

相同接口的多个实现类被同时注册到应用容器时，通过接口类型获取的实例对象将是最后被注册到容器的那一个，此时只能通过实例对象类型才能正确获取。

:::



### 示例一：基本操作

```java
// 业务接口
public interface IDemo {
    String sayHi();
}

// 业务接口实现类：单例模式
@Bean
public class SingletonDemoBean implements IDemo {
  	@Override
    public String sayHi() {
        return "Hello, YMP!";
    }
}

// 业务接口实现类：非单例模式
@Bean(singleton = false)
public class DemoBean implements IDemo {
  	@Override
    public String sayHi() {
        return "Hello, YMP!";
    }
}

@EnableAutoScan
public class Starter {

    static {
        System.setProperty(IApplication.SYSTEM_MAIN_CLASS, Starter.class.getName());
    }

    private static final Log LOG = LogFactory.getLog(Starter.class);

    public static void main(String[] args) throws Exception {
        try (IApplication application = YMP.run(args)) {
            // 1. 通过接口获取实例对象
            IDemo demo = application.getBeanFactory().getBean(IDemo.class);
            LOG.info(demo.sayHi());

            // 2. 直接获取实例对象
            demo = application.getBeanFactory().getBean(DemoBean.class);
            LOG.info(demo.sayHi());
        }
    }
}
```



### 示例二：自定义对象处理器

```java
public class DemoBeanHandler implements IBeanHandler {
    @Override
    public Object handle(Class<?> targetClass) throws Exception {
        // 自定义对象处理逻辑...
        return BeanMeta.create(targetClass, true);
    }
}

// 自定义对象处理器 (将取代原来的处理器)
@Bean(handler=DemoBeanHandler.class)
public class DemoBean implements IDemo {
  	@Override
    public String sayHi() {
        return "Hello, YMP!";
    }
}
```


### 示例三：自定义初始化后处理逻辑

```java
@Bean
public class DemoBean implements IDemo, IBeanInitializer {

  	@Override
    public String sayHi() {
        return "Hello, YMP!";
    }

  	@Override
    public void afterInitialized(IBeanFactory beanFactory) throws Exception {
        System.out.println(sayHi() + " ---- afterInitialized.");
    }
}
```



## 依赖注入（DI）

通过在类成员上声明 `@Inject` 和 `@By` 注解来完成依赖注入的设置，且只有被应用容器管理的类对象才支持依赖注入，下面举例说明：



### 示例一：基本操作

```java
// 业务接口
public interface IDemo {
    String sayHi();
}

// 业务接口实现类1
@Bean
public class DemoOne implements IDemo {
  	@Override
    public String sayHi() {
        return "Hello! I'm DemoOne.";
    }
}

// 业务接口实现类2
@Bean
public class DemoTwo implements IDemo {
  	@Override
    public String sayHi() {
        return "Hello! I'm DemoTwo.";
    }
}

@Bean
public class TestDemo {

    private static final Log LOG = LogFactory.getLog(TestDemo.class);

    @Inject
    private IDemo demo1;

    @Inject
    @By(DemoOne.class)
    private IDemo demo2;

    public void sayHi() {
        // demo1注入的是最后被注册到容器的IDemo接口实现
        LOG.info(demo1.sayHi());
        // demo2注入的是由@By注解指定的DemoOne类
        LOG.info(demo2.sayHi());
    }
}

@EnableAutoScan
public class Starter {

    static {
        System.setProperty(IApplication.SYSTEM_MAIN_CLASS, Starter.class.getName());
    }

    public static void main(String[] args) throws Exception {
        try (IApplication application = YMP.run(args)) {
            application.getBeanFactory().getBean(TestDemo.class).sayHi();
        }
    }
}
```



### 示例二：自定义注入注解

可以通过 `@Injector` 注解声明一个 `IBeanInjector` 接口实现类来向框架注册自定义的注入处理逻辑，下面举例说明如何实现一个名为 `@DemoInject` 的自定义注入注解：

:::tip **注意：**

- 当使用自定义注解进行依赖注入操作时可以忽略 `@Inject` 注解，若存在则优先执行 `@Inject` 注入并将此对象当作 `IBeanInjector` 接口方法参数传入。
- 当成员变量被声明多个自定义注入注解规则时（不推荐），根据框架加载顺序，仅执行首个注入规则。

:::



```java
// 定义一个业务接口
public interface IInjectBean {

    String getName();

    void setName(String name);
}

// 业务接口实现类
@Bean
public class InjectBeanImpl implements IInjectBean {

    private String name;

  	@Override
    public String getName() {
        return name;
    }

  	@Override
    public void setName(String name) {
        this.name = name;
    }
}

// 业务对象包装器类
public class InjectBeanWrapper implements IInjectBean {

    private final IInjectBean targetBean;

    public InjectBeanWrapper(IInjectBean targetBean) {
        this.targetBean = targetBean;
    }

  	@Override
    public String getName() {
        return targetBean.getName();
    }

  	@Override
    public void setName(String name) {
        targetBean.setName(name);
    }
}

// 自定义一个注入注解
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DemoInject {

    String value();
}

// 为自定义注入注解编写注入逻辑
@Injector(DemoInject.class)
public class DemoBeanInjector implements IBeanInjector {

  	@Override
    public Object inject(IBeanFactory beanFactory, Annotation annotation, Class<?> targetClass, Field field, Object originInject) {
        // 为从自定义注解取值做准备
        DemoInject anno = (DemoInject) annotation;
        if (originInject == null) {
            // 若通过@Inject注入的对象不为空则为其赋值
            IInjectBean bean = new InjectBeanImpl();
            bean.setName(anno.value());
            // 创建包装器
            originInject = new InjectBeanWrapper(bean);
        } else {
            // 直接创建包装器并赋值
            InjectBeanWrapper wrapper = new InjectBeanWrapper((IInjectBean) originInject);
            wrapper.setName(anno.value());
            //
            originInject = wrapper;
        }
        return originInject;
    }
}

@Bean
public class TestApp {

    @Inject
    @DemoInject("demo")
    private IInjectBean bean;

    public IInjectBean getBean() {
        return bean;
    }
}

@EnableAutoScan
public class Starter {

    static {
        System.setProperty(IApplication.SYSTEM_MAIN_CLASS, Starter.class.getName());
    }

    private static final Log LOG = LogFactory.getLog(Starter.class);

    public static void main(String[] args) throws Exception {
        try (IApplication application = YMP.run(args)) {
            TestApp testApp = application.getBeanFactory().getBean(TestApp.class);
            IInjectBean bean = testApp.getBean();
            LOG.info(bean.getName());
        }
    }
}
```



## 拦截器（AOP）

YMP 框架的 `AOP` 是基于代理（`Proxy`）技术实现的方法拦截，按其执行方向分为前置拦截和后置拦截，框架初始化时默认并未开启代理，可以通过 `@EnableBeanProxy` 注解或者通过在 `pom.xml` 中引入以下依赖包来开启：

```xml
<dependency>
    <groupId>net.ymate.platform</groupId>
    <artifactId>ymate-platform-starter</artifactId>
    <version>2.1.1</version>
</dependency>
```

拦截器可以通过以下注解进行配置：

- @Before：用于设置一个类或方法的前置拦截器，声明在类上的前置拦截器将被应用到该类所有方法上。

- @After：用于设置一个类或方法的后置拦截器，声明在类上的后置拦截器将被应用到该类所有方法上。

- @Around：用于同时配置一个类或方法的前置和后置拦截器。

- @Clean：用于清理类上全部或指定的拦截器，被清理的拦截器将不会被执行。

- @ContextParam：用于设置上下文参数，主要用于向拦截器传递参数配置。

- @Ignored：声明一个方法将忽略一切拦截器配置。

:::tip **注意：**

- 声明 `@Ignored` 注解的方法、非公有方法和 Object 类方法及 Object 类重载方法将不被拦截器处理。
- 使用 `@Interceptor` 注解声明的拦截器类，框架将自动将其注册到应用容器中并支持依赖注入等特性。

:::



### 自定义拦截器

编写拦截器可以通过实现 `IInterceptor` 接口或直接继承 `AbstractInterceptor` 抽象类来完成，一个类方法可以设置多个拦截器，并按照设置的先后顺序执行。拦截器接口方法返回值为非 `null` 对象时，表示方法已被拦截，将停止执行其它拦截器并使用当前拦截器接口方法的返回值作为被拦截方法的执行结果立即返回。

可以通过 `@ContextParam` 注解配置拦截器上下文参数，`@ContextParam`注解的 `value` 属性允许通过 `$xxx` 的格式支持从框架全局参数中获取 `xxx` 的值。



**示例一：** 通过接口创建自定义拦截器

```java
@Interceptor
public class DemoInterceptor implements IInterceptor {

  	@Override
    public Object intercept(InterceptContext context) throws Exception {
        // 判断当前拦截器执行方向
        switch (context.getDirection()) {
            // 前置
            case BEFORE:
                System.out.println("Before intercept...");
                // 获取拦截器上下文参数
                String param = context.getContextParams().get("param");
                if (StringUtils.isNotBlank(param)) {
                    // 若参数值不为空则替换被拦截方法的返回值
                    return param;
                }
                break;
            // 后置
            case AFTER:
                System.out.println("After intercept...");
        }
        return null;
    }
}
```



**示例二：** 通过继承抽象类创建自定义拦截器

```java
@Interceptor
public class DemoInterceptor extends AbstractInterceptor {

    @Override
    protected Object before(InterceptContext context) throws InterceptException {
        System.out.println("Before intercept...");
        // 获取拦截器上下文参数
        String param = context.getContextParams().get("param");
        if (StringUtils.isNotBlank(param)) {
            // 若参数值不为空则替换被拦截方法的返回值
            return param;
        }
        return null;
    }

    @Override
    protected Object after(InterceptContext context) throws InterceptException {
        System.out.println("After intercept...");
        return null;
    }
}
```



**示例三：** 在方法上使用拦截器


```java
@Bean
public class TestApp {

    @Before(DemoInterceptor.class)
    public String beforeTest() {
        return "前置拦截测试";
    }

    @After(DemoInterceptor.class)
    public String afterTest() {
        return "后置拦截测试";
    }

    @Around(DemoInterceptor.class)
    @ContextParam(key = "param", value = "helloworld")
    public String allTest() {
        return "拦截器参数传递";
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
            TestApp testApp = application.getBeanFactory().getBean(TestApp.class);
            LOG.info(testApp.beforeTest());
            LOG.info(testApp.afterTest());
            LOG.info(testApp.allTest());
        }
    }
}
```



**示例四：** 在类上使用拦截器

```java
@Bean
@Before(DemoInterceptor.class)
@ContextParams({@ContextParam(key = "param", value = "helloworld"))})
public class TestApp {

    public String beforeTest() {
        return "前置拦截测试";
    }

    @After(DemoInterceptor.class)
    public String afterTest() {
        return "后置拦截测试";
    }

    @Clean
    public String cleanTest() {
        return "清理拦截器测试";
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
            TestApp testApp = application.getBeanFactory().getBean(TestApp.class);
            LOG.info(testApp.beforeTest());
            LOG.info(testApp.afterTest());
            LOG.info(testApp.cleanTest());
        }
    }
}
```



### 自定义拦截器注解

在上一节中，介绍了如何编写和使用自定义拦截器，以及如何为拦截器配置上下文参数，尽管比较简单，但在使用过程中，对拦截器的配置还是有一点繁琐，于是 YMP 框架从 `2.1.0` 版本开始支持自定义拦截器注解。

接下来，通过重写上一节的示例代码来展示拦截器注解的创建及使用方法。

首先，创建一个名为 `DemoAnn` 的注解并添加自定义参数 `param` ，该类用到了 `@InterceptAnnotation` 注解，它的作用是声明一个注解类用于支持通过其替代原始拦截器配置，唯一的参数 `value` 用于设置拦截方向（默认为空表示全部方向），执行方式与 `@Around` 注解相同并优先于其它拦截器注解，代码如下：

```java
@Target({ElementType.PACKAGE, ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@InterceptAnnotation({IInterceptor.Direction.BEFORE})
public @interface DemoAnn {

    /**
     * 自定义参数
     */
    String param() default "";
}
```
然后，调整 `DemoInterceptor` 拦截器与 `DemoAnn` 注解之间的关联关系，代码如下：

```java
@Interceptor(DemoAnn.class)
public class DemoInterceptor extends AbstractInterceptor {

    @Override
    protected Object before(InterceptContext context) throws InterceptException {
        System.out.println("Before intercept...");
        // 通过拦截器注解获取拦截器参数
        DemoAnn ann = findInterceptAnnotation(context, DemoAnn.class);
        if (ann != null && StringUtils.isNotBlank(ann.param())) {
            // 若参数值不为空则替换被拦截方法的返回值
            return ann.param();
        }
        return null;
    }

    @Override
    protected Object after(InterceptContext context) throws InterceptException {
        System.out.println("After intercept...");
        return null;
    }
}
```



**示例：** 拦截器注解的使用

```java
@Bean
@DemoAnn(param = "helloworld")
public class TestApp {

    public String beforeTest() {
        return "前置拦截测试";
    }

    @After(DemoInterceptor.class)
    public String afterTest() {
        return "后置拦截测试";
    }

    @Clean(DemoInterceptor.class)
    public String cleanTest() {
        return "清理拦截器测试";
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
            TestApp testApp = application.getBeanFactory().getBean(TestApp.class);
            LOG.info(testApp.beforeTest());
            LOG.info(testApp.afterTest());
            LOG.info(testApp.cleanTest());
        }
    }
}
```



### 拦截器包配置

YMP 框架支持在 `package-info.java` 类中声明 `@Before` 、`@After` 、`@Around` 、 `@ContextParam` 和自定义拦截器注解等注解，注解配置将作用于其所在包下所有子包和类。

拦截器的执行顺序： `package` \> `class` \> `method`



**示例：**

```java
@Before(DemoInterceptor.class)
@ContextParam(key = "param", value = "helloworld"))
package net.ymate.demo.controller;

import net.ymate.demo.intercept.DemoInterceptor;
import net.ymate.platform.core.beans.annotation.Before;
import net.ymate.platform.core.beans.annotation.ContextParam;
```



### 拦截器全局规则设置

有些时候，需要对指定的拦截器或某些类和方法的拦截器配置进行调整，为了避免因修改代码而重新编译、打包和部署等繁琐操作，我们可以通过配置文件来完成此项工作，配置格式及说明如下：

现在我们可以通过配置文件来完成此项工作，配置格式及说明如下：

```properties
#-------------------------------------
# 框架拦截器全局规则设置参数
#-------------------------------------

# 是否开启拦截器全局规则设置, 默认值: false
ymp.intercept.settings_enabled=

# 配置包拦截器, 通过'|'分隔多个拦截器
ymp.intercept.packages.<包名>=<[before:|after:]拦截器类名>

# 设置拦截器状态为禁止执行, 仅当取值为disabled时生效
ymp.intercept.globals.<拦截器类名>=disabled

# 为目标类配置拦截器执行规则:
ymp.intercept.settings.<目标类名>#[方法名称]=<[*|before:*|after:*]或[before:|after:]拦截器类名[+|-]]>
```



**示例一：** 为 `net.ymate.demo.controller` 包添加 `net.ymate.demo.intercept.UserSessionInterceptor` 前置拦截器。

```properties
ymp.intercept.packages.net.ymate.demo.controller=before:net.ymate.demo.intercept.UserSessionInterceptor
```



**示例二：** 禁用拦截器： `net.ymate.demo.intercept.UserSessionInterceptor`

```properties
ymp.intercept.globals.net.ymate.demo.intercept.UserSessionInterceptor=disabled
```



**示例三：** 禁用 `net.ymate.demo.controller.DemoController` 类所有方法的全部拦截器。

```properties
ymp.intercept.settings.net.ymate.demo.controller.DemoController#=*
```



**示例四：** 禁用 `net.ymate.demo.controller.DemoController` 类 `doLogin` 方法全部前置拦截器。

```properties
ymp.intercept.settings.net.ymate.demo.controller.DemoController#doLogin=before:*
```



**示例五：** 禁止 `net.ymate.demo.controller.DemoController` 类 `doLogout` 方法某个前置拦截器并增加一个新的后置拦截器。

```properties
ymp.intercept.settings.net.ymate.demo.controller.DemoController#doLogout=before:net.ymate.demo.intercept.UserSessionInterceptor-|after:net.ymate.demo.intercept.UserStatusUpdateInterceptor+
```



## 事件服务（Events）

通过事件的注册、订阅和广播完成事件消息的处理，目的是为了减少代码侵入，降低模块之间的业务耦合度，事件消息采用队列存储，采用多线程接口回调实现消息及消息上下文对象的传输，支持同步（`NORMAL`）和异步（`ASYNC`）两种处理模式。

:::tip **注意：**

当某个事件被触发后，订阅该事件的接口被回调执行的顺序是不能被保证的。

:::



### 事件对象

以下只是 YMP 框架核心中包含的事件对象，不同模块中包含的事件对象将在其相应的文档中阐述。



#### ApplicationEvent：应用容器事件对象

| 事件枚举名称            | 描述                 |
| ----------------------- | -------------------- |
| APPLICATION_STARTUP     | 应用容器启动事件。   |
| APPLICATION_INITIALIZED | 应用容器初始化事件。 |
| APPLICATION_DESTROYED   | 应用容器销毁事件。   |



#### ModuleEvent：模块事件对象

| 事件枚举名称       | 描述             |
| ------------------ | ---------------- |
| MODULE_STARTUP     | 模块启动事件。   |
| MODULE_INITIALIZED | 模块初始化事件。 |
| MODULE_DESTROYED   | 模块销毁事件。   |



### 订阅事件

针对事件的订阅，只要实现事件监听器 `IEventListener` 接口并将其注册到应用容器的事件管理器即可。

目前提供了多种监听器注册方式，下面以订阅模块事件举例说明：



#### 通过 `@EventListener` 注解实现事件订阅

```java
@EventListener(mode = Events.MODE.ASYNC, value = ModuleEvent.class)
public class ModuleEventListener implements IEventListener<ModuleEvent> {

    @Override
    public boolean handle(ModuleEvent context) {
        String moduleName = context.getSource().getName();
        switch (context.getEventName()) {
            case MODULE_STARTUP:
                System.out.println("Startup: " + moduleName);
                break;
            case MODULE_INITIALIZED:
                System.out.println("Initialized: " + moduleName);
                break;
            case MODULE_DESTROYED:
                System.out.println("Destroyed: " + moduleName);
        }
        return false;
    }
}
```



#### 通过 `@EventRegister` 注解实现事件订阅

```java
@EventRegister
public class DemoEventRegister implements IEventRegister {

  	@Override
    public void register(Events events) throws Exception {
        // 订阅模块事件：异步
        events.registerListener(Events.MODE.ASYNC, ModuleEvent.class, new IEventListener<ModuleEvent>() {
            @Override
            public boolean handle(ModuleEvent context) {
                String moduleName = context.getSource().getName();
                switch (context.getEventName()) {
                    case MODULE_STARTUP:
                        System.out.println("Startup: " + moduleName);
                        break;
                    case MODULE_INITIALIZED:
                        System.out.println("Initialized: " + moduleName);
                        break;
                    case MODULE_DESTROYED:
                        System.out.println("Destroyed: " + moduleName);
                }
                return false;
            }
        });
        // ... 还可以添加更多的事件订阅代码
    }
}
```



#### 通过 `IApplicationInitializer` 接口实现事件订阅

由于生命周期的问题，自动扫描程序是在模块初始化完成后执行的，所以通过上述方式订阅应用容器和模块的 `APPLICATION_STARTUP`、`MODULE_STARTUP`、`MODULE_INITIALIZED` 事件代码**不会**被执行。

因此，在应用容器初始化时，可以通过应用容器扩展初始化处理器 `IApplicationInitializer` 接口完成事件注册等相关操作。

```java
public class DemoAppInitializer implements IApplicationInitializer {

    @Override
    public void afterEventInit(IApplication application, Events events) {
        // 订阅模块事件：默认同步
        events.registerListener(ModuleEvent.class, new IEventListener<ModuleEvent>() {
            @Override
            public boolean handle(ModuleEvent context) {
                String moduleName = context.getSource().getName();
                switch (context.getEventName()) {
                    case MODULE_STARTUP:
                        System.out.println("Startup: " + moduleName);
                        break;
                    case MODULE_INITIALIZED:
                        System.out.println("Initialized: " + moduleName);
                        break;
                    case MODULE_DESTROYED:
                        System.out.println("Destroyed: " + moduleName);
                }
                return false;
            }
        });
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
        // 启动容器时传入DemoAppInitializer类实例对象
        try (IApplication application = YMP.run(args, new DemoAppInitializer())) {
            if (application.isInitialized()) {
                LOG.info("Everything depends on ability!  -- YMP :)");
            }
        }
    }
}
```



### 自定义事件

YMP 框架支持开发者根据自身业务自定义事件对象，在事件对象中需要明确事件源类型和用于监听的事件名称枚举类型，下面举例说明自定义事件的创建、注册和触发等操作。



#### 创建自定义事件对象

事件对象需要继承 `AbstractEventContext` 类并实现 `IEvent` 接口。`AbstractEventContext` 的第一个注解参数代表事件源对象类型，第二个注解参数是指定用于监听事件名称的枚举类型。



**示例：** 创建了一个订单事件对象，其中包括订单创建和订单关闭两个事件。

```java
public class OrderEvent extends AbstractEventContext<Object, OrderEvent.EVENT> implements IEvent {

    private static final long serialVersionUID = 1L;

    public enum EVENT {

        /**
         * 订单创建
         */
        ORDER_CREATE,

        /**
         * 订单关闭
         */
        ORDER_CLOSE
    }

    public OrderEvent(Object owner, EVENT eventName) {
        super(owner, OrderEvent.class, eventName);
    }
}
```



#### 注册自定义事件

##### 通过 `@Event` 注解自动注册

YMP框架支持在初始化时将自动扫描并注册被 `@Event` 注解声明的自定义事件类。



##### 通过 `@EventRegister` 注解注册事件

```java
@EventRegister
public class DemoEventRegister implements IEventRegister {

  	@Override
    public void register(Events events) throws Exception {
        // 注册OrderEvent自定义事件类
        events.registerEvent(OrderEvent.class);
    }
}
```



##### 通过 `IApplicationInitializer` 接口注册事件

```java
public class DemoAppInitializer implements IApplicationInitializer {

    @Override
    public void afterEventInit(IApplication application, Events events) {
        // 注册OrderEvent自定义事件类
        events.registerEvent(OrderEvent.class);
    }
}
```



#### 触发自定义事件

每个应用容器（`IApplication`）都拥有属于自己的事件管理器（`Events`）对象，通过事件管理器提供的 `fireEvent` 方法即可触发指定事件。



**示例：** 触发订单创建事件。

```java
try (IApplication application = YMP.run(args)) {
    // 获取应用容器的事件管理器对象
    Events events = application.getEvents();
    // 创建事件对象
    OrderEvent orderEvent = new OrderEvent(application, OrderEvent.class, OrderEvent.EVENT.ORDER_CREATE);
    // 为当前事件设置扩展参数
    orderEvent.addParamExtend("orderId", "xxxx");
    // 触发事件
    events.fireEvent(orderEvent);
}
```



## 模块（Module）

模块是 YMP 框架针对各种功能、特性封装的基础形式，由框架负责模块的生命周期管理，模块在框架初始化时自动加载并初始化，在框架销毁时自动销毁。

### 模块基础结构

```
src
└── main
    ├── java
    │   └── demo
    │       └── module
    │           ├── Demo.java
    │           ├── IDemo.java
    │           ├── IDemoConfig.java
    │           ├── annotation
    │           │   └── DemoConf.java
    │           └── impl
    │               ├── DefaultDemoConfig.java
    │               └── DefaultDemoConfigurable.java
    └── resources
        └── META-INF
            └── services
                └── internal
                    └── net.ymate.platform.core.module.IModule
```



### 模块代码自动生成

为了便于模块开发，YMP 框架 Maven 插件集中提供了模块代码生成器，在项目的 `pom.xml` 文件中添加如下插件配置：

```xml
<plugin>
    <groupId>net.ymate.maven.plugins</groupId>
    <artifactId>ymate-maven-plugin</artifactId>
    <version>1.0.1</version>
</plugin>
```

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

:::tip
本文使用的 YMP 框架 Maven 插件集版本为 `1.0.1` ，了解更多内容请访问： 

<Tabs
defaultValue="github"
values={[
{ label: 'GitHub', value: 'github', },
{ label: '码云', value: 'gitee', },
]
}>
<TabItem value="github">

[https://github.com/suninformation/ymate-maven-plugin.git](https://github.com/suninformation/ymate-maven-plugin.git)

</TabItem>
<TabItem value="gitee">

[https://gitee.com/suninformation/ymate-maven-plugin.git](https://github.com/suninformation/ymate-maven-plugin.git)

</TabItem>
</Tabs>
:::

以生成 demo 模块为例，在项目路径下执行终端命令：

```shell
mvn ymate:module -Dname=demo
```

模块代码生成器命令参数说明：

| 参数        | 描述                                         |
| ----------- | -------------------------------------------- |
| name        | 模块名称，必须。                             |
| packageName | 指定模块包名称，默认值：`${project.groupId}` |
| overwrite   | 是否覆盖已存在的文件，默认值：`false`        |



### 自定义模块

本章节中的代码是在模块代码生成器生成的基础上调整。



#### 步骤一：根据业务需求创建业务接口

```java
@Ignored
public interface IDemo extends IInitialization<IApplication>, IDestroyable {

    String MODULE_NAME = "module.demo";

    /**
     * 获取所属应用容器
     *
     * @return 返回所属应用容器实例
     */
    IApplication getOwner();

    /**
     * 获取配置
     *
     * @return 返回配置对象
     */
    IDemoConfig getConfig();

    /**
     * 对外暴露的业务方法
     */
    String sayHi(String name);
}
```



#### 步骤二：定义模块的配置参数

前面章节介绍过框架的配置方式一种是配置文件，另一种则是基于注解，两者可以共存。当注解和配置文件中对相同配置项赋值时，则优先使用配置文件中的非空值。

而模块是否支持注解方式，则取决于开发者在设计、开发模块时是否提供了对配置注解的支持。



##### 示例一：模块配置注解

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DemoConf {

    /**
     * @return 模块是否已启用, 默认值: true
     */
    boolean enabled() default true;

    /**
     * @return 自定义参数, 默认值: 空
     */
    String param() default "";
}
```



##### 示例二：模块配置接口

```java
@Ignored
public interface IDemoConfig extends IInitialization<IDemo> {

    String ENABLED = "enabled";

    String PARAM = "param";

    /**
     * 模块是否已启用, 默认值: true
     *
     * @return 返回false表示禁用
     */
    boolean isEnabled();

    /**
     * 获取自定义参数, 默认值: 空
     *
     * @return 返回自定义参数值
     */
    String paramOne();
}
```



##### 示例三：实现模块自定义配置接口

```java
public final class DefaultDemoConfig implements IDemoConfig {

    private boolean enabled = true;

    private String param;

    private boolean initialized;

    public static DefaultDemoConfig defaultConfig() {
        return builder().build();
    }

    public static DefaultDemoConfig create(IModuleConfigurer moduleConfigurer) {
        return new DefaultDemoConfig(null, moduleConfigurer);
    }

    public static DefaultDemoConfig create(Class<?> mainClass, IModuleConfigurer moduleConfigurer) {
        return new DefaultDemoConfig(mainClass, moduleConfigurer);
    }

    public static Builder builder() {
        return new Builder();
    }

    private DefaultDemoConfig() {
    }

    private DefaultDemoConfig(Class<?> mainClass, IModuleConfigurer moduleConfigurer) {
        IConfigReader configReader = moduleConfigurer.getConfigReader();
        //
        DemoConf confAnn = mainClass == null ? null : mainClass.getAnnotation(DemoConf.class);
        //
        enabled = configReader.getBoolean(ENABLED, confAnn == null || confAnn.enabled());
        if (enabled) {
            // TODO 在此处分析配置参数
            param = configReader.getString(PARAM, confAnn == null ? StringUtils.EMPTY : confAnn.param());
        }
    }

    @Override
    public void initialize(IDemo owner) throws Exception {
        if (!initialized) {
            if (enabled) {
                // TODO 在此处验证配置参数
                if (StringUtils.isBlank(param)) {
                    param = StringUtils.EMPTY;
                }
            }
            initialized = true;
        }
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (!initialized) {
            this.enabled = enabled;
        }
    }

    @Override
    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        if (!initialized) {
            this.param = param;
        }
    }

    public static final class Builder {

        private final DefaultDemoConfig config = new DefaultDemoConfig();

        private Builder() {
        }

        public Builder enabled(boolean enabled) {
            config.setEnabled(enabled);
            return this;
        }

        public Builder param(String param) {
            config.setParam(param);
            return this;
        }

        public DefaultDemoConfig build() {
            return config;
        }
    }
}
```



##### 示例四：模块自定义配置对象构建器

```java
public final class DefaultDemoConfigurable extends DefaultModuleConfigurable {

    public static Builder builder() {
        return new Builder();
    }

    private DefaultDemoConfigurable() {
        super(IDemo.MODULE_NAME);
    }

    public static final class Builder {

        private final DefaultDemoConfigurable configurable = new DefaultDemoConfigurable();

        private Builder() {
        }

        public Builder enabled(boolean enabled) {
            configurable.addConfig(IDemoConfig.ENABLED, String.valueOf(enabled));
            return this;
        }

        public Builder param(String param) {
            configurable.addConfig(IDemoConfig.PARAM, param);
            return this;
        }

        public IModuleConfigurer build() {
            return configurable.toModuleConfigurer();
        }
    }
}
```



#### 步骤三：实现模块及业务接口

```java
public final class Demo implements IModule, IDemo {

    private static final Log LOG = LogFactory.getLog(Demo.class);

    private static volatile IDemo instance;

    private IApplication owner;

    private IDemoConfig config;

    private boolean initialized;

    public static IDemo get() {
        IDemo inst = instance;
        if (inst == null) {
            synchronized (Demo.class) {
                inst = instance;
                if (inst == null) {
                    instance = inst = YMP.get().getModuleManager().getModule(Demo.class);
                }
            }
        }
        return inst;
    }

    public Demo() {
    }

    public Demo(IDemoConfig config) {
        this.config = config;
    }

    @Override
    public String getName() {
        return MODULE_NAME;
    }

    @Override
    public void initialize(IApplication owner) throws Exception {
        if (!initialized) {
            //
            YMP.showVersion("Initializing demo-${version}", new Version(1, 0, 0, Demo.class, Version.VersionType.Alpha));
            //
            this.owner = owner;
            if (config == null) {
                IApplicationConfigureFactory configureFactory = owner.getConfigureFactory();
                if (configureFactory != null) {
                    IApplicationConfigurer configurer = configureFactory.getConfigurer();
                    IModuleConfigurer moduleConfigurer = configurer == null ? null : configurer.getModuleConfigurer(MODULE_NAME);
                    if (moduleConfigurer != null) {
                        config = DefaultDemoConfig.create(configureFactory.getMainClass(), moduleConfigurer);
                    } else {
                        config = DefaultDemoConfig.create(configureFactory.getMainClass(), DefaultModuleConfigurer.createEmpty(MODULE_NAME));
                    }
                }
                if (config == null) {
                    config = DefaultDemoConfig.defaultConfig();
                }
            }
            if (!config.isInitialized()) {
                config.initialize(this);
            }
            if (config.isEnabled()) {
                // TODO 在此处编写模块初始化逻辑
            }
            initialized = true;
        }
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void close() throws Exception {
        if (initialized) {
            initialized = false;
            //
            if (config.isEnabled()) {
                // TODO 在此处编写模块销毁逻辑
            }
            //
            config = null;
            owner = null;
        }
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
    public String sayHi(String name) {
        // TODO 业务方法逻辑
        return String.format("Hi! %s", StringUtils.defaultIfBlank(name, config.getParam());
    }
}
```



#### 步骤四：配置模块加载类

YMP 框架从 `2.1.0` 版本开始，调整了模块的加载方式，放弃了自动扫描，转而采用 `SPI` 机制，因此需要（推荐）在 `META-INF/services/internal/` 目录创建名称为 `net.ymate.platform.core.module.IModule` 的文件，并添加需要加载的模块实现类名称。

如：本例的配置文件内容如下所示：

```shell
demo.module.Demo
```



### 调用自定义模块

#### 配置文件方式

在 YMP 框架初始化配置文件 `ymp-conf.properties` 中添加自定义模块的配置内容：

> 格式： ymp.configs.<模块名称>.<参数名称>=[参数值]

```properties
# 模块是否已启用, 默认值: true
ymp.configs.module.demo.enabled=true

# 自定义参数, 默认值: 空
ymp.configs.module.demo.param=Everything depends on ability!
```



**示例：**

```java
@Bean
public class TestApp {

    @Inject
    private Demo demo;

    public String getParam() {
        // 调用模块配置信息
        return demo.getConfig().getParam();
    }
}

public class Starter {

    private static final Log LOG = LogFactory.getLog(Starter.class);

    public static void main(String[] args) throws Exception {
        try (IApplication application = YMP.run(args)) {
            TestApp testApp = application.getBeanFactory().getBean(TestApp.class);
            LOG.info(testApp.getParam());
            // 调用模块业务接口方法
            LOG.info(Demo.get().sayHi());
        }
    }
}
```



#### 配置注解方式


```java
@Bean
public class TestApp {

    @Inject
    private Demo demo;

    public String getParam() {
        // 调用模块配置信息
        return demo.getConfig().getParam();
    }
}

@EnableAutoScan
@EnableBeanProxy
@DemoConf(param = "Everything depends on ability!")
public class Starter {

    static {
        System.setProperty(IApplication.SYSTEM_MAIN_CLASS, Starter.class.getName());
    }

    private static final Log LOG = LogFactory.getLog(Starter.class);

    public static void main(String[] args) throws Exception {
        try (IApplication application = YMP.run(args)) {
            TestApp testApp = application.getBeanFactory().getBean(TestApp.class);
            LOG.info(testApp.getParam());
            // 调用模块业务接口方法
            LOG.info(Demo.get().sayHi());
        }
    }
}
```



## 国际化（I18N）

国际化服务是应用容器在初始化时，会根据 `@I18nConf` 注解或配置文件中 `ymp.default_locale` 参数进行语言设置，默认采用系统运行环境的语言设置。



### 获取国际化资源管理器对象

```java
// 获取应用容器对象
IApplication application = YMP.get();
// 从应用容器中获取国际化资源管理器
I18N i18n = application.getI18n();
```



### 获取当前语言设置

```java
Locale locale = i18n.current();
```



### 设置当前语言

```java
// 变更当前语言设置且不触发事件
i18n.current(Locale.ENGLISH);

// 或者：将触发监听处理器onChanged事件
i18n.change(Locale.ENGLISH);
```


### 重置当前语言设置

```java
// 用于清理当前本地线程中的相关设置
i18n.reset();
```



### 根据当前语言设置加载资源文件属性值

```java
i18n.load("resources", "home_title");
i18n.load("resources", "home_title", "首页");
```



### 格式化消息字符串并绑定参数

```java
// 加载指定名称资源文件内指定的属性并使用格式化参数绑定
i18n.formatMessage("resources", "site_title", "Welcome {0}, {1}", "YMP", "GoodLuck!");

// 使用格式化参数绑定
i18n.formatMsg("Hello, {0}, {1}", "YMP", "GoodLuck!");
```



### 国际化资源事件处理器

YMP 框架可以通过 `II18nEventHandler` 接口，实现自定义语言设置和资源文件的加载逻辑。

国际化资源事件处理器可以完成如下操作：

+ 自定义资源文件加载过程。
+ 自定义获取当前语言设置。
+ 语言设置变更的事件处理过程。



**示例：**

```java
public class DemoI18nEventHandler implements II18nEventHandler {

    @Override
    public Locale onLocale() {
        // 加载并返回当前Locale对象
        return null;
    }

    @Override
    public void onChanged(Locale locale) {
        // 当Locale改变时调用此方法
    }

    @Override
    public InputStream onLoad(String resourceName) throws IOException {
        // 加载资源文件的具体处理逻辑并返回加载的资源文件流
        return null;
    }
}
```



## 记录类成员属性状态（PropertyState）

通过在类成员变量上声明 `@PropertyState` 注解，并使用 `PropertyStateSupport` 工具类配合，可以实现对类成员属性的变化情况进行监控。



### @PropertyState

记录类成员属性值的变化。

| 配置项       | 描述                                             |
| ------------ | ------------------------------------------------ |
| propertyName | 自定义成员属性名称，默认为空则采用当前成员名称。 |
| aliasName    | 自定义别名，默认为空。                           |
| setterName   | 成员属性对应的SET方法名称，默认为空。            |

**示例代码：**

```java
public class Student {

    @PropertyState(propertyName = "user_name")
    private String username;

    @PropertyState(aliasName = "年龄")
    private int age;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public static void main(String[] args) throws Exception {
        Student original = new Student();
        original.setUsername("123456");
        original.setAge(20);
        //
        PropertyStateSupport<Student> stateSupport = PropertyStateSupport.create(original);
        Student bindStudent = stateSupport.bind();
        bindStudent.setUsername("YMPer");
        bindStudent.setAge(30);
        //
        System.out.printf("发生变更的字段名集合: %s%n", Arrays.asList(stateSupport.getChangedPropertyNames()));
        stateSupport.getChangedProperties()
                .forEach(stateMeta ->
                        System.out.printf("已将%s由%s变更为%s%n", StringUtils.defaultIfBlank(stateMeta.getAliasName(), stateMeta.getPropertyName()), stateMeta.getOriginalValue(), stateMeta.getNewValue()));
    }
}
```
**执行结果：**

```sh
发生变更的字段名集合: [user_name, age]
已将user_name由123456变更为YMPer
已将年龄由20变更为30
```



## 对象资源回收（Recycle）

YMP 框架提供了一个对象资源回收助手 `RecycleHelper` 类，允许开发者主动将需要进行资源回收的对象的销毁方法和手段注册进来，并在框架应用容器被销毁之前采用回调的方式执行接口方法，从而达到资源统一回收的目的。



### 示例一：全局资源回收对象销毁方法注册

全局资源回收由框架应用容器统一管理，无需手动触发回收动作。

```java
public class Starter {

    private static final Log LOG = LogFactory.getLog(Starter.class);

    public static void main(String[] args) throws Exception {
        try (IApplication application = YMP.run(args)) {
            RecycleHelper recycleHelper = application.getRecycleHelper();
            recycleHelper.register(new IDestroyable() {
                @Override
                public void close() throws Exception {
                    LOG.info("此处编写目标对象的资源回收销毁逻辑");
                }
            });
        }
    }
}
```



### 示例二：自定义资源回收及对象销毁

```java
// 创建对象资源回收助手实例对象
RecycleHelper recycleHelper = RecycleHelper.create();
// 注册对象销毁方法（采用lambda表达式）
recycleHelper.register(() -> System.out.println("此处编写目标对象的资源回收销毁逻辑"));
// 资源回收方式支持同步和异步两种：
// - 执行资源回收（同步方式）
recycleHelper.recycle();
// - 执行资源回收（异步方式）
recycleHelper.recycle(true);
```

