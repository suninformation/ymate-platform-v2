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
import net.ymate.platform.validation.AbstractValidator;
import net.ymate.platform.validation.ValidateContext;
import net.ymate.platform.validation.ValidateResult;
import net.ymate.platform.validation.annotation.Validator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

/**
 * 数值类型参数验证
 *
 * @author 刘镇 (suninformation@163.com) on 2013-4-17 下午8:36:16
 * @version 1.0
 */
@Validator(VNumeric.class)
@CleanProxy
public class NumericValidator extends AbstractValidator {

    @Override
    public ValidateResult validate(ValidateContext context) {
        Object _paramValue = context.getParamValue();
        if (_paramValue != null) {
            VNumeric _vNumeric = (VNumeric) context.getAnnotation();
            int _result = 0;
            if (_paramValue.getClass().isArray()) {
                Object[] _values = (Object[]) _paramValue;
                for (Object _pValue : _values) {
                    _result = checkNumeric(_pValue, _vNumeric);
                    if (_result > 0) {
                        break;
                    }
                }
            } else {
                _result = checkNumeric(_paramValue, _vNumeric);
            }
            if (_result > 0) {
                String _pName = StringUtils.defaultIfBlank(context.getParamLabel(), context.getParamName());
                _pName = __doGetI18nFormatMessage(context, _pName, _pName);
                String _msg = StringUtils.trimToNull(_vNumeric.msg());
                if (_msg != null) {
                    _msg = __doGetI18nFormatMessage(context, _msg, _msg, _pName);
                } else {
                    if (_result > 1) {
                        _msg = __doGetI18nFormatMessage(context, "ymp.validation.numeric", "{0} not a valid numeric.", _pName);
                    } else {
                        if (_vNumeric.max() > 0 && _vNumeric.min() > 0) {
                            _msg = __doGetI18nFormatMessage(context, "ymp.validation.numeric_between", "{0} numeric must be between {1} and {2}.", _pName, _vNumeric.min(), _vNumeric.max());
                        } else if (_vNumeric.max() > 0) {
                            _msg = __doGetI18nFormatMessage(context, "ymp.validation.numeric_max", "{0} numeric must be lt {1}.", _pName, _vNumeric.max());
                        } else {
                            _msg = __doGetI18nFormatMessage(context, "ymp.validation.numeric_min", "{0} numeric must be gt {1}.", _pName, _vNumeric.min());
                        }
                    }
                }
                return new ValidateResult(context.getParamName(), _msg);
            }
        }
        return null;
    }

    private int checkNumeric(Object paramValue, VNumeric vNumeric) {
        boolean _matched = false;
        boolean _flag = false;
        try {
            Number _number = NumberUtils.createNumber(BlurObject.bind(paramValue).toStringValue());
            if (_number == null) {
                _matched = true;
                _flag = true;
            } else {
                if (vNumeric.min() > 0 && _number.doubleValue() < vNumeric.min()) {
                    _matched = true;
                } else if (vNumeric.max() > 0 && _number.doubleValue() > vNumeric.max()) {
                    _matched = true;
                }
            }
        } catch (Exception e) {
            _matched = true;
            _flag = true;
        }
        return _matched ? (_flag ? 2 : 1) : 0;
    }
}
