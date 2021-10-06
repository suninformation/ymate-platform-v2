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
package net.ymate.platform.commons;

import net.ymate.platform.commons.impl.DefaultThreadFactory;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.commons.util.ThreadUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 可执行队列服务
 *
 * @param <E> 队列元素类型
 * @author 刘镇 (suninformation@163.com) on 2018/5/23 上午1:35
 */
public class ExecutableQueue<E extends Serializable> implements AutoCloseable {

    private static final Log LOG = LogFactory.getLog(ExecutableQueue.class);

    private static final String THREAD_NAME_PREFIX = "ExecutableQueue";

    private ExecutorService executor;

    private BlockingQueue<E> queue;

    private final BlockingQueue<Runnable> workQueue;

    private long queueTimeout;

    private Map<String, IListener<E>> listeners;

    private Semaphore semaphore;

    private ExecutorService innerExecutorService;

    private String prefix;

    private boolean stopped;

    private boolean closed;

    private Speedometer speedometer;

    public ExecutableQueue() {
        this(null, 0, 0, 0, 0, 0, 0, null);
    }

    /**
     * @param prefix 队列名称前缀
     */
    public ExecutableQueue(String prefix) {
        this(prefix, 0, 0, 0, 0, 0, 0, null);
    }

    /**
     * @param rejectedExecutionHandler 拒绝策略
     */
    public ExecutableQueue(RejectedExecutionHandler rejectedExecutionHandler) {
        this(null, 0, 0, 0, 0, 0, 0, rejectedExecutionHandler);
    }

    /**
     * @param concurrentCount          并发数量
     * @param rejectedExecutionHandler 拒绝策略
     */
    public ExecutableQueue(int concurrentCount, RejectedExecutionHandler rejectedExecutionHandler) {
        this(null, 0, 0, 0, 0, 0, concurrentCount, rejectedExecutionHandler);
    }

    /**
     * @param prefix                   队列名称前缀
     * @param concurrentCount          并发数量
     * @param rejectedExecutionHandler 拒绝策略
     */
    public ExecutableQueue(String prefix, int concurrentCount, RejectedExecutionHandler rejectedExecutionHandler) {
        this(prefix, 0, 0, 0, 0, 0, concurrentCount, rejectedExecutionHandler);
    }

    /**
     * @param prefix                   队列名称前缀
     * @param minPoolSize              线程池初始大小
     * @param maxPoolSize              最大线程数
     * @param workQueueSize            工作队列大小
     * @param queueTimeout             队列等待超时时间(秒), 默认30秒
     * @param queueSize                队列大小
     * @param concurrentCount          并发数量
     * @param rejectedExecutionHandler 拒绝策略
     */
    public ExecutableQueue(String prefix, int minPoolSize, int maxPoolSize, int workQueueSize, long queueTimeout, int queueSize, int concurrentCount, RejectedExecutionHandler rejectedExecutionHandler) {
        if (StringUtils.isBlank(prefix)) {
            prefix = THREAD_NAME_PREFIX;
        }
        if (minPoolSize <= 0) {
            minPoolSize = Runtime.getRuntime().availableProcessors();
        }
        if (maxPoolSize <= 0) {
            maxPoolSize = 100;
        }
        if (maxPoolSize < minPoolSize) {
            maxPoolSize = minPoolSize;
        }
        if (concurrentCount > 0 && concurrentCount > maxPoolSize) {
            maxPoolSize = concurrentCount;
        }
        rejectedExecutionHandler = rejectedExecutionHandler != null ? rejectedExecutionHandler : new ThreadPoolExecutor.AbortPolicy();
        //
        workQueue = new LinkedBlockingQueue<>(workQueueSize > 0 ? workQueueSize : Integer.MAX_VALUE);
        //
        init(new ThreadPoolExecutor(minPoolSize, maxPoolSize, 0L, TimeUnit.MILLISECONDS, workQueue, DefaultThreadFactory.create(prefix), rejectedExecutionHandler), prefix, queueTimeout, queueSize, concurrentCount);
    }

