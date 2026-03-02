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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import net.ymate.platform.commons.json.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * FastJSON对象包装器实现，基于Alibaba FastJSON的JSONObject提供JSON对象操作功能。
 * <p>
 * 设计目的：封装FastJSON JSONObject的使用细节，提供统一的JSON对象操作接口实现。
 * <p>
 * 使用场景：
 * - 当需要使用FastJSON进行JSON对象操作时
 * - 当需要统一的JSON对象操作接口时
 * - 当需要支持链式调用的JSON对象操作时
 *
 * @author 刘镇 (suninformation@163.com) on 2020/6/8 5:12 下午
 * @since 2.1.0
 */
public class FastJsonObjectWrapper implements IJsonObjectWrapper {

    private final IJsonAdapter adapter;

    private final JSONObject jsonObject;

    /**
     * 构造函数，创建一个空的JSON对象包装器。
     *
     * @param adapter JSON适配器实例，不能为空
     */
    public FastJsonObjectWrapper(IJsonAdapter adapter) {
        this(adapter, false);
    }

    /**
     * 构造函数，创建一个指定是否有序的JSON对象包装器。
     *
     * @param adapter JSON适配器实例，不能为空
     * @param ordered 是否有序，true表示保持插入顺序
     */
    public FastJsonObjectWrapper(IJsonAdapter adapter, boolean ordered) {
        this.adapter = adapter;
        jsonObject = new JSONObject(ordered);
    }

    /**
     * 构造函数，创建一个指定初始容量的JSON对象包装器。
     *
     * @param adapter         JSON适配器实例，不能为空
     * @param initialCapacity 初始容量，必须大于等于0
     */
    public FastJsonObjectWrapper(IJsonAdapter adapter, int initialCapacity) {
        this(adapter, initialCapacity, false);
    }

    /**
     * 构造函数，创建一个指定初始容量和有序性的JSON对象包装器。
     *
     * @param adapter         JSON适配器实例，不能为空
     * @param initialCapacity 初始容量，必须大于等于0
     * @param ordered         是否有序，true表示保持插入顺序
     */
    public FastJsonObjectWrapper(IJsonAdapter adapter, int initialCapacity, boolean ordered) {
        this.adapter = adapter;
        jsonObject = new JSONObject(initialCapacity, ordered);
    }

    /**
     * 构造函数，根据Map创建JSON对象包装器。
     *
     * @param adapter JSON适配器实例，不能为空
     * @param map     用于创建JSON对象的Map，键值对会被转换为JSON属性
     */
    public FastJsonObjectWrapper(IJsonAdapter adapter, Map<?, ?> map) {
        this.adapter = adapter;
        if (map == null) {
            jsonObject = new JSONObject();
        } else {
            jsonObject = new JSONObject(map instanceof LinkedHashMap);
            map.forEach((key, value) -> put(String.valueOf(key), value));
        }
    }

    /**
     * 构造函数，根据FastJSON的JSONObject创建JSON对象包装器。
     *
     * @param adapter    JSON适配器实例，不能为空
     * @param jsonObject FastJSON的JSONObject实例，若为null则创建空对象
     */
    public FastJsonObjectWrapper(IJsonAdapter adapter, JSONObject jsonObject) {
        this.adapter = adapter;
        this.jsonObject = jsonObject != null ? jsonObject : new JSONObject();
    }

    /**
     * 获取指定键的JSON节点包装器。
     *
     * @param key JSON属性键名，不能为空
     * @return JSON节点包装器，若键不存在则返回null
     */
    @Override
    public IJsonNodeWrapper get(String key) {
        Object object = jsonObject.get(key);
        return object != null ? new FastJsonNodeWrapper(adapter, object) : null;
    }

    /**
     * 获取指定键的布尔值。
     *
     * @param key JSON属性键名，不能为空
     * @return 布尔值，若键不存在或类型不匹配则返回false
     */
    @Override
    public boolean getBoolean(String key) {
        return jsonObject.getBooleanValue(key);
    }

