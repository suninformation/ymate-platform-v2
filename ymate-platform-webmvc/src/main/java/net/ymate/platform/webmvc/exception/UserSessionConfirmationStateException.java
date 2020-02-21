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
 * 用户会话安全确认异常
 *
 * @author 刘镇 (suninformation@163.com) on 2020/02/21 16:20
 * @since 2.1.0
 */
public class UserSessionConfirmationStateException extends RuntimeException {

    private String redirectUrl;

    public UserSessionConfirmationStateException() {
        super();
    }

    public UserSessionConfirmationStateException(String message) {
        super(message);
    }

    public UserSessionConfirmationStateException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserSessionConfirmationStateException(Throwable cause) {
        super(cause);
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }
}
