---
name: ymp-plugin
description: YMP框架插件模块，采用独立ClassLoader管理私有包/类/资源，支持@Plugin注解声明、插件生命周期、默认插件工厂与自定义插件工厂、@PluginRefer依赖注入
version: 2.1.4-dev
author: YMP Team
category: framework
tags:
  - java
  - framework
  - plugin
  - classloader
  - hotswap
  - lifecycle
  - ioc
trigger: 当用户需要创建@Plugin插件类、AbstractPlugin生命周期实现(doInitialize/doStart/doStop/doDestroy)、Plugins.get().getPlugin()获取插件、自定义插件工厂DefaultPluginFactory、@PluginRefer注入、IPluginContext上下文时触发
tools:
  - plugin-factory
  - classloader-isolation
  - lifecycle-management
  - hot-deploy
examples:
  - @Plugin(id/name/version)声明插件类
  - 继承AbstractPlugin实现IEchoService业务接口
  - Plugins.get().getPlugin(IEchoService.class)调用插件
  - DefaultPluginFactory.create()自定义插件工厂
  - @PluginRefer注解依赖注入插件
---

# Plugin 插件技能包

> AI读取指引：本模块边界=插件ClassLoader隔离+@Plugin声明+生命周期+插件工厂；所有类路径前缀`net.ymate.platform.plugin`；依赖core模块，事件/DI跳转core/SKILL.md。

---

## 0. 快速索引

- Maven artifactId：`ymate-platform-plugin`
- 静态入口类：`net.ymate.platform.plugin.Plugins`（Plugins.get().getPlugin(Class/String)）
- 必备注解/配置：`@Plugin`(声明插件) + `@PluginConf`(配置) / `ymp.configs.plugin.*`(properties)
- 5行最简插件调用：
```java
@EnableAutoScan
public class Starter {
    static { System.setProperty(IApplication.SYSTEM_MAIN_CLASS, Starter.class.getName()); }
    public static void main(String[] args) throws Exception {
        try (IApplication app = YMP.run(args)) {
            IEchoService echo = Plugins.get().getPlugin(IEchoService.class);
            echo.sayHi();
        }
    }
}
```

## 1. 模块摘要

插件模块通过独立ClassLoader隔离管理私有JAR/类/资源，实现业务接口细粒度拆分与可重用封装。插件工厂（IPluginFactory）负责分析、加载、初始化及生命周期管理。支持默认插件工厂（自动集成框架）与自定义插件工厂（多实例独立）。

- **ClassLoader隔离**：每个插件独立ClassLoader，.plugin目录共享资源，插件间通过业务接口通信
- **@Plugin注解声明**：id/name/version/automatic/author等元数据，可多插件实现同一接口（后加载覆盖前者）
- **生命周期管理**：AbstractPlugin.doInitialize/doStartup/doShutdown/doDestroy四阶段钩子
- **两种插件工厂**：默认Plugins.get()单例集成框架；DefaultPluginFactory.create()手动构建多实例
- **@PluginRefer注入**：配合@Bean，按类型或value=插件ID/别名注入插件实例

## 2. 核心注解/类速查表（全限定名）

| 类/注解 | 全限定名 | 核心作用 |
|---|---|---|
| Plugins | `net.ymate.platform.plugin.Plugins` | 默认插件模块管理器（IModule实现），Plugins.get()获取单例 |
| IPlugins | `net.ymate.platform.plugin.IPlugins` | 插件模块接口，含getPlugin(Class)/getPlugin(String) |
| @Plugin | `net.ymate.platform.plugin.annotation.Plugin` | 声明插件类（实现IPlugin接口），参数id/name/alias/author/email/version/automatic/description |
| AbstractPlugin | `net.ymate.platform.plugin.AbstractPlugin` | IPlugin抽象实现，继承后覆写doInitialize/doStartup/doShutdown/doDestroy |
| IPlugin | `net.ymate.platform.plugin.IPlugin` | 插件接口：initialize/isInitialized/startup/isStarted/shutdown/close/getPluginContext |
| IPluginContext | `net.ymate.platform.plugin.IPluginContext` | 插件环境上下文：getPluginMeta()/getOwner()/getFactory()/getClassLoader() |
| PluginMeta | `net.ymate.platform.plugin.PluginMeta` | 插件元数据（@Plugin解析结果）：getId()/getName()/getVersion()/getPluginClass() |
| IPluginFactory | `net.ymate.platform.plugin.IPluginFactory` | 插件工厂接口：getPlugin/getPluginMetas/initialize/close |
| DefaultPluginFactory | `net.ymate.platform.plugin.impl.DefaultPluginFactory` | 默认插件工厂实现，DefaultPluginFactory.create(app, class, cfg)构建自定义工厂 |
| @PluginFactory | `net.ymate.platform.plugin.annotation.PluginFactory` | 注解配置自定义工厂：pluginHome/packageNames/automatic/listenerClass/loaderFactoryClass |
| @PluginConf | `net.ymate.platform.plugin.annotation.PluginConf` | 默认插件工厂注解配置：enabled/pluginHome/packageNames/excludedPackageNames/excludedFileNames/includeClasspath/automatic |
| @PluginRefer | `net.ymate.platform.plugin.annotation.PluginRefer` | Bean字段注入插件，value=插件ID/别名（省略则按类型） |
| IPluginEventListener | `net.ymate.platform.plugin.IPluginEventListener` | 插件生命周期事件监听：onInitialized/onStarted/onShutdown/onDestroy |
| IPluginConfig | `net.ymate.platform.plugin.IPluginConfig` | 插件配置接口 |
| DefaultPluginConfig | `net.ymate.platform.plugin.impl.DefaultPluginConfig` | 插件配置Builder：DefaultPluginConfig.create().pluginHome().automatic().autoscanPackages().eventListener() |
| IEchoService（文档示例接口） | - | 业务接口示例，插件实现后通过Plugins.get().getPlugin(IEchoService.class)获取 |

