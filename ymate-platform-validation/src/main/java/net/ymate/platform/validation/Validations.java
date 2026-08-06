/*
 * Copyright 2007-2019 the original author or authors.
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
package net.ymate.platform.validation;

import net.ymate.platform.commons.ReentrantLockHelper;
import net.ymate.platform.commons.lang.BlurObject;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.IApplicationConfigureFactory;
import net.ymate.platform.core.IApplicationConfigurer;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.beans.BeanMeta;
import net.ymate.platform.core.beans.IBeanLoadFactory;
import net.ymate.platform.core.beans.IBeanLoader;
import net.ymate.platform.core.module.IModule;
import net.ymate.platform.validation.annotation.VCondition;
import net.ymate.platform.validation.annotation.ValidateGroups;
import net.ymate.platform.validation.annotation.Validation;
import net.ymate.platform.validation.annotation.Validator;
import net.ymate.platform.validation.handle.ValidateHandler;
import net.ymate.platform.validation.validate.*;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 验证框架模块管理器
 *
 * @author 刘镇 (suninformation@163.com) on 2013-4-7 下午4:43:48
 */
public final class Validations implements IModule, IValidation {

    private static final Log LOG = LogFactory.getLog(Validations.class);

    private static volatile IValidation instance;

    private IApplication owner;

    private boolean initialized;

    private final Map<Class<? extends Annotation>, Class<? extends IValidator>> validators = new ConcurrentHashMap<>();

    private final Map<Class<?>, ValidationMeta> validationMetaMap = new ConcurrentHashMap<>();

    public static IValidation get() {
        IValidation inst = instance;
        if (inst == null) {
            synchronized (Validations.class) {
                inst = instance;
                if (inst == null) {
                    instance = inst = YMP.get().getModuleManager().getModule(Validations.class);
                }
            }
        }
        return inst;
    }

    public Validations() {
    }

    @Override
    public String getName() {
        return MODULE_NAME;
    }

    @Override
    public IApplication getOwner() {
        return owner;
    }

