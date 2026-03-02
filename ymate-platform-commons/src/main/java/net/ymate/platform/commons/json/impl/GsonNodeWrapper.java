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

import com.google.gson.JsonElement;
import net.ymate.platform.commons.json.IJsonAdapter;
import net.ymate.platform.commons.json.IJsonArrayWrapper;
import net.ymate.platform.commons.json.IJsonNodeWrapper;
import net.ymate.platform.commons.json.IJsonObjectWrapper;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Gson节点包装器实现，基于Google Gson的JsonElement提供JSON节点操作功能。
 * <p>
 * 设计目的：封装Gson JsonElement的使用细节，提供统一的JSON节点操作接口实现。
 * <p>
 * 使用场景：
 * - 当需要使用Gson进行JSON节点操作时
 * - 当需要统一的JSON节点操作接口时
 * - 当需要检查JSON节点类型并进行转换时
 *
 * @author 刘镇 (suninformation@163.com) on 2020/6/20 4:12 下午
 * @since 2.1.0
 */
public class GsonNodeWrapper implements IJsonNodeWrapper {

    private final IJsonAdapter adapter;

    private final JsonElement jsonElement;

    /**
     * 构造函数，创建JSON节点包装器。
     *
     * @param adapter     JSON适配器实例，不能为空
     * @param jsonElement Gson的JsonElement实例，不能为空
     */
    public GsonNodeWrapper(IJsonAdapter adapter, JsonElement jsonElement) {
        this.adapter = adapter;
        this.jsonElement = jsonElement;
    }

    /**
     * 获取原始JsonElement对象。
     *
     * @return 原始JsonElement对象，不为null
     */
    @Override
    public Object get() {
        return jsonElement;
    }

    /**
     * 获取布尔值。
     *
     * @return 布尔值，若节点类型不匹配则抛出异常
     */
    @Override
    public boolean getBoolean() {
        return jsonElement.getAsBoolean();
    }

    /**
     * 获取大整数。
     *
     * @return 大整数值，若节点类型不匹配则抛出异常
     */
    @Override
    public BigInteger getBigInteger() {
        return jsonElement.getAsBigInteger();
    }

    /**
     * 获取大小数。
     *
     * @return 大小数值，若节点类型不匹配则抛出异常
     */
    @Override
    public BigDecimal getBigDecimal() {
        return jsonElement.getAsBigDecimal();
    }

    /**
     * 获取双精度浮点数。
     *
     * @return 双精度浮点数，若节点类型不匹配则抛出异常
     */
    @Override
    public double getDouble() {
        return jsonElement.getAsDouble();
    }

    /**
     * 获取单精度浮点数。
     *
     * @return 单精度浮点数，若节点类型不匹配则抛出异常
     */
    @Override
    public float getFloat() {
        return jsonElement.getAsFloat();
    }

    /**
     * 获取整数。
     *
     * @return 整数值，若节点类型不匹配则抛出异常
     */
    @Override
    public int getInt() {
        return jsonElement.getAsInt();
    }

    /**
     * 获取长整数。
     *
     * @return 长整数值，若节点类型不匹配则抛出异常
     */
    @Override
    public long getLong() {
        return jsonElement.getAsLong();
    }

    /**
     * 获取字符串值。
     *
     * @return 字符串值，若节点类型不匹配则抛出异常
     */
    @Override
    public String getString() {
        return jsonElement.getAsString();
    }

    /**
     * 检查节点是否为null。
     *
     * @return 若节点为null则返回true，否则返回false
     */
    @Override
    public boolean isNull() {
        return jsonElement.isJsonNull();
    }

    /**
     * 检查节点是否为JSON数组。
     *
     * @return 若节点为JSON数组则返回true，否则返回false
     */
    @Override
    public boolean isJsonArray() {
        return jsonElement.isJsonArray();
    }

    /**
     * 检查节点是否为JSON对象。
     *
     * @return 若节点为JSON对象则返回true，否则返回false
     */
    @Override
    public boolean isJsonObject() {
        return jsonElement.isJsonObject();
    }

    /**
     * 将节点转换为JSON数组包装器。
     *
     * @return JSON数组包装器，若节点类型不是数组则返回null
     */
    @Override
    public IJsonArrayWrapper getJsonArray() {
        return jsonElement.isJsonArray() ? new GsonArrayWrapper(adapter, jsonElement.getAsJsonArray()) : null;
    }

    /**
     * 将节点转换为JSON对象包装器。
     *
     * @return JSON对象包装器，若节点类型不是对象则返回null
     */
    @Override
    public IJsonObjectWrapper getJsonObject() {
        return jsonElement.isJsonObject() ? new GsonObjectWrapper(adapter, jsonElement.getAsJsonObject()) : null;
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
        GsonNodeWrapper that = (GsonNodeWrapper) o;
        return new EqualsBuilder()
                .append(jsonElement, that.jsonElement)
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
                .append(jsonElement)
                .toHashCode();
    }

    /**
     * 将当前JSON节点转换为字符串（默认紧凑输出）。
     *
     * @return 转换后的JSON字符串
     */
    @Override
    public String toString() {
        return adapter.toJsonString(jsonElement, false, false);
    }
}
