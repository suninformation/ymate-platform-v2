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
package net.ymate.platform.persistence.redis.impl;

import net.ymate.platform.core.util.RuntimeUtils;
import net.ymate.platform.persistence.redis.IRedis;
import net.ymate.platform.persistence.redis.IRedisCommandsHolder;
import net.ymate.platform.persistence.redis.IRedisDataSourceAdapter;
import net.ymate.platform.persistence.redis.RedisDataSourceCfgMeta;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCommands;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author 刘镇 (suninformation@163.com) on 15/12/4 上午1:04
 * @version 1.0
 */
public class RedisCommandsHolder implements IRedisCommandsHolder {

    private static final Log _LOG = LogFactory.getLog(RedisCommandsHolder.class);

    private IRedisDataSourceAdapter __dataSourceAdapter;

    private ThreadLocal<JedisCommands> __jedisCommands = new ThreadLocal<JedisCommands>();

    public RedisCommandsHolder(IRedisDataSourceAdapter dataSourceAdapter) {
        __dataSourceAdapter = dataSourceAdapter;
    }

    @Override
    public RedisDataSourceCfgMeta getDataSourceCfgMeta() {
        return __dataSourceAdapter.getDataSourceCfgMeta();
    }

    @Override
    public Jedis getJedis() {
        JedisCommands _commands = getCommands();
        if (_commands instanceof Jedis) {
            return (Jedis) _commands;
        }
        return null;
    }

    @Override
    public JedisCommands getCommands() {
        JedisCommands _commands = __jedisCommands.get();
        if (_commands == null) {
            _commands = __dataSourceAdapter.getCommands();
            __jedisCommands.set(_commands);
        }
        return _commands;
    }

    @Override
    public void release() {
        JedisCommands _commands = __jedisCommands.get();
        if (_commands != null && !IRedis.ConnectionType.CLUSTER.equals(__dataSourceAdapter.getDataSourceCfgMeta().getConnectionType())) {
            if (_commands instanceof Closeable) {
                try {
                    ((Closeable) _commands).close();
                } catch (IOException e) {
                    _LOG.warn("An exception occurs when the JedisCommands is released: ", RuntimeUtils.unwrapThrow(e));
                }
            }
        }
        __jedisCommands.remove();
    }
}
