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

import net.ymate.platform.commons.util.ParamUtils;
import net.ymate.platform.core.beans.annotation.Ignored;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

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

        private final String code;

        private final String message;

        private final Map<String, Object> attributes = new LinkedHashMap<>();

        public Result(int code, String message) {
            this.code = String.valueOf(code);
            this.message = message;
        }

        public Result(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

        public Map<String, Object> getAttributes() {
            return attributes;
        }

        public Result addAttribute(String attrKey, Object attrValue) {
            if (StringUtils.isNotBlank(attrKey) && !ParamUtils.isInvalid(attrValue)) {
                attributes.put(attrKey, attrValue);
            }
            return this;
        }

        public Result addAttributes(Map<String, Object> attributes) {
            this.attributes.putAll(attributes);
            return this;
        }
    }
}
