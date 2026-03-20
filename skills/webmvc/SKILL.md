---
name: ymp-webmvc
description: YMP框架WebMVC模块，提供标准的MVC实现，支持RESTful、多种视图技术、参数绑定、验证等
version: 2.1.4
author: YMP Team
category: web
tags:
  - java
  - mvc
  - web
  - restful
  - controller
trigger: 当用户需要开发Web应用、实现MVC架构、RESTful API、控制器等场景时触发
tools:
  - web-framework
  - mvc
  - restful-api
examples:
  - 创建控制器
  - 实现RESTful API
  - 参数绑定
  - 参数验证
  - 视图渲染
  - 拦截器配置
---

# YMP WebMVC 模块技能文档

## 模块概述

WebMVC 模块是 YMP 框架中除了 JDBC 持久化模块以外的另一个非常重要的模块，集成了 YMP 框架的诸多特性，在功能结构的设计和使用方法上依然保持一贯的简单风格，同时也继承了主流 MVC 框架的基因，对于了解和熟悉 SSH 或 SSM 等框架技术的开发人员来说，上手极其容易，毫无学习成本。

## 核心功能

- **标准 MVC 实现**：结构清晰，完全基于注解方式配置简单
- **约定模式支持**：无需编写控制器代码，直接匹配并执行视图渲染
- **多种视图技术**：支持 Binary、Forward、Freemarker、HTML、HttpStatus、JSON、JSP、Redirect、Text、Velocity 等
- **RESTful 支持**：支持 RESTful 模式及 URL 风格
- **自动参数绑定**：支持请求参数与控制器方法参数的自动绑定
- **参数有效性验证**：集成验证框架，支持参数验证
- **控制器方法拦截**：支持控制器方法的拦截
- **注解配置路由**：支持注解配置控制器请求路由映射
- **自动扫描注册**：支持自动扫描控制器类并注册
- **自定义事件和异常处理**：支持事件和异常的自定义处理
- **I18N 国际化**：支持 I18N 资源国际化
- **缓存支持**：支持控制器方法和视图缓存
- **插件扩展**：支持插件扩展
- **URL 扩展名匹配**：支持通过 @RequestMapping 注解的 suffix 属性匹配带扩展名的请求路径
- **扩展名值注入**：支持通过 @RequestSuffix 注解将请求路径的扩展名值注入到控制器方法参数

## 架构设计

### 核心架构

WebMVC 模块采用典型的 MVC 架构设计，主要包含以下核心组件：

1. **控制器（Controller）**：处理请求，执行业务逻辑，返回视图
2. **视图（View）**：负责渲染响应内容
3. **模型（Model）**：数据模型，通过参数绑定和视图属性传递
4. **请求处理器（RequestProcessor）**：处理请求参数和路径解析
5. **拦截器（Interceptor）**：拦截控制器方法执行
6. **异常处理器（ErrorProcessor）**：处理异常情况
7. **Web上下文（WebContext）**：封装 Web 环境对象

### 请求处理流程

1. **请求接收**：通过 `DispatchFilter` 或 `DispatchServlet` 接收请求
2. **请求解析**：解析请求路径，确定目标控制器和方法
3. **参数绑定**：将请求参数绑定到控制器方法参数
4. **执行拦截器**：执行前置拦截器
5. **控制器方法执行**：调用目标控制器方法
6. **视图渲染**：根据控制器返回结果渲染视图
7. **执行拦截器**：执行后置拦截器
8. **响应返回**：将渲染结果返回给客户端

## 核心 API

### 控制器注解

#### @Controller

声明一个类为控制器，框架在启动时将会自动扫描所有声明该注解的类并注册。

| 配置项 | 描述 |
|-------|------|
| name | 控制器名称，默认为空 |
| singleton | 是否为单例控制器，默认为 true |

#### @RequestMapping

声明控制器请求路径映射。

| 配置项 | 描述 |
|-------|------|
| value | 控制器请求路径映射，默认为空（即使用方法名称） |
| method | 允许的请求方式，默认为 GET<br/>取值范围：GET、HEAD、POST、PUT、PATCH、DELETE、OPTIONS、TRACE |
| header | 请求头中必须存在的头名称 |
| param | 请求中必须存在的参数名称 |
| suffix | 允许的请求路径扩展名，默认为空数组（即不允许带扩展名），支持 `.*` 通配符匹配任意扩展名 |

#### @RequestParam

绑定请求中的参数。

