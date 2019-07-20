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
import net.ymate.platform.core.support.IDestroyable;

/**
 * 日志管理器接口定义
 *
 * @author 刘镇 (suninformation@163.com) on 15/3/17 下午2:40
 */
@Ignored
public interface ILog extends IDestroyable {

    String MODULE_NAME = "log";

    String LOG_OUT_DIR = "LOG_OUT_DIR";

    /**
     * 初始化
     *
     * @throws Exception 初始过程中产生的任何异常
     */
    void initialize() throws Exception;

    /**
     * 是否已初始化
     *
     * @return 返回true表示已初始化
     */
    boolean isInitialized();

    /**
     * 获取日志记录器配置
     *
     * @return 返回日志记录器配置对象
     */
    ILogConfig getConfig();

    /**
     * 获取默认日志记录器
     *
     * @return 返回日志记录器对象
     */
    ILogger getLogger();

    /**
     * 获取指定名称的日志记录器
     *
     * @param loggerName 日志记录器名称
     * @return 返回日志记录器对象
     * @throws Exception 可能产生的任何异常
     */
    ILogger getLogger(String loggerName) throws Exception;

    /**
     * 获取指定类名称的日志记录器
     *
     * @param clazz 类型
     * @return 返回日志记录器对象
     * @throws Exception 可能产生的任何异常
     */
    ILogger getLogger(Class<?> clazz) throws Exception;
}
