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


import net.ymate.platform.commons.impl.DefaultThreadFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

/**
 * 线程操作工具类
 *
 * @author 刘镇 (suninformation@163.com) on 2018/11/12 12:06 AM
 * @since 2.0.6
 */
public final class ThreadUtils {

    public static ExecutorService newThreadExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime) {
        return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(1024), DefaultThreadFactory.create(), new ThreadPoolExecutor.AbortPolicy());
    }

    public static ExecutorService newThreadExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, int queueCapacity) {
        return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(queueCapacity), DefaultThreadFactory.create(), new ThreadPoolExecutor.AbortPolicy());
    }

    public static ExecutorService newThreadExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, int queueCapacity, ThreadFactory threadFactory) {
        return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(queueCapacity), threadFactory, new ThreadPoolExecutor.AbortPolicy());
    }

    public static ExecutorService newThreadExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, int queueCapacity, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(queueCapacity), threadFactory, handler);
    }

    public static ExecutorService newSingleThreadExecutor() {
        return new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(1024), DefaultThreadFactory.create(), new ThreadPoolExecutor.AbortPolicy());
    }

    public static ExecutorService newSingleThreadExecutor(int queueCapacity) {
        return new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(queueCapacity), DefaultThreadFactory.create(), new ThreadPoolExecutor.AbortPolicy());
    }

    public static ExecutorService newSingleThreadExecutor(int queueCapacity, ThreadFactory threadFactory) {
        return new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(1024), threadFactory, new ThreadPoolExecutor.AbortPolicy());
    }

    public static ScheduledExecutorService newSingleThreadScheduledExecutor() {
        return new ScheduledThreadPoolExecutor(1, DefaultThreadFactory.create(), new ThreadPoolExecutor.AbortPolicy());
    }

    public static ScheduledExecutorService newSingleThreadScheduledExecutor(ThreadFactory threadFactory) {
        return new ScheduledThreadPoolExecutor(1, threadFactory, new ThreadPoolExecutor.AbortPolicy());
    }

    public static ExecutorService newCachedThreadPool() {
        return new ThreadPoolExecutor(0, 1024, 60000L, TimeUnit.MILLISECONDS, new SynchronousQueue<>(), DefaultThreadFactory.create());
    }

    public static ExecutorService newCachedThreadPool(int maximumPoolSize) {
        return new ThreadPoolExecutor(0, maximumPoolSize, 60000L, TimeUnit.MILLISECONDS, new SynchronousQueue<>(), DefaultThreadFactory.create());
    }

    public static ExecutorService newCachedThreadPool(ThreadFactory threadFactory) {
        return new ThreadPoolExecutor(0, 1024, 60000L, TimeUnit.MILLISECONDS, new SynchronousQueue<>(), threadFactory);
    }

    public static ExecutorService newCachedThreadPool(int maximumPoolSize, long keepAliveTime) {
        return new ThreadPoolExecutor(0, maximumPoolSize, keepAliveTime, TimeUnit.MILLISECONDS, new SynchronousQueue<>(), DefaultThreadFactory.create());
    }

    public static ExecutorService newCachedThreadPool(int maximumPoolSize, long keepAliveTime, ThreadFactory threadFactory) {
        return new ThreadPoolExecutor(0, maximumPoolSize, keepAliveTime, TimeUnit.MILLISECONDS, new SynchronousQueue<>(), threadFactory);
    }

    public static ExecutorService newFixedThreadPool(int nThreads) {
        return new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(1024), DefaultThreadFactory.create());
    }

    public static ExecutorService newFixedThreadPool(int nThreads, int queueCapacity) {
        return new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(queueCapacity), DefaultThreadFactory.create());
    }

    public static ExecutorService newFixedThreadPool(int nThreads, int queueCapacity, ThreadFactory threadFactory) {
        return new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(queueCapacity), threadFactory);
    }

    public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize) {
        return new ScheduledThreadPoolExecutor(corePoolSize, DefaultThreadFactory.create());
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
        return executeOnce(worker, timeout, new DefaultFutureResultFilter<>());
    }

    public static <T> T executeOnce(Callable<T> worker, long timeout, IFutureResultFilter<T> resultFilter) throws InterruptedException, ExecutionException {
        return executeOnce(worker, timeout, 0, resultFilter);
    }

    public static <T> T executeOnce(Callable<T> worker, long timeout, int reAwaitTimes, IFutureResultFilter<T> resultFilter) throws InterruptedException, ExecutionException {
        FutureTask<T> future = new FutureTask<>(worker);
        //
        ExecutorService executorService = newSingleThreadExecutor();
        executorService.submit(future);
        shutdownExecutorService(executorService, timeout, reAwaitTimes);
        //
        if (resultFilter != null) {
            return resultFilter.filter(future);
        }
        return future.get();
    }

    public static <T> List<T> executeOnce(List<Callable<T>> workers) throws InterruptedException, ExecutionException {
        return executeOnce(workers, 0L);
    }

    public static <T> List<T> executeOnce(List<Callable<T>> workers, long timeout) throws InterruptedException, ExecutionException {
        return executeOnce(workers, timeout, new DefaultFutureResultFilter<>());
    }

    public static <T> List<T> executeOnce(List<Callable<T>> workers, long timeout, IFutureResultFilter<T> resultFilter) throws InterruptedException, ExecutionException {
        return executeOnce(workers, timeout, 0, resultFilter);
    }

    public static <T> List<T> executeOnce(List<Callable<T>> workers, long timeout, int reAwaitTimes, IFutureResultFilter<T> resultFilter) throws InterruptedException, ExecutionException {
        if (workers != null && !workers.isEmpty()) {
            ExecutorService executorService = newFixedThreadPool(workers.size());
            //
            List<FutureTask<T>> futures = new ArrayList<>();
            workers.stream().map(FutureTask::new).peek(executorService::submit).forEachOrdered(futures::add);
            shutdownExecutorService(executorService, timeout, reAwaitTimes);
            //
            List<T> results = new ArrayList<>();
            for (FutureTask<T> future : futures) {
                T result;
                if (resultFilter != null) {
                    result = resultFilter.filter(future);
                } else {
                    result = future.get();
                }
                if (result != null) {
                    results.add(result);
                }
            }
            return results;
        }
        return Collections.emptyList();
    }

    public static void shutdownExecutorService(ExecutorService executorService, long timeout, int reAwaitTimes) {
        try {
            executorService.shutdown();
            boolean flag = executorService.awaitTermination(timeout > 0L ? timeout : 30000L, TimeUnit.MILLISECONDS);
            if (!flag) {
                if (reAwaitTimes > 0) {
                    while (reAwaitTimes > 0) {
                        flag = executorService.awaitTermination(timeout > 0L ? timeout : 30000L, TimeUnit.MILLISECONDS);
                        if (flag) {
                            break;
                        }
                        reAwaitTimes--;
                    }
                }
            }
            if (!flag) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }

    /**
     * 执行结果过滤器
     *
     * @param <T> 结果对象类型
     */
    public interface IFutureResultFilter<T> {

        /**
         * 对执行结果进行逻辑判断
         *
         * @param futureTask Future任务对象
         * @return 返回值对象将被放置在最终方法执行结果集合中
         * @throws ExecutionException   如果计算抛出异常
         * @throws InterruptedException 如果当前的线程在等待时被中断
         */
        T filter(FutureTask<T> futureTask) throws ExecutionException, InterruptedException;
    }

    public final static class DefaultFutureResultFilter<T> implements IFutureResultFilter<T> {

        @Override
        public T filter(FutureTask<T> futureTask) throws ExecutionException, InterruptedException {
            if (futureTask.isDone()) {
                return futureTask.get();
            }
            return null;
        }
    }
}
