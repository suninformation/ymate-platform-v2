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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author 刘镇 (suninformation@163.com) on 2022/8/29 12:41
 * @since 1.0.0
 */
public class ExecutableQueueTest extends ExecutableQueue<ExecutableQueueTest.CustomMessage>
        implements ExecutableQueue.IListener<ExecutableQueueTest.CustomMessage> {

    private static final Log LOG = LogFactory.getLog(ExecutableQueueTest.class);

    private final List<IFilter<CustomMessage>> filters = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        filters.add(element -> {
            // 过滤掉所有类型为 info 的消息
            return element.getType().equalsIgnoreCase("info");
        });
        // 启动队列监听服务
        addListener(this);
        listenStart();
    }

    @After
    public void tearDown() throws Exception {
        Thread.sleep(TimeUnit.SECONDS.toMillis(5));
        // 移除监听器
        removeListener(ExecutableQueueTest.class);
        // 手动停止队列服务
        listenStop();
    }

    @Test
    public void start() {
        // 定义一个类型为 warn 的消息对象
        CustomMessage message = new CustomMessage();
        message.setId(UUIDUtils.UUID());
        message.setType("warn");
        message.setContent("WARN: 警告信息");
        // 将消息推送到队列
        putElement(message);
        // 定义一个类型为 info 的消息对象
        message = new CustomMessage();
        message.setId(UUIDUtils.UUID());
        message.setType("info");
        message.setContent("INFO: 此条信息将被过滤");
        // 将消息推送到队列
        putElement(message);
        //
        try {
            putElement(execute(() -> {
                CustomMessage msg = new CustomMessage();
                msg.setId(UUIDUtils.UUID());
                msg.setType("warn");
                msg.setContent("通过FutureTask方式执行业务逻辑以获取消息对象，如：HTTP请求某接口、数据库中查询某数据等");
                return msg;
            }, 10));
            //
            execute(Collections.singletonList(() -> {
                CustomMessage msg = new CustomMessage();
                msg.setId(UUIDUtils.UUID());
                msg.setType("warn");
                msg.setContent("批量获取消息对象");
                return msg;
            }));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onListenStarted() {
        LOG.info("重写此方法用于处理队列监听服务启动事件。");
    }

    @Override
    protected void onListenStopped() {
        LOG.info("重写此方法用于处理队列监听服务停止事件。");
    }

    @Override
    protected void onListenerAdded(String id, IListener<CustomMessage> listener) {
        LOG.info("重写此方法用于处理监听器注册成功事件。");
    }

    @Override
    protected void onListenerRemoved(String id, IListener<CustomMessage> listener) {
        LOG.info("重写此方法用于处理监听器移除成功事件。");
    }

    @Override
    protected void onElementAdded(CustomMessage element) {
        LOG.info("重写此方法用于处理元素被成功推送至队列事件。");
    }

    @Override
    protected void onElementAbandoned(CustomMessage element) {
        LOG.info("重写此方法用于处理队列元素被丢弃事件。");
    }

    @Override
    protected void onSpeedometerListen(long speed, long averageSpeed, long maxSpeed, long minSpeed) {
        LOG.info("重写此方法用于处理速度计数器监听数据。");
    }

    @Override
    protected void doSpeedometerStart(Speedometer speedometer) {
        // 重定此方法用于设置速度计数器配置参数
        super.doSpeedometerStart(speedometer.interval(2));
    }

    @Override
    public List<IFilter<CustomMessage>> getFilters() {
        return filters;
    }

    @Override
    public void listen(CustomMessage element) {
        LOG.info(element);
    }

    @Override
    public boolean abandoned(CustomMessage element) {
        return IListener.super.abandoned(element);
    }

    /**
     * 自定义消息
     */
    public static class CustomMessage implements Serializable {

        /**
         * 消息唯一标识
         */
        private String id;

        /**
         * 消息类型
         */
        private String type;

        /**
         * 消息内容
         */
        private String content;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        @Override
        public String toString() {
            return String.format("CustomMessage{id='%s', type='%s', content='%s'}", id, type, content);
        }
    }
}