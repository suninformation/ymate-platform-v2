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
import com.fasterxml.jackson.databind.node.ArrayNode;
import net.ymate.platform.commons.json.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * Jackson数组包装器实现，基于Jackson的ArrayNode提供JSON数组操作功能。
 * <p>
 * 设计目的：封装Jackson ArrayNode的使用细节，提供统一的JSON数组操作接口实现。
 * <p>
 * 使用场景：
 * - 当需要使用Jackson进行JSON数组操作时
 * - 当需要统一的JSON数组操作接口时
 * - 当需要支持链式调用的JSON数组操作时
 *
 * @author 刘镇 (suninformation@163.com) on 2020/6/11 3:41 下午
 * @since 2.1.0
 */
public class JacksonArrayWrapper implements IJsonArrayWrapper {

    private final IJsonAdapter adapter;

    private final ArrayNode arrayNode;

    /**
     * 构造函数，创建一个空的JSON数组包装器。
     *
     * @param adapter JSON适配器实例，不能为空
     */
    public JacksonArrayWrapper(IJsonAdapter adapter) {
        this.adapter = adapter;
        this.arrayNode = JacksonAdapter.OBJECT_MAPPER.createArrayNode();
    }

    /**
     * 构造函数，根据Object数组创建JSON数组包装器。
     *
     * @param adapter JSON适配器实例，不能为空
     * @param array   用于创建JSON数组的Object数组，元素会被转换为JSON数组元素
     */
    public JacksonArrayWrapper(IJsonAdapter adapter, Object[] array) {
        this(adapter);
        Arrays.stream(array).forEach(this::add);
    }

    /**
     * 构造函数，根据Collection创建JSON数组包装器。
     *
     * @param adapter    JSON适配器实例，不能为空
     * @param collection 用于创建JSON数组的Collection，元素会被转换为JSON数组元素
     */
    public JacksonArrayWrapper(IJsonAdapter adapter, Collection<?> collection) {
        this(adapter);
        collection.forEach(this::add);
    }

    /**
     * 构造函数，根据Jackson ArrayNode创建JSON数组包装器。
     *
     * @param adapter   JSON适配器实例，不能为空
     * @param arrayNode Jackson ArrayNode实例，若为null则创建空数组
     */
    public JacksonArrayWrapper(IJsonAdapter adapter, ArrayNode arrayNode) {
        this.adapter = adapter;
        this.arrayNode = arrayNode != null ? arrayNode : JacksonAdapter.OBJECT_MAPPER.createArrayNode();
    }

    /**
     * 获取指定索引的JSON节点包装器。
     *
     * @param index 数组索引，必须大于等于0且小于数组长度
     * @return JSON节点包装器，若索引越界则返回null
     */
    @Override
    public IJsonNodeWrapper get(int index) {
        JsonNode jsonNode = arrayNode.get(index);
        return jsonNode != null ? new JacksonNodeWrapper(adapter, jsonNode) : null;
    }

    /**
     * 获取指定索引的布尔值。
     *
     * @param index 数组索引，必须大于等于0且小于数组长度
     * @return 布尔值，若索引越界或类型不匹配则返回false
     */
    @Override
    public boolean getBoolean(int index) {
        IJsonNodeWrapper jsonNode = get(index);
        return jsonNode != null && jsonNode.getBoolean();
    }

    /**
     * 获取指定索引的布尔值，支持null。
     *
     * @param index 数组索引，必须大于等于0且小于数组长度
     * @return 布尔值包装类，若索引越界则返回null
     */
    @Override
    public Boolean getAsBoolean(int index) {
        IJsonNodeWrapper jsonNode = get(index);
        if (jsonNode != null) {
            return jsonNode.getBoolean();
        }
        return null;
    }

