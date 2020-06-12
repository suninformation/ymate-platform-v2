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
package net.ymate.platform.commons.serialize;

/**
 * @author 刘镇 (suninformation@163.com) on 2017/10/3 下午3:52
 */
public interface ISerializer {

    /**
     * 获取序列化后的ContentType类型
     *
     * @return 返回ContentType类型字符串
     */
    String getContentType();

    /**
     * 序列号对象
     *
     * @param object 待序列化对象
     * @return 返回序列化后的字节数组
     * @throws Exception 可能产生的任何异常
     */
    byte[] serialize(Object object) throws Exception;

    /**
     * 反序列化对象
     *
     * @param bytes 待反序列化字节数组
     * @param clazz 序列化目标类型
     * @param <T>   类泛型
     * @return 返回反序列化后的类对象
     * @throws Exception 可能产生的任何异常
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz) throws Exception;
}
