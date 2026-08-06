---
name: ymp-commons
description: YMP通用工具包，提供BlurObject类型转换、JsonWrapper JSON、HttpClient请求、重试机制、序列化等15+工具类
version: 2.1.4-dev
author: YMP Team
category: utilities
tags:
  - java
  - utilities
  - http-client
  - json
  - serialization
  - retry
  - type-conversion
trigger: 当需要类型转换、HTTP请求、JSON构建解析、重试机制、序列化反序列化、日期/UUID/文件/网络等工具时触发
tools:
  - http-client
  - json-processing
  - serialization
  - type-conversion
  - retry-mechanism
examples:
  - BlurObject绑对象做多类型安全转换（toInt/toLong/toMap/toList）
  - JsonWrapper创建JSONObject/JSONArray、对象JSON序列化反序列化
  - CloseableHttpClientHelper发送GET/POST请求、上传下载文件
  - RetryUtils配置重试次数/延迟策略/可重试异常执行Callable
  - SerializerManager获取Default/JSON/FST/Hessian序列化器
---

# Commons 通用工具包技能包

> AI读取指引：任何需要工具类的场景优先使用本模块；几乎其他所有模块都依赖commons的基础能力。无状态工具类直接静态调用，无需初始化。

---

## 0. 快速索引

- Maven artifactId：`ymate-platform-commons`
- 静态入口类（全限定名）：
  - 类型转换：`net.ymate.platform.commons.lang.BlurObject`
  - JSON：`net.ymate.platform.commons.json.JsonWrapper`
  - 重试：`net.ymate.platform.commons.retry.RetryUtils`
  - 序列化：`net.ymate.platform.commons.serialize.SerializerManager`
  - UUID：`net.ymate.platform.commons.util.UUIDUtils`
- 必备注解：无（可选 `@Converter` / `@Serializer` 自定义扩展时用）
- 3行最简调用示例：

```java
int i = BlurObject.bind("123").toIntValue();
IJsonObjectWrapper j = JsonWrapper.createJsonObject().put("k","v");
String u = UUIDUtils.UUID();
```

## 1. 模块摘要

YMP框架积累的通用工具类库集合，覆盖日常开发80%以上工具场景。核心能力：
- **Lang基础类型**：BlurObject任意类型转换、PairObject键值对、TreeObject无限层级树
- **JSON统一包装**：FastJson/Gson/Jackson自由切换，统一IJsonObjectWrapper/IJsonArrayWrapper API
- **HTTP客户端**：CloseableHttpClientHelper + CloseableHttpRequestBuilder，支持上传下载、SSL证书、超时
- **重试机制**：RetryUtils + RetryConfig，固定/指数/随机延迟策略，异常过滤+总超时
- **序列化管理**：SerializerManager，6种内置序列化器（Default/JSON/FST/Hessian/Kryo/Protobuf）+ @Serializer自定义SPI
- **工具类**：DateTimeHelper/DateTimeUtils、UUIDUtils、CodecUtils(AES/PBE/RSA)、ClassUtils、FileUtils、NetworkUtils、StopWatcher、QRCodeHelper、MarkdownBuilder等

## 2. 核心注解/类 速查表（必须带全限定名）

### 注解（如果有）

| 注解 | 全限定名 | 作用 | 核心参数（只列2-5个） |
|---|---|---|---|
| `@Converter` | `net.ymate.platform.commons.annotation.Converter` | 标记BlurObject自定义类型转换器 | `from`(源类型Class[])、`to`(目标类型Class) |
| `@Serializer` | `net.ymate.platform.commons.serialize.annotation.Serializer`(假设路径，源码在serialize包) | 标记自定义序列化器，自动扫描注册 | `value`(序列化器名称，不区分大小写) |

### 常用工具类（commons模块重点，15-25个）

