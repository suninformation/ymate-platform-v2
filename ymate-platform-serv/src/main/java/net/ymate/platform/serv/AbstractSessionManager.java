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

import net.ymate.platform.commons.Speedometer;
import net.ymate.platform.commons.impl.DefaultSpeedListener;
import net.ymate.platform.commons.impl.DefaultThreadFactory;
import net.ymate.platform.commons.util.DateTimeUtils;
import net.ymate.platform.commons.util.ThreadUtils;
import net.ymate.platform.serv.impl.DefaultSessionIdleChecker;
import net.ymate.platform.serv.nio.INioCodec;
import net.ymate.platform.serv.nio.INioSession;
import org.apache.commons.lang.NullArgumentException;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @param <SESSION_WRAPPER> 会话包装类型
 * @param <SESSION_ID>      会话标识类型
 * @param <MESSAGE_TYPE>    消息类型
 * @author 刘镇 (suninformation@163.com) on 2018/11/14 11:35 AM
 */
public abstract class AbstractSessionManager<SESSION_WRAPPER extends ISessionWrapper<?, ?>, SESSION_ID, MESSAGE_TYPE> implements ISessionManager<SESSION_WRAPPER, SESSION_ID, MESSAGE_TYPE> {

    private final Map<SESSION_ID, SESSION_WRAPPER> sessionWrappers = new ConcurrentHashMap<>();

    private IServer<?, ?> server;

    private final IServerCfg serverCfg;

    private final INioCodec codec;

    private final long idleTimeInMillis;

    private ISessionIdleChecker<SESSION_WRAPPER, SESSION_ID, MESSAGE_TYPE> idleChecker;

    private ScheduledExecutorService idleCheckExecutorService;

    private Speedometer speedometer;

    private final Object locker = new Object();

    /**
     * 构造器
     *
     * @param serverCfg        服务端配置接口实现
     * @param codec            编解码器接口实现
     * @param idleTimeInMillis 会话空闲时间毫秒值, 小于等于0表示不开启空闲检查
     */
    public AbstractSessionManager(IServerCfg serverCfg, INioCodec codec, long idleTimeInMillis) {
        this.serverCfg = serverCfg;
        this.codec = codec;
        this.idleTimeInMillis = idleTimeInMillis;
    }

    @Override
    public SESSION_WRAPPER sessionWrapper(SESSION_ID sessionId) {
        return sessionId == null ? null : sessionWrappers.get(sessionId);
    }

    @Override
    public Collection<SESSION_WRAPPER> sessionWrappers() {
        return Collections.unmodifiableCollection(sessionWrappers.values());
    }

    @Override
    public boolean contains(SESSION_ID sessionId) {
        return sessionWrappers.containsKey(sessionId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void closeSessionWrapper(SESSION_WRAPPER sessionWrapper) {
        if (sessionWrapper != null && sessionWrapper.getId() != null) {
            removeSessionWrapper((SESSION_ID) sessionWrapper.getId());
        }
    }

    @Override
    public long sessionCount() {
        return sessionWrappers.size();
    }

    @Override
    public void speedometer(Speedometer speedometer) {
        if (server == null) {
            this.speedometer = speedometer;
        }
    }

    @Override
    public void idleChecker(ISessionIdleChecker<SESSION_WRAPPER, SESSION_ID, MESSAGE_TYPE> sessionIdleChecker) {
        if (server == null) {
            idleChecker = sessionIdleChecker;
        }
    }

    /**
     * 触发速度计数
     */
    public void speedTouch() {
        if (speedometer != null) {
            speedometer.touch();
        }
    }

    /**
     * 注册客户端会话
     *
     * @param session       会话对象
     * @param socketAddress 目标来源套接字地址
     * @return 返回注册的客户端会话包装器对象
     */
    @SuppressWarnings("unchecked")
    protected SESSION_WRAPPER registerSession(INioSession session, InetSocketAddress socketAddress) {
        SESSION_WRAPPER sessionWrapper = buildSessionWrapper(session, socketAddress);
        if (register(sessionWrapper)) {
            putSessionWrapper((SESSION_ID) sessionWrapper.getId(), sessionWrapper);
            return sessionWrapper;
        }
        return null;
    }

    /**
     * 执行会话注册逻辑
     *
     * @param session 会话包装器对象
     * @return 返回值为false表示不向管理器注册当前会话
     */
    protected boolean register(SESSION_WRAPPER session) {
        return true;
    }

    /**
     * 将会话包装器对象放入管理器
     *
     * @param sessionId      会话标识符
     * @param sessionWrapper 会话包装器对象
     */
    protected void putSessionWrapper(SESSION_ID sessionId, SESSION_WRAPPER sessionWrapper) {
        sessionWrappers.put(sessionId, sessionWrapper);
    }

    /**
     * 移除会话
     *
     * @param sessionId 会话标识符
     * @return 返回被移除的会话对象, 若不存在则返回null
     */
    protected SESSION_WRAPPER removeSessionWrapper(SESSION_ID sessionId) {
        return sessionWrappers.remove(sessionId);
    }

    /**
     * 根据会话对象构建包装器
     *
     * @param session       会话对象
     * @param socketAddress 目标来源套接字地址
     * @return 返回包装器对象
     */
    protected abstract SESSION_WRAPPER buildSessionWrapper(INioSession session, InetSocketAddress socketAddress);

    /**
     * 根据服务端配置构建服务端实例
     *
     * @param serverCfg 服务端配置
     * @param codec     编解码器
     * @return 返回构建后的服务端接口实例对象
     */
    protected abstract IServer<?, ?> buildServer(IServerCfg serverCfg, INioCodec codec);

    @Override
    public void initialize() throws Exception {
        synchronized (locker) {
            if (server == null) {
                server = buildServer(serverCfg, codec);
                if (server == null) {
                    throw new NullArgumentException("server");
                }
            }
        }
        server.start();
        if (speedometer != null && !speedometer.isStarted()) {
            speedometer.start(new DefaultSpeedListener(speedometer));
        }
        //
        if (idleTimeInMillis > 0) {
            if (idleChecker == null) {
                idleChecker = new DefaultSessionIdleChecker<>();
            }
            if (!idleChecker.isInitialized()) {
                idleChecker.initialize(this);
            }
            //
            idleCheckExecutorService = ThreadUtils.newScheduledThreadPool(1, DefaultThreadFactory.create("SessionIdleChecker-"));
            idleCheckExecutorService.scheduleWithFixedDelay(() -> idleChecker.processIdleSession(sessionWrappers, idleTimeInMillis), DateTimeUtils.SECOND, DateTimeUtils.SECOND, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void close() throws Exception {
        if (speedometer != null && speedometer.isStarted()) {
            speedometer.close();
        }
        if (idleCheckExecutorService != null && !idleCheckExecutorService.isShutdown()) {
            idleCheckExecutorService.shutdownNow();
        }
        if (server != null && server.isStarted()) {
            server.close();
        }
    }
}