    /**
     * 获取指定键的布尔值，支持null。
     *
     * @param key JSON属性键名，不能为空
     * @return 布尔值包装类，若键不存在则返回null
     */
    @Override
    public Boolean getAsBoolean(String key) {
        return jsonObject.getBoolean(key);
    }

    /**
     * 获取指定键的大整数。
     *
     * @param key JSON属性键名，不能为空
     * @return 大整数值，若键不存在或类型不匹配则返回null
     */
    @Override
    public BigInteger getBigInteger(String key) {
        return jsonObject.getBigInteger(key);
    }

    /**
     * 获取指定键的大小数。
     *
     * @param key JSON属性键名，不能为空
     * @return 大小数值，若键不存在或类型不匹配则返回null
     */
    @Override
    public BigDecimal getBigDecimal(String key) {
        return jsonObject.getBigDecimal(key);
    }

    /**
     * 获取指定键的双精度浮点数。
     *
     * @param key JSON属性键名，不能为空
     * @return 双精度浮点数，若键不存在或类型不匹配则返回0.0
     */
    @Override
    public double getDouble(String key) {
        return jsonObject.getDoubleValue(key);
    }

    /**
     * 获取指定键的双精度浮点数，支持null。
     *
     * @param key JSON属性键名，不能为空
     * @return 双精度浮点数包装类，若键不存在则返回null
     */
    @Override
    public Double getAsDouble(String key) {
        return jsonObject.getDouble(key);
    }

    /**
     * 获取指定键的单精度浮点数。
     *
     * @param key JSON属性键名，不能为空
     * @return 单精度浮点数，若键不存在或类型不匹配则返回0.0f
     */
    @Override
    public float getFloat(String key) {
        return jsonObject.getFloatValue(key);
    }

    /**
     * 获取指定键的单精度浮点数，支持null。
     *
     * @param key JSON属性键名，不能为空
     * @return 单精度浮点数包装类，若键不存在则返回null
     */
    @Override
    public Float getAsFloat(String key) {
        return jsonObject.getFloat(key);
    }

    /**
     * 获取指定键的整数。
     *
     * @param key JSON属性键名，不能为空
     * @return 整数值，若键不存在或类型不匹配则返回0
     */
    @Override
    public int getInt(String key) {
        return jsonObject.getIntValue(key);
    }

    /**
     * 获取指定键的整数，支持null。
     *
     * @param key JSON属性键名，不能为空
     * @return 整数包装类，若键不存在则返回null
     */
    @Override
    public Integer getAsInteger(String key) {
        return jsonObject.getInteger(key);
    }

    /**
     * 获取指定键的JSON数组包装器。
     *
     * @param key JSON属性键名，不能为空
     * @return JSON数组包装器，若键不存在或类型不匹配则返回null
     */
    @Override
    public IJsonArrayWrapper getJsonArray(String key) {
        JSONArray value = jsonObject.getJSONArray(key);
        return value == null ? null : new FastJsonArrayWrapper(adapter, value);
    }

    /**
     * 获取指定键的JSON对象包装器。
     *
     * @param key JSON属性键名，不能为空
     * @return JSON对象包装器，若键不存在或类型不匹配则返回null
     */
    @Override
    public IJsonObjectWrapper getJsonObject(String key) {
        JSONObject value = jsonObject.getJSONObject(key);
        return value == null ? null : new FastJsonObjectWrapper(adapter, value);
    }

    /**
     * 获取指定键的长整数。
     *
     * @param key JSON属性键名，不能为空
     * @return 长整数值，若键不存在或类型不匹配则返回0L
     */
    @Override
    public long getLong(String key) {
        return jsonObject.getLongValue(key);
    }

