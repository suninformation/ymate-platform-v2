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
package net.ymate.platform.persistence.jdbc.base;

import net.ymate.platform.persistence.base.Type;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

import java.io.UnsupportedEncodingException;

/**
 * SQL参数对象
 *
 * @author 刘镇 (suninformation@163.com) on 2011-8-28 上午01:51:52
 * @version 1.0
 */
public class SQLParameter {

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

    private String __tryBase64Str(String str) {
        String _base64Str;
        try {
            _base64Str = "BASE64@" + Base64.encodeBase64String(((String) value).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            _base64Str = str;
        }
        return _base64Str;
    }

    @Override
    public String toString() {
        if (value instanceof String) {
            if (StringUtils.containsAny((String) value, new char[]{'\r', '\n'})) {
                return "\"".concat(__tryBase64Str((String) value)).concat("\"");
            } else {
                return "\"".concat(value.toString()).concat("\"");
            }
        }
        return value != null ? value.toString() : "@NULL";
    }
}
