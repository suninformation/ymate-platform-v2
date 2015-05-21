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
package net.ymate.platform.core.plugin.impl;

import net.ymate.platform.core.YMP;
import net.ymate.platform.core.beans.BeanMeta;
import net.ymate.platform.core.beans.IBeanFactory;
import net.ymate.platform.core.beans.impl.DefaultBeanFactory;
import net.ymate.platform.core.beans.impl.DefaultBeanLoader;
import net.ymate.platform.core.plugin.*;
import net.ymate.platform.core.plugin.annotation.Plugin;
import net.ymate.platform.core.plugin.annotation.PluginExtend;
import net.ymate.platform.core.plugin.handle.PluginHandler;
import net.ymate.platform.core.util.ClassUtils;
import net.ymate.platform.core.util.RuntimeUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认插件工厂接口实现
 *
 * @author 刘镇 (suninformation@163.com) on 2012-12-2 下午3:22:17
 * @version 1.0
 */
public class DefaultPluginFactory implements IPluginFactory {

    private IBeanFactory __beanFactory;

    private IPluginConfig __config;

    private IPluginEventListener __event;

    private ClassLoader __pluginClassLoader;

    /**
     * PluginID -> PluginMeta
     */
    private Map<String, PluginMeta> __pluginMetaWithId;

    /**
     * PluginClass -> PluginMeta
     */
    private Map<Class<? extends IPlugin>, PluginMeta> __pluginMetaWithClass;

    private YMP __owner;

    private boolean __inited;

    public DefaultPluginFactory() {
        this(null);
    }

