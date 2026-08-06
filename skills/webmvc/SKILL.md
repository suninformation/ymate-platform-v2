---
name: ymp-webmvc
description: YMP框架标准MVC实现，支持RESTful API、参数绑定+验证、视图JSON/Redirect/JSP、文件上传@VUploadFile校验、跨域@CrossDomain、缓存@ResponseCache、签名验证
version: 2.1.4-dev
author: YMP Team
category: web
tags:
  - java
  - mvc
  - web
  - restful
  - controller
  - file-upload
  - cors
trigger: 当用户需要开发Web应用、MVC架构、RESTful API、控制器、参数绑定验证、文件上传、跨域CORS、缓存、JSP/JSON视图等场景时触发
tools:
  - web-framework
  - mvc
  - restful-api
examples:
  - 控制器RESTful：@Controller+@RequestMapping GET/POST/PUT/DELETE+@PathVariable返回JSON
  - 参数绑定+验证：@RequestParam+@VRequired/@VEmail、@ModelBind对象、@ValidateGroups分组+@VCondition条件
  - 视图返回JSON/Redirect/JSP：View.jsonView()/redirectView()/jspView()/httpStatusView()/nullView()
  - 文件上传@FileUpload+@VUploadFile：@FileUpload+IUploadFileWrapper[]+@VUploadFile(max/contentTypes)大小类型校验
  - 跨域@CrossDomain：类/方法级@CrossDomain允许跨域请求
  - 缓存@ResponseCache：@ResponseCache(expire=3600)控制器方法缓存
---

# WebMVC 技能包

> AI读取指引：功能边界为Web控制器开发（路由、参数、视图、上传、跨域、缓存）；依赖ymate-platform-core、ymate-platform-validation；参数验证通用规则（@ValidateGroups分组、@VCondition条件、groups/condition参数）详见validation SKILL；WebMVC特有验证器@VUploadFile/@VHostName/@VToken在此处为权威定义；web.xml必须配置DispatchFilter且FORWARD dispatcher。

---

## 0. 快速索引
- **Maven artifactId**: `ymate-platform-webmvc`
- **静态入口类**: `net.ymate.platform.webmvc.WebMVC`（初始化）、`net.ymate.platform.webmvc.view.View`（视图工厂）、`net.ymate.platform.webmvc.context.WebContext`（上下文）
- **必备注解**: `@Controller`+`@RequestMapping`；web.xml必须配置`GeneralWebFilter`+`DispatchFilter`(含`<dispatcher>FORWARD</dispatcher>`+`<dispatcher>REQUEST</dispatcher>`)+`WebAppEventListener`
- **5行最简调用**:
```java
@Controller @RequestMapping("/hi")
public class HiCtl {
    @RequestMapping("/say") public IView say() {
        return View.jsonView(java.util.Collections.singletonMap("msg","Hello YMP"));
    }
}
```

## 1. 模块摘要
YMP框架标准MVC实现，完全基于注解配置零XML控制器；支持RESTful路由、自动参数绑定（原始类型/DTO/文件上传）、集成validation模块参数校验（支持分组/条件验证）、多种视图（JSON/JSP/Redirect/Text/HttpStatus/Null）、文件上传大小/类型校验@VUploadFile、跨域@CrossDomain、响应缓存@ResponseCache、参数签名@SignatureValidate。

- 典型MVC三层架构：DispatchFilter → RequestMapping解析 → 参数绑定+验证 → 控制器方法 → View渲染
- 10+种视图技术：JSON/JSP/Redirect/Text/HttpStatus/Binary/Html/Forward/Freemarker/Velocity/Null
- 参数绑定：@RequestParam/@PathVariable/@RequestSuffix/@ModelBind/@RequestHeader/@CookieVariable + 验证注解无缝组合
- Web特有验证器@VUploadFile/@VHostName/@VToken，全支持groups分组+condition条件（@since 2.1.4）
- 高级特性：@CrossDomain跨域、@ResponseCache页面缓存、@SignatureValidate参数签名验证、@EnableSnakeCaseParam下划线参数

## 2. 核心注解速查表

### 控制器与路由注解

