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
package net.ymate.platform.core.beans.proxy;

import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.beans.annotation.Ignored;
import net.ymate.platform.core.support.IDestroyable;
import net.ymate.platform.core.support.IInitialization;

import java.util.Collection;
import java.util.List;

/**
 * 代理工厂接口定义
 *
 * @author 刘镇 (suninformation@163.com) on 15-3-3 下午4:38
 */
@Ignored
public interface IProxyFactory extends IInitialization<IApplication>, IDestroyable {

    /**
     * 获取代理工厂所属应用容器管理器
     *
     * @return 返回代理工厂所属应用容器管理器
     */
    IApplication getOwner();

    /**
     * 注册代理
     *
     * @param proxy 代理类对象
     * @return 返回当前代理工厂对象实例
     */
    IProxyFactory registerProxy(IProxy proxy);

    /**
     * 注册代理
     *
     * @param proxies 代理类对象集合
     * @return 返回当前代理工厂对象实例
     */
    IProxyFactory registerProxy(Collection<? extends IProxy> proxies);

    /**
     * 获取当前工厂已注册的代理类对象集合
     *
     * @return 返回代理类对象集合
     */
    List<IProxy> getProxies();

    /**
     * 获取当前工厂已注册的代理类对象集合
     *
     * @param filter 代理过滤器
     * @return 返回代理类对象集合
     */
    List<IProxy> getProxies(IProxyFilter filter);

    /**
     * 通过代理工厂已注册的代理创建代理对象
     *
     * @param targetClass 目标类
     * @param <T>         目标类型
     * @return 返回创建的代理对象
     */
    <T> T createProxy(Class<?> targetClass);

    /**
     * 通过自定义代理集合创建代理对象
     *
     * @param targetClass 目标类
     * @param proxies     代理集合
     * @param <T>         目标类型
     * @return 返回创建的代理对象
     */
    <T> T createProxy(Class<?> targetClass, List<IProxy> proxies);

    /**
     * 创建方法参数代理对象
     *
     * @param targetClass        目标类
     * @param methodParamHandler 方法参数处理器
     * @param <T>                目标类型
     * @return 返回创建的方法参数代理对象
     * @since 2.1.2
     */
    <T> T createProxy(Class<?> targetClass, IProxyMethodParamHandler methodParamHandler);
}
