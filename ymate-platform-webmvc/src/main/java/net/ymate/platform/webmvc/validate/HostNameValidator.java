/*
 * Copyright 2007-2019 the original author or authors.
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
package net.ymate.platform.webmvc.validate;

import net.ymate.platform.commons.lang.BlurObject;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.beans.annotation.CleanProxy;
import net.ymate.platform.core.support.IContext;
import net.ymate.platform.validation.IValidator;
import net.ymate.platform.validation.ValidateContext;
import net.ymate.platform.validation.ValidateResult;
import net.ymate.platform.webmvc.exception.ValidationResultException;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/8/12 上午 02:01
 * @since 2.0.6
 */
@CleanProxy
public class HostNameValidator implements IValidator {

    private static final String I18N_MESSAGE_KEY = "ymp.validation.hostname_invalid";

    private static final String I18N_MESSAGE_DEFAULT_VALUE = "{0} invalid.";

    public static boolean validate(IContext context, String url, Class<? extends IHostNameChecker> checker) throws Exception {
        boolean matched;
        if (IHostNameChecker.class.equals(checker)) {
            // 先尝试通过 SPI 方式加载接口实例
            IHostNameChecker spiChecker = ClassUtils.loadClass(IHostNameChecker.class);
            if (spiChecker != null) {
                matched = !spiChecker.check(context, url);
            } else {
                // 否则使用默认方式
                matched = !IHostNameChecker.DEFAULT.check(context, url);
            }
        } else {
            matched = !ClassUtils.impl(checker, IHostNameChecker.class).check(context, url);
        }
        return matched;
    }

    @Override
    public ValidateResult validate(ValidateContext context) {
        Object paramValue = context.getParamValue();
        if (paramValue != null) {
            boolean matched = false;
            VHostName hostNameAnn = (VHostName) context.getAnnotation();
            try {
                if (paramValue.getClass().isArray()) {
                    Object[] urls = (Object[]) context.getParamValue();
                    if (urls != null) {
                        for (Object url : urls) {
                            matched = validate(context, BlurObject.bind(url).toStringValue(), hostNameAnn.checker());
                            if (matched) {
                                break;
                            }
                        }
                    }
                } else {
                    matched = validate(context, BlurObject.bind(paramValue).toStringValue(), hostNameAnn.checker());
                }
            } catch (Exception e) {
                throw new Error(RuntimeUtils.unwrapThrow(e));
            }
            if (matched) {
                ValidateResult.Builder builder = ValidateResult.builder(context, hostNameAnn.msg(), I18N_MESSAGE_KEY, I18N_MESSAGE_DEFAULT_VALUE).matched(true);
                if (hostNameAnn.httpStatus() > 0) {
                    throw new ValidationResultException(builder.build().getMsg(), hostNameAnn.httpStatus());
                }
                return builder.build();
            }
        }
        return null;
    }
}