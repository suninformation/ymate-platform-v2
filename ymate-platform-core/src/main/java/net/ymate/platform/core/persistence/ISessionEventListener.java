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

/**
 * 会话事件监听器接口
 *
 * @param <EVENT_CONTEXT> 事件上下文类型
 * @author 刘镇 (suninformation@163.com) on 2011-9-27 下午03:46:08
 */
@Ignored
public interface ISessionEventListener<EVENT_CONTEXT extends SessionEventContext> {

    /**
     * 查询操用之前事件调用
     *
     * @param eventContext 事件上下文对象
     * @throws Exception 可能产生的任何异常，将中断本次操作
     */
    default void onQueryBefore(EVENT_CONTEXT eventContext) throws Exception {
    }

    /**
     * 查询操作之后事件调用
     *
     * @param eventContext 事件上下文对象
     * @throws Exception 可能产生的任何异常，将中断本次操作
     */
    default void onQueryAfter(EVENT_CONTEXT eventContext) throws Exception {
    }

    /**
     * 插入操用之前事件调用
     *
     * @param eventContext 事件上下文对象
     * @throws Exception 可能产生的任何异常，将中断本次操作
     */
    default void onInsertBefore(EVENT_CONTEXT eventContext) throws Exception {
    }

    /**
     * 插入操作之后事件调用
     *
     * @param eventContext 事件上下文对象
     * @throws Exception 可能产生的任何异常，将中断本次操作
     */
    default void onInsertAfter(EVENT_CONTEXT eventContext) throws Exception {
    }

    /**
     * 插入(如果记录不存在)操用之前事件调用
     *
     * @param eventContext 事件上下文对象
     * @throws Exception 可能产生的任何异常，将中断本次操作
     */
    default void onInsertIfNotExistBefore(EVENT_CONTEXT eventContext) throws Exception {
    }

    /**
     * 插入(如果记录不存在)操作之后事件调用
     *
     * @param eventContext 事件上下文对象
     * @throws Exception 可能产生的任何异常，将中断本次操作
     */
    default void onInsertIfNotExistAfter(EVENT_CONTEXT eventContext) throws Exception {
    }

    /**
     * 更新操作之前事件调用
     *
     * @param eventContext 事件上下文对象
     * @throws Exception 可能产生的任何异常，将中断本次操作
     */
    default void onUpdateBefore(EVENT_CONTEXT eventContext) throws Exception {
    }

    /**
     * 更新操作之后事件调用
     *
     * @param eventContext 事件上下文对象
     * @throws Exception 可能产生的任何异常，将中断本次操作
     */
    default void onUpdateAfter(EVENT_CONTEXT eventContext) throws Exception {
    }

    /**
     * 更新插入操作之前事件调用
     *
     * @param eventContext 事件上下文对象
     * @throws Exception 可能产生的任何异常，将中断本次操作
     */
    default void onUpsertBefore(EVENT_CONTEXT eventContext) throws Exception {
    }

    /**
     * 更新插入操作之后事件调用
     *
     * @param eventContext 事件上下文对象
     * @throws Exception 可能产生的任何异常，将中断本次操作
     */
    default void onUpsertAfter(EVENT_CONTEXT eventContext) throws Exception {
    }

    /**
     * 删除操作之前事件调用
     *
     * @param eventContext 事件上下文对象
     * @throws Exception 可能产生的任何异常，将中断本次操作
     */
    default void onRemoveBefore(EVENT_CONTEXT eventContext) throws Exception {
    }

    /**
     * 删除操作之后事件调用
     *
     * @param eventContext 事件上下文对象
     * @throws Exception 可能产生的任何异常，将中断本次操作
     */
    default void onRemoveAfter(EVENT_CONTEXT eventContext) throws Exception {
    }
}
