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
package net.ymate.platform.core.persistence;

/**
 * 事务业务操作类，支持业务返回执行结果
 *
 * @param <T> 元素类型
 * @author 刘镇 (suninformation@163.com) on 15/4/28 下午9:26
 */
public abstract class AbstractTrade<T> implements ITrade {

    protected T result;

    /**
     * 获取执行结果
     *
     * @return 返回执行结果对象
     */
    public T getResult() {
        return result;
    }

    @Override
    public void deal() throws Throwable {
        result = dealing();
    }

    /**
     * 执行事务处理
     *
     * @return 返回业务执行结果对象
     * @throws Throwable 可能产生的异常
     */
    public abstract T dealing() throws Throwable;
}
