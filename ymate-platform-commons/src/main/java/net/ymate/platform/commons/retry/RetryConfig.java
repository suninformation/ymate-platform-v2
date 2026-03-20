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

import java.util.Random;

/**
 * 重试配置类，用于定义重试行为的相关参数
 * <p>
 * 该类提供了完整的重试配置选项，包括：
 * <ul>
 *     <li>最大重试次数</li>
 *     <li>延迟策略（固定、指数退避、随机）</li>
 *     <li>总超时时间</li>
 *     <li>可重试的异常类型</li>
 * </ul>
 * <p>
 * 使用示例：
 * <pre>
 * // 使用默认配置
 * RetryConfig config = RetryConfig.defaultConfig();
 *
 * // 使用构建器自定义配置
 * RetryConfig config = RetryConfig.custom()
 *     .maxRetries(5)
 *     .exponentialDelay(1000, 60000)
 *     .retryableExceptions(IOException.class, TimeoutException.class)
 *     .build();
 * </pre>
 *
 * @author 刘镇 (suninformation@163.com) on 2026-03-20
 * @since 2.1.4
 */
public class RetryConfig {

    /**
     * 默认最大重试次数
     */
    public static final int DEFAULT_MAX_RETRIES = 3;

    /**
     * 默认初始延迟时间（毫秒）
     */
    public static final long DEFAULT_INITIAL_DELAY_MS = 1000;

    /**
     * 默认总超时时间（毫秒）- 5 分钟
     */
    public static final long DEFAULT_TOTAL_TIMEOUT_MS = 5 * 60 * 1000;

    private int maxRetries;
    private long initialDelayMs;
    private Long totalTimeoutMs;
    private IRetryDelayStrategy delayStrategy;
    private Class<? extends Exception>[] retryableExceptions;

    /**
     * 构造默认重试配置
     * <p>
     * 默认配置包括：最大重试次数 3 次、初始延迟 1000 毫秒、总超时 5 分钟、指数延迟策略
     */
    @SuppressWarnings("unchecked")
    public RetryConfig() {
        this.maxRetries = DEFAULT_MAX_RETRIES;
        this.initialDelayMs = DEFAULT_INITIAL_DELAY_MS;
        this.totalTimeoutMs = DEFAULT_TOTAL_TIMEOUT_MS;
        this.delayStrategy = new ExponentialDelayStrategy(initialDelayMs);
        this.retryableExceptions = new Class[0];
    }

    /**
     * 设置最大重试次数
     *
     * @param maxRetries 最大重试次数，必须大于 0
     * @throws IllegalArgumentException 如果 maxRetries 小于等于 0
     */
    public void setMaxRetries(int maxRetries) {
        if (maxRetries <= 0) {
            throw new IllegalArgumentException("maxRetries must be greater than 0");
        }
        this.maxRetries = maxRetries;
    }

    /**
     * 设置初始延迟时间
     *
     * @param initialDelayMs 初始延迟时间（毫秒），必须大于 0
     * @throws IllegalArgumentException 如果 initialDelayMs 小于等于 0
     */
    public void setInitialDelayMs(long initialDelayMs) {
        if (initialDelayMs <= 0) {
            throw new IllegalArgumentException("initialDelayMs must be greater than 0");
        }
        this.initialDelayMs = initialDelayMs;
    }

    /**
     * 设置总超时时间
     *
     * @param totalTimeoutMs 总超时时间（毫秒），null 表示不限制超时时间
     * @throws IllegalArgumentException 如果 totalTimeoutMs 小于等于 0
     */
    public void setTotalTimeoutMs(Long totalTimeoutMs) {
        if (totalTimeoutMs != null && totalTimeoutMs <= 0) {
            throw new IllegalArgumentException("totalTimeoutMs must be greater than 0");
        }
        this.totalTimeoutMs = totalTimeoutMs;
    }

    /**
     * 设置延迟策略
     *
     * @param delayStrategy 延迟策略实例，不能为 null
     */
    public void setDelayStrategy(IRetryDelayStrategy delayStrategy) {
        this.delayStrategy = delayStrategy;
    }

    /**
     * 设置可重试的异常类型
     * <p>
     * 只有抛出指定类型的异常时才会进行重试，其他异常将直接抛出
     *
     * @param retryableExceptions 可重试的异常类型数组，为空或 null 表示所有异常都可重试
     */
    @SuppressWarnings("unchecked")
    @SafeVarargs
    public final void setRetryableExceptions(Class<? extends Exception>... retryableExceptions) {
        this.retryableExceptions = retryableExceptions != null ? retryableExceptions : new Class[0];
    }

    /**
     * 获取最大重试次数
     *
     * @return 最大重试次数
     */
    public int getMaxRetries() {
        return maxRetries;
    }

