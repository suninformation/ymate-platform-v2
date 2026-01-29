# Plugin 模块技能文档

## 1. 模块概述

Plugin 模块是 YMP 框架中的一个重要模块，提供了插件化开发的能力。该模块采用独立的类加载器（ClassLoader）来管理私有包、类、资源文件等，设计目标是在接口开发模式下，将需求进行更细颗粒度拆分，从而达到一个理想化可重用代码的封装形态。

**主要功能特点：**

- 独立的类加载器管理插件的私有包、类和资源文件
- 支持插件的自动扫描和注册
- 支持插件的生命周期管理（初始化、启动、停止、销毁）
- 支持插件的依赖注入
- 支持插件的事件监听
- 支持多个插件工厂实例，工厂对象之间完全独立
- 支持从 JAR 包或目录加载插件

## 2. 核心功能

### 2.1 插件工厂

插件工厂是管理插件的容器，负责插件的分析、加载和初始化，以及插件的生命周期管理。插件工厂分为两种：

- **默认插件工厂**：由框架初始化时根据配置参数自动构建，与框架结合得更紧密。
- **自定义插件工厂**：通过代码手动配置构建，与默认插件工厂可以并存。

### 2.2 插件结构

每个插件都是一个封闭的世界，插件与外界之间沟通的唯一方法是通过业务接口调用。插件的目录结构如下：

```shell
<PLUGIN_HOME>\
    |--.plugin\
    |   |--lib\
    |   |   |--xxxx.jar
    |   |   |--...
    |   |--classes\
    |   |   |--...
    |   |--...
    |--<plugin_xxx>\
    |   |--lib\
    |   |   |--xxxx.jar
    |   |   |--...
    |   |--classes\
    |   |   |--...
    |   |--...
    |--<plugin_xxxx>\
    |--...
```

### 2.3 插件生命周期

插件的生命周期包括以下阶段：

1. **初始化（initialize）**：插件被加载后进行初始化操作。
2. **启动（startup）**：插件初始化完成后启动。
3. **运行（running）**：插件处于运行状态，可以处理业务请求。
4. **停止（shutdown）**：插件停止运行。
5. **销毁（close）**：插件被销毁，释放资源。

### 2.4 插件事件

插件模块提供了插件生命周期事件的监听机制，通过 `IPluginEventListener` 接口实现插件生命周期事件监听。

## 3. API 接口

### 3.1 核心接口

#### IPlugin

插件接口，是所有插件的基础接口。

```java
public interface IPlugin {
    
    /**
     * 执行插件初始化
     * @throws Exception 异常
     */
    void initialize() throws Exception;
    
    /**
     * 判断插件是否已初始化
     * @return 是否已初始化
     */
    boolean isInitialized();
    
    /**
     * 获取插件环境上下文对象
     * @return 插件环境上下文
     */
    IPluginContext getPluginContext();
    
    /**
     * 判断插件是否已启动
     * @return 是否已启动
     */
    boolean isStarted();
    
    /**
     * 启动插件
     * @throws Exception 异常
     */
    void startup() throws Exception;
    
    /**
     * 停止插件
     * @throws Exception 异常
     */
    void shutdown() throws Exception;
    
    /**
     * 销毁插件
     * @throws Exception 异常
     */
    void close() throws Exception;
}
```

#### AbstractPlugin

插件抽象类，实现了 `IPlugin` 接口，提供了更方便的插件编写方式。

```java
public abstract class AbstractPlugin implements IPlugin {
    
    /**
     * 执行插件初始化
     * @param context 插件环境上下文
     * @throws Exception 异常
     */
    protected abstract void doInitialize(IPluginContext context) throws Exception;
    
    /**
     * 启动插件
     * @throws Exception 异常
     */
    protected void doStartup() throws Exception {
    }
    
    /**
     * 停止插件
     * @throws Exception 异常
     */
    protected void doShutdown() throws Exception {
    }
    
    /**
     * 销毁插件
     * @throws Exception 异常
     */
    protected void doClose() throws Exception {
    }
}
```

#### IPluginFactory

插件工厂接口，负责插件的管理。

