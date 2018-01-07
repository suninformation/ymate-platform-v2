/*
 * Copyright 2007-2018 the original author or authors.
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
package net.ymate.platform.log.support;

import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/1/8 下午1:16
 * @version 1.0
 */
public interface ILogooAdapter {

    /**
     * @param flags 自定义标识
     * @return 返回拼装后的标识字符串
     */
    String buildFlag(String[] flags);

    /**
     * 日志写入(本地线程调用)完毕后回调
     *
     * @param flag       自定义标识
     * @param action     自定义动作标识
     * @param attributes 扩展属性映射
     */
    void onLogWritten(String flag, String action, Map<String, Object> attributes);
}
