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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.ymate.platform.commons.json.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * Jackson对象包装器实现，基于Jackson的ObjectNode提供JSON对象操作功能。
 * <p>
 * 设计目的：封装Jackson ObjectNode的使用细节，提供统一的JSON对象操作接口实现。
 * <p>
 * 使用场景：
 * - 当需要使用Jackson进行JSON对象操作时
 * - 当需要统一的JSON对象操作接口时
 * - 当需要支持链式调用的JSON对象操作时
 *
 * @author 刘镇 (suninformation@163.com) on 2020/6/11 3:40 下午
 * @since 2.1.0
 */
public class JacksonObjectWrapper implements IJsonObjectWrapper {

    private final IJsonAdapter adapter;

    private final ObjectNode objectNode;

    /**
     * 构造函数，创建一个空的JSON对象包装器。
     *
     * @param adapter JSON适配器实例，不能为空
     */
    public JacksonObjectWrapper(IJsonAdapter adapter) {
        this.adapter = adapter;
        this.objectNode = JacksonAdapter.OBJECT_MAPPER.createObjectNode();
    }

    /**
     * 构造函数，根据Map创建JSON对象包装器。
     *
     * @param adapter JSON适配器实例，不能为空
     * @param map     用于创建JSON对象的Map，键值对会被转换为JSON属性
     */
    public JacksonObjectWrapper(IJsonAdapter adapter, Map<?, ?> map) {
        this(adapter);
        if (map != null && !map.isEmpty()) {
            map.forEach((key, value) -> put(String.valueOf(key), value));
        }
    }

    /**
     * 构造函数，根据Jackson ObjectNode创建JSON对象包装器。
     *
     * @param adapter    JSON适配器实例，不能为空
     * @param objectNode Jackson ObjectNode实例，若为null则创建空对象
     */
    public JacksonObjectWrapper(IJsonAdapter adapter, ObjectNode objectNode) {
        this.adapter = adapter;
        this.objectNode = objectNode != null ? objectNode : JacksonAdapter.OBJECT_MAPPER.createObjectNode();
    }

    /**
     * 获取指定键的JSON节点包装器。
     *
     * @param key JSON属性键名，不能为空
     * @return JSON节点包装器，若键不存在则返回null
     */
    @Override
    public IJsonNodeWrapper get(String key) {
        JsonNode jsonNode = objectNode.get(key);
        return jsonNode != null ? new JacksonNodeWrapper(adapter, jsonNode) : null;
    }

    /**
     * 获取指定键的布尔值。
     *
     * @param key JSON属性键名，不能为空
     * @return 布尔值，若键不存在或类型不匹配则返回false
     */
    @Override
    public boolean getBoolean(String key) {
        IJsonNodeWrapper jsonNode = get(key);
        return jsonNode != null && jsonNode.getBoolean();
    }

    /**
     * 获取指定键的布尔值，支持null。
     *
     * @param key JSON属性键名，不能为空
     * @return 布尔值包装类，若键不存在则返回null
     */
    @Override
    public Boolean getAsBoolean(String key) {
        IJsonNodeWrapper jsonNode = get(key);
        if (jsonNode != null) {
            return jsonNode.getBoolean();
        }
        return null;
    }

    /**
     * 获取指定键的大整数。
     *
     * @param key JSON属性键名，不能为空
     * @return 大整数值，若键不存在或类型不匹配则返回null
     */
    @Override
    public BigInteger getBigInteger(String key) {
        IJsonNodeWrapper jsonNode = get(key);
        return jsonNode != null ? jsonNode.getBigInteger() : null;
    }

    /**
     * 获取指定键的大小数。
     *
     * @param key JSON属性键名，不能为空
     * @return 大小数值，若键不存在或类型不匹配则返回null
     */
    @Override
    public BigDecimal getBigDecimal(String key) {
        IJsonNodeWrapper jsonNode = get(key);
        return jsonNode != null ? jsonNode.getBigDecimal() : null;
    }

