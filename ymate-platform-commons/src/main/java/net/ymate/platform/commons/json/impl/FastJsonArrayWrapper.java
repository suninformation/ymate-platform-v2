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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * FastJSON数组包装器实现，基于Alibaba FastJSON的JSONArray提供JSON数组操作功能。
 * <p>
 * 设计目的：封装FastJSON JSONArray的使用细节，提供统一的JSON数组操作接口实现。
 * <p>
 * 使用场景：
 * - 当需要使用FastJSON进行JSON数组操作时
 * - 当需要统一的JSON数组操作接口时
 * - 当需要支持链式调用的JSON数组操作时
 *
 * @author 刘镇 (suninformation@163.com) on 2020/6/8 5:13 下午
 * @since 2.1.0
 */
public class FastJsonArrayWrapper implements IJsonArrayWrapper {

    private final IJsonAdapter adapter;

    private final JSONArray jsonArray;

    /**
     * 构造函数，创建一个空的JSON数组包装器。
     *
     * @param adapter JSON适配器实例，不能为空
     */
    public FastJsonArrayWrapper(IJsonAdapter adapter) {
        this.adapter = adapter;
        jsonArray = new JSONArray();
    }

    /**
     * 构造函数，创建一个指定初始容量的JSON数组包装器。
     *
     * @param adapter         JSON适配器实例，不能为空
     * @param initialCapacity 初始容量，必须大于等于0
     */
    public FastJsonArrayWrapper(IJsonAdapter adapter, int initialCapacity) {
        this.adapter = adapter;
        jsonArray = new JSONArray(initialCapacity);
    }

    /**
     * 构造函数，根据数组创建JSON数组包装器。
     *
     * @param adapter JSON适配器实例，不能为空
     * @param array   用于创建JSON数组的对象数组
     */
    public FastJsonArrayWrapper(IJsonAdapter adapter, Object[] array) {
        this(adapter, array.length);
        Arrays.stream(array).forEach(this::add);
    }

    /**
     * 构造函数，根据集合创建JSON数组包装器。
     *
     * @param adapter    JSON适配器实例，不能为空
     * @param collection 用于创建JSON数组的集合
     */
    public FastJsonArrayWrapper(IJsonAdapter adapter, Collection<?> collection) {
        this(adapter, collection.size());
        collection.forEach(this::add);
    }

    /**
     * 构造函数，根据FastJSON的JSONArray创建JSON数组包装器。
     *
     * @param adapter   JSON适配器实例，不能为空
     * @param jsonArray FastJSON的JSONArray实例，若为null则创建空数组
     */
    public FastJsonArrayWrapper(IJsonAdapter adapter, JSONArray jsonArray) {
        this.adapter = adapter;
        this.jsonArray = jsonArray != null ? jsonArray : new JSONArray();
    }

    /**
     * 获取指定索引的JSON节点包装器。
     *
     * @param index 数组索引，必须大于等于0且小于数组大小
     * @return JSON节点包装器，若索引越界或值为null则返回null
     */
    @Override
    public IJsonNodeWrapper get(int index) {
        Object object = jsonArray.get(index);
        return object != null ? new FastJsonNodeWrapper(adapter, object) : null;
    }

    /**
     * 获取指定索引的布尔值。
     *
     * @param index 数组索引，必须大于等于0且小于数组大小
     * @return 布尔值，若索引越界或类型不匹配则返回false
     */
    @Override
    public boolean getBoolean(int index) {
        return jsonArray.getBooleanValue(index);
    }

    /**
     * 获取指定索引的布尔值，支持null。
     *
     * @param index 数组索引，必须大于等于0且小于数组大小
     * @return 布尔值包装类，若索引越界或值为null则返回null
     */
    @Override
    public Boolean getAsBoolean(int index) {
        return jsonArray.getBoolean(index);
    }

    /**
     * 获取指定索引的双精度浮点数。
     *
     * @param index 数组索引，必须大于等于0且小于数组大小
     * @return 双精度浮点数，若索引越界或类型不匹配则返回0.0
     */
    @Override
    public double getDouble(int index) {
        return jsonArray.getDoubleValue(index);
    }

