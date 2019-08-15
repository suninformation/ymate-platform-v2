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

import net.ymate.platform.core.beans.annotation.Ignored;

import java.lang.reflect.Method;

/**
 * 代理链接口定义
 *
 * @author 刘镇 (suninformation@163.com) on 15-3-3 下午4:05
 */
@Ignored
public interface IProxyChain {

    /**
     * 获取所属代理工厂
     *
     * @return 返回代理工厂对象
     */
    IProxyFactory getProxyFactory();

    /**
     * 获取方法参数集合
     *
     * @return 返回参数集合
     */
    Object[] getMethodParams();

    /**
     * 获取被代理目标类型
     *
     * @return 返回目标类型
     */
    Class<?> getTargetClass();

    /**
     * 获取代理目标对象
     *
     * @return 获取目标实例对象
     */
    Object getTargetObject();

    /**
     * 获取被代理目标方法对象
     *
     * @return 获取目标方法对象
     */
    Method getTargetMethod();

    /**
     * 执行代理链
     *
     * @return 返回执行结果
     * @throws Throwable 执行过程中可能产生的异常
     */
    Object doProxyChain() throws Throwable;
}