| 类名 | 全限定名 | 核心用途 | 最常用的2-3个方法签名 |
|---|---|---|---|
| **BlurObject** | `net.ymate.platform.commons.lang.BlurObject` | 任意对象安全类型转换 | `bind(Object target)` / `toIntValue()` / `toObjectValue(Class<T> clazz)` / `toListValue()` / `toMapValue()` |
| **PairObject** | `net.ymate.platform.commons.lang.PairObject<K,V>` | 通用键值对容器，可序列化 | `bind(K key, V value)` / `getKey()` / `getValue()` / `isEmpty()` |
| **TreeObject** | `net.ymate.platform.commons.lang.TreeObject` | 无限层级树结构(值/映射/数组三模式) | `put(String key, Object value)` / `add(Object value)` / `get(String key)` / `toJson()` / `fromJson(String)` |
| **JsonWrapper** | `net.ymate.platform.commons.json.JsonWrapper` | JSON统一入口，屏蔽FastJson/Gson/Jackson差异 | `createJsonObject(boolean ordered)` / `createJsonArray(Object[])` / `fromJson(String)` / `toJsonString(Object, boolean pretty, boolean keepNulls, boolean snakeCase)` / `serialize(Object)` / `deserialize(byte[], TypeReferenceWrapper<T>)` |
| **IJsonObjectWrapper** | `net.ymate.platform.commons.json.IJsonObjectWrapper` | JSON对象包装器 | `put(String key, Object value)` / `getString(String key)` / `getInt(String key)` / `getJsonObject(String key)` / `toString(boolean pretty, boolean keepNulls)` |
| **IJsonArrayWrapper** | `net.ymate.platform.commons.json.IJsonArrayWrapper` | JSON数组包装器 | `add(Object value)` / `size()` / `getString(int index)` / `getJsonObject(int index)` / `toList()` |
| **TypeReferenceWrapper** | `net.ymate.platform.commons.json.TypeReferenceWrapper<T>` | 泛型反序列化类型引用（解决类型擦除） | 继承匿名类使用：`new TypeReferenceWrapper<List<User>>() {}` |
| **CloseableHttpClientHelper** | `net.ymate.platform.commons.http.CloseableHttpClientHelper` | HTTP客户端封装(基于Apache HttpComponents)，支持try-with-resources | `create()` / `get(String url, Header[] headers, String charset)` / `post(String url, ContentType contentType, String content)` / `upload(String url, String fieldName, File file)` / `download(String url, IFileHandler)` / `newRequestBuilder(String url)` |
| **CloseableHttpRequestBuilder** | `net.ymate.platform.commons.http.CloseableHttpRequestBuilder` | 请求构建器，支持GET/POST/PUT/DELETE等全部方法 | `create(String url)` / `addParams(Map<String,String>)` / `addContent(String field, File)` / `charset(String)` / `connectionTimeout(int)` / `build()` / `post()` |
| **RetryUtils** | `net.ymate.platform.commons.retry.RetryUtils` | 重试工具类，静态执行入口 | `executeWithRetry(Callable<T>)` / `executeWithRetry(Callable<T>, int maxRetries, long initialDelayMs)` / `executeWithRetry(Callable<T>, RetryConfig)` |
| **RetryConfig** | `net.ymate.platform.commons.retry.RetryConfig` | 重试配置，Builder模式 | `custom()` / `maxRetries(int)` / `fixedDelay(long)` / `exponentialDelay(long initial, long max)` / `randomDelay(long min, long max)` / `retryableExceptions(Class<?>...)` / `totalTimeoutMs(long)` / `build()` |
| **SerializerManager** | `net.ymate.platform.commons.serialize.SerializerManager` | 序列化器管理器 | `getDefaultSerializer()` / `getJsonSerializer()` / `getFstSerializer()` / `getHessianSerializer()` / `getSerializer(String name)` / `registerSerializer(Class<?>)` |
| **ISerializer** | `net.ymate.platform.commons.serialize.ISerializer` | 序列化器接口 | `serialize(Object object)` -> byte[] / `deserialize(byte[], Class<T>)` / `getContentType()` |
| **DateTimeHelper** | `net.ymate.platform.commons.DateTimeHelper` | 链式日期时间助手（JDK8+） | `bind(Date/long/LocalDateTime/String)` / `now()` / `toDayStart()` / `toMonthEnd()` / `daysAdd(int)` / `yearsAdd(int)` / 格式化输出 |
| **UUIDUtils** | `net.ymate.platform.commons.util.UUIDUtils` | UUID与随机数生成 | `UUID()`(32位去横线) / `generateTimeBasedUUID()` / `generateRandomUUID()` / `randomInt(int min, int max)` / `randomStr(int length, boolean digitsOnly)` |
| **CodecUtils** | `net.ymate.platform.commons.util.CodecUtils` | AES/PBE/RSA加解密助手 | `AES.encrypt(content, key)` / `AES.decrypt(content, key)` / `RSA.initRSAKey()` / `RSA.sign(content, privKey)` / `RSA.verify(bytes, pubKey, sign)` |
| **ClassUtils** | `net.ymate.platform.commons.util.ClassUtils` | 类反射/加载/SPI扩展工具 | `getExtensionLoader(Class<T>)` / `wrapperClass(Class<T>)` -> BeanWrapper / `getFields(Class<?>, boolean includeSuper)` / `isSubclassOf(Class, Class)` / `propertyNameToFieldName(String)` / `fieldNameToPropertyName(String, int)` |
| **FileUtils** | `net.ymate.platform.commons.util.FileUtils` | 文件读写/复制/删除/目录操作 | 常见静态方法：读文件、写文件、复制文件、删除目录、获取扩展名等 |
| **NetworkUtils** | `net.ymate.platform.commons.util.NetworkUtils` | 网络相关：本机IP、主机名、端口检测 | 静态方法：获取本机IP列表、检测端口占用、获取主机名 |
| **StopWatcher** | `net.ymate.platform.commons.StopWatcher<V>` | 代码块耗时统计（包装Guava Stopwatch） | `watch(Runnable)` / `watch(Callable<V>)` / `getValue()` / `getStopWatch()` -> 取内部elapsed |
| **MarkdownBuilder** | `net.ymate.platform.commons.markdown.MarkdownBuilder` | Markdown文档链式构建 | `create()` / `title(String, int level)` / `text(String, Text.Style)` / `quote(...)` / `link(text, url)` / `code(code, lang)` / `table(...)` / `append(IMarkdown)` |
| **QRCodeHelper** | `net.ymate.platform.commons.QRCodeHelper` | 二维码生成/解析 | 静态方法：生成二维码图片、解析二维码图像内容 |
| **ConsoleTableBuilder** | `net.ymate.platform.commons.ConsoleTableBuilder` | 控制台表格输出对齐 | create()链式添加表头/行数据，格式化打印 |
| **MathCalcHelper** | `net.ymate.platform.commons.MathCalcHelper` | 高精度浮点数计算（封装BigDecimal，避免浮点误差） | 链式加减乘除、四舍五入、比较 |

