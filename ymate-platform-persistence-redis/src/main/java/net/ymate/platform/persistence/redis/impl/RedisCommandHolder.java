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

import net.ymate.platform.commons.ConcurrentHashSet;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.persistence.redis.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

/**
 * @author 刘镇 (suninformation@163.com) on 15/12/4 上午1:04
 */
public class RedisCommandHolder implements IRedisCommandHolder {

    private static final Log LOG = LogFactory.getLog(RedisCommandHolder.class);

    private final Set<IRedisCommander> cacheCommanders = new ConcurrentHashSet<>();

    private final IRedisDataSourceAdapter dataSourceAdapter;

    public RedisCommandHolder(IRedisDataSourceAdapter dataSourceAdapter) {
        this.dataSourceAdapter = dataSourceAdapter;
    }

    @Override
    public IRedis getOwner() {
        return dataSourceAdapter.getOwner();
    }

    @Override
    public IRedisDataSourceConfig getDataSourceConfig() {
        return dataSourceAdapter.getDataSourceConfig();
    }

    @Override
    public IRedisCommander getConnection() {
        IRedisCommander commander = null;
        try {
            commander = dataSourceAdapter.getConnection();
            cacheCommanders.add(commander);
        } catch (Exception e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("An exception occurs when the IRedisCommander was acquired: ", RuntimeUtils.unwrapThrow(e));
            }
        }
        return commander;
    }

    @Override
    public void close() {
        Iterator<IRedisCommander> iterator = cacheCommanders.iterator();
        while (iterator.hasNext()) {
            IRedisCommander commander = iterator.next();
            if (commander != null && !commander.isClosed() && !IRedis.ConnectionType.CLUSTER.equals(dataSourceAdapter.getDataSourceConfig().getConnectionType())) {
                try {
                    commander.close();
                } catch (IOException e) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("An exception occurs when the IRedisCommander was released: ", RuntimeUtils.unwrapThrow(e));
                    }
                }
            }
            iterator.remove();
        }
    }

    @Override
    public IRedisDataSourceAdapter getDataSourceAdapter() {
        return dataSourceAdapter;
    }
}
