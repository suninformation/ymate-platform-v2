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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 验证器执行结果
 *
 * @author 刘镇 (suninformation@163.com) on 2013-4-12 下午6:00:53
 */
public final class ValidateResult implements Serializable {

    public static String i18nParamLabel(ValidateContext context, String paramName, String label) {
        if (StringUtils.isNotBlank(label)) {
            return formatMessage(context, label, paramName);
        }
        return paramName;
    }

    private static Object[] parseArgs(Object... args) {
        List<Object> argList = new ArrayList<>();
        if (ArrayUtils.isNotEmpty(args)) {
            for (Object arg : args) {
                if (arg.getClass().isArray()) {
                    Collections.addAll(argList, (Object[]) arg);
                } else {
                    argList.add(arg);
                }
            }
        }
        if (!argList.isEmpty()) {
            return argList.toArray();
        }
        return null;
    }

    public static String formatMessage(ValidateContext context, String i18nKey, String defaultValue, Object... args) {
        String message = null;
        if (StringUtils.isNotBlank(i18nKey)) {
            Object[] argArr = parseArgs(args);
            if (StringUtils.isNotBlank(context.getResourceName())) {
                message = context.getOwner().getI18n().formatMessage(context.getResourceName(), i18nKey, StringUtils.EMPTY, argArr);
            }
            if (StringUtils.isBlank(message)) {
                message = context.getOwner().getI18n().formatMessage(IValidator.VALIDATION_I18N_RESOURCE, i18nKey, defaultValue, argArr);
            }
        }
        return StringUtils.defaultIfBlank(message, defaultValue);
    }

    public static Builder builder(ValidateContext context) {
        return new Builder(context);
    }

    public static Builder builder(ValidateContext context, String msg, String i18nKey, String defaultValue) {
        Builder builder = new Builder(context);
        ValidationMeta.ParamInfo paramInfo = context.getParamInfo();
        msg = StringUtils.defaultIfBlank(msg, paramInfo.getMessage());
        if (StringUtils.isNotBlank(msg)) {
            return builder.msg(msg);
        }
        return builder.msg(i18nKey, defaultValue);
    }

    private boolean matched;

    private String name;

    private String msg;

    public ValidateResult() {
    }

    public ValidateResult(String name, String msg) {
        this.name = name;
        this.msg = msg;
    }

    public ValidateResult(String name, String msg, boolean matched) {
        this.name = name;
        this.msg = msg;
        this.matched = matched;
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

    @Override
    public String toString() {
        return String.format("ValidateResult{matched=%s, name='%s', msg='%s'}", matched, name, msg);
    }

    public static class Builder {

        private final ValidateContext context;

        private final ValidateResult target = new ValidateResult();

        public Builder(ValidateContext context) {
            this.context = context;
            target.name = context.getParamInfo().getName();
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

        public Builder msg(String msg) {
            if (StringUtils.isNotBlank(msg)) {
                ValidationMeta.ParamInfo paramInfo = context.getParamInfo();
                String label = paramInfo.getLabel();
                target.msg = context.getOwner().getI18n().formatMsg(msg, StringUtils.isNotBlank(label) ? label : paramInfo.getCustomName());
            }
            return this;
        }

        public Builder msg(String i18nKey, String defaultValue, Object... args) {
            ValidationMeta.ParamInfo paramInfo = context.getParamInfo();
            target.msg = paramInfo.getMessage();
            // 若自定义消息不存在则加载i18n配置
            if (StringUtils.isNotBlank(target.msg)) {
                return msg(target.msg);
            } else if (StringUtils.isNotBlank(i18nKey)) {
                target.msg = formatMessage(context, i18nKey, defaultValue, i18nParamLabel(context, paramInfo.getCustomName(), paramInfo.getLabel()), args);
            }
            return this;
        }

        public ValidateResult build() {
            return target;
        }
    }
}
