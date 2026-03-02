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
import java.util.List;
import java.util.Map;

/**
 * JSON数组包装器接口，用于封装不同JSON库的数组实现，提供统一的JSON数组操作方法。
 * <p>
 * 设计目的：实现JSON数组操作的统一抽象，降低对具体JSON库的依赖，提高代码的可移植性和扩展性。
 * <p>
 * 使用场景：
 * - 需要对JSON数组进行统一的创建、读取、修改和转换操作
 * - 希望通过统一的API操作不同JSON库的数组实现
 * - 需要支持多种JSON格式配置，如格式化输出、空值处理、命名转换等
 *
 * @author 刘镇 (suninformation@163.com) on 2020/6/9 4:26 下午
 * @since 2.1.0
 */
public interface IJsonArrayWrapper extends Serializable {

    /**
     * 获取指定索引的JSON节点包装器。
     *
     * @param index 数组索引，必须大于等于0且小于数组大小
     * @return JSON节点包装器，若索引越界则返回null
     */
    IJsonNodeWrapper get(int index);

    /**
     * 获取指定索引的布尔值。
     *
     * @param index 数组索引，必须大于等于0且小于数组大小
     * @return 布尔值，若索引越界或类型不匹配则返回false
     */
    boolean getBoolean(int index);

    /**
     * 获取指定索引的布尔值，支持null。
     *
     * @param index 数组索引，必须大于等于0且小于数组大小
     * @return 布尔值包装类，若索引越界则返回null
     */
    Boolean getAsBoolean(int index);

    /**
     * 获取指定索引的双精度浮点数。
     *
     * @param index 数组索引，必须大于等于0且小于数组大小
     * @return 双精度浮点数，若索引越界或类型不匹配则返回0.0
     */
    double getDouble(int index);

    /**
     * 获取指定索引的双精度浮点数，支持null。
     *
     * @param index 数组索引，必须大于等于0且小于数组大小
     * @return 双精度浮点数包装类，若索引越界则返回null
     */
    Double getAsDouble(int index);

    /**
     * 获取指定索引的单精度浮点数。
     *
     * @param index 数组索引，必须大于等于0且小于数组大小
     * @return 单精度浮点数，若索引越界或类型不匹配则返回0.0f
     */
    float getFloat(int index);

    /**
     * 获取指定索引的单精度浮点数，支持null。
     *
     * @param index 数组索引，必须大于等于0且小于数组大小
     * @return 单精度浮点数包装类，若索引越界则返回null
     */
    Float getAsFloat(int index);

    /**
     * 获取指定索引的大小数。
     *
     * @param index 数组索引，必须大于等于0且小于数组大小
     * @return 大小数值，若索引越界或类型不匹配则返回null
     */
    BigDecimal getBigDecimal(int index);

    /**
     * 获取指定索引的大整数。
     *
     * @param index 数组索引，必须大于等于0且小于数组大小
     * @return 大整数值，若索引越界或类型不匹配则返回null
     */
    BigInteger getBigInteger(int index);

    /**
     * 获取指定索引的整数。
     *
     * @param index 数组索引，必须大于等于0且小于数组大小
     * @return 整数值，若索引越界或类型不匹配则返回0
     */
    int getInt(int index);

    /**
     * 获取指定索引的整数，支持null。
     *
     * @param index 数组索引，必须大于等于0且小于数组大小
     * @return 整数包装类，若索引越界则返回null
     */
    Integer getAsInteger(int index);

    /**
     * 获取指定索引的JSON数组包装器。
     *
     * @param index 数组索引，必须大于等于0且小于数组大小
     * @return JSON数组包装器，若索引越界或类型不匹配则返回null
     */
    IJsonArrayWrapper getJsonArray(int index);

    /**
     * 获取指定索引的JSON对象包装器。
     *
     * @param index 数组索引，必须大于等于0且小于数组大小
     * @return JSON对象包装器，若索引越界或类型不匹配则返回null
     */
    IJsonObjectWrapper getJsonObject(int index);

