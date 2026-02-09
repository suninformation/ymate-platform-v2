/*
 * Copyright 2007-present the original author or authors.
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
import org.junit.Assert;
import org.junit.Test;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * CodecUtils单元测试
 *
 * @author 刘镇 (suninformation@163.com) on 2026-01-04 15:53
 * @since 2.1.4
 */
public class CodecUtilsTest {

    private static final String TEST_DATA = "测试数据1234567890abcdefghijklmnopqrstuvwxyz";

    @Test
    public void testDES() throws Exception {
        // 生成密钥
        String key = CodecUtils.DES.initKeyToString();
        Assert.assertNotNull(key);
        Assert.assertFalse(key.isEmpty());

        // 加密
        String encrypted = CodecUtils.DES.encrypt(TEST_DATA, key);
        Assert.assertNotNull(encrypted);
        Assert.assertFalse(encrypted.isEmpty());
        Assert.assertNotEquals(TEST_DATA, encrypted);

        // 解密
        String decrypted = CodecUtils.DES.decrypt(encrypted, key);
        Assert.assertNotNull(decrypted);
        Assert.assertEquals(TEST_DATA, decrypted);
    }

    @Test
    public void testAES() throws Exception {
        // 生成密钥
        String key = CodecUtils.AES.initKeyToString();
        Assert.assertNotNull(key);
        Assert.assertFalse(key.isEmpty());

        // 加密
        String encrypted = CodecUtils.AES.encrypt(TEST_DATA, key);
        Assert.assertNotNull(encrypted);
        Assert.assertFalse(encrypted.isEmpty());
        Assert.assertNotEquals(TEST_DATA, encrypted);

        // 解密
        String decrypted = CodecUtils.AES.decrypt(encrypted, key);
        Assert.assertNotNull(decrypted);
        Assert.assertEquals(TEST_DATA, decrypted);
    }

    @Test
    public void testPBE() throws Exception {
        // 生成密钥
        String key = CodecUtils.PBE.initKeyToString();
        Assert.assertNotNull(key);
        Assert.assertFalse(key.isEmpty());

        // 加密
        String encrypted = CodecUtils.PBE.encrypt(TEST_DATA, key);
        Assert.assertNotNull(encrypted);
        Assert.assertFalse(encrypted.isEmpty());
        Assert.assertNotEquals(TEST_DATA, encrypted);

        // 解密
        String decrypted = CodecUtils.PBE.decrypt(encrypted, key);
        Assert.assertNotNull(decrypted);
        Assert.assertEquals(TEST_DATA, decrypted);
    }

    @Test
    public void testPBEWithSalt() throws Exception {
        // 生成密钥和8字节盐值
        String key = "testpassword";

        // 生成8字节的ASCII字符串作为盐值，确保UTF-8编码后仍为8字节
        String salt = "12345678";
        Assert.assertNotNull(key);
        Assert.assertNotNull(salt);
        Assert.assertEquals("Salt must be 8 bytes long", 8, salt.getBytes(java.nio.charset.StandardCharsets.UTF_8).length);

        // 创建新的PBECodecHelper实例，避免缓存问题
        CodecUtils.PBECodecHelper pbeHelper = new CodecUtils.PBECodecHelper(128);

        // 使用指定盐值加密
        String encrypted = pbeHelper.encrypt(TEST_DATA, key, salt);
        Assert.assertNotNull(encrypted);
        Assert.assertFalse(encrypted.isEmpty());

        // 使用相同盐值解密
        String decrypted = pbeHelper.decrypt(encrypted, key, salt);
        Assert.assertNotNull(decrypted);
        Assert.assertEquals(TEST_DATA, decrypted);

        // 使用不同盐值解密，应失败
        String wrongSalt = "87654321";
        Assert.assertEquals("Wrong salt must be 8 bytes long", 8, wrongSalt.getBytes(java.nio.charset.StandardCharsets.UTF_8).length);

        // 预期使用错误盐值会抛出异常，使用断言捕获异常
        Assert.assertThrows(Exception.class, () -> {
            pbeHelper.decrypt(encrypted, key, wrongSalt);
        });
    }

