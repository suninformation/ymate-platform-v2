/*
 * Copyright 2007-present the original author or authors.
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

import net.ymate.platform.validation.validate.DefaultGroup;

import java.lang.annotation.*;

/**
 * 验证分组声明注解，用于在类或方法上声明当前验证使用的分组
 *
 * <p>当验证框架执行验证时，若未显式传入分组参数，
 * 将从目标类或方法上读取此注解声明的分组。</p>
 *
 * <p>使用示例：</p>
 * <pre>
 * &#64;ValidateGroups(Create.class)
 * public class UserCreateDTO {
 *     &#64;VRequired(groups = Create.class)
 *     private String name;
 * }
 *
 * &#64;RequestMapping("/user/create")
 * &#64;ValidateGroups(Create.class)
 * public IView createUser(...) { ... }
 * </pre>
 *
 * @author 刘镇 (suninformation@163.com) on 2026/6/22 01:31
 * @since 2.1.4
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ValidateGroups {

    /**
     * @return 验证分组，默认为DefaultGroup
     */
    Class<?>[] value() default {DefaultGroup.class};
}
