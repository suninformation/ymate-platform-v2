/*
 * Copyright 2007-2016 the original author or authors.
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

import net.ymate.platform.core.util.DateTimeUtils;
import net.ymate.platform.log.ILogger;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ParameterizedMessageFactory;
import org.slf4j.Marker;
import org.slf4j.impl.StaticMarkerBinder;
import org.slf4j.spi.LocationAwareLogger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/4 上午11:30
 * @version 1.0
 */
public class Log4jLogger implements LocationAwareLogger, Serializable {

    private static final long serialVersionUID = 1L;
    private transient Logger __logger;
    private final String name;
    private transient EventDataConverter converter;
    private boolean __allowOutputConsole;

    public Log4jLogger(final Logger logger, String name, boolean allowOutputConsole) {
        this.__logger = logger;
        this.__allowOutputConsole = allowOutputConsole;
        this.name = name;
        this.converter = createConverter();
    }

    protected void __doBuildEx(Object info, Throwable e, ILogger.LogLevel level, Marker marker) {
        StringBuilder _exSB = new StringBuilder(DateTimeUtils.formatTime(System.currentTimeMillis(), DateTimeUtils.YYYY_MM_DD_HH_MM_SS_SSS))
                .append(level.getDispName())
                .append('[')
                .append(Thread.currentThread().getId())
                .append(':')
                .append(__doMakeCallerInfo())
                .append(']')
                .append(' ').append(info == null ? StringUtils.EMPTY : StringUtils.trimToEmpty(info.toString()));
        if (e != null) {
            StringBuilder _stackSB = new StringBuilder(e.getClass().getName())
                    .append(": ")
                    .append(StringUtils.EMPTY)
                    .append(StringUtils.trimToEmpty(e.getMessage()))
                    .append("\n");
            StackTraceElement[] _stacks = e.getStackTrace();
            for (StackTraceElement _stack : _stacks) {
                _stackSB.append("\tat ")
                        .append(_stack)
                        .append("\n");
            }
            __ex(_stackSB, e.getCause());
            _exSB.append("- ").append(_stackSB);
        }
        __logger.log(Level.toLevel(level.getName(), Level.ALL), getMarker(marker), _exSB.toString());
        //
        if (__allowOutputConsole) {
            System.out.println(_exSB.toString());
        }
    }

    /**
     * 获取调用者信息
     *
     * @return 找到的堆栈信息，格式为：className.methodName:lineNumber，如果找不到则返回NO_STACK_TRACE:-1
     */
    protected String __doMakeCallerInfo() {
        StackTraceElement[] _stacks = new Throwable().getStackTrace();
        // 追溯到对应的调用行，如果对应行不存在，则不给出无法确定行号的输出
        if (_stacks.length > 3) {
            StackTraceElement _element = _stacks[3];
            return StringUtils.substringBeforeLast(_element.getClassName(), ".")
                    .concat(".")
                    .concat(_element.getMethodName())
                    .concat(":")
                    .concat(_element.getLineNumber() + StringUtils.EMPTY);
        }
        return "NO_STACK_TRACE:-1";
    }

    /**
     * 将异常输出到字符缓冲中
     *
     * @param stackSB 需要输出到的目标字符串缓冲，不可为空
     * @param t       需要输出的异常
     * @return 如果还有引起异常的源，那么返回true
     */
    protected boolean __ex(StringBuilder stackSB, Throwable t) {
        if (t != null) {
            stackSB.append("Caused by: ")
                    .append(t.getClass().getName())
                    .append(": ")
                    .append(StringUtils.trimToEmpty(t.getMessage()))
                    .append("\n");
            StackTraceElement[] _traces = t.getStackTrace();
            int _tracesSize = _traces.length;
            for (int _idx = 0; _idx < _tracesSize; _idx++) {
                if (_idx < 5) {
                    stackSB.append("\tat ") // 在堆栈行开始增加空格
                            .append(_traces[_idx]).append("\n");
                } else {
                    stackSB.append("\t... ")
                            .append(_tracesSize - 5)
                            .append(" more\n");
                    break;
                }
            }
            if (__ex(stackSB, t.getCause())) {
                return true;
            }
        }
        return false;
    }

    public void trace(final String msg) {
        if (isTraceEnabled()) {
            __doBuildEx(msg, null, ILogger.LogLevel.TRACE, null);
        }
    }

    public void trace(final String format, final Object arg) {
        if (isTraceEnabled()) {
            __doBuildEx(__getSafeMessages(format, arg), null, ILogger.LogLevel.TRACE, null);
        }
    }

    public void trace(final String format, final Object arg1, final Object arg2) {
        if (isTraceEnabled()) {
            __doBuildEx(__getSafeMessages(format, arg1, arg2), null, ILogger.LogLevel.TRACE, null);
        }
    }

