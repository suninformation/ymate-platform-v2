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
package net.ymate.platform.commons.json;

/**
 * JSON属性过滤器接口，用于控制JSON序列化过程中哪些属性被包含或排除。
 * <p>
 * 设计目的：提供一种灵活的方式来过滤JSON序列化过程中的属性，支持自定义过滤逻辑。
 * <p>
 * 使用场景：
 * - 当需要排除敏感属性（如密码、令牌）不被序列化到JSON时
 * - 当需要根据条件动态决定哪些属性被序列化时
 * - 当需要控制不同环境下序列化不同属性时
 *
 * @author 刘镇 (suninformation@163.com) on 2025/6/8 01:05
 * @since 2.1.4
 */
public interface IJsonPropertyFilter {

    /**
     * 判断是否应该过滤指定的属性。
     *
     * @param source 源对象，即包含该属性的对象实例
     * @param name   属性名称，需要判断是否过滤的属性名
     * @return true表示过滤该属性（不序列化到JSON），false表示保留该属性（序列化到JSON）
     */
    boolean filter(Object source, String name);
}
