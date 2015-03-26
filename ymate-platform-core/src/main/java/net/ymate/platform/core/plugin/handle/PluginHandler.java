/*
 * Copyright 2007-2107 the original author or authors.
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
package net.ymate.platform.core.plugin.handle;

import net.ymate.platform.core.beans.IBeanHandler;
import net.ymate.platform.core.plugin.*;
import net.ymate.platform.core.plugin.annotation.Plugin;
import net.ymate.platform.core.util.ClassUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;

/**
 * 插件对象处理器
 *
 * @author 刘镇 (suninformation@163.com) on 15/3/22 下午10:05
 * @version 1.0
 */
public class PluginHandler implements IBeanHandler {

    public PluginHandler(IPluginFactory pluginFactory) {
        pluginFactory.addExcludedInterfaceClass(IPlugin.class);
        pluginFactory.addExcludedInterfaceClass(IPluginConfig.class);
        pluginFactory.addExcludedInterfaceClass(IPluginContext.class);
        pluginFactory.addExcludedInterfaceClass(IPluginEventListener.class);
        pluginFactory.addExcludedInterfaceClass(IPluginExtend.class);
        pluginFactory.addExcludedInterfaceClass(IPluginExtendParser.class);
        pluginFactory.addExcludedInterfaceClass(IPluginFactory.class);
        pluginFactory.addExcludedInterfaceClass(IPluginParser.class);
    }

    @SuppressWarnings("unchecked")
    public Object handle(Class<?> targetClass) throws Exception {
        if (targetClass.isAnnotationPresent(Plugin.class) && ClassUtils.isInterfaceOf(targetClass, IPlugin.class)) {
            Plugin _plugin = targetClass.getAnnotation(Plugin.class);
            PluginMeta _meta = new PluginMeta(targetClass.getClassLoader());
            //
            _meta.setId(StringUtils.defaultIfBlank(_plugin.id(), DigestUtils.md5Hex(targetClass.getName())));
            _meta.setName(StringUtils.defaultIfBlank(_plugin.name(), targetClass.getSimpleName()));
            _meta.setAlias(_plugin.alias());
            _meta.setInitClass((Class<? extends IPlugin>) targetClass);
            _meta.setVersion(_plugin.version());
            _meta.setAuthor(_plugin.version());
            _meta.setEmail(_plugin.email());
            _meta.setAutomatic(_plugin.automatic());
            _meta.setDescription(_plugin.description());
            //
            return _meta;
        }
        return null;
    }
}
