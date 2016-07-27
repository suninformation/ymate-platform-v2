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
package net.ymate.platform.core.beans.intercept;

import net.ymate.platform.core.YMP;
import net.ymate.platform.core.beans.annotation.*;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Method;
import java.util.*;

/**
 * @author 刘镇 (suninformation@163.com) on 16/1/9 上午12:18
 * @version 1.0
 */
public class InterceptAnnoHelper {

    public static List<Class<? extends IInterceptor>> getBeforeIntercepts(Class<?> targetClass, Method targetMethod) {
        List<Class<? extends IInterceptor>> _classes = new ArrayList<Class<? extends IInterceptor>>();
        if (targetClass.isAnnotationPresent(Before.class)) {
            Before _before = targetClass.getAnnotation(Before.class);
            Clean _clean = getCleanIntercepts(targetMethod);
            //
            if (_clean != null &&
                    (_clean.type().equals(IInterceptor.CleanType.ALL) || _clean.type().equals(IInterceptor.CleanType.BEFORE))) {
                if (_clean.value().length > 0) {
                    for (Class<? extends IInterceptor> _clazz : _before.value()) {
                        if (ArrayUtils.contains(_clean.value(), _clazz)) {
                            continue;
                        }
                        _classes.add(_clazz);
                    }
                }
            } else {
                Collections.addAll(_classes, _before.value());
            }
        }
        //
        if (targetMethod.isAnnotationPresent(Before.class)) {
            Collections.addAll(_classes, targetMethod.getAnnotation(Before.class).value());
        }
        //
        return _classes;
    }

    public static List<Class<? extends IInterceptor>> getAfterIntercepts(Class<?> targetClass, Method targetMethod) {
        List<Class<? extends IInterceptor>> _classes = new ArrayList<Class<? extends IInterceptor>>();
        if (targetClass.isAnnotationPresent(After.class)) {
            After _after = targetClass.getAnnotation(After.class);
            Clean _clean = getCleanIntercepts(targetMethod);
            //
            if (_clean != null &&
                    (_clean.type().equals(IInterceptor.CleanType.ALL) || _clean.type().equals(IInterceptor.CleanType.AFTER))) {
                if (_clean.value().length > 0) {
                    for (Class<? extends IInterceptor> _clazz : _after.value()) {
                        if (ArrayUtils.contains(_clean.value(), _clazz)) {
                            continue;
                        }
                        _classes.add(_clazz);
                    }
                }
            } else {
                Collections.addAll(_classes, _after.value());
            }
        }
        //
        if (targetMethod.isAnnotationPresent(After.class)) {
            Collections.addAll(_classes, targetMethod.getAnnotation(After.class).value());
        }
        //
        return _classes;
    }

    public static Clean getCleanIntercepts(Method targetMethod) {
        if (targetMethod.isAnnotationPresent(Clean.class)) {
            return targetMethod.getAnnotation(Clean.class);
        }
        return null;
    }

    private static void __doParseContextParamValue(YMP owner, ContextParam contextParam, Map<String, String> paramsMap) {
        if (contextParam != null) {
            for (ParamItem _item : contextParam.value()) {
                String _key = _item.key();
                String _value = _item.value();
                boolean _flag = _value.length() > 1 && _value.charAt(0) == '$';
                if (StringUtils.isBlank(_key)) {
                    if (_flag) {
                        _key = _value.substring(1);
                        _value = StringUtils.trimToEmpty(owner.getConfig().getParam(_key));
                    } else {
                        _key = _value;
                    }
                } else if (_flag) {
                    _value = StringUtils.trimToEmpty(owner.getConfig().getParam(_value.substring(1)));
                }
                paramsMap.put(_key, _value);
            }
        }
    }

    public static Map<String, String> getContextParams(YMP owner, Class<?> targetClass, Method targetMethod) {
        Map<String, String> _contextParams = new HashMap<String, String>();
        //
        if (targetClass != null) {
            __doParseContextParamValue(owner, targetClass.getAnnotation(ContextParam.class), _contextParams);
        }
        if (targetMethod != null) {
            __doParseContextParamValue(owner, targetMethod.getAnnotation(ContextParam.class), _contextParams);
        }
        //
        return _contextParams;
    }
}
