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

import java.util.Collection;
import java.util.Map;

/**
 * JSON适配器接口，定义了JSON操作的核心方法，用于封装不同JSON库的实现细节。
 * <p>
 * 设计目的：提供统一的JSON操作抽象，使应用程序能够无缝切换不同的JSON库实现。
 * <p>
 * 使用场景：
 * - 实现不同JSON库的适配，如FastJSON、Gson、Jackson等
 * - 提供统一的JSON创建、转换、序列化和反序列化方法
 * - 支持JSON格式的灵活配置，如格式化、空值处理、命名转换等
 *
 * @author 刘镇 (suninformation@163.com) on 2020/6/8 5:14 下午
 * @since 2.1.0
 */
public interface IJsonAdapter {

    /**
     * 创建一个空的JSON对象包装器。
     *
     * @return 空的JSON对象包装器实例
     */
    IJsonObjectWrapper createJsonObject();

    /**
     * 创建一个指定初始容量的JSON对象包装器。
     *
     * @param initialCapacity 初始容量，必须大于等于0
     * @return 指定初始容量的JSON对象包装器实例
     */
    IJsonObjectWrapper createJsonObject(int initialCapacity);

    /**
     * 创建一个指定是否有序的JSON对象包装器。
     *
     * @param ordered 是否有序，true表示保持插入顺序
     * @return 指定有序性的JSON对象包装器实例
     */
    IJsonObjectWrapper createJsonObject(boolean ordered);

    /**
     * 创建一个指定初始容量和有序性的JSON对象包装器。
     *
     * @param initialCapacity 初始容量，必须大于等于0
     * @param ordered         是否有序，true表示保持插入顺序
     * @return 指定初始容量和有序性的JSON对象包装器实例
     */
    IJsonObjectWrapper createJsonObject(int initialCapacity, boolean ordered);

    /**
     * 根据Map创建JSON对象包装器。
     *
     * @param map 用于创建JSON对象的Map，键值对会被转换为JSON属性
     * @return 基于Map创建的JSON对象包装器实例
     */
    IJsonObjectWrapper createJsonObject(Map<?, ?> map);

    /**
     * 创建一个空的JSON数组包装器。
     *
     * @return 空的JSON数组包装器实例
     */
    IJsonArrayWrapper createJsonArray();

    /**
     * 创建一个指定初始容量的JSON数组包装器。
     *
     * @param initialCapacity 初始容量，必须大于等于0
     * @return 指定初始容量的JSON数组包装器实例
     */
    IJsonArrayWrapper createJsonArray(int initialCapacity);

    /**
     * 根据数组创建JSON数组包装器。
     *
     * @param array 用于创建JSON数组的对象数组
     * @return 基于数组创建的JSON数组包装器实例
     */
    IJsonArrayWrapper createJsonArray(Object[] array);

    /**
     * 根据集合创建JSON数组包装器。
     *
     * @param collection 用于创建JSON数组的集合
     * @return 基于集合创建的JSON数组包装器实例
     */
    IJsonArrayWrapper createJsonArray(Collection<?> collection);

    /**
     * 将JSON字符串转换为JsonWrapper对象。
     *
     * @param jsonStr JSON字符串，不能为空
     * @return 转换后的JsonWrapper对象
     */
    JsonWrapper fromJson(String jsonStr);

    /**
     * 将Java对象转换为JsonWrapper对象。
     *
     * @param object 要转换的Java对象
     * @return 转换后的JsonWrapper对象
     */
    JsonWrapper toJson(Object object);

    /**
     * 将Java对象转换为JsonWrapper对象，支持蛇形命名转换。
     *
     * @param object    要转换的Java对象
     * @param snakeCase 是否将驼峰命名转换为蛇形命名
     * @return 转换后的JsonWrapper对象
     */
    JsonWrapper toJson(Object object, boolean snakeCase);

    /**
     * 将Java对象转换为JSON字符串。
     *
     * @param object 要转换的Java对象
     * @return 转换后的JSON字符串
     */
    String toJsonString(Object object);

    /**
     * 将Java对象转换为JSON字符串，支持格式化输出。
     *
     * @param object 要转换的Java对象
     * @param format 是否格式化输出，true表示格式化（带缩进），false表示紧凑输出
     * @return 转换后的JSON字符串
     */
    String toJsonString(Object object, boolean format);

    /**
     * 将Java对象转换为JSON字符串，支持格式化输出和保留空值。
     *
     * @param object        要转换的Java对象
     * @param format        是否格式化输出，true表示格式化（带缩进），false表示紧凑输出
     * @param keepNullValue 是否保留空值，true表示保留，false表示忽略
     * @return 转换后的JSON字符串
     */
    String toJsonString(Object object, boolean format, boolean keepNullValue);

    /**
     * 将Java对象转换为JSON字符串，支持格式化输出、保留空值和蛇形命名转换。
     *
     * @param object        要转换的Java对象
     * @param format        是否格式化输出，true表示格式化（带缩进），false表示紧凑输出
     * @param keepNullValue 是否保留空值，true表示保留，false表示忽略
     * @param snakeCase     是否将驼峰命名转换为蛇形命名
     * @return 转换后的JSON字符串
     */
    String toJsonString(Object object, boolean format, boolean keepNullValue, boolean snakeCase);

