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
package net.ymate.platform.core.beans;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/11/7 7:06 PM
 */
public abstract class AbstractBeanLoader implements IBeanLoader {

    private static final Log LOG = LogFactory.getLog(AbstractBeanLoader.class);

    private ClassLoader classLoader;

    private final List<String> packageNames = new ArrayList<>();

    private final List<String> excludedPackageNames = new ArrayList<>();

    private final List<String> excludedFiles = new ArrayList<>();

    private final Map<Class<? extends Annotation>, IBeanHandler> handlerMap = new HashMap<>();

    @Override
    public ClassLoader getClassLoader() {
        return classLoader != null ? classLoader : this.getClass().getClassLoader();
    }

    @Override
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void load(IBeanFactory beanFactory) throws Exception {
        load(beanFactory, null);
    }

    private boolean hasPackageParent(List<String> targetList, String packageName) {
        boolean flag = false;
        do {
            packageName = StringUtils.substringBeforeLast(packageName, ".");
            if (targetList.contains(packageName)) {
                flag = true;
            }
        } while (!flag && StringUtils.contains(packageName, "."));
        return flag;
    }

    private void parsePackagePath(List<String> targetList, String packageName) {
        if (!targetList.contains(packageName)) {
            if (targetList.isEmpty()) {
                targetList.add(packageName);
            } else if (!hasPackageParent(targetList, packageName)) {
                targetList.removeIf(s -> StringUtils.startsWith(s, packageName));
                targetList.add(packageName);
            }
        }
    }

    @Override
    public void registerPackageName(String packageName) {
        parsePackagePath(this.packageNames, packageName);
    }

    @Override
    public void registerPackageNames(Collection<String> packageNames) {
        packageNames.forEach(packageName -> parsePackagePath(this.packageNames, packageName));
    }

    @Override
    public List<String> getPackageNames() {
        return Collections.unmodifiableList(packageNames);
    }

    @Override
    public void registerExcludedPackageName(String packageName) {
        parsePackagePath(this.excludedPackageNames, packageName);
    }

    @Override
    public void registerExcludedPackageNames(Collection<String> packageNames) {
        packageNames.forEach(packageName -> parsePackagePath(this.excludedPackageNames, packageName));
    }

    @Override
    public List<String> getExcludedPackageNames() {
        return Collections.unmodifiableList(excludedPackageNames);
    }

    @Override
    public void registerHandler(Class<? extends Annotation> annClass, IBeanHandler handler) {
        if (!handlerMap.containsKey(annClass)) {
            handlerMap.put(annClass, handler);
            //
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Handler class [%s:%s] registered.", annClass.getSimpleName(), handler.getClass().getName()));
            }
        } else if (LOG.isWarnEnabled()) {
            LOG.warn(String.format("Handler class [%s:%s] duplicate registration is not allowed.", annClass.getSimpleName(), handler.getClass().getName()));
        }
    }

    @Override
    public void registerHandler(Class<? extends Annotation> annClass) {
        registerHandler(annClass, IBeanHandler.DEFAULT_HANDLER);
    }

    @Override
    public IBeanHandler getBeanHandler(Class<? extends Annotation> annClass) {
        return handlerMap.get(annClass);
    }

    @Override
    public List<String> getExcludedFiles() {
        return Collections.unmodifiableList(excludedFiles);
    }

    @Override
    public void registerExcludedFiles(Collection<String> excludedFiles) {
        this.excludedFiles.addAll(excludedFiles);
    }
}
