/*
 * Copyright 2007-2021 the original author or authors.
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
 * @author 刘镇 (suninformation@163.com) on 2021/12/27 2:49 上午
 * @since 2.1.0
 */
public class JsonArrayJacksonSerializer {

    private static final IJsonAdapter adapter = new JacksonAdapter();

    public static class Serializer extends JsonSerializer<IJsonArrayWrapper> {
        @Override
        public void serialize(IJsonArrayWrapper jsonArrayWrapper, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            if (jsonArrayWrapper == null) {
                jsonGenerator.writeNull();
            } else {
                jsonGenerator.writeObject(JsonWrapper.unwrap(jsonArrayWrapper));
            }
        }
    }

    public static class Deserializer extends JsonDeserializer<IJsonArrayWrapper> {
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