    public void trace(final String format, final Object... args) {
        if (isTraceEnabled()) {
            __doBuildEx(__getSafeMessages(format, args), null, ILogger.LogLevel.TRACE, null);
        }
    }

    public void trace(final String msg, final Throwable t) {
        if (isTraceEnabled()) {
            __doBuildEx(msg, t, ILogger.LogLevel.TRACE, null);
        }
    }

    public boolean isTraceEnabled() {
        return __logger.isEnabled(Level.TRACE);
    }

    public boolean isTraceEnabled(final Marker marker) {
        return __logger.isEnabled(Level.TRACE, getMarker(marker));
    }

    public void trace(final Marker marker, final String msg) {
        if (isTraceEnabled(marker)) {
            __doBuildEx(msg, null, ILogger.LogLevel.TRACE, null);
        }
    }

    public void trace(final Marker marker, final String format, final Object arg) {
        if (isTraceEnabled(marker)) {
            __doBuildEx(__getSafeMessages(format, arg), null, ILogger.LogLevel.TRACE, marker);
        }
    }

    public void trace(final Marker marker, final String format, final Object arg1, final Object arg2) {
        if (isTraceEnabled(marker)) {
            __doBuildEx(__getSafeMessages(format, arg1, arg2), null, ILogger.LogLevel.TRACE, marker);
        }
    }

    public void trace(final Marker marker, final String format, final Object... args) {
        if (isTraceEnabled(marker)) {
            __doBuildEx(__getSafeMessages(format, args), null, ILogger.LogLevel.TRACE, marker);
        }
    }

    public void trace(final Marker marker, final String msg, final Throwable throwable) {
        if (isTraceEnabled(marker)) {
            __doBuildEx(msg, throwable, ILogger.LogLevel.TRACE, marker);
        }
    }

    public void debug(final String msg) {
        if (isDebugEnabled()) {
            __doBuildEx(msg, null, ILogger.LogLevel.DEBUG, null);
        }
    }

    public void debug(final String format, final Object arg) {
        if (isDebugEnabled()) {
            __doBuildEx(__getSafeMessages(format, arg), null, ILogger.LogLevel.DEBUG, null);
        }
    }

    public void debug(final String format, final Object arg1, final Object arg2) {
        if (isDebugEnabled()) {
            __doBuildEx(__getSafeMessages(format, arg1, arg2), null, ILogger.LogLevel.DEBUG, null);
        }
    }

    public void debug(final String format, final Object... args) {
        if (isDebugEnabled()) {
            __doBuildEx(__getSafeMessages(format, args), null, ILogger.LogLevel.DEBUG, null);
        }
    }

    public void debug(final String msg, final Throwable t) {
        if (isDebugEnabled()) {
            __doBuildEx(msg, t, ILogger.LogLevel.DEBUG, null);
        }
    }

    public boolean isDebugEnabled() {
        return __logger.isEnabled(Level.DEBUG);
    }

    public boolean isDebugEnabled(final Marker marker) {
        return __logger.isEnabled(Level.DEBUG, getMarker(marker));
    }

    public void debug(final Marker marker, final String msg) {
        if (isDebugEnabled(marker)) {
            __doBuildEx(msg, null, ILogger.LogLevel.DEBUG, null);
        }
    }

    public void debug(final Marker marker, final String format, final Object arg) {
        if (isDebugEnabled(marker)) {
            __doBuildEx(__getSafeMessages(format, arg), null, ILogger.LogLevel.DEBUG, marker);
        }
    }

    public void debug(final Marker marker, final String format, final Object arg1, final Object arg2) {
        if (isDebugEnabled(marker)) {
            __doBuildEx(__getSafeMessages(format, arg1, arg2), null, ILogger.LogLevel.DEBUG, marker);
        }
    }

    public void debug(final Marker marker, final String format, final Object... args) {
        if (isDebugEnabled(marker)) {
            __doBuildEx(__getSafeMessages(format, args), null, ILogger.LogLevel.DEBUG, marker);
        }
    }

    public void debug(final Marker marker, final String msg, final Throwable throwable) {
        if (isDebugEnabled(marker)) {
            __doBuildEx(msg, throwable, ILogger.LogLevel.DEBUG, marker);
        }
    }

    public void info(final String msg) {
        if (isInfoEnabled()) {
            __doBuildEx(msg, null, ILogger.LogLevel.INFO, null);
        }
    }

    public void info(final String format, final Object arg) {
        if (isInfoEnabled()) {
            __doBuildEx(__getSafeMessages(format, arg), null, ILogger.LogLevel.INFO, null);
        }
    }

