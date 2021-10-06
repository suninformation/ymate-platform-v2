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
package net.ymate.platform.persistence.mongodb.transaction.impl;

import com.mongodb.ClientSessionOptions;
import com.mongodb.TransactionOptions;
import com.mongodb.client.ClientSession;
import net.ymate.platform.commons.util.UUIDUtils;
import net.ymate.platform.persistence.mongodb.IMongoConnectionHolder;
import net.ymate.platform.persistence.mongodb.transaction.ITransaction;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2021/10/17 12:07 上午
 * @since 2.1.0
 */
public class DefaultTransaction implements ITransaction {

    private final String id = UUIDUtils.UUID();

    private final Map<String, TransactionMeta> transactionMetas = new HashMap<>(16);

    public DefaultTransaction(IMongoConnectionHolder connectionHolder, ClientSessionOptions clientSessionOptions) throws Exception {
        String dsName = connectionHolder.getDataSourceConfig().getName();
        TransactionMeta transactionMeta = transactionMetas.get(dsName);
        if (transactionMeta == null) {
            TransactionOptions transactionOptions = null;
            if (clientSessionOptions != null) {
                transactionOptions = clientSessionOptions.getDefaultTransactionOptions();
            }
            if (transactionOptions == null) {
                transactionOptions = TransactionOptions.builder().build();
            }
            transactionMeta = new TransactionMeta(connectionHolder, clientSessionOptions);
            transactionMeta.getClientSession().startTransaction(transactionOptions);
            //
            transactionMetas.put(dsName, transactionMeta);
        }
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void commit() {
        transactionMetas.values().stream().filter(meta -> meta.getClientSession().hasActiveTransaction()).forEach(meta -> meta.getClientSession().commitTransaction());
    }

    @Override
    public void rollback() {
        transactionMetas.values().stream().filter(meta -> meta.getClientSession().hasActiveTransaction()).forEach(meta -> meta.getClientSession().abortTransaction());
    }

    @Override
    public void close() {
        transactionMetas.values().forEach(meta -> meta.getClientSession().close());
    }

    @Override
    public ClientSession getClientSession(String dsName) {
        TransactionMeta transactionMeta = transactionMetas.get(dsName);
        if (transactionMeta != null) {
            return transactionMeta.getClientSession();
        }
        return null;
    }

    @Override
    public IMongoConnectionHolder getConnectionHolder(String dsName) {
        TransactionMeta transactionMeta = transactionMetas.get(dsName);
        if (transactionMeta != null) {
            return transactionMeta.getConnectionHolder();
        }
        return null;
    }

    private static class TransactionMeta {

        private final IMongoConnectionHolder connectionHolder;

        private final ClientSession clientSession;

        public TransactionMeta(IMongoConnectionHolder connectionHolder, ClientSessionOptions clientSessionOptions) throws Exception {
            this.connectionHolder = connectionHolder;
            this.clientSession = connectionHolder.getDataSourceAdapter().getConnection().startSession(clientSessionOptions != null ? clientSessionOptions : ClientSessionOptions.builder().build());
        }

        public IMongoConnectionHolder getConnectionHolder() {
            return connectionHolder;
        }

        public ClientSession getClientSession() {
            return clientSession;
        }
    }
}
