---
sidebar_position: 3
slug: configuration
---

# 配置体系 (Configuration)

配置体系模块，是通过简单的目录结构实现在项目开发以及维护过程中，对配置文件等各种资源的统一管理，为模块化开发和部署提供灵活的、简单有效的解决方案。



## Maven包依赖

```xml
<dependency>
    <groupId>net.ymate.platform</groupId>
    <artifactId>ymate-platform-configuration</artifactId>
    <version>2.1.2</version>
</dependency>
```



## 特点

- 从开发角度规范了模块化开发流程、统一资源文件的生命周期管理；
- 从可维护角度将全部资源集成在整个体系中，具备有效的资源重用和灵活的系统集成构建、部署和数据备份与迁移等优势；
- 简单的配置文件检索、加载及管理模式；
- 模块间资源共享，模块（modules）可以共用所属项目（projects）的配置、类和包等资源文件；
- 默认支持对 XML、Properties 和 JSON 配置文件的解析，可以通过 IConfigurationProvider 接口自定义文件格式，支持缓存，避免重复加载；
- 配置对象支持 @Configuration 注解方式声明，无需编码即可自动加载并填充配置内容到类对象；
- 修改配置文件无需重启服务，支持自动重新加载；
- 集成模块的构建（编译）与分发、服务的启动与停止，以及清晰的资源文件分类结构可快速定位；



## 配置体系目录结构

按优先级由低到高的顺序依次是：全局（configHome）> 项目（projects）> 模块（modules）：


```shell
CONFIG_HOME\
    |--bin\
    |--cfgs\
    |--classes\
    |--dist\
    |--lib\
    |--logs\
    |--plugins\
    |--projects\
    |   |--<project_xxx>
    |   |   |--cfgs\
    |   |   |--classes\
    |   |   |--lib\
    |   |   |--logs\
    |   |   |--modules\
    |   |   |   |--<module_xxx>
    |   |   |   |   |--cfgs\
    |   |   |   |   |--classes\
    |   |   |   |   |--lib\
    |   |   |   |   |--logs\
    |   |   |   |   |--plugins\
    |   |   |   |   |--<......>
    |   |   |   |--<......>
    |   |   |--plugins\
    |   |--<......>
    |--temp\
    |--......
```



## 模块配置

### 配置文件参数说明


```properties
#-------------------------------------
# 配置体系模块初始化参数
#-------------------------------------

# 配置体系根路径, 必须绝对路径, 前缀支持${root} ${user.home}和${user.dir}变量, 默认值: ${root}
ymp.configs.configuration.config_home=${user.dir}

# 项目名称, 做为根路径下级子目录, 对现实项目起分类作用, 默认值: 空
ymp.configs.configuration.project_name=

# 模块名称, 此模块一般指现实项目中分拆的若干子项目的名称, 默认为空
ymp.configs.configuration.module_name=

# 配置文件存放的基准目录名称，不允许以'/'开头但必须以'/'结束，默认值为空
ymp.configs.configuration.config_base_dir=

# 配置文件检查时间间隔(毫秒), 默认值为0表示不开启
ymp.configs.configuration.config_check_time_interval=

# 指定配置体系下的默认配置文件分析器, 默认值: net.ymate.platform.configuration.impl.DefaultConfigurationProvider
ymp.configs.configuration.provider_class=
```

:::tip **注意**：

配置体系根路径 `config_home` 配置项参数，可以通过 `JVM` 启动参数方式进行配置，这种方式将优先于配置文件，如：

````shell
java -jar -Dymp.configHome=...
````
若配置项参数和`JVM`启动参数均未设置时，将尝试加载 `YMP_CONFIG_HOME` 环境变量，若环境变量也不存在，则使用默认值：`${root}`

:::



### 配置注解参数说明

#### @ConfigurationConf

| 配置项            | 描述                         |
| ----------------- | ---------------------------- |
| configHome        | 配置体系根路径               |
| projectName       | 项目名称                     |
| moduleName        | 模块名称                     |
| configBaseDir     | 配置文件基准目录名称           |
| checkTimeInterval | 配置文件检查时间间隔（毫秒） |
| providerClass     | 默认配置文件分析器           |



## 模块使用

### 相关注解及参数说明

#### @Configuration 

用于指定一个类为配置类，以及配置文件加载路径等相关设置。

| 配置项   | 描述                                                         |
| -------- | ------------------------------------------------------------ |
| value    | 配置文件路径名称，若未提供则默认使用 `简单类名称小写.TAG.扩展名` 作为配置文件名 |
| reload   | 是否自动重新加载                                             |
| provider | 配置文件自定义内容分析器                                     |

#### @Configs 

用于辅助 `@ConfigValue` 注解设置对 `configs` 和 `category` 参数进行全局配置。

