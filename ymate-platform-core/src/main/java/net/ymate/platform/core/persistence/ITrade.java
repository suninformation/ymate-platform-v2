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
package net.ymate.platform.core.persistence;

import net.ymate.platform.core.beans.annotation.Ignored;

/**
 * 事务业务操作接口
 *
 * @author 刘镇 (suninformation@163.com) on 15/4/28 下午7:28
 */
@Ignored
public interface ITrade {

    /**
     * 执行事务处理
     *
     * @throws Throwable 可能产生的异常
     */
    void deal() throws Throwable;
}
