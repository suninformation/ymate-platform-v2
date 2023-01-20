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
package net.ymate.platform.core.beans.intercept;

import net.ymate.platform.core.beans.annotation.Ignored;

/**
 * 拦截器接口
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/19 上午11:55
 */
@Ignored
public interface IInterceptor {

    enum Direction {

        /**
         * 前置
         */
        BEFORE,

        /**
         * 后置
         */
        AFTER
    }

    enum CleanType {

        /**
         * 前置
         */
        BEFORE,

        /**
         * 后置
         */
        AFTER,

        /**
         * 全部
         */
        ALL
    }

    enum SettingType {

        /**
         * 添加前置
         */
        ADD_BEFORE,

        /**
         * 移除前置
         */
        REMOVE_BEFORE,

        /**
         * 添加后置
         */
        ADD_AFTER,

        /**
         * 移除后置
         */
        REMOVE_AFTER,

        /**
         * 添加全部
         */
        ADD_ALL,

        /**
         * 移除全部
         */
        REMOVE_ALL,

        /**
         * 清理全部
         */
        CLEAN_ALL,

        /**
         * 清理前置
         */
        CLEAN_BEFORE,

        /**
         * 清理后置
         */
        CLEAN_AFTER
    }

    /**
     * 执行拦截动作，其返回结果将影响前置拦截器组是否继续执行，后置拦截器将忽略返回值
     *
     * @param context 拦截器环境上下文对象
     * @return 返回执行结果
     * @throws InterceptException 执行拦截逻辑可能产生的异常
     */
    Object intercept(InterceptContext context) throws InterceptException;
}
