# Core-AOP 模块技能文档

## 1. 模块概述

Core-AOP 模块是 YMP 框架中的核心模块之一，提供了强大的面向切面编程（AOP）功能。该模块基于代理技术实现了方法拦截机制，允许开发者在方法执行前后插入自定义逻辑，而无需修改原有代码结构。

**主要功能特点：**

- 基于注解的拦截器配置
- 支持前置、后置和环绕拦截
- 支持拦截器参数传递
- 支持多层拦截器嵌套
- 支持拦截器全局规则设置
- 支持自定义拦截器注解

## 2. 核心功能

### 2.1 拦截器机制

Core-AOP 模块的核心是拦截器机制，通过实现 `IInterceptor` 接口或继承 `AbstractInterceptor` 抽象类来创建自定义拦截器。拦截器可以拦截方法的执行，在方法执行前后插入自定义逻辑。

### 2.2 注解配置

模块提供了丰富的注解来配置拦截器：

- `@Before` - 前置拦截器配置
- `@After` - 后置拦截器配置
- `@Around` - 环绕拦截器配置
- `@Clean` - 清理拦截器配置
- `@ContextParam` - 上下文参数配置
- `@Ignored` - 忽略拦截器配置
- `@Interceptor` - 声明拦截器类

### 2.3 拦截器执行顺序

拦截器的执行顺序遵循以下规则：

1. 包级别拦截器（`package-info.java` 中配置）
2. 类级别拦截器
3. 方法级别拦截器

### 2.4 拦截器全局规则

通过配置文件或 `@InterceptSettings` 注解，可以设置拦截器的全局规则，如禁用特定拦截器、为特定包添加拦截器等。

## 3. API 接口

### 3.1 核心接口

#### IInterceptor

拦截器接口，是所有拦截器的基础接口。

```java
public interface IInterceptor {

    /**
     * 拦截方法执行
     * @param context 拦截上下文
     * @return 拦截结果，非null表示方法执行被拦截
     * @throws Exception 异常
     */
    Object intercept(InterceptContext context) throws Exception;
}
```

#### AbstractInterceptor

拦截器抽象类，实现了 `IInterceptor` 接口，提供了更方便的拦截器编写方式。

```java
public abstract class AbstractInterceptor implements IInterceptor {

    /**
     * 前置拦截
     * @param context 拦截上下文
     * @return 拦截结果，非null表示方法执行被拦截
     * @throws InterceptException 拦截异常
     */
    protected abstract Object before(InterceptContext context) throws InterceptException;

    /**
     * 后置拦截
     * @param context 拦截上下文
     * @return 拦截结果
     * @throws InterceptException 拦截异常
     */
    protected abstract Object after(InterceptContext context) throws InterceptException;
}
```

### 3.2 上下文对象

#### InterceptContext

拦截上下文对象，包含了拦截相关的所有信息。

```java
public interface InterceptContext {

    /**
     * 获取目标类
     * @return 目标类
     */
    Class<?> getTargetClass();

    /**
     * 获取目标方法
     * @return 目标方法
     */
    Method getTargetMethod();

    /**
     * 获取目标对象
     * @return 目标对象
     */
    Object getTargetObject();

    /**
     * 获取方法参数
     * @return 方法参数
     */
    Object[] getParameters();

    /**
     * 获取上下文参数
     * @return 上下文参数
     */
    Map<String, Object> getContextParams();

    /**
     * 获取方法执行结果
     * @return 方法执行结果
     */
    Object getResult();

    /**
     * 设置方法执行结果
     * @param result 方法执行结果
     */
    void setResult(Object result);
}
```

### 3.3 配置注解

#### @Interceptor

声明一个类为拦截器。

```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Interceptor {

    /**
     * 拦截器注解类型
     */
    Class<? extends Annotation>[] value() default {};
}
```

#### @Before

配置前置拦截器。

```java
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.PACKAGE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Before {

    /**
     * 拦截器类
     */
    Class<? extends IInterceptor>[] value();
}
```

#### @After

配置后置拦截器。

```java
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.PACKAGE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface After {

    /**
     * 拦截器类
     */
    Class<? extends IInterceptor>[] value();
}
```

#### @Around

配置环绕拦截器。

```java
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.PACKAGE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Around {

    /**
     * 拦截器类
     */
    Class<? extends IInterceptor>[] value();
}
```

## 4. 使用场景

### 4.1 日志记录

通过拦截器记录方法的执行时间、参数和返回值，实现统一的日志记录。

### 4.2 权限验证

通过拦截器验证用户的权限，确保只有具有相应权限的用户才能执行特定方法。

### 4.3 事务管理

通过拦截器实现事务的开启、提交和回滚，确保数据操作的原子性。

### 4.4 缓存控制

通过拦截器实现方法执行结果的缓存，提高系统性能。

### 4.5 异常处理

通过拦截器统一处理方法执行过程中产生的异常，提供一致的异常处理机制。

## 5. 配置方式

### 5.1 注解配置

通过在类或方法上添加拦截器注解来配置拦截器。

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
```

### 5.2 包级别配置

在 `package-info.java` 文件中配置包级别的拦截器。

```java
@Before(DemoInterceptor.class)
@ContextParam(key = "param", value = "helloworld")
package net.ymate.demo.controller;

import net.ymate.demo.intercept.DemoInterceptor;
import net.ymate.platform.core.beans.annotation.Before;
import net.ymate.platform.core.beans.annotation.ContextParam;
```

### 5.3 全局规则配置

通过配置文件设置拦截器全局规则。

```properties
# 是否开启拦截器全局规则设置
ymp.intercept.settings_enabled=true

# 为包添加拦截器
ymp.intercept.packages.net.ymate.demo.controller=before:net.ymate.demo.intercept.UserSessionInterceptor

