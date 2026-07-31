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
    <version>2.1.4-dev</version>
</dependency>
```



## 基础类型（Lang）

提供了针对任意对象之间的类型转换、组合和无限层级树型结构的支持。



### 模糊对象（BlurObject）

`BlurObject` 是一个用于任意类型对象之间转换的工具类，基本涵盖日常使用的数据类型。通过 `IConverter` 接口可以实现自定义转换器，并支持手动注册或 `SPI` 机制自动注册。

#### 核心特性

- **类型安全转换**：支持各种基本数据类型、包装类型、集合类型之间的自动转换
- **智能类型推断**：自动识别源类型并进行相应的转换处理
- **可扩展性**：通过 `IConverter` 接口支持自定义类型转换
- **SPI 自动注册**：支持通过 SPI 机制自动发现和注册转换器

#### 创建 BlurObject 实例

```java
// 使用静态 bind 方法创建
BlurObject blurObject = BlurObject.bind("123.4");

// 使用构造方法创建
BlurObject blurObject = new BlurObject(123);

// 绑定 null 值
BlurObject blurObject = BlurObject.bind(null);
```

#### 基本数据类型转换

`BlurObject` 提供了丰富的方法用于基本数据类型转换：

| 方法 | 返回类型 | 说明 |
|------|----------|------|
| `toBoolean()` / `toBooleanValue()` | `Boolean` / `boolean` | 转换为布尔值 |
| `toInteger()` / `toIntValue()` | `Integer` / `int` | 转换为整数 |
| `toLong()` / `toLongValue()` | `Long` / `long` | 转换为长整型 |
| `toFloat()` / `toFloatValue()` | `Float` / `float` | 转换为浮点数 |
| `toDouble()` / `toDoubleValue()` | `Double` / `double` | 转换为双精度浮点数 |
| `toShort()` / `toShortValue()` | `Short` / `short` | 转换为短整型 |
| `toByte()` / `toByteValue()` | `Byte` / `byte` | 转换为字节 |
| `toStringValue()` | `String` | 转换为字符串 |
| `toCharValue()` | `char` | 转换为字符 |

**示例：** 基本数据类型转换

```java
Object targetObj = "123.4";
BlurObject blurObject = BlurObject.bind(targetObj);

// 转换为整数（自动解析字符串）
int intValue = blurObject.toIntValue();           // 123

// 转换为双精度浮点数
double doubleValue = blurObject.toDoubleValue();  // 123.4

// 转换为浮点数
float floatValue = blurObject.toFloatValue();     // 123.4f

// 转换为字符串
String stringValue = blurObject.toStringValue();  // "123.4"

// 转换为布尔值
BlurObject boolObj = BlurObject.bind("true");
boolean boolValue = boolObj.toBooleanValue();     // true
```

#### 集合类型转换

`BlurObject` 支持将对象转换为各种集合类型：

| 方法 | 返回类型 | 说明 |
|------|----------|------|
| `toMapValue()` | `Map<?, ?>` | 转换为 Map |
| `toListValue()` | `List<?>` | 转换为 List |
| `toSetValue()` | `Set<?>` | 转换为 Set |

**示例：** 集合类型转换

```java
// Map 转换
Map<String, Object> map = new HashMap<>();
map.put("key1", "value1");
map.put("key2", 123);
BlurObject blurObject = BlurObject.bind(map);
Map<?, ?> resultMap = blurObject.toMapValue();

// List 转换
List<String> list = Arrays.asList("a", "b", "c");
BlurObject blurObject = BlurObject.bind(list);
List<?> resultList = blurObject.toListValue();

// 非集合类型会自动包装为单元素集合
BlurObject singleObj = BlurObject.bind("single");
List<?> singleList = singleObj.toListValue();  // ["single"]
```

#### 字节数组转换

`BlurObject` 支持字节数组和包装类型的转换：

| 方法 | 返回类型 | 说明 |
|------|----------|------|
| `toBytes()` | `Byte[]` | 转换为 Byte 数组（包装类型） |
| `toBytesValue()` | `byte[]` | 转换为 byte 数组（基本类型） |

**示例：** 字节数组转换

```java
// byte[] 转换
byte[] bytes = {1, 2, 3, 4, 5};
BlurObject blurObject = BlurObject.bind(bytes);
byte[] result = blurObject.toBytesValue();

// Byte[] 转换
Byte[] byteObjects = {1, 2, 3, 4, 5};
BlurObject blurObject = BlurObject.bind(byteObjects);
byte[] result = blurObject.toBytesValue();
```

#### 指定类型转换

使用 `toObjectValue(Class<?> clazz)` 方法可以将对象转换为指定类型：

```java
BlurObject blurObject = BlurObject.bind("123");

// 转换为指定类型
Integer intObj = (Integer) blurObject.toObjectValue(Integer.class);
Double doubleObj = (Double) blurObject.toObjectValue(Double.class);
String strObj = (String) blurObject.toObjectValue(String.class);
```

#### 自定义类型转换器

通过实现 `IConverter` 接口并配合 `@Converter` 注解，可以创建自定义类型转换器。

**示例：** 自定义类型转换器的注册与使用

```java
// 自定义类型转换器，通过注解明确从...到...类型之间的转换
// 此类可以通过 SPI 方式被自动注册
@Converter(from = {java.util.Date.class}, to = java.sql.Date.class)
public class DateConverter implements IConverter<java.sql.Date> {

    @Override
    public java.sql.Date convert(Object target) {
        return new java.sql.Date(((java.util.Date) target).getTime());
    }
}

// 手动注册转换器
BlurObject.registerConverter(java.util.Date.class, java.sql.Date.class, new DateConverter());

// 执行转换
java.util.Date date = new java.util.Date();
java.sql.Date sqlDate = BlurObject.bind(date).toObjectValue(java.sql.Date.class);
```

#### SPI 自动注册

转换器可以通过 SPI 机制自动注册，只需在 `META-INF/services/net.ymate.platform.commons.lang.IConverter` 文件中添加转换器类的全限定名：

```
com.example.converter.DateConverter
com.example.converter.TimestampConverter
```

#### 特殊类型处理

`BlurObject` 对一些特殊类型提供了专门的处理：

| 源类型 | 处理方式 |
|--------|----------|
| `Clob` | 读取字符流并转换为字符串 |
| `Blob` | 读取二进制流并转换为字节数组 |
| `Date` / `Calendar` | 转换为时间戳（Long） |
| `LocalDate` / `LocalDateTime` / `ZonedDateTime` | 转换为时间戳（Long） |
| 枚举类型 | 通过名称匹配进行转换 |
| `Object[]` | 使用 `\|` 连接为字符串 |

**示例：** 日期时间转换

```java
// Date 转换为 Long（时间戳）
java.util.Date date = new java.util.Date();
BlurObject blurObject = BlurObject.bind(date);
long timestamp = blurObject.toLongValue();

// LocalDateTime 转换为 Long
LocalDateTime localDateTime = LocalDateTime.now();
BlurObject blurObject = BlurObject.bind(localDateTime);
long timestamp = blurObject.toLongValue();
```

#### 最佳实践

1. **优先使用带默认值的方法**：如 `toIntValue()`、`toBooleanValue()` 等，避免处理 null 值时的空指针异常

2. **合理使用类型转换**：了解各种类型之间的转换规则，避免精度丢失或数据截断

3. **自定义转换器**：对于复杂的类型转换需求，建议实现 `IConverter` 接口进行封装

4. **SPI 自动注册**：对于通用的转换器，使用 SPI 机制自动注册，减少手动配置



### 结对对象（PairObject）

`PairObject<K, V>` 是一个通用的键值对容器类，用于将任意两种类型的对象以 `<K, V>` 的形式组合在一起。它实现了 `Serializable` 接口，支持序列化操作。

#### 核心特性

- **泛型支持**：支持任意类型的键和值组合
- **链式调用**：setter 方法支持链式调用，方便构建对象
- **空值检查**：提供便捷的空值检查方法
- **序列化支持**：实现 `Serializable` 接口，支持对象序列化

#### 创建 PairObject 实例

`PairObject` 提供了多种创建实例的方式：

| 方法 | 说明 |
|------|------|
| `PairObject()` | 默认构造方法，创建空的结对对象 |
| `PairObject(K key)` | 使用键创建结对对象，值为 null |
| `PairObject(K key, V value)` | 使用键和值创建结对对象 |
| `PairObject.bind(K key)` | 静态方法，使用键创建结对对象 |
| `PairObject.bind(K key, V value)` | 静态方法，使用键和值创建结对对象 |

**示例：** 创建结对对象

```java
// 使用静态 bind 方法创建（推荐）
PairObject<String, Integer> pairObject1 = PairObject.bind("suninformation", 18);

// 使用构造方法创建
PairObject<String, Integer> pairObject2 = new PairObject<>("suninformation", 18);

// 只设置键，值为 null
PairObject<String, Integer> pairObject3 = PairObject.bind("key");

// 使用默认构造方法，然后通过 setter 设置
PairObject<String, Integer> pairObject4 = new PairObject<>();
pairObject4.setKey("suninformation").setValue(18);
```

#### 常用方法

| 方法 | 返回类型 | 说明 |
|------|----------|------|
| `getKey()` | `K` | 获取键 |
| `setKey(K key)` | `PairObject<K, V>` | 设置键，支持链式调用 |
| `getValue()` | `V` | 获取值 |
| `setValue(V value)` | `PairObject<K, V>` | 设置值，支持链式调用 |
| `isEmpty()` | `boolean` | 检查键和值是否都为 null |
| `isAnyEmpty()` | `boolean` | 检查键或值是否为 null |

#### 使用示例

**示例一：** 基本类型结对

```java
String name = "suninformation";
int age = 18;
PairObject<String, Integer> pairObject = PairObject.bind(name, age);

// 获取键和值
String key = pairObject.getKey();      // "suninformation"
Integer value = pairObject.getValue(); // 18

// 检查是否为空
boolean empty = pairObject.isEmpty();      // false
boolean anyEmpty = pairObject.isAnyEmpty(); // false
```

**示例二：** 复杂类型结对

```java
// 使用集合类型作为键和值
List<String> keyList = new ArrayList<>();
keyList.add("item1");
keyList.add("item2");

Map<String, String> valueMap = new HashMap<>();
valueMap.put("key1", "value1");
valueMap.put("key2", "value2");

PairObject<List<String>, Map<String, String>> pairObject =
    new PairObject<>(keyList, valueMap);

// 获取键和值
List<String> keys = pairObject.getKey();
Map<String, String> values = pairObject.getValue();
```

**示例三：** 链式调用

```java
PairObject<String, Integer> pairObject = new PairObject<>()
    .setKey("age")
    .setValue(25);

// 修改值
pairObject.setValue(26);
```

**示例四：** 空值检查

```java
// 创建空的结对对象
PairObject<String, Integer> emptyPair = new PairObject<>();

// 检查是否为空
boolean isEmpty = emptyPair.isEmpty();      // true
boolean isAnyEmpty = emptyPair.isAnyEmpty(); // true

// 创建只有键的结对对象
PairObject<String, Integer> keyOnlyPair = PairObject.bind("key");
boolean isEmpty2 = keyOnlyPair.isEmpty();      // false（键不为null）
boolean isAnyEmpty2 = keyOnlyPair.isAnyEmpty(); // true（值为null）
```

**示例五：** 在集合中使用

```java
// 创建结对对象列表
List<PairObject<String, Integer>> pairList = new ArrayList<>();

pairList.add(PairObject.bind("Alice", 25));
pairList.add(PairObject.bind("Bob", 30));
pairList.add(PairObject.bind("Charlie", 35));

// 遍历并输出
for (PairObject<String, Integer> pair : pairList) {
    System.out.println(pair.getKey() + ": " + pair.getValue());
}

