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
package net.ymate.platform.serv.nio;

import net.ymate.platform.serv.IListener;
import net.ymate.platform.serv.IServer;
import net.ymate.platform.serv.IServerCfg;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/11/15 4:49 PM
 * @version 1.0
 */
public abstract class AbstractNioServer<LISTENER extends IListener<INioSession>> implements IServer<LISTENER, INioCodec> {

    private static final Log _LOG = LogFactory.getLog(AbstractNioServer.class);

    private IServerCfg __serverCfg;

    private INioEventGroup<LISTENER> __eventGroup;

    private LISTENER __listener;

    private INioCodec __codec;

    private boolean __isStarted;

    protected abstract INioEventGroup<LISTENER> buildEventGroup(IServerCfg serverCfg, LISTENER listener, INioCodec codec) throws IOException;

    @Override
    public void init(IServerCfg serverCfg, LISTENER listener, INioCodec codec) {
        __serverCfg = serverCfg;
        //
        __listener = listener;
        __codec = codec;
        __codec.init(__serverCfg.getCharset());
    }

    @Override
    public void start() throws IOException {
        if (!__isStarted) {
            __isStarted = true;
            __eventGroup = buildEventGroup(__serverCfg, __listener, __codec);
            __eventGroup.start();
            //
            _LOG.info(getClass().getSimpleName() + " [" + __eventGroup.name() + "] started at " + __serverCfg.getServerHost() + ":" + __serverCfg.getPort());
        }
    }

    @Override
    public boolean isStarted() {
        return __isStarted;
    }

    @Override
    public IServerCfg serverCfg() {
        return __serverCfg;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends LISTENER> T listener() {
        return (T) __listener;
    }

    @Override
    public void close() throws IOException {
        if (__isStarted) {
            _LOG.info(getClass().getSimpleName() + " [" + __eventGroup.name() + "] is closing....");
            __isStarted = false;
            __eventGroup.close();
        }
    }
}
