/*
 * Copyright 2007-2018 the original author or authors.
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

import net.ymate.platform.commons.lang.BlurObject;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.CodecUtils;
import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.beans.annotation.CleanProxy;
import net.ymate.platform.core.support.IInitialization;
import net.ymate.platform.validation.AbstractValidator;
import net.ymate.platform.validation.ValidateContext;
import net.ymate.platform.validation.ValidateResult;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.StringUtils;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/10/24 下午6:10
 * @since 2.0.6
 */
@CleanProxy
public final class RSADataValidator extends AbstractValidator {

    private static final String I18N_MESSAGE_KEY = "ymp.validation.rsa_data_invalid";

    private static final String I18N_MESSAGE_DEFAULT_VALUE = "{0} invalid.";

    /**
     * @param paramName 参数名称
     * @return 获取指定名称参数的原始内容
     */
    public static BlurObject getOriginalValue(String paramName) {
        return BlurObject.bind(ValidateContext.getLocalAttributes().get("original_" + paramName));
    }

    /**
     * @param owner         所属应用容器对象
     * @param providerClass 提供者类型
     * @return 加载RSA密钥提供者接口实现类
     * @since 2.1.2
     */
    public static IRSAKeyProvider getRSAKeyProvider(IApplication owner, Class<? extends IRSAKeyProvider> providerClass) {
        IRSAKeyProvider keyProvider = owner.getBeanFactory().getBean(providerClass);
        if (keyProvider == null) {
            // 若非默认值则不加载全局配置
            if (IRSAKeyProvider.class.equals(providerClass)) {
                keyProvider = ClassUtils.loadClass(IRSAKeyProvider.class);
            } else if (!providerClass.isInterface()) {
                keyProvider = ClassUtils.impl(providerClass, IRSAKeyProvider.class);
            }
        }
        // 判断是否需要初始化
        if (keyProvider instanceof IInitialization) {
            try {
                @SuppressWarnings("unchecked")
                IInitialization<IApplication> keyProviderInit = ((IInitialization<IApplication>) keyProvider);
                if (!keyProviderInit.isInitialized()) {
                    keyProviderInit.initialize(owner);
                }
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
        return keyProvider;
    }

    /**
     * 对originalValue字符串内容采用RSA公钥加密
     *
     * @param keyProvider   RSA密钥数据提供者类
     * @param originalValue 字符串内容
     * @return 加密后的BASE64字符串
     * @throws Exception 可能产生的任何异常
     */
    public static String encryptStr(IRSAKeyProvider keyProvider, String originalValue) throws Exception {
        return CodecUtils.RSA.encryptPublicKey(originalValue, keyProvider.getPublicKey());
    }

    public static String encryptPrivateStr(IRSAKeyProvider keyProvider, String originalValue) throws Exception {
        return CodecUtils.RSA.encrypt(originalValue, keyProvider.getPrivateKey());
    }

    /**
     * 对str字符串内容采用RSA私钥解密
     *
     * @param keyProvider RSA密钥数据提供者类
     * @param str         字符串内容
     * @return 解密后的字符串内容
     * @throws Exception 可能产生的任何异常
     */
    public static String decryptStr(IRSAKeyProvider keyProvider, String str) throws Exception {
        return CodecUtils.RSA.decrypt(str, keyProvider.getPrivateKey());
    }

    public static String decryptPublicStr(IRSAKeyProvider keyProvider, String str) throws Exception {
        return CodecUtils.RSA.decryptPublicKey(str, keyProvider.getPublicKey());
    }

    @Override
    public ValidateResult validate(ValidateContext context) {
        VRSAData ann = (VRSAData) context.getAnnotation();
        Object paramValue = context.getParamValue();
        if (paramValue != null) {
            boolean matched = false;
            String value = getParamValue(paramValue, false);
            if (StringUtils.isNotBlank(value)) {
                IRSAKeyProvider keyProvider = getRSAKeyProvider(context.getOwner(), ann.providerClass());
                if (keyProvider == null) {
                    throw new NullArgumentException("providerClass");
                }
                try {
                    String originalValue = decryptStr(keyProvider, value);
                    ValidateContext.getLocalAttributes().put("original_" + StringUtils.defaultIfBlank(ann.value(), context.getParamInfo().getName()), originalValue);
                } catch (Exception e) {
                    matched = true;
                }
            } else {
                matched = true;
            }
            if (matched) {
                return ValidateResult.builder(context, ann.msg(), I18N_MESSAGE_KEY, I18N_MESSAGE_DEFAULT_VALUE).matched(true).build();
            }
        }
        return null;
    }
}
