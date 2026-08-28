---
name: ymp-configuration
description: 配置体系模块，统一管理XML/Properties/JSON/YAML配置文件，支持注解驱动配置注入、动态重载
version: 2.1.4-dev
author: YMP Team
category: configuration
tags:
  - java
  - configuration
  - xml
  - properties
  - json
  - yaml
  - dynamic-reload
trigger: 当需要管理配置文件、解析YAML/JSON/Properties/XML格式配置、@Configuration配置类、@ConfigValue注入、动态配置重载、配置体系目录结构时触发
tools:
  - configuration-management
  - file-parsing
  - dynamic-reload
examples:
  - 创建@Configuration配置类并开启reload自动重载
  - 在@Bean服务中用@Configs和@ConfigValue注入配置值
  - 加载XML/Properties/JSON/YAML格式配置文件
  - 通过Cfgs.searchAsFile搜索配置体系中的文件
  - 设置ConfigurationConf的configHome和checkTimeInterval
---

# Configuration 配置体系技能包

> AI读取指引：需要统一管理多格式配置文件、注解式配置声明、配置值自动注入时使用；常配合core模块的@Bean/@ConfigValue/@Configs做依赖注入，独立使用通过Cfgs静态入口。

---

## 0. 快速索引

- Maven artifactId：`ymate-platform-configuration`
- 静态入口类（全限定名）：`net.ymate.platform.configuration.Cfgs`
- 必备注解：`@Configuration` / `@ConfigurationConf` / `@Configs` / `@ConfigValue`
- 3行最简调用示例：

```java
@Configuration(value = "cfgs/app.xml", reload = true)
public class AppConfig extends DefaultConfiguration {}

Cfgs.get().fillCfg(new AppConfig());
```

## 1. 模块摘要

通过规范的目录结构（全局>项目>模块三级优先级）实现配置文件等资源的统一管理，为模块化开发提供灵活部署方案。核心能力：
- 默认支持 XML、Properties、JSON、YAML 四种配置文件解析（YAML 依赖 SnakeYAML 类库，该类库为可选依赖需自行引入，缺失时跳过 YAML 加载且不影响其它格式），可通过 `IConfigurationProvider` 自定义扩展
- `@Configuration` 注解声明配置类，无需编码即可自动加载填充
- `@Configs` + `@ConfigValue` 直接向 Bean 字段或方法参数注入配置值
- 修改配置文件无需重启，支持 `checkTimeInterval` 定时检查自动重新加载
- 配置搜索支持 `searchAsFile` / `searchAsPath` / `searchAsStream` 三种定位方式

## 2. 核心注解/类 速查表（必须带全限定名）

### 注解

| 注解 | 全限定名 | 作用 | 核心参数（只列2-5个） |
|---|---|---|---|
| `@Configuration` | `net.ymate.platform.configuration.annotation.Configuration` | 声明类为配置类，绑定配置文件路径 | `value`(路径)、`reload`(是否自动重载)、`provider`(自定义解析器) |
| `@ConfigurationConf` | `net.ymate.platform.configuration.annotation.ConfigurationConf` | 配置模块全局参数（启动类上用） | `configHome`、`projectName`、`moduleName`、`checkTimeInterval`、`providerClass` |
| `@Configs` | `net.ymate.platform.configuration.annotation.Configs` | 配合@ConfigValue全局声明配置类集合和category | `value`(配置类Class[])、`category`(分类名，默认default) |
| `@ConfigValue` | `net.ymate.platform.configuration.annotation.ConfigValue` | 字段/参数级配置值注入 | `value`(配置项名)、`defaultValue`、`category`、`configs`(配置类数组) |

### 常用类

