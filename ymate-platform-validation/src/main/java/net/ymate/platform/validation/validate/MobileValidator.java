/*
 * Copyright 2007-2017 the original author or authors.
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
 * 目前匹配号段:
 * - 中国电信号段: 133、149、153、173、177、180、181、189、199
 * - 中国联通号段: 130、131、132、145、155、156、166、175、176、185、186
 * - 中国移动号段: 134(0-8)、135、136、137、138、139、147、150、151、152、157、158、159、178、182、183、184、187、188、198
 * - 其他号段: 14号段以前为上网卡专属号段，如中国联通的是145，中国移动的是147等等。
 * - 虚拟运营商
 * + 电信：1700、1701、1702
 * + 移动：1703、1705、1706
 * + 联通：1704、1707、1708、1709、171
 *
 * @author 刘镇 (suninformation@163.com) on 17/4/6 下午1:57
 */
@CleanProxy
public final class MobileValidator implements IValidator {

    private static final String REGEX_STR = "^1([358][0-9]|4[579]|66|7[0135678]|9[89])[0-9]{8}$";

    private static final String I18N_MESSAGE_KEY = "ymp.validation.mobile";

    private static final String I18N_MESSAGE_DEFAULT_VALUE = "{0} not a valid mobile phone number.";

    /**
     * @param mobile 手机号码
     * @return 返回mobile字符串是否为合法手机号码
     * @since 2.1.0
     */
    public static boolean validate(String mobile) {
        return StringUtils.trimToEmpty(mobile).matches(REGEX_STR);
    }

    /**
     * @param mobile 手机号码
     * @param regex  自定义正则表达式, 若为空则使用默认值
     * @return 返回mobile字符串是否为合法手机号码
     * @since 2.1.0
     */
    public static boolean validate(String mobile, String regex) {
        return StringUtils.trimToEmpty(mobile).matches(StringUtils.defaultIfBlank(regex, REGEX_STR));
    }

    @Override
    public ValidateResult validate(ValidateContext context) {
        Object paramValue = context.getParamValue();
        if (paramValue != null) {
            boolean matched = false;
            VMobile ann = (VMobile) context.getAnnotation();
            String regex = StringUtils.trimToNull(ann.regex());
            if (context.getParamValue().getClass().isArray()) {
                Object[] values = (Object[]) paramValue;
                for (Object pValue : values) {
                    matched = !validate(BlurObject.bind(pValue).toStringValue(), regex);
                    if (matched) {
                        break;
                    }
                }
            } else {
                matched = !validate(BlurObject.bind(paramValue).toStringValue(), regex);
            }
            if (matched) {
                return ValidateResult.builder(context, ann.msg(), I18N_MESSAGE_KEY, I18N_MESSAGE_DEFAULT_VALUE).matched(true).build();
            }
        }
        return null;
    }
}