```java
public interface IPluginFactory {
    
    /**
     * 获取插件工厂配置
     * @return 插件工厂配置
     */
    IPluginConfig getConfig();
    
    /**
     * 获取插件元数据集合
     * @return 插件元数据集合
     */
    Collection<IPluginMeta> getPluginMetas();
    
    /**
     * 根据插件唯一标识获取插件实例
     * @param pluginId 插件唯一标识
     * @param <T> 插件类型
     * @return 插件实例
     */
    <T> T getPlugin(String pluginId);
    
    /**
     * 根据插件类型获取插件实例
     * @param pluginClass 插件类型
     * @param <T> 插件类型
     * @return 插件实例
     */
    <T> T getPlugin(Class<T> pluginClass);
    
    /**
     * 根据接口类型获取插件实例
     * @param interfaceClass 接口类型
     * @param <T> 接口类型
     * @return 插件实例
     */
    <T> T getPluginByInterface(Class<T> interfaceClass);
    
    /**
     * 初始化插件工厂
     * @throws Exception 异常
     */
    void initialize() throws Exception;
    
    /**
     * 启动所有插件
     * @throws Exception 异常
     */
    void startup() throws Exception;
    
    /**
     * 停止所有插件
     * @throws Exception 异常
     */
    void shutdown() throws Exception;
    
    /**
     * 销毁插件工厂
     * @throws Exception 异常
     */
    void close() throws Exception;
}
```

### 3.2 配置接口

#### IPluginConfig

插件配置接口，用于配置插件工厂。

```java
public interface IPluginConfig {
    
    /**
     * 获取插件主目录路径
     * @return 插件主目录路径
     */
    File getPluginHome();
    
    /**
     * 获取自动扫描包名
     * @return 自动扫描包名
     */
    Set<String> getPackageNames();
    
    /**
     * 获取插件是否自动启动
     * @return 是否自动启动
     */
    boolean isAutomatic();
    
    /**
     * 获取插件生命周期事件监听器
     * @return 插件生命周期事件监听器
     */
    IPluginEventListener getEventListener();
    
    /**
     * 获取插件对象加载器工厂
     * @return 插件对象加载器工厂
     */
    IPluginObjectLoaderFactory getLoaderFactory();
}

```

### 3.3 注解

#### @Plugin

声明一个类为插件。

```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Plugin {
    
    /**
     * 插件唯一标识
     */
    String id() default "";
    
    /**
     * 插件名称
     */
    String name() default "";
    
    /**
     * 插件别名
     */
    String alias() default "";
    
    /**
     * 插件作者
     */
    String author() default "";
    
    /**
     * 联系邮箱
     */
    String email() default "";
    
    /**
     * 插件版本
     */
    String version() default "1.0.0";
    
    /**
     * 是否加载后自动启动运行
     */
    boolean automatic() default true;
    
    /**
     * 插件描述
     */
    String description() default "";
}
```

#### @PluginFactory

声明一个类为插件工厂。

```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PluginFactory {
    
    /**
     * 插件存放路径
     */
    String pluginHome();
    
    /**
     * 自动扫描包名
     */
    String[] packageNames() default {};
    
    /**
     * 插件是否自动启动
     */
    boolean automatic() default true;
    
    /**
     * 插件生命周期事件监听器类对象
     */
    Class<? extends IPluginEventListener> listenerClass() default DefaultPluginEventListener.class;
    
    /**
     * 插件对象加载器工厂类
     */
    Class<? extends IPluginObjectLoaderFactory> loaderFactoryClass() default DefaultPluginObjectLoaderFactory.class;
}
```

#### @PluginConf

插件模块初始化参数配置。

```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PluginConf {
    
    /**
     * 是否启用插件模块
     */
    boolean enabled() default true;
    
    /**
     * 插件主目录路径
     */
    String pluginHome() default "${root}/plugins";
    
    /**
     * 插件自动扫描的包名前缀集合
     */
    String[] packageNames() default {};
    
    /**
     * 插件自动扫描时排除包名称集合
     */
    String[] excludedPackageNames() default {};
    
    /**
     * 插件自动扫描时排除包文件名称集合
     */
    String[] excludedFileNames() default {};
    
    /**
     * 是否允许插件自动启动
     */
    boolean automatic() default true;
    
    /**
     * 是否加载当前CLASSPATH内的所有包含插件配置文件的JAR包
     */
    boolean includedClasspath() default false;
}
```

#### @PluginRefer

通过依赖注入引用插件实例。

```java
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PluginRefer {
    
    /**
     * 插件唯一标识或别名
     */
    String value() default "";
}
```

## 4. 使用场景

### 4.1 功能模块化

将系统的不同功能拆分为独立的插件，每个插件负责一个特定的功能，提高代码的可维护性和可扩展性。

### 4.2 插件市场

构建一个插件市场，允许用户根据需要安装和卸载插件，实现系统功能的动态扩展。

### 4.3 第三方集成

将第三方系统的集成封装为插件，通过插件的形式与系统集成，降低系统与第三方系统的耦合度。

### 4.4 多租户系统

为不同的租户提供不同的插件，实现租户间功能的隔离和定制化。

### 4.5 动态功能切换

通过插件的启动和停止，实现系统功能的动态切换，无需重启系统。

## 5. 配置方式

### 5.1 默认插件工厂配置

通过配置文件或 `@PluginConf` 注解配置默认插件工厂。

