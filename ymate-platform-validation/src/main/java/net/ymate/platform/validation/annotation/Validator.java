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
package net.ymate.platform.validation.annotation;

import java.lang.annotation.*;

/**
 * 声明一个类为验证器
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/15 上午11:31
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Validator {

    /**
     * @return 返回验证器处理的自定义验证注解
     */
    Class<? extends Annotation> value();
}
