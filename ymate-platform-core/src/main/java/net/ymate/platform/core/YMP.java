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
package net.ymate.platform.core;

import net.ymate.platform.core.beans.IBeanFactory;
import net.ymate.platform.core.beans.IBeanHandler;
import net.ymate.platform.core.beans.annotation.*;
import net.ymate.platform.core.beans.impl.DefaultBeanFactory;
import net.ymate.platform.core.beans.impl.proxy.DefaultProxyFactory;
import net.ymate.platform.core.beans.proxy.IProxy;
import net.ymate.platform.core.beans.proxy.IProxyFactory;
import net.ymate.platform.core.beans.proxy.IProxyFilter;
import net.ymate.platform.core.lang.BlurObject;
import net.ymate.platform.core.module.IModule;
import net.ymate.platform.core.util.RuntimeUtils;
import org.apache.commons.lang.StringUtils;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

/**
 * YMP框架核心管理器
 *
 * @author 刘镇 (suninformation@163.com) on 15-3-4 下午6:47
 * @version 1.0
 */
public class YMP {

    public static final Version VERSION = new Version(2, 0, 0);

    private static final String __YMP_BASE_PACKAGE = "net.ymate.platform";

    private static YMP __instance;

    private IConfig __config;

    private boolean __inited;

    private IBeanFactory __beanFactory;

    private IProxyFactory __proxyFactory;

    private List<IModule> __modules;

    /**
     * @return 返回默认YMP框架核心管理器对象实例，若未实例化或已销毁则重新创建对象实例
     */
    public static YMP get() {
        if (__instance == null || !__instance.isInited()) {
            synchronized (__YMP_BASE_PACKAGE) {
                if (__instance == null || __instance.getBeanFactory() == null) {
                    __instance = new YMP(new Config());
                }
            }
        }
        return __instance;
    }

    /**
     * 构造方法
     *
     * @param config YMP框架初始化配置
     */
    public YMP(IConfig config) {
        __config = config;
        // 创建模块对象引用集合
        __modules = new ArrayList<IModule>();
        // 创建根对象工厂
        __beanFactory = new DefaultBeanFactory();
        // 创建代理工厂并初始化
        __proxyFactory = new DefaultProxyFactory();
    }

    private void __registerScanPackages(IBeanFactory factory) {
        factory.registerPackage(__YMP_BASE_PACKAGE);
        for (String _packageName : __config.getAutoscanPackages()) {
            if (!_packageName.startsWith(__YMP_BASE_PACKAGE)) {
                factory.registerPackage(_packageName);
            }
        }
    }

    private IBeanFactory __doInitBeanFactory() throws Exception {
        // 设置YMP框架基础包为根工厂扫描路径，并根据配置参数注册自动扫描应用包路径
        __registerScanPackages(__beanFactory);
        // 注册YMP框架核心对象处理器
        __beanFactory.registerHandler(Bean.class);
        //
        __doInitBeanHandles();
        //
        return __beanFactory;
    }

    private void __doInitBeanHandles() throws Exception {
        IBeanFactory _handles = new DefaultBeanFactory();
        __registerScanPackages(_handles);
        _handles.registerHandler(Handler.class);
        _handles.init();
        for (Object _handler : _handles.getBeans().values()) {
            ((IBeanHandler) _handler).init(this);
        }
        _handles.destroy();
    }

    private void __doInitProxyFactory() {
        for (Map.Entry<Class<?>, Object> _entry : __beanFactory.getBeans().entrySet()) {
            if (!_entry.getKey().isInterface()) {
                final Class<?> _targetClass = _entry.getKey();
                List<IProxy> _targetProxies = __proxyFactory.getProxies(new IProxyFilter() {

                    private boolean __doCheckAnnotation(Proxy targetProxyAnno) {
                        // 若设置了自定义注解类型，则判断targetClass是否匹配，否则返回true
                        if (targetProxyAnno.annotation() != null && targetProxyAnno.annotation().length > 0) {
                            for (Class<? extends Annotation> _annoClass : targetProxyAnno.annotation()) {
                                if (_targetClass.isAnnotationPresent(_annoClass)) {
                                    return true;
                                }
                            }
                            return false;
                        }
                        return true;
                    }

                    public boolean filter(IProxy targetProxy) {
                        Proxy _targetProxyAnno = targetProxy.getClass().getAnnotation(Proxy.class);
                        // 若已设置作用包路径
                        if (StringUtils.isNotBlank(_targetProxyAnno.packageScope())) {
                            // 若当前类对象所在包路径匹配
                            if (!StringUtils.startsWith(_targetClass.getPackage().getName(), _targetProxyAnno.packageScope())) {
                                return false;
                            }
                        }
                        return __doCheckAnnotation(_targetProxyAnno);
                    }
                });
                if (!_targetProxies.isEmpty()) {
                    __beanFactory.registerBean(_targetClass, __proxyFactory.createProxy(_targetClass, _targetProxies));
                }
            }
        }
    }

