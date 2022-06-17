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
package net.ymate.platform.serv;

import net.ymate.platform.commons.ISpeedListener;
import net.ymate.platform.commons.Speedometer;

import java.io.IOException;
import java.util.Collection;

/**
 * 客户端会话管理器接口
 *
 * @param <SESSION_WRAPPER> 会话包装器类型
 * @param <SESSION_ID>      会话标识类型
 * @param <MESSAGE_TYPE>    消息类型
 * @author 刘镇 (suninformation@163.com) on 2018/11/14 11:10 AM
 */
public interface ISessionManager<SESSION_WRAPPER extends ISessionWrapper<?, ?>, SESSION_ID, MESSAGE_TYPE> extends AutoCloseable {

    /**
     * 初始化
     *
     * @throws Exception 初始过程中产生的任何异常
     */
    void initialize() throws Exception;

    /**
     * 获取指定sessionId的会话包装器实例
     *
     * @param sessionId 会话ID
     * @return 若不存在则返回null
     */
    SESSION_WRAPPER sessionWrapper(SESSION_ID sessionId);

    /**
     * 返回所有的会话包装器实例(只读)
     *
     * @return 会话包装器实例集合
     */
    Collection<SESSION_WRAPPER> sessionWrappers();

    /**
     * 判断指定的sessionId是否存在
     *
     * @param sessionId 会话ID
     * @return 若不存在则返回false
     */
    boolean contains(SESSION_ID sessionId);

    /**
     * 关闭会话并从会话管理器中移除
     *
     * @param sessionWrapper 会话包装器实例
     */
    void closeSessionWrapper(SESSION_WRAPPER sessionWrapper);

    /**
     * 获取会话实例数量
     *
     * @return 会话数量值
     */
    long sessionCount();

    /**
     * 获取会话事件监听器
     *
     * @return 会话事件监听器接口实现
     */
    ISessionListener<SESSION_WRAPPER> getSessionListener();

    /**
     * 设置速度计数器(仅在服务启动前调用有效)
     *
     * @param speedometer 速度计数器
     * @see ISessionManager#speedometer(ISpeedListener, int, int)
     * @deprecated
     */
    @Deprecated
    void speedometer(Speedometer speedometer);

    /**
     * 自定义速度计数器监听器(仅在服务启动前调用有效)
     *
     * @param listener 监听器接口实现
     * @param interval 时间间隔(毫秒)
     * @param dataSize 抽样数据数量
     */
    void speedometer(ISpeedListener listener, int interval, int dataSize);

    /**
     * 自定义速度计数器监听器(仅在服务启动前调用有效)
     *
     * @param listener 监听器接口实现
     */
    void speedometer(ISpeedListener listener);

    /**
     * 使用默认速度计数器监听器(仅在服务启动前调用有效)
     *
     * @param interval 时间间隔(毫秒)
     * @param dataSize 抽样数据数量
     */
    void speedometer(int interval, int dataSize);

    /**
     * 使用默认速度计数器监听器(仅在服务启动前调用有效)
     *
     * @param interval 时间间隔(毫秒)
     */
    void speedometer(int interval);

    /**
     * 设置会话空闲检查器
     *
     * @param sessionIdleChecker 会话空闲检查器接口实现类
     */
    void idleChecker(ISessionIdleChecker<SESSION_WRAPPER, SESSION_ID, MESSAGE_TYPE> sessionIdleChecker);

    /**
     * 向目标客户端发送消息
     *
     * @param sessionId 目标客户端会话唯一标识
     * @param message   消息对象
     * @return 若指定的目标客户端标识存在且有效则返回true
     * @throws IOException 可能产生的异常
     */
    boolean sendTo(SESSION_ID sessionId, MESSAGE_TYPE message) throws IOException;
}