| 全限定名 | 作用目标 | 核心参数 |
|---------|---------|---------|
| `net.ymate.platform.webmvc.annotation.Controller` | 类 | name(控制器名), singleton(默认true单例) |
| `net.ymate.platform.webmvc.annotation.RequestMapping` | 类 / 方法 | value(路径，默认方法名), method(GET/HEAD/POST/PUT/PATCH/DELETE/OPTIONS/TRACE，默认GET), suffix(扩展名数组如{".json",".xml"}，支持".*"通配), header(请求头必须存在), param(请求参数必须存在) |
| `net.ymate.platform.webmvc.annotation.RequestParam` | 方法参数 | value(参数名，默认变量名), prefix(前缀), defaultValue(默认值), fullScope(request>session>application级联查询，默认false) |
| `net.ymate.platform.webmvc.annotation.PathVariable` | 方法参数 | value(路径变量名，默认变量名) |
| `net.ymate.platform.webmvc.annotation.RequestSuffix` | 方法参数 | 无（注入@suffix扩展名值，需@RequestMapping.suffix配置） |
| `net.ymate.platform.webmvc.annotation.ModelBind` | 方法参数 | prefix(对象参数前缀，如"user." → user.name) |
| `net.ymate.platform.webmvc.annotation.RequestHeader` | 方法参数 | value(Header名), defaultValue, fullScope |
| `net.ymate.platform.webmvc.annotation.CookieVariable` | 方法参数 | value(Cookie名), defaultValue, fullScope |
| `net.ymate.platform.webmvc.cors.annotation.CrossDomain` | 类 / 方法 | 无（允许当前控制器/方法跨域请求，CORS标准响应头） |
| `net.ymate.platform.webmvc.annotation.FileUpload` | 方法 | 无（标记方法支持multipart/form-data文件上传） |
| `net.ymate.platform.webmvc.annotation.ResponseCache` | 方法 | expire(缓存秒数), scope(SESSION/APPLICATION), useGZip(默认true), controlHeader(默认true) |
| `net.ymate.platform.webmvc.annotation.SignatureValidate` | 方法 | signName(签名参数名), timestampName(时间戳参数名，默认timestamp), nonceName(随机串参数名，默认nonce), validSeconds(有效期秒) |
| `net.ymate.platform.webmvc.annotation.EnableSnakeCaseParam` | 类 / 方法 | 无（下划线参数名自动转驼峰，如user_name→userName） |

### WebMVC验证器（全部支持groups/condition通用参数 @since 2.1.4）

> **3个WebMVC验证器通用参数**：`msg`(自定义消息)、`groups`(Class[]，验证分组，默认DefaultGroup，支持多个如{Create.class, Update.class})、`condition`(@VCondition，条件验证，当条件满足时才验证)。分组优先级：显式调用 > 方法@ValidateGroups > 类@ValidateGroups > DefaultGroup。条件类型：ALWAYS / FIELD_EQUALS / FIELD_NOT_EQUALS / FIELD_GT / FIELD_GT_EQ / FIELD_LT / FIELD_LT_EQ / FIELD_NOT_EMPTY / FIELD_EMPTY。详见validation SKILL。

| 全限定名 | 作用目标 | 核心参数（不含通用msg/groups/condition） |
|---------|---------|-------------------------------------|
| `net.ymate.platform.webmvc.validate.VUploadFile` | 方法参数（IUploadFileWrapper或数组） | min(最小字节0不限), max(最大字节0不限), totalMax(总字节0不限), contentTypes(允许MIME，支持扩展名如".jpg"自动转image/jpeg；空则全局配置) → 文件上传大小/类型校验 |
| `net.ymate.platform.webmvc.validate.VHostName` | 方法参数（String hostName） | checker(IHostNameChecker检测器，空SPI默认；默认读webmvc.allowed_access_hosts多主机|分隔), httpStatus(自定义HTTP响应码，0不启用) → 重定向主机白名单校验 |
| `net.ymate.platform.webmvc.validate.VToken` | 方法 / 方法参数 | name(令牌名), reset(验证后是否重置令牌默认false) → 会话令牌防重复提交（传统网页表单） |

