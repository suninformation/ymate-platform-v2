/*
 * Copyright 2007-2020 the original author or authors.
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
import net.ymate.platform.core.event.IEventListener;
import net.ymate.platform.core.event.annotation.EventListener;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/04/27 16:15
 * @since 2.1.0
 */
public final class EventListenerHandler implements IBeanHandler {

    private final IApplication owner;

    public EventListenerHandler(IApplication owner) {
        this.owner = owner;
    }

    @Override
    public Object handle(Class<?> targetClass) throws Exception {
        if (ClassUtils.isNormalClass(targetClass) && !targetClass.isInterface() && ClassUtils.isInterfaceOf(targetClass, IEventListener.class)) {
            EventListener eventListenerAnn = targetClass.getAnnotation(EventListener.class);
            owner.getEvents().registerListener(eventListenerAnn.mode(), eventListenerAnn.value(), (IEventListener<?>) targetClass.newInstance());
        }
        return null;
    }
}
