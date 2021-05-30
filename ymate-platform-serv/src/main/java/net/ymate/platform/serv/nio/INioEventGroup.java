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

import net.ymate.platform.serv.IListener;
import net.ymate.platform.serv.nio.support.NioEventProcessor;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.concurrent.ExecutorService;

/**
 * 多路复用通道事件处理器
 *
 * @param <LISTENER> 监听器类型
 * @author 刘镇 (suninformation@163.com) on 2018/11/16 1:40 AM
 */
public interface INioEventGroup<LISTENER extends IListener<INioSession>> extends Closeable {

    /**
     * 启动
     *
     * @throws IOException 可能产生的IO异常
     */
    void start() throws IOException;

    /**
     * 停止
     *
     * @throws IOException 可能产生的IO异常
     */
    void stop() throws IOException;

    /**
     * 编解码器
     *
     * @return 返回编解码器对象
     */
    INioCodec codec();

    /**
     * 监听器
     *
     * @return 返回监听器对象
     */
    LISTENER listener();

    /**
     * 会话
     *
     * @return 返回会话对象
     */
    INioSession session();

    /**
     * 是否已启动
     *
     * @return 返回true表示已启动
     */
    boolean isStarted();

    /**
     * 是否为服务端
     *
     * @return 返回true表示服务端
     */
    boolean isServer();

    /**
     * 客户端/服务端名称
     *
     * @return 返回名称字符串
     */
    String name();

    /**
     * 设置客户端/服务端名称
     *
     * @param name 名称
     */
    void name(String name);

    /**
     * 缓冲区大小
     *
     * @return 返回缓冲区大小
     */
    int bufferSize();

    /**
     * 执行线程数量
     *
     * @return 返回执行线程数量
     */
    int executorCount();

    /**
     * 连接超时时间(秒)
     *
     * @return 返回连接超时时间
     */
    int connectionTimeout();

    /**
     * 执行线程池
     *
     * @return 返回执行线程池对象
     */
    ExecutorService executorService();

    /**
     * 根据通道选择键获取对象的事件处理器
     *
     * @param key 通道选择键
     * @return 返回事件处理器对象
     */
    @SuppressWarnings("rawtypes")
    NioEventProcessor processor(SelectionKey key);

    /**
     * 获取事件处理器
     *
     * @return 返回事件处理器对象
     */
    @SuppressWarnings("rawtypes")
    NioEventProcessor processor();
}
