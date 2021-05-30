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
package net.ymate.platform.serv;

import java.io.Serializable;
import java.util.Map;

/**
 * 会话包装器接口
 *
 * @param <SESSION_TYPE> 会话类型
 * @param <SESSION_ID>   会话标识类型
 * @author 刘镇 (suninformation@163.com) on 2018/11/14 11:41 AM
 */
public interface ISessionWrapper<SESSION_TYPE extends Serializable, SESSION_ID> extends Serializable {

    /**
     * 获取当前会话标识符
     *
     * @return 返回会话标识
     */
    SESSION_ID getId();

    /**
     * 获取会话对象
     *
     * @return 返回会话实例对象
     */
    SESSION_TYPE getSession();

    /**
     * 获取当前会话属性映射
     *
     * @return 返回会话属性映射
     */
    Map<String, Object> getAttributes();

    /**
     * 获取指定attrKey对应的属性并转换为目标类型
     *
     * @param attrKey 属性键名
     * @param <T>     目标类型
     * @return 返回属性值对象
     */
    <T> T getAttribute(String attrKey);

    /**
     * 向当前会话添加属性
     *
     * @param attrKey   属性键名
     * @param attrValue 属性值
     */
    void addAttribute(String attrKey, Object attrValue);

    /**
     * 更新会话活动状态(触发心跳, 通知会话包装器更新心跳时间))
     */
    void touch();

    /**
     * 获取最后心跳时间(毫秒)
     *
     * @return 返回最后更新会话状态的时间(毫秒)
     */
    long getLastTouchTime();
}
