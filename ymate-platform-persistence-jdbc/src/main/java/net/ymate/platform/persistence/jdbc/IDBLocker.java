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
package net.ymate.platform.persistence.jdbc;

import net.ymate.platform.core.beans.annotation.Ignored;

/**
 * 数据表/记录锁
 *
 * @author 刘镇 (suninformation@163.com) on 16/1/5 下午1:08
 */
@Ignored
public interface IDBLocker {

    IDBLocker DEFAULT = () -> " FOR UPDATE";

    IDBLocker NOWAIT = () -> "FOR UPDATE NOWAIT";

    /**
     * SQLServer数据库锁
     */
    class SQLServer implements IDBLocker {

        /**
         * 锁类型
         */
        enum SQLServerLockType {

            /**
             * 不加锁
             */
            NOLOCK,

            /**
             * 保持锁
             */
            HOLDLOCK,

            /**
             * 修改锁
             */
            UPDLOCK,

            /**
             * 表锁
             */
            TABLOCK,

            /**
             * 页锁
             */
            PAGLOCK,

            /**
             * 排它表锁
             */
            TABLOCKX
        }

        /**
         * 不加锁
         * <p>
         * 在读取或修改数据时不加任何锁。
         * 在这种情况下，用户有可能读取到未完成事务（Uncommited Transaction）或回滚(Roll Back)中的数据, 即所谓的“脏数据”
         */
        public static IDBLocker NOLOCK = new SQLServer(SQLServerLockType.NOLOCK);

        /**
         * 保持锁
         * <p>
         * 会将此共享锁保持至整个事务结束，而不会在途中释放。
         */
        public static IDBLocker HOLDLOCK = new SQLServer(SQLServerLockType.HOLDLOCK);

        /**
         * 修改锁
         * <p>
         * 在读取数据时使用修改锁来代替共享锁，并将此锁保持至整个事务或命令结束。
         * 使用此选项能够保证多个进程能同时读取数据但只有该进程能修改数据。
         */
        public static IDBLocker UPDLOCK = new SQLServer(SQLServerLockType.UPDLOCK);

        /**
         * 表锁
         * <p>
         * 将在整个表上置共享锁直至该命令结束。 这个选项保证其他进程只能读取而不能修改数据。
         */
        public static IDBLocker TABLOCK = new SQLServer(SQLServerLockType.TABLOCK);

        /**
         * 页锁
         */
        public static IDBLocker PAGLOCK = new SQLServer(SQLServerLockType.PAGLOCK);

        /**
         * 排它表锁
         * <p>
         * 将在整个表上置排它锁直至该命令或事务结束。这将防止其他进程读取或修改表中的数据。
         */
        public static IDBLocker TABLOCKX = new SQLServer(SQLServerLockType.TABLOCKX);

        private final SQLServerLockType lockType;

        SQLServer(SQLServerLockType lockType) {
            this.lockType = lockType;
        }

        @Override
        public String toSQL() {
            return String.format("WITH (%s)", lockType.name());
        }
    }

    /**
     * 输出SQL语句
     *
     * @return 返回生成的SQL语句字符串
     */
    String toSQL();
}
