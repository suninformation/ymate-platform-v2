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

import java.lang.annotation.*;

/**
 * 若在方法上声明则该方法将忽略一切拦截器配置;
 * 若在包或类上声明则该类将被自动扫描忽略;
 * 若声明在接口上则表示对象管理器不会使用该接口与实现类绑定映射关系;
 *
 * @author 刘镇 (suninformation@163.com) on 16/4/19 上午10:49
 */
@Target({ElementType.PACKAGE, ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Ignored {
}
