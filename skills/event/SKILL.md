# Event 模块技能文档

## 1. 模块概述

Event 模块是 YMP 框架中的一个核心模块，提供了事件驱动的编程模型。该模块通过事件的注册、订阅和广播机制，实现了模块之间的解耦，降低了代码的侵入性，提高了系统的可维护性和可扩展性。

**主要功能特点：**

- 支持事件的注册、订阅和广播
- 支持同步（NORMAL）和异步（ASYNC）两种事件处理模式
- 基于多线程的事件处理机制
- 支持事件监听器的优先级设置
- 支持事件的过滤和拦截
- 与框架其他模块无缝集成

## 2. 核心功能

### 2.1 事件机制

Event 模块的核心是事件机制，通过以下组件实现：

- **事件对象（Event）**：表示一个具体的事件，包含事件的类型、源对象、事件名称等信息。
- **事件监听器（EventListener）**：监听并处理特定类型的事件。
- **事件管理器（Events）**：负责事件的注册、订阅和广播。

### 2.2 事件处理模式

模块支持两种事件处理模式：

- **同步模式（NORMAL）**：事件广播后，立即执行所有监听器的处理方法，直到所有监听器处理完毕。
- **异步模式（ASYNC）**：事件广播后，将事件放入队列，由后台线程池异步执行监听器的处理方法。

### 2.3 事件监听器

事件监听器是实现了 `IEventListener` 接口的类，用于处理特定类型的事件。监听器可以通过以下方式注册：

- 通过 `@EventListener` 注解声明
- 通过 `@EventRegister` 注解声明
- 通过 `IApplicationInitializer` 接口注册

### 2.4 事件优先级

事件监听器可以设置优先级，优先级高的监听器会先执行。优先级通过 `@EventListener` 注解的 `priority` 参数设置，默认值为 0，值越大优先级越高。

## 3. API 接口

### 3.1 核心接口

#### IEventListener

事件监听器接口，是所有事件监听器的基础接口。

```java
public interface IEventListener<E extends IEvent> {

    /**
     * 处理事件
     * @param context 事件上下文
     * @return 是否继续处理其他监听器
     * @throws Exception 异常
     */
    boolean handle(E context) throws Exception;
}
```

#### IEvent

事件接口，表示一个具体的事件。

```java
public interface IEvent {

    /**
     * 获取事件源对象
     * @return 事件源对象
     */
    Object getSource();

    /**
     * 获取事件名称
     * @return 事件名称
     */
    Enum<?> getEventName();

    /**
     * 获取事件创建时间
     * @return 事件创建时间
     */
    long getTimestamp();
}
```

#### Events

事件管理器类，负责事件的注册、订阅和广播。

```java
public interface Events {

    /**
     * 广播事件
     * @param event 事件对象
     * @return 是否有监听器处理了该事件
     */
    boolean fireEvent(IEvent event);

    /**
     * 注册事件监听器
     * @param mode 事件处理模式
     * @param eventClass 事件类型
     * @param listener 事件监听器
     * @param priority 优先级
     */
    void registerListener(Mode mode, Class<? extends IEvent> eventClass, IEventListener<? extends IEvent> listener, int priority);

    /**
     * 注销事件监听器
     * @param eventClass 事件类型
     * @param listener 事件监听器
     */
    void unregisterListener(Class<? extends IEvent> eventClass, IEventListener<? extends IEvent> listener);
}
```

### 3.2 事件对象

#### ApplicationEvent

应用容器事件，包含应用容器的启动、初始化和销毁事件。

| 事件枚举名称            | 描述                 |
| ----------------------- | -------------------- |
| APPLICATION_STARTUP     | 应用容器启动事件。   |
| APPLICATION_INITIALIZED | 应用容器初始化事件。 |
| APPLICATION_DESTROYED   | 应用容器销毁事件。   |

#### ModuleEvent

模块事件，包含模块的启动、初始化和销毁事件。

| 事件枚举名称       | 描述             |
| ------------------ | ---------------- |
| MODULE_STARTUP     | 模块启动事件。   |
| MODULE_INITIALIZED | 模块初始化事件。 |
| MODULE_DESTROYED   | 模块销毁事件。   |

