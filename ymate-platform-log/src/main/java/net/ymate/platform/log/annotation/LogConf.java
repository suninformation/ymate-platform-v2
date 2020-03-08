/*
 * Copyright 2007-2020 the original author or authors.
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
package net.ymate.platform.log.annotation;

import net.ymate.platform.log.ILogger;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.*;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/03/09 11:32
 * @since 2.1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogConf {

    /**
     * @return 日志记录器配置文件
     */
    String configFile() default StringUtils.EMPTY;

    /**
     * @return 日志文件输出路径
     */
    String outputDir() default StringUtils.EMPTY;

    /**
     * @return 默认日志记录器名称
     */
    String defaultLoggerName() default StringUtils.EMPTY;

    /**
     * @return 日志格式模板
     */
    String logFormat() default StringUtils.EMPTY;

    /**
     * @return 打印堆栈数量
     */
    int printStackCount() default 0;

    /**
     * @return 否允许控制台输出
     */
    boolean allowConsoleOutput() default false;

    /**
     * @return 否采用简化包名输出
     */
    boolean formatPaddedOutput() default false;

    /**
     * @return 否采用格式化填充输出
     */
    boolean simplifiedPackageName() default false;

    /**
     * @return 日志记录器接口实现类
     */
    Class<? extends ILogger> loggerClass() default ILogger.class;
}
