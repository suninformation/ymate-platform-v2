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
package net.ymate.platform.configuration.annotation;

import net.ymate.platform.configuration.IConfiguration;
import net.ymate.platform.configuration.impl.DefaultConfiguration;

import java.lang.annotation.*;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/3/8 下午9:56
 * @version 1.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Configurable {

    /**
     * @return 配置文件路径名称, 若未指定则默认为接口实现类名
     */
    String value() default "";

    /**
     * @return 配置类型
     */
    Class<? extends IConfiguration> type() default DefaultConfiguration.class;
}