# 禁用拦截器
ymp.intercept.globals.net.ymate.demo.intercept.UserSessionInterceptor=disabled

# 为目标类配置拦截器执行规则
ymp.intercept.settings.net.ymate.demo.controller.DemoController#=*
```

## 6. 注意事项

1. **性能考虑**：拦截器会增加方法调用的开销，应避免在性能敏感的方法上使用过多拦截器。

2. **异常处理**：拦截器中应妥善处理异常，避免异常影响正常的业务流程。

3. **线程安全**：拦截器实例可能被多个线程共享，应确保拦截器的线程安全性。

4. **拦截器顺序**：注意拦截器的执行顺序，避免因顺序问题导致的逻辑错误。

5. **避免循环依赖**：避免拦截器之间的循环依赖，以免导致死循环。

6. **拦截器粒度**：合理设置拦截器的粒度，避免过度使用拦截器导致代码难以理解和维护。

## 7. 最佳实践

1. **单一职责**：每个拦截器应只负责一项具体功能，保持拦截器的简洁性和可维护性。

2. **抽象通用逻辑**：将通用的横切关注点（如日志、权限验证等）抽象为拦截器，提高代码的复用性。

3. **合理使用上下文参数**：通过 `@ContextParam` 注解传递上下文参数，使拦截器更加灵活。

4. **使用抽象类**：继承 `AbstractInterceptor` 抽象类来创建拦截器，简化拦截器的编写。

5. **合理配置拦截器**：根据业务需求合理配置拦截器，避免不必要的拦截。

6. **测试拦截器**：编写专门的测试用例来测试拦截器的功能，确保拦截器的正确性。

7. **文档化**：为拦截器添加详细的文档注释，说明拦截器的功能、参数和使用方法。

## 8. 示例代码

### 8.1 自定义拦截器

```java
@Interceptor
public class LogInterceptor extends AbstractInterceptor {

    private static final Log LOG = LogFactory.getLog(LogInterceptor.class);

    @Override
    protected Object before(InterceptContext context) throws InterceptException {
        long startTime = System.currentTimeMillis();
        context.getContextParams().put("startTime", startTime);
        LOG.info("Method " + context.getTargetMethod().getName() + " started");
        return null;
    }

    @Override
    protected Object after(InterceptContext context) throws InterceptException {
        long startTime = (long) context.getContextParams().get("startTime");
        long endTime = System.currentTimeMillis();
        LOG.info("Method " + context.getTargetMethod().getName() + " completed in " + (endTime - startTime) + "ms");
        return null;
    }
}
```

### 8.2 使用拦截器

```java
@Bean
public class UserController {

    @Before(LogInterceptor.class)
    @Before(PermissionInterceptor.class)
    public User getUser(String id) {
        // 业务逻辑
        return userService.getUser(id);
    }

    @Around(TransactionInterceptor.class)
    public void saveUser(User user) {
        // 业务逻辑
        userService.saveUser(user);
    }
}
```

### 8.3 自定义拦截器注解

```java
@Target({ElementType.PACKAGE, ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@InterceptAnnotation({IInterceptor.Direction.BEFORE, IInterceptor.Direction.AFTER})
public @interface Log {

    /**
     * 日志级别
     */
    String level() default "info";

    /**
     * 是否记录参数
     */
    boolean recordParams() default true;

    /**
     * 是否记录结果
     */
    boolean recordResult() default true;
}
```

```java
@Interceptor(Log.class)
public class LogAnnotationInterceptor extends AbstractInterceptor {

    private static final Log LOG = LogFactory.getLog(LogAnnotationInterceptor.class);

    @Override
    protected Object before(InterceptContext context) throws InterceptException {
        Log logAnnotation = findInterceptAnnotation(context, Log.class);
        if (logAnnotation != null && logAnnotation.recordParams()) {
            Method method = context.getTargetMethod();
            Object[] params = context.getParameters();
            StringBuilder sb = new StringBuilder();
            sb.append("Method " + method.getName() + " called with params: ");
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    sb.append(params[i]);
                    if (i < params.length - 1) {
                        sb.append(", ");
                    }
                }
            }
            LOG.info(sb.toString());
        }
        return null;
    }

    @Override
    protected Object after(InterceptContext context) throws InterceptException {
        Log logAnnotation = findInterceptAnnotation(context, Log.class);
        if (logAnnotation != null && logAnnotation.recordResult()) {
            Method method = context.getTargetMethod();
            Object result = context.getResult();
            LOG.info("Method " + method.getName() + " returned: " + result);
        }
        return null;
    }
}
```

```java
@Bean
public class UserService {

    @Log(level = "debug", recordParams = true, recordResult = true)
    public User getUser(String id) {
        // 业务逻辑
        return userDao.getUser(id);
    }
}
```

## 9. 总结

Core-AOP 模块是 YMP 框架中一个强大的模块，提供了灵活、易用的面向切面编程功能。通过该模块，开发者可以轻松实现日志记录、权限验证、事务管理等横切关注点，提高代码的复用性和可维护性。

该模块的主要优势在于：

1. **基于注解的配置**：通过丰富的注解来配置拦截器，使用简单方便。
2. **灵活的拦截器机制**：支持前置、后置和环绕拦截，可以满足各种拦截需求。
3. **多层次的配置**：支持包级别、类级别和方法级别的拦截器配置，灵活性高。
4. **全局规则设置**：通过配置文件或注解设置拦截器的全局规则，方便管理。
5. **与框架集成**：与 YMP 框架的其他模块无缝集成，支持依赖注入等特性。

Core-AOP 模块为 YMP 框架提供了强大的 AOP 功能，是框架中不可或缺的一部分。通过合理使用该模块，可以大大提高代码的质量和开发效率。
