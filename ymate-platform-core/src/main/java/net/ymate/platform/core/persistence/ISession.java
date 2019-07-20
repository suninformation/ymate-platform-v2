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
package net.ymate.platform.core.persistence;

import net.ymate.platform.core.beans.annotation.Ignored;
import net.ymate.platform.core.support.IDestroyable;

/**
 * 基准会话接口
 *
 * @param <CONNECTION_HOLDER>
 * @author 刘镇 (suninformation@163.com) on 15/11/22 下午8:40
 */
@Ignored
public interface ISession<CONNECTION_HOLDER extends IConnectionHolder> extends IDestroyable {

    /**
     * 获取会话唯一标识
     *
     * @return 返回会话对象唯一标识
     */
    String getId();

    /**
     * 获取当前会话事件监听器
     *
     * @return 返回会话事件监听器对象
     */
    ISessionEventListener getSessionEventListener();

    /**
     * 设置会话事件监听器
     *
     * @param sessionEventListener 事件监听器接口
     */
    void setSessionEventListener(ISessionEventListener sessionEventListener);

    /**
     * 获取连接对象
     *
     * @return 返回连接对象
     */
    CONNECTION_HOLDER getConnectionHolder();
}
