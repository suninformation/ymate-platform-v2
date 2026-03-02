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
package net.ymate.platform.commons.json.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.ymate.platform.commons.json.*;
import net.ymate.platform.commons.json.support.JsonArrayJacksonSerializer;
import net.ymate.platform.commons.json.support.JsonObjectJacksonSerializer;
import net.ymate.platform.commons.json.support.JsonWrapperJacksonSerializer;
import net.ymate.platform.commons.util.RuntimeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Jackson JSON适配器实现，基于Jackson库提供统一的JSON操作接口。
 * <p>
 * 设计目的：封装Jackson库的使用细节，提供统一的JSON操作接口实现，支持多种JSON处理场景。
 * <p>
 * 使用场景：
 * - 当需要使用Jackson进行JSON序列化和反序列化时
 * - 当需要统一的JSON操作接口时
 * - 当需要支持多种JSON库切换时
 * - 当需要处理复杂的JSON结构时
 *
 * @author 刘镇 (suninformation@163.com) on 2020/6/11 3:40 下午
 * @since 2.1.0
 */
public class JacksonAdapter implements IJsonAdapter {

    private static final Log LOG = LogFactory.getLog(JacksonAdapter.class);

    /**
     * 全局JsonMapper实例，用于JSON序列化和反序列化操作。
     */
    public static final JsonMapper OBJECT_MAPPER = createObjectMapper();

