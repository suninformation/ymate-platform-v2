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

import net.ymate.platform.core.beans.IBeanFactory;
import net.ymate.platform.core.beans.IBeanInjector;
import net.ymate.platform.plugin.IPluginFactory;
import net.ymate.platform.plugin.annotation.PluginRefer;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/1/7 上午1:38
 */
public class PluginReferInjector implements IBeanInjector {

    private final IPluginFactory owner;

    public PluginReferInjector(IPluginFactory owner) {
        this.owner = owner;
    }

    @Override
    public Object inject(IBeanFactory beanFactory, Annotation annotation, Class<?> targetClass, Field field, Object originInject) {
        Object pluginObj = null;
        if (StringUtils.isNotBlank(((PluginRefer) annotation).value())) {
            pluginObj = owner.getPlugin(((PluginRefer) annotation).value());
        }
        if (pluginObj == null) {
            pluginObj = owner.getPlugin(field.getType());
        }
        return pluginObj;
    }
}
