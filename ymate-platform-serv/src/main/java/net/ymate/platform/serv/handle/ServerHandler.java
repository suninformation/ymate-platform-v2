/*
 * Copyright 2007-2016 the original author or authors.
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
package net.ymate.platform.serv.handle;

import net.ymate.platform.core.beans.IBeanHandler;
import net.ymate.platform.core.util.ClassUtils;
import net.ymate.platform.serv.IServ;
import net.ymate.platform.serv.annotation.Server;
import net.ymate.platform.serv.nio.INioCodec;
import net.ymate.platform.serv.nio.INioServerCfg;
import net.ymate.platform.serv.nio.server.NioServer;
import net.ymate.platform.serv.nio.server.NioServerCfg;
import net.ymate.platform.serv.nio.server.NioServerListener;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/6 下午8:37
 * @version 1.0
 */
public class ServerHandler implements IBeanHandler {

    private IServ __owner;

    public ServerHandler(IServ owner) throws Exception {
        __owner = owner;
    }

    public Object handle(Class<?> targetClass) throws Exception {
        if (ClassUtils.isSubclassOf(targetClass, NioServerListener.class)) {
            Server _anno = targetClass.getAnnotation(Server.class);
            NioServerListener _instance = ClassUtils.impl(targetClass, NioServerListener.class);
            INioServerCfg _cfg = new NioServerCfg();
            _cfg.init(__owner.getModuleCfg(), _cfg.getServerName());
            INioCodec _codec = ClassUtils.impl(_anno.codec(), INioCodec.class);
            _codec.init(_cfg.getCharset());
            //
            NioServer _server = null;
//            if (_anno.udp()) {
//                // TODO
//            } else {
            _server = new NioServer();
//            }
            _server.init(_cfg, _instance, _codec);
            __owner.registerServer(_instance.getClass(), _server);
        }
        return null;
    }
}
