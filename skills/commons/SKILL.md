# Commons 通用工具包模块

## 1. 模块概览

Commons 通用工具包是 YMP 框架中一个功能丰富的工具类库集合，是在开发 YMP 框架过程中积累下来的一些非常实用的辅助工具。该模块提供了广泛的工具类，涵盖了日常开发中常见的各种操作，如 HTTP 客户端、JSON 处理、文件操作、序列化、字符串处理、日期时间处理等。

- **全面的工具类集合**：提供了大量实用的工具类，涵盖了开发中的各种常见操作
- **HttpClient 封装**：基于 Apache HttpComponents 组件封装的 HttpClient 请求与处理工具
- **JSON 包装器**：为不同的第三方 JSON 解析器提供统一的 API 接口
- **文件及资源管理**：提供了文件操作、资源加载等功能
- **序列化支持**：支持多种序列化方式
- **类型转换**：提供了任意类型对象之间的转换功能
- **工具类丰富**：包括日期时间、数学、经纬度、字符串加解密、运行时环境、网络、线程操作等

## 2. 核心功能

### 2.1 基础类型（Lang）

提供了针对任意对象之间的类型转换、组合和无限层级树型结构的支持。

#### 2.1.1 模糊对象（BlurObject）

用于任意类型对象之间的转换，基本涵盖日常使用的数据类型，通过 `IConverter` 接口实现自定义转换器并支持手动或 `SPI` 方式注册。

#### 2.1.2 结对对象（PairObject）

用于将任意两种类型的对象以 `<K, V>` 的形式组合在一起。

#### 2.1.3 树型对象（TreeObject）

使用级联方式存储各种数据类型，不限层级深度，支持 JSON 转换。

### 2.2 HttpClient

基于 Apache HttpComponents 组件封装的 HttpClient 请求与处理工具，使用时需在工程中引入 httpmime 依赖包。

#### 2.2.1 CloseableHttpClientHelper

支持自定义安全连接方式，支持 GET、POST 请求方法，简化文件上传与下载的处理逻辑等。

#### 2.2.2 CloseableHttpRequestBuilder

在 CloseableHttpClientHelper 类的基础上进行了优化、调整请求构建方式及响应的处理逻辑，除了 GET 和 POST 请求方法之外，还增加了对 PUT、OPTIONS、DELETE、HEAD、PATCH、TRACE 等的支持。

### 2.3 JsonWrapper

JSON 包装器，为了让不同的第三方 JSON 解析器拥有统一的 API 接口调用方式并能够做到灵活切换而不影响业务系统的正常运行而提供的一套完整的包装层实现，现已对当前比较流行且使用非常广泛的 FastJson、Gson 和 Jackson 等进行了封装与适配。

### 2.4 Markdown

对 Markdown 语法中常用到的格式，如：标题、文本、引用、表格、代码片段、图片、连接等进行对象封装，避免以往采用字符串拼接形式中经常出现的问题。

### 2.5 序列化（Serialize）

基于 `ISerializer` 接口实现对象序列化与反序列化操作，由 `SerializerManager` 对象序列化管理器维护管理，支持通过 `SPI` 机制和自动扫描 `@Serializer` 注解方式加载并注册，默认提供了几种实现方式：

- DefaultSerializer：基于 Java 对象序列化实现
- JSONSerializer：基于 JSON 对象序列化实现
- HessianSerializer：基于 Hessian 二进制对象序列化实现
- FstSerializer：基于 FST 二进制对象序列化实现

### 2.6 重试机制（Retry）

重试机制提供了一套完整的重试策略实现，用于处理网络请求、服务调用等可能失败的操作。该模块支持自定义重试次数、延迟策略、超时控制以及异常类型过滤等功能。

#### 2.6.1 核心组件

- `IRetryable<T>`：可重试操作接口，定义需要执行重试逻辑的操作
- `IRetryDelayStrategy`：延迟策略接口，定义重试之间的延迟计算逻辑
- `RetryConfig`：重试配置类，用于定义重试行为的相关参数
- `RetryUtils`：重试工具类，提供便捷的重试操作执行方法

#### 2.6.2 延迟策略

提供了三种内置的延迟策略：

