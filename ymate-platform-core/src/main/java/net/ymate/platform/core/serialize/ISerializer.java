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
package net.ymate.platform.core.serialize;

import net.ymate.platform.core.serialize.impl.DefaultSerializer;
import net.ymate.platform.core.serialize.impl.JSONSerializer;
import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 刘镇 (suninformation@163.com) on 2017/10/3 下午3:52
 * @version 1.0
 */
public interface ISerializer {

    String DEFAULT_CHARSET = "UTF-8";

    String getContentType();

    byte[] serialize(Object object) throws Exception;

    <T> T deserialize(byte[] bytes, Class<T> clazz) throws Exception;

    class SerializerManager {

        private static final Map<String, ISerializer> __serializers = new ConcurrentHashMap<String, ISerializer>();

        static {
            __serializers.put("default", new DefaultSerializer());
            __serializers.put("json", new JSONSerializer());
        }

        public static void registerSerializer(String name, Class<? extends ISerializer> targetClass) throws Exception {
            String _key = StringUtils.defaultIfBlank(name, targetClass.getName()).toLowerCase();
            if (!__serializers.containsKey(_key)) {
                __serializers.put(_key, targetClass.newInstance());
            }
        }

        public static ISerializer getDefaultSerializer() {
            return getSerializer("default");
        }

        public static ISerializer getJSONSerializer() {
            return getSerializer("json");
        }

        public static ISerializer getSerializer(String name) {
            if (StringUtils.isBlank(name)) {
                return null;
            }
            return __serializers.get(name.toLowerCase());
        }
    }
}
