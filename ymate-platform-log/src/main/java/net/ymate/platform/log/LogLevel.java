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
/*
 * Copyright (c) 2007-2019, the original author or authors. All rights reserved.
 *
 * This program licensed under the terms of the GNU Lesser General Public License version 3.0
 * as published by the Free Software Foundation.
 */
package net.ymate.platform.log;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;

/**
 * 日志记录级别
 *
 * @author 刘镇 (suninformation@163.com) on 2011-8-27 下午03:58:07
 */
public enum LogLevel {

    /**
     * 全部
     */
    ALL(Integer.MAX_VALUE, "all", "[ALL  ]"),

    /**
     * 跟踪
     */
    TRACE(600, "trace", "[TRACE]"),

    /**
     * 调试
     */
    DEBUG(500, "debug", "[DEBUG]"),

    /**
     * 信息
     */
    INFO(400, "info", "[INFO ]"),

    /**
     * 警告
     */
    WARN(300, "warn", "[WARN ]"),

    /**
     * 错误
     */
    ERROR(200, "error", "[ERROR]"),

    /**
     * 失败
     */
    FATAL(100, "fatal", "[FATAL]"),

    /**
     * 关闭
     */
    OFF(0, "off", "[OFF  ]");

    /**
     * 日志级别名称
     */
    private final String name;

    /**
     * 日志级别显示名称
     */
    private final String displayName;

    /**
     * 日志级别值
     */
    private final int level;

    /**
     * 构造器
     *
     * @param level       日志输出级别值
     * @param name        日志输出级别名称
     * @param displayName 日志输出显示名称
     */
    LogLevel(int level, String name, String displayName) {
        this.level = level;
        this.name = name;
        this.displayName = displayName;
    }

    public static LogLevel parse(String levelName) {
        if (StringUtils.isNotBlank(levelName)) {
            for (LogLevel level : LogLevel.values()) {
                if (level.name.equalsIgnoreCase(levelName)) {
                    return level;
                }
            }
        }
        return null;
    }

    public static LogLevel parse(int level) {
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

    public static Level parse(LogLevel level) {
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

    public int getLevel() {
        return level;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }
}
