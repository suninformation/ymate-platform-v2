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
package net.ymate.platform.configuration.handle;

import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.configuration.Cfgs;
import net.ymate.platform.configuration.annotation.Configuration;
import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.beans.BeanMeta;
import net.ymate.platform.core.beans.IBeanHandler;
import net.ymate.platform.core.configuration.IConfiguration;
import net.ymate.platform.plugin.PluginClassLoader;
import net.ymate.platform.plugin.annotation.Handler;

/**
 * 插件配置文件加载路径注解 (from ymate-framework-addons)
 *
 * @author 刘镇 (suninformation@163.com) on 15/8/15 上午10:27
 */
@Handler(Configuration.class)
public class PluginConfigBeanHandler implements IBeanHandler {

    private final IApplication owner;

    public PluginConfigBeanHandler(IApplication owner) {
        this.owner = owner;
    }

    @Override
    public Object handle(Class<?> targetClass) {
        if (targetClass.getClassLoader() instanceof PluginClassLoader) {
            if (ClassUtils.isNormalClass(targetClass) && !targetClass.isInterface() && ClassUtils.isInterfaceOf(targetClass, IConfiguration.class)) {
                BeanMeta beanMeta = BeanMeta.create(targetClass, true, target -> Cfgs.get().fillCfg((IConfiguration) target));
                owner.getBeanFactory().registerBean(beanMeta);
            }
        }
        return null;
    }
}
