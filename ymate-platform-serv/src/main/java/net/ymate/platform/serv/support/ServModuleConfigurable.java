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
package net.ymate.platform.serv.support;

import net.ymate.platform.core.support.IModuleConfigurable;
import net.ymate.platform.serv.IServ;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2018-12-14 18:50
 * @version 1.0
 * @since 2.0.6
 */
public class ServModuleConfigurable implements IModuleConfigurable {

    public static ServModuleConfigurable create() {
        return new ServModuleConfigurable();
    }

    private Map<String, ServServerConfigurable> __servers = new HashMap<String, ServServerConfigurable>();

    private Map<String, ServClientConfigurable> __clients = new HashMap<String, ServClientConfigurable>();

    public ServModuleConfigurable addServer(ServServerConfigurable serverConfigurable) {
        __servers.put(serverConfigurable.getName(), serverConfigurable);
        return this;
    }

    public ServModuleConfigurable addClient(ServClientConfigurable clientConfigurable) {
        __clients.put(clientConfigurable.getName(), clientConfigurable);
        return this;
    }

    @Override
    public String getModuleName() {
        return IServ.MODULE_NAME;
    }

    @Override
    public Map<String, String> toMap() {
        Map<String, String> _configs = new HashMap<String, String>();
        if (!__servers.isEmpty()) {
            _configs.put("server.name_list", StringUtils.join(__servers.keySet(), "|"));
            for (ServServerConfigurable _server : __servers.values()) {
                _configs.putAll(_server.toMap());
            }
        }
        if (!__clients.isEmpty()) {
            _configs.put("client.name_list", StringUtils.join(__clients.keySet(), "|"));
            for (ServClientConfigurable _client : __clients.values()) {
                _configs.putAll(_client.toMap());
            }
        }
        return _configs;
    }
}
