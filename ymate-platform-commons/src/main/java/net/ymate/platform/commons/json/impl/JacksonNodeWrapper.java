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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.ymate.platform.commons.json.IJsonAdapter;
import net.ymate.platform.commons.json.IJsonArrayWrapper;
import net.ymate.platform.commons.json.IJsonNodeWrapper;
import net.ymate.platform.commons.json.IJsonObjectWrapper;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Jackson节点包装器实现，基于Jackson的JsonNode提供JSON节点操作功能。
 * <p>
 * 设计目的：封装Jackson JsonNode的使用细节，提供统一的JSON节点操作接口实现。
 * <p>
 * 使用场景：
 * - 当需要使用Jackson进行JSON节点操作时
 * - 当需要统一的JSON节点操作接口时
 * - 当需要检查JSON节点类型并进行转换时
 *
 * @author 刘镇 (suninformation@163.com) on 2020/6/20 3:52 下午
 * @since 2.1.0
 */
public class JacksonNodeWrapper implements IJsonNodeWrapper {

    private final IJsonAdapter adapter;

    private final JsonNode jsonNode;

    /**
     * 构造函数，创建JSON节点包装器。
     *
     * @param adapter  JSON适配器实例，不能为空
     * @param jsonNode Jackson JsonNode实例，不能为空
     */
    public JacksonNodeWrapper(IJsonAdapter adapter, JsonNode jsonNode) {
        this.adapter = adapter;
        this.jsonNode = jsonNode;
    }

    /**
     * 获取原始JsonNode对象。
     *
     * @return 原始JsonNode对象，不为null
     */
    @Override
    public Object get() {
        return jsonNode;
    }

    /**
     * 获取布尔值。
     *
     * @return 布尔值，若节点类型不匹配则尝试转换
     */
    @Override
    public boolean getBoolean() {
        return jsonNode.asBoolean();
    }

    /**
     * 获取大整数。
     *
     * @return 大整数值，若节点类型不匹配则尝试转换
     */
    @Override
    public BigInteger getBigInteger() {
        return BigInteger.valueOf(jsonNode.asLong());
    }

    /**
     * 获取大小数。
     *
     * @return 大小数值，若节点类型不匹配则尝试转换
     */
    @Override
    public BigDecimal getBigDecimal() {
        return BigDecimal.valueOf(jsonNode.asDouble());
    }

    /**
     * 获取双精度浮点数。
     *
     * @return 双精度浮点数，若节点类型不匹配则尝试转换
     */
    @Override
    public double getDouble() {
        return jsonNode.asDouble();
    }

    /**
     * 获取单精度浮点数。
     *
     * @return 单精度浮点数，若节点类型不匹配则尝试转换
     */
    @Override
    public float getFloat() {
        return Double.valueOf(jsonNode.asDouble()).floatValue();
    }

    /**
     * 获取整数。
     *
     * @return 整数值，若节点类型不匹配则尝试转换
     */
    @Override
    public int getInt() {
        return jsonNode.asInt();
    }

    /**
     * 获取长整数。
     *
     * @return 长整数值，若节点类型不匹配则尝试转换
     */
    @Override
    public long getLong() {
        return jsonNode.asLong();
    }

    /**
     * 获取字符串值。
     *
     * @return 字符串值，若节点类型不匹配则尝试转换
     */
    @Override
    public String getString() {
        return jsonNode.asText();
    }

    /**
     * 检查节点是否为null。
     *
     * @return 若节点为null则返回true，否则返回false
     */
    @Override
    public boolean isNull() {
        return jsonNode.isNull();
    }

    /**
     * 检查节点是否为JSON数组。
     *
     * @return 若节点为JSON数组则返回true，否则返回false
     */
    @Override
    public boolean isJsonArray() {
        return jsonNode.isArray();
    }

    /**
     * 检查节点是否为JSON对象。
     *
     * @return 若节点为JSON对象则返回true，否则返回false
     */
    @Override
    public boolean isJsonObject() {
        return jsonNode.isObject();
    }

    /**
     * 将节点转换为JSON数组包装器。
     *
     * @return JSON数组包装器，若节点类型不是数组则返回null
     */
    @Override
    public IJsonArrayWrapper getJsonArray() {
        return jsonNode.isArray() ? new JacksonArrayWrapper(adapter, (ArrayNode) jsonNode) : null;
    }

    /**
     * 将节点转换为JSON对象包装器。
     *
     * @return JSON对象包装器，若节点类型不是对象则返回null
     */
    @Override
    public IJsonObjectWrapper getJsonObject() {
        return jsonNode.isObject() ? new JacksonObjectWrapper(adapter, (ObjectNode) jsonNode) : null;
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
        JacksonNodeWrapper that = (JacksonNodeWrapper) o;
        return new EqualsBuilder()
                .append(jsonNode, that.jsonNode)
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
                .append(jsonNode)
                .toHashCode();
    }

    /**
     * 将当前JSON节点转换为字符串（默认紧凑输出）。
     *
     * @return 转换后的JSON字符串
     */
    @Override
    public String toString() {
        return adapter.toJsonString(jsonNode, false, false);
    }
}
