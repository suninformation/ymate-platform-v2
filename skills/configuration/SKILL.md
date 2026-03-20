---
name: ymp-configuration
description: YMP框架配置模块，提供统一的配置文件管理，支持XML、Properties、JSON等多种格式，支持注解驱动配置和动态配置重载
version: 2.1.4
author: YMP Team
category: configuration
tags:
  - java
  - configuration
  - xml
  - properties
  - json
  - dynamic-config
trigger: 当用户需要管理配置文件、加载配置、实现动态配置等功能时触发
tools:
  - configuration-management
  - file-parsing
  - dynamic-reload
examples:
  - 加载XML配置文件
  - 加载Properties配置文件
  - 加载JSON配置文件
  - 使用注解注入配置
  - 实现动态配置重载
---

# YMP框架配置模块技能文档

## 1. 模块概述

配置体系模块，是通过简单的目录结构实现在项目开发以及维护过程中，对配置文件等各种资源的统一管理，为模块化开发和部署提供灵活的、简单有效的解决方案。

## 2. 核心功能

### 2.1 统一资源管理
- 规范化的目录结构，便于资源的组织和管理
- 模块间资源共享，模块（modules）可以共用所属项目（projects）的配置、类和包等资源文件
- 清晰的资源文件分类结构，可快速定位所需资源

### 2.2 多格式配置支持
- 默认支持对 XML、Properties 和 JSON 配置文件的解析
- 可通过 IConfigurationProvider 接口自定义文件格式
- 支持缓存，避免重复加载配置文件

### 2.3 注解驱动配置
- 配置对象支持 @Configuration 注解方式声明，无需编码即可自动加载并填充配置内容到类对象
- 支持通过 @ConfigValue 注解直接注入配置值到类成员变量
- 支持通过 @Configs 注解设置配置类集合

### 2.4 动态配置
- 修改配置文件无需重启服务，支持自动重新加载
- 配置文件检查时间间隔可配置，确保及时发现配置变更

### 2.5 灵活的配置路径
- 支持全局（configHome）> 项目（projects）> 模块（modules）的多级配置路径
- 支持环境变量替换，如 ${root}、${user.home} 和 ${user.dir} 等

## 3. 技术架构

### 3.1 核心接口

| 接口名称 | 描述 | 实现类 |
|---------|------|-------|
| `IConfiguration` | 配置对象接口，提供配置值的获取方法 | `DefaultConfiguration` |
| `IConfigurationProvider` | 配置文件提供者接口，负责配置文件的解析和加载 | `DefaultConfigurationProvider`, `JSONConfigurationProvider`, `PropertyConfigurationProvider` |
| `IConfigFileParser` | 配置文件解析器接口，负责解析不同格式的配置文件 | `XMLConfigFileParser`, `PropertyConfigFileParser`, `JSONConfigFileParser` |
| `IConfigurationConfig` | 配置模块配置接口，提供配置模块的配置信息 | `DefaultConfigurationConfig` |

### 3.2 架构层次

1. **API层**：提供 `Cfgs` 静态工具类，简化配置操作
2. **核心层**：包含配置对象、配置提供者、配置文件解析器等核心组件
3. **实现层**：包含不同格式配置文件的解析器实现
4. **集成层**：与YMP框架集成，支持注解驱动配置

## 4. API接口

### 4.1 核心接口

#### IConfiguration 接口

```java
// 获取指定分类下的所有属性名称
Set<String> getPropertyNames(String category);

// 获取指定分类下的所有属性映射
Map<String, Object> getProperties(String category);

// 获取指定分类下的指定属性值
Object getProperty(String category, String key);

// 获取默认分类下的指定属性值
Object getProperty(String key);

// 获取指定分类下的指定属性值，并转换为字符串
String getString(String category, String key);

// 获取默认分类下的指定属性值，并转换为字符串
String getString(String key);

// 获取指定分类下的指定属性值，并转换为字符串列表
List<String> getList(String category, String key);

// 获取默认分类下的指定属性值，并转换为字符串列表
List<String> getList(String key);

// 获取指定分类下的指定属性值，并转换为映射
Map<String, String> getMap(String category, String key);

// 获取默认分类下的指定属性值，并转换为映射
Map<String, String> getMap(String key);

// 获取配置文件最后修改时间
long getLastModified();

// 检查配置文件是否已被修改
boolean isModified();

// 重新加载配置文件
void reload();
```

