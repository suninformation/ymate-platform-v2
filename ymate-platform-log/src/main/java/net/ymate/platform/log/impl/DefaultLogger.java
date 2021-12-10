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

import net.ymate.platform.log.*;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.*;
import org.apache.logging.log4j.core.config.xml.XmlConfigurationFactory;

import java.io.FileInputStream;

/**
 * 默认日志记录器（基于Log4J2实现）
 *
 * @author 刘镇 (suninformation@163.com) on 2012-12-21 上午10:51:15
 * @version 1.0
 */
public class DefaultLogger extends AbstractLogger {

    private static boolean __logInited;

    private ILog __owner;

    private Logger __logger;

    private String __loggerName;

    private boolean __inited;

    public static LogLevel __parseLevel(int level) {
        switch (level) {
            case 600:
                return LogLevel.TRACE;
            case 500:
                return LogLevel.DEBUG;
            case 400:
                return LogLevel.INFO;
            case 300:
                return LogLevel.WARN;
            case 200:
                return LogLevel.ERROR;
            case 100:
                return LogLevel.FATAL;
            case 0:
                return LogLevel.OFF;
            default:
                return LogLevel.ALL;
        }
    }

    public static Level __parseLogLevel(LogLevel level) {
        switch (level.getLevel()) {
            case 600:
                return Level.TRACE;
            case 500:
                return Level.DEBUG;
            case 400:
                return Level.INFO;
            case 300:
                return Level.WARN;
            case 200:
                return Level.ERROR;
            case 100:
                return Level.FATAL;
            case 0:
                return Level.OFF;
            default:
                return Level.ALL;
        }
    }

    @Override
    protected void __doLogWrite(LogLevel level, LogInfo content) {
        __logger.log(__parseLogLevel(level), content.toString());
        // 日志写入后触发异步事件
        __owner.getOwner().getEvents().fireEvent(new LogEvent(this, LogEvent.EVENT.LOG_WRITE_IN)
                .addParamExtend(LogEvent.LOG_LEVEL, level)
                .addParamExtend(LogEvent.LOG_INFO, content));
    }

    @Override
    protected void __doBuildEx(String info, Throwable e, LogLevel level) {
        if (level == null) {
            level = ILogger.LogLevel.ALL;
        }
        if (!__doIsLogEnabled(level)) {
            return;
        }
        super.__doBuildEx(info, e, level);
    }

    private boolean __doIsLogEnabled(LogLevel logLevel) {
        return __logger.getLevel().intLevel() >= logLevel.getLevel();
    }

    @Override
    public boolean isDebugEnabled() {
        return __doIsLogEnabled(LogLevel.DEBUG);
    }

    @Override
    public boolean isErrorEnabled() {
        return __doIsLogEnabled(LogLevel.ERROR);
    }

    @Override
    public boolean isFatalEnabled() {
        return __doIsLogEnabled(LogLevel.FATAL);
    }

    @Override
    public boolean isInfoEnabled() {
        return __doIsLogEnabled(LogLevel.INFO);
    }

    @Override
    public boolean isTraceEnabled() {
        return __doIsLogEnabled(LogLevel.TRACE);
    }

    @Override
    public boolean isWarnEnabled() {
        return __doIsLogEnabled(LogLevel.WARN);
    }

    @Override
    public synchronized ILogger init(ILog owner, String loggerName) throws Exception {
        if (!__inited) {
            __owner = owner;
            __loggerName = loggerName;
            //
            if (!__logInited) {
                ConfigurationSource _source = new ConfigurationSource(new FileInputStream(__owner.getModuleCfg().getConfigFile()));
                LoggerContext loggerContext = Configurator.initialize(null, _source);
                ConfigurationFactory.setConfigurationFactory(new XmlConfigurationFactory() {

                    private final Configuration _config = new DefaultConfiguration();

                    @Override
                    public Configuration getConfiguration(final LoggerContext loggerContext, final ConfigurationSource source) {
                        return _config;
                    }
                });
                ConfigurationFactory.getInstance().getConfiguration(loggerContext, _source);
                //
                __logInited = true;
            }
            __logger = LogManager.getLogger(StringUtils.defaultIfBlank(loggerName, __owner.getModuleCfg().getLoggerName()));
            __inited = true;
        }
        return this;
    }

    @Override
    public ILogger getLogger(String loggerName) throws Exception {
        return new DefaultLogger().init(__owner, loggerName);
    }

    @Override
    public Object getLoggerImpl() {
        return __logger;
    }

    @Override
    public String getLoggerName() {
        return __loggerName;
    }

    @Override
    public void destroy() {
        __logger = null;
    }

    @Override
    public boolean contains(String loggerName) {
        return LogManager.exists(loggerName);
    }

    @Override
    public LogLevel getLevel() {
        return __parseLevel(__logger.getLevel().intLevel());
    }

    @Override
    public void log(String info, LogLevel level) {
        __doBuildEx(info, null, level);
    }

    @Override
    public void log(Throwable e, LogLevel level) {
        __doBuildEx(null, e, level);
    }

    @Override
    public void log(String info, Throwable e, LogLevel level) {
        __doBuildEx(info, e, level);
    }

    @Override
    public void trace(String info) {
        __doBuildEx(info, null, LogLevel.INFO);
    }

    @Override
    public void trace(Throwable e) {
        __doBuildEx(null, e, LogLevel.TRACE);
    }

    @Override
    public void trace(String info, Throwable e) {
        __doBuildEx(info, e, LogLevel.TRACE);
    }

    @Override
    public void debug(String info) {
        __doBuildEx(info, null, LogLevel.DEBUG);
    }

    @Override
    public void debug(Throwable e) {
        __doBuildEx(null, e, LogLevel.DEBUG);
    }

    @Override
    public void debug(String info, Throwable e) {
        __doBuildEx(info, e, LogLevel.DEBUG);
    }

    @Override
    public void info(String info) {
        __doBuildEx(info, null, LogLevel.INFO);
    }

    @Override
    public void info(Throwable e) {
        __doBuildEx(null, e, LogLevel.INFO);
    }

    @Override
    public void info(String info, Throwable e) {
        __doBuildEx(info, e, LogLevel.INFO);
    }

    @Override
    public void warn(String info) {
        __doBuildEx(info, null, LogLevel.WARN);
    }

    @Override
    public void warn(Throwable e) {
        __doBuildEx(null, e, LogLevel.WARN);
    }

    @Override
    public void warn(String info, Throwable e) {
        __doBuildEx(info, e, LogLevel.WARN);
    }

    @Override
    public void error(String info) {
        __doBuildEx(info, null, LogLevel.ERROR);
    }

    @Override
    public void error(Throwable e) {
        __doBuildEx(null, e, LogLevel.ERROR);
    }

    @Override
    public void error(String info, Throwable e) {
        __doBuildEx(info, e, LogLevel.ERROR);
    }

    @Override
    public void fatal(String info) {
        __doBuildEx(info, null, LogLevel.FATAL);
    }

    @Override
    public void fatal(Throwable e) {
        __doBuildEx(null, e, LogLevel.FATAL);
    }

    @Override
    public void fatal(String info, Throwable e) {
        __doBuildEx(info, e, LogLevel.FATAL);
    }
}
