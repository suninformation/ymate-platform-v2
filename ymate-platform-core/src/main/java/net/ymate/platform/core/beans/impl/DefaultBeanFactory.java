/*
 * Copyright 2007-present the original author or authors.
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

import net.ymate.platform.commons.util.ClassFieldCache;
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
import net.ymate.platform.core.module.IModule;
import net.ymate.platform.core.support.IDestroyable;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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

    private final Map<Class<? extends Annotation>, IBeanInjector> beanInjectorMap = new ConcurrentHashMap<>();

    /**
     * 对象类型 -> 对象实例
     */
    private final Map<Class<?>, BeanMeta> beanInstancesMap = new ConcurrentHashMap<>();

    /**
     * 接口类型 -> 对象类型
     */
    private final Map<Class<?>, Class<?>> beanInterfacesMap = new ConcurrentHashMap<>();

    private final Set<Class<?>> excludedInterfaceClasses = ConcurrentHashMap.newKeySet();

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
                    obj = (T) doCreateObjectInst(beanMeta);
                } else {
                    obj = (T) beanMeta.getBeanObject();
                    if (obj == null) {
                        beanMeta.getCreateLock().lock();
                        try {
                            obj = (T) beanMeta.getBeanObject();
                            if (obj == null) {
                                obj = (T) doCreateObjectInst(beanMeta);
                                if (obj != null) {
                                    beanMeta.setBeanObject(obj);
                                }
                            }
                        } finally {
                            beanMeta.getCreateLock().unlock();
                        }
                    }
                }
            }
            if (obj == null && parentFactory != null) {
                obj = parentFactory.getBean(clazz);
            }
        }
        return obj;
    }

    private Object doCreateObjectInst(BeanMeta beanMeta) {
        Object objectInst = null;
        try {
            objectInst = buildBeanProxyIfNeed(beanMeta.getBeanClass(), beanMeta.getBeanObject());
            initBeanIoC(beanMeta.getBeanClass(), objectInst, beanMeta.getInitializer());
        } catch (Exception e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(StringUtils.EMPTY, e);
            }
        }
        return objectInst;
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
                    beanInstancesMap.put(beanMeta.getBeanObject().getClass(), beanMeta);
                    if (!beanMeta.isInterfaceIgnored()) {
                        beanMeta.getInterfaces(excludedInterfaceClasses).forEach((interfaceClass) -> beanInterfacesMap.put(interfaceClass, beanMeta.getBeanObject().getClass()));
                    } else {
                        beanInterfacesMap.put(beanMeta.getBeanClass(), beanMeta.getBeanObject().getClass());
                    }
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
        if (!beanMeta.isInterfaceIgnored()) {
            beanMeta.getInterfaces(excludedInterfaceClasses).forEach((interfaceClass) -> beanInterfacesMap.put(interfaceClass, beanMeta.getBeanClass()));
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

    private Object buildBeanProxyIfNeed(Class<?> targetClass, Object targetObject) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        if (useProxy && !Modifier.isFinal(targetClass.getModifiers())) {
            List<IProxy> proxies = proxyFactory.getProxies(new BeanProxyFilter(targetClass));
            if (!proxies.isEmpty()) {
                // 由于创建代理是通过接口重新实例化对象并覆盖原对象，所以需要复制原有对象成员（暂时先这样吧，还没想到好的处理办法）
                // 注意：每次调用 createProxy 均创建新的代理实例（单例与否由 BeanMeta 控制），代理类的缓存与复用由各 IProxyFactory 实现内部负责
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
        return targetObject != null ? targetObject : targetClass.getDeclaredConstructor().newInstance();
    }

    /**
     * Bean代理过滤器，用于判断代理是否适用于目标类
     */
    static class BeanProxyFilter implements IProxyFilter {

        private final Class<?> targetClass;

        BeanProxyFilter(Class<?> targetClass) {
            this.targetClass = targetClass;
        }

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
                if (!Strings.CS.startsWith(targetClass.getPackage().getName(), proxyAnn.packageScope())) {
                    return false;
                }
            }
            return checkAnnotation(proxyAnn);
        }
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
        List<Field> fields = ClassFieldCache.getFields(targetClass);
        if (!fields.isEmpty()) {
            for (Field field : fields) {
                Object injectObj = null;
                if (field.isAnnotationPresent(Inject.class)) {
                    if (!field.getType().isInterface() && ClassUtils.isInterfaceOf(field.getType(), IModule.class)) {
                        injectObj = owner.getModuleManager().getModule(field.getType().getName());
                    } else {
                        if (field.isAnnotationPresent(By.class)) {
                            By injectBy = field.getAnnotation(By.class);
                            if (!injectBy.value().isInterface() && ClassUtils.isInterfaceOf(injectBy.value(), IModule.class)) {
                                injectObj = owner.getModuleManager().getModule(injectBy.value().getName());
                            } else {
                                injectObj = this.getBean(injectBy.value());
                            }
                        } else {
                            injectObj = this.getBean(field.getType());
                        }
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
            ((IBeanInitializer) targetObject).afterInitialized(this);
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