## 3. 核心API速查（≤8条最常用调用）

1. **类型转换**：`BlurObject.bind("123.4").toIntValue()` → 123；`BlurObject.bind(map).toMapValue()`
2. **构建JSON**：`JsonWrapper.createJsonObject(true).put("name","xx").put("age",20).toString(true,false)`
3. **解析JSON**：`User u = JsonWrapper.deserialize(jsonStr, true, User.class)` （第2参数snakeCase）
4. **泛型JSON反序列化**：`List<User> list = JsonWrapper.deserialize(jsonArrStr, new TypeReferenceWrapper<List<User>>() {})`
5. **HTTP GET**：`try(CloseableHttpClientHelper h = CloseableHttpClientHelper.create(); IHttpResponse r = h.get(url, null, "UTF-8")) { r.getContent(); }`
6. **重试执行**：`RetryUtils.executeWithRetry(task, RetryConfig.custom().maxRetries(3).exponentialDelay(1000,60000).retryableExceptions(IOException.class).build())`
7. **序列化**：`byte[] b = SerializerManager.getDefaultSerializer().serialize(obj);` 反序列化：`SerializerManager.getJsonSerializer().deserialize(b, T.class)`
8. **UUID/随机**：`UUIDUtils.UUID()` / `UUIDUtils.randomInt(1,100)` / `UUIDUtils.randomStr(6,false)`

## 4. 标准代码模板（最少可运行）

