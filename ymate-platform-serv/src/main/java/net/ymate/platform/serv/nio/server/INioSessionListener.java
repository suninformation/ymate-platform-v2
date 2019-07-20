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
package net.ymate.platform.serv.nio.server;

import net.ymate.platform.core.beans.annotation.Ignored;
import net.ymate.platform.serv.ISessionListener;

import java.io.IOException;

/**
 * TCP会话事件监听器接口
 *
 * @param <SESSION_WRAPPER> 会话包装器类型
 * @param <MESSAGE_TYPE>    消息类型
 * @author 刘镇 (suninformation@163.com) on 2018/11/13 12:11 AM
 */
@Ignored
public interface INioSessionListener<SESSION_WRAPPER extends NioSessionWrapper, MESSAGE_TYPE> extends ISessionListener<SESSION_WRAPPER> {

    /**
     * 会话注册成功事件处理方法
     *
     * @param session 当前会话对象包装器
     * @throws IOException 可能产生的异常
     */
    void onSessionRegistered(SESSION_WRAPPER session) throws IOException;

    /**
     * 会话被接受事件处理方法
     *
     * @param session 当前会话对象包装器
     * @throws IOException 可能产生的异常
     */
    void onSessionAccepted(SESSION_WRAPPER session) throws IOException;

    /**
     * 会话关闭之前事件处理方法
     *
     * @param session 当前会话对象包装器
     * @throws IOException 可能产生的异常
     */
    void onBeforeSessionClosed(SESSION_WRAPPER session) throws IOException;

    /**
     * 会话关闭之后事件处理方法
     *
     * @param session 当前会话对象包装器
     * @throws IOException 可能产生的异常
     */
    void onAfterSessionClosed(SESSION_WRAPPER session) throws IOException;

    /**
     * 消息到达事件处理方法
     *
     * @param message 消息对象
     * @param session 当前会话对象包装器
     * @throws IOException 可能产生的异常
     */
    void onMessageReceived(MESSAGE_TYPE message, SESSION_WRAPPER session) throws IOException;

    /**
     * 异常事件处理方法
     *
     * @param e       异常对象
     * @param session 当前会话对象包装器
     * @throws IOException 可能产生的异常
     */
    void onExceptionCaught(Throwable e, SESSION_WRAPPER session) throws IOException;
}