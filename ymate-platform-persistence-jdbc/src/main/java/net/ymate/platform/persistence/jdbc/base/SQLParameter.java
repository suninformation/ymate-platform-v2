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
package net.ymate.platform.persistence.jdbc.base;

import net.ymate.platform.core.persistence.base.Type;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * SQL参数对象
 *
 * @author 刘镇 (suninformation@163.com) on 2011-8-28 上午01:51:52
 */
public final class SQLParameter {

    public static void addParameter(List<SQLParameter> parameters, Object parameter) {
        if (parameter == null) {
            parameters.add(new SQLParameter(Type.FIELD.UNKNOWN, null));
        } else if (parameter instanceof SQLParameter) {
            parameters.add((SQLParameter) parameter);
        } else {
            parameters.add(new SQLParameter(parameter));
        }
    }

    private Type.FIELD type;

    private Object value;

    public SQLParameter(Object value) {
        this(Type.FIELD.UNKNOWN, value);
    }

    public SQLParameter(Type.FIELD type, Object value) {
        this.type = type;
        this.value = value;
    }

    public Type.FIELD getType() {
        return this.type;
    }

    public Object getValue() {
        return this.value;
    }

    private String tryBase64Str() {
        return "BASE64@" + Base64.encodeBase64String(((String) value).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String toString() {
        if (value instanceof String) {
            if (StringUtils.containsAny((CharSequence) value, '\r', '\n')) {
                return "\"".concat(tryBase64Str()).concat("\"");
            } else {
                return "\"".concat(value.toString()).concat("\"");
            }
        }
        return value != null ? value.toString() : "@NULL";
    }
}