    /**
     * 获取指定索引的长整数。
     *
     * @param index 数组索引，必须大于等于0且小于数组大小
     * @return 长整数值，若索引越界或类型不匹配则返回0L
     */
    long getLong(int index);

    /**
     * 获取指定索引的长整数，支持null。
     *
     * @param index 数组索引，必须大于等于0且小于数组大小
     * @return 长整数包装类，若索引越界则返回null
     */
    Long getAsLong(int index);

    /**
     * 获取指定索引的字符串值。
     *
     * @param index 数组索引，必须大于等于0且小于数组大小
     * @return 字符串值，若索引越界则返回null
     */
    String getString(int index);

    /**
     * 检查指定索引的值是否为null。
     *
     * @param index 数组索引，必须大于等于0且小于数组大小
     * @return 若指定索引的值为null则返回true，否则返回false
     */
    boolean isNull(int index);

    /**
     * 获取JSON数组的大小。
     *
     * @return JSON数组中元素的数量
     */
    int size();

    /**
     * 检查JSON数组是否为空。
     *
     * @return 若JSON数组为空则返回true，否则返回false
     */
    boolean isEmpty();

    /**
     * 向JSON数组末尾添加布尔值，支持链式调用。
     *
     * @param value 布尔值
     * @return 当前JSON数组包装器实例，用于链式调用
     */
    IJsonArrayWrapper add(boolean value);

    /**
     * 向JSON数组末尾添加集合，支持链式调用。
     *
     * @param value 集合对象
     * @return 当前JSON数组包装器实例，用于链式调用
     */
    IJsonArrayWrapper add(Collection<?> value);

    /**
     * 向JSON数组末尾添加双精度浮点数，支持链式调用。
     *
     * @param value 双精度浮点数
     * @return 当前JSON数组包装器实例，用于链式调用
     */
    IJsonArrayWrapper add(double value);

    /**
     * 向JSON数组末尾添加单精度浮点数，支持链式调用。
     *
     * @param value 单精度浮点数
     * @return 当前JSON数组包装器实例，用于链式调用
     */
    IJsonArrayWrapper add(float value);

    /**
     * 向JSON数组末尾添加整数，支持链式调用。
     *
     * @param value 整数值
     * @return 当前JSON数组包装器实例，用于链式调用
     */
    IJsonArrayWrapper add(int value);

    /**
     * 向JSON数组末尾添加长整数，支持链式调用。
     *
     * @param value 长整数值
     * @return 当前JSON数组包装器实例，用于链式调用
     */
    IJsonArrayWrapper add(long value);

    /**
     * 向JSON数组末尾添加Map，支持链式调用。
     *
     * @param value Map对象
     * @return 当前JSON数组包装器实例，用于链式调用
     */
    IJsonArrayWrapper add(Map<?, ?> value);

    /**
     * 向JSON数组末尾添加任意类型值，支持链式调用。
     *
     * @param value 任意类型值
     * @return 当前JSON数组包装器实例，用于链式调用
     */
    IJsonArrayWrapper add(Object value);

    /**
     * 向JSON数组指定索引位置添加布尔值，支持链式调用。
     *
     * @param index 数组索引，必须大于等于0且小于等于数组大小
     * @param value 布尔值
     * @return 当前JSON数组包装器实例，用于链式调用
     */
    IJsonArrayWrapper add(int index, boolean value);

    /**
     * 向JSON数组指定索引位置添加集合，支持链式调用。
     *
     * @param index 数组索引，必须大于等于0且小于等于数组大小
     * @param value 集合对象
     * @return 当前JSON数组包装器实例，用于链式调用
     */
    IJsonArrayWrapper add(int index, Collection<?> value);

    /**
     * 向JSON数组指定索引位置添加双精度浮点数，支持链式调用。
     *
     * @param index 数组索引，必须大于等于0且小于等于数组大小
     * @param value 双精度浮点数
     * @return 当前JSON数组包装器实例，用于链式调用
     */
    IJsonArrayWrapper add(int index, double value);

