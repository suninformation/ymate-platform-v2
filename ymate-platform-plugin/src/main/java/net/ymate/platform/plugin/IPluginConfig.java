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

import net.ymate.platform.core.beans.annotation.Ignored;

import java.io.File;
import java.util.List;

/**
 * 插件初始化配置接口
 *
 * @author 刘镇 (suninformation@163.com) on 2012-11-30 下午6:35:14
 */
@Ignored
public interface IPluginConfig {

    String ENABLED = "enabled";

    String DISABLED = "disabled";

    String PLUGIN_HOME = "plugin_home";

    String PACKAGE_NAMES = "package_names";

    String AUTOMATIC = "automatic";

    String INCLUDED_CLASSPATH = "included_classpath";

    String PARAMS_PLUGIN_DISABLED_PREFIX = "plugin.";

    /**
     * 插件工厂自动扫描的包名前缀集合
     *
     * @return 返回插件工厂自动扫描的包名前缀集合
     */
    List<String> getPackageNames();

    /**
     * 插件生命周期事件监听器
     *
     * @return 返回插件生命周期事件监听器接口实例
     */
    IPluginEventListener getPluginEventListener();

    /**
     * 插件对象加载器工厂对象
     *
     * @return 返回插件对象加载器工厂实例
     */
    IPluginBeanLoaderFactory getPluginBeanLoaderFactory();

    /**
     * 是否允许插件自动启动
     *
     * @return 返回true表示允许插件自动启动
     */
    boolean isAutomatic();

    /**
     * 是否启用插件模块, 默认为true
     *
     * @return 返回true表示开启
     */
    boolean isEnabled();

    /**
     * 插件存放路径
     *
     * @return 返回插件存放路径
     */
    File getPluginHome();
}
