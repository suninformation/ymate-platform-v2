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
package net.ymate.platform.plugin.impl;

import net.ymate.platform.core.beans.BeanMeta;
import net.ymate.platform.core.beans.impl.DefaultBeanFactory;
import net.ymate.platform.plugin.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 刘镇 (suninformation@163.com) on 2018-11-28 19:55
 * @version 1.0
 */
public class DefaultPluginBeanFactory extends DefaultBeanFactory implements IPluginBeanFactory {

    private static final Log _LOG = LogFactory.getLog(DefaultPluginBeanFactory.class);

    /**
     * PluginID -- PluginMeta
     */
    private Map<String, PluginMeta> __pluginMetaIds = new ConcurrentHashMap<String, PluginMeta>();

    private IPluginFactory __pluginFactory;

    private boolean __includedClassPath;

    public DefaultPluginBeanFactory(IPluginFactory factory) {
        this(factory, false);
    }

    DefaultPluginBeanFactory(IPluginFactory factory, boolean includedClassPath) {
        super(factory.getOwner());
        //
        __pluginFactory = factory;
        __includedClassPath = includedClassPath;
    }

    @Override
    public boolean isIncludedClassPath() {
        return __includedClassPath;
    }

    @Override
    protected void __addClass(BeanMeta beanMeta) {
        if (beanMeta.getBeanObject() instanceof PluginMeta) {
            final PluginMeta _meta = (PluginMeta) beanMeta.getBeanObject();
            //
            if (!__includedClassPath && StringUtils.isBlank(_meta.getPath())) {
                return;
            }
            //
            BeanMeta _beanMeta = BeanMeta.create(beanMeta.getBeanClass(), true);
            _beanMeta.setInitializer(new BeanMeta.IInitializer() {
                @Override
                public void init(Object target) throws Exception {
                    // 尝试通过IPluginExtend接口方式获取扩展对象
                    if (_meta.getExtendObject() == null && target instanceof IPluginExtend) {
                        _meta.setExtendObject(((IPluginExtend<?>) target).getExtendObject(new DefaultPluginContext(__pluginFactory, _meta)));
                    }
                }
            });
            super.__addClass(_beanMeta);
            //
            if (__pluginFactory.getOwner().getConfig().isDevelopMode() && _LOG.isInfoEnabled()) {
                _LOG.info("--> " + _meta.toString() + " registered.");
            }
            //
            __pluginMetaIds.put(_meta.getId(), _meta);
        } else {
            __pluginFactory.getOwner().registerBean(beanMeta);
        }
    }

    @Override
    public IPlugin getPlugin(String id) {
        IPlugin _plugin = null;
        if (__pluginMetaIds.containsKey(id)) {
            _plugin = getBean(__pluginMetaIds.get(id).getInitClass());
        }
        return _plugin;
    }

    @Override
    public PluginMeta getPluginMeta(String id) {
        return __pluginMetaIds.get(id);
    }

    @Override
    public Collection<PluginMeta> getPluginMetas() {
        return Collections.unmodifiableCollection(__pluginMetaIds.values());
    }

    @Override
    public void destroy() throws Exception {
        __pluginMetaIds = null;
        //
        super.destroy();
    }
}
