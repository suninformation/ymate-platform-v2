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
package net.ymate.platform.serv.nio;

import net.ymate.platform.core.util.RuntimeUtils;
import net.ymate.platform.serv.AbstractSession;
import net.ymate.platform.serv.IListener;
import net.ymate.platform.serv.ISession;
import net.ymate.platform.serv.nio.support.ByteBufferBuilder;
import net.ymate.platform.serv.nio.support.NioEventProcessor;
import net.ymate.platform.serv.nio.support.NioSession;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
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
 * @author 刘镇 (suninformation@163.com) on 2018/11/15 11:00 PM
 * @version 1.0
 */
public abstract class AbstractNioSession<LISTENER extends IListener<INioSession>> extends AbstractSession implements INioSession {

    private static final Log _LOG = LogFactory.getLog(NioSession.class);

    private final Queue<ByteBuffer> __writeBufferQueue = new LinkedBlockingQueue<ByteBuffer>();

    private final CountDownLatch __connLatch = new CountDownLatch(1);

    private final INioEventGroup<LISTENER> __eventGroup;

    private final SelectableChannel __channel;

    private SelectionKey __selectionKey;

    private ByteBufferBuilder __buffer;

    private ISession.Status __status;

    private boolean __isUdp;

    public AbstractNioSession(INioEventGroup<LISTENER> eventGroup, SelectableChannel channel) {
        __eventGroup = eventGroup;
        __channel = channel;
        //
        __status = ISession.Status.NEW;
        __isUdp = channel instanceof DatagramChannel;
    }

    protected INioEventGroup<LISTENER> eventGroup() {
        return __eventGroup;
    }

    protected SelectableChannel channel() {
        return __channel;
    }

    protected ByteBufferBuilder buffer() {
        return __buffer;
    }

    protected void buffer(ByteBufferBuilder bufferBuilder) {
        __buffer = bufferBuilder;
    }

    @Override
    public boolean isUdp() {
        return __isUdp;
    }

    @Override
    public InetSocketAddress remoteSocketAddress() {
        if (isUdp()) {
            return null;
        }
        return (InetSocketAddress) ((SocketChannel) __channel).socket().getRemoteSocketAddress();
    }

    @Override
    public String remoteAddress() {
        if (__status != ISession.Status.CLOSED && selectionKey() != null) {
            if (channel() != null) {
                InetSocketAddress _address = remoteSocketAddress();
                if (_address != null) {
                    return _address.getHostName() + ":" + _address.getPort();
                }
            }
        }
        return null;
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
    public void close() throws IOException {
        if (__selectionKey == null) {
            return;
        }
        NioEventProcessor _processor = __eventGroup.processor(__selectionKey);
        if (_processor != null) {
            __eventGroup.listener().onBeforeSessionClosed(this);
            _processor.unregisterEvent(this);
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
        __eventGroup.executorService().submit(new Runnable() {
            @Override
            public void run() {
                try {
                    __eventGroup.listener().onAfterSessionClosed(AbstractNioSession.this);
                } catch (IOException ex) {
                    _LOG.error(RuntimeUtils.unwrapThrow(ex));
                }
            }
        });
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
    public boolean connectSync(long time) {
        try {
            return __connLatch.await(time, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            _LOG.error(RuntimeUtils.unwrapThrow(e));
        }
        return false;
    }

    @Override
    public void finishConnect() {
        __connLatch.countDown();
    }

    //

    protected void __doBufferReset(ByteBufferBuilder buffer) {
        if (buffer != null && buffer.remaining() > 0) {
            int _len = buffer.remaining();
            byte[] _bytes = new byte[_len];
            buffer.get(_bytes);
            __buffer = ByteBufferBuilder.wrap(ByteBuffer.wrap(_bytes)).position(_len);
        } else {
            __buffer = null;
        }
    }

    protected void __doPostMessageReceived(final Object message) throws IOException {
        __eventGroup.executorService().submit(new Runnable() {
            @Override
            public void run() {
                try {
                    __eventGroup.listener().onMessageReceived(message, AbstractNioSession.this);
                } catch (IOException e) {
                    try {
                        __eventGroup.listener().onExceptionCaught(e, AbstractNioSession.this);
                    } catch (IOException ex) {
                        try {
                            close();
                        } catch (IOException exx) {
                            _LOG.error(RuntimeUtils.unwrapThrow(exx));
                        }
                    }
                }
            }
        });
    }

    @Override
    public void read() throws IOException {
        if (__buffer == null) {
            __buffer = ByteBufferBuilder.allocate(__eventGroup.bufferSize());
        }
        ByteBuffer _data = ByteBuffer.allocate(__eventGroup.bufferSize());
        int _len;
        while ((_len = ((SocketChannel) channel()).read(_data)) > 0) {
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
                __doPostMessageReceived(_message);
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
                    int _wLen = ((SocketChannel) channel()).write(_buffer);
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
    public void send(Object message) {
        if (selectionKey() != null) {
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
    public String toString() {
        return getClass().getSimpleName() + " [id=" + id()
                + ", remote=" + StringUtils.defaultIfBlank(remoteAddress(), "<UNKNOWN>")
                + ", status=" + status()
                + ", attrs=" + attrs()
                + "]";
    }
}
