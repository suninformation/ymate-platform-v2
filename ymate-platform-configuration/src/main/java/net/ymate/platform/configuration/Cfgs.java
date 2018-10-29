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
package net.ymate.platform.configuration;

import net.ymate.platform.configuration.annotation.Configuration;
import net.ymate.platform.configuration.annotation.ConfigurationProvider;
import net.ymate.platform.configuration.handle.ConfigHandler;
import net.ymate.platform.configuration.impl.DefaultConfiguration;
import net.ymate.platform.configuration.impl.DefaultConfigurationProvider;
import net.ymate.platform.configuration.impl.DefaultModuleCfg;
import net.ymate.platform.configuration.impl.PropertyConfigurationProvider;
import net.ymate.platform.core.Version;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.module.IModule;
import net.ymate.platform.core.module.annotation.Module;
import net.ymate.platform.core.support.DefaultThreadFactory;
import net.ymate.platform.core.util.ClassUtils;
import net.ymate.platform.core.util.FileUtils;
import net.ymate.platform.core.util.ResourceUtils;
import net.ymate.platform.core.util.RuntimeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 配置体系模块管理器
 *
 * @author 刘镇 (suninformation@163.com) on 2011-8-27 上午02:17:46
 * @version 1.0
 */
@Module
public class Cfgs implements IModule, IConfig {

    public static final Version VERSION = new Version(2, 0, 6, Cfgs.class.getPackage().getImplementationVersion(), Version.VersionType.Release);

    private static final Log _LOG = LogFactory.getLog(Cfgs.class);

    private static volatile IConfig __instance;

    private YMP __owner;

    private IConfigModuleCfg __moduleCfg;

    private String __configHome;
    private String __projectHome;
    private String __moduleHome;

    private String __userHome;
    private String __userDir;

    private ConfigFileChecker __fileChecker;

    private boolean __inited;

    /**
     * @return 返回默认配置体系模块管理器实例对象
     */
    public static IConfig get() {
        if (__instance == null) {
            synchronized (VERSION) {
                if (__instance == null) {
                    __instance = YMP.get().getModule(Cfgs.class);
                }
            }
        }
        return __instance;
    }

    /**
     * @param owner YMP框架管理器实例
     * @return 返回指定YMP框架管理器容器内的配置体系模块实例
     */
    public static IConfig get(YMP owner) {
        return owner.getModule(Cfgs.class);
    }

    @Override
    public String getName() {
        return IConfig.MODULE_NAME;
    }

    @Override
    public void init(YMP owner) throws Exception {
        if (!__inited) {
            //
            _LOG.info("Initializing ymate-platform-configuration-" + VERSION);
            //
            __owner = owner;
            __moduleCfg = new DefaultModuleCfg(__owner);
            //
            __owner.registerHandler(Configuration.class, new ConfigHandler(this));
            //
            if (__moduleCfg.getConfigCheckTimeInterval() > 0) {
                __fileChecker = new ConfigFileChecker(__moduleCfg.getConfigCheckTimeInterval());
                __fileChecker.start();
            }
            //
            __configHome = __moduleCfg.getConfigHome();
            //
            if (StringUtils.isNotBlank(__configHome)) {
                __configHome = StringUtils.replace(__configHome, "%20", " ");
                File _configHomeFile = new File(__configHome);
                if (_configHomeFile.exists() && _configHomeFile.isDirectory()) {
                    System.setProperty(__USER_DIR, __configHome = _configHomeFile.getPath());
                    // 在配置体系主目录（configHome）存在的情况下，处理项目主目录
                    if (StringUtils.isNotBlank(__moduleCfg.getProjectName())) {
                        System.setProperty(__USER_DIR, __projectHome = new File(__configHome, __PROJECTS_FOLDER_NAME.concat(File.separator).concat(__moduleCfg.getProjectName())).getPath());
                        // 在项目主目录（projectHome）存在的情况下，处理模块主目录
                        if (StringUtils.isNotBlank(__moduleCfg.getModuleName())) {
                            System.setProperty(__USER_DIR, __moduleHome = new File(__projectHome, __MODULES_FOLDER_NAME.concat(File.separator).concat(__moduleCfg.getModuleName())).getPath());
                        }
                    }
                    __userHome = System.getProperty(__USER_HOME, "");
                    __userDir = System.getProperty(__USER_DIR, "");
                    //
                    __inited = true;
                    //
                    _LOG.info("-->  CONFIG_HOME: " + __configHome);
                    _LOG.info("-->    USER_HOME: " + __userHome);
                    _LOG.info("-->     USER_DIR: " + __userDir);
                    if (StringUtils.isNotBlank(__moduleCfg.getProjectName())) {
                        _LOG.info("--> PROJECT_NAME: " + __moduleCfg.getProjectName());
                    }
                    if (StringUtils.isNotBlank(__moduleCfg.getModuleName())) {
                        _LOG.info("-->  MODULE_NAME: " + __moduleCfg.getModuleName());
                    }
                }
            }
            if (!__inited) {
                throw new IllegalArgumentException("The parameter CONFIG_HOME is invalid or is not a directory path");
            }
        }
    }

