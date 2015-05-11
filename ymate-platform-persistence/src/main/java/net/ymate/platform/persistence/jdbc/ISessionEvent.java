/*
 * Copyright 2007-2107 the original author or authors.
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
package net.ymate.platform.persistence.jdbc;

/**
 * 会话事件处理接口
 *
 * @author 刘镇 (suninformation@163.com) on 2011-9-27 下午03:46:08
 * @version 1.0
 */
public interface ISessionEvent {

    /**
     * 插入操用之前事件调用
     *
     * @param eventContext
     */
    public void onInsertBefore(SessionEventContext eventContext);

    /**
     * 插入操作之后事件调用
     *
     * @param eventContext
     */
    public void onInsertAfter(SessionEventContext eventContext);

    /**
     * 更新操作之前事件调用
     *
     * @param eventContext
     */
    public void onUpdateBefore(SessionEventContext eventContext);

    /**
     * 更新操作之后事件调用
     *
     * @param eventContext
     */
    public void onUpdateAfter(SessionEventContext eventContext);

    /**
     * 删除操作之前事件调用
     *
     * @param eventContext
     */
    public void onRemoveBefore(SessionEventContext eventContext);

    /**
     * 删除操作之后事件调用
     *
     * @param eventContext
     */
    public void onRemoveAfter(SessionEventContext eventContext);
}
