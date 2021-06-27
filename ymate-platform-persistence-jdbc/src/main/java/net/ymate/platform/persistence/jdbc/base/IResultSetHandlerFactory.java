/*
 * Copyright 2007-2021 the original author or authors.
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
package net.ymate.platform.persistence.jdbc.base;

/**
 * @author 刘镇 (suninformation@163.com) on 2021/6/27 1:32 下午
 * @since 2.1.0
 */
public interface IResultSetHandlerFactory<T> {

    /**
     * 创建结果集数据处理器接口实例
     *
     * @return 返回结果集数据处理器接口实例对象
     */
    IResultSetHandler<T> create();
}
