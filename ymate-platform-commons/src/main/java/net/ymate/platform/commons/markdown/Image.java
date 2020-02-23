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
 * @author 刘镇 (suninformation@163.com) on 2020/02/09 13:52
 */
public final class Image implements IMarkdown {

    private String alt;

    private String url;

    private int zoom;

    public static Image create(String url) {
        return new Image(url);
    }

    public static Image create(String alt, String url) {
        return new Image(alt, url);
    }

    public static Image create(String alt, String url, int zoom) {
        return new Image(alt, url, zoom);
    }

    private Image(String url) {
        this(null, url, 0);
    }

    private Image(String alt, String url) {
        this(alt, url, 0);
    }

    private Image(String alt, String url, int zoom) {
        this.alt = StringUtils.trimToEmpty(alt);
        this.url = StringUtils.trimToEmpty(url);
        this.zoom = zoom < 0 ? 0 : Math.min(zoom, 200);
    }

    @Override
    public String toMarkdown() {
        if (StringUtils.isBlank(url)) {
            return StringUtils.EMPTY;
        }
        if (zoom == 0) {
            return String.format("![%s](%s)", alt, url);
        }
        return String.format("<img src=\"%s\" alt=\"%s\" style=\"zoom:%d%%;\" />", url, alt, zoom);
    }

    @Override
    public String toString() {
        return toMarkdown();
    }
}
