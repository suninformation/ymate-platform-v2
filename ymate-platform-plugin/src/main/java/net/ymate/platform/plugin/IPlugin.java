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

import net.ymate.platform.core.beans.annotation.Ignored;
import net.ymate.platform.core.support.IDestroyable;
import net.ymate.platform.core.support.IInitialization;

/**
 * 插件启动器接口类，任何插件的启动器类都必须实现该接口
 *
 * @author 刘镇 (suninformation@163.com) on 2011-10-17 下午04:44:06
 */
@Ignored
public interface IPlugin extends IInitialization<IPluginContext>, IDestroyable {

    /**
     * 获取插件环境上下文
     *
     * @return 返回插件环境上下文对象
     */
    IPluginContext getPluginContext();

    /**
     * 插件是否已启动
     *
     * @return 返回true表示已启动
     */
    boolean isStarted();

    /**
     * 启动插件
     *
     * @throws Exception 启动插件时可能产生的异常
     */
    void startup() throws Exception;

    /**
     * 停止插件
     *
     * @throws Exception 停止插件时可能产生的异常
     */
    void shutdown() throws Exception;
}
