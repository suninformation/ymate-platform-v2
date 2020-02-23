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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/02/09 14:35
 */
public final class ParagraphList implements IMarkdown {

    private final List<Serializable> items = new ArrayList<>();

    private boolean order;

    public static ParagraphList create() {
        return new ParagraphList();
    }

    public static ParagraphList create(boolean order) {
        return new ParagraphList(order);
    }

    private ParagraphList() {
    }

    private ParagraphList(boolean order) {
        this.order = order;
    }

    public ParagraphList addItem(String item) {
        if (StringUtils.isNotBlank(item)) {
            items.add(item);
        }
        return this;
    }

    public ParagraphList addItems(String... items) {
        if (items != null) {
            return addItems(Arrays.asList(items));
        }
        return this;
    }

    public ParagraphList addItems(List<String> items) {
        if (items != null && !items.isEmpty()) {
            items.stream().filter(StringUtils::isNotBlank).forEachOrdered(this.items::add);
        }
        return this;
    }

    public ParagraphList addSubItem(ParagraphList subItem) {
        this.items.add(subItem);
        return this;
    }

    public ParagraphList addSubItem(String subItem) {
        items.add(new ParagraphList(order).addItem(subItem));
        return this;
    }

    public ParagraphList addSubItems(String... subItems) {
        this.items.add(new ParagraphList(order).addItems(subItems));
        return this;
    }

    public ParagraphList addSubItems(List<String> subItems) {
        this.items.add(new ParagraphList(order).addItems(subItems));
        return this;
    }

    public ParagraphList addBody(IMarkdown body) {
        return addBody(body.toMarkdown());
    }

    public ParagraphList addBody(String body) {
        this.items.add(new Body(body));
        return this;
    }

    @Override
    public String toMarkdown() {
        StringBuilder stringBuilder = new StringBuilder();
        int idx = 1;
        for (Object object : items) {
            if (object instanceof String) {
                stringBuilder.append(order ? String.format("%d. ", idx) : "- ").append(StringUtils.replaceEach((String) object, new String[]{"\r\n", "\r", "\n", "\t"}, new String[]{StringUtils.SPACE, StringUtils.EMPTY, StringUtils.SPACE, TAB})).append(P);
            } else if (object instanceof ParagraphList) {
                Arrays.stream(StringUtils.split(((ParagraphList) object).toMarkdown(), P))
                        .forEachOrdered(item -> stringBuilder.append(TAB).append(item).append(P));
            } else if (object instanceof Body) {
                stringBuilder.append(P).append(((Body) object).body).append(P);
            }
            idx++;
        }
        return stringBuilder.toString();
    }

    @Override
    public String toString() {
        return toMarkdown();
    }

    private static class Body implements Serializable {

        String body;

        Body(String body) {
            this.body = body;
        }
    }
}
