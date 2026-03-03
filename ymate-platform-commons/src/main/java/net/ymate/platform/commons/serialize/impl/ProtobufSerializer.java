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

import com.google.protobuf.Message;
import com.google.protobuf.MessageLite;
import net.ymate.platform.commons.serialize.SerializationException;
import net.ymate.platform.commons.serialize.annotation.Serializer;

import java.lang.reflect.Method;

/**
 * Protobuf（Protocol Buffers）序列化器实现，基于Google Protobuf库提供高效的序列化和反序列化功能。
 * <p>
 * 该序列化器使用Protobuf库进行数据序列化，具有高性能、跨语言支持和良好的向前/向后兼容性。
 * Protobuf是Google开发的一种数据序列化格式，比XML和JSON更小、更快、更简单。
 * </p>
 * <p>
 * 设计目的：
 * <ul>
 *   <li>提供高效的序列化实现</li>
 *   <li>支持跨语言数据交换</li>
 *   <li>提供良好的向前/向后兼容性</li>
 *   <li>减少序列化结果的大小</li>
 * </ul>
 * </p>
 * <p>
 * 使用场景：
 * <ul>
 *   <li>需要高性能序列化的场景</li>
 *   <li>跨语言系统数据传输</li>
 *   <li>需要向前/向后兼容性的场景</li>
 *   <li>网络传输数据序列化</li>
 * </ul>
 * </p>
 * <p>
 * 注意事项：
 * <ul>
 *   <li>需要添加Protobuf库依赖</li>
 *   <li>该序列化器通过SPI机制动态加载，仅在Protobuf库可用时注册</li>
 *   <li>如果Protobuf库不可用，序列化和反序列化操作将抛出SerializationException</li>
 *   <li>序列化结果为二进制格式，不可读</li>
 *   <li>仅支持实现了Message或MessageLite接口的对象序列化</li>
 * </ul>
 * </p>
 *
 * @author 刘镇 (suninformation@163.com) on 2026-01-12 01:39:36
 * @since 2.1.4
 */
@Serializer(ProtobufSerializer.NAME)
public class ProtobufSerializer extends AbstractSerializer {

    /**
     * 序列化器名称常量。
     */
    public static final String NAME = "protobuf";

    /**
     * Protobuf序列化器是否可用的标志。
     */
    private boolean available;

    /**
     * 默认构造函数。
     * <p>
     * 该构造函数会检查Protobuf库是否可用。
     * </p>
     */
    public ProtobufSerializer() {
        try {
            // 尝试访问Message和MessageLite类，以检查Protobuf库是否可用
            Class.forName("com.google.protobuf.Message");
            Class.forName("com.google.protobuf.MessageLite");
            this.available = true;
        } catch (LinkageError | Exception ignored) {
            this.available = false;
        }
    }

    /**
     * 获取内容类型。
     *
     * @return 内容类型字符串 "application/x-protobuf"
     */
    @Override
    public String getContentType() {
        return "application/x-protobuf";
    }

    /**
     * 将对象序列化为字节数组。
     * <p>
     * 该方法使用Protobuf库将对象序列化为字节数组。
     * 仅支持实现了Message或MessageLite接口的对象。
     * </p>
     *
     * @param object 要序列化的对象，不能为null且必须实现Message或MessageLite接口
     * @return 序列化后的字节数组
     * @throws SerializationException 当Protobuf库不可用、对象类型不支持或序列化过程中发生错误时抛出
     */
    @Override
    public byte[] serialize(Object object) throws SerializationException {
        validateSerializeInput(object);
        if (!available) {
            throw new SerializationException(
                    NAME,
                    SerializationException.OperationType.SERIALIZE,
                    "Protobuf serialization is not available. Please ensure Protobuf library is properly configured."
            );
        }
        if (!(object instanceof MessageLite)) {
            throw new SerializationException(
                    NAME,
                    SerializationException.OperationType.SERIALIZE,
                    "Object must implement com.google.protobuf.Message or com.google.protobuf.MessageLite interface."
            );
        }
        try {
            if (object instanceof Message) {
                return ((Message) object).toByteArray();
            } else {
                return ((MessageLite) object).toByteArray();
            }
        } catch (Exception e) {
            throw new SerializationException(NAME, SerializationException.OperationType.SERIALIZE, "Failed to serialize object", e);
        }
    }

    /**
     * 将字节数组反序列化为指定类型的对象。
     * <p>
     * 该方法使用Protobuf库将字节数组反序列化为对象。
     * 目标类必须有一个静态的parseFrom(byte[])方法。
     * </p>
     *
     * @param <T>   目标对象的类型
     * @param bytes 要反序列化的字节数组，不能为null或空数组
     * @param clazz 目标对象的Class对象，用于类型转换，不能为null且必须是Protobuf消息类型
     * @return 反序列化后的对象实例
     * @throws SerializationException 当Protobuf库不可用、目标类不支持或反序列化过程中发生错误时抛出
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(byte[] bytes, Class<T> clazz) throws SerializationException {
        validateDeserializeInput(bytes, clazz);
        if (!available) {
            throw new SerializationException(
                    NAME,
                    SerializationException.OperationType.DESERIALIZE,
                    "Protobuf deserialization is not available. Please ensure Protobuf library is properly configured."
            );
        }
        try {
            // 直接调用parseFrom方法
            Method parseFromMethod = clazz.getMethod("parseFrom", byte[].class);
            T result = (T) parseFromMethod.invoke(null, (Object) bytes);
            validateType(result, clazz);
            return result;
        } catch (Exception e) {
            throw new SerializationException(NAME, SerializationException.OperationType.DESERIALIZE, "Failed to deserialize object", e);
        }
    }
}