- **FixedDelayStrategy**：固定延迟策略，每次重试使用相同的延迟时间
- **ExponentialDelayStrategy**：指数延迟策略，延迟时间随重试次数指数增长
- **RandomDelayStrategy**：随机延迟策略，在指定范围内随机选择延迟时间

### 2.7 Utils

提供包含类与反射、字符串加密与解密、地理位置与编码、日期时间、正则表达式、文件、网络、参数、资源、运行时、线程操作等常用工具类封装。

#### 2.7.1 ClassUtils

类操作相关工具类，包括扩展类加载器（ExtensionLoader）、类包裹器（BeanWrapper）等。

#### 2.7.2 其他工具类

- DateUtils：日期时间处理工具
- DigestUtils：摘要工具，提供 MD5、SHA1 等摘要算法
- FileUtils：文件操作工具
- NetworkUtils：网络操作工具
- ParamUtils：参数处理工具
- ResourceUtils：资源加载工具
- RuntimeUtils：运行时环境工具
- ThreadUtils：线程操作工具
- StringUtils：字符串处理工具
- MathUtils：数学计算工具
- GeoUtils：地理位置与编码工具

## 3. 核心 API/类/函数

### 3.1 BlurObject

**主要方法**：
- `bind(Object target)`：绑定目标对象
- `toIntValue()`：转换为 int 值
- `toLongValue()`：转换为 long 值
- `toDoubleValue()`：转换为 double 值
- `toStringValue()`：转换为 String 值
- `toObjectValue(Class<T> targetClass)`：转换为指定类型的对象
- `registerConverter(Class<?> fromClass, Class<?> toClass, IConverter<?> converter)`：注册自定义类型转换器

### 3.2 PairObject

**主要方法**：
- `bind(K key, V value)`：绑定键值对
- `getKey()`：获取键
- `getValue()`：获取值

### 3.3 TreeObject

**主要方法**：
- `put(String key, Object value)`：添加键值对
- `get(String key)`：获取指定键的值
- `add(Object value)`：添加元素到集合
- `toJson()`：转换为 JSON 对象
- `fromJson(String jsonStr)`：从 JSON 字符串创建 TreeObject 对象

### 3.4 CloseableHttpClientHelper

**主要方法**：
- `create()`：创建 CloseableHttpClientHelper 实例
- `get(String url, Header[] headers, String charset)`：发送 GET 请求
- `post(String url, ContentType contentType, String content)`：发送 POST 请求
- `upload(String url, String fieldName, File file)`：上传文件
- `download(String url, IFileHandler fileHandler)`：下载文件

### 3.5 CloseableHttpRequestBuilder

**主要方法**：
- `create(String url)`：创建 CloseableHttpRequestBuilder 实例
- `contentType(ContentType contentType)`：设置内容类型
- `connectionTimeout(int connectionTimeout)`：设置连接超时时间
- `requestTimeout(int requestTimeout)`：设置请求超时时间
- `socketTimeout(int socketTimeout)`：设置 socket 超时时间
- `charset(String charset)`：设置字符集
- `addHeaders(Header[] headers)`：添加请求头
- `addParams(Map<String, String> params)`：添加请求参数
- `addContent(String fieldName, File file)`：添加文件内容
- `build()`：构建请求
- `get()`：发送 GET 请求
- `post()`：发送 POST 请求
- `put()`：发送 PUT 请求
- `delete()`：发送 DELETE 请求

### 3.6 JsonWrapper

**主要方法**：
- `createJsonObject(boolean ordered)`：创建 JSON 对象
- `createJsonArray(Object[] values)`：创建 JSON 数组
- `fromJson(String jsonStr)`：从 JSON 字符串创建 JsonWrapper 对象
- `serialize(Object object, boolean pretty)`：序列化对象
- `deserialize(byte[] bytes, TypeReferenceWrapper<T> typeReference)`：反序列化对象
- `toJsonString(Object object, boolean pretty, boolean keepNulls, boolean snakeCase)`：将对象转换为 JSON 字符串

### 3.7 SerializerManager

**主要方法**：
- `getDefaultSerializer()`：获取默认序列化器
- `getSerializer(String name)`：获取指定名称的序列化器