    /**
     * 获取指定索引的双精度浮点数，支持null。
     *
     * @param index 数组索引，必须大于等于0且小于数组大小
     * @return 双精度浮点数包装类，若索引越界或值为null则返回null
     */
    @Override
    public Double getAsDouble(int index) {
        return jsonArray.getDouble(index);
    }

    /**
     * 获取指定索引的单精度浮点数。
     *
     * @param index 数组索引，必须大于等于0且小于数组大小
     * @return 单精度浮点数，若索引越界或类型不匹配则返回0.0f
     */
    @Override
    public float getFloat(int index) {
        return jsonArray.getFloatValue(index);
    }

    /**
     * 获取指定索引的单精度浮点数，支持null。
     *
     * @param index 数组索引，必须大于等于0且小于数组大小
     * @return 单精度浮点数包装类，若索引越界或值为null则返回null
     */
    @Override
    public Float getAsFloat(int index) {
        return jsonArray.getFloat(index);
    }

    /**
     * 获取指定索引的大小数。
     *
     * @param index 数组索引，必须大于等于0且小于数组大小
     * @return 大小数值，若索引越界或类型不匹配则返回null
     */
    @Override
    public BigDecimal getBigDecimal(int index) {
        return jsonArray.getBigDecimal(index);
    }

    /**
     * 获取指定索引的大整数。
     *
     * @param index 数组索引，必须大于等于0且小于数组大小
     * @return 大整数值，若索引越界或类型不匹配则返回null
     */
    @Override
    public BigInteger getBigInteger(int index) {
        return jsonArray.getBigInteger(index);
    }

    /**
     * 获取指定索引的整数。
     *
     * @param index 数组索引，必须大于等于0且小于数组大小
     * @return 整数值，若索引越界或类型不匹配则返回0
     */
    @Override
    public int getInt(int index) {
        return jsonArray.getIntValue(index);
    }

    /**
     * 获取指定索引的整数，支持null。
     *
     * @param index 数组索引，必须大于等于0且小于数组大小
     * @return 整数包装类，若索引越界或值为null则返回null
     */
    @Override
    public Integer getAsInteger(int index) {
        return jsonArray.getInteger(index);
    }

    /**
     * 获取指定索引的JSON数组包装器。
     *
     * @param index 数组索引，必须大于等于0且小于数组大小
     * @return JSON数组包装器，若索引越界或类型不匹配则返回null
     */
    @Override
    public IJsonArrayWrapper getJsonArray(int index) {
        JSONArray value = jsonArray.getJSONArray(index);
        return value == null ? null : new FastJsonArrayWrapper(adapter, value);
    }

    /**
     * 获取指定索引的JSON对象包装器。
     *
     * @param index 数组索引，必须大于等于0且小于数组大小
     * @return JSON对象包装器，若索引越界或类型不匹配则返回null
     */
    @Override
    public IJsonObjectWrapper getJsonObject(int index) {
        JSONObject value = jsonArray.getJSONObject(index);
        return value == null ? null : new FastJsonObjectWrapper(adapter, value);
    }

    /**
     * 获取指定索引的长整数。
     *
     * @param index 数组索引，必须大于等于0且小于数组大小
     * @return 长整数值，若索引越界或类型不匹配则返回0L
     */
    @Override
    public long getLong(int index) {
        return jsonArray.getLongValue(index);
    }

    /**
     * 获取指定索引的长整数，支持null。
     *
     * @param index 数组索引，必须大于等于0且小于数组大小
     * @return 长整数包装类，若索引越界或值为null则返回null
     */
    @Override
    public Long getAsLong(int index) {
        return jsonArray.getLong(index);
    }

    /**
     * 获取指定索引的字符串值。
     *
     * @param index 数组索引，必须大于等于0且小于数组大小
     * @return 字符串值，若索引越界或值为null则返回null
     */
    @Override
    public String getString(int index) {
        return jsonArray.getString(index);
    }

    /**
     * 检查指定索引的值是否为null。
     *
     * @param index 数组索引，必须大于等于0且小于数组大小
     * @return 若指定索引的值为null则返回true，否则返回false
     */
    @Override
    public boolean isNull(int index) {
        return jsonArray.get(index) == null;
    }

