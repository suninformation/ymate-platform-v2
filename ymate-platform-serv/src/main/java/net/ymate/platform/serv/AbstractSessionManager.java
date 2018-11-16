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
package net.ymate.platform.serv;

import net.ymate.platform.core.support.Speedometer;
import net.ymate.platform.core.support.impl.DefaultSpeedListener;
import net.ymate.platform.serv.nio.INioCodec;
import net.ymate.platform.serv.nio.INioSession;
import org.apache.commons.lang.NullArgumentException;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/11/14 11:35 AM
 * @version 1.0
 */
public abstract class AbstractSessionManager<SESSION_WRAPPER extends ISessionWrapper> implements ISessionManager<SESSION_WRAPPER> {

    private Map<String, SESSION_WRAPPER> __sessions;

    private IServer __server;

    private IServerCfg __serverCfg;

    private INioCodec __codec;

    private Speedometer __speedometer;

    private final Object __locker = new Object();

    /**
     * 构造器
     *
     * @param serverCfg 服务端配置接口实现
     * @param codec     编解码器接口实现
     */
    public AbstractSessionManager(IServerCfg serverCfg, INioCodec codec) {
        __sessions = new ConcurrentHashMap<String, SESSION_WRAPPER>();
        __serverCfg = serverCfg;
        __codec = codec;
    }

    public SESSION_WRAPPER getSessionWrapper(String sessionId) {
        return __sessions.get(sessionId);
    }

    public Collection<SESSION_WRAPPER> getSessionWrappers() {
        return Collections.unmodifiableCollection(__sessions.values());
    }

    public long getSessionCount() {
        return __sessions.size();
    }

    public void speedometer(Speedometer speedometer) {
        if (__server == null) {
            __speedometer = speedometer;
        }
    }

    /**
     * 触发速度计数
     */
    public void speedTouch() {
        if (__speedometer != null) {
            __speedometer.touch();
        }
    }

    /**
     * 注册客户端会话
     *
     * @param session       会话对象
     * @param socketAddress 目标来源套接字地址
     */
    protected SESSION_WRAPPER __doRegisterSession(INioSession session, InetSocketAddress socketAddress) {
        SESSION_WRAPPER _wrapper = doBuildSessionWrapper(session, socketAddress);
        if (doRegister(_wrapper)) {
            putSessionWrapper(_wrapper.getId(), _wrapper);
            return _wrapper;
        }
        return null;
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

    /**
     * 将会话包装器对象放入管理器
     *
     * @param sessionId      会话标识符
     * @param sessionWrapper 会话包装器对象
     */
    protected void putSessionWrapper(String sessionId, SESSION_WRAPPER sessionWrapper) {
        __sessions.put(sessionId, sessionWrapper);
    }

    /**
     * 移除会话
     *
     * @param sessionId 会话标识符
     * @return 返回被移除的会话对象, 若不存在则返回null
     */
    protected SESSION_WRAPPER removeSessionWrapper(String sessionId) {
        return __sessions.remove(sessionId);
    }

    /**
     * 根据会话对象构建包装器
     *
     * @param session       会话对象
     * @param socketAddress 目标来源套接字地址
     * @return 返回包装器对象
     */
    protected abstract SESSION_WRAPPER doBuildSessionWrapper(INioSession session, InetSocketAddress socketAddress);

    protected abstract IServer doBuildServer(IServ owner, IServerCfg serverCfg, INioCodec codec);

    @Override
    public void init(IServ owner) throws Exception {
        synchronized (__locker) {
            if (__server == null) {
                __server = doBuildServer(owner, __serverCfg, __codec);
                if (__server == null) {
                    throw new NullArgumentException("server");
                }
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
}
