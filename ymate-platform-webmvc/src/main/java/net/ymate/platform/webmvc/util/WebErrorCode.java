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
package net.ymate.platform.webmvc.util;

import net.ymate.platform.core.support.ErrorCode;
import net.ymate.platform.webmvc.base.Type;
import org.apache.commons.lang3.StringUtils;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-11-13 22:46
 * @since 2.1.0
 */
public final class WebErrorCode {

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
     * 用户会话确认状态无效
     */
    public final static int USER_SESSION_CONFIRMATION_STATE = -12;

    /**
     * 用户会话被强制下线
     */
    public final static int USER_SESSION_FORCE_OFFLINE = -13;

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

    public final static String MSG_USER_SESSION_CONFIRMATION_STATE = "User session confirmation state invalid.";

    public final static String MSG_USER_SESSION_FORCE_OFFLINE = "User session has been forced offline.";

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

    public static ErrorCode userSessionConfirmationState() {
        return userSessionConfirmationState(null);
    }

    public static ErrorCode userSessionConfirmationState(String redirectUrl) {
        ErrorCode errorCode = ErrorCode.create(USER_SESSION_CONFIRMATION_STATE, MSG_USER_SESSION_CONFIRMATION_STATE);
        if (StringUtils.isNotBlank(redirectUrl)) {
            errorCode.attr(Type.Const.REDIRECT_URL, redirectUrl);
        }
        return errorCode;
    }

    public static ErrorCode userSessionForceOffline() {
        return userSessionForceOffline(null, null, null);
    }

    public static ErrorCode userSessionForceOffline(String remoteAddr, Long eventTime, String description) {
        return ErrorCode.create(USER_SESSION_FORCE_OFFLINE, MSG_USER_SESSION_FORCE_OFFLINE)
                .attr(Type.Const.REMOTE_ADDR, remoteAddr)
                .attr(Type.Const.EVENT_TIME, eventTime)
                .attr(Type.Const.DESCRIPTION, description);
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
}
