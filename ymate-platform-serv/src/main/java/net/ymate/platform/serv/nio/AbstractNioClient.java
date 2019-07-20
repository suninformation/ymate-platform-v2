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

import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.serv.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

/**
 * @param <LISTENER> 监听器类型
 * @author 刘镇 (suninformation@163.com) on 15/11/19 下午4:55
 */
public abstract class AbstractNioClient<LISTENER extends IListener<INioSession>> implements IClient<LISTENER, INioCodec> {

    private static final Log LOG = LogFactory.getLog(AbstractNioClient.class);

    private IClientCfg clientCfg;

    private INioEventGroup<LISTENER> eventGroup;

    private LISTENER listener;

    private INioCodec codec;

    private IReconnectService reconnectService;

    private IHeartbeatService<?> heartbeatService;

    private boolean closing;

    private void startServices() {
        if (reconnectService != null && reconnectService.isInitialized()) {
            reconnectService.start();
        }
        if (heartbeatService != null && heartbeatService.isInitialized()) {
            heartbeatService.start();
        }
    }

    private void stopServices() {
        if (reconnectService != null && reconnectService.isStarted()) {
            try {
                reconnectService.close();
            } catch (IOException e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("An exception occurred while stopping reconnect service: ", RuntimeUtils.unwrapThrow(e));
                }
            }
        }
        if (heartbeatService != null && heartbeatService.isStarted()) {
            try {
                heartbeatService.close();
            } catch (IOException e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("An exception occurred while stopping heartbeat service: ", RuntimeUtils.unwrapThrow(e));
                }
            }
        }
    }

    /**
     * 由子类实现创建多路复用通道事件处理器逻辑
     *
     * @param clientCfg 客户端配置对象
     * @param listener  事件监听器
     * @param codec     编解码器
     * @return 返回多路复用通道事件处理器
     * @throws IOException 可能产生的I/O异常
     */
    protected abstract INioEventGroup<LISTENER> buildEventGroup(IClientCfg clientCfg, LISTENER listener, INioCodec codec) throws IOException;

    @Override
    public void initialize(IClientCfg clientCfg, LISTENER listener, INioCodec codec, IReconnectService reconnectService, IHeartbeatService<?> heartbeatService) {
        this.clientCfg = clientCfg;
        this.listener = listener;
        this.codec = codec;
        this.codec.initialize(this.clientCfg.getCharset());
        //
        this.reconnectService = reconnectService;
        this.heartbeatService = heartbeatService;
    }

    @Override
    public void connect() throws IOException {
        if (eventGroup != null && eventGroup.session() != null) {
            if (eventGroup.session().isConnected() || eventGroup.session().isNew()) {
                return;
            }
        }
        eventGroup = buildEventGroup(clientCfg, listener, codec);
        //
        if (LOG.isInfoEnabled()) {
            LOG.info(String.format("%s [%s] connecting to %s:%d", getClass().getSimpleName(), eventGroup.name(), clientCfg.getRemoteHost(), clientCfg.getPort()));
        }
        //
        eventGroup.start();
        //
        startServices();
    }

    @Override
    public void reconnect() throws IOException {
        if (!isClosing() && !isConnected()) {
            eventGroup.close();
            eventGroup = buildEventGroup(clientCfg, listener, codec);
            //
            if (LOG.isInfoEnabled()) {
                LOG.info(String.format("%s [%s] reconnecting to %s:%d", getClass().getSimpleName(), eventGroup.name(), clientCfg.getRemoteHost(), clientCfg.getPort()));
            }
            //
            eventGroup.start();
        }
    }

    @Override
    public boolean isConnected() {
        return eventGroup != null && eventGroup.session() != null && eventGroup.session().isConnected();
    }

    @Override
    public boolean isClosing() {
        return closing;
    }

    @Override
    public IClientCfg clientCfg() {
        return clientCfg;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends LISTENER> T listener() {
        return (T) listener;
    }

    @Override
    public void send(Object message) throws IOException {
        eventGroup.session().send(message);
    }

    @Override
    public void close() throws IOException {
        if (LOG.isInfoEnabled()) {
            LOG.info(String.format("%s [%s] closing....", getClass().getSimpleName(), eventGroup.name()));
        }
        //
        closing = true;
        stopServices();
        //
        if (eventGroup != null) {
            eventGroup.close();
        }
    }

    @Override
    public void touch() {
        eventGroup.session().touch();
    }

    @Override
    public long lastTouchTime() {
        return eventGroup.session().lastTouchTime();
    }
}
