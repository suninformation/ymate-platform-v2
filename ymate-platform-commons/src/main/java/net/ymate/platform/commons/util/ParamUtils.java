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

import net.ymate.platform.commons.lang.BlurObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

/**
 * @author 刘镇 (suninformation@163.com) on 15-1-12 下午4:50
 */
public class ParamUtils {

    private static final Log LOG = LogFactory.getLog(ParamUtils.class);

    public static final String HTTP_PREFIX = "http://";

    public static final String HTTPS_PREFIX = "https://";

    public static final String PATH_SEPARATOR = "/";

    /**
     * @param url         URL地址
     * @param needEndWith 是否必须以分隔符结束
     * @return 返回修正后的URL地址
     */
    public static String fixUrlWithProtocol(String url, boolean needEndWith) {
        url = StringUtils.trimToNull(url);
        if (url != null) {
            if (!StringUtils.startsWithIgnoreCase(url, HTTP_PREFIX) &&
                    !StringUtils.startsWithIgnoreCase(url, HTTPS_PREFIX)) {
                throw new IllegalArgumentException(String.format("URL '%s' must start with HTTP or HTTPS.", url));
            }
            return fixUrl(url, false, needEndWith);
        }
        return StringUtils.EMPTY;
    }

    /**
     * @param url           URL地址
     * @param needStartWith 是否以'/'开始
     * @param needEndWith   是否以'/'结束
     * @return 返回修正后的URL地址
     */
    public static String fixUrl(String url, boolean needStartWith, boolean needEndWith) {
        url = StringUtils.trimToNull(url);
        if (url != null) {
            if (needStartWith && !StringUtils.startsWith(url, PATH_SEPARATOR)) {
                url = PATH_SEPARATOR + url;
            } else if (!needStartWith && StringUtils.startsWith(url, PATH_SEPARATOR)) {
                url = StringUtils.substringAfter(url, PATH_SEPARATOR);
            }
            if (needEndWith && !StringUtils.endsWith(url, PATH_SEPARATOR)) {
                url = url + PATH_SEPARATOR;
            } else if (!needEndWith && StringUtils.endsWith(url, PATH_SEPARATOR)) {
                url = StringUtils.substringBeforeLast(url, PATH_SEPARATOR);
            }
            return url;
        }
        return StringUtils.EMPTY;
    }

    /**
     * @param str    字符串
     * @param prefix 前缀
     * @return 为字符串添加前缀，若字符串为空则原样返回
     * @since 2.1.3
     */
    public static String appendPrefixIfNotEmpty(String str, String prefix) {
        if (StringUtils.isAnyBlank(str, prefix)) {
            return str;
        }
        return String.format("%s%s", prefix, str);
    }

    /**
     * @param text 文本字符串
     * @return 替换文本字符串中的换行回车单双引号和空格字符使排成一行
     * @since 2.1.3
     */
    public static String inlineText(String text) {
        return StringUtils.replaceChars(text, "\n\r\t '\"\\", StringUtils.EMPTY);
    }

    /**
     * @param text          文本字符串
     * @param start         开始位置
     * @param end           结束位置
     * @param excludedChars 排除的字符集合（集合中的字符不会被替换）
     * @return 对数据进行安全处理（将start与end之间的字符替换为'*'字符）
     * @since 2.1.3
     */
    public static String safetyText(String text, int start, int end, CharSequence... excludedChars) {
        if (StringUtils.isBlank(text)) {
            return text;
        }
        char[] charArray = text.toCharArray();
        if (charArray.length > 0) {
            if (start < 0) {
                start = 0;
            } else if (start >= charArray.length) {
                start = charArray.length - 1;
            }
            if (end <= start) {
                end = charArray.length - start;
            }
            for (int idx = 0; idx < charArray.length; idx++) {
                if (idx >= start && idx < end) {
                    if (StringUtils.equalsAny(String.valueOf(charArray[idx]), excludedChars)) {
                        continue;
                    }
                    charArray[idx] = '*';
                }
            }
        }
        return new String(charArray);
    }

    public static String safetyText(String text, int start, int end, CharSequence excludedChar) {
        return safetyText(text, start, end, StringUtils.EMPTY, excludedChar);
    }

    public static boolean isInvalid(Object obj) {
        return obj == null || obj instanceof Map && ((Map<?, ?>) obj).isEmpty() || obj instanceof Collection && ((Collection<?>) obj).isEmpty() || obj instanceof CharSequence && StringUtils.isBlank((CharSequence) obj);
    }

