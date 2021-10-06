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
package net.ymate.platform.persistence.mongodb.transaction;

import com.mongodb.ClientSessionOptions;
import com.mongodb.client.ClientSession;
import net.ymate.platform.commons.lang.BlurObject;
import net.ymate.platform.persistence.mongodb.IMongoConnectionHolder;
import net.ymate.platform.persistence.mongodb.transaction.impl.DefaultTransaction;

/**
 * @author 刘镇 (suninformation@163.com) on 2021/10/16 9:36 下午
 * @since 2.1.0
 */
public final class Transactions {

    private static final ThreadLocal<ITransaction> TRANS_LOCAL = new ThreadLocal<>();

    private static final ThreadLocal<Integer> COUNT = new ThreadLocal<>();

    public static ITransaction get() {
        return TRANS_LOCAL.get();
    }

    public static ClientSession getClientSession(IMongoConnectionHolder connectionHolder) {
        ITransaction transaction = get();
        if (transaction != null) {
            return transaction.getClientSession(connectionHolder.getDataSourceConfig().getName());
        }
        return null;
    }

    public static void create(IMongoConnectionHolder connectionHolder, ClientSessionOptions clientSessionOptions) throws Exception {
        ITransaction transaction = get();
        if (transaction == null) {
            transaction = new DefaultTransaction(connectionHolder, clientSessionOptions);
            TRANS_LOCAL.set(transaction);
            COUNT.set(0);
        }
        COUNT.set(COUNT.get() + 1);
    }

    public static void commit() {
        if (COUNT.get() > 0) {
            COUNT.set(COUNT.get() - 1);
        }
        if (COUNT.get() == 0) {
            ITransaction transaction = TRANS_LOCAL.get();
            if (transaction != null) {
                transaction.commit();
            }
        }
    }

    public static void rollback() {
        int number = BlurObject.bind(COUNT.get()).toIntValue();
        COUNT.set(number);
        if (COUNT.get() == 0) {
            ITransaction transaction = TRANS_LOCAL.get();
            if (transaction != null) {
                transaction.rollback();
            }
        } else {
            COUNT.set(COUNT.get() - 1);
        }
    }

    public static void close() {
        if (COUNT.get() != null && COUNT.get() == 0) {
            try {
                ITransaction transaction = TRANS_LOCAL.get();
                if (transaction != null) {
                    transaction.close();
                }
            } finally {
                TRANS_LOCAL.remove();
                COUNT.remove();
            }
        }
    }
}