    /**
     * 获取指定索引的双精度浮点数。
     *
     * @param index 数组索引，必须大于等于0且小于数组长度
     * @return 双精度浮点数，若索引越界或类型不匹配则返回0.0
     */
    @Override
    public double getDouble(int index) {
        IJsonNodeWrapper jsonNode = get(index);
        return jsonNode != null ? jsonNode.getDouble() : 0d;
    }

    /**
     * 获取指定索引的双精度浮点数，支持null。
     *
     * @param index 数组索引，必须大于等于0且小于数组长度
     * @return 双精度浮点数包装类，若索引越界则返回null
     */
    @Override
    public Double getAsDouble(int index) {
        IJsonNodeWrapper jsonNode = get(index);
        if (jsonNode != null) {
            return jsonNode.getDouble();
        }
        return null;
    }

    /**
     * 获取指定索引的单精度浮点数。
     *
     * @param index 数组索引，必须大于等于0且小于数组长度
     * @return 单精度浮点数，若索引越界或类型不匹配则返回0.0f
     */
    @Override
    public float getFloat(int index) {
        IJsonNodeWrapper jsonNode = get(index);
        return jsonNode != null ? jsonNode.getFloat() : 0f;
    }

    /**
     * 获取指定索引的单精度浮点数，支持null。
     *
     * @param index 数组索引，必须大于等于0且小于数组长度
     * @return 单精度浮点数包装类，若索引越界则返回null
     */
    @Override
    public Float getAsFloat(int index) {
        IJsonNodeWrapper jsonNode = get(index);
        if (jsonNode != null) {
            return jsonNode.getFloat();
        }
        return null;
    }

    /**
     * 获取指定索引的大小数。
     *
     * @param index 数组索引，必须大于等于0且小于数组长度
     * @return 大小数值，若索引越界或类型不匹配则返回null
     */
    @Override
    public BigDecimal getBigDecimal(int index) {
        IJsonNodeWrapper jsonNode = get(index);
        return jsonNode != null ? jsonNode.getBigDecimal() : null;
    }

    /**
     * 获取指定索引的大整数。
     *
     * @param index 数组索引，必须大于等于0且小于数组长度
     * @return 大整数值，若索引越界或类型不匹配则返回null
     */
    @Override
    public BigInteger getBigInteger(int index) {
        IJsonNodeWrapper jsonNode = get(index);
        return jsonNode != null ? jsonNode.getBigInteger() : null;
    }

    /**
     * 获取指定索引的整数。
     *
     * @param index 数组索引，必须大于等于0且小于数组长度
     * @return 整数值，若索引越界或类型不匹配则返回0
     */
    @Override
    public int getInt(int index) {
        IJsonNodeWrapper jsonNode = get(index);
        return jsonNode != null ? jsonNode.getInt() : 0;
    }

    /**
     * 获取指定索引的整数，支持null。
     *
     * @param index 数组索引，必须大于等于0且小于数组长度
     * @return 整数包装类，若索引越界则返回null
     */
    @Override
    public Integer getAsInteger(int index) {
        IJsonNodeWrapper jsonNode = get(index);
        if (jsonNode != null) {
            return jsonNode.getInt();
        }
        return null;
    }

    /**
     * 获取指定索引的JSON数组包装器。
     *
     * @param index 数组索引，必须大于等于0且小于数组长度
     * @return JSON数组包装器，若索引越界或类型不匹配则返回null
     */
    @Override
    public IJsonArrayWrapper getJsonArray(int index) {
        IJsonNodeWrapper jsonNode = get(index);
        return jsonNode != null ? jsonNode.getJsonArray() : null;
    }

    /**
     * 获取指定索引的JSON对象包装器。
     *
     * @param index 数组索引，必须大于等于0且小于数组长度
     * @return JSON对象包装器，若索引越界或类型不匹配则返回null
     */
    @Override
    public IJsonObjectWrapper getJsonObject(int index) {
        IJsonNodeWrapper jsonNode = get(index);
        return jsonNode != null ? jsonNode.getJsonObject() : null;
    }

