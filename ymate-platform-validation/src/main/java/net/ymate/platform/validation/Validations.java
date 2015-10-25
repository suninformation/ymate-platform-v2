/*
 * Copyright 2007-2016 the original author or authors.
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

import net.ymate.platform.core.Version;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.module.IModule;
import net.ymate.platform.core.module.annotation.Module;
import net.ymate.platform.core.util.RuntimeUtils;
import net.ymate.platform.validation.annotation.Validation;
import net.ymate.platform.validation.annotation.Validator;
import net.ymate.platform.validation.handle.ValidateHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 验证框架模块管理器
 *
 * @author 刘镇 (suninformation@163.com) on 2013-4-7 下午4:43:48
 * @version 1.0
 */
@Module
public class Validations implements IModule, IValidation {

    private static final Log _LOG = LogFactory.getLog(Validations.class);

    public static final Version VERSION = new Version(2, 0, 0, Validations.class.getPackage().getImplementationVersion(), Version.VersionType.Alphal);

    private static IValidation __instance;

    private YMP __owner;

    private boolean __inited;

    private Map<Class<? extends Annotation>, Class<? extends IValidator>> __validators;

    private Map<Class<?>, ValidationMeta> __VALIDATION_META_CACHES;

    /**
     * @return 返回默认验证框架管理器实例对象
     */
    public static IValidation get() {
        if (__instance == null) {
            synchronized (VERSION) {
                if (__instance == null) {
                    __instance = YMP.get().getModule(Validations.class);
                }
            }
        }
        return __instance;
    }

    /**
     * @param owner YMP框架管理器实例
     * @return 返回指定YMP框架管理器容器内的验证框架管理器实例
     */
    public static IValidation get(YMP owner) {
        return owner.getModule(Validations.class);
    }

    public void init(YMP owner) throws Exception {
        if (!__inited) {
            //
            _LOG.info("Initializing ymate-platform-validation-" + VERSION);
            //
            __owner = owner;
            __validators = new HashMap<Class<? extends Annotation>, Class<? extends IValidator>>();
            __VALIDATION_META_CACHES = new HashMap<Class<?>, ValidationMeta>();
            __owner.registerHandler(Validator.class, new ValidateHandler(__owner));
            //
            __inited = true;
        }
    }

    public boolean isInited() {
        return __inited;
    }

    public YMP getOwner() {
        return __owner;
    }

    public void registerValidator(Class<? extends Annotation> annotationClass, Class<? extends IValidator> validatorClass) {
        try {
            __owner.registerBean(validatorClass, validatorClass.newInstance());
            __validators.put(annotationClass, validatorClass);
        } catch (Exception e) {
            _LOG.error("", RuntimeUtils.unwrapThrow(e));
        }
    }

    public boolean containsValidator(Class<? extends Annotation> annotationClass) {
        return __validators.containsKey(annotationClass);
    }

    public Map<String, ValidateResult> validate(Class<?> targetClass, Map<String, Object> paramValues) {
        Map<String, ValidateResult> _returnValues = new LinkedHashMap<String, ValidateResult>();
        ValidationMeta _meta = __doGetCachedMeta(targetClass);
        for (String _fieldName : _meta.getFieldNames()) {
            ValidateResult _result = __doValidate(_meta.getFieldAnnotations(_fieldName), _fieldName, _meta.getFieldLabel(_fieldName), paramValues);
            if (_result != null) {
                _returnValues.put(_fieldName, _result);
                if (_meta.getMode() == Validation.MODE.NORMAL) {
                    break;
                }
            }
        }
        return _returnValues;
    }

    public Map<String, ValidateResult> validate(Class<?> targetClass, Method targetMethod, Map<String, Object> paramValues) {
        Map<String, ValidateResult> _returnValues = new LinkedHashMap<String, ValidateResult>();
        ValidationMeta _meta = __doGetCachedMeta(targetClass);
        Validation _validation = _meta.getMethodValidation(targetMethod);
        Validation.MODE _mode = _validation == null ? _meta.getMode() : _validation.mode();
        //
        Map<String, Annotation[]> _paramAnnoMap = _meta.getMethodParamAnnotations(targetMethod);
        for (String _paramName : _paramAnnoMap.keySet()) {
            ValidateResult _result = __doValidate(_paramAnnoMap.get(_paramName), _paramName, _meta.getFieldLabel(_paramName), paramValues);
            if (_result != null) {
                _returnValues.put(_paramName, _result);
                //
                if (_mode == Validation.MODE.NORMAL) {
                    break;
                }
            }
        }
        return _returnValues;
    }

    /**
     * @param targetClass 目标类型
     * @return 缓存中获取目标类型验证配置描述，若不存在则尝试创建它并加入缓存中
     */
    protected ValidationMeta __doGetCachedMeta(Class<?> targetClass) {
        ValidationMeta _meta = __VALIDATION_META_CACHES.get(targetClass);
        if (_meta == null) {
            _meta = new ValidationMeta(this, targetClass);
            __VALIDATION_META_CACHES.put(targetClass, _meta);
        }
        return _meta;
    }

    protected ValidateResult __doValidate(Annotation[] annotations, String paramName, String paramLabel, Map<String, Object> paramValues) {
        ValidateResult _result = null;
        for (Annotation _ann : annotations) {
            IValidator _validator = __owner.getBean(__validators.get(_ann.annotationType()));
            _result = _validator.validate(new ValidateContext(__owner, _ann, paramName, paramLabel, paramValues));
            if (_result != null) {
                break;
            }
        }
        return _result;
    }

    public void destroy() throws Exception {
        if (__inited) {
            __inited = false;
            //
            __VALIDATION_META_CACHES = null;
            __validators = null;
            __owner = null;
        }
    }
}
