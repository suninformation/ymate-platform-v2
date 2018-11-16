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
import net.ymate.platform.serv.IListener;
import net.ymate.platform.serv.ISession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/11/15 11:33 PM
 * @version 1.0
 */
public abstract class AbstractNioEventProcessor<LISTENER extends IListener<INioSession>> extends Thread implements INioEventProcessor<INioSession> {

    private static final Log _LOG = LogFactory.getLog(AbstractNioEventProcessor.class);

    private final Queue<Object[]> __registeredQueues = new LinkedBlockingQueue<Object[]>();
    private final Queue<INioSession> __closedQueues = new LinkedBlockingQueue<INioSession>();

    private final INioEventGroup<LISTENER> __eventGroup;

    private Selector __selector;

    private long __selectTimeout;

    private boolean __running;

    public AbstractNioEventProcessor(String name, INioEventGroup<LISTENER> eventGroup, long selectTimeout) throws IOException {
        super(name);
        __eventGroup = eventGroup;
        __selector = Selector.open();
        __selectTimeout = selectTimeout <= 0 ? 500L : selectTimeout;
    }

    @Override
    public void start() {
        if (__running) {
            return;
        }
        __running = true;
        super.start();
    }

    @Override
    public void run() {
        try {
            while (__running) {
                __selector.select(__selectTimeout);
                __processRegisteredQueues();
                Iterator<SelectionKey> _keyIterator = __selector.selectedKeys().iterator();
                while (_keyIterator.hasNext()) {
                    SelectionKey _selectionKey = _keyIterator.next();
                    _keyIterator.remove();
                    if (_selectionKey.isValid()) {
                        Object _attachment = _selectionKey.attachment();
                        if (_attachment instanceof INioSession) {
                            ((INioSession) _attachment).touch();
                        }
                        try {
                            if (_selectionKey.isAcceptable()) {
                                onAcceptedEvent(_selectionKey);
                            } else if (_selectionKey.isConnectable()) {
                                onConnectedEvent(_selectionKey);
                            } else if (_selectionKey.isReadable()) {
                                onReadEvent(_selectionKey);
                            } else if (_selectionKey.isWritable()) {
                                onWriteEvent(_selectionKey);
                            }
                        } catch (IOException e) {
                            onExceptionEvent(_selectionKey, e);
                        }
                    }
                }
                __processClosedQueues();
            }
        } catch (IOException e) {
            if (__running) {
                _LOG.error(RuntimeUtils.unwrapThrow(e));
            } else {
                _LOG.warn(RuntimeUtils.unwrapThrow(e));
            }
        }
    }

    @Override
    public void interrupt() {
        try {
            __running = false;
            join();
            __selector.close();
        } catch (Exception e) {
            _LOG.error(RuntimeUtils.unwrapThrow(e));
        }
        super.interrupt();
    }

    @Override
    public void close() throws IOException {
        interrupt();
    }

    public void registerEvent(SelectableChannel channel, int ops, INioSession session) throws IOException {
        if (Thread.currentThread() == this) {
            SelectionKey key = channel.register(__selector, ops, session);
            if (session != null) {
                session.selectionKey(key);
                session.status(ISession.Status.CONNECTED);
                //
                if (__eventGroup.isServer() && session.isUdp()) {
                    return;
                }
                __eventGroup.listener().onSessionRegistered(session);
            }
        } else {
            __registeredQueues.offer(new Object[]{channel, ops, session});
            __selector.wakeup();
        }
    }

    public void unregisterEvent(INioSession session) {
        if (__closedQueues.contains(session)) {
            return;
        }
        __closedQueues.add(session);
        __selector.wakeup();
    }

    @Override
    public Selector selector() {
        return __selector;
    }

    private void __processRegisteredQueues() {
        Object[] _event;
        while ((_event = __registeredQueues.poll()) != null) {
            try {
                SelectableChannel _channel = (SelectableChannel) _event[0];
                if (!_channel.isOpen()) {
                    continue;
                }
                INioSession _session = (INioSession) _event[2];
                SelectionKey _key = _channel.register(__selector, (Integer) _event[1], _session);
                if (_session != null) {
                    _session.selectionKey(_key);
                    _session.status(ISession.Status.CONNECTED);
                    //
                    __eventGroup.listener().onSessionRegistered(_session);
                }
            } catch (IOException e) {
                _LOG.error(RuntimeUtils.unwrapThrow(e));
            }
        }
    }

    private void __processClosedQueues() {
        INioSession _session;
        while ((_session = __closedQueues.poll()) != null) {
            try {
                _session.closeNow();
            } catch (IOException e) {
                _LOG.error(RuntimeUtils.unwrapThrow(e));
            }
        }
    }

    //

    protected void onExceptionEvent(final SelectionKey key, final Throwable e) {
        final INioSession _session = (INioSession) key.attachment();
        if (_session == null) {
            try {
                key.channel().close();
                key.cancel();
            } catch (IOException ex) {
                _LOG.error(RuntimeUtils.unwrapThrow(ex));
            }
        } else {
            _session.status(ISession.Status.ERROR);
        }
        __eventGroup.executorService().submit(new Runnable() {
            @Override
            public void run() {
                try {
                    __eventGroup.listener().onExceptionCaught(e, _session);
                } catch (IOException ex) {
                    _LOG.error(RuntimeUtils.unwrapThrow(ex));
                }
            }
        });
        if (_session != null) {
            try {
                _session.close();
            } catch (IOException ex) {
                _LOG.error(RuntimeUtils.unwrapThrow(ex));
            }
        }
    }

    protected abstract INioSession buildNioSession(INioEventGroup<LISTENER> eventGroup, SelectableChannel channel);

    protected void onAcceptedEvent(SelectionKey key) throws IOException {
        SocketChannel _channel = ((ServerSocketChannel) key.channel()).accept();
        _channel.configureBlocking(false);
        INioSession _session = buildNioSession(__eventGroup, _channel);
        _session.selectionKey(key);
        _session.status(ISession.Status.CONNECTED);
        __eventGroup.listener().onSessionAccepted(_session);
    }

    protected void onConnectedEvent(SelectionKey key) throws IOException {
        INioSession _session = (INioSession) key.attachment();
        if (_session != null) {
            SocketChannel _channel = (SocketChannel) key.interestOps(0).channel();
            if (_channel.finishConnect()) {
                _session.finishConnect();
            }
            _session.selectionKey(key);
            _session.status(ISession.Status.CONNECTED);
            __eventGroup.listener().onSessionConnected(_session);
        }
    }

    protected abstract void onReadEvent(SelectionKey key) throws IOException;

    protected abstract void onWriteEvent(SelectionKey key) throws IOException;
}
