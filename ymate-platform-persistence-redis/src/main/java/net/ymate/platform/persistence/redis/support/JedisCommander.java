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
 * @author 刘镇 (suninformation@163.com) on 2019-05-23 00:24
 * @since 2.1.0
 */
public class JedisCommander implements IRedisCommander {

    private final Jedis jedis;

    private volatile boolean isClosed;

    JedisCommander(Jedis jedis) {
        if (jedis == null) {
            throw new NullArgumentException("jedis");
        }
        this.jedis = jedis;
    }

    @Override
    public void close() {
        if (!isClosed) {
            jedis.close();
            isClosed = true;
        }
    }

    @Override
    public List<Object> roleBinary() {
        return jedis.roleBinary();
    }

    @Override
    public List<byte[]> configGet(byte[] pattern) {
        return jedis.configGet(pattern);
    }

    @Override
    public byte[] configSet(byte[] parameter, byte[] value) {
        return jedis.configSet(parameter, value);
    }

    @Override
    public String configSetBinary(byte[] parameter, byte[] value) {
        return jedis.configSetBinary(parameter, value);
    }

    @Override
    public List<Object> slowlogGetBinary() {
        return jedis.slowlogGetBinary();
    }

    @Override
    public List<Object> slowlogGetBinary(long entries) {
        return jedis.slowlogGetBinary(entries);
    }

    @Override
    public Long objectRefcount(byte[] key) {
        return jedis.objectRefcount(key);
    }

    @Override
    public byte[] objectEncoding(byte[] key) {
        return jedis.objectEncoding(key);
    }

    @Override
    public Long objectIdletime(byte[] key) {
        return jedis.objectIdletime(key);
    }

    @Override
    public List<byte[]> objectHelpBinary() {
        return jedis.objectHelpBinary();
    }

    @Override
    public Long objectFreq(byte[] key) {
        return jedis.objectFreq(key);
    }

    @Override
    public String migrate(String host, int port, byte[] key, int destinationDB, int timeout) {
        return jedis.migrate(host, port, key, destinationDB, timeout);
    }

    @Override
    public String migrate(String host, int port, int destinationDB, int timeout, MigrateParams params, byte[]... keys) {
        return jedis.migrate(host, port, destinationDB, timeout, params, keys);
    }

    @Override
    public String clientKill(byte[] ipPort) {
        return jedis.clientKill(ipPort);
    }

    @Override
    public byte[] clientGetnameBinary() {
        return jedis.clientGetnameBinary();
    }

    @Override
    public byte[] clientListBinary() {
        return jedis.clientListBinary();
    }

    @Override
    public byte[] clientListBinary(ClientType type) {
        return jedis.clientListBinary(type);
    }

    @Override
    public byte[] clientListBinary(long... clientIds) {
        return jedis.clientListBinary(clientIds);
    }

    @Override
    public byte[] clientInfoBinary() {
        return jedis.clientInfoBinary();
    }

    @Override
    public String clientSetname(byte[] name) {
        return jedis.clientSetname(name);
    }

    @Override
    public Long clientId() {
        return jedis.clientId();
    }

    @Override
    public String clientPause(long timeout) {
        return jedis.clientPause(timeout);
    }

    @Override
    public String clientPause(long timeout, ClientPauseMode mode) {
        return jedis.clientPause(timeout, mode);
    }

    @Override
    public byte[] memoryDoctorBinary() {
        return jedis.memoryDoctorBinary();
    }

    @Override
    public byte[] aclWhoAmIBinary() {
        return jedis.aclWhoAmIBinary();
    }

    @Override
    public byte[] aclGenPassBinary() {
        return jedis.aclGenPassBinary();
    }

    @Override
    public List<byte[]> aclListBinary() {
        return jedis.aclListBinary();
    }

    @Override
    public List<byte[]> aclUsersBinary() {
        return jedis.aclUsersBinary();
    }

    @Override
    public AccessControlUser aclGetUser(byte[] name) {
        return jedis.aclGetUser(name);
    }

    @Override
    public String aclSetUser(byte[] name) {
        return jedis.aclSetUser(name);
    }

    @Override
    public String aclSetUser(byte[] name, byte[]... keys) {
        return jedis.aclSetUser(name, keys);
    }

    @Override
    public Long aclDelUser(byte[] name) {
        return jedis.aclDelUser(name);
    }

    @Override
    public List<byte[]> aclCatBinary() {
        return jedis.aclCatBinary();
    }

    @Override
    public List<byte[]> aclCat(byte[] category) {
        return jedis.aclCat(category);
    }

    @Override
    public List<byte[]> aclLogBinary() {
        return jedis.aclLogBinary();
    }

    @Override
    public List<byte[]> aclLogBinary(int limit) {
        return jedis.aclLogBinary(limit);
    }

    @Override
    public byte[] aclLog(byte[] options) {
        return jedis.aclLog(options);
    }

    @Override
    public String aclLoad() {
        return jedis.aclLoad();
    }

    @Override
    public String aclSave() {
        return jedis.aclSave();
    }

    @Override
    public List<Object> role() {
        return jedis.role();
    }

    @Override
    public List<String> configGet(String pattern) {
        return jedis.configGet(pattern);
    }

    @Override
    public String configSet(String parameter, String value) {
        return jedis.configSet(parameter, value);
    }

    @Override
    public String slowlogReset() {
        return jedis.slowlogReset();
    }

    @Override
    public Long slowlogLen() {
        return jedis.slowlogLen();
    }

    @Override
    public List<Slowlog> slowlogGet() {
        return jedis.slowlogGet();
    }

    @Override
    public List<Slowlog> slowlogGet(long entries) {
        return jedis.slowlogGet(entries);
    }

    @Override
    public Long objectRefcount(String key) {
        return jedis.objectRefcount(key);
    }

    @Override
    public String objectEncoding(String key) {
        return jedis.objectEncoding(key);
    }

    @Override
    public Long objectIdletime(String key) {
        return jedis.objectIdletime(key);
    }

    @Override
    public List<String> objectHelp() {
        return jedis.objectHelp();
    }

    @Override
    public Long objectFreq(String key) {
        return jedis.objectFreq(key);
    }

    @Override
    public String migrate(String host, int port, String key, int destinationDB, int timeout) {
        return jedis.migrate(host, port, key, destinationDB, timeout);
    }

    @Override
    public String migrate(String host, int port, int destinationDB, int timeout, MigrateParams params, String... keys) {
        return jedis.migrate(host, port, destinationDB, timeout, params, keys);
    }

    @Override
    public String clientKill(String ipPort) {
        return jedis.clientKill(ipPort);
    }

    @Override
    public String clientKill(String ip, int port) {
        return jedis.clientKill(ip, port);
    }

    @Override
    public Long clientKill(ClientKillParams params) {
        return jedis.clientKill(params);
    }

    @Override
    public Long clientUnblock(long clientId, UnblockType unblockType) {
        return jedis.clientUnblock(clientId, unblockType);
    }

    @Override
    public String clientGetname() {
        return jedis.clientGetname();
    }

    @Override
    public String clientList() {
        return jedis.clientList();
    }

    @Override
    public String clientList(ClientType type) {
        return jedis.clientList(type);
    }

    @Override
    public String clientList(long... clientIds) {
        return jedis.clientList(clientIds);
    }

    @Override
    public String clientInfo() {
        return jedis.clientInfo();
    }

    @Override
    public String clientSetname(String name) {
        return jedis.clientSetname(name);
    }

    @Override
    public String memoryDoctor() {
        return jedis.memoryDoctor();
    }

    @Override
    public Long memoryUsage(String key) {
        return jedis.memoryUsage(key);
    }

    @Override
    public Long memoryUsage(String key, int samples) {
        return jedis.memoryUsage(key, samples);
    }

    @Override
    public String aclWhoAmI() {
        return jedis.aclWhoAmI();
    }

    @Override
    public String aclGenPass() {
        return jedis.aclGenPass();
    }

    @Override
    public List<String> aclList() {
        return jedis.aclList();
    }

    @Override
    public List<String> aclUsers() {
        return jedis.aclUsers();
    }

    @Override
    public AccessControlUser aclGetUser(String name) {
        return jedis.aclGetUser(name);
    }

    @Override
    public String aclSetUser(String name) {
        return jedis.aclSetUser(name);
    }

    @Override
    public String aclSetUser(String name, String... keys) {
        return jedis.aclSetUser(name, keys);
    }

    @Override
    public Long aclDelUser(String name) {
        return jedis.aclDelUser(name);
    }

    @Override
    public List<String> aclCat() {
        return jedis.aclCat();
    }

    @Override
    public List<String> aclCat(String category) {
        return jedis.aclCat(category);
    }

    @Override
    public List<AccessControlLogEntry> aclLog() {
        return jedis.aclLog();
    }

    @Override
    public List<AccessControlLogEntry> aclLog(int limit) {
        return jedis.aclLog(limit);
    }

    @Override
    public String aclLog(String options) {
        return jedis.aclLog(options);
    }

    @Override
    public String ping() {
        return jedis.ping();
    }

    @Override
    public String quit() {
        return jedis.quit();
    }

    @Override
    public String flushDB() {
        return jedis.flushDB();
    }

    @Override
    public String flushDB(FlushMode flushMode) {
        return jedis.flushDB(flushMode);
    }

    @Override
    public Long dbSize() {
        return jedis.dbSize();
    }

    @Override
    public String select(int index) {
        return jedis.select(index);
    }

    @Override
    public String swapDB(int index1, int index2) {
        return jedis.swapDB(index1, index2);
    }

    @Override
    public String flushAll() {
        return jedis.flushAll();
    }

    @Override
    public String flushAll(FlushMode flushMode) {
        return jedis.flushAll(flushMode);
    }

    @Override
    public String auth(String password) {
        return jedis.auth(password);
    }

    @Override
    public String auth(String user, String password) {
        return jedis.auth(user, password);
    }

    @Override
    public String save() {
        return jedis.save();
    }

    @Override
    public String bgsave() {
        return jedis.bgsave();
    }

    @Override
    public String bgrewriteaof() {
        return jedis.bgrewriteaof();
    }

    @Override
    public Long lastsave() {
        return jedis.lastsave();
    }

    @Override
    public String shutdown() {
        return jedis.shutdown();
    }

    @Override
    public void shutdown(SaveMode saveMode) throws JedisException {
        jedis.shutdown(saveMode);
    }

    @Override
    public String info() {
        return jedis.info();
    }

    @Override
    public String info(String section) {
        return jedis.info(section);
    }

    @Override
    public String slaveof(String host, int port) {
        return jedis.slaveof(host, port);
    }

    @Override
    public String slaveofNoOne() {
        return jedis.slaveofNoOne();
    }

    @Override
    public int getDB() {
        return jedis.getDB();
    }

    @Override
    public String debug(DebugParams params) {
        return jedis.debug(params);
    }

    @Override
    public String configResetStat() {
        return jedis.configResetStat();
    }

    @Override
    public String configRewrite() {
        return jedis.configRewrite();
    }

