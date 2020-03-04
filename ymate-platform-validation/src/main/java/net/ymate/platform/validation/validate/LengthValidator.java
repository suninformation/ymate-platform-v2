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
 * 字符串长度验证
 *
 * @author 刘镇 (suninformation@163.com) on 2013-4-17 下午5:17:22
 */
@CleanProxy
public final class LengthValidator implements IValidator {

    private static final String I18N_MESSAGE_BETWEEN_KEY = "ymp.validation.length_between";

    private static final String I18N_MESSAGE_BETWEEN_DEFAULT_VALUE = "{0} length must be between {1} and {2}.";

    private static final String I18N_MESSAGE_MAX_KEY = "ymp.validation.length_max";

    private static final String I18N_MESSAGE_MAX_DEFAULT_VALUE = "{0} length must be less than or equal to {1}.";

    private static final String I18N_MESSAGE_MIN_KEY = "ymp.validation.length_min";

    private static final String I18N_MESSAGE_MIN_DEFAULT_VALUE = "{0} length must be greater than or equal to {1}.";

    private static final String I18N_MESSAGE_EQ_KEY = "ymp.validation.length_eq";

    private static final String I18N_MESSAGE_EQ_DEFAULT_VALUE = "{0} length must be equal to {1}.";

    /**
     * 验证paramValue字符长度是否合法
     *
     * @param paramValue 待验证的值对象
     * @param min        最小长度值（0为不限制）
     * @param max        最大长度值（0为不限制）
     * @return 返回结果为0表示合法，为1表示数值不在min和max之间，为2表示数值小于min值，为3表示数值大于max值，为4表示长度不相等
     * @since 2.1.0
     */
    public static int validate(Object paramValue, int min, int max) {
        int result = 0;
        int length = StringUtils.length(BlurObject.bind(paramValue).toStringValue());
        if (min > 0 && min == max && length != min) {
            result = 4;
        } else {
            boolean cond = min > 0 && max > 0 && (length < min || length > max);
            if (cond) {
                result = 1;
            } else if (min > 0 && length < min) {
                result = 2;
            } else if (max > 0 && length > max) {
                result = 3;
            }
        }
        return result;
    }

    @Override
    public ValidateResult validate(ValidateContext context) {
        Object paramValue = context.getParamValue();
        if (paramValue != null) {
            VLength vLength = (VLength) context.getAnnotation();
            int result = 0;
            if (paramValue.getClass().isArray()) {
                Object[] values = (Object[]) paramValue;
                for (Object pValue : values) {
                    result = validate(pValue, vLength.eq() > 0 ? vLength.eq() : vLength.min(), vLength.eq() > 0 ? vLength.eq() : vLength.max());
                    if (result > 0) {
                        break;
                    }
                }
            } else {
                result = validate(paramValue, vLength.eq() > 0 ? vLength.eq() : vLength.min(), vLength.eq() > 0 ? vLength.eq() : vLength.max());
            }
            if (result > 0) {
                ValidateResult.Builder builder = ValidateResult.builder(context).matched(true);
                if (StringUtils.isNotBlank(vLength.msg())) {
                    return builder.msg(vLength.msg()).build();
                }
                switch (result) {
                    case 1:
                        builder.msg(I18N_MESSAGE_BETWEEN_KEY, I18N_MESSAGE_BETWEEN_DEFAULT_VALUE, vLength.min(), vLength.max());
                        break;
                    case 2:
                        builder.msg(I18N_MESSAGE_MIN_KEY, I18N_MESSAGE_MIN_DEFAULT_VALUE, vLength.min());
                        break;
                    case 3:
                        builder.msg(I18N_MESSAGE_MAX_KEY, I18N_MESSAGE_MAX_DEFAULT_VALUE, vLength.max());
                        break;
                    default:
                        builder.msg(I18N_MESSAGE_EQ_KEY, I18N_MESSAGE_EQ_DEFAULT_VALUE, vLength.eq());
                }
                return builder.build();
            }
        }
        return null;
    }
}
