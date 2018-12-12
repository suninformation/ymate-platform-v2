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
 * 数据版本不匹配异常
 *
 * @author 刘镇 (suninformation@163.com) on 2017/10/18 下午1:59
 * @version 1.0
 */
@ExceptionProcessor(code = WebResult.ErrorCode.DATA_VERSION_NOT_MATCH, msg = "The data version does not match.")
public class DataVersionMismatchException extends RuntimeException {

    public DataVersionMismatchException() {
        super();
    }

    public DataVersionMismatchException(String message) {
        super(message);
    }

    public DataVersionMismatchException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataVersionMismatchException(Throwable cause) {
        super(cause);
    }
}