    /**
     * 获取指定索引的长整数。
     *
     * @param index 数组索引，必须大于等于0且小于数组长度
     * @return 长整数值，若索引越界或类型不匹配则返回0L
     */
    @Override
    public long getLong(int index) {
        IJsonNodeWrapper jsonNode = get(index);
        return jsonNode != null ? jsonNode.getLong() : 0L;
    }

    /**
     * 获取指定索引的长整数，支持null。
     *
     * @param index 数组索引，必须大于等于0且小于数组长度
     * @return 长整数包装类，若索引越界则返回null
     */
    @Override
    public Long getAsLong(int index) {
        IJsonNodeWrapper jsonNode = get(index);
        if (jsonNode != null) {
            return jsonNode.getLong();
        }
        return null;
    }

    /**
     * 获取指定索引的字符串值。
     *
     * @param index 数组索引，必须大于等于0且小于数组长度
     * @return 字符串值，若索引越界则返回null
     */
    @Override
    public String getString(int index) {
        IJsonNodeWrapper jsonNode = get(index);
        return jsonNode != null ? jsonNode.getString() : null;
    }

    /**
     * 检查指定索引的元素是否为null。
     *
     * @param index 数组索引，必须大于等于0且小于数组长度
     * @return 若元素为null或索引越界则返回true，否则返回false
     */
    @Override
    public boolean isNull(int index) {
        IJsonNodeWrapper jsonNode = get(index);
        return jsonNode == null || jsonNode.isNull();
    }

    /**
     * 获取JSON数组的大小。
     *
     * @return JSON数组中元素的数量
     */
    @Override
    public int size() {
        return arrayNode.size();
    }

    /**
     * 检查JSON数组是否为空。
     *
     * @return 若JSON数组为空则返回true，否则返回false
     */
    @Override
    public boolean isEmpty() {
        return arrayNode.isEmpty();
    }

    /**
     * 添加布尔值元素到数组，支持链式调用。
     *
     * @param value 布尔值
     * @return 当前JSON数组包装器实例，用于链式调用
     */
    @Override
    public IJsonArrayWrapper add(boolean value) {
        arrayNode.add(value);
        return this;
    }

    /**
     * 添加集合元素到数组，支持链式调用。
     *
     * @param value 集合对象，元素会被转换为JSON数组元素
     * @return 当前JSON数组包装器实例，用于链式调用
     */
    @Override
    public IJsonArrayWrapper add(Collection<?> value) {
        arrayNode.add(JacksonAdapter.toArrayNode(value));
        return this;
    }

    /**
     * 添加双精度浮点数元素到数组，支持链式调用。
     *
     * @param value 双精度浮点数
     * @return 当前JSON数组包装器实例，用于链式调用
     */
    @Override
    public IJsonArrayWrapper add(double value) {
        arrayNode.add(value);
        return this;
    }

    /**
     * 添加单精度浮点数元素到数组，支持链式调用。
     *
     * @param value 单精度浮点数
     * @return 当前JSON数组包装器实例，用于链式调用
     */
    @Override
    public IJsonArrayWrapper add(float value) {
        arrayNode.add(value);
        return this;
    }

    /**
     * 添加整数元素到数组，支持链式调用。
     *
     * @param value 整数值
     * @return 当前JSON数组包装器实例，用于链式调用
     */
    @Override
    public IJsonArrayWrapper add(int value) {
        arrayNode.add(value);
        return this;
    }

    /**
     * 添加长整数元素到数组，支持链式调用。
     *
     * @param value 长整数值
     * @return 当前JSON数组包装器实例，用于链式调用
     */
    @Override
    public IJsonArrayWrapper add(long value) {
        arrayNode.add(value);
        return this;
    }

