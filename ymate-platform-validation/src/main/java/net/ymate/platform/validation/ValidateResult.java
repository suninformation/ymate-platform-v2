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
package net.ymate.platform.validation;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * 验证器执行结果
 *
 * @author 刘镇 (suninformation@163.com) on 2013-4-12 下午6:00:53
 */
public final class ValidateResult implements Serializable {

    private boolean matched;

    private String name;

    private String msg;

    public ValidateResult() {
    }

    public ValidateResult(String name, String msg) {
        this.name = name;
        this.msg = msg;
    }

    public boolean isMatched() {
        return matched;
    }

    public String getName() {
        return name;
    }

    public String getMsg() {
        return msg;
    }

    public static String formatParamName(ValidateContext context) {
        String pName = StringUtils.defaultIfBlank(context.getParamLabel(), context.getParamName());
        return formatMessage(context, pName, pName);
    }

    public static String formatMessage(ValidateContext context, String i18nKey, String defaultValue, Object... args) {
        String message = null;
        if (StringUtils.isNotBlank(i18nKey)) {
            if (StringUtils.isNotBlank(context.getResourceName())) {
                message = context.getOwner().getI18n().formatMessage(context.getResourceName(), i18nKey, StringUtils.EMPTY, args);
            }
            if (StringUtils.isBlank(message)) {
                message = context.getOwner().getI18n().formatMessage(IValidator.VALIDATION_I18N_RESOURCE, i18nKey, defaultValue, args);
            }
        }
        return StringUtils.defaultIfBlank(message, defaultValue);
    }

    public static Builder builder(ValidateContext context) {
        return new Builder(context);
    }

    public static class Builder {

        private final ValidateContext context;

        private final ValidateResult target = new ValidateResult();

        public Builder(ValidateContext context) {
            this.context = context;
            target.name = formatParamName(context);
        }

        public boolean matched() {
            return target.matched;
        }

        public Builder matched(boolean matched) {
            target.matched = matched;
            return this;
        }

        public String name() {
            return target.name;
        }

        public Builder name(String name) {
            target.name = name;
            return this;
        }

        public Builder msg(String msg) {
            target.msg = ValidateResult.formatMessage(context, msg, msg);
            return this;
        }

        public Builder msg(String i18nKey, String defaultValue, Object... args) {
            // 若自定义消息不存在则加载i18n配置
            if (StringUtils.isNotBlank(context.getParamMessage())) {
                target.msg = formatMessage(context, context.getParamMessage(), context.getParamMessage(), args);
            } else if (StringUtils.isNotBlank(i18nKey)) {
                target.msg = formatMessage(context, i18nKey, defaultValue, args);
            }
            return this;
        }

        public ValidateResult build() {
            return target;
        }
    }
}
