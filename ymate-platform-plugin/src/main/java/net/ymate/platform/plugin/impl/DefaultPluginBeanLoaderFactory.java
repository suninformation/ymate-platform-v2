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
package net.ymate.platform.plugin.impl;

import net.ymate.platform.core.beans.IBeanLoader;
import net.ymate.platform.core.beans.impl.DefaultBeanLoader;
import net.ymate.platform.plugin.IPluginBeanLoaderFactory;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-06-22 02:10
 * @since 2.1.0
 */
public class DefaultPluginBeanLoaderFactory implements IPluginBeanLoaderFactory {

    private final IBeanLoader pluginBeanLoader = new DefaultBeanLoader();

    @Override
    public IBeanLoader getPluginBeanLoader() {
        return pluginBeanLoader;
    }
}
