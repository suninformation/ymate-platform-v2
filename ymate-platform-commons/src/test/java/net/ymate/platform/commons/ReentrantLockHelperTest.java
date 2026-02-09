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

import org.apache.commons.lang.NullArgumentException;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ReentrantLockHelper测试类
 *
 * @author 刘镇 (suninformation@163.com) on 2026-02-07 13:16:36
 * @since 2.1.4
 */
public class ReentrantLockHelperTest {

    @Test
    public void testPutIfAbsent() {
        // Test with ConcurrentHashMap
        Map<String, String> map = new ConcurrentHashMap<>();

        // Test putting a new value
        String result1 = ReentrantLockHelper.putIfAbsent(map, "key1", "value1");
        Assert.assertEquals("value1", result1);
        Assert.assertEquals("value1", map.get("key1"));

        // Test putting an existing value (should return existing value)
        String result2 = ReentrantLockHelper.putIfAbsent(map, "key1", "value2");
        Assert.assertEquals("value1", result2);
        Assert.assertEquals("value1", map.get("key1"));

        // Test putting null value (should not add to map)
        String result3 = ReentrantLockHelper.putIfAbsent(map, "key2", null);
        Assert.assertNull(result3);
        Assert.assertNull(map.get("key2"));

        // Test with null map (should throw exception)
        try {
            ReentrantLockHelper.putIfAbsent(null, "key", "value");
            Assert.fail("Expected NullArgumentException was not thrown");
        } catch (NullArgumentException e) {
            // Expected exception
        }

        // Test with null key (should throw exception)
        try {
            ReentrantLockHelper.putIfAbsent(map, null, "value");
            Assert.fail("Expected NullArgumentException was not thrown");
        } catch (NullArgumentException e) {
            // Expected exception
        }
    }

    @Test
    public void testPutIfAbsentAsync() throws Exception {
        // Test with ConcurrentHashMap
        Map<String, String> map = new ConcurrentHashMap<>();

        // Test putting a new value using ValueGetter
        String result1 = ReentrantLockHelper.putIfAbsentAsync(map, "key1", () -> "value1");
        Assert.assertEquals("value1", result1);
        Assert.assertEquals("value1", map.get("key1"));

        // Test putting an existing value (should return existing value)
        String result2 = ReentrantLockHelper.putIfAbsentAsync(map, "key1", () -> "value2");
        Assert.assertEquals("value1", result2);
        Assert.assertEquals("value1", map.get("key1"));

        // Test putting null value (should not add to map)
        String result3 = ReentrantLockHelper.putIfAbsentAsync(map, "key2", () -> null);
        Assert.assertNull(result3);
        Assert.assertNull(map.get("key2"));

        // Test with null map (should throw exception)
        try {
            ReentrantLockHelper.putIfAbsentAsync(null, "key", () -> "value");
            Assert.fail("Expected NullArgumentException was not thrown");
        } catch (NullArgumentException e) {
            // Expected exception
        }

        // Test with null key (should throw exception)
        try {
            ReentrantLockHelper.putIfAbsentAsync(map, null, () -> "value");
            Assert.fail("Expected NullArgumentException was not thrown");
        } catch (NullArgumentException e) {
            // Expected exception
        }
    }

    @Test
    public void testGetLocker() throws Exception {
        ReentrantLockHelper helper = new ReentrantLockHelper();

        // Test getting a locker for a new key
        ReentrantLock lock1 = helper.getLocker("lock1");
        Assert.assertNotNull(lock1);

        // Test getting the same locker for the same key
        ReentrantLock lock2 = helper.getLocker("lock1");
        Assert.assertNotNull(lock2);
        Assert.assertSame(lock1, lock2);

        // Test getting a different locker for a different key
        ReentrantLock lock3 = helper.getLocker("lock2");
        Assert.assertNotNull(lock3);
        Assert.assertNotSame(lock1, lock3);
    }

    @Test
    public void testUnlock() {
        ReentrantLock lock = new ReentrantLock();

        // Test unlocking an unlocked lock (should do nothing)
        ReentrantLockHelper.unlock(lock);

        // Test unlocking a locked lock
        lock.lock();
        Assert.assertTrue(lock.isLocked());
        ReentrantLockHelper.unlock(lock);
        Assert.assertFalse(lock.isLocked());

        // Test unlocking a null lock (should do nothing)
        ReentrantLockHelper.unlock(null);
    }

    @Test
    public void testDefaultInstance() {
        ReentrantLockHelper defaultInstance = ReentrantLockHelper.DEFAULT;
        Assert.assertNotNull(defaultInstance);
    }

    @Test
    public void testValueGetterInterface() throws Exception {
        // Test ValueGetter implementation
        ReentrantLockHelper.ValueGetter<String> valueGetter = () -> "test value";
        String value = valueGetter.getValue();
        Assert.assertEquals("test value", value);
    }

    @Test
    public void testConcurrentAccess() throws Exception {
        final ReentrantLockHelper helper = new ReentrantLockHelper();
        final Map<String, ReentrantLock> locks = new ConcurrentHashMap<>();

        // Create multiple threads to test concurrent access
        Thread[] threads = new Thread[10];
        for (int i = 0; i < threads.length; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                try {
                    // Get lock for the same key from multiple threads
                    ReentrantLock lock = helper.getLocker("shared-lock");
                    locks.put("thread-" + threadId, lock);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Verify all threads got the same lock instance
        ReentrantLock firstLock = null;
        for (ReentrantLock lock : locks.values()) {
            if (firstLock == null) {
                firstLock = lock;
            } else {
                Assert.assertSame(firstLock, lock);
            }
        }
    }
}
