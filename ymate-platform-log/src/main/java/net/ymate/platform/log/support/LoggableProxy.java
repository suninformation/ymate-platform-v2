/*
 * Copyright 2007-2018 the original author or authors.
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
package net.ymate.platform.log.support;

import net.ymate.platform.core.beans.annotation.Order;
import net.ymate.platform.core.beans.annotation.Proxy;
import net.ymate.platform.core.beans.proxy.IProxy;
import net.ymate.platform.core.beans.proxy.IProxyChain;
import net.ymate.platform.log.annotation.Loggable;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/1/7 下午6:34
 * @version 1.0
 */
@Proxy(annotation = Loggable.class, order = @Order(-2999))
public class LoggableProxy implements IProxy {

    @Override
    public Object doProxy(IProxyChain proxyChain) throws Throwable {
        try {
            if (proxyChain.getTargetMethod().isAnnotationPresent(Loggable.class)) {
                Logoo.createIfNeed(proxyChain.getTargetClass().getAnnotation(Loggable.class), proxyChain.getTargetMethod().getAnnotation(Loggable.class));
            }
            return proxyChain.doProxyChain();
        } catch (Throwable e) {
            Logoo.clean();
            throw e;
        } finally {
            Logoo.release();
        }
    }
}
