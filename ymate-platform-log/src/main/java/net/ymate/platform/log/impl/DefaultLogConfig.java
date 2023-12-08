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
import net.ymate.platform.commons.util.FileUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.configuration.IConfigReader;
import net.ymate.platform.core.module.IModuleConfigurer;
import net.ymate.platform.log.ILog;
import net.ymate.platform.log.ILogConfig;
import net.ymate.platform.log.ILogger;
import net.ymate.platform.log.annotation.LogConf;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

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

    public static DefaultLogConfig defaultConfig() {
        return builder().build();
    }

    public static DefaultLogConfig create(IModuleConfigurer moduleConfigurer) {
        return new DefaultLogConfig(null, moduleConfigurer);
    }

    public static DefaultLogConfig create(Class<?> mainClass, IModuleConfigurer moduleConfigurer) {
        return new DefaultLogConfig(mainClass, moduleConfigurer);
    }

    public static Builder builder() {
        return new Builder();
    }

    private DefaultLogConfig() {
    }

    @SuppressWarnings("unchecked")
    private DefaultLogConfig(Class<?> mainClass, IModuleConfigurer moduleConfigurer) {
        IConfigReader configReader = moduleConfigurer.getConfigReader();
        //
        LogConf confAnn = mainClass == null ? null : mainClass.getAnnotation(LogConf.class);
        //
        configFile = new File(RuntimeUtils.replaceEnvVariable(configReader.getString(CONFIG_FILE, StringUtils.defaultIfBlank(confAnn == null ? null : confAnn.configFile(), DEFAULT_CONFIG_FILE))));
        outputDir = new File(RuntimeUtils.replaceEnvVariable(configReader.getString(OUTPUT_DIR, StringUtils.defaultIfBlank(confAnn == null ? null : confAnn.outputDir(), DEFAULT_OUTPUT_DIR))));
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
        if (loggerClass == null && confAnn != null && !confAnn.loggerClass().equals(ILogger.class)) {
            loggerClass = confAnn.loggerClass();
        }
        //
        defaultLoggerName = configReader.getString(LOGGER_NAME, confAnn == null ? null : confAnn.defaultLoggerName());
        logFormat = configReader.getString(LOG_FORMAT, confAnn == null ? null : confAnn.logFormat());
        printStackCount = configReader.getInt(PRINT_STACK_COUNT, confAnn == null ? 0 : confAnn.printStackCount());
        allowConsoleOutput = configReader.getBoolean(ALLOW_OUTPUT_CONSOLE, confAnn != null && confAnn.allowConsoleOutput());
        simplifiedPackageName = configReader.getBoolean(SIMPLIFIED_PACKAGE_NAME, confAnn != null && confAnn.simplifiedPackageName());
        formatPaddedOutput = configReader.getBoolean(FORMAT_PADDED_OUTPUT, confAnn != null && confAnn.formatPaddedOutput());
    }

    @Override
    public void initialize(ILog owner) throws Exception {
        if (!initialized) {
            if (!configFile.isAbsolute()) {
                throw new IllegalArgumentException(String.format("Parameter config_file value [%s] is not an absolute file path.", configFile.getPath()));
            } else if (!configFile.exists()) {
                File newConfigFile = new File(RuntimeUtils.replaceEnvVariable(DEFAULT_CONFIG_FILE));
                if (!newConfigFile.exists()) {
                    try (InputStream inputStream = DefaultLogConfig.class.getClassLoader().getResourceAsStream("META-INF/default-log4j.xml")) {
                        if (!FileUtils.createFileIfNotExists(configFile, inputStream) && LOG.isWarnEnabled()) {
                            LOG.warn(String.format("Failed to create default log4j file: %s", configFile.getPath()));
                        }
                    } catch (IOException e) {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn(String.format("An exception occurred while trying to generate the default log4j file: %s", configFile.getPath()), RuntimeUtils.unwrapThrow(e));
                        }
                    }
                } else {
                    configFile = newConfigFile;
                }
            }
            //
            if (!outputDir.isAbsolute()) {
                throw new IllegalArgumentException(String.format("Parameter output_dir value [%s] is not an absolute directory path.", outputDir.getPath()));
            } else if (!outputDir.exists()) {
                if (outputDir.mkdirs()) {
                    if (LOG.isInfoEnabled()) {
                        LOG.info(String.format("Successfully created output_dir directory: %s", outputDir.getPath()));
                    }
                } else {
                    throw new IllegalArgumentException(String.format("Failed to create output_dir directory: %s", outputDir.getPath()));
                }
            }
            if (StringUtils.isBlank(defaultLoggerName)) {
                defaultLoggerName = DEFAULT_STR;
            }
            //
            if (this.loggerClass == null) {
                this.loggerClass = ClassUtils.getExtensionLoader(ILogger.class).getExtensionClass();
                if (this.loggerClass == null) {
                    this.loggerClass = DefaultLogger.class;
                }
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

        public DefaultLogConfig build() {
            return config;
        }
    }
}
