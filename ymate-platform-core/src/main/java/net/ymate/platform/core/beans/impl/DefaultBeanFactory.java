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
package net.ymate.platform.core.beans.impl;

import net.ymate.platform.core.beans.IBeanFactory;
import net.ymate.platform.core.beans.IBeanHandler;
import net.ymate.platform.core.beans.IBeanLoader;
import net.ymate.platform.core.beans.annotation.By;
import net.ymate.platform.core.beans.annotation.Inject;
import net.ymate.platform.core.beans.annotation.Proxy;
import net.ymate.platform.core.beans.proxy.IProxy;
import net.ymate.platform.core.beans.proxy.IProxyFactory;
import net.ymate.platform.core.beans.proxy.IProxyFilter;
import net.ymate.platform.core.util.ClassUtils;
import org.apache.commons.lang.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

/**
 * 默认对象工厂接口实现
 *
 * @author 刘镇 (suninformation@163.com) on 15-3-5 下午2:56
 * @version 1.0
 */
public class DefaultBeanFactory implements IBeanFactory {

    private IBeanFactory __parentFactory;

    private List<String> __packageNames;

    private List<Class<?>> __excludedClassSet;

    private Map<Class<? extends Annotation>, IBeanHandler> __beanHandlerMap;

    // 对象类型 -> 对象实例
    private Map<Class<?>, Object> __beanInstancesMap;

    // 接口类型 -> 对象类型
    private Map<Class<?>, Class<?>> __beanInterfacesMap;

    private IBeanLoader __beanLoader;

    public DefaultBeanFactory() {
        this.__packageNames = new ArrayList<String>();
        this.__excludedClassSet = new ArrayList<Class<?>>();
        this.__beanHandlerMap = new HashMap<Class<? extends Annotation>, IBeanHandler>();
        this.__beanInstancesMap = new HashMap<Class<?>, Object>();
        this.__beanInterfacesMap = new HashMap<Class<?>, Class<?>>();
    }

    public DefaultBeanFactory(IBeanFactory parent) {
        this();
        this.__parentFactory = parent;
    }

    public void registerHandler(Class<? extends Annotation> annoClass, IBeanHandler handler) {
        this.__beanHandlerMap.put(annoClass, handler);
    }

    public void registerHandler(Class<? extends Annotation> annoClass) {
        this.__beanHandlerMap.put(annoClass, IBeanHandler.DEFAULT_HANDLER);
    }

    public void registerPackage(String packageName) {
        this.__packageNames.add(packageName);
    }

    public void registerExcludedClass(Class<?> excludedClass) {
        if (excludedClass.isInterface()) {
            this.__excludedClassSet.add(excludedClass);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> clazz) {
        T _obj = null;
        if (clazz != null && !clazz.isAnnotation()) {
            if (clazz.isInterface()) {
                Class<?> _targetClass = this.__beanInterfacesMap.get(clazz);
                _obj = (T) this.__beanInstancesMap.get(_targetClass);
            } else {
                _obj = (T) this.__beanInstancesMap.get(clazz);
            }
            if (_obj == null && this.__parentFactory != null) {
                _obj = this.__parentFactory.getBean(clazz);
            }
        }
        return _obj;
    }

    public Map<Class<?>, Object> getBeans() {
        return Collections.unmodifiableMap(this.__beanInstancesMap);
    }

    public void registerBean(Class<?> clazz) throws Exception {
        registerBean(clazz, clazz.newInstance());
    }

    public void registerBean(Class<?> clazz, Object object) {
        // 注解、枚举和接口类型采用不同方式处理
        if (clazz.isInterface()) {
            Class<?> _targetClass = object.getClass();
            __beanInstancesMap.put(_targetClass, object);
            __addClassInterfaces(_targetClass);
        } else if (!clazz.isAnnotation() && !clazz.isEnum()) {
            __addClass(clazz, object);
        }
    }

    public void init() throws Exception {
        if (this.__beanLoader == null) {
            if (this.__parentFactory != null) {
                this.__beanLoader = this.__parentFactory.getLoader();
            }
            if (this.__beanLoader == null) {
                this.__beanLoader = new DefaultBeanLoader();
            }
        }
        if (!__packageNames.isEmpty()) for (String _packageName : __packageNames) {
            List<Class<?>> _classes = this.__beanLoader.load(_packageName);
            for (Class<?> _class : _classes) {
                // 不扫描注解、枚举和接口类
                if (!_class.isAnnotation() && !_class.isEnum() && !_class.isInterface()) {
                    Annotation[] _annotations = _class.getAnnotations();
                    if (_annotations != null && _annotations.length > 0) {
                        for (Annotation _anno : _annotations) {
                            IBeanHandler _handler = __beanHandlerMap.get(_anno.annotationType());
                            if (_handler != null) {
                                Object _instance = _handler.handle(_class);
                                if (_instance != null) __addClass(_class, _instance);
                            }
                        }
                    }
                }
            }
        }
    }

    protected void __addClass(Class<?> targetClass, Object instance) {
        __beanInstancesMap.put(targetClass, instance);
        //
        __addClassInterfaces(targetClass);
    }

    protected void __addClassInterfaces(Class<?> targetClass) {
        Class<?>[] _interfaces = targetClass.getInterfaces();
        for (Class<?> _interface : _interfaces) {
            // 排除JDK自带的接口和自己定接口列表
            if (/* k.startsWith("java") || */__excludedClassSet.contains(_interface)) {
                continue;
            }
            __beanInterfacesMap.put(_interface, targetClass);
        }
    }

    public void destroy() throws Exception {
        this.__parentFactory = null;
        this.__packageNames = null;
        this.__excludedClassSet = null;
        this.__beanHandlerMap = null;
        this.__beanInstancesMap = null;
        this.__beanInterfacesMap = null;
        this.__beanLoader = null;
    }

    public IBeanFactory getParent() {
        return __parentFactory;
    }

    public void setParent(IBeanFactory parent) {
        this.__parentFactory = parent;
    }

    public IBeanLoader getLoader() {
        return this.__beanLoader;
    }

    public void setLoader(IBeanLoader loader) {
        this.__beanLoader = loader;
    }

    public void bindProxy(IProxyFactory proxyFactory) throws Exception {
        for (Map.Entry<Class<?>, Object> _entry : this.getBeans().entrySet()) {
            if (!_entry.getKey().isInterface()) {
                final Class<?> _targetClass = _entry.getKey();
                List<IProxy> _targetProxies = proxyFactory.getProxies(new IProxyFilter() {

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
                    // 由于创建代理是通过接口重新实例化对象并覆盖原对象，所以需要复制原有对象成员（暂时先这样吧，还没想到好的处理办法）
                    Object _targetObj = ClassUtils.wrapper(_entry.getValue()).duplicate(proxyFactory.createProxy(_targetClass, _targetProxies));
                    this.registerBean(_targetClass, _targetObj);
                }
            }
        }
    }

    public void initBeanIoC() throws Exception {
        for (Map.Entry<Class<?>, Object> _bean : this.getBeans().entrySet()) {
            Field[] _fields = _bean.getKey().getDeclaredFields();
            if (_fields != null && _fields.length > 0) {
                for (Field _field : _fields) {
                    if (_field.isAnnotationPresent(Inject.class)) {
                        Object _injectObj = null;
                        if (_field.isAnnotationPresent(By.class)) {
                            By _injectBy = _field.getAnnotation(By.class);
                            _injectObj = this.getBean(_injectBy.value());
                        } else {
                            _injectObj = this.getBean(_field.getType());
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
}
