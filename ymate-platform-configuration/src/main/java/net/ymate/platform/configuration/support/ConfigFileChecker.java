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
package net.ymate.platform.configuration.support;

import net.ymate.platform.commons.impl.DefaultThreadFactory;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.commons.util.ThreadUtils;
import net.ymate.platform.core.configuration.IConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 配置文件状态变化检查器
 *
 * @author 刘镇 (suninformation@163.com) on 2019-04-22 01:35
 */
public class ConfigFileChecker implements Runnable {

    private static final Log LOG = LogFactory.getLog(ConfigFileChecker.class);

    private final Map<String, FileStatus> fileStatus = new ConcurrentHashMap<>();

    private final ReentrantLock locker = new ReentrantLock();

    private ScheduledExecutorService scheduledExecutorService;

    private final long timeInterval;

    private boolean initialized;

    public ConfigFileChecker(long timeInterval) {
        this.timeInterval = timeInterval;
        if (this.timeInterval > 0) {
            scheduledExecutorService = ThreadUtils.newScheduledThreadPool(1, DefaultThreadFactory.create("ConfigFileChangedChecker"));
            initialized = true;
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void putFileStatus(String fileName, FileStatus fileStatus) {
        if (StringUtils.isNotBlank(fileName) && fileStatus != null) {
            locker.lock();
            try {
                this.fileStatus.put(fileName, fileStatus);
            } finally {
                locker.unlock();
            }
        }
    }

    public void start() {
        if (initialized && scheduledExecutorService != null) {
            scheduledExecutorService.scheduleWithFixedDelay(this, timeInterval, timeInterval, TimeUnit.MILLISECONDS);
        }
    }

    public void stop() {
        if (initialized && scheduledExecutorService != null) {
            scheduledExecutorService.shutdown();
            scheduledExecutorService = null;
        }
    }

    @Override
    public void run() {
        locker.lock();
        try {
            fileStatus.forEach((key, value) -> {
                File file = new File(key);
                if (file.lastModified() != value.getLastModifyTime()) {
                    try {
                        value.getConfiguration().reload();
                        value.setLastModifyTime(file.lastModified());
                    } catch (Exception e) {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn(String.format("An exception occurred while checking configuration file[%s]: ", file.getPath()), RuntimeUtils.unwrapThrow(e));
                        }
                    }
                }
            });
        } finally {
            locker.unlock();
        }
    }

    /**
     * 配置文件状态
     */
    public static class FileStatus {

        private final IConfiguration configuration;

        private long lastModifyTime;

        public FileStatus(IConfiguration configuration, long lastModifyTime) {
            this.configuration = configuration;
            this.lastModifyTime = lastModifyTime;
        }

        IConfiguration getConfiguration() {
            return configuration;
        }

        long getLastModifyTime() {
            return lastModifyTime;
        }

        void setLastModifyTime(long lastModifyTime) {
            this.lastModifyTime = lastModifyTime;
        }
    }
}
