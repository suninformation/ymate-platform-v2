# YMP 验证模块（Validation）技能文档

## 模块概述

验证模块是 YMP 框架中的服务端参数有效性验证工具，采用注解声明方式配置验证规则，更简单、更直观、更友好。该模块支持方法参数和类成员属性验证，支持验证结果国际化 I18N 资源绑定，支持自定义验证器，支持多种验证模式。

## 核心功能

- **注解式验证**：通过简单的注解声明，快速配置验证规则
- **多种验证器**：内置丰富的验证器，覆盖常见验证场景
- **国际化支持**：验证错误信息支持 I18N 国际化资源绑定
- **自定义验证器**：支持开发者自定义验证器，扩展验证能力
- **多层级验证**：支持嵌套对象验证，实现复杂数据结构的验证
- **验证模式**：支持短路式验证（NORMAL）和全量验证（FULL）两种模式

## 架构设计

### 核心架构

验证模块采用分层架构设计，主要包含以下几个核心组件：

1. **验证器接口（IValidator）**：所有验证器的基础接口，定义验证逻辑
2. **验证上下文（ValidateContext）**：存储验证过程中的上下文信息
3. **验证结果（ValidateResult）**：封装验证结果和错误信息
4. **验证管理器（IValidation）**：管理验证器注册和执行验证过程
5. **验证注解**：声明式配置验证规则的注解

### 验证流程

1. 开发者在需要验证的类或方法上添加 `@Validation` 注解
2. 在需要验证的字段或参数上添加具体的验证注解（如 `@VRequired`、`@VEmail` 等）
3. 调用 `Validations.get().validate()` 方法执行验证
4. 验证管理器根据注解配置执行相应的验证器
5. 收集验证结果并返回验证错误信息

## 核心 API

### 验证管理器

```java
// 获取验证管理器实例
IValidation validation = Validations.get();

// 注册自定义验证器
validation.registerValidator(MyValidationAnnotation.class, MyValidator.class);

// 执行类成员参数验证
Map<String, ValidateResult> results = validation.validate(User.class, paramValues);

// 执行类方法参数验证
Map<String, ValidateResult> results = validation.validate(User.class, userMethod, paramValues);
```

### 验证器接口

```java
public interface IValidator {
    /**
     * 执行验证逻辑
     * @param context 验证上下文
     * @return 验证结果，验证通过返回null，否则返回验证错误信息
     */
    ValidateResult validate(ValidateContext context);
}
```

### 验证注解

#### @Validation

声明在类或类方法之上，用于配置验证模式和自定义国际化资源文件。

| 配置项 | 描述 |
|-------|------|
| mode | 验证模式，默认为 NORMAL<br/>NORMAL - 短路式验证，即验证过程中一旦出现未通过即刻终止验证<br/>FULL - 对类属性或方法参数进行全部验证 |
| resourcesName | 自定义 I18N 资源文件名称，默认为空表示采用系统默认 |

#### @VField

指定待验证的成员或方法参数名称的注解。

| 配置项 | 描述 |
|-------|------|
| prefix | 绑定的参数名称前缀 |
| value | 参数名称（用于与集成端业务参数一致） |
| name | 自定义参数名称（用于显示） |
| label | 自定义参数I18n标签名称 |

#### @VModel

声明目标对象是否为 JavaBean 对象，将执行对象嵌套验证。

| 配置项 | 描述 |
|-------|------|
| prefix | 绑定的参数名称前缀 |

#### @VMsg

自定义验证消息，用于替代验证器返回的消息内容。

| 配置项 | 描述 |
|-------|------|
| value | 消息内容 |

## 内置验证器

### @VRequired

必填项验证，即参数值不为空或数组元素数量不为0。

| 配置项 | 描述 |
|-------|------|
| msg | 自定义验证消息，默认为空 |

### @VEmail

邮箱地址格式验证。

| 配置项 | 描述 |
|-------|------|
| msg | 自定义验证消息，默认为空 |

### @VLength

字符串长度验证。

| 配置项 | 描述 |
|-------|------|
| min | 设置最小长度，默认为 0 表示不限制 |
| max | 设置最大长度，默认为 0 表示不限制 |
| eq | 设置固定长度值，与 min 和 max 互斥，默认为 0 表示不限制 |
| msg | 自定义验证消息，默认为空 |

### @VSize

集合或数组元素数量验证。

| 配置项 | 描述 |
|-------|------|
| min | 设置最小元素数量，默认为 0 表示不限制 |
| max | 设置最大元素数量，默认为 0 表示不限制 |
| eq | 设置固定元素数量值，与 min 和 max 互斥，默认为 0 表示不限制 |
| msg | 自定义验证消息，默认为空 |

### @VCompare

比较两个参数值，使用场景如新密码与重复新密码两参数值是否一致的比较等。

| 配置项 | 描述 |
|-------|------|
| cond | 比较条件（枚举值），默认为 EQ<br/>取值范围：EQ、NOT_EQ、GT、GT_EQ、LT和LT_EQ |
| with | 与之比较的参数名称 |
| withLabel | 与之比较的参数标签名称 （用于在验证消息里显示的名称），默认为空 |
| msg | 自定义验证消息，默认为空 |

