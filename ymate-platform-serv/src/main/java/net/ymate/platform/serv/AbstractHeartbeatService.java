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
package net.ymate.platform.serv;

import net.ymate.platform.core.util.DateTimeUtils;
import net.ymate.platform.core.util.RuntimeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/11/6 3:24 PM
 * @version 1.0
 */
public abstract class AbstractHeartbeatService<HEARTBEAT_TYPE> extends AbstractService implements IHeartbeatService<HEARTBEAT_TYPE> {

    private static final Log _LOG = LogFactory.getLog(AbstractHeartbeatService.class);

    private IClient __client;

    private long __heartbeatInterval;

    protected IClient getClient() {
        return __client;
    }

    @Override
    public void init(IClient client) {
        __client = client;
        __doInit();
    }

    @Override
    protected boolean __doStart() {
        setName("HeartbeatService-" + __client.listener().getClass().getSimpleName());
        if (__client.clientCfg().getHeartbeatInterval() > 0) {
            __heartbeatInterval = __client.clientCfg().getHeartbeatInterval() * DateTimeUtils.SECOND;
        } else {
            __heartbeatInterval = 60000L;
        }
        return super.__doStart();
    }

    @Override
    protected void __doService() {
        try {
            if (__client.isConnected()) {
                __client.send(getHeartbeatPacket());
            }
            sleep(__heartbeatInterval);
        } catch (Exception e) {
            if (isStarted()) {
                _LOG.error(e.getMessage(), RuntimeUtils.unwrapThrow(e));
            }
        }
    }
}
