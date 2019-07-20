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
package net.ymate.platform.persistence.redis;

import net.ymate.platform.core.beans.annotation.Ignored;
import net.ymate.platform.core.persistence.IDataSourceConfig;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.util.Map;

/**
 * Redis数据源配置接口
 *
 * @author 刘镇 (suninformation@163.com) on 2019-05-22 02:33
 */
@Ignored
public interface IRedisDataSourceConfig extends IDataSourceConfig<IRedis> {

    /**
     * 获取连接方式
     *
     * @return 返回连接方式枚举值
     */
    IRedis.ConnectionType getConnectionType();

    /**
     * 获取主服务名称
     *
     * @return 返回服务名称字符串
     */
    String getMasterServerName();

    /**
     * 获取主服务配置描述对象
     *
     * @return 返回服务配置描述对象
     */
    RedisServerMeta getMasterServerMeta();

    /**
     * 获取服务配置描述对象映射
     *
     * @return 返回服务配置描述对象映射
     */
    Map<String, RedisServerMeta> getServerMetas();

    /**
     * 获取对象池配置
     *
     * @return 返回对象池配置
     */
    GenericObjectPoolConfig getObjectPoolConfig();
}
