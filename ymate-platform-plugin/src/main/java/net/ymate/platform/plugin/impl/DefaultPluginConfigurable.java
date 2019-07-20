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
package net.ymate.platform.plugin.impl;

import net.ymate.platform.core.module.IModuleConfigurer;
import net.ymate.platform.core.module.impl.DefaultModuleConfigurable;
import net.ymate.platform.plugin.IPluginConfig;
import net.ymate.platform.plugin.IPlugins;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-08-15 01:31
 * @since 2.1.0
 */
public final class DefaultPluginConfigurable extends DefaultModuleConfigurable {

    public static Builder builder() {
        return new Builder();
    }

    private DefaultPluginConfigurable() {
        super(IPlugins.MODULE_NAME);
    }

    public static final class Builder {

        private final DefaultPluginConfigurable configurable = new DefaultPluginConfigurable();

        private Builder() {
        }

        public Builder enabled(boolean enabled) {
            configurable.addConfig(IPluginConfig.ENABLED, String.valueOf(enabled));
            return this;
        }

        public Builder pluginHome(String pluginHome) {
            configurable.addConfig(IPluginConfig.PLUGIN_HOME, pluginHome);
            return this;
        }

        public Builder packageNames(String packageNames) {
            configurable.addConfig(IPluginConfig.PACKAGE_NAMES, packageNames);
            return this;
        }

        public Builder automatic(boolean automatic) {
            configurable.addConfig(IPluginConfig.AUTOMATIC, String.valueOf(automatic));
            return this;
        }

        public Builder includedClasspath(boolean includedClasspath) {
            configurable.addConfig(IPluginConfig.INCLUDED_CLASSPATH, String.valueOf(includedClasspath));
            return this;
        }

        public IModuleConfigurer build() {
            return configurable.toModuleConfigurer();
        }
    }
}
