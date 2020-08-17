/*
 * Copyright 2007-2020 the original author or authors.
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
package net.ymate.platform.commons.http.impl;

import net.ymate.platform.commons.http.HttpClientHelper;
import net.ymate.platform.commons.http.IFileWrapper;
import net.ymate.platform.commons.util.FileUtils;
import net.ymate.platform.commons.util.MimeTypeUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.InputStreamBody;

import java.io.*;

/**
 * 文件包装器接口默认实现
 *
 * @author 刘镇 (suninformation@163.com) on 15/8/29 上午9:37
 */
public class DefaultFileWrapper implements IFileWrapper {

    private boolean hasError;

    private String errMsg;

    private String fileName;

    private String name;

    private String suffix;

    private String contentType;

    private long contentLength;

    private File tempFile;

    public DefaultFileWrapper(String fileName, String contentType, File sourceFile) {
        this.tempFile = sourceFile;
        this.fileName = StringUtils.defaultIfBlank(fileName, sourceFile.getName());
        if (StringUtils.isBlank(contentType)) {
            this.contentType = MimeTypeUtils.getFileMimeType(FileUtils.getExtName(sourceFile.getName()));
        } else {
            this.contentType = contentType;
        }
        this.contentLength = sourceFile.length();
        //
        doParseFileName();
    }

    public DefaultFileWrapper(String fileName, String contentType, long contentLength, InputStream sourceInputStream) throws IOException {
        this.fileName = fileName;
        this.contentType = contentType;
        this.contentLength = contentLength;
        //
        doParseFileName();
        //
        tempFile = File.createTempFile("download_", fileName);
        try (OutputStream outputStream = new FileOutputStream(tempFile)) {
            IOUtils.copyLarge(sourceInputStream, outputStream);
        }
    }

    public DefaultFileWrapper(String contentType, File sourceFile) {
        this(null, contentType, sourceFile);
    }

    public DefaultFileWrapper(File sourceFile) {
        this(null, null, sourceFile);
    }

    public DefaultFileWrapper(String contentType, long contentLength, InputStream sourceInputStream) throws IOException {
        this(null, contentType, contentLength, sourceInputStream);
    }

    private void doParseFileName() {
        if (StringUtils.isNotBlank(this.fileName)) {
            name = StringUtils.substringBefore(StringUtils.replace(this.fileName, "\"", StringUtils.EMPTY), ".");
            suffix = FileUtils.getExtName(this.fileName);
        }
    }

    private static String doParseFileName(HttpResponse httpResponse) {
        String fileName = null;
        if (httpResponse.getStatusLine().getStatusCode() == HttpClientHelper.HTTP_STATUS_CODE_SUCCESS) {
            if (httpResponse.containsHeader(HttpClientHelper.HEADER_CONTENT_DISPOSITION)) {
                fileName = StringUtils.substringAfter(httpResponse.getFirstHeader(HttpClientHelper.HEADER_CONTENT_DISPOSITION).getValue(), "filename=");
            }
        }
        return fileName;
    }

    public DefaultFileWrapper(HttpResponse httpResponse) throws IOException {
        this(doParseFileName(httpResponse), httpResponse.getEntity().getContentType().getValue(), httpResponse.getEntity().getContentLength(), httpResponse.getEntity().getContent());
    }

    public DefaultFileWrapper(String errMsg) {
        hasError = true;
        this.errMsg = errMsg;
    }

    @Override
    public boolean hasError() {
        return hasError;
    }

    @Override
    public String getErrorMsg() {
        return errMsg;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSuffix() {
        return suffix;
    }

    @Override
    public long getContentLength() {
        return contentLength;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(tempFile);
    }

    @Override
    public void writeTo(File distFile) throws IOException {
        FileUtils.writeTo(tempFile, distFile);
    }

    @Override
    public ContentBody toContentBody() throws IOException {
        final long len = getContentLength();
        return new InputStreamBody(getInputStream(), ContentType.create(getContentType()), getFileName()) {
            @Override
            public long getContentLength() {
                return len;
            }
        };
    }
}
