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
 * 用户会话无效或超时异常
 *
 * @author 刘镇 (suninformation@163.com) on 2017/10/18 下午3:28
 * @version 1.0
 */
@ExceptionProcessor(code = ErrorCode.USER_SESSION_INVALID_OR_TIMEOUT, msg = ErrorCode.MSG_USER_SESSION_INVALID_OR_TIMEOUT)
public class UserSessionInvalidException extends RuntimeException {

    public UserSessionInvalidException() {
        super();
    }

    public UserSessionInvalidException(String message) {
        super(message);
    }

    public UserSessionInvalidException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserSessionInvalidException(Throwable cause) {
        super(cause);
    }
}