    public DefaultPluginFactory(YMP owner) {
        __owner = owner;
        //
        final IPluginFactory _factory = this;
        //
        __pluginMetaWithId = new ConcurrentHashMap<String, PluginMeta>();
        __pluginMetaWithClass = new ConcurrentHashMap<Class<? extends IPlugin>, PluginMeta>();
        //
        __beanFactory = new DefaultBeanFactory() {
            @Override
            protected void __addClass(BeanMeta beanMeta) {
                PluginMeta _meta = (PluginMeta) beanMeta.getBeanObject();
                IPluginContext _context = new DefaultPluginContext(_factory, _meta);
                IPlugin _plugin = ClassUtils.impl(beanMeta.getBeanClass(), IPlugin.class);
                if (_plugin != null) {
                    // 先尝试从PluginExtend注解中获取扩展对象
                    if (beanMeta.getBeanClass().isAnnotationPresent(PluginExtend.class)) try {
                        PluginExtend _extend = beanMeta.getBeanClass().getAnnotation(PluginExtend.class);
                        IPluginExtendParser _parser = _extend.parserClass() != null ? _extend.parserClass().newInstance() : __config.getPluginExtendParser();
                        if (_parser != null) {
                            // TODO 插件XML扩展分析
                            _meta.setExtendObject(_parser.doParser(_context, /*TODO extendPart*/null));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // 若扩展对象为null，则尝试通过IPluginExtend接口方式获取扩展对象
                    if (_meta.getExtendObject() == null && ClassUtils.isInterfaceOf(beanMeta.getBeanClass(), IPluginExtend.class)) {
                        IPluginExtend _extend = (IPluginExtend) _plugin;
                        _meta.setExtendObject(_extend.getExtendObject(_context));
                    }
                    //
                    super.__addClass(BeanMeta.create(_plugin, beanMeta.getBeanClass()));
                    //
                    __pluginMetaWithId.put(_meta.getId(), _meta);
                    __pluginMetaWithClass.put(_meta.getInitClass(), _meta);
                }
            }
        };
        __beanFactory.registerHandler(Plugin.class, new PluginHandler(this));
    }

    public void init(IPluginConfig pluginConfig) throws Exception {
        if (!__inited) {
            this.__config = pluginConfig;
            //
            __event = __config.getPluginEventListener();
            if (__event == null) {
                __event = new DefaultPluginEventListener();
            }
            //
            this.__pluginClassLoader = __buildPluginClassLoader();
            //
            if (__owner != null) {
                __owner.bindBeanFactory(__beanFactory);
            }
            __beanFactory.setLoader(new DefaultBeanLoader() {
                @Override
                public ClassLoader getClassLoader() {
                    return __pluginClassLoader;
                }
            });
            for (String _package : __config.getAutoscanPackages()) {
                __beanFactory.registerPackage(_package);
            }
            __beanFactory.init();
            __inited = true;
            //
            for (Map.Entry<Class<? extends IPlugin>, PluginMeta> _meta : __pluginMetaWithClass.entrySet()) {
                IPlugin _plugin = __beanFactory.getBean(_meta.getKey());
                _plugin.init(new DefaultPluginContext(this, _meta.getValue()));
                __event.onInited(_plugin.getPluginContext(), _plugin);
                //
                if (__config.isAutomatic() && _meta.getValue().isAutomatic()) {
                    _plugin.startup();
                    __event.onStarted(_plugin.getPluginContext(), _plugin);
                }
            }
        }
    }

    private synchronized ClassLoader __buildPluginClassLoader() throws Exception {
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
                        e.printStackTrace();
                    }
                }
                // 扫描所有正式插件目录(即目录名称不以'.'开头的)
                File[] _pluginDirs = __config.getPluginHome().listFiles();
                if (_pluginDirs != null) for (File _pluginDir : _pluginDirs) {
                    if (_pluginDir.isDirectory() && _pluginDir.getName().charAt(0) != '.') {
                        // 设置JAR包路径
                        File _pluginLibDir = new File(_pluginDir, "lib");
                        if (_pluginLibDir.exists() && _pluginLibDir.isDirectory()) {
                            File[] _libFiles = _pluginLibDir.listFiles();
                            if (_libFiles != null) for (File _libFile : _libFiles) {
                                if (_libFile.isFile() && _libFile.getAbsolutePath().endsWith("jar")) {
                                    _libs.add(_libFile.toURI().toURL());
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
                //
                __pluginClassLoader = new URLClassLoader(_libs.toArray(new URL[0]), this.getClass().getClassLoader());
            } else {
                throw new IllegalArgumentException("The pluginHome parameter is invalid");
            }
        }
        return __pluginClassLoader;
    }

    public boolean isInited() {
        return __inited;
    }

    public void destroy() throws Exception {
        if (__inited) {
            __inited = false;
            //
            for (Map.Entry<Class<? extends IPlugin>, PluginMeta> _meta : __pluginMetaWithClass.entrySet()) {
                IPlugin _plugin = __beanFactory.getBean(_meta.getKey());
                _plugin.shutdown();
                __event.onShutdown(_plugin.getPluginContext(), _plugin);
                //
                __event.onDestroy(_plugin.getPluginContext(), _plugin);
                _plugin.destroy();
            }
            //
            __config = null;
            __pluginClassLoader = null;
            //
            __pluginMetaWithId = null;
            __pluginMetaWithClass = null;
            //
            __beanFactory.destroy();
            __beanFactory = null;
        }
    }

    public void addExcludedInterfaceClass(Class<?> interfaceClass) {
        __beanFactory.registerExcludedClass(interfaceClass);
    }

    public IPluginConfig getPluginConfig() {
        return __config;
    }

    private void __pluginStatusChecker(IPlugin plugin) {
        if (plugin.isInited() && !plugin.isStarted()) {
            try {
                plugin.startup();
                __event.onStarted(plugin.getPluginContext(), plugin);
            } catch (Exception e) {
                throw new RuntimeException(RuntimeUtils.unwrapThrow(e));
            }
        }
    }

    public IPlugin getPlugin(String id) {
        IPlugin _plugin = null;
        if (__pluginMetaWithId.containsKey(id)) {
            _plugin = __beanFactory.getBean(__pluginMetaWithId.get(id).getInitClass());
            __pluginStatusChecker(_plugin);
        }
        return _plugin;
    }

    public <T> T getPlugin(Class<T> clazz) {
        T _target = __beanFactory.getBean(clazz);
        __pluginStatusChecker((IPlugin) _target);
        return _target;
    }

    public PluginMeta getPluginMeta(String id) {
        return __pluginMetaWithId.get(id);
    }

    public PluginMeta getPluginMeta(Class<? extends IPlugin> clazz) {
        return __pluginMetaWithClass.get(clazz);
    }
}
