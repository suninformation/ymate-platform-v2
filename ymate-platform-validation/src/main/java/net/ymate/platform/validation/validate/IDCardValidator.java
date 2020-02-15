/*
 * Copyright 2007-2020 the original author or authors.
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

import net.ymate.platform.commons.DateTimeHelper;
import net.ymate.platform.commons.lang.BlurObject;
import net.ymate.platform.core.beans.annotation.CleanProxy;
import net.ymate.platform.validation.IValidator;
import net.ymate.platform.validation.ValidateContext;
import net.ymate.platform.validation.ValidateResult;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * 身份证号码验证
 *
 * @author 刘镇 (suninformation@163.com) on 2020/02/15 19:20
 * @since 2.1.0
 */
@CleanProxy
public class IDCardValidator implements IValidator {

    private static final int[] COEFFICIENTS = new int[]{7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};

    private static final char[] CODES = new char[]{'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};

    private static final int ID_CARD_LENGTH = 18;

    private static final Pattern DATE_PATTERN = Pattern.compile("(\\d{4})(\\d{2})(\\d{2})");

    private static final String DATE_FORMAT = "yyyyMMdd";

    private static final int DATE_PATTERN_COUNT = 3;

    private static final String I18N_MESSAGE_KEY = "ymp.validation.id_card";

    private static final String I18N_MESSAGE_DEFAULT_VALUE = "{0} not a valid ID card number.";

    /**
     * @param idCardNo 身份证号码
     * @return 返回idCardNo字符串是否为合法身份证号码
     * @since 2.1.0
     */
    public static boolean validate(String idCardNo) {
        if (StringUtils.length(idCardNo) == ID_CARD_LENGTH) {
            int sum = IntStream.range(0, ID_CARD_LENGTH - 1).map(i -> Integer.parseInt(String.valueOf(idCardNo.charAt(i))) * COEFFICIENTS[i]).sum();
            char code = CODES[sum % 11];
            if (idCardNo.charAt(ID_CARD_LENGTH - 1) == code) {
                String dateStr = StringUtils.substring(idCardNo, 6, 14);
                Matcher matcher = DATE_PATTERN.matcher(dateStr);
                if (matcher.find() && matcher.groupCount() == DATE_PATTERN_COUNT) {
                    try {
                        return Objects.equals(DateTimeHelper.bind(dateStr, DATE_FORMAT).toString(DATE_FORMAT), dateStr);
                    } catch (ParseException ignored) {
                    }
                }
            }
        }
        return false;
    }

    @Override
    public ValidateResult validate(ValidateContext context) {
        Object paramValue = context.getParamValue();
        if (paramValue != null) {
            boolean matched = false;
            VIDCard ann = (VIDCard) context.getAnnotation();
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
                return ValidateResult.builder(context, ann.msg(), I18N_MESSAGE_KEY, I18N_MESSAGE_DEFAULT_VALUE).matched(true).build();
            }
        }
        return null;
    }
}