    /**
     * 获取初始延迟时间
     *
     * @return 初始延迟时间（毫秒）
     */
    public long getInitialDelayMs() {
        return initialDelayMs;
    }

    /**
     * 获取总超时时间
     *
     * @return 总超时时间（毫秒），null 表示不限制
     */
    public Long getTotalTimeoutMs() {
        return totalTimeoutMs;
    }

    /**
     * 获取延迟策略
     *
     * @return 延迟策略实例
     */
    public IRetryDelayStrategy getDelayStrategy() {
        return delayStrategy;
    }

    /**
     * 获取可重试的异常类型
     *
     * @return 可重试的异常类型数组
     */
    public Class<? extends Exception>[] getRetryableExceptions() {
        return retryableExceptions;
    }

    /**
     * 创建默认配置
     *
     * @return 默认重试配置实例
     */
    public static RetryConfig defaultConfig() {
        return new RetryConfig();
    }

    /**
     * 创建自定义配置构建器
     *
     * @return 配置构建器实例
     */
    public static Builder custom() {
        return new Builder();
    }

    /**
     * 固定延迟策略
     * <p>
     * 每次重试使用相同的延迟时间，适用于对延迟时间要求固定的场景
     *
     * @author 刘镇 (suninformation@163.com) on 2026-03-20
     * @since 2.1.4
     */
    public static class FixedDelayStrategy implements IRetryDelayStrategy {

        private final long delayMs;

        /**
         * 构造固定延迟策略
         *
         * @param delayMs 固定延迟时间（毫秒），必须大于 0
         * @throws IllegalArgumentException 如果 delayMs 小于等于 0
         */
        public FixedDelayStrategy(long delayMs) {
            if (delayMs <= 0) {
                throw new IllegalArgumentException("delayMs must be greater than 0");
            }
            this.delayMs = delayMs;
        }

        /**
         * 获取延迟时间
         *
         * @param attempt 当前尝试次数（此策略忽略该参数）
         * @return 固定的延迟时间（毫秒）
         */
        @Override
        public long getDelay(int attempt) {
            return delayMs;
        }
    }

    /**
     * 指数延迟策略（指数退避）
     * <p>
     * 延迟时间随重试次数指数增长，公式为：delay = initialDelay * 2^(attempt-1)
     * <p>
     * 适用于需要逐步增加重试间隔的场景，如网络请求、服务调用等
     *
     * @author 刘镇 (suninformation@163.com) on 2026-03-20
     * @since 2.1.4
     */
    public static class ExponentialDelayStrategy implements IRetryDelayStrategy {

        private final long initialDelayMs;
        private final long maxDelayMs;

        /**
         * 构造指数延迟策略
         *
         * @param initialDelayMs 初始延迟时间（毫秒），必须大于 0
         * @param maxDelayMs     最大延迟时间（毫秒），防止溢出和过长等待，必须大于 0
         * @throws IllegalArgumentException 如果参数小于等于 0
         */
        public ExponentialDelayStrategy(long initialDelayMs, long maxDelayMs) {
            if (initialDelayMs <= 0) {
                throw new IllegalArgumentException("initialDelayMs must be greater than 0");
            }
            if (maxDelayMs <= 0) {
                throw new IllegalArgumentException("maxDelayMs must be greater than 0");
            }
            this.initialDelayMs = initialDelayMs;
            this.maxDelayMs = maxDelayMs;
        }

        /**
         * 构造指数延迟策略（默认最大延迟为 1 小时）
         *
         * @param initialDelayMs 初始延迟时间（毫秒），必须大于 0
         * @throws IllegalArgumentException 如果 initialDelayMs 小于等于 0
         */
        public ExponentialDelayStrategy(long initialDelayMs) {
            this(initialDelayMs, 60 * 60 * 1000L);
        }

        /**
         * 计算延迟时间
         * <p>
         * 使用公式：delay = initialDelay * 2^(attempt-1)，结果不超过 maxDelayMs
         *
         * @param attempt 当前尝试次数（从 1 开始）
         * @return 计算得到的延迟时间（毫秒）
         */
        @Override
        public long getDelay(int attempt) {
            long delay = initialDelayMs * (long) Math.pow(2, attempt - 1);
            if (delay < 0 || delay > maxDelayMs) {
                delay = maxDelayMs;
            }
            return delay;
        }
    }

    /**
     * 随机延迟策略
     * <p>
     * 在指定的最小和最大延迟时间之间随机选择延迟时间，
     * 适用于需要避免重试请求同时发生的场景（如分布式系统中的惊群效应）
     *
     * @author 刘镇 (suninformation@163.com) on 2026-03-20
     * @since 2.1.4
     */
    public static class RandomDelayStrategy implements IRetryDelayStrategy {

        private final long minDelayMs;
        private final long maxDelayMs;
        private final Random random;

