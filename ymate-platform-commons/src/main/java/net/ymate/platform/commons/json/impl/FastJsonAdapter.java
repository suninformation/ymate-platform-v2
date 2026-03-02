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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.*;
import net.ymate.platform.commons.json.*;
import net.ymate.platform.commons.json.support.JsonArrayFastJsonSerializer;
import net.ymate.platform.commons.json.support.JsonObjectFastJsonSerializer;
import net.ymate.platform.commons.json.support.JsonWrapperFastJsonSerializer;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * FastJSON适配器实现，基于Alibaba FastJSON库提供JSON操作功能。
 * <p>
 * 设计目的：封装FastJSON库的使用细节，提供统一的JSON操作接口实现。
 * <p>
 * 使用场景：
 * - 当应用程序选择使用FastJSON作为JSON处理库时
 * - 当需要利用FastJSON的高性能特性时
 * - 当需要支持FastJSON特有的功能时
 *
 * @author 刘镇 (suninformation@163.com) on 2020/6/9 9:17 下午
 * @since 2.1.0
 */
public class FastJsonAdapter implements IJsonAdapter {

    /**
     * 蛇形命名的序列化配置，用于将驼峰命名转换为蛇形命名。
     */
    public static final SerializeConfig SNAKE_CASE_SERIALIZE_CONFIG = new SerializeConfig();

    /**
     * 蛇形命名的反序列化配置，用于将蛇形命名转换为驼峰命名。
     */
    public static final ParserConfig SNAKE_CASE_PARSE_CONFIG = new ParserConfig();

