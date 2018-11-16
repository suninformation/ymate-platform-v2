/*
 * Copyright 2007-2017 the original author or authors.
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
package net.ymate.platform.serv.impl;

import net.ymate.platform.core.util.DateTimeUtils;
import net.ymate.platform.core.util.RuntimeUtils;
import net.ymate.platform.serv.AbstractService;
import net.ymate.platform.serv.IClient;
import net.ymate.platform.serv.IReconnectService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/19 下午3:06
 * @version 1.0
 */
public class DefaultReconnectService extends AbstractService implements IReconnectService {

    private static final Log _LOG = LogFactory.getLog(DefaultReconnectService.class);

    private static final AtomicLong __COUNTER = new AtomicLong(0);

    private IClient __client;

    private long __timeout;

    @Override
    public void init(IClient client) {
        __client = client;
        __doInit();
    }

    @Override
    protected boolean __doStart() {
        setName("ReconnectService-" + __client.listener().getClass().getSimpleName());
        if (__client.clientCfg().getConnectionTimeout() > 0) {
            __timeout = __client.clientCfg().getConnectionTimeout() * DateTimeUtils.SECOND;
        } else {
            __timeout = 5000L;
        }
        return super.__doStart();
    }

    @Override
    protected void __doService() {
        try {
            if (!__client.isConnected() && __COUNTER.getAndIncrement() > 0) {
                __client.reconnect();
                //
                __COUNTER.set(0);
            } else {
                sleep(__timeout);
            }
        } catch (Exception e) {
            if (isStarted()) {
                _LOG.error(e.getMessage(), RuntimeUtils.unwrapThrow(e));
            }
        }
    }
}
