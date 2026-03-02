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
package net.ymate.platform.commons.json;

import net.ymate.platform.commons.json.impl.DefaultJsonAdapterFactory;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Json包装类，提供统一的JSON操作接口，封装不同JSON库的实现细节，便于在不同JSON库间切换。
 * <p>
 * 设计目的：实现JSON操作的统一抽象，降低对具体JSON库的依赖，提高代码的可移植性和扩展性。
 * <p>
 * 使用场景：
 * - 需要在应用中统一处理JSON数据，支持多种JSON库实现
 * - 希望通过配置或扩展机制动态切换JSON处理库
 * - 需要对JSON对象和数组进行统一的包装和操作
 *
 * @author 刘镇 (suninformation@163.com) on 2020/6/9 9:26 下午
 * @since 2.1.0
 */
public final class JsonWrapper implements Serializable {

    private static final Log LOG = LogFactory.getLog(JsonWrapper.class);

    private static IJsonAdapter jsonAdapter;

    static {
        try {
            String jsonAdapterClass = System.getProperty("ymp.jsonAdapterClass");
            jsonAdapter = ClassUtils.impl(jsonAdapterClass, IJsonAdapter.class, JsonWrapper.class);
            if (jsonAdapter == null) {
                IJsonAdapterFactory jsonAdapterFactory = ClassUtils.getExtensionLoader(IJsonAdapterFactory.class).getExtension();
                if (jsonAdapterFactory == null) {
                    jsonAdapterFactory = new DefaultJsonAdapterFactory();
                }
                jsonAdapter = jsonAdapterFactory.getJsonAdapter();
                if (jsonAdapter != null && LOG.isInfoEnabled()) {
                    LOG.info(String.format("Using JsonAdapter class [%s].", jsonAdapter.getClass().getName()));
                }
            } else if (LOG.isInfoEnabled()) {
                LOG.info(String.format("Using JsonAdapter class [%s].", jsonAdapterClass));
            }
        } catch (Exception e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
            }
        }
    }

    /**
     * 获取当前使用的JSON适配器实例。
     *
     * @return 当前JSON适配器实例，可能为null
     */
    public static IJsonAdapter getJsonAdapter() {
        return jsonAdapter;
    }

    /**
     * 将包装对象转换为原始对象，递归处理嵌套的JSON包装类型。
     *
     * @param value 要转换的对象，可能是JsonWrapper、IJsonArrayWrapper、IJsonObjectWrapper、IJsonNodeWrapper、集合或数组
     * @return 转换后的原始对象
     */
    public static Object unwrap(Object value) {
        if (value instanceof JsonWrapper) {
            if (((JsonWrapper) value).isJsonObject()) {
                value = unwrap(((JsonWrapper) value).getAsJsonObject());
            } else if (((JsonWrapper) value).isJsonArray()) {
                value = unwrap(((JsonWrapper) value).getAsJsonArray());
            }
        }
        if (value instanceof IJsonArrayWrapper) {
            value = ((IJsonArrayWrapper) value).toList();
        } else if (value instanceof IJsonObjectWrapper) {
            value = ((IJsonObjectWrapper) value).toMap();
        } else if (value instanceof IJsonNodeWrapper) {
            value = ((IJsonNodeWrapper) value).get();
        } else if (value instanceof Collection) {
            Collection<?> collection = (Collection<?>) value;
            value = collection.stream().map(JsonWrapper::unwrap).collect(Collectors.toCollection(() -> new ArrayList<>(collection.size())));
        } else if (value instanceof Map) {
            Map<String, Object> newMap = new LinkedHashMap<>();
            ((Map<?, ?>) value).forEach((key, v) -> newMap.put(String.valueOf(key), unwrap(v)));
            value = newMap;
        } else if (value != null && value.getClass().isArray()) {
            // 将数组转集合是为了兼顾fastjson对数组进行序列化操作时未能传递SerializeConfig参数导致结果未达预期
            Object[] array = (Object[]) value;
            value = Arrays.asList(array);
        }
        return value;
    }

    /**
     * 创建一个空的JSON对象包装器。
     *
     * @return 空的JSON对象包装器实例
     */
    public static IJsonObjectWrapper createJsonObject() {
        return jsonAdapter.createJsonObject();
    }

    /**
     * 创建一个指定初始容量的JSON对象包装器。
     *
     * @param initialCapacity 初始容量，必须大于等于0
     * @return 指定初始容量的JSON对象包装器实例
     */
    public static IJsonObjectWrapper createJsonObject(int initialCapacity) {
        return jsonAdapter.createJsonObject(initialCapacity);
    }

    /**
     * 创建一个指定是否有序的JSON对象包装器。
     *
     * @param ordered 是否有序，true表示保持插入顺序
     * @return 指定有序性的JSON对象包装器实例
     */
    public static IJsonObjectWrapper createJsonObject(boolean ordered) {
        return jsonAdapter.createJsonObject(ordered);
    }

    /**
     * 创建一个指定初始容量和有序性的JSON对象包装器。
     *
     * @param initialCapacity 初始容量，必须大于等于0
     * @param ordered         是否有序，true表示保持插入顺序
     * @return 指定初始容量和有序性的JSON对象包装器实例
     */
    public static IJsonObjectWrapper createJsonObject(int initialCapacity, boolean ordered) {
        return jsonAdapter.createJsonObject(initialCapacity, ordered);
    }

    /**
     * 根据Map创建JSON对象包装器。
     *
     * @param map 用于创建JSON对象的Map，键值对会被转换为JSON属性
     * @return 基于Map创建的JSON对象包装器实例
     */
    public static IJsonObjectWrapper createJsonObject(Map<?, ?> map) {
        return jsonAdapter.createJsonObject(map);
    }

    /**
     * 创建一个空的JSON数组包装器。
     *
     * @return 空的JSON数组包装器实例
     */
    public static IJsonArrayWrapper createJsonArray() {
        return jsonAdapter.createJsonArray();
    }

    /**
     * 创建一个指定初始容量的JSON数组包装器。
     *
     * @param initialCapacity 初始容量，必须大于等于0
     * @return 指定初始容量的JSON数组包装器实例
     */
    public static IJsonArrayWrapper createJsonArray(int initialCapacity) {
        return jsonAdapter.createJsonArray(initialCapacity);
    }

    /**
     * 根据数组创建JSON数组包装器。
     *
     * @param array 用于创建JSON数组的对象数组
     * @return 基于数组创建的JSON数组包装器实例
     */
    public static IJsonArrayWrapper createJsonArray(Object[] array) {
        return jsonAdapter.createJsonArray(array);
    }

    /**
     * 根据集合创建JSON数组包装器。
     *
     * @param collection 用于创建JSON数组的集合
     * @return 基于集合创建的JSON数组包装器实例
     */
    public static IJsonArrayWrapper createJsonArray(Collection<?> collection) {
        return jsonAdapter.createJsonArray(collection);
    }

    /**
     * 将JSON字符串转换为JsonWrapper对象。
     *
     * @param jsonStr JSON字符串，不能为空
     * @return 转换后的JsonWrapper对象
     */
    public static JsonWrapper fromJson(String jsonStr) {
        return jsonAdapter.fromJson(jsonStr);
    }

    /**
     * 将Java对象转换为JsonWrapper对象。
     *
     * @param object 要转换的Java对象
     * @return 转换后的JsonWrapper对象
     */
    public static JsonWrapper toJson(Object object) {
        return jsonAdapter.toJson(object);
    }

    /**
     * 将Java对象转换为JsonWrapper对象，支持蛇形命名转换。
     *
     * @param object    要转换的Java对象
     * @param snakeCase 是否将驼峰命名转换为蛇形命名
     * @return 转换后的JsonWrapper对象
     */
    public static JsonWrapper toJson(Object object, boolean snakeCase) {
        return jsonAdapter.toJson(object, snakeCase);
    }

    /**
     * 将Java对象转换为JSON字符串。
     *
     * @param object 要转换的Java对象
     * @return 转换后的JSON字符串
     */
    public static String toJsonString(Object object) {
        return jsonAdapter.toJsonString(object);
    }

    /**
     * 将Java对象转换为JSON字符串，支持格式化输出。
     *
     * @param object 要转换的Java对象
     * @param format 是否格式化输出，true表示格式化（带缩进），false表示紧凑输出
     * @return 转换后的JSON字符串
     */
    public static String toJsonString(Object object, boolean format) {
        return jsonAdapter.toJsonString(object, format);
    }

    /**
     * 将Java对象转换为JSON字符串，支持格式化输出和保留空值。
     *
     * @param object        要转换的Java对象
     * @param format        是否格式化输出，true表示格式化（带缩进），false表示紧凑输出
     * @param keepNullValue 是否保留空值，true表示保留，false表示忽略
     * @return 转换后的JSON字符串
     */
    public static String toJsonString(Object object, boolean format, boolean keepNullValue) {
        return jsonAdapter.toJsonString(object, format, keepNullValue);
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
    public static String toJsonString(Object object, boolean format, boolean keepNullValue, boolean snakeCase) {
        return jsonAdapter.toJsonString(object, format, keepNullValue, snakeCase);
    }

    /**
     * 将Java对象转换为JSON字符串，支持属性过滤。
     *
     * @param object 要转换的Java对象
     * @param filter 属性过滤器，用于控制哪些属性被序列化
     * @return 转换后的JSON字符串
     * @since 2.1.4
     */
    public static String toJsonString(Object object, IJsonPropertyFilter filter) {
        return jsonAdapter.toJsonString(object, filter);
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
    public static String toJsonString(Object object, boolean format, IJsonPropertyFilter filter) {
        return jsonAdapter.toJsonString(object, format, filter);
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
    public static String toJsonString(Object object, boolean format, boolean keepNullValue, IJsonPropertyFilter filter) {
        return jsonAdapter.toJsonString(object, format, keepNullValue, filter);
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
     * @since 2.1.4
     */
    public static String toJsonString(Object object, boolean format, boolean keepNullValue, boolean snakeCase, IJsonPropertyFilter filter) {
        return jsonAdapter.toJsonString(object, format, keepNullValue, snakeCase, filter);
    }

    /**
     * 将Java对象序列化为JSON字节数组。
     *
     * @param object 要序列化的Java对象
     * @return 序列化后的JSON字节数组
     */
    public static byte[] serialize(Object object) {
        return jsonAdapter.serialize(object);
    }

    /**
     * 将Java对象序列化为JSON字节数组，支持蛇形命名转换。
     *
     * @param object    要序列化的Java对象
     * @param snakeCase 是否将驼峰命名转换为蛇形命名
     * @return 序列化后的JSON字节数组
     */
    public static byte[] serialize(Object object, boolean snakeCase) {
        return jsonAdapter.serialize(object, snakeCase);
    }

    /**
     * 将JSON字符串反序列化为指定类型的Java对象。
     *
     * @param <T>     目标对象类型
     * @param jsonStr JSON字符串，不能为空
     * @param clazz   目标对象的类类型
     * @return 反序列化后的Java对象
     */
    public static <T> T deserialize(String jsonStr, Class<T> clazz) {
        return jsonAdapter.deserialize(jsonStr, clazz);
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
    public static <T> T deserialize(String jsonStr, boolean snakeCase, Class<T> clazz) {
        return jsonAdapter.deserialize(jsonStr, snakeCase, clazz);
    }

    /**
     * 将JSON字节数组反序列化为指定类型的Java对象。
     *
     * @param <T>   目标对象类型
     * @param bytes JSON字节数组，不能为空
     * @param clazz 目标对象的类类型
     * @return 反序列化后的Java对象
     */
    public static <T> T deserialize(byte[] bytes, Class<T> clazz) {
        return jsonAdapter.deserialize(bytes, clazz);
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
    public static <T> T deserialize(byte[] bytes, boolean snakeCase, Class<T> clazz) {
        return jsonAdapter.deserialize(bytes, snakeCase, clazz);
    }

    /**
     * 将JSON字符串反序列化为指定泛型类型的Java对象。
     *
     * @param <T>     目标对象类型
     * @param jsonStr JSON字符串，不能为空
     * @param typeRef 泛型类型引用，用于处理复杂泛型类型
     * @return 反序列化后的Java对象
     */
    public static <T> T deserialize(String jsonStr, TypeReferenceWrapper<T> typeRef) {
        return jsonAdapter.deserialize(jsonStr, typeRef);
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
    public static <T> T deserialize(String jsonStr, boolean snakeCase, TypeReferenceWrapper<T> typeRef) {
        return jsonAdapter.deserialize(jsonStr, snakeCase, typeRef);
    }

    /**
     * 将JSON字节数组反序列化为指定泛型类型的Java对象。
     *
     * @param <T>     目标对象类型
     * @param bytes   JSON字节数组，不能为空
     * @param typeRef 泛型类型引用，用于处理复杂泛型类型
     * @return 反序列化后的Java对象
     */
    public static <T> T deserialize(byte[] bytes, TypeReferenceWrapper<T> typeRef) {
        return jsonAdapter.deserialize(bytes, typeRef);
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
    public static <T> T deserialize(byte[] bytes, boolean snakeCase, TypeReferenceWrapper<T> typeRef) {
        return jsonAdapter.deserialize(bytes, snakeCase, typeRef);
    }

    private final Object object;

    /**
     * 构造函数，使用JSON对象包装器创建JsonWrapper实例。
     *
     * @param jsonObjectWrapper JSON对象包装器，不能为空
     * @throws NullPointerException 如果jsonObjectWrapper为null
     */
    public JsonWrapper(IJsonObjectWrapper jsonObjectWrapper) {
        this.object = Objects.requireNonNull(jsonObjectWrapper, "jsonObjectWrapper must not be null.");
    }

    /**
     * 构造函数，使用JSON数组包装器创建JsonWrapper实例。
     *
     * @param jsonArrayWrapper JSON数组包装器，不能为空
     * @throws NullPointerException 如果jsonArrayWrapper为null
     */
    public JsonWrapper(IJsonArrayWrapper jsonArrayWrapper) {
        this.object = Objects.requireNonNull(jsonArrayWrapper, "jsonArrayWrapper must not be null.");
    }

    /**
     * 判断当前JsonWrapper是否包装了JSON对象。
     *
     * @return 如果包装了JSON对象返回true，否则返回false
     */
    public boolean isJsonObject() {
        return object instanceof IJsonObjectWrapper;
    }

    /**
     * 判断当前JsonWrapper是否包装了JSON数组。
     *
     * @return 如果包装了JSON数组返回true，否则返回false
     */
    public boolean isJsonArray() {
        return object instanceof IJsonArrayWrapper;
    }

    /**
     * 获取包装的JSON对象。
     *
     * @return 如果当前包装的是JSON对象则返回对应包装器，否则返回null
     */
    public IJsonObjectWrapper getAsJsonObject() {
        if (isJsonObject()) {
            return (IJsonObjectWrapper) object;
        }
        return null;
    }

    /**
     * 获取包装的JSON数组。
     *
     * @return 如果当前包装的是JSON数组则返回对应包装器，否则返回null
     */
    public IJsonArrayWrapper getAsJsonArray() {
        if (isJsonArray()) {
            return (IJsonArrayWrapper) object;
        }
        return null;
    }

    /**
     * 比较当前对象与指定对象是否相等。
     *
     * @param o 要比较的对象
     * @return 如果两个对象相等返回true，否则返回false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JsonWrapper that = (JsonWrapper) o;
        return new EqualsBuilder()
                .append(object, that.object)
                .isEquals();
    }

    /**
     * 获取当前对象的哈希码。
     *
     * @return 对象的哈希码值
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(object)
                .toHashCode();
    }

    /**
     * 将当前对象转换为JSON字符串，支持格式化输出和保留空值。
     *
     * @param format        是否格式化输出，true表示格式化（带缩进），false表示紧凑输出
     * @param keepNullValue 是否保留空值，true表示保留，false表示忽略
     * @return 转换后的JSON字符串
     */
    public String toString(boolean format, boolean keepNullValue) {
        return toString(format, keepNullValue, false);
    }

    /**
     * 将当前对象转换为JSON字符串，支持格式化输出、保留空值和蛇形命名转换。
     *
     * @param format        是否格式化输出，true表示格式化（带缩进），false表示紧凑输出
     * @param keepNullValue 是否保留空值，true表示保留，false表示忽略
     * @param snakeCase     是否将驼峰命名转换为蛇形命名
     * @return 转换后的JSON字符串
     */
    public String toString(boolean format, boolean keepNullValue, boolean snakeCase) {
        if (isJsonObject()) {
            return ((IJsonObjectWrapper) object).toString(format, keepNullValue, snakeCase);
        } else if (isJsonArray()) {
            return ((IJsonArrayWrapper) object).toString(format, keepNullValue, snakeCase);
        }
        return object.toString();
    }

    /**
     * 将当前对象转换为JSON字符串（默认紧凑输出）。
     *
     * @return 转换后的JSON字符串
     */
    @Override
    public String toString() {
        return object.toString();
    }
}
