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
        boolean _matched = false;
        boolean _flag = false;
        double _max = 0d;
        double _min = 0d;
        try {
            Number _number = NumberUtils.createNumber(BlurObject.bind(context.getParamValue()).toStringValue());
            if (_number == null) {
                _matched = true;
                _flag = true;
            } else {
                VNumeric _vNumeric = (VNumeric) context.getAnnotation();
                _max = _vNumeric.max();
                _min = _vNumeric.min();
                if (_min > 0 && _number.doubleValue() < _min) {
                    _matched = true;
                } else if (_max > 0 && _number.doubleValue() > _max) {
                    _matched = true;
                }
            }
        } catch (Exception e) {
            _matched = true;
            _flag = true;
        }
        if (_matched) {
            String _msg = null;
            if (_flag) {
                _msg = I18N.formatMessage(VALIDATION_I18N_RESOURCE, __NUMERIC_VALIDATOR, "{0} not a valid numeric.", context.getParamName());
            } else {
                if (_max > 0 && _min > 0) {
                    _msg = I18N.formatMessage(VALIDATION_I18N_RESOURCE, __NUMERIC_VALIDATOR_BETWEEN, "{0} numeric must be between {1} and {2}.", context.getParamName(), _max, _min);
                } else if (_max > 0) {
                    _msg = I18N.formatMessage(VALIDATION_I18N_RESOURCE, __NUMERIC_VALIDATOR_MAX, "{0} numeric must be gt {1}.", context.getParamName(), _max);
                } else {
                    _msg = I18N.formatMessage(VALIDATION_I18N_RESOURCE, __NUMERIC_VALIDATOR_MIN, "{0} numeric must be lt {1}.", context.getParamName(), _min);
                }
            }
            return new ValidateResult(context.getParamName(), _msg);
        }
        return null;
    }
}
