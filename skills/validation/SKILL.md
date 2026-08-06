---
name: ymp-validation
description: YMP框架服务端参数验证模块，注解式配置验证规则，支持分组验证、条件验证、嵌套对象验证、自定义IValidator、WebMVC参数验证
version: 2.1.4-dev
author: YMP Team
category: validation
tags:
  - java
  - validation
  - parameter-validation
  - annotation
  - group-validation
  - conditional-validation
trigger: 当用户需要实现参数验证、表单校验、DTO数据校验、分组验证、条件验证、嵌套对象验证、自定义验证器、WebMVC参数验证等场景时触发
tools:
  - validation
  - parameter-check
examples:
  - 分组验证groups：同DTO在Create/Update场景下不同验证规则
  - 条件验证@VCondition：FIELD_EQUALS/GT/NOT_EMPTY/EMPTY等条件触发验证
  - 自定义IValidator：@Validator注解+IValidator接口实现业务校验
  - DTO嵌套@VModel：@VModel注解嵌套对象级联验证
  - WebMVC参数验证：与webmvc SKILL配合使用@RequestParam+验证注解
---

# Validation 验证技能包

> AI读取指引：功能边界为服务端JavaBean/方法参数注解式验证；依赖ymate-platform-core；WebMVC控制器参数中使用@VUploadFile请跳转webmvc SKILL；验证通用参数（groups/condition）此处为权威定义。

---

## 0. 快速索引
- **Maven artifactId**: `ymate-platform-validation`
- **静态入口类**: `net.ymate.platform.validation.Validations`
- **必备注解**: `@Validation`（类/方法级）、验证器注解（字段/参数级）
- **5行最简调用**:
```java
@Validation
public class UserDTO {
    @VRequired @VLength(min=2,max=50) private String name;
    // getter/setter...
}
Map<String,ValidateResult> r = Validations.get().validate(UserDTO.class, params);
```

## 1. 模块摘要
YMP框架注解式服务端参数验证工具，支持类成员属性和方法参数验证，内置12种常用验证器，支持分组验证（groups）、条件验证（@VCondition）、嵌套对象（@VModel）、自定义IValidator、国际化I18N消息。

- 注解声明式配置，零XML
- 短路NORMAL / 全量FULL两种验证模式
- 所有验证器统一支持`groups`分组和`condition`条件参数（@since 2.1.4）
- 支持@ValidateGroups在类/方法上预设分组，优先级：显式传参 > 方法@ValidateGroups > 类@ValidateGroups > DefaultGroup
- 嵌套对象@VModel级联验证，参数前缀自动拼接

## 2. 核心注解速查表

### 基础配置注解

| 全限定名 | 作用目标 | 核心参数 |
|---------|---------|---------|
| `net.ymate.platform.validation.annotation.Validation` | 类 / 方法 | mode(NORMAL\|FULL), resourcesName(自定义i18n资源名) |
| `net.ymate.platform.validation.annotation.VField` | 字段 / 参数 | prefix(参数前缀), value(参数名绑定), name(显示名), label(i18n标签) |
| `net.ymate.platform.validation.annotation.VModel` | 字段 / 参数 | prefix(嵌套参数前缀) → 级联验证嵌套JavaBean |
| `net.ymate.platform.validation.annotation.VMsg` | 字段 / 参数 | value(自定义消息，覆盖验证器默认消息) |
| `net.ymate.platform.validation.annotation.ValidateGroups` | 类 / 方法 | value(Class[]，如Create.class, Update.class) → 类/方法级预设分组@since 2.1.4 |
| `net.ymate.platform.validation.annotation.VCondition` | 验证注解属性 | type(ALWAYS\|FIELD_EQUALS\|FIELD_NOT_EQUALS\|FIELD_GT\|FIELD_GT_EQ\|FIELD_LT\|FIELD_LT_EQ\|FIELD_NOT_EMPTY\|FIELD_EMPTY), field(依赖字段名), expectedValue(期望值) @since 2.1.4 |
| `net.ymate.platform.validation.annotation.Validator` | IValidator实现类 | value(注解Class) → 注册自定义验证器 |

> **所有12个内置验证器通用参数**：`msg`(自定义消息)、`groups`(Class[]，验证分组，默认DefaultGroup)、`condition`(@VCondition，验证条件)。groups和condition必须在每个验证器上显式标注支持。

### 12个内置验证器

