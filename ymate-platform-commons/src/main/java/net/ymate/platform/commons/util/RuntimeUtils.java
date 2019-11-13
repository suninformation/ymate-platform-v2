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
package net.ymate.platform.commons.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 运行时工具类，获取运行时相关信息
 *
 * @author 刘镇 (suninformation@163.com) on 2010-8-2 上午10:10:16
 */
public class RuntimeUtils {

    private static final Log LOG = LogFactory.getLog(RuntimeUtils.class);

    public static final String ROOT = "root";

    public static final String USER_HOME = "user.home";

    public static final String USER_DIR = "user.dir";

    public static final String VAR_ROOT = "${" + ROOT + "}";

    public static final String VAR_USER_HOME = "${" + USER_HOME + "}";

    public static final String VAR_USER_DIR = "${" + USER_DIR + "}";

    /**
     * 系统环境变量映射
     */
    private static final Map<String, String> SYSTEM_ENV_MAP = new HashMap<>();

    static {
        initSystemEnvs();
    }

    /**
     * 初始化系统环境，获取当前系统环境变量
     */
    private synchronized static void initSystemEnvs() {
        Process process = null;
        try {
            if (SystemUtils.IS_OS_WINDOWS) {
                process = Runtime.getRuntime().exec("cmd /c set");
            } else if (SystemUtils.IS_OS_UNIX) {
                process = Runtime.getRuntime().exec("/bin/sh -c set");
            } else {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(String.format("Unknown os.name=%s", SystemUtils.OS_NAME));
                }
                SYSTEM_ENV_MAP.clear();
            }
            if (process != null) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        int i = line.indexOf('=');
                        if (i > -1) {
                            String key = line.substring(0, i);
                            String value = line.substring(i + 1);
                            SYSTEM_ENV_MAP.put(key, value);
                        }
                    }
                }
            }
        } catch (IOException ignored) {
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    /**
     * 获取系统运行时，可以进行缓存
     *
     * @return 环境变量对应表
     * @see System#getenv()
     */
    @Deprecated
    public static Map<String, String> getSystemEnvs() {
        if (SYSTEM_ENV_MAP.isEmpty()) {
            initSystemEnvs();
        }
        return Collections.unmodifiableMap(SYSTEM_ENV_MAP);
    }

    /**
     * 获取指定名称的环境值
     *
     * @param envName 环境名，如果为空，返回null
     * @return 当指定名称为空或者对应名称环境变量不存在时返回空
     * @see System#getenv(String)
     */
    @Deprecated
    public static String getSystemEnv(String envName) {
        if (StringUtils.isNotBlank(envName)) {
            if (SYSTEM_ENV_MAP.isEmpty()) {
                initSystemEnvs();
            }
            return SYSTEM_ENV_MAP.get(envName);
        }
        return null;
    }

    /**
     * @return 当前操作系统是否为类Unix系统
     */
    public static boolean isUnixOrLinux() {
        return SystemUtils.IS_OS_UNIX;
    }

    /**
     * @return 当前操作系统是否为Windows系统
     */
    public static boolean isWindows() {
        return SystemUtils.IS_OS_WINDOWS;
    }

    /**
     * @return 返回当前程序执行进程编号
     */
    public static String getProcessId() {
        return StringUtils.split(ManagementFactory.getRuntimeMXBean().getName(), "@")[0];
    }

    /**
     * @return 获取应用根路径（若WEB工程则基于.../WEB-INF/返回，若普通工程则返回类所在路径）
     */
    public static String getRootPath() {
        return getRootPath(true);
    }

    /**
     * @param safe 若WEB工程是否保留WEB-INF
     * @return 返回应用根路径
     */
    public static String getRootPath(boolean safe) {
        //
        String rootPath = null;
        //
        URL rootUrl = RuntimeUtils.class.getClassLoader().getResource("/");
        if (rootUrl == null) {
            rootUrl = RuntimeUtils.class.getClassLoader().getResource(StringUtils.EMPTY);
            if (rootUrl != null) {
                rootPath = rootUrl.getPath();
            }
        } else {
            rootPath = StringUtils.removeEnd(StringUtils.substringBefore(rootUrl.getPath(), safe ? "classes/" : "WEB-INF/"), "/");
        }
        //
        if (rootPath != null) {
            rootPath = StringUtils.replace(rootPath, "%20", StringUtils.SPACE);
            if (isWindows()) {
                rootPath = StringUtils.removeStart(rootPath, "/");
            }
        }
        return StringUtils.trimToEmpty(rootPath);
    }

    /**
     * @param origin 原始字符串
     * @return 替换${root}、${user.dir}和${user.home}环境变量
     * @since 2.1.0 调整当获取应用根路径为空则默认使用user.dir路径
     */
    public static String replaceEnvVariable(String origin) {
        if ((origin = StringUtils.trimToNull(origin)) != null) {
            String rootPath = getRootPath();
            if (StringUtils.isBlank(rootPath)) {
                rootPath = System.getProperty(USER_DIR);
            }
            if (StringUtils.contains(origin, VAR_ROOT)) {
                origin = ExpressionUtils.bind(origin).set(ROOT, rootPath).getResult();
            } else if (StringUtils.contains(origin, VAR_USER_DIR)) {
                origin = ExpressionUtils.bind(origin).set(USER_DIR, System.getProperty(USER_DIR, rootPath)).getResult();
            } else if (StringUtils.contains(origin, VAR_USER_HOME)) {
                origin = ExpressionUtils.bind(origin).set(USER_HOME, System.getProperty(USER_HOME, rootPath)).getResult();
            }
        }
        return origin;
    }

    /**
     * 根据格式化字符串，生成运行时异常
     *
     * @param format 格式
     * @param args   参数
     * @return 运行时异常
     */
    public static RuntimeException makeRuntimeThrow(String format, Object... args) {
        return new RuntimeException(String.format(format, args));
    }

    /**
     * 将抛出对象包裹成运行时异常，并增加描述
     *
     * @param e    抛出对象
     * @param fmt  格式
     * @param args 参数
     * @return 运行时异常
     */
    public static RuntimeException wrapRuntimeThrow(Throwable e, String fmt, Object... args) {
        return new RuntimeException(String.format(fmt, args), e);
    }

    /**
     * 用运行时异常包裹抛出对象，如果抛出对象本身就是运行时异常，则直接返回
     * <p>
     * 若 e 对象是 InvocationTargetException，则将其剥离，仅包裹其 TargetException 对象
     * </p>
     *
     * @param e 抛出对象
     * @return 运行时异常
     */
    public static RuntimeException wrapRuntimeThrow(Throwable e) {
        if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        if (e instanceof InvocationTargetException) {
            return wrapRuntimeThrow(((InvocationTargetException) e).getTargetException());
        }
        return new RuntimeException(e);
    }

    public static Throwable unwrapThrow(Throwable e) {
        if (e == null) {
            return null;
        }
        if (e instanceof InvocationTargetException) {
            InvocationTargetException itEx = (InvocationTargetException) e;
            if (itEx.getTargetException() != null) {
                return unwrapThrow(itEx.getTargetException());
            }
        }
        if (e.getCause() != null) {
            return unwrapThrow(e.getCause());
        }
        return e;
    }

    /**
     * 垃圾回收，返回回收的字节数
     *
     * @return 回收的字节数，如果为负数则表示当前内存使用情况很差，基本属于没有内存可用了
     */
    public static long gc() {
        Runtime rt = Runtime.getRuntime();
        long lastUsed = rt.totalMemory() - rt.freeMemory();
        rt.gc();
        return lastUsed - rt.totalMemory() + rt.freeMemory();
    }
}
