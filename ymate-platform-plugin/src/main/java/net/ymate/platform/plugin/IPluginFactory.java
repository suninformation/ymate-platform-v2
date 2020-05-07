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
import net.ymate.platform.core.beans.IBeanLoader;
import net.ymate.platform.core.beans.annotation.Ignored;
import net.ymate.platform.core.support.IDestroyable;
import net.ymate.platform.core.support.IInitialization;

import java.util.Collection;

/**
 * 插件工厂接口定义
 *
 * @author 刘镇 (suninformation@163.com) on 2011-10-17 下午04:49:03
 */
@Ignored
public interface IPluginFactory extends IInitialization<IApplication>, IDestroyable {

    /**
     * 启动插件
     *
     * @throws Exception 可能产生的任何异常
     */
    void startup() throws Exception;

    /**
     * 获取所属应用容器管理器
     *
     * @return 返回所属应用容器管理器
     */
    IApplication getOwner();

    /**
     * 获取插件工厂配置
     *
     * @return 返回插件工厂配置对象
     */
    IPluginConfig getPluginConfig();

    /**
     * 获取对象加载器
     *
     * @return 返回对象加载器实例
     */
    IBeanLoader getBeanLoader();

    /**
     * 通过插件唯一标识获取插件配置信息元数据描述
     *
     * @param idOrAlias 插件唯一标识或别名
     * @return 返回插件配置信息元数据描述
     */
    PluginMeta getPluginMeta(String idOrAlias);

    /**
     * 获取插件配置信息描述对象集合
     *
     * @return 返回插件配置信息描述对象集合
     */
    Collection<PluginMeta> getPluginMetas();

    /**
     * 通过插件唯一标识获取插件实例
     *
     * @param idOrAlias 插件唯一标识或别名
     * @return 返回插件实例
     */
    IPlugin getPlugin(String idOrAlias);

    /**
     * 通过接口类型获取插件实例
     *
     * @param clazz 插件接口类
     * @param <T>   插件接口类型
     * @return 返回插件实例
     */
    <T> T getPlugin(Class<T> clazz);

    /**
     * 是否扫描当前CLASSPATH内的相关插件
     *
     * @return 返回true表示加载
     */
    boolean isIncludedClassPath();
}
