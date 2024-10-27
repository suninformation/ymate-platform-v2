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

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author 刘镇 (suninformation@163.com) on 15/5/10 上午2:45
 */
public class MimeTypeUtils {

    private static final Map<String, String> MIME_TYPE_MAPS = new HashMap<>();

    private static final Map<String, String> FILE_EXT_MAPS = new HashMap<>();

    static void doLoadMimeTypes(InputStream inputStream) throws IOException {
        if (inputStream != null) {
            Properties configs = new Properties();
            configs.load(inputStream);
            configs.keySet().forEach((key) -> {
                String[] values = StringUtils.split(configs.getProperty((String) key, StringUtils.EMPTY), "|");
                if (values != null && values.length > 0) {
                    FILE_EXT_MAPS.put((String) key, values[0]);
                    for (String value : values) {
                        MIME_TYPE_MAPS.put(value, (String) key);
                    }
                }
            });
        }
    }

    static {
        String[] resourceNames = {"META-INF/mimetypes-default-conf.properties", "mimetypes-conf.properties"};
        for (String resourceName : resourceNames) {
            try (InputStream inputStream = MimeTypeUtils.class.getClassLoader().getResourceAsStream(resourceName)) {
                doLoadMimeTypes(inputStream);
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * @param extName 文件扩展名
     * @return 根据文件扩展名获取对应的MIME_TYPE类型
     */
    public static String getFileMimeType(String extName) {
        if (StringUtils.isBlank(extName)) {
            return null;
        }
        if (extName.charAt(0) == '.') {
            extName = extName.substring(1);
        }
        return MIME_TYPE_MAPS.get(extName);
    }

    /**
     * @param file 文件
     * @return 根据文件扩展名获取对应的MIME_TYPE类型
     * @since 2.1.3
     */
    public static String getFileMimeType(File file) {
        if (file == null) {
            return null;
        }
        return getFileMimeType(FileUtils.getExtName(file));
    }

    /**
     * @param mimeType MIME_TYPE类型
     * @return 根据MIME_TYPE类型获取对应的文件扩展名
     */
    public static String getFileExtName(String mimeType) {
        if (StringUtils.isBlank(mimeType)) {
            return null;
        }
        return FILE_EXT_MAPS.get(mimeType);
    }
}