    private void init(ExecutorService executorService, String prefix, long queueTimeout, int queueSize, int concurrentCount) {
        this.prefix = prefix;
        executor = executorService;
        queue = new LinkedBlockingQueue<>(queueSize > 0 ? queueSize : Integer.MAX_VALUE);
        this.queueTimeout = queueTimeout > 0 ? queueTimeout : 30L;
        if (concurrentCount > 0) {
            semaphore = new Semaphore(concurrentCount);
        }
        //
        speedometer = new Speedometer(this.prefix);
        listeners = new ConcurrentHashMap<>(16);
    }

    /**
     * 当监听线程开启时被调用
     */
    protected void onListenStarted() {
        if (LOG.isInfoEnabled()) {
            LOG.info(String.format("ExecutableQueue Service [%s] Listener Service Started.", prefix));
        }
    }

    /**
     * 当监听线程停止时被调用
     */
    protected void onListenStopped() {
        if (LOG.isInfoEnabled()) {
            LOG.info(String.format("ExecutableQueue Service [%s] Listener Service Stopped.", prefix));
        }
    }

    /**
     * 当添加新监听器时被调用
     *
     * @param id       监听器ID
     * @param listener 监听器对象
     */
    protected void onListenerAdded(String id, IListener<E> listener) {
        if (LOG.isInfoEnabled()) {
            LOG.info(String.format("ExecutableQueue Service [%s] Add Listener [%s@%s].", prefix, id, listener.getClass().getName()));
        }
    }

    /**
     * 当移除监听器时被调用
     *
     * @param id       监听器ID
     * @param listener 监听器对象（可能为空）
     */
    protected void onListenerRemoved(String id, IListener<E> listener) {
        if (LOG.isInfoEnabled()) {
            LOG.info(String.format("ExecutableQueue Service [%s] Remove Listener [%s@%s].", prefix, id, listener == null ? "unknown" : listener.getClass().getName()));
        }
    }

    /**
     * 当向队列添加新元素时被调用
     *
     * @param element 新元素对象
     */
    protected void onElementAdded(E element) {
        if (LOG.isInfoEnabled()) {
            LOG.info(String.format("ExecutableQueue Service [%s] Add Element [%s].", prefix, element.toString()));
        }
    }

    /**
     * 当丢弃队列中的元素时被调用
     *
     * @param element 元素对象
     */
    protected void onElementAbandoned(E element) {
        if (LOG.isInfoEnabled()) {
            LOG.info(String.format("ExecutableQueue Service [%s] Abandon Element [%s].", prefix, element.toString()));
        }
    }