        /**
         * 构造随机延迟策略
         *
         * @param minDelayMs 最小延迟时间（毫秒），必须大于 0
         * @param maxDelayMs 最大延迟时间（毫秒），必须大于 0 且不小于 minDelayMs
         * @throws IllegalArgumentException 如果参数不满足条件
         */
        public RandomDelayStrategy(long minDelayMs, long maxDelayMs) {
            if (minDelayMs <= 0) {
                throw new IllegalArgumentException("minDelayMs must be greater than 0");
            }
            if (maxDelayMs <= 0) {
                throw new IllegalArgumentException("maxDelayMs must be greater than 0");
            }
            if (minDelayMs > maxDelayMs) {
                throw new IllegalArgumentException("minDelayMs must be less than or equal to maxDelayMs");
            }
            this.minDelayMs = minDelayMs;
            this.maxDelayMs = maxDelayMs;
            this.random = new Random();
        }

        /**
         * 获取随机延迟时间
         *
         * @param attempt 当前尝试次数（此策略忽略该参数）
         * @return 在 [minDelayMs, maxDelayMs] 范围内的随机延迟时间（毫秒）
         */
        @Override
        public long getDelay(int attempt) {
            return minDelayMs + (long) (random.nextDouble() * (maxDelayMs - minDelayMs));
        }
    }

    /**
     * 配置构建器
     * <p>
     * 提供链式调用的方式构建 RetryConfig 实例
     *
     * @author 刘镇 (suninformation@163.com) on 2026-03-20
     * @since 2.1.4
     */
    public static class Builder {

        private final RetryConfig config = new RetryConfig();

        /**
         * 设置最大重试次数
         *
         * @param maxRetries 最大重试次数，必须大于 0
         * @return 构建器实例
         */
        public Builder maxRetries(int maxRetries) {
            config.setMaxRetries(maxRetries);
            return this;
        }

        /**
         * 设置初始延迟时间
         *
         * @param initialDelayMs 初始延迟时间（毫秒），必须大于 0
         * @return 构建器实例
         */
        public Builder initialDelayMs(long initialDelayMs) {
            config.setInitialDelayMs(initialDelayMs);
            return this;
        }

        /**
         * 设置总超时时间
         *
         * @param totalTimeoutMs 总超时时间（毫秒），null 表示不限制
         * @return 构建器实例
         */
        public Builder totalTimeoutMs(Long totalTimeoutMs) {
            config.setTotalTimeoutMs(totalTimeoutMs);
            return this;
        }

        /**
         * 设置延迟策略
         *
         * @param delayStrategy 延迟策略实例
         * @return 构建器实例
         */
        public Builder delayStrategy(IRetryDelayStrategy delayStrategy) {
            config.setDelayStrategy(delayStrategy);
            return this;
        }

        /**
         * 使用固定延迟策略
         *
         * @param delayMs 固定延迟时间（毫秒），必须大于 0
         * @return 构建器实例
         */
        public Builder fixedDelay(long delayMs) {
            config.setDelayStrategy(new FixedDelayStrategy(delayMs));
            return this;
        }

        /**
         * 使用指数延迟策略（默认最大延迟 1 小时）
         *
         * @param initialDelayMs 初始延迟时间（毫秒），必须大于 0
         * @return 构建器实例
         */
        public Builder exponentialDelay(long initialDelayMs) {
            config.setDelayStrategy(new ExponentialDelayStrategy(initialDelayMs));
            return this;
        }

        /**
         * 使用指数延迟策略
         *
         * @param initialDelayMs 初始延迟时间（毫秒），必须大于 0
         * @param maxDelayMs     最大延迟时间（毫秒），必须大于 0
         * @return 构建器实例
         */
        public Builder exponentialDelay(long initialDelayMs, long maxDelayMs) {
            config.setDelayStrategy(new ExponentialDelayStrategy(initialDelayMs, maxDelayMs));
            return this;
        }

        /**
         * 使用随机延迟策略
         *
         * @param minDelayMs 最小延迟时间（毫秒），必须大于 0
         * @param maxDelayMs 最大延迟时间（毫秒），必须大于 0 且不小于 minDelayMs
         * @return 构建器实例
         */
        public Builder randomDelay(long minDelayMs, long maxDelayMs) {
            config.setDelayStrategy(new RandomDelayStrategy(minDelayMs, maxDelayMs));
            return this;
        }

        /**
         * 设置可重试的异常类型
         *
         * @param retryableExceptions 可重试的异常类型数组
         * @return 构建器实例
         */
        @SafeVarargs
        public final Builder retryableExceptions(Class<? extends Exception>... retryableExceptions) {
            config.setRetryableExceptions(retryableExceptions);
            return this;
        }

        /**
         * 构建 RetryConfig 实例
         *
         * @return 配置完成的 RetryConfig 实例
         */
        public RetryConfig build() {
            return config;
        }
    }
}
