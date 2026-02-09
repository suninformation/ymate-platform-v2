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

    private final AtomicReference<String> receivedMessage = new AtomicReference<>();

    private final AtomicInteger messageCount = new AtomicInteger(0);

    private final AtomicBoolean heartbeatReceived = new AtomicBoolean(false);

    private final CountDownLatch messageLatch = new CountDownLatch(1);

    private final CountDownLatch heartbeatLatch = new CountDownLatch(1);

    @Before
    public void setUp() throws Exception {
        // 获取本地IP地址
        String[] ipAddresses = NetworkUtils.IP.getHostIPAddresses();
        if (ArrayUtils.isNotEmpty(ipAddresses)) {
            hostName = ipAddresses[0];
        } else {
            hostName = NetworkUtils.IP.getHostName();
        }
        // 启动服务端
        startServer();
        // 等待服务端启动
        Thread.sleep(TimeUnit.SECONDS.toMillis(2));
        // 启动客户端
        startClient();
        // 等待客户端连接
        Thread.sleep(TimeUnit.SECONDS.toMillis(2));
    }

    private void startServer() throws Exception {
        IServerCfg serverCfg = DefaultServerCfg.builder()
                .serverName("UdpTestServer")
                .serverHost(hostName)
                .port(8282)
                .keepAliveTime(10000)
                .build();
        // 创建并启动UDP服务端
        server = Servs.createUdpServer(serverCfg, new TextLineCodec(), this);
        server.start();
        Assert.assertTrue("服务端应该已启动", server.isStarted());
        LOG.info("UDP服务端已启动: " + hostName + ":8282");
    }

    private void startClient() throws Exception {
        IClientCfg clientCfg = DefaultClientCfg.builder()
                .clientName("UdpTestClient")
                .remoteHost(hostName)
                .port(8282)
                .heartbeatInterval(5) // 5秒发送一次心跳
                .build();
        // 创建并启动UDP客户端
        IHeartbeatService<String> heartbeatService = new DefaultHeartbeatServiceImpl();
        client = Servs.createUdpClient(clientCfg, new TextLineCodec(), heartbeatService, this);
        client.connect();
        LOG.info("UDP客户端已初始化: " + hostName + ":8282"); // UDP是无连接协议，不检查isConnected()
    }

    @After
    public void tearDown() throws Exception {
        try {
            // 等待一段时间，确保所有消息和心跳都已处理
            Thread.sleep(TimeUnit.SECONDS.toMillis(10));
            // 关闭客户端
            if (client != null) {
                client.close();
            }
            // 关闭服务端
            if (server != null) {
                server.close();
            }
        } catch (Exception e) {
            LOG.error("测试清理失败", RuntimeUtils.unwrapThrow(e));
        }
    }

    @Test
    public void testBasicCommunication() throws Exception {
        // 发送测试消息
        String testMessage = "Hello UDP Server!";
        client.send(testMessage);
        LOG.info("已发送消息: " + testMessage);

        // 等待消息接收
        boolean received = messageLatch.await(5, TimeUnit.SECONDS);
        Assert.assertTrue("应该收到服务端响应", received);
        Assert.assertNotNull("接收到的消息不应为null", receivedMessage.get());
        LOG.info("收到服务端响应: " + receivedMessage.get());

        // 验证消息计数
        Assert.assertTrue("消息计数应该大于0", messageCount.get() > 0);
    }

    @Test
    public void testHeartbeat() throws Exception {
        // 等待心跳包
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

        // 检查是否为心跳包
        if ("0".equals(msg)) {
            heartbeatReceived.set(true);
            heartbeatLatch.countDown();
            LOG.info("收到心跳包");
        } else {
            receivedMessage.set(msg);
            messageLatch.countDown();
            // 回复客户端
            return "Server response: " + msg;
        }
        return null;
    }

    @Override
    public void onExceptionCaught(InetSocketAddress sourceAddress, Throwable e) throws IOException {
        LOG.error("服务端异常: " + e.getMessage() + " 来自: " + sourceAddress, e);
    }
}
