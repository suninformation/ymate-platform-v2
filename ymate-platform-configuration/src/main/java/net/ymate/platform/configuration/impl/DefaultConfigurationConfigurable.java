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
package net.ymate.platform.configuration.impl;

import net.ymate.platform.core.configuration.IConfig;
import net.ymate.platform.core.configuration.IConfigurationConfig;
import net.ymate.platform.core.configuration.IConfigurationProvider;
import net.ymate.platform.core.module.IModuleConfigurer;
import net.ymate.platform.core.module.impl.DefaultModuleConfigurable;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-08-12 16:43
 * @since 2.1.0
 */
public final class DefaultConfigurationConfigurable extends DefaultModuleConfigurable {

    public static Builder builder() {
        return new Builder();
    }

    private DefaultConfigurationConfigurable() {
        super(IConfig.MODULE_NAME);
    }

    public static final class Builder {

        private final DefaultConfigurationConfigurable configurable = new DefaultConfigurationConfigurable();

        private Builder() {
        }

        public Builder configHome(String configHome) {
            configurable.addConfig(IConfigurationConfig.CONFIG_HOME, configHome);
            return this;
        }

        public Builder projectName(String projectName) {
            configurable.addConfig(IConfigurationConfig.PROJECT_NAME, projectName);
            return this;
        }

        public Builder moduleName(String moduleName) {
            configurable.addConfig(IConfigurationConfig.MODULE_NAME, moduleName);
            return this;
        }

        public Builder configBaseDir(String configBaseDir) {
            configurable.addConfig(IConfigurationConfig.CONFIG_BASE_DIR, configBaseDir);
            return this;
        }

        public Builder configCheckTimeInterval(int configCheckTimeInterval) {
            configurable.addConfig(IConfigurationConfig.CONFIG_CHECK_TIME_INTERVAL, String.valueOf(configCheckTimeInterval));
            return this;
        }

        public Builder configurationProviderClass(Class<? extends IConfigurationProvider> configurationProviderClass) {
            configurable.addConfig(IConfigurationConfig.PROVIDER_CLASS, configurationProviderClass.getName());
            return this;
        }

        public IModuleConfigurer build() {
            return configurable.toModuleConfigurer();
        }
    }
}