| 配置项 | 描述 |
|-------|------|
| prefix | 绑定的参数名称前缀，默认为空 |
| value | 绑定的参数名称，若未指定则默认采用方法参数变量名 |
| defaultValue | 默认值，默认为空 |
| fullScope | 是否尝试其它作用域下获取参数值，默认为 false<br/>优先级顺序：request > session > application，默认为仅从 request 中获取 |

#### @PathVariable

绑定请求映射中的路径参数变量。

| 配置项 | 描述 |
|-------|------|
| value | 绑定的参数名称，若未指定则默认采用方法参数变量名 |



#### @RequestSuffix

绑定请求路径的扩展名值到控制器方法参数。

> 注意：此注解仅在控制器方法的 @RequestMapping 注解中配置了 suffix 属性时生效。

#### @ModelBind

对象参数绑定注解。

| 配置项 | 描述 |
|-------|------|
| prefix | 绑定的参数名称前缀，可选参数，默认为空 |

### 视图操作

#### View 工厂类

```java
// 创建文本视图
IView textView = View.textView("Hello, YMP!");

// 创建JSP视图
IView jspView = View.jspView("/demo/test");

// 创建JSON视图
IView jsonView = View.jsonView(dataObject);

// 创建重定向视图
IView redirectView = View.redirectView("/login");

// 创建空视图
IView nullView = View.nullView();
```

#### 视图属性操作

```java
IView view = View.jspView("/demo/test")
    .addAttribute("name", "YMP")
    .addAttribute("version", "2.1.4")
    .addHeader("X-Powered-By", "YMP")
    .setContentType("text/html");
```

### WebContext

WebContext 封装了 Web 环境对象，提供了便捷的操作方法：

```java
// 获取请求对象
HttpServletRequest request = WebContext.getRequest();

// 获取响应对象
HttpServletResponse response = WebContext.getResponse();

// 获取会话对象
HttpSession session = WebContext.getSession();

// 获取应用上下文
ServletContext application = WebContext.getServletContext();

// 获取请求参数
String username = WebContext.getContext().getParameterToString("username");

// 设置会话属性
WebContext.getContext().getSession().put("user", userInfo);

// 获取应用属性
String appName = WebContext.getContext().getApplicationAttributeToString("app.name");
```

### 配置 API

#### @WebConf

WebMVC 模块配置注解。

| 配置项 | 描述 |
|-------|------|
| mappingParserClass | 控制器请求映射路径分析器 |
| requestProcessClass | 控制器请求处理器 |
| errorProcessorClass | 异常错误处理器 |
| cacheProcessorClass | 缓存处理器 |
| resourceHome | 国际化资源文件存放路径 |
| resourceName | 国际化资源文件名称 |
| languageParamName | 国际化语言设置参数名称，默认值为 _lang |
| defaultCharsetEncoding | 默认字符编码集设置 |
| defaultContentType | 默认 Content-Type 设置 |
| requestIgnoreSuffixes | 请求忽略后缀集合 |
| requestMethodParam | 请求方法参数名称 |
| requestPrefix | 请求路径前缀 |
| requestStrictModeEnabled | 请求路径匹配是否启用严格模式 |
| baseViewPath | 控制器视图文件基础路径 |
| cookiePrefix | Cookie 键前缀 |
| cookieDomain | Cookie 作用域 |
| cookiePath | Cookie 作用路径 |
| cookieAuthKey | Cookie 密钥 |
| cookieAuthEnabled | Cookie 密钥验证是否默认开启 |
| cookieUseHttpOnly | Cookie 是否默认使用 HttpOnly |
| uploadTempDir | 文件上传临时目录 |
| uploadFileCountMax | 上传文件数量最大值 |
| uploadFileSizeMax | 上传文件大小最大值（字节） |
| uploadTotalSizeMax | 上传文件总量大小最大值（字节） |
| uploadSizeThreshold | 内存缓冲区的大小（字节） |
| uploadListenerClass | 文件上传状态监听器 |

## 配置与使用

### 模块初始化

在 Web 应用中，WebMVC 模块通过监听器和过滤器/ servlet 初始化：

#### web.xml 配置

