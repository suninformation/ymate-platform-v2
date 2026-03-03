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

import net.ymate.platform.commons.serialize.SerializationException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Java原生序列化器实现，基于Java标准库的ObjectOutputStream和ObjectInputStream。
 * <p>
 * 该序列化器使用Java原生的序列化机制，支持所有实现了Serializable接口的对象。
 * 序列化后的数据格式为Java专有的二进制格式，具有较好的兼容性和稳定性。
 * </p>
 * <p>
 * 设计目的：
 * <ul>
 *   <li>提供标准的Java序列化实现</li>
 *   <li>作为默认序列化器，确保基本功能的可用性</li>
 *   <li>支持所有可序列化的Java对象</li>
 * </ul>
 * </p>
 * <p>
 * 使用场景：
 * <ul>
 *   <li>需要标准Java序列化格式的场景</li>
 *   <li>对象持久化存储</li>
 *   <li>跨JVM的对象传输</li>
 * </ul>
 * </p>
 * <p>
 * 注意事项：
 * <ul>
 *   <li>被序列化的对象必须实现Serializable接口</li>
 *   <li>序列化效率相对较低，不适合高性能场景</li>
 *   <li>序列化结果较大，占用较多存储空间</li>
 * </ul>
 * </p>
 *
 * @author 刘镇 (suninformation@163.com) on 2017/10/10 上午11:13
 */
public class DefaultSerializer extends AbstractSerializer {

    /**
     * 序列化器名称常量。
     */
    public static final String NAME = "default";

    /**
     * 获取内容类型。
     *
     * @return 内容类型字符串 "application/x-java-serialized-object"
     */
    @Override
    public String getContentType() {
        return "application/x-java-serialized-object";
    }

    /**
     * 将对象序列化为字节数组。
     * <p>
     * 该方法使用Java原生的ObjectOutputStream将对象序列化为字节数组。
     * </p>
     *
     * @param object 要序列化的对象，不能为null
     * @return 序列化后的字节数组
     * @throws SerializationException 当序列化过程中发生IO错误时抛出
     */
    @Override
    public byte[] serialize(Object object) throws SerializationException {
        validateSerializeInput(object);
        try (ByteArrayOutputStream stream = createOutputStream();
             ObjectOutputStream output = new ObjectOutputStream(stream)) {
            output.writeObject(object);
            return stream.toByteArray();
        } catch (java.io.IOException e) {
            throw new SerializationException(NAME, SerializationException.OperationType.SERIALIZE, "Failed to serialize object", e);
        }
    }

    /**
     * 将字节数组反序列化为指定类型的对象。
     * <p>
     * 该方法使用Java原生的ObjectInputStream将字节数组反序列化为对象。
     * </p>
     *
     * @param <T>   目标对象的类型
     * @param bytes 要反序列化的字节数组，不能为null或空数组
     * @param clazz 目标对象的Class对象，用于类型转换，不能为null
     * @return 反序列化后的对象实例
     * @throws SerializationException 当反序列化过程中发生IO错误或类未找到时抛出
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(byte[] bytes, Class<T> clazz) throws SerializationException {
        validateDeserializeInput(bytes, clazz);
        try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            Object obj = input.readObject();
            validateType(obj, clazz);
            return (T) obj;
        } catch (java.io.IOException | ClassNotFoundException e) {
            throw new SerializationException(NAME, SerializationException.OperationType.DESERIALIZE, "Failed to deserialize object", e);
        }
    }
}
