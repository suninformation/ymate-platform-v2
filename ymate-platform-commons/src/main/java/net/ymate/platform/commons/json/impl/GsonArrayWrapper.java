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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.ymate.platform.commons.json.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * Gson数组包装器实现，基于Google Gson的JsonArray提供JSON数组操作功能。
 * <p>
 * 设计目的：封装Gson JsonArray的使用细节，提供统一的JSON数组操作接口实现。
 * <p>
 * 使用场景：
 * - 当需要使用Gson进行JSON数组操作时
 * - 当需要统一的JSON数组操作接口时
 * - 当需要支持链式调用的JSON数组操作时
 *
 * @author 刘镇 (suninformation@163.com) on 2020/6/10 3:00 下午
 * @since 2.1.0
 */
public class GsonArrayWrapper implements IJsonArrayWrapper {

    private final IJsonAdapter adapter;

    private JsonArray jsonArray;

    /**
     * 构造函数，创建一个空的JSON数组包装器。
     *
     * @param adapter JSON适配器实例，不能为空
     */
    public GsonArrayWrapper(IJsonAdapter adapter) {
        this.adapter = adapter;
        jsonArray = new JsonArray();
    }

    /**
     * 构造函数，创建一个指定初始容量的JSON数组包装器。
     *
     * @param adapter         JSON适配器实例，不能为空
     * @param initialCapacity 初始容量，必须大于等于0
     */
    public GsonArrayWrapper(IJsonAdapter adapter, int initialCapacity) {
        this.adapter = adapter;
        jsonArray = new JsonArray(initialCapacity);
    }

    /**
     * 构造函数，根据数组创建JSON数组包装器。
     *
     * @param adapter JSON适配器实例，不能为空
     * @param array   用于创建JSON数组的对象数组
     */
    public GsonArrayWrapper(IJsonAdapter adapter, Object[] array) {
        this(adapter, array.length);
        Arrays.stream(array).forEach(this::add);
    }

    /**
     * 构造函数，根据集合创建JSON数组包装器。
     *
     * @param adapter    JSON适配器实例，不能为空
     * @param collection 用于创建JSON数组的集合
     */
    public GsonArrayWrapper(IJsonAdapter adapter, Collection<?> collection) {
        this(adapter, collection.size());
        collection.forEach(this::add);
    }

    /**
     * 构造函数，根据Gson的JsonArray创建JSON数组包装器。
     *
     * @param adapter   JSON适配器实例，不能为空
     * @param jsonArray Gson的JsonArray实例，若为null则创建空数组
     */
    public GsonArrayWrapper(IJsonAdapter adapter, JsonArray jsonArray) {
        this.adapter = adapter;
        this.jsonArray = jsonArray != null ? jsonArray : new JsonArray();
    }

    /**
     * 获取指定索引的JSON节点包装器。
     *
     * @param index 数组索引，必须大于等于0且小于数组大小
     * @return JSON节点包装器，若索引越界或值为null则返回null
     */
    @Override
    public IJsonNodeWrapper get(int index) {
        JsonElement jsonElement = jsonArray.get(index);
        return jsonElement != null ? new GsonNodeWrapper(adapter, jsonElement) : null;
    }

    /**
     * 获取指定索引的布尔值。
     *
     * @param index 数组索引，必须大于等于0且小于数组大小
     * @return 布尔值，若索引越界或类型不匹配则返回false
     */
    @Override
    public boolean getBoolean(int index) {
        JsonElement jsonElement = jsonArray.get(index);
        return jsonElement != null && jsonElement.getAsBoolean();
    }

    /**
     * 获取指定索引的布尔值，支持null。
     *
     * @param index 数组索引，必须大于等于0且小于数组大小
     * @return 布尔值包装类，若索引越界或值为null则返回null
     */
    @Override
    public Boolean getAsBoolean(int index) {
        JsonElement jsonElement = jsonArray.get(index);
        if (jsonElement != null) {
            return jsonElement.getAsBoolean();
        }
        return null;
    }

    /**
     * 获取指定索引的双精度浮点数。
     *
     * @param index 数组索引，必须大于等于0且小于数组大小
     * @return 双精度浮点数，若索引越界或类型不匹配则返回0.0
     */
    @Override
    public double getDouble(int index) {
        JsonElement jsonElement = jsonArray.get(index);
        return jsonElement != null ? jsonElement.getAsDouble() : 0d;
    }

    /**
     * 获取指定索引的双精度浮点数，支持null。
     *
     * @param index 数组索引，必须大于等于0且小于数组大小
     * @return 双精度浮点数包装类，若索引越界或值为null则返回null
     */
    @Override
    public Double getAsDouble(int index) {
        JsonElement jsonElement = jsonArray.get(index);
        if (jsonElement != null) {
            return jsonElement.getAsDouble();
        }
        return null;
    }

    /**
     * 获取指定索引的单精度浮点数。
     *
     * @param index 数组索引，必须大于等于0且小于数组大小
     * @return 单精度浮点数，若索引越界或类型不匹配则返回0.0f
     */
    @Override
    public float getFloat(int index) {
        JsonElement jsonElement = jsonArray.get(index);
        return jsonElement != null ? jsonElement.getAsFloat() : 0f;
    }

    /**
     * 获取指定索引的单精度浮点数，支持null。
     *
     * @param index 数组索引，必须大于等于0且小于数组大小
     * @return 单精度浮点数包装类，若索引越界或值为null则返回null
     */
    @Override
    public Float getAsFloat(int index) {
        JsonElement jsonElement = jsonArray.get(index);
        if (jsonElement != null) {
            return jsonElement.getAsFloat();
        }
        return null;
    }

