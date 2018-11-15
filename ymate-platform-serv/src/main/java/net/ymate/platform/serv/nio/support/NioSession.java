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
package net.ymate.platform.serv.nio.support;

import net.ymate.platform.core.util.RuntimeUtils;
import net.ymate.platform.serv.AbstractSession;
import net.ymate.platform.serv.IListener;
import net.ymate.platform.serv.ISession;
import net.ymate.platform.serv.nio.INioSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @param <LISTENER> 监听器类型
 * @author 刘镇 (suninformation@163.com) on 15/11/15 下午11:47
 * @version 1.0
 */
public class NioSession<LISTENER extends IListener<INioSession>> extends AbstractSession implements INioSession {

    private static final Log _LOG = LogFactory.getLog(NioSession.class);

    private final NioEventGroup<LISTENER> __eventGroup;

    private SelectableChannel __channel;

    private SelectionKey __selectionKey;

    private final Queue<ByteBuffer> __writeBufferQueue;

    protected ByteBufferBuilder __buffer;

    private final CountDownLatch __connLatch = new CountDownLatch(1);

    private ISession.Status __status;

    public NioSession(NioEventGroup<LISTENER> eventGroup) throws IOException {
        this(eventGroup, null);
    }

    public NioSession(NioEventGroup<LISTENER> eventGroup, SelectableChannel channel) {
        super();
        __status = ISession.Status.NEW;
        __eventGroup = eventGroup;
        __writeBufferQueue = new LinkedBlockingQueue<ByteBuffer>();
        //
        __channel = channel;
    }

    @Override
    public boolean connectSync(long time) {
        try {
            return this.__connLatch.await(time, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            _LOG.error(e.getMessage(), RuntimeUtils.unwrapThrow(e));
        }
        return false;
    }

    @Override
    public void finishConnect() {
        this.__connLatch.countDown();
    }

    @Override
    public Status status() {
        return __status;
    }

    @Override
    public void status(Status status) {
        __status = status;
    }

    @Override
    public void registerEvent(int ops) throws IOException {
        __eventGroup.processor().registerEvent(__channel, ops, this);
    }

    @Override
    public void selectionKey(SelectionKey key) {
        __selectionKey = key;
    }

    @Override
    public SelectionKey selectionKey() {
        return __selectionKey;
    }

    @Override
    public void send(Object message) throws IOException {
        if (__selectionKey != null) {
            ByteBufferBuilder _msgBuffer = __eventGroup.codec().encode(message);
            if (_msgBuffer != null) {
                if (__writeBufferQueue.offer(_msgBuffer.buffer())) {
                    __selectionKey.interestOps(__selectionKey.interestOps() | SelectionKey.OP_WRITE);
                    __selectionKey.selector().wakeup();
                }
            }
        }
    }

    @Override
    public void close() throws IOException {
        if (__selectionKey == null) {
            return;
        }
        NioEventProcessor _handler = __eventGroup.processor(__selectionKey);
        if (_handler != null) {
            __eventGroup.listener().onBeforeSessionClosed(this);
            _handler.unregisterEvent(this);
            __selectionKey.selector().wakeup();
        }
    }

    @Override
    public void closeNow() throws IOException {
        if (status() == ISession.Status.CLOSED) {
            return;
        }
        status(ISession.Status.CLOSED);
        if (__selectionKey != null) {
            __selectionKey.cancel();
            __selectionKey = null;
        }
        if (__channel != null) {
            __channel.close();
        }
        //
        __eventGroup.executorService().submit(new Runnable() {
            @Override
            public void run() {
                try {
                    __eventGroup.listener().onAfterSessionClosed(NioSession.this);
                } catch (IOException ex) {
                    _LOG.error(ex.getMessage(), RuntimeUtils.unwrapThrow(ex));
                }
            }
        });
    }

    protected int __doChannelRead(ByteBuffer buffer) throws IOException {
        return ((SocketChannel) __channel).read(buffer);
    }

    protected int __doChannelWrite(ByteBuffer buffer) throws IOException {
        return ((SocketChannel) __channel).write(buffer);
    }

    @Override
    public InetSocketAddress remoteSocketAddress() {
        if (__channel instanceof DatagramChannel) {
            InetSocketAddress _sourceAddr = attr(SocketAddress.class.getName());
            if (_sourceAddr == null) {
                _sourceAddr = (InetSocketAddress) ((DatagramChannel) __channel).socket().getRemoteSocketAddress();
            }
            return _sourceAddr;
        }
        return (InetSocketAddress) ((SocketChannel) __channel).socket().getRemoteSocketAddress();
    }

    private void __doBufferReset(ByteBufferBuilder buffer) {
        if (buffer != null && buffer.remaining() > 0) {
            int _len = buffer.remaining();
            byte[] _bytes = new byte[_len];
            buffer.get(_bytes);
            __buffer = ByteBufferBuilder.wrap(ByteBuffer.wrap(_bytes)).position(_len);
        } else {
            __buffer = null;
        }
    }

    @Override
    public void read() throws IOException {
        if (__buffer == null) {
            __buffer = ByteBufferBuilder.allocate(__eventGroup.bufferSize());
        }
        ByteBuffer _data = ByteBuffer.allocate(__eventGroup.bufferSize());
        int _len = 0;
        while ((_len = __doChannelRead(_data)) > 0) {
            _data.flip();
            __buffer.append(_data.array(), _data.position(), _data.remaining());
            _data.clear();
        }
        if (_len < 0) {
            close();
            return;
        }
        ByteBufferBuilder _copiedBuffer = __buffer.duplicate().flip();
        while (true) {
            _copiedBuffer.mark();
            Object _message;
            if (_copiedBuffer.remaining() > 0) {
                _message = __eventGroup.codec().decode(_copiedBuffer);
            } else {
                _message = null;
            }
            if (_message == null) {
                _copiedBuffer.reset();
                __doBufferReset(_copiedBuffer);
                break;
            } else {
                final Object _copiedObj = _message;
                __eventGroup.executorService().submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            __eventGroup.listener().onMessageReceived(_copiedObj, NioSession.this);
                        } catch (IOException e) {
                            try {
                                __eventGroup.listener().onExceptionCaught(e, NioSession.this);
                            } catch (IOException ex) {
                                try {
                                    close();
                                } catch (IOException exx) {
                                    _LOG.error(exx.getMessage(), RuntimeUtils.unwrapThrow(exx));
                                }
                            }
                        }
                    }
                });
            }
        }
    }

    @Override
    public void write() throws IOException {
        synchronized (__writeBufferQueue) {
            while (true) {
                ByteBuffer _buffer = __writeBufferQueue.peek();
                if (_buffer == null) {
                    __selectionKey.interestOps(SelectionKey.OP_READ);
                    break;
                } else {
                    int _wLen = __doChannelWrite(_buffer);
                    if (_wLen == 0 && _buffer.remaining() > 0) {
                        break;
                    }
                    if (_buffer.remaining() == 0) {
                        __writeBufferQueue.remove();
                    } else {
                        break;
                    }
                }
            }
        }
    }

    @Override
    public String remoteAddress() {
        if (status() != ISession.Status.CLOSED && __selectionKey != null) {
            if (__channel != null) {
                InetSocketAddress _addr = remoteSocketAddress();
                if (_addr != null) {
                    return _addr.getHostName() + ":" + _addr.getPort();
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "Session [id=" + id()
                + ", remote=" + remoteAddress()
                + ", status=" + status()
                + "]";
    }
}
