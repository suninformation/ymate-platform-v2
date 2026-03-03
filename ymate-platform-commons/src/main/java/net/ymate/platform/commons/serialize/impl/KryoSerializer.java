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

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import net.ymate.platform.commons.serialize.SerializationException;
import net.ymate.platform.commons.serialize.annotation.Serializer;
import net.ymate.platform.commons.serialize.kryo.KryoFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Kryo序列化器实现，基于Kryo库提供高性能的序列化和反序列化功能。
 * <p>
 * 该序列化器使用Kryo库进行数据序列化，具有极高的性能和低内存占用。
 * Kryo是一个快速的Java序列化框架，比Java原生序列化快数倍，且生成的序列化结果更小。
 * </p>
 * <p>
 * 设计目的：
 * <ul>
 *   <li>提供极致的序列化性能</li>
 *   <li>支持序列化任何Java对象，包括未实现Serializable接口的对象</li>
 *   <li>减少序列化结果的大小</li>
 *   <li>支持复杂的对象图序列化</li>
 * </ul>
 * </p>
 * <p>
 * 使用场景：
 * <ul>
 *   <li>需要极高性能序列化的场景</li>
 *   <li>大数据量的对象序列化</li>
 *   <li>游戏开发中的对象序列化</li>
 *   <li>缓存数据序列化</li>
 *   <li>分布式系统数据传输</li>
 * </ul>
 * </p>
 * <p>
 * 注意事项：
 * <ul>
 *   <li>需要添加Kryo库依赖</li>
 *   <li>该序列化器通过SPI机制动态加载，仅在Kryo库可用时注册</li>
 *   <li>如果Kryo库不可用，序列化和反序列化操作将抛出SerializationException</li>
 *   <li>序列化结果为二进制格式，不可读</li>
 *   <li>跨语言支持有限</li>
 *   <li>Kryo实例不是线程安全的，需要为每个线程创建独立实例</li>
 * </ul>
 * </p>
 *
 * @author 刘镇 (suninformation@163.com) on 2026-01-12 01:39:36
 * @since 2.1.4
 */
@Serializer(KryoSerializer.NAME)
public class KryoSerializer extends AbstractSerializer {

    /**
     * 序列化器名称常量。
     */
    public static final String NAME = "kryo";

    /**
     * Kryo序列化器是否可用的标志。
     */
    private boolean available;

    /**
     * 默认构造函数。
     * <p>
     * 该构造函数会检查Kryo库是否可用。
     * </p>
     */
    public KryoSerializer() {
        try {
            // 尝试访问Kryo相关类，以检查Kryo库是否可用
            Class.forName("com.esotericsoftware.kryo.Kryo");
            Class.forName("com.esotericsoftware.kryo.io.Output");
            Class.forName("com.esotericsoftware.kryo.io.Input");
            this.available = true;
        } catch (LinkageError | Exception ignored) {
            this.available = false;
        }
    }

    /**
     * 获取内容类型。
     *
     * @return 内容类型字符串 "application/x-kryo"
     */
    @Override
    public String getContentType() {
        return "application/x-kryo";
    }

    /**
     * 创建新的Kryo实例。
     * <p>
     * 由于Kryo实例不是线程安全的，每次序列化和反序列化操作都需要创建新的实例。
     * 该方法通过KryoFactory获取Kryo实例，支持通过SPI机制扩展自定义配置。
     * </p>
     *
     * @return Kryo实例
     */
    private Kryo createKryo() {
        return KryoFactory.getInstance().createKryo();
    }

    /**
     * 将对象序列化为字节数组。
     * <p>
     * 该方法使用Kryo库将对象序列化为字节数组。
     * </p>
     *
     * @param object 要序列化的对象，不能为null
     * @return 序列化后的字节数组
     * @throws SerializationException 当Kryo库不可用或序列化过程中发生错误时抛出
     */
    @Override
    public byte[] serialize(Object object) throws SerializationException {
        validateSerializeInput(object);
        if (!available) {
            throw new SerializationException(
                    NAME,
                    SerializationException.OperationType.SERIALIZE,
                    "Kryo serialization is not available. Please ensure Kryo library is properly configured."
            );
        }
        try (ByteArrayOutputStream byteArrayOutputStream = createOutputStream()) {
            // 创建Kryo实例
            Kryo kryo = createKryo();
            // 创建Output实例
            Output output = new Output(byteArrayOutputStream, DEFAULT_BUFFER_SIZE);
            // 执行序列化
            kryo.writeClassAndObject(output, object);
            // 刷新输出
            output.flush();
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            throw new SerializationException(NAME, SerializationException.OperationType.SERIALIZE, "Failed to serialize object", e);
        }
    }

    /**
     * 将字节数组反序列化为指定类型的对象。
     * <p>
     * 该方法使用Kryo库将字节数组反序列化为对象。
     * </p>
     *
     * @param <T>   目标对象的类型
     * @param bytes 要反序列化的字节数组，不能为null或空数组
     * @param clazz 目标对象的Class对象，用于类型转换，不能为null
     * @return 反序列化后的对象实例
     * @throws SerializationException 当Kryo库不可用或反序列化过程中发生错误时抛出
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(byte[] bytes, Class<T> clazz) throws SerializationException {
        validateDeserializeInput(bytes, clazz);
        if (!available) {
            throw new SerializationException(
                    NAME,
                    SerializationException.OperationType.DESERIALIZE,
                    "Kryo deserialization is not available. Please ensure Kryo library is properly configured."
            );
        }
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes)) {
            // 创建Kryo实例
            Kryo kryo = createKryo();
            // 创建Input实例
            Input input = new Input(byteArrayInputStream);
            // 执行反序列化
            Object result = kryo.readClassAndObject(input);
            validateType(result, clazz);
            return (T) result;
        } catch (Exception e) {
            throw new SerializationException(NAME, SerializationException.OperationType.DESERIALIZE, "Failed to deserialize object", e);
        }
    }
}