### 3.8 ClassUtils

**主要方法**：
- `getExtensionLoader(Class<T> serviceClass)`：获取扩展类加载器
- `wrapperClass(Class<T> targetClass)`：构建指定类的包裹器实例
- `getFieldAnnotationFirst(Class<?> targetClass, Class<A> annotationClass)`：获取类中成员声明的第一个注解
- `getFieldAnnotations(Class<?> targetClass, Class<A> annotationClass)`：获取类中成员声明的所有注解
- `getFields(Class<?> targetClass, boolean includeSuperClass)`：获取指定类所有的成员对象
- `getMethods(Class<?> targetClass, boolean includeSuperClass)`：获取指定的类所有方法对象

### 3.9 RetryConfig

**主要方法**：
- `custom()`：创建 RetryConfig.Builder 实例
- `getMaxRetries()`：获取最大重试次数
- `getInitialDelayMs()`：获取初始延迟时间
- `getTotalTimeoutMs()`：获取总超时时间
- `getDelayStrategy()`：获取延迟策略
- `getRetryableExceptions()`：获取可重试的异常类型

**Builder 主要方法**：
- `maxRetries(int maxRetries)`：设置最大重试次数
- `fixedDelay(long delayMs)`：设置固定延迟策略
- `exponentialDelay(long initialDelayMs)`：设置指数延迟策略（使用默认最大延迟）
- `exponentialDelay(long initialDelayMs, long maxDelayMs)`：设置指数延迟策略（指定最大延迟）
- `randomDelay(long minDelayMs, long maxDelayMs)`：设置随机延迟策略
- `totalTimeoutMs(long totalTimeoutMs)`：设置总超时时间
- `retryableExceptions(Class<? extends Exception>... exceptions)`：设置可重试的异常类型
- `delayStrategy(IRetryDelayStrategy delayStrategy)`：设置自定义延迟策略
- `build()`：构建 RetryConfig 实例

### 3.10 RetryUtils

**主要方法**：
- `executeWithRetry(Callable<T> task)`：使用默认配置执行重试操作
- `executeWithRetry(Callable<T> task, int maxRetries, long initialDelayMs)`：使用指定重试次数和初始延迟执行重试操作
- `executeWithRetry(Callable<T> task, int maxRetries, long initialDelayMs, Class<? extends Exception>... retryableExceptions)`：使用完整配置执行重试操作
- `executeWithRetry(Callable<T> task, RetryConfig config)`：使用自定义配置执行重试操作

## 4. 技术架构与实现

### 4.1 架构层次

1. **基础层**：提供基础类型转换、工具类等核心功能
2. **功能层**：提供 HttpClient、JSON 处理、序列化等功能
3. **工具层**：提供各种实用工具类

### 4.2 核心组件

- **Lang**：基础类型处理
- **HttpClient**：HTTP 客户端封装
- **JsonWrapper**：JSON 处理封装
- **Serializer**：序列化处理
- **Retry**：重试机制处理
- **Utils**：各种工具类

### 4.3 工作流程

以 BlurObject 为例：
1. 通过 `BlurObject.bind(targetObj)` 绑定目标对象
2. 调用相应的转换方法，如 `toIntValue()`、`toStringValue()` 等
3. 内部通过默认转换器或自定义转换器执行转换操作
4. 返回转换结果

以 RetryUtils 为例：
1. 通过 `RetryConfig.custom()` 创建配置对象
2. 设置重试次数、延迟策略、超时时间等参数
3. 调用 `RetryUtils.executeWithRetry()` 执行重试操作
4. 内部根据配置执行重试逻辑，失败时根据延迟策略等待后重试
5. 返回成功结果或抛出最后一次异常

## 5. 使用指南与典型场景

### 5.1 基础类型转换

**示例：基本数据类型转换**

```java
Object targetObj = "123.4";
//
BlurObject blurObject = BlurObject.bind(targetObj);
blurObject.toIntValue();
blurObject.toDoubleValue();
blurObject.toFloatValue();
blurObject.toStringValue();
// ......
```

**示例：自定义类型转换器的注册与使用**

