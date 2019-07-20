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
package net.ymate.platform.core.serialize;

import net.ymate.platform.commons.ReentrantLockHelper;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.serialize.annotation.Serializer;
import net.ymate.platform.core.serialize.impl.DefaultSerializer;
import net.ymate.platform.core.serialize.impl.JSONSerializer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 刘镇 (suninformation@163.com) on 2017/10/3 下午3:52
 */
public class SerializerManager {

    private static final Log LOG = LogFactory.getLog(SerializerManager.class);

    private static final Map<String, ISerializer> SERIALIZERS = new ConcurrentHashMap<>();

    static {
        SERIALIZERS.put(DefaultSerializer.NAME, new DefaultSerializer());
        SERIALIZERS.put(JSONSerializer.NAME, new JSONSerializer());
        //
        try {
            ClassUtils.ExtensionLoader<ISerializer> extensionLoader = ClassUtils.getExtensionLoader(ISerializer.class);
            for (Class<ISerializer> serializerClass : extensionLoader.getExtensionClasses()) {
                Serializer serializerAnn = serializerClass.getAnnotation(Serializer.class);
                if (serializerAnn != null) {
                    registerSerializer(serializerAnn.value(), serializerClass);
                }
            }
        } catch (Exception e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
            }
        }
    }

    public static void registerSerializer(Class<? extends ISerializer> targetClass) throws Exception {
        registerSerializer(null, targetClass);
    }

    public static void registerSerializer(String name, Class<? extends ISerializer> targetClass) throws Exception {
        String key = StringUtils.defaultIfBlank(name, targetClass.getName()).toLowerCase();
        ReentrantLockHelper.putIfAbsentAsync(SERIALIZERS, key, targetClass::newInstance);
    }

    public static ISerializer getDefaultSerializer() {
        return getSerializer(DefaultSerializer.NAME);
    }

    public static ISerializer getJsonSerializer() {
        return getSerializer(JSONSerializer.NAME);
    }

    public static ISerializer getSerializer(Class<? extends ISerializer> clazz) {
        if (clazz == null) {
            return null;
        }
        return SERIALIZERS.get(clazz.getName().toLowerCase());
    }

    public static ISerializer getSerializer(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        return SERIALIZERS.get(name.toLowerCase());
    }
}
