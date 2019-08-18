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

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.regex.Pattern;

/**
 * 网络操作相关工具类及方法
 *
 * @author 刘镇 (suninformation@163.com) on 2011-7-5 下午12:29:12
 */
public class NetworkUtils {

    /**
     * IP地址工具类，支持IPV4/6版本
     */
    public static class IP {

        private static final Pattern PATTERN = Pattern.compile("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}");

        private static final Pattern PATTERN1 = Pattern.compile(":");

        private static final Pattern PATTERN2 = Pattern.compile("::");

        private static String hostName;

        private static String[] hostIPs;

        /**
         * @return 获取本机名称
         */
        public static String getHostName() {
            if (hostName == null) {
                try {
                    hostName = InetAddress.getLocalHost().getHostName();
                } catch (UnknownHostException e) {
                    hostName = "UNKNOWN_HOST";
                }
            }
            return hostName;
        }

        /**
         * @return 获取本地所有的IP地址数组
         */
        public static String[] getHostIPAddresses() {
            if (hostIPs == null) {
                hostIPs = getHostIPAddresses(getHostName());
            }
            return hostIPs;
        }

        /**
         * @param ipOrName IP地址或计算机名称
         * @return 获取一个DNS或计算机名称所对应的IP地址数组
         */
        public static String[] getHostIPAddresses(String ipOrName) {
            try {
                return Arrays.stream(InetAddress.getAllByName(ipOrName)).map(InetAddress::getHostAddress).toArray(String[]::new);
            } catch (UnknownHostException e) {
                return null;
            }
        }

        /**
         * @param ipAddr 待验证的IP(v4或v6)地址
         * @return 验证IP地址有效性
         */
        public static boolean isIPAddr(String ipAddr) {
            return isIPv4(ipAddr) || isIPv6(ipAddr);
        }

        /**
         * @param ipAddr 待验证的IPv4地址
         * @return 检查IPv4地址的合法性
         */
        public static boolean isIPv4(String ipAddr) {
            return PATTERN.matcher(ipAddr).matches();
        }

        /**
         * @param ipAddr 待验证的IPv6地址
         * @return 检查IPv6地址的合法性
         */
        public static boolean isIPv6(String ipAddr) {
            ipAddr = ipAddr.trim();
            int index = ipAddr.indexOf('%');
            if (index > 0) {
                ipAddr = ipAddr.substring(0, index);
            }
            if (ipAddr.charAt(0) == '[' && ipAddr.charAt(ipAddr.length() - 1) == ']') {
                ipAddr = ipAddr.substring(1, ipAddr.length() - 1);
            }
            return (1 < PATTERN1.split(ipAddr).length)
                    && (PATTERN1.split(ipAddr).length <= 8)
                    && PATTERN2.split(ipAddr).length == 2
                    ? (("::".equals(ipAddr.substring(0, 2)) ? Pattern.matches("^::([\\da-f]{1,4}(:)){0,4}(([\\da-f]{1,4}(:)[\\da-f]{1,4})|([\\da-f]{1,4})|((\\d{1,3}.){3}\\d{1,3}))", ipAddr) : Pattern.matches("^([\\da-f]{1,4}(:|::)){1,5}(([\\da-f]{1,4}(:|::)[\\da-f]{1,4})|([\\da-f]{1,4})|((\\d{1,3}.){3}\\d{1,3}))", ipAddr)))
                    : ("::".equals(ipAddr.substring(ipAddr.length() - 2)) ? Pattern.matches("^([\\da-f]{1,4}(:|::)){1,7}", ipAddr) : Pattern.matches("^([\\da-f]{1,4}:){6}(([\\da-f]{1,4}:[\\da-f]{1,4})|((\\d{1,3}.){3}\\d{1,3}))", ipAddr));
        }

        /**
         * @return 获取本机IPv6地址，遍历网络接口的各个地址并排除诸如0:0:0:0:0:0:0:1等特殊地址，确保外部设置能够访问
         * @throws IOException if an I/O error occurs.
         */
        @Deprecated
        public static String getLocalIPAddr() throws IOException {
            InetAddress inetAddress = null;
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            outer:
            while (networkInterfaces.hasMoreElements()) {
                Enumeration<InetAddress> inetAds = networkInterfaces.nextElement().getInetAddresses();
                while (inetAds.hasMoreElements()) {
                    inetAddress = inetAds.nextElement();
                    if (inetAddress instanceof Inet6Address && !isReservedAddr(inetAddress)) {
                        break outer;
                    }
                }
            }
            assert inetAddress != null;
            String ipAddr = inetAddress.getHostAddress();
            int index = ipAddr.indexOf('%');
            if (index > 0) {
                ipAddr = ipAddr.substring(0, index);
            }
            return ipAddr;
        }

        public static boolean isLocalIPAddr(String ipAddr) {
            return StringUtils.equalsAny(ipAddr, "127.0.0.1", "0:0:0:0:0:0:0:1");
        }

        /**
         * @param inetAddr IP地址
         * @return 检查inetAddr是否为保留IP
         */
        private static boolean isReservedAddr(InetAddress inetAddr) {
            return inetAddr.isAnyLocalAddress() || inetAddr.isLinkLocalAddress() || inetAddr.isLoopbackAddress();
        }
    }
}
