package net.ymate.platform.core.util;

import net.ymate.platform.core.lang.PairObject;
import org.apache.commons.codec.binary.Base64;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/10/26 上午1:40
 * @version 1.0
 */
public class CodecUtilsTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void AESCodecHelper() throws Exception {
        CodecUtils.AESCodecHelper _helper = new CodecUtils.AESCodecHelper(128, 128);
        String _key = _helper.initKeyToString();
        //
        System.out.println("密钥：" + _key);
        //
        String _aesStr = _helper.encrypt("123456", _key);
        System.out.println("加密串：" + _aesStr);
        System.out.println("解密串：" + _helper.decrypt(_aesStr, _key));
    }

    @Test
    public void PBECodecHelper() throws Exception {
        CodecUtils.PBECodecHelper _helper = new CodecUtils.PBECodecHelper(128);
        //
        String _pbeStr = _helper.encrypt("123456", "ymp", "12345678");
        System.out.println("加密串：" + _pbeStr);
        System.out.println("解密串：" + _helper.decrypt(_pbeStr, "ymp", "12345678"));
    }

    @Test
    public void RSACodecHelper() throws Exception {
        CodecUtils.RSACodecHelper _helper = new CodecUtils.RSACodecHelper(1024, CodecUtils.RSA_SIGN_SHA1withRSA, null);
        //
        PairObject<RSAPublicKey, RSAPrivateKey> _keys = _helper.initRSAKey();
        String _pubKey = _helper.getRSAKey(_keys.getKey());
        String _priKey = _helper.getRSAKey(_keys.getValue());
        //
        System.out.println("公钥: " + _pubKey);
        System.out.println("私钥: " + _priKey);
        //
        String _rsaStr = _helper.encryptPublicKey("123456", _pubKey);
        System.out.println("加密串：" + _rsaStr);
        //
        String _sign = _helper.sign(Base64.decodeBase64(_rsaStr), _priKey);
        System.out.println("签名：" + _sign);
        System.out.println("解密串：" + _helper.decrypt(_rsaStr, _priKey));
        System.out.println("验证签名：" + _helper.verify(Base64.decodeBase64(_rsaStr), _pubKey, _sign));
    }
}