```xml
<listener>
    <listener-class>net.ymate.platform.webmvc.support.WebAppEventListener</listener-class>
</listener>

<filter>
    <filter-name>GeneralWebFilter</filter-name>
    <filter-class>net.ymate.platform.webmvc.support.GeneralWebFilter</filter-class>
    <init-param>
        <param-name>responseHeaders</param-name>
        <param-value>X-Frame-Options=SAMEORIGIN</param-value>
    </init-param>
</filter>
<filter-mapping>
    <filter-name>GeneralWebFilter</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>

<filter>
    <filter-name>DispatchFilter</filter-name>
    <filter-class>net.ymate.platform.webmvc.support.DispatchFilter</filter-class>
</filter>
<filter-mapping>
    <filter-name>DispatchFilter</filter-name>
    <url-pattern>/*</url-pattern>
    <dispatcher>REQUEST</dispatcher>
    <dispatcher>FORWARD</dispatcher>
</filter-mapping>
```

### 控制器示例

#### 基本控制器

```java
@Controller
@RequestMapping("/hello")
public class HelloController {

    @RequestMapping(value = "/", method = {Type.HttpMethod.GET, Type.HttpMethod.POST})
    public IView hello() throws Exception {
        return View.textView("Everything depends on ability!  -- YMP :)");
    }
}
```

#### 参数绑定

```java
@Controller
@RequestMapping("/demo")
public class DemoController {

    @RequestMapping("/param")
    public IView testParam(@RequestParam String name,
                           @RequestParam(defaultValue = "18") Integer age,
                           @RequestParam(value = "name", prefix = "user") String username,
                           @RequestHeader(defaultValue = "BASIC") String authType,
                           @CookieVariable(defaultValue = "false") Boolean isLogin) {
        return View.textView(String.format("Hi, %s, UserName: %s, Age: %d, AuthType: %s, IsLogin: %s",
            name, username, age, authType, isLogin));
    }
}
```

#### RESTful 风格

```java
@Controller
@RequestMapping("/demo")
public class DemoController {

    @RequestMapping("/path/{userName}/{age}")
    public IView testPath(@PathVariable(value = "userName") String name,
                          @PathVariable Integer age,
                          @RequestParam(prefix = "user") String sex) {
        return View.textView(String.format("Hi, %s, Age: %d, Sex: %s", name, age, sex));
    }
}
```

#### 文件上传

```java
@Controller
@RequestMapping("/demo")
public class UploadController {

    // 处理单文件上传
    @RequestMapping(value = "/upload", method = Type.HttpMethod.POST)
    @FileUpload
    public IView doUpload(@RequestParam IUploadFileWrapper file) throws Exception {
        // 获取文件名称
        String fileName = file.getName();
        // 获取文件大小
        long fileSize = file.getSize();
        // 保存文件
        file.transferTo(new File("/temp", fileName));
        return View.textView("File uploaded successfully!");
    }

    // 处理多文件上传
    @RequestMapping(value = "/uploads", method = Type.HttpMethod.POST)
    @FileUpload
    public IView doUploadBatch(@RequestParam IUploadFileWrapper[] files) throws Exception {
        for (IUploadFileWrapper file : files) {
            file.transferTo(new File("/temp", file.getName()));
        }
        return View.textView("Files uploaded successfully!");
    }
}
```

#### 响应视图

```java
@Controller
@RequestMapping("/view")
public class ViewController {

    @RequestMapping("/text")
    public IView textView() {
        return View.textView("Hello, Text View!");
    }

    @RequestMapping("/json")
    public Object jsonView() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "YMP");
        data.put("version", "2.1.4");
        return data; // 自动转换为 JSON
    }

    @RequestMapping("/jsp")
    public IView jspView() {
        return View.jspView("/demo/index")
            .addAttribute("message", "Hello, JSP View!");
    }

    @RequestMapping("/redirect")
    public IView redirectView() {
        return View.redirectView("/login");
    }
}
```



#### 扩展名匹配

```java
@Controller
@RequestMapping("/demo")
public class SuffixController {

    // 匹配带 .html 扩展名的请求
    @RequestMapping(value = "/html", suffix = ".html")
    public IView html() {
        return View.textView("HTML View");
    }

    // 匹配带任意扩展名的请求，并将扩展名值注入到参数中
    @RequestMapping(value = "/any", suffix = ".*")
    public IView any(@RequestSuffix String suffix) {
        return View.textView("Suffix: " + suffix);
    }

    // 匹配带 .json 或 .xml 扩展名的请求
    @RequestMapping(value = "/format", suffix = {".json", ".xml"})
    public IView format(@RequestSuffix String suffix) {
        if ("json".equals(suffix)) {
            return View.jsonView(Collections.singletonMap("message", "Hello JSON"));
        } else if ("xml".equals(suffix)) {
            return View.textView("<message>Hello XML</message>", "application/xml");
        }
        return View.textView("Unknown Format");
    }
}
```



