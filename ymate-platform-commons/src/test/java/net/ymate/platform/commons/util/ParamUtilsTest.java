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

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * @author 刘镇 (suninformation@163.com) on 2026-01-13 02:15:36
 * @since 2.1.4
 */
public class ParamUtilsTest {

    @Test
    public void testFixUrlWithProtocol() {
        // 测试正常情况
        assertEquals("http://example.com/", ParamUtils.fixUrlWithProtocol("http://example.com", true));
        assertEquals("https://example.com", ParamUtils.fixUrlWithProtocol("https://example.com/", false));

        // 测试边界情况
        assertEquals("http://example.com", ParamUtils.fixUrlWithProtocol("  http://example.com  ", false));

        // 测试异常情况
        try {
            ParamUtils.fixUrlWithProtocol("ftp://example.com", false);
            fail("Expected IllegalArgumentException but none was thrown");
        } catch (IllegalArgumentException exception) {
            assertEquals("URL 'ftp://example.com' must start with HTTP or HTTPS.", exception.getMessage());
        }

        // 测试 null 和空字符串
        assertEquals("", ParamUtils.fixUrlWithProtocol(null, false));
        assertEquals("", ParamUtils.fixUrlWithProtocol("", false));
    }

    @Test
    public void testFixUrl() {
        Object[][] testData = {
                {"test", true, false, "/test"},
                {"/test", false, true, "test/"},
                {"/test/", true, true, "/test/"},
                {"test/", false, false, "test"},
                {"  test  ", true, false, "/test"},
                {null, true, false, ""},
                {"", true, false, ""}
        };
        for (Object[] data : testData) {
            String url = (String) data[0];
            boolean needStartWith = (boolean) data[1];
            boolean needEndWith = (boolean) data[2];
            String expected = (String) data[3];
            assertEquals(expected, ParamUtils.fixUrl(url, needStartWith, needEndWith));
        }
    }

    @Test
    public void testAppendPrefixIfNotEmpty() {
        // 测试正常情况
        assertEquals("prefix_test", ParamUtils.appendPrefixIfNotEmpty("test", "prefix_"));

        // 测试边界情况
        assertEquals("test", ParamUtils.appendPrefixIfNotEmpty("test", null));
        assertEquals("test", ParamUtils.appendPrefixIfNotEmpty("test", ""));
        assertEquals(null, ParamUtils.appendPrefixIfNotEmpty(null, "prefix_"));
        assertEquals("", ParamUtils.appendPrefixIfNotEmpty("", "prefix_"));
    }

    @Test
    public void testInlineText() {
        // 测试正常情况
        assertEquals("testtexttesttext", ParamUtils.inlineText("test\ntext\r\ttest\"text\\"));

        // 测试边界情况
        assertEquals("", ParamUtils.inlineText("\n\r\t '\"\\"));
        assertEquals("test", ParamUtils.inlineText("test"));
        assertEquals(null, ParamUtils.inlineText(null));
    }

    @Test
    public void testSafetyTextVarargs() {
        // 测试正常情况
        assertEquals("t**t", ParamUtils.safetyText("test", 1, 3));
        assertEquals("te*t", ParamUtils.safetyText("test", 1, 3, "e"));
        assertEquals("test", ParamUtils.safetyText("test", 1, 3, "e", "s"));

        // 测试边界情况
        assertEquals("****", ParamUtils.safetyText("test", 0, 4));
        assertEquals("tes*", ParamUtils.safetyText("test", 4, 5));
        assertEquals("test", ParamUtils.safetyText("test", -1, 0));
        assertEquals("test", ParamUtils.safetyText("test", 1, 1));

        // 测试 null 和空字符串
        assertEquals(null, ParamUtils.safetyText(null, 1, 3));
        assertEquals("", ParamUtils.safetyText("", 1, 3));
    }

    @Test
    public void testSafetyTextSingleExcludedChar() {
        // 测试正常情况
        assertEquals("te*t", ParamUtils.safetyText("test", 1, 3, (CharSequence) "e"));

        // 测试边界情况
        assertEquals("t**t", ParamUtils.safetyText("test", 1, 3, (CharSequence) null));
        assertEquals("t**t", ParamUtils.safetyText("test", 1, 3, (CharSequence) ""));
    }

