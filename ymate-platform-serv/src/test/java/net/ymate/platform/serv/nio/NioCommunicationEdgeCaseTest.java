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
        hostName = NioTestSupport.getLocalHostName();
    }

    @After
    public void tearDown() throws Exception {
        // 兜底清理：每个测试方法应自行管理资源，这里不持有任何字段引用
    }

    @Test
    public void testTcpServerPortConflict() {
        NioServer server1 = null;
        NioServer server2 = null;
        try {
            int port = NioTestSupport.getAvailablePort();
            IServerCfg serverCfg = DefaultServerCfg.builder()
                    .serverName("TestServer1")
                    .serverHost(hostName)
                    .port(port)
                    .build();
            server1 = Servs.<net.ymate.platform.serv.nio.server.NioServerListener, TextLineCodec>createServer()
                    .config(serverCfg)
                    .codec(new TextLineCodec())
                    .listener(serverListener)
                    .build();
            server1.start();
            Assert.assertTrue("第一个服务端应该已启动", server1.isStarted());
            LOG.info(String.format("第一个TCP服务端已启动: %s:%d", hostName, port));

            boolean portConflict = false;
            try {
                IServerCfg serverCfg2 = DefaultServerCfg.builder()
                        .serverName("TestServer2")
                        .serverHost(hostName)
                        .port(port)
                        .build();
                server2 = Servs.<net.ymate.platform.serv.nio.server.NioServerListener, TextLineCodec>createServer()
                        .config(serverCfg2)
                        .codec(new TextLineCodec())
                        .listener(serverListener)
                        .build();
                server2.start();
            } catch (IOException e) {
                portConflict = true;
                LOG.info("端口冲突测试通过: " + e.getMessage());
            }
            Assert.assertTrue("应该发生端口冲突", portConflict);
        } catch (Exception e) {
            LOG.error("测试失败", e);
            Assert.fail("测试失败: " + e.getMessage());
        } finally {
            NioTestSupport.closeQuietly(server2, server1);
        }
    }

    @Test
    public void testUdpServerPortConflict() {
        NioUdpServer server1 = null;
        NioUdpServer server2 = null;
        try {
            int port = NioTestSupport.getAvailablePort();
            IServerCfg serverCfg = DefaultServerCfg.builder()
                    .serverName("UdpTestServer1")
                    .serverHost(hostName)
                    .port(port)
                    .build();
            server1 = Servs.<AbstractNioUdpListener, TextLineCodec>createUdpServer()
                    .config(serverCfg)
                    .codec(new TextLineCodec())
                    .listener(this)
                    .build();
            server1.start();
            Assert.assertTrue("第一个UDP服务端应该已启动", server1.isStarted());
            LOG.info(String.format("第一个UDP服务端已启动: %s:%d", hostName, port));

            boolean portConflict = false;
            try {
                IServerCfg serverCfg2 = DefaultServerCfg.builder()
                        .serverName("UdpTestServer2")
                        .serverHost(hostName)
                        .port(port)
                        .build();
                server2 = Servs.<AbstractNioUdpListener, TextLineCodec>createUdpServer()
                        .config(serverCfg2)
                        .codec(new TextLineCodec())
                        .listener(this)
                        .build();
                server2.start();
            } catch (IOException e) {
                portConflict = true;
                LOG.info("UDP端口冲突测试通过: " + e.getMessage());
            }
            Assert.assertTrue("应该发生端口冲突", portConflict);
        } catch (Exception e) {
            LOG.error("测试失败", e);
            Assert.fail("测试失败: " + e.getMessage());
        } finally {
            NioTestSupport.closeQuietly(server2, server1);
        }
    }

    @Test
    public void testClientConnectToNonExistentServer() {
        NioClient client = null;
        try {
            // 使用一个不太可能被占用的端口号连接不存在的服务端
            IClientCfg clientCfg = DefaultClientCfg.builder()
                    .clientName("TestClient")
                    .remoteHost(hostName)
                    .port(39999)
                    .build();
            client = Servs.<net.ymate.platform.serv.nio.client.NioClientListener, TextLineCodec>createClient()
                    .config(clientCfg)
                    .codec(new TextLineCodec())
                    .listener(clientListener)
                    .build();
            try {
                client.connect();
            } catch (IOException e) {
                LOG.info("连接不存在服务端测试通过: " + e.getMessage());
            }
            Thread.sleep(TimeUnit.SECONDS.toMillis(2));
            Assert.assertFalse("客户端应该未连接", client.isConnected());
        } catch (Exception e) {
            LOG.error("测试失败", e);
            Assert.fail("测试失败: " + e.getMessage());
        } finally {
            NioTestSupport.closeQuietly(client);
        }
    }

    @Test
    public void testUdpClientConnectToNonExistentServer() {
        NioUdpClient client = null;
        try {
            int port = NioTestSupport.getAvailablePort();
            IClientCfg clientCfg = DefaultClientCfg.builder()
                    .clientName("UdpTestClient")
                    .remoteHost(hostName)
                    .port(port)
                    .build();
            client = Servs.<AbstractNioUdpListener, TextLineCodec>createUdpClient()
                    .config(clientCfg)
                    .codec(new TextLineCodec())
                    .listener(this)
                    .build();
            client.connect();
            LOG.info("UDP客户端连接测试通过");
        } catch (Exception e) {
            LOG.error("测试失败", e);
            Assert.fail("测试失败: " + e.getMessage());
        } finally {
            NioTestSupport.closeQuietly(client);
        }
    }

    @Test
    public void testSendEmptyMessage() {
        NioServer server = null;
        NioClient client = null;
        try {
            int port = NioTestSupport.getAvailablePort();
            IServerCfg serverCfg = DefaultServerCfg.builder()
                    .serverName("TestServer")
                    .serverHost(hostName)
                    .port(port)
                    .build();
            server = Servs.<net.ymate.platform.serv.nio.server.NioServerListener, TextLineCodec>createServer()
                    .config(serverCfg)
                    .codec(new TextLineCodec())
                    .listener(serverListener)
                    .build();
            server.start();
            Assert.assertTrue("服务端应该已启动", server.isStarted());

            IClientCfg clientCfg = DefaultClientCfg.builder()
                    .clientName("TestClient")
                    .remoteHost(hostName)
                    .port(port)
                    .build();
            client = Servs.<net.ymate.platform.serv.nio.client.NioClientListener, TextLineCodec>createClient()
                    .config(clientCfg)
                    .codec(new TextLineCodec())
                    .listener(clientListener)
                    .build();
            client.connect();
            Thread.sleep(TimeUnit.SECONDS.toMillis(2));
            Assert.assertTrue("客户端应该已连接", client.isConnected());

            try {
                client.send("");
                LOG.info("发送空消息测试通过");
            } catch (Exception e) {
                LOG.error("发送空消息失败", e);
                Assert.fail("发送空消息失败: " + e.getMessage());
            }
        } catch (Exception e) {
            LOG.error("测试失败", e);
            Assert.fail("测试失败: " + e.getMessage());
        } finally {
            NioTestSupport.closeQuietly(client, server);
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
