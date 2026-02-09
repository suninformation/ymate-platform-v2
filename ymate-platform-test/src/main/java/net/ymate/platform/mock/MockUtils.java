/*
 * Copyright 2007-2023 the original author or authors.
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
package net.ymate.platform.mock;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Mock工具类，提供通用的工具方法
 *
 * @author 刘镇 (suninformation@163.com) on 2026-02-05 16:30
 * @since 2.1.4
 */
public final class MockUtils {

    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");

    private static final String DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";

    private static final String[] DATE_FORMATS = new String[]{
            "EEE, dd MMM yyyy HH:mm:ss zzz",
            "EEE, dd-MMM-yy HH:mm:ss zzz",
            "EEE MMM dd HH:mm:ss yyyy"
    };

    /**
     * 格式化日期为HTTP头部格式
     *
     * @param date 日期时间戳
     * @return 格式化后的日期字符串
     */
    public static String formatDate(long date) {
        return newDateFormat().format(new Date(date));
    }

    /**
     * 解析HTTP头部格式的日期
     *
     * @param value 日期字符串
     * @return 日期时间戳
     * @throws IllegalArgumentException 如果日期格式无效
     */
    public static long parseDate(String value) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.US);
        dateFormat.setTimeZone(GMT);
        try {
            return dateFormat.parse(value).getTime();
        } catch (ParseException ex) {
            throw new IllegalArgumentException("Invalid date format: " + value, ex);
        }
    }

    /**
     * 解析HTTP头部格式的日期，尝试多种格式
     *
     * @param name  头部名称
     * @param value 日期字符串
     * @return 日期时间戳
     * @throws IllegalArgumentException 如果日期格式无效
     */
    public static long parseDateHeader(String name, String value) {
        for (String dateFormat : DATE_FORMATS) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.US);
            simpleDateFormat.setTimeZone(GMT);
            try {
                return simpleDateFormat.parse(value).getTime();
            } catch (ParseException ex) {
                // 忽略，尝试下一种格式
            }
        }
        throw new IllegalArgumentException("Cannot parse date value '" + value + "' for '" + name + "' header");
    }

    /**
     * 创建新的日期格式化器
     *
     * @return 日期格式化器
     */
    private static SimpleDateFormat newDateFormat() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.US);
        dateFormat.setTimeZone(GMT);
        return dateFormat;
    }

    /**
     * 私有构造方法，防止实例化
     */
    private MockUtils() {
    }
}
