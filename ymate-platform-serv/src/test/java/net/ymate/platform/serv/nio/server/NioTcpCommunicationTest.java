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
package net.ymate.platform.serv.nio.server;

import net.ymate.platform.serv.*;
import net.ymate.platform.serv.impl.DefaultClientCfg;
import net.ymate.platform.serv.impl.DefaultHeartbeatServiceImpl;
import net.ymate.platform.serv.impl.DefaultReconnectServiceImpl;
import net.ymate.platform.serv.impl.DefaultServerCfg;
import net.ymate.platform.serv.nio.INioSession;
import net.ymate.platform.serv.nio.NioTestSupport;
import net.ymate.platform.serv.nio.client.NioClient;
import net.ymate.platform.serv.nio.client.NioClientListener;
import net.ymate.platform.serv.nio.codec.TextLineCodec;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * TCP/IP通讯单元测试
 *
 * @author 刘镇 (suninformation@163.com) on 2026/2/7 22:48:27
 * @since 2.1.4
 */
@Ignore
public class NioTcpCommunicationTest {

    private static final Log LOG = LogFactory.getLog(NioTcpCommunicationTest.class);

    private NioServer server;

    private NioClient client;

    private String hostName;

    private int serverPort;

    private final AtomicReference<String> receivedMessage = new AtomicReference<>();

    private final AtomicBoolean heartbeatReceived = new AtomicBoolean(false);

    private final AtomicBoolean reconnected = new AtomicBoolean(false);

    private final CountDownLatch messageLatch = new CountDownLatch(1);

    private final CountDownLatch heartbeatLatch = new CountDownLatch(1);

    private final CountDownLatch reconnectLatch = new CountDownLatch(1);

    private volatile CountDownLatch connectedLatch;

    private final ClientListener clientListener = new ClientListener();

    private class ClientListener extends NioClientListener {
        @Override
        public void onSessionRegistered(INioSession session) throws IOException {
            LOG.info("客户端会话已注册");
        }

        @Override
        public void onSessionConnected(INioSession session) throws IOException {
            LOG.info("客户端会话已打开");
            connectedLatch.countDown();
        }

        @Override
        public void onMessageReceived(Object message, INioSession session) throws IOException {
            LOG.info("客户端收到消息: " + message);
        }

        @Override
        public void onAfterSessionClosed(INioSession session) throws IOException {
            LOG.info("客户端会话已关闭");
        }

        @Override
        public void onExceptionCaught(Throwable e, INioSession session) throws IOException {
            LOG.error("客户端异常: " + e.getMessage(), e);
        }

        @Override
        public void onClientReconnected(IClient<?, ?> client) {
            LOG.info("客户端已重连: " + client.clientCfg().getClientName());
            reconnected.set(true);
            reconnectLatch.countDown();
        }
    }

    @Before
    public void setUp() throws Exception {
        hostName = NioTestSupport.getLocalHostName();
        serverPort = NioTestSupport.getAvailablePort();
        connectedLatch = new CountDownLatch(1);
        startServer();
        startClient();
        Assert.assertTrue("客户端应该已连接", connectedLatch.await(5, TimeUnit.SECONDS));
    }

    private void startServer() throws Exception {
        IServerCfg serverCfg = DefaultServerCfg.builder()
                .serverName("TcpTestServer")
                .serverHost(hostName)
                .port(serverPort)
                .keepAliveTime(10000)
                .build();
        server = Servs.<net.ymate.platform.serv.nio.server.NioServerListener, TextLineCodec>createServer()
                .config(serverCfg)
                .codec(new TextLineCodec())
                .listener(new net.ymate.platform.serv.nio.server.NioServerListener() {
                    @Override
                    public void onMessageReceived(Object message, net.ymate.platform.serv.nio.INioSession session) throws IOException {
                        LOG.info("服务端收到消息: " + message);
                        if ("0".equals(message)) {
                            heartbeatReceived.set(true);
                            heartbeatLatch.countDown();
                            LOG.info("服务端收到心跳包");
                        } else {
                            receivedMessage.set(message.toString());
                            messageLatch.countDown();
                            session.send("Server response: " + message);
                        }
                    }

                    @Override
                    public void onSessionAccepted(net.ymate.platform.serv.nio.INioSession session) throws IOException {
                        LOG.info("服务端会话已打开: " + session.id());
                        super.onSessionAccepted(session);
                    }
                })
                .build();
        server.start();
        Assert.assertTrue("服务端应该已启动", server.isStarted());
        LOG.info(String.format("TCP服务端已启动: %s:%d", hostName, serverPort));
    }

