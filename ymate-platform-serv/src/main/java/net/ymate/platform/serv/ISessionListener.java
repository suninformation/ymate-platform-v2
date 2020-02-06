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
package net.ymate.platform.serv;

import net.ymate.platform.core.beans.annotation.Ignored;

/**
 * @param <SESSION_WRAPPER> 会话包装器类型
 * @author 刘镇 (suninformation@163.com) on 2018/11/21 9:20 PM
 */
@Ignored
public interface ISessionListener<SESSION_WRAPPER extends ISessionWrapper<?, ?>> {

    /**
     * 空闲会话移除事件处理
     *
     * @param sessionWrapper 被移除会话对象包装器
     */
    void onSessionIdleRemoved(SESSION_WRAPPER sessionWrapper);
}
