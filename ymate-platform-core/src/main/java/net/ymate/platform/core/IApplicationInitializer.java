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
package net.ymate.platform.core;

import net.ymate.platform.core.beans.IBeanFactory;
import net.ymate.platform.core.beans.IBeanLoader;
import net.ymate.platform.core.event.Events;
import net.ymate.platform.core.module.ModuleManager;

/**
 * 应用容器扩展初始化处理接口
 *
 * @author 刘镇 (suninformation@163.com) on 2019-08-26 20:30
 * @since 2.1.0
 */
public interface IApplicationInitializer {

    /**
     * 当事件管理器初始化完毕后将调用此方法
     *
     * @param application 应用容器
     * @param events      事件管理器
     */
    void afterEventInit(IApplication application, Events events);

    /**
     * 当对象加载器开始执行加载动作前将调用此方法
     *
     * @param application 应用容器
     * @param beanLoader  对象加载器
     */
    void beforeBeanLoad(IApplication application, IBeanLoader beanLoader);

    /**
     * 当模块管理器执行初始化动作前将调用此方法
     *
     * @param application   应用容器
     * @param moduleManager 模块管理器
     */
    void beforeModuleManagerInit(IApplication application, ModuleManager moduleManager);

    /**
     * 当对象工厂执行初始化动作前将调用此方法
     *
     * @param application 应用容器
     * @param beanFactory 对象工厂
     */
    void beforeBeanFactoryInit(IApplication application, IBeanFactory beanFactory);
}
