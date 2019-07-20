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
package net.ymate.platform.webmvc.validate;

import net.ymate.platform.commons.lang.BlurObject;
import net.ymate.platform.core.beans.annotation.CleanProxy;
import net.ymate.platform.validation.AbstractValidator;
import net.ymate.platform.validation.ValidateContext;
import net.ymate.platform.validation.ValidateResult;
import net.ymate.platform.webmvc.context.WebContext;
import net.ymate.platform.webmvc.util.TokenProcessHelper;
import org.apache.commons.lang.StringUtils;

/**
 * @author 刘镇 (suninformation@163.com) on 16/11/27 下午12:17
 */
@CleanProxy
public class TokenValidator extends AbstractValidator {

    private static final String I18N_MESSAGE_KEY = "ymp.validation.token_invalid";

    private static final String I18N_MESSAGE_DEFAULT_VALUE = "{0} invalid.";

    public static boolean validate(String tokenName, String tokenValue, boolean reset) {
        return !TokenProcessHelper.getInstance().isTokenValid(WebContext.getRequest(), tokenName, tokenValue, reset);
    }

    @Override
    public ValidateResult validate(ValidateContext context) {
        boolean matched = false;
        VToken tokenAnn = (VToken) context.getAnnotation();
        if (context.getParamValue() != null) {
            String tokenValue;
            if (context.getParamValue().getClass().isArray()) {
                tokenValue = getParamValue(context.getParamValue(), true);
            } else {
                tokenValue = BlurObject.bind(context.getParamValue()).toStringValue();
            }
            matched = validate(tokenAnn.name(), tokenValue, tokenAnn.reset());
        }
        if (matched) {
            ValidateResult.Builder builder = ValidateResult.builder(context).matched(true);
            if (StringUtils.isNotBlank(tokenAnn.msg())) {
                return builder.msg(tokenAnn.msg()).build();
            } else {
                return builder.msg(I18N_MESSAGE_KEY, I18N_MESSAGE_DEFAULT_VALUE, builder.name()).build();
            }
        }
        return null;
    }
}
