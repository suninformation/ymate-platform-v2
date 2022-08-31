/*
 * Copyright 2007-2022 the original author or authors.
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

import net.ymate.platform.commons.util.UUIDUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author 刘镇 (suninformation@163.com) on 2022/6/12 23:40
 * @since 2.1.2
 */
public class StopWatcherTest {

    private static final Log LOG = LogFactory.getLog(StopWatcherTest.class);

    @Test
    public void watch() {
        StopWatcher<Void> watcher = StopWatcher.watch(() -> {
            // do something...
        });
        assertNotNull(watcher);
        StopWatch stopWatch = watcher.getStopWatch();
        LOG.info(String.format("watch: %d ms", stopWatch.getTime()));
        LOG.info(String.format("watch: %s sec", MathCalcHelper.bind(stopWatch.getTime(TimeUnit.MICROSECONDS)).scale(3).divide(1000000L).value()));
    }

    @Test
    public void testWatch() {
        StopWatcher<Long> watcher = null;
        try {
            watcher = StopWatcher.watch(() -> {
                TimeUnit.NANOSECONDS.sleep(UUIDUtils.randomLong(3, 9));
                return 10L;
            });
        } catch (Exception ignored) {
        }
        assertNotNull(watcher);
        LOG.info(String.format("testWatch: %dms", watcher.getStopWatch().getTime()));
    }

    @Test
    public void getValue() {
        StopWatcher<Long> watcher = null;
        try {
            watcher = StopWatcher.watch(() -> 10L);
        } catch (Exception ignored) {
        }
        assertNotNull(watcher);
        assertEquals(watcher.getValue().longValue(), 10L);
    }
}