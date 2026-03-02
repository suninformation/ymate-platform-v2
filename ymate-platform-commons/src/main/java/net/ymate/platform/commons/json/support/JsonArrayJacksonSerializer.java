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
import net.ymate.platform.commons.json.IJsonArrayWrapper;
import net.ymate.platform.commons.json.JsonWrapper;
import net.ymate.platform.commons.json.impl.JacksonAdapter;

import java.io.IOException;

/**
 * Jackson JSON数组序列化器实现，提供JSON数组的序列化和反序列化功能。
 * <p>
 * 设计目的：为Jackson提供JSON数组类型的序列化和反序列化支持。
 * <p>
 * 使用场景：
 * - 当需要使用Jackson序列化IJsonArrayWrapper类型时
 * - 当需要使用Jackson反序列化JSON数组到IJsonArrayWrapper类型时
 *
 * @author 刘镇 (suninformation@163.com) on 2021/12/27 2:49 上午
 * @since 2.1.0
 */
public class JsonArrayJacksonSerializer {

    /**
     * Jackson适配器实例，用于JSON序列化和反序列化操作。
     */
    private static final IJsonAdapter adapter = new JacksonAdapter();

    /**
     * JSON数组Jackson序列化器，用于将IJsonArrayWrapper类型序列化为JSON。
     */
    public static class Serializer extends JsonSerializer<IJsonArrayWrapper> {
        /**
         * 将IJsonArrayWrapper类型序列化为JSON。
         *
         * @param jsonArrayWrapper   要序列化的JSON数组包装器
         * @param jsonGenerator      Jackson JSON生成器
         * @param serializerProvider 序列化提供者
         * @throws IOException 当序列化过程中发生IO异常时抛出
         */
        @Override
        public void serialize(IJsonArrayWrapper jsonArrayWrapper, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            if (jsonArrayWrapper == null) {
                jsonGenerator.writeNull();
            } else {
                jsonGenerator.writeObject(JsonWrapper.unwrap(jsonArrayWrapper));
            }
        }
    }

    /**
     * JSON数组Jackson反序列化器，用于将JSON反序列化为IJsonArrayWrapper类型。
     */
    public static class Deserializer extends JsonDeserializer<IJsonArrayWrapper> {
        /**
         * 将JSON反序列化为IJsonArrayWrapper类型。
         *
         * @param jsonParser             Jackson JSON解析器
         * @param deserializationContext 反序列化上下文
         * @return 反序列化后的IJsonArrayWrapper实例，若解析失败则返回null
         * @throws IOException      当反序列化过程中发生IO异常时抛出
         * @throws JacksonException 当反序列化过程中发生Jackson异常时抛出
         */
        @Override
        public IJsonArrayWrapper deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
            JsonNode node = jsonParser.getCodec().readTree(jsonParser);
            if (node.elements().hasNext()) {
                JsonWrapper jsonWrapper = adapter.toJson(node);
                if (jsonWrapper != null) {
                    return jsonWrapper.getAsJsonArray();
                }
            }
            return null;
        }
    }
}
