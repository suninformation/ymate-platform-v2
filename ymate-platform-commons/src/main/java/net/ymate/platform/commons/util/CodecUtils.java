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
package net.ymate.platform.commons.util;

import net.ymate.platform.commons.lang.PairObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.codec.digest.DigestUtils;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * DES/AES/PBE/RSA加密/解密工具类
 *
 * @author 刘镇  (suninformation@163.com) on 2011-6-14 下午12:24:17
 */
public class CodecUtils {

    public static final String RSA_SIGN_MD5_WITH_RSA = "MD5withRSA";

    public static final String RSA_SIGN_SHA1_WITH_RSA = "SHA1withRSA";

    public static final CodecHelper DES = new CodecHelper(56, "DES", "DES/ECB/PKCS5Padding");

    public static final CodecHelper AES = new AESCodecHelper(128, 128);

    public static final CodecHelper PBE = new PBECodecHelper(128);

    public static final RSACodecHelper RSA = new RSACodecHelper(1024);

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

        private Provider cipherProvider;

        public CodecHelper(int keySize, String keyAlgorithm, String cipherAlgorithm) {
            KEY_SIZE = keySize;
            KEY_ALGORITHM = keyAlgorithm;
            CIPHER_ALGORITHM = cipherAlgorithm;
        }

        public CodecHelper(int keySize, String keyAlgorithm, String cipherAlgorithm, Provider provider) {
            this(keySize, keyAlgorithm, cipherAlgorithm);
            cipherProvider = provider;
        }

        protected Cipher getCipherInstance() throws NoSuchPaddingException, NoSuchAlgorithmException {
            Cipher cipher;
            if (cipherProvider != null) {
                cipher = Cipher.getInstance(CIPHER_ALGORITHM, cipherProvider);
            } else {
                cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            }
            return cipher;
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
            return Base64.encodeBase64String(initKey());
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
            Cipher cipher = getCipherInstance();
            // 初始化，设置为加密模式
            cipher.init(Cipher.ENCRYPT_MODE, toKey(key));
            // 执行操作
            return cipher.doFinal(data);
        }

        public String encrypt(String data, String key) throws Exception {
            return Base64.encodeBase64String(encrypt(data.getBytes(), key.getBytes()));
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
            Cipher cipher = getCipherInstance();
            // 初始化，设置为解密模式
            cipher.init(Cipher.DECRYPT_MODE, toKey(key));
            // 执行操作
            return cipher.doFinal(data);
        }