    /**
     * 向JSON数组指定索引位置添加单精度浮点数，支持链式调用。
     *
     * @param index 数组索引，必须大于等于0且小于等于数组大小
     * @param value 单精度浮点数
     * @return 当前JSON数组包装器实例，用于链式调用
     */
    IJsonArrayWrapper add(int index, float value);

    /**
     * 向JSON数组指定索引位置添加整数，支持链式调用。
     *
     * @param index 数组索引，必须大于等于0且小于等于数组大小
     * @param value 整数值
     * @return 当前JSON数组包装器实例，用于链式调用
     */
    IJsonArrayWrapper add(int index, int value);

    /**
     * 向JSON数组指定索引位置添加长整数，支持链式调用。
     *
     * @param index 数组索引，必须大于等于0且小于等于数组大小
     * @param value 长整数值
     * @return 当前JSON数组包装器实例，用于链式调用
     */
    IJsonArrayWrapper add(int index, long value);

    /**
     * 向JSON数组指定索引位置添加Map，支持链式调用。
     *
     * @param index 数组索引，必须大于等于0且小于等于数组大小
     * @param value Map对象
     * @return 当前JSON数组包装器实例，用于链式调用
     */
    IJsonArrayWrapper add(int index, Map<?, ?> value);

    /**
     * 向JSON数组指定索引位置添加任意类型值，支持链式调用。
     *
     * @param index 数组索引，必须大于等于0且小于等于数组大小
     * @param value 任意类型值
     * @return 当前JSON数组包装器实例，用于链式调用
     */
    IJsonArrayWrapper add(int index, Object value);

    /**
     * 移除指定索引的元素。
     *
     * @param index 数组索引，必须大于等于0且小于数组大小
     * @return 被移除的值，若索引越界则返回null
     */
    Object remove(int index);

    /**
     * 将JSON数组转换为字符串，支持格式化输出和保留空值。
     *
     * @param format        是否格式化输出，true表示格式化（带缩进），false表示紧凑输出
     * @param keepNullValue 是否保留空值，true表示保留，false表示忽略
     * @return 转换后的JSON字符串
     */
    String toString(boolean format, boolean keepNullValue);

    /**
     * 将JSON数组转换为字符串，支持格式化输出、保留空值和蛇形命名转换。
     *
     * @param format        是否格式化输出，true表示格式化（带缩进），false表示紧凑输出
     * @param keepNullValue 是否保留空值，true表示保留，false表示忽略
     * @param snakeCase     是否将驼峰命名转换为蛇形命名
     * @return 转换后的JSON字符串
     */
    String toString(boolean format, boolean keepNullValue, boolean snakeCase);

    /**
     * 将JSON数组转换为字符串，支持格式化输出、保留空值和属性过滤。
     *
     * @param format        是否格式化输出，true表示格式化（带缩进），false表示紧凑输出
     * @param keepNullValue 是否保留空值，true表示保留，false表示忽略
     * @param filter        属性过滤器，用于控制哪些属性被序列化
     * @return 转换后的JSON字符串
     * @since 2.1.4
     */
    String toString(boolean format, boolean keepNullValue, IJsonPropertyFilter filter);

    /**
     * 将JSON数组转换为字符串，支持格式化输出、保留空值、蛇形命名转换和属性过滤。
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
     * 将JSON数组转换为List。
     *
     * @return List对象，包含JSON数组的所有元素
     */
    List<Object> toList();

    /**
     * 将JSON数组转换为Object数组。
     *
     * @return Object数组，包含JSON数组的所有元素
     */
    Object[] toArray();

    /**
     * 将当前JSON数组包装器转换为JsonWrapper对象。
     *
     * @return JsonWrapper对象，包含当前JSON数组包装器
     * @since 2.1.4
     */
    JsonWrapper wrap();
}