    /*
     * 静态初始化块，用于配置FastJSON的全局设置和自定义序列化/反序列化器。
     * <p>
     * 配置内容包括：
     * - 禁用循环引用检测
     * - 以普通格式写入BigDecimal
     * - 注册自定义序列化器和反序列化器
     * - 配置数字类型的序列化方式
     */
    static {
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.DisableCircularReferenceDetect.getMask();
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.WriteBigDecimalAsPlain.getMask();
        //
        JsonWrapperFastJsonSerializer.Serializer jsonWrapperFastJsonSerializer = new JsonWrapperFastJsonSerializer.Serializer();
        JsonObjectFastJsonSerializer.Serializer jsonObjectFastJsonSerializer = new JsonObjectFastJsonSerializer.Serializer();
        JsonArrayFastJsonSerializer.Serializer jsonArrayFastJsonSerializer = new JsonArrayFastJsonSerializer.Serializer();
        //
        JsonWrapperFastJsonSerializer.Deserializer jsonWrapperFastJsonDeserializer = new JsonWrapperFastJsonSerializer.Deserializer();
        JsonObjectFastJsonSerializer.Deserializer jsonObjectFastJsonDeserializer = new JsonObjectFastJsonSerializer.Deserializer();
        JsonArrayFastJsonSerializer.Deserializer jsonArrayFastJsonDeserializer = new JsonArrayFastJsonSerializer.Deserializer();
        ObjectSerializer numberSerializer = (serializer, object, fieldName, fieldType, features) -> {
            if (object == null) {
                serializer.writeNull();
            } else {
                serializer.write(new BigDecimal(String.valueOf(object)).toPlainString());
            }
        };
        //
        SerializeConfig globalConfig = SerializeConfig.getGlobalInstance();
        globalConfig.put(JsonWrapper.class, jsonWrapperFastJsonSerializer);
        globalConfig.put(FastJsonObjectWrapper.class, jsonObjectFastJsonSerializer);
        globalConfig.put(FastJsonArrayWrapper.class, jsonArrayFastJsonSerializer);
        globalConfig.put(IJsonObjectWrapper.class, jsonObjectFastJsonSerializer);
        globalConfig.put(IJsonArrayWrapper.class, jsonArrayFastJsonSerializer);
        globalConfig.put(Double.class, numberSerializer);
        globalConfig.put(Float.class, numberSerializer);
        globalConfig.put(Double.TYPE, numberSerializer);
        globalConfig.put(Float.TYPE, numberSerializer);
        //
        SNAKE_CASE_SERIALIZE_CONFIG.propertyNamingStrategy = PropertyNamingStrategy.SnakeCase;
        SNAKE_CASE_SERIALIZE_CONFIG.put(JsonWrapper.class, jsonWrapperFastJsonSerializer);
        SNAKE_CASE_SERIALIZE_CONFIG.put(FastJsonObjectWrapper.class, jsonObjectFastJsonSerializer);
        SNAKE_CASE_SERIALIZE_CONFIG.put(FastJsonArrayWrapper.class, jsonArrayFastJsonSerializer);
        SNAKE_CASE_SERIALIZE_CONFIG.put(IJsonObjectWrapper.class, jsonObjectFastJsonSerializer);
        SNAKE_CASE_SERIALIZE_CONFIG.put(IJsonArrayWrapper.class, jsonArrayFastJsonSerializer);
        SNAKE_CASE_SERIALIZE_CONFIG.put(Double.class, numberSerializer);
        SNAKE_CASE_SERIALIZE_CONFIG.put(Float.class, numberSerializer);
        SNAKE_CASE_SERIALIZE_CONFIG.put(Double.TYPE, numberSerializer);
        SNAKE_CASE_SERIALIZE_CONFIG.put(Float.TYPE, numberSerializer);
        //
        ParserConfig parserConfig = ParserConfig.getGlobalInstance();
        parserConfig.putDeserializer(JsonWrapper.class, jsonWrapperFastJsonDeserializer);
        parserConfig.putDeserializer(FastJsonObjectWrapper.class, jsonObjectFastJsonDeserializer);
        parserConfig.putDeserializer(FastJsonArrayWrapper.class, jsonArrayFastJsonDeserializer);
        parserConfig.putDeserializer(IJsonObjectWrapper.class, jsonObjectFastJsonDeserializer);
        parserConfig.putDeserializer(IJsonArrayWrapper.class, jsonArrayFastJsonDeserializer);
        //
        SNAKE_CASE_PARSE_CONFIG.putDeserializer(JsonWrapper.class, jsonWrapperFastJsonDeserializer);
        SNAKE_CASE_PARSE_CONFIG.putDeserializer(FastJsonObjectWrapper.class, jsonObjectFastJsonDeserializer);
        SNAKE_CASE_PARSE_CONFIG.putDeserializer(FastJsonArrayWrapper.class, jsonArrayFastJsonDeserializer);
        SNAKE_CASE_PARSE_CONFIG.putDeserializer(IJsonObjectWrapper.class, jsonObjectFastJsonDeserializer);
        SNAKE_CASE_PARSE_CONFIG.putDeserializer(IJsonArrayWrapper.class, jsonArrayFastJsonDeserializer);
    }

    /**
     * 将Map转换为FastJSON的JSONObject。
     *
     * @param value 要转换的Map对象，键值对会被转换为JSON属性
     * @return 转换后的JSONObject实例
     */
    public static JSONObject toJsonObject(Map<?, ?> value) {
        JSONObject jsonObj = new JSONObject(value.size(), value instanceof LinkedHashMap);
        value.forEach((key, v) -> jsonObj.put(String.valueOf(key), JsonWrapper.unwrap(v)));
        return jsonObj;
    }

    /**
     * 将Collection转换为FastJSON的JSONArray。
     *
     * @param value 要转换的Collection对象，元素会被转换为JSON数组元素
     * @return 转换后的JSONArray实例
     */
    public static JSONArray toJsonArray(Collection<?> value) {
        JSONArray jsonArr = new JSONArray(value.size());
        value.stream().map(JsonWrapper::unwrap).forEach(jsonArr::add);
        return jsonArr;
    }

    /**
     * 构造函数，用于初始化FastJSON适配器实例。
     */
    public FastJsonAdapter() {
    }

    /**
     * 创建一个空的JSON对象包装器。
     *
     * @return 空的JSON对象包装器实例
     */
    @Override
    public IJsonObjectWrapper createJsonObject() {
        return new FastJsonObjectWrapper(this);
    }

