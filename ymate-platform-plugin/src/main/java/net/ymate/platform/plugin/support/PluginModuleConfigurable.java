/*
 * Copyright 2007-2018 the original author or authors.
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
package net.ymate.platform.plugin.support;

import net.ymate.platform.core.support.IModuleConfigurable;
import net.ymate.platform.plugin.IPluginConfig;
import net.ymate.platform.plugin.IPlugins;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2018-12-14 18:05
 * @version 1.0
 * @since 2.0.6
 */
public class PluginModuleConfigurable implements IModuleConfigurable {

    public static PluginModuleConfigurable create() {
        return new PluginModuleConfigurable();
    }

    private Map<String, String> __configs = new HashMap<String, String>();

    public PluginModuleConfigurable disabled(boolean disabled) {
        __configs.put(IPluginConfig.DISABLED, String.valueOf(disabled));
        return this;
    }

    public PluginModuleConfigurable pluginHome(String pluginHome) {
        __configs.put(IPluginConfig.PLUGIN_HOME, StringUtils.trimToEmpty(pluginHome));
        return this;
    }

    public PluginModuleConfigurable autoscanPackages(String autoscanPackages) {
        __configs.put(IPluginConfig.AUTOSCAN_PACKAGES, StringUtils.trimToEmpty(autoscanPackages));
        return this;
    }

    public PluginModuleConfigurable automatic(boolean automatic) {
        __configs.put(IPluginConfig.AUTOMATIC, String.valueOf(automatic));
        return this;
    }

    public PluginModuleConfigurable includedClasspath(boolean includedClasspath) {
        __configs.put(IPluginConfig.INCLUDED_CLASSPATH, String.valueOf(includedClasspath));
        return this;
    }

    @Override
    public String getModuleName() {
        return IPlugins.MODULE_NAME;
    }

    @Override
    public Map<String, String> toMap() {
        return __configs;
    }
}
