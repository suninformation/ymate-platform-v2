/*
 * Copyright 2007-present the original author or authors.
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

import net.ymate.platform.commons.util.NetworkUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.serv.IClientCfg;
import net.ymate.platform.serv.IServerCfg;
import net.ymate.platform.serv.Servs;
import net.ymate.platform.serv.impl.DefaultClientCfg;
import net.ymate.platform.serv.impl.DefaultServerCfg;
import net.ymate.platform.serv.nio.client.NioClient;
import net.ymate.platform.serv.nio.codec.TextLineCodec;
import net.ymate.platform.serv.nio.datagram.AbstractNioUdpListener;
import net.ymate.platform.serv.nio.datagram.NioUdpClient;
import net.ymate.platform.serv.nio.datagram.NioUdpServer;
import net.ymate.platform.serv.nio.server.NioServer;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * 边界条件和异常处理测试
 *
 * @author 刘镇 (suninformation@163.com) on 2026/2/7 22:48:27
 * @since 2.1.4
 */
@Ignore
public class NioCommunicationEdgeCaseTest extends AbstractNioUdpListener {

    private static final Log LOG = LogFactory.getLog(NioCommunicationEdgeCaseTest.class);

    private String hostName;

    private NioServer server;

    private NioUdpServer udpServer;

    private final net.ymate.platform.serv.nio.server.NioServerListener serverListener = new net.ymate.platform.serv.nio.server.NioServerListener() {
        @Override
        public void onMessageReceived(Object message, net.ymate.platform.serv.nio.INioSession session) throws IOException {
            LOG.info("服务端收到消息: " + message);
        }

        @Override
        public void onExceptionCaught(Throwable e, net.ymate.platform.serv.nio.INioSession session) throws IOException {
            LOG.error("TCP服务端异常: " + e.getMessage(), e);
        }
    };

    private final net.ymate.platform.serv.nio.client.NioClientListener clientListener = new net.ymate.platform.serv.nio.client.NioClientListener() {
        @Override
        public void onMessageReceived(Object message, net.ymate.platform.serv.nio.INioSession session) throws IOException {
            LOG.info("客户端收到消息: " + message);
        }

        @Override
        public void onExceptionCaught(Throwable e, net.ymate.platform.serv.nio.INioSession session) throws IOException {
            LOG.error("TCP客户端异常: " + e.getMessage(), e);
        }
    };

    @Before
    public void setUp() throws Exception {
        // 获取本地IP地址
        String[] ipAddresses = NetworkUtils.IP.getHostIPAddresses();
        if (ArrayUtils.isNotEmpty(ipAddresses)) {
            hostName = ipAddresses[0];
        } else {
            hostName = NetworkUtils.IP.getHostName();
        }
    }

    @After
    public void tearDown() throws Exception {
        try {
            if (server != null) {
                server.close();
            }
            if (udpServer != null) {
                udpServer.close();
            }
        } catch (Exception e) {
            LOG.error("测试清理失败", RuntimeUtils.unwrapThrow(e));
        }
    }

    @Test
    public void testTcpServerPortConflict() {
        // 启动第一个服务端
        try {
            IServerCfg serverCfg = DefaultServerCfg.builder()
                    .serverName("TestServer1")
                    .serverHost(hostName)
                    .port(8284)
                    .build();
            server = Servs.createServer(serverCfg, new TextLineCodec(), serverListener);
            server.start();
            Assert.assertTrue("第一个服务端应该已启动", server.isStarted());
            LOG.info("第一个TCP服务端已启动: " + hostName + ":8284");

            // 尝试在同一端口启动第二个服务端，应该失败
            boolean portConflict = false;
            try {
                IServerCfg serverCfg2 = DefaultServerCfg.builder()
                        .serverName("TestServer2")
                        .serverHost(hostName)
                        .port(8284)
                        .build();
                NioServer server2 = Servs.createServer(serverCfg2, new TextLineCodec(), serverListener);
                server2.start();
            } catch (IOException e) {
                portConflict = true;
                LOG.info("端口冲突测试通过: " + e.getMessage());
            }
            Assert.assertTrue("应该发生端口冲突", portConflict);
        } catch (Exception e) {
            LOG.error("测试失败", RuntimeUtils.unwrapThrow(e));
            Assert.fail("测试失败: " + e.getMessage());
        }
    }

