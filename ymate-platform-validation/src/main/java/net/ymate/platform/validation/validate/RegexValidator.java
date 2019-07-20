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
import net.ymate.platform.core.beans.annotation.CleanProxy;
import net.ymate.platform.validation.IValidator;
import net.ymate.platform.validation.ValidateContext;
import net.ymate.platform.validation.ValidateResult;
import org.apache.commons.lang3.StringUtils;

/**
 * 正则表达式验证
 *
 * @author 刘镇 (suninformation@163.com) on 2013-4-14 上午1:20:13
 */
@CleanProxy
public final class RegexValidator implements IValidator {

    private static final String I18N_MESSAGE_KEY = "ymp.validation.regex";

    private static final String I18N_MESSAGE_DEFAULT_VALUE = "{0} regex not match.";

    /**
     * 验证paramValue是否匹配regex正则
     *
     * @param regex      正则表达式
     * @param paramValue 待验证值对象
     * @return 若匹配则返回true
     * @since 2.1.0
     */
    public static boolean validate(String regex, Object paramValue) {
        boolean result = false;
        if (paramValue != null) {
            if (paramValue.getClass().isArray()) {
                Object[] values = (Object[]) paramValue;
                for (Object pValue : values) {
                    result = !StringUtils.trimToEmpty(BlurObject.bind(pValue).toStringValue()).matches(regex);
                    if (result) {
                        break;
                    }
                }
            } else {
                result = !StringUtils.trimToEmpty(BlurObject.bind(paramValue).toStringValue()).matches(regex);
            }
        }
        return result;
    }

    @Override
    public ValidateResult validate(ValidateContext context) {
        Object paramValue = context.getParamValue();
        if (paramValue != null) {
            VRegex ann = (VRegex) context.getAnnotation();
            boolean matched = validate(ann.regex(), paramValue);
            if (matched) {
                ValidateResult.Builder builder = ValidateResult.builder(context).matched(true);
                if (StringUtils.isNotBlank(ann.msg())) {
                    return builder.msg(ann.msg()).build();
                }
                return builder.msg(I18N_MESSAGE_KEY, I18N_MESSAGE_DEFAULT_VALUE, builder.name()).build();
            }
        }
        return null;
    }
}
