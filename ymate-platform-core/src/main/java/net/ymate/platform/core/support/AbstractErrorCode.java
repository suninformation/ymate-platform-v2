/*
 * Copyright 2007-2022 the original author or authors.
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
package net.ymate.platform.core.support;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2022/7/6 16:25
 * @since 2.1.2
 */
public abstract class AbstractErrorCode<CODE_TYPE extends Serializable, ERROR_CODE_TYPE extends AbstractErrorCode<CODE_TYPE, ERROR_CODE_TYPE>> implements Serializable {

    private CODE_TYPE code;

    private String i18nKey;

    private String message;

    private final Map<String, Object> attributes = new LinkedHashMap<>();

    private final Map<String, Object> data = new LinkedHashMap<>();

    protected AbstractErrorCode(CODE_TYPE code) {
        this.code = code;
    }

    protected AbstractErrorCode(CODE_TYPE code, String message) {
        this(code, null, message);
    }

    protected AbstractErrorCode(CODE_TYPE code, String i18nKey, String message) {
        this.code = code;
        this.i18nKey = i18nKey;
        this.message = message;
    }

    public abstract boolean isSucceed();

    public CODE_TYPE code() {
        return code;
    }

    public void code(CODE_TYPE code) {
        this.code = code;
    }

    public String i18nKey() {
        return i18nKey;
    }

    @SuppressWarnings("unchecked")
    public ERROR_CODE_TYPE i18nKey(String i18nKey) {
        this.i18nKey = i18nKey;
        return (ERROR_CODE_TYPE) this;
    }

    public String message() {
        return message;
    }

    @SuppressWarnings("unchecked")
    public ERROR_CODE_TYPE message(String message) {
        this.message = message;
        return (ERROR_CODE_TYPE) this;
    }

    public Map<String, Object> attrs() {
        return attributes;
    }

    @SuppressWarnings("unchecked")
    public <T> T attr(String attrKey) {
        return (T) attributes.get(attrKey);
    }

    @SuppressWarnings("unchecked")
    public ERROR_CODE_TYPE attr(String attrKey, Object attrValue) {
        this.attributes.put(attrKey, attrValue);
        return (ERROR_CODE_TYPE) this;
    }

    @SuppressWarnings("unchecked")
    public ERROR_CODE_TYPE attrs(Map<String, Object> attributes) {
        this.attributes.putAll(attributes);
        return (ERROR_CODE_TYPE) this;
    }

    public Map<String, Object> data() {
        return data;
    }

    @SuppressWarnings("unchecked")
    public <T> T dataAttr(String dataKey) {
        return (T) data.get(dataKey);
    }

    @SuppressWarnings("unchecked")
    public ERROR_CODE_TYPE dataAttr(String dataKey, Object dataValue) {
        data.put(dataKey, dataValue);
        return (ERROR_CODE_TYPE) this;
    }

    @SuppressWarnings("unchecked")
    public ERROR_CODE_TYPE dataAttrs(Map<String, Object> dataAttributes) {
        data.putAll(dataAttributes);
        return (ERROR_CODE_TYPE) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ERROR_CODE_TYPE errorCode = (ERROR_CODE_TYPE) o;
        return new EqualsBuilder().append(code, errorCode.code()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(code).toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("code", code)
                .append("i18nKey", i18nKey)
                .append("message", message)
                .append("attributes", attributes)
                .append("data", data)
                .toString();
    }
}
