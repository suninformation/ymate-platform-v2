/*
 * Copyright 2007-2018 the original author or authors.
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
package net.ymate.platform.log.support;

import net.ymate.platform.core.util.ClassUtils;
import net.ymate.platform.core.util.DateTimeUtils;
import net.ymate.platform.core.util.RuntimeUtils;
import net.ymate.platform.log.ILogger;
import net.ymate.platform.log.Logs;
import net.ymate.platform.log.annotation.Loggable;
import net.ymate.platform.log.impl.DefaultLogooAdapter;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.message.ParameterizedMessageFactory;

import java.util.*;

/**
 * 怀旧版业务日志记录工具 ( ~ 十年前曾在某系统中使用Logoo作为日志框架名称)
 *
 * @author 刘镇 (suninformation@163.com) on 2018/1/7 下午5:38
 * @version 1.0
 */
public final class Logoo {

    private static final Log _LOG = LogFactory.getLog(Logoo.class);

    private static final ILogooAdapter DEFAULT_ADAPTER = new DefaultLogooAdapter();

    private static final ThreadLocal<Logoo> CURRENT = new ThreadLocal<Logoo>();

    static void createIfNeed(Loggable classAnno, Loggable methodAnno) {
        if (CURRENT.get() == null) {
            List<String> _logNames = new ArrayList<String>(Arrays.asList(classAnno.value()));
            if (ArrayUtils.isNotEmpty(methodAnno.value())) {
                for (String _name : methodAnno.value()) {
                    if (!_logNames.contains(_name)) {
                        _logNames.add(_name);
                    }
                }
            }
            //
            List<String> _flags = new ArrayList<String>();
            if (StringUtils.isNotBlank(classAnno.flag())) {
                _flags.add(classAnno.flag());
            }
            if (StringUtils.isNotBlank(methodAnno.flag())) {
                _flags.add(methodAnno.flag());
            }
            //
            ILogooAdapter _adapter = null;
            if (!DefaultLogooAdapter.class.equals(methodAnno.adapterClass())) {
                _adapter = ClassUtils.impl(methodAnno.adapterClass(), ILogooAdapter.class);
            }
            if (_adapter == null && !DefaultLogooAdapter.class.equals(classAnno.adapterClass())) {
                _adapter = ClassUtils.impl(classAnno.adapterClass(), ILogooAdapter.class);
            }
            //
            Logoo _logoo = new Logoo(_logNames, _flags.toArray(new String[0]), methodAnno.action(), methodAnno.level(), classAnno.merge() || methodAnno.merge(), _adapter);
            CURRENT.set(_logoo);
        }
    }

    static void clean() {
        CURRENT.remove();
    }

    static void release() {
        try {
            Logoo _logoo = CURRENT.get();
            if (_logoo != null && _logoo.__finished) {
                _logoo.doWriteLogs();
            }
        } finally {
            CURRENT.remove();
        }
    }

    // ----------

    private final Map<String, Object> attributes;

    private final List<String> loggerNames;

    private final String flag;

    private final String action;

    private final ILogger.LogLevel level;

    private final boolean merge;

    private final ILogooAdapter logooAdapter;

    private final StringBuilder contentSB;

    private final long startTime = System.currentTimeMillis();

    private boolean __finished;

    /**
     * @param loggerNames 输出到日志记录器名称集合
     * @param flags       自定义标识
     * @param action      自定义动作标识
     * @param level       日志输出级别
     * @param merge       是否日志合并输出
     * @param adapter     自定义日志适配器
     */
    private Logoo(List<String> loggerNames, String[] flags, String action, ILogger.LogLevel level, boolean merge, ILogooAdapter adapter) {
        this.attributes = new HashMap<String, Object>();
        this.loggerNames = loggerNames;
        this.flag = adapter != null ? adapter.buildFlag(flags) : DEFAULT_ADAPTER.buildFlag(flags);
        this.action = action;
        this.level = level;
        this.merge = merge;
        this.logooAdapter = adapter;
        this.contentSB = new StringBuilder();
        if (merge) {
            this.contentSB.append(doBuildLogLinePrefix());
            if (StringUtils.isNotBlank(flag)) {
                this.contentSB.append(" ---> ").append(flag);
            }
            contentSB.append("\n");
        }
    }

