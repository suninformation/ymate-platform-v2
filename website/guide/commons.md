---
sidebar_position: 11
slug: commons
---

# 通用工具包（Commons）

常用的工具类库封装，是在开发 YMP 框架过程中积累下来的一些非常实用的辅助工具，其中主要涉及 HttpClient 请求包装器、JSON 包装器、文件及资源管理、数据导入与导出、视频图片处理、二维码、序列化、类、日期时间、数学、经纬度、字符串加解密、运行时环境、网络、线程操作等。



## Maven包依赖

```xml
<dependency>
    <groupId>net.ymate.platform</groupId>
    <artifactId>ymate-platform-commons</artifactId>
    <version>2.1.1</version>
</dependency>
```



## 基础类型（Lang）

提供了针对任意对象之间的类型转换、组合和无限层级树型结构的支持。



### 模糊对象（BlurObject）

用于任意类型对象之间的转换，基本涵盖日常使用的数据类型，通过 `IConverter` 接口实现自定义转换器并支持手动或 `SPI` 方式注册。



**示例：** 基本数据类型转换

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



**示例：** 自定义类型转换器的注册与使用

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



### 结对对象（PairObject）

用于将任意两种类型的对象以 <K, V> 的形式组合在一起。



**示例：**

```java
String name = "suninformation";
int age = 18;
PairObject<String, Integer> pairObject = PairObject.bind(name, age);
pairObject.getKey();   // suninformation
pairObject.getValue(); // 18
//
List<String> key = new ArrayList<>();
// ......
Map<String, String> value = new HashMap<>();
// ......
PairObject<List<String>, Map<String, String>> pairObject2 = new PairObject<>(key, value);
pairObject2.getKey();
pairObject2.getValue();
```



### 树型对象（TreeObject）

使用级联方式存储各种数据类型，不限层级深度。



**示例：** 综合展示 TreeObject 类的使用方法

```java
public class TreeObjectTest {
    public static void main(String[] args) {
        TreeObject treeObject = new TreeObject()
                .put("id", UUIDUtils.UUID())
                .put("category", new Byte[]{1, 2, 3, 4})
                .put("create_time", System.currentTimeMillis(), true)
                .put("is_locked", true)
                .put("detail", new TreeObject()
                        .put("real_name", "汉字将被混淆", true)
                        .put("age", 32));
        // 创建集合
        TreeObject list = new TreeObject()
                .add("item1")
                .add("item2");
        // 创建映射
        TreeObject map = new TreeObject()
                .put("key1", "value1")
                .put("key2", "value2");
        // 组合
        TreeObject group = new TreeObject()
                .put("ids", list)
                .put("maps", map);
        treeObject.put("group", group);
        // 操作集合
        TreeObject ids = group.get("ids");
        if (ids.isList()) {
            System.out.println("ids: " + ids.getList());
        }
        // 操作映射
        TreeObject maps = group.get("maps");
        if (maps.isMap()) {
            System.out.println("maps: " + maps.getMap());
        }
        // 提取被混淆的汉字内容
        System.out.println("real_name: " + treeObject.get("detail").getMixString("real_name"));
        // 通过TreeObject对象转换为JSON字符串输出
        String jsonStr = treeObject.toJson().toString();
        // 通过JSON字符串转换为TreeObject对象
        TreeObject newTreeObject = TreeObject.fromJson(jsonStr);
        // 格式化输出JSON内容
        System.out.println("JSON: " + newTreeObject.toJson().toString(true, true));
    }
}
```

**执行结果：**

```shell
ids: [item1, item2]
maps: {key1=value1, key2=value2}
real_name: 汉字将被混淆
JSON: {
	"_c":9,
	"_v":{
		"is_locked":{
			"_c":6,
			"_v":true
		},
		"create_time":{
			"_c":5,
			"_v":1634100753548
		},
		"id":{
			"_c":3,
			"_v":"4e6b780192da4f04af9b6b826ea7ead6"
		},
		"detail":{
			"_c":9,
			"_v":{
				"real_name":{
					"_c":2,
					"_v":"5rGJ5a2X5bCG6KKr5re35reG"
				},
				"age":{
					"_c":1,
					"_v":32
				}
			}
		},
		"category":{
			"_c":14,
			"_v":"AQIDBA=="
		},
		"group":{
			"_c":9,
			"_v":{
				"maps":{
					"_c":9,
					"_v":{
						"key1":{
							"_c":3,
							"_v":"value1"
						},
						"key2":{
							"_c":3,
							"_v":"value2"
						}
					}
				},
				"ids":{
					"_c":10,
					"_v":[
						{
							"_c":3,
							"_v":"item1"
						},
						{
							"_c":3,
							"_v":"item2"
						}
					]
				}
			}
		}
	}
}
```



## HttpClient

基于 Apache HttpComponents 组件封装的 HttpClient 请求与处理工具，使用时需在工程中引入以下依赖包：

```xml
<dependency>
    <groupId>org.apache.httpcomponents</groupId>
    <artifactId>httpmime</artifactId>
    <version>4.5.13</version>
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



### HttpClientHelper

此类主要应用于早期 YMP 框架版本及扩展模块中，支持自定义安全连接方式，支持 GET、POST 请求方法，简化文件上传与下载的处理逻辑等。



**示例：** 通过 `GET` 方式发送请求

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
    IHttpResponse httpResponse = HttpClientHelper.create().get(url, headers, charset);
    if (httpResponse != null) {
        System.out.println("StatusCode: " + httpResponse.getStatusCode());
        System.out.println("ContentType: " + httpResponse.getContentType());
        System.out.println("ContentLength: " + httpResponse.getContentLength());
        System.out.println("Content: " + httpResponse.getContent());
        System.out.println("Headers: " + httpResponse.getHeaders());
        System.out.println("Locale: " + httpResponse.getLocale());
        System.out.println("ReasonPhrase: " + httpResponse.getReasonPhrase());
    }
}
```



**示例：** 通过 `POST` 方式发送 `application/x-www-form-urlencoded` 请求

```java
public void sendPostRequest(String url, String charset, Map<String, String> requestParams) throws Exception {
    ContentType contentType = ContentType.create(HttpClientHelper.CONTENT_TYPE_FORM_URL_ENCODED, charset);
    IHttpResponse httpResponse = HttpClientHelper.create()
        .post(url, contentType, ParamUtils.buildQueryParamStr(requestParams, true, charset));
    if (httpResponse != null) {
        // 判断响应状态码是否为 200
        if (httpResponse.getStatusCode() == HttpClientHelper.HTTP_STATUS_CODE_SUCCESS) {
            // ......
        } else {
            System.out.printf("ResponseBody: %s%n", httpResponse);
        }
    }
}
```



**示例：** 文件上传

```java
public void uploadFile(String url, File distFile) throws Exception {
    IHttpResponse httpResponse = HttpClientHelper.create().upload(url, "file", distFile);
    if (httpResponse != null) {
        if (httpResponse.getStatusCode() == HttpClientHelper.HTTP_STATUS_CODE_SUCCESS) {
            // ......
        } else {
            System.out.printf("ResponseBody: %s%n", httpResponse);
        }
    }
}
```



**示例：** 文件下载

```java
public void downloadFile(String url, File distFile) throws Exception {
    HttpClientHelper.create().download(url, new IFileHandler() {
        @Override
        public void handle(HttpResponse response, IFileWrapper fileWrapper) throws IOException {
            // 判断响应状态码是否为 200
            if (response.getStatusLine().getStatusCode() == HttpClientHelper.HTTP_STATUS_CODE_SUCCESS) {
                System.out.println("FileName: " + fileWrapper.getFileName());
                System.out.println("ContentType: " + fileWrapper.getContentType());
                System.out.println("ContentLength: " + fileWrapper.getContentLength());
                // 获取被下载文件对象
                fileWrapper.getFile();
                // 获取被下载文件输入流
                fileWrapper.getInputStream();
                // 将被下载文件写入目标文件
                fileWrapper.writeTo(distFile);
            } else {
                System.out.printf("ReasonPhrase: %s%n", response.getStatusLine().getReasonPhrase());
            }
        }
    });
}
```



**示例：** 自定义安全连接工厂并设置超时时间等配置参数

```java
public void customRequest(String url, String charset, Map<String, String> requestParams, URL certFilePath, String passwordChars) throws Exception {
    // 通过证书文件构建安全套接字工厂
    SSLConnectionSocketFactory socketFactory = HttpClientHelper.createConnectionSocketFactory("PKCS12", certFilePath, passwordChars.toCharArray());
    // 构建请求并设置超时时间等配置参数
    ContentType contentType = ContentType.create(HttpClientHelper.CONTENT_TYPE_FORM_URL_ENCODED, charset);
    IHttpResponse httpResponse = HttpClientHelper.create()
        .customSSL(socketFactory)
        .connectionTimeout(30000)
        .requestTimeout(30000)
        .socketTimeout(30000)
        .post(url, contentType, ParamUtils.buildQueryParamStr(requestParams, true, charset));
    if (httpResponse != null) {
        if (httpResponse.getStatusCode() == HttpClientHelper.HTTP_STATUS_CODE_SUCCESS) {
            // ......
        } else {
            System.out.printf("ResponseBody: %s%n", httpResponse);
        }
    }
}
```



### HttpRequestBuilder

在 HttpClientHelper 的基础上进行了优化、调整请求构建方式及响应的处理逻辑，除了 GET 和 POST 请求方法之外，还增加了对 PUT、 OPTIONS、 DELETE、 HEAD、 PATCH、 TRACE 等的支持。



**示例：** 构建并发送请求

```java
public void sendRequest(String url, String charset, Map<String, String> requestParams, URL certFilePath, String passwordChars) throws Exception {
    IHttpResponse httpResponse = HttpRequestBuilder.create(url)
        .socketFactory(HttpClientHelper.createConnectionSocketFactory("PKCS12", certFilePath, passwordChars.toCharArray()))
        .contentType(ContentType.create(HttpClientHelper.CONTENT_TYPE_FORM_URL_ENCODED, charset))
        .connectionTimeout(30000)
        .requestTimeout(30000)
        .socketTimeout(30000)
        .charset(charset)
        .addHeaders(new Header[]{
            new BasicHeader("Accept", "text/html"),
            new BasicHeader("Accept-Encoding", "gzip, deflate, br"),
            new BasicHeader("Accept-Language", "zh-Hans-CN,zh-CN;"),
            new BasicHeader("Cache-Control", "no-cache"),
            new BasicHeader("Pragma", "no-cache"),
            new BasicHeader("Connection", "keep-alive"),
            new BasicHeader("User-Agent", "Mozilla/5.0......")
        }).addParams(requestParams).build().post();
    if (httpResponse.getStatusCode() == HttpClientHelper.HTTP_STATUS_CODE_SUCCESS) {
        // ......
    } else {
        System.out.printf("ResponseBody: %s%n", httpResponse);
    }
}
```



**示例：** 新文件上传

```java
public static void newUploadFile(String url, File distFile) throws Exception {
    try (IHttpResponse response = HttpRequestBuilder.create(url).addContent("file", distFile).build().post()) {
        if (response.getStatusCode() == HttpClientHelper.HTTP_STATUS_CODE_SUCCESS) {
            // ......
        } else {
            System.out.printf("ResponseBody: %s%n", response);
        }
    }
}
```



**示例：** 新文件下载

