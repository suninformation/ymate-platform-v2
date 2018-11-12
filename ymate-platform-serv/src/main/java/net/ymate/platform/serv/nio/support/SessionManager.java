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
package net.ymate.platform.serv.nio.support;

import net.ymate.platform.core.support.IInitializable;
import net.ymate.platform.core.support.Speedometer;
import net.ymate.platform.core.support.impl.DefaultSpeedListener;
import net.ymate.platform.core.util.RuntimeUtils;
import net.ymate.platform.serv.IServ;
import net.ymate.platform.serv.IServerCfg;
import net.ymate.platform.serv.nio.INioCodec;
import net.ymate.platform.serv.nio.INioSession;
import net.ymate.platform.serv.nio.server.NioServer;
import net.ymate.platform.serv.nio.server.NioServerListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 客户端会话管理器接口
 *
 * @author 刘镇 (suninformation@163.com) on 2018/11/12 3:40 PM
 * @version 1.0
 */
public class SessionManager<SESSION_WRAPPER extends SessionWrapper, MESSAGE_TYPE> extends NioServerListener implements IInitializable<IServ> {

    private static final Log _LOG = LogFactory.getLog(SessionManager.class);

    private Map<String, SESSION_WRAPPER> __sessions;

    private NioServer __server;

    private IServerCfg __serverCfg;

    private INioCodec __codec;

    private ISessionListener<SESSION_WRAPPER, MESSAGE_TYPE> __listener;

    private AtomicLong __counter = new AtomicLong(0L);

    private Speedometer __speedometer;

    private final Object __locker = new Object();

    public SessionManager(IServerCfg serverCfg, INioCodec codec, ISessionListener<SESSION_WRAPPER, MESSAGE_TYPE> listener) {
        __sessions = new ConcurrentHashMap<String, SESSION_WRAPPER>();
        __serverCfg = serverCfg;
        __codec = codec;
        __listener = listener;
    }

    /**
     * 注册客户端会话
     *
     * @param session 会话包装器对象
     */
    private SESSION_WRAPPER __doRegisterSession(INioSession session) {
        SESSION_WRAPPER _wrapper = doBuildWrapper(session);
        if (doRegister(_wrapper)) {
            __counter.incrementAndGet();
            __sessions.put(_wrapper.getId(), _wrapper);
            return _wrapper;
        }
        return null;
    }

    private SESSION_WRAPPER __doRemoveSession(String sessionId) {
        __counter.decrementAndGet();
        return __sessions.remove(sessionId);
    }

    /**
     * 执行会话注册逻辑
     *
     * @param session 会话包装器对象
     * @return 返回值为false表示不向管理器注册当前会话
     */
    protected boolean doRegister(SESSION_WRAPPER session) {
        return true;
    }

    @SuppressWarnings("unchecked")
    protected SESSION_WRAPPER doBuildWrapper(INioSession session) {
        return (SESSION_WRAPPER) new SessionWrapper(session);
    }

    public ISessionListener<SESSION_WRAPPER, MESSAGE_TYPE> getSessionListener() {
        return __listener;
    }

    public SESSION_WRAPPER getSession(String sessionId) {
        return __sessions.get(sessionId);
    }

    public Collection<SESSION_WRAPPER> getSessions() {
        return Collections.unmodifiableCollection(__sessions.values());
    }

    public long getSessionCount() {
        return __counter.get();
    }

    public void speedometer(Speedometer speedometer) {
        __speedometer = speedometer;
    }

    @Override
    public void init(IServ owner) throws Exception {
        synchronized ((__locker)) {
            if (__server == null) {
                __server = owner.buildServer(__serverCfg, __codec, this);
            }
        }
        __server.start();
        if (__speedometer != null && !__speedometer.isStarted()) {
            __speedometer.start(new DefaultSpeedListener(__speedometer));
        }
    }

    @Override
    public void destroy() throws Exception {
        if (__speedometer != null && __speedometer.isStarted()) {
            __speedometer.close();
        }
        __server.close();
    }

    //

    @Override
    public void onSessionRegistered(INioSession session) throws IOException {
        SESSION_WRAPPER _wrapper = __doRegisterSession(session);
        if (_wrapper != null) {
            if (_LOG.isDebugEnabled()) {
                _LOG.debug(session + " - Registered. Session count: " + __counter.get());
            }
            __listener.onSessionRegistered(_wrapper);
        }
    }

    @Override
    public void onSessionAccepted(INioSession session) throws IOException {
        super.onSessionAccepted(session);
        SESSION_WRAPPER _wrapper = getSession(session.id());
        if (_wrapper != null) {
            if (_LOG.isDebugEnabled()) {
                _LOG.debug(session + " - Accepted.");
            }
            __listener.onSessionAccepted(_wrapper);
        }
    }

    @Override
    public void onBeforeSessionClosed(INioSession session) throws IOException {
        SESSION_WRAPPER _wrapper = getSession(session.id());
        if (_wrapper != null) {
            if (_LOG.isDebugEnabled()) {
                _LOG.debug(session + " - Before closed.");
            }
            __listener.onBeforeSessionClosed(_wrapper);
        }
    }

    @Override
    public void onAfterSessionClosed(INioSession session) throws IOException {
        SESSION_WRAPPER _wrapper = __doRemoveSession(session.id());
        if (_wrapper != null) {
            if (_LOG.isDebugEnabled()) {
                _LOG.debug(session + " - After closed. Session count: " + __counter.get());
            }
            __listener.onAfterSessionClosed(_wrapper);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onMessageReceived(Object message, INioSession session) throws IOException {
        SESSION_WRAPPER _wrapper = getSession(session.id());
        if (_wrapper != null) {
            if (_LOG.isDebugEnabled()) {
                _LOG.debug(session + " - Received: " + message);
            }
            if (__speedometer != null) {
                __speedometer.touch();
            }
            __listener.onMessageReceived((MESSAGE_TYPE) message, _wrapper);
        }
    }

    @Override
    public void onExceptionCaught(Throwable e, INioSession session) throws IOException {
        SESSION_WRAPPER _wrapper = getSession(session.id());
        if (_wrapper != null) {
            if (_LOG.isDebugEnabled()) {
                _LOG.debug(session + " - Exception: ", RuntimeUtils.unwrapThrow(e));
            }
            __listener.onExceptionCaught(e, _wrapper);
        }
    }
}
