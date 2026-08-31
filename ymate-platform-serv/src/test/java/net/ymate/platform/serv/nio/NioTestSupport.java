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
package net.ymate.platform.serv.nio;

import net.ymate.platform.commons.util.NetworkUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * NIO通讯测试辅助工具类，提供动态端口分配、本地主机名获取和资源安静关闭等能力，
 * 用于消除测试间因硬编码端口导致的冲突问题。
 *
 * @author 刘镇 (suninformation@163.com) on 2026/8/31 22:00
 * @since 2.1.4
 */
public final class NioTestSupport {

    private static final Log LOG = LogFactory.getLog(NioTestSupport.class);

    /**
     * 动态端口分配起始基数，用于在偶发端口被占用时快速定位问题。
     */
    private static final AtomicInteger PORT_SEQUENCE = new AtomicInteger(0);

    private NioTestSupport() {
    }

    /**
     * 获取一个本地可用端口。通过 {@link ServerSocket#bind(java.net.SocketAddress, int)} 以端口 0
     * 让操作系统分配一个空闲端口，随后立即关闭并返回该端口。此方法用于避免测试间端口冲突。
     *
     * @return 可用端口号
     * @throws IOException 如果无法获取可用端口
     */
    public static int getAvailablePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            int port = socket.getLocalPort();
            LOG.info(String.format("分配可用端口: %d (序号: %d)", port, PORT_SEQUENCE.incrementAndGet()));
            return port;
        }
    }

    /**
     * 获取本地主机名或首选 IP 地址，用于服务端绑定和客户端连接。
     * 优先返回主机 IP 地址列表中的第一个，若列表为空则返回主机名。
     *
     * @return 本地主机名或 IP 地址
     */
    public static String getLocalHostName() {
        String[] ipAddresses = NetworkUtils.IP.getHostIPAddresses();
        if (ArrayUtils.isNotEmpty(ipAddresses)) {
            return ipAddresses[0];
        }
        return NetworkUtils.IP.getHostName();
    }

    /**
     * 安静关闭一个或多个可关闭对象，吞掉关闭过程中产生的异常并记录日志。
     * 用于测试清理阶段确保资源不泄漏，即使部分资源已关闭或为 null 也能安全调用。
     *
     * @param closeables 待关闭的资源数组，允许包含 null 元素
     */
    public static void closeQuietly(AutoCloseable... closeables) {
        if (closeables == null) {
            return;
        }
        for (AutoCloseable closeable : closeables) {
            if (closeable == null) {
                continue;
            }
            try {
                closeable.close();
            } catch (Exception e) {
                LOG.warn("资源关闭时发生异常（已忽略）: " + e.getMessage());
            }
        }
    }
}
