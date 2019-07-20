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
package net.ymate.platform.persistence.redis.impl;

import net.ymate.platform.core.persistence.AbstractSession;
import net.ymate.platform.persistence.redis.IRedis;
import net.ymate.platform.persistence.redis.IRedisCommandHolder;
import net.ymate.platform.persistence.redis.IRedisSession;
import org.apache.commons.lang.NullArgumentException;
import redis.clients.jedis.commands.JedisCommands;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/30 下午11:54
 */
public class RedisSession extends AbstractSession<IRedisCommandHolder> implements IRedisSession {

    private final IRedis owner;

    private final IRedisCommandHolder commandHolder;

    public <T extends JedisCommands> RedisSession(IRedis owner, IRedisCommandHolder commandHolder) {
        if (owner == null) {
            throw new NullArgumentException("owner");
        }
        if (commandHolder == null) {
            throw new NullArgumentException("commandHolder");
        }
        this.owner = owner;
        this.commandHolder = commandHolder;
    }

    public IRedis getOwner() {
        return owner;
    }

    @Override
    public IRedisCommandHolder getConnectionHolder() {
        return commandHolder;
    }

    @Override
    public void close() throws Exception {
        commandHolder.close();
    }
}