    @Override
    public Long waitReplicas(int replicas, long timeout) {
        return jedis.waitReplicas(replicas, timeout);
    }

    @Override
    public String set(byte[] key, byte[] value) {
        return jedis.set(key, value);
    }

    @Override
    public String set(byte[] key, byte[] value, SetParams params) {
        return jedis.set(key, value, params);
    }

    @Override
    public byte[] get(byte[] key) {
        return jedis.get(key);
    }

    @Override
    public byte[] getDel(byte[] key) {
        return jedis.getDel(key);
    }

    @Override
    public byte[] getEx(byte[] key, GetExParams params) {
        return jedis.getEx(key, params);
    }

    @Override
    public Boolean exists(byte[] key) {
        return jedis.exists(key);
    }

    @Override
    public Long persist(byte[] key) {
        return jedis.persist(key);
    }

    @Override
    public String type(byte[] key) {
        return jedis.type(key);
    }

    @Override
    public byte[] dump(byte[] key) {
        return jedis.dump(key);
    }

    @Override
    public String restore(byte[] key, long ttl, byte[] serializedValue) {
        return jedis.restore(key, ttl, serializedValue);
    }

    @Override
    public String restore(byte[] key, long ttl, byte[] serializedValue, RestoreParams params) {
        return jedis.restore(key, ttl, serializedValue, params);
    }

    @Override
    public String restoreReplace(byte[] key, long ttl, byte[] serializedValue) {
        return jedis.restoreReplace(key, ttl, serializedValue);
    }

    @Override
    public Long expire(byte[] key, long seconds) {
        return jedis.expire(key, seconds);
    }

    @Override
    public Long pexpire(byte[] key, long milliseconds) {
        return jedis.pexpire(key, milliseconds);
    }

    @Override
    public Long expireAt(byte[] key, long unixTime) {
        return jedis.expireAt(key, unixTime);
    }

    @Override
    public Long pexpireAt(byte[] key, long millisecondsTimestamp) {
        return jedis.pexpireAt(key, millisecondsTimestamp);
    }

    @Override
    public Long ttl(byte[] key) {
        return jedis.ttl(key);
    }

    @Override
    public Long pttl(byte[] key) {
        return jedis.pttl(key);
    }

    @Override
    public Long touch(byte[] key) {
        return jedis.touch(key);
    }

    @Override
    public Boolean setbit(byte[] key, long offset, boolean value) {
        return jedis.setbit(key, offset, value);
    }

    @Override
    public Boolean setbit(byte[] key, long offset, byte[] value) {
        return jedis.setbit(key, offset, value);
    }

    @Override
    public Boolean getbit(byte[] key, long offset) {
        return jedis.getbit(key, offset);
    }

    @Override
    public Long setrange(byte[] key, long offset, byte[] value) {
        return jedis.setrange(key, offset, value);
    }

    @Override
    public byte[] getrange(byte[] key, long startOffset, long endOffset) {
        return jedis.getrange(key, startOffset, endOffset);
    }

    @Override
    public byte[] getSet(byte[] key, byte[] value) {
        return jedis.getSet(key, value);
    }

    @Override
    public Long setnx(byte[] key, byte[] value) {
        return jedis.setnx(key, value);
    }

    @Override
    public String setex(byte[] key, long seconds, byte[] value) {
        return jedis.setex(key, seconds, value);
    }

    @Override
    public String psetex(byte[] key, long milliseconds, byte[] value) {
        return jedis.psetex(key, milliseconds, value);
    }

    @Override
    public Long decrBy(byte[] key, long decrement) {
        return jedis.decrBy(key, decrement);
    }

    @Override
    public Long decr(byte[] key) {
        return jedis.decr(key);
    }

    @Override
    public Long incrBy(byte[] key, long increment) {
        return jedis.incrBy(key, increment);
    }

    @Override
    public Double incrByFloat(byte[] key, double increment) {
        return jedis.incrByFloat(key, increment);
    }

    @Override
    public Long incr(byte[] key) {
        return jedis.incr(key);
    }

    @Override
    public Long append(byte[] key, byte[] value) {
        return jedis.append(key, value);
    }

    @Override
    public byte[] substr(byte[] key, int start, int end) {
        return jedis.substr(key, start, end);
    }

    @Override
    public Long hset(byte[] key, byte[] field, byte[] value) {
        return jedis.hset(key, field, value);
    }

    @Override
    public Long hset(byte[] key, Map<byte[], byte[]> hash) {
        return jedis.hset(key, hash);
    }

    @Override
    public byte[] hget(byte[] key, byte[] field) {
        return jedis.hget(key, field);
    }

    @Override
    public Long hsetnx(byte[] key, byte[] field, byte[] value) {
        return jedis.hsetnx(key, field, value);
    }

    @Override
    public String hmset(byte[] key, Map<byte[], byte[]> hash) {
        return jedis.hmset(key, hash);
    }

    @Override
    public List<byte[]> hmget(byte[] key, byte[]... fields) {
        return jedis.hmget(key, fields);
    }

    @Override
    public Long hincrBy(byte[] key, byte[] field, long value) {
        return jedis.hincrBy(key, field, value);
    }

    @Override
    public Double hincrByFloat(byte[] key, byte[] field, double value) {
        return jedis.hincrByFloat(key, field, value);
    }

    @Override
    public Boolean hexists(byte[] key, byte[] field) {
        return jedis.hexists(key, field);
    }

    @Override
    public Long hdel(byte[] key, byte[]... field) {
        return jedis.hdel(key, field);
    }

    @Override
    public Long hlen(byte[] key) {
        return jedis.hlen(key);
    }

    @Override
    public Set<byte[]> hkeys(byte[] key) {
        return jedis.hkeys(key);
    }

    @Override
    public List<byte[]> hvals(byte[] key) {
        return jedis.hvals(key);
    }

    @Override
    public Map<byte[], byte[]> hgetAll(byte[] key) {
        return jedis.hgetAll(key);
    }

    @Override
    public byte[] hrandfield(byte[] key) {
        return jedis.hrandfield(key);
    }

    @Override
    public List<byte[]> hrandfield(byte[] key, long count) {
        return jedis.hrandfield(key, count);
    }

    @Override
    public Map<byte[], byte[]> hrandfieldWithValues(byte[] key, long count) {
        return jedis.hrandfieldWithValues(key, count);
    }

    @Override
    public Long rpush(byte[] key, byte[]... args) {
        return jedis.rpush(key, args);
    }

    @Override
    public Long lpush(byte[] key, byte[]... args) {
        return jedis.lpush(key, args);
    }

    @Override
    public Long llen(byte[] key) {
        return jedis.llen(key);
    }

    @Override
    public List<byte[]> lrange(byte[] key, long start, long stop) {
        return jedis.lrange(key, start, stop);
    }

    @Override
    public String ltrim(byte[] key, long start, long stop) {
        return jedis.ltrim(key, start, stop);
    }

    @Override
    public byte[] lindex(byte[] key, long index) {
        return jedis.lindex(key, index);
    }

    @Override
    public String lset(byte[] key, long index, byte[] value) {
        return jedis.lset(key, index, value);
    }

    @Override
    public Long lrem(byte[] key, long count, byte[] value) {
        return jedis.lrem(key, count, value);
    }

    @Override
    public byte[] lpop(byte[] key) {
        return jedis.lpop(key);
    }

    @Override
    public List<byte[]> lpop(byte[] key, int count) {
        return jedis.lpop(key, count);
    }

    @Override
    public Long lpos(byte[] key, byte[] element) {
        return jedis.lpos(key, element);
    }

    @Override
    public Long lpos(byte[] key, byte[] element, LPosParams params) {
        return jedis.lpos(key, element, params);
    }

    @Override
    public List<Long> lpos(byte[] key, byte[] element, LPosParams params, long count) {
        return jedis.lpos(key, element, params, count);
    }

    @Override
    public byte[] rpop(byte[] key) {
        return jedis.rpop(key);
    }

    @Override
    public List<byte[]> rpop(byte[] key, int count) {
        return jedis.rpop(key, count);
    }

    @Override
    public Long sadd(byte[] key, byte[]... member) {
        return jedis.sadd(key, member);
    }

    @Override
    public Set<byte[]> smembers(byte[] key) {
        return jedis.smembers(key);
    }

    @Override
    public Long srem(byte[] key, byte[]... member) {
        return jedis.srem(key, member);
    }

    @Override
    public byte[] spop(byte[] key) {
        return jedis.spop(key);
    }

    @Override
    public Set<byte[]> spop(byte[] key, long count) {
        return jedis.spop(key, count);
    }

    @Override
    public Long scard(byte[] key) {
        return jedis.scard(key);
    }

    @Override
    public Boolean sismember(byte[] key, byte[] member) {
        return jedis.sismember(key, member);
    }

    @Override
    public List<Boolean> smismember(byte[] key, byte[]... members) {
        return jedis.smismember(key, members);
    }

    @Override
    public byte[] srandmember(byte[] key) {
        return jedis.srandmember(key);
    }

    @Override
    public List<byte[]> srandmember(byte[] key, int count) {
        return jedis.srandmember(key, count);
    }

    @Override
    public Long strlen(byte[] key) {
        return jedis.strlen(key);
    }

    @Override
    public Long zadd(byte[] key, double score, byte[] member) {
        return jedis.zadd(key, score, member);
    }

    @Override
    public Long zadd(byte[] key, double score, byte[] member, ZAddParams params) {
        return jedis.zadd(key, score, member, params);
    }

    @Override
    public Long zadd(byte[] key, Map<byte[], Double> scoreMembers) {
        return jedis.zadd(key, scoreMembers);
    }

    @Override
    public Long zadd(byte[] key, Map<byte[], Double> scoreMembers, ZAddParams params) {
        return jedis.zadd(key, scoreMembers, params);
    }

    @Override
    public Double zaddIncr(byte[] key, double score, byte[] member, ZAddParams params) {
        return jedis.zaddIncr(key, score, member, params);
    }

    @Override
    public Set<byte[]> zrange(byte[] key, long start, long stop) {
        return jedis.zrange(key, start, stop);
    }

    @Override
    public Long zrem(byte[] key, byte[]... members) {
        return jedis.zrem(key, members);
    }

    @Override
    public Double zincrby(byte[] key, double increment, byte[] member) {
        return jedis.zincrby(key, increment, member);
    }

    @Override
    public Double zincrby(byte[] key, double increment, byte[] member, ZIncrByParams params) {
        return jedis.zincrby(key, increment, member, params);
    }

    @Override
    public Long zrank(byte[] key, byte[] member) {
        return jedis.zrank(key, member);
    }

    @Override
    public Long zrevrank(byte[] key, byte[] member) {
        return jedis.zrevrank(key, member);
    }

    @Override
    public Set<byte[]> zrevrange(byte[] key, long start, long stop) {
        return jedis.zrevrange(key, start, stop);
    }

    @Override
    public Set<Tuple> zrangeWithScores(byte[] key, long start, long stop) {
        return jedis.zrangeWithScores(key, start, stop);
    }

