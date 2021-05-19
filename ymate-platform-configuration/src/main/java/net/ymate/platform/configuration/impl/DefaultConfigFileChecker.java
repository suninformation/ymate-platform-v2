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
package net.ymate.platform.configuration.impl;

import net.ymate.platform.commons.impl.DefaultThreadFactory;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.commons.util.ThreadUtils;
import net.ymate.platform.core.configuration.IConfigFileChecker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 默认配置文件状态变化检查器
 *
 * @author 刘镇 (suninformation@163.com) on 2019-04-22 01:35
 */
public class DefaultConfigFileChecker implements IConfigFileChecker, Runnable {

    private static final Log LOG = LogFactory.getLog(DefaultConfigFileChecker.class);

    private final Map<String, Status> fileStatus = new ConcurrentHashMap<>();

    private final ReentrantLock locker = new ReentrantLock();

    private ScheduledExecutorService scheduledExecutorService;

    private boolean initialized;

    @Override
    public void initialize(long timeInterval) {
        if (!initialized) {
            if (timeInterval > 0) {
                scheduledExecutorService = ThreadUtils.newScheduledThreadPool(1, DefaultThreadFactory.create("ConfigFileChecker"));
                scheduledExecutorService.scheduleWithFixedDelay(this, timeInterval, timeInterval, TimeUnit.MILLISECONDS);
                //
                initialized = true;
            }
        }
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void addStatus(Status status) {
        if (status != null) {
            locker.lock();
            try {
                this.fileStatus.put(status.getFilePath(), status);
            } finally {
                locker.unlock();
            }
        }
    }

    @Override
    public void run() {
        locker.lock();
        try {
            fileStatus.values().forEach(status -> {
                try {
                    if (status.check() && LOG.isInfoEnabled()) {
                        LOG.info(String.format("Configuration file [%s] has been reloaded.", status.getFilePath()));
                    }
                } catch (Exception e) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(String.format("An exception occurred while checking configuration file [%s]: ", status.getFilePath()), RuntimeUtils.unwrapThrow(e));
                    }
                }
            });
        } finally {
            locker.unlock();
        }
    }

    @Override
    public void close() {
        if (initialized) {
            initialized = false;
            if (scheduledExecutorService != null) {
                scheduledExecutorService.shutdown();
                scheduledExecutorService = null;
            }
        }
    }
}
