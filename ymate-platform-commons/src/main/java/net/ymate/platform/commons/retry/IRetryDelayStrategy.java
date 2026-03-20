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
 * 重试延迟策略接口，用于定义重试之间的延迟计算逻辑
 * <p>
 * 实现此接口可以自定义不同的延迟策略，例如：
 * <ul>
 *     <li>固定延迟：每次重试使用相同的延迟时间</li>
 *     <li>指数退避：延迟时间随重试次数指数增长</li>
 *     <li>随机延迟：在指定范围内随机选择延迟时间</li>
 * </ul>
 *
 * @author 刘镇 (suninformation@163.com) on 2026-03-20
 * @see RetryConfig.FixedDelayStrategy
 * @see RetryConfig.ExponentialDelayStrategy
 * @see RetryConfig.RandomDelayStrategy
 * @since 2.1.4
 */
public interface IRetryDelayStrategy {

    /**
     * 计算指定尝试次数的延迟时间
     *
     * @param attempt 当前尝试次数（从 1 开始计数）
     * @return 延迟时间（毫秒），必须为正数
     */
    long getDelay(int attempt);
}
