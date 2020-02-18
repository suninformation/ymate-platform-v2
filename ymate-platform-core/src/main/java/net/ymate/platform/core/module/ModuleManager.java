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
package net.ymate.platform.core.module;

import net.ymate.platform.commons.ConcurrentHashSet;
import net.ymate.platform.commons.ReentrantLockHelper;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.support.IDestroyable;
import net.ymate.platform.core.support.IInitialization;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 模块管理器
 *
 * @author 刘镇 (suninformation@163.com) on 2019-04-28 17:41
 * @since 2.1.0
 */
public class ModuleManager implements IInitialization<IApplication>, IDestroyable {

    private final Map<String, IModule> modules = new ConcurrentHashMap<>();

    private final Set<String> excludedModules = new ConcurrentHashSet<>();

    private IApplication owner;

    private boolean initialized;

    @Override
    public void initialize(IApplication owner) throws Exception {
        if (!initialized) {
            this.owner = owner;
            owner.getEvents().registerEvent(ModuleEvent.class);
            //
            ClassUtils.getExtensionLoader(IModule.class, true).getExtensionClasses().forEach(this::registerModule);
            //
            modules.entrySet().stream()
                    .filter((entry) -> (!isModuleExcluded(entry.getValue().getName()) && !isModuleExcluded(entry.getKey()) && !entry.getValue().isInitialized()))
                    .forEachOrdered((entry) -> initializeModuleIfNeed(entry.getValue()));
            initialized = true;
        }
    }

    private void initializeModuleIfNeed(IModule module) {
        if (module != null && !module.isInitialized()) {
            try {
                // 触发模块启动事件
                owner.getEvents().fireEvent(new ModuleEvent(module, ModuleEvent.EVENT.MODULE_STARTUP));
                //
                module.initialize(owner);
                // 触发模块初始化完成事件
                owner.getEvents().fireEvent(new ModuleEvent(module, ModuleEvent.EVENT.MODULE_INITIALIZED));
            } catch (Exception e) {
                throw RuntimeUtils.wrapRuntimeThrow(e, "An exception occurred while initializing module [%s].", module.getName());
            }
        }
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void close() throws Exception {
        if (initialized) {
            Iterator<Map.Entry<String, IModule>> moduleIt = modules.entrySet().iterator();
            while (moduleIt.hasNext()) {
                Map.Entry<String, IModule> entry = moduleIt.next();
                try (IModule module = entry.getValue()) {
                    if (module != null && module.isInitialized()) {
                        // 触发模块销毁事件
                        owner.getEvents().fireEvent(new ModuleEvent(module, ModuleEvent.EVENT.MODULE_DESTROYED));
                    }
                }
                moduleIt.remove();
            }
            initialized = false;
        }
    }

    /**
     * @param moduleName 模块名称或类名
     * @return 判断指定名称或类名的模块是否已被排除
     */
    public boolean isModuleExcluded(String moduleName) {
        return excludedModules.contains(moduleName);
    }

    /**
     * 向模块类排除列表添加被排除的模块名或类名
     *
     * @param excludedModuleName 模块名或类名
     */
    public void addExcludedModule(String excludedModuleName) {
        excludedModules.add(excludedModuleName);
    }

    /**
     * 向模块类排除列表添加被排除的模块名或类名集合
     *
     * @param excludedModuleNames 模块名或类名集合
     */
    public void addExcludedModules(Collection<String> excludedModuleNames) {
        excludedModules.addAll(excludedModuleNames);
    }

    /**
     * 注册模块实例
     *
     * @param moduleClass 目标模块类
     */
    public void registerModule(Class<? extends IModule> moduleClass) {
        if (moduleClass != null && !isModuleExcluded(moduleClass.getName())) {
            try {
                ReentrantLockHelper.putIfAbsentAsync(modules, moduleClass.getName(), moduleClass::newInstance);
            } catch (Exception e) {
                throw RuntimeUtils.wrapRuntimeThrow(e, "An exception occurred while registering module [%s].", moduleClass);
            }
        }
    }

    /**
     * 判断指定模块是否存在
     *
     * @param moduleClass 模块类型
     * @return 返回true表示存在
     */
    public boolean hasModule(Class<? extends IModule> moduleClass) {
        return moduleClass != null && modules.containsKey(moduleClass.getName());
    }

    /**
     * 判断指定模块是否存在
     *
     * @param moduleClassName 模块类名称
     * @return 返回true表示存在
     */
    public boolean hasModule(String moduleClassName) {
        return modules.containsKey(moduleClassName);
    }

    /**
     * @param moduleClass 模块类型
     * @param <T>         模块类型
     * @return 获取模块类实例对象
     */
    public <T extends IModule> T getModule(Class<T> moduleClass) {
        return moduleClass != null ? getModule(moduleClass.getName()) : null;
    }

    /**
     * @param moduleClassName 模块类名称
     * @param <T>             模块类型
     * @return 获取模块类实例对象
     */
    @SuppressWarnings("unchecked")
    public <T extends IModule> T getModule(String moduleClassName) {
        if (StringUtils.isNotBlank(moduleClassName) && !isModuleExcluded(moduleClassName)) {
            IModule module = modules.get(moduleClassName);
            if (module != null && !isModuleExcluded(module.getName())) {
                initializeModuleIfNeed(module);
                return (T) module;
            }
        }
        return null;
    }
}