```java
public void newDownloadFile(String url, File distFile) throws Exception {
    try (IHttpResponse response = HttpRequestBuilder.create(url)
         .socketFactory(HttpClientHelper.createConnectionSocketFactory(SSLContexts.custom().setProtocol("TLSv1.3").build()))
         .download(true).build().get()) {
        if (response.getStatusCode() == HttpClientHelper.HTTP_STATUS_CODE_SUCCESS) {
            IFileWrapper fileWrapper = response.getFileWrapper();
            //
            System.out.println("FileName: " + fileWrapper.getFileName());
            System.out.println("ContentType: " + fileWrapper.getContentType());
            System.out.println("ContentLength: " + fileWrapper.getContentLength());
            // 获取被下载文件对象
            fileWrapper.getFile();
            // 获取被下载文件输入流
            fileWrapper.getInputStream();
            // 将被下载文件写入目标文件
            fileWrapper.writeTo(distFile);
        }
    }
}
```



## JsonWrapper

JSON 包装器，为了让不同的第三方 JSON 解析器拥有统一的 API 接口调用方式并能够做到灵活切换而不影响业务系统的正常运行而提供的一套完整的包装层实现，现已对当前比较流行且使用非常广泛的 FastJson、Gson 和 Jackson 等进行了封装与适配，同时也支持通过 SPI 或 JVM 启动参数的形式配置基于 `IJsonAdapter` 接口的自定义实现类。

下面是第三方 JSON 解析器与已实现的包装器类的对应关系，以及需要引入的依赖包版本（或更高版本）：

- FastJson：net.ymate.platform.commons.json.impl.FastJsonAdapter

  ```xml
  <dependency>
      <groupId>com.alibaba</groupId>
      <artifactId>fastjson</artifactId>
      <version>1.2.83</version>
  </dependency>
  ```

- Gson：net.ymate.platform.commons.json.impl.GsonAdapter

  ```xml
  <dependency>
      <groupId>com.google.code.gson</groupId>
      <artifactId>gson</artifactId>
      <version>2.9.0</version>
  </dependency>
  ```

- Jackson：net.ymate.platform.commons.json.impl.JacksonAdapter

  ```xml
  <dependency>
      <groupId>com.fasterxml.jackson.datatype</groupId>
      <artifactId>jackson-datatype-jdk8</artifactId>
      <version>2.13.4</version>
  </dependency>
  ```



### 包装器加载逻辑

JSON 包装器是由 JsonWrapper 类进行统一维护和管理，当其被首次加载时，会按照 FastJson、Gson 和 Jackson 这一顺序依次尝试实例化对应的包装器类，加载成功则停止加载流程并返回（能否加载成功的依据是当前运行环境依赖库中是否存在与之对应的第三方 JSON 解析器的包文件），否则继续尝试直至未加载到任何结果为止。

当上述第三方 JSON 解析器的依赖包文件都（或大于一种）存在于当前运行环境时，默认将使用 FastJson 的 JSON 包装器实现。若此时希望使用指定的实现类做为默认 JSON 包装器时，可以通过 SPI 方式指定 `IJsonAdapterFactory` 工厂接口实现类，也可以通过 JVM 启动参数 `ymp.jsonAdapterClass` 进行如下配置：



**示例：** 通过 JVM 启动参数指定 Gson 做为默认 JSON 包装器

```shell
java -jar xxxx.jar -Dymp.jsonAdapterClass=net.ymate.platform.commons.json.impl.GsonAdapter
```



### 包装器的使用方法

包装器按其类型可划分为：对象包装器、数组包装器和节点包装器三种。



#### 对象包装器（IJsonObjectWrapper）

用于创建和维护 JsonObject 数据结构。



**示例：**

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
System.out.println("Key2: " + attrs.getString("key2"));
```



**执行结果：**

```shell
{
	"name":"suninformation",
	"realName":"有理想的鱼",
	"age":20,
	"gender":null,
	"attrs":{
		"key1":"value1",
		"key2":"value2"
	}
}
Name: suninformation
Age: 20
Key1: value1
Key2: value2
```



#### 数组包装器（IJsonArrayWrapper）

用于创建和维护 JsonArray 数据结构。



**示例：**

```java
// 创建 JsonArray 对象实例
IJsonArrayWrapper jsonArray = JsonWrapper.createJsonArray(new Object[]{1, null, 2, 3, false, true})
    .add(JsonWrapper.createJsonArray().add(new String[]{"a", "b"}))
    .add(JsonWrapper.createJsonObject(true)
         .put("name", "suninformation")
         .put("realName", "有理想的鱼")
         .put("age", 20)
         .put("gender", (String) null))
    .add(11);
// 采用格式化输出并保留值为空的属性
System.out.println(jsonArray.toString(true, false));
// 取值：
System.out.println("Index3: " + jsonArray.getInt(3));
System.out.println("Index4: " + jsonArray.getString(4));
IJsonObjectWrapper jsonObj = jsonArray.getJsonObject(7);
System.out.println("Name: " + jsonObj.getString("name"));
System.out.println("Age: " + jsonObj.getInt("age"));
```



**执行结果：**

```shell
[
	1,
	null,
	2,
	3,
	false,
	true,
	[
		["a","b"]
	],
	{
		"name":"suninformation",
		"realName":"有理想的鱼",
		"age":20
	},
	11
]
Index3: 3
Index4: false
Name: suninformation
Age: 20
```



#### 节点包装器（IJsonNodeWrapper）

在通过对象包装器或数组包装器提供的 `get` 方法获取对应的属性或索引下标值对象时，此值对象将被节点包装器重新包装，其作用是对被包装值对象的数据类型提供判断能力。



**示例：**

```java
// 创建复杂的 JsonObject 对象
IJsonObjectWrapper jsonObj = JsonWrapper.createJsonObject(true)
    .put("name", "suninformation")
    .put("realName", "有理想的鱼")
    .put("age", 20)
    .put("array", JsonWrapper.createJsonArray(new String[]{"a", "b"}))
    .put("attrs", JsonWrapper.createJsonObject()
         .put("key1", "value1")
         .put("key2", "value2"));
// 采用格式化输出并保留值为空的属性
System.out.println(jsonObj.toString(true, true));
// 遍历：
for (String key : jsonObj.keySet()) {
    IJsonNodeWrapper nodeWrapper = jsonObj.get(key);
    if (nodeWrapper.isJsonArray()) {
        // 判断当前元素是否为 JsonArray 对象
        System.out.println(nodeWrapper.getJsonArray().getString(0));
    } else if (nodeWrapper.isJsonObject()) {
        // 判断当前元素是否为 JsonObject 对象
        System.out.println(nodeWrapper.getJsonObject().getString("key1"));
    } else {
        // 否则为值对象，直接取值
        System.out.println(nodeWrapper.getString());
    }
}
```



**执行结果：**

```shell
{
	"name":"suninformation",
	"realName":"有理想的鱼",
	"age":20,
	"array":[
		"a",
		"b"
	],
	"attrs":{
		"key1":"value1",
		"key2":"value2"
	}
}
suninformation
有理想的鱼
20
a
value1
```



### 对象与字符串间转换



**示例：** 

```java
String jsonStr = "{\"age\":20,\"name\":\"suninformation\",\"real_name\":\"有理想的鱼\"}";
// 将字符串转换为 JSON 对象
JsonWrapper jsonWrapper = JsonWrapper.fromJson(jsonStr);
if (jsonWrapper.isJsonObject()) {
    IJsonObjectWrapper jsonObj = jsonWrapper.getAsJsonObject();
    // 取值：
    System.out.println("Name: " + jsonObj.getString("name"));
    System.out.println("Age: " + jsonObj.getInt("age"));
    System.out.println("RealName: " + jsonObj.getString("real_name"));
}
// 将 JSON 对象格式化输出为字符串
System.out.println(jsonWrapper.toString(true, true));
```



**执行结果：**

```shell
Name: suninformation
Age: 20
RealName: 有理想的鱼
{
	"age":20,
	"name":"suninformation",
	"real_name":"有理想的鱼"
}
```



### 对象序列化操作



**示例：**

```java
public class User {

    private String name;

    private Integer age;

    private String realName;

    //
    // 此处省略了Get/Set方法
    //

    @Override
    public String toString() {
        return String.format("User{name='%s', age=%d, realName='%s'}", name, age, realName);
    }

    public static void main(String[] args) throws Exception {
        User user = new User();
        user.setName("suninformation");
        user.setAge(20);
        user.setRealName("有理想的鱼");
        //
        byte[] serializeArr = JsonWrapper.serialize(user, true);
        User newUser = JsonWrapper.deserialize(serializeArr, User.class);
        System.out.println(newUser);
        // 采用 snakeCase 模式输出和反序列化操作
        String jsonStr = JsonWrapper.toJsonString(user, false, false, true);
        User newUser2 = JsonWrapper.deserialize(jsonStr, true, User.class);
        System.out.println(newUser2);
    }
}
```



**执行结果：**

```shell
User{name='suninformation', age=20, realName='有理想的鱼'}
User{name='suninformation', age=20, realName='有理想的鱼'}
```



## Markdown

对 Markdown 语法中常用到的格式，如：标题、文本、引用、表格、代码片段、图片、连接等进行对象封装，避免以往采用字符串拼接形式中经常出现的问题。



**示例：** 

```java
MarkdownBuilder markdownBuilder = MarkdownBuilder.create()
    .title("一级标题").p()
    .title("二级标题", 2).p()
    .text("文本")
    .tab().text("斜体文本", Text.Style.ITALIC)
    .space().text("组体文本", Text.Style.BOLD).p()
    .hr()
    .quote(MarkdownBuilder.create()
           .text("引用文本内容...").p()
           .append(ParagraphList.create()
                   .addItem("Item")
                   .addSubItem("SubItem")
                   .addBody("Item body."))).p()
    .append(Table.create()
            .addHeader("序号", Table.Align.CENTER)
            .addHeader("命令")
            .addHeader("描述", Table.Align.RIGHT).addRow()
            .addColumn("1")
            .addColumn(Code.create("mvn clean"))
            .addColumn("执行工程清理").build()).p()
    .image("Logo image.", "https://ymate.net/img/logo.png").p()
    .code("mvn clean source:jar install", "shell").br()
    .link("YMP", "https://ymate.net/");
System.out.println(markdownBuilder);
```



**输出内容：** 

````markdown
# 一级标题

## 二级标题

文本    *斜体文本* **组体文本**

------


> 引用文本内容...
>
> - Item
>
>     - SubItem
>
> Item body.


|序号|命令|描述|
|:---:|---|---:|
|1|`mvn clean`|执行工程清理|


![Logo image.](https://ymate.net/img/logo.png)

```shell
mvn clean source:jar install
```

[YMP](https://ymate.net/)
````





## 序列化（Serialize）

基于 `ISerializer` 接口实现对象序列化与反序列化操作，由 `SerializerManager` 对象序列化管理器维护管理，支持通过 `SPI` 机制和自动扫描 `@Serializer` 注解方式加载并注册，默认提供了几种实现方式：

- DefaultSerializer：基于 Java 对象序列化实现。

- JSONSerializer：基于 JSON 对象序列化实现。

- HessianSerializer：基于 Hessian 二进制对象序列化实现，需添加以下依赖包：

  ```xml
  <dependency>
      <groupId>com.caucho</groupId>
      <artifactId>hessian</artifactId>
      <version>4.0.66</version>
  </dependency>
  ```

- FstSerializer：基于 FST 二进制对象序列化实现，需添加以下依赖包：

  ```xml
  <dependency>
      <groupId>de.ruedigermoeller</groupId>
      <artifactId>fst</artifactId>
      <version>2.48-jdk-6</version>
  </dependency>
  ```

**示例一：** 对象序列化与反序列化操作

```java
public class SerialDemoBean implements Serializable {

    private String name;

    private String remark;

    //
    // 此处省略了Get/Set方法
    //

