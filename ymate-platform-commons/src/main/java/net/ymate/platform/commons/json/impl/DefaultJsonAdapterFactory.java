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
package net.ymate.platform.commons.json.impl;

import net.ymate.platform.commons.json.IJsonAdapter;
import net.ymate.platform.commons.json.IJsonAdapterFactory;
import net.ymate.platform.commons.util.ClassUtils;

/**
 * 默认JSON适配器工厂实现，用于创建和管理JSON适配器实例。
 * <p>
 * 设计目的：提供默认的JSON适配器创建逻辑，通过扩展加载机制自动检测并加载可用的JSON适配器实现。
 * <p>
 * 使用场景：
 * - 当应用程序没有指定自定义JSON适配器工厂时，作为默认实现
 * - 当需要通过扩展机制自动加载JSON适配器时
 * - 当需要简化JSON适配器的创建和管理时
 *
 * @author 刘镇 (suninformation@163.com) on 2022/9/2 14:59
 * @since 2.1.2
 */
public class DefaultJsonAdapterFactory implements IJsonAdapterFactory {

    private IJsonAdapter jsonAdapter;

    /**
     * 构造函数，用于初始化默认JSON适配器工厂。
     * <p>
     * 该构造函数通过扩展加载机制自动检测并加载可用的JSON适配器实现，按照扩展顺序尝试创建适配器实例，
     * 直到成功创建第一个可用的适配器或所有扩展都尝试完毕。
     */
    public DefaultJsonAdapterFactory() throws Exception {
        ClassUtils.ExtensionLoader<IJsonAdapter> extensionLoader = ClassUtils.getExtensionLoader(IJsonAdapter.class);
        for (Class<IJsonAdapter> adapterClass : extensionLoader.getExtensionClasses()) {
            try {
                jsonAdapter = ClassUtils.impl(adapterClass, IJsonAdapter.class);
                if (jsonAdapter != null) {
                    break;
                }
            } catch (NoClassDefFoundError | Exception ignored) {
            }
        }
    }

    /**
     * 获取JSON适配器实例。
     * <p>
     * 该方法返回通过扩展加载机制创建的JSON适配器实例，如果没有找到可用的适配器则返回null。
     *
     * @return JSON适配器实例，可能为null
     */
    @Override
    public IJsonAdapter getJsonAdapter() {
        return jsonAdapter;
    }
}
