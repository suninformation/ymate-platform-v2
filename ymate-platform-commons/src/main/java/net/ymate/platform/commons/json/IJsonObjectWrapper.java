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

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * JSON对象包装器接口，用于封装不同JSON库的对象实现，提供统一的JSON对象操作方法。
 * <p>
 * 设计目的：实现JSON对象操作的统一抽象，降低对具体JSON库的依赖，提高代码的可移植性和扩展性。
 * <p>
 * 使用场景：
 * - 需要对JSON对象进行统一的创建、读取、修改和转换操作
 * - 希望通过统一的API操作不同JSON库的对象实现
 * - 需要支持多种JSON格式配置，如格式化输出、空值处理、命名转换等
 *
 * @author 刘镇 (suninformation@163.com) on 2020/6/9 3:35 下午
 * @since 2.1.0
 */
public interface IJsonObjectWrapper extends Serializable {

    /**
     * 获取指定键的JSON节点包装器。
     *
     * @param key JSON属性键名，不能为空
     * @return JSON节点包装器，若键不存在则返回null
     */
    IJsonNodeWrapper get(String key);

    /**
     * 获取指定键的布尔值。
     *
     * @param key JSON属性键名，不能为空
     * @return 布尔值，若键不存在或类型不匹配则返回false
     */
    boolean getBoolean(String key);

    /**
     * 获取指定键的布尔值，支持null。
     *
     * @param key JSON属性键名，不能为空
     * @return 布尔值包装类，若键不存在则返回null
     */
    Boolean getAsBoolean(String key);

    /**
     * 获取指定键的大整数。
     *
     * @param key JSON属性键名，不能为空
     * @return 大整数值，若键不存在或类型不匹配则返回null
     */
    BigInteger getBigInteger(String key);

    /**
     * 获取指定键的大小数。
     *
     * @param key JSON属性键名，不能为空
     * @return 大小数值，若键不存在或类型不匹配则返回null
     */
    BigDecimal getBigDecimal(String key);

    /**
     * 获取指定键的双精度浮点数。
     *
     * @param key JSON属性键名，不能为空
     * @return 双精度浮点数，若键不存在或类型不匹配则返回0.0
     */
    double getDouble(String key);

    /**
     * 获取指定键的双精度浮点数，支持null。
     *
     * @param key JSON属性键名，不能为空
     * @return 双精度浮点数包装类，若键不存在则返回null
     */
    Double getAsDouble(String key);

    /**
     * 获取指定键的单精度浮点数。
     *
     * @param key JSON属性键名，不能为空
     * @return 单精度浮点数，若键不存在或类型不匹配则返回0.0f
     */
    float getFloat(String key);

    /**
     * 获取指定键的单精度浮点数，支持null。
     *
     * @param key JSON属性键名，不能为空
     * @return 单精度浮点数包装类，若键不存在则返回null
     */
    Float getAsFloat(String key);

    /**
     * 获取指定键的整数。
     *
     * @param key JSON属性键名，不能为空
     * @return 整数值，若键不存在或类型不匹配则返回0
     */
    int getInt(String key);

    /**
     * 获取指定键的整数，支持null。
     *
     * @param key JSON属性键名，不能为空
     * @return 整数包装类，若键不存在则返回null
     */
    Integer getAsInteger(String key);

    /**
     * 获取指定键的JSON数组包装器。
     *
     * @param key JSON属性键名，不能为空
     * @return JSON数组包装器，若键不存在或类型不匹配则返回null
     */
    IJsonArrayWrapper getJsonArray(String key);

    /**
     * 获取指定键的JSON对象包装器。
     *
     * @param key JSON属性键名，不能为空
     * @return JSON对象包装器，若键不存在或类型不匹配则返回null
     */
    IJsonObjectWrapper getJsonObject(String key);

    /**
     * 获取指定键的长整数。
     *
     * @param key JSON属性键名，不能为空
     * @return 长整数值，若键不存在或类型不匹配则返回0L
     */
    long getLong(String key);

    /**
     * 获取指定键的长整数，支持null。
     *
     * @param key JSON属性键名，不能为空
     * @return 长整数包装类，若键不存在则返回null
     */
    Long getAsLong(String key);

    /**
     * 获取指定键的字符串值。
     *
     * @param key JSON属性键名，不能为空
     * @return 字符串值，若键不存在则返回null
     */
    String getString(String key);

    /**
     * 检查是否包含指定键。
     *
     * @param key JSON属性键名，不能为空
     * @return 若包含指定键则返回true，否则返回false
     */
    boolean has(String key);

    /**
     * 获取所有键的集合。
     *
     * @return 键集合，不为null
     */
    Set<String> keySet();

    /**
     * 获取JSON对象的大小。
     *
     * @return JSON对象中键值对的数量
     */
    int size();

