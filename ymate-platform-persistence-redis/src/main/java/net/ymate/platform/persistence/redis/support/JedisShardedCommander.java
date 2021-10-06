/*
 * Copyright 2007-2021 the original author or authors.
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
import org.apache.commons.lang.NullArgumentException;
import redis.clients.jedis.*;
import redis.clients.jedis.args.*;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.jedis.params.*;
import redis.clients.jedis.resps.KeyedListElement;
import redis.clients.jedis.resps.KeyedZSetElement;
import redis.clients.jedis.resps.LCSMatchResult;
import redis.clients.jedis.util.Slowlog;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author 刘镇 (suninformation@163.com) on 2021-07-23 16:22
 * @since 2.1.0
 */
public class JedisShardedCommander implements IRedisCommander {

    private final ShardedJedis shardedJedis;

    private volatile boolean isClosed;

    JedisShardedCommander(ShardedJedis shardedJedis) {
        if (shardedJedis == null) {
            throw new NullArgumentException("shardedJedis");
        }
        this.shardedJedis = shardedJedis;
    }

    @Override
    public void close() {
        if (!isClosed) {
            shardedJedis.close();
            isClosed = true;
        }
    }

    @Override
    public List<Object> roleBinary() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<byte[]> configGet(byte[] pattern) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] configSet(byte[] parameter, byte[] value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String configSetBinary(byte[] parameter, byte[] value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Object> slowlogGetBinary() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Object> slowlogGetBinary(long entries) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long objectRefcount(byte[] key) {
        return shardedJedis.objectRefcount(key);
    }

    @Override
    public byte[] objectEncoding(byte[] key) {
        return shardedJedis.objectEncoding(key);
    }

    @Override
    public Long objectIdletime(byte[] key) {
        return shardedJedis.objectIdletime(key);
    }

    @Override
    public List<byte[]> objectHelpBinary() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long objectFreq(byte[] key) {
        return shardedJedis.objectFreq(key);
    }

    @Override
    public String migrate(String host, int port, byte[] key, int destinationDB, int timeout) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String migrate(String host, int port, int destinationDB, int timeout, MigrateParams params, byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String clientKill(byte[] ipPort) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] clientGetnameBinary() {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] clientListBinary() {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] clientListBinary(ClientType type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] clientListBinary(long... clientIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] clientInfoBinary() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String clientSetname(byte[] name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long clientId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String clientPause(long timeout) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String clientPause(long timeout, ClientPauseMode mode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] memoryDoctorBinary() {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] aclWhoAmIBinary() {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] aclGenPassBinary() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<byte[]> aclListBinary() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<byte[]> aclUsersBinary() {
        throw new UnsupportedOperationException();
    }

    @Override
    public AccessControlUser aclGetUser(byte[] name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String aclSetUser(byte[] name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String aclSetUser(byte[] name, byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long aclDelUser(byte[] name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<byte[]> aclCatBinary() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<byte[]> aclCat(byte[] category) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<byte[]> aclLogBinary() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<byte[]> aclLogBinary(int limit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] aclLog(byte[] options) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String aclLoad() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String aclSave() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Object> role() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> configGet(String pattern) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String configSet(String parameter, String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String slowlogReset() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long slowlogLen() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Slowlog> slowlogGet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Slowlog> slowlogGet(long entries) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long objectRefcount(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String objectEncoding(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long objectIdletime(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> objectHelp() {
        return shardedJedis.objectHelp();
    }

    @Override
    public Long objectFreq(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String migrate(String host, int port, String key, int destinationDB, int timeout) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String migrate(String host, int port, int destinationDB, int timeout, MigrateParams params, String... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String clientKill(String ipPort) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String clientKill(String ip, int port) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long clientKill(ClientKillParams params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long clientUnblock(long clientId, UnblockType unblockType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String clientGetname() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String clientList() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String clientList(ClientType type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String clientList(long... clientIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String clientInfo() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String clientSetname(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String memoryDoctor() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long memoryUsage(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long memoryUsage(String key, int samples) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String aclWhoAmI() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String aclGenPass() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> aclList() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> aclUsers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public AccessControlUser aclGetUser(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String aclSetUser(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String aclSetUser(String name, String... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long aclDelUser(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> aclCat() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> aclCat(String category) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<AccessControlLogEntry> aclLog() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<AccessControlLogEntry> aclLog(int limit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String aclLog(String options) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String ping() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String quit() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String flushDB() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String flushDB(FlushMode flushMode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long dbSize() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String select(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String swapDB(int index1, int index2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String flushAll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String flushAll(FlushMode flushMode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String auth(String password) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String auth(String user, String password) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String save() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String bgsave() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String bgrewriteaof() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long lastsave() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String shutdown() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void shutdown(SaveMode saveMode) throws JedisException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String info() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String info(String section) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String slaveof(String host, int port) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String slaveofNoOne() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getDB() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String debug(DebugParams params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String configResetStat() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String configRewrite() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long waitReplicas(int replicas, long timeout) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String set(byte[] key, byte[] value) {
        return shardedJedis.set(key, value);
    }

    @Override
    public String set(byte[] key, byte[] value, SetParams params) {
        return shardedJedis.set(key, value, params);
    }

    @Override
    public byte[] get(byte[] key) {
        return shardedJedis.get(key);
    }

    @Override
    public byte[] getDel(byte[] key) {
        return shardedJedis.getDel(key);
    }

    @Override
    public byte[] getEx(byte[] key, GetExParams params) {
        return shardedJedis.getEx(key, params);
    }

    @Override
    public Boolean exists(byte[] key) {
        return shardedJedis.exists(key);
    }

    @Override
    public Long persist(byte[] key) {
        return shardedJedis.persist(key);
    }

    @Override
    public String type(byte[] key) {
        return shardedJedis.type(key);
    }

    @Override
    public byte[] dump(byte[] key) {
        return shardedJedis.dump(key);
    }

    @Override
    public String restore(byte[] key, long ttl, byte[] serializedValue) {
        return shardedJedis.restore(key, ttl, serializedValue);
    }

    @Override
    public String restore(byte[] key, long ttl, byte[] serializedValue, RestoreParams params) {
        return shardedJedis.restore(key, ttl, serializedValue, params);
    }

    @Override
    public String restoreReplace(byte[] key, long ttl, byte[] serializedValue) {
        return shardedJedis.restoreReplace(key, ttl, serializedValue);
    }

    @Override
    public Long expire(byte[] key, long seconds) {
        return shardedJedis.expire(key, seconds);
    }

    @Override
    public Long pexpire(byte[] key, long milliseconds) {
        return shardedJedis.pexpire(key, milliseconds);
    }

    @Override
    public Long expireAt(byte[] key, long unixTime) {
        return shardedJedis.expireAt(key, unixTime);
    }

    @Override
    public Long pexpireAt(byte[] key, long millisecondsTimestamp) {
        return shardedJedis.pexpireAt(key, millisecondsTimestamp);
    }

    @Override
    public Long ttl(byte[] key) {
        return shardedJedis.ttl(key);
    }

    @Override
    public Long pttl(byte[] key) {
        return shardedJedis.pttl(key);
    }

    @Override
    public Long touch(byte[] key) {
        return shardedJedis.touch(key);
    }

    @Override
    public Boolean setbit(byte[] key, long offset, boolean value) {
        return shardedJedis.setbit(key, offset, value);
    }

    @Override
    public Boolean setbit(byte[] key, long offset, byte[] value) {
        return shardedJedis.setbit(key, offset, value);
    }

    @Override
    public Boolean getbit(byte[] key, long offset) {
        return shardedJedis.getbit(key, offset);
    }

    @Override
    public Long setrange(byte[] key, long offset, byte[] value) {
        return shardedJedis.setrange(key, offset, value);
    }

    @Override
    public byte[] getrange(byte[] key, long startOffset, long endOffset) {
        return shardedJedis.getrange(key, startOffset, endOffset);
    }

    @Override
    public byte[] getSet(byte[] key, byte[] value) {
        return shardedJedis.getSet(key, value);
    }

    @Override
    public Long setnx(byte[] key, byte[] value) {
        return shardedJedis.setnx(key, value);
    }

    @Override
    public String setex(byte[] key, long seconds, byte[] value) {
        return shardedJedis.setex(key, seconds, value);
    }

    @Override
    public String psetex(byte[] key, long milliseconds, byte[] value) {
        return shardedJedis.psetex(key, milliseconds, value);
    }

    @Override
    public Long decrBy(byte[] key, long decrement) {
        return shardedJedis.decrBy(key, decrement);
    }

    @Override
    public Long decr(byte[] key) {
        return shardedJedis.decr(key);
    }

    @Override
    public Long incrBy(byte[] key, long increment) {
        return shardedJedis.incrBy(key, increment);
    }

    @Override
    public Double incrByFloat(byte[] key, double increment) {
        return shardedJedis.incrByFloat(key, increment);
    }

    @Override
    public Long incr(byte[] key) {
        return shardedJedis.incr(key);
    }

    @Override
    public Long append(byte[] key, byte[] value) {
        return shardedJedis.append(key, value);
    }

    @Override
    public byte[] substr(byte[] key, int start, int end) {
        return shardedJedis.substr(key, start, end);
    }

    @Override
    public Long hset(byte[] key, byte[] field, byte[] value) {
        return shardedJedis.hset(key, field, value);
    }

    @Override
    public Long hset(byte[] key, Map<byte[], byte[]> hash) {
        return shardedJedis.hset(key, hash);
    }

    @Override
    public byte[] hget(byte[] key, byte[] field) {
        return shardedJedis.hget(key, field);
    }

    @Override
    public Long hsetnx(byte[] key, byte[] field, byte[] value) {
        return shardedJedis.hsetnx(key, field, value);
    }

    @Override
    public String hmset(byte[] key, Map<byte[], byte[]> hash) {
        return shardedJedis.hmset(key, hash);
    }

    @Override
    public List<byte[]> hmget(byte[] key, byte[]... fields) {
        return shardedJedis.hmget(key, fields);
    }

    @Override
    public Long hincrBy(byte[] key, byte[] field, long value) {
        return shardedJedis.hincrBy(key, field, value);
    }

    @Override
    public Double hincrByFloat(byte[] key, byte[] field, double value) {
        return shardedJedis.hincrByFloat(key, field, value);
    }

    @Override
    public Boolean hexists(byte[] key, byte[] field) {
        return shardedJedis.hexists(key, field);
    }

    @Override
    public Long hdel(byte[] key, byte[]... field) {
        return shardedJedis.hdel(key, field);
    }

    @Override
    public Long hlen(byte[] key) {
        return shardedJedis.hlen(key);
    }

    @Override
    public Set<byte[]> hkeys(byte[] key) {
        return shardedJedis.hkeys(key);
    }

    @Override
    public List<byte[]> hvals(byte[] key) {
        return shardedJedis.hvals(key);
    }

    @Override
    public Map<byte[], byte[]> hgetAll(byte[] key) {
        return shardedJedis.hgetAll(key);
    }

    @Override
    public byte[] hrandfield(byte[] key) {
        return shardedJedis.hrandfield(key);
    }

    @Override
    public List<byte[]> hrandfield(byte[] key, long count) {
        return shardedJedis.hrandfield(key, count);
    }

    @Override
    public Map<byte[], byte[]> hrandfieldWithValues(byte[] key, long count) {
        return shardedJedis.hrandfieldWithValues(key, count);
    }

    @Override
    public Long rpush(byte[] key, byte[]... args) {
        return shardedJedis.rpush(key, args);
    }

    @Override
    public Long lpush(byte[] key, byte[]... args) {
        return shardedJedis.lpush(key, args);
    }

    @Override
    public Long llen(byte[] key) {
        return shardedJedis.llen(key);
    }

    @Override
    public List<byte[]> lrange(byte[] key, long start, long stop) {
        return shardedJedis.lrange(key, start, stop);
    }

    @Override
    public String ltrim(byte[] key, long start, long stop) {
        return shardedJedis.ltrim(key, start, stop);
    }

    @Override
    public byte[] lindex(byte[] key, long index) {
        return shardedJedis.lindex(key, index);
    }

    @Override
    public String lset(byte[] key, long index, byte[] value) {
        return shardedJedis.lset(key, index, value);
    }

    @Override
    public Long lrem(byte[] key, long count, byte[] value) {
        return shardedJedis.lrem(key, count, value);
    }

    @Override
    public byte[] lpop(byte[] key) {
        return shardedJedis.lpop(key);
    }

    @Override
    public List<byte[]> lpop(byte[] key, int count) {
        return shardedJedis.lpop(key, count);
    }

    @Override
    public Long lpos(byte[] key, byte[] element) {
        return shardedJedis.lpos(key, element);
    }

    @Override
    public Long lpos(byte[] key, byte[] element, LPosParams params) {
        return shardedJedis.lpos(key, element, params);
    }

    @Override
    public List<Long> lpos(byte[] key, byte[] element, LPosParams params, long count) {
        return shardedJedis.lpos(key, element, params, count);
    }

    @Override
    public byte[] rpop(byte[] key) {
        return shardedJedis.rpop(key);
    }

    @Override
    public List<byte[]> rpop(byte[] key, int count) {
        return shardedJedis.rpop(key, count);
    }

    @Override
    public Long sadd(byte[] key, byte[]... member) {
        return shardedJedis.sadd(key, member);
    }

    @Override
    public Set<byte[]> smembers(byte[] key) {
        return shardedJedis.smembers(key);
    }

    @Override
    public Long srem(byte[] key, byte[]... member) {
        return shardedJedis.srem(key, member);
    }

    @Override
    public byte[] spop(byte[] key) {
        return shardedJedis.spop(key);
    }

    @Override
    public Set<byte[]> spop(byte[] key, long count) {
        return shardedJedis.spop(key, count);
    }

    @Override
    public Long scard(byte[] key) {
        return shardedJedis.scard(key);
    }

    @Override
    public Boolean sismember(byte[] key, byte[] member) {
        return shardedJedis.sismember(key, member);
    }

    @Override
    public List<Boolean> smismember(byte[] key, byte[]... members) {
        return shardedJedis.smismember(key, members);
    }

    @Override
    public byte[] srandmember(byte[] key) {
        return shardedJedis.srandmember(key);
    }

    @Override
    public List<byte[]> srandmember(byte[] key, int count) {
        return shardedJedis.srandmember(key, count);
    }

    @Override
    public Long strlen(byte[] key) {
        return shardedJedis.strlen(key);
    }

    @Override
    public Long zadd(byte[] key, double score, byte[] member) {
        return shardedJedis.zadd(key, score, member);
    }

    @Override
    public Long zadd(byte[] key, double score, byte[] member, ZAddParams params) {
        return shardedJedis.zadd(key, score, member, params);
    }

    @Override
    public Long zadd(byte[] key, Map<byte[], Double> scoreMembers) {
        return shardedJedis.zadd(key, scoreMembers);
    }

    @Override
    public Long zadd(byte[] key, Map<byte[], Double> scoreMembers, ZAddParams params) {
        return shardedJedis.zadd(key, scoreMembers, params);
    }

    @Override
    public Double zaddIncr(byte[] key, double score, byte[] member, ZAddParams params) {
        return shardedJedis.zaddIncr(key, score, member, params);
    }

    @Override
    public Set<byte[]> zrange(byte[] key, long start, long stop) {
        return shardedJedis.zrange(key, start, stop);
    }

    @Override
    public Long zrem(byte[] key, byte[]... members) {
        return shardedJedis.zrem(key, members);
    }

    @Override
    public Double zincrby(byte[] key, double increment, byte[] member) {
        return shardedJedis.zincrby(key, increment, member);
    }

    @Override
    public Double zincrby(byte[] key, double increment, byte[] member, ZIncrByParams params) {
        return shardedJedis.zincrby(key, increment, member, params);
    }

    @Override
    public Long zrank(byte[] key, byte[] member) {
        return shardedJedis.zrank(key, member);
    }

    @Override
    public Long zrevrank(byte[] key, byte[] member) {
        return shardedJedis.zrevrank(key, member);
    }

    @Override
    public Set<byte[]> zrevrange(byte[] key, long start, long stop) {
        return shardedJedis.zrevrange(key, start, stop);
    }

    @Override
    public Set<Tuple> zrangeWithScores(byte[] key, long start, long stop) {
        return shardedJedis.zrangeWithScores(key, start, stop);
    }

    @Override
    public Set<Tuple> zrevrangeWithScores(byte[] key, long start, long stop) {
        return shardedJedis.zrevrangeWithScores(key, start, stop);
    }

    @Override
    public byte[] zrandmember(byte[] key) {
        return shardedJedis.zrandmember(key);
    }

    @Override
    public Set<byte[]> zrandmember(byte[] key, long count) {
        return shardedJedis.zrandmember(key, count);
    }

    @Override
    public Set<Tuple> zrandmemberWithScores(byte[] key, long count) {
        return shardedJedis.zrandmemberWithScores(key, count);
    }

    @Override
    public Long zcard(byte[] key) {
        return shardedJedis.zcard(key);
    }

    @Override
    public Double zscore(byte[] key, byte[] member) {
        return shardedJedis.zscore(key, member);
    }

    @Override
    public List<Double> zmscore(byte[] key, byte[]... members) {
        return shardedJedis.zmscore(key, members);
    }

    @Override
    public Tuple zpopmax(byte[] key) {
        return shardedJedis.zpopmax(key);
    }

    @Override
    public Set<Tuple> zpopmax(byte[] key, int count) {
        return shardedJedis.zpopmax(key, count);
    }

    @Override
    public Tuple zpopmin(byte[] key) {
        return shardedJedis.zpopmin(key);
    }

    @Override
    public Set<Tuple> zpopmin(byte[] key, int count) {
        return shardedJedis.zpopmin(key, count);
    }

    @Override
    public List<byte[]> sort(byte[] key) {
        return shardedJedis.sort(key);
    }

    @Override
    public List<byte[]> sort(byte[] key, SortingParams sortingParameters) {
        return shardedJedis.sort(key, sortingParameters);
    }

    @Override
    public Long zcount(byte[] key, double min, double max) {
        return shardedJedis.zcount(key, min, max);
    }

    @Override
    public Long zcount(byte[] key, byte[] min, byte[] max) {
        return shardedJedis.zcount(key, min, max);
    }

    @Override
    public Set<byte[]> zrangeByScore(byte[] key, double min, double max) {
        return shardedJedis.zrangeByScore(key, min, max);
    }

    @Override
    public Set<byte[]> zrangeByScore(byte[] key, byte[] min, byte[] max) {
        return shardedJedis.zrangeByScore(key, min, max);
    }

    @Override
    public Set<byte[]> zrevrangeByScore(byte[] key, double max, double min) {
        return shardedJedis.zrevrangeByScore(key, max, min);
    }

    @Override
    public Set<byte[]> zrangeByScore(byte[] key, double min, double max, int offset, int count) {
        return shardedJedis.zrangeByScore(key, min, max, offset, count);
    }

    @Override
    public Set<byte[]> zrevrangeByScore(byte[] key, byte[] max, byte[] min) {
        return shardedJedis.zrevrangeByScore(key, max, min);
    }

    @Override
    public Set<byte[]> zrangeByScore(byte[] key, byte[] min, byte[] max, int offset, int count) {
        return shardedJedis.zrangeByScore(key, min, max, offset, count);
    }

    @Override
    public Set<byte[]> zrevrangeByScore(byte[] key, double max, double min, int offset, int count) {
        return shardedJedis.zrevrangeByScore(key, max, min, offset, count);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max) {
        return shardedJedis.zrangeByScoreWithScores(key, min, max);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, double max, double min) {
        return shardedJedis.zrevrangeByScoreWithScores(key, max, min);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max, int offset, int count) {
        return shardedJedis.zrevrangeByScoreWithScores(key, max, min);
    }

    @Override
    public Set<byte[]> zrevrangeByScore(byte[] key, byte[] max, byte[] min, int offset, int count) {
        return shardedJedis.zrevrangeByScore(key, max, min, offset, count);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(byte[] key, byte[] min, byte[] max) {
        return shardedJedis.zrangeByScoreWithScores(key, min, max);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, byte[] max, byte[] min) {
        return shardedJedis.zrevrangeByScoreWithScores(key, max, min);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(byte[] key, byte[] min, byte[] max, int offset, int count) {
        return shardedJedis.zrangeByScoreWithScores(key, min, max, offset, count);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, double max, double min, int offset, int count) {
        return shardedJedis.zrevrangeByScoreWithScores(key, max, min, offset, count);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, byte[] max, byte[] min, int offset, int count) {
        return shardedJedis.zrevrangeByScoreWithScores(key, max, min, offset, count);
    }

    @Override
    public Long zremrangeByRank(byte[] key, long start, long stop) {
        return shardedJedis.zremrangeByRank(key, start, stop);
    }

    @Override
    public Long zremrangeByScore(byte[] key, double min, double max) {
        return shardedJedis.zremrangeByScore(key, min, max);
    }

    @Override
    public Long zremrangeByScore(byte[] key, byte[] min, byte[] max) {
        return shardedJedis.zremrangeByScore(key, min, max);
    }

    @Override
    public Long zlexcount(byte[] key, byte[] min, byte[] max) {
        return shardedJedis.zlexcount(key, min, max);
    }

    @Override
    public Set<byte[]> zrangeByLex(byte[] key, byte[] min, byte[] max) {
        return shardedJedis.zrangeByLex(key, min, max);
    }

    @Override
    public Set<byte[]> zrangeByLex(byte[] key, byte[] min, byte[] max, int offset, int count) {
        return shardedJedis.zrangeByLex(key, min, max, offset, count);
    }

    @Override
    public Set<byte[]> zrevrangeByLex(byte[] key, byte[] max, byte[] min) {
        return shardedJedis.zrevrangeByLex(key, max, min);
    }

    @Override
    public Set<byte[]> zrevrangeByLex(byte[] key, byte[] max, byte[] min, int offset, int count) {
        return shardedJedis.zrevrangeByLex(key, max, min, offset, count);
    }

    @Override
    public Long zremrangeByLex(byte[] key, byte[] min, byte[] max) {
        return shardedJedis.zremrangeByLex(key, min, max);
    }

    @Override
    public Long linsert(byte[] key, ListPosition where, byte[] pivot, byte[] value) {
        return shardedJedis.linsert(key, where, pivot, value);
    }

    @Override
    public Long lpushx(byte[] key, byte[]... arg) {
        return shardedJedis.lpushx(key, arg);
    }

    @Override
    public Long rpushx(byte[] key, byte[]... arg) {
        return shardedJedis.rpushx(key, arg);
    }

    @Override
    public Long del(byte[] key) {
        return shardedJedis.del(key);
    }

    @Override
    public Long unlink(byte[] key) {
        return shardedJedis.unlink(key);
    }

    @Override
    public byte[] echo(byte[] arg) {
        return shardedJedis.echo(arg);
    }

    @Override
    public Long move(byte[] key, int dbIndex) {
        return shardedJedis.move(key, dbIndex);
    }

    @Override
    public Long bitcount(byte[] key) {
        return shardedJedis.bitcount(key);
    }

    @Override
    public Long bitcount(byte[] key, long start, long end) {
        return shardedJedis.bitcount(key, start, end);
    }

    @Override
    public Long pfadd(byte[] key, byte[]... elements) {
        return shardedJedis.pfadd(key, elements);
    }

    @Override
    public long pfcount(byte[] key) {
        return shardedJedis.pfcount(key);
    }

    @Override
    public Long geoadd(byte[] key, double longitude, double latitude, byte[] member) {
        return shardedJedis.geoadd(key, longitude, latitude, member);
    }

    @Override
    public Long geoadd(byte[] key, Map<byte[], GeoCoordinate> memberCoordinateMap) {
        return shardedJedis.geoadd(key, memberCoordinateMap);
    }

    @Override
    public Long geoadd(byte[] key, GeoAddParams params, Map<byte[], GeoCoordinate> memberCoordinateMap) {
        return shardedJedis.geoadd(key, params, memberCoordinateMap);
    }

    @Override
    public Double geodist(byte[] key, byte[] member1, byte[] member2) {
        return shardedJedis.geodist(key, member1, member2);
    }

    @Override
    public Double geodist(byte[] key, byte[] member1, byte[] member2, GeoUnit unit) {
        return shardedJedis.geodist(key, member1, member2, unit);
    }

    @Override
    public List<byte[]> geohash(byte[] key, byte[]... members) {
        return shardedJedis.geohash(key, members);
    }

    @Override
    public List<GeoCoordinate> geopos(byte[] key, byte[]... members) {
        return shardedJedis.geopos(key, members);
    }

    @Override
    public List<GeoRadiusResponse> georadius(byte[] key, double longitude, double latitude, double radius, GeoUnit unit) {
        return shardedJedis.georadius(key, longitude, latitude, radius, unit);
    }

    @Override
    public List<GeoRadiusResponse> georadiusReadonly(byte[] key, double longitude, double latitude, double radius, GeoUnit unit) {
        return shardedJedis.georadiusReadonly(key, longitude, latitude, radius, unit);
    }

    @Override
    public List<GeoRadiusResponse> georadius(byte[] key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param) {
        return shardedJedis.georadius(key, longitude, latitude, radius, unit, param);
    }

    @Override
    public List<GeoRadiusResponse> georadiusReadonly(byte[] key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param) {
        return shardedJedis.georadiusReadonly(key, longitude, latitude, radius, unit, param);
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMember(byte[] key, byte[] member, double radius, GeoUnit unit) {
        return shardedJedis.georadiusByMember(key, member, radius, unit);
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMemberReadonly(byte[] key, byte[] member, double radius, GeoUnit unit) {
        return shardedJedis.georadiusByMemberReadonly(key, member, radius, unit);
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMember(byte[] key, byte[] member, double radius, GeoUnit unit, GeoRadiusParam param) {
        return shardedJedis.georadiusByMember(key, member, radius, unit, param);
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMemberReadonly(byte[] key, byte[] member, double radius, GeoUnit unit, GeoRadiusParam param) {
        return shardedJedis.georadiusByMemberReadonly(key, member, radius, unit, param);
    }

    @Override
    public ScanResult<Map.Entry<byte[], byte[]>> hscan(byte[] key, byte[] cursor) {
        return shardedJedis.hscan(key, cursor);
    }

    @Override
    public ScanResult<Map.Entry<byte[], byte[]>> hscan(byte[] key, byte[] cursor, ScanParams params) {
        return shardedJedis.hscan(key, cursor, params);
    }

    @Override
    public ScanResult<byte[]> sscan(byte[] key, byte[] cursor) {
        return shardedJedis.sscan(key, cursor);
    }

    @Override
    public ScanResult<byte[]> sscan(byte[] key, byte[] cursor, ScanParams params) {
        return shardedJedis.sscan(key, cursor, params);
    }

    @Override
    public ScanResult<Tuple> zscan(byte[] key, byte[] cursor) {
        return shardedJedis.zscan(key, cursor);
    }

    @Override
    public ScanResult<Tuple> zscan(byte[] key, byte[] cursor, ScanParams params) {
        return shardedJedis.zscan(key, cursor, params);
    }

    @Override
    public List<Long> bitfield(byte[] key, byte[]... arguments) {
        return shardedJedis.bitfield(key, arguments);
    }

    @Override
    public List<Long> bitfieldReadonly(byte[] key, byte[]... arguments) {
        return shardedJedis.bitfieldReadonly(key, arguments);
    }

    @Override
    public Long hstrlen(byte[] key, byte[] field) {
        return shardedJedis.hstrlen(key, field);
    }

    @Override
    public byte[] xadd(byte[] key, byte[] id, Map<byte[], byte[]> hash, long maxLen, boolean approximateLength) {
        return shardedJedis.xadd(key, id, hash, maxLen, approximateLength);
    }

    @Override
    public byte[] xadd(byte[] key, Map<byte[], byte[]> hash, XAddParams params) {
        return shardedJedis.xadd(key, hash, params);
    }

    @Override
    public Long xlen(byte[] key) {
        return shardedJedis.xlen(key);
    }

    @Override
    public List<byte[]> xrange(byte[] key, byte[] start, byte[] end) {
        return shardedJedis.xrange(key, start, end);
    }

    @Override
    public List<byte[]> xrange(byte[] key, byte[] start, byte[] end, long count) {
        return shardedJedis.xrange(key, start, end, count);
    }

    @Override
    public List<byte[]> xrange(byte[] key, byte[] start, byte[] end, int count) {
        return shardedJedis.xrange(key, start, end, count);
    }

    @Override
    public List<byte[]> xrevrange(byte[] key, byte[] end, byte[] start) {
        return shardedJedis.xrevrange(key, end, start);
    }

    @Override
    public List<byte[]> xrevrange(byte[] key, byte[] end, byte[] start, int count) {
        return shardedJedis.xrevrange(key, end, start, count);
    }

    @Override
    public Long xack(byte[] key, byte[] group, byte[]... ids) {
        return shardedJedis.xack(key, group, ids);
    }

    @Override
    public String xgroupCreate(byte[] key, byte[] consumer, byte[] id, boolean makeStream) {
        return shardedJedis.xgroupCreate(key, consumer, id, makeStream);
    }

    @Override
    public String xgroupSetID(byte[] key, byte[] consumer, byte[] id) {
        return shardedJedis.xgroupSetID(key, consumer, id);
    }

    @Override
    public Long xgroupDestroy(byte[] key, byte[] consumer) {
        return shardedJedis.xgroupDestroy(key, consumer);
    }

    @Override
    public Long xgroupDelConsumer(byte[] key, byte[] consumer, byte[] consumerName) {
        return shardedJedis.xgroupDelConsumer(key, consumer, consumerName);
    }

    @Override
    public Long xdel(byte[] key, byte[]... ids) {
        return shardedJedis.xdel(key, ids);
    }

    @Override
    public Long xtrim(byte[] key, long maxLen, boolean approximateLength) {
        return shardedJedis.xtrim(key, maxLen, approximateLength);
    }

    @Override
    public Long xtrim(byte[] key, XTrimParams params) {
        return shardedJedis.xtrim(key, params);
    }

    @Override
    public Object xpending(byte[] key, byte[] groupname) {
        return shardedJedis.xpending(key, groupname);
    }

    @Override
    public List<Object> xpending(byte[] key, byte[] groupname, byte[] start, byte[] end, int count, byte[] consumername) {
        return shardedJedis.xpending(key, groupname, start, end, count, consumername);
    }

    @Override
    public List<Object> xpending(byte[] key, byte[] groupname, XPendingParams params) {
        return shardedJedis.xpending(key, groupname, params);
    }

    @Override
    public List<byte[]> xclaim(byte[] key, byte[] groupname, byte[] consumername, long minIdleTime, long newIdleTime, int retries, boolean force, byte[][] ids) {
        return shardedJedis.xclaim(key, groupname, consumername, minIdleTime, newIdleTime, retries, force, ids);
    }

    @Override
    public List<byte[]> xclaim(byte[] key, byte[] group, byte[] consumername, long minIdleTime, XClaimParams params, byte[]... ids) {
        return shardedJedis.xclaim(key, group, consumername, minIdleTime, params, ids);
    }

    @Override
    public List<byte[]> xclaimJustId(byte[] key, byte[] group, byte[] consumername, long minIdleTime, XClaimParams params, byte[]... ids) {
        return shardedJedis.xclaimJustId(key, group, consumername, minIdleTime, params, ids);
    }

    @Override
    public List<Object> xautoclaim(byte[] key, byte[] groupName, byte[] consumerName, long minIdleTime, byte[] start, XAutoClaimParams params) {
        return shardedJedis.xautoclaim(key, groupName, consumerName, minIdleTime, start, params);
    }

    @Override
    public List<Object> xautoclaimJustId(byte[] key, byte[] groupName, byte[] consumerName, long minIdleTime, byte[] start, XAutoClaimParams params) {
        return shardedJedis.xautoclaimJustId(key, groupName, consumerName, minIdleTime, start, params);
    }

    @Override
    public StreamInfo xinfoStream(byte[] key) {
        return shardedJedis.xinfoStream(key);
    }

    @Override
    public Object xinfoStreamBinary(byte[] key) {
        return shardedJedis.xinfoStreamBinary(key);
    }

    @Override
    public List<StreamGroupInfo> xinfoGroup(byte[] key) {
        return shardedJedis.xinfoGroup(key);
    }

    @Override
    public List<Object> xinfoGroupBinary(byte[] key) {
        return shardedJedis.xinfoGroupBinary(key);
    }

    @Override
    public List<StreamConsumersInfo> xinfoConsumers(byte[] key, byte[] group) {
        return shardedJedis.xinfoConsumers(key, group);
    }

    @Override
    public List<Object> xinfoConsumersBinary(byte[] key, byte[] group) {
        return shardedJedis.xinfoConsumersBinary(key, group);
    }

    @Override
    public Long waitReplicas(byte[] key, int replicas, long timeout) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long memoryUsage(byte[] key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long memoryUsage(byte[] key, int samples) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String failover() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String failover(FailoverParams failoverParams) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String failoverAbort() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object eval(byte[] script) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object evalsha(byte[] sha1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Long> scriptExists(byte[]... sha1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] scriptLoad(byte[] script) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String scriptFlush() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String scriptFlush(FlushMode flushMode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String scriptKill() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String clusterNodes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String clusterReplicas(String nodeId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String clusterMeet(String ip, int port) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String clusterAddSlots(int... slots) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String clusterDelSlots(int... slots) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String clusterInfo() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> clusterGetKeysInSlot(int slot, int count) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String clusterSetSlotNode(int slot, String nodeId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String clusterSetSlotMigrating(int slot, String nodeId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String clusterSetSlotImporting(int slot, String nodeId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String clusterSetSlotStable(int slot) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String clusterForget(String nodeId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String clusterFlushSlots() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long clusterKeySlot(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long clusterCountKeysInSlot(int slot) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String clusterSaveConfig() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String clusterReplicate(String nodeId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> clusterSlaves(String nodeId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String clusterFailover() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String clusterFailover(ClusterFailoverOption failoverOption) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Object> clusterSlots() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String clusterReset(ClusterReset resetType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String clusterReset(ClusterResetType resetType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String clusterMyId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String readonly() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String readwrite() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object eval(byte[] script, byte[] keyCount, byte[]... params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object eval(byte[] script, int keyCount, byte[]... params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object eval(byte[] script, List<byte[]> keys, List<byte[]> args) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object eval(byte[] script, byte[] sampleKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object evalsha(byte[] sha1, byte[] sampleKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object evalsha(byte[] sha1, List<byte[]> keys, List<byte[]> args) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object evalsha(byte[] sha1, int keyCount, byte[]... params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Long> scriptExists(byte[] sampleKey, byte[]... sha1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] scriptLoad(byte[] script, byte[] sampleKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String scriptFlush(byte[] sampleKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String scriptFlush(byte[] sampleKey, FlushMode flushMode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String scriptKill(byte[] sampleKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String set(String key, String value) {
        return shardedJedis.set(key, value);
    }

    @Override
    public String set(String key, String value, SetParams params) {
        return shardedJedis.set(key, value, params);
    }

    @Override
    public String get(String key) {
        return shardedJedis.get(key);
    }

    @Override
    public String getDel(String key) {
        return shardedJedis.getDel(key);
    }

    @Override
    public String getEx(String key, GetExParams params) {
        return shardedJedis.getEx(key, params);
    }

    @Override
    public Boolean exists(String key) {
        return shardedJedis.exists(key);
    }

    @Override
    public Long persist(String key) {
        return shardedJedis.persist(key);
    }

    @Override
    public String type(String key) {
        return shardedJedis.type(key);
    }

    @Override
    public byte[] dump(String key) {
        return shardedJedis.dump(key);
    }

    @Override
    public String restore(String key, int ttl, byte[] serializedValue) {
        return shardedJedis.restore(key, ttl, serializedValue);
    }

    @Override
    public String restore(String key, long ttl, byte[] serializedValue) {
        return shardedJedis.restore(key, ttl, serializedValue);
    }

    @Override
    public String restoreReplace(String key, long ttl, byte[] serializedValue) {
        return shardedJedis.restoreReplace(key, ttl, serializedValue);
    }

    @Override
    public String restore(String key, long ttl, byte[] serializedValue, RestoreParams params) {
        return shardedJedis.restore(key, ttl, serializedValue, params);
    }

    @Override
    public Long expire(String key, long seconds) {
        return shardedJedis.expire(key, seconds);
    }

    @Override
    public Long pexpire(String key, long milliseconds) {
        return shardedJedis.pexpire(key, milliseconds);
    }

    @Override
    public Long expireAt(String key, long unixTime) {
        return shardedJedis.expireAt(key, unixTime);
    }

    @Override
    public Long pexpireAt(String key, long millisecondsTimestamp) {
        return shardedJedis.pexpireAt(key, millisecondsTimestamp);
    }

    @Override
    public Long ttl(String key) {
        return shardedJedis.ttl(key);
    }

    @Override
    public Long pttl(String key) {
        return shardedJedis.pttl(key);
    }

    @Override
    public Long touch(String key) {
        return shardedJedis.touch(key);
    }

    @Override
    public Boolean setbit(String key, long offset, boolean value) {
        return shardedJedis.setbit(key, offset, value);
    }

    @Override
    public Boolean setbit(String key, long offset, String value) {
        return shardedJedis.setbit(key, offset, value);
    }

    @Override
    public Boolean getbit(String key, long offset) {
        return shardedJedis.getbit(key, offset);
    }

    @Override
    public Long setrange(String key, long offset, String value) {
        return shardedJedis.setrange(key, offset, value);
    }

    @Override
    public String getrange(String key, long startOffset, long endOffset) {
        return shardedJedis.getrange(key, startOffset, endOffset);
    }

    @Override
    public String getSet(String key, String value) {
        return shardedJedis.getSet(key, value);
    }

    @Override
    public Long setnx(String key, String value) {
        return shardedJedis.setnx(key, value);
    }

    @Override
    public String setex(String key, long seconds, String value) {
        return shardedJedis.setex(key, seconds, value);
    }

    @Override
    public String psetex(String key, long milliseconds, String value) {
        return shardedJedis.psetex(key, milliseconds, value);
    }

    @Override
    public Long decrBy(String key, long decrement) {
        return shardedJedis.decrBy(key, decrement);
    }

    @Override
    public Long decr(String key) {
        return shardedJedis.decr(key);
    }

    @Override
    public Long incrBy(String key, long increment) {
        return shardedJedis.incrBy(key, increment);
    }

    @Override
    public Double incrByFloat(String key, double increment) {
        return shardedJedis.incrByFloat(key, increment);
    }

    @Override
    public Long incr(String key) {
        return shardedJedis.incr(key);
    }

    @Override
    public Long append(String key, String value) {
        return shardedJedis.append(key, value);
    }

    @Override
    public String substr(String key, int start, int end) {
        return shardedJedis.substr(key, start, end);
    }

    @Override
    public Long hset(String key, String field, String value) {
        return shardedJedis.hset(key, field, value);
    }

    @Override
    public Long hset(String key, Map<String, String> hash) {
        return shardedJedis.hset(key, hash);
    }

    @Override
    public String hget(String key, String field) {
        return shardedJedis.hget(key, field);
    }

    @Override
    public Long hsetnx(String key, String field, String value) {
        return shardedJedis.hsetnx(key, field, value);
    }

    @Override
    public String hmset(String key, Map<String, String> hash) {
        return shardedJedis.hmset(key, hash);
    }

    @Override
    public List<String> hmget(String key, String... fields) {
        return shardedJedis.hmget(key, fields);
    }

    @Override
    public Long hincrBy(String key, String field, long value) {
        return shardedJedis.hincrBy(key, field, value);
    }

    @Override
    public Double hincrByFloat(String key, String field, double value) {
        return shardedJedis.hincrByFloat(key, field, value);
    }

    @Override
    public Boolean hexists(String key, String field) {
        return shardedJedis.hexists(key, field);
    }

    @Override
    public Long hdel(String key, String... field) {
        return shardedJedis.hdel(key, field);
    }

    @Override
    public Long hlen(String key) {
        return shardedJedis.hlen(key);
    }

    @Override
    public Set<String> hkeys(String key) {
        return shardedJedis.hkeys(key);
    }

    @Override
    public List<String> hvals(String key) {
        return shardedJedis.hvals(key);
    }

    @Override
    public Map<String, String> hgetAll(String key) {
        return shardedJedis.hgetAll(key);
    }

    @Override
    public String hrandfield(String key) {
        return shardedJedis.hrandfield(key);
    }

    @Override
    public List<String> hrandfield(String key, long count) {
        return shardedJedis.hrandfield(key, count);
    }

    @Override
    public Map<String, String> hrandfieldWithValues(String key, long count) {
        return shardedJedis.hrandfieldWithValues(key, count);
    }

    @Override
    public Long rpush(String key, String... string) {
        return shardedJedis.rpush(key, string);
    }

    @Override
    public Long lpush(String key, String... string) {
        return shardedJedis.lpush(key, string);
    }

    @Override
    public Long llen(String key) {
        return shardedJedis.llen(key);
    }

    @Override
    public List<String> lrange(String key, long start, long stop) {
        return shardedJedis.lrange(key, start, stop);
    }

    @Override
    public String ltrim(String key, long start, long stop) {
        return shardedJedis.ltrim(key, start, stop);
    }

    @Override
    public String lindex(String key, long index) {
        return shardedJedis.lindex(key, index);
    }

    @Override
    public String lset(String key, long index, String value) {
        return shardedJedis.lset(key, index, value);
    }

    @Override
    public Long lrem(String key, long count, String value) {
        return shardedJedis.lrem(key, count, value);
    }

    @Override
    public String lpop(String key) {
        return shardedJedis.lpop(key);
    }

    @Override
    public List<String> lpop(String key, int count) {
        return shardedJedis.lpop(key, count);
    }

    @Override
    public Long lpos(String key, String element) {
        return shardedJedis.lpos(key, element);
    }

    @Override
    public Long lpos(String key, String element, LPosParams params) {
        return shardedJedis.lpos(key, element, params);
    }

    @Override
    public List<Long> lpos(String key, String element, LPosParams params, long count) {
        return shardedJedis.lpos(key, element, params, count);
    }

    @Override
    public String rpop(String key) {
        return shardedJedis.rpop(key);
    }

    @Override
    public List<String> rpop(String key, int count) {
        return shardedJedis.rpop(key, count);
    }

    @Override
    public Long sadd(String key, String... member) {
        return shardedJedis.sadd(key, member);
    }

    @Override
    public Set<String> smembers(String key) {
        return shardedJedis.smembers(key);
    }

    @Override
    public Long srem(String key, String... member) {
        return shardedJedis.srem(key, member);
    }

    @Override
    public String spop(String key) {
        return shardedJedis.spop(key);
    }

    @Override
    public Set<String> spop(String key, long count) {
        return shardedJedis.spop(key, count);
    }

    @Override
    public Long scard(String key) {
        return shardedJedis.scard(key);
    }

    @Override
    public Boolean sismember(String key, String member) {
        return shardedJedis.sismember(key, member);
    }

    @Override
    public List<Boolean> smismember(String key, String... members) {
        return shardedJedis.smismember(key, members);
    }

    @Override
    public String srandmember(String key) {
        return shardedJedis.srandmember(key);
    }

    @Override
    public List<String> srandmember(String key, int count) {
        return shardedJedis.srandmember(key, count);
    }

    @Override
    public Long strlen(String key) {
        return shardedJedis.strlen(key);
    }

    @Override
    public Long zadd(String key, double score, String member) {
        return shardedJedis.zadd(key, score, member);
    }

    @Override
    public Long zadd(String key, double score, String member, ZAddParams params) {
        return shardedJedis.zadd(key, score, member, params);
    }

    @Override
    public Long zadd(String key, Map<String, Double> scoreMembers) {
        return shardedJedis.zadd(key, scoreMembers);
    }

    @Override
    public Long zadd(String key, Map<String, Double> scoreMembers, ZAddParams params) {
        return shardedJedis.zadd(key, scoreMembers, params);
    }

    @Override
    public Double zaddIncr(String key, double score, String member, ZAddParams params) {
        return shardedJedis.zaddIncr(key, score, member, params);
    }

    @Override
    public Set<String> zrange(String key, long start, long stop) {
        return shardedJedis.zrange(key, start, stop);
    }

    @Override
    public Long zrem(String key, String... members) {
        return shardedJedis.zrem(key, members);
    }

    @Override
    public Double zincrby(String key, double increment, String member) {
        return shardedJedis.zincrby(key, increment, member);
    }

    @Override
    public Double zincrby(String key, double increment, String member, ZIncrByParams params) {
        return shardedJedis.zincrby(key, increment, member, params);
    }

    @Override
    public Long zrank(String key, String member) {
        return shardedJedis.zrank(key, member);
    }

    @Override
    public Long zrevrank(String key, String member) {
        return shardedJedis.zrevrank(key, member);
    }

    @Override
    public Set<String> zrevrange(String key, long start, long stop) {
        return shardedJedis.zrevrange(key, start, stop);
    }

    @Override
    public Set<Tuple> zrangeWithScores(String key, long start, long stop) {
        return shardedJedis.zrangeWithScores(key, start, stop);
    }

    @Override
    public Set<Tuple> zrevrangeWithScores(String key, long start, long stop) {
        return shardedJedis.zrevrangeWithScores(key, start, stop);
    }

    @Override
    public String zrandmember(String key) {
        return shardedJedis.zrandmember(key);
    }

    @Override
    public Set<String> zrandmember(String key, long count) {
        return shardedJedis.zrandmember(key, count);
    }

    @Override
    public Set<Tuple> zrandmemberWithScores(String key, long count) {
        return shardedJedis.zrandmemberWithScores(key, count);
    }

    @Override
    public Long zcard(String key) {
        return shardedJedis.zcard(key);
    }

    @Override
    public Double zscore(String key, String member) {
        return shardedJedis.zscore(key, member);
    }

    @Override
    public List<Double> zmscore(String key, String... members) {
        return shardedJedis.zmscore(key, members);
    }

    @Override
    public Tuple zpopmax(String key) {
        return shardedJedis.zpopmax(key);
    }

    @Override
    public Set<Tuple> zpopmax(String key, int count) {
        return shardedJedis.zpopmax(key, count);
    }

    @Override
    public Tuple zpopmin(String key) {
        return shardedJedis.zpopmin(key);
    }

    @Override
    public Set<Tuple> zpopmin(String key, int count) {
        return shardedJedis.zpopmin(key, count);
    }

    @Override
    public List<String> sort(String key) {
        return shardedJedis.sort(key);
    }

    @Override
    public List<String> sort(String key, SortingParams sortingParameters) {
        return shardedJedis.sort(key, sortingParameters);
    }

    @Override
    public Long zcount(String key, double min, double max) {
        return shardedJedis.zcount(key, min, max);
    }

    @Override
    public Long zcount(String key, String min, String max) {
        return shardedJedis.zcount(key, min, max);
    }

    @Override
    public Set<String> zrangeByScore(String key, double min, double max) {
        return shardedJedis.zrangeByScore(key, min, max);
    }

    @Override
    public Set<String> zrangeByScore(String key, String min, String max) {
        return shardedJedis.zrangeByScore(key, min, max);
    }

    @Override
    public Set<String> zrevrangeByScore(String key, double max, double min) {
        return shardedJedis.zrevrangeByScore(key, max, min);
    }

    @Override
    public Set<String> zrangeByScore(String key, double min, double max, int offset, int count) {
        return shardedJedis.zrangeByScore(key, min, max, offset, count);
    }

    @Override
    public Set<String> zrevrangeByScore(String key, String max, String min) {
        return shardedJedis.zrevrangeByScore(key, max, min);
    }

    @Override
    public Set<String> zrangeByScore(String key, String min, String max, int offset, int count) {
        return shardedJedis.zrangeByScore(key, min, max, offset, count);
    }

    @Override
    public Set<String> zrevrangeByScore(String key, double max, double min, int offset, int count) {
        return shardedJedis.zrevrangeByScore(key, max, min, offset, count);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max) {
        return shardedJedis.zrangeByScoreWithScores(key, min, max);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min) {
        return shardedJedis.zrevrangeByScoreWithScores(key, max, min);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max, int offset, int count) {
        return shardedJedis.zrangeByScoreWithScores(key, min, max, offset, count);
    }

    @Override
    public Set<String> zrevrangeByScore(String key, String max, String min, int offset, int count) {
        return shardedJedis.zrevrangeByScore(key, max, min, offset, count);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max) {
        return shardedJedis.zrangeByScoreWithScores(key, min, max);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min) {
        return shardedJedis.zrevrangeByScoreWithScores(key, max, min);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max, int offset, int count) {
        return shardedJedis.zrangeByScoreWithScores(key, min, max, offset, count);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min, int offset, int count) {
        return shardedJedis.zrevrangeByScoreWithScores(key, max, min, offset, count);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min, int offset, int count) {
        return shardedJedis.zrevrangeByScoreWithScores(key, max, min, offset, count);
    }

    @Override
    public Long zremrangeByRank(String key, long start, long stop) {
        return shardedJedis.zremrangeByRank(key, start, stop);
    }

    @Override
    public Long zremrangeByScore(String key, double min, double max) {
        return shardedJedis.zremrangeByScore(key, min, max);
    }

    @Override
    public Long zremrangeByScore(String key, String min, String max) {
        return shardedJedis.zremrangeByScore(key, min, max);
    }

    @Override
    public Long zlexcount(String key, String min, String max) {
        return shardedJedis.zlexcount(key, min, max);
    }

    @Override
    public Set<String> zrangeByLex(String key, String min, String max) {
        return shardedJedis.zrangeByLex(key, min, max);
    }

    @Override
    public Set<String> zrangeByLex(String key, String min, String max, int offset, int count) {
        return shardedJedis.zrangeByLex(key, min, max, offset, count);
    }

    @Override
    public Set<String> zrevrangeByLex(String key, String max, String min) {
        return shardedJedis.zrevrangeByLex(key, max, min);
    }

    @Override
    public Set<String> zrevrangeByLex(String key, String max, String min, int offset, int count) {
        return shardedJedis.zrevrangeByLex(key, max, min, offset, count);
    }

    @Override
    public Long zremrangeByLex(String key, String min, String max) {
        return shardedJedis.zremrangeByLex(key, min, max);
    }

    @Override
    public Long linsert(String key, ListPosition where, String pivot, String value) {
        return shardedJedis.linsert(key, where, pivot, value);
    }

    @Override
    public Long lpushx(String key, String... string) {
        return shardedJedis.lpushx(key, string);
    }

    @Override
    public Long rpushx(String key, String... string) {
        return shardedJedis.rpushx(key, string);
    }

    @Override
    public List<String> blpop(int timeout, String key) {
        return shardedJedis.blpop(timeout, key);
    }

    @Override
    public KeyedListElement blpop(double timeout, String key) {
        return shardedJedis.blpop(timeout, key);
    }

    @Override
    public List<String> brpop(int timeout, String key) {
        return shardedJedis.brpop(timeout, key);
    }

    @Override
    public KeyedListElement brpop(double timeout, String key) {
        return shardedJedis.brpop(timeout, key);
    }

    @Override
    public Long del(String key) {
        return shardedJedis.del(key);
    }

    @Override
    public Long unlink(String key) {
        return shardedJedis.unlink(key);
    }

    @Override
    public String echo(String string) {
        return shardedJedis.echo(string);
    }

    @Override
    public Long move(String key, int dbIndex) {
        return shardedJedis.move(key, dbIndex);
    }

    @Override
    public Long bitcount(String key) {
        return shardedJedis.bitcount(key);
    }

    @Override
    public Long bitcount(String key, long start, long end) {
        return shardedJedis.bitcount(key, start, end);
    }

    @Override
    public Long bitpos(String key, boolean value) {
        return shardedJedis.bitpos(key, value);
    }

    @Override
    public Long bitpos(String key, boolean value, BitPosParams params) {
        return shardedJedis.bitpos(key, value, params);
    }

    @Override
    public ScanResult<Map.Entry<String, String>> hscan(String key, String cursor) {
        return shardedJedis.hscan(key, cursor);
    }

    @Override
    public ScanResult<Map.Entry<String, String>> hscan(String key, String cursor, ScanParams params) {
        return shardedJedis.hscan(key, cursor, params);
    }

    @Override
    public ScanResult<String> sscan(String key, String cursor) {
        return shardedJedis.sscan(key, cursor);
    }

    @Override
    public ScanResult<Tuple> zscan(String key, String cursor) {
        return shardedJedis.zscan(key, cursor);
    }

    @Override
    public ScanResult<Tuple> zscan(String key, String cursor, ScanParams params) {
        return shardedJedis.zscan(key, cursor, params);
    }

    @Override
    public ScanResult<String> sscan(String key, String cursor, ScanParams params) {
        return shardedJedis.sscan(key, cursor, params);
    }

    @Override
    public Long pfadd(String key, String... elements) {
        return shardedJedis.pfadd(key, elements);
    }

    @Override
    public long pfcount(String key) {
        return shardedJedis.pfcount(key);
    }

    @Override
    public Long geoadd(String key, double longitude, double latitude, String member) {
        return shardedJedis.geoadd(key, longitude, latitude, member);
    }

    @Override
    public Long geoadd(String key, Map<String, GeoCoordinate> memberCoordinateMap) {
        return shardedJedis.geoadd(key, memberCoordinateMap);
    }

    @Override
    public Long geoadd(String key, GeoAddParams params, Map<String, GeoCoordinate> memberCoordinateMap) {
        return shardedJedis.geoadd(key, params, memberCoordinateMap);
    }

    @Override
    public Double geodist(String key, String member1, String member2) {
        return shardedJedis.geodist(key, member1, member2);
    }

    @Override
    public Double geodist(String key, String member1, String member2, GeoUnit unit) {
        return shardedJedis.geodist(key, member1, member2, unit);
    }

    @Override
    public List<String> geohash(String key, String... members) {
        return shardedJedis.geohash(key, members);
    }

    @Override
    public List<GeoCoordinate> geopos(String key, String... members) {
        return shardedJedis.geopos(key, members);
    }

    @Override
    public List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius, GeoUnit unit) {
        return shardedJedis.georadius(key, longitude, latitude, radius, unit);
    }

    @Override
    public List<GeoRadiusResponse> georadiusReadonly(String key, double longitude, double latitude, double radius, GeoUnit unit) {
        return shardedJedis.georadiusReadonly(key, longitude, latitude, radius, unit);
    }

    @Override
    public List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param) {
        return shardedJedis.georadius(key, longitude, latitude, radius, unit, param);
    }

    @Override
    public List<GeoRadiusResponse> georadiusReadonly(String key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param) {
        return shardedJedis.georadiusReadonly(key, longitude, latitude, radius, unit, param);
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit) {
        return shardedJedis.georadiusByMember(key, member, radius, unit);
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMemberReadonly(String key, String member, double radius, GeoUnit unit) {
        return shardedJedis.georadiusByMemberReadonly(key, member, radius, unit);
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit, GeoRadiusParam param) {
        return shardedJedis.georadiusByMember(key, member, radius, unit, param);
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMemberReadonly(String key, String member, double radius, GeoUnit unit, GeoRadiusParam param) {
        return shardedJedis.georadiusByMemberReadonly(key, member, radius, unit, param);
    }

    @Override
    public List<Long> bitfield(String key, String... arguments) {
        return shardedJedis.bitfield(key, arguments);
    }

    @Override
    public List<Long> bitfieldReadonly(String key, String... arguments) {
        return shardedJedis.bitfieldReadonly(key, arguments);
    }

    @Override
    public Long hstrlen(String key, String field) {
        return shardedJedis.hstrlen(key, field);
    }

    @Override
    public StreamEntryID xadd(String key, StreamEntryID id, Map<String, String> hash) {
        return shardedJedis.xadd(key, id, hash);
    }

    @Override
    public StreamEntryID xadd(String key, StreamEntryID id, Map<String, String> hash, long maxLen, boolean approximateLength) {
        return shardedJedis.xadd(key, id, hash, maxLen, approximateLength);
    }

    @Override
    public StreamEntryID xadd(String key, Map<String, String> hash, XAddParams params) {
        return shardedJedis.xadd(key, hash, params);
    }

    @Override
    public Long xlen(String key) {
        return shardedJedis.xlen(key);
    }

    @Override
    public List<StreamEntry> xrange(String key, StreamEntryID start, StreamEntryID end) {
        return shardedJedis.xrange(key, start, end);
    }

    @Override
    public List<StreamEntry> xrange(String key, StreamEntryID start, StreamEntryID end, int count) {
        return shardedJedis.xrange(key, start, end, count);
    }

    @Override
    public List<StreamEntry> xrevrange(String key, StreamEntryID end, StreamEntryID start) {
        return shardedJedis.xrevrange(key, end, start);
    }

    @Override
    public List<StreamEntry> xrevrange(String key, StreamEntryID end, StreamEntryID start, int count) {
        return shardedJedis.xrevrange(key, end, start, count);
    }

    @Override
    public long xack(String key, String group, StreamEntryID... ids) {
        return shardedJedis.xack(key, group, ids);
    }

    @Override
    public String xgroupCreate(String key, String groupname, StreamEntryID id, boolean makeStream) {
        return shardedJedis.xgroupCreate(key, groupname, id, makeStream);
    }

    @Override
    public String xgroupSetID(String key, String groupname, StreamEntryID id) {
        return shardedJedis.xgroupSetID(key, groupname, id);
    }

    @Override
    public long xgroupDestroy(String key, String groupname) {
        return shardedJedis.xgroupDestroy(key, groupname);
    }

    @Override
    public Long xgroupDelConsumer(String key, String groupname, String consumername) {
        return shardedJedis.xgroupDelConsumer(key, groupname, consumername);
    }

    @Override
    public StreamPendingSummary xpending(String key, String groupname) {
        return shardedJedis.xpending(key, groupname);
    }

    @Override
    public List<StreamPendingEntry> xpending(String key, String groupname, StreamEntryID start, StreamEntryID end, int count, String consumername) {
        return shardedJedis.xpending(key, groupname, start, end, count, consumername);
    }

    @Override
    public List<StreamPendingEntry> xpending(String key, String groupname, XPendingParams params) {
        return shardedJedis.xpending(key, groupname, params);
    }

    @Override
    public long xdel(String key, StreamEntryID... ids) {
        return shardedJedis.xdel(key, ids);
    }

    @Override
    public long xtrim(String key, long maxLen, boolean approximate) {
        return shardedJedis.xtrim(key, maxLen, approximate);
    }

    @Override
    public long xtrim(String key, XTrimParams params) {
        return shardedJedis.xtrim(key, params);
    }

    @Override
    public List<StreamEntry> xclaim(String key, String group, String consumername, long minIdleTime, long newIdleTime, int retries, boolean force, StreamEntryID... ids) {
        return shardedJedis.xclaim(key, group, consumername, minIdleTime, newIdleTime, retries, force, ids);
    }

    @Override
    public List<StreamEntry> xclaim(String key, String group, String consumername, long minIdleTime, XClaimParams params, StreamEntryID... ids) {
        return shardedJedis.xclaim(key, group, consumername, minIdleTime, params, ids);
    }

    @Override
    public List<StreamEntryID> xclaimJustId(String key, String group, String consumername, long minIdleTime, XClaimParams params, StreamEntryID... ids) {
        return shardedJedis.xclaimJustId(key, group, consumername, minIdleTime, params, ids);
    }

    @Override
    public Map.Entry<StreamEntryID, List<StreamEntry>> xautoclaim(String key, String group, String consumerName, long minIdleTime, StreamEntryID start, XAutoClaimParams params) {
        return shardedJedis.xautoclaim(key, group, consumerName, minIdleTime, start, params);
    }

    @Override
    public Map.Entry<StreamEntryID, List<StreamEntryID>> xautoclaimJustId(String key, String group, String consumerName, long minIdleTime, StreamEntryID start, XAutoClaimParams params) {
        return shardedJedis.xautoclaimJustId(key, group, consumerName, minIdleTime, start, params);
    }

    @Override
    public StreamInfo xinfoStream(String key) {
        return shardedJedis.xinfoStream(key);
    }

    @Override
    public List<StreamGroupInfo> xinfoGroup(String key) {
        return shardedJedis.xinfoGroup(key);
    }

    @Override
    public List<StreamConsumersInfo> xinfoConsumers(String key, String group) {
        return shardedJedis.xinfoConsumers(key, group);
    }

    @Override
    public Object eval(String script, int keyCount, String... params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object eval(String script, List<String> keys, List<String> args) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object eval(String script) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object evalsha(String sha1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object eval(String script, String sampleKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object evalsha(String sha1, String sampleKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object evalsha(String sha1, List<String> keys, List<String> args) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object evalsha(String sha1, int keyCount, String... params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Boolean scriptExists(String sha1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Boolean> scriptExists(String... sha1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String scriptLoad(String script) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Boolean scriptExists(String sha1, String sampleKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Boolean> scriptExists(String sampleKey, String... sha1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String scriptLoad(String script, String sampleKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String scriptFlush(String sampleKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String scriptKill(String sampleKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String moduleLoad(String path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String moduleUnload(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Module> moduleList() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<byte[]> blpop(byte[]... args) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<byte[]> brpop(byte[]... args) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String watch(byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] randomBinaryKey() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Boolean copy(byte[] srcKey, byte[] dstKey, int db, boolean replace) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Boolean copy(byte[] srcKey, byte[] dstKey, boolean replace) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long del(byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long unlink(byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long exists(byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] lmove(byte[] srcKey, byte[] dstKey, ListDirection from, ListDirection to) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] blmove(byte[] srcKey, byte[] dstKey, ListDirection from, ListDirection to, double timeout) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<byte[]> blpop(int timeout, byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<byte[]> blpop(double timeout, byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<byte[]> brpop(int timeout, byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<byte[]> brpop(double timeout, byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<byte[]> bzpopmax(double timeout, byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<byte[]> bzpopmin(double timeout, byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<byte[]> mget(byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String mset(byte[]... keysvalues) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long msetnx(byte[]... keysvalues) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String rename(byte[] oldkey, byte[] newkey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long renamenx(byte[] oldkey, byte[] newkey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] rpoplpush(byte[] srckey, byte[] dstkey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<byte[]> sdiff(byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long sdiffstore(byte[] dstkey, byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<byte[]> sinter(byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long sinterstore(byte[] dstkey, byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long smove(byte[] srckey, byte[] dstkey, byte[] member) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long sort(byte[] key, SortingParams sortingParameters, byte[] dstkey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long sort(byte[] key, byte[] dstkey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<byte[]> sunion(byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long sunionstore(byte[] dstkey, byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<byte[]> zdiff(byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Tuple> zdiffWithScores(byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long zdiffStore(byte[] dstkey, byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<byte[]> zinter(ZParams params, byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Tuple> zinterWithScores(ZParams params, byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long zinterstore(byte[] dstkey, byte[]... sets) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long zinterstore(byte[] dstkey, ZParams params, byte[]... sets) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<byte[]> zunion(ZParams params, byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Tuple> zunionWithScores(ZParams params, byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long zunionstore(byte[] dstkey, byte[]... sets) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long zunionstore(byte[] dstkey, ZParams params, byte[]... sets) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] brpoplpush(byte[] source, byte[] destination, int timeout) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long publish(byte[] channel, byte[] message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void subscribe(BinaryJedisPubSub jedisPubSub, byte[]... channels) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void psubscribe(BinaryJedisPubSub jedisPubSub, byte[]... patterns) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long bitop(BitOP op, byte[] destKey, byte[]... srcKeys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String pfmerge(byte[] destkey, byte[]... sourcekeys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long pfcount(byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long touch(byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ScanResult<byte[]> scan(byte[] cursor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ScanResult<byte[]> scan(byte[] cursor, ScanParams params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ScanResult<byte[]> scan(byte[] cursor, ScanParams params, byte[] type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<byte[]> keys(byte[] pattern) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<byte[]> xread(int count, long block, Map<byte[], byte[]> streams) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<byte[]> xread(XReadParams xReadParams, Map.Entry<byte[], byte[]>... streams) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<byte[]> xreadGroup(byte[] groupname, byte[] consumer, int count, long block, boolean noAck, Map<byte[], byte[]> streams) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<byte[]> xreadGroup(byte[] groupname, byte[] consumer, XReadGroupParams xReadGroupParams, Map.Entry<byte[], byte[]>... streams) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long georadiusStore(byte[] key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param, GeoRadiusStoreParam storeParam) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long georadiusByMemberStore(byte[] key, byte[] member, double radius, GeoUnit unit, GeoRadiusParam param, GeoRadiusStoreParam storeParam) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LCSMatchResult strAlgoLCSKeys(byte[] keyA, byte[] keyB, StrAlgoLCSParams params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LCSMatchResult strAlgoLCSStrings(byte[] strA, byte[] strB, StrAlgoLCSParams params) {
        return shardedJedis.strAlgoLCSStrings(strA, strB, params);
    }

    @Override
    public List<String> blpop(String... args) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> brpop(String... args) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String watch(String... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String unwatch() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String randomKey() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ScanResult<String> scan(String cursor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Boolean copy(String srcKey, String dstKey, int db, boolean replace) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Boolean copy(String srcKey, String dstKey, boolean replace) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long del(String... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long unlink(String... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long exists(String... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String lmove(String srcKey, String dstKey, ListDirection from, ListDirection to) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String blmove(String srcKey, String dstKey, ListDirection from, ListDirection to, double timeout) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> blpop(int timeout, String... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public KeyedListElement blpop(double timeout, String... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> brpop(int timeout, String... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public KeyedListElement brpop(double timeout, String... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public KeyedZSetElement bzpopmax(double timeout, String... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public KeyedZSetElement bzpopmin(double timeout, String... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> mget(String... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String mset(String... keysvalues) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long msetnx(String... keysvalues) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String rename(String oldkey, String newkey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long renamenx(String oldkey, String newkey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String rpoplpush(String srckey, String dstkey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> sdiff(String... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long sdiffstore(String dstkey, String... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> sinter(String... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long sinterstore(String dstkey, String... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long smove(String srckey, String dstkey, String member) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long sort(String key, SortingParams sortingParameters, String dstkey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long sort(String key, String dstkey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> sunion(String... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long sunionstore(String dstkey, String... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> zdiff(String... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Tuple> zdiffWithScores(String... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long zdiffStore(String dstkey, String... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> zinter(ZParams params, String... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Tuple> zinterWithScores(ZParams params, String... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long zinterstore(String dstkey, String... sets) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long zinterstore(String dstkey, ZParams params, String... sets) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> zunion(ZParams params, String... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Tuple> zunionWithScores(ZParams params, String... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long zunionstore(String dstkey, String... sets) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long zunionstore(String dstkey, ZParams params, String... sets) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String brpoplpush(String source, String destination, int timeout) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long publish(String channel, String message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void subscribe(JedisPubSub jedisPubSub, String... channels) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void psubscribe(JedisPubSub jedisPubSub, String... patterns) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long bitop(BitOP op, String destKey, String... srcKeys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String pfmerge(String destkey, String... sourcekeys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long pfcount(String... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long touch(String... keys) {
        throw new UnsupportedOperationException();
    }

    @SafeVarargs
    @Override
    public final List<Map.Entry<String, List<StreamEntry>>> xread(int count, long block, Map.Entry<String, StreamEntryID>... streams) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Map.Entry<String, List<StreamEntry>>> xread(XReadParams xReadParams, Map<String, StreamEntryID> streams) {
        throw new UnsupportedOperationException();
    }

    @SafeVarargs
    @Override
    public final List<Map.Entry<String, List<StreamEntry>>> xreadGroup(String groupname, String consumer, int count, long block, boolean noAck, Map.Entry<String, StreamEntryID>... streams) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Map.Entry<String, List<StreamEntry>>> xreadGroup(String groupname, String consumer, XReadGroupParams xReadGroupParams, Map<String, StreamEntryID> streams) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LCSMatchResult strAlgoLCSKeys(String keyA, String keyB, StrAlgoLCSParams params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LCSMatchResult strAlgoLCSStrings(String strA, String strB, StrAlgoLCSParams params) {
        return shardedJedis.strAlgoLCSStrings(strA, strB, params);
    }

    @Override
    public ScanResult<String> scan(String cursor, ScanParams params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ScanResult<String> scan(String cursor, ScanParams params, String type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> keys(String pattern) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long georadiusStore(String key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param, GeoRadiusStoreParam storeParam) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long georadiusByMemberStore(String key, String member, double radius, GeoUnit unit, GeoRadiusParam param, GeoRadiusStoreParam storeParam) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String sentinelMyId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Map<String, String>> sentinelMasters() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String> sentinelMaster(String masterName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Map<String, String>> sentinelSentinels(String masterName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> sentinelGetMasterAddrByName(String masterName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long sentinelReset(String pattern) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Map<String, String>> sentinelSlaves(String masterName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Map<String, String>> sentinelReplicas(String masterName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String sentinelFailover(String masterName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String sentinelMonitor(String masterName, String ip, int port, int quorum) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String sentinelRemove(String masterName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String sentinelSet(String masterName, Map<String, String> parameterMap) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCluster() {
        return false;
    }

    @Override
    public boolean isSentinel() {
        return false;
    }

    @Override
    public boolean isSharded() {
        return true;
    }

    @Override
    public boolean isNormal() {
        return false;
    }

    @Override
    public boolean isClosed() {
        return isClosed;
    }

    @Override
    public Object getOriginJedis() {
        return shardedJedis;
    }
}
