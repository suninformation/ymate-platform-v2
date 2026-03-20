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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 重试工具类，提供便捷的重试操作执行方法
 * <p>
 * 该类封装了重试逻辑的核心实现，支持：
 * <ul>
 *     <li>自定义重试次数和延迟策略</li>
 *     <li>指定可重试的异常类型</li>
 *     <li>总超时时间控制</li>
 *     <li>详细的日志记录</li>
 * </ul>
 * <p>
 * 使用示例：
 * <pre>
 * // 使用默认配置（3次重试，1秒初始延迟，指数退避）
 * String result = RetryUtils.executeWithRetry(() -&gt; doSomething());
 *
 * // 自定义重试次数和延迟
 * String result = RetryUtils.executeWithRetry(() -&gt; doSomething(), 5, 2000);
 *
 * // 指定可重试的异常类型
 * String result = RetryUtils.executeWithRetry(
 *     () -&gt; doSomething(),
 *     3, 1000,
 *     IOException.class, TimeoutException.class
 * );
 *
 * // 使用完整配置对象
 * RetryConfig config = RetryConfig.custom()
 *     .maxRetries(5)
 *     .exponentialDelay(1000, 60000)
 *     .totalTimeoutMs(300000L)
 *     .retryableExceptions(IOException.class)
 *     .build();
 * String result = RetryUtils.executeWithRetry(() -&gt; doSomething(), config);
 * </pre>
 *
 * @author 刘镇 (suninformation@163.com) on 2026-03-20
 * @since 2.1.4
 */
public class RetryUtils {

    private static final Log LOG = LogFactory.getLog(RetryUtils.class);

