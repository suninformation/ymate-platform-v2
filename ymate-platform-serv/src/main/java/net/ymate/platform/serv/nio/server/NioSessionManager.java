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

import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.serv.AbstractSessionManager;
import net.ymate.platform.serv.IServer;
import net.ymate.platform.serv.IServerCfg;
import net.ymate.platform.serv.nio.INioCodec;
import net.ymate.platform.serv.nio.INioSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * TCP客户端会话管理器
 *
 * @param <SESSION_WRAPPER> 会话包装类型
 * @param <MESSAGE_TYPE>    消息类型
 * @author 刘镇 (suninformation@163.com) on 2018/11/12 3:40 PM
 */
public class NioSessionManager<SESSION_WRAPPER extends NioSessionWrapper, MESSAGE_TYPE> extends AbstractSessionManager<SESSION_WRAPPER, String, MESSAGE_TYPE> {

    private static final Log LOG = LogFactory.getLog(NioSessionManager.class);

    private final INioSessionListener<SESSION_WRAPPER, MESSAGE_TYPE> sessionListener;

    public NioSessionManager(IServerCfg serverCfg, INioCodec codec, INioSessionListener<SESSION_WRAPPER, MESSAGE_TYPE> listener) {
        this(serverCfg, codec, listener, 0L);
    }

    public NioSessionManager(IServerCfg serverCfg, INioCodec codec, INioSessionListener<SESSION_WRAPPER, MESSAGE_TYPE> listener, long idleTimeInMillis) {
        super(serverCfg, codec, idleTimeInMillis);
        sessionListener = listener;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected SESSION_WRAPPER buildSessionWrapper(INioSession session, InetSocketAddress socketAddress) {
        return (SESSION_WRAPPER) new NioSessionWrapper(session);
    }

    @Override
    protected IServer<?, ?> buildServer(IServerCfg serverCfg, INioCodec codec) {
        NioServer server = new NioServer();
        server.initialize(serverCfg, new NioServerListener() {
            @Override
            public void onSessionRegistered(INioSession session) throws IOException {
                SESSION_WRAPPER sessionWrapper = registerSession(session, null);
                if (sessionWrapper != null) {
                    sessionListener.onSessionRegistered(sessionWrapper);
                }
            }

            @Override
            public void onSessionAccepted(INioSession session) throws IOException {
                super.onSessionAccepted(session);
                SESSION_WRAPPER sessionWrapper = sessionWrapper(session.id());
                if (sessionWrapper != null) {
                    sessionListener.onSessionAccepted(sessionWrapper);
                }
            }

            @Override
            public void onBeforeSessionClosed(INioSession session) throws IOException {
                SESSION_WRAPPER sessionWrapper = sessionWrapper(session.id());
                if (sessionWrapper != null) {
                    sessionListener.onBeforeSessionClosed(sessionWrapper);
                }
            }

            @Override
            public void onAfterSessionClosed(INioSession session) throws IOException {
                SESSION_WRAPPER sessionWrapper = removeSessionWrapper(session.id());
                if (sessionWrapper != null) {
                    sessionListener.onAfterSessionClosed(sessionWrapper);
                }
            }

            @Override
            @SuppressWarnings("unchecked")
            public void onMessageReceived(Object message, INioSession session) throws IOException {
                SESSION_WRAPPER sessionWrapper = sessionWrapper(session.id());
                if (sessionWrapper != null) {
                    speedTouch();
                    sessionWrapper.touch();
                    sessionListener.onMessageReceived((MESSAGE_TYPE) message, sessionWrapper);
                }
            }

            @Override
            public void onExceptionCaught(Throwable e, INioSession session) throws IOException {
                SESSION_WRAPPER sessionWrapper = sessionWrapper(session.id());
                if (sessionWrapper != null) {
                    sessionListener.onExceptionCaught(e, sessionWrapper);
                }
            }
        }, codec);
        return server;
    }

    @Override
    public void closeSessionWrapper(SESSION_WRAPPER sessionWrapper) {
        if (sessionWrapper != null && sessionWrapper.getId() != null) {
            SESSION_WRAPPER wrapper = removeSessionWrapper(sessionWrapper.getId());
            if (wrapper == null) {
                wrapper = sessionWrapper;
            }
            try {
                wrapper.getSession().closeNow();
            } catch (IOException e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("%s close exception: ", wrapper), RuntimeUtils.unwrapThrow(e));
                }
            }
        }
    }

    @Override
    public INioSessionListener<SESSION_WRAPPER, MESSAGE_TYPE> getSessionListener() {
        return sessionListener;
    }

    @Override
    public boolean sendTo(String sessionId, MESSAGE_TYPE message) throws IOException {
        SESSION_WRAPPER wrapper = sessionWrapper(sessionId);
        if (wrapper != null) {
            wrapper.getSession().send(message);
            return true;
        }
        return false;
    }
}