// 使用 Map 存储结对对象
Map<String, PairObject<String, Integer>> pairMap = new HashMap<>();
pairMap.put("user1", PairObject.bind("Alice", 25));
pairMap.put("user2", PairObject.bind("Bob", 30));
```

#### 最佳实践

1. **优先使用静态 bind 方法**：代码更简洁，意图更明确

2. **使用泛型确保类型安全**：在创建 `PairObject` 时明确指定键和值的类型

3. **合理使用空值检查**：在获取值之前使用 `isEmpty()` 或 `isAnyEmpty()` 进行检查

4. **链式调用简化代码**：在需要连续设置多个属性时使用链式调用

5. **作为方法返回值**：当方法需要返回两个相关联的值时，可以使用 `PairObject` 作为返回类型

```java
// 作为方法返回值
public PairObject<String, Integer> getUserInfo() {
    String name = getUserName();
    int age = getUserAge();
    return PairObject.bind(name, age);
}
```



### 树型对象（TreeObject）

使用级联方式存储各种数据类型，不限层级深度。

TreeObject 是一个功能强大的树形数据结构，支持以下特性：
- 支持三种存储模式：值模式、映射模式和数组集合模式
- 支持多种数据类型，包括基本类型、字符串、数组、集合等
- 支持 JSON 和 XML 的序列化与反序列化
- 提供丰富的类型转换和取值方法
- 支持通过默认值避免空指针异常


#### 存储模式

TreeObject 支持三种存储模式：

| 常量 | 值 | 说明 |
|------|-----|------|
| MODE_VALUE | 1 | 值模式，用于存储单个值 |
| MODE_MAP | 2 | 映射模式，用于存储键值对 |
| MODE_ARRAY | 3 | 数组集合模式，用于存储有序列表 |


#### 数据类型常量

TreeObject 支持以下数据类型常量：

| 常量 | 值 | 说明 |
|------|-----|------|
| TYPE_NULL | 0 | NULL 类型 |
| TYPE_INTEGER | 1 | Integer 类型 |
| TYPE_MIX_STRING | 2 | 混合 String 类型（通过 Base64 编码的字符串） |
| TYPE_STRING | 3 | String 类型 |
| TYPE_LONG | 4 | Long 类型 |
| TYPE_TIME | 5 | Time 类型（UTC 时间） |
| TYPE_BOOLEAN | 6 | Boolean 类型 |
| TYPE_FLOAT | 7 | Float 类型 |
| TYPE_DOUBLE | 8 | Double 类型 |
| TYPE_MAP | 9 | Map&lt;String, ? extends Object&gt; 类型 |
| TYPE_COLLECTION | 10 | Collection&lt;? extends Object&gt; 类型 |
| TYPE_BYTE | 11 | Byte 类型 |
| TYPE_CHAR | 12 | Character 类型 |
| TYPE_SHORT | 13 | Short 类型 |
| TYPE_BYTES | 14 | byte[] 类型 |
| TYPE_OBJECT | 15 | Object 类型 |
| TYPE_UNKNOWN | 99 | 未知类型 |
| TYPE_TREE_OBJECT | 100 | 树对象类型 |


**示例：** 基本使用

```java
public class TreeObjectTest {
    public static void main(String[] args) {
        // 创建一个映射模式的 TreeObject
        TreeObject treeObject = new TreeObject()
                .put("id", UUIDUtils.UUID())
                .put("category", new Byte[]{1, 2, 3, 4})
                .put("create_time", System.currentTimeMillis(), true)
                .put("is_locked", true)
                .put("detail", new TreeObject()
                        .put("real_name", "汉字将被混淆", true)
                        .put("age", 32));

        // 创建一个数组集合模式的 TreeObject
        TreeObject list = new TreeObject()
                .add("item1")
                .add("item2");

        // 创建另一个映射模式的 TreeObject
        TreeObject map = new TreeObject()
                .put("key1", "value1")
                .put("key2", "value2");

        // 组合多个 TreeObject
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
    }
}
```

**执行结果：**

```shell
ids: [item1, item2]
maps: {key1=value1, key2=value2}
real_name: 汉字将被混淆
```


**示例：** 使用类型转换方法

```java
TreeObject treeObject = new TreeObject()
        .put("count", 100)
        .put("price", 99.99)
        .put("enabled", true)
        .put("timestamp", System.currentTimeMillis(), true);

int count = treeObject.getInt("count");
double price = treeObject.getDouble("price");
boolean enabled = treeObject.getBoolean("enabled");
long timestamp = treeObject.getTime("timestamp");

System.out.println("Count: " + count);
System.out.println("Price: " + price);
System.out.println("Enabled: " + enabled);
System.out.println("Timestamp: " + timestamp);
```


**示例：** 使用默认值避免空指针异常

```java
TreeObject treeObject = new TreeObject()
        .put("name", "test");

String name = treeObject.getString("name", "default_name");
int age = treeObject.getInt("age", 18);
boolean active = treeObject.getBoolean("active", false);

System.out.println("Name: " + name);      // 输出: test
System.out.println("Age: " + age);          // 输出: 18 (默认值)
System.out.println("Active: " + active);   // 输出: false (默认值)
```


**示例：** 判断数据类型和模式

```java
TreeObject treeObject = new TreeObject()
        .put("id", 123)
        .put("list", new TreeObject().add("a").add("b"));

TreeObject idNode = treeObject.get("id");
if (idNode.isValue()) {
    System.out.println("id is value mode, type: " + idNode.getType());
}

TreeObject listNode = treeObject.get("list");
if (listNode.isList()) {
    System.out.println("list is array mode");
}

if (treeObject.isMap()) {
    System.out.println("treeObject is map mode");
}
```


**示例：** JSON 序列化与反序列化

```java
public class TreeObjectJsonTest {
    public static void main(String[] args) {
        TreeObject treeObject = new TreeObject()
                .put("id", UUIDUtils.UUID())
                .put("category", new Byte[]{1, 2, 3, 4})
                .put("create_time", System.currentTimeMillis(), true)
                .put("is_locked", true)
                .put("detail", new TreeObject()
                        .put("real_name", "汉字将被混淆", true)
                        .put("age", 32))
                .put("group", new TreeObject()
                        .put("ids", new TreeObject().add("item1").add("item2"))
                        .put("maps", new TreeObject().put("key1", "value1").put("key2", "value2")));

        // 转换为 JSON 字符串
        String jsonStr = treeObject.toJson().toString(true, true);
        System.out.println("JSON: " + jsonStr);

        // 从 JSON 字符串解析为 TreeObject
        TreeObject fromJson = TreeObject.fromJson(jsonStr);
        System.out.println("ID: " + fromJson.getString("id"));
        System.out.println("Real Name: " + fromJson.get("detail").getMixString("real_name"));
    }
}
```

**执行结果：**

```shell
JSON: {
    "_c": 9,
    "_v": {
        "is_locked": {
            "_c": 6,
            "_v": true
        },
        "create_time": {
            "_c": 5,
            "_v": 1634100753548
        },
        "id": {
            "_c": 3,
            "_v": "4e6b780192da4f04af9b6b826ea7ead6"
        },
        "detail": {
            "_c": 9,
            "_v": {
                "real_name": {
                    "_c": 2,
                    "_v": "5rGJ5a2X5bCG6KKr5re35reG"
                },
                "age": {
                    "_c": 1,
                    "_v": 32
                }
            }
        },
        "category": {
            "_c": 14,
            "_v": "AQIDBA=="
        },
        "group": {
            "_c": 9,
            "_v": {
                "maps": {
                    "_c": 9,
                    "_v": {
                        "key1": {
                            "_c": 3,
                            "_v": "value1"
                        },
                        "key2": {
                            "_c": 3,
                            "_v": "value2"
                        }
                    }
                },
                "ids": {
                    "_c": 10,
                    "_v": [
                        {
                            "_c": 3,
                            "_v": "item1"
                        },
                        {
                            "_c": 3,
                            "_v": "item2"
                        }
                    ]
                }
            }
        }
    }
}
ID: 4e6b780192da4f04af9b6b826ea7ead6
Real Name: 汉字将被混淆
```


**示例：** XML 序列化与反序列化

```java
public class TreeObjectXmlTest {
    public static void main(String[] args) {
        TreeObject treeObject = new TreeObject()
                .put("id", "12345")
                .put("name", "测试用户", true)
                .put("age", 25)
                .put("active", true)
                .put("tags", new TreeObject()
                        .add("java")
                        .add("spring")
                        .add("ymp"));

        // 转换为 XML 字符串
        String xmlStr = treeObject.toXml();
        System.out.println("XML: " + xmlStr);

        // 从 XML 字符串解析为 TreeObject
        TreeObject fromXml = TreeObject.fromXml(xmlStr);
        System.out.println("ID: " + fromXml.getString("id"));
        System.out.println("Name: " + fromXml.getMixString("name"));
        System.out.println("Age: " + fromXml.getInt("age"));
        System.out.println("Active: " + fromXml.getBoolean("active"));

        TreeObject tags = fromXml.get("tags");
        if (tags.isList()) {
            System.out.println("Tags: " + tags.getList());
        }
    }
}
```

**执行结果：**

```shell
XML: <?xml version="1.0" encoding="UTF-8"?>
<tree _c="9">
    <_v>
        <id _c="3">
            <_v>12345</_v>
        </id>
        <name _c="2">
            <_v>5rWL6K+V5a6J</_v>
        </name>
        <age _c="1">
            <_v>25</_v>
        </age>
        <active _c="6">
            <_v>true</_v>
        </active>
        <tags _c="10">
            <_v>
                <item index="0">
                    <_v>java</_v>
                </item>
                <item index="1">
                    <_v>spring</_v>
                </item>
                <item index="2">
                    <_v>ymp</_v>
                </item>
            </_v>
        </tags>
    </_v>
</tree>
ID: 12345
Name: 测试用户
Age: 25
Active: true
Tags: [java, spring, ymp]
```



## HttpClient

基于 Apache HttpComponents 组件封装的 HttpClient 请求与处理工具，使用时需在工程中引入以下依赖包：

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



### CloseableHttpClientHelper

HttpClientHelper 类主要应用于早期 YMP 框架版本及扩展模块中，支持自定义安全连接方式，支持 GET、POST 请求方法，简化文件上传与下载的处理逻辑等。





:::tip **注意**：

从 `2.1.3` 开始 `HttpClientHelper` 标记为不被推荐，请使用 `CloseableHttpClientHelper` 替换。

:::



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
    try (CloseableHttpClientHelper httpClientHelper = CloseableHttpClientHelper.create();
         IHttpResponse response = httpClientHelper.get(url, headers, charset)) {
        System.out.println("StatusCode: " + response.getStatusCode());
        System.out.println("ContentType: " + response.getContentType());
        System.out.println("ContentLength: " + response.getContentLength());
        System.out.println("Content: " + response.getContent());
        System.out.println("Headers: " + response.getHeaders());
        System.out.println("Locale: " + response.getLocale());
        System.out.println("ReasonPhrase: " + response.getReasonPhrase());
    }
}
```



**示例：** 通过 `POST` 方式发送 `application/x-www-form-urlencoded` 请求

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



**示例：** 文件上传

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



