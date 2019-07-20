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
package net.ymate.platform.serv.nio.datagram;

import net.ymate.platform.core.beans.annotation.Ignored;
import net.ymate.platform.serv.ISessionListener;

import java.io.IOException;

/**
 * UDP会话事件监听器接口
 *
 * @param <SESSION_WRAPPER> 会话包装器类型
 * @param <MESSAGE_TYPE>    消息类型
 * @author 刘镇 (suninformation@163.com) on 2018/11/14 3:58 PM
 */
@Ignored
public interface INioUdpSessionListener<SESSION_WRAPPER extends NioUdpSessionWrapper, MESSAGE_TYPE> extends ISessionListener<SESSION_WRAPPER> {

    /**
     * 消息到达事件处理
     *
     * @param sessionWrapper 当前会话对象包装器
     * @param message        消息对象
     * @return 预回应的消息对象, 返回null表示不发送回应消息
     * @throws IOException 可能产生的异常
     */
    Object onMessageReceived(SESSION_WRAPPER sessionWrapper, MESSAGE_TYPE message) throws IOException;

    /**
     * 捕获异常事件处理
     *
     * @param sessionWrapper 当前会话对象包装器
     * @param e              异常对象
     * @throws IOException 可能产生的异常
     */
    void onExceptionCaught(SESSION_WRAPPER sessionWrapper, Throwable e) throws IOException;
}