    @Override
    public Set<Tuple> zrevrangeWithScores(byte[] key, long start, long stop) {
        return jedis.zrevrangeWithScores(key, start, stop);
    }

    @Override
    public byte[] zrandmember(byte[] key) {
        return jedis.zrandmember(key);
    }

    @Override
    public Set<byte[]> zrandmember(byte[] key, long count) {
        return jedis.zrandmember(key, count);
    }

    @Override
    public Set<Tuple> zrandmemberWithScores(byte[] key, long count) {
        return jedis.zrandmemberWithScores(key, count);
    }

    @Override
    public Long zcard(byte[] key) {
        return jedis.zcard(key);
    }

    @Override
    public Double zscore(byte[] key, byte[] member) {
        return jedis.zscore(key, member);
    }

    @Override
    public List<Double> zmscore(byte[] key, byte[]... members) {
        return jedis.zmscore(key, members);
    }

    @Override
    public Tuple zpopmax(byte[] key) {
        return jedis.zpopmax(key);
    }

    @Override
    public Set<Tuple> zpopmax(byte[] key, int count) {
        return jedis.zpopmax(key, count);
    }

    @Override
    public Tuple zpopmin(byte[] key) {
        return jedis.zpopmin(key);
    }

    @Override
    public Set<Tuple> zpopmin(byte[] key, int count) {
        return jedis.zpopmin(key, count);
    }

    @Override
    public List<byte[]> sort(byte[] key) {
        return jedis.sort(key);
    }

    @Override
    public List<byte[]> sort(byte[] key, SortingParams sortingParameters) {
        return jedis.sort(key, sortingParameters);
    }

    @Override
    public Long zcount(byte[] key, double min, double max) {
        return jedis.zcount(key, min, max);
    }

    @Override
    public Long zcount(byte[] key, byte[] min, byte[] max) {
        return jedis.zcount(key, min, max);
    }

    @Override
    public Set<byte[]> zrangeByScore(byte[] key, double min, double max) {
        return jedis.zrangeByScore(key, min, max);
    }

    @Override
    public Set<byte[]> zrangeByScore(byte[] key, byte[] min, byte[] max) {
        return jedis.zrangeByScore(key, min, max);
    }

    @Override
    public Set<byte[]> zrevrangeByScore(byte[] key, double max, double min) {
        return jedis.zrevrangeByScore(key, max, min);
    }

    @Override
    public Set<byte[]> zrangeByScore(byte[] key, double min, double max, int offset, int count) {
        return jedis.zrangeByScore(key, min, max, offset, count);
    }

    @Override
    public Set<byte[]> zrevrangeByScore(byte[] key, byte[] max, byte[] min) {
        return jedis.zrevrangeByScore(key, max, min);
    }

    @Override
    public Set<byte[]> zrangeByScore(byte[] key, byte[] min, byte[] max, int offset, int count) {
        return jedis.zrangeByScore(key, min, max, offset, count);
    }

    @Override
    public Set<byte[]> zrevrangeByScore(byte[] key, double max, double min, int offset, int count) {
        return jedis.zrevrangeByScore(key, max, min, offset, count);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max) {
        return jedis.zrangeByScoreWithScores(key, min, max);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, double max, double min) {
        return jedis.zrevrangeByScoreWithScores(key, max, min);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max, int offset, int count) {
        return jedis.zrevrangeByScoreWithScores(key, max, min);
    }

