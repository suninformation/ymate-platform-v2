---
name: ymp-event
description: YMP框架事件服务模块，支持@EventListener/@EventRegister订阅、自定义事件(继承AbstractEventContext+IEvent)、同步NORMAL/异步ASYNC双模式、priority优先级排序、application.getEvents().fireEvent()广播
version: 2.1.4-dev
author: YMP Team
category: framework
tags:
  - java
  - framework
  - event
  - listener
  - async
  - observer
  - pubsub
trigger: 当用户需要自定义事件类(继承AbstractEventContext)、@EventListener订阅事件(mode/priority)、@EventRegister批量注册、Events.fireEvent()触发、同步NORMAL/异步ASYNC模式切换、事件线程池配置时触发
tools:
  - event-bus
  - event-listener
  - async-dispatch
  - pub-sub
examples:
  - 用户UserEvent自定义事件(USER_CREATED/USER_UPDATED)
  - @EventListener(mode=ASYNC,priority=10)异步高优先级监听
  - @EventRegister批量注册事件与监听器
  - application.getEvents().fireEvent()同步/异步广播
  - 订阅ApplicationEvent/MouleEvent框架内置事件
---

# Event 事件服务技能包

> AI读取指引：本模块边界=事件注册/订阅/广播（均在core包中，非独立模块）；所有类路径前缀`net.ymate.platform.core.event`；Bean/DI跳转core/SKILL.md，插件生命周期事件跳转plugin/SKILL.md。

---

## 0. 快速索引

- Maven artifactId：`ymate-platform-core`（事件集成在core模块中，非独立artifactId）
- 静态入口类：事件管理器通过`application.getEvents()`获取，类型`net.ymate.platform.core.event.Events`
- 必备注解/配置：`@EventListener`(订阅) / `@EventRegister`(批量注册) / `@Event`(自定义事件自动注册) / `@EventsConf`(启动类配置模式+线程池)
- 5行最简事件触发：
```java
try (IApplication app = YMP.run(args)) {
    UserEvent event = new UserEvent(app, UserEvent.EVENT.USER_CREATED).addParamExtend("uid","1001");
    app.getEvents().fireEvent(event);
}
```

## 1. 模块摘要

事件服务通过发布-订阅模式解耦模块业务，消息采用队列存储，支持同步(NORMAL)和异步(ASYNC)两种处理模式，多线程回调执行。支持@EventListener注解自动订阅、@EventRegister手动批量注册、自定义事件类扩展。

- **两种触发模式**：Events.MODE.NORMAL同步阻塞调用 / ASYNC异步线程池分发（默认ASYNC）
- **三种订阅方式**：@EventListener注解（自动扫描）、@EventRegister类（手动批量registerListener）、IApplicationInitializer.afterEventInit（早期启动事件）
- **自定义事件**：继承AbstractEventContext<源类型, 事件枚举> + 实现IEvent接口，内部枚举定义事件名称
- **事件扩展参数**：AbstractEventContext.addParamExtend(key,value) / getParamExtend(key) 传递业务上下文
- **线程池可调**：@EventsConf或ymp.configs.event.thread_pool_size/thread_max_pool_size/thread_queue_size

## 2. 核心注解/类速查表（全限定名）

| 类/注解 | 全限定名 | 核心作用 |
|---|---|---|
| Events | `net.ymate.platform.core.event.Events` | 事件管理器：registerEvent/registerListener/fireEvent；内部枚举MODE={NORMAL,ASYNC} |
| @EventListener | `net.ymate.platform.core.event.annotation.EventListener` | 声明类为事件监听器：value=监听的IEvent类型数组(必填)；mode=NORMAL/ASYNC；priority=int(越大越先执行) |
| @EventRegister | `net.ymate.platform.core.event.annotation.EventRegister` | 声明类实现IEventRegister接口，register(Events events)方法内手动批量注册事件+监听器 |
| IEventRegister | `net.ymate.platform.core.event.IEventRegister` | 批量注册回调接口方法：void register(Events events) throws Exception |
| @Event | `net.ymate.platform.core.event.annotation.Event` | 声明自定义事件类（自动扫描注册，可选，也可手动events.registerEvent） |
| @EventsConf | `net.ymate.platform.core.annotation.EventsConf` | 启动类注解配置：mode默认模式 / threadPoolSize / threadMaxPoolSize / threadQueueSize / providerClass |
| IEventListener<CONTEXT> | `net.ymate.platform.core.event.IEventListener` | 监听器接口：boolean handle(CONTEXT context)；返回true可中断后续链（视provider实现） |
| IEvent | `net.ymate.platform.core.event.IEvent` | 事件标记接口（自定义事件需实现此接口） |
| AbstractEventContext<SOURCE, EVENT extends Enum> | `net.ymate.platform.core.event.AbstractEventContext` | 自定义事件基类：构造(owner, eventClass, eventName)；getSource()事件源；getEventName()事件枚举；addParamExtend/getParamExtend扩展参数 |
| ApplicationEvent | `net.ymate.platform.core.event.ApplicationEvent` | 内置应用容器事件，枚举={APPLICATION_STARTUP, APPLICATION_INITIALIZED, APPLICATION_DESTROYED} |
| ModuleEvent | `net.ymate.platform.core.event.ModuleEvent` | 内置模块事件，枚举={MODULE_STARTUP, MODULE_INITIALIZED, MODULE_DESTROYED} |
| IEventConfig | `net.ymate.platform.core.event.IEventConfig` | 事件配置接口：getDefaultMode()/getThreadPoolSize()等 |
| IEventProvider | `net.ymate.platform.core.event.IEventProvider` | 事件提供者SPI：可替换实现，默认DefaultEventProvider |

