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
package net.ymate.platform.serv.nio.server;

import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.serv.IClient;
import net.ymate.platform.serv.IClientCfg;
import net.ymate.platform.serv.IServerCfg;
import net.ymate.platform.serv.Servs;
import net.ymate.platform.serv.impl.*;
import net.ymate.platform.serv.nio.INioSession;
import net.ymate.platform.serv.nio.client.NioClient;
import net.ymate.platform.serv.nio.client.NioClientListener;
import net.ymate.platform.serv.nio.codec.TextLineCodec;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author 刘镇 (suninformation@163.com) on 2022/8/28 11:00
 * @since 2.1.2
 */
public class NioServerTest extends NioClientListener implements INioSessionListener<NioSessionWrapper, String> {

    private static final Log LOG = LogFactory.getLog(NioServerTest.class);

    private NioSessionManager<NioSessionWrapper, String> sessionManager;

    private NioClient client;

    @Before
    public void setUp() throws Exception {
        doBuildServer();
        doBuildClient();
    }

    private void doBuildServer() throws Exception {
        IServerCfg serverCfg = DefaultServerCfg.builder()
                .serverName("TcpServer")
                .serverHost("localhost")
                .port(8281)
                .keepAliveTime(10000).build();
        // 通过会话管理器创建服务端并设置会话空闲时间为30秒
        sessionManager = new NioSessionManager<>(serverCfg, new TextLineCodec(), this, 10000L);
        // 设置空闲会话检查服务
        sessionManager.idleChecker(new DefaultSessionIdleChecker<>());
        // 设置流量速度计数器检测时间间隔为10秒
        sessionManager.speedometer(10000);
        // 初始化并启动服务
        sessionManager.initialize();
    }

    private void doBuildClient() throws Exception {
        IClientCfg clientCfg = DefaultClientCfg.builder()
                .clientName("TcpClient")
                .remoteHost("0.0.0.0")
                .port(8281)
                .build();
        client = Servs.createClient(clientCfg, new TextLineCodec(), new DefaultReconnectServiceImpl(), new DefaultHeartbeatServiceImpl(), this);
        client.connect();
    }

    @After
    public void tearDown() throws Exception {
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(15));
            // 将已连接的客户端会话从管理器中移除
            for (NioSessionWrapper sessionWrapper : sessionManager.sessionWrappers()) {
                sessionManager.closeSessionWrapper(sessionWrapper);
            }
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
        for (NioSessionWrapper sessionWrapper : sessionManager.sessionWrappers()) {
            sessionManager.sendTo(sessionWrapper.getId(), "Send message from TcpServer.");
        }
    }

    @Override
    public void onSessionRegistered(NioSessionWrapper session) throws IOException {
        LOG.info("onSessionRegistered: " + session.getId());
    }

    @Override
    public void onSessionAccepted(NioSessionWrapper session) throws IOException {
        LOG.info("onSessionAccepted: " + session.getId());
    }

    @Override
    public void onBeforeSessionClosed(NioSessionWrapper session) throws IOException {
        LOG.info("onBeforeSessionClosed: " + session.getId());
    }

    @Override
    public void onAfterSessionClosed(NioSessionWrapper session) throws IOException {
        LOG.info("onAfterSessionClosed: " + session.getId());
    }

    @Override
    public void onMessageReceived(String message, NioSessionWrapper session) throws IOException {
        LOG.info("onMessageReceived: " + message + " from " + session.getId());
    }

    @Override
    public void onExceptionCaught(Throwable e, NioSessionWrapper session) throws IOException {
        LOG.info("onExceptionCaught: " + e.getMessage() + " -- " + session.getId());
    }

    @Override
    public void onSessionIdleRemoved(NioSessionWrapper sessionWrapper) {
        LOG.info("onSessionIdleRemoved: " + sessionWrapper.getId());
    }

    // ---- NioClientListener

    @Override
    public void onClientReconnected(IClient<?, ?> client) {
        LOG.info("onClientReconnected: " + client);
    }

    @Override
    public void onSessionRegistered(INioSession session) throws IOException {
        LOG.info("onSessionRegistered: " + session);
    }

    @Override
    public void onSessionConnected(INioSession session) throws IOException {
        super.onSessionConnected(session);
        LOG.info("onSessionConnected: " + session);
    }

    @Override
    public void onBeforeSessionClosed(INioSession session) throws IOException {
        LOG.info("onBeforeSessionClosed: " + session);
    }

    @Override
    public void onAfterSessionClosed(INioSession session) throws IOException {
        LOG.info("onAfterSessionClosed: " + session);
    }

    @Override
    public void onMessageReceived(Object message, INioSession session) throws IOException {
        super.onMessageReceived(message, session);
        LOG.info("onMessageReceived: " + message + " --> " + session);
        session.send("Bye!");
    }
}