## 3. 核心API速查

| API | 说明 |
|-----|------|
| `View.textView(String text[, String contentType])` | 返回纯文本视图 |
| `View.jsonView(Object data)` / 控制器方法直接返回Object | 返回JSON视图（自动序列化） |
| `View.jspView(String path)` | JSP视图，路径自动拼接base_view_path（如"/demo/index"→/WEB-INF/jsp/demo/index.jsp） |
| `View.redirectView(String url)` / `View.redirectView(String url, Map<String,Object> params)` | 302重定向 |
| `View.forwardView(String path)` | FORWARD转发 |
| `View.httpStatusView(int code)` | 返回HTTP状态码（404/500等）空视图 |
| `View.nullView()` | 空视图（控制器方法直接写Response时使用） |
| `WebContext.getRequest()` / `getResponse()` / `getSession()` / `getServletContext()` | 静态获取原生Servlet对象 |
| `WebContext.getContext().getParameterToString("name")` | 从请求上下文取参数（已含前缀/作用域处理） |
| `WebContext.getContext().getSession().put("user", obj)` | 操作会话属性 |
| `IUploadFileWrapper.transferTo(File dest)` / `getSize()` / `getName()` / `getContentType()` | 上传文件保存/信息获取 |

## 4. 标准代码模板

### 模板1：最简RESTful控制器（JSON+@PathVariable+@RequestParam）
```java
/*
 * Copyright 2024-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 */
package com.example.controller;

import net.ymate.platform.webmvc.annotation.Controller;
import net.ymate.platform.webmvc.annotation.PathVariable;
import net.ymate.platform.webmvc.annotation.RequestMapping;
import net.ymate.platform.webmvc.annotation.RequestParam;
import net.ymate.platform.webmvc.base.Type;
import net.ymate.platform.webmvc.view.IView;
import net.ymate.platform.webmvc.view.View;

import java.util.HashMap;
import java.util.Map;

/**
 * RESTful用户控制器
 *
 * @author YMP Team
 * @since 2.1.4-dev
 */
@Controller
@RequestMapping("/api/user")
public class UserRestController {

    /**
     * GET查询用户-路径变量
     * @param id 用户ID
     * @since 2.1.4-dev
     */
    @RequestMapping(value = "/{id}", method = Type.HttpMethod.GET)
    public IView getById(@PathVariable String id) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("name", "YMP");
        return View.jsonView(data);
    }

    /**
     * GET查询列表-请求参数，直接返回Map自动JSON化
     * @param page 页码默认1
     * @since 2.1.4-dev
     */
    @RequestMapping(value = "/list", method = Type.HttpMethod.GET)
    public Map<String, Object> list(@RequestParam(defaultValue = "1") Integer page,
                                     @RequestParam(defaultValue = "10") Integer pageSize) {
        Map<String, Object> res = new HashMap<>();
        res.put("page", page);
        res.put("pageSize", pageSize);
        res.put("rows", java.util.Collections.emptyList());
        return res;
    }

    /**
     * POST创建
     * @since 2.1.4-dev
     */
    @RequestMapping(value = "/create", method = Type.HttpMethod.POST)
    public IView create(@RequestParam String name, @RequestParam String email) {
        return View.jsonView(java.util.Collections.singletonMap("success", true));
    }
}
```

