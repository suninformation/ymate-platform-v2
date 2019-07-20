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

import net.ymate.platform.core.beans.annotation.Ignored;
import net.ymate.platform.serv.nio.INioCodec;

import java.io.Closeable;
import java.io.IOException;

/**
 * 服务端接口
 *
 * @param <LISTENER> 监听器类型
 * @param <CODEC>    服务端编解码器接口类型
 * @author 刘镇 (suninformation@163.com) on 15/10/15 上午10:21
 */
@Ignored
public interface IServer<LISTENER extends IListener, CODEC extends INioCodec> extends Closeable {

    /**
     * 初始化服务端
     *
     * @param serverCfg 服务端配置
     * @param listener  事件适配器
     * @param codec     解码器
     */
    void initialize(IServerCfg serverCfg, LISTENER listener, CODEC codec);

    /**
     * 启动服务端
     *
     * @throws IOException 可能产生的异常
     */
    void start() throws IOException;

    /**
     * 判断是否已启动
     *
     * @return 返回true表示已启动
     */
    boolean isStarted();

    /**
     * 获取服务端配置
     *
     * @return 服务端配置对象
     */
    IServerCfg serverCfg();

    /**
     * 获取监听器对象
     *
     * @param <T> 监听器类型
     * @return 返回监听器接口实现类对象
     */
    <T extends LISTENER> T listener();
}
