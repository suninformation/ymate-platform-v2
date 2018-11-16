/*
 * Copyright 2007-2018 the original author or authors.
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

import net.ymate.platform.serv.nio.AbstractNioSession;
import net.ymate.platform.serv.nio.support.NioEventGroup;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/11/15 2:44 AM
 * @version 1.0
 */
public class NioUdpSession extends AbstractNioSession<NioUdpListener> {

    public NioUdpSession(NioEventGroup<NioUdpListener> eventGroup, DatagramChannel channel) {
        super(eventGroup, channel);
    }

    public NioUdpSession(NioEventGroup<NioUdpListener> eventGroup, DatagramChannel channel, InetSocketAddress socketAddress) {
        super(eventGroup, channel);
        attr(SocketAddress.class.getName(), socketAddress);
    }
}
