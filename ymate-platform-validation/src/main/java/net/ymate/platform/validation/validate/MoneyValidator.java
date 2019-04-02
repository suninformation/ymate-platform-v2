package net.ymate.platform.validation.validate;

import net.ymate.platform.core.beans.annotation.CleanProxy;
import net.ymate.platform.validation.AbstractValidator;
import net.ymate.platform.validation.ValidateContext;
import net.ymate.platform.validation.ValidateResult;
import net.ymate.platform.validation.annotation.Validator;
import org.apache.commons.lang.StringUtils;

/**
 * 金额验证器
 *
 * @author Xuanzi An

 */
@Validator(VMoney.class)
@CleanProxy
public class MoneyValidator extends AbstractValidator {

    @Override
    public ValidateResult validate(ValidateContext context) {
        Object _paramValue = context.getParamValue();
        if (_paramValue != null) {
            boolean _matched = false;
            VMoney _anno = (VMoney) context.getAnnotation();
            String MoneyRegx = "(^(([1-9][0-9]*)|(([0]\\.\\d{1,2}|[1-9][0-9]*\\.\\d{1,2})))$)";
            if (StringUtils.trimToEmpty(_paramValue.toString()).matches(MoneyRegx)){
                _matched = true;
            }
            if (_matched) {
                String _pName = StringUtils.defaultIfBlank(context.getParamLabel(), context.getParamName());
                _pName = __doGetI18nFormatMessage(context, _pName, _pName);
                String _msg = StringUtils.trimToNull(_anno.msg());
                if (_msg != null) {
                    _msg = __doGetI18nFormatMessage(context, _msg, _msg, _pName);
                }
                return new ValidateResult(context.getParamName(), _msg);
            }
        }
        return null;
    }
}