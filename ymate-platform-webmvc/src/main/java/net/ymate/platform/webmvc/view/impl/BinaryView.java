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
package net.ymate.platform.webmvc.view.impl;

import net.ymate.platform.commons.lang.BlurObject;
import net.ymate.platform.commons.lang.PairObject;
import net.ymate.platform.commons.util.FileUtils;
import net.ymate.platform.commons.util.MimeTypeUtils;
import net.ymate.platform.webmvc.base.Type;
import net.ymate.platform.webmvc.context.WebContext;
import net.ymate.platform.webmvc.view.AbstractView;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * 二进制数据流视图
 *
 * @author 刘镇 (suninformation@163.com) on 2011-10-23 上午11:32:55
 */
public class BinaryView extends AbstractView {

    private String fileName;

    private final Object data;

    private long length = -1;

    /**
     * @param targetFile 目标文件
     * @return 加载文件并转换成二进制视图类对象，若目标文件不存在或无较则返回NULL
     */
    public static BinaryView bind(File targetFile) {
        if (targetFile != null && targetFile.canRead() && targetFile.exists() && targetFile.isFile()) {
            BinaryView binaryView = new BinaryView(targetFile);
            binaryView.setContentType(MimeTypeUtils.getFileMimeType(FileUtils.getExtName(targetFile.getPath())));
            return binaryView;
        }
        return null;
    }

    /**
     * 构造器
     *
     * @param data 数据对象
     */
    public BinaryView(Object data) {
        this.data = data;
    }

    /**
     * 构造器
     *
     * @param inputStream 数据输入流
     * @param length      输入流数据长度
     */
    public BinaryView(InputStream inputStream, long length) {
        data = inputStream;
        if (length > 0) {
            this.length = length;
        }
    }

    @Override
    public void render() throws Exception {
        if (StringUtils.isBlank(contentType)) {
            contentType = Type.ContentType.OCTET_STREAM.getContentType();
        }
        super.render();
    }

    @Override
    protected void doRenderView() throws Exception {
        HttpServletRequest httpServletRequest = WebContext.getRequest();
        HttpServletResponse httpServletResponse = WebContext.getResponse();
        //
        if (StringUtils.isNotBlank(fileName)) {
            StringBuilder dispositionBuilder = new StringBuilder("attachment;filename=");
            if (StringUtils.containsIgnoreCase(httpServletRequest.getHeader(Type.HttpHead.USER_AGENT), "firefox")) {
                dispositionBuilder.append(new String(fileName.getBytes(StandardCharsets.UTF_8), "ISO8859-1"));
            } else {
                dispositionBuilder.append(URLEncoder.encode(fileName, DEFAULT_CHARSET));
            }
            httpServletResponse.setHeader(Type.HttpHead.CONTENT_DISPOSITION, dispositionBuilder.toString());
        }
        //
        if (data == null) {
            return;
        }
        // 文件
        if (data instanceof File) {
            // 读取文件数据长度
            length = ((File) data).length();
            // 尝试计算Range以配合断点续传
            PairObject<Long, Long> rangePairObj = doParseRange(length);
            // 若为断点续传
            if (rangePairObj != null) {
                doSetRangeHeader(httpServletResponse, rangePairObj);
                // 开始续传文件流
                try (InputStream inputStream = Files.newInputStream(((File) data).toPath())) {
                    IOUtils.copyLarge(inputStream, httpServletResponse.getOutputStream(), rangePairObj.getKey(), rangePairObj.getValue());
                }
            } else {
                // 正常下载
                httpServletResponse.setContentLength(BlurObject.bind(length).toIntValue());
                try (InputStream inputStream = Files.newInputStream(((File) data).toPath())) {
                    IOUtils.copyLarge(inputStream, httpServletResponse.getOutputStream());
                }
            }
        }
        // 字节数组
        else if (data instanceof byte[]) {
            byte[] bytes = (byte[]) data;
            httpServletResponse.setContentLength(bytes.length);
            IOUtils.write(bytes, httpServletResponse.getOutputStream());
        }
        // 字符数组
        else if (data instanceof char[]) {
            char[] chars = (char[]) data;
            httpServletResponse.setContentLength(chars.length);
            IOUtils.write(chars, httpServletResponse.getOutputStream(), httpServletRequest.getCharacterEncoding());
        }
        // 文本流
        else if (data instanceof Reader) {
            try (Reader r = (Reader) data) {
                IOUtils.copy(r, httpServletResponse.getOutputStream(), httpServletRequest.getCharacterEncoding());
            }
        }
        // 二进制流
        else if (data instanceof InputStream) {
            PairObject<Long, Long> rangePairObj = doParseRange(length);
            if (rangePairObj != null) {
                doSetRangeHeader(httpServletResponse, rangePairObj);
                try (InputStream in = (InputStream) data) {
                    IOUtils.copyLarge(in, httpServletResponse.getOutputStream(), rangePairObj.getKey(), rangePairObj.getValue());
                }
            } else {
                httpServletResponse.setContentLength(BlurObject.bind(length).toIntValue());
                try (InputStream in = (InputStream) data) {
                    IOUtils.copyLarge(in, httpServletResponse.getOutputStream());
                }
            }
        }
        // 普通对象
        else {
            byte[] content = StringUtils.trimToEmpty(BlurObject.bind(data).toStringValue()).getBytes(httpServletRequest.getCharacterEncoding());
            httpServletResponse.setContentLength(content.length);
            IOUtils.write(content, httpServletResponse.getOutputStream());
        }
    }

