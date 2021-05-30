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
package net.ymate.platform.serv;

import net.ymate.platform.serv.nio.INioSession;

import java.io.IOException;

/**
 * 事件监听器
 *
 * @param <SESSION> 会话类型
 * @author 刘镇 (suninformation@163.com) on 15/11/6 下午3:41
 */
public interface IListener<SESSION extends INioSession> {

    /**
     * 会话注册成功事件处理方法
     *
     * @param session 当前会话对象
     * @throws IOException 可能产生的异常
     */
    void onSessionRegistered(SESSION session) throws IOException;

    /**
     * 会话连接事件处理方法
     *
     * @param session 当前会话对象
     * @throws IOException 可能产生的异常
     */
    void onSessionConnected(SESSION session) throws IOException;

    /**
     * 会话被接受事件处理方法
     *
     * @param session 当前会话对象
     * @throws IOException 可能产生的异常
     */
    void onSessionAccepted(SESSION session) throws IOException;

    /**
     * 会话关闭前事件处理方法
     *
     * @param session 当前会话对象
     * @throws IOException 可能产生的异常
     */
    void onBeforeSessionClosed(SESSION session) throws IOException;

    /**
     * 会话关闭后事件处理方法
     *
     * @param session 当前会话对象
     * @throws IOException 可能产生的异常
     */
    void onAfterSessionClosed(SESSION session) throws IOException;

    /**
     * 消息到达事件处理方法
     *
     * @param message 消息对象
     * @param session 会话对象
     * @throws IOException 可能产生的异常
     */
    void onMessageReceived(Object message, SESSION session) throws IOException;

    /**
     * 异常事件处理方法
     *
     * @param e       异常对象
     * @param session 会话对象
     * @throws IOException 可能产生的异常
     */
    void onExceptionCaught(Throwable e, SESSION session) throws IOException;

    /**
     * 断线重连事件处理方法
     *
     * @param client 当前客户端对象
     * @throws IOException 可能产生的异常
     */
    void onClientReconnected(IClient<?, ?> client) throws IOException;
}
