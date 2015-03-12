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
package net.ymate.platform.core.handle;

import net.ymate.platform.core.YMP;
import net.ymate.platform.core.beans.IBeanHandler;
import net.ymate.platform.core.beans.annotation.Handler;
import net.ymate.platform.core.module.IModule;
import net.ymate.platform.core.module.annotation.Module;
import net.ymate.platform.core.util.ClassUtils;

/**
 * 模块对象处理器
 *
 * @author 刘镇 (suninformation@163.com) on 15/3/12 上午11:59
 * @version 1.0
 */
@Handler
public class ModuleHandler implements IBeanHandler {

    private YMP __owner;

    public void init(Object owner) throws Exception {
        __owner = (YMP) owner;
        __owner.getBeanFactory().registerHandler(Module.class, this);
        __owner.getBeanFactory().registerExcludedClass(IModule.class);
    }

    public Object handle(Class<?> targetClass) throws Exception {
        if (ClassUtils.isInterfaceOf(targetClass, IModule.class)) {
            IModule _module = (IModule) targetClass.newInstance();
            __owner.registerModule(_module);
            return _module;
        }
        return null;
    }
}
