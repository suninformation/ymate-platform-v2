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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * UDP会话管理单元测试
 *
 * @author 刘镇 (suninformation@163.com) on 2026/2/7 22:48:27
 * @since 2.1.4
 */
@Ignore
public class NioUdpSessionManagerTest {

    private static final Log LOG = LogFactory.getLog(NioUdpSessionManagerTest.class);

    private NioUdpSessionManager<NioUdpSessionWrapper, String> sessionManager;

    private final List<NioUdpClient> clients = new ArrayList<>();

    private String hostName;

    private int serverPort;

    private final AtomicInteger clientConnectCount = new AtomicInteger(0);

    private CountDownLatch clientsConnectedLatch;

    private volatile CountDownLatch messageReceivedLatch;

    private final AtomicReference<InetSocketAddress> testClientAddress = new AtomicReference<>();

    private TestUdpSessionListener sessionListener;

    private class TestUdpSessionListener implements INioUdpSessionListener<NioUdpSessionWrapper, String> {
        @Override
        public Object onMessageReceived(NioUdpSessionWrapper sessionWrapper, String message) throws IOException {
            InetSocketAddress sourceAddress = sessionWrapper.getId();
            LOG.info("服务端收到消息: " + message + " 来自: " + sourceAddress);

            int count = clientConnectCount.incrementAndGet();
            if (count == 1) {
                testClientAddress.set(sourceAddress);
                LOG.info("记录测试客户端地址: " + sourceAddress);
            }
            if (count <= 3 && clientsConnectedLatch != null) {
                clientsConnectedLatch.countDown();
                LOG.info("当前连接客户端数量: " + sessionManager.sessionCount());
            }
            if (!"0".equals(message) && messageReceivedLatch != null) {
                messageReceivedLatch.countDown();
            }

            return "Server response: " + message;
        }

        @Override
        public void onExceptionCaught(NioUdpSessionWrapper sessionWrapper, Throwable e) throws IOException {
            LOG.error("服务端异常: " + e.getMessage() + " 来自: " + sessionWrapper.getId(), e);
        }

        @Override
        public void onSessionIdleRemoved(NioUdpSessionWrapper sessionWrapper) {
            LOG.info("会话已被空闲移除: " + sessionWrapper.getId());
        }
    }

    @Before
    public void setUp() throws Exception {
        hostName = NioTestSupport.getLocalHostName();
        serverPort = NioTestSupport.getAvailablePort();
        clientConnectCount.set(0);
        testClientAddress.set(null);
        clientsConnectedLatch = new CountDownLatch(3);
        messageReceivedLatch = new CountDownLatch(1);
        startSessionManager();
    }

    @After
    public void tearDown() throws Exception {
        for (NioUdpClient client : clients) {
            NioTestSupport.closeQuietly(client);
        }
        clients.clear();
        NioTestSupport.closeQuietly(sessionManager);
    }

    private void startSessionManager() throws Exception {
        IServerCfg serverCfg = DefaultServerCfg.builder()
                .serverName("UdpSessionManagerTestServer")
                .serverHost(hostName)
                .port(serverPort)
                .keepAliveTime(10000)
                .build();
        sessionListener = new TestUdpSessionListener();
        sessionManager = new NioUdpSessionManager<>(serverCfg, new TextLineCodec(), sessionListener);
        sessionManager.initialize();
        LOG.info(String.format("UDP会话管理器已启动: %s:%d", hostName, serverPort));
    }

    @Test
    public void testMultiClientConnection() throws Exception {
        messageReceivedLatch = new CountDownLatch(3);
        for (int i = 1; i <= 3; i++) {
            NioUdpClient client = connectUdpClient("UdpClient-" + i);
            client.send("Hello from " + "UdpClient-" + i);
        }

        Assert.assertTrue("所有客户端消息应该被接收", messageReceivedLatch.await(10, TimeUnit.SECONDS));

        LOG.info("\n=== 所有客户端会话信息 ===");
        Collection<NioUdpSessionWrapper> sessionWrappers = sessionManager.sessionWrappers();
        for (NioUdpSessionWrapper sessionWrapper : sessionWrappers) {
            LOG.info("客户端地址: " + sessionWrapper.getId());
        }
        LOG.info("客户端连接数量: " + sessionManager.sessionCount());
        LOG.info("=======================");
    }

    @Test
    public void testFindClient() throws Exception {
        messageReceivedLatch = new CountDownLatch(1);
        NioUdpClient client = connectUdpClient("FindTestClient");
        client.send("Hello from FindTestClient");

        Assert.assertTrue("等待消息接收超时", messageReceivedLatch.await(5, TimeUnit.SECONDS));

        InetSocketAddress clientAddress = testClientAddress.get();
        Assert.assertNotNull("测试客户端地址不应该为null", clientAddress);

        boolean exists = sessionManager.contains(clientAddress);
        Assert.assertTrue("测试客户端应该存在", exists);

        NioUdpSessionWrapper sessionWrapper = sessionManager.sessionWrapper(clientAddress);
        Assert.assertNotNull("测试客户端会话包装器不应该为null", sessionWrapper);
        Assert.assertEquals("客户端地址应该匹配", clientAddress, sessionWrapper.getId());

        LOG.info("查找客户端测试通过，客户端地址: " + clientAddress);
    }

