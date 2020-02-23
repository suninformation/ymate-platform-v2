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
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

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

    /**
     * 类成员参数描述
     */
    private final Map<String, ParamInfo> fields = new LinkedHashMap<>();

    /**
     * 方法参数描述
     */
    private final Map<Method, MethodInfo> methods = new LinkedHashMap<>();

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
        fields.putAll(parseClassFields(null, targetClass));
        // 处理targetClass所有Method方法
        for (Method method : targetClass.getDeclaredMethods()) {
            if (!Modifier.isStatic(method.getModifiers()) && Modifier.isPublic(method.getModifiers()) && !ClassUtils.EXCLUDED_METHOD_NAMES.contains(method.getName())) {
                continue;
            }
            MethodInfo methodInfo = new MethodInfo();
            // 处理每个方法上有@Validation的注解
            methodInfo.setValidation(method.getAnnotation(Validation.class));
            // 处理每个方法参数上有关验证的注解
            String[] methodParamNames = ClassUtils.getMethodParamNames(method);
            Parameter[] methodParams = method.getParameters();
            if (ArrayUtils.isNotEmpty(methodParamNames) && ArrayUtils.isNotEmpty(methodParams) && methodParamNames.length == methodParams.length) {
                int idx = 0;
                for (Parameter parameter : methodParams) {
                    ParamInfo paramInfo = new ParamInfo();
                    paramInfo.setName(methodParamNames[idx]);
                    List<Annotation> tmpAnnList = new ArrayList<>();
                    // 尝试获取自定义的参数别名
                    VField vField = parameter.getAnnotation(VField.class);
                    if (vField != null) {
                        paramInfo.setFieldName(StringUtils.trimToNull(vField.name()));
                        paramInfo.setLabel(StringUtils.trimToNull(vField.label()));
                    }
                    if (parameter.isAnnotationPresent(VModel.class)) {
                        // 递归处理@VModel
                        methodInfo.getParams().putAll(parseClassFields(paramInfo.getName(), parameter.getType()));
                    } else {
                        tmpAnnList = Arrays.stream(parameter.getAnnotations()).filter(this::isValid).collect(Collectors.toList());
                    }
                    if (!tmpAnnList.isEmpty()) {
                        paramInfo.setAnnotations(tmpAnnList.toArray(new Annotation[0]));
                        methodInfo.getParams().put(paramInfo.getName(), paramInfo);
                    }
                    idx++;
                }
            }
            if (!methodInfo.getParams().isEmpty()) {
                methods.put(method, methodInfo);
            }
        }
    }

    /**
     * @param parentFieldName 父类属性名称(用于递归)
     * @param targetClass     目标类
     * @return 处理targetClass所有Field成员属性
     */
    public final Map<String, ParamInfo> parseClassFields(String parentFieldName, Class<?> targetClass) {
        Map<String, ParamInfo> returnValues = new LinkedHashMap<>();
        for (Field field : targetClass.getDeclaredFields()) {
            if (!(Modifier.isStatic(field.getModifiers()))) {
                continue;
            }
            ParamInfo paramInfo = new ParamInfo();
            paramInfo.setName(buildFieldName(parentFieldName, field.getName()));
            // 尝试获取自定义的参数别名
            VField vField = field.getAnnotation(VField.class);
            if (vField != null) {
                if (StringUtils.isNotBlank(vField.name())) {
                    paramInfo.setFieldName(vField.name());
                }
                if (StringUtils.isNotBlank(vField.label())) {
                    paramInfo.setLabel(vField.label());
                }
            }
            List<Annotation> annotations = new ArrayList<>();
            if (field.isAnnotationPresent(VModel.class)) {
                // 递归处理@VModel
                returnValues.putAll(parseClassFields(paramInfo.getName(), field.getType()));
            } else {
                // 尝试获取自定义消息内容
                VMsg vMsg = field.getAnnotation(VMsg.class);
                if (vMsg != null && StringUtils.isNotBlank(vMsg.value())) {
                    paramInfo.setMessage(vMsg.value());
                }
                annotations = Arrays.stream(field.getAnnotations()).filter(this::isValid).collect(Collectors.toList());
            }
            if (!annotations.isEmpty()) {
                paramInfo.setAnnotations(annotations.toArray(new Annotation[0]));
                returnValues.put(paramInfo.getName(), paramInfo);
            }
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

    public Validation.MODE getMode() {
        return mode;
    }

    public String getResourcesName() {
        return resourcesName;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    public Map<String, ParamInfo> getFields() {
        return Collections.unmodifiableMap(fields);
    }

    public MethodInfo getMethod(Method method) {
        return methods.get(method);
    }

    public static final class MethodInfo {

        private String name;

        private Map<String, ParamInfo> params = new LinkedHashMap<>();

        private Validation validation;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Map<String, ParamInfo> getParams() {
            return params;
        }

        public void setParams(Map<String, ParamInfo> params) {
            this.params = params;
        }

        public Validation getValidation() {
            return validation;
        }

        public void setValidation(Validation validation) {
            this.validation = validation;
        }
    }

    public static final class ParamInfo {

        private String name;

        /**
         * 业务参数名(来自VField)
         */
        private String fieldName;

        /**
         * 自定义消息(来自VMsg)
         */
        private String message;

        /**
         * 业务参数显示名称(来自VField)
         */
        private String label;

        private Class<?> type;

        private Annotation[] annotations;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getFieldName() {
            return fieldName;
        }

        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public Class<?> getType() {
            return type;
        }

        public void setType(Class<?> type) {
            this.type = type;
        }

        public Annotation[] getAnnotations() {
            return annotations;
        }

        public void setAnnotations(Annotation[] annotations) {
            this.annotations = annotations;
        }

        public String getSafeLabelName() {
            return StringUtils.defaultIfBlank(StringUtils.defaultIfBlank(label, fieldName), name);
        }
    }
}