### 3.3 配置注解

#### @EventListener

声明一个类为事件监听器。

```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EventListener {

    /**
     * 事件处理模式
     */
    Mode mode() default Mode.ASYNC;

    /**
     * 事件类型
     */
    Class<? extends IEvent>[] value();

    /**
     * 优先级
     */
    int priority() default 0;
}
```

#### @EventRegister

声明一个类为事件注册器，用于注册事件监听器。

```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EventRegister {
}
```

#### @EventsConf

事件模块初始化参数配置。

```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EventsConf {

    /**
     * 事件处理模式
     */
    Mode mode() default Mode.ASYNC;

    /**
     * 事件管理提供者接口实现
     */
    String providerClass() default "";

    /**
     * 初始化线程池大小
     */
    int threadPoolSize() default 0;

    /**
     * 最大线程池大小
     */
    int threadMaxPoolSize() default 200;

    /**
     * 线程队列大小
     */
    int threadQueueSize() default 1024;
}
```

## 4. 使用场景

### 4.1 模块间通信

通过事件机制，模块之间可以通过事件进行通信，而不需要直接引用对方，降低了模块之间的耦合度。

### 4.2 业务流程解耦

将业务流程中的不同步骤通过事件连接起来，每个步骤只关注自己的职责，提高了代码的可维护性和可扩展性。

### 4.3 异步处理

对于耗时的操作，可以通过异步事件处理模式，将操作放入后台线程执行，提高系统的响应速度。

### 4.4 事件监听

监听系统中的特定事件，如用户登录、订单创建等，执行相应的处理逻辑。

### 4.5 插件系统

在插件系统中，通过事件机制，插件可以响应系统的特定事件，实现功能的扩展。

## 5. 配置方式

### 5.1 注解配置

通过 `@EventsConf` 注解配置事件模块的初始化参数。

```java
@EnableAutoScan
@EventsConf(mode = Events.MODE.ASYNC, threadPoolSize = 20, threadMaxPoolSize = 200, threadQueueSize = 1024)
public class Starter {

    public static void main(String[] args) throws Exception {
        try (IApplication application = YMP.run(args)) {
            // 应用逻辑
        }
    }
}
```

### 5.2 配置文件配置

通过配置文件配置事件模块的初始化参数。

```properties
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

## 6. 注意事项

1. **事件处理顺序**：当某个事件被触发后，订阅该事件的监听器被回调执行的顺序是不能被保证的，除非显式设置了优先级。

2. **异常处理**：事件监听器在处理事件时抛出的异常会被捕获，但不会影响其他监听器的执行。

3. **内存泄漏**：长时间运行的应用中，需要注意事件监听器的注册和注销，避免内存泄漏。

4. **异步处理**：使用异步模式时，需要注意线程安全问题，避免并发访问导致的问题。

5. **事件对象**：事件对象应该是不可变的，避免在事件处理过程中被修改。

6. **事件名称**：事件名称应该使用枚举类型，避免使用字符串常量，提高代码的可维护性。

7. **监听器优先级**：合理设置监听器的优先级，确保事件处理的顺序符合业务需求。

## 7. 最佳实践

1. **合理设计事件**：根据业务需求，合理设计事件对象，包含必要的信息。

2. **单一职责**：每个事件监听器应该只负责处理一种类型的事件，保持监听器的简洁性。

3. **异步处理**：对于耗时的操作，建议使用异步模式，提高系统的响应速度。

4. **异常处理**：在事件监听器中妥善处理异常，避免异常影响整个系统的运行。

5. **事件注册**：使用 `@EventListener` 注解注册事件监听器，简化代码。

6. **优先级设置**：根据业务需求，合理设置监听器的优先级，确保事件处理的顺序正确。

7. **测试**：编写专门的测试用例来测试事件的发布和处理，确保事件机制的正确性。

8. **文档化**：为事件和监听器添加详细的文档注释，说明事件的用途和处理逻辑。

## 8. 示例代码

### 8.1 自定义事件

```java
public class UserEvent implements IEvent {

    private final Object source;

    private final EventName eventName;

    private final long timestamp;

    private final User user;

