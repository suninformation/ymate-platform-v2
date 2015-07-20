/*
 * Copyright 2007-2107 the original author or authors.
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

import net.ymate.platform.core.i18n.I18N;
import net.ymate.platform.core.lang.BlurObject;
import net.ymate.platform.core.util.DateTimeUtils;
import net.ymate.platform.validation.IValidator;
import net.ymate.platform.validation.ValidateContext;
import net.ymate.platform.validation.ValidateResult;
import net.ymate.platform.validation.annotation.Validator;
import org.apache.commons.lang.StringUtils;

/**
 * 日期类型参数验证
 *
 * @author 刘镇 (suninformation@163.com) on 2013-4-17 下午9:43:55
 * @version 1.0
 */
@Validator(VDateTime.class)
public class DateTimeValidator implements IValidator {

    public ValidateResult validate(ValidateContext context) {
        if (context.getParamValue() != null) {
            if (!context.getParamValue().getClass().isArray()) {
                String _dateStr = BlurObject.bind(context.getParamValue()).toStringValue();
                if (StringUtils.isNotBlank(_dateStr)) {
                    VDateTime _vDate = (VDateTime) context.getAnnotation();
                    try {
                        DateTimeUtils.parseDateTime(_dateStr, _vDate.pattern());
                    } catch (Exception e) {
                        return new ValidateResult(context.getParamName(), I18N.formatMessage(VALIDATION_I18N_RESOURCE, "ymp.validation.datetime_validator", "not a valid datetime string."));
                    }
                }
            }
        }
        return null;
    }
}
