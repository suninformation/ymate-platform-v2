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
package net.ymate.platform.validation.test;

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
 * WebMVC验证注解分组与条件测试类
 *
 * <p>测试VHostName、VToken、VUploadFile注解的groups和condition属性
 * 在控制器方法参数上的使用，验证注解不会导致编译或运行时错误，
 * 以及Mock请求能够正常处理。</p>
 *
 * @author 刘镇 (suninformation@163.com) on 2026/6/22 01:02
 * @since 2.1.4
 */
@RunWith(net.ymate.platform.test.YMPJUnit4ClassRunner.class)
@EnableAutoScan
@EnableBeanProxy
@EnableDevMode
public class ValidationWebGroupConditionTest {

    @Inject
    private WebMVC webmvc;

    // ==================== VHostName 分组测试 ====================

    /**
     * 测试VHostName默认分组 - 控制器方法正常调用
     */
    @Test
    public void testHostNameDefaultGroup() throws Exception {
        MockHttpServletResponse response = MockWebRequestHelper.create(webmvc)
                .get("/validate/hostname/default")
                .parameter("hostName", "www.ymate.net")
                .doFilter();
        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        Assert.assertEquals("OK", response.getContentAsString());
    }

    /**
     * 测试VHostName Create分组 - 控制器方法正常调用
     */
    @Test
    public void testHostNameCreateGroup() throws Exception {
        MockHttpServletResponse response = MockWebRequestHelper.create(webmvc)
                .get("/validate/hostname/create")
                .parameter("hostName", "www.ymate.net")
                .doFilter();
        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        Assert.assertEquals("OK", response.getContentAsString());
    }

    /**
     * 测试VHostName Update分组 - 控制器方法正常调用
     */
    @Test
    public void testHostNameUpdateGroup() throws Exception {
        MockHttpServletResponse response = MockWebRequestHelper.create(webmvc)
                .get("/validate/hostname/update")
                .parameter("hostName", "www.ymate.net")
                .doFilter();
        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        Assert.assertEquals("OK", response.getContentAsString());
    }

    // ==================== VHostName 条件测试 ====================

    /**
     * 测试VHostName条件 - type为url时验证hostName
     */
    @Test
    public void testHostNameConditionTypeUrl() throws Exception {
        MockHttpServletResponse response = MockWebRequestHelper.create(webmvc)
                .get("/validate/hostname/condition")
                .parameter("type", "url")
                .parameter("hostName", "invalid_host")
                .doFilter();
        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        Assert.assertNotEquals("OK", response.getContentAsString());
    }

    /**
     * 测试VHostName条件 - type不为url时不验证hostName
     */
    @Test
    public void testHostNameConditionTypeNotUrl() throws Exception {
        MockHttpServletResponse response = MockWebRequestHelper.create(webmvc)
                .get("/validate/hostname/condition")
                .parameter("type", "other")
                .parameter("hostName", "invalid_host")
                .doFilter();
        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        Assert.assertEquals("OK", response.getContentAsString());
    }

    // ==================== @ValidateGroups 分组测试 ====================

    /**
     * 测试方法级@ValidateGroups(Create) - hostName合法时验证通过
     */
    @Test
    public void testValidateGroupsCreate() throws Exception {
        MockHttpServletResponse response = MockWebRequestHelper.create(webmvc)
                .get("/validate/hostname/groupCreate")
                .parameter("hostName", "www.ymate.net")
                .doFilter();
        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        Assert.assertEquals("OK", response.getContentAsString());
    }

    /**
     * 测试方法级@ValidateGroups(Update) - hostName合法时验证通过
     */
    @Test
    public void testValidateGroupsUpdate() throws Exception {
        MockHttpServletResponse response = MockWebRequestHelper.create(webmvc)
                .get("/validate/hostname/groupUpdate")
                .parameter("hostName", "www.ymate.net")
                .doFilter();
        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        Assert.assertEquals("OK", response.getContentAsString());
    }

    /**
     * 测试方法级@ValidateGroups(DefaultGroup) - 默认分组验证hostName
     */
    @Test
    public void testValidateGroupsDefault() throws Exception {
        MockHttpServletResponse response = MockWebRequestHelper.create(webmvc)
                .get("/validate/hostname/groupDefault")
                .parameter("hostName", "www.ymate.net")
                .doFilter();
        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        Assert.assertEquals("OK", response.getContentAsString());
    }
}