### 模板1：BlurObject多类型转换

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
package com.example.commons;

import net.ymate.platform.commons.lang.BlurObject;
import net.ymate.platform.commons.lang.PairObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BlurObject类型转换和PairObject使用示例。
 *
 * @author AI Generated
 * @since 2.1.4-dev
 */
public class BlurObjectDemo {

    /**
     * 演示BlurObject的常用类型转换和PairObject键值对。
     *
     * @param args 命令行参数
     * @since 2.1.4-dev
     */
    public static void main(String[] args) {
        Object raw = "123.45";
        BlurObject bo = BlurObject.bind(raw);
        int i = bo.toIntValue();
        long l = bo.toLongValue();
        double d = bo.toDoubleValue();
        String s = bo.toStringValue();
        System.out.printf("i=%d, l=%d, d=%f, s=%s%n", i, l, d, s);

        boolean bool = BlurObject.bind("true").toBooleanValue();
        System.out.println("bool=" + bool);

        List<String> list = Arrays.asList("a", "b", "c");
        List<?> converted = BlurObject.bind(list).toListValue();
        System.out.println("list=" + converted);

        Map<String, Object> map = new HashMap<>();
        map.put("k1", "v1");
        map.put("k2", 42);
        Map<?, ?> mapResult = BlurObject.bind(map).toMapValue();
        System.out.println("map=" + mapResult);

        PairObject<String, Integer> pair = PairObject.bind("age", 18);
        System.out.printf("pair: key=%s, value=%d, empty=%b%n",
                pair.getKey(), pair.getValue(), pair.isEmpty());
    }
}
```

### 模板2：JsonWrapper构建和解析JSON

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
package com.example.commons;

import net.ymate.platform.commons.json.IJsonArrayWrapper;
import net.ymate.platform.commons.json.IJsonObjectWrapper;
import net.ymate.platform.commons.json.JsonWrapper;
import net.ymate.platform.commons.json.TypeReferenceWrapper;

import java.util.Arrays;
import java.util.List;

/**
 * JsonWrapper构建/解析JSON、对象序列化反序列化示例。
 *
 * @author AI Generated
 * @since 2.1.4-dev
 */
public class JsonWrapperDemo {

    public static class User {
        private String name;
        private Integer age;

        public User() {
        }

        public User(String name, Integer age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        @Override
        public String toString() {
            return "User{name='" + name + "', age=" + age + '}';
        }
    }

    /**
     * 演示JsonObject构建、数组、对象<->JSON互转、泛型反序列化。
     *
     * @param args 命令行参数
     * @since 2.1.4-dev
     */
    public static void main(String[] args) {
        IJsonObjectWrapper obj = JsonWrapper.createJsonObject(true)
                .put("name", "suninformation")
                .put("age", 20)
                .put("tags", JsonWrapper.createJsonArray(new Object[]{"java", "ymp"}));
        String prettyJson = obj.toString(true, false);
        System.out.println("构建的JSON:\n" + prettyJson);

        IJsonObjectWrapper parsed = JsonWrapper.createJsonObject().put("nested", obj);
        System.out.println("嵌套取值: " + parsed.getJsonObject("nested").getString("name"));

        User user = new User("demo_user", 25);
        String compact = JsonWrapper.toJsonString(user, false, false, false);
        String snakeCasePretty = JsonWrapper.toJsonString(user, true, false, true);
        System.out.println("紧凑: " + compact);
        System.out.println("蛇形命名格式化:\n" + snakeCasePretty);

        User back = JsonWrapper.deserialize(compact, User.class);
        System.out.println("反序列化: " + back);

        String arrayJson = JsonWrapper.toJsonString(Arrays.asList(
                new User("u1", 20), new User("u2", 22)
        ), false, false, false);
        List<User> list = JsonWrapper.deserialize(arrayJson, new TypeReferenceWrapper<List<User>>() {});
        System.out.println("泛型List反序列化: " + list);
    }
}
```

### 模板3：CloseableHttpClientHelper发起GET/POST

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
package com.example.commons;

