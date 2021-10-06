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
package net.ymate.platform.plugin.impl;

import net.ymate.platform.core.beans.BeanMeta;
import net.ymate.platform.core.beans.impl.DefaultBeanFactory;
import net.ymate.platform.plugin.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 刘镇 (suninformation@163.com) on 2018-11-28 19:55
 */
public class DefaultPluginBeanFactory extends DefaultBeanFactory implements IPluginBeanFactory {

    private static final Log LOG = LogFactory.getLog(DefaultPluginBeanFactory.class);

    /**
     * PluginID -- PluginMeta
     */
    private final Map<String, PluginMeta> pluginMetaMap = new ConcurrentHashMap<>();

    /**
     * 插件别名与插件唯一标识对应关系
     */
    private final Map<String, String> pluginAliasMap = new ConcurrentHashMap<>();

    private final IPluginFactory pluginFactory;

    private final boolean includedClassPath;

    public DefaultPluginBeanFactory(IPluginFactory pluginFactory) {
        this(pluginFactory, false);
    }

    DefaultPluginBeanFactory(IPluginFactory pluginFactory, boolean includedClassPath) {
        super(pluginFactory.getOwner().getBeanFactory());
        //
        this.pluginFactory = pluginFactory;
        this.includedClassPath = includedClassPath;
    }

    @Override
    public boolean isIncludedClassPath() {
        return includedClassPath;
    }

    @Override
    protected void parseClass(BeanMeta beanMeta) {
        if (beanMeta.getBeanObject() instanceof PluginMeta) {
            final PluginMeta pluginMeta = (PluginMeta) beanMeta.getBeanObject();
            //
            if (!includedClassPath && StringUtils.isBlank(pluginMeta.getPath())) {
                return;
            }
            //
            BeanMeta pluginBeanMeta = BeanMeta.create(beanMeta.getBeanClass(), true);
            pluginBeanMeta.setInitializer(target -> {
                // 尝试通过IPluginExtension接口方式获取扩展对象
                if (pluginMeta.getExtensionObject() == null && target instanceof IPluginExtension) {
                    pluginMeta.setExtensionObject(((IPluginExtension<?>) target).getExtensionObject(new DefaultPluginContext(pluginFactory, pluginMeta)));
                }
            });
            super.parseClass(pluginBeanMeta);
            //
            pluginMetaMap.put(pluginMeta.getId(), pluginMeta);
            pluginMeta.getAlias().forEach(alias -> pluginAliasMap.put(alias, pluginMeta.getId()));
            //
            if (pluginFactory.getOwner().isDevEnv() && LOG.isInfoEnabled()) {
                LOG.info(String.format("%s registered.", pluginMeta));
            }
        } else {
            registerBean(beanMeta);
        }
    }

    @Override
    public IPlugin getPlugin(String idOrAlias) {
        if (pluginMetaMap.containsKey(idOrAlias)) {
            return getBean(pluginMetaMap.get(idOrAlias).getInitClass());
        } else {
            String aliasId = pluginAliasMap.get(idOrAlias);
            if (aliasId != null) {
                return getBean(pluginMetaMap.get(aliasId).getInitClass());
            }
        }
        return null;
    }

    @Override
    public PluginMeta getPluginMeta(String idOrAlias) {
        PluginMeta pluginMeta = pluginMetaMap.get(idOrAlias);
        if (pluginMeta == null) {
            String aliasId = pluginAliasMap.get(idOrAlias);
            if (aliasId != null) {
                pluginMeta = pluginMetaMap.get(aliasId);
            }
        }
        return pluginMeta;
    }

    @Override
    public Collection<PluginMeta> getPluginMetas() {
        return Collections.unmodifiableCollection(pluginMetaMap.values());
    }

    @Override
    public void close() throws Exception {
        super.close();
        pluginMetaMap.clear();
        pluginAliasMap.clear();
    }
}
