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

    // TCP Server Builder

    /**
     * TCP 服务端构建器
     *
     * @param <LISTENER> 监听器类型
     * @param <CODEC>    编解码器类型
     * @author 刘镇 (suninformation@163.com) on 2026/8/24
     * @since 2.1.4
     */
    public static class ServerBuilder<LISTENER extends NioServerListener, CODEC extends INioCodec> {
        private IServerCfg serverCfg;
        private CODEC codec;
        private LISTENER listener;

        public ServerBuilder<LISTENER, CODEC> config(IServerCfg cfg) {
            this.serverCfg = cfg;
            return this;
        }

        public ServerBuilder<LISTENER, CODEC> codec(CODEC codec) {
            this.codec = codec;
            return this;
        }

        public ServerBuilder<LISTENER, CODEC> listener(LISTENER listener) {
            this.listener = listener;
            return this;
        }

        public NioServer build() {
            return createServer(serverCfg, codec, listener);
        }
    }

    /**
     * 创建 TCP 服务端构建器
     *
     * @param <LISTENER> 监听器类型
     * @param <CODEC>    编解码器类型
     * @return 返回 TCP 服务端构建器实例
     * @since 2.1.4
     */
    public static <LISTENER extends NioServerListener, CODEC extends INioCodec> ServerBuilder<LISTENER, CODEC> createServer() {
        return new ServerBuilder<>();
    }

    // UDP Server Builder

    /**
     * UDP 服务端构建器
     *
     * @param <LISTENER> 监听器类型
     * @param <CODEC>    编解码器类型
     * @author 刘镇 (suninformation@163.com) on 2026/8/24
     * @since 2.1.4
     */
    public static class UdpServerBuilder<LISTENER extends AbstractNioUdpListener, CODEC extends INioCodec> {
        private IServerCfg serverCfg;
        private CODEC codec;
        private LISTENER listener;

        public UdpServerBuilder<LISTENER, CODEC> config(IServerCfg cfg) {
            this.serverCfg = cfg;
            return this;
        }

        public UdpServerBuilder<LISTENER, CODEC> codec(CODEC codec) {
            this.codec = codec;
            return this;
        }

        public UdpServerBuilder<LISTENER, CODEC> listener(LISTENER listener) {
            this.listener = listener;
            return this;
        }

        public NioUdpServer build() {
            return createUdpServer(serverCfg, codec, listener);
        }
    }

    /**
     * 创建 UDP 服务端构建器
     *
     * @param <LISTENER> 监听器类型
     * @param <CODEC>    编解码器类型
     * @return 返回 UDP 服务端构建器实例
     * @since 2.1.4
     */
    public static <LISTENER extends AbstractNioUdpListener, CODEC extends INioCodec> UdpServerBuilder<LISTENER, CODEC> createUdpServer() {
        return new UdpServerBuilder<>();
    }

    //TCP Client Builder

    /**
     * TCP 客户端构建器
     *
     * @param <LISTENER> 监听器类型
     * @param <CODEC>    编解码器类型
     * @author 刘镇 (suninformation@163.com) on 2026/8/24
     * @since 2.1.4
     */
    public static class ClientBuilder<LISTENER extends NioClientListener, CODEC extends INioCodec> {
        private IClientCfg clientCfg;
        private CODEC codec;
        private LISTENER listener;
        private IReconnectService reconnectService;
        private IHeartbeatService<?> heartbeatService;

        public ClientBuilder<LISTENER, CODEC> config(IClientCfg cfg) {
            this.clientCfg = cfg;
            return this;
        }

        public ClientBuilder<LISTENER, CODEC> codec(CODEC codec) {
            this.codec = codec;
            return this;
        }

        public ClientBuilder<LISTENER, CODEC> listener(LISTENER listener) {
            this.listener = listener;
            return this;
        }

        public ClientBuilder<LISTENER, CODEC> reconnect(IReconnectService svc) {
            this.reconnectService = svc;
            return this;
        }

        public ClientBuilder<LISTENER, CODEC> heartbeat(IHeartbeatService<?> svc) {
            this.heartbeatService = svc;
            return this;
        }

        public NioClient build() throws Exception {
            return createClient(clientCfg, codec, reconnectService, heartbeatService, listener);
        }
    }

    /**
     * 创建 TCP 客户端构建器
     *
     * @param <LISTENER> 监听器类型
     * @param <CODEC>    编解码器类型
     * @return 返回 TCP 客户端构建器实例
     * @since 2.1.4
     */
    public static <LISTENER extends NioClientListener, CODEC extends INioCodec> ClientBuilder<LISTENER, CODEC> createClient() {
        return new ClientBuilder<>();
    }

    // UDP Client Builder

    /**
     * UDP 客户端构建器
     *
     * @param <LISTENER> 监听器类型
     * @param <CODEC>    编解码器类型
     * @author 刘镇 (suninformation@163.com) on 2026/8/24
     * @since 2.1.4
     */
    public static class UdpClientBuilder<LISTENER extends AbstractNioUdpListener, CODEC extends INioCodec> {
        private IClientCfg clientCfg;
        private CODEC codec;
        private LISTENER listener;
        private IHeartbeatService<?> heartbeatService;

        public UdpClientBuilder<LISTENER, CODEC> config(IClientCfg cfg) {
            this.clientCfg = cfg;
            return this;
        }

        public UdpClientBuilder<LISTENER, CODEC> codec(CODEC codec) {
            this.codec = codec;
            return this;
        }

        public UdpClientBuilder<LISTENER, CODEC> listener(LISTENER listener) {
            this.listener = listener;
            return this;
        }

        public UdpClientBuilder<LISTENER, CODEC> heartbeat(IHeartbeatService<?> svc) {
            this.heartbeatService = svc;
            return this;
        }

        public NioUdpClient build() throws Exception {
            return createUdpClient(clientCfg, codec, heartbeatService, listener);
        }
    }

    /**
     * 创建 UDP 客户端构建器
     *
     * @param <LISTENER> 监听器类型
     * @param <CODEC>    编解码器类型
     * @return 返回 UDP 客户端构建器实例
     * @since 2.1.4
     */
    public static <LISTENER extends AbstractNioUdpListener, CODEC extends INioCodec> UdpClientBuilder<LISTENER, CODEC> createUdpClient() {
        return new UdpClientBuilder<>();
    }
}
