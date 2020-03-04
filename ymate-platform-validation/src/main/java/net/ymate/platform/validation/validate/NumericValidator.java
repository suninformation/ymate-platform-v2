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

import net.ymate.platform.commons.MathCalcHelper;
import net.ymate.platform.commons.lang.BlurObject;
import net.ymate.platform.core.beans.annotation.CleanProxy;
import net.ymate.platform.validation.IValidator;
import net.ymate.platform.validation.ValidateContext;
import net.ymate.platform.validation.ValidateResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * 数值类型参数验证
 *
 * @author 刘镇 (suninformation@163.com) on 2013-4-17 下午8:36:16
 */
@CleanProxy
public final class NumericValidator implements IValidator {

    private static final String I18N_MESSAGE_KEY = "ymp.validation.numeric";

    private static final String I18N_MESSAGE_DEFAULT_VALUE = "{0} not a valid numeric.";

    private static final String I18N_MESSAGE_BETWEEN_KEY = "ymp.validation.numeric_between";

    private static final String I18N_MESSAGE_BETWEEN_DEFAULT_VALUE = "{0} numeric must be between {1} and {2}.";

    private static final String I18N_MESSAGE_DECIMALS_KEY = "ymp.validation.numeric_decimals";

    private static final String I18N_MESSAGE_DECIMALS_DEFAULT_VALUE = "{0} numeric must be keep {1} decimals.";

    private static final String I18N_MESSAGE_MAX_KEY = "ymp.validation.numeric_max";

    private static final String I18N_MESSAGE_MAX_DEFAULT_VALUE = "{0} numeric must be less than or equal to {1}.";

    private static final String I18N_MESSAGE_MIN_KEY = "ymp.validation.numeric_min";

    private static final String I18N_MESSAGE_MIN_DEFAULT_VALUE = "{0} numeric must be greater than or equal to {1}.";

    private static final String I18N_MESSAGE_EQ_KEY = "ymp.validation.numeric_eq";

    private static final String I18N_MESSAGE_EQ_DEFAULT_VALUE = "{0} numeric must be equal to {1}.";

    /**
     * 验证paramValue是否为合法数值
     *
     * @param paramValue 待验证的值对象
     * @param min        最小数值（0为不限制）
     * @param max        最大数值（0为不限制）
     * @param decimals   小数位数（0为不限制）
     * @return 返回结果为0表示合法，为1表示不是有效的数值，为2表示小数位数超出范围，为3表示数值不在min和max之间，为4表示数值小于min值，为5表示数值大于max值，为6表示数值不相等
     * @since 2.1.0
     */
    public static int validate(Object paramValue, double min, double max, int decimals) {
        int result = 0;
        try {
            String numStr = BlurObject.bind(paramValue).toStringValue();
            Number number = NumberUtils.createNumber(numStr);
            if (number == null) {
                result = 1;
            } else {
                if (min > 0 && MathCalcHelper.eq(min, max) && number.doubleValue() != min) {
                    result = 6;
                } else {
                    boolean cond = min > 0 && max > 0 && (number.doubleValue() < min || number.doubleValue() > max);
                    if (cond) {
                        result = 3;
                    } else if (min > 0 && number.doubleValue() < min) {
                        result = 4;
                    } else if (max > 0 && number.doubleValue() > max) {
                        result = 5;
                    }
                }
                if (result == 0 && decimals > 0 && StringUtils.substringAfter(numStr, ".").length() > decimals) {
                    result = 2;
                }
            }
        } catch (NumberFormatException e) {
            result = 1;
        }
        return result;
    }

    @Override
    public ValidateResult validate(ValidateContext context) {
        Object paramValue = context.getParamValue();
        if (paramValue != null) {
            VNumeric vNumeric = (VNumeric) context.getAnnotation();
            int result = 0;
            if (paramValue.getClass().isArray()) {
                Object[] values = (Object[]) paramValue;
                for (Object pValue : values) {
                    result = validate(pValue, vNumeric.eq() > 0 ? vNumeric.eq() : vNumeric.min(), vNumeric.eq() > 0 ? vNumeric.eq() : vNumeric.max(), vNumeric.decimals());
                    if (result > 0) {
                        break;
                    }
                }
            } else {
                result = validate(paramValue, vNumeric.eq() > 0 ? vNumeric.eq() : vNumeric.min(), vNumeric.eq() > 0 ? vNumeric.eq() : vNumeric.max(), vNumeric.decimals());
            }
            if (result > 0) {
                ValidateResult.Builder builder = ValidateResult.builder(context).matched(true);
                if (StringUtils.isNotBlank(vNumeric.msg())) {
                    return builder.msg(vNumeric.msg()).build();
                }
                switch (result) {
                    case 2:
                        builder.msg(I18N_MESSAGE_DECIMALS_KEY, I18N_MESSAGE_DECIMALS_DEFAULT_VALUE, vNumeric.decimals());
                        break;
                    case 3:
                        builder.msg(I18N_MESSAGE_BETWEEN_KEY, I18N_MESSAGE_BETWEEN_DEFAULT_VALUE, vNumeric.min(), vNumeric.max());
                        break;
                    case 4:
                        builder.msg(I18N_MESSAGE_MIN_KEY, I18N_MESSAGE_MIN_DEFAULT_VALUE, vNumeric.min());
                        break;
                    case 5:
                        builder.msg(I18N_MESSAGE_MAX_KEY, I18N_MESSAGE_MAX_DEFAULT_VALUE, vNumeric.max());
                        break;
                    case 6:
                        builder.msg(I18N_MESSAGE_EQ_KEY, I18N_MESSAGE_EQ_DEFAULT_VALUE, vNumeric.eq());
                        break;
                    default:
                        builder.msg(I18N_MESSAGE_KEY, I18N_MESSAGE_DEFAULT_VALUE);
                }
                return builder.build();
            }
        }
        return null;
    }
}
