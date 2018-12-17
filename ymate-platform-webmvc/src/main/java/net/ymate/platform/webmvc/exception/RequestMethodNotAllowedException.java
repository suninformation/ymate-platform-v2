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
package net.ymate.platform.webmvc.exception;

import net.ymate.platform.webmvc.annotation.ExceptionProcessor;
import net.ymate.platform.webmvc.util.ErrorCode;

/**
 * 请求方法不支持或不正确异常
 *
 * @author 刘镇 (suninformation@163.com) on 2017/10/18 下午3:19
 * @version 1.0
 */
@ExceptionProcessor(code = ErrorCode.REQUEST_METHOD_NOT_ALLOWED, msg = ErrorCode.MSG_REQUEST_METHOD_NOT_ALLOWED)
public class RequestMethodNotAllowedException extends RuntimeException {

    public RequestMethodNotAllowedException() {
        super();
    }

    public RequestMethodNotAllowedException(String message) {
        super(message);
    }

    public RequestMethodNotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }

    public RequestMethodNotAllowedException(Throwable cause) {
        super(cause);
    }
}