    @Test
    public void testRSA() throws Exception {
        // 生成密钥对
        PairObject<RSAPublicKey, RSAPrivateKey> keyPair = CodecUtils.RSA.initRSAKey();
        Assert.assertNotNull(keyPair);
        Assert.assertNotNull(keyPair.getKey());
        Assert.assertNotNull(keyPair.getValue());

        // 获取公钥和私钥字符串
        String publicKey = CodecUtils.RSA.getRSAKey(keyPair.getKey());
        String privateKey = CodecUtils.RSA.getRSAKey(keyPair.getValue());
        Assert.assertNotNull(publicKey);
        Assert.assertNotNull(privateKey);

        // 使用公钥加密
        String encrypted = CodecUtils.RSA.encryptPublicKey(TEST_DATA, publicKey);
        Assert.assertNotNull(encrypted);
        Assert.assertFalse(encrypted.isEmpty());
        Assert.assertNotEquals(TEST_DATA, encrypted);

        // 使用私钥解密
        String decrypted = CodecUtils.RSA.decrypt(encrypted, privateKey);
        Assert.assertNotNull(decrypted);
        Assert.assertEquals(TEST_DATA, decrypted);

        // 使用私钥加密
        encrypted = CodecUtils.RSA.encrypt(TEST_DATA, privateKey);
        Assert.assertNotNull(encrypted);
        Assert.assertFalse(encrypted.isEmpty());
        Assert.assertNotEquals(TEST_DATA, encrypted);

        // 使用公钥解密
        decrypted = CodecUtils.RSA.decryptPublicKey(encrypted, publicKey);
        Assert.assertNotNull(decrypted);
        Assert.assertEquals(TEST_DATA, decrypted);
    }

    @Test
    public void testRSASignature() throws Exception {
        // 生成密钥对
        PairObject<RSAPublicKey, RSAPrivateKey> keyPair = CodecUtils.RSA.initRSAKey();
        Assert.assertNotNull(keyPair);

        // 获取公钥和私钥字符串
        String publicKey = CodecUtils.RSA.getRSAKey(keyPair.getKey());
        String privateKey = CodecUtils.RSA.getRSAKey(keyPair.getValue());

        // 签名
        String sign = CodecUtils.RSA.sign(TEST_DATA, privateKey);
        Assert.assertNotNull(sign);
        Assert.assertFalse(sign.isEmpty());

        // 验证签名（使用UTF-8编码，与sign方法一致）
        boolean verified = CodecUtils.RSA.verify(TEST_DATA.getBytes("UTF-8"), publicKey, sign);
        Assert.assertTrue(verified);

        // 验证失败的情况
        verified = CodecUtils.RSA.verify("篡改的数据".getBytes("UTF-8"), publicKey, sign);
        Assert.assertFalse(verified);
    }

    @Test
    public void testAES256() throws Exception {
        // 创建256位AES实例
        CodecUtils.CodecHelper aes256 = new CodecUtils.AESCodecHelper(256, 128);

        // 生成密钥
        String key = aes256.initKeyToString();
        Assert.assertNotNull(key);
        Assert.assertFalse(key.isEmpty());

        // 加密
        String encrypted = aes256.encrypt(TEST_DATA, key);
        Assert.assertNotNull(encrypted);
        Assert.assertFalse(encrypted.isEmpty());
        Assert.assertNotEquals(TEST_DATA, encrypted);

        // 解密
        String decrypted = aes256.decrypt(encrypted, key);
        Assert.assertNotNull(decrypted);
        Assert.assertEquals(TEST_DATA, decrypted);
    }

    @Test
    public void testRSA2048() throws Exception {
        // 创建2048位RSA实例
        CodecUtils.RSACodecHelper rsa2048 = new CodecUtils.RSACodecHelper(2048);

        // 生成密钥对
        PairObject<RSAPublicKey, RSAPrivateKey> keyPair = rsa2048.initRSAKey();
        Assert.assertNotNull(keyPair);

        // 获取公钥和私钥字符串
        String publicKey = rsa2048.getRSAKey(keyPair.getKey());
        String privateKey = rsa2048.getRSAKey(keyPair.getValue());

        // 签名
        String sign = rsa2048.sign(TEST_DATA, privateKey);
        Assert.assertNotNull(sign);

        // 验证签名（使用UTF-8编码，与sign方法一致）
        boolean verified = rsa2048.verify(TEST_DATA.getBytes("UTF-8"), publicKey, sign);
        Assert.assertTrue(verified);
    }

