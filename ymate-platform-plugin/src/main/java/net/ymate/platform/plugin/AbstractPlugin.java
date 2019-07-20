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
package net.ymate.platform.plugin;

/**
 * 插件启动器接口抽象实现，完成必要参数的赋值动作
 *
 * @author 刘镇 (suninformation@163.com) on 2012-4-20 下午5:30:30
 */
public abstract class AbstractPlugin implements IPlugin {

    private IPluginContext context;

    private boolean initialized;

    private boolean started;

    @Override
    public void initialize(IPluginContext context) {
        if (!initialized) {
            this.context = context;
            initialized = true;
        }
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public IPluginContext getPluginContext() {
        return context;
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    @Override
    public void startup() {
        if (initialized) {
            started = true;
        }
    }

    @Override
    public void shutdown() {
        if (started) {
            started = false;
        }
    }

    @Override
    public void close() {
        if (initialized) {
            shutdown();
            //
            initialized = false;
            context = null;
        }
    }
}
