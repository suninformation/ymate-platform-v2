/*
 * Copyright 2007-2024 the original author or authors.
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
package net.ymate.platform.commons.http;

import net.ymate.platform.commons.http.impl.DefaultFileWrapper;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2024/5/13 02:30
 * @since 2.1.3
 */
public abstract class AbstractHttpClientHelper<T extends AbstractHttpClientHelper<?>> implements Closeable {

    /**
     * 编码方式
     */
    public static final String DEFAULT_CHARSET = "UTF-8";

    public static final int HTTP_STATUS_CODE_SUCCESS = 200;

    public static final String HEADER_CONTENT_DISPOSITION = "Content-disposition";

    public static final String CONTENT_TYPE_FORM_URL_ENCODED = "application/x-www-form-urlencoded";

    public static final String CONTENT_TYPE_TEXT_PLAIN = "text/plain";

    public static final String CONTENT_TYPE_OCTET_STREAM = "application/octet-stream";

    public static String parseFileName(HttpResponse httpResponse) {
        String fileName = null;
        if (httpResponse.containsHeader(HEADER_CONTENT_DISPOSITION)) {
            fileName = StringUtils.replace(StringUtils.substringAfter(httpResponse.getFirstHeader(HEADER_CONTENT_DISPOSITION).getValue(), "filename="), "\"", StringUtils.EMPTY);
        }
        return fileName;
    }

    public static SSLConnectionSocketFactory createConnectionSocketFactory(URL certFilePath, char[] passwordChars)
            throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        return createConnectionSocketFactory("PKCS12", certFilePath, passwordChars);
    }

    public static SSLConnectionSocketFactory createConnectionSocketFactory(String certType, URL certFilePath, char[] passwordChars)
            throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException {
        return createConnectionSocketFactory(certType, certFilePath, passwordChars, new String[]{"TLSv1"}, null);
    }

    public static SSLConnectionSocketFactory createConnectionSocketFactory(String certType, URL certFilePath, char[] passwordChars, String[] supportedProtocols, String[] supportedCipherSuites)
            throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException {
        if (StringUtils.isBlank(certType)) {
            throw new NullArgumentException("certType");
        }
        if (certFilePath == null) {
            throw new NullArgumentException("certFilePath");
        }
        if (ArrayUtils.isEmpty(passwordChars)) {
            throw new NullArgumentException("passwordChars");
        }
        KeyStore keyStore = KeyStore.getInstance(certType);
        try (InputStream certFileStream = certFilePath.openStream()) {
            keyStore.load(certFileStream, passwordChars);
        }
        SSLContext sslContext = SSLContexts.custom().loadKeyMaterial(keyStore, passwordChars).build();
        return createConnectionSocketFactory(sslContext, supportedProtocols, supportedCipherSuites, new DefaultHostnameVerifier());
    }

    public static SSLConnectionSocketFactory createConnectionSocketFactory(SSLContext sslContext, String[] supportedProtocols, String[] supportedCipherSuites, HostnameVerifier hostnameVerifier) {
        if (sslContext == null) {
            sslContext = SSLContexts.createSystemDefault();
        }
        if (hostnameVerifier == null) {
            hostnameVerifier = new DefaultHostnameVerifier();
        }
        return new SSLConnectionSocketFactory(sslContext, supportedProtocols, supportedCipherSuites, hostnameVerifier);
    }

    public static SSLConnectionSocketFactory createConnectionSocketFactory(SSLContext sslContext, String[] supportedProtocols, String[] supportedCipherSuites) {
        return createConnectionSocketFactory(sslContext, supportedProtocols, supportedCipherSuites, null);
    }

    public static SSLConnectionSocketFactory createConnectionSocketFactory(SSLContext sslContext) {
        return createConnectionSocketFactory(sslContext, null, null, null);
    }

    public static SSLConnectionSocketFactory createConnectionSocketFactory(String[] supportedProtocols, String[] supportedCipherSuites) {
        return createConnectionSocketFactory(null, supportedProtocols, supportedCipherSuites, null);
    }

    /**
     * 连接超时时间
     */
    protected int connectionTimeout = -1;

    protected int requestTimeout = -1;

    protected int socketTimeout = -1;

    protected SSLConnectionSocketFactory socketFactory;

    protected final ICloseableHttpClientConfigurable httpClientConfigurable;

    protected AbstractHttpClientHelper(ICloseableHttpClientConfigurable configurable) {
        httpClientConfigurable = configurable != null ? configurable : new ICloseableHttpClientConfigurable.Default();
    }

    @SuppressWarnings("unchecked")
    public T customSSL(SSLConnectionSocketFactory socketFactory) {
        this.socketFactory = socketFactory;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T connectionTimeout(int connectionTimeout) {
        if (connectionTimeout > -1) {
            this.connectionTimeout = connectionTimeout;
        }
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T requestTimeout(int requestTimeout) {
        if (requestTimeout > -1) {
            this.requestTimeout = requestTimeout;
        }
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T socketTimeout(int socketTimeout) {
        if (socketTimeout > -1) {
            this.socketTimeout = socketTimeout;
        }
        return (T) this;
    }

    protected RequestBuilder doProcessRequestHeaders(String url, Header[] headers) {
        return doProcessRequestHeaders(RequestBuilder.get().setUri(url), headers, null);
    }

    protected RequestBuilder doProcessRequestHeaders(String url, Header[] headers, Map<String, String> params) {
        return doProcessRequestHeaders(RequestBuilder.get().setUri(url), headers, params);
    }

    protected RequestBuilder doProcessRequestHeaders(RequestBuilder requestBuilder, Header[] headers, Map<String, String> params) {
        if (headers != null && headers.length > 0) {
            Arrays.stream(headers).forEachOrdered(requestBuilder::addHeader);
        }
        if (params != null && !params.isEmpty()) {
            params.forEach(requestBuilder::addParameter);
        }
        return requestBuilder;
    }

    protected List<NameValuePair> doBuildNameValuePairs(Map<String, String> params) {
        List<NameValuePair> nameValuePair = new ArrayList<>();
        params.forEach((key, value) -> {
            if (StringUtils.isNotBlank(value)) {
                nameValuePair.add(new BasicNameValuePair(key, value));
            }
        });
        return nameValuePair;
    }

    protected void doDownload(HttpClient httpClient, RequestBuilder requestBuilder, IFileHandler handler) throws Exception {
        httpClient.execute(requestBuilder.build(), response -> {
            IFileWrapper fileWrapper = null;
            if (response.getStatusLine().getStatusCode() == HTTP_STATUS_CODE_SUCCESS) {
                String fileName = parseFileName(response);
                fileWrapper = new DefaultFileWrapper(fileName, response.getEntity().getContentType().getValue(), response.getEntity().getContentLength(), new BufferedInputStream(response.getEntity().getContent()));
            }
            handler.handle(response, fileWrapper);
            return null;
        });
    }
}
