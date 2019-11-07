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
package net.ymate.platform.validation;

import net.ymate.platform.commons.lang.BlurObject;
import org.apache.commons.lang3.StringUtils;

/**
 * @author 刘镇 (suninformation@163.com) on 17/1/10 上午4:35
 */
public abstract class AbstractValidator implements IValidator {

    /**
     * 获取参数值，若参数为数组则提取第0个元素值
     *
     * @param paramValue 参数值对象
     * @param trim       是否去除首尾空格
     * @return 返回参数值，若不存在则可能为null
     * @since 2.1.0
     */
    protected String getParamValue(Object paramValue, boolean trim) {
        String pValue = null;
        if (paramValue != null) {
            if (paramValue.getClass().isArray()) {
                Object[] values = (Object[]) paramValue;
                if (values.length > 0) {
                    pValue = BlurObject.bind(values[0]).toStringValue();
                }
            } else {
                pValue = BlurObject.bind(paramValue).toStringValue();
            }
            if (trim) {
                return StringUtils.trimToEmpty(pValue);
            }
        }
        return pValue;
    }
}
