/*
 * Copyright 2007-2017 the original author or authors.
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

import net.ymate.platform.core.util.ClassUtils;
import net.ymate.platform.plugin.IPluginConfig;
import net.ymate.platform.plugin.IPluginEventListener;
import net.ymate.platform.plugin.IPluginFactory;
import net.ymate.platform.plugin.annotation.PluginFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 默认插件初始化配置接口实现
 *
 * @author 刘镇 (suninformation@163.com) on 15/3/22 下午5:02
 * @version 1.0
 */
public class DefaultPluginConfig implements IPluginConfig {

    public static Builder create() {
        return new Builder();
    }

    /**
     * @param pluginHome   插件根路径
     * @param autoPackages 自动扫描包路径
     * @return 创建默认插件工厂初始化配置
     * @throws Exception 加载配置可能产生的异常
     */
    public static IPluginConfig load(String pluginHome, String[] autoPackages) throws Exception {
        return create().pluginHome(new File(pluginHome))
                .autoscanPackages(Arrays.asList(autoPackages)).build();
    }

    /**
     * @param clazz 插件工厂类
     * @return 通过注解分析插件工厂初始化配置
     * @throws Exception 加载配置可能产生的异常
     */
    public static IPluginConfig load(Class<? extends IPluginFactory> clazz) throws Exception {
        if (clazz != null && clazz.isAnnotationPresent(PluginFactory.class)) {
            PluginFactory _factoryAnno = clazz.getAnnotation(PluginFactory.class);
            //
            Builder _builder = create().pluginHome(new File(_factoryAnno.pluginHome()))
                    .autoscanPackages(Arrays.asList(_factoryAnno.autoscanPackages()))
                    .automatic(_factoryAnno.automatic());
            IPluginEventListener _listener = ClassUtils.impl(_factoryAnno.listenerClass(), IPluginEventListener.class);
            if (_listener != null) {
                _builder.eventListener(_listener);
            } else {
                _builder.eventListener(new DefaultPluginEventListener());
            }
            return _builder.build();
        }
        return null;
    }

    private List<String> __packageNames = new ArrayList<String>();

    private IPluginEventListener __pluginEventListener;

    private boolean __automatic;

    private boolean __includedClassPath;

    private File __pluginHome;

    public DefaultPluginConfig() {
    }

    @Override
    public List<String> getAutoscanPackages() {
        return __packageNames;
    }

    public void addAutoscanPackages(List<String> autoscanPackages) {
        for (String _package : autoscanPackages) {
            if (!__packageNames.contains(_package)) {
                __packageNames.add(_package);
            }
        }
    }

    @Override
    public IPluginEventListener getPluginEventListener() {
        return __pluginEventListener;
    }

    public void setPluginEventListener(IPluginEventListener pluginEventListener) {
        this.__pluginEventListener = pluginEventListener;
    }

    @Override
    public boolean isAutomatic() {
        return __automatic;
    }

    public void setAutomatic(boolean automatic) {
        this.__automatic = automatic;
    }

    @Override
    public File getPluginHome() {
        return __pluginHome;
    }

    public void setPluginHome(File pluginHome) {
        this.__pluginHome = pluginHome;
    }

    public static class Builder {

        private DefaultPluginConfig __config = new DefaultPluginConfig();

        public Builder autoscanPackages(List<String> autoscanPackages) {
            __config.addAutoscanPackages(autoscanPackages);
            return this;
        }

        public Builder eventListener(IPluginEventListener eventListener) {
            __config.setPluginEventListener(eventListener);
            return this;
        }

        public Builder automatic(boolean automatic) {
            __config.setAutomatic(automatic);
            return this;
        }

        public Builder pluginHome(File pluginHome) {
            __config.setPluginHome(pluginHome);
            return this;
        }

        public IPluginConfig build() {
            return __config;
        }
    }
}
