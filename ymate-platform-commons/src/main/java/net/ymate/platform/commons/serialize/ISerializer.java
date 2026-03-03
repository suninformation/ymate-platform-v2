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
package net.ymate.platform.commons.serialize;

import net.ymate.platform.commons.json.TypeReferenceWrapper;

/**
 * 序列化器接口，定义了对象序列化和反序列化的标准行为。
 * <p>
 * 该接口提供了将对象转换为字节数组（序列化）以及将字节数组还原为对象（反序列化）的能力。
 * 实现该接口的类可以支持不同的序列化格式，如Java原生序列化、JSON、FST、Hessian等。
 * </p>
 * <p>
 * 设计目的：
 * <ul>
 *   <li>提供统一的序列化接口，支持多种序列化实现</li>
 *   <li>支持泛型类型反序列化，包括复杂类型（如集合、嵌套对象）</li>
 *   <li>便于扩展和替换不同的序列化策略</li>
 * </ul>
 * </p>
 * <p>
 * 使用场景：
 * <ul>
 *   <li>对象持久化存储</li>
 *   <li>网络传输数据序列化</li>
 *   <li>缓存数据序列化</li>
 *   <li>跨进程通信数据交换</li>
 * </ul>
 * </p>
 *
 * @author 刘镇 (suninformation@163.com) on 2017/10/3 下午3:52
 */
public interface ISerializer {

    /**
     * 默认缓冲区大小，用于序列化操作时的字节缓冲。
     */
    int DEFAULT_BUFFER_SIZE = 8192;

    /**
     * 获取序列化器的内容类型。
     * <p>
     * 内容类型用于标识序列化数据的格式，如 application/json、application/x-java-serialized-object 等。
     * </p>
     *
     * @return 内容类型字符串，表示序列化数据的MIME类型
     */
    String getContentType();

    /**
     * 将对象序列化为字节数组。
     * <p>
     * 该方法将指定的Java对象转换为字节数组，以便于存储或网络传输。
     * </p>
     *
     * @param object 要序列化的对象，不能为null
     * @return 序列化后的字节数组
     * @throws SerializationException 当序列化过程中发生错误时抛出
     */
    byte[] serialize(Object object) throws SerializationException;

    /**
     * 将字节数组反序列化为指定类型的对象。
     * <p>
     * 该方法将字节数组还原为指定类型的Java对象。
     * </p>
     *
     * @param <T>   目标对象的类型
     * @param bytes 要反序列化的字节数组，不能为null或空数组
     * @param clazz 目标对象的Class对象，用于类型转换，不能为null
     * @return 反序列化后的对象实例
     * @throws SerializationException 当反序列化过程中发生错误时抛出
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz) throws SerializationException;

    /**
     * 将字节数组反序列化为指定类型的对象，支持复杂类型。
     * <p>
     * 该方法使用 TypeReferenceWrapper 支持复杂类型的反序列化，如泛型集合、嵌套对象等。
     * 默认实现将类型引用转换为 Class 对象后调用 {@link #deserialize(byte[], Class)} 方法。
     * </p>
     *
     * @param <T>     目标对象的类型
     * @param bytes   要反序列化的字节数组，不能为null或空数组
     * @param typeRef 类型引用包装器，用于描述复杂类型，不能为null
     * @return 反序列化后的对象实例
     * @throws SerializationException 当反序列化过程中发生错误时抛出
     */
    @SuppressWarnings("unchecked")
    default <T> T deserialize(byte[] bytes, TypeReferenceWrapper<T> typeRef) throws SerializationException {
        return deserialize(bytes, (Class<T>) typeRef.getType());
    }

    /**
     * 验证序列化输入参数的有效性。
     * <p>
     * 该方法检查待序列化的对象是否为null，如果是则抛出异常。
     * </p>
     *
     * @param object 待验证的对象
     * @throws SerializationException 当对象为null时抛出
     */
    default void validateSerializeInput(Object object) {
        if (object == null) {
            throw new SerializationException(null, SerializationException.OperationType.SERIALIZE, "Object to serialize cannot be null");
        }
    }

    /**
     * 验证反序列化输入参数的有效性。
     * <p>
     * 该方法检查字节数组和目标Class对象是否有效。
     * </p>
     *
     * @param bytes 待验证的字节数组
     * @param clazz 目标对象的Class对象
     * @throws SerializationException 当字节数组为null或空，或Class对象为null时抛出
     */
    default void validateDeserializeInput(byte[] bytes, Class<?> clazz) {
        if (bytes == null || bytes.length == 0) {
            throw new SerializationException(null, SerializationException.OperationType.DESERIALIZE, "Bytes to deserialize cannot be null or empty");
        }
        if (clazz == null) {
            throw new SerializationException(null, SerializationException.OperationType.DESERIALIZE, "Target class cannot be null");
        }
    }
}
