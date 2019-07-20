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
package net.ymate.platform.persistence.jdbc.transaction.impl;

import net.ymate.platform.commons.util.UUIDUtils;
import net.ymate.platform.core.persistence.base.Type;
import net.ymate.platform.persistence.jdbc.IDatabaseConnectionHolder;
import net.ymate.platform.persistence.jdbc.transaction.ITransaction;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * 默认JDBC事务处理接口实现
 *
 * @author 刘镇 (suninformation@163.com) on 2011-9-6 下午04:43:45
 */
public final class DefaultTransaction implements ITransaction {

    private final String id = UUIDUtils.UUID();

    private Type.TRANSACTION level;

    private Map<String, TransactionMeta> transMetas = new HashMap<>(16);

    public DefaultTransaction() {
    }

    public DefaultTransaction(Type.TRANSACTION level) {
        this.setLevel(level);
    }

    @Override
    public Type.TRANSACTION getLevel() {
        return level;
    }

    @Override
    public void setLevel(Type.TRANSACTION level) {
        if (level != null) {
            if (this.level == null || this.level.getLevel() <= 0) {
                this.level = level;
            }
        }
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void commit() throws SQLException {
        for (TransactionMeta meta : this.transMetas.values()) {
            meta.connectionHolder.getConnection().commit();
        }
    }

    @Override
    public void rollback() throws SQLException {
        for (TransactionMeta meta : this.transMetas.values()) {
            meta.connectionHolder.getConnection().rollback();
        }
    }

    @Override
    public void close() throws SQLException {
        try {
            for (TransactionMeta meta : this.transMetas.values()) {
                meta.release();
            }
        } finally {
            this.transMetas = null;
        }
    }

    @Override
    public IDatabaseConnectionHolder getConnectionHolder(String dsName) {
        if (this.transMetas.containsKey(dsName)) {
            return this.transMetas.get(dsName).connectionHolder;
        }
        return null;
    }

    @Override
    public void registerConnectionHolder(IDatabaseConnectionHolder connectionHolder) throws SQLException {
        String dsName = connectionHolder.getDataSourceConfig().getName();
        if (!this.transMetas.containsKey(dsName)) {
            this.transMetas.put(dsName, new TransactionMeta(connectionHolder, getLevel()));
        }
    }

    /**
     * 事务信息描述对象
     *
     * @author 刘镇 (suninformation@163.com) on 2010-10-16 下午03:50:01
     */
    private static class TransactionMeta {

        /**
         * 数据库连接持有者对象
         */
        IDatabaseConnectionHolder connectionHolder;

        /**
         * 构造器
         *
         * @param connectionHolder 数据库连接持有者对象
         * @param initLevel        初始事务级别
         * @throws SQLException 可能产生的异常
         */
        TransactionMeta(IDatabaseConnectionHolder connectionHolder, Type.TRANSACTION initLevel) throws SQLException {
            this.connectionHolder = connectionHolder;
            if (this.connectionHolder.getConnection().getAutoCommit()) {
                this.connectionHolder.getConnection().setAutoCommit(false);
            }
            if (initLevel != null) {
                if (initLevel.getLevel() != connectionHolder.getConnection().getTransactionIsolation()) {
                    this.connectionHolder.getConnection().setTransactionIsolation(initLevel.getLevel());
                }
            }
        }

        /**
         * 释放数据源、连接资源
         */
        void release() throws SQLException {
            try {
                if (this.connectionHolder != null) {
                    try {
                        this.connectionHolder.close();
                    } catch (Exception e) {
                        if (e instanceof SQLException) {
                            throw (SQLException) e;
                        } else {
                            throw new SQLException(e.getMessage(), e);
                        }
                    }
                }
            } finally {
                this.connectionHolder = null;
            }
        }
    }
}
