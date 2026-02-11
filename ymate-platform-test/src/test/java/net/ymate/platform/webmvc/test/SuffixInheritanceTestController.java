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
import net.ymate.platform.webmvc.view.IView;
import net.ymate.platform.webmvc.view.View;

/**
 * 扩展名继承测试控制器
 *
 * @author 刘镇 (suninformation@163.com) on 2026-02-11 21:20:30
 * @since 2.1.4
 */
@Controller
@RequestMapping(value = "/inheritance", suffix = ".*")
public class SuffixInheritanceTestController {

    // 测试1：继承类级别的扩展名设置（允许任意扩展名）
    @RequestMapping(value = "/class-inherit")
    public IView testClassInherit(@RequestSuffix String suffix) {
        return View.textView("类继承测试 - 扩展名: " + suffix);
    }

    // 测试2：覆盖类级别的扩展名设置（只允许.html扩展名）
    @RequestMapping(value = "/method-override", suffix = ".html")
    public IView testMethodOverride(@RequestSuffix String suffix) {
        return View.textView("方法覆盖测试 - 扩展名: " + suffix);
    }

    // 测试3：覆盖类级别的扩展名设置（不允许有扩展名）
    // 注意：由于我们的实现逻辑，当方法上显式声明了suffix = {}时，会继承类级别的设置
    // 所以这个测试实际上会允许任意扩展名
    @RequestMapping(value = "/method-no-suffix")
    public IView testMethodNoSuffix() {
        return View.textView("方法无扩展名测试 - 继承类级别设置（允许任意扩展名）");
    }

    // 测试4：继承类级别设置，同时添加更多扩展名
    @RequestMapping(value = "/method-add", suffix = {".xml", ".json", ".txt"})
    public IView testMethodAdd(@RequestSuffix String suffix) {
        return View.textView("方法添加测试 - 扩展名: " + suffix);
    }
}
