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
package net.ymate.platform.webmvc.util;

import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 刘镇 (suninformation@163.com) on 2017/10/18 下午3:56
 * @version 1.0
 * @since 2.0.6
 */
public final class ExceptionProcessHelper {

    private static final Log _LOG = LogFactory.getLog(ExceptionProcessHelper.class);

    public final static ExceptionProcessHelper DEFAULT = new ExceptionProcessHelper();

    static {
        DEFAULT.registerProcessor(FileUploadBase.FileSizeLimitExceededException.class, new IExceptionProcessor() {
            @Override
            public Result process(Throwable target) throws Exception {
                return new Result(WebResult.ErrorCode.UPLOAD_FILE_SIZE_LIMIT_EXCEEDED, "The size of the uploaded file exceeds the limit.");
            }
        });
        DEFAULT.registerProcessor(FileUploadBase.SizeLimitExceededException.class, new IExceptionProcessor() {
            @Override
            public Result process(Throwable target) throws Exception {
                return new Result(WebResult.ErrorCode.UPLOAD_SIZE_LIMIT_EXCEEDED, "The total size of uploaded files exceeds the limit.");
            }
        });
        DEFAULT.registerProcessor(FileUploadBase.InvalidContentTypeException.class, new IExceptionProcessor() {
            @Override
            public Result process(Throwable target) throws Exception {
                return new Result(WebResult.ErrorCode.UPLOAD_CONTENT_TYPE_INVALID, "The upload file content type is invalid.");
            }
        });
    }

    public static StringBuilder exceptionToString(Throwable e) {
        StringBuilder _errSB = new StringBuilder();
        if (e != null) {
            _errSB.append("-- Exception: ").append(e.getClass().getName()).append("\n");
            _errSB.append("-- Message: ").append(e.getMessage()).append("\n");
            //
            _errSB.append("-- StackTrace:\n");
            StackTraceElement[] _stacks = e.getStackTrace();
            for (StackTraceElement _stack : _stacks) {
                _errSB.append("\t  at ").append(_stack).append("\n");
            }
        }
        return _errSB;
    }

    private final Map<String, IExceptionProcessor> __processors = new ConcurrentHashMap<String, IExceptionProcessor>();

    public void registerProcessor(Class<? extends Throwable> target, IExceptionProcessor processor) {
        if (target == null) {
            throw new NullArgumentException("target");
        }
        if (processor == null) {
            throw new NullArgumentException("processor");
        }
        // 不允许重复注册
        if (!__processors.containsKey(target.getName())) {
            __processors.put(target.getName(), processor);
        }
    }

    public IExceptionProcessor bind(Class<? extends Throwable> target) {
        return __processors.get(target.getName());
    }
}
