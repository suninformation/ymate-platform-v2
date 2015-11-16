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
import net.ymate.platform.serv.IClientCfg;
import net.ymate.platform.serv.IServ;
import net.ymate.platform.serv.annotation.Client;
import net.ymate.platform.serv.impl.DefaultClientCfg;
import net.ymate.platform.serv.nio.INioCodec;
import net.ymate.platform.serv.nio.client.NioClient;
import net.ymate.platform.serv.nio.client.NioClientListener;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/6 下午8:37
 * @version 1.0
 */
public class ClientHandler implements IBeanHandler {

    private IServ __owner;

    public ClientHandler(IServ owner) throws Exception {
        __owner = owner;
    }

    public Object handle(Class<?> targetClass) throws Exception {
        if (ClassUtils.isSubclassOf(targetClass, NioClientListener.class)) {
            Client _anno = targetClass.getAnnotation(Client.class);
            NioClientListener _instance = ClassUtils.impl(targetClass, NioClientListener.class);
            IClientCfg _cfg = new DefaultClientCfg();
            _cfg.init(__owner.getModuleCfg(), _cfg.getClientName());
            INioCodec _codec = ClassUtils.impl(_anno.codec(), INioCodec.class);
            _codec.init(_cfg.getCharset());
            //
            NioClient _client = null;
//            if (_anno.udp()) {
//                // TODO
//            } else {
            _client = new NioClient();
//            }
            _client.init(_cfg, _instance, _codec);
            __owner.registerClient(_instance.getClass(), _client);
        }
        return null;
    }
}
