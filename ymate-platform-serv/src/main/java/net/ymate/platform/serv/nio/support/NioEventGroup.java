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
package net.ymate.platform.serv.nio.support;

import net.ymate.platform.serv.IClientCfg;
import net.ymate.platform.serv.IListener;
import net.ymate.platform.serv.IServerCfg;
import net.ymate.platform.serv.nio.AbstractNioEventGroup;
import net.ymate.platform.serv.nio.INioCodec;
import net.ymate.platform.serv.nio.INioEventGroup;
import net.ymate.platform.serv.nio.INioSession;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @param <LISTENER> 监听器类型
 * @author 刘镇 (suninformation@163.com) on 15/11/15 下午6:54
 */
public class NioEventGroup<LISTENER extends IListener<INioSession>> extends AbstractNioEventGroup<INioCodec, LISTENER, INioSession> implements INioEventGroup<LISTENER> {

    private SelectableChannel selectableChannel;

    private int selectorCount = 1;

    private List<NioEventProcessor<LISTENER>> eventProcessors;

    private final AtomicInteger handlerCount = new AtomicInteger(0);

    public NioEventGroup(IServerCfg cfg, LISTENER listener, INioCodec codec) throws IOException {
        super(cfg, listener, codec);
        //
        selectableChannel = channelCreate(cfg);
        if (cfg.getSelectorCount() > 0) {
            selectorCount = cfg.getSelectorCount();
        }
    }

    public NioEventGroup(IClientCfg cfg, LISTENER listener, INioCodec codec) throws IOException {
        super(cfg, listener, codec);
    }

    protected SelectableChannel channelCreate(IServerCfg cfg) throws IOException {
        ServerSocketChannel socketChannel = ServerSocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.socket().bind(new InetSocketAddress(cfg.getServerHost(), cfg.getPort()));
        return socketChannel;
    }

    @Override
    protected INioSession sessionCreate(IClientCfg cfg) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.socket().setReuseAddress(true);
        socketChannel.connect(new InetSocketAddress(cfg.getRemoteHost(), cfg.getPort()));
        selectableChannel = socketChannel;
        return new NioSession<>(this, socketChannel);
    }

    @Override
    public synchronized void start() throws IOException {
        super.start();
        //
        eventProcessors = initProcessors();
        registerEvent();
    }

    protected SelectableChannel channel() {
        return selectableChannel;
    }

    protected void channel(SelectableChannel channel) {
        selectableChannel = channel;
    }

    protected int selectorCount() {
        return selectorCount;
    }

    protected List<NioEventProcessor<LISTENER>> processors() {
        return eventProcessors;
    }

    protected String buildProcessorName() {
        return StringUtils.capitalize(name()).concat(isServer() ? "Server" : "Client").concat("-NioEventProcessor-");
    }

    protected List<NioEventProcessor<LISTENER>> initProcessors() throws IOException {
        List<NioEventProcessor<LISTENER>> newEventProcessors = new ArrayList<>(selectorCount);
        for (int idx = 0; idx < selectorCount; idx++) {
            NioEventProcessor<LISTENER> eventProcessor = new NioEventProcessor<>(this, buildProcessorName() + idx);
            eventProcessor.start();
            newEventProcessors.add(eventProcessor);
        }
        return newEventProcessors;
    }

    protected void registerEvent() throws IOException {
        if (isServer()) {
            processor().registerEvent(selectableChannel, SelectionKey.OP_ACCEPT, null);
        } else {
            processor().registerEvent(selectableChannel, SelectionKey.OP_CONNECT, session());
            if (connectionTimeout() > 0) {
                session().connectSync(connectionTimeout());
            }
        }
    }

    @Override
    public void stop() throws IOException {
        for (NioEventProcessor<?> processor : eventProcessors) {
            processor.interrupt();
        }
        if (selectableChannel != null) {
            selectableChannel.close();
            selectableChannel = null;
        }
        //
        super.stop();
    }

    @Override
    public NioEventProcessor<LISTENER> processor(SelectionKey key) {
        return eventProcessors.stream().filter(processor -> key.selector() == processor.selector()).findFirst().orElse(null);
    }

    @Override
    public NioEventProcessor<LISTENER> processor() {
        int nextIdx = handlerCount.getAndIncrement() % selectorCount;
        if (nextIdx < 0) {
            handlerCount.set(0);
            nextIdx = 0;
        }
        return eventProcessors.get(nextIdx);
    }
}
