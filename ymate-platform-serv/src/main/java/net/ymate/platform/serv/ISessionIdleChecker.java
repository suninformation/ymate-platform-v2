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

import net.ymate.platform.core.beans.annotation.Ignored;
import net.ymate.platform.core.support.IDestroyable;

import java.util.Map;

/**
 * 会话空闲检查器接口
 *
 * @author 刘镇 (suninformation@163.com) on 2018/11/21 7:26 PM
 */
@Ignored
public interface ISessionIdleChecker<SESSION_WRAPPER extends ISessionWrapper, SESSION_ID, MESSAGE_TYPE> extends IDestroyable {

    /**
     * 初始化
     *
     * @param sessionManager 会话管理器接口实现
     */
    void initialize(ISessionManager<SESSION_WRAPPER, SESSION_ID, MESSAGE_TYPE> sessionManager);

    /**
     * 获取会话管理器
     *
     * @return 会话管理器接口实现
     */
    ISessionManager<SESSION_WRAPPER, SESSION_ID, MESSAGE_TYPE> getSessionManager();

    /**
     * 判断是否已初始化
     *
     * @return 返回true表示已初始化
     */
    boolean isInitialized();

    /**
     * 执行空闲会话检查
     *
     * @param sessions         会话映射
     * @param idleTimeInMillis 会话空闲时间毫秒值
     */
    void processIdleSession(Map<SESSION_ID, SESSION_WRAPPER> sessions, long idleTimeInMillis);
}