    /**
     * 添加Map元素到数组，支持链式调用。
     *
     * @param value Map对象，会被转换为JSON对象元素
     * @return 当前JSON数组包装器实例，用于链式调用
     */
    @Override
    public IJsonArrayWrapper add(Map<?, ?> value) {
        arrayNode.add(JacksonAdapter.toObjectNode(value));
        return this;
    }

    /**
     * 添加任意类型元素到数组，支持链式调用。
     *
     * @param value 任意类型值，会被转换为JSON元素
     * @return 当前JSON数组包装器实例，用于链式调用
     */
    @Override
    public IJsonArrayWrapper add(Object value) {
        arrayNode.add(JacksonAdapter.toJsonNode(value));
        return this;
    }

    /**
     * 在指定位置插入布尔值元素，支持链式调用。
     *
     * @param index 插入位置索引，必须大于等于0且小于等于数组长度
     * @param value 布尔值
     * @return 当前JSON数组包装器实例，用于链式调用
     */
    @Override
    public IJsonArrayWrapper add(int index, boolean value) {
        arrayNode.insert(index, value);
        return this;
    }

    /**
     * 在指定位置插入集合元素，支持链式调用。
     *
     * @param index 插入位置索引，必须大于等于0且小于等于数组长度
     * @param value 集合对象，元素会被转换为JSON数组元素
     * @return 当前JSON数组包装器实例，用于链式调用
     */
    @Override
    public IJsonArrayWrapper add(int index, Collection<?> value) {
        arrayNode.insert(index, JacksonAdapter.toArrayNode(value));
        return this;
    }

    /**
     * 在指定位置插入双精度浮点数元素，支持链式调用。
     *
     * @param index 插入位置索引，必须大于等于0且小于等于数组长度
     * @param value 双精度浮点数
     * @return 当前JSON数组包装器实例，用于链式调用
     */
    @Override
    public IJsonArrayWrapper add(int index, double value) {
        arrayNode.insert(index, value);
        return this;
    }

    /**
     * 在指定位置插入单精度浮点数元素，支持链式调用。
     *
     * @param index 插入位置索引，必须大于等于0且小于等于数组长度
     * @param value 单精度浮点数
     * @return 当前JSON数组包装器实例，用于链式调用
     */
    @Override
    public IJsonArrayWrapper add(int index, float value) {
        arrayNode.insert(index, value);
        return this;
    }

    /**
     * 在指定位置插入整数元素，支持链式调用。
     *
     * @param index 插入位置索引，必须大于等于0且小于等于数组长度
     * @param value 整数值
     * @return 当前JSON数组包装器实例，用于链式调用
     */
    @Override
    public IJsonArrayWrapper add(int index, int value) {
        arrayNode.insert(index, value);
        return this;
    }

    /**
     * 在指定位置插入长整数元素，支持链式调用。
     *
     * @param index 插入位置索引，必须大于等于0且小于等于数组长度
     * @param value 长整数值
     * @return 当前JSON数组包装器实例，用于链式调用
     */
    @Override
    public IJsonArrayWrapper add(int index, long value) {
        arrayNode.insert(index, value);
        return this;
    }

    /**
     * 在指定位置插入Map元素，支持链式调用。
     *
     * @param index 插入位置索引，必须大于等于0且小于等于数组长度
     * @param value Map对象，会被转换为JSON对象元素
     * @return 当前JSON数组包装器实例，用于链式调用
     */
    @Override
    public IJsonArrayWrapper add(int index, Map<?, ?> value) {
        arrayNode.insert(index, JacksonAdapter.toObjectNode(value));
        return this;
    }

    /**
     * 在指定位置插入任意类型元素，支持链式调用。
     *
     * @param index 插入位置索引，必须大于等于0且小于等于数组长度
     * @param value 任意类型值，会被转换为JSON元素
     * @return 当前JSON数组包装器实例，用于链式调用
     */
    @Override
    public IJsonArrayWrapper add(int index, Object value) {
        arrayNode.insert(index, JacksonAdapter.toJsonNode(value));
        return this;
    }

