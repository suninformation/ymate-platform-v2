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
package net.ymate.platform.log.impl;

import net.ymate.platform.core.IConfig;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.support.IConfigReader;
import net.ymate.platform.core.support.impl.MapSafeConfigReader;
import net.ymate.platform.core.util.ClassUtils;
import net.ymate.platform.core.util.RuntimeUtils;
import net.ymate.platform.log.ILog;
import net.ymate.platform.log.ILogModuleCfg;
import net.ymate.platform.log.ILogger;

import java.io.File;

/**
 * 默认日志记录器模块配置类
 *
 * @author 刘镇 (suninformation@163.com) on 2012-12-23 下午6:26:42
 * @version 1.0
 */
public class DefaultLogModuleCfg implements ILogModuleCfg {

    private File configFile;
    private File outputDir;
    private String loggerName;
    private Class<? extends ILogger> loggerClass;
    private boolean allowOutputConsole;
    private boolean simplifiedPackageName;
    private boolean formatPaddedOutput;

    @SuppressWarnings("unchecked")
    public DefaultLogModuleCfg(YMP owner) {
        IConfigReader _moduleCfg = MapSafeConfigReader.bind(owner.getConfig().getModuleConfigs(ILog.MODULE_NAME));
        //
        this.configFile = new File(RuntimeUtils.replaceEnvVariable(_moduleCfg.getString(CONFIG_FILE, "${root}/cfgs/log4j.xml")));
        if (!this.configFile.isAbsolute() || !this.configFile.exists() || this.configFile.isDirectory() || !this.configFile.canRead()) {
            throw new IllegalArgumentException("The parameter configFile is invalid or is not a file");
        }
        //
        this.outputDir = new File(RuntimeUtils.replaceEnvVariable(_moduleCfg.getString(OUTPUT_DIR, "${root}/logs/")));
        if (!this.outputDir.isAbsolute() || !this.outputDir.exists() || !this.outputDir.isDirectory() || !this.outputDir.canRead()) {
            throw new IllegalArgumentException("The parameter outputDir is invalid or is not a directory");
        }
        //
        this.loggerName = _moduleCfg.getString(LOGGER_NAME, IConfig.DEFAULT_STR);
        //
        try {
            this.loggerClass = (Class<? extends ILogger>) ClassUtils.loadClass(_moduleCfg.getString(LOGGER_CLASS, DefaultLogger.class.getName()), this.getClass());
            if (this.loggerClass == null) {
                this.loggerClass = DefaultLogger.class;
            }
        } catch (Exception e) {
            this.loggerClass = DefaultLogger.class;
        }
        //
        this.allowOutputConsole = _moduleCfg.getBoolean(ALLOW_OUTPUT_CONSOLE);
        this.simplifiedPackageName = _moduleCfg.getBoolean(SIMPLIFIED_PACKAGE_NAME);
        this.formatPaddedOutput = _moduleCfg.getBoolean(FORMAT_PADDED_OUTPUT);
    }

    @Override
    public File getConfigFile() {
        return this.configFile;
    }

    @Override
    public File getOutputDir() {
        return this.outputDir;
    }

    @Override
    public String getLoggerName() {
        return this.loggerName;
    }

    @Override
    public Class<? extends ILogger> getLoggerClass() {
        return this.loggerClass;
    }

    @Override
    public boolean allowOutputConsole() {
        return this.allowOutputConsole;
    }

    @Override
    public boolean simplifiedPackageName() {
        return simplifiedPackageName;
    }

    @Override
    public boolean formatPaddedOutput() {
        return formatPaddedOutput;
    }
}
