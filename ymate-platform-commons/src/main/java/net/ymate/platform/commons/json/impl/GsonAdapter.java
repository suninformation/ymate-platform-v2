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

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.ymate.platform.commons.json.*;
import net.ymate.platform.commons.json.support.JsonArrayGsonSerializer;
import net.ymate.platform.commons.json.support.JsonObjectGsonSerializer;
import net.ymate.platform.commons.json.support.JsonWrapperGsonSerializer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Map;

/**
 * GSON适配器实现，基于Google Gson库提供JSON操作功能。
 * <p>
 * 设计目的：封装Gson库的使用细节，提供统一的JSON操作接口实现。
 * <p>
 * 使用场景：
 * - 当应用程序选择使用Gson作为JSON处理库时
 * - 当需要利用Gson的灵活配置特性时
 * - 当需要支持Gson特有的功能时
 *
 * @author 刘镇 (suninformation@163.com) on 2020/6/10 2:58 下午
 * @since 2.1.0
 */
public class GsonAdapter implements IJsonAdapter {

    /**
     * Gson实例，预配置了自定义序列化器和反序列化器，用于处理特殊类型。
     * <p>
     * 配置内容包括：
     * - 注册JsonWrapper、IJsonObjectWrapper、IJsonArrayWrapper的序列化器
     * - 自定义Double和Float类型的序列化，避免科学记数法
     */
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(JsonWrapper.class, new JsonWrapperGsonSerializer())
            .registerTypeAdapter(IJsonObjectWrapper.class, new JsonObjectGsonSerializer())
            .registerTypeAdapter(IJsonArrayWrapper.class, new JsonArrayGsonSerializer())
            .registerTypeAdapter(Double.class, new TypeAdapter<Double>() {
                // 适配不同小数位数
                private final DecimalFormat df = new DecimalFormat("0.####################");

                @Override
                public void write(JsonWriter out, Double value) throws IOException {
                    if (value == null) {
                        out.nullValue();
                        return;
                    }
                    // 转为普通十进制字符串，避免科学记数法
                    out.value(df.format(value));
                }

                @Override
                public Double read(JsonReader in) throws IOException {
                    return Double.parseDouble(in.nextString());
                }
            })
            .registerTypeAdapter(Float.class, new TypeAdapter<Float>() {
                // 适配不同小数位数
                private final DecimalFormat df = new DecimalFormat("0.####################");

                @Override
                public void write(JsonWriter out, Float value) throws IOException {
                    if (value == null) {
                        out.nullValue();
                        return;
                    }
                    // 转为普通十进制字符串，避免科学记数法
                    out.value(df.format(value));
                }

                @Override
                public Float read(JsonReader in) throws IOException {
                    return Float.parseFloat(in.nextString());
                }
            })
            .create();

    /**
     * 将对象转换为JsonElement。
     *
     * @param value 要转换的对象
     * @return 转换后的JsonElement
     */
    public static JsonElement toJsonElement(Object value) {
        return GSON.toJsonTree(JsonWrapper.unwrap(value));
    }

    /**
     * 将Map转换为JsonObject。
     *
     * @param value 要转换的Map对象
     * @return 转换后的JsonObject
     */
    public static JsonObject toJsonObject(Map<?, ?> value) {
        JsonObject jsonObj = new JsonObject();
        value.forEach((key, v) -> jsonObj.add(String.valueOf(key), toJsonElement(v)));
        return jsonObj;
    }

    /**
     * 将Collection转换为JsonArray。
     *
     * @param value 要转换的Collection对象
     * @return 转换后的JsonArray
     */
    public static JsonArray toJsonArray(Collection<?> value) {
        JsonArray jsonArr = new JsonArray(value.size());
        value.stream().map(GsonAdapter::toJsonElement).forEach(jsonArr::add);
        return jsonArr;
    }

    /**
     * 创建一个空的JSON对象包装器。
     *
     * @return 空的JSON对象包装器实例
     */
    @Override
    public IJsonObjectWrapper createJsonObject() {
        return new GsonObjectWrapper(this);
    }

    /**
     * 创建一个指定初始容量的JSON对象包装器。
     *
     * @param initialCapacity 初始容量，必须大于等于0
     * @return 指定初始容量的JSON对象包装器实例
     */
    @Override
    public IJsonObjectWrapper createJsonObject(int initialCapacity) {
        return new GsonObjectWrapper(this);
    }

