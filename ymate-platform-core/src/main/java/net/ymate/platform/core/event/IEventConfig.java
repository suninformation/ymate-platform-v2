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
package net.ymate.platform.core.event;

import net.ymate.platform.core.beans.annotation.Ignored;

/**
 * 事件配置接口
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/16 上午2:42
 */
@Ignored
public interface IEventConfig {

    String PROVIDER_CLASS = "provider_class";

    String DEFAULT_MODE = "default_mode";

    String THREAD_POOL_SIZE = "thread_pool_size";

    String THREAD_MAX_POOL_SIZE = "thread_max_pool_size";

    String THREAD_QUEUE_SIZE = "thread_queue_size";

    /**
     * 初始化
     */
    void initialize();

    /**
     * 是否已初始化
     *
     * @return 返回true表示已初始化
     */
    boolean isInitialized();

    /**
     * 获取事件管理提供者接口实现，默认为net.ymate.platform.core.event.impl.DefaultEventProvider
     *
     * @return 返回事件管理提供者接口对象
     */
    IEventProvider getEventProvider();

    /**
     * 获取默认事件触发模式，取值范围：NORMAL-同步执行，ASYNC-异步执行，默认为ASYNC
     *
     * @return 返回默认事件触发模式
     */
    Events.MODE getDefaultMode();

    /**
     * 获取初始化线程池大小，默认为 Runtime.getRuntime().availableProcessors()
     *
     * @return 返回初始化线程池大小
     */
    int getThreadPoolSize();

    /**
     * 获取最大线程池大小，默认为 200
     *
     * @return 返回最大线程池大小
     */
    int getThreadMaxPoolSize();

    /**
     * 获取线程队列大小，默认为 1024
     *
     * @return 返回线程队列大小
     */
    int getThreadQueueSize();
}
