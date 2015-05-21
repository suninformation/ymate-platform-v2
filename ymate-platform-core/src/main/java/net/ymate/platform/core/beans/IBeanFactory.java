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
package net.ymate.platform.core.beans;

import net.ymate.platform.core.beans.proxy.IProxyFactory;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * 对象工厂接口
 *
 * @author 刘镇 (suninformation@163.com) on 15-3-5 下午1:18
 * @version 1.0
 */
public interface IBeanFactory {

    /**
     * 注册自定义注解类处理器，重复注册将覆盖前者
     *
     * @param annoClass
     * @param handler
     */
    public void registerHandler(Class<? extends Annotation> annoClass, IBeanHandler handler);

    public void registerHandler(Class<? extends Annotation> annoClass);

    /**
     * 注册扫描包路径(仅在工厂对象执行初始化前有效)
     *
     * @param packageName
     */
    public void registerPackage(String packageName);

    /**
     * 注册排除的接口类
     *
     * @param excludedClass
     */
    public void registerExcludedClass(Class<?> excludedClass);

    /**
     * @param clazz
     * @param <T>
     * @return 提取类型为clazz的对象实例，可能返回null
     */
    public <T> T getBean(Class<T> clazz);

    /**
     * @return 返回当前工厂管理的所有类对象映射
     */
    public Map<Class<?>, BeanMeta> getBeans();

    /**
     * 注册一个类到工厂
     *
     * @param clazz
     */
    public void registerBean(Class<?> clazz);

    public void registerBean(Class<?> clazz, Object object);

    public void registerBean(BeanMeta beanMeta);

    /**
     * 初始化对象工厂
     *
     * @throws Exception
     */
    public void init() throws Exception;

    /**
     * 销毁对象工厂
     *
     * @throws Exception
     */
    public void destroy() throws Exception;

    /**
     * @return 返回Parent对象工厂
     */
    public IBeanFactory getParent();

    /**
     * 设置Parent对象工厂
     *
     * @param parent
     */
    public void setParent(IBeanFactory parent);

    /**
     * @return 返回当前工厂使用的对象加载器
     */
    public IBeanLoader getLoader();

    /**
     * 设置自定义对象加载器
     *
     * @param loader
     */
    public void setLoader(IBeanLoader loader);

    /**
     * 初始化代理工厂
     *
     * @param proxyFactory
     */
    public void initProxy(IProxyFactory proxyFactory) throws Exception;

    /**
     * 初始化依赖注入
     *
     * @throws Exception
     */
    public void initIoC() throws Exception;
}
