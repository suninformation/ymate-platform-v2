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
package net.ymate.platform.log;

import net.ymate.platform.commons.util.RuntimeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.SimpleLog;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-07-05 13:09
 * @since 2.1.0
 */
public abstract class AbstractLogAdapter extends AbstractLogger {

    private Log simpleLog;

    private ILogger logger;

    private final String loggerName;

    private boolean initialized;

    protected AbstractLogAdapter(String loggerName) {
        this.loggerName = loggerName;
        //
        tryCheckAndInitLogImpl();
    }

    private Log tryGetLogSafely() {
        if (simpleLog == null) {
            simpleLog = new SimpleLog(loggerName);
        }
        return simpleLog;
    }

    private boolean tryCheckAndInitLogImpl() {
        ILog logOwner = Logs.get();
        if (logOwner == null || !logOwner.isInitialized()) {
            return false;
        } else if (!initialized) {
            try {
                logger = logOwner.getLogger(loggerName).depth(5);
                initialized = true;
            } catch (Exception e) {
                tryGetLogSafely().warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
            }
        }
        return initialized;
    }

    @Override
    protected void buildEx(String info, Throwable e, LogLevel level) {
        if (tryCheckAndInitLogImpl()) {
            logger.log(info, e, level);
        } else {
            switch (level) {
                case TRACE:
                    tryGetLogSafely().trace(info, e);
                    break;
                case DEBUG:
                    tryGetLogSafely().debug(info, e);
                    break;
                case WARN:
                    tryGetLogSafely().warn(info, e);
                    break;
                case ERROR:
                    tryGetLogSafely().error(info, e);
                    break;
                case FATAL:
                    tryGetLogSafely().fatal(info, e);
                    break;
                default:
                    tryGetLogSafely().info(info, e);
            }
        }
    }

    @Override
    protected void logWrite(LogLevel level, LogInfo content) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ILogger initialize(String loggerName, ILogConfig config) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public ILogger getLogger(String loggerName, ILogConfig config) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getLoggerName() {
        return loggerName;
    }

    @Override
    public void destroy() {
    }

    @Override
    public boolean contains(String loggerName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LogLevel getLevel() {
        throw new UnsupportedOperationException();
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
        if (isTraceEnabled()) {
            buildEx(info, null, LogLevel.TRACE);
        }
    }

    @Override
    public void trace(Throwable e) {
        if (isTraceEnabled()) {
            buildEx(null, e, LogLevel.TRACE);
        }
    }

    @Override
    public void trace(String info, Throwable e) {
        if (isTraceEnabled()) {
            buildEx(info, e, LogLevel.TRACE);
        }
    }

    @Override
    public void debug(String info) {
        if (isDebugEnabled()) {
            buildEx(info, null, LogLevel.DEBUG);
        }
    }

    @Override
    public void debug(Throwable e) {
        if (isDebugEnabled()) {
            buildEx(null, e, LogLevel.DEBUG);
        }
    }

    @Override
    public void debug(String info, Throwable e) {
        if (isDebugEnabled()) {
            buildEx(info, e, LogLevel.DEBUG);
        }
    }

    @Override
    public void info(String info) {
        if (isInfoEnabled()) {
            buildEx(info, null, LogLevel.INFO);
        }
    }

    @Override
    public void info(Throwable e) {
        if (isInfoEnabled()) {
            buildEx(null, e, LogLevel.INFO);
        }
    }

    @Override
    public void info(String info, Throwable e) {
        if (isInfoEnabled()) {
            buildEx(info, e, LogLevel.INFO);
        }
    }

    @Override
    public void warn(String info) {
        if (isWarnEnabled()) {
            buildEx(info, null, LogLevel.WARN);
        }
    }

    @Override
    public void warn(Throwable e) {
        if (isWarnEnabled()) {
            buildEx(null, e, LogLevel.WARN);
        }
    }

    @Override
    public void warn(String info, Throwable e) {
        if (isWarnEnabled()) {
            buildEx(info, e, LogLevel.WARN);
        }
    }

    @Override
    public void error(String info) {
        if (isErrorEnabled()) {
            buildEx(info, null, LogLevel.ERROR);
        }
    }

    @Override
    public void error(Throwable e) {
        if (isErrorEnabled()) {
            buildEx(null, e, LogLevel.ERROR);
        }
    }

    @Override
    public void error(String info, Throwable e) {
        if (isErrorEnabled()) {
            buildEx(info, e, LogLevel.ERROR);
        }
    }

    @Override
    public void fatal(String info) {
        if (isFatalEnabled()) {
            buildEx(info, null, LogLevel.FATAL);
        }
    }

    @Override
    public void fatal(Throwable e) {
        if (isFatalEnabled()) {
            buildEx(null, e, LogLevel.FATAL);
        }
    }

    @Override
    public void fatal(String info, Throwable e) {
        if (isFatalEnabled()) {
            buildEx(info, e, LogLevel.FATAL);
        }
    }

    @Override
    public boolean isDebugEnabled() {
        if (tryCheckAndInitLogImpl()) {
            return logger.isDebugEnabled();
        }
        return tryGetLogSafely().isDebugEnabled();
    }

    @Override
    public boolean isErrorEnabled() {
        if (tryCheckAndInitLogImpl()) {
            return logger.isErrorEnabled();
        }
        return tryGetLogSafely().isErrorEnabled();
    }

    @Override
    public boolean isFatalEnabled() {
        if (tryCheckAndInitLogImpl()) {
            return logger.isFatalEnabled();
        }
        return tryGetLogSafely().isFatalEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        if (tryCheckAndInitLogImpl()) {
            return logger.isInfoEnabled();
        }
        return tryGetLogSafely().isInfoEnabled();
    }

    @Override
    public boolean isTraceEnabled() {
        if (tryCheckAndInitLogImpl()) {
            return logger.isTraceEnabled();
        }
        return tryGetLogSafely().isTraceEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
        if (tryCheckAndInitLogImpl()) {
            return logger.isWarnEnabled();
        }
        return tryGetLogSafely().isWarnEnabled();
    }
}