    @Override
    public boolean isInited() {
        return __inited;
    }

    @Override
    public YMP getOwner() {
        return __owner;
    }

    @Override
    public void destroy() throws Exception {
        if (__inited) {
            __inited = false;
            //
            if (__fileChecker != null && __fileChecker.isInited()) {
                __fileChecker.stop();
            }
            //
            __moduleCfg = null;
            __owner = null;
        }
    }

    @Override
    public IConfigModuleCfg getModuleCfg() {
        return __moduleCfg;
    }

    @Override
    public String getConfigHome() {
        return __configHome;
    }

    @Override
    public String getProjectHome() {
        return __projectHome;
    }

    @Override
    public String getModuleHome() {
        return __moduleHome;
    }

    @Override
    public String getUserHome() {
        return __userHome;
    }

    @Override
    public String getUserDir() {
        return __userDir;
    }

    @Override
    public String searchPath(String cfgFile) {
        if (StringUtils.isNotBlank(cfgFile)) {
            if (cfgFile.startsWith("jar:")) {
                return cfgFile;
            }
            File _targetFile = __doSearch(cfgFile);
            if (_targetFile == null) {
                URL _targetFileURL = ResourceUtils.getResource(cfgFile, this.getClass());
                if (_targetFileURL != null && (_targetFile = FileUtils.toFile(_targetFileURL)) == null) {
                    return _targetFileURL.toString();
                }
            }
            if (_targetFile != null) {
                return _targetFile.getPath();
            }
        }
        return null;
    }

    @Override
    public File searchFile(String cfgFile) {
        if (StringUtils.isNotBlank(cfgFile)) {
            return __doSearch(cfgFile);
        }
        return null;
    }

    @Override
    public IConfiguration loadCfg(String cfgFileName, boolean search) {
        if (StringUtils.isNotBlank(cfgFileName)) {
            Class<? extends IConfigurationProvider> _providerClass = null;
            String _ext = FileUtils.getExtName(cfgFileName);
            if (StringUtils.equalsIgnoreCase(_ext, "xml")) {
                _providerClass = DefaultConfigurationProvider.class;
            } else if (StringUtils.equalsIgnoreCase(_ext, "properties")) {
                _providerClass = PropertyConfigurationProvider.class;
            }
            if (_providerClass != null) {
                IConfiguration _conf = new DefaultConfiguration();
                if (fillCfg(_providerClass, _conf, cfgFileName, search)) {
                    return _conf;
                }
            }
        }
        return null;
    }

    @Override
    public IConfiguration loadCfg(String cfgFileName) {
        return loadCfg(cfgFileName, true);
    }

    private File __doSearch(String cfgFile) {
        // 若指定的 cfgFile 为文件绝对路径名，则直接返回
        File _result = new File(cfgFile);
        if (_result.isFile() && _result.canRead() && _result.isAbsolute() && _result.exists()) {
            return _result;
        }
        // 到 moduleHome(模块路径)路径中去寻找 cfgFile 指定的文件
        if (StringUtils.isNotBlank(__moduleHome)) {
            _result = new File(__moduleHome, cfgFile);
            if (_result.isFile() && _result.canRead() && _result.isAbsolute() && _result.exists()) {
                return _result;
            }
        }
        // 到 projectHome(项目路径)路径中去寻找 cfgFile 指定的文件
        if (StringUtils.isNotBlank(__projectHome)) {
            _result = new File(__projectHome, cfgFile);
            if (_result.isFile() && _result.canRead() && _result.isAbsolute() && _result.exists()) {
                return _result;
            }
        }
        // 到 configHome(主路径)路径中去寻找 cfgFile 指定的文件
        if (StringUtils.isNotBlank(__configHome)) {
            _result = new File(__configHome, cfgFile);
            if (_result.isFile() && _result.canRead() && _result.isAbsolute() && _result.exists()) {
                return _result;
            }
        }
        // 到 userDir(用户路径)路径中去寻找 cfgFile 指定的文件
        if (StringUtils.isNotBlank(__userDir)) {
            _result = new File(__userDir, cfgFile);
            if (_result.isFile() && _result.canRead() && _result.isAbsolute() && _result.exists()) {
                return _result;
            }
        }
        // 到 osUserHome(系统用户路径)路径中去寻找 cfgFile 指定的文件
        if (StringUtils.isNotBlank(__userHome)) {
            _result = new File(__userHome, cfgFile);
            if (_result.isFile() && _result.canRead() && _result.isAbsolute() && _result.exists()) {
                return _result;
            }
        }
        return null;
    }

    @Override
    public boolean fillCfg(IConfiguration config, String cfgFileName) {
        return fillCfg(config, cfgFileName, true);
    }

    @Override
    public synchronized boolean fillCfg(IConfiguration config, String cfgFileName, boolean search) {
        return fillCfg(null, config, cfgFileName, search);
    }

    @Override
    public boolean fillCfg(IConfiguration config) {
        return fillCfg(config, true);
    }

