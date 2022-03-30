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

import net.ymate.platform.serv.AbstractHeartbeatService;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/19 下午3:07
 */
public class DefaultHeartbeatServiceImpl extends AbstractHeartbeatService<String> {

    private String heartbeatPacket;

    @Override
    protected boolean doStart() {
        heartbeatPacket = getClient().clientCfg().getParam(HEARTBEAT_PACKET, "0");
        return super.doStart();
    }

    @Override
    public String getHeartbeatPacket() {
        return heartbeatPacket;
    }
}
