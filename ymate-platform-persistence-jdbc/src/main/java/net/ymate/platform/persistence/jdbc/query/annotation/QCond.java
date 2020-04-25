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

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 定义一个条件
 *
 * @author 刘镇 (suninformation@163.com) on 2020/04/16 19:50
 * @since 2.1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface QCond {

    /**
     * @return 与已存在条件之间的关系，默认为：与
     */
    Cond.LogicalOpt logicalOpt() default Cond.LogicalOpt.AND;

    /**
     * @return 运算操作方式，默认为：等于
     */
    Cond.OPT opt() default Cond.OPT.EQ;

    /**
     * @return 条件字段1
     */
    QField fieldA();

    /**
     * @return 条件字段2（在此QField注解中，支持以'#'开头的字符形式参数变量）
     */
    QField fieldB();
}
