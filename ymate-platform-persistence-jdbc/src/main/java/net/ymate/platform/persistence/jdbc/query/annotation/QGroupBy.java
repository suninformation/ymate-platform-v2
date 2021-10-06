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

import java.lang.annotation.*;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/04/17 11:56
 * @since 2.1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface QGroupBy {

    /**
     * @return 参于分组的字段
     */
    QField[] value();

    /**
     * @return 条件过滤
     */
    QCond[] having() default {};

    /**
     * @return 是否对分组结果进行数据统计，默认为false
     */
    boolean rollup() default false;
}