    /**
     * 启动队列监听服务线程
     */
    public synchronized void listenStart() {
        if (innerExecutorService == null && !closed) {
            if (LOG.isInfoEnabled()) {
                LOG.info(String.format("Starting ExecutableQueue[%s] Listener Service...", prefix));
            }
            speedometer.start((speed, averageSpeed, maxSpeed, minSpeed) -> {
                if (LOG.isInfoEnabled()) {
                    LOG.info(String.format("ExecutableQueue Service [%s] Status: { semaphore: %d, queue: %d, worker: %d, speed: %d, average: %d, max:%d }", prefix, semaphore != null ? semaphore.availablePermits() : -1, queue.size(), workQueue.size(), speed, averageSpeed, maxSpeed));
                }
            });
            innerExecutorService = ThreadUtils.newSingleThreadExecutor(1, DefaultThreadFactory.create(StringUtils.capitalize(prefix) + "ListenerService"));
            innerExecutorService.submit(() -> {
                try {
                    while (!stopped) {
                        E element = queue.poll(queueTimeout, TimeUnit.SECONDS);
                        if (element != null) {
                            speedometer.touch();
                            if (listeners != null && !listeners.isEmpty()) {
                                AtomicInteger abandonedCount = new AtomicInteger(0);
                                for (String id : listeners.keySet()) {
                                    IListener<E> listener = listeners.get(id);
                                    if (listener != null) {
                                        boolean flag = false;
                                        List<IFilter<E>> filters = listener.getFilters();
                                        if (filters != null && !filters.isEmpty()) {
                                            for (IFilter<E> filter : filters) {
                                                flag = filter.filter(element);
                                                if (flag) {
                                                    break;
                                                }
                                            }
                                        }
                                        if (!flag) {
                                            listener.listen(element);
                                        } else if (!listener.abandoned(element)) {
                                            // 被抛弃次数累加
                                            abandonedCount.addAndGet(1);
                                        }
                                    } else {
                                        onListenerRemoved(id, listeners.remove(id));
                                    }
                                }
                                // 如果被抛弃次数与注册的监听器数量相当，表示元素未被处理过
                                if (abandonedCount.get() >= listeners.size()) {
                                    // 调用全局事件处理
                                    onElementAbandoned(element);
                                }
                            } else {
                                onElementAbandoned(element);
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error(String.format("Interruption exception occurred in ExecutableQueue[%s] listener service: ", prefix), RuntimeUtils.unwrapThrow(e));
                    }
                }
            });
            onListenStarted();
        }
    }

    /**
     * 停止队列监听服务线程
     */
    public void listenStop() {
        listenStop(0L);
    }

    /**
     * 停止队列监听服务线程
     *
     * @param millis 等待该线程终止的时间最长为millis毫秒, 为0意味着要一直等下去
     */
    public final synchronized void listenStop(long millis) {
        if (innerExecutorService != null && !executor.isShutdown()) {
            if (!stopped) {
                try {
                    if (LOG.isInfoEnabled()) {
                        LOG.info(String.format("Stopping ExecutableQueue[%s] Listener Service...", prefix));
                    }
                    speedometer.close();
                    //
                    stopped = true;
                    innerExecutorService.shutdown();
                    if (millis > 0) {
                        if (!innerExecutorService.awaitTermination(millis, TimeUnit.MILLISECONDS) && LOG.isWarnEnabled()) {
                            LOG.warn(String.format("Waiting for ExecutableQueue[%s] listener service to stop, but timed out before terminating.", prefix));
                        }
                    }
                } catch (InterruptedException e) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(String.format("Interrupt exception when waiting for ExecutableQueue[%s] listener service to stop: ", prefix), RuntimeUtils.unwrapThrow(e));
                    }
                }
                onListenStopped();
            }
        }
    }

    @Override
    public void close() {
        if (!closed) {
            //
            listenStop();
            //
            if (executor != null && !executor.isShutdown()) {
                if (LOG.isInfoEnabled()) {
                    LOG.info(String.format("Shutting down ExecutableQueue[%s] ExecutorService...", prefix));
                }
                executor.shutdown();
            }
            if (listeners != null) {
                listeners.clear();
            }
            if (queue != null) {
                queue.clear();
            }
            //
            closed = true;
        }
    }

    public boolean checkStatus() {
        boolean flag = !closed && (innerExecutorService == null || !stopped);
        if (flag && LOG.isInfoEnabled()) {
            // 输出当前队列数量
            LOG.info(String.format("ExecutableQueue[%s] Queue size: %d", prefix, queue.size()));
        }
        return flag;
    }

    public void addListener(IListener<E> listener) {
        addListener(listener.getClass().getName(), listener);
    }

    /**
     * @param id       监听器ID
     * @param listener 监听器对象
     */
    public void addListener(String id, IListener<E> listener) {
        if (checkStatus() && StringUtils.isNotBlank(id) && listener != null) {
            listeners.put(id, listener);
            onListenerAdded(id, listener);
        }
    }

    public IListener<E> removeListener(Class<? extends IListener<E>> listener) {
        return removeListener(listener.getName());
    }

    /**
     * @param id 监听器ID
     * @return 返回被移除的监听器对象
     */
    public IListener<E> removeListener(String id) {
        if (checkStatus() && !StringUtils.isNotBlank(id)) {
            IListener<E> listener = listeners.remove(id);
            onListenerRemoved(id, listener);
            return listener;
        }
        return null;
    }

