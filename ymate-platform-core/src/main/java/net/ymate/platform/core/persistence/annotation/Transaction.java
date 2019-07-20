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
package net.ymate.platform.core.persistence.annotation;

import net.ymate.platform.core.persistence.base.Type;

import java.lang.annotation.*;

/**
 * 声明一个类或方法开启数据库事务
 *
 * @author 刘镇 (suninformation@163.com) on 2013年9月18日 下午8:17:44
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
public @interface Transaction {

    /**
     * @return 事务类型
     */
    Type.TRANSACTION value() default Type.TRANSACTION.READ_COMMITTED;
}
