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
 * @author 刘镇 (suninformation@163.com) on 2020/02/09 15:29
 */
public final class MarkdownBuilder implements IMarkdown {

    private final StringBuilder stringBuilder = new StringBuilder();

    public static MarkdownBuilder create() {
        return new MarkdownBuilder();
    }

    private MarkdownBuilder() {
    }

    public MarkdownBuilder br() {
        stringBuilder.append(P);
        return this;
    }

    public MarkdownBuilder p() {
        stringBuilder.append(P).append(P);
        return this;
    }

    public MarkdownBuilder p(int repeat) {
        stringBuilder.append(StringUtils.repeat(P, Math.max(repeat, 1)));
        return this;
    }

    public MarkdownBuilder hr() {
        stringBuilder.append(HR).append(P).append(P);
        return this;
    }

    public MarkdownBuilder space() {
        stringBuilder.append(StringUtils.SPACE);
        return this;
    }

    public MarkdownBuilder space(int repeat) {
        stringBuilder.append(StringUtils.repeat(StringUtils.SPACE, Math.max(repeat, 1)));
        return this;
    }

    public MarkdownBuilder tab() {
        stringBuilder.append(TAB);
        return this;
    }

    public int length() {
        return stringBuilder.length();
    }

    public MarkdownBuilder append(IMarkdown markdown) {
        stringBuilder.append(markdown.toMarkdown());
        return this;
    }

    public MarkdownBuilder append(String content) {
        stringBuilder.append(content);
        return this;
    }

    // -----

    public MarkdownBuilder title(IMarkdown title) {
        stringBuilder.append(Title.create(title));
        return this;
    }

    public MarkdownBuilder title(String title) {
        stringBuilder.append(Title.create(title));
        return this;
    }

    public MarkdownBuilder title(IMarkdown title, int level) {
        stringBuilder.append(Title.create(title, level));
        return this;
    }

    public MarkdownBuilder title(String title, int level) {
        stringBuilder.append(Title.create(title, level));
        return this;
    }

    // -----

    public MarkdownBuilder text(IMarkdown text) {
        stringBuilder.append(Text.create(text));
        return this;
    }

    public MarkdownBuilder text(String text) {
        stringBuilder.append(Text.create(text));
        return this;
    }

    public MarkdownBuilder text(IMarkdown text, Text.Style style) {
        stringBuilder.append(Text.create(text, style));
        return this;
    }

    public MarkdownBuilder text(String text, Text.Style style) {
        stringBuilder.append(Text.create(text, style));
        return this;
    }

    // -----

    public MarkdownBuilder quote(IMarkdown quote) {
        stringBuilder.append(Quote.create(quote));
        return this;
    }

    public MarkdownBuilder quote(String quote) {
        stringBuilder.append(Quote.create(quote));
        return this;
    }

    // -----

    public MarkdownBuilder link(IMarkdown name, String url) {
        stringBuilder.append(Link.create(name, url));
        return this;
    }

    public MarkdownBuilder link(String name, String url) {
        stringBuilder.append(Link.create(name, url));
        return this;
    }

    // -----

    public MarkdownBuilder image(String url) {
        stringBuilder.append(Image.create(url));
        return this;
    }

    public MarkdownBuilder image(String alt, String url) {
        stringBuilder.append(Image.create(alt, url));
        return this;
    }

    public MarkdownBuilder image(String alt, String url, int zoom) {
        stringBuilder.append(Image.create(alt, url, zoom));
        return this;
    }

    // -----

    public MarkdownBuilder code(IMarkdown code) {
        stringBuilder.append(Code.create(code));
        return this;
    }

    public MarkdownBuilder code(String code) {
        stringBuilder.append(Code.create(code));
        return this;
    }

    public MarkdownBuilder code(IMarkdown code, String language) {
        stringBuilder.append(Code.create(code, language));
        return this;
    }

    public MarkdownBuilder code(String code, String language) {
        stringBuilder.append(Code.create(code, language));
        return this;
    }

    // -----

    @Override
    public String toMarkdown() {
        return stringBuilder.toString();
    }

    @Override
    public String toString() {
        return toMarkdown();
    }
}
