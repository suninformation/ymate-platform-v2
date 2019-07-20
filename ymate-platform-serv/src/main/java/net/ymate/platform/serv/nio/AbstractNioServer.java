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

import net.ymate.platform.serv.IListener;
import net.ymate.platform.serv.IServer;
import net.ymate.platform.serv.IServerCfg;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

/**
 * @param <LISTENER> 监听器类型
 * @author 刘镇 (suninformation@163.com) on 2018/11/15 4:49 PM
 */
public abstract class AbstractNioServer<LISTENER extends IListener<INioSession>> implements IServer<LISTENER, INioCodec> {

    private static final Log LOG = LogFactory.getLog(AbstractNioServer.class);

    private IServerCfg serverCfg;

    private INioEventGroup<LISTENER> eventGroup;

    private LISTENER listener;

    private INioCodec codec;

    private boolean started;

    /**
     * 由子类实现创建多路复用通道事件处理器逻辑
     *
     * @param serverCfg 服务端配置对象
     * @param listener  事件监听器
     * @param codec     编解码器
     * @return 返回多路复用通道事件处理器
     * @throws IOException 可能产生的I/O异常
     */
    protected abstract INioEventGroup<LISTENER> buildEventGroup(IServerCfg serverCfg, LISTENER listener, INioCodec codec) throws IOException;

    @Override
    public void initialize(IServerCfg serverCfg, LISTENER listener, INioCodec codec) {
        this.serverCfg = serverCfg;
        //
        this.listener = listener;
        this.codec = codec;
        this.codec.initialize(this.serverCfg.getCharset());
    }

    @Override
    public void start() throws IOException {
        if (!started) {
            started = true;
            eventGroup = buildEventGroup(serverCfg, listener, codec);
            eventGroup.start();
            //
            if (LOG.isInfoEnabled()) {
                LOG.info(String.format("%s [%s] started at %s:%d", getClass().getSimpleName(), eventGroup.name(), serverCfg.getServerHost(), serverCfg.getPort()));
            }
        }
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    @Override
    public IServerCfg serverCfg() {
        return serverCfg;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends LISTENER> T listener() {
        return (T) listener;
    }

    @Override
    public void close() throws IOException {
        if (started) {
            if (LOG.isInfoEnabled()) {
                LOG.info(String.format("%s [%s] closing....", getClass().getSimpleName(), eventGroup.name()));
            }
            started = false;
            eventGroup.close();
        }
    }
}