    private void __doInitIoC() throws Exception {
        for (Map.Entry<Class<?>, Object> _bean : __beanFactory.getBeans().entrySet()) {
            Field[] _fields = _bean.getKey().getDeclaredFields();
            if (_fields != null && _fields.length > 0) {
                for (Field _field : _fields) {
                    if (_field.isAnnotationPresent(Inject.class)) {
                        Object _injectObj = null;
                        if (_field.isAnnotationPresent(By.class)) {
                            By _injectBy = _field.getAnnotation(By.class);
                            _injectObj = __beanFactory.getBean(_injectBy.value());
                        } else {
                            _injectObj = __beanFactory.getBean(_field.getType());
                        }
                        if (_injectObj != null) {
                            _field.setAccessible(true);
                            _field.set(_bean.getValue(), _injectObj);
                        }
                    }
                }
            }
        }
    }

    /**
     * 初始化YMP框架
     *
     * @throws Exception
     */
    public synchronized void init() throws Exception {
        if (!__inited) {
            // 初始化根对象工厂
            __doInitBeanFactory().init();
            // 初始化所有已加载模块
            for (IModule _module : __modules) {
                _module.init(this);
            }
            // 代理对象封装
            __doInitProxyFactory();
            // IoC依赖注入
            __doInitIoC();
            //
            __inited = true;
        }
    }

    /**
     * 销毁YMP框架
     *
     * @throws Exception
     */
    public void destroy() throws Exception {
        if (__inited) {
            __inited = false;
            // 销毁所有已加载模块
            for (IModule _module : __modules) {
                _module.destroy();
            }
            __modules = null;
            // 销毁代理工厂
            __proxyFactory = null;
            // 销毁根对象工厂
            __beanFactory.destroy();
            __beanFactory = null;
        }
    }

    /**
     * @return 返回当前配置对象
     */
    public IConfig getConfig() {
        return __config;
    }

    /**
     * @return 返回YMP框架是否已初始化
     */
    public boolean isInited() {
        return __inited;
    }

    /**
     * @return 返回根对象工厂实例
     */
    public IBeanFactory getBeanFactory() {
        return __beanFactory;
    }

    /**
     * @return 返回代理工厂实例
     */
    public IProxyFactory getProxyFactory() {
        return __proxyFactory;
    }

    /**
     * 注册模块实例(仅在YMP框架被初始化前调用有效)
     *
     * @param module
     */
    public void registerModule(IModule module) {
        if (!__inited) {
            __modules.add(module);
        }
    }

    /**
     * YMP框架配置类
     *
     * @author 刘镇 (suninformation@163.com) on 15-3-9 下午2:50
     * @version 1.0
     */
    private static class Config implements IConfig {

        private Properties __props;

        private Boolean __isDevelopMode;

        private List<String> __packageNames;

        private Map<String, Map<String, String>> __moduleCfgs;

        public Config() {
            __props = new Properties();
            __moduleCfgs = new HashMap<String, Map<String, String>>();
            //
            InputStream _in = null;
            try {
                if (RuntimeUtils.isWindows()) {
                    _in = Config.class.getClassLoader().getResourceAsStream("ymp-conf_WIN.properties");
                } else if (RuntimeUtils.isUnixOrLinux()) {
                    _in = Config.class.getClassLoader().getResourceAsStream("ymp-conf_UNIX.properties");
                }
                if (_in == null) {
                    _in = Config.class.getClassLoader().getResourceAsStream("ymp-conf.properties");
                }
                if (_in != null) {
                    __props.load(_in);
                }
            } catch (Exception e) {
                throw new RuntimeException(RuntimeUtils.unwrapThrow(e));
            } finally {
                try {
                    _in.close();
                } catch (Exception e) {
                }
            }
        }

        public boolean isDevelopMode() {
            if (__isDevelopMode == null) {
                __isDevelopMode = new BlurObject(__props.getProperty("ymp.dev_mode")).toBooleanValue();
            }
            return __isDevelopMode;
        }

        public List<String> getAutoscanPackages() {
            if (__packageNames == null) {
                String[] _packageNameArr = StringUtils.split(__props.getProperty("ymp.autoscan_packages"), "|");
                if (_packageNameArr != null) {
                    __packageNames = new ArrayList<String>(Arrays.asList(_packageNameArr));
                } else {
                    __packageNames = Collections.emptyList();
                }
            }
            return __packageNames;
        }

        public Map<String, String> getModuleConfigs(String moduleName) {
            Map<String, String> _cfgsMap = __moduleCfgs.get(moduleName);
            if (_cfgsMap == null) {
                _cfgsMap = new HashMap<String, String>();
                // 提取模块配置
                for (Object _key : __props.keySet()) {
                    String _prefix = "ymp.configs." + moduleName + ".";
                    if (StringUtils.startsWith((String) _key, _prefix)) {
                        String _cfgKey = StringUtils.substring((String) _key, _prefix.length());
                        String _cfgValue = __props.getProperty((String) _key);
                        _cfgsMap.put(_cfgKey, _cfgValue);
                    }
                }
                __moduleCfgs.put(moduleName, _cfgsMap);
            }
            return _cfgsMap;
        }
    }
}