    @Override
    public String toString() {
        return String.format("SerialDemoBean{name='%s', remark='%s'}", name, remark);
    }

    public static void main(String[] args) throws Exception {
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
    }
}
```



**示例二：** 自定义对象序列化与反序列化实现

本例以通过自动扫描 `@Serializer` 注解方式加载并注册一个名称为`custom`的自定义对象序列化接口实现类。

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

@EnableAutoScan
@EnableBeanProxy
public class Starter {

    static {
        System.setProperty(IApplication.SYSTEM_MAIN_CLASS, Starter.class.getName());
    }

    private static final Log LOG = LogFactory.getLog(Starter.class);

    public static void main(String[] args) throws Exception {
        try (IApplication application = YMP.run(args)) {
            //
            SerialDemoBean demoBean = new SerialDemoBean();
            demoBean.setName("YMP");
            demoBean.setRemark("A lightweight modular simple and powerful Java framework.");
            //
            ISerializer serializer = SerializerManager.getSerializer("custom");
            byte[] bytes = serializer.serialize(demoBean);
            SerialDemoBean deserializeBean = serializer.deserialize(bytes, SerialDemoBean.class);
            //
            System.out.println(deserializeBean.toString());
        }
    }
}
```



## Utils

提供包含类与反射、字符串加密与解密、地理位置与编码、时期时间、正则表达式、文件、网络、参数、资源、运行时、线程操作等常用工具类封装。



### ClassUtils

类操作相关工具类。



####  扩展类加载器（ExtensionLoader）

其实现原理是在 SPI 加载机制的基础之上进行了扩展，增加了对是否优先加载内部（Internal）默认服务配置的处理逻辑，SPI 服务配置文件的存放路径说明如下：

- `META-INF/services/internal/` ：内部配置路径用于存放默认配置。
- `META-INF/services/` ：服务配置路径，该路径将优先于内部配置路径被加载。



**示例一：** 扩展类加载器的基本使用方法

**步骤1：** 定义 `IDemoService` 服务接口及两个实现类。

```java
package net.ymate.demo.service;

/**
 * 示例服务接口类
 */
public interface IDemoService {

    /**
     * 执行业务逻辑
     *
     * @return 返回执行结果
     */
    String doService();
}


package net.ymate.demo.service.impl;

/**
 * 示例服务接口实现类：DemoOneService
 */
public class DemoOneServiceImpl implements IDemoService{
    @Override
    public String doService() {
        return "来自 DemoOneService 的接口实现。";
    }
}

package net.ymate.demo.service.impl;

/**
 * 示例服务接口实现类：DemoTwoService
 */
public class DemoTwoServiceImpl implements IDemoService{
    @Override
    public String doService() {
        return "来自 DemoTwoService 的接口实现。";
    }
}
```

**步骤2：** 在内部配置路径 `META-INF/services/internal/` 中添加 `SPI` 配置文件，内容如下：

```shell
# more META-INF/services/internal/net.ymate.demo.service.IDemoService
net.ymate.demo.service.impl.DemoOneServiceImpl
```

**步骤3：** 加载并执行业务逻辑。

```java
public class Demo {

    public static void main(String[] args) throws Exception {
        IDemoService demoService = ClassUtils.getExtensionLoader(IDemoService.class).getExtension();
        if (demoService != null) {
            // 此处执行输出结果为：来自 DemoOneService 的接口实现。
            System.out.println(demoService.doService());
        }
    }
}
```

**步骤4：** 在自定义配置路径 `META-INF/services/` 中添加 `SPI` 配置文件，内容如下：

```shell
# more META-INF/services/net.ymate.demo.service.IDemoService
net.ymate.demo.service.impl.DemoTwoServiceImpl
```

**步骤5：** 再次加载并执行业务逻辑。

```java
public class Demo {

    public static void main(String[] args) throws Exception {
        IDemoService demoService = ClassUtils.getExtensionLoader(IDemoService.class).getExtension();
        if (demoService != null) {
            // 此处执行输出结果为：来自 DemoTwoService 的接口实现。
            System.out.println(demoService.doService());
        }
    }
}
```

通过本例可以清楚的知道，当通过 `ClassUtils.getExtensionLoader` 方法加载指定接口类的 `SPI` 配置时，其首先尝试加载自定义配置路径下的配置文件，若配置文件存在则加载并返回，否则尝试从内部配置路径中加载。



**示例二：** 加载指定业务接口多实例

根据 **示例一** 的配置，通过以下示例展示如何获取业务接口所配置的全部实现类及实现类实例对象：

```java
public class Demo {

    public static void main(String[] args) throws Exception {
        // 获取指定业务接口配置的所有实现类类型
        List<Class<IDemoService>> demoServiceClasses = ClassUtils.getExtensionLoader(IDemoService.class, true).getExtensionClasses();
        if (demoServiceClasses != null) {
            demoServiceClasses.forEach(demoServiceClass -> System.out.println(demoServiceClass.getName()));
        }
        // 获取指定业务接口配置的所有实现类实例对象
        List<IDemoService> demoServiceImpls = ClassUtils.getExtensionLoader(IDemoService.class, true).getExtensions();
        if (demoServiceImpls != null) {
            demoServiceImpls.forEach(demoServiceImpl -> System.out.println(demoServiceImpl.doService()));
        }
    }
}
```

本例中通过 `ClassUtils.getExtensionLoader` 方法的第二个参数 `alwaysInternal` 是用来指定本次操作是否强制加载内部配置路径，所示需要开发人员自行根据实际业务情况合理使用。



#### 类包裹器（BeanWrapper）

赋予对象简单的类属性及方法的操作能力。



**示例：** 综合展示类包裹器的使用方法

```java
public class Demo {

    private String name;

    private Integer age;

    //
    // 此处省略了Get/Set方法
    //

    public static void main(String[] args) throws Exception {
        // 构建指定类的包裹器实例
        ClassUtils.BeanWrapper<Demo> beanWrapper = ClassUtils.wrapperClass(Demo.class);
        // 遍历类成员名称
        beanWrapper.getFieldNames().forEach(System.out::println);
        // 遍历类成员对象
        beanWrapper.getFields().forEach(field -> System.out.println(field.getName()));
        // 遍历类方法
        beanWrapper.getMethods().forEach(method -> System.out.println(method.getName()));
        // 为成员变量赋值
        beanWrapper.getFields().forEach(field -> {
            try {
                beanWrapper.setValue(field, BlurObject.bind("10").toObjectValue(field.getType()));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
        // 通过映射向成员变量赋值
        Map<String, Object> values = new HashMap<>();
        values.put("name", "suninformation");
        values.put("age", 18);
        beanWrapper.fromMap(values, ((fieldName, fieldValue) -> {
            // 排除 age 属性
            return StringUtils.equals(fieldName, "age");
        }));
        // 获取成员变量值
        beanWrapper.getFields().forEach(field -> {
            try {
                System.out.println(beanWrapper.getValue(field));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
        // 将类成员属性转为映射（支持属性过滤）
        beanWrapper.toMap((fieldName, fieldValue) -> {
            // 排除 age 属性
            return StringUtils.equals(fieldName, "age");
        }).forEach((key, value) -> System.out.println(key + ": " + value));
        // 获取当前类实例对象
        Demo demo = beanWrapper.getTargetObject();
        // 类属性浅拷贝（支持属性过滤）
        Demo newDemo = beanWrapper.duplicate(new Demo(), (fieldName, fieldValue) -> {
            // 排除 age 属性
            return StringUtils.equals(fieldName, "age");
        });
    }
}
```



#### 获取目标类上声明的注解



**示例：** 

```java
Converter converterAnn = ClassUtils.getAnnotation(Demo.class, Converter.class);
```



#### 获取数组元素的数据类型



**示例：** 

```java
Class<?> clazz = ClassUtils.getArrayClassType(String[].class);
System.out.println(String.class.equals(clazz));
```



#### 获取类中成员声明的第一个注解

用于查找任一声明了指定注解的成员对象。



**示例：** 

```java
PairObject<Field, Id> id = ClassUtils.getFieldAnnotationFirst(Demo.class, Id.class);
```



#### 获取类中成员声明的所有注解

用于查找所有声明了指定注解的成员对象。



**示例：** 

```java
List<PairObject<Field, Property>> properties = ClassUtils.getFieldAnnotations(Demo.class, Property.class);
```



#### 获取指定类所有的成员对象

> 若包含其父类对象，直至其父类为空。



**示例：** 

```java
List<Field> fields = ClassUtils.getFields(Demo.class, true);
```



#### 获取类中实现的接口名称集合



**示例：** 

```java
String[] interfaceNames = ClassUtils.getInterfaceNames(Demo.class);
```



#### 获取泛型的数据类型集合

:::tip 注意：

不适用于泛型嵌套，即泛型里若包含泛型则返回此泛型的 RawType 类型

:::



**示例：** 

```java
public class Demo {

    public static class A<T extends Number, P extends Serializable> {
    }

    public static class B extends A<Integer, String> {
    }

    public static void main(String[] args) throws Exception {
        List<Class<?>> parameterizedTypes = ClassUtils.getParameterizedTypes(B.class);
        System.out.println(parameterizedTypes);
    }
}
```



**执行结果：**

```shell
[class java.lang.Integer, class java.lang.String]
```



#### 获取指定的类所有方法对象

> 若包含其父类对象，直至其父类为空。



**示例：** 

```java
List<Method> methods = ClassUtils.getMethods(Demo.class, true);
```



#### 获取方法的参数名集合



**示例：** 

```java
List<Method> methods = ClassUtils.getMethods(Demo.class, true);
for (Method method : methods) {
    String[] paramNames = ClassUtils.getMethodParamNames(method);
    for (String paramName : paramNames) {
        System.out.println(paramName);
    }
}
```



#### 获取目标类被指定注解声明的包对象

> 包含上级包直到包对象为空。



**示例：** 

```java
Package pkg = ClassUtils.getPackage(Demo.class, Before.class);
```



#### 获取目标类所在包声明的指定注解

> 包含上级包直到包对象为空。



**示例：** 

```java
Before beforeAnn = ClassUtils.getPackageAnnotation(Demo.class, Before.class);
```



#### 获得指定名称并限定接口的实现类



**示例：** 

```java
String implClassName = "net.ymate.demo.service.impl.DemoOneServiceImpl";
IDemoService demoService = ClassUtils.impl(implClassName, IDemoService.class, Demo.class);
```



#### 获得指定名称并限定接口且通过特定参数类型构造的实现类



**示例：** 

```java
public class DemoService implements IDemoService {

    private final String name;

    public DemoService(String name) {
        this.name = name;
    }

    public static void main(String[] args) throws Exception {
        IDemoService demoService = ClassUtils.impl(DemoService.class, IDemoService.class, new Class<?>[]{String.class}, new Object[]{"suninformation"}, false);
        System.out.println(demoService.doService());
    }

    @Override
    public String doService() {
        return name;
    }
}
```



#### 判断对象是否存在指定注解



**示例：** 

```java
boolean has = ClassUtils.isAnnotationOf(Demo.class, Before.class);
```



#### 判断类中是否实现指定接口



**示例：** 

```java
boolean has = ClassUtils.isInterfaceOf(Demo.class, IDemoService.class);
```



#### 验证指定类是否不为空且仅是接口或类



**示例：** 

```java
boolean isNormalClass = ClassUtils.isNormalClass(Demo.class);
```



#### 验证 Method 是否为公有非静态、非抽象且不属于 Object 基类方法



**示例：** 

