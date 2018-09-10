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
package net.ymate.platform.configuration.support;

import net.ymate.platform.configuration.Cfgs;
import net.ymate.platform.configuration.IConfigurable;
import net.ymate.platform.configuration.IConfiguration;
import net.ymate.platform.configuration.annotation.Configurable;
import net.ymate.platform.configuration.impl.DefaultConfiguration;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.beans.annotation.Order;
import net.ymate.platform.core.beans.annotation.Proxy;
import net.ymate.platform.core.beans.proxy.IProxy;
import net.ymate.platform.core.beans.proxy.IProxyChain;
import net.ymate.platform.core.util.ClassUtils;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/3/8 下午10:18
 * @version 1.0
 */
@Proxy(annotation = Configurable.class, order = @Order(-1999))
public class ConfigurableProxy implements IProxy {

    @Override
    public Object doProxy(IProxyChain proxyChain) throws Throwable {
        if (ClassUtils.isInterfaceOf(proxyChain.getTargetClass(), IConfigurable.class)) {
            if ("getConfig".equals(proxyChain.getTargetMethod().getName())) {
                IConfiguration _config = (IConfiguration) proxyChain.doProxyChain();
                if (_config == null) {
                    YMP _owner = proxyChain.getProxyFactory().getOwner();
                    Configurable _annotation = proxyChain.getTargetClass().getAnnotation(Configurable.class);
                    if (_annotation.type().equals(DefaultConfiguration.class)) {
                        _config = _annotation.type().newInstance();
                        Cfgs.get(_owner).fillCfg(_config, _annotation.value());
                    } else {
                        _config = _owner.getBean(_annotation.type());
                        if (_config == null) {
                            _config = _annotation.type().newInstance();
                            Cfgs.get(_owner).fillCfg(_config);
                        }
                    }
                    ((IConfigurable) proxyChain.getTargetObject()).setConfig(_config);
                }
                return _config;
            }
        }
        return proxyChain.doProxyChain();
    }
}
