/*
 * Copyright 2007-2016 the original author or authors.
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

import net.ymate.platform.serv.IClient;
import net.ymate.platform.serv.IClientCfg;
import net.ymate.platform.serv.nio.INioCodec;
import net.ymate.platform.serv.nio.support.NioEventGroup;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/15 下午6:56
 * @version 1.0
 */
public class NioClient implements IClient<IClientCfg, NioClientListener, INioCodec> {

    private final Log _LOG = LogFactory.getLog(NioClient.class);

    protected IClientCfg __clientCfg;

    protected NioEventGroup<NioClientListener> __eventGroup;

    protected NioClientListener __listener;

    protected INioCodec __codec;

    public void init(IClientCfg cfg, NioClientListener listener, INioCodec codec) {
        __clientCfg = cfg;
        __listener = listener;
        __codec = codec;
    }

    public void connect() throws IOException {
        if (__eventGroup != null && __eventGroup.session() != null) {
            if (__eventGroup.session().isConnected() || __eventGroup.session().isNew()) {
                return;
            }
        }
        __eventGroup = new NioEventGroup<NioClientListener>(__clientCfg, __listener, __codec);
        __eventGroup.start();
    }

    public boolean isConnected() {
        return __eventGroup != null && __eventGroup.session() != null && __eventGroup.session().isConnected();
    }

    public void close() throws IOException {
        if (__eventGroup != null) {
            __eventGroup.close();
        }
    }
}
