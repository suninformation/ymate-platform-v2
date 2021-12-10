/*
 * Copyright 2007-2017 the original author or authors.
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
package net.ymate.platform.serv;

import net.ymate.platform.core.Version;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.module.IModule;
import net.ymate.platform.core.module.annotation.Module;
import net.ymate.platform.core.util.ClassUtils;
import net.ymate.platform.serv.annotation.Client;
import net.ymate.platform.serv.annotation.Server;
import net.ymate.platform.serv.handle.ClientHandler;
import net.ymate.platform.serv.handle.ServerHandler;
import net.ymate.platform.serv.impl.DefaultServModuleCfg;
import net.ymate.platform.serv.nio.INioCodec;
import net.ymate.platform.serv.nio.client.NioClient;
import net.ymate.platform.serv.nio.client.NioClientListener;
import net.ymate.platform.serv.nio.datagram.NioUdpClient;
import net.ymate.platform.serv.nio.datagram.NioUdpListener;
import net.ymate.platform.serv.nio.datagram.NioUdpServer;
import net.ymate.platform.serv.nio.server.NioServer;
import net.ymate.platform.serv.nio.server.NioServerListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务模块管理器
 *
 * @author 刘镇 (suninformation@163.com) on 15/10/15 上午10:22
 * @version 1.0
 */
@Module
public class Servs implements IModule, IServ {

    public static final Version VERSION = new Version(2, 0, 11, Servs.class.getPackage().getImplementationVersion(), Version.VersionType.Release);

    private static final Log _LOG = LogFactory.getLog(Servs.class);

    private static volatile IServ __instance;

    private YMP __owner;

    private IServModuleCfg __moduleCfg;

    private boolean __inited;

    private Map<String, IServer> __servers;

    private Map<String, IClient> __clients;

    /**
     * @return 返回默认服务模块管理器实例对象
     */
    public static IServ get() {
        if (__instance == null) {
            synchronized (VERSION) {
                if (__instance == null) {
                    __instance = YMP.get().getModule(Servs.class);
                }
            }
        }
        return __instance;
    }

    /**
     * @param owner YMP框架管理器实例
     * @return 返回指定YMP框架管理器容器内的服务模块实例
     */
    public static IServ get(YMP owner) {
        return owner.getModule(Servs.class);
    }

    public Servs() {
        __servers = new ConcurrentHashMap<String, IServer>();
        __clients = new ConcurrentHashMap<String, IClient>();
    }

    @Override
    public String getName() {
        return IServ.MODULE_NAME;
    }

    @Override
    public void init(YMP owner) throws Exception {
        if (!__inited) {
            //
            _LOG.info("Initializing ymate-platform-serv-" + VERSION);
            //
            __owner = owner;
            __moduleCfg = new DefaultServModuleCfg(owner);
            //
            __owner.registerExcludedClass(IServer.class);
            __owner.registerExcludedClass(IServerCfg.class);
            __owner.registerExcludedClass(IClient.class);
            __owner.registerExcludedClass(IClientCfg.class);
            __owner.registerExcludedClass(ICodec.class);
            __owner.registerExcludedClass(IListener.class);
            //
            __owner.registerHandler(Server.class, new ServerHandler(this));
            __owner.registerHandler(Client.class, new ClientHandler(this));
            //
            __inited = true;
        }
    }

    @Override
    public boolean isInited() {
        return __inited;
    }

    @Override
    public YMP getOwner() {
        return __owner;
    }

