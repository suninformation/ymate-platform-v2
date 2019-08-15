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
package net.ymate.platform.configuration.annotation;

import net.ymate.platform.core.configuration.IConfigurationProvider;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.*;

/**
 * 配置文件加载路径注解
 *
 * @author 刘镇 (suninformation@163.com) on 2011-8-27 下午01:13:29
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Configuration {

    /**
     * @return 配置文件路径名称
     */
    String value() default StringUtils.EMPTY;

    /**
     * @return 是否自动重新加载
     */
    boolean reload() default true;

    /**
     * @return 配置文件自定义内容分析器
     */
    Class<? extends IConfigurationProvider> provider() default IConfigurationProvider.class;
}