    private String doBuildLogLinePrefix() {
        return "---> " + DateTimeUtils.formatTime(System.currentTimeMillis(), DateTimeUtils.YYYY_MM_DD_HH_MM_SS_SSS) + " [" + Thread.currentThread().getId() + "] ";
    }

    private void doWriteLogs() {
        if (logooAdapter != null) {
            if (merge) {
                contentSB.append(doBuildLogLinePrefix());
                if (StringUtils.isNotBlank(flag)) {
                    contentSB.append(" <--- ").append(flag);
                }
                contentSB.append("\n");
                //
                String _content = contentSB.toString();
                if (loggerNames == null || loggerNames.isEmpty()) {
                    Logs.get().getLogger().log(_content, null, level);
                } else {
                    for (String _name : loggerNames) {
                        try {
                            Logs.get().getLogger(_name).log(_content, null, level);
                        } catch (Exception e) {
                            _LOG.warn("", RuntimeUtils.unwrapThrow(e));
                        }
                    }
                }
            }
            if (__finished) {
                logooAdapter.onLogWritten(flag, action, contentSB.toString(), attributes);
            }
        }
    }

    private void doWriteLog(String msg, Throwable t) {
        msg = StringUtils.defaultIfBlank(msg, null);
        if (msg == null && t != null) {
            msg = StringUtils.EMPTY;
        }
        if (msg != null) {
            if (!merge) {
                if (StringUtils.isNotBlank(flag)) {
                    msg = "[" + flag + "] " + msg;
                }
                if (loggerNames == null || loggerNames.isEmpty()) {
                    Logs.get().getLogger().log(msg, t, level);
                } else {
                    for (String _name : loggerNames) {
                        try {
                            ILogger _logger = Logs.get().getLogger(_name);
                            _logger.log(msg, t, level);
                        } catch (Exception e) {
                            _LOG.warn("", RuntimeUtils.unwrapThrow(e));
                        }
                    }
                }
            } else {
                String _prefix = doBuildLogLinePrefix();
                if (StringUtils.isNotBlank(msg)) {
                    contentSB.append(_prefix).append(msg).append("\n");
                }
                if (t != null) {
                    contentSB.append(_prefix).append("Caused by: ")
                            .append(t.getClass().getName())
                            .append(": ")
                            .append(StringUtils.trimToEmpty(t.getMessage()))
                            .append("\n");
                    StackTraceElement[] _stacks = t.getStackTrace();
                    for (StackTraceElement _stack : _stacks) {
                        contentSB.append(_prefix).append("\tat ")
                                .append(_stack)
                                .append("\n");
                    }
                }
            }
        }
    }

    private void doPutAttribute(String attrKey, Object attrValue) {
        if (StringUtils.isNotBlank(attrKey)) {
            this.attributes.put(attrKey, attrValue);
        }
    }

    private Map<String, Object> doGetAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    // ----------

    public static void addAttribute(String attrKey, Object attrValue) {
        Logoo _log = CURRENT.get();
        if (_log != null) {
            _log.doPutAttribute(attrKey, attrValue);
        }
    }

    public static Map<String, Object> getAttributes() {
        Logoo _log = CURRENT.get();
        if (_log != null) {
            return _log.doGetAttributes();
        }
        return Collections.emptyMap();
    }

    public static String fmtMessage(String format, Object... arguments) {
        if (StringUtils.isNotBlank(format)) {
            return ParameterizedMessageFactory.INSTANCE.newMessage(format, arguments).getFormattedMessage();
        }
        return format;
    }

    public static void log(String msg) {
        log(msg, (Throwable) null);
    }

    public static void log(String format, Object arg) {
        log(fmtMessage(format, arg));
    }

    public static void log(String format, Object arg1, Object arg2) {
        log(fmtMessage(format, arg1, arg2));
    }

    public static void log(String format, Object... arguments) {
        log(fmtMessage(format, arguments));
    }

    public static void log(String msg, Throwable t) {
        Logoo _log = CURRENT.get();
        if (_log != null) {
            _log.doWriteLog(msg, t);
        }
    }

    /**
     * @return 返回当前时间与日志记录开始时间的差值, 返回值可能为0
     */
    public static long getTotalTime() {
        Logoo _log = CURRENT.get();
        if (_log != null) {
            return System.currentTimeMillis() - _log.startTime;
        }
        return 0L;
    }

    public static void finished() {
        Logoo _log = CURRENT.get();
        if (_log != null) {
            _log.__finished = true;
        }
    }
}