    /**
     * 检查JSON对象是否为空。
     *
     * @return 若JSON对象为空则返回true，否则返回false
     */
    boolean isEmpty();

    /**
     * 设置布尔值属性，支持链式调用。
     *
     * @param key   JSON属性键名，不能为空
     * @param value 布尔值
     * @return 当前JSON对象包装器实例，用于链式调用
     */
    IJsonObjectWrapper put(String key, boolean value);

    /**
     * 设置集合属性，支持链式调用。
     *
     * @param key   JSON属性键名，不能为空
     * @param value 集合对象
     * @return 当前JSON对象包装器实例，用于链式调用
     */
    IJsonObjectWrapper put(String key, Collection<?> value);

    /**
     * 设置双精度浮点数属性，支持链式调用。
     *
     * @param key   JSON属性键名，不能为空
     * @param value 双精度浮点数
     * @return 当前JSON对象包装器实例，用于链式调用
     */
    IJsonObjectWrapper put(String key, double value);

    /**
     * 设置单精度浮点数属性，支持链式调用。
     *
     * @param key   JSON属性键名，不能为空
     * @param value 单精度浮点数
     * @return 当前JSON对象包装器实例，用于链式调用
     */
    IJsonObjectWrapper put(String key, float value);

    /**
     * 设置整数属性，支持链式调用。
     *
     * @param key   JSON属性键名，不能为空
     * @param value 整数值
     * @return 当前JSON对象包装器实例，用于链式调用
     */
    IJsonObjectWrapper put(String key, int value);

    /**
     * 设置长整数属性，支持链式调用。
     *
     * @param key   JSON属性键名，不能为空
     * @param value 长整数值
     * @return 当前JSON对象包装器实例，用于链式调用
     */
    IJsonObjectWrapper put(String key, long value);

    /**
     * 设置Map属性，支持链式调用。
     *
     * @param key   JSON属性键名，不能为空
     * @param value Map对象
     * @return 当前JSON对象包装器实例，用于链式调用
     */
    IJsonObjectWrapper put(String key, Map<?, ?> value);

    /**
     * 设置任意类型属性，支持链式调用。
     *
     * @param key   JSON属性键名，不能为空
     * @param value 任意类型值
     * @return 当前JSON对象包装器实例，用于链式调用
     */
    IJsonObjectWrapper put(String key, Object value);

    /**
     * 移除指定键的属性。
     *
     * @param key JSON属性键名，不能为空
     * @return 被移除的值，若键不存在则返回null
     */
    Object remove(String key);

    /**
     * 将JSON对象转换为字符串，支持格式化输出和保留空值。
     *
     * @param format        是否格式化输出，true表示格式化（带缩进），false表示紧凑输出
     * @param keepNullValue 是否保留空值，true表示保留，false表示忽略
     * @return 转换后的JSON字符串
     */
    String toString(boolean format, boolean keepNullValue);

    /**
     * 将JSON对象转换为字符串，支持格式化输出、保留空值和蛇形命名转换。
     *
     * @param format        是否格式化输出，true表示格式化（带缩进），false表示紧凑输出
     * @param keepNullValue 是否保留空值，true表示保留，false表示忽略
     * @param snakeCase     是否将驼峰命名转换为蛇形命名
     * @return 转换后的JSON字符串
     */
    String toString(boolean format, boolean keepNullValue, boolean snakeCase);

    /**
     * 将JSON对象转换为字符串，支持格式化输出、保留空值和属性过滤。
     *
     * @param format        是否格式化输出，true表示格式化（带缩进），false表示紧凑输出
     * @param keepNullValue 是否保留空值，true表示保留，false表示忽略
     * @param filter        属性过滤器，用于控制哪些属性被序列化
     * @return 转换后的JSON字符串
     * @since 2.1.4
     */
    String toString(boolean format, boolean keepNullValue, IJsonPropertyFilter filter);

    /**
     * 将JSON对象转换为字符串，支持格式化输出、保留空值、蛇形命名转换和属性过滤。
     *
     * @param format        是否格式化输出，true表示格式化（带缩进），false表示紧凑输出
     * @param keepNullValue 是否保留空值，true表示保留，false表示忽略
     * @param snakeCase     是否将驼峰命名转换为蛇形命名
     * @param filter        属性过滤器，用于控制哪些属性被序列化
     * @return 转换后的JSON字符串
     * @since 2.1.4
     */
    String toString(boolean format, boolean keepNullValue, boolean snakeCase, IJsonPropertyFilter filter);

    /**
     * 将JSON对象转换为Map。
     *
     * @return Map对象，包含JSON对象的所有键值对
     */
    Map<String, Object> toMap();

    /**
     * 将当前JSON对象包装器转换为JsonWrapper对象。
     *
     * @return JsonWrapper对象，包含当前JSON对象包装器
     * @since 2.1.4
     */
    JsonWrapper wrap();
}
