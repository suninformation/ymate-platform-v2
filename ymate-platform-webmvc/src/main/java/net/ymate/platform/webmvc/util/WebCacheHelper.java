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
package net.ymate.platform.webmvc.util;

import net.ymate.platform.cache.ICaches;
import net.ymate.platform.commons.lang.PairObject;
import net.ymate.platform.commons.util.DateTimeUtils;
import net.ymate.platform.webmvc.PageCacheElement;
import net.ymate.platform.webmvc.base.Type;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 16/3/28 下午7:56
 */
public class WebCacheHelper {

    private static final int EMPTY_GZIPPED_CONTENT_SIZE = 20;

    private final HttpServletRequest request;

    private final HttpServletResponse response;

    private final PageCacheElement pageCacheElement;

    private final ICaches.Scope scope;

    private WebCacheHelper(HttpServletRequest request, HttpServletResponse response, PageCacheElement pageCacheElement, ICaches.Scope scope) {
        this.request = request;
        this.response = response;
        this.pageCacheElement = pageCacheElement;
        this.scope = scope;
        //
        if (ICaches.Scope.DEFAULT.equals(this.scope)) {
            this.pageCacheElement.getHeaders().entrySet().removeIf(header -> "Last-Modified".equalsIgnoreCase(header.getKey()) || "Expires".equalsIgnoreCase(header.getKey()) || "Cache-Control".equalsIgnoreCase(header.getKey()) || "ETag".equalsIgnoreCase(header.getKey()));
            //
            long expiresTime = System.currentTimeMillis() + this.pageCacheElement.getTimeout() * DateTimeUtils.SECOND;
            //
            this.pageCacheElement.getHeaders().put("Last-Modified", PairObject.bind(Type.HeaderType.DATE, this.pageCacheElement.getLastUpdateTime()));
            this.pageCacheElement.getHeaders().put("Expires", PairObject.bind(Type.HeaderType.DATE, expiresTime));
            this.pageCacheElement.getHeaders().put("Cache-Control", PairObject.bind(Type.HeaderType.STRING, "max-age=" + this.pageCacheElement.getTimeout()));
            this.pageCacheElement.getHeaders().put("ETag", PairObject.bind(Type.HeaderType.STRING, String.format("\"%d\"", expiresTime)));
        }
    }

    public static WebCacheHelper bind(HttpServletRequest request, HttpServletResponse response, PageCacheElement pageCacheElement, ICaches.Scope scope) {
        return new WebCacheHelper(request, response, pageCacheElement, scope);
    }

    public void writeResponse() throws Exception {
        if (ICaches.Scope.DEFAULT.equals(scope)) {
            for (final Map.Entry<String, PairObject<Type.HeaderType, Object>> headerEntry : pageCacheElement.getHeaders().entrySet()) {
                if ("ETag".equals(headerEntry.getKey())) {
                    String ifNoneMatch = request.getHeader("If-None-Match");
                    if (headerEntry.getValue() != null && headerEntry.getValue().getValue().equals(ifNoneMatch)) {
                        response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
                        return;
                    }
                    break;
                }
                if (headerEntry.getValue().getValue() != null && "Last-Modified".equals(headerEntry.getKey())) {
                    long ifModifiedSince = request.getDateHeader("If-Modified-Since");
                    if (ifModifiedSince != -1) {
                        final Date requestDate = new Date(ifModifiedSince);
                        final Date pageDate;
                        switch (headerEntry.getValue().getKey()) {
                            case STRING:
                                pageDate = DateTimeUtils.parseDateTime((String) headerEntry.getValue().getValue(), "EEE, dd MMM yyyy HH:mm:ss z", "0");
                                break;
                            case DATE:
                                pageDate = new Date((Long) headerEntry.getValue().getValue());
                                break;
                            default:
                                throw new IllegalArgumentException(String.format("Header %s is not supported as type: %s", headerEntry, headerEntry.getValue().getKey()));
                        }
                        if (!requestDate.before(pageDate)) {
                            response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
                            response.setHeader("Last-Modified", request.getHeader("If-Modified-Since"));
                            return;
                        }
                    }
                }
            }
        }
        response.setContentType(pageCacheElement.getContentType());
        doSetHeaders();
        //
        byte[] body;
        if (pageCacheElement.isStoreGzipped() && StringUtils.contains(request.getHeader("Accept-Encoding"), "gzip")) {
            body = pageCacheElement.getGzippedBody();
            if (body.length == EMPTY_GZIPPED_CONTENT_SIZE) {
                body = new byte[0];
            } else {
                response.setHeader("Content-Encoding", "gzip");
            }
        } else {
            body = pageCacheElement.getUnGzippedBody();
        }
        response.setContentLength(body.length);
        OutputStream out = new BufferedOutputStream(response.getOutputStream());
        out.write(body);
        out.flush();
    }

    private void doSetHeaders() {
        pageCacheElement.getHeaders().forEach((name, value) -> {
            if (value != null) {
                switch (value.getKey()) {
                    case STRING:
                        if (response.containsHeader(name)) {
                            response.addHeader(name, (String) value.getValue());
                        } else {
                            response.setHeader(name, (String) value.getValue());
                        }
                        break;
                    case DATE:
                        if (response.containsHeader(name)) {
                            response.addDateHeader(name, (Long) value.getValue());
                        } else {
                            response.setDateHeader(name, (Long) value.getValue());
                        }
                        break;
                    case INT:
                        if (response.containsHeader(name)) {
                            response.addIntHeader(name, (Integer) value.getValue());
                        } else {
                            response.setIntHeader(name, (Integer) value.getValue());
                        }
                        break;
                    default:
                        throw new IllegalArgumentException(String.format("No mapping for Header: %s", value.getKey()));
                }
            }
        });
    }
}
