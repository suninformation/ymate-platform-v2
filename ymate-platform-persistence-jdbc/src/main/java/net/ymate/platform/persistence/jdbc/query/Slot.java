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
package net.ymate.platform.persistence.jdbc.query;

import net.ymate.platform.core.persistence.IFunction;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 扩展槽
 *
 * @author 刘镇 (suninformation@163.com) on 2019-11-20 13:46
 * @since 2.1.0
 */
public class Slot {

    private final List<CharSequence> contents = new ArrayList<>();

    public Slot addSlotContent(CharSequence charSequence) {
        contents.add(charSequence);
        return this;
    }

    public Slot addSlotContent(IFunction function) {
        contents.add(function.build());
        return this;
    }

    public boolean hasSlotContent() {
        return !contents.isEmpty();
    }

    public List<CharSequence> getSlotContents() {
        return contents;
    }

    public String buildSlot() {
        return StringUtils.join(contents, StringUtils.SPACE);
    }
}
