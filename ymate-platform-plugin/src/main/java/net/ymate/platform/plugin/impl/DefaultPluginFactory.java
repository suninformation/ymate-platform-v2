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

import net.ymate.platform.core.YMP;
import net.ymate.platform.core.beans.IBeanHandler;
import net.ymate.platform.core.beans.IBeanLoader;
import net.ymate.platform.core.beans.annotation.Bean;
import net.ymate.platform.core.beans.annotation.Interceptor;
import net.ymate.platform.core.beans.impl.DefaultBeanLoader;
import net.ymate.platform.core.util.ClassUtils;
import net.ymate.platform.core.util.RuntimeUtils;
import net.ymate.platform.plugin.*;
import net.ymate.platform.plugin.annotation.Handler;
import net.ymate.platform.plugin.annotation.Plugin;
import net.ymate.platform.plugin.annotation.PluginFactory;
import net.ymate.platform.plugin.handle.BeanHandler;
import net.ymate.platform.plugin.handle.InterceptorBeanHandler;
import net.ymate.platform.plugin.handle.PluginHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * 默认插件工厂接口实现
 *
 * @author 刘镇 (suninformation@163.com) on 2012-12-2 下午3:22:17
 * @version 1.0
 */
public class DefaultPluginFactory implements IPluginFactory, IBeanHandler {

    private static final Log _LOG = LogFactory.getLog(DefaultPluginFactory.class);

    private IPluginBeanFactory __beanFactory;

    private IPluginConfig __config;

    private IPluginEventListener __event;

    private PluginClassLoader __pluginClassLoader;

    private YMP __owner;

    private boolean __includedClassPath;

    private boolean __inited;

    private boolean __startup;

    /**
     * @param owner        所属YMP框架管理器实例
     * @param pluginHome   插件根路径
     * @param autoPackages 自动扫描包路径
     * @return 创建并返回默认插件工厂实例
     * @throws Exception 创建插件工厂时可能产生的异常
     */
    public static IPluginFactory create(YMP owner, String pluginHome, String[] autoPackages) throws Exception {
        IPluginFactory _factory = new DefaultPluginFactory(owner);
        _factory.init(DefaultPluginConfig.load(pluginHome, autoPackages));
        return _factory;
    }

    /**
     * @param owner 所属YMP框架管理器实例
     * @param clazz 指定的插件工厂类型
     * @return 创建并返回由clazz指定类型的插件工厂实例
     * @throws Exception 创建插件工厂时可能产生的异常
     */
    public static IPluginFactory create(YMP owner, Class<? extends IPluginFactory> clazz) throws Exception {
        return create(owner, clazz, null);
    }

    /**
     * @param owner  所属YMP框架管理器实例
     * @param clazz  指定的插件工厂类型
     * @param config 指定的插件工厂初始化配置
     * @return 采用指定的初始化配置创建并返回由clazz指定类型的插件工厂实例
     * @throws Exception 创建插件工厂时可能产生的异常
     */
    public static IPluginFactory create(YMP owner, Class<? extends IPluginFactory> clazz, IPluginConfig config) throws Exception {
        IPluginFactory _factory = ClassUtils.impl(clazz, IPluginFactory.class, new Class<?>[]{YMP.class}, new Object[]{owner}, false);
        if (_factory != null) {
            if (config != null) {
                _factory.init(config);
            } else if (clazz.isAnnotationPresent(PluginFactory.class)) {
                _factory.init(DefaultPluginConfig.load(clazz));
            }
        }
        return _factory;
    }

    /**
     * 构造器
     *
     * @param owner 所属YMP框架管理器实例
     */
    public DefaultPluginFactory(YMP owner) {
        this(owner, false);
    }

    /**
     * 构造器(仅限内部使用)
     *
     * @param owner             所属YMP框架管理器实例
     * @param includedClassPath 是否加载当前CLASSPATH内的所有包含插件配置文件的Jar包
     */
    protected DefaultPluginFactory(YMP owner, boolean includedClassPath) {
        __owner = owner;
        __includedClassPath = includedClassPath;
    }

