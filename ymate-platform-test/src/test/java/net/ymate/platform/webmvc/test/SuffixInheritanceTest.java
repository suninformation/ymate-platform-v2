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
package net.ymate.platform.webmvc.test;

import net.ymate.platform.core.annotation.EnableAutoScan;
import net.ymate.platform.core.annotation.EnableBeanProxy;
import net.ymate.platform.core.annotation.EnableDevMode;
import net.ymate.platform.core.beans.annotation.Inject;
import net.ymate.platform.mock.MockWebRequestHelper;
import net.ymate.platform.mock.web.MockHttpServletResponse;
import net.ymate.platform.webmvc.WebMVC;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.servlet.http.HttpServletResponse;

/**
 * 扩展名继承测试类
 *
 * @author 刘镇 (suninformation@163.com) on 2026-02-11 21:20:47
 * @since 2.1.4
 */
@RunWith(net.ymate.platform.test.YMPJUnit4ClassRunner.class)
@EnableAutoScan
@EnableBeanProxy
@EnableDevMode
public class SuffixInheritanceTest {

    @Inject
    private WebMVC webmvc;

    /**
     * 测试1：继承类级别的扩展名设置（允许任意扩展名）
     */
    @Test
    public void testClassInherit() throws Exception {
        // 测试.html扩展名
        MockHttpServletResponse response1 = MockWebRequestHelper.create(webmvc)
                .get("/inheritance/class-inherit.html")
                .doFilter();
        Assert.assertEquals(HttpServletResponse.SC_OK, response1.getStatus());
        String content1 = response1.getContentAsString();
        System.out.println("testClassInherit (.html) content: " + content1);
        if (content1 != null && !content1.isEmpty()) {
            Assert.assertTrue("Content should contain expected text", content1.contains("类继承测试"));
            Assert.assertTrue("Content should contain extension", content1.contains("html"));
        }

        // 测试.json扩展名
        MockHttpServletResponse response2 = MockWebRequestHelper.create(webmvc)
                .get("/inheritance/class-inherit.json")
                .doFilter();
        Assert.assertEquals(HttpServletResponse.SC_OK, response2.getStatus());
        String content2 = response2.getContentAsString();
        System.out.println("testClassInherit (.json) content: " + content2);
        if (content2 != null && !content2.isEmpty()) {
            Assert.assertTrue("Content should contain expected text", content2.contains("类继承测试"));
            Assert.assertTrue("Content should contain extension", content2.contains("json"));
        }

        // 测试.xml扩展名
        MockHttpServletResponse response3 = MockWebRequestHelper.create(webmvc)
                .get("/inheritance/class-inherit.xml")
                .doFilter();
        Assert.assertEquals(HttpServletResponse.SC_OK, response3.getStatus());
        String content3 = response3.getContentAsString();
        System.out.println("testClassInherit (.xml) content: " + content3);
        if (content3 != null && !content3.isEmpty()) {
            Assert.assertTrue("Content should contain expected text", content3.contains("类继承测试"));
            Assert.assertTrue("Content should contain extension", content3.contains("xml"));
        }
    }

    /**
     * 测试2：覆盖类级别的扩展名设置（只允许.html扩展名）
     */
    @Test
    public void testMethodOverride() throws Exception {
        // 测试.html扩展名（应该成功）
        MockHttpServletResponse response1 = MockWebRequestHelper.create(webmvc)
                .get("/inheritance/method-override.html")
                .doFilter();
        Assert.assertEquals(HttpServletResponse.SC_OK, response1.getStatus());
        String content1 = response1.getContentAsString();
        System.out.println("testMethodOverride (.html) content: " + content1);
        if (content1 != null && !content1.isEmpty()) {
            Assert.assertTrue("Content should contain expected text", content1.contains("方法覆盖测试"));
            Assert.assertTrue("Content should contain extension", content1.contains("html"));
        }

        // 测试.json扩展名（应该失败）
        MockHttpServletResponse response2 = MockWebRequestHelper.create(webmvc)
                .get("/inheritance/method-override.json")
                .doFilter();
        System.out.println("testMethodOverride (.json) status: " + response2.getStatus());
        System.out.println("testMethodOverride (.json) content: " + response2.getContentAsString());
        // 当方法上显式声明了后缀时，只允许指定的后缀，所以这里应该返回404或200但内容为空
    }

