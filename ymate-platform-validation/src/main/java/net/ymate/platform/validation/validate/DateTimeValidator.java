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

import net.ymate.platform.core.beans.annotation.CleanProxy;
import net.ymate.platform.core.lang.BlurObject;
import net.ymate.platform.core.util.DateTimeUtils;
import net.ymate.platform.validation.AbstractValidator;
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
@CleanProxy
public class DateTimeValidator extends AbstractValidator {

    /**
     * @param paramValue 参数值
     * @return 返回paramValue字符串是否为合法的时间戳
     * @since 2.0.6
     */
    public static boolean isDateTime(String paramValue) {
        return isDateTime(paramValue, null);
    }

    /**
     * @param paramValue 参数值
     * @param pattern    日期时间格式
     * @return 返回paramValue字符串是否为合法的时间戳
     * @since 2.0.6
     */
    public static boolean isDateTime(String paramValue, String pattern) {
        try {
            DateTimeUtils.parseDateTime(paramValue, StringUtils.defaultIfBlank(pattern, DateTimeUtils.YYYY_MM_DD_HH_MM_SS));
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public ValidateResult validate(ValidateContext context) {
        Object _paramValue = context.getParamValue();
        if (_paramValue != null) {
            VDateTime _vDate = (VDateTime) context.getAnnotation();
            boolean _matched = false;
            if (context.getParamValue().getClass().isArray()) {
                Object[] _values = (Object[]) _paramValue;
                for (Object _pValue : _values) {
                    _matched = !isDateTime(BlurObject.bind(_pValue).toStringValue(), _vDate.pattern());
                    if (_matched) {
                        break;
                    }
                }
            } else {
                _matched = !isDateTime(BlurObject.bind(_paramValue).toStringValue(), _vDate.pattern());
            }
            if (_matched) {
                String _pName = StringUtils.defaultIfBlank(context.getParamLabel(), context.getParamName());
                _pName = __doGetI18nFormatMessage(context, _pName, _pName);
                //
                String _msg = StringUtils.trimToNull(_vDate.msg());
                if (_msg != null) {
                    _msg = __doGetI18nFormatMessage(context, _msg, _msg, _pName);
                } else {
                    _msg = __doGetI18nFormatMessage(context, "ymp.validation.datetime", "{0} not a valid datetime.", _pName);
                }
                return new ValidateResult(context.getParamName(), _msg);
            }
        }
        return null;
    }
}
