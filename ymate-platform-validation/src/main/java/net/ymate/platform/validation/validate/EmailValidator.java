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
 * 邮箱地址格式验证
 *
 * @author 刘镇 (suninformation@163.com) on 2013-4-17 下午2:03:56
 */
@CleanProxy
public final class EmailValidator implements IValidator {

    private static final String REGEX_STR = "(?:\\w[-._\\w]*\\w@\\w[-._\\w]*\\w\\.\\w{2,8}$)";

    private static final String I18N_MESSAGE_KEY = "ymp.validation.email";

    private static final String I18N_MESSAGE_DEFAULT_VALUE = "{0} not a valid email address.";

    /**
     * @param email 邮件地址
     * @return 返回email字符串是否为合法邮件地址
     * @since 2.1.0
     */
    public static boolean validate(String email) {
        return StringUtils.trimToEmpty(email).matches(REGEX_STR);
    }

    @Override
    public ValidateResult validate(ValidateContext context) {
        Object paramValue = context.getParamValue();
        if (paramValue != null) {
            boolean matched = false;
            if (context.getParamValue().getClass().isArray()) {
                Object[] values = (Object[]) paramValue;
                for (Object pValue : values) {
                    matched = !validate(BlurObject.bind(pValue).toStringValue());
                    if (matched) {
                        break;
                    }
                }
            } else {
                matched = !validate(BlurObject.bind(paramValue).toStringValue());
            }
            if (matched) {
                ValidateResult.Builder builder = ValidateResult.builder(context).matched(true);
                VEmail ann = (VEmail) context.getAnnotation();
                if (StringUtils.isNotBlank(ann.msg())) {
                    return builder.msg(ann.msg()).build();
                }
                return builder.msg(I18N_MESSAGE_KEY, I18N_MESSAGE_DEFAULT_VALUE, builder.name()).build();
            }
        }
        return null;
    }
}