    /**
     * 移除指定位置的元素。
     *
     * @param index 要移除的元素位置索引，必须大于等于0且小于数组长度
     * @return 被移除的元素，若索引越界则返回null
     */
    @Override
    public Object remove(int index) {
        return arrayNode.remove(index);
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
        JacksonArrayWrapper that = (JacksonArrayWrapper) o;
        return new EqualsBuilder()
                .append(arrayNode, that.arrayNode)
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
                .append(arrayNode)
                .toHashCode();
    }

    /**
     * 将当前JSON数组转换为字符串（默认紧凑输出）。
     *
     * @return 转换后的JSON字符串
     */
    @Override
    public String toString() {
        return this.toString(false, false);
    }

    /**
     * 将当前JSON数组转换为字符串，支持格式化输出和保留空值。
     *
     * @param format        是否格式化输出，true表示格式化（带缩进），false表示紧凑输出
     * @param keepNullValue 是否保留空值，true表示保留，false表示忽略
     * @return 转换后的JSON字符串
     */
    @Override
    public String toString(boolean format, boolean keepNullValue) {
        return adapter.toJsonString(arrayNode, format, keepNullValue);
    }

    /**
     * 将当前JSON数组转换为字符串，支持格式化输出、保留空值和蛇形命名转换。
     *
     * @param format        是否格式化输出，true表示格式化（带缩进），false表示紧凑输出
     * @param keepNullValue 是否保留空值，true表示保留，false表示忽略
     * @param snakeCase     是否将驼峰命名转换为蛇形命名
     * @return 转换后的JSON字符串
     */
    @Override
    public String toString(boolean format, boolean keepNullValue, boolean snakeCase) {
        return adapter.toJsonString(arrayNode, format, keepNullValue, snakeCase);
    }

    /**
     * 将当前JSON数组转换为字符串，支持格式化输出、保留空值和属性过滤。
     *
     * @param format        是否格式化输出，true表示格式化（带缩进），false表示紧凑输出
     * @param keepNullValue 是否保留空值，true表示保留，false表示忽略
     * @param filter        属性过滤器，用于控制哪些属性被序列化
     * @return 转换后的JSON字符串
     */
    @Override
    public String toString(boolean format, boolean keepNullValue, IJsonPropertyFilter filter) {
        return adapter.toJsonString(arrayNode, format, keepNullValue, filter);
    }

    /**
     * 将当前JSON数组转换为字符串，支持格式化输出、保留空值、蛇形命名转换和属性过滤。
     *
     * @param format        是否格式化输出，true表示格式化（带缩进），false表示紧凑输出
     * @param keepNullValue 是否保留空值，true表示保留，false表示忽略
     * @param snakeCase     是否将驼峰命名转换为蛇形命名
     * @param filter        属性过滤器，用于控制哪些属性被序列化
     * @return 转换后的JSON字符串
     */
    @Override
    public String toString(boolean format, boolean keepNullValue, boolean snakeCase, IJsonPropertyFilter filter) {
        return adapter.toJsonString(arrayNode, format, keepNullValue, snakeCase, filter);
    }

    /**
     * 将JSON数组转换为List。
     *
     * @return List对象，包含JSON数组的所有元素
     */
    @Override
    public List<Object> toList() {
        return JacksonAdapter.OBJECT_MAPPER.convertValue(arrayNode, new TypeReference<ArrayList<Object>>() {
        });
    }

    /**
     * 将JSON数组转换为Object数组。
     *
     * @return Object数组，包含JSON数组的所有元素
     */
    @Override
    public Object[] toArray() {
        return toList().toArray();
    }

    /**
     * 将当前JSON数组包装器转换为JsonWrapper对象。
     *
     * @return JsonWrapper对象，包含当前JSON数组包装器
     */
    @Override
    public JsonWrapper wrap() {
        return new JsonWrapper(this);
    }
}
