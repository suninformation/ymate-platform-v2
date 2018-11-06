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
package net.ymate.platform.core.beans.impl;

import net.ymate.platform.core.beans.*;
import net.ymate.platform.core.beans.annotation.Ignored;
import net.ymate.platform.core.util.ClassUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 默认对象加载器接口
 *
 * @author 刘镇 (suninformation@163.com) on 15-3-6 下午1:46
 * @version 1.0
 */
public class DefaultBeanLoader implements IBeanLoader {

    private ClassLoader __classLoader;

    public DefaultBeanLoader() {
    }

    @Override
    public ClassLoader getClassLoader() {
        return __classLoader == null ? this.getClass().getClassLoader() : __classLoader;
    }

    @Override
    public void setClassLoader(ClassLoader classLoader) {
        this.__classLoader = classLoader;
    }

    @Override
    public void load(IBeanFactory beanFactory) throws Exception {
        load(beanFactory, null);
    }

    @Override
    public void load(IBeanFactory beanFactory, IBeanFilter filter) throws Exception {
        if (!beanFactory.getPackageNames().isEmpty()) {
            String[] _excludedPackages = beanFactory.getExcludedPackageNames().toArray(new String[0]);
            for (String _packageName : beanFactory.getPackageNames()) {
                List<Class<?>> _classes = __doLoad(beanFactory, _packageName, filter);
                for (Class<?> _class : _classes) {
                    if (!StringUtils.startsWithAny(_class.getPackage().getName(), _excludedPackages)) {
                        // 不扫描注解、枚举类，被声明@Ingored注解的类也将被忽略，因为需要处理package-info信息，所以放开接口限制
                        if (!_class.isAnnotation() && !_class.isEnum() /* && !_class.isInterface() */ && !_class.isAnnotationPresent(Ignored.class)) {
                            Annotation[] _annotations = _class.getAnnotations();
                            if (_annotations != null && _annotations.length > 0) {
                                for (Annotation _anno : _annotations) {
                                    IBeanHandler _handler = beanFactory.getBeanHandler(_anno.annotationType());
                                    if (_handler != null) {
                                        Object _instance = _handler.handle(_class);
                                        if (_instance != null) {
                                            if (_instance instanceof BeanMeta) {
                                                beanFactory.registerBean((BeanMeta) _instance);
                                            } else {
                                                beanFactory.registerBean(_class, _instance);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private List<Class<?>> __doLoad(IBeanFactory beanFactory, String packageName, IBeanFilter filter) throws Exception {
        List<Class<?>> _returnValue = new ArrayList<Class<?>>();
        Enumeration<URL> _resources = this.getClassLoader().getResources(packageName.replaceAll("\\.", "/"));
        while (_resources.hasMoreElements()) {
            URL _res = _resources.nextElement();
            if ("file".equalsIgnoreCase(_res.getProtocol()) || "vfsfile".equalsIgnoreCase(_res.getProtocol())) {
                File[] _files = new File(_res.toURI()).listFiles();
                if (_files != null && _files.length > 0) {
                    for (File _file : _files) {
                        _returnValue.addAll(__doFindClassByClazz(beanFactory, packageName, _file, filter));
                    }
                }
            } else if ("jar".equalsIgnoreCase(_res.getProtocol()) || "wsjar".equalsIgnoreCase(_res.getProtocol())) {
                _returnValue.addAll(__doFindClassByJar(beanFactory, packageName, ((JarURLConnection) _res.openConnection()).getJarFile(), filter));
            } else if ("zip".equalsIgnoreCase(_res.getProtocol())) {
                _returnValue.addAll(__doFindClassByZip(beanFactory, _res, filter));
            }
        }
        return _returnValue;
    }

    private List<Class<?>> __doFindClassByClazz(IBeanFactory beanFactory, String packageName, File resourceFile, IBeanFilter filter) throws Exception {
        List<Class<?>> _returnValue = new ArrayList<Class<?>>();
        String _resFileName = resourceFile.getName();
        if (resourceFile.isFile()) {
            if (_resFileName.endsWith(".class") && _resFileName.indexOf('$') < 0) {
                String _className = packageName + "." + _resFileName.replace(".class", "");
                Class<?> _class = __doLoadClass(_className);
                __doAddClass(_returnValue, _class, filter);
            }
        } else {
            File[] _tmpFiles = resourceFile.listFiles();
            if (_tmpFiles != null && _tmpFiles.length > 0) {
                for (File _tmpFile : _tmpFiles) {
                    _returnValue.addAll(__doFindClassByClazz(beanFactory, packageName + "." + _resFileName, _tmpFile, filter));
                }
            }
        }
        return _returnValue;
    }

    private boolean __doCheckExcludedFile(IBeanFactory beanFactory, String targetFileName) {
        if (!beanFactory.getExcludedFiles().isEmpty() && StringUtils.isNotBlank(targetFileName)) {
            if (beanFactory.getExcludedFiles().contains(targetFileName)) {
                return true;
            }
            for (String _excludedFile : beanFactory.getExcludedFiles()) {
                if (_excludedFile.indexOf('*') > 0) {
                    _excludedFile = StringUtils.substringBefore(_excludedFile, "*");
                }
                if (targetFileName.startsWith(_excludedFile)) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<Class<?>> __doFindClassByJar(IBeanFactory beanFactory, String packageName, JarFile jarFile, IBeanFilter filter) throws Exception {
        List<Class<?>> _returnValue = new ArrayList<Class<?>>();
        if (!__doCheckExcludedFile(beanFactory, new File(jarFile.getName()).getName())) {
            Enumeration<JarEntry> _entriesEnum = jarFile.entries();
            for (; _entriesEnum.hasMoreElements(); ) {
                JarEntry _entry = _entriesEnum.nextElement();
                // 替换文件名中所有的 '/' 为 '.'，并且只存放.class结尾的类名称，剔除所有包含'$'的内部类名称
                String _className = _entry.getName().replaceAll("/", ".");
                if (_className.endsWith(".class") && _className.indexOf('$') < 0) {
                    if (_className.startsWith(packageName)) {
                        _className = _className.substring(0, _className.lastIndexOf('.'));
                        Class<?> _class = __doLoadClass(_className);
                        __doAddClass(_returnValue, _class, filter);
                    }
                }
            }
        }
        return _returnValue;
    }

    private List<Class<?>> __doFindClassByZip(IBeanFactory beanFactory, URL zipUrl, IBeanFilter filter) throws Exception {
        List<Class<?>> _returnValue = new ArrayList<Class<?>>();
        ZipInputStream _zipStream = null;
        try {
            String _zipFilePath = zipUrl.toString();
            if (_zipFilePath.indexOf('!') > 0) {
                _zipFilePath = StringUtils.substringBetween(zipUrl.toString(), "zip:", "!");
            } else {
                _zipFilePath = StringUtils.substringAfter(zipUrl.toString(), "zip:");
            }
            File _zipFile = new File(_zipFilePath);
            if (!__doCheckExcludedFile(beanFactory, _zipFile.getName())) {
                _zipStream = new ZipInputStream(new FileInputStream(_zipFile));
                ZipEntry _zipEntry = null;
                while (null != (_zipEntry = _zipStream.getNextEntry())) {
                    if (!_zipEntry.isDirectory()) {
                        if (_zipEntry.getName().endsWith(".class") && _zipEntry.getName().indexOf('$') < 0) {
                            String _className = StringUtils.substringBefore(_zipEntry.getName().replace("/", "."), ".class");
                            __doAddClass(_returnValue, __doLoadClass(_className), filter);
                        }
                    }
                    _zipStream.closeEntry();
                }
            }
        } finally {
            if (_zipStream != null) {
                try {
                    _zipStream.close();
                } catch (IOException ignored) {
                }
            }
        }
        return _returnValue;
    }

    private Class<?> __doLoadClass(String className) throws ClassNotFoundException {
        Class<?> _class;
        try {
            _class = ClassUtils.loadClass(className, this.getClass());
        } catch (ClassNotFoundException e) {
            _class = getClassLoader().loadClass(className);
        }
        return _class;
    }

    private void __doAddClass(List<Class<?>> collection, Class<?> targetClass, IBeanFilter filter) {
        if (targetClass != null) {
            if (filter != null) {
                if (filter.filter(targetClass)) {
                    collection.add(targetClass);
                }
            } else {
                collection.add(targetClass);
            }
        }
    }

}
