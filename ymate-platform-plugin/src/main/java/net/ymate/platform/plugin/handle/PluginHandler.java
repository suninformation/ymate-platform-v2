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

import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.core.beans.IBeanHandler;
import net.ymate.platform.plugin.IPlugin;
import net.ymate.platform.plugin.PluginClassLoader;
import net.ymate.platform.plugin.PluginMeta;
import net.ymate.platform.plugin.annotation.Plugin;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * 插件对象处理器
 *
 * @author 刘镇 (suninformation@163.com) on 15/3/22 下午10:05
 */
public class PluginHandler implements IBeanHandler {

    public PluginHandler() {
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object handle(Class<?> targetClass) {
        if (ClassUtils.isNormalClass(targetClass) && ClassUtils.isInterfaceOf(targetClass, IPlugin.class)) {
            Plugin pluginAnn = targetClass.getAnnotation(Plugin.class);
            //
            PluginMeta pluginMeta = new PluginMeta(targetClass.getClassLoader());
            pluginMeta.setId(StringUtils.defaultIfBlank(pluginAnn.id(), DigestUtils.md5Hex(targetClass.getName())));
            pluginMeta.setName(StringUtils.defaultIfBlank(pluginAnn.name(), targetClass.getSimpleName()));
            pluginMeta.setAlias(Arrays.asList(pluginAnn.alias()));
            pluginMeta.setInitClass((Class<? extends IPlugin>) targetClass);
            pluginMeta.setVersion(pluginAnn.version());
            pluginMeta.setAuthor(pluginAnn.author());
            pluginMeta.setEmail(pluginAnn.email());
            pluginMeta.setAutomatic(pluginAnn.automatic());
            pluginMeta.setDescription(pluginAnn.description());
            //
            if (targetClass.getClassLoader() instanceof PluginClassLoader) {
                pluginMeta.setPath(((PluginClassLoader) targetClass.getClassLoader()).getPluginHome());
            }
            return pluginMeta;
        }
        return null;
    }
}
