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
package net.ymate.platform.core.plugin;

import java.io.File;
import java.util.List;

/**
 * 插件初始化配置接口
 *
 * @author 刘镇 (suninformation@163.com) on 2012-11-30 下午6:35:14
 * @version 1.0
 */
public interface IPluginConfig {

    public static final String DEFAULT_MANIFEST_FILE = "ymate.plugin.xml";

    /**
     * @return 返回插件工厂自动扫描的包路径前缀集合
     */
    public List<String> getAutoscanPackages();

    /**
     * @return 返回插件配置文件分析器接口实例
     */
    public IPluginParser getPluginParser();

    /**
     * @return 返回插件扩展内容解析器接口实例
     */
    public IPluginExtendParser<?> getPluginExtendParser();

    /**
     * @return 返回插件生命周期事件监听器接口实例
     */
    public IPluginEventListener getPluginEventListener();

    /**
     * @return 是否允许插件自动启动
     */
    public boolean isAutomatic();

    /**
     * @return 是否加载当前CLASSPATH内的所有包含插件配置文件的Jar包
     */
    public boolean isIncludedClassPath();

    /**
     * @return 返回插件存放路径
     */
    public File getPluginHome();

    /**
     * @return 返回插件配置文件名称，默认采用：ymate.plugin.xml
     */
    public String getManifestFile();
}