    /**
     * 获取指定键的长整数，支持null。
     *
     * @param key JSON属性键名，不能为空
     * @return 长整数包装类，若键不存在则返回null
     */
    @Override
    public Long getAsLong(String key) {
        return jsonObject.getLong(key);
    }

    /**
     * 获取指定键的字符串值。
     *
     * @param key JSON属性键名，不能为空
     * @return 字符串值，若键不存在则返回null
     */
    @Override
    public String getString(String key) {
        return jsonObject.getString(key);
    }

    /**
     * 检查是否包含指定键。
     *
     * @param key JSON属性键名，不能为空
     * @return 若包含指定键则返回true，否则返回false
     */
    @Override
    public boolean has(String key) {
        return jsonObject.containsKey(key);
    }

    /**
     * 获取所有键的集合。
     *
     * @return 键集合，不为null
     */
    @Override
    public Set<String> keySet() {
        return jsonObject.keySet();
    }

    /**
     * 获取JSON对象的大小。
     *
     * @return JSON对象中键值对的数量
     */
    @Override
    public int size() {
        return jsonObject.size();
    }

    /**
     * 检查JSON对象是否为空。
     *
     * @return 若JSON对象为空则返回true，否则返回false
     */
    @Override
    public boolean isEmpty() {
        return jsonObject.isEmpty();
    }

    /**
     * 设置布尔值属性，支持链式调用。
     *
     * @param key   JSON属性键名，不能为空
     * @param value 布尔值
     * @return 当前JSON对象包装器实例，用于链式调用
     */
    @Override
    public IJsonObjectWrapper put(String key, boolean value) {
        jsonObject.put(key, value);
        return this;
    }

    /**
     * 设置集合属性，支持链式调用。
     *
     * @param key   JSON属性键名，不能为空
     * @param value 集合对象
     * @return 当前JSON对象包装器实例，用于链式调用
     */
    @Override
    public IJsonObjectWrapper put(String key, Collection<?> value) {
        jsonObject.put(key, FastJsonAdapter.toJsonArray(value));
        return this;
    }

    /**
     * 设置双精度浮点数属性，支持链式调用。
     *
     * @param key   JSON属性键名，不能为空
     * @param value 双精度浮点数
     * @return 当前JSON对象包装器实例，用于链式调用
     */
    @Override
    public IJsonObjectWrapper put(String key, double value) {
        jsonObject.put(key, value);
        return this;
    }

    /**
     * 设置单精度浮点数属性，支持链式调用。
     *
     * @param key   JSON属性键名，不能为空
     * @param value 单精度浮点数
     * @return 当前JSON对象包装器实例，用于链式调用
     */
    @Override
    public IJsonObjectWrapper put(String key, float value) {
        jsonObject.put(key, value);
        return this;
    }

    /**
     * 设置整数属性，支持链式调用。
     *
     * @param key   JSON属性键名，不能为空
     * @param value 整数值
     * @return 当前JSON对象包装器实例，用于链式调用
     */
    @Override
    public IJsonObjectWrapper put(String key, int value) {
        jsonObject.put(key, value);
        return this;
    }

    /**
     * 设置长整数属性，支持链式调用。
     *
     * @param key   JSON属性键名，不能为空
     * @param value 长整数值
     * @return 当前JSON对象包装器实例，用于链式调用
     */
    @Override
    public IJsonObjectWrapper put(String key, long value) {
        jsonObject.put(key, value);
        return this;
    }

    /**
     * 设置Map属性，支持链式调用。
     *
     * @param key   JSON属性键名，不能为空
     * @param value Map对象
     * @return 当前JSON对象包装器实例，用于链式调用
     */
    @Override
    public IJsonObjectWrapper put(String key, Map<?, ?> value) {
        jsonObject.put(key, FastJsonAdapter.toJsonObject(value));
        return this;
    }

