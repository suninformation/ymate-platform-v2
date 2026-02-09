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
package net.ymate.platform.commons;

import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ExecutableQueue测试类
 *
 * @author 刘镇 (suninformation@163.com) on 2022/8/29 12:41
 * @since 2.1.2
 */
public class ExecutableQueueTest {

    // 测试用的元素类
    public static class TestElement implements Serializable {
        private String id;
        private String name;

        public TestElement(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "TestElement{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

    // 测试用的监听器
    public static class TestListener implements ExecutableQueue.IListener<TestElement> {
        private final List<TestElement> receivedElements = new ArrayList<>();
        private final AtomicInteger abandonedCount = new AtomicInteger(0);

        @Override
        public void listen(TestElement element) {
            receivedElements.add(element);
        }

        @Override
        public boolean abandoned(TestElement element) {
            abandonedCount.incrementAndGet();
            return false;
        }

        public List<TestElement> getReceivedElements() {
            return receivedElements;
        }

        public int getAbandonedCount() {
            return abandonedCount.get();
        }
    }

    // 测试用的过滤器
    public static class TestFilter implements ExecutableQueue.IFilter<TestElement> {
        private final String filterId;

        public TestFilter(String filterId) {
            this.filterId = filterId;
        }

        @Override
        public boolean filter(TestElement element) {
            return element.getId().equals(filterId);
        }
    }

    @Test
    public void testConstructors() {
        // 测试默认构造函数
        ExecutableQueue<TestElement> queue1 = new ExecutableQueue<>();
        Assert.assertNotNull(queue1);
        Assert.assertEquals("ExecutableQueue", queue1.getPrefix());

        // 测试带前缀的构造函数
        ExecutableQueue<TestElement> queue2 = new ExecutableQueue<>("test");
        Assert.assertNotNull(queue2);
        Assert.assertEquals("test", queue2.getPrefix());

        // 测试带拒绝策略的构造函数
        RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
        ExecutableQueue<TestElement> queue3 = new ExecutableQueue<>(handler);
        Assert.assertNotNull(queue3);

        // 测试带并发数和拒绝策略的构造函数
        ExecutableQueue<TestElement> queue4 = new ExecutableQueue<>(5, handler);
        Assert.assertNotNull(queue4);

        // 测试带前缀、并发数和拒绝策略的构造函数
        ExecutableQueue<TestElement> queue5 = new ExecutableQueue<>("custom", 5, handler);
        Assert.assertNotNull(queue5);
        Assert.assertEquals("custom", queue5.getPrefix());
    }

    @Test
    public void testBasicOperations() {
        ExecutableQueue<TestElement> queue = new ExecutableQueue<>("test");

        // 测试状态检查
        Assert.assertTrue(queue.checkStatus());

        // 测试队列大小
        Assert.assertEquals(0, queue.getQueueSize());
        Assert.assertEquals(0, queue.getWorkQueueSize());

        // 测试添加元素
        TestElement element1 = new TestElement("1", "Element1");
        queue.putElement(element1);
        Assert.assertEquals(1, queue.getQueueSize());

        // 测试批量添加元素
        List<TestElement> elements = new ArrayList<>();
        elements.add(new TestElement("2", "Element2"));
        elements.add(new TestElement("3", "Element3"));
        queue.putElements(elements);
        Assert.assertEquals(3, queue.getQueueSize());
    }

    @Test
    public void testListenerOperations() {
        ExecutableQueue<TestElement> queue = new ExecutableQueue<>("test");

        // 测试添加监听器
        TestListener listener1 = new TestListener();
        queue.addListener("listener1", listener1);

        // 测试通过类添加监听器
        TestListener listener2 = new TestListener();
        queue.addListener(listener2);

        // 测试移除监听器
        ExecutableQueue.IListener<TestElement> removedListener = queue.removeListener("listener1");
        Assert.assertNotNull(removedListener);

        // 测试移除全部监听器
        java.util.Map<String, ExecutableQueue.IListener<TestElement>> removedListeners = queue.removeAllListeners();
        Assert.assertFalse(removedListeners.isEmpty());
    }

    @Test
    public void testListenStartAndStop() throws InterruptedException {
        ExecutableQueue<TestElement> queue = new ExecutableQueue<>("test");

        // 创建并添加监听器
        TestListener listener = new TestListener();
        queue.addListener(listener);

        // 启动监听服务
        queue.listenStart();
        Thread.sleep(1000); // 等待监听线程启动

        // 添加元素
        TestElement element1 = new TestElement("1", "Element1");
        queue.putElement(element1);
        Thread.sleep(1000); // 等待元素被处理

        // 验证元素被处理
        Assert.assertEquals(1, listener.getReceivedElements().size());
        Assert.assertEquals("1", listener.getReceivedElements().get(0).getId());

        // 停止监听服务
        queue.listenStop(2000);
        Thread.sleep(500); // 等待监听线程停止

        // 添加元素（应该不会被处理）
        TestElement element2 = new TestElement("2", "Element2");
        queue.putElement(element2);
        Thread.sleep(1000); // 等待

        // 验证元素未被处理
        Assert.assertEquals(1, listener.getReceivedElements().size());

        // 关闭队列
        queue.close();
    }

    @Test
    public void testExecuteMethod() throws Exception {
        ExecutableQueue<TestElement> queue = new ExecutableQueue<>("test");

        // 测试执行单个任务
        Callable<TestElement> task = () -> {
            Thread.sleep(500);
            return new TestElement("task1", "TaskResult1");
        };

        TestElement result = queue.execute(task);
        Assert.assertNotNull(result);
        Assert.assertEquals("task1", result.getId());
        Assert.assertEquals("TaskResult1", result.getName());

        // 测试带超时的执行
        Callable<TestElement> longTask = () -> {
            Thread.sleep(100);
            return new TestElement("task2", "TaskResult2");
        };

        TestElement result2 = queue.execute(longTask, 1);
        Assert.assertNotNull(result2);
        Assert.assertEquals("task2", result2.getId());

        // 关闭队列
        queue.close();
    }

    @Test
    public void testExecuteBatchMethod() throws InterruptedException {
        ExecutableQueue<TestElement> queue = new ExecutableQueue<>("test");

        // 启动监听服务
        TestListener listener = new TestListener();
        queue.addListener(listener);
        queue.listenStart();
        Thread.sleep(1000); // 等待监听线程启动

        // 准备批量任务
        List<Callable<TestElement>> tasks = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            final int index = i;
            tasks.add(() -> {
                Thread.sleep(100);
                return new TestElement("batch" + index, "BatchResult" + index);
            });
        }

        // 执行批量任务
        queue.execute(tasks);
        Thread.sleep(2000); // 等待所有任务完成

        // 验证所有任务结果都被处理
        Assert.assertEquals(3, listener.getReceivedElements().size());

        // 关闭队列
        queue.close();
    }

