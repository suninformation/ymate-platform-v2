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
package net.ymate.platform.commons;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 控制台命令执行器
 *
 * @author 刘镇 (suninformation@163.com) on 16/7/22 下午1:41
 */
public class ConsoleCmdExecutor {

    private static final Log LOG = LogFactory.getLog(ConsoleCmdExecutor.class);

    public static String exec(String... command) throws Exception {
        return exec(Arrays.asList(command));
    }

    public static String exec(List<String> command) throws Exception {
        return exec(command, reader -> reader.lines().map(line -> line + "\r\n").collect(Collectors.joining()));
    }

    public static <T> T exec(String[] command, ICmdOutputHandler<T> handler) throws Exception {
        return exec(Arrays.asList(command), handler);
    }

    public static <T> T exec(List<String> command, ICmdOutputHandler<T> handler) throws Exception {
        if (LOG.isInfoEnabled()) {
            LOG.info("Execute the command: " + StringUtils.join(command, ' '));
        }
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()), 1024)) {
            // 读取命令输出流
            T result = handler.handle(bufferedReader);
            // 线程阻塞，等待外部转换进程运行成功运行结束
            process.waitFor();
            //
            return result;
        } finally {
            process.destroy();
        }
    }
}
