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
package net.ymate.platform.commons;

import net.ymate.platform.commons.annotation.ExportColumn;
import net.ymate.platform.commons.lang.BlurObject;
import net.ymate.platform.commons.util.ClassUtils;

/**
 * @author 刘镇 (suninformation@163.com) on 2017/12/25 下午2:42
 */
public interface IExportDataRender {

    /**
     * 渲染列
     * <p>
     * 使用默认实现是为了保持兼容性
     *
     * @param column    列注解
     * @param fieldName 字段名称
     * @param value     值对象
     * @return 返回字段值字符串
     * @throws Exception 可能产生的任何异常
     * @see IExportDataRender#render(ClassUtils.BeanWrapper, ExportColumn, String, Object, boolean)
     */
    @Deprecated
    default String render(ExportColumn column, String fieldName, Object value) throws Exception {
        return BlurObject.bind(value).toStringValue();
    }

    /**
     * 渲染列
     * <p>
     * 使用默认实现是为了保持兼容性
     *
     * @param beanWrapper 目标对象包裹器
     * @param column      列注解
     * @param fieldName   字段名称
     * @param value       值对象
     * @param importing   当前为导入操作
     * @return 返回字段值对象
     * @throws Exception 可能产生的任何异常
     * @since 2.1.3
     */
    default Object render(ClassUtils.BeanWrapper<?> beanWrapper, ExportColumn column, String fieldName, Object value, boolean importing) throws Exception {
        return render(column, fieldName, value);
    }
}
