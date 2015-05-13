/*
 * Copyright 2007-2107 the original author or authors.
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
package net.ymate.platform.core.plugin;

import net.ymate.platform.core.plugin.annotation.PluginExtend;
import net.ymate.platform.core.plugin.annotation.PluginFactory;
import net.ymate.platform.core.plugin.impl.DefaultPluginConfig;
import net.ymate.platform.core.plugin.impl.DefaultPluginEventListener;
import net.ymate.platform.core.plugin.impl.DefaultPluginFactory;
import net.ymate.platform.core.plugin.impl.DefaultPluginParser;
import net.ymate.platform.core.util.ClassUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.Arrays;

/**
 * 插件工厂相关工具
 *
 * @author 刘镇 (suninformation@163.com) on 2012-11-30 下午6:28:20
 * @version 1.0
 */
public class Plugins {

    /**
     * @param pluginHome   插件根路径
     * @param autoPackages 自动扫描包路径
     * @return 创建默认插件工厂初始化配置
     * @throws Exception
     */
    public static DefaultPluginConfig loadConfig(String pluginHome, String[] autoPackages) throws Exception {
        DefaultPluginConfig _config = new DefaultPluginConfig();
        _config.setPluginHome(new File(pluginHome));
        _config.setAutoscanPackages(Arrays.asList(autoPackages));
        return _config;
    }

    /**
     * @param clazz
     * @return 通过注解分析插件工厂初始化配置
     * @throws Exception
     */
    public static DefaultPluginConfig loadConfig(Class<? extends IPluginFactory> clazz) throws Exception {
        DefaultPluginConfig _config = null;
        if (clazz != null && clazz.isAnnotationPresent(PluginFactory.class)) {
            _config = new DefaultPluginConfig();
            //
            PluginFactory _factoryAnno = clazz.getAnnotation(PluginFactory.class);
            if (StringUtils.isNotBlank(_factoryAnno.pluginHome())) {
                _config.setPluginHome(new File(_factoryAnno.pluginHome()));
            }
            String[] _packages = _factoryAnno.autoscanPackages();
            if (ArrayUtils.isEmpty(_packages)) {
                _packages = new String[]{clazz.getPackage().getName()};
            }
            _config.setAutoscanPackages(Arrays.asList(_packages));
            _config.setIncludedClassPath(_factoryAnno.includedClassPath());
            _config.setManifestFile(StringUtils.defaultIfBlank(_factoryAnno.manifestFile(), IPluginConfig.DEFAULT_MANIFEST_FILE));
            _config.setAutomatic(_factoryAnno.automatic());
            //
            IPluginParser _parser = ClassUtils.impl(_factoryAnno.parserClass(), IPluginParser.class);
            if (_parser != null) {
                _config.setPluginParser(_parser);
            } else {
                _config.setPluginParser(new DefaultPluginParser());
            }
            //
            IPluginEventListener _listener = ClassUtils.impl(_factoryAnno.listenerClass(), IPluginEventListener.class);
            if (_listener != null) {
                _config.setPluginEventListener(_listener);
            } else {
                _config.setPluginEventListener(new DefaultPluginEventListener());
            }
            //
            if (clazz.isAnnotationPresent(PluginExtend.class)) {
                PluginExtend _extend = clazz.getAnnotation(PluginExtend.class);
                IPluginExtendParser _extendParser = ClassUtils.impl(_extend.parserClass(), IPluginExtendParser.class);
                if (_extendParser != null) {
                    _config.setPluginExtendParser(_extendParser);
                }
            }
        }
        return _config;
    }

    /**
     * @param pluginHome   插件根路径
     * @param autoPackages 自动扫描包路径
     * @return 创建并返回默认插件工厂实例
     * @throws Exception
     */
    public static IPluginFactory createFactory(String pluginHome, String[] autoPackages) throws Exception {
        IPluginFactory _factory = new DefaultPluginFactory();
        _factory.init(loadConfig(pluginHome, autoPackages));
        return _factory;
    }

    /**
     * @param config
     * @return 采用指定的初始化配置创建并返回默认插件工厂实例
     * @throws Exception
     */
    public static IPluginFactory createFactory(IPluginConfig config) throws Exception {
        IPluginFactory _factory = null;
        if (config != null) {
            _factory = new DefaultPluginFactory();
            _factory.init(config);
        }
        return _factory;
    }

    /**
     * @param clazz
     * @return 创建并返回由clazz指定类型的插件工厂实例
     * @throws Exception
     */
    public static IPluginFactory createFactory(Class<? extends IPluginFactory> clazz) throws Exception {
        IPluginFactory _factory = ClassUtils.impl(clazz, IPluginFactory.class);
        if (_factory != null) {
            if (clazz.isAnnotationPresent(PluginFactory.class)) {
                _factory.init(loadConfig(clazz));
            }
        }
        return _factory;
    }

    /**
     * @param clazz
     * @param config
     * @return 采用指定的初始化配置创建并返回由clazz指定类型的插件工厂实例
     * @throws Exception
     */
    public static IPluginFactory createFactory(Class<? extends IPluginFactory> clazz, IPluginConfig config) throws Exception {
        IPluginFactory _factory = ClassUtils.impl(clazz, IPluginFactory.class);
        if (_factory != null) {
            if (config != null) {
                _factory.init(config);
            } else if (clazz.isAnnotationPresent(PluginFactory.class)) {
                _factory.init(loadConfig(clazz));
            }
        }
        return _factory;
    }
}