| 配置项   | 描述                             |
| -------- | -------------------------------- |
| value    | 配置类集合                       |
| category | 配置分类名称, 默认值为 `default` |

#### @ConfigValue

配置注入，用于读取配置项参数值为类成员变量或方法参数赋值。

| 配置项       | 描述                                             |
| ------------ | ------------------------------------------------ |
| value        | 配置项名称, 若未提供则使用成员变量或方法参数名称 |
| defaultValue | 配置项默认值                                     |
| category     | 配置分类名称, 默认值为 `default`                 |
| configs      | 配置类集合                                       |



### 示例一：解析XML配置

- 基于 XML 文件的基础配置格式如下，为了配合测试代码，请将该文件命名为 `configuration.xml` 并放置在 `config_home` 路径下的 `cfgs` 目录里：


```xml
<?xml version="1.0" encoding="UTF-8"?>
<!-- XML根节点为properties -->
<properties>

  <!-- 分类节点为category, 默认分类名称为default -->
  <category name="default">

    <!-- 属性标签为property, name代表属性名称, value代表属性值(也可以用property标签包裹) -->
    <property name="company_name" value="Apple Inc."/>

    <!-- 用属性标签表示一个数组或集合数据类型的方法 -->
    <property name="products">
      <!-- 集合元素必须用value标签包裹, 且value标签不要包括任何扩展属性 -->
      <value>iphone</value>
      <value>ipad</value>
      <value>imac</value>
      <value>itouch</value>
    </property>

    <!-- 用属性标签表示一个MAP数据类型的方法, abc代表扩展属性key, xyz代表扩展属性值, 扩展属性与item将被合并处理  -->
    <property name="product_spec" abc="xzy">
      <!-- MAP元素用item标签包裹, 且item标签必须包含name扩展属性(其它扩展属性将被忽略), 元素值由item标签包裹 -->
      <item name="color">red</item>
      <item name="weight">120g</item>
      <item name="size">small</item>
      <item name="age">2015</item>
    </property>
  </category>
</properties>
```


- 新建配置类 `DemoConfig`, 通过 `@Configuration` 注解指定配置文件相对路径：


```java
@Configuration(value = "cfgs/configuration.xml", reload = true)
public class DemoConfig extends DefaultConfiguration {
}
```


- 测试代码, 完成模块初始化并加载配置文件内容：


```java
@EnableAutoScan
@ConfigurationConf(configHome = "${user.dir}/configs", checkTimeInterval = 0)
public class Starter {

    static {
        System.setProperty(IApplication.SYSTEM_MAIN_CLASS, Starter.class.getName());
    }

    public static void main(String[] args) throws Exception {
        try (IApplication application = YMP.run(args)) {
            DemoConfig _cfg = new DemoConfig();
            if (Cfgs.get().fillCfg(_cfg)) {
                System.out.println(_cfg.getString("company_name"));
                System.out.println(_cfg.getMap("product_spec"));
                System.out.println(_cfg.getList("products"));
            }
        }
    }
}
```

- 执行结果：


```shell
Apple Inc.
{abc=xzy, color=red, size=small, weight=120g, age=2015}
[itouch, imac, ipad, iphone]
```



### 示例二：解析Properties配置

- 基于 Properties 文件的基础配置格式如下，同样请将该文件命名为 `configuration.properties` 并放置在 `config_home` 路径下的 `cfgs` 目录里：


```properties
#--------------------------------------------------------------------------
# 配置文件内容格式: properties.<categoryName>.<propertyName>=[propertyValue]
#
# 注意: attributes将作为关键字使用, 用于表示分类, 属性, 集合和MAP的子属性集合
#--------------------------------------------------------------------------

# 举例1: 默认分类下表示公司名称, 默认分类名称为default
properties.default.company_name=Apple Inc.

#--------------------------------------------------------------------------
# 数组和集合数据类型的表示方法: 多个值之间用'|'分隔, 如: Value1|Value2|...|ValueN
#--------------------------------------------------------------------------
properties.default.products=iphone|ipad|imac|itouch

#--------------------------------------------------------------------------
# MAP<K, V>数据类型的表示方法:
# 如:产品规格(product_spec)的K分别是color|weight|size|age, 对应的V分别是热red|120g|small|2015
#--------------------------------------------------------------------------
properties.default.product_spec.color=red
properties.default.product_spec.weight=120g
properties.default.product_spec.size=small
properties.default.product_spec.age=2015

# 每个MAP都有属于其自身的属性列表(深度仅为一级), 用attributes表示, abc代表属性key, xyz代表属性值
# 注: MAP数据类型的attributes和MAP本身的表示方法达到的效果是一样的
properties.default.product_spec.attributes.abc=xyz
```


- 修改配置类 `DemoConfig` 如下，通过 `@ConfigurationProvider` 注解指定配置文件内容解析器：


