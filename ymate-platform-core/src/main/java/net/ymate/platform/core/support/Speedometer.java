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

import net.ymate.platform.core.util.ThreadUtils;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;

import java.io.Closeable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;

/**
 * 速度计数器
 *
 * @author 刘镇 (suninformation@163.com) on 16/12/27 下午4:14
 * @version 1.0
 */
public class Speedometer implements Closeable {

    private String __name;

    private int __interval = 5000;

    private int __dataSize = 20;

    private long __touchTimes;

    private LinkedList<Long> __data;

    private LinkedList<Long> __sorted;

    private ISpeedListener __listener;

    private ExecutorService __executorService;

    private boolean __started;

    public Speedometer() {
        this(null);
    }

    public Speedometer(String name) {
        __name = StringUtils.defaultIfBlank(name, "default");
        __data = new LinkedList<Long>();
        __sorted = new LinkedList<Long>();
    }

    public boolean isStarted() {
        return __started;
    }

    public String name() {
        return __name;
    }

    public Speedometer interval(int interval) {
        if (interval >= 1000) {
            __interval = interval;
        }
        return this;
    }

    public Speedometer dataSize(int dataSize) {
        if (dataSize >= 5) {
            __dataSize = dataSize;
        }
        return this;
    }

    public long touchTimes() {
        return __touchTimes;
    }

    public void touch() {
        __touchTimes++;
    }

    public long reset() {
        long _curr = __touchTimes;
        __touchTimes = 0;
        return _curr;
    }

    public void start(ISpeedListener listener) {
        if (listener == null) {
            throw new NullArgumentException("listener");
        }
        __listener = listener;
        if (!__started) {
            __executorService = ThreadUtils.newSingleThreadExecutor(1, ThreadUtils.createFactory("speedometer-"));
            __executorService.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (__listener != null) {
                            long _historyTouchTimes = __touchTimes;
                            Thread.sleep(__interval);
                            long _times = __touchTimes - _historyTouchTimes;
                            if (_times > 0) {
                                __data.add(_times);
                                __sorted.add(_times);
                                //
                                Collections.sort(__sorted);
                                while (__data.size() > __dataSize) {
                                    __sorted.remove(__data.removeFirst());
                                }
                                //
                                long _maxSpeed = __sorted.getLast();
                                long _minSpeed = __sorted.getFirst();
                                long _amount = 0;
                                for (Long s : __sorted) {
                                    _amount += s;
                                }
                                //
                                __listener.listen(_times, (_amount / __sorted.size()), _maxSpeed, _minSpeed);
                            }
                        }
                    } catch (InterruptedException ignored) {
                    }
                }
            });
            //
            __started = true;
        }
    }

    @Override
    public void close() {
        if (__started && __executorService != null) {
            __started = false;
            __executorService.shutdown();
        }
    }
}
