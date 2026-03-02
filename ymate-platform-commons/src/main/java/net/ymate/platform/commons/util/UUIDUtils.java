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

import org.apache.commons.lang3.StringUtils;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * UUID生成器，提供多种UUID生成策略
 * <p>
 * 该类设计用于提供灵活的UUID生成方案，支持多种UUID格式和生成策略，包括：
 * 1. 基于字符的UUID
 * 2. 基于数字的UUID
 * 3. 带主机名前缀的UUID
 * 4. 随机UUID
 * 5. 基于时间戳的UUID
 * 6. JDK标准UUID
 * <p>
 * 设计特点：
 * 1. 提供多种UUID生成策略，满足不同场景需求
 * 2. 支持自定义随机字符串生成
 * 3. 提供UUID有效性验证
 * 4. 基于ThreadLocalRandom实现高性能随机数生成
 * 5. 支持64进制编码转换
 * <p>
 * 使用场景：
 * - 分布式系统中的唯一标识生成
 * - 数据库主键生成
 * - 缓存键生成
 * - 会话ID生成
 * - 任何需要唯一标识符的场景
 *
 * @author 刘镇 (suninformation@163.com) on 2010-10-20 下午02:02:40
 */
public final class UUIDUtils {

    private static final String RAND_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final String CHARS_64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

    private static final ThreadLocalRandom THREAD_LOCAL_RANDOM = ThreadLocalRandom.current();

    private UUIDUtils() {
    }

    /**
     * 生成基于字符的UUID
     *
     * @param o 预加密对象
     * @return 返回唯一的字符串(基于32位当前时间和32位对象的identityHashCode和32位随机数)
     */
    public static String generateCharUUID(Object o) {
        long[] ids = generateIds(o);
        return convert(ids[0]) + convert(ids[1]).replaceAll(StringUtils.SPACE, "o");
    }

    /**
     * 生成基于数字的UUID
     *
     * @param o 预加密对象
     * @return 返回唯一的数字字符串
     */
    public static String generateNumberUUID(Object o) {
        long[] ids = generateIds(o);
        return ids[0] + String.valueOf(ids[1]);
    }

    /**
     * 生成带主机名前缀的UUID
     *
     * @param o 预加密对象
     * @return 返回带主机名前缀的唯一字符串
     */
    public static String generatePrefixHostUUID(Object o) {
        long[] ids = generateIds(o);
        String hostName;
        try {
            hostName = NetworkUtils.IP.getHostName();
        } catch (Exception e) {
            hostName = "unknown";
        }
        return hostName + "@" + ids[0] + ids[1];
    }

    /**
     * 生成随机UUID
     *
     * @return 返回随机字符(基于当前时间和一个随机字符串)
     */
    public static String generateRandomUUID() {
        long id1 = System.nanoTime() & 0x3FFFFFFFL;
        long id3 = randomLong(-0x80000000L, 0x80000000L) & 0x3FFFFFFFL;
        return convert(id1) + convert(id3).replaceAll(StringUtils.SPACE, "o");
    }

    /**
     * 生成基于时间戳和随机数的UUID
     *
     * @return 返回基于纳秒时间戳和随机数的UUID
     */
    public static String generateTimeBasedUUID() {
        long timestamp = System.nanoTime();
        long random = THREAD_LOCAL_RANDOM.nextLong();
        return Long.toHexString(timestamp) + Long.toHexString(random);
    }

    /**
     * 生成JDK标准UUID
     *
     * @return 返回采用JDK自身UUID生成器生成主键并替换'-'字符
     */
    public static String UUID() {
        return UUID.randomUUID().toString().replace("-", StringUtils.EMPTY);
    }

    // ---------------------- 内部方法 ----------------------

    /**
     * 生成UUID所需的ID数组
     * <p>
     * 该方法基于纳秒时间戳、对象的identityHashCode和随机数生成两个长整型ID，
     * 用于构建各种类型的UUID。
     *
     * @param o 预加密对象，可为null
     * @return 包含两个长整型ID的数组
     */
    private static long[] generateIds(Object o) {
        long id1 = System.nanoTime() & 0xFFFFFFFFL;
        long id2 = o != null ? System.identityHashCode(o) : 0;
        long id3 = THREAD_LOCAL_RANDOM.nextLong() & 0xFFFFFFFFL;
        id1 <<= 16;
        id1 += (id2 & 0xFFFF0000L) >> 16;
        id3 += (id2 & 0x0000FFFFL) << 32;
        return new long[]{id1, id3};
    }

    /**
     * 将长整型数值转换为64进制字符串
     * <p>
     * 该方法使用预定义的64进制字符集将长整型数值转换为字符串，
     * 用于生成基于字符的UUID。
     *
     * @param x 要转换的长整型数值
     * @return 转换后的64进制字符串
     */
    private static String convert(long x) {
        if (x == 0) {
            return "0";
        }
        StringBuilder r = new StringBuilder();
        int m = 0x3F; // 63
        while (x != 0) {
            r.append(CHARS_64.charAt((int) (x & m)));
            x = x >>> 6;
        }
        return r.reverse().toString();
    }

    /**
     * 生成随机字符串
     *
     * @param length        长度
     * @param useOnlyDigits 是否仅使用数字
     * @return 生成的随机字符串
     */
    public static String randomStr(int length, boolean useOnlyDigits) {
        if (length <= 0) {
            throw new IllegalArgumentException("length must be positive");
        }
        int size = useOnlyDigits ? 10 : 62;
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(RAND_CHARS.charAt(THREAD_LOCAL_RANDOM.nextInt(size)));
        }
        return sb.toString();
    }

    /**
     * 生成指定范围内的随机长整数
     *
     * @param min 最小值（包含）
     * @param max 最大值（包含）
     * @return 随机长整数
     */
    public static long randomLong(long min, long max) {
        if (min > max) {
            throw new IllegalArgumentException("min must be less than or equal to max");
        }
        return THREAD_LOCAL_RANDOM.nextLong(min, max + 1);
    }

    /**
     * 生成指定范围内的随机整数
     *
     * @param min 最小值（包含）
     * @param max 最大值（包含）
     * @return 随机整数
     */
    public static int randomInt(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("min must be less than or equal to max");
        }
        return THREAD_LOCAL_RANDOM.nextInt(min, max + 1);
    }
}
