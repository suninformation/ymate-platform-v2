---
name: ymp-core
description: YMP框架核心容器模块，提供IApplication应用容器、@Bean/@Inject依赖注入(DI)、自动扫描、事件机制、国际化(I18N)等核心基础设施
version: 2.1.4-dev
author: YMP Team
category: framework
tags:
  - java
  - framework
  - core
  - di
  - ioc
  - event
  - i18n
  - bean
trigger: 当用户需要创建YMP应用启动类、@Bean注册与@Inject依赖注入、YMP.run()启动容器、@EnableAutoScan自动扫描、IApplication生命周期管理、事件监听/触发、I18N多语言时触发；AOP/拦截器问题请跳转core-aop模块
tools:
  - dependency-injection
  - event-management
  - i18n
  - application-container
examples:
  - 基于注解创建标准YMP启动类Starter
  - @Bean业务服务 + @Inject字段注入实现DI
  - 通过application.getBeanFactory()获取容器Bean
  - @EventListener订阅事件 + Events.fireEvent()触发事件
  - I18N资源加载与多语言消息获取
---

# Core 核心容器技能包

> AI读取指引：本模块边界=应用容器(IApplication)+Bean管理(IoC/DI)+事件广播+I18N；凡是拦截器/AOP/@Before/@After/@EnableBeanProxy相关 → 立即跳转 core-aop/SKILL.md；配置体系→configuration/SKILL.md；持久化/Web→对应子模块。

---

## 0. 快速索引（AI一眼定位）

- Maven artifactId：`ymate-platform-core`
- 启动入口类：`net.ymate.platform.core.YMP`（静态方法 `YMP.run(args)`）、`net.ymate.platform.core.IApplication`（容器实例接口）
- 必须启用注解：`@EnableAutoScan`（类扫描/注册@Bean）、按需加 `@EnableBeanProxy`（需AOP时）
- 典型调用示例（5行内）：
```java
@EnableAutoScan
public class Starter {
    static { System.setProperty(IApplication.SYSTEM_MAIN_CLASS, Starter.class.getName()); }
    public static void main(String[] args) throws Exception {
        try (IApplication app = YMP.run(args)) {
            MyService svc = app.getBeanFactory().getBean(MyService.class);
        }
    }
}
```

## 1. 模块摘要（1-2句 + 5条以内核心能力）

YMP框架核心应用容器，负责初始化、生命周期管理、对象IoC/DI、事件广播与国际化资源。所有其它YMP模块都依赖此模块。

- **应用容器**：`IApplication` + `YMP.run()` 提供启动/销毁、try-with-resources自动关闭
- **自动扫描+Bean管理**：`@EnableAutoScan` 扫描 `@Bean` 类，`IBeanFactory` 统一注册/获取
- **依赖注入(DI)**：`@Inject` 字段注入，`@By` 指定多实现场景下的具体类型
- **事件机制**：`@EventListener` 订阅 / `Events.fireEvent()` 广播，同步(NORMAL)+异步(ASYNC)双模式
- **国际化(I18N)**：`I18N.get(key, locale, params)` 多语言资源加载与格式化

## 2. 核心注解速查表（最关键，必须包含全限定名）

