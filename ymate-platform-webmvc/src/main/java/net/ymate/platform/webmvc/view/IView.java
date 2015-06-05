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
package net.ymate.platform.webmvc.view;

import net.ymate.platform.webmvc.base.Type;

import java.io.OutputStream;
import java.util.Map;

/**
 * WebMVC视图接口
 *
 * @author 刘镇 (suninformation@163.com) on 2012-12-5 下午3:11:32
 * @version 1.0
 */
public interface IView {

    /**
     * 添加视图属性
     *
     * @param name  属性名称
     * @param value 属性值
     * @return
     */
    public IView addAttribute(String name, Object value);

    /**
     * @param name 属性名称
     * @return 返回视图对象属性key的值
     */
    public <T> T getAttribute(String name);

    /**
     * @return 返回视图对象的属性映射
     */
    public Map<String, Object> getAttributes();

    /**
     * @return 返回视图内容类型
     */
    public String getContentType();

    /**
     * 设置视图内容类型
     *
     * @param contentType 内容类型
     */
    public IView setContentType(String contentType);

    /**
     * @param name
     * @param date
     * @return 设置请求回应头
     */
    public IView addDateHeader(String name, long date);

    /**
     * @param name
     * @param value
     * @return 设置请求回应头
     */
    public IView addHeader(String name, String value);

    /**
     * @param name
     * @param value
     * @return 设置请求回应头
     */
    public IView addIntHeader(String name, int value);

    /**
     * 视图渲染动作
     *
     * @throws Exception 抛出任何可能异常
     */
    public void render() throws Exception;

    /**
     * @param output 视图渲染指定输出流
     * @throws Exception 抛出任何可能异常
     */
    public void render(OutputStream output) throws Exception;
}
