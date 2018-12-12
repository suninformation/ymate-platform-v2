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
import net.ymate.platform.webmvc.util.WebResult;

/**
 * 请求的资源未授权或无权限异常
 *
 * @author 刘镇 (suninformation@163.com) on 2017/10/18 下午3:11
 * @version 1.0
 */
@ExceptionProcessor(code = WebResult.ErrorCode.REQUEST_RESOURCE_UNAUTHORIZED, msg = "The requested resource unauthorized.")
public class RequestUnauthorizedException extends RuntimeException {

    public RequestUnauthorizedException() {
        super();
    }

    public RequestUnauthorizedException(String message) {
        super(message);
    }

    public RequestUnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }

    public RequestUnauthorizedException(Throwable cause) {
        super(cause);
    }
}
