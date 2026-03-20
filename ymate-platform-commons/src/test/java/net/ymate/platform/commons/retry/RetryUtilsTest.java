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
package net.ymate.platform.commons.retry;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * RetryUtils 单元测试
 *
 * @author 刘镇 (suninformation@163.com) on 2026-03-20
 * @since 2.1.4
 */
public class RetryUtilsTest {

    @Test
    public void testExecuteWithRetrySuccess() throws Exception {
        AtomicInteger attemptCount = new AtomicInteger(0);
        String result = RetryUtils.executeWithRetry(() -> {
            attemptCount.incrementAndGet();
            return "success";
        });
        Assert.assertEquals("success", result);
        Assert.assertEquals(1, attemptCount.get());
    }

    @Test
    public void testExecuteWithRetryRetrySuccess() throws Exception {
        AtomicInteger attemptCount = new AtomicInteger(0);
        String result = RetryUtils.executeWithRetry(() -> {
            int count = attemptCount.incrementAndGet();
            if (count < 3) {
                throw new IOException("Temporary failure");
            }
            return "success after retry";
        }, 3, 100);
        Assert.assertEquals("success after retry", result);
        Assert.assertEquals(3, attemptCount.get());
    }

    @Test(expected = IOException.class)
    public void testExecuteWithRetryAllFailed() throws Exception {
        AtomicInteger attemptCount = new AtomicInteger(0);
        RetryUtils.executeWithRetry(() -> {
            attemptCount.incrementAndGet();
            throw new IOException("Permanent failure");
        }, 3, 100);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExecuteWithRetryNonRetryableException() throws Exception {
        AtomicInteger attemptCount = new AtomicInteger(0);
        RetryUtils.executeWithRetry(() -> {
            attemptCount.incrementAndGet();
            throw new IllegalArgumentException("Non-retryable exception");
        }, 3, 100, IOException.class);
        Assert.assertEquals(1, attemptCount.get());
    }

    @Test(expected = RuntimeException.class)
    public void testExecuteWithRetryTimeout() throws Exception {
        RetryConfig config = RetryConfig.custom()
                .maxRetries(3)
                .fixedDelay(200)
                .totalTimeoutMs(300L)
                .build();
        RetryUtils.executeWithRetry(() -> {
            Thread.sleep(150);
            throw new IOException("Temporary failure");
        }, config);
    }

    @Test(expected = RuntimeException.class)
    public void testExecuteWithRetryInterrupted() throws Exception {
        Thread.currentThread().interrupt();
        try {
            RetryUtils.executeWithRetry(() -> {
                throw new IOException("Temporary failure");
            }, 2, 100);
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    public void testExecuteWithRetryFixedDelay() throws Exception {
        AtomicInteger attemptCount = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();
        RetryConfig config = RetryConfig.custom()
                .maxRetries(3)
                .fixedDelay(200)
                .build();
        try {
            RetryUtils.executeWithRetry(() -> {
                attemptCount.incrementAndGet();
                throw new IOException("Temporary failure");
            }, config);
        } catch (IOException e) {
            // Expected
        }
        long elapsed = System.currentTimeMillis() - startTime;
        Assert.assertEquals(3, attemptCount.get());
        Assert.assertTrue(elapsed >= 400); // 2 retries * 200ms
    }

    @Test
    public void testExecuteWithRetryExponentialDelay() throws Exception {
        AtomicInteger attemptCount = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();
        RetryConfig config = RetryConfig.custom()
                .maxRetries(3)
                .exponentialDelay(100)
                .build();
        try {
            RetryUtils.executeWithRetry(() -> {
                attemptCount.incrementAndGet();
                throw new IOException("Temporary failure");
            }, config);
        } catch (IOException e) {
            // Expected
        }
        long elapsed = System.currentTimeMillis() - startTime;
        Assert.assertEquals(3, attemptCount.get());
        Assert.assertTrue(elapsed >= 300); // 100ms + 200ms
    }

    @Test
    public void testExecuteWithRetryRandomDelay() throws Exception {
        AtomicInteger attemptCount = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();
        RetryConfig config = RetryConfig.custom()
                .maxRetries(3)
                .randomDelay(100, 200)
                .build();
        try {
            RetryUtils.executeWithRetry(() -> {
                attemptCount.incrementAndGet();
                throw new IOException("Temporary failure");
            }, config);
        } catch (IOException e) {
            // Expected
        }
        long elapsed = System.currentTimeMillis() - startTime;
        Assert.assertEquals(3, attemptCount.get());
        Assert.assertTrue(elapsed >= 200); // At least 100ms * 2
    }

    @Test
    public void testExecuteWithRetryDefaultConfig() throws Exception {
        AtomicInteger attemptCount = new AtomicInteger(0);
        String result = RetryUtils.executeWithRetry(() -> {
            int count = attemptCount.incrementAndGet();
            if (count < 2) {
                throw new IOException("Temporary failure");
            }
            return "success";
        });
        Assert.assertEquals("success", result);
        Assert.assertEquals(2, attemptCount.get());
    }
}
