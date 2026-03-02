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
import com.alibaba.fastjson.util.TypeUtils;
import net.ymate.platform.commons.json.IJsonAdapter;
import net.ymate.platform.commons.json.IJsonArrayWrapper;
import net.ymate.platform.commons.json.IJsonNodeWrapper;
import net.ymate.platform.commons.json.IJsonObjectWrapper;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * FastJSON节点包装器实现，基于Alibaba FastJSON提供JSON节点操作功能。
 * <p>
 * 设计目的：封装FastJSON节点的使用细节，提供统一的JSON节点操作接口实现。
 * <p>
 * 使用场景：
 * - 当需要使用FastJSON进行JSON节点操作时
 * - 当需要统一的JSON节点操作接口时
 * - 当需要支持多种JSON节点类型的转换和操作时
 *
 * @author 刘镇 (suninformation@163.com) on 2020/6/20 4:44 下午
 * @since 2.1.0
 */
public class FastJsonNodeWrapper implements IJsonNodeWrapper {

    private final IJsonAdapter adapter;

    private final Object object;

    /**
     * 构造函数，创建一个FastJSON节点包装器。
     *
     * @param adapter JSON适配器实例，不能为空
     * @param object  要包装的节点对象
     */
    public FastJsonNodeWrapper(IJsonAdapter adapter, Object object) {
        this.adapter = adapter;
        this.object = object;
    }

    /**
     * 获取原始节点值。
     *
     * @return 原始节点值，可能为null
     */
    @Override
    public Object get() {
        return object;
    }

    /**
     * 获取布尔值。
     *
     * @return 布尔值，若节点类型不匹配则返回false
     */
    @Override
    public boolean getBoolean() {
        return object != null && TypeUtils.castToBoolean(object);
    }

    /**
     * 获取大整数。
     *
     * @return 大整数值，若节点类型不匹配则返回null
     */
    @Override
    public BigInteger getBigInteger() {
        return TypeUtils.castToBigInteger(object);
    }

    /**
     * 获取大小数。
     *
     * @return 大小数值，若节点类型不匹配则返回null
     */
    @Override
    public BigDecimal getBigDecimal() {
        return TypeUtils.castToBigDecimal(object);
    }

    /**
     * 获取双精度浮点数。
     *
     * @return 双精度浮点数，若节点类型不匹配则返回0.0
     */
    @Override
    public double getDouble() {
        Double doubleValue = TypeUtils.castToDouble(object);
        if (doubleValue == null) {
            return 0d;
        }
        return doubleValue;
    }

    /**
     * 获取单精度浮点数。
     *
     * @return 单精度浮点数，若节点类型不匹配则返回0.0f
     */
    @Override
    public float getFloat() {
        Float floatValue = TypeUtils.castToFloat(object);
        if (floatValue == null) {
            return 0f;
        }
        return floatValue;
    }

    /**
     * 获取整数。
     *
     * @return 整数值，若节点类型不匹配则返回0
     */
    @Override
    public int getInt() {
        Integer intVal = TypeUtils.castToInt(object);
        if (intVal == null) {
            return 0;
        }
        return intVal;
    }

    /**
     * 获取长整数。
     *
     * @return 长整数值，若节点类型不匹配则返回0L
     */
    @Override
    public long getLong() {
        Long longVal = TypeUtils.castToLong(object);
        if (longVal == null) {
            return 0L;
        }
        return longVal;
    }

    /**
     * 获取字符串值。
     *
     * @return 字符串值，若节点类型不匹配则返回null
     */
    @Override
    public String getString() {
        return TypeUtils.castToString(object);
    }

    /**
     * 检查节点值是否为null。
     *
     * @return 若节点值为null则返回true，否则返回false
     */
    @Override
    public boolean isNull() {
        return object == null;
    }

    /**
     * 检查节点是否为JSON数组。
     *
     * @return 若节点为JSON数组则返回true，否则返回false
     */
    @Override
    public boolean isJsonArray() {
        return object instanceof JSONArray;
    }

    /**
     * 检查节点是否为JSON对象。
     *
     * @return 若节点为JSON对象则返回true，否则返回false
     */
    @Override
    public boolean isJsonObject() {
        return object instanceof JSONObject;
    }

    /**
     * 获取JSON数组包装器。
     *
     * @return JSON数组包装器，若节点类型不是JSON数组则返回null
     */
    @Override
    public IJsonArrayWrapper getJsonArray() {
        return isJsonArray() ? new FastJsonArrayWrapper(adapter, (JSONArray) object) : null;
    }

    /**
     * 获取JSON对象包装器。
     *
     * @return JSON对象包装器，若节点类型不是JSON对象则返回null
     */
    @Override
    public IJsonObjectWrapper getJsonObject() {
        return isJsonObject() ? new FastJsonObjectWrapper(adapter, (JSONObject) object) : null;
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
        FastJsonNodeWrapper that = (FastJsonNodeWrapper) o;
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
     * 将当前节点转换为JSON字符串。
     *
     * @return 转换后的JSON字符串
     */
    @Override
    public String toString() {
        return adapter.toJsonString(object, false, false);
    }
}
