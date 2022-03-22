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
package net.ymate.platform.configuration;

import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.FileUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.configuration.annotation.Configuration;
import net.ymate.platform.configuration.impl.*;
import net.ymate.platform.core.IApplicationConfigureFactory;
import net.ymate.platform.core.IApplicationConfigurer;
import net.ymate.platform.core.Version;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.configuration.*;
import net.ymate.platform.core.module.IModuleConfigurer;
import net.ymate.platform.core.module.impl.DefaultModuleConfigurer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.InputStream;

import static net.ymate.platform.commons.util.FileUtils.*;
import static net.ymate.platform.commons.util.RuntimeUtils.USER_DIR;
import static net.ymate.platform.commons.util.RuntimeUtils.USER_HOME;

/**
 * 配置体系管理器
 *
 * @author 刘镇 (suninformation@163.com) on 2011-8-27 上午02:17:46
 */
public final class Cfgs implements IConfig {

    private static final Log LOG = LogFactory.getLog(Cfgs.class);

    private static final IConfig INSTANCE;

    static {
        IConfigurationConfig config = null;
        IApplicationConfigureFactory configureFactory = YMP.getConfigureFactory();
        if (configureFactory != null) {
            IApplicationConfigurer configurer = configureFactory.getConfigurer();
            IModuleConfigurer moduleConfigurer = configurer == null ? null : configurer.getModuleConfigurer(MODULE_NAME);
            if (moduleConfigurer != null) {
                config = DefaultConfigurationConfig.create(configureFactory.getMainClass(), moduleConfigurer);
            } else {
                config = DefaultConfigurationConfig.create(configureFactory.getMainClass(), DefaultModuleConfigurer.createEmpty(MODULE_NAME));
            }
        }
        IConfig configInst = new Cfgs(config != null ? config : DefaultConfigurationConfig.defaultConfig());
        try {
            configInst.initialize();
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
            }
        }
        INSTANCE = configInst;
    }

    private final IConfigurationConfig config;

    private String projectHome;

    private String moduleHome;

    private String userHome;

    private String userDir;

    private IConfigFileSearcher fileSearcher;

    private IConfigFileChecker fileChecker;

    private boolean initialized;

    public static IConfig get() {
        return INSTANCE;
    }

    private Cfgs(IConfigurationConfig config) {
        this.config = config;
    }

    @Override
    public void initialize() throws Exception {
        if (!initialized) {
            if (LOG.isInfoEnabled()) {
                LOG.info(String.format("Initializing ymate-platform-configuration-%s", new Version(YMP.VERSION, this.getClass())));
            }
            //
            if (!config.isInitialized()) {
                config.initialize(this);
            }
            //
            // 在配置体系主目录（configHome）存在的情况下，处理项目主目录
            if (StringUtils.isNotBlank(config.getProjectName())) {
                projectHome = new File(config.getConfigHome(), PROJECTS_FOLDER_NAME.concat(File.separator).concat(config.getProjectName())).getPath();
                System.setProperty(USER_DIR, projectHome);
                // 在项目主目录（projectHome）存在的情况下，处理模块主目录
                if (StringUtils.isNotBlank(config.getModuleName())) {
                    moduleHome = new File(projectHome, MODULES_FOLDER_NAME.concat(File.separator).concat(config.getModuleName())).getPath();
                    System.setProperty(USER_DIR, moduleHome);
                }
            }
            userHome = System.getProperty(USER_HOME, StringUtils.EMPTY);
            userDir = System.getProperty(USER_DIR, StringUtils.EMPTY);
            //
            fileSearcher = ClassUtils.loadClass(IConfigFileSearcher.class, DefaultConfigFileSearcher.class);
            if (LOG.isInfoEnabled()) {
                LOG.info(String.format("Using ConfigFileSearcher class [%s].", fileSearcher.getClass().getName()));
            }
            fileSearcher.initialize(this);
            //
            if (config.getConfigCheckTimeInterval() > 0) {
                fileChecker = ClassUtils.loadClass(IConfigFileChecker.class, DefaultConfigFileChecker.class);
                if (LOG.isInfoEnabled()) {
                    LOG.info(String.format("Using ConfigFileChecker class [%s].", fileChecker.getClass().getName()));
                }
                fileChecker.initialize(config.getConfigCheckTimeInterval());
            }
            //
            if (LOG.isInfoEnabled()) {
                LOG.info(String.format("-- CONFIG_HOME: %s", config.getConfigHome()));
                LOG.info(String.format("-- USER_HOME: %s", userHome));
                LOG.info(String.format("-- USER_DIR: %s", userDir));
                if (StringUtils.isNotBlank(config.getConfigBaseDir())) {
                    LOG.info(String.format("-- CONFIG_BASE_DIR: %s", config.getConfigBaseDir()));
                }
                if (StringUtils.isNotBlank(config.getProjectName())) {
                    LOG.info(String.format("-- PROJECT_NAME: %s", config.getProjectName()));
                }
                if (StringUtils.isNotBlank(config.getModuleName())) {
                    LOG.info(String.format("-- MODULE_NAME: %s", config.getModuleName()));
                }
            }
            initialized = config.isInitialized();
        }
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void close() throws Exception {
        if (initialized) {
            initialized = false;
            //
            fileSearcher.close();
            if (fileChecker != null) {
                fileChecker.close();
            }
        }
    }

    @Override
    public String getConfigHome() {
        return config.getConfigHome();
    }

    @Override
    public String getProjectName() {
        return config.getProjectName();
    }

    @Override
    public String getProjectHome() {
        return projectHome;
    }

    @Override
    public String getModuleName() {
        return config.getModuleName();
    }

    @Override
    public String getModuleHome() {
        return moduleHome;
    }

    @Override
    public String getUserHome() {
        return userHome;
    }

    @Override
    public String getUserDir() {
        return userDir;
    }

    @Override
    public String searchAsPath(String cfgFile) {
        return fileSearcher.searchAsPath(cfgFile);
    }

    @Override
    public File searchAsFile(String cfgFile) {
        return fileSearcher.search(cfgFile);
    }

    @Override
    public InputStream searchAsStream(String cfgFile) {
        return fileSearcher.searchAsStream(cfgFile);
    }

    @Override
    public IConfiguration loadCfg(String cfgFileName, boolean search) {
        if (StringUtils.isNotBlank(cfgFileName)) {
            Class<? extends IConfigurationProvider> provClass = doParseConfigurationProviderClass(cfgFileName);
            if (provClass != null) {
                return fillCfg(provClass, new DefaultConfiguration(), cfgFileName, search);
            }
        }
        return null;
    }

    private Class<? extends IConfigurationProvider> doParseConfigurationProviderClass(String cfgFileName) {
        String extName = FileUtils.getExtName(cfgFileName);
        if (StringUtils.equalsIgnoreCase(extName, FILE_SUFFIX_XML)) {
            return DefaultConfigurationProvider.class;
        } else if (StringUtils.equalsIgnoreCase(extName, FILE_SUFFIX_PROPERTIES)) {
            return PropertyConfigurationProvider.class;
        } else if (StringUtils.equalsAnyIgnoreCase(extName, FILE_SUFFIX_JSON)) {
            return JSONConfigurationProvider.class;
        }
        return config.getConfigurationProviderClass();
    }

    @Override
    public IConfiguration loadCfg(String cfgFileName) {
        return loadCfg(cfgFileName, true);
    }

    @Override
    public <T extends IConfiguration> T fillCfg(T configObject, String cfgFileName) {
        return fillCfg(configObject, cfgFileName, true);
    }

    @Override
    public <T extends IConfiguration> T fillCfg(T configObject, String cfgFileName, boolean search) {
        return fillCfg(null, configObject, cfgFileName, search);
    }

    @Override
    public <T extends IConfiguration> T fillCfg(T configObject) {
        return fillCfg(configObject, true);
    }

    @Override
    public <T extends IConfiguration> T fillCfg(T configObject, boolean search) {
        if (configObject != null) {
            Configuration configuration = ClassUtils.getAnnotation(configObject, Configuration.class);
            String cfgFileName = configuration == null ? null : configuration.value();
            boolean reload = configuration == null || configuration.reload();
            return fillCfg((configuration == null || configuration.provider().equals(IConfigurationProvider.class) ? null : configuration.provider()), configObject, cfgFileName, search, reload);
        }
        return null;
    }

    @Override
    public <T extends IConfiguration> T fillCfg(Class<? extends IConfigurationProvider> providerClass, T configObject, String cfgFileName, boolean search) {
        return fillCfg(providerClass, configObject, cfgFileName, search, true);
    }

    @Override
    public <T extends IConfiguration> T fillCfg(Class<? extends IConfigurationProvider> providerClass, T configObject, String cfgFileName, boolean search, boolean reload) {
        if (initialized) {
            if (configObject != null) {
                try {
                    IConfigurationProvider provider = null;
                    if (providerClass != null) {
                        provider = ClassUtils.impl(providerClass, IConfigurationProvider.class);
                    }
                    if (provider == null) {
                        provider = doParseConfigurationProviderClass(cfgFileName).newInstance();
                    }
                    cfgFileName = StringUtils.trim(cfgFileName);
                    if (StringUtils.isBlank(cfgFileName)) {
                        cfgFileName = String.format("%s%s.%s", configObject.getClass().getSimpleName().toLowerCase(), configObject.getTagName(), provider.getSupportFileExtName());
                    } else if (StringUtils.isBlank(FileUtils.getExtName(cfgFileName))) {
                        cfgFileName += String.format("%s%s", StringUtils.endsWith(cfgFileName, ".") ? StringUtils.EMPTY : ".", provider.getSupportFileExtName());
                    }
                    if (StringUtils.isNotBlank(config.getConfigBaseDir()) && !StringUtils.startsWith(cfgFileName, config.getConfigBaseDir())) {
                        cfgFileName = String.format("%s%s", config.getConfigBaseDir(), cfgFileName);
                    }
                    String targetCfgFile = search ? searchAsPath(cfgFileName) : cfgFileName;
                    if (StringUtils.isNotBlank(targetCfgFile)) {
                        provider.load(targetCfgFile);
                        configObject.initialize(provider);
                        //
                        if (fileChecker != null && reload) {
                            fileChecker.addStatus(new DefaultConfigFileChecker.Status(configObject, new File(targetCfgFile)));
                        }
                        return configObject;
                    } else if (LOG.isWarnEnabled()) {
                        LOG.warn(String.format("Config file [%s] not found.", StringUtils.trimToEmpty(cfgFileName)));
                    }
                } catch (Exception e) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(String.format("An exception occurred while filling the config file [%s]: ", StringUtils.trimToEmpty(cfgFileName)), RuntimeUtils.unwrapThrow(e));
                    }
                }
            }
        } else if (LOG.isWarnEnabled()) {
            LOG.warn("Configuration module not initialized, fill operation could not be completed.");
        }
        return null;
    }
}
