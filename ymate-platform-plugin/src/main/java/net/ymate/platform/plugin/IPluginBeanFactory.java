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

import net.ymate.platform.core.beans.IBeanFactory;
import net.ymate.platform.core.beans.annotation.Ignored;

import java.util.Collection;

/**
 * @author 刘镇 (suninformation@163.com) on 2018-11-28 22:35
 */
@Ignored
public interface IPluginBeanFactory extends IBeanFactory {

    /**
     * 是否加载当前CLASSPATH内的所有包含插件配置文件的Jar包
     *
     * @return 返回true表示包含
     */
    boolean isIncludedClassPath();

    /**
     * 通过插件唯一标识获取插件实例
     *
     * @param idOrAlias 插件唯一标识或别名
     * @return 返回插件实例
     */
    IPlugin getPlugin(String idOrAlias);

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
}
