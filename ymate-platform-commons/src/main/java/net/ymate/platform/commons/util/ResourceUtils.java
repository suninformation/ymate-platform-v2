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
package net.ymate.platform.commons.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**
 * 资源加载工具类
 *
 * @author 刘镇 (suninformation@163.com) on 2010-5-16 下午04:12:19
 */
public class ResourceUtils {

    private static final Log LOG = LogFactory.getLog(ResourceUtils.class);

    public static final char PATH_SEPARATOR_CHAR = '/';

    public static Iterator<URL> getResources(String resourceName, Class<?> callingClass, boolean aggregate) throws IOException {
        resourceName = StringUtils.trimToEmpty(resourceName);
        //
        AggregateIterator<URL> iterator = new AggregateIterator<>();
        iterator.addEnumeration(Thread.currentThread().getContextClassLoader().getResources(resourceName));
        if ((!iterator.hasNext()) || (aggregate)) {
            ClassLoader defaultClassLoader = ClassUtils.getDefaultClassLoader();
            if (defaultClassLoader != null) {
                iterator.addEnumeration(defaultClassLoader.getResources(resourceName));
            }
        }
        if (!iterator.hasNext() || aggregate) {
            if (callingClass != null) {
                ClassLoader cl = callingClass.getClassLoader();
                if (cl != null) {
                    iterator.addEnumeration(cl.getResources(resourceName));
                }
            }
        }
        if (!iterator.hasNext()) {
            if (resourceName.isEmpty() || resourceName.charAt(0) != PATH_SEPARATOR_CHAR) {
                return getResources(PATH_SEPARATOR_CHAR + resourceName, callingClass, aggregate);
            }
        }
        return iterator;
    }

    public static URL getResource(String resourceName, Class<?> callingClass) {
        resourceName = StringUtils.trimToEmpty(resourceName);
        //
        URL url = Thread.currentThread().getContextClassLoader().getResource(resourceName);
        if (url == null) {
            ClassLoader defaultClassLoader = ClassUtils.getDefaultClassLoader();
            if (defaultClassLoader != null) {
                url = defaultClassLoader.getResource(resourceName);
            }
        }
        if (url == null && callingClass != null) {
            url = callingClass.getResource(resourceName);
            if (url == null) {
                ClassLoader cl = callingClass.getClassLoader();
                if (cl != null) {
                    url = cl.getResource(resourceName);
                }
            }
        }
        if (url == null && (resourceName.isEmpty() || resourceName.charAt(0) != PATH_SEPARATOR_CHAR)) {
            return getResource(PATH_SEPARATOR_CHAR + resourceName, callingClass);
        }
        return url;
    }

    public static InputStream getResourceAsStream(String resourceName, Class<?> callingClass) throws IOException {
        URL url = getResource(resourceName, callingClass);
        return url != null ? url.openStream() : null;
    }

    /**
     * 按数组顺序查加载资源文件并返回输入流
     *
     * @param callingClass 调用都类型
     * @param filePaths    资源文件列表
     * @return 返回文件输入流
     * @since 2.1.0
     */
    public static InputStream getResourceAsStream(Class<?> callingClass, String... filePaths) {
        InputStream inputStream = null;
        if (filePaths != null && filePaths.length > 0) {
            ClassLoader classLoader = callingClass.getClassLoader();
            for (String filePath : filePaths) {
                if (StringUtils.isNotBlank(filePath)) {
                    URL url = classLoader.getResource(filePath);
                    if (url != null) {
                        try {
                            inputStream = url.openStream();
                            if (LOG.isInfoEnabled()) {
                                LOG.info(String.format("Found and load the resource file: %s", url));
                            }
                            break;
                        } catch (IOException ignored) {
                        }
                    }
                }
            }
        }
        return inputStream;
    }

    protected static class AggregateIterator<E> implements Iterator<E> {
        LinkedList<Enumeration<E>> enums;
        Enumeration<E> cur;
        E next;
        Set<E> loaded;

        AggregateIterator() {
            this.enums = new LinkedList<>();
            this.cur = null;
            this.next = null;
            this.loaded = new HashSet<>();
        }

        AggregateIterator<E> addEnumeration(Enumeration<E> e) {
            if (e.hasMoreElements()) {
                if (this.cur == null) {
                    this.cur = e;
                    this.next = e.nextElement();
                    this.loaded.add(this.next);
                } else {
                    this.enums.add(e);
                }
            }
            return this;
        }

        @Override
        public boolean hasNext() {
            return this.next != null;
        }

        @Override
        public E next() {
            if (this.next != null) {
                E prev = this.next;
                this.next = loadNext();
                return prev;
            }
            throw new NoSuchElementException();
        }

        private Enumeration<E> determineCurrentEnumeration() {
            if ((this.cur != null) && (!this.cur.hasMoreElements())) {
                if (!this.enums.isEmpty()) {
                    this.cur = this.enums.removeLast();
                } else {
                    this.cur = null;
                }
            }
            return this.cur;
        }

        private E loadNext() {
            if (determineCurrentEnumeration() != null) {
                E tmp = this.cur.nextElement();
                do {
                    if (!this.loaded.contains(tmp)) {
                        break;
                    }
                    tmp = loadNext();
                } while (tmp != null);

                if (tmp != null) {
                    this.loaded.add(tmp);
                }
                return tmp;
            }
            return null;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

}
