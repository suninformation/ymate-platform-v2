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
package net.ymate.platform.serv.nio.support;

import net.ymate.platform.serv.AbstractEventGroup;
import net.ymate.platform.serv.IClientCfg;
import net.ymate.platform.serv.IListener;
import net.ymate.platform.serv.IServerCfg;
import net.ymate.platform.serv.nio.INioCodec;
import net.ymate.platform.serv.nio.INioEventGroup;
import net.ymate.platform.serv.nio.INioSession;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/15 下午6:54
 * @version 1.0
 */
public class NioEventGroup<LISTENER extends IListener<INioSession>> extends AbstractEventGroup<INioCodec, LISTENER, INioSession> implements INioEventGroup<LISTENER> {

    private SelectableChannel __channel;

    private int __selectorCount = 1;

    private NioEventProcessor[] __processors;

    private AtomicInteger __handlerCount = new AtomicInteger(0);

    public NioEventGroup(IServerCfg cfg, LISTENER listener, INioCodec codec) throws IOException {
        super(cfg, listener, codec);
        //
        __channel = __doChannelCreate(cfg);
        __selectorCount = cfg.getSelectorCount();
    }

    public NioEventGroup(IClientCfg cfg, LISTENER listener, INioCodec codec) throws IOException {
        super(cfg, listener, codec);
    }

    protected SelectableChannel __doChannelCreate(IServerCfg cfg) throws IOException {
        ServerSocketChannel _channel = ServerSocketChannel.open();
        _channel.configureBlocking(false);
        _channel.socket().bind(new InetSocketAddress(cfg.getServerHost(), cfg.getPort()));
        return _channel;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected INioSession __doSessionCreate(IClientCfg cfg) throws IOException {
        SocketChannel _channel = SocketChannel.open();
        _channel.configureBlocking(false);
        _channel.socket().setReuseAddress(true);
        _channel.connect(new InetSocketAddress(cfg.getRemoteHost(), cfg.getPort()));
        __channel = _channel;
        return new NioSession(this, _channel);
    }

    @Override
    public synchronized void start() throws IOException {
        super.start();
        //
        __processors = __doInitProcessors();
        __doRegisterEvent();
    }

    protected SelectableChannel channel() {
        return __channel;
    }

    protected void channel(SelectableChannel channel) {
        __channel = channel;
    }

    protected int selectorCount() {
        return __selectorCount;
    }

    protected NioEventProcessor[] processors() {
        return __processors;
    }

    protected String __doBuildProcessorName() {
        return StringUtils.capitalize(name()).concat(isServer() ? "Server" : "Client").concat("-NioEventProcessor-");
    }

    protected NioEventProcessor[] __doInitProcessors() throws IOException {
        NioEventProcessor[] _processors = new NioEventProcessor[__selectorCount];
        for (int _idx = 0; _idx < __selectorCount; _idx++) {
            _processors[_idx] = new NioEventProcessor<LISTENER>(this, __doBuildProcessorName() + _idx);
            _processors[_idx].start();
        }
        return _processors;
    }

    protected void __doRegisterEvent() throws IOException {
        if (isServer()) {
            processor().registerEvent(__channel, SelectionKey.OP_ACCEPT, null);
        } else {
            processor().registerEvent(__channel, SelectionKey.OP_CONNECT, session());
            if (connectionTimeout() > 0) {
                session().connectSync(connectionTimeout());
            }
        }
    }

    @Override
    public void stop() throws IOException {
        for (NioEventProcessor _processor : __processors) {
            _processor.interrupt();
        }
        __channel.close();
        __channel = null;
        //
        super.stop();
    }

    @Override
    public NioEventProcessor processor(SelectionKey key) {
        for (NioEventProcessor _processor : __processors) {
            if (key.selector() == _processor.selector()) {
                return _processor;
            }
        }
        return null;
    }

    @Override
    public NioEventProcessor processor() {
        int _nextIdx = __handlerCount.getAndIncrement() % __selectorCount;
        if (_nextIdx < 0) {
            __handlerCount.set(0);
            _nextIdx = 0;
        }
        return __processors[_nextIdx];
    }
}
