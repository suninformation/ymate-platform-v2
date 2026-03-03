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
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.objenesis.strategy.StdInstantiatorStrategy;

/**
 * Kryo工厂类，负责创建和管理Kryo实例。
 * <p>
 * 该工厂类采用单例模式，通过SPI机制支持自定义配置工厂实现。
 * 默认使用Kryo的默认配置，支持通过扩展机制自定义配置。
 * </p>
 * <p>
 * 设计目的：
 * <ul>
 *   <li>提供统一的Kryo配置管理</li>
 *   <li>支持通过SPI机制扩展自定义配置</li>
 *   <li>确保Kryo实例的正确创建和配置</li>
 *   <li>延迟初始化，提高启动性能</li>
 * </ul>
 * </p>
 * <p>
 * 使用场景：
 * <ul>
 *   <li>为KryoSerializer提供Kryo实例</li>
 *   <li>自定义Kryo序列化配置</li>
 *   <li>优化Kryo序列化性能</li>
 * </ul>
 * </p>
 * <p>
 * 注意事项：
 * <ul>
 *   <li>工厂实例为单例，全局共享</li>
 *   <li>Kryo实例不是线程安全的，每次调用createKryo()都会创建新实例</li>
 *   <li>自定义配置需要实现IKryoFactory接口</li>
 *   <li>需要添加Kryo库依赖</li>
 * </ul>
 * </p>
 *
 * @author 刘镇 (suninformation@163.com) on 2026-01-12 03:45:36
 * @since 2.1.4
 */
public class KryoFactory implements IKryoFactory {

    private static final Log LOG = LogFactory.getLog(KryoFactory.class);

    /**
     * Kryo工厂实例，采用单例模式。
     */
    private static volatile IKryoFactory instance;

    static {
        try {
            IKryoFactory factory = ClassUtils.getExtensionLoader(IKryoFactory.class).getExtension();
            if (factory == null) {
                factory = new KryoFactory();
            }
            instance = factory;
        } catch (Exception e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Failed to initialize Kryo factory, using default configuration", RuntimeUtils.unwrapThrow(e));
            }
            instance = new KryoFactory();
        }
    }

    /**
     * 获取Kryo工厂实例。
     * <p>
     * 该方法采用双重检查锁定机制确保线程安全。
     * 如果实例尚未初始化，则尝试通过SPI机制加载自定义工厂，
     * 如果加载失败则使用默认工厂。
     * </p>
     *
     * @return Kryo工厂实例
     */
    public static IKryoFactory getInstance() {
        if (instance == null) {
            synchronized (KryoFactory.class) {
                if (instance == null) {
                    try {
                        IKryoFactory factory = ClassUtils.getExtensionLoader(IKryoFactory.class).getExtension();
                        instance = factory != null ? factory : new KryoFactory();
                    } catch (Exception e) {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn("Failed to initialize Kryo factory, using default configuration", RuntimeUtils.unwrapThrow(e));
                        }
                        instance = new KryoFactory();
                    }
                }
            }
        }
        return instance;
    }

    /**
     * 创建并返回一个新的Kryo实例。
     * <p>
     * 该方法创建一个配置好的Kryo实例，默认配置包括：
     * <ul>
     *   <li>禁用注册要求（setRegistrationRequired(false)）</li>
     *   <li>使用默认实例化策略，避免ReflectASM导致的模块访问问题</li>
     * </ul>
     * </p>
     *
     * @return 新的Kryo实例
     */
    @Override
    public Kryo createKryo() {
        Kryo kryo = new Kryo();
        // 启用自动注册功能，允许序列化未显式注册的类
        kryo.setRegistrationRequired(false);
        // 使用默认的实例化策略，避免 ReflectASM 导致的模块访问问题
        kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(
                new StdInstantiatorStrategy()));
        return kryo;
    }
}