    /**
     * 将Java对象转换为JSON字符串，支持属性过滤。
     *
     * @param object 要转换的Java对象
     * @param filter 属性过滤器，用于控制哪些属性被序列化
     * @return 转换后的JSON字符串
     * @since 2.1.4
     */
    String toJsonString(Object object, IJsonPropertyFilter filter);

    /**
     * 将Java对象转换为JSON字符串，支持格式化输出和属性过滤。
     *
     * @param object 要转换的Java对象
     * @param format 是否格式化输出，true表示格式化（带缩进），false表示紧凑输出
     * @param filter 属性过滤器，用于控制哪些属性被序列化
     * @return 转换后的JSON字符串
     * @since 2.1.4
     */
    String toJsonString(Object object, boolean format, IJsonPropertyFilter filter);

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
    String toJsonString(Object object, boolean format, boolean keepNullValue, IJsonPropertyFilter filter);

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
    String toJsonString(Object object, boolean format, boolean keepNullValue, boolean snakeCase, IJsonPropertyFilter filter);

    /**
     * 将Java对象序列化为JSON字节数组。
     *
     * @param object 要序列化的Java对象
     * @return 序列化后的JSON字节数组
     */
    byte[] serialize(Object object);

    /**
     * 将Java对象序列化为JSON字节数组，支持蛇形命名转换。
     *
     * @param object    要序列化的Java对象
     * @param snakeCase 是否将驼峰命名转换为蛇形命名
     * @return 序列化后的JSON字节数组
     */
    byte[] serialize(Object object, boolean snakeCase);

    /**
     * 将JSON字符串反序列化为指定类型的Java对象。
     *
     * @param <T>     目标对象类型
     * @param jsonStr JSON字符串，不能为空
     * @param clazz   目标对象的类类型
     * @return 反序列化后的Java对象
     */
    <T> T deserialize(String jsonStr, Class<T> clazz);

    /**
     * 将JSON字符串反序列化为指定类型的Java对象，支持蛇形命名转换。
     *
     * @param <T>       目标对象类型
     * @param jsonStr   JSON字符串，不能为空
     * @param snakeCase 是否将蛇形命名转换为驼峰命名
     * @param clazz     目标对象的类类型
     * @return 反序列化后的Java对象
     */
    <T> T deserialize(String jsonStr, boolean snakeCase, Class<T> clazz);

    /**
     * 将JSON字节数组反序列化为指定类型的Java对象。
     *
     * @param <T>   目标对象类型
     * @param bytes JSON字节数组，不能为空
     * @param clazz 目标对象的类类型
     * @return 反序列化后的Java对象
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz);

    /**
     * 将JSON字节数组反序列化为指定类型的Java对象，支持蛇形命名转换。
     *
     * @param <T>       目标对象类型
     * @param bytes     JSON字节数组，不能为空
     * @param snakeCase 是否将蛇形命名转换为驼峰命名
     * @param clazz     目标对象的类类型
     * @return 反序列化后的Java对象
     */
    <T> T deserialize(byte[] bytes, boolean snakeCase, Class<T> clazz);

    /**
     * 将JSON字符串反序列化为指定泛型类型的Java对象。
     *
     * @param <T>     目标对象类型
     * @param jsonStr JSON字符串，不能为空
     * @param typeRef 泛型类型引用，用于处理复杂泛型类型
     * @return 反序列化后的Java对象
     */
    <T> T deserialize(String jsonStr, TypeReferenceWrapper<T> typeRef);

    /**
     * 将JSON字符串反序列化为指定泛型类型的Java对象，支持蛇形命名转换。
     *
     * @param <T>       目标对象类型
     * @param jsonStr   JSON字符串，不能为空
     * @param snakeCase 是否将蛇形命名转换为驼峰命名
     * @param typeRef   泛型类型引用，用于处理复杂泛型类型
     * @return 反序列化后的Java对象
     */
    <T> T deserialize(String jsonStr, boolean snakeCase, TypeReferenceWrapper<T> typeRef);

    /**
     * 将JSON字节数组反序列化为指定泛型类型的Java对象。
     *
     * @param <T>     目标对象类型
     * @param bytes   JSON字节数组，不能为空
     * @param typeRef 泛型类型引用，用于处理复杂泛型类型
     * @return 反序列化后的Java对象
     */
    <T> T deserialize(byte[] bytes, TypeReferenceWrapper<T> typeRef);

    /**
     * 将JSON字节数组反序列化为指定泛型类型的Java对象，支持蛇形命名转换。
     *
     * @param <T>       目标对象类型
     * @param bytes     JSON字节数组，不能为空
     * @param snakeCase 是否将蛇形命名转换为驼峰命名
     * @param typeRef   泛型类型引用，用于处理复杂泛型类型
     * @return 反序列化后的Java对象
     */
    <T> T deserialize(byte[] bytes, boolean snakeCase, TypeReferenceWrapper<T> typeRef);
}
