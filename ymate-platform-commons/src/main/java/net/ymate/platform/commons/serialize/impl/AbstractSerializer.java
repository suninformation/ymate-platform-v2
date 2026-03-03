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

import net.ymate.platform.commons.serialize.ISerializer;
import net.ymate.platform.commons.serialize.SerializationException;

import java.io.ByteArrayOutputStream;

/**
 * 序列化器抽象基类，提供了序列化和反序列化的通用实现和辅助方法。
 * <p>
 * 该抽象类实现了 ISerializer 接口，并提供了以下功能：
 * <ul>
 *   <li>输入参数校验方法（序列化和反序列化）</li>
 *   <li>创建 ByteArrayOutputStream 的辅助方法</li>
 *   <li>类型转换安全检查方法</li>
 * </ul>
 * </p>
 * <p>
 * 设计目的：
 * <ul>
 *   <li>减少子类代码重复，提高代码复用性</li>
 *   <li>统一参数校验逻辑，确保输入参数的有效性</li>
 *   <li>提供类型安全的反序列化实现</li>
 * </ul>
 * </p>
 * <p>
 * 使用场景：
 * <ul>
 *   <li>作为所有序列化器实现的基类</li>
 *   <li>提供通用的序列化/反序列化辅助方法</li>
 *   <li>确保所有实现类具有一致的参数校验行为</li>
 * </ul>
 * </p>
 *
 * @author 刘镇 (suninformation@163.com) on 2026/01/06 04:19:24
 * @since 2.1.4
 */
public abstract class AbstractSerializer implements ISerializer {

    /**
     * 创建字节数组输出流。
     * <p>
     * 该方法创建一个具有默认缓冲区大小的 ByteArrayOutputStream 实例。
     * </p>
     *
     * @return 字节数组输出流实例
     */
    protected ByteArrayOutputStream createOutputStream() {
        return new ByteArrayOutputStream(DEFAULT_BUFFER_SIZE);
    }

    /**
     * 验证对象的类型是否符合预期。
     * <p>
     * 该方法检查对象是否为指定类型的实例，如果不是则抛出异常。
     * </p>
     *
     * @param obj          待验证的对象，可为null
     * @param expectedType 预期的类型
     * @throws SerializationException 当对象不为null且类型不匹配时抛出
     */
    public void validateType(Object obj, Class<?> expectedType) {
        if (obj != null && !expectedType.isInstance(obj)) {
            throw new SerializationException(null, SerializationException.OperationType.DESERIALIZE, String.format("Cannot cast %s to %s", obj.getClass().getName(), expectedType.getName()));
        }
    }
}