    public void info(final String format, final Object arg1, final Object arg2) {
        if (isInfoEnabled()) {
            __doBuildEx(__getSafeMessages(format, arg1, arg2), null, ILogger.LogLevel.INFO, null);
        }
    }

    public void info(final String format, final Object... args) {
        if (isInfoEnabled()) {
            __doBuildEx(__getSafeMessages(format, args), null, ILogger.LogLevel.INFO, null);
        }
    }

    public void info(final String msg, final Throwable t) {
        if (isInfoEnabled()) {
            __doBuildEx(msg, t, ILogger.LogLevel.INFO, null);
        }
    }

    public boolean isInfoEnabled() {
        return __logger.isEnabled(Level.INFO);
    }

    public boolean isInfoEnabled(final Marker marker) {
        return __logger.isEnabled(Level.INFO, getMarker(marker));
    }

    public void info(final Marker marker, final String msg) {
        if (isInfoEnabled(marker)) {
            __doBuildEx(msg, null, ILogger.LogLevel.INFO, null);
        }
    }

    public void info(final Marker marker, final String format, final Object arg) {
        if (isInfoEnabled(marker)) {
            __doBuildEx(__getSafeMessages(format, arg), null, ILogger.LogLevel.INFO, marker);
        }
    }

    public void info(final Marker marker, final String format, final Object arg1, final Object arg2) {
        if (isInfoEnabled(marker)) {
            __doBuildEx(__getSafeMessages(format, arg1, arg2), null, ILogger.LogLevel.INFO, marker);
        }
    }

    public void info(final Marker marker, final String format, final Object... args) {
        if (isInfoEnabled(marker)) {
            __doBuildEx(__getSafeMessages(format, args), null, ILogger.LogLevel.INFO, marker);
        }
    }

    public void info(final Marker marker, final String msg, final Throwable throwable) {
        if (isInfoEnabled(marker)) {
            __doBuildEx(msg, throwable, ILogger.LogLevel.INFO, marker);
        }
    }

    public void warn(final String msg) {
        if (isWarnEnabled()) {
            __doBuildEx(msg, null, ILogger.LogLevel.WARN, null);
        }
    }

    public void warn(final String format, final Object arg) {
        if (isWarnEnabled()) {
            __doBuildEx(__getSafeMessages(format, arg), null, ILogger.LogLevel.WARN, null);
        }
    }

    public void warn(final String format, final Object arg1, final Object arg2) {
        if (isWarnEnabled()) {
            __doBuildEx(__getSafeMessages(format, arg1, arg2), null, ILogger.LogLevel.WARN, null);
        }
    }

    public void warn(final String format, final Object... args) {
        if (isWarnEnabled()) {
            __doBuildEx(__getSafeMessages(format, args), null, ILogger.LogLevel.WARN, null);
        }
    }

    public void warn(final String msg, final Throwable t) {
        if (isWarnEnabled()) {
            __doBuildEx(msg, t, ILogger.LogLevel.WARN, null);
        }
    }

    public boolean isWarnEnabled() {
        return __logger.isEnabled(Level.WARN);
    }

    public boolean isWarnEnabled(final Marker marker) {
        return __logger.isEnabled(Level.WARN, getMarker(marker));
    }

    public void warn(final Marker marker, final String msg) {
        if (isWarnEnabled(marker)) {
            __doBuildEx(msg, null, ILogger.LogLevel.WARN, null);
        }
    }

    public void warn(final Marker marker, final String format, final Object arg) {
        if (isWarnEnabled(marker)) {
            __doBuildEx(__getSafeMessages(format, arg), null, ILogger.LogLevel.WARN, marker);
        }
    }

    public void warn(final Marker marker, final String format, final Object arg1, final Object arg2) {
        if (isWarnEnabled(marker)) {
            __doBuildEx(__getSafeMessages(format, arg1, arg2), null, ILogger.LogLevel.WARN, marker);
        }
    }

    public void warn(final Marker marker, final String format, final Object... args) {
        if (isWarnEnabled(marker)) {
            __doBuildEx(__getSafeMessages(format, args), null, ILogger.LogLevel.WARN, marker);
        }
    }

    public void warn(final Marker marker, final String msg, final Throwable throwable) {
        if (isWarnEnabled(marker)) {
            __doBuildEx(msg, throwable, ILogger.LogLevel.WARN, marker);
        }
    }

    public void error(final String msg) {
        if (isErrorEnabled()) {
            __doBuildEx(msg, null, ILogger.LogLevel.ERROR, null);
        }
    }

    public void error(final String format, final Object arg) {
        if (isErrorEnabled()) {
            __doBuildEx(__getSafeMessages(format, arg), null, ILogger.LogLevel.ERROR, null);
        }
    }

