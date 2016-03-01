/*
 * Copyright 2007-2016 the original author or authors.
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

/**
 * 字符串长度验证
 *
 * @author 刘镇 (suninformation@163.com) on 2013-4-17 下午5:17:22
 * @version 1.0
 */
@Validator(VLength.class)
public class LengthValidator implements IValidator {

    public ValidateResult validate(ValidateContext context) {
        if (context.getParamValue() != null) {
            if (!context.getParamValue().getClass().isArray()) {
                String _value = BlurObject.bind(context.getParamValue()).toStringValue();
                if (StringUtils.isNotBlank(_value)) {
                    VLength _vLength = (VLength) context.getAnnotation();
                    boolean _matched = false;
                    if (_vLength.min() > 0 && _value.length() < _vLength.min()) {
                        _matched = true;
                    } else if (_vLength.max() > 0 && _value.length() > _vLength.max()) {
                        _matched = true;
                    }
                    if (_matched) {
                        String _pName = StringUtils.defaultIfBlank(context.getParamLabel(), context.getParamName());
                        _pName = I18N.formatMessage(VALIDATION_I18N_RESOURCE, _pName, _pName);
                        String _msg = StringUtils.trimToNull(_vLength.msg());
                        if (_msg != null) {
                            _msg = I18N.formatMessage(VALIDATION_I18N_RESOURCE, _msg, _msg, _pName);
                        } else {
                            if (_vLength.max() > 0 && _vLength.min() > 0) {
                                String __LENGTH_BETWEEN = "ymp.validation.length_between";
                                _msg = I18N.formatMessage(VALIDATION_I18N_RESOURCE, __LENGTH_BETWEEN, "{0} length must be between {1} and {2}.", _pName, _vLength.max(), _vLength.min());
                            } else if (_vLength.max() > 0) {
                                String __LENGTH_MAX = "ymp.validation.length_max";
                                _msg = I18N.formatMessage(VALIDATION_I18N_RESOURCE, __LENGTH_MAX, "{0} length must be lt {1}.", _pName, _vLength.max());
                            } else {
                                String __LENGTH_MIN = "ymp.validation.length_min";
                                _msg = I18N.formatMessage(VALIDATION_I18N_RESOURCE, __LENGTH_MIN, "{0} length must be gt {1}.", _pName, _vLength.min());
                            }
                        }
                        return new ValidateResult(context.getParamName(), _msg);
                    }
                }
            }
        }
        return null;
    }
}
