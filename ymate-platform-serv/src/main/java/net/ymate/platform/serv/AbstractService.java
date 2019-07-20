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
package net.ymate.platform.serv;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/11/20 9:55 AM
 */
public abstract class AbstractService extends Thread implements Closeable {

    private boolean initialized;

    private boolean started;

    public boolean isInitialized() {
        return initialized;
    }

    public boolean isStarted() {
        return started;
    }

    protected void doInit() {
        initialized = true;
    }

    protected boolean doStart() {
        return true;
    }

    /**
     * 由子类实现具体服务处理逻辑
     */
    protected abstract void doService();

    @Override
    public void start() {
        if (initialized && !started) {
            if (doStart()) {
                started = true;
                super.start();
            }
        }
    }

    @Override
    public void run() {
        if (isInitialized()) {
            while (isStarted()) {
                doService();
            }
        }
    }

    @Override
    public void interrupt() {
        if (initialized && started) {
            started = false;
            try {
                join(3000L);
            } catch (InterruptedException ignored) {
            }
            super.interrupt();
        }
    }

    @Override
    public void close() throws IOException {
        interrupt();
    }
}
