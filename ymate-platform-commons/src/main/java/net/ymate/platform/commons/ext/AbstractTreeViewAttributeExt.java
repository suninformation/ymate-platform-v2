/*
 * Copyright 2007-2025 the original author or authors.
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
package net.ymate.platform.commons.ext;

import net.ymate.platform.commons.lang.BlurObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.*;

/**
 * (提取自 ymate-module-security 模块)
 *
 * @author 刘镇 (suninformation@163.com) on 2025/5/23 21:43
 * @since 2.1.4
 */
public abstract class AbstractTreeViewAttributeExt<ID extends Serializable, CHILD_TYPE extends ITreeViewExt<ID, ? extends CHILD_TYPE>> extends AbstractAttributeExt
        implements ITreeViewExt<ID, CHILD_TYPE> {

    private static final long serialVersionUID = 1L;

    private final Map<ID, CHILD_TYPE> children = new LinkedHashMap<>();

    @Override
    public Collection<CHILD_TYPE> getChildren() {
        return Collections.unmodifiableCollection(children.values());
    }

    @Override
    public void addChild(CHILD_TYPE child) {
        if (child != null && StringUtils.isNotBlank(BlurObject.bind(child.getId()).toStringValue())) {
            children.put(child.getId(), child);
        }
    }

    @Override
    public final void removeChildren() {
        children.clear();
    }

    @Override
    public final void removeChildren(Set<ID> ids) {
        if (ids != null && !ids.isEmpty()) {
            ids.stream()
                    .filter(id -> StringUtils.isNotBlank(BlurObject.bind(id).toStringValue()))
                    .forEach(children::remove);
        }
    }

    @Override
    public boolean hasChild(ID id) {
        return StringUtils.isNotBlank(BlurObject.bind(id).toStringValue()) && children.containsKey(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AbstractTreeViewAttributeExt<?, ?> that = (AbstractTreeViewAttributeExt<?, ?>) o;
        return new EqualsBuilder().append(getId(), that.getId()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getId()).toHashCode();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
    }
}
