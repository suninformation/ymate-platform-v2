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
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.beans.BeanMeta;
import net.ymate.platform.core.beans.IBeanLoadFactory;
import net.ymate.platform.core.module.IModule;
import net.ymate.platform.validation.annotation.Validation;
import net.ymate.platform.validation.annotation.Validator;
import net.ymate.platform.validation.handle.ValidateHandler;
import net.ymate.platform.validation.validate.*;
import org.apache.commons.lang3.StringUtils;
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
            IBeanLoadFactory beanLoaderFactory = YMP.getBeanLoadFactory();
            if (beanLoaderFactory != null && beanLoaderFactory.getBeanLoader() != null) {
                beanLoaderFactory.getBeanLoader().registerHandler(Validator.class, new ValidateHandler(this));
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
        Map<String, ValidateResult> returnValues = new LinkedHashMap<>();
        if (initialized) {
            ValidationMeta validationMeta = bindValidationMeta(targetClass);
            if (validationMeta != null) {
                Map<String, String> contextParams = owner.getInterceptSettings().getContextParams(owner, targetClass);
                for (Map.Entry<String, ValidationMeta.ParamInfo> entry : validationMeta.getFields().entrySet()) {
                    ValidateResult validateResult = doValidate(entry.getValue(), paramValues, contextParams, validationMeta.getResourcesName());
                    if (validateResult != null && validateResult.isMatched()) {
                        returnValues.put(validateResult.getName(), validateResult);
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
    public Map<String, ValidateResult> validate(Class<?> targetClass, Method targetMethod, Map<String, Object> paramValues) {
        Map<String, ValidateResult> returnValues = new LinkedHashMap<>();
        if (initialized) {
            ValidationMeta validationMeta = bindValidationMeta(targetClass);
            if (validationMeta != null) {
                ValidationMeta.MethodInfo methodInfo = validationMeta.getMethod(targetMethod);
                if (methodInfo != null) {
                    Validation.MODE mode = methodInfo.getValidation() == null ? validationMeta.getMode() : methodInfo.getValidation().mode();
                    String resourceName = methodInfo.getValidation() == null ? validationMeta.getResourcesName() : StringUtils.defaultIfBlank(methodInfo.getValidation().resourcesName(), validationMeta.getResourcesName());
                    //
                    Map<String, String> contextParams = owner.getInterceptSettings().getContextParams(owner, targetClass, targetMethod);
                    for (Map.Entry<String, ValidationMeta.ParamInfo> entry : methodInfo.getParams().entrySet()) {
                        ValidateResult validateResult = doValidate(entry.getValue(), paramValues, contextParams, resourceName);
                        if (validateResult != null && validateResult.isMatched()) {
                            returnValues.put(validateResult.getName(), validateResult);
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

    private ValidateResult doValidate(ValidationMeta.ParamInfo paramInfo, Map<String, Object> paramValues, Map<String, String> contextParams, String resourceName) {
        ValidateResult validateResult = null;
        for (Annotation ann : paramInfo.getAnnotations()) {
            IValidator validator = owner.getBeanFactory().getBean(validators.get(ann.annotationType()));
            validateResult = validator.validate(new ValidateContext(owner, ann, paramInfo, paramValues, contextParams, resourceName));
            if (validateResult != null && validateResult.isMatched()) {
                break;
            }
        }
        return validateResult;
    }
}