**示例：** 文件下载

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
                    // 获取被下载文件对象
                    fileWrapper.getFile();
                    // 获取被下载文件输入流
                    fileWrapper.getInputStream();
                    // 将被下载文件转移到目标文件
                    fileWrapper.transferTo(distFile);
                    // 将被下载文件写入目标文件
                    fileWrapper.writeTo(distFile);
                } else {
                    System.out.printf("ReasonPhrase: %s%n", response.getStatusLine().getReasonPhrase());
                }
            }
        });
    }
}
```



**示例：** 自定义安全连接工厂并设置超时时间等配置参数

```java
public void custom(URL certFilePath, String passwordChars) throws Exception {
    // 通过证书文件构建安全套接字工厂
    SSLConnectionSocketFactory socketFactory = CloseableHttpClientHelper.createConnectionSocketFactory("PKCS12", certFilePath, passwordChars.toCharArray());
    try (CloseableHttpClientHelper httpClientHelper = CloseableHttpClientHelper.create(new ICloseableHttpClientConfigurable.Default() {
                @Override
                protected void doRequestConfig(RequestConfig.Builder builder) {
                    // 设置自定义代理
                    HttpHost proxy = new HttpHost("localhost", 8889);
                    builder.setProxy(proxy);
                }
            })
            .customSSL(socketFactory)
            .connectionTimeout(30000)
            .requestTimeout(30000)
            .socketTimeout(30000)) {
        // ......
    }
}
```




### CloseableHttpRequestBuilder

此类是在 CloseableHttpClientHelper 类的基础上进行了优化、调整请求构建方式及响应的处理逻辑，除了 GET 和 POST 请求方法之外，还增加了对 PUT、 OPTIONS、 DELETE、 HEAD、 PATCH、 TRACE 等的支持。

:::tip **注意**：

从 `2.1.3` 开始 `HttpClientRequestBuilder` 标记为不被推荐，请使用 `CloseableHttpRequestBuilder` 替换。

:::



**示例：** 构建并发送请求

```java
public void newSendRequest(String url, String charset, Map<String, String> requestParams, URL certFilePath, String passwordChars) throws Exception {
    // 此种方式的每次请求都要重新构建HttpClinet实例
    try (IHttpResponse httpResponse = CloseableHttpRequestBuilder.create(url)
            .socketFactory(CloseableHttpClientHelper.createConnectionSocketFactory("PKCS12", certFilePath, passwordChars.toCharArray()))
            .contentType(ContentType.create(CloseableHttpClientHelper.CONTENT_TYPE_FORM_URL_ENCODED, charset))
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
            }).addParams(requestParams).build().post()) {
        if (httpResponse.isSuccess()) {
            // ......
        } else {
            System.out.printf("ResponseBody: %s%n", httpResponse);
        }
    }
}
```



**示例：** 通过 `CloseableHttpClientHelper` 实例构建并发送请求

```java
public void newSendRequest(CloseableHttpClientHelper httpClientHelper, String url, String charset, Map<String, String> requestParams) throws Exception {
    // 此种方式的每次请求都会复用HttpClient实例
    try (IHttpResponse response = httpClientHelper.newRequestBuilder(url)
            .charset(charset)
            .contentType(ContentType.create(CloseableHttpClientHelper.CONTENT_TYPE_FORM_URL_ENCODED, charset))
            .addHeaders(headers)
            .addParams(requestParams)
            .build()
            .post()) {
        if (response.isSuccess()) {
            // ......
        } else {
            System.out.printf("ReasonPhrase: %s%n", response.getReasonPhrase());
        }
    }
}
```



**示例：** 新文件上传

```java
public void newUploadFile(String url, File distFile) throws Exception {
    try (IHttpResponse response = CloseableHttpRequestBuilder.create(url)
            .addContent("file", distFile).build().post()) {
        if (response.isSuccess()) {
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
    try (CloseableHttpClientHelper newHttpClientHelper = CloseableHttpClientHelper.create()
            .customSSL(CloseableHttpClientHelper.createConnectionSocketFactory(SSLContexts.custom()
                    .setProtocol("TLSv1.3")
                    .build()));
         IHttpResponse response = newHttpClientHelper.newRequestBuilder(url)
                 .download(true)
                 .build()
                 .get()) {
        if (response.isSuccess()) {
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

JSON 包装器，为了让不同的第三方 JSON 解析器拥有统一的 API 接口调用方式并能够做到灵活切换而不影响业务系统的正常运行而提供的一套完整的包装层实现。

### 设计目的

- 实现 JSON 操作的统一抽象，降低对具体 JSON 库的依赖
- 提高代码的可移植性和扩展性
- 支持在不同 JSON 库间无缝切换
- 提供丰富的 JSON 操作功能，满足各种业务场景需求

### 支持的 JSON 库

JsonWrapper 现已对当前比较流行且使用非常广泛的 FastJson、Gson 和 Jackson 等进行了封装与适配，同时也支持通过 SPI 或 JVM 启动参数的形式配置基于 `IJsonAdapter` 接口的自定义实现类。

#### 依赖配置

- **FastJson**
  - 适配器类：net.ymate.platform.commons.json.impl.FastJsonAdapter
  - 依赖配置：
  ```xml
  <dependency>
      <groupId>com.alibaba</groupId>
      <artifactId>fastjson</artifactId>
      <version>1.2.84</version>
  </dependency>
  ```

- **Gson**
  - 适配器类：net.ymate.platform.commons.json.impl.GsonAdapter
  - 依赖配置：
  ```xml
  <dependency>
      <groupId>com.google.code.gson</groupId>
      <artifactId>gson</artifactId>
      <version>2.13.1</version>
  </dependency>
  ```

- **Jackson**
  - 适配器类：net.ymate.platform.commons.json.impl.JacksonAdapter
  - 依赖配置：
  ```xml
  <dependency>
      <groupId>com.fasterxml.jackson.datatype</groupId>
      <artifactId>jackson-datatype-jdk8</artifactId>
      <version>2.21.1</version>
  </dependency>
  ```

### 加载逻辑

JSON 包装器是由 JsonWrapper 类进行统一维护和管理，当其被首次加载时，会按照以下顺序尝试实例化对应的包装器类：

1. 首先检查 JVM 启动参数 `ymp.jsonAdapterClass` 是否指定了自定义适配器
2. 若未指定，则通过 SPI 机制查找 `IJsonAdapterFactory` 实现类
3. 若 SPI 未找到，则使用默认的 `DefaultJsonAdapterFactory`
4. 工厂类会按照 FastJson → Gson → Jackson 的顺序尝试加载适配器
5. 加载成功则停止加载流程并返回，否则继续尝试直至未加载到任何结果为止

**示例：** 通过 JVM 启动参数指定 Gson 做为默认 JSON 包装器

```shell
java -jar xxxx.jar -Dymp.jsonAdapterClass=net.ymate.platform.commons.json.impl.GsonAdapter
```

### 核心功能

#### 创建 JSON 对象和数组

```java
// 创建空的 JSON 对象
IJsonObjectWrapper jsonObject = JsonWrapper.createJsonObject();

// 创建有序的 JSON 对象
IJsonObjectWrapper orderedJsonObject = JsonWrapper.createJsonObject(true);

// 创建指定初始容量的 JSON 对象
IJsonObjectWrapper jsonObjectWithCapacity = JsonWrapper.createJsonObject(16);

// 从 Map 创建 JSON 对象
Map<String, Object> map = new HashMap<>();
map.put("name", "test");
IJsonObjectWrapper jsonObjectFromMap = JsonWrapper.createJsonObject(map);

// 创建空的 JSON 数组
IJsonArrayWrapper jsonArray = JsonWrapper.createJsonArray();

// 创建指定初始容量的 JSON 数组
IJsonArrayWrapper jsonArrayWithCapacity = JsonWrapper.createJsonArray(10);

// 从数组创建 JSON 数组
Object[] array = new Object[]{1, "test", true};
IJsonArrayWrapper jsonArrayFromArray = JsonWrapper.createJsonArray(array);

// 从集合创建 JSON 数组
List<Object> list = Arrays.asList(1, "test", true);
IJsonArrayWrapper jsonArrayFromList = JsonWrapper.createJsonArray(list);
```

#### JSON 字符串与 Java 对象转换

```java
// 将 JSON 字符串转换为 JsonWrapper
String jsonStr = "{\"name\":\"test\",\"age\":20}";
JsonWrapper jsonWrapper = JsonWrapper.fromJson(jsonStr);

// 将 Java 对象转换为 JsonWrapper
User user = new User();
user.setName("test");
user.setAge(20);
JsonWrapper userJson = JsonWrapper.toJson(user);

// 将 Java 对象转换为 JSON 字符串（默认紧凑输出）
String userJsonStr = JsonWrapper.toJsonString(user);

// 将 Java 对象转换为格式化的 JSON 字符串
String formattedJson = JsonWrapper.toJsonString(user, true);

// 转换时保留空值
String jsonWithNulls = JsonWrapper.toJsonString(user, true, true);

// 使用蛇形命名转换
String snakeCaseJson = JsonWrapper.toJsonString(user, true, true, true);
```

#### 序列化和反序列化

```java
// 序列化对象为 JSON 字节数组
byte[] serialized = JsonWrapper.serialize(user);

// 反序列化 JSON 字符串为对象
User deserializedUser = JsonWrapper.deserialize(jsonStr, User.class);

// 反序列化时处理蛇形命名
User userWithSnakeCase = JsonWrapper.deserialize(jsonStr, true, User.class);

// 反序列化泛型类型
String arrayJson = "[\"a\",\"b\",\"c\"]";
List<String> stringList = JsonWrapper.deserialize(arrayJson, new TypeReferenceWrapper<List<String>>() {});
```

#### 属性过滤

```java
// 创建属性过滤器
IJsonPropertyFilter filter = new IJsonPropertyFilter() {
    @Override
    public boolean apply(Object object, String name, Object value) {
        // 过滤掉密码字段
        return !"password".equals(name);
    }
};

// 序列化时应用过滤器
String filteredJson = JsonWrapper.toJsonString(user, true, true, filter);
```

### 包装器类型

JsonWrapper 提供了三种类型的包装器，用于不同场景的 JSON 操作：

#### 对象包装器（IJsonObjectWrapper）

用于创建和维护 JsonObject 数据结构，支持链式调用和各种类型的取值方法。

**示例：**

```java
// 创建 JsonObject 对象实例并设置 ordered 为有序的
IJsonObjectWrapper jsonObj = JsonWrapper.createJsonObject(true);
jsonObj.put("name", "suninformation")
       .put("realName", "有理想的鱼")
       .put("age", 20)
       .put("gender", (String) null)
       .put("attrs", JsonWrapper.createJsonObject()
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

// 检查属性是否存在
if (jsonObj.has("name")) {
    System.out.println("Name exists");
}

// 移除属性
jsonObj.remove("gender");

// 转换为 Map
Map<String, Object> map = jsonObj.toMap();
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
Name exists
```

#### 数组包装器（IJsonArrayWrapper）

用于创建和维护 JsonArray 数据结构，支持链式调用和各种类型的取值方法。

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
System.out.println("Index4: " + jsonArray.getBoolean(4));
IJsonObjectWrapper jsonObj = jsonArray.getJsonObject(7);
System.out.println("Name: " + jsonObj.getString("name"));
System.out.println("Age: " + jsonObj.getInt("age"));

// 获取数组长度
System.out.println("Array length: " + jsonArray.size());

// 转换为 List
List<Object> list = jsonArray.toList();
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
Array length: 9
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

### 高级用法

#### 类型引用（TypeReferenceWrapper）

用于处理泛型类型的反序列化，解决 Java 泛型擦除的问题。

**示例：**

```java
// 反序列化复杂泛型类型
String complexJson = "{\"users\":[{\"name\":\"test1\",\"age\":20},{\"name\":\"test2\",\"age\":21}]}";

// 定义类型引用
TypeReferenceWrapper<Map<String, List<User>>> typeRef = new TypeReferenceWrapper<Map<String, List<User>>>() {};

// 反序列化
Map<String, List<User>> result = JsonWrapper.deserialize(complexJson, typeRef);
System.out.println(result.get("users"));
```

#### 蛇形命名转换

支持在序列化和反序列化时自动处理驼峰命名和蛇形命名之间的转换。

**示例：**

```java
// 序列化时使用蛇形命名
String snakeCaseJson = JsonWrapper.toJsonString(user, true, true, true);
System.out.println(snakeCaseJson);

// 反序列化时处理蛇形命名
User userFromSnakeCase = JsonWrapper.deserialize(snakeCaseJson, true, User.class);
System.out.println(userFromSnakeCase);
```

#### 属性过滤

通过 `IJsonPropertyFilter` 接口可以在序列化时过滤掉不需要的属性。

**示例：**

```java
// 创建用户对象
User user = new User();
user.setName("test");
user.setAge(20);
user.setPassword("secret");

// 创建属性过滤器
IJsonPropertyFilter filter = (object, name, value) -> !"password".equals(name);

// 序列化时应用过滤器
String filteredJson = JsonWrapper.toJsonString(user, true, true, filter);
System.out.println(filteredJson);
```

**执行结果：**

```shell
{
    "name":"test",
    "age":20
}
```

### 最佳实践

1. **选择合适的 JSON 库**：根据项目需求和性能要求选择合适的 JSON 库
2. **使用类型引用处理泛型**：对于复杂泛型类型，使用 TypeReferenceWrapper 进行反序列化
3. **合理使用属性过滤**：在序列化敏感数据时，使用属性过滤器保护隐私信息
4. **统一配置**：通过 JVM 启动参数或 SPI 机制统一配置 JSON 适配器
5. **异常处理**：在处理 JSON 操作时，注意捕获和处理可能的异常

JsonWrapper 为 YMP 框架提供了统一、灵活的 JSON 处理能力，使得开发者可以更加专注于业务逻辑，而不必关心底层 JSON 库的实现细节。


## Markdown

对 Markdown 语法中常用到的格式，如：标题、文本、引用、表格、代码片段、图片、链接等进行对象封装，避免以往采用字符串拼接形式中经常出现的问题。

### 核心组件

Markdown 模块提供了以下核心组件，每个组件都实现了 `IMarkdown` 接口：

| 组件 | 说明 | 主要功能 |
|------|------|----------|
| `MarkdownBuilder` | Markdown 文档构建器 | 链式构建完整 Markdown 文档 |
| `Title` | 标题组件 | 支持 1-6 级标题 |
| `Text` | 文本组件 | 支持普通、粗体、斜体、下划线、删除线样式 |
| `Code` | 代码组件 | 支持行内代码和代码块，可指定语言 |
| `Quote` | 引用组件 | 生成引用块内容 |
| `Link` | 链接组件 | 生成超链接 |
| `Image` | 图片组件 | 生成图片，支持缩放 |
| `Table` | 表格组件 | 生成表格，支持对齐方式 |
| `ParagraphList` | 列表组件 | 支持有序/无序列表、嵌套列表 |

### MarkdownBuilder

`MarkdownBuilder` 是 Markdown 文档构建器，用于链式构建完整的 Markdown 文档。

#### 基础方法

```java
// 创建实例
MarkdownBuilder builder = MarkdownBuilder.create();

// 换行和段落
builder.br();           // 添加换行符
builder.p();            // 添加段落分隔（两个换行符）
builder.p(3);           // 添加指定数量的换行符

// 空格和缩进
builder.space();        // 添加一个空格
builder.space(3);       // 添加指定数量的空格
builder.tab();          // 添加制表符（4个空格）

// 水平分隔线
builder.hr();           // 添加水平分隔线

// 追加内容
builder.append("字符串内容");
builder.append(markdownObject);  // 追加 IMarkdown 对象

// 获取内容长度
int len = builder.length();
```

#### 标题

```java
// 一级标题
builder.title("一级标题");
builder.title(markdownObject);   // 使用 IMarkdown 对象

// 指定级别标题（1-6级）
builder.title("二级标题", 2);
builder.title(markdownObject, 3);
```

#### 文本

```java
// 普通文本
builder.text("普通文本");
builder.text(markdownObject);

// 带样式的文本
builder.text("粗体文本", Text.Style.BOLD);       // **粗体**
builder.text("斜体文本", Text.Style.ITALIC);     // *斜体*
builder.text("下划线文本", Text.Style.UNDERLINE); // <u>下划线</u>
builder.text("删除线文本", Text.Style.STRIKEOUT);  // ~~删除线~~
```

#### 引用

```java
builder.quote("引用文本内容");
builder.quote(markdownObject);
```

#### 链接

```java
// 带显示文本的链接
builder.link("YMP", "https://ymate.net/");       // [YMP](https://ymate.net/)
builder.link(markdownObject, "https://example.com");

// 仅 URL（显示文本与 URL 相同）
Link link = Link.create("https://example.com");  // [https://example.com](https://example.com)
```

#### 图片

```java
// 仅 URL
builder.image("https://ymate.net/img/logo.png");  // ![](url)

// 带替代文本
builder.image("Logo image.", "https://ymate.net/img/logo.png");  // ![alt](url)

// 带缩放比例（0-200，使用 HTML img 标签）
builder.image("Logo", "https://ymate.net/img/logo.png", 50);  // <img style="zoom:50%;" />
```

#### 代码

```java
// 行内代码
builder.code("code");                              // `code`
builder.code(markdownObject);

// 指定语言的代码块
builder.code("public class Test {}", "java");      // ```java\ncode\n```
builder.code(markdownObject, "python");
```

### Text 组件

`Text` 组件用于生成不同样式的文本内容。

```java
// 创建普通文本
Text text = Text.create("Hello World");

// 创建带样式的文本
Text boldText = Text.create("粗体", Text.Style.BOLD);
Text italicText = Text.create("斜体", Text.Style.ITALIC);
Text underlineText = Text.create("下划线", Text.Style.UNDERLINE);
Text strikeoutText = Text.create("删除线", Text.Style.STRIKEOUT);

// 使用 IMarkdown 对象创建文本
Text textFromMarkdown = Text.create(Title.create("标题"));
Text styledMarkdown = Text.create(Title.create("标题"), Text.Style.ITALIC);

// 追加内容（链式调用）
Text text = Text.create("Hello")
    .append(" World")
    .append(Title.create("!"));
```

**支持的样式：**

| 样式 | 枚举值 | Markdown 输出 |
|------|--------|---------------|
| 普通 | `Style.NORMAL` | 普通文本 |
| 粗体 | `Style.BOLD` | `**文本**` |
| 斜体 | `Style.ITALIC` | `*文本*` |
| 下划线 | `Style.UNDERLINE` | `<u>文本</u>` |
| 删除线 | `Style.STRIKEOUT` | `~~文本~~` |

### Title 组件

`Title` 组件用于生成不同级别的标题。

```java
// 创建一级标题
Title title1 = Title.create("一级标题");           // # 一级标题

// 创建指定级别标题（1-6级）
Title title2 = Title.create("二级标题", 2);        // ## 二级标题
Title title6 = Title.create("六级标题", 6);        // ###### 六级标题

// 使用 IMarkdown 对象创建标题
Title title = Title.create(Text.create("粗体标题", Text.Style.BOLD), 2);

// 追加内容（链式调用）
Title title = Title.create("Hello")
    .append(" World")
    .append(Text.create("!"));
```

**说明：**
- 标题级别范围：1-6，超出范围会自动调整（小于1视为1，大于6视为6）
- 标题内容中的换行符会被替换为空格

### Code 组件

`Code` 组件用于生成行内代码或代码块。

```java
// 行内代码
Code inlineCode = Code.create("inline code");      // `inline code`

// 代码块（自动检测换行符）
Code codeBlock = Code.create("line1\nline2");       // ```\nline1\nline2\n```

// 指定语言的代码块
Code javaCode = Code.create("public class Test {}", "java");
// 输出：```java
//      public class Test {}
//      ```

// 使用 IMarkdown 对象
Code code = Code.create(Text.create("code"));
Code styledCode = Code.create(Text.create("code"), "java");

// 追加代码内容（链式调用）
Code code = Code.create("line1")
    .append("\nline2")
    .append("\nline3");
```

**说明：**
- 当明确指定了非空白语言，或内容包含换行符时，使用代码块格式（```）
- 否则使用行内代码格式（`）
- 空内容或仅空白字符的内容返回空字符串

### Quote 组件

`Quote` 组件用于生成引用块。

```java
// 创建引用
Quote quote = Quote.create("引用文本内容");        // > 引用文本内容\n> \n

// 使用 IMarkdown 对象
Quote quote = Quote.create(Text.create("引用文本"));

// 追加内容（链式调用）
Quote quote = Quote.create("第一行")
    .append("\n第二行")
    .append(Text.create("\n第三行"));
```

**说明：**
- 每行内容以 `> ` 开头
- 引用末尾会自动添加一个空引用行 `> `
- 空内容或仅空白字符的内容返回空字符串

### Link 组件

`Link` 组件用于生成超链接。

```java
// 仅 URL（显示文本与 URL 相同）
Link link1 = Link.create("https://example.com");   // [https://example.com](https://example.com)

// 带显示文本的链接
Link link2 = Link.create("示例", "https://example.com");  // [示例](https://example.com)

// 使用 IMarkdown 对象作为显示文本
Link link3 = Link.create(Text.create("粗体链接", Text.Style.BOLD), "https://example.com");
```

**说明：**
- URL 为空或仅空白字符时返回空字符串
- 显示文本为空时会使用 URL 作为显示文本
- 首尾空白字符会被自动去除

### Image 组件

`Image` 组件用于生成图片。

```java
// 仅 URL
Image image1 = Image.create("https://example.com/img.jpg");  // ![](url)

// 带替代文本
Image image2 = Image.create("图片描述", "https://example.com/img.jpg");  // ![alt](url)

// 带缩放比例（0-200）
Image image3 = Image.create("Logo", "https://example.com/logo.png", 50);
// 输出：<img src="url" alt="Logo" style="zoom:50%;" />
```

**说明：**
- URL 为空或仅空白字符时返回空字符串
- 缩放比例为 0 时使用标准 Markdown 图片语法
- 缩放比例范围：0-200，超出范围会自动调整
- 首尾空白字符会被自动去除

### Table 组件

`Table` 组件用于生成表格。

```java
// 创建表格
Table table = Table.create()
    // 添加表头
    .addHeader("序号", Table.Align.CENTER)    // 居中对齐
    .addHeader("命令")                         // 默认左对齐
    .addHeader("描述", Table.Align.RIGHT)     // 右对齐
    // 添加数据行
    .addRow()
        .addColumn("1")
        .addColumn(Code.create("mvn clean"))
        .addColumn("执行工程清理")
        .build()                              // 结束当前行
    .addRow()
        .addColumn("2")
        .addColumn(Code.create("mvn install"))
        .addColumn("执行安装")
        .build();
```

**对齐方式：**

| 对齐方式 | 枚举值 | Markdown 输出 |
|----------|--------|---------------|
| 默认 | `Align.NORMAL` | `---` |
| 左对齐 | `Align.LEFT` | `:---` |
| 居中 | `Align.CENTER` | `:---:` |
| 右对齐 | `Align.RIGHT` | `---:` |

**说明：**
- 表头和列内容支持 `IMarkdown` 对象
- 特殊字符（如 `|`、换行符）会自动转义

### ParagraphList 组件

`ParagraphList` 组件用于生成有序列表或无序列表，支持嵌套。

```java
// 无序列表
ParagraphList unorderedList = ParagraphList.create()
    .addItem("Item 1")
    .addItem("Item 2")
    .addItem("Item 3");

// 有序列表
ParagraphList orderedList = ParagraphList.create(true)
    .addItem("第一步")
    .addItem("第二步")
    .addItem("第三步");

// 添加多个列表项
ParagraphList list = ParagraphList.create()
    .addItems("Item 1", "Item 2", "Item 3");

// 嵌套子列表
ParagraphList nestedList = ParagraphList.create()
    .addItem("父项 1")
    .addSubItem("子项 1.1")
    .addSubItem("子项 1.2")
    .addItem("父项 2")
    .addSubItems("子项 2.1", "子项 2.2");

// 添加指定类型的子列表
ParagraphList list = ParagraphList.create()
    .addItem("父项")
    .addSubItem("有序子项", true)   // 有序子列表
    .addSubItems(false, "无序子项1", "无序子项2");  // 无序子列表

// 添加正文内容
ParagraphList list = ParagraphList.create()
    .addItem("列表项")
    .addBody("这是列表项的正文内容，会显示在列表项下方。");
```

**说明：**
- `create()` 创建无序列表，使用 `- ` 作为前缀
- `create(true)` 创建有序列表，使用 `数字. ` 作为前缀
- 子列表会自动添加缩进（4个空格）
- 空内容或仅空白字符的列表项会被忽略

### 完整示例

```java
MarkdownBuilder markdownBuilder = MarkdownBuilder.create()
    .title("一级标题").p()
    .title("二级标题", 2).p()
    .text("普通文本")
    .tab().text("斜体文本", Text.Style.ITALIC)
    .space().text("粗体文本", Text.Style.BOLD).p()
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

普通文本    *斜体文本* **粗体文本**

------


> 引用文本内容...
>
> - Item
>
>     - SubItem
>
> Item body.


|序号|命令|描述|
|:---:|:---:|---:|
|1|`mvn clean`|执行工程清理|


![Logo image.](https://ymate.net/img/logo.png)

```shell
mvn clean source:jar install
```

[YMP](https://ymate.net/)
````





## 序列化（Serialize）

基于 `ISerializer` 接口实现对象序列化与反序列化操作，由 `SerializerManager` 对象序列化管理器维护管理，支持通过 `SPI` 机制和自动扫描 `@Serializer` 注解方式加载并注册。

### 核心组件

序列化模块提供了以下核心组件：

| 组件 | 说明 | 主要功能 |
|------|------|----------|
| `ISerializer` | 序列化器接口 | 定义序列化和反序列化的标准行为 |
| `SerializerManager` | 序列化器管理器 | 管理和提供各种序列化器实例 |
| `@Serializer` | 序列化器注解 | 标记和命名序列化器实现类 |
| `SerializationException` | 序列化异常 | 序列化过程中的异常处理 |

### 内置序列化器

模块默认提供了以下序列化器实现：

| 序列化器 | 名称 | 内容类型 | 说明 | 依赖 |
|----------|------|----------|------|------|
| `DefaultSerializer` | `default` | `application/x-java-serialized-object` | Java原生序列化 | 无 |
| `JSONSerializer` | `json` | `application/json` | JSON格式序列化 | 无 |
| `FstSerializer` | `fst` | `application/x-java-serialized-fst` | FST高性能序列化 | 可选 |
| `HessianSerializer` | `hessian` | `application/x-java-serialized-hessian` | Hessian二进制序列化 | 可选 |
| `KryoSerializer` | `kryo` | `application/x-kryo` | Kryo高性能序列化 | 可选 |
| `ProtobufSerializer` | `protobuf` | `application/x-protobuf` | Protobuf序列化 | 可选 |

### SerializerManager

`SerializerManager` 是序列化器管理器，负责管理和提供各种序列化器实例。

#### 获取序列化器

```java
// 获取默认序列化器（DefaultSerializer）
ISerializer serializer = SerializerManager.getDefaultSerializer();

// 获取JSON序列化器
ISerializer jsonSerializer = SerializerManager.getJsonSerializer();

// 获取FST序列化器（可选依赖）
ISerializer fstSerializer = SerializerManager.getFstSerializer();

// 获取Hessian序列化器（可选依赖）
ISerializer hessianSerializer = SerializerManager.getHessianSerializer();

// 获取Protobuf序列化器（可选依赖）
ISerializer protobufSerializer = SerializerManager.getProtobufSerializer();

// 获取Kryo序列化器（可选依赖）
ISerializer kryoSerializer = SerializerManager.getKryoSerializer();

// 根据名称获取序列化器（不区分大小写）
ISerializer customSerializer = SerializerManager.getSerializer("custom");
```

#### 注册和注销序列化器

```java
// 注册序列化器（通过Class）
SerializerManager.registerSerializer(CustomSerializer.class);

// 注册序列化器（通过名称和Class）
SerializerManager.registerSerializer("my-serializer", CustomSerializer.class);

// 注销指定名称的序列化器
SerializerManager.unregisterSerializer("custom");

// 注销指定类型的序列化器
SerializerManager.unregisterSerializer(CustomSerializer.class);

// 注销所有序列化器
SerializerManager.unregisterAll();
```

#### 查询序列化器

```java
// 检查是否存在指定名称的序列化器
boolean exists = SerializerManager.containsSerializer("json");

// 检查是否存在指定类型的序列化器
boolean exists = SerializerManager.containsSerializer(JSONSerializer.class);

// 获取所有已注册的序列化器名称
Set<String> names = SerializerManager.getRegisteredNames();

// 获取所有已注册的序列化器实例
Collection<ISerializer> serializers = SerializerManager.getRegisteredSerializers();

// 获取已注册的序列化器数量
int count = SerializerManager.getSerializerCount();
```

### ISerializer 接口

`ISerializer` 是序列化器接口，定义了序列化和反序列化的标准行为。

#### 序列化对象

```java
// 序列化对象为字节数组
byte[] bytes = serializer.serialize(object);
```

#### 反序列化对象

```java
// 反序列化为指定类型
TestObject obj = serializer.deserialize(bytes, TestObject.class);

// 反序列化为复杂类型（支持泛型）
List<TestObject> list = serializer.deserialize(bytes, new TypeReferenceWrapper<List<TestObject>>() {});
```

#### 获取内容类型

```java
// 获取序列化器的内容类型
String contentType = serializer.getContentType();
```

### @Serializer 注解

`@Serializer` 注解用于标记和命名序列化器实现类。

```java
// 使用注解标记序列化器
@Serializer("custom")
public class CustomSerializer implements ISerializer {
    // 实现序列化和反序列化方法
}
```

**说明：**
- 注解的 `value` 用于指定序列化器名称
- 如果未指定名称，则使用类的全限定名
- 序列化器名称不区分大小写
- 通过 SPI 机制加载时，会使用该注解的值作为名称

### 序列化器说明

#### DefaultSerializer

Java原生序列化器，基于Java标准库的 `ObjectOutputStream` 和 `ObjectInputStream`。

**特点：**
- 支持所有实现了 `Serializable` 接口的对象
- 序列化格式为Java专有的二进制格式
- 具有较好的兼容性和稳定性

**注意事项：**
- 序列化效率相对较低，不适合高性能场景
- 序列化结果较大，占用较多存储空间

#### JSONSerializer

JSON序列化器，基于 `IJsonAdapter` 接口提供JSON格式的序列化和反序列化功能。

**特点：**
- 支持跨平台、可读性强的JSON格式
- 支持复杂类型的反序列化，包括泛型集合、嵌套对象等

**注意事项：**
- JSON序列化结果通常比二进制格式大
- 序列化和反序列化性能相对较低
- 不支持循环引用

#### FstSerializer

FST（Fast Serialization）序列化器，基于FST库提供高性能的序列化和反序列化功能。

**特点：**
- 高性能、低内存占用
- 比Java原生序列化快10倍以上
- 支持复杂的对象图序列化

**依赖配置：**

```xml
<dependency>
    <groupId>de.ruedigermoeller</groupId>
    <artifactId>fst</artifactId>
    <version>2.57</version>
</dependency>
```

**注意事项：**
- 需要添加FST库依赖
- 该序列化器通过SPI机制动态加载，仅在FST库可用时注册
- 序列化结果为二进制格式，不可读
- 跨语言支持有限

#### HessianSerializer

Hessian序列化器，基于Hessian二进制协议提供高效的序列化和反序列化功能。

**特点：**
- 跨语言、高性能的二进制协议
- 支持多种编程语言
- 适用于RPC远程调用和分布式系统

**依赖配置：**

```xml
<dependency>
    <groupId>com.caucho</groupId>
    <artifactId>hessian</artifactId>
    <version>4.0.66</version>
</dependency>
```

**注意事项：**
- 需要添加Hessian库依赖
- 该序列化器通过SPI机制动态加载，仅在Hessian库可用时注册
- 序列化结果为二进制格式，不可读
- 某些Java特性可能不完全支持

#### KryoSerializer

Kryo序列化器，基于Kryo库提供高性能的序列化和反序列化功能。

**特点：**
- 极致的序列化性能
- 比Java原生序列化快数倍
- 生成的序列化结果更小
- 支持序列化任何Java对象，包括未实现Serializable接口的对象

**依赖配置：**

```xml
<dependency>
    <groupId>com.esotericsoftware</groupId>
    <artifactId>kryo</artifactId>
    <version>5.6.2</version>
</dependency>
```

**注意事项：**
- 需要添加Kryo库依赖
- 该序列化器通过SPI机制动态加载，仅在Kryo库可用时注册
- 序列化结果为二进制格式，不可读
- 跨语言支持有限
- Kryo实例不是线程安全的，需要为每个线程创建独立实例

#### ProtobufSerializer

Protobuf（Protocol Buffers）序列化器，基于Google Protobuf库提供高效的序列化和反序列化功能。

**特点：**
- 高性能、跨语言支持
- 良好的向前/向后兼容性
- 比XML和JSON更小、更快、更简单

**依赖配置：**

```xml
<dependency>
    <groupId>com.google.protobuf</groupId>
    <artifactId>protobuf-java</artifactId>
    <version>4.34.0</version>
</dependency>
```

**注意事项：**
- 需要添加Protobuf库依赖
- 该序列化器通过SPI机制动态加载，仅在Protobuf库可用时注册
- 序列化结果为二进制格式，不可读
- 仅支持实现了 `Message` 或 `MessageLite` 接口的对象序列化

### 示例一：基本序列化与反序列化操作

```java
public class SerialDemoBean implements Serializable {

    private String name;

    private String remark;

    // Getter和Setter方法
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
        return String.format("SerialDemoBean{name='%s', remark='%s'}", name, remark);
    }

    public static void main(String[] args) throws Exception {
        // 创建待序列化对象
        SerialDemoBean demoBean = new SerialDemoBean();
        demoBean.setName("YMP");
        demoBean.setRemark("A lightweight modular simple and powerful Java framework.");

        // 通过对象序列化管理器获取默认序列化器
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

### 示例二：使用JSON序列化器

```java
public class JsonSerializeDemo {

    public static void main(String[] args) throws Exception {
        // 创建待序列化对象
        SerialDemoBean demoBean = new SerialDemoBean();
        demoBean.setName("YMP");
        demoBean.setRemark("A lightweight modular simple and powerful Java framework.");

        // 获取JSON序列化器
        ISerializer jsonSerializer = SerializerManager.getJsonSerializer();

        // 执行序列化操作
        byte[] bytes = jsonSerializer.serialize(demoBean);
        System.out.println("JSON: " + new String(bytes, StandardCharsets.UTF_8));

        // 执行反序列化操作
        SerialDemoBean deserializeBean = jsonSerializer.deserialize(bytes, SerialDemoBean.class);
        System.out.println(deserializeBean.toString());
    }
}
```

### 示例三：使用FST序列化器（高性能）

```java
public class FstSerializeDemo {

    public static void main(String[] args) throws Exception {
        // 检查FST序列化器是否可用
        ISerializer fstSerializer = SerializerManager.getFstSerializer();
        if (fstSerializer == null) {
            System.out.println("FST serializer is not available. Please add FST dependency.");
            return;
        }

        // 创建待序列化对象
        SerialDemoBean demoBean = new SerialDemoBean();
        demoBean.setName("YMP");
        demoBean.setRemark("High performance serialization with FST.");

        // 执行序列化操作
        byte[] bytes = fstSerializer.serialize(demoBean);
        System.out.println("FST bytes length: " + bytes.length);

        // 执行反序列化操作
        SerialDemoBean deserializeBean = fstSerializer.deserialize(bytes, SerialDemoBean.class);
        System.out.println(deserializeBean.toString());
    }
}
```

### 示例四：自定义序列化器实现

本例通过自动扫描 `@Serializer` 注解方式加载并注册一个名称为 `custom` 的自定义对象序列化器实现类。

```java
@Serializer("custom")
public class CustomSerializer implements ISerializer {

    @Override
    public String getContentType() {
        return "application/json";
    }

    @Override
    public byte[] serialize(Object object) throws SerializationException {
        com.alibaba.fastjson.serializer.JSONSerializer serializer = new com.alibaba.fastjson.serializer.JSONSerializer();
        serializer.config(SerializerFeature.WriteEnumUsingToString, true);
        serializer.write(object);
        return serializer.getWriter().toBytes(StandardCharsets.UTF_8);
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) throws SerializationException {
        return JSON.parseObject(new String(bytes, StandardCharsets.UTF_8), clazz);
    }
}
```

### 示例五：在YMP应用中使用自定义序列化器

```java
@EnableAutoScan
@EnableBeanProxy
public class Starter {

    static {
        System.setProperty(IApplication.SYSTEM_MAIN_CLASS, Starter.class.getName());
    }

    private static final Log LOG = LogFactory.getLog(Starter.class);

    public static void main(String[] args) throws Exception {
        try (IApplication application = YMP.run(args)) {
            // 创建待序列化对象
            SerialDemoBean demoBean = new SerialDemoBean();
            demoBean.setName("YMP");
            demoBean.setRemark("A lightweight modular simple and powerful Java framework.");

            // 获取自定义序列化器
            ISerializer serializer = SerializerManager.getSerializer("custom");

            // 执行序列化操作
            byte[] bytes = serializer.serialize(demoBean);

            // 执行反序列化操作
            SerialDemoBean deserializeBean = serializer.deserialize(bytes, SerialDemoBean.class);

            // 输出对象值
            System.out.println(deserializeBean.toString());
        }
    }
}
```

### 最佳实践

1. **选择合适的序列化器**：
   - 默认场景使用 `DefaultSerializer` 或 `JSONSerializer`
   - 高性能场景使用 `FstSerializer` 或 `KryoSerializer`
   - 跨语言通信使用 `HessianSerializer` 或 `ProtobufSerializer`

2. **处理可选依赖**：
   - 使用可选序列化器前，先检查是否可用
   - 提供降级方案，当可选序列化器不可用时使用默认序列化器

3. **异常处理**：
   - 捕获 `SerializationException` 异常
   - 记录序列化和反序列化过程中的错误信息

4. **性能优化**：
   - 大数据量场景优先使用高性能序列化器
   - 缓存序列化器实例，避免重复创建

5. **自定义序列化器**：
   - 使用 `@Serializer` 注解标记自定义序列化器
   - 通过 SPI 机制或自动扫描注册序列化器
   - 确保序列化和反序列化的对称性



## 重试机制（Retry）

重试机制提供了一套完整的重试策略实现，用于处理网络请求、服务调用等可能失败的操作。该模块支持自定义重试次数、延迟策略、超时控制以及异常类型过滤等功能。



### 核心组件

重试机制包含以下核心组件：

| 组件 | 说明 |
|------|------|
| `IRetryable<T>` | 可重试操作接口，定义需要执行重试逻辑的操作 |
| `IRetryDelayStrategy` | 延迟策略接口，定义重试之间的延迟计算逻辑 |
| `RetryConfig` | 重试配置类，用于定义重试行为的相关参数 |
| `RetryUtils` | 重试工具类，提供便捷的重试操作执行方法 |



### 快速开始

#### 最简单的使用方式

使用默认配置（3次重试、1秒初始延迟、指数退避策略）执行重试操作：

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



#### 自定义重试次数和延迟

```java
import net.ymate.platform.commons.retry.RetryUtils;

public class RetryDemo {
    public static void main(String[] args) throws Exception {
        // 设置最大重试次数为5次，初始延迟为2秒
        String result = RetryUtils.executeWithRetry(() -> {
            return doSomething();
        }, 5, 2000);
    }
}
```



#### 指定可重试的异常类型

只有抛出指定类型的异常时才会进行重试：

```java
import net.ymate.platform.commons.retry.RetryUtils;
import java.io.IOException;
import java.net.SocketTimeoutException;

public class RetryDemo {
    public static void main(String[] args) throws Exception {
        // 只对 IOException 和 SocketTimeoutException 进行重试
        String result = RetryUtils.executeWithRetry(
            () -> doSomething(),
            3, 1000,
            IOException.class, SocketTimeoutException.class
        );
    }
}
```



### 延迟策略

重试机制提供了三种内置的延迟策略，可以根据不同场景选择合适的策略。



#### 固定延迟策略（FixedDelayStrategy）

每次重试使用相同的延迟时间，适用于对延迟时间要求固定的场景。

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

**延迟时间示例：**

| 尝试次数 | 延迟时间 |
|----------|----------|
| 第1次失败后 | 500ms |
| 第2次失败后 | 500ms |
| 第3次失败后 | 500ms |



#### 指数延迟策略（ExponentialDelayStrategy）

延迟时间随重试次数指数增长，公式为：`delay = initialDelay * 2^(attempt-1)`。适用于需要逐步增加重试间隔的场景，如网络请求、服务调用等。

```java
import net.ymate.platform.commons.retry.RetryConfig;
import net.ymate.platform.commons.retry.RetryUtils;

public class RetryDemo {
    public static void main(String[] args) throws Exception {
        // 使用默认最大延迟（1小时）
        RetryConfig config = RetryConfig.custom()
            .maxRetries(5)
            .exponentialDelay(1000)  // 初始延迟1秒
            .build();

        // 或指定最大延迟时间
        RetryConfig config2 = RetryConfig.custom()
            .maxRetries(5)
            .exponentialDelay(1000, 60000)  // 初始延迟1秒，最大延迟1分钟
            .build();

        String result = RetryUtils.executeWithRetry(() -> doSomething(), config);
    }
}
```

**延迟时间示例（初始延迟1000ms）：**

| 尝试次数 | 计算公式 | 延迟时间 |
|----------|----------|----------|
| 第1次失败后 | 1000 * 2^0 | 1000ms |
| 第2次失败后 | 1000 * 2^1 | 2000ms |
| 第3次失败后 | 1000 * 2^2 | 4000ms |
| 第4次失败后 | 1000 * 2^3 | 8000ms |
| 第5次失败后 | 1000 * 2^4 | 16000ms |



#### 随机延迟策略（RandomDelayStrategy）

在指定的最小和最大延迟时间之间随机选择延迟时间，适用于需要避免重试请求同时发生的场景（如分布式系统中的惊群效应）。

```java
import net.ymate.platform.commons.retry.RetryConfig;
import net.ymate.platform.commons.retry.RetryUtils;

public class RetryDemo {
    public static void main(String[] args) throws Exception {
        RetryConfig config = RetryConfig.custom()
            .maxRetries(3)
            .randomDelay(100, 500)  // 延迟时间在100ms到500ms之间随机
            .build();

        String result = RetryUtils.executeWithRetry(() -> doSomething(), config);
    }
}
```



### 完整配置示例

使用 `RetryConfig` 进行完整的重试配置：

```java
import net.ymate.platform.commons.retry.RetryConfig;
import net.ymate.platform.commons.retry.RetryUtils;
import java.io.IOException;
import java.net.SocketTimeoutException;

public class RetryDemo {
    public static void main(String[] args) throws Exception {
        RetryConfig config = RetryConfig.custom()
            // 设置最大重试次数
            .maxRetries(5)
            // 使用指数延迟策略，初始延迟1秒，最大延迟1分钟
            .exponentialDelay(1000, 60000)
            // 设置总超时时间为5分钟
            .totalTimeoutMs(5 * 60 * 1000L)
            // 设置可重试的异常类型
            .retryableExceptions(IOException.class, SocketTimeoutException.class)
            .build();

        String result = RetryUtils.executeWithRetry(() -> {
            // 执行可能失败的操作
            return callRemoteService();
        }, config);

        System.out.println("Result: " + result);
    }

    private static String callRemoteService() throws IOException {
        // 模拟远程服务调用
        return "success";
    }
}
```



### 配置参数说明

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `maxRetries` | int | 3 | 最大重试次数，必须大于0 |
| `initialDelayMs` | long | 1000 | 初始延迟时间（毫秒） |
| `totalTimeoutMs` | Long | 300000 (5分钟) | 总超时时间（毫秒），null表示不限制 |
| `delayStrategy` | IRetryDelayStrategy | ExponentialDelayStrategy | 延迟策略 |
| `retryableExceptions` | Class&lt;? extends Exception&gt;[] | 空数组 | 可重试的异常类型，为空表示所有异常都可重试 |



### 自定义延迟策略

通过实现 `IRetryDelayStrategy` 接口可以创建自定义的延迟策略：

```java
import net.ymate.platform.commons.retry.IRetryDelayStrategy;

public class CustomDelayStrategy implements IRetryDelayStrategy {

    @Override
    public long getDelay(int attempt) {
        // 自定义延迟逻辑
        // 例如：线性增长延迟
        return attempt * 1000L;  // 第1次1秒，第2次2秒，第3次3秒...
    }
}

// 使用自定义延迟策略
RetryConfig config = RetryConfig.custom()
    .maxRetries(3)
    .delayStrategy(new CustomDelayStrategy())
    .build();
```



### 异常处理

#### 异常类型过滤

通过 `retryableExceptions` 参数可以指定哪些异常类型需要重试：

```java
import net.ymate.platform.commons.retry.RetryConfig;
import net.ymate.platform.commons.retry.RetryUtils;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

public class RetryDemo {
    public static void main(String[] args) {
        RetryConfig config = RetryConfig.custom()
            .maxRetries(3)
            .exponentialDelay(1000)
            // 只对网络相关异常进行重试
            .retryableExceptions(
                IOException.class,
                ConnectException.class,
                SocketTimeoutException.class
            )
            .build();

        try {
            String result = RetryUtils.executeWithRetry(() -> doSomething(), config);
        } catch (IOException e) {
            // 重试失败后的处理
            System.err.println("All retries failed: " + e.getMessage());
        } catch (Exception e) {
            // 其他异常（非可重试异常）直接抛出
            System.err.println("Non-retryable exception: " + e.getMessage());
        }
    }
}
```

**异常处理规则：**

1. 如果未指定 `retryableExceptions`，所有异常都会触发重试
2. 如果指定了 `retryableExceptions`，只有匹配的异常类型才会触发重试
3. 非可重试异常会直接抛出，不会触发重试



#### 超时处理

当总执行时间超过 `totalTimeoutMs` 时，会抛出 `RuntimeException`：

```java
import net.ymate.platform.commons.retry.RetryConfig;
import net.ymate.platform.commons.retry.RetryUtils;

public class RetryDemo {
    public static void main(String[] args) {
        RetryConfig config = RetryConfig.custom()
            .maxRetries(10)
            .fixedDelay(1000)
            .totalTimeoutMs(5000L)  // 总超时5秒
            .build();

        try {
            String result = RetryUtils.executeWithRetry(() -> doSomething(), config);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("timeout")) {
                System.err.println("Retry timeout: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
```



### 实际应用场景

#### HTTP 请求重试

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



#### 数据库操作重试

```java
import net.ymate.platform.commons.retry.RetryConfig;
import net.ymate.platform.commons.retry.RetryUtils;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseRetryDemo {

    public static void executeWithRetry(String sql) throws Exception {
        RetryConfig config = RetryConfig.custom()
            .maxRetries(5)
            .exponentialDelay(500, 10000)
            .retryableExceptions(SQLException.class)
            .totalTimeoutMs(60000L)
            .build();

        RetryUtils.executeWithRetry(() -> {
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/test", "user", "password")) {
                conn.createStatement().execute(sql);
            }
            return null;
        }, config);
    }
}
```



#### 分布式锁重试

```java
import net.ymate.platform.commons.retry.RetryConfig;
import net.ymate.platform.commons.retry.RetryUtils;

public class DistributedLockDemo {

    public static <T> T executeWithLock(String lockKey, java.util.concurrent.Callable<T> task) throws Exception {
        RetryConfig config = RetryConfig.custom()
            .maxRetries(10)
            .randomDelay(100, 500)  // 使用随机延迟避免竞争
            .totalTimeoutMs(30000L)
            .build();

        return RetryUtils.executeWithRetry(() -> {
            if (tryLock(lockKey)) {
                try {
                    return task.call();
                } finally {
                    unlock(lockKey);
                }
            } else {
                throw new RuntimeException("Failed to acquire lock: " + lockKey);
            }
        }, config);
    }

    private static boolean tryLock(String key) {
        // 实现获取分布式锁的逻辑
        return true;
    }

    private static void unlock(String key) {
        // 实现释放分布式锁的逻辑
    }
}
```



### 最佳实践

1. **选择合适的延迟策略**
   - 对于网络请求，推荐使用指数退避策略
   - 对于分布式系统中的竞争场景，推荐使用随机延迟策略
   - 对于对延迟时间有严格要求的场景，使用固定延迟策略

2. **合理设置重试次数**
   - 根据业务场景和操作的幂等性设置合适的重试次数
   - 避免设置过大的重试次数，以免造成资源浪费

3. **设置超时时间**
   - 建议始终设置 `totalTimeoutMs`，避免无限重试
   - 超时时间应根据业务需求和用户体验来设置

4. **精确指定异常类型**
   - 只对可恢复的异常进行重试
   - 对于业务异常或参数校验异常，不应重试

5. **确保操作的幂等性**
   - 重试操作必须是幂等的，即多次执行不会产生副作用
   - 对于非幂等操作，需要额外的机制来保证数据一致性

6. **记录重试日志**
   - 重试机制内置了日志记录，建议在日志配置中开启 DEBUG 级别以便排查问题



## Utils

提供包含类与反射、字符串加密与解密、地理位置与编码、日期时间、正则表达式、文件、网络、参数、资源、运行时、线程操作等常用工具类封装。



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
            return Strings.CS.equals(fieldName, "age");
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
            return Strings.CS.equals(fieldName, "age");
        }).forEach((key, value) -> System.out.println(key + ": " + value));
        // 获取当前类实例对象
        Demo demo = beanWrapper.getTargetObject();
        // 类属性浅拷贝（支持属性过滤）
        Demo newDemo = beanWrapper.duplicate(new Demo(), (fieldName, fieldValue) -> {
            // 排除 age 属性
            return Strings.CS.equals(fieldName, "age");
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

日期时间及格式转换工具类，提供了丰富的日期时间处理功能，基于 JDK8 日期时间 API 实现。

#### 时间常量

| 常量 | 值 | 说明 |
|------|-----|------|
| `SECOND` | 1000L | 1秒（毫秒） |
| `MINUTE` | 60_000L | 1分钟（毫秒） |
| `HOUR` | 3_600_000L | 1小时（毫秒） |
| `DAY` | 86_400_000L | 1天（毫秒） |
| `WEEK` | 604_800_000L | 1周（毫秒） |
| `YEAR` | 31_536_000_000L | 1年（按365天计算，毫秒） |

#### 日期格式常量

| 常量 | 格式 | 说明 |
|------|------|------|
| `YYYY_MM_DD_HH_MM_SS_SSS` | yyyy-MM-dd HH:mm:ss.SSS | 带毫秒的完整日期时间格式 |
| `YYYY_MM_DD_HH_MM_SS` | yyyy-MM-dd HH:mm:ss | 标准日期时间格式 |
| `YYYY_MM_DD` | yyyy-MM-dd | 日期格式 |
| `YYYY_MM` | yyyy-MM | 年月格式 |
| `YYYY_MM_DD_HH_MM` | yyyy-MM-dd HH:mm | 带时分的日期格式 |

#### 时区相关功能

```java
// 获取时区信息
String[] timeZoneArr = DateTimeUtils.TIME_ZONES.get("8");
TimeZone timeZone = DateTimeUtils.getTimeZone("8");

// 全局时间修正偏移量
DateTimeUtils.TIMEZONE_OFFSET = "8";
```

#### 当前时间获取

```java
// 获取当前日期时间
Date currentTime = DateTimeUtils.currentTime();

// 获取当前时间毫秒值
long currentTimeMillis = DateTimeUtils.currentTimeMillis();

// 获取当前 UTC 时间（秒）
long currentTimeUTC = DateTimeUtils.currentTimeUTC();

// 获取当前系统 UTC 时间（秒，int类型）
int systemTimeUTC = DateTimeUtils.systemTimeUTC();
```

#### 日期时间格式化

```java
// 格式化输出时间字符串（使用默认格式）
String dateTimeStr = DateTimeUtils.formatTime(System.currentTimeMillis());

// 格式化输出时间字符串（指定格式）
String dateTimeStr = DateTimeUtils.formatTime(System.currentTimeMillis(), DateTimeUtils.YYYY_MM_DD_HH_MM_SS_SSS);

// 格式化输出时间字符串（指定格式和时区）
String dateTimeStr = DateTimeUtils.formatTime(System.currentTimeMillis(), DateTimeUtils.YYYY_MM_DD_HH_MM_SS, "8");

// 格式化 UTC 时间（自动识别并转换）
String dateTimeStr = DateTimeUtils.formatTime(1635246720, DateTimeUtils.YYYY_MM_DD_HH_MM_SS);
```

#### 日期时间解析

```java
// 解析格式化时间字符串为日期对象
Date date = DateTimeUtils.parseDateTime("2021-10-26 13:52:00", DateTimeUtils.YYYY_MM_DD_HH_MM_SS);

// 解析格式化时间字符串为日期对象（指定时区）
Date date = DateTimeUtils.parseDateTime("2021-10-26 13:52:00", DateTimeUtils.YYYY_MM_DD_HH_MM_SS, "8");

// 解析 ISO 8601 格式的日期时间字符串（自动识别）
Date date = DateTimeUtils.parseDateTime("2021-10-26T13:52:00Z");
```

#### 闰年判断

```java
// 判断指定年份是否闰年
boolean isLeap = DateTimeUtils.isLeapYear(2021);
System.out.println("2021 is leap year: " + isLeap); // false

boolean isLeap2020 = DateTimeUtils.isLeapYear(2020);
System.out.println("2020 is leap year: " + isLeap2020); // true
```

#### 时间毫秒值提取

```java
// 尝试通过目标日期时间类对象提取时间毫秒值
Object o = LocalDate.now();
long millis = DateTimeUtils.timeMillis(o);

// 支持多种日期时间类型
long millis1 = DateTimeUtils.timeMillis(new Date());
long millis2 = DateTimeUtils.timeMillis(LocalDateTime.now());
long millis3 = DateTimeUtils.timeMillis(ZonedDateTime.now());
long millis4 = DateTimeUtils.timeMillis(Instant.now());
long millis5 = DateTimeUtils.timeMillis(new Timestamp(System.currentTimeMillis()));
```

#### 完整示例

```java
// 1. 获取当前时间信息
System.out.println("Current time: " + DateTimeUtils.currentTime());
System.out.println("Current time millis: " + DateTimeUtils.currentTimeMillis());
System.out.println("Current time UTC: " + DateTimeUtils.currentTimeUTC());
System.out.println("System time UTC: " + DateTimeUtils.systemTimeUTC());

// 2. 格式化时间
long currentMillis = DateTimeUtils.currentTimeMillis();
String formatted = DateTimeUtils.formatTime(currentMillis, DateTimeUtils.YYYY_MM_DD_HH_MM_SS_SSS);
System.out.println("Formatted time: " + formatted);

// 3. 解析时间
try {
    Date parsedDate = DateTimeUtils.parseDateTime(formatted, DateTimeUtils.YYYY_MM_DD_HH_MM_SS_SSS);
    System.out.println("Parsed date: " + parsedDate);
} catch (ParseException e) {
    e.printStackTrace();
}

// 4. 时区操作
TimeZone timeZone = DateTimeUtils.getTimeZone("8");
System.out.println("Time zone: " + timeZone.getID());

// 5. 闰年判断
System.out.println("2024 is leap year: " + DateTimeUtils.isLeapYear(2024));

// 6. 时间毫秒值提取
System.out.println("LocalDate millis: " + DateTimeUtils.timeMillis(LocalDate.now()));
System.out.println("LocalDateTime millis: " + DateTimeUtils.timeMillis(LocalDateTime.now()));
```

**执行结果：**

```shell
Current time: Mon Mar 03 12:00:00 CST 2026
Current time millis: 1772659200000
Current time UTC: 1772659200
System time UTC: 1772659200
Formatted time: 2026-03-03 12:00:00:000
Parsed date: Mon Mar 03 12:00:00 CST 2026
Time zone: UTC+08:00
2024 is leap year: true
LocalDate millis: 1772601600000
LocalDateTime millis: 1772659200000
```

#### 最佳实践

1. **使用常量**：使用 DateTimeUtils 中定义的时间常量和日期格式常量，保持代码一致性
2. **时区处理**：在处理跨时区业务时，明确指定时区偏移
3. **格式选择**：根据业务需求选择合适的日期时间格式
4. **异常处理**：解析日期时间字符串时，务必捕获 ParseException
5. **类型转换**：使用 timeMillis() 方法方便地获取各种日期时间类型的毫秒值
6. **ISO 8601 支持**：对于国际化应用，优先使用 ISO 8601 格式的日期时间字符串

DateTimeUtils 为 YMP 框架提供了全面的日期时间处理功能，支持从简单的时间获取到复杂的日期时间解析和格式化等各种场景。

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
// 创建临时文件（尝试从指定的fileName提取其扩展名作为后缀，否则为空）
FileUtils.createTempFile("prefix", demoFile.getName());
// 提取文件扩展名称，若不存在则返回空字符串
String extName = FileUtils.getExtName(demoFile.getName());
// 获取文件MD5签名值
String hash = FileUtils.getHash(demoFile);
// 获取文件SHA1签名值
hash = FileUtils.getHash(demoFile, true);
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

> 该类在初始化时，将首先加载 `META-INF/mimetypes-default-conf.properties` 默认配置文件，然后再加载类路径下的 `mimetypes-conf.properties` 文件。



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
String[] ipAddrs = NetworkUtils.IP.getHostIPAddresses();
// 获取一个DNS或计算机名称所对应的IP地址数组
ipAddrs = NetworkUtils.IP.getHostIPAddresses("localhost");
// 获取本机名称
String hostName = NetworkUtils.IP.getHostName();
// 获取本机IPv6地址
String ipV6Addr = NetworkUtils.IP.getLocalIPAddr();
// 获取本机IPv4地址
String ipV4Addr = NetworkUtils.IP.getLocalIPv4Addr();
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



**示例：** 将 Map\<String, ?\> 转换为 Map\<String, String[]\>

```java
Map<String, Object> params = new HashMap<>();
params.put("name", "suninformation");
params.put("age", 20);
Map<String, String[]> convertMap = ParamUtils.convertParamMap(params);
```



**示例：** 解析 URL 参数并转换成 Map\<String, String[]\> 映射

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
// 默认采用 SHA1 进行签名
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
            // 构建字符串输出异常及堆栈信息
            RuntimeUtils.exceptionToString(e);
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

UUID 生成器工具类，提供多种 UUID 生成策略，满足不同场景的需求。

#### 功能特点

- 提供多种 UUID 生成策略，满足不同场景需求
- 支持自定义随机字符串生成
- 基于 ThreadLocalRandom 实现高性能随机数生成
- 支持 64 进制编码转换
- 适用于分布式系统、数据库主键、缓存键、会话 ID 等场景

#### 生成策略

| 方法 | 说明 | 适用场景 |
|------|------|----------|
| `UUID()` | 生成 JDK 标准 UUID（32位） | 通用唯一标识 |
| `generateRandomUUID()` | 生成随机 UUID（10位） | 短标识、临时 ID |
| `generateCharUUID(Object)` | 生成基于字符的 UUID（16位） | 需要字符串形式的唯一标识 |
| `generateNumberUUID(Object)` | 生成基于数字的 UUID | 需要数字形式的唯一标识 |
| `generatePrefixHostUUID(Object)` | 生成带主机名前缀的 UUID | 分布式系统中需要标识来源的场景 |
| `generateTimeBasedUUID()` | 生成基于时间戳和随机数的 UUID | 需要时间有序性的场景 |


**示例：**

```java
public class Demo {

    public static void main(String[] args) throws Exception {
        // 采用 JDK 自身 UUID 生成器生成主键并替换 '-' 字符
        System.out.println("JDK UUID: " + UUIDUtils.UUID());
        // 10个随机字符（基于当前时间和一个随机字符串）
        System.out.println("Random UUID: " + UUIDUtils.generateRandomUUID());
        // 唯一的16位字符串（基于32位当前时间和32位对象的 identityHashCode 和32位随机数）
        // （以下所传入参数为预加密字符串）
        System.out.println("Char UUID: " + UUIDUtils.generateCharUUID("12345678"));
        // 唯一的数值型随机字符串
        System.out.println("Number UUID: " + UUIDUtils.generateNumberUUID("12345678"));
        // 基于主机前缀的唯一的随机字符串
        System.out.println("Prefix Host UUID: " + UUIDUtils.generatePrefixHostUUID("12345678"));
        // 基于时间戳和随机数的 UUID
        System.out.println("Time-based UUID: " + UUIDUtils.generateTimeBasedUUID());
        // 生成指定长度的生成随机字符串（可指定是否仅生成数值型）
        System.out.println("Random String: " + UUIDUtils.randomStr(10, false));
        // 生成指定范围的整型随机数
        System.out.println("Random Int: " + UUIDUtils.randomInt(10, 50));
        // 生成指定范围的长整型随机数
        System.out.println("Random Long: " + UUIDUtils.randomLong(100, 500));
    }
}
```


**执行结果：**

```shell
JDK UUID: cd056590af2b4842a2b568143e6caa3e
Random UUID: qLZ47GSzwN
Char UUID: h8qXcRHwV^gcDsJB
Number UUID: 20030693369906894882132773383
Prefix Host UUID: hostname.local@20030693377466640857723259057
Time-based UUID: 7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2c
Random String: uKVcmRKRfT
Random Int: 32
Random Long: 184
```


#### 方法说明

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `UUID()` | 无 | `String` | 生成 JDK 标准 UUID，长度为 32 位，替换了其中的 '-' 字符 |
| `generateRandomUUID()` | 无 | `String` | 生成基于当前时间和随机字符串的 UUID，长度约为 10 位 |
| `generateCharUUID(Object o)` | o: 预加密对象 | `String` | 生成基于字符的 UUID，长度约为 16 位，基于 32 位当前时间、32 位对象的 identityHashCode 和 32 位随机数 |
| `generateNumberUUID(Object o)` | o: 预加密对象 | `String` | 生成基于数字的 UUID，长度较长，基于时间戳和随机数 |
| `generatePrefixHostUUID(Object o)` | o: 预加密对象 | `String` | 生成带主机名前缀的 UUID，格式为 "hostname@uuid" |
| `generateTimeBasedUUID()` | 无 | `String` | 生成基于纳秒时间戳和随机数的 UUID，时间有序 |
| `randomStr(int length, boolean useOnlyDigits)` | length: 长度<br/>useOnlyDigits: 是否仅使用数字 | `String` | 生成指定长度的随机字符串，可选择是否仅使用数字 |
| `randomInt(int min, int max)` | min: 最小值（包含）<br/>max: 最大值（包含） | `int` | 生成指定范围内的随机整数 |
| `randomLong(long min, long max)` | min: 最小值（包含）<br/>max: 最大值（包含） | `long` | 生成指定范围内的随机长整数 |



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
    .escape()
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

日期时间处理相关的工具类，基于 JDK8 日期时间 API 实现，使用 ZonedDateTime 作为核心日期时间表示，支持时区操作和各种日期时间计算。

#### 主要特点

- 基于 JDK8 日期时间 API，使用 ZonedDateTime 支持时区操作
- 提供丰富的日期时间计算和操作方法
- 支持链式调用，代码更简洁易读
- 兼容旧版 Date API，同时提供新版日期时间类型的支持
- 支持多种方式创建实例（当前时间、Date 对象、时间戳、字符串等）

#### 实例创建

```java
// 获取当前日期时间
DateTimeHelper now = DateTimeHelper.now();

// 绑定 Date 对象
DateTimeHelper fromDate = DateTimeHelper.bind(new Date());

// 绑定时间戳（支持秒级和毫秒级自动识别）
DateTimeHelper fromTimestamp = DateTimeHelper.bind(System.currentTimeMillis());

// 绑定日期时间字符串
DateTimeHelper fromString = DateTimeHelper.bind("2021-10-26 13:52", DateTimeUtils.YYYY_MM_DD_HH_MM);

// 绑定 LocalDateTime 对象（JDK8+）
DateTimeHelper fromLocalDateTime = DateTimeHelper.bind(LocalDateTime.now());

// 绑定年月日
DateTimeHelper fromYMD = DateTimeHelper.bind(2021, 10, 26);

// 绑定年月日时分秒
DateTimeHelper fromYMDHMS = DateTimeHelper.bind(2021, 10, 26, 13, 52, 0);
```

#### 核心功能

##### 获取日期时间信息

```java
// 获取不同类型的日期时间对象
Date date = now.time();
LocalDateTime localDateTime = now.localDateTime();
LocalDate localDate = now.localDate();
LocalTime localTime = now.localTime();
ZonedDateTime zonedDateTime = now.zonedDateTime();

// 获取时间戳
long timeMillis = now.timeMillis();
int timeUTC = now.timeUTC();

// 获取时区信息
TimeZone timeZone = now.timeZone();
ZoneId zoneId = now.zoneId();

// 获取年、月、日、时、分、秒、毫秒
int year = now.year();
int month = now.month();
int day = now.day();
int hour = now.hour();
int minute = now.minute();
int second = now.second();
int millisecond = now.millisecond();

// 获取日期相关信息
int dayOfWeek = now.dayOfWeek(); // 周日=7
int weekOfMonth = now.weekOfMonth();
int weekOfYear = now.weekOfYear();
int dayOfWeekInMonth = now.dayOfWeekInMonth();
int daysOfMonth = now.daysOfMonth();
boolean isLeapYear = now.isLeapYear();
```

##### 设置日期时间

```java
// 设置日期时间
now.time(new Date());
now.time(LocalDateTime.now());
now.timeMillis(System.currentTimeMillis());
now.timeUTC(System.currentTimeMillis() / 1000);

// 设置年、月、日、时、分、秒、毫秒
now.year(2021)
   .month(10)
   .day(26)
   .hour(13)
   .minute(52)
   .second(0)
   .millisecond(0);

// 设置时区
now.timeZone(TimeZone.getTimeZone("GMT+8"));
now.timeZone(ZoneId.of("Asia/Shanghai"));
```

##### 日期时间调整

```java
// 调整到当天开始（00:00:00:000）
now.toDayStart();

// 调整到当天结束（23:59:59:999）
now.toDayEnd();

// 调整到当月开始
now.toMonthStart();

// 调整到当月结束
now.toMonthEnd();

// 调整到当周开始（周一）
now.toWeekStart();

// 调整到当周结束（周日）
now.toWeekEnd();

// 调整到当年开始
now.toYearStart();

// 调整到当年结束
now.toYearEnd();
```

##### 日期时间计算

```java
// 日期时间加减
now.yearsAdd(1);      // 加1年
now.monthsAdd(-1);    // 减1个月
now.daysAdd(1);       // 加1天
now.hoursAdd(-1);     // 减1小时
now.minutesAdd(1);    // 加1分钟
now.secondsAdd(-1);   // 减1秒
now.millisecondsAdd(1); // 加1毫秒
now.weeksAdd(1);      // 加1周

// 计算时间差（毫秒）
long diff = now.subtract(new Date());
diff = now.subtract(DateTimeHelper.now());
diff = now.subtract(LocalDateTime.now());
diff = now.subtract(Instant.now());
diff = now.subtract(LocalDate.now());
diff = now.subtract(ZonedDateTime.now());

// 时间比较
boolean before = now.isBefore(new Date());
before = now.isBefore(DateTimeHelper.now());
before = now.isBefore(LocalDateTime.now());
before = now.isBefore(Instant.now());
before = now.isBefore(LocalDate.now());
before = now.isBefore(ZonedDateTime.now());

boolean after = now.isAfter(new Date());
after = now.isAfter(DateTimeHelper.now());
after = now.isAfter(LocalDateTime.now());
after = now.isAfter(Instant.now());
after = now.isAfter(LocalDate.now());
after = now.isAfter(ZonedDateTime.now());
```

##### 格式化输出

```java
// 默认格式（带毫秒）
String defaultFormat = now.toString();

// 自定义格式
String customFormat = now.toString(DateTimeUtils.YYYY_MM_DD_HH_MM_SS);
```

#### 完整示例

```java
// 创建实例
DateTimeHelper helper = DateTimeHelper.now();

// 输出当前时间
System.out.println("当前时间: " + helper.toString());

// 调整时间
helper.year(2021)
      .month(10)
      .day(26)
      .hour(13)
      .minute(52)
      .second(0);

// 输出调整后的时间
System.out.println("调整后: " + helper.toString());

// 计算操作
helper.daysAdd(1)
      .hoursAdd(2);

// 输出计算后的时间
System.out.println("计算后: " + helper.toString());

// 调整到当天开始
helper.toDayStart();
System.out.println("当天开始: " + helper.toString());

// 调整到当月结束
helper.toMonthEnd();
System.out.println("当月结束: " + helper.toString());

// 获取日期信息
System.out.println("年份: " + helper.year());
System.out.println("月份: " + helper.month());
System.out.println("日期: " + helper.day());
System.out.println("是否闰年: " + helper.isLeapYear());
System.out.println("当月天数: " + helper.daysOfMonth());
System.out.println("周几: " + helper.dayOfWeek());
System.out.println("当月第几周: " + helper.weekOfMonth());
System.out.println("当年第几周: " + helper.weekOfYear());
```

**执行结果：**

```shell
当前时间: 2026-03-03 12:00:00:000
调整后: 2021-10-26 13:52:00:000
计算后: 2021-10-27 15:52:00:000
当天开始: 2021-10-27 00:00:00:000
当月结束: 2021-10-31 23:59:59:999
年份: 2021
月份: 10
日期: 31
是否闰年: false
当月天数: 31
周几: 7
当月第几周: 5
当年第几周: 44
```

#### 最佳实践

1. **链式调用**：充分利用链式调用特性，使代码更简洁
2. **时区处理**：在处理跨时区业务时，明确设置时区信息
3. **类型转换**：根据需要选择合适的日期时间类型（Date、LocalDateTime、ZonedDateTime等）
4. **性能考虑**：对于频繁的日期时间操作，考虑缓存 DateTimeHelper 实例
5. **格式化**：使用 DateTimeUtils 中定义的标准格式常量，保持格式一致性

DateTimeHelper 为 YMP 框架提供了强大、灵活的日期时间处理能力，支持从简单的日期时间获取到复杂的日期时间计算等各种场景。


### ExcelFileAnalysisHelper

Excel 文件数据导入助手类，用于辅助操作和分析 Excel 文件中各 Sheet 页的表格数据，使用时需在工程中引入以下依赖包：

```xml
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.4.0</version>
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
    <version>5.4.0</version>
</dependency>
<dependency>
    <groupId>org.jxls</groupId>
    <artifactId>jxls-poi</artifactId>
    <version>2.14.0</version>
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

- 模板文件的扩展名必须是 `.xls` 或 `.xlsx` 。
- 模板文件路径不包含扩展名将自动补全并优化查找 `.xls` 文件。
- 模板文件路径支持使用 `${root}`、`${user.dir}`、`${user.home}` 等环境变量。

此方式不支持上例中的 @ExportColumn 注解，须提前定义模板文件，关于更多 JXLS 的使用方法，请访问：

[GitHub - jxlsteam/jxls: Java library for creating Excel reports using Excel templates](https://github.com/jxlsteam/jxls)

:::



```java
public static void main(String[] args) throws Exception {
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
                return Strings.CI.equals(name, "content");
            }
        });
        // ......
    }
}
```

