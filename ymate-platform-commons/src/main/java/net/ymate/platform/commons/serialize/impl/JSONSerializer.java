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
package net.ymate.platform.commons.serialize.impl;

import net.ymate.platform.commons.json.IJsonAdapter;
import net.ymate.platform.commons.json.JsonWrapper;
import net.ymate.platform.commons.json.TypeReferenceWrapper;
import net.ymate.platform.commons.serialize.SerializationException;

/**
 * JSON序列化器实现，基于IJsonAdapter接口提供JSON格式的序列化和反序列化功能。
 * <p>
 * 该序列化器使用JSON格式进行数据序列化，具有跨平台、可读性强的特点。
 * 支持复杂类型的反序列化，包括泛型集合、嵌套对象等。
 * </p>
 * <p>
 * 设计目的：
 * <ul>
 *   <li>提供标准JSON格式的序列化实现</li>
 *   <li>支持跨语言和跨平台的数据交换</li>
 *   <li>提供良好的可读性和调试便利性</li>
 * </ul>
 * </p>
 * <p>
 * 使用场景：
 * <ul>
 *   <li>Web API数据传输</li>
 *   <li>配置文件存储</li>
 *   <li>跨语言系统通信</li>
 *   <li>需要人类可读的数据格式</li>
 * </ul>
 * </p>
 * <p>
 * 注意事项：
 * <ul>
 *   <li>JSON序列化结果通常比二进制格式大</li>
 *   <li>序列化和反序列化性能相对较低</li>
 *   <li>不支持循环引用</li>
 * </ul>
 * </p>
 *
 * @author 刘镇 (suninformation@163.com) on 2017/10/10 上午11:14
 */
public class JSONSerializer extends AbstractSerializer {

    /**
     * 序列化器名称常量。
     */
    public static final String NAME = "json";

    /**
     * JSON适配器实例，用于执行JSON序列化和反序列化操作。
     */
    private final IJsonAdapter jsonAdapter;

    /**
     * 默认构造函数，使用默认的JSON适配器。
     * <p>
     * 该构造函数会通过 JsonWrapper 获取默认的JSON适配器。
     * </p>
     */
    public JSONSerializer() {
        this.jsonAdapter = JsonWrapper.getJsonAdapter();
    }

    /**
     * 使用指定的JSON适配器构造序列化器。
     *
     * @param jsonAdapter JSON适配器实例，不能为null
     * @throws IllegalArgumentException 如果 jsonAdapter 为null
     */
    public JSONSerializer(IJsonAdapter jsonAdapter) {
        if (jsonAdapter == null) {
            throw new IllegalArgumentException("JsonAdapter cannot be null");
        }
        this.jsonAdapter = jsonAdapter;
    }

    /**
     * 获取内容类型。
     *
     * @return 内容类型字符串 "application/json"
     */
    @Override
    public String getContentType() {
        return "application/json";
    }

    /**
     * 将对象序列化为字节数组。
     * <p>
     * 该方法使用JSON适配器将对象序列化为JSON格式的字节数组。
     * </p>
     *
     * @param object 要序列化的对象，不能为null
     * @return 序列化后的字节数组，为JSON格式
     * @throws SerializationException 当序列化过程中发生错误时抛出
     */
    @Override
    public byte[] serialize(Object object) throws SerializationException {
        validateSerializeInput(object);
        try {
            return jsonAdapter.serialize(object);
        } catch (Exception e) {
            throw new SerializationException(NAME, SerializationException.OperationType.SERIALIZE, "Failed to serialize object", e);
        }
    }

    /**
     * 将字节数组反序列化为指定类型的对象。
     * <p>
     * 该方法使用JSON适配器将JSON格式的字节数组反序列化为对象。
     * </p>
     *
     * @param <T>   目标对象的类型
     * @param bytes 要反序列化的字节数组，不能为null或空数组
     * @param clazz 目标对象的Class对象，用于类型转换，不能为null
     * @return 反序列化后的对象实例
     * @throws SerializationException 当反序列化过程中发生错误时抛出
     */
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) throws SerializationException {
        validateDeserializeInput(bytes, clazz);
        try {
            return jsonAdapter.deserialize(bytes, clazz);
        } catch (Exception e) {
            throw new SerializationException(NAME, SerializationException.OperationType.DESERIALIZE, "Failed to deserialize object", e);
        }
    }

    /**
     * 将字节数组反序列化为指定类型的对象，支持复杂类型。
     * <p>
     * 该方法使用JSON适配器和类型引用包装器将JSON格式的字节数组反序列化为对象。
     * 支持复杂类型，如泛型集合、嵌套对象等。
     * </p>
     *
     * @param <T>     目标对象的类型
     * @param bytes   要反序列化的字节数组，不能为null或空数组
     * @param typeRef 类型引用包装器，用于描述复杂类型，不能为null
     * @return 反序列化后的对象实例
     * @throws SerializationException 当反序列化过程中发生错误时抛出
     */
    @Override
    public <T> T deserialize(byte[] bytes, TypeReferenceWrapper<T> typeRef) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            throw new SerializationException(null, SerializationException.OperationType.DESERIALIZE, "Bytes to deserialize cannot be null or empty");
        }
        if (typeRef == null) {
            throw new SerializationException(null, SerializationException.OperationType.DESERIALIZE, "TypeReferenceWrapper cannot be null");
        }
        try {
            return jsonAdapter.deserialize(bytes, typeRef);
        } catch (Exception e) {
            throw new SerializationException(NAME, SerializationException.OperationType.DESERIALIZE, "Failed to deserialize object", e);
        }
    }
}
