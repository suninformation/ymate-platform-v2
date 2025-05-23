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

import java.io.Serializable;
import java.util.Map;

/**
 * 扩展属性构建器接口定义
 * (提取自 ymate-module-security 模块)
 *
 * @author 刘镇 (suninformation@163.com) on 2022/3/6 2:59 AM
 * @since 2.1.4
 */
public interface IAttributeExtBuilder<T> extends Serializable {

    T attributes(Map<String, Object> attributes);

    T attribute(String name, Object value);
}
