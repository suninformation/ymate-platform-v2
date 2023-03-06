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
package net.ymate.platform.persistence.jdbc.transaction;

import net.ymate.platform.commons.lang.BlurObject;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.persistence.AbstractTrade;
import net.ymate.platform.core.persistence.ITrade;
import net.ymate.platform.core.persistence.base.Type;
import net.ymate.platform.persistence.jdbc.transaction.impl.DefaultTransaction;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 事务管理器类，用于执行基于JDBC事务的相关操作；<br>
 * 注：支持事务模板的无限层级嵌套，如果每一层嵌套，指定的事务级别有所不同，不同的数据库，可能引发不可预知的错误。
 * 所以嵌套的事务将以最顶层的事务级别为标准，就是说，如果最顶层的事务级别为'TRANSACTION_READ_COMMITTED'，
 * 那么下面所包含的所有事务，无论你指定什么样的事务级别，都是'TRANSACTION_READ_COMMITTED'，
 * 这一点由ITransaction接口实现类来保证，其setLevel当被设置了一个大于0的整数以后，将不再接受任何其他的值。
 *
 * @author 刘镇 (suninformation@163.com) on 2011-9-6 下午04:36:53
 */
public final class Transactions {

    private static final Log LOG = LogFactory.getLog(Transactions.class);

    private static Class<? extends ITransaction> transactionClass;

    static {
        try {
            transactionClass = ClassUtils.getExtensionLoader(ITransaction.class).getExtensionClass();
        } catch (Exception e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
            }
        }
        if (transactionClass == null) {
            transactionClass = DefaultTransaction.class;
        }
        if (LOG.isInfoEnabled()) {
            LOG.info(String.format("Using Transaction class [%s].", transactionClass.getName()));
        }
    }

    private static final ThreadLocal<ITransaction> TRANS_LOCAL = new ThreadLocal<>();

    private static final ThreadLocal<Integer> COUNT = new ThreadLocal<>();

    /**
     * @return 返回当前线程的事务对象，如果不存在事务则返回null
     */
    public static ITransaction get() {
        return TRANS_LOCAL.get();
    }

    /**
     * 开始事务
     *
     * @param level 事务级别
     * @throws Exception 可能产生的异常
     */
    private static void begin(Type.TRANSACTION level) throws Exception {
        ITransaction transaction = TRANS_LOCAL.get();
        int count;
        if (transaction == null) {
            transaction = transactionClass.newInstance();
            transaction.setLevel(level);
            TRANS_LOCAL.set(transaction);
            count = 0;
        } else {
            count = BlurObject.bind(COUNT.get()).toIntValue();
        }
        COUNT.set(count + 1);
    }

    /**
     * 提交事务
     *
     * @throws Exception 可能产生的异常
     */
    private static void commit() throws Exception {
        int count = BlurObject.bind(COUNT.get()).toIntValue();
        if (count > 0) {
            count--;
            COUNT.set(count);
        }
        if (count == 0) {
            ITransaction transaction = TRANS_LOCAL.get();
            if (transaction != null) {
                transaction.commit();
            }
        }
    }

    /**
     * 回滚事务
     *
     * @param number 事务层级计数
     * @throws Exception 可能产生的异常
     */
    private static void rollback(int number) throws Exception {
        COUNT.set(number);
        if (number == 0) {
            ITransaction transaction = TRANS_LOCAL.get();
            if (transaction != null) {
                transaction.rollback();
            }
        } else {
            COUNT.set(number - 1);
        }
    }

    /**
     * 关闭事务
     *
     * @throws Exception 可能产生的异常
     */
    private static void close() throws Exception {
        Integer count = COUNT.get();
        if (count != null && count == 0) {
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

    /**
     * 执行一组事务操作，事务级别默认采用ITransaction.Level.READ_COMMITTED
     *
     * @param trades 事务业务操作对象集合
     * @throws Exception 可能产生的异常
     */
    public static void execute(ITrade... trades) throws Exception {
        execute(Type.TRANSACTION.READ_COMMITTED, trades);
    }

    /**
     * 执行一组由level参数指定事务级别的事务操作
     *
     * @param level  事务级别
     * @param trades 事务业务操作对象集合
     * @throws Exception 可能产生的异常
     */
    public static void execute(Type.TRANSACTION level, ITrade... trades) throws Exception {
        int number = BlurObject.bind(COUNT.get()).toIntValue();
        try {
            begin(level);
            for (ITrade trade : trades) {
                trade.deal();
            }
            commit();
        } catch (Throwable e) {
            try {
                rollback(number);
            } catch (Exception ignored) {
            }
            if (e instanceof Exception) {
                throw (Exception) e;
            } else {
                throw new Exception(RuntimeUtils.unwrapThrow(e));
            }
        } finally {
            try {
                close();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * @param trade 事务业务操作对象
     * @param <T>   返回值类型
     * @return 执行一个有返回值的事务操作，事务级别默认采用ITransaction.Level.READ_COMMITTED
     * @throws Exception 可能产生的异常
     */
    public static <T> T execute(AbstractTrade<T> trade) throws Exception {
        return execute(Type.TRANSACTION.READ_COMMITTED, trade);
    }

    /**
     * @param level 事务级别
     * @param trade 事务业务操作对象
     * @param <T>   返回值类型
     * @return 执行一个由level参数指定事务级别的有返回值的事务操作
     * @throws Exception 可能产生的异常
     */
    public static <T> T execute(Type.TRANSACTION level, AbstractTrade<T> trade) throws Exception {
        int number = BlurObject.bind(COUNT.get()).toIntValue();
        try {
            begin(level);
            trade.deal();
            commit();
            return trade.getResult();
        } catch (Throwable e) {
            try {
                rollback(number);
            } catch (Exception ignored) {
            }
            if (e instanceof Exception) {
                throw (Exception) e;
            } else {
                throw new Exception(RuntimeUtils.unwrapThrow(e));
            }
        } finally {
            try {
                close();
            } catch (Exception ignored) {
            }
        }
    }
}
