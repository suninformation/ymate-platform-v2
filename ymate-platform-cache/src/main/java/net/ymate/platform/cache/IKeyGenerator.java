/*
 * Copyright 2007-2016 the original author or authors.
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
package net.ymate.platform.cache;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * 缓存Key生成器接口
 *
 * @author 刘镇 (suninformation@163.com) on 15/11/3 下午1:40
 * @version 1.0
 */
public interface IKeyGenerator<T extends Serializable> {

    /**
     * @param method 目标方法对象
     * @param params 目标方法参数集合
     * @return 返回生成的Key值
     */
    public T generateKey(Method method, Object[] params);
}
