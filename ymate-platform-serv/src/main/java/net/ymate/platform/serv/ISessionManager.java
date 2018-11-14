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
package net.ymate.platform.serv;

import net.ymate.platform.core.support.IInitializable;
import net.ymate.platform.core.support.Speedometer;

import java.util.Collection;

/**
 * 客户端会话管理器接口
 *
 * @param <SESSION_WRAPPER> 会话包装器类型
 * @author 刘镇 (suninformation@163.com) on 2018/11/14 11:10 AM
 * @version 1.0
 */
public interface ISessionManager<SESSION_WRAPPER extends ISessionWrapper> extends IInitializable<IServ> {

    /**
     * 获取指定sessionId的会话包装器实例
     *
     * @param sessionId 会话ID
     * @return 若不存在则返回null
     */
    SESSION_WRAPPER getSessionWrapper(String sessionId);

    /**
     * 返回所有的会话包装器实例(只读)
     *
     * @return 会话包装器实例集合
     */
    Collection<SESSION_WRAPPER> getSessionWrappers();

    /**
     * 获取会话实例数量
     *
     * @return 会话数量值
     */
    long getSessionCount();

    /**
     * 设置速度计数器(仅在服务启动前调用有效)
     *
     * @param speedometer 速度计数器
     */
    void speedometer(Speedometer speedometer);
}
