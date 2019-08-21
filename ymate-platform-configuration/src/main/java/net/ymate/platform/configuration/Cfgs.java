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
import net.ymate.platform.commons.util.ResourceUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.configuration.annotation.Configuration;
import net.ymate.platform.configuration.impl.DefaultConfiguration;
import net.ymate.platform.configuration.impl.DefaultConfigurationConfig;
import net.ymate.platform.configuration.impl.DefaultConfigurationProvider;
import net.ymate.platform.configuration.impl.PropertyConfigurationProvider;
import net.ymate.platform.configuration.support.ConfigFileChecker;
import net.ymate.platform.core.IApplicationConfigureFactory;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.configuration.IConfig;
import net.ymate.platform.core.configuration.IConfiguration;
import net.ymate.platform.core.configuration.IConfigurationConfig;
import net.ymate.platform.core.configuration.IConfigurationProvider;
import net.ymate.platform.core.module.IModuleConfigurer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;

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
        IApplicationConfigureFactory configureFactory = YMP.getConfigureFactory();
        IConfig configInst;
        IModuleConfigurer moduleConfigurer;
        if (configureFactory == null || configureFactory.getConfigurer() == null || (moduleConfigurer = configureFactory.getConfigurer().getModuleConfigurer(MODULE_NAME)) == null) {
            configInst = new Cfgs(DefaultConfigurationConfig.defaultConfig());
        } else {
            configInst = new Cfgs(DefaultConfigurationConfig.create(moduleConfigurer));
        }
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

    private ConfigFileChecker fileChecker;

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
            if (config.getConfigCheckTimeInterval() > 0) {
                fileChecker = new ConfigFileChecker(config.getConfigCheckTimeInterval());
                fileChecker.start();
            }
            //
            if (LOG.isInfoEnabled()) {
                LOG.info(String.format("-->  CONFIG_HOME: %s", config.getConfigHome()));
                LOG.info(String.format("-->    USER_HOME: %s", userHome));
                LOG.info(String.format("-->     USER_DIR: %s", userDir));
                if (StringUtils.isNotBlank(config.getProjectName())) {
                    LOG.info(String.format("--> PROJECT_NAME: %s", config.getProjectName()));
                }
                if (StringUtils.isNotBlank(config.getModuleName())) {
                    LOG.info(String.format("-->  MODULE_NAME: %s", config.getModuleName()));
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
    public void close() {
        if (initialized) {
            initialized = false;
            //
            if (fileChecker != null && fileChecker.isInitialized()) {
                fileChecker.stop();
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

    private File doSearch(String cfgFile) {
        if (initialized) {
            // 若指定的 cfgFile 为文件绝对路径名，则直接返回
            File result = new File(cfgFile);
            if (result.isAbsolute() && result.canRead() && result.isFile() && result.exists()) {
                return result;
            }
            // 按路径顺序寻找 cfgFile 指定的文件
            String[] paths = {moduleHome, projectHome, config.getConfigHome(), userDir, userHome};
            for (String path : paths) {
                result = new File(path, cfgFile);
                if (result.isAbsolute() && result.canRead() && result.isFile() && result.exists()) {
                    return result;
                }
            }
        }
        return null;
    }

    @Override
    public String searchAsPath(String cfgFile) {
        if (StringUtils.isNotBlank(cfgFile)) {
            if (cfgFile.startsWith(FILE_PREFIX_JAR)) {
                return cfgFile;
            }
            File targetFile = doSearch(cfgFile);
            if (targetFile == null) {
                URL targetFileUrl = ResourceUtils.getResource(cfgFile, this.getClass());
                if (targetFileUrl != null) {
                    targetFile = FileUtils.toFile(targetFileUrl);
                    if (targetFile != null) {
                        return targetFileUrl.toString();
                    }
                }
            }
            if (targetFile != null) {
                return targetFile.getPath();
            }
        }
        return null;
    }

    @Override
    public File searchAsFile(String cfgFile) {
        if (StringUtils.isNotBlank(cfgFile)) {
            return doSearch(cfgFile);
        }
        return null;
    }

    @Override
    public InputStream searchAsStream(String cfgFile) {
        String filePath = searchAsPath(cfgFile);
        try {
            return filePath != null ? new FileInputStream(new File(filePath)) : null;
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    @Override
    public IConfiguration loadCfg(String cfgFileName, boolean search) {
        if (StringUtils.isNotBlank(cfgFileName)) {
            Class<? extends IConfigurationProvider> provClass;
            String extName = FileUtils.getExtName(cfgFileName);
            if (StringUtils.equalsIgnoreCase(extName, FILE_SUFFIX_XML)) {
                provClass = DefaultConfigurationProvider.class;
            } else if (StringUtils.equalsIgnoreCase(extName, FILE_SUFFIX_PROPERTIES)) {
                provClass = PropertyConfigurationProvider.class;
            } else {
                provClass = config.getConfigurationProviderClass();
            }
            if (provClass != null) {
                return fillCfg(provClass, new DefaultConfiguration(), cfgFileName, search);
            }
        }
        return null;
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
            if (StringUtils.isBlank(cfgFileName)) {
                cfgFileName = configObject.getClass().getSimpleName().toLowerCase().concat(configObject.getTagName()).concat(".xml");
            }
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
                String targetCfgFile = search ? searchAsPath(cfgFileName) : cfgFileName;
                if (StringUtils.isNotBlank(targetCfgFile)) {
                    try {
                        IConfigurationProvider provider = null;
                        if (providerClass != null) {
                            provider = ClassUtils.impl(providerClass, IConfigurationProvider.class);
                        }
                        if (provider == null) {
                            provider = config.getConfigurationProviderClass().newInstance();
                        }
                        provider.load(targetCfgFile);
                        configObject.initialize(provider);
                        //
                        if (fileChecker != null && reload) {
                            fileChecker.putFileStatus(targetCfgFile, new ConfigFileChecker.FileStatus(configObject, new File(targetCfgFile).lastModified()));
                        }
                        //
                        return configObject;
                    } catch (Exception e) {
                        LOG.warn(String.format("An exception occurred while filling the config file [%s]: ", StringUtils.trimToEmpty(cfgFileName)), RuntimeUtils.unwrapThrow(e));
                    }
                } else {
                    LOG.warn(String.format("Cfgs file [%s] not found.", StringUtils.trimToEmpty(cfgFileName)));
                }
            }
        } else {
            LOG.warn("Cfgs has not been initialized, unable to filling operation.");
        }
        return null;
    }
}