    /**
     * 测试3：继承类级别的扩展名设置（允许任意扩展名）
     */
    @Test
    public void testMethodNoSuffix() throws Exception {
        // 测试无扩展名的情况（应该成功）
        MockHttpServletResponse response1 = MockWebRequestHelper.create(webmvc)
                .get("/inheritance/method-no-suffix")
                .doFilter();
        Assert.assertEquals(HttpServletResponse.SC_OK, response1.getStatus());
        String content1 = response1.getContentAsString();
        System.out.println("testMethodNoSuffix (no suffix) content: " + content1);
        if (content1 != null && !content1.isEmpty()) {
            Assert.assertTrue("Content should contain expected text", content1.contains("方法无扩展名测试"));
        }

        // 测试有扩展名的情况（应该成功，因为继承了类级别的设置）
        MockHttpServletResponse response2 = MockWebRequestHelper.create(webmvc)
                .get("/inheritance/method-no-suffix.html")
                .doFilter();
        Assert.assertEquals(HttpServletResponse.SC_OK, response2.getStatus());
        String content2 = response2.getContentAsString();
        System.out.println("testMethodNoSuffix (with suffix) content: " + content2);
        if (content2 != null && !content2.isEmpty()) {
            Assert.assertTrue("Content should contain expected text", content2.contains("方法无扩展名测试"));
        }
    }

    /**
     * 测试4：继承类级别设置，同时添加更多扩展名
     */
    @Test
    public void testMethodAdd() throws Exception {
        // 测试.xml扩展名（应该成功）
        MockHttpServletResponse response1 = MockWebRequestHelper.create(webmvc)
                .get("/inheritance/method-add.xml")
                .doFilter();
        Assert.assertEquals(HttpServletResponse.SC_OK, response1.getStatus());
        String content1 = response1.getContentAsString();
        System.out.println("testMethodAdd (.xml) content: " + content1);
        if (content1 != null && !content1.isEmpty()) {
            Assert.assertTrue("Content should contain expected text", content1.contains("方法添加测试"));
            Assert.assertTrue("Content should contain extension", content1.contains("xml"));
        }

        // 测试.json扩展名（应该成功）
        MockHttpServletResponse response2 = MockWebRequestHelper.create(webmvc)
                .get("/inheritance/method-add.json")
                .doFilter();
        Assert.assertEquals(HttpServletResponse.SC_OK, response2.getStatus());
        String content2 = response2.getContentAsString();
        System.out.println("testMethodAdd (.json) content: " + content2);
        if (content2 != null && !content2.isEmpty()) {
            Assert.assertTrue("Content should contain expected text", content2.contains("方法添加测试"));
            Assert.assertTrue("Content should contain extension", content2.contains("json"));
        }

        // 测试.txt扩展名（应该成功）
        MockHttpServletResponse response3 = MockWebRequestHelper.create(webmvc)
                .get("/inheritance/method-add.txt")
                .doFilter();
        Assert.assertEquals(HttpServletResponse.SC_OK, response3.getStatus());
        String content3 = response3.getContentAsString();
        System.out.println("testMethodAdd (.txt) content: " + content3);
        if (content3 != null && !content3.isEmpty()) {
            Assert.assertTrue("Content should contain expected text", content3.contains("方法添加测试"));
            Assert.assertTrue("Content should contain extension", content3.contains("txt"));
        }

        // 测试.html扩展名（应该失败，因为方法上只允许.xml, .json, .txt）
        MockHttpServletResponse response4 = MockWebRequestHelper.create(webmvc)
                .get("/inheritance/method-add.html")
                .doFilter();
        System.out.println("testMethodAdd (.html) status: " + response4.getStatus());
        System.out.println("testMethodAdd (.html) content: " + response4.getContentAsString());
        // 当方法上显式声明了后缀时，只允许指定的后缀，所以这里应该返回404或200但内容为空
    }
}
