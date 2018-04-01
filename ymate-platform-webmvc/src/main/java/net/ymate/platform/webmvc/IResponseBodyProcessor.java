/*
 * Copyright 2007-2018 the original author or authors.
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
package net.ymate.platform.webmvc;

import net.ymate.platform.webmvc.impl.DefaultResponseBodyProcessor;
import net.ymate.platform.webmvc.view.IView;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/1/10 上午3:15
 * @version 1.0
 */
public interface IResponseBodyProcessor {

    IResponseBodyProcessor DEFAULT = new DefaultResponseBodyProcessor();

    /**
     * 处理响应内容
     *
     * @param owner       所属YMP框架管理器实例
     * @param result      控制器方法执行结果对象
     * @param contentType 是否需要设置ContentType响应头信息
     * @param keepNull    是否保留空值
     * @param quoteField  是否为键名使用引号
     * @return 返回响应视图对象, 若为null将交由框架默认处理
     * @throws Exception 可能产生的任何异常
     */
    IView processBody(IWebMvc owner, Object result, boolean contentType, boolean keepNull, boolean quoteField) throws Exception;
}