    public UserEvent(Object source, EventName eventName, User user) {
        this.source = source;
        this.eventName = eventName;
        this.timestamp = System.currentTimeMillis();
        this.user = user;
    }

    @Override
    public Object getSource() {
        return source;
    }

    @Override
    public Enum<?> getEventName() {
        return eventName;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    public User getUser() {
        return user;
    }

    public enum EventName {
        USER_CREATED,
        USER_UPDATED,
        USER_DELETED
    }
}
```

### 8.2 自定义事件监听器

```java
@EventListener(mode = Events.Mode.ASYNC, value = UserEvent.class, priority = 10)
public class UserEventListener implements IEventListener<UserEvent> {

    private static final Log LOG = LogFactory.getLog(UserEventListener.class);

    @Override
    public boolean handle(UserEvent context) throws Exception {
        UserEvent.EventName eventName = (UserEvent.EventName) context.getEventName();
        User user = context.getUser();

        switch (eventName) {
            case USER_CREATED:
                LOG.info("User created: " + user.getName());
                // 发送欢迎邮件等操作
                break;
            case USER_UPDATED:
                LOG.info("User updated: " + user.getName());
                // 更新缓存等操作
                break;
            case USER_DELETED:
                LOG.info("User deleted: " + user.getName());
                // 清理相关数据等操作
                break;
        }

        return false;
    }
}
```

### 8.3 发布事件

```java
@Bean
public class UserService {

    public User createUser(User user) {
        // 创建用户的业务逻辑
        userDao.save(user);

        // 发布用户创建事件
        Events.get().fireEvent(new UserEvent(this, UserEvent.EventName.USER_CREATED, user));

        return user;
    }

    public User updateUser(User user) {
        // 更新用户的业务逻辑
        userDao.update(user);

        // 发布用户更新事件
        Events.get().fireEvent(new UserEvent(this, UserEvent.EventName.USER_UPDATED, user));

        return user;
    }

    public void deleteUser(String userId) {
        User user = userDao.findById(userId);

        // 删除用户的业务逻辑
        userDao.delete(userId);

        // 发布用户删除事件
        Events.get().fireEvent(new UserEvent(this, UserEvent.EventName.USER_DELETED, user));
    }
}
```

### 8.4 通过事件注册器注册监听器

```java
@EventRegister
public class DemoEventRegister implements IEventRegister {

    @Override
    public void register(Events events) throws Exception {
        // 注册用户事件监听器
        events.registerListener(Events.Mode.ASYNC, UserEvent.class, new UserEventListener(), 10);

        // 注册其他事件监听器
        events.registerListener(Events.Mode.SYNC, ModuleEvent.class, new ModuleEventListener(), 5);
    }
}
```

### 8.5 通过应用初始化器注册监听器

```java
public class DemoApplicationInitializer implements IApplicationInitializer {

    @Override
    public void beforeInit(IApplication application) throws Exception {
        // 应用初始化前的操作
    }

    @Override
    public void afterInit(IApplication application) throws Exception {
        // 应用初始化后的操作
        Events events = application.getEvents();

        // 注册事件监听器
        events.registerListener(Events.Mode.ASYNC, UserEvent.class, new UserEventListener(), 10);
    }
}
```

## 9. 总结

Event 模块是 YMP 框架中一个重要的模块，提供了灵活、强大的事件驱动编程模型。通过该模块，开发者可以实现模块之间的解耦，提高系统的可维护性和可扩展性。

该模块的主要优势在于：

1. **灵活的事件处理**：支持同步和异步两种事件处理模式，可以根据业务需求选择合适的模式。

2. **简单易用的 API**：提供了简洁、直观的 API，使得事件的发布和处理变得简单易用。

3. **多种注册方式**：支持通过注解、事件注册器和应用初始化器等多种方式注册事件监听器，满足不同的使用场景。

4. **优先级支持**：支持设置监听器的优先级，确保事件处理的顺序符合业务需求。

5. **与框架集成**：与 YMP 框架的其他模块无缝集成，支持依赖注入等特性。

Event 模块为 YMP 框架提供了强大的事件驱动能力，是构建松耦合、可扩展系统的重要工具。通过合理使用该模块，可以大大提高代码的质量和开发效率。