    /**
     * 设置任意类型属性，支持链式调用。
     *
     * @param key   JSON属性键名，不能为空
     * @param value 任意类型值
     * @return 当前JSON对象包装器实例，用于链式调用
     */
    @Override
    public IJsonObjectWrapper put(String key, Object value) {
        jsonObject.put(key, JsonWrapper.unwrap(value));
        return this;
    }

    /**
     * 移除指定键的属性。
     *
     * @param key JSON属性键名，不能为空
     * @return 被移除的值，若键不存在则返回null
     */
    @Override
    public Object remove(String key) {
        return jsonObject.remove(key);
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
        FastJsonObjectWrapper that = (FastJsonObjectWrapper) o;
        return new EqualsBuilder()
                .append(jsonObject, that.jsonObject)
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
                .append(jsonObject)
                .toHashCode();
    }

    /**
     * 将当前JSON对象转换为字符串（默认紧凑输出）。
     *
     * @return 转换后的JSON字符串
     */
    @Override
    public String toString() {
        return this.toString(false, false);
    }

    /**
     * 将当前JSON对象转换为字符串，支持格式化输出和保留空值。
     *
     * @param format        是否格式化输出，true表示格式化（带缩进），false表示紧凑输出
     * @param keepNullValue 是否保留空值，true表示保留，false表示忽略
     * @return 转换后的JSON字符串
     */
    @Override
    public String toString(boolean format, boolean keepNullValue) {
        return adapter.toJsonString(jsonObject, format, keepNullValue);
    }

    /**
     * 将当前JSON对象转换为字符串，支持格式化输出、保留空值和蛇形命名转换。
     *
     * @param format        是否格式化输出，true表示格式化（带缩进），false表示紧凑输出
     * @param keepNullValue 是否保留空值，true表示保留，false表示忽略
     * @param snakeCase     是否将驼峰命名转换为蛇形命名
     * @return 转换后的JSON字符串
     */
    @Override
    public String toString(boolean format, boolean keepNullValue, boolean snakeCase) {
        return adapter.toJsonString(jsonObject, format, keepNullValue, snakeCase);
    }

    /**
     * 将当前JSON对象转换为字符串，支持格式化输出、保留空值和属性过滤。
     *
     * @param format        是否格式化输出，true表示格式化（带缩进），false表示紧凑输出
     * @param keepNullValue 是否保留空值，true表示保留，false表示忽略
     * @param filter        属性过滤器，用于控制哪些属性被序列化
     * @return 转换后的JSON字符串
     * @since 2.1.4
     */
    @Override
    public String toString(boolean format, boolean keepNullValue, IJsonPropertyFilter filter) {
        return adapter.toJsonString(jsonObject, format, keepNullValue, filter);
    }

    /**
     * 将当前JSON对象转换为字符串，支持格式化输出、保留空值、蛇形命名转换和属性过滤。
     *
     * @param format        是否格式化输出，true表示格式化（带缩进），false表示紧凑输出
     * @param keepNullValue 是否保留空值，true表示保留，false表示忽略
     * @param snakeCase     是否将驼峰命名转换为蛇形命名
     * @param filter        属性过滤器，用于控制哪些属性被序列化
     * @return 转换后的JSON字符串
     * @since 2.1.4
     */
    @Override
    public String toString(boolean format, boolean keepNullValue, boolean snakeCase, IJsonPropertyFilter filter) {
        return adapter.toJsonString(jsonObject, format, keepNullValue, snakeCase, filter);
    }

    /**
     * 将JSON对象转换为Map。
     *
     * @return Map对象，包含JSON对象的所有键值对
     */
    @Override
    public Map<String, Object> toMap() {
        return jsonObject;
    }

    /**
     * 将当前JSON对象包装器转换为JsonWrapper对象。
     *
     * @return JsonWrapper对象，包含当前JSON对象包装器
     * @since 2.1.4
     */
    @Override
    public JsonWrapper wrap() {
        return new JsonWrapper(this);
    }
}
