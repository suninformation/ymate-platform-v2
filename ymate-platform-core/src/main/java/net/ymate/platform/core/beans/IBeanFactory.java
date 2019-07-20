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

import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.beans.proxy.IProxyFactory;
import net.ymate.platform.core.support.IDestroyable;
import net.ymate.platform.core.support.IInitialization;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * 对象工厂接口
 *
 * @author 刘镇 (suninformation@163.com) on 15-3-5 下午1:18
 */
public interface IBeanFactory extends IInitialization<IApplication>, IDestroyable {

    /**
     * 获取类型为clazz的对象实例，可能返回null
     *
     * @param clazz 目标类型
     * @param <T>   类型
     * @return 返回对象实例
     */
    <T> T getBean(Class<T> clazz);

    /**
     * 获取当前工厂管理的所有类对象映射
     *
     * @return 返回当前工厂管理的所有类对象映射
     */
    Map<Class<?>, BeanMeta> getBeans();

    /**
     * 注册一个类到工厂
     *
     * @param clazz 预注册类型
     */
    void registerBean(Class<?> clazz);

    /**
     * 注册一个类定义到工厂
     *
     * @param beanMeta 预注册类描述对象
     */
    void registerBean(BeanMeta beanMeta);

    /**
     * 注册自定义依赖注入注解的逻辑处理器
     *
     * @param annClass 目标注解类型
     * @param injector 目标依赖注入注解逻辑处理器
     */
    void registerInjector(Class<? extends Annotation> annClass, IBeanInjector injector);

    /**
     * 注册排除的接口类
     *
     * @param excludedInterfaceClass 预排除接口类型
     */
    void registerExcludedInterfaceClass(Class<?> excludedInterfaceClass);

    /**
     * 判断是否为排除的接口类
     *
     * @param excludedInterfaceClass 目标接口类型
     * @return 若目标接口被排除则返回true
     */
    boolean isExcludedInterfaceClass(Class<?> excludedInterfaceClass);

    /**
     * 获取所属应用容器管理器
     *
     * @return 返回所属应用容器管理器
     */
    IApplication getOwner();

    /**
     * 获取父对象工厂
     *
     * @return 返回父对象工厂
     */
    IBeanFactory getParent();

    /**
     * 获取代理工厂
     *
     * @return 返回代理工厂对象
     */
    IProxyFactory getProxyFactory();
}