### 模板2：参数绑定+验证组合（@RequestParam验证+分组+@ModelBind对象）
```java
/*
 * Copyright 2024-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 */
package com.example.controller;

import net.ymate.platform.validation.annotation.VCondition;
import net.ymate.platform.validation.annotation.ValidateGroups;
import net.ymate.platform.validation.validate.VEmail;
import net.ymate.platform.validation.validate.VLength;
import net.ymate.platform.validation.validate.VRequired;
import net.ymate.platform.webmvc.annotation.Controller;
import net.ymate.platform.webmvc.annotation.ModelBind;
import net.ymate.platform.webmvc.annotation.RequestMapping;
import net.ymate.platform.webmvc.annotation.RequestParam;
import net.ymate.platform.webmvc.base.Type;
import net.ymate.platform.webmvc.view.IView;
import net.ymate.platform.webmvc.view.View;

/**
 * 创建分组接口
 * @since 2.1.4-dev
 */
interface CreateGroup {}

/**
 * 更新分组接口
 * @since 2.1.4-dev
 */
interface UpdateGroup {}

/**
 * 用户DTO - 与@ModelBind配合
 * @since 2.1.4-dev
 */
class UserForm {
    @VRequired(groups = {CreateGroup.class, UpdateGroup.class})
    @VLength(min = 2, max = 50, groups = {CreateGroup.class, UpdateGroup.class})
    private String name;
    @VEmail(groups = {CreateGroup.class, UpdateGroup.class})
    private String email;
    @VRequired(groups = UpdateGroup.class)
    private String id;
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
}

/**
 * 参数绑定+验证组合控制器
 *
 * @author YMP Team
 * @since 2.1.4-dev
 */
@Controller
@RequestMapping("/valid")
public class ValidDemoController {

    /**
     * 参数级验证-@RequestParam+验证注解
     * 注意：@VRequired/@VEmail 必须放在@RequestParam前面！
     * @since 2.1.4-dev
     */
    @RequestMapping("/param")
    public IView paramValid(@VRequired @VLength(min = 2, max = 20) @RequestParam String username,
                             @VEmail @RequestParam String email) {
        return View.jsonView(java.util.Collections.singletonMap("ok", true));
    }

    /**
     * 分组验证-@ValidateGroups在方法上声明分组
     * 条件验证-type=vip时vipNo必填
     * @since 2.1.4-dev
     */
    @RequestMapping("/update")
    @ValidateGroups(UpdateGroup.class)
    public IView updateValid(@VRequired(groups = UpdateGroup.class) @RequestParam String id,
                              @VRequired @RequestParam String type,
                              @VRequired(condition = @VCondition(type = VCondition.Type.FIELD_EQUALS,
                                      field = "type", expectedValue = "vip")) @RequestParam String vipNo) {
        return View.jsonView(java.util.Collections.singletonMap("ok", true));
    }

    /**
     * @ModelBind对象级验证 + CreateGroup分组
     * @since 2.1.4-dev
     */
    @RequestMapping(value = "/register", method = Type.HttpMethod.POST)
    @ValidateGroups(CreateGroup.class)
    public IView register(@ModelBind UserForm form) {
        return View.jsonView(java.util.Collections.singletonMap("ok", true));
    }
}
```

### 模板3：文件上传 @FileUpload + IUploadFileWrapper + @VUploadFile校验
```java
/*
 * Copyright 2024-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 */
package com.example.controller;

import net.ymate.platform.webmvc.IUploadFileWrapper;
import net.ymate.platform.webmvc.annotation.Controller;
import net.ymate.platform.webmvc.annotation.FileUpload;
import net.ymate.platform.webmvc.annotation.RequestMapping;
import net.ymate.platform.webmvc.annotation.RequestParam;
import net.ymate.platform.webmvc.base.Type;
import net.ymate.platform.webmvc.validate.VUploadFile;
import net.ymate.platform.webmvc.view.IView;
import net.ymate.platform.webmvc.view.View;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传控制器
 *
 * @author YMP Team
 * @since 2.1.4-dev
 */
@Controller
@RequestMapping("/upload")
public class UploadDemoController {

    // 平台无关：使用系统临时目录 + 业务子路径，避免硬编码 Windows 盘符
    private static final String UPLOAD_DIR = System.getProperty("java.io.tmpdir") + File.separator + "ymp_uploads";

    /**
     * 单文件上传+@VUploadFile大小类型校验
     * max=5MB=5*1024*1024；contentTypes支持扩展名自动转MIME
     * @since 2.1.4-dev
     */
    @RequestMapping(value = "/single", method = Type.HttpMethod.POST)
    @FileUpload
    public IView singleUpload(@VUploadFile(max = 5242880L, contentTypes = {".jpg", ".png", "image/gif"})
                               @RequestParam IUploadFileWrapper file) throws Exception {
        String fileName = System.currentTimeMillis() + "_" + file.getName();
        File dest = new File(UPLOAD_DIR, fileName);
        dest.getParentFile().mkdirs();
        file.transferTo(dest);
        Map<String, Object> res = new HashMap<>();
        res.put("fileName", fileName);
        res.put("size", file.getSize());
        res.put("contentType", file.getContentType());
        return View.jsonView(res);
    }

    /**
     * 多文件上传数组方式+总大小限制totalMax
     * @since 2.1.4-dev
     */
    @RequestMapping(value = "/batch", method = Type.HttpMethod.POST)
    @FileUpload
    public IView batchUpload(@VUploadFile(max = 2097152L, totalMax = 10485760L,
                                           contentTypes = {".pdf", ".doc", ".docx"})
                              @RequestParam IUploadFileWrapper[] files) throws Exception {
        int success = 0;
        for (IUploadFileWrapper f : files) {
            if (f != null && f.getSize() > 0) {
                String fileName = System.currentTimeMillis() + "_" + f.getName();
                f.transferTo(new File(UPLOAD_DIR, fileName));
                success++;
            }
        }
        return View.jsonView(java.util.Collections.singletonMap("uploaded", success));
    }
}
```

