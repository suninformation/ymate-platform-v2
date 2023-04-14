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
import net.ymate.platform.commons.util.DateTimeUtils;
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
        String sign = BlurObject.bind(paramValues.get(signatureValidate.paramName())).toStringValue();
        long timestamp = BlurObject.bind(paramValues.get(signatureValidate.timestampName())).toLongValue();
        long timeLifecycle = BlurObject.bind(owner.getOwner().getParam(IWebMvcConfig.PARAMS_SIGNATURE_TIME_LIFECYCLE, String.valueOf(signatureValidate.timeLifecycle()))).toLongValue();
        boolean invalid = StringUtils.isBlank(sign) || timestamp <= 0 || !doNonceValueValidate(owner, signatureValidate.nonceName(), paramValues);
        if (invalid || timeLifecycle > 0 && System.currentTimeMillis() - timestamp > timeLifecycle * DateTimeUtils.SECOND) {
            throw new ParameterSignatureException("Missing signature required parameter or expired timestamp parameter value.");
        }
        Map<String, Object> signatureParams = new HashMap<>(paramValues.size());
        paramValues.forEach((key, value) -> {
            if (!key.equals(signatureValidate.paramName()) && !ArrayUtils.contains(signatureValidate.excludedParams(), key)) {
                signatureParams.put(key, value);
            }
        });
        return StringUtils.equals(sign, doSignature(owner, signatureValidate, signatureParams));
    }

    protected boolean doNonceValueValidate(IWebMvc owner, String nonceName, Map<String, Object> paramValues) {
        if (StringUtils.isNotBlank(nonceName)) {
            String nonceValue = BlurObject.bind(paramValues.get(nonceName)).toStringValue();
            return StringUtils.isNotBlank(nonceValue);
        }
        return true;
    }

    protected String doSignature(IWebMvc owner, SignatureValidate signatureValidate, Map<String, Object> signatureParams) {
        ISignatureExtraParamProcessor extraParamProcessor = null;
        if (!signatureValidate.processorClass().equals(ISignatureExtraParamProcessor.class)) {
            extraParamProcessor = ClassUtils.impl(signatureValidate.processorClass(), ISignatureExtraParamProcessor.class);
        }
        return ParamUtils.createSignature(signatureParams, signatureValidate.encode(), signatureValidate.upperCase(), extraParamProcessor != null ? extraParamProcessor.getExtraParams(owner, signatureParams) : null);
    }
}