    @Override
    public void init(IPluginConfig pluginConfig) throws Exception {
        if (!__inited) {
            __config = pluginConfig;
            //
            __beanFactory = new DefaultPluginBeanFactory(this, __includedClassPath);
            __beanFactory.registerHandler(Bean.class, new BeanHandler(this));
            __beanFactory.registerHandler(Interceptor.class, new InterceptorBeanHandler(this));
            __beanFactory.registerHandler(Handler.class, this);
            __beanFactory.registerHandler(Plugin.class, new PluginHandler(this));
            //
            if (__owner != null) {
                __owner.bindBeanFactory(__beanFactory);
            }
            //
            __event = __config.getPluginEventListener();
            if (__event == null) {
                __event = new DefaultPluginEventListener();
            }
            //
            __pluginClassLoader = __buildPluginClassLoader();
            //
            IBeanLoader _beanLoader = new DefaultBeanLoader();
            _beanLoader.setClassLoader(__pluginClassLoader);
            __beanFactory.setLoader(_beanLoader);
            //
            for (String _package : __config.getAutoscanPackages()) {
                __beanFactory.registerPackage(_package);
            }
            __beanFactory.init();
            //
            __inited = true;
        }
    }

    @Override
    public void startup() throws Exception {
        if (!__startup) {
            __beanFactory.initProxy(__owner.getConfig().getProxyFactory());
            __beanFactory.initIoC();
            //
            for (PluginMeta _meta : __beanFactory.getPluginMetas()) {
                IPlugin _plugin = __beanFactory.getBean(_meta.getInitClass());
                _plugin.init(new DefaultPluginContext(this, _meta));
                //
                __event.onInited(_plugin.getPluginContext(), _plugin);
                //
                if (__config.isAutomatic() && _meta.isAutomatic()) {
                    _plugin.startup();
                    //
                    __event.onStarted(_plugin.getPluginContext(), _plugin);
                }
            }
            //
            __startup = true;
        }
    }

    private synchronized PluginClassLoader __buildPluginClassLoader() throws Exception {
        if (__pluginClassLoader == null) {
            if (__config.getPluginHome() != null && __config.getPluginHome().exists() && __config.getPluginHome().isDirectory()) {
                List<URL> _libs = new ArrayList<URL>();
                // 扫描并分析插件通用类路径
                File _commonFile = new File(__config.getPluginHome(), ".plugin");
                if (_commonFile.exists() && _commonFile.isDirectory()) {
                    try {
                        // 设置通用JAR包路径
                        File _tempFile = new File(_commonFile, "lib");
                        if (_tempFile.exists() && _tempFile.isDirectory()) {
                            File[] _libFiles = _tempFile.listFiles();
                            for (File _libFile : _libFiles != null ? _libFiles : new File[0]) {
                                if (_libFile.getPath().endsWith("jar")) {
                                    _libs.add(_libFile.toURI().toURL());
                                }
                            }
                        }
                        // 设置通用类文件路径
                        _tempFile = new File(_commonFile, "classes");
                        if (_tempFile.exists() && _tempFile.isDirectory()) {
                            _libs.add(_tempFile.toURI().toURL());
                        }
                    } catch (MalformedURLException e) {
                        _LOG.warn("", e);
                    }
                }
                // 扫描所有正式插件目录(即目录名称不以'.'开头的)
                File[] _pluginDirs = __config.getPluginHome().listFiles();
                if (_pluginDirs != null) {
                    for (File _pluginDir : _pluginDirs) {
                        if (_pluginDir.isDirectory() && _pluginDir.getName().charAt(0) != '.') {
                            // 设置JAR包路径
                            File _pluginLibDir = new File(_pluginDir, "lib");
                            if (_pluginLibDir.exists() && _pluginLibDir.isDirectory()) {
                                File[] _libFiles = _pluginLibDir.listFiles();
                                if (_libFiles != null) {
                                    for (File _libFile : _libFiles) {
                                        if (_libFile.isFile() && _libFile.getAbsolutePath().endsWith("jar")) {
                                            _libs.add(_libFile.toURI().toURL());
                                        }
                                    }
                                }
                            }
                            // 设置类文件路径
                            _pluginLibDir = new File(_pluginDir, "classes");
                            if (_pluginLibDir.exists() && _pluginLibDir.isDirectory()) {
                                _libs.add(_pluginLibDir.toURI().toURL());
                            }
                        }
                    }
                }
                //
                __pluginClassLoader = new PluginClassLoader(__config.getPluginHome().getPath(), _libs.toArray(new URL[0]), this.getClass().getClassLoader());
            } else {
                throw new IllegalArgumentException("The pluginHome parameter is invalid");
            }
        }
        return __pluginClassLoader;
    }