    @Test
    public void testFilterFunctionality() throws InterruptedException {
        ExecutableQueue<TestElement> queue = new ExecutableQueue<>("test");

        // 创建带过滤器的监听器
        TestListener listener = new TestListener() {
            @Override
            public List<ExecutableQueue.IFilter<TestElement>> getFilters() {
                List<ExecutableQueue.IFilter<TestElement>> filters = new ArrayList<>();
                filters.add(new TestFilter("filtered"));
                return filters;
            }
        };
        queue.addListener(listener);

        // 启动监听服务
        queue.listenStart();
        Thread.sleep(1000); // 等待监听线程启动

        // 添加普通元素（应该被处理）
        TestElement normalElement = new TestElement("normal", "NormalElement");
        queue.putElement(normalElement);

        // 添加被过滤的元素（应该被抛弃）
        TestElement filteredElement = new TestElement("filtered", "FilteredElement");
        queue.putElement(filteredElement);

        Thread.sleep(2000); // 等待处理完成

        // 验证结果
        Assert.assertEquals(1, listener.getReceivedElements().size());
        Assert.assertEquals("normal", listener.getReceivedElements().get(0).getId());
        Assert.assertEquals(1, listener.getAbandonedCount());

        // 关闭队列
        queue.close();
    }

    @Test
    public void testCloseMethod() {
        ExecutableQueue<TestElement> queue = new ExecutableQueue<>("test");

        // 启动监听服务
        queue.listenStart();

        // 添加元素
        TestElement element = new TestElement("1", "Element1");
        queue.putElement(element);

        // 等待一小段时间让元素被处理
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 关闭队列
        queue.close();

        // 验证状态
        Assert.assertFalse(queue.checkStatus());

        // 尝试添加元素（应该失败）
        queue.putElement(new TestElement("2", "Element2"));
        // 由于队列已关闭，putElement不会添加元素，所以队列大小为0
        Assert.assertEquals(0, queue.getQueueSize());
    }

    @Test
    public void testConcurrentExecution() throws InterruptedException {
        ExecutableQueue<TestElement> queue = new ExecutableQueue<>("concurrent", 5, new ThreadPoolExecutor.CallerRunsPolicy());

        // 启动监听服务
        TestListener listener = new TestListener();
        queue.addListener(listener);
        queue.listenStart();
        Thread.sleep(1000); // 等待监听线程启动

        // 准备任务列表
        int taskCount = 10;
        List<Callable<TestElement>> tasks = new ArrayList<>();
        for (int i = 1; i <= taskCount; i++) {
            final int index = i;
            tasks.add(() -> {
                Thread.sleep(100);
                return new TestElement("concurrent" + index, "ConcurrentResult" + index);
            });
        }

        // 执行任务列表
        queue.execute(tasks);

        // 等待任务完成和处理
        Thread.sleep(3000); // 等待所有任务完成和监听线程处理结果

        // 验证所有任务结果都被处理
        Assert.assertEquals(taskCount, listener.getReceivedElements().size());

        // 关闭队列
        queue.close();
    }

    @Test
    public void testExecutableWorker() throws Exception {
        BlockingQueue<TestElement> testQueue = new LinkedBlockingQueue<>();
        Semaphore semaphore = new Semaphore(5);

        // 创建工作任务
        Callable<TestElement> worker = () -> {
            Thread.sleep(100);
            return new TestElement("worker1", "WorkerResult1");
        };

        // 创建并执行工作器
        ExecutableQueue.ExecutableWorker<TestElement> executableWorker = new ExecutableQueue.ExecutableWorker<>(testQueue, semaphore, worker);
        Assert.assertNotNull(executableWorker);
        Assert.assertEquals(worker, executableWorker.getWorker());

        // 执行工作器
        executableWorker.run();

        // 验证结果被添加到队列
        Thread.sleep(500);
        Assert.assertEquals(1, testQueue.size());
        TestElement result = testQueue.poll();
        Assert.assertNotNull(result);
        Assert.assertEquals("worker1", result.getId());
    }
}
