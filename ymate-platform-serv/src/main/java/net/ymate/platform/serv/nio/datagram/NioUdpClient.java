/*
 * Copyright 2007-2019 the original author or authors.
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

import net.ymate.platform.serv.IClientCfg;
import net.ymate.platform.serv.nio.AbstractNioClient;
import net.ymate.platform.serv.nio.INioCodec;
import net.ymate.platform.serv.nio.INioEventGroup;

import java.io.IOException;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/17 下午3:04
 */
public class NioUdpClient extends AbstractNioClient<AbstractNioUdpListener> {

    @Override
    protected INioEventGroup<AbstractNioUdpListener> buildEventGroup(IClientCfg clientCfg, AbstractNioUdpListener listener, INioCodec codec) throws IOException {
        return new NioUdpEventGroup(clientCfg, listener, codec);
    }

    @Override
    public void reconnect() {
        // Don't need to reconnect
    }
}
