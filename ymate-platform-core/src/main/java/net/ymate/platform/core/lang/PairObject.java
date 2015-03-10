/*
 * Copyright 2007-2107 the original author or authors.
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
package net.ymate.platform.core.lang;

import java.io.Serializable;

/**
 * <p>
 * PairObject
 * </p>
 * <p>
 * 结对对象类型；
 * </p>
 *
 * @author 刘镇 (suninformation@163.com)
 * @version 0.0.0
 *          <table style="border:1px solid gray;">
 *          <tr>
 *          <th width="100px">版本号</th><th width="100px">动作</th><th
 *          width="100px">修改人</th><th width="100px">修改时间</th>
 *          </tr>
 *          <!-- 以 Table 方式书写修改历史 -->
 *          <tr>
 *          <td>0.0.0</td>
 *          <td>创建类</td>
 *          <td>刘镇</td>
 *          <td>2010-4-17上午12:07:42</td>
 *          </tr>
 *          </table>
 */
public class PairObject<K, V> implements Serializable, Cloneable {

    /**
     *
     */
    private static final long serialVersionUID = -6239279408656130276L;

    /**
     * 对数据中的键
     */
    private K key;

    /**
     * 对数据中的值
     */
    private V value;

    /**
     * 构造器
     */
    public PairObject() {
    }

    /**
     * 构造器
     *
     * @param key
     */
    public PairObject(K key) {
        this.key = key;
    }

    /**
     * 构造器
     *
     * @param key
     * @param value
     */
    public PairObject(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PairObject) {
            PairObject<?, ?> _o = (PairObject<?, ?>) obj;
            if (this.getKey().equals(_o.getKey()) && this.getValue().equals(_o.getValue())) {
                return true;
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "{" + "key : '" + this.key + "', value : '" + this.value + "'}";
    }

}
