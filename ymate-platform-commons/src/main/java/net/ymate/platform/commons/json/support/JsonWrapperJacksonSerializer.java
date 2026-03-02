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
import net.ymate.platform.commons.json.JsonWrapper;
import net.ymate.platform.commons.json.impl.JacksonAdapter;

import java.io.IOException;

/**
 * Jackson JsonWrapper序列化器实现，提供JsonWrapper的序列化和反序列化功能。
 * <p>
 * 设计目的：为Jackson提供JsonWrapper类型的序列化和反序列化支持。
 * <p>
 * 使用场景：
 * - 当需要使用Jackson序列化JsonWrapper类型时
 * - 当需要使用Jackson反序列化JSON到JsonWrapper类型时
 *
 * @author 刘镇 (suninformation@163.com) on 2021/12/27 2:49 上午
 * @since 2.1.0
 */
public class JsonWrapperJacksonSerializer {

    /**
     * Jackson适配器实例，用于JSON序列化和反序列化操作。
     */
    private static final IJsonAdapter adapter = new JacksonAdapter();

    /**
     * JsonWrapper Jackson序列化器，用于将JsonWrapper类型序列化为JSON。
     */
    public static class Serializer extends JsonSerializer<JsonWrapper> {
        /**
         * 将JsonWrapper类型序列化为JSON。
         *
         * @param jsonWrapper        要序列化的JsonWrapper实例
         * @param jsonGenerator      Jackson JSON生成器
         * @param serializerProvider 序列化提供者
         * @throws IOException 当序列化过程中发生IO异常时抛出
         */
        @Override
        public void serialize(JsonWrapper jsonWrapper, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            if (jsonWrapper == null) {
                jsonGenerator.writeNull();
            } else {
                jsonGenerator.writeObject(JsonWrapper.unwrap(jsonWrapper));
            }
        }
    }

    /**
     * JsonWrapper Jackson反序列化器，用于将JSON反序列化为JsonWrapper类型。
     */
    public static class Deserializer extends JsonDeserializer<JsonWrapper> {
        /**
         * 将JSON反序列化为JsonWrapper类型。
         *
         * @param jsonParser             Jackson JSON解析器
         * @param deserializationContext 反序列化上下文
         * @return 反序列化后的JsonWrapper实例，若解析失败则返回null
         * @throws IOException      当反序列化过程中发生IO异常时抛出
         * @throws JacksonException 当反序列化过程中发生Jackson异常时抛出
         */
        @Override
        public JsonWrapper deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
            JsonNode node = jsonParser.getCodec().readTree(jsonParser);
            if (!node.elements().hasNext()) {
                return null;
            }
            return adapter.toJson(node);
        }
    }
}