    @Override
    public void initialize(IApplication owner) {
        if (!initialized) {
            //
            YMP.showModuleVersion("ymate-platform-validation", this);
            //
            this.owner = owner;
            //
            IApplicationConfigureFactory configureFactory = owner.getConfigureFactory();
            if (configureFactory != null) {
                IApplicationConfigurer configurer = configureFactory.getConfigurer();
                if (configurer != null) {
                    IBeanLoadFactory beanLoaderFactory = configurer.getBeanLoadFactory();
                    if (beanLoaderFactory != null) {
                        IBeanLoader beanLoader = beanLoaderFactory.getBeanLoader();
                        if (beanLoader != null) {
                            beanLoader.registerHandler(Validator.class, new ValidateHandler(this));
                        }
                    }
                }
            }
            //
            initialized = true;
            //
            registerValidator(VRequired.class, RequiredValidator.class);
            registerValidator(VRegex.class, RegexValidator.class);
            registerValidator(VNumeric.class, NumericValidator.class);
            registerValidator(VMobile.class, MobileValidator.class);
            registerValidator(VLength.class, LengthValidator.class);
            registerValidator(VEmail.class, EmailValidator.class);
            registerValidator(VDateTime.class, DateTimeValidator.class);
            registerValidator(VDataRange.class, DataRangeValidator.class);
            registerValidator(VCompare.class, CompareValidator.class);
            registerValidator(VRSAData.class, RSADataValidator.class);
            registerValidator(VIDCard.class, IDCardValidator.class);
            registerValidator(VSize.class, SizeValidator.class);
        }
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void close() {
        if (initialized) {
            initialized = false;
            //
            validationMetaMap.clear();
            validators.clear();
            owner = null;
        }
    }

    @Override
    public void registerValidator(Class<? extends Annotation> annotationClass, Class<? extends IValidator> validatorClass) {
        if (initialized) {
            validators.put(annotationClass, validatorClass);
            owner.getBeanFactory().registerBean(BeanMeta.create(validatorClass, true));
        }
    }

    @Override
    public boolean containsValidator(Class<? extends Annotation> annotationClass) {
        return validators.containsKey(annotationClass);
    }

    @Override
    public Map<String, ValidateResult> validate(Class<?> targetClass, Map<String, Object> paramValues) {
        return validate(targetClass, paramValues, (Class<?>[]) null);
    }

    @Override
    public Map<String, ValidateResult> validate(Class<?> targetClass, Method targetMethod, Map<String, Object> paramValues) {
        return validate(targetClass, targetMethod, paramValues, (Class<?>[]) null);
    }

    @Override
    public Map<String, ValidateResult> validate(Class<?> targetClass, Map<String, Object> paramValues, Class<?>... groups) {
        Map<String, ValidateResult> returnValues = new LinkedHashMap<>();
        if (initialized) {
            // 若未显式传入分组，从目标类上读取@ValidateGroups注解
            if (ArrayUtils.isEmpty(groups)) {
                groups = resolveGroupsFromClass(targetClass);
            }
            ValidationMeta validationMeta = bindValidationMeta(targetClass);
            if (validationMeta != null) {
                Map<String, String> contextParams = owner.getInterceptSettings().getContextParams(owner, targetClass);
                for (Map.Entry<String, ValidationMeta.ParamInfo> entry : validationMeta.getFields().entrySet()) {
                    ValidateResult validateResult = doValidate(entry.getValue(), paramValues, contextParams, validationMeta.getResourcesName(), groups);
                    if (validateResult != null && validateResult.isMatched()) {
                        returnValues.put(entry.getKey(), validateResult);
                        if (validationMeta.getMode() == Validation.MODE.NORMAL) {
                            break;
                        }
                    }
                }
            }
        }
        return returnValues;
    }

    @Override
    public Map<String, ValidateResult> validate(Class<?> targetClass, Method targetMethod, Map<String, Object> paramValues, Class<?>... groups) {
        Map<String, ValidateResult> returnValues = new LinkedHashMap<>();
        if (initialized) {
            // 若未显式传入分组，优先从方法上读取@ValidateGroups，再从类上读取
            if (ArrayUtils.isEmpty(groups)) {
                groups = resolveGroupsFromMethod(targetMethod, targetClass);
            }
            ValidationMeta validationMeta = bindValidationMeta(targetClass);
            if (validationMeta != null) {
                ValidationMeta.MethodInfo methodInfo = validationMeta.getMethod(targetMethod);
                if (methodInfo != null) {
                    Validation.MODE mode = methodInfo.getValidation() == null ? validationMeta.getMode() : methodInfo.getValidation().mode();
                    String resourceName = methodInfo.getValidation() == null ? validationMeta.getResourcesName() : StringUtils.defaultIfBlank(methodInfo.getValidation().resourcesName(), validationMeta.getResourcesName());
                    //
                    Map<String, String> contextParams = owner.getInterceptSettings().getContextParams(owner, targetClass, targetMethod);
                    for (Map.Entry<String, ValidationMeta.ParamInfo> entry : methodInfo.getParams().entrySet()) {
                        ValidateResult validateResult = doValidate(entry.getValue(), paramValues, contextParams, resourceName, groups);
                        if (validateResult != null && validateResult.isMatched()) {
                            returnValues.put(entry.getKey(), validateResult);
                            if (Validation.MODE.NORMAL.equals(mode)) {
                                break;
                            }
                        }
                    }
                }
            }
        }
        return returnValues;
    }

    /**
     * @param targetClass 目标类型
     * @return 缓存中获取目标类型验证配置描述，若不存在则尝试创建它并加入缓存中
     */
    private ValidationMeta bindValidationMeta(Class<?> targetClass) {
        try {
            return ReentrantLockHelper.putIfAbsentAsync(validationMetaMap, targetClass, () -> new ValidationMeta(this, targetClass));
        } catch (Exception e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
            }
        }
        return null;
    }

    private ValidateResult doValidate(ValidationMeta.ParamInfo paramInfo, Map<String, Object> paramValues, Map<String, String> contextParams, String resourceName, Class<?>[] groups) {
        ValidateResult validateResult = null;
        for (Annotation ann : paramInfo.getAnnotations()) {
            // 分组过滤
            if (!matchesGroup(paramInfo, ann, groups)) {
                continue;
            }
            // 条件判断
            if (!meetsCondition(paramInfo, ann, paramValues)) {
                continue;
            }
            // 执行验证
            IValidator validator = owner.getBeanFactory().getBean(validators.get(ann.annotationType()));
            validateResult = validator.validate(new ValidateContext(owner, ann, paramInfo, paramValues, contextParams, resourceName));
            if (validateResult != null && validateResult.isMatched()) {
                break;
            }
        }
        return validateResult;
    }

    /**
     * 从目标类上解析@ValidateGroups注解声明的分组
     *
     * @param targetClass 目标类
     * @return 分组数组，若类上无@ValidateGroups注解则返回null（由matchesGroup方法默认为DefaultGroup）
     * @since 2.1.4
     */
    private Class<?>[] resolveGroupsFromClass(Class<?> targetClass) {
        ValidateGroups validateGroups = targetClass.getAnnotation(ValidateGroups.class);
        return validateGroups != null ? validateGroups.value() : null;
    }

