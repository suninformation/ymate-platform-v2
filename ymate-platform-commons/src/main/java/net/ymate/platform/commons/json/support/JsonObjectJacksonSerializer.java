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

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import net.ymate.platform.commons.json.IJsonAdapter;
import net.ymate.platform.commons.json.IJsonObjectWrapper;
import net.ymate.platform.commons.json.JsonWrapper;
import net.ymate.platform.commons.json.impl.JacksonAdapter;

import java.io.IOException;

/**
 * Jackson JSON对象序列化器实现，提供JSON对象的序列化和反序列化功能。
 * <p>
 * 设计目的：为Jackson提供JSON对象类型的序列化和反序列化支持。
 * <p>
 * 使用场景：
 * - 当需要使用Jackson序列化IJsonObjectWrapper类型时
 * - 当需要使用Jackson反序列化JSON对象到IJsonObjectWrapper类型时
 *
 * @author 刘镇 (suninformation@163.com) on 2021/12/27 2:49 上午
 * @since 2.1.0
 */
public class JsonObjectJacksonSerializer {

    /**
     * Jackson适配器实例，用于JSON序列化和反序列化操作。
     */
    private static final IJsonAdapter adapter = new JacksonAdapter();

    /**
     * JSON对象Jackson序列化器，用于将IJsonObjectWrapper类型序列化为JSON。
     */
    public static class Serializer extends JsonSerializer<IJsonObjectWrapper> {
        /**
         * 将IJsonObjectWrapper类型序列化为JSON。
         *
         * @param jsonObjectWrapper  要序列化的JSON对象包装器
         * @param jsonGenerator      Jackson JSON生成器
         * @param serializerProvider 序列化提供者
         * @throws IOException 当序列化过程中发生IO异常时抛出
         */
        @Override
        public void serialize(IJsonObjectWrapper jsonObjectWrapper, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            if (jsonObjectWrapper == null) {
                jsonGenerator.writeNull();
            } else {
                jsonGenerator.writeObject(JsonWrapper.unwrap(jsonObjectWrapper));
            }
        }
    }

    /**
     * JSON对象Jackson反序列化器，用于将JSON反序列化为IJsonObjectWrapper类型。
     */
    public static class Deserializer extends JsonDeserializer<IJsonObjectWrapper> {
        /**
         * 将JSON反序列化为IJsonObjectWrapper类型。
         *
         * @param jsonParser             Jackson JSON解析器
         * @param deserializationContext 反序列化上下文
         * @return 反序列化后的IJsonObjectWrapper实例，若解析失败则返回null
         * @throws IOException      当反序列化过程中发生IO异常时抛出
         * @throws JacksonException 当反序列化过程中发生Jackson异常时抛出
         */
        @Override
        public IJsonObjectWrapper deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
            JsonNode node = jsonParser.getCodec().readTree(jsonParser);
            if (node.elements().hasNext()) {
                JsonWrapper jsonWrapper = adapter.toJson(node);
                if (jsonWrapper != null) {
                    return jsonWrapper.getAsJsonObject();
                }
            }
            return null;
        }
    }
}