#### 配置文件配置

```properties
#-------------------------------------
# Plugin插件模块初始化参数
#-------------------------------------

# 是否启用插件模块
ymp.configs.plugin.enabled=true

# 插件主目录路径
ymp.configs.plugin.plugin_home=${root}/plugins

# 插件自动扫描的包名前缀集合
ymp.configs.plugin.package_names=

# 插件自动扫描时排除包名称集合
ymp.configs.plugin.excluded_package_names=

# 插件自动扫描时排除包文件名称集合
ymp.configs.plugin.excluded_file_names=

# 是否允许插件自动启动
ymp.configs.plugin.automatic=true

# 是否加载当前CLASSPATH内的所有包含插件配置文件的JAR包
ymp.configs.plugin.included_classpath=false

# 设置指定插件插件状态为禁用
ymp.params.plugin.xxx=disabled
```

#### 注解配置

```java
@EnableAutoScan
@PluginConf(
        enabled = true,
        pluginHome = "${root}/plugins",
        packageNames = {"com.company", "cn.company"},
        automatic = true,
        includedClasspath = false
)
public class Starter {
    
    public static void main(String[] args) throws Exception {
        try (IApplication application = YMP.run(args)) {
            // 应用逻辑
        }
    }
}
```

### 5.2 自定义插件工厂配置

通过代码手动配置构建自定义插件工厂。

#### 基于注解配置

```java
@PluginFactory(
        pluginHome = "${root}/plugins",
        packageNames = {"com.company", "cn.company"},
        automatic = true,
        listenerClass = DemoPluginEventListener.class
)
public class DemoPluginFactory extends DefaultPluginFactory {
    
    public DemoPluginFactory(IPluginConfig pluginConfig) {
        super(pluginConfig, false);
    }
}

@EnableAutoScan
public class Starter {
    
    public static void main(String[] args) throws Exception {
        try (IApplication application = YMP.run(args)) {
            IPluginFactory pluginFactory = DefaultPluginFactory.create(application, DemoPluginFactory.class);
            pluginFactory.getPluginMetas()
                .forEach(pluginMeta -> System.out.println(pluginMeta.getName()));
        }
    }
}
```

#### 基于代码配置

```java
public class DemoPluginFactory extends DefaultPluginFactory {
    
    public DemoPluginFactory(IPluginConfig pluginConfig) {
        super(pluginConfig, false);
    }
}

@EnableAutoScan
public class Starter {
    
    public static void main(String[] args) throws Exception {
        try (IApplication application = YMP.run(args)) {
            IPluginConfig pluginConfig = DefaultPluginConfig.create()
                .pluginHome(new File(RuntimeUtils.replaceEnvVariable("${root}/plugins")))
                .automatic(true)
                .autoscanPackages(Arrays.asList("com.company", "cn.company"))
                .eventListener(new DemoPluginEventListener());
            IPluginFactory pluginFactory = DefaultPluginFactory.create(application, DemoPluginFactory.class, pluginConfig);
            pluginFactory.getPluginMetas()
                .forEach(pluginMeta -> System.out.println(pluginMeta.getName()));
        }
    }
}
```

## 6. 注意事项

1. **类加载器隔离**：插件使用独立的类加载器，避免与主应用的类冲突。

2. **插件依赖**：插件之间的依赖关系应该明确，避免循环依赖。

3. **资源管理**：插件应该妥善管理自己的资源，确保在销毁时释放所有资源。

4. **异常处理**：插件中的异常应该妥善处理，避免影响整个系统的运行。

5. **版本兼容**：插件应该考虑版本兼容性，避免因主应用版本升级而导致插件无法正常运行。

6. **性能考虑**：插件的加载和初始化会消耗一定的时间和资源，应该合理设计插件的大小和数量。

7. **安全考虑**：插件可能会执行任意代码，应该对插件进行安全审查，避免安全漏洞。

8. **插件通信**：插件与外界之间沟通的唯一方法是通过业务接口调用，应该设计良好的接口。

## 7. 最佳实践

1. **接口设计**：为插件设计清晰、稳定的业务接口，避免频繁修改接口。

2. **插件粒度**：合理划分插件的粒度，每个插件应该只负责一个特定的功能。

3. **插件依赖**：尽量减少插件之间的依赖关系，提高插件的独立性。

4. **资源管理**：在插件的 `close` 方法中释放所有资源，避免资源泄漏。

5. **异常处理**：在插件的各个生命周期方法中妥善处理异常，避免异常影响整个系统。

6. **日志记录**：在插件中使用适当的日志记录，便于问题排查。

7. **测试**：为插件编写专门的测试用例，确保插件的功能正确。

8. **文档化**：为插件添加详细的文档，说明插件的功能、使用方法和配置参数。

