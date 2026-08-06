---
name: ymp-core-aop
description: YMP框架Core-AOP模块，基于代理(Proxy)技术的方法拦截体系，支持@Before前置/@After后置/@Around环绕拦截、自定义拦截器注解、包/类/方法三级拦截配置与全局规则
version: 2.1.4-dev
author: YMP Team
category: aop
tags:
  - java
  - aop
  - interceptor
  - aspect-oriented
  - proxy
  - before
  - after
  - around
trigger: 当用户需要实现方法拦截、前置(@Before)/后置(@After)/环绕(@Around)切面、自定义IInterceptor拦截器、日志/权限/事务/缓存等横切逻辑、@EnableBeanProxy代理开启、自定义拦截器注解@InterceptAnnotation、全局拦截规则@InterceptSettings时触发
tools:
  - aop
  - method-interceptor
  - proxy-factory
examples:
  - 继承AbstractInterceptor实现自定义日志拦截器
  - 方法级别@Before/@After/@Around配置拦截器
  - 创建@InterceptAnnotation驱动的自定义拦截器注解
  - package-info.java配置包级别拦截器
  - @InterceptSettings配置全局拦截规则
---

# Core-AOP 方法拦截技能包

> AI读取指引：本模块边界=代理工厂+拦截器配置+IInterceptor实现+全局拦截规则；Bean/DI/启动类基础配置 → 跳转core/SKILL.md；事务/@Transaction → persistence-jdbc；缓存/@Cacheable → cache模块。

---

## 0. 快速索引（AI一眼定位）

- Maven artifactId：`ymate-platform-core`（AOP已包含在core中，无需额外依赖）
- 启动入口注解：`net.ymate.platform.core.annotation.EnableBeanProxy`（必须标启动类，否则所有拦截器**完全不生效**）
- 拦截器基础类：`net.ymate.platform.core.beans.intercept.AbstractInterceptor`（推荐继承）/ `IInterceptor`（接口）
- 典型调用示例（5行内最简）：
```java
@EnableBeanProxy  // 启动类必须！
public class Starter {
    // 业务方法上标拦截注解
    @Before(LogInterceptor.class)
    public String hello() { return "hi"; }
}
```

## 1. 模块摘要（1-2句 + 5条以内核心能力）

基于CGLIB/Javassist/ByteBuddy动态代理技术的方法拦截(AOP)实现，为YMP容器Bean提供前置/后置/环绕拦截能力，支持包/类/方法三级声明式配置与自定义拦截器注解。

- **三级拦截配置**：package-info（包）→ 类 → 方法，执行顺序由粗到细叠加
- **三种拦截方向**：`@Before` 前置 / `@After` 后置 / `@Around` 环绕（前后都执行）
- **IInterceptor体系**：接口 `intercept(InterceptContext)` 或继承 `AbstractInterceptor` 覆写 `before()`/`after()`
- **自定义拦截器注解**：`@InterceptAnnotation` 元注解标注，业务侧更简洁（一个注解替代@Before+@ContextParam等）
- **全局规则引擎**：`ymp.intercept.settings_enabled=true` 后，通过配置文件或 `@InterceptSettings` 对拦截器批量禁用/添加/清理

## 2. 核心注解速查表（最关键，必须包含全限定名）

