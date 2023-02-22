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

import net.ymate.platform.commons.util.FileUtils;
import net.ymate.platform.commons.util.MimeTypeUtils;
import net.ymate.platform.webmvc.IUploadFileWrapper;
import net.ymate.platform.webmvc.IWebMvc;
import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.file.Files;
import java.util.*;

/**
 * 文件上传处理助手类；注：文件上传页面Form表单必须采用POST方式提交并设置属性：enctype="multipart/form-data"，否则将无法处理
 *
 * @author 刘镇 (suninformation@163.com) on 2011-6-5 下午02:50:07
 */
public final class FileUploadHelper {

    private final HttpServletRequest request;

    /**
     * 监听器
     */
    private ProgressListener listener;

    /**
     * 上传文件临时目录（不支持自定义文件流处理）
     */
    private File uploadTempDir;

    /**
     * 上传文件数量最大值
     */
    private long fileCountMax = -1;

    /**
     * 上传文件最大值, 10485760 = 10M
     */
    private long fileSizeMax = -1;

    /**
     * 上传文件总量的最大值
     */
    private long sizeMax = -1;

    /**
     * 内存缓冲区的大小,默认值为10K,如果文件大于10K,将使用临时文件缓存上传文件, 4096 = 4K
     */
    private int sizeThreshold = 10240;

    private String charsetEncoding;

    public static FileUploadHelper bind(IWebMvc owner, HttpServletRequest request) {
        return new FileUploadHelper(owner, request);
    }

    private FileUploadHelper(IWebMvc owner, HttpServletRequest request) {
        this.request = request;
        if (StringUtils.isBlank(charsetEncoding = owner.getConfig().getDefaultCharsetEncoding())) {
            charsetEncoding = this.request.getCharacterEncoding();
        }
    }

    /**
     * @param processor 文件上传处理器
     * @return 处理表单提交，使用提供的文件上传处理器处理文件流
     * @throws FileUploadException 文件上传时可能产生的异常
     * @throws IOException         文件读写可能产生的异常
     */
    public UploadFormWrapper processUpload(IUploadFileItemProcessor processor) throws FileUploadException, IOException {
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (isMultipart) {
            if (null != processor) {
                return doUploadFileAsStream(processor);
            } else {
                return doUploadFileAsDiskBased();
            }
        }
        return new UploadFormWrapper();
    }

    /**
     * @return 处理表单提交
     * @throws FileUploadException 文件上传时可能产生的异常
     * @throws IOException         文件读写可能产生的异常
     */
    public UploadFormWrapper processUpload() throws FileUploadException, IOException {
        return processUpload(null);
    }

    private ServletFileUpload doBuildServletFileUpload(FileItemFactory fileItemFactory) {
        ServletFileUpload servletFileUpload = new ServletFileUpload(fileItemFactory);
        servletFileUpload.setFileSizeMax(fileSizeMax);
        servletFileUpload.setSizeMax(sizeMax);
        servletFileUpload.setFileCountMax(fileCountMax);
        if (listener != null) {
            servletFileUpload.setProgressListener(listener);
        }
        return servletFileUpload;
    }

    /**
     * 采用文件流的方式处理上传文件（即将上传文件流对象交给用户做进一步处理）
     *
     * @param processor 文件上传处理器
     * @throws FileUploadException 文件上传时可能产生的异常
     * @throws IOException         文件读写可能产生的异常
     */
    private UploadFormWrapper doUploadFileAsStream(IUploadFileItemProcessor processor) throws FileUploadException, IOException {
        ServletFileUpload servletFileUpload = doBuildServletFileUpload(null);
        Map<String, List<String>> fields = new HashMap<>(16);
        Map<String, List<UploadFileWrapper>> files = new HashMap<>(16);
        //
        FileItemIterator fileItemIt = servletFileUpload.getItemIterator(request);
        while (fileItemIt.hasNext()) {
            FileItemStream item = fileItemIt.next();
            if (item.isFormField()) {
                fields.computeIfAbsent(item.getFieldName(), k -> new ArrayList<>()).add(Streams.asString(item.openStream(), charsetEncoding));
            } else {
                files.computeIfAbsent(item.getFieldName(), k -> new ArrayList<>()).add(processor.process(item));
            }
        }
        return new UploadFormWrapper(fields, files);
    }

