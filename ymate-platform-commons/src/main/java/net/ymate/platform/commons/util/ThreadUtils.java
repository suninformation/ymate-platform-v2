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
        return new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(queueCapacity), threadFactory, new ThreadPoolExecutor.AbortPolicy());
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

    /**
     * @since 2.1.4
     */
    public static <T> T executeOnce(Callable<T> worker, long timeout, int reAwaitTimes) throws InterruptedException, ExecutionException {
        return executeOnce(worker, timeout, reAwaitTimes, new DefaultFutureResultFilter<>());
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

    /**
     * @since 2.1.4
     */
    public static <T> List<T> executeOnce(List<Callable<T>> workers, long timeout, int reAwaitTimes) throws InterruptedException, ExecutionException {
        return executeOnce(workers, timeout, reAwaitTimes, new DefaultFutureResultFilter<>());
    }

    public static <T> List<T> executeOnce(List<Callable<T>> workers, long timeout, int reAwaitTimes, IFutureResultFilter<T> resultFilter) throws InterruptedException, ExecutionException {
        if (workers != null && !workers.isEmpty()) {
            ExecutorService executorService = newFixedThreadPool(workers.size());
            //
            List<FutureTask<T>> futures = new ArrayList<>();
            workers.stream().map(FutureTask::new).forEach(futureTask -> {
                executorService.submit(futureTask);
                futures.add(futureTask);
            });
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

    /**
     * 关闭线程池
     *
     * @param executorService 线程池
     * @since 2.1.4
     */
    public static void shutdownExecutorService(ExecutorService executorService) {
        shutdownExecutorService(executorService, 30000L);
    }

    /**
     * 关闭线程池
     *
     * @param executorService 线程池
     * @param timeout         超时时间
     * @since 2.1.4
     */
    public static void shutdownExecutorService(ExecutorService executorService, long timeout) {
        shutdownExecutorService(executorService, timeout, 0);
    }

    /**
     * 关闭线程池
     *
     * @param executorService 线程池
     * @param timeout         超时时间
     * @param reAwaitTimes    重试次数
     */
    public static void shutdownExecutorService(ExecutorService executorService, long timeout, int reAwaitTimes) {
        if (executorService != null && !executorService.isShutdown()) {
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

    /**
     * 线程池监控器
     *
     * @since 2.1.4
     */
    public static class ThreadPoolMonitor {

        private final int corePoolSize;
        private final int maximumPoolSize;
        private final int poolSize;
        private final int activeCount;
        private final int queueSize;
        private final boolean isShutdown;
        private final boolean isTerminated;

        public ThreadPoolMonitor(int corePoolSize, int maximumPoolSize, int poolSize, int activeCount, int queueSize, boolean isShutdown, boolean isTerminated) {
            this.corePoolSize = corePoolSize;
            this.maximumPoolSize = maximumPoolSize;
            this.poolSize = poolSize;
            this.activeCount = activeCount;
            this.queueSize = queueSize;
            this.isShutdown = isShutdown;
            this.isTerminated = isTerminated;
        }

        public int getCorePoolSize() {
            return corePoolSize;
        }

        public int getMaximumPoolSize() {
            return maximumPoolSize;
        }

        public int getPoolSize() {
            return poolSize;
        }

        public int getActiveCount() {
            return activeCount;
        }

        public int getQueueSize() {
            return queueSize;
        }

        public boolean isShutdown() {
            return isShutdown;
        }

        public boolean isTerminated() {
            return isTerminated;
        }
    }

    /**
     * 线程池构建器
     *
     * @since 2.1.4
     */
    public static class ThreadPoolBuilder {

        private int corePoolSize = 1;
        private int maximumPoolSize = 5;
        private long keepAliveTime = 60;
        private TimeUnit unit = TimeUnit.SECONDS;
        private int queueCapacity = 1024;
        private BlockingQueue<Runnable> workQueue;
        private ThreadFactory threadFactory = DefaultThreadFactory.create();
        private RejectedExecutionHandler handler = new ThreadPoolExecutor.AbortPolicy();
        private boolean scheduled = false;

        private ThreadPoolBuilder() {
        }

        public ThreadPoolBuilder corePoolSize(int corePoolSize) {
            this.corePoolSize = corePoolSize;
            return this;
        }

        public ThreadPoolBuilder maximumPoolSize(int maximumPoolSize) {
            this.maximumPoolSize = maximumPoolSize;
            return this;
        }

        public ThreadPoolBuilder keepAliveTime(long keepAliveTime, TimeUnit unit) {
            this.keepAliveTime = keepAliveTime;
            this.unit = unit;
            return this;
        }

        public ThreadPoolBuilder queueCapacity(int queueCapacity) {
            this.queueCapacity = queueCapacity;
            return this;
        }

        public ThreadPoolBuilder linkedBlockingQueue() {
            this.workQueue = new LinkedBlockingQueue<>(queueCapacity);
            return this;
        }

        public ThreadPoolBuilder arrayBlockingQueue() {
            this.workQueue = new ArrayBlockingQueue<>(queueCapacity);
            return this;
        }

        public ThreadPoolBuilder synchronousQueue() {
            this.workQueue = new SynchronousQueue<>();
            return this;
        }

        public ThreadPoolBuilder priorityBlockingQueue() {
            this.workQueue = new PriorityBlockingQueue<>(queueCapacity);
            return this;
        }

        public ThreadPoolBuilder threadFactory(ThreadFactory threadFactory) {
            this.threadFactory = threadFactory;
            return this;
        }

        public ThreadPoolBuilder abortPolicy() {
            this.handler = new ThreadPoolExecutor.AbortPolicy();
            return this;
        }

        public ThreadPoolBuilder callerRunsPolicy() {
            this.handler = new ThreadPoolExecutor.CallerRunsPolicy();
            return this;
        }

        public ThreadPoolBuilder discardPolicy() {
            this.handler = new ThreadPoolExecutor.DiscardPolicy();
            return this;
        }

        public ThreadPoolBuilder discardOldestPolicy() {
            this.handler = new ThreadPoolExecutor.DiscardOldestPolicy();
            return this;
        }

        public ThreadPoolBuilder scheduled() {
            this.scheduled = true;
            return this;
        }

        public ExecutorService build() {
            if (workQueue == null) {
                workQueue = new LinkedBlockingQueue<>(queueCapacity);
            }
            if (scheduled) {
                return new ScheduledThreadPoolExecutor(corePoolSize, threadFactory, handler);
            }
            return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        }
    }

    /**
     * 创建线程池构建器
     *
     * @return 线程池构建器
     * @since 2.1.4
     */
    public static ThreadPoolBuilder builder() {
        return new ThreadPoolBuilder();
    }

    /**
     * 获取线程池监控信息
     *
     * @param executorService 线程池
     * @return 线程池监控信息
     * @since 2.1.4
     */
    public static ThreadPoolMonitor getThreadPoolMonitor(ExecutorService executorService) {
        if (executorService instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor executor = (ThreadPoolExecutor) executorService;
            return new ThreadPoolMonitor(
                    executor.getCorePoolSize(),
                    executor.getMaximumPoolSize(),
                    executor.getPoolSize(),
                    executor.getActiveCount(),
                    executor.getQueue().size(),
                    executor.isShutdown(),
                    executor.isTerminated()
            );
        }
        return null;
    }

    /**
     * @since 2.1.4
     */
    public static ExecutorService newThreadExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit) {
        return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, new LinkedBlockingQueue<>(1024), DefaultThreadFactory.create(), new ThreadPoolExecutor.AbortPolicy());
    }

    /**
     * @since 2.1.4
     */
    public static ExecutorService newThreadExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, int queueCapacity) {
        return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, new LinkedBlockingQueue<>(queueCapacity), DefaultThreadFactory.create(), new ThreadPoolExecutor.AbortPolicy());
    }

    /**
     * @since 2.1.4
     */
    public static ExecutorService newThreadExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, int queueCapacity, ThreadFactory threadFactory) {
        return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, new LinkedBlockingQueue<>(queueCapacity), threadFactory, new ThreadPoolExecutor.AbortPolicy());
    }

    /**
     * @since 2.1.4
     */
    public static ExecutorService newThreadExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, int queueCapacity, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, new LinkedBlockingQueue<>(queueCapacity), threadFactory, handler);
    }

    /**
     * @since 2.1.4
     */
    public static ExecutorService newCachedThreadPool(long keepAliveTime, TimeUnit unit) {
        return new ThreadPoolExecutor(0, 1024, keepAliveTime, unit, new SynchronousQueue<>(), DefaultThreadFactory.create());
    }

    /**
     * @since 2.1.4
     */
    public static ExecutorService newCachedThreadPool(int maximumPoolSize, long keepAliveTime, TimeUnit unit) {
        return new ThreadPoolExecutor(0, maximumPoolSize, keepAliveTime, unit, new SynchronousQueue<>(), DefaultThreadFactory.create());
    }

    /**
     * @since 2.1.4
     */
    public static ExecutorService newCachedThreadPool(int maximumPoolSize, long keepAliveTime, TimeUnit unit, ThreadFactory threadFactory) {
        return new ThreadPoolExecutor(0, maximumPoolSize, keepAliveTime, unit, new SynchronousQueue<>(), threadFactory);
    }

    /**
     * @since 2.1.4
     */
    public static ExecutorService newFixedThreadPool(int nThreads, long keepAliveTime, TimeUnit unit) {
        return new ThreadPoolExecutor(nThreads, nThreads, keepAliveTime, unit, new LinkedBlockingQueue<>(1024), DefaultThreadFactory.create());
    }

    /**
     * @since 2.1.4
     */
    public static ExecutorService newFixedThreadPool(int nThreads, long keepAliveTime, TimeUnit unit, int queueCapacity) {
        return new ThreadPoolExecutor(nThreads, nThreads, keepAliveTime, unit, new LinkedBlockingQueue<>(queueCapacity), DefaultThreadFactory.create());
    }

    /**
     * @since 2.1.4
     */
    public static ExecutorService newFixedThreadPool(int nThreads, long keepAliveTime, TimeUnit unit, int queueCapacity, ThreadFactory threadFactory) {
        return new ThreadPoolExecutor(nThreads, nThreads, keepAliveTime, unit, new LinkedBlockingQueue<>(queueCapacity), threadFactory);
    }

    /**
     * 执行单次任务（带超时）
     *
     * @param task    任务
     * @param timeout 超时时间
     * @param <T>     结果类型
     * @return 结果
     * @throws Exception 执行异常
     * @since 2.1.4
     */
    public static <T> T executeOnce(Callable<T> task, long timeout, TimeUnit unit) throws Exception {
        return executeOnce(task, unit.toMillis(timeout));
    }

    /**
     * 执行单次任务（多任务）
     *
     * @param tasks   任务列表
     * @param timeout 超时时间
     * @param <T>     结果类型
     * @return 结果列表
     * @throws Exception 执行异常
     * @since 2.1.4
     */
    public static <T> List<T> executeOnce(List<Callable<T>> tasks, long timeout, TimeUnit unit) throws Exception {
        return executeOnce(tasks, unit.toMillis(timeout));
    }
}
