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
package net.ymate.platform.serv.nio;

import net.ymate.platform.commons.impl.DefaultThreadFactory;
import net.ymate.platform.commons.util.ThreadUtils;
import net.ymate.platform.serv.IClientCfg;
import net.ymate.platform.serv.IListener;
import net.ymate.platform.serv.IServerCfg;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

/**
 * @param <CODEC>    编码器类型
 * @param <LISTENER> 监听器类型
 * @param <SESSION>  会话类型
 * @author 刘镇 (suninformation@163.com) on 15/11/15 下午10:10
 */
public abstract class AbstractNioEventGroup<CODEC extends INioCodec, LISTENER extends IListener<INioSession>, SESSION extends INioSession> implements INioEventGroup<LISTENER> {

    private String name;

    private ExecutorService executorService;

    private final CODEC codec;

    private final LISTENER listener;

    private SESSION session;

    private int bufferSize = 4096;

    private int executorCount;

    private long keepAliveTime;

    private int threadMaxPoolSize = 200;

    private int threadQueueSize;

    private int connectionTimeout = 30;

    private boolean started;

    private boolean server;

    public AbstractNioEventGroup(IServerCfg cfg, LISTENER listener, CODEC codec) {
        name = cfg.getServerName();
        if (cfg.getBufferSize() > 0) {
            bufferSize = cfg.getBufferSize();
        }
        executorCount = cfg.getExecutorCount();
        if (executorCount <= 0) {
            executorCount = Runtime.getRuntime().availableProcessors();
        }

        keepAliveTime = cfg.getKeepAliveTime();
        if (cfg.getThreadMaxPoolSize() > 0) {
            threadMaxPoolSize = cfg.getThreadMaxPoolSize();
        }
        threadQueueSize = cfg.getThreadQueueSize();
        if (threadQueueSize <= 0) {
            threadQueueSize = 1024;
        }
        //
        this.codec = codec;
        this.listener = listener;
        //
        server = true;
    }

    public AbstractNioEventGroup(IClientCfg cfg, LISTENER listener, CODEC codec) throws IOException {
        name = cfg.getClientName();
        if (cfg.getBufferSize() > 0) {
            bufferSize = cfg.getBufferSize();
        }
        //
        executorCount = threadMaxPoolSize = cfg.getExecutorCount();
        threadQueueSize = Integer.MAX_VALUE;
        //
        this.codec = codec;
        this.listener = listener;
        //
        if (cfg.getConnectionTimeout() > 0) {
            connectionTimeout = cfg.getConnectionTimeout();
        }
        session = sessionCreate(cfg);
    }

    @Override
    public void start() throws IOException {
        if (started) {
            return;
        }
        executorService = ThreadUtils.newThreadExecutor(executorCount, threadMaxPoolSize, keepAliveTime, threadQueueSize, DefaultThreadFactory.create("EventGroup-"));
        //
        started = true;
    }

    @Override
    public void stop() throws IOException {
        if (!started) {
            return;
        }
        started = false;
        if (session != null) {
            session.close();
        }
        executorService.shutdown();
    }

    @Override
    public void close() throws IOException {
        stop();
    }

    /**
     * 构建会话对象
     *
     * @param cfg 客户端配置
     * @return 返回创建的会话对象实例
     * @throws IOException 可能产生的IO异常
     */
    protected abstract SESSION sessionCreate(IClientCfg cfg) throws IOException;

    @Override
    public CODEC codec() {
        return codec;
    }

    @Override
    public LISTENER listener() {
        return listener;
    }

    @Override
    public SESSION session() {
        return session;
    }

    @Override
    public boolean isServer() {
        return server;
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void name(String name) {
        this.name = name;
    }

    @Override
    public int bufferSize() {
        return bufferSize;
    }

    @Override
    public int executorCount() {
        return executorCount;
    }

    @Override
    public int connectionTimeout() {
        return connectionTimeout;
    }

    @Override
    public ExecutorService executorService() {
        return executorService;
    }
}
