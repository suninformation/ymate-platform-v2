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

import org.apache.commons.lang3.time.StopWatch;
import org.junit.Assert;
import org.junit.Test;

/**
 * StopWatcher测试类
 *
 * @author 刘镇 (suninformation@163.com) on 2022/6/12 23:40
 * @since 2.1.2
 */
public class StopWatcherTest {

    @Test
    public void testWatchWithRunnable() {
        // Test with a simple Runnable
        final boolean[] runnableExecuted = {false};

        StopWatcher<Void> watcher = StopWatcher.watch(() -> {
            runnableExecuted[0] = true;
            // Add a small delay to ensure time is measured
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Verify Runnable was executed
        Assert.assertTrue(runnableExecuted[0]);

        // Verify StopWatch was created and stopped
        StopWatch stopWatch = watcher.getStopWatch();
        Assert.assertNotNull(stopWatch);
        Assert.assertTrue(stopWatch.isStopped());
        Assert.assertTrue(stopWatch.getTime() > 0);

        // Verify value is null for Runnable
        Assert.assertNull(watcher.getValue());
    }

    @Test
    public void testWatchWithCallable() throws Exception {
        // Test with a simple Callable that returns a value
        String expectedValue = "test value";

        StopWatcher<String> watcher = StopWatcher.watch(() -> {
            // Add a small delay to ensure time is measured
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return expectedValue;
        });

        // Verify StopWatch was created and stopped
        StopWatch stopWatch = watcher.getStopWatch();
        Assert.assertNotNull(stopWatch);
        Assert.assertTrue(stopWatch.isStopped());
        Assert.assertTrue(stopWatch.getTime() > 0);

        // Verify value was returned from Callable
        String actualValue = watcher.getValue();
        Assert.assertNotNull(actualValue);
        Assert.assertEquals(expectedValue, actualValue);
    }

    @Test
    public void testWatchWithCallableThatThrowsException() throws Exception {
        // Test with a Callable that throws an exception
        Exception expectedException = new RuntimeException("Test exception");

        try {
            StopWatcher.watch(() -> {
                throw expectedException;
            });
            Assert.fail("Expected RuntimeException was not thrown");
        } catch (RuntimeException e) {
            // Expected exception
            Assert.assertSame(expectedException, e);
        }
    }

    @Test
    public void testWatchWithCallableReturningNull() throws Exception {
        // Test with a Callable that returns null
        StopWatcher<String> watcher = StopWatcher.watch(() -> null);

        // Verify StopWatch was created and stopped
        StopWatch stopWatch = watcher.getStopWatch();
        Assert.assertNotNull(stopWatch);
        Assert.assertTrue(stopWatch.isStopped());

        // Verify value is null
        Assert.assertNull(watcher.getValue());
    }

    @Test
    public void testStopWatchTimeMeasurement() {
        // Test that StopWatch properly measures time
        StopWatcher<Void> watcher1 = StopWatcher.watch(() -> {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        StopWatcher<Void> watcher2 = StopWatcher.watch(() -> {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Verify that the second execution took longer
        long time1 = watcher1.getStopWatch().getTime();
        long time2 = watcher2.getStopWatch().getTime();
        Assert.assertTrue(time1 > 0);
        Assert.assertTrue(time2 > 0);
        Assert.assertTrue(time2 > time1);
    }

    @Test
    public void testGetStopWatch() {
        // Test getStopWatch method
        StopWatcher<Void> watcher = StopWatcher.watch(() -> {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        StopWatch stopWatch = watcher.getStopWatch();
        Assert.assertNotNull(stopWatch);
    }

    @Test
    public void testGetValue() throws Exception {
        // Test getValue method with non-null value
        Integer expectedValue = 42;
        StopWatcher<Integer> watcher = StopWatcher.watch(() -> expectedValue);
        Assert.assertEquals(expectedValue, watcher.getValue());

        // Test getValue method with null value
        StopWatcher<Integer> watcherNull = StopWatcher.watch(() -> null);
        Assert.assertNull(watcherNull.getValue());
    }
}
