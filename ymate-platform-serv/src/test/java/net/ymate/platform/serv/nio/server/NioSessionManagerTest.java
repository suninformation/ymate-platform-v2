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

import net.ymate.platform.serv.IClientCfg;
import net.ymate.platform.serv.IHeartbeatService;
import net.ymate.platform.serv.IServerCfg;
import net.ymate.platform.serv.Servs;
import net.ymate.platform.serv.impl.DefaultClientCfg;
import net.ymate.platform.serv.impl.DefaultHeartbeatServiceImpl;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * TCP会话管理单元测试
 *
 * @author 刘镇 (suninformation@163.com) on 2026/2/7 22:48:27
 * @since 2.1.4
 */
@Ignore
public class NioSessionManagerTest {

    private static final Log LOG = LogFactory.getLog(NioSessionManagerTest.class);

    private NioSessionManager<NioSessionWrapper, String> sessionManager;

    private final List<NioClient> clients = new ArrayList<>();

    private String hostName;

    private int serverPort;

    private final AtomicInteger clientConnectCount = new AtomicInteger(0);

    private CountDownLatch clientsConnectedLatch;

    private volatile CountDownLatch sessionAcceptedLatch;

    private final AtomicReference<String> testClientSessionId = new AtomicReference<>();

    private TestSessionListener sessionListener;

    private class TestSessionListener extends AbstractNioSessionListener<NioSessionWrapper, String> {
        @Override
        public void onSessionAccepted(NioSessionWrapper sessionWrapper) throws IOException {
            super.onSessionAccepted(sessionWrapper);
            LOG.info("客户端已连接: " + sessionWrapper.getId());
            int count = clientConnectCount.incrementAndGet();
            if (count == 1) {
                testClientSessionId.set(sessionWrapper.getId());
                LOG.info("记录测试客户端会话ID: " + sessionWrapper.getId());
            }
            if (clientsConnectedLatch != null) {
                clientsConnectedLatch.countDown();
            }
            if (sessionAcceptedLatch != null) {
                sessionAcceptedLatch.countDown();
            }
            LOG.info("当前连接客户端数量: " + sessionManager.sessionCount());
        }

        @Override
        public void onAfterSessionClosed(NioSessionWrapper sessionWrapper) throws IOException {
            super.onAfterSessionClosed(sessionWrapper);
            LOG.info("客户端已断开连接: " + sessionWrapper.getId());
            LOG.info("当前连接客户端数量: " + sessionManager.sessionCount());
        }

        @Override
        public void onMessageReceived(String message, NioSessionWrapper sessionWrapper) throws IOException {
            super.onMessageReceived(message, sessionWrapper);
            LOG.info("服务端收到消息: " + message + " 来自: " + sessionWrapper.getId());
            sessionWrapper.getSession().send("Server response: " + message);
        }

        @Override
        public void onExceptionCaught(Throwable e, NioSessionWrapper sessionWrapper) throws IOException {
            super.onExceptionCaught(e, sessionWrapper);
            LOG.error("服务端异常: " + e.getMessage(), e);
        }
    }

    private class TestClientListener extends NioClientListener {
        @Override
        public void onSessionRegistered(INioSession session) throws IOException {
            super.onSessionRegistered(session);
            LOG.info("客户端会话已注册");
        }

        @Override
        public void onSessionConnected(INioSession session) throws IOException {
            super.onSessionConnected(session);
            LOG.info("客户端会话已打开");
        }

        @Override
        public void onMessageReceived(Object message, INioSession session) throws IOException {
            super.onMessageReceived(message, session);
            LOG.info("客户端收到消息: " + message);
        }

        @Override
        public void onAfterSessionClosed(INioSession session) throws IOException {
            super.onAfterSessionClosed(session);
            LOG.info("客户端会话已关闭");
        }

        @Override
        public void onExceptionCaught(Throwable e, INioSession session) throws IOException {
            super.onExceptionCaught(e, session);
            LOG.error("客户端异常: " + e.getMessage(), e);
        }
    }

    @Before
    public void setUp() throws Exception {
        hostName = NioTestSupport.getLocalHostName();
        serverPort = NioTestSupport.getAvailablePort();
        clientConnectCount.set(0);
        testClientSessionId.set(null);
        clientsConnectedLatch = new CountDownLatch(3);
        sessionAcceptedLatch = new CountDownLatch(1);
        startSessionManager();
    }

    private void startSessionManager() throws Exception {
        IServerCfg serverCfg = DefaultServerCfg.builder()
                .serverName("TcpSessionManagerTestServer")
                .serverHost(hostName)
                .port(serverPort)
                .keepAliveTime(10000)
                .build();
        sessionListener = new TestSessionListener();
        sessionManager = new NioSessionManager<>(serverCfg, new TextLineCodec(), sessionListener);
        sessionManager.initialize();
        LOG.info(String.format("TCP会话管理器已启动: %s:%d", hostName, serverPort));
    }