    /**
     * 创建并配置JsonMapper实例。
     * <p>
     * 配置包括：
     * - 注册自定义序列化器和反序列化器
     * - 启用多种JSON读取特性
     * - 配置枚举类型的序列化和反序列化方式
     * - 禁用未知属性失败和空bean失败
     *
     * @return 配置完成的JsonMapper实例
     */
    private static JsonMapper createObjectMapper() {
        SimpleModule module = new SimpleModule()
                .addSerializer(JsonWrapper.class, new JsonWrapperJacksonSerializer.Serializer())
                .addSerializer(IJsonObjectWrapper.class, new JsonObjectJacksonSerializer.Serializer())
                .addSerializer(IJsonArrayWrapper.class, new JsonArrayJacksonSerializer.Serializer())
                .addDeserializer(JsonWrapper.class, new JsonWrapperJacksonSerializer.Deserializer())
                .addDeserializer(IJsonObjectWrapper.class, new JsonObjectJacksonSerializer.Deserializer())
                .addDeserializer(IJsonArrayWrapper.class, new JsonArrayJacksonSerializer.Deserializer())
                .addSerializer(Double.class, new JsonSerializer<Double>() {
                    // 用于格式化的DecimalFormat，#.## 表示保留两位小数（可根据需求调整），避免科学记数法
                    private final DecimalFormat df = new DecimalFormat("#.##########");

                    @Override
                    public void serialize(Double value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                        if (value == null) {
                            gen.writeNull();
                            return;
                        }
                        // 将double转为普通小数格式的字符串输出
                        gen.writeString(df.format(value));
                        // 若想输出为数字类型（而非字符串），可改用：gen.writeNumber(df.format(value));
                    }
                })
                .addSerializer(Float.class, new JsonSerializer<Float>() {
                    private final DecimalFormat df = new DecimalFormat("#.##########");

                    @Override
                    public void serialize(Float value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                        if (value == null) {
                            gen.writeNull();
                            return;
                        }
                        gen.writeString(df.format(value));
                    }
                });
        return JsonMapper.builder()
                .enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS,
                        JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER,
                        JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES,
                        JsonReadFeature.ALLOW_SINGLE_QUOTES)
                .enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
                .enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .addModule(module)
                .build();
    }

    /**
     * 将对象转换为Jackson JsonNode。
     *
     * @param value 要转换的对象，可以是任意类型
     * @return Jackson JsonNode实例，若对象为null则返回null
     */
    public static JsonNode toJsonNode(Object value) {
        return OBJECT_MAPPER.valueToTree(JsonWrapper.unwrap(value));
    }

    /**
     * 将Map转换为Jackson ObjectNode。
     *
     * @param value 要转换的Map，键值对会被转换为JSON属性
     * @return Jackson ObjectNode实例，不为null
     */
    public static ObjectNode toObjectNode(Map<?, ?> value) {
        ObjectNode objectNode = OBJECT_MAPPER.createObjectNode();
        value.forEach((key, v) -> objectNode.set(String.valueOf(key), toJsonNode(v)));
        return objectNode;
    }

    /**
     * 将Collection转换为Jackson ArrayNode。
     *
     * @param value 要转换的Collection，元素会被转换为JSON数组元素
     * @return Jackson ArrayNode实例，不为null
     */
    public static ArrayNode toArrayNode(Collection<?> value) {
        ArrayNode arrayNode = OBJECT_MAPPER.createArrayNode();
        value.stream().map(JacksonAdapter::toJsonNode).forEach(arrayNode::add);
        return arrayNode;
    }

    /**
     * 创建一个空的JSON对象包装器。
     *
     * @return JSON对象包装器实例，不为null
     */
    @Override
    public IJsonObjectWrapper createJsonObject() {
        return new JacksonObjectWrapper(this);
    }

    /**
     * 创建一个指定初始容量的JSON对象包装器。
     *
     * @param initialCapacity 初始容量（Jackson实现中暂未使用）
     * @return JSON对象包装器实例，不为null
     */
    @Override
    public IJsonObjectWrapper createJsonObject(int initialCapacity) {
        return new JacksonObjectWrapper(this);
    }

    /**
     * 创建一个指定有序性的JSON对象包装器。
     *
     * @param ordered 是否有序（Jackson实现中暂未使用）
     * @return JSON对象包装器实例，不为null
     */
    @Override
    public IJsonObjectWrapper createJsonObject(boolean ordered) {
        return new JacksonObjectWrapper(this);
    }

    /**
     * 创建一个指定初始容量和有序性的JSON对象包装器。
     *
     * @param initialCapacity 初始容量（Jackson实现中暂未使用）
     * @param ordered         是否有序（Jackson实现中暂未使用）
     * @return JSON对象包装器实例，不为null
     */
    @Override
    public IJsonObjectWrapper createJsonObject(int initialCapacity, boolean ordered) {
        return new JacksonObjectWrapper(this);
    }

    /**
     * 根据Map创建JSON对象包装器。
     *
     * @param map 用于创建JSON对象的Map，键值对会被转换为JSON属性
     * @return JSON对象包装器实例，不为null
     */
    @Override
    public IJsonObjectWrapper createJsonObject(Map<?, ?> map) {
        return new JacksonObjectWrapper(this, map);
    }

    /**
     * 创建一个空的JSON数组包装器。
     *
     * @return JSON数组包装器实例，不为null
     */
    @Override
    public IJsonArrayWrapper createJsonArray() {
        return new JacksonArrayWrapper(this);
    }

    /**
     * 创建一个指定初始容量的JSON数组包装器。
     *
     * @param initialCapacity 初始容量（Jackson实现中暂未使用）
     * @return JSON数组包装器实例，不为null
     */
    @Override
    public IJsonArrayWrapper createJsonArray(int initialCapacity) {
        return new JacksonArrayWrapper(this);
    }

    /**
     * 根据数组创建JSON数组包装器。
     *
     * @param array 用于创建JSON数组的数组，元素会被转换为JSON数组元素
     * @return JSON数组包装器实例，不为null
     */
    @Override
    public IJsonArrayWrapper createJsonArray(Object[] array) {
        return new JacksonArrayWrapper(this, array);
    }

    /**
     * 根据Collection创建JSON数组包装器。
     *
     * @param collection 用于创建JSON数组的Collection，元素会被转换为JSON数组元素
     * @return JSON数组包装器实例，不为null
     */
    @Override
    public IJsonArrayWrapper createJsonArray(Collection<?> collection) {
        return new JacksonArrayWrapper(this, collection);
    }

    /**
     * 将JSON字符串解析为JsonWrapper对象。
     *
     * @param jsonStr JSON字符串，可以为null
     * @return JsonWrapper对象，若解析失败则返回null
     */
    @Override
    public JsonWrapper fromJson(String jsonStr) {
        JsonWrapper jsonWrapper = null;
        if (jsonStr != null) {
            try {
                jsonWrapper = parseJsonJsonWrapper(OBJECT_MAPPER.readTree(jsonStr));
            } catch (JsonProcessingException e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Failed to parse JSON string.", RuntimeUtils.unwrapThrow(e));
                }
            }
        }
        return jsonWrapper;
    }

    /**
     * 将JsonNode解析为JsonWrapper对象。
     *
     * @param jsonNode Jackson JsonNode实例，可以为null
     * @return JsonWrapper对象，若解析失败则返回null
     */
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

    /**
     * 将对象转换为JsonWrapper对象。
     *
     * @param object 要转换的对象，可以为null
     * @return JsonWrapper对象，若对象为null则返回null
     */
    @Override
    public JsonWrapper toJson(Object object) {
        return toJson(object, false);
    }

    /**
     * 将对象转换为JsonWrapper对象，支持蛇形命名转换。
     *
     * @param object    要转换的对象，可以为null
     * @param snakeCase 是否将驼峰命名转换为蛇形命名
     * @return JsonWrapper对象，若对象为null则返回null
     */
    @Override
    public JsonWrapper toJson(Object object, boolean snakeCase) {
        JsonWrapper jsonWrapper = null;
        if (object != null) {
            ObjectMapper objectMapper = snakeCase ? OBJECT_MAPPER.copy().setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy()) : OBJECT_MAPPER;
            jsonWrapper = parseJsonJsonWrapper(objectMapper.valueToTree(JsonWrapper.unwrap(object)));
        }
        return jsonWrapper;
    }

    /**
     * 将对象转换为JSON字符串（默认紧凑输出，忽略null值）。
     *
     * @param object 要转换的对象，可以为null
     * @return JSON字符串，若转换失败则返回null
     */
    @Override
    public String toJsonString(Object object) {
        return toJsonString(object, false, false, false, null);
    }

    /**
     * 将对象转换为JSON字符串，支持格式化输出。
     *
     * @param object 要转换的对象，可以为null
     * @param format 是否格式化输出，true表示格式化（带缩进），false表示紧凑输出
     * @return JSON字符串，若转换失败则返回null
     */
    @Override
    public String toJsonString(Object object, boolean format) {
        return toJsonString(object, format, false, false, null);
    }

    /**
     * 将对象转换为JSON字符串，支持格式化输出和保留空值。
     *
     * @param object        要转换的对象，可以为null
     * @param format        是否格式化输出，true表示格式化（带缩进），false表示紧凑输出
     * @param keepNullValue 是否保留空值，true表示保留，false表示忽略
     * @return JSON字符串，若转换失败则返回null
     */
    @Override
    public String toJsonString(Object object, boolean format, boolean keepNullValue) {
        return toJsonString(object, format, keepNullValue, false, null);
    }

    /**
     * 将对象转换为JSON字符串，支持格式化输出、保留空值和蛇形命名转换。
     *
     * @param object        要转换的对象，可以为null
     * @param format        是否格式化输出，true表示格式化（带缩进），false表示紧凑输出
     * @param keepNullValue 是否保留空值，true表示保留，false表示忽略
     * @param snakeCase     是否将驼峰命名转换为蛇形命名
     * @return JSON字符串，若转换失败则返回null
     */
    @Override
    public String toJsonString(Object object, boolean format, boolean keepNullValue, boolean snakeCase) {
        return toJsonString(object, format, keepNullValue, snakeCase, null);
    }

    /**
     * 将对象转换为JSON字符串，支持属性过滤。
     *
     * @param object 要转换的对象，可以为null
     * @param filter 属性过滤器，用于控制哪些属性被序列化
     * @return JSON字符串，若转换失败则返回null
     */
    @Override
    public String toJsonString(Object object, IJsonPropertyFilter filter) {
        return toJsonString(object, false, false, false, filter);
    }

    /**
     * 将对象转换为JSON字符串，支持格式化输出和属性过滤。
     *
     * @param object 要转换的对象，可以为null
     * @param format 是否格式化输出，true表示格式化（带缩进），false表示紧凑输出
     * @param filter 属性过滤器，用于控制哪些属性被序列化
     * @return JSON字符串，若转换失败则返回null
     */
    @Override
    public String toJsonString(Object object, boolean format, IJsonPropertyFilter filter) {
        return toJsonString(object, format, false, false, filter);
    }

    /**
     * 将对象转换为JSON字符串，支持格式化输出、保留空值和属性过滤。
     *
     * @param object        要转换的对象，可以为null
     * @param format        是否格式化输出，true表示格式化（带缩进），false表示紧凑输出
     * @param keepNullValue 是否保留空值，true表示保留，false表示忽略
     * @param filter        属性过滤器，用于控制哪些属性被序列化
     * @return JSON字符串，若转换失败则返回null
     */
    @Override
    public String toJsonString(Object object, boolean format, boolean keepNullValue, IJsonPropertyFilter filter) {
        return toJsonString(object, format, keepNullValue, false, filter);
    }

    /**
     * 将对象转换为JSON字符串，支持格式化输出、保留空值、蛇形命名转换和属性过滤。
     *
     * @param object        要转换的对象，可以为null
     * @param format        是否格式化输出，true表示格式化（带缩进），false表示紧凑输出
     * @param keepNullValue 是否保留空值，true表示保留，false表示忽略
     * @param snakeCase     是否将驼峰命名转换为蛇形命名
     * @param filter        属性过滤器，用于控制哪些属性被序列化
     * @return JSON字符串，若转换失败则返回null
     */
    @Override
    public String toJsonString(Object object, boolean format, boolean keepNullValue, boolean snakeCase, IJsonPropertyFilter filter) {
        ObjectMapper objectMapper = OBJECT_MAPPER.copy();
        if (!keepNullValue) {
            objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
        }
        if (snakeCase) {
            objectMapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
        }
        try {
            Object unwrappedObject = JsonWrapper.unwrap(object);
            if (filter != null) {
                // 将对象转换为Map
                Map<?, ?> map = objectMapper.convertValue(unwrappedObject, Map.class);
                // 过滤Map中的属性
                Map<String, Object> filteredMap = new LinkedHashMap<>();
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    String key = entry.getKey().toString();
                    if (filter.filter(unwrappedObject, key)) {
                        filteredMap.put(key, entry.getValue());
                    }
                }
                // 将过滤后的Map转换为JSON字符串
                if (format) {
                    return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(filteredMap);
                } else {
                    return objectMapper.writeValueAsString(filteredMap);
                }
            } else {
                if (format) {
                    return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(unwrappedObject);
                } else {
                    return objectMapper.writeValueAsString(unwrappedObject);
                }
            }
        } catch (JsonProcessingException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to convert object to JSON string.", RuntimeUtils.unwrapThrow(e));
            }
        }
        return null;
    }

    /**
     * 将对象序列化为JSON字节数组。
     *
     * @param object 要序列化的对象，可以为null
     * @return JSON字节数组，若序列化失败则返回null
     */
    @Override
    public byte[] serialize(Object object) {
        return serialize(object, false);
    }

    /**
     * 将对象序列化为JSON字节数组，支持蛇形命名转换。
     *
     * @param object    要序列化的对象，可以为null
     * @param snakeCase 是否将驼峰命名转换为蛇形命名
     * @return JSON字节数组，若序列化失败则返回null
     */
    @Override
    public byte[] serialize(Object object, boolean snakeCase) {
        return toJsonString(object, false, false, snakeCase).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 将JSON字符串反序列化为指定类型的对象。
     *
     * @param jsonStr JSON字符串，可以为null
     * @param clazz   目标类型的Class对象，不能为空
     * @param <T>     目标类型
     * @return 指定类型的对象，若反序列化失败则返回null
     */
    @Override
    public <T> T deserialize(String jsonStr, Class<T> clazz) {
        return deserialize(jsonStr, false, clazz);
    }

    /**
     * 将JSON字符串反序列化为指定类型的对象，支持蛇形命名转换。
     *
     * @param jsonStr   JSON字符串，可以为null
     * @param snakeCase 是否将蛇形命名转换为驼峰命名
     * @param clazz     目标类型的Class对象，不能为空
     * @param <T>       目标类型
     * @return 指定类型的对象，若反序列化失败则返回null
     */
    @Override
    public <T> T deserialize(String jsonStr, boolean snakeCase, Class<T> clazz) {
        ObjectMapper objectMapper = snakeCase ? OBJECT_MAPPER.copy().setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy()) : OBJECT_MAPPER;
        try {
            return objectMapper.readValue(jsonStr, clazz);
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to deserialize JSON string to class type.", RuntimeUtils.unwrapThrow(e));
            }
        }
        return null;
    }

    /**
     * 将JSON字节数组反序列化为指定类型的对象。
     *
     * @param bytes JSON字节数组，可以为null
     * @param clazz 目标类型的Class对象，不能为空
     * @param <T>   目标类型
     * @return 指定类型的对象，若反序列化失败则返回null
     */
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        return deserialize(bytes, false, clazz);
    }

    /**
     * 将JSON字节数组反序列化为指定类型的对象，支持蛇形命名转换。
     *
     * @param bytes     JSON字节数组，可以为null
     * @param snakeCase 是否将蛇形命名转换为驼峰命名
     * @param clazz     目标类型的Class对象，不能为空
     * @param <T>       目标类型
     * @return 指定类型的对象，若反序列化失败则返回null
     */
    @Override
    public <T> T deserialize(byte[] bytes, boolean snakeCase, Class<T> clazz) {
        return deserialize(new String(bytes, StandardCharsets.UTF_8), snakeCase, clazz);
    }

    /**
     * 将JSON字符串反序列化为指定类型引用的对象。
     *
     * @param jsonStr JSON字符串，可以为null
     * @param typeRef 类型引用包装器，不能为空
     * @param <T>     目标类型
     * @return 指定类型的对象，若反序列化失败则返回null
     */
    @Override
    public <T> T deserialize(String jsonStr, TypeReferenceWrapper<T> typeRef) {
        return deserialize(jsonStr, false, typeRef);
    }

    /**
     * 将JSON字符串反序列化为指定类型引用的对象，支持蛇形命名转换。
     *
     * @param jsonStr   JSON字符串，可以为null
     * @param snakeCase 是否将蛇形命名转换为驼峰命名
     * @param typeRef   类型引用包装器，不能为空
     * @param <T>       目标类型
     * @return 指定类型的对象，若反序列化失败则返回null
     */
    @Override
    public <T> T deserialize(String jsonStr, boolean snakeCase, TypeReferenceWrapper<T> typeRef) {
        ObjectMapper objectMapper = snakeCase ? OBJECT_MAPPER.copy().setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy()) : OBJECT_MAPPER;
        try {
            return objectMapper.readValue(jsonStr, objectMapper.constructType(typeRef.getType()));
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to deserialize JSON string to type reference.", RuntimeUtils.unwrapThrow(e));
            }
        }
        return null;
    }

    /**
     * 将JSON字节数组反序列化为指定类型引用的对象。
     *
     * @param bytes   JSON字节数组，可以为null
     * @param typeRef 类型引用包装器，不能为空
     * @param <T>     目标类型
     * @return 指定类型的对象，若反序列化失败则返回null
     */
    @Override
    public <T> T deserialize(byte[] bytes, TypeReferenceWrapper<T> typeRef) {
        return deserialize(new String(bytes, StandardCharsets.UTF_8), false, typeRef);
    }

    /**
     * 将JSON字节数组反序列化为指定类型引用的对象，支持蛇形命名转换。
     *
     * @param bytes     JSON字节数组，可以为null
     * @param snakeCase 是否将蛇形命名转换为驼峰命名
     * @param typeRef   类型引用包装器，不能为空
     * @param <T>       目标类型
     * @return 指定类型的对象，若反序列化失败则返回null
     */
    @Override
    public <T> T deserialize(byte[] bytes, boolean snakeCase, TypeReferenceWrapper<T> typeRef) {
        return deserialize(new String(bytes, StandardCharsets.UTF_8), snakeCase, typeRef);
    }
}