| 类名 | 全限定名 | 核心用途 | 最常用的2-3个方法签名 |
|---|---|---|---|
| `Cfgs` | `net.ymate.platform.configuration.Cfgs` | 配置体系静态入口 | `fillCfg(IConfiguration cfg)` / `loadCfg(String path)` / `searchAsFile(String path)` |
| `DefaultConfiguration` | `net.ymate.platform.configuration.impl.DefaultConfiguration` | 配置对象默认基类，所有@Configuration类继承它 | `getString(String key)` / `getList(String key)` / `getMap(String key)` |
| `DefaultConfigurationProvider` | `net.ymate.platform.configuration.impl.DefaultConfigurationProvider` | XML格式默认解析器 | 无需直接调用，@Configuration的provider默认值 |
| `PropertyConfigurationProvider` | `net.ymate.platform.configuration.impl.PropertyConfigurationProvider` | Properties格式解析器 | @Configuration的provider设为此Class |
| `JSONConfigurationProvider` | `net.ymate.platform.configuration.impl.JSONConfigurationProvider` | JSON格式解析器 | @Configuration的provider设为此Class |
| `YAMLConfigurationProvider` | `net.ymate.platform.configuration.impl.YAMLConfigurationProvider` | YAML格式解析器（依赖SnakeYAML） | @Configuration的provider设为此Class；文件扩展名为.yaml/.yml时按扩展名自动路由 |
| `IConfiguration` | `net.ymate.platform.core.configuration.IConfiguration` | 配置对象接口 | `getString(String category, String key)` / `reload()` / `isModified()` |

## 3. 核心API速查（≤8条最常用调用）

1. **填充配置对象**：`Cfgs.get().fillCfg(new DemoConfig())` → 返回boolean表示是否成功
2. **直接加载配置**：`IConfiguration cfg = Cfgs.get().loadCfg("cfgs/configuration.properties")`
3. **获取路径**：`Cfgs.get().getConfigHome()` / `getProjectHome()` / `getModuleHome()` / `getUserDir()`
4. **搜索文件**：`Cfgs.get().searchAsFile("cfgs/db.xml")` / `searchAsPath(...)` / `searchAsStream(...)`
5. **取配置值-字符串**：`cfg.getString("company_name")` → 默认category=default
6. **取配置值-集合**：`cfg.getList("products")` 返回 `List<String>`
7. **取配置值-Map**：`cfg.getMap("product_spec")` 返回 `Map<String, String>`
8. **重载检查**：`cfg.isModified()` 检测文件变化 + `cfg.reload()` 强制重新加载

## 4. 标准代码模板（最少可运行）

### 模板1：@Configuration注解的配置类 + reload=true

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
package com.example.config;

import net.ymate.platform.configuration.annotation.Configuration;
import net.ymate.platform.configuration.impl.DefaultConfiguration;

/**
 * 应用配置类，绑定XML配置文件并开启自动重载。
 *
 * @author AI Generated
 * @since 2.1.4-dev
 */
@Configuration(value = "cfgs/app-config.xml", reload = true)
public class AppConfig extends DefaultConfiguration {
}
```

启动类使用：

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
package com.example;

import com.example.config.AppConfig;
import net.ymate.platform.configuration.Cfgs;
import net.ymate.platform.configuration.annotation.ConfigurationConf;
import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.annotation.EnableAutoScan;

/**
 * 配置模块启动示例。
 *
 * @author AI Generated
 * @since 2.1.4-dev
 */
@EnableAutoScan
@ConfigurationConf(configHome = "${user.dir}/configs", checkTimeInterval = 5000)
public class ConfigStarter {

    static {
        System.setProperty(IApplication.SYSTEM_MAIN_CLASS, ConfigStarter.class.getName());
    }

    /**
     * 程序入口，演示加载XML配置并读取值。
     *
     * @param args 命令行参数
     * @throws Exception 初始化或读取异常
     * @since 2.1.4-dev
     */
    public static void main(String[] args) throws Exception {
        try (IApplication application = YMP.run(args)) {
            AppConfig cfg = new AppConfig();
            if (Cfgs.get().fillCfg(cfg)) {
                System.out.println(cfg.getString("company_name"));
                System.out.println(cfg.getList("products"));
                System.out.println(cfg.getMap("product_spec"));
            }
        }
    }
}
```

### 模板2：在@Bean服务中@ConfigValue注入配置值（配合@Configs）

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

import com.example.config.AppConfig;
import net.ymate.platform.configuration.annotation.ConfigValue;
import net.ymate.platform.configuration.annotation.Configs;
import net.ymate.platform.core.beans.annotation.Bean;

import java.util.List;
import java.util.Map;

/**
 * 演示@Configs + @ConfigValue在Bean中注入配置值。
 *
 * @author AI Generated
 * @since 2.1.4-dev
 */