| 全限定名 | 作用目标 | 核心参数（不含通用msg/groups/condition） |
|---------|---------|-------------------------------------|
| `net.ymate.platform.validation.validate.VRequired` | 字段 / 参数 | 无 → 必填项（非空/数组长度>0） |
| `net.ymate.platform.validation.validate.VEmail` | 字段 / 参数 | 无 → 邮箱格式 |
| `net.ymate.platform.validation.validate.VLength` | 字段 / 参数 | min(最小长度), max(最大长度), eq(固定长度，与min/max互斥) → 字符串长度 |
| `net.ymate.platform.validation.validate.VSize` | 字段 / 参数 | min(最少元素), max(最多元素), eq(固定元素数) → 集合/数组大小 |
| `net.ymate.platform.validation.validate.VCompare` | 字段 / 参数 | cond(EQ\|NOT_EQ\|GT\|GT_EQ\|LT\|LT_EQ), with(比较的参数名), withLabel(比较参数显示名) → 两参数比较（如密码与确认密码） |
| `net.ymate.platform.validation.validate.VDataRange` | 字段 / 参数 | value(允许值数组), ignoreCase(默认true), providerClass(IDataRangeValuesProvider) → 值范围校验 |
| `net.ymate.platform.validation.validate.VDateTime` | 字段 / 参数 | value(转换后时间戳存储参数名), pattern(默认yyyy-MM-dd HH:mm:ss), single(单日区间默认true), separator(时间段分隔符默认/), maxDays(时间段天数差) |
| `net.ymate.platform.validation.validate.VIDCard` | 字段 / 参数 | 无 → 身份证有效性 |
| `net.ymate.platform.validation.validate.VMobile` | 字段 / 参数 | regex(自定义正则，覆盖默认) → 手机号格式 |
| `net.ymate.platform.validation.validate.VNumeric` | 字段 / 参数 | digits(仅检查数字默认false，true时其他参数失效), min/max/eq(数值范围), decimals(小数位数) → 数值校验 |
| `net.ymate.platform.validation.validate.VRSAData` | 字段 / 参数 | value(解码后存储参数名), providerClass(IRSAKeyProvider) → RSA数据解码验证 |
| `net.ymate.platform.validation.validate.VRegex` | 字段 / 参数 | regex(正则表达式) → 正则匹配 |

## 3. 核心API速查

| API | 说明 |
|-----|------|
| `Validations.get()` | 获取验证管理器单例IValidation |
| `validation.validate(Class<?> targetClass, Map<String,Object> paramValues, Class<?>... groups)` | 执行类成员属性验证（groups可选） |
| `validation.validate(Class<?> targetClass, Method method, Map<String,Object> paramValues, Class<?>... groups)` | 执行方法参数验证（groups可选） |
| `validation.registerValidator(Class<? extends Annotation> annoClass, Class<? extends IValidator> validatorClass)` | 手动注册自定义验证器（也可@Validator自动注册） |
| `ValidateResult.builder(context).msg(msgKey, defaultMsg, args...).build()` | 构造验证失败结果 |
| `ValidateResult` | 验证结果对象：getFieldName()/getMsg() 字段名和错误消息 |
| `ValidateContext` | 验证上下文：getParamValue()/getAnnotation()/getParamName() |
| `IValidator` 接口 | 自定义验证器实现：`ValidateResult validate(ValidateContext context)`，通过返回null表示验证通过 |

## 4. 标准代码模板

### 模板1：完整UserDTO（嵌套@VModel + 多验证器 + validate调用）
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
package com.example.dto;

import net.ymate.platform.validation.annotation.VField;
import net.ymate.platform.validation.annotation.VModel;
import net.ymate.platform.validation.annotation.VMsg;
import net.ymate.platform.validation.annotation.Validation;
import net.ymate.platform.validation.validate.VCompare;
import net.ymate.platform.validation.validate.VEmail;
import net.ymate.platform.validation.validate.VLength;
import net.ymate.platform.validation.validate.VRequired;
import net.ymate.platform.validation.Validations;
import net.ymate.platform.validation.ValidateResult;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户DTO-完整示例
 *
 * @author YMP Team
 * @since 2.1.4-dev
 */
@Validation(mode = Validation.MODE.FULL)
public class UserDTO {

    @VRequired(msg = "{0}不能为空")
    @VLength(min = 3, max = 16, msg = "{0}长度必须在3到16之间")
    @VField(label = "用户名称")
    private String username;