    /**
     * 采用文件方式处理上传文件（即先将文件上传后，再交给用户已上传文件对象集合）
     *
     * @throws FileUploadException 文件上传时可能产生的异常
     * @throws IOException         文件读写可能产生的异常
     */
    private UploadFormWrapper doUploadFileAsDiskBased() throws FileUploadException, IOException {
        DiskFileItemFactory fileItemFactory = new DiskFileItemFactory();
        fileItemFactory.setRepository(uploadTempDir);
        fileItemFactory.setSizeThreshold(sizeThreshold);
        //
        ServletFileUpload servletFileUpload = doBuildServletFileUpload(fileItemFactory);
        List<FileItem> fileItems = servletFileUpload.parseRequest(request);
        //
        Map<String, List<String>> fields = new HashMap<>(fileItems.size());
        Map<String, List<UploadFileWrapper>> files = new HashMap<>(fileItems.size());
        //
        for (FileItem item : fileItems) {
            if (item.isFormField()) {
                fields.computeIfAbsent(item.getFieldName(), k -> new ArrayList<>()).add(item.getString(charsetEncoding));
            } else {
                files.computeIfAbsent(item.getFieldName(), k -> new ArrayList<>()).add(new UploadFileWrapper(item));
            }
        }
        return new UploadFormWrapper(fields, files);
    }

    /**
     * @return 监听器
     */
    public ProgressListener getFileUploadListener() {
        return listener;
    }

    /**
     * @param listener 文件上传进度监听器
     * @return 设置监听器
     */
    public FileUploadHelper setFileUploadListener(ProgressListener listener) {
        this.listener = listener;
        return this;
    }

    /**
     * @return 上传文件临时目录（不支持自定义文件流处理）
     */
    public File getUploadTempDir() {
        return uploadTempDir;
    }

    /**
     * @param uploadDir 临时目录
     * @return 上传文件临时目录（不支持自定义文件流处理），默认使用：System.getProperty("java.io.tmpdir")
     */
    public FileUploadHelper setUploadTempDir(File uploadDir) {
        uploadTempDir = uploadDir;
        return this;
    }

    /**
     * @return 上传文件数量最大值
     */
    public long getFileCountMax() {
        return fileCountMax;
    }

    /**
     * @param fileCountMax 文件数量
     * @return 设置上传文件数量最大值
     */
    public FileUploadHelper setFileCountMax(long fileCountMax) {
        this.fileCountMax = fileCountMax;
        return this;
    }

    /**
     * @return 上传文件最大值
     */
    public long getFileSizeMax() {
        return fileSizeMax;
    }

    /**
     * @param fileSize 文件大小
     * @return 设置上传文件最大值
     */
    public FileUploadHelper setFileSizeMax(long fileSize) {
        fileSizeMax = fileSize;
        return this;
    }

    /**
     * @return 内存缓冲区的大小, 默认值为10K, 如果文件大于10K, 将使用临时文件缓存上传文件
     */
    public int getSizeThreshold() {
        return sizeThreshold;
    }

    /**
     * @param threshold 缓冲区大小
     * @return 内存缓冲区的大小, 默认值为10K, 如果文件大于10K, 将使用临时文件缓存上传文件
     */
    public FileUploadHelper setSizeThreshold(int threshold) {
        sizeThreshold = threshold;
        return this;
    }

    /**
     * @return 上传文件总量的最大值
     */
    public long getSizeMax() {
        return sizeMax;
    }

    /**
     * @param size 文件总量大小
     * @return 设置上传文件总量的最大值
     */
    public FileUploadHelper setSizeMax(long size) {
        sizeMax = size;
        return this;
    }

    /**
     * 文件上传处理回调接口定义，用于将每个文件交给开发者自行处理
     *
     * @author 刘镇 (suninformation@163.com) on 2011-6-5 下午03:47:49
     */
    public interface IUploadFileItemProcessor {

        /**
         * 处理文件或文件流
         *
         * @param item FileItemStream
         * @return 返回文件上传包装器
         * @throws IOException         文件读写可能产生的异常
         * @throws FileUploadException 文件上传时可能产生的异常
         */
        UploadFileWrapper process(FileItemStream item) throws IOException, FileUploadException;

    }

    /**
     * 文件上传表单包装器；
     *
     * @author 刘镇 (suninformation@163.com) on 2011-6-7 上午09:50:56
     */
    public static class UploadFormWrapper implements AutoCloseable {

        private final Map<String, String[]> fieldMap = new HashMap<>();

        private final Map<String, IUploadFileWrapper[]> fileMap = new HashMap<>();

        UploadFormWrapper() {
        }

