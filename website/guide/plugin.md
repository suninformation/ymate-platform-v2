---
sidebar_position: 6
slug: plugin
---

# 插件（Plugin）

插件模块采用独立的类加载器（ClassLoader）来管理私有包、类、资源文件等，设计目标是在接口开发模式下，将需求进行更细颗粒度拆分，从而达到一个理想化可重用代码的封装形态。

每个插件都是封闭的世界，插件与外界之间沟通的唯一方法是通过业务接口调用，管理这些插件的容器被称之为插件工厂（IPluginFactory），其负责插件的分析、加载和初始化，以及插件的生命周期管理，插件模块支持创建多个插件工厂实例，工厂对象之间完全独立，无任何依赖关系。



## Maven包依赖

```xml
<dependency>
    <groupId>net.ymate.platform</groupId>
    <artifactId>ymate-platform-plugin</artifactId>
    <version>2.1.0</version>
</dependency>
```



## 默认插件工厂

插件工厂分为两种，一种是以模块的形式封装，由框架初始化时根据配置参数自动构建，称之为默认插件工厂（有且仅能存在一个默认工厂实例），另一种是通过代码手动配置构建的自定义插件工厂，不同之处在于默认插件工厂与框架结合得更紧密，两种模式可以并存。



### 配置文件参数说明

```properties
#-------------------------------------
# Plugin插件模块初始化参数
#-------------------------------------

# 是否启用插件模块（若禁用则在框架初始化不会创建默认插件工厂对象，不影响自定义插件工厂的使用），默认为true
ymp.configs.plugin.enabled=

# 插件主目录路径，可选参数，默认值为${root}/plugins
ymp.configs.plugin.plugin_home=

# 插件自动扫描的包名前缀集合（若未设置将默认包含主程序类所在包），多个包名之间用'|'分隔
ymp.configs.plugin.package_names=

# 插件自动扫描时排除包名称集合，多个包名之间用'|'分隔, 被包含在包路径下的类文件在扫描过程中将被忽略
ymp.configs.plugin.excluded_package_names=

# 插件自动扫描时排除包文件名称集合，多个包名之间用'|'分隔, 被包含的JAR或ZIP文件在扫描过程中将被忽略
ymp.configs.plugin.excluded_file_names=

# 是否允许插件自动启动，默认为true
ymp.configs.plugin.automatic=

# 是否加载当前CLASSPATH内的所有包含插件配置文件的JAR包，默认为false
ymp.configs.plugin.included_classpath=

# 设置指定插件插件状态为禁用（xxx代表插件唯一标识或插件类名称，其值必须是disabled）
ymp.params.plugin.xxx=disabled
```



### 配置注解参数说明

#### @PluginConf 

| 配置项               | 描述                                                         |
| -------------------- | ------------------------------------------------------------ |
| enabled              | 是否启用插件模块，默认为 `true`                              |
| pluginHome           | 插件主目录路径，可选参数，默认值为 `${root}/plugins`         |
| packageNames         | 插件自动扫描的包名前缀集合（若未设置将默认包含主程序类所在包），可选参数 |
| excludedPackageNames | 插件自动扫描时排除包名称集合，可选参数                       |
| excludedFileNames    | 插件自动扫描时排除包文件名称集合，可选参数                   |
| includeClasspath     | 是否扫描当前 `CLASSPATH` 内的相关插件，默认为 `false         |
| automatic            | 是否允许插件自动启动，默认为 `true`                          |



### 默认插件工厂事件

默认插件工厂是通过框架的事件服务订阅进行处理，包括以下事件类型：

| 事务类型           | 说明           |
| ------------------ | -------------- |
| PLUGIN_INITIALIZED | 插件初始化事件 |
| PLUGIN_STARTED     | 插件启动事件   |
| PLUGIN_SHUTDOWN    | 插件停止事件   |
| PLUGIN_DESTROYED   | 插件销毁事件   |



## 自定义插件工厂

创建自定义插件工厂时，需要对该插件工厂进行一系列配置，框架针对插件工厂的配置提供了注解和手工编码两种方式，可以根据实际情况自由选择，在通常情况下，基于注解的配置方式更为适用。



### 方式一：基于注解配置

#### @PluginFactory

| 配置项             | 描述                                                         |
| ------------------ | ------------------------------------------------------------ |
| pluginHome         | 插件存放路径，必需提供                                       |
| packageNames       | 自动扫描包名，默认为插件工厂所在包路径                       |
| automatic          | 插件是否自动启动，默认为 `true`                              |
| listenerClass      | 插件生命周期事件监听器类对象，可选配置，若未提供则采用系统默认实现 |
| loaderFactoryClass | 插件对象加载器工厂类，可选配置，若未提供则采用系统默认实现   |

#### 示例代码：

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

@EnableAutoScan
public class Starter {

    static {
        System.setProperty(IApplication.SYSTEM_MAIN_CLASS, Starter.class.getName());
    }

    public static void main(String[] args) throws Exception {
        try (IApplication application = YMP.run(args)) {
            IPluginFactory pluginFactory = DefaultPluginFactory.create(application, DemoPluginFactory.class);
            pluginFactory.getPluginMetas()
                .forEach(pluginMeta -> System.out.println(pluginMeta.getName()));
        }
    }
}
```



### 方式二：手工编码配置

#### 示例代码：