```java
List<Method> methods = ClassUtils.getMethods(Demo.class, true);
for (Method method : methods) {
    if (ClassUtils.isNormalMethod(method)) {
        System.out.println(method.getName());
    }
}
```



#### 验证类成员是否正常



**示例：** 

```java
List<Field> fields = ClassUtils.getFields(Demo.class, true);
for (Field field : fields) {
    if (ClassUtils.isNormalField(field)) {
        System.out.println(field.getName());
    }
}
```



#### 判断类是否是为指定类的子类



**示例：** 

```java
public class DemoService extends DemoOneServiceImpl {

    public static void main(String[] args) throws Exception {
        boolean isSubclass = ClassUtils.isSubclassOf(DemoService.class, DemoOneServiceImpl.class);
        System.out.println(isSubclass);
    }
}
```

#### 处理字段名称使其符合 JavaBean 属性串格式


**示例：**

```java
// 将 "user_name" 转换为 "UserName"
ClassUtils.propertyNameToFieldName("user_name");
```


#### 将 JavaBean 属性串格式转换为下划线小写方式


**示例：**

```java
// 将 "userName" 转换为 "user_name"
ClassUtils.fieldNameToPropertyName("userName", 0);
```


### CodecUtils

提供了 AES、PBE 和 RSA 加密与解密工具类。



**示例：** 为字符串进行 AES 加密与解密操作

```java
CodecUtils.CodecHelper codecHelper = CodecUtils.AES;
// codecHelper = new CodecUtils.AESCodecHelper(128, 128);
// 生成密钥
String key = codecHelper.initKeyToString();
String content = "被加密的文本";
String encryptContent = codecHelper.encrypt(content, key);
// 判断加密前与解密后的内容是否一致
System.out.println(codecHelper.decrypt(encryptContent, key).equalsIgnoreCase(content));
```



**示例：** 为字符串进行 PBE 加密与解密操作

```java
CodecUtils.CodecHelper codecHelper = CodecUtils.PBE;
// codecHelper = new CodecUtils.PBECodecHelper(128);
// 生成密钥
String key = codecHelper.initKeyToString();
String content = "被加密的文本";
String encryptContent = codecHelper.encrypt(content, key);
// 判断加密前与解密后的内容是否一致
System.out.println(codecHelper.decrypt(encryptContent, key).equalsIgnoreCase(content));
```



**示例：** 为字符串进行 RSA 加密与解密操作

```java
CodecUtils.RSACodecHelper codecHelper = CodecUtils.RSA;
// codecHelper = new CodecUtils.RSACodecHelper(1024);
// 生成密钥
PairObject<RSAPublicKey, RSAPrivateKey> keys = codecHelper.initRSAKey();
// 提取公钥串
String publicKey = codecHelper.getRSAKey(keys.getKey());
// 提取私钥串
String privateKey = codecHelper.getRSAKey(keys.getValue());
String content = "被加密的文本";
// 签名与验签
String signStr = codecHelper.sign(content, privateKey);
System.out.println(codecHelper.verify(content.getBytes(StandardCharsets.UTF_8), publicKey, signStr));
// 方式一：私钥加密 -> 公钥解密
String encryptContent = codecHelper.encrypt(content, privateKey);
System.out.println(codecHelper.decryptPublicKey(encryptContent, publicKey).equalsIgnoreCase(content));
// 方式二：公钥加密 -> 私钥解密
encryptContent = codecHelper.encryptPublicKey(content, publicKey);
System.out.println(codecHelper.decrypt(encryptContent, privateKey).equalsIgnoreCase(content));
```



### DateTimeUtils

日期时间及格式转换工具类。



**示例：** 

```java
// 获取时区信息
String[] timeZoneArr = DateTimeUtils.TIME_ZONES.get("8");
TimeZone timeZone = DateTimeUtils.getTimeZone("8");
// 全局时间修正偏移量
DateTimeUtils.TIMEZONE_OFFSET = "8";
// 获取当前日期时间
Date currentTime = DateTimeUtils.currentTime();
// 获取当前时间毫秒值
long currentTimeMillis = DateTimeUtils.currentTimeMillis();
// 获取当前 UTC 时间
long currentTimeUTC = DateTimeUtils.currentTimeUTC();
// 获取当前系统 UTC 时间
int systemTimeUTC = DateTimeUtils.systemTimeUTC();
// 格式化输出时间字符串
String dateTimeStr = DateTimeUtils.formatTime(systemTimeUTC, DateTimeUtils.YYYY_MM_DD_HH_MM_SS_SSS);
// 解析格式化时间字符串为日期对象
Date date = DateTimeUtils.parseDateTime(dateTimeStr, DateTimeUtils.YYYY_MM_DD_HH_MM_SS_SSS);
// 判断指定年份是否闰年
DateTimeUtils.isLeapYear(2021);
```



### ExpressionUtils

表达式字符串替换工具类，使用正则表达式替换使用 `${}` 占位的变量值。



**示例：** 

```java
// 定义表达式字符串（其中包含三个变量）
String exprStr = "I am ${name}, and gender is ${gender}. ${other}";
// 创建表达式替换工具实例对象
ExpressionUtils expr = ExpressionUtils.bind(exprStr);
// 提取表达式字符串中存在的变量名称集合
expr.getVariables().forEach(System.out::println);
// 根据变量名称替换对应的值
expr.set("name", "Henry").set("gender", "M");
// 或通过映射替换对应变量的值
Map<String, Object> values = new HashMap<>();
values.put("name", "Henry");
values.put("gender", "M");
expr.set(values);
// 清理未被替换的变量
expr.clean();
// 获取替换变量后的最终结果
System.out.println(expr.getResult());
```



**执行结果：** 

```shell
name
gender
other
I am Henry, and gender is M. 
```



### FileUtils

文件处理工具类。



**示例：** 

```java
File demoFile = new File(RuntimeUtils.replaceEnvVariable("${root}/files/demo.text"));
// 在指定路径中创建文件（同时生成其父级目录），若内容流不为空则写入内容
boolean success = FileUtils.createFileIfNotExists(demoFile, new FileInputStream(RuntimeUtils.replaceEnvVariable("${root}/files/origin.text")));
// 在指定路径中创建空文件（同时生成其父级目录）
FileUtils.createEmptyFile(demoFile);
// 提取文件扩展名称，若不存在则返回空字符串
String extName = FileUtils.getExtName(demoFile.getName());
// 获取文件MD5签名值
String hash = FileUtils.getHash(demoFile);
// 按数组顺序查加载文件并返回第一个成功读取的文件输入流
String[] filePaths = new String[]{
    RuntimeUtils.replaceEnvVariable("${root}/files/a.properties"),
    RuntimeUtils.replaceEnvVariable("${root}/files/b.properties"),
    RuntimeUtils.replaceEnvVariable("${root}/files/c.properties")
};
InputStream inputStream = FileUtils.loadFileAsStream(filePaths);
// 将文件路径转换成 URL 对象, 返回值可能为 NULL
URL url = FileUtils.toURL(demoFile.getPath());
// 将 URL 地址转换成 File 对象
File file = FileUtils.toFile(url);
// 将数组文件集合压缩成单个 ZIP 文件
File[] files = new File[]{
    new File(RuntimeUtils.replaceEnvVariable("${root}/demo.text")),
    new File(RuntimeUtils.replaceEnvVariable("${root}/origin.text"))
};
File zipFile = FileUtils.toZip("text_", files);
// 从 JAR 包中提取 /META-INF/files 目录下的资源文件并复制到 ${root}/files 目录中
boolean hasUnpacked = FileUtils.unpackJarFile("files", new File(RuntimeUtils.replaceEnvVariable("${root}/files")));
// 复制目录（递归）
FileUtils.writeDirTo(new File(RuntimeUtils.replaceEnvVariable("${root}/files")), new File(RuntimeUtils.replaceEnvVariable("${root}/newFiles")));
// 复制文件
FileUtils.writeTo(demoFile, new File(RuntimeUtils.replaceEnvVariable("${root}/newFiles/demo.txt")));
```



### GeoUtils

地理位置与编码计算相关工具类。



#### 坐标点（GeoPoint）

通过经度和纬度表示一个点，目前支持的坐标系（GeoPointType）包括：GPS（WGS84）、火星（GCJ02）、百度（BD09），默认取值为：GPS（WGS84）。



**示例：** 坐标点的创建与相关操作

```java
// 定义一个坐标点（经度，纬度）
double lon = 110.02;
double lat = 23.62;
GeoPoint point = new GeoPoint(lon, lat, GeoPointType.WGS84);
// 判断当前坐标点是否超出中国范围
boolean notIn = point.notInChina();
// 经纬度值保留小数点后六位
GeoPoint newPoint = point.retain6();
// 计算两点间的距离（米）
double result = point.distance(new GeoPoint(106.69, 34.89));
// 将当前坐标点转换为不同坐标系
GeoPoint bd09 = point.toBd09();
GeoPoint gcj02 = point.toGcj02();
GeoPoint wgs84 = point.toWgs84();
```



#### 圆形区域（GeoCircle）



**示例：** 圆形区域的创建与相关操作

```java
// 定义一个圆形区域（中心点，半径）
GeoPoint centerPoint = new GeoPoint(110.02, 23.62);
GeoCircle circle = new GeoCircle(centerPoint, 500);
// 判断点是否在圆形范围内：-1 表示点在圆外，0 表示点在圆上，1 表示点在圆内
int result = circle.contains(point);
```



#### 矩形区域（GeoBound)



**示例：** 矩形区域的创建与相关操作

```java
// 定义一个矩形区域（左下角坐标点，右上角坐标点）
GeoBound boundOne = new GeoBound(new GeoPoint(110.02, 23.62), new GeoPoint(116.69, 39.89));
// 定义一个矩形区域，取两个矩形区域的并集
GeoBound boundTwo = new GeoBound(new GeoPoint(110.02, 23.62), new GeoPoint(106.69, 34.89));
GeoBound bound = new GeoBound(boundOne, boundTwo);
// 获取矩形区域的中心点坐标
GeoPoint center = bound.getCenter();
// 判断矩形区域是否完全包含于此矩形区域中
boolean result = bound.contains(boundOne);
// 判断坐标点是否位于此矩形内
result = bound.contains(new GeoPoint(110.02, 23.62));
```



**示例：** 创建从坐标点到指定距离（米）的矩形范围

```java
GeoBound bound = GeoUtils.rectangle(new GeoPoint(110.02, 23.62), 500);
```



#### 多边型区域（GeoPolygon）



**示例：** 

```java
// 定义一个多边形区域
GeoPolygon polygon = new GeoPolygon(new GeoPoint[]{
    new GeoPoint(116.69, 39.89),
    new GeoPoint(106.69, 34.89)
});
// 判断坐标点是否在此多边形区域内
boolean result = polygon.in(new GeoPoint(116.69, 39.89));
// 判断坐标点是否在此多边形区域边界上
result = polygon.on(new GeoPoint(116.69, 39.89));
```



### ImageUtils

图片处理工具类，使用时需在工程中引入以下依赖包：

```xml
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>javase</artifactId>
    <version>3.4.1</version>
</dependency>
<dependency>
    <groupId>net.coobird</groupId>
    <artifactId>thumbnailator</artifactId>
    <version>0.4.17</version>
</dependency>
```



**示例：** 计算图片文件 dHash 值

```java
String dHash = ImageUtils.dHash(ImageIO.read(new File(RuntimeUtils.getRootPath(), "demo.png")));
```



**示例：** 计算海明（Hamming Distance）距离

