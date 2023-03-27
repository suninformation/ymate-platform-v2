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
package net.ymate.platform.commons.impl;

import net.ymate.platform.commons.IPasswordProcessor;
import net.ymate.platform.commons.util.CodecUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 默认密码处理器类
 *
 * @author 刘镇 (suninformation@163.com) on 15/4/13 下午3:47
 */
public class DefaultPasswordProcessor implements IPasswordProcessor {

//    private final String KEY = DigestUtils.md5Hex(DefaultPasswordProcessor.class.getName());

    private String passKey;

    public DefaultPasswordProcessor() {
        passKey = StringUtils.trimToNull(System.getProperty(SYSTEM_PASS_KEY));
    }

    @Override
    public void setPassKey(String passKey) {
        this.passKey = passKey;
    }

    @Override
    public String getPassKey() {
        return StringUtils.defaultIfBlank(passKey, "16296b50a6db0d0bd45d2e5f84fcdd76");
    }

    @Override
    public String encrypt(String source) throws Exception {
        return CodecUtils.DES.encrypt(source, getPassKey());
    }

    @Override
    public String decrypt(String target) throws Exception {
        return CodecUtils.DES.decrypt(target, getPassKey());
    }
}
