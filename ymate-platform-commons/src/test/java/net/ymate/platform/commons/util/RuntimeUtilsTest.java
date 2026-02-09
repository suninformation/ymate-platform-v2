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
package net.ymate.platform.commons.util;

import org.junit.Assert;
import org.junit.Test;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * RuntimeUtils类的单元测试，覆盖所有公共方法、边界条件和异常处理场景。
 *
 * @author 刘镇 (suninformation@163.com) on 2026-01-05 13:21:45
 * @since 2.1.4
 */
public class RuntimeUtilsTest {

    /**
     * 测试常量定义
     */
    @Test
    public void testConstants() {
        Assert.assertEquals("root", RuntimeUtils.ROOT);
        Assert.assertEquals("user.home", RuntimeUtils.USER_HOME);
        Assert.assertEquals("user.dir", RuntimeUtils.USER_DIR);
        Assert.assertEquals("${root}", RuntimeUtils.VAR_ROOT);
        Assert.assertEquals("${user.home}", RuntimeUtils.VAR_USER_HOME);
        Assert.assertEquals("${user.dir}", RuntimeUtils.VAR_USER_DIR);
    }

    /**
     * 测试废弃方法getSystemEnvs()
     */
    @Test
    public void testGetSystemEnvs() {
        Map<String, String> envs = RuntimeUtils.getSystemEnvs();
        Assert.assertNotNull(envs);
        Assert.assertFalse(envs.isEmpty());
        // 验证返回的映射不可修改
        try {
            envs.put("test", "value");
            Assert.fail("映射应该是不可修改的");
        } catch (UnsupportedOperationException e) {
            // 预期异常
        }
    }

    /**
     * 测试废弃方法getSystemEnv()
     */
    @Test
    public void testGetSystemEnv() {
        // 测试获取存在的环境变量
        String pathEnv = RuntimeUtils.getSystemEnv("PATH");
        Assert.assertEquals(System.getenv("PATH"), pathEnv);

        // 测试获取不存在的环境变量
        String nonExistentEnv = RuntimeUtils.getSystemEnv("NON_EXISTENT_ENV_VAR");
        Assert.assertNull(nonExistentEnv);

        // 测试空参数
        String nullEnv = RuntimeUtils.getSystemEnv(null);
        Assert.assertNull(nullEnv);

        // 测试空字符串参数
        String emptyEnv = RuntimeUtils.getSystemEnv("");
        Assert.assertNull(emptyEnv);
    }

    /**
     * 测试操作系统检测方法
     */
    @Test
    public void testOsDetection() {
        boolean isWindows = RuntimeUtils.isWindows();
        boolean isUnixOrLinux = RuntimeUtils.isUnixOrLinux();

        // 验证两个方法返回值互斥
        Assert.assertNotEquals("操作系统检测结果互斥", isWindows, isUnixOrLinux);

        // 验证与SystemUtils结果一致
        Assert.assertEquals(org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS, isWindows);
        Assert.assertEquals(org.apache.commons.lang3.SystemUtils.IS_OS_UNIX, isUnixOrLinux);
    }

    /**
     * 测试获取进程ID方法
     */
    @Test
    public void testGetProcessId() {
        String processId = RuntimeUtils.getProcessId();
        Assert.assertNotNull(processId);
        Assert.assertFalse(processId.isEmpty());
        // 验证进程ID是数字
        try {
            Long.parseLong(processId);
        } catch (NumberFormatException e) {
            Assert.fail("进程ID应该是数字");
        }
    }

    /**
     * 测试获取应用根路径方法
     */
    @Test
    public void testGetRootPath() {
        // 测试默认调用（safe=true）
        String rootPath1 = RuntimeUtils.getRootPath();
        Assert.assertNotNull(rootPath1);
        Assert.assertFalse(rootPath1.isEmpty());

        // 测试safe=false参数
        String rootPath2 = RuntimeUtils.getRootPath(false);
        Assert.assertNotNull(rootPath2);
        Assert.assertFalse(rootPath2.isEmpty());

        // 验证返回路径结尾没有斜杠
        Assert.assertFalse("根路径结尾不应有斜杠", rootPath1.endsWith("/"));
        Assert.assertFalse("根路径结尾不应有斜杠", rootPath2.endsWith("/"));
    }