```java
String dHash = ImageUtils.dHash(ImageIO.read(new File(RuntimeUtils.getRootPath(), "demo.png")));
String dHash2 = ImageUtils.dHash(ImageIO.read(new File(RuntimeUtils.getRootPath(), "demo_resized.png")));
long hamming = ImageUtils.hammingDistance(dHash, dHash2);
```



**示例：** 替换原图片里面的二维码

```java
BufferedImage qrCodeImage = QRCodeHelper.create("https://ymate.net/", 300, 300, ErrorCorrectionLevel.H).toBufferedImage();
BufferedImage newImage = ImageUtils.replaceQrCode(ImageIO.read(new File(RuntimeUtils.getRootPath(), "demo.png")), qrCodeImage);
if (!ImageIO.write(newImage, "jpeg", new File(RuntimeUtils.getRootPath(), "newImage.jpeg"))) {
    throw new IOException(String.format("Could not write an image of format %s", "jpeg"));
}
```



**示例：** 将图片等比例缩放50%

```java
String basePath = RuntimeUtils.getRootPath();
File source = new File(basePath, "demo.png");
ImageUtils.resize(ImageIO.read(source), new File(basePath, "demo_resized.png"), 0.5f, 1.0f);
```



**示例：** 将图片宽度调整为100像素且高度等比例缩放

```java
String basePath = RuntimeUtils.getRootPath();
File source = new File(basePath, "demo.webp");
ImageUtils.resize(ImageIO.read(source), new File(basePath, "demo_resized.webp"), 100, -1, 1.0f);
```



:::tip 关于对 webp 图片格式的支持

Webp 是 Google 推出的一种新型图片格式，相比于传统的 PNG 和 JPG 图片有着更小体积的优势，在 Web 中有着广泛的应用。

由于 Webp 格式推出比较晚，JDK 内置的图片编解码库对此并不支持。

