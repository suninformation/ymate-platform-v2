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
package net.ymate.platform.core.beans;

import net.ymate.platform.core.beans.annotation.Ignored;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * IoC受控类描述对象
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/21 上午10:50
 */
public class BeanMeta implements Serializable {

    private final Class<?> beanClass;

    private Object beanObject;

    private final boolean singleton;

    private boolean interfaceIgnored;

    private IInitializer initializer;

    public static BeanMeta create(Class<?> beanClass) {
        return new BeanMeta(beanClass, false);
    }

    public static BeanMeta create(Class<?> beanClass, boolean singleton) {
        return new BeanMeta(beanClass, singleton);
    }

    public static BeanMeta create(Class<?> beanClass, boolean singleton, IInitializer initializer) {
        return new BeanMeta(beanClass, singleton, initializer);
    }

    public BeanMeta(Class<?> beanClass, boolean singleton) {
        this(beanClass, singleton, null);
    }

    public BeanMeta(Class<?> beanClass, boolean singleton, IInitializer initializer) {
        this.beanClass = beanClass;
        this.singleton = singleton;
        //
        this.initializer = initializer;
    }

    public boolean isSingleton() {
        return singleton;
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }

    public Object getBeanObject() {
        return beanObject;
    }

    /**
     * 设置对象实例（此方法仅用于框架内部使用, 不建议直接设置实例对象, 应该通过Class类型进行注册）
     *
     * @param beanObject 类实例对象
     */
    public void setBeanObject(Object beanObject) {
        if (!singleton) {
            throw new IllegalArgumentException("Non-singleton object.");
        }
        this.beanObject = beanObject;
    }

    /**
     * 是否忽略接口分析
     *
     * @return 返回true表示忽略
     */
    public boolean isInterfaceIgnored() {
        return interfaceIgnored;
    }

    public void setInterfaceIgnored(boolean interfaceIgnored) {
        this.interfaceIgnored = interfaceIgnored;
    }

    public IInitializer getInitializer() {
        return initializer;
    }

    public void setInitializer(IInitializer initializer) {
        this.initializer = initializer;
    }

    public Set<Class<?>> getInterfaces(Collection<Class<?>> excludedClassSet) {
        Set<Class<?>> returnValues = new HashSet<>();
        Class<?>[] interfaces;
        if (beanClass.isInterface()) {
            if (beanObject != null) {
                interfaces = beanObject.getClass().getInterfaces();
            } else {
                interfaces = new Class<?>[]{beanClass};
            }
        } else {
            interfaces = beanClass.getInterfaces();
        }
        for (Class<?> interfaceClass : interfaces) {
            if (!interfaceClass.isAnnotationPresent(Ignored.class)) {
                // 排除自定义接口
                if (excludedClassSet != null && excludedClassSet.contains(interfaceClass)) {
                    continue;
                }
                returnValues.add(interfaceClass);
            }
        }
        return returnValues;
    }

    /**
     * 自定义Bean初始化回调接口
     *
     * @since 2.0.6
     */
    public interface IInitializer {

        /**
         * 初始化
         *
         * @param target 目标对象
         * @throws Exception 可能产生任何异常
         */
        void initialize(Object target) throws Exception;
    }
}
