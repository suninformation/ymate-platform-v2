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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.net.*;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 文件处理工具类
 *
 * @author 刘镇 (suninformation@163.com) on 2010-4-23 上午01:14:10
 */
public class FileUtils {

    public static final String SEPARATOR_CHAR = ":";

    public static final String POINT_CHAR = ".";

    public static final String PROTOCOL_JAR = "jar";

    public static final String PROTOCOL_WS_JAR = "wsjar";

    public static final String PROTOCOL_ZIP = "zip";

    public static final String PROTOCOL_FILE = "file";

    public static final String PROTOCOL_HTTP = "http";

    public static final String PROTOCOL_HTTPS = "https";

    public static final String PROTOCOL_FTP = "ftp";

    public static final String PROTOCOL_VFS_FILE = "vfs" + PROTOCOL_FILE;

    public static final String FILE_PREFIX_JAR = PROTOCOL_JAR + SEPARATOR_CHAR;

    public static final String FILE_PREFIX_ZIP = PROTOCOL_ZIP + SEPARATOR_CHAR;

    public static final String FILE_PREFIX_HTTP = PROTOCOL_HTTP + SEPARATOR_CHAR;

    public static final String FILE_PREFIX_HTTPS = PROTOCOL_HTTPS + SEPARATOR_CHAR;

    public static final String FILE_PREFIX_FTP = PROTOCOL_FTP + SEPARATOR_CHAR;

    public static final String FILE_PREFIX_FILE = PROTOCOL_FILE + SEPARATOR_CHAR;

    public static final String FILE_CLASS = "class";

    public static final String FILE_SUFFIX_CLASS = POINT_CHAR + FILE_CLASS;

    public static final String FILE_SUFFIX_XML = "xml";

    public static final String FILE_SUFFIX_PROPERTIES = "properties";

    private static final Log LOG = LogFactory.getLog(FileUtils.class);

    /**
     * @param fileName 原始文件名称
     * @return 提取文件扩展名称，若不存在扩展名则返回原始文件名称
     */
    public static String getExtName(String fileName) {
        String suffix = null;
        int pos = fileName.lastIndexOf('.');
        if (pos > 0 && pos < fileName.length() - 1) {
            suffix = fileName.substring(pos + 1);
        }
        return StringUtils.trimToEmpty(suffix);
    }