## 3. 核心API速查（≤8条最常用）

- `Plugins.get()` → `IPlugins`：获取默认插件工厂管理器（单例）
- `Plugins.get().getPlugin(Class<T> serviceClass)` → `T`：按业务接口类型获取插件实例（推荐）
- `Plugins.get().getPlugin(String idOrAlias)` → `IPlugin`：按插件唯一标识或别名获取插件实例
- `DefaultPluginFactory.create(IApplication, Class<? extends DefaultPluginFactory>)` → `IPluginFactory`：基于@PluginFactory注解创建自定义工厂
- `DefaultPluginFactory.create(IApplication, Class<? extends DefaultPluginFactory>, IPluginConfig)` → `IPluginFactory`：手工配置创建自定义工厂
- `DefaultPluginConfig.create().pluginHome(File).automatic(boolean).autoscanPackages(List).eventListener(IPluginEventListener).build()`：构建自定义配置
- `IPluginContext.getPluginMeta()` / `getOwner()` / `getFactory()` / `getClassLoader()`：插件内获取上下文信息
- `application.getBeanFactory().getBean(Xxx.class).getEchoService()`：通过@PluginRefer注入的Bean调用插件

## 4. 标准代码模板

### 模板1：@Plugin插件实现（继承AbstractPlugin+业务接口IEchoService+四阶段生命周期+调用方）

```java
/*
 * Copyright 2007-2019 the original author or authors.
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
package com.example.plugin;

import net.ymate.platform.plugin.AbstractPlugin;
import net.ymate.platform.plugin.IPluginContext;
import net.ymate.platform.plugin.annotation.Plugin;

/**
 * 业务接口-插件对外暴露的服务契约（通常定义在主程序或公共API包）
 *
 * @author YMP Team
 * @since 2.1.4-dev
 */
interface IEchoService {
    /**
     * 打招呼
     * @since 2.1.4-dev
     */
    void sayHi();
}

/**
 * Echo插件实现类-通过@Plugin声明元数据，继承AbstractPlugin覆写生命周期钩子，实现IEchoService业务接口
 *
 * @author YMP Team
 * @since 2.1.4-dev
 */
@Plugin(
    id = "echo_plugin",
    name = "EchoDemoPlugin",
    author = "YMP Team",
    email = "suninformation@163.com",
    version = "1.0.0",
    automatic = true,
    description = "示例插件：实现IEchoService"
)
public class EchoPlugin extends AbstractPlugin implements IEchoService {

    /**
     * 插件初始化阶段钩子（对应IPlugin.initialize）
     *
     * @param context 插件上下文
     * @throws Exception 初始化异常
     * @since 2.1.4-dev
     */
    @Override
    protected void doInitialize(IPluginContext context) throws Exception {
        System.out.println("EchoPlugin initialized, id=" + context.getPluginMeta().getId());
    }

    /**
     * 插件启动阶段钩子（对应IPlugin.startup）
     *
     * @throws Exception 启动异常
     * @since 2.1.4-dev
     */
    @Override
    protected void doStartup() throws Exception {
        System.out.println("EchoPlugin started.");
    }

    /**
     * 插件停止阶段钩子（对应IPlugin.shutdown）
     *
     * @throws Exception 停止异常
     * @since 2.1.4-dev
     */
    @Override
    protected void doShutdown() throws Exception {
        System.out.println("EchoPlugin shutdown.");
    }

    /**
     * 插件销毁阶段钩子（对应IPlugin.close）
     *
     * @throws Exception 销毁异常
     * @since 2.1.4-dev
     */
    @Override
    protected void doDestroy() throws Exception {
        System.out.println("EchoPlugin destroyed.");
    }

    /**
     * IEchoService业务方法实现
     *
     * @since 2.1.4-dev
     */
    @Override
    public void sayHi() {
        System.out.println("Hi, from EchoPlugin!");
    }
}

/*
 * ==================== 调用方代码（主程序中） ====================
 * 方式一：直接通过Plugins.get()获取
 * 方式二：@PluginRefer注入到@Bean类中
 */
// ---------- 方式一：主程序直接调用 ----------
// package com.example.app;
// import net.ymate.platform.core.IApplication;
// import net.ymate.platform.core.YMP;
// import net.ymate.platform.core.annotation.EnableAutoScan;
// import net.ymate.platform.plugin.Plugins;
// @EnableAutoScan
// public class AppStarter {
//     static { System.setProperty(IApplication.SYSTEM_MAIN_CLASS, AppStarter.class.getName()); }
//     public static void main(String[] args) throws Exception {
//         try (IApplication app = YMP.run(args)) {
//             IEchoService echo = Plugins.get().getPlugin(IEchoService.class);
//             echo.sayHi();
//         }
//     }
// }

// ---------- 方式二：@PluginRefer注入 ----------
// package com.example.app;
// import net.ymate.platform.core.beans.annotation.Bean;
// import net.ymate.platform.core.beans.annotation.Inject;
// import net.ymate.platform.plugin.annotation.PluginRefer;
// @Bean
// public class DemoService {
//     @PluginRefer
//     private IEchoService echoService;
//     public void run() { echoService.sayHi(); }
// }
```

