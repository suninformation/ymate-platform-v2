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
package net.ymate.platform.commons.lang;

/**
 * 类型转换器接口
 *
 * @param <T> 目标类型
 * @author 刘镇 (suninformation@163.com) on 2019-05-20 22:18
 */
public interface IConverter<T> {

    /**
     * 尝试类型转换
     *
     * @param target 目标对象
     * @return 返回转换后的对象
     */
    T convert(Object target);
}
