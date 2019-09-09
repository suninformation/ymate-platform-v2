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
package net.ymate.platform.log.impl;

import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.configuration.IConfigReader;
import net.ymate.platform.core.module.IModuleConfigurer;
import net.ymate.platform.log.ILog;
import net.ymate.platform.log.ILogConfig;
import net.ymate.platform.log.ILogger;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;

/**
 * 默认日志管理器配置类
 *
 * @author 刘镇 (suninformation@163.com) on 2012-12-23 下午6:26:42
 */
public final class DefaultLogConfig implements ILogConfig {

    private static final Log LOG = LogFactory.getLog(DefaultLogConfig.class);

    private File configFile;

    private File outputDir;

    private String defaultLoggerName;

    private int printStackCount;

    private String logFormat;

    private Class<? extends ILogger> loggerClass;

    private boolean allowConsoleOutput;

    private boolean simplifiedPackageName;

    private boolean formatPaddedOutput;

    private boolean initialized;

    public static ILogConfig defaultConfig() {
        return builder().build();
    }

    public static ILogConfig create(IModuleConfigurer moduleConfigurer) {
        return new DefaultLogConfig(moduleConfigurer);
    }

    public static Builder builder() {
        return new Builder();
    }

    private DefaultLogConfig() {
    }

    @SuppressWarnings("unchecked")
    private DefaultLogConfig(IModuleConfigurer moduleConfigurer) {
        IConfigReader configReader = moduleConfigurer.getConfigReader();
        //
        configFile = new File(RuntimeUtils.replaceEnvVariable(configReader.getString(CONFIG_FILE, DEFAULT_CONFIG_FILE)));
        outputDir = new File(RuntimeUtils.replaceEnvVariable(configReader.getString(OUTPUT_DIR, DEFAULT_OUTPUT_DIR)));
        //
        String loggerClassName = configReader.getString(LOGGER_CLASS);
        if (StringUtils.isNotBlank(loggerClassName)) {
            try {
                loggerClass = (Class<? extends ILogger>) ClassUtils.loadClass(loggerClassName, this.getClass());
            } catch (ClassNotFoundException e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
                }
            }
        }
        //
        defaultLoggerName = configReader.getString(LOGGER_NAME);
        logFormat = configReader.getString(LOG_FORMAT);
        printStackCount = configReader.getInt(PRINT_STACK_COUNT);
        allowConsoleOutput = configReader.getBoolean(ALLOW_OUTPUT_CONSOLE);
        simplifiedPackageName = configReader.getBoolean(SIMPLIFIED_PACKAGE_NAME);
        formatPaddedOutput = configReader.getBoolean(FORMAT_PADDED_OUTPUT);
    }

    @Override
    public void initialize(ILog owner) throws Exception {
        if (!initialized) {
            if (configFile == null || !configFile.isAbsolute() || !configFile.canRead() || !configFile.exists() || configFile.isDirectory()) {
                throw new IllegalArgumentException("ConfigFile is not a valid file.");
            }
            //
            if (outputDir == null || !outputDir.isAbsolute() || !outputDir.canRead() || !outputDir.canWrite() || !outputDir.exists() || !outputDir.isDirectory()) {
                throw new IllegalArgumentException("OutputDir is not a valid directory.");
            }
            if (StringUtils.isBlank(defaultLoggerName)) {
                defaultLoggerName = DEFAULT_STR;
            }
            //
            if (this.loggerClass == null) {
                this.loggerClass = DefaultLogger.class;
            }
            //
            initialized = true;
        }
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public File getConfigFile() {
        return configFile;
    }

    public void setConfigFile(File configFile) {
        if (!initialized) {
            this.configFile = configFile;
        }
    }

    @Override
    public File getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(File outputDir) {
        if (!initialized) {
            this.outputDir = outputDir;
        }
    }

    @Override
    public String getDefaultLoggerName() {
        return defaultLoggerName;
    }

    public void setDefaultLoggerName(String defaultLoggerName) {
        if (!initialized) {
            this.defaultLoggerName = defaultLoggerName;
        }
    }

    @Override
    public int getPrintStackCount() {
        return printStackCount;
    }

    public void setPrintStackCount(int printStackCount) {
        if (!initialized) {
            this.printStackCount = printStackCount;
        }
    }

    @Override
    public String getLogFormat() {
        return logFormat;
    }

    public void setLogFormat(String logFormat) {
        if (!initialized) {
            this.logFormat = logFormat;
        }
    }

    @Override
    public Class<? extends ILogger> getLoggerClass() {
        return loggerClass;
    }

    public void setLoggerClass(Class<? extends ILogger> loggerClass) {
        if (!initialized) {
            this.loggerClass = loggerClass;
        }
    }

    @Override
    public boolean isAllowConsoleOutput() {
        return allowConsoleOutput;
    }

    public void setAllowConsoleOutput(boolean allowConsoleOutput) {
        if (!initialized) {
            this.allowConsoleOutput = allowConsoleOutput;
        }
    }

    @Override
    public boolean isSimplifiedPackageName() {
        return simplifiedPackageName;
    }

    public void setSimplifiedPackageName(boolean simplifiedPackageName) {
        if (!initialized) {
            this.simplifiedPackageName = simplifiedPackageName;
        }
    }

    @Override
    public boolean isFormatPaddedOutput() {
        return formatPaddedOutput;
    }

    public void setFormatPaddedOutput(boolean formatPaddedOutput) {
        if (!initialized) {
            this.formatPaddedOutput = formatPaddedOutput;
        }
    }

    public final static class Builder {

        private final DefaultLogConfig config = new DefaultLogConfig();

        private Builder() {
            config.setConfigFile(new File(RuntimeUtils.replaceEnvVariable(DEFAULT_CONFIG_FILE)));
            config.setOutputDir(new File(RuntimeUtils.replaceEnvVariable(DEFAULT_OUTPUT_DIR)));
        }

        public Builder configFile(File configFile) {
            config.setConfigFile(configFile);
            return this;
        }

        public Builder outputDir(File outputDir) {
            config.setOutputDir(outputDir);
            return this;
        }

        public Builder defaultLoggerName(String defaultLoggerName) {
            config.setDefaultLoggerName(defaultLoggerName);
            return this;
        }

        public Builder printStackCount(int printStackCount) {
            config.setPrintStackCount(printStackCount);
            return this;
        }

        public Builder logFormat(String logFormat) {
            config.setLogFormat(logFormat);
            return this;
        }

        public Builder loggerClass(Class<? extends ILogger> loggerClass) {
            config.setLoggerClass(loggerClass);
            return this;
        }

        public Builder allowConsoleOutput(boolean allowConsoleOutput) {
            config.setAllowConsoleOutput(allowConsoleOutput);
            return this;
        }

        public Builder simplifiedPackageName(boolean simplifiedPackageName) {
            config.setSimplifiedPackageName(simplifiedPackageName);
            return this;
        }

        public Builder formatPaddedOutput(boolean formatPaddedOutput) {
            config.setFormatPaddedOutput(formatPaddedOutput);
            return this;
        }

        public ILogConfig build() {
            return config;
        }
    }
}