### 模板4：高级特性（@CrossDomain跨域 + @ResponseCache缓存 + @SignatureValidate签名）
```java
/*
 * Copyright 2024-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 */
package com.example.controller;

import net.ymate.platform.webmvc.annotation.Controller;
import net.ymate.platform.webmvc.annotation.EnableSnakeCaseParam;
import net.ymate.platform.webmvc.annotation.RequestMapping;
import net.ymate.platform.webmvc.annotation.RequestParam;
import net.ymate.platform.webmvc.annotation.ResponseCache;
import net.ymate.platform.webmvc.annotation.SignatureValidate;
import net.ymate.platform.webmvc.cors.annotation.CrossDomain;
import net.ymate.platform.webmvc.view.IView;
import net.ymate.platform.webmvc.view.View;

import java.util.HashMap;
import java.util.Map;

/**
 * 跨域+缓存+签名验证组合控制器
 *
 * @author YMP Team
 * @since 2.1.4-dev
 */
@Controller
@RequestMapping("/api/advanced")
@CrossDomain
public class AdvancedController {

    /**
     * 跨域公开数据接口 - 类上@CrossDomain所有方法均允许跨域
     * @since 2.1.4-dev
     */
    @RequestMapping("/public")
    public IView publicData() {
        return View.jsonView(java.util.Collections.singletonMap("data", "Hello from CORS API"));
    }

    /**
     * 缓存接口 - @ResponseCache(expire=1800)缓存30分钟
     * 适用于不常变动的字典、配置等读多写少数据
     * 注意：不要与DispatchFilter INCLUDE同时使用，会引发死循环！
     * @since 2.1.4-dev
     */
    @RequestMapping("/dict")
    @ResponseCache(expire = 1800)
    public IView dict() {
        Map<String, Object> data = new HashMap<>();
        data.put("genders", new String[]{"男","女"});
        data.put("cities", java.util.Arrays.asList("北京","上海","广州"));
        return View.jsonView(data);
    }

    /**
     * 签名验证接口 - @SignatureValidate 确保请求不被篡改防重放
     * 搭配@EnableSnakeCaseParam：下划线参数(create_time)自动转驼峰(createTime)
     * 客户端签名算法：MD5(clientId + create_time + event + unionId + nonce + secretKey)
     * @param clientId 客户端ID
     * @param createTime 创建时间（下划线参数create_time自动映射）
     * @param event 事件类型
     * @param unionId 唯一ID
     * @since 2.1.4-dev
     */
    @RequestMapping("/notify")
    @EnableSnakeCaseParam
    @SignatureValidate(timestampName = "create_time", nonceName = "nonce", validSeconds = 300)
    public IView notifyCallback(@RequestParam String clientId,
                                 @RequestParam Long createTime,
                                 @RequestParam String event,
                                 @RequestParam String unionId) {
        Map<String, Object> res = new HashMap<>();
        res.put("code", 0);
        res.put("msg", "签名校验通过");
        res.put("event", event);
        return View.jsonView(res);
    }

    /**
     * 混合使用 - 方法级@ResponseCache覆盖类级@CrossDomain
     * @since 2.1.4-dev
     */
    @RequestMapping("/stats")
    @CrossDomain
    @ResponseCache(expire = 600)
    public IView stats() {
        return View.jsonView(java.util.Collections.singletonMap("pv", 10086));
    }
}
```