| 注解 | 全限定名 | 作用目标 | 常用参数（2-5个核心参数名+说明） |
|---|---|---|---|
| @Bean | `net.ymate.platform.core.beans.annotation.Bean` | 类 | singleton=true(单例)/handler=自定义IBeanHandler类型 |
| @Inject | `net.ymate.platform.core.beans.annotation.Inject` | 字段/构造参数 | - |
| @By | `net.ymate.platform.core.beans.annotation.By` | 字段（配合@Inject） | value=指定实现Class（多实现歧义时） |
| @Ignored | `net.ymate.platform.core.beans.annotation.Ignored` | 类/方法/包 | -（自动扫描时跳过） |
| @EnableAutoScan | `net.ymate.platform.core.annotation.EnableAutoScan` | 启动类 | value=扫描包数组/excluded=排除包/excludedModules=排除模块/factoryClass=IBeanLoadFactory |
| @EnableBeanProxy | `net.ymate.platform.core.annotation.EnableBeanProxy` | 启动类 | factoryClass=IProxyFactory（Default/Javassist/ByteBuddy/NoOp） |
| @EnableDevMode | `net.ymate.platform.core.annotation.EnableDevMode` | 启动类 | -（等价ymp.dev_mode=true） |
| @EventListener | `net.ymate.platform.core.event.annotation.EventListener` | 监听器类 | value=监听的IEvent类型数组/mode=NORMAL/ASYNC |
| @EventRegister | `net.ymate.platform.core.event.annotation.EventRegister` | 类（实现IEventRegister） | - |
| @Event | `net.ymate.platform.core.event.annotation.Event` | 自定义事件类 | -（自动注册） |
| @ParamValue | `net.ymate.platform.core.annotation.ParamValue` | 字段/方法参数 | value=参数名/defaultValue=默认值/replaceEnvVariable=是否替换环境变量 |
| @Params/@Param | `net.ymate.platform.core.annotation.Params` + `@Param` | 启动类 | @Param.name/@Param.value（等价ymp.params.xxx） |
| @EventsConf | `net.ymate.platform.core.annotation.EventsConf` | 启动类 | mode=默认模式/threadPoolSize/threadMaxPoolSize/threadQueueSize/providerClass |
| @I18nConf | `net.ymate.platform.core.annotation.I18nConf` | 启动类 | defaultLocale=默认语言/eventHandlerClass=II18nEventHandler |
| @DefaultPasswordProcessClass | `net.ymate.platform.core.annotation.DefaultPasswordProcessClass` | 启动类 | value=IPasswordProcessor实现 |

## 3. 核心API速查（仅入口静态类+最常用方法）

- `YMP.run(String... args)` → `IApplication`：标准启动方式，返回应用容器实例（支持try-with-resources）
- `YMP.run(String[] args, IApplicationInitializer... initializers)` → `IApplication`：带扩展初始化处理器启动
- `IApplication.isInitialized()` → `boolean`：判断容器是否初始化成功
- `IApplication.getBeanFactory()` → `IBeanFactory`：获取对象工厂，用于Bean注册/获取
- `IBeanFactory.getBean(Class<T> clazz)` → `T`：按类型获取Bean实例（接口或实现类）
- `IBeanFactory.registerBean(Class<?> clazz)` / `registerBean(BeanMeta)`：手动注册Bean
- `IApplication.getEvents()` → `Events`：获取事件管理器
- `Events.fireEvent(IEvent event)` → `void`：广播事件
- `Events.registerListener(Class<IEvent>, IEventListener)` / `registerListener(MODE, Class, IEventListener)`：手动注册监听器
- `IApplication.getI18n()` → `I18N`
- `I18N.get(String key)` / `get(key, Locale)` / `get(key, Object... params)` → `String`：获取国际化消息

## 4. 标准代码模板（最少可运行，带import+License+类/方法注释+@since）

### 模板1：最简注解配置Starter启动类

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
import net.ymate.platform.core.annotation.EventsConf;
import net.ymate.platform.core.annotation.I18nConf;
import net.ymate.platform.core.event.Events;

/**
 * YMP应用标准启动类示例。
 *
 * @author YMP Team
 * @since 2.1.4-dev
 */
@EnableAutoScan("com.example")
@EnableBeanProxy
@EnableDevMode
@EventsConf(mode = Events.MODE.ASYNC, threadPoolSize = 10)
@I18nConf(defaultLocale = "zh_CN")
public class Starter {

    static {
        System.setProperty(IApplication.SYSTEM_MAIN_CLASS, Starter.class.getName());
    }

    /**
     * 应用入口方法。
     *
     * @param args 命令行启动参数
     * @throws Exception 启动过程异常
     * @since 2.1.4-dev
     */
    public static void main(String[] args) throws Exception {
        try (IApplication application = YMP.run(args)) {
            if (application.isInitialized()) {
                // TODO: 在此处编写初始化后业务逻辑
            }
        }
    }
}
```

### 模板2：@Bean业务服务 + @Inject字段注入

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
import net.ymate.platform.core.beans.annotation.By;
import net.ymate.platform.core.beans.annotation.Inject;

/**
 * 用户业务服务示例（@Bean注册到容器，支持依赖注入）。
 *
 * @author YMP Team
 * @since 2.1.4-dev
 */
@Bean
public class UserService {

    @Inject
    @By(OrderServiceImpl.class)
    private IOrderService orderService;

    /**
     * 根据用户ID查询用户名称（演示调用注入的Bean）。
     *
     * @param userId 用户ID
     * @return 用户名称拼接订单数
     * @since 2.1.4-dev
     */
    public String getUserName(String userId) {
        int count = orderService.countByUser(userId);
        return "User:" + userId + " -> Orders:" + count;
    }
}
```