    @Test
    public void testRSAWithLargeData() throws Exception {
        // 生成大测试数据（超过RSA默认最大加密块大小）
        StringBuilder largeData = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            largeData.append(TEST_DATA);
        }
        String largeTestData = largeData.toString();

        // 生成密钥对
        PairObject<RSAPublicKey, RSAPrivateKey> keyPair = CodecUtils.RSA.initRSAKey();
        String publicKey = CodecUtils.RSA.getRSAKey(keyPair.getKey());
        String privateKey = CodecUtils.RSA.getRSAKey(keyPair.getValue());

        // 使用公钥加密大数据（测试分段加密）
        String encrypted = CodecUtils.RSA.encryptPublicKey(largeTestData, publicKey);
        Assert.assertNotNull(encrypted);
        Assert.assertFalse(encrypted.isEmpty());

        // 使用私钥解密大数据（测试分段解密）
        String decrypted = CodecUtils.RSA.decrypt(encrypted, privateKey);
        Assert.assertNotNull(decrypted);
        Assert.assertEquals(largeTestData, decrypted);
    }

    @Test
    public void testBinaryDataEncryption() throws Exception {
        // 测试二进制数据加密解密
        byte[] binaryData = new byte[]{0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09};

        // DES测试
        byte[] desKey = CodecUtils.DES.initKey();
        byte[] desEncrypted = CodecUtils.DES.encrypt(binaryData, desKey);
        byte[] desDecrypted = CodecUtils.DES.decrypt(desEncrypted, desKey);
        Assert.assertArrayEquals(binaryData, desDecrypted);

        // AES测试
        byte[] aesKey = CodecUtils.AES.initKey();
        byte[] aesEncrypted = CodecUtils.AES.encrypt(binaryData, aesKey);
        byte[] aesDecrypted = CodecUtils.AES.decrypt(aesEncrypted, aesKey);
        Assert.assertArrayEquals(binaryData, aesDecrypted);

        // PBE测试 - 使用ASCII字符串作为密钥
        byte[] pbeKey = "testpassword".getBytes();
        byte[] pbeEncrypted = CodecUtils.PBE.encrypt(binaryData, pbeKey);
        byte[] pbeDecrypted = CodecUtils.PBE.decrypt(pbeEncrypted, pbeKey);
        Assert.assertArrayEquals(binaryData, pbeDecrypted);
    }

    @Test
    public void testRSAInitKeyExceptions() {
        // 测试RSA的initKey方法抛出异常
        Assert.assertThrows(UnsupportedOperationException.class, CodecUtils.RSA::initKey);

        // 测试RSA的initKeyToString方法抛出异常
        Assert.assertThrows(UnsupportedOperationException.class, CodecUtils.RSA::initKeyToString);
    }

    @Test
    public void testRSAWithDifferentSignatureAlgorithms() throws Exception {
        // 测试不同签名算法
        String[] signatureAlgorithms = {
                CodecUtils.RSA_SIGN_SHA1_WITH_RSA,
                CodecUtils.RSA_SIGN_SHA256_WITH_RSA
        };

        for (String algorithm : signatureAlgorithms) {
            // 为每个算法生成独立的密钥对
            CodecUtils.RSACodecHelper rsaHelper = new CodecUtils.RSACodecHelper(2048, algorithm, null);
            PairObject<RSAPublicKey, RSAPrivateKey> keyPair = rsaHelper.initRSAKey();
            String publicKey = rsaHelper.getRSAKey(keyPair.getKey());
            String privateKey = rsaHelper.getRSAKey(keyPair.getValue());

            String sign = rsaHelper.sign(TEST_DATA, privateKey);
            Assert.assertNotNull(sign);
            // 验证签名（使用UTF-8编码，与sign方法一致）
            boolean verified = rsaHelper.verify(TEST_DATA.getBytes("UTF-8"), publicKey, sign);
            Assert.assertTrue("Signature verification failed for algorithm: " + algorithm, verified);
        }
    }

    @Test
    public void testDESWithCustomProvider() throws Exception {
        // 测试使用自定义Provider的DES加密解密
        CodecUtils.CodecHelper desHelper = new CodecUtils.CodecHelper(56, "DES", CodecUtils.DES_CIPHER_DES_ECB_PKCS5);
        String key = desHelper.initKeyToString();
        String encrypted = desHelper.encrypt(TEST_DATA, key);
        String decrypted = desHelper.decrypt(encrypted, key);
        Assert.assertEquals(TEST_DATA, decrypted);
    }

    @Test
    public void testAESWithCustomIterationCount() throws Exception {
        // 测试使用自定义迭代次数的AES加密解密
        CodecUtils.AESCodecHelper aesHelper = new CodecUtils.AESCodecHelper(256, 256);
        String key = aesHelper.initKeyToString();
        String encrypted = aesHelper.encrypt(TEST_DATA, key);
        String decrypted = aesHelper.decrypt(encrypted, key);
        Assert.assertEquals(TEST_DATA, decrypted);
    }

    @Test
    public void testPBECustomIterationCount() throws Exception {
        // 测试使用自定义迭代次数的PBE加密解密
        CodecUtils.PBECodecHelper pbeHelper = new CodecUtils.PBECodecHelper(256);
        String key = pbeHelper.initKeyToString();
        String encrypted = pbeHelper.encrypt(TEST_DATA, key);
        String decrypted = pbeHelper.decrypt(encrypted, key);
        Assert.assertEquals(TEST_DATA, decrypted);
    }

    @Test
    public void testRSABlockSizeMethods() throws Exception {
        // 测试RSA的块大小设置和获取方法
        CodecUtils.RSACodecHelper rsaHelper = new CodecUtils.RSACodecHelper(2048);

        // 测试默认块大小
        int defaultEncryptBlockSize = rsaHelper.getMaxEncryptBlockSize();
        int defaultDecryptBlockSize = rsaHelper.getMaxDecryptBlockSize();
        // 验证块大小为正数
        Assert.assertTrue(defaultEncryptBlockSize > 0);
        Assert.assertTrue(defaultDecryptBlockSize > 0);

        // 测试设置块大小（使用合理的块大小值，避免解密时填充错误）
        rsaHelper.maxEncryptBlockSize(240);
        rsaHelper.maxDecryptBlockSize(256);
        Assert.assertEquals(240, rsaHelper.getMaxEncryptBlockSize());
        Assert.assertEquals(256, rsaHelper.getMaxDecryptBlockSize());

        // 测试链式调用
        rsaHelper.maxEncryptBlockSize(230).maxDecryptBlockSize(256);
        Assert.assertEquals(230, rsaHelper.getMaxEncryptBlockSize());
        Assert.assertEquals(256, rsaHelper.getMaxDecryptBlockSize());

        // 验证设置后的块大小能正常工作
        PairObject<RSAPublicKey, RSAPrivateKey> keyPair = rsaHelper.initRSAKey();
        String publicKey = rsaHelper.getRSAKey(keyPair.getKey());
        String privateKey = rsaHelper.getRSAKey(keyPair.getValue());

        // 使用较短的测试数据，确保不超过设置的加密块大小
        String shortTestData = "Short test data";
        String encrypted = rsaHelper.encryptPublicKey(shortTestData, publicKey);
        String decrypted = rsaHelper.decrypt(encrypted, privateKey);
        Assert.assertEquals(shortTestData, decrypted);

        // 恢复默认块大小
        rsaHelper.maxEncryptBlockSize(245).maxDecryptBlockSize(256);
        Assert.assertEquals(245, rsaHelper.getMaxEncryptBlockSize());
        Assert.assertEquals(256, rsaHelper.getMaxDecryptBlockSize());
    }
}
