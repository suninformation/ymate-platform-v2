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

import org.junit.Before;
import org.junit.Test;

import java.net.*;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * NetworkUtils类的单元测试
 *
 * @author 刘镇 (suninformation@163.com) on 2022/6/21 00:19
 * @since 2.1.2
 */
public class NetworkUtilsTest {

    /**
     * 在每个测试方法执行前执行
     */
    @Before
    public void setUp() {
        // 重置静态变量，确保测试独立性
        resetStaticVariables();
    }

    /**
     * 重置静态变量
     */
    private void resetStaticVariables() {
        try {
            // 使用反射重置静态变量
            java.lang.reflect.Field hostNameField = NetworkUtils.IP.class.getDeclaredField("hostName");
            hostNameField.setAccessible(true);
            hostNameField.set(null, null);

            java.lang.reflect.Field hostIPsField = NetworkUtils.IP.class.getDeclaredField("hostIPs");
            hostIPsField.setAccessible(true);
            hostIPsField.set(null, null);
        } catch (Exception e) {
            // 忽略反射异常
        }
    }

    /**
     * 测试getHostName方法
     */
    @Test
    public void testGetHostName() {
        // 测试正常情况
        String hostName = NetworkUtils.IP.getHostName();
        assertNotNull(hostName);
        assertNotEquals("UNKNOWN_HOST", hostName);

        // 测试缓存机制
        String hostName2 = NetworkUtils.IP.getHostName();
        assertEquals(hostName, hostName2);
    }

    /**
     * 测试getHostIPAddresses方法（无参数）
     */
    @Test
    public void testGetHostIPAddresses() {
        // 测试正常情况
        String[] hostIPs = NetworkUtils.IP.getHostIPAddresses();
        assertNotNull(hostIPs);
        assertTrue(hostIPs.length > 0);

        // 测试缓存机制
        String[] hostIPs2 = NetworkUtils.IP.getHostIPAddresses();
        assertSame(hostIPs, hostIPs2);
    }

    /**
     * 测试getHostIPAddresses方法（有参数）
     */
    @Test
    public void testGetHostIPAddressesWithParam() {
        // 测试正常情况 - 使用localhost
        String[] localhostIPs = NetworkUtils.IP.getHostIPAddresses("localhost");
        assertNotNull(localhostIPs);
        assertTrue(localhostIPs.length > 0);

        // 测试正常情况 - 使用127.0.0.1
        String[] loopbackIPs = NetworkUtils.IP.getHostIPAddresses("127.0.0.1");
        assertNotNull(loopbackIPs);
        assertTrue(loopbackIPs.length > 0);

        // 测试异常情况 - 使用无效主机名
        String[] invalidIPs = NetworkUtils.IP.getHostIPAddresses("invalid-host-name-123456");
        assertNotNull(invalidIPs);
        assertEquals(0, invalidIPs.length);
    }

    /**
     * 测试isIPAddr方法
     */
    @Test
    public void testIsIPAddr() {
        for (Object[] testData : ipAddrProvider()) {
            String ipAddr = (String) testData[0];
            boolean expected = (boolean) testData[1];
            boolean result = NetworkUtils.IP.isIPAddr(ipAddr);
            assertEquals("IP地址测试失败: " + ipAddr, expected, result);
        }
    }

    /**
     * 提供IP地址测试数据
     */
    static List<Object[]> ipAddrProvider() {
        return Arrays.asList(
                // 有效IPv4地址
                new Object[]{"127.0.0.1", true},
                new Object[]{"0.0.0.0", true},
                new Object[]{"255.255.255.255", true},
                new Object[]{"192.168.1.1", true},
                new Object[]{"10.0.0.1", true},
                new Object[]{"172.16.0.1", true},
                // 有效IPv6地址
                new Object[]{"::1", true},
                new Object[]{"2001:0db8:85a3:0000:0000:8a2e:0370:7334", true},
                new Object[]{"2001:db8:85a3:0:0:8a2e:370:7334", true},
                new Object[]{"2001:db8:85a3::8a2e:370:7334", true},
                new Object[]{"fe80::1ff:fe23:4567:890a", true},
                new Object[]{"[::1]", true}, // 带方括号的IPv6地址
                // 无效IP地址
                new Object[]{null, false},
                new Object[]{"", false},
                new Object[]{"invalid-ip", false},
                new Object[]{"999.999.999.999", false},
                new Object[]{"256.0.0.1", false},
                new Object[]{"192.168.1.", false},
                new Object[]{"192.168.1.1.1", false},
                new Object[]{"2001:0db8:85a3:0000:0000:8a2e:0370:7334:7334", false}, // 过长的IPv6地址
                new Object[]{"2001:0db8:85a3:0000:0000:8a2e:0370", false} // 过短的IPv6地址
        );
    }

    /**
     * 测试isIPv4方法
     */
    @Test
    public void testIsIPv4() {
        for (Object[] testData : ipv4AddrProvider()) {
            String ipAddr = (String) testData[0];
            boolean expected = (boolean) testData[1];
            boolean result = NetworkUtils.IP.isIPv4(ipAddr);
            assertEquals("IPv4地址测试失败: " + ipAddr, expected, result);
        }
    }

