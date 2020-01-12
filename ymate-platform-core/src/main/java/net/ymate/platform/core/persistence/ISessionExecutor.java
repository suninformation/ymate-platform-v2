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

/**
 * 基准会话执行器接口
 *
 * @param <RESULT>  结果集类型
 * @param <SESSION> 会话类型
 * @author 刘镇 (suninformation@163.com) on 2019-05-16 01:04
 * @since 2.1.0
 */
public interface ISessionExecutor<RESULT, SESSION extends ISession> {

    /**
     * 执行会话处理过程
     *
     * @param session 会话对象
     * @return 返回执行结果
     * @throws Exception 可能产生的异常
     */
    RESULT execute(SESSION session) throws Exception;
}
