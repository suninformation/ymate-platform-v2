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
package net.ymate.platform.core.configuration;

import net.ymate.platform.commons.util.FileUtils;
import net.ymate.platform.core.beans.annotation.Ignored;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;

/**
 * 配置文件状态变化检查器接口
 *
 * @author 刘镇 (suninformation@163.com) on 2021/5/18 5:32 下午
 * @since 2.1.0
 */
@Ignored
public interface IConfigFileChecker extends AutoCloseable {

    /**
     * 初始化
     *
     * @param timeInterval 时间间隔（毫秒）
     * @throws Exception 初始过程中产生的任何异常
     */
    void initialize(long timeInterval) throws Exception;

    /**
     * 是否已初始化
     *
     * @return 返回true表示已初始化
     */
    boolean isInitialized();

    /**
     * 添加配置文件状态
     *
     * @param status 配置文件状态对象
     */
    void addStatus(Status status);

    /**
     * 配置文件状态
     */
    class Status {

        private final IConfiguration configuration;

        private final String filePath;

        private final String fileHash;

        private long lastModifyTime;

        public Status(IConfiguration configuration, File targetFile) throws IOException {
            this(configuration, targetFile.getPath(), FileUtils.getHash(targetFile), targetFile.lastModified());
        }

        public Status(IConfiguration configuration, String filePath, String fileHash, long lastModifyTime) {
            this.configuration = configuration;
            this.filePath = filePath;
            this.fileHash = fileHash;
            this.lastModifyTime = lastModifyTime;
        }

        IConfiguration getConfiguration() {
            return configuration;
        }

        public String getFilePath() {
            return filePath;
        }

        public String getFileHash() {
            return fileHash;
        }

        public long getLastModifyTime() {
            return lastModifyTime;
        }

        public boolean check() throws Exception {
            File targetFile = new File(filePath);
            if (targetFile.lastModified() != lastModifyTime) {
                this.lastModifyTime = targetFile.lastModified();
                String hash = FileUtils.getHash(targetFile);
                if (!StringUtils.equals(fileHash, hash)) {
                    configuration.reload();
                    return true;
                }
            }
            return false;
        }
    }
}
