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
package net.ymate.platform.serv;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/11/20 9:55 AM
 * @version 1.0
 */
public abstract class AbstractService extends Thread implements Closeable {

    private boolean __inited;

    private boolean __started;

    public boolean isInited() {
        return __inited;
    }

    public boolean isStarted() {
        return __started;
    }

    protected void __doInit() {
        __inited = true;
    }

    protected boolean __doStart() {
        return true;
    }

    protected abstract void __doService();

    @Override
    public void start() {
        if (__inited && !__started) {
            if (__doStart()) {
                __started = true;
                super.start();
            }
        }
    }

    @Override
    public void run() {
        if (isInited()) {
            while (isStarted()) {
                __doService();
            }
        }
    }

    @Override
    public void interrupt() {
        if (__inited && __started) {
            __started = false;
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
