/*
 * Copyright 2007-present the original author or authors.
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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;

import static org.junit.Assert.*;

/**
 * ResourceUtils测试类
 *
 * @author 刘镇 (suninformation@163.com) on 2026-01-04 23:12:19
 * @since 2.1.4
 */
public class ResourceUtilsTest {

    private static final String TEST_RESOURCE_NAME = "test-resource.properties";
    private static final String NON_EXISTENT_RESOURCE = "non-existent-resource.txt";
    private static final String EMPTY_RESOURCE = "";

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * 测试获取资源URL
     */
    @Test
    public void testGetResource() {
        // 测试获取不存在的资源
        URL nonExistentUrl = ResourceUtils.getResource(NON_EXISTENT_RESOURCE, getClass());
        assertNull(nonExistentUrl);

        // 测试获取空资源名称
        URL emptyUrl = ResourceUtils.getResource(EMPTY_RESOURCE, getClass());
        assertNull(emptyUrl);
    }

    /**
     * 测试获取资源迭代器
     *
     * @throws IOException 如果发生I/O错误
     */
    @Test
    public void testGetResources() throws IOException {
        // 测试获取不存在的资源
        Iterator<URL> nonExistentIterator = ResourceUtils.getResources(NON_EXISTENT_RESOURCE, getClass(), false);
        assertNotNull(nonExistentIterator);
        assertFalse(nonExistentIterator.hasNext());

        // 测试获取空资源名称
        Iterator<URL> emptyIterator = ResourceUtils.getResources(EMPTY_RESOURCE, getClass(), false);
        assertNotNull(emptyIterator);
        assertFalse(emptyIterator.hasNext());

        // 测试聚合模式
        Iterator<URL> aggregateIterator = ResourceUtils.getResources(NON_EXISTENT_RESOURCE, getClass(), true);
        assertNotNull(aggregateIterator);
        assertFalse(aggregateIterator.hasNext());
    }

    /**
     * 测试获取资源输入流
     *
     * @throws IOException 如果发生I/O错误
     */
    @Test
    public void testGetResourceAsStream() throws IOException {
        // 测试获取不存在的资源
        InputStream nonExistentStream = ResourceUtils.getResourceAsStream(NON_EXISTENT_RESOURCE, getClass());
        assertNull(nonExistentStream);

        // 测试获取空资源名称
        InputStream emptyStream = ResourceUtils.getResourceAsStream(EMPTY_RESOURCE, getClass());
        assertNull(emptyStream);
    }

    /**
     * 测试按数组顺序获取资源输入流
     */
    @Test
    public void testGetResourceAsStreamWithArray() {
        // 测试正常情况
        InputStream stream1 = ResourceUtils.getResourceAsStream(getClass(), NON_EXISTENT_RESOURCE);
        assertNull(stream1);

        // 测试多个文件路径
        InputStream stream2 = ResourceUtils.getResourceAsStream(getClass(), NON_EXISTENT_RESOURCE, EMPTY_RESOURCE);
        assertNull(stream2);

        // 测试日志控制
        InputStream stream3 = ResourceUtils.getResourceAsStream(getClass(), String.valueOf(false), NON_EXISTENT_RESOURCE);
        assertNull(stream3);
    }
}
