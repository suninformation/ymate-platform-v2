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
package net.ymate.platform.plugin;

import net.ymate.platform.core.beans.IBeanLoader;
import net.ymate.platform.core.beans.annotation.Ignored;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/03/10 09:45
 * @since 2.1.0
 */
@Ignored
public interface IPluginBeanLoadInitializer {

    /**
     * 当插件对象加载器开始执行加载动作前将调用此方法
     *
     * @param plugins    插件管理器
     * @param beanLoader 对象加载器
     */
    default void beforeBeanLoad(IPlugins plugins, IBeanLoader beanLoader) {
    }
}
