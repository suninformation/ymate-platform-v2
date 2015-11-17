/*
 * Copyright 2007-2016 the original author or authors.
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
import net.ymate.platform.serv.IClient;
import net.ymate.platform.serv.IClientCfg;
import net.ymate.platform.serv.IServModuleCfg;
import net.ymate.platform.serv.nio.INioClientCfg;
import net.ymate.platform.serv.nio.INioCodec;
import net.ymate.platform.serv.nio.INioSession;
import net.ymate.platform.serv.nio.client.NioClientCfg;
import net.ymate.platform.serv.nio.support.NioEventGroup;
import net.ymate.platform.serv.nio.support.NioSession;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/17 下午3:04
 * @version 1.0
 */
public class NioUdpClient implements IClient<NioUdpListener, INioCodec> {

    private final Log _LOG = LogFactory.getLog(NioUdpClient.class);

    protected INioClientCfg __clientCfg;

    protected NioEventGroup<NioUdpListener> __eventGroup;

    protected NioUdpListener __listener;

    protected INioCodec __codec;

    protected boolean __isStarted;

    public void init(IServModuleCfg moduleCfg, String clientName, NioUdpListener listener, INioCodec codec) {
        __clientCfg = new NioClientCfg(moduleCfg, clientName);
        //
        __listener = listener;
        __codec = codec;
        __codec.init(__clientCfg.getCharset());
    }

    public void connect() throws IOException {
        if (!__isStarted) {
            __isStarted = true;
            __eventGroup = new NioEventGroup<NioUdpListener>(__clientCfg, __listener, __codec) {
                @Override
                protected INioSession __doSessionCreate(IClientCfg cfg) throws IOException {
                    DatagramChannel _channel = DatagramChannel.open();
                    _channel.configureBlocking(false);
                    _channel.socket().connect(new InetSocketAddress(cfg.getRemoteHost(), cfg.getPort()));
                    __channel = _channel;
                    return new NioSession<NioUdpListener>(this, __channel) {
                        @Override
                        protected int __doChannelRead(ByteBuffer buffer) throws IOException {
                            SocketAddress _address = ((DatagramChannel) __channel).receive(buffer);
                            if (_address != null) {
                                return __buffer.remaining();
                            }
                            return 0;
                        }

                        @Override
                        protected int __doChannelWrite(ByteBuffer buffer) throws IOException {
                            SocketAddress _address = ((DatagramChannel) __channel).socket().getRemoteSocketAddress();
                            if (_address != null) {
                                return ((DatagramChannel) __channel).send(buffer, _address);
                            }
                            buffer.reset();
                            return 0;
                        }
                    };
                }

                @Override
                public synchronized void start() throws IOException {
                    _LOG.info("UdpClient [" + __eventGroup.name() + "] connecting to " + __clientCfg.getRemoteHost() + ":" + __clientCfg.getPort());
                    super.start();
                }

                @Override
                protected String __doBuildProcessorName() {
                    return StringUtils.capitalize(name()).concat("UdpClient-NioEventProcessor-");
                }

                @Override
                protected void __doStart() throws IOException {
                    processor().registerEvent(__channel, SelectionKey.OP_READ, session());
                }
            };
            __eventGroup.start();
        }
    }

    public boolean isConnected() {
        return __isStarted;
    }

    public void send(Object message) throws IOException {
        if (!isConnected()) {
            throw RuntimeUtils.makeRuntimeThrow("Client was not connected");
        }
        __eventGroup.session().send(message);
    }

    public void close() throws IOException {
        if (__isStarted) {
            __isStarted = false;
            __eventGroup.close();
        }
    }
}
