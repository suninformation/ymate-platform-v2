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

import net.ymate.platform.core.beans.annotation.Ignored;
import net.ymate.platform.core.support.IInitialization;

import java.io.File;

/**
 * 日志管理器配置接口
 *
 * @author 刘镇 (suninformation@163.com) on 2012-11-27 下午7:01:57
 */
@Ignored
public interface ILogConfig extends IInitialization<ILog> {

    String DEFAULT_STR = "default";

    String DEFAULT_CONFIG_FILE = "${root}/cfgs/log4j.xml";

    String DEFAULT_OUTPUT_DIR = "${root}/logs/";

    String CONFIG_FILE = "config_file";

    String OUTPUT_DIR = "output_dir";

    String LOGGER_NAME = "logger_name";

    String LOGGER_CLASS = "logger_class";

    String LOG_FORMAT = "log_format";

    String PRINT_STACK_COUNT = "print_stack_count";

    String ALLOW_OUTPUT_CONSOLE = "allow_output_console";

    String SIMPLIFIED_PACKAGE_NAME = "simplified_package_name";

    String FORMAT_PADDED_OUTPUT = "format_padded_output";

    /**
     * 获取日志记录器配置文件
     *
     * @return 返回日志记录器配置文件
     */
    File getConfigFile();

    /**
     * 获取日志文件输出路径
     *
     * @return 返回日志文件输出路径
     */
    File getOutputDir();

    /**
     * 获取默认日志记录器名称
     *
     * @return 返回默认日志记录器名称
     */
    String getDefaultLoggerName();

    /**
     * 获取打印堆栈数量，超过这个数量会省略输出，默认值：5
     *
     * @return 返回打印堆栈数量
     */
    int getPrintStackCount();

    /**
     * 获取日志格式模板, 默认为: "${dateTime} ${level} [${hostName}] [${threadName}] [${threadId}:${callerInfo}] ${logContent}"
     *
     * @return 返回日志格式模板字符串
     */
    String getLogFormat();

    /**
     * 获取ILogger接口实现类类型
     *
     * @return 返回ILogger接口实现类类型
     */
    Class<? extends ILogger> getLoggerClass();

    /**
     * 否允许控制台输出
     *
     * @return 日志记录器是否允许控制台输出
     */
    boolean isAllowConsoleOutput();

    /**
     * 否采用简化包名输出
     *
     * @return 日志记录器是否采用简化包名输出
     */
    boolean isSimplifiedPackageName();

    /**
     * 否采用格式化填充输出
     *
     * @return 日志记录器是否采用格式化填充输出
     */
    boolean isFormatPaddedOutput();
}
