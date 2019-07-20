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

/**
 * 日志记录器接口定义
 *
 * @author 刘镇 (suninformation@163.com) on 2011-8-27 下午03:58:07
 */
@Ignored
public interface ILogger {

    /**
     * 初始化日志记录器，并提供默认记录级别
     *
     * @param loggerName 日志记录名称
     * @param config     日志配置
     * @return 返回日志记录器实例
     * @throws Exception 初始化时可能产生的异常
     */
    ILogger initialize(String loggerName, ILogConfig config) throws Exception;

    /**
     * 是否已初始化
     *
     * @return 返回true表示已初始化
     */
    boolean isInitialized();

    /**
     * 获取指定名称的日志记录器对象
     *
     * @param loggerName 日志记录器名称
     * @param config     日志配置
     * @return 返回日志记录器对象
     * @throws Exception 获取日志记录器时可能产生异常
     */
    ILogger getLogger(String loggerName, ILogConfig config) throws Exception;

    /**
     * 获取日志记录器名称
     *
     * @return 返回名称字符串
     */
    String getLoggerName();

    /**
     * 销毁(停止)当前的日志记录器，需要清除所占用的资源，而且日志记录器一旦被停止，将无法恢复使用
     */
    void destroy();

    /**
     * 设置调用者深度
     *
     * @param depth 必须大于等于零
     * @return 返回当前日志记录器
     */
    ILogger depth(int depth);

    /**
     * 是否存在某个日志记录器
     *
     * @param loggerName 日志记录器名称
     * @return 如果当前日志记录器存在那么返回true，如果不存在那么返回false
     */
    boolean contains(String loggerName);

    /**
     * 获取当前日志级别
     *
     * @return 返回日志级别枚举值
     */
    LogLevel getLevel();

    //

    /**
     * 输出日志
     *
     * @param info  日志内容
     * @param level 日志级别
     */
    void log(String info, LogLevel level);

    /**
     * 输出日志
     *
     * @param e     异常对象
     * @param level 日志级别
     */
    void log(Throwable e, LogLevel level);

    /**
     * 输出日志
     *
     * @param info  日志内容
     * @param e     异常对象
     * @param level 日志级别
     */
    void log(String info, Throwable e, LogLevel level);

    //

    /**
     * 输出跟踪级别的日志
     *
     * @param info 日志内容
     */
    void trace(String info);

    /**
     * 输出跟踪级别的日志
     *
     * @param e 异常对象
     */
    void trace(Throwable e);

    /**
     * 输出跟踪级别的日志
     *
     * @param info 日志内容
     * @param e    异常对象
     */
    void trace(String info, Throwable e);

    //

    /**
     * 输出调试级别的日志
     *
     * @param info 日志内容
     */
    void debug(String info);

    /**
     * 输出调试级别的日志
     *
     * @param e 异常对象
     */
    void debug(Throwable e);

    /**
     * 输出调试级别的日志
     *
     * @param info 日志内容
     * @param e    异常对象
     */
    void debug(String info, Throwable e);

    //

    /**
     * 输出信息级别的日志
     *
     * @param info 日志内容
     */
    void info(String info);

    /**
     * 输出信息级别的日志
     *
     * @param e 异常对象
     */
    void info(Throwable e);

    /**
     * 输出信息级别的日志
     *
     * @param info 日志内容
     * @param e    异常对象
     */
    void info(String info, Throwable e);

    //

    /**
     * 输出警告级别的日志
     *
     * @param info 日志内容
     */
    void warn(String info);

    /**
     * 输出警告级别的日志
     *
     * @param e 异常对象
     */
    void warn(Throwable e);

    /**
     * 输出警告级别的日志
     *
     * @param info 日志内容
     * @param e    异常对象
     */
    void warn(String info, Throwable e);

    //

    /**
     * 输出错误级别的日志
     *
     * @param info 日志内容
     */
    void error(String info);

    /**
     * 输出错误级别的日志
     *
     * @param e 异常对象
     */
    void error(Throwable e);

    /**
     * 输出错误级别的日志
     *
     * @param info 日志内容
     * @param e    异常对象
     */
    void error(String info, Throwable e);

    //

    /**
     * 输出失败级别的日志
     *
     * @param info 日志内容
     */
    void fatal(String info);

    /**
     * 输出失败级别的日志
     *
     * @param e 异常对象
     */
    void fatal(Throwable e);

    /**
     * 输出失败级别的日志
     *
     * @param info 日志内容
     * @param e    异常对象
     */
    void fatal(String info, Throwable e);

    //

    /**
     * 判断当前日志级别是否为调试
     *
     * @return 返回true表示当前日志级别为调试
     */
    boolean isDebugEnabled();

    /**
     * 判断当前日志级别是否为错误
     *
     * @return 返回true表示当前日志级别为错误
     */
    boolean isErrorEnabled();

    /**
     * 判断当前日志级别是否为失败
     *
     * @return 返回true表示当前日志级别为失败
     */
    boolean isFatalEnabled();

    /**
     * 判断当前日志级别是否为信息
     *
     * @return 返回true表示当前日志级别为信息
     */
    boolean isInfoEnabled();

    /**
     * 判断当前日志级别是否为跟踪
     *
     * @return 返回true表示当前日志级别为跟踪
     */
    boolean isTraceEnabled();

    /**
     * 判断当前日志级别是否为警告
     *
     * @return 返回true表示当前日志级别为警告
     */
    boolean isWarnEnabled();
}
