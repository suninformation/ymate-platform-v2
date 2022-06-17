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

import net.ymate.platform.commons.lang.PairObject;
import org.apache.commons.lang3.StringUtils;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * UUID生成器
 *
 * @author 刘镇 (suninformation@163.com) on 2010-10-20 下午02:02:40
 */
public final class UUIDUtils {

    private static final String RAND_CHARS = "0123456789abcdefghigklmnopqrstuvtxyzABCDEFGHIGKLMNOPQRSTUVWXYZ";

    private static final String CHARS_64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789^~abcdefghijklmnopqrstuvwxyz";

    private static final SecureRandom RANDOM = new SecureRandom();

    private UUIDUtils() {
    }

    /**
     * @param o 预加密字符串
     * @return 返回唯一的16位字符串(基于32位当前时间和32位对象的identityHashCode和32位随机数)
     */
    public static String generateCharUUID(Object o) {
        PairObject<Long, Long> ids = generateIds(o);
        return convert(ids.getKey()) + convert(ids.getValue()).replaceAll(StringUtils.SPACE, "o");
    }

    public static String generateNumberUUID(Object o) {
        PairObject<Long, Long> ids = generateIds(o);
        return ids.getKey().toString() + ids.getValue();
    }

    public static String generatePrefixHostUUID(Object o) {
        PairObject<Long, Long> ids = generateIds(o);
        return NetworkUtils.IP.getHostName() + "@" + ids.getKey() + ids.getValue();
    }

    /**
     * @return 返回10个随机字符(基于当前时间和一个随机字符串)
     */
    public static String generateRandomUUID() {
        long id1 = System.currentTimeMillis() & 0x3FFFFFFFL;
        long id3 = randomLong(-0x80000000L, 0x80000000L) & 0x3FFFFFFFL;
        return convert(id1) + convert(id3).replaceAll(StringUtils.SPACE, "o");
    }

    /**
     * @return 返回采用JDK自身UUID生成器生成主键并替换'-'字符
     */
    public static String UUID() {
        return UUID.randomUUID().toString().replace("-", StringUtils.EMPTY);
    }

    private static PairObject<Long, Long> generateIds(Object o) {
        long id1 = System.currentTimeMillis() & 0xFFFFFFFFL;
        long id2 = System.identityHashCode(o);
        long id3 = randomLong(-0x80000000L, 0x80000000L) & 0xFFFFFFFFL;
        id1 <<= 16;
        id1 += (id2 & 0xFFFF0000L) >> 16;
        id3 += (id2 & 0x0000FFFFL) << 32;
        return PairObject.bind(id1, id3);
    }

    private static String convert(long x) {
        if (x == 0) {
            return "0";
        }
        StringBuilder r = new StringBuilder();
        int m = 1 << 6;
        m--;
        while (x != 0) {
            r.insert(0, CHARS_64.charAt((int) (x & m)));
            x = x >>> 6;
        }
        return r.toString();
    }

    /**
     * @param length    长度
     * @param isOnlyNum 是否仅使用数字
     * @return 生成随机字符串
     */
    public static String randomStr(int length, boolean isOnlyNum) {
        int size = isOnlyNum ? 10 : 62;
        StringBuilder hash = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            hash.append(RAND_CHARS.charAt(RANDOM.nextInt(size)));
        }
        return hash.toString();
    }

    public static long randomLong(long min, long max) {
        return min + (long) (RANDOM.nextDouble() * (max - min));
    }

    public static int randomInt(int min, int max) {
        return min + (int) (RANDOM.nextDouble() * (max - min));
    }
}
