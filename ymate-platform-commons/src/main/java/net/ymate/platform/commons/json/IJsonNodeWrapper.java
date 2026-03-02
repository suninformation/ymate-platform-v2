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

/**
 * JSON节点包装器接口，用于封装不同JSON库的节点实现，提供统一的JSON节点访问方法。
 * <p>
 * 设计目的：实现JSON节点操作的统一抽象，降低对具体JSON库的依赖，提高代码的可移植性和扩展性。
 * <p>
 * 使用场景：
 * - 需要对JSON节点进行统一的类型检查和值获取操作
 * - 希望通过统一的API访问不同JSON库的节点实现
 * - 需要支持多种JSON节点类型的转换和操作
 *
 * @author 刘镇 (suninformation@163.com) on 2020/6/20 3:30 下午
 * @since 2.1.0
 */
public interface IJsonNodeWrapper extends Serializable {

    /**
     * 获取原始节点值。
     *
     * @return 原始节点值，可能为null
     */
    Object get();

    /**
     * 获取布尔值。
     *
     * @return 布尔值，若节点类型不匹配则返回false
     */
    boolean getBoolean();

    /**
     * 获取大整数。
     *
     * @return 大整数值，若节点类型不匹配则返回null
     */
    BigInteger getBigInteger();

    /**
     * 获取大小数。
     *
     * @return 大小数值，若节点类型不匹配则返回null
     */
    BigDecimal getBigDecimal();

    /**
     * 获取双精度浮点数。
     *
     * @return 双精度浮点数，若节点类型不匹配则返回0.0
     */
    double getDouble();

    /**
     * 获取单精度浮点数。
     *
     * @return 单精度浮点数，若节点类型不匹配则返回0.0f
     */
    float getFloat();

    /**
     * 获取整数。
     *
     * @return 整数值，若节点类型不匹配则返回0
     */
    int getInt();

    /**
     * 获取长整数。
     *
     * @return 长整数值，若节点类型不匹配则返回0L
     */
    long getLong();

    /**
     * 获取字符串值。
     *
     * @return 字符串值，若节点类型不匹配则返回null
     */
    String getString();

    /**
     * 检查节点值是否为null。
     *
     * @return 若节点值为null则返回true，否则返回false
     */
    boolean isNull();

    /**
     * 检查节点是否为JSON数组。
     *
     * @return 若节点为JSON数组则返回true，否则返回false
     */
    boolean isJsonArray();

    /**
     * 检查节点是否为JSON对象。
     *
     * @return 若节点为JSON对象则返回true，否则返回false
     */
    boolean isJsonObject();

    /**
     * 获取JSON数组包装器。
     *
     * @return JSON数组包装器，若节点类型不是JSON数组则返回null
     */
    IJsonArrayWrapper getJsonArray();

    /**
     * 获取JSON对象包装器。
     *
     * @return JSON对象包装器，若节点类型不是JSON对象则返回null
     */
    IJsonObjectWrapper getJsonObject();
}
