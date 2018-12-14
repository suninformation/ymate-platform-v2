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
package net.ymate.platform.configuration.support;

import net.ymate.platform.configuration.IConfig;
import net.ymate.platform.configuration.IConfigModuleCfg;
import net.ymate.platform.configuration.IConfigurationProvider;
import net.ymate.platform.core.support.IModuleConfigurable;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2018-12-14 11:06
 * @version 1.0
 * @since 2.0.6
 */
public class ConfigModuleConfigurable implements IModuleConfigurable {

    public static ConfigModuleConfigurable create() {
        return new ConfigModuleConfigurable();
    }

    private Map<String, String> __configs = new HashMap<String, String>();

    public ConfigModuleConfigurable configHome(String configHome) {
        __configs.put(IConfigModuleCfg.CONFIG_HOME, StringUtils.trimToEmpty(configHome));
        return this;
    }

    public ConfigModuleConfigurable projectName(String projectName) {
        __configs.put(IConfigModuleCfg.PROJECT_NAME, StringUtils.trimToEmpty(projectName));
        return this;
    }

    public ConfigModuleConfigurable moduleName(String moduleName) {
        __configs.put(IConfigModuleCfg.MODULE_NAME, StringUtils.trimToEmpty(moduleName));
        return this;
    }

    public ConfigModuleConfigurable configCheckTimeInterval(long configCheckTimeInterval) {
        __configs.put(IConfigModuleCfg.CONFIG_CHECK_TIME_INTERVAL, String.valueOf(configCheckTimeInterval));
        return this;
    }

    public ConfigModuleConfigurable providerClass(Class<? extends IConfigurationProvider> providerClass) {
        __configs.put(IConfigModuleCfg.PROVIDER_CLASS, providerClass.getName());
        return this;
    }

    @Override
    public String getModuleName() {
        return IConfig.MODULE_NAME;
    }

    @Override
    public Map<String, String> toMap() {
        return __configs;
    }
}
