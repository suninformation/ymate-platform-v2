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
package net.ymate.platform.serv.nio.server;

import net.ymate.platform.serv.IServerCfg;
import net.ymate.platform.serv.nio.AbstractNioServer;
import net.ymate.platform.serv.nio.INioCodec;
import net.ymate.platform.serv.nio.INioEventGroup;
import net.ymate.platform.serv.nio.support.NioEventGroup;

import java.io.IOException;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/15 下午6:22
 */
public class NioServer extends AbstractNioServer<NioServerListener> {

    @Override
    protected INioEventGroup<NioServerListener> buildEventGroup(IServerCfg serverCfg, NioServerListener listener, INioCodec codec) throws IOException {
        return new NioEventGroup<>(serverCfg, listener, codec);
    }
}
