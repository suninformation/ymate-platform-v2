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
package net.ymate.platform.serv.nio;

import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.commons.util.UUIDUtils;
import net.ymate.platform.serv.IListener;
import net.ymate.platform.serv.nio.support.ByteBufferBuilder;
import net.ymate.platform.serv.nio.support.NioEventProcessor;
import net.ymate.platform.serv.nio.support.NioSession;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;

/**
 * @param <LISTENER> 监听器类型
 * @author 刘镇 (suninformation@163.com) on 2018/11/15 11:00 PM
 */
public abstract class AbstractNioSession<LISTENER extends IListener<INioSession>> implements INioSession {

    private static final long serialVersionUID = 1L;

    private static final Log LOG = LogFactory.getLog(NioSession.class);

    private final String id = UUIDUtils.UUID();

    private long lastTouchTime = System.currentTimeMillis();

    private final ConcurrentMap<String, Object> attributes = new ConcurrentHashMap<>();

    private final Queue<ByteBuffer> byteBufferQueue = new LinkedBlockingQueue<>();

    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    private final INioEventGroup<LISTENER> eventGroup;

    private final SelectableChannel channel;

    private SelectionKey selectionKey;

    private ByteBufferBuilder bufferBuilder;

    private Status status;

    private final boolean udp;

    public AbstractNioSession(INioEventGroup<LISTENER> eventGroup, SelectableChannel channel) {
        this.eventGroup = eventGroup;
        this.channel = channel;
        //
        status = Status.NEW;
        udp = channel instanceof DatagramChannel;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public boolean isNew() {
        return status() == Status.NEW;
    }

    @Override
    public boolean isConnected() {
        return status() == Status.CONNECTED;
    }

    @Override
    public void touch() {
        lastTouchTime = System.currentTimeMillis();
    }

    @Override
    public long lastTouchTime() {
        return lastTouchTime;
    }

    @Override
    public Map<String, Object> attrs() {
        return attributes;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T attr(String key) {
        return (T) attributes.get(key);
    }

    @Override
    public void attr(String key, Object value) {
        attributes.put(key, value);
    }

    protected INioEventGroup<LISTENER> eventGroup() {
        return eventGroup;
    }

    protected SelectableChannel channel() {
        return channel;
    }

    protected ByteBufferBuilder buffer() {
        return bufferBuilder;
    }

    protected void buffer(ByteBufferBuilder bufferBuilder) {
        this.bufferBuilder = bufferBuilder;
    }

    @Override
    public boolean isUdp() {
        return udp;
    }

    @Override
    public InetSocketAddress remoteSocketAddress() {
        if (isUdp()) {
            return null;
        }
        return (InetSocketAddress) ((SocketChannel) channel).socket().getRemoteSocketAddress();
    }

    @Override
    public String remoteAddress() {
        if (status != Status.CLOSED && selectionKey() != null) {
            if (channel() != null) {
                InetSocketAddress socketAddress = remoteSocketAddress();
                if (socketAddress != null) {
                    return socketAddress.getHostName() + ":" + socketAddress.getPort();
                }
            }
        }
        return null;
    }

    @Override
    public Status status() {
        return status;
    }

    @Override
    public void status(Status status) {
        this.status = status;
    }

    @Override
    public void close() throws IOException {
        if (selectionKey == null) {
            return;
        }
        NioEventProcessor<?> eventProcessor = eventGroup.processor(selectionKey);
        if (eventProcessor != null) {
            eventGroup.listener().onBeforeSessionClosed(this);
            eventProcessor.unregisterEvent(this);
            selectionKey.selector().wakeup();
        }
    }

    @Override
    public void closeNow() throws IOException {
        if (status() == Status.CLOSED) {
            return;
        }
        status(Status.CLOSED);
        if (selectionKey != null) {
            selectionKey.cancel();
            selectionKey = null;
        }
        if (channel != null) {
            channel.close();
        }
        eventGroup.executorService().submit(() -> {
            try {
                eventGroup.listener().onAfterSessionClosed(AbstractNioSession.this);
            } catch (IOException ex) {
                LOG.error(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(ex));
            }
        });
    }

    @Override
    public void registerEvent(int ops) throws IOException {
        eventGroup.processor().registerEvent(channel, ops, this);
    }

    @Override
    public void selectionKey(SelectionKey key) {
        selectionKey = key;
    }

    @Override
    public SelectionKey selectionKey() {
        return selectionKey;
    }

    @Override
    public boolean connectSync(long time) {
        try {
            return countDownLatch.await(time, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            LOG.error(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
        }
        return false;
    }

    @Override
    public void finishConnect() {
        countDownLatch.countDown();
    }

    //

    protected void bufferReset(ByteBufferBuilder buffer) {
        if (buffer != null && buffer.remaining() > 0) {
            int len = buffer.remaining();
            byte[] bytes = new byte[len];
            buffer.get(bytes);
            bufferBuilder = ByteBufferBuilder.wrap(ByteBuffer.wrap(bytes)).position(len);
        } else {
            bufferBuilder = null;
        }
    }

    protected void postMessageReceived(final Object message) {
        eventGroup.executorService().submit(() -> {
            try {
                eventGroup.listener().onMessageReceived(message, AbstractNioSession.this);
            } catch (IOException e) {
                try {
                    eventGroup.listener().onExceptionCaught(e, AbstractNioSession.this);
                } catch (IOException ex) {
                    try {
                        close();
                    } catch (IOException exx) {
                        LOG.error(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(exx));
                    }
                }
            }
        });
    }

    @Override
    public void read() throws IOException {
        if (bufferBuilder == null) {
            bufferBuilder = ByteBufferBuilder.allocate(eventGroup.bufferSize());
        }
        ByteBuffer buffer = ByteBuffer.allocate(eventGroup.bufferSize());
        int len;
        while ((len = ((SocketChannel) channel()).read(buffer)) > 0) {
            buffer.flip();
            bufferBuilder.append(buffer.array(), buffer.position(), buffer.remaining());
            buffer.clear();
        }
        if (len < 0) {
            close();
            return;
        }
        ByteBufferBuilder copiedBuffer = bufferBuilder.duplicate().flip();
        while (true) {
            copiedBuffer.mark();
            Object message;
            if (copiedBuffer.remaining() > 0) {
                message = eventGroup.codec().decode(copiedBuffer);
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
        synchronized (byteBufferQueue) {
            while (true) {
                ByteBuffer buffer = byteBufferQueue.peek();
                if (buffer == null) {
                    selectionKey.interestOps(SelectionKey.OP_READ);
                    break;
                } else {
                    int len = ((SocketChannel) channel()).write(buffer);
                    if (len == 0 && buffer.remaining() > 0) {
                        break;
                    }
                    if (buffer.remaining() == 0) {
                        byteBufferQueue.remove();
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
            ByteBufferBuilder msgBuffer = eventGroup.codec().encode(message);
            if (msgBuffer != null) {
                if (byteBufferQueue.offer(msgBuffer.buffer())) {
                    selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);
                    selectionKey.selector().wakeup();
                }
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AbstractNioSession<?> session = (AbstractNioSession<?>) o;
        return id.equals(session.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return String.format("%s [id=%s, remote=%s, status=%s, attrs=%s]", getClass().getSimpleName(), id(), StringUtils.defaultIfBlank(remoteAddress(), "<UNKNOWN>"), status(), attrs());
    }
}
