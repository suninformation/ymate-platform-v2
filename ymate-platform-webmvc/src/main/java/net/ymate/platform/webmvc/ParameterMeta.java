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
import net.ymate.platform.validation.ValidationMeta;
import net.ymate.platform.validation.annotation.VField;
import net.ymate.platform.validation.annotation.VModel;
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
     * @param prefix      前缀
     * @param paramName   参数名称
     * @param defaultName 默认名称
     * @param snakeCase   是否使用蛇形命名
     * @return 根据前缀生成有效的参数名称
     */
    public static String buildParamName(String prefix, String paramName, String defaultName, boolean snakeCase) {
        String name = StringUtils.defaultIfBlank(paramName, defaultName);
        if (snakeCase) {
            name = ValidationMeta.parsePrefixValue(prefix, ClassUtils.fieldNameToPropertyName(name, 0));
        } else {
            name = ValidationMeta.parsePrefixValue(prefix, name);
        }
        return name;
    }

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

    /**
     * 是否保留原始参数名称格式
     *
     * @since 2.1.3
     */
    private boolean keepParamName;

    public ParameterMeta(Class<?> paramType, String fieldName, Annotation[] fieldAnnotations) {
        this.fieldName = fieldName;
        this.paramType = paramType;
        this.array = paramType.isArray();
        this.uploadFile = paramType.equals(IUploadFileWrapper.class);
        //
        for (Annotation annotation : fieldAnnotations) {
            this.paramField = parseAnnotation(annotation);
            if (this.paramField) {
                break;
            }
        }
        if (this.paramField) {
            splitArraySeparator = Arrays.stream(fieldAnnotations)
                    .filter(annotation -> annotation instanceof SplitArrayWith)
                    .findFirst().map(annotation -> StringUtils.trimToNull(((SplitArrayWith) annotation).separator()))
                    .orElse(this.splitArraySeparator);
            if (modelBind) {
                prefix = Arrays.stream(fieldAnnotations)
                        .filter(ann -> ann instanceof VModel)
                        .findFirst()
                        .map(ann -> StringUtils.defaultIfBlank(((VModel) ann).prefix(), prefix))
                        .orElse(this.prefix);
            } else {
                for (Annotation fieldAnn : fieldAnnotations) {
                    if (fieldAnn instanceof VField) {
                        VField vField = ((VField) fieldAnn);
                        this.prefix = StringUtils.defaultIfBlank(vField.prefix(), this.prefix);
                        this.paramName = StringUtils.defaultIfBlank(vField.value(), this.paramName);
                        break;
                    }
                }
            }
            this.paramName = StringUtils.defaultIfBlank(paramName, fieldName);
            if (StringUtils.isNotBlank(prefix)) {
                if (keepParamName) {
                    this.paramName = StringUtils.join(prefix, paramName);
                } else if (StringUtils.endsWithAny(prefix, ".", "_")) {
                    this.paramName = StringUtils.join(prefix, paramName);
                } else {
                    this.paramName = StringUtils.join(prefix, StringUtils.capitalize(paramName));
                }
            }
        }
    }

    public ParameterMeta(Field paramField) {
        this(paramField.getType(), paramField.getName(), paramField.getAnnotations());
    }

    private boolean parseAnnotation(Annotation annotation) {
        boolean flag = false;
        if (annotation != null) {
            if (annotation instanceof CookieVariable) {
                CookieVariable ann = (CookieVariable) annotation;
                this.paramAnnotation = ann;
                this.prefix = ann.prefix();
                this.paramName = ann.value();
                this.keepParamName = true;
                flag = true;
            } else if (annotation instanceof PathVariable) {
                PathVariable ann = (PathVariable) annotation;
                this.paramAnnotation = ann;
                this.paramName = ann.value();
                this.keepParamName = true;
                flag = true;
            } else if (annotation instanceof RequestHeader) {
                RequestHeader ann = (RequestHeader) annotation;
                this.paramAnnotation = ann;
                this.prefix = ann.prefix();
                this.paramName = ann.value();
                this.keepParamName = true;
                flag = true;
            } else if (annotation instanceof RequestParam) {
                RequestParam ann = (RequestParam) annotation;
                this.paramAnnotation = ann;
                this.prefix = ann.prefix();
                this.paramName = ann.value();
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

    public boolean isKeepParamName() {
        return keepParamName;
    }
}