    @Test
    public void testIsInvalid() {
        // 测试正常情况
        assertTrue(ParamUtils.isInvalid(null));
        assertTrue(ParamUtils.isInvalid(new HashMap<>()));
        assertTrue(ParamUtils.isInvalid(new ArrayList<>()));
        assertTrue(ParamUtils.isInvalid(""));
        assertTrue(ParamUtils.isInvalid("   "));

        assertFalse(ParamUtils.isInvalid("test"));
        assertFalse(ParamUtils.isInvalid(Collections.singletonMap("key", "value")));
        assertFalse(ParamUtils.isInvalid(Collections.singletonList("value")));
        assertFalse(ParamUtils.isInvalid(123));
        assertFalse(ParamUtils.isInvalid(true));
    }

    @Test
    public void testBuildQueryParamStr() {
        // 测试正常情况
        Map<String, Object> params = new HashMap<>();
        params.put("key1", "value1");
        params.put("key2", "value2");
        assertEquals("key1=value1&key2=value2", ParamUtils.buildQueryParamStr(params, false, null));

        // 测试编码情况
        params.put("key3", "value 3");
        String encoded = ParamUtils.buildQueryParamStr(params, true, "UTF-8");
        assertTrue(encoded.contains("key3=value+3"));

        // 测试数组和集合情况
        params.put("array", new String[]{"value1", "value2"});
        params.put("list", Arrays.asList("value3", "value4"));
        String result = ParamUtils.buildQueryParamStr(params, false, null);
        assertTrue(result.contains("array=value1"));
        assertTrue(result.contains("array=value2"));
        assertTrue(result.contains("list=value3"));
        assertTrue(result.contains("list=value4"));
    }

    @Test
    public void testAppendQueryParamValue() {
        // 测试正常情况
        Map<String, Object> params = new HashMap<>();
        params.put("key1", "value1");
        params.put("key2", "value2");

        assertEquals("http://example.com?key1=value1&key2=value2",
                ParamUtils.appendQueryParamValue("http://example.com", params, false, null));

        assertEquals("http://example.com?existing=param&key1=value1&key2=value2",
                ParamUtils.appendQueryParamValue("http://example.com?existing=param", params, false, null));

        // 测试边界情况
        assertEquals("http://example.com",
                ParamUtils.appendQueryParamValue("http://example.com", null, false, null));

        assertEquals("http://example.com",
                ParamUtils.appendQueryParamValue("http://example.com", new HashMap<>(), false, null));
    }

    @Test
    public void testConvertParamMap() {
        // 测试正常情况
        Map<String, Object> sourceMap = new HashMap<>();
        sourceMap.put("key1", "value1");
        sourceMap.put("key2", new String[]{"value2", "value3"});
        sourceMap.put("key3", Arrays.asList("value4", "value5"));

        Map<String, String[]> result = ParamUtils.convertParamMap(sourceMap);
        assertEquals(1, result.get("key1").length);
        assertEquals("value1", result.get("key1")[0]);
        assertEquals(2, result.get("key2").length);
        assertEquals(2, result.get("key3").length);

        // 测试 null 值
        sourceMap.put("key4", null);
        result = ParamUtils.convertParamMap(sourceMap);
        assertNull(result.get("key4"));
    }

    @Test
    public void testParseQueryParamStr() {
        // 测试正常情况
        Map<String, String[]> result = ParamUtils.parseQueryParamStr("key1=value1&key2=value2");
        assertEquals(1, result.get("key1").length);
        assertEquals("value1", result.get("key1")[0]);
        assertEquals(1, result.get("key2").length);
        assertEquals("value2", result.get("key2")[0]);

        // 测试边界情况
        result = ParamUtils.parseQueryParamStr("key1=value1&key1=value2");
        assertEquals(2, result.get("key1").length);

        // 测试没有 = 符号的参数
        result = ParamUtils.parseQueryParamStr("key1&key2=value2");
        assertEquals(1, result.get("key1").length);
        assertEquals("", result.get("key1")[0]);

        // 测试 null 和空字符串
        result = ParamUtils.parseQueryParamStr(null);
        assertTrue(result.isEmpty());

        result = ParamUtils.parseQueryParamStr("");
        assertTrue(result.isEmpty());
    }

    @Test
    public void testParseQueryParamStrWithParams() {
        // 测试正常情况
        Map<String, String[]> result = ParamUtils.parseQueryParamStr("key1=value1&key2=value2", false, null);
        assertEquals(1, result.get("key1").length);

        // 测试解码情况
        result = ParamUtils.parseQueryParamStr("key1=value+1&key2=value%202", true, "UTF-8");
        assertEquals("value 1", result.get("key1")[0]);
        assertEquals("value 2", result.get("key2")[0]);
    }

