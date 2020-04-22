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

import net.ymate.platform.persistence.jdbc.query.Join;

import java.lang.annotation.*;

/**
 * 定义一个关联关系配置
 *
 * @author 刘镇 (suninformation@163.com) on 2020/04/16 20:06
 * @since 2.1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(QJoins.class)
public @interface QJoin {

    /**
     * @return 指定关联表或子查询
     */
    QFrom from();

    /**
     * @return 设置关联条件集合
     */
    QCond[] on();

    /**
     * @return 关联方式，默认为：左连接
     */
    Join.Type type() default Join.Type.LEFT;
}
