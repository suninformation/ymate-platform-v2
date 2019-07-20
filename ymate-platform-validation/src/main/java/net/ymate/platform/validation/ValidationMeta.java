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

import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.validation.annotation.VField;
import net.ymate.platform.validation.annotation.VModel;
import net.ymate.platform.validation.annotation.VMsg;
import net.ymate.platform.validation.annotation.Validation;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * 目标类型验证配置描述
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/23 上午1:55
 */
public final class ValidationMeta implements Serializable {

    private final IValidation validation;

    private final Validation.MODE mode;

    private String resourcesName;

    private final Class<?> targetClass;

    private final Map<String, Annotation[]> fields = new LinkedHashMap<>();

    private final Map<String, String> labels = new LinkedHashMap<>();

    private final Map<Method, Map<String, String>> methodLabels = new LinkedHashMap<>();

    private final Map<String, String> messages = new LinkedHashMap<>();

    private final Map<Method, Map<String, String>> methodMessages = new LinkedHashMap<>();

    private final Map<Method, Validation> methods = new LinkedHashMap<>();

    private final Map<Method, Map<String, Annotation[]>> methodParams = new LinkedHashMap<>();

    public ValidationMeta(IValidation validation, Class<?> targetClass) {
        this.validation = validation;
        // 处理targetClass声明的@Validation注解，提取默认验证模式
        Validation classAnnotation = targetClass.getAnnotation(Validation.class);
        if (classAnnotation != null) {
            mode = classAnnotation.mode();
            resourcesName = classAnnotation.resourcesName();
        } else {
            mode = Validation.MODE.NORMAL;
        }
        this.targetClass = targetClass;
        // 处理targetClass所有Field成员属性
        fields.putAll(parseClassFields(null, targetClass, labels, messages));
        // 处理targetClass所有Method方法
        for (Method method : targetClass.getDeclaredMethods()) {
            Map<String, String> paramLabels = new LinkedHashMap<>();
            methodLabels.put(method, paramLabels);
            Map<String, String> paramMessages = new LinkedHashMap<>();
            methodMessages.put(method, paramMessages);
            // 处理每个方法上有@Validation的注解
            Validation methodValidation = method.getAnnotation(Validation.class);
            if (methodValidation != null) {
                methods.put(method, methodValidation);
            }
            // 处理每个方法参数上有关验证的注解
            Map<String, Annotation[]> paramAnnotations = new LinkedHashMap<>();
            for (Parameter parameter : method.getParameters()) {
                List<Annotation> tmpAnnList = new ArrayList<>();
                String paramName = parameter.getName();
                // 尝试获取自定义的参数别名
                VField vField = parameter.getAnnotation(VField.class);
                if (vField != null) {
                    if (StringUtils.isNotBlank(vField.name())) {
                        paramName = vField.name();
                    }
                    if (StringUtils.isNotBlank(vField.label())) {
                        paramLabels.put(paramName, vField.label());
                    }
                }
                if (parameter.isAnnotationPresent(VModel.class)) {
                    // 递归处理@VModel
                    paramAnnotations.putAll(parseClassFields(paramName, parameter.getType(), paramLabels, paramMessages));
                } else {
                    for (Annotation annotation : parameter.getAnnotations()) {
                        parseAnnotation(annotation, tmpAnnList);
                    }
                }
                if (!tmpAnnList.isEmpty()) {
                    paramAnnotations.put(paramName, tmpAnnList.toArray(new Annotation[0]));
                }
            }
            if (!paramAnnotations.isEmpty()) {
                methodParams.put(method, paramAnnotations);
            }
        }
    }

