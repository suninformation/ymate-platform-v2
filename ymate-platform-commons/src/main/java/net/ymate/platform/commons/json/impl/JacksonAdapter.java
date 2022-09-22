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
package net.ymate.platform.commons.json.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.ymate.platform.commons.json.IJsonAdapter;
import net.ymate.platform.commons.json.IJsonArrayWrapper;
import net.ymate.platform.commons.json.IJsonObjectWrapper;
import net.ymate.platform.commons.json.JsonWrapper;
import net.ymate.platform.commons.json.support.JsonArrayJacksonSerializer;
import net.ymate.platform.commons.json.support.JsonObjectJacksonSerializer;
import net.ymate.platform.commons.json.support.JsonWrapperJacksonSerializer;
import net.ymate.platform.commons.util.RuntimeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/6/11 3:40 下午
 * @since 2.1.0
 */
public class JacksonAdapter implements IJsonAdapter {

    private static final Log LOG = LogFactory.getLog(JacksonAdapter.class);

    public static final ObjectMapper OBJECT_MAPPER = createObjectMapper();

    private static ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = JsonMapper.builder()
                .enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS,
                        JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER,
                        JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES,
                        JsonReadFeature.ALLOW_SINGLE_QUOTES)
                .enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
                .enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .build();
        SimpleModule module = new SimpleModule()
                .addSerializer(JsonWrapper.class, new JsonWrapperJacksonSerializer.Serializer())
                .addSerializer(IJsonObjectWrapper.class, new JsonObjectJacksonSerializer.Serializer())
                .addSerializer(IJsonArrayWrapper.class, new JsonArrayJacksonSerializer.Serializer());
        module.addDeserializer(JsonWrapper.class, new JsonWrapperJacksonSerializer.Deserializer())
                .addDeserializer(IJsonObjectWrapper.class, new JsonObjectJacksonSerializer.Deserializer())
                .addDeserializer(IJsonArrayWrapper.class, new JsonArrayJacksonSerializer.Deserializer());
        return objectMapper.registerModule(module);
    }

    public static JsonNode toJsonNode(Object value) {
        return OBJECT_MAPPER.valueToTree(JsonWrapper.unwrap(value));
    }

    public static ObjectNode toObjectNode(Map<?, ?> value) {
        ObjectNode objectNode = OBJECT_MAPPER.createObjectNode();
        value.forEach((key, v) -> objectNode.set(String.valueOf(key), toJsonNode(v)));
        return objectNode;
    }

    public static ArrayNode toArrayNode(Collection<?> value) {
        ArrayNode arrayNode = OBJECT_MAPPER.createArrayNode();
        value.stream().map(JacksonAdapter::toJsonNode).forEach(arrayNode::add);
        return arrayNode;
    }

    @Override
    public IJsonObjectWrapper createJsonObject() {
        return new JacksonObjectWrapper(this);
    }

    @Override
    public IJsonObjectWrapper createJsonObject(int initialCapacity) {
        return new JacksonObjectWrapper(this);
    }

    @Override
    public IJsonObjectWrapper createJsonObject(boolean ordered) {
        return new JacksonObjectWrapper(this);
    }

    @Override
    public IJsonObjectWrapper createJsonObject(int initialCapacity, boolean ordered) {
        return new JacksonObjectWrapper(this);
    }

    @Override
    public IJsonObjectWrapper createJsonObject(Map<?, ?> map) {
        return new JacksonObjectWrapper(this, map);
    }

    @Override
    public IJsonArrayWrapper createJsonArray() {
        return new JacksonArrayWrapper(this);
    }

    @Override
    public IJsonArrayWrapper createJsonArray(int initialCapacity) {
        return new JacksonArrayWrapper(this);
    }

    @Override
    public IJsonArrayWrapper createJsonArray(Object[] array) {
        return new JacksonArrayWrapper(this, array);
    }

    @Override
    public IJsonArrayWrapper createJsonArray(Collection<?> collection) {
        return new JacksonArrayWrapper(this, collection);
    }

    @Override
    public JsonWrapper fromJson(String jsonStr) {
        JsonWrapper jsonWrapper = null;
        if (jsonStr != null) {
            try {
                jsonWrapper = parseJsonJsonWrapper(OBJECT_MAPPER.readTree(jsonStr));
            } catch (JsonProcessingException e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
                }
            }
        }
        return jsonWrapper;
    }

    private JsonWrapper parseJsonJsonWrapper(JsonNode jsonNode) {
        JsonWrapper jsonWrapper = null;
        if (jsonNode != null) {
            if (jsonNode.isObject()) {
                jsonWrapper = new JsonWrapper(new JacksonObjectWrapper(this, (ObjectNode) jsonNode));
            } else if (jsonNode.isArray()) {
                jsonWrapper = new JsonWrapper(new JacksonArrayWrapper(this, (ArrayNode) jsonNode));
            }
        }
        return jsonWrapper;
    }

    @Override
    public JsonWrapper toJson(Object object) {
        return toJson(object, false);
    }

    @Override
    public JsonWrapper toJson(Object object, boolean snakeCase) {
        JsonWrapper jsonWrapper = null;
        if (object != null) {
            ObjectMapper objectMapper = createObjectMapper();
            if (snakeCase) {
                objectMapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
            }
            jsonWrapper = parseJsonJsonWrapper(objectMapper.valueToTree(JsonWrapper.unwrap(object)));
        }
        return jsonWrapper;
    }

    @Override
    public String toJsonString(Object object) {
        return toJsonString(object, false, false, false);
    }

    @Override
    public String toJsonString(Object object, boolean format) {
        return toJsonString(object, format, false, false);
    }

    @Override
    public String toJsonString(Object object, boolean format, boolean keepNullValue) {
        return toJsonString(object, format, keepNullValue, false);
    }

    @Override
    public String toJsonString(Object object, boolean format, boolean keepNullValue, boolean snakeCase) {
        ObjectMapper objectMapper = createObjectMapper();
        if (!keepNullValue) {
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        }
        if (snakeCase) {
            objectMapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
        }
        try {
            if (format) {
                return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(JsonWrapper.unwrap(object));
            } else {
                return objectMapper.writeValueAsString(JsonWrapper.unwrap(object));
            }
        } catch (JsonProcessingException e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
            }
        }
        return null;
    }

    @Override
    public byte[] serialize(Object object) throws Exception {
        return serialize(object, false);
    }

    @Override
    public byte[] serialize(Object object, boolean snakeCase) throws Exception {
        return toJsonString(object, false, false, snakeCase).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public <T> T deserialize(String jsonStr, Class<T> clazz) throws Exception {
        return deserialize(jsonStr, false, clazz);
    }

    @Override
    public <T> T deserialize(String jsonStr, boolean snakeCase, Class<T> clazz) throws Exception {
        if (snakeCase) {
            return createObjectMapper()
                    .setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy())
                    .readValue(jsonStr, clazz);
        }
        return OBJECT_MAPPER.readValue(jsonStr, clazz);
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) throws Exception {
        return deserialize(bytes, false, clazz);
    }

    @Override
    public <T> T deserialize(byte[] bytes, boolean snakeCase, Class<T> clazz) throws Exception {
        return deserialize(new String(bytes, StandardCharsets.UTF_8), snakeCase, clazz);
    }
}
