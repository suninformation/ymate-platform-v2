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

import net.ymate.platform.commons.http.impl.DefaultHttpResponse;
import org.apache.http.Header;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.FormBodyPartBuilder;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 基于HttpClient工具封装
 *
 * @author 刘镇 (suninformation@163.com) on 14/3/15 下午5:15:32
 * @see CloseableHttpClientHelper
 */
@Deprecated
public class HttpClientHelper extends AbstractHttpClientHelper<HttpClientHelper> {

    public static HttpClientHelper create() {
        return new HttpClientHelper();
    }

    public static HttpClientHelper create(IHttpClientConfigurable configurable) {
        return new HttpClientHelper(configurable);
    }

    private HttpClientHelper() {
        super(null);
    }

    private HttpClientHelper(IHttpClientConfigurable configurable) {
        super(configurable);
    }

    public CloseableHttpClient getHttpClient() {
        return httpClientConfigurable.createHttpClient(socketFactory, connectionTimeout, requestTimeout, socketTimeout);
    }

    public IHttpResponse execute(RequestBuilder requestBuilder, final String defaultResponseCharset) throws Exception {
        CloseableHttpClient httpClient = getHttpClient();
        try {
            return httpClient.execute(requestBuilder.build(), (ResponseHandler<IHttpResponse>) response -> new DefaultHttpResponse(response, defaultResponseCharset));
        } finally {
            if (httpClientConfigurable instanceof IHttpClientConfigurable) {
                ((IHttpClientConfigurable) httpClientConfigurable).closeHttpClient(httpClient);
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
            if (httpClientConfigurable instanceof IHttpClientConfigurable) {
                ((IHttpClientConfigurable) httpClientConfigurable).closeHttpClient(httpClient);
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
        RequestBuilder requestBuilder = doProcessRequestHeaders(url, headers);
        return execute(requestBuilder, defaultResponseCharset);
    }

    public IHttpResponse get(String url, Map<String, String> params) throws Exception {
        return get(url, params, null);
    }

    public IHttpResponse get(String url, Map<String, String> params, Header[] headers) throws Exception {
        return get(url, params, headers, null);
    }

    public IHttpResponse get(String url, Map<String, String> params, Header[] headers, String defaultResponseCharset) throws Exception {
        return get(url, params, StandardCharsets.UTF_8, headers, defaultResponseCharset);
    }

    public IHttpResponse get(String url, Map<String, String> params, Charset charset, Header[] headers, String defaultResponseCharset) throws Exception {
        RequestBuilder requestBuilder = doProcessRequestHeaders(url, headers, params);
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
        return execute(doProcessRequestHeaders(requestBuilder, headers, null), defaultResponseCharset);
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
        return execute(doProcessRequestHeaders(requestBuilder, headers, null), null);
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
        return execute(doProcessRequestHeaders(requestBuilder, headers, null), defaultResponseCharset);
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
                        .setParameters(doBuildNameValuePairs(params)).build());
        return execute(doProcessRequestHeaders(requestBuilder, headers, null), defaultResponseCharset);
    }

    public IHttpResponse post(String url, Map<String, String> params) throws Exception {
        return post(url, ContentType.create(CONTENT_TYPE_FORM_URL_ENCODED, DEFAULT_CHARSET), params, null, null);
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
        return execute(doProcessRequestHeaders(requestBuilder, headers, null), defaultResponseCharset);
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
            doDownload(httpClient, requestBuilder, handler);
        } finally {
            if (httpClientConfigurable instanceof IHttpClientConfigurable) {
                ((IHttpClientConfigurable) httpClientConfigurable).closeHttpClient(httpClient);
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
        execHttpDownload(doProcessRequestHeaders(requestBuilder, headers, null), handler);
    }

    public void download(String url, String content, IFileHandler handler) throws Exception {
        download(url, ContentType.create(CONTENT_TYPE_FORM_URL_ENCODED, DEFAULT_CHARSET), content, null, handler);
    }

    public void download(String url, Header[] headers, final IFileHandler handler) throws Exception {
        RequestBuilder requestBuilder = doProcessRequestHeaders(url, headers);
        execHttpDownload(requestBuilder, handler);
    }

    public void download(String url, IFileHandler handler) throws Exception {
        download(url, new Header[0], handler);
    }

    @Override
    public void close() throws IOException {
    }
}
