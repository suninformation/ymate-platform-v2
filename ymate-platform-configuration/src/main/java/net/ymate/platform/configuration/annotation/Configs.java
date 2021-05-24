/*
 * Copyright 2007-2021 the original author or authors.
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

import net.ymate.platform.core.configuration.IConfiguration;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.*;

/**
 * @author 刘镇 (suninformation@163.com) on 2021/4/28 10:13 下午
 * @since 2.1.0
 */
@Target({ElementType.PACKAGE, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Configs {

    /**
     * @return 配置分类名称, 默认值为: default
     */
    String category() default StringUtils.EMPTY;

    /**
     * @return 配置类集合
     */
    Class<? extends IConfiguration>[] value() default {};

    /**
     * @return 配置文件路径集合
     */
    String[] files() default {};
}