    /**
     * 创建一个指定初始容量的JSON对象包装器。
     *
     * @param initialCapacity 初始容量，必须大于等于0
     * @return 指定初始容量的JSON对象包装器实例
     */
    @Override
    public IJsonObjectWrapper createJsonObject(int initialCapacity) {
        return new FastJsonObjectWrapper(this, initialCapacity);
    }

    /**
     * 创建一个指定是否有序的JSON对象包装器。
     *
     * @param ordered 是否有序，true表示保持插入顺序
     * @return 指定有序性的JSON对象包装器实例
     */
    @Override
    public IJsonObjectWrapper createJsonObject(boolean ordered) {
        return new FastJsonObjectWrapper(this, ordered);
    }

    /**
     * 创建一个指定初始容量和有序性的JSON对象包装器。
     *
     * @param initialCapacity 初始容量，必须大于等于0
     * @param ordered         是否有序，true表示保持插入顺序
     * @return 指定初始容量和有序性的JSON对象包装器实例
     */
    @Override
    public IJsonObjectWrapper createJsonObject(int initialCapacity, boolean ordered) {
        return new FastJsonObjectWrapper(this, initialCapacity, ordered);
    }

    /**
     * 根据Map创建JSON对象包装器。
     *
     * @param map 用于创建JSON对象的Map，键值对会被转换为JSON属性
     * @return 基于Map创建的JSON对象包装器实例
     */
    @Override
    public IJsonObjectWrapper createJsonObject(Map<?, ?> map) {
        return new FastJsonObjectWrapper(this, map);
    }

    /**
     * 创建一个空的JSON数组包装器。
     *
     * @return 空的JSON数组包装器实例
     */
    @Override
    public IJsonArrayWrapper createJsonArray() {
        return new FastJsonArrayWrapper(this);
    }

    /**
     * 创建一个指定初始容量的JSON数组包装器。
     *
     * @param initialCapacity 初始容量，必须大于等于0
     * @return 指定初始容量的JSON数组包装器实例
     */
    @Override
    public IJsonArrayWrapper createJsonArray(int initialCapacity) {
        return new FastJsonArrayWrapper(this, initialCapacity);
    }

    /**
     * 根据数组创建JSON数组包装器。
     *
     * @param array 用于创建JSON数组的对象数组
     * @return 基于数组创建的JSON数组包装器实例
     */
    @Override
    public IJsonArrayWrapper createJsonArray(Object[] array) {
        return new FastJsonArrayWrapper(this, array);
    }

    /**
     * 根据集合创建JSON数组包装器。
     *
     * @param collection 用于创建JSON数组的集合
     * @return 基于集合创建的JSON数组包装器实例
     */
    @Override
    public IJsonArrayWrapper createJsonArray(Collection<?> collection) {
        return new FastJsonArrayWrapper(this, collection);
    }

    /**
     * 将JSON字符串转换为JsonWrapper对象。
     *
     * @param jsonStr JSON字符串，不能为空
     * @return 转换后的JsonWrapper对象，若字符串为null则返回null
     */
    @Override
    public JsonWrapper fromJson(String jsonStr) {
        JsonWrapper jsonWrapper = null;
        if (jsonStr != null) {
            Object obj = JSON.parse(jsonStr, ParserConfig.getGlobalInstance(), Feature.OrderedField);
            if (obj instanceof JSONObject) {
                jsonWrapper = new JsonWrapper(new FastJsonObjectWrapper(this, (JSONObject) obj));
            } else if (obj instanceof JSONArray) {
                jsonWrapper = new JsonWrapper(new FastJsonArrayWrapper(this, (JSONArray) obj));
            }
        }
        return jsonWrapper;
    }

    /**
     * 将Java对象转换为JsonWrapper对象。
     *
     * @param object 要转换的Java对象
     * @return 转换后的JsonWrapper对象
     */
    @Override
    public JsonWrapper toJson(Object object) {
        return toJson(object, false);
    }

