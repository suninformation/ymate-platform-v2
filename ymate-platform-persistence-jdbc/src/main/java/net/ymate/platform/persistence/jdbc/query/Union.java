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

/**
 * @author 刘镇 (suninformation@163.com) on 15/9/25 13:23
 */
public final class Union {

    private final Select select;

    private boolean all;

    public static Union create(Select select) {
        return new Union(select);
    }

    public Union(Select select) {
        this.select = select;
    }

    public Union all() {
        all = true;
        return this;
    }

    public boolean isAll() {
        return all;
    }

    public Select select() {
        return select;
    }

}
