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
package net.ymate.platform.core.persistence.annotation;

import net.ymate.platform.core.persistence.IValueRenderer;

import java.lang.annotation.*;

/**
 * @author 刘镇 (suninformation@163.com) on 2021/12/22 7:36 下午
 * @since 2.1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Documented
public @interface ValueRenderer {

    /**
     * @return 指定属性值渲染器类型集合，将按配置顺序执行渲染操作
     */
    Class<? extends IValueRenderer>[] value();

    /**
     * @return 自定义参数集合
     * @since 2.1.3
     */
    String[] params() default {};
}