import net.ymate.platform.commons.http.CloseableHttpClientHelper;
import net.ymate.platform.commons.http.IHttpResponse;
import net.ymate.platform.commons.util.ParamUtils;
import org.apache.http.entity.ContentType;

import java.util.HashMap;
import java.util.Map;

/**
 * CloseableHttpClientHelper GET/POST请求示例。
 *
 * @author AI Generated
 * @since 2.1.4-dev
 */
public class HttpClientDemo {

    private static final String URL_GET = "https://httpbin.org/get";
    private static final String URL_POST = "https://httpbin.org/post";
    private static final String CHARSET = "UTF-8";

    /**
     * 发送GET请求。
     *
     * @param url 请求URL
     * @param params 查询参数Map
     * @return 响应内容字符串
     * @throws Exception IO/解析异常
     * @since 2.1.4-dev
     */
    public static String doGet(String url, Map<String, String> params) throws Exception {
        try (CloseableHttpClientHelper http = CloseableHttpClientHelper.create();
             IHttpResponse resp = http.get(url, params, null, CHARSET)) {
            System.out.println("StatusCode=" + resp.getStatusCode());
            System.out.println("ContentType=" + resp.getContentType());
            return resp.getContent();
        }
    }

    /**
     * 发送application/x-www-form-urlencoded POST请求。
     *
     * @param url 请求URL
     * @param params 表单参数Map
     * @return 响应内容字符串
     * @throws Exception IO/解析异常
     * @since 2.1.4-dev
     */
    public static String doPostForm(String url, Map<String, String> params) throws Exception {
        ContentType ct = ContentType.create(CloseableHttpClientHelper.CONTENT_TYPE_FORM_URL_ENCODED, CHARSET);
        String body = ParamUtils.buildQueryParamStr(params, true, CHARSET);
        try (CloseableHttpClientHelper http = CloseableHttpClientHelper.create();
             IHttpResponse resp = http.post(url, ct, body)) {
            if (resp.isSuccess()) {
                return resp.getContent();
            }
            throw new RuntimeException("HTTP失败: " + resp.getReasonPhrase());
        }
    }

    /**
     * 入口：演示GET和POST。
     *
     * @param args CLI参数
     * @throws Exception 请求异常
     * @since 2.1.4-dev
     */
    public static void main(String[] args) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("foo", "123");
        params.put("bar", "ymp");

        System.out.println("--- GET ---");
        String r1 = doGet(URL_GET, params);
        System.out.println(r1.length() > 300 ? r1.substring(0, 300) + "..." : r1);

        System.out.println("\n--- POST ---");
        String r2 = doPostForm(URL_POST, params);
        System.out.println(r2.length() > 300 ? r2.substring(0, 300) + "..." : r2);
    }
}
```

### 模板4：RetryUtils重试调用

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
package com.example.commons;

import net.ymate.platform.commons.retry.RetryConfig;
import net.ymate.platform.commons.retry.RetryUtils;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * RetryUtils重试策略演示：指数延迟、可重试异常过滤、总超时。
 *
 * @author AI Generated
 * @since 2.1.4-dev
 */
public class RetryDemo {

    /**
     * 模拟一个会间歇性失败的远程服务调用。
     *
     * @param counter 计数器引用
     * @return 成功字符串
     * @throws IOException 第1、2次调用抛出，模拟网络抖动
     * @since 2.1.4-dev
     */
    private static String flakyRemoteCall(AtomicInteger counter) throws IOException {
        int attempt = counter.incrementAndGet();
        System.out.printf("第%d次尝试...%n", attempt);
        if (attempt < 3) {
            throw new IOException("模拟连接超时(attempt=" + attempt + ")");
        }
        return "SUCCESS-data-" + System.currentTimeMillis();
    }

    /**
     * 演示三种重试方式：默认配置、简化参数、完整RetryConfig。
     *
     * @param args CLI参数
     * @throws Exception 重试耗尽仍失败时向上抛出
     * @since 2.1.4-dev
     */
    public static void main(String[] args) throws Exception {
        System.out.println("=== 方式一：默认配置(3次重试+指数1s) ===");
        try {
            AtomicInteger c1 = new AtomicInteger(0);
            String r = RetryUtils.executeWithRetry(() -> flakyRemoteCall(c1));
            System.out.println("结果: " + r);
        } catch (Exception e) {
            System.out.println("耗尽: " + e.getMessage());
        }

        System.out.println("\n=== 方式二：简化参数(5次重试，2s初始延迟) ===");
        AtomicInteger c2 = new AtomicInteger(0);
        String r2 = RetryUtils.executeWithRetry(() -> flakyRemoteCall(c2), 5, 2000L);
        System.out.println("结果: " + r2);

        System.out.println("\n=== 方式三：完整配置(指数退避+异常过滤+总超时) ===");
        RetryConfig config = RetryConfig.custom()
                .maxRetries(5)
                .exponentialDelay(500L, 10_000L)
                .retryableExceptions(IOException.class, java.net.SocketTimeoutException.class)
                .totalTimeoutMs(60_000L)
                .build();
        AtomicInteger c3 = new AtomicInteger(0);
        String r3 = RetryUtils.executeWithRetry(() -> flakyRemoteCall(c3), config);
        System.out.println("结果: " + r3);
    }
}
```