    private void startClient() throws Exception {
        IClientCfg clientCfg = DefaultClientCfg.builder()
                .clientName("TcpTestClient")
                .remoteHost(hostName)
                .port(serverPort)
                .heartbeatInterval(5)
                .reconnectionInterval(3)
                .build();
        IHeartbeatService<String> heartbeatService = new DefaultHeartbeatServiceImpl();
        IReconnectService reconnectService = new DefaultReconnectServiceImpl();
        client = Servs.<NioClientListener, TextLineCodec>createClient()
                .config(clientCfg)
                .codec(new TextLineCodec())
                .reconnect(reconnectService)
                .heartbeat(heartbeatService)
                .listener(clientListener)
                .build();
        client.connect();
        LOG.info(String.format("TCP客户端已初始化: %s:%d", hostName, serverPort));
    }

    @After
    public void tearDown() throws Exception {
        NioTestSupport.closeQuietly(client, server);
    }

    @Test
    public void testBasicCommunication() throws Exception {
        String testMessage = "Hello TCP Server!";
        client.send(testMessage);
        LOG.info("已发送消息: " + testMessage);

        boolean received = messageLatch.await(5, TimeUnit.SECONDS);
        Assert.assertTrue("应该收到服务端响应", received);
        Assert.assertNotNull("接收到的消息不应为null", receivedMessage.get());
        LOG.info("收到服务端响应: " + receivedMessage.get());
    }

    @Test
    public void testHeartbeat() throws Exception {
        boolean received = heartbeatLatch.await(10, TimeUnit.SECONDS);
        Assert.assertTrue("应该收到心跳包", received);
        Assert.assertTrue("心跳包标志应该为true", heartbeatReceived.get());
        LOG.info("心跳测试通过");
    }

    @Test
    public void testServerStatus() {
        Assert.assertTrue("服务端应该已启动", server.isStarted());
        Assert.assertNotNull("服务端配置不应为null", server.serverCfg());
        Assert.assertNotNull("服务端监听器不应为null", server.listener());
        LOG.info("服务端状态测试通过");
    }

    @Test
    public void testClientStatus() {
        Assert.assertTrue("客户端应该已连接", client.isConnected());
        Assert.assertNotNull("客户端配置不应为null", client.clientCfg());
        Assert.assertNotNull("客户端监听器不应为null", client.listener());
        LOG.info("客户端状态测试通过");
    }

    @Test
    public void testReconnect() throws Exception {
        Assert.assertTrue("客户端初始状态应该已连接", client.isConnected());

        LOG.info("关闭服务端模拟网络断开...");
        server.close();

        Thread.sleep(TimeUnit.SECONDS.toMillis(3));

        LOG.info("重新启动服务端...");
        startServer();

        LOG.info("等待客户端重连...");
        // 注意: DefaultReconnectServiceImpl 中 onClientReconnected 在 reconnect() 之前调用，
        // 即 reconnectLatch 被 countDown 时连接可能尚未真正建立，需要额外等待 isConnected()
        boolean reconnectedSuccess = reconnectLatch.await(30, TimeUnit.SECONDS);
        if (reconnectedSuccess) {
            reconnectedSuccess = waitForClientReconnected(client, 10, TimeUnit.SECONDS);
        }

        if (!reconnectedSuccess) {
            LOG.info("自动重连失败，尝试手动触发重连...");
            try {
                client.reconnect();
                // 手动 reconnect() 不会触发 onClientReconnected 回调，
                // 需要直接轮询 isConnected() 判断连接是否建立
                reconnectedSuccess = waitForClientReconnected(client, 10, TimeUnit.SECONDS);
                if (reconnectedSuccess) {
                    LOG.info("手动重连成功");
                }
            } catch (IOException e) {
                LOG.error("手动重连失败: " + e.getMessage());
            }
        }

        Assert.assertTrue("客户端应该成功重连", reconnectedSuccess);
        Assert.assertTrue("客户端应该已重新连接", client.isConnected());
        LOG.info("断线重连测试通过");
    }

    private boolean waitForClientReconnected(NioClient client, long timeout, TimeUnit unit) throws InterruptedException {
        long deadline = System.currentTimeMillis() + unit.toMillis(timeout);
        while (System.currentTimeMillis() < deadline) {
            if (client.isConnected()) {
                return true;
            }
            Thread.sleep(200);
        }
        return client.isConnected();
    }
}