    /**
     * 将Java对象转换为JsonWrapper对象，支持蛇形命名转换。
     *
     * @param object    要转换的Java对象
     * @param snakeCase 是否将驼峰命名转换为蛇形命名
     * @return 转换后的JsonWrapper对象
     */
    @Override
    public JsonWrapper toJson(Object object, boolean snakeCase) {
        JsonWrapper jsonWrapper = null;
        Object obj = JSON.toJSON(JsonWrapper.unwrap(object), snakeCase ? SNAKE_CASE_SERIALIZE_CONFIG : SerializeConfig.globalInstance);
        if (obj instanceof JSONObject) {
            jsonWrapper = new JsonWrapper(new FastJsonObjectWrapper(this, (JSONObject) obj));
        } else if (obj instanceof JSONArray) {
            jsonWrapper = new JsonWrapper(new FastJsonArrayWrapper(this, (JSONArray) obj));
        }
        return jsonWrapper;
    }

    /**
     * 将Java对象转换为JSON字符串。
     *
     * @param object 要转换的Java对象
     * @return 转换后的JSON字符串
     */
    @Override
    public String toJsonString(Object object) {
        return toJsonString(object, false, false, false, null);
    }

    /**
     * 将Java对象转换为JSON字符串，支持格式化输出。
     *
     * @param object 要转换的Java对象
     * @param format 是否格式化输出，true表示格式化（带缩进），false表示紧凑输出
     * @return 转换后的JSON字符串
     */
    @Override
    public String toJsonString(Object object, boolean format) {
        return toJsonString(object, format, false, false, null);
    }

    /**
     * 将Java对象转换为JSON字符串，支持格式化输出和保留空值。
     *
     * @param object        要转换的Java对象
     * @param format        是否格式化输出，true表示格式化（带缩进），false表示紧凑输出
     * @param keepNullValue 是否保留空值，true表示保留，false表示忽略
     * @return 转换后的JSON字符串
     */
    @Override
    public String toJsonString(Object object, boolean format, boolean keepNullValue) {
        return toJsonString(object, format, keepNullValue, false, null);
    }

    /**
     * 将Java对象转换为JSON字符串，支持格式化输出、保留空值和蛇形命名转换。
     *
     * @param object        要转换的Java对象
     * @param format        是否格式化输出，true表示格式化（带缩进），false表示紧凑输出
     * @param keepNullValue 是否保留空值，true表示保留，false表示忽略
     * @param snakeCase     是否将驼峰命名转换为蛇形命名
     * @return 转换后的JSON字符串
     */
    @Override
    public String toJsonString(Object object, boolean format, boolean keepNullValue, boolean snakeCase) {
        return toJsonString(object, format, keepNullValue, snakeCase, null);
    }

    /**
     * 将Java对象转换为JSON字符串，支持属性过滤。
     *
     * @param object 要转换的Java对象
     * @param filter 属性过滤器，用于控制哪些属性被序列化
     * @return 转换后的JSON字符串
     * @since 2.1.4
     */
    @Override
    public String toJsonString(Object object, IJsonPropertyFilter filter) {
        return toJsonString(object, false, false, false, filter);
    }

    /**
     * 将Java对象转换为JSON字符串，支持格式化输出和属性过滤。
     *
     * @param object 要转换的Java对象
     * @param format 是否格式化输出，true表示格式化（带缩进），false表示紧凑输出
     * @param filter 属性过滤器，用于控制哪些属性被序列化
     * @return 转换后的JSON字符串
     * @since 2.1.4
     */
    @Override
    public String toJsonString(Object object, boolean format, IJsonPropertyFilter filter) {
        return toJsonString(object, format, false, false, filter);
    }

