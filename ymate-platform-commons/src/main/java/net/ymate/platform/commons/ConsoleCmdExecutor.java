/*
 * Copyright 2007-2025 the original author or authors.
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
package net.ymate.platform.commons;

import net.ymate.platform.commons.impl.DefaultThreadFactory;
import net.ymate.platform.commons.util.ThreadUtils;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 控制台命令执行器
 *
 * @author 刘镇 (suninformation@163.com) on 16/7/22 下午1:41
 */
public class ConsoleCmdExecutor implements AutoCloseable {

    private static final Log LOG = LogFactory.getLog(ConsoleCmdExecutor.class);

    /**
     * 执行命令并返回字符串结果
     *
     * @param command 命令数组
     * @return 命令执行结果字符串
     * @throws Exception 执行异常
     */
    public static String exec(String... command) throws Exception {
        if (command == null) {
            throw new IllegalArgumentException("command cannot be null");
        }
        return exec(Arrays.asList(command));
    }

    /**
     * 执行命令并返回字符串结果
     *
     * @param command 命令列表
     * @return 命令执行结果字符串
     * @throws Exception 执行异常
     */
    public static String exec(List<String> command) throws Exception {
        return exec(command, reader -> reader.lines().map(line -> line + "\r\n").collect(Collectors.joining()));
    }

    /**
     * 执行命令并使用处理器处理输出
     *
     * @param command 命令数组
     * @param handler 输出处理器
     * @return 处理结果
     * @throws Exception 执行异常
     * @since 2.1.4
     */
    public static <T> T exec(String[] command, ICmdOutputHandler<T> handler) throws Exception {
        return exec(Arrays.asList(command), handler);
    }

    /**
     * 执行命令并使用处理器处理输出，带有超时设置
     *
     * @param command 命令数组
     * @param handler 输出处理器
     * @param timeout 超时时间
     * @param unit    时间单位
     * @return 处理结果
     * @throws Exception 执行异常
     * @since 2.1.4
     */
    public static <T> T exec(String[] command, ICmdOutputHandler<T> handler, long timeout, TimeUnit unit) throws Exception {
        return exec(Arrays.asList(command), handler, timeout, unit);
    }

    /**
     * 执行命令并使用处理器处理输出
     *
     * @param command 命令列表
     * @param handler 输出处理器
     * @return 处理结果
     * @throws Exception 执行异常
     */
    public static <T> T exec(List<String> command, ICmdOutputHandler<T> handler) throws Exception {
        return exec(command, handler, 0, null);
    }

