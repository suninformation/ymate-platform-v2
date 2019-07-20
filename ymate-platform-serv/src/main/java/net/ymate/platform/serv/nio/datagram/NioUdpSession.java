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
 */
public class NioUdpSession extends AbstractNioSession<AbstractNioUdpListener> {

    private static final long serialVersionUID = 1L;

    private final Queue<NioUdpMessageWrapper<ByteBuffer>> writeQueue = new LinkedBlockingQueue<>();

    private InetSocketAddress socketAddress;

    public NioUdpSession(NioEventGroup<AbstractNioUdpListener> eventGroup, DatagramChannel channel) {
        super(eventGroup, channel);
    }

    public NioUdpSession(NioEventGroup<AbstractNioUdpListener> eventGroup, DatagramChannel channel, InetSocketAddress socketAddress) {
        super(eventGroup, channel);
        this.socketAddress = socketAddress;
    }

    private int channelRead(ByteBuffer buffer) throws IOException {
        SocketAddress address = ((DatagramChannel) channel()).receive(buffer);
        if (address != null) {
            attr(SocketAddress.class.getName(), address);
            return buffer().remaining();
        }
        return 0;
    }

    private int channelWrite(DatagramChannel channel, ByteBuffer buffer) throws IOException {
        if (channel != null) {
            return channel.write(buffer);
        }
        buffer.reset();
        return 0;
    }

    @Override
    public InetSocketAddress remoteSocketAddress() {
        if (socketAddress == null) {
            return (InetSocketAddress) ((DatagramChannel) channel()).socket().getRemoteSocketAddress();
        }
        return socketAddress;
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
        ByteBuffer dataByteBuffer = ByteBuffer.allocate(eventGroup().bufferSize());
        //
        int length;
        while ((length = channelRead(dataByteBuffer)) > 0) {
            dataByteBuffer.flip();
            buffer().append(dataByteBuffer.array(), dataByteBuffer.position(), dataByteBuffer.remaining());
            dataByteBuffer.clear();
        }
        if (length < 0) {
            close();
            return;
        }
        ByteBufferBuilder copiedBuffer = buffer().duplicate().flip();
        while (true) {
            copiedBuffer.mark();
            NioUdpMessageWrapper message;
            InetSocketAddress address = attr(SocketAddress.class.getName());
            if (address != null && copiedBuffer.remaining() > 0) {
                message = new NioUdpMessageWrapper<>(address, eventGroup().codec().decode(copiedBuffer));
            } else {
                message = null;
            }
            if (message == null) {
                copiedBuffer.reset();
                bufferReset(copiedBuffer);
                break;
            } else {
                postMessageReceived(message);
            }
        }
    }

    @Override
    public void write() throws IOException {
        synchronized (writeQueue) {
            while (true) {
                NioUdpMessageWrapper<ByteBuffer> messageWrapper = writeQueue.peek();
                if (messageWrapper == null) {
                    selectionKey().interestOps(SelectionKey.OP_READ);
                    break;
                } else {
                    DatagramChannel channel = null;
                    try {
                        channel = (DatagramChannel) channel();
                        if (!channel.isConnected()) {
                            channel = channel.connect(messageWrapper.getSocketAddress());
                        }
                        int writeLen = channelWrite(channel, messageWrapper.getMessage());
                        if (writeLen == 0 && messageWrapper.getMessage().remaining() > 0) {
                            break;
                        }
                        if (messageWrapper.getMessage().remaining() == 0) {
                            writeQueue.remove();
                        } else {
                            break;
                        }
                    } finally {
                        if (channel != null) {
                            channel.disconnect();
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
            ByteBufferBuilder messageBuffer = eventGroup().codec().encode(message);
            if (messageBuffer != null) {
                if (writeQueue.offer(new NioUdpMessageWrapper<>(socketAddress, messageBuffer.buffer()))) {
                    selectionKey().interestOps(selectionKey().interestOps() | SelectionKey.OP_WRITE);
                    selectionKey().selector().wakeup();
                }
            }
        }
    }
}