    @Override
    public boolean fillCfg(IConfiguration config, boolean search) {
        if (config != null) {
            Configuration _configuration = config.getClass().getAnnotation(Configuration.class);
            ConfigurationProvider _providerClass = config.getClass().getAnnotation(ConfigurationProvider.class);
            String _cfgFileName = _configuration == null ? null : _configuration.value();
            boolean _reload = _configuration == null || _configuration.relaod();
            if (StringUtils.isBlank(_cfgFileName)) {
                _cfgFileName = config.getClass().getSimpleName().toLowerCase().concat(config.getTagName()).concat(".xml");
            }
            return fillCfg((_providerClass != null ? _providerClass.value() : null), config, _cfgFileName, search, _reload);
        }
        return false;
    }

    @Override
    public synchronized boolean fillCfg(Class<? extends IConfigurationProvider> providerClass, IConfiguration config, String cfgFileName, boolean search) {
        return fillCfg(providerClass, config, cfgFileName, search, true);
    }

    @Override
    public boolean fillCfg(Class<? extends IConfigurationProvider> providerClass, IConfiguration config, String cfgFileName, boolean search, boolean reload) {
        if (__inited) {
            if (config != null) {
                String _targetCfgFile = search ? searchPath(cfgFileName) : cfgFileName;
                if (StringUtils.isNotBlank(_targetCfgFile)) {
                    try {
                        IConfigurationProvider _provider = null;
                        if (providerClass != null) {
                            _provider = ClassUtils.impl(providerClass, IConfigurationProvider.class);
                        }
                        if (_provider == null) {
                            _provider = __moduleCfg.getProviderClass().newInstance();
                        }
                        _provider.load(_targetCfgFile);
                        config.initialize(_provider);
                        //
                        if (__fileChecker != null && reload) {
                            __fileChecker.putFileStatus(_targetCfgFile, new ConfigFileStatus(config, new File(_targetCfgFile).lastModified()));
                        }
                        //
                        return true;
                    } catch (Exception e) {
                        _LOG.warn("An exception occurred while filling the configuration file [" + StringUtils.trimToEmpty(cfgFileName) + "]", RuntimeUtils.unwrapThrow(e));
                    }
                } else {
                    _LOG.warn("The configuration file [" + StringUtils.trimToEmpty(cfgFileName) + "] was not found");
                }
            }
        } else {
            _LOG.warn("Module configuration has not been initialized, unable to complete the configuration object filling operation");
        }
        return false;
    }

    /**
     * 配置文件状态变化检查器
     */
    class ConfigFileChecker implements Runnable {

        private final Map<String, ConfigFileStatus> __configFileStatus = new ConcurrentHashMap<String, ConfigFileStatus>();

        private final ReentrantLock __locker = new ReentrantLock();

        private ScheduledExecutorService __scheduledExecutorService;

        private long __timeInterval;

        private boolean __inited;

        ConfigFileChecker(long timeInterval) {
            __timeInterval = timeInterval;
            if (__timeInterval > 0) {
                __scheduledExecutorService = DefaultThreadFactory.newScheduledThreadPool(1, DefaultThreadFactory.create("ConfigFileChangedChecker"));
                __inited = true;
            }
        }

        boolean isInited() {
            return __inited;
        }

        void putFileStatus(String fileName, ConfigFileStatus fileStatus) {
            if (StringUtils.isNotBlank(fileName) && fileStatus != null) {
                __locker.lock();
                try {
                    __configFileStatus.put(fileName, fileStatus);
                } finally {
                    __locker.unlock();
                }
            }
        }

        void start() {
            if (__inited && __scheduledExecutorService != null) {
                __scheduledExecutorService.scheduleWithFixedDelay(this, __timeInterval, __timeInterval, TimeUnit.MILLISECONDS);
            }
        }

        void stop() {
            if (__inited && __scheduledExecutorService != null) {
                __scheduledExecutorService.shutdown();
                __scheduledExecutorService = null;
            }
        }

        @Override
        public void run() {
            __locker.lock();
            try {
                for (Map.Entry<String, ConfigFileStatus> _entry : __configFileStatus.entrySet()) {
                    File _file = new File(_entry.getKey());
                    if (_file.lastModified() != _entry.getValue().getLastModifyTime()) {
                        try {
                            _entry.getValue().getConfiguration().reload();
                            _entry.getValue().setLastModifyTime(_file.lastModified());
                        } catch (Exception e) {
                            _LOG.warn("", RuntimeUtils.unwrapThrow(e));
                        }
                    }
                }
            } finally {
                __locker.unlock();
            }
        }
    }

    /**
     * 配置文件状态
     */
    class ConfigFileStatus {

        private IConfiguration configuration;

        private long lastModifyTime;

        ConfigFileStatus(IConfiguration configuration, long lastModifyTime) {
            this.configuration = configuration;
            this.lastModifyTime = lastModifyTime;
        }

        public IConfiguration getConfiguration() {
            return configuration;
        }

        public long getLastModifyTime() {
            return lastModifyTime;
        }

        public void setLastModifyTime(long lastModifyTime) {
            this.lastModifyTime = lastModifyTime;
        }
    }
}
