---
sidebar_position: 10
slug: webmvc
---

# WebMVC

WebMVC 模块在 YMP 框架中是除了 JDBC 持久化模块以外的另一个非常重要的模块，集成了 YMP 框架的诸多特性，在功能结构的设计和使用方法上依然保持一贯的简单风格，同时也继承了主流 MVC 框架的基因，对于了解和熟悉 SSH 或 SSM 等框架技术的开发人员来说，上手极其容易，毫无学习成本。

其主要功能特性如下：

- 标准 MVC 实现，结构清晰，完全基于注解方式配置简单；
- 支持约定模式，无需编写控制器代码，直接匹配并执行视图渲染；
- 支持多种视图技术（Binary、Forward、Freemarker、HTML、HttpStatus、JSON、JSP、Redirect、Text、Velocity 等）；
- 支持 RESTful 模式及 URL 风格；
- 支持请求参数与控制器方法参数的自动绑定；
- 支持参数有效性验证；
- 支持控制器方法的拦截；
- 支持注解配置控制器请求路由映射；
- 支持自动扫描控制器类并注册；
- 支持事件和异常的自定义处理；
- 支持I18N资源国际化；
- 支持控制器方法和视图缓存；
- 支持插件扩展；



## Maven包依赖

```xml
<dependency>
    <groupId>net.ymate.platform</groupId>
    <artifactId>ymate-platform-webmvc</artifactId>
    <version>2.1.1</version>
</dependency>
```



## 模块初始化

在 Web 程序中监听器（Listener）是最先被容器初始化的，所以 WebMVC 模块是由监听器负责对 YMP 框架进行初始化：

> 监听器（Listener）：net.ymate.platform.webmvc.support.WebAppEventListener

处理浏览器请求并与模块中控制器匹配、路由的过程可分别由过滤器（Filter）和服务端程序（Servlet）完成：

> 过滤器（Filter）：net.ymate.platform.webmvc.support.DispatchFilter

> 服务端程序（Servlet）：net.ymate.platform.webmvc.support.DispatchServlet



