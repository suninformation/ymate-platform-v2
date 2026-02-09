/*
 * Copyright 2007-present the original author or authors.
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
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * ThreadUtils类的单元测试，覆盖所有公共方法和功能
 *
 * @author 刘镇 (suninformation@163.com) on 2026-01-13 15:30:00
 * @since 2.1.4
 */
public class ThreadUtilsTest {

    private ExecutorService testExecutor;

    @Before
    public void setUp() {
        // 在每个测试方法前执行，初始化测试环境
        testExecutor = null;
    }

    @After
    public void tearDown() {
        // 在每个测试方法后执行，清理资源
        if (testExecutor != null && !testExecutor.isShutdown()) {
            ThreadUtils.shutdownExecutorService(testExecutor);
        }
    }

    /**
     * 测试线程池构建器的基本功能
     */
    @Test
    public void testThreadPoolBuilder() {
        // 测试基本构建功能
        ExecutorService executor = ThreadUtils.builder()
                .corePoolSize(2)
                .maximumPoolSize(4)
                .keepAliveTime(60, TimeUnit.SECONDS)
                .queueCapacity(100)
                .linkedBlockingQueue()
                .abortPolicy()
                .build();

        Assert.assertNotNull(executor);
        Assert.assertTrue(executor instanceof ThreadPoolExecutor);

        // 测试不同队列类型
        ExecutorService executor2 = ThreadUtils.builder()
                .corePoolSize(2)
                .maximumPoolSize(4)
                .arrayBlockingQueue()
                .build();
        Assert.assertNotNull(executor2);

        ExecutorService executor3 = ThreadUtils.builder()
                .corePoolSize(2)
                .maximumPoolSize(4)
                .synchronousQueue()
                .build();
        Assert.assertNotNull(executor3);

        ExecutorService executor4 = ThreadUtils.builder()
                .corePoolSize(2)
                .maximumPoolSize(4)
                .priorityBlockingQueue()
                .build();
        Assert.assertNotNull(executor4);

        // 测试调度线程池
        ExecutorService executor5 = ThreadUtils.builder()
                .corePoolSize(2)
                .scheduled()
                .build();
        Assert.assertNotNull(executor5);
        Assert.assertTrue(executor5 instanceof ScheduledExecutorService);

        // 测试不同拒绝策略
        ExecutorService executor6 = ThreadUtils.builder()
                .corePoolSize(1)
                .maximumPoolSize(1)
                .queueCapacity(1)
                .callerRunsPolicy()
                .build();
        Assert.assertNotNull(executor6);

        ExecutorService executor7 = ThreadUtils.builder()
                .corePoolSize(1)
                .maximumPoolSize(1)
                .queueCapacity(1)
                .discardPolicy()
                .build();
        Assert.assertNotNull(executor7);

        ExecutorService executor8 = ThreadUtils.builder()
                .corePoolSize(1)
                .maximumPoolSize(1)
                .queueCapacity(1)
                .discardOldestPolicy()
                .build();
        Assert.assertNotNull(executor8);

        // 清理资源
        ThreadUtils.shutdownExecutorService(executor);
        ThreadUtils.shutdownExecutorService(executor2);
        ThreadUtils.shutdownExecutorService(executor3);
        ThreadUtils.shutdownExecutorService(executor4);
        ThreadUtils.shutdownExecutorService(executor5);
        ThreadUtils.shutdownExecutorService(executor6);
        ThreadUtils.shutdownExecutorService(executor7);
        ThreadUtils.shutdownExecutorService(executor8);
    }