#### IConfigurationProvider 接口

```java
// 获取配置文件搜索器
IConfigFileSearcher getConfigFileSearcher();

// 获取配置文件检查器
IConfigFileChecker getConfigFileChecker();

// 加载指定路径的配置文件
IConfiguration loadConfiguration(String cfgFileName);

// 加载指定路径的配置文件，并指定字符编码
IConfiguration loadConfiguration(String cfgFileName, String encoding);

// 加载指定文件对象的配置文件
IConfiguration loadConfiguration(File cfgFile);

// 加载指定文件对象的配置文件，并指定字符编码
IConfiguration loadConfiguration(File cfgFile, String encoding);

// 解析指定输入流的配置内容
IConfiguration loadConfiguration(InputStream inputStream, String encoding);
```

### 4.2 注解接口

#### @Configuration 注解

```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Configuration {
    /**
     * 配置文件路径名称，若未提供则默认使用 简单类名称小写.TAG.扩展名 作为配置文件名
     */
    String value() default "";

    /**
     * 是否自动重新加载
     */
    boolean reload() default false;

    /**
     * 配置文件自定义内容分析器
     */
    Class<? extends IConfigurationProvider> provider() default DefaultConfigurationProvider.class;
}
```

#### @ConfigValue 注解

```java
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigValue {
    /**
     * 配置项名称, 若未提供则使用成员变量或方法参数名称
     */
    String value() default "";

    /**
     * 配置项默认值
     */
    String defaultValue() default "";

    /**
     * 配置分类名称, 默认值为 default
     */
    String category() default "default";

    /**
     * 配置类集合
     */
    Class<?>[] configs() default {};
}
```

#### @Configs 注解

```java
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Configs {
    /**
     * 配置类集合
     */
    Class<?>[] value();

    /**
     * 配置分类名称, 默认值为 default
     */
    String category() default "default";
}
```

#### @ConfigurationConf 注解

```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigurationConf {
    /**
     * 配置体系根路径, 必须绝对路径, 前缀支持${root} ${user.home}和${user.dir}变量, 默认值: ${root}
     */
    String configHome() default "${root}";

    /**
     * 项目名称, 做为根路径下级子目录, 对现实项目起分类作用, 默认值: 空
     */
    String projectName() default "";

    /**
     * 模块名称, 此模块一般指现实项目中分拆的若干个子项目的名称, 默认为空
     */
    String moduleName() default "";

    /**
     * 配置文件存放的基准目录名称，不允许以'/'开头但必须以'/'结束，默认值为空
     */
    String configBaseDir() default "";

    /**
     * 配置文件检查时间间隔(毫秒), 默认值为0表示不开启
     */
    long checkTimeInterval() default 0;

    /**
     * 指定配置体系下的默认配置文件分析器, 默认值: net.ymate.platform.configuration.impl.DefaultConfigurationProvider
     */
    String providerClass() default "";
}
```

## 5. 配置方式

### 5.1 配置文件参数

```properties
#-------------------------------------
# 配置体系模块初始化参数
#-------------------------------------

# 配置体系根路径, 必须绝对路径, 前缀支持${root} ${user.home}和${user.dir}变量, 默认值: ${root}
ymp.configs.configuration.config_home=${user.dir}

# 项目名称, 做为根路径下级子目录, 对现实项目起分类作用, 默认值: 空
ymp.configs.configuration.project_name=

# 模块名称, 此模块一般指现实项目中分拆的若干个子项目的名称, 默认为空
ymp.configs.configuration.module_name=

# 配置文件存放的基准目录名称，不允许以'/'开头但必须以'/'结束，默认值为空
ymp.configs.configuration.config_base_dir=

# 配置文件检查时间间隔(毫秒), 默认值为0表示不开启
ymp.configs.configuration.config_check_time_interval=

# 指定配置体系下的默认配置文件分析器, 默认值: net.ymate.platform.configuration.impl.DefaultConfigurationProvider
ymp.configs.configuration.provider_class=
```

### 5.2 配置体系目录结构

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

## 6. 使用示例

### 6.1 基本配置解析

#### XML配置示例

**配置文件（configuration.xml）：**

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

**配置类：**

```java
@Configuration(value = "cfgs/configuration.xml", reload = true)
public class DemoConfig extends DefaultConfiguration {
}
```

