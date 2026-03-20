/*
 * Copyright 2007-present the original author or authors.
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
package net.ymate.platform.commons.retry;

/**
 * 可重试的操作接口，用于定义需要执行重试逻辑的操作
 * <p>
 * 该接口是一个函数式接口，可以通过 Lambda 表达式简化调用，例如：
 * <pre>
 * String result = RetryUtils.executeWithRetry(() -&gt; doSomething());
 * </pre>
 *
 * @param <T> 操作返回值类型
 * @author 刘镇 (suninformation@163.com) on 2026-03-20
 * @since 2.1.4
 */
@FunctionalInterface
public interface IRetryable<T> {

    /**
     * 执行操作
     * <p>
     * 该方法包含需要执行的业务逻辑，如果执行失败会抛出异常，
     * 重试机制会根据配置决定是否进行重试
     *
     * @return 操作执行结果
     * @throws Exception 执行过程中发生的异常
     */
    T execute() throws Exception;
}