    /**
     * @param parentFieldName 父类属性名称(用于递归)
     * @param targetClass     目标类
     * @param paramLabels     自定义参数标签名称映射
     * @param paramMessages   自定义验证消息映射
     * @return 处理targetClass所有Field成员属性
     */
    public final Map<String, Annotation[]> parseClassFields(String parentFieldName, Class<?> targetClass, Map<String, String> paramLabels, Map<String, String> paramMessages) {
        Map<String, Annotation[]> returnValues = new LinkedHashMap<>();
        ClassUtils.BeanWrapper<?> wrapper = ClassUtils.wrapper(targetClass);
        if (wrapper != null) {
            wrapper.getFields().forEach((field) -> {
                String fieldName = field.getName();
                // 尝试获取自定义的参数别名
                VField vField = field.getAnnotation(VField.class);
                if (vField != null) {
                    if (StringUtils.isNotBlank(vField.name())) {
                        fieldName = vField.name();
                    }
                    if (StringUtils.isNotBlank(vField.label())) {
                        labels.put(fieldName, vField.label());
                        paramLabels.put(buildFieldName(parentFieldName, fieldName), vField.label());
                    }
                }
                List<Annotation> annotations = new ArrayList<>();
                if (field.isAnnotationPresent(VModel.class)) {
                    // 拼装带层级关系的Field名称
                    String fieldNamePr = buildFieldName(parentFieldName, fieldName);
                    if (vField != null && StringUtils.isNotBlank(vField.label())) {
                        paramLabels.put(fieldNamePr, vField.label());
                    }
                    // 递归处理@VModel
                    returnValues.putAll(parseClassFields(fieldNamePr, field.getType(), paramLabels, paramMessages));
                } else {
                    // 尝试获取自定义消息内容
                    VMsg vMsg = field.getAnnotation(VMsg.class);
                    if (vMsg != null && StringUtils.isNotBlank(vMsg.value())) {
                        messages.put(fieldName, vMsg.value());
                        paramMessages.put(buildFieldName(parentFieldName, fieldName), vMsg.value());
                    }
                    for (Annotation annotation : field.getAnnotations()) {
                        parseAnnotation(annotation, annotations);
                    }
                }
                if (!annotations.isEmpty()) {
                    // 拼装带层级关系的Field名称
                    String fieldNamePr = buildFieldName(parentFieldName, fieldName);
                    returnValues.put(fieldNamePr, annotations.toArray(new Annotation[0]));
                }
            });
        }
        return returnValues;
    }

    /**
     * @param parentFieldName 父类属性名称
     * @param fieldName       属性名称
     * @return 返回带层级关系的Field名称
     */
    private String buildFieldName(String parentFieldName, String fieldName) {
        if (StringUtils.isNotBlank(parentFieldName)) {
            return parentFieldName.concat(".").concat(fieldName);
        }
        return fieldName;
    }

    private boolean isValid(Annotation annotation) {
        // 判断是否包含验证器中声明的注解
        return validation.containsValidator(annotation.annotationType());
    }

    private void parseAnnotation(Annotation annotation, List<Annotation> annotationList) {
        if (annotation.getClass().equals(VModel.class) || annotation.getClass().equals(VField.class) || annotation.getClass().equals(VMsg.class)) {
            return;
        }
        if (isValid(annotation)) {
            annotationList.add(annotation);
        }
    }

    public Validation.MODE getMode() {
        return mode;
    }

    public String getResourcesName() {
        return resourcesName;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    public Set<String> getFieldNames() {
        return Collections.unmodifiableSet(fields.keySet());
    }

    public String getFieldLabel(String fieldName) {
        return labels.get(fieldName);
    }

    public String getFieldLabel(Method method, String fieldName) {
        return methodLabels.get(method).get(fieldName);
    }

    public String getFieldMessage(String fieldName) {
        return messages.get(fieldName);
    }

    public String getFieldMessage(Method method, String fieldName) {
        return methodMessages.get(method).get(fieldName);
    }

    public Annotation[] getFieldAnnotations(String fieldName) {
        return fields.get(fieldName);
    }

    public Validation getMethodValidation(Method method) {
        return methods.get(method);
    }

    public Map<String, Annotation[]> getMethodParamAnnotations(Method method) {
        if (methodParams.containsKey(method)) {
            return Collections.unmodifiableMap(methodParams.get(method));
        }
        return Collections.emptyMap();
    }
}
