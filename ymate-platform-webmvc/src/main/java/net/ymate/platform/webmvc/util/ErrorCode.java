/*
 * Copyright 2007-2018 the original author or authors.
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
package net.ymate.platform.webmvc.util;

import net.ymate.platform.core.lang.BlurObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * ret = 0: 正确返回<br>
 * ret &gt; 0: 调用OpenAPI时发生错误，需要开发者进行相应的处理<br>
 * -50 &lt;= ret &lt;= -1: 接口调用不能通过接口代理机校验，需要开发者进行相应的处理<br>
 * ret &lt;-50: 系统内部错误
 *
 * @author 刘镇 (suninformation@163.com) on 14/7/6 下午6:53
 * @version 1.0
 * @since 2.0.6
 */
public class ErrorCode implements Serializable {

    /**
     * 请求成功
     */
    public final static int SUCCEED = 0;

    /**
     * 参数验证无效
     */
    public final static int INVALID_PARAMS_VALIDATION = -1;

    /**
     * 访问的资源未找到或不存在
     */
    public final static int RESOURCE_NOT_FOUND_OR_NOT_EXIST = -2;

    /**
     * 请求方法不支持或不正确
     */
    public final static int REQUEST_METHOD_NOT_ALLOWED = -3;

    /**
     * 请求的资源未授权或无权限
     */
    public final static int REQUEST_RESOURCE_UNAUTHORIZED = -4;

    /**
     * 用户会话无效或超时
     */
    public final static int USER_SESSION_INVALID_OR_TIMEOUT = -5;

    /**
     * 请求的操作被禁止
     */
    public final static int REQUEST_OPERATION_FORBIDDEN = -6;

    /**
     * 用户会话已授权(登录)
     */
    public static final int USER_SESSION_AUTHORIZED = -7;

    /**
     * 参数签名无效
     */
    public static final int INVALID_PARAMS_SIGNATURE = -8;

    /**
     * 上传文件大小超出限制
     */
    public final static int UPLOAD_FILE_SIZE_LIMIT_EXCEEDED = -9;

    /**
     * 上传文件总大小超出限制
     */
    public final static int UPLOAD_SIZE_LIMIT_EXCEEDED = -10;

    /**
     * 上传文件类型无效
     */
    public final static int UPLOAD_CONTENT_TYPE_INVALID = -11;

    /**
     * 数据版本不匹配
     */
    public final static int DATA_VERSION_NOT_MATCH = -20;

    /**
     * 系统内部错误
     */
    public final static int INTERNAL_SYSTEM_ERROR = -50;

    // ----------

    public final static String MSG_RESOURCE_NOT_FOUND_OR_NOT_EXIST = "The resources was not found or not existed.";

    public final static String MSG_INVALID_PARAMS_VALIDATION = "Request parameter validation is invalid.";

    public final static String MSG_REQUEST_METHOD_NOT_ALLOWED = "The request method is unsupported or incorrect.";

    public final static String MSG_REQUEST_RESOURCE_UNAUTHORIZED = "The requested resource is not authorized or privileged.";

    public final static String MSG_USER_SESSION_INVALID_OR_TIMEOUT = "User session invalid or timeout.";

    public final static String MSG_REQUEST_OPERATION_FORBIDDEN = "The requested operation is forbidden.";

    public final static String MSG_USER_SESSION_AUTHORIZED = "User session is authorized (logged in).";

    public final static String MSG_INVALID_PARAMS_SIGNATURE = "The parameter signature is invalid.";

    public final static String MSG_UPLOAD_FILE_SIZE_LIMIT_EXCEEDED = "The size of the uploaded file exceeds the limit.";

    public final static String MSG_UPLOAD_SIZE_LIMIT_EXCEEDED = "The total size of uploaded files exceeds the limit.";

    public final static String MSG_UPLOAD_CONTENT_TYPE_INVALID = "The upload file content type is invalid.";

    public final static String MSG_DATA_VERSION_NOT_MATCH = "The data version does not match.";

    public final static String MSG_INTERNAL_SYSTEM_ERROR = "The system is busy, try again later!";

    // ----------

    public static ErrorCode succeed() {
        return new ErrorCode(SUCCEED);
    }

    public static ErrorCode resourceNotFoundOrNotExist() {
        return ErrorCode.create(RESOURCE_NOT_FOUND_OR_NOT_EXIST, MSG_RESOURCE_NOT_FOUND_OR_NOT_EXIST);
    }

    public static ErrorCode invalidParamsValidation() {
        return ErrorCode.create(INVALID_PARAMS_VALIDATION, MSG_INVALID_PARAMS_VALIDATION);
    }

    public static ErrorCode requestMethodNotAllowed() {
        return ErrorCode.create(REQUEST_METHOD_NOT_ALLOWED, MSG_REQUEST_METHOD_NOT_ALLOWED);
    }

    public static ErrorCode requestResourceUnauthorized() {
        return ErrorCode.create(REQUEST_RESOURCE_UNAUTHORIZED, MSG_REQUEST_RESOURCE_UNAUTHORIZED);
    }

    public static ErrorCode userSessionInvalidOrTimeout() {
        return ErrorCode.create(USER_SESSION_INVALID_OR_TIMEOUT, MSG_USER_SESSION_INVALID_OR_TIMEOUT);
    }

    public static ErrorCode requestOperationForbidden() {
        return ErrorCode.create(REQUEST_OPERATION_FORBIDDEN, MSG_REQUEST_OPERATION_FORBIDDEN);
    }

    public static ErrorCode userSessionAuthorized() {
        return ErrorCode.create(USER_SESSION_AUTHORIZED, MSG_USER_SESSION_AUTHORIZED);
    }

    public static ErrorCode invalidParamsSignature() {
        return ErrorCode.create(INVALID_PARAMS_SIGNATURE, MSG_INVALID_PARAMS_SIGNATURE);
    }

    public static ErrorCode uploadFileSizeLimitExceeded() {
        return ErrorCode.create(UPLOAD_FILE_SIZE_LIMIT_EXCEEDED, MSG_UPLOAD_FILE_SIZE_LIMIT_EXCEEDED);
    }

    public static ErrorCode uploadSizeLimitExceeded() {
        return ErrorCode.create(UPLOAD_SIZE_LIMIT_EXCEEDED, MSG_UPLOAD_SIZE_LIMIT_EXCEEDED);
    }

    public static ErrorCode uploadContentTypeInvalid() {
        return ErrorCode.create(UPLOAD_CONTENT_TYPE_INVALID, MSG_UPLOAD_CONTENT_TYPE_INVALID);
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

    private Map<String, Object> attributes = new HashMap<String, Object>();

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

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getI18nKey() {
        return i18nKey;
    }

    public ErrorCode setI18nKey(String i18nKey) {
        this.i18nKey = i18nKey;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public ErrorCode setMessage(String message) {
        this.message = message;
        return this;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String attrKey) {
        return (T) attributes.get(attrKey);
    }

    public BlurObject getAttr(String attrKey) {
        return BlurObject.bind(attributes.get(attrKey));
    }

    public ErrorCode addAttribute(String attrKey, Object attrValue) {
        this.attributes.put(attrKey, attrValue);
        return this;
    }

    public ErrorCode addAttributes(Map<String, Object> attributes) {
        this.attributes.putAll(attributes);
        return this;
    }
}