9. **版本管理**：为插件设置合理的版本号，便于版本管理和升级。

10. **插件市场**：构建一个插件市场，便于插件的分发和管理。

## 8. 示例代码

### 8.1 创建插件

```java
public interface IEchoService {
    void sayHi();
}

@Plugin(id = "echo_plugin",
        name = "DemoPlugin",
        author = "有理想的鱼",
        email = "suninformaiton#163.com", version = "1.0.0")
public class EchoPlugin extends AbstractPlugin implements IEchoService {
    
    @Override
    protected void doInitialize(IPluginContext context) throws Exception {
        System.out.println("initialized.");
    }
    
    @Override
    protected void doStartup() throws Exception {
        System.out.println("started.");
    }
    
    @Override
    protected void doShutdown() throws Exception {
        System.out.println("shutdown.");
    }
    
    @Override
    public void sayHi() {
        System.out.println("Hi, from Plugin.");
    }
}
```

### 8.2 使用插件

```java
@EnableAutoScan
public class Starter {
    
    public static void main(String[] args) throws Exception {
        try (IApplication application = YMP.run(args)) {
            // 方式一：获取插件实例对象
            EchoPlugin echoPlugin = Plugins.get().getPlugin(EchoPlugin.class);
            // echoPlugin = (EchoPlugin) Plugins.get().getPlugin("echo_plugin");
            echoPlugin.sayHi();
            // 方式二：直接获取业务接口实例对象
            IEchoService echoService = Plugins.get().getPlugin(IEchoService.class);
            echoService.sayHi();
        }
    }
}
```

### 8.3 通过依赖注入引用插件

```java
@Bean
public class Demo {
    
    @PluginRefer
    private IEchoService echoService;
    
    public IEchoService getEchoService() {
        return echoService;
    }
}

@EnableAutoScan
public class Starter {
    
    public static void main(String[] args) throws Exception {
        try (IApplication application = YMP.run(args)) {
            Demo demo = application.getBeanFactory().getBean(Demo.class);
            demo.getEchoService().sayHi();
        }
    }
}
```

### 8.4 自定义插件工厂

```java
@PluginFactory(pluginHome = "${root}/plugins",
               packageNames = {"com.company", "cn.company"},
               automatic = true,
               listenerClass = DemoPluginEventListener.class)
public class DemoPluginFactory extends DefaultPluginFactory {
    
    public DemoPluginFactory(IPluginConfig pluginConfig) {
        super(pluginConfig, false);
    }
}

public class DemoPluginEventListener implements IPluginEventListener {
    
    @Override
    public void onInitialized(IPluginContext context, IPlugin plugin) {
        System.out.println("onInitialized: " + context.getPluginMeta().getName());
    }
    
    @Override
    public void onStarted(IPluginContext context, IPlugin plugin) {
        System.out.println("onStarted: " + context.getPluginMeta().getName());
    }
    
    @Override
    public void onShutdown(IPluginContext context, IPlugin plugin) {
        System.out.println("onShutdown: " + context.getPluginMeta().getName());
    }
    
    @Override
    public void onDestroy(IPluginContext context, IPlugin plugin) {
        System.out.println("onDestroy: " + context.getPluginMeta().getName());
    }
}

@EnableAutoScan
public class Starter {
    
    public static void main(String[] args) throws Exception {
        try (IApplication application = YMP.run(args)) {
            IPluginFactory pluginFactory = DefaultPluginFactory.create(application, DemoPluginFactory.class);
            pluginFactory.getPluginMetas()
                .forEach(pluginMeta -> System.out.println(pluginMeta.getName()));
        }
    }
}
```

## 9. 总结

Plugin 模块是 YMP 框架中一个强大的模块，提供了灵活、易用的插件化开发能力。通过该模块，开发者可以实现功能的模块化、插件化，提高代码的可维护性和可扩展性。

该模块的主要优势在于：

1. **独立的类加载器**：插件使用独立的类加载器，避免与主应用的类冲突。

2. **完整的生命周期管理**：支持插件的初始化、启动、停止和销毁等完整的生命周期管理。

3. **灵活的配置方式**：支持通过配置文件和注解两种方式配置插件模块。

4. **支持多种插件工厂**：支持默认插件工厂和自定义插件工厂，可以并存。

5. **与框架集成**：与 YMP 框架的其他模块无缝集成，支持依赖注入等特性。

6. **插件通信**：通过业务接口调用实现插件与外界的通信，降低耦合度。

7. **事件监听**：支持插件生命周期事件的监听，便于扩展。

Plugin 模块为 YMP 框架提供了强大的插件化能力，是构建模块化、可扩展系统的重要工具。通过合理使用该模块，可以大大提高代码的质量和开发效率。