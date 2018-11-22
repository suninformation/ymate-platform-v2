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
package net.ymate.platform.serv.nio.datagram;

import net.ymate.platform.core.util.RuntimeUtils;
import net.ymate.platform.serv.*;
import net.ymate.platform.serv.nio.INioCodec;
import net.ymate.platform.serv.nio.INioSession;
import net.ymate.platform.serv.nio.server.NioSessionManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/11/14 4:47 PM
 * @version 1.0
 */
public class NioUdpSessionManager<SESSION_WRAPPER extends NioUdpSessionWrapper, MESSAGE_TYPE> extends AbstractSessionManager<SESSION_WRAPPER, InetSocketAddress, MESSAGE_TYPE> {

    private static final Log _LOG = LogFactory.getLog(NioSessionManager.class);

    private INioUdpSessionListener<SESSION_WRAPPER, MESSAGE_TYPE> __listener;

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
        __listener = listener;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected SESSION_WRAPPER doBuildSessionWrapper(INioSession session, InetSocketAddress socketAddress) {
        return (SESSION_WRAPPER) new NioUdpSessionWrapper((NioUdpSession) session, socketAddress);
    }

    @Override
    protected IServer doBuildServer(IServ owner, IServerCfg serverCfg, INioCodec codec) {
        return owner.buildUdpServer(serverCfg, codec, new NioUdpListener() {
            @Override
            @SuppressWarnings("unchecked")
            protected void onMessageReceived(NioUdpMessageWrapper messageWrapper, INioSession session) throws IOException {
                SESSION_WRAPPER _wrapper = sessionWrapper(messageWrapper.getSocketAddress());
                if (_wrapper == null) {
                    _wrapper = __doRegisterSession(session, messageWrapper.getSocketAddress());
                    if (_LOG.isDebugEnabled()) {
                        _LOG.debug(_wrapper + " - Registered. Session count: " + sessionCount());
                    }
                } else {
                    speedTouch();
                    _wrapper.touch();
                }
                if (_wrapper != null) {
                    if (_LOG.isDebugEnabled()) {
                        _LOG.debug(_wrapper + " - Received: " + messageWrapper.getMessage());
                    }
                    Object _result = __listener.onMessageReceived(_wrapper, (MESSAGE_TYPE) messageWrapper.getMessage());
                    if (_result != null) {
                        ((NioUdpSession) session).send(messageWrapper.getSocketAddress(), _result);
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
                SESSION_WRAPPER _wrapper = sessionWrapper(sourceAddress);
                if (_wrapper != null) {
                    if (_LOG.isDebugEnabled()) {
                        _LOG.debug(_wrapper + " - Exception: ", RuntimeUtils.unwrapThrow(e));
                    }
                    __listener.onExceptionCaught(_wrapper, e);
                }
            }
        });
    }

    @Override
    public ISessionListener<SESSION_WRAPPER> sessionListener() {
        return __listener;
    }

    @Override
    public boolean sendTo(InetSocketAddress sessionId, MESSAGE_TYPE message) throws IOException {
        NioUdpSessionWrapper _wrapper = sessionWrapper(sessionId);
        if (_wrapper != null) {
            _wrapper.getSession().send(sessionId, message);
            return true;
        }
        return false;
    }
}
