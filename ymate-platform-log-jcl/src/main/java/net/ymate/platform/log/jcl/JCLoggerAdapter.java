/*
 * Copyright 2007-2016 the original author or authors.
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
package net.ymate.platform.log.jcl;

import net.ymate.platform.core.YMP;
import net.ymate.platform.log.Logs;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.SimpleLog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.spi.LoggerAdapter;

import java.io.IOException;

/**
 * @author 刘镇 (suninformation@163.com) on 15/9/28 21:53
 * @version 1.0
 */
public class JCLoggerAdapter implements LoggerAdapter<Log> {

    public Log getLogger(String name) {
        if (YMP.get().isInited() && Logs.get().isInited()) {
            return new JCLogger(LogManager.getRootLogger(), Logs.get().getModuleCfg().allowOutputConsole());
        }
        return new SimpleLog(name);
    }

    public void close() throws IOException {
    }
}
