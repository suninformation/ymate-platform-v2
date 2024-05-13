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

import net.ymate.platform.log.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.*;
import org.apache.logging.log4j.core.config.xml.XmlConfigurationFactory;

import java.io.InputStream;
import java.nio.file.Files;

/**
 * 默认日志记录器（基于Log4J2实现）
 *
 * @author 刘镇 (suninformation@163.com) on 2012-12-21 上午10:51:15
 */
public class DefaultLogger extends AbstractLogger {

    private static volatile boolean loggerInitialized;

    private Logger logger;

    private String loggerName;

    private ILogConfig config;

    private boolean initialized;

    public DefaultLogger() {
    }

    @Override
    protected void logWrite(LogLevel level, LogInfo content) {
        if (logger != null) {
            logger.log(LogLevel.parse(level), config != null ? content.toString(config.getLogFormat(), config.isFormatPaddedOutput()) : content.toString());
        }
    }

    @Override
    protected void buildEx(String info, Throwable e, LogLevel level) {
        if (level == null) {
            level = LogLevel.ALL;
        }
        if (!isLogEnabled(level)) {
            return;
        }
        super.buildEx(info, e, level);
    }

    private boolean isLogEnabled(LogLevel logLevel) {
        return logger.getLevel().intLevel() >= logLevel.getLevel();
    }

    @Override
    public boolean isDebugEnabled() {
        return isLogEnabled(LogLevel.DEBUG);
    }

    @Override
    public boolean isErrorEnabled() {
        return isLogEnabled(LogLevel.ERROR);
    }

    @Override
    public boolean isFatalEnabled() {
        return isLogEnabled(LogLevel.FATAL);
    }

    @Override
    public boolean isInfoEnabled() {
        return isLogEnabled(LogLevel.INFO);
    }

    @Override
    public boolean isTraceEnabled() {
        return isLogEnabled(LogLevel.TRACE);
    }

    @Override
    public boolean isWarnEnabled() {
        return isLogEnabled(LogLevel.WARN);
    }

    @Override
    public ILogger initialize(String loggerName, ILogConfig config) throws Exception {
        if (!initialized) {
            this.loggerName = loggerName;
            this.config = config;
            //
            synchronized (DefaultLogger.class) {
                if (!loggerInitialized) {
                    try (InputStream inputStream = Files.newInputStream(config.getConfigFile().toPath())) {
                        ConfigurationSource source = new ConfigurationSource(inputStream);
                        LoggerContext loggerContext = Configurator.initialize(null, source);
                        ConfigurationFactory.setConfigurationFactory(new XmlConfigurationFactory() {

                            private final Configuration config = new DefaultConfiguration();

                            @Override
                            public Configuration getConfiguration(final LoggerContext loggerContext, final ConfigurationSource source) {
                                return config;
                            }
                        });
                        ConfigurationFactory.getInstance().getConfiguration(loggerContext, source);
                        //
                        loggerInitialized = true;
                    }
                }
            }
            logger = LogManager.getLogger(StringUtils.defaultIfBlank(loggerName, config.getDefaultLoggerName()));
            initialized = true;
        }
        return this;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public ILogger getLogger(String loggerName, ILogConfig config) throws Exception {
        return new DefaultLogger().initialize(loggerName, config);
    }

    @Override
    public String getLoggerName() {
        return loggerName;
    }

    @Override
    public void destroy() {
        logger = null;
    }

    @Override
    public boolean contains(String loggerName) {
        return LogManager.exists(loggerName);
    }

    @Override
    public LogLevel getLevel() {
        return LogLevel.parse(logger.getLevel().intLevel());
    }

    @Override
    public void log(String info, LogLevel level) {
        buildEx(info, null, level);
    }

    @Override
    public void log(Throwable e, LogLevel level) {
        buildEx(null, e, level);
    }

    @Override
    public void log(String info, Throwable e, LogLevel level) {
        buildEx(info, e, level);
    }

    @Override
    public void trace(String info) {
        buildEx(info, null, LogLevel.INFO);
    }

    @Override
    public void trace(Throwable e) {
        buildEx(null, e, LogLevel.TRACE);
    }

    @Override
    public void trace(String info, Throwable e) {
        buildEx(info, e, LogLevel.TRACE);
    }

    @Override
    public void debug(String info) {
        buildEx(info, null, LogLevel.DEBUG);
    }

    @Override
    public void debug(Throwable e) {
        buildEx(null, e, LogLevel.DEBUG);
    }

    @Override
    public void debug(String info, Throwable e) {
        buildEx(info, e, LogLevel.DEBUG);
    }

    @Override
    public void info(String info) {
        buildEx(info, null, LogLevel.INFO);
    }

    @Override
    public void info(Throwable e) {
        buildEx(null, e, LogLevel.INFO);
    }

    @Override
    public void info(String info, Throwable e) {
        buildEx(info, e, LogLevel.INFO);
    }

    @Override
    public void warn(String info) {
        buildEx(info, null, LogLevel.WARN);
    }

    @Override
    public void warn(Throwable e) {
        buildEx(null, e, LogLevel.WARN);
    }

    @Override
    public void warn(String info, Throwable e) {
        buildEx(info, e, LogLevel.WARN);
    }

    @Override
    public void error(String info) {
        buildEx(info, null, LogLevel.ERROR);
    }

    @Override
    public void error(Throwable e) {
        buildEx(null, e, LogLevel.ERROR);
    }

    @Override
    public void error(String info, Throwable e) {
        buildEx(info, e, LogLevel.ERROR);
    }

    @Override
    public void fatal(String info) {
        buildEx(info, null, LogLevel.FATAL);
    }

    @Override
    public void fatal(Throwable e) {
        buildEx(null, e, LogLevel.FATAL);
    }

    @Override
    public void fatal(String info, Throwable e) {
        buildEx(info, e, LogLevel.FATAL);
    }
}