这里推荐大家使用开源项目：[https://github.com/nintha/webp-imageio-core](https://github.com/nintha/webp-imageio-core)

由于这个项目并未发布到 Maven 中央仓库，所以需要手动下载并安装到本地仓库，执行命令如下：

```shell
git clone https://github.com/nintha/webp-imageio-core.git
cd webp-imageio-core
mvn clean source:jar install
```

然后，在工程的 pom.xml 文件中添加如下依赖配置：

```xml
<dependency>
    <groupId>com.github.nintha</groupId>
    <artifactId>webp-imageio-core</artifactId>
    <version>0.1.3</version>
</dependency>
```

该依赖包中已集成了跨平台动态链接库依赖，经测试只需引入即可支持 Webp 图片格式，而无需编写任何代码，更多使用方法请阅读此项目源码及文档。

:::



### MimeTypeUtils

文件的 MimeType 类型处理工具类。

> 该类在初始化时，将首先加载类路径下的 `mimetypes-conf.properties` 文件，若未找到或内容为空则加载 `META-INF/mimetypes-default-conf.properties` 默认配置文件。



**示例：** 

```java
// 根据文件扩展名获取对应的 MIME_TYPE 类型
String mimeType = MimeTypeUtils.getFileMimeType("doc");
// 根据 MIME_TYPE 类型获取对应的文件扩展名
String extName = MimeTypeUtils.getFileExtName("text/plain");
```



### NetworkUtils

网络操作相关工具类。



**示例：** 

```java
// 获取本地所有的IP地址数组
String[] ipAddrs = NetworkUtils.IP.getHostIPAddresses());
// 获取一个DNS或计算机名称所对应的IP地址数组
ipAddrs = NetworkUtils.IP.getHostIPAddresses("localhost");
// 获取本机名称
String hostName = NetworkUtils.IP.getHostName();
// 验证IP地址有效性
boolean isValid = NetworkUtils.IP.isIPAddr("192.168.3.6");
// 检查IPv4地址的合法性
isValid = NetworkUtils.IP.isIPv4("192.168.3.6");
// 检查IPv6地址的合法性
isValid = NetworkUtils.IP.isIPv6("0:0:0:0:0:0:0:1");
```



### ParamUtils

HTTP 请求参数编码及处理相关工具类。



**示例：** 参数有效性判断

```java
String str = "";
Collection<String> params = new ArrayList<>();
Map<String, String> map = new HashMap<>();
// 验证参数值不为 null 或空 且元素数量不为 0
ParamUtils.isInvalid(str);
ParamUtils.isInvalid(params);
ParamUtils.isInvalid(map);
```



**示例：** 对参数进行 ASCII 正序排列并生成请求参数串

```java
Map<String, Object> params = new HashMap<>();
params.put("name", "suninformation");
params.put("nickName", "有理想的鱼");
params.put("age", 20);
params.put("gender", "M");
System.out.println(ParamUtils.buildQueryParamStr(params, true, "UTF-8"));
```



**执行结果：**

```shell
age=20&gender=M&name=suninformation&nickName=%E6%9C%89%E7%90%86%E6%83%B3%E7%9A%84%E9%B1%BC
```



**示例：** 将参数拼装到 URL 请求中

```java
Map<String, Object> params = new HashMap<>();
params.put("name", "suninformation");
params.put("age", 20);
System.out.println(ParamUtils.appendQueryParamValue("/user/find?gender=M", params, true, "UTF-8"));
```



**执行结果：**

```shell
/user/find?gender=M&age=20&name=suninformation
```



**示例：** 将 Map<String, ?> 转换为 Map<String, String[]>

```java
Map<String, Object> params = new HashMap<>();
params.put("name", "suninformation");
params.put("age", 20);
Map<String, String[]> convertMap = ParamUtils.convertParamMap(params);
```



**示例：** 解析 URL 参数并转换成 Map<String, String[]> 映射

```java
String url = "/user/find?age=20&gender=M&name=suninformation&nickName=%E6%9C%89%E7%90%86%E6%83%B3%E7%9A%84%E9%B1%BC";
Map<String, String[]> params = ParamUtils.parseQueryParamStr(url, true, "UTF-8");
```



**示例：** 产生随机字符串，长度为6到32位不等

```java
String nonceStr = ParamUtils.createNonceStr()
```



**示例：** 为请求参数进行签名

```java
Map<String, Object> params = new HashMap<>();
params.put("name", "suninformation");
params.put("nickName", "有理想的鱼");
params.put("age", 20);
params.put("gender", "M");
// 默认采用 MD5 进行签名
String signStr = ParamUtils.createSignature(params, true, true, "扩展参数1", "扩展参数n");
// 或自定义签名逻辑
signStr = ParamUtils.createSignature(params, true, true, new ParamUtils.ISignatureBuilder() {
    @Override
    public String build(String content) {
        // 自定义签名逻辑
        return DigestUtils.md5Hex(content);
    }
}, "扩展参数1", "扩展参数n")
```



**示例：** 构建自动提交的 Form 表单

```java
Map<String, Object> params = new HashMap<>();
params.put("name", "suninformation");
params.put("nickName", "有理想的鱼");
params.put("age", 20);
params.put("gender", "M");
System.out.println(ParamUtils.buildActionForm("/order/create", true, true, true, "UTF-8", params));
```



**执行结果：**

```shell
<form id="_payment_submit" name="_payment_submit" action="/order/create" method="POST"" enctype="application/x-www-form-urlencoded;charset=UTF-8"><input type="hidden" name="gender" value="M"><input type="hidden" name="nickName" value="%E6%9C%89%E7%90%86%E6%83%B3%E7%9A%84%E9%B1%BC"><input type="hidden" name="name" value="suninformation"><input type="hidden" name="age" value="20"><input type="submit" value="doSubmit" style="display:none;"></form><script>document.forms['_payment_submit'].submit();</script>
```



### ResourceUtils

资源加载工具类。



**示例：** 获取类路径下资源文件的 URL 路径

```java
public class Demo {

    public static void main(String[] args) throws Exception {
        URL url = ResourceUtils.getResource("conf.properties", Demo.class);
        // ......
    }
}
```



**示例：** 获取类路径下资源文件的输入流

```java
public class Demo {

    public static void main(String[] args) throws Exception {
        InputStream inputStream = ResourceUtils.getResourceAsStream("conf.properties", Demo.class);
        // ......
    }
}
```



**示例：** 按数组顺序加载类路径下资源文件输入流

```java
public class Demo {

    public static void main(String[] args) throws Exception {
        InputStream inputStream = ResourceUtils.getResourceAsStream(Demo.class, "conf.properties", "conf_dev.properties");
        // ......
    }
}
```



### RuntimeUtils

运行时工具类，获取运行时相关信息。



**示例：** 

```java
// 定义 MBean 接口
public interface IDemoMBean {

    String getInfo();
}

// 实现 MBean 接口
public class DemoMBean extends StandardMBean implements IDemoMBean {

    private final ObjectName objectName;

    public <T> DemoMBean() throws NotCompliantMBeanException, MalformedObjectNameException {
        super(IDemoMBean.class);
        objectName = new ObjectName("demo.mbean", "type", "demo");
    }

    public ObjectName getObjectName() {
        return objectName;
    }

    @Override
    public String getInfo() {
        return "MBean Info.";
    }
}

public class Demo {

    public static void main(String[] args) throws Exception {
        // 获取应用根路径（若 WEB 工程则基于 .../WEB-INF 返回，若普通工程则返回类所在路径）
        RuntimeUtils.getRootPath();
        // 若获取的路径为空则默认使用 user.dir 路径（结尾的斜杠字符将被移除）
        // 若 WEB 工程是否保留 WEB-INF
        RuntimeUtils.getRootPath(true);
        // 调整当获取应用根路径为空则默认使用 user.dir 路径替换 ${root}、${user.dir} 和 ${user.home} 环境变量
        RuntimeUtils.replaceEnvVariable("${root}");
        // 注册 JMXBean
        DemoMBean mBean = new DemoMBean();
        RuntimeUtils.registerManagedBean(mBean.getObjectName(), mBean);
        // 解注册 JMXBean
        RuntimeUtils.unregisterManagedBean(mBean.getObjectName());
        // 返回当前程序执行进程编号
        RuntimeUtils.getProcessId();
        // 当前操作系统是否为 Windows 系统
        RuntimeUtils.isWindows();
        // 当前操作系统是否为类 Unix 系统
        RuntimeUtils.isUnixOrLinux();
        // 根据格式化字符串并生成运行时异常
        RuntimeUtils.makeRuntimeThrow("code: %s, msg: %s", "10001", "System Error.");
        // 垃圾回收并返回回收的字节数
        RuntimeUtils.gc();
        //
        try {
            // ......
        } catch (Exception e) {
            // 解包异常
            RuntimeUtils.unwrapThrow(e);
            // 通过异常构建运行时异常
            RuntimeUtils.wrapRuntimeThrow(e);
            // 通过异常构建自定义消息的运行时异常
            RuntimeUtils.wrapRuntimeThrow(e, "code: %s, msg: %s", "10001", "System Error.");
        }
    }
}
```



### ThreadUtils

线程操作工具类。



**示例：** 

```java
// 自定义线程工厂
ThreadFactory threadFactory = DefaultThreadFactory.create("prefix_");
// 创建单线程的线程池
ExecutorService executorService = ThreadUtils.newSingleThreadExecutor(1, threadFactory);
// 创建自定义线程池
executorService = ThreadUtils.newThreadExecutor(3, 5, 30000, Integer.MAX_VALUE, threadFactory);
// 创建可缓存的线程池
executorService = ThreadUtils.newCachedThreadPool(10);
// 创建固定线程数量的线程池
executorService = ThreadUtils.newFixedThreadPool(10);
// 创建定时器线程池
executorService = ThreadUtils.newScheduledThreadPool(10);
//
ThreadUtils.shutdownExecutorService(executorService, 30000, 10000);
// 创建临时线程池并执行线程，等待线程执行结束后关闭池程池并返回线程执行结果
String result = ThreadUtils.executeOnce(new Callable<String>() {
    @Override
    public String call() throws Exception {
        // 执行线程业务逻辑
        return null;
    }
});
// 创建临时线程池并执行线程，等待线程执行结束或超过指定时间主动关闭池程池并返回线程执行结果
result = ThreadUtils.executeOnce(new Callable<String>() {
    @Override
    public String call() throws Exception {
        // 执行线程业务逻辑
        return null;
    }
}, 30000, new ThreadUtils.IFutureResultFilter<String>() {
    @Override
    public String filter(FutureTask<String> futureTask) throws ExecutionException, InterruptedException {
        // 此处获取线程执行结果
        return null;
    }
});
// 创建临时线程池并批量执行线程，等待线程执行结束或超过指定时间主动关闭池程池并返回各线程执行结果集合
List<Callable<String>> callables = new ArrayList<>();
callables.add(new Callable<String>() {
    @Override
    public String call() throws Exception {
        // 执行线程业务逻辑
        return null;
    }
});
List<String> results = ThreadUtils.executeOnce(callables, 30000);
// 支持对线程执行结果的自定义处理逻辑
results = ThreadUtils.executeOnce(callables, 30000L, new ThreadUtils.IFutureResultFilter<String>() {
    @Override
    public String filter(FutureTask<String> futureTask) throws ExecutionException, InterruptedException {
        return null;
    }
});
```



### UUIDUtils

UUID 生成器工具类。



**示例：**

```java
public class Demo {

    public static void main(String[] args) throws Exception {
        // 采用 JDK 自身 UUID 生成器生成主键并替换 '-' 字符
        System.out.println(UUIDUtils.UUID());
        // 10个随机字符（基于当前时间和一个随机字符串）
        System.out.println(UUIDUtils.generateRandomUUID());
        // 唯一的16位字符串（基于32位当前时间和32位对象的 identityHashCode 和32位随机数）
        // （以下所传入参数为预加密字符串）
        System.out.println(UUIDUtils.generateCharUUID("12345678"));
        // 唯一的数值型随机字符串
        System.out.println(UUIDUtils.generateNumberUUID("12345678"));
        // 基于主机前缀的唯一的随机字符串
        System.out.println(UUIDUtils.generatePrefixHostUUID("12345678"));
        // 生成指定长度的生成随机字符串（可指定是否仅生成数值型）
        System.out.println(UUIDUtils.randomStr(10, false));
        // 生成指定范围的整型随机数
        System.out.println(UUIDUtils.randomInt(10, 50));
        // 生成指定范围的长整型随机数
        System.out.println(UUIDUtils.randomLong(100, 500));
    }
}
```



**执行结果：** 

```shell
cd056590af2b4842a2b568143e6caa3e
qLZ47GSzwN
h8qXcRHwV^gcDsJB
20030693369906894882132773383
hostname.local@20030693377466640857723259057
uKVcmRKRfT
32
184
```



## Helpers



### ConcurrentHashSet

支持并发的哈希集合类型。



**示例：** 

```java
Set<String> set = new ConcurrentHashSet<>();
```



### ConsoleCmdExecutor

控制台命令执行器。



**示例：** 执行命令

```java
String output = ConsoleCmdExecutor.exec("mvn", "-version");
System.out.println(output);
```



**执行结果：** 

```shell
Picked up JAVA_TOOL_OPTIONS: -Dfile.encoding=UTF-8
Apache Maven 3.1.1 (0728685237757ffbf44136acec0402957f723d9a; 2013-09-17 23:22:22+0800)
Maven home: /Users/....../Java/apache-maven-3.1.1
Java version: 1.8.0_271, vendor: Oracle Corporation
Java home: /Library/Java/JavaVirtualMachines/jdk1.8.0_271.jdk/Contents/Home/jre
Default locale: zh_CN, platform encoding: UTF-8
OS name: "mac os x", version: "10.15.7", arch: "x86_64", family: "mac"
```



**示例：** 执行命令并自定义处理控制台输出（输出结果与上例一致）

```java
String[] cmd = new String[]{"mvn", "-version"};
String output = ConsoleCmdExecutor.exec(cmd, new ICmdOutputHandler<String>() {
    @Override
    public String handle(BufferedReader reader) throws Exception {
        return reader.lines().map(line -> line + "\r\n").collect(Collectors.joining());
    }
});
System.out.println(output);
```



### ConsoleTableBuilder

控制台表格构建工具，同时支持 CSV 和 Markdown 格式。



**示例：** 

```java
// 创建表格构建工具实例，并指定表格列数
ConsoleTableBuilder tableBuilder = ConsoleTableBuilder.create(3)
    // 开启转义
    ..escape()
    // 添加一行
    .addRow()
    // 添加当前行各列
    .addColumn("No.")
    .addColumn("Name")
    .addColumn("Top N").builder();
// 添加各行数据
tableBuilder.addRow().addColumn("1").addColumn("Java").addColumn("1");
tableBuilder.addRow().addColumn("2").addColumn("C++").addColumn("2");
// 以下是为便于示例展示，请在实际使用时，在构建工具实例时首先指定表格输出格式！！
// 以控制台表格输出
System.out.println(tableBuilder);
// 以 CSV 格式输出
System.out.println(tableBuilder.csv());
// 以 Markdown 格式输出
System.out.println(tableBuilder.markdown());
// 输出内容到指定文件
tableBuilder.writeTo(new FileOutputStream("/Temp/table.txt"), "UTF-8");
```



**执行结果：** 

```shell
+-----+------+-------+
| No. | Name | Top N |
+-----+------+-------+
| 1   | Java | 1     |
| 2   | C++  | 2     |
+-----+------+-------+

No.,Name,Top N
1,Java,1
2,C++,2

|No.|Name|Top N|
|---|---|---|
|1|Java|1|
|2|C++|2|
```



### DateTimeHelper

Date（日期）类型数据处理相关的函数工具集合。



**示例：** 

```java
// 构造实例对象的方式：
DateTimeHelper dateTimeHelper = DateTimeHelper.now();
// dateTimeHelper = DateTimeHelper.bind(new Date());
// dateTimeHelper = DateTimeHelper.bind(System.currentTimeMillis());
// dateTimeHelper = DateTimeHelper.bind("2021-10-26 13:52", DateTimeUtils.YYYY_MM_DD_HH_MM);
// 获取当前 Date 对象
Date date = dateTimeHelper.time();
// 获取当前日期时间的 UTC 值
int timeUTC = dateTimeHelper.timeUTC();
// 获取当前日期时间的毫秒值
long timeMillis = dateTimeHelper.timeMillis();
// 获取当前时区设置
TimeZone timeZone = dateTimeHelper.timeZone();
// 设置日期时间和时区设置值
dateTimeHelper.time(new Date());
dateTimeHelper.timeUTC(System.currentTimeMillis() / 1000);
dateTimeHelper.timeMillis(System.currentTimeMillis());
dateTimeHelper.timeZone(DateTimeUtils.getTimeZone("8"));
// 获取当前年、月、日、时、分、秒、毫秒值
dateTimeHelper.year();
dateTimeHelper.month();
dateTimeHelper.day();
dateTimeHelper.hour();
dateTimeHelper.minute();
dateTimeHelper.second();
dateTimeHelper.millisecond();
// 设置年、月、日、时、分、秒、毫秒值
dateTimeHelper.year(2021);
dateTimeHelper.month(10);
dateTimeHelper.day(26);
dateTimeHelper.hour(13);
dateTimeHelper.minute(52);
dateTimeHelper.second(0);
dateTimeHelper.millisecond(0);
// 日期年、月、日、时、分、秒、毫秒值的加、减法运算
dateTimeHelper.yearsAdd(1);
dateTimeHelper.monthsAdd(-1);
dateTimeHelper.daysAdd(1);
dateTimeHelper.hoursAdd(-1);
dateTimeHelper.minutesAdd(1);
dateTimeHelper.secondsAdd(-1);
dateTimeHelper.millisecondsAdd(1);
// 获取当前日期是当前周的第几天
dateTimeHelper.dayOfWeek();
// 获取当前日期是当前月的第几周
dateTimeHelper.dayOfWeekInMonth();
// 获取当前月有多少天
dateTimeHelper.daysOfMonth();
// 获取当前月有几周
dateTimeHelper.weekOfMonth();
// 获取当前周是当前年的第几周
dateTimeHelper.weekOfYear();
// 判断当前年是否为闰年
dateTimeHelper.isLeapYear();
// 调整当前时间到当天的开始，即：00:00:00:000
dateTimeHelper.toDayStart();
// 调整当前时间到当天的结束，即：59:59:59:999
dateTimeHelper.toDayEnd();
// 调整当前日期至所在周的周一当天
dateTimeHelper.toWeekStart();
// 调整当前日期至所在周的周日当天
dateTimeHelper.toWeekEnd();
// 两日期时间相减计算相差毫秒值
long result = dateTimeHelper.subtract(new Date());
result = dateTimeHelper.subtract(DateTimeHelper.now());
```



### ExcelFileAnalysisHelper

Excel 文件数据导入助手类，用于辅助操作和分析 Excel 文件中各 Sheet 页的表格数据，使用时需在工程中引入以下依赖包：

```xml
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.2</version>
</dependency>
```



**示例：** 展示如何读取 Excel 文件中的 Sheet 页数据并以表格形式输出到控制台

```java
public class Demo {

    public static void outputSheet(File excelFile) throws Exception {
        try (ExcelFileAnalysisHelper analysisHelper = ExcelFileAnalysisHelper.bind(excelFile)) {
            // 遍历 Excel 文件中全部 Sheet 页名称
            for (String sheetName : analysisHelper.getSheetNames()) {
                // 读取指定名称的 Sheet 页数据
                List<Object[]> results = analysisHelper.openSheet(sheetName);
                if (!results.isEmpty()) {
                    // 默认第一行为表格头
                    Object[] firstRow = results.get(0);
                    if (firstRow != null && firstRow.length > 0) {
                        // 构建控制台表格对象
                        ConsoleTableBuilder tableBuilder = ConsoleTableBuilder.create(firstRow.length).escape();
                        // 添加表格头各列信息
                        ConsoleTableBuilder.Row header = tableBuilder.addRow();
                        Arrays.stream(firstRow).map(o -> ((Object[]) o)[0]).map(Object::toString).forEach(header::addColumn);
                        // 遍历表格各行数据
                        results.forEach(row -> {
                            ConsoleTableBuilder.Row newRow = tableBuilder.addRow();
                            // 遍历行各列数据
                            Arrays.stream(row).map(column -> BlurObject.bind(((Object[]) column)[1]).toStringValue()).forEach(newRow::addColumn);
                        });
                        // 输出表格到控制台
                        System.out.println(tableBuilder);
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        outputSheet(new File(RuntimeUtils.replaceEnvVariable("${root}/demo.xlsx")));
    }
}
```



### ExcelFileExportHelper

数据导出 Excel 文件助手类，支持导出 CSV 和 Excel 格式文件，其中 Excel 格式文件的导出分别采用 POI 和 JXLS 模板两种方式，在使用时需在工程中引入对应的依赖包，如下所示：

```xml
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.2</version>
</dependency>
<dependency>
    <groupId>org.jxls</groupId>
    <artifactId>jxls</artifactId>
    <version>2.11.0</version>
</dependency>
```



**示例：** 通过 POI 导出数据到 Excel 文件

```java
public class Demo {

    public static void outputSheet(File excelFile) throws Exception {
        // 此处省略上例中的代码部份......用于控制台输出 Excel 表格内容
    }

    // 自定义单元格内容渲染器
    public static class IdCardRender implements IExportDataRender {

        @Override
        public String render(ExportColumn column, String fieldName, Object value) throws Exception {
            if (fieldName.equalsIgnoreCase("idCard") && value != null) {
                String idCard = BlurObject.bind(value).toStringValue();
                if (StringUtils.isNotBlank(idCard)) {
                    // 本渲染器目的是隐藏身份证件中的敏感数据
                    return StringUtils.rightPad(StringUtils.substring(idCard, 0, 6), 14, '*') + StringUtils.substring(idCard, 14);
                }
            }
            return null;
        }
    }

    // 导出数据的结构及规则类定义
    public static class User {

        @ExportColumn(value = "Name")
        private String name;

        @ExportColumn(value = "Age")
        private Integer age;

        @ExportColumn(value = "Gender", dataRange = {"UNKNOWN", "M", "F"})
        private Integer gender;

        @ExportColumn(value = "ID Card", render = IdCardRender.class)
        private String idCard;

        @ExportColumn(value = "Birthday", dateTime = true)
        private Long createTime;

        @ExportColumn(excluded = true)
        private Long lastModifyTime;

        //
        // 此处省略了Get/Set方法
        //
    }

    public static void main(String[] args) throws Exception {
        // 导出数据到临时 Excel 文件（默认为CSV格式）
        // 当导出的数据文件数量大于1时将返回 .zip 文件，否则返回 .xlsx 或 .csv 文件
        File exportFile = ExcelFileExportHelper.bind(new IExportDataProcessor() {
            @Override
            public List<?> getData(int index) throws Exception {
                // index 表示当前导出文件的序号（从1开始）
                if (index == 1) {
                    // 本例仅导出单个文件
                    List<User> users = new ArrayList<>();
                    User user = new User();
                    user.setName("suninformation");
                    user.setAge(18);
                    user.setGender(1);
                    user.setIdCard("123456789012345678");
                    user.setCreateTime(System.currentTimeMillis());
                    users.add(user);
                    //
                    return users;
                }
                return null;
            }
        }).export(User.class, true);
        // 将导出的 Excel 文件内容以表格形式输出到控制台
        outputSheet(exportFile);
    }
}
```



**执行结果：** 

```shell
+----------------+-----+--------+--------------------+---------------------+
| Name           | Age | Gender | ID Card            | Birthday            |
+----------------+-----+--------+--------------------+---------------------+
| suninformation | 18  | M      | 123456********5678 | 2021-10-26 20:29:45 |
+----------------+-----+--------+--------------------+---------------------+
```



**示例：** 通过 JXLS 模板导出数据到 Excel 文件

:::tip 注意：

此方式不支持上例中的 @ExportColumn 注解，须提前定义模板文件，关于更多 JXLS 的使用方法，请访问：

[GitHub - jxlsteam/jxls: Java library for creating Excel reports using Excel templates](https://github.com/jxlsteam/jxls)

:::



```java
public static void main(String[] args) throws Exception {
    // 注意：模板文件路径不能包含扩展名！
    // （模板文件的扩展名必须是.xls且必须放置在类路径下）
    // 传递数据方式一：
    Map<String, Object> data = new HashMap<>();
    // ......
    File exportFile = ExcelFileExportHelper.bind(data).export("tmpl/demo");
    // 传递数据方式二：
    exportFile = ExcelFileExportHelper.bind(index -> {
        if (index == 1) {
            List<User> users = new ArrayList<>();
            // ......
            return users;
        }
        return null;
    }).export("tmpl/demo");
    //
    outputSheet(exportFile);
}
```



### ExecutableQueue

可执行队列服务。



**示例：** 综合展示如何使用可执行队列服务

```java
public class CustomMessageQueue extends ExecutableQueue<CustomMessageQueue.CustomMessage> 
    implements ExecutableQueue.IListener<CustomMessageQueue.CustomMessage> {

    private final List<IFilter<CustomMessage>> filters = new ArrayList<>();

    @Override
    public List<IFilter<CustomMessage>> getFilters() {
        return filters;
    }

    @Override
    public void listen(CustomMessage element) {
        // 仅打印消息对象到控制台
        System.out.println(element);
    }

    @Override
    protected void onListenStarted() {
        System.out.println("重写此方法用于处理队列监听服务启动事件。");
    }

    @Override
    protected void onListenStopped() {
        System.out.println("重写此方法用于处理队列监听服务停止事件。");
    }

    @Override
    protected void onListenerAdded(String id, IListener<CustomMessage> listener) {
        System.out.println("重写此方法用于处理监听器注册成功事件。");
    }

    @Override
    protected void onListenerRemoved(String id, IListener<CustomMessage> listener) {
        System.out.println("重写此方法用于处理监听器移除成功事件。");
    }

    @Override
    protected void onElementAdded(CustomMessage element) {
        System.out.println("重写此方法用于处理元素被成功推送至队列事件。");
    }

    @Override
    protected void onElementAbandoned(CustomMessage element) {
        System.out.println("重写此方法用于处理队列元素被丢弃事件。");
    }

    @Override
    protected void onSpeedometerListen(long speed, long averageSpeed, long maxSpeed, long minSpeed) {
      System.out.println("重写此方法用于处理速度计数器监听数据。");
    }

    @Override
    protected void doSpeedometerStart(Speedometer speedometer) {
      // 重定此方法用于设置速度计数器配置参数
      super.doSpeedometerStart(speedometer.interval(2));
    }

    /**
     * 自定义消息
     */
    public static class CustomMessage implements Serializable {

        /**
         * 消息唯一标识
         */
        private String id;

        /**
         * 消息类型
         */
        private String type;

        /**
         * 消息内容
         */
        private String content;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        @Override
        public String toString() {
            return String.format("CustomMessage{id='%s', type='%s', content='%s'}", id, type, content);
        }
    }

    public CustomMessageQueue() {
        // 可根据以下构造参数自定义：
        // prefix                   – 队列名称前缀
        // minPoolSize              – 线程池初始大小
        // maxPoolSize              – 最大线程数
        // workQueueSize            – 工作队列大小
        // queueTimeout             – 队列等待超时时间(秒), 默认30秒
        // queueSize                – 队列大小
        // concurrentCount          – 并发数量
        // rejectedExecutionHandler – 拒绝策略
        super("message");
        // 注册自定义消息监听器
        addListener(this);
        // 注册消息过滤器
        filters.add(new IFilter<CustomMessage>() {
            @Override
            public boolean filter(CustomMessage element) {
                // 过滤掉所有类型为 info 的消息
                return element.getType().equalsIgnoreCase("info");
            }
        });
    }

    public static void main(String[] args) throws Exception {
        try (CustomMessageQueue queue = new CustomMessageQueue()) {
            // 启动队列监听服务
            queue.listenStart();
            // 定义一个类型为 warn 的消息对象
            CustomMessage message = new CustomMessage();
            message.setId(UUIDUtils.UUID());
            message.setType("warn");
            message.setContent("WARN: 警告信息");
            // 将消息推送到队列
            queue.putElement(message);
            // 定义一个类型为 info 的消息对象
            message = new CustomMessage();
            message.setId(UUIDUtils.UUID());
            message.setType("info");
            message.setContent("INFO: 此条信息将被过滤");
            // 将消息推送到队列
            queue.putElement(message);
            //
            try {
              queue.putElement(queue.execute(() -> {
                CustomMessage msg = new CustomMessage();
                msg.setId(UUIDUtils.UUID());
                msg.setType("warn");
                msg.setContent("通过FutureTask方式执行业务逻辑以获取消息对象，如：HTTP请求某接口、数据库中查询某数据等");
                return msg;
              }, 10));
              //
              queue.execute(Collections.singletonList(() -> {
                CustomMessage msg = new CustomMessage();
                msg.setId(UUIDUtils.UUID());
                msg.setType("warn");
                msg.setContent("批量获取消息对象");
                return msg;
              }));
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
              throw new RuntimeException(e);
            }
            //
            Thread.sleep(TimeUnit.SECONDS.toMillis(5));
            // 移除监听器
            queue.removeListener(CustomMessageQueue.class);
            // 手动停止队列服务
            queue.listenStop();
        }
    }
}
```



**执行结果：** 

```shell
重写此方法用于处理监听器注册成功事件。
重写此方法用于处理队列监听服务启动事件。
CustomMessage{id='d37f053f5dd145ad82bf2ec136f3335a', type='warn', content='WARN: 警告信息'}
重写此方法用于处理元素被成功推送至队列事件。
重写此方法用于处理队列元素被丢弃事件。
重写此方法用于处理元素被成功推送至队列事件。
CustomMessage{id='ee59790938d144f5866c058f944f2b2a', type='warn', content='通过FutureTask方式执行业务逻辑以获取消息对象，如：HTTP请求某接口、数据库中查询某数据等'}
CustomMessage{id='7cd1c917d6f94f24bf86e54bfcc9bfbd', type='warn', content='批量获取消息对象'}
重写此方法用于处理速度计数器监听数据。
重写此方法用于处理队列监听服务停止事件。
```



### FFmpegHelper

FFmpeg 音视频操作辅助工具类。

:::tip

关于更多 FFmpeg 的使用方法，请访问：[Documentation (ffmpeg.org)](https://ffmpeg.org/documentation.html)

:::



**示例：** 创建 FFmpeg 辅助类实例

```java
// 由于 FFmpeg 属于外部命令，需要提前下载并配置环境变量
// 1. 假定已经配置相关环境变量的情况下可以直接创建，它默认使用的命令是：ffmpeg
FFmpegHelper.create();
// 2. 在没有配置配置环境变量时，可以指定 ffmpeg 可执行文件的绝对路径：
FFmpegHelper.create("/opt/ffmpeg-4.4.1/ffmpeg")
```



**示例：** 音频文件的操作

```java
// 基准路径
File basePath = new File(RuntimeUtils.getRootPath());
// 创建 FFmpeg 辅助类实例
FFmpegHelper ffmpegHelper = FFmpegHelper.create()
    // 设置为不输出 FFmpeg 日志
    .writeLog(false)
    // 绑定目标音频文件
    .bind(basePath, "/太阳照常升起，让子弹飞.mp3");
// 提取音频文件媒体信息
FFmpegHelper.MediaInfo mediaInfo = ffmpegHelper.getMediaInfo();
// 音频编码格式：
System.out.println("Audio Encoding Format: " + mediaInfo.getAudioEncodingFormat());
// 音频采样率：
System.out.println("Audio Sampling Rate: " + mediaInfo.getAudioSamplingRate());
// 比特率：
System.out.println("Bit Rates: " + mediaInfo.getBitrates());
// 总时长：
System.out.println("Time: " + FFmpegHelper.buildTimeStr(mediaInfo.getTime()));
// 执行音频文件格式转换：
ffmpegHelper.convertAudio("aac", new File(basePath, "/audio.aac"));
```



**执行结果：** 

```shell
Audio Encoding Format: mp3
Audio Sampling Rate: 44100
Bit Rates: 160
Time: 0:0:42
```





**示例：** 视频文件的操作

```java
// 基准路径
File basePath = new File(RuntimeUtils.getRootPath());
// 创建 FFmpeg 辅助类实例
FFmpegHelper ffmpegHelper = FFmpegHelper.create()
    // 设置为不输出 FFmpeg 日志
    .writeLog(false)
    // 绑定目标视频文件
    .bind(new File(basePath, "/demo.mp4"));
// 提取视频文件媒体信息
FFmpegHelper.MediaInfo mediaInfo = ffmpegHelper.getMediaInfo();
// 音频编码格式：
System.out.println("Audio Encoding Format: " + mediaInfo.getAudioEncodingFormat());
// 音频采样率：
System.out.println("Audio Sampling Rate: " + mediaInfo.getAudioSamplingRate());
// 视频格式：
System.out.println("Video Format: " + mediaInfo.getVideoFormat());
// 比特率：
System.out.println("Bit Rates: " + mediaInfo.getBitrates());
// 分辨率：
System.out.println("Resolution: " + mediaInfo.getResolution());
// 图像宽度：
System.out.println("Image Width: " + mediaInfo.getImageWidth());
// 图像高度：
System.out.println("Image Height: " + mediaInfo.getImageHeight());
// 视频总时长
System.out.println("Time: " + FFmpegHelper.buildTimeStr(mediaInfo.getTime()));
// 截图（从第10秒开始截取2张，每秒一张，注意文件命名规则）
File screen = ffmpegHelper.screenshotVideo(10, mediaInfo.getImageWidth(), mediaInfo.getImageHeight(), 2, new File(basePath, "/screen-%03d.jpeg"));
// 裁剪视频文件（从第10秒开始截取30秒时长）
File video = ffmpegHelper.videoCut(10, 30, "mpeg4", "aac", new File(basePath, "/video.mp4"));
// 将裁剪的视频文件进行缩放
File videoScaleFile = FFmpegHelper.create().writeLog(false).bind(video).videoScale(mediaInfo.getImageWidth() / 2, mediaInfo.getImageHeight() / 2, new File(basePath, "/video_scale.mp4"));
// 为缩放的视频文件添加水印图片
ffmpegHelper = FFmpegHelper.create().writeLog(false).bind(videoScaleFile);
File videoWithLogoFile = ffmpegHelper.videoOverlayLogo(new File(basePath, "/logo.png"), false, new File(basePath, "/video_with_logo.mp4"));
// 将添加水印图片的视频文件转换为 flv 格式
ffmpegHelper = FFmpegHelper.create().writeLog(false).bind(videoWithLogoFile);
FFmpegHelper.MediaInfo videoInfo = ffmpegHelper.getMediaInfo();
ffmpegHelper.videoToFlv(videoInfo.getImageWidth(), videoInfo.getImageHeight(), new File(basePath, "/video.flv"));
```



**执行结果：** 

```shell
Audio Encoding Format: null
Audio Sampling Rate: null
Video Format: yuvj420p(pc)
Bit Rates: 244
Resolution: 2160x1080
Image Width: 2160
Image Height: 1080
Time: 0:17:56
```



### FreemarkerConfigBuilder

Freemarker 模板引擎配置构建工具类，使用与 Freemarker 相关的功能时，需在工程中引入以下依赖包：

```xml
<dependency>
    <groupId>org.freemarker</groupId>
    <artifactId>freemarker</artifactId>
    <version>2.3.31</version>
</dependency>
```



**示例：** 

```java
public class Demo {

    public static void main(String[] args) throws Exception {
        Configuration configuration = FreemarkerConfigBuilder.create()
                .addTemplateFileDir(new File(RuntimeUtils.getRootPath(), "/templates"))
                .addTemplateClass(Demo.class, "/templates")
                .addTemplateLoader(new MultiTemplateLoader(new TemplateLoader[]{}))
                .addTemplateSource("tmpl", "<#if (a > 1)>${a}<#else>a <= 1</#if>")
                .build();
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("a", 1);
            configuration.getTemplate("tmpl").process(dataModel, new BufferedWriter(new OutputStreamWriter(output)));
            System.out.println(output);
        }
    }
}
```



### MathCalcHelper

精确的数学计算工具类。



**示例：** 展示加、减、乘、除法的运算过程

```java
// 创建精确计算工具类实例，参与计算的类型可以是数字字符串，也可以是数值
String result = MathCalcHelper.bind("123.45678")
    // 设置保留2位小数
    .scale(2)
    // 加法
    .add(123.321)
    // 减法
    .subtract("56.78")
    // 乘法
    .multiply(123.45)
    // 除法
    .divide("12.34")
    // 四舍五入
    .round()
    // 将结果转换为模糊对象
    .toBlurObject()
    // 将结果输出为字符串
    .toStringValue();
System.out.println(result);
```



### QRCodeHelper

二维码生成工具类，使用时需在工程中引入以下依赖包：

```xml
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>core</artifactId>
    <version>3.4.1</version>
</dependency>
```



**示例：** 生成宽度和高度均为300像素的二维码并附加 LOGO 图片

```java
String basePath = RuntimeUtils.getRootPath();
// 创建二维码生成工具实例，设置二维码文字内容、图片宽和高及容错级别
QRCodeHelper.create("https://ymate.net/", 300, 300, ErrorCorrectionLevel.H)
    // 设置图片文件格式
    .setFormat("png")
    // 可以为二维码附加 LOGO 图片
    .setLogo(new File(basePath, "/logo.png"), 5, 3, Color.WHITE, Color.WHITE)
    // 将生成的二维码写入文件
    .writeToFile(new File(basePath, "/qrcode.png"));
```



### ReentrantLockHelper

重入锁辅助工具类。



**示例：** 

```java
public class Demo {

    public static void main(String[] args) throws Exception {
        // 特性一：
        // 构建重入锁辅助工具实例（ReentrantLockHelper.DEFAULT 为全局实例）
        ReentrantLockHelper lockHelper = ReentrantLockHelper.DEFAULT;
        // 或构建私有实例： lockHelper = new ReentrantLockHelper();
        // 获取指定名称的重入锁对象（若不存在则创建并返回）
        ReentrantLock lock = lockHelper.getLocker("customKey");
        try {
            // 加锁
            lock.lock();
            // ...... 具体业务逻辑
        } finally {
            // 解锁
            lock.unlock();
            // 或 ReentrantLockHelper.unlock(lock);
        }
        // 特性二：
        // 同步映射赋值，若指定的 key 不存在则创建，否则返回已存在的值
        Map<String, String> data = new HashMap<>();
        String value = ReentrantLockHelper.putIfAbsent(data, "key1", "value1");
        // 异步映射赋值，与同步相比，仅当 key 不存在时才会计算值
        value = ReentrantLockHelper.putIfAbsentAsync(data, "key1", new ReentrantLockHelper.ValueGetter<String>() {
            @Override
            public String getValue() throws Exception {
                return "value2";
            }
        });
        System.out.println(value);
    }
}
```



### Speedometer

速度计数器。



**示例：** 

```java
public class Demo {

    public static void main(String[] args) throws Exception {
        // 创建计数器实例
        Speedometer speedometer = new Speedometer("counter")
            // 设置监听器执行时间间隔为1秒
            .interval(1000)
            // 设置采样数据量为10
            .dataSize(10);
        // 启动计数器监听
        speedometer.start(new ISpeedListener() {
            @Override
            public void listen(long speed, long averageSpeed, long maxSpeed, long minSpeed) {
                // 输出当前、平均、最高和最低处理速度值
                System.out.printf("Speed: %d, Speed avg: %d, Speed max: %d, Speed min: %d%n", speed, averageSpeed, maxSpeed, minSpeed);
            }
        });
        // 模拟100次请求
        for (int i = 0; i < 100; i++) {
            // 触发计数器
            speedometer.touch();
            // 随机间隔
            Thread.sleep(UUIDUtils.randomInt(100, 500));
            if (i == 50) {
                // 重置计数器
                speedometer.reset();
            }
        }
        // 停止并关闭计数器监听服务
        speedometer.close();
    }
}
```



### StopWatcher

跑表辅助工具类，用于计算某业务处理过程所花费的时间。



**示例：** 

```java
public class Demo {

    public static void main(String[] args) throws Exception {
        // 构建带返回值的跑表辅助工具实例对象
        StopWatcher<String> stopWatcher = StopWatcher.watch(new Callable<String>() {
            @Override
            public String call() throws Exception {
                // 模拟业务处理时间：随机等待2-5秒
                new CountDownLatch(1).await(UUIDUtils.randomLong(2000, 5000), TimeUnit.MILLISECONDS);
                return "value";
            }
        });
        // 获取业务返回值
        System.out.printf("Result: %s%n", stopWatcher.getValue());
        // 获取业务处理总耗时
        System.out.printf("Total elapsed time: %d%n", stopWatcher.getStopWatch().getTime());
    }
}
```



### XPathHelper

XPath 辅助工具类。



**示例一：** 基本操作演示

```java
public class Demo {

    public static void main(String[] args) throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<xml>" +
                "    <category name=\"default\">" +
                "        <property name=\"key1\">value1</property>" +
                "        <property name=\"key2\">value2</property>" +
                "    </category>" +
                "</xml>";
        Document document = XPathHelper.newDocumentBuilderFactory()
                .newDocumentBuilder()
                .parse(new InputSource(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))));
        XPathHelper xPathHelper = XPathHelper.create(XPathHelper.newXPathFactory(), document);
        System.out.println(xPathHelper.getStringValue("/xml//category/@name"));
        NodeList nodeList = xPathHelper.getNodeList("/xml//category//property");
        for (int idx = 0; idx < nodeList.getLength(); idx++) {
            Node item = nodeList.item(idx);
            String name = xPathHelper.getStringValue(item, "./@name");
            String value = xPathHelper.getStringValue(item, "./@value");
            if (StringUtils.isBlank(value)) {
                value = xPathHelper.getStringValue(item, ".");
            }
            System.out.printf("Name: %s, Value: %s%n", name, value);
        }
    }
}
```



**示例二：** 直接将 XML 转换为类对象

```java
public class Main {

    @XPathNode("xml")
    public static class Config {

        @XPathNode(value = "//category", child = true)
        private Category[] categories;

        //
        // 此处省略了Get/Set方法
        //
    }

    public static class Category {

        @XPathNode("@name")
        private String name;

        @XPathNode(value = "//property", child = true)
        private Property[] properties;

        //
        // 此处省略了Get/Set方法
        //
    }

    public static class Property {

        @XPathNode("@name")
        private String key;

        @XPathNode(".")
        private String value;

        //
        // 此处省略了Get/Set方法
        //
    }

    public static void main(String[] args) throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<xml>" +
                "    <category name=\"default\">" +
                "        <property name=\"key1\">value1</property>" +
                "        <property name=\"key2\">value2</property>" +
                "    </category>" +
                "</xml>";
        Config config = XPathHelper.Builder.create().build(xml).toObject(Config.class);
        System.out.println(JsonWrapper.toJsonString(config, true, true));
    }
}
```



**执行结果：** 

```json
{
	"categories":[{
		"name":"default",
		"properties":[{
			"key":"key1",
			"value":"value1"
		},{
			"key":"key2",
			"value":"value2"
		}]
	}]
}
```



### XStreamHelper

XStream 辅助构建工具类，使用时需在工程中引入以下依赖包：

```xml
<dependency>
    <groupId>com.thoughtworks.xstream</groupId>
    <artifactId>xstream</artifactId>
    <version>1.4.19</version>
</dependency>
```



**示例：** 

```java
public class Demo {

    public static void main(String[] args) throws Exception {
        // 创建 XStream 实例对象并指定是否支持 CDATA 标签
        XStream xStream = XStreamHelper.createXStream(true);
        // 或采用自定义过滤属性对 CDATA 标签的支持
        xStream = XStreamHelper.createXStream(true, new XStreamHelper.INodeFilter() {
            @Override
            public boolean doFilter(String name) {
                // 用于自定义过滤哪些属性需要使用 <![CDATA[...]]> 包裹
                return StringUtils.equalsIgnoreCase(name, "content");
            }
        });
        // ......
    }
}
```

