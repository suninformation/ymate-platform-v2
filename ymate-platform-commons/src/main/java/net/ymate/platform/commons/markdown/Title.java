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
 * @author 刘镇 (suninformation@163.com) on 2020/02/09 12:21
 */
public class Title implements IMarkdown {

    public StringBuilder title = new StringBuilder();

    private int level;

    public static Title create(IMarkdown title) {
        return new Title(title);
    }

    public static Title create(String title) {
        return new Title(title);
    }

    public static Title create(IMarkdown title, int level) {
        return new Title(title, level);
    }

    public static Title create(String title, int level) {
        return new Title(title, level);
    }

    private Title(IMarkdown title) {
        this(title.toMarkdown());
    }

    private Title(String title) {
        this(title, 1);
    }

    private Title(IMarkdown title, int level) {
        this(title.toMarkdown(), level);
    }

    private Title(String title, int level) {
        append(title);
        this.level = level <= 0 ? 1 : Math.min(level, 6);
    }

    public Title append(IMarkdown content) {
        return append(content.toMarkdown());
    }

    public Title append(String content) {
        this.title.append(StringUtils.trimToEmpty(content));
        return this;
    }

    @Override
    public String toMarkdown() {
        if (title.length() == 0) {
            return StringUtils.EMPTY;
        }
        return String.format("%s %s", StringUtils.repeat('#', level), StringUtils.replaceEach(title.toString(), new String[]{"\r\n", "\r", "\n", "\t"}, new String[]{StringUtils.SPACE, StringUtils.EMPTY, StringUtils.SPACE, TAB}));
    }

    @Override
    public String toString() {
        return toMarkdown();
    }
}