    /**
     * @param params  请求参数映射
     * @param encode  是否对参数进行编码
     * @param charset Encode编码字符集，默认UTF-8
     * @return 对参数进行ASCII正序排列并生成请求参数串
     */
    public static String buildQueryParamStr(Map<String, ?> params, boolean encode, String charset) {
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);
        StringBuilder stringBuilder = new StringBuilder();
        keys.forEach(key -> {
            Object value = params.get(key);
            if (value != null) {
                if (value.getClass().isArray()) {
                    for (Object v : (Object[]) value) {
                        doAppendParamValue(key, v, stringBuilder, encode, charset);
                    }
                } else if (value instanceof Collection) {
                    for (Object v : (Collection<?>) value) {
                        doAppendParamValue(key, v, stringBuilder, encode, charset);
                    }
                } else {
                    doAppendParamValue(key, value, stringBuilder, encode, charset);
                }
            }
        });
        return stringBuilder.toString();
    }

    private static void doAppendParamValue(String key, Object v, StringBuilder stringBuilder, boolean encode, String charset) {
        if (stringBuilder.length() > 0) {
            stringBuilder.append("&");
        }
        String valueStr = v.toString();
        if (encode) {
            try {
                stringBuilder.append(key).append("=").append(URLEncoder.encode(valueStr, StringUtils.defaultIfBlank(charset, "UTF-8")));
            } catch (UnsupportedEncodingException e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
                }
            }
        } else {
            stringBuilder.append(key).append("=").append(valueStr);
        }
    }

    public static String appendQueryParamValue(String url, Map<String, ?> params, boolean encode, String charset) {
        if (params != null && !params.isEmpty()) {
            StringBuilder stringBuilder = new StringBuilder(url);
            if (!url.contains("?")) {
                stringBuilder.append("?");
            } else {
                stringBuilder.append("&");
            }
            return stringBuilder.append(buildQueryParamStr(params, encode, charset)).toString();
        }
        return url;
    }

    public static Map<String, String[]> convertParamMap(Map<String, ?> sourceMap) {
        Map<String, List<String>> params = new HashMap<>(sourceMap.size());
        sourceMap.forEach((key, value) -> {
            if (value != null) {
                if (value.getClass().isArray()) {
                    for (Object v : (Object[]) value) {
                        params.computeIfAbsent(key, k -> new ArrayList<>()).add(BlurObject.bind(v).toStringValue());
                    }
                } else if (value instanceof Collection) {
                    for (Object v : (Collection<?>) value) {
                        params.computeIfAbsent(key, k -> new ArrayList<>()).add(BlurObject.bind(v).toStringValue());
                    }
                } else {
                    params.computeIfAbsent(key, k -> new ArrayList<>()).add(BlurObject.bind(value).toStringValue());
                }
            }
        });
        Map<String, String[]> returnValue = new HashMap<>(params.size());
        params.forEach((key, value) -> returnValue.put(key, value.toArray(new String[0])));
        return returnValue;
    }

    /**
     * 解析远程模拟提交后返回的信息, 并将参数串转换成Map映射
     *
     * @param paramStr 要解析的字符串
     * @return 解析结果
     */
    public static Map<String, String[]> parseQueryParamStr(String paramStr) {
        return parseQueryParamStr(paramStr, false, null);
    }

    public static Map<String, String[]> parseQueryParamStr(String paramStr, boolean decode, String charset) {
        // 以“&”字符切割字符串
        String[] paramArr = StringUtils.split(paramStr, '&');
        // 把切割后的字符串数组变成变量与数值组合的字典数组
        Map<String, List<String>> params = new HashMap<>(paramArr.length);
        for (String param : paramArr) {
            //获得第一个=字符的位置
            int nPos = param.indexOf('=');
            //获得字符串长度
            int nLen = param.length();
            //获得变量名
            String strKey = param.substring(0, nPos);
            //获得数值
            String strValue = param.substring(nPos + 1, nLen);
            if (decode) {
                try {
                    strValue = URLDecoder.decode(strValue, StringUtils.defaultIfBlank(charset, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
                    }
                }
            }
            //放入MAP类中
            params.computeIfAbsent(strKey, key -> new ArrayList<>()).add(strValue);
        }
        Map<String, String[]> returnValue = new HashMap<>(params.size());
        params.forEach((key, value) -> returnValue.put(key, value.toArray(new String[0])));
        return returnValue;
    }

    public static String buildActionForm(String actionUrl, boolean usePost, Map<String, ?> params) {
        return buildActionForm(actionUrl, usePost, false, false, null, params);
    }

    public static String buildActionForm(String actionUrl, boolean usePost, boolean encode, boolean enctype, String charset, Map<String, ?> params) {
        String fixedCharset = StringUtils.defaultIfBlank(charset, "UTF-8");
        StringBuilder stringBuilder = new StringBuilder("<form id=\"_payment_submit\" name=\"_payment_submit\" action=\"")
                .append(actionUrl).append("\" method=\"")
                .append(usePost ? "POST" : "GET").append("\"");
        if (enctype) {
            stringBuilder.append("\" enctype=\"application/x-www-form-urlencoded;charset=").append(fixedCharset).append("\"");
        }
        stringBuilder.append(">");
        //
        params.forEach((key, value) -> {
            if (value != null) {
                if (value.getClass().isArray()) {
                    for (Object v : (Object[]) value) {
                        doAppendHiddenElement(stringBuilder, key, BlurObject.bind(v).toStringValue(), encode, fixedCharset);
                    }
                } else {
                    doAppendHiddenElement(stringBuilder, key, BlurObject.bind(value).toStringValue(), encode, fixedCharset);
                }
            }
        });
        // submit按钮控件请不要含有name属性
        stringBuilder.append("<input type=\"submit\" value=\"doSubmit\" style=\"display:none;\"></form>")
                .append("<script>document.forms['_payment_submit'].submit();</script>");
        return stringBuilder.toString();
    }

    private static void doAppendHiddenElement(StringBuilder stringBuilder, String key, String value, boolean encode, String charset) {
        if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
            String splitStr = StringUtils.contains(value, '\"') ? "'" : "\"";
            //
            stringBuilder.append("<input type=").append(splitStr).append("hidden").append(splitStr).append(" name=").append(splitStr).append(key).append(splitStr);
            //
            String valueStr = value;
            if (encode) {
                try {
                    valueStr = URLEncoder.encode(valueStr, charset);
                } catch (UnsupportedEncodingException e) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
                    }
                }
            }
            stringBuilder.append(" value=").append(splitStr).append(valueStr).append(splitStr).append(">");
        }
    }

    /**
     * @return 产生随机字符串，长度为6到32位不等
     */
    public static String createNonceStr() {
        return UUIDUtils.randomStr(UUIDUtils.randomInt(6, 32), false).toLowerCase();
    }

    /**
     * @param queryParamMap 请求协议参数对象映射
     * @param encode        是否进行编码
     * @param extraParams   扩展参数
     * @return 返回最终生成的签名
     */
    public static String createSignature(Map<String, ?> queryParamMap, boolean encode, String... extraParams) {
        return createSignature(queryParamMap, encode, true, null, extraParams);
    }

    public static String createSignature(Map<String, ?> queryParamMap, boolean encode, ISignatureBuilder signatureBuilder, String... extraParams) {
        return createSignature(queryParamMap, encode, true, signatureBuilder, extraParams);
    }

    public static String createSignature(Map<String, ?> queryParamMap, boolean encode, boolean upperCase, String... extraParams) {
        return createSignature(queryParamMap, encode, upperCase, null, extraParams);
    }

    public static String createSignature(Map<String, ?> queryParamMap, boolean encode, boolean upperCase, ISignatureBuilder signatureBuilder, String... extraParams) {
        StringBuilder stringBuilder = new StringBuilder(buildQueryParamStr(queryParamMap, encode, null));
        if (extraParams != null && extraParams.length > 0) {
            Arrays.stream(extraParams).forEachOrdered(extraParam -> stringBuilder.append("&").append(extraParam));
        }
        String signStr;
        if (signatureBuilder != null) {
            signStr = signatureBuilder.build(stringBuilder.toString());
        } else {
            signStr = DigestUtils.sha1Hex(stringBuilder.toString());
        }
        if (upperCase) {
            signStr = signStr.toUpperCase();
        }
        return signStr;
    }

    /**
     * 签名生成器接口
     *
     * @since 2.1.0
     */
    public interface ISignatureBuilder {

        /**
         * 生成签名
         *
         * @param content 待签内容
         * @return 返回签名字符串
         */
        String build(String content);
    }
}