## 5. 配置速查

### 5.1 ymp.properties核心配置（ymp.configs.webmvc.* 前缀）

| Key | 默认值 | 说明 |
|-----|-------|------|
| `ymp.configs.webmvc.request_mapping_parser_class` | DefaultRequestMappingParser | 请求映射路径分析器 |
| `ymp.configs.webmvc.default_charset_encoding` | UTF-8 | 默认字符编码集 |
| `ymp.configs.webmvc.default_content_type` | text/html | 默认Content-Type响应头 |
| `ymp.configs.webmvc.resources_home` | 空 | 国际化资源文件存放路径 |
| `ymp.configs.webmvc.resource_name` | messages | 国际化资源文件名 |
| `ymp.configs.webmvc.language_param_name` | _lang | 国际化语言切换参数名 |
| `ymp.configs.webmvc.request_ignore_regex` | jsp\|jspx\|png\|gif\|jpg\|jpeg\|js\|css\|swf\|ico\|htm\|html\|... | **请求忽略扩展名正则**（关键：.html在默认忽略列表！需改此项才能用@RequestMapping suffix=.html） |
| `ymp.configs.webmvc.request_method_param` | _method | 请求方法参数名（POST用_method=DELETE冒充DELETE） |
| `ymp.configs.webmvc.request_prefix` | 空 | 请求路径全局前缀（如"/api"） |
| `ymp.configs.webmvc.request_strict_mode_enabled` | false | 请求路径严格模式（true时"/demo"与"/demo/"不同） |
| `ymp.configs.webmvc.base_view_path` | /WEB-INF/jsp/ | JSP视图基础路径（View.jspView("/demo/index")→/WEB-INF/jsp/demo/index.jsp） |
| `ymp.configs.webmvc.upload_temp_dir` | （容器临时目录） | 文件上传临时目录 |
| `ymp.configs.webmvc.upload_file_count_max` | 10 | 单次最大上传文件数 |
| `ymp.configs.webmvc.upload_file_size_max` | 10485760(10MB) | 单文件最大字节 |
| `ymp.configs.webmvc.upload_total_size_max` | 104857600(100MB) | 上传总大小字节 |
| `ymp.configs.webmvc.upload_size_threshold` | 10240(10KB) | 内存缓冲阈值，超则写临时文件 |
| `ymp.configs.webmvc.upload_listener_class` | 空 | 上传进度监听器IUploadListener实现 |
| `ymp.configs.webmvc.cookie_prefix` | 空 | Cookie名前缀 |
| `ymp.configs.webmvc.cookie_domain` | 空 | Cookie作用域 |
| `ymp.configs.webmvc.cookie_path` | / | Cookie作用路径 |
| `ymp.configs.webmvc.cookie_auth_key` | 空 | Cookie加密密钥 |
| `ymp.configs.webmvc.cookie_auth_enabled` | false | Cookie签名验证默认开启 |
| `ymp.configs.webmvc.cookie_use_http_only` | true | Cookie默认HttpOnly防XSS |
| `ymp.configs.webmvc.allowed_access_hosts` | 空 | @VHostName默认检测器白名单，多主机用`\|`分隔 |

### 5.2 注解配置核心参数