**使用代码：**

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

#### Properties配置示例

**配置文件（configuration.properties）：**

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
# 如:产品规格(product_spec)的K分别是color|weight|size|age, 对应的V分别是red|120g|small|2015
#--------------------------------------------------------------------------
properties.default.product_spec.color=red
properties.default.product_spec.weight=120g
properties.default.product_spec.size=small
properties.default.product_spec.age=2015

# 每个MAP都有属于其自身的属性列表(深度仅为一级), 用attributes表示, abc代表属性key, xyz代表属性值
# 注: MAP数据类型的attributes和MAP本身的表示方法达到的效果是一样的
properties.default.product_spec.attributes.abc=xyz
```

**配置类：**

```java
@Configuration(value = "cfgs/configuration.properties", provider = PropertyConfigurationProvider.class)
public class DemoConfig extends DefaultConfiguration {
}
```

#### JSON配置示例

**配置文件（configuration.json）：**

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

**配置类：**

```java
@Configuration(value = "cfgs/configuration.json", provider = JSONConfigurationProvider.class)
public class DemoConfig extends DefaultConfiguration {
}
```

### 6.2 直接加载配置文件

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

### 6.3 直接注入配置值

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

### 6.4 配置路径操作

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

// 在配置体系中搜索cfgs/configuration.xml文件并返回其File对象
Cfgs.get().searchAsFile("cfgs/configuration.xml");

// 在配置体系中搜索cfgs/configuration.properties文件并返回其绝对路径
Cfgs.get().searchAsPath("cfgs/configuration.properties");

// 在配置体系中搜索cfgs/configuration.json文件并返回其文件流
Cfgs.get().searchAsStream("cfgs/configuration.properties");
```

## 7. 注意事项

1. **配置文件路径**：配置文件路径应该使用相对于配置体系根路径的相对路径

2. **配置文件格式**：不同格式的配置文件有不同的语法规则，需要按照对应格式的语法编写

3. **配置文件编码**：默认使用UTF-8编码，确保配置文件保存为正确的编码格式

4. **环境变量**：配置文件中可以使用环境变量，如 ${root}、${user.home} 等

5. **配置文件检查**：设置合理的配置文件检查时间间隔，避免频繁检查影响性能

6. **配置优先级**：全局配置 < 项目配置 < 模块配置，高优先级会覆盖低优先级的配置

7. **配置注入**：使用@ConfigValue注解时，需要确保配置类已经被正确初始化

8. **配置类型转换**：配置值默认以字符串形式存储，需要根据需要进行类型转换

9. **配置文件解析**：自定义配置文件格式时，需要实现对应的IConfigurationProvider接口

10. **配置文件加载**：配置文件加载失败时，会使用默认值或抛出异常，需要做好异常处理

## 8. 最佳实践

1. **配置文件组织**：
   - 按功能模块划分配置文件
   - 使用清晰的命名规范
   - 合理使用分类（category）组织配置项

2. **配置值管理**：
   - 对于频繁使用的配置值，使用@ConfigValue注解注入
   - 对于复杂的配置结构，使用配置类封装
   - 对于需要动态更新的配置，开启配置文件检查

3. **配置安全性**：
   - 敏感配置（如数据库密码）应加密存储
   - 避免在配置文件中硬编码敏感信息
   - 使用环境变量或外部配置源管理敏感信息

4. **配置可维护性**：
   - 添加详细的配置项注释
   - 使用统一的配置命名规范
   - 定期清理无用的配置项

5. **配置扩展性**：
   - 设计可扩展的配置结构
   - 支持配置项的默认值
   - 提供配置项的验证机制

6. **性能优化**：
   - 合理设置配置文件检查时间间隔
   - 避免过多的配置文件和配置项
   - 使用配置缓存减少IO操作

7. **测试友好**：
   - 支持不同环境的配置文件（dev、test、prod）
   - 提供配置项的默认值
   - 支持配置的程序化修改用于测试

## 9. 总结

配置模块是YMP框架中一个基础但重要的组件，通过提供统一的配置管理机制，简化了应用的配置管理，提高了应用的可维护性和可扩展性。合理使用配置模块，可以使应用的配置更加灵活、安全和易于管理。

开发者应该根据具体的业务场景，选择合适的配置方式和组织结构，遵循最佳实践，充分发挥配置模块的优势，构建更加健壮和可维护的应用系统。
