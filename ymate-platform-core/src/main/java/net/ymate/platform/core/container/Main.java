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
package net.ymate.platform.core.container;

import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.container.impl.DefaultContainer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/03/15 14:41
 */
public class Main {

    private static final Log LOG = LogFactory.getLog(Main.class);

    private static volatile boolean running = true;

    /**
     * 主程序入口
     *
     * @param args 参数集合
     */
    public static void main(String[] args) {
        try {
            List<IContainer> containers = new ArrayList<>(ContainerManager.getContainers());
            if (containers.isEmpty()) {
                // 若未设定任何容器则添加默认容器
                containers.add(new DefaultContainer());
            }
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                for (IContainer container : containers) {
                    try {
                        container.stop();
                        if (LOG.isInfoEnabled()) {
                            LOG.info(String.format("Container [%s] stopped.", container.getClass().getName()));
                        }
                    } catch (Throwable e) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error(e.getMessage(), RuntimeUtils.unwrapThrow(e));
                        }
                    }
                    synchronized (Main.class) {
                        running = false;
                        Main.class.notify();
                    }
                }
            }));
            for (IContainer container : containers) {
                container.start(args);
                if (LOG.isInfoEnabled()) {
                    LOG.info(String.format("Container [%s] started.", container.getClass().getName()));
                }
            }
        } catch (Throwable e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getMessage(), RuntimeUtils.unwrapThrow(e));
            }
            System.exit(1);
        }
        synchronized (Main.class) {
            while (running) {
                try {
                    Main.class.wait();
                } catch (Throwable ignored) {
                }
            }
        }
    }
}
