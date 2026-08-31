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
package net.ymate.platform.serv.nio.datagram;

import net.ymate.platform.serv.IClientCfg;
import net.ymate.platform.serv.IHeartbeatService;
import net.ymate.platform.serv.IServerCfg;
import net.ymate.platform.serv.Servs;
import net.ymate.platform.serv.impl.DefaultClientCfg;
import net.ymate.platform.serv.impl.DefaultHeartbeatServiceImpl;
import net.ymate.platform.serv.impl.DefaultServerCfg;
import net.ymate.platform.serv.nio.NioTestSupport;
import net.ymate.platform.serv.nio.codec.TextLineCodec;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * UDP通讯单元测试
 *
 * @author 刘镇 (suninformation@163.com) on 2026/2/7 22:48:27
 * @since 2.1.4
 */
@Ignore
public class NioUdpCommunicationTest extends AbstractNioUdpListener {

    private static final Log LOG = LogFactory.getLog(NioUdpCommunicationTest.class);

    private NioUdpServer server;

    private NioUdpClient client;

    private String hostName;

    private int serverPort;

    private final AtomicReference<String> receivedMessage = new AtomicReference<>();

    private final AtomicInteger messageCount = new AtomicInteger(0);

    private final AtomicBoolean heartbeatReceived = new AtomicBoolean(false);

    private final CountDownLatch messageLatch = new CountDownLatch(1);

    private final CountDownLatch heartbeatLatch = new CountDownLatch(1);

    @Before
    public void setUp() throws Exception {
        hostName = NioTestSupport.getLocalHostName();
        serverPort = NioTestSupport.getAvailablePort();
        startServer();
        // 等待服务端 NIO selector 就绪，避免客户端消息在服务端注册完成前发送导致丢失
        Thread.sleep(500);
        startClient();
    }

    private void startServer() throws Exception {
        IServerCfg serverCfg = DefaultServerCfg.builder()
                .serverName("UdpTestServer")
                .serverHost(hostName)
                .port(serverPort)
                .keepAliveTime(10000)
                .build();
        server = Servs.<AbstractNioUdpListener, TextLineCodec>createUdpServer()
                .config(serverCfg)
                .codec(new TextLineCodec())
                .listener(this)
                .build();
        server.start();
        Assert.assertTrue("服务端应该已启动", server.isStarted());
        LOG.info(String.format("UDP服务端已启动: %s:%d", hostName, serverPort));
    }

    private void startClient() throws Exception {
        IClientCfg clientCfg = DefaultClientCfg.builder()
                .clientName("UdpTestClient")
                .remoteHost(hostName)
                .port(serverPort)
                .heartbeatInterval(5)
                .build();
        IHeartbeatService<String> heartbeatService = new DefaultHeartbeatServiceImpl();
        client = Servs.<AbstractNioUdpListener, TextLineCodec>createUdpClient()
                .config(clientCfg)
                .codec(new TextLineCodec())
                .heartbeat(heartbeatService)
                .listener(this)
                .build();
        client.connect();
        // 等待客户端 NIO 线程完成 registerEvent（selectionKey 和 status 均就绪），
        // 避免 send() 在 selectionKey 为 null 时静默丢弃消息
        Assert.assertTrue("客户端应该已就绪", waitForClientConnected(client, 5, TimeUnit.SECONDS));
        LOG.info(String.format("UDP客户端已初始化: %s:%d", hostName, serverPort));
    }

    @After
    public void tearDown() throws Exception {
        NioTestSupport.closeQuietly(client, server);
    }

    private boolean waitForClientConnected(NioUdpClient client, long timeout, TimeUnit unit) throws InterruptedException {
        long deadline = System.currentTimeMillis() + unit.toMillis(timeout);
        while (System.currentTimeMillis() < deadline) {
            if (client.isConnected()) {
                return true;
            }
            Thread.sleep(100);
        }
        return client.isConnected();
    }

    @Test
    public void testBasicCommunication() throws Exception {
        String testMessage = "Hello UDP Server!";
        client.send(testMessage);
        LOG.info("已发送消息: " + testMessage);

        boolean received = messageLatch.await(5, TimeUnit.SECONDS);
        Assert.assertTrue("应该收到服务端响应", received);
        Assert.assertNotNull("接收到的消息不应为null", receivedMessage.get());
        LOG.info("收到服务端响应: " + receivedMessage.get());

        Assert.assertTrue("消息计数应该大于0", messageCount.get() > 0);
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
        Assert.assertNotNull("客户端配置不应为null", client.clientCfg());
        Assert.assertNotNull("客户端监听器不应为null", client.listener());
        LOG.info("客户端状态测试通过");
    }

    // ---- AbstractNioUdpListener 实现

    @Override
    public Object onSessionReady() throws IOException {
        LOG.info("会话准备就绪");
        return "SessionReady";
    }

    @Override
    public Object onMessageReceived(InetSocketAddress sourceAddress, Object message) throws IOException {
        String msg = message.toString();
        LOG.info("服务端收到消息: " + msg + " 来自: " + sourceAddress);
        messageCount.incrementAndGet();

        if ("0".equals(msg)) {
            heartbeatReceived.set(true);
            heartbeatLatch.countDown();
            LOG.info("收到心跳包");
        } else {
            receivedMessage.set(msg);
            messageLatch.countDown();
            return "Server response: " + msg;
        }
        return null;
    }

    @Override
    public void onExceptionCaught(InetSocketAddress sourceAddress, Throwable e) throws IOException {
        LOG.error("服务端异常: " + e.getMessage() + " 来自: " + sourceAddress, e);
    }
}