    @Override
    public IServModuleCfg getModuleCfg() {
        return __moduleCfg;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IServer> T getServer(Class<? extends IListener> clazz) {
        return (T) __servers.get(clazz.getName());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IClient> T getClient(Class<? extends IListener> clazz) {
        return (T) __clients.get(clazz.getName());
    }

    @Override
    public void registerServer(Class<? extends IListener> listenerClass) {
        Server _annoServer = listenerClass.getAnnotation(Server.class);
        if (_annoServer == null) {
            throw new IllegalArgumentException("No Server annotation present on class");
        }
        registerServer(_annoServer.name(), _annoServer.implClass(), _annoServer.codec(), listenerClass);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void registerServer(String serverName, Class<? extends IServer> implClass, Class<? extends ICodec> codec, Class<? extends IListener> listenerClass) {
        if (!__servers.containsKey(listenerClass.getName())) {
            IServer _server = ClassUtils.impl(implClass, IServer.class);
            IServerCfg _serverCfg = __moduleCfg.getServerCfg(serverName);
            _server.init(_serverCfg, ClassUtils.impl(listenerClass, IListener.class), ClassUtils.impl(codec, ICodec.class));
            __servers.put(listenerClass.getName(), _server);
        }
    }

    @Override
    public <LISTENER extends NioServerListener, CODEC extends INioCodec> NioServer buildServer(IServerCfg serverCfg, CODEC codec, LISTENER listener) {
        NioServer _server = new NioServer();
        _server.init(serverCfg, listener, codec);
        return _server;
    }

    @Override
    public <LISTENER extends NioUdpListener, CODEC extends INioCodec> NioUdpServer buildUdpServer(IServerCfg serverCfg, CODEC codec, LISTENER listener) {
        NioUdpServer _server = new NioUdpServer();
        _server.init(serverCfg, listener, codec);
        return _server;
    }

    @Override
    public void registerClient(Class<? extends IListener> listenerClass) {
        Client _annoClient = listenerClass.getAnnotation(Client.class);
        if (_annoClient == null) {
            throw new IllegalArgumentException("No Client annotation present on class");
        }
        registerClient(_annoClient.name(), _annoClient.implClass(), _annoClient.codec(), listenerClass, _annoClient.reconnectClass(), _annoClient.heartbeatClass());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void registerClient(String clientName, Class<? extends IClient> implClass, Class<? extends ICodec> codec, Class<? extends IListener> listenerClass, Class<? extends IReconnectService> reconnectClass, Class<? extends IHeartbeatService> heartbeatClass) {
        if (!__clients.containsKey(listenerClass.getName())) {
            IClient _client = ClassUtils.impl(implClass, IClient.class);
            //
            IReconnectService _reconnectService = null;
            if (!NioUdpListener.class.equals(listenerClass)) {
                if (!IReconnectService.NONE.class.equals(reconnectClass)) {
                    _reconnectService = ClassUtils.impl(reconnectClass, IReconnectService.class);
                    _reconnectService.init(_client);
                }
            }
            IHeartbeatService _heartbeatService = null;
            if (!IHeartbeatService.NONE.class.equals(heartbeatClass)) {
                _heartbeatService = ClassUtils.impl(heartbeatClass, IHeartbeatService.class);
                _heartbeatService.init(_client);
            }
            IClientCfg _clientCfg = __moduleCfg.getClientCfg(clientName);
            //
            _client.init(_clientCfg, ClassUtils.impl(listenerClass, IListener.class), ClassUtils.impl(codec, ICodec.class), _reconnectService, _heartbeatService);
            __clients.put(listenerClass.getName(), _client);
        }
    }

    @Override
    public <LISTENER extends NioClientListener, CODEC extends INioCodec> NioClient buildClient(IClientCfg clientCfg, CODEC codec, IReconnectService reconnect, IHeartbeatService heartbeat, LISTENER listener) {
        NioClient _client = new NioClient();
        if (reconnect != null && !reconnect.isInited()) {
            reconnect.init(_client);
        }
        if (heartbeat != null && !heartbeat.isInited()) {
            heartbeat.init(_client);
        }
        _client.init(clientCfg, listener, codec, reconnect, heartbeat);
        return _client;
    }

    @Override
    public <LISTENER extends NioUdpListener, CODEC extends INioCodec> NioUdpClient buildUdpClient(IClientCfg clientCfg, CODEC codec, IHeartbeatService heartbeat, LISTENER listener) {
        NioUdpClient _client = new NioUdpClient();
        if (heartbeat != null && !heartbeat.isInited()) {
            heartbeat.init(_client);
        }
        _client.init(clientCfg, listener, codec, null, heartbeat);
        return _client;
    }

    @Override
    public void startup() throws Exception {
        for (IServer _server : __servers.values()) {
            if (!_server.isStarted()) {
                _server.start();
            }
        }
        //
        for (IClient _client : __clients.values()) {
            if (!_client.isConnected()) {
                _client.connect();
            }
        }
    }

    @Override
    public void destroy() throws Exception {
        if (__inited) {
            __inited = false;
            //
            for (IClient _client : __clients.values()) {
                if (_client.isConnected()) {
                    _client.close();
                }
            }
            __clients = null;
            //
            for (IServer _server : __servers.values()) {
                if (_server.isStarted()) {
                    _server.close();
                }
            }
            __servers = null;
            //
            __moduleCfg = null;
            __owner = null;
        }
    }
}
