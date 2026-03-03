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

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import net.ymate.platform.commons.serialize.SerializationException;
import net.ymate.platform.commons.serialize.annotation.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Hessian序列化器实现，基于Hessian二进制协议提供高效的序列化和反序列化功能。
 * <p>
 * 该序列化器使用Hessian协议进行数据序列化，具有跨语言、高性能的特点。
 * Hessian是一种轻量级的二进制协议，支持多种编程语言。
 * </p>
 * <p>
 * 设计目的：
 * <ul>
 *   <li>提供跨语言的序列化实现</li>
 *   <li>提供高性能的二进制序列化</li>
 *   <li>支持多种编程语言的数据交换</li>
 * </ul>
 * </p>
 * <p>
 * 使用场景：
 * <ul>
 *   <li>跨语言系统通信</li>
 *   <li>RPC远程调用</li>
 *   <li>高性能数据传输</li>
 *   <li>分布式系统数据交换</li>
 * </ul>
 * </p>
 * <p>
 * 注意事项：
 * <ul>
 *   <li>需要添加Hessian库依赖</li>
 *   <li>该序列化器通过SPI机制动态加载，仅在Hessian库可用时注册</li>
 *   <li>序列化结果为二进制格式，不可读</li>
 *   <li>某些Java特性可能不完全支持</li>
 * </ul>
 * </p>
 *
 * @author 刘镇 (suninformation@163.com) on 2021/6/14 9:57 下午
 * @since 2.1.0
 */
@Serializer(HessianSerializer.NAME)
public class HessianSerializer extends AbstractSerializer {

    /**
     * 序列化器名称常量。
     */
    public static final String NAME = "hessian";

    /**
     * 获取内容类型。
     *
     * @return 内容类型字符串 "application/x-java-serialized-hessian"
     */
    @Override
    public String getContentType() {
        return "application/x-java-serialized-hessian";
    }

    /**
     * 将对象序列化为字节数组。
     * <p>
     * 该方法使用Hessian协议将对象序列化为字节数组。
     * </p>
     *
     * @param object 要序列化的对象，不能为null
     * @return 序列化后的字节数组
     * @throws SerializationException 当序列化过程中发生IO错误时抛出
     */
    @Override
    public byte[] serialize(Object object) throws SerializationException {
        validateSerializeInput(object);
        ByteArrayOutputStream outputStream = createOutputStream();
        Hessian2Output out = new Hessian2Output(outputStream);
        try {
            out.writeObject(object);
            out.flush();
            return outputStream.toByteArray();
        } catch (java.io.IOException e) {
            throw new SerializationException(NAME, SerializationException.OperationType.SERIALIZE, "Failed to serialize object", e);
        } finally {
            try {
                out.close();
            } catch (java.io.IOException ignored) {
            }
        }
    }

    /**
     * 将字节数组反序列化为指定类型的对象。
     * <p>
     * 该方法使用Hessian协议将字节数组反序列化为对象。
     * </p>
     *
     * @param <T>   目标对象的类型
     * @param bytes 要反序列化的字节数组，不能为null或空数组
     * @param clazz 目标对象的Class对象，用于类型转换，不能为null
     * @return 反序列化后的对象实例
     * @throws SerializationException 当反序列化过程中发生IO错误时抛出
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(byte[] bytes, Class<T> clazz) throws SerializationException {
        validateDeserializeInput(bytes, clazz);
        Hessian2Input input = new Hessian2Input(new ByteArrayInputStream(bytes));
        try {
            Object obj = input.readObject(clazz);
            validateType(obj, clazz);
            return (T) obj;
        } catch (java.io.IOException e) {
            throw new SerializationException(NAME, SerializationException.OperationType.DESERIALIZE, "Failed to deserialize object", e);
        } finally {
            try {
                input.close();
            } catch (java.io.IOException ignored) {
            }
        }
    }
}
