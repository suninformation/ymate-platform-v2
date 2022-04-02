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
package net.ymate.platform.serv;

import net.ymate.platform.commons.util.DateTimeUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

/**
 * @param <HEARTBEAT_TYPE> 心跳包类型
 * @author 刘镇 (suninformation@163.com) on 2018/11/6 3:24 PM
 */
public abstract class AbstractHeartbeatService<HEARTBEAT_TYPE> extends AbstractService implements IHeartbeatService<HEARTBEAT_TYPE> {

    private static final Log LOG = LogFactory.getLog(AbstractHeartbeatService.class);

    private IClient<?, ?> client;

    private long heartbeatInterval;

    protected IClient<?, ?> getClient() {
        return client;
    }

    @Override
    public void initialize(IClient<?, ?> client) {
        this.client = client;
        doInit();
    }

    @Override
    protected boolean doStart() {
        setName(String.format("%sClient-HeartbeatService", StringUtils.capitalize(StringUtils.defaultIfBlank(client.clientCfg().getClientName(), client.listener().getClass().getSimpleName()))));
        int interval = client.clientCfg().getHeartbeatInterval();
        if (interval > 0) {
            heartbeatInterval = interval * DateTimeUtils.SECOND;
        } else {
            heartbeatInterval = 60000L;
        }
        return super.doStart();
    }

    @Override
    protected void doService() {
        try {
            if (!client.isClosing()) {
                if (client.isConnected()) {
                    HEARTBEAT_TYPE heartbeatObj = getHeartbeatPacket();
                    if (heartbeatObj != null) {
                        client.send(heartbeatObj);
                    }
                }
                sleep(heartbeatInterval);
            }
        } catch (IOException | InterruptedException e) {
            if (isStarted() && LOG.isErrorEnabled()) {
                LOG.error(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
            }
        }
    }
}