```java
@Configuration(value = "cfgs/configuration.properties", provider = PropertyConfigurationProvider.class)
public class DemoConfig extends DefaultConfiguration {
}
```


- 重新执行示例代码，执行结果与示例一结果相同：


```shell
Apple Inc.
{abc=xzy, color=red, size=small, weight=120g, age=2015}
[itouch, imac, ipad, iphone]
```



### 示例三：解析JSON配置

- 基于 JSON 文件的基础配置格式如下，同样请将该文件命名为 `configuration.json` 并放置在 `config_home` 路径下的 `cfgs` 目录里：

```json
{
    "categories": [
        {
            "name": "default",
            "properties": [
                {
                    "name": "company_name",
                    "content": "Apple Inc.",
                    "attributes": {}
                },
                {
                    "name": "products",
                    "content": [
                        "iphone",
                        "ipad",
                        "imac",
                        "itouch"
                    ],
                    "attributes": {}
                },
                {
                    "name": "product_spec",
                    "content": "spec.",
                    "attributes": {
                        "abc": "xzy",
                        "color": "red",
                        "weight": 120,
                        "size": "small",
                        "year": 2015
                    }
                }
            ],
            "attributes": {}
        }
    ]
}
```

- 修改配置类 `DemoConfig` 如下，通过 `@ConfigurationProvider` 注解指定配置文件内容解析器：

```java
@Configuration(value = "cfgs/configuration.json", provider = JSONConfigurationProvider.class)
public class DemoConfig extends DefaultConfiguration {
}
```



### 示例四：无需创建配置对象, 直接加载配置文件

```java
@EnableAutoScan
@ConfigurationConf(configHome = "${user.dir}/configs", checkTimeInterval = 0)
public class Starter {

    static {
        System.setProperty(IApplication.SYSTEM_MAIN_CLASS, Starter.class.getName());
    }

    public static void main(String[] args) throws Exception {
        try (IApplication application = YMP.run(args)) {
            IConfiguration cfg = Cfgs.get().loadCfg("cfgs/configuration.properties");
            if (cfg != null) {
                System.out.println(cfg.getString("company_name"));
                System.out.println(cfg.getMap("product_spec"));
                System.out.println(cfg.getList("products"));
            }
        }
    }
}
```



### 示例五：直接注入配置值

本例将演示如何直接通过 `@Configs` 和 `@ConfigValue` 注解注入 `DemoConfig` 配置对象中的配置值：

```java
@Bean
@Configs(DemoConfig.class)
public class Demo {

    @ConfigValue("company_name")
    private String companyName;

    @ConfigValue("product_spec")
    private Map<String, String> productSpec;

    @ConfigValue("products")
    private List<String> products;

    public String getCompanyName() {
        return companyName;
    }

    public Map<String, String> getProductSpec() {
        return productSpec;
    }

    public List<String> getProducts() {
        return products;
    }
}

@EnableAutoScan
@ConfigurationConf(configHome = "${user.dir}/configs", checkTimeInterval = 0)
public class Starter {

    static {
        System.setProperty(IApplication.SYSTEM_MAIN_CLASS, Starter.class.getName());
    }

    public static void main(String[] args) throws Exception {
        try (IApplication application = YMP.run(args)) {
            Demo demo = application.getBeanFactory().getBean(Demo.class);
            System.out.println(demo.getCompanyName());
            System.out.println(demo.getProductSpec());
            System.out.println(demo.getProducts());
        }
    }
}
```

:::tip 注意：

目前该方式仅支持以下数据类型的注入：

- 基础数据类型：如：String、Long、Integer、Long、Double等；
- 基础数据类型数组：如：String[]、Long[]、Integer[]、Long[]、Double[]等；
- 集合类型：如：List<String\>等；
- 映射类型：如：Map<String, String>等；

:::



### 配置体系模块更多操作

#### 获取路径信息

```java
// 返回配置体系根路径
Cfgs.get().getConfigHome();

// 返回项目根路径
Cfgs.get().getProjectHome();

// 返回项目模块根路径
Cfgs.get().getModuleHome();

// 返回user.dir所在路径
Cfgs.get().getUserDir();

// 返回user.home所在路径
Cfgs.get().getUserHome();
```

#### 搜索目标文件

```java
// 在配置体系中搜索cfgs/configuration.xml文件并返回其File对象
Cfgs.get().searchAsFile("cfgs/configuration.xml");

// 在配置体系中搜索cfgs/configuration.properties文件并返回其绝对路径
Cfgs.get().searchAsPath("cfgs/configuration.properties");

// 在配置体系中搜索cfgs/configuration.json文件并返回其文件流
Cfgs.get().searchAsStream("cfgs/configuration.properties");
```