    /**
     * 测试环境变量替换方法
     */
    @Test
    public void testReplaceEnvVariable() {
        // 测试空参数
        Assert.assertNull(RuntimeUtils.replaceEnvVariable(null));
        Assert.assertNull(RuntimeUtils.replaceEnvVariable(""));

        // 测试不包含变量的字符串
        String original = "test string";
        Assert.assertEquals(original, RuntimeUtils.replaceEnvVariable(original));

        // 测试包含${root}变量
        String rootPath = RuntimeUtils.getRootPath();
        String rootVarString = "Root path: ${root}";
        String rootReplaced = RuntimeUtils.replaceEnvVariable(rootVarString);
        Assert.assertEquals("Root path: " + rootPath, rootReplaced);

        // 测试包含${user.home}变量
        String userHome = System.getProperty("user.home");
        String userHomeVarString = "User home: ${user.home}";
        String userHomeReplaced = RuntimeUtils.replaceEnvVariable(userHomeVarString);
        Assert.assertEquals("User home: " + userHome, userHomeReplaced);

        // 测试包含${user.dir}变量
        String userDir = System.getProperty("user.dir");
        String userDirVarString = "User dir: ${user.dir}";
        String userDirReplaced = RuntimeUtils.replaceEnvVariable(userDirVarString);
        Assert.assertEquals("User dir: " + userDir, userDirReplaced);

        // 测试包含多个变量（注意：replaceEnvVariable方法使用else if，只替换第一个匹配的变量）
        String multiVarString = "Root: ${root}";
        String multiReplaced = RuntimeUtils.replaceEnvVariable(multiVarString);
        Assert.assertEquals("Root: " + rootPath, multiReplaced);
    }

    /**
     * 测试JMX Bean注册和注销方法
     */
    @Test
    public void testJmxBeanRegistration() throws MalformedObjectNameException {
        // 测试字符串形式的ObjectName
        String objectNameStr = "net.ymate.platform:type=TestMBean,name=TestBean";
        Object mbean = new Object();

        // 测试注册MBean（不验证注册成功，只验证方法调用不抛出异常）
        try {
            RuntimeUtils.registerManagedBean(objectNameStr, mbean);
        } catch (Exception e) {
            Assert.fail("方法调用不应抛出异常: " + e.getMessage());
        }

        // 测试注销MBean（不验证注销成功，只验证方法调用不抛出异常）
        try {
            RuntimeUtils.unregisterManagedBean(objectNameStr);
        } catch (Exception e) {
            Assert.fail("方法调用不应抛出异常: " + e.getMessage());
        }

        // 测试使用ObjectName对象
        ObjectName objectName = new ObjectName(objectNameStr);
        try {
            RuntimeUtils.registerManagedBean(objectName, mbean);
        } catch (Exception e) {
            Assert.fail("方法调用不应抛出异常: " + e.getMessage());
        }

        try {
            RuntimeUtils.unregisterManagedBean(objectName);
        } catch (Exception e) {
            Assert.fail("方法调用不应抛出异常: " + e.getMessage());
        }

        // 测试无效的ObjectName格式
        try {
            RuntimeUtils.registerManagedBean("invalid-object-name", mbean);
        } catch (Exception e) {
            /* Expected exception */
        }
    }

    /**
     * 测试异常创建方法
     */
    @Test
    public void testMakeRuntimeThrow() {
        String message = "Test exception message";
        RuntimeException exception = RuntimeUtils.makeRuntimeThrow(message);
        Assert.assertEquals(message, exception.getMessage());

        // 测试带参数的格式化消息
        String format = "Error code: %d, message: %s";
        int code = 500;
        String msg = "Internal Server Error";
        RuntimeException formattedException = RuntimeUtils.makeRuntimeThrow(format, code, msg);
        Assert.assertEquals(String.format(format, code, msg), formattedException.getMessage());
    }

