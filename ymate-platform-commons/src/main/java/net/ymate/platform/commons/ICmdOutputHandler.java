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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;

/**
 * 命令行输出内容处理器接口
 *
 * @param <T> 结果对象类型
 * @author 刘镇 (suninformation@163.com) on 16/7/22 下午1:53
 */
public interface ICmdOutputHandler<T> {

    class WriteConsoleLog implements ICmdOutputHandler<Void> {

        private static Log LOG = LogFactory.getLog(WriteConsoleLog.class);

        private boolean output;

        public WriteConsoleLog() {
            this(true);
        }

        public WriteConsoleLog(boolean output) {
            this.output = output;
        }

        @Override
        public Void handle(BufferedReader reader) throws Exception {
            String line;
            while ((line = reader.readLine()) != null) {
                if (output && LOG.isInfoEnabled()) {
                    LOG.info(line);
                }
            }
            return null;
        }
    }

    /**
     * 执行处理过程
     *
     * @param reader Reader对象
     * @return 返回处理结果对象
     * @throws Exception 可能产生的任何异常
     */
    T handle(BufferedReader reader) throws Exception;
}
