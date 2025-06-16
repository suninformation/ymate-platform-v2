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

/**
 * 树型层级结构扩展构建器接口定义
 *
 * @author 刘镇 (suninformation@163.com) on 2025/6/16 13:21
 * @since 2.1.4
 */
public interface ITreeViewExtBuilder<ID extends Serializable, T> extends Serializable {

    ID id();

    T id(ID id);

    ID rootId();

    T rootId(ID rootId);

    ID parentId();

    T parentId(ID rootId);

    String path();

    T path(String path);

    Long depth();

    T depth(Long depth);
}
