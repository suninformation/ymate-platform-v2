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
package net.ymate.platform.log.jcl;

import net.ymate.platform.log.AbstractLogAdapter;
import net.ymate.platform.log.LogLevel;
import org.apache.commons.logging.Log;

/**
 * @author 刘镇 (suninformation@163.com) on 15/9/28 21:16
 */
public class JCLogger extends AbstractLogAdapter implements Log {

    JCLogger(String name) {
        super(name);
    }

    @Override
    public void debug(Object message) {
        if (isDebugEnabled()) {
            buildEx(message == null ? null : message.toString(), null, LogLevel.DEBUG);
        }
    }

    @Override
    public void debug(Object message, Throwable t) {
        if (isDebugEnabled()) {
            buildEx(message == null ? null : message.toString(), t, LogLevel.DEBUG);
        }
    }

    @Override
    public void error(Object message) {
        if (isErrorEnabled()) {
            buildEx(message == null ? null : message.toString(), null, LogLevel.ERROR);
        }
    }

    @Override
    public void error(Object message, Throwable t) {
        if (isErrorEnabled()) {
            buildEx(message == null ? null : message.toString(), t, LogLevel.ERROR);
        }
    }

    @Override
    public void fatal(Object message) {
        if (isFatalEnabled()) {
            buildEx(message == null ? null : message.toString(), null, LogLevel.FATAL);
        }
    }

    @Override
    public void fatal(Object message, Throwable t) {
        if (isFatalEnabled()) {
            buildEx(message == null ? null : message.toString(), t, LogLevel.FATAL);
        }
    }

    @Override
    public void info(Object message) {
        if (isInfoEnabled()) {
            buildEx(message == null ? null : message.toString(), null, LogLevel.INFO);
        }
    }

    @Override
    public void info(Object message, Throwable t) {
        if (isInfoEnabled()) {
            buildEx(message == null ? null : message.toString(), t, LogLevel.INFO);
        }
    }

    @Override
    public void trace(Object message) {
        if (isTraceEnabled()) {
            buildEx(message == null ? null : message.toString(), null, LogLevel.TRACE);
        }
    }

    @Override
    public void trace(Object message, Throwable t) {
        if (isTraceEnabled()) {
            buildEx(message == null ? null : message.toString(), t, LogLevel.TRACE);
        }
    }

    @Override
    public void warn(Object message) {
        if (isWarnEnabled()) {
            buildEx(message == null ? null : message.toString(), null, LogLevel.WARN);
        }
    }

    @Override
    public void warn(Object message, Throwable t) {
        if (isWarnEnabled()) {
            buildEx(message == null ? null : message.toString(), t, LogLevel.WARN);
        }
    }
}