以下为完整的 `web.xml` 配置文件内容：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app id="ymate-cms-webapp" version="3.0" xmlns="http://java.sun.com/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd">

    <listener>
        <listener-class>net.ymate.platform.webmvc.support.WebAppEventListener</listener-class>
    </listener>

    <filter>
        <filter-name>GeneralWebFilter</filter-name>
        <filter-class>net.ymate.platform.webmvc.support.GeneralWebFilter</filter-class>
        <init-param>
            <param-name>responseHeaders</param-name>
            <!--
            HTTP 响应头信息中的 X-Frame-Options，可以指示浏览器是否应该加载一个 iframe 中的页面。
            如果服务器响应头信息中没有 X-Frame-Options，则该网站存在 ClickJacking 攻击风险。
            网站可以通过设置 X-Frame-Options 阻止站点内的页面被其他页面嵌入从而防止点击劫持。
            添加 X-Frame-Options 响应头，赋值有如下三种：
                1、DENY: 不能被嵌入到任何iframe或者frame中。
                2、SAMEORIGIN: 页面只能被本站页面嵌入到iframe或者frame中。
                3、ALLOW-FROM uri: 只能被嵌入到指定域名的框架中。
            -->
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

    <!--
    <servlet>
        <servlet-name>DispatchServlet</servlet-name>
        <servlet-class>net.ymate.platform.webmvc.support.DispatchServlet</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>DispatchServlet</servlet-name>
        <url-pattern>/service/*</url-pattern>
    </servlet-mapping>
    -->

    <!--
    OPTIONS 方法是用于请求获得由 Request-URI 标识的资源在请求/响应的通信过程中可以使用的功能选项。
    通过这个方法，客户端可以在采取具体资源请求之前，决定对该资源采取何种必要措施，或者了解服务器的性能。
    OPTIONS 方法可能会暴露一些敏感信息，这些信息将帮助攻击者准备更进一步的攻击。
    -->
    <!--
    <security-constraint>
        <web-resource-collection>
            <web-resource-name>RequestMethodAllowed</web-resource-name>
            <url-pattern>/*</url-pattern>
            <http-method-omission>GET</http-method-omission>
            <http-method-omission>POST</http-method-omission>
            <http-method-omission>OPTIONS</http-method-omission>
            <http-method-omission>PUT</http-method-omission>
            <http-method-omission>HEAD</http-method-omission>
            <http-method-omission>TRACE</http-method-omission>
            <http-method-omission>DELETE</http-method-omission>
            <http-method-omission>SEARCH</http-method-omission>
            <http-method-omission>COPY</http-method-omission>
            <http-method-omission>MOVE</http-method-omission>
            <http-method-omission>PROPFIND</http-method-omission>
            <http-method-omission>PROPPATCH</http-method-omission>
            <http-method-omission>MKCOL</http-method-omission>
            <http-method-omission>LOCK</http-method-omission>
            <http-method-omission>UNLOCK</http-method-omission>
        </web-resource-collection>
        <auth-constraint/>
        <user-data-constraint>
            <transport-guarantee>NONE</transport-guarantee>
        </user-data-constraint>
    </security-constraint>
    -->

    <welcome-file-list>
        <welcome-file>index.html</welcome-file>
        <welcome-file>index.jsp</welcome-file>
    </welcome-file-list>

    <error-page>
        <error-code>400</error-code>
        <location>/WEB-INF/templates/error.jsp?status=400</location>
    </error-page>
    <error-page>
        <error-code>401</error-code>
        <location>/WEB-INF/templates/error.jsp?status=401</location>
    </error-page>
    <error-page>
        <error-code>404</error-code>
        <location>/WEB-INF/templates/error.jsp?status=404</location>
    </error-page>
    <error-page>
        <error-code>405</error-code>
        <location>/WEB-INF/templates/error.jsp?status=405</location>
    </error-page>
    <error-page>
        <error-code>500</error-code>
        <location>/WEB-INF/templates/error.jsp?status=500</location>
    </error-page>
</web-app>
```



## 模块配置

### 配置文件参数说明

#### 基本参数配置

```properties
#-------------------------------------
# WebMVC模块初始化参数
#-------------------------------------

# 控制器请求映射路径分析器, 可选值为已知分析器名称或自定义分析器类名称, 默认值: default, 目前支持已知分析器[default|...]
ymp.configs.webmvc.request_mapping_parser_class=

# 控制器请求处理器, 可选值为已知处理器名称或自定义处理器类名称, 自定义类需实现net.ymate.platform.webmvc.IRequestProcessor接口, 默认值: default, 目前支持已知处理器[default|json|xml|...]
ymp.configs.webmvc.request_processor_class=

# 异常错误处理器, 可选参数, 默认值: net.ymate.platform.webmvc.impl.DefaultWebErrorProcessor
ymp.configs.webmvc.error_processor_class=

# 缓存处理器, 可选参数, 此类需实现net.ymate.platform.webmvc.IWebCacheProcessor接口, 默认值: net.ymate.platform.webmvc.support.WebCacheProcessor
ymp.configs.webmvc.cache_processor_class=

# 默认字符编码集设置, 可选参数, 默认值: UTF-8
ymp.configs.webmvc.default_charset_encoding=

# 默认Content-Type设置, 可选参数, 默认值: text/html
ymp.configs.webmvc.default_content_type=

# 国际化资源文件存放路径, 可选参数, 默认值: ${root}/i18n/
ymp.configs.webmvc.resources_home=

# 国际化资源文件名称, 可选参数, 默认值: messages
ymp.configs.webmvc.resource_name=

# 国际化语言设置参数名称, 可选参数, 默认值: _lang
ymp.configs.webmvc.language_param_name=

# 请求忽略后缀集合, 可选参数, 默认值: jsp|jspx|png|gif|jpg|jpeg|js|css|swf|ico|htm|html|eot|woff|ttf|svg|map
ymp.configs.webmvc.request_ignore_regex=

# 请求方法参数名称, 可选参数, 默认值: _method
ymp.configs.webmvc.request_method_param=

# 请求路径前缀, 可选参数, 默认值: 空
ymp.configs.webmvc.request_prefix=

# 控制器视图文件基础路径(必须是以 '/' 开始和结尾), 默认值: /WEB-INF/templates/
ymp.configs.webmvc.base_view_path=

#-------------------------------------
# Cookie相关参数配置
#-------------------------------------

# Cookie键前缀, 可选参数, 默认值: 空
ymp.configs.webmvc.cookie_prefix=

# Cookie作用域, 可选参数, 默认值: 空
ymp.configs.webmvc.cookie_domain=

# Cookie作用路径, 可选参数, 默认值: /
ymp.configs.webmvc.cookie_path=

# Cookie密钥, 可选参数, 默认值: 空
ymp.configs.webmvc.cookie_auth_key=

# Cookie密钥验证是否默认开启, 默认值: false
ymp.configs.webmvc.cookie_auth_enabled=

# Cookie是否默认使用HttpOnly, 默认值: false
ymp.configs.webmvc.cookie_use_http_only=

#-------------------------------------
# 文件上传相关参数配置
#-------------------------------------

# 文件上传临时目录, 为空则默认使用：System.getProperty("java.io.tmpdir")
ymp.configs.webmvc.upload_temp_dir=

# 上传文件大小最大值(字节), 默认值: -1(注: 10485760 = 10M)
ymp.configs.webmvc.upload_file_size_max=

# 上传文件总量大小最大值(字节), 默认值: -1(注：10485760 = 10M)
ymp.configs.webmvc.upload_total_size_max=

# 内存缓冲区的大小, 默认值: 10240字节(=10K), 即如果文件大于10K, 将使用临时文件缓存上传文件
ymp.configs.webmvc.upload_size_threshold=

# 文件上传状态监听器, 可选参数, 此类需实现org.apache.commons.fileupload.ProgressListener接口, 默认值: 空
ymp.configs.webmvc.upload_listener_class=
```

:::tip **说明**：

1. 在服务端程序（Servlet）方式的请求处理中，请求忽略正则表达式 `request_ignore_regex` 参数无效。

2. 在配置 `request_ignore_regex` 参数时，可以通过首元素为 `~` 符号实现继承，即在默认值的基础上追加。

   例如：为默认请求忽略后缀集合增加 `xml` 和 `json` 后缀。

   ```properties
   ymp.configs.webmvc.request_ignore_regex=~|xml|json
   ```


:::



#### 扩展参数配置

```properties
#-------------------------------------
# 扩展参数配置
#-------------------------------------

# 控制器请求URL后缀, 默认值: 空
ymp.params.webmvc.request_suffix=

# 服务名称, 默认值: request.getServerName();
ymp.params.webmvc.server_name=

# 异常信息视图文件, 默认值: error.jsp
ymp.params.webmvc.error_view=

# 系统异常分析是否关闭, 默认值: false
ymp.params.webmvc.exception_analysis_disabled=

# 默认异常响应视图格式, 默认值: 空, 可选范围: json|xml
ymp.params.webmvc.error_default_view_format=

# 异常响应时是否使用状态码, 默认值: false, 若开启则当发生异常时响应的状态码值为400(BAD_REQUEST)
ymp.params.webmvc.error_with_status_code=

# 验证结果消息模板, 默认值: ${items}
ymp.params.webmvc.validation_template_element=

# 验证结果消息项模板, 默认值: ${message}<br/>
ymp.params.webmvc.validation_template_item=

# 允许访问和重定向的主机名称, 多个主机名称用'|'分隔, 默认值: 空(表示不限制)
ymp.params.webmvc.allowed_access_hosts=

# 允许上传的文件类型, 多个用'|'分隔, 默认值: 空(表示不限制)
ymp.params.webmvc.allowed_upload_content_types=
```



### 配置注解参数说明

#### @WebConf

| 配置项                 | 描述                                               |
| ---------------------- | -------------------------------------------------- |
| mappingParserClass     | 控制器请求映射路径分析器                           |
| requestProcessClass    | 控制器请求处理器                                   |
| errorProcessorClass    | 异常错误处理器                                     |
| cacheProcessorClass    | 缓存处理器                                         |
| resourceHome           | 国际化资源文件存放路径                             |
| resourceName           | 国际化资源文件名称                                 |
| languageParamName      | 国际化语言设置参数名称，可选参数，默认值为 `_lang` |
| defaultCharsetEncoding | 默认字符编码集设置                                 |
| defaultContentType     | 默认Content-Type设置                               |
| requestIgnoreSuffixes  | 请求忽略后缀集合                                   |
| requestMethodParam     | 请求方法参数名称                                   |
| requestPrefix          | 请求路径前缀                                       |
| baseViewPath           | 控制器视图文件基础路径                             |
| cookiePrefix           | Cookie键前缀                                       |
| cookieDomain           | Cookie作用域                                       |
| cookiePath             | Cookie作用路径                                     |
| cookieAuthKey          | Cookie密钥                                         |
| cookieAuthEnabled      | Cookie密钥验证是否默认开启                         |
| cookieUseHttpOnly      | Cookie是否默认使用 HttpOnly                        |
| uploadTempDir          | 文件上传临时目录                                   |
| uploadFileSizeMax      | 上传文件大小最大值（字节）                         |
| uploadTotalSizeMax     | 上传文件总量大小最大值（字节）                     |
| uploadSizeThreshold    | 内存缓冲区的大小（字节）                           |
| uploadListenerClass    | 文件上传状态监听器                                 |



## 模块事件

事件枚举对象 `WebEvent` 包括以下事件类型：

|事务类型|说明|
|---|---|
|SERVLET_CONTEXT_INITIALIZED|容器初始化事件|
|SERVLET_CONTEXT_DESTROYED|容器销毁事件|
|SERVLET_CONTEXT_ATTR_ADDED|容器属性添加事件|
|SERVLET_CONTEXT_ATTR_REMOVED|容器属性移除事件|
|SERVLET_CONTEXT_ATTR_REPLACED|容器属性替换事件|
|SESSION_CREATED|会话创建事件|
|SESSION_DESTROYED|会话销毁事件|
|SESSION_ATTR_ADDED|会话属性添加事件|
|SESSION_ATTR_REMOVEED|会话属性移除事件|
|SESSION_ATTR_REPLACED|会话属性替换事件|
|REQUEST_INITIALIZED|请求初始化事件|
|REQUEST_DESTROYED|请求销毁事件|
|REQUEST_ATTR_ADDED|请求属性添加事件|
|REQUEST_ATTR_REMOVEED|请求属性移除事件|
|REQUEST_ATTR_REPLACED|请求属性替换事件|
|REQUEST_RECEIVED|接收控制器方法请求事件|
|REQUEST_COMPLETED|完成控制器方法请求事件|
|REQUEST_UNEXPECTED_ERROR|控制器方法执行过程中发生异常错误事件|



## 控制器（Controller）

控制器是 MVC 体系中的核心，它负责处理浏览器发起的所有请求和决定响应内容的逻辑处理，控制器就是一个标准的 Java 类，不需要继承任何基类，通过类中的非静态公共方法向外部暴露接口，该方法的返回结果将决定向浏览器响应的具体内容。

下面通过示例展示如何编写控制器：

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

启动 Tomcat 服务并在浏览器中访问 `http://localhost:8080/hello`，页面输出结果：

```html
Everything depends on ability!  -- YMP :)
```

以上示例代码中使用了 `@Controller` 和 `@RequestMapping` 两个注解，它们的作用及参数含义是：

### @Controller

声明一个类为控制器，框架在启动时将会自动扫描所有声明该注解的类并注册。

| 配置项    | 描述                                       |
| --------- | ------------------------------------------ |
| name      | 控制器名称，默认为空（该参数暂时未被使用） |
| singleton | 是否为单例控制器，默认为 `true`            |



### @RequestMapping

声明控制器请求路径映射。

| 配置项 | 描述                                                         |
| ------ | ------------------------------------------------------------ |
| value  | 控制器请求路径映射，必选参数                                 |
| method | 允许的请求方式，默认为 `Type.HttpMethod.GET`<br/>取值范围：`GET`、`HEAD`、`POST`、`PUT`、`PATCH`、`DELETE`、`OPTIONS`、`TRACE` |
| header | 请求头中必须存在的头名称                                     |
| param  | 请求中必须存在的参数名称                                     |



### 示例一

创建非单例控制器，并完成如下规则设置：

> 1. 设置控制器方法仅支持 `POST` 和 `PUT` 方式访问。
> 2. 设置请求头参数中必须包含 `X-Requested-With=XMLHttpRequest`（即判断是否 `Ajax` 请求）。
> 3. 设置请求参数中必须存在 `name` 参数。

```java
@Controller(singleton = false)
@RequestMapping("/demo")
public class DemoController {

	@RequestMapping(value = "/sayhi",
        method = {Type.HttpMethod.POST, Type.HttpMethod.PUT},
        header = {"X-Requested-With=XMLHttpRequest"},
        param = {"name=*"})
	public IView sayHi() {
		return View.textView("Hi, YMPer!");
	}
}
```

本例主要展示了如何使用 `@Controller` 和 `@RequestMapping` 注解对控制器和控制器方法对进配置，需要注意的是控制器方法必须使用 `public` 修饰，否则无效。

由于控制器类和方法上都声明了 `@RequestMapping` 注解，所以控制器方法的请求路径映射将继承上层配置，即：`/demo/sayhi`。

另外，`@RequestMapping` 注解的 `header` 和 `param` 参数值支持`*`通配符，如：`key=*`，表示请求中须存在名称为 `key` 的请求头或参数。



### 示例二

创建单例控制器，并完成如下规则设置：

> 1. 通过注解设置响应头参数 `X-From=China` 和 `X-Age=18`。
> 2. 通过注解设置控制器返回文本视图，其返回内容为：`Hi, YMPer!`

```java
@Controller
@RequestMapping("/demo")
public class DemoController {

	@RequestMapping("/sayhi")
	@ResponseView(value = "Hi, YMPer!", type = Type.View.TEXT)
    @ResponseHeaders({
            @ResponseHeader(name = "X-From", value = "China"),
            @ResponseHeader(name = "X-Age", value = "18", type = Type.HeaderType.INT)})
	public void sayHi() {
	}
}
```

本例中使用了 `@ResponseView`、`@ResponseHeaders` 和 `@ResponseHeader` 三个新注解，它们的作用及参数含义是：

### @ResponseView

声明控制器方法默认返回的视图，仅在方法无返回值或返回值无效时使用。

| 配置项 | 描述                                                         |
| ------ | ------------------------------------------------------------ |
| value  | 视图模板文件路径，默认为空                                   |
| type   | 视图文件类型，默认为 `Type.View.NULL`<br/>取值范围：`BINARY`、`FORWARD`、`FREEMARKER`、`VELOCITY`、`HTML`、`HTTP_STATES`、<br/>`JSON`、`JSP`、`NULL`、`REDIRECT`、`TEXT` |



### @ResponseHeaders

设置控制器方法返回结果时增加响应头参数的注解。

| 配置项 | 描述           |
| ------ | -------------- |
| value  | 响应头参数集合 |



### @ResponseHeader

声明一个响应头键值对。

| 配置项 | 描述                                                         |
| ------ | ------------------------------------------------------------ |
| name   | 响应头键名称，必选参数                                       |
| value  | 响应头值，默认为空                                           |
| type   | 响应头值类型，默认为 `Type.HeaderType.STRING`<br/>取值范围：`STRING`、`INT`、`DATE` |




## 控制器参数（Parameter）

WebMVC 模块不但让编写控制器变得非常简单，处理请求参数也变得更加容易！WebMVC 会根据控制器方法参数或类成员的注解配置，自动转换与方法参数或类成员对应的数据类型，参数的绑定涉及以下注解：



### 基本参数注解

#### @RequestParam

绑定请求中的参数。

#### @RequestHeader

绑定请求头中的参数变量。

#### @CookieVariable

绑定Cookie中的参数变量。



以上注解拥有以下相同的配置参数：

| 配置项       | 描述                                                         |
| ------------ | ------------------------------------------------------------ |
| prefix       | 绑定的参数名称前缀，默认为空                                 |
| value        | 绑定的参数名称，若未指定则默认采用方法参数变量名             |
| defaultValue | 默认值，默认为空                                             |
| fullScope    | 是否尝试其它作用域下获取参数值，默认为 `false`<br/>优先级顺序：request `>` session `>` application，默认为仅从 request 中获取 |



#### 示例代码

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
        return View.textView(String.format("Hi, %s, UserName: %s, Age: %d, AuthType: %s, IsLogin: %s", name, username, age, authType, isLogin));
    }
}
```

通过浏览器访问 URL 地址：`http://localhost:8080/demo/param?name=webmvc&user.name=ymper`，输出结果：

```shell
Hi, webmvc, UserName: ymper, Age: 18, AuthType: BASIC, isLogin: false
```



### 特别的参数注解

#### @PathVariable

绑定请求映射中的路径参数变量。

| 配置项 | 描述                                             |
| ------ | ------------------------------------------------ |
| value  | 绑定的参数名称，若未指定则默认采用方法参数变量名 |



#### 示例一

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

通过浏览器访问URL地址：`http://localhost:8080/demo/path/webmvc/20?user.sex=F`，输出结果：

```shell
Hi, webmvc, Age: 20, Sex: F
```

WebMVC 模块可以通过实现 `IRequestMappingParser` 接口自定义请求路径规则分析器逻辑并通过配置 `request_mapping_parser_class` 参数使其生效，当使用默认的请求路径规则分析器时，路径中变量可以不连续，即以下格式均能正确解析：

> - 正确：`/path/{name}/{age}`
>- 正确：`/path/{name}/age/{sex}`



#### @ModelBind

对象参数绑定注解。

| 配置项 | 描述                                   |
| ------ | -------------------------------------- |
| prefix | 绑定的参数名称前缀，可选参数，默认为空 |



#### 示例二

```java
public class MemberDTO {

    @PathVariable
    private String name;

    @RequestParam
    private String sex;

    @RequestParam(prefix = "ext")
    private Integer age;

	// 省略Get和Set方法
}

@Controller
@RequestMapping("/demo")
public class DemoController {

	@RequestMapping("/bind/{demo.name}")
    public IView testBind(@ModelBind(prefix = "demo") MemberDTO member) {
        return View.textView(String.format("Hi, %s, Age: %d, Sex: %s", member.getName(), member.getAge(), member.getSex()));
    }
}
```

通过浏览器访问 URL 地址：`http://localhost:8080/demo/bind/webmvc?demo.sex=F&demo.ext.age=20`，输出结果：

```shell
Hi, webmvc, Age: 20, Sex: F
```



#### @SplitArrayWith

字符串数组拆分注解。

| 配置项    | 描述                                            |
| --------- | ----------------------------------------------- |
| separator | 指定的用于拆分字符串数组的分隔符，默认值为：`,` |



#### 示例三

```java
@Controller
@RequestMapping("/demo")
public class ArrayController {

    @RequestMapping("/splitArray")
    public IView split(@RequestParam @SplitArrayWith String[] names) throws Exception {
        return View.textView(StringUtils.join(names, ", "));
    }
}
```

通过浏览器访问 URL 地址：`http://localhost:8080/demo/splitArray?names=A&names=B&names=C`，输出结果：

```shell
A, B, C
```

在请求的 URL 地址中有多个 `names` 参数，这是最常用的数组类型参数传递方式，但在拼装URL参数时相对比较麻烦，这时我们可以通过 `@SplitArrayWith` 注解，让其支持以下方式进行数组参数的传递：

```shell
http://localhost:8088/demo/splitArray?names=A,B,C
```

数组元素之间的分隔符默认为 `,`，可以通过注解中的 `separator` 参数自定义。



## 参数签名验证

为了保障接口调用的安全性，验证接口访问者身份的合法、有效，避免请求参数被非法篡改等，目前比较简单、有效的方法是对请求的参数进行签名，参数签名需要将请求中的参数按照规则并行拼装并使用客户端与服务端约定的密钥进行加密，WebMVC 模块将常用的签名逻辑进行封装并允许通过 `@SignatureValidate` 注解进行自定义配置。

### @SignatureValidate

用于开启和配置参数签名验证规则。

| 配置项         | 描述                     |
| -------------- | ------------------------ |
| paramName      | 签名参数名称             |
| nonceName      | 随机参数名称             |
| encode         | 是否进行URLEncoder编码   |
| upperCase      | 是否转换签名字符串为大写 |
| disabled       | 是否已禁用               |
| excludedParams | 排除的参数名称集合       |
| validatorClass | 签名验证器类             |
| parserClass    | 签名参数分析器类型       |
| processorClass | 附加签名参数处理器类型   |

### 默认实现的签名规则

- 将所有参于签名的参数放入集合中并将参数名称按 `ASCII` 码从小到大（字典序）排序；

- 参数值为空不参与签名；

- 参数名称区分大小写且参数内容不要进行 `URLEncoder` 编码处理；

- 签名参数（如：`sign`）本身不参与签名；

- 将参与签名的参数以URL键值对的格式进行拼接，示例如下：

  ```java
  String signStr = "client_id=CLIENT_ID&create_time=1487178385184&event=subscribe&union_id=o6_bmasdasdsad6_2sgVt7hMZOPfL";
  ```

- 将拼接好的 `signStr` 字符串与 `client_secret` 密钥进行拼接并生成签名，示例如下：

  ```java
  // 拼接密钥
  signStr = signStr + "&client_secret=6bf18fa2f9a136273fb90e58dff4a964";
  // 执行MD5签名并将字符转换为大写
  signStr = MD5(signStr).toUpperCase();
  ```

- 如果有必要，可以在请求参数集合中增加随机参数（如：`nonce`），通过随机数函数生成并转换为字符串，从而保证签名的不可预测性；

- 客户端与服务端均采用相同规则进行参数签名后进行结果比对，两端签名结果一致则验签通过；



### 示例代码

```java
@Controller
@RequestMapping("/demo")
public class SignController {

    @RequestMapping("/sign")
    @EnableSnakeCaseParam
    @SignatureValidate(nonceName = "nonce")
    public IView sign(@RequestParam String clientId,
                      @RequestParam Long createTime,
                      @RequestParam String event,
                      @RequestParam String unionId) throws Exception {
        return View.textView("Hi!");
    }
}
```

通过浏览器访问 URL 地址：`http://localhost:8080/demo/sign?client_id=...&create_time=...&nonce=...&sign=...`，若请求中缺少签名所需参数或验签失败时，都将响应如下错误（以JSON格式为例）报文：

```json
{"ret":-8,"msg":"参数签名无效","data":{}}
```



## 开启SnakeCase参数命明方式

在上例代码中，我们用到了 `@EnableSnakeCaseParam` 注解，该注解可以应用在包、类和方法之上，它的作用是将驼峰式参数名称自动转换为采用下划线分隔的方式，上例中的参数名称转换后的结果如下：

| 原参数名称 | 转换后      |
| ---------- | ----------- |
| clientId   | client_id   |
| createTime | create_time |
| event      | event       |
| unionId    | union_id    |



## 非单例控制器的特殊用法

控制器按其运行模式可以分为单例和非单例两种，单例控制器与非单例控制器的区别：

- 单例控制器类在向模块注册时就已经被实例化。
- 非单例控制器类则是在每次接收到请求时都将创建实例对象，请求结束后该实例对象将被释放。

综上所述，非单例控制器是可以通过类成员来接收请求参数的，示例代码如下：

```java
@Controller(singleton = false)
@RequestMapping("/demo")
public class DemoController {

	@RequestParam
	private String content;

    @RequestMapping("/sayHi")
    public IView sayHi(@RequestParam String name) {
        return View.textView("Hi, " + name + ". " + content);
    }
}
```

通过浏览器访问 URL 地址：`http://localhost:8080/demo/sayHi?name=YMPer&content=Welcome!`，输出结果：

	Hi, YMPer. Welcome!

:::tip **注意**：

在单例模式下，WebMVC 模块将忽略对控制器类成员赋值，同时也建议在单例模式下不要使用成员变量做为参数，在并发多线程环境下会产生意想不到的问题！！

:::



## 环境上下文对象（WebContext）

为了让开发人员能够随时随地获取和使用像 Application、Request、Response、Session 等 Web 容器对象，在 WebMVC 模块中提供了一个名叫 WebContext 的 Web 环境上下文封装类，简单又实用，先了解一下它提供的一些方法：

### 获取Web容器对象

- 获取 ServletContext 对象：

```java
WebContext.getServletContext();
```

- 获取 HttpServletRequest 对象：

```java
WebContext.getRequest();
```

- 获取 HttpServletResponse 对象：

```java
WebContext.getResponse();
```

- 获取 PageContext 对象：

```java
WebContext.getPageContext();
```



### 获取WebMVC容器对象

- 获取 IRequestContext 对象：该对象为 WebMVC 请求上下文接口，主要用于分析请求路径及存储相关参数。

```java
WebContext.getRequestContext();
```

- 获取 WebContext 对象当前线程实例：

```java
WebContext.getContext();
```

WebContext 将 Application、Session、Request 等 Web 容器对象的属性转换成 Map 映射存储，同时向 Map 的赋值也将自动同步至 Web 容器对象中，起初的目的是为了能够方便代码移植并脱离 Web 环境依赖进行开发测试（功能参考 Struts2）：

- WebContext.getContext().getApplication();

- WebContext.getContext().getSession();

- WebContext.getContext().getAttribute(Type.Context.REQUEST);

- WebContext.getContext().getAttributes();
- WebContext.getContext().getLocale();
- WebContext.getContext().getOwner();
- WebContext.getContext().getParameters();



### WebContext对象方法

#### Application相关

- boolean getApplicationAttributeToBoolean(String name);

- int getApplicationAttributeToInt(String name);

- long getApplicationAttributeToLong(String name);

- String getApplicationAttributeToString(String name);

- <T\> T getApplicationAttributeToObject(String name);

- WebContext addApplicationAttribute(String name, Object value)

#### Session相关

- boolean getSessionAttributeToBoolean(String name);

- int getSessionAttributeToInt(String name);

- long getSessionAttributeToLong(String name);

- String getSessionAttributeToString(String name);

- <T\> T getSessionAttributeToObject(String name);

- WebContext addSessionAttribute(String name, Object value)

#### Request相关

- boolean getRequestAttributeToBoolean(String name);
- int getRequestAttributeToInt(String name);
- long getRequestAttributeToLong(String name);
- String getRequestAttributeToString(String name);
- <T\> T getRequestAttributeToObject(String name);
- WebContext addRequestAttribute(String name, Object value)

#### Parameter相关

- boolean getParameterToBoolean(String name);

- int getParameterToInt(String name)

- long getParameterToLong(String name);

- String getParameterToString(String name);

#### Attribute相关

- <T\> T getAttribute(String name);
- WebContext addAttribute(String name, Object value);

#### 上传文件相关

- IUploadFileWrapper getUploadFile(String name);
- IUploadFileWrapper[] getUploadFiles(String name);
- Set<IUploadFileWrapper\> getUploadFiles();



## 文件上传（Upload）

WebMVC 模块针对文件的上传处理以及对上传的文件操作都非常的简单，通过注解就轻松搞定：

### @FileUpload

声明控制器方法需要处理上传的文件流，无参数。

需要注意的是文件上传处理的表单 `enctype` 属性值必须是 `multipart/form-data`，示例：

```html
<form action="/demo/upload" method="POST" enctype="multipart/form-data">
......
</form>
```

### IUploadFileWrapper

上传文件包装器接口，自定义的控制器方法参数类型，用于包装已上传的文件对象并提供对文件操作的一系列方法：

| 方法名称                       | 描述                     |
| ------------------------------ | ------------------------ |
| String getPath()               | 获取完整的文件名及路径   |
| String getName()               | 获取文件名称             |
| long getSize()                 | 获取文件大小             |
| File getFile()                 | 获取临时文件对象         |
| String getContentType()        | 获取文件 Content-Type 值 |
| void transferTo(File dest)     | 转移文件                 |
| void writeTo(File dest)        | 保存文件                 |
| void delete()                  | 删除文件                 |
| InputStream getInputStream()   | 获取文件输入流对象       |
| OutputStream getOutputStream() | 获取文件输出流对象       |



### 示例一

```java
@Controller
@RequestMapping("/demo")
public class UploadController {

    // 处理单文件上传
    @RequestMapping(value = "/upload", method = Type.HttpMethod.POST)
    @FileUpload
    public IView doUpload(@RequestParam IUploadFileWrapper file) throws Exception {
        // 获取文件名称
        file.getName();
        // 获取文件大小
        file.getSize();
        // 获取完整的文件名及路径
        file.getPath();
        // 获取文件Content-Type
        file.getContentType();
        // 转移文件
        file.transferTo(new File("/temp", file.getName()));
        // 保存文件
        file.writeTo(new File("/temp", file.getName()));
        // 删除文件
        file.delete();
        // 获取文件输入流对象
        file.getInputStream();
        // 获取文件输出流对象
        file.getOutputStream();
        // ......
        return View.nullView();
    }

    // 处理多文件上传
    @RequestMapping(value = "/uploads", method = Type.HttpMethod.POST)
    @FileUpload
    public IView doUploadBatch(@RequestParam IUploadFileWrapper[] files) throws Exception {
        // ......
        return View.nullView();
    }
}
```



### 文件上传状态监听器

WebMVC 模块的文件上传是基于 Apache Commons FileUpload 组件实现的，所以通过其提供的 `ProgressListener` 接口即可实现对文件上传状态的监听，该接口实现类需在文件上传状态监听器（`upload_listener_class`）配置项中指定，属于全局性配置。

### 示例二

本例实现对上传文件的进度计算并将计算结果存储在会话中，以便于前端通过 Ajax 轮循等方式对上传进度值的获取和页面展示。

```java
public class UploadProgressListener implements ProgressListener {

    public void update(long pBytesRead, long pContentLength, int pItems) {
        if (pContentLength == 0) {
            return;
        }
        // 计算上传进度百分比
        double percent = (double) pBytesRead / (double) pContentLength;
        // 将百分比存储在用户会话中
        WebContext.getContext().getSession().put("upload_percent", percent);
    }
}
```



## 视图（View）

WebMVC 支持多种视图技术，包括 Binary、Forward、Freemarker、HTML、HttpStatus、JSON、JSP、Redirect、Text、Velocity 等，也可以通过 IView 接口扩展实现自定义视图。



### 控制器视图的表示方法

- 通过返回 IView 接口类型；
- 通过字符串表达一种视图类型；
- 无返回值或返回值为空，将使用当前 RequestMapping 路径对应的 JSP 视图；
- 任意 Java 对象，将根据自定义响应处理器配置返回相应的报文（默认为 JSON 或 XML 格式）内容；



### 视图文件路径配置

控制器视图文件基础路径，必须是以 `/` 开始和结尾，默认值：`/WEB-INF/templates/`

```properties
ymp.configs.webmvc.base_view_path=/WEB-INF/templates/
```



### 视图对象操作示例

视图文件可以省略扩展名称，通过 IView 接口可以直接设置请求参数和内容类型。

```java
// 通过View对象创建视图对象
IView view = View.jspView("/demo/test")
    .addAttribute("attr1", "value")
    .addAttribute("attr2", 2)
    .addHeader("head", "value")
    .setContentType(Type.ContentType.HTML.getContentType());

// 直接创建视图对象
view = new JspView("/demo/test");

// 下面三种方式的结果是一样的，使用请求路径对应的视图文件返回
view = View.jspView("/demo/test");
view = JspView.bind("/demo/test");
view = new JspView("/demo/test");
```



### 默认提供的视图

#### JSP视图

```java
View.jspView("/demo/test.jsp");
JspView.bind("/demo/test");
new JspView("/demo/test");
// = "jsp:/demo/test"
```



#### Freemarker视图

```java
View.freemarkerView("/demo/test.ftl");
FreemarkerView.bind("/demo/test");
new FreemarkerView("/demo/test");
// = "freemarker:/demo/test"
```



#### Velocity视图

```java
View.velocityView("/demo/test.vm");
VelocityView.bind("/demo/test");
new VelocityView("/demo/test");
// = "velocity:/demo/test"
```



#### Text视图

```java
View.textView("Hi, YMPer!");
TextView.bind("Hi, YMPer!");
new TextView("Hi, YMPer!");
// = "text:Hi, YMPer!"
```



#### HTML视图

````java
View.htmlView(WebContext.getContext().getOwner(), "/demo/test.html");
// = "html:/demo/test"
//
View.htmlView("<p>Hi, YMPer!</p>");
HtmlView.bind("<p>Hi, YMPer!</p>");
new HtmlView("<p>Hi, YMPer!</p>");
````



#### JSON视图

```java
// 直接传递对象
User user = new User();
user.setId("...");
...
View.jsonView(user)
    .keepNullValue()                // 设置保留空值
    .snakeCase()                    // 采用SnakeCase样式输出属性名称
    .withContentType()              // 设置Content-Type响应头信息
    .withJsonCallback("_callback"); // 采用JSONP格式返回

// 传递JSON字符串
View.jsonView("{id:\"...\", ...}");
// = "json:{id:\"...\", ...}"
```



#### 二进制数据流视图

```java
// 下载文件，并重新指定文件名称
View.binaryView(new File("/temp/demo.txt"))
    .useAttachment("测试文本.txt");
// = "binary:/temp/demo.txt:测试文本.txt"
```

:::tip 注意：

使用 `useAttachment` 方法控制浏览器强制下载。

该方法将在响应头中将包含 `Content-Disposition: attachment;filename=xxx.xxx` 信息。

:::



#### 请求转发视图

```java
View.forwardView("/demo/test");
// = "forward:/demo/test"
```



#### 请求重定向视图

```java
View.redirectView("/demo/test");
// = "redirect:/demo/test"
```



#### HTTP状态视图

```java
View.httpStatusView(404);
// = "http_status:404"

View.httpStatusView(500, "系统忙, 请稍后再试...");
// = "http_status:500:系统忙, 请稍后再试..."
```



#### 空视图

```java
View.nullView();
```



## 验证（Validation）

WebMVC 模块已集成验证模块，控制器方法可以直接使用验证注解完成参数的有效性验证，参数是按验证注解的配置顺序执行，由框架自动调用完成验证过程，并允许开发人员通过异常错误处理器（IWebErrorProcessor）接口实现自定义验证执行结果的处理逻辑。

:::tip **特别说明**：

参数验证过程将在控制器配置的拦截器执行完毕后执行，也就是说拦截器中获取的请求参数值并未经过验证，更多详细内容请参阅[验证模块文档](validation)。

:::



WebMVC 模块提供了以下验证器，用于 Web 开发过程中涉及的一些场景：

### @VUploadFile

用于对已上传的文件大小和类型等进行验证。

| 配置项       | 描述                                                |
| ------------ | --------------------------------------------------- |
| min          | 设置最小字节长度，默认为 `0` （表示不限制）         |
| max          | 设置最大字节长度，默认为 `0` （表示不限制）         |
| totalMax     | 上传文件总量最大字节长度，默认为 `0` （表示不限制） |
| contentTypes | 允许的文件类型                                      |
| msg          | 自定义验证消息                                      |



### @VHostName

用于对客户端主机的有效性进行验证。

| 配置项     | 描述                                               |
| ---------- | -------------------------------------------------- |
| checker    | 允许访问的主机名称检测器，若未设置则使用默认检测器 |
| httpStatus | 自定义HTTP响应状态码，默认为 `0`（表示不启用）     |
| msg        | 自定义验证消息                                     |

当使用默认检测器时，需要对扩展参数 `webmvc.allowed_access_hosts` 进行配置，将允许访问或重定向的多个主机名称用 `|` 分隔，若为空则表示不限制。



### @VToken

用于简单令牌有效性验证，该令牌由服务端生成并存储于当前会话中，其作用是防止表单重复提交，适用于传统的网页开发模式。

| 配置项 | 描述                          |
| ------ | ----------------------------- |
| name   | 令牌名称                      |
| reset  | 是否重置令牌，默认值：`false` |
| msg    | 自定义验证消息                |



## 拦截器（Intercept）

WebMVC 模块基于 YMP v2.x 的新特性，原生支持 AOP 方法拦截，更多详细内容请参阅[核心模块文档](core)。



## 缓存（Cache）

### 基于默认方式缓存

WebMVC 模块已集成[缓存模块](cache)，通过 `@Cacheable` 注解即可轻松实现控制器方法的缓存。

本例将使控制器方法执行的结果以会话（SESSION）作用域内缓存180秒，代码如下：

```java
@Controller
@RequestMapping("/demo")
@Cacheable
public class CacheController {

    @RequestMapping("/cache")
    @Cacheable(scope = ICaches.Scope.SESSION, timeout = 180)
    public IView doCacheable(@RequestParam String content) throws Exception {
        return View.textView("Content: " + content);
    }
}
```

这里需要说明一下，上述代码中的 `doCacheable` 方法的返回值是 `IView` 接口类型，基于 `@Cacheable` 的方法缓存只是缓存控制器方法返回的 `IView` 视图接口实例对象，并没有对视图的最终执行结果进行缓存，因此在控制器方法上不建议采用此种方式进行缓存处理。

### 自定义缓存处理器

WebMVC 模块提供了缓存处理器 `IWebCacheProcessor` 接口并提供了该接口的默认实现用以缓存视图的执行结果，可以让开发者通过此接口对控制器执行结果进行最终处理，该接口作用于被声明 `@ResponseCache` 注解的控制器类和方法上。

:::tip **特别需要注意**：

当使用缓存处理器对视图的执行结果进行缓存时，请检查 `web.xml` 的 `DispatchFilter` 过滤器中不能含有 `<dispatcher>INCLUDE</dispatcher>` 配置项，否则将会产生死循环。

:::



#### @ResponseCache

| 配置项         | 描述                                                         |
| -------------- | ------------------------------------------------------------ |
| cacheName      | 缓存名称，默认值为 `default`                                 |
| key            | 缓存键名，若未设置则自动生成                                 |
| processorClass | 自定义视图缓存处理器，若未提供则采用默认实现                 |
| scope          | 缓存作用域，可选值为 `APPLICATION`、`SESSION` 和 `DEFAULT`，默认为 `DEFAULT` |
| timeout        | 缓存数据超时时间，默认为 `0`，即使用默认缓存数据超时时间     |
| useGZip        | 是否使用 GZIP 压缩，默认值为 `true`                          |

#### 缓存处理器使用示例

本例是在示例一的基础上进行调整，以实现对控制器方法返回的视图等对象的执行结果进行缓存：

```java
@Controller
@RequestMapping("/demo")
public class CacheController {

	@RequestMapping("/cache")
	@ResponseCache(scope = ICaches.Scope.SESSION, timeout = 180)
	public IView doCacheable(@RequestParam String content) throws Exception {
		return View.textView("Content: " + content);
	}
}
```

#### 缓存处理器使用注意事项

- 由于缓存处理器会判断当前请求的响应状态值，仅状态为 `200` 的响应结果会被缓存。但需要注意的是 WebMVC 框架的默认错误处理器在捕获到异常或参数验证结果时，其响应状态码仍然是 `200`，也就是说这种情况下会将错误的执行结果缓存，而这并不是我们想要的结果。因些，需要设置 `webmvc.error_with_status_code` 扩展配置参数值为 `true`，以对参数验证、异常错误等情况发生时，让其返回非 `200` 的状态码。
- 尽量提前指定缓存的 `key` 值或通过 `key` 参数值以  `#`  开头来指定使用某个特定的请求参数值做为当前缓存的 `key` 值，或自行实现更可靠缓存 `key` 生成规则，避免使用框架默认的自动生成规则，因为这种生成方式存在的问题是请求参数数量由请求端控制，可能存在安全风险。



## 跨域（CORS）

**名词解释**：CORS（Cross-Origin Resource Sharing，跨域资源共享）是一个系统，它由一系列传输的 HTTP 头组成，这些 HTTP 头决定浏览器是否阻止前端 JavaScript 代码获取跨域请求的响应。同源安全策略 默认阻止“跨域”获取资源。但是 CORS 给了 Web 服务器这样的权限，即服务器可以选择，允许跨域请求访问到它们的资源。

WebMVC 框架从 `2.1.0` 版开始基于最新的拦截器注解机制重构了跨域配置和使用方式，主要使用以下两个注解便可轻松实现跨域支持：

### @CrossDomain

声明开启跨域处理的拦截器注解，可以应用在控制器所在的包、类或方法之上，该注解无任何参数配置。

由于它本身的实现方式就是一个前置拦截器，当在使用 `@Clean` 注解进行拦截器清理时，它也将被一并清理，因此在使用过程中需要注意。

### @CrossDomainSetting

声明自定义跨域配置注解，当某个（些）控制器请求跨域的规则与全局配置不同的情况下使用，默认使用全局配置即可，可以应用在控制器所在的包、类或方法之上，尽管其配置参数项与 `@EnableCrossDomainSettings` 注解相同，但在执行过程中将优先于默认全局配置。

| 配置项                | 描述                             |
| --------------------- | -------------------------------- |
| optionsAutoReply      | 针对 OPTIONS 请求是否自动回复    |
| allowedCredentials    | 是否允许跨域请求带有验证信息     |
| maxAge                | 跨域请求响应的最大缓存时间（秒） |
| allowedOrigins        | 允许跨域的原始主机               |
| allowedOriginsChecker | 允许跨域的主机名称检测器         |
| allowedMethods        | 允许跨域请求的方法               |
| allowedHeaders        | 允许跨域请求携带的 Header 信息   |
| exposedHeaders        | 允许跨域访问的 Header 信息       |



### 示例代码

```java
@Controller
@RequestMapping("/cors")
@CrossDomainSetting(optionsAutoReply = true, allowedCredentials = true, maxAge = 0)
public class CorsController {

    @RequestMapping(value = "/", method = {Type.HttpMethod.OPTIONS, Type.HttpMethod.POST})
    @CrossDomain
    public IView cors() throws Exception {
        return View.textView("Everything depends on ability!  -- YMP :)");
    }
}
```



### 跨域相关配置

#### 配置文件参数说明

```properties
#-------------------------------------
# 跨域相关参数配置
#-------------------------------------

# 是否开启跨域设置, 可选参数, 默认值: false
ymp.configs.webmvc.cross_domain_settings_enabled=

# 针对OPTIONS请求是否自动回复, 默认值: false
ymp.configs.webmvc.cross_domain_options_auto_reply=

# 允许跨域的原始主机, 多个主机名称用'|'分隔, 可选参数, 默认值: *
ymp.configs.webmvc.cross_domain_allowed_origins=

# 允许跨域的主机名称检测器, 可选参数, 此类需实现net.ymate.platform.webmvc.validate.IHostNameChecker接口, 默认值: 空
ymp.configs.webmvc.cross_domain_allowed_origins_checker_class=

# 允许跨域请求的方法, 多个方法名称用'|'分隔, 可选参数, 默认值: 空
ymp.configs.webmvc.cross_domain_allowed_methods=

# 允许跨域请求携带的Header信息, 多个Header名称用'|'分隔, 可选参数, 默认值: 空
ymp.configs.webmvc.cross_domain_allowed_headers=

# 允许跨域访问的Header信息, 多个Header名称用'|'分隔, 可选参数, 默认值: 空
ymp.configs.webmvc.cross_domain_exposed_headers=

# 是否允许跨域请求带有验证信息, 可选参数, 默认值: false
ymp.configs.webmvc.cross_domain_allowed_credentials=

# 跨域请求响应的最大缓存时间(秒), 可选参数, 默认值: 0
ymp.configs.webmvc.cross_domain_max_age=
```



#### 配置注解参数说明

##### @EnableCrossDomainSettings

| 配置项                | 描述                             |
| --------------------- | -------------------------------- |
| optionsAutoReply      | 针对 OPTIONS 请求是否自动回复    |
| allowedCredentials    | 是否允许跨域请求带有验证信息     |
| maxAge                | 跨域请求响应的最大缓存时间（秒） |
| allowedOrigins        | 允许跨域的原始主机               |
| allowedOriginsChecker | 允许跨域的主机名称检测器         |
| allowedMethods        | 允许跨域请求的方法               |
| allowedHeaders        | 允许跨域请求携带的 Header 信息   |
| exposedHeaders        | 允许跨域访问的Header信息         |



## Cookies

WebMVC 模块针对 Cookies 这个小甜点提供了一个名为 CookieHelper 的小工具类，支持 Cookie 参数的设置、读取和移除等操作，同时支持对其编码和加密处理，并允许通过配置参数调整 Cookie 策略。

### 示例代码：演示Cookie操作

```java
// 创建CookieHelper对象
CookieHelper helper = CookieHelper.bind()
    // 设置开启采用密钥加密(将默认开启Base64编码)
    .allowUseAuthKey()
    // 设置开启采用Base64编码(默认支持UrlEncode编码)
    .allowUseBase64()
    // 设置开启使用HttpOnly
    .allowUseHttpOnly()
    // 添加或重设Cookie，过期时间基于Session时效
    .setCookie("current_username", "YMPer")
    // 添加或重设Cookie，并指定过期时间
    .setCookie("current_username", "YMPer", 1800);
// 获取指定名称的Cookie值
helper.getCookie("current_username").toStringValue();
// 获取全部Cookie
helper.getCookies()
    .forEach((key, value) -> System.out.printf("name: %s, value: %s%n", key, value.toStringValue()));
// 移除指定名称的Cookie
helper.removeCookie("current_username");
// 清理所有的Cookie
helper.clearCookies();
```



## 国际化（I18N）

基于 YMP v2.x 框架 I18N 支持，整合 WebMVC 模块并提供了默认 II18NEventHandler 接口实现，其默认加载当前语言设置的步骤：

1. 通过 `webmvc.i18n_language_param_name` 加载语言设置参数名称，默认值为：`_lang`
2. 尝试加载请求作用域中 `_lang` 参数值；
3. 尝试从 Cookies 中加载 `_lang` 参数值；
4. 使用系统默认语言设置；



## 约定模式（Convention Mode）

**名词解释**：约定优于配置（Convention Over Configuration），也称作按约定编程，是一种软件设计范式，通过命名规则之类的约束来减少程序中的配置，旨在减少软件开发人员需要做决定的数量，获得简单的好处，而又不失灵活性。

有些时候我们仅仅是为了能够访问一个视图文件而不得不编写一个控制器方法与之对应，当这种重复性的工作很多时，就会增加大量无用代码，因此，在 WebMVC 模块中，通过开启约定模式即可支持直接访问 `base_view_path` 路径下的视图文件，无需编写任何代码。

WebMVC 模块的约定模式默认为关闭状态，需要通过 `convention_mode` 配置参数开启。



### 访问权限规则配置

在约定模式模式下，支持设置不同路径的访问权限，规则是：`-` 号代表禁止访问，`+` 或无符串代表允许访问，多个路径间用 `|` 分隔；

访问权限示例：禁止访问 `admin` 目录和 `index.jsp` 文件，目录结构如下：

```shell
WEB-INF\
|
|--templates\
|	|
|	+--admin\
|	|
|	+--users\
|	|
|	+--reports\
|	|
|	+--index.jsp
|	|
|	<...>
```

示例参数配置：

```properties
ymp.configs.webmvc.convention_view_paths=admin-|index-|users|reports+
```



### 拦截器规则配置

由于在约定模式下，访问视图文件无需控制器，所以无法通过控制器方法添加拦截器配置，因此，WebMVC 模块针对约定模式单独提供了拦截器规则配置这一扩展功能，主要是通过 `@InterceptorRule` 注解并配合 `IInterceptorRule` 接口使用。

拦截器规则设置默认为关闭状态，需要通过 `convention_interceptor_mode` 配置参数开启。



拦截规则配置示例：

```java
@InterceptorRule("/demo")
@Before(WebUserSessionCheck.class)
public class InterceptRuleDemo implements IInterceptorRule {

    @InterceptorRule("/admin/*")
    @Before(AdminTypeCheckFilter.class)
    public void adminAll() {
    }

    @Clean
    @InterceptorRule("/admin/login")
    public void adminLogin() {
    }

    @InterceptorRule("/user/*")
    public void userAll() {
    }

    @InterceptorRule("/mobile/person/*")
    public void mobilePersonAll() {
    }
}
```

:::tip **说明：**

@InterceptorRule：拦截器规则注解；

- 在实现 `IInterceptorRule` 接口的类上声明，表示该类为拦截规则配置；
- 在类方法上声明，表示针对一个具体的请求路径配置规则，与 `@RequestMapping` 的作用相似；

规则配置中支持的注解：

- @Before：约定模式下的拦截器仅支持 `@Before` 前置拦截；
- @Clean：清理上层指定的拦截器；
- @ContextParam：上下文参数；
- @ResponseCache：声明控制器方法返回视图对象的执行结果将被缓存；

:::



:::tip **注意**：

配置规则类的方法可以是任意的，方法本身无任何意义，仅是通过方法使用注解。

:::



### URL伪静态

WebMVC 模块通过约定模式可以将参数融合在 URL 中，不再通过 `?` 传递参数，让 URL 看上去更好看一些。

伪静态模式默认为关闭状态，需要通过 `convention_url_rewrite_mode` 配置参数开启。

伪静态模式下的参数传递规则：

- URL 中通过分隔符`_`传递多个请求参数；
- 通过 `UrlParams[index]` 方式引用参数值；

假如，URL 原始格式为：

```shell
http://localhost:8080/user/info/list?type=all&page=2&page_size=15
```

其对应的 URL 伪静态格式如下:

```shell
http://localhost:8080/user/info/list_all_2_15
```

在 JSP 中可以通过 EL 表达式获取请求参数的引用：

```jsp
${UrlParams[0]}：all
${UrlParams[1]}：2
${UrlParams[2]}：15
```

:::tip **注意**：

伪静态参数必须是连续的，`UrlParams` 参数集合存储在 Request 作用域内。

:::



### 约定模式相关配置

#### 配置文件参数说明

```properties
#-------------------------------------
# Convention模式相关参数配置
#-------------------------------------

# 是否开启视图自动渲染(约定优于配置, 无需编写控制器代码, 直接匹配并执行视图)模式, 可选参数, 默认值: false
ymp.configs.webmvc.convention_mode=

# Convention模式开启时视图文件路径(基于base_view_path的相对路径, '-'号代表禁止访问, '+'或无符串代表允许访问), 多个路径间用'|'分隔, 可选参数, 默认值: 空(即不限制访问路径)
ymp.configs.webmvc.convention_view_paths=

# Convention模式开启时是否采用URL伪静态(URL中通过分隔符'_'传递多个请求参数, 通过UrlParams[index]方式引用参数值)模式, 可选参数, 默认值: false
ymp.configs.webmvc.convention_url_rewrite_mode=

# Convention模式开启时是否采用拦截器规则设置, 可选参数, 默认值: false
ymp.configs.webmvc.convention_interceptor_mode=
```



#### 配置注解参数说明

##### @EnableConventionMode

| 配置项            | 描述                                    |
| ----------------- | --------------------------------------- |
| urlRewriteMode    | 是否采用URL伪静态，默认值：`false`      |
| interceptorMode   | 是否采用拦截器规则设置，默认值：`false` |
| viewAllowPaths    | 允许访问的视图文件路径集合，默认值：空  |
| viewNotAllowPaths | 禁止访问的视图文件路径集合，默认值：空  |



## 面向接口开发

现如今，基于 B/S 架构的业务系统的开发工作不再是传统的 MVC 模式（即服务端渲染页面），随着前端技术的不断发展，前后端分离已成为互联网项目开发的业界标准，此时的前端与后端之间完全基于开放的 API 接口进行通信，一般采用  JSON 或 XML 作为底层通信协议。

为了能够统一接口的响应结构和规范异常错误处理，框架分别提供了 WebResult、ErrorCode 和 IWebResultBuilder 辅助工具类。

### WebResult

WebResult 是框架针对接口开发提供的默认响应报文结构实现类，其中包括响应码、消息描述和业务数据内容等。响应报文结构需实现 `IWebResult<CODE_TYPE extends Serializable>` 接口，其中 `CODE_TYPE` 泛型用于指定响应码 `ret` 的数据类型，框架默认实现使用的是 `Integer` 类型。

示例代码：

```java
WebResult.create(-1)
    .msg("请求参数验证无效")
    .dataAttr("username", "用户名称为必填项.");
```

响应报文结构如下：

```json
{
	"ret":-1,
	"msg":"请求参数验证无效",
	"data":{
		"username":"用户名称为必填项."
	}
}
```

默认响应属性说明：

| 名称 | 类型    | 描述                                                       |
| :--- | :------ | :--------------------------------------------------------- |
| ret  | Integer | 响应码                                                     |
| msg  | String  | 消息描述                                                   |
| data | Object  | 业务数据内容，根据业务逻辑决定其具体数据类型及是否为必须项 |

当控制器方法返回的对象是基于 `IWebResult` 接口实现的响应报文结构类型时，控制器将根据框架配置及客户端请求的方式判断其最终的输出格式，规则如下：

1. 首先，判断请求头 `Accept` 值是否包含 `application/json` 或请求参数中 `format=json` 是否存在，条件成立则采用 `JSON` 格式输出；
2. 其次，判断请求头 `Accept` 值是否包含 `application/xml` 或请求参数 `format=xml` 是否存在，条件成立则采用 `XML` 格式输出；
3. 若以上均不成立则以 `JSP` 视图格式输出。



### IWebResultBuilder

上例中，通过 `WebResult.create` 方法构建的响应报文结构对象是基于框架提供的默认实现，可能并不满足于您的需求，可以通过 `IWebResultBuilder` 接口实现自定义的响应报文结构类，该接口实现类是采用 `SPI` 机制加载，因此需要在 `META-INF/services/internal/` 或 `META-INF/services/` 目录下创建名称为 `net.ymate.platform.webmvc.IWebResultBuilder` 的文件，其内容为自定义响应报文结构类全名。

若要使自定义的响应报文结构类生效，需要使用 `WebResult.builder` 方法构建，当加载过程中出现任何问题，框架将仍使用默认实现。

代码的编写与上例非常相似，调整后内容如下：

```java
WebResult.builder()
    .code(-1)
    .msg("请求参数验证无效")
    .dataAttr("username", "用户名称为必填项.");
```



### ErrorCode

在接口开发过程中，一般会优先根据整体业务分类和逻辑制定一整套的错误代码，此时可以通过框架提供的 `ErrorCode` 类来完成。

错误码定义示例代码：

```java
public static final ErrorCode ERROR_USER_NOT_EXIST = ErrorCode.create(10010, "用户不存在");
```

通过错误码对象构建响应报文结构示例代码：

```java
// 通过ErrorCode对象构建：
WebResult.builder(ERROR_USER_NOT_EXIST);
// 相当于：
WebResult.builder().code(10010).msg("用户不存在");
```

两种书写方式对比之下，通过错误码常量进行响应报文构建的代码更简洁，避免书写过程中出错，修改起来也更方便。

框架也将开发过程中经常用到的错误代码封装成辅助工具类方法供开发人员使用，调用方法如下：

| 响应码 | 消息描述                               | 调用方法                                   |
| :----- | :------------------------------------- | ------------------------------------------ |
| 0      | 请求成功                               | ErrorCode.succeed()                        |
| -1     | 请求参数验证无效                       | WebErrorCode.invalidParamsValidation()     |
| -2     | 访问的资源未找到或不存在               | WebErrorCode.resourceNotFoundOrNotExist()  |
| -3     | 请求方法不支持或不正确                 | WebErrorCode.requestMethodNotAllowed()     |
| -4     | 请求的资源未授权或无权限               | WebErrorCode.requestResourceUnauthorized() |
| -5     | 用户未授权登录或会话已过期，请重新登录 | WebErrorCode.userSessionInvalidOrTimeout() |
| -6     | 请求的操作被禁止                       | WebErrorCode.requestOperationForbidden()   |
| -7     | 用户已经授权登录                       | WebErrorCode.userSessionAuthorized()       |
| -8     | 参数签名无效                           | WebErrorCode.invalidParamsSignature()      |
| -9   | 上传文件大小超出限制   |WebErrorCode.uploadFileSizeLimitExceeded()|
| -10  | 上传文件总大小超出限制 |WebErrorCode.uploadSizeLimitExceeded()|
| -11  | 上传文件类型无效       |WebErrorCode.uploadContentTypeInvalid()|
| -12  | 用户会话确认状态无效   |WebErrorCode.userSessionConfirmationState()|
| -13  | 用户会话已强制下线     |WebErrorCode.userSessionForceOffline()|
| -20  | 数据版本不匹配         |ErrorCode.dataVersionNotMatch()|
| -50  | 系统繁忙，请稍后重试！ |ErrorCode.internalSystemError()|



## 高级特性

### 控制器请求处理器

在 WebMVC 模块中除了支持标准Web请求的处理过程，同时也对基于 XML 和 JSON 协议格式的请求提供支持，有两种使用场景：

#### 场景一：全局设置

通过下面的参数配置，将作用于所有控制器方法，默认为 `default`，可选值为：`default`、`json` 和 `xml`，也可以是开发者自定义的 `IRequestProcessor` 接口实现类名称。

```properties
ymp.configs.webmvc.request_processor_class=default
```



#### 场景二：针对具体的控制器方法进行设置

```java
@Controller
@RequestMapping("/demo")
public class DemoController {

    @RequestMapping("/sayHi")
    @RequestProcessor(JSONRequestProcessor.class)
    public IView sayHi(@RequestParam String name， @RequestParam String content) {
        return View.textView("Hi, " + name + ", Content: " + content);
    }

    @RequestMapping("/sayHello")
    @RequestProcessor(XMLRequestProcessor.class)
    public IView sayHello(@RequestParam String name， @RequestParam String content) {
        return View.textView("Hi, " + name + ", Content: " + content);
    }
}
```

通过 POST 方式向 `http://localhost:8080/demo/sayHi` 发送如下 JSON 数据：

```json
{ "name" : "YMPer", "content" : "Welcome!" }
```

通过 POST 方式向 `http://localhost:8080/demo/sayHello` 发送如下 XM L数据：

```xml
<xml>
	<name>YMPer</name>
	<content><![CDATA[Welcome!]]></content>
</xml>
```

以上这两种协议格式的控制器方法，同样支持参数的验证等特性。



### 控制器执行结果自定义响应处理

通过 `@ResponseBody` 注解可以将控制器方法返回的执行结果对象（`String` 和 `IView` 类型除外）进行自定义输出，默认将以 JSON 格式输出，可以通过 `IResponseBodyProcessor` 接口自定义实现输出方式。

#### @ResponseBody

| 配置项      | 描述                                                         |
| ----------- | ------------------------------------------------------------ |
| value       | 自定义对象输出处理器（即 IResponseBodyProcessor 接口实现类），默认以 JSON 格式输出 |
| contextType | 响应头是否携带 Content-Type 参数项，默认为 `true`            |
| keepNull    | 是否保留空值参数项，默认为 `true`                            |
| snakeCase   | 是否使用下划线分隔属性名称，默认为 `false`                   |

#### 示例代码

```java
public class DemoBean implements Serializable {

    private String name;

    private Integer age;

	// 省略Get和Set方法
}

@Controller
public class HelloController {

    @RequestMapping("/hello")
    @ResponseBody
    public DemoBean hello() throws Exception {
        DemoBean result = new DemoBean();
        result.setName("YMPer");
        result.setAge(10);
        //
        return result;
    }
}
```

执行结果：

```json
{"name":"YMPer","age":10}
```



### 异常错误处理器

#### 方式一：全局设置

WebMVC 模块为开发者提供了一个 `IWebErrorProcessor` 接口，通过该接口允许开发人员针对异常、参数验证结果和约定模式的 URL 解析逻辑实现自定义扩展，若未进行任何配置则框架将使用默认实现。

通过配置 `error_processor_class` 参数进行自定义设置，如下所示：

```properties
ymp.configs.webmvc.error_processor_class=net.ymate.platform.webmvc.impl.DefaultWebErrorProcessor
```

**示例代码**：

```java
public class DemoWebErrorProcessor implements IWebErrorProcessor {

	/**
     * 异常时将执行事件回调
     *
     * @param owner 所属YMP框架管理器实例
     * @param e     异常对象
     */
    public void onError(IWebMvc owner, Throwable e) {
    	// ...你的代码逻辑
    }

	/**
     * @param owner   所属YMP框架管理器实例
     * @param results 验证器执行结果集合
     * @return 处理结果数据并返回视图对象，若返回null则由框架默认处理
     */
    public IView onValidation(IWebMvc owner, Map<String, ValidateResult> results) {
    	// ...你的代码逻辑
    	return View.nullView();
    }

	/**
     * 自定义处理URL请求过程
     *
     * @param owner          所属YMP框架管理器实例
     * @param requestContext 请求上下文
     * @return 可用视图对象，若为空则采用系统默认
     * @throws Exception 可能产生的异常
     */
    public IView onConvention(IWebMvc owner, IRequestContext requestContext) throws Exception {
    	// ...你的代码逻辑
    	return View.nullView();
    }
}
```

#### 方式二：针对具体的控制器方法进行设置

通过 `@ResponseErrorProcessor` 注解并配合 `IResponseErrorProcessor` 接口实现控制器类或方法指定自定义异常处理过程，若自定义异常处理过程执行的返回值为 `null` 时，将交由全局异常处理器进行统一处理。

框架中默认提供了 `DefaultResponseErrorProcessor` 、 `JSONResponseErrorProcessor` 和 `XMLResponseErrorProcessor` 三种实现方式。自定义实现及使用示例如下：

**示例代码**：

```java
public class DemoRequestProcessor implements IResponseErrorProcessor {