    @VRequired
    @VLength(eq = 32)
    @VMsg("{0}无效")
    @VField(name = "密码")
    private String password;

    @VRequired
    @VCompare(cond = VCompare.Cond.EQ, with = "password", withLabel = @VField(name = "密码"))
    private String repassword;

    @VRequired
    @VEmail
    @VField(name = "邮箱")
    private String email;

    @VModel
    @VField(name = "ext")
    private UserExt userExt;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRepassword() { return repassword; }
    public void setRepassword(String repassword) { this.repassword = repassword; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public UserExt getUserExt() { return userExt; }
    public void setUserExt(UserExt userExt) { this.userExt = userExt; }

    /**
     * 嵌套扩展信息
     * @since 2.1.4-dev
     */
    public static class UserExt {
        @VLength(max = 10)
        private String sex;
        @VRequired
        private Integer age;
        public String getSex() { return sex; }
        public void setSex(String sex) { this.sex = sex; }
        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }
    }

    /**
     * 调用示例
     * @since 2.1.4-dev
     */
    public static void main(String[] args) {
        Map<String, Object> params = new HashMap<>();
        params.put("username", "lz");
        params.put("password", "123");
        params.put("repassword", "456");
        params.put("email", "@163.com");
        params.put("ext.age", "17");
        params.put("ext.sex", "male");

        Map<String, ValidateResult> results = Validations.get().validate(UserDTO.class, params);
        results.forEach((key, result) -> System.out.println(key + ": " + result.getMsg()));
    }
}
```

### 模板2：分组验证（Create/Update接口 + @ValidateGroups）
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
package com.example.dto;

import net.ymate.platform.validation.annotation.ValidateGroups;
import net.ymate.platform.validation.annotation.Validation;
import net.ymate.platform.validation.validate.VLength;
import net.ymate.platform.validation.validate.VRequired;
import net.ymate.platform.validation.Validations;
import net.ymate.platform.validation.ValidateResult;

import java.util.HashMap;
import java.util.Map;

/**
 * 分组验证-创建操作分组
 * @since 2.1.4-dev
 */
interface Create {}

/**
 * 分组验证-更新操作分组
 * @since 2.1.4-dev
 */
interface Update {}

/**
 * 用户DTO-分组验证示例
 *
 * @author YMP Team
 * @since 2.1.4-dev
 */
@Validation
@ValidateGroups(Create.class)
public class GroupUserDTO {

    @VRequired(groups = Create.class)
    @VLength(min = 6, max = 32, groups = Create.class)
    private String password;

    @VRequired(groups = Update.class)
    private String id;

    @VRequired(groups = {Create.class, Update.class})
    @VLength(min = 2, max = 50, groups = {Create.class, Update.class})
    private String name;

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    /**
     * 分组调用示例
     * @since 2.1.4-dev
     */
    public static void main(String[] args) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "test");
        params.put("password", "123");
        params.put("id", "U001");

        // Create分组：类上@ValidateGroups(Create.class)，不传groups也生效
        Map<String, ValidateResult> createResults = Validations.get().validate(GroupUserDTO.class, params);

        // 显式传Update分组：仅验证groups含Update的字段
        Map<String, ValidateResult> updateResults = Validations.get().validate(GroupUserDTO.class, params, Update.class);
    }
}
```

### 模板3：条件验证（OrderDTO 4种@VCondition用法）
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
package com.example.dto;

import net.ymate.platform.validation.annotation.VCondition;
import net.ymate.platform.validation.annotation.Validation;
import net.ymate.platform.validation.validate.VEmail;
import net.ymate.platform.validation.validate.VRequired;
import net.ymate.platform.validation.Validations;
import net.ymate.platform.validation.ValidateResult;

import java.util.HashMap;
import java.util.Map;

/**
 * 订单DTO-条件验证4种用法示例
 *
 * @author YMP Team
 * @since 2.1.4-dev
 */
@Validation
public class OrderDTO {

    @VRequired
    private String type;

    private String amount;
    private String phone;
    private String email;

    // 用法1：FIELD_EQUALS - 当type为express时address必填
    @VRequired(condition = @VCondition(type = VCondition.Type.FIELD_EQUALS, field = "type", expectedValue = "express"))
    private String address;

    // 用法2：FIELD_GT - 当amount>10000时approvalCode必填
    @VRequired(condition = @VCondition(type = VCondition.Type.FIELD_GT, field = "amount", expectedValue = "10000"))
    private String approvalCode;

