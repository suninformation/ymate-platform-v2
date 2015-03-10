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
import net.ymate.platform.core.beans.annotation.By;
import net.ymate.platform.core.beans.annotation.Inject;
import net.ymate.platform.core.beans.annotation.Proxy;
import net.ymate.platform.core.beans.impl.DefaultBeanFactory;
import net.ymate.platform.core.beans.impl.proxy.DefaultProxyFactory;
import net.ymate.platform.core.beans.proxy.IProxy;
import net.ymate.platform.core.beans.proxy.IProxyFactory;
import net.ymate.platform.core.beans.proxy.IProxyFilter;
import net.ymate.platform.core.module.IModule;
import net.ymate.platform.core.module.annotation.Module;
import net.ymate.platform.core.util.ClassUtils;
import org.apache.commons.lang.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * YMP框架核心管理器
 *
 * @author 刘镇 (suninformation@163.com) on 15-3-4 下午6:47
 * @version 1.0
 */
public class YMP {

    private static final String __YMP_BASE_PACKAGE = "net.ymate.platform";

    private static IBeanFactory __beanFactory;

    private static IProxyFactory __proxyFactory;

    private static List<IModule> __modules;

    private static boolean __inited;

    private static IBeanFactory __doInitBeanFactory() {
        // 设置YMP框架基础包为根工厂扫描路径
        // 并根据配置参数注册自动扫描应用包路径
        __beanFactory.registerPackage(__YMP_BASE_PACKAGE);
        for (String _packageName : Config.get().getAutoscanPackages()) {
            if (!_packageName.startsWith(__YMP_BASE_PACKAGE)) {
                __beanFactory.registerPackage(_packageName);
            }
        }
        // 注册YMP框架核心对象处理器
        __beanFactory.registerHandler(Bean.class);
        // 注册对象工厂需要忽略的接口类型
        __beanFactory.registerExcludedClass(IModule.class);
        __beanFactory.registerHandler(Module.class, new IBeanHandler() {
            public Object handle(Class<?> targetClass) throws Exception {
                if (ClassUtils.isInterfaceOf(targetClass, IModule.class)) {
                    IModule _module = (IModule) targetClass.newInstance();
                    __modules.add(_module);
                    return _module;
                }
                return null;
            }
        });
        __beanFactory.registerExcludedClass(IProxy.class);
        __beanFactory.registerHandler(Proxy.class, new IBeanHandler() {
            public Object handle(Class<?> targetClass) throws Exception {
                if (ClassUtils.isInterfaceOf(targetClass, IProxy.class)) {
                    __proxyFactory.registerProxy((IProxy) targetClass.newInstance());
                }
                return null;
            }
        });
        return __beanFactory;
    }

    private static void __doInitProxyFactory() {
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

    private static void __doInitIoC() throws Exception {
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
    public static synchronized void init() throws Exception {
        if (!__inited) {
            // 加载并初始化YMP框架配置
            Config.get();
            // 创建模块对象引用集合
            __modules = new ArrayList<IModule>();
            // 创建根对象工厂
            __beanFactory = new DefaultBeanFactory();
            // 创建代理工厂并初始化
            __proxyFactory = new DefaultProxyFactory();
            // 初始化根对象工厂
            __doInitBeanFactory().init();
            // 初始化所有已加载模块
            for (IModule _module : __modules) {
                _module.init();
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
    public static void destroy() throws Exception {
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
     * @return 返回YMP框架是否已初始化
     */
    public static boolean isInited() {
        return __inited;
    }

    /**
     * @return 返回根对象工厂实例
     */
    public static IBeanFactory getBeanFactory() {
        return __beanFactory;
    }

}
