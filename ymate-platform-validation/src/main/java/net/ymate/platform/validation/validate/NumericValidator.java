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

    public ValidateResult validate(ValidateContext context) {
        boolean _matched = false;
        try {
            Number _number = NumberUtils.createNumber(BlurObject.bind(context.getParamValue()).toStringValue());
            if (_number == null) {
                _matched = true;
            } else {
                VNumeric _vNumeric = (VNumeric) context.getAnnotation();
                if (_vNumeric.min() > 0 && _number.doubleValue() < _vNumeric.min()) {
                    _matched = true;
                } else if (_vNumeric.max() > 0 && _number.doubleValue() > _vNumeric.max()) {
                    _matched = true;
                }
            }
        } catch (Exception e) {
            _matched = true;
        }
        if (_matched) {
            return new ValidateResult(context.getParamName(), "not a valid number size.");
        }
        return null;
    }
}
