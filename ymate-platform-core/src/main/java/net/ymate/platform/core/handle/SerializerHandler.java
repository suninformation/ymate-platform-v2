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
import net.ymate.platform.core.beans.IBeanHandler;
import net.ymate.platform.core.serialize.ISerializer;
import net.ymate.platform.core.serialize.SerializerManager;
import net.ymate.platform.core.serialize.annotation.Serializer;

/**
 * @author 刘镇 (suninformation@163.com) on 2017/10/10 上午11:46
 */
public final class SerializerHandler implements IBeanHandler {

    @Override
    @SuppressWarnings("unchecked")
    public Object handle(Class<?> targetClass) throws Exception {
        if (!targetClass.isInterface() && ClassUtils.isInterfaceOf(targetClass, ISerializer.class)) {
            SerializerManager.registerSerializer(targetClass.getAnnotation(Serializer.class).value(), (Class<? extends ISerializer>) targetClass);
        }
        return null;
    }
}
