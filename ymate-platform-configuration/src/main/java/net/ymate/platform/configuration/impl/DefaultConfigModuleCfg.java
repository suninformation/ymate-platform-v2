/*
 * Copyright 2007-2017 the original author or authors.
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

import net.ymate.platform.configuration.IConfig;
import net.ymate.platform.configuration.IConfigModuleCfg;
import net.ymate.platform.configuration.IConfigurationProvider;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.support.IConfigReader;
import net.ymate.platform.core.support.impl.MapSafeConfigReader;
import net.ymate.platform.core.util.ClassUtils;
import net.ymate.platform.core.util.RuntimeUtils;
import org.apache.commons.lang.StringUtils;

/**
 * 默认配置体系模块配置类
 *
 * @author 刘镇 (suninformation@163.com) on 15/3/16 下午6:57
 * @version 1.0
 */
public class DefaultConfigModuleCfg implements IConfigModuleCfg {

    private String configHome;
    private String projectName;
    private String moduleName;
    private long configCheckTimeInterval;
    private Class<? extends IConfigurationProvider> providerClass;

    @SuppressWarnings("unchecked")
    public DefaultConfigModuleCfg(YMP owner) {
        IConfigReader _moduleCfg = MapSafeConfigReader.bind(owner.getConfig().getModuleConfigs(IConfig.MODULE_NAME));
        //
        this.configHome = StringUtils.defaultIfBlank(System.getProperty(SYSTEM_CONFIG_HOME), _moduleCfg.getString(CONFIG_HOME));
        if (StringUtils.isBlank(this.configHome)) {
            // 尝试通过运行时变量或系统变量获取CONFIG_HOME参数
            this.configHome = StringUtils.defaultIfBlank(System.getenv(IConfig.__CONFIG_HOME), RuntimeUtils.getSystemEnv(IConfig.__CONFIG_HOME));
        }
        this.configHome = RuntimeUtils.replaceEnvVariable(StringUtils.defaultIfEmpty(this.configHome, "${root}"));
        //
        this.projectName = _moduleCfg.getString(PROJECT_NAME);
        this.moduleName = _moduleCfg.getString(MODULE_NAME);
        //
        this.configCheckTimeInterval = _moduleCfg.getLong(CONFIG_CHECK_TIME_INTERVAL);
        //
        try {
            this.providerClass = (Class<? extends IConfigurationProvider>) ClassUtils.loadClass(_moduleCfg.getString(PROVIDER_CLASS, DefaultConfigurationProvider.class.getName()), this.getClass());
            if (this.providerClass == null) {
                this.providerClass = DefaultConfigurationProvider.class;
            }
        } catch (Exception e) {
            this.providerClass = DefaultConfigurationProvider.class;
        }
    }

    @Override
    public String getConfigHome() {
        return this.configHome;
    }

    @Override
    public String getProjectName() {
        return this.projectName;
    }

    @Override
    public String getModuleName() {
        return this.moduleName;
    }

    @Override
    public long getConfigCheckTimeInterval() {
        return this.configCheckTimeInterval;
    }

    @Override
    public Class<? extends IConfigurationProvider> getProviderClass() {
        return this.providerClass;
    }
}