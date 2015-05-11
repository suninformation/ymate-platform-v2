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
 * @author 刘镇 (suninformation@163.com) on 15/5/9 下午8:12
 * @version 1.0
 */
public class Cond {

    private StringBuilder __condSB;

    /**
     * SQL参数集合
     */
    private Params __params;

    public static Cond create() {
        return new Cond();
    }

    private Cond() {
        __condSB = new StringBuilder();
        __params = Params.create();
    }

    public Params getParams() {
        return this.__params;
    }

    public Cond addParam(Object param) {
        this.__params.add(param);
        return this;
    }

    public Cond eq(String field) {
        __condSB.append(field).append(" = ?");
        return this;
    }

    public Cond and() {
        __doAppendCond("AND");
        return this;
    }

    public Cond or() {
        __doAppendCond("OR");
        return this;
    }

    private void __doAppendCond(String cond) {
        if (__condSB.length() > 0) {
            __condSB.append(" ").append(cond).append(" ");
        }
    }

    @Override
    public String toString() {
        return __condSB.toString();
    }
}
