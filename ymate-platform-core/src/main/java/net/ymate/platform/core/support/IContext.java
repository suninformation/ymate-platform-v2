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
package net.ymate.platform.core.support;

import net.ymate.platform.commons.lang.BlurObject;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.beans.annotation.Ignored;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 环境上下文接口
 *
 * @author 刘镇 (suninformation@163.com) on 2018/8/30 上午11:44
 * @since 2.0.6
 */
@Ignored
public interface IContext {

    /**
     * 获取所属应用管理器
     *
     * @return 返回所属应用管理器实例
     */
    IApplication getOwner();

    /**
     * 获取上下文参数映射
     *
     * @return 返回上下文参数映射
     */
    Map<String, String> getContextParams();

    /**
     * 查找上下文参数并返回参数值，若上下文中不存在则尝试从全局参数中查找
     *
     * @param paramName    参数名
     * @param defaultValue 默认值
     * @return 返回参数值
     * @since 2.1.3
     */
    default String findContextParamValue(String paramName, String defaultValue) {
        String paramValue = getContextParams().get(paramName);
        if (StringUtils.isBlank(paramValue)) {
            paramValue = getOwner().getParam(paramName, defaultValue);
        }
        return paramValue;
    }

    default BlurObject findContextParamValueAsBlur(String paramName, String defaultValue) {
        return BlurObject.bind(findContextParamValue(paramName, defaultValue));
    }

    default String[] findContextParamValueAsArray(String paramName, String defaultValue) {
        return StringUtils.split(findContextParamValue(paramName, defaultValue), "|");
    }

    @SuppressWarnings("unchecked")
    default <T> Class<? extends T>[] findContextParamValueAsClasses(String paramName, Class<T> clazz, Class<? extends T>[] defaultValue) {
        String[] values = findContextParamValueAsArray(paramName, null);
        if (ArrayUtils.isNotEmpty(values)) {
            List<Class<? extends T>> classes = new ArrayList<>();
            for (String value : values) {
                Class<?> cl = ClassUtils.loadClassOrNull(value, getClass());
                if (ClassUtils.isNormalClass(cl) && !cl.isInterface() && (ClassUtils.isInterfaceOf(cl, clazz) || ClassUtils.isSubclassOf(cl, clazz))) {
                    classes.add((Class<T>) cl);
                }
            }
            if (!classes.isEmpty()) {
                return classes.toArray(new Class[0]);
            }
        }
        return defaultValue;
    }
}