### @VDataRange

验证参数值是否在指定的取值范围内。

| 配置项 | 描述 |
|-------|------|
| value | 允许参数集合，若 providerClass 参数存在则此值将被忽略 |
| ignoreCase | 忽略大小写，默认为 true |
| providerClass | 允许参数集合数据提供者类，默认为空 |
| msg | 自定义验证消息，默认为空 |

### @VDateTime

验证参数字符串是否为有效的日期时间格式。

| 配置项 | 描述 |
|-------|------|
| value | 自定参数名称（用于存储转换后的时间毫秒值） |
| pattern | 日期格式字符串，默认为 yyyy-MM-dd HH:mm:ss |
| single | 仅接收单日期，默认为 true<br/>即所选日期的00点00分00秒0毫秒到所选日期的23点59分59秒0毫秒 |
| separator | 时间段字符串之间的分割符号，默认为 / |
| maxDays | 时间段之间的天数最大差值，默认为 0 表示不限制 |
| msg | 自定义验证消息，默认为空 |

### @VIDCard

身份证号码有效性验证。

| 配置项 | 描述 |
|-------|------|
| msg | 自定义验证消息，默认为空 |

### @VMobile

手机号码格式验证。

| 配置项 | 描述 |
|-------|------|
| regex | 自定义正则表达式（将覆盖原判断逻辑） |
| msg | 自定义验证消息，默认为空 |

### @VNumeric

验证参数值是否为有效的数值类型。

| 配置项 | 描述 |
|-------|------|
| digits | 仅检查值是否为数字，默认为 false<br/>（仅当取值为 true 时生效，同时其它参数将失效） |
| min | 设置最小值，默认为 0 表示不限制 |
| max | 设置最大值，默认为 0 表示不限制 |
| eq | 设置值相等，默认为 0 表示不限制 |
| decimals | 设置小数位数，默认为 0 表示不限制 |
| msg | 自定义验证消息，默认为空 |

### @VRSAData

对指定参数进行 RSA 解码以验证其是否合法有效。

| 配置项 | 描述 |
|-------|------|
| value | 自定参数名称，默认为空 |
| providerClass | RSA密钥数据提供者类 |
| msg | 自定义验证消息，默认为空 |

### @VRegex

正则表达式验证。

| 配置项 | 描述 |
|-------|------|
| regex | 正则表达式 |
| msg | 自定义验证消息，默认为空 |

## 配置与使用

### 基本配置

验证模块无需特殊配置，默认情况下会自动加载 `validation.properties` 国际化资源文件。

### 使用示例

#### 示例一：基本验证

```java
@Validation(mode = Validation.MODE.FULL)
public class UserBase {

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

    @VModel
    @VField(name = "ext")
    private UserExt userExt;

    // Getter和Setter方法
}

public class UserExt {

    @VLength(max = 10)
    private String sex;

    @VRequired
    @VNumeric(min = 18, max = 30)
    private int age;

    @VRequired
    @VEmail
    private String email;

    // Getter和Setter方法
}

// 执行验证
Map<String, Object> paramValues = new HashMap<>();
paramValues.put("username", "lz");
paramValues.put("password", 1233);
paramValues.put("repassword", "12333");
paramValues.put("ext.age", "17");
paramValues.put("ext.email", "@163.com");

Map<String, ValidateResult> resultMap = Validations.get()
    .validate(UserBase.class, paramValues);
resultMap.forEach((key, value) -> System.out.println(value));
```

#### 示例二：自定义验证器

1. **创建验证器注解**

```java
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface VEmailCanUse {
    /**
     * @return 邮箱后缀
     */
    String suffix();

    /**
     * @return 自定义验证消息
     */
    String msg() default "";
}
```

2. **实现验证器**

```java
@Validator(VEmailCanUse.class)
public class EmailCanUseValidator implements IValidator {

    private static final String I18N_MESSAGE_KEY = "ymp.validation.email_can_use";

    private static final String I18N_MESSAGE_DEFAULT_VALUE = "E-mail address \"{1}\" cannot be used.";

    @Override
    public ValidateResult validate(ValidateContext context) {
        String paramValue = BlurObject.bind(context.getParamValue()).toStringValue();
        if (StringUtils.isNotBlank(paramValue)) {
            VEmailCanUse annotation = (VEmailCanUse) context.getAnnotation();
            if (StringUtils.isNotBlank(annotation.suffix())
                    && Strings.CS.endsWithIgnoreCase(paramValue, annotation.suffix())) {
                if (StringUtils.isNotBlank(annotation.msg())) {
                    return ValidateResult.builder(context).msg(annotation.msg()).build();
                }
                return ValidateResult.builder(context).msg(I18N_MESSAGE_KEY, I18N_MESSAGE_DEFAULT_VALUE, paramValue).matched(true).build();
            }
        }
        return null;
    }
}
```

