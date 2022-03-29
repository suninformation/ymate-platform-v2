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

import net.ymate.platform.serv.AbstractSessionManager;
import net.ymate.platform.serv.IServer;
import net.ymate.platform.serv.IServerCfg;
import net.ymate.platform.serv.nio.INioCodec;
import net.ymate.platform.serv.nio.INioSession;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * @param <SESSION_WRAPPER> 会话包装类型
 * @param <MESSAGE_TYPE>    消息类型
 * @author 刘镇 (suninformation@163.com) on 2018/11/14 4:47 PM
 */
public class NioUdpSessionManager<SESSION_WRAPPER extends NioUdpSessionWrapper, MESSAGE_TYPE> extends AbstractSessionManager<SESSION_WRAPPER, InetSocketAddress, MESSAGE_TYPE> {

    private final INioUdpSessionListener<SESSION_WRAPPER, MESSAGE_TYPE> sessionListener;

    /**
     * 构造器
     *
     * @param serverCfg 服务端配置接口实现
     * @param codec     编解码器接口实现
     * @param listener  会话事件监听器
     */
    public NioUdpSessionManager(IServerCfg serverCfg, INioCodec codec, INioUdpSessionListener<SESSION_WRAPPER, MESSAGE_TYPE> listener) {
        this(serverCfg, codec, listener, 0L);
    }

    /**
     * 构造器
     *
     * @param serverCfg        服务端配置接口实现
     * @param codec            编解码器接口实现
     * @param listener         会话事件监听器
     * @param idleTimeInMillis 会话空闲时间毫秒值, 小于等于0表示不开启空闲检查
     */
    public NioUdpSessionManager(IServerCfg serverCfg, INioCodec codec, INioUdpSessionListener<SESSION_WRAPPER, MESSAGE_TYPE> listener, long idleTimeInMillis) {
        super(serverCfg, codec, idleTimeInMillis);
        this.sessionListener = listener;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected SESSION_WRAPPER buildSessionWrapper(INioSession session, InetSocketAddress socketAddress) {
        return (SESSION_WRAPPER) new NioUdpSessionWrapper((NioUdpSession) session, socketAddress);
    }

    @Override
    protected IServer<?, ?> buildServer(IServerCfg serverCfg, INioCodec codec) {
        NioUdpServer udpServer = new NioUdpServer();
        udpServer.initialize(serverCfg, new AbstractNioUdpListener() {
            @Override
            @SuppressWarnings("unchecked")
            protected void onMessageReceived(NioUdpMessageWrapper<?> messageWrapper, INioSession session) throws IOException {
                SESSION_WRAPPER sessionWrapper = sessionWrapper(messageWrapper.getSocketAddress());
                if (sessionWrapper == null) {
                    sessionWrapper = registerSession(session, messageWrapper.getSocketAddress());
                } else {
                    speedTouch();
                    sessionWrapper.touch();
                }
                if (sessionWrapper != null) {
                    Object result = sessionListener.onMessageReceived(sessionWrapper, (MESSAGE_TYPE) messageWrapper.getMessage());
                    if (result != null) {
                        ((NioUdpSession) session).send(messageWrapper.getSocketAddress(), result);
                    }
                }
            }

            @Override
            public Object onMessageReceived(InetSocketAddress sourceAddress, Object message) throws IOException {
                // DO NOTHING...
                return null;
            }

            @Override
            public void onExceptionCaught(InetSocketAddress sourceAddress, Throwable e) throws IOException {
                SESSION_WRAPPER sessionWrapper = sessionWrapper(sourceAddress);
                if (sessionWrapper != null) {
                    sessionListener.onExceptionCaught(sessionWrapper, e);
                }
            }
        }, codec);
        return udpServer;
    }

    @Override
    public INioUdpSessionListener<SESSION_WRAPPER, MESSAGE_TYPE> getSessionListener() {
        return sessionListener;
    }

    @Override
    public boolean sendTo(InetSocketAddress sessionId, MESSAGE_TYPE message) throws IOException {
        NioUdpSessionWrapper sessionWrapper = sessionWrapper(sessionId);
        if (sessionWrapper != null) {
            sessionWrapper.getSession().send(sessionId, message);
            return true;
        }
        return false;
    }
}