    /**
     * 获取JSON数组的大小。
     *
     * @return JSON数组中元素的数量
     */
    @Override
    public int size() {
        return jsonArray.size();
    }

    /**
     * 检查JSON数组是否为空。
     *
     * @return 若JSON数组为空则返回true，否则返回false
     */
    @Override
    public boolean isEmpty() {
        return jsonArray.isEmpty();
    }

    /**
     * 向JSON数组末尾添加布尔值，支持链式调用。
     *
     * @param value 布尔值
     * @return 当前JSON数组包装器实例，用于链式调用
     */
    @Override
    public IJsonArrayWrapper add(boolean value) {
        jsonArray.add(value);
        return this;
    }

    /**
     * 向JSON数组末尾添加集合，支持链式调用。
     *
     * @param value 集合对象
     * @return 当前JSON数组包装器实例，用于链式调用
     */
    @Override
    public IJsonArrayWrapper add(Collection<?> value) {
        jsonArray.add(FastJsonAdapter.toJsonArray(value));
        return this;
    }

    /**
     * 向JSON数组末尾添加双精度浮点数，支持链式调用。
     *
     * @param value 双精度浮点数
     * @return 当前JSON数组包装器实例，用于链式调用
     */
    @Override
    public IJsonArrayWrapper add(double value) {
        jsonArray.add(value);
        return this;
    }

    /**
     * 向JSON数组末尾添加单精度浮点数，支持链式调用。
     *
     * @param value 单精度浮点数
     * @return 当前JSON数组包装器实例，用于链式调用
     */
    @Override
    public IJsonArrayWrapper add(float value) {
        jsonArray.add(value);
        return this;
    }

    /**
     * 向JSON数组末尾添加整数，支持链式调用。
     *
     * @param value 整数值
     * @return 当前JSON数组包装器实例，用于链式调用
     */
    @Override
    public IJsonArrayWrapper add(int value) {
        jsonArray.add(value);
        return this;
    }

    /**
     * 向JSON数组末尾添加长整数，支持链式调用。
     *
     * @param value 长整数值
     * @return 当前JSON数组包装器实例，用于链式调用
     */
    @Override
    public IJsonArrayWrapper add(long value) {
        jsonArray.add(value);
        return this;
    }

    /**
     * 向JSON数组末尾添加Map，支持链式调用。
     *
     * @param value Map对象
     * @return 当前JSON数组包装器实例，用于链式调用
     */
    @Override
    public IJsonArrayWrapper add(Map<?, ?> value) {
        jsonArray.add(FastJsonAdapter.toJsonObject(value));
        return this;
    }

    /**
     * 向JSON数组末尾添加任意类型值，支持链式调用。
     *
     * @param value 任意类型值
     * @return 当前JSON数组包装器实例，用于链式调用
     */
    @Override
    public IJsonArrayWrapper add(Object value) {
        jsonArray.add(JsonWrapper.unwrap(value));
        return this;
    }

    /**
     * 向JSON数组指定索引位置添加布尔值，支持链式调用。
     *
     * @param index 数组索引，必须大于等于0且小于等于数组大小
     * @param value 布尔值
     * @return 当前JSON数组包装器实例，用于链式调用
     */
    @Override
    public IJsonArrayWrapper add(int index, boolean value) {
        jsonArray.add(index, value);
        return this;
    }

    /**
     * 向JSON数组指定索引位置添加集合，支持链式调用。
     *
     * @param index 数组索引，必须大于等于0且小于等于数组大小
     * @param value 集合对象
     * @return 当前JSON数组包装器实例，用于链式调用
     */
    @Override
    public IJsonArrayWrapper add(int index, Collection<?> value) {
        jsonArray.add(index, value);
        return this;
    }

    /**
     * 向JSON数组指定索引位置添加双精度浮点数，支持链式调用。
     *
     * @param index 数组索引，必须大于等于0且小于等于数组大小
     * @param value 双精度浮点数
     * @return 当前JSON数组包装器实例，用于链式调用
     */
    @Override
    public IJsonArrayWrapper add(int index, double value) {
        jsonArray.add(index, value);
        return this;
    }