    @Test
    public void testSendToSpecificClient() throws Exception {
        messageReceivedLatch = new CountDownLatch(2);
        NioUdpClient client1 = connectUdpClient("SendTestClient1");
        client1.send("Hello from SendTestClient1");

        NioUdpClient client2 = connectUdpClient("SendTestClient2");
        client2.send("Hello from SendTestClient2");

        Assert.assertTrue("等待消息接收超时", messageReceivedLatch.await(5, TimeUnit.SECONDS));

        InetSocketAddress clientAddress = testClientAddress.get();
        Assert.assertNotNull("测试客户端地址不应该为null", clientAddress);

        String testMessage = "Hello from SessionManager!";
        boolean sent = sessionManager.sendTo(clientAddress, testMessage);
        Assert.assertTrue("消息应该成功发送", sent);

        LOG.info("向指定客户端发送消息测试通过，客户端地址: " + clientAddress + "，消息: " + testMessage);
    }

    @Test
    public void testDisconnectSpecificClient() throws Exception {
        messageReceivedLatch = new CountDownLatch(2);
        NioUdpClient client1 = connectUdpClient("DisconnectTestClient1");
        client1.send("Hello from DisconnectTestClient1");

        NioUdpClient client2 = connectUdpClient("DisconnectTestClient2");
        client2.send("Hello from DisconnectTestClient2");

        Assert.assertTrue("等待消息接收超时", messageReceivedLatch.await(5, TimeUnit.SECONDS));

        InetSocketAddress clientAddress = testClientAddress.get();
        Assert.assertNotNull("测试客户端地址不应该为null", clientAddress);

        NioUdpSessionWrapper sessionWrapper = sessionManager.sessionWrapper(clientAddress);
        Assert.assertNotNull("测试客户端会话包装器不应该为null", sessionWrapper);

        sessionManager.closeSessionWrapper(sessionWrapper);

        Assert.assertTrue("等待客户端断开超时", waitForSessionCount(1, 5, TimeUnit.SECONDS));

        boolean exists = sessionManager.contains(clientAddress);
        Assert.assertFalse("测试客户端应该不存在", exists);

        LOG.info("断开指定客户端连接测试通过，客户端地址: " + clientAddress);
        LOG.info("当前客户端连接数量: " + sessionManager.sessionCount());
    }

    private NioUdpClient connectUdpClient(String clientName) throws Exception {
        IClientCfg clientCfg = DefaultClientCfg.builder()
                .clientName(clientName)
                .remoteHost(hostName)
                .port(serverPort)
                .heartbeatInterval(10)
                .build();
        IHeartbeatService<String> heartbeatService = new DefaultHeartbeatServiceImpl();
        AbstractNioUdpListener clientListener = new AbstractNioUdpListener() {
            @Override
            public Object onMessageReceived(InetSocketAddress sourceAddress, Object message) throws IOException {
                LOG.info("客户端收到消息: " + message + " 来自: " + sourceAddress);
                return null;
            }

            @Override
            public void onExceptionCaught(InetSocketAddress sourceAddress, Throwable e) throws IOException {
                LOG.error("客户端异常: " + e.getMessage() + " 来自: " + sourceAddress, e);
            }
        };
        NioUdpClient client = Servs.<AbstractNioUdpListener, TextLineCodec>createUdpClient()
                .config(clientCfg)
                .codec(new TextLineCodec())
                .heartbeat(heartbeatService)
                .listener(clientListener)
                .build();
        client.connect();
        // 等待客户端 NIO 线程完成 registerEvent（selectionKey 不为 null），
        // 避免 send() 在 selectionKey 为 null 时静默丢弃消息
        Assert.assertTrue("客户端应该已就绪: " + clientName, waitForClientConnected(client, 5, TimeUnit.SECONDS));
        clients.add(client);
        LOG.info(String.format("UDP客户端已初始化: %s -> %s:%d", clientName, hostName, serverPort));
        return client;
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

    private boolean waitForSessionCount(int expected, long timeout, TimeUnit unit) throws InterruptedException {
        long deadline = System.currentTimeMillis() + unit.toMillis(timeout);
        while (System.currentTimeMillis() < deadline) {
            if (sessionManager.sessionCount() == expected) {
                return true;
            }
            Thread.sleep(100);
        }
        return sessionManager.sessionCount() == expected;
    }
}
