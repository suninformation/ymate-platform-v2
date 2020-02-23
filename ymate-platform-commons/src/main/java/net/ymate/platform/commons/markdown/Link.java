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
 * @author 刘镇 (suninformation@163.com) on 2020/02/09 13:43
 */
public final class Link implements IMarkdown {

    private String name;

    private String url;

    public static Link create(String url) {
        return new Link((String) null, url);
    }

    public static Link create(IMarkdown name, String url) {
        return new Link(name, url);
    }

    public static Link create(String name, String url) {
        return new Link(name, url);
    }

    private Link(IMarkdown name, String url) {
        this(name.toMarkdown(), url);
    }

    private Link(String name, String url) {
        this.name = StringUtils.trimToEmpty(name);
        this.url = StringUtils.trimToEmpty(url);
    }

    @Override
    public String toMarkdown() {
        if (StringUtils.isBlank(url)) {
            return StringUtils.EMPTY;
        }
        return String.format("[%s](%s)", StringUtils.defaultIfBlank(name, url), url);
    }

    @Override
    public String toString() {
        return toMarkdown();
    }
}
