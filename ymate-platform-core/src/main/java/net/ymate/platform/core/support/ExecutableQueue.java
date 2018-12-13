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
package net.ymate.platform.core.support;

import net.ymate.platform.core.util.RuntimeUtils;
import net.ymate.platform.core.util.ThreadUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.*;

/**
 * 可执行队列服务
 *
 * @author 刘镇 (suninformation@163.com) on 2018/5/23 上午1:35
 * @version 1.0
 */
public class ExecutableQueue<E extends Serializable> implements IDestroyable {

    private static final Log _LOG = LogFactory.getLog(ExecutableQueue.class);

    private static final String __THREAD_NAME_PREFIX = "ExecutableQueue";

    private ExecutorService __executor;

    private BlockingQueue<E> __queue;

    private BlockingQueue<Runnable> __workQueue;

    private long __queueTimeout;

    private Map<String, IListener<E>> __listeners;

    private Semaphore __semaphore;

    private ExecutorService __innerExecutorService;

    private String __prefix;

    private boolean __stopped;

    private boolean __destroyed;

    private Speedometer __speedometer;

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
            prefix = __THREAD_NAME_PREFIX;
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
        __workQueue = new LinkedBlockingQueue<Runnable>(workQueueSize > 0 ? workQueueSize : Integer.MAX_VALUE);
        //
        __init(new ThreadPoolExecutor(minPoolSize, maxPoolSize, 0L, TimeUnit.MILLISECONDS, __workQueue, new DefaultThreadFactory(prefix), rejectedExecutionHandler), prefix, queueTimeout, queueSize, concurrentCount);
    }

    private void __init(ExecutorService executorService, String prefix, long queueTimeout, int queueSize, int concurrentCount) {
        __prefix = prefix;
        __executor = executorService;
        __queue = new LinkedBlockingQueue<E>(queueSize > 0 ? queueSize : Integer.MAX_VALUE);
        __queueTimeout = queueTimeout > 0 ? queueTimeout : 30L;
        if (concurrentCount > 0) {
            __semaphore = new Semaphore(concurrentCount);
        }
        //
        __speedometer = new Speedometer(__prefix);
        __listeners = new ConcurrentHashMap<String, IListener<E>>();
    }

    /**
     * 当监听线程开启时被调用
     */
    protected void __onListenStarted() {
        if (_LOG.isInfoEnabled()) {
            _LOG.info("ExecutableQueue Service [" + __prefix + "] Listener Service Started.");
        }
    }

    /**
     * 当监听线程停止时被调用
     */
    protected void __onListenStopped() {
        if (_LOG.isInfoEnabled()) {
            _LOG.info("ExecutableQueue Service [" + __prefix + "] Listener Service Stopped.");
        }
    }

    /**
     * 当添加新监听器时被调用
     *
     * @param id       监听器ID
     * @param listener 监听器对象
     */
    protected void __onListenerAdded(String id, IListener<E> listener) {
        if (_LOG.isInfoEnabled()) {
            _LOG.info("ExecutableQueue Service [" + __prefix + "] Add Listener [" + id + "@" + listener.getClass().getName() + "].");
        }
    }

    /**
     * 当移除监听器时被调用
     *
     * @param id       监听器ID
     * @param listener 监听器对象（可能为空）
     */
    protected void __onListenerRemoved(String id, IListener<E> listener) {
        if (_LOG.isInfoEnabled()) {
            _LOG.info("ExecutableQueue Service [" + __prefix + "] Remove Listener [" + id + "@" + (listener == null ? "unknown" : listener.getClass().getName()) + "].");
        }
    }

    /**
     * 当向队列添加新元素时被调用
     *
     * @param element 新元素对象
     */
    protected void __onElementAdded(E element) {
        if (_LOG.isInfoEnabled()) {
            _LOG.info("ExecutableQueue Service [" + __prefix + "] Add Element [" + element.toString() + "].");
        }
    }

    /**
     * 当丢弃队列中的元素时被调用
     *
     * @param element 元素对象
     */
    protected void __onElementAbandoned(E element) {
        if (_LOG.isInfoEnabled()) {
            _LOG.info("ExecutableQueue Service [" + __prefix + "] Abandon Element [" + element.toString() + "].");
        }
    }

    /**
     * 启动队列监听服务线程
     */
    public synchronized void listenStart() {
        if (__innerExecutorService == null && !__destroyed) {
            if (_LOG.isInfoEnabled()) {
                _LOG.info("Starting ExecutableQueue[" + __prefix + "] Listener Service...");
            }
            __speedometer.start(new ISpeedListener() {
                @Override
                public void listen(long speed, long averageSpeed, long maxSpeed, long minSpeed) {
                    if (_LOG.isInfoEnabled()) {
                        _LOG.info("ExecutableQueue Service [" + __prefix + "] Status: { semaphore: " + (__semaphore != null ? __semaphore.availablePermits() : -1) + ", queue: " + __queue.size() + ", worker: " + __workQueue.size() + ", speed: " + speed + ", average: " + averageSpeed + ", max:" + maxSpeed + " }");
                    }
                }
            });
            __innerExecutorService = ThreadUtils.newSingleThreadExecutor(1, ThreadUtils.createFactory(__prefix + "-ListenerService-"));
            __innerExecutorService.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (!__stopped) {
                            E _element = __queue.poll(__queueTimeout, TimeUnit.SECONDS);
                            if (_element != null) {
                                __speedometer.touch();
                                if (__listeners != null && !__listeners.isEmpty()) {
                                    for (String _id : __listeners.keySet()) {
                                        IListener<E> _listener = __listeners.get(_id);
                                        if (_listener != null) {
                                            boolean _flag = false;
                                            List<IFilter<E>> _filters = _listener.getFilters();
                                            if (_filters != null && !_filters.isEmpty()) {
                                                for (IFilter<E> _filter : _filters) {
                                                    _flag = _filter.filter(_element);
                                                    if (_flag) {
                                                        break;
                                                    }
                                                }
                                            }
                                            if (!_flag) {
                                                _listener.listen(_element);
                                            } else {
                                                __onElementAbandoned(_element);
                                            }
                                        } else {
                                            __onListenerRemoved(_id, __listeners.remove(_id));
                                        }
                                    }
                                } else {
                                    __onElementAbandoned(_element);
                                }
                            }
                        }
                    } catch (InterruptedException e) {
                        if (_LOG.isErrorEnabled()) {
                            _LOG.error("Interruption exception occurred in ExecutableQueue[" + __prefix + "] listener service: ", RuntimeUtils.unwrapThrow(e));
                        }
                    }
                }
            });
            __onListenStarted();
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
        if (__innerExecutorService != null && !__executor.isShutdown()) {
            if (!__stopped) {
                try {
                    if (_LOG.isInfoEnabled()) {
                        _LOG.info("Stopping ExecutableQueue[" + __prefix + "] Listener Service...");
                    }
                    __speedometer.close();
                    //
                    __stopped = true;
                    __innerExecutorService.shutdown();
                    if (millis > 0) {
                        __innerExecutorService.awaitTermination(millis, TimeUnit.MILLISECONDS);
                    }
                } catch (InterruptedException e) {
                    if (_LOG.isWarnEnabled()) {
                        _LOG.warn("Interrupt exception when waiting for ExecutableQueue[" + __prefix + "] listener service to stop: ", RuntimeUtils.unwrapThrow(e));
                    }
                }
            }
            __onListenStopped();
        }
    }

    @Override
    public void destroy() {
        if (!__destroyed) {
            //
            listenStop();
            //
            if (__executor != null && !__executor.isShutdown()) {
                if (_LOG.isInfoEnabled()) {
                    _LOG.info("Shutting down ExecutableQueue[" + __prefix + "] ExecutorService...");
                }
                __executor.shutdown();
            }
            if (__listeners != null) {
                __listeners.clear();
            }
            if (__queue != null) {
                __queue.clear();
            }
            //
            __destroyed = true;
        }
    }

    public boolean checkStatus() {
        boolean _flag = !__destroyed && (__innerExecutorService == null || !__stopped);
        if (_flag && _LOG.isInfoEnabled()) {
            // 输出当前队列数量
            _LOG.info("ExecutableQueue[" + __prefix + "] Queue size: " + __queue.size());
        }
        return _flag;
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
            __listeners.put(id, listener);
            __onListenerAdded(id, listener);
        }
    }

    public IListener<E> removeListener(Class<? extends IListener> listener) {
        return removeListener(listener.getName());
    }

    /**
     * @param id 监听器ID
     * @return 返回被移除的监听器对象
     */
    public IListener<E> removeListener(String id) {
        if (checkStatus() && !StringUtils.isNotBlank(id)) {
            IListener<E> _listener = __listeners.remove(id);
            __onListenerRemoved(id, _listener);
            return _listener;
        }
        return null;
    }

    /**
     * 移除全部监听器
     */
    public Map<String, IListener<E>> removeAllListeners() {
        if (checkStatus()) {
            Map<String, IListener<E>> _map = new HashMap<String, IListener<E>>(__listeners);
            __listeners.clear();
            //
            return _map;
        }
        return Collections.emptyMap();
    }

    public void putElement(E element) {
        if (checkStatus() && element != null) {
            __queue.add(element);
            __onElementAdded(element);
        }
    }

    public void putElements(Collection<E> elements) {
        if (checkStatus() && elements != null && !elements.isEmpty()) {
            __queue.addAll(elements);
            for (E _element : elements) {
                __onElementAdded(_element);
            }
        }
    }

    /**
     * @return 返回当前队列大小
     */
    public int getQueueSize() {
        return __queue.size();
    }

    /**
     * @return 返回当前工作队列大小
     */
    public int getWorkQueueSize() {
        return __workQueue.size();
    }

    public E execute(Callable<E> worker) throws InterruptedException, ExecutionException, TimeoutException {
        return execute(worker, 0L);
    }

    public E execute(Callable<E> worker, long timeout) throws InterruptedException, ExecutionException, TimeoutException {
        if (!checkStatus()) {
            return null;
        }
        FutureTask<E> _future = __bindFutureTaskWorker(worker);
        __executor.submit(_future);
        //
        E _result;
        if (timeout > 0) {
            _result = _future.get(timeout, TimeUnit.SECONDS);
        } else {
            _result = _future.get();
        }
        if (!_future.isDone() && !_future.isCancelled()) {
            _future.cancel(true);
        }
        return _result;
    }

    public void execute(List<Callable<E>> workers) {
        if (checkStatus()) {
            if (workers != null && !workers.isEmpty()) {
                _LOG.info("ExecutableQueue[" + __prefix + "] Executor Submit: " + workers.size());
                for (final Callable<E> _worker : workers) {
                    __executor.submit(new ExecutableWorker<E>(__queue, __semaphore, _worker));
                }
            }
        }
    }

    private FutureTask<E> __bindFutureTaskWorker(final Callable<E> worker) {
        return new FutureTask<E>(new Callable<E>() {
            @Override
            public E call() throws Exception {
                try {
                    if (__semaphore != null) {
                        __semaphore.acquire();
                    }
                    return worker.call();
                } finally {
                    if (__semaphore != null) {
                        __semaphore.release();
                    }
                }
            }
        });
    }

    public static class ExecutableWorker<E> implements Runnable {

        private BlockingQueue<E> __queue;

        private Semaphore __semaphore;

        private Callable<E> __worker;

        public ExecutableWorker(BlockingQueue<E> queue, Semaphore semaphore, Callable<E> worker) {
            __queue = queue;
            __semaphore = semaphore;
            __worker = worker;
        }

        public Callable<E> getWorker() {
            return __worker;
        }

        @Override
        public void run() {
            try {
                if (__semaphore != null) {
                    __semaphore.acquire();
                }
                E _result = __worker.call();
                if (_result != null) {
                    __queue.add(_result);
                }
            } catch (Exception e) {
                _LOG.warn("An error occurred when ExecutableWorker was executed:", RuntimeUtils.unwrapThrow(e));
            } finally {
                if (__semaphore != null) {
                    __semaphore.release();
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
         * @return 返回元素过滤器集合
         */
        List<IFilter<E>> getFilters();

        /**
         * 执行监听器处理逻辑
         *
         * @param element 元素对象
         */
        void listen(E element);
    }
}
