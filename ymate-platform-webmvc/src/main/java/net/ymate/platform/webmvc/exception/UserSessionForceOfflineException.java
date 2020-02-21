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
 * 用户会话被强制下线异常
 *
 * @author 刘镇 (suninformation@163.com) on 2020/02/21 18:17
 * @since 2.1.0
 */
public class UserSessionForceOfflineException extends RuntimeException {

    private String remoteAddr;

    private Long eventTime;

    private String description;

    public UserSessionForceOfflineException() {
        super();
    }

    public UserSessionForceOfflineException(String message) {
        super(message);
    }

    public UserSessionForceOfflineException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserSessionForceOfflineException(Throwable cause) {
        super(cause);
    }

    public String getRemoteAddr() {
        return remoteAddr;
    }

    public void setRemoteAddr(String remoteAddr) {
        this.remoteAddr = remoteAddr;
    }

    public Long getEventTime() {
        return eventTime;
    }

    public void setEventTime(Long eventTime) {
        this.eventTime = eventTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