    @Override
    public Set<byte[]> zrevrangeByScore(byte[] key, byte[] max, byte[] min, int offset, int count) {
        return jedis.zrevrangeByScore(key, max, min, offset, count);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(byte[] key, byte[] min, byte[] max) {
        return jedis.zrangeByScoreWithScores(key, min, max);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, byte[] max, byte[] min) {
        return jedis.zrevrangeByScoreWithScores(key, max, min);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(byte[] key, byte[] min, byte[] max, int offset, int count) {
        return jedis.zrangeByScoreWithScores(key, min, max, offset, count);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, double max, double min, int offset, int count) {
        return jedis.zrevrangeByScoreWithScores(key, max, min, offset, count);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, byte[] max, byte[] min, int offset, int count) {
        return jedis.zrevrangeByScoreWithScores(key, max, min, offset, count);
    }

    @Override
    public Long zremrangeByRank(byte[] key, long start, long stop) {
        return jedis.zremrangeByRank(key, start, stop);
    }

    @Override
    public Long zremrangeByScore(byte[] key, double min, double max) {
        return jedis.zremrangeByScore(key, min, max);
    }

    @Override
    public Long zremrangeByScore(byte[] key, byte[] min, byte[] max) {
        return jedis.zremrangeByScore(key, min, max);
    }

    @Override
    public Long zlexcount(byte[] key, byte[] min, byte[] max) {
        return jedis.zlexcount(key, min, max);
    }

    @Override
    public Set<byte[]> zrangeByLex(byte[] key, byte[] min, byte[] max) {
        return jedis.zrangeByLex(key, min, max);
    }

    @Override
    public Set<byte[]> zrangeByLex(byte[] key, byte[] min, byte[] max, int offset, int count) {
        return jedis.zrangeByLex(key, min, max, offset, count);
    }

    @Override
    public Set<byte[]> zrevrangeByLex(byte[] key, byte[] max, byte[] min) {
        return jedis.zrevrangeByLex(key, max, min);
    }

    @Override
    public Set<byte[]> zrevrangeByLex(byte[] key, byte[] max, byte[] min, int offset, int count) {
        return jedis.zrevrangeByLex(key, max, min, offset, count);
    }

    @Override
    public Long zremrangeByLex(byte[] key, byte[] min, byte[] max) {
        return jedis.zremrangeByLex(key, min, max);
    }

    @Override
    public Long linsert(byte[] key, ListPosition where, byte[] pivot, byte[] value) {
        return jedis.linsert(key, where, pivot, value);
    }

    @Override
    public Long lpushx(byte[] key, byte[]... arg) {
        return jedis.lpushx(key, arg);
    }

    @Override
    public Long rpushx(byte[] key, byte[]... arg) {
        return jedis.rpushx(key, arg);
    }

    @Override
    public Long del(byte[] key) {
        return jedis.del(key);
    }

    @Override
    public Long unlink(byte[] key) {
        return jedis.unlink(key);
    }

    @Override
    public byte[] echo(byte[] arg) {
        return jedis.echo(arg);
    }

    @Override
    public Long move(byte[] key, int dbIndex) {
        return jedis.move(key, dbIndex);
    }

    @Override
    public Long bitcount(byte[] key) {
        return jedis.bitcount(key);
    }

    @Override
    public Long bitcount(byte[] key, long start, long end) {
        return jedis.bitcount(key, start, end);
    }

    @Override
    public Long pfadd(byte[] key, byte[]... elements) {
        return jedis.pfadd(key, elements);
    }

    @Override
    public long pfcount(byte[] key) {
        return jedis.pfcount(key);
    }

    @Override
    public Long geoadd(byte[] key, double longitude, double latitude, byte[] member) {
        return jedis.geoadd(key, longitude, latitude, member);
    }

    @Override
    public Long geoadd(byte[] key, Map<byte[], GeoCoordinate> memberCoordinateMap) {
        return jedis.geoadd(key, memberCoordinateMap);
    }

    @Override
    public Long geoadd(byte[] key, GeoAddParams params, Map<byte[], GeoCoordinate> memberCoordinateMap) {
        return jedis.geoadd(key, params, memberCoordinateMap);
    }

    @Override
    public Double geodist(byte[] key, byte[] member1, byte[] member2) {
        return jedis.geodist(key, member1, member2);
    }

    @Override
    public Double geodist(byte[] key, byte[] member1, byte[] member2, GeoUnit unit) {
        return jedis.geodist(key, member1, member2, unit);
    }

    @Override
    public List<byte[]> geohash(byte[] key, byte[]... members) {
        return jedis.geohash(key, members);
    }

    @Override
    public List<GeoCoordinate> geopos(byte[] key, byte[]... members) {
        return jedis.geopos(key, members);
    }

    @Override
    public List<GeoRadiusResponse> georadius(byte[] key, double longitude, double latitude, double radius, GeoUnit unit) {
        return jedis.georadius(key, longitude, latitude, radius, unit);
    }

    @Override
    public List<GeoRadiusResponse> georadiusReadonly(byte[] key, double longitude, double latitude, double radius, GeoUnit unit) {
        return jedis.georadiusReadonly(key, longitude, latitude, radius, unit);
    }

    @Override
    public List<GeoRadiusResponse> georadius(byte[] key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param) {
        return jedis.georadius(key, longitude, latitude, radius, unit, param);
    }

    @Override
    public List<GeoRadiusResponse> georadiusReadonly(byte[] key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param) {
        return jedis.georadiusReadonly(key, longitude, latitude, radius, unit, param);
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMember(byte[] key, byte[] member, double radius, GeoUnit unit) {
        return jedis.georadiusByMember(key, member, radius, unit);
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMemberReadonly(byte[] key, byte[] member, double radius, GeoUnit unit) {
        return jedis.georadiusByMemberReadonly(key, member, radius, unit);
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMember(byte[] key, byte[] member, double radius, GeoUnit unit, GeoRadiusParam param) {
        return jedis.georadiusByMember(key, member, radius, unit, param);
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMemberReadonly(byte[] key, byte[] member, double radius, GeoUnit unit, GeoRadiusParam param) {
        return jedis.georadiusByMemberReadonly(key, member, radius, unit, param);
    }

    @Override
    public ScanResult<Map.Entry<byte[], byte[]>> hscan(byte[] key, byte[] cursor) {
        return jedis.hscan(key, cursor);
    }

    @Override
    public ScanResult<Map.Entry<byte[], byte[]>> hscan(byte[] key, byte[] cursor, ScanParams params) {
        return jedis.hscan(key, cursor, params);
    }

    @Override
    public ScanResult<byte[]> sscan(byte[] key, byte[] cursor) {
        return jedis.sscan(key, cursor);
    }

    @Override
    public ScanResult<byte[]> sscan(byte[] key, byte[] cursor, ScanParams params) {
        return jedis.sscan(key, cursor, params);
    }

    @Override
    public ScanResult<Tuple> zscan(byte[] key, byte[] cursor) {
        return jedis.zscan(key, cursor);
    }

    @Override
    public ScanResult<Tuple> zscan(byte[] key, byte[] cursor, ScanParams params) {
        return jedis.zscan(key, cursor, params);
    }

    @Override
    public List<Long> bitfield(byte[] key, byte[]... arguments) {
        return jedis.bitfield(key, arguments);
    }

    @Override
    public List<Long> bitfieldReadonly(byte[] key, byte[]... arguments) {
        return jedis.bitfieldReadonly(key, arguments);
    }

    @Override
    public Long hstrlen(byte[] key, byte[] field) {
        return jedis.hstrlen(key, field);
    }

    @Override
    public byte[] xadd(byte[] key, byte[] id, Map<byte[], byte[]> hash, long maxLen, boolean approximateLength) {
        return jedis.xadd(key, id, hash, maxLen, approximateLength);
    }

    @Override
    public byte[] xadd(byte[] key, Map<byte[], byte[]> hash, XAddParams params) {
        return jedis.xadd(key, hash, params);
    }

    @Override
    public Long xlen(byte[] key) {
        return jedis.xlen(key);
    }

    @Override
    public List<byte[]> xrange(byte[] key, byte[] start, byte[] end) {
        return jedis.xrange(key, start, end);
    }

    @Override
    public List<byte[]> xrange(byte[] key, byte[] start, byte[] end, long count) {
        return jedis.xrange(key, start, end, count);
    }

    @Override
    public List<byte[]> xrange(byte[] key, byte[] start, byte[] end, int count) {
        return jedis.xrange(key, start, end, count);
    }

    @Override
    public List<byte[]> xrevrange(byte[] key, byte[] end, byte[] start) {
        return jedis.xrevrange(key, end, start);
    }

    @Override
    public List<byte[]> xrevrange(byte[] key, byte[] end, byte[] start, int count) {
        return jedis.xrevrange(key, end, start, count);
    }

    @Override
    public Long xack(byte[] key, byte[] group, byte[]... ids) {
        return jedis.xack(key, group, ids);
    }

    @Override
    public String xgroupCreate(byte[] key, byte[] consumer, byte[] id, boolean makeStream) {
        return jedis.xgroupCreate(key, consumer, id, makeStream);
    }

    @Override
    public String xgroupSetID(byte[] key, byte[] consumer, byte[] id) {
        return jedis.xgroupSetID(key, consumer, id);
    }

    @Override
    public Long xgroupDestroy(byte[] key, byte[] consumer) {
        return jedis.xgroupDestroy(key, consumer);
    }

    @Override
    public Long xgroupDelConsumer(byte[] key, byte[] consumer, byte[] consumerName) {
        return jedis.xgroupDelConsumer(key, consumer, consumerName);
    }

    @Override
    public Long xdel(byte[] key, byte[]... ids) {
        return jedis.xdel(key, ids);
    }

    @Override
    public Long xtrim(byte[] key, long maxLen, boolean approximateLength) {
        return jedis.xtrim(key, maxLen, approximateLength);
    }

    @Override
    public Long xtrim(byte[] key, XTrimParams params) {
        return jedis.xtrim(key, params);
    }

    @Override
    public Object xpending(byte[] key, byte[] groupname) {
        return jedis.xpending(key, groupname);
    }

    @Override
    public List<Object> xpending(byte[] key, byte[] groupname, byte[] start, byte[] end, int count, byte[] consumername) {
        return jedis.xpending(key, groupname, start, end, count, consumername);
    }

    @Override
    public List<Object> xpending(byte[] key, byte[] groupname, XPendingParams params) {
        return jedis.xpending(key, groupname, params);
    }

    @Override
    public List<byte[]> xclaim(byte[] key, byte[] groupname, byte[] consumername, long minIdleTime, long newIdleTime, int retries, boolean force, byte[][] ids) {
        return jedis.xclaim(key, groupname, consumername, minIdleTime, newIdleTime, retries, force, ids);
    }

    @Override
    public List<byte[]> xclaim(byte[] key, byte[] group, byte[] consumername, long minIdleTime, XClaimParams params, byte[]... ids) {
        return jedis.xclaim(key, group, consumername, minIdleTime, params, ids);
    }

    @Override
    public List<byte[]> xclaimJustId(byte[] key, byte[] group, byte[] consumername, long minIdleTime, XClaimParams params, byte[]... ids) {
        return jedis.xclaimJustId(key, group, consumername, minIdleTime, params, ids);
    }

    @Override
    public List<Object> xautoclaim(byte[] key, byte[] groupName, byte[] consumerName, long minIdleTime, byte[] start, XAutoClaimParams params) {
        return jedis.xautoclaim(key, groupName, consumerName, minIdleTime, start, params);
    }

    @Override
    public List<Object> xautoclaimJustId(byte[] key, byte[] groupName, byte[] consumerName, long minIdleTime, byte[] start, XAutoClaimParams params) {
        return jedis.xautoclaimJustId(key, groupName, consumerName, minIdleTime, start, params);
    }

    @Override
    public StreamInfo xinfoStream(byte[] key) {
        return jedis.xinfoStream(key);
    }

    @Override
    public Object xinfoStreamBinary(byte[] key) {
        return jedis.xinfoStreamBinary(key);
    }

    @Override
    public List<StreamGroupInfo> xinfoGroup(byte[] key) {
        return jedis.xinfoGroup(key);
    }

    @Override
    public List<Object> xinfoGroupBinary(byte[] key) {
        return jedis.xinfoGroupBinary(key);
    }

    @Override
    public List<StreamConsumersInfo> xinfoConsumers(byte[] key, byte[] group) {
        return jedis.xinfoConsumers(key, group);
    }

    @Override
    public List<Object> xinfoConsumersBinary(byte[] key, byte[] group) {
        return jedis.xinfoConsumersBinary(key, group);
    }

    @Override
    public Long waitReplicas(byte[] key, int replicas, long timeout) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long memoryUsage(byte[] key) {
        return jedis.memoryUsage(key);
    }

    @Override
    public Long memoryUsage(byte[] key, int samples) {
        return jedis.memoryUsage(key, samples);
    }

    @Override
    public String failover() {
        return jedis.failover();
    }

    @Override
    public String failover(FailoverParams failoverParams) {
        return jedis.failover(failoverParams);
    }

    @Override
    public String failoverAbort() {
        return jedis.failoverAbort();
    }

    @Override
    public Object eval(byte[] script) {
        return jedis.eval(script);
    }

    @Override
    public Object evalsha(byte[] sha1) {
        return jedis.evalsha(sha1);
    }

    @Override
    public List<Long> scriptExists(byte[]... sha1) {
        return jedis.scriptExists(sha1);
    }

    @Override
    public byte[] scriptLoad(byte[] script) {
        return jedis.scriptLoad(script);
    }

    @Override
    public String scriptFlush() {
        return jedis.scriptFlush();
    }

    @Override
    public String scriptFlush(FlushMode flushMode) {
        return jedis.scriptFlush(flushMode);
    }

    @Override
    public String scriptKill() {
        return jedis.scriptKill();
    }

    @Override
    public String clusterNodes() {
        return jedis.clusterNodes();
    }

    @Override
    public String clusterReplicas(String nodeId) {
        return jedis.clusterReplicas(nodeId);
    }

    @Override
    public String clusterMeet(String ip, int port) {
        return jedis.clusterMeet(ip, port);
    }

    @Override
    public String clusterAddSlots(int... slots) {
        return jedis.clusterAddSlots(slots);
    }

    @Override
    public String clusterDelSlots(int... slots) {
        return jedis.clusterDelSlots(slots);
    }

    @Override
    public String clusterInfo() {
        return jedis.clusterInfo();
    }

    @Override
    public List<String> clusterGetKeysInSlot(int slot, int count) {
        return jedis.clusterGetKeysInSlot(slot, count);
    }

    @Override
    public String clusterSetSlotNode(int slot, String nodeId) {
        return jedis.clusterSetSlotNode(slot, nodeId);
    }

    @Override
    public String clusterSetSlotMigrating(int slot, String nodeId) {
        return jedis.clusterSetSlotMigrating(slot, nodeId);
    }

    @Override
    public String clusterSetSlotImporting(int slot, String nodeId) {
        return jedis.clusterSetSlotImporting(slot, nodeId);
    }

    @Override
    public String clusterSetSlotStable(int slot) {
        return jedis.clusterSetSlotStable(slot);
    }

    @Override
    public String clusterForget(String nodeId) {
        return jedis.clusterForget(nodeId);
    }

    @Override
    public String clusterFlushSlots() {
        return jedis.clusterFlushSlots();
    }

    @Override
    public Long clusterKeySlot(String key) {
        return jedis.clusterKeySlot(key);
    }

    @Override
    public Long clusterCountKeysInSlot(int slot) {
        return jedis.clusterCountKeysInSlot(slot);
    }

    @Override
    public String clusterSaveConfig() {
        return jedis.clusterSaveConfig();
    }

    @Override
    public String clusterReplicate(String nodeId) {
        return jedis.clusterReplicate(nodeId);
    }

    @Override
    public List<String> clusterSlaves(String nodeId) {
        return jedis.clusterSlaves(nodeId);
    }

    @Override
    public String clusterFailover() {
        return jedis.clusterFailover();
    }

    @Override
    public String clusterFailover(ClusterFailoverOption failoverOption) {
        return jedis.clusterFailover(failoverOption);
    }

    @Override
    public List<Object> clusterSlots() {
        return jedis.clusterSlots();
    }

    @Override
    public String clusterReset(ClusterReset resetType) {
        return jedis.clusterReset(resetType);
    }

    @Override
    public String clusterReset(ClusterResetType resetType) {
        return jedis.clusterReset(resetType);
    }

    @Override
    public String clusterMyId() {
        return jedis.clusterMyId();
    }

    @Override
    public String readonly() {
        return jedis.readonly();
    }

    @Override
    public String readwrite() {
        return jedis.readwrite();
    }

    @Override
    public Object eval(byte[] script, byte[] keyCount, byte[]... params) {
        return jedis.eval(script, keyCount, params);
    }

    @Override
    public Object eval(byte[] script, int keyCount, byte[]... params) {
        return jedis.eval(script, keyCount, params);
    }

    @Override
    public Object eval(byte[] script, List<byte[]> keys, List<byte[]> args) {
        return jedis.eval(script, keys, args);
    }

    @Override
    public Object eval(byte[] script, byte[] sampleKey) {
        return jedis.eval(script, sampleKey);
    }

    @Override
    public Object evalsha(byte[] sha1, byte[] sampleKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object evalsha(byte[] sha1, List<byte[]> keys, List<byte[]> args) {
        return jedis.evalsha(sha1, keys, args);
    }

    @Override
    public Object evalsha(byte[] sha1, int keyCount, byte[]... params) {
        return jedis.evalsha(sha1, keyCount, params);
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
        return jedis.set(key, value);
    }

    @Override
    public String set(String key, String value, SetParams params) {
        return jedis.set(key, value, params);
    }

    @Override
    public String get(String key) {
        return jedis.get(key);
    }

    @Override
    public String getDel(String key) {
        return jedis.getDel(key);
    }

    @Override
    public String getEx(String key, GetExParams params) {
        return jedis.getEx(key, params);
    }

    @Override
    public Boolean exists(String key) {
        return jedis.exists(key);
    }

    @Override
    public Long persist(String key) {
        return jedis.persist(key);
    }

    @Override
    public String type(String key) {
        return jedis.type(key);
    }

    @Override
    public byte[] dump(String key) {
        return jedis.dump(key);
    }

    @Override
    public String restore(String key, int ttl, byte[] serializedValue) {
        return jedis.restore(key, ttl, serializedValue);
    }

    @Override
    public String restore(String key, long ttl, byte[] serializedValue) {
        return jedis.restore(key, ttl, serializedValue);
    }

    @Override
    public String restoreReplace(String key, long ttl, byte[] serializedValue) {
        return jedis.restoreReplace(key, ttl, serializedValue);
    }

    @Override
    public String restore(String key, long ttl, byte[] serializedValue, RestoreParams params) {
        return jedis.restore(key, ttl, serializedValue, params);
    }

    @Override
    public Long expire(String key, long seconds) {
        return jedis.expire(key, seconds);
    }

    @Override
    public Long pexpire(String key, long milliseconds) {
        return jedis.pexpire(key, milliseconds);
    }

    @Override
    public Long expireAt(String key, long unixTime) {
        return jedis.expireAt(key, unixTime);
    }

    @Override
    public Long pexpireAt(String key, long millisecondsTimestamp) {
        return jedis.pexpireAt(key, millisecondsTimestamp);
    }

    @Override
    public Long ttl(String key) {
        return jedis.ttl(key);
    }

    @Override
    public Long pttl(String key) {
        return jedis.pttl(key);
    }

    @Override
    public Long touch(String key) {
        return jedis.touch(key);
    }

    @Override
    public Boolean setbit(String key, long offset, boolean value) {
        return jedis.setbit(key, offset, value);
    }

    @Override
    public Boolean setbit(String key, long offset, String value) {
        return jedis.setbit(key, offset, value);
    }

    @Override
    public Boolean getbit(String key, long offset) {
        return jedis.getbit(key, offset);
    }

    @Override
    public Long setrange(String key, long offset, String value) {
        return jedis.setrange(key, offset, value);
    }

    @Override
    public String getrange(String key, long startOffset, long endOffset) {
        return jedis.getrange(key, startOffset, endOffset);
    }

    @Override
    public String getSet(String key, String value) {
        return jedis.getSet(key, value);
    }

    @Override
    public Long setnx(String key, String value) {
        return jedis.setnx(key, value);
    }

    @Override
    public String setex(String key, long seconds, String value) {
        return jedis.setex(key, seconds, value);
    }

    @Override
    public String psetex(String key, long milliseconds, String value) {
        return jedis.psetex(key, milliseconds, value);
    }

    @Override
    public Long decrBy(String key, long decrement) {
        return jedis.decrBy(key, decrement);
    }

    @Override
    public Long decr(String key) {
        return jedis.decr(key);
    }

    @Override
    public Long incrBy(String key, long increment) {
        return jedis.incrBy(key, increment);
    }

    @Override
    public Double incrByFloat(String key, double increment) {
        return jedis.incrByFloat(key, increment);
    }

    @Override
    public Long incr(String key) {
        return jedis.incr(key);
    }

    @Override
    public Long append(String key, String value) {
        return jedis.append(key, value);
    }

    @Override
    public String substr(String key, int start, int end) {
        return jedis.substr(key, start, end);
    }

    @Override
    public Long hset(String key, String field, String value) {
        return jedis.hset(key, field, value);
    }

    @Override
    public Long hset(String key, Map<String, String> hash) {
        return jedis.hset(key, hash);
    }

    @Override
    public String hget(String key, String field) {
        return jedis.hget(key, field);
    }

    @Override
    public Long hsetnx(String key, String field, String value) {
        return jedis.hsetnx(key, field, value);
    }

    @Override
    public String hmset(String key, Map<String, String> hash) {
        return jedis.hmset(key, hash);
    }

    @Override
    public List<String> hmget(String key, String... fields) {
        return jedis.hmget(key, fields);
    }

    @Override
    public Long hincrBy(String key, String field, long value) {
        return jedis.hincrBy(key, field, value);
    }

    @Override
    public Double hincrByFloat(String key, String field, double value) {
        return jedis.hincrByFloat(key, field, value);
    }

    @Override
    public Boolean hexists(String key, String field) {
        return jedis.hexists(key, field);
    }

    @Override
    public Long hdel(String key, String... field) {
        return jedis.hdel(key, field);
    }

    @Override
    public Long hlen(String key) {
        return jedis.hlen(key);
    }

    @Override
    public Set<String> hkeys(String key) {
        return jedis.hkeys(key);
    }

    @Override
    public List<String> hvals(String key) {
        return jedis.hvals(key);
    }

    @Override
    public Map<String, String> hgetAll(String key) {
        return jedis.hgetAll(key);
    }

    @Override
    public String hrandfield(String key) {
        return jedis.hrandfield(key);
    }

    @Override
    public List<String> hrandfield(String key, long count) {
        return jedis.hrandfield(key, count);
    }

    @Override
    public Map<String, String> hrandfieldWithValues(String key, long count) {
        return jedis.hrandfieldWithValues(key, count);
    }

    @Override
    public Long rpush(String key, String... string) {
        return jedis.rpush(key, string);
    }

    @Override
    public Long lpush(String key, String... string) {
        return jedis.lpush(key, string);
    }

    @Override
    public Long llen(String key) {
        return jedis.llen(key);
    }

    @Override
    public List<String> lrange(String key, long start, long stop) {
        return jedis.lrange(key, start, stop);
    }

    @Override
    public String ltrim(String key, long start, long stop) {
        return jedis.ltrim(key, start, stop);
    }

    @Override
    public String lindex(String key, long index) {
        return jedis.lindex(key, index);
    }

    @Override
    public String lset(String key, long index, String value) {
        return jedis.lset(key, index, value);
    }

    @Override
    public Long lrem(String key, long count, String value) {
        return jedis.lrem(key, count, value);
    }

    @Override
    public String lpop(String key) {
        return jedis.lpop(key);
    }

    @Override
    public List<String> lpop(String key, int count) {
        return jedis.lpop(key, count);
    }

    @Override
    public Long lpos(String key, String element) {
        return jedis.lpos(key, element);
    }

    @Override
    public Long lpos(String key, String element, LPosParams params) {
        return jedis.lpos(key, element, params);
    }

    @Override
    public List<Long> lpos(String key, String element, LPosParams params, long count) {
        return jedis.lpos(key, element, params, count);
    }

    @Override
    public String rpop(String key) {
        return jedis.rpop(key);
    }

    @Override
    public List<String> rpop(String key, int count) {
        return jedis.rpop(key, count);
    }

    @Override
    public Long sadd(String key, String... member) {
        return jedis.sadd(key, member);
    }

    @Override
    public Set<String> smembers(String key) {
        return jedis.smembers(key);
    }

    @Override
    public Long srem(String key, String... member) {
        return jedis.srem(key, member);
    }

    @Override
    public String spop(String key) {
        return jedis.spop(key);
    }

    @Override
    public Set<String> spop(String key, long count) {
        return jedis.spop(key, count);
    }

    @Override
    public Long scard(String key) {
        return jedis.scard(key);
    }

    @Override
    public Boolean sismember(String key, String member) {
        return jedis.sismember(key, member);
    }

    @Override
    public List<Boolean> smismember(String key, String... members) {
        return jedis.smismember(key, members);
    }

    @Override
    public String srandmember(String key) {
        return jedis.srandmember(key);
    }

    @Override
    public List<String> srandmember(String key, int count) {
        return jedis.srandmember(key, count);
    }

    @Override
    public Long strlen(String key) {
        return jedis.strlen(key);
    }

    @Override
    public Long zadd(String key, double score, String member) {
        return jedis.zadd(key, score, member);
    }

    @Override
    public Long zadd(String key, double score, String member, ZAddParams params) {
        return jedis.zadd(key, score, member, params);
    }

    @Override
    public Long zadd(String key, Map<String, Double> scoreMembers) {
        return jedis.zadd(key, scoreMembers);
    }

    @Override
    public Long zadd(String key, Map<String, Double> scoreMembers, ZAddParams params) {
        return jedis.zadd(key, scoreMembers, params);
    }

    @Override
    public Double zaddIncr(String key, double score, String member, ZAddParams params) {
        return jedis.zaddIncr(key, score, member, params);
    }

    @Override
    public Set<String> zrange(String key, long start, long stop) {
        return jedis.zrange(key, start, stop);
    }

    @Override
    public Long zrem(String key, String... members) {
        return jedis.zrem(key, members);
    }

    @Override
    public Double zincrby(String key, double increment, String member) {
        return jedis.zincrby(key, increment, member);
    }

    @Override
    public Double zincrby(String key, double increment, String member, ZIncrByParams params) {
        return jedis.zincrby(key, increment, member, params);
    }

    @Override
    public Long zrank(String key, String member) {
        return jedis.zrank(key, member);
    }

    @Override
    public Long zrevrank(String key, String member) {
        return jedis.zrevrank(key, member);
    }

    @Override
    public Set<String> zrevrange(String key, long start, long stop) {
        return jedis.zrevrange(key, start, stop);
    }

    @Override
    public Set<Tuple> zrangeWithScores(String key, long start, long stop) {
        return jedis.zrangeWithScores(key, start, stop);
    }

    @Override
    public Set<Tuple> zrevrangeWithScores(String key, long start, long stop) {
        return jedis.zrevrangeWithScores(key, start, stop);
    }

    @Override
    public String zrandmember(String key) {
        return jedis.zrandmember(key);
    }

    @Override
    public Set<String> zrandmember(String key, long count) {
        return jedis.zrandmember(key, count);
    }

    @Override
    public Set<Tuple> zrandmemberWithScores(String key, long count) {
        return jedis.zrandmemberWithScores(key, count);
    }

    @Override
    public Long zcard(String key) {
        return jedis.zcard(key);
    }

    @Override
    public Double zscore(String key, String member) {
        return jedis.zscore(key, member);
    }

    @Override
    public List<Double> zmscore(String key, String... members) {
        return jedis.zmscore(key, members);
    }

    @Override
    public Tuple zpopmax(String key) {
        return jedis.zpopmax(key);
    }

    @Override
    public Set<Tuple> zpopmax(String key, int count) {
        return jedis.zpopmax(key, count);
    }

    @Override
    public Tuple zpopmin(String key) {
        return jedis.zpopmin(key);
    }

    @Override
    public Set<Tuple> zpopmin(String key, int count) {
        return jedis.zpopmin(key, count);
    }

    @Override
    public List<String> sort(String key) {
        return jedis.sort(key);
    }

    @Override
    public List<String> sort(String key, SortingParams sortingParameters) {
        return jedis.sort(key, sortingParameters);
    }

    @Override
    public Long zcount(String key, double min, double max) {
        return jedis.zcount(key, min, max);
    }

    @Override
    public Long zcount(String key, String min, String max) {
        return jedis.zcount(key, min, max);
    }

    @Override
    public Set<String> zrangeByScore(String key, double min, double max) {
        return jedis.zrangeByScore(key, min, max);
    }

    @Override
    public Set<String> zrangeByScore(String key, String min, String max) {
        return jedis.zrangeByScore(key, min, max);
    }

    @Override
    public Set<String> zrevrangeByScore(String key, double max, double min) {
        return jedis.zrevrangeByScore(key, max, min);
    }

    @Override
    public Set<String> zrangeByScore(String key, double min, double max, int offset, int count) {
        return jedis.zrangeByScore(key, min, max, offset, count);
    }

    @Override
    public Set<String> zrevrangeByScore(String key, String max, String min) {
        return jedis.zrevrangeByScore(key, max, min);
    }

    @Override
    public Set<String> zrangeByScore(String key, String min, String max, int offset, int count) {
        return jedis.zrangeByScore(key, min, max, offset, count);
    }

    @Override
    public Set<String> zrevrangeByScore(String key, double max, double min, int offset, int count) {
        return jedis.zrevrangeByScore(key, max, min, offset, count);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max) {
        return jedis.zrangeByScoreWithScores(key, min, max);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min) {
        return jedis.zrevrangeByScoreWithScores(key, max, min);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max, int offset, int count) {
        return jedis.zrangeByScoreWithScores(key, min, max, offset, count);
    }

    @Override
    public Set<String> zrevrangeByScore(String key, String max, String min, int offset, int count) {
        return jedis.zrevrangeByScore(key, max, min, offset, count);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max) {
        return jedis.zrangeByScoreWithScores(key, min, max);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min) {
        return jedis.zrevrangeByScoreWithScores(key, max, min);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max, int offset, int count) {
        return jedis.zrangeByScoreWithScores(key, min, max, offset, count);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min, int offset, int count) {
        return jedis.zrevrangeByScoreWithScores(key, max, min, offset, count);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min, int offset, int count) {
        return jedis.zrevrangeByScoreWithScores(key, max, min, offset, count);
    }

    @Override
    public Long zremrangeByRank(String key, long start, long stop) {
        return jedis.zremrangeByRank(key, start, stop);
    }

    @Override
    public Long zremrangeByScore(String key, double min, double max) {
        return jedis.zremrangeByScore(key, min, max);
    }

    @Override
    public Long zremrangeByScore(String key, String min, String max) {
        return jedis.zremrangeByScore(key, min, max);
    }

    @Override
    public Long zlexcount(String key, String min, String max) {
        return jedis.zlexcount(key, min, max);
    }

    @Override
    public Set<String> zrangeByLex(String key, String min, String max) {
        return jedis.zrangeByLex(key, min, max);
    }

    @Override
    public Set<String> zrangeByLex(String key, String min, String max, int offset, int count) {
        return jedis.zrangeByLex(key, min, max, offset, count);
    }

    @Override
    public Set<String> zrevrangeByLex(String key, String max, String min) {
        return jedis.zrevrangeByLex(key, max, min);
    }

    @Override
    public Set<String> zrevrangeByLex(String key, String max, String min, int offset, int count) {
        return jedis.zrevrangeByLex(key, max, min, offset, count);
    }

    @Override
    public Long zremrangeByLex(String key, String min, String max) {
        return jedis.zremrangeByLex(key, min, max);
    }

    @Override
    public Long linsert(String key, ListPosition where, String pivot, String value) {
        return jedis.linsert(key, where, pivot, value);
    }

    @Override
    public Long lpushx(String key, String... string) {
        return jedis.lpushx(key, string);
    }

    @Override
    public Long rpushx(String key, String... string) {
        return jedis.rpushx(key, string);
    }

    @Override
    public List<String> blpop(int timeout, String key) {
        return jedis.blpop(timeout, key);
    }

    @Override
    public KeyedListElement blpop(double timeout, String key) {
        return jedis.blpop(timeout, key);
    }

    @Override
    public List<String> brpop(int timeout, String key) {
        return jedis.brpop(timeout, key);
    }

    @Override
    public KeyedListElement brpop(double timeout, String key) {
        return jedis.brpop(timeout, key);
    }

    @Override
    public Long del(String key) {
        return jedis.del(key);
    }

    @Override
    public Long unlink(String key) {
        return jedis.unlink(key);
    }

    @Override
    public String echo(String string) {
        return jedis.echo(string);
    }

    @Override
    public Long move(String key, int dbIndex) {
        return jedis.move(key, dbIndex);
    }

    @Override
    public Long bitcount(String key) {
        return jedis.bitcount(key);
    }

    @Override
    public Long bitcount(String key, long start, long end) {
        return jedis.bitcount(key, start, end);
    }

    @Override
    public Long bitpos(String key, boolean value) {
        return jedis.bitpos(key, value);
    }

    @Override
    public Long bitpos(String key, boolean value, BitPosParams params) {
        return jedis.bitpos(key, value, params);
    }

    @Override
    public ScanResult<Map.Entry<String, String>> hscan(String key, String cursor) {
        return jedis.hscan(key, cursor);
    }

    @Override
    public ScanResult<Map.Entry<String, String>> hscan(String key, String cursor, ScanParams params) {
        return jedis.hscan(key, cursor, params);
    }

    @Override
    public ScanResult<String> sscan(String key, String cursor) {
        return jedis.sscan(key, cursor);
    }

    @Override
    public ScanResult<Tuple> zscan(String key, String cursor) {
        return jedis.zscan(key, cursor);
    }

    @Override
    public ScanResult<Tuple> zscan(String key, String cursor, ScanParams params) {
        return jedis.zscan(key, cursor, params);
    }

    @Override
    public ScanResult<String> sscan(String key, String cursor, ScanParams params) {
        return jedis.sscan(key, cursor, params);
    }

    @Override
    public Long pfadd(String key, String... elements) {
        return jedis.pfadd(key, elements);
    }

    @Override
    public long pfcount(String key) {
        return jedis.pfcount(key);
    }

    @Override
    public Long geoadd(String key, double longitude, double latitude, String member) {
        return jedis.geoadd(key, longitude, latitude, member);
    }

    @Override
    public Long geoadd(String key, Map<String, GeoCoordinate> memberCoordinateMap) {
        return jedis.geoadd(key, memberCoordinateMap);
    }

    @Override
    public Long geoadd(String key, GeoAddParams params, Map<String, GeoCoordinate> memberCoordinateMap) {
        return jedis.geoadd(key, params, memberCoordinateMap);
    }

    @Override
    public Double geodist(String key, String member1, String member2) {
        return jedis.geodist(key, member1, member2);
    }

    @Override
    public Double geodist(String key, String member1, String member2, GeoUnit unit) {
        return jedis.geodist(key, member1, member2, unit);
    }

    @Override
    public List<String> geohash(String key, String... members) {
        return jedis.geohash(key, members);
    }

    @Override
    public List<GeoCoordinate> geopos(String key, String... members) {
        return jedis.geopos(key, members);
    }

    @Override
    public List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius, GeoUnit unit) {
        return jedis.georadius(key, longitude, latitude, radius, unit);
    }

    @Override
    public List<GeoRadiusResponse> georadiusReadonly(String key, double longitude, double latitude, double radius, GeoUnit unit) {
        return jedis.georadiusReadonly(key, longitude, latitude, radius, unit);
    }

    @Override
    public List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param) {
        return jedis.georadius(key, longitude, latitude, radius, unit, param);
    }

    @Override
    public List<GeoRadiusResponse> georadiusReadonly(String key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param) {
        return jedis.georadiusReadonly(key, longitude, latitude, radius, unit, param);
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit) {
        return jedis.georadiusByMember(key, member, radius, unit);
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMemberReadonly(String key, String member, double radius, GeoUnit unit) {
        return jedis.georadiusByMemberReadonly(key, member, radius, unit);
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit, GeoRadiusParam param) {
        return jedis.georadiusByMember(key, member, radius, unit, param);
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMemberReadonly(String key, String member, double radius, GeoUnit unit, GeoRadiusParam param) {
        return jedis.georadiusByMemberReadonly(key, member, radius, unit, param);
    }

    @Override
    public List<Long> bitfield(String key, String... arguments) {
        return jedis.bitfield(key, arguments);
    }

    @Override
    public List<Long> bitfieldReadonly(String key, String... arguments) {
        return jedis.bitfieldReadonly(key, arguments);
    }

    @Override
    public Long hstrlen(String key, String field) {
        return jedis.hstrlen(key, field);
    }

    @Override
    public StreamEntryID xadd(String key, StreamEntryID id, Map<String, String> hash) {
        return jedis.xadd(key, id, hash);
    }

    @Override
    public StreamEntryID xadd(String key, StreamEntryID id, Map<String, String> hash, long maxLen, boolean approximateLength) {
        return jedis.xadd(key, id, hash, maxLen, approximateLength);
    }

    @Override
    public StreamEntryID xadd(String key, Map<String, String> hash, XAddParams params) {
        return jedis.xadd(key, hash, params);
    }

    @Override
    public Long xlen(String key) {
        return jedis.xlen(key);
    }

    @Override
    public List<StreamEntry> xrange(String key, StreamEntryID start, StreamEntryID end) {
        return jedis.xrange(key, start, end);
    }

    @Override
    public List<StreamEntry> xrange(String key, StreamEntryID start, StreamEntryID end, int count) {
        return jedis.xrange(key, start, end, count);
    }

    @Override
    public List<StreamEntry> xrevrange(String key, StreamEntryID end, StreamEntryID start) {
        return jedis.xrevrange(key, end, start);
    }

    @Override
    public List<StreamEntry> xrevrange(String key, StreamEntryID end, StreamEntryID start, int count) {
        return jedis.xrevrange(key, end, start, count);
    }

    @Override
    public long xack(String key, String group, StreamEntryID... ids) {
        return jedis.xack(key, group, ids);
    }

    @Override
    public String xgroupCreate(String key, String groupname, StreamEntryID id, boolean makeStream) {
        return jedis.xgroupCreate(key, groupname, id, makeStream);
    }

    @Override
    public String xgroupSetID(String key, String groupname, StreamEntryID id) {
        return jedis.xgroupSetID(key, groupname, id);
    }

    @Override
    public long xgroupDestroy(String key, String groupname) {
        return jedis.xgroupDestroy(key, groupname);
    }

    @Override
    public Long xgroupDelConsumer(String key, String groupname, String consumername) {
        return jedis.xgroupDelConsumer(key, groupname, consumername);
    }

    @Override
    public StreamPendingSummary xpending(String key, String groupname) {
        return jedis.xpending(key, groupname);
    }

    @Override
    public List<StreamPendingEntry> xpending(String key, String groupname, StreamEntryID start, StreamEntryID end, int count, String consumername) {
        return jedis.xpending(key, groupname, start, end, count, consumername);
    }

    @Override
    public List<StreamPendingEntry> xpending(String key, String groupname, XPendingParams params) {
        return jedis.xpending(key, groupname, params);
    }

    @Override
    public long xdel(String key, StreamEntryID... ids) {
        return jedis.xdel(key, ids);
    }

    @Override
    public long xtrim(String key, long maxLen, boolean approximate) {
        return jedis.xtrim(key, maxLen, approximate);
    }

    @Override
    public long xtrim(String key, XTrimParams params) {
        return jedis.xtrim(key, params);
    }

    @Override
    public List<StreamEntry> xclaim(String key, String group, String consumername, long minIdleTime, long newIdleTime, int retries, boolean force, StreamEntryID... ids) {
        return jedis.xclaim(key, group, consumername, minIdleTime, newIdleTime, retries, force, ids);
    }

    @Override
    public List<StreamEntry> xclaim(String key, String group, String consumername, long minIdleTime, XClaimParams params, StreamEntryID... ids) {
        return jedis.xclaim(key, group, consumername, minIdleTime, params, ids);
    }

    @Override
    public List<StreamEntryID> xclaimJustId(String key, String group, String consumername, long minIdleTime, XClaimParams params, StreamEntryID... ids) {
        return jedis.xclaimJustId(key, group, consumername, minIdleTime, params, ids);
    }

    @Override
    public Map.Entry<StreamEntryID, List<StreamEntry>> xautoclaim(String key, String group, String consumerName, long minIdleTime, StreamEntryID start, XAutoClaimParams params) {
        return jedis.xautoclaim(key, group, consumerName, minIdleTime, start, params);
    }

    @Override
    public Map.Entry<StreamEntryID, List<StreamEntryID>> xautoclaimJustId(String key, String group, String consumerName, long minIdleTime, StreamEntryID start, XAutoClaimParams params) {
        return jedis.xautoclaimJustId(key, group, consumerName, minIdleTime, start, params);
    }

    @Override
    public StreamInfo xinfoStream(String key) {
        return jedis.xinfoStream(key);
    }

    @Override
    public List<StreamGroupInfo> xinfoGroup(String key) {
        return jedis.xinfoGroup(key);
    }

    @Override
    public List<StreamConsumersInfo> xinfoConsumers(String key, String group) {
        return jedis.xinfoConsumers(key, group);
    }

    @Override
    public Object eval(String script, int keyCount, String... params) {
        return jedis.eval(script, keyCount, params);
    }

    @Override
    public Object eval(String script, List<String> keys, List<String> args) {
        return jedis.eval(script, keys, args);
    }

    @Override
    public Object eval(String script) {
        return jedis.eval(script);
    }

    @Override
    public Object evalsha(String sha1) {
        return jedis.evalsha(sha1);
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
        return jedis.evalsha(sha1, keys, args);
    }

    @Override
    public Object evalsha(String sha1, int keyCount, String... params) {
        return jedis.evalsha(sha1, keyCount, params);
    }

    @Override
    public Boolean scriptExists(String sha1) {
        return jedis.scriptExists(sha1);
    }

    @Override
    public List<Boolean> scriptExists(String... sha1) {
        return jedis.scriptExists(sha1);
    }

    @Override
    public String scriptLoad(String script) {
        return jedis.scriptLoad(script);
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
        return jedis.moduleLoad(path);
    }

    @Override
    public String moduleUnload(String name) {
        return jedis.moduleUnload(name);
    }

    @Override
    public List<redis.clients.jedis.Module> moduleList() {
        return jedis.moduleList();
    }

    @Override
    public List<byte[]> blpop(byte[]... args) {
        return jedis.blpop(args);
    }

    @Override
    public List<byte[]> brpop(byte[]... args) {
        return jedis.brpop(args);
    }

    @Override
    public String watch(byte[]... keys) {
        return jedis.watch(keys);
    }

    @Override
    public byte[] randomBinaryKey() {
        return jedis.randomBinaryKey();
    }

    @Override
    public Boolean copy(byte[] srcKey, byte[] dstKey, int db, boolean replace) {
        return jedis.copy(srcKey, dstKey, db, replace);
    }

    @Override
    public Boolean copy(byte[] srcKey, byte[] dstKey, boolean replace) {
        return jedis.copy(srcKey, dstKey, replace);
    }

    @Override
    public Long del(byte[]... keys) {
        return jedis.del(keys);
    }

    @Override
    public Long unlink(byte[]... keys) {
        return jedis.unlink(keys);
    }

    @Override
    public Long exists(byte[]... keys) {
        return jedis.exists(keys);
    }

    @Override
    public byte[] lmove(byte[] srcKey, byte[] dstKey, ListDirection from, ListDirection to) {
        return jedis.lmove(srcKey, dstKey, from, to);
    }

    @Override
    public byte[] blmove(byte[] srcKey, byte[] dstKey, ListDirection from, ListDirection to, double timeout) {
        return jedis.blmove(srcKey, dstKey, from, to, timeout);
    }

    @Override
    public List<byte[]> blpop(int timeout, byte[]... keys) {
        return jedis.blpop(timeout, keys);
    }

    @Override
    public List<byte[]> blpop(double timeout, byte[]... keys) {
        return jedis.blpop(timeout, keys);
    }

    @Override
    public List<byte[]> brpop(int timeout, byte[]... keys) {
        return jedis.brpop(timeout, keys);
    }

    @Override
    public List<byte[]> brpop(double timeout, byte[]... keys) {
        return jedis.brpop(timeout, keys);
    }

    @Override
    public List<byte[]> bzpopmax(double timeout, byte[]... keys) {
        return jedis.bzpopmax(timeout, keys);
    }

    @Override
    public List<byte[]> bzpopmin(double timeout, byte[]... keys) {
        return jedis.bzpopmin(timeout, keys);
    }

    @Override
    public List<byte[]> mget(byte[]... keys) {
        return jedis.mget(keys);
    }

    @Override
    public String mset(byte[]... keysvalues) {
        return jedis.mset(keysvalues);
    }

    @Override
    public Long msetnx(byte[]... keysvalues) {
        return jedis.msetnx(keysvalues);
    }

    @Override
    public String rename(byte[] oldkey, byte[] newkey) {
        return jedis.rename(oldkey, newkey);
    }

    @Override
    public Long renamenx(byte[] oldkey, byte[] newkey) {
        return jedis.renamenx(oldkey, newkey);
    }

    @Override
    public byte[] rpoplpush(byte[] srckey, byte[] dstkey) {
        return jedis.rpoplpush(srckey, dstkey);
    }

    @Override
    public Set<byte[]> sdiff(byte[]... keys) {
        return jedis.sdiff(keys);
    }

    @Override
    public Long sdiffstore(byte[] dstkey, byte[]... keys) {
        return jedis.sdiffstore(dstkey, keys);
    }

    @Override
    public Set<byte[]> sinter(byte[]... keys) {
        return jedis.sinter(keys);
    }

    @Override
    public Long sinterstore(byte[] dstkey, byte[]... keys) {
        return jedis.sinterstore(dstkey, keys);
    }

    @Override
    public Long smove(byte[] srckey, byte[] dstkey, byte[] member) {
        return jedis.smove(srckey, dstkey, member);
    }

    @Override
    public Long sort(byte[] key, SortingParams sortingParameters, byte[] dstkey) {
        return jedis.sort(key, sortingParameters, dstkey);
    }

    @Override
    public Long sort(byte[] key, byte[] dstkey) {
        return jedis.sort(key, dstkey);
    }

    @Override
    public Set<byte[]> sunion(byte[]... keys) {
        return jedis.sunion(keys);
    }

    @Override
    public Long sunionstore(byte[] dstkey, byte[]... keys) {
        return jedis.sunionstore(dstkey, keys);
    }

    @Override
    public Set<byte[]> zdiff(byte[]... keys) {
        return jedis.zdiff(keys);
    }

    @Override
    public Set<Tuple> zdiffWithScores(byte[]... keys) {
        return jedis.zdiffWithScores(keys);
    }

    @Override
    public Long zdiffStore(byte[] dstkey, byte[]... keys) {
        return jedis.zdiffStore(dstkey, keys);
    }

    @Override
    public Set<byte[]> zinter(ZParams params, byte[]... keys) {
        return jedis.zinter(params, keys);
    }

    @Override
    public Set<Tuple> zinterWithScores(ZParams params, byte[]... keys) {
        return jedis.zinterWithScores(params, keys);
    }

    @Override
    public Long zinterstore(byte[] dstkey, byte[]... sets) {
        return jedis.zinterstore(dstkey, sets);
    }

    @Override
    public Long zinterstore(byte[] dstkey, ZParams params, byte[]... sets) {
        return jedis.zinterstore(dstkey, params, sets);
    }

    @Override
    public Set<byte[]> zunion(ZParams params, byte[]... keys) {
        return jedis.zunion(params, keys);
    }

    @Override
    public Set<Tuple> zunionWithScores(ZParams params, byte[]... keys) {
        return jedis.zunionWithScores(params, keys);
    }

    @Override
    public Long zunionstore(byte[] dstkey, byte[]... sets) {
        return jedis.zunionstore(dstkey, sets);
    }

    @Override
    public Long zunionstore(byte[] dstkey, ZParams params, byte[]... sets) {
        return jedis.zunionstore(dstkey, params, sets);
    }

    @Override
    public byte[] brpoplpush(byte[] source, byte[] destination, int timeout) {
        return jedis.brpoplpush(source, destination, timeout);
    }

    @Override
    public Long publish(byte[] channel, byte[] message) {
        return jedis.publish(channel, message);
    }

    @Override
    public void subscribe(BinaryJedisPubSub jedisPubSub, byte[]... channels) {
        jedis.subscribe(jedisPubSub, channels);
    }

    @Override
    public void psubscribe(BinaryJedisPubSub jedisPubSub, byte[]... patterns) {
        jedis.psubscribe(jedisPubSub, patterns);
    }

    @Override
    public Long bitop(BitOP op, byte[] destKey, byte[]... srcKeys) {
        return jedis.bitop(op, destKey, srcKeys);
    }

    @Override
    public String pfmerge(byte[] destkey, byte[]... sourcekeys) {
        return jedis.pfmerge(destkey, sourcekeys);
    }

    @Override
    public Long pfcount(byte[]... keys) {
        return jedis.pfcount(keys);
    }

    @Override
    public Long touch(byte[]... keys) {
        return jedis.touch(keys);
    }

    @Override
    public ScanResult<byte[]> scan(byte[] cursor) {
        return jedis.scan(cursor);
    }

    @Override
    public ScanResult<byte[]> scan(byte[] cursor, ScanParams params) {
        return jedis.scan(cursor, params);
    }

    @Override
    public ScanResult<byte[]> scan(byte[] cursor, ScanParams params, byte[] type) {
        return jedis.scan(cursor, params, type);
    }

    @Override
    public Set<byte[]> keys(byte[] pattern) {
        return jedis.keys(pattern);
    }

    @Override
    public List<byte[]> xread(int count, long block, Map<byte[], byte[]> streams) {
        return jedis.xread(count, block, streams);
    }

    @Override
    public List<byte[]> xread(XReadParams xReadParams, Map.Entry<byte[], byte[]>... streams) {
        return jedis.xread(xReadParams, streams);
    }

    @Override
    public List<byte[]> xreadGroup(byte[] groupname, byte[] consumer, int count, long block, boolean noAck, Map<byte[], byte[]> streams) {
        return jedis.xreadGroup(groupname, consumer, count, block, noAck, streams);
    }

    @Override
    public List<byte[]> xreadGroup(byte[] groupname, byte[] consumer, XReadGroupParams xReadGroupParams, Map.Entry<byte[], byte[]>... streams) {
        return jedis.xreadGroup(groupname, consumer, xReadGroupParams, streams);
    }

    @Override
    public Long georadiusStore(byte[] key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param, GeoRadiusStoreParam storeParam) {
        return jedis.georadiusStore(key, longitude, latitude, radius, unit, param, storeParam);
    }

    @Override
    public Long georadiusByMemberStore(byte[] key, byte[] member, double radius, GeoUnit unit, GeoRadiusParam param, GeoRadiusStoreParam storeParam) {
        return jedis.georadiusByMemberStore(key, member, radius, unit, param, storeParam);
    }

    @Override
    public LCSMatchResult strAlgoLCSKeys(byte[] keyA, byte[] keyB, StrAlgoLCSParams params) {
        return jedis.strAlgoLCSKeys(keyA, keyB, params);
    }

    @Override
    public LCSMatchResult strAlgoLCSStrings(byte[] strA, byte[] strB, StrAlgoLCSParams params) {
        return jedis.strAlgoLCSStrings(strA, strB, params);
    }

    @Override
    public List<String> blpop(String... args) {
        return jedis.blpop(args);
    }

    @Override
    public List<String> brpop(String... args) {
        return jedis.brpop(args);
    }

    @Override
    public String watch(String... keys) {
        return jedis.watch(keys);
    }

    @Override
    public String unwatch() {
        return jedis.unwatch();
    }

    @Override
    public String randomKey() {
        return jedis.randomKey();
    }

    @Override
    public ScanResult<String> scan(String cursor) {
        return jedis.scan(cursor);
    }

    @Override
    public Boolean copy(String srcKey, String dstKey, int db, boolean replace) {
        return jedis.copy(srcKey, dstKey, db, replace);
    }

    @Override
    public Boolean copy(String srcKey, String dstKey, boolean replace) {
        return jedis.copy(srcKey, dstKey, replace);
    }

    @Override
    public Long del(String... keys) {
        return jedis.del(keys);
    }

    @Override
    public Long unlink(String... keys) {
        return jedis.del(keys);
    }

    @Override
    public Long exists(String... keys) {
        return jedis.exists(keys);
    }

    @Override
    public String lmove(String srcKey, String dstKey, ListDirection from, ListDirection to) {
        return jedis.lmove(srcKey, dstKey, from, to);
    }

    @Override
    public String blmove(String srcKey, String dstKey, ListDirection from, ListDirection to, double timeout) {
        return jedis.blmove(srcKey, dstKey, from, to, timeout);
    }

    @Override
    public List<String> blpop(int timeout, String... keys) {
        return jedis.blpop(timeout, keys);
    }

    @Override
    public KeyedListElement blpop(double timeout, String... keys) {
        return jedis.blpop(timeout, keys);
    }

    @Override
    public List<String> brpop(int timeout, String... keys) {
        return jedis.brpop(timeout, keys);
    }

    @Override
    public KeyedListElement brpop(double timeout, String... keys) {
        return jedis.brpop(timeout, keys);
    }

    @Override
    public KeyedZSetElement bzpopmax(double timeout, String... keys) {
        return jedis.bzpopmax(timeout, keys);
    }

    @Override
    public KeyedZSetElement bzpopmin(double timeout, String... keys) {
        return jedis.bzpopmin(timeout, keys);
    }

    @Override
    public List<String> mget(String... keys) {
        return jedis.mget(keys);
    }

    @Override
    public String mset(String... keysvalues) {
        return jedis.mset(keysvalues);
    }

    @Override
    public Long msetnx(String... keysvalues) {
        return jedis.msetnx(keysvalues);
    }

    @Override
    public String rename(String oldkey, String newkey) {
        return jedis.rename(oldkey, newkey);
    }

    @Override
    public Long renamenx(String oldkey, String newkey) {
        return jedis.renamenx(oldkey, newkey);
    }

    @Override
    public String rpoplpush(String srckey, String dstkey) {
        return jedis.rpoplpush(srckey, dstkey);
    }

    @Override
    public Set<String> sdiff(String... keys) {
        return jedis.sdiff(keys);
    }

    @Override
    public Long sdiffstore(String dstkey, String... keys) {
        return jedis.sdiffstore(dstkey, keys);
    }

    @Override
    public Set<String> sinter(String... keys) {
        return jedis.sinter(keys);
    }

    @Override
    public Long sinterstore(String dstkey, String... keys) {
        return jedis.sinterstore(dstkey, keys);
    }

    @Override
    public Long smove(String srckey, String dstkey, String member) {
        return jedis.smove(srckey, dstkey, member);
    }

    @Override
    public Long sort(String key, SortingParams sortingParameters, String dstkey) {
        return jedis.sort(key, sortingParameters, dstkey);
    }

    @Override
    public Long sort(String key, String dstkey) {
        return jedis.sort(key, dstkey);
    }

    @Override
    public Set<String> sunion(String... keys) {
        return jedis.sunion(keys);
    }

    @Override
    public Long sunionstore(String dstkey, String... keys) {
        return jedis.sunionstore(dstkey, keys);
    }

    @Override
    public Set<String> zdiff(String... keys) {
        return jedis.zdiff(keys);
    }

    @Override
    public Set<Tuple> zdiffWithScores(String... keys) {
        return jedis.zdiffWithScores(keys);
    }

    @Override
    public Long zdiffStore(String dstkey, String... keys) {
        return jedis.zdiffStore(dstkey, keys);
    }

    @Override
    public Set<String> zinter(ZParams params, String... keys) {
        return jedis.zinter(params, keys);
    }

    @Override
    public Set<Tuple> zinterWithScores(ZParams params, String... keys) {
        return jedis.zinterWithScores(params, keys);
    }

    @Override
    public Long zinterstore(String dstkey, String... sets) {
        return jedis.zinterstore(dstkey, sets);
    }

    @Override
    public Long zinterstore(String dstkey, ZParams params, String... sets) {
        return jedis.zinterstore(dstkey, params, sets);
    }

    @Override
    public Set<String> zunion(ZParams params, String... keys) {
        return jedis.zunion(params, keys);
    }

    @Override
    public Set<Tuple> zunionWithScores(ZParams params, String... keys) {
        return jedis.zunionWithScores(params, keys);
    }

    @Override
    public Long zunionstore(String dstkey, String... sets) {
        return jedis.zunionstore(dstkey, sets);
    }

    @Override
    public Long zunionstore(String dstkey, ZParams params, String... sets) {
        return jedis.zunionstore(dstkey, params, sets);
    }

    @Override
    public String brpoplpush(String source, String destination, int timeout) {
        return jedis.brpoplpush(source, destination, timeout);
    }

    @Override
    public Long publish(String channel, String message) {
        return jedis.publish(channel, message);
    }

    @Override
    public void subscribe(JedisPubSub jedisPubSub, String... channels) {
        jedis.subscribe(jedisPubSub, channels);
    }

    @Override
    public void psubscribe(JedisPubSub jedisPubSub, String... patterns) {
        jedis.psubscribe(jedisPubSub, patterns);
    }

    @Override
    public Long bitop(BitOP op, String destKey, String... srcKeys) {
        return jedis.bitop(op, destKey, srcKeys);
    }

    @Override
    public String pfmerge(String destkey, String... sourcekeys) {
        return jedis.pfmerge(destkey, sourcekeys);
    }

    @Override
    public long pfcount(String... keys) {
        return jedis.pfcount(keys);
    }

    @Override
    public Long touch(String... keys) {
        return jedis.touch(keys);
    }

    @SafeVarargs
    @Override
    public final List<Map.Entry<String, List<StreamEntry>>> xread(int count, long block, Map.Entry<String, StreamEntryID>... streams) {
        return jedis.xread(count, block, streams);
    }

    @Override
    public List<Map.Entry<String, List<StreamEntry>>> xread(XReadParams xReadParams, Map<String, StreamEntryID> streams) {
        return jedis.xread(xReadParams, streams);
    }

    @SafeVarargs
    @Override
    public final List<Map.Entry<String, List<StreamEntry>>> xreadGroup(String groupname, String consumer, int count, long block, boolean noAck, Map.Entry<String, StreamEntryID>... streams) {
        return jedis.xreadGroup(groupname, consumer, count, block, noAck, streams);
    }

    @Override
    public List<Map.Entry<String, List<StreamEntry>>> xreadGroup(String groupname, String consumer, XReadGroupParams xReadGroupParams, Map<String, StreamEntryID> streams) {
        return jedis.xreadGroup(groupname, consumer, xReadGroupParams, streams);
    }

    @Override
    public LCSMatchResult strAlgoLCSKeys(String keyA, String keyB, StrAlgoLCSParams params) {
        return jedis.strAlgoLCSKeys(keyA, keyB, params);
    }

    @Override
    public LCSMatchResult strAlgoLCSStrings(String strA, String strB, StrAlgoLCSParams params) {
        return jedis.strAlgoLCSStrings(strA, strB, params);
    }

    @Override
    public ScanResult<String> scan(String cursor, ScanParams params) {
        return jedis.scan(cursor, params);
    }

    @Override
    public ScanResult<String> scan(String cursor, ScanParams params, String type) {
        return jedis.scan(cursor, params, type);
    }

    @Override
    public Set<String> keys(String pattern) {
        return jedis.keys(pattern);
    }

    @Override
    public Long georadiusStore(String key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param, GeoRadiusStoreParam storeParam) {
        return jedis.georadiusStore(key, longitude, latitude, radius, unit, param, storeParam);
    }

    @Override
    public Long georadiusByMemberStore(String key, String member, double radius, GeoUnit unit, GeoRadiusParam param, GeoRadiusStoreParam storeParam) {
        return jedis.georadiusByMemberStore(key, member, radius, unit, param, storeParam);
    }

    @Override
    public String sentinelMyId() {
        return jedis.sentinelMyId();
    }

    @Override
    public List<Map<String, String>> sentinelMasters() {
        return jedis.sentinelMasters();
    }

    @Override
    public Map<String, String> sentinelMaster(String masterName) {
        return jedis.sentinelMaster(masterName);
    }

    @Override
    public List<Map<String, String>> sentinelSentinels(String masterName) {
        return jedis.sentinelSentinels(masterName);
    }

    @Override
    public List<String> sentinelGetMasterAddrByName(String masterName) {
        return jedis.sentinelGetMasterAddrByName(masterName);
    }

    @Override
    public Long sentinelReset(String pattern) {
        return jedis.sentinelReset(pattern);
    }

    @Override
    public List<Map<String, String>> sentinelSlaves(String masterName) {
        return jedis.sentinelSlaves(masterName);
    }

    @Override
    public List<Map<String, String>> sentinelReplicas(String masterName) {
        return jedis.sentinelReplicas(masterName);
    }

    @Override
    public String sentinelFailover(String masterName) {
        return jedis.sentinelFailover(masterName);
    }

    @Override
    public String sentinelMonitor(String masterName, String ip, int port, int quorum) {
        return jedis.sentinelMonitor(masterName, ip, port, quorum);
    }

    @Override
    public String sentinelRemove(String masterName) {
        return jedis.sentinelRemove(masterName);
    }

    @Override
    public String sentinelSet(String masterName, Map<String, String> parameterMap) {
        return jedis.sentinelSet(masterName, parameterMap);
    }

    @Override
    public boolean isCluster() {
        return false;
    }

    @Override
    public boolean isClosed() {
        return isClosed;
    }
}
