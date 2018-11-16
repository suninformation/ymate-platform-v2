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
package net.ymate.platform.serv.nio;

import net.ymate.platform.serv.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/19 下午4:55
 * @version 1.0
 */
public abstract class AbstractNioClient<LISTENER extends IListener<INioSession>> implements IClient<LISTENER, INioCodec> {

    private static final Log _LOG = LogFactory.getLog(AbstractNioClient.class);

    private IClientCfg __clientCfg;

    private INioEventGroup<LISTENER> __eventGroup;

    private LISTENER __listener;

    private INioCodec __codec;

    private IReconnectService __reconnectService;

    private IHeartbeatService __heartbeatService;

    private void __startServices() {
        if (__reconnectService != null && __reconnectService.isInited()) {
            __reconnectService.start();
        }
        if (__heartbeatService != null && __heartbeatService.isInited()) {
            __heartbeatService.start();
        }
    }

    private void __stopServices() {
        if (__reconnectService != null && __reconnectService.isStarted()) {
            IOUtils.closeQuietly(__reconnectService);
        }
        if (__heartbeatService != null && __heartbeatService.isStarted()) {
            IOUtils.closeQuietly(__heartbeatService);
        }
    }

    protected abstract INioEventGroup<LISTENER> buildEventGroup(IClientCfg clientCfg, LISTENER listener, INioCodec codec) throws IOException;

    @Override
    public void init(IClientCfg clientCfg, LISTENER listener, INioCodec codec, IReconnectService reconnectService, IHeartbeatService heartbeatService) {
        __clientCfg = clientCfg;
        __listener = listener;
        __codec = codec;
        __codec.init(__clientCfg.getCharset());
        //
        __reconnectService = reconnectService;
        __heartbeatService = heartbeatService;
    }

    @Override
    public void connect() throws IOException {
        if (__eventGroup != null && __eventGroup.session() != null) {
            if (__eventGroup.session().isConnected() || __eventGroup.session().isNew()) {
                return;
            }
        }
        __eventGroup = buildEventGroup(__clientCfg, __listener, __codec);
        //
        _LOG.info(getClass().getSimpleName() + " [" + __eventGroup.name() + "] connecting to " + __clientCfg.getRemoteHost() + ":" + __clientCfg.getPort());
        //
        __eventGroup.start();
        //
        __startServices();
    }

    @Override
    public void reconnect() throws IOException {
        if (!isConnected()) {
            __eventGroup.close();
            __eventGroup = buildEventGroup(__clientCfg, __listener, __codec);
            //
            _LOG.info(getClass().getSimpleName() + " [" + __eventGroup.name() + "] reconnecting to " + __clientCfg.getRemoteHost() + ":" + __clientCfg.getPort());
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
    public <T extends LISTENER> T listener() {
        return (T) __listener;
    }

    @Override
    public void send(Object message) throws IOException {
        __eventGroup.session().send(message);
    }

    @Override
    public void close() throws IOException {
        _LOG.info(getClass().getSimpleName() + " [" + __eventGroup.name() + "] is closing....");
        //
        __stopServices();
        //
        if (__eventGroup != null) {
            __eventGroup.close();
        }
    }
}
