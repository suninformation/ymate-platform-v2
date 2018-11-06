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
package net.ymate.platform.serv.nio.client;

import net.ymate.platform.serv.*;
import net.ymate.platform.serv.nio.INioCodec;
import net.ymate.platform.serv.nio.support.NioEventGroup;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/15 下午6:56
 * @version 1.0
 */
public class NioClient extends AbstractService implements IClient<NioClientListener, INioCodec> {

    private static final Log _LOG = LogFactory.getLog(NioClient.class);

    private IClientCfg __clientCfg;

    private NioEventGroup<NioClientListener> __eventGroup;

    private NioClientListener __listener;

    private INioCodec __codec;

    @Override
    public void init(IClientCfg clientCfg,
                     NioClientListener listener,
                     INioCodec codec,
                     IReconnectService reconnectService,
                     IHeartbeatService heartbeatService) {
        __clientCfg = clientCfg;
        __listener = listener;
        __codec = codec;
        __codec.init(__clientCfg.getCharset());
        //
        setReconnectService(reconnectService);
        setHeartbeatService(heartbeatService);
    }

    @Override
    public void connect() throws IOException {
        if (__eventGroup != null && __eventGroup.session() != null) {
            if (__eventGroup.session().isConnected() || __eventGroup.session().isNew()) {
                return;
            }
        }
        __eventGroup = new NioEventGroup<NioClientListener>(__clientCfg, __listener, __codec);
        //
        _LOG.info("Client [" + __eventGroup.name() + "] connecting to " + __clientCfg.getRemoteHost() + ":" + __clientCfg.getPort());
        //
        __eventGroup.start();
        //
        startHeartbeatService();
        startReconnectService();
    }

    @Override
    public void reconnect() throws IOException {
        if (!isConnected()) {
            __eventGroup.close();
            __eventGroup = new NioEventGroup<NioClientListener>(__clientCfg, __listener, __codec);
            //
            _LOG.info("Client [" + __eventGroup.name() + "] reconnecting to " + __clientCfg.getRemoteHost() + ":" + __clientCfg.getPort());
            //
            __eventGroup.start();
        }
    }

    @Override
    public boolean isConnected() {
        return __eventGroup != null && __eventGroup.session() != null && __eventGroup.session().isConnected();
    }

    @Override
    public IClientCfg clientCfg() {
        return __clientCfg;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends NioClientListener> T listener() {
        return (T) __listener;
    }

    @Override
    public void send(Object message) throws IOException {
        __eventGroup.session().send(message);
    }

    @Override
    public void close() throws IOException {
        stopHeartbeatService();
        stopReconnectService();
        //
        if (__eventGroup != null) {
            __eventGroup.close();
        }
    }
}