## 3. 核心API速查（≤8条最常用）

- `IApplication.getEvents()` → `Events`：获取事件管理器（application初始化后可用）
- `Events.registerEvent(Class<? extends IEvent>)` → `Events`：手动注册自定义事件类（可选，@Event注解已自动）
- `Events.registerListener(Class<IEvent>, IEventListener)` → `Events`：同步默认模式注册监听器
- `Events.registerListener(Events.MODE mode, Class<IEvent>, IEventListener)` → `Events`：指定模式注册监听器（常用）
- `Events.fireEvent(AbstractEventContext context)` → `Events`：触发事件广播（根据listener mode分别同步/异步执行）
- `Events.unregisterListener(Class<IEvent>, Class<IEventListener>)` → `boolean`：注销监听器
- `new AbstractEventContext(owner, eventClass, eventName).addParamExtend(key, val)`：构造自定义事件并挂载参数
- `AbstractEventContext.getSource()` / `getEventName()` / `getParamExtend(key)`：监听器内获取事件源/枚举名/业务参数

## 4. 标准代码模板

### 模板1：自定义UserEvent + @EventListener异步监听 + fireEvent触发

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
package com.example.event;

import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.annotation.EnableAutoScan;
import net.ymate.platform.core.event.AbstractEventContext;
import net.ymate.platform.core.event.Events;
import net.ymate.platform.core.event.IEvent;
import net.ymate.platform.core.event.IEventListener;
import net.ymate.platform.core.event.annotation.EventListener;

/**
 * 自定义用户事件类-包含用户创建/更新/删除三种事件枚举，继承AbstractEventContext并实现IEvent标记接口
 *
 * @author YMP Team
 * @since 2.1.4-dev
 */
public class UserEvent extends AbstractEventContext<Object, UserEvent.EVENT> implements IEvent {

    private static final long serialVersionUID = 1L;

    /**
     * 用户事件枚举-用于标识具体事件类型
     * @since 2.1.4-dev
     */
    public enum EVENT {
        /**
         * 用户已创建
         */
        USER_CREATED,
        /**
         * 用户已更新
         */
        USER_UPDATED,
        /**
         * 用户已删除
         */
        USER_DELETED
    }

    /**
     * 构造用户事件
     *
     * @param owner     事件源对象（通常传IApplication或触发者Bean）
     * @param eventName 事件枚举名称（EVENT.USER_CREATED等）
     * @since 2.1.4-dev
     */
    public UserEvent(Object owner, EVENT eventName) {
        super(owner, UserEvent.class, eventName);
    }
}

/**
 * 用户事件监听器-异步模式，优先级10（越大越先执行），处理所有USER_*事件并根据分支执行业务
 *
 * @author YMP Team
 * @since 2.1.4-dev
 */
@EventListener(mode = Events.MODE.ASYNC, priority = 10, value = UserEvent.class)
public class UserEventListener implements IEventListener<UserEvent> {

    /**
     * 处理用户事件回调
     *
     * @param context 事件上下文（含事件源/枚举/扩展参数）
     * @return 是否中断后续监听器（默认false不中断）
     * @since 2.1.4-dev
     */
    @Override
    public boolean handle(UserEvent context) {
        String uid = (String) context.getParamExtend("uid");
        switch (context.getEventName()) {
            case USER_CREATED:
                System.out.println("[ASYNC] User created, uid=" + uid + " source=" + context.getSource());
                break;
            case USER_UPDATED:
                System.out.println("[ASYNC] User updated, uid=" + uid);
                break;
            case USER_DELETED:
                System.out.println("[ASYNC] User deleted, uid=" + uid);
                break;
        }
        return false;
    }
}

/**
 * 启动类-演示事件触发流程
 *
 * @author YMP Team
 * @since 2.1.4-dev
 */