    /**
     * 执行命令并使用处理器处理输出，带有超时设置
     *
     * @param command 命令列表
     * @param handler 输出处理器
     * @param timeout 超时时间，0表示不超时
     * @param unit    时间单位
     * @return 处理结果
     * @throws Exception 执行异常
     * @since 2.1.4
     */
    public static <T> T exec(List<String> command, ICmdOutputHandler<T> handler, long timeout, TimeUnit unit) throws Exception {
        if (command == null || command.isEmpty()) {
            throw new NullArgumentException("command");
        }
        if (handler == null) {
            throw new NullArgumentException("handler");
        }
        String commandStr = StringUtils.join(command, StringUtils.SPACE);
        if (LOG.isInfoEnabled()) {
            LOG.info("Execute the command: " + commandStr);
        }
        Process process = null;
        try {
            // 创建进程并合并错误流到标准输出流
            process = new ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .start();
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()), 1024)) {
                // 读取命令输出流
                T result = handler.handle(bufferedReader);
                // 等待进程完成，支持超时设置
                if (timeout > 0 && unit != null) {
                    boolean completed = process.waitFor(timeout, unit);
                    if (!completed) {
                        throw new TimeoutException(String.format("Command execution timed out after %d %s: %s", timeout, unit.name().toLowerCase(), commandStr));
                    }
                } else {
                    process.waitFor();
                }
                // 检查退出码
                int exitCode = process.exitValue();
                if (exitCode != 0) {
                    throw new ProcessExecutionException(String.format("Command exited with non-zero code: %d", exitCode), exitCode, commandStr);
                }
                return result;
            }
        } catch (Exception e) {
            if (e instanceof ProcessExecutionException || e instanceof TimeoutException) {
                throw e;
            }
            throw new Exception(String.format("Failed to execute command: %s", commandStr), e);
        } finally {
            if (process != null) {
                try {
                    process.destroy();
                    // 等待一小段时间让进程有机会优雅退出
                    boolean destroyed = process.waitFor(100, TimeUnit.MILLISECONDS);
                    if (!destroyed) {
                        // 如果没有正常退出，强制销毁
                        process.destroyForcibly();
                    }
                } catch (Exception ex) {
                    // 销毁进程失败时记录日志但不抛出异常
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("Failed to destroy process", ex);
                    }
                }
            }
        }
    }

    // ------

    /**
     * @since 2.1.4
     */
    private final ExecutorService executorService;

    /**
     * 默认构造方法
     *
     * @since 2.1.4
     */
    public ConsoleCmdExecutor() {
        this("ConsoleCmdExecutor");
    }

    /**
     * 构造方法
     *
     * @param prefix 线程名称前缀
     * @since 2.1.4
     */
    public ConsoleCmdExecutor(String prefix) {
        executorService = ThreadUtils.newCachedThreadPool(DefaultThreadFactory.create(StringUtils.defaultIfBlank(prefix, "ConsoleCmdExecutor")).daemon(true));
    }

    /**
     * 构造方法
     *
     * @param executorService 自定义执行器
     * @since 2.1.4
     */
    public ConsoleCmdExecutor(ExecutorService executorService) {
        if (executorService == null) {
            throw new NullArgumentException("executorService");
        }
        this.executorService = executorService;
    }

    /**
     * @since 2.1.4
     */
    @Override
    public void close() throws Exception {
        try {
            executorService.shutdown();
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executorService.shutdownNow();
        }
    }

    /**
     * 异步执行命令
     *
     * @param command 命令数组
     * @return 任务对象，可用于取消任务和获取结果
     * @since 2.1.4
     */
    public AsyncCmdTask<String> execAsync(String... command) {
        return execAsync(Arrays.asList(command));
    }

    /**
     * 异步执行命令
     *
     * @param command 命令列表
     * @return 任务对象，可用于取消任务和获取结果
     * @since 2.1.4
     */
    public AsyncCmdTask<String> execAsync(List<String> command) {
        return execAsync(command, reader -> reader.lines().map(line -> line + "\r\n").collect(Collectors.joining()));
    }

    /**
     * 异步执行命令并使用处理器处理输出
     *
     * @param command 命令数组
     * @param handler 输出处理器
     * @return 任务对象，可用于取消任务和获取结果
     * @since 2.1.4
     */
    public <T> AsyncCmdTask<T> execAsync(String[] command, ICmdOutputHandler<T> handler) {
        return execAsync(Arrays.asList(command), handler);
    }

    /**
     * 异步执行命令并使用处理器处理输出
     *
     * @param command 命令列表
     * @param handler 输出处理器
     * @return 任务对象，可用于取消任务和获取结果
     * @since 2.1.4
     */
    public <T> AsyncCmdTask<T> execAsync(List<String> command, ICmdOutputHandler<T> handler) {
        if (command == null || command.isEmpty()) {
            throw new IllegalArgumentException("command must not be null or empty.");
        }
        Objects.requireNonNull(handler, "handler must not be null.");
        String commandStr = StringUtils.join(command, StringUtils.SPACE);
        if (LOG.isInfoEnabled()) {
            LOG.info(String.format("Async executing command: %s", commandStr));
        }
        final Process[] processHolder = new Process[1];
        CompletableFuture<T> future = new CompletableFuture<T>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                boolean result = super.cancel(mayInterruptIfRunning);
                if (result && processHolder[0] != null && processHolder[0].isAlive()) {
                    processHolder[0].destroyForcibly();
                }
                return result;
            }
        };
        executorService.execute(() -> {
            Process process = null;
            try {
                process = processHolder[0] = new ProcessBuilder(command)
                        .redirectErrorStream(true)
                        .start();
                try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()), 1024)) {
                    T result = handler.handle(bufferedReader);
                    process.waitFor();
                    int exitCode = process.exitValue();
                    if (exitCode != 0) {
                        future.completeExceptionally(new ProcessExecutionException(String.format("Command exited with non-zero code: %d", exitCode), exitCode, commandStr));
                    } else {
                        future.complete(result);
                    }
                }
            } catch (Exception e) {
                if (!future.isCancelled()) {
                    future.completeExceptionally(e);
                }
            } finally {
                if (process != null) {
                    try {
                        process.destroy();
                        process.waitFor(100, TimeUnit.MILLISECONDS);
                        if (process.isAlive()) {
                            process.destroyForcibly();
                        }
                    } catch (Exception ex) {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn("Failed to destroy process", ex);
                        }
                    }
                }
            }
        });
        return new AsyncCmdTask<>(processHolder[0], future, commandStr);
    }

    // ------

    /**
     * 命令执行异常类
     *
     * @since 2.1.4
     */
    public static class ProcessExecutionException extends Exception {

        private final int exitCode;

        private final String command;

        public ProcessExecutionException(String message, int exitCode, String command) {
            super(message);
            this.exitCode = exitCode;
            this.command = command;
        }

        public ProcessExecutionException(String message, Throwable cause, int exitCode, String command) {
            super(message, cause);
            this.exitCode = exitCode;
            this.command = command;
        }

        public int getExitCode() {
            return exitCode;
        }

        public String getCommand() {
            return command;
        }
    }

    /**
     * 异步任务执行结果包装类
     *
     * @since 2.1.4
     */
    public static class AsyncCmdTask<T> {

        private final Process process;

        private final CompletableFuture<T> future;

        private final String commandStr;

        /**
         * 构造方法
         * <p>
         * 创建异步命令执行任务对象，封装进程、Future和命令字符串。
         * </p>
         *
         * @param process    命令执行的进程对象
         * @param future     异步执行的Future对象
         * @param commandStr 执行的命令字符串
         */
        protected AsyncCmdTask(Process process, CompletableFuture<T> future, String commandStr) {
            this.process = process;
            this.future = future;
            this.commandStr = commandStr;
        }

        /**
         * 获取命令执行的进程对象
         * <p>
         * 返回当前命令执行关联的进程对象，可用于进程管理操作。
         * </p>
         *
         * @return 命令执行的进程对象
         */
        public Process getProcess() {
            return process;
        }

        /**
         * 获取执行的命令字符串
         * <p>
         * 返回当前任务执行的命令字符串，用于日志记录和错误诊断。
         * </p>
         *
         * @return 命令字符串
         */
        public String getCommandStr() {
            return commandStr;
        }

        /**
         * 取消正在执行的命令
         * <p>
         * 如果命令正在执行中，将强制终止对应的进程。
         * 如果命令已经完成，此方法返回false。
         * 如果命令尚未开始执行，将尝试取消任务。
         * </p>
         *
         * @return 如果成功取消返回true，否则返回false
         */
        public boolean cancel() {
            // 首先尝试取消Future任务
            boolean futureCancelled = future.cancel(true);

            // 如果进程正在运行，强制终止进程
            if (process != null && process.isAlive()) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Cancelling command execution: " + commandStr);
                }
                try {
                    process.destroyForcibly();
                    return true;
                } catch (Exception e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error("Failed to cancel command execution", e);
                    }
                    return futureCancelled;
                }
            }

            // 如果进程已结束或为null，返回Future取消结果
            return futureCancelled;
        }

        /**
         * 获取异步执行结果的Future对象
         * <p>
         * 返回的Future对象可以用于更复杂的异步操作，
         * 例如添加回调、组合多个异步操作等。
         * </p>
         *
         * @return CompletableFuture包装的异步结果
         */
        public CompletableFuture<T> getFuture() {
            return future;
        }

        /**
         * 同步等待执行完成，带有超时设置
         * <p>
         * 此方法会阻塞当前线程，直到命令执行完成或超时。
         * 如果超时，将抛出 {@link java.util.concurrent.TimeoutException}。
         * </p>
         *
         * @param timeout 超时时间，必须为正数
         * @param unit    时间单位，不能为null
         * @return 命令执行结果
         * @throws InterruptedException 如果线程被中断
         * @throws ExecutionException   如果命令执行失败
         * @throws TimeoutException     如果等待超时
         */
        public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return future.get(timeout, unit);
        }

        /**
         * 同步等待执行完成
         * <p>
         * 此方法会阻塞当前线程，直到命令执行完成。
         * </p>
         *
         * @return 命令执行结果
         * @throws InterruptedException 如果线程被中断
         * @throws ExecutionException   如果命令执行失败
         */
        public T get() throws InterruptedException, ExecutionException {
            return future.get();
        }
    }
}
