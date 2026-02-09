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
package net.ymate.platform.mock.web;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class MockHttpServletResponse implements HttpServletResponse {

    private static final String CHARSET_PREFIX = "charset=";

    private static final String CONTENT_TYPE_HEADER = "Content-Type";

    private static final String CONTENT_LENGTH_HEADER = "Content-Length";

    private static final String LOCATION_HEADER = "Location";

    private static final String DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";

    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");

    private boolean outputStreamAccessAllowed = true;

    private boolean writerAccessAllowed = true;

    private String characterEncoding = "UTF-8";

    private boolean charset = false;

    private final ByteArrayOutputStream content = new ByteArrayOutputStream(1024);

    private final ServletOutputStream outputStream = new ResponseServletOutputStream(this.content);

    private PrintWriter writer;

    private long contentLength = 0;

    private String contentType = "text/plain";

    private int bufferSize = 4096;

    private boolean committed;

    private Locale locale = Locale.getDefault();

    private final List<Cookie> cookies = new ArrayList<>();

    private final Map<String, HeaderValueHolder> headers = new LinkedHashMap<>();

    private int status = HttpServletResponse.SC_OK;

    private String errorMessage;

    private String forwardedUrl;

    private final List<String> includedUrls = new ArrayList<>();

    public void setOutputStreamAccessAllowed(boolean outputStreamAccessAllowed) {
        this.outputStreamAccessAllowed = outputStreamAccessAllowed;
    }

    public boolean isOutputStreamAccessAllowed() {
        return this.outputStreamAccessAllowed;
    }

    public void setWriterAccessAllowed(boolean writerAccessAllowed) {
        this.writerAccessAllowed = writerAccessAllowed;
    }

    public boolean isWriterAccessAllowed() {
        return this.writerAccessAllowed;
    }

    public boolean isCharset() {
        return this.charset;
    }

    @Override
    public void setCharacterEncoding(String characterEncoding) {
        this.characterEncoding = characterEncoding;
        this.charset = true;
        updateContentTypeHeader();
    }

    private void updateContentTypeHeader() {
        if (this.contentType != null) {
            String value = this.contentType;
            if (this.charset && !value.toLowerCase().contains(CHARSET_PREFIX)) {
                value = value + ';' + CHARSET_PREFIX + this.characterEncoding;
            }
            doAddHeaderValue(CONTENT_TYPE_HEADER, value, true);
        }
    }

    @Override
    public String getCharacterEncoding() {
        return this.characterEncoding;
    }

    @Override
    public ServletOutputStream getOutputStream() {
        if (!this.outputStreamAccessAllowed) {
            throw new IllegalStateException("OutputStream access not allowed");
        }
        return this.outputStream;
    }

    @Override
    public PrintWriter getWriter() throws UnsupportedEncodingException {
        if (!this.writerAccessAllowed) {
            throw new IllegalStateException("Writer access not allowed");
        }
        if (this.writer == null) {
            Writer targetWriter = (this.characterEncoding != null ?
                    new OutputStreamWriter(this.content, this.characterEncoding) :
                    new OutputStreamWriter(this.content));
            this.writer = new ResponsePrintWriter(targetWriter);
        }
        return this.writer;
    }

    public byte[] getContentAsByteArray() {
        flushBuffer();
        return this.content.toByteArray();
    }

    public String getContentAsString() throws UnsupportedEncodingException {
        flushBuffer();
        return (this.characterEncoding != null ?
                this.content.toString(this.characterEncoding) : this.content.toString());
    }

    public void write(byte[] b) throws IOException {
        getOutputStream().write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        getOutputStream().write(b, off, len);
    }

    @Override
    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
        doAddHeaderValue(CONTENT_LENGTH_HEADER, contentLength, true);
    }

    public int getContentLength() {
        return (int) this.contentLength;
    }

    public void setContentLengthLong(long contentLength) {
        this.contentLength = contentLength;
        doAddHeaderValue(CONTENT_LENGTH_HEADER, contentLength, true);
    }

    public long getContentLengthLong() {
        return this.contentLength;
    }

    @Override
    public void setContentType(String contentType) {
        this.contentType = contentType;
        if (contentType != null) {
            int charsetIndex = contentType.toLowerCase().indexOf(CHARSET_PREFIX);
            if (charsetIndex != -1) {
                this.characterEncoding = contentType.substring(charsetIndex + CHARSET_PREFIX.length());
                this.charset = true;
            }
            updateContentTypeHeader();
        }
    }

    @Override
    public String getContentType() {
        return this.contentType;
    }

    @Override
    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    @Override
    public int getBufferSize() {
        return this.bufferSize;
    }

    @Override
    public void flushBuffer() {
        setCommitted(true);
    }

    @Override
    public void resetBuffer() {
        if (isCommitted()) {
            throw new IllegalStateException("Cannot reset buffer - response is already committed");
        }
        this.content.reset();
    }

    private void setCommittedIfBufferSizeExceeded() {
        int bufSize = getBufferSize();
        if (bufSize > 0 && this.content.size() > bufSize) {
            setCommitted(true);
        }
    }

    public void setCommitted(boolean committed) {
        this.committed = committed;
    }

    @Override
    public boolean isCommitted() {
        return this.committed;
    }

    @Override
    public void reset() {
        resetBuffer();
        this.characterEncoding = null;
        this.charset = false;
        this.contentLength = 0;
        this.contentType = null;
        this.locale = null;
        this.cookies.clear();
        this.headers.clear();
        this.status = HttpServletResponse.SC_OK;
        this.errorMessage = null;
    }

    @Override
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    @Override
    public Locale getLocale() {
        return this.locale;
    }

    @Override
    public void addCookie(Cookie cookie) {
        Objects.requireNonNull(cookie, "Cookie must not be null");
        this.cookies.add(cookie);
    }

    public Cookie[] getCookies() {
        return this.cookies.toArray(new Cookie[0]);
    }

    public Cookie getCookie(String name) {
        Objects.requireNonNull(name, "Cookie name must not be null");
        for (Cookie cookie : this.cookies) {
            if (name.equals(cookie.getName())) {
                return cookie;
            }
        }
        return null;
    }

    @Override
    public boolean containsHeader(String name) {
        return (HeaderValueHolder.getByName(this.headers, name) != null);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return this.headers.keySet();
    }

    @Override
    public String getHeader(String name) {
        HeaderValueHolder header = HeaderValueHolder.getByName(this.headers, name);
        return (header != null ? header.getStringValue() : null);
    }

    @Override
    public List<String> getHeaders(String name) {
        HeaderValueHolder header = HeaderValueHolder.getByName(this.headers, name);
        if (header != null) {
            return header.getStringValues();
        } else {
            return Collections.emptyList();
        }
    }

    public Object getHeaderValue(String name) {
        HeaderValueHolder header = HeaderValueHolder.getByName(this.headers, name);
        return (header != null ? header.getValue() : null);
    }

    public List<Object> getHeaderValues(String name) {
        HeaderValueHolder header = HeaderValueHolder.getByName(this.headers, name);
        if (header != null) {
            return header.getValues();
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public String encodeURL(String url) {
        return url;
    }

    @Override
    public String encodeRedirectURL(String url) {
        return encodeURL(url);
    }

    @Override
    @Deprecated
    public String encodeUrl(String url) {
        return encodeURL(url);
    }

    @Override
    @Deprecated
    public String encodeRedirectUrl(String url) {
        return encodeRedirectURL(url);
    }

    @Override
    public void sendError(int status, String errorMessage) throws IOException {
        if (isCommitted()) {
            throw new IllegalStateException("Cannot set error status - response is already committed");
        }
        this.status = status;
        this.errorMessage = errorMessage;
        setCommitted(true);
    }

    @Override
    public void sendError(int status) throws IOException {
        if (isCommitted()) {
            throw new IllegalStateException("Cannot set error status - response is already committed");
        }
        this.status = status;
        setCommitted(true);
    }

    @Override
    public void sendRedirect(String url) throws IOException {
        if (isCommitted()) {
            throw new IllegalStateException("Cannot send redirect - response is already committed");
        }
        Objects.requireNonNull(url, "Redirect URL must not be null");
        setHeader(LOCATION_HEADER, url);
        setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
        setCommitted(true);
    }

    public String getRedirectedUrl() {
        return getHeader(LOCATION_HEADER);
    }

    @Override
    public void setDateHeader(String name, long value) {
        setHeaderValue(name, formatDate(value));
    }

    @Override
    public void addDateHeader(String name, long value) {
        addHeaderValue(name, formatDate(value));
    }

    public long getDateHeader(String name) {
        String headerValue = getHeader(name);
        if (headerValue == null) {
            return -1;
        }
        try {
            return newDateFormat().parse(getHeader(name)).getTime();
        } catch (ParseException ex) {
            throw new IllegalArgumentException("Value for header '" + name + "' is not a valid Date: " + headerValue);
        }
    }

    public int getIntHeader(String name) {
        String headerValue = getHeader(name);
        if (headerValue == null) {
            return -1;
        }
        try {
            return Integer.parseInt(headerValue);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Value for header '" + name + "' is not a valid integer: " + headerValue);
        }
    }

    private String formatDate(long date) {
        return newDateFormat().format(new Date(date));
    }

    private DateFormat newDateFormat() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.US);
        dateFormat.setTimeZone(GMT);
        return dateFormat;
    }

    @Override
    public void setHeader(String name, String value) {
        setHeaderValue(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        addHeaderValue(name, value);
    }

    @Override
    public void setIntHeader(String name, int value) {
        setHeaderValue(name, value);
    }

    @Override
    public void addIntHeader(String name, int value) {
        addHeaderValue(name, value);
    }

    private void setHeaderValue(String name, Object value) {
        if (setSpecialHeader(name, value)) {
            return;
        }
        doAddHeaderValue(name, value, true);
    }

    private void addHeaderValue(String name, Object value) {
        if (setSpecialHeader(name, value)) {
            return;
        }
        doAddHeaderValue(name, value, false);
    }

    private boolean setSpecialHeader(String name, Object value) {
        if (CONTENT_TYPE_HEADER.equalsIgnoreCase(name)) {
            setContentType(value.toString());
            return true;
        } else if (CONTENT_LENGTH_HEADER.equalsIgnoreCase(name)) {
            setContentLength(value instanceof Number ? ((Number) value).intValue() : Integer.parseInt(value.toString()));
            return true;
        } else {
            return false;
        }
    }

    private void doAddHeaderValue(String name, Object value, boolean replace) {
        HeaderValueHolder header = HeaderValueHolder.getByName(this.headers, name);
        Objects.requireNonNull(value, "Header value must not be null");
        if (header == null) {
            header = new HeaderValueHolder();
            this.headers.put(name, header);
        }
        if (replace) {
            header.setValue(value);
        } else {
            header.addValue(value);
        }
    }

    @Override
    public void setStatus(int status) {
        if (!this.isCommitted()) {
            this.status = status;
        }
    }

    @Override
    @Deprecated
    public void setStatus(int status, String errorMessage) {
        if (!this.isCommitted()) {
            this.status = status;
            this.errorMessage = errorMessage;
        }
    }

    @Override
    public int getStatus() {
        return this.status;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public void setForwardedUrl(String forwardedUrl) {
        this.forwardedUrl = forwardedUrl;
    }

    public String getForwardedUrl() {
        return this.forwardedUrl;
    }

    public void setIncludedUrl(String includedUrl) {
        this.includedUrls.clear();
        if (includedUrl != null) {
            this.includedUrls.add(includedUrl);
        }
    }

    public String getIncludedUrl() {
        int count = this.includedUrls.size();
        if (count > 1) {
            throw new IllegalStateException("More than 1 URL included - check getIncludedUrls instead: " + this.includedUrls);
        }
        return (count == 1 ? this.includedUrls.get(0) : null);
    }

    public void addIncludedUrl(String includedUrl) {
        Objects.requireNonNull(includedUrl, "Included URL must not be null");
        this.includedUrls.add(includedUrl);
    }

    public List<String> getIncludedUrls() {
        return this.includedUrls;
    }

    private class ResponseServletOutputStream extends DelegatingServletOutputStream {

        public ResponseServletOutputStream(OutputStream out) {
            super(out);
        }

        @Override
        public void write(int b) throws IOException {
            super.write(b);
            super.flush();
            setCommittedIfBufferSizeExceeded();
        }

        @Override
        public void write(byte[] b) throws IOException {
            super.write(b);
            super.flush();
            setCommittedIfBufferSizeExceeded();
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            super.write(b, off, len);
            super.flush();
            setCommittedIfBufferSizeExceeded();
        }

        @Override
        public void flush() throws IOException {
            super.flush();
            setCommitted(true);
        }
    }

    private class ResponsePrintWriter extends PrintWriter {

        public ResponsePrintWriter(Writer out) {
            super(out, true);
        }

        @Override
        public void write(char[] buf, int off, int len) {
            super.write(buf, off, len);
            super.flush();
            setCommittedIfBufferSizeExceeded();
        }

        @Override
        public void write(String s, int off, int len) {
            super.write(s, off, len);
            super.flush();
            setCommittedIfBufferSizeExceeded();
        }

        @Override
        public void write(int c) {
            super.write(c);
            super.flush();
            setCommittedIfBufferSizeExceeded();
        }

        @Override
        public void flush() {
            super.flush();
            setCommitted(true);
        }
    }

    public static class Builder {
        private String characterEncoding = "UTF-8";
        private boolean charset = false;
        private long contentLength = 0;
        private String contentType = "text/plain";
        private int bufferSize = 4096;
        private boolean committed = false;
        private Locale locale = Locale.getDefault();
        private final List<Cookie> cookies = new ArrayList<>();
        private final Map<String, HeaderValueHolder> headers = new LinkedHashMap<>();
        private int status = HttpServletResponse.SC_OK;
        private String errorMessage;
        private String forwardedUrl;
        private final List<String> includedUrls = new ArrayList<>();

        public static Builder create() {
            return new Builder();
        }

        public Builder characterEncoding(String characterEncoding) {
            this.characterEncoding = characterEncoding;
            this.charset = true;
            return this;
        }

        public Builder contentLength(int contentLength) {
            this.contentLength = contentLength;
            return this;
        }

        public Builder contentLengthLong(long contentLength) {
            this.contentLength = contentLength;
            return this;
        }

        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder bufferSize(int bufferSize) {
            this.bufferSize = bufferSize;
            return this;
        }

        public Builder committed(boolean committed) {
            this.committed = committed;
            return this;
        }

        public Builder locale(Locale locale) {
            this.locale = locale;
            return this;
        }

        public Builder cookie(Cookie cookie) {
            Objects.requireNonNull(cookie, "Cookie must not be null");
            this.cookies.add(cookie);
            return this;
        }

        public Builder cookies(Cookie... cookies) {
            if (cookies != null) {
                Collections.addAll(this.cookies, cookies);
            }
            return this;
        }

        public Builder header(String name, Object value) {
            Objects.requireNonNull(name, "Header name must not be null");
            Objects.requireNonNull(value, "Header value must not be null");
            HeaderValueHolder header = this.headers.get(name);
            if (header == null) {
                header = new HeaderValueHolder();
                this.headers.put(name, header);
            }
            header.setValue(value);
            return this;
        }

        public Builder addHeader(String name, Object value) {
            Objects.requireNonNull(name, "Header name must not be null");
            Objects.requireNonNull(value, "Header value must not be null");
            HeaderValueHolder header = this.headers.get(name);
            if (header == null) {
                header = new HeaderValueHolder();
                this.headers.put(name, header);
            }
            header.addValue(value);
            return this;
        }

        public Builder status(int status) {
            this.status = status;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder forwardedUrl(String forwardedUrl) {
            this.forwardedUrl = forwardedUrl;
            return this;
        }

        public Builder includedUrl(String includedUrl) {
            Objects.requireNonNull(includedUrl, "Included URL must not be null");
            this.includedUrls.add(includedUrl);
            return this;
        }

        public Builder includedUrls(String... includedUrls) {
            if (includedUrls != null) {
                Collections.addAll(this.includedUrls, includedUrls);
            }
            return this;
        }

        public MockHttpServletResponse build() {
            MockHttpServletResponse response = new MockHttpServletResponse();
            response.characterEncoding = this.characterEncoding;
            response.charset = this.charset;
            response.contentLength = this.contentLength;
            response.contentType = this.contentType;
            response.bufferSize = this.bufferSize;
            response.committed = this.committed;
            response.locale = this.locale;
            response.cookies.addAll(this.cookies);
            response.headers.putAll(this.headers);
            response.status = this.status;
            response.errorMessage = this.errorMessage;
            response.forwardedUrl = this.forwardedUrl;
            response.includedUrls.addAll(this.includedUrls);
            return response;
        }
    }
}
