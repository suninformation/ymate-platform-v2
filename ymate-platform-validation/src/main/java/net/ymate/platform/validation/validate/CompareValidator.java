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

import java.math.BigDecimal;

/**
 * 参数值比较验证
 *
 * @author 刘镇 (suninformation@163.com) on 2013-4-17 下午9:57:16
 * @version 1.0
 */
@Validator(VCompare.class)
@CleanProxy
public class CompareValidator extends AbstractValidator {

    @Override
    public ValidateResult validate(ValidateContext context) {
        Object _paramValue = context.getParamValue();
        if (_paramValue != null) {
            VCompare _vCompare = (VCompare) context.getAnnotation();
            boolean _matched = true;
            String _paramValueStr = getParamValue(_paramValue);
            String _compareValueStr = getParamValue(context.getParamValue(_vCompare.with()));
            //
            if (StringUtils.isNumeric(_paramValueStr)) {
                int _compValue = new BigDecimal(_paramValueStr).compareTo(new BigDecimal(_compareValueStr));
                switch (_vCompare.cond()) {
                    case EQ:
                        _matched = _compValue == 0;
                        break;
                    case NOT_EQ:
                        _matched = _compValue != 0;
                        break;
                    case GT:
                        _matched = _compValue > 0;
                        break;
                    case LT:
                        _matched = _compValue < 0;
                        break;
                    case GT_EQ:
                        _matched = _compValue >= 0;
                        break;
                    case LT_EQ:
                        _matched = _compValue <= 0;
                        break;
                    default:
                }
            } else {
                switch (_vCompare.cond()) {
                    case EQ:
                        _matched = StringUtils.equals(_paramValueStr, _compareValueStr);
                        break;
                    case NOT_EQ:
                        _matched = !StringUtils.equals(_paramValueStr, _compareValueStr);
                        break;
                    default:
                }
            }
            if (!_matched) {
                String _pName = StringUtils.defaultIfBlank(context.getParamLabel(), context.getParamName());
                _pName = __doGetI18nFormatMessage(context, _pName, _pName);

                String _pLabel = StringUtils.defaultIfBlank(_vCompare.withLabel(), _vCompare.with());
                _pLabel = __doGetI18nFormatMessage(context, _pLabel, _pLabel);
                //
                String _msg = StringUtils.trimToNull(_vCompare.msg());
                if (_msg != null) {
                    _msg = __doGetI18nFormatMessage(context, _msg, _msg, _pName, _pLabel);
                } else {
                    switch (_vCompare.cond()) {
                        case NOT_EQ:
                            _msg = __doGetI18nFormatMessage(context, "ymp.validation.compare_not_eq", "{0} can not eq {1}.", _pName, _pLabel);
                            break;
                        case EQ:
                            _msg = __doGetI18nFormatMessage(context, "ymp.validation.compare_eq", "{0} must be eq {1}.", _pName, _pLabel);
                            break;
                        case GT:
                            _msg = __doGetI18nFormatMessage(context, "ymp.validation.compare_gt", "{0} must be gt {1}.", _pName, _pLabel);
                            break;
                        case LT:
                            _msg = __doGetI18nFormatMessage(context, "ymp.validation.compare_lt", "{0} must be lt {1}.", _pName, _pLabel);
                            break;
                        case GT_EQ:
                            _msg = __doGetI18nFormatMessage(context, "ymp.validation.compare_gt_eq", "{0} must be gt eq {1}.", _pName, _pLabel);
                            break;
                        case LT_EQ:
                            _msg = __doGetI18nFormatMessage(context, "ymp.validation.compare_lt_eq", "{0} must be lt eq {1}.", _pName, _pLabel);
                        default:
                    }
                }
                return new ValidateResult(context.getParamName(), _msg);
            }
        }
        return null;
    }

    private String getParamValue(Object paramValue) {
        String _pValue = null;
        if (paramValue.getClass().isArray()) {
            Object[] _objArr = (Object[]) paramValue;
            if (_objArr.length > 0) {
                _pValue = BlurObject.bind(_objArr[0]).toStringValue();
            }
        } else {
            _pValue = BlurObject.bind(paramValue).toStringValue();
        }
        return StringUtils.trimToEmpty(_pValue);
    }
}
