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

import net.ymate.platform.commons.util.NetworkUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.serv.IClientCfg;
import net.ymate.platform.serv.IHeartbeatService;
import net.ymate.platform.serv.IServerCfg;
import net.ymate.platform.serv.Servs;
import net.ymate.platform.serv.impl.DefaultClientCfg;
import net.ymate.platform.serv.impl.DefaultHeartbeatServiceImpl;
import net.ymate.platform.serv.impl.DefaultServerCfg;
import net.ymate.platform.serv.nio.INioSession;
import net.ymate.platform.serv.nio.client.NioClient;
import net.ymate.platform.serv.nio.client.NioClientListener;
import net.ymate.platform.serv.nio.codec.TextLineCodec;
import org.apache.commons.lang3.ArrayUtils;
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

    private final AtomicInteger clientConnectCount = new AtomicInteger(0);

    private final CountDownLatch clientsConnectedLatch = new CountDownLatch(3);

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
            clientsConnectedLatch.countDown();
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
            // 回复客户端
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
        // 获取本地IP地址
        String[] ipAddresses = NetworkUtils.IP.getHostIPAddresses();
        if (ArrayUtils.isNotEmpty(ipAddresses)) {
            hostName = ipAddresses[0];
        } else {
            hostName = NetworkUtils.IP.getHostName();
        }
        // 启动会话管理器
        startSessionManager();
        // 等待会话管理器启动
        Thread.sleep(TimeUnit.SECONDS.toMillis(2));
    }

    private void startSessionManager() throws Exception {
        IServerCfg serverCfg = DefaultServerCfg.builder()
                .serverName("TcpSessionManagerTestServer")
                .serverHost(hostName)
                .port(8285)
                .keepAliveTime(10000)
                .build();
        // 初始化会话监听器
        sessionListener = new TestSessionListener();
        // 创建并初始化会话管理器
        sessionManager = new NioSessionManager<>(serverCfg, new TextLineCodec(), sessionListener);
        sessionManager.initialize();
        LOG.info("TCP会话管理器已启动: " + hostName + ":8285");
    }

    @After
    public void tearDown() throws Exception {
        try {
            // 关闭所有客户端
            for (NioClient client : clients) {
                if (client != null) {
                    client.close();
                }
            }
            clients.clear();
            // 等待一段时间，确保所有客户端都已关闭
            Thread.sleep(TimeUnit.SECONDS.toMillis(5));
            // 关闭会话管理器
            if (sessionManager != null) {
                sessionManager.close();
            }
        } catch (Exception e) {
            LOG.error("测试清理失败", RuntimeUtils.unwrapThrow(e));
        }
    }

    @Test
    public void testMultiClientConnection() throws Exception {
        // 连接3个客户端
        for (int i = 1; i <= 3; i++) {
            connectClient("Client-" + i);
        }

        // 等待所有客户端连接成功
        boolean allConnected = clientsConnectedLatch.await(30, TimeUnit.SECONDS);
        Assert.assertTrue("所有客户端应该成功连接", allConnected);
        Assert.assertEquals("客户端连接数量应该为3", 3, sessionManager.sessionCount());

        // 输出所有客户端会话信息
        LOG.info("\n=== 所有客户端会话信息 ===");
        Collection<NioSessionWrapper> sessionWrappers = sessionManager.sessionWrappers();
        for (NioSessionWrapper sessionWrapper : sessionWrappers) {
            LOG.info("会话ID: " + sessionWrapper.getId());
        }
        LOG.info("========================");
    }

    @Test
    public void testFindClient() throws Exception {
        // 连接1个客户端
        connectClient("FindTestClient");

        // 等待客户端连接成功
        Thread.sleep(TimeUnit.SECONDS.toMillis(5));

        // 验证客户端是否存在
        String sessionId = testClientSessionId.get();
        Assert.assertNotNull("测试客户端会话ID不应该为null", sessionId);

        boolean exists = sessionManager.contains(sessionId);
        Assert.assertTrue("测试客户端应该存在", exists);

        NioSessionWrapper sessionWrapper = sessionManager.sessionWrapper(sessionId);
        Assert.assertNotNull("测试客户端会话包装器不应该为null", sessionWrapper);
        Assert.assertEquals("会话ID应该匹配", sessionId, sessionWrapper.getId());

        LOG.info("查找客户端测试通过，会话ID: " + sessionId);
    }

    @Test
    public void testSendToSpecificClient() throws Exception {
        // 连接2个客户端
        connectClient("SendTestClient1");
        connectClient("SendTestClient2");

        // 等待客户端连接成功
        Thread.sleep(TimeUnit.SECONDS.toMillis(5));

        // 向指定客户端发送消息
        String sessionId = testClientSessionId.get();
        Assert.assertNotNull("测试客户端会话ID不应该为null", sessionId);

        String testMessage = "Hello from SessionManager!";
        boolean sent = sessionManager.sendTo(sessionId, testMessage);
        Assert.assertTrue("消息应该成功发送", sent);

        LOG.info("向指定客户端发送消息测试通过，会话ID: " + sessionId + "，消息: " + testMessage);
    }

    @Test
    public void testDisconnectSpecificClient() throws Exception {
        // 连接2个客户端
        connectClient("DisconnectTestClient1");
        connectClient("DisconnectTestClient2");

        // 等待客户端连接成功
        Thread.sleep(TimeUnit.SECONDS.toMillis(5));
        Assert.assertEquals("客户端连接数量应该为2", 2, sessionManager.sessionCount());

        // 断开指定客户端连接
        String sessionId = testClientSessionId.get();
        Assert.assertNotNull("测试客户端会话ID不应该为null", sessionId);

        NioSessionWrapper sessionWrapper = sessionManager.sessionWrapper(sessionId);
        Assert.assertNotNull("测试客户端会话包装器不应该为null", sessionWrapper);

        sessionManager.closeSessionWrapper(sessionWrapper);

        // 等待一段时间，确保客户端已断开
        Thread.sleep(TimeUnit.SECONDS.toMillis(2));

        // 验证客户端是否已断开
        boolean exists = sessionManager.contains(sessionId);
        Assert.assertFalse("测试客户端应该不存在", exists);
        Assert.assertEquals("客户端连接数量应该为1", 1, sessionManager.sessionCount());

        LOG.info("断开指定客户端连接测试通过，会话ID: " + sessionId);
    }

    private void connectClient(String clientName) throws Exception {
        IClientCfg clientCfg = DefaultClientCfg.builder()
                .clientName(clientName)
                .remoteHost(hostName)
                .port(8285)
                .heartbeatInterval(10) // 10秒发送一次心跳
                .build();
        IHeartbeatService<String> heartbeatService = new DefaultHeartbeatServiceImpl();
        NioClient client = Servs.createClient(clientCfg, new TextLineCodec(), null, heartbeatService, new TestClientListener());
        client.connect();
        Assert.assertTrue("客户端应该已连接", client.isConnected());
        clients.add(client);
        LOG.info("客户端已连接: " + clientName);
    }
}
