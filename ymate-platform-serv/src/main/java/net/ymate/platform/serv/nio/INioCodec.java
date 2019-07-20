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
package net.ymate.platform.serv.nio;

import net.ymate.platform.core.beans.annotation.Ignored;
import net.ymate.platform.serv.nio.support.ByteBufferBuilder;

/**
 * 编解码器接口
 *
 * @author 刘镇 (suninformation@163.com) on 15/11/15 下午8:44
 * @version 1.0
 */
@Ignored
public interface INioCodec {

    /**
     * 初始化编解码器
     *
     * @param charset 字符集名称
     */
    void initialize(String charset);

    /**
     * 编码
     *
     * @param message 预编码对象
     * @return 返回编码后的对象
     */
    ByteBufferBuilder encode(Object message);

    /**
     * 解码
     *
     * @param source 预解码对象
     * @return 返回解码后的对象
     */
    Object decode(ByteBufferBuilder source);
}