```java
// 自定义类型转换器，通过注解明确从...到...类型之间的转换
// 此类可以通过 SPI 方式被自动注册
@Converter(from = {java.util.Date.class}, to = java.sql.Date.class)
public class DateConverter implements IConverter<java.sql.Date> {

    @Override
    public java.sql.Date convert(Object target) {
        return new Date(((java.util.Date) target).getTime());
    }
}

// 手动注册
BlurObject.registerConverter(java.util.Date.class, java.sql.Date.class, new DateConverter());
// 执行转换
java.util.Date date = new java.util.Date();
BlurObject.bind(date).toObjectValue(java.sql.Date.class);
```

### 5.2 HttpClient 使用

**示例：发送 GET 请求**

```java
public void sendGetRequest(String url, String charset) throws Exception {
    Header[] headers = {
            new BasicHeader("Accept", "text/html"),
            new BasicHeader("Accept-Encoding", "gzip, deflate, br"),
            new BasicHeader("Accept-Language", "zh-Hans-CN,zh-CN;"),
            new BasicHeader("Cache-Control", "no-cache"),
            new BasicHeader("Pragma", "no-cache"),
            new BasicHeader("Connection", "keep-alive"),
            new BasicHeader("User-Agent", "Mozilla/5.0......")
    };
    try (CloseableHttpClientHelper httpClientHelper = CloseableHttpClientHelper.create();
         IHttpResponse response = httpClientHelper.get(url, headers, charset)) {
        System.out.println("StatusCode: " + response.getStatusCode());
        System.out.println("ContentType: " + response.getContentType());
        System.out.println("ContentLength: " + response.getContentLength());
        System.out.println("Content: " + response.getContent());
    }
}
```

**示例：发送 POST 请求**

```java
public void sendPostRequest(String url, String charset, Map<String, String> requestParams) throws Exception {
    ContentType contentType = ContentType.create(CloseableHttpClientHelper.CONTENT_TYPE_FORM_URL_ENCODED, charset);
    try (CloseableHttpClientHelper httpClientHelper = CloseableHttpClientHelper.create();
         IHttpResponse response = httpClientHelper.post(url, contentType, ParamUtils.buildQueryParamStr(requestParams, true, charset))) {
        // 判断响应状态码是否为 200
        if (response.isSuccess()) {
            System.out.printf("Content: %s%n", response.getContent());
        } else {
            System.out.printf("ReasonPhrase: %s%n", response.getReasonPhrase());
        }
    }
}
```

**示例：文件上传**

```java
public void uploadFile(String url, File distFile) throws Exception {
    try (CloseableHttpClientHelper httpClientHelper = CloseableHttpClientHelper.create();
         IHttpResponse response = httpClientHelper.upload(url, "file", distFile)) {
        // 判断响应状态码是否为 200
        if (response.isSuccess()) {
            // ......
        } else {
            System.out.printf("ReasonPhrase: %s%n", response.getReasonPhrase());
        }
    }
}
```

**示例：文件下载**

```java
public void downloadFile(String url, File distFile) throws Exception {
    try (CloseableHttpClientHelper httpClientHelper = CloseableHttpClientHelper.create()) {
        httpClientHelper.download(url, new IFileHandler() {
            @Override
            public void handle(HttpResponse response, IFileWrapper fileWrapper) throws IOException {
                if (fileWrapper != null) {
                    System.out.println("FileName: " + fileWrapper.getFileName());
                    System.out.println("ContentType: " + fileWrapper.getContentType());
                    System.out.println("ContentLength: " + fileWrapper.getContentLength());
                    // 将被下载文件转移到目标文件
                    fileWrapper.transferTo(distFile);
                } else {
                    System.out.printf("ReasonPhrase: %s%n", response.getStatusLine().getReasonPhrase());
                }
            }
        });
    }
}
```

### 5.3 JSON 处理

**示例：创建 JSON 对象**

