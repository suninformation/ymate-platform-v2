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
import net.ymate.platform.plugin.*;
import net.ymate.platform.plugin.annotation.Plugin;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Arrays;

/**
 * 插件对象处理器
 *
 * @author 刘镇 (suninformation@163.com) on 15/3/22 下午10:05
 */
public class PluginHandler implements IBeanHandler {

    private static final Log LOG = LogFactory.getLog(PluginHandler.class);

    private final IPluginFactory pluginFactory;

    public PluginHandler(IPluginFactory pluginFactory) {
        this.pluginFactory = pluginFactory;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object handle(Class<?> targetClass) {
        if (ClassUtils.isNormalClass(targetClass) && ClassUtils.isInterfaceOf(targetClass, IPlugin.class)) {
            Plugin pluginAnn = targetClass.getAnnotation(Plugin.class);
            //
            String pluginId = StringUtils.defaultIfBlank(pluginAnn.id(), targetClass.getName());
            if (!StringUtils.equalsIgnoreCase(pluginFactory.getOwner().getParam(IPluginConfig.PARAMS_PLUGIN_DISABLED_PREFIX + pluginId), IPluginConfig.DISABLED)) {
                PluginMeta pluginMeta = new PluginMeta(targetClass.getClassLoader());
                pluginMeta.setId(pluginId);
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
            } else if (pluginFactory.getOwner().isDevEnv() && LOG.isWarnEnabled()) {
                LOG.warn(String.format("Plugin class [%s:%s] has been disabled.", pluginId, targetClass.getName()));
            }
        }
        return null;
    }
}
