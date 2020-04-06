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

import net.ymate.platform.commons.util.ExpressionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.ThreadContext;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 16/6/12 上午1:27
 */
public class LogInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String DEFAULT_LOG_FORMAT = "${dateTime} ${level} [${hostName}] [${threadName}] [${threadId}:${callerInfo}] ${logContent}";

    private static volatile int SIMPLIFIED_PACKAGE_NAME_MAX_LENGTH;

    private static volatile int SIMPLIFIED_THREAD_NAME_MAX_LENGTH;

    private static volatile int SIMPLIFIED_THREAD_ID_MAX_LENGTH;

    private static synchronized int safeGetAndSetPackageNameMaxLength(int currentLength) {
        if (currentLength > SIMPLIFIED_PACKAGE_NAME_MAX_LENGTH) {
            SIMPLIFIED_PACKAGE_NAME_MAX_LENGTH = currentLength;
        }
        return SIMPLIFIED_PACKAGE_NAME_MAX_LENGTH;
    }

    private static synchronized int safeGetAndSetThreadNameMaxLength(int currentLength) {
        if (currentLength > SIMPLIFIED_THREAD_NAME_MAX_LENGTH) {
            SIMPLIFIED_THREAD_NAME_MAX_LENGTH = currentLength;
        }
        return SIMPLIFIED_THREAD_NAME_MAX_LENGTH;
    }

    private static synchronized int safeGetAndSetThreadIdMaxLength(int currentLength) {
        if (currentLength > SIMPLIFIED_THREAD_ID_MAX_LENGTH) {
            SIMPLIFIED_THREAD_ID_MAX_LENGTH = currentLength;
        }
        return SIMPLIFIED_THREAD_ID_MAX_LENGTH;
    }

    private String logName;

    private LogLevel level;

    private String hostName;

    private String threadName;

    private String threadId;

    private String callerInfo;

    private String logContent;

    private String stackInfo;

    private String createTime;

    public LogInfo(String logName, LogLevel level, String hostName, String threadName, String threadId, String callerInfo, String logContent, String stackInfo, String createTime) {
        this.logName = logName;
        this.level = level;
        this.hostName = hostName;
        this.threadName = threadName;
        this.threadId = threadId;
        this.callerInfo = callerInfo;
        this.logContent = logContent;
        this.stackInfo = stackInfo;
        this.createTime = createTime;
    }

    public String getLogName() {
        return logName;
    }

    public void setLogName(String logName) {
        this.logName = logName;
    }

    public LogLevel getLevel() {
        return level;
    }

    public void setLevel(LogLevel level) {
        this.level = level;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public String getCallerInfo() {
        return callerInfo;
    }

    public void setCallerInfo(String callerInfo) {
        this.callerInfo = callerInfo;
    }

    public String getLogContent() {
        return logContent;
    }

    public void setLogContent(String logContent) {
        this.logContent = logContent;
    }

    public String getStackInfo() {
        return stackInfo;
    }

    public void setStackInfo(String stackInfo) {
        this.stackInfo = stackInfo;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return toString(null, false);
    }

    /**
     * 日志输出
     *
     * @param logFormat 格式模板
     * @param padded    否采用格式化填充
     * @return 返回格式化日志内容
     */
    public String toString(String logFormat, boolean padded) {
        ExpressionUtils expressionUtils = ExpressionUtils.bind(StringUtils.defaultIfBlank(logFormat, DEFAULT_LOG_FORMAT))
                .set("dateTime", createTime)
                .set("level", level.getDisplayName())
                .set("hostName", hostName)
                .set("threadName", padded ? StringUtils.rightPad(threadName, safeGetAndSetThreadNameMaxLength(threadName.length()), StringUtils.SPACE) : threadName)
                .set("threadId", padded ? StringUtils.rightPad(threadId, safeGetAndSetThreadIdMaxLength(threadId.length()), StringUtils.SPACE) : threadId)
                .set("callerInfo", padded ? StringUtils.rightPad(callerInfo, safeGetAndSetPackageNameMaxLength(callerInfo.length()), StringUtils.SPACE) : callerInfo)
                .set("logContent", logContent);
        List<String> vars = expressionUtils.getVariables();
        if (!vars.isEmpty()) {
            Map<String, String> contextMap = ThreadContext.getContext();
            if (!contextMap.isEmpty()) {
                vars.forEach(var -> expressionUtils.set(var, contextMap.get(var)));
            }
        }
        String logStr = expressionUtils.clean().getResult();
        StringBuilder stringBuilder = new StringBuilder(StringUtils.trimToEmpty(logStr));
        if (StringUtils.isNotBlank(stackInfo)) {
            stringBuilder.append(" - ").append(stackInfo);
        }
        //
        return StringUtils.trim(stringBuilder.toString());
    }
}
