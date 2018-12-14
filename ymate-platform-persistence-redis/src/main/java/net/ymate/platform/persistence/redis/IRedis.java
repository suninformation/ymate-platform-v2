/*
 * Copyright 2007-2017 the original author or authors.
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
package net.ymate.platform.persistence.redis;

import net.ymate.platform.core.YMP;
import net.ymate.platform.persistence.IDataSourceRouter;
import redis.clients.jedis.JedisPubSub;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/30 上午3:16
 * @version 1.0
 */
public interface IRedis {

    String MODULE_NAME = "persistence.redis";

    /**
     * @return 返回所属YMP框架管理器实例
     */
    YMP getOwner();

    /**
     * @return 返回Redis模块配置对象
     */
    IRedisModuleCfg getModuleCfg();

    /**
     * @return 获取默认命令对象持有者对象
     */
    IRedisCommandsHolder getDefaultCommandsHolder();

    /**
     * @param dsName 数据源名称
     * @return 获取由dsName指定的命令对象持有者对象
     */
    IRedisCommandsHolder getCommandsHolder(String dsName);

    <T> T openSession(IRedisSessionExecutor<T> executor) throws Exception;

    <T> T openSession(String dsName, IRedisSessionExecutor<T> executor) throws Exception;

    <T> T openSession(IRedisCommandsHolder commandsHolder, IRedisSessionExecutor<T> executor) throws Exception;

    <T> T openSession(IDataSourceRouter dataSourceRouter, IRedisSessionExecutor<T> executor) throws Exception;

    /**
     * @return 开启Redis连接会话(注意一定记得关闭会话)
     */
    IRedisSession openSession();

    IRedisSession openSession(String dsName);

    IRedisSession openSession(IRedisCommandsHolder commandsHolder);

    IRedisSession openSession(IDataSourceRouter dataSourceRouter);

    /**
     * 订阅
     *
     * @param jedisPubSub 发布订阅对象
     * @param channels    频道
     */
    void subscribe(JedisPubSub jedisPubSub, String... channels);

    void subscribe(String dsName, JedisPubSub jedisPubSub, String... channels);

    /**
     * 数据源连接方式
     */
    enum ConnectionType {
        DEFAULT, SHARD, SENTINEL, CLUSTER
    }
}
