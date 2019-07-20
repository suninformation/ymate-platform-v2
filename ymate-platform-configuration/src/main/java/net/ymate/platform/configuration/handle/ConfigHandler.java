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
import net.ymate.platform.core.beans.BeanMeta;
import net.ymate.platform.core.beans.IBeanHandler;
import net.ymate.platform.core.configuration.IConfig;
import net.ymate.platform.core.configuration.IConfiguration;

/**
 * 配置对象处理器
 *
 * @author 刘镇 (suninformation@163.com) on 15/3/13 下午1:44
 */
public class ConfigHandler implements IBeanHandler {

    private final IConfig owner;

    public ConfigHandler(IConfig owner) {
        this.owner = owner;
    }

    @Override
    public Object handle(Class<?> targetClass) throws Exception {
        if (ClassUtils.isNormalClass(targetClass) && !targetClass.isInterface() && ClassUtils.isInterfaceOf(targetClass, IConfiguration.class)) {
            return BeanMeta.create(targetClass, true, target -> owner.fillCfg((IConfiguration) target));
        }
        return null;
    }
}