    /**
     * 执行带重试的操作（使用配置对象）
     * <p>
     * 该方法是重试机制的核心实现，根据配置执行重试逻辑：
     * <ol>
     *     <li>检查配置参数有效性</li>
     *     <li>循环执行操作，直到成功或达到最大重试次数</li>
     *     <li>每次失败后根据延迟策略等待</li>
     *     <li>检查总超时时间</li>
     *     <li>检查异常类型是否可重试</li>
     * </ol>
     *
     * @param action 要执行的操作，不能为 null
     * @param config 重试配置，不能为 null
     * @param <T>    返回值类型
     * @return 操作执行结果
     * @throws IllegalArgumentException 如果 config 为 null
     * @throws RuntimeException         如果重试被中断或超时
     * @throws Exception                如果所有重试都失败，抛出最后一次异常
     */
    public static <T> T executeWithRetry(IRetryable<T> action, RetryConfig config) throws Exception {
        if (config == null) {
            throw new IllegalArgumentException("RetryConfig cannot be null");
        }

        int maxRetries = config.getMaxRetries();
        IRetryDelayStrategy delayStrategy = config.getDelayStrategy();
        Long totalTimeoutMs = config.getTotalTimeoutMs();
        Class<? extends Exception>[] retryableExceptions = config.getRetryableExceptions();

        if (maxRetries <= 0) {
            throw new IllegalArgumentException("maxRetries must be greater than 0");
        }
        if (delayStrategy == null) {
            throw new IllegalArgumentException("RetryDelayStrategy cannot be null");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Starting retry operation: maxRetries=%d, delayStrategy=%s, totalTimeoutMs=%s",
                    maxRetries, delayStrategy.getClass().getSimpleName(), totalTimeoutMs));
        }

        long startTime = System.currentTimeMillis();
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            if (totalTimeoutMs != null) {
                long elapsed = System.currentTimeMillis() - startTime;
                if (elapsed >= totalTimeoutMs) {
                    String message = String.format("Retry timeout after %dms, completed %d/%d attempts",
                            elapsed, attempt - 1, maxRetries);
                    if (LOG.isErrorEnabled()) {
                        LOG.error(message, lastException);
                    }
                    throw new RuntimeException(message, lastException);
                }
            }

            try {
                T result = action.execute();
                if (LOG.isInfoEnabled()) {
                    long totalTime = System.currentTimeMillis() - startTime;
                    LOG.info(String.format("Retry succeeded on attempt %d/%d, total time: %dms", attempt, maxRetries, totalTime));
                }
                return result;
            } catch (Exception e) {
                if (!isRetryableException(e, retryableExceptions)) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error(String.format("Non-retryable exception encountered: %s", e.getMessage()), e);
                    }
                    throw e;
                }

                lastException = e;
                if (LOG.isWarnEnabled()) {
                    LOG.warn(String.format("Attempt %d/%d failed: %s", attempt, maxRetries, e.getMessage()));
                }

                if (attempt < maxRetries) {
                    long delay = delayStrategy.getDelay(attempt);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(String.format("Retrying in %dms...", delay));
                    }
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        if (LOG.isErrorEnabled()) {
                            LOG.error("Retry interrupted", ie);
                        }
                        throw new RuntimeException("Retry interrupted", ie);
                    }
                }
            }
        }

        if (LOG.isErrorEnabled()) {
            long totalTime = System.currentTimeMillis() - startTime;
            LOG.error(String.format("All %d attempts failed after %dms, last error: %s",
                    maxRetries, totalTime, lastException.getMessage()), lastException);
        }

        throw lastException;
    }

    /**
     * 执行带重试的操作（支持异常类型过滤）
     * <p>
     * 使用指数退避延迟策略，可指定需要重试的异常类型
     *
     * @param action              要执行的操作，不能为 null
     * @param maxRetries          最大重试次数，必须大于 0
     * @param initialDelayMs      初始重试间隔（毫秒），必须大于 0
     * @param retryableExceptions 需要重试的异常类型，如果为空则对所有异常进行重试
     * @param <T>                 返回值类型
     * @return 操作执行结果
     * @throws Exception 如果所有重试都失败
     */
    @SafeVarargs
    public static <T> T executeWithRetry(IRetryable<T> action, int maxRetries, long initialDelayMs,
                                         Class<? extends Exception>... retryableExceptions) throws Exception {
        RetryConfig config = RetryConfig.custom()
                .maxRetries(maxRetries)
                .exponentialDelay(initialDelayMs)
                .retryableExceptions(retryableExceptions)
                .build();
        return executeWithRetry(action, config);
    }

    /**
     * 执行带重试的操作
     * <p>
     * 使用指数退避延迟策略，对所有异常类型进行重试
     *
     * @param action         要执行的操作，不能为 null
     * @param maxRetries     最大重试次数，必须大于 0
     * @param initialDelayMs 初始重试间隔（毫秒），必须大于 0
     * @param <T>            返回值类型
     * @return 操作执行结果
     * @throws Exception 如果所有重试都失败
     */
    @SuppressWarnings("unchecked")
    public static <T> T executeWithRetry(IRetryable<T> action, int maxRetries, long initialDelayMs) throws Exception {
        return (T) executeWithRetry(action, maxRetries, initialDelayMs, new Class[0]);
    }

    /**
     * 执行带重试的操作（使用默认参数）
     * <p>
     * 默认参数：最大重试次数 3 次、初始延迟 1000 毫秒、指数退避延迟策略、对所有异常重试
     *
     * @param action 要执行的操作，不能为 null
     * @param <T>    返回值类型
     * @return 操作执行结果
     * @throws Exception 如果所有重试都失败
     */
    public static <T> T executeWithRetry(IRetryable<T> action) throws Exception {
        return executeWithRetry(action, 3, 1000);
    }

    /**
     * 检查异常是否属于可重试的类型
     *
     * @param exception           待检查的异常
     * @param retryableExceptions 可重试的异常类型列表
     * @return 如果属于可重试类型返回 true，否则返回 false
     */
    private static boolean isRetryableException(Exception exception, Class<? extends Exception>[] retryableExceptions) {
        if (retryableExceptions == null || retryableExceptions.length == 0) {
            return true;
        }

        for (Class<? extends Exception> retryableType : retryableExceptions) {
            if (retryableType.isInstance(exception)) {
                return true;
            }
        }
        return false;
    }

}