| 注解 | 全限定名 | 作用目标 | 常用参数（2-5个核心参数名+说明） |
|---|---|---|---|
| @EnableBeanProxy | `net.ymate.platform.core.annotation.EnableBeanProxy` | 启动类 | factoryClass=IProxyFactory（Default/Javassist/ByteBuddy/NoOp禁用AOP） |
| @Before | `net.ymate.platform.core.beans.annotation.Before` | 包/类/方法 | value=IInterceptor实现类数组（前置拦截） |
| @After | `net.ymate.platform.core.beans.annotation.After` | 包/类/方法 | value=IInterceptor实现类数组（后置拦截） |
| @Around | `net.ymate.platform.core.beans.annotation.Around` | 包/类/方法 | value=IInterceptor实现类数组（前后都拦截，等价@Before+@After） |
| @Clean | `net.ymate.platform.core.beans.annotation.Clean` | 类/方法 | value=要清理的IInterceptor类数组；清空则清理类级全部 |
| @ContextParam | `net.ymate.platform.core.beans.annotation.ContextParam` | 包/类/方法 | key=参数名 / value=参数值（支持`$xxx`从全局参数取） |
| @ContextParams | `net.ymate.platform.core.beans.annotation.ContextParams` | 包/类/方法 | value=@ContextParam数组 |
| @Ignored | `net.ymate.platform.core.beans.annotation.Ignored` | 类/方法 | -（被标注的方法完全跳过拦截；非public/Object类方法默认也跳过） |
| @Interceptor | `net.ymate.platform.core.beans.annotation.Interceptor` | 拦截器类（实现IInterceptor） | value=关联的自定义拦截器注解Class数组（可选） |
| @InterceptAnnotation | `net.ymate.platform.core.beans.annotation.InterceptAnnotation` | 自定义注解（元注解） | value=Direction.BEFORE/AFTER数组，空=全方向（标记一个注解为拦截器注解，可替代@Before/@After配置） |
| @InterceptSettings | `net.ymate.platform.core.beans.annotation.InterceptSettings` | 包/类 | globals=禁用拦截器数组 / packages=@PackageSet / value=@InterceptSet（需settings_enabled=true） |
| @Proxy | `net.ymate.platform.core.beans.annotation.Proxy` | 类 | value=IProxy实现类数组（低级代理扩展） |
| @CleanProxy | `net.ymate.platform.core.beans.annotation.CleanProxy` | 类/方法 | value=IProxy类数组，空=全部清理 |
| @Order | `net.ymate.platform.core.beans.annotation.Order` | 拦截器类/自定义拦截注解 | value=顺序号（同位置多拦截器执行顺序，越小越先） |

## 3. 核心API速查（仅入口静态类+最常用方法）

- `IInterceptor.intercept(InterceptContext context)` → `Object`：拦截器唯一接口方法；返回`null`=继续后续拦截/方法执行，返回非`null`=停止并以此作为方法返回值（仅前置方向有效）
- `AbstractInterceptor.before(InterceptContext ctx)` → `Object`：前置拦截逻辑（子类覆写）
- `AbstractInterceptor.after(InterceptContext ctx)` → `Object`：后置拦截逻辑（子类覆写，默认return null）
- `AbstractInterceptor.findInterceptAnnotation(ctx, Ann.class)` → `<T extends Annotation> T`：从方法→类→包三级查找自定义拦截器注解（配合@InterceptAnnotation使用）
- `InterceptContext.getDirection()` → `IInterceptor.Direction`：当前执行方向 `BEFORE` / `AFTER`
- `InterceptContext.getTargetClass()` / `getTargetMethod()` / `getTargetObject()` → 目标类/方法/实例
- `InterceptContext.getParameters()` → `Object[]`：方法入参数组
- `InterceptContext.getContextParams()` → `Map<String,Object>`：取@ContextParam传入的自定义参数（可读写，before→after传递数据用）
- `InterceptContext.getResult()` / `setResult(Object)`：后置阶段读取/改写方法实际返回值
- `IApplication.registerInterceptor(Class<? extends IInterceptor>)`：手动注册拦截器类

## 4. 标准代码模板（最少可运行，带import+License+类/方法注释+@since）

### 模板1：自定义拦截器（继承AbstractInterceptor + @Interceptor标记）

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
package com.example.intercept;

import net.ymate.platform.core.beans.annotation.Interceptor;
import net.ymate.platform.core.beans.intercept.AbstractInterceptor;
import net.ymate.platform.core.beans.intercept.InterceptContext;
import net.ymate.platform.core.beans.intercept.InterceptException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 方法耗时日志拦截器示例（前置+后置组合）。
 *
 * @author YMP Team
 * @since 2.1.4-dev
 */
@Interceptor
public class LogInterceptor extends AbstractInterceptor {

    private static final Log LOG = LogFactory.getLog(LogInterceptor.class);

    private static final String START_TIME_KEY = "_startTime";

    /**
     * 前置拦截：记录方法开始时间戳。
     *
     * @param context 拦截上下文
     * @return null表示不拦截，继续执行
     * @throws InterceptException 拦截逻辑异常
     * @since 2.1.4-dev
     */
    @Override
    protected Object before(InterceptContext context) throws InterceptException {
        long start = System.currentTimeMillis();
        context.getContextParams().put(START_TIME_KEY, start);
        LOG.info("[BEFORE] method=" + context.getTargetMethod().getName());
        return null;
    }

