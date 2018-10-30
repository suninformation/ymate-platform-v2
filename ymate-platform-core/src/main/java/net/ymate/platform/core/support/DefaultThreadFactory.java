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
package net.ymate.platform.core.support;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 刘镇 (suninformation@163.com) on 2017/12/11 上午3:29
 * @version 1.0
 */
public class DefaultThreadFactory implements ThreadFactory {

    private static final AtomicInteger __poolNumber = new AtomicInteger(1);

    private final ThreadGroup __group;

    private final AtomicInteger __threadNumber = new AtomicInteger(1);

    private final String __namePrefix;

    private boolean __daemon;

    private int __priority = Thread.NORM_PRIORITY;

    private Thread.UncaughtExceptionHandler __uncaughtExceptionHandler;

    public static ExecutorService newThreadExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime) {
        return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(1024), create(), new ThreadPoolExecutor.AbortPolicy());
    }

    public static ExecutorService newThreadExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, int queueCapacity) {
        return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(queueCapacity), create(), new ThreadPoolExecutor.AbortPolicy());
    }

    public static ExecutorService newThreadExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, int queueCapacity, ThreadFactory threadFactory) {
        return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(queueCapacity), threadFactory, new ThreadPoolExecutor.AbortPolicy());
    }

    public static ExecutorService newThreadExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, int queueCapacity, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(queueCapacity), threadFactory, handler);
    }

    public static ExecutorService newSingleThreadExecutor() {
        return new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(1024), create(), new ThreadPoolExecutor.AbortPolicy());
    }

    public static ExecutorService newSingleThreadExecutor(int queueCapacity) {
        return new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(queueCapacity), create(), new ThreadPoolExecutor.AbortPolicy());
    }

    public static ExecutorService newSingleThreadExecutor(int queueCapacity, ThreadFactory threadFactory) {
        return new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(1024), threadFactory, new ThreadPoolExecutor.AbortPolicy());
    }

    public static ScheduledExecutorService newSingleThreadScheduledExecutor() {
        return new ScheduledThreadPoolExecutor(1, create(), new ThreadPoolExecutor.AbortPolicy());
    }

    public static ScheduledExecutorService newSingleThreadScheduledExecutor(ThreadFactory threadFactory) {
        return new ScheduledThreadPoolExecutor(1, threadFactory, new ThreadPoolExecutor.AbortPolicy());
    }

    public static ExecutorService newCachedThreadPool() {
        return new ThreadPoolExecutor(0, 1024, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
    }

    public static ExecutorService newCachedThreadPool(int maximumPoolSize) {
        return new ThreadPoolExecutor(0, maximumPoolSize, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
    }

    public static ExecutorService newCachedThreadPool(ThreadFactory threadFactory) {
        return new ThreadPoolExecutor(0, 1024, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), threadFactory);
    }

    public static ExecutorService newCachedThreadPool(int maximumPoolSize, long keepAliveTime) {
        return new ThreadPoolExecutor(0, maximumPoolSize, keepAliveTime, TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>());
    }

    public static ExecutorService newCachedThreadPool(int maximumPoolSize, long keepAliveTime, ThreadFactory threadFactory) {
        return new ThreadPoolExecutor(0, maximumPoolSize, keepAliveTime, TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>(), threadFactory);
    }

    public static ExecutorService newFixedThreadPool(int nThreads) {
        return new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(1024));
    }

    public static ExecutorService newFixedThreadPool(int nThreads, int queueCapacity) {
        return new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(queueCapacity));
    }

    public static ExecutorService newFixedThreadPool(int nThreads, int queueCapacity, ThreadFactory threadFactory) {
        return new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(queueCapacity), threadFactory);
    }

    public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize) {
        return new ScheduledThreadPoolExecutor(corePoolSize, create());
    }

    public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize, ThreadFactory threadFactory) {
        return new ScheduledThreadPoolExecutor(corePoolSize, threadFactory);
    }

    public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        return new ScheduledThreadPoolExecutor(corePoolSize, threadFactory, handler);
    }

    //

    public static <T> T executeOnce(Callable<T> worker) throws InterruptedException, ExecutionException {
        return executeOnce(worker, 0L);
    }

    public static <T> T executeOnce(Callable<T> worker, long timeout) throws InterruptedException, ExecutionException {
        FutureTask<T> _future = new FutureTask<T>(worker);
        //
        ExecutorService _executorService = newSingleThreadExecutor();
        _executorService.submit(_future);
        _executorService.shutdown();
        _executorService.awaitTermination(timeout > 0L ? timeout : 30L, TimeUnit.SECONDS);
        //
        return _future.get();
    }

    public static <T> List<T> executeOnce(List<Callable<T>> workers) throws InterruptedException, ExecutionException {
        return executeOnce(workers, 0L);
    }

    public static <T> List<T> executeOnce(List<Callable<T>> workers, long timeout) throws InterruptedException, ExecutionException {
        if (workers != null && !workers.isEmpty()) {
            ExecutorService _executorService = newFixedThreadPool(workers.size());
            //
            List<FutureTask<T>> _futures = new ArrayList<FutureTask<T>>();
            for (Callable<T> _worker : workers) {
                FutureTask<T> _future = new FutureTask<T>(_worker);
                _executorService.submit(_future);
                _futures.add(_future);
            }
            _executorService.shutdown();
            _executorService.awaitTermination(timeout > 0L ? timeout : 30L, TimeUnit.SECONDS);
            //
            List<T> _results = new ArrayList<T>();
            for (FutureTask<T> _future : _futures) {
                _results.add(_future.get());
            }
            return _results;
        }
        return Collections.emptyList();
    }

    public static ThreadFactory create() {
        return new DefaultThreadFactory();
    }

    public static ThreadFactory create(String prefix) {
        return new DefaultThreadFactory(prefix);
    }

    public DefaultThreadFactory() {
        this("ymp-pool-");
    }

    public DefaultThreadFactory(String prefix) {
        if (StringUtils.isBlank(prefix)) {
            throw new NullArgumentException("prefix");
        }
        SecurityManager _securityManager = System.getSecurityManager();
        __group = (_securityManager != null) ? _securityManager.getThreadGroup() : Thread.currentThread().getThreadGroup();
        __namePrefix = prefix + __poolNumber.getAndIncrement() + "-thread-";
    }

    public DefaultThreadFactory daemon(boolean daemon) {
        __daemon = daemon;
        return this;
    }

    public DefaultThreadFactory priority(int priority) {
        __priority = priority;
        return this;
    }

    public DefaultThreadFactory uncaughtExceptionHandler(Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
        __uncaughtExceptionHandler = uncaughtExceptionHandler;
        return this;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread _thread = new Thread(__group, r, __namePrefix + __threadNumber.getAndIncrement(), 0);
        if (__daemon) {
            _thread.setDaemon(true);
        }
        if (__priority > 0) {
            _thread.setPriority(__priority);
        }
        if (__uncaughtExceptionHandler != null) {
            _thread.setUncaughtExceptionHandler(__uncaughtExceptionHandler);
        }
        return _thread;
    }
}
