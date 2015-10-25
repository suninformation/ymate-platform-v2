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

import net.ymate.platform.core.YMP;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * 验证器上下文环境
 *
 * @author 刘镇 (suninformation@163.com) on 2013-4-13 上午11:28:22
 * @version 1.0
 */
public class ValidateContext {

    private YMP __owner;

    private Annotation annotation;

    private String paramName;

    private String paramLabel;

    private Map<String, Object> paramValues;

    public ValidateContext(YMP owner, Annotation annotation, String paramName, String paramLabel, Map<String, Object> paramValues) {
        this.__owner = owner;
        //
        this.annotation = annotation;
        this.paramName = paramName;
        this.paramLabel = paramLabel;
        this.paramValues = paramValues;
    }

    public YMP getOwner() {
        return __owner;
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    public String getParamName() {
        return paramName;
    }

    public String getParamLabel() {
        return paramLabel;
    }

    public Object getParamValue() {
        return paramValues.get(this.paramName);
    }

    public Map<String, Object> getParamValues() {
        return paramValues;
    }
}
