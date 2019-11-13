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
package net.ymate.platform.core.support;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * ret = 0: 正确返回<br>
 * ret &gt; 0: 调用时发生错误，需要开发者进行相应的处理<br>
 * -50 &lt;= ret &lt;= -1: 方法调用不能通过校验，需要开发者进行相应的处理<br>
 * ret &lt;-50: 系统内部错误
 *
 * @author 刘镇 (suninformation@163.com) on 14/7/6 下午6:53
 * @since 2.0.6
 * @since 2.1.0 调整其为框架基础类
 */
public class ErrorCode implements Serializable {

    /**
     * 请求成功
     */
    public final static int SUCCEED = 0;

    /**
     * 数据版本不匹配
     */
    public final static int DATA_VERSION_NOT_MATCH = -20;

    /**
     * 系统内部错误
     */
    public final static int INTERNAL_SYSTEM_ERROR = -50;

    public final static String MSG_DATA_VERSION_NOT_MATCH = "The data version does not match.";

    public final static String MSG_INTERNAL_SYSTEM_ERROR = "The system is busy, try again later!";

    public static ErrorCode succeed() {
        return new ErrorCode(SUCCEED);
    }

    public static ErrorCode dataVersionNotMatch() {
        return ErrorCode.create(DATA_VERSION_NOT_MATCH, MSG_DATA_VERSION_NOT_MATCH);
    }

    public static ErrorCode internalSystemError() {
        return ErrorCode.create(INTERNAL_SYSTEM_ERROR, MSG_INTERNAL_SYSTEM_ERROR);
    }

    public static ErrorCode create(int code) {
        return new ErrorCode(code);
    }

    public static ErrorCode create(int code, String message) {
        return new ErrorCode(code, message);
    }

    public static ErrorCode create(int code, String i18nKey, String message) {
        return new ErrorCode(code, i18nKey, message);
    }

    private int code;

    private String i18nKey;

    private String message;

    private final Map<String, Object> attributes = new HashMap<>();

    private final Map<String, Object> data = new HashMap<>();

    public ErrorCode(int code) {
        this.code = code;
    }

    public ErrorCode(int code, String message) {
        this(code, null, message);
    }

    public ErrorCode(int code, String i18nKey, String message) {
        this.code = code;
        this.i18nKey = i18nKey;
        this.message = message;
    }

    public boolean isSucceed() {
        return SUCCEED == code;
    }

    public int code() {
        return code;
    }

    public void code(int code) {
        this.code = code;
    }

    public String i18nKey() {
        return i18nKey;
    }

    public ErrorCode i18nKey(String i18nKey) {
        this.i18nKey = i18nKey;
        return this;
    }

    public String message() {
        return message;
    }

    public ErrorCode message(String message) {
        this.message = message;
        return this;
    }

    public Map<String, Object> attrs() {
        return attributes;
    }

    @SuppressWarnings("unchecked")
    public <T> T attr(String attrKey) {
        return (T) attributes.get(attrKey);
    }

    public ErrorCode attr(String attrKey, Object attrValue) {
        this.attributes.put(attrKey, attrValue);
        return this;
    }

    public ErrorCode attrs(Map<String, Object> attributes) {
        this.attributes.putAll(attributes);
        return this;
    }

    public Map<String, Object> data() {
        return data;
    }

    @SuppressWarnings("unchecked")
    public <T> T dataAttr(String dataKey) {
        return (T) data.get(dataKey);
    }

    public ErrorCode dataAttr(String dataKey, Object dataValue) {
        data.put(dataKey, dataValue);
        return this;
    }

    public ErrorCode dataAttrs(Map<String, Object> dataAttributes) {
        data.putAll(dataAttributes);
        return this;
    }
}
