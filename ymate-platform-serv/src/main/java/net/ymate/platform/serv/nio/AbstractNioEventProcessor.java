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
import net.ymate.platform.serv.IListener;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @param <LISTENER> 监听器类型
 * @author 刘镇 (suninformation@163.com) on 2018/11/15 11:33 PM
 */
public abstract class AbstractNioEventProcessor<LISTENER extends IListener<INioSession>> extends Thread implements INioEventProcessor<INioSession> {

    private static final Log LOG = LogFactory.getLog(AbstractNioEventProcessor.class);

    private final Queue<Object[]> registeredQueues = new LinkedBlockingQueue<>();

    private final Queue<INioSession> closedQueues = new LinkedBlockingQueue<>();

    private final INioEventGroup<LISTENER> eventGroup;

    private final Selector selector;

    private final long selectTimeout;

    private boolean running;

    public AbstractNioEventProcessor(String name, INioEventGroup<LISTENER> eventGroup, long selectTimeout) throws IOException {
        super(name);
        this.eventGroup = eventGroup;
        selector = Selector.open();
        this.selectTimeout = selectTimeout <= 0 ? 500L : selectTimeout;
    }

    @Override
    public void start() {
        if (running) {
            return;
        }
        running = true;
        super.start();
    }

    @Override
    public void run() {
        try {
            while (running) {
                selector.select(selectTimeout);
                processRegisteredQueues();
                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey selectionKey = keyIterator.next();
                    keyIterator.remove();
                    if (selectionKey.isValid()) {
                        Object attachment = selectionKey.attachment();
                        if (attachment instanceof INioSession) {
                            ((INioSession) attachment).touch();
                        }
                        try {
                            if (selectionKey.isAcceptable()) {
                                onAcceptedEvent(selectionKey);
                            } else if (selectionKey.isConnectable()) {
                                onConnectedEvent(selectionKey);
                            } else if (selectionKey.isReadable()) {
                                onReadEvent(selectionKey);
                            } else if (selectionKey.isWritable()) {
                                onWriteEvent(selectionKey);
                            }
                        } catch (IOException e) {
                            onExceptionEvent(selectionKey, e);
                        }
                    }
                }
                processClosedQueues();
            }
        } catch (IOException e) {
            if (running) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
                }
            } else if (LOG.isWarnEnabled()) {
                LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
            }
        }
    }

    @Override
    public void interrupt() {
        try {
            running = false;
            join();
            selector.close();
        } catch (IOException | InterruptedException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
            }
        }
        super.interrupt();
    }

    @Override
    public void close() throws IOException {
        interrupt();
    }

    @Override
    public void registerEvent(SelectableChannel channel, int ops, INioSession session) throws IOException {
        if (Thread.currentThread() == this) {
            SelectionKey key = channel.register(selector, ops, session);
            if (session != null) {
                session.selectionKey(key);
                session.status(INioSession.Status.CONNECTED);
                //
                if (eventGroup.isServer() && session.isUdp()) {
                    return;
                }
                eventGroup.listener().onSessionRegistered(session);
            }
        } else {
            registeredQueues.offer(new Object[]{channel, ops, session});
            selector.wakeup();
        }
    }

    @Override
    public void unregisterEvent(INioSession session) {
        if (closedQueues.contains(session)) {
            return;
        }
        closedQueues.add(session);
        selector.wakeup();
    }

    @Override
    public Selector selector() {
        return selector;
    }

    private void processRegisteredQueues() {
        Object[] element;
        while ((element = registeredQueues.poll()) != null) {
            try {
                SelectableChannel channel = (SelectableChannel) element[0];
                if (!channel.isOpen()) {
                    continue;
                }
                INioSession session = (INioSession) element[2];
                SelectionKey selectionKey = channel.register(selector, (Integer) element[1], session);
                if (session != null) {
                    session.selectionKey(selectionKey);
                    session.status(INioSession.Status.CONNECTED);
                    //
                    eventGroup.listener().onSessionRegistered(session);
                }
            } catch (IOException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
                }
            }
        }
    }

    private void processClosedQueues() {
        INioSession session;
        while ((session = closedQueues.poll()) != null) {
            try {
                session.closeNow();
            } catch (IOException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
                }
            }
        }
    }

    //

    protected void onExceptionEvent(final SelectionKey key, final Throwable e) {
        INioSession session = (INioSession) key.attachment();
        if (session == null) {
            try {
                key.channel().close();
                key.cancel();
            } catch (IOException ex) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(ex));
                }
            }
        } else {
            session.status(INioSession.Status.ERROR);
        }
        eventGroup.executorService().submit(() -> {
            try {
                eventGroup.listener().onExceptionCaught(e, session);
            } catch (IOException ex) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(ex));
                }
            }
        });
        if (session != null) {
            try {
                session.close();
            } catch (IOException ex) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(ex));
                }
            }
        }
    }

    /**
     * 由子类实现创建会话的具体逻辑
     *
     * @param eventGroup 多路复用通道事件处理器
     * @param channel    通道
     * @return 返回会话对象
     */
    protected abstract INioSession buildNioSession(INioEventGroup<LISTENER> eventGroup, SelectableChannel channel);

    private void onAcceptedEvent(SelectionKey key) throws IOException {
        SocketChannel channel = ((ServerSocketChannel) key.channel()).accept();
        channel.configureBlocking(false);
        INioSession session = buildNioSession(eventGroup, channel);
        session.selectionKey(key);
        session.status(INioSession.Status.CONNECTED);
        eventGroup.listener().onSessionAccepted(session);
    }

    private void onConnectedEvent(SelectionKey key) throws IOException {
        INioSession session = (INioSession) key.attachment();
        if (session != null) {
            SocketChannel channel = (SocketChannel) key.interestOps(0).channel();
            if (channel.finishConnect()) {
                session.finishConnect();
            }
            session.selectionKey(key);
            session.status(INioSession.Status.CONNECTED);
            eventGroup.listener().onSessionConnected(session);
        }
    }

    /**
     * 由子类实现通道读逻辑
     *
     * @param key 选择键
     * @throws IOException 可能产生的I/O异常
     */
    protected abstract void onReadEvent(SelectionKey key) throws IOException;

    /**
     * 由子类实现通道写逻辑
     *
     * @param key 选择键
     * @throws IOException 可能产生的I/O异常
     */
    protected abstract void onWriteEvent(SelectionKey key) throws IOException;
}