        UploadFormWrapper(Map<String, List<String>> fields, Map<String, List<UploadFileWrapper>> files) {
            fields.forEach((key, value) -> fieldMap.put(key, value.toArray(new String[0])));
            files.forEach((key, value) -> fileMap.put(key, value.toArray(new UploadFileWrapper[0])));
        }

        public Map<String, String[]> getFieldMap() {
            return Collections.unmodifiableMap(fieldMap);
        }

        public Map<String, IUploadFileWrapper[]> getFileMap() {
            return Collections.unmodifiableMap(fileMap);
        }

        public String[] getField(String fieldName) {
            return fieldMap.get(fieldName);
        }

        public IUploadFileWrapper[] getFile(String fieldName) {
            return fileMap.get(fieldName);
        }

        @Override
        public void close() throws Exception {
            for (IUploadFileWrapper[] fileWrappers : fileMap.values()) {
                if (fileWrappers != null) {
                    for (IUploadFileWrapper fileWrapper : fileWrappers) {
                        if (fileWrapper != null) {
                            fileWrapper.close();
                        }
                    }
                }
            }
        }
    }

    /**
     * 上传文件对象包装器
     *
     * @author 刘镇 (suninformation@163.com) on 2011-6-6 上午01:16:45
     */
    public static class UploadFileWrapper implements IUploadFileWrapper {

        private FileItem fileItem;

        private File file;

        private File tempFile;

        private boolean fileObj;

        public UploadFileWrapper(FileItem fileItem) {
            this.fileItem = fileItem;
        }

        public UploadFileWrapper(File file) {
            this.file = file;
            fileObj = true;
        }

        @Override
        public String getPath() {
            if (fileObj) {
                return file == null ? StringUtils.EMPTY : file.getAbsolutePath();
            }
            return fileItem.getName();
        }

        @Override
        public String getName() {
            String filePath;
            if (fileObj) {
                filePath = file == null ? StringUtils.EMPTY : file.getAbsolutePath();
            } else {
                filePath = fileItem.getName();
            }
            if (filePath != null) {
                int pos = filePath.lastIndexOf('\\');
                if (pos == -1) {
                    pos = filePath.lastIndexOf('/');
                }
                filePath = filePath.substring(pos + 1);
            }
            return filePath;
        }

        /**
         * 获取文件内容，将其存储在字节数组中（不适合对大文件操作）
         *
         * @return byte[]
         */
        public byte[] get() {
            if (fileObj) {
                if (file == null) {
                    return null;
                }
                byte[] fileData = new byte[(int) file.length()];
                try (FileInputStream fileInputStream = new FileInputStream(file)) {
                    IOUtils.readFully(fileInputStream, fileData);
                } catch (IOException e) {
                    fileData = null;
                }
                return fileData;
            }
            return fileItem.get();
        }

        @Override
        public void delete() {
            if (fileObj) {
                if (file != null && file.exists()) {
                    if (!file.delete()) {
                        file.deleteOnExit();
                    }
                }
            } else {
                fileItem.delete();
            }
        }

        @Override
        public void transferTo(File dest) throws Exception {
            writeTo(dest);
        }

        @Override
        public void writeTo(File file) throws Exception {
            if (fileObj) {
                FileUtils.writeTo(this.file, file);
            } else {
                fileItem.write(file);
            }
        }

        @Override
        public InputStream getInputStream() throws IOException {
            if (fileObj) {
                return Files.newInputStream(file.toPath());
            }
            return fileItem.getInputStream();
        }

        @Override
        public long getSize() {
            if (fileObj) {
                return file == null ? 0 : file.length();
            }
            return fileItem.getSize();
        }

        @Override
        public File getFile() throws Exception {
            if (fileObj) {
                return file;
            }
            if (tempFile == null) {
                tempFile = File.createTempFile("upload_", getName());
                tempFile.deleteOnExit();
                try (InputStream inputStream = new BufferedInputStream(fileItem.getInputStream());
                     OutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(tempFile.toPath()))) {
                    IOUtils.copyLarge(inputStream, outputStream);
                }
            }
            return tempFile;
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            if (fileObj) {
                return Files.newOutputStream(file.toPath());
            }
            return fileItem.getOutputStream();
        }

        @Override
        public String getContentType() {
            if (fileObj) {
                if (file != null && file.exists()) {
                    return MimeTypeUtils.getFileMimeType(FileUtils.getExtName(file.getAbsolutePath()));
                }
            } else if (fileItem != null) {
                return fileItem.getContentType();
            }
            return null;
        }

        @Override
        public void close() throws Exception {
            delete();
        }
    }
}
