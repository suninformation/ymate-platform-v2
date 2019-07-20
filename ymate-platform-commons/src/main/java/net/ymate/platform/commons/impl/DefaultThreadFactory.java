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
package net.ymate.platform.commons.impl;

import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 刘镇 (suninformation@163.com) on 2017/12/11 上午3:29
 */
public class DefaultThreadFactory implements ThreadFactory {

    private static final AtomicInteger POOL_NUMBER = new AtomicInteger(1);

    private final ThreadGroup group;

    private final AtomicInteger threadNumber = new AtomicInteger(1);

    private final String namePrefix;

    private boolean daemon;

    private int priority = Thread.NORM_PRIORITY;

    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

    public static ThreadFactory create() {
        return new DefaultThreadFactory();
    }

    public static ThreadFactory create(String prefix) {
        return new DefaultThreadFactory(prefix);
    }

    public DefaultThreadFactory() {
        this(null);
    }

    public DefaultThreadFactory(String prefix) {
        SecurityManager securityManager = System.getSecurityManager();
        group = (securityManager != null) ? securityManager.getThreadGroup() : Thread.currentThread().getThreadGroup();
        namePrefix = prefixFixed(StringUtils.defaultIfBlank(prefix, "ymp-pool")) + POOL_NUMBER.getAndIncrement() + "-thread";
    }

    private String prefixFixed(String prefix) {
        if (!StringUtils.endsWith(prefix, "-")) {
            prefix += '-';
        }
        return prefix;
    }

    public DefaultThreadFactory daemon(boolean daemon) {
        this.daemon = daemon;
        return this;
    }

    public DefaultThreadFactory priority(int priority) {
        this.priority = priority;
        return this;
    }

    public DefaultThreadFactory uncaughtExceptionHandler(Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
        this.uncaughtExceptionHandler = uncaughtExceptionHandler;
        return this;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(group, r, prefixFixed(namePrefix) + threadNumber.getAndIncrement(), 0);
        if (daemon) {
            thread.setDaemon(true);
        }
        if (priority > 0) {
            thread.setPriority(priority);
        }
        if (uncaughtExceptionHandler != null) {
            thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
        }
        return thread;
    }
}