    /**
     * 获取指定键的双精度浮点数。
     *
     * @param key JSON属性键名，不能为空
     * @return 双精度浮点数，若键不存在或类型不匹配则返回0.0
     */
    @Override
    public double getDouble(String key) {
        IJsonNodeWrapper jsonNode = get(key);
        return jsonNode != null ? jsonNode.getDouble() : 0d;
    }

    /**
     * 获取指定键的双精度浮点数，支持null。
     *
     * @param key JSON属性键名，不能为空
     * @return 双精度浮点数包装类，若键不存在则返回null
     */
    @Override
    public Double getAsDouble(String key) {
        IJsonNodeWrapper jsonNode = get(key);
        if (jsonNode != null) {
            return jsonNode.getDouble();
        }
        return null;
    }

    /**
     * 获取指定键的单精度浮点数。
     *
     * @param key JSON属性键名，不能为空
     * @return 单精度浮点数，若键不存在或类型不匹配则返回0.0f
     */
    @Override
    public float getFloat(String key) {
        IJsonNodeWrapper jsonNode = get(key);
        return jsonNode != null ? jsonNode.getFloat() : 0f;
    }

    /**
     * 获取指定键的单精度浮点数，支持null。
     *
     * @param key JSON属性键名，不能为空
     * @return 单精度浮点数包装类，若键不存在则返回null
     */
    @Override
    public Float getAsFloat(String key) {
        IJsonNodeWrapper jsonNode = get(key);
        if (jsonNode != null) {
            return jsonNode.getFloat();
        }
        return null;
    }

    /**
     * 获取指定键的整数。
     *
     * @param key JSON属性键名，不能为空
     * @return 整数值，若键不存在或类型不匹配则返回0
     */
    @Override
    public int getInt(String key) {
        IJsonNodeWrapper jsonNode = get(key);
        return jsonNode != null ? jsonNode.getInt() : 0;
    }

    /**
     * 获取指定键的整数，支持null。
     *
     * @param key JSON属性键名，不能为空
     * @return 整数包装类，若键不存在则返回null
     */
    @Override
    public Integer getAsInteger(String key) {
        IJsonNodeWrapper jsonNode = get(key);
        if (jsonNode != null) {
            return jsonNode.getInt();
        }
        return null;
    }

    /**
     * 获取指定键的JSON数组包装器。
     *
     * @param key JSON属性键名，不能为空
     * @return JSON数组包装器，若键不存在或类型不匹配则返回null
     */
    @Override
    public IJsonArrayWrapper getJsonArray(String key) {
        IJsonNodeWrapper jsonNode = get(key);
        return jsonNode != null ? jsonNode.getJsonArray() : null;
    }

    /**
     * 获取指定键的JSON对象包装器。
     *
     * @param key JSON属性键名，不能为空
     * @return JSON对象包装器，若键不存在或类型不匹配则返回null
     */
    @Override
    public IJsonObjectWrapper getJsonObject(String key) {
        IJsonNodeWrapper jsonNode = get(key);
        return jsonNode != null ? jsonNode.getJsonObject() : null;
    }

    /**
     * 获取指定键的长整数。
     *
     * @param key JSON属性键名，不能为空
     * @return 长整数值，若键不存在或类型不匹配则返回0L
     */
    @Override
    public long getLong(String key) {
        IJsonNodeWrapper jsonNode = get(key);
        return jsonNode != null ? jsonNode.getLong() : 0L;
    }

    /**
     * 获取指定键的长整数，支持null。
     *
     * @param key JSON属性键名，不能为空
     * @return 长整数包装类，若键不存在则返回null
     */
    @Override
    public Long getAsLong(String key) {
        IJsonNodeWrapper jsonNode = get(key);
        if (jsonNode != null) {
            return jsonNode.getLong();
        }
        return null;
    }

