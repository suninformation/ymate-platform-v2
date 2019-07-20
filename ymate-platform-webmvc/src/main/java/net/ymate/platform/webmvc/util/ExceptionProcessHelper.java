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
        DEFAULT.registerProcessor(FileUploadBase.FileSizeLimitExceededException.class, target -> new IExceptionProcessor.Result(ErrorCode.UPLOAD_FILE_SIZE_LIMIT_EXCEEDED, ErrorCode.MSG_UPLOAD_FILE_SIZE_LIMIT_EXCEEDED));
        DEFAULT.registerProcessor(FileUploadBase.SizeLimitExceededException.class, target -> new IExceptionProcessor.Result(ErrorCode.UPLOAD_SIZE_LIMIT_EXCEEDED, ErrorCode.MSG_UPLOAD_SIZE_LIMIT_EXCEEDED));
        DEFAULT.registerProcessor(FileUploadBase.InvalidContentTypeException.class, target -> new IExceptionProcessor.Result(ErrorCode.UPLOAD_CONTENT_TYPE_INVALID, ErrorCode.MSG_UPLOAD_CONTENT_TYPE_INVALID));
        DEFAULT.registerProcessor(FileUploadBase.InvalidContentTypeException.class, target -> new IExceptionProcessor.Result(ErrorCode.DATA_VERSION_NOT_MATCH, ErrorCode.MSG_DATA_VERSION_NOT_MATCH));
        DEFAULT.registerProcessor(FileUploadBase.InvalidContentTypeException.class, target -> new IExceptionProcessor.Result(ErrorCode.REQUEST_OPERATION_FORBIDDEN, ErrorCode.MSG_REQUEST_OPERATION_FORBIDDEN));
        DEFAULT.registerProcessor(FileUploadBase.InvalidContentTypeException.class, target -> new IExceptionProcessor.Result(ErrorCode.REQUEST_METHOD_NOT_ALLOWED, ErrorCode.MSG_REQUEST_METHOD_NOT_ALLOWED));
        DEFAULT.registerProcessor(FileUploadBase.InvalidContentTypeException.class, target -> new IExceptionProcessor.Result(ErrorCode.REQUEST_RESOURCE_UNAUTHORIZED, ErrorCode.MSG_REQUEST_RESOURCE_UNAUTHORIZED));
        DEFAULT.registerProcessor(FileUploadBase.InvalidContentTypeException.class, target -> new IExceptionProcessor.Result(ErrorCode.RESOURCE_NOT_FOUND_OR_NOT_EXIST, ErrorCode.MSG_RESOURCE_NOT_FOUND_OR_NOT_EXIST));
        DEFAULT.registerProcessor(FileUploadBase.InvalidContentTypeException.class, target -> new IExceptionProcessor.Result(ErrorCode.USER_SESSION_INVALID_OR_TIMEOUT, ErrorCode.MSG_USER_SESSION_INVALID_OR_TIMEOUT));
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
