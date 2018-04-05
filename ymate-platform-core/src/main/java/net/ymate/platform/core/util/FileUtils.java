/*
 * Copyright 2007-2017 the original author or authors.
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
package net.ymate.platform.core.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 文件处理工具类
 *
 * @author 刘镇 (suninformation@163.com) on 2010-4-23 上午01:14:10
 * @version 1.0
 */
public class FileUtils {

    private static final Log _LOG = LogFactory.getLog(FileUtils.class);

    /**
     * @param fileName 原始文件名称
     * @return 提取文件扩展名称，若不存在扩展名则返回原始文件名称
     */
    public static String getExtName(String fileName) {
        String suffix = "";
        int pos = fileName.lastIndexOf('.');
        if (pos > 0 && pos < fileName.length() - 1) {
            suffix = fileName.substring(pos + 1);
        }
        return suffix;
    }

    /**
     * @param url 目标URL地址
     * @return 将URL地址转换成File对象, 若url指向的是jar包中文件，则返回null
     */
    public static File toFile(URL url) {
        if ((url == null) || (!url.getProtocol().equals("file"))) {
            return null;
        }
        String filename = url.getFile().replace('/', File.separatorChar);
        int pos = 0;
        while ((pos = filename.indexOf('%', pos)) >= 0) {
            if (pos + 2 < filename.length()) {
                String hexStr = filename.substring(pos + 1, pos + 3);
                char ch = (char) Integer.parseInt(hexStr, 16);
                filename = filename.substring(0, pos) + ch + filename.substring(pos + 3);
            }
        }
        return new File(filename);
    }

    /**
     * @param filePath 目标文件路径
     * @return 将文件路径转换成URL对象, 返回值可能为NULL, 若想将jar包中文件，必须使用URL.toString()方法生成filePath参数—即以"jar:"开头
     */
    public static URL toURL(String filePath) {
        if (StringUtils.isBlank(filePath)) {
            throw new NullArgumentException("filePath");
        }
        try {
            if (!filePath.startsWith("jar:")
                    && !filePath.startsWith("file:")
                    && !filePath.startsWith("zip:")
                    && !filePath.startsWith("http:")
                    && !filePath.startsWith("ftp:")) {

                return new File(filePath).toURI().toURL();
            }
            return new URL(filePath);
        } catch (MalformedURLException e) {
            // DO NOTHING...
        }
        return null;
    }

    /**
     * @param prefix 临时文件名前缀, 若为空则由系统随机生成8位长度字符串
     * @param files  文件集合
     * @return 将文件集合压缩成单个ZIP文件
     * @throws IOException 可能产生的异常
     */
    public static File toZip(String prefix, File... files) throws IOException {
        if (ArrayUtils.isEmpty(files)) {
            throw new NullArgumentException("files");
        }
        if (StringUtils.isBlank(prefix)) {
            prefix = UUIDUtils.randomStr(8, false);
        }
        if (StringUtils.endsWith(prefix, "_")) {
            prefix = prefix.concat("_");
        }
        File _zipFile = File.createTempFile(prefix, ".zip");
        ZipOutputStream _outputStream = null;
        try {
            _outputStream = new ZipOutputStream(new FileOutputStream(_zipFile));
            for (File file : files) {
                ZipEntry _zipEntry = new ZipEntry(file.getName());
                _outputStream.putNextEntry(_zipEntry);
                //
                InputStream _inputStream = null;
                try {
                    _inputStream = new FileInputStream(file);
                    IOUtils.copyLarge(_inputStream, _outputStream);
                } finally {
                    IOUtils.closeQuietly(_inputStream);
                }
            }
        } finally {
            IOUtils.closeQuietly(_outputStream);
        }
        return _zipFile;
    }

    /**
     * 复制文件
     *
     * @param src  原文件
     * @param dest 目标文件
     * @throws IOException 可能产生的异常
     */
    public static void writeTo(File src, File dest) throws IOException {
        if (src == null || !src.exists() || !src.isFile()) {
            throw new IllegalArgumentException("Failure to write file, Source file type must be file and exists.");
        }
        if (dest == null || !dest.isAbsolute()) {
            throw new IllegalArgumentException("Failure to write file, Dest file must be absolute path.");
        }
        if (!src.renameTo(dest)) {
            BufferedInputStream _inStream = null;
            BufferedOutputStream _outStream = null;
            try {
                _inStream = new BufferedInputStream(new FileInputStream(src));
                _outStream = new BufferedOutputStream(new FileOutputStream(dest));
                IOUtils.copyLarge(_inStream, _outStream);
            } finally {
                IOUtils.closeQuietly(_inStream);
                IOUtils.closeQuietly(_outStream);
            }
        }
    }
}
