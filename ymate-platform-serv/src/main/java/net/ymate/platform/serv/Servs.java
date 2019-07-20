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

import net.ymate.platform.serv.nio.INioCodec;
import net.ymate.platform.serv.nio.client.NioClient;
import net.ymate.platform.serv.nio.client.NioClientListener;
import net.ymate.platform.serv.nio.datagram.AbstractNioUdpListener;
import net.ymate.platform.serv.nio.datagram.NioUdpClient;
import net.ymate.platform.serv.nio.datagram.NioUdpServer;
import net.ymate.platform.serv.nio.server.NioServer;
import net.ymate.platform.serv.nio.server.NioServerListener;

/**
 * 服务管理器
 *
 * @author 刘镇 (suninformation@163.com) on 15/10/15 上午10:22
 */
public final class Servs {

    public static <LISTENER extends NioServerListener, CODEC extends INioCodec> NioServer createServer(IServerCfg serverCfg, CODEC codec, LISTENER listener) {
        NioServer nioServer = new NioServer();
        nioServer.initialize(serverCfg, listener, codec);
        return nioServer;
    }

    public static <LISTENER extends AbstractNioUdpListener, CODEC extends INioCodec> NioUdpServer createUdpServer(IServerCfg serverCfg, CODEC codec, LISTENER listener) {
        NioUdpServer udpServer = new NioUdpServer();
        udpServer.initialize(serverCfg, listener, codec);
        return udpServer;
    }

    public static <LISTENER extends NioClientListener, CODEC extends INioCodec> NioClient createClient(IClientCfg clientCfg, CODEC codec, IReconnectService reconnect, IHeartbeatService<?> heartbeat, LISTENER listener) throws Exception {
        NioClient nioClient = new NioClient();
        if (reconnect != null && !reconnect.isInitialized()) {
            reconnect.initialize(nioClient);
        }
        if (heartbeat != null && !heartbeat.isInitialized()) {
            heartbeat.initialize(nioClient);
        }
        nioClient.initialize(clientCfg, listener, codec, reconnect, heartbeat);
        return nioClient;
    }

    public static <LISTENER extends AbstractNioUdpListener, CODEC extends INioCodec> NioUdpClient createUdpClient(IClientCfg clientCfg, CODEC codec, IHeartbeatService<?> heartbeat, LISTENER listener) throws Exception {
        NioUdpClient udpClient = new NioUdpClient();
        if (heartbeat != null && !heartbeat.isInitialized()) {
            heartbeat.initialize(udpClient);
        }
        udpClient.initialize(clientCfg, listener, codec, null, heartbeat);
        return udpClient;
    }
}
