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

import net.ymate.platform.validation.annotation.VCondition;
import net.ymate.platform.validation.annotation.ValidateGroups;
import net.ymate.platform.webmvc.annotation.Controller;
import net.ymate.platform.webmvc.annotation.RequestMapping;
import net.ymate.platform.webmvc.annotation.RequestParam;
import net.ymate.platform.webmvc.validate.VHostName;
import net.ymate.platform.webmvc.view.IView;
import net.ymate.platform.webmvc.view.View;

/**
 * WebMVC验证注解分组与条件测试控制器
 *
 * @author 刘镇 (suninformation@163.com) on 2026/6/22 01:02
 * @since 2.1.4
 */
@Controller
@RequestMapping("/validate")
public class ValidationWebTestController {

    /**
     * 创建操作分组
     */
    public interface Create {
    }

    /**
     * 更新操作分组
     */
    public interface Update {
    }

    /**
     * VHostName分组测试 - 仅Create分组验证hostName
     */
    @RequestMapping("/hostname/create")
    public IView testHostNameCreate(@RequestParam @VHostName(groups = Create.class) String hostName) {
        return View.textView("OK");
    }

    /**
     * VHostName分组测试 - 仅Update分组验证hostName
     */
    @RequestMapping("/hostname/update")
    public IView testHostNameUpdate(@RequestParam @VHostName(groups = Update.class) String hostName) {
        return View.textView("OK");
    }

    /**
     * VHostName默认分组测试 - Default分组验证hostName
     */
    @RequestMapping("/hostname/default")
    public IView testHostNameDefault(@RequestParam @VHostName String hostName) {
        return View.textView("OK");
    }

    /**
     * VHostName条件测试 - 当type为url时验证hostName
     */
    @RequestMapping("/hostname/condition")
    public IView testHostNameCondition(
            @RequestParam String type,
            @RequestParam @VHostName(condition = @VCondition(type = VCondition.Type.FIELD_EQUALS, field = "type", expectedValue = "url")) String hostName) {
        return View.textView("OK");
    }

    // ==================== @ValidateGroups 分组测试 ====================

    /**
     * 方法级@ValidateGroups测试 - 声明Create分组
     */
    @RequestMapping("/hostname/groupCreate")
    @ValidateGroups(Create.class)
    public IView testHostNameGroupCreate(@RequestParam @VHostName(groups = Create.class) String hostName) {
        return View.textView("OK");
    }

    /**
     * 方法级@ValidateGroups测试 - 声明Update分组
     */
    @RequestMapping("/hostname/groupUpdate")
    @ValidateGroups(Update.class)
    public IView testHostNameGroupUpdate(@RequestParam @VHostName(groups = Update.class) String hostName) {
        return View.textView("OK");
    }

    /**
     * 方法级@ValidateGroups测试 - 声明DefaultGroup分组
     */
    @RequestMapping("/hostname/groupDefault")
    @ValidateGroups
    public IView testHostNameGroupDefault(@RequestParam @VHostName String hostName) {
        return View.textView("OK");
    }
}