3. **注册验证器**

```java
// 自动注册（通过@Validator注解）
// 或手动注册
Validations.get().registerValidator(VEmailCanUse.class, EmailCanUseValidator.class);
```

4. **使用验证器**

```java
public class User {
    @VRequired
    @VEmail
    @VEmailCanUse(suffix = "@qq.com")
    private String email;

    // Getter和Setter方法
}
```

## 国际化支持

验证框架默认使用的国际化资源文件名称为 `validation.properties`，请根据不同语言进行配置并将资源文件放置在类路径中。

### 中文资源文件示例（validation_zh_CN.properties）

```properties
# CompareValidator
ymp.validation.compare_not_eq={0}不能与{1}相同
ymp.validation.compare_eq={0}必须与{1}相同
ymp.validation.compare_gt={0}必须大于{1}
ymp.validation.compare_gt_eq={0}必须大于或等于{1}
ymp.validation.compare_lt={0}必须小于{1}
ymp.validation.compare_lt_eq={0}小于或等于{1}

# DataRangeValidator
ymp.validation.data_range_invalid={0}值超出数据范围

# DateTimeValidator
ymp.validation.datetime={0}不是有效的日期
ymp.validation.datetime_max_days={0}超出最大天数范围

# EmailValidator
ymp.validation.email={0}不是有效的邮箱地址

# LengthValidator
ymp.validation.length_between={0}长度必须介于{1}与{2}之间
ymp.validation.length_eq={0}长度必须等于{1}
ymp.validation.length_min={0}长度必须大于{1}
ymp.validation.length_max={0}长度必须小于{1}

# SizeValidator
ymp.validation.size_between={0}元素数量必须介于{1}与{2}之间
ymp.validation.size_eq={0}元素数量必须等于{1}
ymp.validation.size_min={0}元素数量必须大于{1}
ymp.validation.size_max={0}元素数量必须小于{1}

# NumericValidator
ymp.validation.numeric={0}不是有效的数字
ymp.validation.numeric_between={0}数值必须介于{1}与{2}之间
ymp.validation.numeric_decimals={0}数值必须保留{1}位小数
ymp.validation.numeric_eq={0}数值必须等于{1}
ymp.validation.numeric_min={0}数值必须大于或等于{1}
ymp.validation.numeric_max={0}数值必须小于或等于{1}

# RegexValidator
ymp.validation.regex={0}正则表达式不匹配.

# RequiredValidator
ymp.validation.required={0}为必填项.

# RSADataValidator
ymp.validation.rsa_data_invalid={0}不是有效的RSA数据

# MobileValidator
ymp.validation.mobile={0}不是有效的手机号码

# IDCardValidator
ymp.validation.id_card={0}不是有效的身份证号码
```

## 最佳实践

1. **合理使用验证模式**：对于简单表单，使用默认的 NORMAL 模式即可；对于复杂表单，建议使用 FULL 模式获取所有验证错误。

2. **使用国际化资源**：通过国际化资源文件管理验证错误消息，便于后续维护和多语言支持。

3. **组合验证器**：根据业务需求组合使用多个验证器，如 `@VRequired` + `@VEmail` 实现必填邮箱验证。

4. **自定义验证器**：对于业务特有的验证逻辑，创建自定义验证器以提高代码复用性。

5. **验证消息清晰**：验证错误消息应清晰明确，告知用户具体的错误原因和修正方法。

6. **嵌套对象验证**：对于复杂数据结构，使用 `@VModel` 实现嵌套对象的验证。

## 常见问题与解决方案

### 1. 验证器不生效

**问题**：添加了验证注解但验证器未执行。

**解决方案**：
- 确保验证器类上添加了 `@Validator` 注解
- 检查验证器是否正确注册到验证管理器
- 确认验证方法调用是否正确

### 2. 国际化消息不显示

**问题**：验证错误消息未显示国际化内容。

**解决方案**：
- 确保国际化资源文件存在且命名正确
- 检查资源文件编码是否为 UTF-8
- 确认系统语言设置是否正确

### 3. 嵌套对象验证失败

**问题**：嵌套对象的验证规则未执行。

**解决方案**：
- 确保在嵌套对象字段上添加了 `@VModel` 注解
- 检查嵌套对象的验证注解是否正确配置

### 4. 自定义验证器开发问题

**问题**：自定义验证器开发后无法正常工作。

**解决方案**：
- 确保验证器实现了 `IValidator` 接口
- 检查验证器注册是否正确
- 验证 `validate` 方法的实现逻辑是否正确

## 总结

验证模块是 YMP 框架中一个强大而灵活的参数验证工具，通过注解式配置和丰富的验证器，大大简化了服务端参数验证的开发工作。它不仅支持常见的验证场景，还提供了自定义验证器的扩展能力，满足各种复杂业务需求。

合理使用验证模块，可以有效提高代码质量，减少运行时错误，提升用户体验。通过本文档的介绍，相信开发者能够快速掌握验证模块的使用方法，并在实际项目中灵活应用。