## 5. 配置速查

### 5.1 配置文件最常改项（≤12条 key|默认值|说明）

| 配置项（ymp.configs.plugin.*） | 默认值 | 说明 |
|---|---|---|
| enabled | true | 是否启用插件模块（禁用则不创建默认工厂） |
| plugin_home | ${root}/plugins | 插件主目录路径（内含<plugin_id>/lib、<plugin_id>/classes等） |
| package_names | 主程序类所在包 | 插件自动扫描包名前缀集合，多个用\|分隔 |
| excluded_package_names | - | 扫描时排除包名集合，\|分隔 |
| excluded_file_names | - | 扫描时排除JAR/ZIP文件名集合，\|分隔 |
| automatic | true | 是否允许插件自动启动（否则需手动plugin.startup()） |
| included_classpath | false | 是否加载当前CLASSPATH中含插件配置的JAR包 |
| ymp.params.plugin.xxx | - | 禁用指定插件xxx（值=disabled），xxx为插件ID或类名 |

### 5.2 注解配置核心参数

| 注解参数 | 类型 | 说明 |
|---|---|---|
| @Plugin.id | String | 插件唯一标识（未填则用初始化类名） |
| @Plugin.name | String | 插件名称 |
| @Plugin.version | String | 插件版本，默认1.0.0 |
| @Plugin.automatic | boolean | 是否加载后自动启动，默认true |
| @Plugin.author / @Plugin.email / @Plugin.description | String | 插件作者/邮箱/描述 |
| @PluginConf.enabled / @PluginConf.automatic | boolean | 同properties |
| @PluginConf.pluginHome / @PluginConf.packageNames | String / String[] | 同properties |
| @PluginConf.includeClasspath / @PluginConf.excludedPackageNames / @PluginConf.excludedFileNames | boolean / String[] / String[] | 同properties |
| @PluginFactory.pluginHome | String | 自定义插件工厂的插件存放路径（必填） |
| @PluginFactory.packageNames | String[] | 自动扫描包名，默认工厂所在包 |
| @PluginFactory.automatic / @PluginFactory.listenerClass / @PluginFactory.loaderFactoryClass | boolean / Class / Class | 自动启动/事件监听器类/加载器工厂类 |
| @PluginRefer.value | String | 插件ID或别名（不填则按字段类型匹配） |

## 6. 常见坑点排查

| 现象 | 可能原因 | 排查/修复 |
|---|---|---|
| Plugins.get().getPlugin()返回null/插件未加载 | package_names未覆盖插件包；ymp.configs.plugin.enabled=false；ymp.params.plugin.xxx=disabled | 检查@EnableAutoScan是否配置；log中搜索"plugin"关键词确认扫描路径；确认未被disabled；JAR插件确保META-INF/services中配置正确 |
| ClassCastException: Xxx cannot be cast to Xxx | 插件ClassLoader与主程序ClassLoader隔离，同一接口被两个ClassLoader加载为不同Class | 业务接口(如IEchoService)必须放在主程序CLASSPATH（或.plugin共享目录），禁止放在插件私有lib/classes中；插件实现接口，主程序仅依赖接口类型 |
| 插件与主程序依赖冲突（NoSuchMethod/LinkageError） | 插件私有lib中与主程序含不同版本的同一JAR | 将共用JAR移至${plugin_home}/.plugin/lib共享；或在插件中排除与主程序冲突的依赖；使用excluded_file_names排除问题JAR |
| @PluginRefer注入为null | DemoService未加@Bean或未被扫描；插件尚未初始化完成 | 确保调用方类有@Bean且@EnableAutoScan包覆盖；避免在@Bean构造器中直接调用（依赖注入在对象创建后执行），改用IBeanInitializer.afterInitialized() |
| 自定义DefaultPluginFactory加载不到插件 | pluginHome路径不存在/权限；packageNames未配置；未执行初始化 | 检查路径绝对路径是否正确；DefaultPluginFactory.create()后工厂已自动initialize；自定义@PluginFactory注解需在继承DefaultPluginFactory的类上声明 |