    /**
     * 向JSON数组指定索引位置添加单精度浮点数，支持链式调用。
     *
     * @param index 数组索引，必须大于等于0且小于等于数组大小
     * @param value 单精度浮点数
     * @return 当前JSON数组包装器实例，用于链式调用
     */
    @Override
    public IJsonArrayWrapper add(int index, float value) {
        jsonArray.add(index, value);
        return this;
    }

    /**
     * 向JSON数组指定索引位置添加整数，支持链式调用。
     *
     * @param index 数组索引，必须大于等于0且小于等于数组大小
     * @param value 整数值
     * @return 当前JSON数组包装器实例，用于链式调用
     */
    @Override
    public IJsonArrayWrapper add(int index, int value) {
        jsonArray.add(index, value);
        return this;
    }

    /**
     * 向JSON数组指定索引位置添加长整数，支持链式调用。
     *
     * @param index 数组索引，必须大于等于0且小于等于数组大小
     * @param value 长整数值
     * @return 当前JSON数组包装器实例，用于链式调用
     */
    @Override
    public IJsonArrayWrapper add(int index, long value) {
        jsonArray.add(index, value);
        return this;
    }

    /**
     * 向JSON数组指定索引位置添加Map，支持链式调用。
     *
     * @param index 数组索引，必须大于等于0且小于等于数组大小
     * @param value Map对象
     * @return 当前JSON数组包装器实例，用于链式调用
     */
    @Override
    public IJsonArrayWrapper add(int index, Map<?, ?> value) {
        jsonArray.add(index, FastJsonAdapter.toJsonObject(value));
        return this;
    }

    /**
     * 向JSON数组指定索引位置添加任意类型值，支持链式调用。
     *
     * @param index 数组索引，必须大于等于0且小于等于数组大小
     * @param value 任意类型值
     * @return 当前JSON数组包装器实例，用于链式调用
     */
    @Override
    public IJsonArrayWrapper add(int index, Object value) {
        jsonArray.add(index, JsonWrapper.unwrap(value));
        return this;
    }

    /**
     * 移除指定索引的元素。
     *
     * @param index 数组索引，必须大于等于0且小于数组大小
     * @return 被移除的值，若索引越界则返回null
     */
    @Override
    public Object remove(int index) {
        return jsonArray.remove(index);
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
        FastJsonArrayWrapper that = (FastJsonArrayWrapper) o;
        return new EqualsBuilder()
                .append(jsonArray, that.jsonArray)
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
                .append(jsonArray)
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
        return adapter.toJsonString(jsonArray, format, keepNullValue);
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
        return adapter.toJsonString(jsonArray, format, keepNullValue, snakeCase);
    }

    /**
     * 将当前JSON数组转换为字符串，支持格式化输出、保留空值和属性过滤。
     *
     * @param format        是否格式化输出，true表示格式化（带缩进），false表示紧凑输出
     * @param keepNullValue 是否保留空值，true表示保留，false表示忽略
     * @param filter        属性过滤器，用于控制哪些属性被序列化
     * @return 转换后的JSON字符串
     * @since 2.1.4
     */
    @Override
    public String toString(boolean format, boolean keepNullValue, IJsonPropertyFilter filter) {
        return adapter.toJsonString(jsonArray, format, keepNullValue, filter);
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
        return adapter.toJsonString(jsonArray, format, keepNullValue, snakeCase, filter);
    }

    /**
     * 将JSON数组转换为List。
     *
     * @return List对象，包含JSON数组的所有元素
     */
    @Override
    public List<Object> toList() {
        return jsonArray;
    }

    /**
     * 将JSON数组转换为Object数组。
     *
     * @return Object数组，包含JSON数组的所有元素
     */
    @Override
    public Object[] toArray() {
        return jsonArray.toArray();
    }

    /**
     * 将当前JSON数组包装器转换为JsonWrapper对象。
     *
     * @return JsonWrapper对象，包含当前JSON数组包装器
     * @since 2.1.4
     */
    @Override
    public JsonWrapper wrap() {
        return new JsonWrapper(this);
    }
}
