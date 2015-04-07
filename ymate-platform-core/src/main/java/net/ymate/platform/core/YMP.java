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
import net.ymate.platform.core.beans.annotation.Bean;
import net.ymate.platform.core.beans.annotation.Proxy;
import net.ymate.platform.core.beans.impl.DefaultBeanFactory;
import net.ymate.platform.core.beans.impl.proxy.DefaultProxyFactory;
import net.ymate.platform.core.beans.proxy.IProxy;
import net.ymate.platform.core.beans.proxy.IProxyFactory;
import net.ymate.platform.core.handle.ModuleHandler;
import net.ymate.platform.core.handle.ProxyHandler;
import net.ymate.platform.core.lang.BlurObject;
import net.ymate.platform.core.module.IModule;
import net.ymate.platform.core.module.annotation.Module;
import net.ymate.platform.core.util.RuntimeUtils;
import org.apache.commons.lang.StringUtils;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * YMP框架核心管理器
 *
 * @author 刘镇 (suninformation@163.com) on 15-3-4 下午6:47
 * @version 1.0
 */
public class YMP {

    public static final Version VERSION = new Version(2, 0, 0, Version.VersionType.Alphal);

    private static final String __YMP_BASE_PACKAGE = "net.ymate.platform";

    private static YMP __instance;

    private IConfig __config;

    private boolean __inited;

    private IBeanFactory __moduleFactory;

    private IBeanFactory __beanFactory;

    private IProxyFactory __proxyFactory;

    private Map<Class<? extends IModule>, IModule> __modules;

