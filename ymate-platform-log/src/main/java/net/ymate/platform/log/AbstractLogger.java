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

import net.ymate.platform.commons.util.DateTimeUtils;
import net.ymate.platform.commons.util.NetworkUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 抽象日志记录器接口实现
 *
 * @author 刘镇 (suninformation@163.com) on 2012-12-21 下午12:27:37
 */
public abstract class AbstractLogger implements ILogger {

    private static final Map<String, String> SIMPLIFIED_PACKAGE_NAMES = new ConcurrentHashMap<>();

    /**
     * 堆栈深度，向上寻找堆栈长度
     */
    private int depth = 3;

    @Override
    public ILogger depth(int depth) {
        this.depth = depth;
        return this;
    }

    /**
     * 日志写入方法, 需子类实现
     *
     * @param level   日志级别
     * @param content 内容
     */
    protected abstract void logWrite(LogLevel level, LogInfo content);

    protected void buildEx(String info, Throwable e, LogLevel level) {
        Thread currentThread = Thread.currentThread();
        LogInfo logInfo = new LogInfo(getLoggerName(), level, NetworkUtils.IP.getHostName(), currentThread.getName(), String.valueOf(currentThread.getId()), buildMakeCallerInfo(), info, buildMakeStackInfo(e),
                DateTimeUtils.formatTime(System.currentTimeMillis(), DateTimeUtils.YYYY_MM_DD_HH_MM_SS_SSS));
        // 判断是否输出到控制台
        ILog logOwner = Logs.get();
        ILogConfig logConfig = logOwner != null ? logOwner.getConfig() : null;
        if (logConfig != null && logConfig.isAllowConsoleOutput()) {
            System.out.println(logInfo.toString(logConfig.getLogFormat(), logConfig.isFormatPaddedOutput()));
        }
        //
        logWrite(level, logInfo);
    }

    /**
     * 获取调用者信息
     *
     * @return 找到的堆栈信息，格式为：className.methodName:lineNumber，如果找不到则返回NO_STACK_TRACE:-1
     */
    public String buildMakeCallerInfo() {
        StackTraceElement[] stacks = new Throwable().getStackTrace();
        // 追溯到对应的调用行，如果对应行不存在，则不给出无法确定行号的输出
        if (depth >= 0 && stacks.length > 1 + depth) {
            StackTraceElement element = stacks[1 + depth];
            return buildSimplePackageName(element.getClassName()) + "." + element.getMethodName() + ":" + element.getLineNumber();
        }
        return "NO_STACK_TRACE:-1";
    }

    public String buildSimplePackageName(String originPackageName) {
        ILog logOwner = Logs.get();
        ILogConfig logConfig = logOwner != null ? logOwner.getConfig() : null;
        if (logConfig != null && logConfig.isSimplifiedPackageName()) {
            String packageName = SIMPLIFIED_PACKAGE_NAMES.get(originPackageName);
            if (packageName == null) {
                String[] nameParts = StringUtils.split(originPackageName, '.');
                if (nameParts != null && nameParts.length > 1) {
                    for (int idx = 0; idx < nameParts.length - 1; idx++) {
                        String part = nameParts[idx];
                        if (part != null && part.length() > 1) {
                            nameParts[idx] = String.valueOf(part.charAt(0));
                        }
                    }
                    packageName = StringUtils.join(nameParts, '.');
                    //
                    SIMPLIFIED_PACKAGE_NAMES.put(originPackageName, packageName);
                }
            }
            return StringUtils.defaultIfBlank(packageName, originPackageName);
        }
        return originPackageName;
    }

    /**
     * 将异常转换为堆栈输出串
     *
     * @param e 需要输出的异常对象
     * @return 转换出的字符串，不为空
     */
    public String buildMakeStackInfo(Throwable e) {
        if (e == null) {
            return StringUtils.EMPTY;
        }
        StringBuilder stringBuilder = new StringBuilder(e.getClass().getName())
                .append(": ")
                .append(StringUtils.EMPTY)
                .append(StringUtils.trimToEmpty(e.getMessage()))
                .append("\n");
        StackTraceElement[] stacks = e.getStackTrace();
        for (StackTraceElement stack : stacks) {
            stringBuilder.append("\tat ")
                    .append(stack)
                    .append("\n");
        }
        ex(stringBuilder, e.getCause());
        return stringBuilder.toString();
    }

    /**
     * 将异常输出到字符缓冲中
     *
     * @param stack 需要输出到的目标字符串缓冲，不可为空
     * @param t     需要输出的异常
     * @return 如果还有引起异常的源，那么返回true
     */
    public boolean ex(StringBuilder stack, Throwable t) {
        if (t != null) {
            stack.append("Caused by: ")
                    .append(t.getClass().getName())
                    .append(": ")
                    .append(StringUtils.trimToEmpty(t.getMessage()))
                    .append("\n");
            StackTraceElement[] traces = t.getStackTrace();
            int tracesSize = traces.length;
            //
            ILog logOwner = Logs.get();
            ILogConfig logConfig = logOwner != null ? logOwner.getConfig() : null;
            int printStackCount = logConfig != null ? logConfig.getPrintStackCount() : 0;
            if (printStackCount <= 0) {
                printStackCount = 5;
            }
            for (int idx = 0; idx < tracesSize; idx++) {
                if (idx < printStackCount) {
                    stack.append("\tat ")
                            .append(traces[idx]).append("\n");
                } else {
                    stack.append("\t... ")
                            .append(tracesSize - printStackCount)
                            .append(" more\n");
                    break;
                }
            }
            return ex(stack, t.getCause());
        }
        return false;
    }
}
