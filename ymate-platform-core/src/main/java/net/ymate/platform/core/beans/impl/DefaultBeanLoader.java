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
package net.ymate.platform.core.beans.impl;

import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.FileUtils;
import net.ymate.platform.core.beans.*;
import net.ymate.platform.core.beans.annotation.Ignored;
import org.apache.commons.lang3.StringUtils;

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
 */
public class DefaultBeanLoader extends AbstractBeanLoader {

    @Override
    public void load(IBeanFactory beanFactory, IBeanFilter filter) throws Exception {
        List<String> packageNames = getPackageNames();
        if (!packageNames.isEmpty()) {
            String[] excludedPackages = getExcludedPackageNames().toArray(new String[0]);
            for (String packageName : packageNames) {
                List<Class<?>> classes = doLoad(packageName, filter);
                for (Class<?> clazz : classes) {
                    if (!StringUtils.startsWithAny(clazz.getPackage().getName(), excludedPackages)) {
                        // 不扫描注解、枚举类，被声明@Ingored注解的类也将被忽略，因为需要处理package-info信息，所以放开接口限制
                        if (!clazz.isAnnotation() && !clazz.isEnum() && !clazz.isAnnotationPresent(Ignored.class)) {
                            Annotation[] annotations = clazz.getAnnotations();
                            if (annotations != null && annotations.length > 0) {
                                for (Annotation annotation : annotations) {
                                    IBeanHandler beanHandler = getBeanHandler(annotation.annotationType());
                                    if (beanHandler != null) {
                                        Object instanceObj = beanHandler.handle(clazz);
                                        if (instanceObj instanceof BeanMeta) {
                                            beanFactory.registerBean((BeanMeta) instanceObj);
                                        } else if (instanceObj != null) {
                                            BeanMeta beanMeta = BeanMeta.create(clazz, true);
                                            beanMeta.setBeanObject(instanceObj);
                                            beanFactory.registerBean(beanMeta);
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

    private List<Class<?>> doLoad(String packageName, IBeanFilter filter) throws Exception {
        List<Class<?>> returnValue = new ArrayList<>();
        Enumeration<URL> resources = this.getClassLoader().getResources(packageName.replaceAll("\\.", "/"));
        while (resources.hasMoreElements()) {
            URL res = resources.nextElement();
            if (FileUtils.PROTOCOL_FILE.equalsIgnoreCase(res.getProtocol()) || FileUtils.PROTOCOL_VFS_FILE.equalsIgnoreCase(res.getProtocol())) {
                File[] files = new File(res.toURI()).listFiles();
                if (files != null && files.length > 0) {
                    for (File file : files) {
                        returnValue.addAll(findClassByClazz(packageName, file, filter));
                    }
                }
            } else if (FileUtils.PROTOCOL_JAR.equalsIgnoreCase(res.getProtocol()) || FileUtils.PROTOCOL_WS_JAR.equalsIgnoreCase(res.getProtocol())) {
                returnValue.addAll(findClassByJar(packageName, ((JarURLConnection) res.openConnection()).getJarFile(), filter));
            } else if (FileUtils.PROTOCOL_ZIP.equalsIgnoreCase(res.getProtocol())) {
                returnValue.addAll(findClassByZip(res, filter));
            }
        }
        return returnValue;
    }

    private List<Class<?>> findClassByClazz(String packageName, File resourceFile, IBeanFilter filter) throws Exception {
        List<Class<?>> returnValue = new ArrayList<>();
        String resourceFileName = resourceFile.getName();
        if (resourceFile.isFile()) {
            if (resourceFileName.endsWith(FileUtils.FILE_SUFFIX_CLASS) && resourceFileName.indexOf('$') < 0) {
                String className = packageName + FileUtils.POINT_CHAR + resourceFileName.replace(FileUtils.FILE_SUFFIX_CLASS, StringUtils.EMPTY);
                Class<?> clazz = loadClass(className);
                addClass(returnValue, clazz, filter);
            }
        } else {
            File[] files = resourceFile.listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    returnValue.addAll(findClassByClazz(packageName + FileUtils.POINT_CHAR + resourceFileName, file, filter));
                }
            }
        }
        return returnValue;
    }

    private boolean checkNonExcludedFile(String targetFileName) {
        List<String> excludedFiles = getExcludedFiles();
        if (!excludedFiles.isEmpty() && StringUtils.isNotBlank(targetFileName)) {
            if (excludedFiles.contains(targetFileName)) {
                return false;
            }
            return excludedFiles.stream().filter(StringUtils::isNotBlank).map((excludedFile) -> {
                if (excludedFile.indexOf('*') > 0) {
                    excludedFile = StringUtils.substringBefore(excludedFile, "*");
                }
                return excludedFile;
            }).noneMatch(targetFileName::startsWith);
        }
        return true;
    }

    private List<Class<?>> findClassByJar(String packageName, JarFile jarFile, IBeanFilter filter) throws Exception {
        List<Class<?>> returnValue = new ArrayList<>();
        if (checkNonExcludedFile(new File(jarFile.getName()).getName())) {
            Enumeration<JarEntry> entryEnumeration = jarFile.entries();
            while (entryEnumeration.hasMoreElements()) {
                JarEntry jarEntry = entryEnumeration.nextElement();
                // 替换文件名中所有的 '/' 为 '.'，并且只存放.class结尾的类名称，剔除所有包含'$'的内部类名称
                String className = jarEntry.getName().replaceAll("/", FileUtils.POINT_CHAR);
                if (className.endsWith(FileUtils.FILE_SUFFIX_CLASS) && className.indexOf('$') < 0) {
                    if (className.startsWith(packageName)) {
                        className = className.substring(0, className.lastIndexOf('.'));
                        addClass(returnValue, loadClass(className), filter);
                    }
                }
            }
        }
        return returnValue;
    }

    private List<Class<?>> findClassByZip(URL zipUrl, IBeanFilter filter) throws Exception {
        List<Class<?>> returnValue = new ArrayList<>();
        ZipInputStream zipInputStream = null;
        try {
            String zipPath = zipUrl.toString();
            if (zipPath.indexOf('!') > 0) {
                zipPath = StringUtils.substringBetween(zipUrl.toString(), FileUtils.FILE_PREFIX_ZIP, "!");
            } else {
                zipPath = StringUtils.substringAfter(zipUrl.toString(), FileUtils.FILE_PREFIX_ZIP);
            }
            File zipFile = new File(zipPath);
            if (checkNonExcludedFile(zipFile.getName())) {
                zipInputStream = new ZipInputStream(new FileInputStream(zipFile));
                ZipEntry zipEntry;
                while (null != (zipEntry = zipInputStream.getNextEntry())) {
                    if (!zipEntry.isDirectory()) {
                        if (zipEntry.getName().endsWith(FileUtils.FILE_SUFFIX_CLASS) && zipEntry.getName().indexOf('$') < 0) {
                            String className = StringUtils.substringBefore(zipEntry.getName().replace("/", FileUtils.POINT_CHAR), FileUtils.FILE_SUFFIX_CLASS);
                            addClass(returnValue, loadClass(className), filter);
                        }
                    }
                    zipInputStream.closeEntry();
                }
            }
        } finally {
            if (zipInputStream != null) {
                try {
                    zipInputStream.close();
                } catch (IOException ignored) {
                }
            }
        }
        return returnValue;
    }

    private Class<?> loadClass(String className) throws ClassNotFoundException {
        Class<?> clazz;
        try {
            clazz = ClassUtils.loadClass(className, this.getClass());
        } catch (ClassNotFoundException e) {
            clazz = getClassLoader().loadClass(className);
        }
        return clazz;
    }

    private void addClass(List<Class<?>> collection, Class<?> targetClass, IBeanFilter filter) {
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