```java
// 创建 JsonObject 对象实例并设置 ordered 为有序的
IJsonObjectWrapper jsonObj = JsonWrapper.createJsonObject(true);
jsonObj.put("name", "suninformation");
jsonObj.put("realName", "有理想的鱼");
jsonObj.put("age", 20);
jsonObj.put("gender", (String) null);
jsonObj.put("attrs", JsonWrapper.createJsonObject()
            .put("key1", "value1")
            .put("key2", "value2"));
// 采用格式化输出并保留值为空的属性
System.out.println(jsonObj.toString(true, true));
// 取值：
System.out.println("Name: " + jsonObj.getString("name"));
System.out.println("Age: " + jsonObj.getInt("age"));
IJsonObjectWrapper attrs = jsonObj.getJsonObject("attrs");
System.out.println("Key1: " + attrs.getString("key1"));
```

**示例：对象序列化与反序列化**

```java
public class User {

    private String name;
    private Integer age;
    private String realName;

    // 省略 Get 和 Set 方法
}

User user = new User();
user.setName("suninformation");
user.setAge(20);
user.setRealName("有理想的鱼");

// 序列化
byte[] serializeArr = JsonWrapper.serialize(user, true);
// 反序列化
User newUser = JsonWrapper.deserialize(serializeArr, new TypeReferenceWrapper<User>() {});
System.out.println(newUser);

// 采用 snakeCase 模式输出和反序列化操作
String jsonStr = JsonWrapper.toJsonString(user, false, false, true);
User newUser2 = JsonWrapper.deserialize(jsonStr, true, User.class);
System.out.println(newUser2);
```

### 5.4 序列化操作

**示例：对象序列化与反序列化**

```java
public class SerialDemoBean implements Serializable {

    private String name;
    private String remark;

    // 省略 Get 和 Set 方法
}

// 创建待序列化对象实现
SerialDemoBean demoBean = new SerialDemoBean();
demoBean.setName("YMP");
demoBean.setRemark("A lightweight modular simple and powerful Java framework.");
// 通过对象序列化管理器获取指定的对象序列化接口实例
ISerializer serializer = SerializerManager.getDefaultSerializer();
// 执行对象序列化操作
byte[] bytes = serializer.serialize(demoBean);
// 执行对象反序列化操作
SerialDemoBean deserializeBean = serializer.deserialize(bytes, SerialDemoBean.class);
// 输出对象值
System.out.println(deserializeBean.toString());
```

**示例：自定义对象序列化与反序列化实现**

```java
@Serializer("custom")
public class CustomSerializer implements ISerializer {

    @Override
    public String getContentType() {
        return "application/json";
    }

    @Override
    public byte[] serialize(Object object) throws Exception {
        com.alibaba.fastjson.serializer.JSONSerializer serializer = new com.alibaba.fastjson.serializer.JSONSerializer();
        serializer.config(SerializerFeature.WriteEnumUsingToString, true);
        serializer.write(object);
        return serializer.getWriter().toBytes(StandardCharsets.UTF_8);
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        return JSON.parseObject(new String(bytes, StandardCharsets.UTF_8), clazz);
    }
}

// 使用自定义序列化器
ISerializer serializer = SerializerManager.getSerializer("custom");
byte[] bytes = serializer.serialize(demoBean);
SerialDemoBean deserializeBean = serializer.deserialize(bytes, SerialDemoBean.class);
```

### 5.5 重试机制使用

**示例：最简单的使用方式**

```java
import net.ymate.platform.commons.retry.RetryUtils;

public class RetryDemo {
    public static void main(String[] args) throws Exception {
        String result = RetryUtils.executeWithRetry(() -> {
            // 执行可能失败的操作
            return doSomething();
        });
        System.out.println("Result: " + result);
    }

    private static String doSomething() throws Exception {
        // 模拟网络请求或其他可能失败的操作
        return "success";
    }
}
```

**示例：使用固定延迟策略**

```java
import net.ymate.platform.commons.retry.RetryConfig;
import net.ymate.platform.commons.retry.RetryUtils;

public class RetryDemo {
    public static void main(String[] args) throws Exception {
        RetryConfig config = RetryConfig.custom()
            .maxRetries(3)
            .fixedDelay(500)  // 每次重试固定延迟500毫秒
            .build();

        String result = RetryUtils.executeWithRetry(() -> doSomething(), config);
    }
}
```

**示例：使用指数延迟策略**

