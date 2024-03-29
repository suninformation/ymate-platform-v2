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
package net.ymate.platform.cache.support;

import java.io.Serializable;

/**
 * @author 刘镇 (suninformation@163.com) on 15/12/7 上午9:15
 */
public class MultilevelKey implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Object key;

    private final boolean master;

    public static MultilevelKey bind(Object keyObj) {
        if (keyObj instanceof MultilevelKey) {
            return (MultilevelKey) keyObj;
        }
        return new MultilevelKey(keyObj);
    }

    public static Object unbind(Object keyObj) {
        if (keyObj instanceof MultilevelKey) {
            return ((MultilevelKey) keyObj).getKey();
        }
        return keyObj;
    }

    public MultilevelKey(Object key, boolean master) {
        this.key = key;
        this.master = master;
    }

    public MultilevelKey(Object key) {
        this(key, true);
    }

    public Object getKey() {
        return key;
    }

    public boolean isMaster() {
        return master;
    }

    @Override
    public String toString() {
        return String.valueOf(key);
    }
}
