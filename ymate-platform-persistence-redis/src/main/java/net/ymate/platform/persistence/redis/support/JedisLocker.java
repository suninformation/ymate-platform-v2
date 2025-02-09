/*
 * Copyright 2007-2025 the original author or authors.
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

import net.ymate.platform.commons.util.DateTimeUtils;
import net.ymate.platform.commons.util.UUIDUtils;
import net.ymate.platform.persistence.redis.IRedis;
import net.ymate.platform.persistence.redis.IRedisCommander;
import net.ymate.platform.persistence.redis.IRedisConfig;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.params.SetParams;

import java.util.Collections;

/**
 * 参考自：<a href="https://github.com/kaidul/jedis-lock/blob/master/src/main/java/com/github/jedis/lock/JedisLock.java">github.com/kaidul/jedis-lock</a>
 *
 * @author 刘镇 (suninformation@163.com) on 2025/1/11 00:25
 * @since 2.1.4
 */
public class JedisLocker {

    private final static String DELETE_IF_OWNED_LUA_SNIPPET =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "return redis.call('del', KEYS[1]) " +
                    "else " +
                    "return 0 " +
                    "end";

    private final IRedis owner;

    private final String dataSourceName;

    private final String uuid;

    private final long acquireTimeoutMillis;

    private final long expiryInMillis;

    private final int resolutionMillis;

    private boolean readLocked;

    private boolean writeLocked;

    public JedisLocker(IRedis owner) {
        this(owner, null, 0, 0, 0);
    }

    public JedisLocker(IRedis owner, String dataSourceName) {
        this(owner, dataSourceName, 0, 0, 0);
    }

    public JedisLocker(IRedis owner, String dataSourceName, long acquireTimeoutMillis, long expiryInMillis, int resolutionMillis) {
        this.owner = owner;
        this.dataSourceName = StringUtils.defaultIfBlank(dataSourceName, IRedisConfig.DEFAULT_STR);
        this.uuid = UUIDUtils.UUID();
        this.acquireTimeoutMillis = acquireTimeoutMillis > 0 ? acquireTimeoutMillis : Long.getLong("ymp.jedisLocker.acquireTimeoutMillis", 10 * DateTimeUtils.SECOND);
        this.expiryInMillis = expiryInMillis > 0 ? expiryInMillis : Long.getLong("ymp.jedisLocker.expiryMillis", 60 * DateTimeUtils.SECOND);
        this.resolutionMillis = resolutionMillis > 0 ? resolutionMillis : Integer.getInteger("ymp.jedisLocker.resolutionMillis", 100);
    }

    private boolean doRenew(Object key, long timeout, boolean forWrite) throws Exception {
        if ((forWrite ? !writeLocked : !readLocked) || doCheckRemoteLocked(key, forWrite)) {
            return false;
        }
        try (IRedisCommander commander = owner.getConnectionHolder(dataSourceName).getConnection()) {
            while (timeout >= 0) {
                if ("OK".equals(commander.set(doBuildKey(key, forWrite), uuid, SetParams.setParams().xx().px(expiryInMillis + 1)))) {
                    return true;
                }
                timeout -= resolutionMillis;
                Thread.sleep(resolutionMillis);
            }
        }
        return false;
    }

    private boolean doGetLock(Object key, long timeout, boolean forWrite) throws Exception {
        if (timeout <= 0) {
            timeout = acquireTimeoutMillis;
        }
        if (forWrite ? writeLocked : readLocked) {
            return doRenew(key, timeout, forWrite);
        }
        try (IRedisCommander commander = owner.getConnectionHolder(dataSourceName).getConnection()) {
            while (timeout >= 0) {
                if ("OK".equals(commander.set(doBuildKey(key, forWrite), uuid, SetParams.setParams().nx().px(expiryInMillis + 1)))) {
                    return forWrite ? (this.writeLocked = true) : (this.readLocked = true);
                }
                timeout -= resolutionMillis;
                Thread.sleep(resolutionMillis);
            }
        }
        return false;
    }

    private void doReleaseLock(Object key, boolean forWrite) throws Exception {
        if (forWrite ? writeLocked : readLocked) {
            try (IRedisCommander commander = owner.getConnectionHolder(dataSourceName).getConnection()) {
                commander.eval(DELETE_IF_OWNED_LUA_SNIPPET, Collections.singletonList(doBuildKey(key, forWrite)), Collections.singletonList(uuid));
                if (forWrite) {
                    writeLocked = false;
                } else {
                    readLocked = false;
                }
            }
        }
    }

    private boolean doCheckRemoteLocked(Object key, boolean forWrite) throws Exception {
        if (forWrite ? writeLocked : readLocked) {
            return false;
        }
        try (IRedisCommander commander = owner.getConnectionHolder(dataSourceName).getConnection()) {
            return commander.get(doBuildKey(key, forWrite)) != null;
        }
    }

    private String doBuildKey(Object key, boolean forWrite) {
        return String.format("%s_%s_lock", key.toString(), forWrite ? "write" : "read");
    }

    public synchronized void readLock(Object key) throws Exception {
        doGetLock(key, acquireTimeoutMillis, false);
    }

    public synchronized void writeLock(Object key) throws Exception {
        doGetLock(key, acquireTimeoutMillis, true);
    }

    public synchronized boolean tryReadLock(Object key, long timeout) throws Exception {
        return doGetLock(key, timeout, false);
    }

    public synchronized boolean tryWriteLock(Object key, long timeout) throws Exception {
        return doGetLock(key, timeout, true);
    }

    public synchronized void releaseReadLock(Object key) throws Exception {
        doReleaseLock(key, false);
    }

    public synchronized void releaseWriteLock(Object key) throws Exception {
        doReleaseLock(key, true);
    }

    // ---

    public IRedis getOwner() {
        return owner;
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

    public String getUuid() {
        return uuid;
    }

    public long getAcquireTimeoutMillis() {
        return acquireTimeoutMillis;
    }

    public long getExpiryInMillis() {
        return expiryInMillis;
    }

    public int getResolutionMillis() {
        return resolutionMillis;
    }

    public boolean isReadLocked() {
        return readLocked;
    }

    public boolean isWriteLocked() {
        return writeLocked;
    }
}
