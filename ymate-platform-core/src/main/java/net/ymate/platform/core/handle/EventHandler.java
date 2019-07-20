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
import net.ymate.platform.core.event.IEvent;

/**
 * @author 刘镇 (suninformation@163.com) on 2017/11/14 上午2:17
 */
public final class EventHandler implements IBeanHandler {

    private final IApplication owner;

    public EventHandler(IApplication owner) {
        this.owner = owner;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object handle(Class<?> targetClass) {
        if (ClassUtils.isNormalClass(targetClass) && !targetClass.isInterface() && ClassUtils.isInterfaceOf(targetClass, IEvent.class)) {
            owner.getEvents().registerEvent((Class<? extends IEvent>) targetClass);
        }
        return null;
    }
}
