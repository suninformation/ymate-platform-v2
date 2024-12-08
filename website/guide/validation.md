---
sidebar_position: 8
slug: validation
---

# 验证（Validation）

验证模块是服务端参数有效性验证工具，采用注解声明方式配置验证规则，更简单、更直观、更友好，支持方法参数和类成员属性验证，支持验证结果国际化 I18N 资源绑定，支持自定义验证器，支持多种验证模式。

## Maven包依赖

```xml
<dependency>
    <groupId>net.ymate.platform</groupId>
    <artifactId>ymate-platform-validation</artifactId>
    <version>2.1.3</version>
</dependency>
```



## 基础注解及参数说明



### @Validation

声明在类或类方法之上，用于配置验证模式和自定义国际化资源文件。

当目标类和方法上都声明了该注解，则方法上的声明将被优先使用。

| 配置项        | 描述                                                         |
| ------------- | ------------------------------------------------------------ |
| mode          | 验证模式，默认为 `NORMAL`<br/>`NORMAL` - 短路式验证，即验证过程中一旦出现未通过即刻终止验证<br/>`FULL` - 对类属性或方法参数进行全部验证 |
| resourcesName | 自定义 I18N 资源文件名称，默认为空表示采用系统默认           |



### @Validator

声明一个类为验证器。

| 配置项 | 描述                           |
| ------ | ------------------------------ |
| value  | 设置与验证器绑定的验证注解类型 |



### @VField

指定待验证的成员或方法参数名称的注解。

| 配置项 | 描述                                             |
| ------ | ------------------------------------------------ |
| prefix | 绑定的参数名称前缀 @since 2.1.3                  |
| value  | 参数名称（用于与集成端业务参数一致）@since 2.1.3 |
| name   | 自定义参数名称（用于显示）                       |
| label  | 自定义参数I18n标签名称                           |



### @VModel

声明目标对象是否为 JavaBean 对象，将执行对象嵌套验证。

| 配置项 | 描述                            |
| ------ | ------------------------------- |
| prefix | 绑定的参数名称前缀 @since 2.1.3 |





### @VMsg

自定义验证消息，用于替代验证器返回的消息内容。

验证器注解中的 `msg` 参数优先级高于 `@VMsg` 注解。

| 配置项 | 描述     |
| ------ | -------- |
| value  | 消息内容 |



## 默认验证器及参数说明



### @VCompare

比较两个参数值，使用场景如新密码与重复新密码两参数值是否一致的比较等。

| 配置项    | 描述                                                         |
| --------- | ------------------------------------------------------------ |
| cond      | 比较条件（枚举值），默认为 `EQ`<br/>取值范围：`EQ`、`NOT_EQ`、`GT`、`GT_EQ`、`LT`和`LT_EQ` |
| with      | 与之比较的参数名称                                           |
| withLabel | 与之比较的参数标签名称 （用于在验证消息里显示的名称），默认为空 |
| msg       | 自定义验证消息，默认为空                                     |



### @VDataRange

验证参数值是否在指定的取值范围内。

| 配置项        | 描述                                                    |
| ------------- | ------------------------------------------------------- |
| value         | 允许参数集合，若 `providerClass` 参数存在则此值将被忽略 |
| ignoreCase    | 忽略大小写，默认为 `true`                               |
| providerClass | 允许参数集合数据提供者类，默认为空                      |
| msg           | 自定义验证消息，默认为空                                |



### @VDateTime

验证参数字符串是否为有效的日期时间格式。

| 配置项    | 描述                                                         |
| --------- | ------------------------------------------------------------ |
| value     | 自定参数名称（用于存储转换后的时间毫秒值）                   |
| pattern   | 日期格式字符串，默认为 `yyyy-MM-dd HH:mm:ss`                 |
| single    | 仅接收单日期，默认为 `true`<br/>即所选日期的00点00分00秒0毫秒到所选日期的23点59分59秒0毫秒 |
| separator | 时间段字符串之间的分割符号，默认为 `/`                       |
| maxDays   | 时间段之间的天数最大差值，默认为 `0` 表示不限制              |
| msg       | 自定义验证消息，默认为空                                     |



### @VEmail

邮箱地址格式验证。

| 配置项 | 描述                     |
| ------ | ------------------------ |
| msg    | 自定义验证消息，默认为空 |



### @VIDCard

身份证号码有效性验证。

| 配置项 | 描述                     |
| ------ | ------------------------ |
| msg    | 自定义验证消息，默认为空 |



### @VLength

字符串长度验证。

| 配置项 | 描述                                                         |
| ------ | ------------------------------------------------------------ |
| min    | 设置最小长度，默认为 `0` 表示不限制                          |
| max    | 设置最大长度，默认为 `0` 表示不限制                          |
| eq     | 设置固定长度值，与 `min` 和 `max` 互斥，默认为 `0` 表示不限制 |
| msg    | 自定义验证消息，默认为空                                     |



### @VMobile

手机号码格式验证。

默认正则表达式为：

```regexp
^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\\d{8}$
```

:::tip 提示：
由于手机号段可能随时变化，因此允许通过系统参数重设：
```shell
java -Dmobile.regex=<自定义正则表达式> ...
```
:::

| 配置项 | 描述                                 |
| ------ | ------------------------------------ |
| regex  | 自定义正则表达式（将覆盖原判断逻辑） |
| msg    | 自定义验证消息，默认为空             |



