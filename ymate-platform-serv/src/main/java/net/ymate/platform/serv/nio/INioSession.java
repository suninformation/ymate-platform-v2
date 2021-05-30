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

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/15 下午6:10
 */
public interface INioSession extends Serializable, Closeable {

    /**
     * 会话状态枚举
     */
    enum Status {

        /**
         * 新建连接
         */
        NEW,

        /**
         * 已连接
         */
        CONNECTED,

        /**
         * 已关闭
         */
        CLOSED,

        /**
         * 连接错误
         */
        ERROR
    }

    /**
     * 获取会话唯一标识
     *
     * @return 返回会话唯一标识
     */
    String id();

    /**
     * 判断会话状态是否为NEW
     *
     * @return 返回会话状态是否为NEW
     */
    boolean isNew();

    /**
     * 判断会话状态是否为CONNECTED
     *
     * @return 返回会话状态是否为CONNECTED
     */
    boolean isConnected();

    /**
     * 获取远程套接字地址对象
     *
     * @return 返回远程套接字地址对象
     */
    InetSocketAddress remoteSocketAddress();

    /**
     * 获取远程IP地址端口号
     *
     * @return 返回远程IP地址端口号
     */
    String remoteAddress();

    /**
     * 获取当前会话状态
     *
     * @return 返回当前会话状态
     */
    Status status();

    /**
     * 更新会话活动状态
     */
    void touch();

    /**
     * 获取最后更新会话状态的时间(毫秒)
     *
     * @return 返回最后更新会话状态的时间(毫秒)
     */
    long lastTouchTime();

    /**
     * 获取属性映射
     *
     * @return 返回属性映射对象
     */
    Map<String, Object> attrs();

    /**
     * 获取指定键名的属性值
     *
     * @param key 键名
     * @param <T> 键值类型
     * @return 返回属性值对象
     */
    <T> T attr(String key);

    /**
     * 添加属性
     *
     * @param key   键名
     * @param value 键值
     */
    void attr(String key, Object value);

    /**
     * 向会话发送消息
     *
     * @param message 消息对象
     * @throws IOException 可能产生的异常
     */
    void send(Object message) throws IOException;

    /**
     * 注册事件
     *
     * @param ops 事件参数
     * @throws IOException 可能产生的异常
     */
    void registerEvent(int ops) throws IOException;

    /**
     * 设置选择键
     *
     * @param key 选择键
     */
    void selectionKey(SelectionKey key);

    /**
     * 获取选择键
     *
     * @return 返回当前选择键
     */
    SelectionKey selectionKey();

    /**
     * 等待连接指定time时间
     *
     * @param time 等待时间(毫秒)
     * @return 返回true表示正常结束等待
     */
    boolean connectSync(long time);

    /**
     * 结束等待连接
     */
    void finishConnect();

    /**
     * 判断是否为UDP会话
     *
     * @return 返回true表示是UDP会话
     */
    boolean isUdp();

    /**
     * 设置当前会话状态
     *
     * @param status 会话状态对象
     */
    void status(Status status);

    /**
     * 立即关闭
     *
     * @throws IOException 可能产生的I/O异常
     */
    void closeNow() throws IOException;

    /**
     * 从会话读取数据
     *
     * @throws IOException 可能产生的I/O异常
     */
    void read() throws IOException;

    /**
     * 向会话写入数据
     *
     * @throws IOException 可能产生的I/O异常
     */
    void write() throws IOException;
}
