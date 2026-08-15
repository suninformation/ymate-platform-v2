/*
 * Copyright 2007-present the original author or authors.
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

import net.ymate.platform.core.support.IInitialization;

/**
 * @author 刘镇 (suninformation@163.com) on 2026/8/15 2:50
 * @since 2.1.4
 */
public interface ILoggerFactory extends IInitialization<ILogConfig> {

    /**
     * 获取指定名称的日志记录器对象
     *
     * @param loggerName 日志记录器名称
     * @return 返回日志记录器对象
     * @throws Exception 获取日志记录器时可能产生异常
     */
    ILogger getLogger(String loggerName) throws Exception;

    /**
     * 销毁
     */
    void destroy();
}
