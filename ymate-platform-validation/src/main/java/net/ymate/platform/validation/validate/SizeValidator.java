/*
 * Copyright 2007-present the original author or authors.
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

import net.ymate.platform.core.beans.annotation.CleanProxy;
import net.ymate.platform.validation.IValidator;
import net.ymate.platform.validation.ValidateContext;
import net.ymate.platform.validation.ValidateResult;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;

/**
 * 集合元素数量验证
 *
 * @author 刘镇 (suninformation@163.com) on 2026/3/2 15:07
 * @since 2.1.4
 */
@CleanProxy
public class SizeValidator implements IValidator {

    private static final String I18N_MESSAGE_BETWEEN_KEY = "ymp.validation.size_between";

    private static final String I18N_MESSAGE_BETWEEN_DEFAULT_VALUE = "{0} size must be between {1} and {2}.";

    private static final String I18N_MESSAGE_MAX_KEY = "ymp.validation.size_max";

    private static final String I18N_MESSAGE_MAX_DEFAULT_VALUE = "{0} size must be less than or equal to {1}.";

    private static final String I18N_MESSAGE_MIN_KEY = "ymp.validation.size_min";

    private static final String I18N_MESSAGE_MIN_DEFAULT_VALUE = "{0} size must be greater than or equal to {1}.";

    private static final String I18N_MESSAGE_EQ_KEY = "ymp.validation.size_eq";

    private static final String I18N_MESSAGE_EQ_DEFAULT_VALUE = "{0} size must be equal to {1}.";

    /**
     * 验证paramValue集合元素数量是否合法
     *
     * @param paramValue 待验证的值对象
     * @param min        最小长度值（0为不限制）
     * @param max        最大长度值（0为不限制）
     * @return 返回结果为0表示合法，为1表示数值不在min和max之间，为2表示数值小于min值，为3表示数值大于max值，为4表示长度不相等
     */
    public static int validate(Object paramValue, int min, int max) {
        int result = 0;
        if (paramValue != null) {
            int size = 0;
            if (paramValue instanceof Collection) {
                size = ((Collection<?>) paramValue).size();
            } else if (paramValue.getClass().isArray()) {
                size = ((Object[]) paramValue).length;
            }
            if (min > 0 && min == max && size != min) {
                result = 4;
            } else {
                boolean cond = min > 0 && max > 0 && (size < min || size > max);
                if (cond) {
                    result = 1;
                } else if (min > 0 && size < min) {
                    result = 2;
                } else if (max > 0 && size > max) {
                    result = 3;
                }
            }
        }
        return result;
    }

    @Override
    public ValidateResult validate(ValidateContext context) {
        Object paramValue = context.getParamValue();
        if (paramValue != null) {
            VSize vSize = (VSize) context.getAnnotation();
            int result = validate(paramValue, vSize.eq() > 0 ? vSize.eq() : vSize.min(), vSize.eq() > 0 ? vSize.eq() : vSize.max());
            if (result > 0) {
                ValidateResult.Builder builder = ValidateResult.builder(context).matched(true);
                if (StringUtils.isNotBlank(vSize.msg())) {
                    return builder.msg(vSize.msg()).build();
                }
                switch (result) {
                    case 1:
                        builder.msg(I18N_MESSAGE_BETWEEN_KEY, I18N_MESSAGE_BETWEEN_DEFAULT_VALUE, vSize.min(), vSize.max());
                        break;
                    case 2:
                        builder.msg(I18N_MESSAGE_MIN_KEY, I18N_MESSAGE_MIN_DEFAULT_VALUE, vSize.min());
                        break;
                    case 3:
                        builder.msg(I18N_MESSAGE_MAX_KEY, I18N_MESSAGE_MAX_DEFAULT_VALUE, vSize.max());
                        break;
                    default:
                        builder.msg(I18N_MESSAGE_EQ_KEY, I18N_MESSAGE_EQ_DEFAULT_VALUE, vSize.eq());
                }
                return builder.build();
            }
        }
        return null;
    }
}
