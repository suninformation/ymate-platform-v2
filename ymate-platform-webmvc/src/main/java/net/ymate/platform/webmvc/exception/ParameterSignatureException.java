/*
 * Copyright 2007-2020 the original author or authors.
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

/**
 * 请求参数签名无效异常
 *
 * @author 刘镇 (suninformation@163.com) on 2020/02/18 14:02
 * @since 2.1.0
 */
public class ParameterSignatureException extends RuntimeException {

    public ParameterSignatureException() {
        super();
    }

    public ParameterSignatureException(String message) {
        super(message);
    }

    public ParameterSignatureException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParameterSignatureException(Throwable cause) {
        super(cause);
    }
}