### 模板3：@EventListener监听器

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
package com.example.listener;

import net.ymate.platform.core.event.Events;
import net.ymate.platform.core.event.IEventListener;
import net.ymate.platform.core.event.ModuleEvent;
import net.ymate.platform.core.event.annotation.EventListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 模块生命周期事件监听器示例（异步模式）。
 *
 * @author YMP Team
 * @since 2.1.4-dev
 */
@EventListener(mode = Events.MODE.ASYNC, value = ModuleEvent.class)
public class ModuleEventListener implements IEventListener<ModuleEvent> {

    private static final Log LOG = LogFactory.getLog(ModuleEventListener.class);

    /**
     * 处理模块事件。
     *
     * @param context 模块事件上下文
     * @return 返回true则停止后续监听器执行
     * @since 2.1.4-dev
     */
    @Override
    public boolean handle(ModuleEvent context) {
        String moduleName = context.getSource().getName();
        switch (context.getEventName()) {
            case MODULE_STARTUP:
                LOG.info("Module startup: " + moduleName);
                break;
            case MODULE_INITIALIZED:
                LOG.info("Module initialized: " + moduleName);
                break;
            case MODULE_DESTROYED:
                LOG.info("Module destroyed: " + moduleName);
                break;
            default:
                break;
        }
        return false;
    }
}
```

## 5. 配置速查（ymp-conf.properties / 注解配置）

### 5.1 配置文件常用项（ymp-conf.properties，放classpath根目录）

| 配置key | 默认值 | 说明 |
|---|---|---|
| `ymp.dev_mode` | `false` | 是否开发模式（更多日志/热加载） |
| `ymp.packages` | `net.ymate.platform` | 自动扫描包，多包用`\|`分隔，已含主类所在包 |
| `ymp.excluded_packages` | 无 | 扫描排除包，多包用`\|`分隔 |
| `ymp.excluded_files` | 无 | 扫描排除JAR/ZIP文件名，`\|`分隔 |
| `ymp.excluded_modules` | 无 | 排除加载的模块类名，`\|`分隔 |
| `ymp.included_modules` | 无 | 仅加载包含的模块类名，`\|`分隔 |
| `ymp.default_locale` | 系统语言 | I18N默认语言，如`zh_CN`/`en_US` |
| `ymp.default_password_process_class` | 空 | IPasswordProcessor实现类（配置加密用） |
| `ymp.params.xxx` | 无 | 自定义扩展参数（@ParamValue("xxx")注入） |
| `ymp.configs.event.default_mode` | `ASYNC` | 默认事件模式：NORMAL同步/ASYNC异步 |
| `ymp.configs.event.thread_pool_size` | CPU核心数 | 事件线程池初始化大小 |
| `ymp.configs.event.thread_max_pool_size` | `200` | 事件线程池最大线程数 |
| `ymp.configs.event.thread_queue_size` | `1024` | 事件线程池队列大小 |
| `ymp.intercept.settings_enabled` | `false` | 是否开启拦截器全局规则（AOP用） |

### 5.2 启动注解配置（标注在启动类上）

- `@EnableAutoScan`：value=扫描包数组、excluded=排除包、factoryClass=自定义BeanLoadFactory
- `@EnableBeanProxy`：factoryClass=代理工厂（`DefaultProxyFactory` CGLIB / `JavassistProxyFactory` / `ByteBuddyProxyFactory` / `NoOpProxyFactory` 禁用AOP）
- `@EventsConf`：mode=默认模式、threadPoolSize / threadMaxPoolSize / threadQueueSize、providerClass=IEventProvider
- `@I18nConf`：defaultLocale=默认语言、eventHandlerClass=II18nEventHandler
- `@Params({@Param(name="k",value="v")})`：自定义全局参数
- `@DefaultPasswordProcessClass`：value=IPasswordProcessor实现

## 6. 常见坑点排查（3-6条）

| 坑现象 | 原因 | 解决办法 |
|---|---|---|
| @Inject注入字段为null | ①类无@Bean未注册 ②未@EnableAutoScan或扫描包不含该类 ③对象是new出来而非从BeanFactory获取 | 类加@Bean；启动类加@EnableAutoScan并指定正确value；通过 `app.getBeanFactory().getBean(X.class)` 获取实例 |
| 接口多实现时注入报错/取到非期望实现 | 容器里同接口多个@Bean，按注册顺序取最后一个 | 注入字段上加`@By(具体实现Class.class)`显式指定 |
| 启动类注解配置不生效 | ①static块未设置SYSTEM_MAIN_CLASS ②配置文件ymp-conf.properties中同key有非空值（配置文件优先） | static{System.setProperty(IApplication.SYSTEM_MAIN_CLASS, Starter.class.getName());}；需注解生效则清空配置文件对应项 |
| @EventListener订阅APPLICATION_STARTUP/MODULE_STARTUP不触发 | 自动扫描在模块初始化后才执行，注解订阅注册太晚 | 实现 `IApplicationInitializer` 在 `afterEventInit()` 中手动 `events.registerListener(...)` 注册 |
| 打包后找不到配置类/Bean未注册 | JVM未指定mainClass，或启动类不在扫描范围 | 启动参数加 `-Dymp.mainClass=com.example.Starter`，或static块强制设SYSTEM_MAIN_CLASS |
| I18N.get(key)返回key本身而非值 | resources/i18n/下无对应properties文件或命名不对，或未被打包进classpath | 按 messages_zh_CN.properties 命名放 resources/i18n/，检查maven-resources配置 |

## 7. 本模块注解全限定名索引（AI拼import备用）

| 短名 | 全限定名 |
|---|---|
| @Bean | `net.ymate.platform.core.beans.annotation.Bean` |
| @Inject | `net.ymate.platform.core.beans.annotation.Inject` |
| @By | `net.ymate.platform.core.beans.annotation.By` |
| @Ignored | `net.ymate.platform.core.beans.annotation.Ignored` |
| @Order | `net.ymate.platform.core.beans.annotation.Order` |
| @Injector | `net.ymate.platform.core.beans.annotation.Injector` |
| @PropertyState | `net.ymate.platform.core.beans.annotation.PropertyState` |
| @Proxy | `net.ymate.platform.core.beans.annotation.Proxy` |
| @CleanProxy | `net.ymate.platform.core.beans.annotation.CleanProxy` |
| @EnableAutoScan | `net.ymate.platform.core.annotation.EnableAutoScan` |
| @EnableBeanProxy | `net.ymate.platform.core.annotation.EnableBeanProxy` |
| @EnableDevMode | `net.ymate.platform.core.annotation.EnableDevMode` |
| @EventsConf | `net.ymate.platform.core.annotation.EventsConf` |
| @I18nConf | `net.ymate.platform.core.annotation.I18nConf` |
| @Param | `net.ymate.platform.core.annotation.Param` |
| @Params | `net.ymate.platform.core.annotation.Params` |
| @ParamValue | `net.ymate.platform.core.annotation.ParamValue` |
| @DefaultPasswordProcessClass | `net.ymate.platform.core.annotation.DefaultPasswordProcessClass` |
| @EventListener | `net.ymate.platform.core.event.annotation.EventListener` |
| @EventRegister | `net.ymate.platform.core.event.annotation.EventRegister` |
| @Event | `net.ymate.platform.core.event.annotation.Event` |

| 常用入口类 | 全限定名 |
|---|---|
| YMP | `net.ymate.platform.core.YMP` |
| IApplication | `net.ymate.platform.core.IApplication` |
| IBeanFactory | `net.ymate.platform.core.beans.IBeanFactory` |
| BeanMeta | `net.ymate.platform.core.beans.BeanMeta` |
| IBeanInitializer | `net.ymate.platform.core.beans.IBeanInitializer` |
| IBeanInjector | `net.ymate.platform.core.beans.IBeanInjector` |
| Events | `net.ymate.platform.core.event.Events` |
| IEvent | `net.ymate.platform.core.event.IEvent` |
| IEventListener | `net.ymate.platform.core.event.IEventListener` |
| IEventRegister | `net.ymate.platform.core.event.IEventRegister` |
| AbstractEventContext | `net.ymate.platform.core.event.AbstractEventContext` |
| ApplicationEvent | `net.ymate.platform.core.ApplicationEvent` |
| ModuleEvent | `net.ymate.platform.core.module.ModuleEvent` |
| IApplicationInitializer | `net.ymate.platform.core.IApplicationInitializer` |
| I18N | `net.ymate.platform.core.i18n.I18N` |