    @Override
    public boolean isInited() {
        return __inited;
    }

    @Override
    public void destroy() throws Exception {
        if (__inited) {
            __inited = false;
            __startup = false;
            //
            for (PluginMeta _meta : __beanFactory.getPluginMetas()) {
                IPlugin _plugin = __beanFactory.getBean(_meta.getInitClass());
                if (_plugin != null) {
                    _plugin.shutdown();
                    __event.onShutdown(_plugin.getPluginContext(), _plugin);
                    __event.onDestroy(_plugin.getPluginContext(), _plugin);
                    _plugin.destroy();
                }
            }
            //
            __beanFactory.destroy();
            __beanFactory = null;
            //
            __config = null;
            __pluginClassLoader = null;
        }
    }

    @Override
    public void addExcludedInterfaceClass(Class<?> interfaceClass) {
        __beanFactory.registerExcludedClass(interfaceClass);
    }

    @Override
    public YMP getOwner() {
        return __owner;
    }

    @Override
    public IPluginConfig getPluginConfig() {
        return __config;
    }

    @Override
    public IPluginBeanFactory getBeanFactory() {
        return __beanFactory;
    }

    private void __checkPluginStatus(IPlugin plugin) {
        if (plugin != null && plugin.isInited() && !plugin.isStarted()) {
            try {
                plugin.startup();
                __event.onStarted(plugin.getPluginContext(), plugin);
            } catch (Exception e) {
                _LOG.warn("A exception occurred while starting plugin [" + plugin.getPluginContext().getPluginMeta().getName() + "]: ", RuntimeUtils.unwrapThrow(e));
            }
        }
    }

    @Override
    public IPlugin getPlugin(String id) {
        IPlugin _plugin = __beanFactory.getPlugin(id);
        __checkPluginStatus(_plugin);
        return _plugin;
    }

    @Override
    public <T> T getPlugin(Class<T> clazz) {
        T _target = __beanFactory.getBean(clazz);
        __checkPluginStatus((IPlugin) _target);
        return _target;
    }

    @Override
    public Object handle(Class<?> targetClass) throws Exception {
        if (!(targetClass.isInterface() || targetClass.isAnnotation() || targetClass.isEnum()) && (targetClass.isAnnotationPresent(Handler.class) && ClassUtils.isInterfaceOf(targetClass, IBeanHandler.class))) {
            IBeanHandler _handler;
            try {
                _handler = (IBeanHandler) targetClass.getConstructor(YMP.class).newInstance(__owner);
            } catch (NoSuchMethodException e) {
                try {
                    _handler = (IBeanHandler) targetClass.getConstructor(IPluginFactory.class).newInstance(this);
                } catch (NoSuchMethodException ex) {
                    _handler = (IBeanHandler) targetClass.newInstance();
                }
            }
            __beanFactory.registerHandler(targetClass.getAnnotation(Handler.class).value(), _handler);
        }
        return null;
    }
}
