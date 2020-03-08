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
package net.ymate.platform.plugin;

import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.beans.annotation.Ignored;
import net.ymate.platform.core.support.IDestroyable;
import net.ymate.platform.core.support.IInitialization;

/**
 * 插件管理器接口
 *
 * @author 刘镇 (suninformation@163.com) on 15/7/9 下午2:09
 */
@Ignored
public interface IPlugins extends IInitialization<IApplication>, IDestroyable {

    String MODULE_NAME = "plugin";

    /**
     * 获取所属应用容器实例
     *
     * @return 返回所属应用容器实例
     */
    IApplication getOwner();

    /**
     * 获取插件默认工厂配置
     *
     * @return 返回插件工厂配置对象, 若插件模块被禁用则返回null
     */
    IPluginConfig getConfig();

    /**
     * 通过ID获取默认插件工厂中的插件实例
     *
     * @param id 插件唯一ID
     * @return 返回插件实例
     */
    IPlugin getPlugin(String id);

    /**
     * 通过接口类型获取默认插件工厂中的插件实例
     *
     * @param clazz 插件接口类
     * @param <T>   插件接口类型
     * @return 返回插件实例
     */
    <T> T getPlugin(Class<T> clazz);
}
