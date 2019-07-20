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
package net.ymate.platform.persistence.redis.support;

import net.ymate.platform.persistence.redis.IRedisCommander;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-05-23 00:24
 * @since 2.1.0
 */
public final class JedisCommandsWrapper {

    public static IRedisCommander bind(JedisCluster jedisCluster) {
        return new JedisClusterCommander(jedisCluster);
    }

    public static IRedisCommander bind(Jedis jedis) {
        return new JedisCommander(jedis);
    }
}