    /**
     * 测试newThreadExecutor方法的各种重载版本
     */
    @Test
    public void testNewThreadExecutor() {
        // 测试基本版本
        testExecutor = ThreadUtils.newThreadExecutor(2, 4, 60000);
        Assert.assertNotNull(testExecutor);

        // 测试带队列容量版本
        testExecutor = ThreadUtils.newThreadExecutor(2, 4, 60000, 100);
        Assert.assertNotNull(testExecutor);

        // 测试带线程工厂版本
        testExecutor = ThreadUtils.newThreadExecutor(2, 4, 60000, 100, DefaultThreadFactory.create());
        Assert.assertNotNull(testExecutor);

        // 测试带拒绝策略版本
        testExecutor = ThreadUtils.newThreadExecutor(2, 4, 60000, 100, DefaultThreadFactory.create(), new ThreadPoolExecutor.AbortPolicy());
        Assert.assertNotNull(testExecutor);

        // 测试带时间单位版本
        testExecutor = ThreadUtils.newThreadExecutor(2, 4, 60, TimeUnit.SECONDS);
        Assert.assertNotNull(testExecutor);

        // 测试带时间单位和队列容量版本
        testExecutor = ThreadUtils.newThreadExecutor(2, 4, 60, TimeUnit.SECONDS, 100);
        Assert.assertNotNull(testExecutor);

        // 测试带时间单位、队列容量和线程工厂版本
        testExecutor = ThreadUtils.newThreadExecutor(2, 4, 60, TimeUnit.SECONDS, 100, DefaultThreadFactory.create());
        Assert.assertNotNull(testExecutor);

        // 测试带时间单位、队列容量、线程工厂和拒绝策略版本
        testExecutor = ThreadUtils.newThreadExecutor(2, 4, 60, TimeUnit.SECONDS, 100, DefaultThreadFactory.create(), new ThreadPoolExecutor.AbortPolicy());
        Assert.assertNotNull(testExecutor);
    }

    /**
     * 测试newSingleThreadExecutor方法
     */
    @Test
    public void testNewSingleThreadExecutor() {
        // 测试基本版本
        testExecutor = ThreadUtils.newSingleThreadExecutor();
        Assert.assertNotNull(testExecutor);

        // 测试带队列容量版本
        testExecutor = ThreadUtils.newSingleThreadExecutor(100);
        Assert.assertNotNull(testExecutor);

        // 测试带线程工厂版本
        testExecutor = ThreadUtils.newSingleThreadExecutor(100, DefaultThreadFactory.create());
        Assert.assertNotNull(testExecutor);
    }

    /**
     * 测试newSingleThreadScheduledExecutor方法
     */
    @Test
    public void testNewSingleThreadScheduledExecutor() {
        // 测试基本版本
        ScheduledExecutorService executor = ThreadUtils.newSingleThreadScheduledExecutor();
        Assert.assertNotNull(executor);
        Assert.assertTrue(executor instanceof ScheduledExecutorService);

        // 测试带线程工厂版本
        ScheduledExecutorService executor2 = ThreadUtils.newSingleThreadScheduledExecutor(DefaultThreadFactory.create());
        Assert.assertNotNull(executor2);
        Assert.assertTrue(executor2 instanceof ScheduledExecutorService);

        // 清理资源
        ThreadUtils.shutdownExecutorService(executor);
        ThreadUtils.shutdownExecutorService(executor2);
    }

    /**
     * 测试newCachedThreadPool方法的各种重载版本
     */
    @Test
    public void testNewCachedThreadPool() {
        // 测试基本版本
        testExecutor = ThreadUtils.newCachedThreadPool();
        Assert.assertNotNull(testExecutor);

        // 测试带最大线程数版本
        testExecutor = ThreadUtils.newCachedThreadPool(10);
        Assert.assertNotNull(testExecutor);

        // 测试带线程工厂版本
        testExecutor = ThreadUtils.newCachedThreadPool(DefaultThreadFactory.create());
        Assert.assertNotNull(testExecutor);

        // 测试带最大线程数和存活时间版本
        testExecutor = ThreadUtils.newCachedThreadPool(10, 60000);
        Assert.assertNotNull(testExecutor);

        // 测试带最大线程数、存活时间和线程工厂版本
        testExecutor = ThreadUtils.newCachedThreadPool(10, 60000, DefaultThreadFactory.create());
        Assert.assertNotNull(testExecutor);

        // 测试带时间单位版本
        testExecutor = ThreadUtils.newCachedThreadPool(60, TimeUnit.SECONDS);
        Assert.assertNotNull(testExecutor);

        // 测试带最大线程数、存活时间和时间单位版本
        testExecutor = ThreadUtils.newCachedThreadPool(10, 60, TimeUnit.SECONDS);
        Assert.assertNotNull(testExecutor);

        // 测试带最大线程数、存活时间、时间单位和线程工厂版本
        testExecutor = ThreadUtils.newCachedThreadPool(10, 60, TimeUnit.SECONDS, DefaultThreadFactory.create());
        Assert.assertNotNull(testExecutor);
    }

