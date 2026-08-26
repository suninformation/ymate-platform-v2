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
package net.ymate.platform.core.module;

import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.IApplicationConfigureFactory;
import net.ymate.platform.core.IApplicationConfigurer;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.module.impl.DefaultModuleConfigurer;
import net.ymate.platform.core.support.IDestroyable;
import net.ymate.platform.core.support.IInitialization;

/**
 * 模块抽象基类，封装模块初始化、配置加载、生命周期管理的公共逻辑
 *
 * @param <C> 模块配置类型，若模块无配置则使用 {@link Void}
 * @author 刘镇 (suninformation@163.com) on 2026-08-24
 * @since 2.1.4
 */
public abstract class AbstractModule<C> implements IModule {

    private IApplication owner;

    private C config;

    private boolean initialized;

    /**
     * @return 模块版本标识，用于 {@link YMP#showModuleVersion(String, IModule)}
     */
    protected abstract String doGetModuleVersion();

    /**
     * 从模块配置器创建模块配置实例
     *
     * @param mainClass        主类
     * @param moduleConfigurer 模块配置器
     * @return 配置实例，若模块无配置则返回 {@code null}
     * @throws Exception 可能抛出的异常
     */
    protected abstract C doCreateModuleConfig(Class<?> mainClass, IModuleConfigurer moduleConfigurer) throws Exception;

    /**
     * @return 创建默认配置实例，若模块无配置则返回 {@code null}
     */
    protected abstract C doCreateDefaultConfig();

    /**
     * 模块初始化钩子（config 已加载完毕，子类在此执行注册事件、注册 Handler、Proxy 等逻辑）
     *
     * @param owner 应用所有者
     * @throws Exception 可能抛出的异常
     */
    protected abstract void onInit(IApplication owner) throws Exception;

    /**
     * 模块关闭钩子，子类在此执行特定的清理逻辑
     *
     * @throws Exception 可能抛出的异常
     */
    protected abstract void onClose() throws Exception;

    /**
     * 模块初始化前钩子（在 showVersion 之前调用，用于 Plugins 等需要提前准备状态的模块）
     *
     * @param owner 应用所有者
     * @throws Exception 可能抛出的异常
     */
    protected void onBeforeInit(IApplication owner) throws Exception {
    }

    /**
     * 显示模块版本信息，默认使用当前模块实例，子类可按需重写（如扩展模块可能传入其他对象）
     */
    protected void doShowVersion() {
        YMP.showModuleVersion(doGetModuleVersion(), this);
    }

    /**
     * 加载模块配置（子类可重写以提供自定义加载逻辑，如 Plugins）
     */
    protected void doLoadConfig(IApplication owner) throws Exception {
        if (config != null) {
            return;
        }
        IApplicationConfigureFactory configureFactory = owner.getConfigureFactory();
        if (configureFactory != null) {
            IApplicationConfigurer configurer = configureFactory.getConfigurer();
            IModuleConfigurer moduleConfigurer = configurer == null ? null : configurer.getModuleConfigurer(getName());
            if (moduleConfigurer != null) {
                config = doCreateModuleConfig(configureFactory.getMainClass(), moduleConfigurer);
            } else {
                config = doCreateModuleConfig(configureFactory.getMainClass(), DefaultModuleConfigurer.createEmpty(getName()));
            }
        }
        if (config == null) {
            config = doCreateDefaultConfig();
        }
        if (config != null) {
            doInitConfig();
        }
    }

    /**
     * 初始化配置实例（子类可重写以处理非标准配置初始化）
     *
     * @throws Exception 可能抛出的异常
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void doInitConfig() throws Exception {
        if (config instanceof IInitialization) {
            IInitialization initialization = (IInitialization) config;
            if (!initialization.isInitialized()) {
                initialization.initialize(this);
            }
        } else {
            try {
                java.lang.reflect.Method isInitMethod = config.getClass().getMethod("isInitialized");
                if (!(boolean) isInitMethod.invoke(config)) {
                    java.lang.reflect.Method initMethod = config.getClass().getMethod("initialize", this.getClass().getInterfaces()[0]);
                    initMethod.invoke(config, this);
                }
            } catch (NoSuchMethodException ignored) {
            }
        }
    }

    /**
     * 销毁配置实例
     *
     * @throws Exception 可能抛出的异常
     */
    protected void doDestroyConfig() throws Exception {
        if (config != null) {
            if (config instanceof IDestroyable) {
                ((IDestroyable) config).close();
            } else {
                try {
                    java.lang.reflect.Method closeMethod = config.getClass().getMethod("close");
                    closeMethod.invoke(config);
                } catch (NoSuchMethodException ignored) {
                }
            }
            config = null;
        }
    }

    @Override
    public void initialize(IApplication owner) throws Exception {
        if (!initialized) {
            this.owner = owner;
            //
            onBeforeInit(owner);
            //
            doShowVersion();
            //
            doLoadConfig(owner);
            //
            onInit(owner);
            //
            initialized = true;
        }
    }

    @Override
    public void close() throws Exception {
        if (initialized) {
            initialized = false;
            //
            onClose();
            //
            doDestroyConfig();
            //
            owner = null;
        }
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    public IApplication getOwner() {
        return owner;
    }

    public C getConfig() {
        return config;
    }

    /**
     * 设置模块配置实例（用于子类构造器中预置配置，替代默认加载流程）
     *
     * @param config 配置实例
     */
    protected void doSetConfig(C config) {
        this.config = config;
    }
}