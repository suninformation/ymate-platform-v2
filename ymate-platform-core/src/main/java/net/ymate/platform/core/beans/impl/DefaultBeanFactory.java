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
package net.ymate.platform.core.beans.impl;

import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.beans.BeanMeta;
import net.ymate.platform.core.beans.IBeanFactory;
import net.ymate.platform.core.beans.IBeanInitializer;
import net.ymate.platform.core.beans.IBeanInjector;
import net.ymate.platform.core.beans.annotation.*;
import net.ymate.platform.core.beans.proxy.IProxy;
import net.ymate.platform.core.beans.proxy.IProxyFactory;
import net.ymate.platform.core.beans.proxy.IProxyFilter;
import net.ymate.platform.core.support.IDestroyable;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

/**
 * 默认对象工厂接口实现
 *
 * @author 刘镇 (suninformation@163.com) on 15-3-5 下午2:56
 */
public class DefaultBeanFactory implements IBeanFactory {

    private static final Log LOG = LogFactory.getLog(DefaultBeanFactory.class);

    private IApplication owner;

    private boolean initialized;

    private boolean useProxy;

    private IBeanFactory parentFactory;

    private IProxyFactory proxyFactory;

    private final Map<Class<? extends Annotation>, IBeanInjector> beanInjectorMap = new HashMap<>();

    /**
     * 对象类型 -> 对象实例
     */
    private final Map<Class<?>, BeanMeta> beanInstancesMap = new HashMap<>();

    /**
     * 接口类型 -> 对象类型
     */
    private final Map<Class<?>, Class<?>> beanInterfacesMap = new HashMap<>();

    private final Set<Class<?>> excludedInterfaceClasses = new HashSet<>();

    public DefaultBeanFactory() {
    }

    public DefaultBeanFactory(IProxyFactory proxyFactory) {
        this.proxyFactory = proxyFactory;
    }

    public DefaultBeanFactory(IBeanFactory parentFactory) {
        this.parentFactory = parentFactory;
    }

    public DefaultBeanFactory(IBeanFactory parentFactory, IProxyFactory proxyFactory) {
        this.parentFactory = parentFactory;
        this.proxyFactory = proxyFactory;
    }

