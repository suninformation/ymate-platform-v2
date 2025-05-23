/*
 * Copyright 2007-2025 the original author or authors.
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
package net.ymate.platform.commons.ext;

import net.ymate.platform.commons.lang.BlurObject;

import java.io.Serializable;
import java.util.Map;

/**
 * 扩展属性接口定义
 * (提取自 ymate-module-security 模块)
 *
 * @author 刘镇 (suninformation@163.com) on 2022/3/6 2:59 AM
 * @since 2.1.4
 */
public interface IAttributeExt extends Serializable {

    /**
     * 获取属性映射
     *
     * @return 返回属性映射
     */
    Map<String, Object> getAttributes();

    /**
     * 获取指定名称的属性值
     *
     * @param name 属性名称
     * @return 返回属性值
     */
    Object getAttribute(String name);

    BlurObject getAttributeBlur(String name);

    /**
     * 添加属性
     *
     * @param name  属性名称
     * @param value 属性值
     */
    void addAttribute(String name, Object value);

    /**
     * 删除属性
     *
     * @param names 属性名称集合（为空时表示清空）
     */
    void removeAttributes(String... names);

    /**
     * 判断指定名称的属性是否已存在
     *
     * @param name 属性名称
     * @return 返回true表示已存在
     */
    boolean hasAttribute(String name);
}