    /**
     * 后置拦截：计算并打印耗时。
     *
     * @param context 拦截上下文
     * @return 返回null（后置返回值通常被忽略）
     * @throws InterceptException 拦截逻辑异常
     * @since 2.1.4-dev
     */
    @Override
    protected Object after(InterceptContext context) throws InterceptException {
        Long start = (Long) context.getContextParams().get(START_TIME_KEY);
        long cost = start == null ? -1 : System.currentTimeMillis() - start;
        LOG.info("[AFTER ] method=" + context.getTargetMethod().getName()
                + " cost=" + cost + "ms"
                + " result=" + context.getResult());
        return null;
    }
}
```

### 模板2：在方法/类上使用 @Before + @ContextParam

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

import net.ymate.platform.core.beans.annotation.After;
import net.ymate.platform.core.beans.annotation.Around;
import net.ymate.platform.core.beans.annotation.Before;
import net.ymate.platform.core.beans.annotation.Bean;
import net.ymate.platform.core.beans.annotation.ContextParam;

import com.example.intercept.LogInterceptor;
import com.example.intercept.PermissionInterceptor;

/**
 * 业务服务类，演示拦截器在类/方法级别的使用。
 *
 * @author YMP Team
 * @since 2.1.4-dev
 */
@Bean
@Before(LogInterceptor.class)  // 类级别：所有public方法都前置走LogInterceptor
public class OrderService {

    /**
     * 创建订单（类级@Before已生效，方法级再加权限拦截+上下文参数）。
     *
     * @param userId 用户ID
     * @param amount 金额
     * @return 订单号
     * @since 2.1.4-dev
     */
    @Before(PermissionInterceptor.class)
    @ContextParam(key = "permCode", value = "ORDER_CREATE")
    public String createOrder(String userId, long amount) {
        return "ORD_" + System.currentTimeMillis();
    }

    /**
     * 查询订单（加后置拦截 + @Around环绕）。
     *
     * @param orderId 订单号
     * @return 订单详情字符串
     * @since 2.1.4-dev
     */
    @After(LogInterceptor.class)
    @Around(PermissionInterceptor.class)
    @ContextParam(key = "permCode", value = "ORDER_QUERY")
    public String queryOrder(String orderId) {
        return "OrderDetail#" + orderId;
    }
}
```

### 模板3：自定义拦截器注解（@InterceptAnnotation元注解方式）

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
package com.example.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.ymate.platform.core.beans.annotation.InterceptAnnotation;
import net.ymate.platform.core.beans.intercept.IInterceptor;

/**
 * 自定义权限拦截注解（带参数，替代@Before+@ContextParam组合）。
 *
 * @author YMP Team
 * @since 2.1.4-dev
 */
