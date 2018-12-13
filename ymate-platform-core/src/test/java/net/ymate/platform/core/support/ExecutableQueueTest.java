package net.ymate.platform.core.support;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

/**
 * @author 刘镇 (suninformation@163.com) on 2018-12-13 10:12
 * @version 1.0
 */
public class ExecutableQueueTest {

    @Test
    public void executableQueue() {
        ExecutableQueue<String> _queue = new ExecutableQueue<String>("MyQueue");
        _queue.addListener(new ExecutableQueue.IListener<String>() {
            @Override
            public List<ExecutableQueue.IFilter<String>> getFilters() {
                ExecutableQueue.IFilter<String> _filter = new ExecutableQueue.IFilter<String>() {
                    @Override
                    public boolean filter(String element) {
                        return StringUtils.containsIgnoreCase(element, "filter");
                    }
                };
                return Collections.singletonList(_filter);
            }

            @Override
            public void listen(String element) {
                System.out.println("Listen: " + element);
            }
        });
        _queue.listenStart();
        _queue.putElement("Hi.");
        _queue.putElement("This element will be filtered.");
        _queue.putElement("ExecutableQueue.");
        _queue.destroy();
    }
}