    @Test
    public void testBuildActionForm() {
        // 测试正常情况
        Map<String, Object> params = new HashMap<>();
        params.put("key1", "value1");
        params.put("key2", "value2");

        String form = ParamUtils.buildActionForm("http://example.com", true, params);
        assertTrue(form.contains("<form id=\"_payment_submit\""));
        assertTrue(form.contains("name=\"_payment_submit\""));
        assertTrue(form.contains("action=\"http://example.com\""));
        assertTrue(form.contains("method=\"POST\""));
        assertTrue(form.contains("<input type=\"hidden\" name=\"key1\" value=\"value1\">"));
        assertTrue(form.contains("<input type=\"hidden\" name=\"key2\" value=\"value2\">"));
        assertTrue(form.contains("<script>document.forms['_payment_submit'].submit();</script>"));
    }

    @Test
    public void testBuildActionFormWithParams() {
        // 测试正常情况
        Map<String, Object> params = new HashMap<>();
        params.put("key1", "value1");
        params.put("key2", new String[]{"value2", "value3"});
        params.put("key3", Arrays.asList("value4", "value5"));

        String form = ParamUtils.buildActionForm("http://example.com", false, true, true, "UTF-8", params);
        assertTrue(form.contains("method=\"GET\""));
        assertTrue(form.contains("enctype=\"application/x-www-form-urlencoded;charset=UTF-8\""));
        assertTrue(form.contains("<input type=\"hidden\" name=\"key1\" value=\"value1\">"));
        assertTrue(form.contains("<input type=\"hidden\" name=\"key2\" value=\"value2\">"));
        assertTrue(form.contains("<input type=\"hidden\" name=\"key2\" value=\"value3\">"));
        assertTrue(form.contains("<input type=\"hidden\" name=\"key3\" value=\"value4\">"));
        assertTrue(form.contains("<input type=\"hidden\" name=\"key3\" value=\"value5\">"));

        // 测试 null 参数
        form = ParamUtils.buildActionForm("http://example.com", true, false, false, null, null);
        assertTrue(form.contains("<form"));
        assertTrue(form.contains("</form>"));
    }

    @Test
    public void testCreateNonceStr() {
        // 测试正常情况
        String nonce1 = ParamUtils.createNonceStr();
        String nonce2 = ParamUtils.createNonceStr();
        assertNotNull(nonce1);
        assertNotNull(nonce2);
        assertNotEquals(nonce1, nonce2);
        assertTrue(nonce1.length() >= 6 && nonce1.length() <= 32);
        assertTrue(nonce2.length() >= 6 && nonce2.length() <= 32);
    }

    @Test
    public void testCreateSignature() {
        // 测试正常情况
        Map<String, Object> params = new HashMap<>();
        params.put("key1", "value1");
        params.put("key2", "value2");

        String signature1 = ParamUtils.createSignature(params, false, "extra1=extraValue1");
        String signature2 = ParamUtils.createSignature(params, false, "extra1=extraValue1");
        assertEquals(signature1, signature2);

        // 测试不同参数生成不同签名
        String signature3 = ParamUtils.createSignature(params, false, "extra2=extraValue2");
        assertNotEquals(signature1, signature3);

        // 测试编码情况
        String signature4 = ParamUtils.createSignature(params, true, "UTF-8", "extra1=extraValue1");
        assertNotEquals(signature1, signature4);

        // 测试大小写
        String signature5 = ParamUtils.createSignature(params, false, true, "extra1=extraValue1");
        String signature6 = ParamUtils.createSignature(params, false, false, "extra1=extraValue1");
        assertNotEquals(signature5, signature6);
        assertEquals(signature5, signature6.toUpperCase());

        // 测试自定义签名生成器
        ParamUtils.ISignatureBuilder signatureBuilder = content -> "custom_signature";
        String signature7 = ParamUtils.createSignature(params, false, false, signatureBuilder, "extra1=extraValue1");
        assertEquals("custom_signature", signature7);

        // 测试自定义签名生成器（大写）
        String signature8 = ParamUtils.createSignature(params, false, true, signatureBuilder, "extra1=extraValue1");
        assertEquals("CUSTOM_SIGNATURE", signature8);
    }
}