@Target({ElementType.PACKAGE, ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@InterceptAnnotation({IInterceptor.Direction.BEFORE})  // 仅前置方向
public @interface RequirePermission {

    /**
     * @return 权限编码（拦截器内通过findInterceptAnnotation取此值）
     */
    String code();

    /**
     * @return 是否必须校验通过，默认true
     */
    boolean required() default true;
}
```

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
package com.example.intercept;

import net.ymate.platform.core.beans.annotation.Interceptor;
import net.ymate.platform.core.beans.intercept.AbstractInterceptor;
import net.ymate.platform.core.beans.intercept.InterceptContext;
import net.ymate.platform.core.beans.intercept.InterceptException;

import com.example.annotation.RequirePermission;

/**
 * 配合自定义注解@RequirePermission的权限拦截器实现。
 *
 * @author YMP Team
 * @since 2.1.4-dev
 */
@Interceptor(RequirePermission.class)  // 关联自定义注解
public class PermissionInterceptor extends AbstractInterceptor {

    /**
     * 前置权限校验。
     *
     * @param context 拦截上下文
     * @return 非null返回值将中断执行并直接返回
     * @throws InterceptException 无权限时抛出
     * @since 2.1.4-dev
     */
    @Override
    protected Object before(InterceptContext context) throws InterceptException {
        // 从方法→类→包三级查找注解
        RequirePermission ann = findInterceptAnnotation(context, RequirePermission.class);
        if (ann != null && ann.required()) {
            String code = ann.code();
            // TODO: 真实项目此处调用权限服务校验当前用户是否持有code权限
            boolean hasPerm = "ORDER_CREATE".equals(code) || "ORDER_QUERY".equals(code);
            if (!hasPerm) {
                throw new InterceptException("无权限：" + code);
            }
        }
        return null;
    }
}
```

```java
/* 业务侧使用：一个注解搞定拦截+参数（无需@Before/@ContextParam分开写） */
@Bean
public class OrderService {
    @RequirePermission(code = "ORDER_CREATE")
    public String createOrder(String userId, long amount) { return "OK"; }

    @RequirePermission(code = "ORDER_QUERY")
    public String queryOrder(String orderId) { return "DETAIL"; }
}
```

### 模板4：package-info.java 包级别拦截配置

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

/**
 * 控制器包：包级别自动加登录校验拦截器 + 日志上下文参数。
 * 执行顺序：package(LoginInterceptor) → class → method
 *
 * @since 2.1.4-dev
 */
@Before(com.example.intercept.LoginInterceptor.class)
@ContextParam(key = "module", value = "WEB_CONTROLLER")
package com.example.controller;

import net.ymate.platform.core.beans.annotation.Before;
import net.ymate.platform.core.beans.annotation.ContextParam;
```

## 5. 配置速查（ymp-conf.properties / 注解配置）

### 5.1 配置文件常用项（ymp-conf.properties）

| 配置key | 默认值 | 说明 |
|---|---|---|
| `ymp.intercept.settings_enabled` | `false` | **必须=true**，配置文件/@InterceptSettings全局规则才会生效 |
| `ymp.intercept.packages.<包全名>` | 无 | 为指定包批量加拦截器，格式：`before:完整类名\|after:类名`，多拦截器用`\|`分隔 |
| `ymp.intercept.globals.<拦截器全类名>` | 空 | 取值`disabled`=全局禁用该拦截器（代码中已声明的该拦截器全部跳过） |
| `ymp.intercept.settings.<目标类全类名>#` | 无 | 类级规则：`*`=清空该类所有拦截器；`before:*`=清空全部前置；`after:*`=清空全部后置 |
| `ymp.intercept.settings.<类全类名>#<方法名>` | 无 | 方法级规则：`before:类名+`=追加前置；`before:类名-`=移除前置；`after:类名+`=追加后置 |

示例片段：
```properties
ymp.intercept.settings_enabled=true
ymp.intercept.packages.com.example.controller=before:com.example.intercept.LoginInterceptor
ymp.intercept.globals.com.example.intercept.DebugInterceptor=disabled
ymp.intercept.settings.com.example.controller.UserController#login=before:*
ymp.intercept.settings.com.example.controller.OrderController#createOrder=after:com.example.intercept.AuditInterceptor+
```

### 5.2 启动/配置注解核心参数

- `@EnableBeanProxy`：`factoryClass`=代理工厂实现
  - `DefaultProxyFactory`（默认，基于CGLIB）
  - `JavassistProxyFactory`（Javassist实现）
  - `ByteBuddyProxyFactory`（ByteBuddy实现）
  - `NoOpProxyFactory`（空实现=关闭AOP，排查代理相关问题时临时使用）
- `@InterceptSettings`（标在包/类上，需settings_enabled=true）：
  - `globals`=Class<? extends IInterceptor>[] → 批量禁用
  - `packages`=@PackageSet[] → 批量为包加拦截器+上下文参数
  - `value`=@InterceptSet[] → 为目标类/方法加/移除/清空拦截器
- `@Before/@After/@Around`：`value`=IInterceptor[]，顺序=数组顺序执行（多拦截器）
- `@ContextParam`：`key`/`value`，value支持`$xxx`从`ymp.params.xxx`读取环境值

## 6. 常见坑点排查（3-6条）

| 坑现象 | 原因 | 解决办法 |
|---|---|---|
| 拦截器完全不进，日志也没任何反应 | ①启动类缺`@EnableBeanProxy`（90%情况）②方法非public ③方法是Object类继承方法(toString/equals等) ④对象是`new`出来的而非从BeanFactory取 ⑤@Ignored标注了方法/类 | 启动类补`@EnableBeanProxy`；方法改public；通过 `app.getBeanFactory().getBean(X.class)` 获取Bean实例 |
| 前置拦截器返回非null后，后置拦截器仍执行？ | 前置拦截器返回非null是中断方法本体和后续前置拦截器，但后置拦截器链独立执行是设计行为 | 如业务需要跳过后置，在context.getContextParams里设标记，after()里读标记判断是否return |
| `@InterceptSettings`/配置文件全局规则完全无效 | `ymp.intercept.settings_enabled` 未置为`true`（默认false，出于性能考虑不开启解析） | ymp-conf.properties写 `ymp.intercept.settings_enabled=true` |
| 同方法多拦截器执行顺序不明确 | 同位置未指定@Order时按注解数组顺序，但包→类→方法三级叠加顺序容易误解 | 三级顺序固定：**package → class → method**；同层级多拦截器加`@Order(数字)`，数字越小越先执行 |
| 自定义拦截器中findInterceptAnnotation()永远返回null | ①拦截器类的@Interceptor没传自定义注解Class参数 ②自定义注解缺@InterceptAnnotation元注解 ③注解未标RUNTIME保留 | 自定义注解上加`@Retention(RetentionPolicy.RUNTIME) + @InterceptAnnotation`；拦截器类`@Interceptor(MyAnn.class)` |
| 拦截器里修改getParameters()数组元素，业务方法收到的参数没变 | InterceptContext.getParameters()返回副本引用，框架没做参数回写（设计如此，避免副作用） | 如必须改参数，使用更低级IProxy接口而非拦截器，或改造业务为接受封装对象参数 |
| @Clean加在方法上不生效 / 类级拦截器清理不掉 | @Clean作用是清理包和类**继承**下来的拦截器，需settings_enabled=true，且清理操作仅对声明式（非全局）有效 | 方法级@Clean()不传参=清空类级；传具体拦截器Class=只清那几个；全局声明的用ymp.intercept.globals.xxx=disabled |

## 7. 本模块注解全限定名索引（AI拼import备用）

| 短名 | 全限定名 |
|---|---|
| @EnableBeanProxy | `net.ymate.platform.core.annotation.EnableBeanProxy` |
| @Before | `net.ymate.platform.core.beans.annotation.Before` |
| @After | `net.ymate.platform.core.beans.annotation.After` |
| @Around | `net.ymate.platform.core.beans.annotation.Around` |
| @Clean | `net.ymate.platform.core.beans.annotation.Clean` |
| @ContextParam | `net.ymate.platform.core.beans.annotation.ContextParam` |
| @ContextParams | `net.ymate.platform.core.beans.annotation.ContextParams` |
| @Interceptor | `net.ymate.platform.core.beans.annotation.Interceptor` |
| @InterceptAnnotation | `net.ymate.platform.core.beans.annotation.InterceptAnnotation` |
| @InterceptSettings | `net.ymate.platform.core.beans.annotation.InterceptSettings` |
| @Proxy | `net.ymate.platform.core.beans.annotation.Proxy` |
| @CleanProxy | `net.ymate.platform.core.beans.annotation.CleanProxy` |
| @Order | `net.ymate.platform.core.beans.annotation.Order` |
| @Ignored | `net.ymate.platform.core.beans.annotation.Ignored` |

| 核心类/接口 | 全限定名 |
|---|---|
| IInterceptor | `net.ymate.platform.core.beans.intercept.IInterceptor` |
| IInterceptor.Direction | `net.ymate.platform.core.beans.intercept.IInterceptor.Direction` |
| IInterceptor.SettingType | `net.ymate.platform.core.beans.intercept.IInterceptor.SettingType` |
| AbstractInterceptor | `net.ymate.platform.core.beans.intercept.AbstractInterceptor` |
| InterceptContext | `net.ymate.platform.core.beans.intercept.InterceptContext` |
| InterceptException | `net.ymate.platform.core.beans.intercept.InterceptException` |
| InterceptMeta | `net.ymate.platform.core.beans.intercept.InterceptMeta` |
| IProxyFactory | `net.ymate.platform.core.beans.proxy.IProxyFactory` |
| DefaultProxyFactory | `net.ymate.platform.core.beans.proxy.impl.DefaultProxyFactory` |
| JavassistProxyFactory | `net.ymate.platform.core.beans.proxy.impl.JavassistProxyFactory` |
| ByteBuddyProxyFactory | `net.ymate.platform.core.beans.proxy.impl.ByteBuddyProxyFactory` |
| NoOpProxyFactory | `net.ymate.platform.core.beans.proxy.impl.NoOpProxyFactory` |
