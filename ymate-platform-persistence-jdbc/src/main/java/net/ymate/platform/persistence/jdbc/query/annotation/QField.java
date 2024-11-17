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
package net.ymate.platform.persistence.jdbc.query.annotation;

import net.ymate.platform.persistence.jdbc.query.Cond;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.*;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/04/16 20:13
 * @since 2.1.0
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface QField {

    /**
     * @return 自定义前缀
     */
    String prefix() default StringUtils.EMPTY;

    /**
     * @return 名称
     */
    String value();

    /**
     * @return 别名
     */
    String alias() default StringUtils.EMPTY;

    /**
     * @return 是否包装标识符
     * @since 2.1.2
     */
    boolean wrapIdentifier() default true;

    /**
     * @return 配合 @QGroupBy 注解使用，标记当前字段用于分组
     * @since 2.1.3
     */
    boolean grouped() default true;

    /**
     * @return 运算操作方式（用于通过类成员属性构建 Cond 对象），默认为：等于
     * @since 2.1.3
     */
    Cond.OPT opt() default Cond.OPT.EQ;
}