```java
import net.ymate.platform.commons.retry.RetryConfig;
import net.ymate.platform.commons.retry.RetryUtils;

public class RetryDemo {
    public static void main(String[] args) throws Exception {
        RetryConfig config = RetryConfig.custom()
            .maxRetries(5)
            .exponentialDelay(1000, 60000)  // 初始延迟1秒，最大延迟1分钟
            .build();

        String result = RetryUtils.executeWithRetry(() -> doSomething(), config);
    }
}
```

**示例：指定可重试的异常类型**

```java
import net.ymate.platform.commons.retry.RetryConfig;
import net.ymate.platform.commons.retry.RetryUtils;
import java.io.IOException;
import java.net.SocketTimeoutException;

public class RetryDemo {
    public static void main(String[] args) throws Exception {
        RetryConfig config = RetryConfig.custom()
            .maxRetries(3)
            .exponentialDelay(1000)
            // 只对网络相关异常进行重试
            .retryableExceptions(IOException.class, SocketTimeoutException.class)
            .build();

        String result = RetryUtils.executeWithRetry(() -> doSomething(), config);
    }
}
```

**示例：HTTP 请求重试**

```java
import net.ymate.platform.commons.retry.RetryConfig;
import net.ymate.platform.commons.retry.RetryUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import java.io.IOException;

public class HttpRetryDemo {

    public static String httpGetWithRetry(String url) throws Exception {
        RetryConfig config = RetryConfig.custom()
            .maxRetries(3)
            .exponentialDelay(1000, 30000)
            .retryableExceptions(IOException.class)
            .build();

        return RetryUtils.executeWithRetry(() -> {
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpGet request = new HttpGet(url);
                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    return EntityUtils.toString(response.getEntity());
                }
            }
        }, config);
    }
}
```

**示例：自定义延迟策略**

```java
import net.ymate.platform.commons.retry.IRetryDelayStrategy;
import net.ymate.platform.commons.retry.RetryConfig;
import net.ymate.platform.commons.retry.RetryUtils;

public class CustomDelayStrategy implements IRetryDelayStrategy {

    @Override
    public long getDelay(int attempt) {
        // 自定义延迟逻辑：线性增长延迟
        return attempt * 1000L;  // 第1次1秒，第2次2秒，第3次3秒...
    }
}

// 使用自定义延迟策略
RetryConfig config = RetryConfig.custom()
    .maxRetries(3)
    .delayStrategy(new CustomDelayStrategy())
    .build();

String result = RetryUtils.executeWithRetry(() -> doSomething(), config);
```

## 6. 配置、部署与开发

### 6.1 依赖配置

在 Maven 项目中添加以下依赖：

```xml
<dependency>
    <groupId>net.ymate.platform</groupId>
    <artifactId>ymate-platform-commons</artifactId>
    <version>2.1.4-dev</version>
</dependency>
```

### 6.2 可选依赖

#### 6.2.1 HttpClient 依赖

使用 HttpClient 相关功能时，需添加以下依赖：

```xml
<dependency>
    <groupId>org.apache.httpcomponents</groupId>
    <artifactId>httpmime</artifactId>
    <version>4.5.14</version>
    <exclusions>
        <!-- YMP 框架已引入更高版本，排除为了避免在产生不必要的问题  -->
        <exclusion>
            <groupId>commons-codec</groupId>
            <artifactId>commons-codec</artifactId>
        </exclusion>
        <exclusion>
            <groupId>commons-logging</groupId>
            <artifactId>commons-logging</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

#### 6.2.2 JSON 解析器依赖

使用 JsonWrapper 相关功能时，需添加以下依赖之一：

**FastJson**：

```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>fastjson</artifactId>
    <version>1.2.83</version>
</dependency>
```

**Gson**：

```xml
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.9.0</version>
</dependency>
```

**Jackson**：

```xml
<dependency>
    <groupId>com.fasterxml.jackson.datatype</groupId>
    <artifactId>jackson-datatype-jdk8</artifactId>
    <version>2.13.4</version>
</dependency>
```

#### 6.2.3 序列化依赖

使用 Hessian 序列化时，需添加以下依赖：

```xml
<dependency>
    <groupId>com.caucho</groupId>
    <artifactId>hessian</artifactId>
    <version>4.0.66</version>
