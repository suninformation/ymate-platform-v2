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
package net.ymate.platform.webmvc.impl;

import net.ymate.platform.commons.lang.BlurObject;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.ParamUtils;
import net.ymate.platform.webmvc.*;
import net.ymate.platform.webmvc.annotation.SignatureValidate;
import net.ymate.platform.webmvc.exception.ParameterSignatureException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/02/26 11:08
 * @since 2.1.0
 */
public class DefaultSignatureValidator implements ISignatureValidator {

    @Override
    public boolean validate(IWebMvc owner, RequestMeta requestMeta, SignatureValidate signatureValidate) {
        ISignatureParamParser paramParser = ClassUtils.impl(signatureValidate.parserClass(), ISignatureParamParser.class);
        Map<String, Object> paramValues = new HashMap<>(paramParser.getParams(owner, requestMeta));
        String originSign = null;
        if (paramValues.containsKey(signatureValidate.paramName())) {
            originSign = BlurObject.bind(paramValues.get(signatureValidate.paramName())).toStringValue();
        }
        boolean invalid = StringUtils.isBlank(originSign) || StringUtils.isNotBlank(signatureValidate.nonceName()) && !paramValues.containsKey(signatureValidate.nonceName());
        if (invalid) {
            throw new ParameterSignatureException("Missing signature required parameter.");
        }
        Map<String, Object> signatureParams = new HashMap<>(paramValues.size());
        paramValues.forEach((key, value) -> {
            if (!key.equals(signatureValidate.paramName()) && !ArrayUtils.contains(signatureValidate.excludedParams(), key)) {
                signatureParams.put(key, value);
            }
        });
        ISignatureExtraParamProcessor extraParamProcessor = null;
        if (!signatureValidate.processorClass().equals(ISignatureExtraParamProcessor.class)) {
            extraParamProcessor = ClassUtils.impl(signatureValidate.processorClass(), ISignatureExtraParamProcessor.class);
        }
        String sign = ParamUtils.createSignature(signatureParams, signatureValidate.encode(), signatureValidate.upperCase(), extraParamProcessor != null ? extraParamProcessor.getExtraParams(owner, signatureParams) : null);
        return StringUtils.equals(originSign, sign);
    }
}
