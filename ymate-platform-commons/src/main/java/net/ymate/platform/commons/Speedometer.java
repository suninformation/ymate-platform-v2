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
import net.ymate.platform.commons.util.DateTimeUtils;
import net.ymate.platform.commons.util.ThreadUtils;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;

/**
 * 速度计数器
 *
 * @author 刘镇 (suninformation@163.com) on 16/12/27 下午4:14
 */
public class Speedometer implements AutoCloseable {

    public static Speedometer create(String name) {
        return new Speedometer(name);
    }

    private final String name;

    private int interval = 5000;

    private int dataSize = 20;

    private long touchTimes;

    private final LinkedList<Long> data = new LinkedList<>();

    private final LinkedList<Long> sorted = new LinkedList<>();

    private ISpeedListener listener;

    private ExecutorService executorService;

    private boolean started;

    public Speedometer(String name) {
        if (StringUtils.isBlank(name)) {
            throw new NullArgumentException("name");
        }
        this.name = name;
    }

    public boolean isStarted() {
        return started;
    }

    public String name() {
        return name;
    }

    public Speedometer interval(int interval) {
        if (interval >= DateTimeUtils.SECOND) {
            this.interval = interval;
        }
        return this;
    }

    public Speedometer dataSize(int dataSize) {
        if (dataSize >= 5) {
            this.dataSize = dataSize;
        }
        return this;
    }

    public long touchTimes() {
        return touchTimes;
    }

    public void touch() {
        touchTimes++;
    }

    public long reset() {
        long curr = touchTimes;
        touchTimes = 0;
        return curr;
    }

    public void start(ISpeedListener listener) {
        if (listener == null) {
            throw new NullArgumentException("listener");
        }
        this.listener = listener;
        if (!started) {
            executorService = ThreadUtils.newSingleThreadExecutor(1, DefaultThreadFactory.create(String.format("%s-Speedometer", StringUtils.capitalize(name))));
            executorService.submit(() -> {
                try {
                    while (Speedometer.this.listener != null) {
                        long historyTouchTimes = touchTimes;
                        Thread.sleep(interval);
                        long times = touchTimes - historyTouchTimes;
                        if (times > 0) {
                            data.add(times);
                            sorted.add(times);
                            //
                            Collections.sort(sorted);
                            while (data.size() > dataSize) {
                                sorted.remove(data.removeFirst());
                            }
                            //
                            long maxSpeed = sorted.getLast();
                            long minSpeed = sorted.getFirst();
                            long amount = sorted.stream().mapToLong(s -> s).sum();
                            //
                            Speedometer.this.listener.listen(times, (amount / sorted.size()), maxSpeed, minSpeed);
                        }
                    }
                } catch (InterruptedException ignored) {
                }
            });
            //
            started = true;
        }
    }

    @Override
    public void close() {
        if (started && executorService != null) {
            started = false;
            executorService.shutdownNow();
        }
    }

    /**
     * 速度统计监听器接口
     *
     * @author 刘镇 (suninformation@163.com) on 16/12/27 下午4:14
     * @see net.ymate.platform.commons.ISpeedListener
     * @deprecated 已迁移为独立接口文件
     */
    @Deprecated
    public interface IListener extends ISpeedListener {
    }
}