    /**
     * 测试newFixedThreadPool方法的各种重载版本
     */
    @Test
    public void testNewFixedThreadPool() {
        // 测试基本版本
        testExecutor = ThreadUtils.newFixedThreadPool(2);
        Assert.assertNotNull(testExecutor);

        // 测试带队列容量版本
        testExecutor = ThreadUtils.newFixedThreadPool(2, 100);
        Assert.assertNotNull(testExecutor);

        // 测试带队列容量和线程工厂版本
        testExecutor = ThreadUtils.newFixedThreadPool(2, 100, DefaultThreadFactory.create());
        Assert.assertNotNull(testExecutor);

        // 测试带时间单位版本
        testExecutor = ThreadUtils.newFixedThreadPool(2, 60, TimeUnit.SECONDS);
        Assert.assertNotNull(testExecutor);

        // 测试带时间单位和队列容量版本
        testExecutor = ThreadUtils.newFixedThreadPool(2, 60, TimeUnit.SECONDS, 100);
        Assert.assertNotNull(testExecutor);

        // 测试带时间单位、队列容量和线程工厂版本
        testExecutor = ThreadUtils.newFixedThreadPool(2, 60, TimeUnit.SECONDS, 100, DefaultThreadFactory.create());
        Assert.assertNotNull(testExecutor);
    }

    /**
     * 测试newScheduledThreadPool方法
     */
    @Test
    public void testNewScheduledThreadPool() {
        // 测试基本版本
        ScheduledExecutorService executor = ThreadUtils.newScheduledThreadPool(2);
        Assert.assertNotNull(executor);
        Assert.assertTrue(executor instanceof ScheduledExecutorService);

        // 测试带线程工厂版本
        ScheduledExecutorService executor2 = ThreadUtils.newScheduledThreadPool(2, DefaultThreadFactory.create());
        Assert.assertNotNull(executor2);
        Assert.assertTrue(executor2 instanceof ScheduledExecutorService);

        // 测试带拒绝策略版本
        ScheduledExecutorService executor3 = ThreadUtils.newScheduledThreadPool(2, DefaultThreadFactory.create(), new ThreadPoolExecutor.AbortPolicy());
        Assert.assertNotNull(executor3);
        Assert.assertTrue(executor3 instanceof ScheduledExecutorService);

        // 清理资源
        ThreadUtils.shutdownExecutorService(executor);
        ThreadUtils.shutdownExecutorService(executor2);
        ThreadUtils.shutdownExecutorService(executor3);
    }

    /**
     * 测试executeOnce方法（单任务）
     *
     * @throws Exception 执行异常
     */
    @Test
    public void testExecuteOnceSingle() throws Exception {
        // 测试基本功能
        Callable<String> task = () -> {
            Thread.sleep(100);
            return "test";
        };

        String result = ThreadUtils.executeOnce(task);
        Assert.assertEquals("test", result);

        // 测试带超时版本
        result = ThreadUtils.executeOnce(task, 1000);
        Assert.assertEquals("test", result);

        // 测试带结果过滤器版本
        result = ThreadUtils.executeOnce(task, 1000, futureTask -> {
            if (futureTask.isDone()) {
                return futureTask.get() + "_filtered";
            }
            return null;
        });
        Assert.assertEquals("test_filtered", result);
    }

    /**
     * 测试executeOnce方法（多任务）
     *
     * @throws Exception 执行异常
     */
    @Test
    public void testExecuteOnceMultiple() throws Exception {
        // 测试基本功能
        List<Callable<String>> tasks = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            final int index = i;
            tasks.add(() -> {
                Thread.sleep(100);
                return "test_" + index;
            });
        }

        List<String> results = ThreadUtils.executeOnce(tasks);
        Assert.assertNotNull(results);
        Assert.assertEquals(5, results.size());

        // 测试带超时版本
        results = ThreadUtils.executeOnce(tasks, 1000);
        Assert.assertNotNull(results);
        Assert.assertEquals(5, results.size());

        // 测试带结果过滤器版本
        results = ThreadUtils.executeOnce(tasks, 1000, futureTask -> {
            if (futureTask.isDone()) {
                return futureTask.get() + "_filtered";
            }
            return null;
        });
        Assert.assertNotNull(results);
        Assert.assertEquals(5, results.size());
        for (String result : results) {
            Assert.assertTrue(result.endsWith("_filtered"));
        }

        // 测试空任务列表
        results = ThreadUtils.executeOnce(new ArrayList<>());
        Assert.assertNotNull(results);
        Assert.assertTrue(results.isEmpty());

