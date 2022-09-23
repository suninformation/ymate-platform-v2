/*
 * Copyright 2007-2022 the original author or authors.
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
import net.ymate.platform.serv.IServerCfg;
import net.ymate.platform.serv.Servs;
import net.ymate.platform.serv.impl.DefaultClientCfg;
import net.ymate.platform.serv.impl.DefaultHeartbeatServiceImpl;
import net.ymate.platform.serv.impl.DefaultServerCfg;
import net.ymate.platform.serv.impl.DefaultSessionIdleChecker;
import net.ymate.platform.serv.nio.codec.TextLineCodec;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * @author 刘镇 (suninformation@163.com) on 2022/8/28 11:45
 * @since 2.1.2
 */
public class NioUdpServerTest extends AbstractNioUdpListener implements INioUdpSessionListener<NioUdpSessionWrapper, String> {

    private static final Log LOG = LogFactory.getLog(NioUdpServerTest.class);

    private NioUdpSessionManager<NioUdpSessionWrapper, String> sessionManager;

    private NioUdpClient client;

    private String hostName;

    @Before
    public void setUp() throws Exception {
        String[] ipAddresses = NetworkUtils.IP.getHostIPAddresses();
        if (ArrayUtils.isNotEmpty(ipAddresses)) {
            hostName = ipAddresses[0];
        } else {
            hostName = NetworkUtils.IP.getHostName();
        }
        doBuildServer();
        doBuildClient();
    }

    private void doBuildServer() throws Exception {
        IServerCfg serverCfg = DefaultServerCfg.builder()
                .serverName("UdpServer")
                .serverHost(hostName)
                .port(8281)
                .keepAliveTime(10000).build();
        // 通过会话管理器创建服务端并设置会话空闲时间为30秒
        sessionManager = new NioUdpSessionManager<>(serverCfg, new TextLineCodec(), this, 10000L);
        // 设置空闲会话检查服务
        sessionManager.idleChecker(new DefaultSessionIdleChecker<>());
        // 设置流量速度计数器检测时间间隔为10秒
        sessionManager.speedometer(10000);
        // 初始化并启动服务
        sessionManager.initialize();
    }

    private void doBuildClient() throws Exception {
        Thread.sleep(TimeUnit.SECONDS.toMillis(5));
        IClientCfg clientCfg = DefaultClientCfg.builder()
                .clientName("UdpClient")
                .remoteHost(hostName)
                .port(8281)
                .build();
        client = Servs.createUdpClient(clientCfg, new TextLineCodec(), new DefaultHeartbeatServiceImpl(), this);
        client.connect();
    }

    @After
    public void tearDown() throws Exception {
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(20));
            // 当前会话总数
            LOG.info("Current session count: " + sessionManager.sessionCount());
            // 将已连接的客户端会话从管理器中移除
            sessionManager.sessionWrappers().forEach(sessionManager::closeSessionWrapper);
            // 销毁会话管理器
            sessionManager.close();
            // 销毁客户端
            client.close();
        } catch (Exception e) {
            LOG.error(e.getMessage(), RuntimeUtils.unwrapThrow(e));
        }
    }

    @Test
    public void sendTo() throws IOException {
        // 获取当前会话总数
        LOG.info("Current session count: " + sessionManager.sessionCount());
        // 遍历会话并向其发送消息
        sessionManager.sessionWrappers().forEach(nioUdpSessionWrapper -> {
            try {
                sessionManager.sendTo(nioUdpSessionWrapper.getId(), "Send message from UdpServer.");
            } catch (IOException e) {
                LOG.error(e.getMessage(), RuntimeUtils.unwrapThrow(e));
            }
        });
    }

    @Override
    public Object onMessageReceived(NioUdpSessionWrapper sessionWrapper, String message) throws IOException {
        LOG.info("onMessageReceived: " + message + " from " + sessionWrapper.getId());
        // 当收到消息后，可以直接向客户端回复消息
        return "Hi, " + sessionWrapper.getId();
    }

    @Override
    public void onExceptionCaught(NioUdpSessionWrapper sessionWrapper, Throwable e) throws IOException {
        LOG.info("onExceptionCaught: " + e.getMessage() + " -- " + sessionWrapper.getId());
    }

    @Override
    public void onSessionIdleRemoved(NioUdpSessionWrapper sessionWrapper) {
        LOG.info("onSessionIdleRemoved: " + sessionWrapper.getId());
    }

    // ---- AbstractNioUdpListener

    @Override
    public Object onSessionReady() throws IOException {
        return "Hello!";
    }

    @Override
    public Object onMessageReceived(InetSocketAddress sourceAddress, Object message) throws IOException {
        LOG.info("onMessageReceived: " + message + ", from " + sourceAddress);
        return null;
    }

    @Override
    public void onExceptionCaught(InetSocketAddress sourceAddress, Throwable e) throws IOException {
        LOG.info(sourceAddress + "--->" + e);
    }
}