```java
public class DemoPluginFactory extends DefaultPluginFactory {

    public DemoPluginFactory(IPluginConfig pluginConfig) {
        super(pluginConfig, false);
    }
}

@EnableAutoScan
public class Starter {

    static {
        System.setProperty(IApplication.SYSTEM_MAIN_CLASS, Starter.class.getName());
    }

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



### 自定义插件工厂事件处理

自定义插件工厂的事件处理方式与默认插件工厂有所不同，须通过 `IPluginEventListener` 接口实现插件生命周期事件监听，而默认插件工厂也是通过此接口与框架事件服务集成。在前面的示例代码中，用到了一个名称为 `DemoPluginEventListener` 的类，此类就是通过实现该接口进行事件处理的，接口方法及说明如下：

|事件|说明|
|---|---|
|onInitialized|插件初始化完毕将调用此方法|
|onStarted|插件启动完毕后将调用此方法|
|onShutdown|插件停止完毕将调用此方法|
|onDestroy|插件初销毁前将调用此方法|

#### 示例代码：

```java
public class DefaultPluginEventListener implements IPluginEventListener {

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
```

## 主目录及插件结构

插件有两种形式，一种是将插件以JAR包文件形式存储，这类插件可以直接与工程类路径下其它依赖包一起使用，另一种是将插件类文件及插件依赖包等资源放在插件目录结构下，这类插件可以放在工程路径以外，可以多模块共用插件。

每个插件工厂所指定的 `PLUGIN_HOME` 根路径下都可以通过一个名称为 `.plugin` 的目录将一些JAR包或类等资源文件进行全局共享。每个插件都是一个独立的目录，一般以插件唯一标识命名（但不限于），并将插件相关的 `JAR` 包和类文件等资源分别放置在对应的 `lib`、`classes` 或其它目录下，完整的目录结构如下所示：

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



## 插件

通过在一个实现了 `IPlugin` 接口的类上声明 `@Plugin` 注解来创建插件启动类，其将被插件工厂加载和管理，一个插件包可以包括多个插件启动类，每个插件启动类可以实现自己的业务接口对外提供的服务。

### @Plugin注解参数说明

| 配置项      | 描述                                               |
| ----------- | -------------------------------------------------- |
| id          | 插件唯一标识，若未填写则使用初始化类名称，默认为空 |
| name        | 插件名称，默认为空                                 |
| alias       | 插件别名，默认为空                                 |
| author      | 插件作者，默认为空                                 |
| email       | 联系邮箱，默认为空                                 |
| version     | 插件版本，默认为 `1.0.0`                           |
| automatic   | 是否加载后自动启动运行，默认为 `true`              |
| description | 插件描述，默认为空                                 |


### IPlugin接口方法说明

| 方法名称         | 描述                   |
| ---------------- | ---------------------- |
| initialize       | 执行插件初始化         |
| isInitialized    | 判断插件是否已初始化   |
| getPluginContext | 获取插件环境上下文对象 |
| isStarted        | 判断插件是否已启动     |
| startup          | 启动插件               |
| shutdown         | 停止插件               |
| close            | 销毁插件               |



### 创建自定义插件

通过继承框架中提供的 `AbstractPlugin` 抽象类来构建自定义插件，该类是 `IPlugin` 接口的抽象实现，建议直接继承。

本例中定义了一个唯一标识为 `echo_plugin` 的插件并实现了其自定义的业务接口 `IEchoService`，需要注意的是同一个插件可以实现多个业务接口，若多个插件实现同一个业务接口，根据插件加载顺序，最后加载的插件实例对象将替换前者。

示例代码：

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



## 插件的使用

下面的示例代码展示了如何获取自定义的插件实例并调用其业务接口方法：

```java
@EnableAutoScan
public class Starter {

    static {
        System.setProperty(IApplication.SYSTEM_MAIN_CLASS, Starter.class.getName());
    }

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



## 通过依赖注入引用插件

通过 `@PluginRefer` 注解指定注入插件实例，注解参数说明如下：

|配置项|描述|
|---|---|
|value|插件唯一标识或别名，若不指定则默认根据成员对象类型查找插件|

示例代码：

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

    static {
        System.setProperty(IApplication.SYSTEM_MAIN_CLASS, Starter.class.getName());
    }

    public static void main(String[] args) throws Exception {
        try (IApplication application = YMP.run(args)) {
            Demo demo = application.getBeanFactory().getBean(Demo.class);
            demo.getEchoService().sayHi();
        }
    }
}
```



## 插件类加载扩展初始化处理器接口（IPluginBeanLoadInitializer）

插件模块在初始化时将通过指定的类加载器插件类的扫描，为了能够使插件与其它模块更好的集成，有时候需要在它执行加载逻辑之前需要做一些额外的操作，这时我们可以通过插件类加载扩展初始化处理器（IPluginBeanLoadInitializer） 接口完成，该接口是通过`SPI`方式在插件模块启动时自动扫描类和包路径下 `META-INF/services/` 或 `META-INF/services/internal/` 目录中所有名称为 `net.ymate.platform.core.IApplicationInitializer` 的文件并加载文件中指定的所有接口实现类。

该接口可以帮助你完成如下操作：

| 方法名称       | 描述                                           |
| -------------- | ---------------------------------------------- |
| beforeBeanLoad | 当插件对象加载器开始执行加载动作前将调用此方法 |

