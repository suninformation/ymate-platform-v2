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
package net.ymate.platform.core.handle;

import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.beans.IBeanHandler;
import net.ymate.platform.core.event.IEventRegister;

/**
 * 事件注册器对象处理器
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/20 上午12:11
 */
public final class EventRegisterHandler implements IBeanHandler {

    private final IApplication owner;

    public EventRegisterHandler(IApplication owner) {
        this.owner = owner;
    }

    @Override
    public Object handle(Class<?> targetClass) throws Exception {
        if (ClassUtils.isNormalClass(targetClass) && !targetClass.isInterface() && ClassUtils.isInterfaceOf(targetClass, IEventRegister.class)) {
            ((IEventRegister) targetClass.newInstance()).register(owner.getEvents());
        }
        return null;
    }
}
