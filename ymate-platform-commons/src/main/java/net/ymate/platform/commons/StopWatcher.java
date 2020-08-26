/*
 * Copyright 2007-2020 the original author or authors.
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

import java.util.concurrent.Callable;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/03/02 12:49
 * @since 2.1.0
 */
public final class StopWatcher<V> {

    public static StopWatcher<Void> watch(Runnable runnable) {
        return new StopWatcher<>(runnable);
    }

    public static <V> StopWatcher<V> watch(Callable<V> callable) throws Exception {
        return new StopWatcher<>(callable);
    }

    private final StopWatch stopWatch = new StopWatch();

    private V value;

    public StopWatcher(Runnable runnable) {
        stopWatch.start();
        try {
            runnable.run();
        } finally {
            stopWatch.stop();
        }
    }

    public StopWatcher(Callable<V> callable) throws Exception {
        stopWatch.start();
        try {
            value = callable.call();
        } finally {
            stopWatch.stop();
        }
    }

    public V getValue() {
        return value;
    }

    public StopWatch getStopWatch() {
        return stopWatch;
    }
}
