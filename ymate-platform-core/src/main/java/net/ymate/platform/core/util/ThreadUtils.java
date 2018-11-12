/*
 * Copyright 2007-2018 the original author or authors.
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
package net.ymate.platform.core.util;

import net.ymate.platform.core.support.DefaultThreadFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

/**
 * 线程操作工具类
 *
 * @author 刘镇 (suninformation@163.com) on 2018/11/12 12:06 AM
 * @version 1.0
 * @since 2.0.6
 */
public final class ThreadUtils {

    public static ExecutorService newThreadExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime) {
        return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(1024), createFactory(), new ThreadPoolExecutor.AbortPolicy());
    }

    public static ExecutorService newThreadExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, int queueCapacity) {
        return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(queueCapacity), createFactory(), new ThreadPoolExecutor.AbortPolicy());
    }

    public static ExecutorService newThreadExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, int queueCapacity, ThreadFactory threadFactory) {
        return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(queueCapacity), threadFactory, new ThreadPoolExecutor.AbortPolicy());
    }

    public static ExecutorService newThreadExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, int queueCapacity, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(queueCapacity), threadFactory, handler);
    }

    public static ExecutorService newSingleThreadExecutor() {
        return new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(1024), createFactory(), new ThreadPoolExecutor.AbortPolicy());
    }

    public static ExecutorService newSingleThreadExecutor(int queueCapacity) {
        return new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(queueCapacity), createFactory(), new ThreadPoolExecutor.AbortPolicy());
    }

    public static ExecutorService newSingleThreadExecutor(int queueCapacity, ThreadFactory threadFactory) {
        return new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(1024), threadFactory, new ThreadPoolExecutor.AbortPolicy());
    }

    public static ScheduledExecutorService newSingleThreadScheduledExecutor() {
        return new ScheduledThreadPoolExecutor(1, createFactory(), new ThreadPoolExecutor.AbortPolicy());
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
        return new ScheduledThreadPoolExecutor(corePoolSize, createFactory());
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

    public static ThreadFactory createFactory() {
        return new DefaultThreadFactory();
    }

    public static ThreadFactory createFactory(String prefix) {
        return new DefaultThreadFactory(prefix);
    }
}
