/*
 * Copyright 2007-2017 the original author or authors.
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

import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.support.AbstractContext;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 验证器上下文环境
 *
 * @author 刘镇 (suninformation@163.com) on 2013-4-13 上午11:28:22
 */
public class ValidateContext extends AbstractContext {

    private static final ThreadLocal<Map<String, Object>> ATTRIBUTES = ThreadLocal.withInitial(() -> new HashMap<>(16));

    public static Map<String, Object> getLocalAttributes() {
        return ATTRIBUTES.get();
    }

    private final String resourceName;

    private final Annotation annotation;

    private final ValidationMeta.ParamInfo paramInfo;

    private final Map<String, Object> paramValues;

    public ValidateContext(IApplication owner, Annotation annotation, ValidationMeta.ParamInfo paramInfo, Map<String, Object> paramValues, Map<String, String> contextParams, String resourceName) {
        super(owner, contextParams);
        //
        this.annotation = annotation;
        this.paramInfo = paramInfo;
        this.paramValues = paramValues;
        //
        this.resourceName = resourceName;
    }

    public String getResourceName() {
        return resourceName;
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    public ValidationMeta.ParamInfo getParamInfo() {
        return paramInfo;
    }

    public Object getParamValue() {
        return getParamValue(paramInfo.getName());
    }

    public Object getParamValue(String paramName) {
        Object targetValue = this.paramValues.get(paramName);
        if (targetValue == null) {
            // 修正对JavaBean对象验证时无法正确获取属性参数值的问题:
            // 先以'.'拆分参数名称并按层级关系尝试获取参数值
            String[] pNames = StringUtils.split(paramName, '.');
            if (pNames.length > 1) {
                try {
                    for (String pName : pNames) {
                        if (targetValue == null) {
                            targetValue = this.paramValues.get(pName);
                        } else {
                            targetValue = ClassUtils.wrapper(targetValue).getValue(pName);
                        }
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    // 出现任何异常都将返回null
                    targetValue = null;
                } finally {
                    // 上述过程无论取值是否为空都将被缓存, 防止多次执行
                    this.paramValues.put(paramInfo.getName(), targetValue);
                }
            }
        }
        return targetValue;
    }

    public Map<String, Object> getParamValues() {
        return Collections.unmodifiableMap(paramValues);
    }
}
