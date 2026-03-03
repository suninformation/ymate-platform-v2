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
package net.ymate.platform.commons.serialize.kryo;

import com.esotericsoftware.kryo.Kryo;

/**
 * Kryo工厂接口，定义了获取Kryo实例的标准方法。
 * <p>
 * 该接口允许通过SPI机制自定义Kryo配置，以满足不同的序列化需求。
 * 实现该接口的类可以通过SPI机制被自动发现和加载。
 * </p>
 * <p>
 * 设计目的：
 * <ul>
 *   <li>提供Kryo配置的统一接口</li>
 *   <li>支持通过SPI机制扩展自定义配置</li>
 *   <li>便于配置的统一管理和替换</li>
 * </ul>
 * </p>
 * <p>
 * 使用场景：
 * <ul>
 *   <li>自定义Kryo序列化配置</li>
 *   <li>优化Kryo序列化性能</li>
 *   <li>配置Kryo的特定功能</li>
 * </ul>
 * </p>
 * <p>
 * 注意事项：
 * <ul>
 *   <li>实现类需要通过SPI机制注册</li>
 *   <li>Kryo实例不是线程安全的，每次使用都应创建新实例</li>
 *   <li>建议使用工厂模式管理Kryo实例的创建</li>
 * </ul>
 * </p>
 *
 * @author 刘镇 (suninformation@163.com) on 2026-01-12 03:45:36
 * @since 2.1.4
 */
public interface IKryoFactory {

    /**
     * 创建并返回一个新的Kryo实例。
     * <p>
     * 该方法返回一个配置好的Kryo实例，用于序列化和反序列化操作。
     * 由于Kryo实例不是线程安全的，每次调用该方法都会创建一个新实例。
     * </p>
     *
     * @return 新的Kryo实例
     */
    Kryo createKryo();
}