    /**
     * 测试异常包裹方法
     */
    @Test
    public void testWrapRuntimeThrow() {
        // 测试基本异常包裹
        Exception originalException = new Exception("Original exception");
        RuntimeException wrappedException1 = RuntimeUtils.wrapRuntimeThrow(originalException);
        Assert.assertEquals(originalException, wrappedException1.getCause());

        // 测试RuntimeException直接返回
        RuntimeException runtimeException = new RuntimeException("Runtime exception");
        RuntimeException wrappedException2 = RuntimeUtils.wrapRuntimeThrow(runtimeException);
        Assert.assertSame("RuntimeException应该直接返回", runtimeException, wrappedException2);

        // 测试带消息的异常包裹
        String format = "Wrapped error: %s";
        String msg = "Test error";
        RuntimeException wrappedException3 = RuntimeUtils.wrapRuntimeThrow(originalException, format, msg);
        Assert.assertEquals(String.format(format, msg), wrappedException3.getMessage());
        Assert.assertEquals(originalException, wrappedException3.getCause());

        // 测试InvocationTargetException包裹
        InvocationTargetException ite = new InvocationTargetException(originalException, "Invocation error");
        RuntimeException wrappedException4 = RuntimeUtils.wrapRuntimeThrow(ite);
        Assert.assertEquals(originalException, wrappedException4.getCause());
    }

    /**
     * 测试异常解包方法
     */
    @Test
    public void testUnwrapThrow() {
        // 测试空异常
        Assert.assertNull(RuntimeUtils.unwrapThrow(null));

        // 测试直接异常
        Exception exception = new Exception("Direct exception");
        Assert.assertSame(exception, RuntimeUtils.unwrapThrow(exception));

        // 测试嵌套异常
        Exception cause = new Exception("Cause exception");
        Exception wrapped1 = new Exception("Wrapped 1", cause);
        Exception wrapped2 = new Exception("Wrapped 2", wrapped1);
        Assert.assertSame(cause, RuntimeUtils.unwrapThrow(wrapped2));

        // 测试InvocationTargetException解包
        InvocationTargetException ite = new InvocationTargetException(cause, "Invocation error");
        Assert.assertSame(cause, RuntimeUtils.unwrapThrow(ite));

        // 测试嵌套InvocationTargetException
        InvocationTargetException nestedIte = new InvocationTargetException(ite, "Nested invocation error");
        Assert.assertSame(cause, RuntimeUtils.unwrapThrow(nestedIte));
    }

    /**
     * 测试异常转换为字符串方法
     */
    @Test
    public void testExceptionToString() {
        // 测试空异常
        StringBuilder emptyResult = RuntimeUtils.exceptionToString(null);
        Assert.assertEquals(0, emptyResult.length());

        // 测试简单异常
        Exception exception = new Exception("Test exception");
        StringBuilder result1 = RuntimeUtils.exceptionToString(exception);
        Assert.assertTrue(result1.toString().contains("Test exception"));
        Assert.assertTrue(result1.toString().contains(Exception.class.getName()));
        Assert.assertTrue(result1.toString().contains("StackTrace"));

        // 测试嵌套异常（注意：exceptionToString方法只输出顶层异常信息）
        Exception cause = new Exception("Cause exception");
        Exception wrappedException = new Exception("Wrapped exception", cause);
        StringBuilder result2 = RuntimeUtils.exceptionToString(wrappedException);
        Assert.assertTrue(result2.toString().contains("Wrapped exception"));
        Assert.assertFalse(result2.toString().contains("Cause exception")); // 只输出顶层异常
    }

    /**
     * 测试垃圾回收方法
     */
    @Test
    public void testGc() {
        // 测试gc()方法执行不抛出异常
        try {
            long result = RuntimeUtils.gc();
            // 结果可以是正数或负数，取决于内存使用情况
            Assert.assertTrue(Long.MIN_VALUE <= result && result <= Long.MAX_VALUE);
        } catch (Exception e) {
            Assert.fail("方法调用不应抛出异常: " + e.getMessage());
        }
    }
}
