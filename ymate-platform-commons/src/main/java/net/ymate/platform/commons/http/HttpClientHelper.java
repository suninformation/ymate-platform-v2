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
package net.ymate.platform.commons.http;

import net.ymate.platform.commons.http.impl.DefaultFileWrapper;
import net.ymate.platform.commons.http.impl.DefaultHttpResponse;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.FormBodyPartBuilder;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 基于HttpClient工具封装
 *
 * @author 刘镇 (suninformation@163.com) on 14/3/15 下午5:15:32
 */
public class HttpClientHelper {

    /**
     * 编码方式
     */
    public static final String DEFAULT_CHARSET = "UTF-8";

    public static final int HTTP_STATUS_CODE_SUCCESS = 200;

    public static final String HEADER_CONTENT_DISPOSITION = "Content-disposition";

    public static final String CONTENT_TYPE_FORM_URL_ENCODED = "application/x-www-form-urlencoded";

    public static final String CONTENT_TYPE_TEXT_PLAIN = "text/plain";

    public static final String CONTENT_TYPE_OCTET_STREAM = "application/octet-stream";

    /**
     * 连接超时时间
     */
    private int connectionTimeout = -1;

    private int requestTimeout = -1;

    private int socketTimeout = -1;

    private SSLConnectionSocketFactory socketFactory;

    private IHttpClientConfigurable httpClientConfigurable;

    public static HttpClientHelper create() {
        return new HttpClientHelper();
    }

    public static HttpClientHelper create(IHttpClientConfigurable configurable) {
        return new HttpClientHelper(configurable);
    }