    @Override
    public IView processError(IWebMvc owner, Throwable e) {
        return TextView.bind("Error: " + e.getMessage);
    }
}

@Controller
@RequestMapping("/demo")
public class DemoController {

    @RequestMapping("/sayHi")
    @ResponseErrorProcessor(DemoRequestProcessor.class)
    public IView sayHi(@RequestParam String name, @RequestParam String content) {
        // 模拟异常
        System.out.println(1 / 0);
        return View.textView("Hi, " + name + ", Content: " + content);
    }
}
```



#### 方式三：为指定的异常类设置错误响应码和描述信息

在一个自定义异常类上，可以通过 `@ExceptionProcessor` 注解声明其对应的错误码和错误描述信息，框架初始化时将被自动扫描注册。

**@ExceptionProcessor 注解的配置属性说明**：

| 配置项 | 描述                |
| ------ | ------------------- |
| code   | 异常错误码 |
| msg    | 默认错误描述        |

自定义异常类，代码如下：

```java
@ExceptionProcessor(code = "10010", msg = "自定义异常消息")
public class DemoException extends Exception {

    public DemoException() {
    }

    public DemoException(String message) {
        super(message);
    }

    public DemoException(String message, Throwable cause) {
        super(message, cause);
    }

    public DemoException(Throwable cause) {
        super(cause);
    }
}
```

也可以通过手工方式注册，代码如下：

```java
ExceptionProcessHelper.DEFAULT.registerProcessor(DemoException.class, new IExceptionProcessor() {
    @Override
    public Result process(Throwable target) throws Exception {
       return new Result(10010, "自定义异常消息");
    }
});
```

当框架使用默认的异常错误处理器时，它将首先尝试加载对应异常的错误响应配置，其内部处理逻辑代码如下：

```java
IExceptionProcessor exceptionProcessor = ExceptionProcessHelper.DEFAULT.bind(unwrapThrow.getClass());
if (exceptionProcessor != null) {
    IExceptionProcessor.Result result = exceptionProcessor.process(unwrapThrow);
    if (result != null) {
        showErrorMsg(result.getCode(), WebUtils.errorCodeI18n(this.owner, result), result.getAttributes()).render();
    } else {
        doProcessError(unwrapThrow);
    }
} else {
    doProcessError(unwrapThrow);
    showErrorMsg(String.valueOf(ErrorCode.INTERNAL_SYSTEM_ERROR), WebUtils.errorCodeI18n(this.owner, ErrorCode.INTERNAL_SYSTEM_ERROR, ErrorCode.MSG_INTERNAL_SYSTEM_ERROR), null).render();
}
```

上述代码中，通过 `WebUtils.errorCodeI18n(this.owner, result)` 方法的调用，实现尝试优先加载 `code` 响应码对应的国际化资源，国际化资源文件中的配置采用 `webmvc.error_code_<CODE>` 方式，如下所示：

```properties
webmvc.error_code_10010=自定义异常描述
```



:::tip 特别需要注意

相同异常类型不允许重复注册，仅首次注册生效。

:::



以下是框架默认的错误码与其对应的消息描述：


|响应码|消息描述|
|:---|:---|
|0|请求成功|
|-1|请求参数验证无效|
|-2|访问的资源未找到或不存在|
|-3|请求方法不支持或不正确|
|-4|请求的资源未授权或无权限|
|-5|用户未授权登录或会话已过期，请重新登录|
|-6|请求的操作被禁止|
|-7|用户已经授权登录|
|-8|参数签名无效|
|-9|上传文件大小超出限制|
|-10|上传文件总大小超出限制|
|-11|上传文件类型无效|
|-12|用户会话确认状态无效|
|-13|用户会话已强制下线|
|-20|数据版本不匹配|
|-50|系统繁忙，请稍后重试！|

