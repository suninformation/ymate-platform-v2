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
package net.ymate.platform.commons.serialize.fst;

import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nustaq.serialization.FSTConfiguration;

/**
 * FST配置工厂类，负责创建和管理FSTConfiguration实例。
 * <p>
 * 该工厂类采用单例模式，通过SPI机制支持自定义配置工厂实现。
 * 默认使用FST的默认配置，支持通过扩展机制自定义配置。
 * </p>
 * <p>
 * 设计目的：
 * <ul>
 *   <li>提供统一的FST配置管理</li>
 *   <li>支持通过SPI机制扩展自定义配置</li>
 *   <li>确保配置实例的线程安全</li>
 *   <li>延迟初始化，提高启动性能</li>
 * </ul>
 * </p>
 * <p>
 * 使用场景：
 * <ul>
 *   <li>为FstSerializer提供配置实例</li>
 *   <li>自定义FST序列化配置</li>
 *   <li>优化FST序列化性能</li>
 * </ul>
 * </p>
 * <p>
 * 注意事项：
 * <ul>
 *   <li>配置实例为单例，全局共享</li>
 *   <li>自定义配置需要实现IFstConfigurationFactory接口</li>
 *   <li>需要添加FST库依赖</li>
 * </ul>
 * </p>
 *
 * @author 刘镇 (suninformation@163.com) on 2022/9/27 13:24
 * @since 2.1.2
 */
public class FstConfigurationFactory implements IFstConfigurationFactory {

    private static final Log LOG = LogFactory.getLog(FstConfigurationFactory.class);

    /**
     * FST配置工厂实例，采用单例模式。
     */
    private static volatile IFstConfigurationFactory instance;

    static {
        try {
            IFstConfigurationFactory factory = ClassUtils.getExtensionLoader(IFstConfigurationFactory.class).getExtension();
            if (factory == null) {
                factory = new FstConfigurationFactory();
            }
            instance = factory;
        } catch (Exception e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Failed to initialize FST configuration factory, using default configuration", RuntimeUtils.unwrapThrow(e));
            }
            instance = new FstConfigurationFactory();
        }
    }

    /**
     * 获取FST配置工厂实例。
     * <p>
     * 该方法采用双重检查锁定机制确保线程安全。
     * 如果实例尚未初始化，则尝试通过SPI机制加载自定义配置工厂，
     * 如果加载失败则使用默认配置工厂。
     * </p>
     *
     * @return FST配置工厂实例
     */
    public static IFstConfigurationFactory getInstance() {
        if (instance == null) {
            synchronized (FstConfigurationFactory.class) {
                if (instance == null) {
                    try {
                        IFstConfigurationFactory factory = ClassUtils.getExtensionLoader(IFstConfigurationFactory.class).getExtension();
                        instance = factory != null ? factory : new FstConfigurationFactory();
                    } catch (Exception e) {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn("Failed to initialize FST configuration factory, using default configuration", RuntimeUtils.unwrapThrow(e));
                        }
                        instance = new FstConfigurationFactory();
                    }
                }
            }
        }
        return instance;
    }

    /**
     * FST配置实例，使用默认配置创建。
     */
    private final FSTConfiguration fstConfiguration = FSTConfiguration.createDefaultConfiguration();

    /**
     * 获取FST配置实例。
     *
     * @return FST配置实例
     */
    @Override
    public FSTConfiguration getFstConfiguration() {
        return fstConfiguration;
    }
}
