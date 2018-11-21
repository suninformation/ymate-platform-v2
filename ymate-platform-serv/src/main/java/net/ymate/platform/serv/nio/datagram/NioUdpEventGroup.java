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
package net.ymate.platform.serv.nio.datagram;

import net.ymate.platform.core.util.RuntimeUtils;
import net.ymate.platform.serv.IClientCfg;
import net.ymate.platform.serv.IServerCfg;
import net.ymate.platform.serv.nio.INioCodec;
import net.ymate.platform.serv.nio.INioSession;
import net.ymate.platform.serv.nio.support.NioEventGroup;
import net.ymate.platform.serv.nio.support.NioEventProcessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/11/16 3:29 AM
 * @version 1.0
 */
public class NioUdpEventGroup extends NioEventGroup<NioUdpListener> {

    private static final Log _LOG = LogFactory.getLog(NioUdpEventGroup.class);

    public NioUdpEventGroup(IServerCfg cfg, NioUdpListener listener, INioCodec codec) throws IOException {
        super(cfg, listener, codec);
    }

    public NioUdpEventGroup(IClientCfg cfg, NioUdpListener listener, INioCodec codec) throws IOException {
        super(cfg, listener, codec);
    }

    @Override
    protected SelectableChannel __doChannelCreate(IServerCfg cfg) throws IOException {
        DatagramChannel _channel = DatagramChannel.open();
        _channel.configureBlocking(false);
        _channel.socket().bind(new InetSocketAddress(cfg.getServerHost(), cfg.getPort()));
        return _channel;
    }

    @Override
    protected INioSession __doSessionCreate(IClientCfg cfg) throws IOException {
        final DatagramChannel _channel = DatagramChannel.open();
        _channel.configureBlocking(false);
        _channel.socket().connect(new InetSocketAddress(cfg.getRemoteHost(), cfg.getPort()));
        channel(_channel);
        return new NioUdpSession(this, _channel, (InetSocketAddress) _channel.socket().getRemoteSocketAddress());
    }

    private NioEventProcessor<NioUdpListener> __doEventProcessorCreate(String name) throws IOException {
        return new NioEventProcessor<NioUdpListener>(this, name) {
            @Override
            protected void onExceptionEvent(SelectionKey key, final Throwable e) {
                final INioSession _session = (INioSession) key.attachment();
                if (_session != null) {
                    executorService().submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                listener().onExceptionCaught(e, _session);
                            } catch (IOException ex) {
                                _LOG.error(e.getMessage(), RuntimeUtils.unwrapThrow(ex));
                            }
                        }
                    });
                } else {
                    _LOG.error(RuntimeUtils.unwrapThrow(e));
                }
            }
        };
    }

    @Override
    protected NioEventProcessor[] __doInitProcessors() throws IOException {
        NioEventProcessor[] _processors = new NioEventProcessor[selectorCount()];
        for (int _idx = 0; _idx < selectorCount(); _idx++) {
            _processors[_idx] = __doEventProcessorCreate(__doBuildProcessorName() + _idx);
            _processors[_idx].start();
        }
        return _processors;
    }

    @Override
    protected void __doRegisterEvent() throws IOException {
        for (NioEventProcessor _processor : processors()) {
            _processor.registerEvent(channel(), SelectionKey.OP_READ, isServer() ? new NioUdpSession(this, (DatagramChannel) channel()) : session());
        }
    }
}