    /**
     * 创建一个指定是否有序的JSON对象包装器。
     *
     * @param ordered 是否有序，true表示保持插入顺序
     * @return 指定有序性的JSON对象包装器实例
     */
    @Override
    public IJsonObjectWrapper createJsonObject(boolean ordered) {
        return new GsonObjectWrapper(this);
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
        return new GsonObjectWrapper(this);
    }

    /**
     * 根据Map创建JSON对象包装器。
     *
     * @param map 用于创建JSON对象的Map，键值对会被转换为JSON属性
     * @return 基于Map创建的JSON对象包装器实例
     */
    @Override
    public IJsonObjectWrapper createJsonObject(Map<?, ?> map) {
        return new GsonObjectWrapper(this, map);
    }

    /**
     * 创建一个空的JSON数组包装器。
     *
     * @return 空的JSON数组包装器实例
     */
    @Override
    public IJsonArrayWrapper createJsonArray() {
        return new GsonArrayWrapper(this);
    }

    /**
     * 创建一个指定初始容量的JSON数组包装器。
     *
     * @param initialCapacity 初始容量，必须大于等于0
     * @return 指定初始容量的JSON数组包装器实例
     */
    @Override
    public IJsonArrayWrapper createJsonArray(int initialCapacity) {
        return new GsonArrayWrapper(this, initialCapacity);
    }

    /**
     * 根据数组创建JSON数组包装器。
     *
     * @param array 用于创建JSON数组的对象数组
     * @return 基于数组创建的JSON数组包装器实例
     */
    @Override
    public IJsonArrayWrapper createJsonArray(Object[] array) {
        return new GsonArrayWrapper(this, array);
    }

    /**
     * 根据集合创建JSON数组包装器。
     *
     * @param collection 用于创建JSON数组的集合
     * @return 基于集合创建的JSON数组包装器实例
     */
    @Override
    public IJsonArrayWrapper createJsonArray(Collection<?> collection) {
        return new GsonArrayWrapper(this, collection);
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
            Object obj = GSON.fromJson(jsonStr, JsonElement.class);
            if (obj instanceof JsonObject) {
                jsonWrapper = new JsonWrapper(new GsonObjectWrapper(this, (JsonObject) obj));
            } else if (obj instanceof JsonArray) {
                jsonWrapper = new JsonWrapper(new GsonArrayWrapper(this, (JsonArray) obj));
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
        return fromJson(toJsonString(object, false, false));
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
        return fromJson(toJsonString(object, false, false, snakeCase));
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
        GsonBuilder gsonBuilder = GSON.newBuilder();
        if (filter != null) {
            gsonBuilder.registerTypeAdapterFactory(new TypeAdapterFactory() {
                @Override
                public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
                    final TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
                    return new TypeAdapter<T>() {
                        @Override
                        public void write(JsonWriter out, T value) throws IOException {
                            if (value == null) {
                                out.nullValue();
                                return;
                            }
                            // 为POJO类型创建过滤后的JsonElement
                            JsonElement jsonElement = delegate.toJsonTree(value);
                            if (jsonElement.isJsonObject()) {
                                JsonObject filteredJson = new JsonObject();
                                JsonObject originalJson = jsonElement.getAsJsonObject();
                                // 遍历所有属性，根据filter决定是否包含
                                for (Map.Entry<String, JsonElement> entry : originalJson.entrySet()) {
                                    if (filter.filter(value, entry.getKey())) {
                                        filteredJson.add(entry.getKey(), entry.getValue());
                                    }
                                }
                                out.jsonValue(gson.toJson(filteredJson));
                            } else {
                                // 非对象类型直接写入
                                delegate.write(out, value);
                            }
                        }

                        @Override
                        public T read(JsonReader in) throws IOException {
                            return delegate.read(in);
                        }
                    };
                }
            });
        }
        if (format) {
            gsonBuilder.setPrettyPrinting();
        }
        if (snakeCase) {
            gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
        }
        if (keepNullValue) {
            gsonBuilder.serializeNulls();
        }
        return gsonBuilder.create().toJson(JsonWrapper.unwrap(object));
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
        return toJsonString(object, false, false, snakeCase).getBytes(StandardCharsets.UTF_8);
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
        return deserialize(jsonStr, false, clazz);
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
        if (snakeCase) {
            return GSON.newBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .create().fromJson(jsonStr, clazz);
        }
        return GSON.fromJson(jsonStr, clazz);
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
        if (snakeCase) {
            return GSON.newBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .create().fromJson(jsonStr, typeRef.getType());
        }
        return GSON.fromJson(jsonStr, typeRef.getType());
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
