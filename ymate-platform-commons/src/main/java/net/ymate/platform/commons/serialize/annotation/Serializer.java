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
package net.ymate.platform.commons.serialize.annotation;

import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.*;

/**
 * 序列化器注解，用于标记和命名序列化器实现类。
 * <p>
 * 该注解应用于实现 ISerializer 接口的类，用于指定序列化器的名称。
 * 当通过 SPI 机制加载序列化器时，会使用该注解的值作为序列化器的名称。
 * 如果未指定名称，则使用类的全限定名作为序列化器名称。
 * </p>
 * <p>
 * 设计目的：
 * <ul>
 *   <li>为序列化器提供自定义名称</li>
 *   <li>便于通过名称查找和获取序列化器</li>
 *   <li>支持 SPI 机制自动加载序列化器</li>
 * </ul>
 * </p>
 * <p>
 * 使用场景：
 * <ul>
 *   <li>标记自定义序列化器实现</li>
 *   <li>为序列化器指定易于记忆的名称</li>
 *   <li>通过 SPI 机制注册序列化器</li>
 * </ul>
 * </p>
 * <p>
 * 注意事项：
 * <ul>
 *   <li>注解的值不区分大小写</li>
 *   <li>如果未指定名称，将使用类的全限定名</li>
 *   <li>名称建议使用小写字母和连字符</li>
 * </ul>
 * </p>
 *
 * @author 刘镇 (suninformation@163.com) on 2017/10/10 上午11:15
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Serializer {

    /**
     * 序列化器名称。
     * <p>
     * 用于标识和查找序列化器，如果不指定则使用类的全限定名。
     * </p>
     *
     * @return 序列化器名称，默认为空字符串
     */
    String value() default StringUtils.EMPTY;
}
