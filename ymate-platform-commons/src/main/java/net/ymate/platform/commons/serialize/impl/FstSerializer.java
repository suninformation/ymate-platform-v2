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
import net.ymate.platform.commons.serialize.annotation.Serializer;
import net.ymate.platform.commons.serialize.fst.FstConfigurationFactory;
import net.ymate.platform.commons.serialize.fst.IFstConfigurationFactory;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * FST（Fast Serialization）序列化器实现，基于FST库提供高性能的序列化和反序列化功能。
 * <p>
 * 该序列化器使用FST库进行数据序列化，具有高性能、低内存占用的特点。
 * FST是一个高性能的Java序列化库，比Java原生序列化快10倍以上。
 * </p>
 * <p>
 * 设计目的：
 * <ul>
 *   <li>提供高性能的序列化实现</li>
 *   <li>减少序列化结果的内存占用</li>
 *   <li>支持复杂的对象图序列化</li>
 * </ul>
 * </p>
 * <p>
 * 使用场景：
 * <ul>
 *   <li>需要高性能序列化的场景</li>
 *   <li>大数据量的对象序列化</li>
 *   <li>缓存数据序列化</li>
 *   <li>分布式系统数据传输</li>
 * </ul>
 * </p>
 * <p>
 * 注意事项：
 * <ul>
 *   <li>需要添加FST库依赖</li>
 *   <li>该序列化器通过SPI机制动态加载，仅在FST库可用时注册</li>
 *   <li>如果FST库不可用，序列化和反序列化操作将抛出SerializationException</li>
 *   <li>序列化结果为二进制格式，不可读</li>
 *   <li>跨语言支持有限</li>
 * </ul>
 * </p>
 *
 * @author 刘镇 (suninformation@163.com) on 2022/9/22 13:20
 * @since 2.1.2
 */
@Serializer(FstSerializer.NAME)
public class FstSerializer extends AbstractSerializer {

    /**
     * 序列化器名称常量。
     */
    public static final String NAME = "fst";

    /**
     * FST配置实例，用于控制FST序列化器的行为。
     */
    private FSTConfiguration fstConfiguration;

    /**
     * 默认构造函数，使用默认的FST配置。
     * <p>
     * 该构造函数会尝试通过 FstConfigurationFactory 获取配置实例。
     * 如果FST库不可用，则 fstConfiguration 将为null。
     * </p>
     */
    public FstSerializer() {
        try {
            IFstConfigurationFactory factory = FstConfigurationFactory.getInstance();
            if (factory != null) {
                this.fstConfiguration = factory.getFstConfiguration();
            }
        } catch (LinkageError | Exception ignored) {
        }
    }

    /**
     * 使用指定的FST配置构造序列化器。
     *
     * @param fstConfiguration FST配置实例，不能为null
     * @throws IllegalArgumentException 如果 fstConfiguration 为null
     */
    public FstSerializer(FSTConfiguration fstConfiguration) {
        if (fstConfiguration == null) {
            throw new IllegalArgumentException("FSTConfiguration cannot be null");
        }
        this.fstConfiguration = fstConfiguration;
    }

    /**
     * 获取内容类型。
     *
     * @return 内容类型字符串 "application/x-java-serialized-fst"
     */
    @Override
    public String getContentType() {
        return "application/x-java-serialized-fst";
    }

    /**
     * 将对象序列化为字节数组。
     * <p>
     * 该方法使用FST库将对象序列化为字节数组。
     * </p>
     *
     * @param object 要序列化的对象，不能为null
     * @return 序列化后的字节数组
     * @throws SerializationException 当FST库不可用或序列化过程中发生错误时抛出
     */
    @Override
    public byte[] serialize(Object object) throws SerializationException {
        validateSerializeInput(object);
        if (fstConfiguration == null) {
            throw new SerializationException(
                    NAME,
                    SerializationException.OperationType.SERIALIZE,
                    "FST serialization is not available. Please ensure FST library is properly configured."
            );
        }
        try (ByteArrayOutputStream outputStream = createOutputStream();
             FSTObjectOutput out = fstConfiguration.getObjectOutput(outputStream)) {
            out.writeObject(object);
            out.flush();
            return outputStream.toByteArray();
        } catch (java.io.IOException e) {
            throw new SerializationException(NAME, SerializationException.OperationType.SERIALIZE, "Failed to serialize object", e);
        }
    }

    /**
     * 将字节数组反序列化为指定类型的对象。
     * <p>
     * 该方法使用FST库将字节数组反序列化为对象。
     * </p>
     *
     * @param <T>   目标对象的类型
     * @param bytes 要反序列化的字节数组，不能为null或空数组
     * @param clazz 目标对象的Class对象，用于类型转换，不能为null
     * @return 反序列化后的对象实例
     * @throws SerializationException 当FST库不可用或反序列化过程中发生错误时抛出
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(byte[] bytes, Class<T> clazz) throws SerializationException {
        validateDeserializeInput(bytes, clazz);
        if (fstConfiguration == null) {
            throw new SerializationException(
                    NAME,
                    SerializationException.OperationType.DESERIALIZE,
                    "FST deserialization is not available. Please ensure FST library is properly configured."
            );
        }
        try (FSTObjectInput input = fstConfiguration.getObjectInput(new ByteArrayInputStream(bytes))) {
            Object obj = input.readObject(clazz);
            validateType(obj, clazz);
            return (T) obj;
        } catch (Exception e) {
            throw new SerializationException(NAME, SerializationException.OperationType.DESERIALIZE, "Failed to deserialize object", e);
        }
    }
}