    private void doSetRangeHeader(HttpServletResponse response, PairObject<Long, Long> range) {
        // 表示使用了断点续传（默认是“none”，可以不指定）
        addHeader(Type.HttpHead.ACCEPT_RANGES, "bytes");
        // Content-Length: [文件的总大小] - [客户端请求的下载的文件块的开始字节]
        long totalLength = range.getValue() - range.getKey();
        addHeader(Type.HttpHead.CONTENT_LENGTH, String.valueOf(totalLength));
        // Content-Range: bytes [文件块的开始字节]-[文件的总大小 - 1]/[文件的总大小]
        addHeader(Type.HttpHead.CONTENT_RANGE, String.format("bytes %d-%d/%d", range.getKey(), range.getValue() - 1, length));
        // response.setHeader("Connection", "Close"); //此语句将不能用IE直接下载
        // Status: 206
        response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
    }

    /**
     * 分析请求头中的Range参数，计算并返回本次数据的开始和结束位置
     *
     * @param length 数据大小
     * @return 若非断点续传则返回null
     */
    private PairObject<Long, Long> doParseRange(long length) {
        PairObject<Long, Long> returnValue = null;
        // 通过请求头Range参数判断是否采用断点续传
        String separatorStr = "bytes=";
        String rangeStr = WebContext.getRequest().getHeader(Type.HttpHead.RANGE);
        if (rangeStr != null && rangeStr.startsWith(separatorStr) && rangeStr.length() >= 7) {
            rangeStr = StringUtils.substringAfter(rangeStr, separatorStr);
            String[] ranges = StringUtils.split(rangeStr, ",");
            // 可能存在多个Range，目前仅处理第一个...
            for (String range : ranges) {
                if (StringUtils.isBlank(range)) {
                    return null;
                }
                // bytes=-100
                if (range.startsWith("-")) {
                    long end = Long.parseLong(range);
                    long start = length + end;
                    if (start < 0) {
                        return null;
                    }
                    returnValue = new PairObject<>(start, length);
                    break;
                }
                // bytes=1024-
                if (range.endsWith("-")) {
                    long start = Long.parseLong(StringUtils.substringBefore(range, "-"));
                    if (start < 0) {
                        return null;
                    }
                    returnValue = new PairObject<>(start, length);
                    break;
                }
                // bytes=10-1024
                if (range.contains("-")) {
                    String[] tmp = range.split("-");
                    long start = Long.parseLong(tmp[0]);
                    long end = Long.parseLong(tmp[1]);
                    if (start > end) {
                        return null;
                    }
                    returnValue = new PairObject<>(start, end + 1);
                }
            }
        }
        return returnValue;
    }

    /**
     * @param dispFileName 显示的文件名称
     * @return 设置采用档案下载的方式
     */
    public BinaryView useAttachment(String dispFileName) {
        fileName = dispFileName;
        return this;
    }
}