> **注意事项**：在使用 `@RequestMapping` 的 `suffix` 属性配置扩展名匹配时，需要注意 `webmvc.request_ignore_regex` 配置项（默认值：`jsp|jspx|png|gif|jpg|jpeg|js|css|swf|ico|htm|html|eot|woff|woff2|ttf|svg|map`）可能会过滤掉指定的扩展名。如果需要匹配的扩展名在默认过滤列表中，需要修改该配置项以包含所需的扩展名。



> **扩展名配置继承**：`@RequestMapping` 的 `suffix` 属性支持继承特性，配置优先级从高到低为：方法级别 > 类级别 > 包级别。
>
> - 若方法上配置了 `suffix` 属性，则使用方法上的配置
> - 若方法上未配置，则使用类上的 `@RequestMapping` 注解的 `suffix` 配置
> - 若类上也未配置，则使用包上的 `@RequestMapping` 注解的 `suffix` 配置
> - 若包上类上方法上都有声明，则方法上的配置会替换掉类或包上的配置，并生效

**示例**：

```java
// 包级别配置
@RequestMapping(suffix = ".*")
package com.example.controller;

// 类级别配置
@Controller
@RequestMapping(value = "/demo", suffix = ".html")
public class DemoController {

    // 使用类级别的 .html 扩展名配置
    @RequestMapping("/class")
    public IView classLevel() {
        return View.textView("Class level suffix");
    }

    // 使用方法级别的 .json 扩展名配置，覆盖类级别的配置
    @RequestMapping(value = "/method", suffix = ".json")
    public IView methodLevel() {
        return View.jsonView(Collections.singletonMap("message", "Method level suffix"));
    }
}
```

## 高级特性

### 参数签名验证

WebMVC 模块提供了参数签名验证功能，用于保障接口调用的安全性：

```java
@Controller
@RequestMapping("/api")
public class ApiController {

    @RequestMapping("/sign")
    @EnableSnakeCaseParam
    @SignatureValidate(nonceName = "nonce", timestampName = "create_time")
    public IView sign(@RequestParam String clientId,
                      @RequestParam Long createTime,
                      @RequestParam String event,
                      @RequestParam String unionId) throws Exception {
        return View.textView("Signature validated successfully!");
    }
}
```

### 跨域支持

WebMVC 模块提供了跨域请求支持：

```java
@Controller
@RequestMapping("/api")
@CrossDomain
public class ApiController {

    @RequestMapping("/data")
    public Object getData() {
        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("message", "Hello, Cross Domain!");
        return data;
    }
}
```

### 异常处理

WebMVC 模块支持自定义异常处理：

```java
@ResponseErrorProcessor
public class CustomErrorProcessor extends DefaultResponseErrorProcessor {

    @Override
    public IView processError(Throwable e) throws Exception {
        if (e instanceof ResourceNotFoundException) {
            return View.httpStatusView(404)
                .addHeader("X-Error-Message", "Resource not found");
        }
        return super.processError(e);
    }
}
```

### 缓存支持

WebMVC 模块支持控制器方法缓存：

```java
@Controller
@RequestMapping("/demo")
public class CacheController {

    @RequestMapping("/cache")
    @ResponseCache(expire = 3600)
    public IView cachedMethod() {
        return View.textView("This response is cached for 1 hour.");
    }
}
```

## 配置项

### 基本配置

```properties
#-------------------------------------
# WebMVC模块初始化参数
#-------------------------------------

# 控制器请求映射路径分析器
ymp.configs.webmvc.request_mapping_parser_class=

# 控制器请求处理器
ymp.configs.webmvc.request_processor_class=

# 异常错误处理器
ymp.configs.webmvc.error_processor_class=

# 缓存处理器
ymp.configs.webmvc.cache_processor_class=

# 默认字符编码集设置
ymp.configs.webmvc.default_charset_encoding=

# 默认Content-Type设置
ymp.configs.webmvc.default_content_type=

# 国际化资源文件存放路径
ymp.configs.webmvc.resources_home=

# 国际化资源文件名称
ymp.configs.webmvc.resource_name=

# 国际化语言设置参数名称
ymp.configs.webmvc.language_param_name=

# 请求忽略后缀集合
ymp.configs.webmvc.request_ignore_regex=

# 请求方法参数名称
ymp.configs.webmvc.request_method_param=

# 请求路径前缀
ymp.configs.webmvc.request_prefix=

# 请求路径匹配是否启用严格模式
ymp.configs.webmvc.request_strict_mode_enabled=

# 控制器视图文件基础路径
ymp.configs.webmvc.base_view_path=
```