</dependency>
```

使用 FST 序列化时，需添加以下依赖：

```xml
<dependency>
    <groupId>de.ruedigermoeller</groupId>
    <artifactId>fst</artifactId>
    <version>2.48-jdk-6</version>
</dependency>
```

### 6.3 开发建议

1. **选择合适的工具类**：根据具体的开发需求，选择合适的工具类，避免重复造轮子。

2. **注意依赖管理**：使用 HttpClient、JSON 解析器、序列化等功能时，注意添加相应的依赖。

3. **合理使用类型转换**：使用 BlurObject 进行类型转换时，注意处理可能的转换异常。

4. **优化 HTTP 请求**：使用 HttpClient 时，注意设置合理的超时时间，避免请求阻塞。

5. **选择合适的 JSON 解析器**：根据项目需求和性能要求，选择合适的 JSON 解析器。

6. **注意序列化性能**：对于频繁的序列化操作，选择性能较好的序列化实现。

7. **使用 try-with-resources**：使用 CloseableHttpClientHelper 等实现了 AutoCloseable 接口的类时，使用 try-with-resources 语句确保资源正确释放。

8. **合理使用重试机制**：使用重试机制时，根据业务场景选择合适的延迟策略（网络请求推荐指数退避，分布式竞争场景推荐随机延迟），设置合理的重试次数和超时时间，确保操作的幂等性。

## 7. 监控与维护

### 7.1 性能监控

- **HTTP 请求性能**：监控 HTTP 请求的响应时间，识别慢请求。
- **JSON 处理性能**：监控 JSON 序列化和反序列化的性能，识别性能瓶颈。
- **序列化性能**：监控对象序列化和反序列化的性能，选择性能较好的实现。
- **重试机制性能**：监控重试操作的次数和成功率，识别需要优化的重试策略。

### 7.2 常见问题与解决方案

| 问题 | 原因 | 解决方案 |
| ---- | ---- | ------ |
| HTTP 请求超时 | 网络问题或服务器响应慢 | 增加超时时间，优化网络连接，检查服务器状态 |
| JSON 解析错误 | JSON 格式不正确或解析器不支持某些特性 | 检查 JSON 格式，选择合适的 JSON 解析器 |
| 序列化失败 | 对象未实现 Serializable 接口或包含不可序列化的字段 | 确保对象实现 Serializable 接口，处理不可序列化的字段 |
| 类型转换错误 | 源类型与目标类型不兼容 | 检查类型兼容性，使用合适的转换器 |
| 文件操作异常 | 文件不存在或权限不足 | 检查文件路径和权限，处理异常情况 |
| 重试操作失败 | 重试次数不足或延迟策略不合适 | 根据业务场景调整重试次数和延迟策略，设置合理的超时时间 |
| 重试导致重复操作 | 操作不具备幂等性 | 确保重试操作的幂等性，或添加额外的机制保证数据一致性 |

## 8. 总结与亮点回顾

Commons 通用工具包模块是 YMP 框架中一个功能丰富、使用方便的工具类库集合，它的主要亮点包括：

- **全面的工具类**：提供了大量实用的工具类，涵盖了开发中的各种常见操作，大大简化了开发工作。

- **HttpClient 封装**：基于 Apache HttpComponents 组件封装的 HttpClient 请求与处理工具，简化了 HTTP 请求的发送和处理。

- **JSON 包装器**：为不同的第三方 JSON 解析器提供统一的 API 接口，使开发者可以灵活切换 JSON 解析器而不影响业务代码。

- **类型转换**：提供了任意类型对象之间的转换功能，简化了类型转换的代码。

- **序列化支持**：支持多种序列化方式，满足不同场景的需求。

- **重试机制**：提供了一套完整的重试策略实现，支持自定义重试次数、延迟策略、超时控制以及异常类型过滤等功能，适用于网络请求、服务调用等可能失败的操作。

- **丰富的工具类**：包括日期时间、数学、经纬度、字符串加解密、运行时环境、网络、线程操作等，覆盖了开发中的各种常见需求。

通过 Commons 通用工具包模块，开发者可以更加便捷地处理各种常见的开发任务，提高开发效率，减少重复代码，使代码更加简洁、易读、易维护。