### @VNumeric

验证参数值是否为有效的数值类型。

| 配置项   | 描述                                                         |
| -------- | ------------------------------------------------------------ |
| digits   | 仅检查值是否为数字，默认为 `false` <br />（仅当取值为 `true` 时生效，同时其它参数将失效） |
| min      | 设置最小值，默认为 `0` 表示不限制                            |
| max      | 设置最大值，默认为 `0` 表示不限制                            |
| eq       | 设置值相等，默认为 `0` 表示不限制                            |
| decimals | 设置小数位数，默认为 `0` 表示不限制                          |
| msg      | 自定义验证消息，默认为空                                     |



### @VRegex

正则表达式验证。

| 配置项 | 描述                     |
| ------ | ------------------------ |
| regex  | 正则表达式               |
| msg    | 自定义验证消息，默认为空 |



### @VRequired

必填项验证，即参数值不为空或数组元素数量不为0。

| 配置项 | 描述                     |
| ------ | ------------------------ |
| msg    | 自定义验证消息，默认为空 |



### @VRSAData

对指定参数进行 RSA 解码以验证其是否合法有效。

| 配置项        | 描述                     |
| ------------- | ------------------------ |
| value         | 自定参数名称，默认为空   |
| providerClass | RSA密钥数据提供者类      |
| msg           | 自定义验证消息，默认为空 |



## 验证框架使用示例

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

    //
    // 此处省略了Get/Set方法
    //
}

public class UserExt {

    @VLength(max = 10)
    private String sex;

    @VRequired
    @VNumeric(min = 18, max = 30)
    private int age;

    @VRequried
    @VEmail
    private String email;

    //
    // 此处省略了Get/Set方法
    //
}

public class Starter {

    static {
        System.setProperty(IApplication.SYSTEM_MAIN_CLASS, Starter.class.getName());
    }

    private static final Log LOG = LogFactory.getLog(Starter.class);

    public static void main(String[] args) throws Exception {
        try (IApplication application = YMP.run(args)) {
            if (application.isInitialized()) {
                Map<String, Object> paramValues = new HashMap<>();
                paramValues.put("username", "lz");
                paramValues.put("password", 1233);
                paramValues.put("repassword", "12333");
                paramValues.put("ext.age", "17");
                paramValues.put("ext.email", "@163.com");
                //
                Map<String, ValidateResult> resultMap = Validations.get()
                    .validate(UserBase.class, paramValues);
                resultMap.forEach((key, value) -> System.out.println(value));
            }
        }
    }
}
```

执行结果：

```shell
username : 用户名称长度必须在3到16之间
password : 密码无效
repassword : repassword must be equal to 密码.
ext.age : ext.age numeric must be between 18 and 30.
ext.email : ext.email not a valid email address.
```



## 国际化配置示例

验证框架默认使用的国际化资源文件名称为 `validation.properties`，请根据不同语言进行配置并将资源文件放置在类路径中，以中文为例将资源文件命名为 `validation_zh_CN.properties`，其内容如下：

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

再次执行上例中的代码，执行结果如下：

```shell
username : 用户名称长度必须在3到16之间
password : 密码无效
repassword : repassword必须与密码相同
ext.age : ext.age数值必须介于18与30之间
ext.email : ext.email不是有效的邮箱地址
```



## 自定义验证器

验证器由验证器注解和验证器接口实现类两部份组成，可以手动向框架注册，也可以通过在验证器接口实现类上声明 `@Validator` 注解绑定两者之间的关系，被 `@Validator` 注解声明的类在框架初始化时自动扫描并注册。

本例中，我们创建一个简单的自定义验证器，用来验证当前用户输入的邮箱地址是否可以使用。



### 步骤一：创建自定义验证器注解

验证器注解一般以大写字母 `V` 开头，主要是为了便于区分，无特殊含义和限制，代码如下：

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



### 步骤二：自定义验证器接口实现

作为验证器需要实现 `IValidator` 接口，也可以直接继承 `AbstractValidator` 抽象类，代码如下：

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
                    && StringUtils.endsWithIgnoreCase(paramValue, annotation.suffix())) {
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



### 如何注册自定义验证器

在 **步骤二** 中的 `EmailCanUseValidator` 类已声明了 `@Validator` 注解，若框架开启了自动扫描特性则在启动时该验证器将被自动扫描并完成注册，下面演示的是如何进行手动注册，同样非常简单，代码如下：

```java
Validations.get().registerValidator(VEmailCanUse.class, EmailCanUseValidator.class);
```



### 使用自定义验证器

本例演示 `email` 参数值不接受 QQ 邮箱地址，代码如下：

```java
public class VEmailCanUseBean {

    @VRequired
    @VEmail
    @VEmailCanUse(suffix = "@qq.com")
    private String email;

    //
    // 此处省略了Get/Set方法
    //

    public static void main(String[] args) throws Exception {
        try (IApplication application = YMP.run(args)) {
            if (application.isInitialized()) {
                Map<String, Object> paramValues = new HashMap<>();
                paramValues.put("email", "demo@qq.com");
                Map<String, ValidateResult> resultMap = Validations.get().validate(VEmailCanUseBean.class, paramValues);
                resultMap.forEach((key, value) -> System.out.printf("%s : %s%n", key, value.getMsg()));
            }
        }
    }
}
```

**执行结果：**

```shell
email : Email address "demo@qq.com" cannot be used.
```

