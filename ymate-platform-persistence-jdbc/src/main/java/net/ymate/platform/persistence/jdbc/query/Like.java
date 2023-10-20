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
package net.ymate.platform.persistence.jdbc.query;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/07/12 18:09
 * @since 2.1.0
 */
public class Like implements Serializable {

    public static Like create(String originStr) {
        return new Like(originStr);
    }

    private final String originStr;

    public Like(String originStr) {
        this.originStr = StringUtils.replaceEach(originStr, new String[]{"\\", "%", "_"}, new String[]{"\\\\", "\\%", "\\_"});
    }

    public String contains() {
        return String.format("%%%s%%", originStr);
    }

    public String endsWith() {
        return String.format("%%%s", originStr);
    }

    public String startsWith() {
        return String.format("%s%%", originStr);
    }

    @Override
    public String toString() {
        return originStr;
    }
}
