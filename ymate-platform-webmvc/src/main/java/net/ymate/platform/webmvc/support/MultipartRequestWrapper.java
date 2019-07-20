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
package net.ymate.platform.webmvc.support;

import net.ymate.platform.webmvc.IMultipartRequestWrapper;
import net.ymate.platform.webmvc.IUploadFileWrapper;
import net.ymate.platform.webmvc.IWebMvc;
import net.ymate.platform.webmvc.util.FileUploadHelper;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * 表单类型为"multipart/form-data"请求包装类
 *
 * @author 刘镇 (suninformation@163.com) on 2011-8-5 上午10:19:47
 */
public class MultipartRequestWrapper extends HttpServletRequestWrapper implements IMultipartRequestWrapper {

    private final FileUploadHelper.UploadFormWrapper formWrapper;

    public MultipartRequestWrapper(IWebMvc owner, HttpServletRequest request) throws IOException, FileUploadException {
        super(request);
        // 绑定并初始化文件上传帮助类
        formWrapper = FileUploadHelper.bind(owner, request)
                .setUploadTempDir(new File(StringUtils.defaultIfBlank(owner.getConfig().getUploadTempDir(), System.getProperty("java.io.tmpdir"))))
                .setFileSizeMax(owner.getConfig().getUploadTotalSizeMax())
                .setSizeMax(owner.getConfig().getUploadFileSizeMax())
                .setSizeThreshold(owner.getConfig().getUploadSizeThreshold())
                .setFileUploadListener(owner.getConfig().getUploadListener())
                .processUpload();
    }

    @Override
    public String getParameter(String name) {
        String returnStr = super.getParameter(name);
        if (StringUtils.isBlank(returnStr)) {
            String[] params = formWrapper.getFieldMap().get(name);
            returnStr = (params == null ? null : params[0]);
        }
        return returnStr;
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] returnStr = super.getParameterValues(name);
        if (returnStr == null || returnStr.length == 0) {
            returnStr = formWrapper.getFieldMap().get(name);
        }
        return returnStr;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> returnMap = new HashMap<>(super.getParameterMap());
        returnMap.putAll(formWrapper.getFieldMap());
        return Collections.unmodifiableMap(returnMap);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return new Enumeration<String>() {

            private final Iterator<String> it = getParameterMap().keySet().iterator();

            @Override
            public boolean hasMoreElements() {
                return it.hasNext();
            }

            @Override
            public String nextElement() {
                return it.next();
            }
        };
    }

    /**
     * @param name 文件字段名称
     * @return 获取上传的文件
     */
    @Override
    public IUploadFileWrapper getUploadFile(String name) {
        IUploadFileWrapper[] files = formWrapper.getFileMap().get(name);
        return files == null ? null : files[0];
    }

    /**
     * @param name 文件字段名称
     * @return 获取上传的文数组
     */
    @Override
    public IUploadFileWrapper[] getUploadFiles(String name) {
        return formWrapper.getFileMap().get(name);
    }

    /**
     * @return 获取所有的上传文件
     */
    @Override
    public Set<IUploadFileWrapper> getUploadFiles() {
        Set<IUploadFileWrapper> returnValues = new HashSet<>();
        formWrapper.getFileMap().values().stream().filter((fileWrappers) -> (ArrayUtils.isNotEmpty(fileWrappers))).forEachOrdered((fileWrappers) -> {
            Collections.addAll(returnValues, fileWrappers);
        });
        return returnValues;
    }
}