    /**
     * 获取指定键的字符串值。
     *
     * @param key JSON属性键名，不能为空
     * @return 字符串值，若键不存在则返回null
     */
    @Override
    public String getString(String key) {
        IJsonNodeWrapper jsonNode = get(key);
        return jsonNode != null ? jsonNode.getString() : null;
    }

    /**
     * 检查是否包含指定键。
     *
     * @param key JSON属性键名，不能为空
     * @return 若包含指定键则返回true，否则返回false
     */
    @Override
    public boolean has(String key) {
        return objectNode.has(key);
    }

    /**
     * 获取所有键的集合。
     *
     * @return 键集合，不为null
     */
    @Override
    public Set<String> keySet() {
        Set<String> keySet = new LinkedHashSet<>(objectNode.size());
        Iterator<String> iterator = objectNode.fieldNames();
        while (iterator.hasNext()) {
            keySet.add(iterator.next());
        }
        return keySet;
    }

    /**
     * 获取JSON对象的大小。
     *
     * @return JSON对象中键值对的数量
     */
    @Override
    public int size() {
        return objectNode.size();
    }

    /**
     * 检查JSON对象是否为空。
     *
     * @return 若JSON对象为空则返回true，否则返回false
     */
    @Override
    public boolean isEmpty() {
        return objectNode.isEmpty();
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
        objectNode.put(key, value);
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
        objectNode.set(key, JacksonAdapter.toArrayNode(value));
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
        objectNode.put(key, value);
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
        objectNode.put(key, value);
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
        objectNode.put(key, value);
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
        objectNode.put(key, value);
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
        objectNode.set(key, JacksonAdapter.toObjectNode(value));
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
        objectNode.set(key, JacksonAdapter.toJsonNode(value));
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
        return objectNode.remove(key);
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
        JacksonObjectWrapper that = (JacksonObjectWrapper) o;
        return new EqualsBuilder()
                .append(objectNode, that.objectNode)
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
                .append(objectNode)
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
        return adapter.toJsonString(objectNode, format, keepNullValue);
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
        return adapter.toJsonString(objectNode, format, keepNullValue, snakeCase);
    }

    /**
     * 将当前JSON对象转换为字符串，支持格式化输出、保留空值和属性过滤。
     *
     * @param format        是否格式化输出，true表示格式化（带缩进），false表示紧凑输出
     * @param keepNullValue 是否保留空值，true表示保留，false表示忽略
     * @param filter        属性过滤器，用于控制哪些属性被序列化
     * @return 转换后的JSON字符串
     */
    @Override
    public String toString(boolean format, boolean keepNullValue, IJsonPropertyFilter filter) {
        return adapter.toJsonString(objectNode, format, keepNullValue, filter);
    }

    /**
     * 将当前JSON对象转换为字符串，支持格式化输出、保留空值、蛇形命名转换和属性过滤。
     *
     * @param format        是否格式化输出，true表示格式化（带缩进），false表示紧凑输出
     * @param keepNullValue 是否保留空值，true表示保留，false表示忽略
     * @param snakeCase     是否将驼峰命名转换为蛇形命名
     * @param filter        属性过滤器，用于控制哪些属性被序列化
     * @return 转换后的JSON字符串
     */
    @Override
    public String toString(boolean format, boolean keepNullValue, boolean snakeCase, IJsonPropertyFilter filter) {
        return adapter.toJsonString(objectNode, format, keepNullValue, snakeCase, filter);
    }

    /**
     * 将JSON对象转换为Map。
     *
     * @return Map对象，包含JSON对象的所有键值对
     */
    @Override
    public Map<String, Object> toMap() {
        return JacksonAdapter.OBJECT_MAPPER.convertValue(objectNode, new TypeReference<Map<String, Object>>() {
        });
    }

    /**
     * 将当前JSON对象包装器转换为JsonWrapper对象。
     *
     * @return JsonWrapper对象，包含当前JSON对象包装器
     */
    @Override
    public JsonWrapper wrap() {
        return new JsonWrapper(this);
    }
}
