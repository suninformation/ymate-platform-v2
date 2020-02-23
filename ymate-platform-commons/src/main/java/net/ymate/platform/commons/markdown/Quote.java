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

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/02/09 13:31
 */
public final class Quote implements IMarkdown {

    private final StringBuilder content = new StringBuilder();

    public static Quote create(IMarkdown content) {
        return new Quote(content);
    }

    public static Quote create(String content) {
        return new Quote(content);
    }

    private Quote(IMarkdown content) {
        this(content.toMarkdown());
    }

    private Quote(String content) {
        append(content);
    }

    public Quote append(IMarkdown content) {
        return append(content.toMarkdown());
    }

    public Quote append(String content) {
        this.content.append(StringUtils.trimToEmpty(content));
        return this;
    }

    @Override
    public String toMarkdown() {
        if (content.length() == 0) {
            return StringUtils.EMPTY;
        }
        return Arrays.stream(StringUtils.split(content.toString(), P)).map(line -> "> " + line + P).collect(Collectors.joining(">" + P));
    }

    @Override
    public String toString() {
        return toMarkdown();
    }
}
