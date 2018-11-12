/*
 * Copyright 2007-2017 the original author or authors.
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

import net.ymate.platform.core.util.ThreadUtils;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

/**
 * @param <CODEC>    编码器类型
 * @param <LISTENER> 监听器类型
 * @param <SESSION>  会话类型
 * @author 刘镇 (suninformation@163.com) on 15/11/15 下午10:10
 * @version 1.0
 */
public abstract class AbstractEventGroup<CODEC extends ICodec, LISTENER extends IListener<SESSION>, SESSION extends ISession>
        implements IEventGroup<CODEC, LISTENER, SESSION> {

    private String __name;

    private ExecutorService __executorService;

    private final CODEC __codec;
    private final LISTENER __listener;

    private SESSION __session;

    private int __bufferSize = 4096;

    private int __executorCount = Runtime.getRuntime().availableProcessors();

    private long __keepAliveTime;

    private int __threadMaxPoolSize;

    private int __threadQueueSize;

    private int __connectionTimeout = 5000;

    private boolean __isStarted = false;

    private boolean __isServer;

    public AbstractEventGroup(IServerCfg cfg, LISTENER listener, CODEC codec) throws IOException {
        __name = cfg.getServerName();
        if (cfg.getBufferSize() > 0) {
            __bufferSize = cfg.getBufferSize();
        }
        __executorCount = cfg.getExecutorCount();
        __keepAliveTime = cfg.getKeepAliveTime();
        __threadMaxPoolSize = cfg.getThreadMaxPoolSize();
        __threadQueueSize = cfg.getThreadQueueSize();
        //
        __codec = codec;
        __listener = listener;
        //
        __isServer = true;
    }

    public AbstractEventGroup(IClientCfg cfg, LISTENER listener, CODEC codec) throws IOException {
        __name = cfg.getClientName();
        if (cfg.getBufferSize() > 0) {
            __bufferSize = cfg.getBufferSize();
        }
        //
        __executorCount = __threadMaxPoolSize = cfg.getExecutorCount();
        __threadQueueSize = Integer.MAX_VALUE;
        //
        __codec = codec;
        __listener = listener;
        //
        if (cfg.getConnectionTimeout() > 0) {
            __connectionTimeout = cfg.getConnectionTimeout();
        }
        __session = __doSessionCreate(cfg);
    }

    @Override
    public void start() throws IOException {
        if (__isStarted) {
            return;
        }
        __executorService = ThreadUtils.newThreadExecutor(__executorCount, __threadMaxPoolSize, __keepAliveTime, __threadQueueSize, ThreadUtils.createFactory("serv-pool-"));
        //
        __isStarted = true;
    }

    @Override
    public void stop() throws IOException {
        if (!__isStarted) {
            return;
        }
        __isStarted = false;
        if (__session != null) {
            __session.close();
        }
        __executorService.shutdown();
    }

    @Override
    public void close() throws IOException {
        stop();
    }

    protected abstract SESSION __doSessionCreate(IClientCfg cfg) throws IOException;

    @Override
    public CODEC codec() {
        return __codec;
    }

    @Override
    public LISTENER listener() {
        return __listener;
    }

    @Override
    public SESSION session() {
        return __session;
    }

    protected boolean isServer() {
        return __isServer;
    }

    @Override
    public boolean isStarted() {
        return __isStarted;
    }

    @Override
    public String name() {
        return __name;
    }

    @Override
    public void name(String name) {
        __name = name;
    }

    @Override
    public int bufferSize() {
        return __bufferSize;
    }

    @Override
    public int executorCount() {
        return __executorCount;
    }

    @Override
    public int connectionTimeout() {
        return __connectionTimeout;
    }

    @Override
    public ExecutorService executorService() {
        return __executorService;
    }
}
