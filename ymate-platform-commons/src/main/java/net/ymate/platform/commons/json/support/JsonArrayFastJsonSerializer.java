/*
 * Copyright 2007-present the original author or authors.
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
package net.ymate.platform.commons.json.support;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import net.ymate.platform.commons.json.IJsonAdapter;
import net.ymate.platform.commons.json.JsonWrapper;
import net.ymate.platform.commons.json.impl.FastJsonAdapter;

import java.lang.reflect.Type;

/**
 * FastJSON JSON数组序列化器实现，提供JSON数组的序列化和反序列化功能。
 * <p>
 * 设计目的：为FastJSON提供JSON数组类型的序列化和反序列化支持。
 * <p>
 * 使用场景：
 * - 当需要使用FastJSON序列化IJsonArrayWrapper类型时
 * - 当需要使用FastJSON反序列化JSON数组到IJsonArrayWrapper类型时
 *
 * @author 刘镇 (suninformation@163.com) on 2021/12/25 7:19 PM
 * @since 2.1.0
 */
public class JsonArrayFastJsonSerializer {

    /**
     * 私有构造函数，防止外部实例化。
     */
    private JsonArrayFastJsonSerializer() {
    }

    /**
     * JSON数组FastJSON序列化器，用于将IJsonArrayWrapper类型序列化为JSON。
     */
    public static class Serializer extends AbstractFastJsonSerializer {
    }

    /**
     * JSON数组FastJSON反序列化器，用于将JSON反序列化为IJsonArrayWrapper类型。
     */
    public static class Deserializer implements ObjectDeserializer {

        /**
         * FastJSON适配器实例，用于JSON反序列化操作。
         */
        private final IJsonAdapter adapter = new FastJsonAdapter();

        /**
         * 将JSON反序列化为IJsonArrayWrapper类型。
         *
         * @param parser    FastJSON解析器实例
         * @param type      目标类型
         * @param fieldName 字段名称
         * @param <T>       目标类型泛型
         * @return 反序列化后的IJsonArrayWrapper实例，若解析失败则返回null
         */
        @Override
        @SuppressWarnings("unchecked")
        public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
            JsonWrapper jsonWrapper = adapter.toJson(parser.parse());
            if (jsonWrapper == null) {
                return null;
            }
            return (T) jsonWrapper.getAsJsonArray();
        }
    }
}
