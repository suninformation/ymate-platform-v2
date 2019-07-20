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
package net.ymate.platform.commons;

/**
 * 密码处理器接口(对数据库等登录密码进行加密或解密操作)
 *
 * @author 刘镇 (suninformation@163.com) on 15/4/13 下午1:38
 */
public interface IPasswordProcessor {

    /**
     * 设置加/解密密钥, 若为未提供则采用默认
     *
     * @param passKey 加/解密KEY
     */
    void setPassKey(String passKey);

    /**
     * 获取加/解密密钥
     *
     * @return 返回当前设置的加/解密密钥
     */
    String getPassKey();

    /**
     * 字符串加密
     *
     * @param source 源字符串
     * @return 对source字符串进行加密后返回
     * @throws Exception 可能产生的异常
     */
    String encrypt(String source) throws Exception;

    /**
     * 字符串解密
     *
     * @param target 目标字符串
     * @return 对target字符串进行解密后返回
     * @throws Exception 可能产生的异常
     */
    String decrypt(String target) throws Exception;
}
