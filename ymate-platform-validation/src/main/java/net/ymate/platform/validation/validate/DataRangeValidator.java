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
package net.ymate.platform.validation.validate;

import net.ymate.platform.commons.lang.BlurObject;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.core.beans.annotation.CleanProxy;
import net.ymate.platform.validation.IValidator;
import net.ymate.platform.validation.ValidateContext;
import net.ymate.platform.validation.ValidateResult;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/09/05 下午 17:11
 * @since 2.0.6
 */
@CleanProxy
public final class DataRangeValidator implements IValidator {

    private static final String I18N_MESSAGE_KEY = "ymp.validation.data_range_invalid";

    private static final String I18N_MESSAGE_DEFAULT_VALUE = "{0} invalid.";

    /**
     * 判断paramValue是否被collection包含
     *
     * @param collection 取值范围集合
     * @param paramValue 待验证的值对象
     * @param ignoreCase 是否忽略大小写
     * @return 若paramValue包含于collection集合中则返回true
     * @since 2.1.0
     */
    public static boolean validate(Collection<String> collection, Object paramValue, boolean ignoreCase) {
        boolean contained = false;
        if (paramValue.getClass().isArray()) {
            Object[] values = (Object[]) paramValue;
            for (Object pValue : values) {
                String pValueStr = BlurObject.bind(pValue).toStringValue();
                if (ignoreCase) {
                    for (String value : collection) {
                        if (StringUtils.equalsIgnoreCase(pValueStr, value)) {
                            contained = true;
                            break;
                        }
                    }
                } else {
                    contained = collection.contains(pValueStr);
                }
            }
        } else {
            contained = collection.contains(BlurObject.bind(paramValue).toStringValue());
        }
        return contained;
    }

    @Override
    public ValidateResult validate(ValidateContext context) {
        Object paramValue = context.getParamValue();
        if (paramValue != null) {
            boolean matched = false;
            VDataRange vDataRange = (VDataRange) context.getAnnotation();
            if (!vDataRange.providerClass().equals(IDataRangeValuesProvider.class)) {
                IDataRangeValuesProvider provider = context.getOwner().getBeanFactory().getBean(vDataRange.providerClass());
                if (provider == null) {
                    provider = ClassUtils.impl(vDataRange.providerClass(), IDataRangeValuesProvider.class);
                }
                if (provider != null) {
                    matched = !validate(provider.values(), paramValue, vDataRange.ignoreCase());
                } else {
                    matched = !validate(Arrays.asList(vDataRange.value()), paramValue, vDataRange.ignoreCase());
                }
            }
            if (matched) {
                return ValidateResult.builder(context, vDataRange.msg(), I18N_MESSAGE_KEY, I18N_MESSAGE_DEFAULT_VALUE).matched(true).build();
            }
        }
        return null;
    }
}