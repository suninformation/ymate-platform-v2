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

import java.io.Closeable;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-07-31 18:34
 * @since 2.1.0
 */
public interface IService extends Closeable {

    /**
     * 初始化
     *
     * @param owner 指定所属容器参数对象
     * @throws Exception 初始过程中产生的任何异常
     */
    void initialize(IClient<?, ?> owner) throws Exception;

    /**
     * 是否已初始化
     *
     * @return 返回true表示已初始化
     */
    boolean isInitialized();

    /**
     * 启动服务
     */
    void start();

    /**
     * 是否已启动
     *
     * @return 返回true表示已启动
     */
    boolean isStarted();
}
