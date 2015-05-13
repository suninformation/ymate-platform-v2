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
package net.ymate.platform.persistence.jdbc.query;

/**
 * 连接查询语句对象
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/12 下午6:04
 * @version 1.0
 */
public class Join {

    private String __from;

    private Cond __on;

    public static Join inner(String from) {
        return new Join("INNER JOIN ", from);
    }

    public static Join inner(Select select) {
        return inner(select.toString());
    }

    public static Join left(String from) {
        return new Join("LEFT JOIN ", from);
    }

    public static Join right(String from) {
        return new Join("RIGHT JOIN ", from);
    }

    private Join(String type, String from) {
        __from = type.concat(" ").concat(from);
    }

    public Join on(Cond cond) {
        __on = cond;
        return this;
    }

    public Params getParams() {
        return __on.getParams();
    }

    @Override
    public String toString() {
        return new StringBuilder(__from).append(" ON ").append(__on.toString()).toString();
    }
}
