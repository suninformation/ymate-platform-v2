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
import redis.clients.jedis.commands.*;

import java.io.Closeable;
import java.util.List;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-05-23 02:48
 * @since 2.1.0
 */
@Ignored
public interface IRedisCommander extends BinaryJedisClusterCommands, MultiKeyBinaryJedisClusterCommands, JedisClusterBinaryScriptingCommands, MultiKeyJedisClusterCommands, JedisClusterScriptingCommands,
        BinaryJedisCommands, MultiKeyBinaryCommands, AdvancedBinaryJedisCommands, BinaryScriptingCommands, JedisCommands, MultiKeyCommands, AdvancedJedisCommands, ScriptingCommands, BasicCommands, ClusterCommands, SentinelCommands, ModuleCommands, Closeable {

    /**
     * 判断当前是否为集群模式
     *
     * @return 若是则返回true
     */
    boolean isCluster();

    /**
     * 判断当前是否已被关闭
     *
     * @return 若是则返回true
     */
    boolean isClosed();

    @Override
    @Deprecated
    default String restore(byte[] key, int ttl, byte[] serializedValue) {
        return restore(key, (long) ttl, serializedValue);
    }

    @Override
    @Deprecated
    default String setex(byte[] key, int seconds, byte[] value) {
        return setex(key, (long) seconds, value);
    }

    @Override
    @Deprecated
    default List<byte[]> xrange(byte[] key, byte[] start, byte[] end, long count) {
        return xrange(key, start, end, (int) Math.min(count, (long) Integer.MAX_VALUE));
    }

    @Override
    @Deprecated
    default Long expire(byte[] key, int seconds) {
        return expire(key, (long) seconds);
    }
}