    public void error(final String format, final Object arg1, final Object arg2) {
        if (isErrorEnabled()) {
            __doBuildEx(__getSafeMessages(format, arg1, arg2), null, ILogger.LogLevel.ERROR, null);
        }
    }

    public void error(final String format, final Object... args) {
        if (isErrorEnabled()) {
            __doBuildEx(__getSafeMessages(format, args), null, ILogger.LogLevel.ERROR, null);
        }
    }

    public void error(final String msg, final Throwable t) {
        if (isErrorEnabled()) {
            __doBuildEx(msg, t, ILogger.LogLevel.ERROR, null);
        }
    }

    public boolean isErrorEnabled() {
        return __logger.isEnabled(Level.ERROR);
    }

    public boolean isErrorEnabled(final Marker marker) {
        return __logger.isEnabled(Level.ERROR, getMarker(marker));
    }

    public void error(final Marker marker, final String msg) {
        if (isErrorEnabled(marker)) {
            __doBuildEx(msg, null, ILogger.LogLevel.ERROR, null);
        }
    }

    public void error(final Marker marker, final String format, final Object arg) {
        if (isErrorEnabled(marker)) {
            __doBuildEx(__getSafeMessages(format, arg), null, ILogger.LogLevel.ERROR, marker);
        }
    }

    public void error(final Marker marker, final String format, final Object arg1, final Object arg2) {
        if (isErrorEnabled(marker)) {
            __doBuildEx(__getSafeMessages(format, arg1, arg2), null, ILogger.LogLevel.ERROR, marker);
        }
    }

    public void error(final Marker marker, final String format, final Object... args) {
        if (isErrorEnabled(marker)) {
            __doBuildEx(__getSafeMessages(format, args), null, ILogger.LogLevel.ERROR, marker);
        }
    }

    public void error(final Marker marker, final String msg, final Throwable throwable) {
        if (isErrorEnabled(marker)) {
            __doBuildEx(msg, throwable, ILogger.LogLevel.ERROR, marker);
        }
    }

    public void log(final Marker marker, final String fqcn, final int level, final String message, final Object[] params, Throwable throwable) {
        __doBuildEx(__getSafeMessages(message, params), throwable, __parseLevel(level), marker);
    }

    private static org.apache.logging.log4j.Marker getMarker(final Marker marker) {
        if (marker == null) {
            return null;
        } else if (marker instanceof Log4jMarker) {
            return ((Log4jMarker) marker).getLog4jMarker();
        } else {
            final Log4jMarkerFactory factory = (Log4jMarkerFactory) StaticMarkerBinder.SINGLETON.getMarkerFactory();
            return ((Log4jMarker) factory.getMarker(marker)).getLog4jMarker();
        }
    }

    public String getName() {
        return name;
    }

    private void readObject(final ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {
        aInputStream.defaultReadObject();
        __logger = LogManager.getContext().getLogger(name);
        converter = createConverter();
    }

    private void writeObject(final ObjectOutputStream aOutputStream) throws IOException {
        aOutputStream.defaultWriteObject();
    }

    private static EventDataConverter createConverter() {
        try {
            Class.forName("org.slf4j.ext.EventData");
            return new EventDataConverter();
        } catch (final ClassNotFoundException cnfe) {
            return null;
        }
    }

    private static String __getSafeMessages(String msg, Object... args) {
        if (StringUtils.isNotBlank(msg)) {
            return ParameterizedMessageFactory.INSTANCE.newMessage(msg, args).getFormattedMessage();
        }
        return msg;
    }

    private static ILogger.LogLevel __parseLevel(int level) {
        switch (level) {
            case 600:
                return ILogger.LogLevel.TRACE;
            case 500:
                return ILogger.LogLevel.DEBUG;
            case 400:
                return ILogger.LogLevel.INFO;
            case 300:
                return ILogger.LogLevel.WARN;
            case 200:
                return ILogger.LogLevel.ERROR;
            case 100:
                return ILogger.LogLevel.FATAL;
            case 0:
                return ILogger.LogLevel.OFF;
            default:
                return ILogger.LogLevel.ALL;
        }
    }

    private static Level getLevel(final int i) {
        switch (i) {
            case LocationAwareLogger.TRACE_INT:
                return Level.TRACE;
            case LocationAwareLogger.DEBUG_INT:
                return Level.DEBUG;
            case LocationAwareLogger.INFO_INT:
                return Level.INFO;
            case LocationAwareLogger.WARN_INT:
                return Level.WARN;
            case LocationAwareLogger.ERROR_INT:
                return Level.ERROR;
        }
        return Level.ERROR;
    }
}
