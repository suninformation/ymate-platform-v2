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
package net.ymate.platform.commons.exception;

/**
 * 数据版本不匹配异常
 *
 * @author 刘镇 (suninformation@163.com) on 2017/10/18 下午1:59
 */
public class DataVersionMismatchException extends RuntimeException {

    /**
     * 版本比较，若版本不相等则抛出 DataVersionMismatchException 异常
     * <p>
     * 比较规则：
     * <ul>
     *     <li>两个版本均为 null 时视为相等，不抛出异常；</li>
     *     <li>仅一方为 null 时视为不相等，抛出异常；</li>
     *     <li>类型相同时，若两者均实现了 {@link Comparable} 且可相互比较则优先使用 {@code compareTo}，
     *     否则回退到 {@link Object#equals}；</li>
     *     <li>类型不同但均为数值类型时，按数值大小进行比较，规避 {@code Long} 与 {@code Integer}
     *     等因类型不一致导致 {@code equals} 返回 false 的问题；</li>
     *     <li>其它类型不一致的场景，始终视为不相等。</li>
     * </ul>
     *
     * @param originVersion  原始版本(持有者拥有的版本)
     * @param currentVersion 当前版本(数据最新版本)
     */
    @SuppressWarnings("unchecked")
    public static <T> void comparisonVersion(T originVersion, T currentVersion) {
        // 两者均为 null 视为相等
        if (originVersion == null && currentVersion == null) {
            return;
        }
        // 仅一方为 null，版本不匹配
        if (originVersion == null || currentVersion == null) {
            doThrowMismatch(originVersion, currentVersion);
        }
        // 类型相同
        if (originVersion.getClass().equals(currentVersion.getClass())) {
            if (originVersion instanceof Comparable) {
                try {
                    if (((Comparable<Object>) originVersion).compareTo(currentVersion) != 0) {
                        doThrowMismatch(originVersion, currentVersion);
                    }
                    return;
                } catch (ClassCastException ignored) {
                    // 类型相同但不兼容 Comparable（理论上不会发生），回退到 equals
                }
            }
            if (!originVersion.equals(currentVersion)) {
                doThrowMismatch(originVersion, currentVersion);
            }
            return;
        }
        // 类型不同但均为数值类型，按数值比较
        if (originVersion instanceof Number && currentVersion instanceof Number) {
            if (((Number) originVersion).longValue() != ((Number) currentVersion).longValue()) {
                doThrowMismatch(originVersion, currentVersion);
            }
            return;
        }
        // 类型不同且无法比较，视为版本不匹配
        doThrowMismatch(originVersion, currentVersion);
    }

    private static <T> void doThrowMismatch(T originVersion, T currentVersion) {
        throw new DataVersionMismatchException(String.format("Data version mismatch. origin: %s, current: %s", originVersion, currentVersion));
    }

    public DataVersionMismatchException() {
        super();
    }

    public DataVersionMismatchException(String message) {
        super(message);
    }

    public DataVersionMismatchException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataVersionMismatchException(Throwable cause) {
        super(cause);
    }
}
