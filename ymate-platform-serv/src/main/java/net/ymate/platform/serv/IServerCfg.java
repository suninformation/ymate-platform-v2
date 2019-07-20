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

import java.util.Map;

/**
 * 服务端配置接口
 *
 * @author 刘镇 (suninformation@163.com) on 15/11/4 下午5:35
 */
@Ignored
public interface IServerCfg {

    /**
     * 获取服务名称
     *
     * @return 返回服务名称
     */
    String getServerName();

    /**
     * 获取主机名称或IP地址
     *
     * @return 返回主机名称或IP地址
     */
    String getServerHost();

    /**
     * 获取服务监听端口
     *
     * @return 返回服务监听端口
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
     * 获取执行线程数量，默认为 Runtime.getRuntime().availableProcessors()
     *
     * @return 返回执行线程数量
     */
    int getExecutorCount();

    /**
     * 获取空闲线程等待新任务的最长时间, 默认为 0
     *
     * @return 返回空闲线程等待新任务的最长时间
     */
    long getKeepAliveTime();

    /**
     * 获取最大线程池大小，默认为 200
     *
     * @return 返回最大线程池大小
     */
    int getThreadMaxPoolSize();

    /**
     * 获取线程队列大小，默认为 1024
     *
     * @return 返回线程队列大小
     */
    int getThreadQueueSize();

    /**
     * 获取选择器数量
     *
     * @return 返回选择器数量
     */
    int getSelectorCount();

    /**
     * 获取 服务端自定义参数映射
     *
     * @return 返回服务端自定义参数映射
     */
    Map<String, String> getParams();

    /**
     * 获取服务端自定义参数值
     *
     * @param key 参数名称
     * @return 返回服务端自定义参数值
     */
    String getParam(String key);

    /**
     * 获取服务端自定义参数值
     *
     * @param key          参数名称
     * @param defaultValue 默认值
     * @return 返回服务端自定义参数值
     */
    String getParam(String key, String defaultValue);
}
