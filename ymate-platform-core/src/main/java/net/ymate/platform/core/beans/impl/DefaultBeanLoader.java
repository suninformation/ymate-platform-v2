/*
 * Copyright 2007-2107 the original author or authors.
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

import net.ymate.platform.core.beans.IBeanFilter;
import net.ymate.platform.core.beans.IBeanLoader;
import net.ymate.platform.core.util.ClassUtils;
import net.ymate.platform.core.util.ResourceUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
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

    public List<Class<?>> load(String packageName) throws Exception {
        return load(packageName, null);
    }

    public List<Class<?>> load(String packageName, IBeanFilter filter) throws Exception {
        List<Class<?>> _returnValue = new ArrayList<Class<?>>();
        Iterator<URL> _resources = ResourceUtils.getResources(packageName.replaceAll("\\.", "/"), this.getClass(), true);
        while (_resources.hasNext()) {
            URL _res = _resources.next();
            if (_res.getProtocol().equalsIgnoreCase("file") || _res.getProtocol().equalsIgnoreCase("vfsfile")) {
                File[] _files = new File(_res.toURI()).listFiles();
                if (_files != null && _files.length > 0) for (File _file : _files) {
                    _returnValue.addAll(__doFindClassByClazz(packageName, _file, filter));
                }
            } else if (_res.getProtocol().equalsIgnoreCase("jar") || _res.getProtocol().equalsIgnoreCase("wsjar")) {
                _returnValue.addAll(__doFindClassByJar(packageName, ((JarURLConnection) _res.openConnection()).getJarFile(), filter));
            } else if (_res.getProtocol().equalsIgnoreCase("zip")) {
                _returnValue.addAll(__doFindClassByZip(_res, filter));
            }
        }
        return _returnValue;
    }

    private List<Class<?>> __doFindClassByClazz(String packageName, File resourceFile, IBeanFilter filter) throws Exception {
        List<Class<?>> _returnValue = new ArrayList<Class<?>>();
        String _resFileName = resourceFile.getName();
        if (resourceFile.isFile()) {
            if (_resFileName.endsWith(".class") && _resFileName.indexOf('$') < 0) {
                Class<?> _class = ClassUtils.loadClass(packageName + "." + _resFileName.replace(".class", ""), this.getClass());
                //
                __doAddClass(_returnValue, _class, filter);
            }
        } else {
            File[] _tmpFiles = resourceFile.listFiles();
            if (_tmpFiles != null && _tmpFiles.length > 0) for (File _tmpFile : _tmpFiles) {
                _returnValue.addAll(__doFindClassByClazz(packageName + "." + _resFileName, _tmpFile, filter));
            }
        }
        return _returnValue;
    }

    private List<Class<?>> __doFindClassByJar(String packageName, JarFile jarFile, IBeanFilter filter) throws Exception {
        List<Class<?>> _returnValue = new ArrayList<Class<?>>();
        Enumeration<JarEntry> _entriesEnum = jarFile.entries();
        for (; _entriesEnum.hasMoreElements(); ) {
            JarEntry _entry = _entriesEnum.nextElement();
            // 替换文件名中所有的 '/' 为 '.'，并且只存放.class结尾的类名称，剔除所有包含'$'的内部类名称
            String _className = _entry.getName().replaceAll("/", ".");
            if (_className.endsWith(".class") && _className.indexOf('$') < 0) {
                if (_className.startsWith(packageName)) {
                    Class<?> _class = ClassUtils.loadClass(_className.substring(0, _className.lastIndexOf('.')), this.getClass());
                    //
                    __doAddClass(_returnValue, _class, filter);
                }
            }
        }
        return _returnValue;
    }

    private List<Class<?>> __doFindClassByZip(URL zipUrl, IBeanFilter filter) throws Exception {
        List<Class<?>> _returnValue = new ArrayList<Class<?>>();
        ZipInputStream _zipStream = null;
        try {
            String _zipFilePath = zipUrl.toString();
            if (_zipFilePath.indexOf('!') > 0) {
                _zipFilePath = StringUtils.substringBetween(zipUrl.toString(), "zip:", "!");
            } else {
                _zipFilePath = StringUtils.substringAfter(zipUrl.toString(), "zip:");
            }
            ClassUtils.InnerClassLoader _innerLoader = ((ClassUtils.InnerClassLoader) ClassUtils.getDefaultClassLoader());
            _zipStream = new ZipInputStream(new FileInputStream(new File(_zipFilePath)));
            ZipEntry _zipEntry = null;
            while (null != (_zipEntry = _zipStream.getNextEntry())) {
                if (!_zipEntry.isDirectory()) {
                    if (_zipEntry.getName().endsWith(".class") && _zipEntry.getName().indexOf('$') < 0) {
                        _innerLoader.addURL(zipUrl);
                        String _className = StringUtils.substringBefore(_zipEntry.getName().replace("/", "."), ".class");
                        //
                        __doAddClass(_returnValue, Class.forName(_className, false, _innerLoader), filter);
                    }
                }
                _zipStream.closeEntry();
            }
        } finally {
            try {
                _zipStream.close();
            } catch (IOException e) {
            }
        }
        return _returnValue;
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
