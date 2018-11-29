/*
 * Copyright 2007-2017 the original author or authors.
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

import net.ymate.platform.configuration.IConfig;
import net.ymate.platform.configuration.IConfiguration;
import net.ymate.platform.core.beans.BeanMeta;
import net.ymate.platform.core.beans.IBeanHandler;
import net.ymate.platform.core.util.ClassUtils;

/**
 * 配置对象处理器
 *
 * @author 刘镇 (suninformation@163.com) on 15/3/13 下午1:44
 * @version 1.0
 */
public class ConfigHandler implements IBeanHandler {

    private final IConfig __owner;

    public ConfigHandler(IConfig owner) {
        __owner = owner;
        __owner.getOwner().registerExcludedClass(IConfiguration.class);
    }

    @Override
    public Object handle(Class<?> targetClass) throws Exception {
        if (ClassUtils.isInterfaceOf(targetClass, IConfiguration.class)) {
            BeanMeta _beanMeta = BeanMeta.create(targetClass, true);
            _beanMeta.setInitializer(new BeanMeta.IInitializer() {
                @Override
                public void init(Object target) throws Exception {
                    __owner.fillCfg((IConfiguration) target);
                }
            });
            return _beanMeta;
        }
        return null;
    }
}