    /**
     * 提供IPv4地址测试数据
     */
    static List<Object[]> ipv4AddrProvider() {
        return Arrays.asList(
                // 有效IPv4地址
                new Object[]{"127.0.0.1", true},
                new Object[]{"0.0.0.0", true},
                new Object[]{"255.255.255.255", true},
                new Object[]{"192.168.1.1", true},
                new Object[]{"10.0.0.1", true},
                new Object[]{"172.16.0.1", true},
                // 无效IPv4地址
                new Object[]{null, false},
                new Object[]{"", false},
                new Object[]{"invalid-ip", false},
                new Object[]{"999.999.999.999", false},
                new Object[]{"256.0.0.1", false},
                new Object[]{"192.168.1.", false},
                new Object[]{"192.168.1.1.1", false},
                // IPv6地址
                new Object[]{"::1", false},
                new Object[]{"2001:0db8:85a3:0000:0000:8a2e:0370:7334", false}
        );
    }

    /**
     * 测试isIPv6方法
     */
    @Test
    public void testIsIPv6() {
        for (Object[] testData : ipv6AddrProvider()) {
            String ipAddr = (String) testData[0];
            boolean expected = (boolean) testData[1];
            boolean result = NetworkUtils.IP.isIPv6(ipAddr);
            assertEquals("IPv6地址测试失败: " + ipAddr, expected, result);
        }
    }

    /**
     * 提供IPv6地址测试数据
     */
    static List<Object[]> ipv6AddrProvider() {
        return Arrays.asList(
                // 有效IPv6地址
                new Object[]{"::1", true},
                new Object[]{"2001:0db8:85a3:0000:0000:8a2e:0370:7334", true},
                new Object[]{"2001:db8:85a3:0:0:8a2e:370:7334", true},
                new Object[]{"2001:db8:85a3::8a2e:370:7334", true},
                new Object[]{"fe80::1ff:fe23:4567:890a", true},
                new Object[]{"[::1]", true}, // 带方括号的IPv6地址
                new Object[]{"fe80::1ff:fe23:4567:890a%eth0", true}, // 带区域标识符的IPv6地址
                // 无效IPv6地址
                new Object[]{null, false},
                new Object[]{"", false},
                new Object[]{"invalid-ip", false},
                new Object[]{"127.0.0.1", false},
                new Object[]{"2001:0db8:85a3:0000:0000:8a2e:0370:7334:7334", false}, // 过长的IPv6地址
                new Object[]{"2001:0db8:85a3:0000:0000:8a2e:0370", false} // 过短的IPv6地址
        );
    }

    /**
     * 测试getLocalIPAddr方法（无参数）
     */
    @Test
    public void testGetLocalIPAddr() {
        // 测试正常情况
        String localIPv6Addr = NetworkUtils.IP.getLocalIPAddr();
        assertNotNull(localIPv6Addr);
        assertTrue(NetworkUtils.IP.isIPv6(localIPv6Addr));
    }

    /**
     * 测试getLocalIPv4Addr方法
     */
    @Test
    public void testGetLocalIPv4Addr() {
        // 测试正常情况
        String localIPv4Addr = NetworkUtils.IP.getLocalIPv4Addr();
        assertNotNull(localIPv4Addr);
        assertTrue(NetworkUtils.IP.isIPv4(localIPv4Addr));
    }

    /**
     * 测试getLocalIPAddr方法（有参数）
     * 由于该方法直接操作网络接口，难以模拟，这里主要测试其返回值类型
     */
    @Test
    public void testGetLocalIPAddrWithParam() throws SocketException {
        // 测试获取IPv4地址
        InetAddress ipv4Addr = NetworkUtils.IP.getLocalIPAddr(true);
        if (ipv4Addr != null) {
            assertTrue(ipv4Addr instanceof Inet4Address);
        }

        // 测试获取IPv6地址
        InetAddress ipv6Addr = NetworkUtils.IP.getLocalIPAddr(false);
        if (ipv6Addr != null) {
            assertTrue(ipv6Addr instanceof Inet6Address);
        }
    }

    /**
     * 测试isLocalIPAddr方法
     */
    @Test
    public void testIsLocalIPAddr() {
        for (Object[] testData : localIPAddrProvider()) {
            String ipAddr = (String) testData[0];
            boolean expected = (boolean) testData[1];
            boolean result = NetworkUtils.IP.isLocalIPAddr(ipAddr);
            assertEquals("本地IP地址测试失败: " + ipAddr, expected, result);
        }
    }

    /**
     * 提供本地IP地址测试数据
     */
    static List<Object[]> localIPAddrProvider() {
        return Arrays.asList(
                // 本地地址
                new Object[]{"127.0.0.1", true},
                new Object[]{"0.0.0.0", true},
                new Object[]{"::1", true},
                new Object[]{"fe80::1ff:fe23:4567:890a", true},
                // 非本地地址
                new Object[]{null, false},
                new Object[]{"", false},
                new Object[]{"invalid-ip", false},
                new Object[]{"192.168.1.1", false}, // 私有地址，但不是本地地址
                new Object[]{"8.8.8.8", false} // 公共地址
        );
    }

    /**
     * 测试isReservedAddr方法
     */
    @Test
    public void testIsReservedAddr() throws UnknownHostException {
        // 测试回环地址
        InetAddress loopbackAddr = InetAddress.getByName("127.0.0.1");
        assertTrue(NetworkUtils.IP.isReservedAddr(loopbackAddr));

        // 测试链路本地地址
        InetAddress linkLocalAddr = InetAddress.getByName("fe80::1ff:fe23:4567:890a");
        assertTrue(NetworkUtils.IP.isReservedAddr(linkLocalAddr));

        // 测试任意本地地址
        InetAddress anyLocalAddr = InetAddress.getByName("0.0.0.0");
        assertTrue(NetworkUtils.IP.isReservedAddr(anyLocalAddr));

        // 测试公共地址
        InetAddress publicAddr = InetAddress.getByName("8.8.8.8");
        assertFalse(NetworkUtils.IP.isReservedAddr(publicAddr));
    }
}
