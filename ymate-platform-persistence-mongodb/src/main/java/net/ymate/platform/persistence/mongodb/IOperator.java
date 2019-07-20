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
package net.ymate.platform.persistence.mongodb;

import net.ymate.platform.core.beans.annotation.Ignored;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/27 下午5:59
 */
@Ignored
public interface IOperator extends IBsonable {

    /**
     * 添加操作器
     *
     * @param operator 操作器对象
     */
    void add(IOperator operator);
}
