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
package net.ymate.platform.webmvc.util;

import net.ymate.platform.core.beans.annotation.Ignored;

/**
 * 异常处理器接口
 *
 * @author 刘镇 (suninformation@163.com) on 2017/10/18 下午10:40
 * @since 2.0.6
 */
@Ignored
public interface IExceptionProcessor {

    /**
     * 获取异常处理结果
     *
     * @param target 目标异常对象
     * @return 返回错误码和错误信息
     * @throws Exception 可能产生的任何异常
     */
    Result process(Throwable target) throws Exception;

    /**
     * 异常处理结果
     */
    class Result {

        private final int code;

        private final String message;

        public Result(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }
}
