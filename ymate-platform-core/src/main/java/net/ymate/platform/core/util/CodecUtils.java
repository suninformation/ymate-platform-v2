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
package net.ymate.platform.core.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.spec.KeySpec;

/**
 * DES/AES加密/解密工具类
 *
 * @author 刘镇 (suninformation@163.com) on 2011-6-14 下午12:24:17
 * @version 1.0
 */
public class CodecUtils {

    public static final CodecHelper DES;

    public static final CodecHelper AES;

    static {
        DES = new CodecHelper(56, "DES", "DES/ECB/PKCS5Padding");
        AES = new CodecHelper(128, "AES", "AES") {
            @Override
            public Key toKey(byte[] key) throws Exception {
                SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
                KeySpec spec = new PBEKeySpec(StringUtils.newStringUtf8(key).toCharArray(), key, KEY_SIZE * Runtime.getRuntime().availableProcessors(), KEY_SIZE);
                SecretKey tmp = factory.generateSecret(spec);
                return new SecretKeySpec(tmp.getEncoded(), CIPHER_ALGORITHM);
            }
        };
    }

    public static class CodecHelper {

        /**
         * 密钥算法
         */
        protected final String KEY_ALGORITHM;

        /**
         * 加密/解密算法/工作模式/填充方式
         */
        protected final String CIPHER_ALGORITHM;

        protected final int KEY_SIZE;

        public CodecHelper(int keySize, String keyAlgorithm, String cipherAlgorithm) {
            KEY_SIZE = keySize;
            KEY_ALGORITHM = keyAlgorithm;
            CIPHER_ALGORITHM = cipherAlgorithm;
        }

        /**
         * 生成密钥
         *
         * @return byte[] 二进制密钥
         * @throws Exception if an error occurs.
         */
        public byte[] initKey() throws Exception {
            // 实例化密钥生成器
            KeyGenerator kg = KeyGenerator.getInstance(KEY_ALGORITHM);
            // 初始化密钥生成器
            kg.init(KEY_SIZE);
            // 生成密钥
            SecretKey secretKey = kg.generateKey();
            // 获取二进制密钥编码形式
            return secretKey.getEncoded();
        }

        public String initKeyToString() throws Exception {
            return Base64.encodeBase64URLSafeString(initKey());
        }

        /**
         * 转换密钥
         *
         * @param key 二进制密钥
         * @return Key 密钥
         * @throws Exception if an error occurs.
         */
        public Key toKey(byte[] key) throws Exception {
            // 实例化密钥工厂
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(KEY_ALGORITHM);
            // 生成密钥
            return keyFactory.generateSecret(new SecretKeySpec(key, KEY_ALGORITHM));
        }

        /**
         * 加密数据
         *
         * @param data 待加密数据
         * @param key  密钥
         * @return byte[] 加密后的数据
         * @throws Exception if an error occurs.
         */
        public byte[] encrypt(byte[] data, byte[] key) throws Exception {
            // 实例化
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            // 初始化，设置为加密模式
            cipher.init(Cipher.ENCRYPT_MODE, toKey(key));
            // 执行操作
            return cipher.doFinal(data);
        }

        public String encrypt(String data, String key) throws Exception {
            return Base64.encodeBase64URLSafeString(encrypt(data.getBytes(), key.getBytes()));
        }

        /**
         * 解密数据
         *
         * @param data 待解密数据
         * @param key  密钥
         * @return byte[] 解密后的数据
         * @throws Exception if an error occurs.
         */
        public byte[] decrypt(byte[] data, byte[] key) throws Exception {
            // 实例化
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            // 初始化，设置为解密模式
            cipher.init(Cipher.DECRYPT_MODE, toKey(key));
            // 执行操作
            return cipher.doFinal(data);
        }

        public String decrypt(String data, String key) throws Exception {
            return StringUtils.newStringUtf8(decrypt(Base64.decodeBase64(data), key.getBytes()));
        }
    }
}
