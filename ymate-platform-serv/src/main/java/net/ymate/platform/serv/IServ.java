/*
 * Copyright 2007-2016 the original author or authors.
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

import net.ymate.platform.core.YMP;

/**
 * @author 刘镇 (suninformation@163.com) on 15/10/15 上午10:22
 * @version 1.0
 */
public interface IServ {

    String MODULE_NAME = "serv";

    /**
     * @return 返回所属YMP框架管理器实例
     */
    YMP getOwner();

    /**
     * @return 返回服务模块配置对象
     */
    IServModuleCfg getModuleCfg();

    <T> T getServer(Class<? extends IListener> clazz);

    <T> T getClient(Class<? extends IListener> clazz);

    /**
     * 注册服务端
     *
     * @param listenerClass 服务监听接口类型
     * @param server        服务端事件适配器
     * @throws Exception 可能产生的异常
     */
    void registerServer(Class<? extends IListener> listenerClass, IServer server) throws Exception;

    /**
     * 注册客户端
     *
     * @param listenerClass 服务监听接口类型
     * @param client        客户端事件适配器
     * @throws Exception 可能产生的异常
     */
    void registerClient(Class<? extends IListener> listenerClass, IClient client) throws Exception;
}
