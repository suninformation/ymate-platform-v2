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

import net.ymate.platform.webmvc.annotation.Controller;
import net.ymate.platform.webmvc.annotation.RequestMapping;
import net.ymate.platform.webmvc.annotation.RequestSuffix;
import net.ymate.platform.webmvc.context.WebContext;
import net.ymate.platform.webmvc.view.IView;
import net.ymate.platform.webmvc.view.View;

/**
 * 扩展名支持测试控制器
 *
 * @author 刘镇 (suninformation@163.com) on 2026-02-11 04:36:23
 * @since 2.1.4
 */
@Controller
@RequestMapping(value = "/suffix")
public class SuffixTestController {

    // 测试1：精确扩展名匹配 - 只匹配 /suffix/test.html
    @RequestMapping(value = "/test", suffix = ".html")
    public IView testHtml(@RequestSuffix String suffix) {
        return View.textView("HTML测试 - 扩展名: " + suffix);
    }

    // 测试2：精确扩展名匹配 - 只匹配 /suffix/test.json
    @RequestMapping(value = "/test", suffix = ".json")
    public IView testJson(@RequestSuffix String suffix) {
        return View.textView("JSON测试 - 扩展名: " + suffix);
    }

    // 测试3：通配符扩展名匹配 - 匹配 /suffix/any 后面跟任意扩展名
    @RequestMapping(value = "/any", suffix = ".*")
    public IView testAny(@RequestSuffix String suffix) {
        return View.textView("通配符测试 - 扩展名: " + suffix);
    }

    // 测试4：无扩展名限制 - 匹配 /suffix/default 后面允许跟扩展名
    @RequestMapping(value = "/default")
    public IView testDefault(@RequestSuffix String suffix) {
        return View.textView("默认测试 - 扩展名: " + suffix);
    }

    // 测试5：结合路径变量和扩展名
    @RequestMapping(value = "/path/{id}", suffix = ".*")
    public IView testPath(@RequestSuffix String suffix, @RequestSuffix String format) {
        String id = WebContext.getRequestContext().getAttribute("id");
        return View.textView("路径变量测试 - ID: " + id + ", 扩展名: " + suffix + ", 格式: " + format);
    }

    // 测试6：类成员字段注入
    @RequestSuffix
    private String suffix;

    @RequestMapping(value = "/field", suffix = ".*")
    public IView testField() {
        return View.textView("字段注入测试 - 扩展名: " + suffix);
    }

    // 测试7：无扩展名限制 - 不允许有扩展名
    @RequestMapping(value = "/no-suffix")
    public IView testNoSuffix() {
        return View.textView("无扩展名测试 - 不允许有扩展名");
    }

    // 测试8：多个精确扩展名匹配
    @RequestMapping(value = "/multiple", suffix = {".xml", ".json", ".txt"})
    public IView testMultiple(@RequestSuffix String suffix) {
        return View.textView("多扩展名测试 - 扩展名: " + suffix);
    }
}
