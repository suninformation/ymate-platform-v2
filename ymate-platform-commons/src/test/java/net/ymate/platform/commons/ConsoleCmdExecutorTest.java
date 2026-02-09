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
package net.ymate.platform.commons;

import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

/**
 * ConsoleCmdExecutor测试类
 *
 * @author 刘镇 (suninformation@163.com) on 2026-02-07 13:03:45
 * @since 2.1.4
 */
public class ConsoleCmdExecutorTest {

    private String getEchoCommand(String message) {
        // 适配Windows和Linux环境
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            return "cmd.exe /c echo " + message;
        } else {
            return "echo " + message;
        }
    }

    private List<String> getEchoCommandList(String message) {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            return Arrays.asList("cmd.exe", "/c", "echo", message);
        } else {
            return Arrays.asList("echo", message);
        }
    }

    @Test
    public void testExecWithArray() throws Exception {
        // 测试执行简单命令
        String[] command = getEchoCommand("Hello World").split(" ");
        String result = ConsoleCmdExecutor.exec(command);
        Assert.assertTrue(result.contains("Hello World"));
    }

    @Test
    public void testExecWithList() throws Exception {
        // 测试执行简单命令
        List<String> command = getEchoCommandList("Hello List");
        String result = ConsoleCmdExecutor.exec(command);
        Assert.assertTrue(result.contains("Hello List"));
    }

    @Test
    public void testExecWithHandler() throws Exception {
        // 测试使用自定义处理器
        List<String> command = getEchoCommandList("Hello Handler");
        String result = ConsoleCmdExecutor.exec(command, reader -> {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
            return builder.toString();
        });
        Assert.assertTrue(result.contains("Hello Handler"));
    }

    @Test
    public void testExecWithTimeout() throws Exception {
        // 测试带超时设置的命令执行
        List<String> command = getEchoCommandList("Hello Timeout");
        String result = ConsoleCmdExecutor.exec(command, reader -> reader.lines().collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString(), 5, TimeUnit.SECONDS);
        Assert.assertTrue(result.contains("Hello Timeout"));
    }

    @Test(expected = ConsoleCmdExecutor.ProcessExecutionException.class)
    public void testExecWithNonZeroExitCode() throws Exception {
        // 测试执行失败的命令（返回非零退出码）
        List<String> command;
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            command = Arrays.asList("cmd.exe", "/c", "exit", "1");
        } else {
            command = Arrays.asList("sh", "-c", "exit 1");
        }
        ConsoleCmdExecutor.exec(command);
    }

    @Test
    public void testConstructor() {
        // 测试默认构造
        ConsoleCmdExecutor executor1 = new ConsoleCmdExecutor();
        Assert.assertNotNull(executor1);

        // 测试带前缀的构造
        ConsoleCmdExecutor executor2 = new ConsoleCmdExecutor("TestPrefix");
        Assert.assertNotNull(executor2);

        // 测试带自定义执行器的构造
        ConsoleCmdExecutor executor3 = new ConsoleCmdExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        Assert.assertNotNull(executor3);
    }

    @Test
    public void testClose() throws Exception {
        ConsoleCmdExecutor executor = new ConsoleCmdExecutor();
        try {
            // 在关闭前执行命令
            List<String> command = getEchoCommandList("Hello Before Close");
            String result = executor.execAsync(command).get(5, TimeUnit.SECONDS);
            Assert.assertTrue(result.contains("Hello Before Close"));
        } finally {
            // 关闭执行器
            executor.close();
        }
    }

    @Test
    public void testExecAsyncWithArray() throws Exception {
        ConsoleCmdExecutor executor = new ConsoleCmdExecutor();
        try {
            String[] command = getEchoCommand("Hello Async Array").split(" ");
            ConsoleCmdExecutor.AsyncCmdTask<String> task = executor.execAsync(command);
            CompletableFuture<String> future = task.getFuture();
            String result = future.get(5, TimeUnit.SECONDS);
            Assert.assertTrue(result.contains("Hello Async Array"));
        } finally {
            executor.close();
        }
    }

    @Test
    public void testExecAsyncWithList() throws Exception {
        ConsoleCmdExecutor executor = new ConsoleCmdExecutor();
        try {
            List<String> command = getEchoCommandList("Hello Async List");
            ConsoleCmdExecutor.AsyncCmdTask<String> task = executor.execAsync(command);
            CompletableFuture<String> future = task.getFuture();
            String result = future.get(5, TimeUnit.SECONDS);
            Assert.assertTrue(result.contains("Hello Async List"));
        } finally {
            executor.close();
        }
    }

    @Test
    public void testExecAsyncWithHandler() throws Exception {
        ConsoleCmdExecutor executor = new ConsoleCmdExecutor();
        try {
            List<String> command = getEchoCommandList("Hello Async Handler");
            ConsoleCmdExecutor.AsyncCmdTask<String> task = executor.execAsync(command, reader -> {
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line).append("\n");
                }
                return builder.toString();
            });
            CompletableFuture<String> future = task.getFuture();
            String result = future.get(5, TimeUnit.SECONDS);
            Assert.assertTrue(result.contains("Hello Async Handler"));
        } finally {
            executor.close();
        }
    }

    @Test
    public void testCancelAsyncTask() throws Exception {
        ConsoleCmdExecutor executor = new ConsoleCmdExecutor();
        try {
            // 执行一个会睡眠的命令（用于测试取消）
            List<String> command;
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                command = Arrays.asList("cmd.exe", "/c", "timeout", "5");
            } else {
                command = Arrays.asList("sleep", "5");
            }
            ConsoleCmdExecutor.AsyncCmdTask<String> task = executor.execAsync(command);
            // 立即取消任务
            boolean cancelled = task.cancel();
            Assert.assertTrue(cancelled);
            // 尝试获取结果应该抛出CancellationException异常
            try {
                task.get(1, TimeUnit.SECONDS);
                Assert.fail("Should throw CancellationException");
            } catch (CancellationException e) {
                // 预期会抛出CancellationException异常
            } catch (ExecutionException e) {
                // 也可能抛出ExecutionException异常
            } catch (TimeoutException e) {
                // 也可能抛出超时异常
            }
        } finally {
            executor.close();
        }
    }

    @Test
    public void testWriteConsoleLogHandler() throws Exception {
        // 测试内置的WriteConsoleLog处理器
        ICmdOutputHandler<Void> handler = new ICmdOutputHandler.WriteConsoleLog(true);
        // 测试处理器的handle方法
        String testOutput = "Test Console Log\nLine 2\n";
        Void result = handler.handle(new BufferedReader(new StringReader(testOutput)));
        Assert.assertNull(result);

        // 测试静默模式
        ICmdOutputHandler<Void> silentHandler = new ICmdOutputHandler.WriteConsoleLog(false);
        result = silentHandler.handle(new BufferedReader(new StringReader(testOutput)));
        Assert.assertNull(result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullCommand() throws Exception {
        // 测试空命令参数
        ConsoleCmdExecutor.exec((String[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyCommand() throws Exception {
        // 测试空命令列表
        ConsoleCmdExecutor.exec(Arrays.asList());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullHandler() throws Exception {
        // 测试空处理器
        ConsoleCmdExecutor.exec(Arrays.asList("echo", "test"), null);
    }
}