### 模板5：SerializerManager序列化/反序列化

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
package com.example.commons;

import net.ymate.platform.commons.serialize.ISerializer;
import net.ymate.platform.commons.serialize.SerializerManager;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

/**
 * SerializerManager多序列化器演示：Default(Java原生)、JSON、FST/Hessian可选。
 *
 * @author AI Generated
 * @since 2.1.4-dev
 */
public class SerializerDemo {

    public static class UserBean implements Serializable {
        private static final long serialVersionUID = 1L;
        private String name;
        private String remark;

        public UserBean() {
        }

        public UserBean(String name, String remark) {
            this.name = name;
            this.remark = remark;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        @Override
        public String toString() {
            return "UserBean{name='" + name + "', remark='" + remark + "'}";
        }
    }

    /**
     * 执行指定序列化器的round-trip并打印bytes长度与结果。
     *
     * @param serializer 序列化器实例，null则跳过（表示可选依赖未加载）
     * @param name 打印标签
     * @param bean 目标对象
     * @param <T> bean类型
     * @throws Exception 序列化或IO异常
     * @since 2.1.4-dev
     */
    private static <T extends Serializable> void roundTrip(ISerializer serializer, String name, T bean) throws Exception {
        if (serializer == null) {
            System.out.printf("[%s] 不可用（需加可选依赖）%n", name);
            return;
        }
        byte[] bytes = serializer.serialize(bean);
        @SuppressWarnings("unchecked")
        T back = (T) serializer.deserialize(bytes, bean.getClass());
        System.out.printf("[%s] contentType=%-45s bytes=%-6d -> %s%n",
                name, serializer.getContentType(),
                bytes.length, back);
        if ("JSON".equals(name)) {
            System.out.println("  JSON可视内容: " + new String(bytes, StandardCharsets.UTF_8));
        }
    }

