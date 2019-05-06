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
package net.ymate.platform.log;

import net.ymate.platform.core.util.DateTimeUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 抽象日志记录器接口实现
 *
 * @author 刘镇 (suninformation@163.com) on 2012-12-21 下午12:27:37
 * @version 1.0
 */
public abstract class AbstractLogger implements ILogger {

    private static Map<String, String> __SIMPLIFIED_PACKAGE_NAMES = new ConcurrentHashMap<String, String>();

    private static volatile int __SIMPLIFIED_PACKAGE_NAME_MAX_LENGTH;

    /**
     * 打印堆栈数量，超过这个数量会省略输出
     */
    public static int PRINT_STACK_COUNT = 5;

    private boolean __allowOutputConsole;

    private boolean __simplifiedPackageName;

    private boolean __formatPadded;

    /**
     * 堆栈深度，向上寻找堆栈长度
     */
    private int __depth = 3;

    @Override
    public ILogger console(boolean enable) {
        __allowOutputConsole = enable;
        return this;
    }

    @Override
    public ILogger simplified(boolean enable) {
        __simplifiedPackageName = enable;
        return this;
    }

    @Override
    public ILogger padded(boolean enable) {
        __formatPadded = enable;
        return this;
    }

    @Override
    public ILogger depth(int depth) {
        __depth = depth;
        return this;
    }

    protected abstract void __doLogWrite(LogLevel level, LogInfo content);

    protected void __doBuildEx(String info, Throwable e, ILogger.LogLevel level) {
        long _long = Thread.currentThread().getId();
        LogInfo _info = new LogInfo(getLoggerName(),
                level.getDispName(),
                _long, __doMakeCallerInfo(String.valueOf(_long).length() + 1), info, __doMakeStackInfo(e),
                DateTimeUtils.formatTime(System.currentTimeMillis(), DateTimeUtils.YYYY_MM_DD_HH_MM_SS_SSS));
        //
        __doLogWrite(level, _info);
        // 判断是否输出到控制台
        if (__allowOutputConsole) {
            System.out.println(_info.toString());
        }
    }

    private static synchronized int __safeGetAndSetMaxLength(int currentLength) {
        if (currentLength > __SIMPLIFIED_PACKAGE_NAME_MAX_LENGTH) {
            __SIMPLIFIED_PACKAGE_NAME_MAX_LENGTH = currentLength;
        }
        return __SIMPLIFIED_PACKAGE_NAME_MAX_LENGTH;
    }

    /**
     * 获取调用者信息
     *
     * @param extLength 扩展长度
     * @return 找到的堆栈信息，格式为：className.methodName:lineNumber，如果找不到则返回NO_STACK_TRACE:-1
     */
    protected String __doMakeCallerInfo(int extLength) {
        StackTraceElement[] _stacks = new Throwable().getStackTrace();
        // 追溯到对应的调用行，如果对应行不存在，则不给出无法确定行号的输出
        if (__depth >= 0 && _stacks.length > 1 + __depth) {
            StackTraceElement _element = _stacks[1 + __depth];
            String _logRow = __doSimplePackageName(_element.getClassName()) + "." + _element.getMethodName() + ":" + _element.getLineNumber();
            int _currLength = __safeGetAndSetMaxLength(_logRow.length() + extLength);
            return __formatPadded ? StringUtils.rightPad(_logRow, _currLength - extLength, ' ') : _logRow;
        }
        return "NO_STACK_TRACE:-1";
    }

    private String __doSimplePackageName(String originPackageName) {
        if (__simplifiedPackageName) {
            String _packageName = __SIMPLIFIED_PACKAGE_NAMES.get(originPackageName);
            if (_packageName == null) {
                String[] _nameParts = StringUtils.split(originPackageName, '.');
                if (_nameParts != null && _nameParts.length > 1) {
                    for (int _idx = 0; _idx < _nameParts.length - 1; _idx++) {
                        String _part = _nameParts[_idx];
                        if (_part != null && _part.length() > 1) {
                            _nameParts[_idx] = String.valueOf(_part.charAt(0));
                        }
                    }
                    _packageName = StringUtils.join(_nameParts, '.');
                    //
                    __SIMPLIFIED_PACKAGE_NAMES.put(originPackageName, _packageName);
                }
            }
            return StringUtils.defaultIfBlank(_packageName, originPackageName);
        }
        return originPackageName;
    }

    /**
     * 将异常转换为堆栈输出串
     *
     * @param e 需要输出的异常对象
     * @return 转换出的字符串，不为空
     */
    protected String __doMakeStackInfo(Throwable e) {
        if (e == null) {
            return StringUtils.EMPTY;
        }
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
        return _stackSB.toString();
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
                if (_idx < PRINT_STACK_COUNT) {
                    stackSB.append("\tat ")
                            .append(_traces[_idx]).append("\n");
                } else {
                    stackSB.append("\t... ")
                            .append(_tracesSize - PRINT_STACK_COUNT)
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
}
