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
package net.ymate.platform.webmvc;

import net.ymate.platform.core.beans.annotation.Ignored;
import net.ymate.platform.webmvc.base.Type;

import java.util.Map;

/**
 * WebMVC请求上下文接口，分析请求路径，仅返回控制器请求映射
 *
 * @author 刘镇 (suninformation@163.com) on 2012-12-17下午11:39:56
 */
@Ignored
public interface IRequestContext {

    /**
     * 获取请求映射字符串(注 : 必须以字符 ' / ' 开始且不以 ' / ' 结束)
     *
     * @return 返回请求映射字符串
     */
    String getRequestMapping();

    /**
     * 获取原始URL请求路径
     *
     * @return 返回原始URL请求路径
     */
    String getOriginalUrl();

    /**
     * 获取URL前缀
     *
     * @return 返回URL前缀
     */
    String getPrefix();

    /**
     * 获取URL后缀(扩展名称)
     *
     * @return 返回URL后缀
     */
    String getSuffix();

    /**
     * 是否开启严格模式
     *
     * @return 返回true表示开启
     * @since 2.1.3
     */
    boolean isStrictMode();

    /**
     * 获取当前请求方式
     *
     * @return 返回当前请求方式
     */
    Type.HttpMethod getHttpMethod();

    /**
     * 获取指定名称的属性值
     *
     * @param name 属性名称
     * @param <T>  属性类型
     * @return 返回属性值
     */
    <T> T getAttribute(String name);

    /**
     * 添加属性
     *
     * @param name  属性名称
     * @param value 属性值
     * @return 返回当前上下文对象
     */
    IRequestContext addAttribute(String name, Object value);

    /**
     * 获取所有属性
     *
     * @return 返回属性映射
     */
    Map<String, Object> getAttributes();
}
