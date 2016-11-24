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
package net.ymate.platform.log.jcl;

import net.ymate.platform.core.util.DateTimeUtils;
import net.ymate.platform.log.ILogger;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;

/**
 * @author 刘镇 (suninformation@163.com) on 15/9/28 21:16
 * @version 1.0
 */
public class JCLogger implements Log, Serializable {

    private static final long serialVersionUID = 1L;

    private final Logger __logger;

    private boolean __allowOutputConsole;

    public JCLogger(final Logger logger, boolean allowOutputConsole) {
        __logger = logger;
        __allowOutputConsole = allowOutputConsole;
    }

    protected void __doBuildEx(Object info, Throwable e, ILogger.LogLevel level) {
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
        __logger.log(Level.toLevel(level.getName(), Level.ALL), _exSB.toString());
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
            return _element.getClassName() + "." + _element.getMethodName() + ":" + _element.getLineNumber() + StringUtils.EMPTY;
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

    public void debug(Object message) {
        if (isDebugEnabled()) {
            __doBuildEx(message, null, ILogger.LogLevel.DEBUG);
        }
    }

    public void debug(Object message, Throwable t) {
        if (isDebugEnabled()) {
            __doBuildEx(message, t, ILogger.LogLevel.DEBUG);
        }
    }

    public void error(Object message) {
        if (isErrorEnabled()) {
            __doBuildEx(message, null, ILogger.LogLevel.ERROR);
        }
    }

    public void error(Object message, Throwable t) {
        if (isErrorEnabled()) {
            __doBuildEx(message, t, ILogger.LogLevel.ERROR);
        }
    }

    public void fatal(Object message) {
        if (isFatalEnabled()) {
            __doBuildEx(message, null, ILogger.LogLevel.FATAL);
        }
    }

    public void fatal(Object message, Throwable t) {
        if (isFatalEnabled()) {
            __doBuildEx(message, t, ILogger.LogLevel.FATAL);
        }
    }

    public void info(Object message) {
        if (isInfoEnabled()) {
            __doBuildEx(message, null, ILogger.LogLevel.INFO);
        }
    }

    public void info(Object message, Throwable t) {
        if (isInfoEnabled()) {
            __doBuildEx(message, t, ILogger.LogLevel.INFO);
        }
    }

    public boolean isDebugEnabled() {
        return __logger.isDebugEnabled();
    }

    public boolean isErrorEnabled() {
        return __logger.isErrorEnabled();
    }

    public boolean isFatalEnabled() {
        return __logger.isFatalEnabled();
    }

    public boolean isInfoEnabled() {
        return __logger.isInfoEnabled();
    }

    public boolean isTraceEnabled() {
        return __logger.isTraceEnabled();
    }

    public boolean isWarnEnabled() {
        return __logger.isWarnEnabled();
    }

    public void trace(Object message) {
        if (isTraceEnabled()) {
            __doBuildEx(message, null, ILogger.LogLevel.TRACE);
        }
    }

    public void trace(Object message, Throwable t) {
        if (isTraceEnabled()) {
            __doBuildEx(message, t, ILogger.LogLevel.TRACE);
        }
    }

    public void warn(Object message) {
        if (isWarnEnabled()) {
            __doBuildEx(message, null, ILogger.LogLevel.WARN);
        }
    }

    public void warn(Object message, Throwable t) {
        if (isWarnEnabled()) {
            __doBuildEx(message, t, ILogger.LogLevel.WARN);
        }
    }
}
