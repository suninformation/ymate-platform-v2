/*
 * Copyright 2007-2025 the original author or authors.
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
package net.ymate.platform.commons.impl;

import net.ymate.platform.commons.IPasswordCipherEncoder;
import net.ymate.platform.commons.IPasswordProcessor;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.StringUtils;

/**
 * 默认密码编码器接口实现
 * (提取自 ymate-module-passport 模块)
 *
 * @author 刘镇 (suninformation@163.com) on 2025/5/29 12:17
 * @since 2.1.4
 */
public class DefaultPasswordCipherEncoder implements IPasswordCipherEncoder {

    @Override
    public String encode(String origin) throws Exception {
        int position = StringUtils.lastIndexOf(origin, '@');
        if (position > -1) {
            String originStr = StringUtils.substring(origin, 0, position);
            String salt = StringUtils.substring(origin, position);
            return encode(originStr, salt);
        }
        throw new IllegalArgumentException("Cannot find '@' in the origin string, invalid parameter.");
    }

    @Override
    public String encode(String origin, String salt) throws Exception {
        if (StringUtils.isBlank(origin)) {
            throw new NullArgumentException("origin");
        }
        if (StringUtils.isBlank(salt)) {
            throw new NullArgumentException("salt");
        }
        IPasswordProcessor passwordProcessor = new DefaultPasswordProcessor();
        passwordProcessor.setPassKey(DigestUtils.sha1Hex(salt));
        String encoded = passwordProcessor.encrypt(origin);
        //
        return String.format("%s@%s", DigestUtils.sha1Hex(encoded), salt);
    }

    @Override
    public boolean match(String origin, String encoded) throws Exception {
        return !StringUtils.isAnyBlank(origin, encoded) && StringUtils.equals(encoded, encode(origin));
    }

    @Override
    public boolean match(String origin, String encoded, String salt) throws Exception {
        return !StringUtils.isAnyBlank(origin, encoded, salt) && StringUtils.equals(encoded, encode(origin, salt));
    }
}
