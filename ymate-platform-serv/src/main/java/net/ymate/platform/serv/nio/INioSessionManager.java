/*
 * Copyright 2007-2018 the original author or authors.
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
package net.ymate.platform.serv.nio;

import net.ymate.platform.serv.ISessionManager;
import net.ymate.platform.serv.ISessionWrapper;

import java.io.IOException;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/11/16 7:01 PM
 * @version 1.0
 */
public interface INioSessionManager<SESSION_WRAPPER extends ISessionWrapper, MESSAGE_TYPE> extends ISessionManager<SESSION_WRAPPER> {

    /**
     * 向目标客户端发送消息
     *
     * @param sessionId 目标客户端会话唯一标识
     * @param message   消息对象
     * @return 若指定的目标客户端标识存在且有效则返回true
     * @throws IOException 可能产生的异常
     */
    boolean sendTo(String sessionId, MESSAGE_TYPE message) throws IOException;
}
