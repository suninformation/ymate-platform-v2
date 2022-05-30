/*
 * Copyright 2007-2022 the original author or authors.
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

import java.lang.reflect.Method;

/**
 * 方法参数处理器接口
 *
 * @author 刘镇 (suninformation@163.com) on 2022/5/30 11:14
 * @since 2.1.2
 */
public interface IProxyMethodParamHandler {

    /**
     * @param targetObject 目标类
     * @param targetMethod 目标方法
     * @param methodParams 方法参数集合
     * @return 返回处理后的方法参数集合
     * @throws Throwable 可能产生的异常
     */
    Object[] handle(Object targetObject, Method targetMethod, Object[] methodParams) throws Throwable;
}