    /**
     * @param url 目标URL地址
     * @return 将URL地址转换成File对象
     */
    public static File toFile(URL url) {
        if (url == null) {
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
            if (!filePath.startsWith(FILE_PREFIX_JAR)
                    && !filePath.startsWith(FILE_PREFIX_FILE)
                    && !filePath.startsWith(FILE_PREFIX_ZIP)
                    && !filePath.startsWith(FILE_PREFIX_HTTP)
                    && !filePath.startsWith(FILE_PREFIX_HTTPS)
                    && !filePath.startsWith(FILE_PREFIX_FTP)) {

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
        File zipFile = File.createTempFile(prefix, ".zip");
        try (ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(zipFile))) {
            for (File file : files) {
                ZipEntry zipEntry = new ZipEntry(file.getName());
                outputStream.putNextEntry(zipEntry);
                //
                try (InputStream inputStream = new FileInputStream(file)) {
                    IOUtils.copyLarge(inputStream, outputStream);
                }
            }
        }
        return zipFile;
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
            throw new IllegalArgumentException(String.format("Failure to write file, Source file [%s] type must be file and exist.", src != null ? src.getPath() : StringUtils.EMPTY));
        }
        if (dest == null || !dest.isAbsolute()) {
            throw new IllegalArgumentException(String.format("Failure to write file, Dest file [%s] must be absolute path.", dest != null ? dest.getPath() : StringUtils.EMPTY));
        }
        if (!src.renameTo(dest)) {
            try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(src));
                 BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(dest))) {
                IOUtils.copyLarge(inputStream, outputStream);
            }
        }
    }

    /**
     * 从JAR包中提取/META-INF/{prefixPath}目录下的资源文件并复制到{targetFile}指定的目录中
     *
     * @param prefixPath 资源文件目录名称
     * @param targetFile 目标文件目录
     * @return 是否有文件被提取
     * @throws IOException 可能生产的任何异常
     */
    public static boolean unpackJarFile(String prefixPath, File targetFile) throws IOException {
        return unpackJarFile(prefixPath, targetFile, FileUtils.class);
    }

    /**
     * 从JAR包中提取/META-INF/{prefixPath}目录下的资源文件并复制到{targetFile}指定的目录中
     *
     * @param callingClass 调用类
     * @param prefixPath   资源文件目录名称
     * @param targetFile   目标文件目录
     * @return 是否有文件被提取
     * @throws IOException 可能生产的任何异常
     */
    public static boolean unpackJarFile(String prefixPath, File targetFile, Class<?> callingClass) throws IOException {
        if (callingClass == null) {
            throw new NullArgumentException("callingClass");
        }
        if (StringUtils.isBlank(prefixPath)) {
            throw new NullArgumentException("prefixPath");
        }
        if (targetFile == null || !targetFile.isAbsolute() || !targetFile.isDirectory()) {
            throw new IllegalArgumentException(String.format("Unpack target file [%s] must be directory and absolute path.", targetFile != null ? targetFile.getPath() : StringUtils.EMPTY));
        }
        boolean result = false;
        prefixPath = "META-INF/" + prefixPath;
        URL url = callingClass.getResource("/" + prefixPath);
        if (url != null) {
            URLConnection connection = url.openConnection();
            try {
                if (connection instanceof JarURLConnection) {
                    try (JarFile jarFile = ((JarURLConnection) connection).getJarFile()) {
                        Enumeration<JarEntry> entriesEnum = jarFile.entries();
                        while (entriesEnum.hasMoreElements()) {
                            JarEntry entry = entriesEnum.nextElement();
                            if (StringUtils.startsWith(entry.getName(), prefixPath)) {
                                if (!entry.isDirectory()) {
                                    String entryName = StringUtils.substringAfter(entry.getName(), prefixPath);
                                    File distFile = new File(targetFile, entryName);
                                    File distFileParent = distFile.getParentFile();
                                    if (!distFileParent.exists() && !distFileParent.mkdirs()) {
                                        throw new IOException(String.format("Unable to create directory: %s", distFileParent.getPath()));
                                    }
                                    if (LOG.isDebugEnabled()) {
                                        LOG.debug(String.format("Unpacking resource file: %s", entry.getName()));
                                    }
                                    try (InputStream inputStream = jarFile.getInputStream(entry);
                                         OutputStream outputStream = new FileOutputStream(distFile)) {
                                        IOUtils.copyLarge(inputStream, outputStream);
                                        result = true;
                                    }
                                }
                            }
                        }
                    }
                } else {
                    try {
                        writeDirTo(new File(url.toURI()), targetFile);
                        result = true;
                    } catch (URISyntaxException e) {
                        throw new IOException(String.format("Unable to unpack file: %s", url), e);
                    }
                }
            } finally {
                IOUtils.close(connection);
            }
        }
        return result;
    }

    /**
     * 复制目录(递归)
     *
     * @param sources   源目录
     * @param targetDir 目标目录
     * @throws IOException 可能产生的异常
     */
    public static void writeDirTo(File sources, File targetDir) throws IOException {
        if (sources != null && sources.isDirectory()) {
            File[] files = sources.listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    File targetFile = new File(targetDir, file.getName());
                    if (!file.isDirectory()) {
                        File targetFileParent = targetFile.getParentFile();
                        if (!targetFileParent.exists() && !targetFileParent.mkdirs()) {
                            throw new IOException(String.format("Unable to create directory: %s", targetFileParent.getPath()));
                        }
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(String.format("Unpacking resource file: %s", targetFile.getPath()));
                        }
                        try (InputStream inputStream = new FileInputStream(file);
                             OutputStream outputStream = new FileOutputStream(targetFile)) {
                            IOUtils.copyLarge(inputStream, outputStream);
                        }
                    } else {
                        writeDirTo(file, targetFile);
                    }
                }
            }
        }
    }

    /**
     * 在指定路径中创建空文件(同时生成其父级目录)
     *
     * @param newFile 新文件
     * @return 若文件创建成功或已存在则返回true, 若存在且为目录则返回false
     * @throws IOException 可能产生的异常
     * @since 2.1.0
     */
    public static boolean createEmptyFile(File newFile) throws IOException {
        return createFileIfNotExists(newFile, null);
    }

    /**
     * 在指定路径中创建文件(同时生成其父级目录)若内容流不为空则写入内容
     *
     * @param newFile     新文件
     * @param inputStream 新文件内容流
     * @return 若文件创建成功或已存在则返回true, 若存在且为目录则返回false
     * @throws IOException 可能产生的异常
     * @since 2.1.0
     */
    public static boolean createFileIfNotExists(File newFile, InputStream inputStream) throws IOException {
        if (!newFile.isAbsolute()) {
            throw new IllegalArgumentException(String.format("File path [%s] is not an absolute path.", newFile.getPath()));
        } else if (!newFile.exists()) {
            File parentFile = newFile.getParentFile();
            if (!parentFile.exists() && !parentFile.mkdirs()) {
                throw new IOException(String.format("Unable to create directory: %s", parentFile.getPath()));
            }
            if (newFile.createNewFile()) {
                if (inputStream != null) {
                    try (OutputStream outputStream = new FileOutputStream(newFile)) {
                        IOUtils.copyLarge(inputStream, outputStream);
                    }
                }
                if (LOG.isInfoEnabled()) {
                    LOG.info(String.format("Successfully created file: %s", newFile.getPath()));
                }
            }
        }
        return !newFile.isDirectory();
    }

    /**
     * 按数组顺序查加载文件并返回文件输入流
     *
     * @param filePaths 文件列表
     * @return 返回文件输入流
     * @since 2.1.0
     */
    public static InputStream loadFileAsStream(String... filePaths) {
        InputStream inputStream = null;
        if (filePaths != null && filePaths.length > 0) {
            for (String filePath : filePaths) {
                if (StringUtils.isNotBlank(filePath)) {
                    File file = new File(filePath);
                    if (file.isAbsolute() && file.exists() && file.isFile()) {
                        try {
                            inputStream = new FileInputStream(file);
                            if (LOG.isInfoEnabled()) {
                                LOG.info(String.format("Found and load the file: %s", file.getPath()));
                            }
                            break;
                        } catch (FileNotFoundException ignored) {
                        }
                    }
                }
            }
        }
        return inputStream;
    }
}
