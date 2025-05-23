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

import java.io.Serializable;
import java.util.Collection;

/**
 * 支持树型层级结构扩展
 * (提取自 ymate-module-security 模块)
 *
 * @author 刘镇 (suninformation@163.com) on 2025/5/23 21:31
 * @since 2.1.4
 */
public interface ITreeViewExt<ID extends Serializable, CHILD_TYPE extends ITreeViewExt<ID, ? extends CHILD_TYPE>> extends Serializable {

    /**
     * @return 返回节点唯一标识
     */
    ID getId();

    /**
     * @return 返回根节点唯一标识
     */
    ID getRootId();

    /**
     * @return 返回父级节点唯一标识
     */
    ID getParentId();

    /**
     * @return 返回当前节点层级路径（各节点唯一标识采用'|'分隔）
     */
    String getPath();

    /**
     * @return 返回当前节点层级深度
     */
    Long getDepth();

    /**
     * @return 返回子节点集合
     */
    Collection<CHILD_TYPE> getChildren();

    /**
     * 添加子节点
     *
     * @param child 子节点
     */
    void addChild(CHILD_TYPE child);

    /**
     * 删除子节点
     *
     * @param id 子节点唯一标识集合（为空时表示清空）
     */
    void removeChildren(ID... id);

    /**
     * 判断指定名称的属性是否已存在
     *
     * @param id 子节点唯一标识
     * @return 返回true表示已存在
     */
    boolean hasChild(ID id);
}
