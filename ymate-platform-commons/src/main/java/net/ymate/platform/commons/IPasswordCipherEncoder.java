/*
 * Copyright 2007-2025 the original author or authors.
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
package net.ymate.platform.commons;

/**
 * 密码编码器接口
 * (提取自 ymate-module-passport 模块)
 *
 * @author 刘镇 (suninformation@163.com) on 2025/5/29 12:12
 * @since 2.1.4
 */
public interface IPasswordCipherEncoder {

    /**
     * 编码
     *
     * @param origin 原始值
     * @return 返回编码后的字符串
     * @throws Exception 可能产生的任务异常
     */
    String encode(String origin) throws Exception;

    /**
     * 编码
     *
     * @param origin 原始值
     * @param salt   盐
     * @return 返回编码后的字符串
     * @throws Exception 可能产生的任务异常
     */
    String encode(String origin, String salt) throws Exception;

    /**
     * 匹配
     *
     * @param origin  原始值
     * @param encoded 编码值
     * @return 返回true表示两值匹配
     * @throws Exception 可能产生的任务异常
     */
    boolean match(String origin, String encoded) throws Exception;

    /**
     * 匹配
     *
     * @param origin  原始值
     * @param encoded 编码值
     * @param salt    盐
     * @return 返回true表示两值匹配
     * @throws Exception 可能产生的任务异常
     */
    boolean match(String origin, String encoded, String salt) throws Exception;
}
