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
package net.ymate.platform.plugin.handle;

import net.ymate.platform.core.beans.BeanMeta;
import net.ymate.platform.core.beans.IBeanHandler;
import net.ymate.platform.core.handle.InterceptorHandler;
import net.ymate.platform.plugin.IPluginFactory;
import net.ymate.platform.plugin.PluginClassLoader;

/**
 * @author 刘镇 (suninformation@163.com) on 2018-11-29 10:28
 */
public class PluginInterceptorHandler implements IBeanHandler {

    private final InterceptorHandler interceptorHandler = new InterceptorHandler();

    private final IPluginFactory pluginFactory;

    public PluginInterceptorHandler(IPluginFactory pluginFactory) {
        this.pluginFactory = pluginFactory;
    }

    @Override
    public Object handle(Class<?> targetClass) {
        if (targetClass.getClassLoader() instanceof PluginClassLoader) {
            pluginFactory.getOwner().getBeanFactory().registerBean((BeanMeta) interceptorHandler.handle(targetClass));
        }
        return null;
    }
}
