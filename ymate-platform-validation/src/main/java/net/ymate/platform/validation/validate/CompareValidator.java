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

import net.ymate.platform.core.beans.annotation.CleanProxy;
import net.ymate.platform.validation.AbstractValidator;
import net.ymate.platform.validation.ValidateContext;
import net.ymate.platform.validation.ValidateResult;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

/**
 * 参数值比较验证
 *
 * @author 刘镇 (suninformation@163.com) on 2013-4-17 下午9:57:16
 */
@CleanProxy
public final class CompareValidator extends AbstractValidator {

    private static final String I18N_MESSAGE_NOT_EQ_KEY = "ymp.validation.compare_not_eq";

    private static final String I18N_MESSAGE_NOT_EQ_DEFAULT_VALUE = "{0} can not equal to {1}.";

    private static final String I18N_MESSAGE_EQ_KEY = "ymp.validation.compare_eq";

    private static final String I18N_MESSAGE_EQ_DEFAULT_VALUE = "{0} must be equal to {1}.";

    private static final String I18N_MESSAGE_GT_KEY = "ymp.validation.compare_gt";

    private static final String I18N_MESSAGE_GT_DEFAULT_VALUE = "{0} must be greater than {1}.";

    private static final String I18N_MESSAGE_GT_EQ_KEY = "ymp.validation.compare_gt_eq";

    private static final String I18N_MESSAGE_GT_EQ_DEFAULT_VALUE = "{0} must be greater than or equal to {1}.";

    private static final String I18N_MESSAGE_LT_KEY = "ymp.validation.compare_lt";

    private static final String I18N_MESSAGE_LT_DEFAULT_VALUE = "{0} must be less than {1}.";

    private static final String I18N_MESSAGE_LT_EQ_KEY = "ymp.validation.compare_lt_eq";

    private static final String I18N_MESSAGE_LT_EQ_DEFAULT_VALUE = "{0} must be less than or equal to {1}.";

    @Override
    public ValidateResult validate(ValidateContext context) {
        Object paramValue = context.getParamValue();
        if (paramValue != null) {
            if (paramValue.getClass().isArray()) {
                throw new UnsupportedOperationException("Array parameters are not supported by the CompareValidator.");
            }
            VCompare vCompare = (VCompare) context.getAnnotation();
            boolean matched = false;
            //
            String paramValueStr = getParamValue(paramValue, true);
            String compareValueStr = getParamValue(context.getParamValue(vCompare.with()), true);
            //
            if (StringUtils.isNumeric(paramValueStr)) {
                if (StringUtils.isNumeric(compareValueStr)) {
                    int compResult = new BigDecimal(paramValueStr).compareTo(new BigDecimal(compareValueStr));
                    switch (vCompare.cond()) {
                        case EQ:
                            matched = compResult != 0;
                            break;
                        case NOT_EQ:
                            matched = compResult == 0;
                            break;
                        case GT:
                            matched = compResult <= 0;
                            break;
                        case LT:
                            matched = compResult >= 0;
                            break;
                        case GT_EQ:
                            matched = compResult < 0;
                            break;
                        case LT_EQ:
                            matched = compResult > 0;
                            break;
                        default:
                    }
                } else {
                    matched = true;
                }
            } else {
                switch (vCompare.cond()) {
                    case EQ:
                        matched = !StringUtils.equals(paramValueStr, compareValueStr);
                        break;
                    case NOT_EQ:
                        matched = StringUtils.equals(paramValueStr, compareValueStr);
                        break;
                    default:
                        throw new UnsupportedOperationException("Non numeric type parameters only support equal or unequal operations.");
                }
            }
            if (matched) {
                String compParamName = StringUtils.defaultIfBlank(vCompare.withLabel().label(), vCompare.withLabel().name());
                if (StringUtils.isBlank(compParamName)) {
                    compParamName = vCompare.with();
                }
                compParamName = ValidateResult.formatMessage(context, compParamName, compParamName);
                //
                ValidateResult.Builder builder = ValidateResult.builder(context).matched(true);
                if (StringUtils.isNotBlank(vCompare.msg())) {
                    return builder.msg(vCompare.msg()).build();
                }
                switch (vCompare.cond()) {
                    case NOT_EQ:
                        builder.msg(I18N_MESSAGE_NOT_EQ_KEY, I18N_MESSAGE_NOT_EQ_DEFAULT_VALUE, context.getParamInfo().getSafeLabelName(), compParamName);
                        break;
                    case GT:
                        builder.msg(I18N_MESSAGE_GT_KEY, I18N_MESSAGE_GT_DEFAULT_VALUE, context.getParamInfo().getSafeLabelName(), compParamName);
                        break;
                    case LT:
                        builder.msg(I18N_MESSAGE_LT_KEY, I18N_MESSAGE_LT_DEFAULT_VALUE, context.getParamInfo().getSafeLabelName(), compParamName);
                        break;
                    case GT_EQ:
                        builder.msg(I18N_MESSAGE_GT_EQ_KEY, I18N_MESSAGE_GT_EQ_DEFAULT_VALUE, context.getParamInfo().getSafeLabelName(), compParamName);
                        break;
                    case LT_EQ:
                        builder.msg(I18N_MESSAGE_LT_EQ_KEY, I18N_MESSAGE_LT_EQ_DEFAULT_VALUE, context.getParamInfo().getSafeLabelName(), compParamName);
                        break;
                    default:
                        builder.msg(I18N_MESSAGE_EQ_KEY, I18N_MESSAGE_EQ_DEFAULT_VALUE, context.getParamInfo().getSafeLabelName(), compParamName);
                }
                return builder.build();
            }
        }
        return null;
    }
}
