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
package net.ymate.platform.serv.impl;

import net.ymate.platform.commons.util.DateTimeUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.serv.AbstractService;
import net.ymate.platform.serv.IClient;
import net.ymate.platform.serv.IReconnectService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/19 下午3:06
 */
public class DefaultReconnectServiceImpl extends AbstractService implements IReconnectService {

    private static final Log LOG = LogFactory.getLog(DefaultReconnectServiceImpl.class);

    private final AtomicLong counter = new AtomicLong(0);

    private IClient<?, ?> client;

    private long timeout;

    @Override
    public void initialize(IClient<?, ?> client) {
        this.client = client;
        doInit();
    }

    @Override
    protected boolean doStart() {
        setName(String.format("%sClient-ReconnectService", StringUtils.capitalize(StringUtils.defaultIfBlank(client.clientCfg().getClientName(), client.listener().getClass().getSimpleName()))));
        int interval = client.clientCfg().getReconnectionInterval();
        if (interval > 0) {
            timeout = interval * DateTimeUtils.SECOND;
        } else {
            timeout = DateTimeUtils.SECOND;
        }
        return super.doStart();
    }

    @Override
    protected void doService() {
        try {
            if (!client.isClosing()) {
                if (!client.isConnected() && counter.getAndIncrement() > 0) {
                    client.listener().onClientReconnected(client);
                    client.reconnect();
                    //
                    counter.set(0);
                } else {
                    sleep(timeout);
                }
            }
        } catch (IOException | InterruptedException e) {
            if (isStarted() && LOG.isErrorEnabled()) {
                LOG.error(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
            }
        }
    }
}