    /**
     * 从目标方法上解析@ValidateGroups注解声明的分组，若方法上无注解则尝试从类上获取
     *
     * @param targetMethod 目标方法
     * @param targetClass  目标类
     * @return 分组数组，若均无@ValidateGroups注解则返回null（由matchesGroup方法默认为DefaultGroup）
     * @since 2.1.4
     */
    private Class<?>[] resolveGroupsFromMethod(Method targetMethod, Class<?> targetClass) {
        if (targetMethod != null) {
            ValidateGroups validateGroups = targetMethod.getAnnotation(ValidateGroups.class);
            if (validateGroups != null) {
                return validateGroups.value();
            }
        }
        return resolveGroupsFromClass(targetClass);
    }

    /**
     * 判断验证注解是否匹配当前分组
     *
     * @param paramInfo 参数描述
     * @param ann       验证注解
     * @param groups    当前激活的分组
     * @return 若匹配则返回true
     * @since 2.1.4
     */
    private boolean matchesGroup(ValidationMeta.ParamInfo paramInfo, Annotation ann, Class<?>[] groups) {
        Class<?>[] annGroups = paramInfo.getAnnotationGroups(ann);
        if (ArrayUtils.isEmpty(groups)) {
            groups = new Class<?>[]{DefaultGroup.class};
        }
        for (Class<?> g : groups) {
            for (Class<?> ag : annGroups) {
                if (g == ag) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断验证注解的条件是否满足
     *
     * @param paramInfo   参数描述
     * @param ann         验证注解
     * @param paramValues 参数集合
     * @return 若条件满足则返回true
     * @since 2.1.4
     */
    private boolean meetsCondition(ValidationMeta.ParamInfo paramInfo, Annotation ann, Map<String, Object> paramValues) {
        VCondition condition = paramInfo.getAnnotationCondition(ann);
        if (condition == null) {
            return true;
        }
        String fieldName = condition.field();
        if (StringUtils.isBlank(fieldName)) {
            return true;
        }
        // 获取依赖字段的值
        Object depValue = getDependentFieldValue(fieldName, paramValues, paramInfo);
        switch (condition.type()) {
            case FIELD_NOT_EMPTY:
                return !RequiredValidator.validate(depValue);
            case FIELD_EMPTY:
                return RequiredValidator.validate(depValue);
            case FIELD_EQUALS:
                return Strings.CS.equals(BlurObject.bind(depValue).toStringValue(), condition.expectedValue());
            case FIELD_NOT_EQUALS:
                return !Strings.CS.equals(BlurObject.bind(depValue).toStringValue(), condition.expectedValue());
            case FIELD_GT:
                return compareValues(depValue, condition.expectedValue()) > 0;
            case FIELD_GT_EQ:
                return compareValues(depValue, condition.expectedValue()) >= 0;
            case FIELD_LT:
                return compareValues(depValue, condition.expectedValue()) < 0;
            case FIELD_LT_EQ:
                return compareValues(depValue, condition.expectedValue()) <= 0;
            default:
                return true;
        }
    }

    /**
     * 获取依赖字段的值
     *
     * @param fieldName   字段名称
     * @param paramValues 参数集合
     * @param paramInfo   当前参数描述
     * @return 依赖字段的值
     * @since 2.1.4
     */
    private Object getDependentFieldValue(String fieldName, Map<String, Object> paramValues, ValidationMeta.ParamInfo paramInfo) {
        // 尝试直接从paramValues中获取
        Object value = paramValues.get(fieldName);
        if (value == null && paramInfo.getName() != null) {
            // 尝试基于当前参数的层级关系构建依赖字段名称
            String parentPrefix = paramInfo.getPrefix();
            if (StringUtils.isNotBlank(parentPrefix)) {
                String fullFieldName = ValidationMeta.parsePrefixValue(parentPrefix, fieldName);
                value = paramValues.get(fullFieldName);
            }
        }
        return value;
    }

    /**
     * 比较两个数值
     *
     * @param value1       第一个值
     * @param expectedStr2 预期值字符串
     * @return 比较结果
     * @since 2.1.4
     */
    private int compareValues(Object value1, String expectedStr2) {
        String str1 = BlurObject.bind(value1).toStringValue();
        if (StringUtils.isNumeric(str1) && StringUtils.isNumeric(expectedStr2)) {
            return new java.math.BigDecimal(str1).compareTo(new java.math.BigDecimal(expectedStr2));
        }
        return 0;
    }
}
