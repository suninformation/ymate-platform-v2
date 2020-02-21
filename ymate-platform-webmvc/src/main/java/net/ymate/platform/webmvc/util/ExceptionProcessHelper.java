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

import net.ymate.platform.commons.ReentrantLockHelper;
import net.ymate.platform.core.support.ErrorCode;
import net.ymate.platform.webmvc.base.Type;
import net.ymate.platform.webmvc.exception.*;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.lang.NullArgumentException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 刘镇 (suninformation@163.com) on 2017/10/18 下午3:56
 * @since 2.0.6
 */
public final class ExceptionProcessHelper {

    public final static ExceptionProcessHelper DEFAULT = new ExceptionProcessHelper();

    private final Map<String, IExceptionProcessor> processorMap = new ConcurrentHashMap<>();

    static {
        DEFAULT.registerProcessor(FileUploadBase.FileSizeLimitExceededException.class, target -> new IExceptionProcessor.Result(WebErrorCode.UPLOAD_FILE_SIZE_LIMIT_EXCEEDED, WebErrorCode.MSG_UPLOAD_FILE_SIZE_LIMIT_EXCEEDED));
        DEFAULT.registerProcessor(FileUploadBase.SizeLimitExceededException.class, target -> new IExceptionProcessor.Result(WebErrorCode.UPLOAD_SIZE_LIMIT_EXCEEDED, WebErrorCode.MSG_UPLOAD_SIZE_LIMIT_EXCEEDED));
        DEFAULT.registerProcessor(FileUploadBase.InvalidContentTypeException.class, target -> new IExceptionProcessor.Result(WebErrorCode.UPLOAD_CONTENT_TYPE_INVALID, WebErrorCode.MSG_UPLOAD_CONTENT_TYPE_INVALID));
        DEFAULT.registerProcessor(DataVersionMismatchException.class, target -> new IExceptionProcessor.Result(ErrorCode.DATA_VERSION_NOT_MATCH, ErrorCode.MSG_DATA_VERSION_NOT_MATCH));
        DEFAULT.registerProcessor(RequestForbiddenException.class, target -> new IExceptionProcessor.Result(WebErrorCode.REQUEST_OPERATION_FORBIDDEN, WebErrorCode.MSG_REQUEST_OPERATION_FORBIDDEN));
        DEFAULT.registerProcessor(RequestMethodNotAllowedException.class, target -> new IExceptionProcessor.Result(WebErrorCode.REQUEST_METHOD_NOT_ALLOWED, WebErrorCode.MSG_REQUEST_METHOD_NOT_ALLOWED));
        DEFAULT.registerProcessor(RequestUnauthorizedException.class, target -> new IExceptionProcessor.Result(WebErrorCode.REQUEST_RESOURCE_UNAUTHORIZED, WebErrorCode.MSG_REQUEST_RESOURCE_UNAUTHORIZED));
        DEFAULT.registerProcessor(ResourceNotFoundException.class, target -> new IExceptionProcessor.Result(WebErrorCode.RESOURCE_NOT_FOUND_OR_NOT_EXIST, WebErrorCode.MSG_RESOURCE_NOT_FOUND_OR_NOT_EXIST));
        DEFAULT.registerProcessor(UserSessionInvalidException.class, target -> new IExceptionProcessor.Result(WebErrorCode.USER_SESSION_INVALID_OR_TIMEOUT, WebErrorCode.MSG_USER_SESSION_INVALID_OR_TIMEOUT));
        DEFAULT.registerProcessor(ParameterSignatureException.class, target -> new IExceptionProcessor.Result(WebErrorCode.INVALID_PARAMS_SIGNATURE, WebErrorCode.MSG_INVALID_PARAMS_SIGNATURE));
        DEFAULT.registerProcessor(UserSessionConfirmationStateException.class, target -> new IExceptionProcessor.Result(WebErrorCode.USER_SESSION_CONFIRMATION_STATE, WebErrorCode.MSG_USER_SESSION_CONFIRMATION_STATE)
                .addAttribute(Type.Const.REDIRECT_URL, ((UserSessionConfirmationStateException) target).getRedirectUrl()));
        DEFAULT.registerProcessor(UserSessionForceOfflineException.class, target -> new IExceptionProcessor.Result(WebErrorCode.USER_SESSION_FORCE_OFFLINE, WebErrorCode.MSG_USER_SESSION_FORCE_OFFLINE)
                .addAttribute("remote_addr", ((UserSessionForceOfflineException) target).getRemoteAddr())
                .addAttribute("event_time", ((UserSessionForceOfflineException) target).getEventTime())
                .addAttribute("description", ((UserSessionForceOfflineException) target).getDescription()));
    }

    public static StringBuilder exceptionToString(Throwable e) {
        StringBuilder stringBuilder = new StringBuilder();
        if (e != null) {
            stringBuilder.append("-- Exception: ").append(e.getClass().getName()).append("\n");
            stringBuilder.append("-- Message: ").append(e.getMessage()).append("\n");
            //
            stringBuilder.append("-- StackTrace:\n");
            StackTraceElement[] stackTrace = e.getStackTrace();
            for (StackTraceElement traceElement : stackTrace) {
                stringBuilder.append("\t  at ").append(traceElement).append("\n");
            }
        }
        return stringBuilder;
    }

    public void registerProcessor(Class<? extends Throwable> target, IExceptionProcessor processor) {
        if (target == null) {
            throw new NullArgumentException("target");
        }
        if (processor == null) {
            throw new NullArgumentException("processor");
        }
        // 不允许重复注册
        if (!processorMap.containsKey(target.getName())) {
            ReentrantLockHelper.putIfAbsent(processorMap, target.getName(), processor);
        }
    }

    public IExceptionProcessor bind(Class<? extends Throwable> target) {
        return processorMap.get(target.getName());
    }
}
