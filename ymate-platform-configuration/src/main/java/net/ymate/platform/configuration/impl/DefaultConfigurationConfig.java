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
import net.ymate.platform.configuration.annotation.ConfigurationConf;
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

    private long configCheckTimeInterval;

    private Class<? extends IConfigurationProvider> configurationProviderClass;

    private boolean initialized;

    public static DefaultConfigurationConfig defaultConfig() {
        return builder().build();
    }

    public static DefaultConfigurationConfig create(IModuleConfigurer moduleConfigurer) {
        return new DefaultConfigurationConfig(null, moduleConfigurer);
    }

    public static DefaultConfigurationConfig create(Class<?> mainClass, IModuleConfigurer moduleConfigurer) {
        return new DefaultConfigurationConfig(mainClass, moduleConfigurer);
    }

    public static Builder builder() {
        return new Builder();
    }

    private DefaultConfigurationConfig() {
    }

    @SuppressWarnings("unchecked")
    private DefaultConfigurationConfig(Class<?> mainClass, IModuleConfigurer moduleConfigurer) {
        IConfigReader configReader = moduleConfigurer.getConfigReader();
        //
        ConfigurationConf confAnn = mainClass == null ? null : mainClass.getAnnotation(ConfigurationConf.class);
        //
        configHome = configReader.getString(CONFIG_HOME, confAnn == null ? null : confAnn.configHome());
        projectName = configReader.getString(PROJECT_NAME, confAnn == null ? null : confAnn.projectName());
        moduleName = configReader.getString(MODULE_NAME, confAnn == null ? null : confAnn.moduleName());
        //
        configCheckTimeInterval = configReader.getLong(CONFIG_CHECK_TIME_INTERVAL, confAnn == null ? 0 : confAnn.checkTimeInterval());
        //
        try {
            configurationProviderClass = (Class<? extends IConfigurationProvider>) ClassUtils.loadClass(configReader.getString(PROVIDER_CLASS, confAnn == null || confAnn.providerClass().equals(IConfigurationProvider.class) ? null : confAnn.providerClass().getName()), this.getClass());
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
            if (!configHomeFile.isAbsolute()) {
                throw new IllegalArgumentException(String.format("Parameter config_home value [%s] is not an absolute path.", configHomeFile.getPath()));
            } else if (!configHomeFile.exists() || !configHomeFile.isDirectory()) {
                if (configHomeFile.mkdirs()) {
                    if (LOG.isInfoEnabled()) {
                        LOG.info(String.format("Successfully created config_home directory: %s", configHomeFile.getPath()));
                    }
                } else {
                    throw new IllegalArgumentException(String.format("Failed to create config_home directory: %s", configHomeFile.getPath()));
                }
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

        public DefaultConfigurationConfig build() {
            return config;
        }
    }
}