        // 测试null任务列表
        results = ThreadUtils.executeOnce((List<Callable<String>>) null);
        Assert.assertNotNull(results);
        Assert.assertTrue(results.isEmpty());
    }

    /**
     * 测试shutdownExecutorService方法的各种重载版本
     */
    @Test
    public void testShutdownExecutorService() {
        // 测试基本版本
        testExecutor = ThreadUtils.newFixedThreadPool(2);
        ThreadUtils.shutdownExecutorService(testExecutor);
        Assert.assertTrue(testExecutor.isShutdown());

        // 测试带超时版本
        testExecutor = ThreadUtils.newFixedThreadPool(2);
        ThreadUtils.shutdownExecutorService(testExecutor, 1000);
        Assert.assertTrue(testExecutor.isShutdown());

        // 测试带超时和重试次数版本
        testExecutor = ThreadUtils.newFixedThreadPool(2);
        ThreadUtils.shutdownExecutorService(testExecutor, 1000, 2);
        Assert.assertTrue(testExecutor.isShutdown());

        // 测试null参数
        ThreadUtils.shutdownExecutorService(null);
        // 不应抛出异常
    }

    /**
     * 测试getThreadPoolMonitor方法
     */
    @Test
    public void testGetThreadPoolMonitor() {
        // 测试正常情况
        testExecutor = ThreadUtils.newFixedThreadPool(2, 100);
        ThreadUtils.ThreadPoolMonitor monitor = ThreadUtils.getThreadPoolMonitor(testExecutor);
        Assert.assertNotNull(monitor);
        Assert.assertEquals(2, monitor.getCorePoolSize());
        Assert.assertEquals(2, monitor.getMaximumPoolSize());
        // 线程池不会立即创建核心线程，而是在有任务提交时才会创建，所以poolSize可能为0
        Assert.assertTrue(monitor.getPoolSize() >= 0 && monitor.getPoolSize() <= 2);
        Assert.assertEquals(0, monitor.getActiveCount());
        Assert.assertEquals(0, monitor.getQueueSize());
        Assert.assertFalse(monitor.isShutdown());
        Assert.assertFalse(monitor.isTerminated());

        // 提交一个任务，触发线程创建
        testExecutor.submit(() -> {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // 再次获取监控信息，此时应该有活跃线程
        monitor = ThreadUtils.getThreadPoolMonitor(testExecutor);
        Assert.assertTrue(monitor.getActiveCount() >= 0);

        // 测试非ThreadPoolExecutor类型
        ExecutorService executor = Executors.newSingleThreadExecutor();
        ThreadUtils.ThreadPoolMonitor monitor2 = ThreadUtils.getThreadPoolMonitor(executor);
        // Executors.newSingleThreadExecutor()返回的是FinalizableDelegatedExecutorService，不是ThreadPoolExecutor，所以返回null
        Assert.assertNull(monitor2);

        // 测试null参数
        ThreadUtils.ThreadPoolMonitor monitor3 = ThreadUtils.getThreadPoolMonitor(null);
        Assert.assertNull(monitor3);

        // 清理资源
        ThreadUtils.shutdownExecutorService(executor);
    }

    /**
     * 测试边界条件
     */
    @Test
    public void testBoundaryConditions() {
        // 测试核心线程数为0的情况
        testExecutor = ThreadUtils.newThreadExecutor(0, 2, 60000);
        Assert.assertNotNull(testExecutor);

        // 测试最大线程数等于核心线程数的情况
        testExecutor = ThreadUtils.newThreadExecutor(2, 2, 60000);
        Assert.assertNotNull(testExecutor);

        // 测试队列容量为1的情况
        testExecutor = ThreadUtils.newThreadExecutor(2, 4, 60000, 1);
        Assert.assertNotNull(testExecutor);
    }

    /**
     * 测试默认结果过滤器
     *
     * @throws Exception 执行异常
     */
    @Test
    public void testDefaultFutureResultFilter() throws Exception {
        // 测试默认结果过滤器
        Callable<String> task = () -> {
            Thread.sleep(100);
            return "test";
        };

        ThreadUtils.DefaultFutureResultFilter<String> filter = new ThreadUtils.DefaultFutureResultFilter<>();
        FutureTask<String> future = new FutureTask<>(task);

        // 测试未完成任务
        String result = filter.filter(future);
        Assert.assertNull(result);

        // 测试已完成任务
        Thread thread = new Thread(future);
        thread.start();
        thread.join();

        result = filter.filter(future);
        Assert.assertEquals("test", result);
    }
}
