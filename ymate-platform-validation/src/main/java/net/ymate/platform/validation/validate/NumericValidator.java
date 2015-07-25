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
import net.ymate.platform.validation.IValidator;
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
public class NumericValidator implements IValidator {

    private static String __NUMERIC_VALIDATOR = "ymp.validation.numeric_validator";

    private static String __NUMERIC_VALIDATOR_BETWEEN = "ymp.validation.numeric_validator_between";

    private static String __NUMERIC_VALIDATOR_MIN = "ymp.validation.numeric_validator_min";

    private static String __NUMERIC_VALIDATOR_MAX = "ymp.validation.numeric_validator_max";

    public ValidateResult validate(ValidateContext context) {
        if (context.getParamValue() != null) {
            boolean _matched = false;
            boolean _flag = false;
            VNumeric _vNumeric = (VNumeric) context.getAnnotation();
            try {
                Number _number = NumberUtils.createNumber(BlurObject.bind(context.getParamValue()).toStringValue());
                if (_number == null) {
                    _matched = true;
                    _flag = true;
                } else {
                    if (_vNumeric.min() > 0 && _number.doubleValue() < _vNumeric.min()) {
                        _matched = true;
                    } else if (_vNumeric.max() > 0 && _number.doubleValue() > _vNumeric.max()) {
                        _matched = true;
                    }
                }
            } catch (Exception e) {
                _matched = true;
                _flag = true;
            }
            if (_matched) {
                String _pName = StringUtils.defaultIfBlank(context.getParamLabel(), context.getParamName());
                String _msg = StringUtils.trimToNull(_vNumeric.msg());
                if (_msg != null) {
                    _msg = I18N.formatMessage(VALIDATION_I18N_RESOURCE, _msg, _msg, _pName);
                } else {
                    if (_flag) {
                        _msg = I18N.formatMessage(VALIDATION_I18N_RESOURCE, __NUMERIC_VALIDATOR, "{0} not a valid numeric.", _pName);
                    } else {
                        if (_vNumeric.max() > 0 && _vNumeric.min() > 0) {
                            _msg = I18N.formatMessage(VALIDATION_I18N_RESOURCE, __NUMERIC_VALIDATOR_BETWEEN, "{0} numeric must be between {1} and {2}.", _pName, _vNumeric.max(), _vNumeric.min());
                        } else if (_vNumeric.max() > 0) {
                            _msg = I18N.formatMessage(VALIDATION_I18N_RESOURCE, __NUMERIC_VALIDATOR_MAX, "{0} numeric must be lt {1}.", _pName, _vNumeric.max());
                        } else {
                            _msg = I18N.formatMessage(VALIDATION_I18N_RESOURCE, __NUMERIC_VALIDATOR_MIN, "{0} numeric must be gt {1}.", _pName, _vNumeric.min());
                        }
                    }
                }
                return new ValidateResult(context.getParamName(), _msg);
            }
        }
        return null;
    }
}
