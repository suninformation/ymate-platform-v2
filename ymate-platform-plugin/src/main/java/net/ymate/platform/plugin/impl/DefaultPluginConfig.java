/*
 * Copyright 2007-2019 the original author or authors.
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
package net.ymate.platform.plugin.impl;

import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.plugin.IPluginBeanLoaderFactory;
import net.ymate.platform.plugin.IPluginConfig;
import net.ymate.platform.plugin.IPluginEventListener;
import net.ymate.platform.plugin.IPluginFactory;
import net.ymate.platform.plugin.annotation.PluginFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 默认插件初始化配置接口实现
 *
 * @author 刘镇 (suninformation@163.com) on 15/3/22 下午5:02
 */
public final class DefaultPluginConfig implements IPluginConfig {

    /**
     * @param pluginHome   插件根路径
     * @param packageNames 自动扫描包路径
     * @return 创建默认插件工厂初始化配置
     */
    public static IPluginConfig load(String pluginHome, String[] packageNames) {
        return builder().pluginHome(new File(pluginHome)).packageNames(Arrays.asList(packageNames)).build();
    }

    /**
     * @param clazz 插件工厂类
     * @return 通过注解分析插件工厂初始化配置
     */
    public static IPluginConfig load(Class<? extends IPluginFactory> clazz) {
        if (clazz != null && clazz.isAnnotationPresent(PluginFactory.class)) {
            PluginFactory factoryAnn = clazz.getAnnotation(PluginFactory.class);
            Builder builder = builder()
                    .pluginHome(new File(factoryAnn.pluginHome()))
                    .packageNames(Arrays.asList(factoryAnn.packageNames()))
                    .automatic(factoryAnn.automatic());
            IPluginEventListener pluginEventListener = ClassUtils.impl(factoryAnn.listenerClass(), IPluginEventListener.class);
            if (pluginEventListener != null) {
                builder.eventListener(pluginEventListener);
            } else {
                builder.eventListener(new DefaultPluginEventListener());
            }
            IPluginBeanLoaderFactory loaderFactory = ClassUtils.impl(factoryAnn.loaderFactoryClass(), IPluginBeanLoaderFactory.class);
            if (loaderFactory != null) {
                builder.beanLoaderFactory(loaderFactory);
            } else {
                builder.beanLoaderFactory(new DefaultPluginBeanLoaderFactory());
            }
            return builder.build();
        }
        return null;
    }

    public static Builder builder() {
        return new Builder();
    }

    private final List<String> packageNames = new ArrayList<>();

    private IPluginEventListener pluginEventListener;

    private IPluginBeanLoaderFactory pluginBeanLoaderFactory;

    private boolean automatic;

    private boolean enabled = true;

    private File pluginHome;

    private DefaultPluginConfig() {
    }

    @Override
    public List<String> getPackageNames() {
        return Collections.unmodifiableList(packageNames);
    }

    public void addPackageNames(List<String> packageNames) {
        packageNames.stream().filter(packageName -> !this.packageNames.contains(packageName)).forEach(this.packageNames::add);
    }

    @Override
    public IPluginEventListener getPluginEventListener() {
        return pluginEventListener;
    }

    public void setPluginEventListener(IPluginEventListener pluginEventListener) {
        this.pluginEventListener = pluginEventListener;
    }

    @Override
    public IPluginBeanLoaderFactory getPluginBeanLoaderFactory() {
        return pluginBeanLoaderFactory;
    }

    public void setPluginBeanLoaderFactory(IPluginBeanLoaderFactory pluginBeanLoaderFactory) {
        this.pluginBeanLoaderFactory = pluginBeanLoaderFactory;
    }

    @Override
    public boolean isAutomatic() {
        return automatic;
    }

    public void setAutomatic(boolean automatic) {
        this.automatic = automatic;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public File getPluginHome() {
        return pluginHome;
    }

    public void setPluginHome(File pluginHome) {
        this.pluginHome = pluginHome;
    }

    public static final class Builder {

        private final DefaultPluginConfig pluginConfig = new DefaultPluginConfig();

        private Builder() {
        }

        public Builder packageNames(List<String> packageNames) {
            pluginConfig.addPackageNames(packageNames);
            return this;
        }

        public Builder eventListener(IPluginEventListener eventListener) {
            pluginConfig.setPluginEventListener(eventListener);
            return this;
        }

        public Builder beanLoaderFactory(IPluginBeanLoaderFactory beanLoaderFactory) {
            pluginConfig.setPluginBeanLoaderFactory(beanLoaderFactory);
            return this;
        }

        public Builder automatic(boolean automatic) {
            pluginConfig.setAutomatic(automatic);
            return this;
        }

        public Builder enabled(boolean enabled) {
            pluginConfig.setEnabled(enabled);
            return this;
        }

        public Builder pluginHome(File pluginHome) {
            pluginConfig.setPluginHome(pluginHome);
            return this;
        }

        public IPluginConfig build() {
            return pluginConfig;
        }
    }
}