### Cookie 配置

```properties
#-------------------------------------
# Cookie相关参数配置
#-------------------------------------

# Cookie键前缀
ymp.configs.webmvc.cookie_prefix=

# Cookie作用域
ymp.configs.webmvc.cookie_domain=

# Cookie作用路径
ymp.configs.webmvc.cookie_path=

# Cookie密钥
ymp.configs.webmvc.cookie_auth_key=

# Cookie密钥验证是否默认开启
ymp.configs.webmvc.cookie_auth_enabled=

# Cookie是否默认使用HttpOnly
ymp.configs.webmvc.cookie_use_http_only=
```

### 文件上传配置

```properties
#-------------------------------------
# 文件上传相关参数配置
#-------------------------------------

# 文件上传临时目录
ymp.configs.webmvc.upload_temp_dir=

# 上传文件数量最大值
ymp.configs.webmvc.upload_file_count_max=

# 上传文件大小最大值(字节)
ymp.configs.webmvc.upload_file_size_max=

# 上传文件总量大小最大值(字节)
ymp.configs.webmvc.upload_total_size_max=

# 内存缓冲区的大小
ymp.configs.webmvc.upload_size_threshold=

# 文件上传状态监听器
ymp.configs.webmvc.upload_listener_class=
```

## 最佳实践

1. **控制器设计**：控制器应保持简洁，主要负责请求处理和视图返回，业务逻辑应封装到服务层

2. **参数验证**：使用验证注解对请求参数进行验证，确保数据合法性

3. **视图选择**：根据业务需求选择合适的视图类型，如 RESTful API 使用 JSON 视图

4. **异常处理**：统一处理异常，返回友好的错误信息

5. **路径设计**：遵循 RESTful 风格设计 API 路径，保持一致性

6. **缓存策略**：合理使用缓存，提高系统性能

7. **跨域处理**：对于前后端分离架构，正确配置跨域设置

8. **安全性**：使用参数签名验证，防止恶意请求

9. **国际化**：支持多语言，提高用户体验

10. **代码规范**：遵循代码规范，保持代码可读性

## 常见问题与解决方案

### 1. 控制器不被扫描

**问题**：添加了 `@Controller` 注解但控制器未被注册。

**解决方案**：
- 确保开启了自动扫描 `@EnableAutoScan`
- 检查控制器类是否在扫描包路径下
- 确认控制器类使用了 `public` 修饰符

### 2. 视图路径找不到

**问题**：控制器返回视图但页面找不到。

**解决方案**：
- 检查 `base_view_path` 配置是否正确
- 确认视图文件存在于正确的路径
- 检查视图名称是否正确

### 3. 参数绑定失败

**问题**：请求参数无法正确绑定到控制器方法参数。

**解决方案**：
- 检查参数名称是否匹配
- 确认参数类型转换是否正确
- 查看是否缺少必要的参数

### 4. 文件上传失败

**问题**：文件上传功能无法正常工作。

**解决方案**：
- 确保添加了 `@FileUpload` 注解
- 检查文件上传配置是否正确
- 确认表单 `enctype` 属性设置为 `multipart/form-data`

### 5. 跨域请求被拒绝

**问题**：前端跨域请求被拒绝。

**解决方案**：
- 添加 `@CrossDomain` 注解
- 检查跨域配置是否正确
- 确认响应头是否包含正确的 CORS 信息

## 总结

WebMVC 模块是 YMP 框架中一个功能强大、设计简洁的 MVC 实现，它提供了丰富的特性和灵活的配置选项，使开发者能够快速构建 Web 应用。通过注解式配置、自动参数绑定、多种视图技术支持等特性，大大简化了 Web 应用的开发工作。

WebMVC 模块不仅支持传统的 JSP 视图，还支持 JSON、RESTful 等现代 Web 开发需求，同时提供了文件上传、跨域支持、参数验证等实用功能，满足了各种 Web 应用场景的需求。

通过本文档的介绍，相信开发者能够快速掌握 WebMVC 模块的使用方法，并在实际项目中灵活应用，构建高质量的 Web 应用。