    /**
     * @return 返回默认YMP框架核心管理器对象实例，若未实例化或已销毁则重新创建对象实例
     */
    public static YMP get() {
        if (__instance == null || !__instance.isInited()) {
            synchronized (__YMP_BASE_PACKAGE) {
                if (__instance == null) {
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
        // 创建根对象工厂
        __beanFactory = new DefaultBeanFactory();
        __beanFactory.registerHandler(Bean.class);
        // 创建模块对象引用集合
        __modules = new HashMap<Class<? extends IModule>, IModule>();
        // 创建模块对象工厂
        __moduleFactory = new BeanFactory(this);
        __moduleFactory.registerHandler(Module.class, new ModuleHandler(this));
        __moduleFactory.registerHandler(Proxy.class, new ProxyHandler(this));
        // 设置自动扫描应用包路径
        __registerScanPackages(__moduleFactory);
        __registerScanPackages(__beanFactory);
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

    /**
     * 初始化YMP框架
     *
     * @return 返回当前YMP核心框架管理器对象
     * @throws Exception
     */
    public synchronized YMP init() throws Exception {
        if (!__inited) {
            // 初始化根对象工厂
            __moduleFactory.init();
            if (this.getConfig().isModuleAutoload()) {
                for (IModule _module : __modules.values()) {
                    if (!_module.isInited()) {
                        _module.init(this);
                    }
                }
            }
            // 初始化对象工厂
            __beanFactory.init();
            // 代理对象封装
            __beanFactory.bindProxy(__proxyFactory);
            // IoC依赖注入
            __beanFactory.initBeanIoC();
            //
            __inited = true;
        }
        return this;
    }

    /**
     * 初始化所有已加载的模块(调用此方法前须保证YMP已经初始化)
     *
     * @throws Exception
     */
    public synchronized void initModules() throws Exception {
        if (!__inited) {
            // 初始化所有已加载模块
            for (IModule _module : __modules.values()) {
                if (!_module.isInited()) {
                    _module.init(this);
                }
            }
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
            for (IModule _module : __modules.values()) {
                _module.destroy();
            }
            __modules = null;
            // 销毁代理工厂
            __proxyFactory = null;
            // 销毁根对象工厂
            __moduleFactory.destroy();
            __moduleFactory = null;
            //
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
     * 注册自定义注解类处理器，重复注册将覆盖前者
     *
     * @param annoClass
     * @param handler
     */
    public void registerHandler(Class<? extends Annotation> annoClass, IBeanHandler handler) {
        __beanFactory.registerHandler(annoClass, handler);
    }

    public void registerHandler(Class<? extends Annotation> annoClass) {
        __beanFactory.registerHandler(annoClass);
    }

    /**
     * 注册排除的接口类
     *
     * @param excludedClass
     */
    public void registerExcludedClass(Class<?> excludedClass) {
        __beanFactory.registerExcludedClass(excludedClass);
    }

    /**
     * 注册自定义类型
     *
     * @param clazz
     * @throws Exception
     */
    public void registerBean(Class<?> clazz) throws Exception {
        __beanFactory.registerBean(clazz);
    }

    public void registerBean(Class<?> clazz, Object object) {
        __beanFactory.registerBean(clazz, object);
    }

    /**
     * @return 提取类型为clazz的对象实例
     */
    public <T> T getBean(Class<T> clazz) {
        return __beanFactory.getBean(clazz);
    }

    /**
     * 向工厂注册代理类对象
     *
     * @param proxy
     */
    public void registerProxy(IProxy proxy) {
        __proxyFactory.registerProxy(proxy);
    }

    public void registerProxy(Collection<? extends IProxy> proxies) {
        __proxyFactory.registerProxy(proxies);
    }

    /**
     * 注册模块实例(此方法仅在YMP框架核心管理器未初始化前有效)
     *
     * @param module
     */
    public void registerModule(IModule module) {
        if (!__inited) {
            if (module != null) {
                __moduleFactory.registerBean(module.getClass(), module);
                __modules.put(module.getClass(), module);
            }
        }
    }

    /**
     * @param moduleClass
     * @param <T>
     * @return 获取模块类实例对象
     */
    public <T extends IModule> T getModule(Class<T> moduleClass) {
        return __moduleFactory.getBean(moduleClass);
    }

    /**
     * @param targetFactory 目标对象工厂
     * @param <T>
     * @return 将目标对象工厂的Parent设置为当前YMP容器的对象工厂
     */
    public <T extends IBeanFactory> T bindBeanFactory(T targetFactory) {
        targetFactory.setParent(__beanFactory);
        return targetFactory;
    }

    /**
     * YMP框架根对象工厂类
     */
    private static class BeanFactory extends DefaultBeanFactory {
        private final YMP __owner;

        public BeanFactory(YMP owner) {
            this.__owner = owner;
        }

        @Override
        public <T> T getBean(Class<T> clazz) {
            T _bean = super.getBean(clazz);
            // 重写此方法是为了在获取模块对象时始终保证其已被初始化
            if (_bean != null && _bean instanceof IModule) {
                IModule _module = (IModule) _bean;
                if (!_module.isInited()) {
                    try {
                        _module.init(__owner);
                    } catch (Exception e) {
                        throw new RuntimeException(RuntimeUtils.unwrapThrow(e));
                    }
                }
            }
            return _bean;
        }
    }

    /**
     * YMP框架配置类
     */
    private static class Config implements IConfig {

        private Properties __props;

        private Boolean __isDevelopMode;

        private List<String> __packageNames;

        private Boolean __isModuleAutoLoad;

        private Map<String, String> __paramsMap;

        private Map<String, Map<String, String>> __moduleCfgs;

        public Config() {
            __props = new Properties();
            __moduleCfgs = new ConcurrentHashMap<String, Map<String, String>>();
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
                    if (_in != null) _in.close();
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

        public boolean isModuleAutoload() {
            if (__isModuleAutoLoad == null) {
                String _tmpCfgV = StringUtils.defaultIfBlank(__props.getProperty("ymp.module_autoload"), "true");
                __isModuleAutoLoad = new BlurObject(_tmpCfgV).toBooleanValue();
            }
            return __isModuleAutoLoad;
        }

        public Map<String, String> getParams() {
            if (__paramsMap == null) {
                __paramsMap = new ConcurrentHashMap<String, String>();
                // 提取模块配置
                String _prefix = "ymp.params.";
                for (Object _key : __props.keySet()) {
                    if (StringUtils.startsWith((String) _key, _prefix)) {
                        String _cfgKey = StringUtils.substring((String) _key, _prefix.length());
                        String _cfgValue = __props.getProperty((String) _key);
                        __paramsMap.put(_cfgKey, _cfgValue);
                    }
                }
            }
            return Collections.unmodifiableMap(__paramsMap);
        }

        public String getParam(String name) {
            return this.getParams().get(name);
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
                __moduleCfgs.put(moduleName, Collections.unmodifiableMap(_cfgsMap));
            }
            return _cfgsMap;
        }
    }
}
