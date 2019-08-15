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
package net.ymate.platform.core.beans.annotation;

import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.*;

/**
 * 记录类成员属性值的变化
 *
 * @author 刘镇 (suninformation@163.com) on 16/7/3 上午2:58
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PropertyState {

    /**
     * @return 成员属性名称
     */
    String propertyName() default StringUtils.EMPTY;

    /**
     * @return 自定义别名
     */
    String aliasName() default StringUtils.EMPTY;

    /**
     * @return 成员属性设置方法名称
     */
    String setterName() default StringUtils.EMPTY;
}
