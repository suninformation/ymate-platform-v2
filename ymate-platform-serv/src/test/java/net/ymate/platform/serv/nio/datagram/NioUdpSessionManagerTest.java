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

import net.ymate.platform.commons.util.NetworkUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.serv.IClientCfg;
import net.ymate.platform.serv.IHeartbeatService;
import net.ymate.platform.serv.IServerCfg;
import net.ymate.platform.serv.Servs;
import net.ymate.platform.serv.impl.DefaultClientCfg;
import net.ymate.platform.serv.impl.DefaultHeartbeatServiceImpl;
import net.ymate.platform.serv.impl.DefaultServerCfg;
import net.ymate.platform.serv.nio.codec.TextLineCodec;
import org.apache.commons.lang3.ArrayUtils;
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

    private final AtomicInteger clientConnectCount = new AtomicInteger(0);

    private final CountDownLatch clientsConnectedLatch = new CountDownLatch(3);

    private final AtomicReference<InetSocketAddress> testClientAddress = new AtomicReference<>();

    private TestUdpSessionListener sessionListener;

    private class TestUdpSessionListener implements INioUdpSessionListener<NioUdpSessionWrapper, String> {
        @Override
        public Object onMessageReceived(NioUdpSessionWrapper sessionWrapper, String message) throws IOException {
            InetSocketAddress sourceAddress = sessionWrapper.getId();
            LOG.info("服务端收到消息: " + message + " 来自: " + sourceAddress);

            // 记录第一个客户端的地址
            if (clientConnectCount.get() == 0) {
                testClientAddress.set(sourceAddress);
                LOG.info("记录测试客户端地址: " + sourceAddress);
            }

            // 增加客户端连接计数
            int count = clientConnectCount.incrementAndGet();
            if (count <= 3) {
                clientsConnectedLatch.countDown();
                LOG.info("当前连接客户端数量: " + sessionManager.sessionCount());
            }

            // 回复客户端
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
        // 重置测试变量
        clientConnectCount.set(0);
        testClientAddress.set(null);
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

    @After
    public void tearDown() throws Exception {
        try {
            // 关闭所有客户端
            for (NioUdpClient client : clients) {
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

    private void startSessionManager() throws Exception {
        IServerCfg serverCfg = DefaultServerCfg.builder()
                .serverName("UdpSessionManagerTestServer")
                .serverHost(hostName)
                .port(8286)
                .keepAliveTime(10000)
                .build();
        // 初始化会话监听器
        sessionListener = new TestUdpSessionListener();
        // 创建并初始化会话管理器
        sessionManager = new NioUdpSessionManager<>(serverCfg, new TextLineCodec(), sessionListener);
        sessionManager.initialize();
        LOG.info("UDP会话管理器已启动: " + hostName + ":8286");
    }

    @Test
    public void testMultiClientConnection() throws Exception {
        // 连接3个客户端并发送消息
        for (int i = 1; i <= 3; i++) {
            NioUdpClient client = connectUdpClient("UdpClient-" + i);
            // 发送消息以触发会话创建
            client.send("Hello from " + "UdpClient-" + i);
        }

        // 等待客户端连接成功
        Thread.sleep(TimeUnit.SECONDS.toMillis(5));

        // 输出所有客户端会话信息
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
        // 连接1个客户端并发送消息
        NioUdpClient client = connectUdpClient("FindTestClient");
        client.send("Hello from FindTestClient");

        // 等待客户端连接成功
        Thread.sleep(TimeUnit.SECONDS.toMillis(5));

        // 验证客户端是否存在
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
        // 连接2个客户端并发送消息
        NioUdpClient client1 = connectUdpClient("SendTestClient1");
        client1.send("Hello from SendTestClient1");

        NioUdpClient client2 = connectUdpClient("SendTestClient2");
        client2.send("Hello from SendTestClient2");

        // 等待客户端连接成功
        Thread.sleep(TimeUnit.SECONDS.toMillis(5));

        // 向指定客户端发送消息
        InetSocketAddress clientAddress = testClientAddress.get();
        Assert.assertNotNull("测试客户端地址不应该为null", clientAddress);

        String testMessage = "Hello from SessionManager!";
        boolean sent = sessionManager.sendTo(clientAddress, testMessage);
        Assert.assertTrue("消息应该成功发送", sent);

        LOG.info("向指定客户端发送消息测试通过，客户端地址: " + clientAddress + "，消息: " + testMessage);
    }

    @Test
    public void testDisconnectSpecificClient() throws Exception {
        // 连接2个客户端并发送消息
        NioUdpClient client1 = connectUdpClient("DisconnectTestClient1");
        client1.send("Hello from DisconnectTestClient1");

        NioUdpClient client2 = connectUdpClient("DisconnectTestClient2");
        client2.send("Hello from DisconnectTestClient2");

        // 等待客户端连接成功
        Thread.sleep(TimeUnit.SECONDS.toMillis(5));

        // 断开指定客户端连接
        InetSocketAddress clientAddress = testClientAddress.get();
        Assert.assertNotNull("测试客户端地址不应该为null", clientAddress);

        NioUdpSessionWrapper sessionWrapper = sessionManager.sessionWrapper(clientAddress);
        Assert.assertNotNull("测试客户端会话包装器不应该为null", sessionWrapper);

        sessionManager.closeSessionWrapper(sessionWrapper);

        // 等待一段时间，确保客户端已断开
        Thread.sleep(TimeUnit.SECONDS.toMillis(2));

        // 验证客户端是否已断开
        boolean exists = sessionManager.contains(clientAddress);
        Assert.assertFalse("测试客户端应该不存在", exists);

        LOG.info("断开指定客户端连接测试通过，客户端地址: " + clientAddress);
        LOG.info("当前客户端连接数量: " + sessionManager.sessionCount());
    }

    private NioUdpClient connectUdpClient(String clientName) throws Exception {
        IClientCfg clientCfg = DefaultClientCfg.builder()
                .clientName(clientName)
                .remoteHost(hostName)
                .port(8286)
                .heartbeatInterval(10) // 10秒发送一次心跳
                .build();
        IHeartbeatService<String> heartbeatService = new DefaultHeartbeatServiceImpl();
        // 创建一个简单的AbstractNioUdpListener实现用于客户端
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
        NioUdpClient client = Servs.createUdpClient(clientCfg, new TextLineCodec(), heartbeatService, clientListener);
        client.connect();
        clients.add(client);
        LOG.info("UDP客户端已初始化: " + clientName + " -> " + hostName + ":8286");
        return client;
    }
}
