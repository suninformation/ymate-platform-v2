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
package net.ymate.platform.webmvc;

import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.webmvc.annotation.*;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * @author 刘镇 (suninformation@163.com) on 15/10/31 上午11:04
 */
public class ParameterMeta {

    /**
     * 请求参数映射名称
     */
    private String paramName;

    /**
     * 成员属性或方法参数名称
     */
    private final String fieldName;

    /**
     * 参数类型
     */
    private final Class<?> paramType;

    /**
     * 参数名称前缀
     */
    private String prefix;

    /**
     * 参数绑定的注解
     */
    private Annotation paramAnnotation;

    /**
     * 是否为数组
     */
    private final boolean array;

    /**
     * 拆分字符串数组的分隔符
     */
    private String splitArraySeparator;

    /**
     * 是否为上传文件类型
     */
    private final boolean uploadFile;

    /**
     * 是否为ModelBind模式
     */
    private boolean modelBind;

    private boolean paramField;

    public ParameterMeta(Class<?> paramType, String fieldName, Annotation[] fieldAnnotations, boolean snakeCase) {
        this.fieldName = fieldName;
        this.paramType = paramType;
        this.array = paramType.isArray();
        this.uploadFile = paramType.equals(IUploadFileWrapper.class);
        //
        for (Annotation annotation : fieldAnnotations) {
            this.paramField = parseAnnotation(annotation, snakeCase);
            if (this.paramField) {
                break;
            }
        }
        if (this.paramField) {
            splitArraySeparator = Arrays.stream(fieldAnnotations)
                    .filter(annotation -> annotation instanceof SplitArrayWith)
                    .findFirst().map(annotation -> StringUtils.trimToNull(((SplitArrayWith) annotation).separator()))
                    .orElse(this.splitArraySeparator);
        }
    }

    public ParameterMeta(Field paramField, boolean snakeCase) {
        this(paramField.getType(), paramField.getName(), paramField.getAnnotations(), snakeCase);
    }

    private boolean parseAnnotation(Annotation annotation, boolean snakeCase) {
        boolean flag = false;
        if (annotation != null) {
            if (annotation instanceof CookieVariable) {
                CookieVariable ann = (CookieVariable) annotation;
                this.paramAnnotation = ann;
                this.paramName = doBuildParamName(StringUtils.defaultIfBlank(ann.prefix(), prefix), ann.value(), fieldName, false);
                flag = true;
            } else if (annotation instanceof PathVariable) {
                PathVariable ann = (PathVariable) annotation;
                this.paramAnnotation = ann;
                this.paramName = doBuildParamName(StringUtils.EMPTY, ann.value(), fieldName, false);
                flag = true;
            } else if (annotation instanceof RequestHeader) {
                RequestHeader ann = (RequestHeader) annotation;
                this.paramAnnotation = ann;
                this.paramName = doBuildParamName(StringUtils.defaultIfBlank(ann.prefix(), prefix), ann.value(), fieldName, false);
                flag = true;
            } else if (annotation instanceof RequestParam) {
                RequestParam ann = (RequestParam) annotation;
                this.paramAnnotation = ann;
                this.paramName = doBuildParamName(StringUtils.defaultIfBlank(ann.prefix(), prefix), ann.value(), fieldName, snakeCase);
                flag = true;
            } else if (annotation instanceof ModelBind) {
                ModelBind ann = (ModelBind) annotation;
                this.paramAnnotation = annotation;
                this.prefix = ann.prefix();
                this.modelBind = true;
                flag = true;
            }
        }
        return flag;
    }

    /**
     * @param prefix      前缀
     * @param paramName   参数名称
     * @param defaultName 默认名称
     * @param snakeCase   是否使用蛇形命名
     * @return 根据前缀生成有效的参数名称
     */
    public String doBuildParamName(String prefix, String paramName, String defaultName, boolean snakeCase) {
        String name = StringUtils.defaultIfBlank(paramName, defaultName);
        if (snakeCase) {
            name = ClassUtils.fieldNameToPropertyName(name, 0);
        }
        if (StringUtils.isNotBlank(prefix)) {
            name = prefix.trim().concat(".").concat(name);
        }
        return name;
    }

    public String getParamName() {
        return paramName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Class<?> getParamType() {
        return paramType;
    }

    public String getPrefix() {
        return prefix;
    }

    public Annotation getParamAnnotation() {
        return paramAnnotation;
    }

    public boolean isArray() {
        return array;
    }

    public String getSplitArraySeparator() {
        return splitArraySeparator;
    }

    public boolean isUploadFile() {
        return uploadFile;
    }

    public boolean isModelBind() {
        return modelBind;
    }

    public boolean isParamField() {
        return this.paramField;
    }
}
