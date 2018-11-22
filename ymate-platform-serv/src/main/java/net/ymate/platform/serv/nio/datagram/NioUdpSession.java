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
import net.ymate.platform.serv.nio.support.ByteBufferBuilder;
import net.ymate.platform.serv.nio.support.NioEventGroup;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/11/15 2:44 AM
 * @version 1.0
 */
public class NioUdpSession extends AbstractNioSession<NioUdpListener> {

    private final Queue<NioUdpMessageWrapper<ByteBuffer>> __writeQueue = new LinkedBlockingQueue<NioUdpMessageWrapper<ByteBuffer>>();

    private InetSocketAddress __socketAddress;

    public NioUdpSession(NioEventGroup<NioUdpListener> eventGroup, DatagramChannel channel) {
        super(eventGroup, channel);
    }

    public NioUdpSession(NioEventGroup<NioUdpListener> eventGroup, DatagramChannel channel, InetSocketAddress socketAddress) {
        super(eventGroup, channel);
        __socketAddress = socketAddress;
    }

    private int __doChannelRead(ByteBuffer buffer) throws IOException {
        SocketAddress _address = ((DatagramChannel) channel()).receive(buffer);
        if (_address != null) {
            attr(SocketAddress.class.getName(), _address);
            return buffer().remaining();
        }
        return 0;
    }

    private int __doChannelWrite(DatagramChannel channel, ByteBuffer buffer) throws IOException {
        if (channel != null) {
            return channel.write(buffer);
        }
        buffer.reset();
        return 0;
    }

    @Override
    public InetSocketAddress remoteSocketAddress() {
        if (__socketAddress == null) {
            return (InetSocketAddress) ((DatagramChannel) channel()).socket().getRemoteSocketAddress();
        }
        return __socketAddress;
    }

    @Override
    public void close() throws IOException {
        super.close();
    }

    @Override
    public void read() throws IOException {
        if (buffer() == null) {
            buffer(ByteBufferBuilder.allocate(eventGroup().bufferSize()));
        }
        ByteBuffer _data = ByteBuffer.allocate(eventGroup().bufferSize());
        //
        int _length;
        while ((_length = __doChannelRead(_data)) > 0) {
            _data.flip();
            buffer().append(_data.array(), _data.position(), _data.remaining());
            _data.clear();
        }
        if (_length < 0) {
            close();
            return;
        }
        ByteBufferBuilder _copiedBuffer = buffer().duplicate().flip();
        while (true) {
            _copiedBuffer.mark();
            NioUdpMessageWrapper _message;
            InetSocketAddress _socketAddress = attr(SocketAddress.class.getName());
            if (_socketAddress != null && _copiedBuffer.remaining() > 0) {
                _message = new NioUdpMessageWrapper<Object>(_socketAddress, eventGroup().codec().decode(_copiedBuffer));
            } else {
                _message = null;
            }
            if (_message == null) {
                _copiedBuffer.reset();
                __doBufferReset(_copiedBuffer);
                break;
            } else {
                __doPostMessageReceived(_message);
            }
        }
    }

    @Override
    public void write() throws IOException {
        synchronized (__writeQueue) {
            while (true) {
                NioUdpMessageWrapper<ByteBuffer> _msgWrapper = __writeQueue.peek();
                if (_msgWrapper == null) {
                    selectionKey().interestOps(SelectionKey.OP_READ);
                    break;
                } else {
                    DatagramChannel _channel = null;
                    try {
                        _channel = (DatagramChannel) channel();
                        if (!_channel.isConnected()) {
                            _channel = _channel.connect(_msgWrapper.getSocketAddress());
                        }
                        int _wLen = __doChannelWrite(_channel, _msgWrapper.getMessage());
                        if (_wLen == 0 && _msgWrapper.getMessage().remaining() > 0) {
                            break;
                        }
                        if (_msgWrapper.getMessage().remaining() == 0) {
                            __writeQueue.remove();
                        } else {
                            break;
                        }
                    } finally {
                        if (_channel != null) {
                            _channel.disconnect();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void send(Object message) {
        send(remoteSocketAddress(), message);
    }

    public void send(InetSocketAddress socketAddress, Object message) {
        if (socketAddress != null) {
            ByteBufferBuilder _msgBuffer = eventGroup().codec().encode(message);
            if (_msgBuffer != null) {
                if (__writeQueue.offer(new NioUdpMessageWrapper<ByteBuffer>(socketAddress, _msgBuffer.buffer()))) {
                    selectionKey().interestOps(selectionKey().interestOps() | SelectionKey.OP_WRITE);
                    selectionKey().selector().wakeup();
                }
            }
        }
    }
}