    /**
     * 移除全部监听器
     *
     * @return 返回被移除的监听器对象映射
     */
    public Map<String, IListener<E>> removeAllListeners() {
        if (checkStatus()) {
            Map<String, IListener<E>> map = new HashMap<>(listeners);
            listeners.clear();
            //
            return map;
        }
        return Collections.emptyMap();
    }

    public void putElement(E element) {
        if (checkStatus() && element != null) {
            queue.add(element);
            onElementAdded(element);
        }
    }

    public void putElements(Collection<E> elements) {
        if (checkStatus() && elements != null && !elements.isEmpty()) {
            queue.addAll(elements);
            elements.forEach(this::onElementAdded);
        }
    }

    /**
     * @return 返回当前队列大小
     */
    public int getQueueSize() {
        return queue.size();
    }

    /**
     * @return 返回当前工作队列大小
     */
    public int getWorkQueueSize() {
        return workQueue.size();
    }

    public E execute(Callable<E> worker) throws InterruptedException, ExecutionException, TimeoutException {
        return execute(worker, 0L);
    }

    public E execute(Callable<E> worker, long timeout) throws InterruptedException, ExecutionException, TimeoutException {
        if (!checkStatus()) {
            return null;
        }
        FutureTask<E> future = bindFutureTaskWorker(worker);
        executor.submit(future);
        //
        E result;
        if (timeout > 0) {
            result = future.get(timeout, TimeUnit.SECONDS);
        } else {
            result = future.get();
        }
        if (!future.isDone() && !future.isCancelled()) {
            future.cancel(true);
        }
        return result;
    }

    public void execute(List<Callable<E>> workers) {
        if (checkStatus()) {
            if (workers != null && !workers.isEmpty()) {
                if (LOG.isInfoEnabled()) {
                    LOG.info(String.format("ExecutableQueue[%s] Executor Submit: %d", prefix, workers.size()));
                }
                workers.forEach((worker) -> executor.submit(new ExecutableWorker<>(queue, semaphore, worker)));
            }
        }
    }

    private FutureTask<E> bindFutureTaskWorker(Callable<E> worker) {
        return new FutureTask<>(() -> {
            try {
                if (semaphore != null) {
                    semaphore.acquire();
                }
                return worker.call();
            } finally {
                if (semaphore != null) {
                    semaphore.release();
                }
            }
        });
    }

    public static class ExecutableWorker<E> implements Runnable {

        private final BlockingQueue<E> queue;

        private final Semaphore semaphore;

        private final Callable<E> worker;

        public ExecutableWorker(BlockingQueue<E> queue, Semaphore semaphore, Callable<E> worker) {
            this.queue = queue;
            this.semaphore = semaphore;
            this.worker = worker;
        }

        public Callable<E> getWorker() {
            return worker;
        }

        @Override
        public void run() {
            try {
                if (semaphore != null) {
                    semaphore.acquire();
                }
                E result = worker.call();
                if (result != null) {
                    queue.add(result);
                }
            } catch (Exception e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("An error occurred when ExecutableWorker was executed:", RuntimeUtils.unwrapThrow(e));
                }
            } finally {
                if (semaphore != null) {
                    semaphore.release();
                }
            }
        }
    }

    public interface IFilter<E> {

        /**
         * 执行队列元素过滤
         *
         * @param element 原始元素对象
         * @return 返回过滤后元素对象
         */
        boolean filter(E element);
    }

    public interface IListener<E> {

        /**
         * 获取元素过滤器集合
         *
         * @return 返回元素过滤器集合
         */
        List<IFilter<E>> getFilters();

        /**
         * 执行监听器处理逻辑
         *
         * @param element 元素对象
         */
        void listen(E element);

        /**
         * 当元素被丢弃（即被过滤）时调用该方法
         *
         * @param element 元素对象
         * @return 返回 true 表示该方法内已对元素进行处置，否则该元素将交给队列全局事件处理
         */
        default boolean abandoned(E element) {
            return false;
        }
    }
}
