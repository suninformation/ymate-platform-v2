/*
 * Copyright 2007-2020 the original author or authors.
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
package net.ymate.platform.core.annotation;

import net.ymate.platform.core.event.Events;
import net.ymate.platform.core.event.IEventProvider;

import java.lang.annotation.*;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/03/08 18:22
 * @since 2.1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EventsConf {

    /**
     * @return 默认事件触发模式
     */
    Events.MODE mode() default Events.MODE.ASYNC;

    /**
     * @return 事件管理提供者
     */
    Class<? extends IEventProvider> providerClass() default IEventProvider.class;

    /**
     * @return 初始化线程池大小
     */
    int threadPoolSize() default 0;

    /**
     * @return 最大线程池大小
     */
    int threadMaxPoolSize() default 0;

    /**
     * @return 线程队列大小
     */
    int threadQueueSize() default 0;
}
