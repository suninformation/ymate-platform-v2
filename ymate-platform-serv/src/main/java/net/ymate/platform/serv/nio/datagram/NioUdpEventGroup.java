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
package net.ymate.platform.serv.nio.datagram;

import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.serv.IClientCfg;
import net.ymate.platform.serv.IServerCfg;
import net.ymate.platform.serv.nio.INioCodec;
import net.ymate.platform.serv.nio.INioSession;
import net.ymate.platform.serv.nio.support.NioEventGroup;
import net.ymate.platform.serv.nio.support.NioEventProcessor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/11/16 3:29 AM
 */
public class NioUdpEventGroup extends NioEventGroup<AbstractNioUdpListener> {

    private static final Log LOG = LogFactory.getLog(NioUdpEventGroup.class);

    public NioUdpEventGroup(IServerCfg cfg, AbstractNioUdpListener listener, INioCodec codec) throws IOException {
        super(cfg, listener, codec);
    }

    public NioUdpEventGroup(IClientCfg cfg, AbstractNioUdpListener listener, INioCodec codec) throws IOException {
        super(cfg, listener, codec);
    }

    @Override
    protected SelectableChannel channelCreate(IServerCfg cfg) throws IOException {
        DatagramChannel channel = DatagramChannel.open();
        channel.configureBlocking(false);
        channel.socket().bind(new InetSocketAddress(cfg.getServerHost(), cfg.getPort()));
        return channel;
    }

    @Override
    protected INioSession sessionCreate(IClientCfg cfg) throws IOException {
        DatagramChannel channel = DatagramChannel.open();
        channel.configureBlocking(false);
        channel.socket().connect(new InetSocketAddress(cfg.getRemoteHost(), cfg.getPort()));
        channel(channel);
        return new NioUdpSession(this, channel, (InetSocketAddress) channel.socket().getRemoteSocketAddress());
    }

    private NioEventProcessor<AbstractNioUdpListener> eventProcessorCreate(String name) throws IOException {
        return new NioEventProcessor<AbstractNioUdpListener>(this, name) {
            @Override
            protected void onExceptionEvent(SelectionKey key, final Throwable e) {
                final INioSession session = (INioSession) key.attachment();
                if (session != null) {
                    executorService().submit(() -> {
                        try {
                            listener().onExceptionCaught(e, session);
                        } catch (IOException ex) {
                            if (LOG.isErrorEnabled()) {
                                LOG.error(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(ex));
                            }
                        }
                    });
                } else if (LOG.isErrorEnabled()) {
                    LOG.error(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
                }
            }
        };
    }

    @Override
    protected List<NioEventProcessor<AbstractNioUdpListener>> initProcessors() throws IOException {
        List<NioEventProcessor<AbstractNioUdpListener>> newEventProcessors = new ArrayList<>(selectorCount());
        for (int idx = 0; idx < selectorCount(); idx++) {
            NioEventProcessor<AbstractNioUdpListener> eventProcessor = eventProcessorCreate(buildThreadNamePrefix("-NioUdpEventProcessor-") + idx);
            eventProcessor.start();
            newEventProcessors.add(eventProcessor);
        }
        return newEventProcessors;
    }

    @Override
    protected void registerEvent() throws IOException {
        for (NioEventProcessor<AbstractNioUdpListener> processor : processors()) {
            processor.registerEvent(channel(), SelectionKey.OP_READ, isServer() ? new NioUdpSession(this, (DatagramChannel) channel()) : session());
        }
    }
}
