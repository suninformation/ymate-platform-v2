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

import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.configuration.IConfig;
import net.ymate.platform.core.configuration.IConfigReader;
import net.ymate.platform.core.configuration.IConfigurationConfig;
import net.ymate.platform.core.configuration.IConfigurationProvider;
import net.ymate.platform.core.module.IModuleConfigurer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;

import static net.ymate.platform.commons.util.RuntimeUtils.USER_DIR;
import static net.ymate.platform.commons.util.RuntimeUtils.VAR_ROOT;

/**
 * 默认配置体系配置类
 *
 * @author 刘镇 (suninformation@163.com) on 15/3/16 下午6:57
 */
public final class DefaultConfigurationConfig implements IConfigurationConfig {

    private static final Log LOG = LogFactory.getLog(DefaultConfigurationConfig.class);

    private String configHome;

    private String projectName;

    private String moduleName;

    private int configCheckTimeInterval;

    private Class<? extends IConfigurationProvider> configurationProviderClass;

    private boolean initialized;

    public static IConfigurationConfig defaultConfig() {
        return builder().build();
    }

    public static IConfigurationConfig create(IModuleConfigurer moduleConfigurer) {
        return new DefaultConfigurationConfig(moduleConfigurer);
    }

    public static Builder builder() {
        return new Builder();
    }

    private DefaultConfigurationConfig() {
    }

    @SuppressWarnings("unchecked")
    private DefaultConfigurationConfig(IModuleConfigurer moduleConfigurer) {
        IConfigReader configReader = moduleConfigurer.getConfigReader();
        //
        configHome = configReader.getString(CONFIG_HOME);
        projectName = configReader.getString(PROJECT_NAME);
        moduleName = configReader.getString(MODULE_NAME);
        //
        configCheckTimeInterval = configReader.getInt(CONFIG_CHECK_TIME_INTERVAL);
        //
        try {
            configurationProviderClass = (Class<? extends IConfigurationProvider>) ClassUtils.loadClass(configReader.getString(PROVIDER_CLASS, DefaultConfigurationProvider.class.getName()), this.getClass());
        } catch (ClassNotFoundException e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
            }
        }
    }

    @Override
    public void initialize(IConfig owner) throws Exception {
        if (!initialized) {
            if (StringUtils.isBlank(configHome)) {
                // 尝试通过运行时变量或系统变量获取参数值, 运行时变量优先于环境变量
                configHome = StringUtils.defaultIfBlank(System.getProperty(IApplication.SYSTEM_CONFIG_HOME), System.getenv(IConfig.YMP_CONFIG_HOME));
            }
            configHome = StringUtils.replace(RuntimeUtils.replaceEnvVariable(StringUtils.defaultIfEmpty(configHome, VAR_ROOT)), "%20", StringUtils.SPACE);
            //
            if (configurationProviderClass == null) {
                configurationProviderClass = DefaultConfigurationProvider.class;
            }
            //
            File configHomeFile = new File(configHome);
            if (!configHomeFile.exists() || !configHomeFile.isDirectory()) {
                throw new IllegalArgumentException("CONFIG_HOME invalid directory.");
            }
            configHome = configHomeFile.getPath();
            System.setProperty(USER_DIR, configHome);
            //
            initialized = true;
        }
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public String getConfigHome() {
        return configHome;
    }

    public void setConfigHome(String configHome) {
        if (!initialized) {
            this.configHome = configHome;
        }
    }

    @Override
    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        if (!initialized) {
            this.projectName = projectName;
        }
    }

    @Override
    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        if (!initialized) {
            this.moduleName = moduleName;
        }
    }

    @Override
    public long getConfigCheckTimeInterval() {
        return configCheckTimeInterval;
    }

    public void setConfigCheckTimeInterval(int configCheckTimeInterval) {
        if (!initialized) {
            this.configCheckTimeInterval = configCheckTimeInterval;
        }
    }

    @Override
    public Class<? extends IConfigurationProvider> getConfigurationProviderClass() {
        return configurationProviderClass;
    }

    public void setConfigurationProviderClass(Class<? extends IConfigurationProvider> configurationProviderClass) {
        if (!initialized) {
            this.configurationProviderClass = configurationProviderClass;
        }
    }

    public final static class Builder {

        private final DefaultConfigurationConfig config = new DefaultConfigurationConfig();

        private Builder() {
        }

        public Builder configHome(String configHome) {
            config.setConfigHome(configHome);
            return this;
        }

        public Builder projectName(String projectName) {
            config.setProjectName(projectName);
            return this;
        }

        public Builder moduleName(String moduleName) {
            config.setModuleName(moduleName);
            return this;
        }

        public Builder configCheckTimeInterval(int configCheckTimeInterval) {
            config.setConfigCheckTimeInterval(configCheckTimeInterval);
            return this;
        }

        public Builder configurationProviderClass(Class<? extends IConfigurationProvider> configurationProviderClass) {
            config.setConfigurationProviderClass(configurationProviderClass);
            return this;
        }

        public IConfigurationConfig build() {
            return config;
        }
    }
}
