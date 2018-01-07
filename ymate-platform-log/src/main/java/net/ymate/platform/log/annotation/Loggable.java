/*
 * Copyright 2007-2018 the original author or authors.
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
import net.ymate.platform.log.impl.DefaultLogooAdapter;
import net.ymate.platform.log.support.ILogooAdapter;

import java.lang.annotation.*;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/1/7 下午5:43
 * @version 1.0
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Loggable {

    /**
     * @return 设定输出到日志记录器名称集合
     */
    String[] value() default {};

    /**
     * @return 自定义标识
     */
    String flag() default "";

    /**
     * @return 自定义动作标识
     */
    String action() default "";

    /**
     * @return 日志输出级别, 默认为: INFO
     */
    ILogger.LogLevel level() default ILogger.LogLevel.INFO;

    /**
     * @return 是否日志合并输出, 默认为: false
     */
    boolean merge() default false;

    /**
     * @return 自定义日志适配器类
     */
    Class<? extends ILogooAdapter> adapterClass() default DefaultLogooAdapter.class;
}