    @Override
    public void initialize(IApplication owner) throws Exception {
        if (!initialized) {
            this.owner = owner;
            //
            useProxy = proxyFactory != null;
            if (useProxy && !proxyFactory.isInitialized()) {
                proxyFactory.initialize(owner);
            }
            //
            for (Map.Entry<Class<?>, BeanMeta> entry : this.getBeans().entrySet()) {
                if (!entry.getKey().isInterface() && entry.getValue().isSingleton()) {
                    Object beanProxyObj = buildBeanProxyIfNeed(entry.getValue().getBeanClass(), entry.getValue().getBeanObject());
                    entry.getValue().setBeanObject(beanProxyObj);
                }
            }
            //
            for (Map.Entry<Class<?>, BeanMeta> entry : this.getBeans().entrySet()) {
                if (!entry.getKey().isInterface() && entry.getValue().isSingleton()) {
                    initBeanIoC(entry.getKey(), entry.getValue().getBeanObject(), entry.getValue().getInitializer());
                }
            }
            //
            initialized = true;
        }
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public IApplication getOwner() {
        return owner;
    }

    @Override
    public void close() throws Exception {
        if (initialized) {
            initialized = false;
            //
            parentFactory = null;
            proxyFactory = null;
            //
            beanInjectorMap.clear();
            beanInterfacesMap.clear();
            excludedInterfaceClasses.clear();
            //
            Iterator<Map.Entry<Class<?>, BeanMeta>> entryIterator = beanInstancesMap.entrySet().iterator();
            while (entryIterator.hasNext()) {
                Map.Entry<Class<?>, BeanMeta> entry = entryIterator.next();
                entryIterator.remove();
                if (entry.getValue().isSingleton() && entry.getValue().getBeanObject() != null) {
                    if (entry.getValue().getBeanObject() instanceof IDestroyable) {
                        try {
                            ((IDestroyable) entry.getValue().getBeanObject()).close();
                        } catch (Exception e) {
                            if (LOG.isWarnEnabled()) {
                                LOG.warn(String.format("An exception occurred while destroying object [%s].", entry.getKey().getName()), RuntimeUtils.unwrapThrow(e));
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isOwnerDev() {
        return owner == null || owner.isDevEnv();
    }

    @Override
    public void registerInjector(Class<? extends Annotation> annClass, IBeanInjector injector) {
        if (!beanInjectorMap.containsKey(annClass)) {
            beanInjectorMap.put(annClass, injector);
            //
            if (isOwnerDev() && LOG.isDebugEnabled()) {
                LOG.debug(String.format("Injector class [%s:%s] registered.", annClass.getSimpleName(), injector.getClass().getName()));
            }
        } else if (owner.isDevEnv() && LOG.isWarnEnabled()) {
            LOG.warn(String.format("Injector class [%s:%s] duplicate registration is not allowed.", annClass.getSimpleName(), injector.getClass().getName()));
        }
    }

    @Override
    public void registerExcludedInterfaceClass(Class<?> excludedInterfaceClass) {
        if (excludedInterfaceClass.isInterface()) {
            excludedInterfaceClasses.add(excludedInterfaceClass);
        } else if (isOwnerDev() && LOG.isWarnEnabled()) {
            LOG.warn(String.format("Class [%s] is not an interface class, ignored.", excludedInterfaceClass.getName()));
        }
    }

    @Override
    public boolean isExcludedInterfaceClass(Class<?> excludedInterfaceClass) {
        if (excludedInterfaceClass.isInterface()) {
            boolean result = excludedInterfaceClass.isAnnotationPresent(Ignored.class) || excludedInterfaceClasses.contains(excludedInterfaceClass);
            return result && (parentFactory != null && parentFactory.isExcludedInterfaceClass(excludedInterfaceClass));
        }
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> clazz) {
        T obj = null;
        if (ClassUtils.isNormalClass(clazz)) {
            BeanMeta beanMeta = null;
            if (clazz.isInterface()) {
                Class<?> targetClass = beanInterfacesMap.get(clazz);
                if (targetClass != null) {
                    beanMeta = beanInstancesMap.get(targetClass);
                }
            } else {
                beanMeta = beanInstancesMap.get(clazz);
            }
            if (beanMeta != null) {
                if (!beanMeta.isSingleton()) {
                    try {
                        obj = (T) buildBeanProxyIfNeed(beanMeta.getBeanClass(), beanMeta.getBeanObject());
                        initBeanIoC(beanMeta.getBeanClass(), obj, beanMeta.getInitializer());
                    } catch (Exception e) {
                        LOG.warn(StringUtils.EMPTY, e);
                    }
                } else {
                    obj = (T) beanMeta.getBeanObject();
                }
            }
            if (obj == null && parentFactory != null) {
                obj = parentFactory.getBean(clazz);
            }
        }
        return obj;
    }

    @Override
    public Map<Class<?>, BeanMeta> getBeans() {
        return Collections.unmodifiableMap(beanInstancesMap);
    }

    @Override
    public void registerBean(BeanMeta beanMeta) {
        if (beanMeta != null && ClassUtils.isNormalClass(beanMeta.getBeanClass())) {
            // 注解、枚举和接口类型采用不同方式处理
            if (beanMeta.getBeanClass().isInterface()) {
                if (beanMeta.getBeanObject() != null) {
                    beanInstancesMap.put(beanMeta.getBeanClass(), beanMeta);
                    parseInterfaces(beanMeta);
                } else if (isOwnerDev() && LOG.isWarnEnabled()) {
                    LOG.warn(String.format("BeanMeta interface [%s] instance object not provided, ignored.", beanMeta.getBeanClass().getName()));
                }
            } else {
                parseClass(beanMeta);
            }
        }
    }

    @Override
    public void registerBean(Class<?> clazz) {
        registerBean(BeanMeta.create(clazz));
    }

    protected void parseClass(BeanMeta beanMeta) {
        beanInstancesMap.put(beanMeta.getBeanClass(), beanMeta);
        //
        parseInterfaces(beanMeta);
    }

    protected void parseInterfaces(BeanMeta beanMeta) {
        if (!beanMeta.isInterfaceIgnored()) {
            beanMeta.getInterfaces(excludedInterfaceClasses).forEach((interfaceClass) -> {
                beanInterfacesMap.put(interfaceClass, beanMeta.getBeanClass());
            });
        } else if (isExcludedInterfaceClass(beanMeta.getBeanClass())) {
            beanInterfacesMap.put(beanMeta.getBeanClass(), beanMeta.getBeanClass());
        }
    }

    @Override
    public IBeanFactory getParent() {
        return parentFactory;
    }

    @Override
    public IProxyFactory getProxyFactory() {
        return proxyFactory;
    }

    private Object buildBeanProxyIfNeed(Class<?> targetClass, Object targetObject) throws IllegalAccessException, InstantiationException {
        if (useProxy) {
            List<IProxy> proxies = proxyFactory.getProxies(new IProxyFilter() {

                private boolean checkAnnotation(Proxy targetProxyAnn) {
                    // 若设置了自定义注解类型，则判断targetClass是否匹配，否则返回true
                    if (targetProxyAnn != null && targetProxyAnn.annotation().length > 0) {
                        for (Class<? extends Annotation> annClass : targetProxyAnn.annotation()) {
                            if (targetClass.isAnnotationPresent(annClass)) {
                                return true;
                            }
                        }
                        return false;
                    }
                    return true;
                }

                @Override
                public boolean filter(IProxy targetProxy) {
                    CleanProxy cleanProxy = targetClass.getAnnotation(CleanProxy.class);
                    if (cleanProxy != null) {
                        if (cleanProxy.value().length > 0) {
                            for (Class<? extends IProxy> proxyClass : cleanProxy.value()) {
                                if (proxyClass.equals(targetProxy.getClass())) {
                                    return false;
                                }
                            }
                        } else {
                            return false;
                        }
                    }
                    Proxy proxyAnn = targetProxy.getClass().getAnnotation(Proxy.class);
                    // 若已设置作用包路径
                    if (proxyAnn != null && StringUtils.isNotBlank(proxyAnn.packageScope())) {
                        // 若当前类对象所在包路径匹配
                        if (!StringUtils.startsWith(targetClass.getPackage().getName(), proxyAnn.packageScope())) {
                            return false;
                        }
                    }
                    return checkAnnotation(proxyAnn);
                }
            });
            if (!proxies.isEmpty()) {
                // 由于创建代理是通过接口重新实例化对象并覆盖原对象，所以需要复制原有对象成员（暂时先这样吧，还没想到好的处理办法）
                Object proxyObject = proxyFactory.createProxy(targetClass, proxies);
                if (proxyObject != null) {
                    if (targetObject != null) {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn(String.format("Important Warning: It is not recommended to register instance[%s] objects directly with BeanFactory!!!", targetObject.getClass().getName()));
                        }
                        return ClassUtils.wrapper(targetObject).duplicate(proxyObject);
                    }
                    return proxyObject;
                }
            }
        }
        return targetObject != null ? targetObject : targetClass.newInstance();
    }

    /**
     * 对目标类进行IoC注入
     *
     * @param targetClass  目标类型对象(不允许是代理对象)
     * @param targetObject 目标类型对象实例
     * @param initializer  自定义初始化回调接口
     * @throws Exception 可能产生的异常
     */
    private void initBeanIoC(Class<?> targetClass, Object targetObject, BeanMeta.IInitializer initializer) throws Exception {
        Field[] fields = targetClass.getDeclaredFields();
        if (ArrayUtils.isNotEmpty(fields)) {
            for (Field field : fields) {
                Object injectObj = null;
                if (field.isAnnotationPresent(Inject.class)) {
                    if (field.isAnnotationPresent(By.class)) {
                        By injectBy = field.getAnnotation(By.class);
                        injectObj = this.getBean(injectBy.value());
                    } else {
                        injectObj = this.getBean(field.getType());
                    }
                }
                injectObj = tryBeanInjector(targetClass, field, injectObj);
                if (injectObj != null) {
                    field.setAccessible(true);
                    field.set(targetObject, injectObj);
                }
            }
        }
        if (initializer != null) {
            initializer.initialize(targetObject);
        }
        if (targetObject instanceof IBeanInitializer) {
            ((IBeanInitializer) targetObject).afterInitialized();
        }
    }

    private Object tryBeanInjector(Class<?> targetClass, Field field, Object originInject) {
        if (!beanInjectorMap.isEmpty()) {
            for (Map.Entry<Class<? extends Annotation>, IBeanInjector> entry : beanInjectorMap.entrySet()) {
                Annotation annotation = field.getAnnotation(entry.getKey());
                if (annotation != null) {
                    return entry.getValue().inject(this, annotation, targetClass, field, originInject);
                }
            }
        }
        return originInject;
    }
}