    @Test
    public void testUdpServerPortConflict() {
        // 启动第一个UDP服务端
        try {
            IServerCfg serverCfg = DefaultServerCfg.builder()
                    .serverName("UdpTestServer1")
                    .serverHost(hostName)
                    .port(8285)
                    .build();
            udpServer = Servs.createUdpServer(serverCfg, new TextLineCodec(), this);
            udpServer.start();
            Assert.assertTrue("第一个UDP服务端应该已启动", udpServer.isStarted());
            LOG.info("第一个UDP服务端已启动: " + hostName + ":8285");

            // 尝试在同一端口启动第二个UDP服务端，应该失败
            boolean portConflict = false;
            try {
                IServerCfg serverCfg2 = DefaultServerCfg.builder()
                        .serverName("UdpTestServer2")
                        .serverHost(hostName)
                        .port(8285)
                        .build();
                NioUdpServer udpServer2 = Servs.createUdpServer(serverCfg2, new TextLineCodec(), this);
                udpServer2.start();
            } catch (IOException e) {
                portConflict = true;
                LOG.info("UDP端口冲突测试通过: " + e.getMessage());
            }
            Assert.assertTrue("应该发生端口冲突", portConflict);
        } catch (Exception e) {
            LOG.error("测试失败", RuntimeUtils.unwrapThrow(e));
            Assert.fail("测试失败: " + e.getMessage());
        }
    }

    @Test
    public void testClientConnectToNonExistentServer() {
        // 尝试连接到不存在的服务端
        try {
            IClientCfg clientCfg = DefaultClientCfg.builder()
                    .clientName("TestClient")
                    .remoteHost(hostName)
                    .port(9999) // 不存在的端口
                    .build();
            NioClient client = Servs.createClient(clientCfg, new TextLineCodec(), null, null, clientListener);
            try {
                client.connect();
            } catch (IOException e) {
                LOG.info("连接不存在服务端测试通过: " + e.getMessage());
            }
            // 注意：由于NIO的非阻塞特性，connect可能不会立即失败，需要检查isConnected()
            Thread.sleep(TimeUnit.SECONDS.toMillis(2));
            Assert.assertFalse("客户端应该未连接", client.isConnected());
            client.close();
        } catch (Exception e) {
            LOG.error("测试失败", RuntimeUtils.unwrapThrow(e));
            Assert.fail("测试失败: " + e.getMessage());
        }
    }

    @Test
    public void testUdpClientConnectToNonExistentServer() {
        // 尝试连接到不存在的UDP服务端
        try {
            IClientCfg clientCfg = DefaultClientCfg.builder()
                    .clientName("UdpTestClient")
                    .remoteHost(hostName)
                    .port(9999) // 不存在的端口
                    .build();
            NioUdpClient client = Servs.createUdpClient(clientCfg, new TextLineCodec(), null, this);
            client.connect();
            // UDP是无连接的，所以connect总是成功的
            client.close();
            LOG.info("UDP客户端连接测试通过");
        } catch (Exception e) {
            LOG.error("测试失败", RuntimeUtils.unwrapThrow(e));
            Assert.fail("测试失败: " + e.getMessage());
        }
    }

    @Test
    public void testSendEmptyMessage() {
        // 启动服务端
        try {
            IServerCfg serverCfg = DefaultServerCfg.builder()
                    .serverName("TestServer")
                    .serverHost(hostName)
                    .port(8286)
                    .build();
            server = Servs.createServer(serverCfg, new TextLineCodec(), serverListener);
            server.start();
            Assert.assertTrue("服务端应该已启动", server.isStarted());

            // 启动客户端并发送空消息
            IClientCfg clientCfg = DefaultClientCfg.builder()
                    .clientName("TestClient")
                    .remoteHost(hostName)
                    .port(8286)
                    .build();
            NioClient client = Servs.createClient(clientCfg, new TextLineCodec(), null, null, clientListener);
            client.connect();
            Assert.assertTrue("客户端应该已连接", client.isConnected());

            // 发送空消息，应该不会抛出异常
            try {
                client.send("");
                LOG.info("发送空消息测试通过");
            } catch (Exception e) {
                LOG.error("发送空消息失败", RuntimeUtils.unwrapThrow(e));
                Assert.fail("发送空消息失败: " + e.getMessage());
            }

            client.close();
        } catch (Exception e) {
            LOG.error("测试失败", RuntimeUtils.unwrapThrow(e));
            Assert.fail("测试失败: " + e.getMessage());
        }
    }

    // ---- AbstractNioUdpListener 实现

    @Override
    public Object onSessionReady() throws IOException {
        return null;
    }

    @Override
    public Object onMessageReceived(InetSocketAddress sourceAddress, Object message) throws IOException {
        return null;
    }

    @Override
    public void onExceptionCaught(InetSocketAddress sourceAddress, Throwable e) throws IOException {
        LOG.error("UDP异常: " + e.getMessage(), e);
    }


}