    @After
    public void tearDown() throws Exception {
        for (NioClient client : clients) {
            NioTestSupport.closeQuietly(client);
        }
        clients.clear();
        NioTestSupport.closeQuietly(sessionManager);
    }

    @Test
    public void testMultiClientConnection() throws Exception {
        for (int i = 1; i <= 3; i++) {
            connectClient("Client-" + i);
        }

        boolean allConnected = clientsConnectedLatch.await(30, TimeUnit.SECONDS);
        Assert.assertTrue("所有客户端应该成功连接", allConnected);
        Assert.assertEquals("客户端连接数量应该为3", 3, sessionManager.sessionCount());

        LOG.info("\n=== 所有客户端会话信息 ===");
        Collection<NioSessionWrapper> sessionWrappers = sessionManager.sessionWrappers();
        for (NioSessionWrapper sessionWrapper : sessionWrappers) {
            LOG.info("会话ID: " + sessionWrapper.getId());
        }
        LOG.info("========================");
    }

    @Test
    public void testFindClient() throws Exception {
        connectClient("FindTestClient");

        Assert.assertNotNull("测试客户端会话ID不应该为null", testClientSessionId.get());

        String sessionId = testClientSessionId.get();
        boolean exists = sessionManager.contains(sessionId);
        Assert.assertTrue("测试客户端应该存在", exists);

        NioSessionWrapper sessionWrapper = sessionManager.sessionWrapper(sessionId);
        Assert.assertNotNull("测试客户端会话包装器不应该为null", sessionWrapper);
        Assert.assertEquals("会话ID应该匹配", sessionId, sessionWrapper.getId());

        LOG.info("查找客户端测试通过，会话ID: " + sessionId);
    }

    @Test
    public void testSendToSpecificClient() throws Exception {
        connectClient("SendTestClient1");
        connectClient("SendTestClient2");

        String sessionId = testClientSessionId.get();
        Assert.assertNotNull("测试客户端会话ID不应该为null", sessionId);

        String testMessage = "Hello from SessionManager!";
        boolean sent = sessionManager.sendTo(sessionId, testMessage);
        Assert.assertTrue("消息应该成功发送", sent);

        LOG.info("向指定客户端发送消息测试通过，会话ID: " + sessionId + "，消息: " + testMessage);
    }

    @Test
    public void testDisconnectSpecificClient() throws Exception {
        connectClient("DisconnectTestClient1");
        connectClient("DisconnectTestClient2");

        Assert.assertEquals("客户端连接数量应该为2", 2, sessionManager.sessionCount());

        String sessionId = testClientSessionId.get();
        Assert.assertNotNull("测试客户端会话ID不应该为null", sessionId);

        NioSessionWrapper sessionWrapper = sessionManager.sessionWrapper(sessionId);
        Assert.assertNotNull("测试客户端会话包装器不应该为null", sessionWrapper);

        sessionManager.closeSessionWrapper(sessionWrapper);

        Assert.assertTrue("等待客户端断开超时", waitForSessionCount(1, 5, TimeUnit.SECONDS));
        Assert.assertFalse("测试客户端应该不存在", sessionManager.contains(sessionId));
        Assert.assertEquals("客户端连接数量应该为1", 1, sessionManager.sessionCount());

        LOG.info("断开指定客户端连接测试通过，会话ID: " + sessionId);
    }

    private NioClient connectClient(String clientName) throws Exception {
        sessionAcceptedLatch = new CountDownLatch(1);
        IClientCfg clientCfg = DefaultClientCfg.builder()
                .clientName(clientName)
                .remoteHost(hostName)
                .port(serverPort)
                .heartbeatInterval(10)
                .build();
        IHeartbeatService<String> heartbeatService = new DefaultHeartbeatServiceImpl();
        NioClient client = Servs.<NioClientListener, TextLineCodec>createClient()
                .config(clientCfg)
                .codec(new TextLineCodec())
                .heartbeat(heartbeatService)
                .listener(new TestClientListener())
                .build();
        client.connect();
        Assert.assertTrue("客户端应该已连接: " + clientName, waitForClientConnected(client, 5, TimeUnit.SECONDS));
        Assert.assertTrue("服务端应该已接受连接: " + clientName, sessionAcceptedLatch.await(5, TimeUnit.SECONDS));
        clients.add(client);
        LOG.info("客户端已连接: " + clientName);
        return client;
    }

    private boolean waitForClientConnected(NioClient client, long timeout, TimeUnit unit) throws InterruptedException {
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
