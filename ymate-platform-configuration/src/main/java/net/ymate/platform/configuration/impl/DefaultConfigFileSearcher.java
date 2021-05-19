/*
 * Copyright 2007-2021 the original author or authors.
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
package net.ymate.platform.configuration.impl;

import net.ymate.platform.commons.util.ResourceUtils;
import net.ymate.platform.core.configuration.IConfig;
import net.ymate.platform.core.configuration.IConfigFileSearcher;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;

import static net.ymate.platform.commons.util.FileUtils.FILE_PREFIX_JAR;

/**
 * 默认配置文件搜索器
 *
 * @author 刘镇 (suninformation@163.com) on 2021/5/18 11:49 下午
 * @since 2.1.0
 */
public class DefaultConfigFileSearcher implements IConfigFileSearcher {

    private IConfig owner;

    private boolean initialized;

    @Override
    public void initialize(IConfig owner) {
        if (!initialized) {
            this.owner = owner;
            initialized = true;
        }
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void close() {
        if (initialized) {
            initialized = false;
            owner = null;
        }
    }

    @Override
    public File search(String cfgFile) {
        if (initialized && owner.isInitialized() && StringUtils.isNotBlank(cfgFile)) {
            // 若指定的 cfgFile 为文件绝对路径名，则直接返回
            File result = new File(cfgFile);
            if (result.isAbsolute() && result.canRead() && result.isFile() && result.exists()) {
                return result;
            }
            // 按路径顺序寻找 cfgFile 指定的文件
            String[] paths = {owner.getModuleHome(), owner.getProjectHome(), owner.getConfigHome(), owner.getUserDir(), owner.getUserHome()};
            for (String path : paths) {
                result = new File(path, cfgFile);
                if (result.isAbsolute() && result.canRead() && result.isFile() && result.exists()) {
                    return result;
                }
            }
        }
        return null;
    }

    @Override
    public String searchAsPath(String cfgFile) {
        if (StringUtils.isNotBlank(cfgFile)) {
            if (cfgFile.startsWith(FILE_PREFIX_JAR)) {
                return cfgFile;
            }
            File targetFile = search(cfgFile);
            if (targetFile == null) {
                URL targetFileUrl = ResourceUtils.getResource(cfgFile, this.getClass());
                if (targetFileUrl != null) {
                    return targetFileUrl.toString();
                }
            }
            if (targetFile != null) {
                return targetFile.getPath();
            }
        }
        return null;
    }

    @Override
    public InputStream searchAsStream(String cfgFile) {
        String filePath = searchAsPath(cfgFile);
        try {
            return filePath != null ? new FileInputStream(new File(filePath)) : null;
        } catch (FileNotFoundException e) {
            return null;
        }
    }
}