    public static SSLConnectionSocketFactory createConnectionSocketFactory(URL certFilePath, char[] passwordChars)
            throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        return createConnectionSocketFactory("PKCS12", certFilePath, passwordChars);
    }

    public static SSLConnectionSocketFactory createConnectionSocketFactory(String certType, URL certFilePath, char[] passwordChars)
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
        return new SSLConnectionSocketFactory(sslContext, new String[]{"TLSv1"}, null, new DefaultHostnameVerifier());
    }

    private HttpClientHelper() {
    }

    private HttpClientHelper(IHttpClientConfigurable configurable) {
        httpClientConfigurable = configurable;
    }

    public HttpClientHelper customSSL(SSLConnectionSocketFactory socketFactory) {
        this.socketFactory = socketFactory;
        return this;
    }

    public HttpClientHelper connectionTimeout(int connectionTimeout) {
        if (connectionTimeout > -1) {
            this.connectionTimeout = connectionTimeout;
        }
        return this;
    }

    public HttpClientHelper requestTimeout(int requestTimeout) {
        if (requestTimeout > -1) {
            this.requestTimeout = requestTimeout;
        }
        return this;
    }

    public HttpClientHelper socketTimeout(int socketTimeout) {
        if (socketTimeout > -1) {
            this.socketTimeout = socketTimeout;
        }
        return this;
    }

    public CloseableHttpClient getHttpClient() {
        CloseableHttpClient httpClient = httpClientConfigurable != null ? httpClientConfigurable.createHttpClient(socketFactory, connectionTimeout, requestTimeout, socketTimeout) : null;
        if (httpClient == null) {
            HttpClientBuilder clientBuilder = HttpClientBuilder.create()
                    .setDefaultRequestConfig(RequestConfig.custom()
                            .setConnectTimeout(connectionTimeout)
                            .setSocketTimeout(socketTimeout)
                            .setConnectionRequestTimeout(requestTimeout).build());
            if (socketFactory == null) {
                socketFactory = new SSLConnectionSocketFactory(SSLContexts.createSystemDefault(), NoopHostnameVerifier.INSTANCE);
            }
            httpClient = clientBuilder.setSSLSocketFactory(socketFactory).build();
        }
        return httpClient;
    }

    private RequestBuilder processRequestHeaders(String url, Header[] headers) {
        return processRequestHeaders(RequestBuilder.get().setUri(url), headers, null);
    }

    private RequestBuilder processRequestHeaders(String url, Header[] headers, Map<String, String> params) {
        return processRequestHeaders(RequestBuilder.get().setUri(url), headers, params);
    }

    private RequestBuilder processRequestHeaders(RequestBuilder requestBuilder, Header[] headers, Map<String, String> params) {
        if (headers != null && headers.length > 0) {
            Arrays.stream(headers).forEachOrdered(requestBuilder::addHeader);
        }
        if (params != null && !params.isEmpty()) {
            params.forEach(requestBuilder::addParameter);
        }
        return requestBuilder;
    }

    public IHttpResponse execute(RequestBuilder requestBuilder, final String defaultResponseCharset) throws Exception {
        CloseableHttpClient httpClient = getHttpClient();
        try {
            return httpClient.execute(requestBuilder.build(), (ResponseHandler<IHttpResponse>) response -> new DefaultHttpResponse(response, defaultResponseCharset));
        } finally {
            if (httpClientConfigurable != null) {
                httpClientConfigurable.closeHttpClient(httpClient);
            } else {
                httpClient.close();
            }
        }
    }

    public <T> T execute(IHttpRequestExecutor<T> requestExecutor) throws Exception {
        CloseableHttpClient httpClient = getHttpClient();
        try {
            return requestExecutor.execute(httpClient);
        } finally {
            if (httpClientConfigurable != null) {
                httpClientConfigurable.closeHttpClient(httpClient);
            } else {
                httpClient.close();
            }
        }
    }

    public IHttpResponse get(String url) throws Exception {
        return get(url, new Header[0], null);
    }

    public IHttpResponse get(String url, Header[] headers) throws Exception {
        return get(url, headers, null);
    }

    public IHttpResponse get(String url, Header[] headers, String defaultResponseCharset) throws Exception {
        RequestBuilder requestBuilder = processRequestHeaders(url, headers);
        return execute(requestBuilder, defaultResponseCharset);
    }

    public IHttpResponse get(String url, Map<String, String> params) throws Exception {
        return get(url, params, null);
    }

    public IHttpResponse get(String url, Map<String, String> params, Header[] headers) throws Exception {
        return get(url, params, headers, null);
    }

    public IHttpResponse get(String url, Map<String, String> params, Header[] headers, String defaultResponseCharset) throws Exception {
        return get(url, params, Charset.forName(DEFAULT_CHARSET), headers, defaultResponseCharset);
    }

    public IHttpResponse get(String url, Map<String, String> params, Charset charset, Header[] headers, String defaultResponseCharset) throws Exception {
        RequestBuilder requestBuilder = processRequestHeaders(url, headers, params);
        if (charset != null) {
            requestBuilder.setCharset(charset);
        }
        return execute(requestBuilder, defaultResponseCharset);
    }

    public IHttpResponse post(String url, ContentType contentType, String content, Header[] headers) throws Exception {
        return post(url, contentType, content, headers, null);
    }

    public IHttpResponse post(String url, ContentType contentType, String content, Header[] headers, String defaultResponseCharset) throws Exception {
        RequestBuilder requestBuilder = RequestBuilder.post()
                .setUri(url)
                .setEntity(EntityBuilder.create()
                        .setContentEncoding(contentType == null || contentType.getCharset() == null ? DEFAULT_CHARSET : contentType.getCharset().name())
                        .setContentType(contentType)
                        .setText(content).build());
        requestBuilder = processRequestHeaders(requestBuilder, headers, null);
        return execute(requestBuilder, defaultResponseCharset);
    }

    public IHttpResponse post(String url, ContentType contentType, String content) throws Exception {
        return post(url, contentType, content, null, null);
    }

    public IHttpResponse post(String url, String content) throws Exception {
        return post(url, ContentType.create(CONTENT_TYPE_TEXT_PLAIN, DEFAULT_CHARSET), content, null, null);
    }

    public IHttpResponse post(String url, ContentType contentType, byte[] content, Header[] headers) throws Exception {
        RequestBuilder requestBuilder = RequestBuilder.post()
                .setUri(url)
                .setEntity(EntityBuilder.create()
                        .setContentEncoding(contentType == null || contentType.getCharset() == null ? DEFAULT_CHARSET : contentType.getCharset().name())
                        .setContentType(contentType)
                        .setBinary(content).build());
        requestBuilder = processRequestHeaders(requestBuilder, headers, null);
        return execute(requestBuilder, null);
    }

    public IHttpResponse post(String url, ContentType contentType, InputStream content, Header[] headers) throws Exception {
        return post(url, contentType, content, headers, null);
    }

    public IHttpResponse post(String url, ContentType contentType, InputStream content, Header[] headers, String defaultResponseCharset) throws Exception {
        RequestBuilder requestBuilder = RequestBuilder.post()
                .setUri(url)
                .setEntity(EntityBuilder.create()
                        .setContentEncoding(contentType == null || contentType.getCharset() == null ? DEFAULT_CHARSET : contentType.getCharset().name())
                        .setContentType(contentType)
                        .setStream(content).build());
        requestBuilder = processRequestHeaders(requestBuilder, headers, null);
        return execute(requestBuilder, defaultResponseCharset);
    }

    public IHttpResponse post(String url, ContentType contentType, byte[] content) throws Exception {
        return post(url, contentType, content, null);
    }

    public IHttpResponse post(String url, byte[] content) throws Exception {
        return post(url, ContentType.create(CONTENT_TYPE_OCTET_STREAM, DEFAULT_CHARSET), content, null);
    }

    public IHttpResponse post(String url, Map<String, String> params, Header[] headers) throws Exception {
        return post(url, null, params, headers, null);
    }

    public IHttpResponse post(String url, ContentType contentType, Map<String, String> params, Header[] headers) throws Exception {
        return post(url, contentType, params, headers, null);
    }

    public IHttpResponse post(String url, ContentType contentType, Map<String, String> params, Header[] headers, String defaultResponseCharset) throws Exception {
        RequestBuilder requestBuilder = RequestBuilder.post()
                .setUri(url)
                .setEntity(EntityBuilder.create()
                        .setContentType(contentType)
                        .setContentEncoding(contentType == null || contentType.getCharset() == null ? DEFAULT_CHARSET : contentType.getCharset().name())
                        .setParameters(buildNameValuePairs(params)).build());
        requestBuilder = processRequestHeaders(requestBuilder, headers, null);
        return execute(requestBuilder, defaultResponseCharset);
    }

    public IHttpResponse post(String url, Map<String, String> params) throws Exception {
        return post(url, ContentType.create(CONTENT_TYPE_FORM_URL_ENCODED, DEFAULT_CHARSET), params, null, null);
    }

    private static List<NameValuePair> buildNameValuePairs(Map<String, String> params) {
        List<NameValuePair> nameValuePair = new ArrayList<>();
        params.forEach((key, value) -> {
            if (StringUtils.isNotBlank(value)) {
                nameValuePair.add(new BasicNameValuePair(key, value));
            }
        });
        return nameValuePair;
    }

    public IHttpResponse upload(String url, String fieldName, ContentBody contentBody, Header[] headers) throws Exception {
        return upload(url, fieldName, contentBody, headers, null);
    }

    public IHttpResponse upload(String url, String fieldName, ContentBody contentBody, Header[] headers, String defaultResponseCharset) throws Exception {
        return upload(url, new FormBodyPart[]{FormBodyPartBuilder.create(fieldName, contentBody).build()}, headers, defaultResponseCharset);
    }

    public IHttpResponse upload(String url, FormBodyPart[] formBodyParts, Header[] headers, String defaultResponseCharset) throws Exception {
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        for (FormBodyPart formBodyPart : formBodyParts) {
            multipartEntityBuilder.addPart(formBodyPart);
        }
        RequestBuilder requestBuilder = RequestBuilder.post()
                .setUri(url)
                .setEntity(multipartEntityBuilder.build());
        requestBuilder = processRequestHeaders(requestBuilder, headers, null);
        return execute(requestBuilder, defaultResponseCharset);
    }

    public IHttpResponse upload(String url, String fieldName, File uploadFile, Header[] headers) throws Exception {
        return upload(url, fieldName, new FileBody(uploadFile), headers, null);
    }

    public IHttpResponse upload(String url, File uploadFile, Header[] headers) throws Exception {
        return upload(url, "media", uploadFile, headers);
    }

    public IHttpResponse upload(String url, String fieldName, File uploadFile) throws Exception {
        return upload(url, fieldName, uploadFile, null);
    }

    public IHttpResponse upload(String url, File uploadFile) throws Exception {
        return upload(url, uploadFile, null);
    }

    public void execHttpDownload(RequestBuilder requestBuilder, final IFileHandler handler) throws Exception {
        CloseableHttpClient httpClient = getHttpClient();
        try {
            httpClient.execute(requestBuilder.build(), response -> {
                String fileName = null;
                if (response.getStatusLine().getStatusCode() == HTTP_STATUS_CODE_SUCCESS) {
                    if (response.containsHeader(HEADER_CONTENT_DISPOSITION)) {
                        fileName = StringUtils.substringAfter(response.getFirstHeader(HEADER_CONTENT_DISPOSITION).getValue(), "filename=");
                    }
                }
                handler.handle(response, new DefaultFileWrapper(fileName, response.getEntity().getContentType().getValue(), response.getEntity().getContentLength(), new BufferedInputStream(response.getEntity().getContent())));
                return null;
            });
        } finally {
            if (httpClientConfigurable != null) {
                httpClientConfigurable.closeHttpClient(httpClient);
            } else {
                httpClient.close();
            }
        }
    }

    public void download(String url, ContentType contentType, String content, Header[] headers, final IFileHandler handler) throws Exception {
        RequestBuilder requestBuilder = RequestBuilder.post()
                .setUri(url)
                .setEntity(EntityBuilder.create()
                        .setContentEncoding(contentType == null || contentType.getCharset() == null ? DEFAULT_CHARSET : contentType.getCharset().name())
                        .setContentType(contentType)
                        .setText(content).build());
        execHttpDownload(processRequestHeaders(requestBuilder, headers, null), handler);
    }

    public void download(String url, String content, IFileHandler handler) throws Exception {
        download(url, ContentType.create(CONTENT_TYPE_FORM_URL_ENCODED, DEFAULT_CHARSET), content, null, handler);
    }

    public void download(String url, Header[] headers, final IFileHandler handler) throws Exception {
        RequestBuilder requestBuilder = processRequestHeaders(url, headers);
        execHttpDownload(requestBuilder, handler);
    }

    public void download(String url, IFileHandler handler) throws Exception {
        download(url, new Header[0], handler);
    }
}
