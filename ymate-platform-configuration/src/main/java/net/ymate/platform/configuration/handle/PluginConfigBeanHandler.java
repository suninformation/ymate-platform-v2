/*
 * Copyright 2007-2018 the original author or authors.
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

import net.ymate.platform.configuration.Cfgs;
import net.ymate.platform.configuration.IConfiguration;
import net.ymate.platform.configuration.annotation.Configuration;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.beans.BeanMeta;
import net.ymate.platform.core.beans.IBeanHandler;
import net.ymate.platform.core.util.ClassUtils;
import net.ymate.platform.plugin.PluginClassLoader;
import net.ymate.platform.plugin.annotation.Handler;

/**
 * 插件配置文件加载路径注解 (from ymate-framework-addons)
 *
 * @author 刘镇 (suninformation@163.com) on 15/8/15 上午10:27
 * @version 1.0
 */
@Handler(Configuration.class)
public class PluginConfigBeanHandler implements IBeanHandler {

    private YMP __owner;

    public PluginConfigBeanHandler(YMP owner) {
        __owner = owner;
    }

    @Override
    public Object handle(Class<?> targetClass) throws Exception {
        if (targetClass.getClassLoader() instanceof PluginClassLoader) {
            if (ClassUtils.isInterfaceOf(targetClass, IConfiguration.class)) {
                BeanMeta _beanMeta = BeanMeta.create(targetClass, true);
                _beanMeta.setInitializer(new BeanMeta.IInitializer() {
                    @Override
                    public void init(Object target) throws Exception {
                        __owner.getModule(Cfgs.class).fillCfg((IConfiguration) target);
                    }
                });
                __owner.registerBean(_beanMeta);
            }
        }
        return null;
    }
}
