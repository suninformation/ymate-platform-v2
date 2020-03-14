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
 * @author 刘镇 (suninformation@163.com) on 2020/02/09 12:35
 */
public final class Text implements IMarkdown {

    private final StringBuilder content = new StringBuilder();

    private final Style style;

    public static Text create(IMarkdown content) {
        return new Text(content);
    }

    public static Text create(IMarkdown content, Style style) {
        return new Text(content, style);
    }

    public static Text create(String content) {
        return new Text(content);
    }

    public static Text create(String content, Style style) {
        return new Text(content, style);
    }

    private Text(IMarkdown content) {
        this(content.toMarkdown(), null);
    }

    private Text(IMarkdown content, Style style) {
        this(content.toMarkdown(), style);
    }

    private Text(String content) {
        this(content, null);
    }

    private Text(String content, Style style) {
        append(content);
        this.style = style == null ? Style.NORMAL : style;
    }

    public Text append(IMarkdown content) {
        return append(content.toMarkdown());
    }

    public Text append(String content) {
        this.content.append(StringUtils.trimToEmpty(content));
        return this;
    }

    @Override
    public String toMarkdown() {
        switch (style) {
            case BOLD:
                return toString("**%s**");
            case ITALIC:
                return toString("*%s*");
            case UNDERLINE:
                return toString("<u>%s</u>");
            case STRIKEOUT:
                return toString("~~%s~~");
            default:
        }
        return content.toString();
    }

    private String toString(String prefix) {
        if (content.length() == 0) {
            return StringUtils.EMPTY;
        }
        return Arrays.stream(StringUtils.split(content.toString(), P)).map(c -> String.format(prefix, c)).collect(Collectors.joining(P));
    }

    @Override
    public String toString() {
        return toMarkdown();
    }

    public enum Style {
        /**
         * 正常
         */
        NORMAL,

        /**
         * 粗体
         */
        BOLD,

        /**
         * 斜体
         */
        ITALIC,

        /**
         * 下划线
         */
        UNDERLINE,

        /**
         * 删除线
         */
        STRIKEOUT
    }
}
