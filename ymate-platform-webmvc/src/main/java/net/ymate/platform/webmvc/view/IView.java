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
package net.ymate.platform.webmvc.view;

import net.ymate.platform.core.beans.annotation.Ignored;

import java.io.OutputStream;
import java.io.Serializable;
import java.util.Map;

/**
 * 视图接口
 *
 * @author 刘镇 (suninformation@163.com) on 2012-12-5 下午3:11:32
 */
@Ignored
public interface IView extends Serializable {

    String DEFAULT_CHARSET = "UTF-8";

    /**
     * 添加视图属性
     *
     * @param name  属性名称
     * @param value 属性值
     * @return 返回当前视图对象
     */
    IView addAttribute(String name, Object value);

    /**
     * 添加视图属性
     *
     * @param attributes 属性映射
     * @return 返回当前视图对象
     */
    IView addAttributes(Map<String, Object> attributes);

    /**
     * 获取视图对象属性key的值
     *
     * @param <T>  属性类型
     * @param name 属性名称
     * @return 返回视图对象属性key的值
     */
    <T> T getAttribute(String name);

    /**
     * 获取视图对象的属性映射
     *
     * @return 返回视图对象的属性映射
     */
    Map<String, Object> getAttributes();

    /**
     * 获取视图内容类型
     *
     * @return 返回视图内容类型
     */
    String getContentType();

    /**
     * 设置视图内容类型
     *
     * @param contentType 内容类型
     * @return 返回当前视图对象
     */
    IView setContentType(String contentType);

    /**
     * 设置请求回应头
     *
     * @param name Head名称
     * @param date 值
     * @return 返回当前视图对象
     */
    IView addDateHeader(String name, long date);

    /**
     * 设置请求回应头
     *
     * @param name  Head名称
     * @param value 值
     * @return 返回当前视图对象
     */
    IView addHeader(String name, String value);

    /**
     * 设置请求回应头
     *
     * @param name  Head名称
     * @param value 值
     * @return 返回当前视图对象
     */
    IView addIntHeader(String name, int value);

    /**
     * 视图渲染动作
     *
     * @throws Exception 抛出任何可能异常
     */
    void render() throws Exception;

    /**
     * 视图渲染动作
     *
     * @param output 视图渲染指定输出流
     * @throws Exception 抛出任何可能异常
     */
    void render(OutputStream output) throws Exception;
}
