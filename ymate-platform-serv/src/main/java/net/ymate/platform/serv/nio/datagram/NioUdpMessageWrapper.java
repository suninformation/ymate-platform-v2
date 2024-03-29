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

import java.io.Serializable;
import java.net.InetSocketAddress;

/**
 * @param <MESSAGE_TYPE> 消息类型
 * @author 刘镇 (suninformation@163.com) on 2018/11/21 3:27 PM
 */
public class NioUdpMessageWrapper<MESSAGE_TYPE> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final InetSocketAddress socketAddress;

    private final MESSAGE_TYPE message;

    public NioUdpMessageWrapper(InetSocketAddress socketAddress, MESSAGE_TYPE message) {
        this.socketAddress = socketAddress;
        this.message = message;
    }

    public InetSocketAddress getSocketAddress() {
        return socketAddress;
    }

    public MESSAGE_TYPE getMessage() {
        return message;
    }
}
