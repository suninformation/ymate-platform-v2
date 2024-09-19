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
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * 必填项验证
 *
 * @author 刘镇 (suninformation@163.com) on 2013-4-13 下午6:06:29
 */
@CleanProxy
public final class RequiredValidator implements IValidator {

    private static final String I18N_MESSAGE_KEY = "ymp.validation.required";

    private static final String I18N_MESSAGE_DEFAULT_VALUE = "{0} is required.";

    /**
     * 验证paramValue值是否不空
     *
     * @param paramValue 待验证值对象
     * @return 若为空则返回true
     * @since 2.1.0
     */
    public static boolean validate(Object paramValue) {
        boolean result;
        if (paramValue == null) {
            result = true;
        } else if (paramValue.getClass().isArray()) {
            result = ArrayUtils.isEmpty((Object[]) paramValue) || Arrays.stream(((Object[]) paramValue)).anyMatch(o -> StringUtils.isBlank(BlurObject.bind(o).toStringValue()));
        } else {
            result = StringUtils.isBlank(BlurObject.bind(paramValue).toStringValue());
        }
        return result;
    }

    @Override
    public ValidateResult validate(ValidateContext context) {
        boolean matched = validate(context.getParamValue());
        if (matched) {
            VRequired ann = (VRequired) context.getAnnotation();
            return ValidateResult.builder(context, ann.msg(), I18N_MESSAGE_KEY, I18N_MESSAGE_DEFAULT_VALUE).matched(true).build();
        }
        return null;
    }
}