    /**
     * 将Java对象转换为JSON字符串，支持格式化输出、保留空值和属性过滤。
     *
     * @param object        要转换的Java对象
     * @param format        是否格式化输出，true表示格式化（带缩进），false表示紧凑输出
     * @param keepNullValue 是否保留空值，true表示保留，false表示忽略
     * @param filter        属性过滤器，用于控制哪些属性被序列化
     * @return 转换后的JSON字符串
     * @since 2.1.4
     */
    @Override
    public String toJsonString(Object object, boolean format, boolean keepNullValue, IJsonPropertyFilter filter) {
        return toJsonString(object, format, keepNullValue, false, filter);
    }

    /**
     * 将Java对象转换为JSON字符串，支持格式化输出、保留空值、蛇形命名转换和属性过滤。
     *
     * @param object        要转换的Java对象
     * @param format        是否格式化输出，true表示格式化（带缩进），false表示紧凑输出
     * @param keepNullValue 是否保留空值，true表示保留，false表示忽略
     * @param snakeCase     是否将驼峰命名转换为蛇形命名
     * @param filter        属性过滤器，用于控制哪些属性被序列化
     * @return 转换后的JSON字符串
     */
    @Override
    public String toJsonString(Object object, boolean format, boolean keepNullValue, boolean snakeCase, IJsonPropertyFilter filter) {
        List<SerializerFeature> serializerFeatures = new ArrayList<>();
        if (format) {
            serializerFeatures.add(SerializerFeature.PrettyFormat);
        }
        if (keepNullValue) {
            serializerFeatures.addAll(Arrays.asList(
                    SerializerFeature.WriteMapNullValue,
                    SerializerFeature.WriteNullBooleanAsFalse,
                    SerializerFeature.WriteNullListAsEmpty,
                    SerializerFeature.WriteNullStringAsEmpty,
                    SerializerFeature.WriteNullNumberAsZero));
        }
        // 收集所有的SerializeFilter
        List<SerializeFilter> filters = new ArrayList<>();
        if (filter != null) {
            PropertyPreFilter propertyPreFilter = (serializer, source, name) -> filter.filter(source, name);
            filters.add(propertyPreFilter);
        }
        // 创建一个ValueFilter，拦截并转换Double和Float值为普通格式
        ValueFilter valueFilter = (object1, name, value) -> {
            if (value instanceof Double) {
                return new BigDecimal(String.valueOf(value)).toPlainString();
            } else if (value instanceof Float) {
                return new BigDecimal(String.valueOf(value)).toPlainString();
            }
            return value;
        };
        filters.add(valueFilter);
        // 使用现有的配置
        SerializeConfig config = snakeCase ? SNAKE_CASE_SERIALIZE_CONFIG : SerializeConfig.getGlobalInstance();
        return JSON.toJSONString(JsonWrapper.unwrap(object), config, filters.toArray(new SerializeFilter[0]), serializerFeatures.toArray(new SerializerFeature[0]));
    }

    /**
     * 将Java对象序列化为JSON字节数组。
     *
     * @param object 要序列化的Java对象
     * @return 序列化后的JSON字节数组
     */
    @Override
    public byte[] serialize(Object object) {
        return serialize(object, false);
    }

    /**
     * 将Java对象序列化为JSON字节数组，支持蛇形命名转换。
     *
     * @param object    要序列化的Java对象
     * @param snakeCase 是否将驼峰命名转换为蛇形命名
     * @return 序列化后的JSON字节数组
     */
    @Override
    public byte[] serialize(Object object, boolean snakeCase) {
        // 使用与toJsonString相同的方法，正确处理嵌套类型
        String jsonStr = toJsonString(object, false, false, snakeCase);
        return jsonStr.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 将JSON字符串反序列化为指定类型的Java对象。
     *
     * @param <T>     目标对象类型
     * @param jsonStr JSON字符串，不能为空
     * @param clazz   目标对象的类类型
     * @return 反序列化后的Java对象
     */
    @Override
    public <T> T deserialize(String jsonStr, Class<T> clazz) {
        return JSON.parseObject(jsonStr, clazz, ParserConfig.getGlobalInstance(), Feature.OrderedField, Feature.SupportArrayToBean);
    }

    /**
     * 将JSON字符串反序列化为指定类型的Java对象，支持蛇形命名转换。
     *
     * @param <T>       目标对象类型
     * @param jsonStr   JSON字符串，不能为空
     * @param snakeCase 是否将蛇形命名转换为驼峰命名
     * @param clazz     目标对象的类类型
     * @return 反序列化后的Java对象
     */
    @Override
    public <T> T deserialize(String jsonStr, boolean snakeCase, Class<T> clazz) {
        return JSON.parseObject(jsonStr, clazz, snakeCase ? SNAKE_CASE_PARSE_CONFIG : ParserConfig.getGlobalInstance(), Feature.OrderedField, Feature.SupportArrayToBean);
    }

    /**
     * 将JSON字节数组反序列化为指定类型的Java对象。
     *
     * @param <T>   目标对象类型
     * @param bytes JSON字节数组，不能为空
     * @param clazz 目标对象的类类型
     * @return 反序列化后的Java对象
     */
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        return deserialize(bytes, false, clazz);
    }

