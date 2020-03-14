/*
 * Copyright 2007-2020 the original author or authors.
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
package net.ymate.platform.commons.markdown;

import org.apache.commons.lang3.StringUtils;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/02/09 13:16
 */
public final class Code implements IMarkdown {

    private final StringBuilder code = new StringBuilder();

    private final String language;

    public static Code create(IMarkdown code) {
        return new Code(code);
    }

    public static Code create(IMarkdown code, String language) {
        return new Code(code, language);
    }

    public static Code create(String code) {
        return new Code(code);
    }

    public static Code create(String code, String language) {
        return new Code(code, language);
    }

    private Code(IMarkdown code) {
        this(code.toMarkdown(), null);
    }

    private Code(IMarkdown code, String language) {
        this(code.toMarkdown(), language);
    }

    private Code(String code) {
        this(code, null);
    }

    private Code(String code, String language) {
        append(code);
        this.language = StringUtils.trimToEmpty(language);
    }

    public Code append(String code) {
        this.code.append(StringUtils.trimToEmpty(code));
        return this;
    }

    @Override
    public String toMarkdown() {
        if (code.length() == 0) {
            return StringUtils.EMPTY;
        }
        if (StringUtils.contains(code, StringUtils.LF) || StringUtils.isNotBlank(language)) {
            return String.format("```%s\n%s\n```\n", language, code);
        }
        return String.format("`%s`", code);
    }

    @Override
    public String toString() {
        return toMarkdown();
    }
}