| 注解 | 核心参数重点 |
|-----|-----------|
| @RequestMapping.suffix | 扩展名数组继承规则优先级：方法级 > 类级 > 包级（package-info.java）；方法级配置覆盖类级；支持".*"匹配任意；注意避开request_ignore_regex列表 |
| @RequestParam | fullScope=true按request→session→application顺序查找，慎用（session读写入性能） |
| @FileUpload | 仅作用于方法；未加此注解multipart请求中的文件不解析，IUploadFileWrapper注入为null |
| @VUploadFile | contentTypes支持扩展名".jpg"写法，自动转换image/jpeg；空数组使用upload_file_size_max/upload_total_size_max全局限制 |
| @ResponseCache | expire单位秒；与DispatchFilter INCLUDE dispatcher冲突，请勿混用（引发页面死循环）；仅对GET请求建议缓存 |
| @CrossDomain | 类级声明所有方法跨域；方法级仅该方法跨域；自动响应Access-Control-Allow-Origin: *等CORS标准头 |
| @SignatureValidate | 签名算法默认HMAC-MD5；可自定义ISignatureValidator实现替换；客户端按固定参数顺序+密钥拼接签名字符串 |
| groups + condition（@VUploadFile/@VHostName/@VToken） | 与validation模块完全一致：groups数组交集匹配；condition @VCondition字段名是@RequestParam.value值；@ValidateGroups方法级优先类级 |

## 6. 常见坑点排查

| 现象 | 原因 | 解决方案 |
|-----|------|---------|
| @VRequired/@VEmail放在@RequestParam参数上完全不校验 | 注解顺序错误！验证注解必须写在@RequestParam**前面** | `@VRequired @RequestParam String name` 而不是 `@RequestParam @VRequired String name` |
| @RequestMapping(suffix=".html")请求永远404 | `request_ignore_regex`默认包含html/htm等扩展名，被当静态资源忽略了 | 修改ymp.configs.webmvc.request_ignore_regex，将html从列表中删除；或换用.json/.xml等不在列表中的扩展名 |
| @RequestMapping(suffix=".json")方法级不生效（生效类级的.html） | suffix属性继承规则：方法级有则用方法级，没有才用类级；确认方法级是否真写了suffix | 方法上显式写：`@RequestMapping(value="/x", suffix=".json")`；包级@RequesMapping放在package-info.java中 |
| JSP视图404（View.jspView("/demo/index")） | base_view_path默认/WEB-INF/jsp/，实际jsp路径应为/WEB-INF/jsp/demo/index.jsp | 检查jsp文件实际路径；或调整ymp.configs.webmvc.base_view_path为你的真实路径；注意路径前后斜杠 |
| DispatchFilter配置后JSP FORWARD页面404/死循环 | web.xml中DispatchFilter的filter-mapping缺少`<dispatcher>FORWARD</dispatcher>`；或多配了`<dispatcher>INCLUDE</dispatcher>`与@ResponseCache冲突 | filter-mapping必加`<dispatcher>REQUEST</dispatcher><dispatcher>FORWARD</dispatcher>`；**绝对不要加INCLUDE dispatcher** |
| @ResponseCache接口反复加载/死循环 | DispatchFilter配了INCLUDE dispatcher；或页面本身被INCLUDE | 从web.xml DispatchFilter filter-mapping中删除`<dispatcher>INCLUDE</dispatcher>`；仅保留REQUEST+FORWARD |
| @VUploadFile校验绕过/不生效 | 1. 方法未加@FileUpload；2. 参数类型不是IUploadFileWrapper/IUploadFileWrapper[]；3. 参数名与form file字段名不一致 | 1. 方法加@FileUpload；2. 用@RequestParam("file")显式指定参数名；3. form enctype=multipart/form-data |
| @CrossDomain加了浏览器仍报CORS错误 | 1. 只加在类上但@RequestMapping路径方法不匹配；2. 预检OPTIONS请求被拦截返回401/403 | 确保@CrossDomain在匹配的方法或类上；GeneralWebFilter/DispatchFilter顺序正确，OPTIONS预检正常通过；自定义拦截器放行OPTIONS |
| @ModelBind对象嵌套属性注入失败（user.address.city为null） | 1. 嵌套对象getter/setter缺失；2. 表单字段名未加前缀 | 1. 所有层级（含内部类）public且有getter/setter；2. 前端name写"address.city"或加@ModelBind(prefix="user")则写"user.address.city" |
| @VCondition FIELD_EQUALS比较始终不触发 | condition.field写的是@RequestParam.value值（不是Java变量名）；大小写敏感 | 确认condition.field与@RequestParam("xxx")的xxx完全一致，或与@VField.value一致 |
