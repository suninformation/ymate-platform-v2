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

import net.ymate.platform.core.YMP;
import net.ymate.platform.core.support.IConfigReader;
import net.ymate.platform.core.support.impl.MapSafeConfigReader;
import net.ymate.platform.serv.IClientCfg;
import net.ymate.platform.serv.IServ;
import net.ymate.platform.serv.IServModuleCfg;
import net.ymate.platform.serv.IServerCfg;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 服务模块配置类
 *
 * @author 刘镇 (suninformation@163.com) on 15/10/15 上午10:27
 * @version 1.0
 */
public class DefaultServModuleCfg implements IServModuleCfg {

    private final Map<String, IServerCfg> __serverCfgs;

    private final Map<String, IClientCfg> __clientCfgs;

    public DefaultServModuleCfg(YMP owner) throws Exception {
        IConfigReader _moduleCfg = MapSafeConfigReader.bind(owner.getConfig().getModuleConfigs(IServ.MODULE_NAME));
        //
        String[] _serverNames = StringUtils.split(_moduleCfg.getString("server.name_list", IServ.Const.DEFAULT_NAME), "|");
        __serverCfgs = new HashMap<String, IServerCfg>(_serverNames.length);
        for (String _name : _serverNames) {
            __serverCfgs.put(_name, new DefaultServerCfg(_moduleCfg.getMap("server." + _name + "."), _name));
        }
        //
        String[] _clientNames = StringUtils.split(_moduleCfg.getString("client.name_list", IServ.Const.DEFAULT_NAME), "|");
        __clientCfgs = new HashMap<String, IClientCfg>(_clientNames.length);
        for (String _name : _clientNames) {
            __clientCfgs.put(_name, new DefaultClientCfg(_moduleCfg.getMap("client." + _name + "."), _name));
        }
    }

    @Override
    public IServerCfg getServerCfg(String serverName) {
        return __serverCfgs.get(serverName);
    }

    @Override
    public IClientCfg getClientCfg(String clientName) {
        return __clientCfgs.get(clientName);
    }
}
