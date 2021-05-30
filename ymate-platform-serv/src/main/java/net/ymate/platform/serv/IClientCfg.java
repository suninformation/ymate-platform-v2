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

import java.util.Map;

/**
 * 客户端服务配置接口
 *
 * @author 刘镇 (suninformation@163.com) on 15/11/4 下午5:36
 */
public interface IClientCfg {

    /**
     * 获取客户端名称
     *
     * @return 返回客户端名称
     */
    String getClientName();

    /**
     * 获取远程主机名称或IP地址
     *
     * @return 返回远程主机名称或IP地址
     */
    String getRemoteHost();

    /**
     * 获取远程服务监听端口
     *
     * @return 返回远程服务监听端口
     */
    int getPort();

    /**
     * 获取字符编码
     *
     * @return 返回字符编码
     */
    String getCharset();

    /**
     * 获取缓冲区大小
     *
     * @return 返回缓冲区大小
     */
    int getBufferSize();

    /**
     * 获取执行线程数量
     *
     * @return 返回执行线程数量
     */
    int getExecutorCount();

    /**
     * 获取连接超时时间(秒)
     *
     * @return 返回连接超时时间
     */
    int getConnectionTimeout();

    /**
     * 获取断线重连检测间隔(秒)
     *
     * @return 返回断线重连检测间隔
     */
    int getReconnectionInterval();

    /**
     * 获取心跳包发送间隔(秒)
     *
     * @return 返回心跳包发送间隔
     */
    int getHeartbeatInterval();

    /**
     * 获取客户端自定义参数映射
     *
     * @return 返回客户端自定义参数映射
     */
    Map<String, String> getParams();

    /**
     * 获取客户端自定义参数值
     *
     * @param key 参数名称
     * @return 返回客户端自定义参数值
     */
    String getParam(String key);

    /**
     * 获取客户端自定义参数值
     *
     * @param key          参数名称
     * @param defaultValue 默认值
     * @return 返回客户端自定义参数值
     */
    String getParam(String key, String defaultValue);
}
