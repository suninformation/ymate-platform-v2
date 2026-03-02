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
 * JSON适配器工厂接口，用于创建和管理JSON适配器实例。
 * <p>
 * 设计目的：提供一种灵活的方式来创建JSON适配器，支持通过扩展机制动态切换不同的JSON库实现。
 * <p>
 * 使用场景：
 * - 当需要为应用程序配置特定的JSON库实现时
 * - 当需要在运行时动态切换JSON处理库时
 * - 当需要自定义JSON适配器的创建和初始化逻辑时
 *
 * @author 刘镇 (suninformation@163.com) on 2022/9/2 14:54
 * @since 2.1.2
 */
public interface IJsonAdapterFactory {

    /**
     * 获取JSON适配器实例。
     * <p>
     * 该方法负责创建和初始化JSON适配器实例，具体实现类可以根据配置或环境选择不同的JSON库实现。
     *
     * @return JSON适配器实例，不能为空
     */
    IJsonAdapter getJsonAdapter();
}
