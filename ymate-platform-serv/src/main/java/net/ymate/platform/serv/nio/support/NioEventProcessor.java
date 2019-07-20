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
package net.ymate.platform.serv.nio.support;

import net.ymate.platform.serv.IListener;
import net.ymate.platform.serv.nio.AbstractNioEventProcessor;
import net.ymate.platform.serv.nio.INioEventGroup;
import net.ymate.platform.serv.nio.INioSession;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

/**
 * @param <LISTENER> 监听器类型
 * @author 刘镇 (suninformation@163.com) on 15/11/9 上午9:28
 */
public class NioEventProcessor<LISTENER extends IListener<INioSession>> extends AbstractNioEventProcessor<LISTENER> {

    public NioEventProcessor(INioEventGroup<LISTENER> eventGroup, String name) throws IOException {
        super(name, eventGroup, 0L);
    }

    @Override
    protected INioSession buildNioSession(INioEventGroup<LISTENER> eventGroup, SelectableChannel channel) {
        return new NioSession<>(eventGroup, channel);
    }

    @Override
    protected void onReadEvent(SelectionKey key) throws IOException {
        final INioSession session = (INioSession) key.attachment();
        if (session != null && session.isConnected()) {
            session.read();
        }
    }

    @Override
    protected void onWriteEvent(SelectionKey key) throws IOException {
        INioSession session = (INioSession) key.attachment();
        if (session != null && session.isConnected()) {
            session.write();
        }
    }
}