    /**
     * 入口：Default/JSON/FST/Hessian/Kryo/Protobuf序列化对比。
     *
     * @param args CLI参数
     * @throws Exception 序列化异常
     * @since 2.1.4-dev
     */
    public static void main(String[] args) throws Exception {
        UserBean demo = new UserBean("YMP", "A lightweight modular simple and powerful Java framework.");

        roundTrip(SerializerManager.getDefaultSerializer(), "Default(Java原生)", demo);
        roundTrip(SerializerManager.getJsonSerializer(), "JSON", demo);
        roundTrip(SerializerManager.getFstSerializer(), "FST(高性能)", demo);
        roundTrip(SerializerManager.getHessianSerializer(), "Hessian(二进制RPC)", demo);
        roundTrip(SerializerManager.getKryoSerializer(), "Kryo(极致性能)", demo);
        roundTrip(SerializerManager.getProtobufSerializer(), "Protobuf(跨语言)", demo);

        System.out.println("\n已注册序列化器名称集: " + SerializerManager.getRegisteredNames());
    }
}
```

## 5. 配置速查

### 5.1 配置文件常用项（≤12条）：key | 默认值 | 说明

| key | 默认值 | 说明 |
|---|---|---|
| JVM参数 `-Dymp.jsonAdapterClass` | 自动探测 | 指定JSON适配器：FastJsonAdapter/GsonAdapter/JacksonAdapter全限定名 |
| SPI `META-INF/services/net.ymate.platform.commons.json.IJsonAdapterFactory` | - | 自定义JSON适配器工厂（优先级高于JVM参数） |
| SPI `META-INF/services/internal/` | - | ClassUtils扩展加载器内部默认配置路径（优先级低于services/） |
| SPI `META-INF/services/net.ymate.platform.commons.lang.IConverter` | - | BlurObject自定义转换器自动注册 |
| SPI `META-INF/services/net.ymate.platform.commons.serialize.ISerializer` | - | SerializerManager自定义序列化器自动注册 |
| FST可选依赖 | 未引入 | SerializerManager.getFstSerializer()返回null，需加de.ruedigermoeller:fst |
| Hessian可选依赖 | 未引入 | 需加com.caucho:hessian，否则getHessianSerializer()为null |
| Kryo可选依赖 | 未引入 | 需加com.esotericsoftware:kryo |
| Protobuf可选依赖 | 未引入 | 需加com.google.protobuf:protobuf-java，仅序列化Message/MessageLite对象 |
| HttpClient可选依赖 | 未引入 | 使用CloseableHttpClientHelper必须加org.apache.httpcomponents:httpmime并排除commons-codec/logging旧版本 |

### 5.2 注解配置核心参数

**@Converter：**
- `from`：Class<?>[]，源类型列表（如Date.class, Long.class）
- `to`：Class<?>，目标类型（如java.sql.Date.class）

**@Serializer：**
- `value`：String，序列化器名称（不区分大小写，获取时getSerializer(name)用）

**RetryConfig.Builder主要链式参数：**
- `maxRetries(int)` / `fixedDelay(long ms)` / `exponentialDelay(long initial, long max)` / `randomDelay(long min, long max)`
- `retryableExceptions(Class<? extends Exception>...)` 空=所有异常都重试
- `totalTimeoutMs(long)` 总超时，null=不限制

## 6. 常见坑点排查（3-6条表格）：现象 | 原因 | 解决

| 现象 | 原因 | 解决 |
|---|---|---|
| JsonWrapper反序列化复杂泛型拿到的是JsonObject而非实际Bean | 未用TypeReferenceWrapper导致泛型擦除 | 必须写 `new TypeReferenceWrapper<List<User>>(){}` 匿名类继承形式 |
| toJsonString输出字段null被剔除/下划线和驼峰不一致 | keepNulls参数=false默认剔除；snakeCase参数未传 | `toJsonString(obj, true, true, true)` 后两个参数：keepNulls=true保留null，snakeCase=true驼峰转下划线 |
| SerializerManager.getXxxSerializer()返回null | 对应的可选依赖（FST/Hessian/Kryo/Protobuf）未加入pom | 加对应依赖；调用前判断是否为null做降级（fallback到Default/JSON） |
| BlurObject.bind(date).toLongValue()为0或转换异常 | 传入Date/Calendar/LocalDateTime走特殊处理，但要确保非null | 先判空；必要时自定义@Converter转换器SPI注册 |
| RetryUtils重试逻辑不生效 / 所有异常都重试 / 直接抛出 | retryableExceptions未设置或设置类型不匹配；非IOException被业务异常直接抛出 | 精确枚举可重试异常（仅网络/超时/临时故障类）；业务参数校验异常不可重试 |
| HTTP调用偶发连接泄漏/资源未释放 | 未使用try-with-resources关闭CloseableHttpClientHelper / IHttpResponse | 两个都实现Closeable，必须嵌套在 `try(...create(); ...response = get/post...)` 语法中自动关闭 |
| JSON库切换失败（仍走FastJson/报ClassNotFound） | JVM参数ymp.jsonAdapterClass全限定名写错 / SPI文件格式错误 / 对应JSON库未引入 | 检查适配器类名拼写；确认pom有FastJson/Gson/jackson-datatype-jdk8其一；SPI文件一行一个全限定名 |
