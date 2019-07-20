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
import net.ymate.platform.serv.nio.AbstractNioSession;
import net.ymate.platform.serv.nio.INioEventGroup;
import net.ymate.platform.serv.nio.INioSession;

import java.nio.channels.SelectableChannel;

/**
 * @param <LISTENER> 监听器类型
 * @author 刘镇 (suninformation@163.com) on 15/11/15 下午11:47
 */
public class NioSession<LISTENER extends IListener<INioSession>> extends AbstractNioSession<LISTENER> {

    private static final long serialVersionUID = 1L;

    public NioSession(INioEventGroup<LISTENER> eventGroup, SelectableChannel channel) {
        super(eventGroup, channel);
    }
}
