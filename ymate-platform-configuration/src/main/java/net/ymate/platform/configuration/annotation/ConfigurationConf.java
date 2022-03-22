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
package net.ymate.platform.configuration.annotation;

import net.ymate.platform.core.configuration.IConfigurationProvider;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.*;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/03/09 16:16
 * @since 2.1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigurationConf {

    /**
     * @return 配置体系根路径
     */
    String configHome() default StringUtils.EMPTY;

    /**
     * @return 项目名称
     */
    String projectName() default StringUtils.EMPTY;

    /**
     * @return 模块名称
     */
    String moduleName() default StringUtils.EMPTY;

    /**
     * @return 配置文件基准目录名称
     */
    String configBaseDir() default StringUtils.EMPTY;

    /**
     * @return 配置文件检查时间间隔(毫秒)
     */
    long checkTimeInterval() default 0;

    /**
     * @return 默认配置文件分析器
     */
    Class<? extends IConfigurationProvider> providerClass() default IConfigurationProvider.class;
}
