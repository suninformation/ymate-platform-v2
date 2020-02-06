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

import net.ymate.platform.core.beans.annotation.Ignored;
import net.ymate.platform.core.support.IDestroyable;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.Selector;

/**
 * 事件处理器
 *
 * @param <SESSION> 会话类型
 * @author 刘镇 (suninformation@163.com) on 15/11/15 下午6:31
 */
@Ignored
public interface INioEventProcessor<SESSION extends INioSession> extends IDestroyable {

    /**
     * 注册事件
     *
     * @param channel 多路复用频道
     * @param ops     操作标识
     * @param session 会话对象
     * @throws IOException 可能产生的IO异常
     */
    void registerEvent(SelectableChannel channel, int ops, SESSION session) throws IOException;

    /**
     * 解注册事件
     *
     * @param session 会话对象
     */
    void unregisterEvent(SESSION session);

    /**
     * 获取选择器
     *
     * @return 返回选择器对象
     */
    Selector selector();

    /**
     * 启动
     */
    void start();
}