    // 用法3：FIELD_NOT_EMPTY - 当email非空时才校验邮箱格式
    @VEmail(condition = @VCondition(type = VCondition.Type.FIELD_NOT_EMPTY, field = "email"))
    private String emailCheck;

    // 用法4：FIELD_EMPTY - 当phone为空时backupPhone必填
    @VRequired(condition = @VCondition(type = VCondition.Type.FIELD_EMPTY, field = "phone"))
    private String backupPhone;

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getAmount() { return amount; }
    public void setAmount(String amount) { this.amount = amount; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getApprovalCode() { return approvalCode; }
    public void setApprovalCode(String approvalCode) { this.approvalCode = approvalCode; }
    public String getEmailCheck() { return emailCheck; }
    public void setEmailCheck(String emailCheck) { this.emailCheck = emailCheck; }
    public String getBackupPhone() { return backupPhone; }
    public void setBackupPhone(String backupPhone) { this.backupPhone = backupPhone; }

    /**
     * 条件验证调用示例
     * @since 2.1.4-dev
     */
    public static void main(String[] args) {
        Map<String, Object> params = new HashMap<>();
        params.put("type", "express");
        params.put("address", "");
        params.put("amount", "15000");
        params.put("approvalCode", "");
        params.put("phone", "");
        params.put("backupPhone", "");
        params.put("email", "bad-email");
        params.put("emailCheck", "bad-email");
        Map<String, ValidateResult> results = Validations.get().validate(OrderDTO.class, params);
        results.forEach((k, v) -> System.out.println(k + ": " + v.getMsg()));
    }
}
```

### 模板4：自定义IValidator（@VEmailCanUse + EmailCanUseValidator）
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
package com.example.validator;

import net.ymate.platform.commons.lang.BlurObject;
import net.ymate.platform.commons.util.StringUtils;
import net.ymate.platform.validation.IValidator;
import net.ymate.platform.validation.ValidateContext;
import net.ymate.platform.validation.ValidateResult;
import net.ymate.platform.validation.annotation.Validator;

import java.lang.annotation.*;

/**
 * 自定义验证器注解：邮箱后缀可用性校验
 *
 * @author YMP Team
 * @since 2.1.4-dev
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@interface VEmailCanUse {
    String suffix();
    String msg() default "";
    Class<?>[] groups() default {};
    VCondition condition() default @VCondition;
}

/**
 * 自定义验证器实现：邮箱后缀校验
 *
 * @author YMP Team
 * @since 2.1.4-dev
 */
@Validator(VEmailCanUse.class)
class EmailCanUseValidator implements IValidator {

    private static final String I18N_KEY = "ymp.validation.email_can_use";
    private static final String I18N_DEFAULT = "E-mail address \"{1}\" cannot be used.";

    @Override
    public ValidateResult validate(ValidateContext context) {
        String paramValue = BlurObject.bind(context.getParamValue()).toStringValue();
        if (StringUtils.isNotBlank(paramValue)) {
            VEmailCanUse anno = (VEmailCanUse) context.getAnnotation();
            if (StringUtils.isNotBlank(anno.suffix())
                    && paramValue.toLowerCase().endsWith(anno.suffix().toLowerCase())) {
                if (StringUtils.isNotBlank(anno.msg())) {
                    return ValidateResult.builder(context).msg(anno.msg()).build();
                }
                return ValidateResult.builder(context).msg(I18N_KEY, I18N_DEFAULT, paramValue).matched(true).build();
            }
        }
        return null;
    }
}

/**
 * 自定义验证器使用示例
 *
 * @author YMP Team
 * @since 2.1.4-dev
 */
class CustomValidatorDemo {
    @VEmailCanUse(suffix = "@qq.com", msg = "邮箱不能使用QQ邮箱")
    private String email;

    public static void main(String[] args) {
        Validations.get().registerValidator(VEmailCanUse.class, EmailCanUseValidator.class);
    }
}
```

## 5. 配置速查

### 5.1 国际化资源validation_zh_CN.properties key清单

| Key | 默认值（中文） | 说明 |
|-----|-------------|------|
| `ymp.validation.required` | {0}为必填项. | @VRequired |
| `ymp.validation.email` | {0}不是有效的邮箱地址 | @VEmail |
| `ymp.validation.length_between` | {0}长度必须介于{1}与{2}之间 | @VLength范围 |
| `ymp.validation.length_eq` | {0}长度必须等于{1} | @VLength固定 |
| `ymp.validation.length_min` | {0}长度必须大于{1} | @VLength最小 |
| `ymp.validation.length_max` | {0}长度必须小于{1} | @VLength最大 |
| `ymp.validation.size_between` | {0}元素数量必须介于{1}与{2}之间 | @VSize范围 |
| `ymp.validation.size_eq` | {0}元素数量必须等于{1} | @VSize固定 |
| `ymp.validation.size_min` | {0}元素数量必须大于{1} | @VSize最小 |
| `ymp.validation.size_max` | {0}元素数量必须小于{1} | @VSize最大 |
| `ymp.validation.compare_eq` | {0}必须与{1}相同 | @VCompare EQ |
| `ymp.validation.compare_not_eq` | {0}不能与{1}相同 | @VCompare NOT_EQ |
| `ymp.validation.compare_gt` | {0}必须大于{1} | @VCompare GT |
| `ymp.validation.numeric` | {0}不是有效的数字 | @VNumeric |
| `ymp.validation.numeric_between` | {0}数值必须介于{1}与{2}之间 | @VNumeric范围 |
| `ymp.validation.numeric_decimals` | {0}数值必须保留{1}位小数 | @VNumeric小数 |
| `ymp.validation.regex` | {0}正则表达式不匹配 | @VRegex |
| `ymp.validation.mobile` | {0}不是有效的手机号码 | @VMobile |
| `ymp.validation.id_card` | {0}不是有效的身份证号码 | @VIDCard |
| `ymp.validation.datetime` | {0}不是有效的日期 | @VDateTime |
| `ymp.validation.data_range_invalid` | {0}值超出数据范围 | @VDataRange |
| `ymp.validation.rsa_data_invalid` | {0}不是有效的RSA数据 | @VRSAData |

### 5.2 注解配置核心参数

| 注解 | 核心参数重点 |
|-----|-----------|
| @Validation | mode: NORMAL(短路，一旦出错即停) / FULL(全量返回所有错误) |
| @VCompare | with必须填写对方参数名（与@VField.value一致）；withLabel用于显示 |
| @DateTime | pattern默认`yyyy-MM-dd HH:mm:ss`；时间段用separator分隔，single=false时才解析时间段 |
| @VModel | prefix拼接子对象参数名：prefix="ext" → 子字段age参数为"ext.age" |
| @VCondition | FIELD_*比较时expectedValue为字符串；GT/LT比较时按数值大小比较 |
| groups参数 | 空数组 → 与任何分组都不匹配；传groups时取交集（注解groups ∩ 验证groups 非空才验证） |
| @ValidateGroups | 方法级覆盖类级；显式validate传groups参数优先级最高 |

## 6. 常见坑点排查

| 现象 | 原因 | 解决方案 |
|-----|------|---------|
| 加了验证注解但完全不验证 | 1. 类没加@Validation；2. 验证时传的groups与注解groups无交集；3. 字段不是public且无getter | 1. 类/方法上标注@Validation；2. 不传groups默认DefaultGroup；3. 确保getter存在 |
| @VCondition写了但没生效 | condition.field写错（应为@VField.value或属性名）；expectedValue类型不匹配 | 检查@VField.value/属性名是否和condition.field完全一致；GT/LT条件expectedValue传数值字符串 |
| @VModel嵌套对象字段全部未验证 | 1. 未加@VModel注解；2. @VModel.prefix与实际参数前缀不一致 | 1. 嵌套对象字段标注@VModel；2. 参数名按"prefix.子字段名"格式传入，如"ext.age" |
| 返回消息是key本身（如ymp.validation.required）而不是中文 | 类路径下缺少validation_zh_CN.properties；或JVM locale不是zh_CN | 确认resources目录存在validation_zh_CN.properties（UTF-8编码）；或@VMsg直接写消息 |
| 自定义Validator验证无反应 | 1. 注解未加@Validator(Anno.class)在实现类上；2. 注解缺少groups/condition属性（通用参数）；3. 手动调用了registerValidator但顺序在validate之后 | 1. IValidator类必须标注@Validator；2. 自定义注解中定义`Class<?>[] groups() default {}; VCondition condition() default @VCondition;`；3. 初始化时先注册再验证 |
| FULL模式下仍只返回第一个错误 | 类上@Validation未写mode=Validation.MODE.FULL，默认NORMAL | 在类@Validation或方法@Validation上显式设置mode=FULL |