        public String decrypt(String data, String key) throws Exception {
            return StringUtils.newStringUtf8(decrypt(Base64.decodeBase64(data), key.getBytes()));
        }
    }

    public static class AESCodecHelper extends CodecHelper {

        private final int ITERATION_COUNT;

        public AESCodecHelper(int keySize, int iterationCount) {
            super(keySize, "AES", "AES");
            ITERATION_COUNT = iterationCount <= 0 ? 128 : iterationCount;
        }

        @Override
        public Key toKey(byte[] key) throws Exception {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec = new PBEKeySpec(StringUtils.newStringUtf8(key).toCharArray(), key, ITERATION_COUNT, KEY_SIZE);
            SecretKey tmp = factory.generateSecret(spec);
            return new SecretKeySpec(tmp.getEncoded(), CIPHER_ALGORITHM);
        }
    }

    public static class PBECodecHelper extends CodecHelper {

        private final int ITERATION_COUNT;

        public PBECodecHelper(int iterationCount) {
            super(0, "PBE", "PBEWithMD5AndDES");
            ITERATION_COUNT = iterationCount <= 0 ? 128 : iterationCount;
        }

        @Override
        public byte[] initKey() throws Exception {
            throw new UnsupportedOperationException();
        }

        @Override
        public String initKeyToString() throws Exception {
            throw new UnsupportedOperationException();
        }

        @Override
        public Key toKey(byte[] key) throws Exception {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(CIPHER_ALGORITHM);
            KeySpec spec = new PBEKeySpec(StringUtils.newStringUtf8(key).toCharArray());
            return factory.generateSecret(spec);
        }

        @Override
        public byte[] encrypt(byte[] data, byte[] key) throws Exception {
            return encrypt(data, key, DigestUtils.md5Hex(key).substring(0, 8).getBytes());
        }

        public byte[] encrypt(byte[] data, byte[] key, byte[] salt) throws Exception {
            // 实例化
            Cipher cipher = getCipherInstance();
            // 初始化，设置为加密模式
            PBEParameterSpec parameterSpec = new PBEParameterSpec(salt, ITERATION_COUNT);
            cipher.init(Cipher.ENCRYPT_MODE, toKey(key), parameterSpec);
            // 执行操作
            return cipher.doFinal(data);
        }

        public String encrypt(String data, String key, String salt) throws Exception {
            return Base64.encodeBase64String(encrypt(data.getBytes(), key.getBytes(), salt.getBytes()));
        }

        @Override
        public byte[] decrypt(byte[] data, byte[] key) throws Exception {
            return decrypt(data, key, DigestUtils.md5Hex(key).substring(0, 8).getBytes());
        }

        public byte[] decrypt(byte[] data, byte[] key, byte[] salt) throws Exception {
            // 实例化
            Cipher cipher = getCipherInstance();
            // 初始化，设置为解密模式
            PBEParameterSpec parameterSpec = new PBEParameterSpec(salt, ITERATION_COUNT);
            cipher.init(Cipher.DECRYPT_MODE, toKey(key), parameterSpec);
            // 执行操作
            return cipher.doFinal(data);
        }

        public String decrypt(String data, String key, String salt) throws Exception {
            return StringUtils.newStringUtf8(decrypt(Base64.decodeBase64(data), key.getBytes(), salt.getBytes()));
        }
    }

    public static class RSACodecHelper extends CodecHelper {

        private String signatureAlgorithm;

        private Provider signatureAlgorithmProvider;

        public RSACodecHelper(int keySize) {
            super(keySize, "RSA", "RSA/ECB/PKCS1Padding");
        }

        public RSACodecHelper(int keySize, String signatureAlgorithm, Provider signatureAlgorithmProvider) {
            this(keySize, "RSA/ECB/PKCS1Padding", null, signatureAlgorithm, signatureAlgorithmProvider);
        }

        public RSACodecHelper(int keySize, String cipherAlgorithm) {
            super(keySize, "RSA", cipherAlgorithm);
        }

        public RSACodecHelper(int keySize, String cipherAlgorithm, String signatureAlgorithm, Provider signatureAlgorithmProvider) {
            this(keySize, cipherAlgorithm, null, signatureAlgorithm, signatureAlgorithmProvider);
        }

        public RSACodecHelper(int keySize, String cipherAlgorithm, Provider cipherAlgorithmProvider, String signatureAlgorithm, Provider signatureAlgorithmProvider) {
            super(keySize, "RSA", cipherAlgorithm, cipherAlgorithmProvider);
            this.signatureAlgorithm = org.apache.commons.lang3.StringUtils.defaultIfBlank(signatureAlgorithm, RSA_SIGN_MD5_WITH_RSA);
            this.signatureAlgorithmProvider = signatureAlgorithmProvider;
        }

        private Signature getSignatureInstance() throws NoSuchAlgorithmException {
            Signature sign;
            if (signatureAlgorithmProvider != null) {
                sign = Signature.getInstance(signatureAlgorithm, signatureAlgorithmProvider);
            } else {
                sign = Signature.getInstance(signatureAlgorithm);
            }
            return sign;
        }

        @Override
        public byte[] initKey() throws Exception {
            throw new UnsupportedOperationException();
        }

        @Override
        public String initKeyToString() throws Exception {
            throw new UnsupportedOperationException();
        }

        public PairObject<RSAPublicKey, RSAPrivateKey> initRSAKey() throws Exception {
            KeyPairGenerator keyPG = KeyPairGenerator.getInstance(KEY_ALGORITHM);
            keyPG.initialize(KEY_SIZE);
            //
            KeyPair keyPair = keyPG.generateKeyPair();
            //
            return new PairObject<>((RSAPublicKey) keyPair.getPublic(), (RSAPrivateKey) keyPair.getPrivate());
        }

        public String getRSAKey(Key rsaKey) throws Exception {
            return Base64.encodeBase64String(rsaKey.getEncoded());
        }

        public String sign(byte[] data, String privateKey) throws Exception {
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(privateKey));
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            PrivateKey privKey = keyFactory.generatePrivate(keySpec);
            //
            Signature sign = getSignatureInstance();
            sign.initSign(privKey);
            sign.update(data);
            //
            return Base64.encodeBase64String(sign.sign());
        }

        public boolean verify(byte[] data, String publicKey, String sign) throws Exception {
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.decodeBase64(publicKey));
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            PublicKey pubKey = keyFactory.generatePublic(keySpec);
            //
            Signature signature = getSignatureInstance();
            signature.initVerify(pubKey);
            signature.update(data);
            //
            return signature.verify(Base64.decodeBase64(sign));
        }

        private byte[] dataSegment(byte[] data, Cipher cipher, int inputLen) throws Exception {
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                int offSet = 0;
                byte[] buffer;
                int idx = 0;
                int length = data.length;
                while (length - offSet > 0) {
                    if (length - offSet > inputLen) {
                        buffer = cipher.doFinal(data, offSet, inputLen);
                    } else {
                        buffer = cipher.doFinal(data, offSet, length - offSet);
                    }
                    outputStream.write(buffer, 0, buffer.length);
                    idx++;
                    offSet = idx * inputLen;
                }
                return outputStream.toByteArray();
            }
        }

        @Override
        public byte[] encrypt(byte[] data, byte[] key) throws Exception {
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(key);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            Key privKey = keyFactory.generatePrivate(keySpec);
            Cipher cipher = getCipherInstance();
            cipher.init(Cipher.ENCRYPT_MODE, privKey);
            //
            return dataSegment(data, cipher, 117);
        }

        @Override
        public String encrypt(String data, String key) throws Exception {
            return Base64.encodeBase64String(encrypt(data.getBytes(), Base64.decodeBase64(key)));
        }

        @Override
        public byte[] decrypt(byte[] data, byte[] key) throws Exception {
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(key);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            Key privKey = keyFactory.generatePrivate(keySpec);
            Cipher cipher = getCipherInstance();
            cipher.init(Cipher.DECRYPT_MODE, privKey);
            //
            return dataSegment(data, cipher, 128);
        }

        @Override
        public String decrypt(String data, String key) throws Exception {
            return StringUtils.newStringUtf8(decrypt(Base64.decodeBase64(data), Base64.decodeBase64(key)));
        }

        public byte[] encryptPublicKey(byte[] data, byte[] key) throws Exception {
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(key);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            Key pubKey = keyFactory.generatePublic(keySpec);
            Cipher cipher = getCipherInstance();
            cipher.init(Cipher.ENCRYPT_MODE, pubKey);
            //
            return dataSegment(data, cipher, 117);
        }

        public String encryptPublicKey(String data, String key) throws Exception {
            return Base64.encodeBase64String(encryptPublicKey(data.getBytes(), Base64.decodeBase64(key)));
        }

        public byte[] decryptPublicKey(byte[] data, byte[] key) throws Exception {
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(key);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            Key pubKey = keyFactory.generatePublic(keySpec);
            Cipher cipher = getCipherInstance();
            cipher.init(Cipher.DECRYPT_MODE, pubKey);
            //
            return dataSegment(data, cipher, 128);
        }

        public String decryptPublicKey(String data, String key) throws Exception {
            return StringUtils.newStringUtf8(decryptPublicKey(Base64.decodeBase64(data), Base64.decodeBase64(key)));
        }
    }
}