    /**
     * 获取指定索引的大小数。
     *
     * @param index 数组索引，必须大于等于0且小于数组大小
     * @return 大小数值，若索引越界或类型不匹配则返回null
     */
    @Override
    public BigDecimal getBigDecimal(int index) {
        JsonElement jsonElement = jsonArray.get(index);
        return jsonElement != null ? jsonElement.getAsBigDecimal() : null;
    }

    /**
     * 获取指定索引的大整数。
     *
     * @param index 数组索引，必须大于等于0且小于数组大小
     * @return 大整数值，若索引越界或类型不匹配则返回null
     */
    @Override
    public BigInteger getBigInteger(int index) {
        JsonElement jsonElement = jsonArray.get(index);
        return jsonElement != null ? jsonElement.getAsBigInteger() : null;
    }

    /**
     * 获取指定索引的整数。
     *
     * @param index 数组索引，必须大于等于0且小于数组大小
     * @return 整数值，若索引越界或类型不匹配则返回0
     */
    @Override
    public int getInt(int index) {
        JsonElement jsonElement = jsonArray.get(index);
        return jsonElement != null ? jsonElement.getAsInt() : 0;
    }

    /**
     * 获取指定索引的整数，支持null。
     *
     * @param index 数组索引，必须大于等于0且小于数组大小
     * @return 整数包装类，若索引越界或值为null则返回null
     */
    @Override
    public Integer getAsInteger(int index) {
        JsonElement jsonElement = jsonArray.get(index);
        if (jsonElement != null) {
            return jsonElement.getAsInt();
        }
        return null;
    }

    /**
     * 获取指定索引的JSON数组包装器。
     *
     * @param index 数组索引，必须大于等于0且小于数组大小
     * @return JSON数组包装器，若索引越界或类型不匹配则返回null
     */
    @Override
    public IJsonArrayWrapper getJsonArray(int index) {
        JsonArray value = jsonArray.get(index).getAsJsonArray();
        return value == null ? null : new GsonArrayWrapper(adapter, value);
    }

    /**
     * 获取指定索引的JSON对象包装器。
     *
     * @param index 数组索引，必须大于等于0且小于数组大小
     * @return JSON对象包装器，若索引越界或类型不匹配则返回null
     */
    @Override
    public IJsonObjectWrapper getJsonObject(int index) {
        JsonObject value = jsonArray.get(index).getAsJsonObject();
        return value == null ? null : new GsonObjectWrapper(adapter, value);
    }

    /**
     * 获取指定索引的长整数。
     *
     * @param index 数组索引，必须大于等于0且小于数组大小
     * @return 长整数值，若索引越界或类型不匹配则返回0L
     */
    @Override
    public long getLong(int index) {
        JsonElement jsonElement = jsonArray.get(index);
        return jsonElement != null ? jsonElement.getAsLong() : 0L;
    }

    /**
     * 获取指定索引的长整数，支持null。
     *
     * @param index 数组索引，必须大于等于0且小于数组大小
     * @return 长整数包装类，若索引越界或值为null则返回null
     */
    @Override
    public Long getAsLong(int index) {
        JsonElement jsonElement = jsonArray.get(index);
        if (jsonElement != null) {
            return jsonElement.getAsLong();
        }
        return null;
    }

    /**
     * 获取指定索引的字符串值。
     *
     * @param index 数组索引，必须大于等于0且小于数组大小
     * @return 字符串值，若索引越界或值为null则返回null
     */
    @Override
    public String getString(int index) {
        JsonElement jsonElement = jsonArray.get(index);
        return jsonElement != null ? jsonElement.getAsString() : null;
    }

    /**
     * 检查指定索引的值是否为null。
     *
     * @param index 数组索引，必须大于等于0且小于数组大小
     * @return 若指定索引的值为null则返回true，否则返回false
     */
    @Override
    public boolean isNull(int index) {
        JsonElement jsonElement = jsonArray.get(index);
        return jsonElement == null || jsonElement.isJsonNull();
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
        jsonArray.add(GsonAdapter.toJsonArray(value));
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
        jsonArray.add(GsonAdapter.toJsonObject(value));
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
        jsonArray.add(GsonAdapter.toJsonElement(value));
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
        return add(index, (Object) value);
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
        return add(index, (Object) value);
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
        return add(index, (Object) value);
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
        return add(index, (Object) value);
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
        return add(index, (Object) value);
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
        return add(index, (Object) value);
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
        return add(index, (Object) value);
    }

    /**
     * 向JSON数组指定索引位置添加任意类型值，支持链式调用。
     * <p>
     * 注意：由于Gson的JsonArray没有直接的insert方法，该方法通过创建新数组并复制元素来实现插入功能。
     *
     * @param index 数组索引，必须大于等于0且小于等于数组大小
     * @param value 任意类型值
     * @return 当前JSON数组包装器实例，用于链式调用
     */
    @Override
    public IJsonArrayWrapper add(int index, Object value) {
        // Gson的JsonArray没有直接的add(int, JsonElement)方法，需要手动实现
        List<JsonElement> elements = new ArrayList<>(jsonArray.size() + 1);
        // 复制现有元素
        for (int i = 0; i < jsonArray.size(); i++) {
            elements.add(jsonArray.get(i));
        }
        // 在指定索引插入新元素
        elements.add(index, GsonAdapter.toJsonElement(value));
        // 创建新的JsonArray并添加所有元素
        JsonArray newJsonArray = new JsonArray(elements.size());
        elements.forEach(newJsonArray::add);
        // 替换原数组
        this.jsonArray = newJsonArray;
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
        GsonArrayWrapper that = (GsonArrayWrapper) o;
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
    @SuppressWarnings("unchecked")
    public List<Object> toList() {
        return GsonAdapter.GSON.fromJson(jsonArray, List.class);
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