@EnableAutoScan
class EventDemoStarter {
    static {
        System.setProperty(IApplication.SYSTEM_MAIN_CLASS, EventDemoStarter.class.getName());
    }

    /**
     * 主方法：启动容器后构造UserEvent并触发，监听器异步回调处理
     *
     * @param args CLI参数
     * @throws Exception 启动/触发异常
     * @since 2.1.4-dev
     */
    public static void main(String[] args) throws Exception {
        try (IApplication application = YMP.run(args)) {
            // 触发用户创建事件-携带uid扩展参数
            UserEvent createEvent = new UserEvent(application, UserEvent.EVENT.USER_CREATED)
                    .addParamExtend("uid", "U1001")
                    .addParamExtend("name", "Alice");
            application.getEvents().fireEvent(createEvent);
            // 触发用户更新事件
            application.getEvents().fireEvent(
                    new UserEvent(application, UserEvent.EVENT.USER_UPDATED).addParamExtend("uid", "U1001")
            );
            // 等待异步监听器执行（仅演示用）
            Thread.sleep(500);
        }
    }
}
```

## 5. 配置速查

### 5.1 配置文件最常改项（≤12条 key|默认值|说明）

| 配置项（ymp.configs.event.*） | 默认值 | 说明 |
|---|---|---|
| default_mode | ASYNC | 默认事件触发模式：NORMAL同步 / ASYNC异步 |
| provider_class | net.ymate.platform.core.event.impl.DefaultEventProvider | 事件管理提供者SPI实现（一般无需改） |
| thread_pool_size | Runtime.getRuntime().availableProcessors() | 异步线程池核心大小 |
| thread_max_pool_size | 200 | 异步线程池最大线程数 |
| thread_queue_size | 1024 | 异步线程池队列大小（满后走RejectedPolicy） |

### 5.2 注解配置核心参数

| 注解参数 | 类型 | 说明 |
|---|---|---|
| @EventsConf.mode | Events.MODE | 默认事件模式，覆盖ymp.configs.event.default_mode |
| @EventsConf.threadPoolSize / threadMaxPoolSize / threadQueueSize | int | 同properties对应项 |
| @EventsConf.providerClass | Class<? extends IEventProvider> | 替换事件提供者实现（高级） |
| @EventListener.value | Class<? extends IEvent>[] | 监听的事件类型数组（必填，可多个） |
| @EventListener.mode | Events.MODE | 该监听器独立触发模式（不写则用default_mode） |
| @EventListener.priority | int | 执行优先级，数值越大越先回调，默认0（同级顺序不保证） |
| AbstractEventContext.addParamExtend(String key, Object value) | - | 事件挂载业务参数（监听器内getParamExtend取） |
| AbstractEventContext构造(owner, eventClass, eventName) | - | owner=事件源；eventClass=自定义事件类.class（用于注册匹配）；eventName=枚举实例 |

## 6. 常见坑点排查

| 现象 | 可能原因 | 排查/修复 |
|---|---|---|
| @EventListener监听器不执行handle() | 未@EnableAutoScan；监听器所在包未被扫描；事件未注册；监听器类未加@EventListener或value不匹配事件类型 | 确认启动类@EnableAutoScan，监听器包在value范围；检查@EventListener.value是否包含触发的事件类型；自定义事件类建议加@Event或在@EventRegister中events.registerEvent(XxxEvent.class) |
| 异步事件异常被吞/堆栈丢失 | ASYNC模式下线程池执行未捕获异常，DefaultEventProvider日志级别 | 监听器handle()内部自行try-catch并记录log（推荐）；检查LOG.error是否开启；如需全局异常处理可自定义IEventProvider |
| 同步事件阻塞主流程 | 误把耗时IO/DB操作放在NORMAL模式监听器 | 耗时操作@EventListener(mode=ASYNC)；关键同步流程才用NORMAL；默认模式为ASYNC不要乱改default_mode |
| APPLICATION_STARTUP/MODULE_STARTUP事件收不到 | @EventListener/@EventRegister由AutoScan加载，时机晚于Startup事件（模块初始化在扫描之前） | 要订阅启动早期事件：实现IApplicationInitializer，在afterEventInit()中events.registerListener注册；在YMP.run(args, new DemoAppInit())中传入初始器 |
| priority顺序不生效 | 同事件多个监听器priority相同；不同监听器mode混用(同步/异步分开排队) | priority值拉开差距（如10/20/30）；文档已说明"当某个事件被触发后，订阅该事件的接口被回调执行的顺序是不能被保证的"，不依赖顺序做业务 |
| fireEvent后异步监听器单元测试中断言失败 | 测试主线程先退出，异步任务未执行完 | 单元测试中触发后Thread.sleep(200)或用CountDownLatch等待；同步模式测试更可靠@EventListener(mode=NORMAL) |
