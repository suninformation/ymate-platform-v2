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
package net.ymate.platform.cache.impl;

import net.ymate.platform.cache.IKeyGenerator;
import net.ymate.platform.core.lang.BlurObject;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/3 下午1:43
 * @version 1.0
 */
public class DefaultKeyGenerator implements IKeyGenerator {

    public Serializable generateKey(Method method, Object[] params) {
        // [className:methodName:{paramTypeName1:paramValue1,...,paramTypeNameN:paramValueN,}]
        StringBuilder __keyGenBuilder = new StringBuilder();
        __keyGenBuilder.append("[").append(method.getDeclaringClass().getName())
                .append(":").append(method.getName()).append("{");
        Class<?>[] _paramTypes = method.getParameterTypes();
        for (int _idx = 0; _idx < _paramTypes.length; _idx++) {
            __keyGenBuilder.append(_paramTypes[_idx].getName()).append(":");
            if (_paramTypes[_idx].isArray()) {
                __keyGenBuilder.append("[");
                Object[] _arrValues = (Object[]) params[_idx];
                for (Object _value : _arrValues) {
                    __keyGenBuilder.append(BlurObject.bind(_value).toStringValue()).append(",");
                }
                __keyGenBuilder.append("]");
            } else {
                __keyGenBuilder.append(BlurObject.bind(params[_idx]).toStringValue()).append(",");
            }
        }
        __keyGenBuilder.append("}]");
        return DigestUtils.md5Hex(__keyGenBuilder.toString());
    }
}
