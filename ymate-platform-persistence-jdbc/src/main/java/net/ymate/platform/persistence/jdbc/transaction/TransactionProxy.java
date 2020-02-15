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

import net.ymate.platform.core.beans.annotation.Order;
import net.ymate.platform.core.beans.proxy.IProxy;
import net.ymate.platform.core.beans.proxy.IProxyChain;
import net.ymate.platform.core.persistence.AbstractTrade;
import net.ymate.platform.core.persistence.annotation.Transaction;
import net.ymate.platform.core.persistence.base.Type;

/**
 * JDBC数据库事务代理
 *
 * @author 刘镇 (suninformation@163.com) on 15/4/29 上午10:07
 */
@Order(60000)
public class TransactionProxy implements IProxy {

    @Override
    public Object doProxy(final IProxyChain proxyChain) throws Throwable {
        Type.TRANSACTION currentLevel = null;
        // 判断方法对象是否被声明@Transaction注解，否则忽略
        if (proxyChain.getTargetMethod().isAnnotationPresent(Transaction.class)) {
            // 获取当前类声明的全局事务级别参数
            currentLevel = proxyChain.getTargetClass().getAnnotation(Transaction.class).value();
            //
            Type.TRANSACTION tmpLevel = proxyChain.getTargetMethod().getAnnotation(Transaction.class).value();
            // 如果全局事务级别被设置或低于NONE，则分析targetMethod是否存在@Transaction注解声明并尝试获取其事务级别设置
            if (currentLevel.compareTo(Type.TRANSACTION.NONE) > 0) {
                currentLevel = tmpLevel;
            }
        }
        // 如果事务级别非空，则开启事务
        if (currentLevel != null) {
            return Transactions.execute(currentLevel, new AbstractTrade<Object>() {
                @Override
                public Object dealing() throws Throwable {
                    return proxyChain.doProxyChain();
                }
            });
        } else {
            return proxyChain.doProxyChain();
        }
    }
}
