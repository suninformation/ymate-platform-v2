/*
 * Copyright 2007-2107 the original author or authors.
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
import net.ymate.platform.configuration.impl.DefaultModuleCfg;
import net.ymate.platform.core.Version;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.module.IModule;
import net.ymate.platform.core.module.annotation.Module;
import net.ymate.platform.core.util.ClassUtils;
import net.ymate.platform.core.util.FileUtils;
import net.ymate.platform.core.util.ResourceUtils;
import net.ymate.platform.core.util.RuntimeUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.net.URL;

/**
 * 配置体系模块管理器
 *
 * @author 刘镇 (suninformation@163.com) on 15-3-9 上午12:15
 * @version 1.0
 */
@Module
public class Cfgs implements IModule, IConfig {

    public static final Version VERSION = new Version(2, 0, 0, Cfgs.class.getPackage().getImplementationVersion(), Version.VersionType.Alphal);

    private static IConfig __instance;

    private YMP __owner;

    private IConfigModuleCfg __moduleCfg;

    private String __configHome;
    private String __projectHome;
    private String __moduleHome;

    private String __userHome;
    private String __userDir;

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

    public void init(YMP owner) throws Exception {
        if (!__inited) {
            __owner = owner;
            __moduleCfg = new DefaultModuleCfg(__owner);
            //
            __owner.registerHandler(Configuration.class, new ConfigHandler(__owner));
            //
            __configHome = __moduleCfg.getConfigHome();
            if (StringUtils.isBlank(__configHome)) {
                // 尝试通过运行时变量或系统变量获取CONFIG_HOME参数
                __configHome = StringUtils.defaultIfBlank(System.getenv(__CONFIG_HOME), RuntimeUtils.getSystemEnv(__CONFIG_HOME));
            }
            //
            if (StringUtils.isNotBlank(__configHome)) {
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
                }
            }
            if (!__inited) {
                throw new IllegalArgumentException("The parameter CONFIG_HOME is invalid or is not a directory path");
            }
        }
    }

    public boolean isInited() {
        return __inited;
    }

    public void destroy() throws Exception {
        if (__inited) {
            __inited = false;
            //
            __moduleCfg = null;
            __owner = null;
        }
    }

    public IConfigModuleCfg getModuleCfg() {
        return __moduleCfg;
    }

    public String getConfigHome() {
        return __configHome;
    }

    public String getProjectHome() {
        return __projectHome;
    }

    public String getModuleHome() {
        return __moduleHome;
    }

    public String getUserHome() {
        return __userHome;
    }

    public String getUserDir() {
        return __userDir;
    }

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

    public File searchFile(String cfgFile) {
        if (StringUtils.isNotBlank(cfgFile)) {
            return __doSearch(cfgFile);
        }
        return null;
    }

    private File __doSearch(String cfgFile) {
        // 若指定的 cfgFile 为文件绝对路径名，则直接返回
        File _result = new File(cfgFile);
        if (_result.isAbsolute()) {
            return _result;
        }
        // 到 moduleHome(模块路径)路径中去寻找 cfgFile 指定的文件
        if (StringUtils.isNotBlank(__moduleHome)) {
            _result = new File(__moduleHome, cfgFile);
            if (_result.canRead() && _result.isAbsolute() && _result.exists()) {
                return _result;
            }
        }
        // 到 projectHome(项目路径)路径中去寻找 cfgFile 指定的文件
        if (StringUtils.isNotBlank(__projectHome)) {
            _result = new File(__projectHome, cfgFile);
            if (_result.canRead() && _result.isAbsolute() && _result.exists()) {
                return _result;
            }
        }
        // 到 configHome(主路径)路径中去寻找 cfgFile 指定的文件
        if (StringUtils.isNotBlank(__configHome)) {
            _result = new File(__configHome, cfgFile);
            if (_result.canRead() && _result.isAbsolute() && _result.exists()) {
                return _result;
            }
        }
        // 到 userDir(用户路径)路径中去寻找 cfgFile 指定的文件
        if (StringUtils.isNotBlank(__userDir)) {
            _result = new File(__userDir, cfgFile);
            if (_result.canRead() && _result.isAbsolute() && _result.exists()) {
                return _result;
            }
        }
        // 到 osUserHome(系统用户路径)路径中去寻找 cfgFile 指定的文件
        if (StringUtils.isNotBlank(__userHome)) {
            _result = new File(__userHome, cfgFile);
            if (_result.canRead() && _result.isAbsolute() && _result.exists()) {
                return _result;
            }
        }
        return null;
    }

    public boolean fillCfg(IConfiguration config, String cfgFileName) {
        return fillCfg(config, cfgFileName, true);
    }

    public synchronized boolean fillCfg(IConfiguration config, String cfgFileName, boolean search) {
        return fillCfg(null, config, cfgFileName, search);
    }

    public boolean fillCfg(IConfiguration config) {
        return fillCfg(config, true);
    }

    public boolean fillCfg(IConfiguration config, boolean search) {
        if (config != null) {
            Configuration _configuration = config.getClass().getAnnotation(Configuration.class);
            ConfigurationProvider _providerClass = config.getClass().getAnnotation(ConfigurationProvider.class);
            String _cfgFileName = _configuration == null ? null : _configuration.value();
            if (StringUtils.isBlank(_cfgFileName)) {
                _cfgFileName = config.getClass().getSimpleName().toLowerCase().concat(config.getTagName()).concat(".xml");
            }
            return fillCfg((_providerClass != null ? _providerClass.value() : null), config, _cfgFileName, search);
        }
        return false;
    }

    public synchronized boolean fillCfg(Class<? extends IConfigurationProvider> providerClass, IConfiguration config, String cfgFileName, boolean search) {
        if (__inited) {
            if (config != null) {
                try {
                    IConfigurationProvider _provider = null;
                    if (providerClass != null) {
                        _provider = ClassUtils.impl(providerClass, IConfigurationProvider.class);
                    }
                    if (_provider == null) {
                        _provider = __moduleCfg.getProviderClass().newInstance();
                    }
                    if (search) {
                        _provider.load(searchPath(cfgFileName));
                    } else {
                        _provider.load(cfgFileName);
                    }
                    config.initialize(_provider);
                    return true;
                } catch (Exception e) {
                    System.err.println("Warnring: " + e.getMessage() + " [" + StringUtils.trimToEmpty(cfgFileName) + "]");
                }
            }
        } else {
            System.err.println("Module configuration has not been initialized, unable to complete the configuration object filling operation");
        }
        return false;
    }
}
