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
package net.ymate.platform.core;

import net.ymate.platform.core.beans.*;
import net.ymate.platform.core.beans.annotation.*;
import net.ymate.platform.core.beans.impl.DefaultBeanFactory;
import net.ymate.platform.core.beans.impl.DefaultBeanLoader;
import net.ymate.platform.core.beans.intercept.InterceptProxy;
import net.ymate.platform.core.beans.proxy.IProxy;
import net.ymate.platform.core.beans.proxy.IProxyFactory;
import net.ymate.platform.core.event.Events;
import net.ymate.platform.core.event.annotation.Event;
import net.ymate.platform.core.event.annotation.EventRegister;
import net.ymate.platform.core.handle.*;
import net.ymate.platform.core.i18n.I18N;
import net.ymate.platform.core.module.IModule;
import net.ymate.platform.core.module.ModuleEvent;
import net.ymate.platform.core.module.annotation.Module;
import net.ymate.platform.core.serialize.ISerializer;
import net.ymate.platform.core.serialize.annotation.Serializer;
import net.ymate.platform.core.support.ConfigBuilder;
import net.ymate.platform.core.support.IDestroyable;
import net.ymate.platform.core.support.IInitializable;
import net.ymate.platform.core.support.RecycleHelper;
import net.ymate.platform.core.util.ClassUtils;
import net.ymate.platform.core.util.ResourceUtils;
import net.ymate.platform.core.util.RuntimeUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * YMP框架核心管理器
 *
 * @author 刘镇 (suninformation@163.com) on 2012-12-23 下午5:52:44
 * @version 1.0
 */
public class YMP {

    public static final Version VERSION = new Version(2, 0, 6, Version.VersionType.Release);

    private static final Log _LOG = LogFactory.getLog(YMP.class);

    private static final String __YMP_BASE_PACKAGE = "net.ymate.platform";

    private static volatile YMP __instance;

    private final IConfig __config;

    private boolean __inited;

    private boolean __errorFlag;

    private IBeanFactory __moduleFactory;

    private IBeanFactory __beanFactory;

    private IProxyFactory __proxyFactory;

    private Set<Class<? extends IModule>> __modules = new HashSet<Class<? extends IModule>>();

    private Events __events;

