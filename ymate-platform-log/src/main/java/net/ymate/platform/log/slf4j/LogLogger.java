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
package net.ymate.platform.log.slf4j;

import net.ymate.platform.log.AbstractLogAdapter;
import net.ymate.platform.log.LogLevel;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.message.ParameterizedMessageFactory;
import org.slf4j.Marker;
import org.slf4j.spi.LocationAwareLogger;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/4 上午11:30
 */
public class LogLogger extends AbstractLogAdapter implements LocationAwareLogger {

    LogLogger(String name) {
        super(name);
    }

    @Override
    public void trace(Throwable e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void trace(final String format, final Object arg) {
        if (isTraceEnabled()) {
            buildEx(getSafeMessages(format, arg), null, LogLevel.TRACE);
        }
    }

    @Override
    public void trace(final String format, final Object arg1, final Object arg2) {
        if (isTraceEnabled()) {
            buildEx(getSafeMessages(format, arg1, arg2), null, LogLevel.TRACE);
        }
    }

    @Override
    public void trace(final String format, final Object... args) {
        if (isTraceEnabled()) {
            buildEx(getSafeMessages(format, args), null, LogLevel.TRACE);
        }
    }

    @Override
    public boolean isTraceEnabled(final Marker marker) {
        return isTraceEnabled();
    }

    @Override
    public void trace(final Marker marker, final String msg) {
        if (isTraceEnabled(marker)) {
            buildEx(msg, null, LogLevel.TRACE);
        }
    }

    @Override
    public void trace(final Marker marker, final String format, final Object arg) {
        if (isTraceEnabled(marker)) {
            buildEx(getSafeMessages(format, arg), null, LogLevel.TRACE);
        }
    }

    @Override
    public void trace(final Marker marker, final String format, final Object arg1, final Object arg2) {
        if (isTraceEnabled(marker)) {
            buildEx(getSafeMessages(format, arg1, arg2), null, LogLevel.TRACE);
        }
    }

    @Override
    public void trace(final Marker marker, final String format, final Object... args) {
        if (isTraceEnabled(marker)) {
            buildEx(getSafeMessages(format, args), null, LogLevel.TRACE);
        }
    }

    @Override
    public void trace(final Marker marker, final String msg, final Throwable throwable) {
        if (isTraceEnabled(marker)) {
            buildEx(msg, throwable, LogLevel.TRACE);
        }
    }

    @Override
    public void debug(final String format, final Object arg) {
        if (isDebugEnabled()) {
            buildEx(getSafeMessages(format, arg), null, LogLevel.DEBUG);
        }
    }

    @Override
    public void debug(final String format, final Object arg1, final Object arg2) {
        if (isDebugEnabled()) {
            buildEx(getSafeMessages(format, arg1, arg2), null, LogLevel.DEBUG);
        }
    }

    @Override
    public void debug(final String format, final Object... args) {
        if (isDebugEnabled()) {
            buildEx(getSafeMessages(format, args), null, LogLevel.DEBUG);
        }
    }

    @Override
    public void debug(final String msg, final Throwable t) {
        if (isDebugEnabled()) {
            buildEx(msg, t, LogLevel.DEBUG);
        }
    }

    @Override
    public boolean isDebugEnabled(final Marker marker) {
        return isDebugEnabled();
    }

    @Override
    public void debug(final Marker marker, final String msg) {
        if (isDebugEnabled(marker)) {
            buildEx(msg, null, LogLevel.DEBUG);
        }
    }

    @Override
    public void debug(final Marker marker, final String format, final Object arg) {
        if (isDebugEnabled(marker)) {
            buildEx(getSafeMessages(format, arg), null, LogLevel.DEBUG);
        }
    }

    @Override
    public void debug(final Marker marker, final String format, final Object arg1, final Object arg2) {
        if (isDebugEnabled(marker)) {
            buildEx(getSafeMessages(format, arg1, arg2), null, LogLevel.DEBUG);
        }
    }

    @Override
    public void debug(final Marker marker, final String format, final Object... args) {
        if (isDebugEnabled(marker)) {
            buildEx(getSafeMessages(format, args), null, LogLevel.DEBUG);
        }
    }

    @Override
    public void debug(final Marker marker, final String msg, final Throwable throwable) {
        if (isDebugEnabled(marker)) {
            buildEx(msg, throwable, LogLevel.DEBUG);
        }
    }

    @Override
    public void info(final String format, final Object arg) {
        if (isInfoEnabled()) {
            buildEx(getSafeMessages(format, arg), null, LogLevel.INFO);
        }
    }

    @Override
    public void info(final String format, final Object arg1, final Object arg2) {
        if (isInfoEnabled()) {
            buildEx(getSafeMessages(format, arg1, arg2), null, LogLevel.INFO);
        }
    }

    @Override
    public void info(final String format, final Object... args) {
        if (isInfoEnabled()) {
            buildEx(getSafeMessages(format, args), null, LogLevel.INFO);
        }
    }

    @Override
    public boolean isInfoEnabled(final Marker marker) {
        return isInfoEnabled();
    }

    @Override
    public void info(final Marker marker, final String msg) {
        if (isInfoEnabled(marker)) {
            buildEx(msg, null, LogLevel.INFO);
        }
    }

    @Override
    public void info(final Marker marker, final String format, final Object arg) {
        if (isInfoEnabled(marker)) {
            buildEx(getSafeMessages(format, arg), null, LogLevel.INFO);
        }
    }

    @Override
    public void info(final Marker marker, final String format, final Object arg1, final Object arg2) {
        if (isInfoEnabled(marker)) {
            buildEx(getSafeMessages(format, arg1, arg2), null, LogLevel.INFO);
        }
    }

    @Override
    public void info(final Marker marker, final String format, final Object... args) {
        if (isInfoEnabled(marker)) {
            buildEx(getSafeMessages(format, args), null, LogLevel.INFO);
        }
    }

    @Override
    public void info(final Marker marker, final String msg, final Throwable throwable) {
        if (isInfoEnabled(marker)) {
            buildEx(msg, throwable, LogLevel.INFO);
        }
    }

    @Override
    public void warn(final String format, final Object arg) {
        if (isWarnEnabled()) {
            buildEx(getSafeMessages(format, arg), null, LogLevel.WARN);
        }
    }

    @Override
    public void warn(final String format, final Object arg1, final Object arg2) {
        if (isWarnEnabled()) {
            buildEx(getSafeMessages(format, arg1, arg2), null, LogLevel.WARN);
        }
    }

    @Override
    public void warn(final String format, final Object... args) {
        if (isWarnEnabled()) {
            buildEx(getSafeMessages(format, args), null, LogLevel.WARN);
        }
    }

    @Override
    public boolean isWarnEnabled(final Marker marker) {
        return isWarnEnabled();
    }

    @Override
    public void warn(final Marker marker, final String msg) {
        if (isWarnEnabled(marker)) {
            buildEx(msg, null, LogLevel.WARN);
        }
    }

    @Override
    public void warn(final Marker marker, final String format, final Object arg) {
        if (isWarnEnabled(marker)) {
            buildEx(getSafeMessages(format, arg), null, LogLevel.WARN);
        }
    }

    @Override
    public void warn(final Marker marker, final String format, final Object arg1, final Object arg2) {
        if (isWarnEnabled(marker)) {
            buildEx(getSafeMessages(format, arg1, arg2), null, LogLevel.WARN);
        }
    }

    @Override
    public void warn(final Marker marker, final String format, final Object... args) {
        if (isWarnEnabled(marker)) {
            buildEx(getSafeMessages(format, args), null, LogLevel.WARN);
        }
    }

    @Override
    public void warn(final Marker marker, final String msg, final Throwable throwable) {
        if (isWarnEnabled(marker)) {
            buildEx(msg, throwable, LogLevel.WARN);
        }
    }

    @Override
    public void error(final String format, final Object arg) {
        if (isErrorEnabled()) {
            buildEx(getSafeMessages(format, arg), null, LogLevel.ERROR);
        }
    }

    @Override
    public void error(final String format, final Object arg1, final Object arg2) {
        if (isErrorEnabled()) {
            buildEx(getSafeMessages(format, arg1, arg2), null, LogLevel.ERROR);
        }
    }

    @Override
    public void error(final String format, final Object... args) {
        if (isErrorEnabled()) {
            buildEx(getSafeMessages(format, args), null, LogLevel.ERROR);
        }
    }

    @Override
    public boolean isErrorEnabled(final Marker marker) {
        return isErrorEnabled();
    }

    @Override
    public void error(final Marker marker, final String msg) {
        if (isErrorEnabled(marker)) {
            buildEx(msg, null, LogLevel.ERROR);
        }
    }

    @Override
    public void error(final Marker marker, final String format, final Object arg) {
        if (isErrorEnabled(marker)) {
            buildEx(getSafeMessages(format, arg), null, LogLevel.ERROR);
        }
    }

    @Override
    public void error(final Marker marker, final String format, final Object arg1, final Object arg2) {
        if (isErrorEnabled(marker)) {
            buildEx(getSafeMessages(format, arg1, arg2), null, LogLevel.ERROR);
        }
    }

    @Override
    public void error(final Marker marker, final String format, final Object... args) {
        if (isErrorEnabled(marker)) {
            buildEx(getSafeMessages(format, args), null, LogLevel.ERROR);
        }
    }

    @Override
    public void error(final Marker marker, final String msg, final Throwable throwable) {
        if (isErrorEnabled(marker)) {
            buildEx(msg, throwable, LogLevel.ERROR);
        }
    }

    @Override
    public void log(final Marker marker, final String fqcn, final int level, final String message, final Object[] params, Throwable throwable) {
        LogLevel logLevel;
        switch (level) {
            case TRACE_INT:
                logLevel = LogLevel.TRACE;
                break;
            case DEBUG_INT:
                logLevel = LogLevel.DEBUG;
                break;
            case WARN_INT:
                logLevel = LogLevel.WARN;
                break;
            case ERROR_INT:
                logLevel = LogLevel.ERROR;
                break;
            default:
                logLevel = LogLevel.INFO;
        }
        buildEx(getSafeMessages(message, params), throwable, logLevel);
    }

    @Override
    public String getName() {
        return getLoggerName();
    }

    private static String getSafeMessages(String msg, Object... args) {
        if (StringUtils.isNotBlank(msg)) {
            return ParameterizedMessageFactory.INSTANCE.newMessage(msg, args).getFormattedMessage();
        }
        return msg;
    }
}