@Bean
@Configs(AppConfig.class)
public class DemoConfigService {

    @ConfigValue("company_name")
    private String companyName;

    @ConfigValue(value = "product_spec", category = "default")
    private Map<String, String> productSpec;

    @ConfigValue("products")
    private List<String> products;

    /**
     * 获取公司名称。
     *
     * @return 公司名称字符串
     * @since 2.1.4-dev
     */
    public String getCompanyName() {
        return companyName;
    }

    /**
     * 获取产品规格映射。
     *
     * @return 规格K-V映射
     * @since 2.1.4-dev
     */
    public Map<String, String> getProductSpec() {
        return productSpec;
    }

    /**
     * 获取产品列表。
     *
     * @return 产品名称集合
     * @since 2.1.4-dev
     */
    public List<String> getProducts() {
        return products;
    }
}
```

## 5. 配置速查

### 5.1 配置文件常用项（≤12条）：key | 默认值 | 说明

| key | 默认值 | 说明 |
|---|---|---|
| `ymp.configs.configuration.config_home` | `${root}` | 配置体系根路径，支持${root}/${user.home}/${user.dir}变量 |
| `ymp.configs.configuration.project_name` | 空 | 项目分类子目录名 |
| `ymp.configs.configuration.module_name` | 空 | 模块子目录名 |
| `ymp.configs.configuration.config_base_dir` | 空 | 配置文件基准目录，不能以/开头但要以/结尾 |
| `ymp.configs.configuration.config_check_time_interval` | `0` | 配置文件检查间隔(毫秒)，0=不开启自动重载 |
| `ymp.configs.configuration.provider_class` | `DefaultConfigurationProvider` | 默认配置文件分析器全限定名 |
| JVM参数 `-Dymp.configHome` | - | 优先级高于配置文件的config_home |
| 环境变量 `YMP_CONFIG_HOME` | - | JVM参数未设置时尝试读取 |

### 5.2 注解配置核心参数

**@Configuration：**
- `value`：配置文件相对路径，空则使用 `简单类名小写.TAG.扩展名`
- `reload`：boolean，是否开启文件变更自动重新加载
- `provider`：Class<? extends IConfigurationProvider>，自定义解析器

**@ConfigurationConf：**
- `configHome` / `projectName` / `moduleName` / `configBaseDir`：路径相关
- `checkTimeInterval`：long，毫秒；`providerClass`：String

**@ConfigValue：**
- `value`：配置项名，空则用字段名/参数名
- `defaultValue` / `category`(默认default) / `configs`(Class<?>[])

## 6. 常见坑点排查（3-6条表格）：现象 | 原因 | 解决

| 现象 | 原因 | 解决 |
|---|---|---|
| @ConfigValue注入字段为null | 类未被@Bean托管或未加@Configs注解声明配置类 | 类加@Bean + @Configs(配置类.class)，确保被YMP扫描到 |
| fillCfg返回false或取不到配置值 | config_home下找不到对应路径文件 / 路径拼写错误 | 用Cfgs.get().searchAsFile(path)调试是否定位到文件；检查三级目录优先级是否被覆盖 |
| reload=true但修改后不生效 | checkTimeInterval=0（默认不开启） | 启动类@ConfigurationConf或properties中设置checkTimeInterval>0（如5000毫秒） |
| Properties/MAP字段取出值不对 | Properties格式未按properties.<category>.<key>=value书写；MAP用`|`分隔或分级`key.subkey` | 严格按文档格式：集合用\|分隔，MAP用嵌套属性或attributes.xxx |
| JSON配置解析异常 | JSON结构不符合categories/properties/attributes嵌套规范 | 参考文档JSON模板结构，必须有categories数组包裹category |
| YAML配置文件未被解析或内容为空 | 工程缺少SnakeYAML依赖（optional依赖不传递，需自行引入）；或YAML结构不符合规范 | pom中显式引入org.yaml:snakeyaml；YAML内容结构与JSON格式一致：categories数组包裹category，集合content用列表书写，MAP用attributes键值对表示 |
| 搜索文件返回null | 文件实际放错层级（全局/项目/模块）未对应configHome/projectName/moduleName设置 | 按三级优先级检查目录：全局<项目<模块，取最近匹配 |