    /**
     * 将JSON字节数组反序列化为指定类型的Java对象，支持蛇形命名转换。
     *
     * @param <T>       目标对象类型
     * @param bytes     JSON字节数组，不能为空
     * @param snakeCase 是否将蛇形命名转换为驼峰命名
     * @param clazz     目标对象的类类型
     * @return 反序列化后的Java对象
     */
    @Override
    public <T> T deserialize(byte[] bytes, boolean snakeCase, Class<T> clazz) {
        return deserialize(new String(bytes, StandardCharsets.UTF_8), snakeCase, clazz);
    }

    /**
     * 将JSON字符串反序列化为指定泛型类型的Java对象。
     *
     * @param <T>     目标对象类型
     * @param jsonStr JSON字符串，不能为空
     * @param typeRef 泛型类型引用，用于处理复杂泛型类型
     * @return 反序列化后的Java对象
     */
    @Override
    public <T> T deserialize(String jsonStr, TypeReferenceWrapper<T> typeRef) {
        return deserialize(jsonStr, false, typeRef);
    }

    /**
     * 将JSON字符串反序列化为指定泛型类型的Java对象，支持蛇形命名转换。
     *
     * @param <T>       目标对象类型
     * @param jsonStr   JSON字符串，不能为空
     * @param snakeCase 是否将蛇形命名转换为驼峰命名
     * @param typeRef   泛型类型引用，用于处理复杂泛型类型
     * @return 反序列化后的Java对象
     */
    @Override
    public <T> T deserialize(String jsonStr, boolean snakeCase, TypeReferenceWrapper<T> typeRef) {
        return JSON.parseObject(jsonStr, typeRef.getType(), snakeCase ? SNAKE_CASE_PARSE_CONFIG : ParserConfig.getGlobalInstance(), Feature.OrderedField, Feature.SupportArrayToBean);
    }

    /**
     * 将JSON字节数组反序列化为指定泛型类型的Java对象。
     *
     * @param <T>     目标对象类型
     * @param bytes   JSON字节数组，不能为空
     * @param typeRef 泛型类型引用，用于处理复杂泛型类型
     * @return 反序列化后的Java对象
     */
    @Override
    public <T> T deserialize(byte[] bytes, TypeReferenceWrapper<T> typeRef) {
        return deserialize(new String(bytes, StandardCharsets.UTF_8), false, typeRef);
    }

    /**
     * 将JSON字节数组反序列化为指定泛型类型的Java对象，支持蛇形命名转换。
     *
     * @param <T>       目标对象类型
     * @param bytes     JSON字节数组，不能为空
     * @param snakeCase 是否将蛇形命名转换为驼峰命名
     * @param typeRef   泛型类型引用，用于处理复杂泛型类型
     * @return 反序列化后的Java对象
     */
    @Override
    public <T> T deserialize(byte[] bytes, boolean snakeCase, TypeReferenceWrapper<T> typeRef) {
        return deserialize(new String(bytes, StandardCharsets.UTF_8), snakeCase, typeRef);
    }
}