    /**
     * @return 返回默认YMP框架核心管理器对象实例，若未实例化或已销毁则重新创建对象实例
     */
    public static YMP get() {
        if (__instance == null) {
            synchronized (VERSION) {
                if (__instance == null) {
                    __instance = new YMP(ConfigBuilder.system().build());
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
        // 创建对象工厂
        __moduleFactory = new BeanFactory(this);
        __beanFactory = new DefaultBeanFactory(this, __moduleFactory);
    }

    private void __registerPackages() {
        __moduleFactory.registerPackage(__YMP_BASE_PACKAGE);
        __beanFactory.registerPackage(__YMP_BASE_PACKAGE);
        for (String _packageName : __config.getAutoscanPackages()) {
            __moduleFactory.registerPackage(_packageName);
            __beanFactory.registerPackage(_packageName);
        }
        for (String _packageName : __config.getExcludedPackages()) {
            __moduleFactory.registerExcludedPackage(_packageName);
            __beanFactory.registerExcludedPackage(_packageName);
        }
    }

    /**
     * @return 加载Banner字符徽标
     */
    private String __loadBanner() {
        String _banner = null;
        InputStream _input = null;
        try {
            _input = ResourceUtils.getResourceAsStream("banner.txt", YMP.class);
            if (_input != null) {
                _banner = IOUtils.toString(_input, "UTF-8");
            }
        } catch (IOException ignored) {
        } finally {
            IOUtils.closeQuietly(_input);
        }
        if (StringUtils.isBlank(_banner)) {
            _banner = "\n__   ____  __ ____          ____  \n" +
                    "\\ \\ / /  \\/  |  _ \\  __   _|___ \\ \n" +
                    " \\ V /| |\\/| | |_) | \\ \\ / / __) |\n" +
                    "  | | | |  | |  __/   \\ V / / __/ \n" +
                    "  |_| |_|  |_|_|       \\_/ |_____|  Website: http://www.ymate.net/";
        }
        return _banner;
    }

    /**
     * 初始化YMP框架
     *
     * @return 返回当前YMP核心框架管理器对象
     * @throws Exception 框架初始化失败时将抛出异常
     */
    public YMP init() throws Exception {
        if (!__inited) {
            //
            _LOG.info(__loadBanner());
            //
            StopWatch _watch = new StopWatch();
            _watch.start();
            //
            _LOG.info("Initializing ymate-platform-core-" + VERSION + " - debug:" + __config.isDevelopMode() + " - env:" + __config.getRunEnv().name().toLowerCase());

            // 初始化I18N
            I18N.initialize(__config.getDefaultLocale(), __config.getI18NEventHandler());
            // 初始化事件管理器，并注册框架、模块事件
            __events = Events.create(this, __config.getEventConfigs());
            __events.registerEvent(ApplicationEvent.class);
            __events.registerEvent(ModuleEvent.class);
            // 检查对象加载器, 若未配置则采用默认加载器
            IBeanLoader _beanLoader = __config.getBeanLoader();
            if (_beanLoader == null) {
                _beanLoader = new DefaultBeanLoader();
            }
            // 配置模块对象工厂
            __moduleFactory.setLoader(_beanLoader);
            __moduleFactory.setExcludedFiles(__config.getExcludedFiles());
            __moduleFactory.registerHandler(Module.class, new ModuleHandler(this));
            __moduleFactory.registerHandler(Proxy.class, new ProxyHandler(this));
            __moduleFactory.registerHandler(Event.class, new EventHandler(this));
            __moduleFactory.registerHandler(EventRegister.class, new EventRegisterHandler(this));
            __moduleFactory.registerHandler(Injector.class, new InjectorHandler(__beanFactory));
            __moduleFactory.registerHandler(Serializer.class, new SerializerHandler(this));
            // 配置根对象工厂
            __beanFactory.setLoader(_beanLoader);
            __beanFactory.setExcludedFiles(__config.getExcludedFiles());
            __beanFactory.registerExcludedClass(IInitializable.class);
            __beanFactory.registerExcludedClass(IDestroyable.class);
            __beanFactory.registerExcludedClass(ISerializer.class);
            __beanFactory.registerHandler(Bean.class);
            __beanFactory.registerHandler(Interceptor.class, new InterceptorHandler(this));
            __beanFactory.registerHandler(Packages.class, new PackagesHandler(this));
            // 设置自动扫描应用包路径
            __registerPackages();
            // 配置代理工厂
            __proxyFactory = __config.getProxyFactory();
            __proxyFactory.init(this);
            __proxyFactory.registerProxy(new InterceptProxy());
            // 初始化模块对象工厂
            __moduleFactory.init();
            __moduleFactory.initProxy(null);
            __moduleFactory.initIoC();
            // 优化尝试加载配置体系模块，若存在则将决定配置文件加载的路径
            if (!isModuleExcluded(IConfig.MODULE_NAME_CONFIGURATION) && !isModuleExcluded(IConfig.MODULE_CLASS_NAME_CONFIGURATION)) {
                getModule(IConfig.MODULE_CLASS_NAME_CONFIGURATION);
            }
            //
            for (Class<? extends IModule> _moduleClass : __modules) {
                IModule _module = getModule(_moduleClass);
                if (!_module.isInited()) {
                    try {
                        _module.init(this);
                        // 触发模块初始化完成事件
                        __events.fireEvent(new ModuleEvent(_module, ModuleEvent.EVENT.MODULE_INITED));
                    } catch (Exception e) {
                        _LOG.error("Module '" + _module.getName() + "' initialization error: ", RuntimeUtils.unwrapThrow(e));
                        //
                        __errorFlag = true;
                        __inited = true;
                        break;
                    }
                }
            }
            if (!__errorFlag) {
                // 初始化根对象工厂
                __beanFactory.init();
                // 初始化对象代理
                __beanFactory.initProxy(__proxyFactory);
                // IoC依赖注入
                __beanFactory.initIoC();
                //
                __inited = true;
                //
                _watch.stop();
                _LOG.info("RecycleHelper has registered the number of resources to be recycled: " + RecycleHelper.getInstance().size());
                _LOG.info("Initialization completed, Total time: " + _watch.getTime() + "ms");
                // 触发框架初始化完成事件
                __events.fireEvent(new ApplicationEvent(this, ApplicationEvent.EVENT.APPLICATION_INITED));
            }
        }
        return this;
    }

    /**
     * 销毁YMP框架
     *
     * @throws Exception 框架销毁失败时将抛出异常
     */
    public void destroy() throws Exception {
        if (__inited) {
            if (!__errorFlag) {
                // 触发框架销毁事件
                __events.fireEvent(new ApplicationEvent(this, ApplicationEvent.EVENT.APPLICATION_DESTROYED));
            }
            //
            RecycleHelper.getInstance().recycle();
            //
            __inited = false;
            // 销毁所有已加载模块
            for (Class<? extends IModule> _moduleClass : __modules) {
                IModule _module = getModule(_moduleClass);
                if (_module.isInited()) {
                    // 触发模块销毁事件
                    __events.fireEvent(new ModuleEvent(_module, ModuleEvent.EVENT.MODULE_DESTROYED));
                    //
                    _module.destroy();
                }
            }
            __modules = null;
            // 销毁根对象工厂
            __moduleFactory.destroy();
            __moduleFactory = null;
            //
            __beanFactory.destroy();
            __beanFactory = null;
            // 销毁代理工厂
            __proxyFactory = null;
            // 销毁事件管理器
            __events.destroy();
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
     * @param annoClass 自定义注解类型
     * @param handler   注解对象处理器
     */
    public void registerHandler(Class<? extends Annotation> annoClass, IBeanHandler handler) {
        if (annoClass.equals(Event.class) || annoClass.equals(EventRegister.class)
                || annoClass.equals(Injector.class) || annoClass.equals(Interceptor.class)
                || annoClass.equals(Module.class) || annoClass.equals(Packages.class)
                || annoClass.equals(Proxy.class) || annoClass.equals(Serializer.class)) {
            if (getConfig().isDevelopMode() && _LOG.isWarnEnabled()) {
                _LOG.warn("Handler [" + annoClass.getSimpleName() + "] duplicate registration is not allowed");
            }
            return;
        }
        __beanFactory.registerHandler(annoClass, handler);
    }

    public void registerHandler(Class<? extends Annotation> annoClass) {
        registerHandler(annoClass, IBeanHandler.DEFAULT_HANDLER);
    }

    public void registerInjector(Class<? extends Annotation> annoClass, IBeanInjector injector) {
        __beanFactory.registerInjector(annoClass, injector);
    }

    /**
     * 注册排除的接口类
     *
     * @param excludedClass 预排除的接口类型
     */
    public void registerExcludedClass(Class<?> excludedClass) {
        __beanFactory.registerExcludedClass(excludedClass);
    }

    /**
     * 注册类
     *
     * @param clazz 目标类
     */
    public void registerBean(Class<?> clazz) {
        __beanFactory.registerBean(clazz);
    }

    @Deprecated
    public void registerBean(Class<?> clazz, Object object) {
        __beanFactory.registerBean(clazz, object);
    }

    public void registerBean(BeanMeta beanMeta) {
        __beanFactory.registerBean(beanMeta);
    }

    /**
     * @param <T>   返回类型
     * @param clazz 接口类型
     * @return 提取类型为clazz的对象实例
     */
    public <T> T getBean(Class<T> clazz) {
        return __beanFactory.getBean(clazz);
    }

    /**
     * 向工厂注册代理类对象
     *
     * @param proxy 目标代理类
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
     * @param moduleClass 目标模块类
     */
    public void registerModule(Class<? extends IModule> moduleClass) {
        if (!__inited && moduleClass != null) {
            if (!__modules.contains(moduleClass)) {
                __moduleFactory.registerBean(BeanMeta.create(moduleClass, true));
                __modules.add(moduleClass);
            } else if (getConfig().isDevelopMode() && _LOG.isWarnEnabled()) {
                _LOG.warn("Module [" + moduleClass + "] duplicate registration is not allowed");
            }
        }
    }

    /**
     * @param moduleClass 模块类型
     * @param <T>         模块类型
     * @return 获取模块类实例对象
     */
    public <T extends IModule> T getModule(Class<T> moduleClass) {
        return __moduleFactory.getBean(moduleClass);
    }

    /**
     * @param moduleClassName 模块类名称
     * @param <T>             模块类型
     * @return 获取模块类实例对象
     */
    @SuppressWarnings("unchecked")
    public <T extends IModule> T getModule(String moduleClassName) {
        if (!isModuleExcluded(moduleClassName)) {
            try {
                return (T) __moduleFactory.getBean(ClassUtils.loadClass(moduleClassName, this.getClass()));
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    /**
     * @param moduleName 模块名称或类名
     * @return 判断指定名称或类名的模块是否已被排除
     */
    public boolean isModuleExcluded(String moduleName) {
        return __config.getExcludedModules().contains("moduleName");
    }

    /**
     * @return 获取事件管理器
     */
    public Events getEvents() {
        return __events;
    }

    /**
     * @param targetFactory 目标对象工厂
     * @param <T>           对象工厂类型
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

        public BeanFactory(YMP owner) {
            super(owner);
        }

        @Override
        public <T> T getBean(Class<T> clazz) {
            T _bean = super.getBean(clazz);
            // 重写此方法是为了在获取模块对象时始终保证其已被初始化
            if (_bean instanceof IModule) {
                IModule _module = (IModule) _bean;
                if (!_module.isInited()) {
                    if (getOwner().getConfig().getExcludedModules().contains(_module.getName()) || getOwner().getConfig().getExcludedModules().contains(_module.getClass().getName())) {
                        return null;
                    }
                    try {
                        _module.init(getOwner());
                        // 触发模块初始化完成事件
                        getOwner().getEvents().fireEvent(new ModuleEvent(_module, ModuleEvent.EVENT.MODULE_INITED));
                    } catch (Exception e) {
                        throw new RuntimeException(RuntimeUtils.unwrapThrow(e));
                    }
                }
            }
            return _bean;
        }

        @Override
        protected void __addClassInterfaces(BeanMeta beanMeta) {
            // Do Nothing...
        }
    }
}
